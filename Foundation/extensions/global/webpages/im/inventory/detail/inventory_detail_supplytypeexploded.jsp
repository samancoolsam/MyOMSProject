<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/console/jsp/inventory.jspf" %>

<%
	String tagControlFlag = "N";
    String timeSensitive = "N";
	YFCElement itemDetailsElem = (YFCElement) request.getAttribute("ItemDetails");
	tagControlFlag = getValue("ItemDetails","xml:/Item/InventoryParameters/@TagControlFlag");
    timeSensitive = getValue("ItemDetails","xml:/Item/InventoryParameters/@TimeSensitive");    
	YFCElement itemElem = (YFCElement) request.getAttribute("Item");
	String itemID = itemElem.getAttribute("ItemID");
	String productClass = itemElem.getAttribute("ProductClass");
	String unitOfMeasure = itemElem.getAttribute("UnitOfMeasure");
	String organizationCode = itemElem.getAttribute("OrganizationCode");
%>

<table width="100%" class="table" editable="false">
<thead>
	<tr> 
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Supplies/InventorySupply/@ShipNode")%>">
            <yfc:i18n>Node</yfc:i18n>
        </td> 
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Supplies/InventorySupply/@ETA")%>">
            <yfc:i18n>ETA</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Supplies/InventorySupply/@SupplyReference")%>">
            <yfc:i18n>PO_#</yfc:i18n>
        </td>
        <%if (equals(timeSensitive, "Y")) {%>
           <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Supplies/InventorySupply/@ShipByDate")%>">
               <yfc:i18n>Ship_By_Date</yfc:i18n>
           </td>
        <%}%>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Supplies/InventorySupply/@SegmentType")%>">
            <yfc:i18n>Segment_Type</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Supplies/InventorySupply/@Segment")%>">
            <yfc:i18n>Segment</yfc:i18n>
        </td>

        <% if ((itemDetailsElem != null) && ((equals(tagControlFlag, "Y")) || (equals(tagControlFlag, "S")))) { %> 
        	<% setIdentifierAttribute(itemDetailsElem); %>
	        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Supplies/InventorySupply/@LotNumber")%>">
	            <yfc:i18n><yfc:getXMLValue binding="xml:ItemDetails:/Item/InventoryTagAttributes/@Identifier" /></yfc:i18n>
	        </td>
	    <%}%>
	    
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Supplies/InventorySupply/@Quantity")%>">
            <yfc:i18n>Quantity</yfc:i18n>
        </td>
    </tr>
