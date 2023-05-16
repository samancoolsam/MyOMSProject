<%@ page import="com.yantra.wms.util.WMSNumberFormat" %>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/im.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/countrequest.js"></script>

<div style="width:858px;overflow:auto">
<table class="table">
  <tbody>
    <tr>
      <td width="100%" style="border:1px solid black">
        <table  editable="false" class="table" >
        <thead>
          <tr>
            <td class="checkboxheader" sortable="no">
                    <input type="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
            </td>
            <td class="tablecolumnheader"><yfc:i18n>Inventory_Status</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Tag_#</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Segment_Type</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Segment</yfc:i18n></td>
			<td class="tablecolumnheader"><yfc:i18n>Ship_By_Date</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Original_Location</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Serial_#</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>System_Quantity</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Count_Quantity</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Variance_Quantity</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Variance_Value</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Currency</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Last_Variance_Quantity</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Last_But_One_Variance_Quantity</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Variance_Type</yfc:i18n></td>
			<td class="tablecolumnheader"><yfc:i18n>Variance_Accepted</yfc:i18n></td>
			<td class="tablecolumnheader"><yfc:i18n>Count_Entered_By</yfc:i18n></td>
			<td class="tablecolumnheader"><yfc:i18n>Count_Entered_Date</yfc:i18n></td>
          </tr>
        </thead>
        <tbody>
        <yfc:loopXML binding="xml:/CountResultList/@SummaryResultList" id="SummaryResultList">
        <%
        String sLineNo=getParameter("optionSetBelongingToLine");
        Integer myInteger=new Integer(Integer.parseInt(sLineNo));
        if (equals(myInteger,SummaryResultListCounter)){%>
          <yfc:loopXML binding="xml:/SummaryResultList/@CountResult" id="CountResult">
          <tr>
            <yfc:makeXMLInput name="CountResultKey">
                <yfc:makeXMLKey binding="xml:/CountRequest/CountResultList/CountResult/@CountResultKey"
                    value="xml:/CountResult/@CountResultKey" />
                <yfc:makeXMLKey binding="xml:/CountRequest/@SummaryTaskId"
                    value="xml:/CountResult/@SummaryTaskId" />
                <yfc:makeXMLKey binding="xml:/CountRequest/@CountRequestKey"
                    value="xml:/CountResult/@CountRequestKey" />
                <yfc:makeXMLKey binding="xml:/CountRequest/@Node"
                    value="xml:/CountResult/@Node" />
            </yfc:makeXMLInput>

            <%if(!isVoid(resolveValue("xml:/CountResult/@ItemID"))){%>
                <yfc:makeXMLInput name="tagKey">
                <yfc:makeXMLKey binding="xml:/CountResult/@Node" value="xml:/CountResult/@Node"/>
                <yfc:makeXMLKey binding="xml:/CountResult/@LocationId" value="xml:/CountResult/@LocationId"/>
                <yfc:makeXMLKey binding="xml:/CountResult/@CaseId" value="xml:/CountResult/@CaseId"/>
                <yfc:makeXMLKey binding="xml:/CountResult/@PalletId" value="xml:/CountResult/@PalletId"/>
                <yfc:makeXMLKey binding="xml:/CountResult/@ItemID" value="xml:/CountResult/@ItemID"/>
                <yfc:makeXMLKey binding="xml:/CountResult/@Description" value="xml:/CountResult/Item/@Description"/>
                <yfc:makeXMLKey binding="xml:/CountResult/@ProductClass" value="xml:/CountResult/@ProductClass"/>
                <yfc:makeXMLKey binding="xml:/CountResult/@UnitOfMeasure" value="xml:/CountResult/@UnitOfMeasure"/>
                <yfc:makeXMLKey binding="xml:/CountResult/@OrganizationCode" value="xml:/CountResult/@OrganizationCode"/>
                <yfc:makeXMLKey binding="xml:/CountResult/Tag/@LotAttribute1" value="xml:/CountResult/CountResultTag/@LotAttribute1"/>
                <yfc:makeXMLKey binding="xml:/CountResult/Tag/@LotAttribute2" value="xml:/CountResult/CountResultTag/@LotAttribute2"/>
                <yfc:makeXMLKey binding="xml:/CountResult/Tag/@LotAttribute3" value="xml:/CountResult/CountResultTag/@LotAttribute3"/>
                <yfc:makeXMLKey binding="xml:/CountResult/Tag/@LotExpirationDate" value="xml:/CountResult/CountResultTag/@LotExpirationDate"/>
                <yfc:makeXMLKey binding="xml:/CountResult/Tag/@LotKeyReference" value="xml:/CountResult/CountResultTag/@LotKeyReference"/>
                <yfc:makeXMLKey binding="xml:/CountResult/Tag/@LotNumber" value="xml:/CountResult/CountResultTag/@LotNumber"/>
                <yfc:makeXMLKey binding="xml:/CountResult/Tag/@ManufacturingDate" value="xml:/CountResult/CountResultTag/@ManufacturingDate"/>
                <yfc:makeXMLKey binding="xml:/CountResult/Tag/@RevisionNo" value="xml:/CountResult/CountResultTag/@RevisionNo"/>
                <yfc:makeXMLKey binding="xml:/CountResult/Tag/@BatchNo" value="xml:/CountResult/CountResultTag/@BatchNo"/>
             </yfc:makeXMLInput>

            <td class="checkboxcolumn">
                <input type="checkbox" name="VarianceKey" yfcMultiSelectCounter='<%=SummaryResultListCounter.toString()+CountResultCounter.toString()%>' yfcMultiSelectValue1='<%=getValue("CountResult", "xml:/CountResult/@CountResultKey")%>'
                yHiddenInputName1="VarianceKey_<%=SummaryResultListCounter.toString()+CountResultCounter.toString()%>"/>

                <input type="hidden" value='<%=getParameter("CountResultKey")%>' name="VarianceKey_<%=SummaryResultListCounter.toString()+CountResultCounter.toString()%>" />

            </td>
            <td class="tablecolumn" nowrap="true"><yfc:getXMLValue binding="xml:/CountResult/@InventoryStatus"/></td>
            <td class="tablecolumn" nowrap="true">
                <a <%=getDetailHrefOptions("L01", getParameter("tagKey"),"")%> >
                    <yfc:getXMLValue name="CountResult" binding="xml:/CountResult/@TagNumber"/>
                </a>
            </td>
            <td class="tablecolumn" nowrap="true">
				<yfc:getXMLValue binding="xml:/CountResult/@SegmentType"/>
			</td>
            <td class="tablecolumn" nowrap="true">
				<yfc:getXMLValue binding="xml:/CountResult/@Segment"/>
			</td>
			<td class="tablecolumn" nowrap="true">
				<yfc:getXMLValue binding="xml:/CountResult/@ShipByDate"/>
			</td>
            <td class="tablecolumn" nowrap="true">
				<yfc:getXMLValue binding="xml:/CountResult/@OriginalLocation"/>
			</td>
            <td class="tablecolumn" nowrap="true">
				<yfc:getXMLValue binding="xml:/CountResult/@SerialNo"/>
			</td>
            <td class="numerictablecolumn" nowrap="true">
				<yfc:getXMLValue binding="xml:/CountResult/@SystemQuantity"/>
			</td>
            <td class="numerictablecolumn" nowrap="true">
				<yfc:getXMLValue binding="xml:/CountResult/@CountQuantity"/></td>
            <td class="numerictablecolumn" nowrap="true">
            <% String varqty = WMSNumberFormat.getUnsignedValue(resolveValue("xml:/CountResult/@VarianceQuantity")); 
			double dvarqty = Double.parseDouble(varqty);%>
			<%=getLocalizedStringFromDouble(getLocale(), dvarqty)%>
            <%=WMSNumberFormat.getSign(resolveValue("xml:/CountResult/@VarianceQuantity"))%>
            </td>
            <td class="numerictablecolumn" nowrap="true">
			<% String varval = WMSNumberFormat.getUnsignedValue(resolveValue("xml:/CountResult/@VarianceValue")); 
			double dvarval = Double.parseDouble(varval);%>
            <%=getLocalizedStringFromDouble(getLocale(), dvarval)%>
            <%=WMSNumberFormat.getSign(resolveValue("xml:/CountResult/@VarianceValue"))%>
            </td>
            <td class="tablecolumn" nowrap="true"><yfc:getXMLValue binding="xml:/CountResult/@Currency"/></td>
            <td class="numerictablecolumn" nowrap="true">
            <%=WMSNumberFormat.getUnsignedValue(resolveValue("xml:/CountResult/@LastVarianceQuantity"))%>
            <%=WMSNumberFormat.getSign(resolveValue("xml:/CountResult/@LastVarianceQuantity"))%>
            </td>
            <td class="numerictablecolumn" nowrap="true">
            <%=WMSNumberFormat.getUnsignedValue(resolveValue("xml:/CountResult/@LastButOneVarianceQuantity"))%>
            <%=WMSNumberFormat.getSign(resolveValue("xml:/CountResult/@LastButOneVarianceQuantity"))%>
            </td>
            <td class="tablecolumn" nowrap="true">
				<yfc:getXMLValue binding="xml:/CountResult/@VarianceType"/>
			</td>
			<td class="tablecolumn" nowrap="true">
				<yfc:getXMLValue binding="xml:/CountResult/@VarianceAccepted"/>
			</td>
			<td class="tablecolumn" nowrap="true">
				<yfc:getXMLValue binding="xml:/CountResult/@Createuserid"/>
			</td>
			<td class="tablecolumn" nowrap="true">
				<yfc:getXMLValue binding="xml:/CountResult/@Createts"/>
			</td>
          </tr>
                <%}%>
          </yfc:loopXML>
            <%}%>
        </yfc:loopXML>
        </tbody>
        </table>
      </td>
    </tr>
  </tbody>

</table>
</div>