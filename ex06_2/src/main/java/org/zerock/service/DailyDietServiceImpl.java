package org.zerock.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.zerock.domain.DailyDietVO;
import org.zerock.mapper.DailyDietMapper;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DailyDietServiceImpl implements DailyDietService {
	private final DailyDietMapper mapper;

	@Override
	public List<DailyDietVO> getDietList(int dayOffset, String userid) {
		return mapper.getDietList(dayOffset, userid);
	}
}
