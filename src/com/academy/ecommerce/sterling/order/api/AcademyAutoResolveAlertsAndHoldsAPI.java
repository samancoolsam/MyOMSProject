package com.academy.ecommerce.sterling.order.api;

import java.io.File;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;

public class AcademyAutoResolveAlertsAndHoldsAPI implements YIFCustomApi {
	
	

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}
	
/*public static void main(String args[]) {
		File f = new File("D:/TestInput_ResolvePartAvailHoldAlert_0.xml");
	//	File f1 = new File("C:/test1.xml");
		Document docInput=XMLUtil.getDocumentFromFile(f);
		System.out.println("#### Input Document is " + XMLUtil.getXMLString(docInput));
		AcademyAutoResolveAlertsAndHoldsAPI instance=new AcademyAutoResolveAlertsAndHoldsAPI();
		Document docTest=instance.createInputToResolveHold("20090814162304310526",null);
		System.out.println("#### Output of createInputToResolveHold is " + XMLUtil.getXMLString(docTest));
		Document docTest1=instance.callChangeOrderToCancelQuantitiesAndRemoveHold(docInput,docTest,"20090814162304310526",null);
		System.out.println("#### Output of callChangeOrderToCancelQuantitiesAndRemoveHold is " + XMLUtil.getXMLString(docTest1));
	
		
	}*/

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyAutoResolveAlertsAndHoldsAPI.class);
	public Document processAndAutoResolveHoldsAndAlerts(YFSEnvironment env,
			Document inDoc) {

		String strOrdrHdrKey = null;
		Document docOrder = null;
		Document docOutput=null;
		//STL-1265: Avoid backorder cancellation logic for Gift card refund "Y" orders - START
		String strOrderPurpose=null;
		String strOrderNo=null;
		//STL-1265: Avoid backorder cancellation logic for Gift card refund "Y" orders - END

		try {
			log.beginTimer(" Begining of AcademyAutoResolveAlertsAndHoldsAPI->processAndAutoResolveHoldsAndAlerts Api");
			if (!YFCObject.isVoid(inDoc)) {
				strOrdrHdrKey = XPathUtil.getString(inDoc.getDocumentElement(),
						AcademyConstants.XPATH_ORDERHEADER_KEY);
				//STL-1265: Avoid backorder cancellation logic for Gift card refund "Y" orders - START
				strOrderPurpose = XPathUtil.getString(inDoc.getDocumentElement(),
						AcademyConstants.XPATH_ORDER_PURPOSE);
				strOrderNo = XPathUtil.getString(inDoc.getDocumentElement(),
						AcademyConstants.XPATH_ORDER_NO);
				//STL-1265: Avoid backorder cancellation logic for Gift card refund "Y" orders - END
			}
			//STL-1265: Avoid backorder cancellation logic for Gift card refund "Y" orders - START
			if(!(strOrderPurpose.equalsIgnoreCase(AcademyConstants.STR_REFUND) && strOrderNo.startsWith(AcademyConstants.STR_YES)))
			{
				log.verbose("Order is not Refund , Y Order");
				//STL-1265: Avoid backorder cancellation logic for Gift card refund "Y" orders - END
			docOrder = createInputToResolveHold(strOrdrHdrKey, env);
			docOutput=callChangeOrderToCancelQuantitiesAndRemoveHold(inDoc, docOrder,
					strOrdrHdrKey, env);
			}
			log.endTimer(" End of AcademyAutoResolveAlertsAndHoldsAPI->processAndAutoResolveHoldsAndAlerts Api");
			return docOutput;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

	}

	private Document createInputToResolveHold(String orderHeaderKey,
			YFSEnvironment env) {
		Document docChangeOrderInput = null;
		Element eleOrderHoldTypes = null;
		Element eleOrderHoldType = null;

		try {
			docChangeOrderInput = XMLUtil
					.createDocument(AcademyConstants.ELE_ORDER);
			docChangeOrderInput.getDocumentElement().setAttribute(
					AcademyConstants.STR_ORDR_HDR_KEY, orderHeaderKey);
			eleOrderHoldTypes = docChangeOrderInput
					.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPES);
			XMLUtil.appendChild(docChangeOrderInput.getDocumentElement(),
					eleOrderHoldTypes);
			eleOrderHoldType = docChangeOrderInput
					.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPE);
			XMLUtil.appendChild(eleOrderHoldTypes, eleOrderHoldType);
			eleOrderHoldType.setAttribute(AcademyConstants.ATTR_HOLD_TYPE,
					AcademyConstants.ATTR_PART_AVAIL_HOLD);
			eleOrderHoldType.setAttribute(AcademyConstants.ATTR_STATUS,
					AcademyConstants.STR_HOLD_RESOLVED_STATUS);
			return docChangeOrderInput;

		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
	}

	private Document callChangeOrderToCancelQuantitiesAndRemoveHold(
			Document docInput, Document docChangeOrderInput, String orderHeaderKey,
			YFSEnvironment env) {
		log.beginTimer(" Begin of AcademyAutoResolveAlertsAndHoldsAPI->callChangeOrderToCancelQuantitiesAndRemoveHold Api");
		NodeList nListOrderLines = null;
		Element eleCurrentOrderLine = null;
		Element eleOrderLines = null;
		Element eleOrderLine = null;
		Element eleItem = null;
		Element eleItemTransQty = null;
		//Document docChangeOrderInput = null;
		Document docApiResult=null;
		String strBackOrderedQty = null;
		String strCancelQty = null;
		int iCancelQty = 0;
		int iRevisedOrderQty = 0;
		int iNoOfOrderLines = 0;
		nListOrderLines = docInput
				.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINE);
		iNoOfOrderLines = nListOrderLines.getLength();
		//boolean bCallChangeOrder = false;
				try {
			for (int i = 0; i < iNoOfOrderLines; i++) {
				eleCurrentOrderLine = (Element) nListOrderLines.item(i);
				strBackOrderedQty = XPathUtil.getString(eleCurrentOrderLine,
						AcademyConstants.XPATH_BKORDERED_QTY);

				if (!YFCObject.isVoid(strBackOrderedQty)) {
					if (new Float(strBackOrderedQty).intValue() > 0) {
						//bCallChangeOrder = true;
						if (!YFCObject.isVoid(docChangeOrderInput)) {

							docChangeOrderInput
									.getDocumentElement()
									.setAttribute(
											AcademyConstants.ATTR_MOD_REASON_CODE,
											AcademyConstants.STR_MOD_REASON_VALUE);
							docChangeOrderInput
									.getDocumentElement()
									.setAttribute(
											AcademyConstants.ATTR_IGNORE_ORDERING,
											AcademyConstants.STR_YES);
							docChangeOrderInput
									.getDocumentElement()
									.setAttribute(
											AcademyConstants.ATTR_DOC_TYPE,
											XPathUtil
													.getString(
															docInput
																	.getDocumentElement(),
															AcademyConstants.XPATH_ORDR_DOC_TYPE));
							eleOrderLines=(Element)docChangeOrderInput.getElementsByTagName("OrderLines").item(0);
							if(YFCObject.isVoid(eleOrderLines))
							{
								eleOrderLines = docChangeOrderInput
									.createElement(AcademyConstants.ELEM_ORDER_LINES);
								XMLUtil.appendChild(docChangeOrderInput
										.getDocumentElement(), eleOrderLines);
							}
							
							eleOrderLine = docChangeOrderInput
									.createElement(AcademyConstants.ELEM_ORDER_LINE);
							eleOrderLine.setAttribute(
									AcademyConstants.ATTR_ACTION,
									AcademyConstants.STR_ACTION_MODIFY_UPPR);
							eleOrderLine
									.setAttribute(
											AcademyConstants.ATTR_ORDER_LINE_KEY,
											eleCurrentOrderLine
													.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
							eleOrderLine
									.setAttribute(
											AcademyConstants.ATTR_DEL_METHOD,
											eleCurrentOrderLine
													.getAttribute(AcademyConstants.ATTR_DEL_METHOD));
							eleOrderLine
									.setAttribute(
											AcademyConstants.ATTR_AVBL_QTY_TO_CANCEL,
											eleCurrentOrderLine
													.getAttribute(AcademyConstants.ATTR_OPEN_QTY));
							eleOrderLine
									.setAttribute(
											AcademyConstants.ATTR_OPEN_QTY,
											eleCurrentOrderLine
													.getAttribute(AcademyConstants.ATTR_OPEN_QTY));
							eleOrderLine.setAttribute(
									AcademyConstants.ATTR_OPNQTY_MOD_ALLOWED,
									AcademyConstants.STR_YES);
							eleOrderLine
									.setAttribute(
											AcademyConstants.ATTR_MODIFICN_QTY,
											eleCurrentOrderLine
													.getAttribute(AcademyConstants.ATTR_ORDERED_QTY));
							eleOrderLine
									.setAttribute(
											AcademyConstants.ATTR_ORDERED_QTY,
											eleCurrentOrderLine
													.getAttribute(AcademyConstants.ATTR_ORDERED_QTY));
							iCancelQty = new Float(strBackOrderedQty)
									.intValue();
							strCancelQty = String.valueOf(iCancelQty);
							eleOrderLine.setAttribute(
									AcademyConstants.ATTR_QTY_TO_CANCEL,
									strCancelQty);
							eleOrderLine
									.setAttribute(
											AcademyConstants.ATTR_MAXLINE_STATUS,
											eleCurrentOrderLine
													.getAttribute(AcademyConstants.ATTR_MAXLINE_STATUS));

							eleItem = docChangeOrderInput
									.createElement(AcademyConstants.ITEM);
							eleItem
									.setAttribute(
											
											AcademyConstants.ATTR_ITEM_ID,
											XPathUtil
													.getString(
															eleCurrentOrderLine,
															AcademyConstants.XPATH_ITEM_ITEMID));
							eleItem.setAttribute(AcademyConstants.ATTR_UOM,
									XPathUtil.getString(eleCurrentOrderLine,
											AcademyConstants.XPATH_ITEM_UOM));
							XMLUtil.importElement(eleOrderLine, eleItem);
							//XMLUtil.appendChild(eleOrderLine, eleItem);
							eleItemTransQty = docChangeOrderInput
									.createElement(AcademyConstants.ELE_ORDER_LINE_TRANSQTY);
							eleItemTransQty.setAttribute(
									AcademyConstants.ATTR_AVBL_QTY_TO_CANCEL,
									strCancelQty);
							eleItemTransQty.setAttribute(
									AcademyConstants.ATTR_MODIFICN_QTY,
									strCancelQty);
							eleItemTransQty.setAttribute(
									AcademyConstants.ATTR_TRANS_UOM,
									XPathUtil.getString(eleCurrentOrderLine,
											AcademyConstants.XPATH_ITEM_UOM));
							iRevisedOrderQty = (new Float(
									eleCurrentOrderLine
											.getAttribute(AcademyConstants.ATTR_ORDERED_QTY))
									.intValue())
									- (new Float(strBackOrderedQty).intValue());
							eleItemTransQty.setAttribute(
									AcademyConstants.ATTR_ORDERED_QTY, String
											.valueOf(iRevisedOrderQty));
							eleItemTransQty.setAttribute(
									AcademyConstants.ATTR_NO_SHP_MODFCNQTY,
									String.valueOf(iRevisedOrderQty));
							XMLUtil.importElement(eleOrderLine, eleItemTransQty);
							XMLUtil.importElement(eleOrderLines, eleOrderLine);
						}// end if

					}// end if
				}// end if void check
			}

			docApiResult=callChangeOrderApi(docChangeOrderInput, env);

		} catch (Exception e) {
						throw new YFSException(e.getMessage());
		}
		log.endTimer(" End of AcademyAutoResolveAlertsAndHoldsAPI->callChangeOrderToCancelQuantitiesAndRemoveHold Api");
		return docApiResult;

	}

	private Document callChangeOrderApi(Document apiInput, YFSEnvironment env) {
		Document docChangeOrderOutput = null;
		try {
			docChangeOrderOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_CHANGE_ORDER, apiInput);
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		return docChangeOrderOutput;
	}
}
