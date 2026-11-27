<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%@include file="includes/header.jsp"%>

<style>
	.mypage-panel {
		border-color: #FFE0C2;
	}
	.mypage-panel > .panel-heading {
		background-color: #FFF8F0;
		border-color: #FFE0C2;
		color: #3D312A;
		font-weight: bold;
	}
	.btn-mypage-save {
		background-color: #FF7A00;
		border-color: #E65C00;
		color: #FFFFFF;
		font-weight: bold;
	}
	.btn-mypage-save:hover,
	.btn-mypage-save:focus {
		background-color: #E65C00;
		border-color: #B34700;
		color: #FFFFFF;
	}
	.nav-tabs > li.active > a,
	.nav-tabs > li.active > a:hover,
	.nav-tabs > li.active > a:focus {
		color: #E65C00;
		font-weight: bold;
	}
	.star-badge {
		color: #FF7A00;
		font-weight: bold;
	}
	.bookmark-heart-icon {
		color: #d12e4b;
	}
	.mypage-bookmark-btn {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		min-width: 36px;
		min-height: 36px;
		padding: 4px;
		border: 0;
		background: transparent;
		color: #d12e4b;
		font-size: 22px;
		cursor: pointer;
		vertical-align: middle;
		transition: color .15s ease, transform .15s ease;
	}
	.mypage-bookmark-btn:hover {
		color: #b8203a;
		transform: scale(1.08);
	}
	.mypage-bookmark-btn:focus-visible {
		outline: 2px solid #b34700;
		outline-offset: 2px;
		border-radius: 4px;
	}
	.mypage-bookmark-btn:disabled {
		opacity: .6;
		cursor: wait;
	}
	@media (prefers-reduced-motion: reduce) {
		.mypage-bookmark-btn {
			transition: none;
		}
	}
	.mypage-category-buttons {
		display: flex;
		flex-wrap: wrap;
		gap: 8px;
		margin-bottom: 16px;
	}
	.mypage-category-buttons .btn-primary {
		background-color: #E65C00;
		border-color: #B34700;
	}
	.mypage-category-buttons .btn-primary:hover,
	.mypage-category-buttons .btn-primary:focus {
		background-color: #B34700;
		border-color: #993D00;
	}
	.mypage-bookmark-feedback {
		margin-bottom: 12px;
	}
</style>

<div class="row">
	<div class="col-lg-12">
		<h1 class="page-header">마이페이지</h1>
	</div>
</div>

<c:if test="${not empty msg}">
	<div class="alert alert-success alert-dismissable">
		<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
		<c:out value="${msg}"/>
	</div>
</c:if>
<c:if test="${not empty error}">
	<div class="alert alert-danger alert-dismissable">
		<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
		<c:out value="${error}"/>
	</div>
</c:if>

<!-- 1. 내 정보 관리 -->
<div class="row">
	<div class="col-lg-12">
		<div class="panel panel-default mypage-panel">
			<div class="panel-heading clearfix">
				<span class="pull-left" style="padding-top: 5px;">
					<i class="fa fa-user fa-fw"></i> 내 정보 관리
				</span>
				<a href="${pageContext.request.contextPath}/mypage/edit" class="btn btn-sm btn-mypage-save pull-right">
					<i class="fa fa-pencil fa-fw"></i> 회원 정보 수정
				</a>
			</div>
			<div class="panel-body">
				<div class="form-group">
					<label>사용자 ID</label>
					<p class="form-control-static" style="font-size: 16px; color: #3D312A; margin-bottom: 0;">
						<c:out value="${member.userid}"/>
					</p>
				</div>
				<div class="form-group" style="margin-bottom: 0;">
					<label>사용자 이름</label>
					<p class="form-control-static" style="font-size: 16px; color: #3D312A; margin-bottom: 0;">
						<c:out value="${member.userName}"/>
					</p>
				</div>
			</div>
		</div>
	</div>
</div>

