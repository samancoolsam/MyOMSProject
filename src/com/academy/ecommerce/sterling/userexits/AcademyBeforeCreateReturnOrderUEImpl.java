package com.academy.ecommerce.sterling.userexits;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.academy.ecommerce.sterling.util.AcademyPricingAndPromotionUtil;
import com.academy.ecommerce.sterling.util.AcademyReturnOrderUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDoubleUtils;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSBeforeCreateOrderUE;

public class AcademyBeforeCreateReturnOrderUEImpl implements
		YFSBeforeCreateOrderUE {

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyBeforeCreateReturnOrderUEImpl.class);

	public String beforeCreateOrder(YFSEnvironment arg0, String arg1)
			throws YFSUserExitException {

		return arg1;
	}

	/**
	 * return outDoc Returns the final document which is manipulated based on
	 * the input passed
	 * 
	 * This method would call the product (COM) services that needs to be called
	 * before order creation and then calls custom services which woould perform
	 * certain custom logic & stamp attributes
	 */
	public Document beforeCreateOrder(YFSEnvironment env,
			Document returnOrderDoc) throws YFSUserExitException {
		String proNo = "";
		// capturing this value to indicate internally for white Glove Returns,
		// it will come as Y as we do white glove returns from CC.
		String draftOrderFlag = "";
		String isWebOrder = AcademyConstants.STR_NO, documentType, strParentSalesOrderNo;
		boolean isWhiteGloveItem = false;
		String derivedOrderLineKey = null;
		try {
			log
					.beginTimer(" Begining of AcademyBeforeCreateReturnOrderUEImpl-> beforeCreateOrder UE IMpl");
			log
					.verbose("AcademyBeforeCreateReturnOrderUEImpl : beforeCreateOrder - Entering");
			log.verbose("****************** Input Return Order Document :::::"
					+ XMLUtil.getXMLString(returnOrderDoc));
			/*
			 * outYCDServiceOutput = AcademyUtil.invokeService(env,
			 * AcademyConstants.YCD_BEFORE_CREATE_ORDER_SERVICE, doc);
			 * log.verbose("****************** Output Document from YCD Service
			 * :::::" + XMLUtil.getXMLString(outYCDServiceOutput));
			 */
			documentType = returnOrderDoc.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_DOC_TYPE);
			// Below logic Implemented as part of CR#18- For ReturnOrders
			// stamping the Shipping&ShippingTaxCharges at line level by getting
			// from WCS.
			if (documentType.equals(AcademyConstants.STR_RETURN_DOCTYPE)) {
				draftOrderFlag = returnOrderDoc.getDocumentElement()
						.getAttribute("DraftOrderFlag");
				Element E_ExtnEle = (Element) returnOrderDoc
						.getElementsByTagName(AcademyConstants.ELE_EXTN)
						.item(0);
				if (E_ExtnEle != null) {
					if (E_ExtnEle
							.getAttribute(AcademyConstants.ATTR_EXTN_IS_WEBORDER) != "")
						isWebOrder = E_ExtnEle
								.getAttribute(AcademyConstants.ATTR_EXTN_IS_WEBORDER);
				} else {
					E_ExtnEle = returnOrderDoc.createElement("Extn");
					returnOrderDoc.getDocumentElement().appendChild(E_ExtnEle);
				}

				// Api call to get the CLS address details:
				Document getCLSNodeDoc = XMLUtil.createDocument("Organization");
				getCLSNodeDoc.getDocumentElement().setAttribute(
						AcademyConstants.ORGANIZATION_CODE,
						AcademyConstants.STR_CLS_RECVING_NODE);
				env
						.setApiTemplate("getOrganizationHierarchy",
								"global/template/api/getOrganizationHierarchy.createReturnOrderUEImpl.xml");
				Document clsNodeDocDetails = AcademyUtil.invokeAPI(env,
						"getOrganizationHierarchy", getCLSNodeDoc);
				env.clearApiTemplate("getOrganizationHierarchy");

				// logic to stamp PersonInfoShipTo address Details on Return
				// Orders.
				String associatedSalesOrderHeaderKey = ((Element) returnOrderDoc
						.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE)
						.item(0)).getAttribute("DerivedFromOrderHeaderKey");
				Document docOrderDet = XMLUtil
						.createDocument(AcademyConstants.ELE_ORDER);
				docOrderDet.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_ORDER_HEADER_KEY,
						associatedSalesOrderHeaderKey);
				docOrderDet.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_DOC_TYPE,
						AcademyConstants.SALES_DOCUMENT_TYPE);
				// env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
				// "global/template/api/getOrderDetails.createReturnOrderUEImpl.xml");
				env
						.setApiTemplate(
								AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
								"global/template/api/getCompleteOrderDetails.CallToVertex.xml");
				Document associatedSalesOrder = AcademyUtil.invokeAPI(env,
						AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
						docOrderDet);
				env
						.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS);
				// log.verbose("Associated sales order - " +
				// XMLUtil.getXMLString(associatedSalesOrder));

				String soCustEmailID = associatedSalesOrder
						.getDocumentElement().getAttribute(
								AcademyConstants.ATTR_CUST_EMAIL_ID);
				returnOrderDoc.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_CUST_EMAIL_ID, soCustEmailID);
				                        // added CustomerFirstName and CustomerLastName
                String soCustFirstName = associatedSalesOrder
                                    .getDocumentElement().getAttribute("CustomerFirstName");
                returnOrderDoc.getDocumentElement().setAttribute(
                                    "CustomerFirstName", soCustFirstName);
                String soCustLastName = associatedSalesOrder
                                    .getDocumentElement().getAttribute("CustomerLastName");
                returnOrderDoc.getDocumentElement().setAttribute(
                                    "CustomerLastName", soCustLastName);
		

				// # 2880 - Add Notes to Return order
				String attrAction = returnOrderDoc.getDocumentElement()
						.getAttribute(AcademyConstants.ATTR_ACTION);
				if (attrAction != null
						&& attrAction.equals(AcademyConstants.STR_CREATE)
						&& isWebOrder.equalsIgnoreCase(AcademyConstants.STR_NO)) {
					Element eleOrderNotes = null;
					if (returnOrderDoc.getDocumentElement()
							.getElementsByTagName(AcademyConstants.ELE_NOTES)
							.getLength() > 0)
						eleOrderNotes = (Element) returnOrderDoc
								.getDocumentElement().getElementsByTagName(
										AcademyConstants.ELE_NOTES).item(0);
					else
						eleOrderNotes = returnOrderDoc
								.createElement(AcademyConstants.ELE_NOTES);
					strParentSalesOrderNo = associatedSalesOrder
							.getDocumentElement().getAttribute(
									AcademyConstants.ATTR_ORDER_NO);
					Element eleNote = returnOrderDoc
							.createElement(AcademyConstants.ELE_NOTE);
					String noteText = "Return Order created for sales order # "
							+ strParentSalesOrderNo;
					eleNote.setAttribute(AcademyConstants.ATTR_NOTE_TEXT,
							noteText);
					eleOrderNotes.appendChild(eleNote);
					returnOrderDoc.getDocumentElement().appendChild(
							eleOrderNotes);
				}
				// # 2880

				NodeList ordLineEle = returnOrderDoc
						.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
				NodeList salesOrdLineEleList = associatedSalesOrder
						.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
				int salesLineLen = salesOrdLineEleList.getLength();
				int ordLineLen = ordLineEle.getLength();
				// getCommonCodeList for valid Carrier
				Document docGetWGScacCode = null;
				for (int i = 0; i < ordLineLen; i++) {
					for (int j = 0; j < salesLineLen; j++) {
						Element salesOrdLineEle = (Element) salesOrdLineEleList
								.item(j);
						Element ordLineElem = (Element) ordLineEle.item(i);
						if (((salesOrdLineEle.getAttribute("OrderLineKey")))
								.equalsIgnoreCase(((Element) ordLineEle.item(i))
										.getAttribute("DerivedFromOrderLineKey"))) {
							// log.verbose("After Matching line Keys-- Sales
							// Order line" +
							// XMLUtil.getElementXMLString(salesOrdLineEle));
							Element returnLineExtn = XMLUtil.createElement(
									returnOrderDoc, "Extn", null);

							Element extnelement = XMLUtil
									.getFirstElementByName(salesOrdLineEle,
											"Extn");
							XMLUtil.copyElement(returnOrderDoc, extnelement,
									returnLineExtn);
							// log.verbose("Sales Order line details after copy"
							// + XMLUtil.getElementXMLString(returnLineExtn));

							// IN RMA scenario return order already has a
							// existing extn element under orderline. If we
							// append second extn is ignored
							// Append the child if extn element is not present.
							// If present copy attributes.

							Element roExtnElExists = XMLUtil
									.getFirstElementByName((Element) ordLineEle
											.item(i), "Extn");
							if (roExtnElExists != null) {
								// log.verbose("Does RO extn exists? " +
								// XMLUtil.getElementXMLString(roExtnElExists));
								// XMLUtil.getFirstElementByName((Element)ordLineEle.item(i),
								// "Extn").setAttribute("ExtnWCOrderItemIdentifier",
								// returnLineExtn.getAttribute("ExtnWCOrderItemIdentifier"));
								((Element) ((Element) ordLineEle.item(i))
										.getElementsByTagName("Extn").item(0))
										.setAttribute(
												"ExtnWCOrderItemIdentifier",
												returnLineExtn
														.getAttribute("ExtnWCOrderItemIdentifier"));
								// log.verbose("After stamping " +
								// XMLUtil.getElementXMLString((Element)ordLineEle.item(i)));

								// XMLUtil.copyElement(doc, roExtnElExists,
								// returnLineExtn);
							} else {
								ordLineEle.item(i).appendChild(returnLineExtn);
							}

							// log.verbose("Sales Order after copy & adding
							// ShipNode attr cls as -703" +
							// XMLUtil.getXMLString(returnOrderDoc));
							// ((Element)
							// ordLineEle.item(i)).setAttribute("ShipNode",
							// "703");
							// if
							// (isWebOrder.equalsIgnoreCase(AcademyConstants.STR_NO))

							Element ItemDetailsEle = (Element) salesOrdLineEle
									.getElementsByTagName(
											AcademyConstants.ELE_ITEM_DETAILS)
									.item(0);

							// passing 'white_glove' as a CarrierService if
							// Order is
							// WhiteGlove Item else passing as 'Ground' to get
							// the
							// ReturnShippingCharges

							if (((Element) ItemDetailsEle.getElementsByTagName(
									AcademyConstants.ELE_EXTN).item(0))
									.getAttribute(
											AcademyConstants.ATTR_EXTN_WHITE_GLOVE_ELIGIBLE)
									.equalsIgnoreCase(AcademyConstants.ATTR_Y)) {
								((Element) ordLineEle.item(i)).setAttribute(
										"CarrierServiceCode", "white_glove");
								isWhiteGloveItem = true;
								derivedOrderLineKey = ordLineElem
										.getAttribute("DerivedFromOrderLineKey");
								if (docGetWGScacCode == null)
									docGetWGScacCode = getReturnWGCarrier(env);
								String parentScac = salesOrdLineEle
										.getAttribute("SCAC");
								log.verbose("Original Sales Order Scac is : "
										+ parentScac);
								Node scacNode = XPathUtil.getNode(
										docGetWGScacCode,
										"CommonCodeList/CommonCode[@CodeValue='"
												+ parentScac + "']");
								int hasCommonCode = docGetWGScacCode
										.getDocumentElement()
										.getElementsByTagName("CommonCode")
										.getLength();
								log
										.verbose("The length of Return WG SCAC is : "
												+ hasCommonCode);
								if (scacNode == null && hasCommonCode > 0) {
									log
											.verbose(" Return WG Scac from CommonCode is : "
													+ XPathUtil
															.getString(
																	docGetWGScacCode,
																	"CommonCodeList/CommonCode/@CodeValue")
													+ "\n ScacAndService is : "
													+ XPathUtil
															.getString(
																	docGetWGScacCode,
																	"CommonCodeList/CommonCode/@CodeShortDescription"));
									((Element) ordLineEle.item(i))
											.setAttribute(
													"SCAC",
													XPathUtil
															.getString(
																	docGetWGScacCode,
																	"CommonCodeList/CommonCode/@CodeValue"));
									((Element) ordLineEle.item(i))
											.setAttribute(
													"ScacAndService",
													XPathUtil
															.getString(
																	docGetWGScacCode,
																	"CommonCodeList/CommonCode/@CodeShortDescription"));
								} else {
									// The SCAC of Return White Glove is valid.
									// Therefore, copy same from Parent Sales
									// order to Return Order
									((Element) ordLineEle.item(i))
											.setAttribute(
													"SCAC",
													salesOrdLineEle
															.getAttribute("SCAC"));
									log
											.verbose("ScacAndService At line Level"
													+ salesOrdLineEle
															.getAttribute("ScacAndService"));
									((Element) ordLineEle.item(i))
											.setAttribute(
													"ScacAndService",
													salesOrdLineEle
															.getAttribute("ScacAndService"));
								}
							} else {
								((Element) ordLineEle.item(i)).setAttribute(
										"CarrierServiceCode", "Ground");
								// Modified as part of CR - New Carrier for
								// White Glove
								((Element) ordLineEle.item(i)).setAttribute(
										"SCAC", salesOrdLineEle
												.getAttribute("SCAC"));
								log
										.verbose("ScacAndService At line Level"
												+ salesOrdLineEle
														.getAttribute("ScacAndService")
												+ "   salesOrdLineEle=="
												+ XMLUtil
														.getElementXMLString(salesOrdLineEle));
								((Element) ordLineEle.item(i))
										.setAttribute(
												"ScacAndService",
												salesOrdLineEle
														.getAttribute("ScacAndService"));

							}
						}
					}
				}
				log
						.verbose("******Calling AcademyReturnOrderUtil.addingAddresses - & Copying onto the RETURN ORDER ******* ");
				returnOrderDoc = AcademyReturnOrderUtil
						.addingAddresses(returnOrderDoc, associatedSalesOrder,
								clsNodeDocDetails);
				// log.verbose("after calling
				// AcademyReturnOrderUtil.addingAddresses
				// doc"+XMLUtil.getXMLString(returnOrderDoc));

				if (isWebOrder.equalsIgnoreCase(AcademyConstants.STR_NO)) {
					// Setting this flag as Y to invoke Re-Order Pricing UE Impl
					env.setTxnObject("RepricingFlag", AcademyConstants.STR_YES);
					// log.verbose("Entering White Glove Tax Calculation.... " +
					// XMLUtil.getXMLString(returnOrderDoc));

					/**
					 * Call the custom return shipping charges logic and Vertex
					 * if it's white glove item. if(isWhiteGloveItem) - Also
					 * checking DraftOrderFlag="Y" or not . if white glove
					 * return creates from CC then only we are doing return Tax
					 * cal from VERTEX interface
					 */
					if (isWhiteGloveItem
							&& (draftOrderFlag
									.equalsIgnoreCase(AcademyConstants.STR_YES)))

					{
						log.verbose(" Inside the WHite GLOVE tax cal Logic ");
						/**
						 * CR - New White Glove Carrier and Prorate WG shipping
						 * charges The Pro# should be new pro# of return to
						 * track.
						 */
						Document outPRODoc = getPRONumber(env);
						log.verbose("*************** pro number : "
								+ XMLUtil.getXMLString(outPRODoc));
						proNo = outPRODoc.getDocumentElement().getAttribute(
								"ProNumber");

						if ((YFCObject.isNull(proNo) || YFCObject.isVoid(proNo))
								&& associatedSalesOrder.getElementsByTagName(
										"Shipment").item(0) != null) {
							Element eleShipment = (Element) XPathUtil.getNode(
									associatedSalesOrder.getDocumentElement(),
									"Shipments/Shipment[ShipmentLines/ShipmentLine/@OrderLineKey='"
											+ derivedOrderLineKey + "']");
							if (eleShipment != null)
								proNo = eleShipment.getAttribute("ProNo");
							else
								proNo = ((Element) associatedSalesOrder
										.getElementsByTagName("Shipment").item(
												0)).getAttribute("ProNo");
							log
									.verbose("inside the shipment details & getting the proNumber to stamp on white glove retuns"
											+ proNo);
						}
						E_ExtnEle.setAttribute("ExtnProNo", proNo);
						env.setTxnObject("isWGReturns",
								AcademyConstants.STR_YES);
						// Disabling as fix for 4964
						// Document vertexOutputDoc = invokeVertexCall(env,
						// associatedSalesOrder, returnOrderDoc);
						invokeVertexCall(env, associatedSalesOrder,
								returnOrderDoc);

						/*
						 * if (vertexOutputDoc != null) { log .verbose("response
						 * Doc from Vertex is & commenting below Mappart" +
						 * XMLUtil .getXMLString(vertexOutputDoc));
						 * mapGetVertexOrderResponseToUEOutput(returnOrderDoc,
						 * vertexOutputDoc); log .verbose("after Calling
						 * mapGetVertexOrderResponseToUEOutput - the returnDoc
						 * is " + XMLUtil .getXMLString(returnOrderDoc)); //
						 * env.setTxnObject("isWGReturns", //
						 * AcademyConstants.STR_YES); }
						 */
					}

				}
			}
			log
					.endTimer(" End of AcademyBeforeCreateReturnOrderUEImpl-> beforeCreateOrder UE IMpl");
		} catch (Exception e) {
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
			// throw new YFSUserExitException();
		}

		/**
		 * Removing the order header key added during vertex request.
		 */
		returnOrderDoc.getDocumentElement().setAttribute("OrderHeaderKey", "");

		log.verbose("Output from beforeCreateOrder - "
				+ XMLUtil.getXMLString(returnOrderDoc));
		log
				.verbose("AcademyBeforeCreateReturnOrderUEImpl : beforeCreateOrder - Exiting");

		return returnOrderDoc;
	}

	private Document getReturnWGCarrier(YFSEnvironment env) throws Exception {
		Document docWGSCACLst = null;
		try {
			Document docScacCodeInput = XMLUtil
					.createDocument(AcademyConstants.ELE_COMMON_CODE);
			docScacCodeInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_CODE_TYPE,
					AcademyConstants.STR_RET_WG_SCAC_CODE);
			docScacCodeInput.getDocumentElement().setAttribute(
					AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			env.setApiTemplate(AcademyConstants.API_GET_COMMON_CODELIST,
					"global/template/api/getCommonCodeList.IsWhiteGloveSCAC.xml");
			docWGSCACLst = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_COMMON_CODELIST, docScacCodeInput);
			env.clearApiTemplate(AcademyConstants.API_GET_COMMON_CODELIST);
		} catch (Exception e) {
			e.printStackTrace();
			log.verbose("Failed to invoke getCommonCodeList API : " + e);
			throw e;
		}
		return docWGSCACLst;
	}

	/**
	 * Sending the return invoice detail to Vertex and get the tax details
	 * 
	 * @param env -
	 *            YFSEnvironment
	 * @param associatedSalesOrder -
	 *            Document
	 * @throws Exception
	 */
	private void invokeVertexCall(YFSEnvironment env,
			Document associatedSalesOrder, Document returnOrderDoc)
			throws Exception {
		Document vertexDistributeCallResp = null;
		log
				.verbose("AcademyBeforeCreateReturnOrderUEImpl : invokeVertexCall - Entering");
		try {
			log
					.beginTimer(" Begining of AcademyBeforeCreateReturnOrderUEImpl-> invokeVertexCall ");
			if (env.getTxnObject("ReturnInvoiceCall") == null) {
				if (!YFCObject.isVoid(associatedSalesOrder)) {
					String orderHeaderKey = associatedSalesOrder
							.getDocumentElement()
							.getAttribute("OrderHeaderKey");
					log.verbose("Sales Order Header Key - " + orderHeaderKey);

					// Calculating the return shipping charges.

					returnOrderDoc = this
							.calculateReturnShippingChargesForWhiteGlove(env,
									returnOrderDoc, associatedSalesOrder);
					// madhura
					returnOrderDoc = this
							.calculateReturnShippingTaxesForWhiteGlove(env,
									returnOrderDoc, associatedSalesOrder);

					log
							.verbose("AcademyBeforeCreateReturnOrderUEImpl - Before Invoking Vertex services - Return Doc is"
									+ XMLUtil.getXMLString(returnOrderDoc));
					// do the distribute call
					env.setTxnObject("ReturnDistributeCall", "Y");

					// convert the input to Vertex Quote Call Request xml.
					returnOrderDoc.getDocumentElement().setAttribute(
							"OrderHeaderKey", orderHeaderKey);
					returnOrderDoc.getDocumentElement().setAttribute(
							"CallType", "QuoteCall");
					// START- CR - Vertex Changes; Sterling Function name and
					// Original Sales Order no as Flexible fields 6 and 7
					returnOrderDoc.getDocumentElement().setAttribute(
							"TranType", "CreateDraftReturn");
					returnOrderDoc.getDocumentElement().setAttribute(
							"DerivedOrderNo",
							associatedSalesOrder.getDocumentElement()
									.getAttribute(
											AcademyConstants.ATTR_ORDER_NO));
					//KER-12036 : Payment Migration Changes to support new Payment Type
					String paymentType = XPathUtil
							.getString(
									associatedSalesOrder,
									"Order/PaymentMethods/PaymentMethod[@SuspendAnyMoreCharges='N' and (@PaymentType='CREDIT_CARD' or @PaymentType='Credit_Card')]/@PaymentType");

					if (YFCObject.isVoid(paymentType)) {
						paymentType = XPathUtil
								.getString(
										associatedSalesOrder,
										"Order/PaymentMethods/PaymentMethod[@SuspendAnyMoreCharges='N' and @PaymentType='GIFT_CARD']/@PaymentType");
					}
					log.verbose("returning PaymentType " + paymentType);
					returnOrderDoc.getDocumentElement().setAttribute(
							"DerivedPaymentTender", paymentType);
					// End - CR
					// Disabling as part of fix for 4964
					// Document vertexDistributeCallReq = AcademyUtil
					// .invokeService(
					// env,
					// "AcademyChangeReturnOrderToQuoteCallRequest",
					// returnOrderDoc);
					// log.verbose("Request XML for Vertex call is "
					// + XMLUtil.getXMLString(vertexDistributeCallReq));
					// Document vertexDistributeCallReq =
					// AcademyUtil.invokeService(env,
					// "AcademyChangeOrderToDistributeCallRequest", returnDoc);
					// vertexDistributeCallResp = XMLUtil.getDocument(new
					// File("/dbdata/Sterling_Files/Build/Logs/VertexResponse.xml").toString());
					// vertexDistributeCallResp = AcademyUtil.invokeService(env,
					// "AcademyVertexQuoteCallRequest",
					// vertexDistributeCallReq);
					// log
					// .verbose("Request XML for Vertex call is
					// vertexDistributeCallResp- "
					// + XMLUtil
					// .getXMLString(vertexDistributeCallResp));
					// if (vertexDistributeCallReq != null) {
					// log
					// .verbose("AcademyBeforeCreateReturnOrderUEImpl - vertex
					// distribute call request : "
					// + XMLUtil
					// .getXMLString(vertexDistributeCallReq));
					// }

					/*
					 * if (vertexDistributeCallResp != null) { log
					 * .verbose("AcademyBeforeCreateReturnOrderUEImpl - vertex
					 * distribute call response : " + XMLUtil
					 * .getXMLString(vertexDistributeCallResp)); }
					 */
					returnOrderDoc.getDocumentElement().setAttribute(
							"OrderHeaderKey", "");

				}
			}
			log
					.endTimer(" End of AcademyBeforeCreateReturnOrderUEImpl-> invokeVertexCall "
							+ XMLUtil.getXMLString(returnOrderDoc));
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		log
				.verbose("AcademyBeforeCreateReturnOrderUEImpl : invokeVertexCall - Exiting");
		// return vertexDistributeCallResp;
		// return returnDoc;
	}

	/**
	 * Method to construct the tax response from the Vertex response.
	 * 
	 * @param docVertexResponse
	 *            Document
	 * @param sOrdLineKey
	 *            String
	 * @return aMap HashMap
	 */
	// public HashMap<String, Double> getVertexTaxAmt(Document
	// docVertexResponse, String sOrdLineKey)
	public String getVertexTaxAmt(Document docVertexResponse, String sOrdLineKey) {
		HashMap<String, Double> aMap = new HashMap<String, Double>();
		String vertexReturnShippingTax = "0";
		double vertexMerchandiseTax = 0.0;
		try {
			log
					.beginTimer("Begining of AcademyBeforeCreateReturnOrderUEImpl-> getVertexTaxAmt ");
			NodeList nlLineList = docVertexResponse
					.getElementsByTagName("LineItem");
			int iNoOfLines = nlLineList.getLength();
			for (int i = 0; i < iNoOfLines; i++) {
				Element eleLineItem = (Element) nlLineList.item(i);
				log.verbose("Vertex Line..."
						+ XMLUtil.getElementXMLString(eleLineItem));
				NodeList nlCodeFields = eleLineItem
						.getElementsByTagName("FlexibleCodeField");
				int iNoOfCodeFields = nlCodeFields.getLength();
				String sFieldValue = "";
				for (int j = 0; j < iNoOfCodeFields; j++) {
					Element eleCodeField = (Element) nlCodeFields.item(j);
					log.verbose("Vertex Code Feidl Line..."
							+ XMLUtil.getElementXMLString(eleCodeField));
					log.verbose("Vertex Code Feidl Line value..."
							+ eleCodeField.getTextContent());
					if (eleCodeField.getAttribute("fieldId").equals("3")) {
						sFieldValue = eleCodeField.getTextContent();
						log.verbose("checking -- linkeKeys" + sFieldValue);
						if (sFieldValue.equalsIgnoreCase(sOrdLineKey)) {
							log
									.verbose("if corresponding lines were matched then get the total Tax amount");
							vertexReturnShippingTax = ((Element) eleLineItem
									.getElementsByTagName("TotalTax").item(0))
									.getTextContent();
						}
						log.verbose("field 3 value..." + sFieldValue + ".."
								+ sFieldValue.indexOf(sOrdLineKey));
					}
				}

			}
			log
					.endTimer("End of AcademyBeforeCreateReturnOrderUEImpl-> getVertexTaxAmt ");
		} catch (Exception ex) {
			this.getYFSUserExceptionWithTrace(ex);
		}
		return vertexReturnShippingTax;
	}

	// TODO: Work Around solution (Vertex Class Cast Exception) for development.
	// Don't forget to remove it
	private String getVertexTaxAmt(Document docVertexResponse,
			double rtnChargePerLine) {
		String vertexReturnShippingTax = "0";
		try {
			log
					.beginTimer("Begining of AcademyBeforeCreateReturnOrderUEImpl-> getVertexTaxAmt ");
			NodeList nlLineList = docVertexResponse
					.getElementsByTagName("LineItem");
			int iNoOfLines = nlLineList.getLength();
			for (int i = 0; i < iNoOfLines; i++) {
				Element eleLineItem = (Element) nlLineList.item(i);
				log.verbose("Vertex Line..."
						+ XMLUtil.getElementXMLString(eleLineItem));
				// TODO: work around solution
				String vertexReturnShippingCharge = ((Element) eleLineItem
						.getElementsByTagName("ExtendedPrice").item(0))
						.getTextContent();
				if (rtnChargePerLine == Double
						.parseDouble(vertexReturnShippingCharge))
					vertexReturnShippingTax = ((Element) eleLineItem
							.getElementsByTagName("TotalTax").item(0))
							.getTextContent();
			}
		} catch (Exception ex) {
			this.getYFSUserExceptionWithTrace(ex);
		}
		log
				.endTimer("End of AcademyBeforeCreateReturnOrderUEImpl-> getVertexTaxAmt "
						+ vertexReturnShippingTax);
		return vertexReturnShippingTax;
	}

	/**
	 * Map the vertex response to Return Order UE Output
	 * 
	 * Method below construct the line taxes.
	 * 
	 * <LineTaxes> <LineTax TaxName="Merchandise" ChargeCategory="TAXES"
	 * ChargeName="Taxes" ChargeNameKey="" InvoicedTax="" Reference_1=""
	 * Reference_2="" Reference_3="" RemainingTax="" Tax="" TaxPercentage="" />
	 * <LineTax TaxName="ShippingTax" ChargeCategory="Shipping"
	 * ChargeName="ShippingCharge" ChargeNameKey="" InvoicedTax=""
	 * Reference_1="" Reference_2="" Reference_3="" RemainingTax="" Tax=""
	 * TaxPercentage="" /> </LineTaxes>
	 * 
	 * @param orderOutputDoc
	 *            Document
	 * @param vertexOutputDoc
	 *            Document
	 * @return orderOutputDoc Document
	 * @throws Exception
	 * 
	 */
	private Document mapGetVertexOrderResponseToUEOutput(Document inDoc,
			Document docVertexResponse) throws Exception {
		log
				.beginTimer("End of AcademyBeforeCreateReturnOrderUEImpl-> mapGetVertexOrderResponseToUEOutput ");
		log
				.verbose("AcademyBeforeCreateReturnOrderUEImpl : mapGetVertexOrderResponseToUEOutput - Entering");

		YFCDocument yfcDoc = YFCDocument.getDocumentFor(inDoc);
		YFCNodeList<YFCElement> orderLineList = yfcDoc
				.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
		String vertexReturnShippingTax = "0";
		YFCElement lineTaxes, lineTaxCharge = null;

		int ordLineLength = orderLineList.getLength();

		for (int i = 0; i < ordLineLength; i++) {
			YFCElement orderLine = (YFCElement) orderLineList.item(i);
			// Get the tax detail for the order line key
			// TODO: work around to be continue development
			/*
			 * YFCNodeList<YFCElement> lstCharges =
			 * orderLine.getElementsByTagName("LineCharge"); for(int a=0; a<lstCharges.getLength();
			 * a++){ YFCElement nLineCharge = lstCharges.item(a);
			 * if(nLineCharge.getAttribute("ChargeCategory").equals("ReturnShippingCharge")){
			 * vertexReturnShippingTax = getVertexTaxAmt(docVertexResponse,
			 * Double.parseDouble(nLineCharge.getAttribute("ChargePerLine"))); } }
			 */

			vertexReturnShippingTax = this.getVertexTaxAmt(docVertexResponse,
					orderLine.getAttribute("OrderLineKey"));
			log.verbose("vertexReturnShippingTax-- from Vertex Systems is "
					+ vertexReturnShippingTax);
			// Fix for Bug# 4964
			Double chargeReturnShippingTax = Double
					.parseDouble(vertexReturnShippingTax);

			// Merchandise Taxes
			lineTaxes = orderLine.getElementsByTagName("LineTaxes").item(0);
			if (YFCObject.isVoid(lineTaxes)) {
				lineTaxes = orderLine.createChild("LineTaxes");
			}
			// Commenting this part as Merchandise Tax copied from SO
			// Return Shipping Taxes
			lineTaxCharge = yfcDoc.createElement(AcademyConstants.ELE_LINE_TAX);
			lineTaxCharge.setAttribute(AcademyConstants.ATTR_CHARGE_CATEGORY,
					AcademyConstants.STR_RETURNSHIPPING_CHARGE);
			lineTaxCharge.setAttribute(AcademyConstants.ATTR_CHARGE_NAME,
					AcademyConstants.STR_RETURNSHIPPING_CHARGE);
			lineTaxCharge.setAttribute(AcademyConstants.ATTR_TAX_NAME,
					AcademyConstants.STR_RETURNSHIP_TAX);
			// Fix for Bug# 4964
			lineTaxCharge.setAttribute(AcademyConstants.ATTR_TAX,
					chargeReturnShippingTax.toString());
			// lineTaxCharge.setAttribute(AcademyConstants.ATTR_TAX, String
			// .valueOf(vertexReturnShippingTax));
			// lineTaxCharge.setAttribute(AcademyConstants.ATTR_REM_TAXES,
			// String.valueOf(vertexReturnShippingTax));
			lineTaxes.appendChild(lineTaxCharge);
			orderLine.appendChild(lineTaxes);
		}
		log
				.verbose("before exiting mapGetVertexOrderResponseToUEOutput -  XML Doc is "
						+ XMLUtil.getXMLString(inDoc));

		return inDoc;
	}

	/**
	 * Method to calculate the return shipping charges for white glove item.
	 * 
	 * Changes to the implementation (For white glove returns) - Sterling will
	 * not call out to website for returns shipping charge or for return
	 * shipping tax. - Return shipping charge would be copied/obtained from the
	 * outbound sales order (outbound shipping charge).
	 * 
	 * @param returnOrderDoc
	 * @param salesOrderDoc
	 * @return
	 */
	public Document calculateReturnShippingChargesForWhiteGlove(
			YFSEnvironment env, Document returnOrderDoc, Document salesOrderDoc) {
		Element returnOrderLine, eleSalesOrderLine, salesLineCharges, eleRefundableReasonCode = null;
		Document docRefundableReasonCodes = null;
		String salesOrderQty, returnOrderLineKey, returnOrderQty, returnShippingCharge = null;
		String strReturnReasonCode = "";
		log
				.verbose("AcademyBeforeCreateReturnOrderUEImpl : calculateReturnShippingChargesForWhiteGlove - Entering - and returDoc is "
						+ XMLUtil.getXMLString(returnOrderDoc));

		if (salesOrderDoc != null) {
			log
					.verbose("AcademyBeforeCreateReturnOrderUEImpl : calculateReturnShippingChargesForWhiteGlove - Input XML");
			// log.verbose(XMLUtil.getXMLString(salesOrderDoc));
		}

		try {
			log
					.beginTimer("Begining of AcademyBeforeCreateReturnOrderUEImpl-> calculateReturnShippingChargesForWhiteGlove ");
			NodeList returnOrderLineList = XPathUtil.getNodeList(returnOrderDoc
					.getDocumentElement(), AcademyConstants.XPATH_ORDERLINE);
			int returnOrdLineLength = returnOrderLineList.getLength();
			log.verbose("Number of Order Lines are :" + returnOrdLineLength);
			for (int i = 0; i < returnOrdLineLength; i++) {
				log.verbose("inside the For Loop");
				returnOrderLine = (Element) returnOrderLineList.item(i);

				/* Fix for bug # 4964 */
				strReturnReasonCode = returnOrderLine
						.getAttribute("ReturnReason");

				docRefundableReasonCodes = AcademyReturnOrderUtil
						.getRefundableReasonCodeValuesList(env);

				eleRefundableReasonCode = (Element) XPathUtil.getNode(
						docRefundableReasonCodes.getDocumentElement(),
						"CommonCode[@CodeValue='" + strReturnReasonCode + "']");

				returnOrderLineKey = returnOrderLine
						.getAttribute(AcademyConstants.ATTR_DERIVEDFROM_ORDERLINE_KEY);
				returnOrderQty = returnOrderLine
						.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
				// CR - Vertex Changes; Apply sorting on OrderLine/@PrimeLineNo.
				// Therefore, set the PrimeLineNo
				int lineNo = i + 1;
				returnOrderLine.setAttribute(
						AcademyConstants.ATTR_PRIME_LINE_NO, String
								.valueOf(lineNo));

				eleSalesOrderLine = (Element) XPathUtil.getNode(salesOrderDoc
						.getDocumentElement(),
						"OrderLines/OrderLine[@OrderLineKey='"
								+ returnOrderLineKey + "']");
				salesOrderQty = eleSalesOrderLine
						.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);

				/**
				 * If any one quantity is returne per order line, the entire
				 * shipping charges will be returned, if it's white glove items.
				 */
				if ((returnOrderQty != null)
						&& !returnOrderQty.equalsIgnoreCase("0.00")) {
					log.verbose("first Check");

					salesLineCharges = (Element) XPathUtil.getNode(
							eleSalesOrderLine,
							"LineCharges/LineCharge[@ChargeCategory='"
									+ "Shipping" + "']");
					log
							.verbose("AcademyBeforeCreateReturnOrderUEImpl - salesLineCharges:"
									+ XMLUtil
											.getElementXMLString(salesLineCharges));
					if (!YFCObject.isVoid(salesLineCharges)) {
						String prorateVal = "";
						Element lineChargesEle = null;
						Element lineChargeEle = null;
						Element lineshippingChargeEle = null;
						returnShippingCharge = salesLineCharges
								.getAttribute("ChargePerLine");
						lineChargesEle = returnOrderDoc
								.createElement("LineCharges");
						log
								.verbose("if its Sales Order Charge is not Void then Add the returnShipping Charges to Order Line");
						if (YFCObject.isVoid(eleRefundableReasonCode)) {

							// CR - Return Shipping Charges Computation
							prorateVal = getProratedReturnShippingCharge(
									returnShippingCharge, returnOrderQty,
									salesOrderQty, eleSalesOrderLine,
									salesOrderDoc.getDocumentElement());

							lineChargeEle = returnOrderDoc
									.createElement("LineCharge");
							// lineshippingChargeEle = returnOrderDoc
							// .createElement("LineCharge");
							lineChargeEle.setAttribute(
									AcademyConstants.ATTR_CHARGE_CATEGORY,
									AcademyConstants.STR_RETURNSHIPPING_CHARGE);
							lineChargeEle.setAttribute(
									AcademyConstants.ATTR_CHARGE_NAME,
									AcademyConstants.STR_RETURNSHIPPING_CHARGE);
							lineChargeEle.setAttribute(
									AcademyConstants.ATTR_CHARGES_PER_LINE,
									prorateVal);
							lineChargeEle.setAttribute("ChargeAmount",
									prorateVal);
							lineChargesEle.appendChild((Node) lineChargeEle);

							// CR - Pro rate Outbound shipping charges
						} else {
							prorateVal = getProratedShippingCharges(
									returnShippingCharge, returnOrderQty,
									salesOrderQty, eleSalesOrderLine,
									salesOrderDoc.getDocumentElement());

							lineshippingChargeEle = returnOrderDoc
									.createElement("LineCharge");

							lineshippingChargeEle.setAttribute(
									AcademyConstants.ATTR_CHARGE_CATEGORY,
									AcademyConstants.STR_SHIPPING);
							lineshippingChargeEle.setAttribute(
									AcademyConstants.ATTR_CHARGE_NAME,
									AcademyConstants.STR_SHIPPING_CHARGE);
							lineshippingChargeEle.setAttribute(
									AcademyConstants.ATTR_CHARGES_PER_LINE,
									prorateVal);
							lineshippingChargeEle.setAttribute("ChargeAmount",
									prorateVal);
							lineshippingChargeEle.setAttribute("Reference",
									"yes");

							lineChargesEle
									.appendChild((Node) lineshippingChargeEle);
						}
						returnOrderLine.appendChild((Node) lineChargesEle);
						log
								.verbose("AcademyBeforeCreateReturnOrderUEImpl calculateReturnShippingChargesForWhiteGlove- returnOrderLine:"
										+ XMLUtil
												.getElementXMLString(returnOrderLine));

					} else {
						log.verbose("Sales Order Charge is Void");
					}
					/*					
					 */
				}
			}
			log
					.verbose("AcademyBeforeCreateReturnOrderUEImpl - calculateReturnShippingChargesForWhiteGlove :"
							+ XMLUtil.getXMLString(returnOrderDoc));
			log
					.endTimer("End of AcademyBeforeCreateReturnOrderUEImpl-> calculateReturnShippingChargesForWhiteGlove ");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		log
				.verbose("AcademyBeforeCreateReturnOrderUEImpl : calculateReturnShippingChargesForWhiteGlove - Exiting -");
		log.verbose("Shipping Calculation output -"
				+ XMLUtil.getXMLString(returnOrderDoc));

		return returnOrderDoc;
	}

	// Gets the pro-rate Return Shipping Charges based on return qty. Compute
	// Penny if the Return Qty is the last qty of the SO line item
	private String getProratedShippingCharges(String rtnShippingCharges,
			String strReturnedQty, String strOrigOrderLineQty,
			Element origSalesOrderLine, Element parentSalesOrder)
			throws Exception {
		log.verbose("Start calculation of Prorate Shipping Charges........");
		float qtyBeingRtn = new Float(strReturnedQty).floatValue();
		String parentOLK = origSalesOrderLine
				.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);

		NodeList lstReturnQtyStatus = XPathUtil
				.getNodeList(origSalesOrderLine,
						"OrderStatuses/OrderStatus[@Status='3700.02' or @Status='3700.01']");
		log.verbose("Already had return order/s : "
				+ lstReturnQtyStatus.getLength());
		float prevProcessedRtnQty = 0;
		for (int indx = 0; indx < lstReturnQtyStatus.getLength(); indx++) {
			Element eleReturnQtyStatus = (Element) lstReturnQtyStatus
					.item(indx);
			prevProcessedRtnQty += Float.parseFloat(eleReturnQtyStatus
					.getAttribute("StatusQty"));
		}
		log.verbose("Already returned Qty is : " + prevProcessedRtnQty);
		float totalRtnQty = prevProcessedRtnQty + qtyBeingRtn;
		log.verbose("Total return qty including the processing qty is : "
				+ totalRtnQty);
		boolean reqRoundedVal = false;
		if (Float.parseFloat(strOrigOrderLineQty) == totalRtnQty) {
			reqRoundedVal = true;
		}
		log.verbose("Required rounded value : " + reqRoundedVal);
		Float dRtnShippingCharges = Float.valueOf(rtnShippingCharges);
		float dRetQty = Float.parseFloat(strReturnedQty);
		float dSOQty = Float.parseFloat(strOrigOrderLineQty);
		float prorateVal = ((dRtnShippingCharges * dRetQty) / dSOQty);
		String strProrateVal = String.valueOf(prorateVal);
		float prevReturnedCharges = 0;
		log.verbose("ChargeCategory : " + AcademyConstants.STR_SHIPPING);
		if (parentSalesOrder != null) {
			NodeList lstROLineCharges = XPathUtil.getNodeList(parentSalesOrder,
					"ReturnOrders/ReturnOrder/OrderLines/OrderLine[@DerivedFromOrderLineKey='"
							+ parentOLK
							+ "']/LineCharges/LineCharge[@ChargeCategory='"
							+ AcademyConstants.STR_SHIPPING + "']");
			log.verbose(" total line charges of Past ROs : "
					+ lstROLineCharges.getLength());
			for (int ltIndx = 0; ltIndx < lstROLineCharges.getLength(); ltIndx++) {
				Element prevCharge = (Element) lstROLineCharges.item(ltIndx);
				prevReturnedCharges += Float.parseFloat(prevCharge
						.getAttribute(AcademyConstants.ATTR_CHARGE_AMT));
			}
		}
		// Current RO Line not have any Shipping Charges at this point.
		// Therefore, take Prorate value for further calculations.
		log.verbose(Float.parseFloat(rtnShippingCharges) + " - "
				+ prevReturnedCharges);
		float remainCharges = Float.parseFloat(rtnShippingCharges)
				- prevReturnedCharges;
		log.verbose(" Remain Charge is :: " + remainCharges);
		if (!reqRoundedVal) {
			if (remainCharges > 0) {
				strProrateVal = truncateDecimal(strProrateVal,
						AcademyConstants.INT_NUMBER_OF_DECIMALS);
			} else
				strProrateVal = "0";
			log.verbose("after truncate the prorated value is : "
					+ strProrateVal);
		} else if (new Float(rtnShippingCharges).floatValue() != 0) {
			if (remainCharges > 0) {
				float delta = remainCharges - prorateVal;
				if (YFCDoubleUtils.roundOff(delta) > 0.09) {
					strProrateVal = truncateDecimal(String
							.valueOf(strProrateVal),
							AcademyConstants.INT_NUMBER_OF_DECIMALS);
				} else {
					double d = Double
							.parseDouble(String.valueOf(remainCharges));
					log
							.verbose("Before Round off the last qty to be return is :: "
									+ d);
					strProrateVal = String.valueOf(YFCDoubleUtils.roundOff(d,
							AcademyConstants.INT_NUMBER_OF_DECIMALS));
				}
			} else
				strProrateVal = "0";
		}
		log.verbose("Final prorated value is :: " + strProrateVal);
		log.verbose("Ending Prorate the Shipping Charges........");
		return strProrateVal;
	}

	private String getProratedReturnShippingCharge(String rtnShippingCharges,
			String strReturnedQty, String strOrigOrderLineQty,
			Element origSalesOrderLine, Element parentSalesOrder)
			throws Exception {
		float qtyBeingRtn = new Float(strReturnedQty).floatValue();
		String parentOLK = origSalesOrderLine
				.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);

		NodeList lstReturnQtyStatus = XPathUtil
				.getNodeList(origSalesOrderLine,
						"OrderStatuses/OrderStatus[@Status='3700.02' or @Status='3700.01']");
		log.verbose("Already had return order/s : "
				+ lstReturnQtyStatus.getLength());
		float prevProcessedRtnQty = 0;
		for (int indx = 0; indx < lstReturnQtyStatus.getLength(); indx++) {
			Element eleReturnQtyStatus = (Element) lstReturnQtyStatus
					.item(indx);
			prevProcessedRtnQty += Float.parseFloat(eleReturnQtyStatus
					.getAttribute("StatusQty"));
		}
		log.verbose("Already returned Qty is : " + prevProcessedRtnQty);
		float totalRtnQty = prevProcessedRtnQty + qtyBeingRtn;
		log.verbose("Total return qty including the processing qty is : "
				+ totalRtnQty);
		boolean reqRoundedVal = false;
		if (Float.parseFloat(strOrigOrderLineQty) == totalRtnQty) {
			reqRoundedVal = true;
		}
		log.verbose("Required rounded value : " + reqRoundedVal);
		Float dRtnShippingCharges = Float.valueOf(rtnShippingCharges);
		float dRetQty = Float.parseFloat(strReturnedQty);
		float dSOQty = Float.parseFloat(strOrigOrderLineQty);
		float prorateVal = ((dRtnShippingCharges * dRetQty) / dSOQty);
		String strProrateVal = String.valueOf(prorateVal);
		if (!reqRoundedVal) {
			strProrateVal = truncateDecimal(strProrateVal,
					AcademyConstants.INT_NUMBER_OF_DECIMALS);
			log.verbose("after truncate the prorated value is : "
					+ strProrateVal);
		} else if (new Float(rtnShippingCharges).floatValue() != 0) {
			float prevReturnedCharges = 0;
			log.verbose("ChargeCategory : "
					+ AcademyConstants.STR_RETURNSHIPPING_CHARGE);
			if (parentSalesOrder != null) {
				NodeList lstROLineCharges = XPathUtil.getNodeList(
						parentSalesOrder,
						"ReturnOrders/ReturnOrder/OrderLines/OrderLine[@DerivedFromOrderLineKey='"
								+ parentOLK
								+ "']/LineCharges/LineCharge[@ChargeCategory='"
								+ AcademyConstants.STR_RETURNSHIPPING_CHARGE
								+ "']");
				log.verbose(" total line charges of Past ROs : "
						+ lstROLineCharges.getLength());
				for (int ltIndx = 0; ltIndx < lstROLineCharges.getLength(); ltIndx++) {
					Element prevCharge = (Element) lstROLineCharges
							.item(ltIndx);
					prevReturnedCharges += Float.parseFloat(prevCharge
							.getAttribute(AcademyConstants.ATTR_CHARGE_AMT));
				}
			}
			// Current RO Line not have any Return Shipping Charges at this
			// point. Therefore, take Prorate value for further calculations.
			log.verbose(Float.parseFloat(rtnShippingCharges) + " - "
					+ prevReturnedCharges);
			float remainCharges = Float.parseFloat(rtnShippingCharges)
					- prevReturnedCharges;
			log.verbose(" Remain Charge is :: " + remainCharges);
			if (remainCharges > 0) {
				float delta = remainCharges - prorateVal;
				if (YFCDoubleUtils.roundOff(delta) > 0.09) {
					strProrateVal = truncateDecimal(String
							.valueOf(strProrateVal),
							AcademyConstants.INT_NUMBER_OF_DECIMALS);
				} else {
					double d = Double
							.parseDouble(String.valueOf(remainCharges));
					log
							.verbose("Before Round off the last qty to be return is :: "
									+ d);
					strProrateVal = String.valueOf(YFCDoubleUtils.roundOff(d,
							AcademyConstants.INT_NUMBER_OF_DECIMALS));
				}
			} else {
				if (reqRoundedVal) {
					double d = Double
							.parseDouble(String.valueOf(strProrateVal));
					log
							.verbose("Before Round off the last qty to be return is :: "
									+ d);
					strProrateVal = String.valueOf(YFCDoubleUtils.roundOff(d,
							AcademyConstants.INT_NUMBER_OF_DECIMALS));
				} else
					strProrateVal = truncateDecimal(strProrateVal,
							AcademyConstants.INT_NUMBER_OF_DECIMALS);
			}

		}
		log.verbose("Final prorated value is :: " + strProrateVal);
		return strProrateVal;
	}

	// Added below method as part of Penny issue - truncate after specified
	// decimals
	private String truncateDecimal(String value, int noOfDecimal) {
		String newVal = null;
		int decimalPos = value.indexOf(".");
		newVal = value.substring(0, decimalPos);
		String tempStr = value.substring(decimalPos + 1);
		if (tempStr.length() > noOfDecimal) {
			newVal = newVal + "." + tempStr.substring(0, noOfDecimal);
		} else
			newVal = newVal + "." + tempStr;
		return newVal;
	}

	/**
	 * Method wraps Exception object to YFSUserExitException object
	 * 
	 * @param e
	 * @return YFSUserExitException
	 */
	private YFSUserExitException getYFSUserExceptionWithTrace(Exception e) {
		YFSUserExitException yfsUEException = new YFSUserExitException();
		yfsUEException.setStackTrace(e.getStackTrace());
		return yfsUEException;
	}

	private Document getPRONumber(YFSEnvironment env) {
		Document outDoc = null;
		Document inputToPROSvc = null;
		try {
			inputToPROSvc = XMLUtil.createDocument("Order");
			outDoc = AcademyUtil.invokeService(env, "AcademyGeneratePRONumber",
					inputToPROSvc);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return outDoc;
	}

	// Fix for 4964
	public Document calculateReturnShippingTaxesForWhiteGlove(
			YFSEnvironment env, Document returnOrderDoc, Document salesOrderDoc) {
		Element returnOrderLine, eleSalesOrderLine, salesLineCharges, eleRefundableReasonCode = null;
		Document docRefundableReasonCodes = null;
		String salesOrderQty, returnOrderLineKey, returnOrderQty, returnShippingCharge = null;
		String strReturnReasonCode = "";

		if (salesOrderDoc != null) {

		}

		try {

			NodeList returnOrderLineList = XPathUtil.getNodeList(returnOrderDoc
					.getDocumentElement(), AcademyConstants.XPATH_ORDERLINE);
			int returnOrdLineLength = returnOrderLineList.getLength();
			log.verbose("Number of Order Lines are :" + returnOrdLineLength);
			for (int i = 0; i < returnOrdLineLength; i++) {
				log.verbose("inside the For Loop");
				returnOrderLine = (Element) returnOrderLineList.item(i);

				/* Fix for bug # 4964 */
				strReturnReasonCode = returnOrderLine
						.getAttribute("ReturnReason");

				docRefundableReasonCodes = AcademyReturnOrderUtil
						.getRefundableReasonCodeValuesList(env);

				eleRefundableReasonCode = (Element) XPathUtil.getNode(
						docRefundableReasonCodes.getDocumentElement(),
						"CommonCode[@CodeValue='" + strReturnReasonCode + "']");

				returnOrderLineKey = returnOrderLine
						.getAttribute(AcademyConstants.ATTR_DERIVEDFROM_ORDERLINE_KEY);
				returnOrderQty = returnOrderLine
						.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
				// CR - Vertex Changes; Apply sorting on OrderLine/@PrimeLineNo.
				// Therefore, set the PrimeLineNo
				int lineNo = i + 1;
				returnOrderLine.setAttribute(
						AcademyConstants.ATTR_PRIME_LINE_NO, String
								.valueOf(lineNo));

				eleSalesOrderLine = (Element) XPathUtil.getNode(salesOrderDoc
						.getDocumentElement(),
						"OrderLines/OrderLine[@OrderLineKey='"
								+ returnOrderLineKey + "']");
				salesOrderQty = eleSalesOrderLine
						.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);

				/**
				 * If any one quantity is returne per order line, the entire
				 * shipping charges will be returned, if it's white glove items.
				 */
				if ((returnOrderQty != null)
						&& !returnOrderQty.equalsIgnoreCase("0.00")) {
					log.verbose("first Check");

					salesLineCharges = (Element) XPathUtil.getNode(
							eleSalesOrderLine,
							"LineTaxes/LineTax[@ChargeCategory='" + "Shipping"
									+ "']");
					log
							.verbose("AcademyBeforeCreateReturnOrderUEImpl - salesLineCharges:"
									+ XMLUtil
											.getElementXMLString(salesLineCharges));
					if (!YFCObject.isVoid(salesLineCharges)) {
						String prorateVal = "";
						Element lineChargesEle = null;
						Element lineChargeEle = null;
						Element lineshippingChargeEle = null;
						returnShippingCharge = salesLineCharges
								.getAttribute("Tax");
						lineChargesEle = returnOrderDoc
								.createElement("LineTaxes");
						if (YFCObject.isVoid(eleRefundableReasonCode)) {

							// CR - Return Shipping Charges Computation

							// madhura

							prorateVal = getProratedReturnShippingTax(
									returnShippingCharge, returnOrderQty,
									salesOrderQty, eleSalesOrderLine,
									salesOrderDoc.getDocumentElement());

							lineChargeEle = returnOrderDoc
									.createElement("LineTax");
							// lineshippingChargeEle = returnOrderDoc
							// .createElement("LineCharge");
							lineChargeEle.setAttribute(
									AcademyConstants.ATTR_CHARGE_CATEGORY,
									AcademyConstants.STR_RETURNSHIPPING_CHARGE);
							lineChargeEle.setAttribute(
									AcademyConstants.ATTR_CHARGE_NAME,
									AcademyConstants.STR_RETURNSHIPPING_CHARGE);
							lineChargeEle.setAttribute(
									AcademyConstants.ATTR_TAX_NAME,
									AcademyConstants.STR_RETURNSHIP_TAX);
							// Fix for Bug# 4964
							lineChargeEle.setAttribute(
									AcademyConstants.ATTR_TAX, prorateVal);
							lineChargesEle.appendChild((Node) lineChargeEle);
							// returnOrderLine.appendChild(lineChargesEle);

							// CR - Pro rate Outbound shipping charges
						}
						returnOrderLine.appendChild((Node) lineChargesEle);

					} else {
						log.verbose("Sales Order Charge is Void");
					}
					/*					
					 */
				}
			}
			log
					.verbose("AcademyBeforeCreateReturnOrderUEImpl - calculateReturnShippingChargesForWhiteGlove :"
							+ XMLUtil.getXMLString(returnOrderDoc));
			log
					.endTimer("End of AcademyBeforeCreateReturnOrderUEImpl-> calculateReturnShippingChargesForWhiteGlove ");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		log
				.verbose("AcademyBeforeCreateReturnOrderUEImpl : calculateReturnShippingChargesForWhiteGlove - Exiting -");
		log.verbose("Shipping Calculation output -"
				+ XMLUtil.getXMLString(returnOrderDoc));

		return returnOrderDoc;
	}

	// Fix for 4964
	private String getProratedReturnShippingTax(String rtnShippingCharges,
			String strReturnedQty, String strOrigOrderLineQty,
			Element origSalesOrderLine, Element parentSalesOrder)
			throws Exception {
		float qtyBeingRtn = new Float(strReturnedQty).floatValue();
		String parentOLK = origSalesOrderLine
				.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);

		NodeList lstReturnQtyStatus = XPathUtil
				.getNodeList(origSalesOrderLine,
						"OrderStatuses/OrderStatus[@Status='3700.02' or @Status='3700.01']");
		log.verbose("Already had return order/s : "
				+ lstReturnQtyStatus.getLength());
		float prevProcessedRtnQty = 0;
		for (int indx = 0; indx < lstReturnQtyStatus.getLength(); indx++) {
			Element eleReturnQtyStatus = (Element) lstReturnQtyStatus
					.item(indx);
			prevProcessedRtnQty += Float.parseFloat(eleReturnQtyStatus
					.getAttribute("StatusQty"));
		}
		log.verbose("Already returned Qty is : " + prevProcessedRtnQty);
		float totalRtnQty = prevProcessedRtnQty + qtyBeingRtn;
		log.verbose("Total return qty including the processing qty is : "
				+ totalRtnQty);
		boolean reqRoundedVal = false;
		if (Float.parseFloat(strOrigOrderLineQty) == totalRtnQty) {
			reqRoundedVal = true;
		}
		log.verbose("Required rounded value : " + reqRoundedVal);
		Float dRtnShippingCharges = Float.valueOf(rtnShippingCharges);
		float dRetQty = Float.parseFloat(strReturnedQty);
		float dSOQty = Float.parseFloat(strOrigOrderLineQty);
		float prorateVal = ((dRtnShippingCharges * dRetQty) / dSOQty);
		String strProrateVal = String.valueOf(prorateVal);
		if (!reqRoundedVal) {
			strProrateVal = truncateDecimal(strProrateVal,
					AcademyConstants.INT_NUMBER_OF_DECIMALS);
			log.verbose("after truncate the prorated value is : "
					+ strProrateVal);
		} else if (new Float(rtnShippingCharges).floatValue() != 0) {
			float prevReturnedCharges = 0;
			log.verbose("ChargeCategory : "
					+ AcademyConstants.STR_RETURNSHIPPING_CHARGE);
			if (parentSalesOrder != null) {
				NodeList lstROLineCharges = XPathUtil.getNodeList(
						parentSalesOrder,
						"ReturnOrders/ReturnOrder/OrderLines/OrderLine[@DerivedFromOrderLineKey='"
								+ parentOLK
								+ "']/LineTaxes/LineTax[@ChargeCategory='"
								+ AcademyConstants.STR_RETURNSHIPPING_CHARGE
								+ "']");
				log.verbose(" total line charges of Past ROs : "
						+ lstROLineCharges.getLength());
				for (int ltIndx = 0; ltIndx < lstROLineCharges.getLength(); ltIndx++) {
					Element prevCharge = (Element) lstROLineCharges
							.item(ltIndx);
					prevReturnedCharges += Float.parseFloat(prevCharge
							.getAttribute(AcademyConstants.ATTR_CHARGE_AMT));
				}
			}
			// Current RO Line not have any Return Shipping Charges at this
			// point. Therefore, take Prorate value for further calculations.
			log.verbose(Float.parseFloat(rtnShippingCharges) + " - "
					+ prevReturnedCharges);
			float remainCharges = Float.parseFloat(rtnShippingCharges)
					- prevReturnedCharges;
			log.verbose(" Remain Charge is :: " + remainCharges);
			if (remainCharges > 0) {
				float delta = remainCharges - prorateVal;
				if (YFCDoubleUtils.roundOff(delta) > 0.09) {
					strProrateVal = truncateDecimal(String
							.valueOf(strProrateVal),
							AcademyConstants.INT_NUMBER_OF_DECIMALS);
				} else {
					double d = Double
							.parseDouble(String.valueOf(remainCharges));
					log
							.verbose("Before Round off the last qty to be return is :: "
									+ d);
					strProrateVal = String.valueOf(YFCDoubleUtils.roundOff(d,
							AcademyConstants.INT_NUMBER_OF_DECIMALS));
				}
			} else {
				if (reqRoundedVal) {
					double d = Double
							.parseDouble(String.valueOf(strProrateVal));
					log
							.verbose("Before Round off the last qty to be return is :: "
									+ d);
					strProrateVal = String.valueOf(YFCDoubleUtils.roundOff(d,
							AcademyConstants.INT_NUMBER_OF_DECIMALS));
				} else
					strProrateVal = truncateDecimal(strProrateVal,
							AcademyConstants.INT_NUMBER_OF_DECIMALS);
			}

		}
		log.verbose("Final prorated value is :: " + strProrateVal);
		return strProrateVal;
	}
}
