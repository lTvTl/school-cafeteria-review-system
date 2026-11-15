package org.zerock.service;

import java.util.List;

import org.zerock.domain.MenuVO;

public interface MenuService {

	public List<MenuVO> getList();

	/** Accepts only MAIN, SIDE, DESSERT, DRINK or ETC. */
	public List<MenuVO> getListByCategory(String category);

	/** Returns null if the positive menu ID does not exist. */
	public MenuVO get(Long menuId);

	public int getCountByCategory(String category);
}
