package org.zerock.mapper;

import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;
import org.zerock.domain.DailyDietVO;

public class DailyDietMapperTests {
    @Test public void homeQueryBindsUserAndDateWithoutReviewDependency() {
        Configuration config = new Configuration();
        config.addMapper(DailyDietMapper.class);
        for (String userid : new String[]{null, "user0", "' OR 1=1 --"}) {
            for (int offset = 0; offset <= 1; offset++) {
                Map<String, Object> params = new HashMap<>();
                params.put("userid", userid);
                params.put("dayOffset", offset);
                BoundSql sql = config.getMappedStatement(DailyDietMapper.class.getName() + ".getDietList").getBoundSql(params);
                assertFalse(sql.getSql().contains("tbl_menu_review"));
                assertFalse(sql.getSql().contains("' OR 1=1 --"));
                assertTrue(sql.getSql().contains("tbl_bookmark"));
                assertTrue(sql.getSql().contains("NVL(d.actual_menu_id, d.planned_menu_id)"));
                assertEquals(2, sql.getParameterMappings().size());
                assertEquals("userid", sql.getParameterMappings().get(0).getProperty());
                assertEquals("dayOffset", sql.getParameterMappings().get(1).getProperty());
            }
        }
        assertEquals(boolean.class, config.getReflectorFactory().findForClass(DailyDietVO.class).getSetterType("bookmarked"));
    }
}
