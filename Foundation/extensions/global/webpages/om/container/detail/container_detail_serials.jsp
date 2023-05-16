<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<table width="100%" class="view">
<table width="100%" class="view">
<tr>
	<td class="detaillabel" >
		<yfc:i18n>Node</yfc:i18n>
	</td>
	<td  class="protectedtext">
		<yfc:getXMLValue binding="xml:Serial:/SerialList/Serial/@ShipNode"/>
	</td>
		<td class="detaillabel" >
		<yfc:i18n>Location</yfc:i18n>
		</td>
        <td class="protectedtext">
		<yfc:getXMLValue binding="xml:/Container/@LocationId"/>
		</td>
	<td class="detaillabel" >
		<yfc:i18n>Case_ID</yfc:i18n>
	</td>
	<td  class="protectedtext">
		<yfc:getXMLValue binding="xml:Serial:/SerialList/Serial/@CaseId"/>
	</td>

</tr>
<tr>
	<td class="detaillabel" >
		<yfc:i18n>Pallet_ID</yfc:i18n>
	</td>
	<td  class="protectedtext">
		<yfc:getXMLValue binding="xml:Serial:/SerialList/Serial/@PalletId"/>
	</td>
	<td class="detaillabel" nowrap="true">
        <yfc:i18n>Serial_#</yfc:i18n>
    </td>
		<td  class="protectedtext">
			<%if(!isVoid(resolveValue("xml:Serial:/SerialList/Serial/@SerialNo"))){%>
			<yfc:getXMLValue binding="xml:Serial:/SerialList/Serial/@SerialNo"/>
			<%}else{%>
			<yfc:getXMLValue binding="xml:/Container/@SerialNo"/>
			<%}%>
	</td>
	<td/>
	<td/>
</tr>
</table>
 <fieldset width="3">
 <legend><yfc:i18n>Child_Serials</yfc:i18n></legend> 
<table class="view" width="100%">
<tr>
	<td class="detaillabel" >
		<yfc:i18n>Secondary_Serial_1</yfc:i18n>
	</td>
	<td  class="protectedtext">
		<yfc:getXMLValue binding="xml:Serial:/SerialList/Serial/@SecondarySerial1"/>
	</td>
	<td class="detaillabel" >
		<yfc:i18n>Secondary_Serial_2</yfc:i18n>
	</td>
	<td  class="protectedtext">
		<yfc:getXMLValue binding="xml:Serial:/SerialList/Serial/@SecondarySerial2"/>
	</td>
	<td class="detaillabel" >
		<yfc:i18n>Secondary_Serial_3</yfc:i18n>
	</td>
	<td  class="protectedtext">
		<yfc:getXMLValue binding="xml:Serial:/SerialList/Serial/@SecondarySerial3"/>
	</td>
</tr>
<tr>
	<td class="detaillabel" >
		<yfc:i18n>Secondary_Serial_4</yfc:i18n>
	</td>
	<td  class="protectedtext">
		<yfc:getXMLValue binding="xml:Serial:/SerialList/Serial/@SecondarySerial4"/>
	</td>
	<td class="detaillabel" >
		<yfc:i18n>Secondary_Serial_5</yfc:i18n>
	</td>
	<td  class="protectedtext">
		<yfc:getXMLValue binding="xml:Serial:/SerialList/Serial/@SecondarySerial5"/>
	</td>
	<td class="detaillabel" >
		<yfc:i18n>Secondary_Serial_6</yfc:i18n>
	</td>
	<td  class="protectedtext">
		<yfc:getXMLValue binding="xml:Serial:/SerialList/Serial/@SecondarySerial6"/>
	</td>
</tr>
<tr>
	<td class="detaillabel" >
		<yfc:i18n>Secondary_Serial_7</yfc:i18n>
	</td>
	<td  class="protectedtext">
		<yfc:getXMLValue binding="xml:Serial:/SerialList/Serial/@SecondarySerial7"/>
	</td>
	<td class="detaillabel" >
		<yfc:i18n>Secondary_Serial_8</yfc:i18n>
	</td>
	<td  class="protectedtext">
		<yfc:getXMLValue binding="xml:Serial:/SerialList/Serial/@SecondarySerial8"/>
	</td>
	<td class="detaillabel" >
		<yfc:i18n>Secondary_Serial_9</yfc:i18n>
	</td>
	<td  class="protectedtext">
		<yfc:getXMLValue binding="xml:Serial:/SerialList/Serial/@SecondarySerial9"/>
	</td>
</tr>
</table>
</fieldset>
</table>