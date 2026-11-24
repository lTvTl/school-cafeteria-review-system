package org.zerock.service;

import java.util.List;
import org.zerock.domain.BookmarkStatus;
import org.zerock.domain.BookmarkVO;

public interface BookmarkService {
    BookmarkStatus add(Long menuId);
    BookmarkStatus remove(Long menuId);
    BookmarkStatus getStatus(Long menuId);
    List<BookmarkVO> getMyBookmarks();
}
