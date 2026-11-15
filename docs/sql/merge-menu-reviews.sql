-- Existing HEAD/main review schemas need this compatibility migration.
-- Run manually in SQL Developer (F5), in the application's schema.
-- Oracle DDL commits automatically. Inspect/backup the schema before execution.
-- tbl_menu, tbl_daily_diet, tbl_member, tbl_menu_review must already exist.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
DECLARE
    n NUMBER;
BEGIN
    SELECT COUNT(*) INTO n FROM user_tab_columns
    WHERE table_name = 'TBL_MENU_REVIEW' AND column_name = 'IS_DELETED';
    IF n = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE tbl_menu_review ADD (is_deleted NUMBER(1) DEFAULT 0 NOT NULL CHECK (is_deleted IN (0,1)))';
    END IF;
    SELECT COUNT(*) INTO n FROM user_tab_columns
    WHERE table_name = 'TBL_MENU_REVIEW' AND column_name = 'DIET_ID' AND nullable = 'N';
    IF n > 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE tbl_menu_review MODIFY (diet_id NULL)';
    END IF;
    -- Oracle composite UNIQUE also restricts partially NULL keys. Popup reviews have no diet ID.
    SELECT COUNT(*) INTO n FROM user_constraints WHERE constraint_name = 'UQ_REVIEW_USER_DIET';
    IF n > 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE tbl_menu_review DROP CONSTRAINT uq_review_user_diet';
    END IF;
    SELECT COUNT(*) INTO n FROM user_indexes WHERE index_name = 'UX_REVIEW_ACTIVE_DIET';
    IF n = 0 THEN
        EXECUTE IMMEDIATE 'CREATE UNIQUE INDEX ux_review_active_diet ON tbl_menu_review (
            CASE WHEN diet_id IS NOT NULL AND is_deleted = 0 THEN diet_id END,
            CASE WHEN diet_id IS NOT NULL AND is_deleted = 0 THEN userid END)';
    END IF;
END;
/
-- Keep main's ux_menu_review_day if already installed. This preserves its daily limit,
-- including deletion history. Without it the popup service still serializes daily checks.
-- No existing data is deleted. Do not run either fresh-install CREATE TABLE script again.
