<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.dom.*" %>
<%@ page import="com.yantra.yfc.util.*" %>
<%@ page import="java.util.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/im.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<%
	String orgCode = resolveValue("xml:CurrentOrganization:/Organization/@OrganizationCode");
	String currSelectedOrg = resolveValue("xml:/Item/@OrganizationCode");
	if (!isVoid(currSelectedOrg)) { 
		setCommonFieldAttribute("OrganizationCode",currSelectedOrg);
	} else {
		setCommonFieldAttribute("OrganizationCode",orgCode);
	}
    String sItemID=getValue("InventoryItem","xml:/InventoryItem/@ItemID");
    
    //Get a list of all Inventory Orgs of all Enterprises in which the User's Org participates.
    List invOrgList = new ArrayList();
	YFCElement oEnterpriseList = (YFCElement) request.getAttribute("OrganizationList");
	if (oEnterpriseList !=null ){
        for (Iterator i = oEnterpriseList.getChildren(); i.hasNext();) {
            YFCElement oEnterprise = (YFCElement)i.next();
			String inventoryOrganizationCode = oEnterprise.getAttribute("InventoryOrganizationCode");
			if(!invOrgList.contains(inventoryOrganizationCode)) {
				invOrgList.add(inventoryOrganizationCode);
			}
        }
	}
%>

<script language="javascript">
function doPopup()
{
    var sVal = document.all("hidMyEntityKey");
    var tmp = '<%=HTMLEncode.htmlEscape(getParameter("hidPrepareEntityKey"))%>';
    var oError = document.all("isError");
    if(tmp == "Y" && oError.value == "N")
    {
        oError.value="Y";
        callPopupWithEntity('supply',sVal.value, "true");
    }
}

function doNormalWindow()
{
    var sVal = document.all("hidMyEntityKey");
    var tmp = '<%=HTMLEncode.htmlEscape(getParameter("hidPrepareEntityKey"))%>';
    var oError = document.all("isError");
    if(tmp == "Y" && oError.value == "N")
    {
        entityType = "supply";
        window.doNotCheck = true;
        oError.value="Y";
        showDetailFor(sVal.value);  
    }
}

function proceedClicked()
{
	var orgCodeObj = document.all("xml:/Item/@OrganizationCode");
	if (orgCodeObj != null) {
		var orgCodeVal = orgCodeObj.value;
		if (orgCodeVal == "") {
			// Need to popup an error here instead of defaulting.
			orgCodeObj.value = '<%=orgCode%>';
		}
	}
	
	 var itemInput = document.all("xml:/Item/@ItemID");
	 if (orgCodeObj != null) {
		var itemInputVal = itemInput.value;
		itemInputVal = itemInputVal.replace(/^\s*/, '').replace(/\s*$/, '');
		 if (itemInputVal != "") {
			var tmp = document.all("hidPrepareEntityKey");
			tmp.value = "Y";
			if(validateControlValues()) {			
				yfcChangeDetailView(getCurrentViewId());
			}
		 } else {
				alert(YFCMSG037);
		 }
	 }
}

function checkKeyPress(oEvent)
{
    if(oEvent.keyCode == 13) {
		proceedClicked();
		return (false);
    }
}
</script>

<% if(equals(getParameter("hidPrepareEntityKey"),"Y")) { 

	if(equals(request.getParameter(YFCUIBackendConsts.YFC_IN_POPUP),"Y")) {%>
	<script>
	    window.attachEvent("onload",doPopup);
	</script>
	<% } else {%>
	<script>
	    window.attachEvent("onload",doNormalWindow);
	</script>
	<%}
}%>

<yfc:callAPI apiID="AP1"/>
<yfc:callAPI apiID="AP2"/>

