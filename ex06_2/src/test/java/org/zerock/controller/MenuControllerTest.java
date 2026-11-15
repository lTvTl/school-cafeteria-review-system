package org.zerock.controller;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.*;
import org.junit.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.zerock.domain.*;
import org.zerock.service.MenuReviewService;

public class MenuControllerTest {
    private MockMvc mvc;
    private ResponseStatusException failure;
    private int mutations;
    @Before public void setUp() {
        failure=null; mutations=0;
        MenuReviewService service=new MenuReviewService() {
            public MenuReviewPage getPage(Long id,int page) {
                if(id<=0 || id==99) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                MenuVO menu=new MenuVO();menu.setMenuId(id);menu.setCategory("MAIN");menu.setMenuName("돈까스");
                MenuReviewPage p=new MenuReviewPage();p.setMenu(menu);p.setSummary(getSummary(id));p.setReviews(Collections.emptyList());p.setPage(1);p.setTotalPages(1);return p;
            }
            public MenuReviewSummary getSummary(Long id) {MenuReviewSummary s=new MenuReviewSummary();s.setMenuId(id);return s;}
            public void register(Long id,Integer score,String text) {if(failure!=null)throw failure;mutations++;}
            public void modify(Long id,Long rid,Integer score,String text) {register(id,score,text);}
            public void remove(Long id,Long rid) {register(id,1,"");}
        };
        mvc=MockMvcBuilders.standaloneSetup(new HomeController(),new MenuController(service),new MenuReviewController(service))
            .setViewResolvers(new InternalResourceViewResolver("/WEB-INF/views/",".jsp")).build();
    }
    @Test public void anonymousDetailIsIndependentPopupView() throws Exception {
        mvc.perform(get("/menu/1")).andExpect(status().isOk()).andExpect(view().name("menu/popup"))
            .andExpect(header().string("Cache-Control","no-store")).andExpect(model().attribute("categoryLabel","메인메뉴"));
    }
    @Test public void missingAndMalformedMenuIdsDoNotOpenWrongFood() throws Exception {
        mvc.perform(get("/menu/99")).andExpect(status().isNotFound());
        mvc.perform(get("/menu/-1")).andExpect(status().isNotFound());
        mvc.perform(get("/menu/not-a-number")).andExpect(status().isBadRequest());
    }
    @Test public void allMutationsUsePrgAndSuccessFlash() throws Exception {
        for(String path:Arrays.asList("/menu/1/reviews","/menu/1/reviews/2/modify","/menu/1/reviews/2/remove")) {
            mvc.perform(post(path).param("starScore","5").param("reviewText","맛있어요"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/menu/1")).andExpect(flash().attribute("reviewSaved",true));
            mvc.perform(get(path)).andExpect(status().isMethodNotAllowed());
        }
        assertEquals(3,mutations);
    }
    @Test public void validationErrorsNeverEmitSuccessFlag() throws Exception {
        failure=new ResponseStatusException(HttpStatus.CONFLICT,"하루 한 번만 작성할 수 있습니다.");
        MvcResult result=mvc.perform(post("/menu/1/reviews").param("starScore","5").param("reviewText","text"))
            .andExpect(redirectedUrl("/menu/1")).andReturn();
        assertFalse(result.getFlashMap().containsKey("reviewSaved"));assertTrue(result.getFlashMap().containsKey("errorMessage"));
        mvc.perform(post("/menu/1/reviews").param("starScore","bad")).andExpect(redirectedUrl("/menu/1"));
        assertEquals(0,mutations);
    }
    @Test public void forbiddenMutationsRemainForbidden() throws Exception {
        failure=new ResponseStatusException(HttpStatus.FORBIDDEN,"본인 리뷰만 변경할 수 있습니다.");
        mvc.perform(post("/menu/1/reviews/2/remove")).andExpect(status().isForbidden());
    }
}
