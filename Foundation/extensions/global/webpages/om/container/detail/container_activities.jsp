<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<table class="table" width="100%">
<thead>
    <tr> 
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerActivities/ContainerActivity/@ActivityTimeStamp")%>">
            <yfc:i18n>Date</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerActivities/ContainerActivity/@ActivityUserId")%>">
            <yfc:i18n>Recorded_By</yfc:i18n>
        </td>
		<td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerActivities/ContainerActivity/@ActivityCode")%>">
            <yfc:i18n>Activity_Code</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= 
		getUITableSize("xml:/Container/ContainerActivities/ContainerActivity/ActivityLocation/@ActivityLocationId")%>">
            <yfc:i18n>Location_ID</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerActivities/ContainerActivity/ActivityLocation/@ActivityNodeKey")%>">
            <yfc:i18n>Node</yfc:i18n>
        </td>
		<td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Shipment/ShipmentStatusAudits/ShipmentStatusAudit/@NewStatusDate")%>">
            <yfc:i18n>Address</yfc:i18n>
        </td>
        <td nowrap="true" class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerActivities/ContainerActivity/@IsException")%>">
            <yfc:i18n>Is_An_Exception</yfc:i18n>
        </td>
		<td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerActivities/ContainerActivity/@Notes")%>">
            <yfc:i18n>Notes</yfc:i18n>
        </td>
    </tr>
</thead>
<tbody>
<%
  
  YFCElement containerElem = (YFCElement) request.getAttribute("Container");
  YFCElement containerActivitiesElem =  containerElem.getChildElement("ContainerActivities");
  String[] sortingAttrs = {"Modifyts"};
		containerActivitiesElem.sortChildren(sortingAttrs, false, false);
%>

     <yfc:loopXML binding="xml:/Container/ContainerActivities/@ContainerActivity" id="ContainerActivity"> 
  
  <%
        String sName = resolveValue("xml:/ContainerActivity/ActivityLocation/@FirstName")+ " " + resolveValue("xml:/ContainerActivity/ActivityLocation/@MiddleName")+ " " + resolveValue("xml:/ContainerActivity/ActivityLocation/@LastName");
		String sCompany = resolveValue("xml:/ContainerActivity/ActivityLocation/@Company");
		String sAddressLine1 = resolveValue("xml:/ContainerActivity/ActivityLocation/@AddressLine1");
		String sAddressLine2 = resolveValue("xml:/ContainerActivity/ActivityLocation/@AddressLine2");
		String sAddressLine3 = resolveValue("xml:/ContainerActivity/ActivityLocation/@AddressLine3");
		String sAddressLine4 = resolveValue("xml:/ContainerActivity/ActivityLocation/@AddressLine4");
		String sAddressLine5 = resolveValue("xml:/ContainerActivity/ActivityLocation/@AddressLine5");
		String sAddressLine6 = resolveValue("xml:/ContainerActivity/ActivityLocation/@AddressLine6");
		String sCity = resolveValue("xml:/ContainerActivity/ActivityLocation/@City");
		String sState = resolveValue("xml:/ContainerActivity/ActivityLocation/@State");
		String sZipCode = resolveValue("xml:/ContainerActivity/ActivityLocation/@ZipCode");
		String sCountry = resolveValue("xml:/ContainerActivity/ActivityLocation/@Country");
  
  %>

    <tr>
			<td class="tablecolumn" >
			 <yfc:getXMLValueI18NDB name="ContainerActivity" 
			binding="xml:/ContainerActivity/@ActivityTimeStamp"/> 
            </td>       
			<td class="tablecolumn" >
			<yfc:getXMLValueI18NDB name="ContainerActivity" 
			binding="xml:/ContainerActivity/@ActivityUserId"/>
            </td>       
			<td class="tablecolumn" >
			<yfc:getXMLValueI18NDB name="ContainerActivity" 
			binding="xml:/ContainerActivity/@ActivityCode"/>
            </td>
			<td class="tablecolumn" >
			<yfc:getXMLValueI18NDB name="ContainerActivity" 
			binding="xml:/ContainerActivity/ActivityLocation/@ActivityLocationId"/>
            </td>       
			<td class="tablecolumn" >
			<yfc:getXMLValueI18NDB name="ContainerActivity" 
			binding="xml:/ContainerActivity/ActivityLocation/@ActivityNodeKey"/>
            </td>       
			<td class="tablecolumn">
			
			<%
				if(!isVoid(sName)){
			%>
				<%=sName%><BR>
			<%
			    }
			    if(!isVoid(sCompany)){
			%>
				<%=sCompany%><BR>
			<%
			    }
			    if(!isVoid(sAddressLine1)){
			%>
				<%=sAddressLine1%><BR>
			<%
			    }
			    if(!isVoid(sAddressLine2)){
			%>
				<%=sAddressLine2%><BR>
			<%
			    }
			    if(!isVoid(sAddressLine3)){
			%>
				<%=sAddressLine3%><BR>
			<%
			    }
			    if(!isVoid(sAddressLine4)){
			%>
				<%=sAddressLine4%><BR>
			<%
			    }
			    if(!isVoid(sAddressLine5)){
			%>
				<%=sAddressLine5%><BR>
			<%
			    }
			    if(!isVoid(sAddressLine6)){
			%>
				<%=sAddressLine6%><BR>
			<%
			    }
			    if(!isVoid(sCity)){
			%>
				<%=sCity%><BR>
			<%
			    }
			    if(!isVoid(sState)){
			%>
				<%=sState%><BR>
			<%
			    }
			    if(!isVoid(sZipCode)){
			%>
				<%=sZipCode%><BR>
			<%
			    }
			    if(!isVoid(sCountry)){
			%>
				<%=sCountry%>
            <%
				}	
			%> 

			

            </td>
			<td class="tablecolumn" >
			<yfc:getXMLValueI18NDB name="ContainerActivity" 
			binding="xml:/ContainerActivity/@IsException"/>
            </td>       
			<td class="tablecolumn" >
			<yfc:getXMLValueI18NDB name="ContainerActivity" 
			binding="xml:/ContainerActivity/@Notes"/>
            </td>       
			
		 </tr>
		</yfc:loopXML> 
</tbody>
</table>
