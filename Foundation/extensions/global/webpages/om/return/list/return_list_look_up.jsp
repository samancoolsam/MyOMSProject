<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
	
	<table  editable="false" class="table">
		<thead>
			<tr>
			<td class="tablecolumnheader" sortable="no">&nbsp;</td>
			<td class="tablecolumnheader"><yfc:i18n>Return_#</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Status</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Enterprise</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Buyer</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Order_Date</yfc:i18n></td>
			</tr>
		</thead>
		<tbody>
			<yfc:loopXML binding="xml:/OrderList/@Order" id="Order">
				<tr>
					<td class="tablecolumn">
				 <yfc:makeXMLInput name="orderKey">
                    <yfc:makeXMLKey binding="xml:/Order/@OrderNo" value="xml:/Order/@OrderNo" />
               </yfc:makeXMLInput>                
							<img class="icon"  onClick="setLookupValue(this.value)"  value='<%=resolveValue("xml:/Order/@OrderNo")%>' <%=getImageOptions(YFSUIBackendConsts.GO_ICON, "Click_to_Select")%>/>
					</td>
					 <td class="tablecolumn">
                    <yfc:getXMLValue binding="xml:/Order/@OrderNo"/>
                </td>
                <td class="tablecolumn">
                    <% if (isVoid(getValue("Order", "xml:/Order/@Status"))) { %>
                        [<yfc:i18n>Draft</yfc:i18n>]
                    <% } else { %>
                        <%=displayOrderStatus(getValue("Order","xml:/Order/@MultipleStatusesExist"),getValue("Order","xml:/Order/@MaxOrderStatusDesc"),true)%>
                    <% } %>
                    <% if (equals("Y", getValue("Order", "xml:/Order/@HoldFlag"))) { %>
                        <img class="icon" onmouseover="this.style.cursor='default'" <%=getImageOptions(YFSUIBackendConsts.HELD_ORDER, "This_order_is_held")%>/>
                    <% } %>
                    <% if(equals("Y", getValue("Order","xml:/Order/@isHistory") )){ %>
                        <img class="icon" onmouseover="this.style.cursor='default'" <%=getImageOptions(YFSUIBackendConsts.HISTORY_ORDER, "This_is_an_archived_order")%>/>
                    <% } %>
                </td>
                <td class="tablecolumn"><yfc:getXMLValue binding="xml:/Order/@EnterpriseCode"/></td>
                <td class="tablecolumn"><yfc:getXMLValue binding="xml:/Order/@BuyerOrganizationCode"/></td>
                <td class="tablecolumn"><yfc:getXMLValue binding="xml:/Order/@OrderDate"/></td>
				</tr>
			</yfc:loopXML>
		</tbody>
	</table>
