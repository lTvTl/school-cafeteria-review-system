# 음식 리뷰 팝업 구현 및 운영 안내

기존 Spring MVC 5.2.7 / MyBatis 3.4.6 / Oracle / JSP / Spring Security 구조를 사용합니다.
Java 8 타겟이며 외부 애플리케이션 라이브러리는 추가하지 않았습니다. 원격 push/PR은 하지 않았습니다.

## 현재 로컬 적용 상태

- 실제 애플리케이션 Oracle 스키마에 `tbl_menu_review`, `seq_menu_review`, 외래키, 별점·내용 검사 제약과 인덱스를 설치했습니다.
- 기존 배추김치·야채볶음밥·쫄면·된장국·닭강정을 보존하고, 최종 요청의 예시인 돈까스를 중복 없이 추가했습니다. 돈까스 ID는 현재 `30`이며 화면에서는 ID를 하드코딩하지 않습니다.
- 실행 중인 STS Tomcat에 최신 클래스, Mapper XML, JSP, CSS/JS, 보안 규칙을 반영했습니다. 현재 서버의 contextPath는 빈 문자열입니다.
- 현재 주소: `http://localhost:8080/category?type=main`, 돈까스 상세: `http://localhost:8080/menu/30`.
- 테스트용 계정·메뉴·리뷰는 정리했습니다. 실제 리뷰를 임의로 생성하지 않았으므로 초기에는 리뷰 0개/평균 0.00입니다.
- 배포 전 파일은 `target/menu-review-deployment-backup-*`, 로그인 복귀 추가 반영 전 파일은 `target/menu-review-login-backup-*`에 보관했습니다.

## 신규 파일

운영 Java 소스 (`src/main/java/org/zerock/`):

- `domain/MenuReviewVO.java`: 리뷰, 작성자 표시 이름, DB 날짜 기준 수정 가능 여부.
- `domain/MenuReviewSummary.java`: 실제 리뷰 개수·평균·1~5점별 개수.
- `domain/MenuReviewPage.java`: 메뉴·요약·10개 단위 목록·페이지 상태.
- `mapper/MenuReviewMapper.java`: 조회, 행 잠금, 중복 검사, CRUD, 평균 갱신.
- `service/MenuReviewService.java`, `service/MenuReviewServiceImpl.java`: 인증·입력·소유권·당일 제한 검증과 트랜잭션.
- `controller/MenuController.java`: 상세 JSP, 요약 JSON, 로그인 후 상세 복귀.
- `controller/MenuReviewController.java`: POST 변경과 성공/오류 flash 및 PRG.

운영 리소스:

- `src/main/resources/org/zerock/mapper/MenuReviewMapper.xml`.
- `src/main/webapp/WEB-INF/views/menu/popup.jsp`.
- `src/main/webapp/resources/css/menu-popup.css`.
- `src/main/webapp/resources/js/menu-popup.js`.
- `src/main/webapp/resources/js/menu-category.js`.

SQL:

- `sql/menu/05_create_menu_review.sql`: 신규 리뷰 스키마 설치.
- `sql/menu/06_verify_menu_review.sql`: 읽기 전용 제약·인덱스·집계 확인.
- `sql/menu/07_menu_review_test_data.sql`: 별도 임시 예시 데이터, 마지막에 ROLLBACK.
- `sql/menu/08_add_pork_cutlet.sql`: 돈까스 메뉴가 없을 때만 추가. 임의 리뷰나 평점을 넣지 않습니다.

테스트:

- `src/test/java/org/zerock/service/MenuReviewServiceTest.java`.
- `src/test/java/org/zerock/controller/MenuControllerTest.java`.
- `src/test/java/org/zerock/mapper/MenuReviewMapperXmlTest.java`.
- `src/test/java/org/zerock/service/MenuReviewOracleIT.java`.
- `src/test/java/org/zerock/service/MenuReviewHttpIT.java`.
- `src/test/js/menu-popup.test.js`.

## 기존 수정 파일

