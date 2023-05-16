<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.wms.util.WMSCountUISortComparator" %>
<%@ page import="com.yantra.wms.util.WMSCountUISummResultComparator" %>
<%@ page import="java.util.*" %>

<%String sIgnoreElement="N";
  String sPureLPN = "N";
  String sZeroRecord = "N";
  String sAlreadyConsidered ="Y";
  String sTagInformation="";
  String sCountResultInformation="";
  Map elemMap = new HashMap();
  Map qtyMap = new HashMap();
  boolean bSerialTracked=false;
%>
<%
YFCElement container = (YFCElement) request.getAttribute("CountRequest");
%>
<%boolean bUpdateCount = (YFCCommon.equals(resolveValue("xml:/Task/@TaskStatus"), "2000") && !YFCCommon.equals(resolveValue("xml:/CountRequest/@Status"), "2000") ); 
//Count Request is not completed, task is completed 
%>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/moverequest.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/wmsim.js"></script>
<script language="javascript">
    function processSaveRecordsForCountResults(){
		yfcSpecialChangeNames("CountResult", true);
	}
	function deleteRowForCounter(iCounter)
	{
		var containerForm= document.all("containerform")
		var hiddenKeyInput = document.createElement("<INPUT type='hidden' name='xml:/RecordCountResult/CountResult_" + iCounter + "/@IsNewRow'/>");
		hiddenKeyInput.value = "Y";
		containerForm.insertBefore(hiddenKeyInput);
		updateCurrentView();
	}

    function disableCountQty(counter){
		var obj = document.all("xml:/RecordCountResult/CountResult_"+counter+"/@CountQuantity");
		if(obj[0].disabled){
			obj[0].disabled=false;
		}else{
			obj[0].disabled=true;
			obj[0].value="";
		}
	}
	
	//document.body.attachEvent("onunload", processSaveRecordsForCountResults);
</script>
<%
	String sCounter = null;
	String temp = null;
	YFCElement nodeInventoryElem =(YFCElement)request.getAttribute("Inventory");
	YFCElement locInvList = nodeInventoryElem.getChildElement("LocationInventoryList");
	YFCNodeList inventory = locInvList.getElementsByTagName("LocationInventory");
	for(int j=0;j<inventory.getLength();j++) {
		YFCElement invElem = (YFCElement)inventory.item(j);
		YFCElement itemList = invElem.getChildElement("ItemInventoryDetailList");
		YFCNodeList items = itemList.getElementsByTagName("ItemInventoryDetail");
		for(int i=0;i<items.getLength();i++) {
			YFCElement inventoryElem = (YFCElement)items.item(i);
			String elemKey = prepareKey(inventoryElem);
			inventoryElem.setAttribute("ElementKey",elemKey);  
			if(!elemMap.containsKey(elemKey)) {				
				double dQuantity = inventoryElem.getDoubleAttribute("Quantity");
				if(dQuantity>0){
					qtyMap.put(elemKey,new Double(dQuantity));
					elemMap.put(elemKey,inventoryElem);
				}else{
					inventoryElem.setAttribute("IgnoreElement","Y");
				}				
			}else{
				double dCountQty = inventoryElem.getDoubleAttribute("Quantity");
				double dOtherQty = ((Double)qtyMap.get(elemKey)).doubleValue();
				qtyMap.put(elemKey, new Double(dCountQty +dOtherQty));
				inventoryElem.setAttribute("IgnoreElement","Y");					
			}
		}				
	}

	for(Iterator itr = elemMap.entrySet().iterator(); itr.hasNext();) {
		Map.Entry entry = (Map.Entry)itr.next();
		YFCElement itemDetailElem=(YFCElement)entry.getValue();
		String elemKey = itemDetailElem.getAttribute("ElementKey");
		double dOtherQty = ((Double)qtyMap.get(elemKey)).doubleValue();
		itemDetailElem.setAttribute("Quantity",dOtherQty);
	}
	request.setAttribute("Inventory",nodeInventoryElem);

	if(!bUpdateCount){
	   	ArrayList LocationInventoryList = new ArrayList();
		YFCElement nodeInventoryElem1 =(YFCElement)request.getAttribute("Inventory");
       	YFCElement locInvList1 = nodeInventoryElem1.getChildElement("LocationInventoryList");
       	YFCDocument root= YFCDocument.createDocument("Dummy");
       	YFCElement tempELem = nodeInventoryElem1.createChild("DummyElem");
       	YFCElement myNewInvElem1 = (YFCElement)root.importNode(tempELem.importNode(locInvList1),true);
       	YFCNodeList inventory1 = myNewInvElem1.getElementsByTagName("LocationInventory");
       	for(int j=0;j<inventory1.getLength();j++) {
    		YFCElement invElem1 = (YFCElement)inventory1.item(j);
        	YFCElement itemList1 = invElem1.getChildElement("ItemInventoryDetailList");
    		YFCNodeList items1 = itemList1.getElementsByTagName("ItemInventoryDetail");
    		YFCDocument oDoc = YFCDocument.createDocument("Dummy");
    		YFCElement oElem = nodeInventoryElem1.createChild("DummyElem");
    		for (int k=0;k<items1.getLength();k++){
    			YFCElement myNewInvElem = (YFCElement)oDoc.importNode(oElem.importNode(invElem1),true);
    			YFCElement itemDetailElem = (YFCElement)items1.item(k);
    			myNewInvElem.removeChild(myNewInvElem.getChildElement("ItemInventoryDetailList"));
    			myNewInvElem.createChild("ItemInventoryDetailList").importNode(itemDetailElem);
    			LocationInventoryList.add(myNewInvElem);
       		}
    	}
		Collections.sort(LocationInventoryList,new WMSCountUISortComparator());
		YFCElement getNodeInventoryOutputElem =(YFCElement)request.getAttribute("Inventory");
		YFCNodeList nodelist1 = getNodeInventoryOutputElem.getElementsByTagName("DummyElem");
		for (int i=0;i<nodelist1.getLength();i++){
			getNodeInventoryOutputElem.removeChild(nodelist1.item(i));
		}
		YFCElement newLocationInventoryList=getNodeInventoryOutputElem.createChild("LocationInventoryList");
		for (int i=0;i<LocationInventoryList.size();i++){
			YFCElement ele = (YFCElement)LocationInventoryList.get(i);
        	newLocationInventoryList.addXMLToNode(ele.getString());
		}
	}
