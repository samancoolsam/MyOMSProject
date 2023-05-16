<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/im.js"></script>
<table class="table" border="0" cellspacing="0" width="100%">
<thead>
    <tr> 
        <td class="checkboxheader" sortable="no">
            <input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
        </td>
        <td class="tablecolumnheader" nowrap="true" style="width:<%= getUITableSize("xml:/Item/@ItemID")%>">
            <yfc:i18n>Item_ID</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/PrimaryInformation/@DefaultProductClass")%>">
            <yfc:i18n>PC</yfc:i18n>
        </td>
        <td class="tablecolumnheader"  nowrap="true"  style="width:<%= getUITableSize("xml:/Item/@UnitOfMeasure")%>">
            <yfc:i18n>UOM</yfc:i18n>
        </td>
        <td class="tablecolumnheader">
            <yfc:i18n>Description</yfc:i18n>
        </td>
    </tr>
</thead>
<tbody>
    <yfc:loopXML name="getItemList" binding="xml:/ItemList/@Item" id="Item"  keyName="ItemKey" > 
    <tr> 
        <yfc:makeXMLInput name="ItemKey">
            <yfc:makeXMLKey binding="xml:/InventoryItem/@ItemID" value="xml:/Item/@ItemID" />
            <yfc:makeXMLKey binding="xml:/InventoryItem/@UnitOfMeasure" value="xml:/Item/@UnitOfMeasure" />
            <yfc:makeXMLKey binding="xml:/InventoryItem/@ProductClass" value="xml:/Item/@ProductClass" />
            <yfc:makeXMLKey binding="xml:/InventoryItem/@OrganizationCode" value="xml:ItemList:/ItemList/@OrganizationCode" />
			<% if(isShipNodeUser()) { %>
				<yfc:makeXMLKey binding="xml:/Item/@ShipNode" value="xml:CurrentUser:/User/@Node" />
			<%}%>
        </yfc:makeXMLInput>
        <td class="checkboxcolumn">
            <input type="checkbox" value='<%=getParameter("ItemKey")%>' name="EntityKey"/>
			<input type="hidden" name='ItemID_<%=ItemCounter%>' value='<%=resolveValue("xml:/Item/@ItemID")%>' />
			<input type="hidden" name='UOM_<%=ItemCounter%>' value='<%=resolveValue("xml:/Item/@UnitOfMeasure")%>' />
			<input type="hidden" name='PC_<%=ItemCounter%>' value='<%=resolveValue("xml:/Item/PrimaryInformation/@DefaultProductClass")%>' />
			<input type="hidden" name='OrgCode_<%=ItemCounter%>' value='<%=resolveValue("xml:ItemList:/ItemList/Item/@OrganizationCode")%>' />
        </td>
        <td class="tablecolumn">
            <a onclick="javascript:showDetailFor('<%=getParameter("ItemKey")%>');return false;" href=""><yfc:getXMLValue name="Item" binding="xml:/Item/@ItemID"/></a>
        </td>
        <td class="tablecolumn"><yfc:getXMLValue name="Item" binding="xml:/Item/PrimaryInformation/@DefaultProductClass"/></td>
        <td class="tablecolumn"><yfc:getXMLValue name="Item" binding="xml:/Item/@UnitOfMeasure"/></td>
        <td class="tablecolumn"><yfc:getXMLValue name="Item" binding="xml:/Item/PrimaryInformation/@Description"/></td>
    </tr>
    </yfc:loopXML> 
</tbody>
</table>
