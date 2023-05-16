<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript">

function proceedClicked1(obj) {
   if(!document.all("xml:/AdjustLocationInventory/@Node").value){
		alert(YFCMSG077); //Node not Passed
		doNormalWindow();
	}else if(!document.all("xml:/AdjustLocationInventory/@EnterpriseCode").value){
		alert(YFCMSG078);//Enterprise not Passed
		doNormalWindow();
	}else if(!(document.all("xml:/AdjustLocationInventory/Source/@LocationId").value||document.all("xml:/AdjustLocationInventory/Source/@CaseId").value||document.all("xml:/AdjustLocationInventory/Source/@PalletId").value)){
		alert(YFCMSG079);//Location or Case/Pallet must be Passed
		doNormalWindow();
	}
	else if(document.all("xml:/AdjustLocationInventory/Source/@PalletId").value  && document.all("xml:/AdjustLocationInventory/Source/@CaseId").value ){
		alert(YFCMSG135);//Only one of case/pallet should be passed
		doNormalWindow();
	}
	else if(!(document.all("xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID").value||document.all("xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure").value)){
		alert(YFCMSG080);//Item and UOM must be Passed
		doNormalWindow();
	}else if(!(document.all("xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ProductClass").value)){
        alert(YFCMSG081);//Product Class must be Passed
		doNormalWindow();
	}else
	{
		var tmp = document.all("hidPrepareEntityKey");
	    tmp.value = "Y";
		if(validateControlValues()){
        yfcChangeDetailView(getCurrentViewId());
		}
	}
}
function doNormalWindow()
{
    var sVal = document.all("myEntityKey");
    var tmp = '<%=getParameter("hidPrepareEntityKey")%>';
    var oError = document.all("isError");
    if(tmp == "Y" && oError.value == "N")
    {
        oError.value="Y";
        showDetailFor(sVal.value);  
    }
}
</script>

<% 
	String palletid = resolveValue("xml:/AdjustLocationInventory/Source/@PalletId");
	String caseid = resolveValue("xml:/AdjustLocationInventory/Source/@CaseId");
	String inventorystatus = resolveValue("xml:/AdjustLocationInventory/Source/Inventory/@InventoryStatus");
	String locationid = resolveValue("xml:/AdjustLocationInventory/Source/@LocationId");
	
	boolean locAPIcalled= false;
	boolean lpnAPIcalled= false;
	boolean bValidLocation = true;
	boolean bValidLPN = true;
	if(equals(getParameter("hidPrepareEntityKey"),"Y")) { %>
<%		if (!isVoid(resolveValue("xml:/AdjustLocationInventory/Source/@PalletId")) || 																!isVoid(resolveValue("xml:/AdjustLocationInventory/Source/@CaseId"))) { %>
			
<%			lpnAPIcalled = true;
		System.out.println("API called........");
			
		} %>
<%		if (bValidLPN) {
			if (!isVoid(resolveValue("xml:/AdjustLocationInventory/Source/@LocationId"))) { %>
				<yfc:callAPI apiID='AP6'/>
<%				locAPIcalled = true;
				if (isVoid(getValue("Location","xml:/Location/@LocationId"))) {
					bValidLocation = false; %>
					<script>
						alert(YFCMSG082);//Invalid Location
					</script>
<%				}
			}
			if (bValidLocation) {%>
				
					<yfc:callAPI apiID='AP1'/>
<%					String NoOfLocinvlist = getValue("Inventory","xml:/NodeInventory/LocationInventoryList/@TotalNumberOfRecords");
					if ((Integer.parseInt(NoOfLocinvlist) == 0) || (Integer.parseInt(NoOfLocinvlist) == 1)){ %>
						<script>
							window.attachEvent("onload",doNormalWindow);
						</script>
<%					} else if (Integer.parseInt(NoOfLocinvlist) >= 1 ) { 
						if (YFCCommon.isVoid(inventorystatus)) { %>
							<script>
								alert(YFCMSG084);//Data not enough to identify unique record try giving inventory status or go to Location Inventory Console and Adjust the Inventory
							</script>
<%						} else if (YFCCommon.isVoid(palletid) && YFCCommon.isVoid(caseid)) { %>
							<script>
								alert(YFCMSG085);//Data not enough to identify unique record try giving Case Id or Pallet Id or go to Location Inventory Console and Adjust the Inventory
							</script>
<%						}
					}
				
			}
		} 
	}%> 
