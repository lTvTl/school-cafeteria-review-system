package org.zerock.domain;

import java.util.Date;
import lombok.Data;

@Data
public class BookmarkVO {
    private Long bookmarkId;
    private String userid;
    private Long menuId;
    private Date regDate;
}
