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
            <td class="tablecolumnheader" style="width:25px"  sortable="no"><BR /></td>
            <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/InventoryInformation/Item/InventoryTotals/Demands/Demand/@DemandType")%>">
                <yfc:i18n>Demand_Type</yfc:i18n>
            </td>
			<td class="tablecolumnheader" style="width:<%= getUITableSize("xml:demand:/Demand/@OrganizationCode")%>"><yfc:i18n>Organization_Code</yfc:i18n></td>
            <td class="tablecolumnheader"  style="width:<%= getUITableSize("xml:/InventoryInformation/Item/InventoryTotals/Demands/Demand/@Quantity")%>">
                <yfc:i18n>Quantity</yfc:i18n>
            </td>
        </tr>
    </thead>
    <tbody>
        <%String str = "xml:" + sReqID + ":/InventoryInformation/Item/InventoryTotals/Demands/@Demand";%>
        <yfc:loopXML name="InventoryInformation" binding="<%=str%>" id="demand"  keyName="DemandType"> 
        <tr>
            <td class="tablecolumn">
                <%if(!isVoid(resolveValue("xml:demand:/Demand/@Color"))) {%>
                    <table  border="1" style="border-color:Black" cellspacing="2" cellpadding="2" bgcolor="<yfc:getXMLValue  name="demand" binding="xml:/Demand/@Color" />">
                    <tr>
                        <td style="height:10px;width:15px"></td>
                    </tr>
                    </table>				
                <%}%>
            </td>
            <td class="tablecolumn"><%=getComboText("xml:DemandTypeList:/InventoryDemandTypeList/@InventoryDemandType","Description","DemandType",demandKey,true)%></td>
			<td class="tablecolumn">
							<%if(resolveValue("xml:demand:/Demand/@OrganizationCode").equals("")){%>
								<%=resolveValue("xml:/InventoryInformation/Item/@OrganizationCode")%>
								<%}else%>
								<%=resolveValue("xml:demand:/Demand/@OrganizationCode")%>
			</td>
            <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:demand:/Demand/@Quantity")%>">
                <% if ((getDoubleFromLocalizedString(getLocale(),resolveValue("xml:demand:/Demand/@Quantity"))) > 0  && resolveValue("xml:demand:/Demand/@OrganizationCode").equals(resolveValue("xml:/InventoryInformation/Item/@InventoryOrganization"))) {%>
                    <a href="" onclick="showDemandList('<%=resolveValue("xml:/InventoryInformation/Item/@ItemID")%>', '<%=resolveValue("xml:/InventoryInformation/Item/@UnitOfMeasure")%>', '<%=resolveValue("xml:/InventoryInformation/Item/@ProductClass")%>', '<%=resolveValue("xml:/InventoryInformation/Item/@OrganizationCode")%>', '<%=resolveValue("xml:demand:/Demand/@DemandType")%>', 
					'<%=resolveValue("xml:/InventoryInformation/Item/@ShipNode")%>','',
					'<%=resolveValue("xml:/InventoryInformation/Item/@EndDate")%>',
					'<%=resolveValue("xml:/InventoryInformation/Item/@DistributionRuleId")%>',
					'<%=resolveValue("xml:/InventoryInformation/Item/@ConsiderAllNodes")%>','BETWEEN'
					);return false;">
                        <yfc:getXMLValue  name="demand" binding="xml:demand:/Demand/@Quantity" />
                    </a>
                <%} else {%>
                    <yfc:getXMLValue  name="demand" binding="xml:demand:/Demand/@Quantity" />
                <%}%>
            </td>
        </tr>
        </yfc:loopXML>
    </tbody>
</table>
