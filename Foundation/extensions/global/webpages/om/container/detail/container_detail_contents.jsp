<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<%
	YFCElement container = (YFCElement) request.getAttribute("Container");
	String sAppCode = resolveValue("xml:/CurrentEntity/@ApplicationCode");
	boolean outboundShipment = false;
	if(equals(sAppCode,"omd")){
       outboundShipment = true;
	}
	boolean showPackedQty = false;
	if(outboundShipment){
		if(!isVoid(resolveValue("xml:/Container/ContainerStatusAudits/@TotalNumberOfRecords"))){
			if(!equals(resolveValue("xml:/Container/ContainerStatusAudits/@TotalNumberOfRecords"),"0")){
				showPackedQty = true;
			}
		}
	}
%>
<table class="table" width="100%">
<thead>
    <tr> 
        <td class="tablecolumnheader" style="width:30px" sortable="no">
            <yfc:i18n>Tag_Details</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerDetails/ContainerDetail/Order/@OrderNo")%>" sortable="no">
            <yfc:i18n>Order_#</yfc:i18n>
        </td>
		<td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerDetails/ContainerDetail/OrderLine/@PrimeLineNo")%>" sortable="no">
            <yfc:i18n>Line_#</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerDetails/ContainerDetail/OrderRelease/@ReleaseNo")%>" sortable="no">
            <yfc:i18n>Release_#</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerDetails/ContainerDetail/@ItemID")%>" sortable="no">
            <yfc:i18n>Item_ID</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerDetails/ContainerDetail/@ProductClass")%>" sortable="no">
            <yfc:i18n>PC</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerDetails/ContainerDetail/@UOM")%>" sortable="no">
            <yfc:i18n>UOM</yfc:i18n>
        </td>
		<td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerDetails/ContainerDetail/@CountryOfOrigin")%>" sortable="no">
            <yfc:i18n>COO</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerDetails/ContainerDetail/@ItemDesc")%>" sortable="no">
            <yfc:i18n>Description</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerDetails/ContainerDetail/@ShippedQuantity")%>" sortable="no">
            <yfc:i18n>Quantity</yfc:i18n>
        </td>
		<%
		if(showPackedQty){
			%>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerDetails/ContainerDetail/@ShippedQuantity")%>" sortable="no">
            <yfc:i18n>Packed_Quantity</yfc:i18n>
        </td>
		<%
		}
		%>
    </tr>
