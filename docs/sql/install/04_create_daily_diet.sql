-- 메인페이지 식단용 추가 테이블만 생성합니다. 기존 00~08 SQL은 변경하지 않습니다.
-- tbl_menu가 필요합니다. 기존 tbl_daily_diet/seq_diet가 있으면 실행하지 마세요.
-- UTF-8 / SQL Developer F5. Oracle DDL은 자동 커밋됩니다.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
DECLARE
    n NUMBER;
BEGIN
    SELECT COUNT(*) INTO n FROM user_objects WHERE object_name IN
        ('TBL_DAILY_DIET', 'SEQ_DIET', 'PK_DAILY_DIET', 'UQ_DIET_PLANNED', 'UQ_DIET_SERVED', 'IX_DIET_ACTUAL');
    IF n > 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Daily diet objects already exist. No DDL executed.');
    END IF;
    SELECT COUNT(*) INTO n FROM user_constraints WHERE constraint_name IN
        ('PK_DAILY_DIET', 'FK_DIET_PLANNED', 'FK_DIET_ACTUAL', 'UQ_DIET_PLANNED', 'CK_DIET_DATE', 'CK_DIET_MEAL', 'CK_DIET_MATCH');
    IF n > 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Diet constraint names already exist. No DDL executed.');
    END IF;
    SELECT COUNT(*) INTO n FROM user_constraints
    WHERE table_name = 'TBL_MENU' AND constraint_type = 'P' AND status = 'ENABLED';
    IF n = 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Create tbl_menu with 01_create_menu.sql first.');
    END IF;
END;
/
CREATE TABLE tbl_daily_diet (
    diet_id NUMBER(19) NOT NULL,
    serve_date DATE NOT NULL,
    meal_time VARCHAR2(10) NOT NULL,
    planned_menu_id NUMBER(19) NOT NULL,
    actual_menu_id NUMBER(19),
    is_matched CHAR(1),
    CONSTRAINT pk_daily_diet PRIMARY KEY (diet_id),
    CONSTRAINT fk_diet_planned FOREIGN KEY (planned_menu_id) REFERENCES tbl_menu(menu_id),
    CONSTRAINT fk_diet_actual FOREIGN KEY (actual_menu_id) REFERENCES tbl_menu(menu_id),
    CONSTRAINT uq_diet_planned UNIQUE (serve_date, meal_time, planned_menu_id),
    CONSTRAINT ck_diet_date CHECK (serve_date = TRUNC(serve_date)),
    CONSTRAINT ck_diet_meal CHECK (meal_time IN ('BREAKFAST', 'LUNCH', 'DINNER')),
    CONSTRAINT ck_diet_match CHECK (
        (actual_menu_id IS NULL AND is_matched IS NULL)
        OR (actual_menu_id IS NOT NULL AND is_matched IS NOT NULL AND (
            (actual_menu_id = planned_menu_id AND is_matched = 'Y')
            OR (actual_menu_id <> planned_menu_id AND is_matched = 'N')
        ))
    )
);
CREATE SEQUENCE seq_diet START WITH 1 INCREMENT BY 1
    MINVALUE 1 MAXVALUE 9223372036854775807 NOCYCLE NOCACHE;
CREATE UNIQUE INDEX uq_diet_served ON tbl_daily_diet (
    CASE WHEN actual_menu_id IS NOT NULL THEN serve_date END,
    CASE WHEN actual_menu_id IS NOT NULL THEN meal_time END,
    CASE WHEN actual_menu_id IS NOT NULL THEN actual_menu_id END
);
CREATE INDEX ix_diet_actual ON tbl_daily_diet(actual_menu_id);
