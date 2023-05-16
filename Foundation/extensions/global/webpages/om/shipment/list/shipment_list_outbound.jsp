<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/om.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/modificationreason.js"></script>

<%  String destNode ="";%>

<table class="table" editable="false" width="100%" cellspacing="0">
    <thead> 
        <tr>
            <td sortable="no" class="checkboxheader">
                <input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
                <input type="hidden" name="xml:/DeliveryPlan/@DeliveryPlanKey" />
            </td>
            <td class="tablecolumnheader">
				<yfc:i18n>Shipment_#</yfc:i18n>
			</td>
            <td class="tablecolumnheader">
				<yfc:i18n>Status</yfc:i18n>
			</td>
            <td class="tablecolumnheader">
				<yfc:i18n>Buyer</yfc:i18n>
			</td>
			<td class="tablecolumnheader">
				<yfc:i18n>Carrier_Service</yfc:i18n>
			</td>
            <td class="tablecolumnheader">
				<yfc:i18n>Expected_Ship_Date</yfc:i18n>
			</td>
            <td class="tablecolumnheader">
				<yfc:i18n>Origin</yfc:i18n>
			</td>
            <td class="tablecolumnheader">
				<yfc:i18n>Destination</yfc:i18n>
			</td>
            <td class="tablecolumnheader">
				<yfc:i18n>Total_Weight</yfc:i18n>
			</td>
            <td class="tablecolumnheader">
				<yfc:i18n>Total_Volume</yfc:i18n>
			</td>
			<td class="tablecolumnheader" style="width:50">
				<yfc:i18n>Tag_Serial_Requested</yfc:i18n>
			</td>
        </tr>
    </thead>
    <tbody>
		<yfc:loopXML binding="xml:/Shipments/@Shipment" id="Shipment">
            <tr>
                 <yfc:makeXMLInput name="shipmentKey">
					<yfc:makeXMLKey binding="xml:/Shipment/@ShipNode" value="xml:/Shipment/@ShipNode"> </yfc:makeXMLKey>
					<yfc:makeXMLKey binding="xml:/Shipment/@ShipmentKey" value="xml:Shipment/@ShipmentKey"> </yfc:makeXMLKey>
					<yfc:makeXMLKey binding="xml:/Shipment/@ShipmentNo" 
					value="xml:Shipment/@ShipmentNo"> </yfc:makeXMLKey>
					<yfc:makeXMLKey binding="xml:/Shipment/@EnterpriseCode" value="xml:/Shipment/@EnterpriseCode"> 
					</yfc:makeXMLKey>
					<yfc:makeXMLKey binding="xml:/Shipment/@SellerOrganizationCode" value="xml:/Shipment/@SellerOrganizationCode">
					</yfc:makeXMLKey>
					<yfc:makeXMLKey binding="xml:/Shipment/@SCAC" value="xml:/Shipment/@SCAC"> </yfc:makeXMLKey>
                </yfc:makeXMLInput>

				<yfc:makeXMLInput name="shipmentPrintKey">
					<yfc:makeXMLKey binding="xml:/Print/Shipment/@ShipmentKey" value="xml:/Shipment/@ShipmentKey" />
					<yfc:makeXMLKey binding="xml:/Print/Shipment/@ShipNode" value="xml:/Shipment/@ShipNode" />
					<yfc:makeXMLKey binding="xml:/Print/Shipment/@SCAC" value="xml:/Shipment/@SCAC" />
					<yfc:makeXMLKey binding="xml:/Print/Shipment/@EnterpriseCode" value="xml:/Shipment/@EnterpriseCode" />
					<yfc:makeXMLKey binding="xml:/Print/Shipment/@BuyerOrganizationCode" value="xml:/Shipment/@BuyerOrganizationCode" />
					<yfc:makeXMLKey binding="xml:/Print/Shipment/@SellerOrganizationCode" value="xml:/Shipment/@SellerOrganizationCode" />
					<yfc:makeXMLKey binding="xml:/Print/Shipment/@ShipmentType" value="xml:/Shipment/@ShipmentType" />
					<yfc:makeXMLKey binding="xml:/Print/Shipment/@PickListNo" value="xml:/Shipment/@PickListNo" />
					<yfc:makeXMLKey binding="xml:/Print/Shipment/@HazardousMaterialFlag" value="xml:/Shipment/@HazardousMaterialFlag" />
				</yfc:makeXMLInput>
                <td class="checkboxcolumn"> 
                    <input type="checkbox" value='<%=getParameter("shipmentKey")%>' name="EntityKey" yfcMultiSelectCounter='<%=ShipmentCounter%>' yfcMultiSelectValue1='<%=getValue("Shipment", "xml:/Shipment/@ShipmentKey")%>' PrintEntityKey='<%=getParameter("shipmentPrintKey")%>'/>
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
				<td class="tablecolumn">
							<yfc:getXMLValue binding="xml:/Shipment/@BuyerOrganizationCode"/>
				</td>
				<td class="tablecolumn">
							<yfc:getXMLValueI18NDB binding="xml:/Shipment/ScacAndService/@ScacAndServiceDesc"/>
				</td>
				<td class="tablecolumn" sortValue="<%=getDateValue("xml:/Shipment/@ExpectedShipmentDate")%>">
							<yfc:getXMLValue binding="xml:/Shipment/@ExpectedShipmentDate"/>
				</td>
				<td class="tablecolumn">
							<yfc:getXMLValue binding="xml:/Shipment/@ShipNode"/>
				</td>
				<td class="tablecolumn">
				<%	destNode = getValue("Shipment","xml:/Shipment/@ReceivingNode");
					if (!isVoid(destNode)) { %>
						<yfc:getXMLValue binding="xml:/Shipment/@ReceivingNode"/>
					<%} else { %>
						<yfc:getXMLValue binding="xml:/Shipment/ToAddress/@City"/>&nbsp; 
						<yfc:getXMLValue binding="xml:/Shipment/ToAddress/@State"/> &nbsp; 
						<yfc:getXMLValue binding="xml:/Shipment/ToAddress/@Country"/>
					<%}%>
				</td>
				<td class="tablecolumn" sortValue="<%=getNumericValue("xml:Shipment:/Shipment/@TotalWeight")%>">
					<yfc:getXMLValue binding="xml:/Shipment/@TotalWeight"/>&nbsp;
					<yfc:getXMLValue binding="xml:/Shipment/@TotalWeightUOM"/>
				</td>
				<td class="tablecolumn" sortValue="<%=getNumericValue("xml:Shipment:/Shipment/@TotalVolume")%>">
					<yfc:getXMLValue binding="xml:/Shipment/@TotalVolume"/>&nbsp;
					<yfc:getXMLValue binding="xml:/Shipment/@TotalVolumeUOM"/>
				</td>
				<td class="tablecolumn">
					 <yfc:i18n><yfc:getXMLValue binding="xml:/Shipment/@IsTagSerialRequested"/></yfc:i18n>
				</td>
			</tr>
        </yfc:loopXML> 
   </tbody>
   	<input type="hidden" value='<%=userHasOverridePermissions()%>' name="userHasOverridePermissions" />
	<input type="hidden" value='N' name="xml:/Shipment/@OverrideModificationRules" />
	<input type="hidden" name="xml:/Shipment/@DataElementPath" value="xml:/Shipment"/>
	<input type="hidden" name="xml:/Shipment/@ApiName" value="getShipmentDetails"/>
	<input type="hidden" name="xml:/Shipment/@OrderAvailableOnSystem" value=""/>
</table>
