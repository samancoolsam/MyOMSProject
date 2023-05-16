 <%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Order Management  (5725-D10)
IBM Sterling Configure Price Quote (5725-D11)
(C) Copyright IBM Corp. 2005, 2014 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/im/inventory/detail/inventory_detail_graphinclude.jspf" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/im.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<%
    String sAvailable = "TITLE_Available";
    String sAvailableQty = null ;
    if(equals(resolveValue("xml:/InventoryInformation/Item/@TrackedEverywhere"),"N")) 
		sAvailableQty = "INFINITE";
    else
	    sAvailableQty = resolveValue("xml:/InventoryInformation/Item/@AvailableToSell");


	String sSupplyTitle = "TITLE_Supply" ;
	String sSupplyQty = resolveValue("xml:/InventoryInformation/Item/InventoryTotals/Supplies/@TotalSupply");


    String sDemandTitle = "TITLE_Demand" ;
	String sDemandQty = resolveValue("xml:/InventoryInformation/Item/InventoryTotals/Demands/@TotalDemand");
        
	String sDisplayShipNode = "";
	if (isShipNodeUser())
		sDisplayShipNode = "Y";
		
%>

<table class="anchor" cellpadding="7px"  cellSpacing="0" >
<tr>
    <td colspan="2" >
		<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
			<jsp:param name="CurrentInnerPanelID" value="I01"/>
			<jsp:param name="ShowShipNode" value="<%=sDisplayShipNode%>"/>
		</jsp:include>
    </td>
</tr>
<tr>
    <td width="50%" valign="top" >
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I02"/>
            <jsp:param name="requestID" value="<%=sRequestID%>" />
            <jsp:param name="totalSupply" value="<%=String.valueOf(iTotalSupply)%>" />
            <jsp:param name="Title" value="<%=sSupplyTitle%>" />
			<jsp:param name="TitleQty" value="<%=sSupplyQty%>" />
            <jsp:param name="IPHeight" value="150px" />
        </jsp:include>
    </td>
    <td width="50%" valign="top" >
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I03"/>
            <jsp:param name="requestID" value="<%=sRequestID%>" />
            <jsp:param name="totalDemand" value="<%=String.valueOf(iTotalDemand)%>" />
            <jsp:param name="Title" value="<%=sDemandTitle%>" />
			<jsp:param name="TitleQty" value="<%=sDemandQty%>" />
            <jsp:param name="IPHeight" value="150px" />
        </jsp:include>
    </td>
</tr>
<tr>
    <td colspan="2" >
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I04"/>
            <jsp:param name="requestID" value="<%=sRequestID%>" />
            <jsp:param name="Title" value="<%=sAvailable%>" />
	 		<jsp:param name="TitleQty" value="<%=sAvailableQty%>" />
        </jsp:include>
    </td>
</tr>
</table>
