<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ page import="com.yantra.yfc.core.*" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>

<table class="table" width="100%" >
  <thead> 
  <tr> 
    <td class="tablecolumnheader" true style="width:<%=getUITableSize("xml:/InboxAudit/@Createts")%>"><yfc:i18n>Date</yfc:i18n></td>
    <td class="tablecolumnheader" true style="width:<%=getUITableSize("xml:/InboxAudit/@TransactionType")%>"><yfc:i18n>Transaction_Type</yfc:i18n></td>
    <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/InboxAudit/@FromUserid")%>"><yfc:i18n>From_User</yfc:i18n></td>
    <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/InboxAudit/@ToUserid")%>"><yfc:i18n>To_User</yfc:i18n></td>
    <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/InboxAudit/@FromQueueId")%>"><yfc:i18n>From_Queue(Organization)</yfc:i18n></td>
    <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/InboxAudit/@ToQueueId")%>"><yfc:i18n>To_Queue(Organization)</yfc:i18n></td>
    <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/InboxAudit/@FromStatus")%>"><yfc:i18n>From_Status</yfc:i18n></td>
    <td class="tablecolumnheader" style="width:<%=getUITableSize("xml:/InboxAudit/@ToStatus")%>"><yfc:i18n>To_Status</yfc:i18n></td>
  </tr>
  </thead> <tbody> <yfc:loopXML  binding="xml:/Inbox/InboxAuditList/@InboxAudit" id="InboxAudit"> 
  <tr> 
    <td class="tablecolumn" sortValue="<%=getDateValue("xml:/InboxAudit/@Createts")%>"><yfc:getXMLValue binding="xml:/InboxAudit/@Createts"/></td>
    <td class="tablecolumn"><yfc:getXMLValue binding="xml:/InboxAudit/@TransactionType"/></td>
    <td class="tablecolumn"><yfc:getXMLValue binding="xml:/InboxAudit/@FromUserid"/></td>
    <td class="tablecolumn"><yfc:getXMLValue binding="xml:/InboxAudit/@ToUserid"/></td>
	<% if (!isVoid(getValue("InboxAudit","xml:/InboxAudit/@FromQueueId"))) { %>
    	<td class="tablecolumn"><yfc:getXMLValue binding="xml:/InboxAudit/@FromQueueId"/><yfc:i18n>(</yfc:i18n><yfc:getXMLValue binding="xml:/InboxAudit/@FromQueueOwnerKey"/><yfc:i18n>)</yfc:i18n></td>
	<% } else{ %>
    	<td class="tablecolumn"><yfc:getXMLValue binding="xml:/InboxAudit/@FromQueueId"/></td>
	<% } %>
	<% if (!isVoid(getValue("InboxAudit","xml:/InboxAudit/@ToQueueId"))) { %>
    	<td class="tablecolumn"><yfc:getXMLValue binding="xml:/InboxAudit/@ToQueueId"/><yfc:i18n>(</yfc:i18n><yfc:getXMLValue binding="xml:/InboxAudit/@ToQueueOwnerKey"/><yfc:i18n>)</yfc:i18n></td>
	<% } else{ %>
    	<td class="tablecolumn"><yfc:getXMLValue binding="xml:/InboxAudit/@ToQueueId"/></td>
	<% } %>
    <%
    String fromStatus = getValue("InboxAudit","xml:/InboxAudit/@FromStatus");
    if ((YFCObject.equals(fromStatus,"OPEN"))||YFCObject.equals(fromStatus,"WIP")||YFCObject.equals(fromStatus,"CLOSED")) {
        fromStatus = "ALERT_STATUS_" + fromStatus;
    }

    String toStatus = getValue("InboxAudit","xml:/InboxAudit/@ToStatus");
    if ((YFCObject.equals(toStatus,"OPEN"))||YFCObject.equals(toStatus,"WIP")||YFCObject.equals(toStatus,"CLOSED")) {
        toStatus = "ALERT_STATUS_" + toStatus;
    }
    %>

    <td class="tablecolumn"><yfc:i18n><%=fromStatus%></yfc:i18n></td>
    <td class="tablecolumn"><yfc:i18n><%=toStatus%></yfc:i18n></td>
  </tr>
  </yfc:loopXML> </tbody> 
</table>
