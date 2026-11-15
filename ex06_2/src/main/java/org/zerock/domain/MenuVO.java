package org.zerock.domain;

import java.util.Date;

import lombok.Data;

@Data
public class MenuVO {

	private Long menuId;
	private String menuName;
	private String category;
	private Double avgRating;
	private int bookmarkCnt;
	private Date regDate;
}
