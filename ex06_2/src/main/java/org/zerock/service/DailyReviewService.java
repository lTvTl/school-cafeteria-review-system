package org.zerock.service;

import org.zerock.domain.DailyReviewPage;

public interface DailyReviewService {
    DailyReviewPage getPage(int requestedPage);
    void register(Integer score, String text);
    void modify(Long reviewId, Integer score, String text);
    void remove(Long reviewId);
}
