package org.zerock.service;

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.zerock.domain.*;
import org.zerock.mapper.DailyReviewMapper;

/** Opt in with -DmenuReview.dbTest=true -DdailyReview.dbTest=true. Only UUID fixtures are removed. */
public class DailyReviewOracleIT {
    private static DailyReviewMapper mapper;
    private MenuReviewOracleIT fixture;
    private DailyReviewService service;
    private int baselineCount;
    private final List<String> extraUsers = new ArrayList<>();

    @BeforeClass public static void connect() throws Exception {
        Assume.assumeTrue(Boolean.getBoolean("dailyReview.dbTest"));
        MenuReviewOracleIT.connect();
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean(); factory.setDataSource(MenuReviewOracleIT.dataSource);
        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration(); config.addMapper(DailyReviewMapper.class);
        factory.setConfiguration(config); mapper = new SqlSessionTemplate(factory.getObject()).getMapper(DailyReviewMapper.class);
    }
    @Before public void setup() {
        fixture = new MenuReviewOracleIT(); fixture.fixture();
        ProxyFactory proxy = new ProxyFactory(new DailyReviewServiceImpl(mapper));
        proxy.addAdvice(new TransactionInterceptor(new DataSourceTransactionManager(MenuReviewOracleIT.dataSource),new AnnotationTransactionAttributeSource()));
        service = (DailyReviewService)proxy.getProxy(); baselineCount = service.getPage(1).getSummary().getReviewCount();
    }
    @After public void cleanup() {
        if (fixture == null) return;
        List<String> users = new ArrayList<>(extraUsers); users.add(fixture.alice); users.add(fixture.bob);
        for (String user : users) if (user != null) MenuReviewOracleIT.jdbc.update("DELETE FROM tbl_daily_review WHERE userid=?",user);
        for (String user : extraUsers) MenuReviewOracleIT.jdbc.update("DELETE FROM tbl_member WHERE userid=?",user);
        fixture.cleanup();
    }
    private Long ownId() { return service.getPage(1).getOwnReview().getDailyReviewId(); }
    private void expect(HttpStatus status, Runnable action) { fixture.expect(status,action); }

