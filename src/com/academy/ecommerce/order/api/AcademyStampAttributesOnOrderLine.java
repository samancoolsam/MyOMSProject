package com.academy.ecommerce.order.api;

import java.io.File;
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

/**
 * DOCUMENT ME!
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

		if (!YFCObject.isVoid(inDoc)) {
			prepareOutputDocument(inDoc);
			strCustomerId = inDoc.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_BILL_TO_ID);
			log.verbose("#### Input Document is :::::"+XMLUtil.getElementXMLString(inDoc.getDocumentElement()));
			log.verbose("#### strCustomerId is :::::"+strCustomerId);
			docCustomerDetails = getCustomerDetails(strCustomerId);

			try {
				if (!YFCObject.isVoid(docCustomerDetails)) {
					// eleCustomerExtension = (Element)
					// docCustomerDetails.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
					// strCorporateCustomer =
					// eleCustomerExtension.getAttribute(AcademyConstants.ATTR_EXTN_CORP_CUSTOMER);
					strCorporateCustomer = XPathUtil.getString(
							docCustomerDetails.getDocumentElement(),
							AcademyConstants.XPATH_CUST_CORP_CUSTOMER);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!YFCObject.isVoid(strCorporateCustomer)) {
				stampOrderAttributesIfCorporateCustomer(strCorporateCustomer);
			}

			stampPackListTypeForOrderLines();
			clearTemplatesForAPIs();
		}

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

		try {
			docGetCustomerDetailsInput = XMLUtil
					.createDocument(AcademyConstants.ELE_CUSTOMER);
			docGetCustomerDetailsInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_CUST_ID, strCustomerId);
			docGetCustomerDetailsTemplate = XMLUtil
					.createDocument("CustomerList");
			eleCustomer = docGetCustomerDetailsTemplate
					.createElement(AcademyConstants.ELE_CUSTOMER);
			docGetCustomerDetailsTemplate.getDocumentElement().appendChild(
					eleCustomer);
			eleCustomerContactList = docGetCustomerDetailsTemplate
					.createElement("CustomerContactList");
			eleCustomer.appendChild(eleCustomerContactList);
			eleCustomerContact = docGetCustomerDetailsTemplate
					.createElement("CustomerContact");
			eleCustomerContactList.appendChild(eleCustomerContact);
			eleCustomerExtension = docGetCustomerDetailsTemplate
					.createElement(AcademyConstants.ELE_EXTN);
			eleCustomerContact.appendChild(eleCustomerExtension);
			envLocal.setApiTemplate(AcademyConstants.API_GET_CUST_LIST,
					docGetCustomerDetailsTemplate);
			System.out
					.println("######## docGetCustomerDetailsTemplate #########"
							+ XMLUtil
									.getXMLString(docGetCustomerDetailsTemplate));
			System.out
			.println("######## docGetCustomerDetailsInput #########"
					+ XMLUtil
							.getXMLString(docGetCustomerDetailsInput));
			docGetCustomerrDetailsOutput = AcademyUtil.invokeAPI(this.envLocal,
					AcademyConstants.API_GET_CUST_LIST,
					docGetCustomerDetailsInput);
			System.out
					.println("######## docGetCustomerrDetailsOutput #########"
							+ XMLUtil
									.getXMLString(docGetCustomerrDetailsOutput));

			return docGetCustomerrDetailsOutput;
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param inDoc
	 *            DOCUMENT ME!
	 */
	private void prepareOutputDocument(Document inDoc) {
		try {
			this.docOutput = XMLUtil.cloneDocument(inDoc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param strCorporateCustomer
	 *            DOCUMENT ME!
	 */
	private void stampOrderAttributesIfCorporateCustomer(
			String strCorporateCustomer) {
		if (!YFCObject.isVoid(strCorporateCustomer)) {
			log.verbose("######## Inside method :stampOrderAttributesIfCorporateCustomer ## ");
			log.verbose("######## strCorporateCustomer ## :::::"+strCorporateCustomer);
			if (strCorporateCustomer.equals(AcademyConstants.STR_YES)) {
				try {
					docOutput.getDocumentElement().setAttribute(
							AcademyConstants.ATTR_SOURC_CLASS,
							AcademyConstants.STR_BUSINESS);

					// Element eleOrderExtension=(Element)
					// XPathUtil.getNode(docOutput.getDocumentElement(),"Extn");
					Element eleOrderExtension = docOutput.createElement("Extn");
					docOutput.getDocumentElement().appendChild(
							eleOrderExtension);
					eleOrderExtension.setAttribute("ExtnCorporateCustomer",
							AcademyConstants.STR_YES);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 */
	private void stampPackListTypeForOrderLines() {
		int iNoOfOrderLines = 0;
		String strItemID = "";
		Element eleOrderInOutput = docOutput.getDocumentElement();

		try 
		{
			NodeList nListOrderLines = XMLUtil.getNodeList(eleOrderInOutput, AcademyConstants.XPATH_ORDERLINE);

			if (!YFCObject.isVoid(nListOrderLines)) 
			{
				iNoOfOrderLines = nListOrderLines.getLength();
			}

			for (int i = 0; i < iNoOfOrderLines; i++) 
			{
				Element eleCurrentOrderLine = (Element) nListOrderLines.item(i);

				if (!YFCObject.isVoid(eleCurrentOrderLine)) 
				{
					strItemID = XPathUtil.getString(eleCurrentOrderLine, AcademyConstants.XPATH_ITEM_ITEMID);
					log.verbose("Item ID - " + strItemID);
					
					Document getItemList = XMLUtil.createDocument(AcademyConstants.ITEM);
					getItemList.getDocumentElement().setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemID);
					Document outPutTemplate = YFCDocument.getDocumentFor("<ItemList> <Item ItemKey=''><Extn ExtnShipAlone=''/></Item> </ItemList>").getDocument();
					envLocal.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, outPutTemplate);
					log.verbose("itemList " + XMLUtil.getXMLString(getItemList));
					Document itemListDocument = AcademyUtil.invokeAPI(envLocal, AcademyConstants.API_GET_ITEM_LIST, getItemList);
					envLocal.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
					log.verbose("getItemList output " + XMLUtil.getXMLString(itemListDocument));

					if (!YFCObject.isVoid(itemListDocument)) 
					{
						Element extnItem = (Element) itemListDocument.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
						String strShipAloneFlag = extnItem.getAttribute(AcademyConstants.ATTR_EXTN_SHPALONE);

						if (!YFCObject.isVoid(strShipAloneFlag)) 
						{
							if (strShipAloneFlag.equals(AcademyConstants.STR_YES)) 
							{
								eleCurrentOrderLine.setAttribute(AcademyConstants.ATTR_PACKLIST_TYPE, strItemID);
							}
						}
					}
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 */
	private void clearTemplatesForAPIs() {
		this.envLocal.clearApiTemplates();
	}
	
	public static void main(String[] args)
	{
		Document doc = YFCDocument.getDocumentFor(new File("C://input.xml")).getDocument();
		log.verbose(XMLUtil.getXMLString(doc));
		Element extnItem = (Element) doc.getDocumentElement().getElementsByTagName("Extn").item(0);
		String strShipAloneFlag = extnItem.getAttribute("ExtnShipAlone");
		log.verbose(strShipAloneFlag);

	}
}
