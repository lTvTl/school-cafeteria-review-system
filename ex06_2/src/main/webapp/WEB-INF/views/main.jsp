<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@include file="includes/header.jsp"%>
<link href="<c:url value='/resources/dist/css/main.css'/>" rel="stylesheet">
<c:set var="categoryLabels" value="${{'MAIN':'메인', 'SIDE':'반찬', 'DESSERT':'디저트', 'DRINK':'음료', 'ETC':'기타'}}"/>

<main class="cafeteria-main" lang="ko">
    <h1 class="page-header"><i class="fa fa-home" aria-hidden="true"></i> 홈</h1>
    <p class="intro">오늘과 내일의 식단을 확인하세요.</p>
    <c:if test="${not empty menuLoadError}"><p class="alert alert-warning" role="alert"><c:out value="${menuLoadError}"/></p></c:if>
    <div class="menu-grid">
        <c:forEach begin="0" end="1" var="day">
            <c:set var="dietList" value="${day == 0 ? todayDietList : tomorrowDietList}"/>
            <details class="meal-card" aria-labelledby="menu-title-${day}" open>
                <summary class="card-heading">
                    <h2 id="menu-title-${day}">
                        ${day == 0 ? '오늘의 메뉴' : '내일의 메뉴'}
                        <c:forTokens items="BREAKFAST,LUNCH,DINNER" delims="," var="mealTime">
                            <c:set var="hasMealTime" value="${false}"/>
                            <c:forEach items="${dietList}" var="diet">
                                <c:if test="${diet.mealTime == mealTime}"><c:set var="hasMealTime" value="${true}"/></c:if>
                            </c:forEach>
                            <c:if test="${hasMealTime}">
                                <span class="category-badge">${mealTime == 'BREAKFAST' ? '조식' : mealTime == 'LUNCH' ? '중식' : '석식'}</span>
                            </c:if>
                        </c:forTokens>
                        <span class="menu-subtitle">${day == 0 ? '오늘 학교식당에서 만나는 한 끼' : '미리 확인하는 내일의 한 끼'}</span>
                        <i class="fa fa-chevron-down menu-toggle-icon" aria-hidden="true"></i>
                    </h2>
                </summary>
                <div class="card-body">
                    <c:if test="${empty dietList and empty menuLoadError}">
                        <p class="empty-menu"><i class="fa fa-cutlery" aria-hidden="true"></i>아직 등록된 메뉴가 없습니다.</p>
                    </c:if>
                    <ul class="diet-list">
                        <c:set var="previousCategory" value=""/>
                        <c:forEach items="${dietList}" var="diet">
                            <c:if test="${diet.category != previousCategory}">
                                <li class="category-heading"><h3><c:out value="${categoryLabels[diet.category]}"/></h3></li>
                                <c:set var="previousCategory" value="${diet.category}"/>
                            </c:if>
                            <li>
                                <c:url var="menuCategoryUrl" value="/category"><c:param name="type" value="${diet.category}"/></c:url>
                                <strong><a href="<c:out value='${menuCategoryUrl}'/>"><c:out value="${diet.menuName}"/></a></strong>
                                <c:if test="${diet.bookmarked}"><span class="bookmark-heart"><i class="fa fa-heart" aria-hidden="true"></i><span class="sr-only">북마크한 메뉴</span></span></c:if>
                                <c:if test="${not empty diet.actualMenuId and diet.isMatched == 'N'}"><span class="menu-status">변경 전: <c:out value="${diet.plannedMenuName}"/></span></c:if>
                            </li>
                        </c:forEach>
                    </ul>
                </div>
            </details>
        </c:forEach>
    </div>

    <section class="review-compose" aria-labelledby="review-compose-title">
        <div class="card-heading">
            <h2 id="review-compose-title">리뷰 작성</h2>
            <p class="review-compose-note">오늘 먹은 메뉴는 어땠나요?</p>
        </div>
        <div class="card-body">
            <fieldset class="review-score">
                <legend>별점</legend>
                <div class="review-score-options">
                    <c:forEach begin="1" end="5" var="score">
                        <label class="review-score-option">
                            <input type="radio" name="starScore" value="${score}">
                            <span><i class="fa fa-star" aria-hidden="true"></i> ${score}점</span>
                        </label>
                    </c:forEach>
                </div>
            </fieldset>
            <label for="home-review-text">리뷰 내용</label>
            <textarea id="home-review-text" name="reviewText" class="form-control" rows="4" maxlength="1000" aria-describedby="home-review-hint" placeholder="맛과 양, 좋았던 점을 자유롭게 남겨 주세요."></textarea>
            <p id="home-review-hint" class="review-compose-note">최대 1,000자 · 리뷰 등록은 준비 중입니다.</p>
            <div class="review-compose-actions"><button type="button" class="btn btn-primary" disabled>리뷰 등록</button></div>
        </div>
    </section>
</main>
<script src="<c:url value='/resources/js/home-menu.js'/>"></script>
<%@include file="includes/footer.jsp"%>
