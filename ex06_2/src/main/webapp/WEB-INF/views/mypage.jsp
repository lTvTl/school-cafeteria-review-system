<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%@include file="includes/header.jsp"%>

<div class="row">
	<div class="col-lg-12">
		<h1 class="page-header">마이페이지</h1>
	</div>
	<!-- /.col-lg-12 -->
</div>
<!-- /.row -->

<div class="row">
	<div class="col-lg-12">
		<div class="panel panel-default">
			<div class="panel-heading">
				<i class="fa fa-user fa-fw"></i> 내 정보 관리
			</div>
			<!-- /.panel-heading -->
			<div class="panel-body">
				<sec:authorize access="isAuthenticated()">
					<div class="form-group">
						<label>사용자 ID</label>
						<input class="form-control" value='<sec:authentication property="principal.username"/>' readonly="readonly">
					</div>
				</sec:authorize>
				<div class="well">
					<h3>마이페이지 영역</h3>
					<p>회원 정보 수정, 내가 작성한 리뷰 및 게시글 확인 등의 마이페이지 세부 기능이 구현될 영역입니다.</p>
				</div>
			</div>
			<!-- /.panel-body -->
		</div>
		<!-- /.panel -->
	</div>
	<!-- /.col-lg-12 -->
</div>
<!-- /.row -->

<%@include file="includes/footer.jsp"%>
