package com.academy.ecommerce.sterling.bopis.api;

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyStampInvoiceNoOnBOPISOrders implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyStampInvoiceNoOnBOPISOrders.class);

	public Document stampInvoiceNo(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer("start of setProFormaInvoiceNo :");
		log.verbose("input to setProFormaInvoiceNo :" + XMLUtil.getXMLString(inDoc));
		String strSHPKey = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		// input to getShipemntDetails
		Document inDocGetShipList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		inDocGetShipList.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strSHPKey);

		// template getShipmentDetails
		Document tempDocGetShipDetails = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENTS);
		Element eleShipment = SCXmlUtil.createChild(tempDocGetShipDetails.getDocumentElement(),
				AcademyConstants.ELE_SHIPMENT);
		eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, "");
		eleShipment.setAttribute(AcademyConstants.ATTR_SHIP_DATE, "");
		eleShipment.setAttribute(AcademyConstants.ATTR_SHIP_NODE, "");
		eleShipment.setAttribute(AcademyConstants.ATTR_SCAC, "");
		eleShipment.setAttribute(AcademyConstants.ATTR_DEL_METHOD, "");
		Element eleShipExtn = SCXmlUtil.createChild(eleShipment, AcademyConstants.ELE_EXTN);
		eleShipExtn.setAttribute(AcademyConstants.ATTR_EXTN_INVOICE_NO, "");
		Element eleOrderInvoiceList = SCXmlUtil.createChild(eleShipment, AcademyConstants.ELE_ORDER_INVOICE_LIST);
		Element eleOrderInvoice = SCXmlUtil.createChild(eleOrderInvoiceList, AcademyConstants.ELE_ORDER_INVOICE);
		Element eleInvoiceExtn = SCXmlUtil.createChild(eleOrderInvoice, AcademyConstants.ELE_EXTN);
		eleInvoiceExtn.setAttribute(AcademyConstants.ATTR_EXTN_INVOICE_NO, "");
	
		
		log.verbose("template to getShipmentList :" + XMLUtil.getXMLString(tempDocGetShipDetails));
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, tempDocGetShipDetails);

		log.verbose("input to getShipmentList :" + XMLUtil.getXMLString(inDocGetShipList));

		// invoking getShipmentList
		Document outDocGetShipDetails = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
				inDocGetShipList);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		log.verbose("outdoc getShipmentList :" + XMLUtil.getXMLString(outDocGetShipDetails));

		Element eleShipOut = SCXmlUtil.getChildElement(outDocGetShipDetails.getDocumentElement(),
				AcademyConstants.ELE_SHIPMENT);

		String strExtnInvoiceNo = XMLUtil.getString(eleShipOut, "OrderInvoiceList/OrderInvoice/Extn/@ExtnInvoiceNo");
		String strShipExtnInvoiceNo = XMLUtil.getString(eleShipOut, "Extn/@ExtnInvoiceNo");

		if (YFCCommon.isVoid(strExtnInvoiceNo)) {
			if (YFCCommon.isVoid(strShipExtnInvoiceNo)) {
				log.verbose(
						"Both Ship ExtnInvoiceNo and OrderInvoice ExtnInvoiceNo are empty : calling callGetInvoiceNoUE to stamp ExtnInvoiceNo :");
				Document outdocc = callGetInvoiceNoUE(env, inDocGetShipList, eleShipOut);
			} else {
				log.verbose("Ship ExtnInvoiceNo is :" + strShipExtnInvoiceNo
						+ "Same ship ExtnInvoiceNo will be stamped as OrderInvoice ExtnInvoiceNo");
				stampShipInvoiceNoToOrderInvoice(env, strShipExtnInvoiceNo, eleShipOut);
			}
		}

		log.verbose("output of stampInvoiceNo" + XMLUtil.getXMLString(inDoc));

		return inDoc;

	}

	private void stampShipInvoiceNoToOrderInvoice(YFSEnvironment env, String strShipExtnInvoiceNo, Element eleShipOut)
			throws Exception {

		String strGetProformaOrderInvoiceKey = XMLUtil.getString(eleShipOut,
				"OrderInvoiceList/OrderInvoice/@OrderInvoiceKey");
		Document docInput = XMLUtil.createDocument("OrderInvoice");
		docInput.getDocumentElement().setAttribute("OrderInvoiceKey", strGetProformaOrderInvoiceKey);
		Element eleExtn = XMLUtil.createElement(docInput, "Extn", null);
		docInput.getDocumentElement().appendChild(eleExtn);
		eleExtn.setAttribute("ExtnInvoiceNo", strShipExtnInvoiceNo);
		Document outdoc = AcademyUtil.invokeAPI(env, "changeOrderInvoice", docInput);
		log.verbose("*********** output of change order Invoice api : " + XMLUtil.getXMLString(outdoc));

	}

	private Document callGetInvoiceNoUE(YFSEnvironment env, Document inDoc, Element eleShipment) throws Exception {
		log.beginTimer(" Begining of AcademyProrateForPackSlip-> callGetInvoiceNoUE Api");

		Document docInvoiceDetail = XMLUtil.createDocument("InvoiceDetail");
		Element eleInvoiceHeader = XMLUtil.createElement(docInvoiceDetail, "InvoiceHeader", null);
		docInvoiceDetail.getDocumentElement().appendChild(eleInvoiceHeader);

		Element eleShipment1 = XMLUtil.createElement(docInvoiceDetail, "Shipment", null);
		eleInvoiceHeader.appendChild(eleShipment1);
		eleShipment1.setAttribute("ShipmentKey", eleShipment.getAttribute("ShipmentKey"));
		eleShipment1.setAttribute("ShipDate", eleShipment.getAttribute("ShipDate"));
		eleShipment1.setAttribute("ShipNode", eleShipment.getAttribute("ShipNode"));
		eleShipment1.setAttribute("SCAC", eleShipment.getAttribute("SCAC"));
		eleShipment1.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, eleShipment.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD));
		eleShipment1.setAttribute("CallInvoiceUEFromPrintFLow", "Y");

		log.verbose("********** CallInvoiceUEFromPrintFLow set to Y");
		Document outDoc = AcademyUtil.invokeService(env, "AcademyInvokeGetInvoiceNo", docInvoiceDetail);
		log.verbose("********** output doc of AcademyInvokeGetInvoiceNo : " + XMLUtil.getXMLString(outDoc));

		String strInvoiceNo = outDoc.getDocumentElement().getAttribute("InvoiceNo");
		log.verbose("********** invoice number : " + strInvoiceNo);
		String strGetProformaOrderInvoiceKey = XMLUtil.getString(eleShipment,
				"OrderInvoiceList/OrderInvoice/@OrderInvoiceKey");
		Document docInput = XMLUtil.createDocument("OrderInvoice");
		docInput.getDocumentElement().setAttribute("OrderInvoiceKey", strGetProformaOrderInvoiceKey);
		Element eleExtn = XMLUtil.createElement(docInput, "Extn", null);
		docInput.getDocumentElement().appendChild(eleExtn);
		eleExtn.setAttribute("ExtnInvoiceNo", strInvoiceNo);
		Document outdoc = AcademyUtil.invokeAPI(env, "changeOrderInvoice", docInput);
		log.verbose("*********** output of change order Invoice api : " + XMLUtil.getXMLString(outdoc));
		log.endTimer(" End of AcademyProrateForPackSlip-> callGetInvoiceNoUE Api");		
		return inDoc;

	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

}
