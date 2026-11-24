package org.zerock.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.service.DailyReviewService;

@Controller
@RequestMapping("/daily-reviews")
public class DailyReviewController {
    private static final Logger log = Logger.getLogger(DailyReviewController.class);
    private final DailyReviewService service;

    @Autowired
    public DailyReviewController(DailyReviewService service) { this.service = service; }

    @GetMapping
    public String list(@RequestParam(value = "page", defaultValue = "1") int page, RedirectAttributes redirect) {
        redirect.addAttribute("reviewPage", Math.max(1, page));
        return "redirect:/main#daily-reviews";
    }

    @PostMapping
    public String register(@RequestParam(value = "starScore", required = false) String score,
            @RequestParam(value = "reviewText", required = false) String text, RedirectAttributes flash) {
        return change(flash, () -> service.register(score(score), text));
    }

    @PostMapping("/{reviewId}/modify")
    public String modify(@PathVariable("reviewId") Long reviewId,
            @RequestParam(value = "starScore", required = false) String score,
            @RequestParam(value = "reviewText", required = false) String text, RedirectAttributes flash) {
        return change(flash, () -> service.modify(reviewId, score(score), text));
    }

    @PostMapping("/{reviewId}/remove")
    public String remove(@PathVariable("reviewId") Long reviewId, RedirectAttributes flash) {
        return change(flash, () -> service.remove(reviewId));
    }

    private String change(RedirectAttributes flash, Runnable mutation) {
        try {
            mutation.run();
            flash.addFlashAttribute("dailySuccessMessage", "오늘의 학식 리뷰 변경사항이 저장되었습니다.");
        } catch (ResponseStatusException e) {
            if (e.getStatus() != HttpStatus.BAD_REQUEST && e.getStatus() != HttpStatus.CONFLICT) throw e;
            flash.addFlashAttribute("dailyErrorMessage", e.getReason());
        } catch (DataAccessException e) {
            log.error("Daily review persistence failed", e);
            flash.addFlashAttribute("dailyErrorMessage", "리뷰를 저장하지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }
        return "redirect:/main#daily-reviews";
    }

    private Integer score(String value) {
        try { return Integer.valueOf(value); }
        catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "별점은 1~5점 중 선택해 주세요.");
        }
    }
}
