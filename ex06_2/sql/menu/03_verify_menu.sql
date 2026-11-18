-- Read-only verification. Fresh schema + optional seed: counts 3,3,2,2,1.
SELECT category, COUNT(*) AS menu_count FROM tbl_menu
GROUP BY category ORDER BY category;

SELECT menu_id, menu_name, category, avg_rating, bookmark_cnt, regdate
FROM tbl_menu ORDER BY category, menu_name, menu_id;

-- Expected: 0 invalid rows.
SELECT COUNT(*) AS invalid_rows FROM tbl_menu
WHERE menu_id IS NULL OR menu_name IS NULL OR category IS NULL
   OR category NOT IN ('MAIN', 'SIDE', 'DESSERT', 'DRINK', 'ETC')
   OR avg_rating IS NULL OR avg_rating < 0 OR avg_rating > 5
   OR bookmark_cnt IS NULL OR bookmark_cnt < 0 OR regdate IS NULL;
