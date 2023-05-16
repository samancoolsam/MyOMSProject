package com.academy.ecommerce.sterling.dsv.invoice;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCLocale;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyDSVCreateInvoiceNo implements YIFCustomApi {
	String strInvoiceShipDate = null;

	String strInvoiceShipNode = null;

	String shipmentKey = "";

	YFCLocale yfcLocale = null;

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyDSVCreateInvoiceNo.class);

	Document docOutput = null;

	public Document createCustomInvNo(YFSEnvironment env, Document inDoc)
			throws Exception, ParserConfigurationException, SAXException,
			IOException, YIFClientCreationException, RemoteException {

		System.out
				.println("Input document AcademyDropShipCreateExtnInvoiceNo :: "
						+ XMLUtil.getXMLString(inDoc));
		System.out
				.println("AcademyGetInvoiceNoUEImpl : getInvoiceNo started :: Inside IF :: Y.equals(strCallInvoiceUEFromPrintFLow) (0003.equals(strDocumentType)) or  CREDIT_MEMO.equals(invoiceType)");
		log
				.beginTimer(" Begining of AcademyProrateForPackSlip-> callGetInvoiceNoUE Api");
		Element eleShipment = inDoc.getDocumentElement();
		Document docInvoiceDetail = XMLUtil.createDocument("InvoiceDetail");
		Element eleInvoiceHeader = XMLUtil.createElement(docInvoiceDetail,
				"InvoiceHeader", null);
		docInvoiceDetail.getDocumentElement().appendChild(eleInvoiceHeader);

		Element eleShipmentInvoiceUEInput = XMLUtil.createElement(
				docInvoiceDetail, "Shipment", null);
		eleInvoiceHeader.appendChild(eleShipmentInvoiceUEInput);
		eleShipmentInvoiceUEInput.setAttribute("ShipmentKey", eleShipment
				.getAttribute("ShipmentKey"));
		eleShipmentInvoiceUEInput.setAttribute("ShipDate", eleShipment
				.getAttribute("ShipDate"));
		eleShipmentInvoiceUEInput.setAttribute("ShipNode", eleShipment
				.getAttribute("ShipNode"));
		eleShipmentInvoiceUEInput.setAttribute("SCAC", eleShipment
				.getAttribute("SCAC"));
		eleShipmentInvoiceUEInput.setAttribute(
				"CallInvoiceUEFromDSVConfirmShipmentOnSuccess", "Y");

		log
				.verbose("********** CallInvoiceUEFromDSVConfirmShipmentOnSuccess set to Y");
		Document outDoc = AcademyUtil.invokeService(env,
				"AcademyInvokeGetInvoiceNo", docInvoiceDetail);
		log.verbose("********** output doc of AcademyInvokeGetInvoiceNo : "
				+ XMLUtil.getXMLString(outDoc));
		String strInvoiceNo = outDoc.getDocumentElement().getAttribute(
				"InvoiceNo");

		Document getOrderInvoiceListInput = XMLUtil
				.createDocument("OrderInvoice");
		getOrderInvoiceListInput.getDocumentElement().setAttribute(
				"ShipmentKey", eleShipment.getAttribute("ShipmentKey"));
		Document getOrderInvoiceListOutDoc = AcademyUtil.invokeAPI(env,
				"getOrderInvoiceList", getOrderInvoiceListInput);

		String strGetProformaOrderInvoiceKey = XMLUtil.getString(
				getOrderInvoiceListOutDoc.getDocumentElement(),
				"/OrderInvoiceList/OrderInvoice/@OrderInvoiceKey");
		Document docInput = XMLUtil.createDocument("OrderInvoice");
		docInput.getDocumentElement().setAttribute("OrderInvoiceKey",
				strGetProformaOrderInvoiceKey);
		Element eleExtn = XMLUtil.createElement(docInput, "Extn", null);
		docInput.getDocumentElement().appendChild(eleExtn);
		eleExtn.setAttribute("ExtnInvoiceNo", strInvoiceNo);
		Document outdoc = AcademyUtil.invokeAPI(env, "changeOrderInvoice",
				docInput);
		log.verbose("*********** output of change order Invoice api : "
				+ XMLUtil.getXMLString(outdoc));
		log
				.endTimer(" End of AcademyProrateForPackSlip-> callGetInvoiceNoUE Api");
		env.setTxnObject("ShipmentInvoiceNo", strInvoiceNo);
		return inDoc;

	}

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

}
