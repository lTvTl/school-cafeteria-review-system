package org.zerock.service;

import java.util.List;
import org.zerock.domain.Criteria;
import org.zerock.domain.DailyDietVO;
import org.zerock.domain.MenuReviewVO;

public interface MenuReviewService {
	List<DailyDietVO> getDietList(int dayOffset, String userid);
	List<MenuReviewVO> getList(Criteria cri, String userid);
	int getCount();
	void register(MenuReviewVO review, String userid);
	void modify(MenuReviewVO review, String userid);
	void remove(Long reviewId, String userid);
}
