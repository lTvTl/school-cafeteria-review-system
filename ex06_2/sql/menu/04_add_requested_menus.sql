-- Add the five requested menus; preserve any existing menu with the same name.
-- Run with SQL*Plus or SQL Developer Run Script (F5), using UTF-8.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

DECLARE
    PROCEDURE add_menu(p_name VARCHAR2, p_category VARCHAR2) IS
    BEGIN
        INSERT INTO tbl_menu (menu_id, menu_name, category, avg_rating, bookmark_cnt)
        SELECT seq_menu.NEXTVAL, p_name, p_category, 0, 0
        FROM dual
        WHERE NOT EXISTS (SELECT 1 FROM tbl_menu WHERE menu_name = p_name);
    END;
BEGIN
    LOCK TABLE tbl_menu IN EXCLUSIVE MODE WAIT 5;
    add_menu('배추김치', 'SIDE');
    add_menu('야채볶음밥', 'MAIN');
    add_menu('쫄면', 'MAIN');
    add_menu('된장국', 'ETC');
    add_menu('닭강정', 'MAIN');
END;
/
COMMIT;