</thead>
<tbody>
    <%String className="oddrow";%>
    <yfc:loopXML name="Container" binding="xml:/Container/ContainerDetails/@ContainerDetail" id="ContainerDetail"  keyName="ContainerDetailsKey" > 
        <yfc:makeXMLInput name="OrderHeaderKey" >
            <yfc:makeXMLKey binding="xml:/Order/@OrderHeaderKey" value="xml:/ContainerDetail/@OrderHeaderKey" />
        </yfc:makeXMLInput>
        <yfc:makeXMLInput name="OrderLineKey" >
            <yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderLineKey" value="xml:/ContainerDetail/@OrderLineKey" />
        </yfc:makeXMLInput>
        <yfc:makeXMLInput name="OrderReleaseKey" >
            <yfc:makeXMLKey binding="xml:/OrderReleaseDetail/@OrderReleaseKey" value="xml:/ContainerDetail/@OrderReleaseKey" />
        </yfc:makeXMLInput>
   	<tr class='<%=className%>'>
		<%
			if (equals("oddrow",className))
				className="evenrow";
			else
				className="oddrow";													
		%>
	<td>
		<% 
			container.setAttribute("DummyItemID",resolveValue("xml:ContainerDetail:/ContainerDetail/@ItemID"));
			container.setAttribute("DummyProductClass",resolveValue("xml:ContainerDetail:/ContainerDetail/@ProductClass"));
			container.setAttribute("DummyUnitOfMeasure",resolveValue("xml:ContainerDetail:/ContainerDetail/@UnitOfMeasure"));
			container.setAttribute("DummyEnterpriseCode",resolveValue("xml:/Container/Shipment/@EnterpriseCode"));


		if(!isVoid(resolveValue("xml:/Container/Shipment/@ShipNode"))){
			YFCElement tempNodeItem = YFCDocument.parse("<Item ItemID=\"\" ItemKey=\"\" TagCapturedInShipping=\"\"><PrimaryInformation SerializedFlag=\"\" SerialCapturedInReturns=\"\" SerialCapturedInShipping=\"\" SerialCapturedInInventory=\"\" SerialCapturedInReceiving=\"\" /><InventoryParameters TagControlFlag=\"\" IsSerialTracked=\"\" TimeSensitive=\"\"/><InventoryTagAttributes ItemTagKey=\"\" ItemKey=\"\" LotNumber=\"\" LotKeyReference=\"\" ManufacturingDate=\"\" LotExpirationDate=\"\" LotAttribute1=\"\" LotAttribute2=\"\" LotAttribute3=\"\" RevisionNo=\"\" BatchNo=\"\"><Extn/></InventoryTagAttributes> </Item>").getDocumentElement();

					YFCElement oNodeItemDetails = YFCDocument.createDocument("Item").getDocumentElement(); 
					oNodeItemDetails.setAttribute("OrganizationCode",resolveValue("xml:/Container/@DummyEnterpriseCode"));
					oNodeItemDetails.setAttribute("ItemID",resolveValue("xml:/Container/@DummyItemID"));
					oNodeItemDetails.setAttribute("ProductClass",resolveValue("xml:/Container/@DummyProductClass"));	oNodeItemDetails.setAttribute("UnitOfMeasure",resolveValue("xml:/Container/@DummyUnitOfMeasure"));
					oNodeItemDetails.setAttribute("Node",resolveValue("xml:/Container/Shipment/@ShipNode"));
					%>
						<yfc:callAPI apiName="getNodeItemDetails" inputElement="<%=oNodeItemDetails%>" templateElement="<%=tempNodeItem%>" outputNamespace="ItemDetails"/>
					<%


		}else{

%>
		<yfc:callAPI apiID="AP1"/>

<%		
		}
				//changing the serial tracked flag for RQ-WMS-158
				//boolean  serialTracked=equals("Y",getValue("ItemDetails","xml:/Item/PrimaryInformation/@SerializedFlag"));
				boolean  serialTracked=(equals("Y",getValue("ItemDetails","xml:/Item/PrimaryInformation/@SerialCapturedInShipping")) || equals("Y",getValue("ItemDetails","xml:/Item/PrimaryInformation/@SerialCapturedInReceiving")));
				boolean  tagControlled=equals("Y",getValue("ItemDetails","xml:/Item/InventoryParameters/@TagControlFlag")) ||equals("S",getValue("ItemDetails","xml:/Item/InventoryParameters/@TagControlFlag"));
				boolean  timeSensitive=equals("Y",getValue("ItemDetails","xml:/Item/InventoryParameters/@TimeSensitive"));

				if(!isVoid(resolveValue("xml:/Container/Shipment/@ShipNode"))){
					YFCElement tempItemElement = YFCDocument.parse("<Item ItemID=\"\" ItemKey=\"\" TagCapturedInShipping=\"\"><PrimaryInformation SerializedFlag=\"\" SerialCapturedInReceiving=\"\" SerialCapturedInInventory=\"\" SerialCapturedInShipping=\"\" SerialCapturedInReturnsReceiving=\"\"/> <InventoryParameters TagControlFlag=\"\" IsSerialTracked=\"\" TimeSensitive=\"\"/><InventoryTagAttributes ItemTagKey=\"\" ItemKey=\"\" LotNumber=\"\" LotKeyReference=\"\" ManufacturingDate=\"\" LotExpirationDate=\"\" LotAttribute1=\"\" LotAttribute2=\"\" LotAttribute3=\"\" RevisionNo=\"\" BatchNo=\"\"><Extn/></InventoryTagAttributes> </Item>").getDocumentElement();
					YFCElement oItemDetailsElement = YFCDocument.createDocument("Item").getDocumentElement(); 
					oItemDetailsElement.setAttribute("CallingOrganizationCode",resolveValue("xml:/Container/Shipment/@EnterpriseCode"));
					oItemDetailsElement.setAttribute("ItemID",resolveValue("xml:ContainerDetail:/ContainerDetail/@ItemID"));
					oItemDetailsElement.setAttribute("Node",resolveValue("xml:/Container/Shipment/@ShipNode"));
					oItemDetailsElement.setAttribute("BuyerOrganization",resolveValue("xml:/Container/Shipment/@BuyerOrganizationCode"));
					oItemDetailsElement.setAttribute("UnitOfMeasure",resolveValue("xml:ContainerDetail:/ContainerDetail/@UnitOfMeasure")); 
%>
					<yfc:callAPI apiName="getNodeItemDetails" inputElement="<%=oItemDetailsElement%>" templateElement="<%=tempItemElement%>" outputNamespace="ItemDetails"/>
<%
					tagControlled = equals("Y",getValue("ItemDetails","xml:/Item/@TagCapturedInShipping")) ||equals("S",getValue("ItemDetails","xml:/Item/@TagCapturedInShipping"));

					serialTracked=(equals("Y",getValue("ItemDetails","xml:/Item/PrimaryInformation/@SerialCapturedInShipping")) || equals("Y",getValue("ItemDetails","xml:/Item/PrimaryInformation/@SerialCapturedInReceiving")));
				}else{
					serialTracked=equals("Y",getValue("ItemDetails","xml:/Item/PrimaryInformation/@SerializedFlag"));
				}
				if(serialTracked||tagControlled||timeSensitive){
%>          
		  <img onclick="expandCollapseDetails('optionSet_<%=ContainerDetailCounter%>','<%=getI18N("Click_To_See_Tag_Info")%>','<%=getI18N("Click_To_Hide_Tag_Info")%>','<%=YFSUIBackendConsts.FOLDER_COLLAPSE%>','<%=YFSUIBackendConsts.FOLDER_EXPAND%>')"  style="cursor:hand" <%=getImageOptions(YFSUIBackendConsts.FOLDER,"Click_To_See_Tag_Info")%> />
	<%}%>
	</td>

        <td class="tablecolumn">
			<% if(showOrderNo("ContainerDetail","Order")) {%>
	            <a <%=getDetailHrefOptions("L01",getValue("ContainerDetail", "xml:/ContainerDetail/Order/@DocumentType"),getParameter("OrderHeaderKey"),"")%> >
		            <yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/Order/@OrderNo"/>  
			    </a>
			<%} else {%>
				<yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/Order/@OrderNo"/>  
			<%}%>
        </td>
		<%
		if(!isVoid(resolveValue("xml:ContainerDetail:/ContainerDetail/OrderLine/@PrimeLineNo"))) { %>
			<td class="tablecolumn" sortValue="<%=getNumericValue("xml:ContainerDetail:/ContainerDetail/OrderLine/@PrimeLineNo")%>">
				<% if(showOrderLineNo("ContainerDetail","Order")) {%>
					<a <%=getDetailHrefOptions("L02",getParameter("OrderLineKey"),"")%> >
						<yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/OrderLine/@PrimeLineNo"/> /
						<yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/OrderLine/@SubLineNo"/>
					</a>	
				<%} else {%>
					<yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/OrderLine/@PrimeLineNo"/> /
					<yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/OrderLine/@SubLineNo"/>
				<%}%>
			</td>
		<%}else {%>
			<td class="tablecolumn" sortValue="<%=getNumericValue("xml:ContainerDetail:/ContainerDetail/ShipmentLine/@PrimeLineNo")%>">
				<yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/ShipmentLine/@PrimeLineNo"/> /
				<yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/ShipmentLine/@SubLineNo"/>				
			</td>
		<%}%>
        <td class="tablecolumn" sortValue="<%=getNumericValue("xml:ContainerDetail:/ContainerDetail/OrderRelease/@ReleaseNo")%>">
            <a <%=getDetailHrefOptions("L03", getParameter("OrderReleaseKey"),"")%> >
                <yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/OrderRelease/@ReleaseNo"/>            
            </a>
        </td>
        <td class="tablecolumn">
            <yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/@ItemID"/>
        </td>
        <td class="tablecolumn">
            <yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/@ProductClass"/>
        </td>
        <td class="tablecolumn">
            <yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/@UnitOfMeasure"/>
        </td>
		<td class="tablecolumn">
            <yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/@CountryOfOrigin"/>
        </td>
		<td class="tablecolumn">
            <yfc:getXMLValue name="ItemDetails" binding="xml:/Item/PrimaryInformation/@ShortDescription"/>
        </td>
        <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:ContainerDetail:/ContainerDetail/@Quantity")%>">
            <yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/@Quantity"/>
        </td>
		<%
		if(showPackedQty){
			%>
			<td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:ContainerDetail:/ContainerDetail/@QuantityPlaced")%>">
				<yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/@QuantityPlaced"/>
			</td>
		<%}%>
      	<tr id='<%="optionSet_"+ContainerDetailCounter%>' class='<%=className%>' style="display:none">
			<td colspan="7" >
				<jsp:include page="/om/shipment/detail/shipment_detail_includepacktag.jsp" flush="true">
					<jsp:param name="optionSetBelongingToLine" value='<%=String.valueOf(ContainerDetailCounter)%>'/>
					<jsp:param name="Modifiable" value='false'/>
					<jsp:param name="LabelTDClass" value='detaillabel'/>
					<jsp:param name="TagContainer" value='Container'/>
					<jsp:param name="TagElement" value='ShipmentTagSerial'/>
				</jsp:include>
			</td>
		</tr>
    </tr>
    </yfc:loopXML> 
</tbody>
</table>
 
