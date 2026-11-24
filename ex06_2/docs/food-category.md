# 음식 카테고리 구현 및 실행 안내

> 2026-11-24: 카테고리 표를 음식명 / 평균 별점 / 북마크 수의 세 열로 변경하고 북마크 하트 등록·해제를 구현했습니다. 현재 사용법·API·DB·검증 결과는 [북마크 안내](bookmarks.md)를 참고하세요. 아래는 최초 구현 기록입니다.

> 최신 상태: 메뉴 DB 설치와 실제 서버 반영을 완료했고 음식 클릭 리뷰 팝업이 추가되었습니다. 아래 최초 구현 기록의 DB 미설치/리뷰 미구현 표시는 당시 상태입니다. 현재 실행·SQL·테스트 방법은 [음식 리뷰 팝업 안내](../../docs/menu-review.md)를 참고하세요.

기존 Spring MVC / MyBatis / Oracle / JSP 구조를 유지했습니다. 신규 라이브러리는 없습니다.
운영 화면은 `MenuMapper.xml`을 통해 `tbl_menu`를 조회합니다. 테스트용 데이터 대역은 `src/test`에만 있습니다.

## 변경 파일

신규 운영 코드:

- `src/main/java/org/zerock/domain/MenuVO.java`: menuId, menuName, category, avgRating, bookmarkCnt, regDate. 기존 Lombok `@Data` 사용.
- `src/main/java/org/zerock/domain/MenuCategory.java`: 5개 코드, 한글 이름, URL type 변환 및 허용값 검증.
- `src/main/java/org/zerock/mapper/MenuMapper.java`: 전체/카테고리 목록, ID 단건, 카테고리 개수 조회.
- `src/main/resources/org/zerock/mapper/MenuMapper.xml`: 명시적 resultMap, 바인딩 파라미터, 음식명 및 ID 오름차순 정렬.
- `src/main/java/org/zerock/service/MenuService.java`: 다른 기능에서도 사용할 조회 인터페이스.
- `src/main/java/org/zerock/service/MenuServiceImpl.java`: 기존 setter 주입 방식, 카테고리와 양수 ID 검증 후 Mapper 호출.
- `src/main/java/org/zerock/controller/CategoryController.java`: GET `/category`, 기본 main, 모델 전달, 400/500 안전한 안내.

신규 SQL:

- `sql/menu/00_inspect_menu.sql`: 기존 객체·컬럼·기본값·제약조건·인덱스·시퀀스 확인. 읽기 전용.
- `sql/menu/01_create_menu.sql`: 신규 테이블, PK/검사 제약, 시퀀스, 카테고리 인덱스 생성.
- `sql/menu/02_seed_menu_optional.sql`: 선택 실행하는 11개 예시 음식. 같은 카테고리/이름은 다시 넣지 않음.
- `sql/menu/03_verify_menu.sql`: 카테고리별 개수, 목록, 유효하지 않은 행 수 확인. 읽기 전용.

신규 테스트와 문서:

- `src/test/java/org/zerock/controller/CategoryControllerTest.java`: HTTP/모델/오류/기존 홈 매핑 테스트 6개.
- `src/test/java/org/zerock/service/MenuServiceTest.java`: 검증·조회 계약 테스트 3개.
- `src/test/java/org/zerock/mapper/MenuMapperXmlTest.java`: 실제 MyBatis XML 로딩, resultMap, SQL 바인딩/정렬 테스트 3개.
- `src/test/java/org/zerock/menu/StubMenuMapper.java`: DB를 변경하지 않는 테스트 전용 대역.
- `docs/food-category.md`: 이 안내 문서.

수정한 기존 파일:

- `src/main/java/org/zerock/controller/HomeController.java`: 기존 `/category` 메서드만 제거하여 중복 매핑 방지.
- `src/main/webapp/WEB-INF/views/category.jsp`: 카테고리 버튼, 선택 강조, 실제 목록, 별점/북마크, 빈 목록/오류 안내. 음식명은 `c:out`으로 출력. 모바일에는 버튼 줄바꿈과 표 내부 가로 스크롤 적용.
- `src/main/webapp/WEB-INF/views/includes/header.jsp`: 내부 링크/CSS/JS에 컨텍스트 경로 반영. 기존 로그인·로그아웃 및 권한별 메뉴 유지.
- `src/main/webapp/WEB-INF/views/includes/footer.jsp`: 공통 JS 경로에 컨텍스트 경로 반영.
- `pom.xml`: Windows 기본 MS949에 의존하지 않도록 소스 인코딩 UTF-8 명시. Java/Spring/MyBatis/라이브러리 버전은 변경하지 않음.

DB 접속 파일, 보안 설정, 게시판·회원·관리자 구현은 수정하지 않았습니다.

## 기존 설정과의 연결

