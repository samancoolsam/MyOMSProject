<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/im/inventory/detail/inventory_detail_shipnodelist_include.jspf" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/im.js"></script>
<script language="javascript">

	window.attachEvent("onload",getShipNodeValue);	
		function getShipNodeValue()
		{
			var response= document.all(
					"xml:/ShipNodeInventory/Item/@Response").value;
			if(response=="204")
			{
				alert("No inventory available at any shipnodes");		
			}else if(response=="Error"){
				alert("Inventory Information Not available");				
			}							
		}
	
</script> 
<table width="100%" class="table" editable="false">
<thead>
    <tr> 
        <td class="tablecolumnheader" sortable="yes" style="width:50%<%= getUITableSize("xml:/ShipNodeInventory/Item/ShipNodes/ShipNode/@ShipNode")%>">
            <yfc:i18n>Ship_Node</yfc:i18n>
        </td>
        <td class="tablecolumnheader"  sortable="yes" style="width:50%<%= getUITableSize("xml:/ShipNodeInventory/Item/ShipNodes/ShipNode/@TotalSupply")%>">
            <yfc:i18n>Supply</yfc:i18n>
        </td>
    </tr>
</thead>	
<tbody>
    <yfc:loopXML name="ShipNodeInventory" binding="xml:/ShipNodeInventory/Item/ShipNodes/@ShipNode" id="shipnode"> 
    <tr>
        <td class="tablecolumn" width="50%" style="text-align:left">
                <yfc:getXMLValue name="shipnode" binding="xml:/ShipNode/@ShipNode"/>
        </td>
        <td class="numerictablecolumn" width="50%" style="text-align:left" sortValue="<%=getNumericValue("xml:shipnode:/ShipNode/@TotalSupply")%>">
            <yfc:getXMLValue name="shipnode" binding="xml:/ShipNode/@TotalSupply" />
        </td>
    </tr>
    </yfc:loopXML>
	<input type="hidden" <%=getTextOptions("xml:/ShipNodeInventory/Item/@Response")%>/>
</tbody>	
</table>
