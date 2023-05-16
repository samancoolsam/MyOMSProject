package com.academy.ecommerce.sterling.cancelOrder.extn;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XPathUtil;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.YRCXmlUtils;

public class AcademyCancelOrderExtnWizardBehavior extends
		YRCWizardExtensionBehavior {
	private final String pageId = "com.yantra.pca.ycd.rcp.tasks.cancelOrder.wizardpages.YCDCancelOrderSelectQuantityPage";

	/**
	 * This method initializes the behavior class.
	 */
	@Override
	public void init() {
	}

	public String getExtnNextPage(String currentPageId) {
		return null;
	}

	@Override
	public IYRCComposite createPage(String arg0) {
		return null;
	}

	@Override
	public void pageBeingDisposed(String arg0) {
	}

	/**
	 * Called when a wizard page is about to be shown for the first time.
	 * 
	 */
	@Override
	public void initPage(String pageBeingShown) {
	}

	/**
	 * Method for validating the text box.
	 */
	@Override
	public YRCValidationResponse validateTextField(String fieldName,
			String fieldValue) {
		return super.validateTextField(fieldName, fieldValue);
	}

	/**
	 * Method for validating the combo box entry.
	 */
	@Override
	public void validateComboField(String fieldName, String fieldValue) {
		super.validateComboField(fieldName, fieldValue);
	}

	/**
	 * Method called when a button is clicked.
	 */
	@Override
	public YRCValidationResponse validateButtonClick(String fieldName) {
		return super.validateButtonClick(fieldName);
	}

	@Override
	public boolean preCommand(YRCApiContext arg0) {		
		if (arg0.getApiName().equals("changeOrder")) {
			boolean isValid = false;
			try {
				YRCPlatformUI
						.trace("#### calling validateOrderLineStatus Method #############");
				isValid = validateOrderLineStatus();
				YRCPlatformUI.trace("#### is Order valid to be canceled : "
						+ isValid);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!isValid) {
				return false;
			} else {/*
					 * String[] apiNames = arg0.getApiNames(); for (int i = 0; i <
					 * apiNames.length; i++) { if
					 * (apiNames[i].equals("changeShipment")) { Document d0 =
					 * arg0.getInputXmls()[i]; YRCPlatformUI.trace("input to
					 * changeShipment " + YRCXmlUtils.getString(d0)); if
					 * (!YRCPlatformUI.isVoid(d0)) { Element eleShipment =
					 * d0.getDocumentElement(); if
					 * (eleShipment.hasAttribute("ShipmentKey")) {
					 */
				/**
				 * <Container><Shipment ShipmentKey=""/></Container>
				 */
				/*
				 * Document docConatiner = YRCXmlUtils
				 * .createDocument("Container"); Element eleContainerShipment =
				 * YRCXmlUtils .createChild(docConatiner .getDocumentElement(),
				 * "Shipment"); eleContainerShipment.setAttribute(
				 * "ShipmentKey", eleShipment .getAttribute("ShipmentKey"));
				 * YRCPlatformUI .trace("input to
				 * AcademyGetChangeShipmentContainerService" + YRCXmlUtils
				 * .getString(docConatiner)); YRCApiContext context = new
				 * YRCApiContext(); YRCApiCaller syncApiCaller = new
				 * YRCApiCaller( context, true); context
				 * .setApiName("AcademyGetChangeShipmentContainerService");
				 * context.setFormId(arg0.getFormId()); // Set input xml at API
				 * Context context.setInputXml(docConatiner);
				 * syncApiCaller.invokeApi(); Document outputOfChangeShipment =
				 * context .getOutputXml(); YRCPlatformUI .trace(" Output of
				 * AcademyGetChangeShipmentContainerService:: " + YRCXmlUtils
				 * .getString(outputOfChangeShipment)); } } } }
				 */
				// Start - #3477 - Cancel Active Shipment - part of R-023.
				
				// Check for Api Names and Input xml
				String[] apiNames0 = arg0.getApiNames();
				boolean reqInputChanges = false;
				int indx = 0;
				for (int i = 0; i <apiNames0.length; i++) {
					if(!apiNames0[i].equals("changeShipment"))
						indx++;
					else if(apiNames0[i].equals("changeShipment"))
						reqInputChanges = true;
				}
				if(reqInputChanges){
					//Modify the input xml to changeOrder API and remove changeShipment API call.
					String[] apiNames = arg0.getApiNames();
					String[] updateApiNames = new String[indx]; // changeOrder API and manageStopDeliveryRequest API
					Document[] updateInputDocs = new Document[indx];
					int j=0;
					for (int i = 0; i <apiNames.length; i++) {
						if(!apiNames[i].equals("changeShipment")){
							YRCPlatformUI.trace(" API is :: "+apiNames[i]);
							if(apiNames[i].equals("changeOrder")){
								Document cancelOrderDoc = arg0.getInputXmls()[i];
								YRCPlatformUI.trace("input xml is ::\n"+YRCXmlUtils.getString(cancelOrderDoc));
								Element orderModel = getTargetModel("CancellableOrder");
								try{
									NodeList nSelectedLines = XPathUtil.getNodeList(orderModel,"/Order/OrderLines/OrderLine[@Checked='Y']");
									int numberOfSeletedLines = nSelectedLines.getLength();
									YRCPlatformUI.trace("Number of selected lines to cancel : "+ numberOfSeletedLines);
	
									// case when "Select All Available Items" option is selected then
									// all lines need to be processed.
									if (numberOfSeletedLines == 0) {
										YRCPlatformUI.trace("Select All option is selected to cancel");
										nSelectedLines = XPathUtil.getNodeList(orderModel,"/Order/OrderLines/OrderLine");
										numberOfSeletedLines = nSelectedLines.getLength();									
									}
									
									// Temporariy work around fix for Issue# 3990 - support case 294408. Remove this work around 
									// once product fix is available
									
									// *** Start *** Issue#3990
									YRCPlatformUI.trace(" Input xml before removing notes : "+ YRCXmlUtils.getString(cancelOrderDoc));
									Element notesEle = null;
									if(cancelOrderDoc.getDocumentElement().getElementsByTagName("Note").getLength()>0) {
										notesEle=(Element)cancelOrderDoc.getDocumentElement().getElementsByTagName("Note").item(0);	
										if("YCD_CANCEL_INFO".equalsIgnoreCase(notesEle.getAttribute("ReasonCode"))) {
											//notesEle.removeAttribute("ReasonCode");
											Element eleScrnModel = getModel("CancelOrder");
											if(eleScrnModel.getElementsByTagName("Notes").getLength()>0){
												String noOfNotes = ((Element)eleScrnModel.getElementsByTagName("Notes").item(0)).getAttribute("NumberOfNotes");
												if(noOfNotes == null || (noOfNotes != null && Integer.valueOf(noOfNotes) <= 0))
													notesEle.setAttribute("SequenceNo", "1");
											}
										}
									}
									YRCPlatformUI.trace(" Input xml after removing notes : "+ YRCXmlUtils.getString(cancelOrderDoc));	
									// *** End *** Issue#3990
									
									Element orderLinesEle = null;
									if(cancelOrderDoc.getDocumentElement().getElementsByTagName("OrderLines").getLength()>0)
										orderLinesEle=(Element)cancelOrderDoc.getDocumentElement().getElementsByTagName("OrderLines").item(0);
									else
										orderLinesEle = YRCXmlUtils.createChild(cancelOrderDoc.getDocumentElement(), "OrderLines");
									YRCPlatformUI.trace("element <OrderLines> is : \n"+YRCXmlUtils.getString(orderLinesEle));
									for (int ol = 0; ol < numberOfSeletedLines; ol++) {
										Element eleSelectedLine = (Element) nSelectedLines.item(ol);
										String orderLineKey = eleSelectedLine.getAttribute("OrderLineKey");
										Element eleOrderLine = (Element)XPathUtil.getNode(orderLinesEle, "/OrderLines/OrderLine[@OrderLineKey='"+orderLineKey+"']");
										if(YRCPlatformUI.isVoid(eleOrderLine)){
											// Add a new OrderLine to OrderLines element
											eleOrderLine = YRCXmlUtils.createChild(orderLinesEle, "OrderLine");
											eleOrderLine.setAttribute("Action", "CANCEL");
											eleOrderLine.setAttribute("OrderLineKey", orderLineKey);
										}									
									}
								}catch(Exception e){
									e.printStackTrace();
								}
								YRCPlatformUI.trace("final input xml to changeOrder API is :\n"+YRCXmlUtils.getString(cancelOrderDoc));
								updateApiNames[j]=apiNames[i];
								updateInputDocs[j]=cancelOrderDoc;
								j=j+1;
							}else{
								updateApiNames[j]=apiNames[i];
								updateInputDocs[j]=arg0.getInputXmls()[i];
								j=j+1;
							}						
						}
					}
					arg0.setApiNames(updateApiNames);
					arg0.setInputXmls(updateInputDocs);
					// End - # 3477 - Cancel active shipment
				}
			}
		}
		return super.preCommand(arg0);
	}

	/*
	 * This method is called to check if the order line(s) in the order are
	 * eligible to be canceled or not
	 */
	private boolean validateOrderLineStatus() throws Exception {
		boolean isLineEligible = true;
		Element orderModel = getTargetModel("CancellableOrder");
		YRCPlatformUI.trace("Order to be Cancel" + orderModel);
		try {
			NodeList nSelectedLines = XPathUtil.getNodeList(orderModel,
					"/Order/OrderLines/OrderLine[@Checked='Y']");
			int numberOfSeletedLines = nSelectedLines.getLength();
			YRCPlatformUI
					.trace("*********** Number of selected lines *********"
							+ numberOfSeletedLines);

			// case when "Select All Available Items" option is selected then
			// all lines need to be processed.
			if (numberOfSeletedLines == 0) {
				YRCPlatformUI
						.trace("*********** Select All option is selected  *********");
				nSelectedLines = XPathUtil.getNodeList(orderModel,
						"/Order/OrderLines/OrderLine");
				numberOfSeletedLines = nSelectedLines.getLength();
			}

			for (int i = 0; i < numberOfSeletedLines; i++) {
				Element eleSelectedLine = (Element) nSelectedLines.item(i);
				String orderLineKey = eleSelectedLine
						.getAttribute("OrderLineKey");
				String lineShipmentStatus = XPathUtil
						.getString(
								orderModel,
								"/Order/OrderLines/OrderLine[@OrderLineKey=\""
										+ orderLineKey
										+ "\"]/ShipmentLines/ShipmentLine/Shipment/@Status");
				YRCPlatformUI.trace("#### line status #############"
						+ lineShipmentStatus);

				if (lineShipmentStatus.length() > 1) {
					lineShipmentStatus = lineShipmentStatus.substring(0, 4);
					int orderLineStatus = Integer.parseInt(lineShipmentStatus);

					YRCPlatformUI.trace("#### Order Line Status #############"
							+ orderLineStatus);

					// 1300 is the status code for shipment in pack process.
					// Once pack process being, lines cannot be canceled.
					if (orderLineStatus < 1300 || (orderLineStatus == 9000)) {
						isLineEligible = true;
					} else {
						isLineEligible = false;
					}
				}

				if (!isLineEligible) {
					YRCPlatformUI
							.showError(
									AcademyPCAConstants.ATTR_ERROR,
									"One or more order line(s) have already been packed or in the process of packing and cannot be canceled.");
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isLineEligible;
	}

	/**
	 * Method called when a link is clicked.
	 */
	@Override
	public YRCValidationResponse validateLinkClick(String fieldName) {
		return super.validateLinkClick(fieldName);
	}

	/**
	 * Create and return the binding data for advanced table columns added to
	 * the tables.
	 */
	@Override
	public YRCExtendedTableBindingData getExtendedTableBindingData(
			String tableName, ArrayList tableColumnNames) {
		return super.getExtendedTableBindingData(tableName, tableColumnNames);
	}

}
