package org.zerock.domain;

import java.util.Date;
import lombok.Data;

@Data
public class DailyReviewVO {
    private Long dailyReviewId;
    private Date serveDate;
    private String userid;
    private String userName;
    private Integer starScore;
    private String reviewText;
    private Date regDate;
    private Date updateDate;
    private boolean editable;
}
