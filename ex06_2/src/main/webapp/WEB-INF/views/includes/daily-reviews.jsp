<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="ui" %>
<c:set var="dailyCurrentUser" value=""/>
<sec:authorize access="isAuthenticated()"><sec:authentication property="name" var="dailyCurrentUser"/></sec:authorize>
<section id="daily-reviews" class="review-compose" aria-labelledby="review-compose-title">
    <div class="card-heading">
        <h2 id="review-compose-title">오늘의 학식 리뷰</h2>
        <p class="review-compose-note">오늘 제공된 학식 전체는 어떠셨나요?</p>
        <c:if test="${not empty dailyReviewPage}"><p class="review-compose-note"><fmt:formatDate value="${dailyReviewPage.serveDate}" pattern="yyyy.MM.dd"/> · 한국 시간 기준 · 하루 전체 식단에 대한 평가</p></c:if>
    </div>
    <div class="card-body">
        <c:if test="${not empty dailySuccessMessage}"><p class="alert alert-success" role="status"><c:out value="${dailySuccessMessage}"/></p></c:if>
        <c:if test="${not empty dailyErrorMessage}"><p class="alert alert-danger" role="alert"><c:out value="${dailyErrorMessage}"/></p></c:if>
        <c:if test="${not empty dailyReviewLoadError}"><p class="alert alert-warning" role="alert"><c:out value="${dailyReviewLoadError}"/></p></c:if>
        <c:if test="${not empty dailyReviewPage}">
            <sec:authorize access="isAnonymous()">
                <p>리뷰는 누구나 볼 수 있습니다. 작성하려면 <a href="<c:url value='/customLogin'/>">로그인</a>해 주세요.</p>
            </sec:authorize>
            <sec:authorize access="isAuthenticated()">
                <c:choose>
                    <c:when test="${empty dailyReviewPage.ownReview}">
                        <form method="post" action="<c:url value='/daily-reviews'/>" class="review-form daily-review-form">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <fieldset class="review-score"><legend>별점을 선택해 주세요</legend><ui:starRating id="daily-new-star"/></fieldset>
                            <label for="home-review-text">리뷰 내용</label>
                            <textarea id="home-review-text" name="reviewText" class="form-control" rows="4" maxlength="1000" required aria-describedby="home-review-hint" placeholder="오늘 학식 전체의 맛과 양, 좋았던 점을 남겨 주세요."></textarea>
                            <p id="home-review-hint" class="review-compose-note">1~1,000자 · 날짜별로 한 개의 리뷰를 작성할 수 있습니다.</p>
                            <div class="review-compose-actions"><button type="submit" class="btn btn-primary">리뷰 등록</button></div>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <c:set var="ownDailyReview" value="${dailyReviewPage.ownReview}"/>
                        <div id="daily-own-review" class="daily-own-review">
                            <h3>내 리뷰</h3><p>오늘의 학식 리뷰를 이미 작성했습니다. 아래에서 확인하거나 수정할 수 있습니다.</p>
                            <p class="daily-review-score" aria-label="${ownDailyReview.starScore}점">★ ${ownDailyReview.starScore}점</p>
                            <p class="daily-review-text"><c:out value="${ownDailyReview.reviewText}"/></p>
                            <c:if test="${ownDailyReview.editable}">
                                <details><summary>내 리뷰 수정</summary>
                                    <form method="post" action="<c:url value='/daily-reviews/${ownDailyReview.dailyReviewId}/modify'/>" class="review-form daily-review-form">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <fieldset class="review-score"><legend>별점 수정</legend><ui:starRating id="daily-edit-star" value="${ownDailyReview.starScore}"/></fieldset>
                                        <label for="daily-edit-text">리뷰 내용 수정</label>
                                        <textarea id="daily-edit-text" name="reviewText" class="form-control" rows="4" maxlength="1000" required><c:out value="${ownDailyReview.reviewText}"/></textarea>
                                        <div class="review-compose-actions"><button type="submit" class="btn btn-primary">수정 저장</button></div>
                                    </form>
                                </details>
                            </c:if>
                            <form method="post" action="<c:url value='/daily-reviews/${ownDailyReview.dailyReviewId}/remove'/>" class="daily-remove-review">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button type="submit" class="btn btn-default">내 리뷰 삭제</button>
                            </form>
                        </div>
                    </c:otherwise>
                </c:choose>
            </sec:authorize>
            <section class="daily-review-list" aria-labelledby="daily-list-title">
                <h3 id="daily-list-title">오늘 함께 나눈 리뷰</h3>
                <p class="daily-rating-summary"><span aria-hidden="true">★</span> <strong><fmt:formatNumber value="${dailyReviewPage.summary.avgRating}" pattern="0.00"/></strong> / 5 · 리뷰 <c:out value="${dailyReviewPage.summary.reviewCount}"/>개</p>
                <details class="daily-distribution"><summary>별점별 평가 보기</summary><ul>
                    <li>5점: <c:out value="${dailyReviewPage.summary.star5}"/>개</li>
                    <li>4점: <c:out value="${dailyReviewPage.summary.star4}"/>개</li>
                    <li>3점: <c:out value="${dailyReviewPage.summary.star3}"/>개</li>
                    <li>2점: <c:out value="${dailyReviewPage.summary.star2}"/>개</li>
                    <li>1점: <c:out value="${dailyReviewPage.summary.star1}"/>개</li>
                </ul></details>
                <c:if test="${empty dailyReviewPage.reviews}"><p class="daily-empty-state">아직 오늘의 학식 리뷰가 없습니다.</p></c:if>
                <c:forEach items="${dailyReviewPage.reviews}" var="dailyReview">
                    <article class="daily-review-card">
                        <div class="daily-review-meta"><strong><c:out value="${dailyReview.userName}"/></strong><span class="daily-review-score" aria-label="${dailyReview.starScore}점">★ ${dailyReview.starScore}점</span></div>
                        <p class="daily-review-text"><c:out value="${dailyReview.reviewText}"/></p>
                        <p class="daily-review-date"><fmt:formatDate value="${dailyReview.regDate}" pattern="yyyy.MM.dd HH:mm"/><c:if test="${not empty dailyReview.updateDate}"> · 수정됨 <fmt:formatDate value="${dailyReview.updateDate}" pattern="MM.dd HH:mm"/></c:if></p>
                        <c:if test="${not empty dailyCurrentUser and dailyCurrentUser eq dailyReview.userid}">
                            <c:if test="${dailyReview.editable}"><a href="#daily-own-review">내 리뷰 수정</a></c:if>
                            <form method="post" action="<c:url value='/daily-reviews/${dailyReview.dailyReviewId}/remove'/>" class="daily-remove-review">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button type="submit" class="btn btn-default">삭제</button>
                            </form>
                        </c:if>
                    </article>
                </c:forEach>
                <nav class="daily-pagination" aria-label="오늘의 학식 리뷰 페이지">
                    <c:if test="${dailyReviewPage.page gt 1}"><c:url var="dailyPrev" value="/main"><c:param name="reviewPage" value="${dailyReviewPage.page - 1}"/></c:url><a href="<c:out value='${dailyPrev}'/>#daily-reviews">이전</a></c:if>
                    <span>${dailyReviewPage.page} / ${dailyReviewPage.totalPages} 페이지</span>
                    <c:if test="${dailyReviewPage.page lt dailyReviewPage.totalPages}"><c:url var="dailyNext" value="/main"><c:param name="reviewPage" value="${dailyReviewPage.page + 1}"/></c:url><a href="<c:out value='${dailyNext}'/>#daily-reviews">다음</a></c:if>
                </nav>
            </section>
        </c:if>
    </div>
</section>
