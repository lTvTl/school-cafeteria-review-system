package org.zerock.domain;

import java.util.Date;
import lombok.Data;

@Data
public class DailyDietVO {
	private Long dietId;
	private Date serveDate;
	private String mealTime;
	private Long plannedMenuId;
	private Long actualMenuId;
	private String isMatched;
	private String menuName;
	private String category;
	private String plannedMenuName;
	private boolean bookmarked;
}
