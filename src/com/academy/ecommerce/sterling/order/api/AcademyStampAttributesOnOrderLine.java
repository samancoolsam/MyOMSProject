package com.academy.ecommerce.sterling.order.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * This class will stamp certain key attribiutes like 'SourcingClassification'
 * on order header and 'PackListType' on order line depending on certain other
 * associated attributes of the order
 * 
 * @author Jayaraman Parameswaran
 * @version 1.0, May 14th 2009
 */
public class AcademyStampAttributesOnOrderLine implements YIFCustomApi {
	/** log variable for logging messages */
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyStampAttributesOnOrderLine.class);

	/** Environment class variable */
	YFSEnvironment envLocal = null;

	/** Output document class variable */
	Document docOutput = null;

	/**
	 * During configuration of custom API's, Name/Value pairs can be specified,
	 * these name/value pairs can be accessed by the custom API as a Properties
	 * object by implementing the YIFCustom API interface
	 * 
	 * @param arg0
	 *            Properties instance containing the Name/Value pairs
	 * 
	 * @throws Exception
	 *             Generic Exception
	 */
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}

	/**
	 * This would get the BeforeCreateOrderUE input and verify if attributes
	 * like 'SourcingClassification' at the header level and 'PackListType' at
	 * the line level needs to be stamped based on certain Item & Customer
	 * attributes. If required, appropriate methods will be called to stamp the
	 * above attributes.
	 * 
	 * @param env
	 *            Envrionment variable passed to the API
	 * @param inDoc
	 *            Input document of
	 * 
	 * @return docOutput Manipulated output document for further processing of
	 *         createOrder transaction
	 * 
	 * @throws Exception
	 *             Generic Exception
	 */
	public Document stampAttributesOnOrderLines(YFSEnvironment env,
			Document inDoc) throws Exception {
		Document docCustomerDetails = null;
		String strCorporateCustomer = null;
		String strCustomerId = null;
		this.envLocal = env;
		log.beginTimer(" Begining of AcademyStampAttributesOnOrderLine- >stampAttributesOnOrderLines Api");
		if (!YFCObject.isVoid(inDoc)) {
			if (log.isVerboseEnabled()) {
				log
						.verbose("********* Inside stampAttributesOnOrderLines input document is :::"
								+ XMLUtil.getXMLString(inDoc));
			}
			prepareOutputDocument(inDoc);
			strCustomerId = inDoc.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_BILL_TO_ID);
			
			/*  Start - 12/17/2010 - Commented by Manju to avoid getCustomerList call since corporate Customer logic is out of scope
			docCustomerDetails = getCustomerDetails(strCustomerId);
			try {
				if (!YFCObject.isVoid(docCustomerDetails)) {
					strCorporateCustomer = XPathUtil.getString(
							docCustomerDetails.getDocumentElement(),
							AcademyConstants.XPATH_CUST_CORP_CUSTOMER);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!YFCObject.isVoid(strCorporateCustomer)) {
				if (log.isVerboseEnabled()) {
					log
							.verbose("********* Calling method to verify and stamp attributes on order header ******");
				}
				stampOrderAttributesIfCorporateCustomer(strCorporateCustomer);
			}
			   End - 12/17/2010
			*/
			if (log.isVerboseEnabled()) {
				log
						.verbose("********* Calling method to verify and stamp attributes on order lines ******");
			}
			stampPackListTypeForOrderLines();
			if (log.isVerboseEnabled()) {
				log
						.verbose("********* Calling method to clear templates in env ******");
			}
			clearTemplatesForAPIs();
		}
		/*
		 * System.out.println("#### Final docOutput is :::::" +
		 * XMLUtil.getXMLString(docOutput));
		 */
		log.endTimer(" End of AcademyStampAttributesOnOrderLine- >stampAttributesOnOrderLines Api");
		return docOutput;
	}

	/**
	 * This method will make a getCustomerList API call and get specific details
	 * of the customer id passed
	 * 
	 * @param strCustomerId
	 *            Customer Id or Bill To Id
	 * 
	 * @return docGetCustomerrDetailsOutput Document containing customer details
	 */
	private Document getCustomerDetails(String strCustomerId) {
		Document docGetCustomerDetailsInput = null;
		Document docGetCustomerrDetailsOutput = null;
		Document docGetCustomerDetailsTemplate = null;
		Element eleCustomerExtension = null;
		Element eleCustomer = null;
		Element eleCustomerContact = null;
		Element eleCustomerContactList = null;
		log.beginTimer(" Begin of AcademyStampAttributesOnOrderLine- >getCustomerDetails Api");
		if (log.isVerboseEnabled()) {
			log
					.verbose("*************  Inside getCustomerDetails for customer id :::"
							+ strCustomerId);
		}
		try {
			docGetCustomerDetailsInput = XMLUtil
					.createDocument(AcademyConstants.ELE_CUSTOMER);
			docGetCustomerDetailsInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_CUST_ID, strCustomerId);
			docGetCustomerDetailsInput.getDocumentElement().setAttribute(
					AcademyConstants.ORGANIZATION_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
			docGetCustomerDetailsTemplate = XMLUtil
					.createDocument(AcademyConstants.ELE_CUSTOMER_LIST);
			eleCustomer = docGetCustomerDetailsTemplate
					.createElement(AcademyConstants.ELE_CUSTOMER);
			docGetCustomerDetailsTemplate.getDocumentElement().appendChild(
					eleCustomer);
			eleCustomerContactList = docGetCustomerDetailsTemplate
					.createElement(AcademyConstants.ELE_CUSTOMER_CONTACT_LIST);
			eleCustomer.appendChild(eleCustomerContactList);
			eleCustomerContact = docGetCustomerDetailsTemplate
					.createElement(AcademyConstants.ELE_CUSTOMER_CONTACT);
			eleCustomerContactList.appendChild(eleCustomerContact);
			eleCustomerExtension = docGetCustomerDetailsTemplate
					.createElement(AcademyConstants.ELE_EXTN);
			eleCustomerContact.appendChild(eleCustomerExtension);
			envLocal.setApiTemplate(AcademyConstants.API_GET_CUST_LIST,
					docGetCustomerDetailsTemplate);
			docGetCustomerrDetailsOutput = AcademyUtil.invokeAPI(this.envLocal,
					AcademyConstants.API_GET_CUST_LIST,
					docGetCustomerDetailsInput);
			if (log.isVerboseEnabled()) {
				log
						.verbose("*************  Returning details for customer id :::"
								+ XMLUtil
										.getXMLString(docGetCustomerrDetailsOutput));
			}
			log.endTimer(" End of AcademyStampAttributesOnOrderLine- >getCustomerDetails Api");
			return docGetCustomerrDetailsOutput;
		} catch (Exception e) {
						return null;
		}
	}

	/**
	 * This method prepares the output document by cloning the input document.
	 * 
	 * @param inDoc
	 *            input Document passed to the API
	 * 
	 */
	private void prepareOutputDocument(Document inDoc) {
		try {
			if (log.isVerboseEnabled())
				log
						.verbose("*********** Inside prepareOutputDocument  **********");
			this.docOutput = XMLUtil.cloneDocument(inDoc);
			log.verbose("*********** Prepared output document :::"
					+ XMLUtil.getXMLString(this.docOutput));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method stamps "SourcingClassification" attribute on order header if
	 * order is placed by a business customer. Attribute at customer level is
	 * checked to determine if it's a business customerF
	 * 
	 * @param strCorporateCustomer
	 *            Attribute which marks customer as Business Customer
	 * 
	 */
	private void stampOrderAttributesIfCorporateCustomer(
			String strCorporateCustomer) {
		log.beginTimer(" Begin of AcademyStampAttributesOnOrderLine- >getCustomerDetails Api");
		if (log.isVerboseEnabled()) {
			log
					.verbose("********* Inisde  stampOrderAttributesIfCorporateCustomer ****");
		}
		if (!YFCObject.isVoid(strCorporateCustomer)) {
			if (strCorporateCustomer.equals(AcademyConstants.STR_YES)) {
				if (log.isVerboseEnabled()) {
					log
							.verbose("********* It is a Business Customer and hence stamping attributes ****");
				}
				try {
					docOutput.getDocumentElement().setAttribute(
							AcademyConstants.ATTR_SOURC_CLASS,
							AcademyConstants.STR_BUSINESS);

					// Element eleOrderExtension=(Element)
					// XPathUtil.getNode(docOutput.getDocumentElement(),"Extn");
					Element eleOrderExtension = docOutput
							.createElement(AcademyConstants.ELE_EXTN);
					docOutput.getDocumentElement().appendChild(
							eleOrderExtension);
					eleOrderExtension.setAttribute(
							AcademyConstants.ATTR_CORP_CUSTOMER_ORDERHEADER,
							AcademyConstants.STR_YES);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				if (log.isVerboseEnabled()) {
					log.verbose("*******  Not a Business Customer *****");
				}
			}
		}
		log.endTimer(" End of AcademyStampAttributesOnOrderLine- >stampOrderAttributesIfCorporateCustomer Api");
	}

	/**
	 * This method will stamp 'PackListType' attribute on lines which have items
	 * that have 'Ship Alone' flag enabled
	 * 
	 * 
	 */
	private void stampPackListTypeForOrderLines() throws YFSException{
		Element eleOrderInOutput = null;
		NodeList nListOrderLines = null;
		int iNoOfOrderLines = 0;
		Element eleCurrentOrderLine = null;
		Element extnItem = null;
		String strItemID = null;
		String strPrimeLineNo = null;
		String strShipAloneFlag = null;
		eleOrderInOutput = docOutput.getDocumentElement();
		YFSException itemExce = null;

		try {
			log.beginTimer(" Begin of AcademyStampAttributesOnOrderLine- >stampPackListTypeForOrderLines Api");
			if (log.isVerboseEnabled()) {
				log
						.verbose("********* Inside stampPackListTypeForOrderLines  ******");
			}
			// Start - Fix for # 3968
			Element elePersonInfoShipTo = eleOrderInOutput.getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).getLength()> 0 ? (Element)eleOrderInOutput.getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).item(0) : null;
			// End - Fix for # 3968
			nListOrderLines = XMLUtil.getNodeList(eleOrderInOutput,
					AcademyConstants.XPATH_ORDERLINE);

			if (!YFCObject.isVoid(nListOrderLines)) {
				iNoOfOrderLines = nListOrderLines.getLength();
			}

			for (int i = 0; i < iNoOfOrderLines; i++) {
				eleCurrentOrderLine = (Element) nListOrderLines.item(i);

				if (!YFCObject.isVoid(eleCurrentOrderLine)) {
					strItemID = XPathUtil.getString(eleCurrentOrderLine, AcademyConstants.XPATH_ITEM_ITEMID);
					strPrimeLineNo = eleCurrentOrderLine .getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO);
					// Start - Fix for # 3968
					int countPersonInfoShipTo = eleCurrentOrderLine.getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).getLength();
					Element personInfoShipTo = countPersonInfoShipTo > 0 ? (Element)eleCurrentOrderLine.getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).item(0) : docOutput.createElement(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO);
					if(!(personInfoShipTo.hasAttribute("AddressLine1") && personInfoShipTo.hasAttribute(AcademyConstants.ATTR_FNAME) 
							&& personInfoShipTo.hasAttribute(AcademyConstants.ATTR_LNAME)) && 
							!(YFCObject.isNull(elePersonInfoShipTo) && YFCObject.isVoid(elePersonInfoShipTo))){
						log.verbose("PersonInfoShipTo on Line : "+XMLUtil.getElementXMLString(personInfoShipTo));						
						XMLUtil.setAttributes(elePersonInfoShipTo, personInfoShipTo);
						if(countPersonInfoShipTo <= 0)
							eleCurrentOrderLine.appendChild(elePersonInfoShipTo);
						log.verbose("Post copy of PersonInfoShipTo is : "+XMLUtil.getElementXMLString(eleCurrentOrderLine));
					}
					// End - Fix for # 3968
				}
				if(strItemID == null || (strItemID != null && strItemID.trim().length()<=0)){
					log.verbose(AcademyConstants.ERR_ITEM_NOT_FOUND);
					itemExce = new YFSException();
					itemExce.setErrorCode("EXTN_ACADEMY_10");
					itemExce.setErrorDescription(AcademyConstants.ERR_ITEM_NOT_FOUND);
					throw itemExce;
				}

				Document getItemList = XMLUtil.createDocument(AcademyConstants.ITEM);
				getItemList.getDocumentElement().setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemID);
				Document outPutTemplate = YFCDocument.getDocumentFor("<ItemList> <Item ItemKey=''><Extn ExtnShipAlone='' ExtnWhiteGloveEligible=''/></Item> </ItemList>").getDocument();
				envLocal.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, outPutTemplate);
				log.verbose("itemList " + XMLUtil.getXMLString(getItemList));
				Document docGetItemDetailsOutput = AcademyUtil.invokeAPI(envLocal, AcademyConstants.API_GET_ITEM_LIST, getItemList);
				envLocal.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
				log.verbose("getItemList output " + XMLUtil.getXMLString(docGetItemDetailsOutput));

				if (!YFCObject.isVoid(docGetItemDetailsOutput) && docGetItemDetailsOutput.getDocumentElement().getElementsByTagName(AcademyConstants.ITEM).getLength()>0) 
				{
					extnItem = (Element) docGetItemDetailsOutput.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
					strShipAloneFlag = extnItem.getAttribute(AcademyConstants.ATTR_EXTN_SHPALONE);

					String strExtnWhiteGloveEligible = extnItem.getAttribute(AcademyConstants.ATTR_EXTN_WHITE_GLOVE_ELIGIBLE);
					if (!YFCObject.isVoid(strShipAloneFlag)) 
					{
						/** *****Start CR 38 ******** */
						if (!YFCObject.isVoid(strExtnWhiteGloveEligible)) 
						{
							if ((strShipAloneFlag.equals(AcademyConstants.STR_YES))
									&& (strExtnWhiteGloveEligible.equals(AcademyConstants.STR_NO))) 
							{
								/** *****End CR 38 ******** */
								if (log.isVerboseEnabled()) 
								{
									log.verbose("******** Order line has Ship Alone Item & non-white glove and hence stamping PackListType ***8");
								}
								eleCurrentOrderLine.setAttribute(AcademyConstants.ATTR_PACKLIST_TYPE, strPrimeLineNo);
							}
						}
					}
				}else{
					log.verbose("Item "+strItemID+" is not found. It may held status or not existed.");
					itemExce =  new YFSException();
					itemExce.setErrorCode("EXTN_ACADEMY_11");
					itemExce.setErrorDescription("Item "+strItemID+" is not found. It may held status or not existed.");
					throw itemExce;
				}
			}
			
			log.endTimer(" End of AcademyStampAttributesOnOrderLine- >stampPackListTypeForOrderLines Api");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method clears all templates set in the environment variable
	 */
	private void clearTemplatesForAPIs() {
		this.envLocal.clearApiTemplates();
	}
}
