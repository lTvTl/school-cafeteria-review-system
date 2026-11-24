package org.zerock.domain;

import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class DailyReviewPage {
    private Date serveDate;
    private DailyReviewSummary summary;
    private List<DailyReviewVO> reviews;
    private DailyReviewVO ownReview;
    private int page;
    private int totalPages;
}
