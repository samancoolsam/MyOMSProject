<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%
    String ri = getParameter("requestID");
    double dSupplyWithinDisb = getDoubleFromLocalizedString(getLocale(),resolveValue("xml:/InventoryInformation/Item/InventoryTotals/Supplies/@TotalInternalWithinDistribution")) + getDoubleFromLocalizedString(getLocale(),resolveValue("xml:/InventoryInformation/Item/InventoryTotals/Supplies/@TotalExternalWithinDistribution"));
    double dSupplyOutsideDisb = getDoubleFromLocalizedString(getLocale(),resolveValue("xml:/InventoryInformation/Item/InventoryTotals/Supplies/@TotalInternalOutsideDistribution")) + getDoubleFromLocalizedString(getLocale(),resolveValue("xml:/InventoryInformation/Item/InventoryTotals/Supplies/@TotalExternalOutsideDistribution"));
    String sSupplyWithinDisbTitle = getI18N("Supply_Within_Distribution") + ":" + getLocalizedStringFromDouble(getLocale(),dSupplyWithinDisb);
    String sSupplyOutsideDisbTitle = getI18N("Supply_Outside_Distribution") + ":" + getLocalizedStringFromDouble(getLocale(),dSupplyOutsideDisb);
%>

<table class="anchor" cellpadding="2px"  cellspacing="0" >
<tr>
    <td valign="top">
        <jsp:include page="/im/inventory/detail/inventory_detail_supplywithindisb.jsp" flush="true">
            <jsp:param name="requestID" value="<%=ri%>" />
            <jsp:param name="Title" value="<%=sSupplyWithinDisbTitle%>" />
        </jsp:include>
    </td>
</tr>
<tr>
    <td valign="top">
        <jsp:include page="/im/inventory/detail/inventory_detail_supplyoutsidedisb.jsp" flush="true">
            <jsp:param name="requestID" value="<%=ri%>" />
            <jsp:param name="Title" value="<%=sSupplyOutsideDisbTitle%>" />
        </jsp:include>
    </td>
</tr>
</table>
