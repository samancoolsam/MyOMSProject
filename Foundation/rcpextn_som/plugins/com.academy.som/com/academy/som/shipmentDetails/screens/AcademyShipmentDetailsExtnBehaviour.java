
package com.academy.som.shipmentDetails.screens;

/**
 * Created on Mar 12,2014
 *
 */

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCXPathUtils;
import com.yantra.yfc.rcp.YRCXmlUtils;
import java.util.Iterator;
import com.yantra.yfc.rcp.internal.YRCRelatedTasksManager;
import com.yantra.yfc.rcp.YRCRelatedTask;


/**
 * Custom extension behavior class that overrides the functionality of
 * OOB class : "com.yantra.pca.ycd.rcp.tasks.shipmentTracking.screens.YCDShipmentDetails"
 * 
 * @author <a href="mailto:Kruthi.KM@cognizant.com">Kruthi K M</a>
 * © Copyright IBM Corp. All Rights Reserved.
 */
public class AcademyShipmentDetailsExtnBehaviour extends YRCExtentionBehavior{
	private static String CLASSNAME = "AcademyShipmentDetailsExtnBehaviour";

	//START: SHIN-11
	Element shipmentDtlsElem;
	//END: SHIN-11
	
	/**
	 * This method initializes the behavior class.
	 */
	public void init() {
		//TODO: Write behavior init here.
		callShipmentTypeCC();
	
		
	}
	
	/* Start - Changes made for STL-685 Cancel Shipment from SIM */
	public void postCommand(YRCApiContext apiContext) {
		if (apiContext.getApiName().equals("getShipmentDetails"))
		{
			String Status = apiContext.getOutputXml().getDocumentElement().getAttribute(AcademyPCAConstants.STATUS_ATTR);

			//Start : OMNI-6616 : STS RCP SOM UI Changes
			String strShipmentType  = apiContext.getOutputXml().getDocumentElement().getAttribute(AcademyPCAConstants.SHIPMENT_TYPE_ATTR);
			AcademySIMTraceUtil.logMessage("strShipmentType :: " + strShipmentType);
			
			if(strShipmentType != null && AcademyPCAConstants.STR_STS.equals(strShipmentType)) {
				setControlVisible(AcademyPCAConstants.EXTN_CANCEL_SHIPMENT, false);
			}
			//End : OMNI-6616 : STS RCP SOM UI Changes

			else if (Status != null && !Status.equals(AcademyPCAConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL) && !Status.equals(AcademyPCAConstants.STATUS_READY_FOR_CUSTOMER_VAL)) {
				setControlVisible(AcademyPCAConstants.EXTN_CANCEL_SHIPMENT, false);
			}
		}
		super.postCommand(apiContext);
	}
	/* End - Changes made for STL-685 Cancel Shipment from SIM */
	
	/**
	 * Method to call the getCommonCodeList API for populating
	 * the Shipment Type custom field with the description value in Shipment details
	 * page
	 *
	 */
	private void callShipmentTypeCC() {
		final String methodName="callShipmentTypeCC()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.SHIPMENT_TYPE_CC_COMMAND);
		context.setInputXml(prepareInputForGetShipmentTypeCC());
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * Method to prepare the Input document for getCommonCodeList API call
	 * to populate the Shipment Type custom field in Shipment Details page
	 * @return Document
	 * 			<br/> - Input for populating Shipment Type
	 * 			<pre>
	 * 			{@code
	 * 				<CommonCode CodeType="SHIPMENT_TYPE" />
	 * 			}
	 * 			</pre> 
	 */
	private Document prepareInputForGetShipmentTypeCC() {
		final String methodName="prepareInputForGetShipmentTypeCC()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		Document commonCodeInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.COMMON_CODE_ELEMENT);
		Element rootElement = commonCodeInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.CODE_TYPE_ATTR, AcademyPCAConstants.CODE_TYPE_VAL_FOR_SHIPMENT_TYPE);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return commonCodeInputDoc;
	}