    @Test public void wholeMealAndFoodReviewsNeverAffectEachOther() {
        service.register(4,"whole meal"); Long dailyId=ownId();
        assertEquals(0,fixture.storedAverage(),0);
        DailyReviewSummary before=service.getPage(1).getSummary();
        assertEquals(baselineCount+1,before.getReviewCount());
        fixture.service.register(fixture.menuId,5,"one food");
        assertEquals(5,fixture.storedAverage(),0);
        assertEquals(before,service.getPage(1).getSummary());
        assertEquals("one food",fixture.service.getPage(fixture.menuId,1).getReviews().get(0).getReviewText());
        assertEquals("whole meal",mapper.read(dailyId).getReviewText());
        service.modify(dailyId,2,"changed whole meal"); assertEquals(5,fixture.storedAverage(),0);
        service.remove(dailyId); assertEquals(5,fixture.storedAverage(),0);
        service.register(3,"re-register after deletion");
        assertEquals(baselineCount+1,service.getPage(1).getSummary().getReviewCount());
    }
    @Test public void ownershipOldDatesAndTodayFilteringAreEnforced() {
        service.register(4,"yesterday fixture"); Long id=ownId();
        fixture.login(fixture.bob);
        expect(HttpStatus.FORBIDDEN,()->service.modify(id,1,"attack")); expect(HttpStatus.FORBIDDEN,()->service.remove(id));
        fixture.login(fixture.alice);
        MenuReviewOracleIT.jdbc.update("UPDATE tbl_daily_review SET serve_date=serve_date-1,regdate=regdate-1 WHERE daily_review_id=?",id);
        assertFalse(mapper.read(id).isEditable()); assertNull(service.getPage(1).getOwnReview());
        assertEquals(baselineCount,service.getPage(1).getSummary().getReviewCount());
        expect(HttpStatus.FORBIDDEN,()->service.modify(id,1,"late"));
        service.register(5,"today fixture"); assertEquals(baselineCount+1,service.getPage(1).getSummary().getReviewCount());
        service.remove(id); assertNull(mapper.read(id));
    }
    @Test public void databaseConstraintsStopBypassedDuplicateInvalidScoreAndTime() {
        service.register(4,"first"); Long id=ownId();
        expect(HttpStatus.CONFLICT,()->service.register(5,"duplicate"));
        for (String sql : Arrays.asList(
                "INSERT INTO tbl_daily_review(daily_review_id,serve_date,userid,star_score,review_text) SELECT seq_daily_review.NEXTVAL,serve_date,userid,5,'duplicate' FROM tbl_daily_review WHERE daily_review_id=?",
                "UPDATE tbl_daily_review SET star_score=6 WHERE daily_review_id=?",
                "UPDATE tbl_daily_review SET serve_date=serve_date+0.5 WHERE daily_review_id=?",
                "UPDATE tbl_daily_review SET review_text='   ' WHERE daily_review_id=?")) {
            try { MenuReviewOracleIT.jdbc.update(sql,id); fail("Constraint missing"); }
            catch (DataIntegrityViolationException expected) { assertNotNull(mapper.read(id)); }
        }
    }
    @Test public void unicodeContentAndThousandCharactersRoundTrip() {
        String text=String.join("",Collections.nCopies(1000,"김")); service.register(5,text); Long id=ownId();
        assertEquals(text,mapper.read(id).getReviewText());
        service.modify(id,4,"<script>alert('x')</script>"); assertNotNull(mapper.read(id).getUpdateDate());
        assertEquals("<script>alert('x')</script>",mapper.read(id).getReviewText());
    }
    @Test public void midnightGuardRejectsStaleServerDateAtInsert() {
        DailyReviewVO stale = new DailyReviewVO(); stale.setUserid(fixture.alice); stale.setStarScore(4); stale.setReviewText("stale day");
        stale.setServeDate(new Date(mapper.today().getTime()-86400000L));
        assertEquals(0,mapper.insert(stale)); assertNull(mapper.read(stale.getDailyReviewId()));
    }
    @Test public void transactionRollbackDoesNotLeaveReview() {
        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(MenuReviewOracleIT.dataSource));
        transaction.execute(status -> { service.register(4,"rollback fixture"); status.setRollbackOnly(); return null; });
        assertNull(service.getPage(1).getOwnReview());
    }
    @Test public void pagingReturnsTenNewestAndOwnReviewOnLaterPages() {
        service.register(3,"owner"); Long own=ownId();
        for (int i=0;i<11;i++) {
            String user="daily_it_"+UUID.randomUUID().toString().replace("-","").substring(0,20); extraUsers.add(user);
            MenuReviewOracleIT.jdbc.update("INSERT INTO tbl_member(userid,userpw,username,enabled) VALUES(?,'disabled-test-account','daily fixture','0')",user);
            fixture.login(user); service.register(4,"page-"+i);
        }
        fixture.login(fixture.alice);
        DailyReviewPage first=service.getPage(1),second=service.getPage(2);
        assertEquals(10,first.getReviews().size()); assertEquals(own,second.getOwnReview().getDailyReviewId());
        assertEquals(baselineCount+12,first.getSummary().getReviewCount());
        Set<Long> ids=new HashSet<>(); for(DailyReviewVO r:first.getReviews())assertTrue(ids.add(r.getDailyReviewId()));
        for(DailyReviewVO r:second.getReviews())assertTrue(ids.add(r.getDailyReviewId()));
        assertTrue(first.getReviews().get(0).getDailyReviewId()>first.getReviews().get(9).getDailyReviewId());
    }
    @Test public void simultaneousRegistrationsHaveOneWinner() throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(2); CountDownLatch ready=new CountDownLatch(2),go=new CountDownLatch(1);
        try {
            List<Future<Integer>> results=new ArrayList<>();
            for(int i=0;i<2;i++) results.add(pool.submit(()-> {
                fixture.login(fixture.alice); ready.countDown(); go.await(5,TimeUnit.SECONDS);
                try {service.register(4,"parallel");return 200;} catch(ResponseStatusException e){return e.getStatus().value();}
                finally{SecurityContextHolder.clearContext();}
            }));
            assertTrue(ready.await(5,TimeUnit.SECONDS));go.countDown();List<Integer> statuses=new ArrayList<>();
            for(Future<Integer> result:results)statuses.add(result.get(20,TimeUnit.SECONDS));Collections.sort(statuses);
            assertEquals(Arrays.asList(200,409),statuses); assertEquals(baselineCount+1,service.getPage(1).getSummary().getReviewCount());
        } finally {go.countDown();pool.shutdownNow();}
    }
}