- `root-context.xml`과 샘플 모두 `org.zerock.mapper` 및 `org.zerock.service`를 자동 스캔합니다.
- Mapper 인터페이스와 동일한 클래스패스 위치의 XML을 MyBatis가 읽습니다. 별도 mapperLocations 추가가 필요하지 않습니다.
- `servlet-context.xml`은 `org.zerock.controller`를 스캔하고 `/WEB-INF/views/` + 뷰 이름 + `.jsp`로 연결합니다.
- 기존 Security URL 규칙에서 `/category`는 인증 제한 대상으로 지정되어 있지 않습니다. 기존 규칙은 그대로 두었습니다.
- STS 프로젝트의 기본 컨텍스트 경로는 `/controller`입니다. 카테고리와 공통 링크는 루트 배포에서도 동작하도록 컨텍스트 경로를 사용합니다.

## Oracle 적용 순서

프로그램이 사용하는 Oracle 스키마로 SQL Developer에 접속하고 UTF-8로 파일을 열어 Run Script(F5)로 실행합니다. SYS/SYSTEM에 설치하지 않습니다. 접속정보는 기존 로컬 설정을 사용하고 소스에 추가하지 않습니다.

1. `00_inspect_menu.sql` 실행.
2. 메뉴 관련 객체가 없는 신규 설치라면 `01_create_menu.sql` 실행.
3. 테스트 데이터가 필요하면 `02_seed_menu_optional.sql` 실행. 실서비스에 예시 데이터를 넣을 필요는 없습니다.
4. `03_verify_menu.sql` 실행. 신규 설치에 예시 데이터를 넣었다면 MAIN 3, SIDE 3, DESSERT 2, DRINK 2, ETC 1개입니다.

SQL*Plus에서도 `@sql/menu/00_inspect_menu.sql`과 같은 방법으로 실행할 수 있습니다. 한글 seed 파일을 실행할 때 클라이언트 문자셋을 UTF-8에 맞추십시오.

`01`은 동일 객체가 있으면 DDL 전에 오류로 중단합니다. 기존 테이블이나 시퀀스를 삭제·초기화하거나 구조가 같다고 추정하여 재사용하지 않습니다. 기존 객체가 있으면 `00`의 결과로 컬럼 타입·NOT NULL·기본값·PK·허용 범위·인덱스를 비교하고, 기존 menu_id와 시퀀스 발급 범위를 확인한 뒤 별도 마이그레이션을 준비하십시오. 호환되는 객체가 이미 있다면 `01`을 다시 실행할 필요가 없습니다. Oracle DDL은 암묵적으로 커밋되므로 중간 실패 시 먼저 객체 상태를 다시 확인해야 합니다.

현재 작업 중 실제 설정으로 **읽기 전용 DB 연결에 성공했고 메뉴 관련 객체는 0개**임을 확인했습니다. 스키마 생성 및 선택적 seed SQL은 실제 DB에 실행하지 않았습니다. 따라서 화면에서 정상 조회하려면 위 설치 단계가 먼저 필요합니다.

## 실행 방법

1. 위 SQL을 적용합니다.
2. STS에서 프로젝트를 Refresh하고 Maven > Update Project를 실행합니다. JDK 8 또는 15, 컴파일 타겟 1.8, UTF-8을 사용합니다.
3. 기존 `root-context.xml`에 설정된 로컬 접속정보를 유지하고 Tomcat 9에 프로젝트를 추가합니다.
4. Run on Server 후 `http://localhost:8080/controller/category`에 접속합니다. 서버 포트/컨텍스트 경로를 다르게 설정했다면 해당 값으로 바꿉니다. 루트 배포는 `/category`입니다.

등록 데이터가 없으면 정상 응답(200)으로 `등록된 음식이 없습니다.`를 표시합니다. 테이블 미설치/접속 장애는 500과 일반 오류 안내가 표시됩니다. SQL·스택 트레이스는 화면 모델에 전달하지 않습니다.

## 테스트 방법

DB를 변경하지 않는 신규 자동 테스트:

```text
mvn -Dtest=CategoryControllerTest,MenuServiceTest,MenuMapperXmlTest -Dmaven.test.failure.ignore=false test
```

기존 POM은 테스트 실패를 무시하도록 설정되어 있으므로 위 플래그를 사용하고 테스트 결과를 확인합니다. 기존 `MemberTests`에는 실제 회원 삽입 테스트가 있어 이번 검증에서는 실행하지 않았습니다.

브라우저 확인:

