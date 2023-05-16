package com.academy.ecommerce.order.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSCheckOrderBeforeProcessingUE;

/**
 * Added to implement the requirement of OMNI-66584 UE implemented to enable the
 * STS order to get release, pick and stage regardless of payment status
 *
 */
public class AcademyCheckOrderBeforeProcessingUEImpl implements YFSCheckOrderBeforeProcessingUE {

	/**
	 * Instance of logger
	 * 
	 */

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyCheckOrderBeforeProcessingUEImpl.class);

	/**
	 * Method check if order has STS line to release then it will let the order to
	 * release regardless of payment status
	 */
	public boolean checkOrderBeforeProcessing(YFSEnvironment yfsEnv, Document docInput) throws YFSUserExitException {
		log.beginTimer("Begining of AcademyCheckOrderBeforeProcessingUEImpl -> checkOrderBeforeProcessing");
		log.verbose("AcademyCheckOrderBeforeProcessingUEImpl : checkOrderBeforeProcessing() : START "
				+ XMLUtil.getXMLString(docInput));
		Element eleOrder = docInput.getDocumentElement();
		String strTranID = eleOrder.getAttribute(AcademyConstants.ATTR_TRANS_ID);
		String strPaymentStatus = eleOrder.getAttribute(AcademyConstants.ATTR_PAYMENT_STATUS);
		String strDocType = eleOrder.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
		String strProgID = yfsEnv.getProgId();
		String strExtnIsPromoItem = "N";
		String isPaymentStatusPaid="N";
		log.verbose("strProgId : " + strProgID);

		if (AcademyConstants.STR_AUTHORIZED.equalsIgnoreCase(strPaymentStatus))
			return true;
		

		Element eleOrderLines = SCXmlUtil.getChildElement(eleOrder, AcademyConstants.ELE_ORDER_LINES);
		NodeList nlOrderLines = eleOrderLines.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);

		for (int i = 0; i < nlOrderLines.getLength(); i++) {
			Element eleOrderLine = (Element) nlOrderLines.item(i);
			String strFulfillmentType = eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
			String strMinLineStatus = eleOrderLine.getAttribute(AcademyConstants.ATTR_MIN_LINE_STATUS);
			if (AcademyConstants.SALES_DOCUMENT_TYPE.equalsIgnoreCase(strDocType)) {
				if (AcademyConstants.V_FULFILLMENT_TYPE_STS.equalsIgnoreCase(strFulfillmentType)
						&& AcademyConstants.STR_RELEASE_TRAN_KEY.equalsIgnoreCase(strTranID)
						&& AcademyConstants.V_STATUS_2160_70_06_10.equalsIgnoreCase(strMinLineStatus)
						&& AcademyConstants.SERVER_STS_RELEASE_PROCESSING.equalsIgnoreCase(strProgID)) {

					log.verbose("AcademyCheckOrderBeforeProcessingUEImpl: Order Line is STS so returning true ");
					return true;

				}
			} else {
				log.verbose("returning true as not sales order");
						return true;
					}
				}
		//OMNI-94829 starts
		if (AcademyConstants.STR_PAID.equalsIgnoreCase(strPaymentStatus)) {
			try {
				NodeList nl = XPathUtil.getNodeList(docInput,
						AcademyConstants.STR_MIN_LINE_STATUS_3200+AcademyConstants.STR_EXTN_PROMO_ITEM_Y);
				if (nl.getLength() > 0)
				return true;
			} catch (Exception e) {
				throw new YFSException(e.getMessage());
			}

		}
				//OMNI-94829 ends
		log.verbose("AcademyCheckOrderBeforeProcessingUEImpl : checkOrderBeforeProcessing() : END");
		log.beginTimer("End of AcademyCheckOrderBeforeProcessingUEImpl -> checkOrderBeforeProcessing");
		return false;
	}

}