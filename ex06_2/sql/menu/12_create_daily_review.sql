-- Run once in the application's schema after tbl_member exists. No existing table is modified.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
DECLARE
    n NUMBER;
BEGIN
    SELECT COUNT(*) INTO n FROM user_objects
    WHERE object_name IN ('TBL_DAILY_REVIEW','SEQ_DAILY_REVIEW','PK_DAILY_REVIEW','UX_DAILY_REVIEW_DAY','IDX_DAILY_REVIEW_LIST');
    IF n > 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Daily review objects already exist; inspect before applying.');
    END IF;
    SELECT COUNT(*) INTO n FROM user_constraints
    WHERE constraint_name IN ('PK_DAILY_REVIEW','FK_DR_USER','CK_DR_STAR','CK_DR_DATE','CK_DR_TEXT','UX_DAILY_REVIEW_DAY');
    IF n > 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Daily review constraint names already exist.');
    END IF;
END;
/
CREATE TABLE tbl_daily_review (
    daily_review_id NUMBER(19) NOT NULL,
    serve_date DATE NOT NULL,
    userid VARCHAR2(50) NOT NULL,
    star_score NUMBER(1) NOT NULL,
    review_text VARCHAR2(1000 CHAR) NOT NULL,
    regdate DATE DEFAULT SYSDATE NOT NULL,
    updatedate DATE,
    CONSTRAINT pk_daily_review PRIMARY KEY (daily_review_id),
    CONSTRAINT fk_dr_user FOREIGN KEY (userid) REFERENCES tbl_member(userid),
    CONSTRAINT ck_dr_star CHECK (star_score BETWEEN 1 AND 5),
    CONSTRAINT ck_dr_date CHECK (serve_date = TRUNC(serve_date)),
    CONSTRAINT ck_dr_text CHECK (LENGTH(review_text) BETWEEN 1 AND 1000 AND REGEXP_LIKE(review_text, '[^[:space:]]')),
    CONSTRAINT ux_daily_review_day UNIQUE (serve_date, userid)
);
CREATE SEQUENCE seq_daily_review START WITH 1 INCREMENT BY 1
    MINVALUE 1 MAXVALUE 9223372036854775807 NOCYCLE NOCACHE;
CREATE INDEX idx_daily_review_list ON tbl_daily_review(serve_date, regdate DESC, daily_review_id DESC);
-- The application explicitly writes KST timestamps; the default is for manual DBA inserts.
-- Hard deletion releases the UNIQUE day/user key; concurrent registrations are still constrained.

-- Compare the legacy diet/menu day with KST. A DBA must align the DB host clock to KST
-- when these differ; ALTER SESSION TIME_ZONE alone does not change SYSDATE.
SELECT DBTIMEZONE, SESSIONTIMEZONE, SYSDATE,
    SYSTIMESTAMP AT TIME ZONE 'Asia/Seoul' AS korea_now,
    CASE WHEN TRUNC(SYSDATE) = TRUNC(CAST(SYSTIMESTAMP AT TIME ZONE 'Asia/Seoul' AS DATE))
         THEN 'MATCH' ELSE 'CHECK_DB_HOST_TIMEZONE' END AS legacy_day_check
FROM dual;
