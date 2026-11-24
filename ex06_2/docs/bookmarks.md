# 카테고리 북마크 구현 및 검증

2026-11-24 기준. 원격 `main`의 `520fd1dd8eed9c82de2db667caba5701375bdc0f`를 반영하고 기존 로컬 일별 리뷰 작업을 보존했습니다. 로컬 작업 백업은 `target/bookmark-check/pre-bookmark.patch`입니다. 원격 push와 PR 생성은 하지 않았습니다.

## 화면과 사용 방법

- `/category` 표는 **음식명 / 평균 별점 / 북마크 수**의 3개 열입니다. 카테고리 선택 버튼, 제목, 메뉴 개수와 DB의 카테고리 필드는 유지합니다.
- 로그인 상태에서 빈 하트를 누르면 등록, 빨간 하트를 누르면 해제합니다. 오른쪽 숫자는 전체 사용자의 실제 북마크 수입니다. 새로고침하면 DB 기준 상태를 다시 표시합니다.
- 비로그인 사용자는 조회할 수 있고 하트를 누르면 로그인 안내와 링크를 봅니다.
- 하트 클릭은 페이지 이동이나 팝업을 발생시키지 않습니다. 음식명 클릭은 기존 520×760 리뷰 팝업을 열고, 리뷰 저장 후 부모의 평균 별점 갱신도 유지합니다.
- 메인페이지에는 북마크 버튼·변경 요청·이벤트를 추가하지 않았습니다. 기존 `diet.bookmarked` 하트는 클릭할 수 없는 상태 표시로 유지합니다.
- 버튼은 네이티브 키보드/터치 조작과 `aria-label`, `aria-pressed`를 지원합니다. 오류 안내는 live region으로 표시합니다. 긴 음식명은 줄바꿈하며 390px 모바일 화면에서 숫자까지 보이도록 검증했습니다.

## API

| 요청 | 동작 |
|---|---|
| `POST /menu/{menuId}/bookmark/add` | 현재 로그인 사용자의 음식 북마크 등록 |
| `POST /menu/{menuId}/bookmark/remove` | 현재 로그인 사용자의 음식 북마크 해제 |

성공 응답 예:

```json
{"menuId": 5, "bookmarked": true, "bookmarkCnt": 1}
```

응답은 트랜잭션 완료 후 반환합니다. `userid` 요청 파라미터는 받지 않고 Spring Security 인증 정보만 사용합니다. JSON 응답은 실제 요청자의 확정 상태와 전체 개수를 나타냅니다.

두 경로는 인증된 POST만 허용하고 CSRF 검증을 유지합니다. 클라이언트는 JSP의 CSRF 헤더 이름·토큰을 전송합니다. 없는 메뉴는 404, GET 변경 요청은 405입니다. 비로그인 요청은 기존 보안 설정에 따라 로그인으로 리다이렉트됩니다. CSRF 누락은 기존 접근 오류 처리로 거부됩니다. UI는 세션 만료·접근 오류·잘못된 JSON·네트워크 실패 시 기존 하트와 숫자를 유지하고 안내합니다.

## 저장과 동시성

기존 `tbl_bookmark`와 `seq_bookmark`를 사용합니다. 별도의 북마크 테이블이나 프레임워크는 추가하지 않았습니다.

등록·해제는 `tbl_menu`의 해당 행을 `FOR UPDATE`로 잠근 다음 처리합니다. 등록 전 중복 검사와 기존 `(userid, menu_id)` UNIQUE 제약을 함께 사용합니다. 이미 등록한 메뉴에 add를 반복하거나 이미 해제한 메뉴에 remove를 반복해도 성공한 현재 상태를 반환합니다.

변경 후 `SELECT COUNT(*) FROM tbl_bookmark WHERE menu_id = ?`로 `tbl_menu.bookmark_cnt`를 갱신합니다. 등록·해제·개수 갱신은 하나의 트랜잭션이며, 개수 갱신 실패 시 변경도 롤백됩니다. 동일 메뉴의 여러 사용자 요청도 같은 메뉴 잠금을 사용합니다. 기존 음식 리뷰의 `avg_rating`은 건드리지 않습니다.

