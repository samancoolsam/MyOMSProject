<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<table width="100%" class="table" editable="false">
<thead>
	<tr> 
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Demands/Demand/DemandDetails/@OrderNo")%>">
            <yfc:i18n>Order_#</yfc:i18n>
        </td> 
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Demands/Demand/DemandDetails/@OrderName")%>">
            <yfc:i18n>Order_Name</yfc:i18n>
        </td> 
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Demands/Demand/DemandDetails/@DocumentType")%>">
            <yfc:i18n>Document_Type</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Demands/Demand/DemandDetails/@LineNo")%>">
            <yfc:i18n>Line_#</yfc:i18n>
        </td> 
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Demands/Demand/DemandDetails/@EnterpriseCode")%>">
            <yfc:i18n>Enterprise</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Demands/Demand/DemandDetails/@BuyerOrganiationCode")%>">
            <yfc:i18n>Buyer</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Demands/Demand/DemandDetails/@SellerOrganizationCode")%>">
            <yfc:i18n>Seller</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Demands/Demand/@LotNumber")%>">
            <yfc:i18n>Lot_#</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Item/Demands/Demand/DemandDetails/@Quantity")%>">
            <yfc:i18n>Quantity</yfc:i18n>
        </td>
    </tr>
</thead>	
<tbody>
    <yfc:loopXML name="Item" binding="xml:/Item/Demands/@Demand" id="demand"> 
		<yfc:loopXML name="demand" binding="xml:demand:/Demand/@DemandDetails" id="demanddetails"> 
			<tr>
			    <yfc:makeXMLInput name="orderKey">
					<yfc:makeXMLKey binding="xml:/Order/@OrderHeaderKey" value="xml:demanddetails:/DemandDetails/@OrderHeaderKey" />
			    </yfc:makeXMLInput>
			    <yfc:makeXMLInput name="orderLineKey">
					<yfc:makeXMLKey binding="xml:/OrderLine/@OrderHeaderKey" value="xml:demanddetails:/DemandDetails/@OrderHeaderKey" />
					<yfc:makeXMLKey binding="xml:/OrderLine/@OrderLineKey" value="xml:demanddetails:/DemandDetails/@OrderLineKey" />
			    </yfc:makeXMLInput>
				<td class="tablecolumn">
					<% if(showOrderNo("demanddetails","DemandDetails")) {%>
				        <a <%=getDetailHrefOptions("L01",getParameter("orderKey"),"")%>>
							<yfc:getXMLValue name="demanddetails" binding="xml:/DemandDetails/@OrderNo" />
						</a>
					<%} else {%>
						<yfc:getXMLValue name="demanddetails" binding="xml:/DemandDetails/@OrderNo" />
					<%}%>
				</td>
				<td class="tablecolumn">
					<yfc:getXMLValue name="demanddetails" binding="xml:/DemandDetails/@OrderName" />
				</td>
				<td class="tablecolumn"><yfc:getXMLValue name="demanddetails" binding="xml:/DemandDetails/@DocumentType" /></td>
				<td class="tablecolumn" sortValue="<%=getNumericValue("xml:demanddetails:/DemandDetails/@LineNo")%>">
			        <a <%=getDetailHrefOptions("L02",getParameter("orderLineKey"),"")%>>
						<yfc:getXMLValue name="demanddetails" binding="xml:/DemandDetails/@LineNo" />
					</a>
				</td>
				<td class="tablecolumn"><yfc:getXMLValue name="demanddetails" binding="xml:/DemandDetails/@EnterpriseCode" /></td>
				<td class="tablecolumn"><yfc:getXMLValue name="demanddetails" binding="xml:/DemandDetails/@BuyerOrganizationCode" /></td>
				<td class="tablecolumn"><yfc:getXMLValue name="demanddetails" binding="xml:/DemandDetails/@SellerOrganizationCode" /></td>
				<td class="tablecolumn"><yfc:getXMLValue name="demand" binding="xml:/Demand/@LotNo" /></td>
				<td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:demanddetails:/DemandDetails/@Quantity")%>">
					<yfc:getXMLValue name="demanddetails" binding="xml:/DemandDetails/@Quantity" />
				</td>
			</tr>
		</yfc:loopXML> 
    </yfc:loopXML> 
</tbody>	
</table>

