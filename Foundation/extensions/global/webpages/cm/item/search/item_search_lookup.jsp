<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/im.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<table class="view">

<%
	String isReturnService = getParameter("IsReturnService");
	if (isVoid(isReturnService)) {
		isReturnService = resolveValue("xml:/Item/PrimaryInformation/@IsReturnService");
	}
	if (isVoid(isReturnService)) {
		isReturnService = "N";
	}

	String canUseAsServiceTool = getParameter("CanUseAsServiceTool");
	if (isVoid(canUseAsServiceTool)) {
		canUseAsServiceTool = resolveValue("xml:/Item/@CanUseAsServiceTool");
	}

	String itemGroupCode = getParameter("ItemGroupCode");
	if (isVoid(itemGroupCode)) {
		itemGroupCode = resolveValue("xml:/Item/@ItemGroupCode");
	}
	if (isVoid(itemGroupCode)) {
		itemGroupCode = "PROD";
	}
	YFCElement itemGroupCodeElem = YFCDocument.createDocument("ItemGroupCode").getDocumentElement();
	request.setAttribute("ItemGroupCode", itemGroupCodeElem);
	itemGroupCodeElem.setAttribute("ItemGroupCode", itemGroupCode);

    String callingOrgCode = getValue("Item", "xml:/Item/@CallingOrganizationCode");
	if(isVoid(callingOrgCode)){
		callingOrgCode = getValue("CurrentOrganization", getSelectedOrgCodeValue("xml:/Item/@CallingOrganizationCode"));
	}
			
%>

<script language="javascript">
	window.dialogArguments.parentWindow.defaultOrganizationCode = "<%=callingOrgCode%>";
</script>

<tr>
	<td>
		<input type="hidden" name="xml:/Item/PrimaryInformation/@IsReturnService" value='<%=isReturnService%>'/>
		<input type="hidden" name="xml:/Item/@ItemGroupCode" value='<%=itemGroupCode%>'/>
		<input type="hidden" name="xml:/Item/@CanUseAsServiceTool" value='<%=canUseAsServiceTool%>'/>
	</td>
</tr>

<tr>
    <td class="searchlabel" ><yfc:i18n>Organization</yfc:i18n></td>
</tr>
<tr>
    <td nowrap="true" class="searchcriteriacell" >
		<input type="text" class="protectedinput" contenteditable="false" <%=getTextOptions("xml:/Item/@CallingOrganizationCode")%>/>
    </td>
</tr>

    <% // Now call the APIs that are dependent on the calling organization code %>
	<yfc:callAPI apiID="AP2"/>
    <yfc:callAPI apiID="AP3"/>

<tr>
    <td class="searchlabel" ><yfc:i18n>Item_ID</yfc:i18n></td>
</tr>
<tr>
    <td nowrap="true" class="searchcriteriacell" >
        <select name="xml:/Item/@ItemIDQryType" class="combobox" >
            <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" 
                name="QueryTypeDesc" value="QueryType" selected="xml:/Item/@ItemIDQryType"/>
        </select>
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Item/@ItemID") %> />
    </td>
</tr><!-- Start Enhancement for UPC Code --><tr>    <td class="searchlabel" ><yfc:i18n>UPC_Code</yfc:i18n></td></tr><tr><td>  <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Item/ItemAliasList/ItemAlias/@AliasValue") %> /></td></tr><!-- End -->
<tr>
    <td class="searchlabel" ><yfc:i18n>Default_Product_Class</yfc:i18n></td>
</tr>
<tr>
    <td nowrap="true" class="searchcriteriacell" >
        <select name="xml:/Item/PrimaryInformation/@DefaultProductClass" class="combobox" >
            <yfc:loopOptions binding="xml:ProductClass:/CommonCodeList/@CommonCode" 
            name="CodeValue" value="CodeValue" selected="xml:/Item/PrimaryInformation/@DefaultProductClass"/>
        </select>
    </td>
</tr>
<tr>
    <td class="searchlabel" ><yfc:i18n>Unit_Of_Measure</yfc:i18n></td>
</tr>
<tr>
    <td nowrap="true" class="searchcriteriacell">
        <select name="xml:/Item/@UnitOfMeasure" class="combobox" >
            <yfc:loopOptions binding="xml:UnitOfMeasureList:/ItemUOMMasterList/@ItemUOMMaster" name="UnitOfMeasure" value="UnitOfMeasure" selected="xml:/Item/@UnitOfMeasure"/>
        </select>
    </td>
</tr>
<tr>
    <td class="searchlabel" ><yfc:i18n>Short_Description</yfc:i18n></td>
</tr>
<tr>
    <td nowrap="true" class="searchcriteriacell" >
        <select name="xml:/Item/PrimaryInformation/@ShortDescriptionQryType" class="combobox" >
            <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" 
                name="QueryTypeDesc" value="QueryType" selected="xml:/Item/PrimaryInformation/@ShortDescriptionQryType"/>
        </select>
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Item/PrimaryInformation/@ShortDescription") %> />
    </td>
</tr>
<tr>
    <td class="searchlabel" ><yfc:i18n>Master_Catalog_ID</yfc:i18n></td>
</tr>
<tr>
    <td nowrap="true" class="searchcriteriacell" >
        <select name="xml:/Item/PrimaryInformation/@MasterCatalogIDQryType" class="combobox" >
            <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" 
                name="QueryTypeDesc" value="QueryType" selected="xml:/Item/PrimaryInformation/@MasterCatalogIDQryType"/>
        </select>
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Item/PrimaryInformation/@MasterCatalogID") %> />            
    </td>
</tr>
<tr>
	<td class="searchcriteriacell">
		<input type="checkbox" <%=getCheckBoxOptions("xml:/Item/@IsShippingCntr", "xml:/Item/@IsShippingCntr", "Y")%> yfcCheckedValue='Y' yfcUnCheckedValue='N' ><yfc:i18n>Shipping_Container</yfc:i18n></input>
	</td>
</tr>    
<%	if (equals(itemGroupCode,"PROD")) { %>
<tr>
    <td class="searchlabel" ><yfc:i18n>Global_Item_Id</yfc:i18n></td>
</tr>
<tr>
    <td nowrap="true" class="searchcriteriacell" >
        <select name="xml:/Item/@GlobalItemIDQryType" class="combobox" >
            <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" 
                name="QueryTypeDesc" value="QueryType" selected="xml:/Item/@GlobalItemIDQryType"/>
        </select>
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Item/@GlobalItemID") %> />            
    </td>
</tr>
<% } %>
</table>