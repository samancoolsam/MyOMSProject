package com.academy.ecommerce.sterling.dsv.inventory;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @267159 Created on 03/09/2012 This class is to validate all the mandatory
 *         attributes in the Trickle Feed msg coming from IP.
 * 
 */
public class AcademyValidateTrickleFeedXML implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyValidateTrickleFeedXML.class);

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	public Document validateTrickleFeedXML(YFSEnvironment env,
			Document trickleFeedInpDoc) {
		String errorMsg = "";
		String strApplyDiff = "";
		String strCompInvFlag = "";
		Element eleInputTrickleFeedMsg = null;
		// start validating
		errorMsg = ValidateXML(env, trickleFeedInpDoc);
		log.verbose("error mess received is" + errorMsg);

		if (!isEmptyOrNull(errorMsg)) {

			YFSException excep = new YFSException(errorMsg);
			excep.setErrorCode("Invalid Inforamtion");
			excep.setErrorDescription("Mandatory Feilds are missing");
			throw excep;

		} else
			// get ApplyDifferences from input

			eleInputTrickleFeedMsg = trickleFeedInpDoc.getDocumentElement();
		strApplyDiff = eleInputTrickleFeedMsg
				.getAttribute(AcademyConstants.ATTR_APPLY_DIFF);

		// get CompleteInventoryFlag from input
		Element eleShipNode = (Element) eleInputTrickleFeedMsg
				.getElementsByTagName(AcademyConstants.SHIP_NODE).item(0);
		strCompInvFlag = eleShipNode
				.getAttribute(AcademyConstants.ATTR_COMP_INV_FLAG);

		// if ApplyDifferences attribute is not there in the input or if it is
		// empty then set it and process
		if (YFCCommon.isVoid(strApplyDiff)) {

			eleInputTrickleFeedMsg.setAttribute(
					AcademyConstants.ATTR_APPLY_DIFF, "Y");
		}
		// if CompleteInventoryFlag attribute is not there in the input or if it
		// is empty then set it and process
		if (YFCCommon.isVoid(strCompInvFlag)) {

			eleInputTrickleFeedMsg.setAttribute(
					AcademyConstants.ATTR_COMP_INV_FLAG, "N");
		}

		log.verbose("Document is validated and looks fine.......");
		return trickleFeedInpDoc;
	}

	public String ValidateXML(YFSEnvironment env, Document inputDoc) {
		String strErrorMsg = "";
		Element eleinputDoc = null;
		String strShipNode = "";
		String strTimestamp = "";
		String strItemID = "";
		String strQty = "";
		eleinputDoc = inputDoc.getDocumentElement();

		log.verbose("input doc in ValidateXML method"
				+ XMLUtil.getXMLString(inputDoc));
		// ShipNode validation

		Element eleShipnode = (Element) eleinputDoc.getElementsByTagName(
				AcademyConstants.SHIP_NODE).item(0);
		strShipNode = eleShipnode.getAttribute(AcademyConstants.SHIP_NODE);
		if (isEmptyOrNull(strShipNode)) {
			strErrorMsg = strErrorMsg
					+ "ShipNode Attribute is missing or ShipNode is Empty";
		}

		// Timestamp validation
		strTimestamp = eleinputDoc
				.getAttribute(AcademyConstants.VENDOR_TIMESTAMP);
		if (isEmptyOrNull(strTimestamp)) {
			strErrorMsg = strErrorMsg
					+ "Timestamp Attribute is missing or Timestamp is Empty";
		}

		// ItemID validation
		NodeList itemNL = eleShipnode
				.getElementsByTagName(AcademyConstants.ITEM);
		for (int iCounter = 0; iCounter < itemNL.getLength(); iCounter++) {
			Element eleItem = (Element) itemNL.item(iCounter);
			// System.out.println("item
			// element"+XMLUtil.getElementXMLString(eleItem.g));
			strItemID = eleItem.getAttribute(AcademyConstants.ITEM_ID);

			if (isEmptyOrNull(strItemID)) {
				strErrorMsg = strErrorMsg
						+ "ItemID's missing or ItemID is Empty";
			}

			// Quantity validation
			Element eleSupplyDtls = (Element) eleItem.getElementsByTagName(
					AcademyConstants.SUPPLY_DETAILS).item(0);
			strQty = eleSupplyDtls.getAttribute(AcademyConstants.ATTR_QUANTITY);
			if (isEmptyOrNull(strQty)) {
				strErrorMsg = strErrorMsg
						+ "Quantity is missing or it is Empty";
			}

		}

		log.verbose("error msg returned is" + strErrorMsg);
		return strErrorMsg;
	}

	private static boolean isEmptyOrNull(final String argS) {
		if (null == argS) {
			return true;
		}
		for (int ln = 0; ln < argS.length()
				&& !Character.isWhitespace(argS.charAt(ln)); ln++) {
			return false;
		}
		return true;
	}
}
