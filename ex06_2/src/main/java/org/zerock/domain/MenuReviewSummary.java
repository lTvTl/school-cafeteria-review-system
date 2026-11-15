package org.zerock.domain;

import lombok.Data;

@Data
public class MenuReviewSummary {
    private Long menuId;
    private double avgRating;
    private int reviewCount;
    private int star1;
    private int star2;
    private int star3;
    private int star4;
    private int star5;
}
