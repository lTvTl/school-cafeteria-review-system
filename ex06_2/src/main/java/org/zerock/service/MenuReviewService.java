package org.zerock.service;

import java.util.List;
import org.zerock.domain.Criteria;
import org.zerock.domain.DailyDietVO;
import org.zerock.domain.MenuReviewVO;
import org.zerock.domain.MenuReviewPage;
import org.zerock.domain.MenuReviewSummary;

public interface MenuReviewService {
    MenuReviewPage getPage(Long menuId, int page);
    MenuReviewSummary getSummary(Long menuId);
    void register(Long menuId, Integer score, String text);
    void modify(Long menuId, Long reviewId, Integer score, String text);
    void remove(Long menuId, Long reviewId);

	List<DailyDietVO> getDietList(int dayOffset, String userid);
	List<MenuReviewVO> getList(Criteria cri, String userid);
	int getCount();
	void register(MenuReviewVO review, String userid);
	void modify(MenuReviewVO review, String userid);
	void remove(Long reviewId, String userid);

}