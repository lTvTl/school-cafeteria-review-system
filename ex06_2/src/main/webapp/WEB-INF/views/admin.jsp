<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@include file="includes/header.jsp"%>
<link href="${pageContext.request.contextPath}/resources/css/admin.css" rel="stylesheet">

<main class="admin-page" lang="ko">
    <h1 class="page-header"><i class="fa fa-lock" aria-hidden="true"></i> 관리자 페이지</h1>
    <p class="admin-intro">학교식당 리뷰 시스템의 관리자 전용 공간입니다.</p>
    <section class="admin-monitor" id="admin-monitor" data-status-url="<c:url value='/admin/status'/>" data-memory-usage="${memoryUsage}" data-active-users="${activeUserCnt}" aria-labelledby="admin-monitor-title">
        <div class="admin-monitor-heading">
            <h2 id="admin-monitor-title">서버 현황</h2>
            <button type="button" id="admin-refresh" class="btn btn-default"><i class="fa fa-refresh" aria-hidden="true"></i> 새로고침</button>
        </div>
        <div class="admin-stats">
            <div class="admin-stat">
                <h3>서버 상태</h3>
                <strong id="admin-server-status" class="admin-status ${serverStatus == 'NORMAL' ? 'is-normal' : 'is-busy'}">${serverStatus == 'NORMAL' ? '정상' : '혼잡'}</strong>
                <p>JVM 힙 메모리 사용률 <span id="admin-memory-usage"><fmt:formatNumber value="${memoryUsage}" maxFractionDigits="1"/>%</span></p>
                <div class="admin-chart-labels"><span>JVM 힙 메모리</span><span>100%</span></div>
                <svg class="admin-chart memory-chart" viewBox="0 0 600 160" preserveAspectRatio="none" role="img" aria-label="최근 60초 JVM 힙 메모리 사용률 그래프. 현재 값은 위에 표시됩니다.">
                    <path id="admin-memory-area" class="chart-area"/>
                    <path id="admin-memory-line" class="chart-line"/>
                </svg>
                <div class="admin-chart-labels"><span>60초 전</span><span>현재 · 0%</span></div>
                <small>힙 사용률 90% 이상이면 혼잡으로 표시합니다.</small>
            </div>
            <div class="admin-stat">
                <h3>현재 접속자</h3>
                <strong class="admin-session-count"><span id="admin-active-users"><c:out value="${activeUserCnt}"/></span> <small>세션</small></strong>
                <p>로그인·비로그인 및 관리자 세션 포함</p>
                <div class="admin-chart-labels"><span>활성 세션</span><span id="admin-users-max">5 세션</span></div>
                <svg class="admin-chart users-chart" viewBox="0 0 600 160" preserveAspectRatio="none" role="img" aria-label="최근 60초 활성 세션 수 그래프. 현재 값은 위에 표시됩니다.">
                    <path id="admin-users-area" class="chart-area"/>
                    <path id="admin-users-line" class="chart-line"/>
                </svg>
                <div class="admin-chart-labels"><span>60초 전</span><span>현재 · 0 세션</span></div>
                <small>브라우저를 닫아도 세션이 만료될 때까지 집계됩니다.</small>
            </div>
        </div>
        <p id="admin-monitor-note" class="admin-monitor-note" role="status">2초마다 갱신 · 최근 60초 · 이 페이지를 연 이후의 측정값만 표시합니다.</p>
    </section>
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
<script src="<c:url value='/resources/js/admin-status.js'/>"></script>
<%@include file="includes/footer.jsp"%>
