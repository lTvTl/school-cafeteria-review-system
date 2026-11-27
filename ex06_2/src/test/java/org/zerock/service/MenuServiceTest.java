package org.zerock.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.zerock.domain.MenuVO;
import org.zerock.menu.StubMenuMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class MenuServiceTest {
	@After public void clearSecurity() { SecurityContextHolder.clearContext(); }
	private StubMenuMapper mapper;
	private MenuServiceImpl service;

	@Before
	public void setUp() {
		mapper = new StubMenuMapper();
		service = new MenuServiceImpl();
		service.setMapper(mapper);
	}

	@Test
	public void rejectsInvalidCodesBeforeBothQueries() {
		for (String invalid : new String[] { null, "", "main", "OTHER", "MAIN' OR 1=1--" }) {
			assertThrows(IllegalArgumentException.class, () -> service.getListByCategory(invalid));
			assertThrows(IllegalArgumentException.class, () -> service.getCountByCategory(invalid));
		}
		assertEquals(0, mapper.calls);
	}

	@Test
	public void listDetailAndCountUseSameMenuIdentifier() {
		MenuVO row = new MenuVO();
		row.setMenuId(42L);
		row.setCategory("SIDE");
		mapper.rows.add(row);
		assertEquals(1, service.getList().size());
		assertSame(row, service.get(42L));
		assertEquals(1, service.getCountByCategory("SIDE"));
		assertEquals(0, service.getCountByCategory("MAIN"));
		assertNull(service.get(43L));
	}

	@Test
	public void rejectsInvalidMenuIdsWithoutQuery() {
		for (Long id : new Long[] { null, 0L, -1L }) {
			assertThrows(IllegalArgumentException.class, () -> service.get(id));
		}
		assertEquals(0, mapper.calls);
	}

	@Test public void categoryReadsAuthenticatedBookmarkStateInOneQuery() {
		SecurityContextHolder.clearContext(); service.getListByCategory("MAIN");
		assertNull(mapper.lastUserid); assertEquals(1,mapper.calls);
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("alice","unused",java.util.Collections.emptyList()));
		service.getListByCategory("SIDE"); assertEquals("alice",mapper.lastUserid); assertEquals(2,mapper.calls);
	}

	@Test
	public void bookmarkedListReturnsEmptyForBlankUseridAndQueriesTrimmedUserid() {
		assertTrue(service.getBookmarkedListByUserid(null).isEmpty());
		assertTrue(service.getBookmarkedListByUserid("   ").isEmpty());
		assertEquals(0, mapper.calls);

		MenuVO bookmarked = new MenuVO();
		bookmarked.setMenuId(10L);
		bookmarked.setMenuName("돈까스");
		bookmarked.setBookmarked(true);
		MenuVO unbookmarked = new MenuVO();
		unbookmarked.setMenuId(11L);
		unbookmarked.setMenuName("김치찌개");
		unbookmarked.setBookmarked(false);
		mapper.rows.add(bookmarked);
		mapper.rows.add(unbookmarked);

		java.util.List<MenuVO> result = service.getBookmarkedListByUserid(" alice ");
		assertEquals(1, result.size());
		assertEquals(Long.valueOf(10L), result.get(0).getMenuId());
		assertEquals("alice", mapper.lastUserid);
		assertEquals(1, mapper.calls);
	}
}
