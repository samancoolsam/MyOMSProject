<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<table class="anchor" cellpadding="7px"  cellSpacing="0" >
	<tr>
	    <td colspan="3" valign="top">
	        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
	            <jsp:param name="CurrentInnerPanelID" value="I01"/>
	        </jsp:include>
	    </td>
	</tr>
	<tr>
	    <td colspan="3" valign="top">
	        <jsp:include page="/yfc/innerpanel.jsp" flush="true">
	        	<jsp:param name="CurrentInnerPanelID" value="I03"/>
	    	</jsp:include>
	    </td>
	</tr>
	<tr>
	    <td width="50%" height="100%" valign="top">
	        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
	            <jsp:param name="CurrentInnerPanelID" value="I02"/>
	        </jsp:include>
	    </td>
	    <td width="50%" valign="top">
	        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
	            <jsp:param name="CurrentInnerPanelID" value="I05"/>
	            <jsp:param name="BindingNode" value="xml:/Items/Item"/>
	        </jsp:include>
	    </td>
	</tr>
</table>
