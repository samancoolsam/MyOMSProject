package com.academy.ecommerce.sterling.oraclerightnow;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyOracleRNGetReturnOrExchangeOrderDetails {
	/**
	 * @author kgopal
	 * 
	 * Customer gives Order No to fetch return/exchange order details
	 * 
	 * Service Name: AcademyOracleRNGetReturnOrderDetails
	 * 
	 * Input XML:
	 * 
	 * 	 <Input FlowName="AcademyOracleRNGetReturnOrderDetails"> 
	        <Login LoginID="admin" Password="password" /> 
	        <Order OrderNo="15003503" OrderHeaderKey="201404080332252365644129"/>
		</Input> 
	 * 	
	 * 		
	 */
	
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyOracleRNGetOrderDetails.class);
	
	public Document oracleRNGetReturnOrderDetails(YFSEnvironment env,Document inDoc) throws Exception{
		if (log.isVerboseEnabled()) {
		log.verbose("Input to method reqOracleCCGetReturnOrderDetails is: "+XMLUtil.getXMLString(inDoc));
		}
		HashMap<String, String> hmTest = new HashMap<String, String>();
		Document docLoginOutput = null;
		Document docSalesOrderDetailsInput= null;
		Document docSalesOrderDetailsOutput = null;
		Document docOrderRetDetailsOutput = null;
		
		/*Element eleSalOrder = null;
		Element eleSalReturnOrders = null;
		Element eleSalReturnOrder = null;*/
		Element eleSalExchangeOrders = null;
		Element eleSalExchangeOrder = null;
		//Element eleOrderLine = null;
		
		double orderRefund = 0.00;
		double orderPendingRefund = 0.00;
		double orderActualPendingRefund = 0.00;
		double orderExchangeTotalAmount= 0.00;
		double orderReturnTotalAmount = 0.00;
		
		
		Element eleRetOrderList = null;
		Element eleRetOrder = null;
		Element eleRetOrderLines = null;
		Element eleRetOrderLine = null;
		Element eleOutOrder = null;
		Element eleOutOrderLines = null;
		Element eleOutOrderLine = null;
		
		String strOrderLineKey = "";
		String strReasonCode = "";
		String strReturnReasonValue = "";
		String strIntRetReason = "";
		
		DecimalFormat decimalCorrection = new DecimalFormat("0.00");
		
		Element rootElement = inDoc.getDocumentElement();
		Element eleLogin = (Element)rootElement.getElementsByTagName("Login").item(0);
		String strLoginID = eleLogin.getAttribute("LoginID");
		String strPassword = eleLogin.getAttribute("Password");
		Document docResponse = AcademyUtil.validateLoginCredentials(env,strLoginID, strPassword);				
		if (YFCObject.isVoid(docResponse)) {
			docLoginOutput = XMLUtil.createDocument("Error");
			docLoginOutput.getDocumentElement().setAttribute("ErrorCode",
					AcademyConstants.LOGIN_ERROR_CODE);
			docLoginOutput.getDocumentElement().setAttribute(
					"ErrorDescription", AcademyConstants.LOGIN_ERROR_DESC);
			if (log.isVerboseEnabled()) {
			log.verbose("Error Output is :"+XMLUtil.getXMLString(docLoginOutput));
			}
			return docLoginOutput;
		} else 
		{
			if (log.isVerboseEnabled()) {
				log.verbose("User is authenticated successfully. Going to call getOrderDetails() API");
		}
			
			
			Element eleOrder = (Element)rootElement.getElementsByTagName("Order").item(0);
			
			//added for getting OrderHeaderKey if not coming from input
			/*Document docOrderDetailsInput=null;
			Document docOrderDetailsOutput=null;
			
			eleOrder.setAttribute("DocumentType",
					AcademyConstants.SALES_DOCUMENT_TYPE);
			eleOrder.setAttribute("EnterpriseCode",
					AcademyConstants.ENTERPRISE_CODE_SHIPMENT);

			docOrderDetailsInput = XMLUtil.getDocumentForElement(eleOrder);
			Document getOrderListOPTempl = XMLUtil.getDocument("<OrderList>" + "<Order OrderNo=\"\" OrderHeaderKey=\"\" />"
						+ "</OrderList>");
        	env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, getOrderListOPTempl);

			if (log.isVerboseEnabled()) {
				log.verbose("Order Detail input is :"
						+ XMLUtil.getXMLString(docOrderDetailsInput));
			}
			docOrderDetailsOutput = AcademyUtil.invokeAPI(env,
				"getOrderList", docOrderDetailsInput);

			if (log.isVerboseEnabled()) {
				log.verbose("Order Detail raw output from service is :"
						+ XMLUtil.getXMLString(docOrderDetailsOutput));
			}
			
			Element eleGetOrderListRoot = docOrderDetailsOutput.getDocumentElement();
			Element eleGetOrder = (Element)eleGetOrderListRoot.getElementsByTagName("").item(0);
			String strOrderHeaderKey = eleGetOrder.getAttribute("OrderHeaderKey");*/
			//added for getting OrderHeaderKey if not coming from input
			
			String strOrderHeaderKey= eleOrder.getAttribute("OrderHeaderKey");
			docSalesOrderDetailsInput = XMLUtil.createDocument("Order");
			Element docOrder = docSalesOrderDetailsInput.getDocumentElement();
			Element docOrderLine = docSalesOrderDetailsInput.createElement("OrderLine");
			docOrderLine.setAttribute("DerivedFromOrderHeaderKey", strOrderHeaderKey);
			docOrder.appendChild(docOrderLine);
			if (log.isVerboseEnabled()) {
				log.verbose("The input to get salesOrderDetails Is: "+XMLUtil.getXMLString(docSalesOrderDetailsInput));
			}
			
			docSalesOrderDetailsOutput = AcademyUtil.invokeService(env, "AcademyOracleGetOrderListForReturnOrder", docSalesOrderDetailsInput);
			if (log.isVerboseEnabled()) {
				log.verbose("output of getOrderDetails API is :"+XMLUtil.getXMLString(docSalesOrderDetailsOutput));
			}
		
			eleRetOrderList = docSalesOrderDetailsOutput.getDocumentElement();
			if ((!YFCObject.isVoid(eleRetOrderList))&& (eleRetOrderList.hasChildNodes())){
				NodeList nlReturnOrder = eleRetOrderList.getElementsByTagName("Order");
				for (int i=0; i < nlReturnOrder.getLength(); i++){
					double exchangeTotalAmount= 0.00;
					double returnTotalAmount = 0.00;
					double grandTotal = 0.00;
					double transferToExchange = 0.00;
					double appliedToExchange = 0.00;
					double refund = 0.00;
					double pendingRefund = 0.00;
					double actualPendingRefund = 0.00;
					String strGrandTotal = "";
					String strExchangeAmount = "";
					String strReturnAmount = "";
					
					// To get the return Reason Code
					
					eleRetOrder = (Element) nlReturnOrder.item(i);
					eleRetOrderLines = (Element) eleRetOrder.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINES).item(0);
					if (!YFCObject.isVoid(eleRetOrderLines)){
						NodeList nlRetOrderLine = eleRetOrderLines.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINE);
						  for (int f=0; f < nlRetOrderLine.getLength(); f++){
							  eleRetOrderLine  = (Element) nlRetOrderLine.item(f);
							  strOrderLineKey = eleRetOrderLine.getAttribute("DerivedFromOrderLineKey");
							  strReasonCode = eleRetOrderLine.getAttribute("ReturnReason");
							  strIntRetReason = getReturnReasonCode(env,strReasonCode);
							  strReturnReasonValue = getCommonCodeList(env,strIntRetReason);
								
									String RCode ="";
									
									if(hmTest.containsKey(strOrderLineKey))
									{
										log.verbose("iF Condition:: "+ strOrderLineKey);
										String strRR = hmTest.get(strOrderLineKey);
										RCode = strRR + "|" + strReturnReasonValue;
									}
									else
									{
										log.verbose("Else Condition");
										RCode = strReturnReasonValue;
									}
									hmTest.put(strOrderLineKey, RCode);
									
								
						  }
					}
					
					// To get the return Reason Code
										
				    Element eleRetPriceInfo = (Element)eleRetOrder.getElementsByTagName("PriceInfo").item(0);
				    strReturnAmount = eleRetPriceInfo.getAttribute("TotalAmount");
				    returnTotalAmount = Double.parseDouble(strReturnAmount);
				    if (log.isVerboseEnabled()) {
				    	log.verbose("returnTotalAmount is ::"+returnTotalAmount);
				    }			    
				    
				    eleSalExchangeOrders = (Element)eleRetOrder.getElementsByTagName("ExchangeOrders").item(0);
				    
				    if ((!YFCObject.isVoid(eleSalExchangeOrders))&& (eleSalExchangeOrders.hasChildNodes())){
				    NodeList nlExchangeOrder = eleSalExchangeOrders.getElementsByTagName("ExchangeOrder");
				    for (int j=0; j < nlExchangeOrder.getLength(); j++){
				    	eleSalExchangeOrder = (Element) nlExchangeOrder.item(j);
				    	   Element eleExPriceInfo = (Element)eleSalExchangeOrder.getElementsByTagName("PriceInfo").item(0);
				    	   strExchangeAmount = eleExPriceInfo.getAttribute("TotalAmount");			    	   
				    	   exchangeTotalAmount = Double.parseDouble(strExchangeAmount);  
				    	   if (log.isVerboseEnabled()) {
				    		   log.verbose("exchangeTotalAmount is ::"+exchangeTotalAmount);
				    	   }
				    	   
				    }
				    }
				    Element eleRetInvoiceTotals = (Element)eleRetOrder.getElementsByTagName("InvoicedTotals").item(0);
				    if (!YFCObject.isVoid(eleRetInvoiceTotals)) {
				       strGrandTotal = eleRetInvoiceTotals.getAttribute("GrandTotal");
				       grandTotal = Double.parseDouble(strGrandTotal);
				       if (log.isVerboseEnabled()) {
				    	   log.verbose("grandTotal is ::"+grandTotal);
				       }
				      
				      }
				    if (returnTotalAmount > exchangeTotalAmount){
				        transferToExchange = exchangeTotalAmount;
				        if (log.isVerboseEnabled()) {
				        	log.verbose("transferToExchange is ::"+transferToExchange);
				        }
				        
				    }
				      else {
				    	  transferToExchange = returnTotalAmount;
				    	   if (log.isVerboseEnabled()) {
				    		   log.verbose("transferToExchange in else loop is ::"+transferToExchange);
				    	   }
				    	 
				      }
				    if (!strGrandTotal.equals("0.00")) {
				    	 if (log.isVerboseEnabled()) {
				    		 log.verbose("grand total is not equal to 0.00"); 
				    	 }
				    	
				        if (returnTotalAmount > exchangeTotalAmount)
				        {
				          pendingRefund = (returnTotalAmount - exchangeTotalAmount);
				          
				        }
				        
				        if (grandTotal > exchangeTotalAmount) {
				          appliedToExchange = exchangeTotalAmount;
				          refund = (grandTotal - appliedToExchange);
				        } else {
				          appliedToExchange = grandTotal;
				          refund = 0.00;
				        }
				        if (returnTotalAmount - transferToExchange - refund > 0.00){
				          actualPendingRefund = (returnTotalAmount - transferToExchange - refund);
				          
				        }
				        
				      }
				    else
				    {
				      if (returnTotalAmount > exchangeTotalAmount){
				        pendingRefund = (returnTotalAmount - exchangeTotalAmount);
				        
				      }
				      
				      if (returnTotalAmount - transferToExchange > 0.00){
				        actualPendingRefund = (returnTotalAmount - transferToExchange);
				        
				      }
				     
				    }
				    eleRetOrder.setAttribute("TotalReturnAtLineLevel",decimalCorrection.format(returnTotalAmount));
				    log.verbose("Total return At Line Level is :"+returnTotalAmount);
				    eleRetOrder.setAttribute("TotalExchangeAtLineLevel",decimalCorrection.format(exchangeTotalAmount));
				    log.verbose("Total Exchange At Line Level is :"+exchangeTotalAmount);
				    eleRetOrder.setAttribute("RefundPendingAtLineLevel",decimalCorrection.format(pendingRefund));
				    log.verbose("Refund Pending At Line Level is :"+pendingRefund);
				    eleRetOrder.setAttribute("AmountRefundAtLineLevel",decimalCorrection.format(refund));
				    log.verbose("Amount Refund At Line Level is :"+refund);
				    eleRetOrder.setAttribute("ActualPendingRefundAtLineLevel",decimalCorrection.format(actualPendingRefund));
				    log.verbose("Actual Pending Refund At Line Level is :"+actualPendingRefund);
				    	
				    	orderRefund = orderRefund + refund;
						orderPendingRefund = orderPendingRefund + pendingRefund ;
						orderActualPendingRefund = orderActualPendingRefund + actualPendingRefund;
						orderExchangeTotalAmount= orderExchangeTotalAmount + exchangeTotalAmount;
						orderReturnTotalAmount = orderReturnTotalAmount + returnTotalAmount;
				}
				docOrderRetDetailsOutput = XMLUtil.createDocument("Order");
				eleOutOrder = docOrderRetDetailsOutput.getDocumentElement();	
				eleOutOrderLines = docOrderRetDetailsOutput.createElement("OrderLines");
				eleOutOrder.appendChild(eleOutOrderLines);
				eleOutOrder.setAttribute("TotalReturn", decimalCorrection.format(orderReturnTotalAmount));
				log.verbose("Total return is :"+orderReturnTotalAmount);
				eleOutOrder.setAttribute("TotalExchange", decimalCorrection.format(orderExchangeTotalAmount));
				log.verbose("Total Exchange is :"+orderExchangeTotalAmount);
				eleOutOrder.setAttribute("RefundPending", decimalCorrection.format(orderPendingRefund));
				log.verbose("Refund Pending is :"+orderPendingRefund);
				eleOutOrder.setAttribute("AmountRefund", decimalCorrection.format(orderRefund));
				log.verbose("Amount Refund is :"+orderRefund);
				eleOutOrder.setAttribute("ActualPendingRefund", decimalCorrection.format(orderActualPendingRefund));
				 log.verbose("Actual Pending Refund is :"+orderActualPendingRefund);
				 
				
				 Iterator orderLineIterator = hmTest.entrySet().iterator();
					while (orderLineIterator.hasNext()) {
						Entry thisEntry = (Entry) orderLineIterator.next();

						Object key = thisEntry.getKey();
						String strHashOrderLineKey = key.toString();
						String strHashReasonCode = hmTest.get(strHashOrderLineKey);
						 eleOutOrderLine = docOrderRetDetailsOutput.createElement("OrderLine");
						 eleOutOrderLine.setAttribute("OrderLineKey", strHashOrderLineKey);
						 eleOutOrderLine.setAttribute("ReturnReason", strHashReasonCode);
						eleOutOrderLines.appendChild(eleOutOrderLine);
						 
					}
				
				 if (log.isVerboseEnabled()) {
					 log.verbose("output to the web service call for get order details is : "+XMLUtil.getXMLString(docOrderRetDetailsOutput));
				 }
				 return docOrderRetDetailsOutput;
			}
			else {
				return docSalesOrderDetailsOutput;
			}
			
		}
		
		
	}

	private String getCommonCodeList(YFSEnvironment env,String strIntRetReason) throws Exception {

		log.beginTimer("***Beginning of getCommonCodeList()");
		
		Document getCommonCodeListInDoc =null;
		Document getCommonCodeListOutDoc = null;
		String strCommCodeShortDescription = "";
		
        try {
        	getCommonCodeListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
        	getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.RETURN_REASON);
        	getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
        	getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE, strIntRetReason);
        	 if (log.isVerboseEnabled()) {
        		 log.verbose("getCommonCodeList input Doc: "+XMLUtil.getXMLString(getCommonCodeListInDoc));
        	 }
        	
        	Document getCommonCodeListOPTempl = XMLUtil.getDocument("<CommonCode OrganizationCode ='' CodeType='' CodeValue='' CodeShortDescription=''/>");
        	env.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST, getCommonCodeListOPTempl);
      
        	getCommonCodeListOutDoc = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_COMMONCODE_LIST, getCommonCodeListInDoc);
        	env.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);
        	 if (log.isVerboseEnabled()) {
        		 log.verbose("getCommonCodeList API Output Doc: " +XMLUtil.getXMLString(getCommonCodeListOutDoc));
        	 }
        	
        	Element eleOutCommomCodeList = getCommonCodeListOutDoc.getDocumentElement();
        	Element eleOutCommomCode = (Element)eleOutCommomCodeList.getElementsByTagName("CommonCode").item(0);
        	strCommCodeShortDescription = eleOutCommomCode.getAttribute("CodeShortDescription"); 
        }catch (Exception e){
			e.printStackTrace();
			throw (Exception)e;
        }
        if (log.isVerboseEnabled()) {
        	 log.endTimer("***strReasonCode is : :"+strCommCodeShortDescription);
     		
        }
       
		return strCommCodeShortDescription;
		// TODO Auto-generated method stub
		
	}
	private String getReturnReasonCode(YFSEnvironment env, String strReasonCode) {
		
		int returnReason = Integer.parseInt(strReasonCode);
		String strreturnReason = Integer.toString(returnReason);
		if (log.isVerboseEnabled()) {
			log.verbose("int value is ::"+strreturnReason);
		}
		return strreturnReason;
	}
}

