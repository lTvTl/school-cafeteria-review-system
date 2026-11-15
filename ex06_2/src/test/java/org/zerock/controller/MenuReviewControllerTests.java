package org.zerock.controller;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import java.lang.reflect.Proxy;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zerock.domain.MenuReviewVO;
import org.zerock.service.MenuReviewService;

public class MenuReviewControllerTests {
    private MockMvc mvc;
    private MenuReviewVO saved;
    private String actor;
    private RuntimeException failure;

    @Before public void setUp() {
        MenuReviewService service = (MenuReviewService) Proxy.newProxyInstance(
            MenuReviewService.class.getClassLoader(), new Class<?>[]{MenuReviewService.class},
            (proxy, method, args) -> {
                if (failure != null) throw failure;
                if (args[0] instanceof MenuReviewVO) saved = (MenuReviewVO) args[0];
                actor = (String) args[1]; return null;
            });
        mvc = MockMvcBuilders.standaloneSetup(new MenuReviewController(service)).build();
    }
    @Test public void forgedIdentityAndMenuAreNotBound() throws Exception {
        MvcResult result = mvc.perform(post("/menu-reviews").principal(() -> "alice")
            .param("dietId", "2").param("starScore", "4").param("reviewText", "좋아요")
            .param("userid", "admin").param("menuId", "999").param("reviewId", "999")
            .param("editable", "true")).andReturn();
        assertEquals("/main#menu-reviews", result.getResponse().getRedirectedUrl());
        assertEquals("alice", actor); assertNull(saved.getUserid()); assertNull(saved.getMenuId());
        assertNull(saved.getReviewId()); assertFalse(saved.isEditable());
    }
    @Test public void updateUsesPathIdNotSubmittedId() throws Exception {
        mvc.perform(post("/menu-reviews/3/update").principal(() -> "alice")
            .param("reviewId", "999").param("starScore", "5").param("reviewText", "수정"));
        assertEquals(Long.valueOf(3), saved.getReviewId()); assertEquals("alice", actor);
    }
    @Test public void invalidNumericInputDoesNotReachService() throws Exception {
        MvcResult result = mvc.perform(post("/menu-reviews").principal(() -> "alice")
            .param("dietId", "2").param("starScore", "4.5").param("reviewText", "보존할 내용")).andReturn();
        assertNull(saved); assertNotNull(result.getFlashMap().get("error"));
        assertEquals("보존할 내용", ((MenuReviewVO) result.getFlashMap().get("reviewDraft")).getReviewText());
    }
    @Test public void duplicateReviewPreservesDraftAndShowsError() throws Exception {
        failure = new DuplicateKeyException("duplicate");
        MvcResult result = mvc.perform(post("/menu-reviews").principal(() -> "alice")
            .param("dietId", "2").param("starScore", "5").param("reviewText", "내용")).andReturn();
        assertNotNull(result.getFlashMap().get("error"));
        assertNotNull(result.getFlashMap().get("reviewDraft"));
        assertNull(result.getFlashMap().get("result"));
    }
}