%>
<%
	YFCElement countResultElem =(YFCElement)request.getAttribute("CountResultList");
	if(!isVoid(countResultElem)){
		YFCNodeList summaryResultList = countResultElem.getElementsByTagName("SummaryResultList");
		for(int k=0; k<summaryResultList.getLength(); k++){
			YFCElement summaryResultListElem = (YFCElement)summaryResultList.item(k);
			summaryResultListElem.setDoubleAttribute("NetSystemQuantity", 0);
			summaryResultListElem.setDoubleAttribute("NetCountQuantity", 0);
			YFCNodeList childCountResList = summaryResultListElem.getElementsByTagName("CountResult");
			for(int h=0; h<childCountResList.getLength(); h++){
				YFCElement childCountResElem = (YFCElement)childCountResList.item(h);
				//childCountResElem.setAttribute("AlreadyConsidered", "N");
				if(isVoid(childCountResElem.getAttribute("ItemID")) && 
					(!isVoid(childCountResElem.getAttribute("CaseId")) || !isVoid(childCountResElem.getAttribute("PalletId"))) ){
					childCountResElem.setAttribute("PureLPN", "Y");
				}
				if(!isVoid(childCountResElem.getAttribute("ItemID"))&& (childCountResElem.getDoubleAttribute("SystemQuantity")<=0) && (childCountResElem.getDoubleAttribute("CountQuantity")<=0) ){
					childCountResElem.setAttribute("ZeroRecord", "Y");
				}
				double dSystemQuantity = childCountResElem.getDoubleAttribute("SystemQuantity");
				double dCountQuantity = childCountResElem.getDoubleAttribute("CountQuantity");

				double dNetSystemQuantity = summaryResultListElem.getDoubleAttribute("NetSystemQuantity");
				double dNetCountQuantity = summaryResultListElem.getDoubleAttribute("NetCountQuantity");

				if(dNetCountQuantity>0){
					childCountResElem.setAttribute("AlreadyConsidered", "Y");
				}

				dNetSystemQuantity = dNetSystemQuantity+dSystemQuantity;
				dNetCountQuantity = dNetCountQuantity+dCountQuantity;
				
				summaryResultListElem.setDoubleAttribute("NetSystemQuantity", dNetSystemQuantity);
				summaryResultListElem.setDoubleAttribute("NetCountQuantity", dNetCountQuantity);

				summaryResultListElem.setAttributes(childCountResElem.getAttributes());
                if(childCountResElem!=null){
				YFCElement oCountTag = childCountResElem.getChildElement("CountResultTag");
				}
			}		
		}
	}
	request.setAttribute("CountResultList",countResultElem);

	if(bUpdateCount){
		ArrayList SummaryResultList1 = new ArrayList();
    	YFCElement countResultElem1 =(YFCElement)request.getAttribute("CountResultList");
    	if (countResultElem1 !=null){
    		YFCNodeList nodeSummaryResultlist1=countResultElem1.getElementsByTagName("SummaryResultList");
    		for (int i=0;i<nodeSummaryResultlist1.getLength();i++){
    			YFCElement eachSummResultElem1 = (YFCElement)nodeSummaryResultlist1.item(i);
    			SummaryResultList1.add(eachSummResultElem1);
    		}
    	}

    	Collections.sort(SummaryResultList1,new WMSCountUISummResultComparator());

		YFCElement countResultElem11 =(YFCElement)request.getAttribute("CountResultList");
		countResultElem11=YFCDocument.parse("<CountResultList/>").getDocumentElement();
		for (int i=0;i<SummaryResultList1.size();i++){
			YFCElement ele1 = (YFCElement)SummaryResultList1.get(i);
			countResultElem11.addXMLToNode(ele1.getString());
		}
		request.setAttribute("CountResultList",countResultElem11);

	}
