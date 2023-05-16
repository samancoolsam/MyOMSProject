<%
/* 
 * Licensed Materials - Property of IBM
 * IBM Sterling Selling and Fulfillment Suite
 * (C) Copyright IBM Corp. 2001, 2013 All Rights Reserved.
 * US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */
%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<table class="table" editable="false" width="100%" cellspacing="0">
    <thead> 
        <tr>
            <td sortable="no" class="checkboxheader">
                <input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
            </td>
            <td class="tablecolumnheader"><yfc:i18n>Alert_ID</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Type</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Description</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Queue</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Priority</yfc:i18n></td>
			<td class="tablecolumnheader"><yfc:i18n>Status</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Owner</yfc:i18n></td>
			<td class="tablecolumnheader"><yfc:i18n>Last_Raised_On</yfc:i18n></td>
			<td class="tablecolumnheader"><yfc:i18n>Raised_Count</yfc:i18n></td>
        </tr>
    </thead>
    <tbody>
        <yfc:loopXML binding="xml:/InboxList/@Inbox" id="Inbox">
			<%
                String activeFlag = getValue("Inbox","xml:/Inbox/@ActiveFlag");
                String status = "";
                if ("Y".equals(activeFlag) ) {
                    status = "Open";
                } else if ( "N".equals(activeFlag) ) {
                    status = "Closed";
                }
            %>
            <tr>
                <yfc:makeXMLInput name="inboxKey">
                    <yfc:makeXMLKey binding="xml:/Inbox/@InboxKey" value="xml:/Inbox/@InboxKey"/>
                </yfc:makeXMLInput>
                <td> 
                    <input type="checkbox" value='<%=getParameter("inboxKey")%>' name="chkEntityKey"/>
                </td>
                <td class="tablecolumn">
                    <a <%=getDetailHrefOptions("L01", getParameter("inboxKey"),"")%> >
                        <yfc:getXMLValue binding="xml:/Inbox/@InboxKey"/>
                    </a>
                </td>
                <td class="tablecolumn"><yfc:getXMLValue binding="xml:/Inbox/@ExceptionType"/></td>
				<%
					String description= getValue("Inbox","xml:/Inbox/@Description");
				%>
                <td class="tablecolumn"><yfc:i18n><%=description%></yfc:i18n></td>
                <td class="tablecolumn"><yfc:getXMLValue binding="xml:/Inbox/Queue/@QueueId"/></td>
                <td class="tablecolumn" sortValue="<%=getNumericValue("xml:Inbox:/Inbox/@Priority")%>"><yfc:getXMLValue binding="xml:/Inbox/@Priority"/></td>
				<td class="protectedtext"><yfc:i18n><%=status%></yfc:i18n></td>
                <td class="tablecolumn"><yfc:getXMLValue binding="xml:/Inbox/User/@Loginid"/></td>
                <td class="tablecolumn" sortValue="<%=getDateValue("xml:Inbox:/Inbox/@LastOccurredOn")%>"><yfc:getXMLValue binding="xml:/Inbox/@LastOccurredOn"/></td>
                <td class="tablecolumn" sortValue="<%=getNumericValue("xml:/Inbox/@ConsolidationCount")%>"><yfc:getXMLValue binding="xml:/Inbox/@ConsolidationCount"/></td>
            </tr>
        </yfc:loopXML>
   </tbody>
</table>