package org.zerock.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.service.MenuReviewService;

@Controller
@RequestMapping("/menu/{menuId}/reviews")
@PreAuthorize("isAuthenticated()")
public class MenuPopupReviewController {
    private static final Logger log = Logger.getLogger(MenuPopupReviewController.class);
    private final MenuReviewService service;

    @Autowired
    public MenuPopupReviewController(MenuReviewService service) { this.service = service; }

    @PostMapping
    public String register(@PathVariable("menuId") Long menuId,
            @RequestParam(value = "starScore", required = false) String score,
            @RequestParam(value = "reviewText", required = false) String text, RedirectAttributes flash) {
        return change(menuId, flash, () -> service.register(menuId, score(score), text));
    }

    @PostMapping("/{reviewId}/modify")
    public String modify(@PathVariable("menuId") Long menuId, @PathVariable("reviewId") Long reviewId,
            @RequestParam(value = "starScore", required = false) String score,
            @RequestParam(value = "reviewText", required = false) String text, RedirectAttributes flash) {
        return change(menuId, flash, () -> service.modify(menuId, reviewId, score(score), text));
    }

    @PostMapping("/{reviewId}/remove")
    public String remove(@PathVariable("menuId") Long menuId, @PathVariable("reviewId") Long reviewId,
            RedirectAttributes flash) {
        return change(menuId, flash, () -> service.remove(menuId, reviewId));
    }

    private String change(Long menuId, RedirectAttributes flash, Runnable mutation) {
        try {
            mutation.run(); // Transaction proxy has committed before this returns.
            flash.addFlashAttribute("reviewSaved", true);
            flash.addFlashAttribute("successMessage", "리뷰 변경사항이 저장되었습니다.");
        } catch (ResponseStatusException e) {
            if (e.getStatus() != HttpStatus.BAD_REQUEST && e.getStatus() != HttpStatus.CONFLICT) throw e;
            flash.addFlashAttribute("errorMessage", e.getReason());
        } catch (DataAccessException e) {
            log.error("Menu review persistence failed for menu " + menuId, e);
            flash.addFlashAttribute("errorMessage", "리뷰를 저장하지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }
        return "redirect:/menu/" + menuId;
    }

    private Integer score(String value) {
        try { return Integer.valueOf(value); }
        catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "별점은 1~5점 중 선택해 주세요.");
        }
    }
}