%>
<div style="height:200px;overflow:auto">
<table class="table"  ID="CountResult" border="0" cellspacing="0" width="100%">
<thead>
    <tr> 
		<td sortable="no" class="checkboxheader">
			<input type="hidden" value=""/>
		</td>
        <td sortable="no" class="tablecolumnheader" width="30px">
            <yfc:i18n>Tag_Serial</yfc:i18n>
        </td>
        <td sortable="no" class="tablecolumnheader" nowrap="true" >
            <yfc:i18n>Enterprise</yfc:i18n>
        </td>
       <td sortable="no" class="tablecolumnheader"  nowrap="true"  >
            <yfc:i18n>Pallet_ID</yfc:i18n>
        </td>
        <td sortable="no" class="tablecolumnheader"  nowrap="true"  >
            <yfc:i18n>Case_ID</yfc:i18n>
        </td>
		<td sortable="no" class="tablecolumnheader" nowrap="true" >
            <yfc:i18n>Item_ID</yfc:i18n>
        </td>
        <td sortable="no" class="tablecolumnheader" nowrap="true" >
            <yfc:i18n>PC</yfc:i18n>
        </td>
        <td sortable="no" class="tablecolumnheader" nowrap="true" >
            <yfc:i18n>UOM</yfc:i18n>
        </td>
		<td sortable="no" class="tablecolumnheader" nowrap="true" >
            <yfc:i18n>Inventory_Status</yfc:i18n>
        </td>
        <td sortable="no" class="numerictablecolumnheader"  nowrap="true"  >
            <yfc:i18n>System_Quantity</yfc:i18n>
        </td>
		<td sortable="no" class="numerictablecolumnheader"  nowrap="true"  >
            <yfc:i18n>Count_Quantity</yfc:i18n>
        </td>
    </tr>
</thead>
<tbody> 
  	<%String className="oddrow";%>


