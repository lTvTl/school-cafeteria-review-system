<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%@include file="includes/header.jsp"%>

<div class="row">
	<div class="col-lg-12">
		<h1 class="page-header">음식카테고리</h1>
	</div>
	<!-- /.col-lg-12 -->
</div>
<!-- /.row -->

<div class="row">
	<div class="col-lg-12">
		<div class="panel panel-default">
			<div class="panel-heading">
				<i class="fa fa-cutlery fa-fw"></i> 카테고리별 메뉴 조회
			</div>
			<!-- /.panel-heading -->
			<div class="panel-body">
				<div style="margin-bottom: 20px;">
					<a href="/category?type=main" class="btn ${empty param.type or param.type eq 'main' ? 'btn-primary' : 'btn-default'}">메인메뉴</a>
					<a href="/category?type=side" class="btn ${param.type eq 'side' ? 'btn-primary' : 'btn-default'}">반찬</a>
					<a href="/category?type=dessert" class="btn ${param.type eq 'dessert' ? 'btn-primary' : 'btn-default'}">디저트</a>
					<a href="/category?type=drink" class="btn ${param.type eq 'drink' ? 'btn-primary' : 'btn-default'}">음료</a>
					<a href="/category?type=etc" class="btn ${param.type eq 'etc' ? 'btn-primary' : 'btn-default'}">기타</a>
				</div>
				<div class="well">
					<h3>
						<c:choose>
							<c:when test="${param.type eq 'side'}">반찬 카테고리 페이지</c:when>
							<c:when test="${param.type eq 'dessert'}">디저트 카테고리 페이지</c:when>
							<c:when test="${param.type eq 'drink'}">음료 카테고리 페이지</c:when>
							<c:when test="${param.type eq 'etc'}">기타 카테고리 페이지</c:when>
							<c:otherwise>메인메뉴 카테고리 페이지</c:otherwise>
						</c:choose>
					</h3>
					<p>해당 카테고리의 음식 목록 및 리뷰 기능이 구현될 영역입니다.</p>
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
