<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>
<%@include file="includes/header.jsp"%>
<c:set var="bookmarkLoggedIn" value="${false}"/>
<sec:authorize access="isAuthenticated()"><c:set var="bookmarkLoggedIn" value="${true}"/></sec:authorize>

<style>
    .food-category .category-buttons { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 20px; }
    .food-category .category-buttons .btn-primary { background: #E65C00; border-color: #B34700; }
    .food-category .category-buttons .btn-primary:hover,
    .food-category .category-buttons .btn-primary:focus { background: #B34700; }
    .food-category .panel-heading { background: #FFF8F0; border-color: #FFE0C2; }
    .food-category .menu-table { min-width: 320px; table-layout: fixed; }
    .food-category .menu-table th:nth-child(1) { width: 42%; }
    .food-category .menu-table th:nth-child(2) { width: 31%; }
    .food-category .menu-table th:nth-child(3) { width: 27%; }
    .food-category .menu-table .menu-name { white-space: normal; overflow-wrap: anywhere; word-break: break-word; }
    .food-category .menu-rating { white-space: nowrap; }
    .food-category .menu-rating .fa-star { color: #E65C00; }
    .food-category .menu-bookmarks { white-space: nowrap; }
    .food-category .bookmark-button { display: inline-flex; align-items: center; justify-content: center; min-width: 40px; min-height: 40px; padding: 4px; border: 0; background: transparent; color: #8a7770; font-size: 22px; cursor: pointer; vertical-align: middle; transition: color .15s ease, transform .15s ease; }
    .food-category .bookmark-button[aria-pressed="true"] { color: #d12e4b; }
    .food-category .bookmark-button:hover { color: #b8203a; transform: scale(1.08); }
    .food-category .bookmark-button:focus-visible { outline: 2px solid #b34700; outline-offset: 2px; border-radius: 4px; }
    .food-category .bookmark-button:disabled { opacity: .6; cursor: wait; }
    .food-category .bookmark-count { display: inline-block; min-width: 1em; vertical-align: middle; }
    .food-category .bookmark-feedback { color: #943900; margin: 10px 0; }
    @media (prefers-reduced-motion: reduce) { .food-category .bookmark-button { transition: none; } }
    @media (max-width: 767px) { .food-category .panel-body { padding: 15px; } }
    @media (max-width: 480px) { .food-category .menu-table { font-size: 13px; } .food-category .menu-table th, .food-category .menu-table td { padding: 8px 6px; } }
</style>

<div class="food-category" data-bookmark-authenticated="${bookmarkLoggedIn}"
     data-csrf-header="<c:out value='${_csrf.headerName}'/>" data-csrf-token="<c:out value='${_csrf.token}'/>">
    <div class="row">
        <div class="col-lg-12"><h1 class="page-header">음식 카테고리</h1></div>
    </div>
    <div class="row">
        <div class="col-lg-12">
            <div class="panel panel-default">
                <div class="panel-heading"><i class="fa fa-cutlery fa-fw" aria-hidden="true"></i> 카테고리별 메뉴 조회</div>
                <div class="panel-body">
                    <nav class="category-buttons" aria-label="음식 카테고리 선택">
                        <c:forEach items="${categories}" var="category">
                            <c:url value="/category" var="categoryUrl"><c:param name="type" value="${category.type}" /></c:url>
                            <a href="<c:out value='${categoryUrl}'/>"
                               class="btn ${selectedCategory eq category.type ? 'btn-primary' : 'btn-default'}"
                               aria-current="${selectedCategory eq category.type ? 'page' : 'false'}"><c:out value="${category.label}" /></a>
                        </c:forEach>
                    </nav>
                    <c:choose>
                        <c:when test="${not empty errorMessage}">
                            <div class="alert alert-warning" role="alert"><c:out value="${errorMessage}" /></div>
                        </c:when>
                        <c:otherwise>
                            <h2 class="h3"><c:out value="${categoryLabel}" /> <small>총 <c:out value="${menuCount}" />개</small></h2>
                            <p class="bookmark-feedback" role="status" aria-live="polite" hidden><span class="bookmark-feedback-text"></span> <a class="bookmark-login" href="<c:url value='/customLogin'/>" hidden>로그인</a></p>
                            <c:choose>
                                <c:when test="${empty menuList}">
                                    <div class="well">등록된 음식이 없습니다.</div>
                                </c:when>
                                <c:otherwise>
                                    <div class="table-responsive">
                                        <table class="table table-striped menu-table">
                                            <caption class="sr-only"><c:out value="${categoryLabel}" /> 음식 목록</caption>
                                            <thead><tr><th scope="col">음식명</th><th scope="col">평균 별점</th><th scope="col">북마크 수</th></tr></thead>
                                            <tbody>
                                                <c:forEach items="${menuList}" var="menu">
                                                    <tr data-menu-id="${menu.menuId}">
                                                        <td class="menu-name">
                                                            <c:url value="/menu/${menu.menuId}" var="menuUrl" />
                                                            <a class="menu-popup-link" href="<c:out value='${menuUrl}'/>" data-menu-id="${menu.menuId}" aria-haspopup="dialog"><c:out value="${menu.menuName}" /></a>
                                                        </td>
                                                        <td class="menu-rating"><i class="fa fa-star" aria-hidden="true"></i> <span class="menu-average" aria-live="polite"><fmt:formatNumber value="${menu.avgRating}" pattern="0.00" /></span> / 5</td>
                                                        <td class="menu-bookmarks">
                                                            <c:url var="bookmarkAddUrl" value="/menu/${menu.menuId}/bookmark/add"/>
                                                            <c:url var="bookmarkRemoveUrl" value="/menu/${menu.menuId}/bookmark/remove"/>
                                                            <button type="button" class="bookmark-button" data-menu-id="${menu.menuId}"
                                                                data-menu-name="<c:out value='${menu.menuName}'/>" data-bookmarked="${menu.bookmarked}"
                                                                data-add-url="<c:out value='${bookmarkAddUrl}'/>" data-remove-url="<c:out value='${bookmarkRemoveUrl}'/>"
                                                                aria-pressed="${menu.bookmarked}" aria-label="<c:out value='${menu.menuName}'/> 북마크 ${menu.bookmarked ? '해제' : '등록'}">
                                                                <i class="fa ${menu.bookmarked ? 'fa-heart' : 'fa-heart-o'}" aria-hidden="true"></i>
                                                            </button>
                                                            <span class="bookmark-count" aria-live="polite"><fmt:formatNumber value="${menu.bookmarkCnt}" pattern="#,##0" /></span>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="<c:url value='/resources/js/menu-category.js'/>"></script>
<%@include file="includes/footer.jsp"%>
