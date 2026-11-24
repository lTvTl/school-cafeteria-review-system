package org.zerock.mapper;

import static org.junit.Assert.*;
import java.util.*;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

public class BookmarkMapperXmlTest {
    @Test public void allOperationsBindUserAndMenuAndDoNotAlterReviews() {
        Configuration c=new Configuration();c.addMapper(BookmarkMapper.class);
        Map<String,Object> p=new HashMap<>();p.put("userid","' OR 1=1 --");p.put("menuId",1L);
        for(java.lang.reflect.Method m:BookmarkMapper.class.getMethods()) {
            BoundSql sql=c.getMappedStatement(BookmarkMapper.class.getName()+"."+m.getName()).getBoundSql(p);
            assertFalse(sql.getSql().contains("' OR 1=1"));assertFalse(sql.getParameterMappings().isEmpty());
            assertFalse(sql.getSql().contains("tbl_menu_review"));assertFalse(sql.getSql().contains("tbl_daily_review"));assertFalse(sql.getSql().contains("avg_rating"));
        }
        assertTrue(c.getMappedStatement(BookmarkMapper.class.getName()+".lockMenu").getBoundSql(p).getSql().contains("FOR UPDATE"));
        assertTrue(c.getMappedStatement(BookmarkMapper.class.getName()+".refreshCount").getBoundSql(p).getSql().contains("COUNT(*)"));
    }
    @Test public void categoryQueryIncludesUserStateAndActualTotalTogether() {
        Configuration c=new Configuration();c.addMapper(MenuMapper.class);Map<String,Object> p=new HashMap<>();p.put("userid",null);p.put("category","MAIN");
        BoundSql sql=c.getMappedStatement(MenuMapper.class.getName()+".getListByCategory").getBoundSql(p);
        assertTrue(sql.getSql().contains("as bookmarked"));assertTrue(sql.getSql().contains("as bookmark_cnt"));
        assertEquals(2,sql.getParameterMappings().size());assertEquals("userid",sql.getParameterMappings().get(0).getProperty());
    }
}