<%
	if(!bUpdateCount){%>

	<yfc:loopXML binding="xml:Inventory:/NodeInventory/LocationInventoryList/@LocationInventory" id="LocationInventory" > 
<%
container.setAttribute("OrganizationCode", 		
		resolveValue("xml:/LocationInventory/InventoryItem/@OrganizationCode"));
container.setAttribute("ItemID", 
		resolveValue("xml:/LocationInventory/InventoryItem/@ItemID"));
container.setAttribute("ProductClass", 
		resolveValue("xml:/LocationInventory/InventoryItem/@ProductClass"));
container.setAttribute("UnitOfMeasure", 
		resolveValue("xml:/LocationInventory/InventoryItem/@UnitOfMeasure"));
container.setAttribute("Node", 
		resolveValue("xml:Inventory:/NodeInventory/@Node"));

	YFCElement elemItem = YFCDocument.createDocument("Item").getDocumentElement();
	request.setAttribute("Item",elemItem);
%>
	<yfc:callAPI apiID='AP1'/>
	
	<yfc:loopXML binding="xml:/LocationInventory/ItemInventoryDetailList/@ItemInventoryDetail" id="ItemInventoryDetail" > 
	<% sIgnoreElement = resolveValue("xml:/ItemInventoryDetail/@IgnoreElement");%>
	<% if(!equals(sIgnoreElement,"Y")) {%>

		<% sCounter="99"+LocationInventoryCounter.toString()+ItemInventoryDetailCounter.toString();
		if(temp == null){
		temp = sCounter;
		}%>
		<tr class='<%=className%>'>
			<%
					if (className.equals("oddrow"))
							className="evenrow";
						else
							className="oddrow";													
				%>
			<td class="tablecolumn"></td>
			<td>
			<%
			if(!YFCCommon.equals((String)request.getAttribute("SaveAllowed"),"N") ){
			bSerialTracked=false;			
			if(getValue("Item","xml:/Item/PrimaryInformation/@SerialCapturedInInventory").equals("Y")
			||(getValue("Item","xml:/Item/@TagCapturedInInventory").equals("Y"))
			||(getValue("Item","xml:/Item/@TagCapturedInInventory").equals("S"))
			||(getValue("Item","xml:/Item/InventoryParameters/@TimeSensitive").equals("Y"))){ 
			bSerialTracked=true;%>
			  <img onclick="disableCountQty('<%=sCounter%>');expandCollapseDetails('optionSet_<%=sCounter%>', '<%=getI18N("Click_To_See_Tag_Info")%>','<%=getI18N("Click_To_Hide_Tag_Info")%>','<%=YFSUIBackendConsts.FOLDER_COLLAPSE%>','<%=YFSUIBackendConsts.FOLDER_EXPAND%>')"  style="cursor:hand" <%=getImageOptions(YFSUIBackendConsts.FOLDER,"Click_To_See_Tag_Info")%> />
			  <%} }%>
			</td>

			<td class="tablecolumn"><yfc:getXMLValue name="LocationInventory" binding="xml:/LocationInventory/InventoryItem/@OrganizationCode"/></td>
			
			<td class="tablecolumn"><yfc:getXMLValue name="ItemInventoryDetail" binding="xml:/ItemInventoryDetail/@PalletId"/></td>
			<td class="tablecolumn"><yfc:getXMLValue name="ItemInventoryDetail" binding="xml:/ItemInventoryDetail/@CaseId"/></td>
			<td class="tablecolumn"><yfc:getXMLValue name="LocationInventory" binding="xml:/LocationInventory/InventoryItem/@ItemID"/></td>
			<td class="tablecolumn"><yfc:getXMLValue name="LocationInventory" binding="xml:/LocationInventory/InventoryItem/@ProductClass"/></td>
			<td class="tablecolumn"><yfc:getXMLValue name="LocationInventory" binding="xml:/LocationInventory/InventoryItem/@UnitOfMeasure"/></td>
			<td class="tablecolumn"><yfc:getXMLValue name="ItemInventoryDetail" binding="xml:/ItemInventoryDetail/@InventoryStatus"/></td>
			<td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:/ItemInventoryDetail/@Quantity")%>">
			   <yfc:getXMLValue name="ItemInventoryDetail" binding="xml:/ItemInventoryDetail/@Quantity"/>
			</td>
			<td class="numerictablecolumn">
				<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@CountQuantity")%>/>
				<% String sCaseId = resolveValue("xml:/ItemInventoryDetail/@CaseId");
			     %>
					<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@CaseId", "xml:/ItemInventoryDetail/@CaseId")%>/>
								
					<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@PalletId", "xml:/ItemInventoryDetail/@PalletId")%>/>
				
				<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@ItemID", "xml:/LocationInventory/InventoryItem/@ItemID")%>/>
				<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@ProductClass", "xml:/LocationInventory/InventoryItem/@ProductClass")%>/>
				<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@UnitOfMeasure", "xml:/LocationInventory/InventoryItem/@UnitOfMeasure")%>/>
				<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@OrganizationCode", "xml:/LocationInventory/InventoryItem/@OrganizationCode")%>/>
				<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@InventoryStatus", "xml:/ItemInventoryDetail/@InventoryStatus")%>/>		
				<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@SystemQuantity", "xml:/ItemInventoryDetail/@Quantity")%>/>				
			</td>
			<tr id='<%="optionSet_"+sCounter%>' class='<%=className%>' style="display:none">
				<td colspan="7" >
					<jsp:include page="/wms/count/detail/count_detail_includetag.jsp" flush="true">
						<jsp:param name="optionSetBelongingToLine" value='<%=sCounter%>'/>
						<jsp:param name="Modifiable" value='true'/>
						<jsp:param name="LabelTDClass" value='detaillabel'/>
						<jsp:param name="TotalBinding" value='xml:/RecordCountResult/CountResult'/>
						<jsp:param name="TagContainer" value='RecordCountResult'/>
						<jsp:param name="SerialTracked" value='<%=getValue("Item","xml:/Item/PrimaryInformation/@SerialCapturedInInventory")%>'/>
						<jsp:param name="NumSecondarySerials" value='<%=getValue("Item","xml:/Item/PrimaryInformation/@NumSecondarySerials")%>'/>
						<jsp:param name="TimeSensitive" value='<%=getValue("Item","xml:/Item/InventoryParameters/@TimeSensitive")%>'/>
						<jsp:param name="CaseId" value='<%=resolveValue("xml:/ItemInventoryDetail/@CaseId")%>'/>
						<jsp:param name="PalletId" value='<%=resolveValue("xml:/ItemInventoryDetail/@PalletId")%>'/>
						<jsp:param name="ItemID" value='<%=resolveValue("xml:/LocationInventory/InventoryItem/@ItemID")%>'/>
						<jsp:param name="ProductClass" value='<%=resolveValue("xml:/LocationInventory/InventoryItem/@ProductClass")%>'/>
						<jsp:param name="UnitOfMeasure" value='<%=resolveValue("xml:/LocationInventory/InventoryItem/@UnitOfMeasure")%>'/>
						<jsp:param name="OrganizationCode" value='<%=resolveValue("xml:/LocationInventory/InventoryItem/@OrganizationCode")%>'/>
						<jsp:param name="InventoryStatus" value='<%=resolveValue("xml:/ItemInventoryDetail/@InventoryStatus")%>'/>
						<jsp:param name="Segment" value='<%=resolveValue("xml:/ItemInventoryDetail/@Segment")%>'/>
						<jsp:param name="SegmentType" value='<%=resolveValue("xml:/ItemInventoryDetail/@SegmentType")%>'/>

					</jsp:include>
				</td>
			</tr>
			<tr style="display:none">
			</tr>
		</tr>
	<%}%>
    </yfc:loopXML> 
    </yfc:loopXML> 
	<%}
		else{%>
<yfc:loopXML binding="xml:CountResultList:CountResultList/@SummaryResultList" id="SummaryResultList" >
<%
container.setAttribute("OrganizationCode", 		
		resolveValue("xml:/SummaryResultList/@OrganizationCode"));
container.setAttribute("ItemID", 
		resolveValue("xml:/SummaryResultList/@ItemID"));
container.setAttribute("ProductClass", 
		resolveValue("xml:/SummaryResultList/@ProductClass"));
container.setAttribute("UnitOfMeasure", 
		resolveValue("xml:/SummaryResultList/@UnitOfMeasure"));
container.setAttribute("Node", 
		resolveValue("xml:/SummaryResultList/CountRequest/@Node"));

	YFCElement elemItem = YFCDocument.createDocument("Item").getDocumentElement();
	request.setAttribute("Item",elemItem);
	YFCElement TagInformation = YFCDocument.createDocument("Tag").getDocumentElement();
	request.setAttribute("Tag",TagInformation);
	YFCElement CountInformation = YFCDocument.createDocument("Count").getDocumentElement();
	request.setAttribute("Count",CountInformation);
%>

	<yfc:loopXML binding="xml:/SummaryResultList/@CountResult" id="CountResult">  
	
	<%
	YFCElement oCountResultForTagInfo = (YFCElement)request.getAttribute("CountResultList");
	if(oCountResultForTagInfo!=null){
	    YFCNodeList SummaryList = (YFCNodeList)oCountResultForTagInfo.getElementsByTagName("SummaryResultList");
		YFCElement oSummaryList = (YFCElement)SummaryList.item(SummaryResultListCounter.intValue()-1);
		 if(oSummaryList!=null){
		  YFCNodeList CRList = (YFCNodeList)oSummaryList.getElementsByTagName("CountResult");
           YFCElement oCountResult = (YFCElement)CRList.item(CountResultCounter.intValue()-1);
		    if(oCountResult!=null){
              CountInformation.setAttributes(oCountResult.getAttributes());
			   YFCElement oCountResultTag = oCountResult.getChildElement("CountResultTag");
				 if(oCountResultTag!=null){
				 TagInformation.setAttributes(oCountResultTag.getAttributes());
			    }
			}
		}
	}
	    sTagInformation = (String)TagInformation.toString();
		sCountResultInformation = (String)CountInformation.toString();
		sPureLPN = resolveValue("xml:/CountResult/@PureLPN");%>
	<% sZeroRecord = resolveValue("xml:/CountResult/@ZeroRecord");%>
	
	<% if(!equals(sPureLPN,"Y") && !equals(sZeroRecord,"Y")) {%>

		<%sCounter="10"+SummaryResultListCounter.toString()+CountResultCounter.toString();%>
		<% sAlreadyConsidered = resolveValue("xml:/CountResult/@AlreadyConsidered");%>
		
	
	<yfc:callAPI apiID='AP1'/>
		
		<tr class='<%=className%>'>
			<%
					if (className.equals("oddrow"))
							className="evenrow";
						else
							className="oddrow";													
				%>
			<td class="tablecolumn"></td>
			<td>
			<%
			if(!YFCCommon.equals((String)request.getAttribute("SaveAllowed"),"N") ){
			bSerialTracked=false;			
			if(getValue("Item","xml:/Item/PrimaryInformation/@SerialCapturedInInventory").equals("Y")
			||(getValue("Item","xml:/Item/@TagCapturedInInventory").equals("Y"))
			||(getValue("Item","xml:/Item/@TagCapturedInInventory").equals("S"))
			||(getValue("Item","xml:/Item/InventoryParameters/@TimeSensitive").equals("Y"))){ 
			bSerialTracked=true;%>
			  <img onclick="disableCountQty('<%=sCounter%>');expandCollapseDetails('optionSet_<%=sCounter%>', '<%=getI18N("Click_To_See_Tag_Info")%>','<%=getI18N("Click_To_Hide_Tag_Info")%>','<%=YFSUIBackendConsts.FOLDER_COLLAPSE%>','<%=YFSUIBackendConsts.FOLDER_EXPAND%>')"  style="cursor:hand" <%=getImageOptions(YFSUIBackendConsts.FOLDER,"Click_To_See_Tag_Info")%> />
			  <%} }%>
			</td>

			<td class="tablecolumn"><yfc:getXMLValue name="CountResult" binding="xml:/CountResult/@OrganizationCode"/></td>
			<td class="tablecolumn"><yfc:getXMLValue name="CountResult" binding="xml:/CountResult/@PalletId"/></td>
			<td class="tablecolumn"><yfc:getXMLValue name="CountResult" binding="xml:/CountResult/@CaseId"/></td>
			<td class="tablecolumn"><yfc:getXMLValue name="CountResult" binding="xml:/CountResult/@ItemID"/></td>
			<td class="tablecolumn"><yfc:getXMLValue name="CountResult" binding="xml:/CountResult/@ProductClass"/></td>
			<td class="tablecolumn"><yfc:getXMLValue name="CountResult" binding="xml:/CountResult/@UnitOfMeasure"/></td>
			<td class="tablecolumn"><yfc:getXMLValue name="CountResult" binding="xml:/CountResult/@InventoryStatus"/></td>
			<td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:/SummaryResultList/@SystemQuantity")%>">
			   <yfc:getXMLValue name="CountResult" binding="xml:/CountResult/@SystemQuantity"/>
			</td>
			<td class="numerictablecolumn">
				<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@CountQuantity",
				"xml:/CountResult/@CountQuantity")%>/>
				<% String sCaseId = resolveValue("xml:/SummaryResultList/@CaseId");
				   if(!isVoid(sCaseId)) {%>
					<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@CaseId", "xml:/SummaryResultList/CountResult/@CaseId")%>/>
				<%}else {%>				
					<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@PalletId", "xml:/SummaryResultList/CountResult/@PalletId")%>/>
				<%}%>
				
				<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@ItemID", "xml:/CountResult/@ItemID")%>/>
				<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@ProductClass", "xml:/CountResult/@ProductClass")%>/>
				<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@UnitOfMeasure", "xml:/CountResult/@UnitOfMeasure")%>/>
				<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@OrganizationCode", "xml:/CountResult/@OrganizationCode")%>/>
				<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@InventoryStatus", "xml:/CountResult/@InventoryStatus")%>/>		
				<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sCounter+"/@SystemQuantity", "xml:/CountResult/@SystemQuantity")%>/>				
			</td>

			 <tr id='<%="optionSet_"+sCounter%>' class='<%=className%>' style="display:none">
				<td colspan="7" >
					<jsp:include page="/wms/count/detail/count_detail_includetag.jsp" flush="true">
						<jsp:param name="optionSetBelongingToLine" value='<%=sCounter%>'/>
						<jsp:param name="Modifiable" value='true'/>
						<jsp:param name="LabelTDClass" value='detaillabel'/>
						<jsp:param name="TotalBinding" value='xml:/RecordCountResult/CountResult'/>
						<jsp:param name="TagContainer" value='RecordCountResult'/>
						<jsp:param name="TagInfo" value='<%=sTagInformation%>'/>
						<jsp:param name="CountInfo" value='<%=sCountResultInformation%>'/>
						<jsp:param name="SerialTracked" value='<%=getValue("Item","xml:/Item/PrimaryInformation/@SerialCapturedInInventory")%>'/>
						<jsp:param name="NumSecondarySerials" value='<%=getValue("Item","xml:/Item/PrimaryInformation/@NumSecondarySerials")%>'/>
						<jsp:param name="TimeSensitive" value='<%=getValue("Item","xml:/Item/InventoryParameters/@TimeSensitive")%>'/>
						<jsp:param name="CaseId" value='<%=resolveValue("xml:/SummaryResultList/CountResult/@CaseId")%>'/>
						<jsp:param name="PalletId" value='<%=resolveValue("xml:/SummaryResultList/CountResult/@PalletId")%>'/>
						<jsp:param name="ItemID" value='<%=resolveValue("xml:/CountResult/@ItemID")%>'/>
						<jsp:param name="ProductClass" value='<%=resolveValue("xml:/CountResult/@ProductClass")%>'/>
						<jsp:param name="UnitOfMeasure" value='<%=resolveValue("xml:/CountResult/@UnitOfMeasure")%>'/>
						<jsp:param name="OrganizationCode" value='<%=resolveValue("xml:/CountResult/@OrganizationCode")%>'/>
						<jsp:param name="InventoryStatus" value='<%=resolveValue("xml:/CountResult/@InventoryStatus")%>'/>
						<jsp:param name="Segment" value='<%=resolveValue("xml:/CountResult/@Segment")%>'/>
						<jsp:param name="SegmentType" value='<%=resolveValue("xml:/CountResult/@SegmentType")%>'/>
						<jsp:param name="SerialNo" value='<%=resolveValue("xml:/CountResult/@SerialNo")%>'/>
						<jsp:param name="ShipByDate" value='<%=resolveValue("xml:/CountResult/@ShipByDate")%>'/>
						<jsp:param name="InventoryTagKey" value='<%=resolveValue("xml:/CountResult/CountResultTag/@InventoryTagKey")%>'/>
					</jsp:include>
				</td>
			</tr> 
			</tr>
			<tr style="display:none">
		</tr>
	 
	<%}%>
	</yfc:loopXML> 
	</yfc:loopXML> 
	 <%}%>
	

 
