<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ include file="/console/jsp/inventory.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<SCRIPT LANGUAGE="JavaScript">
	function toggleSerialScanning(sCounter) {
		var docInputs=document.getElementsByTagName("tr");		
		for (var i=0;i<docInputs.length;i++) {
		var docInput=docInputs.item(i);
		if(docInput.getAttribute("action") == "ADD"){
			var tds = docInput.getElementsByTagName("td");
			for(var j=0;j<tds.length;j++){
				var td = tds.item(j);
				if( td.getAttribute("id") == "singleserialtext"+sCounter){
					if(td.style.display == "none")		
						td.style.display = '';
					else
						td.style.display = 'none'; 		
				}
				if(td.getAttribute("id") == "serialrangefromtext"+sCounter){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				if(td.getAttribute("id") == "serialrangefromlabel"+sCounter){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				if(td.getAttribute("id") == "serialrangetotext"+sCounter){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				if(td.getAttribute("id") == "serialrangetolabel"+sCounter){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				if(td.getAttribute("id") == "countqty"+sCounter){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				if(td.getAttribute("id") == "countqtyheader"+sCounter){
					alert("INSIDE COUNT HEADER");
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				
			}
		}else{
			var tds = docInput.getElementsByTagName("td");
			for(var j=0;j<tds.length;j++){
				var td = tds.item(j);
			if(td.getAttribute("id") == "countqtyheader"+sCounter){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
			}

		}
		if (docInput.getAttribute("TemplateRow") == "true") {
			var tds = docInput.getElementsByTagName("td");
			
			for(var j=0;j<tds.length;j++){
				var td = tds.item(j);
				if( td.getAttribute("id") == "singleserialtext"+sCounter){
					if(td.style.display == "none")		
						td.style.display = '';
					else
						td.style.display = 'none'; 		
				}
				if(td.getAttribute("id") == "serialrangefromtext"+sCounter){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				if(td.getAttribute("id") == "serialrangefromlabel"+sCounter){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				if(td.getAttribute("id") == "serialrangetotext"+sCounter){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				if(td.getAttribute("id") == "serialrangetolabel"+sCounter){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				if(td.getAttribute("id") == "countqty"+sCounter){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				if(td.getAttribute("id") == "countqtyheader"+sCounter){
					alert("INSIDE COUNT HEADER");
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}


			}
			
		}
		
	}


}
</SCRIPT>
<% 
  int tagtrack=0; 
  int NoSecSerials = 0;
  String sNoSecSerials = request.getParameter("NumSecondarySerials");
  String sSerialNo = request.getParameter("SerialNo");
  String sShipByDate = request.getParameter("ShipByDate");
  String sInventoryTagKey = request.getParameter("InventoryTagKey");
  String sTagAttribute="";
  String sCountQty="";
  if(!isVoid(sNoSecSerials)){
	 NoSecSerials = (new Integer(sNoSecSerials)).intValue();
  }	 
  String isSerialRange = resolveValue("xml:/RecordCountResult/@IsSerialRangeEntry");
	
  String sourceChecked="No";
  String targetChecked="No";
  String eitherChecked="No";

	if (equals(isSerialRange,"01")) { 
        sourceChecked = "01";
    }
	else if (equals(isSerialRange,"02")) { 
	    targetChecked = "02";
	}
	else {
		eitherChecked = " ";
	}
%>
<table class="table" width="100%" >
<tbody>
    <tr id='<%="defaulttable"+request.getParameter("optionSetBelongingToLine")%>' >
		<td width="10%" >
			&nbsp;
		</td>
<td width="60%" style="border:1px solid black">
<table class="table" editable="true" width="100%" cellspacing="0">
<%
	Map identifierAttrMap=null;
	Map descriptorAttrMap=null;
	Map extnIdentifierAttrMap=null;
	Map extnDescriptorAttrMap=null;
	String tagContainer  = request.getParameter("TagContainer");
	if (isVoid(tagContainer)) {
		tagContainer = "TagContainer";
	}
	String tagElement  = request.getParameter("TagElement");
	if (isVoid(tagElement)) {
		tagElement = "Tag";
	}
	YFCElement TagDetailElem = YFCDocument.createDocument("TagDetail").getDocumentElement();
	request.setAttribute("TagDetail",TagDetailElem);
	String tagInfo  = request.getParameter("TagInfo");
	if(!isVoid(tagInfo)){
	YFCElement TagDetailElem1 = YFCDocument.parse(tagInfo).getDocumentElement();
	 if(TagDetailElem1!=null){
	    TagDetailElem.setAttributes(TagDetailElem1.getAttributes());
	   }
    }
	String countInfo  = request.getParameter("CountInfo");
	if(!isVoid(countInfo)){
	YFCElement CountDetailElem = YFCDocument.parse(countInfo).getDocumentElement();
	if(CountDetailElem!=null){
       sCountQty = CountDetailElem.getAttribute("CountQuantity");
	 } else{
	   sCountQty="";
	 }
	}
	%>  
	 <yfc:hasXMLNode binding="xml:Item:/Item/InventoryTagAttributes">
	<%
		prepareTagDetails ((YFCElement) request.getAttribute(tagContainer),tagElement,(YFCElement) request.getAttribute("Item"));
		identifierAttrMap = getTagAttributesMap((YFCElement) request.getAttribute("Item"),"IdentifierAttributes");
		descriptorAttrMap = getTagAttributesMap((YFCElement) request.getAttribute("Item"),"DescriptorAttributes");
		extnIdentifierAttrMap = getTagAttributesMap((YFCElement) request.getAttribute("Item"),"ExtnIdentifierAttributes");
		extnDescriptorAttrMap = getTagAttributesMap((YFCElement) request.getAttribute("Item"),"ExtnDescriptorAttributes");
	%>
	 </yfc:hasXMLNode>
	<%
	String modifiable = request.getParameter("Modifiable");
	boolean isModifiable = false;
	String sOptionCounter = request.getParameter("optionSetBelongingToLine");
	if (equals(modifiable,"true")) {
		isModifiable = true;
	}%>
    <thead> 
   <tr>
   <td class="tablecolumnheader">&nbsp
   </td>
	<%
	int i = 0;
	String isTagCaptured = getValue("Item","xml:/Item/@TagCapturedInInventory");
    if(isTagCaptured.equals("Y") || isTagCaptured.equals("S")){ 
		while (i < 2) { 
			int j = 0;
			Map normalMap = null;
			Map extnMap = null;
			Map currentMap = null;
			if (i == 0) {
				normalMap = identifierAttrMap;
				extnMap = extnIdentifierAttrMap;
			} else {
				normalMap = descriptorAttrMap;
				extnMap = extnDescriptorAttrMap;
			}		
			if ((normalMap != null) || (extnMap != null)) {
				tagtrack=1;%>
						
								<%while (j < 2) {
									boolean isExtn = false;
									if (j == 0) {
										currentMap = normalMap;
										isExtn = false;
									} else {
										currentMap = extnMap;
										isExtn = true;
									}
									if (currentMap != null) {
										if (!currentMap.isEmpty()) {
											for (Iterator k = currentMap.keySet().iterator(); k.hasNext();) {
												String currentAttr = (String) k.next();
												String currentAttrValue = (String) currentMap.get(currentAttr);%>
												<td class="tablecolumnheader"><yfc:i18n><%=currentAttr%></yfc:i18n></td>
											<%
											}
										}
									}
									
									j++;
								}%>
			<%}
			i++;
		}
	}%>				
				<%if(equals("Y",request.getParameter("TimeSensitive"))){%>
					<td id='<%="serialheader"+request.getParameter("optionSetBelongingToLine")%>' sortable="no" class="tablecolumnheader" colspan="4">
						<yfc:i18n>Ship_By_Date</yfc:i18n>
					</td>
				<%}%>
		
				<%if(equals("Y",request.getParameter("SerialTracked"))){%>
					<td id='<%="serialheader"+request.getParameter("optionSetBelongingToLine")%>' sortable="no" class="tablecolumnheader" colspan="4"> 
						<yfc:i18n>Serial_#</yfc:i18n>
					</td>
					<% for (int s=1; s <= NoSecSerials ; s++){
					  String serLabel= "Secondary_Serial_"+s; %>
						<td sortable="no" class="tablecolumnheader">
							<yfc:i18n><%=serLabel%></yfc:i18n>
						</td>
					<%}%>	
				<%}%>
					<td id='<%="countqtyheader"+request.getParameter("optionSetBelongingToLine")%>'  sortable="no" class="tablecolumnheader" > 
						<yfc:i18n>Count_Quantity</yfc:i18n>
					</td>
				<% if(NoSecSerials==0){ %>
					<td>
						<img class="lookupicon" name="search" onclick="toggleSerialScanning(<%=sOptionCounter%>);return false" <%=getImageOptions(YFSUIBackendConsts.MILESTONE_COLUMN, "Toggle_Serial_Entry") %> />
					</td>
				<% } %>
</tr>
</thead>
<tbody>
</tbody>
 <tfoot>
 <tr id='SingleSerial' style='display:none' TemplateRow="true">
    <td class="checkboxcolumn" >
    </td>
	<%
	String binding = request.getParameter("TotalBinding");
	//String sOptionCounter = request.getParameter("optionSetBelongingToLine");
	i = 0;
	while (i < 2) { 
		int j = 0;
		Map normalMap = null;
		Map extnMap = null;
		Map currentMap = null;
		if (i == 0) {
			normalMap = identifierAttrMap;
			extnMap = extnIdentifierAttrMap;
	
		} else {
			normalMap = descriptorAttrMap;
			extnMap = extnDescriptorAttrMap;
	
		}		
		if ((normalMap != null) || (extnMap != null)) {%>

						<%while (j < 2) {
								
                                boolean isExtn = false;
								if (j == 0) {
									currentMap = normalMap;
									isExtn = false;
							
								} else {
									currentMap = extnMap;
									isExtn = true;
								}
								if (currentMap != null) {
									if (!currentMap.isEmpty()) {
										   for (Iterator k = currentMap.keySet().iterator(); k.hasNext();) {
									        String currentAttr = (String) k.next();
									        String currentAttrValue = (String) currentMap.get(currentAttr);
											String sCountResultTagBinding = "/CountResultTag/@" ;
											if(isExtn){
												sCountResultTagBinding = "/CountResultTag/Extn/@" ;
											}
											if(TagDetailElem!=null){
												sTagAttribute = TagDetailElem.getAttribute(currentAttr);
												}else{
												sTagAttribute = "";
											}
										
											
%>
	    <td nowrap="true" class="tablecolumn">
            <input type="text" class="unprotectedinput" <%=getTextOptions(binding + "_"+sOptionCounter+sCountResultTagBinding+currentAttr,sTagAttribute) %>/>
       </td>
											
									    	<%
								           }
							        }
							}
					        	
					        	j++;
					        }}
		i++;
}%> 

	<%if(equals("Y",request.getParameter("TimeSensitive"))){%>
	    <td nowrap="true" class="tablecolumn">
              <input type="text" class="unprotectedinput" <%=getTextOptions(binding + "_" +sOptionCounter+"/@ShipByDate", sShipByDate)%>/>
			<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
        </td>
	<%}%>
	<%if(equals("Y",request.getParameter("SerialTracked"))){%>
	   <td nowrap="true" id='<%="singleserialtext"+request.getParameter("optionSetBelongingToLine")%>' class="tablecolumn" style='display:' colspan="4">
		   <input type="text" class="unprotectedinput"   <%=getTextOptions(binding+ "_" +sOptionCounter+"/@SerialNo")%>/>
       </td>
	   		<td class="detaillabel" id='<%="serialrangefromtext"+request.getParameter("optionSetBelongingToLine")%>' style='display:none'>
            <yfc:i18n>From_Serial_#</yfc:i18n>
		</td>

		<td nowrap="true" id='<%="serialrangefromlabel"+request.getParameter("optionSetBelongingToLine")%>' class="tablecolumn" style='display:none' >
			<input type="text" class="unprotectedinput"   <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sOptionCounter+"/SerialRange/@FromSerialNo")%>/>		     
		</td>

		<td class="detaillabel" id='<%="serialrangetolabel"+request.getParameter("optionSetBelongingToLine")%>' style='display:none'>
            <yfc:i18n>To_Serial_#</yfc:i18n>
		</td> 

		<td nowrap="true" id='<%="serialrangetotext"+request.getParameter("optionSetBelongingToLine")%>' class="tablecolumn" style='display:none' >
		   <input type="text" class="unprotectedinput"   <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sOptionCounter+"/SerialRange/@ToSerialNo")%>/>
       </td>
	  <% for (int s=1; s <= NoSecSerials; s++){%>
			<td>
				<input type="text" class="unprotectedinput" <%=getTextOptions(binding + "_" +sOptionCounter+"/@SecondarySerial"+s)%>/>
			</td>
		<%}%>
	<%}%>
	   	<td nowrap="true" class="tablecolumn" id='<%="countqty"+request.getParameter("optionSetBelongingToLine")%>' >
		  <%if(equals("Y",request.getParameter("SerialTracked"))){
			if(isVoid(sCountQty)){%>
			 <input type="text" class="unprotectedinput"  <%=getTextOptions(binding + "_" +sOptionCounter+"/@CountQuantity","1")%>/>
		   <%}else{%>
           <input type="text" class="unprotectedinput" <%=getTextOptions(binding + "_" +sOptionCounter+"/@CountQuantity",sCountQty)%>/>
			<%}
			}else{%>
			<input type="text" class="unprotectedinput" <%=getTextOptions(binding + "_" +sOptionCounter+"/@CountQuantity",sCountQty)%>/>
		   <%}%>
	   </td>
	   <td nowrap="true" class="tablecolumn">
   			<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sOptionCounter+"/@IsNewRow","Y")%>/> 
	      	<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sOptionCounter+"/@CaseId", request.getParameter("CaseId"))%>/>
			<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sOptionCounter+"/@PalletId", request.getParameter("PalletId"))%>/>
			<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sOptionCounter+"/@ItemID", request.getParameter("ItemID"))%>/>
			<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sOptionCounter+"/@ProductClass", request.getParameter("ProductClass"))%>/>
			<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sOptionCounter+"/@UnitOfMeasure", request.getParameter("UnitOfMeasure"))%>/>
			<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sOptionCounter+"/@OrganizationCode", request.getParameter("OrganizationCode"))%>/>
			<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sOptionCounter+"/@InventoryStatus", request.getParameter("InventoryStatus"))%>/>
			<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sOptionCounter+"/@Segment", request.getParameter("Segment"))%>/>
			<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_"+sOptionCounter+"/@SegmentType", request.getParameter("SegmentType"))%>/>
	   </td>
	 </tr>
	 <tr>
    	<td nowrap="true" colspan="15">
    		<jsp:include page="/common/editabletbl.jsp" flush="true"/>
    	</td>
    </tr>
</tfoot>
</table>
</td>
</tr>
</tbody>
</table>	