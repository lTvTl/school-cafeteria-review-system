package org.zerock.service;

import static org.junit.Assert.*;
import java.lang.reflect.Proxy;
import java.util.*;
import org.junit.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.zerock.domain.*;
import org.zerock.mapper.*;

public class MenuReviewServiceTest {
    private MenuReviewService service;
    private MenuReviewVO existing;
    private List<String> calls;
    private int duplicates;
    private boolean menuExists;
    private int updateCount;
    private int total;
    private long start;
    private long end;
    private MenuReviewVO inserted;

    @Before public void setUp() {
        calls = new ArrayList<>(); duplicates = 0; menuExists = true; updateCount = 1; total = 0;
        existing = new MenuReviewVO(); existing.setMenuId(1L); existing.setReviewId(10L);
        existing.setUserid("alice"); existing.setEditable(true);
        login("alice");
        MenuMapper menus = (MenuMapper) Proxy.newProxyInstance(MenuMapper.class.getClassLoader(), new Class<?>[]{MenuMapper.class}, (p,m,a) -> {
            if (m.getName().equals("read")) {
                if (!menuExists) return null;
                MenuVO menu = new MenuVO(); menu.setMenuId((Long)a[0]); menu.setCategory("MAIN"); return menu;
            }
            throw new UnsupportedOperationException(m.getName());
        });
        MenuReviewMapper reviews = (MenuReviewMapper) Proxy.newProxyInstance(MenuReviewMapper.class.getClassLoader(), new Class<?>[]{MenuReviewMapper.class}, (p,m,a) -> {
            calls.add(m.getName());
            switch(m.getName()) {
                case "lockMenu": return menuExists ? a[0] : null;
                case "read": return existing;
                case "countToday": return duplicates;
                case "insert": inserted = (MenuReviewVO)a[0]; return 1;
                case "update": return updateCount;
                case "delete": return 1;
                case "refreshAverage": return 1;
                case "getSummary": MenuReviewSummary s = new MenuReviewSummary(); s.setReviewCount(total); return s;
                case "getList": start=(Long)a[1]; end=(Long)a[2]; return Collections.emptyList();
                default: throw new UnsupportedOperationException(m.getName());
            }
        });
        service = new MenuReviewServiceImpl(menus, reviews);
    }

    @After public void clearSecurity() { SecurityContextHolder.clearContext(); }
    private void login(String id) { SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(id,"unused",Collections.emptyList())); }
    private void expect(HttpStatus status, Runnable action) {
        try { action.run(); fail("Expected " + status); }
        catch (ResponseStatusException e) { assertEquals(status, e.getStatus()); }
    }

    @Test public void unauthenticatedMutationIsRejected() {
        SecurityContextHolder.clearContext();
        expect(HttpStatus.UNAUTHORIZED, () -> service.register(1L, 5, "good"));
        expect(HttpStatus.UNAUTHORIZED, () -> service.modify(1L, 10L, 5, "good"));
        expect(HttpStatus.UNAUTHORIZED, () -> service.remove(1L, 10L));
        assertTrue(calls.isEmpty());
    }
    @Test public void validatesStarsAndUnicodeWhitespaceBeforeWriting() {
        for (Integer score : new Integer[]{null, 0, 6}) expect(HttpStatus.BAD_REQUEST, () -> service.register(1L,score,"text"));
        for (String text : new String[]{null,"", " \n\t", "\u00a0\u3000", String.join("",Collections.nCopies(1001,"가"))})
            expect(HttpStatus.BAD_REQUEST, () -> service.register(1L,5,text));
        assertTrue(calls.isEmpty());
    }
    @Test public void acceptsThousandKoreanCharactersAndUsesAuthenticatedIdentity() {
        String text = String.join("",Collections.nCopies(1000,"가"));
        service.register(1L,5,text);
        assertEquals("alice",inserted.getUserid()); assertEquals(text,inserted.getReviewText());
        assertEquals(Arrays.asList("lockMenu","countToday","insert","refreshAverage"),calls);
    }
    @Test public void duplicatesDoNotWriteOrRefresh() {
        duplicates=1; expect(HttpStatus.CONFLICT, () -> service.register(1L,5,"text"));
        assertEquals(Arrays.asList("lockMenu","countToday"),calls);
    }
    @Test public void missingMenuAndInvalidIdAreNotFound() {
        menuExists=false; expect(HttpStatus.NOT_FOUND, () -> service.register(99L,5,"text"));
        expect(HttpStatus.NOT_FOUND, () -> service.getPage(-1L,1));
        expect(HttpStatus.NOT_FOUND, () -> service.getSummary(99L));
    }
    @Test public void otherUsersCannotModifyOrDelete() {
        login("bob"); expect(HttpStatus.FORBIDDEN, () -> service.modify(1L,10L,5,"text"));
        expect(HttpStatus.FORBIDDEN, () -> service.remove(1L,10L));
        assertFalse(calls.contains("update")); assertFalse(calls.contains("delete"));
    }
    @Test public void reviewMustBelongToRequestedMenu() {
        expect(HttpStatus.NOT_FOUND, () -> service.modify(2L,10L,5,"text"));
        expect(HttpStatus.NOT_FOUND, () -> service.remove(2L,10L));
    }
    @Test public void oldReviewCannotBeModifiedButCanBeDeleted() {
        existing.setEditable(false);
        expect(HttpStatus.FORBIDDEN, () -> service.modify(1L,10L,4,"text"));
        service.remove(1L,10L);
        assertTrue(calls.contains("delete")); assertTrue(calls.contains("refreshAverage"));
        assertFalse(calls.contains("update"));
    }
    @Test public void midnightBoundaryBlocksUpdateAndAverageRefresh() {
        updateCount=0; expect(HttpStatus.FORBIDDEN, () -> service.modify(1L,10L,3,"text"));
        assertFalse(calls.contains("refreshAverage"));
    }
    @Test public void modificationsLockBeforeReadingAndRefreshAfterWriting() {
        service.modify(1L,10L,3,"updated");
        assertEquals(Arrays.asList("lockMenu","read","update","refreshAverage"),calls);
    }
    @Test public void pagingClampsBoundsWithoutIntegerOverflow() {
        total=21; assertEquals(3,service.getPage(1L,Integer.MAX_VALUE).getPage()); assertEquals(21,start); assertEquals(30,end);
        assertEquals(1,service.getPage(1L,-10).getPage()); assertEquals(1,start);
        total=Integer.MAX_VALUE; service.getPage(1L,Integer.MAX_VALUE); assertTrue(end > Integer.MAX_VALUE);
    }
}
