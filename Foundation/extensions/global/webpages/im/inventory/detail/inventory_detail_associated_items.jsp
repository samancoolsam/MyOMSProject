<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/im/inventory/detail/inventory_detail_associated_items_include.jspf" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>

<%
    // The first time this screens comes up, it will show associations of type "Substitutions".
    String selectedAssociationType = request.getParameter("associationType");
    if (isVoid(selectedAssociationType)) 
        selectedAssociationType = "Substitutions";

   // All associations are retrieved when this screens comes up. We filter out the other
   // association types other then the selectedAssociationType
   filterAssociationTypes((YFCElement) request.getAttribute("AssociationList"), selectedAssociationType);
%>

<table width="100%" class="view">
    <tr>
        <td class="detaillabel">
            <yfc:i18n>Association_Type</yfc:i18n>
        </td>
        <td>
            <select name="associationType" class="combobox" onchange="yfcChangeDetailView(getCurrentViewId())">
                <yfc:loopOptions binding="xml:ItemAssociationType:/CommonCodeList/@CommonCode" name="CodeShortDescription" value="CodeValue" selected="<%=selectedAssociationType%>" isLocalized="Y"/>
            </select>
        </td>
        <td></td><td></td>
        <td></td><td></td>
    </tr>
</table>
<table width="100%" class="table">
    <thead>
        <tr>
            <td class="checkboxheader" sortable="no">
                <input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
            </td>
            <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/Association/@EffectiveFrom")%>">
                <yfc:i18n>Effective_From</yfc:i18n>
            </td>
            <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/Association/@EffectiveTo")%>">
                <yfc:i18n>Effective_To</yfc:i18n>
            </td>
            <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/Association/Item/@ItemID")%>">
                <yfc:i18n>Item_ID</yfc:i18n>
            </td>
            <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/Association/Item/@ProductClass")%>">
                <yfc:i18n>PC</yfc:i18n>
            </td>
            <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/Association/Item/@UnitOfMeasure")%>">
                <yfc:i18n>UOM</yfc:i18n>
            </td>
            <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/Association/Item/@Description")%>">
                <yfc:i18n>Description</yfc:i18n>
            </td>
            <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/Association/@AssociatedQuantity")%>">
                <yfc:i18n>Quantity</yfc:i18n>
            </td>
        </tr>
    </thead>
    <tbody>
        <yfc:loopXML binding="xml:/AssociationList/@Association" id="Association">
            <yfc:loopXML binding="xml:/Association/@Item" id="Item">
                <yfc:makeXMLInput name="itemKey" >
                    <yfc:makeXMLKey binding="xml:/InventoryItem/@ItemID" value="xml:/Item/@ItemID"/>
                    <yfc:makeXMLKey binding="xml:/InventoryItem/@UnitOfMeasure" value="xml:/Item/@UnitOfMeasure"/>
                    <yfc:makeXMLKey binding="xml:/InventoryItem/@ProductClass" value="xml:/InventoryItem/@ProductClass"/>
                    <yfc:makeXMLKey binding="xml:/InventoryItem/@OrganizationCode" value="xml:/InventoryItem/@OrganizationCode"/>
					<%if(isShipNodeUser()) { %>
						<yfc:makeXMLKey binding="xml:/InventoryItem/@ShipNode" value="xml:CurrentUser:/User/@Node"/>
					<%}%>
                </yfc:makeXMLInput>
                <tr>
                    <td class="checkboxcolumn"> 
                        <input type="checkbox" value='<%=getParameter("itemKey")%>' name="chkEntityKey"/>
                    </td>
                    <td class="tablecolumn" sortValue="<%=getDateValue("xml:/Association/@EffectiveFrom")%>">
                        <yfc:getXMLValue binding="xml:/Association/@EffectiveFrom"/>
                    </td>
                    <td class="tablecolumn" sortValue="<%=getDateValue("xml:/Association/@EffectiveTo")%>">
                        <yfc:getXMLValue binding="xml:/Association/@EffectiveTo"/>
                    </td>
                    <td class="tablecolumn">
                        <a <%=getDetailHrefOptions("L01", getParameter("itemKey"), "")%>>
                            <yfc:getXMLValue binding="xml:/Item/@ItemID"/>
                        </a>
                    </td>
                    <td class="tablecolumn">
                        <%//Set the current item's PC in the item association list%>
                        <%=resolveValue("xml:/InventoryItem/@ProductClass")%>
                    </td>
                    <td class="tablecolumn">
                        <yfc:getXMLValue binding="xml:/Item/@UnitOfMeasure"/>
                    </td>
                    <td class="tablecolumn">
                        <yfc:getXMLValue binding="xml:/Item/PrimaryInformation/@Description"/>
                    </td>
                    <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:/Association/@AssociatedQuantity")%>">
                        <yfc:getXMLValue binding="xml:/Association/@AssociatedQuantity"/>
                    </td>
                </tr>
            </yfc:loopXML>
        </yfc:loopXML>
    </tbody>
</table>
