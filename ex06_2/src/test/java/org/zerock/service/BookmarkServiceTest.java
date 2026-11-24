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
import org.zerock.mapper.BookmarkMapper;
import org.zerock.menu.StubMenuMapper;

public class BookmarkServiceTest {
    private BookmarkService service;
    private final List<String> calls=new ArrayList<>();
    private BookmarkVO inserted;
    private int exists, count;
    private boolean missing;
    private String deletedUser;
    @Before public void setup() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("alice","unused",Collections.emptyList()));
        StubMenuMapper menus=new StubMenuMapper(); MenuVO menu=new MenuVO();menu.setMenuId(1L);menus.rows.add(menu);
        BookmarkMapper mapper=(BookmarkMapper)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{BookmarkMapper.class},(p,m,a)->{
            calls.add(m.getName());
            switch(m.getName()) {
                case "lockMenu": return missing?null:a[0];
                case "exists": return exists;
                case "insert": inserted=(BookmarkVO)a[0];exists=1;count++;return 1;
                case "delete": deletedUser=(String)a[1];int removed=exists;count-=exists;exists=0;return removed;
                case "refreshCount":return 1;
                case "countByMenu":return count;
                case "getListByUser": assertEquals("alice",a[0]);return Collections.emptyList();
                default:throw new AssertionError(m.getName());
            }
        });
        service=new BookmarkServiceImpl(menus,mapper);
    }
    @After public void clear(){SecurityContextHolder.clearContext();}
    private void expect(HttpStatus status,Runnable action){try{action.run();fail("Expected "+status);}catch(ResponseStatusException e){assertEquals(status,e.getStatus());}}
    @Test public void writesAndPrivateListRequireAuthentication() {
        SecurityContextHolder.clearContext();expect(HttpStatus.UNAUTHORIZED,()->service.add(1L));expect(HttpStatus.UNAUTHORIZED,()->service.remove(1L));expect(HttpStatus.UNAUTHORIZED,()->service.getMyBookmarks());assertTrue(calls.isEmpty());
        assertFalse(service.getStatus(1L).isBookmarked());assertFalse(calls.contains("exists"));
    }
    @Test public void repeatedAddIsIdempotentAndUsesAuthenticatedUser() {
        BookmarkStatus first=service.add(1L);assertTrue(first.isBookmarked());assertEquals(1,first.getBookmarkCnt());assertEquals("alice",inserted.getUserid());
        service.add(1L);assertEquals(1,Collections.frequency(calls,"insert"));assertEquals(2,Collections.frequency(calls,"refreshCount"));
        assertEquals("lockMenu",calls.get(0));assertTrue(calls.indexOf("insert")<calls.indexOf("refreshCount"));
    }
    @Test public void removalCannotDeleteAnotherUserAndNeverMakesCountNegative() {
        count=2;exists=1;BookmarkStatus state=service.remove(1L);assertFalse(state.isBookmarked());assertEquals(1,state.getBookmarkCnt());assertEquals("alice",deletedUser);
        assertEquals(1,service.remove(1L).getBookmarkCnt());
    }
    @Test public void invalidAndMissingMenusReturn404BeforeWriting() {
        for(Long id:new Long[]{null,0L,-1L})expect(HttpStatus.NOT_FOUND,()->service.add(id));assertTrue(calls.isEmpty());
        missing=true;expect(HttpStatus.NOT_FOUND,()->service.add(99L));expect(HttpStatus.NOT_FOUND,()->service.remove(99L));assertFalse(calls.contains("insert"));assertFalse(calls.contains("delete"));
        expect(HttpStatus.NOT_FOUND,()->service.getStatus(99L));
    }
    @Test public void readsReturnActualCountAndUserList(){count=5;exists=1;assertEquals(5,service.getStatus(1L).getBookmarkCnt());assertTrue(service.getStatus(1L).isBookmarked());assertTrue(service.getMyBookmarks().isEmpty());}
}
