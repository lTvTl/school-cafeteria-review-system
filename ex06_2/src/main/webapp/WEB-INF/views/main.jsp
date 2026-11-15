<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>
<%@include file="includes/header.jsp"%>
<link href="<c:url value='/resources/dist/css/main.css'/>" rel="stylesheet">
<c:url var="reviewAction" value="/menu-reviews"/>
<c:set var="categoryLabels" value="${{'MAIN':'메인메뉴', 'SIDE':'반찬', 'DESSERT':'디저트', 'DRINK':'음료', 'ETC':'기타'}}"/>
<sec:authorize access="isAuthenticated()"><sec:authentication property="principal.username" var="currentUserid"/></sec:authorize>

<main class="cafeteria-main" lang="ko">
    <h1 class="page-header"><i class="fa fa-home" aria-hidden="true"></i> 홈</h1>
    <p class="intro">오늘과 내일의 식단을 확인하고, 오늘 먹은 메뉴를 평가해 주세요.</p>
    <c:if test="${not empty menuLoadError}"><p class="alert alert-warning" role="alert"><c:out value="${menuLoadError}"/></p></c:if>
    <div class="menu-grid">
        <c:forEach begin="0" end="1" var="day">
            <c:set var="dietList" value="${day == 0 ? todayDietList : tomorrowDietList}"/>
            <section class="meal-card" aria-labelledby="menu-title-${day}">
                <div class="card-heading">
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
                    </h2>
                    <p>${day == 0 ? '오늘 학교식당에서 만나는 한 끼' : '미리 확인하는 내일의 한 끼'}</p>
                </div>
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
                                <strong><c:out value="${diet.menuName}"/></strong>
                                <c:choose>
                                    <c:when test="${empty diet.actualMenuId}"><span class="menu-status">예정 메뉴</span></c:when>
                                    <c:when test="${diet.isMatched == 'N'}"><span class="menu-status">메뉴 변경 · 기존 <c:out value="${diet.plannedMenuName}"/></span></c:when>
                                    <c:otherwise><span class="menu-status">공지 동일</span></c:otherwise>
                                </c:choose>
                            </li>
                        </c:forEach>
                    </ul>
                </div>
            </section>
        </c:forEach>
    </div>

    <section class="review-card" id="menu-reviews" aria-labelledby="today-review-title">
        <div class="card-heading">
            <h2 id="today-review-title">오늘의 메뉴 평가</h2>
            <p>먹어 본 메뉴마다 별점과 리뷰를 남겨 주세요.</p>
        </div>
        <div class="card-body">
            <c:if test="${not empty result}"><p class="alert alert-success" role="status"><c:out value="${result}"/></p></c:if>
            <c:if test="${not empty error}"><p class="alert alert-danger" role="alert"><c:out value="${error}"/></p></c:if>
            <sec:authorize access="isAnonymous()">
                <p>리뷰를 작성하려면 <a href="<c:url value='/customLogin'/>">로그인</a>해 주세요.</p>
            </sec:authorize>
            <sec:authorize access="isAuthenticated()">
                <p class="review-note">메뉴별로 한 번씩 작성할 수 있으며, 작성 당일에만 수정할 수 있습니다. 삭제는 날짜와 관계없이 가능합니다.</p>
                <c:if test="${not empty reviewDraft.reviewId}">
                    <c:url var="draftAction" value="/menu-reviews/${reviewDraft.reviewId}/update"/>
                    <form method="post" action="${draftAction}" class="menu-review-form">
                        <h3>수정 중이던 리뷰</h3>
                        <p>저장되지 않은 입력 내용을 복원했습니다.</p>
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <label for="draftScore">별점</label>
                        <select id="draftScore" name="starScore" class="form-control" required>
                            <option value="">별점 선택</option>
                            <c:forEach begin="1" end="5" var="score"><option value="${score}" ${reviewDraft.starScore == score ? 'selected' : ''}>${score}점</option></c:forEach>
                        </select>
                        <label for="draftText">리뷰</label>
                        <textarea id="draftText" name="reviewText" class="form-control" rows="3" maxlength="1000" required><c:out value="${reviewDraft.reviewText}"/></textarea>
                        <button class="btn btn-review" type="submit">수정 다시 저장</button>
                    </form>
                </c:if>
                <c:if test="${empty todayDietList and empty menuLoadError}"><p>오늘의 식단이 등록되면 메뉴별로 평가할 수 있습니다.</p></c:if>
                <c:set var="previousReviewCategory" value=""/>
                <c:forEach items="${todayDietList}" var="diet">
                    <c:if test="${diet.category != previousReviewCategory}">
                        <h3 class="category-heading"><c:out value="${categoryLabels[diet.category]}"/></h3>
                        <c:set var="previousReviewCategory" value="${diet.category}"/>
                    </c:if>
                    <div class="menu-review-form">
                        <h4><c:out value="${diet.menuName}"/></h4>
                        <c:choose>
                            <c:when test="${empty diet.actualMenuId}"><p class="review-note">실제 제공 메뉴가 확정되면 평가할 수 있습니다.</p></c:when>
                            <c:when test="${not empty diet.reviewId}"><p class="review-note">작성 완료 · 아래 리뷰 목록에서 본인 리뷰를 수정하거나 삭제할 수 있습니다.</p></c:when>
                            <c:otherwise>
                                <form method="post" action="${reviewAction}">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <input type="hidden" name="dietId" value="${diet.dietId}">
                                    <fieldset>
                                        <legend><span class="sr-only"><c:out value="${diet.menuName}"/> </span>별점 선택</legend>
                                        <div class="rating-options">
                                            <c:forEach begin="1" end="5" var="score">
                                                <label><input type="radio" name="starScore" value="${score}" required ${empty reviewDraft.reviewId and reviewDraft.dietId == diet.dietId and reviewDraft.starScore == score ? 'checked' : ''}><span aria-hidden="true">★</span> ${score}점</label>
                                            </c:forEach>
                                        </div>
                                    </fieldset>
                                    <label for="reviewText-${diet.dietId}">리뷰 <small>(최대 1,000자)</small></label>
                                    <textarea class="form-control" id="reviewText-${diet.dietId}" name="reviewText" rows="3" maxlength="1000" required placeholder="이 메뉴의 맛과 양은 어땠나요?"><c:if test="${empty reviewDraft.reviewId and reviewDraft.dietId == diet.dietId}"><c:out value="${reviewDraft.reviewText}"/></c:if></textarea>
                                    <button type="submit" class="btn btn-review">리뷰 등록</button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:forEach>
                <c:if test="${not empty reviewDraft and empty reviewDraft.reviewId}">
                    <details class="review-draft"><summary>저장되지 않은 입력 내용 확인</summary><p class="review-text"><c:out value="${reviewDraft.reviewText}"/></p></details>
                </c:if>
            </sec:authorize>

            <h3 class="review-list-title">메뉴 리뷰</h3>
            <p class="review-note">이전 날짜의 리뷰도 최신순으로 표시합니다.</p>
            <c:if test="${empty menuReviewList and empty menuLoadError}"><p>아직 작성된 리뷰가 없습니다. 첫 리뷰를 남겨 주세요.</p></c:if>
            <c:forEach items="${menuReviewList}" var="review">
                <article class="review-item" id="review-${review.reviewId}">
                    <h4><span class="category-badge"><c:out value="${categoryLabels[review.category]}"/></span> <c:out value="${review.menuName}"/> <small><fmt:formatDate value="${review.serveDate}" pattern="yyyy.MM.dd"/></small></h4>
                    <p><strong class="review-stars" aria-label="5점 만점에 ${review.starScore}점"><c:forEach begin="1" end="5" var="score"><span aria-hidden="true">${score le review.starScore ? '★' : '☆'}</span></c:forEach> ${review.starScore}점</strong> · <c:out value="${review.userName}"/></p>
                    <p class="review-text"><c:out value="${review.reviewText}"/></p>
                    <p class="review-note">작성 <fmt:formatDate value="${review.regDate}" pattern="yyyy.MM.dd HH:mm"/> · 최근 저장 <fmt:formatDate value="${review.updateDate}" pattern="yyyy.MM.dd HH:mm"/></p>
                    <c:if test="${not empty currentUserid and currentUserid == review.userid}">
                        <c:if test="${review.editable}">
                            <details class="review-edit">
                                <summary>리뷰 수정</summary>
                                <c:url var="updateAction" value="/menu-reviews/${review.reviewId}/update"/>
                                <form method="post" action="${updateAction}">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <label for="editScore-${review.reviewId}">별점</label>
                                    <select class="form-control" id="editScore-${review.reviewId}" name="starScore" required>
                                        <c:forEach begin="1" end="5" var="score"><option value="${score}" ${review.starScore == score ? 'selected' : ''}>${score}점</option></c:forEach>
                                    </select>
                                    <label for="editText-${review.reviewId}">리뷰</label>
                                    <textarea class="form-control" id="editText-${review.reviewId}" name="reviewText" rows="3" maxlength="1000" required><c:out value="${review.reviewText}"/></textarea>
                                    <button type="submit" class="btn btn-review">수정 저장</button>
                                </form>
                            </details>
                        </c:if>
                        <c:url var="deleteAction" value="/menu-reviews/${review.reviewId}/delete"/>
                        <form method="post" action="${deleteAction}" onsubmit="return confirm('이 리뷰를 삭제할까요?');">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <button type="submit" class="btn btn-default">리뷰 삭제</button>
                        </form>
                    </c:if>
                </article>
            </c:forEach>
            <c:if test="${reviewPages gt 1}">
                <nav class="review-pagination" aria-label="리뷰 목록 페이지">
                    <c:if test="${reviewPage gt 1}"><c:url var="previousPage" value="/main"><c:param name="reviewPage" value="${reviewPage - 1}"/></c:url><a class="btn btn-default" href="${previousPage}#menu-reviews">이전</a></c:if>
                    <span>${reviewPage} / ${reviewPages} 페이지</span>
                    <c:if test="${reviewPage lt reviewPages}"><c:url var="nextPage" value="/main"><c:param name="reviewPage" value="${reviewPage + 1}"/></c:url><a class="btn btn-default" href="${nextPage}#menu-reviews">다음</a></c:if>
                </nav>
            </c:if>
        </div>
    </section>
</main>
<%@include file="includes/footer.jsp"%>
