<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<table class="table" width="100%">
<thead>
    <tr>
        <td class="checkboxheader" sortable="no">
            <input type="checkbox" value="checkbox" name="checkbox" onclick="doCheckAll(this);"/>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/OrderReleaseList/OrderRelease/@ReleaseNo")%>"><yfc:i18n>Release_#</yfc:i18n></td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/OrderReleaseList/OrderRelease/@ShipNode")%>"><yfc:i18n>Ship_Node</yfc:i18n></td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/OrderReleaseList/OrderRelease/@Status")%>"><yfc:i18n>Status</yfc:i18n></td>
    </tr>
</thead>
<tbody>
    <yfc:loopXML binding="xml:/OrderReleaseList/@OrderRelease" id="OrderRelease">
        <tr>
            <yfc:makeXMLInput name="orderReleaseKey">
                <yfc:makeXMLKey binding="xml:/OrderReleaseDetail/@OrderReleaseKey" value="xml:/OrderRelease/@OrderReleaseKey" />
            </yfc:makeXMLInput>
            <yfc:makeXMLInput name="statusKey">
                <yfc:makeXMLKey binding="xml:/OrderReleaseDetail/@OrderReleaseKey" value="xml:/OrderRelease/@OrderReleaseKey" />
            </yfc:makeXMLInput>
            <yfc:makeXMLInput name="shipNodeKey">
                <yfc:makeXMLKey binding="xml:/ShipNode/@ShipNode" value="xml:/OrderRelease/@ShipNode" />               
            </yfc:makeXMLInput>
            <td class="checkboxcolumn"><input type="checkbox" value='<%=getParameter("orderReleaseKey")%>' name="chkEntityKey"/></td>
            <td class="tablecolumn" sortValue="<%=getNumericValue("xml:OrderRelease:/OrderRelease/@ReleaseNo")%>">
                <a <%=getDetailHrefOptions("L01",getParameter("orderReleaseKey"),"")%>>
                <yfc:getXMLValue binding="xml:/OrderRelease/@ReleaseNo"/></a>
            </td>
            <td class="tablecolumn">
                <a <%=getDetailHrefOptions("L03",getParameter("shipNodeKey"),"shipnode")%>>
                <yfc:getXMLValue binding="xml:/OrderRelease/@ShipNode"/></a>
            </td>
            <td class="tablecolumn">
                <a <%=getDetailHrefOptions("L02",getParameter("statusKey"),"ShowReleaseNo=N")%>>
                <%=displayOrderStatus(getValue("OrderRelease","xml:/OrderRelease/@MultipleStatusesExist"),getValue("OrderRelease","xml:/OrderRelease/@MaxOrderReleaseStatusDesc"),true)%></a>
            </td>
        </tr>
    </yfc:loopXML> 
</tbody>
</table>
