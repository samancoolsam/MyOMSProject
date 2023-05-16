<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.dom.YFCDocument" %>
<%@ page import="com.yantra.yfc.dom.YFCElement" %>

<script language="javascript">
	window.attachEvent("onload", IgnoreChangeNames);
</script>
<SCRIPT LANGUAGE="JavaScript">
	function refreshIfValueChanged(controlObj) {
		if (yfcHasControlChanged(controlObj)) {
			updateCurrentView();
		}
	}
</SCRIPT>
<%
   	String tagControlFlag = getValue("ItemDetails","xml:/Item/InventoryParameters/@TagControlFlag");
    String timeSensitive = getValue("ItemDetails","xml:/Item/InventoryParameters/@TimeSensitive");
	String sShipNode = resolveValue("xml:/Items/Item/@ShipNode");
	if(isShipNodeUser()) {
		sShipNode = resolveValue("xml:CurrentUser:/User/@Node"); 
	}
	if(!isVoid(sShipNode) && ((equals(tagControlFlag, "Y")) || (equals(tagControlFlag, "S")))){

		YFCElement elemItem = YFCDocument.createDocument("Item").getDocumentElement();
		elemItem.setAttribute("OrganizationCode",getValue("ItemDetails","xml:/Item/@OrganizationCode"));
		elemItem.setAttribute("ItemID",getValue("ItemDetails","xml:/Item/@ItemID"));
		elemItem.setAttribute("UnitOfMeasure",getValue("ItemDetails","xml:/Item/@UnitOfMeasure"));
		elemItem.setAttribute("Node",sShipNode); 
		YFCElement tempElem = YFCDocument.parse("<Item><PrimaryInformation DefaultProductClass=\"\" CountryOfOrigin=\"\" Description=\"\" ShortDescription=\"\" SerializedFlag=\"\" NumSecondarySerials=\"\"/><InventoryParameters />  <ClassificationCodes /> <ItemAliasList /> <ItemExclusionList /><AdditionalAttributeList />   <LanguageDescriptionList /> <Components /><InventoryTagAttributes><Extn/></InventoryTagAttributes>  <AlternateUOMList TotalNumberOfRecords=\"\" ><AlternateUOM  />  </AlternateUOMList> <ItemInstructionList TotalNumberOfRecords=\"\">  <ItemInstruction  />   </ItemInstructionList>  <ItemOptionList />   <ItemServiceAssocList /> <CategoryList /> </Item>").getDocumentElement();
%>

		<yfc:callAPI apiName="getNodeItemDetails" inputElement="<%=elemItem%>" 				templateElement="<%=tempElem%>" outputNamespace=""/>

<%
		elemItem = (YFCElement)request.getAttribute("Item");
		if(!isVoid(elemItem)){
			tagControlFlag = elemItem.getAttribute("TagCapturedInInventory");
		}
	}
    
    if ((equals(tagControlFlag, "Y")) || (equals(tagControlFlag, "S"))) {
	    YFCDocument inDoc = YFCDocument.createDocument("InventorySupply"); 
		YFCElement inventorySupplyElem = inDoc.getDocumentElement();
		YFCElement tagElem = inventorySupplyElem.createChild("Tag");
		
		for(Enumeration e = request.getParameterNames();e.hasMoreElements();)  {
			String name = (String)e.nextElement();

			String sTag = "Tag/@";
			String sTagExtn = "Tag/Extn/@";
			if (name.startsWith("xml:/Items/Item/" + sTag) || name.startsWith("xml:/Items/Item/" + sTagExtn) ) {
				String value = getParameter(name);
				int i = name.lastIndexOf("@");
				i = i + 1;
				String attributeName = name.substring(i,name.length()); 
				String binding = "";
				if (name.startsWith("xml:/Items/Item/" + sTag) )
					binding = "xml:/InventorySupply/" + sTag + attributeName;

				if (name.startsWith("xml:/Items/Item/" + sTagExtn) )
					binding = "xml:/InventorySupply/" + sTagExtn + attributeName;

				setValue(inventorySupplyElem,binding,value);
			}	
		}
		request.setAttribute("InventorySupply",inventorySupplyElem);
	}
	
	if(!isShipNodeUser()) {		
		String sOwnerKeyClause = "";
		String OrgCode = resolveValue("xml:CurrentOrganization:/Organization/@OrganizationCode");
		if(!isHub()){
			sOwnerKeyClause = "OwnerKey=\"" + OrgCode + "\"";
		}		
		YFCElement shipNodeListInput = YFCDocument.parse("<ShipNode "+sOwnerKeyClause+"></ShipNode>").getDocumentElement();
		YFCElement shipNodeListTemplate = YFCDocument.parse("<ShipNodeList><ShipNode ShipNode=\"\"/></ShipNodeList>").getDocumentElement();
		%>
		<yfc:callAPI apiName="getShipNodeList" inputElement="<%=shipNodeListInput%>" templateElement="<%=shipNodeListTemplate%>" outputNamespace="ShipNodeList"/>
<%
	}
