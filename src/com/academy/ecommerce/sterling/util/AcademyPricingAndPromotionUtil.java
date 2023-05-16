package com.academy.ecommerce.sterling.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.ResourceUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyPricingAndPromotionUtil {
	
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyPricingAndPromotionUtil.class);
	
	public static Document createGetWCOrderRequest(String orderNo) {
		try {
			log.verbose("Inside createGetWCOrderRequest with orderNo " + orderNo);
			Document getWCOrderReq = AcademyServiceUtil.getSOAPMsgTemplateForWCGetOrder();
			getWCOrderReq = AcademyPricingAndPromotionUtil.setUserCredentialsForWCRequest(getWCOrderReq);
			Element docEle = getWCOrderReq.getDocumentElement();
			Element orderNoEle = (Element)docEle.getElementsByTagName("Oagis9:Expression").item(0);
			String orderXpath = orderNoEle.getTextContent();
			orderXpath = orderXpath.replaceFirst("UniqueID=''", "UniqueID='" + orderNo + "'");
			orderNoEle.setTextContent(orderXpath);
			log.verbose("Inside createGetWCOrderRequest with return doc " + XMLUtil.getXMLString(getWCOrderReq));
			return getWCOrderReq;
		} catch (Exception e) {
			log.verbose("Inside createGetWCOrderRequest with exception " + e.getMessage());
			throw new YFSException(e.getMessage());
		}
	}
	
	public static Document createCheckOutWCOrderRequest(String orderNo) {
		try {
			log.verbose("Inside createCheckOutWCOrderRequest with orderNo " + orderNo);
			Document getWCOrderReq = AcademyServiceUtil.getSOAPMsgTemplateForWCProcessOrder();
			getWCOrderReq = AcademyPricingAndPromotionUtil.setUserCredentialsForWCRequest(getWCOrderReq);
			Element docEle = getWCOrderReq.getDocumentElement();
			docEle.getElementsByTagName("_wcf:UniqueID").item(0).setTextContent(orderNo);
			log.verbose("Inside createCheckOutWCOrderRequest with return doc " + XMLUtil.getXMLString(getWCOrderReq));
			return getWCOrderReq;
		} catch (Exception e) {
			log.verbose("Inside createCheckOutWCOrderRequest with exception " + e.getMessage());
			throw new YFSException(e.getMessage());
		}
	}
	
	public static boolean hasError(Document inDoc) throws YFSException {
		hasSoapFault(inDoc);
		NodeList errorResponse = inDoc.getDocumentElement().getElementsByTagName("Oagis9:Respond");
		if(errorResponse.getLength() > 0) {
			YFSException yfsException = new YFSException();
			yfsException.setErrorCode(inDoc.getDocumentElement().getElementsByTagName("Oagis9:ReasonCode").item(0).getTextContent());
			yfsException.setErrorDescription(inDoc.getDocumentElement().getElementsByTagName("Oagis9:Description").item(0).getTextContent());
			throw yfsException;
		}
		return false;
	}
	
	public static YFSException wrapToYFSException(Exception e) {
		YFSException yfsException = new YFSException();
		yfsException.setErrorCode(e.getMessage());
		yfsException.setErrorDescription(e.getMessage());
		yfsException.setStackTrace(e.getStackTrace());
		return yfsException;
	}
	
	public static Document setUserCredentialsForWCRequest(Document inDoc) {
		Element docEle = inDoc.getDocumentElement();
		docEle.getElementsByTagName("wsse:Username").item(0).setTextContent(ResourceUtil.get("WC_PRICE_USER_NAME"));
		docEle.getElementsByTagName("wsse:Password").item(0).setTextContent(ResourceUtil.get("WC_PRICE_USER_PASSWORD"));
		docEle.getElementsByTagName("_wcf:ContextData").item(0).setTextContent(ResourceUtil.get("WC_PRICE_STORE_ID"));
		return inDoc;
	} 
	
	public static boolean hasSoapFault(Document inDoc) throws YFSException {
		if(inDoc.getDocumentElement().getNodeName().equals("soapenv:Fault")) {
			Node faultCode = inDoc.getDocumentElement().getElementsByTagName("faultcode").item(0);
			Node faultDescription = inDoc.getDocumentElement().getElementsByTagName("faultstring").item(0);
			YFSException yfsException = new YFSException();
			yfsException.setErrorCode(faultCode.getTextContent());
			yfsException.setErrorDescription(faultDescription.getTextContent());
			throw yfsException;
		}
		return false;
	}
	
	
	public static String[] getScacCodeForShipModeId(YFSEnvironment env, String shipModeId) 
		throws YFSException {
		try {
			Document docCommonCodeListOutput = null;
			Document docScacCodeInput = null;
			String[] scacAndServiceCode = new String[] {"", ""};
			docScacCodeInput = XMLUtil
					.createDocument(AcademyConstants.ELE_COMMON_CODE);
			docScacCodeInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_CODE_TYPE, "WC_SCAC_CODE");
			docScacCodeInput.getDocumentElement().setAttribute(
					"OrganizationCode", AcademyConstants.PRIMARY_ENTERPRISE);
			docCommonCodeListOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_COMMON_CODELIST,
					docScacCodeInput);
			if(!YFCObject.isVoid(docCommonCodeListOutput)) {
				scacAndServiceCode[0] = XPathUtil.getString(docCommonCodeListOutput, 
				"CommonCodeList/CommonCode[@CodeValue='" + shipModeId + "']/@CodeShortDescription");
				scacAndServiceCode[1] = XPathUtil.getString(docCommonCodeListOutput, 
						"CommonCodeList/CommonCode[@CodeValue='" + shipModeId + "']/@CodeLongDescription");
			}
			return scacAndServiceCode;	
		} catch (Exception e) {
			throw wrapToYFSException(e);
		}
		
	}
	
	
	/**
	 * Gets Item detail from the Sterling System.
	 * @param env
	 * @param itemID
	 * @return
	 * @throws YFSException
	 */
	public static Document getItemDetailFromSterling(YFSEnvironment env, String itemID) throws YFSException {
		try {
			log.verbose("Inside getItemDetailFromSterling () with itemID " + itemID);
			YFCDocument getItemListInDoc = YFCDocument.createDocument("Item");
			getItemListInDoc.getDocumentElement().setAttribute("ItemID", itemID);
			getItemListInDoc.getDocumentElement().setAttribute("OrganizationCode", "DEFAULT");
			YFCDocument getItemListTemplate = YFCDocument.getDocumentFor(
					"<ItemList><Item ItemID='' ItemKey='' OrganizationCode='' UnitOfMeasure=''>" +
					"<PrimaryInformation DefaultProductClass=''/></Item></ItemList>");
			env.setApiTemplate("getItemList", getItemListTemplate.getDocument());
			Document getItemDocOutput = AcademyUtil.invokeAPI(env, "getItemList", getItemListInDoc.getDocument());
			env.clearApiTemplate("getItemList");
			return getItemDocOutput;
		} catch (Exception e) {
			log.verbose("Exception inside getItemChieldItems " + e.getMessage());
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		}
	}
	
	public static String getAbsValue(String value) {
		try {
			return String.valueOf(Math.abs(Double.parseDouble(value)));
		} catch (Exception e) {
			return "0.00";
		}
	}
}