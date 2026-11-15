-- OPTIONAL disposable fixture. Run in a dedicated test session, UTF-8.
-- All fixture rows and average changes are rolled back. Never COMMIT this script.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
DECLARE
    test_user VARCHAR2(50) := 'review_test_' || SUBSTR(RAWTOHEX(SYS_GUID()), 1, 20);
    test_menu NUMBER;
BEGIN
    INSERT INTO tbl_member(userid, userpw, username, enabled)
    VALUES(test_user, 'disabled-test-account', 'Review test fixture', '0');
    SELECT seq_menu.NEXTVAL INTO test_menu FROM dual;
    INSERT INTO tbl_menu(menu_id, menu_name, category) VALUES(test_menu, 'Review test fixture', 'MAIN');
    INSERT INTO tbl_menu_review(review_id, menu_id, userid, star_score, review_text, regdate)
    VALUES(seq_menu_review.NEXTVAL, test_menu, test_user, 5, '오늘의 테스트 리뷰', SYSDATE);
    INSERT INTO tbl_menu_review(review_id, menu_id, userid, star_score, review_text, regdate)
    VALUES(seq_menu_review.NEXTVAL, test_menu, test_user, 3, '이전 날짜 테스트 리뷰', SYSDATE - 1);
    UPDATE tbl_menu SET avg_rating=(SELECT ROUND(AVG(star_score),2) FROM tbl_menu_review WHERE menu_id=test_menu AND is_deleted=0)
    WHERE menu_id=test_menu;
END;
/
SELECT m.menu_name, m.avg_rating, r.star_score, r.review_text
FROM tbl_menu m JOIN tbl_menu_review r ON r.menu_id=m.menu_id
WHERE m.menu_name='Review test fixture' AND r.is_deleted=0;
ROLLBACK;
