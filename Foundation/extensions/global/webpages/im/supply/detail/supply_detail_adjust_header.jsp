<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>


<table width="100%" class="view">

<tr>
    <td class="detaillabel" >
        <yfc:i18n>Item_ID</yfc:i18n>
    </td>
    <td class="protectedtext">
        <%=resolveValue("xml:/Item/@ItemID")%>
        <input type="hidden" class="protectedinput" <%=getTextOptions("xml:/Items/Item/@ItemID","xml:/Item/@ItemID")%> />
    </td>
    <td class="detaillabel" >
        <yfc:i18n>Product_Class</yfc:i18n>
    </td>
    <td class="protectedtext">
        <%=resolveValue("xml:/Item/PrimaryInformation/@DefaultProductClass")%>
        <input type="hidden" class="protectedinput" <%=getTextOptions("xml:/Items/Item/@ProductClass","xml:/Item/PrimaryInformation/@DefaultProductClass")%> />
    </td>
    <td class="detaillabel" >
        <yfc:i18n>Unit_Of_Measure</yfc:i18n>
    </td>
    <td class="protectedtext">
        <%=resolveValue("xml:/Item/@UnitOfMeasure")%>
        <input type="hidden" class="protectedinput" <%=getTextOptions("xml:/Items/Item/@UnitOfMeasure","xml:/Item/@UnitOfMeasure")%> />
    </td>
</tr>
<tr>
    <td>
        <input type="hidden" class="protectedinput" <%=getTextOptions("xml:/Items/Item/@OrganizationCode","xml:/InventoryItem/@OrganizationCode")%> />
    </td>
    <td>
        <input type="hidden" class="protectedinput" <%=getTextOptions("xml:/Items/Item/@ETA","xml:/Item/Supplies/InventorySupply/@ETA")%> />
    </td>
    <td>
        <input type="hidden" class="protectedinput" <%=getTextOptions("xml:/Items/Item/@AdjustmentType","ADJUSTMENT")%> />
    </td>
</tr>
</table>
