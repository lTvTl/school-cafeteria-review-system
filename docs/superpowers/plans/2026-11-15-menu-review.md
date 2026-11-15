# Menu Review Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 메뉴별 별점·리뷰 등록, 공개 목록, 본인 수정·삭제를 메인페이지에 연결한다.
**Architecture:** 기존 JSP POST/redirect/GET, Spring Security, Service/Mapper 구조를 따른다. 날짜와 소유권은 SQL 조건으로 검증한다.
**Tech Stack:** Java 8, Spring MVC 5, MyBatis, Oracle, JUnit 4.
**Spec:** `docs/menu-review-design.md`

## Global Constraints

DB SQL은 사용자가 직접 실행한다. DB 쓰기 테스트와 기존 MemberTests는 실행하지 않는다. 별점 1~5 정수, 리뷰 1~1,000자, 당일 수정, 본인만 변경, 식단별 1건. 기존 미커밋 작업을 유지하고 현재 작업 폴더에서 직접 구현한다.

## Review Focus

위조 userid, 다른 사용자의 reviewId, 자정 경과, 변경/미확정 식단, 중복 등록 및 동시 평균 갱신을 확인한다. CSRF와 HTML 출력 이스케이프를 유지한다.

## Tasks

- [x] 서비스 테스트부터 작성: 잘못된 별점·빈 내용 거절, 세션 작성자 사용, 수정 실패·삭제 실패 처리, 평균 갱신 순서. 실행해 구현 전 실패 확인.
- [x] `domain/DailyDietVO.java`, `domain/MenuReviewVO.java`, `mapper/MenuReviewMapper.java`, 대응 XML, `service/MenuReviewService.java`와 Impl 추가. 식단 조회, 목록 페이지 조회, 등록/수정/삭제 및 메뉴 행 잠금 후 평균 갱신 제공.
- [x] `controller/MenuReviewController.java`, HomeController, security-context.xml 연결. POST 쓰기와 CSRF, 허용 필드 바인딩, 오류 후 사용자 입력 보존 적용.
- [x] `views/main.jsp`, `resources/dist/css/main.css` 구현. 메뉴별 폼·리뷰 목록·수정 폼·삭제 확인·페이지 이동. 기존 공통 디자인 유지.
- [x] `docs/sql/menu-review.sql`에 승인한 스키마와 실행 전 확인 절차 제공. `docs/sql/menu-review-example.sql`에 선택적인 식단 등록 예시 제공.
- [x] JUnit 서비스/컨트롤러 테스트, 전체 Java 컴파일, MyBatis XML 파싱 및 JSP 컴파일 검증. 실행하지 못한 DB/브라우저 검증은 명시.

## Verification result

전체 Java 소스를 Java 8 대상으로 컴파일했고, 로컬 캐시 의존성으로 JUnit 16개가 통과했다. Tomcat Jasper에서 main.jsp 컴파일 오류 0개를 확인했다. 실제 Oracle SQL 실행, 서버 실행과 브라우저 검증은 하지 않았다.

검토에서 발견한 Oracle 복합 UNIQUE의 NULL 처리 문제는 실제 제공 메뉴가 확정된 행만 검사하는 조건부 유니크 인덱스로 수정했다. CSRF 테스트는 기존 CustomAccessDeniedHandler의 /accessError 리다이렉트 동작을 확인한다. Maven 실행 파일이 없어 직접 javac/JUnit으로 검증했으며 기존 DB 삽입 테스트는 제외했다.

