# SQL 용도와 실행 순서

## 관리자 권한 설정 — admin

[01_grant_admin.sql](admin/01_grant_admin.sql)은 기존 회원에게 `ROLE_ADMIN`을 부여하는 선택 실행 파일입니다. 기본 아이디 `admin0`를 실제 관리자 계정으로 바꿔 실행하고 다시 로그인하세요. 테이블이나 계정을 생성하지 않습니다. 자세한 내용은 [관리자 안내](../admin.md)를 참고하세요.

SQL 파일은 용도별로 분류했으며 **13개 SQL의 내용은 변경하지 않았습니다.** 각 폴더 안에서 01부터 번호를 매겼습니다. 모든 파일을 번호순으로 실행하는 방식이 아닙니다.

애플리케이션 Oracle 계정에서 UTF-8로 열어 SQL Developer **F5**로 실행합니다. 서버 시작 시 자동 실행되지 않습니다.

## 1. 최초 설치 필수 — install

**메뉴 관련 테이블이 없는 새 DB에서만** 다음 순서로 한 번씩 실행합니다. 완전히 빈 DB는 `01_database.sql`부터 실행합니다. 회원·게시판 등 공통 테이블이 이미 있다면 해당 파일을 건너뜁니다.

| 순서 | 파일 | 생성 대상 |
|---|---|---|
| 1 | [01_database.sql](install/01_database.sql) | 회원·권한·게시판·댓글·첨부파일·자동 로그인 공통 테이블 |
| 2 | [02_create_menu.sql](install/02_create_menu.sql) | 메뉴 테이블·시퀀스 |
| 3 | [03_create_menu_review.sql](install/03_create_menu_review.sql) | 메뉴 리뷰 테이블·시퀀스·하루 1회 제약 |
| 4 | [04_create_daily_diet.sql](install/04_create_daily_diet.sql) | 오늘·내일 식단 테이블·시퀀스 |
| 5 | [05_create_bookmark.sql](install/05_create_bookmark.sql) | 사용자별 북마크 테이블·시퀀스 |

이미 존재하는 테이블의 생성 파일은 **건너뜁니다.** 재시작할 때마다 실행하지 않습니다. `02~05`는 기존 객체를 발견하면 중단합니다. `01_database.sql`에는 재실행 방지 장치가 없으므로 빈 DB에서만 실행하세요. Oracle DDL은 자동 커밋되므로 중간 실패 시 생성된 객체를 먼저 확인하세요.

## 2. 화면 테스트 데이터 — test-data (선택)

| 파일 | 용도·특징 |
|---|---|
| [01_seed_menu_optional.sql](test-data/01_seed_menu_optional.sql) | 카테고리별 예시 메뉴 11개. 가상 평점·북마크 수 포함 |
| [02_add_requested_menus.sql](test-data/02_add_requested_menus.sql) | 배추김치·야채볶음밥·쫄면·된장국·닭강정 추가. 동일 이름은 건너뜀 |
| [03_add_pork_cutlet.sql](test-data/03_add_pork_cutlet.sql) | 돈까스가 없을 때만 추가. 평점·북마크 수 0 |
| [04_home_test_data.sql](test-data/04_home_test_data.sql) | **홈 테스트 권장.** 기존 `user0` 기준 `[홈테스트]` 메뉴 6개, 오늘·내일 점심 각 5개, 북마크 2개 저장. 리뷰·회원은 생성하지 않음 |

이 폴더의 파일은 COMMIT하여 데이터를 저장합니다. **홈 테스트는 `04_home_test_data.sql`만 실행해도 됩니다.** 나머지는 원하는 메뉴 데이터가 필요할 때 선택하며, 식단을 자동으로 연결하지 않습니다.

`04_home_test_data.sql`은 같은 날 재실행해도 같은 식단/북마크를 중복 추가하지 않습니다. 기존 테스트 식단이 다르면 중단합니다. 날짜가 바뀌어 전날 미확정 식단과 충돌하는 경우에도 기존 데이터를 확인해야 합니다. 회원 비밀번호·권한은 변경하지 않습니다.

## 3. 상태 조회·검증 — verify (읽기 전용)

| 파일 | 용도 |
|---|---|
| [01_inspect_menu.sql](verify/01_inspect_menu.sql) | 메뉴 테이블·제약조건·인덱스·시퀀스 점검. 설치 전 확인 가능 |
| [02_verify_menu.sql](verify/02_verify_menu.sql) | 메뉴 목록·카테고리별 개수·잘못된 값 확인. 메뉴 생성 후 실행 |
| [03_verify_menu_review.sql](verify/03_verify_menu_review.sql) | 리뷰 구조·하루 중복·평균 일치 확인. 리뷰 생성 후 실행 |

식단/북마크 객체가 이미 있는지는 다음으로 확인합니다.

```sql
SELECT object_name, object_type FROM user_objects
WHERE object_name IN ('TBL_DAILY_DIET', 'SEQ_DIET', 'TBL_BOOKMARK', 'SEQ_BOOKMARK');
```

테이블과 시퀀스가 둘 다 없을 때만 해당 생성 파일을 실행합니다. 한쪽만 있거나 기존 구조가 다르면 먼저 점검하세요. 조회 결과의 예시 개수는 기존 데이터에 따라 달라질 수 있습니다.

## 4. 일회성 리뷰 DB 테스트 — rollback-test (선택)

[01_menu_review_test_data.sql](rollback-test/01_menu_review_test_data.sql)은 테스트 회원·메뉴·리뷰를 잠깐 생성하고 조회한 뒤 **ROLLBACK**합니다. 실행 후 데이터가 남지 않으므로 웹 화면용 데이터 추가 파일이 아닙니다.

## 빠른 선택

- **새 DB:** `install`의 5개를 위 순서대로 → 필요하면 테스트 데이터 → `verify` 확인.
- **이미 설치된 DB:** 생성 파일은 생략 → 원하는 테스트 데이터만 추가.
- **홈 화면 확인:** 설치 완료 + 로그인 가능한 `user0` → `test-data/04_home_test_data.sql` → [화면 확인 안내](../home.md).

데이터를 넣는 테스트는 별도 테스트 DB/연결에서 실행하세요. COMMIT/ROLLBACK은 같은 연결의 다른 미저장 작업에도 적용되며, 시퀀스 번호는 롤백해도 되돌아가지 않습니다. 실제 Oracle 적용은 별도 확인이 필요합니다.

`01_database.sql`은 `tbl_member`에 로그인 계정을 넣지 않습니다. 홈 테스트용 `user0`와 권한은 별도로 준비해야 합니다. SQL 본문은 보존했으므로 일부 주석·오류 메시지의 예전 파일 번호는 현재 파일명과 다를 수 있습니다.
