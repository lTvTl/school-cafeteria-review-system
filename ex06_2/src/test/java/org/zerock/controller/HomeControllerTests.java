package org.zerock.controller;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.zerock.service.DailyDietService;
import org.zerock.service.DailyReviewService;
import org.zerock.domain.DailyReviewPage;

public class HomeControllerTests {
    private MockMvc mvc(List<String> calls) {
        DailyDietService service = (DailyDietService) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[]{DailyDietService.class}, (proxy, method, args) -> {
                if (!method.getName().equals("getDietList")) throw new AssertionError("Unexpected review call: " + method.getName());
                calls.add(args[0] + ":" + args[1]);
                return Collections.emptyList();
            });
        HomeController controller = new HomeController();
        ReflectionTestUtils.setField(controller, "dailyDietService", service);
        DailyReviewService reviews = (DailyReviewService) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[]{DailyReviewService.class}, (proxy, method, args) -> {
                assertEquals("getPage", method.getName());
                DailyReviewPage page = new DailyReviewPage(); page.setPage((Integer) args[0]); return page;
            });
        ReflectionTestUtils.setField(controller, "dailyReviewService", reviews);
        return MockMvcBuilders.standaloneSetup(controller)
            .setViewResolvers(new InternalResourceViewResolver("/WEB-INF/views/", ".jsp")).build();
    }

    @Test public void homeOnlyLoadsTodayAndTomorrowForCurrentUser() throws Exception {
        List<String> calls = new ArrayList<>();
        mvc(calls).perform(get("/main").principal(() -> "user0").param("reviewPage", "2"))
            .andExpect(status().isOk()).andExpect(view().name("main"))
            .andExpect(model().attributeExists("todayDietList", "tomorrowDietList", "dailyReviewPage"))
            .andExpect(model().attributeDoesNotExist("menuReviewList", "reviewPage", "reviewPages"));
        assertEquals("[0:user0, 1:user0]", calls.toString());
    }

    @Test public void anonymousHomeKeepsMenuAccess() throws Exception {
        List<String> calls = new ArrayList<>();
        mvc(calls).perform(get("/main")).andExpect(status().isOk());
        assertEquals("[0:null, 1:null]", calls.toString());
    }

    @Test public void removedDietReviewRoutesHaveNoHandler() throws Exception {
        MockMvc mvc = mvc(new ArrayList<>());
        for (String path : new String[]{"/menu-reviews", "/menu-reviews/1/update", "/menu-reviews/1/delete"}) {
            mvc.perform(post(path).principal(() -> "user0")).andExpect(status().isNotFound());
        }
    }
}