- `category.jsp`: 기존 카테고리 목록을 유지하면서 음식 이름을 ID 기반 상세 링크로 변경. 평균 별점을 개별 갱신할 DOM 요소 추가.
- `security-context.xml`: 리뷰 POST와 로그인 복귀용 GET에 인증 규칙 추가. CSRF와 기존 회원/게시판/관리자 권한 유지.
- `customLogin.jsp`: 로그인 form·링크·정적 파일에 contextPath 반영, 로그인 버튼을 실제 submit 버튼으로 변경.
- `docs/food-category.md`: 현재 리뷰 연동 안내 링크 추가.

기존 `HomeController`, `CategoryController`, `MenuMapper`, `MenuService`, 헤더, 회원·게시판·댓글 코드는 이번 리뷰 구현에서 변경하지 않았습니다. 기존 `/category` 단일 매핑을 재사용합니다.

## 팝업 생성과 화면 동작

`category.jsp`의 `.menu-popup-link`를 `resources/js/menu-category.js`에서 처리합니다.
사용자 click 핸들러 안에서 `window.open(link.href, 'menu_review_' + menuId, ...)`을 동기 호출합니다.
옵션은 520×760, 현재 창 중앙 기준 위치, `scrollbars=yes,resizable=yes`입니다.
브라우저의 창 크기·팝업 정책에 따라 실제 크기는 조정될 수 있습니다.

- 정상 생성 시에만 원래 링크 이동을 막습니다. 차단되어 null이 반환되면 기본 링크가 현재 탭에서 상세를 엽니다.
- 같은 메뉴는 열린 Window 참조를 재사용하고 focus합니다. 다른 메뉴는 서로 다른 이름의 창을 사용합니다.
- `popup.jsp`는 독립 HTML이며 전체 헤더/사이드바를 포함하지 않습니다.
- 별점은 접근 가능한 radio + 별 아이콘이며 등록/수정 모두 키보드로 선택할 수 있습니다.
- 리뷰 내용·작성자·음식명은 `c:out`으로 출력하고 수정 textarea에서도 escape합니다.
- 본인 리뷰만 수정/삭제 UI를 표시하며 지난 날짜에는 삭제만 표시합니다.
- 닫기 버튼은 opener가 있는 창을 닫습니다. 직접 연 상세 페이지에서는 카테고리로 돌아갑니다.

## URL과 처리 흐름

- `GET /menu/{menuId}`: 음식 존재 확인 → 실제 요약·최신순 리뷰 조회 → `menu/popup.jsp`.
- `GET /menu/{menuId}/summary`: 실제 리뷰 기반 평균·개수·별점별 개수 JSON, `Cache-Control: no-store`.
- `GET /menu/{menuId}/write`: 인증이 필요합니다. 비로그인 시 Spring Security가 GET 목적지를 저장하고 로그인 화면으로 안내합니다. 로그인 후 해당 음식 상세로 복귀합니다.
- `POST /menu/{menuId}/reviews`: 등록.
- `POST /menu/{menuId}/reviews/{reviewId}/modify`: 당일 수정.
- `POST /menu/{menuId}/reviews/{reviewId}/remove`: 날짜와 무관한 본인 리뷰 삭제.

리뷰 POST는 Security 필터의 인증/CSRF 검사와 Controller 메서드 보안을 통과해야 합니다.
Service는 `SecurityContextHolder`의 인증된 ID를 사용하며 요청의 `userid`를 받지 않습니다.
별점 1~5와 공백이 아닌 1~1,000자 내용을 검사합니다. 한글 저장을 위해 DB는 `VARCHAR2(1000 CHAR)`입니다.
수정/삭제는 리뷰의 `menuId`와 로그인 ID를 모두 검사하고 SQL WHERE 절에서도 메뉴·소유자를 재검사합니다.

DB 저장과 commit이 성공한 후에만 success flash를 넣어 상세 URL로 redirect합니다.
GET에서 목록·평균·개수를 다시 조회하고 성공 flash가 있을 때만 부모 창에 알립니다.
입력 오류/중복 작성은 안전한 메시지를 flash로 표시하며 성공 알림을 전송하지 않습니다.
없는 음식/다른 메뉴의 리뷰는 404, 권한·수정 날짜 위반은 403입니다. 숫자가 아닌 경로 ID는 MVC 400입니다.

## 트랜잭션과 동시성

