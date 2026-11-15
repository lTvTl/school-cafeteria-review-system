package org.zerock.mapper;

import static org.junit.Assert.*;
import java.util.*;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

public class MenuReviewMapperXmlTest {
    @Test public void allStatementsLoadAndBindUntrustedValues() {
        Configuration c=new Configuration();c.addMapper(MenuReviewMapper.class);
        Map<String,Object> p=new HashMap<>();p.put("menuId",1L);p.put("reviewId",2L);p.put("userid","' OR 1=1 --");p.put("reviewText","<script>test</script>");
        for(String id:Arrays.asList("lockMenu","getList","read","countToday","getSummary","insert","update","delete","refreshAverage")) {
            BoundSql sql=c.getMappedStatement("org.zerock.mapper.MenuReviewMapper."+id).getBoundSql(p);
            assertFalse(sql.getSql().contains("' OR 1=1 --"));assertFalse(sql.getSql().contains("<script>"));assertFalse(sql.getParameterMappings().isEmpty());
        }
    }
}
