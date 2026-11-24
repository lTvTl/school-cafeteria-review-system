<%@ tag language="java" pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="id" required="true" type="java.lang.String" %>
<%@ attribute name="value" required="false" type="java.lang.Integer" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="star-picker">
    <c:forEach begin="1" end="5" var="score">
        <input type="radio" id="${id}-${score}" name="starScore" value="${score}" ${score eq value ? 'checked' : ''} required>
        <label for="${id}-${score}" title="${score}점" class="${score le value ? 'selected' : ''}"><span data-star-icon aria-hidden="true">${score le value ? '★' : '☆'}</span><span class="star-rating-sr-only">${score}점</span></label>
    </c:forEach>
</div>
