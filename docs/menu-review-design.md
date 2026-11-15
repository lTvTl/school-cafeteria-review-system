# 메뉴별 리뷰 구현 설계 및 Oracle SQL

## 구현 범위

- 기존 Spring MVC, JSP, MyBatis와 로그인 기능을 사용한다.
- 오늘/내일 식단을 조회하고, 오늘 실제 제공된 메뉴마다 별점(1~5점)과 리뷰를 작성한다.
- 작성한 리뷰는 메인페이지 하단에 메뉴명·배식 시간대·작성자 이름·별점·내용·작성 시각과 함께 표시한다.
- 조회는 비로그인 사용자도 가능하며, 등록·수정·삭제는 로그인한 본인만 가능하다.
- 변수명 정의서에 따라 작성 당일만 수정 가능하다. 삭제는 본인 글이면 날짜와 관계없이 가능하다.
- 제안 기본값: 사용자별 식단 항목당 리뷰 1건, 내용은 공백을 제외해 1~1,000자. 목록은 최신순으로 페이지를 나누고 이전 리뷰에도 접근할 수 있도록 한다.
- 실제 제공 메뉴가 미확정인 식단은 리뷰를 받지 않는다. 오늘 여부와 수정 가능 날짜는 정의서대로 Oracle SYSDATE를 기준으로 판단한다.

## 구현 방식

현재 코드 구조에 맞는 JSP 폼 제출 후 메인페이지로 돌아오는 방식을 제안한다. 별도 Ajax API 방식도 가능하지만 이번 기능에는 불필요한 JavaScript와 오류 처리 경로가 늘어난다. 새 프레임워크와 라이브러리는 추가하지 않는다.

수정 범위는 HomeController, 메인 JSP/CSS, 리뷰 Controller·Service·Mapper·VO 및 테스트다. 작성자 ID는 요청값 대신 로그인 세션에서 가져온다. SQL에도 소유자·날짜 조건을 넣고, 서버 입력 검증·CSRF 보호·출력 이스케이프를 적용한다. 메뉴와 식단 ID의 일치는 DB FK로도 보장한다. 평균 별점은 등록·수정·삭제와 같은 트랜잭션에서 갱신하며 메뉴 행 잠금으로 동시 갱신을 직렬화한다.

관리자 식단 등록 화면, 북마크, 회원 인증 확장은 이번 범위에서 제외한다. SQL은 사용자가 실행하며 애플리케이션이 자동으로 테이블을 생성하지 않는다.

## 실행 전 확인

아래는 **신규 테이블이 없는 경우 한 번만 실행하는 SQL 초안**이다. 기존 `tbl_member(userid)`가 있어야 한다. 2026-11-13 조회 당시 신규 세 테이블은 없었지만 현재 DB는 재조회하지 않았다. 같은 이름의 테이블·시퀀스가 있으면 그대로 실행하지 않는다. Oracle DDL은 자동 커밋되므로 실행 전 대상 계정을 확인한다.

```sql
SELECT table_name FROM user_tables
WHERE table_name IN ('TBL_MENU', 'TBL_DAILY_DIET', 'TBL_MENU_REVIEW');

SELECT sequence_name FROM user_sequences
WHERE sequence_name IN ('SEQ_MENU', 'SEQ_DIET', 'SEQ_MENU_REVIEW');
```

## 테이블 생성 SQL

