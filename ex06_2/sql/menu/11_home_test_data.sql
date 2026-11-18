-- 선택: 테스트 DB에서만 F5 실행. 기존 user0 사용, 계정/리뷰는 생성하지 않습니다.
-- 01의 메뉴 테이블과 09/10의 식단/북마크 테이블이 필요합니다.
-- [홈테스트] 메뉴만 추가/사용하며 기존 일반 메뉴를 변경하지 않습니다.
-- COMMIT되어 화면에서 확인 가능합니다. 별도 연결에서 실행하세요.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
DECLARE
    v_userid tbl_member.userid%TYPE := 'user0';
    v_today DATE := TRUNC(SYSDATE);
    v_main NUMBER;
    v_planned NUMBER;
    v_side NUMBER;
    v_dessert NUMBER;
    v_drink NUMBER;
    v_etc NUMBER;
    n NUMBER;
    FUNCTION find_menu(p_name VARCHAR2, p_category VARCHAR2) RETURN NUMBER IS
        v_id NUMBER;
        v_count NUMBER;
        v_category tbl_menu.category%TYPE;
    BEGIN
        SELECT COUNT(*), MIN(menu_id), MIN(category) INTO v_count, v_id, v_category
        FROM tbl_menu WHERE menu_name = p_name;
        IF v_count > 1 OR (v_count = 1 AND v_category <> p_category) THEN
            RAISE_APPLICATION_ERROR(-20010, 'Duplicate or mismatched home test menu.');
        END IF;
        IF v_count = 0 THEN
            SELECT seq_menu.NEXTVAL INTO v_id FROM dual;
            INSERT INTO tbl_menu(menu_id, menu_name, category) VALUES(v_id, p_name, p_category);
        END IF;
        RETURN v_id;
    END;
    PROCEDURE add_diet(p_date DATE, p_planned NUMBER, p_actual NUMBER) IS
        v_count NUMBER;
        v_actual NUMBER;
    BEGIN
        SELECT COUNT(*), MIN(actual_menu_id) INTO v_count, v_actual FROM tbl_daily_diet
        WHERE serve_date = p_date AND meal_time = 'LUNCH' AND planned_menu_id = p_planned;
        IF v_count = 0 THEN
            INSERT INTO tbl_daily_diet(diet_id, serve_date, meal_time, planned_menu_id, actual_menu_id, is_matched)
            VALUES(seq_diet.NEXTVAL, p_date, 'LUNCH', p_planned, p_actual,
                CASE WHEN p_actual IS NULL THEN NULL WHEN p_actual = p_planned THEN 'Y' ELSE 'N' END);
        ELSIF NVL(v_actual, -1) <> NVL(p_actual, -1) THEN
            RAISE_APPLICATION_ERROR(-20011, 'Existing test diet differs. No existing diet overwritten.');
        END IF;
    END;
BEGIN
    SELECT COUNT(*) INTO n FROM tbl_member WHERE userid = v_userid;
    IF n <> 1 THEN
        RAISE_APPLICATION_ERROR(-20012, 'Existing user0 login account is required.');
    END IF;
    LOCK TABLE tbl_menu IN EXCLUSIVE MODE WAIT 5;
    v_main := find_menu('[홈테스트] 돈까스', 'MAIN');
    v_planned := find_menu('[홈테스트] 제육볶음', 'MAIN');
    v_side := find_menu('[홈테스트] 배추김치', 'SIDE');
    v_dessert := find_menu('[홈테스트] 푸딩', 'DESSERT');
    v_drink := find_menu('[홈테스트] 주스', 'DRINK');
    v_etc := find_menu('[홈테스트] 추가밥', 'ETC');
    add_diet(v_today, v_planned, v_main);
    add_diet(v_today, v_side, v_side);
    add_diet(v_today, v_dessert, v_dessert);
    add_diet(v_today, v_drink, v_drink);
    add_diet(v_today, v_etc, v_etc);
    add_diet(v_today + 1, v_planned, NULL);
    add_diet(v_today + 1, v_side, NULL);
    add_diet(v_today + 1, v_dessert, NULL);
    add_diet(v_today + 1, v_drink, NULL);
    add_diet(v_today + 1, v_etc, NULL);
    FOR item IN (SELECT v_main menu_id FROM dual UNION ALL SELECT v_planned FROM dual) LOOP
        INSERT INTO tbl_bookmark(bookmark_id, userid, menu_id)
        SELECT seq_bookmark.NEXTVAL, v_userid, item.menu_id FROM dual
        WHERE NOT EXISTS (SELECT 1 FROM tbl_bookmark WHERE userid = v_userid AND menu_id = item.menu_id);
        UPDATE tbl_menu SET bookmark_cnt = (SELECT COUNT(*) FROM tbl_bookmark WHERE menu_id = item.menu_id)
        WHERE menu_id = item.menu_id;
    END LOOP;
    SELECT COUNT(*) INTO n FROM tbl_daily_diet
    WHERE serve_date IN (v_today, v_today + 1) AND meal_time = 'LUNCH'
      AND planned_menu_id IN (v_planned, v_side, v_dessert, v_drink, v_etc);
    IF n <> 10 THEN
        RAISE_APPLICATION_ERROR(-20013, 'Expected 10 home test diet rows.');
    END IF;
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK;
    RAISE;
END;
/
-- user0: 오늘 돈까스/내일 제육볶음에 하트. 비로그인/다른 계정에는 해당 하트 없음.
