<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@include file="includes/header.jsp"%>
<link href="<c:url value='/resources/dist/css/main.css'/>" rel="stylesheet">
<link href="<c:url value='/resources/css/star-rating.css'/>" rel="stylesheet">
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

    <%@include file="includes/daily-reviews.jsp"%>
</main>
<script src="<c:url value='/resources/js/home-menu.js'/>"></script>
<script src="<c:url value='/resources/js/star-rating.js'/>"></script>
<script src="<c:url value='/resources/js/daily-review.js'/>"></script>
<%@include file="includes/footer.jsp"%>