1. `/category`와 `?type=main`은 메인메뉴를 표시해야 합니다.
2. `?type=side`, `dessert`, `drink`, `etc`는 각 코드의 음식만 표시하고 해당 버튼이 강조되어야 합니다.
3. 초기 설치 후 seed 적용 전에 빈 목록 안내를 확인할 수 있습니다. 기존 데이터를 삭제하여 시험하지 않습니다.
4. `?type=unknown`, `?type=`은 HTTP 400과 안전한 안내가 표시되어야 합니다. 값 생략만 main으로 기본 처리합니다. 유효한 값은 앞뒤 공백과 대소문자를 정규화합니다.
5. 예시 돈까스는 별점 `4.50 / 5`, 북마크 `12`, 추가밥은 `0.00 / 5`, `0`이어야 합니다.
6. 개발용 DB 연결 장애 시 500과 일반 안내만 보여야 합니다. 자동 테스트는 실제 DB 설정 변경 없이 이 상황을 재현합니다.
7. 사이드바와 카테고리 버튼, 게시판, 로그인·로그아웃, 회원/관리자 권한 화면을 확인합니다.
8. PC와 모바일 너비에서 버튼 줄바꿈, 표 가로 스크롤, 한글/긴 메뉴명 표시를 확인합니다.

## 이번 작업의 검증 결과와 한계

- JDK 15 `javac --release 8 -encoding UTF-8`로 **전체 main Java 소스 컴파일 성공**. 기존 deprecated API 안내만 발생했습니다.
- 기존 로컬 의존성으로 신규 테스트 소스를 컴파일하고 JUnit 4.13.2로 **12개 테스트 통과**했습니다.
- MyBatis 3.4.6에서 인터페이스 등록으로 XML이 발견되고 6개 필드 매핑, 바인딩, 정렬이 정상임을 확인했습니다.
- HomeController와 CategoryController를 함께 등록한 MockMvc에서 중복 매핑 없이 요청 처리와 기존 홈 매핑을 확인했습니다.
- Tomcat 9.0.123 Jasper로 `category.jsp` 및 포함된 header/footer를 **실제 JSP 컴파일 성공(오류 0개)** 했습니다.
- 기존 설정을 변경하거나 인증정보를 출력하지 않고 Oracle 읽기 전용 연결/메타데이터 조회에 성공했습니다. TBL_MENU, SEQ_MENU, IDX_MENU_CATEGORY는 아직 없습니다.
- STS 내장 Maven 3.8.1의 오프라인 `test`는 **JaCoCo 0.8.7 플러그인 의존성 캐시 누락으로 실패**했습니다. Maven 전체 빌드/WAR 패키징 성공으로 보고하지 않습니다. 별도의 javac/JUnit/Jasper 검증 결과와 구분합니다.
- SQL DDL/seed 실행, 실제 Oracle 메뉴 조회 결과 매핑, Tomcat 전체 앱 기동/브라우저 화면, 실제 로그인·게시판 동작, JDK 8 JVM에서의 실행은 아직 검증하지 않았습니다. MockMvc는 JSP HTML을 렌더링하지 않으므로 화면·XSS 동작을 실브라우저 검증했다고 주장하지 않습니다.
- 검증 로그 및 생성 클래스는 `target/food-category-check/`에 있습니다. 기존 `target/classes`를 대체하지 않았습니다.
- 작업 폴더/상위 폴더에 Git 저장소 정보가 없어 main과의 동일성 확인 및 `feature/food-category` 브랜치 생성은 불가능했습니다. Git 초기화, 커밋, push, PR 생성은 하지 않았습니다.

## 리뷰·북마크·식단 연동 인터페이스

공통 식별자는 `MenuVO.menuId` (`Long`) / `tbl_menu.menu_id`입니다. 신규 메뉴는 `seq_menu.NEXTVAL`로 ID를 발급합니다.

서비스 계약:

```java
List<MenuVO> getList();
List<MenuVO> getListByCategory(String category); // 대문자 코드 5종만 허용
MenuVO get(Long menuId);                       // 양수 ID, 없으면 null
int getCountByCategory(String category);        // 없으면 0
```

잘못된 코드/ID는 `IllegalArgumentException`이며 DB에 전달하지 않습니다. 목록 조회 결과가 없으면 빈 List입니다. 화면의 menuCount는 실제 조회 목록의 크기로 계산하여 두 쿼리 사이 데이터 변경에 따른 불일치를 피합니다.

현재 avgRating/bookmarkCnt는 `tbl_menu` 컬럼 그대로 읽습니다. 리뷰/북마크 집계·갱신 로직은 이번 범위에 포함하지 않았으며 관련 테이블에 JOIN하지 않습니다. 향후 담당자는 동일 menuId를 참조하고 집계 결과를 갱신하거나 Mapper의 조회 방식을 확장할 수 있습니다. DTO 및 서비스 반환 계약을 유지하면 JSP를 바꿀 필요가 없습니다. 미구현 리뷰 URL/북마크 동작 버튼은 추가하지 않았습니다.