<table width="100%" class="view">

	<yfc:makeXMLInput name="MyEntityKey">
			<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/@Node" value="xml:/AdjustLocationInventory/@Node"/>
			<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/@EnterpriseCode" value="xml:/AdjustLocationInventory/@EnterpriseCode"/>
			<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/@LocationId" value="xml:/AdjustLocationInventory/Source/@LocationId"/>
			<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/@PalletId" value="xml:/AdjustLocationInventory/Source/@PalletId"/>
			<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/@CaseId" value="xml:/AdjustLocationInventory/Source/@CaseId"/>
			<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID" value="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID"/>
			<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ProductClass" value="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ProductClass"/>
			<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure" value="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure"/>
			<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/@InventoryStatus" value="xml:/AdjustLocationInventory/Source/Inventory/@InventoryStatus"/>
			<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/@FromMenu" value="xml:/AdjustLocationInventory/@Node"/>

	</yfc:makeXMLInput>
    <input type="hidden" name="myEntityKey" value='<%=getParameter("MyEntityKey")%>' />

<jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
		<jsp:param name="ScreenType" value="detail"/>
		<jsp:param name="ShowDocumentType" value="false"/>
		<jsp:param name="ShowNode" value="true"/>
		<jsp:param name="EnterpriseCodeBinding" value="xml:/AdjustLocationInventory/@EnterpriseCode"/>
		<jsp:param name="NodeBinding" value="xml:/AdjustLocationInventory/@Node"/>
        <jsp:param name="RefreshOnNode" value="true"/>
		 <jsp:param name="RefreshOnEnterpriseCode" value="true"/>
        <jsp:param name="EnterpriseListForNodeField" value="true"/>
</jsp:include>
<yfc:callAPI apiID="AP2"/>
<yfc:callAPI apiID="AP3"/>
<yfc:callAPI apiID="AP4"/>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>Location</yfc:i18n>
    </td>
    <td nowrap="true">
		<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/AdjustLocationInventory/Source/@LocationId","xml:/AdjustLocationInventory/Source/@LocationId")%> />
        <img class="lookupicon" onclick="callLookup(this,'location','xml:/Location/@Node=' +  document.all['xml:/AdjustLocationInventory/@Node'].value)" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Location") %> />
    </td>
    <td class="detaillabel" >
        <yfc:i18n>Pallet_ID</yfc:i18n>
    </td>
    <td nowrap="true">
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/AdjustLocationInventory/Source/@PalletId","xml:/AdjustLocationInventory/Source/@PalletId")%> />
    </td>

	<td class="detaillabel" >
        <yfc:i18n>Case_ID</yfc:i18n>
    </td>
    <td nowrap="true">
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/AdjustLocationInventory/Source/@CaseId","xml:/AdjustLocationInventory/Source/@CaseId")%> />
    </td>

</tr>
<tr>
    <td class="detaillabel" >
        <yfc:i18n>Item_ID</yfc:i18n>
    </td>
    <td nowrap="true">
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID","xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID")%> />
		 <% String extraParams = getExtraParamsForTargetBinding("xml:/Item/@CallingOrganizationCode", getValue("CommonFields", "xml:/CommonFields/@EnterpriseCode")); %>
		<img class="lookupicon" name="search"onclick="callItemLookup('xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID','xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ProductClass','xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure','item','<%=extraParams%>')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Item") %> />
    </td>

	<td class="detaillabel" >
        <yfc:i18n>Product_Class</yfc:i18n>
    </td>
    <td nowrap="true">
		<select name="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ProductClass" class="combobox" >
			<yfc:loopOptions binding="xml:ProductClass:/CommonCodeList/@CommonCode" 
				name="CodeValue" value="CodeValue" selected="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ProductClass" />
		</select>
    </td>

	<td class="detaillabel" >
        <yfc:i18n>Unit_Of_Measure</yfc:i18n>
    </td>
    <td nowrap="true">
        <select name="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure" class="combobox">
            <yfc:loopOptions binding="xml:UnitOfMeasureList:/ItemUOMMasterList/@ItemUOMMaster" name="UnitOfMeasure" value="UnitOfMeasure" selected="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure"/>
        </select>
    </td>
</tr>
<tr>
    <td class="detaillabel" >
        <yfc:i18n>Inventory_Status</yfc:i18n>
    </td>
    <td nowrap="true">
       	<select name="xml:/AdjustLocationInventory/Source/Inventory/@InventoryStatus" class="combobox" >
            <yfc:loopOptions binding="xml:InventoryStatusList:/InventoryStatusList/@InventoryStatus" 
                name="InventoryStatus" value="InventoryStatus" />
        </select>
    </td>
</tr>
<tr>
    <td colspan="8" align="center">
        <input type="hidden"  name="hidPrepareEntityKey" value="N"/>
        <input type="hidden"  name="isError" value="N"/>
        <input class="button" type="button" value="<%=getI18N("Proceed")%>" onclick="proceedClicked1(this);" />
    </td>
</tr>
</table>
