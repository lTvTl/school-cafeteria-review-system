package org.zerock.service;

import static org.junit.Assert.*;
import java.io.File;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.*;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.*;
import org.zerock.domain.*;
import org.zerock.mapper.*;

/** Opt-in Oracle integration tests. Only random, test-owned fixture rows are removed. */
public class MenuReviewOracleIT {
    protected static DataSource dataSource;
    protected static JdbcTemplate jdbc;
    protected static MenuMapper menus;
    protected static MenuReviewMapper mapper;
    protected MenuReviewService service;
    protected Long menuId;
    protected Long otherMenuId;
    protected String alice;
    protected String bob;

    @BeforeClass public static void connect() throws Exception {
        Assume.assumeTrue(Boolean.getBoolean("menuReview.dbTest"));
        DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        Document doc=f.newDocumentBuilder().parse(new File("src/main/webapp/WEB-INF/spring/root-context.xml"));
        Properties values=new Properties(); NodeList beans=doc.getElementsByTagName("bean");
        for(int i=0;i<beans.getLength();i++){Element b=(Element)beans.item(i);if("hikariConfig".equals(b.getAttribute("id"))){NodeList props=b.getElementsByTagName("property");for(int j=0;j<props.getLength();j++){Element p=(Element)props.item(j);values.setProperty(p.getAttribute("name"),p.getAttribute("value"));}}}
        DriverManagerDataSource ds=new DriverManagerDataSource();
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setUrl(values.getProperty("jdbcUrl").replace("jdbc:log4jdbc:","jdbc:"));
        ds.setUsername(values.getProperty("username"));ds.setPassword(values.getProperty("password"));
        Properties timeouts=new Properties();timeouts.setProperty("oracle.net.CONNECT_TIMEOUT","5000");timeouts.setProperty("oracle.jdbc.ReadTimeout","15000");ds.setConnectionProperties(timeouts);
        dataSource=ds;jdbc=new JdbcTemplate(ds);
        SqlSessionFactoryBean factory=new SqlSessionFactoryBean();factory.setDataSource(ds);
        org.apache.ibatis.session.Configuration config=new org.apache.ibatis.session.Configuration();
        config.addMapper(MenuMapper.class);config.addMapper(MenuReviewMapper.class);factory.setConfiguration(config);
        SqlSessionTemplate session=new SqlSessionTemplate(factory.getObject());
        menus=session.getMapper(MenuMapper.class);mapper=session.getMapper(MenuReviewMapper.class);
    }

    protected MenuReviewService transactional(MenuReviewMapper reviewMapper) {
        ProxyFactory proxy=new ProxyFactory(new MenuReviewServiceImpl(menus,reviewMapper));
        proxy.addAdvice(new TransactionInterceptor(new DataSourceTransactionManager(dataSource),new AnnotationTransactionAttributeSource()));
        return (MenuReviewService)proxy.getProxy();
    }

