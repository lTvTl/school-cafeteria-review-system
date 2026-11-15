package org.zerock.service;

import static org.junit.Assert.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import org.junit.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Opt-in test of the deployed JSP, Security filter chain, PRG and Oracle persistence. */
public class MenuReviewHttpIT {
    private MenuReviewOracleIT fixture;
    private String password;
    private String path;
    private Client alice;
    private Client bob;
    private static final String BASE=System.getProperty("menuReview.baseUrl","http://localhost:8080");

    @BeforeClass public static void connect() throws Exception {
        Assume.assumeTrue(Boolean.getBoolean("menuReview.httpTest"));
        MenuReviewOracleIT.connect();
    }
    @Before public void setup() throws Exception {
        fixture=new MenuReviewOracleIT();fixture.fixture();path="/menu/"+fixture.menuId;
        password=UUID.randomUUID().toString();String encoded=new BCryptPasswordEncoder().encode(password);
        for(String user:Arrays.asList(fixture.alice,fixture.bob)){
            MenuReviewOracleIT.jdbc.update("UPDATE tbl_member SET userpw=?,enabled='1' WHERE userid=?",encoded,user);
            MenuReviewOracleIT.jdbc.update("INSERT INTO tbl_member_auth(userid,auth) VALUES(?,'ROLE_MEMBER')",user);
        }
        alice=new Client();bob=new Client();
    }
    @After public void cleanup() throws Exception {
        try {
            for(Client client:Arrays.asList(alice,bob)) if(client!=null) {
                client.post("/customLogout",params("_csrf",token(client.get("/customLogin").body)));
            }
        } finally {if(fixture!=null)fixture.cleanup();}
    }

