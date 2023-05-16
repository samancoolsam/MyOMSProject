<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/currencyutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/taskmanagement.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/wmsim.js"></script>
<script language="JavaScript">
	function massageNodeItemAttributes(){
		// Start Fix for #4004 as part of release R027
		if(document.all("nodeFieldObj").value == ""){
			alert("Node is required");
			return false;
		}
		if(document.all("xml:/CountRequest/@EnterpriseCode").value == ""){
			alert("Enterprise is required");
			return false;
		}
		
		if(document.all("xml:/CountRequest/@RequestType").value == ""){
			alert("Request Type is required");
			return false;
		}
		// Either Location Id or From and To Location required
		if(document.all("xml:/CountRequest/@LocationId").value == ""){
			if(document.all("xml:/CountRequest/@FromLocation").value == "" && document.all("xml:/CountRequest/@ToLocation").value == ""){
				alert("Either Location or Location Range Required");
				return false;
			}
			if(document.all("xml:/CountRequest/@FromLocation").value == ""){
				alert("From Location is required");
				return false;
			}
			if(document.all("xml:/CountRequest/@ToLocation").value == ""){
				alert("To Location is required");
				return false;
			}
		}
		// End Fix for #4004 		
		if(window.document.getElementById("itemRadio")){
			if(window.document.getElementById("itemRadio").checked){
				if(document.all("xml:/CountRequest/@ItemClassification1")){
				document.all("xml:/CountRequest/@ItemClassification1").value="";
				}
				if(document.all("xml:/CountRequest/@ItemClassification2")){
				document.all("xml:/CountRequest/@ItemClassification2").value="";
				}
				if(document.all("xml:/CountRequest/@ItemClassification3")){
				document.all("xml:/CountRequest/@ItemClassification3").value="";
				}
			}else{
				//Remove Item Here
				document.all("xml:/CountRequest/@ItemID").value="";
				document.all("xml:/CountRequest/@UnitOfMeasure").value="";
			}
		}
		return true;
	}

	function showClassification(){
		window.document.getElementById("Classification").style.display = "";
		window.document.getElementById("Item").style.display = "none";
	}

	function hideClassification(){
		window.document.getElementById("Item").style.display = "";
		window.document.getElementById("Classification").style.display = "none";
	}

	function validateRange(view){
		key = "<Location"; 
		if(document.all("xml:/CountRequest/@ToLocation").value == "" || document.all("xml:/CountRequest/@FromLocation").value == ""){
			alert("Location Range Required");
		}else{
				var temp = document.all("xml:/CountRequest/@ToLocation").value;
				var temp2 = document.all("xml:/CountRequest/@FromLocation").value;
				var temp3 = document.all("xml:/Location/@Node").value;
				key += " FromLocation =\""+temp2 +"\" ToLocation =\""+temp+"\" Node =\""+temp3+"\"/>";
				yfcShowDetailPopupWithParams(view, "", "550", "275", "","count", key,"");
				return true;
		}
	}

</script>
<script language="javascript">
	window.attachEvent("onload", setCorrectStyle);
	function setCorrectStyle(){
		if(window.document.getElementById("classificationRadio")){
			if(window.document.getElementById("classificationRadio").checked){
				showClassification();
			}
		}
	}
</script>
<table width="100%" class="view">
<%if(!isVoid(resolveValue("xml:/CountRequest/@CountRequestKey"))){%>
<tr>
	<td class="detaillabel" ><yfc:i18n>Zone</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@ZoneId" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/@ZoneId","xml:/CountRequest/@ZoneId")%> />
    </td> 
	<td class="detaillabel" ><yfc:i18n>Pallet_ID</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@PalletId" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/@PalletId","xml:/CountRequest/@PalletId")%> />
    </td>
	<td class="detaillabel" ><yfc:i18n>Receipt_#</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/Receipt/@ReceiptNo" />
        <input type="hidden"  
          <%=getTextOptions("xml:/CountRequest/Receipt/@ReceiptNo","xml:/CountRequest/Receipt/@ReceiptNo")%> />
    </td>
</tr>
<tr>
	<td class="detaillabel" ><yfc:i18n>Location</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@LocationId" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/@LocationId","xml:/CountRequest/@LocationId")%> />
    </td>
	<td class="detaillabel" ><yfc:i18n>Case_ID</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@CaseId" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/@CaseId","xml:/CountRequest/@CaseId")%> />
    </td>
	<td class="detaillabel" ><yfc:i18n>Product_Class</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@ProductClass" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/@ProductClass","xml:/CountRequest/@ProductClass")%> />
    </td>
