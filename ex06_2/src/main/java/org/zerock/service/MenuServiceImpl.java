package org.zerock.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zerock.domain.MenuCategory;
import org.zerock.domain.MenuVO;
import org.zerock.mapper.MenuMapper;

import lombok.Setter;

@Service
public class MenuServiceImpl implements MenuService {

	@Setter(onMethod_ = @Autowired)
	private MenuMapper mapper;

	@Override
	public List<MenuVO> getList() {
		return mapper.getList();
	}

	@Override
	public List<MenuVO> getListByCategory(String category) {
		MenuCategory.fromCode(category);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String userid = auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken ? null : auth.getName();
		return mapper.getListByCategory(category, userid);
	}

	@Override
	public MenuVO get(Long menuId) {
		if (menuId == null || menuId <= 0) {
			throw new IllegalArgumentException("Menu ID must be positive");
		}
		return mapper.read(menuId);
	}

	@Override
	public int getCountByCategory(String category) {
		MenuCategory.fromCode(category);
		return mapper.getCountByCategory(category);
	}
}
