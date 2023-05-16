<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%
    String ri = getParameter("requestID");
    double dDemandWithinDisb = getDoubleFromLocalizedString(getLocale(),resolveValue("xml:/InventoryInformation/Item/InventoryTotals/Demands/@TotalInternalWithinDistribution")) + getDoubleFromLocalizedString(getLocale(),resolveValue("xml:/InventoryInformation/Item/InventoryTotals/Demands/@TotalExternalWithinDistribution"));
    double dDemandOutsideDisb = getDoubleFromLocalizedString(getLocale(),resolveValue("xml:/InventoryInformation/Item/InventoryTotals/Demands/@TotalInternalOutsideDistribution")) + getDoubleFromLocalizedString(getLocale(),resolveValue("xml:/InventoryInformation/Item/InventoryTotals/Demands/@TotalExternalOutsideDistribution"));
    double dDemandUnassigned = getDoubleFromLocalizedString(getLocale(),resolveValue("xml:/InventoryInformation/Item/InventoryTotals/Demands/@TotalInternalUnassigned")) + getDoubleFromLocalizedString(getLocale(),resolveValue("xml:/InventoryInformation/Item/InventoryTotals/Demands/@TotalExternalUnassigned"));

    String sDemandWithinDisbTitle = getI18N("Demand_Within_Distribution") + ":" + getLocalizedStringFromDouble(getLocale(),dDemandWithinDisb);
    String sDemandOutsideDisbTitle = getI18N("Demand_Outside_Distribution") + ":" + getLocalizedStringFromDouble(getLocale(),dDemandOutsideDisb);
    String sUnassignedDemandTitle = getI18N("Unassigned_Demand") + ":" + getLocalizedStringFromDouble(getLocale(),dDemandUnassigned);
%>

<table class="anchor" cellpadding="2px"  cellspacing="0" >
<tr>
    <td valign="top">
        <jsp:include page="/im/inventory/detail/inventory_detail_demandwithindisb.jsp" flush="true">
            <jsp:param name="requestID" value="<%=ri%>" />
            <jsp:param name="Title" value="<%=sDemandWithinDisbTitle%>" />
        </jsp:include>
    </td>
</tr>
<tr>
    <td valign="top">
        <jsp:include page="/im/inventory/detail/inventory_detail_demandoutsidedisb.jsp" flush="true">
            <jsp:param name="requestID" value="<%=ri%>" />
            <jsp:param name="Title" value="<%=sDemandOutsideDisbTitle%>" />
        </jsp:include>
    </td>
</tr>
<tr>
    <td valign="top">
        <jsp:include page="/im/inventory/detail/inventory_detail_unassigneddemand.jsp" flush="true">
            <jsp:param name="requestID" value="<%=ri%>" />
            <jsp:param name="Title" value="<%=sUnassignedDemandTitle%>" />
        </jsp:include>
    </td>
</tr>
</table>
