package org.zerock.service;

import java.util.List;
import org.zerock.domain.DailyDietVO;

public interface DailyDietService {
	List<DailyDietVO> getDietList(int dayOffset, String userid);
}
