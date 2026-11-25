<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>

<%@include file="../includes/header.jsp"%>

<sec:authorize access="hasRole('ROLE_ADMIN')" var="isAdmin"/>
<c:set var="currentUserId" value=""/>
<sec:authorize access="isAuthenticated()">
	<sec:authentication property="principal.username" var="currentUserId"/>
</sec:authorize>

<style>
	.review-board-summary {
		display: flex;
		align-items: center;
		justify-content: space-between;
		flex-wrap: wrap;
		gap: 12px;
		background-color: #FFF8F0;
		border: 1px solid #FFE0C2;
		border-radius: 12px;
		padding: 16px 22px;
		margin-bottom: 20px;
	}
	.review-board-summary .summary-title {
		font-size: 18px;
		font-weight: bold;
		color: #3D312A;
		margin: 0;
	}
	.review-board-summary .summary-sub {
		font-size: 14px;
		color: #7A6558;
		margin: 4px 0 0 0;
	}
	.review-card-grid {
		display: flex;
		flex-direction: column;
		gap: 16px;
	}
	.review-item-card {
		background-color: #FFFFFF;
		border: 1px solid #FFE0C2;
		border-left: 5px solid #FF7A00;
		border-radius: 10px;
		padding: 18px 22px;
		box-shadow: 0 2px 6px rgba(61, 49, 42, 0.05);
	}
	.review-item-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		flex-wrap: wrap;
		gap: 8px;
		margin-bottom: 10px;
	}
	.review-author-box {
		display: flex;
		align-items: center;
		gap: 10px;
	}
	.review-author-name {
		font-size: 17px;
		font-weight: bold;
		color: #3D312A;
	}
	.review-star-badge {
		display: inline-block;
		background-color: #FFF0E0;
		color: #E65C00;
		font-weight: bold;
		font-size: 15px;
		padding: 3px 10px;
		border-radius: 20px;
	}
	.review-date-meta {
		font-size: 14px;
		color: #8A7770;
	}
	.review-item-body {
		font-size: 16px;
		line-height: 1.6;
		color: #3D312A;
		white-space: pre-wrap;
		word-break: break-word;
		margin: 8px 0 4px 0;
	}
	.review-item-footer {
		display: flex;
		justify-content: flex-end;
		gap: 8px;
		margin-top: 12px;
		padding-top: 10px;
		border-top: 1px dashed #FFE8D1;
	}
	.review-empty-card {
		text-align: center;
		padding: 50px 20px;
		background-color: #FFFDF9;
		border: 1px dashed #FFE0C2;
		border-radius: 12px;
		color: #8A7770;
	}
</style>

<div class="row">
	<div class="col-lg-12">
		<h1 class="page-header">학식 리뷰 확인</h1>
	</div>
</div>

