package com.academy.ecommerce.sterling.customerAppeasement.extn;

/**
 * Created on Oct 06,2009
 *
 */

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyConstants;
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.academy.ecommerce.sterling.util.XPathUtil;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.YRCXmlUtils;

/**
 * @author schandran Copyright © 2005-2008 Sterling Commerce, Inc. All Rights
 *         Reserved.
 */
public class AcademyCustomerAppeasementExtnWizardBehavior extends
		YRCWizardExtensionBehavior {
	private final String pageId = "com.yantra.pca.ycd.rcp.tasks.customerAppeasement.wizardpages.YCDCustomerAppeasementSelectReasons";

	private Element orderModel;

	private Element linePriceInfo;

	private Element changeOrderForGC;
	
	boolean giftCard = true;

	private boolean b;

	private String strAppeasement;

	private StringBuffer errorMessage;

	/**
	 * This method initializes the behavior class.
	 */
	public void init() {
		
	}

	public String getExtnNextPage(String currentPageId) {
		// TODO
		return null;
	}

	public IYRCComposite createPage(String pageIdToBeShown) {
		// TODO
		return null;
	}

	public void pageBeingDisposed(String pageToBeDisposed) {
		// TODO
	}

	/**
	 * Called when a wizard page is about to be shown for the first time.
	 * 
	 */
	public void initPage(String pageBeingShown) {
		
	}

	/**
	 * Method for validating the text box.
	 */
	public YRCValidationResponse validateTextField(String fieldName,
			String fieldValue) {
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
	public YRCValidationResponse validateButtonClick(String fieldName) {
		//START: STL-1129. User will not receive 0.00 balance Gift Card as Appeasement
		if (fieldName.equals("btnConfirm")) {
			try {
				Element eleAppeasementOffer = (Element)XPathUtil.getNode(getModel(AcademyPCAConstants.MODEL_APPEASMENT_OFFERS), AcademyPCAConstants.XPATH_AppeasementOffer);
				double dOfferAmount = XMLUtil.getDoubleAttribute(eleAppeasementOffer, AcademyPCAConstants.ATTR_OFFER_AMOUNT);
				YRCPlatformUI.trace("Appeasement Details: \t"+XMLUtil.getElementXMLString(eleAppeasementOffer));
				
				if(dOfferAmount <= 0){
					YRCPlatformUI.showError(AcademyPCAConstants.ATTR_ERROR, "ERROR_NO_CUSTOMER_APPEASEMENT_FOR_ZERO_AMOUNT");
					return new YRCValidationResponse(YRCValidationResponse.YRC_VALIDATION_ERROR, "ERROR_NO_CUSTOMER_APPEASEMENT_FOR_ZERO_AMOUNT");
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		//END: STL-1129. User will not receive 0.00 balance Gift Card as Appeasement
		
		if (fieldName.equals("btnNext")) {
			try {
				Element prevAppOffers = getExtentionModel("extn_ExistingAppeasementsForOrder");
				String sReasonCode = getFieldValue("cmbAppeasementReason");
				String sCodeLongDesc = XPathUtil.getString(getModel("AppeasementReasons"), "CommonCode[@CodeValue='" + sReasonCode + "']/@CodeLongDescription");
				strAppeasement = sCodeLongDesc;
				if (!YRCPlatformUI.isVoid(sCodeLongDesc)) 
				{
					b=false;
					validateOrderLineStatusToProvideAppeasement();
					if (!b) {
						if (sCodeLongDesc.equals("Shipping")) {
							Element orderElem = getModel("OrderDetails");
							NodeList listofSelectedLines = XPathUtil
									.getNodeList(orderElem,
											"/Order/OrderLines/OrderLine[@Checked='Y']");
							int len = listofSelectedLines.getLength();
							int countofwglines = 0;
							int countofNonwglines = 0;
							int countofShippedLines = 0;
							int countofUnShippedLines = 0;
							if (len > 0) {
								for (int i = 0; i < len; i++) {
									Element currSelectedLine = (Element) listofSelectedLines
											.item(i);
									Element itemElem = (Element) currSelectedLine
											.getElementsByTagName(
													AcademyPCAConstants.ATTR_ITEM_DETAILS)
											.item(0);
									if (itemElem != null
											&& itemElem.hasChildNodes()) {
										Element itemExtnElem = (Element) itemElem
												.getElementsByTagName(
														AcademyPCAConstants.ATTR_EXTN)
												.item(0);
										if (itemExtnElem != null
												&& itemExtnElem.hasAttributes()) {
											if ((AcademyPCAConstants.ATTR_Y)
													.equals(itemExtnElem
															.getAttribute(AcademyPCAConstants.ATTR_ITEM_IS_WHITE_GLOVE))) {
												countofNonwglines++;
											} else {
												countofwglines++;
											}
										}
									}
									double status = Double
											.parseDouble(currSelectedLine
													.getAttribute("MaxLineStatus"));
									if (status >= 3700) {
										countofShippedLines++;
									} else {
										countofUnShippedLines++;
									}
									if (countofNonwglines > 0
											&& countofwglines > 0) {

										YRCPlatformUI.showError(AcademyPCAConstants.ATTR_ERROR, "Please select only white-glove lines or only non white-glove lines");
										return new YRCValidationResponse(YRCValidationResponse.YRC_VALIDATION_ERROR, "Please select only white-glove lines or only non white-glove lines");
									}
									if (countofShippedLines > 0
											&& countofUnShippedLines > 0) {

										YRCPlatformUI.showError(AcademyPCAConstants.ATTR_ERROR, "You cannot select Shipped and UnShipped lines together");
										return new YRCValidationResponse(YRCValidationResponse.YRC_VALIDATION_ERROR, "You cannot select Shipped and UnShipped lines together");
									}
								}
							}
						}

						// Logic for validating if an appeasement has already given
						if (isAppeasementAlreadyGiven(sCodeLongDesc)) 
						{
							YRCPlatformUI.showError(AcademyPCAConstants.ATTR_ERROR, errorMessage.toString());
							return new YRCValidationResponse(YRCValidationResponse.YRC_VALIDATION_ERROR, errorMessage.toString());
						}
					}
					else 
					{
						return new YRCValidationResponse(YRCValidationResponse.YRC_VALIDATION_ERROR, "One or more order line(s) have not been shipped.Please select only shipped lines to provide appeasement");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		return super.validateButtonClick(fieldName);
	}

	/*
	 * This method is called to check if the orderline(s) in the order are in
	 * shipped status to provide appeasement.Only shipped lines will be eligible
	 * for appeasement.This is part of CR#51 fix.
	 */
	private boolean validateOrderLineStatusToProvideAppeasement() throws Exception 
	{
		boolean isSOFOrder = false;//WN-2327 Not able to create appeasement for SOF orders 
		Element linesShipmentStatusElement = getModel("extn_ShipmentStatusOfOrderlines");
		YRCPlatformUI.trace("#### output doc #############" + YRCXmlUtils.getString(linesShipmentStatusElement));
		YRCPlatformUI.trace("####validateOrderLineStatusToProvideAppeasement Method#############");
		try {
			
				//Start WN-2327 Not able to create appeasement for SOF orders
				NodeList nSOFLine = XPathUtil.getNodeList(linesShipmentStatusElement, AcademyPCAConstants.XPATH_SOF_ORDERLINES);
				if(nSOFLine.getLength()>0){
					isSOFOrder = true;
				}
				//End WN-2327 Not able to create appeasement for SOF orders
				
				NodeList nSelectedLines = XPathUtil.getNodeList(orderModel, "/Order/OrderLines/OrderLine[@Checked='Y']");
				for (int i = 0; i < nSelectedLines.getLength(); i++) 
				{
					YRCPlatformUI.trace("#### input #############" + XMLUtil.getElementXMLString(linesShipmentStatusElement));
					Element eleSelectedLine = (Element) nSelectedLines.item(i);
					String orderLineKey = eleSelectedLine.getAttribute("OrderLineKey");
					String strLineType = eleSelectedLine.getAttribute("LineType");
					
					NodeList shipmentLines = XPathUtil.getNodeList(linesShipmentStatusElement, "/Order/OrderLines/OrderLine[@OrderLineKey=\"" + orderLineKey + "\"]/ShipmentLines/ShipmentLine[Shipment/@Status!='9000']");
					int numberOfShipmentLines = shipmentLines.getLength();
					if(numberOfShipmentLines<=0)
						b= true;
					
					for(int j=0; j<numberOfShipmentLines; j++)
					{
						Element shipmentLine = (Element) shipmentLines.item(j);
						NodeList shipmentNode = shipmentLine.getElementsByTagName("Shipment");
						String lineShipmentStatus = ((Element)shipmentNode.item(0)).getAttribute("Status");
						
						YRCPlatformUI.trace("#### line status #############" + lineShipmentStatus);

						// if the order is not shipped yet then line shipment status will be blank
						if(lineShipmentStatus.length() > 1)
						{
							// need to remove multiple decimals from status otherwise it will throw 
							// number format exception while parsing Double value in next step
							if(lineShipmentStatus.length()>8)
							{
								lineShipmentStatus = lineShipmentStatus.substring(0,8);
							}

							double iMinOrderLineStatus = Double.parseDouble(lineShipmentStatus);
							
							// 9000 is for shipment canceled status. So if status is shipment canceled then ignore it
							if(iMinOrderLineStatus == 9000)
							{
								// if there are only shipment canceled lines then show error (this happens in case of backordered status)
								if(j == numberOfShipmentLines -1)
								{
									b = true;
									break;
								}
								else
								{
									continue;
								}
							}
							
							YRCPlatformUI.trace("#### Min Order Line Status #############" + iMinOrderLineStatus);
							

							//Start WN-2327 Not able to create appeasement for SOF orders
							//1600.002 is the status code for Shipment Invoiced.  
							//SOF Orders have POs which will not go to Invoiced 1600.002 status.
							if (isSOFOrder && iMinOrderLineStatus < 1600) {
								b = true;
							}
							else if (!isSOFOrder && iMinOrderLineStatus < 1600.002) 
							//if (iMinOrderLineStatus < 1600.002) 
							//End WN-2327 Not able to create appeasement for SOF orders
								
							{
								b = true;
							} 
							else 
							{
								b = false;
							}
						}
						else
						{
							b = true;
						}
					}

					if (b) 
					{
						YRCPlatformUI.showError(AcademyPCAConstants.ATTR_ERROR, "One or more order line(s) have not been shipped. Please select only shipped lines to provide appeasement");
						break;
					}

				}
				return b;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// return new
			// YRCValidationResponse(YRCValidationResponse.YRC_VALIDATION_OK,"Validation
			// is successfull");
		return b;
	}

	private boolean isAppeasementAlreadyGiven(String codeLongDesc) {
		boolean retVal = false;
		Element orderElem = getModel("OrderDetails");
		Element appeasementsForOrder = getExtentionModel("extn_ExistingAppeasementsForOrder");
		Element userNamespc = getModel("UserNameSpace");
		String userid = userNamespc.getAttribute("Loginid");
		NodeList listofSelectedLines;
		try {
			listofSelectedLines = XPathUtil.getNodeList(orderElem, "/Order/OrderLines/OrderLine[@Checked='Y']");
			int len = listofSelectedLines.getLength();
			YRCPlatformUI.trace("number of selected lines : " + len);
			if (len > 0) 
			{
				for (int i = 0; i < len; i++) 
				{
					Element currSelectedLine = (Element) listofSelectedLines.item(i);
					YRCPlatformUI.trace("current selected line : " + XMLUtil.getElementXMLString(currSelectedLine));
					String orderLineKey = currSelectedLine.getAttribute("OrderLineKey");

					StringBuffer xpath = new StringBuffer();
					errorMessage = new StringBuffer();

					YRCPlatformUI.trace("appeasement reason : " + codeLongDesc);
					if(codeLongDesc.equals("Shipping"))
					{
						// if 100% shipping appeasement has already been given, then don't allow 
						// shipping appeasement anymore
						StringBuffer xpathForShipping = new StringBuffer();
						xpathForShipping.append("ACADOrderAppeasement[@AcademyOrderLineKey='");
						xpathForShipping.append(orderLineKey);
						xpathForShipping.append("' and @AppeasementPercent='100.00'");
						xpathForShipping.append(" and @AppeasementReason='");
						xpathForShipping.append(codeLongDesc);
						xpathForShipping.append("']");

						YRCPlatformUI.trace("xpath : " + xpathForShipping.toString());
						NodeList listofAppeasements = XPathUtil.getNodeList(appeasementsForOrder, xpathForShipping.toString());
						if (listofAppeasements != null) 
						{
							if (listofAppeasements.getLength() > 0) 
							{
								errorMessage.append("An appeasement of category ");
								errorMessage.append(codeLongDesc);
								errorMessage.append(" has already been given on the Order.");
								errorMessage.append(" Please select a different line or a different appeasement category.");
								YRCPlatformUI.trace("error : " + errorMessage.toString());
								return true;
							}
						}
					}
					
					YRCPlatformUI.trace("appeasement reason : " + codeLongDesc);
					// if the same user has given this appeasement before then 
					// dont allow the same user to give appeasement again
					xpath.append("ACADOrderAppeasement[@AcademyOrderLineKey='");
					xpath.append(orderLineKey);
					xpath.append("' and @UserId='");
					xpath.append(userid);
					xpath.append("' and @AppeasementReason='");
					xpath.append(codeLongDesc);
					xpath.append("']");
					YRCPlatformUI.trace("xpath : " + xpath.toString());
					
					NodeList listofAppeasements = XPathUtil.getNodeList(appeasementsForOrder, xpath.toString());
					if (listofAppeasements != null) 
					{
						if (listofAppeasements.getLength() > 0) 
						{
							errorMessage.append("An appeasement of category ");
							errorMessage.append(codeLongDesc);
							errorMessage.append(" has already been given on the Order by this user.");
							errorMessage.append(" Please select a different line or a different appeasement category.");
							YRCPlatformUI.trace("error : " + errorMessage.toString());
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retVal;
	}

	@Override
	public boolean preCommand(YRCApiContext ctx) {
		if (ctx.getApiName().equals("invokeGetAppeasementOffersUE")) {
			try {
				YRCPlatformUI.trace("Inside invokeGetAppeasementOffersUE block");
				Element inputXML = ctx.getInputXml().getDocumentElement();
				
				//  CR #2683 incorporated by setting a flag to indicate immediate appeasement has to be stopped
				
				Element orderDetails = getModel("OrderDetails");
				
				
				Element paymentMethods = (Element) orderDetails
				.getElementsByTagName("PaymentMethods").item(0);
				
				NodeList paymentMethodList = paymentMethods.getElementsByTagName("PaymentMethod");
				
				String preventImmAppeasementtForGCTender=AcademyConstants.STR_NO;
				
				for(int i=0;i< paymentMethodList.getLength();i++)
				{
					Element paymentMethod = (Element)paymentMethodList.item(i);
					YRCPlatformUI.trace("Payment method is "+XMLUtil.getElementXMLString(paymentMethod));
					String paymentType = paymentMethod.getAttribute("PaymentType");
					if("GIFT_CARD".equalsIgnoreCase(paymentType))
					{
						preventImmAppeasementtForGCTender=AcademyConstants.STR_YES;
						break;
					}
				}
				
				
				
				Element eXmldata = (Element) inputXML.getElementsByTagName(
						"XMLData").item(0);
				Element eAppeasementOffers = (Element) eXmldata
						.getElementsByTagName("AppeasementOffers").item(0);
				Element eOrder = (Element) eAppeasementOffers
						.getElementsByTagName("Order").item(0);
				Element eAppeasementReason = (Element) eOrder
						.getElementsByTagName("AppeasementReason").item(0);
				String sReasonCode = eAppeasementReason
						.getAttribute("ReasonCode");

				String sCodeLongDesc = XPathUtil.getString(
						getModel("AppeasementReasons"),
						"CommonCode[@CodeValue='" + sReasonCode
								+ "']/@CodeLongDescription");
				eAppeasementReason.setAttribute("CodeLongDescription",
						sCodeLongDesc);				
				eOrder.setAttribute("PreventImmedeateAppeasement", preventImmAppeasementtForGCTender);
				
				
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (ctx.getApiName().equals(
				"invokeSendFutureOrderCustomerAppeasementUE")) {
			Element orderLines = ctx.getInputXml().createElement("OrderLines");
			Element input = (Element) ctx.getInputXml().getElementsByTagName(
					"Order").item(0);
			String sReasonCode = getFieldValue("cmbAppeasementReason");
			String sCodeLongDesc = "";
			String sCodeShrtDesc = "";
			try {
				sCodeLongDesc = XPathUtil.getString(
						getModel("AppeasementReasons"),
						"CommonCode[@CodeValue='" + sReasonCode
								+ "']/@CodeLongDescription");
				sCodeShrtDesc = XPathUtil.getString(
						getModel("AppeasementReasons"),
						"CommonCode[@CodeValue='" + sReasonCode
								+ "']/@CodeShortDescription");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			input.setAttribute("AppeasementCategory", sCodeLongDesc);
			input.setAttribute("AppeasementDescription", sCodeShrtDesc);
			input.appendChild(orderLines);
			appendSelectedOrderLines(ctx.getInputXml(), orderLines);
			appendNotesOnOrder(input);
			YRCXmlUtils.getString(ctx.getInputXml());
		}
		for (int i = 0; i < ctx.getInputXmls().length; i++) {
			if (ctx.getApiNames()[i].equals("recordInvoiceCreation")) {
				linePriceInfo = ctx.getInputXmls()[i].getDocumentElement();
				YRCXmlUtils.getString(ctx.getInputXmls()[i]);
				giftCard = false;
			}
			if (ctx.getApiNames()[i].equals("changeOrder")) {
				
				YRCPlatformUI.trace(YRCXmlUtils.getString(ctx.getInputXmls()[i]));
				if (!giftCard) {
					modifyChangeOrderForNonGiftCards(ctx.getInputXmls()[i]);
					ctx.getInputXmls()[i].getDocumentElement().setAttribute(
							"IsGiftCard", "N");
					appendAppeasementRecords(ctx.getInputXmls()[i]);
					appendOrderNo(ctx.getInputXmls()[i]);
				} else {
					ctx.getInputXmls()[i].getDocumentElement().setAttribute(
							"IsGiftCard", "Y");
					changeOrderForGC = ctx.getInputXmls()[i]
							.getDocumentElement();

				}
				YRCXmlUtils.getString(ctx.getInputXmls()[i]);

			}

		}
		
		
		return super.preCommand(ctx);
	}

	private void appendNotesOnOrder(Element input) 
	{
		Element notesElem = (Element) changeOrderForGC.getElementsByTagName("Notes").item(0);
		
		// Fix for Defect 2087 : [CHANGE] Appeasement as Gift Card does not show the dollar amount in Notes
		try
		{
			String appeasementOfferAmount = YRCXmlUtils.getAttributeValue(input, "/Order/AppeasementOffer/@OfferAmount");
			String noteText = YRCXmlUtils.getAttributeValue(notesElem, "/Notes/Note/@NoteText");
			StringBuffer finalNoteText = new StringBuffer();
			int lastIndex = noteText.lastIndexOf("%");
			finalNoteText.append(noteText.substring(0, lastIndex + 1));
			finalNoteText.append(" ($");
			finalNoteText.append(appeasementOfferAmount);
			finalNoteText.append(")");
			finalNoteText.append(noteText.substring(lastIndex + 1, noteText.length()));
			
			Element noteChildElem = (Element) notesElem.getElementsByTagName("Note").item(0);
			noteChildElem.setAttribute("NoteText", finalNoteText.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Node tempNode = notesElem.cloneNode(true);
		XMLUtil.importElement(input, (Element) tempNode);
		// input.appendChild(tempNode);
	}

	private void appendOrderNo(Document document) {
		Element orderElem = getModel("OrderDetails");
		if (!YRCPlatformUI.isVoid(orderElem.getAttribute("OrderNo"))) {
			document.getDocumentElement().setAttribute("OrderNo",
					orderElem.getAttribute("OrderNo"));

		}

	}

	private void appendSelectedOrderLines(Document inputXml, Element orderLines) {
		try {
			Element orderElem = getModel("OrderDetails");
			NodeList listofSelectedLines = XPathUtil.getNodeList(orderElem,
					"/Order/OrderLines/OrderLine[@Checked='Y']");
			if (listofSelectedLines != null) {
				int len = listofSelectedLines.getLength();
				if (len > 0) {
					for (int i = 0; i < len; i++) {
						Element currOrderLine = (Element) listofSelectedLines
								.item(i);
						YRCXmlUtils.importElement(orderLines, currOrderLine);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void appendAppeasementRecords(Document changeOrdeDocument) {
		/*
		 * <ACADOrderAppeasementList> <ACADOrderAppeasement
		 * AcademyOrderHeaderKey="" AcademyOrderLineKey="" UserId=""
		 * AppeasementReason="" AppeasementPercent="" AppeasementAmount=""/>
		 * </ACADOrderAppeasementList>
		 */
		try {

			Element rootElem = changeOrdeDocument.getDocumentElement();
			String orderheaderKey = rootElem.getAttribute("OrderHeaderKey");
			Element userNamespc = getModel("UserNameSpace");
			Element eleOrderDetails = getModel("OrderDetails");

			String strUsername = userNamespc.getAttribute("Username");
			String strOrderNo = eleOrderDetails.getAttribute("OrderNo");

			String userid = userNamespc.getAttribute("Loginid");
			String sReasonCode = getFieldValue("cmbAppeasementReason");
			Element selectedAppeasementOffer = (Element) XPathUtil.getNode(
					getModel("AppeasementOffers"),
					"AppeasementOffer[@Checked='Y']");
			String sCodeLongDesc = XPathUtil.getString(
					getModel("AppeasementReasons"), "CommonCode[@CodeValue='"
							+ sReasonCode + "']/@CodeLongDescription");
			String sCodeShrtDesc = XPathUtil.getString(
					getModel("AppeasementReasons"), "CommonCode[@CodeValue='"
							+ sReasonCode + "']/@CodeShortDescription");
			Element extnElem = changeOrdeDocument.createElement("Extn");
			rootElem.appendChild(extnElem);
			Element acadOrderAppListElem = changeOrdeDocument
					.createElement("ACADOrderAppeasementList");
			extnElem.appendChild(acadOrderAppListElem);
			NodeList orderLineList = rootElem.getElementsByTagName("OrderLine");
			if (orderLineList != null && selectedAppeasementOffer != null) {
				if (orderLineList.getLength() > 0) {
					for (int i = 0; i < orderLineList.getLength(); i++) {
						Element currOrderLineElem = (Element) orderLineList
								.item(i);
						Element lineCharge = (Element) XPathUtil
								.getNode(currOrderLineElem,
										"LineCharges/LineCharge[@ChargeCategory='CUSTOMER_APPEASEMENT']");
						String lineamnt = lineCharge
								.getAttribute("ChargePerLine");
						Element acadOrdAppeasement = changeOrdeDocument
								.createElement("ACADOrderAppeasement");
						acadOrdAppeasement.setAttribute(
								"AcademyOrderHeaderKey", orderheaderKey);
						acadOrdAppeasement.setAttribute("AcademyOrderLineKey",
								currOrderLineElem.getAttribute("OrderLineKey"));
						acadOrdAppeasement.setAttribute("UserId", userid);

						acadOrdAppeasement
								.setAttribute("UserName", strUsername);
						acadOrdAppeasement.setAttribute("OrderNumber",
								strOrderNo);

						acadOrdAppeasement.setAttribute("AppeasementReason",
								sCodeLongDesc);
						acadOrdAppeasement.setAttribute(
								"AppeasementDescription", sCodeShrtDesc);
						acadOrdAppeasement.setAttribute("AppeasementAmount",
								lineamnt);
						acadOrdAppeasement.setAttribute("AppeasementPercent",
								selectedAppeasementOffer
										.getAttribute("DiscountPercent"));
						acadOrdAppeasement.setAttribute("OfferType",
								selectedAppeasementOffer
										.getAttribute("OfferType"));
						acadOrderAppListElem.appendChild(acadOrdAppeasement);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void modifyChangeOrderForNonGiftCards(Document document) {
		/**
		 * <OrderInvoice InvoiceType="CREDIT_MEMO"
		 * OrderHeaderKey="2009052512255486673" UseOrderLineCharges="N">
		 * <LineDetails> <LineDetail OrderLineKey="2009052512262586674"
		 * Quantity="1.00"> <LineChargeList> <LineCharge
		 * ChargeCategory="CUSTOMER_APPEASEMENT"
		 * ChargeName="CUSTOMER_APPEASEMENT" ChargePerLine="0.00"/>
		 * </LineChargeList> </LineDetail> </LineDetails> <HeaderChargeList>
		 * <HeaderCharge ChargeAmount="0.00"
		 * ChargeCategory="CUSTOMER_APPEASEMENT"
		 * ChargeName="CUSTOMER_APPEASEMENT"/> </HeaderChargeList>
		 * </OrderInvoice>
		 */
		YRCXmlUtils.getString(linePriceInfo);
		
		
		Element orderElem = getModel("OrderDetails");
		Element orderLines = YRCXmlUtils.createChild(document
				.getDocumentElement(), "OrderLines");
		stampLineChargeOnChangeOrderInput(linePriceInfo, orderLines, orderElem);

	}

	private void stampLineChargeOnChangeOrderInput(Element linePriceInfo,
			Element changeOrderorderLines, Element orderElem) {
		/**
		 * <OrderInvoice InvoiceType="CREDIT_MEMO"
		 * OrderHeaderKey="2009052512255486673" UseOrderLineCharges="N">
		 * <LineDetails> <LineDetail OrderLineKey="2009052512262586674"
		 * Quantity="1.00"> <LineChargeList> <LineCharge
		 * ChargeCategory="CUSTOMER_APPEASEMENT"
		 * ChargeName="CUSTOMER_APPEASEMENT" ChargePerLine="0.00"/>
		 * </LineChargeList> </LineDetail> </LineDetails> <HeaderChargeList>
		 * <HeaderCharge ChargeAmount="0.00"
		 * ChargeCategory="CUSTOMER_APPEASEMENT"
		 * ChargeName="CUSTOMER_APPEASEMENT"/> </HeaderChargeList>
		 * </OrderInvoice>
		 */
		Element lineDetails = YRCXmlUtils.getChildElement(linePriceInfo,
				"LineDetails");
		// Element lineChargeList=YRCXmlUtils.getChildElement(lineDetails,
		// "LineChargeList");
		try {
			NodeList lineDetailList = linePriceInfo
					.getElementsByTagName("LineDetail");
			for (int i = 0; i < lineDetailList.getLength(); i++) {
				Element lineDetail = (Element) lineDetailList.item(i);
				
				YRCPlatformUI.trace("line Detail" +YRCXmlUtils.getString(lineDetail));
				Element lineCharge = (Element) XPathUtil
						.getNode(lineDetail,
								"LineChargeList/LineCharge[@ChargeCategory='CUSTOMER_APPEASEMENT' and @ChargeName='"+ this.strAppeasement+"']");
										

				// String chargeAmount=lineCharge.getAttribute("ChargePerLine");
				Element orderLine = YRCXmlUtils.createChild(
						changeOrderorderLines, "OrderLine");
				orderLine.setAttribute("Action", "MODIFY");
				orderLine.setAttribute("OrderLineKey", lineDetail
						.getAttribute("OrderLineKey"));
				stampLineStatus(orderLine, orderElem, lineDetail
						.getAttribute("OrderLineKey"));
				String existingDiscount = getExistingDiscountOnOrder(lineDetail
						.getAttribute("OrderLineKey"));
				Element lineCharges = YRCXmlUtils.createChild(orderLine,
						"LineCharges");
				//Added as part of Upgrade: Start
				Double updatedChargePerLine = Double.parseDouble(lineCharge.getAttribute("ChargePerLine")) * Double.parseDouble("-1.0");
				YRCPlatformUI.trace("updatedChargePerLine"+ updatedChargePerLine);
				if (!YRCPlatformUI.isVoid(existingDiscount)) {
					lineCharge.setAttribute("ChargePerLine", Double.toString(Double.parseDouble(existingDiscount)+ updatedChargePerLine));
				}
				/* STL - 747 : Changed the Below IF Condition to Else IF Condition to consider the already Existing Discount 
				on OrderLine While giving multiple Appeasements on OrderLine/Order  */
				
				else if(!YRCPlatformUI.isVoid(lineCharge.getAttribute("ChargePerLine")))
				{
					lineCharge.setAttribute("ChargePerLine",Double.toString(updatedChargePerLine));
				}
					
				//Added as part of Upgrade: End
				String strReasonCode =((Element)getTargetModel("SelectedReasons")).getAttribute("ReasonCode");
				
				lineCharge.setAttribute("Reference",strReasonCode);
				YRCXmlUtils.importElement(lineCharges, lineCharge);

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getExistingDiscountOnOrder(String orderLineKey) {
		String existingDiscount = "";
		try {
			Element currOrderLine = (Element) XPathUtil.getNode(
					getModel("OrderDetails"),
					"/Order/OrderLines/OrderLine[@OrderLineKey='"
							+ orderLineKey + "']");
			if (!YRCPlatformUI.isVoid(currOrderLine)) {
				existingDiscount = XPathUtil
						.getString(currOrderLine,
								"LineCharges/LineCharge[@ChargeCategory='CUSTOMER_APPEASEMENT'and @ChargeName='"+strAppeasement+"']/@ChargeAmount");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return existingDiscount;
	}

	private void stampLineStatus(Element orderLine, Element orderElem,
			String orderLineKey) {
		try {
			String minLineStatus = XPathUtil.getString(orderElem,
					"/Order/OrderLines/OrderLine[@OrderLineKey='"
							+ orderLineKey + "']/@MinLineStatus");
			String maxLineStatus = XPathUtil.getString(orderElem,
					"/Order/OrderLines/OrderLine[@OrderLineKey='"
							+ orderLineKey + "']/@MaxLineStatus");
			orderLine.setAttribute("MinLineStatus", minLineStatus);
			orderLine.setAttribute("MaxLineStatus", maxLineStatus);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void modifyChangeOrderAndCreateFreshOrder() {
		// TODO Auto-generated method stub

	}

	private boolean checkIfGiftCardAppeasement() {
		// TODO Auto-generated method stub
		YRCPlatformUI.trace("ab");
		return false;
	}

	@Override
	public void postSetModel(String modelName) {
		if ("OrderDetails".equalsIgnoreCase(modelName)) {
			getExistingAppeasementsForOrder();
			getOrderLinesShipmentStatus();
		}
		/*
		 * if(modelName.equals("AppeasementOffers")){ String formId=getFormId();
		 * try { Element eAppeasementOffers=getModel("AppeasementOffers");
		 * NodeList
		 * lAppeasementOffer=eAppeasementOffers.getElementsByTagName("AppeasementOffer");
		 * Element eGCAppeasementOffer=(Element) lAppeasementOffer.item(0);
		 * if(eGCAppeasementOffer.getAttribute("OfferType").equals("VARIABLE_AMOUNT_ORDER")){
		 * String sReasonCode=getFieldValue("cmbAppeasementReason"); //String
		 * sCodeLongDesc; String sCodeLongDesc =
		 * XPathUtil.getString(getModel("AppeasementReasons"),
		 * "CommonCode[@CodeValue='"+sReasonCode+"']/@CodeLongDescription");
		 * if(!YRCPlatformUI.isVoid(sCodeLongDesc)){
		 * if(sCodeLongDesc.equals("Merchandise")){ StringBuffer st=new
		 * StringBuffer(); st.append("GC Appeasement upto "); st.append(25);
		 * st.append("%"); eGCAppeasementOffer.setAttribute("ExtnDescription",
		 * st.toString());
		 * System.out.println(XMLUtil.serialize(getModel("AppeasementOffers"))); } } }
		 * 
		 * }catch (Exception e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } repopulateModel("AppeasementOffers"); }
		 */
		if (modelName.equals("AppeasementReasons")) {
			Element commonCodeList = getModel("AppeasementReasons");
			NodeList commonCdList = commonCodeList
					.getElementsByTagName("CommonCode");
			if (commonCdList != null) {
				if (commonCdList.getLength() > 0) {
					for (int i = 0; i < commonCdList.getLength(); i++) {
						Element currCommonCodeElem = (Element) commonCdList
								.item(i);
						String newDesc = currCommonCodeElem
								.getAttribute("CodeValue")
								+ "_"
								+ currCommonCodeElem
										.getAttribute("CodeShortDescription")
								+ "_"
								+ currCommonCodeElem
										.getAttribute("CodeLongDescription");
						currCommonCodeElem.setAttribute("CodeShortDescription",
								newDesc);
					}
				}
			}
			repopulateModel("AppeasementReasons");
		}
		super.postSetModel(modelName);
	}

	private void getOrderLinesShipmentStatus()
	{
		setOrderDetailsModel();
		YRCPlatformUI.trace("######### in getOrderLineShipStatus method ######");
		Document doc = YRCXmlUtils.createDocument(AcademyPCAConstants.ATTR_ORDER);
		doc.getDocumentElement().setAttribute("OrderHeaderKey", orderModel.getAttribute("OrderHeaderKey"));
		YRCApiContext context = new YRCApiContext();
		context.setFormId("com.yantra.pca.ycd.rcp.tasks.customerAppeasement.wizards.YCDCustomerAppeasementWizard");
		context.setApiName("AcademyGetOrderLinesShipmentStatus");
		context.setInputXml(doc);
		callApi(context);
	}
	
	private void getExistingAppeasementsForOrder() {
		/**
		 * <ACADOrderAppeasement AcademyAppeasementKey=" " AppeasementReason=" "
		 * Createprogid=" " Createts=" " Createuserid=" " Lockid=" "
		 * Modifyprogid=" " Modifyts=" " Modifyuserid=" " OrderHeaderKey=" "
		 * OrderLineKey=" " UserID=" "/>
		 */
		setOrderDetailsModel();
		Document acadAppeaeInDoc = YRCXmlUtils
				.createDocument("ACADOrderAppeasement");
		//START: STL-1129. 
		/*acadAppeaeInDoc.getDocumentElement().setAttribute("OrderHeaderKey",
				orderModel.getAttribute("OrderHeaderKey"));*/
		acadAppeaeInDoc.getDocumentElement().setAttribute(AcademyPCAConstants.ATTR_ACADEMY_ORDER_HEADER_KEY,
				orderModel.getAttribute(AcademyPCAConstants.ATTR_ORDER_HEADER_KEY));
		//END: STL-1129. 
		YRCApiContext ctx = new YRCApiContext();
		ctx
				.setFormId("com.yantra.pca.ycd.rcp.tasks.customerAppeasement.wizards.YCDCustomerAppeasementWizard");
		// ctx.setApiName(AcademyPCAConstants.API_GET_EXISTING_APPEASEMENTS_FOR_AN_ORDER);
		ctx.setApiName("AcademyGetAppeasementListForOrderService");
		ctx.setInputXml(acadAppeaeInDoc);
		callApi(ctx);

	}

	private void setOrderDetailsModel() {
		Element orderDetails = getModel("OrderDetails");
		this.orderModel = orderDetails;

	}

	@Override
	public void handleApiCompletion(YRCApiContext ctx) {
		if ("AcademyGetAppeasementListForOrderService".equals(ctx.getApiName())) {
			setExtentionModel("extn_ExistingAppeasementsForOrder", ctx
					.getOutputXml().getDocumentElement());
		}
		
		if ("AcademyGetOrderLinesShipmentStatus".equals(ctx.getApiName())) 
		{
			setExtentionModel("extn_ShipmentStatusOfOrderlines", ctx.getOutputXml().getDocumentElement());
		}

		super.handleApiCompletion(ctx);
	}

	@Override
	public void postCommand(YRCApiContext ctx) {
		if (ctx.getApiName().equals("invokeGetAppeasementOffersUE")) {
			// System.out.println(XMLUtil.serialize(getModel("AppeasementOffers")));
		}
		super.postCommand(ctx);
	}

	/**
	 * Method called when a link is clicked.
	 */
	public YRCValidationResponse validateLinkClick(String fieldName) {
		// TODO Validation required for the following controls.

		// TODO Create and return a response.
		return super.validateLinkClick(fieldName);
	}

	/**
	 * Create and return the binding data for advanced table columns added to
	 * the tables.
	 */
	public YRCExtendedTableBindingData getExtendedTableBindingData(
			String tableName, ArrayList tableColumnNames) {
		// Create and return the binding data definition for the table.

		// The defualt super implementation does nothing.
		return super.getExtendedTableBindingData(tableName, tableColumnNames);
	}
}
// TODO Validation required for a Button control: btnNext