</tr>
<tr>
	<td class="detaillabel"><yfc:i18n>Aisle_Number</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@AisleNumber" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/@AisleNumber","xml:/CountRequest/@AisleNumber")%> />
    </td>
	<td class="detaillabel"><yfc:i18n>Bay_Number</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@BayNumber" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/@BayNumber","xml:/CountRequest/@BayNumber")%> />
    </td>
	<td class="detaillabel"><yfc:i18n>Level_Number</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@LevelNumber" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/@LevelNumber","xml:/CountRequest/@LevelNumber")%> />
    </td>
</tr>
<tr>
	<td class="detaillabel" ><yfc:i18n>From_Location</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@FromLocation" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/@FromLocation","xml:/CountRequest/@FromLocation")%> />
    </td>
	<td class="detaillabel" ><yfc:i18n>To_Location</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@ToLocation" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/@ToLocation","xml:/CountRequest/@ToLocation")%> />
    </td>
</tr>
<tr>
	<td class="detaillabel" ><yfc:i18n>Item_ID</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@ItemID" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/@ItemID","xml:/CountRequest/@ItemID")%> />
    </td>
	<td class="detaillabel" ><yfc:i18n>Description</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/Item/@Description" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/Item/@Description","xml:/CountRequest/Item/@Description")%> />
    </td>
	<td class="detaillabel" ><yfc:i18n>Unit_Of_Measure</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@UnitOfMeasure" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/@UnitOfMeasure","xml:/CountRequest/@UnitOfMeasure")%> />
    </td>
</tr>
<%if(!isVoid(resolveValue("xml:/CountRequest/@EnterpriseCode"))){%>
<yfc:callAPI apiID="AP3"/>

	<%
			YFCElement elemClassificationPurpose = (YFCElement) request.getAttribute("ClassificationPurpose");
			//System.out.println(" elemClassificationPurpose --> is "+elemClassificationPurpose.toString());
			Map mp = new HashMap();
			if((!isVoid(elemClassificationPurpose)) && elemClassificationPurpose.hasChildNodes()){
				int iCounter = 1;
                String root[] = new String[1];
                root[0] = "ClassificationPurposeCode";
                elemClassificationPurpose.sortChildren(root, true);
				%>
					<tr>
						<%for(Iterator j = elemClassificationPurpose.getChildren();j.hasNext();){
						YFCElement childElem = (YFCElement)j.next();
						if(!isVoid(childElem.getAttribute("ClassificationPurposeCode"))){
						%>
						<td class="detaillabel">
							<yfc:i18n> <%=childElem.getAttribute("AttributeName")%></yfc:i18n>
						</td>
						<%String sAttrBinding = "xml:/CountRequest/@ItemClassification"+String.valueOf(iCounter);
							++iCounter; 
						%>
						<td nowrap="true">
							<input type="text" contenteditable="false" class="protectedtext" <%=getTextOptions(sAttrBinding)%> />
						</td>
						<%}
					}%>
				</tr>
			<%}
}
%>
<%}else{%>
<yfc:callAPI apiID="AP1"/>
<yfc:callAPI apiID="AP2"/>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>Zone</yfc:i18n>
    </td>
    <td nowrap="true">
		<input type="text" disabled class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@ZoneId","xml:/CountRequest/@ZoneId")%> />
        <img class="lookupicon" disabled onclick="callListLookup(this,'zone','&xml:/Zone/@Node=<%=resolveValue("xml:CommonFields:/CommonFields/@Node")%>')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Zone") %> />
    </td> 
	<td class="detaillabel" >
        <yfc:i18n>Pallet_ID</yfc:i18n>
    </td>
    <td nowrap="true">
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@PalletId","xml:/CountRequest/@PalletId")%> />
    </td>
	<td class="detaillabel" ><yfc:i18n>Receipt_#</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/Receipt/@ReceiptNo" />
        <input type="text" class="unprotectedinput"           <%=getTextOptions("xml:/CountRequest/Receipt/@ReceiptNo","xml:/CountRequest/Receipt/@ReceiptNo")%> />
           <% String receiptExtraParams = getExtraParamsForTargetBinding("xml:/Receipt/Shipment/@EnterpriseCode", getValue("CommonFields", "xml:/CommonFields/@EnterpriseCode")); %>
		  <img class="lookupicon" onclick="callLookup(this,'receiptlookup','<%=receiptExtraParams%>')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Receipt") %> />
    </td>
