-- SQL Developer에서 프로젝트 DB 계정으로 연결한 뒤 스크립트 실행(F5).
-- 테이블 생성 SQL은 이미 실행되어 있어야 합니다. 이 파일은 데이터만 추가합니다.
-- 오늘 중식 3개(메뉴 변경 포함), 내일 예정 중식 2개를 생성합니다.
-- 회원과 리뷰는 만들지 않습니다. 기존 계정으로 로그인하여 직접 리뷰를 작성하세요.
-- 반복 실행하면 예시 데이터가 추가됩니다. 미커밋 작업이 없는 연결에서 실행하세요.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
DECLARE
    v_main NUMBER;
    v_side NUMBER;
    v_actual NUMBER;
    v_drink NUMBER;
    v_count NUMBER;
    v_today DATE := TRUNC(SYSDATE);
BEGIN
    SELECT seq_menu.NEXTVAL INTO v_main FROM dual;
    SELECT seq_menu.NEXTVAL INTO v_side FROM dual;
    SELECT seq_menu.NEXTVAL INTO v_actual FROM dual;
    SELECT seq_menu.NEXTVAL INTO v_drink FROM dual;
    INSERT INTO tbl_menu(menu_id, menu_name, category) VALUES (v_main, '제육볶음 (예시)', 'MAIN');
    INSERT INTO tbl_menu(menu_id, menu_name, category) VALUES (v_side, '배추김치 (예시)', 'SIDE');
    INSERT INTO tbl_menu(menu_id, menu_name, category) VALUES (v_actual, '돈까스 (예시)', 'MAIN');
    INSERT INTO tbl_menu(menu_id, menu_name, category) VALUES (v_drink, '요구르트 (예시)', 'DRINK');

    -- 오늘: 제육볶음 대신 돈까스 제공, 김치와 요구르트는 공지대로 제공.
    INSERT INTO tbl_daily_diet(diet_id, serve_date, meal_time, planned_menu_id, actual_menu_id, is_matched)
        VALUES(seq_diet.NEXTVAL, v_today, 'LUNCH', v_main, v_actual, 'N');
    INSERT INTO tbl_daily_diet(diet_id, serve_date, meal_time, planned_menu_id, actual_menu_id, is_matched)
        VALUES(seq_diet.NEXTVAL, v_today, 'LUNCH', v_side, v_side, 'Y');
    INSERT INTO tbl_daily_diet(diet_id, serve_date, meal_time, planned_menu_id, actual_menu_id, is_matched)
        VALUES(seq_diet.NEXTVAL, v_today, 'LUNCH', v_drink, v_drink, 'Y');

    -- 내일: 실제 제공 메뉴는 아직 미확정이므로 리뷰 작성 대상에서 제외.
    INSERT INTO tbl_daily_diet(diet_id, serve_date, meal_time, planned_menu_id)
        VALUES(seq_diet.NEXTVAL, v_today + 1, 'LUNCH', v_main);
    INSERT INTO tbl_daily_diet(diet_id, serve_date, meal_time, planned_menu_id)
        VALUES(seq_diet.NEXTVAL, v_today + 1, 'LUNCH', v_side);

    -- 이번 실행의 식단 5개가 모두 생성된 경우에만 커밋.
    SELECT COUNT(*) INTO v_count FROM tbl_daily_diet
    WHERE planned_menu_id IN (v_main, v_side, v_drink)
      AND serve_date IN (v_today, v_today + 1);
    IF v_count <> 5 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Expected 5 sample diet rows.');
    END IF;
    COMMIT;
EXCEPTION WHEN OTHERS THEN
    ROLLBACK;
    RAISE;
END;
/

-- 확인: 오늘/내일의 예시 식단을 표시합니다. 이전에 넣은 예시도 포함됩니다.
SELECT TO_CHAR(d.serve_date, 'YYYY-MM-DD') AS serve_date,
       d.meal_time, p.menu_name AS planned_menu,
       a.menu_name AS actual_menu, d.is_matched, d.diet_id
FROM tbl_daily_diet d
JOIN tbl_menu p ON p.menu_id = d.planned_menu_id
LEFT JOIN tbl_menu a ON a.menu_id = d.actual_menu_id
WHERE d.serve_date IN (TRUNC(SYSDATE), TRUNC(SYSDATE) + 1)
  AND p.menu_name LIKE '%(예시)'
ORDER BY d.serve_date, d.diet_id;
