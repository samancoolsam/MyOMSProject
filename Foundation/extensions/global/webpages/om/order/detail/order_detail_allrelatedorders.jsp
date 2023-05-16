<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/order.jspf" %>
<%@ page import="com.yantra.util.*" %>

<%
    String sDocumentType = getValue("DocumentType", "xml:/DocumentType/@DocumentType");
     String sOrderNoColumnHeader = getI18N(sDocumentType+"_Order_#") ;
     if(YFCUtils.equals(sOrderNoColumnHeader,sDocumentType+"_Order_#"))
     {
         //no entry exists in ycpbundle.properties.use default value
        sOrderNoColumnHeader = getI18N("0001_Order_#") ;
     }
     boolean IsReturnService = false;
     if(equals("true", getParameter("IsReturnService")) || equals("Y", getParameter("IsReturnService") ) )      {           
         IsReturnService = true;
     }
%>


<table class="table" width="100%">
    <thead>
        <tr>
            <td class="checkboxheader" sortable="no">
                <input type="checkbox" value="checkbox" name="checkbox" onclick="doCheckAll(this);"/>
            </td>
            <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/DocumentType/Order/@OrderNo")%>" sortable="no"><%=sOrderNoColumnHeader%></td>            
            <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/DocumentType/Order/@Status")%>" sortable="no"><yfc:i18n>Relationship</yfc:i18n></td>
            <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/DocumentType/Order/@Status")%>" sortable="no"><yfc:i18n>Status</yfc:i18n></td>
            <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/DocumentType/Order/@BuyerOrganizationCode")%>" sortable="no"><yfc:i18n>Buyer</yfc:i18n></td>
            <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/DocumentType/Order/@SellerOrganizationCode")%>" sortable="no"><yfc:i18n>Seller</yfc:i18n></td>
        </tr>
    </thead>
    <tbody>
    <%String className="evenrow";%>
		<yfc:loopXML name="DocumentType" binding="xml:/DocumentType/@Order" id="Order">
            <tr class='<%=className%>'>
                <yfc:makeXMLInput name="orderKey">
                    <yfc:makeXMLKey binding="xml:/Order/@OrderHeaderKey" value="xml:/Order/@OrderHeaderKey"/>
                </yfc:makeXMLInput>
                <td class="checkboxcolumn">
                    <input type="checkbox" value='<%=getParameter("orderKey")%>' name="<%=getDefaultEntityKeyName()%>" yfsTargetEntity="<%=getEntityIDForLink("L01", getValue("Order", "xml:/Order/@DocumentType"))%>"
					<% if((!showOrderNo("Order","Order")) || (isVoid(getEntityIDForLink("L01", getValue("Order", "xml:/Order/@DocumentType")))) ) {%> disabled="true" <%}%>
					/>
                </td>
                <td class="tablecolumn" style='width:150px'>
					<% if(showOrderNo("Order","Order")) { %>                        
						<a <%=getDetailHrefOptions("L01", getValue("Order", "xml:/Order/@DocumentType"), getParameter("orderKey"), "")%>>
                            <yfc:getXMLValue binding="xml:/Order/@OrderNo"/>
                        </a>

					<%} else {%>
						<yfc:getXMLValue binding="xml:/Order/@OrderNo"/>
					<%}%>
                    <%  if(IsReturnService)         {           
                        request.setAttribute("Order", pageContext.getAttribute("Order") );

                        String divToDisplay = "yfsReturnOrder_" + OrderCounter; %>
                        <img onclick="expandCollapseDetails('<%=divToDisplay%>', '<%=replaceI18N("Click_To_See_Order_Lines")%>', '<%=replaceI18N("Click_To_Hide_Order_Lines")%>', '<%=YFSUIBackendConsts.FOLDER_COLLAPSE%>', '<%=YFSUIBackendConsts.FOLDER_EXPAND%>')" style="cursor:hand" <%=getImageOptions(YFSUIBackendConsts.FOLDER_COLLAPSE,"Click_To_Hide_Order_Lines")%> />                       

                    <%}%>
                </td>
                <td class="tablecolumn" style='width:150px'><yfc:i18n><yfc:getXMLValue binding="xml:/Order/@Relationship"/></yfc:i18n></td>
                <td class="tablecolumn" style='width:150px'>
                    <a <%=getDetailHrefOptions("L02", getValue("Order", "xml:/Order/@DocumentType"), getParameter("orderKey"), "")%>>	<%=displayOrderStatus(getValue("Order","xml:/Order/@MultipleStatusesExist"),getValue("Order","xml:/Order/@MaxOrderStatusDesc"),true)%></a>
                    <% if (equals("Y", getValue("Order", "xml:/Order/@HoldFlag"))) { %>
                        <img class="icon" onmouseover="this.style.cursor='default'" <%=getImageOptions(YFSUIBackendConsts.HELD_ORDER, "This_order_is_held")%>/>
                    <% } %>
                    <% if(equals("Y", getValue("Order","xml:/Order/@isHistory") )){ %>
                        <img class="icon" onmouseover="this.style.cursor='default'" <%=getImageOptions(YFSUIBackendConsts.HISTORY_ORDER, "This_is_an_archived_order")%>/>
                    <% } %>
                </td>                                
                <td class="tablecolumn"><yfc:getXMLValue binding="xml:/Order/@BuyerOrganizationCode"/></td>
                <td class="tablecolumn"><yfc:getXMLValue binding="xml:/Order/@SellerOrganizationCode"/></td>
               <%  if(IsReturnService)         {           %>
                </tr>
                    <tr id='<%="yfsReturnOrder_"+OrderCounter%>' >
                        <td colspan="6" >
                                <jsp:include page="/om/order/detail/order_detail_return_order_lines.jsp" flush="true" >
                                        <jsp:param name="className" value="<%=className%>"/>
                                </jsp:include>
                        </td>
                <%}%>

                <%
                    if (className.equals("oddrow"))
                            className="evenrow";
                        else
                            className="oddrow";                                                 
                %>
            </tr>
        </yfc:loopXML>
   </tbody>
</table>
