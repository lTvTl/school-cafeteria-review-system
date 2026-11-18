# SQL 실행 방법

## 최초 설치 순서

회원·권한 테이블(`tbl_member`, `tbl_member_auth`)이 먼저 있어야 합니다. 현재 폴더에는 공통 테이블 생성용 `database.sql`이 없으므로, 빈 DB라면 공통 테이블 SQL을 별도로 준비해야 합니다.

1. [01_create_menu.sql](menu/01_create_menu.sql) — 메뉴
2. [05_create_menu_review.sql](menu/05_create_menu_review.sql) — 메뉴 리뷰
3. [09_create_daily_diet.sql](menu/09_create_daily_diet.sql) — 오늘·내일 식단
4. [10_create_bookmark.sql](menu/10_create_bookmark.sql) — 북마크

이미 만든 테이블의 생성 SQL은 다시 실행하지 않습니다. DDL은 자동 커밋되므로 중간 실패 시 생성된 객체를 먼저 확인하세요.

## 화면 테스트

로그인 가능한 `user0` 계정을 먼저 준비한 뒤 [11_home_test_data.sql](menu/11_home_test_data.sql)을 실행하면 오늘·내일 식단과 북마크 테스트 데이터가 저장됩니다. 회원은 생성하지 않습니다.

추가 예시 메뉴가 필요할 때만 다음 파일을 실행합니다.

- [02_seed_menu_optional.sql](menu/02_seed_menu_optional.sql): 예시 메뉴 11개와 임의 평점·북마크 수
- [04_add_requested_menus.sql](menu/04_add_requested_menus.sql): 추가 메뉴
- [08_add_pork_cutlet.sql](menu/08_add_pork_cutlet.sql): 돈까스

## 확인용 SQL

- [00_inspect_menu.sql](menu/00_inspect_menu.sql): 기존 메뉴 객체 확인
- [03_verify_menu.sql](menu/03_verify_menu.sql): 메뉴 데이터 확인
- [06_verify_menu_review.sql](menu/06_verify_menu_review.sql): 리뷰 검증
- [07_menu_review_test_data.sql](menu/07_menu_review_test_data.sql): 리뷰 테스트 후 **ROLLBACK**하므로 데이터가 남지 않음

테스트 데이터는 테스트 DB의 별도 연결에서 실행하세요. 파일의 COMMIT/ROLLBACK은 같은 연결의 다른 미저장 작업에도 적용됩니다.