<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%@include file="includes/header.jsp"%>

<style>
	.signup-panel {
		margin-top: 20px;
		border-color: #FFE0C2;
		box-shadow: 0 2px 8px rgba(61, 49, 42, 0.06);
	}
	.signup-panel > .panel-heading {
		background-color: #FFF8F0;
		border-color: #FFE0C2;
		color: #3D312A;
	}
	.signup-panel .panel-title {
		font-size: 20px;
		font-weight: bold;
		color: #3D312A;
	}
	.signup-panel .btn-register {
		background-color: #FF7A00;
		border-color: #E65C00;
		color: #FFFFFF;
		font-weight: bold;
	}
	.signup-panel .btn-register:hover,
	.signup-panel .btn-register:focus {
		background-color: #E65C00;
		border-color: #B34700;
		color: #FFFFFF;
	}
	.signup-panel .btn-check {
		background-color: #FFF0E0;
		border-color: #FFE0C2;
		color: #E65C00;
		font-weight: bold;
		height: 40px;
	}
	.signup-panel .btn-check:hover,
	.signup-panel .btn-check:focus {
		background-color: #FFE0C2;
		border-color: #FF7A00;
		color: #B34700;
	}
</style>

<div class="row">
	<div class="col-lg-12">
		<h1 class="page-header">회원가입</h1>
	</div>
</div>

<div class="row">
	<div class="col-md-6 col-md-offset-3 col-lg-4 col-lg-offset-4">
		<div class="signup-panel panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title"><i class="fa fa-user-plus fa-fw"></i> Sign Up</h3>
			</div>
			<div class="panel-body">
				<c:if test="${not empty errorMsg}">
					<div class="alert alert-danger" role="alert"><c:out value="${errorMsg}"/></div>
				</c:if>

				<form id="signupForm" role="form" method="post" action="<c:url value='/customSignup'/>">
					<fieldset>
						<!-- 1. 아이디 입력 및 중복 확인 -->
						<div class="form-group">
							<label for="userid">아이디</label>
							<div class="input-group">
								<input class="form-control" placeholder="사용할 아이디 입력"
									id="userid" name="userid" type="text" autofocus required>
								<span class="input-group-btn">
									<button type="button" id="btnCheckId" class="btn btn-default btn-check">중복확인</button>
								</span>
							</div>
							<small id="idCheckMsg" class="help-block" style="margin-bottom: 0;"></small>
						</div>

						<!-- 2. 비밀번호 입력 -->
						<div class="form-group">
							<label for="userpw">비밀번호</label>
							<input class="form-control" placeholder="비밀번호 입력"
								id="userpw" name="userpw" type="password" value="" required>
						</div>

						<!-- 3. 이름 입력 -->
						<div class="form-group">
							<label for="userName">이름</label>
							<input class="form-control" placeholder="이름 입력"
								id="userName" name="userName" type="text" value="" required>
						</div>

						<%--
						===================================================================
						[추후 확장 영역] 학교 웹메일 재학생 인증 UI
						나중에 웹메일 인증 기능을 붙일 때 아래 주석을 해제하여 사용할 수 있습니다.
						===================================================================
						<div class="form-group">
							<label for="schoolEmail">학교 웹메일 인증</label>
							<div class="input-group">
								<input class="form-control" placeholder="학교 이메일 입력"
									id="schoolEmail" name="schoolEmail" type="email">
								<span class="input-group-btn">
									<button type="button" id="btnSendMail" class="btn btn-default btn-check">인증번호 발송</button>
								</span>
							</div>
						</div>
						<div class="form-group">
							<div class="input-group">
								<input class="form-control" placeholder="인증번호 6자리 입력"
									id="authCode" name="authCode" type="text">
								<span class="input-group-btn">
									<button type="button" id="btnVerifyMail" class="btn btn-default btn-check">인증확인</button>
								</span>
							</div>
						</div>
						--%>

						<button type="submit" class="btn btn-lg btn-register btn-block">Register</button>
						<a href="<c:url value='/customLogin'/>" class="btn btn-lg btn-default btn-block">Back to Login</a>
					</fieldset>
					<input type="hidden" name="${_csrf.parameterName}"
						value="${_csrf.token}" />
				</form>
			</div>
		</div>
	</div>
</div>

<script>
$(document).ready(function() {
	// 아이디 중복 확인 버튼 이벤트
	$("#btnCheckId").on("click", function() {
		var userid = $.trim($("#userid").val());
		var msgBox = $("#idCheckMsg");

		if (!userid) {
			msgBox.text("아이디를 먼저 입력해주세요.").css("color", "#D9534F");
			$("#userid").focus();
			return;
		}

		$.ajax({
			url: "<c:url value='/member/checkId'/>",
			type: "GET",
			data: { userid: userid },
			success: function(result) {
				if (result === "available") {
					msgBox.text("사용 가능한 아이디입니다.").css("color", "#2E7D32");
				} else if (result === "duplicate") {
					msgBox.text("이미 사용 중인 아이디입니다.").css("color", "#D9534F");
				} else {
					msgBox.text("아이디를 확인해주세요.").css("color", "#D9534F");
				}
			}
		});
	});
});
</script>

<%@include file="includes/footer.jsp"%>
