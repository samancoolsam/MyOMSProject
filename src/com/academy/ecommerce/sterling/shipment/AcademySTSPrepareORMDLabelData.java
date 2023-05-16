package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySTSPrepareORMDLabelData implements YIFCustomApi {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSPrepareORMDLabelData.class);

	public Document prepareORMDLabelData(YFSEnvironment env, Document docInp) {

		Document docPrintDocumentSetInp = null;

		log.verbose("Input - prepareORMDLabelData() :: " + XMLUtil.getXMLString(docInp));
		try {

			docPrintDocumentSetInp = prepareInputForPrintDocumentSet(env, docInp);

		} catch (Exception e) {
			log.error("Exception - prepareORMDLabelData() :: " + e);
		}
		return docPrintDocumentSetInp;
	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	private Document prepareInputForPrintDocumentSet(YFSEnvironment env, Document docRaiseEventInp) throws Exception {

		Document docPrintDocuments = null;

		Element elePrintDocuments = null;
		Element eleRaiseEventInp = null;
		Element eleShipment = null;

		String strPrinterId = (String) env.getTxnObject(AcademyConstants.STR_STS_ORMD_LABEL_PRINTER_ID);

		docPrintDocuments = XMLUtil.createDocument(AcademyConstants.ELE_PRINT_DOCUMENTS);
		elePrintDocuments = docPrintDocuments.getDocumentElement();

		elePrintDocuments.setAttribute(AcademyConstants.ATTR_FLUSH_TO_PRINTER, AcademyConstants.STR_YES);
		elePrintDocuments.setAttribute(AcademyConstants.ATTR_PRINT_NAME,
				AcademyConstants.STR_STS_ORMD_LABEL_PRINT_DOCUMENT_ID);

		Element elePrintDocument = docPrintDocuments.createElement(AcademyConstants.ELE_PRINT_DOCUMENT);
		elePrintDocuments.appendChild(elePrintDocument);

		elePrintDocument.setAttribute(AcademyConstants.ATTR_DOCUMENT_ID, AcademyConstants.STR_STS_ORMD_LABEL_PRINT_DOCUMENT_ID);
		elePrintDocument.setAttribute(AcademyConstants.ATTR_DATA_ELEMENT_PATH,
				AcademyConstants.STR_DATA_ELEMENT_CONTAINER);

		Element eleInputData = docPrintDocuments.createElement(AcademyConstants.ELE_INPUT_DATA);
		elePrintDocument.appendChild(eleInputData);

		eleRaiseEventInp = docRaiseEventInp.getDocumentElement();
		XMLUtil.importElement(eleInputData, eleRaiseEventInp);

		Element elePrinterPreference = docPrintDocuments.createElement(AcademyConstants.ELE_PRINT_PREFERENCE);
		elePrintDocument.appendChild(elePrinterPreference);

		eleShipment = (Element) docRaiseEventInp.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
		String strshipNode = eleShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE);

		elePrinterPreference.setAttribute(AcademyConstants.ORGANIZATION_CODE, strshipNode);
		elePrinterPreference.setAttribute(AcademyConstants.ATTR_PRINTER_ID, strPrinterId);
		elePrinterPreference.setAttribute(AcademyConstants.ATTR_WORKSTATION_ID,
				AcademyConstants.STR_STS_WORKSTATION_ID);

		Element eleLabelPreference = docPrintDocuments.createElement(AcademyConstants.ELE_LABEL_PREFERENCE);
		elePrintDocument.appendChild(eleLabelPreference);

		eleLabelPreference.setAttribute(AcademyConstants.ATTR_NODE, strshipNode);

		log.verbose("Input to API - PrintDocumentSet :: " + XMLUtil.getXMLString(docPrintDocuments));
		return docPrintDocuments;
	}
}
