package org.zerock.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zerock.domain.MenuVO;

public interface MenuMapper {

	public List<MenuVO> getList();

	public List<MenuVO> getListByCategory(@Param("category") String category, @Param("userid") String userid);

	public MenuVO read(@Param("menuId") Long menuId);

	public int getCountByCategory(@Param("category") String category);

	public List<MenuVO> getBookmarkedListByUserid(@Param("userid") String userid);
}
