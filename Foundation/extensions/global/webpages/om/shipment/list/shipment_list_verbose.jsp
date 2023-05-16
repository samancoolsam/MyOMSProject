<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/modificationreason.js"></script>

<%  String destNode ="";%>

<table class="table" editable="false" width="100%" cellspacing="0">
    <thead> 
        <tr>
            <td sortable="no" class="checkboxheader">
                <input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
                <input type="hidden" name="xml:/DeliveryPlan/@DeliveryPlanKey" />
            </td>
            <td class="tablecolumnheader"><yfc:i18n>Shipment_#</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Status</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Expected_Ship_Date</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Actual_Ship_Date</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Expected_Delivery_Date</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Actual_Delivery_Date</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Origin</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Destination</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Mode</yfc:i18n></td>
        </tr>
    </thead>
    <tbody>
        <yfc:loopXML binding="xml:/Shipments/@Shipment" id="Shipment">
            <tr>
                <yfc:makeXMLInput name="shipmentKey">
					<yfc:makeXMLKey binding="xml:/Shipment/@ShipNode" value="xml:/Shipment/@ShipNode"> </yfc:makeXMLKey>
					<yfc:makeXMLKey binding="xml:/Shipment/@ReceivingNode" value="xml:/Shipment/@ReceivingNode"> </yfc:makeXMLKey>
					<yfc:makeXMLKey binding="xml:/Shipment/@DocumentType" value="xml:/Shipment/@DocumentType"> </yfc:makeXMLKey>
					<yfc:makeXMLKey binding="xml:/Shipment/@ShipmentKey" value="xml:/Shipment/@ShipmentKey"> </yfc:makeXMLKey>
					<yfc:makeXMLKey binding="xml:/Shipment/@ShipmentNo" 
					value="xml:/Shipment/@ShipmentNo"> </yfc:makeXMLKey>
					<yfc:makeXMLKey binding="xml:/Shipment/@OrderNo" 
					value="xml:/Shipment/@OrderNo"> </yfc:makeXMLKey>
					<yfc:makeXMLKey binding="xml:/Shipment/@EnterpriseCode" value="xml:/Shipment/@EnterpriseCode"> 
					</yfc:makeXMLKey>
					<yfc:makeXMLKey binding="xml:/Shipment/@BuyerOrganizationCode" value="xml:/Shipment/@BuyerOrganizationCode"> 
					</yfc:makeXMLKey>
					<yfc:makeXMLKey binding="xml:/Shipment/@SellerOrganizationCode" value="xml:/Shipment/@SellerOrganizationCode">
					</yfc:makeXMLKey>
                </yfc:makeXMLInput>
					<yfc:makeXMLInput name="shipmentPrintKey">
					<yfc:makeXMLKey binding="xml:/Print/Shipment/@ShipmentKey" value="xml:/Shipment/@ShipmentKey" />
					<yfc:makeXMLKey binding="xml:/Print/Shipment/@ShipNode" value="xml:/Shipment/@ShipNode" />
					<yfc:makeXMLKey binding="xml:/Print/Shipment/@SCAC" value="xml:/Shipment/@SCAC" />
					<yfc:makeXMLKey binding="xml:/Print/Shipment/@EnterpriseCode" value="xml:/Shipment/@EnterpriseCode" />
					<yfc:makeXMLKey binding="xml:/Print/Shipment/@BuyerOrganizationCode" value="xml:/Shipment/@BuyerOrganizationCode" />
					<yfc:makeXMLKey binding="xml:/Print/Shipment/@SellerOrganizationCode" value="xml:/Shipment/@SellerOrganizationCode" />
					<yfc:makeXMLKey binding="xml:/Print/Shipment/@PickListNo" value="xml:/Shipment/@PickListNo" />
				</yfc:makeXMLInput>
				<yfc:makeXMLInput name="findReceiptKey" >
					<yfc:makeXMLKey binding="xml:/Receipt/Shipment/@ShipmentKey" value="xml:/Shipment/@ShipmentKey" />
					<yfc:makeXMLKey binding="xml:/Receipt/@DocumentType" value="xml:/Shipment/@DocumentType" />
					<yfc:makeXMLKey binding="xml:/Receipt/@ReceivingNode" value="xml:/Shipment/@ReceivingNode" />
					<yfc:makeXMLKey binding="xml:/Receipt/Shipment/@EnterpriseCode" value="xml:/Shipment/@EnterpriseCode"/>
				</yfc:makeXMLInput>

                <td class="checkboxcolumn"> 
                      <input type="checkbox" value='<%=getParameter("shipmentKey")%>' name="EntityKey" yfcMultiSelectCounter='<%=ShipmentCounter%>' yfcMultiSelectValue1='<%=getValue("Shipment", "xml:/Shipment/@ShipmentKey")%>' yHiddenInputName1='FindReceiptKey_<%=ShipmentCounter%>' PrintEntityKey='<%=getParameter("shipmentPrintKey")%>'/>
					  <input type="hidden" value='<%=getParameter("findReceiptKey")%>' name="FindReceiptKey_<%=ShipmentCounter%>" />
                </td>
                <td class="tablecolumn"><a href="javascript:showDetailFor('<%=getParameter("shipmentKey")%>');">
                    <yfc:getXMLValue binding="xml:/Shipment/@ShipmentNo"/></a>
                </td>
                <td class="tablecolumn">
                    <yfc:getXMLValueI18NDB binding="xml:/Shipment/Status/@Description"/>
					<% 
					 if (equals("Y", getValue("Shipment", "xml:/Shipment/@HoldFlag"))) { %>
                        <img class="icon" onmouseover="this.style.cursor='default'" <%=getImageOptions(YFSUIBackendConsts.HELD_ORDER, "This_shipment_is_held")%>/>
                    <% } %>
				</td>
				<td class="tablecolumn" sortValue="<%=getDateValue("xml:/Shipment/@ExpectedShipmentDate")%>">
							<yfc:getXMLValue binding="xml:/Shipment/@ExpectedShipmentDate"/>
				</td>
				<td class="tablecolumn"  sortValue="<%=getDateValue("xml:/Shipment/@ActualShipmentDate")%>">
							<yfc:getXMLValue binding="xml:/Shipment/@ActualShipmentDate"/>
				</td>
				<td class="tablecolumn" sortValue="<%=getDateValue("xml:/Shipment/@ExpectedDeliveryDate")%>">
							<yfc:getXMLValue binding="xml:/Shipment/@ExpectedDeliveryDate"/>
				</td>
				<td class="tablecolumn" sortValue="<%=getDateValue("xml:/Shipment/@ActualDeliveryDate")%>">
							<yfc:getXMLValue binding="xml:/Shipment/@ActualDeliveryDate"/>
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
							<yfc:i18n><yfc:getXMLValue binding="xml:/Shipment/@ShipMode" /></yfc:i18n>
				</td>
            </tr>
        </yfc:loopXML> 
   </tbody>
</table>
<input type="hidden" name="xml:/Receipt/Shipment/@SCAC"/>
<input type="hidden" name="xml:/Receipt/Shipment/@BolNo"/>
<input type="hidden" name="xml:/Receipt/@DocumentType"/>
<input type="hidden" name="xml:/Receipt/@ReceiptNo"/>
<input type="hidden" name="xml:/Receipt/@ReceivingNode"/>
<input type="hidden" name="xml:/Receipt/Shipment/@ShipNode"/>
<input type="hidden" name="xml:/Receipt/@ReceivingDock"/>
<input type="hidden" name="xml:/Receipt/Shipment/@EnterpriseCode"/>
<input type="hidden" name="xml:/Receipt/@ReceiptDate"/>
<input type="hidden" name="xml:/Receipt/Shipment/@BuyerOrganizationCode"/>
<input type="hidden" name="xml:/Receipt/Shipment/@SellerOrganizationCode"/>
<input type="hidden" name="xml:/Receipt/@NumOfPallets"/>
<input type="hidden" name="xml:/Receipt/@NumOfCartons"/>
