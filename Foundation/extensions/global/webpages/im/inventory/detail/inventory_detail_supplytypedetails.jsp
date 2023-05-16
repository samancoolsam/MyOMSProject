<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%
    String sReqID = getParameter("requestID");
%>
<table width="100%" class="table" suppressFooter="true">
<thead>
    <tr>
        <td class="tablecolumnheader" style="width:25px" sortable="no"><BR /></td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/InventoryInformation/Item/InventoryTotals/Supplies/Supply/@SupplyType")%>">
            <yfc:i18n>Supply_Type</yfc:i18n>
        </td>
		<td class="tablecolumnheader" style="width:<%= getUITableSize("xml:supply:/Supply/@OrganizationCode")%>"><yfc:i18n>Organization_Code</yfc:i18n></td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/InventoryInformation/Item/InventoryTotals/Supplies/Supply/@Quantity")%>">
            <yfc:i18n>Quantity</yfc:i18n>
        </td>
    </tr>
</thead>
<tbody>
	<%String str = "xml:" + sReqID + ":/InventoryInformation/Item/InventoryTotals/Supplies/@Supply";%>
    <yfc:loopXML name="InventoryInformation" binding="<%=str%>" id="supply"  keyName="SupplyType"> 
	<tr>
        <yfc:makeXMLInput name="supplyTypeKey" >
            <yfc:makeXMLKey binding="xml:/InventorySupply/@ItemID" value="xml:/InventoryInformation/Item/@ItemID" />
            <yfc:makeXMLKey binding="xml:/InventorySupply/@UnitOfMeasure" value="xml:/InventoryInformation/Item/@UnitOfMeasure" />
            <yfc:makeXMLKey binding="xml:/InventorySupply/@ProductClass" value="xml:/InventoryInformation/Item/@ProductClass" />
            <yfc:makeXMLKey binding="xml:/InventorySupply/@OrganizationCode" value="xml:/InventoryInformation/Item/@OrganizationCode" />
            <yfc:makeXMLKey binding="xml:/InventorySupply/@SupplyType" value="xml:supply:/Supply/@SupplyType" />
            <yfc:makeXMLKey binding="xml:/InventorySupply/@EndDate" value="xml:/InventoryInformation/Item/@EndDate" />
            <yfc:makeXMLKey binding="xml:/InventorySupply/@ShipNode" value="xml:/InventoryInformation/Item/@ShipNode" />
            <yfc:makeXMLKey binding="xml:/InventorySupply/@DistributionRuleId" value="xml:/InventoryInformation/Item/@DistributionRuleId"/>
            <yfc:makeXMLKey binding="xml:/InventorySupply/@ConsiderAllNodes" value="xml:/InventoryInformation/Item/@ConsiderAllNodes"/>
            <yfc:makeXMLKey binding="xml:/InventorySupply/@ConsiderAllSegments" value="xml:/InventoryInformation/Item/@ConsiderAllSegments"/>
            <yfc:makeXMLKey binding="xml:/InventorySupply/@SegmentType" value="xml:/InventoryInformation/Item/@SegmentType"/>
            <yfc:makeXMLKey binding="xml:/InventorySupply/@Segment" value="xml:/InventoryInformation/Item/@Segment"/>
        </yfc:makeXMLInput>
        <td class="tablecolumn">
            <% if(!isVoid(resolveValue("xml:supply:/Supply/@Color"))){%>
                <table  border="1" style="border-color:Black" cellspacing="2" cellpadding="2" bgcolor="<yfc:getXMLValue  name="supply" binding="xml:supply:/Supply/@Color" />">
                <tr>
                    <td style="height:10px;width:15px"></td>
                </tr>
                </table>				
            <%}%>
        </td>
        <td class="tablecolumn"><%=getComboText("xml:SupplyTypeList:/InventorySupplyTypeList/@InventorySupplyType","Description","SupplyType",supplyKey,true)%></td>
		<td class="tablecolumn">
						<%if(resolveValue("xml:supply:/Supply/@OrganizationCode").equals("")){%>
						<%=resolveValue("xml:/InventoryInformation/Item/@OrganizationCode")%>
						<%}else%>
						<%=resolveValue("xml:supply:/Supply/@OrganizationCode")%>
		</td>
        <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:supply:/Supply/@Quantity")%>">
            <% if ((getDoubleFromLocalizedString(getLocale(),resolveValue("xml:supply:/Supply/@Quantity"))) > 0  && resolveValue("xml:supply:/Supply/@OrganizationCode").equals(resolveValue("xml:/InventoryInformation/Item/@InventoryOrganization"))) {%>
                <a <%=getDetailHrefOptions("L01",getParameter("supplyTypeKey"),"")%>>
                    <yfc:getXMLValue  name="supply" binding="xml:supply:/Supply/@Quantity" />
                </a>
            <%} else {%>
               <yfc:getXMLValue  name="supply" binding="xml:supply:/Supply/@Quantity" />
            <%}%>
        </td>
    </tr>
    </yfc:loopXML>
</tbody>
</table>
