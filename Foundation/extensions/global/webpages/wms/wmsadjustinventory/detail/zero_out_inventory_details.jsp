<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>

<table class="view" width="100%">


		
		<% String noOfUnits = getValue("LocationInventorySummary","xml:/LocationInventorySummary/@TotalNoOfUnits");
		
		String noOfLooseItems = getValue("LocationInventorySummary","xml:/LocationInventorySummary/@TotalNoOfLooseItems");
		String noOfItemsInLPNs = getValue("LocationInventorySummary","xml:/LocationInventorySummary/@TotalNoOfItemsLyingInsideLicensePlates");


		String noOfItems = getValue("LocationInventorySummary","xml:/LocationInventorySummary/@TotalNoOfItems");		
		String totalInvValue = getValue("LocationInventorySummary","xml:/LocationInventorySummary/@TotalValueofInventory");
		String currency = getValue("LocationInventorySummary","xml:/LocationInventorySummary/@Currency");
		String sEntCode = getValue("LocationInventorySummary","xml:/LocationInventorySummary/@EnterpriseCode");
			

		double dtotalInvValue = Double.parseDouble(totalInvValue);
		String localizedVal = getLocalizedStringFromDouble(getLocale(), dtotalInvValue);
		String value = localizedVal + " " +currency;
		%>
		
		
		<tr>
		<td class="detaillabel" >
			<yfc:i18n>No_of_Loose_Items</yfc:i18n>
		</td> 
		<td class="protectedtext" >
		<%=  noOfLooseItems %>
		</td>
		<td/><td/>
		<td/><td/><td/>
		</tr>

		<tr>
		<td class="detaillabel" nowrap="true">
			<yfc:i18n>No_of_Items_Inside_License_Plates</yfc:i18n>
		</td>
		<td class="protectedtext" >
		<%=  noOfItemsInLPNs %>
		</td>
		<td class="detaillabel" >
			<yfc:i18n>Total_dollar_value</yfc:i18n>
		</td>
		<td class="protectedtext" >
			<%=  value %>
		</td>
		<td/><td/><td/>
		</tr>
	


	

</table>