<!-- 2. 내가 작성한 활동 내역 (메뉴 리뷰 / 게시글 / 댓글) -->
<div class="row">
	<div class="col-lg-12">
		<div class="panel panel-default mypage-panel">
			<div class="panel-heading">
				<i class="fa fa-list-alt fa-fw"></i> 내 활동 내역
			</div>
			<div class="panel-body">
				<ul class="nav nav-tabs">
					<li class="active">
						<a href="#tab-reviews" data-toggle="tab">
							내가 작성한 메뉴 리뷰 <span class="badge"><c:out value="${fn:length(myReviewList)}"/></span>
						</a>
					</li>
					<li>
						<a href="#tab-boards" data-toggle="tab">
							내가 작성한 게시글 <span class="badge"><c:out value="${fn:length(myBoardList)}"/></span>
						</a>
					</li>
					<li>
						<a href="#tab-replies" data-toggle="tab">
							내가 작성한 댓글 <span class="badge"><c:out value="${fn:length(myReplyList)}"/></span>
						</a>
					</li>
				</ul>

				<div class="tab-content" style="margin-top: 20px;">
					<!-- 메뉴 리뷰 탭 -->
					<div class="tab-pane fade in active" id="tab-reviews">
						<c:choose>
							<c:when test="${empty myReviewList}">
								<p class="text-muted">작성한 메뉴 리뷰가 없습니다.</p>
							</c:when>
							<c:otherwise>
								<div class="table-responsive">
									<table class="table table-striped table-bordered table-hover">
										<thead>
											<tr>
												<th style="width: 20%;">메뉴명</th>
												<th style="width: 12%;">별점</th>
												<th>리뷰 내용</th>
												<th style="width: 18%;">작성일</th>
											</tr>
										</thead>
										<tbody>
											<c:forEach items="${myReviewList}" var="review">
												<tr>
													<td>
														<a href="${pageContext.request.contextPath}/menu/${review.menuId}">
															<c:out value="${review.menuName}"/>
														</a>
													</td>
													<td class="star-badge">&#9733; <c:out value="${review.starScore}"/> / 5</td>
													<td><c:out value="${review.reviewText}"/></td>
													<td><fmt:formatDate pattern="yyyy-MM-dd HH:mm" value="${review.regDate}"/></td>
												</tr>
											</c:forEach>
										</tbody>
									</table>
								</div>
							</c:otherwise>
						</c:choose>
					</div>

					<!-- 게시글 탭 -->
					<div class="tab-pane fade" id="tab-boards">
						<c:choose>
							<c:when test="${empty myBoardList}">
								<p class="text-muted">작성한 게시글이 없습니다.</p>
							</c:when>
							<c:otherwise>
								<div class="table-responsive">
									<table class="table table-striped table-bordered table-hover">
										<thead>
											<tr>
												<th style="width: 10%;">번호</th>
												<th>제목</th>
												<th style="width: 12%;">댓글 수</th>
												<th style="width: 18%;">작성일</th>
											</tr>
										</thead>
										<tbody>
											<c:forEach items="${myBoardList}" var="board">
												<tr>
													<td><c:out value="${board.bno}"/></td>
													<td>
														<a href="${pageContext.request.contextPath}/board/get?bno=${board.bno}">
															<c:out value="${board.title}"/>
														</a>
													</td>
													<td><c:out value="${board.replyCnt}"/></td>
													<td><fmt:formatDate pattern="yyyy-MM-dd" value="${board.regdate}"/></td>
												</tr>
											</c:forEach>
										</tbody>
									</table>
								</div>
							</c:otherwise>
						</c:choose>
					</div>

					<!-- 댓글 탭 -->
					<div class="tab-pane fade" id="tab-replies">
						<c:choose>
							<c:when test="${empty myReplyList}">
								<p class="text-muted">작성한 댓글이 없습니다.</p>
							</c:when>
							<c:otherwise>
								<div class="table-responsive">
									<table class="table table-striped table-bordered table-hover">
										<thead>
											<tr>
												<th style="width: 15%;">게시글 번호</th>
												<th>댓글 내용</th>
												<th style="width: 18%;">작성일</th>
											</tr>
										</thead>
										<tbody>
											<c:forEach items="${myReplyList}" var="reply">
												<tr>
													<td>
														<a href="${pageContext.request.contextPath}/board/get?bno=${reply.bno}">
															#<c:out value="${reply.bno}"/>
														</a>
													</td>
													<td><c:out value="${reply.reply}"/></td>
													<td><fmt:formatDate pattern="yyyy-MM-dd HH:mm" value="${reply.replyDate}"/></td>
												</tr>
											</c:forEach>
										</tbody>
									</table>
								</div>
							</c:otherwise>
						</c:choose>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>

