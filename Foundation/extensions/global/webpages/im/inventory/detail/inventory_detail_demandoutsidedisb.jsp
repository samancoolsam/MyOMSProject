<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%
    String sReqID = getParameter("requestID");
    String sDemandOutsideDisbTitle = getParameter("Title");
%>
<table width="100%" class="table" suppressFooter="true" cellspacing="0" cellpadding="0">
<thead>
    <tr>
        <td class="secondaryinnerpaneltitle" colspan="4" sortable="no"><%=sDemandOutsideDisbTitle%></td>
    </tr>
    <tr>
        <td class="tablecolumnheader" style="width:25px" sortable="no"><BR /></td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/InventoryInformation/Item/Description")%>">
            <yfc:i18n>Description</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/InventoryInformation/Item/InventoryTotals/Demands/Demand/@Quantity")%>">
            <yfc:i18n>Quantity</yfc:i18n>
        </td>
    </tr>
</thead>
<tbody>
	<%
	   String str = "xml:" + sReqID + ":/InventoryInformation/Item/InventoryTotals/Demands/@Demand";
       String sTotalInternalDemandOutsideDisb = resolveValue("xml:/InventoryInformation/Item/InventoryTotals/Demands/@TotalInternalOutsideDistribution");
       String sTotalExternalDemandOutsideDisb = resolveValue("xml:/InventoryInformation/Item/InventoryTotals/Demands/@TotalExternalOutsideDistribution");
	%>
	<tr>
		<td class="tablecolumn"  sortable="no">
            <% if ((getDoubleFromLocalizedString(getLocale(),sTotalInternalDemandOutsideDisb)) > 0) {%> 
                <table  border="1" style="border-color:Black" cellspacing="2" cellpadding="2" bgcolor="<yfc:getXMLValue 				binding="xml:/InventoryInformation/Item/InventoryTotals/Demands/@TotalInternalOutsideDistribution" />">
                <tr>
                    <td style="height:10px;width:15px"></td>
                </tr>
                </table>				
            <%}%>
        </td>
        <td class="tablecolumn"><yfc:i18n>Internal</yfc:i18n></td>
        <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:/InventoryInformation/item/InventoryTotals/Demands/@TotalInternalOutsideDistribution")%>">
			<%=sTotalInternalDemandOutsideDisb%>
            <% if ((getDoubleFromLocalizedString(getLocale(),sTotalInternalDemandOutsideDisb)) > 0) {%> 
	           <img onclick="showDIV('divInternalDemandOutsideDisb','<%=getI18N("Click_To_Expand")%>','<%=getI18N("Click_To_Collapse")%>','<%=YFSUIBackendConsts.FOLDER_COLLAPSE%>','<%=YFSUIBackendConsts.FOLDER_EXPAND%>')" <%=getImageOptions(YFSUIBackendConsts.FOLDER,"Click_to_Expand")%> />
	        <%}%>
		</td>
    </tr>
	<!-- Don't remove the blank <TR> element below.This will messup the row color for the DIV inside the next <TR> element -->
	<tr style="display:none"></tr>
	<tr style="display:none">
		<td></td>
		<td colspan="3">
			<div id="divInternalDemandOutsideDisb" style="display:none;padding-top:5px">
                <table width="100%"  class="simpletable" cellspacing="0"  cellpadding="0">
                <thead>
                    <tr>
                        <td class="tablecolumnheadernosort" style="width:<%= getUITableSize("xml:/Demand/@DemandType")%>"><yfc:i18n>Demand_Type</yfc:i18n></td>
						<td class="tablecolumnheadernosort" style="width:<%= getUITableSize("xml:/Demand/@DemandDate")%>"><yfc:i18n>Demand_Date</yfc:i18n></td>
                        <td class="tablecolumnheadernosort" style="width:<%= getUITableSize("xml:/Demand/@Quantity")%>"><yfc:i18n>Quantity</yfc:i18n></td>
                    </tr>
                </thead>
                <tbody>
                   <yfc:loopXML binding="xml:/InventoryInformation/Item/InventoryTotals/Demands/@Demand" id="demandintnoutsidedisb"> 
				   <% if(equals(resolveValue("xml:demandintnoutsidedisb:/Demand/@DemandClass"),"3")) {%>
                    <tr>
                        <yfc:makeXMLInput name="DemandTypeKey">
                            <yfc:makeXMLKey binding="xml:/InventoryDemand/@ItemID" value="xml:/InventoryInformation/Item/@ItemID" />
                            <yfc:makeXMLKey binding="xml:/InventoryDemand/@UnitOfMeasure" value="xml:/InventoryInformation/Item/@UnitOfMeasure" />
                            <yfc:makeXMLKey binding="xml:/InventoryDemand/@ProductClass" value="xml:/InventoryInformation/Item/@ProductClass" />
                            <yfc:makeXMLKey binding="xml:/InventoryDemand/@OrganizationCode" value="xml:/InventoryInformation/Item/@OrganizationCode" />
                            <yfc:makeXMLKey binding="xml:/InventoryDemand/@DemandType" value="xml:demandintnoutsidedisb:/Demand/@DemandType" />
                            <yfc:makeXMLKey binding="xml:/InventoryDemand/@ShipNode" value="xml:/InventoryInformation/Item/@ShipNode" />
                            <yfc:makeXMLKey binding="xml:/InventoryDemand/@EndDate" value="xml:/InventoryInformation/Item/@EndDate" />
                        </yfc:makeXMLInput>
                        <td class="tablecolumn"><yfc:getXMLValue name="demandintnoutsidedisb" binding="xml:/Demand/@DemandType" /></td>
                        <td class="tablecolumn"><yfc:getXMLValue name="demandintnoutsidedisb" binding="xml:/Demand/@DemandDate" /></td>
                        <td class="numerictablecolumn">
                            <a <%=getDetailHrefOptions("L01",getParameter("DemandTypeKey"),"")%>>
                                <yfc:getXMLValue name="demandintnoutsidedisb" binding="xml:/Demand/@Quantity" />
                            </a>
                        </td>
                    </tr>
					<%}%>
                    </yfc:loopXML>
                </tbody>
                </table>
            </div>
		</td>
	</tr>
	<tr>
		<td class="tablecolumn"  sortable="no">
            <% if ((getDoubleFromLocalizedString(getLocale(),sTotalExternalDemandOutsideDisb)) > 0) {%> 
                <table  border="1" style="border-color:Black" cellspacing="2" cellpadding="2" bgcolor="<yfc:getXMLValue binding="xml:/InventoryInformation/Item/InventoryTotals/Demands/@TotalExternalOutsideDistribution" />">
                <tr>
                    <td style="height:10px;width:15px"></td>
                </tr>
                </table>				
            <%}%>
        </td>
        <td class="tablecolumn"><yfc:i18n>External</yfc:i18n></td>
        <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:/InventoryInformation/item/InventoryTotals/Demands/@TotalExternalOutsideDistribution")%>">
			<%=sTotalExternalDemandOutsideDisb%>
            <% if ((getDoubleFromLocalizedString(getLocale(),sTotalExternalDemandOutsideDisb)) > 0) {%> 
	           <img onclick="showDIV('divExternalDemandOutsideDisb','<%=getI18N("Click_To_Expand")%>','<%=getI18N("Click_To_Collapse")%>','<%=YFSUIBackendConsts.FOLDER_COLLAPSE%>','<%=YFSUIBackendConsts.FOLDER_EXPAND%>')" <%=getImageOptions(YFSUIBackendConsts.FOLDER,"Click_to_Expand")%> />
			<%}%>
		</td>
    </tr>
	<!-- Don't remove the blank <TR> element below.This will messup the row color for the DIV inside the next <TR> element -->
	<tr style="display:none"></tr>
	<tr style="display:none">
		<td></td>
		<td colspan="3">
			<div id="divExternalDemandOutsideDisb" style="display:none;padding-top:5px">
                <table width="100%"  class="simpletable" cellspacing="0"  cellpadding="0">
                <thead>
                    <tr>
                        <td class="tablecolumnheadernosort" style="width:<%= getUITableSize("xml:/Demand/@DemandType")%>"><yfc:i18n>Demand_Type</yfc:i18n></td>
						<td class="tablecolumnheadernosort" style="width:<%= getUITableSize("xml:/Demand/@DemandDate")%>"><yfc:i18n>Demand_Date</yfc:i18n></td>
                        <td class="tablecolumnheadernosort" style="width:<%= getUITableSize("xml:/Demand/@Quantity")%>"><yfc:i18n>Quantity</yfc:i18n></td>
                    </tr>
                </thead>
                <tbody>
                   <yfc:loopXML binding="xml:/InventoryInformation/Item/InventoryTotals/Demands/@Demand" id="demandintnoutsidedisb"> 
				   <% if(equals(resolveValue("xml:demandintnoutsidedisb:/Demand/@DemandClass"),"4")) {%>
                    <tr>
                        <yfc:makeXMLInput name="DemandTypeKey">
                            <yfc:makeXMLKey binding="xml:/InventoryDemand/@ItemID" value="xml:/InventoryInformation/Item/@ItemID" />
                            <yfc:makeXMLKey binding="xml:/InventoryDemand/@UnitOfMeasure" value="xml:/InventoryInformation/Item/@UnitOfMeasure" />
                            <yfc:makeXMLKey binding="xml:/InventoryDemand/@ProductClass" value="xml:/InventoryInformation/Item/@ProductClass" />
                            <yfc:makeXMLKey binding="xml:/InventoryDemand/@OrganizationCode" value="xml:/InventoryInformation/Item/@OrganizationCode" />
                            <yfc:makeXMLKey binding="xml:/InventoryDemand/@DemandType" value="xml:demandintnoutsidedisb:/Demand/@DemandType" />
                            <yfc:makeXMLKey binding="xml:/InventoryDemand/@ShipNode" value="xml:/InventoryInformation/Item/@ShipNode" />
                            <yfc:makeXMLKey binding="xml:/InventoryDemand/@EndDate" value="xml:/InventoryInformation/Item/@EndDate" />
                        </yfc:makeXMLInput>
                        <td class="tablecolumn"><yfc:getXMLValue name="demandintnoutsidedisb" binding="xml:/Demand/@DemandType" /></td>
						<td class="tablecolumn"><yfc:getXMLValue name="demandintnoutsidedisb" binding="xml:/Demand/@DemandDate" /></td>
                        <td class="numerictablecolumn">
                            <a <%=getDetailHrefOptions("L01",getParameter("DemandTypeKey"),"")%>>
                                <yfc:getXMLValue name="demandintnoutsidedisb" binding="xml:/Demand/@Quantity" />
                            </a>
                        </td>
                    </tr>
					<%}%>
                    </yfc:loopXML>
                </tbody>
                </table>
            </div>
		</td>
	</tr>
</tbody>
</table>
