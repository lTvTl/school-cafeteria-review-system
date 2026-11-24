package org.zerock.service;

import static org.junit.Assert.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.zerock.domain.*;
import org.zerock.mapper.*;

/** Opt-in local Oracle integration checks. Only random fixture records are cleaned up. */
public class BookmarkOracleIT {
    private static BookmarkMapper mapper;
    private static DailyDietMapper diets;
    private MenuReviewOracleIT fixture;
    private BookmarkService service;
    private Long dietId;

    @BeforeClass public static void connect() throws Exception {
        Assume.assumeTrue(Boolean.getBoolean("bookmark.dbTest"));MenuReviewOracleIT.connect();
        SqlSessionFactoryBean factory=new SqlSessionFactoryBean();factory.setDataSource(MenuReviewOracleIT.dataSource);
        org.apache.ibatis.session.Configuration config=new org.apache.ibatis.session.Configuration();config.addMapper(BookmarkMapper.class);config.addMapper(DailyDietMapper.class);factory.setConfiguration(config);
        SqlSessionTemplate session=new SqlSessionTemplate(factory.getObject());mapper=session.getMapper(BookmarkMapper.class);diets=session.getMapper(DailyDietMapper.class);
    }
    @Before public void setup(){fixture=new MenuReviewOracleIT();fixture.fixture();service=transactional(mapper);}
    private BookmarkService transactional(BookmarkMapper delegate) {
        ProxyFactory proxy=new ProxyFactory(new BookmarkServiceImpl(MenuReviewOracleIT.menus,delegate));
        proxy.addAdvice(new TransactionInterceptor(new DataSourceTransactionManager(MenuReviewOracleIT.dataSource),new AnnotationTransactionAttributeSource()));return (BookmarkService)proxy.getProxy();
    }
    @After public void cleanup(){
        if(fixture==null)return;
        if(dietId!=null)MenuReviewOracleIT.jdbc.update("DELETE FROM tbl_daily_diet WHERE diet_id=?",dietId);
        for(Long id:Arrays.asList(fixture.menuId,fixture.otherMenuId))if(id!=null)MenuReviewOracleIT.jdbc.update("DELETE FROM tbl_bookmark WHERE menu_id=?",id);
        fixture.cleanup();
    }
    private int storedCount(){return MenuReviewOracleIT.jdbc.queryForObject("SELECT bookmark_cnt FROM tbl_menu WHERE menu_id=?",Integer.class,fixture.menuId);}
    @Test public void addRemoveAndRepeatedRequestsKeepAuthoritativeCount() {
        MenuReviewOracleIT.jdbc.update("UPDATE tbl_menu SET bookmark_cnt=99 WHERE menu_id=?",fixture.menuId);
        assertEquals(1,service.add(fixture.menuId).getBookmarkCnt());assertEquals(1,service.add(fixture.menuId).getBookmarkCnt());
        assertEquals(1,storedCount());assertEquals(1,service.getMyBookmarks().size());
        assertFalse(service.remove(fixture.menuId).isBookmarked());assertEquals(0,service.remove(fixture.menuId).getBookmarkCnt());assertEquals(0,storedCount());
    }
    @Test public void usersAreIndependentAndCategoryLoadsBothStateAndCount() {
        service.add(fixture.menuId);fixture.login(fixture.bob);assertFalse(service.getStatus(fixture.menuId).isBookmarked());
        assertEquals(1,service.getStatus(fixture.menuId).getBookmarkCnt());assertEquals(1,service.remove(fixture.menuId).getBookmarkCnt());
        assertEquals(2,service.add(fixture.menuId).getBookmarkCnt());fixture.login(fixture.alice);service.remove(fixture.menuId);
        for(String user:Arrays.asList(null,fixture.alice,fixture.bob)) {
            MenuVO row=MenuReviewOracleIT.menus.getListByCategory("MAIN",user).stream().filter(m->m.getMenuId().equals(fixture.menuId)).findFirst().get();
            assertEquals(1,row.getBookmarkCnt());assertEquals(fixture.bob.equals(user),row.isBookmarked());
        }
        assertEquals(1,storedCount());
    }
    @Test public void missingMenuAndAnonymousWritesAreBlocked() {
        fixture.expect(HttpStatus.NOT_FOUND,()->service.add(Long.MAX_VALUE));fixture.expect(HttpStatus.NOT_FOUND,()->service.remove(Long.MAX_VALUE));
        SecurityContextHolder.clearContext();fixture.expect(HttpStatus.UNAUTHORIZED,()->service.add(fixture.menuId));fixture.expect(HttpStatus.UNAUTHORIZED,()->service.remove(fixture.menuId));assertEquals(0,storedCount());
    }
    @Test public void uniqueConstraintStopsOutOfServiceDuplicate() {
        service.add(fixture.menuId);
        try{MenuReviewOracleIT.jdbc.update("INSERT INTO tbl_bookmark(bookmark_id,userid,menu_id) VALUES(seq_bookmark.NEXTVAL,?,?)",fixture.alice,fixture.menuId);fail("Missing UNIQUE");}
        catch(DuplicateKeyException expected){assertEquals(1,storedCount());assertEquals(1,mapper.countByMenu(fixture.menuId));}
    }
    @Test public void countFailureRollsBackInsertAndDelete() {
        BookmarkMapper failing=(BookmarkMapper)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{BookmarkMapper.class},(p,m,a)->{
            if(m.getName().equals("refreshCount"))throw new IllegalStateException("rollback check");
            try{return m.invoke(mapper,a);}catch(InvocationTargetException e){throw e.getCause();}
        });
        try{transactional(failing).add(fixture.menuId);fail("Expected rollback");}catch(IllegalStateException expected){assertEquals(0,mapper.countByMenu(fixture.menuId));assertEquals(0,storedCount());}
        service.add(fixture.menuId);
        try{transactional(failing).remove(fixture.menuId);fail("Expected rollback");}catch(IllegalStateException expected){assertEquals(1,mapper.countByMenu(fixture.menuId));assertEquals(1,storedCount());}
    }
    @Test public void bookmarkDoesNotChangeFoodReviewsAndDietReadsTheSameState() {
        fixture.service.register(fixture.menuId,4,"unchanged food review");service.add(fixture.menuId);
        assertEquals(4,fixture.storedAverage(),0);assertEquals(1,fixture.service.getSummary(fixture.menuId).getReviewCount());
        dietId=MenuReviewOracleIT.jdbc.queryForObject("SELECT seq_diet.NEXTVAL FROM dual",Long.class);
        MenuReviewOracleIT.jdbc.update("INSERT INTO tbl_daily_diet(diet_id,serve_date,meal_time,planned_menu_id) VALUES(?,TRUNC(SYSDATE),'LUNCH',?)",dietId,fixture.menuId);
        assertTrue(diets.getDietList(0,fixture.alice).stream().filter(d->d.getDietId().equals(dietId)).findFirst().get().isBookmarked());
        assertFalse(diets.getDietList(0,fixture.bob).stream().filter(d->d.getDietId().equals(dietId)).findFirst().get().isBookmarked());
        service.remove(fixture.menuId);assertFalse(diets.getDietList(0,fixture.alice).stream().filter(d->d.getDietId().equals(dietId)).findFirst().get().isBookmarked());assertEquals(4,fixture.storedAverage(),0);
    }
    private void simultaneous(String first, String second, boolean removeFirst) throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch ready=new CountDownLatch(2),go=new CountDownLatch(1);
        try{
            List<Future<?>> futures=new ArrayList<>();String[] users={first,second};
            for(int i=0;i<2;i++){final String user=users[i];final boolean remove=removeFirst&&i==0;futures.add(pool.submit(()->{
                fixture.login(user);ready.countDown();try{assertTrue(go.await(5,TimeUnit.SECONDS));if(remove)service.remove(fixture.menuId);else service.add(fixture.menuId);}finally{SecurityContextHolder.clearContext();}return null;
            }));}
            assertTrue(ready.await(5,TimeUnit.SECONDS));go.countDown();for(Future<?> future:futures)future.get(20,TimeUnit.SECONDS);
            assertEquals(mapper.countByMenu(fixture.menuId),storedCount());
        }finally{go.countDown();pool.shutdownNow();}
    }
    @Test public void concurrentDuplicateHasExactlyOneRow() throws Exception {simultaneous(fixture.alice,fixture.alice,false);assertEquals(1,storedCount());}
    @Test public void concurrentDifferentUsersAndMixedMutationsKeepCount() throws Exception {
        simultaneous(fixture.alice,fixture.bob,false);assertEquals(2,storedCount());
        fixture.login(fixture.bob);service.remove(fixture.menuId);simultaneous(fixture.alice,fixture.bob,true);assertEquals(1,storedCount());
    }
}
