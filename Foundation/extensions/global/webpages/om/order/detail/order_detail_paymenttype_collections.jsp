<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
 <!-- Added as Part of OMNI-65895 - Klarna VCN - Sterling to display "Klarna" memo (DOM) -->
<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ include file="/console/jsp/paymentutils.jspf" %>

<%@ page import="org.slf4j.*" %>
<%@ page import="com.yantra.yfc.log.*" %>

<%
	//YFCLogCategory catSecurity = YFCLogCategory.instance("com.stercomm.SecurityLogger");
	Logger catSecurity = LoggerFactory.getLogger("com.stercomm.SecurityLogger");
    String prePathId = getParameter("PrePathId");
    String showAuthorizationParams = getParameter("ShowAdditionalParams");

	String paymentType = resolveValue("xml:/" + prePathId + "/@PaymentType");
	String paymentTypeGroup = "";
	if(!isVoid(paymentType)){
		YFCElement paymentTypeListElem = (YFCElement)request.getAttribute("PaymentTypeList");
		for (Iterator i = paymentTypeListElem.getChildren(); i.hasNext();) {
			YFCElement childElem = (YFCElement) i.next();
			if(equals(paymentType, childElem.getAttribute("PaymentType"))){
				paymentTypeGroup = childElem.getAttribute("PaymentTypeGroup");
				break;
			}
		}
	}
	String creditCardEncrypted = isAttributeEncrypted((YFCElement) request.getAttribute("CreditCardRule"));
	String creditCardAddnlEncrypted = isAttributeEncrypted((YFCElement) request.getAttribute("CreditCardAddnlRule"));
	String storedValueCardEncrypted = isAttributeEncrypted((YFCElement) request.getAttribute("StoredValueCardRule"));
	String customerAccountEncrypted = isAttributeEncrypted((YFCElement) request.getAttribute("CustomerAccountRule"));
	String otherPaymentTypeEncrypted = isAttributeEncrypted((YFCElement) request.getAttribute("OtherPaymentTypeRule"));
%>
<table class="view">
<tr >
    <td class="detaillabel" ><yfc:i18n>Payment_Type</yfc:i18n></td>
    <!-- Start - OMNI-65895 - Klarna VCN - Sterling to display "Klarna" memo (DOM) -->
    <% logPaymentInfo(catSecurity,"PaymentReference5","PaymentReference5",resolveValue(buildBinding("xml:/",prePathId,"@PaymentReference5")),(YFCElement)request.getAttribute("Order")); %>
    <% if (equals(resolveValue(buildBinding("xml:/",prePathId,"/@PaymentReference5")),"Klarna")) {%>
    		<td class="protectedtext">	
    			<yfc:getXMLValue binding='<%=buildBinding("xml:/",prePathId,"/@PaymentReference5")%>'/>&nbsp;
    		</td>
    <%} else  { %>
    	<td class="protectedtext"> <%=getComboText("xml:/PaymentTypeList/@PaymentType","PaymentTypeDescription","PaymentType",buildBinding("xml:/",prePathId,"/@PaymentType"), true)%>
		&nbsp; </td> <% } %>    
	<!-- End - OMNI-65895 - Klarna VCN - Sterling to display "Klarna" memo (DOM) -->

