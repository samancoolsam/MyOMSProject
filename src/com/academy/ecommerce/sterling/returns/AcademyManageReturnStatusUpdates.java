package com.academy.ecommerce.sterling.returns;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/*########################################################################################################################################
*
* Project Name                : POD OMS
* Module                      : OMS
* Author                      : CTS
* Date                        : 02-DEC-2022
* Description                 : This file implements the logic to 
* 							    Publish Return Status Updates to WCS
* 									 
* Change Revision
* ----------------------------------------------------------------------------------------------------------------------------------------
* Date            Author                  Version#       Remarks/Description                     
* ----------------------------------------------------------------------------------------------------------------------------------------
* 02-JAN-2023     CTS                      4.0            Status Updates for Return Created if Return Initiated through CLS
* 16-DEC-2022     CTS					   3.0			  Status Updates for Return Refunded if Return Initiated through WCC/CLS
* 08-DEC-2022     CTS					   2.0			  Status Updates for Return Received if Return Initiated through WCS/WCC/CLS 
* 														  and for Return Refunded if Return Initiated through WCS
* 02-DEC-2022     CTS                      1.0            Status Updates for Return Created if Return Initiated through WCC
* ###########################################################################################################################################*/

public class AcademyManageReturnStatusUpdates implements YIFCustomApi {

	public static final YFCLogCategory log = YFCLogCategory.instance(AcademyManageReturnStatusUpdates.class);

