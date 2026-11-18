<%@ page language="java" contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%@include file="../includes/header.jsp"%>


<div class="row">
  <div class="col-lg-12">
    <h1 class="page-header">게시글 상세</h1>
  </div>
  <!-- /.col-lg-12 -->
</div>
<!-- /.row -->

<div class="row">
  <div class="col-lg-12">
    <div class="panel panel-default">

      <div class="panel-heading">게시글 상세보기</div>
      <!-- /.panel-heading -->
      <div class="panel-body">

          <div class="form-group">
          <label>번호</label> <input class="form-control" name='bno'
            value='<c:out value="${board.bno }"/>' readonly="readonly">
        </div>

        <div class="form-group">
          <label>제목</label> <input class="form-control" name='title'
            value='<c:out value="${board.title }"/>' readonly="readonly">
        </div>

        <div class="form-group">
          <label>내용</label>
          <textarea class="form-control" rows="3" name='content'
            readonly="readonly"><c:out value="${board.content}" /></textarea>
        </div>

        <div class="form-group">
          <label>작성자</label> <input class="form-control" name='writer'
            value='<c:out value="${board.writer }"/>' readonly="readonly">
        </div>

<%-- 		<button data-oper='modify' class="btn btn-default">
        <a href="/board/modify?bno=<c:out value="${board.bno}"/>">Modify</a></button>
        <button data-oper='list' class="btn btn-info">
        <a href="/board/list">List</a></button> --%>
<!-- <button data-oper='modify' class="btn btn-default">Modify</button>
 -->
        <%-- TODO: [통합] 세션 유저 정보 및 관리자 권한(ROLE_ADMIN) 예외 처리 점검 --%>
        <sec:authentication property="principal" var="pinfo"/>
        <sec:authorize access="hasRole('ROLE_ADMIN')" var="isAdmin"/>
        <sec:authorize access="isAuthenticated()">
        <c:if test="${pinfo.username eq board.writer}">
        	<button data-oper='modify' class="btn btn-default">수정</button>
        </c:if>
        <c:if test="${pinfo.username eq board.writer or isAdmin}">
        	<button data-oper='remove' class="btn btn-danger">삭제</button>
        </c:if>
        </sec:authorize>

<button data-oper='list' class="btn btn-info">목록</button>

<%-- <form id='operForm' action="/boad/modify" method="get">
  <input type='hidden' id='bno' name='bno' value='<c:out value="${board.bno}"/>'>
</form> --%>


<form id='operForm' action="/board/modify" method="get">
  <input type='hidden' id='bno' name='bno' value='<c:out value="${board.bno}"/>'>
  <input type='hidden' name='pageNum' value='<c:out value="${cri.pageNum}"/>'>
  <input type='hidden' name='amount' value='<c:out value="${cri.amount}"/>'>
  <input type='hidden' name='keyword' value='<c:out value="${cri.keyword}"/>'>
  <input type='hidden' name='type' value='<c:out value="${cri.type}"/>'>  
 
</form>



      </div>
      <!--  end panel-body -->

    </div>
    <!--  end panel-body -->
  </div>
  <!-- end panel -->
</div>
<!-- /.row -->



<div class='bigPictureWrapper'>
  <div class='bigPicture'>
  </div>
</div>



<style>
.uploadResult {
  width:100%;
  background-color: gray;
}
.uploadResult ul{
  display:flex;
  flex-flow: row;
  justify-content: center;
  align-items: center;
}
.uploadResult ul li {
  list-style: none;
  padding: 10px;
  align-content: center;
  text-align: center;
}
.uploadResult ul li img{
  width: 100px;
}
.uploadResult ul li span {
  color:white;
}
.bigPictureWrapper {
  position: absolute;
  display: none;
  justify-content: center;
  align-items: center;
  top:0%;
  width:100%;
  height:100%;
  background-color: gray; 
  z-index: 100;
  background:rgba(255,255,255,0.5);
}
.bigPicture {
  position: relative;
  display:flex;
  justify-content: center;
  align-items: center;
}

