package org.zerock.controller;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;
import org.zerock.domain.DailyDietVO;
import org.zerock.service.SchoolMenuWeek;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import org.zerock.service.DailyDietService;
import org.zerock.service.DailyReviewService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	@Autowired
	private DailyDietService dailyDietService;
	@Autowired
	private DailyReviewService dailyReviewService;
	@Autowired
	private SchoolMenuWeek schoolMenuWeek;
	
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
	public String main(Principal principal, Model model,
			@RequestParam(value = "reviewPage", defaultValue = "1") int reviewPage) {
		String userid = principal == null ? null : principal.getName();
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		schoolMenuWeek.refresh(today);
		for (int offset = 0; offset < 2; offset++) {
			List<DailyDietVO> saved = Collections.emptyList();
			try {
				saved = dailyDietService.getDietList(offset, userid);
			} catch (DataAccessException e) {
				logger.error("Could not load saved cafeteria menus", e);
				model.addAttribute("menuLoadError", "저장된 식단을 불러오지 못했습니다.");
			}
			model.addAttribute(offset == 0 ? "todayDietList" : "tomorrowDietList", saved);
		}
		try {
			model.addAttribute("dailyReviewPage", dailyReviewService.getPage(reviewPage));
		} catch (DataAccessException e) {
			logger.error("Could not load daily reviews", e);
			model.addAttribute("dailyReviewLoadError", "오늘의 학식 리뷰를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
		}
		return "main";
	}
	
	@RequestMapping(value = "/customSignup", method = RequestMethod.GET)
	public String signup() {
		return "customSignup";
	}
	
}
