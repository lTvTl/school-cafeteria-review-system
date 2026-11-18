-- Read-only inspection and consistency checks.
SELECT column_name, data_type, char_length, char_used, nullable
FROM user_tab_columns WHERE table_name = 'TBL_MENU_REVIEW' ORDER BY column_id;
SELECT constraint_name, constraint_type, status FROM user_constraints WHERE table_name = 'TBL_MENU_REVIEW';
SELECT index_name, uniqueness, status FROM user_indexes WHERE table_name = 'TBL_MENU_REVIEW';
SELECT menu_id, COUNT(*) AS review_count, NVL(ROUND(AVG(star_score), 2), 0) AS avg_rating
FROM tbl_menu_review WHERE is_deleted = 0 GROUP BY menu_id;
-- Expected: no duplicates and no inconsistent averages for reviewed menus.
SELECT menu_id, userid, TRUNC(regdate), COUNT(*) FROM tbl_menu_review
GROUP BY menu_id, userid, TRUNC(regdate) HAVING COUNT(*) > 1;
SELECT m.menu_id, m.avg_rating, r.avg_rating AS actual_average
FROM tbl_menu m JOIN (
    SELECT menu_id, ROUND(AVG(star_score), 2) avg_rating FROM tbl_menu_review WHERE is_deleted = 0 GROUP BY menu_id
) r ON r.menu_id = m.menu_id WHERE m.avg_rating <> r.avg_rating;