</tr>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>Location</yfc:i18n>
    </td>
    <td nowrap="true">
		<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@LocationId","xml:/CountRequest/@LocationId")%> />
        <img class="lookupicon" onclick="callLookup(this,'location','xml:/Location/@Node=<%=resolveValue("xml:CommonFields:/CommonFields/@Node")%>', 'xml:/Location/@ZoneId=<%=resolveValue("xml:/CountRequest/@ZoneId")%>')" 
		<%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Location") %> />
    </td>
	<td class="detaillabel" >
        <yfc:i18n>Case_ID</yfc:i18n>
    </td>
    <td nowrap="true">
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@CaseId","xml:/CountRequest/@CaseId")%> />
    </td>
	<td class="detaillabel" >
        <yfc:i18n>Product_Class</yfc:i18n>
    </td>
    <td nowrap="true">
		<select name="xml:/CountRequest/@ProductClass" class="combobox" >
			<yfc:loopOptions binding="xml:ProductClass:/CommonCodeList/@CommonCode" 
				name="CodeValue" value="CodeValue" selected="xml:/CountRequest/@ProductClass" />
		</select>
    </td>
</tr>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>From_Location</yfc:i18n>
    </td>
    <td nowrap="true">
		<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@FromLocation","xml:/CountRequest/@FromLocation")%> />
        <img class="lookupicon" onclick="callLookup(this,'location','xml:/Location/@Node=<%=resolveValue("xml:CommonFields:/CommonFields/@Node")%>', 'xml:/Location/@ZoneId=<%=resolveValue("xml:/CountRequest/@ZoneId")%>')" 
		<%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Location") %> />
    </td>
	<td class="detaillabel" >
        <yfc:i18n>To_Location</yfc:i18n>
    </td>
    <td nowrap="true">
		<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@ToLocation","xml:/CountRequest/@ToLocation")%> />
        <img class="lookupicon" onclick="callLookup(this,'location','xml:/Location/@Node=<%=resolveValue("xml:CommonFields:/CommonFields/@Node")%>', 'xml:/Location/@ZoneId=<%=resolveValue("xml:/CountRequest/@ZoneId")%>')" 
		<%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Location") %> />
    </td>
	</tr>
	<tr>
	<td class="detaillabel" nowrap="true" sortable="no" style="width:<%=getUITableSize("xml:/CountRequest/@AisleNumber")%>">
        <yfc:i18n>Aisle_Number</yfc:i18n>
    </td>
    <td nowrap="true">
		<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@AisleNumber","xml:/CountRequest/@AisleNumber")%> />
        <!-- <img class="lookupicon" onclick="callLookup(this,'aisle','xml:/Location/@Node=<%=resolveValue("xml:CommonFields:/CommonFields/@Node")%>', 'xml:/Location/@ZoneId=<%=resolveValue("xml:/CountRequest/@ZoneId")%>')" 
		<%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Location") %>  />-->
    </td>
	<td class="detaillabel" nowrap="true" sortable="no" style="width:<%=getUITableSize("xml:/CountRequest/@BayNumber")%>">
        <yfc:i18n>Bay_Number</yfc:i18n>
    </td>
    <td nowrap="true">
		<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@BayNumber","xml:/CountRequest/@BayNumber")%> />
        <!-- <img class="lookupicon" onclick="callLookup(this,'location','xml:/Location/@Node=<%=resolveValue("xml:CommonFields:/CommonFields/@Node")%>', 'xml:/Location/@ZoneId=<%=resolveValue("xml:/CountRequest/@ZoneId")%>')" 
		<%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Location") %> /> -->
    </td>
	<td class="detaillabel" nowrap="true" sortable="no" style="width:<%=getUITableSize("xml:/CountRequest/@LevelNumber")%>">
        <yfc:i18n>Level_Number</yfc:i18n>
    </td>
    <td nowrap="true">
		<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@LevelNumber","xml:/CountRequest/@LevelNumber")%> />
       <!--  <img class="lookupicon" onclick="callLookup(this,'location','xml:/Location/@Node=<%=resolveValue("xml:CommonFields:/CommonFields/@Node")%>', 'xml:/Location/@ZoneId=<%=resolveValue("xml:/CountRequest/@ZoneId")%>')" 
		<%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Location") %> /> -->
    </td>
