<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%
    String sReqID = getParameter("requestID");
    String sSupplyWithinDisbTitle = getParameter("Title");
%>
<table width="100%" class="table" suppressFooter="true" cellspacing="0" cellpadding="0">
<thead>
    <tr>
        <td class="secondaryinnerpaneltitle" colspan="4" sortable="no"><%=sSupplyWithinDisbTitle%></td>
    </tr>
    <tr>
        <td class="tablecolumnheader" style="width:25px" sortable="no"><BR /></td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/InventoryInformation/Item/Description")%>">
            <yfc:i18n>Description</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/InventoryInformation/Item/InventoryTotals/Supplies/Supply/@Quantity")%>">
            <yfc:i18n>Quantity</yfc:i18n>
        </td>
    </tr>
</thead>
<tbody>
	<%
	   String str = "xml:" + sReqID + ":/InventoryInformation/Item/InventoryTotals/Supplies/@Supply";
       String sTotalInternalSupplyWithinDisb = resolveValue("xml:/InventoryInformation/Item/InventoryTotals/Supplies/@TotalInternalWithinDistribution");
       String sTotalExternalSupplyWithinDisb = resolveValue("xml:/InventoryInformation/Item/InventoryTotals/Supplies/@TotalExternalWithinDistribution");
	%>
	<tr>
		<td class="tablecolumn"  sortable="no">
            <% if ((getDoubleFromLocalizedString(getLocale(),sTotalInternalSupplyWithinDisb)) > 0) {%> 
                <table  border="1" style="border-color:Black" cellspacing="2" cellpadding="2" bgcolor="<yfc:getXMLValue  name="supply" binding="xml:supply:/Supply/@Color" />">
                <tr>
                    <td style="height:10px;width:15px"></td>
                </tr>
                </table>				
            <%}%>
        </td>
        <td class="tablecolumn"><yfc:i18n>Internal</yfc:i18n></td>
        <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:/InventoryInformation/item/InventoryTotals/Supplies/@TotalInternalWithinDistribution")%>">
			<%=sTotalInternalSupplyWithinDisb%>
            <% if ((getDoubleFromLocalizedString(getLocale(),sTotalInternalSupplyWithinDisb)) > 0) {%> 
	           <img onclick="showDIV('divInternalSupplyWithinDisb','<%=getI18N("Click_To_Expand")%>','<%=getI18N("Click_To_Collapse")%>','<%=YFSUIBackendConsts.FOLDER_COLLAPSE%>','<%=YFSUIBackendConsts.FOLDER_EXPAND%>')" <%=getImageOptions(YFSUIBackendConsts.FOLDER,"Click_to_Expand")%> />
	        <%}%>
		</td>
    </tr>
	<!-- Don't remove the blank <TR> element below.This will messup the row color for the DIV inside the next <TR> element -->
	<tr style="display:none"></tr>
	<tr style="display:none">
		<td></td>
		<td colspan="3">
			<div id="divInternalSupplyWithinDisb" style="display:none;padding-top:5px">
                <table width="100%"  class="simpletable" cellspacing="0"  cellpadding="0">
                <thead>
                    <tr>
                        <td class="tablecolumnheadernosort" style="width:<%= getUITableSize("xml:/Supply/@SupplyType")%>"><yfc:i18n>Supply_Type</yfc:i18n></td>
                        <td class="tablecolumnheadernosort" style="width:<%= getUITableSize("xml:/Supply/@Quantity")%>"><yfc:i18n>Quantity</yfc:i18n></td>
                    </tr>
                </thead>
                <tbody>
                   <yfc:loopXML binding="xml:/InventoryInformation/Item/InventoryTotals/Supplies/@Supply" id="supplyintnwithindisb"> 
				   <% if(equals(resolveValue("xml:supplyintnwithindisb:/Supply/@SupplyClass"),"1")) {%>
                    <tr>
                        <yfc:makeXMLInput name="SupplyTypeKey">
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@ItemID" value="xml:/InventoryInformation/Item/@ItemID" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@UnitOfMeasure" value="xml:/InventoryInformation/Item/@UnitOfMeasure" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@ProductClass" value="xml:/InventoryInformation/Item/@ProductClass" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@OrganizationCode" value="xml:/InventoryInformation/Item/@OrganizationCode" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@SupplyType" value="xml:supplyintnwithindisb:/Supply/@SupplyType" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@ShipNode" value="xml:/InventoryInformation/Item/@ShipNode" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@EndDate" value="xml:/InventoryInformation/Item/@EndDate" />
                        </yfc:makeXMLInput>
                        <td class="tablecolumn"><yfc:getXMLValue name="supplyintnwithindisb" binding="xml:/Supply/@SupplyType" /></td>
                        <td class="numerictablecolumn">
                            <a <%=getDetailHrefOptions("L01",getParameter("SupplyTypeKey"),"")%>>
                                <yfc:getXMLValue name="supplyintnwithindisb" binding="xml:/Supply/@Quantity" />
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
            <% if ((getDoubleFromLocalizedString(getLocale(),sTotalExternalSupplyWithinDisb)) > 0) {%> 
                <table  border="1" style="border-color:Black" cellspacing="2" cellpadding="2" bgcolor="<yfc:getXMLValue  name="supply" binding="xml:supply:/Supply/@Color" />">
                <tr>
                    <td style="height:10px;width:15px"></td>
                </tr>
                </table>				
            <%}%>
        </td>
        <td class="tablecolumn"><yfc:i18n>External</yfc:i18n></td>
        <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:/InventoryInformation/item/InventoryTotals/Supplies/@TotalExternalWithinDistribution")%>">
			<%=sTotalExternalSupplyWithinDisb%>
            <% if ((getDoubleFromLocalizedString(getLocale(),sTotalExternalSupplyWithinDisb)) > 0) {%> 
	           <img onclick="showDIV('divExternalSupplyWithinDisb','<%=getI18N("Click_To_Expand")%>','<%=getI18N("Click_To_Collapse")%>','<%=YFSUIBackendConsts.FOLDER_COLLAPSE%>','<%=YFSUIBackendConsts.FOLDER_EXPAND%>')" <%=getImageOptions(YFSUIBackendConsts.FOLDER,"Click_to_Expand")%> />
			<%}%>
		</td>
    </tr>
	<!-- Don't remove the blank <TR> element below.This will messup the row color for the DIV inside the next <TR> element -->
	<tr style="display:none"></tr>
	<tr style="display:none">
		<td></td>
		<td colspan="3">
			<div id="divExternalSupplyWithinDisb" style="display:none;padding-top:5px">
                <table width="100%"  class="simpletable" cellspacing="0"  cellpadding="0">
                <thead>
                    <tr>
                        <td class="tablecolumnheadernosort" style="width:<%= getUITableSize("xml:/Supply/@SupplyType")%>"><yfc:i18n>Supply_Type</yfc:i18n></td>
                        <td class="tablecolumnheadernosort" style="width:<%= getUITableSize("xml:/Supply/@Quantity")%>"><yfc:i18n>Quantity</yfc:i18n></td>
                    </tr>
                </thead>
                <tbody>
                   <yfc:loopXML binding="xml:/InventoryInformation/Item/InventoryTotals/Supplies/@Supply" id="supplyintnwithindisb"> 
				   <% if(equals(resolveValue("xml:supplyintnwithindisb:/Supply/@SupplyClass"),"2")) {%>
                    <tr>
                        <yfc:makeXMLInput name="SupplyTypeKey">
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@ItemID" value="xml:/InventoryInformation/Item/@ItemID" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@UnitOfMeasure" value="xml:/InventoryInformation/Item/@UnitOfMeasure" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@ProductClass" value="xml:/InventoryInformation/Item/@ProductClass" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@OrganizationCode" value="xml:/InventoryInformation/Item/@OrganizationCode" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@SupplyType" value="xml:supplyintnwithindisb:/Supply/@SupplyType" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@ShipNode" value="xml:/InventoryInformation/Item/@ShipNode" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@EndDate" value="xml:/InventoryInformation/Item/@EndDate" />
                        </yfc:makeXMLInput>
                        <td class="tablecolumn"><yfc:getXMLValue name="supplyintnwithindisb" binding="xml:/Supply/@SupplyType" /></td>
                        <td class="numerictablecolumn">
                            <a <%=getDetailHrefOptions("L01",getParameter("SupplyTypeKey"),"")%>>
                                <yfc:getXMLValue name="supplyintnwithindisb" binding="xml:/Supply/@Quantity" />
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
