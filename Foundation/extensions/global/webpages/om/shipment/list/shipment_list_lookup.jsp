<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>

<%  String destNode ="";
    String chkBoxCounterPrefix = "chkShipment_" ;
%>

<table class="table" editable="false" width="100%" cellspacing="0">
    <thead> 
        <tr>
            <td sortable="no" class="checkboxheader">
                <input type="checkbox" name="checkbox" value="checkbox" onclick='doCheckAll(this);'/>
            </td>
            <td class="tablecolumnheader"><yfc:i18n>Shipment_#</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Status</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Expected_Ship_Date</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Actual_Ship_Date</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Origin</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Destination</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Mode</yfc:i18n></td>
        </tr>
    </thead>
    <tbody>
        <yfc:loopXML binding="xml:/Shipments/@Shipment" id="Shipment">
            <tr>
                <yfc:makeXMLInput name="shipmentKey">
                    <yfc:makeXMLKey binding="xml:/Shipment/@ShipmentKey" value="xml:/Shipment/@ShipmentKey" />
                    <yfc:makeXMLKey binding="xml:/Shipment/@ShipmentNo" value="xml:/Shipment/@ShipmentNo" />
                    <yfc:makeXMLKey binding="xml:/Shipment/@EnterpriseCode" value="xml:/Shipment/@EnterpriseCode" />
                </yfc:makeXMLInput>
                <td class="checkboxcolumn"> 
                    <input type="checkbox"  value='<%=getParameter("shipmentKey")%>' name="EntityKey" 
                        yfcMultiSelectCounter='<%=ShipmentCounter%>' yfcMultiSelectValue1='<%=getValue("Shipment", "xml:/Shipment/@ShipmentKey")%>'
                        yfcMultiSelectValue2='Modify'
                        yfcMultiSelectValue3='<%=getValue("Shipment", "xml:/Shipment/@ShipmentKey")%>'
                        yfcMultiSelectValue4='Add'/>
                </td>
				<td class="tablecolumn">
					<%if ( equals(request.getParameter(YFCUIBackendConsts.YFC_IN_POPUP),"Y")) {%>
					<a href="" onClick="showPopupDetailFor('<%=getParameter("shipmentKey")%>', '', '900', '550', new Object());return false;" >
	                    <yfc:getXMLValue binding="xml:/Shipment/@ShipmentNo"/></a>
					</a>
					<%} else {%>
						<a href="javascript:showDetailFor('<%=getParameter("shipmentKey")%>');">
		                    <yfc:getXMLValue binding="xml:/Shipment/@ShipmentNo"/>
						</a>
						<%}%>
				</td>
                <td class="tablecolumn">
                    <yfc:getXMLValueI18NDB binding="xml:/Shipment/Status/@Description"/>
		</td>
		<td class="tablecolumn" sortValue="<%=getDateValue("xml:/Shipment/@ExpectedShipmentDate")%>">
                    <yfc:getXMLValue binding="xml:/Shipment/@ExpectedShipmentDate"/>
		</td>
		<td class="tablecolumn" sortValue="<%=getDateValue("xml:/Shipment/@ActualShipmentDate")%>">
                    <yfc:getXMLValue binding="xml:/Shipment/@ActualShipmentDate"/>
		</td>
		<td class="tablecolumn">
                    <yfc:getXMLValue binding="xml:/Shipment/@ShipNode"/>
		</td>
		<td class="tablecolumn">
		<%	destNode = getValue("Shipment","xml:/Shipment/@ReceivingNode");
			if (!isVoid(destNode)) { %>
				<yfc:getXMLValue binding="xml:/Shipment/@ReceivingNode"/>
			<%} else { %>
				<% request.setAttribute("Shipment", pageContext.getAttribute("Shipment")); %>
				<jsp:include page="/common/smalladdress.jsp" flush="true" >
				     <jsp:param name="Path" value="Shipment/ToAddress"/>
				</jsp:include>
			<%}%>
		</td>
		<td class="tablecolumn">
                    <yfc:getXMLValue binding="xml:/Shipment/@ShipMode"/>
		</td>
            </tr>
        </yfc:loopXML> 
   </tbody>
</table>
