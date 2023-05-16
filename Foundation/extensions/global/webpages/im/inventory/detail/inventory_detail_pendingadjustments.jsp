<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<table width="100%" class="table">
    <thead>
        <tr> 
            <td class="checkboxheader" sortable="no">
                <input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
            </td>
            <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Adjustments/ShipNode/@ShipNode")%>">
                <yfc:i18n>Ship_Node</yfc:i18n>
            </td>
               <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Adjustments/ShipNode/Item/@ItemID")%>">
                <yfc:i18n>Item_ID</yfc:i18n>
            </td>
            <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Adjustments/ShipNode/Item/@ProductClass")%>">
                <yfc:i18n>PC</yfc:i18n>
            </td>
            <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Adjustments/ShipNode/Item/@UnitOfMeasure")%>">
                <yfc:i18n>UOM</yfc:i18n>
            </td>
            <td class="tablecolumnheader">
                <yfc:i18n>Description</yfc:i18n>
            </td>
        </tr>
    </thead>
    <tbody>
        <yfc:loopXML name="Adjustments" binding="xml:/Adjustments/@ShipNode" id="ShipNode">
            <yfc:loopXML binding="xml:/ShipNode/@Item" id="Item">
                <yfc:makeXMLInput name="pendingAdjustmentKey">
                    <yfc:makeXMLKey binding="xml:/PendingAdjustment/@ItemID" value="xml:/Item/@ItemID"/>
                    <yfc:makeXMLKey binding="xml:/PendingAdjustment/@UnitOfMeasure" value="xml:/Item/@UnitOfMeasure"/>
                    <yfc:makeXMLKey binding="xml:/PendingAdjustment/@ProductClass" value="xml:/Item/@ProductClass"/>
                    <yfc:makeXMLKey binding="xml:/PendingAdjustment/@ShipNode" value="xml:/ShipNode/@ShipNode"/>
					<yfc:makeXMLKey binding="xml:/PendingAdjustment/@OrganizationCode" value="xml:/InventoryItem/@OrganizationCode"/>
                </yfc:makeXMLInput>
                <tr>
                    <td class="checkboxcolumn"> 
                        <input type="checkbox" value='<%=getParameter("pendingAdjustmentKey")%>' name="chkEntityKey"/>
                    </td>
                    <td class="tablecolumn">
                        <yfc:getXMLValue binding="xml:/ShipNode/@ShipNode"/>
                    </td>
                    <td class="tablecolumn">
                        <a <%=getDetailHrefOptions("L01", getParameter("pendingAdjustmentKey"), "")%>>
                            <yfc:getXMLValue binding="xml:/Item/@ItemID"/>
                        </a>
                    </td>
                    <td class="tablecolumn">
                        <yfc:getXMLValue binding="xml:/Item/@ProductClass"/>
                    </td>
                    <td class="tablecolumn">
                        <yfc:getXMLValue binding="xml:/Item/@UnitOfMeasure"/>
                    </td>
                    <td class="tablecolumn">
                        <yfc:getXMLValue binding="xml:/Item/@Description"/>
                    </td>
                </tr>
            </yfc:loopXML> 
        </yfc:loopXML> 
    </tbody>
</table>
