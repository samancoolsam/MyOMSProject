<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/currencyutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/modificationreason.js"></script> 
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/om.js"></script>

<table class="table" editable="false" width="100%" cellspacing="0">
    <thead> 
        <tr>
            <td class="tablecolumnheader"><yfc:i18n>GiftCardNO#</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Quantity</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>ContainerDetailKey</yfc:i18n></td>
			<td class="tablecolumnheader"><yfc:i18n>UnitCost</yfc:i18n></td>
        </tr>
    </thead>
    <tbody>
		<yfc:loopXML name="ShipmentTagSerialList" binding="xml:ShipmentTagSerialList/@ShipmentTagSerial" id="ShipmentTagSerial">
            <tr>
                <td class="tablecolumn"><yfc:getXMLValue binding="xml:/ShipmentTagSerial/@SerialNo"/></td>
                <td class="tablecolumn"><yfc:getXMLValue binding="xml:/ShipmentTagSerial/@Quantity"/></td>
				<td class="tablecolumn"><yfc:getXMLValue binding="xml:/ShipmentTagSerial/@ContainerDetailKey"/></td> 
				<td class="tablecolumn"><yfc:getXMLValue binding="xml:/ShipmentTagSerial/@UnitCost"/></td>
            </tr>
        </yfc:loopXML>
   </tbody>
</table>