모든 리뷰 변경은 `@Transactional` 안에서 다음 순서로 실행합니다.

1. 인증 및 입력값 검증.
2. `tbl_menu`의 해당 행을 `SELECT ... FOR UPDATE`로 잠금.
3. 당일 중복 또는 리뷰 소유권·당일 수정 여부 확인.
4. INSERT / UPDATE / DELETE 실행.
5. `NVL(ROUND(AVG(star_score), 2), 0)`으로 `tbl_menu.avg_rating` 갱신.
6. 같은 트랜잭션 commit. 어느 단계든 실패하면 리뷰와 평균 변경이 함께 rollback.

동일 음식에 대한 변경은 메뉴 행 잠금으로 직렬화됩니다.
`ux_menu_review_day(menu_id, userid, TRUNC(regdate))` 고유 인덱스가 별도 연결/동시 요청에서도 중복 행을 막습니다.
하루의 기준은 Oracle 서버의 `SYSDATE`이며 JVM 날짜를 혼용하지 않습니다.
삭제는 `is_deleted=1`로 기록하면서 리뷰 내용을 `[deleted]`로 지우고 별점도 보관용 고정값 1로 바꿉니다. 삭제된 행은 모든 목록·상세·평균·분포·리뷰 수에서 제외합니다. 최소한의 `(menu_id, userid, 작성일)` 기록은 유지하여 삭제 후 재작성으로 하루 제한을 우회하지 못하게 합니다. 다음 날짜에는 새 리뷰를 작성할 수 있습니다.
목록은 `regdate DESC, review_id DESC`로 정렬하고 `ROW_NUMBER()`로 10개씩 조회합니다.
페이지 번호는 유효 범위로 보정하며 큰 입력에 정수 overflow가 생기지 않도록 long으로 범위를 계산합니다.

## 부모 카테고리 갱신

성공한 PRG 응답에서만 `menu-popup.js`가 `{type: 'menu-review-saved', menuId: '...'}`를 동일 출처로 전송합니다.
부모는 origin, 메시지 구조, ID 형식, 실제 저장해 둔 popup의 `event.source`를 검사합니다.
메시지에 담긴 별점을 믿지 않고 해당 음식의 `/summary`를 다시 읽어 그 행의 `.menu-average`만 갱신합니다.
동시에 여러 요약 요청이 돌아오면 이전 응답이 최신 값을 덮어쓰지 못하게 요청 순서를 검사합니다.

## Oracle SQL 실행

현재 로컬 DB에는 이미 설치했습니다. `05`는 기존 객체를 덮어쓰지 않으므로 재실행하면 중단됩니다.
다른 환경에서는 애플리케이션 설정과 같은 Oracle 스키마에 접속하고 UTF-8로 SQL Developer의 Run Script(F5) 또는 SQL*Plus를 사용합니다.

```sql
@sql/menu/05_create_menu_review.sql
@sql/menu/06_verify_menu_review.sql
-- 필요한 경우에만:
@sql/menu/08_add_pork_cutlet.sql
-- 테스트 전용 세션에서만, 마지막 ROLLBACK 유지:
@sql/menu/07_menu_review_test_data.sql
```

`tbl_menu`, `seq_menu`, `tbl_member(userid PK/UK)`가 먼저 존재해야 합니다.
기존 `tbl_daily_diet`는 로컬 DB에 없어서 `diet_id`만 nullable로 두었습니다. 식단표 키가 확정되면 별도 마이그레이션으로 FK를 추가합니다.
Oracle DDL은 암묵적으로 commit하므로 설치 중 실패했다면 현재 객체를 확인하고 부분 설치를 검토합니다. DROP/초기화 스크립트는 제공하지 않습니다.
DB 접속 값은 기존 로컬 `root-context.xml`에서 읽고 신규 파일에 계정·비밀번호를 복사하지 않습니다.

## 테스트 실행 및 결과

표준 Maven 선택 실행:

