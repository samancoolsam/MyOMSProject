<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<script language="javascript" src="<%=request.getContextPath()%>/css/scripts/editabletbl.js"></script>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<% String quantityname; 
   int realcounter = 0;
   double numSecondarySerials=getNumericValue("xml:ItemDetails:/Item/PrimaryInformation/@NumSecondarySerials");
  %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<div style="height:<%=request.getParameter("height")%>px;overflow:auto">
<table class="table" ID="RemoveSerialNo" cellspacing="0" width="100%" >
    <thead>
        <tr>
            <td class="checkboxheader" sortable="no">
                <input type="checkbox" value="checkbox" name="checkbox" onclick="doCheckAll(this);"/>
            </td>
            <td class="tablecolumnheader" nowrap="true" style="width:<%=getUITableSize("xml:/AdjustLocationInventory/Source/Inventory/SerialList/SerialDetail/@SerialNo")%>"><yfc:i18n>Serial_#</yfc:i18n></td>
         
		   <% for (int i=1; i < numSecondarySerials+1 ; i++){
			  String serLabel= "Secondary_Serial_" + i;
			%>
			<td class="tablecolumnheader" nowrap="true" style="width:<%=getUITableSize("xml:/AdjustLocationInventory/Source/Inventory/SerialList/SerialDetail/@SecondarySerial"+i)%>"><yfc:i18n><%=serLabel%></yfc:i18n></td>
			<%}%>
        </tr>
    </thead>
    <tbody>
        <yfc:loopXML name="NodeInventory" binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/@ItemInventoryDetail" id="ItemInventoryDetail">
		<yfc:loopXML name="ItemInventoryDetail" binding="xml:/ItemInventoryDetail/SerialList/@SerialDetail" id="SerialDetail">
			<tr>
				<td class="checkboxcolumn" >
					<% realcounter = SerialDetailCounter.intValue() + 5000;
					  String multiSelectValStr="";
					    multiSelectValStr+="yfcMultiSelectValue1='"+ getValue("SerialDetail", "xml:/SerialDetail/@SerialNo") + "'" ;
					   multiSelectValStr+=" yfcMultiSelectValue2"+"=-1";
				   %>
					<input type="checkbox" value='' name="chkEntityKey" yfcMultiSelectCounter='<%=SerialDetailCounter%>' <%=multiSelectValStr%> />
				</td>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/SerialDetail/@SerialNo" name="SerialDetail"/>
				</td>
				<% for (int i=1; i < numSecondarySerials+1 ; i++){
					 String serBinding="xml:/SerialDetail/@SecondarySerial"+i;
				%>
                <td class="tablecolumn">
					<yfc:getXMLValue binding="<%=serBinding%>" name="SerialDetail"/>
				</td>
				<%}%>

			</tr>
			<% request.setAttribute("SerialCounter", new Integer(SerialDetailCounter.intValue() + 1)); %>
        </yfc:loopXML>
        </yfc:loopXML>
    </tbody>
	<tfoot>
    </tfoot>
</table>
<% if (realcounter == 0) {
	request.setAttribute("SerialCounter", new Integer(1));
					}%>
</div>

