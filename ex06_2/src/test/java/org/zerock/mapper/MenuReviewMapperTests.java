package org.zerock.mapper;

import static org.junit.Assert.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;
import org.zerock.domain.Criteria;
import org.zerock.domain.MenuReviewVO;

public class MenuReviewMapperTests {
    @Test public void mapperParsesAndBindsAnonymousListingAndWrites() throws Exception {
        Configuration config = new Configuration();
        String resource = "org/zerock/mapper/MenuReviewMapper.xml";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input);
            new XMLMapperBuilder(input, config, resource, config.getSqlFragments()).parse();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("cri", new Criteria(1, 10)); params.put("userid", null); params.put("dayOffset", 0);
        assertNotNull(config.getMappedStatement(MenuReviewMapper.class.getName() + ".getDietReviews").getBoundSql(params));
        assertNotNull(config.getMappedStatement(MenuReviewMapper.class.getName() + ".getDietList").getBoundSql(params));
        MenuReviewVO review = new MenuReviewVO(); review.setUserid("alice");
        for (String method : new String[]{"insert", "update"}) {
            assertNotNull(config.getMappedStatement(MenuReviewMapper.class.getName() + "." + method).getBoundSql(review));
        }
        for (java.lang.reflect.Method method : MenuReviewMapper.class.getMethods()) {
            assertTrue("Missing mapping: " + method.getName(), config.hasStatement(MenuReviewMapper.class.getName() + "." + method.getName()));
        }
    }
}
