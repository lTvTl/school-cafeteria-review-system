package org.zerock.mapper;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.zerock.domain.MenuVO;

public class MenuMapperXmlTest {
	private Configuration configuration;
	private static final String PREFIX = "org.zerock.mapper.MenuMapper.";

	@Before
	public void loadMapperFromClasspath() {
		configuration = new Configuration();
		// Same interface/XML classpath discovery used by MyBatis-Spring scanning.
		configuration.addMapper(MenuMapper.class);
	}

	@Test
	public void explicitResultMapIncludesEveryColumn() {
		ResultMap resultMap = configuration.getResultMap(PREFIX + "menuResultMap");
		assertEquals(MenuVO.class, resultMap.getType());
		Map<String, String> actual = new HashMap<>();
		for (ResultMapping mapping : resultMap.getResultMappings()) {
			actual.put(mapping.getColumn(), mapping.getProperty());
		}
		assertEquals(7, actual.size());
		assertEquals("bookmarked", actual.get("bookmarked"));
		assertEquals("menuId", actual.get("menu_id"));
		assertEquals("menuName", actual.get("menu_name"));
		assertEquals("category", actual.get("category"));
		assertEquals("avgRating", actual.get("avg_rating"));
		assertEquals("bookmarkCnt", actual.get("bookmark_cnt"));
		assertEquals("regDate", actual.get("regdate"));
	}

	@Test
	public void categoryQueriesBindValuesRatherThanInterpolateSql() {
		Map<String, Object> params = new HashMap<>();
		String injection = "MAIN' OR 1=1--";
		params.put("category", injection);
		for (String statement : new String[] { "getListByCategory", "getCountByCategory" }) {
			BoundSql bound = configuration.getMappedStatement(PREFIX + statement).getBoundSql(params);
			String sql = bound.getSql().replaceAll("\\s+", " ").trim();
			assertTrue(sql.contains("where category = ?"));
			assertFalse(sql.contains(injection));
			assertTrue(bound.getParameterMappings().stream().anyMatch(p -> p.getProperty().equals("category")));
		}
	}

	@Test
	public void listOrderingAndDetailParameterAreRegistered() {
		for (String statement : new String[] { "getList", "getListByCategory" }) {
			String sql = configuration.getMappedStatement(PREFIX + statement).getBoundSql(null).getSql();
			assertTrue(sql.contains("order by menu_name asc, menu_id asc"));
		}
		BoundSql detail = configuration.getMappedStatement(PREFIX + "read").getBoundSql(null);
		assertTrue(detail.getSql().contains("where menu_id = ?"));
		assertEquals("menuId", detail.getParameterMappings().get(0).getProperty());
	}
}