<!-- 3. 내가 북마크한 선호 메뉴 -->
<div class="row">
	<div class="col-lg-12">
		<div class="panel panel-default mypage-panel" id="mypageBookmarkSection"
			data-csrf-header="<c:out value='${_csrf.headerName}'/>"
			data-csrf-token="<c:out value='${_csrf.token}'/>">
			<div class="panel-heading clearfix">
				<span class="pull-left" style="padding-top: 5px;">
					<i class="fa fa-heart fa-fw bookmark-heart-icon"></i> 내가 북마크한 선호 메뉴
				</span>
				<a href="${pageContext.request.contextPath}/category" class="btn btn-sm btn-default pull-right">
					<i class="fa fa-cutlery fa-fw"></i> 메뉴 보러가기
				</a>
			</div>
			<div class="panel-body">
				<div class="mypage-category-buttons" role="group" aria-label="선호 메뉴 카테고리 선택">
					<button type="button" class="btn btn-primary btn-bookmark-category" data-category="ALL" aria-pressed="true">전체</button>
					<button type="button" class="btn btn-default btn-bookmark-category" data-category="MAIN" aria-pressed="false">메인메뉴</button>
					<button type="button" class="btn btn-default btn-bookmark-category" data-category="SIDE" aria-pressed="false">반찬</button>
					<button type="button" class="btn btn-default btn-bookmark-category" data-category="DESSERT" aria-pressed="false">디저트</button>
					<button type="button" class="btn btn-default btn-bookmark-category" data-category="DRINK" aria-pressed="false">음료</button>
					<button type="button" class="btn btn-default btn-bookmark-category" data-category="ETC" aria-pressed="false">기타</button>
				</div>
				<div id="mypageBookmarkFeedback" class="alert mypage-bookmark-feedback" role="status" aria-live="polite" style="display: none;"></div>
				<p class="text-muted" id="mypageBookmarkEmpty" style="${empty myBookmarkList ? '' : 'display: none;'}">
					북마크한 선호 메뉴가 없습니다. <a href="${pageContext.request.contextPath}/category">음식 카테고리</a>에서 마음에 드는 메뉴에 하트를 눌러보세요.
				</p>
				<p class="text-muted" id="mypageBookmarkCategoryEmpty" style="display: none;">
					해당 카테고리에 북마크한 선호 메뉴가 없습니다.
				</p>
				<c:if test="${not empty myBookmarkList}">
					<div class="table-responsive" id="mypageBookmarkTableWrap">
						<table class="table table-striped table-bordered table-hover" style="margin-bottom: 0;">
							<thead>
								<tr>
									<th>메뉴명</th>
									<th style="width: 22%;">평균 별점</th>
									<th style="width: 14%; text-align: center;">북마크</th>
								</tr>
							</thead>
							<tbody id="mypageBookmarkTbody">
								<c:forEach items="${myBookmarkList}" var="menu">
									<c:url var="bookmarkRemoveUrl" value="/menu/${menu.menuId}/bookmark/remove"/>
									<tr data-menu-id="${menu.menuId}" data-category="<c:out value='${menu.category}'/>">
										<td style="vertical-align: middle;">
											<a href="${pageContext.request.contextPath}/menu/${menu.menuId}">
												<c:out value="${menu.menuName}"/>
											</a>
										</td>
										<td class="star-badge" style="vertical-align: middle;">
											&#9733; <fmt:formatNumber value="${menu.avgRating}" pattern="0.00"/> / 5
										</td>
										<td style="text-align: center; vertical-align: middle;">
											<button type="button" class="mypage-bookmark-btn btn-remove-bookmark"
												data-menu-id="${menu.menuId}"
												data-menu-name="<c:out value='${menu.menuName}'/>"
												data-remove-url="<c:out value='${bookmarkRemoveUrl}'/>"
												aria-pressed="true"
												aria-label="<c:out value='${menu.menuName}'/> 북마크 해제">
												<i class="fa fa-heart" aria-hidden="true"></i>
											</button>
										</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</c:if>
			</div>
		</div>
	</div>
