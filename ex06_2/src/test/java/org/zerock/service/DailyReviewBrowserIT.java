package org.zerock.service;

import static org.junit.Assert.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Optional real Chromium test; no browser dependency is added to the application. */
public class DailyReviewBrowserIT {
    @Test public void realBrowserCrudHoverKeyboardTouchAndIsolation() throws Exception {
        Assume.assumeTrue(Boolean.getBoolean("dailyReview.browserTest"));
        MenuReviewOracleIT.connect();
        MenuReviewOracleIT fixture = new MenuReviewOracleIT();
        try {
            fixture.fixture();
            String password=UUID.randomUUID().toString(); String hash=new BCryptPasswordEncoder().encode(password);
            for(String user:Arrays.asList(fixture.alice,fixture.bob)) {
                MenuReviewOracleIT.jdbc.update("UPDATE tbl_member SET userpw=?,enabled='1' WHERE userid=?",hash,user);
                MenuReviewOracleIT.jdbc.update("INSERT INTO tbl_member_auth(userid,auth) VALUES(?,'ROLE_USER')",user);
            }
            ProcessBuilder builder=new ProcessBuilder("node", "src/test/browser/daily-review.browser.js").inheritIO();
            Map<String,String> env=builder.environment();
            env.put("REVIEW_TEST_ALICE",fixture.alice); env.put("REVIEW_TEST_BOB",fixture.bob); env.put("REVIEW_TEST_PASSWORD",password);
            env.put("REVIEW_TEST_MENU",String.valueOf(fixture.menuId));
            env.put("REVIEW_BASE_URL",System.getProperty("dailyReview.baseUrl","http://localhost:8080/controller"));
            env.put("PLAYWRIGHT_MODULE",new File(System.getProperty("dailyReview.playwrightModule","target/server-tools/browser/node_modules/playwright")).getAbsolutePath());
            env.put("CHROME_EXECUTABLE",System.getProperty("dailyReview.chrome","C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe"));
            Process process=builder.start();
            if(!process.waitFor(120,TimeUnit.SECONDS)){process.destroyForcibly();fail("Browser test timed out");}
            assertEquals("See browser assertions above",0,process.exitValue());
        } finally {
            for(String user:Arrays.asList(fixture.alice,fixture.bob))if(user!=null)
                MenuReviewOracleIT.jdbc.update("DELETE FROM tbl_daily_review WHERE userid=?",user);
            fixture.cleanup();
        }
    }
}
