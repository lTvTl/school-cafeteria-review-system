# 일별 전체 학식 리뷰와 개별 음식 리뷰

기준 커밋: `959d79828a1927dd05e033ec918a1c65b17ecd2c`. 기존 Spring MVC / MyBatis / Oracle / JSP 구조를 유지하며 운영 의존성은 추가하지 않았습니다. 원격 push와 PR 생성은 하지 않았습니다.

## 사용 방법

1. `/main`에서 오늘·내일 식단을 확인합니다. 로그인하지 않아도 오늘의 일별 리뷰, 평균, 개수, 별점별 분포를 볼 수 있습니다.
2. 로그인 후 **오늘의 학식 리뷰**에서 별점을 클릭하고 1~1,000자 내용을 입력해 등록합니다. 음식 선택은 없으며 조식·중식·석식을 합친 그 날짜 전체 학식이 평가 대상입니다.
3. 본인 리뷰가 있으면 작성 폼 대신 **내 리뷰**를 표시합니다. 당일에는 수정할 수 있고 본인 리뷰는 삭제할 수 있습니다. 목록의 다른 페이지를 보고 있어도 내 리뷰에 접근할 수 있습니다.
4. 삭제 후 재작성할 수 있으며 동일 날짜·동일 사용자 리뷰는 최대 1개입니다. 등록·수정·삭제는 POST 후 `/main#daily-reviews`로 리다이렉트하며, 새 GET에서 목록·평균·개수가 갱신됩니다. 식단 접힘 상태는 sessionStorage로 같은 탭에 보관합니다.
5. `/category`에서 음식명을 클릭하면 기존 520×760 팝업이 열립니다. 이 팝업의 CRUD·평균·분포·페이징은 해당 `menuId`에만 적용됩니다. 성공 시 기존 부모 창 갱신 메시지도 유지합니다.

## 데이터와 날짜

| 구분 | 기준 | 저장소 | 평균 |
|---|---|---|---|
| 메인페이지 전체 학식 | 서버가 정한 `serveDate` | `tbl_daily_review` | 해당 날짜의 일별 리뷰만 실시간 집계 |
| 카테고리 개별 음식 | `menuId` | 기존 `tbl_menu_review` | 기존 `MenuReviewServiceImpl`의 음식 평균 갱신 |

일별 리뷰에는 `menu_id`, `diet_id`, 식사 시간대가 없습니다. 메뉴 리뷰 서비스·매퍼와 기존 식단·북마크 SQL은 변경하지 않았습니다.

신규 컬럼은 `daily_review_id`, `serve_date`, `userid`, `star_score`, `review_text`, `regdate`, `updatedate`입니다. `seq_daily_review`를 사용하고, 회원 FK·날짜 정규화 CHECK·1~5점 CHECK·유효 내용 CHECK·`(serve_date, userid)` UNIQUE와 날짜별 최신순 인덱스를 둡니다. 한글 1,000자를 저장할 수 있도록 `VARCHAR2(1000 CHAR)`를 사용합니다.

인증된 사용자 ID는 Spring Security에서 가져오고 요청의 `userid`, `serveDate`, `menuId`는 받지 않습니다. 서비스의 회원 행 잠금은 같은 사용자의 등록·수정·삭제를 직렬화합니다. 중복 사전 검사와 DB UNIQUE 제약을 함께 적용하며, DB 중복 예외는 사용자에게 재작성 불가 안내로 전달합니다. 일별 리뷰는 물리 삭제하므로 삭제 후 재작성 가능하고, 기존 개별 음식 리뷰의 삭제 정책은 그대로입니다.

날짜는 Oracle의 `TRUNC(CAST(SYSTIMESTAMP AT TIME ZONE 'Asia/Seoul' AS DATE))`로 결정합니다. 조회 요청은 날짜를 한 번만 읽어 목록·평균·내 리뷰에 같은 날짜를 적용합니다. 등록 SQL은 서버가 읽은 날짜와 실행 시점 날짜가 같은지 다시 확인하고, 수정 SQL도 현재 날짜·소유자를 다시 확인합니다. 자정 경계를 지난 요청은 재시도 안내 또는 수정 거부로 처리합니다.

확인한 로컬 Oracle 시각은 2026-11-23 20:35 KST로 한국 시간과 일치했습니다. `DBTIMEZONE=+00:00`, `SESSIONTIMEZONE=Asia/Seoul`이었으며 `SYSDATE`는 한국 시각이었습니다. 운영 DB 시각은 별도로 확인해야 합니다. 기존 식단·음식 리뷰는 `TRUNC(SYSDATE)`를 사용하므로 DB 호스트의 한국 시간 운영이 필요합니다.

## 별점 컴포넌트

