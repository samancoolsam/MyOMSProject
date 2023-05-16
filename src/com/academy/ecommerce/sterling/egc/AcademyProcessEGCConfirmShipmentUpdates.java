package com.academy.ecommerce.sterling.egc;

import java.util.ArrayList;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import org.apache.commons.lang3.StringUtils;

/*##################################################################################
*
* Project Name                : POD March Release
* Module                      : OMS
* Author                      : CTS
* Date                        : 05-FEB-2021
* Description                 : This file implements the logic to 
* 								1. Process EGC Shipment confirmation updates from WCS.
* 									once processed, EGC order will move to 'Shipped' and
* 									EGC shipment will move to 'Shipment Shipped'status.
*                               2. OMS processes updates only if acceptance criteria is met.
*                                    else, it will throw exception and update remain unprocessed.
*
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author                  Version#       Remarks/Description                     
* ---------------------------------------------------------------------------------
* 05-FEB-2021     CTS                      1.0            Initial version
* 14-JUL-2022     CTS                      2.0            Revised version (OMNI-78103)
* ##################################################################################*/

public class AcademyProcessEGCConfirmShipmentUpdates implements YIFCustomApi {

	private static final YFCLogCategory logger = YFCLogCategory.instance(AcademyProcessEGCConfirmShipmentUpdates.class);
	private Properties props;

	@Override
	public void setProperties(Properties props) throws Exception {

		this.props = props;

	}

	public Document processConfirmShipmentUpdates(YFSEnvironment env, Document docInp) throws Exception {

		logger.verbose("Input to AcademyProcessEGCConfirmShipmentUpdates.processConfirmShipmentUpdates() :: "
				+ XMLUtil.getXMLString(docInp));

		ArrayList<String> alAddnlSequences = new ArrayList<String>();
		ArrayList<String> alOrderLinekey = new ArrayList<String>();

		Element eleOrderWCS = docInp.getDocumentElement();

		String strOrderNoWCS = eleOrderWCS.getAttribute(AcademyConstants.ATTR_ORDER_NO);

		Document docShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);

		Document docShipmentOut = null;

		Document docGetOrderListOutput = prepareAndInvokeGetOrderList(env, strOrderNoWCS);

