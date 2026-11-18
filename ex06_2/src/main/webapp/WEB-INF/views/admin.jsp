<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="includes/header.jsp"%>
<link href="${pageContext.request.contextPath}/resources/css/admin.css" rel="stylesheet">

<main class="admin-page" lang="ko">
    <h1 class="page-header"><i class="fa fa-lock" aria-hidden="true"></i> 관리자 페이지</h1>
    <p class="admin-intro">학교식당 리뷰 시스템의 관리자 전용 공간입니다.</p>
    <section class="admin-panel" aria-labelledby="admin-access-title">
        <div class="admin-panel-heading">
            <span class="admin-badge">관리자 전용</span>
            <h2 id="admin-access-title">접근 권한</h2>
        </div>
        <div class="admin-panel-body">
            <dl class="admin-account">
                <dt>로그인 계정</dt>
                <dd><sec:authentication property="principal.username" htmlEscape="true"/></dd>
                <dt>권한</dt>
                <dd>관리자 <span class="admin-role">ROLE_ADMIN</span></dd>
            </dl>
            <p>관리자 권한이 있는 계정만 이 페이지에 접근할 수 있습니다.</p>
            <a class="btn btn-default" href="${pageContext.request.contextPath}/main"><i class="fa fa-home" aria-hidden="true"></i> 홈으로 이동</a>
        </div>
    </section>
</main>
<%@include file="includes/footer.jsp"%>
