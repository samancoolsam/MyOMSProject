<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>

<table class="view">
 <tr>
        <td>
            <input type="hidden" name="xml:/Shipment/@StatusQryType" value="BETWEEN"/>
          <!--   <input type="hidden" name="xml:/Shipment/@DraftOrderFlag" value="N"/> -->

					<input type="hidden" name="xml:/Shipment/ShipmentHoldTypes/ShipmentHoldType/@Status" value=""/>
			<input type="hidden" name="xml:/Shipment/ShipmentHoldTypes/ShipmentHoldType/@StatusQryType" value="" />
        </td>
    </tr>
<jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
  <jsp:param name="ShowDocumentType" value="false"/>
  <jsp:param name="RefreshOnDocumentType" value="true"/>
  <jsp:param name="DocumentTypeBinding" value="xml:/Shipment/@DocumentType"/>
  <jsp:param name="EnterpriseCodeBinding" value="xml:/Shipment/@EnterpriseCode"/> 
  
  <jsp:param name="ShowEnterpriseCode" value="false"/>
  <jsp:param name="RefreshOnEnterpriseCode" value="true"/>
  <jsp:param name="ScreenType" value="search"/>
 </jsp:include>

<% // Now call the APIs that are dependent on the common fields (Doc Type & Enterprise Code) %>
    <yfc:callAPI apiID="AP2"/>
    <yfc:callAPI apiID="AP3"/>
   
    <%
		String docType = resolveValue("xml:CommonFields:/CommonFields/@DocumentType");
		
		if(!isTrue("xml:/Rules/@RuleSetValue") )	{
    %>
			<yfc:callAPI apiID="AP4"/>
    
	<%
			YFCElement listElement = (YFCElement)request.getAttribute("HoldTypeList");

			YFCDocument document = listElement.getOwnerDocument();
			YFCElement newElement = document.createElement("HoldType");

			newElement.setAttribute("HoldType", " ");
			newElement.setAttribute("HoldTypeDescription", getI18N("All_Held_Shipments"));

			YFCElement eFirst = listElement.getFirstChildElement();
			if(eFirst != null)	{
				listElement.insertBefore(newElement, eFirst);
			}	else	{
				listElement.appendChild(newElement);
			}
			request.setAttribute("defaultHoldType", newElement);
		}

        //Remove Statuses 'Draft Order Created' and 'Held' from the Status Search Combobox.
		//prepareOrderSearchByStatusElement((YFCElement) request.getAttribute("StatusList"));
    %>


  <yfc:callAPI apiID="AP2"/>
  <tr> 
        <td>
            <input type="hidden" name="xml:/Shipment/@StatusQryType" value="BETWEEN"/>
        </td>
    </tr>    
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Shipment_#</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
            <select name="xml:/Shipment/@ShipmentNoQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                value="QueryType" selected="xml:/Shipment/@ShipmentNoQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Shipment/@ShipmentNo")%>/>
        </td>
    </tr>
   
</table>
<input type="hidden" class="unprotectedinput" <%=getTextOptions("xml:/Shipment/ShipmentLines/ShipmentLine/@WaveNo")%>/>
<input type="hidden" name="xml:/Shipment/@OrderAvailableOnSystem" value=" "/>
