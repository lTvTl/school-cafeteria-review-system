<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${menu.menuName}" /> 리뷰</title>
    <link rel="stylesheet" href="<c:url value='/resources/css/menu-popup.css'/>">
    <script defer src="<c:url value='/resources/js/menu-popup.js'/>"></script>
</head>
<body data-menu-id="${menu.menuId}" data-review-saved="${reviewSaved eq true}">
<c:url value="/menu/${menu.menuId}" var="detailUrl" />
<c:url value="/menu/${menu.menuId}/reviews" var="createUrl" />
<c:set var="currentUser" value="" />
<sec:authorize access="isAuthenticated()"><sec:authentication property="name" var="currentUser" /></sec:authorize>
<main class="popup-shell">
    <header class="food-header">
        <div class="top-line"><span class="eyebrow">학교식당 · 음식 리뷰</span><button type="button" id="close-popup" class="quiet-button">닫기</button></div>
        <span class="category-badge"><c:out value="${categoryLabel}" /></span>
        <h1><c:out value="${menu.menuName}" /></h1>
        <p class="rating-summary"><span class="star" aria-hidden="true">★</span> <strong><fmt:formatNumber value="${summary.avgRating}" pattern="0.00" /></strong> <span>/ 5 · 리뷰 <c:out value="${summary.reviewCount}" />개</span></p>
        <details class="distribution"><summary>별점별 평가 보기</summary>
            <ul aria-label="별점별 리뷰 개수">
                <li>5점 <span><c:out value="${summary.star5}" />개</span></li>
                <li>4점 <span><c:out value="${summary.star4}" />개</span></li>
                <li>3점 <span><c:out value="${summary.star3}" />개</span></li>
                <li>2점 <span><c:out value="${summary.star2}" />개</span></li>
                <li>1점 <span><c:out value="${summary.star1}" />개</span></li>
            </ul>
        </details>
    </header>

    <c:if test="${not empty successMessage}"><p class="notice success" role="status"><c:out value="${successMessage}" /></p></c:if>
    <c:if test="${not empty errorMessage}"><p class="notice error" role="alert"><c:out value="${errorMessage}" /></p></c:if>

    <section class="write-section" aria-labelledby="write-title">
        <h2 id="write-title">오늘의 한줄평</h2>
        <sec:authorize access="isAnonymous()">
            <p class="muted">리뷰는 누구나 볼 수 있어요. 작성하려면 로그인해 주세요.</p>
            <a class="primary-button" href="<c:url value='/menu/${menu.menuId}/write'/>">로그인하고 리뷰 쓰기</a>
        </sec:authorize>
        <sec:authorize access="isAuthenticated()">
            <form method="post" action="<c:out value='${createUrl}'/>" class="review-form">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <fieldset class="star-field"><legend>별점을 선택해 주세요</legend>
                    <div class="star-picker">
                        <c:forEach begin="1" end="5" var="score">
                            <input type="radio" id="new-star-${score}" name="starScore" value="${score}" required>
                            <label for="new-star-${score}" title="${score}점"><span aria-hidden="true">★</span><span class="sr-only">${score}점</span></label>
                        </c:forEach>
                    </div>
                </fieldset>
                <label for="new-review">리뷰 내용</label>
                <textarea id="new-review" name="reviewText" rows="3" maxlength="1000" required placeholder="음식에 대한 솔직한 한줄평을 남겨 주세요."></textarea>
                <p class="form-help">1~1,000자 · 같은 음식은 하루 한 번 작성할 수 있어요.</p>
                <button type="submit" class="primary-button">리뷰 등록</button>
            </form>
        </sec:authorize>
    </section>

    <section class="reviews-section" aria-labelledby="reviews-title">
        <h2 id="reviews-title">함께 나눈 리뷰 <span class="count"><c:out value="${summary.reviewCount}" /></span></h2>
        <c:if test="${empty reviewPage.reviews}"><p class="empty-state">아직 등록된 리뷰가 없습니다. 첫 리뷰를 작성해보세요!</p></c:if>
        <c:forEach items="${reviewPage.reviews}" var="review">
            <article class="review-card">
                <div class="review-meta"><strong><c:out value="${review.userName}" /></strong><span class="review-score" aria-label="${review.starScore}점">★ ${review.starScore}</span></div>
                <p class="review-text"><c:out value="${review.reviewText}" /></p>
                <p class="review-date"><fmt:formatDate value="${review.regDate}" pattern="yyyy.MM.dd HH:mm" />
                    <c:if test="${not empty review.updateDate}"> · 수정됨 <fmt:formatDate value="${review.updateDate}" pattern="MM.dd HH:mm" /></c:if>
                </p>
                <c:if test="${not empty currentUser and currentUser eq review.userid}">
                    <div class="own-actions">
                        <c:if test="${review.editable}">
                            <details class="edit-review"><summary>수정</summary>
                                <c:url value="/menu/${menu.menuId}/reviews/${review.reviewId}/modify" var="modifyUrl" />
                                <form method="post" action="<c:out value='${modifyUrl}'/>" class="review-form">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <fieldset class="star-field"><legend>별점 수정</legend><div class="star-picker">
                                        <c:forEach begin="1" end="5" var="score">
                                            <input type="radio" id="edit-${review.reviewId}-star-${score}" name="starScore" value="${score}" ${score eq review.starScore ? 'checked' : ''} required>
                                            <label for="edit-${review.reviewId}-star-${score}" title="${score}점"><span aria-hidden="true">★</span><span class="sr-only">${score}점</span></label>
                                        </c:forEach>
                                    </div></fieldset>
                                    <label for="edit-text-${review.reviewId}">리뷰 내용 수정</label>
                                    <textarea id="edit-text-${review.reviewId}" name="reviewText" rows="3" maxlength="1000" required><c:out value="${review.reviewText}" /></textarea>
                                    <button type="submit" class="primary-button">수정 저장</button>
                                </form>
                            </details>
                        </c:if>
                        <c:if test="${not review.editable}"><p class="form-help">작성일이 지난 리뷰는 삭제만 가능합니다.</p></c:if>
                        <c:url value="/menu/${menu.menuId}/reviews/${review.reviewId}/remove" var="removeUrl" />
                        <form method="post" action="<c:out value='${removeUrl}'/>" class="remove-review">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <button type="submit" class="quiet-button danger">삭제</button>
                        </form>
                    </div>
                </c:if>
            </article>
        </c:forEach>
        <nav class="pagination" aria-label="리뷰 페이지">
            <c:if test="${reviewPage.page gt 1}">
                <c:url value="/menu/${menu.menuId}" var="prevUrl"><c:param name="page" value="${reviewPage.page - 1}" /></c:url>
                <a href="<c:out value='${prevUrl}'/>">이전</a>
            </c:if>
            <span><c:out value="${reviewPage.page}" /> / <c:out value="${reviewPage.totalPages}" /> 페이지</span>
            <c:if test="${reviewPage.page lt reviewPage.totalPages}">
                <c:url value="/menu/${menu.menuId}" var="nextUrl"><c:param name="page" value="${reviewPage.page + 1}" /></c:url>
                <a href="<c:out value='${nextUrl}'/>">다음</a>
            </c:if>
        </nav>
    </section>
    <footer class="popup-footer"><a href="<c:url value='/category'/>">음식 카테고리로 돌아가기</a></footer>
</main>
</body>
</html>