</tr>
<yfc:callAPI apiID="AP3"/>
	<%
			boolean showRadio = false;
			YFCElement elemClassificationPurpose = (YFCElement) request.getAttribute("ClassificationPurpose");
			Map mp = new HashMap();
			if((!isVoid(elemClassificationPurpose)) && elemClassificationPurpose.hasChildNodes()){
				showRadio = true;
			}

if(showRadio){%>
<tr>
	<td colspan="2">
		<table class="view" height="100%" width="100%" >
		<tr>
			<td class="detaillabel">
				<input type="radio" id="itemRadio" onclick="hideClassification()" <%=getRadioOptions("xml:/CountProgram/@CountProgramLevel","xml:/CountProgram/@CountProgramLevel","")%>>
				</input>
			</td>
			<td class="detaillabel" align="right">
				<yfc:i18n>By_Item</yfc:i18n>
			</td>
		</tr>
		<tr>
			<td class="detaillabel">
				<input type="radio" id="classificationRadio" onclick="showClassification()" <%=getRadioOptions("xml:/CountProgram/@CountProgramLevel","xml:/CountProgram/@CountProgramLevel","N")%>>
				</input>
			</td>
			<td class="detaillabel">
				<yfc:i18n>By_Item_Classification</yfc:i18n>
			</td>
		</tr>
		</table>
	</td>
<%}%>
<td colspan="4">
	<table class="view" height="100%" width="100%" >
		<tr id="Item" style="display:">
			<td class="detaillabel">
				<yfc:i18n>Item_ID</yfc:i18n>
		    </td>
		    <td nowrap="true">
				<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@ItemID","xml:/CountRequest/@ItemID")%> />
	            <% String extraParams = getExtraParamsForTargetBinding("xml:/Item/@CallingOrganizationCode", getValue("CommonFields", "xml:/CommonFields/@EnterpriseCode")); %>
				<img class="lookupicon" name="search"onclick="callItemLookup('xml:/CountRequest/@ItemID','xml:/CountRequest/@ProductClass','xml:/CountRequest/@UnitOfMeasure','item','<%=extraParams%>');"<%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Item") %> />
			</td>
			<td class="detaillabel">
				<yfc:i18n>Unit_Of_Measure</yfc:i18n>
			</td>
			<td nowrap="true">
				<select name="xml:/CountRequest/@UnitOfMeasure" class="combobox">
				<yfc:loopOptions binding="xml:UnitOfMeasureList:/ItemUOMMasterList/@ItemUOMMaster" name="UnitOfMeasure" value="UnitOfMeasure" selected="xml:/CountRequest/@UnitOfMeasure"/>
				</select>
			</td>
		</tr>
		<tr style="display:" height="20px">
			<td/><td/><td/><td/>
		</tr>
		<tr id="Classification" style="display:none">
		<%			
				 String extraParamsForClassification = getExtraParamsForTargetBinding("xml:/Category/@CallingOrganizationCode", getValue("CommonFields", "xml:/CommonFields/@EnterpriseCode"));
				int iCounter = 1;
                String root[] = new String[1];
                root[0] = "ClassificationPurposeCode";
                elemClassificationPurpose.sortChildren(root, true);
				for(Iterator j = elemClassificationPurpose.getChildren();j.hasNext();){
					YFCElement childElem = (YFCElement)j.next();
					if(!isVoid(childElem.getAttribute("ClassificationPurposeCode"))){
					%>
					<td class="detaillabel">
				        <yfc:i18n> <%=childElem.getAttribute("AttributeName")%></yfc:i18n>
				    </td>
					<%String sAttrBinding = "xml:/CountRequest/@ItemClassification"+String.valueOf(iCounter);
						++iCounter;
						String sCatDomKey = childElem.getAttribute("CategoryDomainKey");
						if(sCatDomKey == null){
							sCatDomKey = "";
						}
						%>
					<td nowrap="true">
						<input type="text" class="unprotectedinput" <%=getTextOptions(sAttrBinding)%> />
						<img class="lookupicon" name="search"onclick="callLookup(this,'itemclassification','<%=extraParamsForClassification%>'						+'&xml:/Category/@CategoryDomainKey=<%=sCatDomKey%>');" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Item_Classification") %> />
					</td>
					<%}
				}

		%>
		</tr>
		</table>
		</td>
		</tr>
		<%}
		%>
		<input type="hidden" name="xml:/Location/@Node" value='<%=resolveValue("xml:CommonFields:/CommonFields/@Node")%>'/>
</table>
