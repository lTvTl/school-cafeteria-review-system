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
import org.zerock.domain.BookmarkStatus;
import org.zerock.domain.BookmarkVO;
import org.zerock.mapper.BookmarkMapper;
import org.zerock.mapper.MenuMapper;

@Service
public class BookmarkServiceImpl implements BookmarkService {
    private final MenuMapper menus;
    private final BookmarkMapper bookmarks;

    @Autowired
    public BookmarkServiceImpl(MenuMapper menus, BookmarkMapper bookmarks) {
        this.menus = menus;
        this.bookmarks = bookmarks;
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken ? null : auth.getName();
    }

    private String authenticatedUser() {
        String user = currentUser();
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 후 북마크해 주세요.");
        return user;
    }

    private void validateId(Long menuId) {
        if (menuId == null || menuId <= 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "음식을 찾을 수 없습니다.");
    }

    @Override
    @Transactional
    public BookmarkStatus add(Long menuId) { return change(menuId, true); }

    @Override
    @Transactional
    public BookmarkStatus remove(Long menuId) { return change(menuId, false); }

    private BookmarkStatus change(Long menuId, boolean add) {
        String userid = authenticatedUser();
        validateId(menuId);
        // All bookmark mutations lock the same parent row, including repeated requests.
        // This lock order also agrees with the existing menu review service.
        if (bookmarks.lockMenu(menuId) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "음식을 찾을 수 없습니다.");
        if (add) {
            if (bookmarks.exists(menuId, userid) == 0) {
                BookmarkVO row = new BookmarkVO(); row.setUserid(userid); row.setMenuId(menuId);
                try {
                    if (bookmarks.insert(row) != 1) throw new IllegalStateException("Bookmark insert failed");
                } catch (DuplicateKeyException e) {
                    // The UNIQUE key also protects against inserts outside this service.
                    if (bookmarks.exists(menuId, userid) == 0) throw e;
                }
            }
        } else {
            bookmarks.delete(menuId, userid); // Idempotent: deleting a missing bookmark is successful.
        }
        if (bookmarks.refreshCount(menuId) != 1) throw new IllegalStateException("Bookmark count update failed");
        return status(menuId, userid);
    }

    private BookmarkStatus status(Long menuId, String userid) {
        return new BookmarkStatus(menuId, userid != null && bookmarks.exists(menuId, userid) > 0, bookmarks.countByMenu(menuId));
    }

    @Override
    @Transactional(readOnly = true)
    public BookmarkStatus getStatus(Long menuId) {
        validateId(menuId);
        if (menus.read(menuId) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "음식을 찾을 수 없습니다.");
        return status(menuId, currentUser());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkVO> getMyBookmarks() { return bookmarks.getListByUser(authenticatedUser()); }
}