.bigPicture img {
  width:600px;
}

</style>



<div class="row">
  <div class="col-lg-12">
    <div class="panel panel-default">

      <div class="panel-heading">첨부파일</div>
      <!-- /.panel-heading -->
      <div class="panel-body">
        
        <div class='uploadResult'> 
          <ul>
          </ul>
        </div>
      </div>
      <!--  end panel-body -->
    </div>
    <!--  end panel-body -->
  </div>
  <!-- end panel -->
</div>
<!-- /.row -->


<div class='row'>

  <div class="col-lg-12">

    <!-- /.panel -->
    <div class="panel panel-default">
<!--       <div class="panel-heading">
        <i class="fa fa-comments fa-fw"></i> Reply
      </div> -->
      
<!--       <div class="panel-heading">
        <i class="fa fa-comments fa-fw"></i> Reply
        <button id='addReplyBtn' class='btn btn-primary btn-xs pull-right'>New Reply</button>
      </div>      -->
      
      <div class="panel-heading">
        <i class="fa fa-comments fa-fw"></i> 댓글
      </div>      
       
      <!-- /.panel-heading -->
      <div class="panel-body">        
        <sec:authorize access="isAuthenticated()">
        <div class="form-group" style="margin-bottom: 15px;">
          <div class="row">
            <div class="col-sm-2">
              <input type="text" id="inlineReplyer" class="form-control" 
                value="<sec:authentication property='principal.username'/>" readonly="readonly">
            </div>
            <div class="col-sm-8">
              <input type="text" id="inlineReply" class="form-control" placeholder="댓글 내용을 입력하세요.">
            </div>
            <div class="col-sm-2">
              <button id="inlineReplyAddBtn" type="button" class="btn btn-primary btn-block">댓글 등록</button>
            </div>
          </div>
        </div>
        <hr style="margin-top: 0; margin-bottom: 15px;">
        </sec:authorize>

        <ul class="chat">

        </ul>
        <!-- ./ end ul -->
      </div>
      <!-- /.panel .chat-panel -->

	<div class="panel-footer"></div>


		</div>
  </div>
  <!-- ./ end row -->
</div>



<!-- Modal -->
      <div class="modal fade" id="myModal" tabindex="-1" role="dialog"
        aria-labelledby="myModalLabel" aria-hidden="true">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <button type="button" class="close" data-dismiss="modal"
                aria-hidden="true">&times;</button>
              <h4 class="modal-title" id="myModalLabel">댓글 관리</h4>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label>댓글 내용</label> 
                <input class="form-control" name='reply' value='New Reply!!!!'>
              </div>      
              <div class="form-group">
                <label>작성자</label> 
                <input class="form-control" name='replyer' value='replyer' readonly="readonly">
              </div>
              <div class="form-group">
                <label>작성일</label> 
                <input class="form-control" name='replyDate' value='2018-01-01 13:13' readonly="readonly">
              </div>
      
            </div>
<div class="modal-footer">
        <button id='modalModBtn' type="button" class="btn btn-warning">수정</button>
        <button id='modalRemoveBtn' type="button" class="btn btn-danger">삭제</button>
        <button id='modalRegisterBtn' type="button" class="btn btn-primary">등록</button>
        <button id='modalCloseBtn' type="button" class="btn btn-default">닫기</button>
      </div>          </div>
          <!-- /.modal-content -->
        </div>
        <!-- /.modal-dialog -->
      </div>
      <!-- /.modal -->



<script type="text/javascript" src="/resources/js/reply.js"></script>

<script>

