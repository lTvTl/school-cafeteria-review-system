package org.zerock.controller;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.lang.reflect.Proxy;
import org.junit.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.zerock.domain.BookmarkStatus;
import org.zerock.service.BookmarkService;

public class BookmarkControllerTest {
    private MockMvc mvc;
    private HttpStatus rejected;
    private Object[] passed;
    @Before public void setup() {
        BookmarkService service=(BookmarkService)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{BookmarkService.class},(p,m,a)->{
            if(rejected!=null)throw new ResponseStatusException(rejected,"rejected");passed=a;return new BookmarkStatus((Long)a[0],m.getName().equals("add"),m.getName().equals("add")?3:2);
        });mvc=MockMvcBuilders.standaloneSetup(new BookmarkController(service)).build();
    }
    @Test public void writesReturnCommittedJsonAndIgnoreSuppliedUserid() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper json=new com.fasterxml.jackson.databind.ObjectMapper();
        String added=mvc.perform(post("/menu/7/bookmark/add").param("userid","attacker")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(json.readTree("{\"menuId\":7,\"bookmarked\":true,\"bookmarkCnt\":3}"),json.readTree(added));assertArrayEquals(new Object[]{7L},passed);
        String removed=mvc.perform(post("/menu/7/bookmark/remove")).andReturn().getResponse().getContentAsString();
        assertEquals(json.readTree("{\"menuId\":7,\"bookmarked\":false,\"bookmarkCnt\":2}"),json.readTree(removed));
    }
    @Test public void getNeverChangesBookmarks() throws Exception {
        mvc.perform(get("/menu/7/bookmark/add")).andExpect(status().isMethodNotAllowed());mvc.perform(get("/menu/7/bookmark/remove")).andExpect(status().isMethodNotAllowed());assertNull(passed);
    }
    @Test public void missingMenuReturns404AndUnauthenticatedServiceReturns401() throws Exception {
        rejected=HttpStatus.NOT_FOUND;mvc.perform(post("/menu/999/bookmark/add")).andExpect(status().isNotFound());
        rejected=HttpStatus.UNAUTHORIZED;mvc.perform(post("/menu/7/bookmark/remove")).andExpect(status().isUnauthorized());
    }
}