</thead>	
<tbody>
    <yfc:loopXML binding="xml:/Item/Supplies/@InventorySupply" id="supply"> 
    <tr> 
		<yfc:makeXMLInput name="orderlinekey">
			<yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderLineKey" value="xml:supply:/InventorySupply/@SupplyLineReference" />
		</yfc:makeXMLInput>
		<% if(!isVoid(resolveValue("xml:supply:/InventorySupply/@SupplyLineReference"))) {
			request.setAttribute("supply", pageContext.getAttribute("supply"));
		%>
			<yfc:callAPI apiID='AP1'/>
		<%}%>
			<td class="tablecolumn"><yfc:getXMLValue name="supply" binding="xml:/InventorySupply/@ShipNode" /></td>
			<td class="tablecolumn" sortValue="<%=getDateValue("xml:supply:/InventorySupply/@ETA")%>"><yfc:getXMLValue name="supply" binding="xml:/InventorySupply/@ETA" /></td>
			<td class="tablecolumn">
			<% if(showOrderLineNo("OrderLine","Order")) {%>
				<a <%=getDetailHrefOptions("L01",resolveValue("xml:supply:/InventorySupply/@SupplyReferenceType"),getParameter("orderlinekey"), "")%> >
					<yfc:getXMLValue name="supply" binding="xml:/InventorySupply/@SupplyReference" />
				</a>
			<% } else {%>
				<yfc:getXMLValue name="supply" binding="xml:/InventorySupply/@SupplyReference" />
			<%}%>
			</td>
            <%if (equals(timeSensitive, "Y")) {%>
			     <td class="tablecolumn" sortValue="<%=getDateValue("xml:supply:/InventorySupply/@ShipByDate")%>"><yfc:getXMLValue name="supply" binding="xml:/InventorySupply/@ShipByDate" /></td>
			<%}%>
			<% 
			   if(!isVoid(resolveValue("xml:supply:/InventorySupply/@SegmentType"))){	
				   YFCElement commCodeElem = YFCDocument.createDocument("CommonCode").getDocumentElement();
				   commCodeElem.setAttribute("CodeType","SEGMENT_TYPE");
				   commCodeElem.setAttribute("CodeValue",resolveValue("xml:supply:/InventorySupply/@SegmentType"));
				   YFCElement templateElem = YFCDocument.parse("<CommonCode CodeName=\"\" CodeShortDescription=\"\" CodeType=\"\" CodeValue=\"\" CommonCodeKey=\"\" />").getDocumentElement();
            %>
				   <yfc:callAPI apiName="getCommonCodeList" inputElement="<%=commCodeElem%>" 
															templateElement="<%=templateElem%>" outputNamespace=""/>
				   <td class="tablecolumn"><yfc:getXMLValueI18NDB binding="xml:/CommonCodeList/CommonCode/@CodeShortDescription" /></td>
			<% } else { %>
	            <td class="tablecolumn">&nbsp;</td>
			<% } %>
            <td class="tablecolumn"><yfc:getXMLValue name="supply" binding="xml:/InventorySupply/@Segment" /></td>

			<%	//Need to form YFCElement to pass
				YFCElement supplyElem = (YFCElement) pageContext.getAttribute("supply");
				String supplyType = supplyElem.getAttribute("SupplyType");
				String shipNode = supplyElem.getAttribute("ShipNode");

				YFCElement inventorySupplyElem = YFCDocument.createDocument("InventorySupply").getDocumentElement();
				inventorySupplyElem.setAttribute("ItemID",itemID);
				inventorySupplyElem.setAttribute("ProductClass",productClass);
				inventorySupplyElem.setAttribute("UnitOfMeasure",unitOfMeasure);
				inventorySupplyElem.setAttribute("OrganizationCode",organizationCode);
				inventorySupplyElem.setAttribute("SupplyType",supplyType);
				inventorySupplyElem.setAttribute("ShipNode",shipNode);
				inventorySupplyElem.setAttribute("ConsiderAllSegments","Y");
				if ((equals(tagControlFlag, "Y")) || (equals(tagControlFlag, "S"))) {
					YFCElement tagElem = supplyElem.getChildElement("Tag");
					if (tagElem != null) {
						inventorySupplyElem.importNode(tagElem);
					}
				}
				String getInventorySupplyInputEncoded = java.net.URLEncoder.encode(inventorySupplyElem.getString());
				getInventorySupplyInputEncoded = "getInventorySupplyInput=" + getInventorySupplyInputEncoded;
			%>

			<% if ((itemDetailsElem != null) && ((equals(tagControlFlag, "Y")) || (equals(tagControlFlag, "S")))) { %> 
				<yfc:makeXMLInput name="inventorySupplyKey">
					<yfc:makeXMLKey binding="xml:/InventorySupply/@dummy" value="xml:/Item/@dummy" />
				</yfc:makeXMLInput>
				<% 	
					String identifierAttribute = getValue("ItemDetails","xml:ItemDetails:/Item/InventoryTagAttributes/@Identifier");
					String inventoryTagBinding = null;
					if( identifierAttribute.startsWith("Extn") )
						inventoryTagBinding = "xml:supply:/InventorySupply/Tag/Extn/@" + identifierAttribute;
					else
						inventoryTagBinding = "xml:supply:/InventorySupply/Tag/@" + identifierAttribute;
				%>
				<td class="tablecolumn">
					<a <%=getDetailHrefOptions("L02", getParameter("inventorySupplyKey"), getInventorySupplyInputEncoded)%>>
						<yfc:getXMLValue name="supply" binding="<%=inventoryTagBinding%>"/> 
					</a>
				</td>													
			<%}%>
			
			<td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:supply:/InventorySupply/@Quantity")%>"><yfc:getXMLValue name="supply" binding="xml:/InventorySupply/@Quantity" /></td>
    </tr>
    </yfc:loopXML> 
</tbody>	
</table>

