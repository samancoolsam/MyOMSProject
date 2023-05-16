<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ page import="com.yantra.yfc.core.*" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/modificationreason.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/exceptionutils.js"></script>

<script language="javascript">
    function showAssignToUserPopup(viewId) {
        return doEMPopupDialog('<%=getDefaultDetailViewForGroup("YEMD011")%>',635,180);
    }
</script>
<script language="javascript">
    function showAssignToQueuePopup(viewId) {
        return doEMPopupDialog('<%=getDefaultDetailViewForGroup("YEMD012")%>',635,180);
    }
</script>

<%
    String modifyView = request.getParameter("ModifyView");
    modifyView = modifyView == null ? "" : modifyView;
	String activeFlag = getValue("Inbox","xml:/Inbox/@ActiveFlag");
	String	resolvedBy = "";
	
	if ( "N".equals(activeFlag) ) {
		resolvedBy = getValue("Inbox","xml:/Inbox/@Modifyuserid");
	}
%>
<%  if(!isVoid(resolveValue("xml:/Inbox/@OrderHeaderKey") ) ) { %>
        <yfc:callAPI apiID="AP1"/>
<%  } %>

<yfc:makeXMLInput name="orderKey">
	<yfc:makeXMLKey binding="xml:/Order/@OrderHeaderKey" value="xml:/Inbox/@OrderHeaderKey"/>
</yfc:makeXMLInput>

<table class="view" width="100%" >
    <tr>
    <td><yfc:i18n>Alert_ID</yfc:i18n></td>
		<yfc:makeXMLInput name="shipmentSearchKey">
			<yfc:makeXMLKey binding="xml:/Shipment/@HasNodeException" value="Y"/>
		</yfc:makeXMLInput>
        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Inbox/@InboxKey"/></td>
<%	if (YFCCommon.equals(resolveValue("xml:/Inbox/@ExceptionType"),"SHORTAGES_DETECTED")) {%>
    <td><yfc:i18n>Alert_Type</yfc:i18n></td>
	<td>
		<a onclick="yfcShowListPopupWithParams('YOMS750','',1100,900,new Object(),'shipment','&xml:/Shipment/@HasNodeExceptions=Y&xml:/SearchData/@MaxRecords=30');return false;" href=""><yfc:getXMLValue binding="xml:/Inbox/@ExceptionType"/></a>
    </td>
<%	} else {	%>
<%
			String alertType = getValue("Inbox","xml:/Inbox/@ExceptionType"); //above if block code is related to SC. risky to make modification there.
%>
    <td><yfc:i18n>Alert_Type</yfc:i18n></td>
        <td class="protectedtext"><yfc:i18n><%=alertType%></yfc:i18n></td>
	</td>
<%	}	%> 

<%
	String description= getValue("Inbox","xml:/Inbox/@Description");
%>
    <td><yfc:i18n>Description</yfc:i18n></td>
        <td class="protectedtext"><yfc:i18n><%=description%></yfc:i18n></td>
    </tr>
    <tr>
    <td><yfc:i18n>Queue</yfc:i18n></td>
        <td class="protectedtext"><yfc:getXMLValueI18NDB binding="xml:/Inbox/Queue/@QueueDescription"/></td>
    <td><yfc:i18n>Assigned_To_User</yfc:i18n></td>
        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Inbox/User/@Loginid"/></td>
		<%
        String alertStatus = getValue("Inbox","xml:/Inbox/@Status");
        
        if (YFCObject.isVoid(alertStatus)) {
            alertStatus = "OPEN";
        }
        alertStatus = "ALERT_STATUS_" + alertStatus;
        %>

    <td><yfc:i18n>Alert_Status</yfc:i18n></td>
        <td class="protectedtext"><yfc:i18n><%=alertStatus%></yfc:i18n></td>
    </tr>
    <tr>
        
    <td><yfc:i18n>Ship_Node</yfc:i18n></td>
        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Inbox/@ShipnodeKey"/></td>
        
    <td><yfc:i18n>Order_No</yfc:i18n></td>
        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Inbox/@OrderNo"/></td>
        
    <td><yfc:i18n>Order_Status</yfc:i18n></td>
        <td class="protectedtext"><%=displayOrderStatus(getValue("Order","xml:/Order/@MultipleStatusesExist"),getValue("Order","xml:/Order/@MaxOrderStatusDesc"),true)%></td>
    </tr>
    <tr>
    <td><yfc:i18n>Supplier</yfc:i18n></td>
        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Inbox/@SupplierKey"/></td>

    <td><yfc:i18n>Item_ID</yfc:i18n></td>
        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Inbox/@ItemId"/></td>

    <td><yfc:i18n>Raised_By_Transaction</yfc:i18n></td>
	<td class="protectedtext">
	    <%=getComboText("xml:/TransactionList/@Transaction", "Tranname", "Tranid", "xml:/Inbox/@InboxType", true)%>
	</td>
        
    </tr>
    <tr>
        <td><yfc:i18n>First_Raised_On</yfc:i18n></td>
        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Inbox/@GeneratedOn"/></td>
        <td><yfc:i18n>Last_Raised_On</yfc:i18n></td>
        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Inbox/@LastOccurredOn"/></td>
        <td><yfc:i18n>Raised_Count</yfc:i18n></td>
        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Inbox/@ConsolidationCount"/></td>
    </tr>
    <tr>
        <td><yfc:i18n>Resolved_On</yfc:i18n></td>
        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Inbox/@ResolutionDate"/></td>
        <td><yfc:i18n>Resolved_By</yfc:i18n></td>
        <td class="protectedtext"><%=resolvedBy%></td>
		<td><yfc:i18n>Enterprise_Key</yfc:i18n></td>
        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Inbox/@EnterpriseKey"/></td>
    </tr>
    <tr>
		<%	
            boolean bDetcontainsScript = false;
            YFCElement elemTemp = getElement("Inbox");
            String sDetailDesc = elemTemp.getAttribute("DetailDescription");
            if ( sDetailDesc.toUpperCase().indexOf("<SCRIPT") != -1 ){
                bDetcontainsScript = true;
            }
			//START MPMT-121 : PayZ
            String strExceptionType = getValue("Inbox","xml:/Inbox/@ExceptionType");
            String strAuthReturnMsg = "";
            //END MPMT-121 : PayZ
        %>
        <td><yfc:i18n>Details</yfc:i18n></td>
        <% if ( bDetcontainsScript ) { %>
            <td class="protectedtext" colspan=3><yfc:getXMLValue binding="xml:/Inbox/@DetailDescription"/></td>
            
            <!--START MPMT-121 : PayZ -->
        <% } else if ( ( strExceptionType.equalsIgnoreCase("PAYMENT_SRVC_UNAVAIL") || strExceptionType.equalsIgnoreCase("PAYMENT_HARD_DECLINED") )
        				&& sDetailDesc.indexOf("AuthReturnMessage=") != -1 ) {
        				 
        		YFCElement paymentFailEle = YFCDocument.parse(sDetailDesc).getElementsByTagName("CollectionFailureDetails").item(0);
				strAuthReturnMsg = paymentFailEle.getAttribute("AuthReturnMessage");	
        %>  
            <td class="protectedtext" colspan=3 ><%=strAuthReturnMsg%></td>
            <!--END MPMT-121 : PayZ -->            
        <% } else { %>
            <td class="protectedtext" colspan=3 ><%=sDetailDesc%></td>
        <% } %>
    </tr>
</table>