`starRating.tag`가 접근 가능한 네이티브 라디오와 1~5점 라벨을 렌더링합니다. 각 폼은 독립적인 라디오 그룹이고, 태그의 고유 ID로 라벨을 연결합니다. 수정 폼에는 기존 점수를 전달합니다.

`star-rating.js`는 각 picker의 클로저에 `hoverScore`와 `selectedScore`를 따로 저장합니다. 포인터 진입 시 표시만 미리보기로 바꾸며 `checked`는 수정하지 않습니다. 포인터 이탈·취소 시 확정 점수를 복원합니다. 네이티브 클릭·터치·키보드 방향키의 `change`에서만 확정 점수를 다시 읽습니다. 폼 reset도 초기값으로 복원합니다.

빈 별은 회색 `☆`, 채워진 별은 오렌지 `★`입니다. 크기는 36px, 간격은 6px이며 박스 배경은 없습니다. 포커스 테두리와 스크린리더 라벨을 유지합니다. 별점 미선택·빈 내용은 브라우저와 서버 양쪽에서 거부합니다. 서버는 Unicode 공백과 코드포인트 길이도 검사합니다. JSP 사용자 텍스트는 `c:out`으로 이스케이프합니다.

## Oracle 설치

신규 파일: [12_create_daily_review.sql](../sql/menu/12_create_daily_review.sql).

- 기존 설치: 회원 테이블이 있는 애플리케이션 스키마에서 12번만 추가 실행합니다.
- 최초 설치: 회원/권한 테이블 → 01 메뉴 → 05 메뉴 리뷰 → 09 식단 → 10 북마크 → 12 일별 리뷰. 11번은 선택적 식단 테스트 데이터입니다.
- 12번은 기존 객체가 있으면 중단합니다. 이미 적용한 DB에서는 다시 실행하지 않습니다.
- 이번 작업에서는 로컬 `localhost:1521/XEPDB1`에 12번을 적용했습니다. 기존 테이블 변경·삭제와 기존 데이터 삭제는 하지 않았습니다.

## 검증 결과

2026-11-23 실행 결과:

- Maven 컴파일 및 단위·보안 테스트 54개 통과.
- 실제 Oracle 통합 테스트 16개 통과: 일별 8개, 기존 메뉴 8개.
- 실제 서버 HTTP 테스트 3개 및 Chrome 브라우저 통합 테스트 1개 통과. Java 테스트 합계 74개, 실패·오류 0개.
- JS 테스트 5개 파일 통과: 관리자 현황, 식단 접힘, 기존 메뉴 팝업, SQL 구조, 공통 별점.
- `javac --release 8` 전체 운영 소스 컴파일 통과. Maven/Tomcat 실행 JVM은 설치된 JDK 17이며 실제 JDK 8 런타임은 미검증입니다.
- 실제 Chrome에서 메인·팝업의 빈 별, 3점 hover, 5점 확정 후 2점 hover/이탈/클릭, 방향키, 수정 초기값, 독립 라디오 그룹, 모바일 터치를 확인했습니다.
- 실제 POST/Redirect/GET, XSS 이스케이프, 중복 등록 거부, 일별·메뉴 평균 상호 독립, 부모 카테고리 갱신, 삭제 후 재작성, 식단 접힘 유지 확인.
- 테스트는 임의 UUID 회원·메뉴·리뷰를 만들어 사용하고 종료 후 해당 테스트 데이터만 정리했습니다.

테스트 실행 예시 (`ex06_2` 기준):

```powershell
mvn '-Dtest=*Test,*Tests,!MemberTests' test
Get-ChildItem src/test/js/*.test.js | ForEach-Object { node $_.FullName }
mvn '-Dtest=DailyReviewOracleIT,MenuReviewOracleIT' '-DmenuReview.dbTest=true' '-DdailyReview.dbTest=true' test
mvn '-Dtest=MenuReviewHttpIT' '-DmenuReview.dbTest=true' '-DmenuReview.httpTest=true' '-DmenuReview.baseUrl=http://localhost:8080/controller' test
```

브라우저 테스트는 별도 테스트 도구 설치 후 실행합니다. Playwright는 target 폴더에만 설치하며 운영 POM에 추가하지 않습니다.

```powershell
npm install --prefix target/server-tools/browser --no-save --package-lock=false playwright
mvn '-Dtest=DailyReviewBrowserIT' '-DmenuReview.dbTest=true' '-DdailyReview.browserTest=true' test
```

