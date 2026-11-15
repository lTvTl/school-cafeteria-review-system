package org.zerock.controller;

import java.security.Principal;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.domain.MenuReviewVO;
import org.zerock.service.MenuReviewService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/menu-reviews")
@PreAuthorize("isAuthenticated()")
@AllArgsConstructor
@Log4j
public class MenuReviewController {
	private final MenuReviewService service;

	@InitBinder("review")
	public void bindReview(WebDataBinder binder) {
		binder.setAllowedFields("dietId", "starScore", "reviewText");
	}

	@PostMapping
	public String register(@ModelAttribute("review") MenuReviewVO review, BindingResult binding,
			Principal principal, RedirectAttributes redirect) {
		if (binding.hasErrors()) return invalid(review, "입력한 메뉴와 별점을 확인해 주세요.", redirect);
		try {
			service.register(review, principal.getName());
			redirect.addFlashAttribute("result", "리뷰를 등록했습니다.");
		} catch (IllegalArgumentException e) { return invalid(review, e.getMessage(), redirect);
		} catch (DuplicateKeyException e) { return invalid(review, "이미 평가한 메뉴입니다. 기존 리뷰를 수정해 주세요.", redirect);
		} catch (DataAccessException e) {
			log.error("Menu review registration failed", e);
			return invalid(review, "리뷰를 저장하지 못했습니다. 잠시 후 다시 시도해 주세요.", redirect);
		}
		return "redirect:/main#menu-reviews";
	}

	@PostMapping("/{reviewId}/update")
	public String modify(@PathVariable("reviewId") Long reviewId, @ModelAttribute("review") MenuReviewVO review,
			BindingResult binding, Principal principal, RedirectAttributes redirect) {
		review.setReviewId(reviewId);
		if (binding.hasErrors()) return invalid(review, "별점은 1~5점 중 선택해 주세요.", redirect);
		try {
			service.modify(review, principal.getName());
			redirect.addFlashAttribute("result", "리뷰를 수정했습니다.");
		} catch (IllegalArgumentException e) { return invalid(review, e.getMessage(), redirect);
		} catch (DataAccessException e) {
			log.error("Menu review update failed", e);
			return invalid(review, "리뷰를 수정하지 못했습니다. 잠시 후 다시 시도해 주세요.", redirect);
		}
		return "redirect:/main#menu-reviews";
	}

	@PostMapping("/{reviewId}/delete")
	public String remove(@PathVariable("reviewId") Long reviewId, Principal principal, RedirectAttributes redirect) {
		try {
			service.remove(reviewId, principal.getName());
			redirect.addFlashAttribute("result", "리뷰를 삭제했습니다.");
		} catch (IllegalArgumentException e) { redirect.addFlashAttribute("error", e.getMessage());
		} catch (DataAccessException e) {
			log.error("Menu review deletion failed", e);
			redirect.addFlashAttribute("error", "리뷰를 삭제하지 못했습니다. 잠시 후 다시 시도해 주세요.");
		}
		return "redirect:/main#menu-reviews";
	}

	private String invalid(MenuReviewVO review, String message, RedirectAttributes redirect) {
		redirect.addFlashAttribute("error", message);
		redirect.addFlashAttribute("reviewDraft", review);
		return "redirect:/main#menu-reviews";
	}
}
