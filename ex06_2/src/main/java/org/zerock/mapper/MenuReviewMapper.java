package org.zerock.mapper;

import java.util.List;
import org.zerock.domain.Criteria;
import org.zerock.domain.DailyDietVO;
import org.apache.ibatis.annotations.Param;
import org.zerock.domain.MenuReviewVO;
import org.zerock.domain.MenuReviewSummary;

public interface MenuReviewMapper {
    Long lockMenu(@Param("menuId") Long menuId);
    List<MenuReviewVO> getList(@Param("menuId") Long menuId, @Param("start") long start, @Param("end") long end);
    MenuReviewVO read(@Param("reviewId") Long reviewId);
    int countToday(@Param("menuId") Long menuId, @Param("userid") String userid);
    MenuReviewSummary getSummary(@Param("menuId") Long menuId);
    int insert(MenuReviewVO review);
    int update(MenuReviewVO review);
    int delete(@Param("menuId") Long menuId, @Param("reviewId") Long reviewId, @Param("userid") String userid);
    int refreshAverage(@Param("menuId") Long menuId);

	List<DailyDietVO> getDietList(@Param("dayOffset") int dayOffset, @Param("userid") String userid);
	List<MenuReviewVO> getDietReviews(@Param("cri") Criteria cri, @Param("userid") String userid);
	int getCount();
	Long findTodayMenu(Long dietId);
	Long findOwnedMenu(@Param("reviewId") Long reviewId, @Param("userid") String userid);
	Long lockDietMenu(Long menuId);
	int insertDietReview(MenuReviewVO review);
	int updateDietReview(MenuReviewVO review);
	int deleteDietReview(@Param("reviewId") Long reviewId, @Param("userid") String userid);
	int refreshRating(Long menuId);

}