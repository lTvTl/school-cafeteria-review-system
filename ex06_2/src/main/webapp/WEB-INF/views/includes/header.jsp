<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>        
<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>SB Admin 2 - Bootstrap Admin Theme</title>

    <!-- Bootstrap Core CSS -->
    <link href="/resources/vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet">

    <!-- MetisMenu CSS -->
    <link href="/resources/vendor/metisMenu/metisMenu.min.css" rel="stylesheet">

    <!-- DataTables CSS -->
    <link href="/resources/vendor/datatables-plugins/dataTables.bootstrap.css" rel="stylesheet">

    <!-- DataTables Responsive CSS -->
    <link href="/resources/vendor/datatables-responsive/dataTables.responsive.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link href="/resources/dist/css/sb-admin-2.css" rel="stylesheet">

    <!-- Custom Fonts -->
    <link href="/resources/vendor/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">

    <style>
        /* 1. 따뜻한 크림/오렌지 컬러 팔레트 적용 */
        body {
            font-size: 16px;
            background-color: #FFF8F0;
            color: #3D312A;
        }
        a {
            color: #E65C00;
        }
        a:hover, a:focus {
            color: #B34700;
        }

        /* 2. 컨셉 B: 화이트/크림 상단바 & 오렌지 배지 로고 */
        .navbar-default {
            background-color: #FFFDF9;
            border-color: #FFE0C2;
        }
        .navbar-brand {
            font-size: 22px;
            font-weight: bold;
            height: 60px;
            line-height: 30px;
            color: #3D312A !important;
        }
        .brand-badge {
            display: inline-block;
            background-color: #FF7A00;
            color: #FFFFFF;
            padding: 4px 10px;
            border-radius: 8px;
            margin-right: 8px;
            font-size: 18px;
        }
        .navbar-top-links li a {
            padding: 18px 20px;
            min-height: 60px;
            font-size: 18px;
            color: #FF7A00;
        }
        .navbar-top-links li a:hover,
        .navbar-top-links li a:focus,
        .navbar-top-links .open > a {
            background-color: #FFF0E0 !important;
            color: #E65C00;
        }
        .navbar-top-links .dropdown-menu li a {
            padding: 10px 20px;
            font-size: 16px;
            color: #3D312A;
        }

        /* 좌측 사이드바 및 알약(Pill) 형태 메뉴 강조 */
        @media (min-width: 768px) {
            .sidebar {
                width: 280px;
                margin-top: 61px;
                background-color: #FFF8F0;
            }
            #page-wrapper {
                margin: 0 0 0 280px;
                padding: 15px 40px;
                min-height: 800px;
                background-color: #FFFFFF;
                border-left: 1px solid #FFE0C2;
            }
        }
        .sidebar ul li {
            border-bottom: 1px solid #FFE8D1;
        }
        .sidebar ul li a {
            padding: 14px 20px;
            margin: 6px 12px;
            border-radius: 25px;
            font-size: 18px;
            color: #3D312A;
        }
        .sidebar ul li a:hover,
        .sidebar ul li a:focus {
            background-color: #FFE8D1;
            color: #E65C00;
        }
        .sidebar ul li a.active {
            background-color: #FF7A00 !important;
            color: #FFFFFF !important;
            font-weight: bold;
        }
        .sidebar .nav-second-level li a {
            padding: 10px 20px 10px 40px;
            margin: 4px 12px;
            font-size: 16px;
        }

        /* 본문 기본 크기 및 구분선 색상 */
        .page-header {
            font-size: 32px;
            margin: 30px 0 25px;
            padding-bottom: 12px;
            border-bottom: 2px solid #FFE0C2;
            color: #3D312A;
        }
        .panel-heading {
            font-size: 18px;
            padding: 14px 20px;
        }
        .panel-body {
            padding: 25px;
            font-size: 16px;
        }
        .btn {
            font-size: 15px;
            padding: 8px 16px;
        }
        .btn-xs {
            font-size: 13px;
            padding: 4px 10px;
        }
        .form-control {
            font-size: 16px;
            height: 40px;
        }
        .table > thead > tr > th,
        .table > tbody > tr > td {
            padding: 12px 15px;
            font-size: 16px;
        }
    </style>

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
        <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

</head>

<body>

    <div id="wrapper">

        <!-- Navigation -->
        <nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="/main">
                    <span class="brand-badge"><i class="fa fa-cutlery"></i></span>학교식당 리뷰 시스템
                </a>
            </div>
            <!-- /.navbar-header -->

            <ul class="nav navbar-top-links navbar-right">
                <li class="dropdown">
                    <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="fa fa-user fa-fw"></i> <i class="fa fa-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu dropdown-user">
                        <sec:authorize access="isAuthenticated()">
                            <li><a href="/customLogout"><i class="fa fa-sign-out fa-fw"></i> Logout</a></li>
                        </sec:authorize>
                        <sec:authorize access="isAnonymous()">
                            <li><a href="/customLogin"><i class="fa fa-sign-in fa-fw"></i> Login</a></li>
                        </sec:authorize>
                    </ul>
                    <!-- /.dropdown-user -->
                </li>
                <!-- /.dropdown -->
            </ul>
            <!-- /.navbar-top-links -->

            <div class="navbar-default sidebar" role="navigation">
                <div class="sidebar-nav navbar-collapse">
                    <ul class="nav" id="side-menu">
                        <li>
                            <a href="/main"><i class="fa fa-home fa-fw"></i> 메인페이지</a>
                        </li>
                        <li>
                            <a href="#"><i class="fa fa-cutlery fa-fw"></i> 음식카테고리<span class="fa arrow"></span></a>
                            <ul class="nav nav-second-level">
                                <li>
                                    <a href="/category?type=main">메인메뉴</a>
                                </li>
                                <li>
                                    <a href="/category?type=side">반찬</a>
                                </li>
                                <li>
                                    <a href="/category?type=dessert">디저트</a>
                                </li>
                                <li>
                                    <a href="/category?type=drink">음료</a>
                                </li>
                                <li>
                                    <a href="/category?type=etc">기타</a>
                                </li>
                            </ul>
                            <!-- /.nav-second-level -->
                        </li>
                        <li>
                            <a href="/board/list"><i class="fa fa-table fa-fw"></i> 게시판</a>
                        </li>
                        <sec:authorize access="isAuthenticated()">
                        <li>
                            <a href="/mypage"><i class="fa fa-user fa-fw"></i> 마이페이지</a>
                        </li>
                        </sec:authorize>
                        <sec:authorize access="hasRole('ROLE_ADMIN')">
                        <li>
                            <a href="/admin"><i class="fa fa-cogs fa-fw"></i> 관리자 페이지</a>
                        </li>
                        </sec:authorize>
                    </ul>
                </div>
                <!-- /.sidebar-collapse -->
            </div>
            <!-- /.navbar-static-side -->
        </nav>

        <div id="page-wrapper">
        
       <!--  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script> -->
       <script src="/resources/js/jquery.min.js"></script>