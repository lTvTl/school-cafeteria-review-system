package org.zerock.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookmarkStatus {
    private Long menuId;
    private boolean bookmarked;
    private int bookmarkCnt;
}
