package org.zerock.service;

import static org.junit.Assert.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Opt-in browser/API test using UUID accounts and fixture menus only. */
public class BookmarkBrowserIT {
    @Test public void categoryHeartsAndReadOnlyHomeStateWorkInRealBrowser() throws Exception {
        Assume.assumeTrue(Boolean.getBoolean("bookmark.browserTest"));MenuReviewOracleIT.connect();
        MenuReviewOracleIT fixture=new MenuReviewOracleIT();Long dietId=null;
        try {
            fixture.fixture();String password=UUID.randomUUID().toString();String hash=new BCryptPasswordEncoder().encode(password);
            for(String user:Arrays.asList(fixture.alice,fixture.bob)) {
                MenuReviewOracleIT.jdbc.update("UPDATE tbl_member SET userpw=?,enabled='1' WHERE userid=?",hash,user);
                MenuReviewOracleIT.jdbc.update("INSERT INTO tbl_member_auth(userid,auth) VALUES(?,'ROLE_USER')",user);
            }
            String menuName="북마크 검증 음식 "+fixture.menuId;
            MenuReviewOracleIT.jdbc.update("UPDATE tbl_menu SET menu_name=? WHERE menu_id=?",menuName,fixture.menuId);
            MenuReviewOracleIT.jdbc.update("UPDATE tbl_menu SET category='SIDE' WHERE menu_id=?",fixture.otherMenuId);
            dietId=MenuReviewOracleIT.jdbc.queryForObject("SELECT seq_diet.NEXTVAL FROM dual",Long.class);
            MenuReviewOracleIT.jdbc.update("INSERT INTO tbl_daily_diet(diet_id,serve_date,meal_time,planned_menu_id) VALUES(?,TRUNC(SYSDATE),'LUNCH',?)",dietId,fixture.menuId);
            ProcessBuilder builder=new ProcessBuilder("node","src/test/browser/menu-bookmark.browser.js").inheritIO();Map<String,String> env=builder.environment();
            env.put("BOOKMARK_TEST_ALICE",fixture.alice);env.put("BOOKMARK_TEST_BOB",fixture.bob);env.put("BOOKMARK_TEST_PASSWORD",password);
            env.put("BOOKMARK_TEST_MENU",String.valueOf(fixture.menuId));env.put("BOOKMARK_TEST_OTHER_MENU",String.valueOf(fixture.otherMenuId));env.put("BOOKMARK_TEST_MENU_NAME",menuName);
            env.put("BOOKMARK_BASE_URL",System.getProperty("bookmark.baseUrl","http://localhost:8080/controller"));
            env.put("PLAYWRIGHT_MODULE",new File(System.getProperty("bookmark.playwrightModule","target/server-tools/browser/node_modules/playwright")).getAbsolutePath());
            env.put("CHROME_EXECUTABLE",System.getProperty("bookmark.chrome","C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe"));
            Process process=builder.start();if(!process.waitFor(120,TimeUnit.SECONDS)){process.destroyForcibly();fail("Browser test timeout");}assertEquals("See browser assertions",0,process.exitValue());
        } finally {
            if(dietId!=null)MenuReviewOracleIT.jdbc.update("DELETE FROM tbl_daily_diet WHERE diet_id=?",dietId);
            for(Long id:Arrays.asList(fixture.menuId,fixture.otherMenuId))if(id!=null)MenuReviewOracleIT.jdbc.update("DELETE FROM tbl_bookmark WHERE menu_id=?",id);
            // Restore this test's renamed fixture so the shared name-guarded cleanup recognizes it.
            if(fixture.menuId!=null)MenuReviewOracleIT.jdbc.update("UPDATE tbl_menu SET menu_name='Review integration fixture' WHERE menu_id=? AND menu_name=?",fixture.menuId,"북마크 검증 음식 "+fixture.menuId);
            fixture.cleanup();
            if(fixture.menuId!=null)assertEquals(Integer.valueOf(0),MenuReviewOracleIT.jdbc.queryForObject("SELECT COUNT(*) FROM tbl_menu WHERE menu_id=?",Integer.class,fixture.menuId));
        }
    }
}