</tbody>
<% 
	if(!YFCCommon.equals((String)request.getAttribute("SaveAllowed"),"N") ){
		bSerialTracked=true;%>
		<% sCounter = temp;%>
<tfoot>
    <tr style='display:none' TemplateRow="true">
		<td class="checkboxcolumn" ></td>
  		<td>
			<img onclick="disableCountQty('<%=sCounter%>');expandCollapseDetails('optionSet_<%=sCounter%>', '<%=getI18N("Click_To_Refresh_and_identify_Tag_Serial_Items")%>','<%=getI18N("Click_To_Refresh_and_identify_Tag_Serial_Items")%>','<%=YFSUIBackendConsts.FOLDER_COLLAPSE%>','<%=YFSUIBackendConsts.FOLDER_EXPAND%>')"  style="cursor:hand" <%=getImageOptions(YFSUIBackendConsts.FOLDER,"Click_To_Refresh_and_identify_Tag_Serial_Items")%> />
		</td>
		<td class="tablecolumn" nowrap="true">
			 <select class="combobox" name="xml:/RecordCountResult/CountResult_/@OrganizationCode">
					<yfc:loopOptions binding="xml:EnterpriseList:/OrganizationList/@Organization" name="OrganizationCode"
                value="OrganizationCode"/>
			</select>
		</td>
		 
        <td nowrap="true" class="tablecolumn">
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/RecordCountResult/CountResult_/@PalletId")%>/>
        </td>
		 <td nowrap="true" class="tablecolumn">
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/RecordCountResult/CountResult_/@CaseId")%>/>
        </td>
		<td nowrap="true" class="tablecolumn">
            <input type="text" class="unprotectedinput"  <%=getTextOptions("xml:/RecordCountResult/CountResult_/@ItemID")%>/> 
			<img class="lookupicon" onclick="templateRowCallItemLookup(this,'ItemID','ProductClass','UnitOfMeasure','item','xml:/Item/@CallingOrganizationCode=' +  document.all['xml:/Task/@EnterpriseKey'].value )" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Item")%>/>
        </td>
		<td nowrap="true" class="tablecolumn"  >
		<select class="combobox"  <%=getComboOptions("xml:/RecordCountResult/CountResult_/@ProductClass")%>>
				<yfc:loopOptions binding="xml:ProductClass:/CommonCodeList/@CommonCode" 
					name="CodeValue" value="CodeValue" selected="xml:/RecordCountResult/CountResult_/@ProductClass"/>
		</select>
        </td>
		<td nowrap="true" class="tablecolumn">
			<select <%=getComboOptions("xml:/RecordCountResult/CountResult_/@UnitOfMeasure")%> class="combobox"  >
				<yfc:loopOptions binding="xml:UnitOfMeasureList:/ItemUOMMasterList/@ItemUOMMaster" name="UnitOfMeasure" value="UnitOfMeasure" selected="xml:/RecordCountResult/CountResult_/@UnitOfMeasure"/>
			</select>
		</td>
		<td nowrap="true" class="tablecolumn">
	       	<select name="xml:/RecordCountResult/CountResult_/@InventoryStatus" class="combobox" >
            <yfc:loopOptions binding="xml:InventoryStatusList:/InventoryStatusList/@InventoryStatus" 
                name="InventoryStatus" value="InventoryStatus" selected="xml:/RecordCountResult/CountResult_/@InventoryStatus"/>
		    </select>
        </td>
		<td/>
		<td nowrap="true" class="numerictablecolumn" >
			<input type="text" class="unprotectedinput"  <%=getTextOptions("xml:/RecordCountResult/CountResult_/@CountQuantity")%>/> 
        </td>
	</tr>
	<tr>
    	<td nowrap="true" colspan="15">
    		<jsp:include page="/common/editabletbl.jsp" flush="true"/>
    	</td>
    </tr>
</tfoot>
<%}%>
</table>
</div>
<%!
 private String prepareKey(YFCElement itemInventoryElement) {
	 StringBuffer resultKey = new StringBuffer("|");
	 resultKey.append(itemInventoryElement.getAttribute("InventoryItemKey")).append("|");
	 resultKey.append(itemInventoryElement.getAttribute("CaseId")).append("|");
	 resultKey.append(itemInventoryElement.getAttribute("PalletId")).append("|");	
	 resultKey.append(itemInventoryElement.getAttribute("InventoryStatus")).append("|");
	 return resultKey.toString();
}
%>
