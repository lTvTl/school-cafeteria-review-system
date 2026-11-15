-- Requested end-to-end example menu. No fabricated reviews or ratings.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
INSERT INTO tbl_menu(menu_id, menu_name, category, avg_rating, bookmark_cnt)
SELECT seq_menu.NEXTVAL, '돈까스', 'MAIN', 0, 0 FROM dual
WHERE NOT EXISTS (SELECT 1 FROM tbl_menu WHERE menu_name='돈까스');
COMMIT;
