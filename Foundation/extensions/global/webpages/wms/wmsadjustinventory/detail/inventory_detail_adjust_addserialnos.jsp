<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ include file="/yfsjspcommon/editable_util_lines.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<script>
function processSaveRecordsForChildNode() {
   var addRow = window.document.getElementById("userOperation");
    var numRowsToAdd = window.document.getElementById("numRowsToAdd");
    if(addRow)
    {
        if(addRow.value != 'Y')
        {
            //reset numRowsToAdd attribute
            if(numRowsToAdd)
                numRowsToAdd.value="";
            yfcSpecialChangeNames("specialChange", false);
        }
    }
    else
        yfcSpecialChangeNames("specialChange", false);
}
</script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript">
    document.body.attachEvent("onunload", processSaveRecordsForChildNode);
</script>
<SCRIPT LANGUAGE="JavaScript">
	function toggleSerialScanning(sCounter) {
		var docInputs=document.getElementsByTagName("tr");	
		var docTds=document.getElementsByTagName("td");	
		
		for(var s=0;s<docTds.length;s++){
				var td = docTds.item(s);
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
		}

		for (var i=0;i<docInputs.length;i++) {
		var docInput=docInputs.item(i);
			var tds = docInput.getElementsByTagName("td");
			if(docInput.getAttribute("action") == "ADD"){
				var tds = docInput.getElementsByTagName("td");
				for(var j=0;j<tds.length;j++){
				var td = tds.item(j);
				if( td.getAttribute("id") == "singleserialtext"){
					if(td.style.display == "none")		
						td.style.display = '';
					else
						td.style.display = 'none'; 		
				}
				if(td.getAttribute("id") == "serialrangefromtext"){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				if(td.getAttribute("id") == "serialrangefromlabel"){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				if(td.getAttribute("id") == "serialrangetotext"){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				if(td.getAttribute("id") == "serialrangetolabel"){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				} 
		}

			}

		if (docInput.getAttribute("TemplateRow") == "true") {
			var tds = docInput.getElementsByTagName("td");
			
			for(var k=0;k<tds.length;k++){
				var td = tds.item(k);
				if( td.getAttribute("id") == "singleserialtext"){
					if(td.style.display == "none")	{
						td.style.display = '';
					}
						
					else{
						td.style.display = 'none'; 		
					}
						
				}
				if(td.getAttribute("id") == "serialrangefromtext"){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				if(td.getAttribute("id") == "serialrangefromlabel"){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				if(td.getAttribute("id") == "serialrangetotext"){
					if(td.style.display == "none")
						td.style.display = "";
					else
						td.style.display = "none";
				}
				if(td.getAttribute("id") == "serialrangetolabel"){
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
<% String height = request.getParameter("height"); 
String currentSerialCounter = request.getAttribute("SerialCounter").toString();
String newCounter = "";%>
<%int currentCounter = Integer.parseInt(currentSerialCounter);%>
<%double numSecondarySerials=getNumericValue("xml:ItemDetails:/Item/PrimaryInformation/@NumSecondarySerials");
%>
<div style="height:120px;overflow:auto">
<table class="table" ID="specialChange" cellspacing="0" width="100%" >
    <thead>
        <tr>
            <td class="checkboxheader" sortable="no" id="hiddenattributes" >&nbsp;
				<input type="hidden" id="userOperation" name="userOperation" value="" />
				<input type="hidden" id="numRowsToAdd" name="numRowsToAdd" value="" />
				<input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
            </td>
           <td class="tablecolumnheader" nowrap="true" colspan="4"  style="width:<%=getUITableSize("xml:/AdjustLocationInventory/Source/Inventory/SerialList/SerialDetail/@SerialNo")%>"><yfc:i18n>Serial_#</yfc:i18n></td>
	
		   <% for (int i=1; i < numSecondarySerials+1 ; i++){
			  String serLabel= "Secondary_Serial_" + i;
			%>
			<td class="tablecolumnheader" nowrap="true" style="width:<%=getUITableSize("xml:/AdjustLocationInventory/Source/Inventory/SerialList/SerialDetail/@SecondarySerial"+i)%>"><yfc:i18n><%=serLabel%></yfc:i18n></td>
			<%}%>
			<% if(numSecondarySerials==0){ %>
			<td>
				<img class="lookupicon" name="search" onclick="toggleSerialScanning(<%=currentSerialCounter%>);return false" <%=getImageOptions(YFSUIBackendConsts.MILESTONE_COLUMN, "Toggle_Serial_Entry") %> />
			</td>
			<% } %>
        </tr>
    </thead>
    <tbody>
    <yfc:loopXML name="AdjustLocationInventory" binding="xml:/AdjustLocationInventory/Source/Inventory/SerialList/@SerialDetail" id="SerialDetail" > 
<%	if(YFCCommon.isVoid(resolveValue("xml:SerialDetail:/SerialDetail/@Quantity")) || (1 == getNumericValue("xml:SerialDetail:/SerialDetail/@Quantity"))){ %>
		<tr DeleteRowIndex="<%=SerialDetailCounter%>">
		<%int serialCounter = SerialDetailCounter.intValue();
            newCounter = String.valueOf(serialCounter+(currentCounter-1)); %>
			<td class="checkboxcolumn"> 
				<img class="icon" onclick="setDeleteOperationForRow(this,'xml:/AdjustLocationInventory/Source/Inventory/SerialList/SerialDetail')" <%=getImageOptions(YFSUIBackendConsts.DELETE_ICON, "Remove_Row")%>/>
			</td>
			<td nowrap="true" id='<%="singleserialtext"+currentSerialCounter%>' class="tablecolumn" style='display:' colspan="4">
				<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/SerialList/SerialDetail_"+newCounter+"/@DeleteRow",  "")%> />
				<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/SerialList/SerialDetail_"+newCounter+"/@Action","xml:/SerialDetail/@Action","CREATE")%> />
				<input type="text" class="unprotectedinput" OldValue="" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/SerialList/SerialDetail_" + newCounter +"/@SerialNo","xml:/SerialDetail/@SerialNo")%>/>
				<input type="hidden" class="unprotectedinput" OldValue="" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/SerialList/SerialDetail_" + newCounter  +"/@Quantity","xml:/SerialDetail/@Quantity","1")%>/> 
			</td>
			<td class="detaillabel" id='<%="serialrangefromlabel"+currentSerialCounter%>' style='display:none'>
            <yfc:i18n>From_Serial_#</yfc:i18n>
		</td>

		<td nowrap="true" id='<%="serialrangefromtext"+currentSerialCounter%>' class="tablecolumn" style='display:none' >
			<input type="text" class="unprotectedinput" OldValue=""  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/SerialRange_"+newCounter+"/@FromSerialNo")%>/>		     
		</td>

		<td class="detaillabel" id='<%="serialrangetolabel"+currentSerialCounter%>' style='display:none'>
            <yfc:i18n>To_Serial_#</yfc:i18n>
		</td> 

		<td nowrap="true" id='<%="serialrangetotext"+currentSerialCounter%>' class="tablecolumn" style='display:none' >
		   <input type="text" class="unprotectedinput" OldValue=""  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/SerialRange_"+newCounter+"/@ToSerialNo")%>/>
		</td> 
			
		   <% for (int i=1; i < numSecondarySerials+1 ; i++){
			  String serLabel= "Secondary_Serial_" + i;
			%>
			<td nowrap="true" class="tablecolumn">
				<input type="text" class="unprotectedinput" OldValue="" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/SerialList/SerialDetail_" + newCounter  +"/@SecondarySerial"+i,"xml:/SerialDetail/@SecondarySerial"+i)%>/> 
			</td>
			<%}%>
		
		</tr>
<%	}	%>
	 </yfc:loopXML> 
    </tbody>
	<tfoot>
        <tr style='display:none' TemplateRow="true">
            <td class="checkboxcolumn">
            </td>
            <td nowrap="true" id='<%="singleserialtext"+""%>' class="tablecolumn" style='display:' colspan="4">
                <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/SerialList/SerialDetail_" + currentSerialCounter + "/@SerialNo","")%>/>
				  <input type="hidden" name="<%=buildBinding("xml:/AdjustLocationInventory/Source/Inventory/SerialList/SerialDetail_", currentSerialCounter,"/@Quantity")%>" value="1"/>
            </td>
			<td class="detaillabel" id='<%="serialrangefromlabel"+""%>' style='display:none'>
            <yfc:i18n>From_Serial_#</yfc:i18n>
			</td>

			<td nowrap="true" id='<%="serialrangefromtext"+""%>' class="tablecolumn" style='display:none' >
			<input type="text" class="unprotectedinput" OldValue=""  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/SerialRange_"+newCounter+"/@FromSerialNo")%>/>		     
			</td>

			<td class="detaillabel" id='<%="serialrangetolabel"+""%>' style='display:none'>
            <yfc:i18n>To_Serial_#</yfc:i18n>
			</td> 

			<td nowrap="true" id='<%="serialrangetotext"+""%>' class="tablecolumn" style='display:none' >
		   <input type="text" class="unprotectedinput" OldValue=""  	<%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/SerialRange_"+newCounter+"/@ToSerialNo")%>/>
			</td> 
	        <% for (int i=1; i < numSecondarySerials+1 ; i++){
			  String serLabel= "Secondary_Serial_" + i;
			%>
			<td class="tablecolumn" nowrap="true">
                <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/SerialList/SerialDetail_" + currentSerialCounter + "/@SecondarySerial"+i,"")%>/>
            </td>
			<%}%>
        </tr>
        <tr>
        	<td nowrap="true" colspan="5">
        		<jsp:include page="/common/editabletbl.jsp" flush="true">
					<jsp:param name="ReloadOnAddLine" value="N"/>
        		</jsp:include>
        	</td>
        </tr>
    </tfoot>
</table>
</div>