<div class="row">
	<div class="col-lg-12">
		<div class="review-board-summary">
			<div>
				<p class="summary-title">
					<i class="fa fa-comments" style="color: #FF7A00;"></i>
					오늘의 학식 리뷰 (<c:out value="${pageMaker.total}" />개)
				</p>
				<p class="summary-sub">오늘 작성된 학식 리뷰만 표시됩니다. 본인이 오늘 작성한 리뷰는 수정 및 삭제가 가능합니다.</p>
			</div>
			<a href="${pageContext.request.contextPath}/main#daily-reviews" class="btn btn-primary" style="background-color: #FF7A00; border-color: #E65C00;">
				<i class="fa fa-pencil"></i> 오늘의 리뷰 작성하러 가기
			</a>
		</div>

		<c:choose>
			<c:when test="${empty reviewList}">
				<div class="review-empty-card">
					<i class="fa fa-comment-o fa-3x" style="color: #FFB370; margin-bottom: 12px;"></i>
					<h4 style="margin-bottom: 6px; color: #3D312A;">아직 오늘 등록된 학식 리뷰가 없습니다.</h4>
					<p style="margin-bottom: 0;">메인페이지에서 오늘의 첫 번째 학식 리뷰를 남겨보세요!</p>
				</div>
			</c:when>
			<c:otherwise>
				<div class="review-card-grid">
					<c:forEach items="${reviewList}" var="review">
						<div class="review-item-card">
							<div class="review-item-header">
								<div class="review-author-box">
									<span class="review-author-name">
										<i class="fa fa-user-circle-o" style="color: #FF7A00;"></i>
										<c:out value="${not empty review.userName ? review.userName : review.userid}" />
									</span>
									<span class="review-star-badge">
										<c:forEach begin="1" end="5" var="s">
											<c:choose>
												<c:when test="${s <= review.starScore}">★</c:when>
												<c:otherwise>☆</c:otherwise>
											</c:choose>
										</c:forEach>
										<c:out value="${review.starScore}" />점
									</span>
								</div>
								<div class="review-date-meta">
									<fmt:formatDate pattern="yyyy-MM-dd HH:mm" value="${review.regDate}" />
									<c:if test="${not empty review.updateDate}">
										<span> · 수정됨 (<fmt:formatDate pattern="HH:mm" value="${review.updateDate}" />)</span>
									</c:if>
								</div>
							</div>

							<div class="review-item-body"><c:out value="${review.reviewText}" /></div>

							<c:if test="${(not empty currentUserId and currentUserId eq review.userid) or isAdmin}">
								<div class="review-item-footer">
									<c:if test="${not empty currentUserId and currentUserId eq review.userid and review.editable}">
										<button type="button" class="btn btn-xs btn-warning btn-edit-review"
											data-id="<c:out value='${review.dailyReviewId}'/>"
											data-userid="<c:out value='${review.userid}'/>"
											data-star="<c:out value='${review.starScore}'/>"
											data-text="<c:out value='${review.reviewText}'/>">
											<i class="fa fa-edit"></i> 수정
										</button>
									</c:if>
									<button type="button" class="btn btn-xs btn-danger btn-remove-review"
										data-id="<c:out value='${review.dailyReviewId}'/>"
										data-userid="<c:out value='${review.userid}'/>">
										<i class="fa fa-trash-o"></i> 삭제
									</button>
								</div>
							</c:if>
						</div>
					</c:forEach>
				</div>
			</c:otherwise>
		</c:choose>

		<div class="pull-right" style="margin-top: 20px;">
			<ul class="pagination">
				<c:if test="${pageMaker.prev}">
					<li class="paginate_button previous">
						<a href="${pageMaker.startPage - 1}">이전</a>
					</li>
				</c:if>

				<c:forEach var="num" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
					<li class="paginate_button ${pageMaker.cri.pageNum == num ? 'active' : ''}">
						<a href="${num}">${num}</a>
					</li>
				</c:forEach>

				<c:if test="${pageMaker.next}">
					<li class="paginate_button next">
						<a href="${pageMaker.endPage + 1}">다음</a>
					</li>
				</c:if>
			</ul>
		</div>

		<form id="actionForm" action="${pageContext.request.contextPath}/board/reviews" method="get">
			<input type="hidden" name="pageNum" value="<c:out value='${pageMaker.cri.pageNum}'/>">
			<input type="hidden" name="amount" value="<c:out value='${pageMaker.cri.amount}'/>">
		</form>

		<form id="removeReviewForm" action="${pageContext.request.contextPath}/board/reviews/remove" method="post" style="display:none;">
			<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
			<input type="hidden" name="dailyReviewId" id="removeReviewId">
			<input type="hidden" name="userid" id="removeReviewUserid">
			<input type="hidden" name="pageNum" value="<c:out value='${pageMaker.cri.pageNum}'/>">
			<input type="hidden" name="amount" value="<c:out value='${pageMaker.cri.amount}'/>">
		</form>

		<!-- 당일 리뷰 수정 모달 -->
		<div class="modal fade" id="editReviewModal" tabindex="-1" role="dialog" aria-labelledby="editReviewModalLabel" aria-hidden="true">
			<div class="modal-dialog">
				<div class="modal-content">
					<form id="editReviewForm" action="${pageContext.request.contextPath}/board/reviews/modify" method="post">
						<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
						<input type="hidden" name="dailyReviewId" id="editReviewId">
						<input type="hidden" name="userid" id="editReviewUserid">
						<input type="hidden" name="pageNum" value="<c:out value='${pageMaker.cri.pageNum}'/>">
						<input type="hidden" name="amount" value="<c:out value='${pageMaker.cri.amount}'/>">

						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
							<h4 class="modal-title" id="editReviewModalLabel">오늘의 학식 리뷰 수정</h4>
						</div>
						<div class="modal-body">
							<div class="form-group">
								<label for="editStarScore">별점</label>
								<select class="form-control" name="starScore" id="editStarScore">
									<option value="5">★★★★★ (5점)</option>
									<option value="4">★★★★☆ (4점)</option>
									<option value="3">★★★☆☆ (3점)</option>
									<option value="2">★★☆☆☆ (2점)</option>
									<option value="1">★☆☆☆☆ (1점)</option>
								</select>
							</div>
							<div class="form-group">
								<label for="editReviewText">리뷰 내용</label>
								<textarea class="form-control" name="reviewText" id="editReviewText" rows="4" maxlength="1000"></textarea>
							</div>
						</div>
						<div class="modal-footer">
							<button type="submit" class="btn btn-primary">수정 저장</button>
							<button type="button" class="btn btn-default" data-dismiss="modal">닫기</button>
						</div>
					</form>
				</div>
			</div>
		</div>

		<!-- 알림 모달 -->
		<div class="modal fade" id="msgModal" tabindex="-1" role="dialog" aria-labelledby="msgModalLabel" aria-hidden="true">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
						<h4 class="modal-title" id="msgModalLabel">알림</h4>
					</div>
					<div class="modal-body" id="msgModalBody"></div>
					<div class="modal-footer">
						<button type="button" class="btn btn-primary" data-dismiss="modal">확인</button>
					</div>
				</div>
			</div>
		</div>

	</div>
</div>

<script type="text/javascript">
$(document).ready(function() {
	var reviewMsg = '<c:out value="${reviewMsg}"/>';
	if (reviewMsg !== '' && !history.state) {
		$("#msgModalBody").text(reviewMsg);
		$("#msgModal").modal("show");
	}
	history.replaceState({}, null, null);

	var actionForm = $("#actionForm");
	$(".paginate_button a").on("click", function(e) {
		e.preventDefault();
		actionForm.find("input[name='pageNum']").val($(this).attr("href"));
		actionForm.submit();
	});

	$(".btn-edit-review").on("click", function() {
		var btn = $(this);
		$("#editReviewId").val(btn.data("id"));
		$("#editReviewUserid").val(btn.data("userid"));
		$("#editStarScore").val(btn.data("star"));
		$("#editReviewText").val(btn.data("text"));
		$("#editReviewModal").modal("show");
	});

	$("#editReviewForm").on("submit", function(e) {
		var text = $.trim($("#editReviewText").val());
		if (!text) {
			alert("리뷰 내용을 입력하세요.");
			$("#editReviewText").focus();
			e.preventDefault();
			return false;
		}
	});

	$(".btn-remove-review").on("click", function() {
		if (!confirm("이 학식 리뷰를 삭제하시겠습니까?")) {
			return;
		}
		var btn = $(this);
		$("#removeReviewId").val(btn.data("id"));
		$("#removeReviewUserid").val(btn.data("userid"));
		$("#removeReviewForm").submit();
	});
});
</script>

<%@include file="../includes/footer.jsp"%>
