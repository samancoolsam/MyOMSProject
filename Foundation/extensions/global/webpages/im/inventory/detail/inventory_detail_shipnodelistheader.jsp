<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/im.js"></script>

<%
    String acrossAllRules = resolveValue("xml:/ShipNodeInventory/Item/@ConsiderAllNodes");
	String acrossAllSegments = resolveValue("xml:/ShipNodeInventory/Item/@ConsiderAllSegments");
	String segmentType = resolveValue("xml:/ShipNodeInventory/Item/@SegmentType");
	String segment = resolveValue("xml:/ShipNodeInventory/Item/@Segment");
	if (equals(acrossAllSegments,"N")) {
		if ((isVoid(segmentType)) && (isVoid(segment))) {
			acrossAllSegments = " ";
		}
	}
%>

<table width="100%" border="0" cellpadding="0" cellSpacing="7px">
    <tr>
        <td width="50%" height="100%">
            <table class="view" width="100%">
				<tr>
					<td class="detaillabel" ><yfc:i18n>Organization_Code</yfc:i18n></td>
					<td class="protectedtext"><yfc:getXMLValue binding="xml:/ShipNodeInventory/Item/@OrganizationCode"></yfc:getXMLValue></td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
                <tr>
                    <td class="detaillabel" ><yfc:i18n>Item_ID</yfc:i18n></td>
                    <td class="protectedtext"><yfc:getXMLValue binding="xml:/ShipNodeInventory/Item/@ItemID"></yfc:getXMLValue></td>
                    <td class="detaillabel" ><yfc:i18n>Product_Class</yfc:i18n></td>
                    <td class="protectedtext"><yfc:getXMLValue binding="xml:/ShipNodeInventory/Item/@ProductClass"></yfc:getXMLValue></td>
                </tr>
                <tr>
                    <td class="detaillabel" ><yfc:i18n>Unit_Of_Measure</yfc:i18n></td>
                    <td class="protectedtext"><yfc:getXMLValue binding="xml:/ShipNodeInventory/Item/@UnitOfMeasure"></yfc:getXMLValue></td>
                </tr>
                <tr>
                    <td class="detaillabel"><yfc:i18n>Description</yfc:i18n></td>
					<td class="protectedtext" colspan="3"><yfc:getXMLValue binding="xml:/ShipNodeInventory/Item/@Description"></yfc:getXMLValue></td>
                </tr>
            </table>
        </td>
    </tr>
</table>
