
/*
 * Created on Jul 04,2012
 *
 */
package com.academy.som.shipmentDetails.screens;

import java.util.HashMap;

import javax.xml.xpath.XPathConstants;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCBaseBehavior;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCXPathUtils;
import com.yantra.yfc.rcp.YRCXmlUtils;

/**
 * Custom Behavior class done for managing the 
 * custom panel popup : "com.academy.som.shipmentDetails.screens.AcademyContainerSelectionPanelPopup" 
 * to select the container for reprinting Packslip, Shipping and Return Labels
 *
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 * Copyright © 2006-2009 Sterling Commerce, Inc. All Rights Reserved.
 */

public class AcademyContainerSelectionPanelBehavior extends YRCBehavior {

	private static String CLASSNAME = "AcademyContainerSelectionPanelBehavior";
	private String strPackslipPrinterId;
	private String strSinglePckstnCodeValue;
	private String strConcatShipNode;
	private String strCommonComboValue;
	private String strComboboxValue;
	HashMap<String, String> packMap = new HashMap<String, String>();

	/**
	 * Constructor for the behavior class.
	 * @param ownerComposite
	 * 			<br/> - Composite of Custom Panel
	 * @param formId 
	 * 			<br/> - FORM ID of Custom Panel : "com.academy.sfs.searchshipment.screens.AcademyContainerSelectionPanelPopup"
	 * @param shipmenDetails 
	 * 			<br/> - Editor Input XML of Shipment Details screen
	 */
	public AcademyContainerSelectionPanelBehavior(Composite ownerComposite, String formId, Element shipmentDetails) {
		super(ownerComposite, formId);
		final String methodName="AcademyContainerSelectionPanelBehavior(ownerComposite, formId, shipmentDetails)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		init();

		// removeReduntantShipmentElements(shipmentDetails);
		//setting the screen model of Editor Input for populating the Container Selection combo of panel
		setModel(AcademyPCAConstants.OOB_SEARCH_SHIPMNT_DTLS_MODEL, shipmentDetails);
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * This method initializes the behavior class.
	 */    
	public void init() {
		//TODO: write behavior init here
		//WMS Optmization 001 printer Id
		getCommonCodeList("MUL_PACK_STORE");

		//WMS Optmization 001 printer Id
	}

	public String getComboPackStation()
	{
		String cmbPackStn = getFieldValue("cmbPackStation");
		return cmbPackStn;
	}
	/**
	 * Method to remove reduntant Shipment Elements present in the Editor input XML  
	 * @param shipmentDetails
	 * 			<br/> - Editor Input XML of Shipment Details screen 
	 */
	/*private void removeReduntantShipmentElements(Element shipmentDetails) {
		final String methodName="removeReduntantShipmentElements(shipmentDetails)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		NodeList shipmentNL = (NodeList)YRCXPathUtils.evaluate(shipmentDetails, 
											AcademyPCAConstants.SHIPDTL_SHIPMENT_SHIPMENT_ELE_XPATH, XPathConstants.NODESET);
		Element shipmentElement$0 = (Element)shipmentNL.item(0);
		for(int listIndex = 1; listIndex<shipmentNL.getLength(); listIndex++){
			Element currShipmentElement = (Element)shipmentNL.item(listIndex);
			//checking for redundancy in Shipment elements with the first Shipment element from the NodeList
			if(currShipmentElement.getAttribute(AcademyPCAConstants.SHIPMENT_KEY_ATTR)
					.equals(shipmentElement$0.getAttribute(AcademyPCAConstants.SHIPMENT_KEY_ATTR))){
				//deleting the node from the Input if reduntant
				currShipmentElement.getParentNode().removeChild(currShipmentElement);
			}
		}


		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}*/
//	Start of WMS Optimization Printer id

	private void getCommonCodeList(String CodeType) {
		// TODO Auto-generated method stub
		AcademySIMTraceUtil.logMessage("Inside getCommonCodeList");
		YRCApiContext yrcApiContext = new YRCApiContext();
		yrcApiContext.setApiName("GetCCLForPackStation");
		Document inDoc = YRCXmlUtils.createDocument("CommonCode");		
		//Document outDocument = YRCXmlUtils.createDocument("CommonCodeList");
		yrcApiContext.setInputXml(getCommonCodeListInput(inDoc,CodeType).getOwnerDocument());
		//Element rootElement1=getCommonCodeListInput(inDoc,CodeType);
		//yrcApiContext.setInputXml(rootElement1.getOwnerDocument());
		yrcApiContext.setFormId(this.getFormId());
		AcademySIMTraceUtil.logMessage("formid"+ this.getFormId());
		callApi(yrcApiContext);
	}



	private Element getCommonCodeListInput(Document docInputgetCommonCodeList,String codeType) {
		AcademySIMTraceUtil.logMessage("Inside getCommonCodeListInput");
		final String methodName="getCommonCodeListInput(docInputgetCommonCodeList)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);		
		Element rootElement = docInputgetCommonCodeList.getDocumentElement();
		rootElement.setAttribute("CodeType", codeType);
		AcademySIMTraceUtil.logMessage("CodeType Is :" + codeType);
		AcademySIMTraceUtil.logMessage("INPUT to getCommonCodeList : " + YRCXmlUtils.getString(rootElement));
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return rootElement;
	}


	
	public void handleApiCompletion(YRCApiContext context) {
		final String methodName="handleApiCompletion(context)";

		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		if(context.getInvokeAPIStatus()<0){
			//to handle API call failures
			AcademySIMTraceUtil.logMessage(context.getApiName()+" call Failed");
			AcademySIMTraceUtil.logMessage("Error Output: \n", context.getOutputXml().getDocumentElement());
		}
		else if (context.getApiName().equals("GetCCLForPackStation")) {
			AcademySIMTraceUtil.logMessage("Inside handle api");
			AcademySIMTraceUtil.logMessage("Context API: "
					+ context.getApiName());
			
			Element reasonCodeOutput = context.getOutputXml()
					.getDocumentElement();
			AcademySIMTraceUtil.logMessage("Api Output: \n", context
					.getOutputXml().getDocumentElement());
			Element eleCommonCode = (Element) reasonCodeOutput
					.getElementsByTagName("CommonCode").item(0);
			this.strSinglePckstnCodeValue = eleCommonCode
					.getAttribute("CodeValue");
			AcademySIMTraceUtil
					.logMessage("Single PACK Station Code Value is :"
							+ this.strSinglePckstnCodeValue);
			String strCodeType = eleCommonCode.getAttribute("CodeType");
			/*if (strCodeType.equalsIgnoreCase("INV_SHRT_RSN")) {
				setExtentionModel("Extn_inv_shrt_rsn_Output",
						reasonCodeOutput);
			}*/
			if (strCodeType.equalsIgnoreCase("MUL_PACK_STORE")) {
				AcademySIMTraceUtil
						.logMessage("getCommonCodeList Output for MUL_PACK_STORE for Pack station:"
								+ YRCXmlUtils.getString(reasonCodeOutput));

				NodeList mulPckStnNL = reasonCodeOutput
						.getElementsByTagName("CommonCode");
				Element eleMulPckStnCommonCode;
				for (int i = 0; i < mulPckStnNL.getLength(); i++) {
					eleMulPckStnCommonCode = (Element) mulPckStnNL.item(i);
					AcademySIMTraceUtil
							.logMessage("inside for loop for Pack station"
									+ YRCXmlUtils
											.getString(eleMulPckStnCommonCode));
					String strCommonCodeValue = eleMulPckStnCommonCode
							.getAttribute("CodeValue");
					AcademySIMTraceUtil
							.logMessage("Multiple PACK Station Code Value is :"
									+ strCommonCodeValue);
					String strCommonCodeShrDes = eleMulPckStnCommonCode
							.getAttribute("CodeShortDescription");
					AcademySIMTraceUtil
							.logMessage("Multiple PACK Station Description is :"
									+ strCommonCodeShrDes);
					this.packMap.put(strCommonCodeValue,
							strCommonCodeShrDes);
					AcademySIMTraceUtil
							.logMessage("Hash MAP Value for Pack station :"
									+ this.packMap);
				}
				for (String key : this.packMap.keySet()) {
					String value = (String) this.packMap.get(key);
					AcademySIMTraceUtil.logMessage(key + " " + value);
				}
				String shipNodeFromInput = YRCPlatformUI.getUserElement()
						.getAttribute("ShipNode");
				AcademySIMTraceUtil.logMessage("ShipNode Value is::"
						+ shipNodeFromInput);
				if (this.packMap.containsKey(shipNodeFromInput)) {
					this.strConcatShipNode = shipNodeFromInput
							.concat("_PACK_STN");
					AcademySIMTraceUtil
							.logMessage("CommonCodeType for Pack station Is::"
									+ this.strConcatShipNode);
					getCommonCodeList(this.strConcatShipNode);
				} else {
					AcademySIMTraceUtil
							.logMessage("CommonCodeType Is::PACK_PRINTER_ID");
					getCommonCodeList("PACK_PRINTER_ID");
					/*hideField("cmbPackStation", this);
					hideField("cmbPackStation", this);*/
					AcademySIMTraceUtil
							.logMessage("The Node User has currently logged in has Single Pack Station:"
									+ YRCXmlUtils
											.getString(reasonCodeOutput));
				}

				AcademySIMTraceUtil.logMessage("concatenated string"
						+ this.strConcatShipNode);
			} else if (strCodeType.equalsIgnoreCase(this.strConcatShipNode)) {
				setModel("Extn_PackSlip_PrinterID_Output",
						reasonCodeOutput);
				AcademySIMTraceUtil
						.logMessage("getCommonCodeList Output With Multiple PAckStation"
								+ reasonCodeOutput);
			}

			AcademySIMTraceUtil
					.logMessage("The Node User has currently logged in has Single Pack Station:"
							+ YRCXmlUtils.getString(reasonCodeOutput));
			setModel("Extn_PackSlip_PrinterID_Output",
					reasonCodeOutput);
		}
	}
	public static void hideField(String fieldName, Object extnBehavior) {
		GridData gridData = new GridData();
		gridData.exclude = true;
		YRCBaseBehavior parentBehavior = (YRCBaseBehavior) extnBehavior;
		parentBehavior.setControlVisible(fieldName, false);
		parentBehavior.setControlLayoutData(fieldName, gridData);
	}
//	End of WMS Optimization Printer id
}