	/**
	 * 
	 * This method customizes input inDocxml to Publish Return Status Updates to WCS
	 * 
	 * @param inDocXML
	 * @param env
	 * @return finalOutputDoc
	 * 
	 * @throws Exception
	 * 
	 */
	public Document manageReturnStatusUpdates(YFSEnvironment env, Document inDocXML) throws Exception {
		log.beginTimer(" Begining of manageReturnStatusUpdates method");
		log.verbose("Input to AcademyManageReturnStatusUpdates.manageReturnStatusUpdates "
				+ XMLUtil.getXMLString(inDocXML));

		Document finalOutputDoc = inDocXML;
		String postReturnUpdateToWCSFlag = "N";
		String publishPaymentInfoFlag = "N";
		if (!YFCObject.isVoid(inDocXML)) {
			String strMessageType = XPathUtil.getString(inDocXML.getDocumentElement(),
					AcademyConstants.XPATH_MESSAGE_TYPE);
			log.verbose("Message Type:: " + strMessageType);
			if (!YFCObject.isVoid(strMessageType)
					&& ((AcademyConstants.VAL_RETURN_INITIATED).equalsIgnoreCase(strMessageType)
							|| (AcademyConstants.VAL_RETURN_RECEIVED).equalsIgnoreCase(strMessageType)
							|| (AcademyConstants.VAL_RETURN_REFUNDED).equalsIgnoreCase(strMessageType))) {

				try {
			// calling callGetOrderList to invoke getOrderList API
			Document outDocOfGetOrderList = callGetOrderList(env, inDocXML);
					log.verbose(
							"Output of callGetOrderList method is::  " + XMLUtil.getXMLString(outDocOfGetOrderList));

					if (!YFCObject.isVoid(outDocOfGetOrderList)) {
				// Appending an InitiatedBy Attribute which specifies if it is
				// "ByWCS/ByCallCenter/ByCLS" and also specifying the postReturnUpdateToWCSFlag
				// value
				Element eleInDoc = inDocXML.getDocumentElement();
				String strEntryType = XPathUtil.getString(outDocOfGetOrderList.getDocumentElement(),
						AcademyConstants.XPATH_ENTRY_TYPE);
				String strOrderType = XPathUtil.getString(outDocOfGetOrderList.getDocumentElement(),
						AcademyConstants.XPATH_ORDER_TYPE);
						String strReturnOrderNo = XPathUtil.getString(inDocXML.getDocumentElement(),
								"//Order/@OrderNo");
				if (strReturnOrderNo.startsWith(AcademyConstants.STR_YES)) {
					if (!YFCObject.isVoid(strEntryType)
							&& strEntryType.equalsIgnoreCase(AcademyConstants.VAL_CALL_CENTER)) {
								eleInDoc.setAttribute(AcademyConstants.ATTR_INITIATED_BY,
										AcademyConstants.VAL_BY_CALL_CENTER);
						// If Return is Created from CallCenter and Message Type is Initiated or
						// Received or Refunded we need to send update.
						postReturnUpdateToWCSFlag = "Y";
					} else {
						eleInDoc.setAttribute(AcademyConstants.ATTR_INITIATED_BY, AcademyConstants.VAL_BY_CLS);
						// If Return is Created from CLS and Message Type is Initiated or Received or
						// Refunded we need to send update.
							postReturnUpdateToWCSFlag = "Y";
					}
				} else if (YFCObject.isVoid(strOrderType)) {
					eleInDoc.setAttribute(AcademyConstants.ATTR_INITIATED_BY, AcademyConstants.VAL_BY_WCS);
					// If Return is Created from WCS and Message Type is Received or Refunded we
					// need to send update.
					if (!strMessageType.equalsIgnoreCase(AcademyConstants.VAL_RETURN_INITIATED)) {
						postReturnUpdateToWCSFlag = "Y";
					}
				}
				//OMNI-103428 Starts
				else if (!YFCObject.isVoid(strOrderType) && strOrderType.equalsIgnoreCase(AcademyConstants.STR_INSTORE_RETURN)) {
					eleInDoc.setAttribute(AcademyConstants.ATTR_INITIATED_BY, AcademyConstants.VAL_INSTORE);
					postReturnUpdateToWCSFlag = "Y";
				}
				//OMNI-103428 ends

				if (postReturnUpdateToWCSFlag.equalsIgnoreCase("Y")) {
					// calling a method to add required attributes to the input document from the
					// output of getOrderList API
					finalOutputDoc = appendRequiredAttributes(inDocXML, outDocOfGetOrderList);
					Element eleOfFinalOutDoc = finalOutputDoc.getDocumentElement();
					if ((AcademyConstants.VAL_RETURN_INITIATED).equalsIgnoreCase(strMessageType)) {
						log.verbose("Return Initiated Flow");
						// Appending the StatusCondiiton attribute to document
						eleOfFinalOutDoc.setAttribute(AcademyConstants.ATTR_STATUS_CONDITION,
								AcademyConstants.VAL_STATUS_RETURN_CREATED);
					} else if ((AcademyConstants.VAL_RETURN_RECEIVED).equalsIgnoreCase(strMessageType)) {
						log.verbose("Return Received Flow");
						publishPaymentInfoFlag = "Y";
						// Appending the StatusCondiiton attribute to document
						eleOfFinalOutDoc.setAttribute(AcademyConstants.ATTR_STATUS_CONDITION,
								AcademyConstants.VAL_STATUS_RETURN_RECEIVED);
					} else if ((AcademyConstants.VAL_RETURN_REFUNDED).equalsIgnoreCase(strMessageType)) {
						log.verbose("Return Refunded Flow");
						//OMNI-103428 Starts
						if(!YFCObject.isVoid(strOrderType) && !strOrderType.equalsIgnoreCase(AcademyConstants.STR_INSTORE_RETURN))
						publishPaymentInfoFlag = "Y";
						//OMNI-103428 ends
						// Appending the StatusCondiiton attribute to document
						eleOfFinalOutDoc.setAttribute(AcademyConstants.ATTR_STATUS_CONDITION,
								AcademyConstants.VAL_STATUS_RETURN_REFUNDED);
					}

					eleOfFinalOutDoc.setAttribute(AcademyConstants.ATTR_PUBLISH_PAYMENT_INFO_FLAG,
							publishPaymentInfoFlag);
					log.verbose("PublishPaymentInfoFlag value is :: " + publishPaymentInfoFlag);
				}
					} else {
						YFSException e = new YFSException();
						e.setErrorCode("outDocOfGetOrderList is Void");
						e.setErrorDescription(
								"The ouptut document of AcademyManageReturnStatusUpdates.callGetOrderList method is NULL.");
						throw e;
			}
				} catch (Exception exp) {
					YFSException e = new YFSException(exp.getMessage());
					throw e;
		}
			} else {
				log.verbose(
						"***MessageType is either Void or not equal to ReturnInitiated/ReturnReceived/RefundSuccessful. Hence not Posting any Update to WCS***");
			}

			log.verbose("PostReturnUpdateToWCS Flag value is :: " + postReturnUpdateToWCSFlag);
			Element eleFinalOutDoc = finalOutputDoc.getDocumentElement();
			eleFinalOutDoc.setAttribute(AcademyConstants.ATTR_POST_RETURN_UPDATETOWCS_FLAG, postReturnUpdateToWCSFlag);
		log.verbose("Output of AcademyManageReturnStatusUpdates.manageReturnStatusUpdates "
				+ XMLUtil.getXMLString(finalOutputDoc));

		log.endTimer(" End of manageReturnStatusUpdates method");
		return finalOutputDoc;
		} else {
			YFSException e = new YFSException();
			e.setErrorCode("inDocXML is Void");
			e.setErrorDescription(
					"Input Document to AcademyManageReturnStatusUpdates.manageReturnStatusUpdates is NULL ");
			throw e;
		}

	}

