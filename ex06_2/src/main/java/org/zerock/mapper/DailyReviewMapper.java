package org.zerock.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.zerock.domain.DailyReviewVO;
import org.zerock.domain.DailyReviewSummary;

public interface DailyReviewMapper {
    Date today();
    String lockUser(@Param("userid") String userid);
    DailyReviewSummary getSummary(@Param("serveDate") Date serveDate);
    List<DailyReviewVO> getList(@Param("serveDate") Date serveDate, @Param("start") long start, @Param("end") long end);
    DailyReviewVO findOwn(@Param("serveDate") Date serveDate, @Param("userid") String userid);
    DailyReviewVO read(@Param("reviewId") Long reviewId);
    int insert(DailyReviewVO review);
    int update(DailyReviewVO review);
    int delete(@Param("reviewId") Long reviewId, @Param("userid") String userid);
}
