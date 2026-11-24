# 메인페이지 구현 및 확인

## 현재 동작

- 오늘·내일 식단을 카테고리별로 표시하고 식사 시간대를 제목 옆에 표시합니다.
- 실제 메뉴가 변경된 경우에만 변경 전 메뉴를 표시합니다.
- 음식 이름 클릭 시 해당 카테고리 페이지로 이동합니다.
- 로그인 사용자가 북마크한 실제 제공 메뉴에 하트를 표시합니다. 미확정 식단은 예정 메뉴 기준입니다.
- 식단 조회는 `DailyDietService` → `DailyDietMapper`로 처리합니다. 접기·펼치기 상태는 같은 탭에서 리뷰 제출·페이지 이동 후에도 유지합니다.
- 오늘 날짜 전체 학식 리뷰는 별도의 `DailyReviewService` → `DailyReviewMapper` → `tbl_daily_review`로 처리합니다. 음식 선택이나 식사 시간대별 평가는 없습니다.
- 평균·개수·별점별 분포와 최신순 10개 페이징을 제공하며, 본인 리뷰는 목록 페이지와 무관하게 수정 영역에 표시합니다.
- 카테고리·메뉴 상세의 기존 리뷰 기능은 유지합니다.

## DB

식단 조회의 기존 테이블·북마크 SQL은 유지합니다. 일별 리뷰에는 신규 `tbl_daily_review`만 사용하며 개별 메뉴 평균을 수정하지 않습니다. [SQL 안내](../sql/README.md)의 순서를 따르세요. 이번 작업에서는 로컬 DB에 12번 일별 리뷰 SQL을 적용했습니다. 상세 구현·테스트 결과와 미검증 항목은 [일별 리뷰 안내](daily-review.md)에 정리했습니다.

## 화면 확인

테스트 DB에 [11_home_test_data.sql](../sql/menu/11_home_test_data.sql) 적용 후 최신 코드를 Clean 빌드하여 Tomcat에 배포합니다.

| 확인 | 기대 결과 |
|---|---|
| `/main`의 오늘·내일 | `[홈테스트]` 식단 각각 5개, 메인/반찬/디저트/음료/기타와 중식 표시 |
| 오늘 돈까스 | 변경 전 제육볶음 표시 |
| 오늘 김치/내일 예정 식단 | 추가 상태 문구 없음 |
| user0 로그인 | 오늘 돈까스와 내일 제육볶음에 하트 |
| 로그아웃/다른 계정 | user0의 북마크로 하트가 표시되지 않음 |
| 각 음식 이름 클릭 | 해당 카테고리 페이지로 이동 |
| 카테고리 메뉴 클릭 | 기존 상세 팝업과 리뷰 기능 유지 |
| 모바일 로그인 아이콘 | 메뉴가 화면 왼쪽 밖으로 벗어나지 않음 |

북마크 등록·취소는 음식 카테고리 페이지에서 제공합니다. 메인페이지에는 조작 버튼 없이 로그인 사용자의 북마크 상태만 하트로 표시합니다. [북마크 안내](bookmarks.md)를 참고하세요.

## 자동 검사

`ex06_2`에서 실행합니다. DB에 데이터를 넣는 `MemberTests`와 통합 테스트는 제외합니다.

```text
mvn -Dtest=HomeControllerTests,DailyDietMapperTests,MenuReviewMapperTests,MenuReviewSecurityTests,MenuReviewServiceTest,MenuControllerTest,MenuReviewMapperXmlTest,CategoryControllerTest,MenuServiceTest,MenuMapperXmlTest -Dmaven.test.failure.ignore=false test
node src/test/js/sql-schema.test.js
node src/test/js/menu-popup.test.js
```

SQL 검사는 생성 객체 중복·기존 테이블 변경 구문·사전 중단 조건을 확인하는 정적 검사입니다. 실제 Oracle 실행 및 브라우저 확인을 대신하지 않습니다.

최신 확인 결과(2026-11-23): Maven 컴파일 및 Java 테스트 74개, JS 테스트 5개 파일, 로컬 Oracle 통합 테스트, 실제 Chrome 데스크톱·모바일 테스트 통과. Java 8 API 호환성은 `javac --release 8`로 전체 운영 소스를 별도 검증했습니다. 로컬 DB에는 기존 식단·북마크 테이블이 없어 식단 실데이터 조회는 미검증이며, 해당 경고는 리뷰 기능과 별개로 표시됩니다.

2026-11-24 추가 확인: 기존 09·10번 SQL로 누락된 식단·북마크 테이블을 설치했습니다. 테스트 식단으로 카테고리 북마크와 메인 하트 표시 연동을 검증했고, 테스트 데이터는 정리했습니다. 최신 검증 결과는 Java 테스트 94개와 JS 테스트 6개 파일 통과입니다. 실제 식단은 아직 입력하지 않은 상태입니다.
