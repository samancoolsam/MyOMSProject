<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%
	String scacAndServiceKey=getValue("Container","xml:/Container/Shipment/@ScacAndServiceKey");
%>
<table width="100%" class="view">
<tr>
    <td class="detaillabel" >
        <yfc:i18n>Carrier_Service</yfc:i18n>
    </td>
    <td  nowrap="true" class="protectedtext">
		<% if(!isVoid(scacAndServiceKey)  ){ %>
        <yfc:getXMLValueI18NDB name="ScacAndServiceList" binding="xml:/ScacAndServiceList/ScacAndService/@ScacAndServiceDesc"/>
		<%}%>
    </td>
    <td class="detaillabel" >
        <yfc:i18n>Tracking_#</yfc:i18n> 
    </td>
	<td class="protectedtext">
		<yfc:getXMLValue name="Container" binding="xml:/Container/@TrackingNo"/>
	       <input type="hidden" <%=getTextOptions( "xml:/Container/@TrackingNo", "xml:/Container/@TrackingNo")%>/>
	</td>
	<td class="detaillabel" >
        <yfc:i18n>Gross_Weight</yfc:i18n>
    </td>
    <td class="protectedtext">
        <yfc:getXMLValue name="Container" binding="xml:/Container/@ContainerGrossWeight"/>
        &nbsp;
        <%=getComboText("xml:WeightUomList:/UomList/@Uom" ,"UomDescription" ,"Uom" ,"xml:/Container/@ContainerGrossWeightUOM",true)%>
    </td>
</tr>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>COD_Pay_Method</yfc:i18n>
    </td>
    <td class="protectedtext">
		  <yfc:getXMLValueI18NDB name="Container" binding="xml:/Container/Shipment/@CODPayMethod"/>
	</td>
    <td class="detaillabel" >
        <yfc:i18n>COD_Return_Tracking_#</yfc:i18n> 
    </td>
	<td class="protectedtext">
		<yfc:getXMLValue name="Container" binding="xml:/Container/@CODReturnTrackingNo"/>
	       <input type="hidden" <%=getTextOptions( "xml:/Container/@CODReturnTrackingNo", "xml:/Container/@CODReturnTrackingNo")%>/>
	</td>
	<td class="detaillabel" >
        <yfc:i18n>Net_Weight</yfc:i18n>
    </td>
    <td class="protectedtext">
        <yfc:getXMLValue name="Container" binding="xml:/Container/@ContainerNetWeight"/>
        &nbsp;
        <%=getComboText("xml:WeightUomList:/UomList/@Uom" ,"UomDescription" ,"Uom" ,"xml:/Container/@ContainerNetWeightUOM",true)%>
    </td>
  </tr>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>COD_Amount</yfc:i18n>
    </td>
    <td class="protectedtext">
		<yfc:getXMLValue name="CurrencyList" binding="xml:/CurrencyList/Currency/@PrefixSymbol"/> 
        &nbsp;
        <yfc:getXMLValue name="Container" binding="xml:/Container/@CODAmount"/>
        &nbsp;
        <yfc:getXMLValue name="CurrencyList" binding="xml:/CurrencyList/Currency/@PostfixSymbol"/>
	</td>
   <td class="detaillabel" >
        <yfc:i18n>Size</yfc:i18n>
    </td>
    <td class="protectedtext">
        <yfc:getXMLValue name="Container" binding="xml:/Container/Corrugation/PrimaryInformation/@ShortDescription"/>
    </td>
  	<td class="detaillabel" >
        <yfc:i18n>Actual_Weight</yfc:i18n>
    </td>
    <td class="protectedtext">
        <yfc:getXMLValue name="Container" binding="xml:/Container/@ActualWeight"/>
        &nbsp;
        <%=getComboText("xml:WeightUomList:/UomList/@Uom" ,"UomDescription" ,"Uom" ,"xml:/Container/@ActualWeightUOM",true)%>
    </td>
