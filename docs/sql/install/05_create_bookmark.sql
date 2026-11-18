-- 사용자별 하트 표시용 추가 테이블만 생성합니다.
-- tbl_member(userid PK/UK), tbl_menu가 필요합니다.
-- 기존 tbl_bookmark/seq_bookmark가 있으면 실행하지 마세요.
-- UTF-8 / SQL Developer F5. Oracle DDL은 자동 커밋됩니다.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
DECLARE
    n NUMBER;
BEGIN
    SELECT COUNT(*) INTO n FROM user_objects WHERE object_name IN
        ('TBL_BOOKMARK', 'SEQ_BOOKMARK', 'PK_BOOKMARK', 'UQ_BOOKMARK_USER_MENU', 'IX_BOOKMARK_MENU');
    IF n > 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Bookmark objects already exist. No DDL executed.');
    END IF;
    SELECT COUNT(*) INTO n FROM user_constraints WHERE constraint_name IN
        ('PK_BOOKMARK', 'FK_BOOKMARK_USER', 'FK_BOOKMARK_MENU', 'UQ_BOOKMARK_USER_MENU');
    IF n > 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Bookmark constraint names already exist. No DDL executed.');
    END IF;
    SELECT COUNT(*) INTO n FROM user_tables WHERE table_name IN ('TBL_MEMBER', 'TBL_MENU');
    IF n <> 2 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Existing tbl_member and tbl_menu are required.');
    END IF;
END;
/
CREATE TABLE tbl_bookmark (
    bookmark_id NUMBER(19) NOT NULL,
    userid VARCHAR2(50) NOT NULL,
    menu_id NUMBER(19) NOT NULL,
    regdate DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT pk_bookmark PRIMARY KEY (bookmark_id),
    CONSTRAINT fk_bookmark_user FOREIGN KEY (userid) REFERENCES tbl_member(userid),
    CONSTRAINT fk_bookmark_menu FOREIGN KEY (menu_id) REFERENCES tbl_menu(menu_id),
    CONSTRAINT uq_bookmark_user_menu UNIQUE (userid, menu_id)
);
CREATE SEQUENCE seq_bookmark START WITH 1 INCREMENT BY 1
    MINVALUE 1 MAXVALUE 9223372036854775807 NOCYCLE NOCACHE;
CREATE INDEX ix_bookmark_menu ON tbl_bookmark(menu_id);
