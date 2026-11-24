package org.zerock.service;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.zerock.domain.DailyReviewPage;
import org.zerock.domain.DailyReviewVO;
import org.zerock.mapper.DailyReviewMapper;

@Service
public class DailyReviewServiceImpl implements DailyReviewService {
    private final DailyReviewMapper reviews;

    @Autowired
    public DailyReviewServiceImpl(DailyReviewMapper reviews) { this.reviews = reviews; }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken
                ? null : auth.getName();
    }

    private String authenticatedUser() {
        String user = currentUser();
        if (user == null) throw failure(HttpStatus.UNAUTHORIZED, "로그인 후 리뷰를 작성해 주세요.");
        return user;
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

    private void lock(String userid) {
        // A stable parent row serializes register/modify/delete even when no review exists yet.
        if (reviews.lockUser(userid) == null)
            throw failure(HttpStatus.UNAUTHORIZED, "회원 정보를 확인할 수 없습니다. 다시 로그인해 주세요.");
    }

    private DailyReviewVO owned(Long reviewId, String userid) {
        DailyReviewVO review = reviewId == null || reviewId <= 0 ? null : reviews.read(reviewId);
        if (review == null) throw failure(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다.");
        if (!userid.equals(review.getUserid()))
            throw failure(HttpStatus.FORBIDDEN, "본인이 작성한 리뷰만 변경할 수 있습니다.");
        return review;
    }

    @Override
    @Transactional(readOnly = true)
    public DailyReviewPage getPage(int requestedPage) {
        // Capture one database day for the entire response, even across midnight.
        Date today = reviews.today();
        DailyReviewPage result = new DailyReviewPage();
        result.setServeDate(today);
        result.setSummary(reviews.getSummary(today));
        int totalPages = (int) Math.max(1L, (result.getSummary().getReviewCount() + 9L) / 10L);
        int page = Math.max(1, Math.min(requestedPage, totalPages));
        result.setPage(page);
        result.setTotalPages(totalPages);
        result.setReviews(reviews.getList(today, (page - 1L) * 10L + 1, page * 10L));
        String userid = currentUser();
        if (userid != null) result.setOwnReview(reviews.findOwn(today, userid));
        return result;
    }

    @Override
    @Transactional
    public void register(Integer score, String text) {
        String userid = authenticatedUser();
        String content = validate(score, text);
        lock(userid);
        Date today = reviews.today();
        if (reviews.findOwn(today, userid) != null)
            throw failure(HttpStatus.CONFLICT, "오늘의 학식 리뷰를 이미 작성했습니다. 내 리뷰를 수정해 주세요.");
        DailyReviewVO review = new DailyReviewVO();
        review.setServeDate(today);
        review.setUserid(userid);
        review.setStarScore(score);
        review.setReviewText(content);
        try {
            if (reviews.insert(review) != 1)
                throw failure(HttpStatus.CONFLICT, "날짜가 변경되었습니다. 새로고침 후 다시 작성해 주세요.");
        } catch (DuplicateKeyException e) {
            throw failure(HttpStatus.CONFLICT, "오늘의 학식 리뷰를 이미 작성했습니다. 내 리뷰를 수정해 주세요.");
        }
        // Daily averages are aggregated from this table on GET, never cached on tbl_menu.
    }

    @Override
    @Transactional
    public void modify(Long reviewId, Integer score, String text) {
        String userid = authenticatedUser();
        String content = validate(score, text);
        lock(userid);
        DailyReviewVO review = owned(reviewId, userid);
        if (!review.isEditable()) throw failure(HttpStatus.FORBIDDEN, "리뷰는 작성한 당일에만 수정할 수 있습니다.");
        review.setStarScore(score);
        review.setReviewText(content);
        if (reviews.update(review) != 1)
            throw failure(HttpStatus.FORBIDDEN, "리뷰 수정 가능 시간이 지났습니다.");
    }

    @Override
    @Transactional
    public void remove(Long reviewId) {
        String userid = authenticatedUser();
        lock(userid);
        owned(reviewId, userid);
        if (reviews.delete(reviewId, userid) != 1)
            throw failure(HttpStatus.CONFLICT, "리뷰가 이미 변경되었습니다.");
    }

    private static ResponseStatusException failure(HttpStatus status, String message) {
        return new ResponseStatusException(status, message);
    }
}