```sql
CREATE TABLE tbl_menu (
    menu_id NUMBER(10) NOT NULL,
    menu_name VARCHAR2(100 CHAR) NOT NULL,
    category VARCHAR2(10) NOT NULL,
    avg_rating NUMBER(3,2) DEFAULT 0 NOT NULL,
    bookmark_cnt NUMBER(10) DEFAULT 0 NOT NULL,
    regdate DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT pk_menu PRIMARY KEY (menu_id),
    CONSTRAINT ck_menu_category CHECK
        (category IN ('MAIN', 'SIDE', 'DESSERT', 'DRINK', 'ETC')),
    CONSTRAINT ck_menu_rating CHECK (avg_rating BETWEEN 0 AND 5),
    CONSTRAINT ck_menu_bookmarks CHECK (bookmark_cnt >= 0)
);

CREATE TABLE tbl_daily_diet (
    diet_id NUMBER(10) NOT NULL,
    serve_date DATE NOT NULL,
    meal_time VARCHAR2(10) NOT NULL,
    planned_menu_id NUMBER(10) NOT NULL,
    actual_menu_id NUMBER(10),
    is_matched CHAR(1),
    CONSTRAINT pk_daily_diet PRIMARY KEY (diet_id),
    CONSTRAINT fk_diet_planned FOREIGN KEY (planned_menu_id)
        REFERENCES tbl_menu(menu_id),
    CONSTRAINT fk_diet_actual FOREIGN KEY (actual_menu_id)
        REFERENCES tbl_menu(menu_id),
    CONSTRAINT uq_diet_actual UNIQUE (diet_id, actual_menu_id),
    CONSTRAINT uq_diet_planned UNIQUE
        (serve_date, meal_time, planned_menu_id),
    CONSTRAINT ck_diet_date CHECK (serve_date = TRUNC(serve_date)),
    CONSTRAINT ck_diet_meal CHECK
        (meal_time IN ('BREAKFAST', 'LUNCH', 'DINNER')),
    CONSTRAINT ck_diet_match CHECK (
        (actual_menu_id IS NULL AND is_matched IS NULL)
        OR (actual_menu_id IS NOT NULL AND is_matched IS NOT NULL AND (
            (actual_menu_id = planned_menu_id AND is_matched = 'Y')
            OR (actual_menu_id <> planned_menu_id AND is_matched = 'N')
        ))
    )
);

CREATE TABLE tbl_menu_review (
    review_id NUMBER(10) NOT NULL,
    menu_id NUMBER(10) NOT NULL,
    diet_id NUMBER(10) NOT NULL,
    userid VARCHAR2(50) NOT NULL,
    star_score NUMBER NOT NULL,
    review_text VARCHAR2(1000 CHAR) NOT NULL,
    regdate DATE DEFAULT SYSDATE NOT NULL,
    updatedate DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT pk_menu_review PRIMARY KEY (review_id),
    CONSTRAINT fk_review_member FOREIGN KEY (userid)
        REFERENCES tbl_member(userid),
    CONSTRAINT fk_review_menu FOREIGN KEY (menu_id)
        REFERENCES tbl_menu(menu_id),
    CONSTRAINT fk_review_diet_menu FOREIGN KEY (diet_id, menu_id)
        REFERENCES tbl_daily_diet(diet_id, actual_menu_id),
    CONSTRAINT uq_review_user_diet UNIQUE (diet_id, userid),
    CONSTRAINT ck_review_score CHECK
        (star_score BETWEEN 1 AND 5 AND star_score = TRUNC(star_score)),
    CONSTRAINT ck_review_text CHECK (TRIM(review_text) IS NOT NULL)
);

CREATE SEQUENCE seq_menu START WITH 1 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE seq_diet START WITH 1 INCREMENT BY 1 NOCYCLE;
CREATE SEQUENCE seq_menu_review START WITH 1 INCREMENT BY 1 NOCYCLE;

-- 미확정 메뉴는 제외하고 실제 제공이 확정된 메뉴만 중복을 제한합니다.
CREATE UNIQUE INDEX uq_diet_served ON tbl_daily_diet (
    CASE WHEN actual_menu_id IS NOT NULL THEN serve_date END,
    CASE WHEN actual_menu_id IS NOT NULL THEN meal_time END,
    CASE WHEN actual_menu_id IS NOT NULL THEN actual_menu_id END
);
CREATE INDEX ix_diet_planned ON tbl_daily_diet(planned_menu_id);
CREATE INDEX ix_diet_actual ON tbl_daily_diet(actual_menu_id);
CREATE INDEX ix_review_menu ON tbl_menu_review(menu_id);
CREATE INDEX ix_review_user ON tbl_menu_review(userid);
CREATE INDEX ix_review_diet_menu ON tbl_menu_review(diet_id, menu_id);
CREATE INDEX ix_review_recent ON tbl_menu_review(regdate DESC, review_id DESC);
```

`diet_id` 하나는 특정 날짜·시간대의 메뉴 한 항목이다. 실제 배식이 확정되면 `actual_menu_id`와 `is_matched`를 함께 입력한다. 예정 메뉴와 같으면 `Y`, 다르면 `N`이다. 리뷰가 연결된 실제 메뉴는 FK 때문에 임의로 교체할 수 없다.

`userName`은 회원 테이블 조인으로, `editable`은 로그인 사용자와 작성 날짜 비교로 구한다. 두 값은 정의서대로 별도 저장 컬럼을 만들지 않는다. 평균 별점의 계산·당일 작성 제한·로그인 검증은 애플리케이션이 담당한다.

## 검증 계획

DB에 쓰지 않는 단위 테스트로 로그인 사용자 적용, 별점 범위, 빈 리뷰, 수정·삭제 소유권, 당일 제한 결과 처리를 확인한다. 사용자가 SQL을 실행한 테스트 DB에서 매퍼와 트랜잭션을 검증하고, 로그인/비로그인 및 두 계정으로 등록·목록·수정·삭제를 확인한다. 기존 MemberTests는 회원 데이터를 삽입하므로 실행하지 않는다.

승인된 설계에 따라 애플리케이션 코드를 구현했다. 실행용 SQL과 적용 안내는 docs/sql/에 있다. Java/JUnit 및 JSP 컴파일은 확인했으며, 실제 DB SQL 실행과 브라우저 연동 검증은 아직 하지 않았다.


