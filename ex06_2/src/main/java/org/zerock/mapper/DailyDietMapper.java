package org.zerock.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.zerock.domain.DailyDietVO;

public interface DailyDietMapper {
	List<DailyDietVO> getDietList(@Param("dayOffset") int dayOffset, @Param("userid") String userid);
}
