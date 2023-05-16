<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<table cellSpacing=0 class="anchor" cellpadding="7px">
<tr>
    <td>
	    <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I01"/>
        </jsp:include>
    </td>
</tr>
<tr>
    <td>
	    <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I02"/>
			<jsp:param name="ReqNameSpace" value="Order"/>
			<jsp:param name="Level" value="01"/>
        </jsp:include>
    </td>
</tr>
</table>
