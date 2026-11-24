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

<%@include file="includes/footer.jsp"%>