$(document).ready(function () {
  
  var bnoValue = '<c:out value="${board.bno}"/>';
  var replyUL = $(".chat");
  
    showList(1);
    
    function showList(page){
    	
    	console.log("show list " + page);
        
        replyService.getList({bno:bnoValue,page: page|| 1 }, function(replyCnt, list) {
          
        console.log("replyCnt: "+ replyCnt );
        console.log("list: " + list);
        console.log(list);
        
        if(page == -1){
          pageNum = Math.ceil(replyCnt/10.0);
          showList(pageNum);
          return;
        }
          
         var str="";
         
         if(list == null || list.length == 0){
           replyUL.html("");
           replyPageFooter.html("");
           return;
         }
         
         for (var i = 0, len = list.length || 0; i < len; i++) {
           str +="<li class='left clearfix' data-rno='"+list[i].rno+"'>";
           str +="  <div><div class='header'><strong class='primary-font'>"
        	   +list[i].replyer+"</strong>"; 
           str +="    <small class='pull-right text-muted'>"
               +replyService.displayTime(list[i].replyDate)+"</small></div>";
           str +="    <p>"+list[i].reply+"</p></div></li>";
         }
         
         replyUL.html(str);
         
         showReplyPage(replyCnt);

     
       });//end function
         
     }//end showList
    
    var pageNum = 1;
    var replyPageFooter = $(".panel-footer");
    
    function showReplyPage(replyCnt){
      
      var endNum = Math.ceil(pageNum / 10.0) * 10;  
      var startNum = endNum - 9; 
      
      var prev = startNum != 1;
      var next = false;
      
      if(endNum * 10 >= replyCnt){
        endNum = Math.ceil(replyCnt/10.0);
      }
      
      if(endNum * 10 < replyCnt){
        next = true;
      }
      
      var str = "<ul class='pagination pull-right'>";
      
      if(prev){
        str+= "<li class='page-item'><a class='page-link' href='"+(startNum -1)+"'>이전</a></li>";
      }
      
       
      
      for(var i = startNum ; i <= endNum; i++){
        
        var active = pageNum == i? "active":"";
        
        str+= "<li class='page-item "+active+" '><a class='page-link' href='"+i+"'>"+i+"</a></li>";
      }
      
      if(next){
        str+= "<li class='page-item'><a class='page-link' href='"+(endNum + 1)+"'>다음</a></li>";
      }
      
      str += "</ul></div>";
      
      console.log(str);
      
      replyPageFooter.html(str);
    }
     
    replyPageFooter.on("click","li a", function(e){
        e.preventDefault();
        console.log("page click");
        
        var targetPageNum = $(this).attr("href");
        
        console.log("targetPageNum: " + targetPageNum);
        
        pageNum = targetPageNum;
        
        showList(pageNum);
      });     
   
    var modal = $(".modal");
    var modalInputReply = modal.find("input[name='reply']");
    var modalInputReplyer = modal.find("input[name='replyer']");
    var modalInputReplyDate = modal.find("input[name='replyDate']");
    
    var modalModBtn = $("#modalModBtn");
    var modalRemoveBtn = $("#modalRemoveBtn");
    var modalRegisterBtn = $("#modalRegisterBtn");
    
    $("#modalCloseBtn").on("click", function(e){
    	modal.modal('hide');
    });
    
    $("#addReplyBtn").on("click", function(e){
        modal.find("input").val("");
        modal.find("input[name='replyer']").val(replyer);
        modalInputReplyDate.closest("div").hide();
        modal.find("button[id !='modalCloseBtn']").hide();
        
        modalRegisterBtn.show();
        
        $(".modal").modal("show");
        
      });

    
    $(document).ajaxSend(function(e, xhr, options) { 
        xhr.setRequestHeader(csrfHeaderName, csrfTokenValue); 
      }); 

    $("#inlineReplyAddBtn").on("click", function(e){
      var replyText = $.trim($("#inlineReply").val());
      if(!replyText){
        alert("댓글 내용을 입력하세요.");
        $("#inlineReply").focus();
        return;
      }
      var reply = {
        reply: replyText,
        replyer: replyer,
        bno: bnoValue
      };
      replyService.add(reply, function(result){
        alert(result);
        $("#inlineReply").val("");
        showList(-1);
      });
    });

    $("#inlineReply").on("keydown", function(e){
      if(e.which === 13){
        e.preventDefault();
        $("#inlineReplyAddBtn").click();
      }
    }); 

    

    modalRegisterBtn.on("click",function(e){
      
      var replyText = $.trim(modalInputReply.val());
      if(!replyText){
        alert("댓글 내용을 입력하세요.");
        modalInputReply.focus();
        return;
      }
      
      var reply = {
            reply: replyText,
            replyer:modalInputReplyer.val(),
            bno:bnoValue
          };
      replyService.add(reply, function(result){
        
        alert(result);
        
        modal.find("input").val("");
        modal.modal("hide");
        
        //showList(1);
        showList(-1);
        
      });
      
    });


  //댓글 조회 클릭 이벤트 처리 
    $(".chat").on("click", "li", function(e){
      
      var rno = $(this).data("rno");
      
      replyService.get(rno, function(reply){
      
        modalInputReply.val(reply.reply);
        modalInputReplyer.val(reply.replyer).attr("readonly","readonly");
        modalInputReplyDate.closest("div").show();
        modalInputReplyDate.val(replyService.displayTime( reply.replyDate))
        .attr("readonly","readonly");
        modal.data("rno", reply.rno);
        
        modal.find("button[id !='modalCloseBtn']").hide();
        if(replyer && replyer === reply.replyer){
          modalModBtn.show();
        }
        if((replyer && replyer === reply.replyer) || isAdmin){
          modalRemoveBtn.show();
        }
        
        $(".modal").modal("show");
            
      });
    });
  
    
modalModBtn.on("click", function(e){
	
	var originalReplyer = modalInputReplyer.val();
	var replyText = $.trim(modalInputReply.val());
	
	if(!replyText){
		 alert("댓글 내용을 입력하세요.");
		 modalInputReply.focus();
		 return;
	}
	
  var reply = {
		      rno:modal.data("rno"), 
		      reply: replyText,
		      replyer: originalReplyer};
  
	if(!replyer){
		 alert("로그인후 수정이 가능합니다.");
		 modal.modal("hide");
		 return;
	}

	console.log("Original Replyer: " + originalReplyer);
	
	if(replyer  != originalReplyer){
	 
		 alert("자신이 작성한 댓글만 수정이 가능합니다.");
		 modal.modal("hide");
		 return;
	 
	}
	  
	replyService.update(reply, function(result){
	      
	  alert(result);
	  modal.modal("hide");
	  showList(pageNum);
	  
	});
  
});   	
   	
   	modalRemoveBtn.on("click", function (e){
   	  
   	  var rno = modal.data("rno");

   	  console.log("RNO: " + rno);
   	  console.log("REPLYER: " + replyer);
   	  
   	  if(!replyer){
   		  alert("로그인후 삭제가 가능합니다.");
   		  modal.modal("hide");
   		  return;
   	  }
   	  
   	  var originalReplyer = modalInputReplyer.val();
   	  
   	  console.log("Original Replyer: " + originalReplyer);
   	  
   	  if(replyer != originalReplyer && !isAdmin){
   		  
   		  alert("자신이 작성한 댓글만 삭제가 가능합니다.");
   		  modal.modal("hide");
   		  return;
   		  
   	  }
   	  
   	  
   	  replyService.remove(rno, originalReplyer, function(result){
   	        
   	      alert(result);
   	      modal.modal("hide");
   	      showList(pageNum);
   	      
   	  });
   	  
   	});
   	
    var replyer = null;
    var isAdmin = ${isAdmin ? 'true' : 'false'};
    
    <sec:authorize access="isAuthenticated()">
    	replyer = '<sec:authentication property="principal.username"/>';   
	</sec:authorize>
 
    var csrfHeaderName ="${_csrf.headerName}"; 
    var csrfTokenValue="${_csrf.token}";
 
});

