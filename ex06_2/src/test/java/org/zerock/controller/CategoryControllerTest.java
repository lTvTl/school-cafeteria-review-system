package org.zerock.controller;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.zerock.domain.MenuCategory;
import org.zerock.domain.MenuVO;
import org.zerock.menu.StubMenuMapper;
import org.zerock.service.MenuServiceImpl;

public class CategoryControllerTest {
	private StubMenuMapper mapper;
	private MockMvc mvc;

	@Before
	public void setUp() {
		mapper = new StubMenuMapper();
		for (MenuCategory category : MenuCategory.values()) {
			MenuVO row = new MenuVO();
			row.setMenuId((long) category.ordinal() + 1);
			row.setCategory(category.name());
			row.setMenuName("음식 " + category.getLabel());
			row.setAvgRating(4.25);
			row.setBookmarkCnt(12);
			mapper.rows.add(row);
		}
		MenuServiceImpl service = new MenuServiceImpl();
		service.setMapper(mapper);
		// Register both controllers: duplicate /category mappings fail setup.
		mvc = MockMvcBuilders.standaloneSetup(homeWithDietService(), new CategoryController(service), new AdminController())
				.setViewResolvers(new InternalResourceViewResolver("/WEB-INF/views/", ".jsp"))
				.build();
	}

	@Test
	public void defaultCategoryIsMain() throws Exception {
		mvc.perform(get("/category")).andExpect(status().isOk()).andExpect(view().name("category"))
				.andExpect(model().attribute("selectedCategory", "main"))
				.andExpect(model().attribute("menuCount", 1));
		assertEquals("MAIN", mapper.lastCategory);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void allFiveCategoriesRetainRatingsAndBookmarks() throws Exception {
		for (MenuCategory category : MenuCategory.values()) {
			MvcResult result = mvc.perform(get("/category").param("type", category.getType()))
					.andExpect(status().isOk()).andExpect(model().attribute("selectedCategory", category.getType()))
					.andExpect(model().attribute("menuCount", 1)).andReturn();
			List<MenuVO> rows = (List<MenuVO>) result.getModelAndView().getModel().get("menuList");
			assertEquals(1, rows.size());
			assertEquals(category.name(), rows.get(0).getCategory());
			assertEquals(Double.valueOf(4.25), rows.get(0).getAvgRating());
			assertEquals(12, rows.get(0).getBookmarkCnt());
		}
	}

	@Test
	public void emptyCategoryIsSuccessfulEmptyList() throws Exception {
		mapper.rows.clear();
		MvcResult result = mvc.perform(get("/category").param("type", "etc"))
				.andExpect(status().isOk()).andExpect(model().attribute("menuCount", 0)).andReturn();
		assertTrue(((List<?>) result.getModelAndView().getModel().get("menuList")).isEmpty());
		assertFalse(result.getModelAndView().getModel().containsKey("errorMessage"));
	}

	@Test
	public void invalidCategoriesNeverReachMapper() throws Exception {
		for (String value : new String[] { "unknown", "", "MAIN' OR '1'='1", "<script>alert(1)</script>" }) {
			mvc.perform(get("/category").param("type", value)).andExpect(status().isBadRequest())
					.andExpect(view().name("category"))
					.andExpect(model().attribute("errorMessage", "올바른 음식 카테고리를 선택해 주세요."));
		}
		assertEquals(0, mapper.calls);
	}

	@Test
	public void databaseFailureReturnsGenericError() throws Exception {
		mapper.failure = new DataAccessResourceFailureException("secret SQL connection detail");
		MvcResult result = mvc.perform(get("/category")).andExpect(status().isInternalServerError())
				.andExpect(view().name("category"))
				.andExpect(model().attribute("errorMessage", "음식 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요."))
				.andReturn();
		assertFalse(result.getModelAndView().getModel().toString().contains("secret SQL"));
	}

	@Test
	public void existingHomeMappingsStillResolve() throws Exception {
		mvc.perform(get("/")).andExpect(view().name("redirect:/main"));
		for (String page : new String[] { "main", "mypage", "admin", "customSignup" }) {
			mvc.perform(get("/" + page)).andExpect(status().isOk()).andExpect(view().name(page));
		}
	}

    private HomeController homeWithDietService() {
        HomeController home = new HomeController();
        org.zerock.service.DailyDietService reviews = (org.zerock.service.DailyDietService) java.lang.reflect.Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[]{org.zerock.service.DailyDietService.class},
            (proxy, method, args) -> java.util.Collections.emptyList());
        org.springframework.test.util.ReflectionTestUtils.setField(home, "dailyDietService", reviews);
        return home;
    }
}