</tr>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>Actual_Freight_Charge</yfc:i18n>
    </td>
    <td class="protectedtext">
		<yfc:getXMLValue name="CurrencyList" binding="xml:/CurrencyList/Currency/@PrefixSymbol"/> 
        &nbsp;
        <yfc:getXMLValue name="Container" binding="xml:/Container/@ActualFreightCharge"/>
        &nbsp;
        <yfc:getXMLValue name="CurrencyList" binding="xml:/CurrencyList/Currency/@PostfixSymbol"/>
	</td>
   <td class="detaillabel" >
        <yfc:i18n>Length</yfc:i18n>
    </td>
    <td class="protectedtext">
        <yfc:getXMLValue name="Container" binding="xml:/Container/@ContainerLength"/>
        &nbsp;
        <%=getComboText("xml:DimensionUomList:/UomList/@Uom" ,"UomDescription" ,"Uom" ,"xml:/Container/@ContainerLengthUOM",true)%>
    </td>
  	<td class="detaillabel" >
        <yfc:i18n>Billed_Weight</yfc:i18n>
    </td>
    <td class="protectedtext">
        <yfc:getXMLValue name="Container" binding="xml:/Container/@AppliedWeight"/>
        &nbsp;
        <%=getComboText("xml:WeightUomList:/UomList/@Uom" ,"UomDescription" ,"Uom" ,"xml:/Container/@ContainerNetWeightUOM",true)%>
    </td>  	
</tr>
<tr>
	<td  nowrap="true" class="detaillabel" >
        <yfc:i18n>Special_Services_Surcharge</yfc:i18n>
    </td>
    <td class="protectedtext">
		<yfc:getXMLValue name="CurrencyList" binding="xml:/CurrencyList/Currency/@PrefixSymbol"/> 
        &nbsp;
        <yfc:getXMLValue name="Container" binding="xml:/Container/@SpecialServicesSurcharge"/>
        &nbsp;
        <yfc:getXMLValue name="CurrencyList" binding="xml:/CurrencyList/Currency/@PostfixSymbol"/>
	</td>
   <td class="detaillabel" >
        <yfc:i18n>Width</yfc:i18n>
    </td>
    <td class="protectedtext">
        <yfc:getXMLValue name="Container" binding="xml:/Container/@ContainerWidth"/>
        &nbsp;
        <%=getComboText("xml:DimensionUomList:/UomList/@Uom" ,"UomDescription" ,"Uom" ,"xml:/Container/@ContainerWidthUOM",true)%>
    </td>
	<td class="detaillabel" >
        <yfc:i18n>Has_Hazardous_Item(s)</yfc:i18n>
    </td>
    <td class="protectedtext">
        <yfc:getXMLValue name="Container" binding="xml:/Container/@IsHazmat"/>
    </td>
</tr>
<tr>
	<td  nowrap="true" class="detaillabel" >
        <yfc:i18n>Declared_Insurance_Value</yfc:i18n> 
    </td>
    <td class="protectedtext">
        <yfc:getXMLValue name="CurrencyList" binding="xml:/CurrencyList/Currency/@PrefixSymbol"/> 
        &nbsp;
        <yfc:getXMLValue name="Container" binding="xml:/Container/@DeclaredValue"/>
        &nbsp;
        <yfc:getXMLValue name="CurrencyList" binding="xml:/CurrencyList/Currency/@PostfixSymbol"/>
    </td>
   <td class="detaillabel" >
        <yfc:i18n>Height</yfc:i18n>
    </td>
    <td class="protectedtext">
        <yfc:getXMLValue name="Container" binding="xml:/Container/@ContainerHeight"/>
        &nbsp;
        <%=getComboText("xml:DimensionUomList:/UomList/@Uom" ,"UomDescription" ,"Uom" ,"xml:/Container/@ContainerHeightUOM",true)%>
    </td>
</tr>
</table>