</script>



<script>

/* console.log("===============");
console.log("JS TEST");

var bnoValue = '<c:out value="${board.bno}"/>'; */

//for replyService add test
/* replyService.add(
    
    {reply:"JS Test", replyer:"tester", bno:bnoValue}
    ,
    function(result){ 
      alert("RESULT: " + result);
    }
); */


//reply List Test
/* replyService.getList({bno:bnoValue, page:1}, function(list){
    
	  for(var i = 0,  len = list.length||0; i < len; i++ ){
	    console.log(list[i]);
	  }
});
 */

 
/*  //17번 댓글 삭제 테스트 
 replyService.remove(17, function(count) {

   console.log(count);

   if (count === "success") {
     alert("REMOVED");
   }
 }, function(err) {
   alert('ERROR...');
 });
 */
 

//12번 댓글 수정 
/* replyService.update({
  rno : 12,
  bno : bnoValue,
  reply : "Modified Reply...."
}, function(result) {

  alert("수정 완료...");

});  
 */

</script>  


<script type="text/javascript">
$(document).ready(function() {
  
  var operForm = $("#operForm"); 
  
  $("button[data-oper='modify']").on("click", function(e){
    
    operForm.attr("action","/board/modify").submit();
    
  });
  
  $("button[data-oper='remove']").on("click", function(e){
    
    if(!confirm("게시글을 삭제하시겠습니까?")){
      return;
    }
    operForm.append("<input type='hidden' name='writer' value='<c:out value="${board.writer}"/>'>");
    operForm.append("<input type='hidden' name='${_csrf.parameterName}' value='${_csrf.token}'>");
    operForm.attr("action","/board/remove").attr("method","post").submit();
    
  });
    
  $("button[data-oper='list']").on("click", function(e){
    
    operForm.find("#bno").remove();
    operForm.attr("action","/board/list")
    operForm.submit();
    
  });  
});
</script>


