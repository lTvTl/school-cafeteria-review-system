# 메인페이지 구현 및 확인

## 현재 동작

- 오늘·내일 식단을 카테고리별로 표시하고 식사 시간대를 제목 옆에 표시합니다.
- 실제 메뉴가 변경된 경우에만 변경 전 메뉴를 표시합니다.
- 음식 이름 클릭 시 해당 카테고리 페이지로 이동합니다.
- 로그인 사용자가 북마크한 실제 제공 메뉴에 하트를 표시합니다. 미확정 식단은 예정 메뉴 기준입니다.
- 홈 평가 API/리뷰 조회는 없으며, 식단 조회는 `DailyDietService` → `DailyDietMapper`로 처리합니다.
- 카테고리·메뉴 상세의 기존 리뷰 기능은 유지합니다.

## DB

기존 `tbl_menu`와 추가 식단·북마크 테이블만 조회합니다. [SQL 안내](../sql/README.md)의 순서를 따르세요. `00~08` SQL, 음식 카테고리 화면과 메뉴 조회 코드는 변경하지 않았습니다. DB에는 직접 실행하지 않았습니다.

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

북마크 등록·취소 UI는 제공하지 않습니다. 다른 계정에 직접 추가한 북마크가 있다면 해당 계정의 하트는 표시됩니다.

## 자동 검사

`ex06_2`에서 실행합니다. DB에 데이터를 넣는 `MemberTests`와 통합 테스트는 제외합니다.

```text
mvn -Dtest=HomeControllerTests,DailyDietMapperTests,MenuReviewMapperTests,MenuReviewSecurityTests,MenuReviewServiceTest,MenuControllerTest,MenuReviewMapperXmlTest,CategoryControllerTest,MenuServiceTest,MenuMapperXmlTest -Dmaven.test.failure.ignore=false test
node src/test/js/sql-schema.test.js
node src/test/js/menu-popup.test.js
```

SQL 검사는 생성 객체 중복·기존 테이블 변경 구문·사전 중단 조건을 확인하는 정적 검사입니다. 실제 Oracle 실행 및 브라우저 확인을 대신하지 않습니다.

확인 결과: Java 전체 소스 컴파일, JUnit 36개, SQL 정적 검사, 팝업 JavaScript 검사 통과. 기존 SQL 9개와 카테고리/상세 관련 파일 9개의 SHA-256이 작업 전과 일치합니다. Maven 명령이 없어 JDK 15와 캐시된 의존성으로 직접 컴파일/JUnit을 실행했습니다. 로그는 `ex06_2/target/home-sql-check`에 있습니다. 실제 Oracle SQL 실행과 브라우저 검증은 미실시입니다.
