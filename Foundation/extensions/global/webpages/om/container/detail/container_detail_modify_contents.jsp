<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<script language="javascript">
	yfcDoNotPromptForChanges(true);
</script>
<%
	YFCElement container = (YFCElement) request.getAttribute("Container");
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
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerDetails/ContainerDetail/@ItemDesc")%>" sortable="no">
            <yfc:i18n>Description</yfc:i18n>
        </td>
		<td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerDetails/ContainerDetail/@ShipByDate")%>" sortable="no">
            <yfc:i18n>Ship_By_Date</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerDetails/ContainerDetail/@Quantity")%>" sortable="no">
            <yfc:i18n>Containerized_Quantity</yfc:i18n>
        </td>
		<td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerDetails/ContainerDetail/@Quantity")%>" sortable="no">
            <yfc:i18n>Packed_Quantity</yfc:i18n>
        </td>

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
		%>
		<yfc:callAPI apiID="AP1"/>
		
		<%		
				//changing the serial tracked flag for RQ-WMS-158
				//boolean  serialTracked=equals("Y",getValue("ItemDetails","xml:/Item/PrimaryInformation/@SerializedFlag"));
				boolean  serialTracked=(equals("Y",getValue("ItemDetails","xml:/Item/PrimaryInformation/@SerialCapturedInShipping")) || equals("Y",getValue("ItemDetails","xml:/Item/PrimaryInformation/@SerialCapturedInReceiving")));
				boolean  tagControlled=equals("Y",getValue("ItemDetails","xml:/Item/InventoryParameters/@TagControlFlag")) ||equals("S",getValue("ItemDetails","xml:/Item/InventoryParameters/@TagControlFlag"));
				boolean  timeSensitive=equals("Y",getValue("ItemDetails","xml:/Item/InventoryParameters/@TimeSensitive"));

				if(tagControlled && !isVoid(resolveValue("xml:/Container/Shipment/@ShipNode"))){
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
	            <a <%=getDetailHrefOptions("L01",getParameter("OrderHeaderKey"),"")%> >
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
            <yfc:getXMLValue name="ItemDetails" binding="xml:/Item/PrimaryInformation/@Description"/>
        </td>
		<td class="tablecolumn" sortValue="<%=getDateValue("xml:ContainerDetail:/ContainerDetail/@ShipByDate")%>">
            <yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/@ShipByDate"/>
        </td>
        <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:ContainerDetail:/ContainerDetail/@Quantity")%>">
			<input type="text" <%=yfsGetTemplateRowOptions("xml:/Container/ContainerDetails/ContainerDetail_"+ContainerDetailCounter+"/@Quantity", "xml:/ContainerDetail/@Quantity", "xml:/Container/AllowedModifications","CONTAINER_ADD_TO_CONTAINER_DETAIL","text")%>/>
			<input type="hidden" <%=getTextOptions("xml:/Container/ContainerDetails/ContainerDetail_"+ContainerDetailCounter +"/@ContainerDetailsKey", "xml:/ContainerDetail/@ContainerDetailsKey","xml:/ContainerDetail/@ContainerDetailsKey")%>/>
        </td>
        <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:ContainerDetail:/ContainerDetail/@QuantityPlaced")%>">
            <yfc:getXMLValue name="ContainerDetail" binding="xml:/ContainerDetail/@QuantityPlaced"/>
        </td>


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
 