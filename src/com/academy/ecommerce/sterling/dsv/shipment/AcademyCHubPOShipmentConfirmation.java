package com.academy.ecommerce.sterling.dsv.shipment;

import java.util.HashMap;

/**#########################################################################################
*
* Project Name                : CHub Migration for DSV
* Author                      : Everest
* Author Group				  : DSV
* Date                        : 09-AUG-2022 
* Description				  : This class retrieves the Shipment confirmation message from CHUB and
* 								do the necessary checks and call the confirmShipment API in sterling 
* 								 
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       		Remarks/Description                      
* ---------------------------------------------------------------------------------
* 12-AUG-2022		Everest  	 			  1.0           	Updated version
*
* #########################################################################################*/

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;

public class AcademyCHubPOShipmentConfirmation implements YIFCustomApi  {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyCHubPOShipmentConfirmation.class);
	public static final String CLASS_NAME = AcademyCHubPOShipmentConfirmation.class.getName();
	String strErrorMsg = "";
	/**
	 * This method is invoked from the Service to validate and process Shipment confirmation
	 * 
	 * @param env
	 * @return shipmentConfDoc
	 * @throws Exception
	 */
	
	public Document stampShipmentType(YFSEnvironment env, Document docShipmentConf) throws Exception {
	log.beginTimer("AcademyCHubPOShipmentConfirmation.stampShipmentType() method");
	log.debug("\nInput XML to AcademyCHubPOShipmentConfirmation API:\n" + XMLUtil.getXMLString(docShipmentConf));

	boolean bIsShipmentToBeProcessed = false;
		Document docGetOrderListIn = null;
		Document docGetOrderListOut = null;
		String strOrdHdrKey = "";
		String isWhiteglove = "N";
		try {
		Element eleInDoc = docShipmentConf.getDocumentElement();
		String strOrderNo = eleInDoc.getAttribute(AcademyConstants.ATTR_ORDER_NO);
		//Validate if mandatory parameters are present
		bIsShipmentToBeProcessed = validateXML(docShipmentConf);
		if (bIsShipmentToBeProcessed) {
			log.verbose("input document is" + XMLUtil.getXMLString(docShipmentConf));
			String strChubSCAC = null;
			String strChubCarrierServiceCode = null;
			// Logic to fetch the SCAC and Carrier service code using common code
			String strShippingServiceLevelCode = eleInDoc
					.getAttribute(AcademyConstants.STR_SHIPPING_SERVICE_LEVEL_CODE);
			Document docGetCommonCodeListInput = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			docGetCommonCodeListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE,
					AcademyConstants.STR_TO_CHUB_SCAC_MAP);
			docGetCommonCodeListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC,
					strShippingServiceLevelCode);
			log.verbose("input to getCommonCodeList API" + XMLUtil.getXMLString(docGetCommonCodeListInput));
			Document docGetCommonCodeListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST,
					docGetCommonCodeListInput);
			log.verbose("Output to getCommonCodeList API" + XMLUtil.getXMLString(docGetCommonCodeListOutput));
			Element eleDocEle = docGetCommonCodeListOutput.getDocumentElement();
			Element eleCommonCode = SCXmlUtil.getChildElement(eleDocEle, AcademyConstants.ELE_COMMON_CODE);
		
			if (!YFCObject.isVoid(eleCommonCode)) {
				String strChubShippingServiceLevel = eleCommonCode.getAttribute(AcademyConstants.CODE_LONG_DESC);
				log.verbose(
						"Value of strChubShippingServiceLevel as per common code--->" + strChubShippingServiceLevel);
				// Split the code long description and replace the values in SCAC and Carrier
				if (!YFCObject.isVoid(strChubShippingServiceLevel)) {
					String strSCACarr[] = strChubShippingServiceLevel.split(" ", 2);
					if (strSCACarr.length == 2) {
						strChubSCAC = strSCACarr[0];
						strChubCarrierServiceCode = strSCACarr[1];
					}
					
					eleInDoc.setAttribute(AcademyConstants.ATTR_SCAC, strChubSCAC);
					eleInDoc.setAttribute(AcademyConstants.CARRIER_SERVICE_CODE, strChubCarrierServiceCode);
					log.verbose("sChubSCAC is " + strChubSCAC);
					log.verbose("XsChubCarrierServiceCode " + strChubCarrierServiceCode);
				}
			} else {
				//Creating info logger for Splunk alerts 
					log.info("DSV Shipment Confirmation Error :: SCAC & CarrierService Validation failed for OrderNo::" 
							+ docShipmentConf.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO)
							+ " and SCAC and Service sent from Chub is :: " + strShippingServiceLevelCode);

					YFSException yfsExeSCACAndService = new YFSException("InValid ShippingServiceLevelCode received from CHUB");
					yfsExeSCACAndService.setErrorCode("CHUB_002");
					yfsExeSCACAndService.setErrorDescription(
							"Invalid ShippingServiceLevelCode received from CHUB for shipment confirmation");
					throw yfsExeSCACAndService;

			}

			// Stamping SCAC and CarrierServiceCode at Extn level
			String strScac = eleInDoc.getAttribute(AcademyConstants.ATTR_SCAC);
			String strCarrServiceCode = eleInDoc.getAttribute(AcademyConstants.CARRIER_SERVICE_CODE);
			String strExtnVendorShipmentNo = eleInDoc.getAttribute(AcademyConstants.STR_VENDOR_SHIPMENT_NO);
			Element eExtn = docShipmentConf.createElement(AcademyConstants.ELE_EXTN);
			eExtn.setAttribute(AcademyConstants.STR_ORIGINALSHIPMENT_SCAC, strScac);
			/* Start Jira#DSA-9 Extn_Vendor_Shipment_No */
			eExtn.setAttribute(AcademyConstants.STR_EXTN_VENDOR_SHIPMENT_NO, strExtnVendorShipmentNo);
			/* End Jira#DSA-9 Extn_Vendor_Shipment_No */
			eExtn.setAttribute(AcademyConstants.STR_ORIGINALSHIPMENT_LOS, strCarrServiceCode);
			eleInDoc.appendChild(eExtn);
			// prepare input to getOrderList API
			docGetOrderListIn = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			Element eleInOrder = docGetOrderListIn.getDocumentElement();
			eleInOrder.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.ENTERPRISE_CODE_SHIPMENT);
			eleInOrder.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.DOCUMENT_TYPE_SHIPMENT);
			eleInOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
			log.verbose("input to getOrderList API" + XMLUtil.getXMLString(docGetOrderListIn));
			
			//Start : OMNI-107324 : CHUB Duplicate Shipment messages
			Document outputTemplate = XMLUtil.getDocument(
					"<OrderList><Order OrderHeaderKey =''><OrderLines><OrderLine OrderLineKey='' PrimeLineNo='' SubLineNo='' MinLineStatus='' MaxLineStatus=''>"
					+ "</OrderLine></OrderLines></Order></OrderList>");
			//End : OMNI-107324 : CHUB Duplicate Shipment messages

			env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, outputTemplate);
			docGetOrderListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, docGetOrderListIn);
			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
			log.verbose("Output from getOrderList API" + XMLUtil.getXMLString(docGetOrderListOut));
			// fetch OrderHeaderKey from output of getOrderList API
			Element eleOutOrderList = docGetOrderListOut.getDocumentElement();
			Element eleOutOrder = (Element) eleOutOrderList.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
			if(YFCObject.isVoid(eleOutOrder)) {
				//Creating info logger for Splunk alerts 
				log.info("DSV Shipment Confirmation Error :: Invalid Order No OrderNo::" 
						+ docShipmentConf.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO));

				YFSException yfsExecInvalidOrder = new YFSException();
				yfsExecInvalidOrder.setErrorCode("CHUB_002");
				yfsExecInvalidOrder.setErrorDescription("Invalid OrderNo. OrderNo is not available in OMS.");
				throw yfsExecInvalidOrder;
			}
			
			//Start : OMNI-107324 : CHUB Duplicate Shipment messages
			NodeList nlShippedMinLineStatus = XPathUtil.getNodeList(docGetOrderListOut,	"/OrderList/Order/OrderLines/OrderLine[@MinLineStatus>='3700']");
			NodeList nlShippedMaxLineStatus = XPathUtil.getNodeList(docGetOrderListOut,	"/OrderList/Order/OrderLines/OrderLine[@MaxLineStatus>='3700' and @MaxLineStatus<'9000']");
			log.verbose(" the MinLineStatusLength :: "+nlShippedMinLineStatus.getLength());
			log.verbose(" the nlShippedMaxLineStatus :: "+nlShippedMaxLineStatus.getLength());
			
			HashMap<String, HashMap<String, String>> hmDuplicateContainer = new HashMap<String, HashMap<String, String>>();
			
			if(nlShippedMaxLineStatus.getLength()> 0 || nlShippedMaxLineStatus.getLength() > 0) {
				log.verbose("Partial lines on order already Shipped. Validate Tracking Info");
				Document docShipmentList = prepareAndInvokeGetShipmentList(env, strOrderNo, strExtnVendorShipmentNo);
				NodeList nlShipment = docShipmentList.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
				
				log.verbose(":: nlShipment :: "+nlShipment.getLength());

				if(nlShipment.getLength() > 0) {
					for(int iShipment=0; iShipment<nlShipment.getLength(); iShipment++) {
						Element eleShipment = (Element) nlShipment.item(iShipment);
						
						NodeList nlShipmentContainer = eleShipment.getElementsByTagName(AcademyConstants.ELE_CONTAINER);

						log.verbose(":: nlShipmentContainer :: "+nlShipmentContainer.getLength());

						for(int iContainer=0; iContainer < nlShipmentContainer.getLength(); iContainer++) {
							Element eleContainer = (Element) nlShipmentContainer.item(iContainer);
							String strTrackingNo = eleContainer.getAttribute(AcademyConstants.ATTR_TRACKING_NO);
							
							HashMap<String, String> hmShipmentLineInfo = new HashMap<String, String>();
							if(hmDuplicateContainer.containsKey(strTrackingNo)) {
								hmShipmentLineInfo = hmDuplicateContainer.get(strTrackingNo);
							}
							
							NodeList nlContainerShipmentLine = eleContainer.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
							log.verbose(":: nlContainerShipmentLine :: "+nlContainerShipmentLine.getLength());

							for(int iShipmentLine=0; iShipmentLine < nlContainerShipmentLine.getLength(); iShipmentLine++) {
								Element eleContainerShipmentLine = (Element) nlContainerShipmentLine.item(iShipmentLine);
								String strPrimeLineNo = eleContainerShipmentLine.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO);
								String strQuantity = eleContainerShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
								hmShipmentLineInfo.put(strPrimeLineNo, strQuantity);
							}
							hmDuplicateContainer.put(strTrackingNo, hmShipmentLineInfo);							
						}
					}
					log.verbose(" Duplicate Containers on Shipment are :: " + hmDuplicateContainer.toString());
				}
			}
			//End : OMNI-107324 : CHUB Duplicate Shipment messages

			strOrdHdrKey = eleOutOrder.getAttribute(AcademyConstants.STR_ORDR_HDR_KEY);
			// set OrderHeaderKey at root level of the input Document i.e at <Shipment>
			// level and also in <ShipmentLine> level
			eleInDoc.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, strOrdHdrKey);
			eleInDoc.setAttribute(AcademyConstants.ATTR_DOC_TYPE, "0005");
			NodeList nlOrderLinesList = eleOutOrder.getElementsByTagName(AcademyConstants.ELE_ORDER_LINES);
			Element eleOrderLines = (Element) nlOrderLinesList.item(0);
			NodeList nlOrderLineList = eleOrderLines.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
			NodeList nlShipmentLinesList = eleInDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES);
			Element eleShipmentLines = (Element) nlShipmentLinesList.item(0);
			NodeList nlShipmentLineNL = eleShipmentLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			for (int iCounter = 0; iCounter < nlShipmentLineNL.getLength(); iCounter++) {
				Element eleNode = (Element) nlShipmentLineNL.item(iCounter);
				eleNode.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, strOrdHdrKey);
				// Fetching PrimeLineNo from Shipment xml
				String sLineNo = eleNode.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO);
				log.verbose("testing :: "+sLineNo);
				// Looping through each OrderLine tag
				for (int jcounter = 0; jcounter < nlOrderLineList.getLength(); jcounter++) {
					Element eleOrderLine = (Element) nlOrderLineList.item(jcounter);
					// Matching the line No
					if (sLineNo.equals(eleOrderLine.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO))) {
						// Stamping OrderLineKey to the ShipmentLine.
						String sOrderLineKey = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
						eleNode.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, sOrderLineKey);
						log.verbose("testing sOrderLineKey :: "+sOrderLineKey);

					}
				}

				String strItemID = eleNode.getAttribute(AcademyConstants.ITEM_ID);

				/**
				 * invoke getItemList API to check whether the item is WG or NWG
				 * 
				 */

				Document getItemListInDoc = XMLUtil.createDocument(AcademyConstants.ITEM);
				Element getItemListInEle = getItemListInDoc.getDocumentElement();
				getItemListInEle.setAttribute(AcademyConstants.ITEM_ID, strItemID);

				Document outputTemplateForItemList = XMLUtil.getDocument(
						"<ItemList><Item ItemID='' UnitOfMeasure=''><Extn ExtnWhiteGloveEligible=''/></Item></ItemList>");

				env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, outputTemplateForItemList);
				log.verbose("getItemListInDoc input XML: --> " + XMLUtil.getXMLString(getItemListInDoc));

				Document getItemListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST,
						getItemListInDoc);
				env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
				log.verbose("getItemListOutDoc out XML: --> " + XMLUtil.getXMLString(getItemListOutDoc));
				Element eleGetItemListOutDoc = getItemListOutDoc.getDocumentElement();
				Element eleItem = (Element) eleGetItemListOutDoc.getElementsByTagName(AcademyConstants.ITEM).item(0);
				if(YFCObject.isVoid(eleItem)) {
						//Creating info logger for Splunk alerts 
						log.info("DSV Shipment Confirmation Error :: Invalid ItemID for OrderNo::" 
								+ docShipmentConf.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO)
								+ " ItemID is :: " + strItemID);

						YFSException yfsExecInvalidOrder = new YFSException();
						yfsExecInvalidOrder.setErrorCode("CHUB_002");
						yfsExecInvalidOrder.setErrorDescription("Invalid Item. Not present in OMS");
						throw yfsExecInvalidOrder;


					}
				// stamp UOM at <ShipmentLine> level
				String sUOM = eleItem.getAttribute(AcademyConstants.ATTR_UOM);
				eleNode.setAttribute(AcademyConstants.ATTR_UOM, sUOM);
				// stamp UOM at <ContainerDetail> level
				Element eleContainers = XMLUtil.getElementByXPath(docShipmentConf, "/Shipment/Containers");
				NodeList containerNL = eleContainers.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
				// loop through the <Container> tag
				for (int cCounter = 0; cCounter < containerNL.getLength(); cCounter++) {
					Element eleContainer = (Element) containerNL.item(cCounter);
					
					//Start : OMNI-107324 : CHUB Duplicate Shipment messages
					HashMap<String,String> hmShipmentLineInfo = new HashMap<String,String>();
					boolean bContainerToBeRemoved = false;
					String strTrackingNo = eleContainer.getAttribute(AcademyConstants.ATTR_TRACKING_NO);
					if(hmDuplicateContainer.size() > 0 & hmDuplicateContainer.containsKey(strTrackingNo)) {
						bContainerToBeRemoved = true;
						hmShipmentLineInfo = hmDuplicateContainer.get(strTrackingNo);
					}
					//End : OMNI-107324 : CHUB Duplicate Shipment messages

					Element eleContainerDtls = (Element) eleContainer
							.getElementsByTagName(AcademyConstants.ELE_CONTAINER_DTLS).item(0);
					NodeList containerDtlNL = eleContainerDtls.getElementsByTagName(AcademyConstants.CONTAINER_DETAIL);
					// inner loop to loop through <ContainerDetail> tag
					for (int cdCounter = 0; cdCounter < containerDtlNL.getLength(); cdCounter++) {
						Element eleContainerDtl = (Element) containerDtlNL.item(cdCounter);
						NodeList shipLineNL = eleContainerDtl.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
						// loop through <ShipmentLine> tag
						for (int sCounter = 0; sCounter < shipLineNL.getLength(); sCounter++) {
							Element eleShipmntLine = (Element) shipLineNL.item(sCounter);
							String strItemId = eleShipmntLine.getAttribute(AcademyConstants.ITEM_ID);
							if (strItemId.equals(strItemID)) {
								eleShipmntLine.setAttribute(AcademyConstants.ATTR_UOM, sUOM);
							}
							//Start : OMNI-107324 : CHUB Duplicate Shipment messages
							String strPrimeLineNo = eleShipmntLine.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO);
							if(hmShipmentLineInfo.size() > 0 && hmShipmentLineInfo.containsKey(strPrimeLineNo)) {
								log.verbose(" Shipment line is eligible to be removed");
								Element eleShipmentLine = XMLUtil.getElementByXPath(docShipmentConf, "/Shipment/ShipmentLines/ShipmentLine[@PrimeLineNo='"+ strPrimeLineNo +"']");
								if(!YFCObject.isVoid(eleShipmentLine)) {
									log.verbose(" Line To be removed " + SCXmlUtil.getString(eleShipmentLine));
									String strQuantity = eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
									if(!YFCObject.isVoid(strQuantity)) {
										int iShipmentLineQty = (int) Double.parseDouble(strQuantity);
										int iContainerLineQty = (int) Double.parseDouble(hmShipmentLineInfo.get(strPrimeLineNo));
										log.verbose(" :: iContainerLineQty :: "+ iContainerLineQty);
										log.verbose(" :: iShipmentLineQty :: "+ iShipmentLineQty);
										
										if(iContainerLineQty == iShipmentLineQty) {
											log.verbose(" Complete ShipmentLine Eligible for removal " + SCXmlUtil.getString(eleShipmentLine));
											eleShipmentLines.removeChild(eleShipmentLine);
											log.verbose(" :: strPrimeLineNo :: "+ strPrimeLineNo + " :sLineNo :: "+sLineNo);
											if(strPrimeLineNo.equals(sLineNo)) {
												log.verbose("Reducing Shipment line counter if matches");
												iCounter--;
											}
										}
										else if (iShipmentLineQty > iContainerLineQty ) {
											log.verbose(" Quantity to be updated :: " + (iShipmentLineQty-iContainerLineQty));
											eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, 
													Integer.toString(iShipmentLineQty - iContainerLineQty));
										}
									}
								}
							}
							//End : OMNI-107324 : CHUB Duplicate Shipment messages
						}
					}
					//Start : OMNI-107324 : CHUB Duplicate Shipment messages
					if(bContainerToBeRemoved) {
						eleContainers.removeChild(eleContainer);
						cCounter --;
					}
					//End : OMNI-107324 : CHUB Duplicate Shipment messages
				}
				Element eleExtnList = (Element) eleItem.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
				if (AcademyConstants.ATTR_Y
						.equalsIgnoreCase(eleExtnList.getAttribute(AcademyConstants.ATTR_EXTN_WHITE_GLOVE_ELIGIBLE))) {
					isWhiteglove = "Y";
				}
			}
			if (isWhiteglove.equals("Y")) {
				// SET SHIPMENT_TYPE as WG
				// if shipment contains mix items, then SHIPMENT_TYPE should be WG.
				eleInDoc.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.WG);
			} else {
				// SET SHIPMENT_TYPE as NWG
				// if shipment contains mix items, then SHIPMENT_TYPE should be NWG.
				eleInDoc.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.NWG);
			}
		}
		}
		catch (YFSException yfsEx) {
			throw yfsEx;
		}
		catch (Exception exe) {
			//Creating info logger for Splunk alerts 
			log.info("DSV Shipment Confirmation Error ::  Internal OMS Error for OrderNo::" 
					+ docShipmentConf.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO) 
					+ " . Error Trace :: "+exe.getStackTrace());
			
			YFSException yfsExcep = new YFSException(exe.getMessage());
			yfsExcep.setErrorCode("CHUB_INTERNAL_ERROR");
			yfsExcep.setErrorDescription("Error While trying to process Shipment in OMS ");
			throw yfsExcep;

		}

		log.debug("output XML from the service AcademyDSVPOShipmentConfirmation :: "
				+ XMLUtil.getXMLString(docShipmentConf));
		log.endTimer("AcademyCHubPOShipmentConfirmation.stampShipmentType() method");

		return docShipmentConf;
	}

	/**
	 * This method is used to validate all mandatory attributes from input xml 
	 * 
	 * @param shipmentConfDoc
	 */
	
	private boolean validateXML(Document docShipmentConf) {
	log.beginTimer("AcademyCHubPOShipmentConfirmation.validateXML() method");
	log.debug("Input XML for Validation" + XMLUtil.getXMLString(docShipmentConf));
	boolean isShipmentToBeProcessed = true;
		
		String strOrderNo = docShipmentConf.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO);
		String strCountry = ((Element) docShipmentConf.getElementsByTagName(AcademyConstants.SHIP_NODE_ADD).item(0))
				.getAttribute(AcademyConstants.COUNTRY);
		String strZipCode = ((Element) docShipmentConf.getElementsByTagName(AcademyConstants.SHIP_NODE_ADD).item(0))
				.getAttribute(AcademyConstants.ZIP_CODE);
		String strShippingServiceLevelCode = docShipmentConf.getDocumentElement()
				.getAttribute(AcademyConstants.STR_SHIPPING_SERVICE_LEVEL_CODE);
	//Validate if the Shipping Service Codes are available in the message
	if (YFCObject.isVoid(strShippingServiceLevelCode)) {
		updateErrorMessage(AcademyConstants.STR_SHIPPING_SERVICE_LEVEL_CODE, AcademyConstants.ELE_SHIPMENT );
		}
		
		
		//Validate if OrderNo is present in the message
		if (YFCObject.isVoid(strOrderNo)) {
			updateErrorMessage(AcademyConstants.ATTR_ORDER_NO,  AcademyConstants.ELE_SHIPMENT );
		}
		//Validating address level info
		if (YFCObject.isVoid(strCountry)) {
			updateErrorMessage(AcademyConstants.COUNTRY,  AcademyConstants.SHIP_NODE_ADD );
		}
		if (YFCObject.isVoid(strZipCode)) {
			updateErrorMessage(AcademyConstants.ZIP_CODE,  AcademyConstants.SHIP_NODE_ADD );
		}

		//Validating the Container Level info
		NodeList nlContainerList = docShipmentConf.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
		for (int iCon = 0; iCon < nlContainerList.getLength(); iCon++) {

			Element eleContainer = (Element) nlContainerList.item(iCon);
			String strTrackingNo = eleContainer.getAttribute(AcademyConstants.ATTR_TRACKING_NO).trim();
			eleContainer.setAttribute(AcademyConstants.ATTR_TRACKING_NO, strTrackingNo);
			log.verbose("The valueof trackingNo After trimming" + strTrackingNo);

			//Validating Container Detail level information
			Element eleContainerDetail = (Element) eleContainer.getElementsByTagName(AcademyConstants.CONTAINER_DETAIL)
					.item(0);
			String strQuantity = eleContainerDetail.getAttribute(AcademyConstants.ATTR_QUANTITY);
			Element eleContainerShipmentLine = (Element) eleContainerDetail
					.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE).item(0);

			String strDocumentType = eleContainerShipmentLine.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
			String strItemID = eleContainerShipmentLine.getAttribute(AcademyConstants.ITEM_ID);
			String strPrimeLineNo = eleContainerShipmentLine.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO);
			String strSubLineNo = eleContainerShipmentLine.getAttribute(AcademyConstants.SUB_LINE_NO);
			String strOrderLineKey = eleContainerShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			String strShipmentLineOrderNo = eleContainerShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			String strOrderReleaseKey = eleContainerShipmentLine.getAttribute(AcademyConstants.ORDER_RELEASE_KEY);

			//Validating Order Line Level Attributes mandatory in OMS
			if (YFCObject.isVoid(strOrderLineKey)) {
				boolean bPrimeLineNoMissing = false;
				boolean bSubLineNoMissing = false;
				if (YFCObject.isVoid(strPrimeLineNo)) {
					bPrimeLineNoMissing = true;
					      
				}
				if (YFCObject.isVoid(strSubLineNo)) {
					bSubLineNoMissing = true;
					
				}

				if (bPrimeLineNoMissing && !bSubLineNoMissing) {
					updateErrorMessage(AcademyConstants.ATTR_PRIME_LINE_NO, 
							AcademyConstants.CONTAINER_DETAIL + "/" + AcademyConstants.ELE_SHIPMENT_LINE );
				} else if (!bPrimeLineNoMissing && bSubLineNoMissing) {
					updateErrorMessage(AcademyConstants.SUB_LINE_NO, 
							AcademyConstants.CONTAINER_DETAIL + "/" + AcademyConstants.ELE_SHIPMENT_LINE );
				} else if (bPrimeLineNoMissing && bSubLineNoMissing) {
					updateErrorMessage(AcademyConstants.ATTR_PRIME_LINE_NO + "," +AcademyConstants.SUB_LINE_NO ,
							AcademyConstants.CONTAINER_DETAIL + "/" + AcademyConstants.ELE_SHIPMENT_LINE );
				}
			}

			if (YFCObject.isVoid(strOrderReleaseKey)) {
				updateErrorMessage(AcademyConstants.ORDER_RELEASE_KEY, 
						AcademyConstants.CONTAINER_DETAIL + "/" + AcademyConstants.ELE_SHIPMENT_LINE);
			}
			if (YFCObject.isVoid(strShipmentLineOrderNo)) {
				updateErrorMessage(AcademyConstants.ATTR_ORDER_NO, 
						AcademyConstants.CONTAINER_DETAIL + "/" + AcademyConstants.ELE_SHIPMENT_LINE);
			}

			//VAlidating line level item and qty info
			if (YFCObject.isVoid(strQuantity)) {
				updateErrorMessage(AcademyConstants.ATTR_QUANTITY, 
						AcademyConstants.CONTAINER_DETAIL + "/" + AcademyConstants.ELE_SHIPMENT_LINE);
			}
			if (YFCObject.isVoid(strDocumentType)) {
				updateErrorMessage(AcademyConstants.ATTR_DOC_TYPE, 
						AcademyConstants.CONTAINER_DETAIL + "/" + AcademyConstants.ELE_SHIPMENT_LINE);
			}
			if (YFCObject.isVoid(strItemID)) {
				updateErrorMessage(AcademyConstants.ITEM_ID, 
						AcademyConstants.CONTAINER_DETAIL + "/" + AcademyConstants.ELE_SHIPMENT_LINE);
			}		

			//VAlidating Tracking Info
			if (YFCObject.isVoid(strTrackingNo)) {
				updateErrorMessage(AcademyConstants.ATTR_TRACKING_NO, AcademyConstants.CONTAINER_DETAIL );
			}
		}
		Element eleShipmentLines = (Element) docShipmentConf.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES)
				.item(0);
		NodeList nlShipmentLineList = eleShipmentLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

		//VAlidating Shipment Line level information
		for (int iShipLine = 0; iShipLine < nlShipmentLineList.getLength(); iShipLine++) {
			Element eleShipmentLine = (Element) nlShipmentLineList.item(iShipLine);

			String strShipItemID = eleShipmentLine.getAttribute(AcademyConstants.ITEM_ID);
			String strShipOrderLineKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			String strShipOrderNo = eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			String strShipPrimeNo = eleShipmentLine.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO);
			String strShipSubNo = eleShipmentLine.getAttribute(AcademyConstants.SUB_LINE_NO);
			String strShipOrderReleaseKey = eleShipmentLine.getAttribute(AcademyConstants.ORDER_RELEASE_KEY);
			String strShipQuantity = eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);

			//Validating Order Line Level Attributes mandatory in OMS
			if (YFCObject.isVoid(strShipOrderLineKey)) {
				
				boolean bPrimeLineNoMissing = false;
				boolean bSubLineNoMissing = false;
				if (YFCObject.isVoid(strShipPrimeNo)) {
					bPrimeLineNoMissing = true;
					      
				}
				if (YFCObject.isVoid(strShipSubNo)) {
					bSubLineNoMissing = true;
					
				}

				if (bPrimeLineNoMissing && !bSubLineNoMissing) {
					updateErrorMessage(AcademyConstants.ATTR_PRIME_LINE_NO,AcademyConstants.ELE_SHIPMENT_LINE );
				} else if (!bPrimeLineNoMissing && bSubLineNoMissing) {
					updateErrorMessage(AcademyConstants.SUB_LINE_NO,AcademyConstants.ELE_SHIPMENT_LINE );
				} else if (bPrimeLineNoMissing && bSubLineNoMissing) {
					updateErrorMessage(AcademyConstants.ATTR_PRIME_LINE_NO + "," +AcademyConstants.SUB_LINE_NO ,AcademyConstants.ELE_SHIPMENT_LINE );
				}
			}

			if (YFCObject.isVoid(strShipOrderReleaseKey)) {
				updateErrorMessage(AcademyConstants.ORDER_RELEASE_KEY, AcademyConstants.ELE_SHIPMENT_LINE);
			}
			if (YFCObject.isVoid(strShipOrderNo)) {
				updateErrorMessage(AcademyConstants.ATTR_ORDER_NO, AcademyConstants.ELE_SHIPMENT_LINE);
			}

			//Validating Item and Qty level info
			if (YFCObject.isVoid(strShipItemID)) {
				updateErrorMessage(AcademyConstants.ITEM_ID, AcademyConstants.ELE_SHIPMENT_LINE);
			}

			if (YFCObject.isVoid(strShipQuantity)) {
				updateErrorMessage(AcademyConstants.ATTR_QUANTITY, AcademyConstants.ELE_SHIPMENT_LINE);
			}

		}

		if (!YFCObject.isVoid(strErrorMsg)) {
			//Creating a info log to for Splunk alerts 
			log.info("DSV Shipment Confirmation Error :: Mandatory Parameters missing for OrderNo:: " 
					+ strOrderNo + " . Error Message :: "+ strErrorMsg);

			//Throw custom Exception to roll back the transaction
			YFSException yfsExcep = new YFSException(strErrorMsg);
			yfsExcep.setErrorCode("CHUB_001");
			yfsExcep.setErrorDescription("Mandatory Feilds are missing");
			throw yfsExcep;
		}
		log.debug("Document is validated...." + XMLUtil.getXMLString(docShipmentConf));
		log.endTimer("AcademyCHubPOShipmentConfirmation.validateXML() method");

		return isShipmentToBeProcessed;
	}
	
	
	/**
	 * This method updates the Error message based on the data missing
	 * 
	 * @param shipmentConfDoc
	 */
	private void updateErrorMessage(String strAttributeName, String strElement) {
		log.beginTimer("AcademyCHubPOShipmentConfirmation.setErrorMessage() method");

		if(!YFCObject.isVoid(strElement)) {
			strErrorMsg = strErrorMsg + strAttributeName +" is missing at " + strElement + " Level \n";
		}
		else {
			strErrorMsg = strErrorMsg + strAttributeName +" is missing \n";
		}

		log.debug("Updated strErrorMsg...." + strErrorMsg);
		log.endTimer("AcademyCHubPOShipmentConfirmation.setErrorMessage() method");
	}

	
	/**
	 * This method invokes getShipmentList API to fetch exsiting shipments on Order
	 * 
	 * @param shipmentConfDoc
	 */
	private Document prepareAndInvokeGetShipmentList(YFSEnvironment env, String strOrderNo, String strVendorShipmentNo) throws Exception{
		log.beginTimer("AcademyCHubPOShipmentConfirmation.prepareAndInvokeGetShipmentList() method");
		Document docGetShipmentListOut = null;
		
		Document docGetShipmentListInp = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleShipmentInp = docGetShipmentListInp.getDocumentElement();
		eleShipmentInp.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		
		Element eleExtn = SCXmlUtil.createChild(eleShipmentInp, AcademyConstants.ELE_EXTN);
		eleExtn.setAttribute(AcademyConstants.STR_EXTN_VENDOR_SHIPMENT_NO, strVendorShipmentNo);
		
		Document docGetShipmentListTemplate = XMLUtil.getDocument(
				"<Shipments><Shipment ShipmentNo=''><Containers><Container TrackingNo=''><ContainerDetails>"
				+ "<ContainerDetail Quantity=''><ShipmentLine ItemID='' PrimeLineNo='' Quantity='' /></ContainerDetail>"
				+ "</ContainerDetails></Container></Containers></Shipment></Shipments>");

		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docGetShipmentListTemplate);
		log.verbose("getItemListInDoc input XML: --> " + XMLUtil.getXMLString(docGetShipmentListTemplate));
		docGetShipmentListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
				docGetShipmentListInp);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);		

		log.endTimer("AcademyCHubPOShipmentConfirmation.prepareAndInvokeGetShipmentList() method");
		return docGetShipmentListOut;
	}
	
	
	@Override
	public void setProperties(Properties arg0) throws Exception {
		
		
	}

	}
