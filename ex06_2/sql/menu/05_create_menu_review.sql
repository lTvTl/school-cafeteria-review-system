-- Oracle SQL*Plus / SQL Developer (F5). Use the application's schema, not SYS/SYSTEM.
-- Fresh installation only; never drop or overwrite an existing object.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
DECLARE
    n NUMBER;
BEGIN
    SELECT COUNT(*) INTO n FROM user_objects
    WHERE object_name IN ('TBL_MENU_REVIEW', 'SEQ_MENU_REVIEW', 'UX_MENU_REVIEW_DAY', 'IDX_MENU_REVIEW_LIST', 'PK_MENU_REVIEW');
    IF n > 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Review objects already exist; inspect before applying this script.');
    END IF;
    SELECT COUNT(*) INTO n FROM user_constraints
    WHERE constraint_name IN ('PK_MENU_REVIEW','FK_MR_MENU','FK_MR_USER','CK_MR_STAR','CK_MR_TEXT','CK_MR_DELETED');
    IF n > 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Review constraint names already exist.');
    END IF;
END;
/
CREATE TABLE tbl_menu_review (
    review_id NUMBER(19) NOT NULL,
    menu_id NUMBER(19) NOT NULL,
    diet_id NUMBER(19),
    userid VARCHAR2(50) NOT NULL,
    star_score NUMBER(1) NOT NULL,
    review_text VARCHAR2(1000 CHAR) NOT NULL,
    regdate DATE DEFAULT SYSDATE NOT NULL,
    updatedate DATE,
    is_deleted NUMBER(1) DEFAULT 0 NOT NULL,
    CONSTRAINT pk_menu_review PRIMARY KEY (review_id),
    CONSTRAINT fk_mr_menu FOREIGN KEY (menu_id) REFERENCES tbl_menu(menu_id),
    CONSTRAINT fk_mr_user FOREIGN KEY (userid) REFERENCES tbl_member(userid),
    CONSTRAINT ck_mr_star CHECK (star_score BETWEEN 1 AND 5),
    CONSTRAINT ck_mr_text CHECK (REGEXP_LIKE(review_text, '[^[:space:]]')),
    CONSTRAINT ck_mr_deleted CHECK (is_deleted IN (0, 1))
);
CREATE SEQUENCE seq_menu_review START WITH 1 INCREMENT BY 1
    MINVALUE 1 MAXVALUE 9223372036854775807 NOCYCLE NOCACHE;
-- TRUNC(SYSDATE) is shared with service queries: Oracle server date defines a day.
CREATE UNIQUE INDEX ux_menu_review_day ON tbl_menu_review(menu_id, userid, TRUNC(regdate));
CREATE INDEX idx_menu_review_list ON tbl_menu_review(menu_id, regdate DESC, review_id DESC);
-- diet_id intentionally has no FK until the daily-diet schema and key are agreed.
-- Deleted content is erased; the (menu, user, day) key remains to enforce the daily limit.