    private void login(Client client,String user) throws Exception {
        String csrf=token(client.get("/customLogin").body);
        Response response=client.post("/login",params("username",user,"password",password,"_csrf",csrf));
        assertEquals(302,response.code);assertFalse(response.location.contains("error"));
        assertTrue(client.get(path).body.contains("id=\"new-review\""));
    }
    private Map<String,String> params(String... pairs) {
        Map<String,String> result=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)result.put(pairs[i],pairs[i+1]);return result;
    }
    private String token(String html) {
        Matcher m=Pattern.compile("name=\"_csrf\"\\s+value=\"([^\"]+)\"").matcher(html);assertTrue("CSRF field missing",m.find());return m.group(1);
    }
    private Response post(Client client,String url,String... fields) throws Exception {
        Map<String,String> form=params(fields);form.put("_csrf",token(client.get(path).body));return client.post(url,form);
    }
    private void redirected(Response result) {assertEquals(302,result.code);assertTrue(result.location.endsWith(path));}

    @Test public void anonymousReadAndSecurityFiltersPreserveExistingPages() throws Exception {
        Response popup=alice.get(path);assertEquals(200,popup.code);assertTrue(popup.body.contains("아직 등록된 리뷰가 없습니다."));
        assertFalse(popup.body.contains("sidebar"));assertFalse(popup.body.contains("id=\"new-review\""));
        Response missingCsrf=alice.post(path+"/reviews",params("starScore","5","reviewText","blocked"));
        assertTrue(missingCsrf.code==403 || (missingCsrf.code==302&&missingCsrf.location.endsWith("/accessError")));
        String csrf=token(alice.get("/customLogin").body);
        Response anonymous=alice.post(path+"/reviews",params("starScore","5","reviewText","blocked","_csrf",csrf));
        assertEquals(302,anonymous.code);assertTrue(anonymous.location.endsWith("/customLogin"));
        assertEquals(0,fixture.service.getSummary(fixture.menuId).getReviewCount());
        assertEquals(404,alice.get("/menu/9223372036854775807").code);
        for(String url:Arrays.asList("/category?type=main","/main","/board/list","/customLogin"))assertEquals(200,alice.get(url).code);
        Response admin=alice.get("/admin");assertEquals(302,admin.code);assertTrue(admin.location.endsWith("/customLogin"));
    }

    @Test public void loginCrudPrgXssAndParentSummaryUseCommittedValues() throws Exception {
        Response loginPrompt=alice.get(path+"/write");
        assertEquals(302,loginPrompt.code);assertTrue(loginPrompt.location.endsWith("/customLogin"));
        login(alice,fixture.alice);
        Response returnToFood=alice.get(path+"/write");
        redirected(returnToFood);
        String xss="<script>alert('review')</script> 맛있어요";
        redirected(post(alice,path+"/reviews","starScore","5","reviewText",xss,"userid",fixture.bob));
        assertEquals(fixture.alice,MenuReviewOracleIT.mapper.read(fixture.reviewId()).getUserid());
        Response page=alice.get(path);assertEquals(200,page.code);assertTrue(page.body.contains("data-review-saved=\"true\""));
        assertTrue(page.body.contains("&lt;script&gt;"));assertFalse(page.body.contains("<script>alert('review')"));
        assertTrue(page.body.contains("수정 저장"));assertTrue(page.body.contains("삭제"));
        assertFalse(alice.get(path).body.contains("data-review-saved=\"true\""));
        assertTrue(alice.get(path+"/summary").body.contains("\"avgRating\":5.0"));
        Long id=fixture.reviewId();
        redirected(post(alice,path+"/reviews/"+id+"/modify","starScore","2","reviewText","바뀐 한줄평"));
        page=alice.get(path);assertTrue(page.body.contains("바뀐 한줄평"));assertTrue(page.body.contains("수정됨"));assertEquals(2,fixture.storedAverage(),0);
        redirected(post(alice,path+"/reviews/"+id+"/remove"));
        page=alice.get(path);assertTrue(page.body.contains("아직 등록된 리뷰가 없습니다."));assertTrue(page.body.contains("data-review-saved=\"true\""));
        assertTrue(alice.get(path+"/summary").body.contains("\"avgRating\":0.0"));
    }

    @Test public void authenticatedTamperingDuplicatesAndPastDateAreBlocked() throws Exception {
        login(alice,fixture.alice);login(bob,fixture.bob);
        redirected(post(alice,path+"/reviews","starScore","4","reviewText","first"));Long id=fixture.reviewId();
        redirected(post(alice,path+"/reviews","starScore","5","reviewText","duplicate"));
        Response duplicate=alice.get(path);assertTrue(duplicate.body.contains("하루에 한 번"));assertFalse(duplicate.body.contains("data-review-saved=\"true\""));
        assertEquals(1,fixture.service.getSummary(fixture.menuId).getReviewCount());
        assertEquals(403,post(bob,path+"/reviews/"+id+"/modify","starScore","1","reviewText","attack").code);
        assertEquals(403,post(bob,path+"/reviews/"+id+"/remove").code);
        assertFalse(bob.get(path).body.contains("수정 저장"));
        assertEquals(404,post(alice,"/menu/"+fixture.otherMenuId+"/reviews/"+id+"/remove").code);
        MenuReviewOracleIT.jdbc.update("UPDATE tbl_menu_review SET regdate=SYSDATE-1 WHERE review_id=?",id);
        assertEquals(403,post(alice,path+"/reviews/"+id+"/modify","starScore","2","reviewText","late").code);
        assertFalse(alice.get(path).body.contains("수정 저장"));
        redirected(post(alice,path+"/reviews/"+id+"/remove"));assertEquals(0,fixture.storedAverage(),0);
    }

    private static class Response {int code;String body;String location;}
    private static class Client {
        final CookieManager cookies=new CookieManager(null,CookiePolicy.ACCEPT_ALL);
        Response get(String path)throws Exception{return request(path,null);}
        Response post(String path,Map<String,String> values)throws Exception{return request(path,values);}
        Response request(String path,Map<String,String> values)throws Exception {
            URI uri=URI.create(BASE+path);HttpURLConnection conn=(HttpURLConnection)uri.toURL().openConnection();
            conn.setInstanceFollowRedirects(false);conn.setConnectTimeout(5000);conn.setReadTimeout(15000);
            for(Map.Entry<String,List<String>> h:cookies.get(uri,Collections.emptyMap()).entrySet())conn.setRequestProperty(h.getKey(),String.join("; ",h.getValue()));
            if(values!=null){conn.setRequestMethod("POST");conn.setDoOutput(true);conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");StringBuilder form=new StringBuilder();for(Map.Entry<String,String> e:values.entrySet()){if(form.length()>0)form.append('&');form.append(URLEncoder.encode(e.getKey(),"UTF-8")).append('=').append(URLEncoder.encode(e.getValue(),"UTF-8"));}try(OutputStream out=conn.getOutputStream()){out.write(form.toString().getBytes(StandardCharsets.UTF_8));}}
            Response result=new Response();result.code=conn.getResponseCode();result.location=conn.getHeaderField("Location");cookies.put(uri,conn.getHeaderFields());
            InputStream stream=result.code>=400?conn.getErrorStream():conn.getInputStream();ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            if(stream!=null)try(InputStream in=stream){byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1)bytes.write(buffer,0,n);}
            result.body=new String(bytes.toByteArray(),StandardCharsets.UTF_8);conn.disconnect();return result;
        }
    }
}
