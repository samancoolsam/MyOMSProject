<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<%
	String rootNodeName = "ShipmentList";
%>

<table class="table" width="100%">
<thead>
    <tr> 
        <td class="checkboxheader" sortable="no">
            <input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
        </td>
		<td class="tablecolumnheader" nowrap="true" style="width:15px">&nbsp;</td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ShipmentList/Shipment/@ShipmentNo")%>">
            <yfc:i18n>Shipment_#</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ShipmentList/Shipment/@PickticketNo")%>">
            <yfc:i18n>Shippers_Ref_#</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ShipmentList/Shipment/@ExpectedShipmentDate")%>">
            <yfc:i18n>Expected_Ship_Date</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ShipmentList/Shipment/@ShipMode")%>">
            <yfc:i18n>Ship_Mode</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ShipmentList/Shipment/@ReceivingNode")%>">
            <yfc:i18n>Ship_Node</yfc:i18n>            
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ShipmentList/Shipment/Status/@Description")%>">
            <yfc:i18n>Status</yfc:i18n>
        </td>
    </tr>
</thead> 
<tbody>
    <yfc:loopXML binding='<%=buildBinding("xml:/",rootNodeName,"/@Shipment")%>' id="Shipment"> 
        <yfc:makeXMLInput name="shipmentKey" >
            <yfc:makeXMLKey binding="xml:/Shipment/@ShipmentKey" value="xml:/Shipment/@ShipmentKey" />
        </yfc:makeXMLInput>
    <tr>
        <td class="checkboxcolumn"> 
            <input type="checkbox" value='<%=getParameter("shipmentKey")%>' name="chkEntityKey" yfsTargetEntity="<%=getEntityIDForLink("L01", getValue("Shipment", "xml:/Shipment/@DocumentType"))%>" <%if (isVoid(getEntityIDForLink("L01", getValue("Shipment", "xml:/Shipment/@DocumentType")))) {%> disabled="true" <%}%> />
        </td>
		<td class="tablecolumn" nowrap="true">
			<%
				String sOrderDocType = resolveValue("xml:/Order/@DocumentType");
				String sShipmentDocType = resolveValue("xml:/Shipment/@DocumentType");
				//to handle the case when we are coming from the OrderRelease screen
				if(isVoid(sOrderDocType))
					sOrderDocType = resolveValue("xml:/OrderRelease/Order/@DocumentType");
			
				if( (!equals(sOrderDocType,sShipmentDocType)) && (!isVoid(sOrderDocType)) )
				{%>
					<img class="columnicon" onmouseover="this.style.cursor='default'" <%=getImageOptions(YFSUIBackendConsts.CHAINED_ORDERLINES_COLUMN, 	"Shipment_for_chained_order")%>>
				<%}%>
		</td>
        <td class="tablecolumn">
            <a <%=getDetailHrefOptions("L01",getValue("Shipment", "xml:/Shipment/@DocumentType"),getParameter("shipmentKey"),"")%> >
                <yfc:getXMLValue name="Shipment" binding="xml:/Shipment/@ShipmentNo"/>
            </a>
        </td>
        <td class="tablecolumn">
            <yfc:getXMLValue name="Shipment" binding="xml:/Shipment/@PickticketNo"/>
        </td>
        <td class="tablecolumn" sortValue="<%=getDateValue("xml:Shipment:/Shipment/@ExpectedShipmentDate")%>">
            <yfc:getXMLValue name="Shipment" binding="xml:/Shipment/@ExpectedShipmentDate"/>
        </td>
        <td class="tablecolumn">
			<%=getComboText("xml:ShipmentModeList:/CommonCodeList/@CommonCode", "CodeShortDescription", "CodeValue", "xml:/Shipment/@ShipMode",true)%>
        </td>
        <td class="tablecolumn">
            <yfc:getXMLValue name="Shipment" binding="xml:/Shipment/@ReceivingNode"/>
        </td>
        <td class="tablecolumn">
            <yfc:getXMLValueI18NDB name="Shipment" binding="xml:/Shipment/Status/@Description"/>
        </td>
    </tr>
    </yfc:loopXML> 
</tbody>
</table>