	/*
	 * This method appends the required attributes to the input document from the
	 * output of getOrderList API
	 */
	private Document appendRequiredAttributes(Document inDocXML, Document outDocOfGetOrderList) throws Exception {
		log.beginTimer(" Begining of appendRequiredAttributes method");
		log.verbose("Input of appendRequiredAttributes method is inDocXML::  " + XMLUtil.getXMLString(inDocXML));
		log.verbose("Input of appendRequiredAttributes method is outDocOfGetOrderList::  "
				+ XMLUtil.getXMLString(outDocOfGetOrderList));

		NodeList nlOrderLines = inDocXML.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINE);
		int nlLenght = nlOrderLines.getLength();
		for (int i = 0; i < nlLenght; i++) {
			Element eleInDocOrderLine = (Element) nlOrderLines.item(i);
			String strOrderLineKey = eleInDocOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			log.verbose("OrderLineKey of current iteration is:: " + strOrderLineKey);

			Element eleOutDocOfGetOrderListOrderLine = XMLUtil.getElementByXPath(outDocOfGetOrderList,
					"/OrderList/Order/OrderLines/OrderLine[@OrderLineKey='" + strOrderLineKey + "']");
			if (!YFCObject.isVoid(eleOutDocOfGetOrderListOrderLine)) {
				String strExtnOrderItemIdentifier = XPathUtil.getString(eleOutDocOfGetOrderListOrderLine,
						".//DerivedFromOrderLine/Extn/@ExtnWCOrderItemIdentifier");
				String strReturnReason = eleOutDocOfGetOrderListOrderLine
						.getAttribute(AcademyConstants.ATTR_RETURN_REASON);
				eleInDocOrderLine.setAttribute(AcademyConstants.ATTR_RETURN_REASON, strReturnReason);
				eleInDocOrderLine.setAttribute(AcademyConstants.ATTR_EXTN_WC_ORDER_ITEM_IDENTIFIER,
						strExtnOrderItemIdentifier);
			}
		}

		// parsing OrderDate as yyyy-MM-DD format
		String strOrderDate = XPathUtil.getString(inDocXML.getDocumentElement(), AcademyConstants.XPATH_ORDER_DATE);

		String strDateTypeFormat = AcademyConstants.STR_SIMPLE_DATE_PATTERN;
		SimpleDateFormat sdf = new SimpleDateFormat(strDateTypeFormat);
		String DateFormatter = AcademyConstants.STR_SIMPLE_DATE_PATTERN;
		SimpleDateFormat dateFormat = new SimpleDateFormat(DateFormatter, Locale.ENGLISH);
		Date parsingTS = dateFormat.parse(strOrderDate);
		String strDate = sdf.format(parsingTS.getTime());

		Element eleInput = inDocXML.getDocumentElement();
		eleInput.setAttribute(AcademyConstants.ATTR_ORDER_RETURN_DATE, strDate);

		log.endTimer(" End of appendRequiredAttributes method");
		return inDocXML;
	}

	/**
	 * 
	 * This method invokes the 'getOrderList' API to get the required attributes.
	 * 
	 * @param inDocXML
	 * @param env
	 * @return getOrderListOutDoc
	 * 
	 * @throws Exception
	 * 
	 */
	private Document callGetOrderList(YFSEnvironment env, Document inDocXML) {
		log.beginTimer(" Begining of callGetOrderList method");
		log.verbose("Input to callGetOrderList method is ::" + XMLUtil.getXMLString(inDocXML));
		Document getOrderListOutDoc = null;
		String strOrderHeaderKey = null;
		try {
			strOrderHeaderKey = XPathUtil.getString(inDocXML.getDocumentElement(), "//Order/@OrderHeaderKey");
		if (!YFCObject.isVoid(strOrderHeaderKey)) {
			// preparing input to getOrderList API
			Document getOrderListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			Element getOrderListInEle = getOrderListInDoc.getDocumentElement();
			getOrderListInEle.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);

			env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST,
					AcademyConstants.STR_TEMPLATEFILE_GETORDERLIST_RETURNS);

			log.verbose("Input to getOrderList API is ::" + XMLUtil.getXMLString(getOrderListInDoc));
			getOrderListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, getOrderListInDoc);
			log.verbose("Output of getOrderList API is ::" + XMLUtil.getXMLString(getOrderListOutDoc));
			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
			} else {
				YFSException e = new YFSException();
				e.setErrorCode("OrderHeaderKey is Void");
				e.setErrorDescription(
						"OrderHeaderKey in the inDOCXML of AcademyManageReturnStatusUpdates.manageReturnStatusUpdates is NULL.");
				throw e;
			}
		} catch (Exception exp) {
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}
		log.endTimer(" End of callGetOrderList method");
		return getOrderListOutDoc;
	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

}
