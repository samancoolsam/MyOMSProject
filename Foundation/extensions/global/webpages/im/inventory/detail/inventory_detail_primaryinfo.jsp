<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<%
    String acrossAllRules = resolveValue("xml:/InventoryInformation/Item/@ConsiderAllNodes");
	String acrossAllSegments = resolveValue("xml:/InventoryInformation/Item/@ConsiderAllSegments");
	String segmentType = resolveValue("xml:/InventoryInformation/Item/@SegmentType");
	String segment = resolveValue("xml:/InventoryInformation/Item/@Segment");
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
                    <td><input type="hidden" <%=getTextOptions("xml:/InventoryInformation/Item/@ItemID")%> /></td>
                    <td><input type="hidden" <%=getTextOptions("xml:/InventoryInformation/Item/@UnitOfMeasure")%> /></td>
                    <td><input type="hidden" <%=getTextOptions("xml:/InventoryInformation/Item/@ProductClass")%> /></td>
                    <td><input type="hidden" <%=getTextOptions("xml:/InventoryInformation/Item/@OrganizationCode")%> /></td>
                </tr>
				<tr>
					<td class="detaillabel" ><yfc:i18n>Organization_Code</yfc:i18n></td>
                    <td class="protectedtext" >
						<yfc:getXMLValue binding="xml:/Item/@OrganizationCode"></yfc:getXMLValue>
                    </td>
                    <% if (equals(resolveValue("xml:/InventoryInformation/@ShowEnableSourcing"),"Y")) {%>
                        <td class="protectedtext" colspan="2">
                            <img <%=getImageOptions(YFSUIBackendConsts.HELD_ORDER, "")%>/>
                            <yfc:i18n>Sourcing_is_currently_disabled_for_this_Node/Item_until</yfc:i18n>
                            <yfc:i18n><%=resolveValue("xml:/InventoryInformation/@ShowUntilDate")%></yfc:i18n>
                        </td>
                    <% } else {%>
                        <td>&nbsp;</td>
                        <td>&nbsp;</td>
                    <% } %>
				</tr>
                <tr>
                    <td class="detaillabel" ><yfc:i18n>Item_ID</yfc:i18n></td>
					<td class="protectedtext"><yfc:getXMLValue binding="xml:/Item/@ItemID" ></yfc:getXMLValue></td>
                    <td class="detaillabel" ><yfc:i18n>Product_Class</yfc:i18n></td>
					<td class="protectedtext"><yfc:getXMLValue binding="xml:/Item/PrimaryInformation/@DefaultProductClass" ></yfc:getXMLValue></td>
                </tr>
                <tr>
                    <td class="detaillabel" ><yfc:i18n>Unit_Of_Measure</yfc:i18n></td>
					 <td class="protectedtext"><yfc:getXMLValue binding="xml:/Item/@UnitOfMeasure" ></yfc:getXMLValue></td>
                </tr>
                <tr>
                    <td class="detaillabel"><yfc:i18n>Description</yfc:i18n></td>
                 <td class="protectedtext" colspan="3"><yfc:getXMLValue binding="xml:/Item/PrimaryInformation/@Description"></yfc:getXMLValue></td> 					
                </tr>
            </table>
        </td>
    </tr>
</table>