<table width="100%" class="view">
<tr>

    <yfc:makeXMLInput name="ItemDetailsKey">
	    <yfc:makeXMLKey binding="xml:/InventoryItem/@ItemID" value="xml:/Item/@ItemID" /> 
	    <yfc:makeXMLKey binding="xml:/InventoryItem/@UnitOfMeasure" value="xml:/Item/@UnitOfMeasure" />
	    <yfc:makeXMLKey binding="xml:/InventoryItem/@ProductClass" value="xml:/Item/@ProductClass" />
	    <yfc:makeXMLKey binding="xml:/InventoryItem/@OrganizationCode" value="xml:/Item/@OrganizationCode" />
	</yfc:makeXMLInput>
	
    <% if (!isVoid(sItemID)) { %>
        <td>
			<input type="hidden" class="protectedinput" <%=getTextOptions("xml:/Item/@OrganizationCode","xml:/InventoryItem/@OrganizationCode")%> />
		</td>

    <%} else if((!isShipNodeUser()) && (!isEnterprise())) { %>
		<td class="detaillabel" ><yfc:i18n>Organization</yfc:i18n></td>
		<td class="protectedtext">
			<yfc:getXMLValue binding="xml:CurrentOrganization:/Organization/@OrganizationCode"/>
			<input type="hidden" class="protectedinput" <%=getTextOptions("xml:/Item/@OrganizationCode","xml:CurrentOrganization:/Organization/@OrganizationCode")%> />
		</td>

    <% } else { %>
        <td class="detaillabel" ><yfc:i18n>Organization</yfc:i18n></td>
        <td nowrap="true">
            <select id="OrgCombo" <%=getComboOptions("xml:/Item/@OrganizationCode")%> class="combobox" onchange="yfcChangeDetailView(getCurrentViewId());">
			<%if(invOrgList.size() != 1) {%>
				<option value="" selected="true"/>
			<%}%>
            <% for (int i=0;i<invOrgList.size();i++) {
                String invOrgCode = (String) invOrgList.get(i); %>
                <option value="<%=invOrgCode%>" <%if(equals(invOrgCode,currSelectedOrg)) {%> selected <%}%>><%=invOrgCode%></option>
            <% } %>
            </select>
        </td>
    <% } %>

    <td class="detaillabel" >
        <yfc:i18n>Item_ID</yfc:i18n>
        <input type="hidden" name="hidMyEntityKey" value='<%=getParameter("ItemDetailsKey")%>' />
    </td>
    <% if (!isVoid(sItemID)) {%>
    <td class="protectedtext">
        <yfc:getXMLValue name="InventoryItem" binding="xml:/InventoryItem/@ItemID"/>
        <input type="hidden" class="protectedinput" onkeydown="return checkKeyPress(event)" <%=getTextOptions("xml:/Item/@ItemID","xml:/InventoryItem/@ItemID")%> />
    </td>
    <%}else {%>
    <td nowrap="true">
        <input type="text" class="unprotectedinput" onkeydown="return checkKeyPress(event)" <%=getTextOptions("xml:/Item/@ItemID","xml:/Item/@ItemID")%> />
		<% String extraParams = getExtraParamsForTargetBinding("xml:/Item/@CallingOrganizationCode", getValue("CommonFields", "xml:/CommonFields/@OrganizationCode")); %>
        <img class="lookupicon" name="search"onclick="callItemLookup('xml:/Item/@ItemID','xml:/Item/@ProductClass','xml:/Item/@UnitOfMeasure','item','<%=extraParams%>');"<%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Item") %> />
    </td>
    <% } %>
    <td class="detaillabel" >
        <yfc:i18n>Product_Class</yfc:i18n>
    </td>

    <% if (!isVoid(sItemID)) {%>
        <td class="protectedtext">
            <yfc:getXMLValue name="InventoryItem" binding="xml:/InventoryItem/@ProductClass"/>
            <input type="hidden" class="protectedinput" <%=getTextOptions("xml:/Item/@ProductClass","xml:/InventoryItem/@ProductClass")%> />
        </td>    
    <%}else {%>
        <td>
            <select <%=getComboOptions("xml:/Item/@ProductClass")%> class="combobox" >
                <yfc:loopOptions binding="xml:ProductClass:/CommonCodeList/@CommonCode" 
                    name="CodeValue" value="CodeValue" selected="xml:/Item/@ProductClass"/>
            </select>
        </td>
    <% } %>

    <td class="detaillabel" >
        <yfc:i18n>Unit_Of_Measure</yfc:i18n>
    </td>
    <% if (!isVoid(sItemID)) {%>
    <td class="protectedtext">
        <yfc:getXMLValue name="InventoryItem" binding="xml:/InventoryItem/@UnitOfMeasure"/>
        <input type="hidden" class="protectedinput" <%=getTextOptions("xml:/Item/@UnitOfMeasure","xml:/InventoryItem/@UnitOfMeasure")%> />
    </td>        
    <%}else {%>
    <td>
        <select <%=getComboOptions("xml:/Item/@UnitOfMeasure")%> class="combobox" >
            <yfc:loopOptions binding="xml:UnitOfMeasure:/ItemUOMMasterList/@ItemUOMMaster" 
                name="UnitOfMeasure" value="UnitOfMeasure" selected="xml:/Item/@UnitOfMeasure"/>
        </select>
    </td>        
    <% } %>
</tr>
<tr>
<tr>
    <td colspan="8" align="center">
        <input type="hidden"  name="hidPrepareEntityKey" value="N"/>
        <input type="hidden"  name="isError" value="N"/>
        <input class="button" type="button" value="<%=getI18N("Proceed")%>" onclick="proceedClicked();" />
    </td>
</tr>
</table>
