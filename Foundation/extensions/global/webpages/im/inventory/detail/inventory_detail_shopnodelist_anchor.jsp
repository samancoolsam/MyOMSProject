<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/im.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<%
	if (isShipNodeUser()) {

		YFCElement shipNodeInventoryElem = null;

		YFCElement root = getRequestDOM();
		if (root != null)
		{
			shipNodeInventoryElem = root.getChildElement("ShipNodeInventory");

			if(shipNodeInventoryElem == null) {
				shipNodeInventoryElem = root.createChild("ShipNodeInventory");
			}

			shipNodeInventoryElem = root.getChildElement("ShipNodeInventory");

			if(shipNodeInventoryElem == null) {
				shipNodeInventoryElem = root.createChild("ShipNodeInventory");
			}

			YFCElement itemElem = shipNodeInventoryElem.getChildElement("Item");
			if (itemElem==null) {
				itemElem = shipNodeInventoryElem.createChild("Item");
			}
			itemElem.setAttribute("Node",resolveValue("xml:CurrentUser:/User/@Node"));
			itemElem.setAttribute("ConsiderAllNodes","Y");
			
			request.setAttribute("ShipNodeInventory",shipNodeInventoryElem);
		}
	}
%>
<yfc:callAPI  apiID="AP1" />
<table class="anchor" cellpadding="7px"  cellSpacing="0" >
<tr>
    <td colspan="2" >
		<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
			<jsp:param name="CurrentInnerPanelID" value="I01"/>
		</jsp:include>
    </td>
</tr>
<tr>
    <td colspan="2" >
		<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
			<jsp:param name="CurrentInnerPanelID" value="I02"/>
		</jsp:include>
    </td>
</tr>
</table>
