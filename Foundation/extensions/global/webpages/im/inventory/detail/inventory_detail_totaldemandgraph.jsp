<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>

<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/im/inventory/detail/inventory_detail_fusiongraphinclude.jspf" %>
<%
	String ri = getParameter("requestID");
	YFCElement root = (YFCElement)YFCUISessionCache.getInstance().getAttribute(request, ri);
	String sGraphData = getDemandGraphData(root);
  double dTotalDemand = Double.parseDouble(getParameter("totalDemand"));
%>
<table border="0" width="100%" cellpadding="0" cellspacing="0">
<% if( dTotalDemand > 0){%>
<tr>
 <!--   <td width="30%" valign="top"> 
        <img src="<%=request.getContextPath()%>/inventory/totaldemandgraph/graph?requestID=<%=ri%>" />
    </td>
-->
	<td width="30%"  valign="top">
		<yfc:YFCSingleSeriesChartTag xmlData="<%=sGraphData%>" chartType="3D_Pie_Chart" width="250" height="170" />
	</td>

    <td width="70%" valign="top" >
        <jsp:include page="/im/inventory/detail/inventory_detail_demandtypedetails.jsp" flush="true">
            <jsp:param name="requestID" value="<%=ri%>" />
        </jsp:include>
    </td>
</tr>
 <%}%>
</table>
