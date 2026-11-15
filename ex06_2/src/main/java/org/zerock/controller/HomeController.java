package org.zerock.controller;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.security.Principal;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.RequestParam;
import org.zerock.domain.Criteria;
import org.zerock.service.MenuReviewService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	@Autowired
	private MenuReviewService menuReviewService;
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		
		return "redirect:/main";
	}
	
	@RequestMapping(value = "/main", method = RequestMethod.GET)
	public String main(@RequestParam(value = "reviewPage", defaultValue = "1") int reviewPage,
			Principal principal, Model model) {
		String userid = principal == null ? null : principal.getName();
		model.addAttribute("todayDietList", Collections.emptyList());
		model.addAttribute("tomorrowDietList", Collections.emptyList());
		model.addAttribute("menuReviewList", Collections.emptyList());
		model.addAttribute("reviewPage", 1);
		model.addAttribute("reviewPages", 1);
		try {
			int total = menuReviewService.getCount();
			int pages = Math.max(1, (int) Math.ceil(total / 10.0));
			int page = Math.max(1, Math.min(reviewPage, pages));
			model.addAttribute("todayDietList", menuReviewService.getDietList(0, userid));
			model.addAttribute("tomorrowDietList", menuReviewService.getDietList(1, userid));
			model.addAttribute("menuReviewList", menuReviewService.getList(new Criteria(page, 10), userid));
			model.addAttribute("reviewPage", page);
			model.addAttribute("reviewPages", pages);
		} catch (DataAccessException e) {
			logger.error("Could not load cafeteria menus and reviews", e);
			model.addAttribute("menuLoadError", "식단과 리뷰를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
		}
		return "main";
	}
	
	@RequestMapping(value = "/mypage", method = RequestMethod.GET)
	public String mypage() {
		return "mypage";
	}
	
	@RequestMapping(value = "/admin", method = RequestMethod.GET)
	public String admin() {
		return "admin";
	}
	
	@RequestMapping(value = "/customSignup", method = RequestMethod.GET)
	public String signup() {
		return "customSignup";
	}
	
}
