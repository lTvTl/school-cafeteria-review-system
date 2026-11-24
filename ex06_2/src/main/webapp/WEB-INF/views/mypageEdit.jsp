<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
</style>

<div class="row">
	<div class="col-lg-12">
		<h1 class="page-header">회원 정보 수정</h1>
	</div>
</div>

<c:if test="${not empty error}">
	<div class="alert alert-danger alert-dismissable">
		<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
		<c:out value="${error}"/>
	</div>
</c:if>

<div class="row">
	<div class="col-md-8 col-lg-6">
		<div class="panel panel-default mypage-panel">
			<div class="panel-heading">
				<i class="fa fa-pencil fa-fw"></i> 내 정보 수정
			</div>
			<div class="panel-body">
				<form role="form" method="post" action="${pageContext.request.contextPath}/mypage/update">
					<div class="form-group">
						<label for="userid">사용자 ID</label>
						<input type="text" id="userid" name="userid" class="form-control"
							value="<c:out value='${member.userid}'/>" readonly>
					</div>
					<div class="form-group">
						<label for="userName">사용자 이름</label>
						<input type="text" id="userName" name="userName" class="form-control"
							value="<c:out value='${member.userName}'/>" required>
					</div>
					<div class="form-group">
						<label for="userpw">새 비밀번호 <small class="text-muted">(변경할 경우에만 입력)</small></label>
						<input type="password" id="userpw" name="userpw" class="form-control"
							placeholder="8자리 이상, 특수문자 1개 이상 포함">
					</div>
					<div class="form-group">
						<label for="userpwConfirm">새 비밀번호 확인</label>
						<input type="password" id="userpwConfirm" name="userpwConfirm" class="form-control"
							placeholder="새 비밀번호 다시 입력">
					</div>
					<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
					<button type="submit" class="btn btn-mypage-save">
						<i class="fa fa-check fa-fw"></i> 저장
					</button>
					<a href="${pageContext.request.contextPath}/mypage" class="btn btn-default">
						취소
					</a>
				</form>
			</div>
		</div>
	</div>
</div>

<%@include file="includes/footer.jsp"%>
