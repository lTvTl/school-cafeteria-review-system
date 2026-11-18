package org.zerock.service;



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


}
