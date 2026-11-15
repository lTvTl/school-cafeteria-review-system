package org.zerock.domain;

import java.util.List;
import lombok.Data;

@Data
public class MenuReviewPage {
    private MenuVO menu;
    private MenuReviewSummary summary;
    private List<MenuReviewVO> reviews;
    private int page;
    private int totalPages;
}