    @Before public void fixture() {
        String suffix=UUID.randomUUID().toString().replace("-","").substring(0,20);
        alice="review_it_a_"+suffix;bob="review_it_b_"+suffix;
        for(String user:Arrays.asList(alice,bob))jdbc.update("INSERT INTO tbl_member(userid,userpw,username,enabled) VALUES(?,?,?,?)",user,"disabled-test-account","리뷰 테스트", "0");
        menuId=newMenu();otherMenuId=newMenu();service=transactional(mapper);login(alice);
    }
    protected Long newMenu() {
        Long id=jdbc.queryForObject("SELECT seq_menu.NEXTVAL FROM dual",Long.class);
        jdbc.update("INSERT INTO tbl_menu(menu_id,menu_name,category) VALUES(?,?,'MAIN')",id,"Review integration fixture");return id;
    }
    protected void login(String user) {SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,"unused",Collections.emptyList()));}
    protected void expect(HttpStatus status,Runnable action) {try{action.run();fail("Expected "+status);}catch(ResponseStatusException e){assertEquals(status,e.getStatus());}}
    protected Long reviewId() {return jdbc.queryForObject("SELECT review_id FROM tbl_menu_review WHERE menu_id=? AND userid=?",Long.class,menuId,alice);}
    protected double storedAverage() {return jdbc.queryForObject("SELECT avg_rating FROM tbl_menu WHERE menu_id=?",Double.class,menuId);}
    @After public void cleanup() {
        SecurityContextHolder.clearContext();
        for(Long id:Arrays.asList(menuId,otherMenuId))if(id!=null){jdbc.update("DELETE FROM tbl_menu_review WHERE menu_id=?",id);jdbc.update("DELETE FROM tbl_menu WHERE menu_id=? AND menu_name='Review integration fixture'",id);}
        for(String user:Arrays.asList(alice,bob))if(user!=null){jdbc.update("DELETE FROM tbl_member_auth WHERE userid=?",user);jdbc.update("DELETE FROM tbl_member WHERE userid=?",user);}
    }

    @Test public void persistsCrudAndAverageWithRealMapper() {
        service.register(menuId,5,"맛있어요");Long id=reviewId();
        assertEquals(5,storedAverage(),0);assertEquals(1,service.getSummary(menuId).getStar5());
        MenuReviewVO row=service.getPage(menuId,1).getReviews().get(0);assertEquals("리뷰 테스트",row.getUserName());assertTrue(row.isEditable());
        service.modify(menuId,id,3,"수정한 리뷰");assertEquals(3,storedAverage(),0);assertNotNull(mapper.read(id).getUpdateDate());
        service.remove(menuId,id);assertEquals(0,storedAverage(),0);assertEquals(0,service.getSummary(menuId).getReviewCount());
        assertNull(mapper.read(id));
        assertEquals("[deleted]",jdbc.queryForObject("SELECT review_text FROM tbl_menu_review WHERE review_id=?",String.class,id));
        expect(HttpStatus.CONFLICT,()->service.register(menuId,5,"cannot bypass daily limit after deletion"));
    }
    @Test public void enforcesDayAndOwnershipInDatabaseBackedService() {
        service.register(menuId,4,"original");Long id=reviewId();
        login(bob);expect(HttpStatus.FORBIDDEN,()->service.modify(menuId,id,2,"attack"));expect(HttpStatus.FORBIDDEN,()->service.remove(menuId,id));
        login(alice);expect(HttpStatus.NOT_FOUND,()->service.remove(otherMenuId,id));
        jdbc.update("UPDATE tbl_menu_review SET regdate=SYSDATE-1 WHERE review_id=?",id);
        assertFalse(mapper.read(id).isEditable());expect(HttpStatus.FORBIDDEN,()->service.modify(menuId,id,2,"late"));
        service.remove(menuId,id);assertEquals(0,storedAverage(),0);
    }
    @Test public void databaseUniqueIndexRejectsBypassedDuplicateInsert() {
        service.register(menuId,4,"first");
        expect(HttpStatus.CONFLICT,()->service.register(menuId,3,"duplicate"));
        try{jdbc.update("INSERT INTO tbl_menu_review(review_id,menu_id,userid,star_score,review_text) VALUES(seq_menu_review.NEXTVAL,?,?,5,'bypass')",menuId,alice);fail("unique index missing");}
        catch(DuplicateKeyException expected){assertEquals(1,service.getSummary(menuId).getReviewCount());}
    }
    @Test public void storesThousandKoreanCharactersAndEscapableTextUnmodified() {
        String text=String.join("",Collections.nCopies(1000,"김"));service.register(menuId,5,text);assertEquals(text,mapper.read(reviewId()).getReviewText());
        service.modify(menuId,reviewId(),4,"<script>alert('x')</script>");assertEquals("<script>alert('x')</script>",mapper.read(reviewId()).getReviewText());
    }
    @Test public void paginationIsStableAndMenuScoped() {
        for(int i=0;i<11;i++)jdbc.update("INSERT INTO tbl_menu_review(review_id,menu_id,userid,star_score,review_text,regdate) VALUES(seq_menu_review.NEXTVAL,?,?,5,?,SYSDATE-?)",menuId,alice,"page-"+i,i);
        service.register(otherMenuId,1,"different menu");
        MenuReviewPage first=service.getPage(menuId,1),second=service.getPage(menuId,2);
        assertEquals(10,first.getReviews().size());assertEquals(1,second.getReviews().size());assertEquals(2,first.getTotalPages());
        assertEquals("page-0",first.getReviews().get(0).getReviewText());assertEquals("page-10",second.getReviews().get(0).getReviewText());
        assertEquals(11,first.getSummary().getStar5());
    }
    @Test public void averageFailureRollsBackReviewInsertion() {
        MenuReviewMapper failing=(MenuReviewMapper)Proxy.newProxyInstance(MenuReviewMapper.class.getClassLoader(),new Class<?>[]{MenuReviewMapper.class},(p,m,a)->{
            if(m.getName().equals("refreshAverage"))throw new IllegalStateException("test rollback");
            try{return m.invoke(mapper,a);}catch(InvocationTargetException e){throw e.getCause();}
        });
        try{transactional(failing).register(menuId,5,"must rollback");fail("Expected failure");}catch(IllegalStateException expected){assertEquals("test rollback",expected.getMessage());}
        assertEquals(0,service.getSummary(menuId).getReviewCount());assertEquals(0,storedAverage(),0);
    }
    private List<Integer> concurrent(String first,String second) throws Exception {
        ExecutorService executor=Executors.newFixedThreadPool(2);CountDownLatch ready=new CountDownLatch(2),go=new CountDownLatch(1);
        try {
            List<Future<Integer>> futures=new ArrayList<>();int score=1;
            for(String user:Arrays.asList(first,second)){final int rating=score;score=5;futures.add(executor.submit(()->{
                login(user);ready.countDown();go.await(5,TimeUnit.SECONDS);
                try{service.register(menuId,rating,"parallel");return 200;}catch(ResponseStatusException e){return e.getStatus().value();}finally{SecurityContextHolder.clearContext();}
            }));}
            assertTrue(ready.await(5,TimeUnit.SECONDS));go.countDown();List<Integer> results=new ArrayList<>();for(Future<Integer> f:futures)results.add(f.get(20,TimeUnit.SECONDS));return results;
        } finally {go.countDown();executor.shutdownNow();}
    }
    @Test public void concurrentSameUserHasExactlyOneSuccess() throws Exception {
        List<Integer> results=concurrent(alice,alice);Collections.sort(results);assertEquals(Arrays.asList(200,409),results);assertEquals(1,service.getSummary(menuId).getReviewCount());
        assertEquals(service.getSummary(menuId).getAvgRating(),storedAverage(),0);
    }
    @Test public void concurrentDifferentUsersKeepAverageConsistent() throws Exception {
        assertEquals(Arrays.asList(200,200),concurrent(alice,bob));assertEquals(2,service.getSummary(menuId).getReviewCount());assertEquals(3,storedAverage(),0);
    }
}
