<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%
String sFilename="address_" + getLocale().getCountry()+".jsp";
java.net.URL oURL=pageContext.getServletContext().getResource("/yfsjspcommon/"+sFilename);
%>

<% if (equals("Y", (String) request.getParameter("ShowAnswerSetOptions"))) {
    String answerOptionsBinding = (String) request.getParameter("AnswerSetOptionsBinding");
%>
    <input type="hidden" name="AnswerSetOptionsBinding" value='<%=answerOptionsBinding%>'/>
<% } %>

<% if (oURL != null) { %>
    <jsp:include page="<%=sFilename%>" flush="true" />
<% } else { %>
    <jsp:include page="/yfsjspcommon/address_default.jsp" flush="true" />
<% } %>