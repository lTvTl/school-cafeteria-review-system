-- Fresh installation only. SQL*Plus or SQL Developer Run Script (F5).
-- Existing objects are never dropped, reset, altered or silently reused.
-- Oracle DDL commits implicitly. Run 00_inspect_menu.sql first.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

DECLARE
    existing_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO existing_count FROM user_objects
    WHERE object_name IN ('TBL_MENU', 'SEQ_MENU', 'IDX_MENU_CATEGORY', 'PK_MENU');
    IF existing_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20001,
            'Menu objects already exist. Run 00_inspect_menu.sql and review; no DDL was executed.');
    END IF;
    SELECT COUNT(*) INTO existing_count FROM user_constraints
    WHERE constraint_name IN ('PK_MENU', 'CK_MENU_CATEGORY', 'CK_MENU_RATING', 'CK_MENU_BOOKMARK');
    IF existing_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Menu constraint names already exist; review before installation.');
    END IF;
END;
/

CREATE TABLE tbl_menu (
    menu_id      NUMBER(19) NOT NULL,
    menu_name    VARCHAR2(100) NOT NULL,
    category     VARCHAR2(20) NOT NULL,
    avg_rating   NUMBER(3,2) DEFAULT 0 NOT NULL,
    bookmark_cnt NUMBER(10) DEFAULT 0 NOT NULL,
    regdate      DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT pk_menu PRIMARY KEY (menu_id),
    CONSTRAINT ck_menu_category CHECK (category IN ('MAIN', 'SIDE', 'DESSERT', 'DRINK', 'ETC')),
    CONSTRAINT ck_menu_rating CHECK (avg_rating BETWEEN 0 AND 5),
    CONSTRAINT ck_menu_bookmark CHECK (bookmark_cnt >= 0)
);

CREATE SEQUENCE seq_menu START WITH 1 INCREMENT BY 1
    MINVALUE 1 MAXVALUE 9223372036854775807 NOCYCLE NOCACHE;

CREATE INDEX idx_menu_category ON tbl_menu (category);

-- Every future INSERT must allocate menu_id with seq_menu.NEXTVAL.
-- No trigger, review/bookmark/diet tables or pre-existing data are required.
