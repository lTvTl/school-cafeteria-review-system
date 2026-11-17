<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%@include file="includes/header.jsp"%>

<style>
	.login-panel {
		margin-top: 20px;
		border-color: #FFE0C2;
		box-shadow: 0 2px 8px rgba(61, 49, 42, 0.06);
	}
	.login-panel > .panel-heading {
		background-color: #FFF8F0;
		border-color: #FFE0C2;
		color: #3D312A;
	}
	.login-panel .panel-title {
		font-size: 20px;
		font-weight: bold;
		color: #3D312A;
	}
	.login-panel .btn-login {
		background-color: #FF7A00;
		border-color: #E65C00;
		color: #FFFFFF;
		font-weight: bold;
	}
	.login-panel .btn-login:hover,
	.login-panel .btn-login:focus {
		background-color: #E65C00;
		border-color: #B34700;
		color: #FFFFFF;
	}
	.login-panel .btn-signup {
		background-color: #FFF0E0;
		border-color: #FFE0C2;
		color: #E65C00;
		font-weight: bold;
	}
	.login-panel .btn-signup:hover,
	.login-panel .btn-signup:focus {
		background-color: #FFE0C2;
		border-color: #FF7A00;
		color: #B34700;
	}
</style>

<div class="row">
	<div class="col-lg-12">
		<h1 class="page-header">로그인</h1>
	</div>
</div>

<div class="row">
	<div class="col-md-6 col-md-offset-3 col-lg-4 col-lg-offset-4">
		<div class="login-panel panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title"><i class="fa fa-sign-in fa-fw"></i> Please Sign In</h3>
			</div>
			<div class="panel-body">
				<c:if test="${not empty signupMsg}">
					<div class="alert alert-success" role="alert"><c:out value="${signupMsg}"/></div>
				</c:if>
				<c:if test="${not empty error}">
					<div class="alert alert-danger" role="alert"><c:out value="${error}"/></div>
				</c:if>
				<form role="form" method='post' action="<c:url value='/login'/>">
					<fieldset>
						<div class="form-group">
							<input class="form-control" placeholder="userid"
								name="username" type="text" autofocus>
						</div>
						<div class="form-group">
							<input class="form-control" placeholder="Password"
								name="password" type="password" value="">
						</div>
						<div class="checkbox">
							<label>
								<input name="remember-me" type="checkbox">Remember Me
							</label>
						</div>
						<button type="submit" class="btn btn-lg btn-success btn-login btn-block">Login</button>
						<a href="<c:url value='/customSignup'/>" class="btn btn-lg btn-info btn-signup btn-block">Sign Up</a>
						<a href="<c:url value='/main'/>" class="btn btn-lg btn-default btn-block">Main Page</a>
					</fieldset>
					<input type="hidden" name="${_csrf.parameterName}"
						value="${_csrf.token}" />
				</form>
			</div>
		</div>
	</div>
</div>

<script>
$(".btn-success").on("click", function(e){
	e.preventDefault();
	$("form").submit();
});
</script>

<c:if test="${param.logout != null}">
	<script>
	$(document).ready(function(){
		alert("로그아웃하였습니다.");
	});
	</script>
</c:if>

<%@include file="includes/footer.jsp"%>
