<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@include file="includes/header.jsp"%>
<link href="<c:url value='/resources/dist/css/main.css'/>" rel="stylesheet">
<link href="<c:url value='/resources/css/star-rating.css'/>" rel="stylesheet">
<c:set var="categoryLabels" value="${{'MAIN':'메인메뉴', 'SIDE':'반찬', 'DESSERT':'디저트', 'DRINK':'음료', 'ETC':'기타'}}"/>

<main class="cafeteria-main" lang="ko">
    <h1 class="page-header"><i class="fa fa-home" aria-hidden="true"></i> 홈</h1>
    <c:if test="${not empty menuLoadError}"><p class="alert alert-warning" role="alert"><c:out value="${menuLoadError}"/></p></c:if>
    <div class="menu-grid">
        <c:forEach begin="0" end="1" var="day">
            <c:set var="dietList" value="${day == 0 ? todayDietList : tomorrowDietList}"/>
            <details class="meal-card" aria-labelledby="menu-title-${day}" open>
                <summary class="card-heading">
                    <h2 id="menu-title-${day}">
                        ${day == 0 ? '오늘의 식단' : '내일의 식단'}
                        <i class="fa fa-chevron-down menu-toggle-icon" aria-hidden="true"></i>
                    </h2>
                </summary>
                <div class="card-body">
                    <div class="meal-slots">
                      <c:forTokens items="BREAKFAST,LUNCH,DINNER" delims="," var="slot">
                        <c:set var="hasSlot" value="${false}"/>
                        <c:forEach items="${dietList}" var="diet">
                            <c:if test="${diet.mealTime == slot}"><c:set var="hasSlot" value="${true}"/></c:if>
                        </c:forEach>
                        <c:if test="${slot != 'DINNER' or hasSlot}">
                        <section class="meal-slot">
                        <h3><i class="fa ${slot == 'BREAKFAST' ? 'fa-coffee' : slot == 'LUNCH' ? 'fa-sun-o' : 'fa-moon-o'}" aria-hidden="true"></i>${slot == 'BREAKFAST' ? '조식' : slot == 'LUNCH' ? '중식' : '석식'}</h3>
                        <ul class="diet-list">
                        <c:if test="${not hasSlot}"><li>등록된 메뉴가 없습니다.</li></c:if>
                        <c:set var="previousCategory" value=""/>
                        <c:forEach items="${dietList}" var="diet">
                          <c:if test="${diet.mealTime == slot}">
                            <c:if test="${diet.category != previousCategory}">
                                <li class="category-heading"><h4><c:out value="${categoryLabels[diet.category]}"/></h4></li>
                                <c:set var="previousCategory" value="${diet.category}"/>
                            </c:if>
                            <li>
                                <c:url var="menuCategoryUrl" value="/category"><c:param name="type" value="${diet.category}"/></c:url>
                                <a class="dish-link" data-category="${diet.category}" href="<c:out value='${menuCategoryUrl}'/>"><c:out value="${diet.menuName}"/></a>
                                <c:if test="${diet.bookmarked}"><span class="bookmark-heart"><i class="fa fa-heart" aria-hidden="true"></i><span class="sr-only">북마크한 메뉴</span></span></c:if>
                                <c:if test="${not empty diet.actualMenuId and diet.isMatched == 'N'}"><span class="menu-status">변경 전: <c:out value="${diet.plannedMenuName}"/></span></c:if>
                            </li>
                          </c:if>
                        </c:forEach>
                        </ul>
                        </section>
                        </c:if>
                      </c:forTokens>
                    </div>
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