<% if (equals("CREDIT_CARD", paymentTypeGroup)) {%>
    <td class="detaillabel" ><yfc:i18n>Credit_Card_#</yfc:i18n></td>
        <td class="protectedtext">
			<%=showEncryptedString(resolveValue(buildBinding("xml:/",prePathId,"/@DisplayCreditCardNo")))%>&nbsp;
		</td>

<!--    <% if (userHasDecryptedPaymentAttributesPermissions()) {%>
		<% if (equals(creditCardEncrypted,"Y")) {%>
			<%
				String encryptedString = resolveValue("xml:/"+prePathId+"/@CreditCardNo");
				YFCElement inputElem = YFCDocument.parse("<GetDecryptedString StringToDecrypt=\""+encryptedString+"\"/>").getDocumentElement();
				YFCElement templateElem = YFCDocument.parse("<GetDecryptedString DecryptedString=\"\"/>").getDocumentElement();
			%>
			<% logPaymentInfo(catSecurity,"CreditCardNo","DisplayCreditCardNo",resolveValue(buildBinding("xml:/",prePathId,"/@DisplayCreditCardNo")),(YFCElement)request.getAttribute("Order")); %>
			<yfc:callAPI apiName="getDecryptedString" inputElement="<%=inputElem%>" templateElement="<%=templateElem%>" outputNamespace="GetDecryptedString"/>
			<td>
				<input class="protectedinput" contenteditable="false" <%=getTextOptions( "xml:/GetDecryptedString/@DecryptedString", "xml:/GetDecryptedString/@DecryptedString")%>/>
			</td>
		<% } else { %>
			<td>
				<input class="protectedinput" contenteditable="false" <%=getTextOptions( buildBinding("xml:/",prePathId,"/@CreditCardNo"), buildBinding("xml:/",prePathId,"/@CreditCardNo"))%>/>
			</td>
	    <% } %>
    <% } else { %>
        <td class="protectedtext">
			<%=showEncryptedCreditCardNo(resolveValue(buildBinding("xml:/",prePathId,"/@DisplayCreditCardNo")))%>&nbsp;
		</td>
    <% } %>
-->
</tr>
<tr >
    <td class="detaillabel" ><yfc:i18n>Expiration_Date</yfc:i18n></td>
	<td class="protectedtext"><yfc:getXMLValue binding='<%=buildBinding("xml:/",prePathId,"/@CreditCardExpDate")%>'/>&nbsp;</td>    
<!--
    <% if (userHasDecryptedPaymentAttributesPermissions()) {%>
		<% if (equals(creditCardEncrypted,"Y") && equals(creditCardAddnlEncrypted,"Y")) {%>
			<%
				String encryptedString = resolveValue("xml:/"+prePathId+"/@CreditCardExpDate");
				YFCElement inputElem = YFCDocument.parse("<GetDecryptedString StringToDecrypt=\""+encryptedString+"\"/>").getDocumentElement();
				YFCElement templateElem = YFCDocument.parse("<GetDecryptedString DecryptedString=\"\"/>").getDocumentElement();
			%>
			<% logPaymentInfo(catSecurity,"CreditCardExpDate","DisplayCreditCardNo",resolveValue(buildBinding("xml:/",prePathId,"/@DisplayCreditCardNo")),(YFCElement)request.getAttribute("Order")); %>
			<yfc:callAPI apiName="getDecryptedString" inputElement="<%=inputElem%>" templateElement="<%=templateElem%>" outputNamespace="GetDecryptedString"/>
			<td>
				<input class="protectedinput" contenteditable="false" <%=getTextOptions( "xml:/GetDecryptedString/@DecryptedString", "xml:/GetDecryptedString/@DecryptedString")%>/>
			</td>
		<% } else { %>
			<td class="protectedtext"><yfc:getXMLValue binding='<%=buildBinding("xml:/",prePathId,"/@CreditCardExpDate")%>'/>&nbsp;</td>    
		<% } %>
    <% } else { %>
			<td class="protectedtext">
			<%=showEncryptedString("")%>
			&nbsp;
			</td>
    <% } %>
-->
    <td class="detaillabel" ><yfc:i18n>Credit_Card_Type</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding='<%=buildBinding("xml:/",prePathId,"/@CreditCardType")%>'/>&nbsp;</td>    
</tr>
<tr >
    <td class="detaillabel" ><yfc:i18n>Name_On_Card</yfc:i18n></td>
	<td class="protectedtext"><yfc:getXMLValue binding='<%=buildBinding("xml:/",prePathId,"/@CreditCardName")%>'/>&nbsp;</td>    
<!--
    <% if (userHasDecryptedPaymentAttributesPermissions()) {%>
		<% if (equals(creditCardEncrypted,"Y") && equals(creditCardAddnlEncrypted,"Y")) {%>
			<%
				String encryptedString = resolveValue("xml:/"+prePathId+"/@CreditCardName");
				YFCElement inputElem = YFCDocument.parse("<GetDecryptedString StringToDecrypt=\""+encryptedString+"\"/>").getDocumentElement();
				YFCElement templateElem = YFCDocument.parse("<GetDecryptedString DecryptedString=\"\"/>").getDocumentElement();
			%>
			<% logPaymentInfo(catSecurity,"CreditCardName","DisplayCreditCardNo",resolveValue(buildBinding("xml:/",prePathId,"/@DisplayCreditCardNo")),(YFCElement)request.getAttribute("Order")); %>
			<yfc:callAPI apiName="getDecryptedString" inputElement="<%=inputElem%>" templateElement="<%=templateElem%>" outputNamespace="GetDecryptedString"/>
			<td>
				<input class="protectedinput" contenteditable="false" <%=getTextOptions( "xml:/GetDecryptedString/@DecryptedString", "xml:/GetDecryptedString/@DecryptedString")%>/>
			</td>
		<% } else { %>
				<td class="protectedtext"><yfc:getXMLValue binding='<%=buildBinding("xml:/",prePathId,"/@CreditCardName")%>'/>&nbsp;</td>    
		<% } %>
    <% } else { %>
			<td class="protectedtext">
			<%=showEncryptedString("")%>
			&nbsp;
			</td>
    <% } %>
-->

<% }  
else if (equals("CUSTOMER_ACCOUNT", paymentTypeGroup)) {%> 

    <td class="detaillabel" ><yfc:i18n>Customer_Account_#</yfc:i18n></td>
	<% if (userHasDecryptedPaymentAttributesPermissions()) {%>
		<% if (equals(customerAccountEncrypted,"Y")) {%>
			<%
				String encryptedString = resolveValue("xml:/"+prePathId+"/@CustomerAccountNo");
				YFCElement inputElem = YFCDocument.parse("<GetDecryptedString StringToDecrypt=\""+encryptedString+"\"/>").getDocumentElement();
				YFCElement templateElem = YFCDocument.parse("<GetDecryptedString DecryptedString=\"\"/>").getDocumentElement();
			%>
			<% logPaymentInfo(catSecurity,"CustomerAccountNo","DisplayCustomerAccountNo",resolveValue(buildBinding("xml:/",prePathId,"/@DisplayCustomerAccountNo")),(YFCElement)request.getAttribute("Order")); %>
			<yfc:callAPI apiName="getDecryptedString" inputElement="<%=inputElem%>" templateElement="<%=templateElem%>" outputNamespace="GetDecryptedString"/>
			<td>
				<input class="protectedinput" contenteditable="false" <%=getTextOptions( "xml:/GetDecryptedString/@DecryptedString", "xml:/GetDecryptedString/@DecryptedString")%>/>
			</td>
		<% } else { %>
			<td>
				<input class="protectedinput" contenteditable="false" <%=getTextOptions( buildBinding("xml:/",prePathId,"/@CustomerAccountNo"), buildBinding("xml:/",prePathId,"/@CustomerAccountNo"))%>/>
			</td>
	    <% } %>
    <% } else { %>
        <td class="protectedtext">
			<%=showEncryptedString(resolveValue(buildBinding("xml:/",prePathId,"/@DisplayCustomerAccountNo")))%>&nbsp;
		</td>
    <% } %>
</tr>
<tr >
    <td class="detaillabel" ><yfc:i18n>Customer_PO_#</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding='<%=buildBinding("xml:/",prePathId,"/@CustomerPONo")%>'/>&nbsp;</td>
    
	<td class="detaillabel" ><yfc:i18n>Payment_Reference_#1</yfc:i18n></td>
    <% if (userHasDecryptedPaymentAttributesPermissions()) {%>
		<% if (equals(customerAccountEncrypted,"Y")) {%>
			<%
				String encryptedString = resolveValue("xml:/"+prePathId+"/@PaymentReference1");
				YFCElement inputElem = YFCDocument.parse("<GetDecryptedString StringToDecrypt=\""+encryptedString+"\"/>").getDocumentElement();
				YFCElement templateElem = YFCDocument.parse("<GetDecryptedString DecryptedString=\"\"/>").getDocumentElement();
			%>
			<% logPaymentInfo(catSecurity,"PaymentReference1","DisplayPaymentReference1",resolveValue(buildBinding("xml:/",prePathId,"@DisplayPaymentReference1")),(YFCElement)request.getAttribute("Order")); %>
			<yfc:callAPI apiName="getDecryptedString" inputElement="<%=inputElem%>" templateElement="<%=templateElem%>" outputNamespace="GetDecryptedString"/>
			<td>
				<input class="protectedinput" contenteditable="false" <%=getTextOptions( "xml:/GetDecryptedString/@DecryptedString", "xml:/GetDecryptedString/@DecryptedString")%>/>
			</td>
		<% } else { %>
			<td>
				<input class="protectedinput" contenteditable="false" <%=getTextOptions( buildBinding("xml:/",prePathId,"/@PaymentReference1"), buildBinding("xml:/",prePathId,"/@PaymentReference1"))%>/>
			</td>
	    <% } %>
    <% } else { %>
        <td class="protectedtext">
			<%=showEncryptedString(resolveValue(buildBinding("xml:/",prePathId,"/@DisplayPaymentReference1")))%>&nbsp;
		</td>
    <% } %>
</tr>
<tr >
    <td class="detaillabel" ><yfc:i18n>Payment_Reference_#2</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding='<%=buildBinding("xml:/",prePathId,"/@PaymentReference2")%>'/>&nbsp;</td>

<% }  
else if (equals("STORED_VALUE_CARD",paymentTypeGroup)) {%> 

    <td class="detaillabel" ><yfc:i18n>Stored_Value_Card_#</yfc:i18n></td>
	<td class="protectedtext">
		<%=showEncryptedString(resolveValue(buildBinding("xml:/",prePathId,"/@DisplaySvcNo")))%>&nbsp;
	</td>
<!--
    <% if (userHasDecryptedPaymentAttributesPermissions()) {%>
		<% if (equals(storedValueCardEncrypted,"Y")) {%>
			<%
				String encryptedString = resolveValue("xml:/"+prePathId+"/@SvcNo");
				YFCElement inputElem = YFCDocument.parse("<GetDecryptedString StringToDecrypt=\""+encryptedString+"\"/>").getDocumentElement();
				YFCElement templateElem = YFCDocument.parse("<GetDecryptedString DecryptedString=\"\"/>").getDocumentElement();
			%>
			<% logPaymentInfo(catSecurity,"SvcNo","DisplaySvcNo",resolveValue(buildBinding("xml:/",prePathId,"/@DisplaySvcNo")),(YFCElement)request.getAttribute("Order")); %>
			<yfc:callAPI apiName="getDecryptedString" inputElement="<%=inputElem%>" templateElement="<%=templateElem%>" outputNamespace="GetDecryptedString"/>
			<td>
				<input class="protectedinput" contenteditable="false" <%=getTextOptions( "xml:/GetDecryptedString/@DecryptedString", "xml:/GetDecryptedString/@DecryptedString")%>/>
			</td>
		<% } else { %>
			<td>
				<input class="protectedinput" contenteditable="false" <%=getTextOptions( buildBinding("xml:/",prePathId,"/@SvcNo"), buildBinding("xml:/",prePathId,"/@SvcNo"))%>/>
			</td>
	    <% } %>
    <% } else { %>
        <td class="protectedtext">
			<%=showEncryptedString(resolveValue(buildBinding("xml:/",prePathId,"/@DisplaySvcNo")))%>&nbsp;
		</td>
    <% } %>
-->
</tr>
<tr >
    <td class="detaillabel" ><yfc:i18n>Payment_Reference_#1</yfc:i18n></td>
	<td>
		<input class="protectedinput" contenteditable="false" <%=getTextOptions( buildBinding("xml:/",prePathId,"/@PaymentReference1"), buildBinding("xml:/",prePathId,"/@PaymentReference1"))%>/>
	</td>

<!--
	<% if (userHasDecryptedPaymentAttributesPermissions()) {%>
		<% if (equals(storedValueCardEncrypted,"Y")) {%>
			<%
				String encryptedString = resolveValue("xml:/"+prePathId+"/@PaymentReference1");
				YFCElement inputElem = YFCDocument.parse("<GetDecryptedString StringToDecrypt=\""+encryptedString+"\"/>").getDocumentElement();
				YFCElement templateElem = YFCDocument.parse("<GetDecryptedString DecryptedString=\"\"/>").getDocumentElement();
			%>
			<% logPaymentInfo(catSecurity,"PaymentReference1","DisplayPaymentReference1",resolveValue(buildBinding("xml:/",prePathId,"/@DisplayPaymentReference1")),(YFCElement)request.getAttribute("Order")); %>
			<yfc:callAPI apiName="getDecryptedString" inputElement="<%=inputElem%>" templateElement="<%=templateElem%>" outputNamespace="GetDecryptedString"/>
			<td>
				<input class="protectedinput" contenteditable="false" <%=getTextOptions( "xml:/GetDecryptedString/@DecryptedString", "xml:/GetDecryptedString/@DecryptedString")%>/>
			</td>
		<% } else { %>
			<td>
				<input class="protectedinput" contenteditable="false" <%=getTextOptions( buildBinding("xml:/",prePathId,"/@PaymentReference1"), buildBinding("xml:/",prePathId,"/@PaymentReference1"))%>/>
			</td>
	    <% } %>
    <% } else { %>
        <td class="protectedtext">
			<%=showEncryptedString(resolveValue(buildBinding("xml:/",prePathId,"/@DisplayPaymentReference1")))%>&nbsp;
		</td>
    <% } %>
-->

	<td class="detaillabel" ><yfc:i18n>Payment_Reference_#2</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding='<%=buildBinding("xml:/",prePathId,"/@PaymentReference2")%>'/>&nbsp;</td>
</tr>
<tr >
    <td class="detaillabel" ><yfc:i18n>Payment_Reference_#3</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding='<%=buildBinding("xml:/",prePathId,"/@PaymentReference3")%>'/>&nbsp;</td>

<% }  
else {%> 

    <td class="detaillabel" ><yfc:i18n>Payment_Reference_#1</yfc:i18n></td>
    <% if (userHasDecryptedPaymentAttributesPermissions()) {%>
		<% if (equals(otherPaymentTypeEncrypted,"Y")) {%>
			<%
				String encryptedString = resolveValue("xml:/"+prePathId+"/@PaymentReference1");
				YFCElement inputElem = YFCDocument.parse("<GetDecryptedString StringToDecrypt=\""+encryptedString+"\"/>").getDocumentElement();
				YFCElement templateElem = YFCDocument.parse("<GetDecryptedString DecryptedString=\"\"/>").getDocumentElement();
			%>
			<% logPaymentInfo(catSecurity,"PaymentReference1","DisplayPaymentReference1",resolveValue(buildBinding("xml:/",prePathId,"/@DisplayPaymentReference1")),(YFCElement)request.getAttribute("Order")); %>
			<yfc:callAPI apiName="getDecryptedString" inputElement="<%=inputElem%>" templateElement="<%=templateElem%>" outputNamespace="GetDecryptedString"/>
			<td>
				<input class="protectedinput" contenteditable="false" <%=getTextOptions( "xml:/GetDecryptedString/@DecryptedString", "xml:/GetDecryptedString/@DecryptedString")%>/>
			</td>
		<% } else { %>
			<td>
				<input class="protectedinput" contenteditable="false" <%=getTextOptions( buildBinding("xml:/",prePathId,"/@PaymentReference1"), buildBinding("xml:/",prePathId,"/@PaymentReference1"))%>/>
			</td>
	    <% } %>
    <% } else { %>
        <td class="protectedtext">
			<%=showEncryptedString(resolveValue(buildBinding("xml:/",prePathId,"/@DisplayPaymentReference1")))%>&nbsp;
		</td>
    <% } %>
</tr>
<tr >
    <td class="detaillabel" ><yfc:i18n>Payment_Reference_#2</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding='<%=buildBinding("xml:/",prePathId,"/@PaymentReference2")%>'/>&nbsp;</td>
    <td class="detaillabel" ><yfc:i18n>Payment_Reference_#3</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding='<%=buildBinding("xml:/",prePathId,"/@PaymentReference3")%>'/>&nbsp;</td>
</tr>
<%}
  if (equals(showAuthorizationParams, "Y")) {    
%>

    <td class="detaillabel" ><yfc:i18n>Authorization_ID</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/ChargeTransactionDetail/@AuthorizationID"/>&nbsp;</td>
</tr>
<tr >
    <td class="detaillabel" ><yfc:i18n>Authorization_Expiration_Date</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/ChargeTransactionDetail/@AuthorizationExpirationDate"/>&nbsp;</td>
    <td class="detaillabel" ><yfc:i18n>Pending_Execution_Amount</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/ChargeTransactionDetail/@RequestAmount"/>&nbsp;</td>
</tr>
<tr >
    <td class="detaillabel" ><yfc:i18n>Authorization_Code</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/ChargeTransactionDetail/CreditCardTransactions/CreditCardTransaction/@AuthCode"/>&nbsp;</td>
    <td class="detaillabel" ><yfc:i18n>Authorization_AVS</yfc:i18n></td>
    <td>
        <input class="protectedinput" contenteditable="false" <%=getTextOptions( "xml:/ChargeTransactionDetail/CreditCardTransactions/CreditCardTransaction/@AuthAvs", "xml:/ChargeTransactionDetail/CreditCardTransactions/CreditCardTransaction/@AuthAvs")%>/>
    </td>
</tr>
<tr >
    <td class="detaillabel" ><yfc:i18n>Authorization_Message</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/ChargeTransactionDetail/CreditCardTransactions/CreditCardTransaction/@AuthReturnMessage"/>&nbsp;</td>
	<% if (equals("CREDIT_CARD", paymentTypeGroup)) {%>
		<td class="detaillabel" ><yfc:i18n>CVV_Authorization_Code</yfc:i18n></td>
		<td class="protectedtext"><yfc:getXMLValue binding="xml:/ChargeTransactionDetail/CreditCardTransactions/CreditCardTransaction/@CVVAuthCode"/>&nbsp;</td>
    <% } %>
</tr>
<%}%>
</table>
