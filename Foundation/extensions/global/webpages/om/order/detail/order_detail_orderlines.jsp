<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<!-- Added as part of  OMNI-65895 - Klarna VCN - Sterling to display "Klarna" memo (DOM) -->
<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ include file="/console/jsp/order.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ include file="/yfsjspcommon/editable_util_lines.jspf" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/om.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/css/scripts/editabletbl.js"></script>

<script language="javascript">
    document.body.attachEvent("onunload", processSaveRecordsForChildNode);
</script>

<%
	appendBundleRootParent((YFCElement)request.getAttribute("Order"));
	boolean bAppendOldValue = false;
	if(!isVoid(errors) || equals(sOperation,"Y") || equals(sOperation,"DELETE")) 
		bAppendOldValue = true;
	String modifyView = request.getParameter("ModifyView");
    modifyView = modifyView == null ? "" : modifyView;

    String driverDate = getValue("Order", "xml:/Order/@DriverDate");
	String extraParams = getExtraParamsForTargetBinding("xml:/Item/@CallingOrganizationCode", getValue("Order", "xml:/Order/@EnterpriseCode"));
%>

<table class="table" ID="OrderLines" cellspacing="0" width="100%" yfcMaxSortingRecords="1000" >
    <thead>
        <tr>
            <td class="checkboxheader" sortable="no">
                <input type="hidden" id="userOperation" name="userOperation" value="" />
                <input type="hidden" id="numRowsToAdd" name="numRowsToAdd" value="" />
                <input type="checkbox" value="checkbox" name="checkbox" onclick="doCheckAll(this);"/>
            </td>
            <td class="tablecolumnheader" nowrap="true" style="width:30px">&nbsp;</td>
            <td class="tablecolumnheader" nowrap="true" style="width:<%=getUITableSize("xml:/OrderLine/@PrimeLineNo")%>"><yfc:i18n>Line</yfc:i18n></td>
            <td class="tablecolumnheader" nowrap="true" style="width:<%=getUITableSize("xml:/OrderLine/Item/@ItemID")%>"><yfc:i18n>Item_ID</yfc:i18n></td>
            <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/OrderLine/Item/@ProductClass")%>"><yfc:i18n>PC</yfc:i18n></td>
            <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/OrderLine/OrderLineTranQuantity/@TransactionalUOM")%>"><yfc:i18n>UOM</yfc:i18n></td>
            <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/OrderLine/Item/@ItemDesc")%>"><yfc:i18n>Description</yfc:i18n></td>
            <td class="tablecolumnheader" nowrap="true" sortable="no" style="width:<%=getUITableSize("xml:/OrderLine/@ReceivingNode")%>"><yfc:i18n>Recv_Node</yfc:i18n></td>
            <td class="tablecolumnheader" nowrap="true" sortable="no" style="width:<%=getUITableSize("xml:/OrderLine/@ShipNode")%>"><yfc:i18n>Ship_Node</yfc:i18n></td>
            <% if (equals(driverDate, "02")) { %>
                <td class="tablecolumnheader" nowrap="true" sortable="no" style="width:<%=getUITableSize("xml:/OrderLine/@ReqDeliveryDate")%>"><yfc:i18n>Delivery_Date</yfc:i18n></td>
            <% } else { %>
                <td class="tablecolumnheader" nowrap="true" sortable="no" style="width:<%=getUITableSize("xml:/OrderLine/@ReqShipDate")%>"><yfc:i18n>Ship_Date</yfc:i18n></td>
            <% } %>
            <td class="tablecolumnheader" nowrap="true" sortable="no" style="width:<%=getUITableSize("xml:/OrderLine/@OrderedQty")%>"><yfc:i18n>Line_Qty</yfc:i18n></td>
            <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/OrderLine/LineOverallTotals/@LineTotal")%>"><yfc:i18n>Amount</yfc:i18n></td>
            <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/OrderLine/@Status")%>"><yfc:i18n>Status</yfc:i18n></td>
        </tr>
    </thead>
    <tbody>
        <yfc:loopXML name="Order" binding="xml:/Order/OrderLines/@OrderLine" id="OrderLine">
        	<%	
			//Set variables to indicate the orderlines dependency situation. 
			boolean isDependentParent = false;
			boolean isDependentChild = false;
			if (equals(getValue("OrderLine","xml:/OrderLine/@ParentOfDependentGroup"),"Y")) {  
				isDependentParent = true;
			}
			if (!isVoid(getValue("OrderLine","xml:/OrderLine/@DependentOnLineKey"))) {
				isDependentChild = true;
			}
		
			if(!isVoid(resolveValue("xml:OrderLine:/OrderLine/@Status"))) {  
				if (equals(getValue("OrderLine","xml:/OrderLine/@ItemGroupCode"),"PROD") )
					//display line in this inner panel only if item has ItemGroupCode = PROD
				{
					if(bAppendOldValue) {
						String sOrderLineKey = resolveValue("xml:OrderLine:/OrderLine/@OrderLineKey");
						if(oMap.containsKey(sOrderLineKey))
							request.setAttribute("OrigAPIOrderLine",(YFCElement)oMap.get(sOrderLineKey));
				} else 
						request.setAttribute("OrigAPIOrderLine",(YFCElement)pageContext.getAttribute("OrderLine"));
					%>
				<tr>
					<yfc:makeXMLInput name="orderLineKey">
						<yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderLineKey" value="xml:/OrderLine/@OrderLineKey"/>
						<yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderHeaderKey" value="xml:/Order/@OrderHeaderKey"/>
						<yfc:makeXMLKey binding="xml:/OrderLineDetail/@IsBundleParent" value="xml:/OrderLine/@IsBundleParent"/>
					</yfc:makeXMLInput>
					<yfc:makeXMLInput name="bundleRootParentKey">
						<yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderLineKey" value="xml:/OrderLine/@BundleRootParentKey"/>
						<yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderHeaderKey" value="xml:/Order/@OrderHeaderKey"/>
						<yfc:makeXMLKey binding="xml:/OrderLineDetail/@IsBundleParent" value="xml:/OrderLine/@IsBundleParent"/>
						<yfc:makeXMLKey binding="xml:/OrderLineDetail/@OriginalLineItemClicked" value="xml:/OrderLine/@PrimeLineNo"/>
					</yfc:makeXMLInput>
					<td class="checkboxcolumn" >
						<input type="checkbox" value='<%=getParameter("orderLineKey")%>' name="chkEntityKey" 
						<% if (isDependentParent || isDependentChild) {%> inExistingDependency="true" <%}%>/>
						<%/*This hidden input is required by yfc to match up each line attribute that is editable in this row against the appropriate order line # on the server side once you save.  */%>
						<input type="hidden" name='OrderLineKey_<%=OrderLineCounter%>' value='<%=resolveValue("xml:/OrderLine/@OrderLineKey")%>' />
						<input type="hidden" name='OrderHeaderKey_<%=OrderLineCounter%>' value='<%=resolveValue("xml:/Order/@OrderHeaderKey")%>' />
                        <input type="hidden" <%=getTextOptions("xml:/Order/OrderLines/OrderLine_" + OrderLineCounter + "/@OrderLineKey", "xml:/OrderLine/@OrderLineKey")%> />
						<input type="hidden" <%=getTextOptions("xml:/Order/OrderLines/OrderLine_" + OrderLineCounter + "/@PrimeLineNo", "xml:/OrderLine/@PrimeLineNo")%> />
						<input type="hidden" <%=getTextOptions("xml:/Order/OrderLines/OrderLine_" + OrderLineCounter + "/@SubLineNo", "xml:/OrderLine/@SubLineNo")%> />
					</td>
					<td class="tablecolumn" nowrap="true">
						<% if (equals(getValue("OrderLine","xml:/OrderLine/@GiftFlag"),"Y") ){ %>
							<img <%=getImageOptions(request.getContextPath() + "/console/icons/gift.gif", "This_is_a_Gift_Line")%>/>
						<% } %>
						<yfc:hasXMLNode binding="xml:/OrderLine/Instructions/Instruction">
							<a <%=getDetailHrefOptions("L01", getParameter("orderLineKey"), "")%>>
								<img class="columnicon" <%=getImageOptions(YFSUIBackendConsts.INSTRUCTIONS_COLUMN, "Instructions")%>></a>
						</yfc:hasXMLNode>
						<yfc:hasXMLNode binding="xml:/OrderLine/KitLines/KitLine">
							<a <%=getDetailHrefOptions("L02", getParameter("orderLineKey"), "")%>>
								<img class="columnicon" <%=getImageOptions(YFSUIBackendConsts.KIT_COMPONENTS_COLUMN, "Kit_Components")%>></a>
						</yfc:hasXMLNode>

						<% if (equals(getValue("OrderLine","xml:/OrderLine/@IsBundleParent"),"Y")) { %>
						<a <%=getDetailHrefOptions("L02", getParameter("bundleRootParentKey"), "")%>>
								<img class="columnicon" <%=getImageOptions(YFSUIBackendConsts.KIT_COMPONENTS_COLUMN, "Bundle_Components")%>></a>
								<%}%>


						<yfc:makeXMLInput name="orderLineKey">
							<yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderLineKey" value="xml:/OrderLine/@OrderLineKey"/>
							<yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderHeaderKey" value="xml:/Order/@OrderHeaderKey"/>
						</yfc:makeXMLInput>
					<%	if(!equals("false", getParameter("CanAddServiceLines") ) )
						{
							if (equals(getValue("OrderLine","xml:/OrderLine/@HasServiceLines"),"Y")) { %>
								<a <%=getDetailHrefOptions("L12", getParameter("orderLineKey"), "")%>><img class="columnicon" <%=getImageOptions(request.getContextPath() + "/console/icons/providedservicecol.gif", "Line_Has_Associated_Service_Requests")%> ></a>

						<%	}	else	if (equals(getValue("OrderLine","xml:/OrderLine/@CanAddServiceLines"),"Y") && ! equals(getValue("Order","xml:/Order/@isHistory"),"Y")) { %>  
								<a <%=getDetailHrefOptions("L10", getParameter("orderLineKey"), "")%>><img class="columnicon" <%=getImageOptions(request.getContextPath() + "/console/icons/addprovidedservice.gif", "Line_Has_Service_Requests_That_Can_Be_Added")%> ></a>
						<%	}	%>
						<%}%>
						<%	if (isDependentParent) { %>    
								<yfc:makeXMLInput name="dependentParentKey">
									<yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderLineKey" value="xml:/OrderLine/@OrderLineKey"/>
									<yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderHeaderKey" value="xml:/Order/@OrderHeaderKey"/>
								</yfc:makeXMLInput>
								<a <%=getDetailHrefOptions("L05", getParameter("dependentParentKey"), "")%>><img class="columnicon" <%=getImageOptions(YFSUIBackendConsts.PARENT_DEPENDENCY_COMPONENTS_COLUMN, "Dependent_Parent")%> ></a>
						<% } %>
						<% if (isDependentChild) { %>
							<yfc:makeXMLInput name="dependentChildKey">
								<yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderLineKey" value="xml:/OrderLine/@DependentOnLineKey"/>
								<yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderHeaderKey" value="xml:/Order/@OrderHeaderKey"/>
							</yfc:makeXMLInput>
							<a <%=getDetailHrefOptions("L06", getParameter("dependentChildKey"), "")%>>
								<img class="columnicon" <%=getImageOptions(YFSUIBackendConsts.CHILD_DEPENDENCY_COMPONENTS_COLUMN, "Dependent_Child")%>></a>
						<% }%>
						<% if (equals(getValue("OrderLine","xml:/OrderLine/@HasChainedLines"),"Y") || (!isVoid(getValue("OrderLine","xml:/OrderLine/@ChainedFromOrderLineKey"))) || equals(getValue("OrderLine","xml:/OrderLine/@HasDerivedChild"),"Y") || (!isVoid(getValue("OrderLine","xml:/OrderLine/@DerivedFromOrderLineKey")))) { %>                        
							<a <%=getDetailHrefOptions("L09", getParameter("orderLineKey"), "")%>>
								<img class="columnicon" <%=getImageOptions(YFSUIBackendConsts.CHAINED_ORDERLINES_COLUMN, "Related_Lines")%>></a>
						<% }%>
						<% if (equals(getValue("OrderLine","xml:/OrderLine/@AwaitingDeliveryRequest"),"Y") && ! equals(getValue("Order","xml:/Order/@isHistory"),"Y")) { %>
							<yfc:makeXMLInput name="orderKey">
								<yfc:makeXMLKey binding="xml:/Order/@OrderHeaderKey" value="xml:/Order/@OrderHeaderKey"/>
							</yfc:makeXMLInput>
							<a <%=getDetailHrefOptions("L13", getParameter("orderKey"), "")%>>
								<img class="columnicon" <%=getImageOptions(request.getContextPath() + "/console/icons/deliveryitem.gif", "Delivery_Request_needs_to_be_added")%>></a>
						<%	}
                            // If the order line has a CurrentWorkOrderKey, the icon will link directly to work order details
                            String linkForWorkOrder = "";
                            String keyNameForLink = "";
							String currentWorkOrderKey = getValue("OrderLine", "xml:/OrderLine/@CurrentWorkOrderKey");
                            if (!isVoid(currentWorkOrderKey)) {
                                linkForWorkOrder = "L15";
                                keyNameForLink = "workOrderKey";
                        %>
                                <yfc:makeXMLInput name="workOrderKey">
                                    <yfc:makeXMLKey binding="xml:/WorkOrder/@WorkOrderKey" value="xml:/OrderLine/@CurrentWorkOrderKey"/>
                                </yfc:makeXMLInput>
                        <%
                            } else {
                                // If the order line has other work orders (without current work order key), the icon will
                                // link to the line-level work order list screen.
                                String numberOfWorkOrders = getValue("OrderLine","xml:/OrderLine/WorkOrders/@NumberOfWorkOrders");
							    if (!isVoid(numberOfWorkOrders)) {
                                    int numberOfWorkOrdersInt = (new Integer(numberOfWorkOrders)).intValue();	
                                    if (numberOfWorkOrdersInt > 0) {
                                        linkForWorkOrder = "L14";
                                        keyNameForLink = "orderLineKey";
                                    }
                                }
                            }
                            if (!isVoid(linkForWorkOrder) && !equals(getValue("Order","xml:/Order/@isHistory"),"Y")) {
                        %>
                                <a <%=getDetailHrefOptions(linkForWorkOrder, getParameter(keyNameForLink), "")%>>
                                    <img class="columnicon" <%=getImageOptions(request.getContextPath() + "/console/icons/workorders.gif", "View_Work_Orders")%>>
                                </a>
						<%  } %>
					</td>
					<td class="tablecolumn" sortValue="<%=getNumericValue("xml:OrderLine:/OrderLine/@PrimeLineNo")%>">
						<% if(showOrderLineNo("Order","Order")) {%>
							<a <%=getDetailHrefOptions("L03", getParameter("orderLineKey"), "")%>>
								<yfc:getXMLValue binding="xml:/OrderLine/@PrimeLineNo"/></a>
						<%} else {%>
							<yfc:getXMLValue binding="xml:/OrderLine/@PrimeLineNo"/>
						<%}%>
					</td>
					<td class="tablecolumn"><yfc:getXMLValue binding="xml:/OrderLine/Item/@ItemID"/></td>
					<td class="tablecolumn"><yfc:getXMLValue binding="xml:/OrderLine/Item/@ProductClass"/></td>
                    <td class="tablecolumn">
                        <yfc:getXMLValue binding="xml:/OrderLine/OrderLineTranQuantity/@TransactionalUOM"/>
                        <input type="hidden" <%=getTextOptions("xml:/Order/OrderLines/OrderLine_" + OrderLineCounter + "/OrderLineTranQuantity/@TransactionalUOM", "xml:/OrderLine/OrderLineTranQuantity/@TransactionalUOM")%> />
                    </td>
					<td class="tablecolumn"><%=getLocalizedOrderLineDescription("OrderLine")%></td>
					<td class="tablecolumn" nowrap="true">
                    <input type="text"  <%if(bAppendOldValue) { %>OldValue="<%=resolveValue("xml:OrigAPIOrderLine:/OrderLine/@ReceivingNode")%>"  <%}%> <%=yfsGetTextOptions("xml:/Order/OrderLines/OrderLine_" + OrderLineCounter + "/@ReceivingNode", "xml:/OrderLine/@ReceivingNode", "xml:/OrderLine/AllowedModifications")%>/>
						<img class="lookupicon" onclick="callLookup(this,'shipnode')" <%=yfsGetImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Receiving_Node", "xml:/OrderLine/@ReceivingNode", "xml:/OrderLine/AllowedModifications")%>/>
					</td>
					<td class="tablecolumn" nowrap="true">
                    <input type="text"   <%if(bAppendOldValue) { %>OldValue="<%=resolveValue("xml:OrigAPIOrderLine:/OrderLine/@ShipNode")%>"  <%}%> <%=yfsGetTextOptions("xml:/Order/OrderLines/OrderLine_" + OrderLineCounter + "/@ShipNode", "xml:/OrderLine/@ShipNode", "xml:/OrderLine/AllowedModifications")%>/>
						<img class="lookupicon" onclick="callLookup(this,'shipnode')" <%=yfsGetImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Ship_Node", "xml:/OrderLine/@ShipNode", "xml:/OrderLine/AllowedModifications")%>/>
					</td>
					<td class="tablecolumn" nowrap="true">
						<% if (equals(driverDate, "02")) { %>
                        <input type="text"  <%if(bAppendOldValue) { %> OldValue="<%=resolveValue("xml:OrigAPIOrderLine:/OrderLine/@ReqDeliveryDate")%>"  <%}%> <%=yfsGetTextOptions("xml:/Order/OrderLines/OrderLine_" + OrderLineCounter + "/@ReqDeliveryDate", "xml:/OrderLine/@ReqDeliveryDate", "xml:/OrderLine/AllowedModifications")%>/>
							<img class="lookupicon" onclick="invokeCalendar(this);return false" <%=yfsGetImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar", "xml:/OrderLine/@ReqDeliveryDate", "xml:/OrderLine/AllowedModifications")%>/>
						<% } else { %>
                        <input type="text"  <%if(bAppendOldValue) { %> OldValue="<%=resolveValue("xml:OrigAPIOrderLine:/OrderLine/@ReqShipDate")%>"  <%}%> <%=yfsGetTextOptions("xml:/Order/OrderLines/OrderLine_" + OrderLineCounter + "/@ReqShipDate", "xml:/OrderLine/@ReqShipDate", "xml:/OrderLine/AllowedModifications")%>/>
							<img class="lookupicon" onclick="invokeCalendar(this);return false" <%=yfsGetImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar", "xml:/OrderLine/@ReqShipDate", "xml:/OrderLine/AllowedModifications")%>/>
						<% } %>
					</td>
					<td class="numerictablecolumn">
                    <input type="text"  <%if(bAppendOldValue) { %> OldValue="<%=resolveValue("xml:OrigAPIOrderLine:/OrderLine/OrderLineTranQuantity/@OrderedQty")%>"  <%}%> <%=yfsGetTextOptions("xml:/Order/OrderLines/OrderLine_" + OrderLineCounter + "/OrderLineTranQuantity/@OrderedQty", "xml:/OrderLine/OrderLineTranQuantity/@OrderedQty", "xml:/OrderLine/AllowedModifications")%> style='width:40px' title='<%=getI18N("Open_Qty")%>: <%=resolveValue("xml:OrigAPIOrderLine:/OrderLine/OrderLineTranQuantity/@OrderedQty")%>'/>
					</td>
					<td class="numerictablecolumn" nowrap="true" sortValue="<%=getNumericValue("xml:OrderLine:/OrderLine/LineOverallTotals/@LineTotal")%>">
						<% String[] curr0 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr0[0]%>&nbsp;<yfc:getXMLValue binding="xml:/OrderLine/LineOverallTotals/@LineTotal"/>&nbsp;<%=curr0[1]%>
					</td>
					<td class="tablecolumn">
						<a <%=getDetailHrefOptions("L04", getParameter("orderLineKey"),"ShowReleaseNo=Y")%>><%=displayOrderStatus(getValue("OrderLine","xml:/OrderLine/@MultipleStatusesExist"),getValue("OrderLine","xml:/OrderLine/@MaxLineStatusDesc"),true)%></a>
					</td>
				</tr>
                <%}
            } else if(isVoid(resolveValue("xml:OrderLine:/OrderLine/@OrderLineKey"))) {%>
					<tr DeleteRowIndex="<%=OrderLineCounter%>">
						<td class="checkboxcolumn"> 
							<img class="icon" onclick="setDeleteOperationForRow(this,'xml:/Order/OrderLines/OrderLine')" <%=getImageOptions(YFSUIBackendConsts.DELETE_ICON, "Remove_Row")%>/>
					</td>
                    <td class="tablecolumn">
						<input type="hidden" OldValue="" <%=getTextOptions("xml:/Order/OrderLines/OrderLine_"+OrderLineCounter+"/@Action", "xml:/Order/OrderLines/OrderLine_"+OrderLineCounter+"/@Action", "CREATE")%> />
						<input type="hidden"  <%=getTextOptions("xml:/Order/OrderLines/OrderLine_"+OrderLineCounter+"/@DeleteRow",  "")%> />
                    </td>
                    <td class="tablecolumn">&nbsp;</td>
                    <td class="tablecolumn" nowrap="true">
						<input type="text" OldValue="" <%=yfsGetTemplateRowOptions("xml:/Order/OrderLines/OrderLine_"+OrderLineCounter+"/Item/@ItemID","xml:/OrderLine/Item/@ItemID","xml:/Order/AllowedModifications","ADD_LINE","text")%> />		
						<img class="lookupicon" onclick="templateRowCallItemLookup(this,'ItemID','ProductClass','TransactionalUOM','item','<%=extraParams%>')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Item")%> />
                    </td>
                    <td class="tablecolumn">
		                <select OldValue="" <%=yfsGetTemplateRowOptions("xml:/Order/OrderLines/OrderLine_"+OrderLineCounter+"/Item/@ProductClass","xml:/OrderLine/Item/@ProductClass", "xml:/Order/AllowedModifications","ADD_LINE","combo")%>>
                            <yfc:loopOptions binding="xml:ProductClassList:/CommonCodeList/@CommonCode" name="CodeValue" 
		                    value="CodeValue" selected="xml:/OrderLine/Item/@ProductClass"/>
                        </select>
                    </td>
                    <td class="tablecolumn">
		                <select OldValue="" <%=yfsGetTemplateRowOptions("xml:/Order/OrderLines/OrderLine_"+OrderLineCounter+"/OrderLineTranQuantity/@TransactionalUOM","xml:/OrderLine/OrderLineTranQuantity/@TransactionalUOM","xml:/Order/AllowedModifications","ADD_LINE","combo")%>>
                           <yfc:loopOptions binding="xml:UnitOfMeasureList:/ItemUOMMasterList/@ItemUOMMaster" name="UnitOfMeasure"
		                    value="UnitOfMeasure" selected="xml:/OrderLine/OrderLineTranQuantity/@TransactionalUOM"/>
                        </select>
                    </td>
                    <td class="tablecolumn">&nbsp;</td>
                    <td class="tablecolumn" nowrap="true">
						<input type="text" OldValue="" <%=yfsGetTemplateRowOptions("xml:/Order/OrderLines/OrderLine_"+OrderLineCounter+"/@ReceivingNode","xml:/OrderLine/@ReceivingNode","xml:/Order/AllowedModifications","ADD_LINE","text")%> />
                        <img class="lookupicon" onclick="callLookup(this,'shipnode')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Recieving_Node")%>/>
                    </td>
                    <td class="tablecolumn" nowrap="true">
						<input type="text" OldValue="" <%=yfsGetTemplateRowOptions("xml:/Order/OrderLines/OrderLine_"+OrderLineCounter+"/@ShipNode","xml:/OrderLine/@ShipNode","xml:/Order/AllowedModifications","ADD_LINE","text")%>/>
                        <img class="lookupicon" onclick="callLookup(this,'shipnode')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Ship_Node")%>/>
                    </td> 
                    <td class="tablecolumn" nowrap="true">
                       <% if (equals(driverDate, "02")) { %>
							<input type="text" OldValue="" <%=yfsGetTemplateRowOptions("xml:/Order/OrderLines/OrderLine_"+OrderLineCounter+"/@ReqDeliveryDate","xml:/OrderLine/@ReqDeliveryDate","xml:/Order/AllowedModifications","ADD_LINE","text")%>/>
                           <img class="lookupicon" onclick="invokeCalendar(this)" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar")%>/>
                       <% } else { %>
							<input type="text" OldValue="" <%=yfsGetTemplateRowOptions("xml:/Order/OrderLines/OrderLine_"+OrderLineCounter+"/@ReqShipDate","xml:/OrderLine/@ReqShipDate","xml:/Order/AllowedModifications","ADD_LINE","text")%>/>
                           <img class="lookupicon" onclick="invokeCalendar(this)" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar")%>/>
                       <% } %>
                    </td>             
                    <td class="numerictablecolumn">
						<input type="text" OldValue="" 			<%=yfsGetTemplateRowOptions("xml:/Order/OrderLines/OrderLine_"+OrderLineCounter+"/OrderLineTranQuantity/@OrderedQty","xml:/OrderLine/OrderLineTranQuantity/@OrderedQty","xml:/Order/AllowedModifications","ADD_LINE","text")%> style='width:40px'/>
                    </td>
                    <td class="tablecolumn">&nbsp;</td>
                    <td class="tablecolumn">&nbsp;</td>
                </tr>
            <%}%>
        </yfc:loopXML>
    </tbody>
    <tfoot>        
		<%if (isModificationAllowed("xml:/@AddLine","xml:/Order/AllowedModifications")) { %>
        <tr>
        	<td nowrap="true" colspan="13">
        		<jsp:include page="/common/editabletbl.jsp" flush="true">
                <jsp:param name="ReloadOnAddLine" value="Y"/>
        		</jsp:include>
        	</td>
        </tr>
        <%}%>
    </tfoot>
</table>
