package org.zerock.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.zerock.domain.MenuVO;
import org.zerock.menu.StubMenuMapper;

public class MenuServiceTest {
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
}
