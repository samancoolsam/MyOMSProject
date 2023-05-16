package com.academy.ecommerce.sterling.shipment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySTSPrepareShippingLabelData implements YIFCustomApi {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSPrepareShippingLabelData.class);

	public Document prepareShippingLabelData(YFSEnvironment env, Document docInp) {

		Document docPrintDocumentSetInp = null;
		Document docGetOrdListOut = null;
		Element eleOrderLine = null;

		String strChainedFromOrderHeaderKey = null;

		log.verbose("Input - prepareShippingLabelData() :: " + XMLUtil.getXMLString(docInp));
		try {

			eleOrderLine = (Element) docInp.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE).item(0);

			strChainedFromOrderHeaderKey = eleOrderLine
					.getAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_HEADER_KEY);

			if (!StringUtil.isEmpty(strChainedFromOrderHeaderKey)) {
				log.verbose("Chained From OHK :: " + strChainedFromOrderHeaderKey);

				docGetOrdListOut = invokeGetOrderList(env, strChainedFromOrderHeaderKey);

				docInp = setRequiredAttributes(env, docInp, docGetOrdListOut);

				docPrintDocumentSetInp = prepareInputForPrintDocumentSet(env, docInp);
			}

		} catch (Exception e) {
			log.error("Exception - prepareShippingLabelData() :: " + e);
		}
		return docPrintDocumentSetInp;
	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	private Document setRequiredAttributes(YFSEnvironment env, Document docRaiseEventInp, Document docGetOrderListOut) throws Exception {

		// split container no
		// stamp customer name
		Element eleContainer = null;
		Element eleOrderLine = null;
		Element eleOrderLineExtn = null;
		Element elePersonInfoShipTo = null;
		Element eleShipment = null;
		Element eleShipmentExtn = null;
		Element eleOrder = null;

		String strContainerNo = null;
		String strExtnPromiseDate = null;
		String strExtnStoreDeliveryDate = null;
		String strFirstName = null;
		String strLastName = null;
		String strCustomerName = null;
		String strStatusDate = null;
		String strSalesOrderNo = null;

		eleContainer = docRaiseEventInp.getDocumentElement();

		strContainerNo = eleContainer.getAttribute(AcademyConstants.ATTR_CONTAINER_NO);

		eleContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_NO_PART_1, strContainerNo.substring(0, 3));
		eleContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_NO_PART_2, strContainerNo.substring(3));

		strStatusDate = formatDateString(eleContainer.getAttribute(AcademyConstants.ATTR_STATUS_DATE));
		eleContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_PACK_DATE, strStatusDate);

		elePersonInfoShipTo = (Element) docGetOrderListOut
				.getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).item(0);

		if (null != elePersonInfoShipTo) {
			
			strFirstName = elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_FNAME);
			strLastName = elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_LNAME);

			if (!StringUtil.isEmpty(strFirstName) && !StringUtil.isEmpty(strLastName)) {

				strCustomerName = strLastName + AcademyConstants.STR_COMMA + strFirstName;
				
			} else if (!StringUtil.isEmpty(strFirstName) && StringUtil.isEmpty(strLastName)) {

				strCustomerName = AcademyConstants.STR_BLANK + AcademyConstants.STR_COMMA + strFirstName;

			} else if (StringUtil.isEmpty(strFirstName) && !StringUtil.isEmpty(strLastName)) {

				strCustomerName = strLastName + AcademyConstants.STR_COMMA + AcademyConstants.STR_BLANK;
			}
			
			if(!StringUtil.isEmpty(strCustomerName)) {
				eleContainer.setAttribute(AcademyConstants.ATTR_CUST_NAME, strCustomerName.trim());
			}
			

			if (!StringUtil.isEmpty(strFirstName)) {
				eleContainer.setAttribute(AcademyConstants.ATTR_FNAME, strFirstName + AcademyConstants.STR_COMMA);
			} else {
				eleContainer.setAttribute(AcademyConstants.ATTR_FNAME,
						AcademyConstants.STR_BLANK + AcademyConstants.STR_COMMA);
			}

			if (!StringUtil.isEmpty(strLastName)) {
				eleContainer.setAttribute(AcademyConstants.ATTR_LNAME, strLastName);
			} else {
				eleContainer.setAttribute(AcademyConstants.ATTR_LNAME, AcademyConstants.STR_BLANK);
			}

		}

		eleOrder = (Element) docGetOrderListOut.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
		if (null != eleOrder) {
			strSalesOrderNo = eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			if (!StringUtil.isEmpty(strSalesOrderNo)) {
				eleContainer.setAttribute(AcademyConstants.ATTR_SALES_ORDER_NO, strSalesOrderNo);
			}
		}

		eleOrderLine = (Element) docGetOrderListOut.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE).item(0);
		if (null != eleOrderLine) {
			eleOrderLineExtn = (Element) eleOrderLine.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
			if (null != eleOrderLineExtn) {

				strExtnPromiseDate = formatDateString(
						eleOrderLineExtn.getAttribute(AcademyConstants.ATTR_EXTN_INITIAL_PROMISE_DATE));

				strExtnStoreDeliveryDate = formatDateString(
						eleOrderLineExtn.getAttribute(AcademyConstants.ATTR_EXTN_STORE_DELIVERY_DATE));
			}
		}

		eleContainer.setAttribute(AcademyConstants.ATTR_PICKUP_DATE, strExtnPromiseDate);
		eleContainer.setAttribute(AcademyConstants.ATTR_STORE_DELIVERY_DATE, strExtnStoreDeliveryDate);

		eleShipment = (Element) eleContainer.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
		if (null != eleShipment) {

			eleShipmentExtn = (Element) eleShipment.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
			if (null != eleShipmentExtn) {

				String strExtnFirearm = eleShipmentExtn.getAttribute(AcademyConstants.ATTR_EXTN_IS_FIREARM);
				if (AcademyConstants.STR_YES.equalsIgnoreCase(strExtnFirearm)) {

					eleContainer.setAttribute(AcademyConstants.ATTR_FIREARMS_TEXT,
							AcademyConstants.STR_FIREARMS_TEXT_VALUE);
					eleContainer.setAttribute(AcademyConstants.ATTR_FIREARMS_VALUE, strExtnFirearm);
				}
				//OMNI-65683 STS Label changes for Sorter, Door, Lane - Begin
				String strLane = eleShipmentExtn.getAttribute(AcademyConstants.ATTR_EXTN_LANE);
				if (!YFCObject.isVoid(strLane)) {
					String strStoreNode = eleShipment.getAttribute(AcademyConstants.ATTR_RECV_NODE);
					Document docSorterDoorCommonCode = getCommonCodeList(env, AcademyConstants.STS_SORTER_DOOR_MAP,
							strStoreNode, "FLIKE");
					NodeList nlCommonCode = docSorterDoorCommonCode
							.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
					Element eleCommonCode = null;
					String strCodeValue = "";
					String strCodeShortDesc = "";
					String strSorter = "";
					String strDoor = "";

					for (int i = 0; i < nlCommonCode.getLength(); i++) {
						eleCommonCode = (Element) nlCommonCode.item(i);
						strCodeValue = eleCommonCode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
						// CodeValue="Store_Sorter" CodeShortDescription="Lane_Door"
						strCodeShortDesc = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
						if (!YFCCommon.isVoid(strCodeValue) && !YFCCommon.isVoid(strCodeShortDesc)
								&& strCodeValue.startsWith(strStoreNode) && strCodeShortDesc.startsWith(strLane)) {
							strSorter = strCodeValue.substring(strCodeValue.indexOf("_") + 1);
							strDoor = strCodeShortDesc.substring(strCodeShortDesc.indexOf("_") + 1);

							eleShipmentExtn.setAttribute(AcademyConstants.ATTR_EXTN_SORTER, strSorter);
							eleShipmentExtn.setAttribute(AcademyConstants.ATTR_EXTN_DOOR, strDoor);
						}
					}
					log.verbose(
							"Shipment Extn with Sorter, Door, Lane: \n" + XMLUtil.getElementXMLString(eleShipmentExtn));
				}
				//OMNI-65683 STS Label changes for Sorter, Door, Lane - End
			}
		}

		return docRaiseEventInp;
	}

	private Document prepareInputForPrintDocumentSet(YFSEnvironment env, Document docRaiseEventInp) throws Exception {

		Document docPrintDocuments = null;

		Element elePrintDocuments = null;
		Element eleRaiseEventInp = null;
		Element eleShipment = null;
		
		String strPrinterId = (String) env.getTxnObject(AcademyConstants.STR_STS_SHIPPING_LABEL_PRINTER_ID);
				
		docPrintDocuments = XMLUtil.createDocument(AcademyConstants.ELE_PRINT_DOCUMENTS);
		elePrintDocuments = docPrintDocuments.getDocumentElement();

		elePrintDocuments.setAttribute(AcademyConstants.ATTR_FLUSH_TO_PRINTER, AcademyConstants.STR_YES);
		elePrintDocuments.setAttribute(AcademyConstants.ATTR_PRINT_NAME, AcademyConstants.STR_STS_PRINT_NAME);

		Element elePrintDocument = docPrintDocuments.createElement(AcademyConstants.ELE_PRINT_DOCUMENT);
		elePrintDocuments.appendChild(elePrintDocument);

		elePrintDocument.setAttribute(AcademyConstants.ATTR_DOCUMENT_ID, AcademyConstants.STR_STS_PRINT_DOCUMENT_ID);
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

	private Document invokeGetOrderList(YFSEnvironment env, String strChainedFromOrderHeaderKey) throws Exception {

		log.verbose("Begin - invokeGetOrderList() :: ");

		Document docGetOrdListInp = null;
		Document docGetOrdListOut = null;

		docGetOrdListInp = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		docGetOrdListInp.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
				strChainedFromOrderHeaderKey);
		log.verbose("input - getOrderList :: " + XMLUtil.getXMLString(docGetOrdListInp));

		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST,
				AcademyConstants.STR_TEMPLATEFILE_GET_ORDER_LIST_STS_LABEL);
		docGetOrdListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, docGetOrdListInp);
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);

		log.verbose("output - getOrderList :: " + XMLUtil.getXMLString(docGetOrdListOut));

		log.verbose("Begin - invokeGetOrderList() :: ");

		return docGetOrdListOut;
	}

	private String formatDateString(String strDate) throws Exception {

		log.verbose("Begin - formatDateString() :: ");

		String strFormattedDate = AcademyConstants.STR_BLANK;

		if (!StringUtil.isEmpty(strDate)) {

			log.verbose("input strDate ::" + strDate);
			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			SimpleDateFormat sdfSTS = new SimpleDateFormat(AcademyConstants.STR_STS_SHIPPING_LABEL_DATE_FORMAT);

			Date objDate = sdf.parse(strDate);
			strFormattedDate = sdfSTS.format(objDate);

			log.verbose("FormatDateString :: " + strFormattedDate);
		}
		log.verbose("End - formatDateString() :: ");
		return strFormattedDate;
	}
	
	private Document getCommonCodeList(YFSEnvironment env,String strCodeType,String strCodeValue, String strCodeValueQryType) throws Exception 
	{
		log.verbose("Entering AcademyBOPISUtil.getCommonCodeList() :: strCodeType - "+strCodeType);
		
		Element eleInGetCommonCodeList = null;
		Document docInGetCommonCodeList = null;
		Document docOutGetCommonCodeList = null;
		Document templateGetCommonCodeListAPI = null;

		docInGetCommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		eleInGetCommonCodeList = docInGetCommonCodeList.getDocumentElement();
		eleInGetCommonCodeList.setAttribute(AcademyConstants.ATTR_CODE_TYPE, strCodeType);
		eleInGetCommonCodeList.setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE, strCodeValue);
		eleInGetCommonCodeList.setAttribute("CodeValueQryType" , strCodeValueQryType);
		
		templateGetCommonCodeListAPI = YFCDocument.getDocumentFor("<CommonCode CodeValue=\"\" CodeLongDescription=\"\" CodeShortDescription=\"\" />").getDocument();

		log.verbose("Input to getCommonCodeList : "+XMLUtil.getXMLString(docInGetCommonCodeList));
		env.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST, templateGetCommonCodeListAPI);
		docOutGetCommonCodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST, docInGetCommonCodeList);
		env.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);
		log.verbose("Output from getCommonCodeList : "+XMLUtil.getXMLString(docOutGetCommonCodeList));
		
		log.verbose("Exiting AcademyBOPISUtil.getCommonCodeList()");
		
		return docOutGetCommonCodeList;
	}

}