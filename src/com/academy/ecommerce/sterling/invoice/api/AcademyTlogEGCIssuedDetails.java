package com.academy.ecommerce.sterling.invoice.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;


/*##################################################################################
*
* Project Name                : POD March Release
* Module                      : OMS
* Author                      : CTS
* Date                        : 05-FEB-2021
* Description                 : This file implements the logic to 
* 								1. Add EGC details to TLog.
* 									which includes, EGC Serial no associated with 
* 									shipment lines.
*                               2. IF associated shipment is not an EGC shipment
*                                    then, no data will be added to TLog.
*
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author                  Version#       Remarks/Description                     
* ---------------------------------------------------------------------------------
* 05-FEB-2021     CTS                      1.0            Initial version
* ##################################################################################*/

public class AcademyTlogEGCIssuedDetails implements YIFCustomApi {

	private static Logger logger = Logger.getLogger(AcademyTlogEGCIssuedDetails.class.getName());
	private Properties props;

	@Override
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	/**
	 * @param env
	 * @param inpDoc
	 * @return Document
	 * @throws Exception
	 * 
	 */
	public Document updateEGCDetails(YFSEnvironment env, Document inpDoc) throws Exception {

		boolean bAppendGCDtl = false;
		boolean bAppendContr = false;

		logger.verbose("input AcademyTlogEGCIssuedDetails.updateEGCDetails() :: " + XMLUtil.getXMLString(inpDoc));

		String strShipmentKey = XPathUtil.getString(inpDoc, AcademyConstants.XPATH_INVOICE_HEADER_SHIPMENT_KEY);
		String strShipmentNo = XPathUtil.getString(inpDoc, AcademyConstants.XPATH_INVOICE_HEADER_SHIPMENT_NO);

		logger.verbose("AcademyTlogEGCIssuedDetails.updateEGCDetails() - ShipmentKey ::" + strShipmentKey);

		Document giftCardIssued = XMLUtil.createDocument(AcademyConstants.ELE_GIFT_CARD_ISSUED);

		if (!StringUtil.isEmpty(strShipmentKey)) {

			Document docShipmentDetailsInp = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element eleShipmentDetailsInp = docShipmentDetailsInp.getDocumentElement();
			eleShipmentDetailsInp.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);

			logger.verbose("Shipment details input :: " + XMLUtil.getXMLString(docShipmentDetailsInp));

			Document docGetShipmentListOut = AcademyUtil.invokeService(env,
					AcademyConstants.ACADEMY_GET_SHIPMENT_LIST_FOR_EGC_SERVICE, docShipmentDetailsInp);

			if (!YFCObject.isNull(docGetShipmentListOut)) {

				logger.verbose("AcademyTlogEGCIssuedDetails.processTlog() getShipmentDetails OutputXML ::"
						+ XMLUtil.getXMLString(docGetShipmentListOut));

				NodeList nlShipmentLine = XPathUtil.getNodeList(docGetShipmentListOut,
						"/Shipments/Shipment[@ShipmentType='EGC']/ShipmentLines/ShipmentLine");

				if (!YFCObject.isNull(nlShipmentLine) && nlShipmentLine.getLength() > 0) {

					Element eleShipment = giftCardIssued.createElement(AcademyConstants.ELE_SHIPMENT);
					eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
					eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);

					giftCardIssued.getDocumentElement().appendChild(eleShipment);

					Element eleContrs = giftCardIssued.createElement(AcademyConstants.ELE_CONTAINERS);
					eleShipment.appendChild(eleContrs);

					Element eleContr = giftCardIssued.createElement(AcademyConstants.ELE_CONTAINER);
					eleContr.setAttribute(AcademyConstants.ATTR_TRACKING_NO, AcademyConstants.STR_EMPTY_STRING);
					eleContrs.appendChild(eleContr);

					Element eleContrDtls = giftCardIssued.createElement(AcademyConstants.ELE_CONTAINER_DTLS);
					eleContr.appendChild(eleContrDtls);

					for (int i = 0; i < nlShipmentLine.getLength(); i++) {

						Element eleShipmentLine = (Element) nlShipmentLine.item(i);

						Element eleContainerDetail = giftCardIssued
								.createElement(AcademyConstants.CONTAINER_DETL_ELEMENT);

						eleContainerDetail.setAttribute(AcademyConstants.ATTR_CONTAINER_DETAILS_KEY,
								AcademyConstants.STR_EMPTY_STRING);
						eleContainerDetail.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
								eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
						eleContainerDetail.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY,
								eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
						eleContainerDetail.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,
								eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
						eleContainerDetail.setAttribute(AcademyConstants.ATTR_ITEM_ID,
								eleShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID));

						NodeList nlShpTagSerial = eleShipmentLine
								.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_TAG_SERIAL);

						if (!YFCObject.isNull(nlShpTagSerial) && nlShpTagSerial.getLength() > 0) {

							Element eleShipTagSerls = giftCardIssued
									.createElement(AcademyConstants.ELE_SHIPMENT_TAG_SERIALS);
							eleContainerDetail.appendChild(eleShipTagSerls);

							eleShipTagSerls.setAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS,
									String.valueOf(nlShpTagSerial.getLength()));

							for (int j = 0; j < nlShpTagSerial.getLength(); j++) {

								Element eleShpTagSerial = (Element) nlShpTagSerial.item(j);

								Element eleShipTagSerl = giftCardIssued
										.createElement(AcademyConstants.ELE_SHIPMENT_TAG_SERIAL);

								if (!StringUtil
										.isEmpty(eleShpTagSerial.getAttribute(AcademyConstants.ATTR_SERIAL_NO))) {

									bAppendContr = true;

									bAppendGCDtl = true;

									eleShipTagSerl.setAttribute(AcademyConstants.ATTR_LOT_ATTRIBUTE_1,
											eleShpTagSerial.getAttribute(AcademyConstants.ATTR_LOT_ATTRIBUTE_1));
									eleShipTagSerl.setAttribute(AcademyConstants.ATTR_LOT_ATTRIBUTE_2,
											eleShpTagSerial.getAttribute(AcademyConstants.ATTR_LOT_ATTRIBUTE_2));
									eleShipTagSerl.setAttribute(AcademyConstants.ATTR_SERIAL_NO,
											eleShpTagSerial.getAttribute(AcademyConstants.ATTR_SERIAL_NO));
									eleShipTagSerls.appendChild(eleShipTagSerl);
								}

							}

							if (bAppendContr) {

								eleContrDtls.appendChild(eleContainerDetail);
							}
						}

						bAppendContr = false;

					}

				}

				if (bAppendGCDtl) {

					Element elamentFinalTlog = (Element) inpDoc.importNode(giftCardIssued.getDocumentElement(), true);
					inpDoc.getDocumentElement().appendChild(elamentFinalTlog);
				}

			}

		}

		logger.verbose("output AcademyTlogEGCIssuedDetails.processTlog() :: " + XMLUtil.getXMLString(inpDoc));

		return inpDoc;
	}

}
