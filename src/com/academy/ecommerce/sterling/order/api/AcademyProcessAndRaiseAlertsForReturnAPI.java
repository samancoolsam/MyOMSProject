package com.academy.ecommerce.sterling.order.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;

public class AcademyProcessAndRaiseAlertsForReturnAPI implements YIFCustomApi {
	
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyProcessAndRaiseAlertsForReturnAPI.class);
	public void raiseAlertsForReturnOrder(YFSEnvironment env, Document inDoc)
	{
		String strSalesOrderHeaderKey=null;
		String strReturnOrderHeaderKey=null;
		String strReturnOrderNo=null;
		String strExchangeOrderNo=null;
		Document docSalesOrderDetails=null;
		Element eleMatchingReturnOrder=null;
		Element eleMatchingExchangeOrder=null;
		
		try
		{ log.beginTimer(" Begining of AcademyProcessAndRaiseAlertsForReturnAPI->raiseAlertsForReturnOrder Api");
			strReturnOrderHeaderKey=XPathUtil.getString(inDoc.getDocumentElement(),AcademyConstants.XPATH_MONITOR_ORDER_HEADER_KEY);
			strSalesOrderHeaderKey=XPathUtil.getString(inDoc.getDocumentElement(),AcademyConstants.XPATH_MONITOR_DERIVEDORDER_HEADER_KEY);
/*			System.out.println("############  Return Order HEader Key #######"+strReturnOrderHeaderKey);
			System.out.println("############  Sales Order HEader Key #######"+strSalesOrderHeaderKey);*/
			docSalesOrderDetails=getSalesOrderDetailsWithReturn(strSalesOrderHeaderKey,env); 
/*			if(!YFCObject.isVoid(docSalesOrderDetails))
			System.out.println("############  docSalesOrderDetails #######"+XMLUtil.getXMLString(docSalesOrderDetails));
			else
				System.out.println("############### docSalesOrderDetails is NULL ################");*/
			eleMatchingReturnOrder=(Element) XPathUtil.getNode(docSalesOrderDetails.getDocumentElement(),"//ReturnOrders/ReturnOrder[@OrderHeaderKey='"+strReturnOrderHeaderKey+"']");
/*			if(!YFCObject.isVoid(eleMatchingReturnOrder))
				System.out.println("############  eleMatchingReturnOrder #######"+XMLUtil.getElementXMLString(eleMatchingReturnOrder));
			else
				System.out.println("############### eleMatchingReturnOrder is NULL ################");	*/	
			strReturnOrderNo=eleMatchingReturnOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			strExchangeOrderNo=XPathUtil.getString(eleMatchingReturnOrder,AcademyConstants.XPATH_EXCHANGE_ORDER_ORDERNO);
/*			System.out.println("############### Matching Return Order No" +strReturnOrderNo);
			System.out.println("############### Matching Exchange Order No" +strExchangeOrderNo);*/
			raiseReturnNonReceiptAlert(strReturnOrderNo,strExchangeOrderNo,strReturnOrderHeaderKey,env);
			log.endTimer(" End of AcademyProcessAndRaiseAlertsForReturnAPI->raiseAlertsForReturnOrder Api");
			
		}
		 catch (Exception e) {
				e.printStackTrace();
				throw new YFSException(e.getMessage());
			}
		
		
	}
	
	private void raiseReturnNonReceiptAlert(String strReturnOrderNo,String strExchangeOrderNo,String strReturnOrderHeaderKey,YFSEnvironment env )
	{
		log.beginTimer(" Begining of AcademyProcessAndRaiseAlertsForReturnAPI->raiseAlertsForReturnOrder Api");
		Element eleInboxRefList = null;
		Element eleInboxRef = null;
		Element eleConsolidationTemplt=null;
		Element eleInboxConsolidationTemplt=null;
		String strExceptionValue = null;
		Document docExceptionInput = null;
		Document docCreateExceptionAPIOutput = null;
		try {
			docExceptionInput = XMLUtil
					.createDocument(AcademyConstants.ELE_INBOX);
			docExceptionInput.getDocumentElement()
					.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG,
							AcademyConstants.STR_YES);
			docExceptionInput.getDocumentElement()
			.setAttribute(AcademyConstants.ATTR_CONSOLIDATE,
					AcademyConstants.STR_YES);
			docExceptionInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_EXCPTN_TYPE,
					AcademyConstants.STR_RETURN_NONRECEIPT_EXCEPTION);
			docExceptionInput.getDocumentElement().setAttribute(
					AcademyConstants.STR_ORDR_HDR_KEY,
					strReturnOrderHeaderKey);
			docExceptionInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_ORDER_NO,
					strReturnOrderNo);
			eleConsolidationTemplt=docExceptionInput
			.createElement(AcademyConstants.ELE_CONSOLIDATE_TEMPLT);
			XMLUtil.appendChild(docExceptionInput.getDocumentElement(), eleConsolidationTemplt);
			eleInboxConsolidationTemplt=docExceptionInput.createElement(AcademyConstants.ELE_INBOX);
			eleInboxConsolidationTemplt.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, "");
			eleInboxConsolidationTemplt.setAttribute(AcademyConstants.ATTR_ORDER_NO, "");
			eleInboxConsolidationTemplt.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE, "");
			XMLUtil.appendChild(eleConsolidationTemplt, eleInboxConsolidationTemplt);
			eleInboxRefList = docExceptionInput
					.createElement(AcademyConstants.ELE_INBOX_REF_LIST);
			XMLUtil.appendChild(docExceptionInput.getDocumentElement(),
					eleInboxRefList);
			eleInboxRef = docExceptionInput
					.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			XMLUtil.appendChild(eleInboxRefList, eleInboxRef);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_INBOX_REFKEY, strReturnOrderNo);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE,
					AcademyConstants.STR_EXCPTN_REF_VALUE);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME,
					AcademyConstants.STR_NON_RECEIPT);
			strExceptionValue =AcademyConstants.STR_RETURN_NONRECEIPT_STRING1+strReturnOrderNo;
			if(!YFCObject.isVoid(strExchangeOrderNo))
				strExceptionValue=strExceptionValue
					+AcademyConstants.STR_RETURN_NONRECEIPT_STRING2+strExchangeOrderNo;
			eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE,
					strExceptionValue);
			
/*			if(!YFCObject.isVoid(docExceptionInput))
				System.out.println("  ############# docExceptionInput ############"+XMLUtil.getXMLString(docExceptionInput));
			else
				System.out.println("  ############# docExceptionInput is NULL############");*/
			docCreateExceptionAPIOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_CREATE_EXCEPTION, docExceptionInput);
			log.endTimer(" End of AcademyProcessAndRaiseAlertsForReturnAPI->raiseAlertsForReturnOrder Api");	
				
		} catch (Exception e) {
						throw new YFSException(e.getMessage());
		}
	}
	
	private Document getSalesOrderDetailsWithReturn(String orderHeaderKey,YFSEnvironment env)
	{
		Document docGetSalesOrderDetails=null;
		Document docGetOrderDetailsAPIInput=null;
		try
		{
			docGetOrderDetailsAPIInput=XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			docGetOrderDetailsAPIInput.getDocumentElement().setAttribute(AcademyConstants.STR_ORDR_HDR_KEY,orderHeaderKey);
			env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
					AcademyConstants.STR_TEMPLATEFILE_GETCOMPLETEORDER_DETAILS);
			docGetSalesOrderDetails=AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, docGetOrderDetailsAPIInput);
		}
		 catch (Exception e) {
				e.printStackTrace();
				throw new YFSException(e.getMessage());
			}
		
		return docGetSalesOrderDetails;
	}

}