<script>


$(document).ready(function(){
  
  (function(){
  
    var bno = '<c:out value="${board.bno}"/>';
    
    /* $.getJSON("/board/getAttachList", {bno: bno}, function(arr){
    
      console.log(arr);
      
      
    }); *///end getjson
    $.getJSON("/board/getAttachList", {bno: bno}, function(arr){
        
       console.log(arr);
       
       var str = "";
       
       $(arr).each(function(i, attach){
       
         //image type
         if(attach.fileType){
           var fileCallPath =  encodeURIComponent( attach.uploadPath+ "/s_"+attach.uuid +"_"+attach.fileName);
           
           str += "<li data-path='"+attach.uploadPath+"' data-uuid='"+attach.uuid+"' data-filename='"+attach.fileName+"' data-type='"+attach.fileType+"' ><div>";
           str += "<img src='/display?fileName="+fileCallPath+"'>";
           str += "</div>";
           str +"</li>";
         }else{
             
           str += "<li data-path='"+attach.uploadPath+"' data-uuid='"+attach.uuid+"' data-filename='"+attach.fileName+"' data-type='"+attach.fileType+"' ><div>";
           str += "<span> "+ attach.fileName+"</span><br/>";
           str += "<img src='/resources/img/attach.png'></a>";
           str += "</div>";
           str +"</li>";
         }
       });
       
       $(".uploadResult ul").html(str);
       
       
     });//end getjson

    
  })();//end function
  
  $(".uploadResult").on("click","li", function(e){
      
    console.log("view image");
    
    var liObj = $(this);
    
    var path = encodeURIComponent(liObj.data("path")+"/" + liObj.data("uuid")+"_" + liObj.data("filename"));
    
    if(liObj.data("type")){
      showImage(path.replace(new RegExp(/\\/g),"/"));
    }else {
      //download 
      self.location ="/download?fileName="+path
    }
    
    
  });
  
  function showImage(fileCallPath){
	    
    $(".bigPictureWrapper").css("display","flex").show();
    
    $(".bigPicture")
    .html("<img src='/display?fileName="+fileCallPath+"' >")
    .animate({width:'100%', height: '100%'}, 1000);
    
  }

  $(".bigPictureWrapper").on("click", function(e){
    $(".bigPicture").animate({width:'0%', height: '0%'}, 1000);
    setTimeout(function(){
      $('.bigPictureWrapper').hide();
    }, 1000);
  });

  
});

</script>



<%@include file="../includes/footer.jsp"%>
