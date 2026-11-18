package org.zerock.domain;

import java.util.Date;
import lombok.Data;

@Data
public class MenuReviewVO {
	private Long reviewId;
	private Long menuId;
	private Long dietId;
	private String userid;
	private String userName;
	private Integer starScore;
	private String reviewText;
	private Date regDate;
	private Date updateDate;
	private boolean editable;
}
