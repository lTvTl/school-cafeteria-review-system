package org.zerock.service;

import static org.junit.Assert.*;
import java.lang.reflect.Proxy;
import java.util.*;
import org.junit.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.zerock.domain.*;
import org.zerock.mapper.DailyReviewMapper;

public class DailyReviewServiceTest {
    private DailyReviewService service;
    private DailyReviewVO existing, inserted, own;
    private final List<String> calls = new ArrayList<>();
    private final Date today = new Date(1795392000000L);
    private int writeCount = 1, total;
    private boolean duplicate;
    private long start, end;

    @Before public void setup() {
        login("alice"); existing = new DailyReviewVO(); existing.setDailyReviewId(1L);
        existing.setUserid("alice"); existing.setServeDate(today); existing.setEditable(true);
        DailyReviewMapper mapper = (DailyReviewMapper) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{DailyReviewMapper.class}, (p, m, a) -> {
                calls.add(m.getName());
                switch (m.getName()) {
                    case "today": return today;
                    case "lockUser": return a[0];
                    case "read": return existing;
                    case "findOwn": assertEquals(today, a[0]); return own;
                    case "insert": if (duplicate) throw new DuplicateKeyException("race"); inserted = (DailyReviewVO)a[0]; return writeCount;
                    case "update": case "delete": return writeCount;
                    case "getSummary": assertEquals(today, a[0]); DailyReviewSummary summary = new DailyReviewSummary(); summary.setReviewCount(total); return summary;
                    case "getList": assertEquals(today, a[0]); start = (Long)a[1]; end = (Long)a[2]; return Collections.emptyList();
                    default: throw new AssertionError(m.getName());
                }
            });
        service = new DailyReviewServiceImpl(mapper);
    }
    @After public void clear() { SecurityContextHolder.clearContext(); }
    private void login(String user) { SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,"unused",Collections.emptyList())); }
    private void expect(HttpStatus status, Runnable action) {
        try { action.run(); fail("Expected " + status); } catch (ResponseStatusException e) { assertEquals(status,e.getStatus()); }
    }
    @Test public void anonymousCanReadButCannotWrite() {
        SecurityContextHolder.clearContext();
        expect(HttpStatus.UNAUTHORIZED, () -> service.register(5,"text"));
        expect(HttpStatus.UNAUTHORIZED, () -> service.modify(1L,5,"text"));
        expect(HttpStatus.UNAUTHORIZED, () -> service.remove(1L));
        assertTrue(calls.isEmpty()); service.getPage(1); assertFalse(calls.contains("findOwn"));
    }
    @Test public void validatesScoreAndUnicodeTextBeforeDatabaseAccess() {
        for (Integer score : new Integer[]{null,0,6}) expect(HttpStatus.BAD_REQUEST, () -> service.register(score,"text"));
        for (String text : new String[]{null,""," \t\n","\u00a0\u3000",String.join("",Collections.nCopies(1001,"가"))})
            expect(HttpStatus.BAD_REQUEST, () -> service.register(4,text));
        assertTrue(calls.isEmpty());
    }
    @Test public void identityAndDateComeFromServerAndThousandKoreanCharactersAreAllowed() {
        String text = String.join("",Collections.nCopies(1000,"가")); service.register(4,text);
        assertEquals("alice",inserted.getUserid()); assertEquals(today,inserted.getServeDate()); assertEquals(text,inserted.getReviewText());
        assertEquals(Arrays.asList("lockUser","today","findOwn","insert"),calls);
    }
    @Test public void serviceAndDatabaseBothRejectDuplicate() {
        own = existing; expect(HttpStatus.CONFLICT, () -> service.register(4,"duplicate")); assertFalse(calls.contains("insert"));
        own = null; duplicate = true; expect(HttpStatus.CONFLICT, () -> service.register(4,"race"));
    }
    @Test public void foreignAndMissingReviewsAreProtected() {
        login("bob"); expect(HttpStatus.FORBIDDEN, () -> service.modify(1L,4,"attack")); expect(HttpStatus.FORBIDDEN, () -> service.remove(1L));
        expect(HttpStatus.NOT_FOUND, () -> service.remove(-1L)); existing = null; expect(HttpStatus.NOT_FOUND, () -> service.modify(99L,4,"missing"));
        assertFalse(calls.contains("update")); assertFalse(calls.contains("delete"));
    }
    @Test public void oldReviewCanOnlyBeDeletedAndDayIsRecheckedDuringWrite() {
        existing.setEditable(false); expect(HttpStatus.FORBIDDEN, () -> service.modify(1L,4,"old"));
        service.remove(1L); assertTrue(calls.contains("delete"));
        existing.setEditable(true); writeCount = 0;
        expect(HttpStatus.FORBIDDEN, () -> service.modify(1L,4,"midnight"));
        expect(HttpStatus.CONFLICT, () -> service.register(4,"midnight"));
    }
    @Test public void pagingUsesOneCapturedDayAndOwnReviewIsFetchedOnEveryPage() {
        own = existing; total = 21; DailyReviewPage page = service.getPage(Integer.MAX_VALUE);
        assertEquals(3,page.getPage()); assertEquals(21,start); assertEquals(30,end); assertSame(own,page.getOwnReview());
        assertEquals(1,Collections.frequency(calls,"today"));
        total = Integer.MAX_VALUE; service.getPage(Integer.MAX_VALUE); assertTrue(end > Integer.MAX_VALUE);
        assertEquals(1,service.getPage(-1).getPage());
    }
}
