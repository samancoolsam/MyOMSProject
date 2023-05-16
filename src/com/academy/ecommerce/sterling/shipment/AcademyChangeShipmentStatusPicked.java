package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyChangeShipmentStatusPicked implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyChangeShipmentStatusPicked.class);

	private Properties props;

	public void setProperties(Properties props) {
		this.props = props;
	}

	/**
	 * OMNI-46029 This method invokes changeShipmentStatus API based on DocumentType
	 */

	public Document changeShipmentStatusTO(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer("AcademyChangeShipmentStatusPicked.changeShipmentStatusTO()");
		log.verbose("Begining of AcademyChangeShipmentStatusPicked" + XMLUtil.getXMLString(inDoc));
		Document outDoc = null;
		try {
			/*
			 * <Shipment BaseDropStatus="1100.70.06.50"
			 * DisplayLocalizedFieldInLocale="xml:CurrentUser:/User/@Localecode"
			 * ShipNode="xml:CurrentStore:/Store/@ShipNode" ShipmentKey="" ShipmentNo=""
			 * TransactionId="YCD_BACKROOM_PICK.0006"/>
			 */

			Element inEle = inDoc.getDocumentElement();
			String strShipmentKey = inEle.getAttribute(AcademyConstants.SHIPMENT_KEY);
			Document strGetShipmentTemplate = XMLUtil
					.getDocument("<Shipments><Shipment ShipmentKey='' DocumentType='' /></Shipments>");
			Document outputTemplate = YFCDocument.getDocumentFor(strGetShipmentTemplate).getDocument();
			Document docGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			docGetShipmentList.getDocumentElement().setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
			env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, outputTemplate);
			Document outGetShipmentList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
					docGetShipmentList);
			env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
			Element eleShipment = XMLUtil.getElementByXPath(outGetShipmentList, "/Shipments/Shipment");
			String strDocType = eleShipment.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
			String strtransactionID = inEle.getAttribute(AcademyConstants.ATTR_TRANS_ID);
			log.verbose(" transaction ID " + strtransactionID);
			inEle.removeAttribute(AcademyConstants.ATTR_TRANS_ID);
			if (AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE.equalsIgnoreCase(strDocType)) {
				inEle.setAttribute(AcademyConstants.ATTR_TRANS_ID, AcademyConstants.TRAN_YCD_BACKROOM_PICK_0006);
			} else {
				inEle.setAttribute(AcademyConstants.ATTR_TRANS_ID, AcademyConstants.TRAN_YCD_BACKROOM_PICK);
			}
			outDoc = AcademyUtil.invokeAPI(env, AcademyConstants.CHANGE_SHIPMENT_STATUS_API, inDoc);
			log.verbose(" Output Document " + outDoc);

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		log.verbose("End of AcademyChangeShipmentStatusPicked");
		log.endTimer("AcademyChangeShipmentStatusPicked.changeShipmentStatusTO()");

		return outDoc;

	}

}