`MenuService.getListByCategory()`와 `CategoryController`의 호출 구조는 유지합니다. 서비스가 현재 사용자 ID를 얻어 매퍼에 전달하며, 카테고리 목록 SQL 한 번에서 `EXISTS`로 사용자별 `bookmarked`와 실제 전체 `bookmarkCnt`를 함께 조회합니다. 음식마다 별도 SQL을 반복하지 않습니다. 비로그인 사용자는 `bookmarked=false`입니다. 목록 개수는 실제 북마크 데이터를 집계하므로 예전 seed의 임의 `bookmark_cnt` 값도 화면에 사용하지 않습니다.

## DB 적용 내역

- 새 SQL 파일을 만들거나 기존 스키마 파일을 수정하지 않았습니다.
- 로컬 DB에 식단·북마크 테이블이 없어 기존 `09_create_daily_diet.sql`, `10_create_bookmark.sql`을 그대로 적용했습니다. 이미 있는 환경에서는 다시 실행하면 안 됩니다.
- 기존 회원·메뉴·리뷰 테이블과 데이터를 삭제하거나 초기화하지 않았습니다.
- 테스트는 임의 UUID 계정과 전용 메뉴·식단·리뷰·북마크만 사용하고 정리했습니다. 최종 확인 시 원래 회원 3명, 음식 6개, 음식 리뷰 1개가 보존됐습니다.
- 실제 식단은 아직 입력하지 않아 메인페이지에 등록된 식단이 없다는 안내가 표시됩니다. 테스트 전용 식단을 사용해 하트 연동은 검증했습니다.

## 테스트 결과

- Maven 전체 운영 코드 컴파일 성공.
- 단위·보안 테스트 65개, Oracle 통합 테스트 24개, 실제 서버 HTTP 테스트 3개, Chrome 브라우저 테스트 2개: 합계 94개, 실패·오류 0개.
- JS 테스트 6개 파일 통과. 북마크 중복 클릭, CSRF 헤더, 실패 시 상태 보존, 독립 행 상태, 만료 세션 안내와 기존 팝업·별점·식단 기능을 포함합니다.
- 실제 Oracle: 반복 add/remove, UNIQUE 제약, 두 사용자의 독립 상태, 동일 사용자·다른 사용자의 동시 요청, 등록/해제 혼합 요청, 실패 시 롤백, 메뉴별 집계 일치, 식단 하트와 리뷰 평균 독립을 검증했습니다.
- 실제 Chrome: 세 열, 카테고리 전환, 로그인 안내, 북마크 등록·해제 및 새로고침, 오류 시 상태 보존, 위조 userid 무시, CSRF/404/405, 음식 팝업과 부모 평균 갱신, 메인페이지의 표시 전용 하트, 키보드·모바일 터치를 검증했습니다.
- Java 8 API 호환성을 `javac --release 8` 전체 소스 컴파일로 확인했습니다. 실제 서버 JVM은 설치된 JDK 17입니다.
- 기존 POM의 `testFailureIgnore=true` 때문에 BUILD SUCCESS만 믿지 않고 Surefire XML의 실패·오류 수를 확인했습니다. 기존 회원을 대량 생성하는 `MemberTests`는 제외했습니다.

실행 명령 (`ex06_2` 기준):

```powershell
mvn '-Dtest=*Test,*Tests,!MemberTests,BookmarkOracleIT,DailyReviewOracleIT,MenuReviewOracleIT' '-DmenuReview.dbTest=true' '-DdailyReview.dbTest=true' '-Dbookmark.dbTest=true' test
Get-ChildItem src/test/js/*.test.js | ForEach-Object { node $_.FullName }
mvn '-Dtest=BookmarkBrowserIT,DailyReviewBrowserIT,MenuReviewHttpIT' '-DmenuReview.dbTest=true' '-Dbookmark.browserTest=true' '-DdailyReview.browserTest=true' '-DmenuReview.httpTest=true' '-DmenuReview.baseUrl=http://localhost:8080/controller' test
```

