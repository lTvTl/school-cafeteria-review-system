package org.zerock.controller;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zerock.domain.MenuCategory;
import org.zerock.domain.MenuVO;
import org.zerock.service.MenuService;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@Log4j
@AllArgsConstructor
public class CategoryController {

	private MenuService service;

	@GetMapping("/category")
	public String category(@RequestParam(value = "type", required = false) String type,
			Model model, HttpServletResponse response) {
		model.addAttribute("categories", MenuCategory.values());
		model.addAttribute("menuList", Collections.emptyList());
		model.addAttribute("menuCount", 0);
		model.addAttribute("selectedCategory", "");

		MenuCategory category;
		try {
			category = MenuCategory.fromType(type);
		} catch (IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			model.addAttribute("errorMessage", "올바른 음식 카테고리를 선택해 주세요.");
			return "category";
		}

		model.addAttribute("selectedCategory", category.getType());
		model.addAttribute("categoryLabel", category.getLabel());
		try {
			List<MenuVO> menuList = service.getListByCategory(category.name());
			model.addAttribute("menuList", menuList);
			// Count the displayed snapshot to avoid a second, potentially inconsistent query.
			model.addAttribute("menuCount", menuList.size());
		} catch (RuntimeException e) {
			log.error("Failed to load menu category " + category.name(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			model.addAttribute("errorMessage", "음식 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
		}
		return "category";
	}
}
