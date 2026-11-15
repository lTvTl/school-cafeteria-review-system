package org.zerock.service;

import static org.junit.Assert.*;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.zerock.domain.MenuReviewVO;
import org.zerock.mapper.MenuReviewMapper;

public class MenuReviewServiceTests {
    private MenuReviewServiceImpl service;
    private List<String> calls;
    private int affected;
    private Long menuId;
    private MenuReviewVO review;

    @Before public void setUp() {
        calls = new ArrayList<>(); affected = 1; menuId = 7L;
        MenuReviewMapper mapper = (MenuReviewMapper) Proxy.newProxyInstance(
            MenuReviewMapper.class.getClassLoader(), new Class<?>[]{MenuReviewMapper.class},
            (proxy, method, args) -> {
                calls.add(method.getName());
                switch (method.getName()) {
                case "findTodayMenu": case "findOwnedMenu": case "lockDietMenu": return menuId;
                case "insertDietReview": case "updateDietReview": return affected;
                case "deleteDietReview": assertEquals("alice", args[1]); return affected;
                case "refreshRating": return 1;
                default: throw new AssertionError(method.getName());
                }
            });
        service = new MenuReviewServiceImpl(mapper);
        review = new MenuReviewVO();
        review.setDietId(2L); review.setReviewId(3L); review.setMenuId(999L);
        review.setUserid("forged"); review.setStarScore(5); review.setReviewText(" 맛있어요 ");
    }

    @Test public void registerUsesSessionAndDatabaseMenu() {
        service.register(review, "alice");
        assertEquals("alice", review.getUserid()); assertEquals(Long.valueOf(7), review.getMenuId());
        assertEquals("맛있어요", review.getReviewText());
        assertEquals("[findTodayMenu, lockDietMenu, insertDietReview, refreshRating]", calls.toString());
    }
    @Test public void invalidScoresNeverReachDatabase() {
        for (int score : new int[]{0, 6, -1}) {
            review.setStarScore(score);
            assertThrows(IllegalArgumentException.class, () -> service.register(review, "alice"));
        }
        assertTrue(calls.isEmpty());
    }
    @Test public void blankAndLongTextAreRejected() {
        for (String value : new String[]{null, " \t\n", "\u3000", new String(new char[1001]).replace('\0', 'a')}) {
            review.setReviewText(value);
            assertThrows(IllegalArgumentException.class, () -> service.register(review, "alice"));
        }
        assertTrue(calls.isEmpty());
    }
    @Test public void anonymousCannotWrite() {
        assertThrows(IllegalArgumentException.class, () -> service.register(review, null));
        assertTrue(calls.isEmpty());
    }
    @Test public void unconfirmedOrPastDietCannotBeReviewed() {
        menuId = null;
        assertThrows(IllegalArgumentException.class, () -> service.register(review, "alice"));
        assertFalse(calls.contains("insertDietReview"));
    }
    @Test public void midnightBetweenLookupAndInsertRejectsWrite() {
        affected = 0;
        assertThrows(IllegalArgumentException.class, () -> service.register(review, "alice"));
        assertFalse(calls.contains("refreshRating"));
    }
    @Test public void otherUsersReviewCannotBeModifiedOrDeleted() {
        menuId = null;
        assertThrows(IllegalArgumentException.class, () -> service.modify(review, "alice"));
        assertThrows(IllegalArgumentException.class, () -> service.remove(3L, "alice"));
        assertFalse(calls.contains("updateDietReview")); assertFalse(calls.contains("deleteDietReview"));
    }
    @Test public void expiredModificationDoesNotRefreshAverage() {
        affected = 0;
        assertThrows(IllegalArgumentException.class, () -> service.modify(review, "alice"));
        assertFalse(calls.contains("refreshRating"));
    }
    @Test public void modificationAndDeletionLockBeforeWriting() {
        service.modify(review, "alice");
        assertEquals("alice", review.getUserid());
        assertEquals("[findOwnedMenu, lockDietMenu, updateDietReview, refreshRating]", calls.toString());
        calls.clear(); service.remove(3L, "alice");
        assertEquals("[findOwnedMenu, lockDietMenu, deleteDietReview, refreshRating]", calls.toString());
    }
}
