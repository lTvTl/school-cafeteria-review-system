package org.zerock.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.zerock.domain.BookmarkVO;

public interface BookmarkMapper {
    Long lockMenu(@Param("menuId") Long menuId);
    int exists(@Param("menuId") Long menuId, @Param("userid") String userid);
    int insert(BookmarkVO bookmark);
    int delete(@Param("menuId") Long menuId, @Param("userid") String userid);
    int countByMenu(@Param("menuId") Long menuId);
    List<BookmarkVO> getListByUser(@Param("userid") String userid);
    int refreshCount(@Param("menuId") Long menuId);
}
