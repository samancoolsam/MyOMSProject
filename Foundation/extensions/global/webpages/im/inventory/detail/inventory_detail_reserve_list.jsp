 <%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Order Management  (5725-D10)
IBM Sterling Configure Price Quote (5725-D11)
(C) Copyright IBM Corp. 2005, 2014 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<table class="table" width="100%">
<thead>
    <tr> 
        <td class="checkboxheader" sortable="no">
            <input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ItemReservation/@ReservationID")%>">
            <yfc:i18n>Reservation_ID</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ItemReservation/@ReservationID")%>">
            <yfc:i18n>Ship_Node</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ItemReservation/@ShipDate")%>">
            <yfc:i18n>Ship_Date</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ItemReservation/@ReservationDate")%>">
            <yfc:i18n>Reservation_Date</yfc:i18n>
        </td>
		<td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ItemReservation/@ReservationQuantity")%>">
            <yfc:i18n>Quantity</yfc:i18n>
        </td>

    </tr>
</thead>
<tbody>
    <yfc:loopXML name="Item" binding="xml:/Item/ItemReservations/@ItemReservation" id="ItemReservation"  keyName="ReservationID" > 
        <yfc:makeXMLInput name="itemReservationKey" >
            <yfc:makeXMLKey binding="xml:/InventoryItem/@ItemID" value="xml:/InventoryItem/@ItemID" />
            <yfc:makeXMLKey binding="xml:/InventoryItem/@UnitOfMeasure" value="xml:/InventoryItem/@UnitOfMeasure" />
            <yfc:makeXMLKey binding="xml:/InventoryItem/@ProductClass" value="xml:/InventoryItem/@ProductClass" />
            <yfc:makeXMLKey binding="xml:/InventoryItem/@OrganizationCode" value="xml:/InventoryItem/@OrganizationCode" />
            <yfc:makeXMLKey binding="xml:/InventoryItem/@ReservationID" value="xml:ItemReservation:/ItemReservation/@ReservationID" />
            <yfc:makeXMLKey binding="xml:/InventoryItem/@ShipNode" value="xml:ItemReservation:/ItemReservation/@ShipNode" />
            <yfc:makeXMLKey binding="xml:/InventoryItem/@DemandType" value="xml:ItemReservation:/ItemReservation/@DemandType" />
            <yfc:makeXMLKey binding="xml:/InventoryItem/@QtyToBeCancelled" value="xml:ItemReservation:/ItemReservation/@ReservationQuantity" />
            <yfc:makeXMLKey binding="xml:/InventoryItem/@ShipDate" value="xml:ItemReservation:/ItemReservation/@ShipDate" />
			<yfc:makeXMLKey binding="xml:/InventoryItem/@TagNumber" value="xml:ItemReservation:/ItemReservation/@TagNumber" />
        </yfc:makeXMLInput>
		<yfc:makeXMLInput name="modifyReservationKey" >
            <yfc:makeXMLKey binding="xml:/ReserveItemInventory/@ItemID" value="xml:/InventoryItem/@ItemID" />
            <yfc:makeXMLKey binding="xml:/ReserveItemInventory/@UnitOfMeasure" value="xml:/InventoryItem/@UnitOfMeasure" />
            <yfc:makeXMLKey binding="xml:/ReserveItemInventory/@ProductClass" value="xml:/InventoryItem/@ProductClass" />
            <yfc:makeXMLKey binding="xml:/ReserveItemInventory/@OrganizationCode" value="xml:/InventoryItem/@OrganizationCode" />
            <yfc:makeXMLKey binding="xml:/ReserveItemInventory/@ReservationID" value="xml:/ItemReservation/@ReservationID" />
            <yfc:makeXMLKey binding="xml:/ReserveItemInventory/@ShipNode" value="xml:ItemReservation:/ItemReservation/@ShipNode" />
            <yfc:makeXMLKey binding="xml:/ReserveItemInventory/@DemandType" value="xml:ItemReservation:/ItemReservation/@DemandType" />
            <yfc:makeXMLKey binding="xml:/ReserveItemInventory/@ShipDate" value="xml:/ItemReservation/@ShipDate" />
        </yfc:makeXMLInput>
    <tr>
        <td class="checkboxcolumn"> 
            <input type="checkbox" value='<%=getParameter("itemReservationKey")%>' name="chkEntityKey"/>
        </td>
        <td class="tablecolumn">
            <a <%=getDetailHrefOptions("L01",getParameter("modifyReservationKey"),"")%> >
                <yfc:getXMLValue name="ItemReservation" binding="xml:/ItemReservation/@ReservationID"/>
            </a>
        </td>
        <td class="tablecolumn">
            <yfc:getXMLValue name="ItemReservation" binding="xml:/ItemReservation/@ShipNode"/>
        </td>
        <td class="tablecolumn"  sortValue="<%=getDateValue("xml:/ItemReservation/@ShipDate")%>">
            <yfc:getXMLValue name="ItemReservation" binding="xml:/ItemReservation/@ShipDate"/>
        </td>
        <td class="tablecolumn" sortValue="<%=getDateValue("xml:/ItemReservation/@ReservationDate")%>">
            <yfc:getXMLValue name="ItemReservation" binding="xml:/ItemReservation/@ReservationDate"/>
        </td>
		<td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:/ItemReservation/@ReservationQuantity")%>">
            <yfc:getXMLValue name="ItemReservation" binding="xml:/ItemReservation/@ReservationQuantity"/>
        </td>
    </tr>
    </yfc:loopXML> 
</tbody>
</table>
