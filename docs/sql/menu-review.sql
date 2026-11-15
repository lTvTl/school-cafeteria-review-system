-- 신규 테이블이 없는 경우 한 번만 실행하세요. 기존 tbl_member(userid)가 필요합니다.
-- Oracle DDL은 자동 커밋됩니다. 실행 전 대상 계정 및 기존 객체를 확인하세요.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

-- SQL Developer에서는 '스크립트 실행(F5)'으로 실행합니다.
DECLARE
    v_existing NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_existing FROM user_objects
    WHERE object_name IN ('TBL_MENU', 'TBL_DAILY_DIET', 'TBL_MENU_REVIEW',
        'SEQ_MENU', 'SEQ_DIET', 'SEQ_MENU_REVIEW');
    IF v_existing > 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Existing menu/review objects found. Stop and inspect before running DDL.');
    END IF;
END;
/

CREATE TABLE tbl_menu (
    menu_id NUMBER(10) NOT NULL,
    menu_name VARCHAR2(100 CHAR) NOT NULL,
    category VARCHAR2(10) NOT NULL,
    avg_rating NUMBER(3,2) DEFAULT 0 NOT NULL,
    bookmark_cnt NUMBER(10) DEFAULT 0 NOT NULL,
    regdate DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT pk_menu PRIMARY KEY (menu_id),
    CONSTRAINT ck_menu_category CHECK
        (category IN ('MAIN', 'SIDE', 'DESSERT', 'DRINK', 'ETC')),
    CONSTRAINT ck_menu_rating CHECK (avg_rating BETWEEN 0 AND 5),
    CONSTRAINT ck_menu_bookmarks CHECK (bookmark_cnt >= 0)
);

CREATE TABLE tbl_daily_diet (
    diet_id NUMBER(10) NOT NULL,
    serve_date DATE NOT NULL,
    meal_time VARCHAR2(10) NOT NULL,
    planned_menu_id NUMBER(10) NOT NULL,
    actual_menu_id NUMBER(10),
    is_matched CHAR(1),
    CONSTRAINT pk_daily_diet PRIMARY KEY (diet_id),
    CONSTRAINT fk_diet_planned FOREIGN KEY (planned_menu_id)
        REFERENCES tbl_menu(menu_id),
    CONSTRAINT fk_diet_actual FOREIGN KEY (actual_menu_id)
        REFERENCES tbl_menu(menu_id),
    CONSTRAINT uq_diet_actual UNIQUE (diet_id, actual_menu_id),
    CONSTRAINT uq_diet_planned UNIQUE
        (serve_date, meal_time, planned_menu_id),
    CONSTRAINT ck_diet_date CHECK (serve_date = TRUNC(serve_date)),
    CONSTRAINT ck_diet_meal CHECK
        (meal_time IN ('BREAKFAST', 'LUNCH', 'DINNER')),
    CONSTRAINT ck_diet_match CHECK (
        (actual_menu_id IS NULL AND is_matched IS NULL)
        OR (actual_menu_id IS NOT NULL AND is_matched IS NOT NULL AND (
            (actual_menu_id = planned_menu_id AND is_matched = 'Y')
            OR (actual_menu_id <> planned_menu_id AND is_matched = 'N')
        ))
    )
);

CREATE TABLE tbl_menu_review (
    review_id NUMBER(10) NOT NULL,
    menu_id NUMBER(10) NOT NULL,
    diet_id NUMBER(10) NOT NULL,
    userid VARCHAR2(50) NOT NULL,
    star_score NUMBER NOT NULL,
    review_text VARCHAR2(1000 CHAR) NOT NULL,
    regdate DATE DEFAULT SYSDATE NOT NULL,
    updatedate DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT pk_menu_review PRIMARY KEY (review_id),
    CONSTRAINT fk_review_member FOREIGN KEY (userid)
        REFERENCES tbl_member(userid),
    CONSTRAINT fk_review_menu FOREIGN KEY (menu_id)
        REFERENCES tbl_menu(menu_id),
    CONSTRAINT fk_review_diet_menu FOREIGN KEY (diet_id, menu_id)
        REFERENCES tbl_daily_diet(diet_id, actual_menu_id),
    CONSTRAINT uq_review_user_diet UNIQUE (diet_id, userid),
    CONSTRAINT ck_review_score CHECK
        (star_score BETWEEN 1 AND 5 AND star_score = TRUNC(star_score)),
    CONSTRAINT ck_review_text CHECK (TRIM(review_text) IS NOT NULL)
);

CREATE SEQUENCE seq_menu START WITH 1 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE seq_diet START WITH 1 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE seq_menu_review START WITH 1 INCREMENT BY 1 NOCYCLE;

-- 미확정 메뉴는 제외하고 실제 제공이 확정된 메뉴만 중복을 제한합니다.
CREATE UNIQUE INDEX uq_diet_served ON tbl_daily_diet (
    CASE WHEN actual_menu_id IS NOT NULL THEN serve_date END,
    CASE WHEN actual_menu_id IS NOT NULL THEN meal_time END,
    CASE WHEN actual_menu_id IS NOT NULL THEN actual_menu_id END
);
CREATE INDEX ix_diet_planned ON tbl_daily_diet(planned_menu_id);
CREATE INDEX ix_diet_actual ON tbl_daily_diet(actual_menu_id);
CREATE INDEX ix_review_menu ON tbl_menu_review(menu_id);
CREATE INDEX ix_review_user ON tbl_menu_review(userid);
CREATE INDEX ix_review_diet_menu ON tbl_menu_review(diet_id, menu_id);
CREATE INDEX ix_review_recent ON tbl_menu_review(regdate DESC, review_id DESC);


