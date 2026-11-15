package org.zerock.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.zerock.domain.*;
import org.zerock.mapper.MenuMapper;
import org.zerock.mapper.MenuReviewMapper;

@Service
public class MenuReviewServiceImpl implements MenuReviewService {
    private final MenuMapper menus;
    private final MenuReviewMapper reviews;

    @Autowired
    public MenuReviewServiceImpl(MenuMapper menus, MenuReviewMapper reviews) {
        this.menus = menus;
        this.reviews = reviews;
    }

    private MenuVO requireMenu(Long menuId) {
        if (menuId == null || menuId <= 0) throw failure(HttpStatus.NOT_FOUND, "음식을 찾을 수 없습니다.");
        MenuVO menu = menus.read(menuId);
        if (menu == null) throw failure(HttpStatus.NOT_FOUND, "음식을 찾을 수 없습니다.");
        return menu;
    }

    private void lock(Long menuId) {
        if (menuId == null || menuId <= 0 || reviews.lockMenu(menuId) == null)
            throw failure(HttpStatus.NOT_FOUND, "음식을 찾을 수 없습니다.");
    }

    private String authenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            throw failure(HttpStatus.UNAUTHORIZED, "로그인 후 리뷰를 작성해 주세요.");
        return auth.getName();
    }

    private String validate(Integer score, String text) {
        if (score == null || score < 1 || score > 5)
            throw failure(HttpStatus.BAD_REQUEST, "별점은 1~5점 중 선택해 주세요.");
        if (text == null || text.codePoints().allMatch(c -> Character.isWhitespace(c) || Character.isSpaceChar(c)))
            throw failure(HttpStatus.BAD_REQUEST, "리뷰 내용을 입력해 주세요.");
        if (text.codePointCount(0, text.length()) > 1000)
            throw failure(HttpStatus.BAD_REQUEST, "리뷰는 1,000자 이내로 입력해 주세요.");
        return text.trim();
    }

    private MenuReviewVO owned(Long menuId, Long reviewId, String userid) {
        MenuReviewVO review = reviewId == null || reviewId <= 0 ? null : reviews.read(reviewId);
        if (review == null || !menuId.equals(review.getMenuId()))
            throw failure(HttpStatus.NOT_FOUND, "이 음식의 리뷰를 찾을 수 없습니다.");
        if (!userid.equals(review.getUserid()))
            throw failure(HttpStatus.FORBIDDEN, "본인이 작성한 리뷰만 변경할 수 있습니다.");
        return review;
    }

    @Override
    @Transactional(readOnly = true)
    public MenuReviewPage getPage(Long menuId, int requestedPage) {
        MenuReviewPage result = new MenuReviewPage();
        result.setMenu(requireMenu(menuId));
        MenuReviewSummary summary = reviews.getSummary(menuId);
        result.setSummary(summary);
        int totalPages = (int) Math.max(1L, (summary.getReviewCount() + 9L) / 10L);
        int page = Math.max(1, Math.min(requestedPage, totalPages));
        result.setPage(page);
        result.setTotalPages(totalPages);
        result.setReviews(reviews.getList(menuId, (page - 1L) * 10L + 1, page * 10L));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public MenuReviewSummary getSummary(Long menuId) {
        requireMenu(menuId);
        return reviews.getSummary(menuId);
    }

    @Override
    @Transactional
    public void register(Long menuId, Integer score, String text) {
        String userid = authenticatedUser();
        String content = validate(score, text);
        lock(menuId); // Serialize all mutations before checking daily duplicates or recalculating AVG.
        if (reviews.countToday(menuId, userid) > 0)
            throw failure(HttpStatus.CONFLICT, "같은 음식의 리뷰는 하루에 한 번만 작성할 수 있습니다.");
        MenuReviewVO review = new MenuReviewVO();
        review.setMenuId(menuId);
        review.setUserid(userid);
        review.setStarScore(score);
        review.setReviewText(content);
        try {
            if (reviews.insert(review) != 1) throw new IllegalStateException("Review insert failed");
        } catch (DuplicateKeyException e) {
            throw failure(HttpStatus.CONFLICT, "같은 음식의 리뷰는 하루에 한 번만 작성할 수 있습니다.");
        }
        refresh(menuId);
    }

    @Override
    @Transactional
    public void modify(Long menuId, Long reviewId, Integer score, String text) {
        String userid = authenticatedUser();
        String content = validate(score, text);
        lock(menuId);
        MenuReviewVO review = owned(menuId, reviewId, userid);
        if (!review.isEditable()) throw failure(HttpStatus.FORBIDDEN, "리뷰는 작성한 당일에만 수정할 수 있습니다.");
        review.setStarScore(score);
        review.setReviewText(content);
        // Repeat date/owner/menu predicates in SQL, including a midnight boundary during the request.
        if (reviews.update(review) != 1) throw failure(HttpStatus.FORBIDDEN, "리뷰 수정 가능 시간이 지났습니다.");
        refresh(menuId);
    }

    @Override
    @Transactional
    public void remove(Long menuId, Long reviewId) {
        String userid = authenticatedUser();
        lock(menuId);
        owned(menuId, reviewId, userid);
        if (reviews.delete(menuId, reviewId, userid) != 1) throw failure(HttpStatus.CONFLICT, "리뷰가 이미 변경되었습니다.");
        refresh(menuId);
    }

    private void refresh(Long menuId) {
        if (reviews.refreshAverage(menuId) != 1) throw new IllegalStateException("Menu average update failed");
    }

    private static ResponseStatusException failure(HttpStatus status, String message) {
        return new ResponseStatusException(status, message);
    }

    public MenuReviewServiceImpl(MenuReviewMapper reviews) { this(null, reviews); }
	@Override public List<DailyDietVO> getDietList(int dayOffset, String userid) {
		return reviews.getDietList(dayOffset, userid);
	}
	@Override public List<MenuReviewVO> getList(Criteria cri, String userid) {
		return reviews.getDietReviews(cri, userid);
	}
	@Override public int getCount() { return reviews.getCount(); }

	@Transactional
	@Override public void register(MenuReviewVO review, String userid) {
		validate(review, userid);
		requireId(review.getDietId());
		Long menuId = reviews.findTodayMenu(review.getDietId());
		if (menuId == null) throw new IllegalArgumentException("오늘 실제 제공이 확정된 메뉴만 평가할 수 있습니다.");
		lockDiet(menuId);
		review.setMenuId(menuId);
		if (reviews.insertDietReview(review) != 1) throw new IllegalArgumentException("식단이 변경되었거나 작성 가능 날짜가 지났습니다. 새로고침해 주세요.");
		reviews.refreshRating(menuId);
	}

	@Transactional
	@Override public void modify(MenuReviewVO review, String userid) {
		validate(review, userid);
		Long menuId = ownedMenu(review.getReviewId(), userid);
		lockDiet(menuId);
		if (reviews.updateDietReview(review) != 1) throw new IllegalArgumentException("본인이 오늘 작성한 리뷰만 수정할 수 있습니다.");
		reviews.refreshRating(menuId);
	}

	@Transactional
	@Override public void remove(Long reviewId, String userid) {
		Long menuId = ownedMenu(reviewId, userid);
		lockDiet(menuId);
		if (reviews.deleteDietReview(reviewId, userid) != 1) throw new IllegalArgumentException("리뷰가 없거나 삭제 권한이 없습니다.");
		reviews.refreshRating(menuId);
	}

	private Long ownedMenu(Long reviewId, String userid) {
		requireUser(userid);
		requireId(reviewId);
		Long menuId = reviews.findOwnedMenu(reviewId, userid);
		if (menuId == null) throw new IllegalArgumentException("리뷰가 없거나 변경 권한이 없습니다.");
		return menuId;
	}
	private void lockDiet(Long menuId) {
		// Serialize review writes per menu so the stored average includes concurrent changes.
		if (reviews.lockDietMenu(menuId) == null) throw new IllegalArgumentException("메뉴를 찾을 수 없습니다.");
	}
	private void validate(MenuReviewVO review, String userid) {
		requireUser(userid);
		if (review == null || review.getStarScore() == null || review.getStarScore() < 1 || review.getStarScore() > 5)
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