	/**
	 * Related task action method to reprint Shipment Invoice(Return label)
	 * @param xml
	 * 			<br/> - Editor Input XML of shipment details page
	 * @param shipmentCntrKey
	 * 			<br/> - ShipmentContainerKey selected for reprint
	 */
	public void processShipmentInvoiceRequest(Element xml, String shipmentCntrKey, String strpackStn) {
		final String methodName="processShipmentInvoiceRequest(xml, shipmentCntrKey)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		AcademySIMTraceUtil.logMessage(CLASSNAME+" : processShipmentInvoiceRequest(Element, String):: \n", xml);
		callServiceForPrint(AcademyPCAConstants.REPRINT_INVOICE_SERVICE_COMMAND, getReprintRequestDocument(xml, shipmentCntrKey,strpackStn ));

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}
	/**
	 * Method to call the reprint services for reprinting Packslip,
	 * Shipping Label, Return Label and Shipment Pick Ticket
	 * @param commandName
	 * 			<br/> - Service Command Name to be called for reprint request 
	 * @param inDoc
	 * 			<br/> - Input document for reprint services
	 */
	private void callServiceForPrint(String commandName, Document inDoc){
		final String methodName="callServiceForPrint(commandName, inDoc)";
		AcademySIMTraceUtil.logMessage("Input to rerprint service is : :"+YRCXmlUtils.getString(inDoc));
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		YRCApiContext context = new YRCApiContext();
		context.setApiName(commandName);
		context.setInputXml(inDoc);
		context.setFormId(getFormId());
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}
	/**
	 * Method to prepare the Input document for reprinting services,
	 * to reprint Packslip, Shipping Label, Return Label and Shipment Pick Ticket 
	 * @param xml
	 * 			<br/> - Editor Input XML of shipment details page
	 * @param shipmentCntrKey
	 * 			<br/> - ShipmentContainerKey selected for reprint
	 * @return Document
	 * 			<br/> - Input for reprint services
	 * 			<pre>
	 * 			{@code
	 * 				<Print ShipNode="<<xml:/ShipmentDetail/Shipment/@ShipNode>>" 
	 * 					   ShipmentKey="<<xml:/ShipmentDetail/Shipment/@ShipmentKey>>"
	 * 					   ShipmentContainerKey="<<shipmentCntrKey>>" PickTicketPrinted="Y"
	 * 					   ShipmentType="<<xml:/ShipmentDetail/Shipment/Shipment/@ShipmentType>>" />
	 * 			}
	 * 			</pre>
	 */
	private Document getReprintRequestDocument(Element xml, String shipmentCntrKey, String packStn){
		final String methodName="getReprintRequestDocument(xml, shipmentCntrKey)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		String strIsPickTicketPrinted = "Y";
		Document printInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.PRINT_ELEMENT);
		Element rootElement = printInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.SHIPNODE_ATTR, xml.getAttribute(AcademyPCAConstants.SHIPNODE_ATTR));
		rootElement.setAttribute(AcademyPCAConstants.SHIPMENT_KEY_ATTR, xml.getAttribute(AcademyPCAConstants.SHIPMENT_KEY_ATTR));
		rootElement.setAttribute(AcademyPCAConstants.SHIPMENT_CNTR_KEY_ATTR, shipmentCntrKey);
		rootElement.setAttribute(AcademyPCAConstants.PACK_STATION_FOR_REPRITN, packStn);		
		rootElement.setAttribute(AcademyPCAConstants.SHIPMENT_TYPE_ATTR, xml.getAttribute(AcademyPCAConstants.SHIPMENT_TYPE_ATTR));
		//OMNI-7980
		rootElement.setAttribute(AcademyPCAConstants.DOCUMENT_TYPE_ATTR, xml.getAttribute(AcademyPCAConstants.DOCUMENT_TYPE_ATTR));
		//OMNI-7980
		//START : STL-1501 Print pick ticket for MO device
		Element eleUserNameSpaceModel = getModel(AcademyPCAConstants.USER_NAME_SPACE_MODEL);
		Document docLogin = YRCXmlUtils.createDocument(AcademyPCAConstants.ELE_LOGIN);
		Element eleLogin = docLogin.getDocumentElement();
		eleLogin.setAttribute(AcademyPCAConstants.ATTR_LOGIN_ID, eleUserNameSpaceModel.getAttribute(AcademyPCAConstants.ATTR_USER_LOGIN_ID));
		//rootElement.appendChild(eleLogin);
		YRCXmlUtils.importElement(rootElement, eleLogin);
		//END : STL-1501 Print pick ticket for MO device
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		//  SFS2.0
		rootElement.setAttribute("PickTicketPrinted",strIsPickTicketPrinted);
		AcademySIMTraceUtil.logMessage("PickTicketPrinted");
		//SFS2.0
		return printInputDoc;
	}
	/**
	 * Related task action method to reprint Shipping Label
	 * @param xml
	 * 			<br/> - Editor Input XML of shipment details page
	 * @param shipmentCntrKey
	 * 			<br/> - ShipmentContainerKey selected for reprint
	 */
	public void processShippingLabelRequest(Element xml, String shipmentCntrKey, String strpackStn) {
		final String methodName="processShippingLabelRequest(xml, shipmentCntrKey)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		AcademySIMTraceUtil.logMessage(CLASSNAME+" : processShippingLabelRequest(Element, String):: \n", xml);
		callServiceForPrint(AcademyPCAConstants.REPRINT_SHIP_LABEL_SERVICE_COMMAND, getReprintRequestDocument(xml, shipmentCntrKey, strpackStn));

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}
	
	/**
      * Added as a part of STL-737
      * Related task action method to reprint ORMD Label
      * 
      * @param xml
      *            <br/> - Editor Input XML of shipment details page
      * @param shipmentCntrKey
      *            <br/> - ShipmentContainerKey selected for reprint
      */
    public void processORMDLabelRequest(Element xml, String shipmentCntrKey, String strpackStn) {
      AcademySIMTraceUtil.startMessage(CLASSNAME, "processORMDLabelRequest(xml, shipmentCntrKey)");
      AcademySIMTraceUtil.logMessage(CLASSNAME + " : processORMDLabelRequest(Element, String):: \n", xml);
      callServiceForPrint("AcademySFSRePrintORMDLabel", getReprintRequestDocument(xml, shipmentCntrKey, strpackStn));	
      AcademySIMTraceUtil.endMessage(CLASSNAME, "processORMDLabelRequest(xml, shipmentCntrKey)");
    } //End processORMDLabelRequest
	
	/**
	 * Related task action method to reprint Shipment Pick Ticket
	 * @param xml
	 * 			<br/> - Editor Input XML of shipment details page
	 */
	public void processShipmentPickTicketRequest(Element xml) {
		final String methodName="processShipmentPickTicketRequest(xml)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		String PicktTicketStation="";

		AcademySIMTraceUtil.logMessage(CLASSNAME+" : processShipmentPickTicketRequest(Element):: \n", xml);
		callServiceForPrint(AcademyPCAConstants.REPRINT_SHIPMENT_PICK_TICKET_SERVICE_COMMAND, getReprintRequestDocument(xml, AcademyPCAConstants.EMPTY_STRING, PicktTicketStation));

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}
	/**
	 * Related task action method to reprint Packslip
	 * @param xml
	 * 			<br/> - Editor Input XML of shipment details page
	 * @param shipmentCntrKey
	 * 			<br/> - ShipmentContainerKey selected for reprint
	 */
	public void processPackslipRequest(Element xml, String shipmentCntrKey, String strpackStn) {
		final String methodName="processPackslipRequest(xml, shipmentCntrKey)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		AcademySIMTraceUtil.logMessage(CLASSNAME+" : processPackslipRequest(Element, String):: \n", xml);
		callServiceForPrint(AcademyPCAConstants.REPRINT_PACK_LIST_SERVICE_COMMAND, getReprintRequestDocument(xml, shipmentCntrKey, strpackStn));

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}
	/**
	 * Related Task Action method called for correcting the shipment
	 * @param shipmentDtlsElement
	 * 			<br/> - Editor Input XML of shipment details page
	 */
	public void processAutoShipmentCorrectionRequest(Element shipmentDtlsElement) {
		final String methodName="processAutoShipmentCorrectionRequest(shipmentDtlsElement)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		AcademySIMTraceUtil.logMessage(CLASSNAME+" : processAutoShipmentCorrectionRequest(Element):: \n", shipmentDtlsElement);
		callServiceForAutoShipmentCorrection(shipmentDtlsElement);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}
	/**
	 * Method to call the custom service for shipment correction
	 * @param shipmentDtlsElement
	 * 			<br/> - Editor Input XML of shipment details page
	 */
	private void callServiceForAutoShipmentCorrection(Element shipmentDtlsElement) {
		final String methodName="callServiceForAutoShipmentCorrection(shipmentDtlsElement)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		YRCApiContext context = new YRCApiContext();
		context.setApiName(AcademyPCAConstants.AUTO_CORRECT_SHPMNT_SERVICE_COMMAND);
		context.setInputXml(getInputDocForAutoShipmentCorrect(shipmentDtlsElement));
		context.setFormId(getFormId());
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}
	/**
	 * Method to prepare the Input document for shipment correction service
	 * @param shipmentDtlsElement
	 * 			<br/> - Editor Input XML of shipment details page
	 * @return Document
	 * 			<br/> - Input for shipment correction service
	 * 			<pre>
	 * 			{@code
	 * 				<Shipment ShipmentKey="<<shipmentDtlsElement:/ShipmentDetail/Shipment/@ShipmentKey>>" />
	 * 			}
	 * 			</pre>
	 */
	private Document getInputDocForAutoShipmentCorrect(Element shipmentDtlsElement) {
		final String methodName="getInputDocForAutoShipmentCorrect(shipmentDtlsElement)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		Document shipmentInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.SHIPMENT_ELEMENT);
		Element rootElement = shipmentInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.SHIPMENT_KEY_ATTR, 
				shipmentDtlsElement.getAttribute("ShipmentKey"));

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);		
		return shipmentInputDoc;
	}
	/**
	 * Related Task Action method for correcting the Manifest of shipment
	 * @param shipmentDtlsElement
	 * 			<br/> - Editor Input XML of shipment details page
	 */
	public void processAutoManifestCorrectionRequest(Element shipmentDtlsElement) {
		final String methodName="processAutoManifestCorrectionRequest(shipmentDtlsElement)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		AcademySIMTraceUtil.logMessage(CLASSNAME+" : processAutoManifestCorrectionRequest(Element):: \n", shipmentDtlsElement);
		callServiceForAutoManifestCorrection(shipmentDtlsElement);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * Method to call the custom service for manifest correction
	 * @param shipmentDtlsElement
	 * 			<br/> - Editor Input XML of shipment details page
	 */
	private void callServiceForAutoManifestCorrection(Element shipmentDtlsElement) {
		final String methodName="callServiceForAutoManifestCorrection(shipmentDtlsElement)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		YRCApiContext context = new YRCApiContext();
		context.setApiName(AcademyPCAConstants.AUTO_CORRECT_MANIFEST_SERVICE_COMMAND);
		context.setInputXml(getInputDocForAutoManifestCorrect(shipmentDtlsElement));
		context.setFormId(getFormId());
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * Method to prepare the Input document for manifest correction service
	 * @param shipmentDtlsElement
	 * 			<br/> - Editor Input XML of shipment details page
	 * @return Document
	 * 			<br/> - Input for manifest correction service
	 * 			<pre>
	 * 			{@code
	 * 				<Shipment ManifestKey="<<shipmentDtlsElement:/ShipmentDetail/Shipment/@ManifestKey>>" />
	 * 			}
	 * 			</pre>
	 */
	private Document getInputDocForAutoManifestCorrect(Element shipmentDtlsElement) {
		final String methodName="getInputDocForAutoManifestCorrect(shipmentDtlsElement)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		Document shipmentInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.SHIPMENT_ELEMENT);
		Element rootElement = shipmentInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.MANIFEST_KEY_ATTR, 
				shipmentDtlsElement.getAttribute("ManifestKey"));

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return shipmentInputDoc;
	}
	/**
	 * Superclass method called to modify the OOB screen models when set
	 * @param nameSpace
	 * 			<br/> - name of the OOB screen model
	 */
	public void postSetModel(String nameSpace) {
		final String methodName = "postSetModel(nameSpace)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		if(nameSpace.equals(AcademyPCAConstants.SHIPMENT_OOB_MODEL))
		{
			Element eleShipment = getModel(AcademyPCAConstants.SHIPMENT_OOB_MODEL);
			String shipmentTypeVal = eleShipment.getAttribute("ShipmentType");
			String shipmentTypeDescXpath = AcademyPCAConstants.COMMONCODE_ELEMENT_XPATH + "[@" +
			AcademyPCAConstants.CODE_VALUE_ATTR + "='" + shipmentTypeVal + 
			"']/@" + AcademyPCAConstants.CODE_LONG_DESC_ATTR;
			//setting the Shipment Type text field in Shipment detail screen with codeLongDescription Value 
			Element commonCodeDocele = getExtentionModel("Extn_getShipmentTypeCC_Output");
			Document commonCodeDoc=commonCodeDocele.getOwnerDocument();
			AcademySIMTraceUtil.logMessage("common code"+YRCXmlUtils.getString(commonCodeDocele));
			setFieldValue(AcademyPCAConstants.CUSTOM_TXT_SHIPMENT_TYPE, 
					(String)YRCXPathUtils.evaluate(commonCodeDoc.getDocumentElement(), shipmentTypeDescXpath, XPathConstants.STRING));
			String strDocType = eleShipment.getAttribute(AcademyPCAConstants.DOCUMENT_TYPE_ATTR);
			String strReqShipDate = eleShipment.getAttribute("RequestedShipmentDate");
			DateFormat dbDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
			try
			{
				String str = formatter.format(dbDateTimeFormatter.parse(strReqShipDate));
				setFieldValue("extn_txtDelDate", str);
			}catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if(strDocType.equals("0001"))
			{
				setFieldValue("extn_txtDocType", "Sales Order");
			}
			if(strDocType.equals("0003"))
			{
				setFieldValue("extn_txtDocType", "Return Order");
			}
			//Start : OMNI-6616 : Changes for STS
			if(strDocType.equals(AcademyPCAConstants.STR_TRANSFER_ORDER_DOC_TYPE))
			{
				setFieldValue("extn_txtDocType", "Transfer Order");
			}
			//End : OMNI-6616 : Changes for STS
		}
	}

	public Document getOOBModel()
	{
		Element ele = getModel(AcademyPCAConstants.SHIPMENT_OOB_MODEL);
		AcademySIMTraceUtil.logMessage("ele to be used in action class"+YRCXmlUtils.getString(ele));
		Document doc = ele.getOwnerDocument();
		return doc;

	}
	/**
	 * Method for validating the text box.
	 */
	public YRCValidationResponse validateTextField(String fieldName, String fieldValue) {
		// TODO Validation required for the following controls.

		// TODO Create and return a response.
		return super.validateTextField(fieldName, fieldValue);
	}

	/**
	 * Method for validating the combo box entry.
	 */
	public void validateComboField(String fieldName, String fieldValue) {
		// TODO Validation required for the following controls.

		// TODO Create and return a response.
		super.validateComboField(fieldName, fieldValue);
	}

	/**
	 * Method called when a button is clicked.
	 */
	/* Start Changes made as a part of STL-685 */
	public YRCValidationResponse validateButtonClick(String fieldName) {
		// TODO Validation required for the following controls.
		
		try{
		if (fieldName.equals(AcademyPCAConstants.EXTN_CANCEL_SHIPMENT)) {
				shipmentDtlsElem = getModel(AcademyPCAConstants.OOB_SEARCH_SHIPMNT_DTLS_MODEL);
			
			//START: SHIN-11
			/*Getting the attribute IsSharedInventoryDC and checking the condition strIsSharedInvNode is not null
			and the strIsSharedInvNode value equals Y then set the attribute IsSharedInventoryDC is Y */
			
			
			String strIsSharedInvNode = YRCPlatformUI.getUserElement().getAttribute(AcademyPCAConstants.ATTR_IS_SHAREDINV_DC);
			AcademySIMTraceUtil.logMessage(CLASSNAME+"::IsSharedInventory::" +strIsSharedInvNode );
			
			//START: SHIN-22 commented the below logic due to the post window initializer class
			
			/*//Handling the case of UserNameSpace not having the attribute initialized
			if (YRCPlatformUI.isVoid(strIsSharedInvNode))
			{
				AcademySIMTraceUtil.logMessage(CLASSNAME+"::The value is void for strIsSharedInvNode so calling getShipNodeList");
				callGetShipNodeListAPI();
			}
			*
			*/
			//If UserNameSpace was initialized with attribute, and strIsSharedInvNode is "Y" 
			if (!YRCPlatformUI.isVoid(strIsSharedInvNode) && AcademyPCAConstants.STRING_Y.equals(strIsSharedInvNode)) {
			
			// else if (AcademyPCAConstants.STRING_Y.equals(strIsSharedInvNode)) {
			// End : SHIN-22 */
				
				AcademySIMTraceUtil.logMessage(CLASSNAME+"::IsSharedInventoryDC::" +strIsSharedInvNode );
				shipmentDtlsElem.setAttribute(AcademyPCAConstants.ATTR_IS_SHAREDINV_DC, AcademyPCAConstants.STRING_Y);
				callCancelShipmentService();
			}
			//If UserNameSpace was initialized with attribute, and strIsSharedInvNode is "N" 
			
			else if (AcademyPCAConstants.STRING_N.equals(strIsSharedInvNode)) {
				
				AcademySIMTraceUtil.logMessage(CLASSNAME+"::IsSharedInventoryDC::" +strIsSharedInvNode );
				callCancelShipmentService();
			}
			//END: SHIN-11 
			
			
			

		}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		// TODO Create and return a response.
		return super.validateButtonClick(fieldName);
	}
	
	public void callCancelShipmentService() {
		// Call the service AcademyCancelShipmentService
		AcademySIMTraceUtil.logMessage(CLASSNAME+"::AcademyCancelShipmentService input:" + YRCXmlUtils.getString(shipmentDtlsElem));
		Document CancelShipmentDoc = shipmentDtlsElem.getOwnerDocument();
		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.ACAD_CANCEL_SHIPMENT_SERVICE);
		context.setInputXml(CancelShipmentDoc);
		callApi(context);
	}

	/* End Changes made as a part of STL-685 */
	/**
	 * Method called when a link is clicked.
	 */
	public YRCValidationResponse validateLinkClick(String fieldName) {
		// TODO Validation required for the following controls.

		// TODO Create and return a response.
		return super.validateLinkClick(fieldName);
	}

	/**
	 * Create and return the binding data for advanced table columns added to the tables.
	 */
	public YRCExtendedTableBindingData getExtendedTableBindingData(String tableName, ArrayList tableColumnNames) {
		// Create and return the binding data definition for the table.

		// The defualt super implementation does nothing.
		return super.getExtendedTableBindingData(tableName, tableColumnNames);
	}
	
	//START: SHIN-22 commented the below logic due to the post window initializer class
	
	/*public void callGetShipNodeListAPI() {
		String strMethodName = "callGetShipNodeListAPI()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, strMethodName);
		
		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.GET_SHIP_NODE_LIST_COMMAND);
		context.setInputXml(prepareInputForGetShipNodeList());
		YRCPlatformUI.callApi(context, this);
		
		AcademySIMTraceUtil.endMessage(CLASSNAME,strMethodName);
	}
	// The method will prepare the input for getshipNodeList API
	public Document prepareInputForGetShipNodeList() {
		String str = "prepareInputForGetShipNodeList()";
		
		AcademySIMTraceUtil.startMessage(CLASSNAME, str);
		Document getShipNodeListInputDoc = YRCXmlUtils
				.createDocument(AcademyPCAConstants.SHIPNODE_ATTR);
		Element eleRootElement = getShipNodeListInputDoc.getDocumentElement();
		eleRootElement.setAttribute(AcademyPCAConstants.SHIPNODE_ATTR,
				YRCPlatformUI.getUserElement().getAttribute(
						AcademyPCAConstants.ATTR_NODE));
		
		AcademySIMTraceUtil.endMessage(CLASSNAME,str);
		
		return getShipNodeListInputDoc;
		
		
	}*/
	
	//END: SHIN-22
	
	/**
	 * Superclass method to set the screen models for the custom API Calls
	 * and to handle failure of API calls
	 * @param context
	 * 			<br/> - the context in which the API is called
	 */
	public void handleApiCompletion(YRCApiContext context) {
		final String methodName="handleApiCompletion(context)";
		
		//START: SHIN-6
		Document shipNodeListDoc;
		Element eleshipNode;
		String strNodeType="";
		//END: SHIN-6
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		if(context.getInvokeAPIStatus()<0){
			//to handle API call failures
			
			AcademySIMTraceUtil.logMessage(context.getApiName()+" call Failed");
			AcademySIMTraceUtil.logMessage("Error Output: \n", context.getOutputXml().getDocumentElement());
		}else{
			if(context.getApiName().equals(AcademyPCAConstants.SHIPMENT_TYPE_CC_COMMAND)){
				//to handle the command "GetShipmentTypeCC"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				Document commonCodeDoc = context.getOutputXml();
				setExtentionModel(AcademyPCAConstants.EXTN_SHIPMENT_TYPE_CC_MODEL, commonCodeDoc.getDocumentElement());
			}
			if(context.getApiName().equals(AcademyPCAConstants.REPRINT_PACK_LIST_SERVICE_COMMAND)){
				//to handle the command "AcademySFSRePrintPackList"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				//setting the success message in status line
				YRCPlatformUI.setMessage(AcademyPCAConstants.REPRINT_PACK_LIST_SUCCESS_MSG);
			}
			if(context.getApiName().equals(AcademyPCAConstants.REPRINT_INVOICE_SERVICE_COMMAND)){
				//to handle the command "AcademySFSRePrintInvoice"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				//setting the success message in status line
				YRCPlatformUI.setMessage(AcademyPCAConstants.REPRINT_INVOICE_SUCCESS_MSG);
			}
			if(context.getApiName().equals(AcademyPCAConstants.REPRINT_SHIP_LABEL_SERVICE_COMMAND)){
				//to handle the command "AcademySFSRePrintShippingLabel"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				//setting the success message in status line
				YRCPlatformUI.setMessage(AcademyPCAConstants.REPRINT_SHIP_LABEL_SUCCESS_MSG);
			}
			if(context.getApiName().equals(AcademyPCAConstants.REPRINT_SHIPMENT_PICK_TICKET_SERVICE_COMMAND)){
				//to handle the command "AcademySFSPrintShipmentPickTicket"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				//setting the success message in status line
				YRCPlatformUI.setMessage(AcademyPCAConstants.SHIPMENT_PICK_TKT_SUCCESS_MSG);
			}
			if(context.getApiName().equals(AcademyPCAConstants.AUTO_CORRECT_SHPMNT_SERVICE_COMMAND)){
				//to handle the command "AcademySFSAutoCorrectShipmentError"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				//setting the success message in status line
				YRCPlatformUI.setMessage(AcademyPCAConstants.AUTO_CORRECT_SHIP_SUCCESS_MSG);
			}

			if(context.getApiName().equals(AcademyPCAConstants.AUTO_CORRECT_MANIFEST_SERVICE_COMMAND)){
				//to handle the command "AcademySFSAutoCorrectManifestError"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				//setting the success message in status line
				YRCPlatformUI.setMessage(AcademyPCAConstants.AUTO_CORRECT_MANIFEST_SUCCESS_MSG);
			}

			//STL- 685: Start
			if (context.getApiName().equals(AcademyPCAConstants.ACAD_CANCEL_SHIPMENT_SERVICE)) {
				AcademySIMTraceUtil.logMessage(methodName + " :: " + context.getApiName());
				
				
				// Button has to get invisible as shipment will be cancelled.
				setControlVisible(AcademyPCAConstants.EXTN_CANCEL_SHIPMENT, false);
				// To set the Shipment to cancelled status.
				Element eleShipmentDetailsFromModel = getModel(AcademyPCAConstants.OOB_SEARCH_SHIPMNT_DTLS_MODEL);
				Element eleStatusFromModel = YRCXmlUtils.getChildElement(eleShipmentDetailsFromModel, AcademyPCAConstants.SHIPMENT_STATUS_ELE);
				if (eleStatusFromModel != null) {
					Document changeShipmentOutDoc = context.getOutputXml();
					AcademySIMTraceUtil.logMessage("output xml" +context.getOutputXml() );
					
					Element eleStatus = YRCXmlUtils.getChildElement(changeShipmentOutDoc.getDocumentElement(), AcademyPCAConstants.SHIPMENT_STATUS_ELE);
					if (eleStatus != null) {
						eleStatusFromModel.setAttribute(AcademyPCAConstants.STATUS_DESC_ATTR, eleStatus.getAttribute(AcademyPCAConstants.STATUS_DESC_ATTR));
						repopulateModel(AcademyPCAConstants.OOB_SEARCH_SHIPMNT_DTLS_MODEL);
					}
				}
			}
			//STL- 685: End
			//Start STL-737 Changes: Print proper message when ORMD label is successfully printed 
			if (context.getApiName().equals(AcademyPCAConstants.REPRINT_ORMD_SERVICE_COMMAND)) {
				// to handle the command "AcademySFSRePrintShippingLabel"
				AcademySIMTraceUtil.logMessage(methodName + " :: " + context.getApiName());
				// setting the success message in status line
				YRCPlatformUI.setMessage(AcademyPCAConstants.REPRINT_ORMD_SUCCESS_MSG);
			}
			
			//START: SHIN-22 commented the below logic due to the post window initializer class
			
			/*//START: SHIN-11
			if (context.getApiName().equals(
					AcademyPCAConstants.GET_SHIP_NODE_LIST_COMMAND)) {
				AcademySIMTraceUtil
						.logMessage(CLASSNAME+"::handleApiCompletion(context) :: "
								+ context.getApiName());
				
				shipNodeListDoc = context.getOutputXml();
				eleshipNode = (Element) shipNodeListDoc
						.getElementsByTagName(AcademyPCAConstants.SHIPNODE_ATTR)
						.item(0);
				strNodeType = eleshipNode.getAttribute(AcademyPCAConstants.ATTR_NODE_TYPE);
			
				if (AcademyPCAConstants.ATTR_VAL_SHAREDINV_DC.equals(strNodeType)){
					
					AcademySIMTraceUtil.logMessage(CLASSNAME+"::The value is  SharedInventoryDC for ShipNode NodeType");
					AcademySIMTraceUtil.logMessage(CLASSNAME+"::NodeyType::" +strNodeType );
					shipmentDtlsElem.setAttribute(AcademyPCAConstants.ATTR_IS_SHAREDINV_DC, AcademyPCAConstants.STRING_Y);
					callCancelShipmentService();
				} else {
					AcademySIMTraceUtil.logMessage(CLASSNAME+"::The value is NOT SharedInventoryDC for ShipNode NodeType");
					callCancelShipmentService();
					
				}
			}
			//END: SHIN-11*/
			
			//END: SHIN-22
		}
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		super.handleApiCompletion(context);
	}
	/**
	 * Related task action method to reprint Shipment BOL for WG shipments
	 * @param xml
	 * 			<br/> - Editor Input XML of shipment details page
	 * @param shipmentCntrKey
	 * 			<br/> - ShipmentContainerKey selected for reprint
	 */
	/*public void processShipmentBOLRequest(Element xml, String shipmentCntrKey, String strpackStn) {
		
	}*/

	public void processShipmentBOLRequest(Element xml, String shipmentCntrKey, String strpackStn) {
		final String methodName="processShippingLabelRequest(xml, shipmentCntrKey)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		AcademySIMTraceUtil.logMessage(CLASSNAME+" : processShippingLabelRequest(Element, String):: \n", xml);
		callServiceForPrint(AcademyPCAConstants.REPRINT_SHIP_BOL_SERVICE_COMMAND, getReprintRequestDocument(xml, shipmentCntrKey, strpackStn));

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		// TODO Auto-generated method stub
		
	}
}