브라우저 경로는 `-DdailyReview.chrome=...`, Playwright 모듈 경로는 `-DdailyReview.playwrightModule=...`, 서버 URL은 `-DdailyReview.baseUrl=...`로 바꿀 수 있습니다. 서버가 먼저 실행되어 있어야 합니다. 테스트용 DB에서만 opt-in 통합 테스트를 실행하세요. `MemberTests`는 기존 회원 100개를 삽입하므로 이번 검증에서 제외했습니다. 기존 POM의 `testFailureIgnore=true` 때문에 BUILD SUCCESS만으로 판정하지 않고 `target/surefire-reports/TEST-*.xml`의 실패·오류를 확인했습니다.

현재 환경의 Maven은 `target/server-tools/apache-maven-3.9.9/bin/mvn.cmd`입니다. 구형 Oracle 저장소 대신 기존 JDBC 캐시를 이용하는 로컬 설정 `-s target/server-tools/settings.xml`을 사용했습니다. 소스 POM과 사용자 전역 Maven 설정은 변경하지 않았습니다.

로그: `target/server-tools/review-tests.log`, `browser-tests.log`. 화면 캡처: `daily-review-desktop.png`, `daily-review-mobile.png`. 모두 같은 `target/server-tools` 아래에 있습니다.

## 남은 환경 확인

2026-11-24 갱신: 아래의 식단·북마크 테이블 미설치 항목은 [북마크 구현](bookmarks.md) 과정에서 기존 09·10번 SQL을 적용하여 해결했습니다. 테스트 식단으로 메인 하트 연동까지 검증했으며 일별 리뷰 코드는 유지했습니다.

- 현재 로컬 DB에는 기존 `tbl_daily_diet`, `tbl_bookmark`가 없습니다. 따라서 식단 실데이터·북마크 표시의 브라우저 검증은 하지 못했고, 기존 식단 조회 경고를 유지합니다. 관련 서비스·SQL은 변경하지 않았으며 기존 단위 테스트는 통과했습니다.
- 운영 DB, 실제 자정 시각 전환, 실제 JDK 8 JVM, Safari/Firefox 및 실제 모바일 기기는 미검증입니다. 자정 경계는 과거 서버 날짜 입력과 SQL 날짜 조건으로 자동 검증했고 모바일은 Chrome 터치 에뮬레이션을 사용했습니다.
- 회원가입 메일 발송, 게시판 CRUD, 관리자·마이페이지 전체 업무 흐름은 이번 범위의 종단간 테스트 대상이 아닙니다. 기존 테스트와 로그인·주요 페이지 접근은 확인했습니다.

## 생성 파일

경로는 모두 `ex06_2/` 기준입니다.

- `src/main/java/org/zerock/controller/DailyReviewController.java`
- `src/main/java/org/zerock/domain/DailyReviewVO.java`
- `src/main/java/org/zerock/domain/DailyReviewSummary.java`
- `src/main/java/org/zerock/domain/DailyReviewPage.java`
- `src/main/java/org/zerock/mapper/DailyReviewMapper.java`
- `src/main/java/org/zerock/service/DailyReviewService.java`
- `src/main/java/org/zerock/service/DailyReviewServiceImpl.java`
- `src/main/resources/org/zerock/mapper/DailyReviewMapper.xml`
- `src/main/webapp/WEB-INF/tags/starRating.tag`
- `src/main/webapp/WEB-INF/views/includes/daily-reviews.jsp`
- `src/main/webapp/resources/css/star-rating.css`
- `src/main/webapp/resources/js/star-rating.js`
- `src/main/webapp/resources/js/daily-review.js`
- `sql/menu/12_create_daily_review.sql`
- `src/test/java/org/zerock/controller/DailyReviewControllerTest.java`
- `src/test/java/org/zerock/mapper/DailyReviewMapperXmlTest.java`
- `src/test/java/org/zerock/service/DailyReviewServiceTest.java`
- `src/test/java/org/zerock/service/DailyReviewOracleIT.java`
- `src/test/java/org/zerock/service/DailyReviewBrowserIT.java`
- `src/test/js/star-rating.test.js`
- `src/test/browser/daily-review.browser.js`
- `docs/daily-review.md`

## 수정 파일

- `src/main/java/org/zerock/controller/HomeController.java`
- `src/main/webapp/WEB-INF/spring/security-context.xml`
- `src/main/webapp/WEB-INF/views/main.jsp`
- `src/main/webapp/WEB-INF/views/menu/popup.jsp`
- `src/main/webapp/resources/css/menu-popup.css`
- `src/main/webapp/resources/dist/css/main.css`
- `src/main/webapp/resources/js/home-menu.js`
- `src/main/webapp/resources/js/menu-popup.js`
- `src/test/java/org/zerock/controller/CategoryControllerTest.java`
- `src/test/java/org/zerock/controller/HomeControllerTests.java`
- `src/test/java/org/zerock/security/MenuReviewSecurityTests.java`
- `src/test/js/home-menu.test.js`
- `sql/README.md`
- `docs/home.md`
