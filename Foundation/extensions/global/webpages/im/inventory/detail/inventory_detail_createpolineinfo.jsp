<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/orderentry.jspf"%>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>

<%
    String sItemID = resolveValue("xml:/InventoryItem/@ItemID");
    String sShipNode = resolveValue("xml:/InventoryItem/@ShipNode");
	String extraParams = getExtraParamsForTargetBinding("xml:/Item/@CallingOrganizationCode", getValue("Order", "xml:/InventoryItem/@OrganizationCode"));
%>

<table class="view" width="100%">
    <tr>
        <td class="detaillabel"><yfc:i18n>Item_ID</yfc:i18n></td>
        <% if (isVoid(sItemID)) {%>
        <td>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/OrderLines/OrderLine/Item/@ItemID")%>/>
                <img class="lookupicon" onclick="callItemLookup('xml:/Order/OrderLines/OrderLine/Item/@ItemID','xml:/Order/OrderLines/OrderLine/Item/@ProductClass','xml:/Order/OrderLines/OrderLine/Item/@UnitOfMeasure','item','<%=extraParams%>')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Item") %> />
            </td>
            <% } else { %>
            <td class="protectedtext">
                <%=sItemID%>
                <input type="hidden" name="xml:/Order/OrderLines/OrderLine/Item/@ItemID" value="<%=sItemID%>"/>
            </td>
        <%}%>
        <td class="detaillabel"><yfc:i18n>Product_Class</yfc:i18n></td>
        <% if (!isVoid(sItemID)) {%>
            <td class="protectedtext">
                <yfc:getXMLValue name="InventoryItem" binding="xml:/InventoryItem/@ProductClass"/>
                <input type="hidden" class="protectedinput" <%=getTextOptions("xml:/Order/OrderLines/OrderLine/Item/@ProductClass","xml:/InventoryItem/@ProductClass")%> />
            </td>
        <%}else {%>
            <td>
                <select name="xml:/Order/OrderLines/OrderLine/Item/@ProductClass" class="combobox" >
                    <yfc:loopOptions binding="xml:ProductClass:/CommonCodeList/@CommonCode" 
                        name="CodeValue" value="CodeValue" selected="xml:/Order/OrderLines/OrderLine/Item/@ProductClass"/>
                </select>
            </td>        
        <% } %>
    </tr>
    <tr>
        <td class="detaillabel" ><yfc:i18n>Unit_Of_Measure</yfc:i18n></td>
        <% if (!isVoid(sItemID)) { %>
            <td class="protectedtext">
                <yfc:getXMLValue name="InventoryItem" binding="xml:/InventoryItem/@UnitOfMeasure"/>
                <input type="hidden" class="protectedinput" <%=getTextOptions("xml:/Order/OrderLines/OrderLine/Item/@UnitOfMeasure","xml:/InventoryItem/@UnitOfMeasure")%> />
            </td>
        <% } else { %>
            <td>
                <select name="xml:/Order/OrderLines/OrderLine/Item/@UnitOfMeasure" class="combobox" >
                    <yfc:loopOptions binding="xml:UnitOfMeasure:/ItemUOMMasterList/@ItemUOMMaster" 
                        name="UnitOfMeasure" value="UnitOfMeasure" selected="xml:/Order/OrderLines/OrderLine/Item/@UnitOfMeasure"/>
                </select>
            </td>
        <% } %>
        <td class="detaillabel"><yfc:i18n>Quantity</yfc:i18n></td>
        <td>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/OrderLines/OrderLine/@OrderedQty")%>/>
        </td>
    </tr>
    <tr>
        <td class="detaillabel"><yfc:i18n>Receiving_Node</yfc:i18n></td>
        <td>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/OrderLines/OrderLine/@ReceivingNode",sShipNode)%>/>
            <img class="lookupicon" onclick="callLookup(this,'shipnode')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Receiving_Node") %> />
        </td>
        <td class="detaillabel"><yfc:i18n>Ship_Node</yfc:i18n></td>
        <td>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/OrderLines/OrderLine/@ShipNode")%>/>
            <img class="lookupicon" onclick="callLookup(this,'shipnode')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Ship_Node") %> />
        </td>
    </tr>
    <tr>
        <td class="detaillabel"><yfc:i18n>Requested_Ship_Date</yfc:i18n></td>
        <td>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/OrderLines/OrderLine/@ReqShipDate")%>/>
            <img class="lookupicon" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar")%>/>
        </td>
    </tr>
</table>
