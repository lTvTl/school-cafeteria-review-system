package org.zerock.domain;

import java.util.Locale;

/** Shared category codes for the URL, service validation and view labels. */
public enum MenuCategory {
	MAIN("메인메뉴"), SIDE("반찬"), DESSERT("디저트"), DRINK("음료"), ETC("기타");

	private final String label;

	MenuCategory(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public String getType() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static MenuCategory fromType(String type) {
		return fromCode(type == null ? "MAIN" : type.trim().toUpperCase(Locale.ROOT));
	}

	public static MenuCategory fromCode(String code) {
		if (code != null) {
			for (MenuCategory category : values()) {
				if (category.name().equals(code)) {
					return category;
				}
			}
		}
		throw new IllegalArgumentException("Unsupported menu category");
	}
}
