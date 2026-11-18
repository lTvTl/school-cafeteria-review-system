-- OPTIONAL: development/test schema only, after schema validation/creation.
-- UTF-8 client required. Re-running skips existing (category, menu_name) pairs.
-- Run once at a time; this is not a production upsert/concurrency API.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

DECLARE
    PROCEDURE add_menu(p_name VARCHAR2, p_category VARCHAR2,
                       p_rating NUMBER, p_bookmarks NUMBER) IS
    BEGIN
        INSERT INTO tbl_menu (menu_id, menu_name, category, avg_rating, bookmark_cnt)
        SELECT seq_menu.NEXTVAL, p_name, p_category, p_rating, p_bookmarks
        FROM dual
        WHERE NOT EXISTS (
            SELECT 1 FROM tbl_menu WHERE category = p_category AND menu_name = p_name
        );
    END;
BEGIN
    add_menu('돈까스', 'MAIN', 4.50, 12);
    add_menu('제육볶음', 'MAIN', 4.25, 9);
    add_menu('비빔밥', 'MAIN', 4.00, 7);
    add_menu('배추김치', 'SIDE', 3.75, 2);
    add_menu('계란말이', 'SIDE', 4.75, 8);
    add_menu('멸치볶음', 'SIDE', 3.50, 1);
    add_menu('요거트', 'DESSERT', 4.25, 4);
    add_menu('푸딩', 'DESSERT', 4.50, 6);
    add_menu('식혜', 'DRINK', 4.00, 3);
    add_menu('주스', 'DRINK', 3.75, 2);
    add_menu('추가밥', 'ETC', 0, 0);
END;
/
COMMIT;
