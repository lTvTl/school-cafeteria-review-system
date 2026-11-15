<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@include file="includes/header.jsp"%>

<style>
    .food-category .category-buttons { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 20px; }
    .food-category .category-buttons .btn-primary { background: #E65C00; border-color: #B34700; }
    .food-category .category-buttons .btn-primary:hover,
    .food-category .category-buttons .btn-primary:focus { background: #B34700; }
    .food-category .panel-heading { background: #FFF8F0; border-color: #FFE0C2; }
    .food-category .menu-table { min-width: 480px; }
    .food-category .menu-name { overflow-wrap: anywhere; word-break: break-word; }
    .food-category .menu-rating { white-space: nowrap; }
    .food-category .menu-rating .fa-star { color: #E65C00; }
    @media (max-width: 767px) { .food-category .panel-body { padding: 15px; } }
</style>

<div class="food-category">
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
                            <c:choose>
                                <c:when test="${empty menuList}">
                                    <div class="well">등록된 음식이 없습니다.</div>
                                </c:when>
                                <c:otherwise>
                                    <div class="table-responsive">
                                        <table class="table table-striped menu-table">
                                            <caption class="sr-only"><c:out value="${categoryLabel}" /> 음식 목록</caption>
                                            <thead><tr><th scope="col">음식명</th><th scope="col">카테고리</th><th scope="col">평균 별점</th><th scope="col">북마크 수</th></tr></thead>
                                            <tbody>
                                                <c:forEach items="${menuList}" var="menu">
                                                    <tr data-menu-id="${menu.menuId}">
                                                        <td class="menu-name">
                                                            <c:url value="/menu/${menu.menuId}" var="menuUrl" />
                                                            <a class="menu-popup-link" href="<c:out value='${menuUrl}'/>" data-menu-id="${menu.menuId}" aria-haspopup="dialog"><c:out value="${menu.menuName}" /></a>
                                                        </td>
                                                        <td><c:out value="${categoryLabel}" /></td>
                                                        <td class="menu-rating"><i class="fa fa-star" aria-hidden="true"></i> <span class="menu-average" aria-live="polite"><fmt:formatNumber value="${menu.avgRating}" pattern="0.00" /></span> / 5</td>
                                                        <td><fmt:formatNumber value="${menu.bookmarkCnt}" pattern="#,##0" /></td>
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
