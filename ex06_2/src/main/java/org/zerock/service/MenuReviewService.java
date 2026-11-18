package org.zerock.service;

import org.zerock.domain.MenuReviewPage;
import org.zerock.domain.MenuReviewSummary;

public interface MenuReviewService {
    MenuReviewPage getPage(Long menuId, int page);
    MenuReviewSummary getSummary(Long menuId);
    void register(Long menuId, Integer score, String text);
    void modify(Long menuId, Long reviewId, Integer score, String text);
    void remove(Long menuId, Long reviewId);


}