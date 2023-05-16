<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<table width="100%" class="view">
	
    <% // Now call the APIs that are dependent on the common fields (Organization Code) %>
    <yfc:callAPI apiID="AP2"/>
    <yfc:callAPI apiID="AP3"/>
	
<tr>
    <td class="searchlabel" ><yfc:i18n>Item_ID</yfc:i18n></td>
</tr>
<tr >
    <td class="searchcriteriacell" nowrap="true">
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/InventoryItem/@ItemID") %> />
		<% String extraParams = getExtraParamsForTargetBinding("xml:/Item/@CallingOrganizationCode", getValue("CommonFields", "xml:/CommonFields/@EnterpriseCode")); %>
        <img class="lookupicon" onclick="callItemLookup('xml:/InventoryItem/@ItemID','xml:/InventoryItem/@ProductClass','xml:/InventoryItem/@UnitOfMeasure','item','<%=extraParams%>')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Item") %> />
		
    </td>
</tr>
</table>
