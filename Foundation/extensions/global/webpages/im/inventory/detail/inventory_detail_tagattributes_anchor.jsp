<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>

<%
    String inventorySuppyInputEncoded = (String) request.getParameter("getInventorySupplyInput");
	String inventorySuppyInputTxt = java.net.URLDecoder.decode(inventorySuppyInputEncoded);
	YFCElement inventorySuppyInputElem = YFCDocument.parse(inventorySuppyInputTxt).getDocumentElement();
	
	String inventorySupplyTemplateTxt = "<Item ItemID=\"\" ProductClass=\"\" UnitOfMeasure=\"\" OrganizationCode=\"\" >" +
                   "<Supplies>" +
                      "<InventorySupply>" +
                         "<Tag TagNumber=\"\" InventoryTagKey=\"\" InventoryItemKey=\"\" LotNumber=\"\" LotKeyReference=\"\" ManufacturingDate=\"\" LotExpirationDate=\"\" LotAttribute1=\"\" LotAttribute2=\"\" LotAttribute3=\"\" RevisionNo=\"\" BatchNo=\"\" >" +
							"<Extn/>" +
						 "</Tag>" +
                      "</InventorySupply>" +
                   "</Supplies>" +
                "</Item>" ;
	YFCElement inventorySupplyTemplateElem = YFCDocument.parse(inventorySupplyTemplateTxt).getDocumentElement();

%>
	<yfc:callAPI apiName="getInventorySupply" inputElement="<%=inventorySuppyInputElem%>" templateElement="<%=inventorySupplyTemplateElem%>" outputNamespace="TagContainer"/>
<%	
	YFCElement itemElem = (YFCElement)request.getAttribute("TagContainer");
	YFCElement itemInputElem = YFCDocument.createDocument("Item").getDocumentElement();
	itemInputElem.setAttribute("ItemID",itemElem.getAttribute("ItemID"));
	itemInputElem.setAttribute("ProductClass",itemElem.getAttribute("ProductClass"));
	itemInputElem.setAttribute("UnitOfMeasure",itemElem.getAttribute("UnitOfMeasure"));
	itemInputElem.setAttribute("OrganizationCode",itemElem.getAttribute("OrganizationCode"));

	String itemDetailsTemplateTxt = "<Item>" +
				"<InventoryTagAttributes ItemTagKey=\"\" ItemKey=\"\" LotNumber=\"\" LotKeyReference=\"\" ManufacturingDate=\"\" LotExpirationDate=\"\" LotAttribute1=\"\" LotAttribute2=\"\" LotAttribute3=\"\" RevisionNo=\"\" BatchNo=\"\" >" +
					"<Extn />" +
				"</InventoryTagAttributes>" +
			"</Item>" ;
	YFCElement itemDetailsTemplateElem = YFCDocument.parse(itemDetailsTemplateTxt).getDocumentElement();
%>
	<yfc:callAPI apiName="getItemDetails" inputElement="<%=itemInputElem%>" templateElement="<%=itemDetailsTemplateElem%>" outputNamespace="ItemDetails"/>


<table class="anchor" cellpadding="7px"  cellSpacing="0" >
	<tr>
		<td>
			<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
				<jsp:param name="CurrentInnerPanelID" value="I01"/>
			</jsp:include>
		</td>
	</tr>
</table>