</div>

<script>
(function () {
	'use strict';
	var section = document.getElementById('mypageBookmarkSection');
	if (!section) return;

	var feedback = document.getElementById('mypageBookmarkFeedback');
	var emptyMsg = document.getElementById('mypageBookmarkEmpty');
	var categoryEmptyMsg = document.getElementById('mypageBookmarkCategoryEmpty');
	var tableWrap = document.getElementById('mypageBookmarkTableWrap');
	var tbody = document.getElementById('mypageBookmarkTbody');
	var categoryButtons = section.querySelectorAll('.btn-bookmark-category');
	var activeCategory = 'ALL';

	function showFeedback(message, isError) {
		feedback.textContent = message;
		feedback.className = 'alert mypage-bookmark-feedback ' + (isError ? 'alert-danger' : 'alert-success');
		feedback.style.display = 'block';
	}

	function applyCategoryFilter() {
		var rows = tbody ? tbody.querySelectorAll('tr') : [];
		var total = rows.length;
		var visible = 0;

		Array.prototype.forEach.call(rows, function (row) {
			var match = activeCategory === 'ALL' || row.getAttribute('data-category') === activeCategory;
			row.style.display = match ? '' : 'none';
			if (match) visible++;
		});

		if (total === 0) {
			if (tableWrap) tableWrap.style.display = 'none';
			if (emptyMsg) emptyMsg.style.display = 'block';
			if (categoryEmptyMsg) categoryEmptyMsg.style.display = 'none';
		} else if (visible === 0) {
			if (tableWrap) tableWrap.style.display = 'none';
			if (emptyMsg) emptyMsg.style.display = 'none';
			if (categoryEmptyMsg) categoryEmptyMsg.style.display = 'block';
		} else {
			if (tableWrap) tableWrap.style.display = '';
			if (emptyMsg) emptyMsg.style.display = 'none';
			if (categoryEmptyMsg) categoryEmptyMsg.style.display = 'none';
		}
	}

	Array.prototype.forEach.call(categoryButtons, function (button) {
		button.addEventListener('click', function () {
			activeCategory = button.getAttribute('data-category') || 'ALL';
			Array.prototype.forEach.call(categoryButtons, function (btn) {
				var selected = btn === button;
				btn.classList.toggle('btn-primary', selected);
				btn.classList.toggle('btn-default', !selected);
				btn.setAttribute('aria-pressed', String(selected));
			});
			applyCategoryFilter();
		});
	});

	Array.prototype.forEach.call(section.querySelectorAll('.btn-remove-bookmark'), function (button) {
		button.addEventListener('click', function () {
			if (button.disabled) return;
			var header = section.getAttribute('data-csrf-header');
			var token = section.getAttribute('data-csrf-token');
			var menuName = button.getAttribute('data-menu-name') || '메뉴';
			var removeUrl = button.getAttribute('data-remove-url');
			if (!header || !token || !removeUrl) {
				showFeedback('페이지를 새로고침한 후 다시 시도해 주세요.', true);
				return;
			}

			button.disabled = true;
			var xhr = new XMLHttpRequest();
			xhr.open('POST', removeUrl, true);
			xhr.timeout = 15000;
			xhr.setRequestHeader('Accept', 'application/json');
			xhr.setRequestHeader(header, token);
			xhr.onload = function () {
				if (xhr.status !== 200) {
					button.disabled = false;
					showFeedback('북마크를 해제하지 못했습니다. 잠시 후 다시 시도해 주세요.', true);
					return;
				}
				var row = button.closest('tr');
				if (row && row.parentNode) {
					row.parentNode.removeChild(row);
				}
				applyCategoryFilter();
				showFeedback('"' + menuName + '" 북마크를 해제했습니다.', false);
			};
			xhr.onerror = function () {
				button.disabled = false;
				showFeedback('네트워크 오류로 북마크를 해제하지 못했습니다.', true);
			};
			xhr.ontimeout = xhr.onerror;
			xhr.send();
		});
	});
}());
</script>

<%@include file="includes/footer.jsp"%>
