package org.zerock.service;

import java.util.List;
import org.zerock.domain.MenuReviewPage;
import org.zerock.domain.MenuReviewSummary;
import org.zerock.domain.MenuReviewVO;

public interface MenuReviewService {
    MenuReviewPage getPage(Long menuId, int page);
    MenuReviewSummary getSummary(Long menuId);
    List<MenuReviewVO> getListByUserid(String userid);
    void register(Long menuId, Integer score, String text);
    void modify(Long menuId, Long reviewId, Integer score, String text);
    void remove(Long menuId, Long reviewId);


}