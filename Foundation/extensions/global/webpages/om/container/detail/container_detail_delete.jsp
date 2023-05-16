<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%boolean isOrderAvailableOnSystem=false;%>
<script language="javascript">
    var myObject = new Object();
    myObject = dialogArguments;
    var parentWindow = myObject.currentWindow;
    var orderAvailableOnSystem=myObject.OrderAvailableOnSystem; 
	if(orderAvailableOnSystem=="Y"){<%
		isOrderAvailableOnSystem=true;
      %>
	} else {
		    <% isOrderAvailableOnSystem=false;%>
	}  
    function setClickedAttribute() {

        var retVal = new Object();

		if (document.all["xml:/Container/@BackOrderRemovedQuantity"].checked) {
					retVal["xml:/Container/@BackOrderRemovedQuantity"] = "Y";
		}
		else {
			retVal["xml:/Container/@BackOrderRemovedQuantity"] = "N";
		}

		if (document.all["xml:/Container/@RemoveFromShipmentLine"].checked) {
					retVal["xml:/Container/@RemoveFromShipmentLine"] = "Y";
		}
		else {
			retVal["xml:/Container/@RemoveFromShipmentLine"] = "N";
		}
        window.dialogArguments["OMReturnValue"] = retVal;
        window.dialogArguments["OKClicked"] = "YES";
        window.close();
    }

	function setRemoveQuantityFromShipmentLine(){
		 var removeQuantityFromShipmentLineObj= document.all["xml:/Container/@RemoveFromShipmentLine"] ;
		 var backorderRemovedQuantityObj= document.all["xml:/Container/@BackOrderRemovedQuantity"] ;
		 if ( backorderRemovedQuantityObj.checked) {
		  	 removeQuantityFromShipmentLineObj.checked= true;
		 } else{
			 removeQuantityFromShipmentLineObj.checked= false;
		 }
	}
</script>

<table width="100%" class="view">
<%if(isOrderAvailableOnSystem==true){%>
    <tr>
        <td class="detaillabel">
            <yfc:i18n>Backorder_removed_quantity</yfc:i18n> 
        </td>
        <td>
            <input class="checkbox" type="checkbox" <%=getCheckBoxOptions("xml:/Container/@BackOrderRemovedQuantity", "N", "Y")%> onclick="setRemoveQuantityFromShipmentLine();return true;"/>
        </td>
    </tr>
	<%}%>
	<tr>
        <td class="detaillabel">
            <yfc:i18n>Remove_quantity_from_shipment_line</yfc:i18n> 
        </td>
         <td>
		 <%if(isOrderAvailableOnSystem==false){%>
		    <input class="checkbox" type="hidden" <%=getCheckBoxOptions("xml:/Container/@BackOrderRemovedQuantity", "N", "Y")%> onclick="setRemoveQuantityFromShipmentLine();return true;"/>
			<%}%>
            <input class="checkbox" type="checkbox" <%=getCheckBoxOptions("xml:/Container/@RemoveFromShipmentLine", "N", "Y")%> />
        </td>
    </tr>
    <tr>
		<td/>
        <td align="right">
            <input type="button" class="button" value='<%=getI18N("__OK__")%>' onclick="setClickedAttribute();return true;"/>
            <input type="button" class="button" value='<%=getI18N("Cancel")%>' onclick="window.close();"/>
        <td>
    <tr>
</table>