package org.zerock.controller;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.lang.reflect.Proxy;
import java.util.*;
import org.junit.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.zerock.service.DailyReviewService;

public class DailyReviewControllerTest {
    private MockMvc mvc;
    private Object[] arguments;
    private HttpStatus failure;
    @Before public void setup() {
        DailyReviewService service = (DailyReviewService) Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{DailyReviewService.class},(p,m,a)-> {
            if (failure != null) throw new ResponseStatusException(failure,"rejected");
            arguments = a; return null;
        });
        mvc = MockMvcBuilders.standaloneSetup(new DailyReviewController(service)).build();
    }
    @Test public void registrationUsesPrgAndIgnoresForgedUserDateAndMenu() throws Exception {
        mvc.perform(post("/daily-reviews").param("starScore","4").param("reviewText","whole meal")
            .param("userid","attacker").param("serveDate","2000-01-01").param("menuId","1"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/main#daily-reviews"))
            .andExpect(flash().attributeExists("dailySuccessMessage"));
        assertArrayEquals(new Object[]{4,"whole meal"},arguments);
    }
    @Test public void invalidScoreAndDuplicateReturnErrorFlash() throws Exception {
        mvc.perform(post("/daily-reviews").param("starScore","4.5")).andExpect(flash().attributeExists("dailyErrorMessage")); assertNull(arguments);
        failure = HttpStatus.CONFLICT;
        mvc.perform(post("/daily-reviews").param("starScore","4")).andExpect(redirectedUrl("/main#daily-reviews")).andExpect(flash().attributeExists("dailyErrorMessage"));
    }
    @Test public void ownershipFailuresAreNotReportedAsSuccess() throws Exception {
        failure = HttpStatus.FORBIDDEN;
        mvc.perform(post("/daily-reviews/1/modify").param("starScore","4")).andExpect(status().isForbidden());
        mvc.perform(post("/daily-reviews/1/remove")).andExpect(status().isForbidden());
    }
    @Test public void writesRedirectAndListingIsPublicGet() throws Exception {
        mvc.perform(post("/daily-reviews/7/modify").param("starScore","2").param("reviewText","updated")).andExpect(redirectedUrl("/main#daily-reviews"));
        assertArrayEquals(new Object[]{7L,2,"updated"},arguments);
        mvc.perform(post("/daily-reviews/7/remove")).andExpect(redirectedUrl("/main#daily-reviews")); assertArrayEquals(new Object[]{7L},arguments);
        mvc.perform(get("/daily-reviews").param("page","2")).andExpect(redirectedUrl("/main?reviewPage=2#daily-reviews"));
    }
}
