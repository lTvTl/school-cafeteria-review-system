package org.zerock.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.zerock.domain.MenuVO;
import org.zerock.mapper.MenuMapper;

/** Test fixture only: production always uses MyBatis and Oracle. */
public class StubMenuMapper implements MenuMapper {
	public final List<MenuVO> rows = new ArrayList<>();
	public int calls;
	public String lastCategory;
	public RuntimeException failure;

	@Override
	public List<MenuVO> getList() {
		calls++;
		return rows;
	}

	@Override
	public List<MenuVO> getListByCategory(String category) {
		calls++;
		lastCategory = category;
		if (failure != null) {
			throw failure;
		}
		return rows.stream().filter(row -> category.equals(row.getCategory())).collect(Collectors.toList());
	}

	@Override
	public MenuVO read(Long menuId) {
		calls++;
		return rows.stream().filter(row -> menuId.equals(row.getMenuId())).findFirst().orElse(null);
	}

	@Override
	public int getCountByCategory(String category) {
		return getListByCategory(category).size();
	}
}