```text
mvn -Dtest=MenuReviewServiceTest,MenuControllerTest,MenuReviewMapperXmlTest,CategoryControllerTest,MenuServiceTest,MenuMapperXmlTest -Dmaven.test.failure.ignore=false test
mvn -Dtest=MenuReviewOracleIT -DmenuReview.dbTest=true -Dmaven.test.failure.ignore=false test
mvn -Dtest=MenuReviewHttpIT -DmenuReview.dbTest=true -DmenuReview.httpTest=true -DmenuReview.baseUrl=http://localhost:8080 -Dmaven.test.failure.ignore=false test
node src/test/js/menu-popup.test.js
```

Oracle/HTTP 통합 테스트는 명시적인 시스템 속성을 켠 경우에만 실행합니다.
실제 DB에 임의 ID의 테스트 전용 계정·메뉴를 생성하고 테스트 후 해당 행만 삭제합니다.
HTTP 테스트는 서버가 최신 코드로 실행 중이어야 하며 임시 BCrypt 계정으로 실제 로그인합니다. 테스트 비밀번호는 런타임에서 생성합니다.
기존 회원 추가 테스트를 함께 실행하지 않도록 `-Dtest` 목록을 지정합니다.

검증 결과:

- JDK 15 `javac --release 8 -encoding UTF-8` 전체 main 소스 컴파일 성공.
- Maven `org.apache.maven.plugins:maven-compiler-plugin:2.5.1:compile` 직접 실행은 **BUILD SUCCESS**였습니다. 이미 javac로 생성한 클래스가 최신이라 추가 컴파일 없이 종료했으며, 새로 컴파일한 검증 결과는 위 javac 결과입니다.
- JUnit 단위/MVC/MyBatis XML 및 기존 카테고리 테스트: **29개 통과**.
- 실제 Oracle CRUD·당일/소유권·삭제 후 당일 재등록 차단·1,000자 한글·페이징·고유 인덱스·롤백·동시성: **8개 통과**.
- 실제 Tomcat HTTP 로그인·CSRF·PRG·XSS 출력·권한·평균·기존 페이지: **3개 통과**.
- JavaScript 자동 테스트: 520×760 옵션, contextPath, 같은 창 재사용, 서로 다른 창, 차단 시 fallback, origin/source/형식 검증, 늦은 응답 방지, 성공 시 알림, 닫기/직접 접근 대체 동작 통과.
- 실제 Tomcat이 popup/category/login JSP를 렌더링하는 HTTP 응답으로 JSP 컴파일을 확인했습니다.
- Maven 3.8.1 오프라인 `test`는 **기존 JaCoCo 0.8.7 플러그인 의존성 캐시 누락** 때문에 실패했습니다. Maven 전체 빌드 성공으로 보고하지 않습니다. 설치된 프로젝트 의존성을 이용해 javac/JUnit으로 위 검증을 별도로 실행했습니다.
- 로그: `target/menu-review-check/unit-tests.log`, `oracle-tests.log`, `http-tests.log`, `final-mvc-mapper-tests.log`, `maven.log`, `maven-compile.log`.

## 실제 브라우저에서 확인할 항목

브라우저 제어 도구가 Windows sandbox 초기화 오류로 실행되지 않았습니다. 다음은 JavaScript 자동 검증과 HTTP 검증을 통과했지만 실제 GUI에서 관찰하지 못했습니다.

1. 카테고리에서 돈까스를 클릭하면 새 작은 창이 열리고 원래 카테고리가 유지되는지 확인.
2. 같은 음식을 다시 클릭했을 때 기존 창 focus, 다른 음식은 별도 창인지 확인.
3. 520px 폭에서 별 클릭·키보드 선택·스크롤·레이아웃이 자연스러운지 확인.
4. 로그인 후 음식 상세 복귀, 등록/수정/삭제 후 부모 별점 표시가 바뀌는지 확인.
5. 팝업 차단 설정에서 현재 탭 상세 이동, 닫기 버튼 동작 확인.

현재 요청 범위의 구현 누락은 없습니다. 실제 GUI 확인과 기존 Maven 의존성 캐시 보완은 위 검증 한계로 남습니다.
STS에서 작업을 이어갈 때 프로젝트 Refresh(F5) 후 서버 Publish를 사용합니다. 배포 파일을 직접 고치지 말고 이 프로젝트 소스/빌드 결과를 배포해야 합니다.
