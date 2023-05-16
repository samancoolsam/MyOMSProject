<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/console/jsp/order.jspf" %>

<% checkEnterpriseCurrency((YFCElement) request.getAttribute("Order")); %>

<table class="anchor" cellpadding="7px"  cellSpacing="0" >
<tr>
    <td >
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I01"/>
        </jsp:include>
    </td>
</tr>
<tr>
    <td >
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I02"/>
        </jsp:include>
    </td>
</tr>
<tr>
    <td >
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I03"/>
			<jsp:param name="IsForReturn" value="Y"/>
        </jsp:include>
    </td>
</tr>
<% if (isTrue("xml:/Order/@HasDeliveryLines") ) { 	//add following inner panel only if there are any Delivery service lines in the order %>
	<tr>
		<td >
			<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
				<jsp:param name="CurrentInnerPanelID" value="I05"/>
				<jsp:param name="IsForReturn" value="Y"/>
			</jsp:include>
		</td>
	</tr>
<% } %>
</table>
