<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ page import="com.yantra.yfc.core.*" %>

<%
	YFCElement inboxElem = (YFCElement) request.getAttribute("Inbox");

	String status = inboxElem.getAttribute("Status");

	if (YFCObject.equals(status,"WIP")) {
		inboxElem.setAttribute("OpenVisibleFlag",  "Y");
		inboxElem.setAttribute("WipVisibleFlag",   "N");
		inboxElem.setAttribute("ClosedVisibleFlag","Y");
	} else if (YFCObject.equals(status,"CLOSED")) {
		inboxElem.setAttribute("OpenVisibleFlag",  "N");
		inboxElem.setAttribute("WipVisibleFlag",   "N");
		inboxElem.setAttribute("ClosedVisibleFlag","N");
	} else {
		// 'OPEN' status
		inboxElem.setAttribute("OpenVisibleFlag",  "N");
		inboxElem.setAttribute("WipVisibleFlag",   "Y");
		inboxElem.setAttribute("ClosedVisibleFlag","Y");
	}
%>

<table class="anchor" cellpadding="7px"  cellSpacing="0" >
<tr>
    <td colspan="2" width='100%' >
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I01"/>
        </jsp:include>
    </td>
</tr>
<tr>
    <td width='50%' >
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I04"/>
        </jsp:include>
    </td>
    <td width='50%' >
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I03"/>
        </jsp:include>
    </td>
</tr>
<tr>
    <td colspan="2" width='100%' >
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I02"/>
        </jsp:include>
    </td>
</tr>
</table>
