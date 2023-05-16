package com.academy.ecommerce.sterling.shipment;

/**
 * This class invoke to reprint pack slip and/or Shipping Label
 *  
 */
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

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
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyReprintPackSlipAndShippingLabel implements YIFCustomApi {

	private Properties props;

	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyReprintPackSlipAndShippingLabel.class);
	private YFSException yfsEx = null;
	private YFCException yfcEx = null;

	/**
	 * This method validate the Container and invoke Reprint process only for
	 * White Glove or Bulk shipments
	 * 
	 * @param env
	 * @param inDoc
	 * @throws YFSException
	 * @throws ParserConfigurationException
	 */
	public void processReprintForBulkShipment(YFSEnvironment env, Document inDoc)
			throws YFSException, ParserConfigurationException {
		log
				.beginTimer(" Begining of processReprintForBulkShipment -> AcademyReprintPackSlipAndShippingLabel");
		/*
		 * this condition is used to check input for
		 * inputForGetShipmentContainerList is not empty <Print
		 * IgnoreOrdering="Y"> <Shipment HazardousMaterialFlag="N" PickListNo=""
		 * ShipmentKey="2011042100595412692749"/> <PrinterPreference
		 * OrganizationCode="005" PrinterId="PRNT-PACK3" UserId="admin"/>
		 * <LabelPreference BuyerOrganizationCode=""
		 * EnterpriseCode="Academy_Direct" NoOfCopies="" Node="005" SCAC="UPSN"
		 * Scac="UPSN" SellerOrganizationCode="Academy_Direct"/> </Print>
		 * 
		 */
		if (!YFCObject.isVoid(inDoc)) {
			log.verbose("Input document processReprintForBulkShipment: "
					+ XMLUtil.getXMLString(inDoc));
			Element elePrinterPreference = (Element) inDoc.getDocumentElement()
					.getElementsByTagName("PrinterPreference").item(0);
			String strPrinterId = elePrinterPreference
					.getAttribute("PrinterId");
			log.verbose("********PrinterId is******" + strPrinterId);
			if (inDoc.getDocumentElement().getElementsByTagName(
					AcademyConstants.ELE_SHIPMENT).getLength() <= 0)
				return;
			log.verbose("Contains Shipment");
			Element eleShipment = (Element) inDoc.getDocumentElement()
					.getElementsByTagName(AcademyConstants.ELE_SHIPMENT)
					.item(0);
			if (YFCObject.isNull(eleShipment) || YFCObject.isVoid(eleShipment))
				return;

			String strShipmentKey = eleShipment.getAttribute("ShipmentKey");
			Document docShipment = getShipmentDetails(env, strShipmentKey);
			if (docShipment.getDocumentElement().getElementsByTagName(
					AcademyConstants.ELE_SHIPMENT).getLength() <= 0)
				return;
			Element eShipment = (Element) docShipment.getDocumentElement()
					.getElementsByTagName(AcademyConstants.ELE_SHIPMENT)
					.item(0);
			if (YFCObject.isNull(eShipment) || YFCObject.isVoid(eShipment))
				return;
			// Get the Shipment Status
			String shipmentStatus = eShipment
					.getAttribute(AcademyConstants.ATTR_STATUS);
			log.verbose("The status of the shipment is : " + shipmentStatus);
			// Check for canceled shipment
			if (shipmentStatus != null && shipmentStatus.equals("9000")) {
				log
						.verbose("Shipment is in Cancelled status .. Therefore, stop the reprint process.. ");
				yfcEx = new YFCException();
				yfcEx.setAttribute(YFCException.ERROR_CODE, "EXTN_ACADEMY_09");
				yfcEx.setAttribute(YFCException.ERROR_DESCRIPTION,
						"Reprint of cancelled Shipment is not allowed.");
				yfcEx.printStackTrace();
				throw yfcEx;
			}
			String strShipmentType = eShipment
					.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
			log.verbose("****ShipmentType is:" + strShipmentType);
			// check for White Glove Shipment
			if (strShipmentType != null && strShipmentType.equals("WG")) {
				log.verbose("White Glove Shipment ... ");
				reprintPackSlipForShipment(env, eShipment, true, strPrinterId);
			} else if (strShipmentType != null
					&& (strShipmentType.equals("BLP")
							|| strShipmentType.equals("BULKOVNT") || strShipmentType
							.equals("BNLP"))) {
				log.verbose("Bulk Shipment ... ");
				reprintPackSlipForShipment(env, eShipment, false, strPrinterId);
			} else {
				log.verbose("Neither White Glove nor Bulk shipment...");
				yfcEx = new YFCException();
				yfcEx.setAttribute(YFCException.ERROR_CODE,"EXTN_ACADEMY_10");
				yfcEx
						.setAttribute(YFCException.ERROR_DESCRIPTION,"Neither White Glove nor Bulk shipment.Use correct service to reprint");
				yfcEx.printStackTrace();
				throw yfcEx;

			}
		}
		log
				.endTimer("End of processReprintForBulkShipment -> AcademyReprintPackSlipAndShippingLabel");
	}

	private Document getShipmentDetails(YFSEnvironment env,
			String strShipmentKey) throws ParserConfigurationException {
		Document docOutputGetShipmentList = null;
		try {
			Document docShipment = XMLUtil.createDocument("Shipment");
			docShipment.getDocumentElement().setAttribute("ShipmentKey",
					strShipmentKey);
			env.setApiTemplate("getShipmentList",
					"global/template/api/getShipmentList.DetailsForReprint.xml");
			docOutputGetShipmentList = AcademyUtil.invokeAPI(env,
					"getShipmentList", docShipment);
			log.verbose("**** Output of getShipmentList is *********"
					+ XMLUtil.getXMLString(docOutputGetShipmentList));
			env.clearApiTemplate("getShipmentList");
			return docOutputGetShipmentList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return docOutputGetShipmentList;
	}

	/**
	 * This method invoke print process repeatedly only for "eligible
	 * Containers"
	 * 
	 * @param env
	 * @param eleShipment
	 * @param isWGShipment
	 */
	private void reprintPackSlipForShipment(YFSEnvironment env,
			Element eleShipment, boolean isWGShipment, String strPrinterId) throws YFSException {
		log.beginTimer("Beginning of reprintPackSlipForShipment ..");
		try {
			// Get the Container List
			NodeList containerLst = eleShipment
					.getElementsByTagName("Container");
			if (containerLst.getLength() <= 0) {
				// No Container in the shipment
				log
						.verbose("No Active container in the shipment. Stop the reprint process..");
				yfcEx = new YFCException();
				yfcEx.setAttribute(YFCException.ERROR_CODE,"EXTN_ACADEMY_11");
				yfcEx
						.setAttribute(YFCException.ERROR_DESCRIPTION,"No Active container in the shipment. Reprint process is stopped");
				yfcEx.printStackTrace();
				throw yfcEx;
			}
			/**
			 * Set variable on YFSEnvironment object to prevent unnecessary
			 * process. Reprint using the exist process of Pack Slip print which
			 * is part of the ProNo/Tracking# and Invoice No generation.
			 */
			env.setTxnObject("ReprintPackSlip", "Y");
			env.setTxnObject("ReprintPrinterId", strPrinterId);
			for (int i = 0; i < containerLst.getLength(); i++) {
				Element elecontainer = (Element) containerLst.item(i);
				String strProTrackingNo = null;
				log.verbose("IsWhiteGloveShipment : " + isWGShipment);
				// Get ProNo if WhiteGlove shipment Else Tracking#.
				if (isWGShipment)
					strProTrackingNo = eleShipment.getAttribute("ProNo");
				else
					strProTrackingNo = elecontainer
							.getAttribute(AcademyConstants.ATTR_TRACKING_NO);
				log.verbose("ProNo if WGShipment, else Tracking No is : "
						+ strProTrackingNo);
				// Check for container is eligible or not for Reprint
				if (strProTrackingNo != null
						&& strProTrackingNo.trim().length() > 0) {
					log.verbose("Container is eligible for Reprint");
					Document reprintInput = XMLUtil.createDocument("Container");
					reprintInput.getDocumentElement().setAttribute(
							"ShipmentContainerKey",
							elecontainer.getAttribute("ShipmentContainerKey"));
					log.verbose("Input to GetDataForPackSlip reprint is : "
							+ XMLUtil.getXMLString(reprintInput));
					// Invoke appropriate Print Flow
					if (isWGShipment)
						AcademyUtil.invokeService(env,
								"AcademyPrintShippingLabelForWhiteGlove",
								reprintInput);
					else
						AcademyUtil.invokeService(env,
								"AcademyPrintBulkPackSlip", reprintInput);
				} else {
					log
							.verbose("Tracking No for a Bulk Shipment or ProNo for a White Glove Shipment isn't stamped.Hence reprint will not execute.");
					yfcEx = new YFCException();
					yfcEx.setAttribute(YFCException.ERROR_CODE,"EXTN_ACADEMY_13");
					yfcEx
							.setAttribute(YFCException.ERROR_DESCRIPTION,"Tracking Number /Pro No doesn't exist for the container.");
					yfcEx.printStackTrace();
					throw yfcEx;
				}
			}
			// Reset reprint flag
			env.setTxnObject("ReprintPrinterId", "");
			env.setTxnObject("ReprintPackSlip", "N");
		} catch (Exception e) {
			e.printStackTrace();
			yfsEx = new YFSException(e.getMessage());
			throw yfsEx;
		}
		log.verbose("End of reprintPackSlipForShipment ... ");
	}

	/**
	 * This method invokes to reprint the Container Pack Slip/Invoice only for
	 * Conveyable Shipment
	 * 
	 * @param env
	 * @param inDoc
	 * @throws Exception 
	 * @throws Exception 
	 * @throws Exception 
	 */

	/*
	 * <Print IgnoreOrdering="Y"> <Container SCAC="UPSN" Scac="UPSN"
	 * ShipmentContainerKey="201009080212382760774"/> <PrinterPreference
	 * OrganizationCode="005" PrinterId="PRNT-PACK19" UserId="admin"/>
	 * <LabelPreference BuyerOrganizationCode="" EnterpriseCode="Academy_Direct"
	 * NoOfCopies="1" Node="005" SCAC="UPSN" Scac="UPSN"
	 * SellerOrganizationCode="Academy_Direct"/> </Print>
	 */
	public void processReprintForContainerInvoice(YFSEnvironment env,
			Document inDoc) throws Exception {
		log
				.beginTimer("Beginning of processReprintForContainerInvoice - > AcademyReprintPackSlipAndShippingLabel ");
			if (YFSObject.isNull(inDoc) || YFSObject.isVoid(inDoc))
				return;
			log.verbose("input is : " + XMLUtil.getXMLString(inDoc));
			if (inDoc.getDocumentElement().getElementsByTagName("Container")
					.getLength() <= 0)
				return;
			Element eleContainer = (Element) inDoc.getDocumentElement()
					.getElementsByTagName("Container").item(0);
			if (YFSObject.isNull(eleContainer
					.getAttribute("ShipmentContainerKey"))
					|| YFSObject.isVoid(eleContainer
							.getAttribute("ShipmentContainerKey")))
				return;
			Element elePrinterPreference = (Element) inDoc.getDocumentElement()
					.getElementsByTagName("PrinterPreference").item(0);
			String strPrinterId = elePrinterPreference
					.getAttribute("PrinterId");
			log.verbose("********PrinterId is******" + strPrinterId);
			Document inputToContainerList;
				inputToContainerList = XMLUtil.createDocument("Container");
				inputToContainerList.getDocumentElement().setAttribute(
						"ShipmentContainerKey",
						eleContainer.getAttribute("ShipmentContainerKey"));
				env
						.setApiTemplate("getShipmentContainerList",
								"global/template/api/getShipmentConatinerList.DetailsForReprint.xml");
				Document shipmentContainerLst = AcademyUtil.invokeAPI(env,
						"getShipmentContainerList", inputToContainerList);
				if (shipmentContainerLst != null
						&& shipmentContainerLst.getDocumentElement()
								.hasChildNodes()) {
					Element eContainer = (Element) shipmentContainerLst
							.getElementsByTagName("Container").item(0);
					Element eleShipment = (Element) shipmentContainerLst
							.getDocumentElement().getElementsByTagName(
									AcademyConstants.ELE_SHIPMENT).item(0);
					// Get the Shipment Status
					String shipmentStatus = eleShipment
							.getAttribute(AcademyConstants.ATTR_STATUS);
					// Check for canceled shipment
					if (shipmentStatus != null && shipmentStatus.equals("9000")) {
						log
								.verbose("Shipment is in Cancelled status .. Therefore, stop the reprint process.. ");
						yfcEx = new YFCException();
						yfcEx.setAttribute(YFCException.ERROR_CODE,"EXTN_ACADEMY_09");
						yfcEx
								.setAttribute(YFCException.ERROR_DESCRIPTION,"Reprint of cancelled Shipment is not allowed.");
						yfcEx.printStackTrace();
						throw yfcEx;
					}
					String shipmentType = null;
					if (eleShipment != null && eleShipment.hasAttributes())
						shipmentType = eleShipment
								.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
					if (shipmentType != null
							&& (shipmentType.equals("GC") || shipmentType.equalsIgnoreCase(AcademyConstants.STR_GC_ONLY_SHIP_TYPE)
									|| shipmentType.equals("CON") || shipmentType
									.equals("CONOVNT"))) {
						Document docContainer = XMLUtil.createDocument("Container");
						XMLUtil.copyElement(docContainer, eContainer, docContainer
								.getDocumentElement());
						env.setTxnObject("ReprintPackSlip", "Y");
						env.setTxnObject("ReprintPrinterId", strPrinterId);
						AcademyUtil.invokeService(env,
								"AcademyPrintPackSlipForContainer", docContainer);
						env.setTxnObject("ReprintPrinterId", "");
						env.setTxnObject("ReprintPackSlip", "N");
					} else {
						log.verbose("**** Not a conveyable container");
						yfcEx = new YFCException();
						yfcEx.setAttribute(YFCException.ERROR_CODE,"EXTN_ACADEMY_14");
						yfcEx
								.setAttribute(YFCException.ERROR_DESCRIPTION,"Not a conveyable shipment.Use correct reprint service.");
						yfcEx.printStackTrace();
						throw yfcEx;
					}
				}
		log
				.endTimer("End of processReprintForContainerInvoice - > AcademyReprintPackSlipAndShippingLabel ");
	}

	/**
	 * This method invokes to reprint the Invoice and Shipping label for Bulk
	 * 
	 * @param env
	 * @param inDoc
	 * @throws Exception 
	 * @throws Exception 
	 */
	/*
	 * <Print IgnoreOrdering="Y"> <Container SCAC="UPSN" Scac="UPSN"
	 * ShipmentContainerKey="2011042101185912692912"/> <PrinterPreference
	 * OrganizationCode="005" PrinterId="PRNT-PACK3" UserId="admin"/>
	 * <LabelPreference BuyerOrganizationCode="" EnterpriseCode="Academy_Direct"
	 * NoOfCopies="" Node="005" SCAC="UPSN" Scac="UPSN"
	 * SellerOrganizationCode="Academy_Direct"/> </Print>
	 * 
	 */
	public void processReprintForBulkContainer(YFSEnvironment env,
			Document inDoc) throws Exception {
		log
				.beginTimer("Beginning of processRepringForBulkContainer - > AcademyReprintPackSlipAndShippingLabel ");
			if (YFSObject.isNull(inDoc) || YFSObject.isVoid(inDoc))
				return;
			log.verbose("input is : " + XMLUtil.getXMLString(inDoc));
			Element elePrinterPreference = (Element) inDoc.getDocumentElement()
					.getElementsByTagName("PrinterPreference").item(0);
			String strPrinterId = elePrinterPreference
					.getAttribute("PrinterId");
			log.verbose("********PrinterId is******" + strPrinterId);
			if (inDoc.getDocumentElement().getElementsByTagName("Container")
					.getLength() <= 0)
				return;
			Element eleContainer = (Element) inDoc.getDocumentElement()
					.getElementsByTagName("Container").item(0);
			if (YFSObject.isNull(eleContainer
					.getAttribute("ShipmentContainerKey"))
					|| YFSObject.isVoid(eleContainer
							.getAttribute("ShipmentContainerKey")))
				return;
			Document inputToContainerList;
				inputToContainerList = XMLUtil.createDocument("Container");
				inputToContainerList.getDocumentElement().setAttribute(
						"ShipmentContainerKey",
						eleContainer.getAttribute("ShipmentContainerKey"));
				env
						.setApiTemplate("getShipmentContainerList",
								"global/template/api/getShipmentConatinerList.DetailsForReprint.xml");
				Document shipmentContainerLst = AcademyUtil.invokeAPI(env,
						"getShipmentContainerList", inputToContainerList);
				
				if (shipmentContainerLst != null
						&& shipmentContainerLst.getDocumentElement()
								.hasChildNodes()) {
					Element eContainer = (Element) shipmentContainerLst
							.getDocumentElement().getElementsByTagName("Container")
							.item(0);
					Element eleShipment = (Element) shipmentContainerLst
							.getDocumentElement().getElementsByTagName(
									AcademyConstants.ELE_SHIPMENT).item(0);
					// Get the Shipment Status
					String shipmentStatus = eleShipment
							.getAttribute(AcademyConstants.ATTR_STATUS);
					// Check for canceled shipment
					if (shipmentStatus != null && shipmentStatus.equals("9000")) {
						log
								.verbose("Shipment is in Cancelled status .. Therefore, stop the reprint process.. ");
						yfcEx = new YFCException();
						yfcEx.setAttribute(YFCException.ERROR_CODE,"EXTN_ACADEMY_09");
						yfcEx
								.setAttribute(YFCException.ERROR_DESCRIPTION,"Reprint of cancelled Shipment is not allowed.");
						yfcEx.printStackTrace();
						throw yfcEx;
					}
					String strProTrackingNo = null;
					String shipmentType = null;
					String strShipmentType = eleShipment
							.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
					log.verbose("****ShipmentType is:" + strShipmentType);
					// check for White Glove Shipment
					if (strShipmentType != null && strShipmentType.equals("WG")) {
						log.verbose("White Glove Container ... ");
						strProTrackingNo = eleShipment.getAttribute("ProNo");
						log.verbose("ProNo if WGShipment, else Tracking No is : "
								+ strProTrackingNo);
						// Check for container is eligible or not for Reprint
						if (strProTrackingNo != null
								&& strProTrackingNo.trim().length() > 0) {
							log.verbose("Container is eligible for Reprint");
							Document reprintInput = XMLUtil
									.createDocument("Container");
							reprintInput
									.getDocumentElement()
									.setAttribute(
											"ShipmentContainerKey",
											eContainer
													.getAttribute("ShipmentContainerKey"));
							log.verbose("Input to GetDataForPackSlip reprint is : "
									+ XMLUtil.getXMLString(reprintInput));
							env.setTxnObject("ReprintPackSlip", "Y");
							env.setTxnObject("ReprintPrinterId", strPrinterId);
							AcademyUtil.invokeService(env,
									"AcademyPrintShippingLabelForWhiteGlove",
									reprintInput);
							env.setTxnObject("ReprintPrinterId", "");
							env.setTxnObject("ReprintPackSlip", "N");
						}
						// reprintPackSlipForShipment(env, eleShipment, true);
					} else if (strShipmentType != null
							&& (strShipmentType.equals("BLP")
									|| strShipmentType.equals("BULKOVNT") || strShipmentType
									.equals("BNLP"))) {
						log.verbose("Bulk Container ... ");
						strProTrackingNo = eContainer
								.getAttribute(AcademyConstants.ATTR_TRACKING_NO);
						log.verbose("ProNo if WGShipment, else Tracking No is : "
								+ strProTrackingNo);
						if (strProTrackingNo != null
								&& strProTrackingNo.trim().length() > 0) {
							log.verbose("Container is eligible for Reprint");
							Document reprintInput = XMLUtil
									.createDocument("Container");
							reprintInput
									.getDocumentElement()
									.setAttribute(
											"ShipmentContainerKey",
											eContainer
													.getAttribute("ShipmentContainerKey"));
							log.verbose("Input to GetDataForPackSlip reprint is : "
									+ XMLUtil.getXMLString(reprintInput));
							env.setTxnObject("ReprintPackSlip", "Y");
							env.setTxnObject("ReprintPrinterId", strPrinterId);
							AcademyUtil.invokeService(env,
									"AcademyPrintBulkPackSlip", reprintInput);
							env.setTxnObject("ReprintPrinterId", "");
							env.setTxnObject("ReprintPackSlip", "N");
						} else {
							log
									.verbose("Tracking No for a Bulk Shipment or ProNo for a White Glove Shipment isn't stamped.Hence reprint will not execute.");
							yfcEx = new YFCException();
							yfcEx.setAttribute(YFCException.ERROR_CODE,"EXTN_ACADEMY_13");
							yfcEx
									.setAttribute(YFCException.ERROR_DESCRIPTION,"Tracking Number /Pro No doesn't exist for the container.");
							yfcEx.printStackTrace();
							throw yfcEx;
						}
						// reprintPackSlipForShipment(env, eleShipment, false);
					} else {
						log.verbose("Neither White Glove nor Bulk Container...");
						yfcEx = new YFCException();
						yfcEx.setAttribute(YFCException.ERROR_CODE,"EXTN_ACADEMY_14");
						yfcEx
								.setAttribute(YFCException.ERROR_DESCRIPTION,"Neither White Glove nor Bulk Container.Please select correct service.");
						yfcEx.printStackTrace();
						throw yfcEx;
					}
				}
		log
				.endTimer("End of processRepringForBulkContainer - > AcademyReprintPackSlipAndShippingLabel ");
	}

	/**
	 * This method invokes to reprint the shipping container label for
	 * Conveyable
	 * 
	 * @param env
	 * @param inDoc
	 * @throws Exception 
	 */
	public void processReprintForConatinerShippingLabel(YFSEnvironment env,
			Document inDoc) throws Exception {
		log
				.beginTimer("Beginning of processReprintForConatinerShippingLabel - > AcademyReprintPackSlipAndShippingLabel ");
			if (YFSObject.isNull(inDoc) || YFSObject.isVoid(inDoc))
				return;
			log.verbose("input is : " + XMLUtil.getXMLString(inDoc));
			Element elePrinterPreference = (Element) inDoc.getDocumentElement()
					.getElementsByTagName("PrinterPreference").item(0);
			String strPrinterId = elePrinterPreference
					.getAttribute("PrinterId");
			log.verbose("********PrinterId is******" + strPrinterId);
			if (inDoc.getDocumentElement().getElementsByTagName("Container")
					.getLength() <= 0)
				return;
			Element eleContainer = (Element) inDoc.getDocumentElement()
					.getElementsByTagName("Container").item(0);
			if (YFSObject.isNull(eleContainer
					.getAttribute("ShipmentContainerKey"))
					|| YFSObject.isVoid(eleContainer
							.getAttribute("ShipmentContainerKey")))
				return;
			Document inputToContainerList = XMLUtil.createDocument("Container");
			inputToContainerList.getDocumentElement().setAttribute(
					"ShipmentContainerKey",
					eleContainer.getAttribute("ShipmentContainerKey"));
			env
					.setApiTemplate("getShipmentContainerList",
							"global/template/api/getShipmentConatinerList.DetailsForReprint.xml");
			Document shipmentContainerLst = AcademyUtil.invokeAPI(env,
					"getShipmentContainerList", inputToContainerList);
			if (shipmentContainerLst != null
					&& shipmentContainerLst.getDocumentElement()
							.hasChildNodes()) {
				Element eleShipment = (Element) shipmentContainerLst
						.getDocumentElement().getElementsByTagName(
								AcademyConstants.ELE_SHIPMENT).item(0);
				// Get the Shipment Status
				String shipmentStatus = eleShipment
						.getAttribute(AcademyConstants.ATTR_STATUS);
				// Check for canceled shipment
				if (shipmentStatus != null && shipmentStatus.equals("9000")) {
					log
							.verbose("Shipment is in Cancelled status .. Therefore, stop the reprint process.. ");
					yfcEx = new YFCException();
					yfcEx.setAttribute(YFCException.ERROR_CODE,"EXTN_ACADEMY_09");
					yfcEx
							.setAttribute(YFCException.ERROR_DESCRIPTION,"Reprint of cancelled Shipment is not allowed.");
					yfcEx.printStackTrace();
					throw yfcEx;
				}
				String shipmentType = null;
				if (eleShipment != null && eleShipment.hasAttributes())
					shipmentType = eleShipment
							.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
				if (shipmentType != null
						&& (shipmentType.equals("GC") || shipmentType.equalsIgnoreCase(AcademyConstants.STR_GC_ONLY_SHIP_TYPE)
								|| shipmentType.equals("CON") || shipmentType
								.equals("CONOVNT"))) {
					Element eContainer = (Element) shipmentContainerLst
							.getDocumentElement().getElementsByTagName(
									"Container").item(0);
					String strContainerNo = eContainer
							.getAttribute("ContainerNo");
					eleContainer.setAttribute("ContainerNo", strContainerNo);
					Document docContainer = XMLUtil.createDocument("Container");
					XMLUtil.copyElement(docContainer, eleContainer,
							docContainer.getDocumentElement());
					env.setTxnObject("ReprintPackSlip", "Y");
					env.setTxnObject("ReprintPrinterId", strPrinterId);
					AcademyUtil.invokeService(env, "AcademyPrintShippingLabel",
							docContainer);
					env.setTxnObject("ReprintPrinterId", "");
					env.setTxnObject("ReprintPackSlip", "N");
				} else {
					yfcEx = new YFCException();
					yfcEx.setAttribute(YFCException.ERROR_CODE,"EXTN_ACADEMY_15");
					yfcEx
							.setAttribute(YFCException.ERROR_DESCRIPTION,"Reprint of non-conveyable containers labels not allowed.");
					yfcEx.printStackTrace();
					throw yfcEx;
				}
			}
		log
				.endTimer("End of processReprintForConatinerShippingLabel - > AcademyReprintPackSlipAndShippingLabel ");
	}

	
	
	/**
	 * This method invokes to reprint the invoice container label (Return Label) for
	 * Conveyable
	 * 
	 * @param env
	 * @param inDoc
	 * @throws Exception 
	 */
	public void processReprintForInvoiceLabel(YFSEnvironment env,
			Document inDoc) throws Exception {
		log
				.beginTimer("Beginning of processReprintForInvoiceLabel - > AcademyReprintPackSlipAndShippingLabel ");
			if (YFSObject.isNull(inDoc) || YFSObject.isVoid(inDoc))
				return;
			log.verbose("input is : " + XMLUtil.getXMLString(inDoc));
			Element elePrinterPreference = (Element) inDoc.getDocumentElement()
					.getElementsByTagName("PrinterPreference").item(0);
			String strPrinterId = elePrinterPreference
					.getAttribute("PrinterId");
			log.verbose("********PrinterId is******" + strPrinterId);
			if (inDoc.getDocumentElement().getElementsByTagName("Container")
					.getLength() <= 0)
				return;
			Element eleContainer = (Element) inDoc.getDocumentElement()
					.getElementsByTagName("Container").item(0);
			if (YFSObject.isNull(eleContainer
					.getAttribute("ShipmentContainerKey"))
					|| YFSObject.isVoid(eleContainer
							.getAttribute("ShipmentContainerKey")))
				return;
			Document inputToContainerList = XMLUtil.createDocument("Container");
			inputToContainerList.getDocumentElement().setAttribute(
					"ShipmentContainerKey",
					eleContainer.getAttribute("ShipmentContainerKey"));
			env
					.setApiTemplate("getShipmentContainerList",
							"global/template/api/getShipmentConatinerList.DetailsForReprint.xml");
			Document shipmentContainerLst = AcademyUtil.invokeAPI(env,
					"getShipmentContainerList", inputToContainerList);
			if (shipmentContainerLst != null
					&& shipmentContainerLst.getDocumentElement()
							.hasChildNodes()) {
				Element eleShipment = (Element) shipmentContainerLst
						.getDocumentElement().getElementsByTagName(
								AcademyConstants.ELE_SHIPMENT).item(0);
				// Get the Shipment Status
				String shipmentStatus = eleShipment
						.getAttribute(AcademyConstants.ATTR_STATUS);
				// Check for canceled shipment
				if (shipmentStatus != null && shipmentStatus.equals("9000")) {
					log
							.verbose("Shipment is in Cancelled status .. Therefore, stop the reprint process.. ");
					yfcEx = new YFCException();
					yfcEx.setAttribute(YFCException.ERROR_CODE,"EXTN_ACADEMY_09");
					yfcEx
							.setAttribute(YFCException.ERROR_DESCRIPTION,"Reprint of cancelled Shipment is not allowed.");
					yfcEx.printStackTrace();
					throw yfcEx;
				}
				String shipmentType = null;
				if (eleShipment != null && eleShipment.hasAttributes())
					shipmentType = eleShipment
							.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
				if (shipmentType != null
						&& (shipmentType.equals("GC") || shipmentType.equalsIgnoreCase(AcademyConstants.STR_GC_ONLY_SHIP_TYPE)
								|| shipmentType.equals("CON") || shipmentType
								.equals("CONOVNT"))) {
					Element eContainer = (Element) shipmentContainerLst
							.getDocumentElement().getElementsByTagName(
									"Container").item(0);
					String containerStatus =  XPathUtil.getString(eContainer, "Status/@Status");
					if(containerStatus != null && containerStatus.length()>0){
						if(containerStatus.indexOf(".")!=-1)
							containerStatus = containerStatus.substring(0, containerStatus.indexOf("."));				
						int iContainerStatus = Integer.valueOf(containerStatus);
						if(iContainerStatus < 1300){
							// not completed Packing
							yfcEx = new YFCException();
							yfcEx.setAttribute(YFCException.ERROR_CODE,"EXTN_ACADEMY_16");
							yfcEx
									.setAttribute(YFCException.ERROR_DESCRIPTION,"Reprint of non packed conveyable container invoice label not allowed.");
							yfcEx.printStackTrace();
							throw yfcEx;
						}
					}
					String containerInvoiceNo = XPathUtil.getString(eContainer, "Shipment/OrderInvoiceList/OrderInvoice[@InvoiceType='PRO_FORMA']/Extn/@ExtnInvoiceNo");					
					//String strContainerNo = eContainer.getAttribute("ContainerNo");
					//eleContainer.setAttribute("ContainerNo", strContainerNo);
					Document docContainer = XMLUtil.createDocument("Container");
					XMLUtil.copyElement(docContainer, eContainer, docContainer.getDocumentElement());
					//XMLUtil.copyElement(docContainer, eleContainer,docContainer.getDocumentElement());
					docContainer.getDocumentElement().setAttribute("PrinterId", strPrinterId);
					docContainer.getDocumentElement().setAttribute("InvoiceNo", containerInvoiceNo);
					AcademyUtil.invokeService(env, "AcademyPrintConveyableReturnLabel",
							docContainer);					
				} else {
					yfcEx = new YFCException();
					yfcEx.setAttribute(YFCException.ERROR_CODE,"EXTN_ACADEMY_15");
					yfcEx
							.setAttribute(YFCException.ERROR_DESCRIPTION,"Reprint of non-conveyable containers labels not allowed.");
					yfcEx.printStackTrace();
					throw yfcEx;
				}
			}
		log
				.endTimer("End of processReprintForInvoiceLabel - > AcademyReprintPackSlipAndShippingLabel ");
	}

	/**
	 * OMNI-56946 - AcademyBatchPrintServer removal - Start
	 * This method invokes to reprint the Batch Pack Slip/Invoice only for
	 * Conveyable Shipment
	 * 
	 * @param env
	 * @param inDoc
	 * @throws Exception 
	 

	public void processReprintForBatchPackSlip(YFSEnvironment env,
			Document inDoc) throws Exception {
		log
				.beginTimer("Beginning of processReprintForBatchPackSlip - > AcademyReprintPackSlipAndShippingLabel ");
			if (YFSObject.isNull(inDoc) || YFSObject.isVoid(inDoc))
				return;
			log.verbose("input is : " + XMLUtil.getXMLString(inDoc));
			Element elePrinterPreference = (Element) inDoc.getDocumentElement()
					.getElementsByTagName("PrinterPreference").item(0);
			String strPrinterId = elePrinterPreference
					.getAttribute("PrinterId");
			log.verbose("********PrinterId is******"
					+ strPrinterId
					+ " Batch length is "
					+ inDoc.getDocumentElement().getElementsByTagName("Batch")
							.getLength());
			/*
			 * if (inDoc.getDocumentElement().getElementsByTagName("Batch")
			 * .getLength() <= 0) return;
			 */
			/**Element eleBatch = (Element) inDoc.getDocumentElement()
					.getElementsByTagName("Batch").item(0);
			Document docInputGetBatchList = XMLUtil.createDocument("Batch");
			docInputGetBatchList.getDocumentElement().setAttribute("BatchNo",
					eleBatch.getAttribute("BatchNo"));
			env.setApiTemplate("getBatchList",
					"global/template/api/getBatchList.ToBatchPrint.xml");
			Document docOutputGetBatchList = AcademyUtil.invokeAPI(env,
					"getBatchList", docInputGetBatchList);
			log.verbose("**** Output of getBatchList is *********"
					+ XMLUtil.getXMLString(docOutputGetBatchList));
			env.clearApiTemplate("getBatchList");
			if (docOutputGetBatchList != null
					&& docOutputGetBatchList.getDocumentElement()
							.hasChildNodes()) {
				Element eBatch = (Element) docOutputGetBatchList
						.getDocumentElement().getElementsByTagName("Batch")
						.item(0);
				eBatch.setAttribute("PrinterId", strPrinterId);
				String strBatchNo = eBatch.getAttribute("BatchNo");
				String strBatchStatus = eBatch.getAttribute("Status");
				if (strBatchStatus != null && strBatchStatus.equals("9000")) {
					log
							.verbose("Batch is in Cancelled status .. Therefore, stop the reprint process.. ");
					yfcEx = new YFCException();
					yfcEx.setAttribute(YFCException.ERROR_CODE,"EXTN_ACADEMY_16");
					yfcEx
							.setAttribute(YFCException.ERROR_DESCRIPTION,"Reprint of cancelled Batch is not allowed.");
					yfcEx.printStackTrace();
					throw yfcEx;
				}
				log.verbose("****** Batch being executed is *********"
						+ strBatchNo);
				//getShipmentLineList API is called to retrieve list of Shipment associated with the current Wave
				Document docShipmentLineList = getShipmentLineListForWave(env,eBatch.getAttribute("WaveNo"));
				// Create Document with each Batch and shipment details of the Batch and drop in JMS Queue. The format of the message is
				/**
				 * <BatchList>
				 * 	<Batch>
				 * 		<BatchLocations><BatchLocation /></BatchLocations>
				 * 	</Batch>
				 * 	<ShipmentList>
				 * 		<Shipment>
				 * 			<ShipmentLines>
				 * 				<OrderLine>
				 * 					<Item/>
				 * 					<Order>
				 * 						<PaymentMethods><PaymentMethod/></PaymentMethods>
				 * 					</Order>
				 * 				</OrderLine>
				 * 			</ShipmentLines>
				 * 			<BillToAddress/>
				 * 			<ToAddress/>
				 * 			<OrderInvoiceList/>
				 * 			<Instructions><Instruction/><Instructions>
				 * 		</Shipment>
				 * 	</ShipmentList> 
				 * </BatchList>
				 */
				/**docOutputGetBatchList.getDocumentElement().appendChild(docOutputGetBatchList.createElement("ShipmentList"));
				appendShipmentNoToBatchLocation(eBatch,docShipmentLineList.getDocumentElement(),docOutputGetBatchList, strPrinterId);
				log
						.verbose("*** AcademyPrintBatchPackSlip being called for re-printing Batch ******"
								+ XMLUtil.getXMLString(docOutputGetBatchList));
				/*env.setTxnObject("ReprintPackSlip", "Y");
				env.setTxnObject("ReprintPrinterId", strPrinterId);*/
				/*As part of the performance team recommendation, we will be dropping the Batch document into OMS.BATCH_PRINT.OMS queue.
				 * Prints will be generated from the AcademyBatchPrintServer by picking up messages from the above queue
				AcademyUtil.invokeService(env, "AcademyDropMessageForBatchPrint",docOutputGetBatchList);
				/*env.setTxnObject("ReprintPrinterId", "");
				env.setTxnObject("ReprintPackSlip", "N");
			}
		log
				.endTimer("End of processReprintForBatchPackSlip - > AcademyReprintPackSlipAndShippingLabel ");
	}
	*OMNI-56946 - AcademyBatchPrintServer removal - End
	*/

	private void appendShipmentNoToBatchLocation(Element eleBatch, Element shipmentLinelst,Document outDocBatchList, String strPrinterId) throws ParserConfigurationException, Exception {
		log.beginTimer("Bigging of the appendShipmentNoToBatchLocation() ");
		try{
			NodeList nBatchLocation = eleBatch.getElementsByTagName("BatchLocation");		
			log.verbose("number of batchlocations at batch is :"+nBatchLocation.getLength());
			for(int j=0;j < nBatchLocation.getLength();j++){
				Element eleBatchLocation = (Element)nBatchLocation.item(j);
				// get the ShipmentLine of the current Shipment using ShipmentKey
				NodeList lstShipmentLines = XPathUtil.getNodeList(shipmentLinelst, "ShipmentLine[@ShipmentKey='"+eleBatchLocation.getAttribute("ShipmentKey")+"']");
				log.verbose("Total shipment lines at batch location :"+eleBatchLocation.getAttribute("CartLocationId")+" is : "+lstShipmentLines.getLength());
				if(lstShipmentLines.getLength()>0){
					Element eleShipmentList = XMLUtil.getFirstElementByName(outDocBatchList.getDocumentElement(), "ShipmentList");
					Element eleShipment = null;
					Element eleShipmentLines = null;
					for(int indx=0;indx<lstShipmentLines.getLength();indx++){
						Element eleShipmentLine = (Element)lstShipmentLines.item(indx);
						Element eleBatchShipment= null;
						if(indx==0){
							//Create Element Shipment and format as required
							eleBatchShipment = XMLUtil.getFirstElementByName(eleShipmentLine,"Shipment");
							eleShipment = outDocBatchList.createElement("Shipment");
							XMLUtil.copyElement(outDocBatchList, eleBatchShipment, eleShipment);
							eleBatchLocation.setAttribute("ShipmentNo", eleBatchShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));						
							eleShipmentList.appendChild(eleShipment);
							eleShipment.setAttribute("ReprintPrinterId", strPrinterId);
							eleShipment.setAttribute("ReprintPackSlip", "Y");
							eleShipmentLines = outDocBatchList.createElement("ShipmentLines");
							eleShipment.appendChild(eleShipmentLines);
							eleShipmentLine.removeChild(eleBatchShipment);
							// Append to <ShipmentLines>
							Element eleDestShipmentLine = outDocBatchList.createElement("ShipmentLine");
							XMLUtil.copyElement(outDocBatchList, eleShipmentLine, eleDestShipmentLine);
							eleShipmentLines.appendChild(eleDestShipmentLine);
						}else{
							eleBatchShipment = XMLUtil.getFirstElementByName(eleShipmentLine,"Shipment");
							eleShipmentLine.removeChild(eleBatchShipment);
							// Append to <ShipmentLines>
							Element eleDestShipmentLine = outDocBatchList.createElement("ShipmentLine");
							XMLUtil.copyElement(outDocBatchList, eleShipmentLine, eleDestShipmentLine);
							eleShipmentLines.appendChild(eleDestShipmentLine);
						}
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}
		log.endTimer("Ending of appendShipmentNoToBatchLocation() ");
	}
	private Document getShipmentLineListForWave(YFSEnvironment env, String wave){
		Document docShipmentLineList = null;
		log.beginTimer("Begining of getShipmentLineList associated to Wave : getShipmentLineListForWave()");
		try{
			Document inputToShipmentLineLst = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT_LINE);
			inputToShipmentLineLst.getDocumentElement().setAttribute("WaveNo", wave);
			env.setApiTemplate("getShipmentLineList", "global/template/api/getShipmentLineList.ToBatchPrint.xml");
			docShipmentLineList = AcademyUtil.invokeAPI(env, "getShipmentLineList", inputToShipmentLineLst);
			env.clearApiTemplate("getShipmentLineList");
			log.verbose("Output of getShipmentLineList API is :\n"+XMLUtil.getXMLString(docShipmentLineList));
		}catch(Exception ex){
			ex.printStackTrace();
		}
		log.endTimer("End of getShipmentLineList associated to Wave : getShipmentLineListForWave()");
		return docShipmentLineList;
	}
}
