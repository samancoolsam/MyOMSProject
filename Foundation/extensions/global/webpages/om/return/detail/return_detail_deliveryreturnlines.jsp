<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.shared.inv.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/om.js"></script>

<script language="javascript">
    document.body.attachEvent("onunload", processSaveRecordsForOrderLines);
</script>

<%
   String extraParams = getExtraParamsForTargetBinding("xml:/Item/@CallingOrganizationCode", getValue("Order", "xml:/Order/@EnterpriseCode"));
%>
<table class="table" ID="OrderLines" cellspacing="0" width="100%" <%if (isModificationAllowed("xml:/@AddLine","xml:/Order/AllowedModifications")) {%> initialRows="8" <%}%> >
    <thead>
        <tr>
            <td class="checkboxheader" sortable="no">
                <input type="checkbox" value="checkbox" name="checkbox" onclick="doCheckAll(this);"/>
            </td>
            <td class="tablecolumnheader" nowrap="true" style="width:30px">&nbsp;</td>
            <td class="tablecolumnheader" nowrap="true" style="width:<%=getUITableSize("xml:/OrderLine/@PrimeLineNo")%>"><yfc:i18n>Line</yfc:i18n></td>
			<%if (isTrue("xml:/Order/@HasDerivedParent")) {%>
	            <td class="tablecolumnheader" nowrap="true" style="width:<%=getUITableSize("xml:/OrderLine/DerivedFromOrderHeader/@OrderNo")%>"><yfc:i18n>Order_#_Line_#</yfc:i18n></td>
			<%}%>
            <td class="tablecolumnheader" nowrap="true" style="width:<%=getUITableSize("xml:/OrderLine/Item/@ItemID")%>"><yfc:i18n>Item_ID</yfc:i18n></td>
            <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/OrderLine/Item/@ItemShortDesc")%>"><yfc:i18n>Description</yfc:i18n></td>
            <td class="tablecolumnheader" nowrap="true" sortable="no" style="width:<%=getUITableSize("xml:/OrderLine/@PromisedApptStartDate")%>"><yfc:i18n>Appointment</yfc:i18n></td>
			<td class="tablecolumnheader" nowrap="true" sortable="no" style="width:<%=getUITableSize("xml:/OrderLine/@ShipNode")%>"><yfc:i18n>Ship_Node</yfc:i18n></td>
			<td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/OrderLine/LineOverallTotals/@LineTotal")%>"><yfc:i18n>Amount</yfc:i18n></td>
            <td class="tablecolumnheader" ><yfc:i18n>Status</yfc:i18n></td>
        </tr>
    </thead>
    <tbody>
        <yfc:loopXML name="Order" binding="xml:/Order/OrderLines/@OrderLine" id="OrderLine">
		<% if (equals(getValue("OrderLine","xml:/OrderLine/@ItemGroupCode"), INVConstants.ITEM_GROUP_CODE_DELIVERY) )
		{%>
            <tr>
                <yfc:makeXMLInput name="orderLineKey">
                    <yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderLineKey" value="xml:/OrderLine/@OrderLineKey"/>
					<yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderHeaderKey" value="xml:/Order/@OrderHeaderKey"/>
                </yfc:makeXMLInput>

                <yfc:makeXMLInput name="derivedFromLineKey">
                    <yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderLineKey" value="xml:/OrderLine/@DerivedFromOrderLineKey"/>
                    <yfc:makeXMLKey binding="xml:/OrderLineDetail/@PrimeLineNo" value="xml:/OrderLine/DerivedFromOrderLine/@PrimeLineNo"/>
                    <yfc:makeXMLKey binding="xml:/OrderLineDetail/@SubLineNo" value="xml:/OrderLine/DerivedFromOrderLine/@SubLineNo"/>
                </yfc:makeXMLInput>
                <yfc:makeXMLInput name="derivedFromOrderKey">
                    <yfc:makeXMLKey binding="xml:/Order/@OrderHeaderKey" value="xml:/OrderLine/@DerivedFromOrderHeaderKey"/>
                </yfc:makeXMLInput>

                <td class="checkboxcolumn" >
                    <input type="checkbox" value='<%=getParameter("orderLineKey")%>' name="chkEntityKey"
				<% 
					if( !showOrderLineNo("Order","Order") ){%> disabled="true" <%}%>
					/>
                    <%/*This hidden input is required by yfc to match up each line attribute that is editable in this row 
                        against the appropriate order line # on the server side once you save.  */%>
                    <input type="hidden" <%=getTextOptions("xml:/Order/OrderLines/OrderLine_" + OrderLineCounter + "/@PrimeLineNo", "xml:/OrderLine/@PrimeLineNo")%> />
                    <input type="hidden" <%=getTextOptions("xml:/Order/OrderLines/OrderLine_" + OrderLineCounter + "/@SubLineNo", "xml:/OrderLine/@SubLineNo")%> />
                </td>
                <td class="tablecolumn" nowrap="true">
                    <% if( ! equals("Y", getValue("Order","xml:/Order/@isHistory")) ) { %>
                        <yfc:hasXMLNode binding="xml:/OrderLine/Exceptions/Exception">
                            <a <%=getDetailHrefOptions("L05", getParameter("orderLineKey"), "")%>>
                                <img class="columnicon" <%=getImageOptions(YFSUIBackendConsts.YANTRA_TITLE_ALERT_EXCEPTIONS, "Exceptions")%>></a>
                        </yfc:hasXMLNode>
                    <% } %>
                    <yfc:hasXMLNode binding="xml:/OrderLine/KitLines/KitLine">
                        <a <%=getDetailHrefOptions("L06", getParameter("orderLineKey"), "")%>>
                            <img class="columnicon" <%=getImageOptions(YFSUIBackendConsts.KIT_COMPONENTS_COLUMN, "Kit_Components")%>></a>
                    </yfc:hasXMLNode>
                    <% if (equals(getValue("OrderLine","xml:/OrderLine/@HasDerivedChild"),"Y") || (!isVoid(getValue("OrderLine","xml:/OrderLine/@DerivedFromOrderLineKey"))) || equals(getValue("OrderLine","xml:/OrderLine/@HasChainedChild"),"Y") || (!isVoid(getValue("OrderLine","xml:/OrderLine/@ChainedFromOrderLineKey")))) { %>                        
                        <a <%=getDetailHrefOptions("L09", getParameter("orderLineKey"), "")%>>
                            <img class="columnicon" <%=getImageOptions(YFSUIBackendConsts.CHAINED_ORDERLINES_COLUMN, "Related_Order_Lines")%>></a>
                    <% }
                       String currentWorkOrderKey = getValue("OrderLine", "xml:/OrderLine/@CurrentWorkOrderKey");
                       if(!isVoid(currentWorkOrderKey) && !equals("Y", resolveValue("xml:/Order/@isHistory"))) {
                    %>
                            <yfc:makeXMLInput name="workOrderKey">
                                <yfc:makeXMLKey binding="xml:/WorkOrder/@WorkOrderKey" value="xml:/OrderLine/@CurrentWorkOrderKey"/>
                            </yfc:makeXMLInput>
                            <a <%=getDetailHrefOptions("L11", getParameter("workOrderKey"), "")%>>
                                <img class="columnicon" <%=getImageOptions(request.getContextPath() + "/console/icons/workorders.gif", "View_Work_Order")%>>
                            </a>
                    <% } %>
                </td>
                <td class="tablecolumn" sortValue="<%=getNumericValue("xml:OrderLine:/OrderLine/@PrimeLineNo")%>">
					<% if(showOrderLineNo("Order","Order")) {%>
	                    <a <%=getDetailHrefOptions("L01", getParameter("orderLineKey"), "")%>>
		                    <yfc:getXMLValue binding="xml:/OrderLine/@PrimeLineNo"/>
						</a>
					<%} else {%>
						<yfc:getXMLValue binding="xml:/OrderLine/@PrimeLineNo"/>
					<%}%>
                </td>

			<%if (isTrue("xml:/Order/@HasDerivedParent")) {%>
                    <td class="tablecolumn">
					<%if (! isVoid(getValue("OrderLine", "xml:/OrderLine/DerivedFromOrder/@OrderNo"))) {%>
						<% if(showOrderNo("OrderLine","DerivedFromOrder")) {%>
		                    <a <%=getDetailHrefOptions("L02", getParameter("derivedFromOrderKey"), "")%>>
								<yfc:getXMLValue binding="xml:/OrderLine/DerivedFromOrder/@OrderNo"/>
							</a>
						<%} else {%>
							<yfc:getXMLValue binding="xml:/OrderLine/DerivedFromOrder/@OrderNo"/>
						<%}%>
					<%}%>
                    </td>
               <%}%>
                <td class="tablecolumn"><yfc:getXMLValue binding="xml:/OrderLine/Item/@ItemID"/></td>
                <td class="tablecolumn"><yfc:getXMLValue binding="xml:/OrderLine/Item/@ItemShortDesc"/></td>
				<td class="tablecolumn">
				<%-- cr 34417 --%>
				<%
					String sTimeWindow = displayTimeWindow(resolveValue("xml:/OrderLine/@PromisedApptStartDate"), resolveValue("xml:/OrderLine/@PromisedApptEndDate"), resolveValue("xml:OrderLine:/OrderLine/@Timezone") );
					
					if(!isVoid(sTimeWindow) )
					{
				%>
						<%=sTimeWindow%>
						<%=showTimeZoneIcon(resolveValue("xml:OrderLine:/OrderLine/@Timezone"), getLocale().getTimezone())%>
				<%	} %>
				</td>
                <td class="tablecolumn" nowrap="true">
                    <input type="text" <%=yfsGetTextOptions("xml:/Order/OrderLines/OrderLine_" + OrderLineCounter + "/@ShipNode", "xml:/OrderLine/@ShipNode", "xml:/OrderLine/AllowedModifications")%>/>
                    <img class="lookupicon" onclick="callLookup(this,'shipnode')" <%=yfsGetImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Ship_Node", "xml:/OrderLine/@ShipNode", "xml:/OrderLine/AllowedModifications")%>/>
                </td>

				<td class="numerictablecolumn" nowrap="true" sortValue="<%=getNumericValue("xml:OrderLine:/OrderLine/LineOverallTotals/@LineTotal")%>">
                    <% String[] curr0 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr0[0]%>&nbsp;<yfc:getXMLValue binding="xml:/OrderLine/LineOverallTotals/@LineTotal"/>&nbsp;<%=curr0[1]%>
                </td>
                <td class="tablecolumn"><%=displayOrderStatus(getValue("OrderLine","xml:/OrderLine/@MultipleStatusesExist"),getValue("OrderLine","xml:/OrderLine/@MaxLineStatusDesc"),true)%></td>	<!-- cr 40096, replaced with proper i18n for cr 56554 -->
            </tr>
		<%}%>
        </yfc:loopXML>
    </tbody>
</table>