%>

<table width="100%" class="view"> 
<tr>
 <%if(isShipNodeUser()) {
	 String shipNode = resolveValue("xml:CurrentUser:/User/@Node"); 
	 
%>
	
			<td class="detaillabel" >
				<yfc:i18n>Ship_Node</yfc:i18n>
			</td>
			<td class="protectedtext">
				<I18N><%=shipNode%></I18N>
				<input type="hidden" class="protectedinput" <%=getTextOptions("xml:/Items/Item/@ShipNode","xml:CurrentUser:/User/@Node")%> />
			</td>
<%
	}else{
		List shipNodeList = new ArrayList();
			YFCElement shipNodeListElem = (YFCElement) request.getAttribute("ShipNodeList");
			if (shipNodeListElem !=null ){
				for (Iterator i = shipNodeListElem.getChildren(); i.hasNext();) {
					YFCElement shipNodeElem = (YFCElement)i.next();
					String shipNode = shipNodeElem.getAttribute("ShipNode");
					if(!shipNodeList.contains(shipNode)) {
						shipNodeList.add(shipNode);
					}
				}
			} 
%>
			<td class="detaillabel" >
				<yfc:i18n>Ship_Node</yfc:i18n>
			</td>
			<td nowrap="true">
<%
				if(shipNodeList.size() <= 20){	
%>
					<select <%=getComboOptions("xml:/Items/Item/@ShipNode")%> class="combobox" onChange="updateCurrentView()">
						<%if((shipNodeList.size() != 1)&&isVoid(resolveValue("xml:/Items/Item/@ShipNode"))) {%>
							<option value="" selected/>
						<%}%>
<% 
							for (int i=0;i<shipNodeList.size();i++) {
								String shipNode = (String) shipNodeList.get(i); 
								if(equals(shipNode,resolveValue("xml:/Items/Item/@ShipNode"))){
									%><option value="<%=shipNode%>" selected><%=shipNode%></option><%
								}else{
									%><option value="<%=shipNode%>"><%=shipNode%></option><%
								}
							}
%>
					</select>
<%
				}else{
%>	
					<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Items/Item/@ShipNode")%> onChange="refreshIfValueChanged(document.all('xml:/Items/Item/@ShipNode'))"/>
					<img class="lookupicon" onclick="callLookup(this,'shipnode','xml:/Organization/EnterpriseOrgList/OrgEnterprise/@EnterpriseOrganizationKey=<%=resolveValue("xml:CurrentOrganization:/Organization/@OrganizationCode")%>');refreshIfValueChanged(document.all('xml:/Items/Item/@ShipNode'))" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Ship_Node") %> />
<%
				}
%>
			</td>
<%
	}
%>
    <% if(equals(timeSensitive,"Y")) { %> 
		<td class="detaillabel" >
	        <yfc:i18n>Ship_By_Date</yfc:i18n>
	    </td>
	    <td nowrap="true">
	        <input type="text" class="dateinput" <%=getTextOptions("xml:/Items/Item/@ShipByDate") %> />
	        <img class="lookupicon" name="Calendar" onclick="invokeCalendar(this);return false;" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
	    </td>
    <% } else {%>
    	<td>&nbsp;</td>
    	<td>&nbsp;</td>
    <% } %>
</tr> 
<% if ((equals(tagControlFlag, "Y")) || (equals(tagControlFlag, "S"))) { %> 				    
	<tr>
		<td colspan="10">    
		<jsp:include page="/im/inventory/detail/inventory_detail_tagattributes.jsp" flush="true">
		        <jsp:param name="Modifiable" value='true'/>
		        <jsp:param name="BindingPrefix" value='xml:/Items/Item/Tag'/>
			</jsp:include>
		</td>
	</tr>
<% } %>
</table>