현재 로컬 Maven 경로는 `target/server-tools/apache-maven-3.9.9/bin/mvn.cmd`이며 `-s target/server-tools/settings.xml`로 기존 Oracle JDBC 캐시를 사용했습니다. 브라우저 테스트의 Playwright는 `target/server-tools/browser/node_modules/playwright`에만 설치되어 운영 의존성에는 포함되지 않습니다. Chrome 경로와 서버 URL은 `-Dbookmark.chrome=...`, `-Dbookmark.baseUrl=...`로 지정할 수 있습니다. 통합 테스트는 테스트 DB에서 opt-in으로 실행합니다.

검증 로그와 캡처는 `target/bookmark-check/` 아래의 `db-tests.log`, `browser-tests.log`, `browser-final.log`, `category-desktop.png`, `category-mobile.png`에 있습니다.

미검증: 운영 DB/운영 배포, 실제 JDK 8 JVM 실행, Safari/Firefox, 실제 모바일 기기. 모바일 검증은 Chrome 터치 에뮬레이션입니다.

## 신규 파일

경로는 모두 `ex06_2/` 기준입니다.

- `src/main/java/org/zerock/domain/BookmarkVO.java`
- `src/main/java/org/zerock/domain/BookmarkStatus.java`
- `src/main/java/org/zerock/mapper/BookmarkMapper.java`
- `src/main/resources/org/zerock/mapper/BookmarkMapper.xml`
- `src/main/java/org/zerock/service/BookmarkService.java`
- `src/main/java/org/zerock/service/BookmarkServiceImpl.java`
- `src/main/java/org/zerock/controller/BookmarkController.java`
- `src/test/java/org/zerock/service/BookmarkServiceTest.java`
- `src/test/java/org/zerock/service/BookmarkOracleIT.java`
- `src/test/java/org/zerock/service/BookmarkBrowserIT.java`
- `src/test/java/org/zerock/mapper/BookmarkMapperXmlTest.java`
- `src/test/java/org/zerock/controller/BookmarkControllerTest.java`
- `src/test/js/menu-bookmark.test.js`
- `src/test/browser/menu-bookmark.browser.js`
- `docs/bookmarks.md`

## 이번 작업에서 수정한 기존 파일

- `src/main/java/org/zerock/domain/MenuVO.java`: 사용자별 조회용 `bookmarked` 추가.
- `src/main/java/org/zerock/mapper/MenuMapper.java`: 카테고리 조회에 userid 바인딩 추가.
- `src/main/resources/org/zerock/mapper/MenuMapper.xml`: 상태·실제 개수를 한 번에 조회.
- `src/main/java/org/zerock/service/MenuServiceImpl.java`: 인증 사용자 ID 전달.
- `src/main/webapp/WEB-INF/spring/security-context.xml`: 북마크 POST 인증 제한.
- `src/main/webapp/WEB-INF/views/category.jsp`: 세 열, 하트·개수·접근성·모바일 스타일.
- `src/main/webapp/resources/js/menu-category.js`: 기존 팝업 코드를 유지하고 북마크 Ajax 추가.
- `src/test/java/org/zerock/controller/CategoryControllerTest.java`: 최신 main에서 마이페이지 담당 컨트롤러가 이동한 점 반영.
- `src/test/java/org/zerock/menu/StubMenuMapper.java`
- `src/test/java/org/zerock/mapper/MenuMapperXmlTest.java`
- `src/test/java/org/zerock/service/MenuServiceTest.java`
- `src/test/java/org/zerock/security/MenuReviewSecurityTests.java`
- `src/test/js/menu-popup.test.js`
- `docs/home.md`, `docs/food-category.md`, `docs/daily-review.md`: 현재 북마크 지원과 DB 검증 상태 안내.

메인 JSP, 일별 리뷰 구현, `DailyDietMapper.xml`, 음식 리뷰 서비스·매퍼는 북마크 작업으로 수정하지 않았습니다. 최신 main의 마이페이지 변경과 기존 로컬 일별 리뷰 작업을 함께 보존합니다.
