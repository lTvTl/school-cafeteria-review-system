package org.zerock.mapper;

import static org.junit.Assert.*;
import java.util.*;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

public class DailyReviewMapperXmlTest {
    @Test public void statementsBindValuesAndNeverTouchMenuOrDietData() {
        Configuration c = new Configuration(); c.addMapper(DailyReviewMapper.class);
        Map<String,Object> p = new HashMap<>(); p.put("userid","' OR 1=1 --"); p.put("reviewText","<script>x</script>");
        for (java.lang.reflect.Method method : DailyReviewMapper.class.getMethods()) {
            BoundSql sql = c.getMappedStatement(DailyReviewMapper.class.getName()+"."+method.getName()).getBoundSql(p);
            assertFalse(sql.getSql().contains("' OR 1=1 --")); assertFalse(sql.getSql().contains("<script>"));
            assertFalse(sql.getSql().contains("tbl_menu")); assertFalse(sql.getSql().contains("tbl_daily_diet"));
            if (!method.getName().equals("today")) assertFalse(sql.getParameterMappings().isEmpty());
        }
        for (String id : Arrays.asList("today","insert","update","read"))
            assertTrue(c.getMappedStatement(DailyReviewMapper.class.getName()+"."+id).getBoundSql(p).getSql().contains("Asia/Seoul"));
        c.addMapper(MenuReviewMapper.class);
        for (java.lang.reflect.Method method : MenuReviewMapper.class.getMethods())
            assertFalse(c.getMappedStatement(MenuReviewMapper.class.getName()+"."+method.getName()).getBoundSql(p).getSql().contains("tbl_daily_review"));
    }
}
