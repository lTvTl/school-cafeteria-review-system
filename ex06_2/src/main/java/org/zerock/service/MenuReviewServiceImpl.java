package org.zerock.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.domain.Criteria;
import org.zerock.domain.DailyDietVO;
import org.zerock.domain.MenuReviewVO;
import org.zerock.mapper.MenuReviewMapper;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MenuReviewServiceImpl implements MenuReviewService {
	private final MenuReviewMapper mapper;

	@Override public List<DailyDietVO> getDietList(int dayOffset, String userid) {
		return mapper.getDietList(dayOffset, userid);
	}
	@Override public List<MenuReviewVO> getList(Criteria cri, String userid) {
		return mapper.getList(cri, userid);
	}
	@Override public int getCount() { return mapper.getCount(); }

	@Transactional
	@Override public void register(MenuReviewVO review, String userid) {
		validate(review, userid);
		requireId(review.getDietId());
		Long menuId = mapper.findTodayMenu(review.getDietId());
		if (menuId == null) throw new IllegalArgumentException("오늘 실제 제공이 확정된 메뉴만 평가할 수 있습니다.");
		lock(menuId);
		review.setMenuId(menuId);
		if (mapper.insert(review) != 1) throw new IllegalArgumentException("식단이 변경되었거나 작성 가능 날짜가 지났습니다. 새로고침해 주세요.");
		mapper.refreshRating(menuId);
	}

	@Transactional
	@Override public void modify(MenuReviewVO review, String userid) {
		validate(review, userid);
		Long menuId = ownedMenu(review.getReviewId(), userid);
		lock(menuId);
		if (mapper.update(review) != 1) throw new IllegalArgumentException("본인이 오늘 작성한 리뷰만 수정할 수 있습니다.");
		mapper.refreshRating(menuId);
	}

	@Transactional
	@Override public void remove(Long reviewId, String userid) {
		Long menuId = ownedMenu(reviewId, userid);
		lock(menuId);
		if (mapper.delete(reviewId, userid) != 1) throw new IllegalArgumentException("리뷰가 없거나 삭제 권한이 없습니다.");
		mapper.refreshRating(menuId);
	}

	private Long ownedMenu(Long reviewId, String userid) {
		requireUser(userid);
		requireId(reviewId);
		Long menuId = mapper.findOwnedMenu(reviewId, userid);
		if (menuId == null) throw new IllegalArgumentException("리뷰가 없거나 변경 권한이 없습니다.");
		return menuId;
	}
	private void lock(Long menuId) {
		// Serialize review writes per menu so the stored average includes concurrent changes.
		if (mapper.lockMenu(menuId) == null) throw new IllegalArgumentException("메뉴를 찾을 수 없습니다.");
	}
	private void validate(MenuReviewVO review, String userid) {
		requireUser(userid);
		if (review == null || review.getStarScore() < 1 || review.getStarScore() > 5)
			throw new IllegalArgumentException("별점은 1~5점 중 선택해 주세요.");
		String text = review.getReviewText();
		if (text == null || text.codePoints().allMatch(c -> Character.isWhitespace(c) || Character.isSpaceChar(c))
				|| text.length() > 1000)
			throw new IllegalArgumentException("리뷰는 공백이 아닌 내용으로 1~1,000자 입력해 주세요.");
		review.setReviewText(text.trim());
		review.setUserid(userid);
	}
	private void requireUser(String userid) {
		if (userid == null || userid.trim().isEmpty()) throw new IllegalArgumentException("로그인이 필요합니다.");
	}
	private void requireId(Long id) {
		if (id == null || id <= 0) throw new IllegalArgumentException("올바른 메뉴 또는 리뷰를 선택해 주세요.");
	}
}