		if (!YFCObject.isNull(docGetOrderListOutput)) {

			Element eleOrderOMS = XmlUtils.getChildElement(docGetOrderListOutput.getDocumentElement(),
					AcademyConstants.ELE_ORDER);

			if (YFCObject.isNull(eleOrderOMS)) {

				YFSException yfsExec = new YFSException(AcademyConstants.ERR_CODE_28_MESSAGE);
				yfsExec.setErrorCode(AcademyConstants.ERR_CODE_28);
				yfsExec.setErrorDescription(AcademyConstants.ERR_CODE_28_MESSAGE);
				throw yfsExec;
			}

			String strOrderTypeOMS = eleOrderOMS.getAttribute(AcademyConstants.ATTR_ORDER_TYPE);
			String strOrderPurposeOMS = eleOrderOMS.getAttribute(AcademyConstants.STR_ORDER_PURPOSE);

			String strOrderTypeWCS = eleOrderWCS.getAttribute(AcademyConstants.ATTR_ORDER_TYPE);

			if (AcademyConstants.STR_ORDER_TYPE_SALES.equals(strOrderTypeWCS)) {

				if (AcademyConstants.STR_APPEASEMENT.equals(strOrderTypeOMS)
						|| AcademyConstants.STR_REFUND.equals(strOrderPurposeOMS)) {

					throwCustomException(AcademyConstants.ERR_CODE_27_DESC_ORDER_TYPE);
				}

			} else if (AcademyConstants.STR_ORDER_TYPE_REFUND.equals(strOrderTypeWCS)) {

				if (!(AcademyConstants.STR_APPEASEMENT.equals(strOrderTypeOMS)
						|| AcademyConstants.STR_REFUND.equals(strOrderPurposeOMS))) {

					throwCustomException(AcademyConstants.ERR_CODE_27_DESC_ORDER_TYPE);

				}
			}

			Element eleShipment = docShipment.getDocumentElement();

			eleShipment.setAttribute(AcademyConstants.ATTR_ORDER_NO,
					eleOrderOMS.getAttribute(AcademyConstants.ATTR_ORDER_NO));
			eleShipment.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
					eleOrderOMS.getAttribute(AcademyConstants.ATTR_DOC_TYPE));
			eleShipment.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,
					eleOrderOMS.getAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE));
			eleShipment.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
					eleOrderOMS.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));

			Element eleShipmentLines = XmlUtils.createChild(eleShipment, AcademyConstants.ELE_SHIPMENT_LINES);

			//getNodeList of Regular eGC Items
			NodeList nlOrderLineEGC = XPathUtil.getNodeList(docGetOrderListOutput,
					"/OrderList/Order/OrderLines/OrderLine[@FulfillmentType='EGC' and (@MaxLineStatus='1100' or @MaxLineStatus='1200')]/Extn[not(@ExtnIsPromoItem='Y')]");
			NodeList nlOrderLineWCS = docInp.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);

			if (!YFCObject.isNull(nlOrderLineWCS) && nlOrderLineWCS.getLength() > 0) {

				for (int i = 0; i < nlOrderLineWCS.getLength(); i++) {

					Element eleOrderLineWCS = (Element) nlOrderLineWCS.item(i);

					String strItemIdWCS = eleOrderLineWCS.getAttribute(AcademyConstants.ATTR_ITEM_ID);
					String strQuantityWCS = eleOrderLineWCS.getAttribute(AcademyConstants.ATTR_QUANTITY);
					String strWCOrderItemIdentifierWCS = eleOrderLineWCS
							.getAttribute(AcademyConstants.ATTR_EXTN_WC_ORDER_ITEM_IDENTIFIER);

					NodeList nlEGCSeqNo = eleOrderLineWCS.getElementsByTagName(AcademyConstants.ELE_EGC_SEQUENCE_NO);

					Element eleOrderLineOMS = null;

					if (AcademyConstants.STR_ORDER_TYPE_SALES.equals(strOrderTypeWCS)) {

						eleOrderLineOMS = (Element) XPathUtil.getNode(docGetOrderListOutput.getDocumentElement(),
								"/OrderList/Order/OrderLines/OrderLine[@FulfillmentType='EGC' and Extn/@ExtnWCOrderItemIdentifier='"
										+ strWCOrderItemIdentifierWCS + "']");

					} else if (AcademyConstants.STR_ORDER_TYPE_REFUND.equals(strOrderTypeWCS)) {

						eleOrderLineOMS = (Element) XPathUtil.getNode(docGetOrderListOutput.getDocumentElement(),
								"/OrderList/Order/OrderLines/OrderLine[@FulfillmentType='EGC' and Item/@ItemID='"
										+ strItemIdWCS + "']");
					}

					if (!YFCObject.isNull(eleOrderLineOMS)) {

						String strQuantityOMS = eleOrderLineOMS.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
						String strOrderLineKey = eleOrderLineOMS.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
						Element eleItemOMS = XmlUtils.getChildElement(eleOrderLineOMS, AcademyConstants.ELEM_ITEM);
						String strItemIdOMS = eleItemOMS.getAttribute(AcademyConstants.ATTR_ITEM_ID);
                       //Getting ExtnPromoItem from the Doc - Start
					   Element eleExtn = XmlUtils.getChildElement(eleOrderLineOMS, AcademyConstants.ELE_EXTN);
						String strExtnPromoItem = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_IS_PROMO_ITEM);
						//Getting ExtnPromoItem from the Doc - End
						eleOrderLineOMS.setAttribute(AcademyConstants.ATTR_ORDER_TYPE, strOrderTypeWCS);
						eleOrderLineOMS.setAttribute(AcademyConstants.ATTR_ORDER_NO,
								eleOrderOMS.getAttribute(AcademyConstants.ATTR_ORDER_NO));

						eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE,
								eleOrderLineOMS.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE));
						eleShipment.setAttribute(AcademyConstants.ATTR_SHIP_NODE,
								eleOrderLineOMS.getAttribute(AcademyConstants.ATTR_SHIP_NODE));

						if (AcademyConstants.STR_ORDER_TYPE_SALES.equals(strOrderTypeWCS)
								&& strItemIdWCS.equals(strItemIdOMS)
								&& Double.parseDouble(strQuantityWCS) == Double.parseDouble(strQuantityOMS)
								&& Double.parseDouble(strQuantityOMS) == nlEGCSeqNo.getLength()) {

							Element eleShipmentLine = prepareShipmentLineForConfirmShipment(eleOrderLineOMS,
									eleOrderLineWCS, alAddnlSequences);

							Element eleShipmentLineFinal = (Element) docShipment.importNode(eleShipmentLine, true);
							eleShipmentLines.appendChild(eleShipmentLineFinal);

						} else if (AcademyConstants.STR_ORDER_TYPE_REFUND.equals(strOrderTypeWCS)
								&& strItemIdWCS.equals(strItemIdOMS)
								&& Double.parseDouble(strQuantityWCS) == Double.parseDouble(strQuantityOMS)
								&& nlEGCSeqNo.getLength() > 0) {

							Element eleShipmentLine = prepareShipmentLineForConfirmShipment(eleOrderLineOMS,
									eleOrderLineWCS, alAddnlSequences);

							Element eleShipmentLineFinal = (Element) docShipment.importNode(eleShipmentLine, true);
							eleShipmentLines.appendChild(eleShipmentLineFinal);

						} else {

							throwCustomException(AcademyConstants.ERR_CODE_27_DESC_ORDLN_PARMS);

						}
                        //Adding the OrderLineKey if it is only an regular eGC
						if (!(AcademyConstants.ATTR_Y).equalsIgnoreCase(strExtnPromoItem)) {
						if (!alOrderLinekey.contains(strOrderLineKey)) {
							alOrderLinekey.add(strOrderLineKey);
						}
}
					} else {

						throwCustomException(AcademyConstants.ERR_CODE_27_DESC_ORDLN_PARMS);

					}

				}
               //Skip the condition for eGC Promo Item
			   int iOrderLineKey = alOrderLinekey.size();
				if ((iOrderLineKey != 0) && (iOrderLineKey != nlOrderLineEGC.getLength())) {
					throwCustomException(AcademyConstants.ERR_CODE_27_DESC_EGC_ORDLN_COUNT);
				}

			}

			logger.verbose("AcademyProcessEGCConfirmShipmentUpdates.processConfirmShipmentUpdates() - "
					+ "Input to confirmShipment API :: " + XMLUtil.getXMLString(docShipment));

			YFSEnvironment newEnv = createEnvironment(env);

			docShipmentOut = AcademyUtil.invokeService(newEnv,
					AcademyConstants.ACADEMY_CONFIRM_SHIPMENT_FOR_EGC_SERVICE, docShipment);

			logger.verbose("AcademyProcessEGCConfirmShipmentUpdates.processConfirmShipmentUpdates() - "
					+ "Output of confirmShipment API :: " + XMLUtil.getXMLString(docShipmentOut));

			if (alAddnlSequences.size() > 0) {
				prepareAndInvokeChangeShipment(env, docShipmentOut, alAddnlSequences);
			}

		}

		return docInp;
	}

	private Document prepareAndInvokeGetOrderList(YFSEnvironment env, String strOrderNo) throws Exception {
		logger.beginTimer("AcademyProcessEGCConfirmShipmentUpdates.prepareAndInvokeGetOrderList() :: ");

		Document docGetOrderListOutput = null;

		if (!StringUtil.isEmpty(strOrderNo)) {

			logger.verbose("AcademyProcessEGCConfirmShipmentUpdates.prepareAndInvokeChangeShipment() - "
					+ "Order Number :: " + strOrderNo);

			Document docGetOrderListInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			Element eleGetOrderListInput = docGetOrderListInput.getDocumentElement();
			eleGetOrderListInput.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);

			logger.verbose(
					"AcademyProcessEGCConfirmShipmentUpdates.prepareAndInvokeGetOrderList() - Input to getOrderList API :: "
							+ XMLUtil.getXMLString(docGetOrderListInput));

			docGetOrderListOutput = AcademyUtil.invokeService(env,
					AcademyConstants.ACADEMY_GETORDERLIST_FOR_EGC_SERVICE, docGetOrderListInput);

			logger.verbose(
					"AcademyProcessEGCConfirmShipmentUpdates.prepareAndInvokeGetOrderList() - Output of getOrderList API :: "
							+ XMLUtil.getXMLString(docGetOrderListOutput));

		}

		logger.endTimer("AcademyProcessEGCConfirmShipmentUpdates.prepareAndInvokeGetOrderList() ::");

		return docGetOrderListOutput;
	}

	private Element prepareShipmentLineForConfirmShipment(Element eleOrderLineOMS, Element eleOrderLineWCS,
			ArrayList<String> alAddnlSequences) throws Exception {

		logger.verbose("Input to AcademyProcessEGCConfirmShipmentUpdates.prepareShipmentLineForConfirmShipment() - "
				+ "Element OrderLine OMS :: " + XMLUtil.getElementXMLString(eleOrderLineOMS)
				+ " Element OrderLine WCS :: " + XMLUtil.getElementXMLString(eleOrderLineWCS) + " ArrayList :: "
				+ alAddnlSequences.toString());

		Document docShipmentLine = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT_LINE);
		Element eleShipmentLine = docShipmentLine.getDocumentElement();

		Element eleItemOMS = XmlUtils.getChildElement(eleOrderLineOMS, AcademyConstants.ELEM_ITEM);

		eleShipmentLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,
				eleOrderLineOMS.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
		eleShipmentLine.setAttribute(AcademyConstants.ATTR_PRIME_LINE_NO,
				eleOrderLineOMS.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO));
		eleShipmentLine.setAttribute(AcademyConstants.SUB_LINE_NO,
				eleOrderLineOMS.getAttribute(AcademyConstants.SUB_LINE_NO));
		eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY,
				eleOrderLineOMS.getAttribute(AcademyConstants.ATTR_ORDERED_QTY));
		eleShipmentLine.setAttribute(AcademyConstants.ATTR_ORDER_NO,
				eleOrderLineOMS.getAttribute(AcademyConstants.ATTR_ORDER_NO));
		eleShipmentLine.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
				eleOrderLineOMS.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));

		eleShipmentLine.setAttribute(AcademyConstants.ATTR_ITEM_ID,
				eleItemOMS.getAttribute(AcademyConstants.ATTR_ITEM_ID));
		eleShipmentLine.setAttribute(AcademyConstants.ATTR_PROD_CLASS,
				eleItemOMS.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
		eleShipmentLine.setAttribute(AcademyConstants.ATTR_UOM, eleItemOMS.getAttribute(AcademyConstants.ATTR_UOM));

		NodeList nlEGCSeqNo = eleOrderLineWCS.getElementsByTagName(AcademyConstants.ELE_EGC_SEQUENCE_NO);

		Element eleShpTagSerials = XmlUtils.createChild(eleShipmentLine, AcademyConstants.ELE_SHIPMENT_TAG_SERIALS);

		for (int i = 0; i < nlEGCSeqNo.getLength(); i++) {

			Element eleEGCSeqNo = (Element) nlEGCSeqNo.item(i);

			if ((i + 1) <= Double.parseDouble(eleOrderLineOMS.getAttribute(AcademyConstants.ATTR_ORDERED_QTY))) {

				Element eleShpTagSerial = XmlUtils.createChild(eleShpTagSerials,
						AcademyConstants.ELE_SHIPMENT_TAG_SERIAL);

				eleShpTagSerial.setAttribute(AcademyConstants.ATTR_QUANTITY, AcademyConstants.STR_ONE);
				eleShpTagSerial.setAttribute(AcademyConstants.ATTR_SERIAL_NO,
						eleEGCSeqNo.getAttribute(AcademyConstants.ATTR_SEQUENCE_NO));

			} else {

				alAddnlSequences.add(eleEGCSeqNo.getAttribute(AcademyConstants.ATTR_SEQUENCE_NO));
			}

		}

		logger.verbose("Input to AcademyProcessEGCConfirmShipmentUpdates.prepareShipmentLineForConfirmShipment() - "
				+ "Element ShipmentLine :: " + XMLUtil.getElementXMLString(eleShipmentLine));

		return eleShipmentLine;
	}

	private Document prepareAndInvokeChangeShipment(YFSEnvironment env, Document docShipmentOMS,
			ArrayList<String> alAddnlSequences) throws Exception {

		logger.verbose(
				"Input to AcademyProcessEGCConfirmShipmentUpdates.prepareAndInvokeChangeShipment() - " + "Shipment :: "
						+ XMLUtil.getXMLString(docShipmentOMS) + " ArrayList :: " + alAddnlSequences.toString());

		Document docChangeShipInp = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Document docChangeShipOut = null;

		Element eleShipmentOMS = docShipmentOMS.getDocumentElement();
		Element eleShipment = docChangeShipInp.getDocumentElement();

		eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
				eleShipmentOMS.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));

		eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE,
				eleShipmentOMS.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE));

		Element eleShipmentLines = XmlUtils.createChild(eleShipment, AcademyConstants.ELE_SHIPMENT_LINES);

		Element eleShipmentLinesOMS = XmlUtils.getChildElement(eleShipmentOMS, AcademyConstants.ELE_SHIPMENT_LINES);

		Element eleShipmentLineOMS = XmlUtils.getChildElement(eleShipmentLinesOMS, AcademyConstants.ELE_SHIPMENT_LINE);

		Element eleShipmentLine = XmlUtils.createChild(eleShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);
		eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY,
				eleShipmentLineOMS.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));

		Element eleShipTagSerials = XmlUtils.createChild(eleShipmentLine, AcademyConstants.ELE_SHIPMENT_TAG_SERIALS);

		for (String strSequence : alAddnlSequences) {

			Element eleShipTagSerial = XmlUtils.createChild(eleShipTagSerials,
					AcademyConstants.ELE_SHIPMENT_TAG_SERIAL);
			eleShipTagSerial.setAttribute(AcademyConstants.ATTR_QUANTITY, AcademyConstants.STR_ONE);
			eleShipTagSerial.setAttribute(AcademyConstants.ATTR_SERIAL_NO, strSequence);
			eleShipTagSerial.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY,
					eleShipmentLineOMS.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));

		}

		logger.verbose("AcademyProcessEGCConfirmShipmentUpdates.prepareAndInvokeChangeShipment() - "
				+ "Input to changeShipment :: " + XMLUtil.getXMLString(docChangeShipInp));

		AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipInp);

		return docChangeShipOut;
	}

	private YFSEnvironment createEnvironment(YFSEnvironment env) throws Exception {

		logger.verbose("Begin - AcademyProcessEGCConfirmShipmentUpdates.createEnvironment() :: ");

		YIFApi api = YIFClientFactory.getInstance().getLocalApi();
		Document doc = XMLUtil.createDocument(AcademyConstants.ELE_YFS_ENVIROMENT);
		Element elem = doc.getDocumentElement();
		elem.setAttribute(AcademyConstants.ATTR_USR_ID, env.getUserId());
		elem.setAttribute(AcademyConstants.ATTR_PROG_ID, env.getProgId());
		YFSEnvironment envNew = api.createEnvironment(doc);

		logger.verbose("End - AcademyProcessEGCConfirmShipmentUpdates.createEnvironment() :: ");
		return envNew;

	}

	private void throwCustomException(String strDescription) throws Exception {

		YFSException yfsExec = new YFSException(AcademyConstants.ERR_CODE_27_MESSAGE);
		yfsExec.setErrorCode(AcademyConstants.ERR_CODE_27);
		yfsExec.setErrorDescription(strDescription);
		throw yfsExec;

	}

}
