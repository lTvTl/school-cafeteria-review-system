package org.zerock.controller;

import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zerock.domain.*;
import org.zerock.service.MenuReviewService;

@Controller
@RequestMapping("/menu")
public class MenuController {
    private final MenuReviewService service;

    @Autowired
    public MenuController(MenuReviewService service) { this.service = service; }

    // A protected GET lets Spring Security save the destination before showing login.
    @GetMapping("/{menuId}/write")
    @PreAuthorize("isAuthenticated()")
    public String write(@PathVariable("menuId") Long menuId) {
        service.getSummary(menuId);
        return "redirect:/menu/" + menuId;
    }

    @GetMapping("/{menuId}")
    public String popup(@PathVariable("menuId") Long menuId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        MenuReviewPage result = service.getPage(menuId, page);
        model.addAttribute("reviewPage", result);
        model.addAttribute("menu", result.getMenu());
        model.addAttribute("summary", result.getSummary());
        model.addAttribute("categoryLabel", MenuCategory.fromCode(result.getMenu().getCategory()).getLabel());
        return "menu/popup";
    }

    @GetMapping(value = "/{menuId}/summary", produces = "application/json")
    @ResponseBody
    public MenuReviewSummary summary(@PathVariable("menuId") Long menuId, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return service.getSummary(menuId);
    }
}
