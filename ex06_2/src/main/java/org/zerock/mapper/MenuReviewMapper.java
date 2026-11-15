package org.zerock.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.zerock.domain.Criteria;
import org.zerock.domain.DailyDietVO;
import org.zerock.domain.MenuReviewVO;

public interface MenuReviewMapper {
	List<DailyDietVO> getDietList(@Param("dayOffset") int dayOffset, @Param("userid") String userid);
	List<MenuReviewVO> getList(@Param("cri") Criteria cri, @Param("userid") String userid);
	int getCount();
	Long findTodayMenu(Long dietId);
	Long findOwnedMenu(@Param("reviewId") Long reviewId, @Param("userid") String userid);
	Long lockMenu(Long menuId);
	int insert(MenuReviewVO review);
	int update(MenuReviewVO review);
	int delete(@Param("reviewId") Long reviewId, @Param("userid") String userid);
	int refreshRating(Long menuId);
}
