package com.academy.ecommerce.sterling.bopis.api;

/**#########################################################################################
*
* Project Name                : CurbsideConsolidation Fulfillment_SEP_30_2022_Rel10
* Date                        : 27-Mar-2020 
* Description				  : This class invokes recordCustomerPick (to confirm the PICK shipments), 
* changeShipment (to update ACADEMY_CUSTOMER_PICKEDUP_DATE), 
* AcademyStampInvoiceNoOnBOPISOrders (to stamp invoice)
* for curbside consolidated PICK shipments instead of the mashup - extn_customerpickup_recordCustomerPickup_Ref.
* 								 
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       		Remarks/Description                      
* ---------------------------------------------------------------------------------
* 09-SEP-2022	  Everest Technologies  	 1.0           	Updated version
*
* #########################################################################################*/
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/** Sample input:
 * <Shipment AddVerificationMethod="Y" DisplayLocalizedFieldInLocale="xml:CurrentUser:/User/@Localecode"
	SellerOrganizationCode="" ShipNode="" ShipmentKey="123" ShipmentNo="ABC" TransactionId="CONFIRM_SHIPMENT" strCurbShipment="ABC-123,DEF-456">
	<Extn ExtnShipmentPickedBy="Primary"/>
	<Notes>
		<Note ContactReference="" ContactType=""
			ContactUser="" NoteText="Harika came to pick up products and was verified by License Verification." Priority="0"
			ReasonCode="YCD_CUSTOMER_VERIFICATION" VisibleToAll="Y"/>
	</Notes>
</Shipment>
 *
 * Sample Output:
 * <Shipment ShipmentKey="123"/> 
 */
public class AcademyRecordCustPickForCurbsideConsolidation implements YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory.instance(
			AcademyRecordCustPickForCurbsideConsolidation.class);

	public Document processRecordCustomerPick(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer("ProcessRecordCustomerPick - Start");
		log.verbose("Input for ProcessRecordCustomerPick() :\n " + XMLUtil.getXMLString(inDoc));
		Document docOutput = null;
		Element eleInput = null;
		String strCurrentShipKey = null;
		String strCurbShipment = null;
		String strCurbShipmentKey = null;	
		String strPickedBy = null;
		String[] strArryCurbShipNo = null;		
		String strConsolidatedShipment = null;
		String strValidShipment = null;//OMNI-102403

		try {
			eleInput = inDoc.getDocumentElement();
			strCurrentShipKey = eleInput.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			strCurbShipment = eleInput.getAttribute(AcademyConstants.ATTR_CURBSIDE_SHIPMENT);
			strConsolidatedShipment = eleInput.getAttribute("ConsolidatedShipment");
			//OMNI-102403 - Start
			if(!YFCCommon.isVoid(strCurbShipment)) {
				strValidShipment = strCurbShipment;
			}else if(!YFCCommon.isVoid(strConsolidatedShipment)) {
				strValidShipment = strConsolidatedShipment;
			}
			//OMNI-102403 - End
			strPickedBy = XPathUtil.getString(eleInput, AcademyConstants.XPATH_EXTN_PICKEDUP_BY);

			if (!YFCCommon.isVoid(strValidShipment) && strValidShipment.contains(AcademyConstants.STR_COMMA)) {
				strArryCurbShipNo = strValidShipment.split(AcademyConstants.STR_COMMA);
				for (int iShipNo = 0; iShipNo < strArryCurbShipNo.length; iShipNo++) {
					strValidShipment = strArryCurbShipNo[iShipNo];
					log.verbose("Multi Shipment :: ShipmentKey :: \n" + strValidShipment);
					log.verbose("Input :: " + XMLUtil.getXMLString(inDoc));
					Document docStampInvoiceOutput = invokeApis(env, inDoc, strValidShipment, strPickedBy);
					if (strCurrentShipKey.equalsIgnoreCase(strCurbShipmentKey)) {
						docOutput = docStampInvoiceOutput;
					}					
				}
			} else if (!YFCCommon.isVoid(strValidShipment) && !strValidShipment.contains(AcademyConstants.STR_COMMA)) {
				log.verbose("Single Shipment :: \n");
				docOutput = invokeApis(env, inDoc, strValidShipment, strPickedBy);
			}
			if (YFCCommon.isVoid(docOutput)) {
				docOutput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
				docOutput.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_SHIPMENT_KEY, strCurrentShipKey);
				log.verbose("Returning docOutput:: " + XMLUtil.getXMLString(docOutput));
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.verbose("Exception in processRecordCustomerPick with input:\n" + XMLUtil.getXMLString(inDoc));
			log.info(" Error log :: " + e.toString());
		}
		log.verbose("Final Output :: " + XMLUtil.getXMLString(docOutput));
		return docOutput;
	}
	
	
	/**
	 * This method invokes all the APIs required to update the shipment and picked up 
	 * and related updated on the Order notes and updating the InvoiceNo.
	 *
	 * @param env
	 * @param inDoc
	 * @param strCurbShipment
	 * @param strPickedBy
	 * @return 
	 * @return docStampInvoiceOutput
	 */
	private Document invokeApis(YFSEnvironment env, Document inDoc, String strCurbShipment, String strPickedBy) {
		Document docStampInvoiceOutput = null;
		try {
			String strCurbShipmentKey = strCurbShipment.substring(
					strCurbShipment.indexOf('-')+1, strCurbShipment.length());
			log.verbose("CurbShipment :: \n" + strCurbShipment);
			log.verbose("ShipmentKey :: \n" + strCurbShipmentKey);
			log.verbose("inDoc :: " + XMLUtil.getXMLString(inDoc));
			Document docRecordCustPickOutput = recordCustomerPick(env, inDoc, strCurbShipmentKey);
			Document docChangeShipmentOutput = changeShipment(env, docRecordCustPickOutput, strPickedBy);
			docStampInvoiceOutput = stampInvoiceNoOnBOPISOrders(env, docChangeShipmentOutput);
			log.verbose("Returning :: " + XMLUtil.getXMLString(docStampInvoiceOutput));
			
		} catch (Exception exp) {
			exp.printStackTrace();
			log.info(" Error info for Shipment " + strCurbShipment + " and Error is :: " + exp.toString());
		}		
		return docStampInvoiceOutput;
	}


	/** Prepare input to AcademyStampInvoiceNoOnBOPISOrders based on changeShipment output
	 * <Shipment ShipmentKey="202206251236345588476754"/> 
	 * and invoke AcademyStampInvoiceNoOnBOPISOrders.
	 * @param env
	 * @param docChangeShipmentOutput
	 * @return docStampInvoiceOutput
	 */
	private Document stampInvoiceNoOnBOPISOrders(YFSEnvironment env, Document docChangeShipmentOutput) {
		log.beginTimer("stampInvoiceNoOnBOPISOrders:: AcademyStampInvoiceNoOnBOPISOrders - Start");
		Document docStampInvoiceOutput = null;
		Document docStampInvoiceInput = null;
		try {
			Element eleChangeShipmentOutput = docChangeShipmentOutput.getDocumentElement();
			String shipmentKey = eleChangeShipmentOutput.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			String status = eleChangeShipmentOutput.getAttribute(AcademyConstants.ATTR_STATUS);
			if (AcademyConstants.VAL_SHIPPED_STATUS.equalsIgnoreCase(status) ||
					AcademyConstants.VAL_SHIPMENT_INVOICED_STATUS.equalsIgnoreCase(status)) {
				docStampInvoiceInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
				docStampInvoiceInput.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_SHIPMENT_KEY, shipmentKey);
			}
			log.verbose("Input to AcademyStampInvoiceNoOnBOPISOrders :: " 
					+ XMLUtil.getXMLString(docStampInvoiceInput));
			docStampInvoiceOutput = AcademyUtil.invokeService(env, 
					AcademyConstants.SER_STAMP_INVOICE_NO, docStampInvoiceInput);
		} catch (Exception exp) {
			exp.printStackTrace();
			log.info(" Exception in AcademyStampInvoiceNoOnBOPISOrders + " + 
					docChangeShipmentOutput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY) +
					":: " + exp.toString());
		}
		log.verbose("Output from AcademyStampInvoiceNoOnBOPISOrders :: " 
				+ XMLUtil.getXMLString(docStampInvoiceOutput));
		log.endTimer("stampInvoiceNoOnBOPISOrders:: AcademyStampInvoiceNoOnBOPISOrders - End");
		return docStampInvoiceOutput;
	}

	/**Prepare input to changeShipment based on output of recordCustomerPick
	 * and invoke changeShipment to update ACADEMY_CUSTOMER_PICKEDUP_DATE as currentDate 
	 * and ExtnShipmentPickedBy as Primary/ Alternate
	 * @param env
	 * @param docRecordCustPickOutput
	 * @param strPickedBy
	 * @return docChangeShipmentOutput
	 */
	private Document changeShipment(YFSEnvironment env, Document docRecordCustPickOutput, String strPickedBy) {
		log.beginTimer("changeShipment to update PickedupDate - Start");
		Document docChangeShipmentOutput = null;
		try {
			Element eleRecordCustPickOutput = docRecordCustPickOutput.getDocumentElement();
			String strStatus = docRecordCustPickOutput.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_STATUS);
			if (AcademyConstants.VAL_SHIPPED_STATUS.equalsIgnoreCase(strStatus)) {
				Element eleExtn = SCXmlUtil.createChild(eleRecordCustPickOutput, AcademyConstants.ELE_EXTN);
				if (strPickedBy.equalsIgnoreCase(AcademyConstants.ATTR_PICKEDBY_VAL)) {
					eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_SHIP_PICKEDBY, AcademyConstants.ATTR_VAL_ALTERNATE);
				} else {
					eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_SHIP_PICKEDBY, AcademyConstants.ATTR_VAL_PRIMARY);
				}
				Element eleAdditionalDates = SCXmlUtil.createChild(eleRecordCustPickOutput, 
						AcademyConstants.E_ADDITIONAL_DATES);
				Element eleAdditionalDate = SCXmlUtil.createChild(eleAdditionalDates,
						AcademyConstants.E_ADDITIONAL_DATE);
				eleAdditionalDate.setAttribute(AcademyConstants.A_DATE_TYPE_ID,
						AcademyConstants.DATE_TYPE_PICKUP_DATE_ACKSLIP);
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern(
						AcademyConstants.STR_SIMPLE_DATE_PATTERN);
				LocalDateTime ldtSystemTime = LocalDateTime.now();
				eleAdditionalDate.setAttribute(AcademyConstants.ATTR_ACTUAL_DATE, 
						dtf.format(ldtSystemTime));
				
				Element eleShipmentLines = SCXmlUtil.getChildElement(
						eleRecordCustPickOutput, AcademyConstants.ELE_SHIPMENT_LINES);
				if (!YFCObject.isVoid(eleShipmentLines)) {
					eleRecordCustPickOutput.removeChild(eleShipmentLines);
				}			
			}
			log.verbose("Input to changeShipment :: " + XMLUtil.getXMLString(docRecordCustPickOutput));
			docChangeShipmentOutput = AcademyUtil.invokeAPI(env, 
					AcademyConstants.API_CHANGE_SHIPMENT, docRecordCustPickOutput);
		} catch (Exception c) {
			c.printStackTrace();
			log.info(" Exception occurred while invoking changeShipment :: " +
					docRecordCustPickOutput.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_SHIPMENT_KEY) + " :: Error is : " +c.toString());
		}
		log.verbose("Output from changeShipment :: " + XMLUtil.getXMLString(docChangeShipmentOutput));
		log.endTimer("changeShipment to update PickedupDate - End");
		return docChangeShipmentOutput;
	}

	/**Prepare input to recordCustomerPick.
	 * @param env
	 * @param inDoc
	 * @param strShipmentKey
	 * @return docRecordCustPickOutput
	 */
	private Document recordCustomerPick(YFSEnvironment env, Document inDoc, String strCurbShipmentKey) {
		log.beginTimer("recordCustomerPick to confirmShipments - Start");
		Document docRecordCustPickOutput = null;
		Document docRecordCustPicIn = null;
		Document docGetShipmentLineListOuput = null;
		Document docGetShipmentLineListIn = null;

		try {			
			/*
			 * invoke getShipmentLineList and loop through shipment lines from output. Do as
			 * OOB RecordCustomerPickMashup class <ShipmentLine ShipmentKey=""/>
			 */
			docGetShipmentLineListIn = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT_LINE);
			docGetShipmentLineListIn.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
					strCurbShipmentKey);
			
			Document docgetShipmentLineListTemplate = XMLUtil.getDocument(
					AcademyConstants.ACAD_GET_SHIPMENT_LINE_LIST);
			log.verbose("getShipmentLineList API indoc XML : \n" 
					+ XMLUtil.getXMLString(docGetShipmentLineListIn));
			env.setApiTemplate(AcademyConstants.SER_GET_SHIPMENT_LINE_LIST, 
					docgetShipmentLineListTemplate);
			docGetShipmentLineListOuput = AcademyUtil.invokeAPI(env, 
					AcademyConstants.SER_GET_SHIPMENT_LINE_LIST, docGetShipmentLineListIn);
			env.clearApiTemplate(AcademyConstants.SER_GET_SHIPMENT_LINE_LIST);
			log.verbose("getShipmentLineList API output XML : \n" 
					+ XMLUtil.getXMLString(docGetShipmentLineListOuput));
			
			docRecordCustPicIn = getRecordCustomerPickShipmentLines(docGetShipmentLineListOuput, inDoc, strCurbShipmentKey);
			log.verbose("Input to recordCustomerPick :: " + XMLUtil.getXMLString(docRecordCustPicIn));
			docRecordCustPickOutput = AcademyUtil.invokeAPI(env, 
					AcademyConstants.API_RECORD_CUSTOMER_PICK, docRecordCustPicIn);
			
		} catch (Exception r) {
			r.printStackTrace();
			log.info(" Exception occurred while invoking recordCustomerPick :: with input :: " +
					XMLUtil.getXMLString(docRecordCustPickOutput) + "Error is :: "+r.toString());
		}
		log.verbose("Output from recordCustomerPick :: " + XMLUtil.getXMLString(docRecordCustPickOutput));
		log.endTimer("recordCustomerPick to confirmShipments - End");
		return docRecordCustPickOutput;
	}

	/**Prepare input to recordCustomerPick based on the getShipmentLineList output.
	 * @param docGetShipmentLineListOuput
	 * @param inDoc
	 * @return inDoc
	 */
	private Document getRecordCustomerPickShipmentLines(Document docGetShipmentLineListOuput, Document inDoc, String strCurbShipmentKey) {
		try {
			Element eleShipment =inDoc.getDocumentElement();
			eleShipment.setAttribute(AcademyConstants.ATTR_TRANS_ID, 
					AcademyConstants.TRAN_CONFIRM_SHIPMENT);
			eleShipment.setAttribute(AcademyConstants.ATTR_VERIFICATION_METHOD, 
					AcademyConstants.STR_YES);
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, 
					strCurbShipmentKey);
			Element elePreviousShipLines = SCXmlUtil.getChildElement(eleShipment, AcademyConstants.ELE_SHIPMENT_LINES);
			if (!YFCObject.isVoid(elePreviousShipLines)) {
				eleShipment.removeChild(elePreviousShipLines);
			}
			Element eleShipmentLines = SCXmlUtil.createChild(inDoc.getDocumentElement(), 
					AcademyConstants.ELE_SHIPMENT_LINES);
			NodeList nlShipmentLine = docGetShipmentLineListOuput.getDocumentElement()
					.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			if (!YFCCommon.isVoid(nlShipmentLine) && nlShipmentLine.getLength() > 0) {
				for (int i = 0; i < nlShipmentLine.getLength(); i++) {
					Element eleShipmentLine = SCXmlUtil.createChild(eleShipmentLines, 
							AcademyConstants.ELE_SHIPMENT_LINE);
					Element eleShipLine = (Element) nlShipmentLine.item(i);
					Double quantity = Double.valueOf(Double.parseDouble(
							eleShipLine.getAttribute(AcademyConstants.ATTR_QUANTITY)));
					Double pickedQty = Double.valueOf(0.0D);
					if (!YFCCommon.isVoid(eleShipLine.getAttribute(AcademyConstants.ATTR_CUST_PCKD_QTY)))
						pickedQty = Double.valueOf(Double.parseDouble(
								eleShipLine.getAttribute(AcademyConstants.ATTR_CUST_PCKD_QTY)));
					if (eleShipLine.getAttribute(AcademyConstants.ATTR_IS_PICKABLE_FLAG).equals(AcademyConstants.ATTR_N))
						pickedQty = quantity;
					eleShipmentLine.setAttribute(AcademyConstants.ATTR_PICKED_QUANTITY, 
							String.valueOf(pickedQty));
					if (pickedQty.doubleValue() < quantity.doubleValue()) {
						eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHORTAGE_REASON,
								eleShipLine.getAttribute(AcademyConstants.ATTR_SHRTG_RESOL_REASON));
						eleShipmentLine.setAttribute(AcademyConstants.ATTR_REASON_CODE, 
								eleShipLine.getAttribute(AcademyConstants.ATTR_CANCEL_REASON));
					}
					eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, 
							eleShipLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
					eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, 
							eleShipLine.getAttribute(AcademyConstants.ATTR_QUANTITY));
				}
			}
		} catch (Exception g) {
			g.printStackTrace();
			log.info("Exception in getRecordCustomerPickShipmentLines with input:\n"
					+ XMLUtil.getXMLString(docGetShipmentLineListOuput));
			log.info(" Error Info :: " + g.toString());
		}		
		return inDoc;
	}
	
	/**
	 * This method is invoked on recordShortage of one/more lines and complete the order from Customer Pickup
	 * to invoke AcademyStoreRecordShortage Service during consolidated curbside shipments
	 * 
	 * @param env,
	 * @param inDoc
	 * Sample Input:
	 * <Shipment CurbsideConsToggle="Y" CurbsideShipmentKey="202209160534305681787186,202209160534385681787211" 
	 * DeliveryMethod="PICK" IgnoreOrdering="Y" ShipNode="033" ShipmentKey="202209160534305681787186" 
	 * ShipmentNo="100554496" Status="1100.70.06.30.5">
  		<ShipmentLines>
    	<ShipmentLine ActualQuantity="1.00" CustomerPickedQuantity="1.00" ItemID="109050554" 
    	OrderNo="20220916Test03" Quantity="1.00" ShipmentLineKey="202209160534305681787185" 
    	ShipmentKey="202209160534305681787186" ShortageQty="0">
      	<Extn ExtnMsgToSIM="N" />
    	</ShipmentLine>
    	</ShipmentLines>
    	</Shipment>
	 */
	
	public Document recordShortageForCurbsideConsolidation(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("AcademyRecordCustPickForCurbsideConsolidation:: recordShortageForCurbsideConsolidation Starts");
		log.verbose("recordShortageForCurbsideConsolidation() input :\n " + XMLUtil.getXMLString(inDoc));
		Element eleInput = null;
		eleInput = inDoc.getDocumentElement();
		String strCurbShipmentKey = eleInput.getAttribute(AcademyConstants.ATTR_CURB_SHP_KEY);
		String strConsolidatedShipmentKey = eleInput.getAttribute("ConsolidatedShipmentKey");
		String strValidShipmentKey = null;//OMNI-102218
		//OMNI-102218 - Start
		if(!YFCCommon.isVoid(strCurbShipmentKey)) {
			strValidShipmentKey = strCurbShipmentKey;
		}else if(!YFCCommon.isVoid(strConsolidatedShipmentKey)) {
			strValidShipmentKey = strConsolidatedShipmentKey;
		}
		//OMNI-102218 - End
		String[] strArryCurbShipNo = null;
		Document docRecordCustPickOutput = null;

		if (null!=strValidShipmentKey && strValidShipmentKey.contains(AcademyConstants.STR_COMMA)) {
			strArryCurbShipNo = strValidShipmentKey.split(AcademyConstants.STR_COMMA);
			for (int iShipNo = 0; iShipNo < strArryCurbShipNo.length; iShipNo++) {
				strValidShipmentKey = strArryCurbShipNo[iShipNo];
				log.verbose("Multi Shipment :: ShipmentKey :: " + strValidShipmentKey);
				log.verbose("inDoc :: " + XMLUtil.getXMLString(inDoc));
				docRecordCustPickOutput = callStoreRecordShortage(env, inDoc, strValidShipmentKey);
			}
		} else if (null!=strValidShipmentKey && !strValidShipmentKey.contains(AcademyConstants.STR_COMMA)) {
			log.verbose("Single Shipment ::" +strValidShipmentKey);
			docRecordCustPickOutput = callStoreRecordShortage(env, inDoc, strValidShipmentKey);
		}
		
		log.verbose("AcademyRecordCustPickForCurbsideConsolidation:: recordShortageForCurbsideConsolidation Ends");
		return docRecordCustPickOutput;
	}
	
	/**
	 * This method is frames input for each shipment and invokes AcademyStoreRecordShortage Service
	 * 
	 * @param env,
	 * @param inDoc
	 * Sample Input to service:
	 * <Shipment DeliveryMethod="PICK" ShipNode="033"
    	ShipmentKey="202209160534305681787186" Status="1100.70.06.30.5">
    	<ShipmentLines>
        	<ShipmentLine ActualQuantity="1.00"
            CustomerPickedQuantity="1.00" ItemID="109050554"
            OrderNo="20220916Test03" Quantity="1.00"
            ShipmentKey="202209160534305681787186"
            ShipmentLineKey="202209160534305681787185" ShortageQty="0">
            <Extn ExtnMsgToSIM="N"/>
        	</ShipmentLine>
		</ShipmentLines>
		</Shipment>
	 */

	private Document callStoreRecordShortage(YFSEnvironment env, Document inDoc, String strCurbShipmentKey)
			throws Exception {
		log.beginTimer("AcademyRecordCustPickForCurbsideConsolidation:: callStoreRecordShortage - Start");
		Element eleInput = null;
		Element eleShipment = null;
		boolean bInvokeRecordShortage = false;
		eleInput = inDoc.getDocumentElement();

		String strDeliveryMethod = eleInput.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
		String strShipNode = eleInput.getAttribute(AcademyConstants.STR_SHIPNODE);
		String strStatus = eleInput.getAttribute(AcademyConstants.ATTR_STATUS);
		Document docRecordShortageInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleShipment = docRecordShortageInput.getDocumentElement();
		eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strCurbShipmentKey);
		eleShipment.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, strDeliveryMethod);
		eleShipment.setAttribute(AcademyConstants.STR_SHIPNODE, strShipNode);
		eleShipment.setAttribute(AcademyConstants.ATTR_STATUS, strStatus);
		Element eleShipmentLines = SCXmlUtil.createChild(docRecordShortageInput.getDocumentElement(),
				AcademyConstants.ELE_SHIPMENT_LINES);

		NodeList nlShipmentLine = eleInput.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		log.verbose("nlShipmentLine:: Length:: " + nlShipmentLine.getLength());
		if (!YFCCommon.isVoid(nlShipmentLine) && nlShipmentLine.getLength() > 0) {
			for (int i = 0; i < nlShipmentLine.getLength(); i++) {
				Element eleShpLine = (Element) nlShipmentLine.item(i);
				String strCurrentShpKey = eleShpLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
				String strShortageQty = eleShpLine.getAttribute(AcademyConstants.ATTR_SHORTAGE_QTY);
				Double dShortageQty = Double.parseDouble(strShortageQty);
				 if (strCurrentShpKey.equals(strCurbShipmentKey) && dShortageQty > 0.0) {
					SCXmlUtil.importElement(eleShipmentLines, eleShpLine);
					bInvokeRecordShortage = true;
				}
			}

		}
		if (bInvokeRecordShortage) {
		Document docRecordShortageOutput = AcademyUtil.invokeService(env,
				AcademyConstants.ACADEMY_STORE_RECORD_SHORTAGE_SERVICE, docRecordShortageInput);
		log.verbose("Output from recordShortage :: " + XMLUtil.getXMLString(docRecordShortageOutput));
		}
		log.endTimer("AcademyRecordCustPickForCurbsideConsolidation:: callStoreRecordShortage - End");
		return docRecordShortageInput;
	}
	
	public Document beginFireArmPaperWork(YFSEnvironment env, Document inDoc) throws Exception {
		Document docChangeShipmentStatusIn = null;
		String strShipmentKey = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		log.verbose("beginFireArmPaperWork :: strShipmentKey :: " + strShipmentKey);
		Document docChangeShipmentStatusOut = null;
		Element eleChangeShipStatus = null;
		docChangeShipmentStatusIn = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleChangeShipStatus = docChangeShipmentStatusIn.getDocumentElement();
		eleChangeShipStatus.setAttribute("BaseDropStatus", "1100.70.06.30.7");
		eleChangeShipStatus.setAttribute("TransactionId", "INITIATE_PAPER_WORK.0001.ex");

		if (strShipmentKey.contains(AcademyConstants.STR_COMMA)) {
			String strShipmentKeys[] = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY)
					.split(AcademyConstants.STR_COMMA);
			if (strShipmentKeys.length > 1) {
				int iShipmentKey = 0;
				do {
					log.verbose(" :: strShipmentKey :: " + strShipmentKeys[iShipmentKey]);

					eleChangeShipStatus.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKeys[iShipmentKey]);
					iShipmentKey++;
					docChangeShipmentStatusOut = AcademyUtil.invokeAPI(env, AcademyConstants.CHANGE_SHIPMENT_STATUS_API,
							docChangeShipmentStatusIn);
				} while (iShipmentKey < strShipmentKeys.length);
			}
		} else {
			eleChangeShipStatus.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			docChangeShipmentStatusOut = AcademyUtil.invokeAPI(env, AcademyConstants.CHANGE_SHIPMENT_STATUS_API,
					docChangeShipmentStatusIn);
		}
		return docChangeShipmentStatusOut;
	}

	/**
	 * This method invokes on apply of record shortage when MarkAllShortLineWithShortage is sent as Y from UI via 
	 * Mashup id : extn_changeShipmentForCurbsideConsoldiation
	 * Sample Input XML:
	 *   <Shipment CurbsideShipmentKey="202208111004465667049294,202208111004465667049200"
    	DisplayLocalizedFieldInLocale="en_US_CST" IgnoreOrdering="Y" ShipmentKey="202208111004465667049294">
    	<ShipmentLines>
        	<ShipmentLine CancelReason="Wrong Color Ordered"
            ShipmentKey="202208111004465667049294"
            ShipmentLineKey="202208111004465667049293" ShortageResolutionReason="Wrong Color Ordered"/>
    	</ShipmentLines>
		</Shipment>
	 * 
	 * @param env
	 * @param inDoc
	 * @return docChangeShipmentOut
	 */

	public Document changeShipmentForUpdateQty(YFSEnvironment env, Document inDoc) throws Exception {
		
		log.beginTimer("AcademyRecordCustPickForCurbsideConsolidation :: changeShipmentForUpdateQty()");
		log.verbose("changeShipmentForUpdateQty() input :\n " + XMLUtil.getXMLString(inDoc));
		Element eleInput = null;
		eleInput = inDoc.getDocumentElement();
		String strValidShipmentKey = null;
		String strCurbShipmentKey = eleInput.getAttribute(AcademyConstants.ATTR_CURB_SHP_KEY);
		String strConsolidatedShipmentKey = eleInput.getAttribute(AcademyConstants.ATTR_CONSOLD_SHIPMENT_KEY);//OMNI-102218
		
		strValidShipmentKey = getValidShipmentDetails(strCurbShipmentKey,strConsolidatedShipmentKey);
		
		String strShipmentKey = eleInput.getAttribute(AcademyConstants.SHIPMENT_KEY);
		String[] strArryCurbShipNo = null;
		Document docGetShipmentLineListOutput = null;
		Document docChangeShipmentInput = null;
		Document docChangeShipmentOutput = null;
		Element shipmentLine = SCXmlUtil.getFirstChildElement(SCXmlUtil.getChildElement(eleInput, AcademyConstants.ELE_SHIPMENT_LINES));
		String strShortageResolutionReason = shipmentLine.getAttribute(AcademyConstants.ATTR_SHRTG_RESOL_REASON);
		String strCancelReason = shipmentLine.getAttribute(AcademyConstants.ATTR_CANCEL_REASON);
		
		Document docChangeShipmentOut = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleChangeShipmentOut = docChangeShipmentOut.getDocumentElement();
		eleChangeShipmentOut.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
		eleChangeShipmentOut.setAttribute(AcademyConstants.ATTR_CURB_SHP_KEY, strValidShipmentKey);
		
		Element eleShipmentLines = SCXmlUtil.createChild(docChangeShipmentOut.getDocumentElement(),
				AcademyConstants.ELE_SHIPMENT_LINES);
		ArrayList<Element> shipmentLineList = new ArrayList<Element>();
		
		if (null!=strValidShipmentKey && strValidShipmentKey.contains(AcademyConstants.STR_COMMA)) {
			strArryCurbShipNo = strValidShipmentKey.split(AcademyConstants.STR_COMMA);
			for (int iShipNo = 0; iShipNo < strArryCurbShipNo.length; iShipNo++) {
				strValidShipmentKey = strArryCurbShipNo[iShipNo];
				log.verbose("Multi Shipment :: ShipmentKey :: " + strValidShipmentKey);
				docGetShipmentLineListOutput = callGetShipmentLineList(env, strValidShipmentKey);
				docChangeShipmentInput = prepareChangeShipmentInput(inDoc, docGetShipmentLineListOutput,
						strShortageResolutionReason, strCancelReason, strValidShipmentKey);
				Document docChangeShipmentOutTemplate = XMLUtil
						.getDocument(AcademyConstants.ACAD_CHANGE_SHPMT_FOR_RCD_CUST);
				log.verbose(
						"changeShipmentOutTemplate API indoc XML : \n" + XMLUtil.getXMLString(docChangeShipmentInput));
				env.setApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipmentOutTemplate);
				docChangeShipmentOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT,
						docChangeShipmentInput);
				env.clearApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT);
				log.verbose("changeShipment API output XML : \n" + XMLUtil.getXMLString(docChangeShipmentOutput));
				Element eleChangeShpLinesOut =  docChangeShipmentOutput.getDocumentElement();
				NodeList nlChangeShpLinesOut = eleChangeShpLinesOut.getElementsByTagName("ShipmentLine");
				for (int shpLines = 0; shpLines < nlChangeShpLinesOut.getLength(); shpLines++) {
					Element eleShpLines = (Element) nlChangeShpLinesOut.item(shpLines);
					shipmentLineList.add(eleShpLines);
				}			
			}
			if ((!YFCCommon.isVoid(shipmentLineList)) && (shipmentLineList.size() > 0)) {
				Iterator<Element> shipmentLineItr = shipmentLineList.iterator();
				while (shipmentLineItr.hasNext()) {
					Element eleShipmentLine = (Element) shipmentLineItr.next();
					SCXmlUtil.importElement(eleShipmentLines, eleShipmentLine);
				}
			}
		} else if (null!=strValidShipmentKey && !strValidShipmentKey.contains(AcademyConstants.STR_COMMA)) {
			log.verbose("Single Shipment ::" + strValidShipmentKey);
			docGetShipmentLineListOutput = callGetShipmentLineList(env, strValidShipmentKey);
			docChangeShipmentInput = prepareChangeShipmentInput(inDoc, docGetShipmentLineListOutput,
					strShortageResolutionReason, strCancelReason, strValidShipmentKey);
			Document docChangeShipmentOutTemplate = XMLUtil
					.getDocument(AcademyConstants.ACAD_CHANGE_SHPMT_FOR_RCD_CUST);
			log.verbose("changeShipmentOutTemplate API indoc XML : \n" + XMLUtil.getXMLString(docChangeShipmentInput));
			env.setApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipmentOutTemplate);
			docChangeShipmentOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT,
					docChangeShipmentInput);
			env.clearApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT);
			log.verbose("changeShipment API output XML : \n" + XMLUtil.getXMLString(docChangeShipmentOut));	
		}
		
		log.verbose("changeShipmentForUpdateQty() :: docChangeShipmentOut:: " + XMLUtil.getXMLString(docChangeShipmentOut));
		log.endTimer("AcademyRecordCustPickForCurbsideConsolidation :: changeShipmentForUpdateQty()");
		return docChangeShipmentOut;
	}
	
	//OMNI-102218 - Start
	private String getValidShipmentDetails(String strCurbShipmentKey, String strConsolidatedShipmentKey) {
		String strValidShipmentKey = null;
		if (!YFCCommon.isVoid(strCurbShipmentKey)) {
			strValidShipmentKey = strCurbShipmentKey;
		} else if (!YFCCommon.isVoid(strConsolidatedShipmentKey)) {
			strValidShipmentKey = strConsolidatedShipmentKey;
		}
		return strValidShipmentKey;
	}
	//OMNI-102218 - End



	/**
	 * This method prepares change shipment input based on the shipmentkey  
	 * Sample changeShipment Input XML:
	 * <Shipment
    	CurbsideShipmentKey="202208111004465667049294,202208111004465667049200"
    	DisplayLocalizedFieldInLocale="en_US_CST" IgnoreOrdering="Y" ShipmentKey="202208111004465667049200">
    	<ShipmentLines>
        <ShipmentLine CancelReason="Wrong Color Ordered"
            CustomerPickedQuantity="0.00" Quantity="1.00"
            ShipmentLineKey="202208111004465667049293" ShortageQty="1.0" ShortageResolutionReason="Wrong Color Ordered"/>
    	</ShipmentLines>
	</Shipment>
	 * 
	 * @param inDoc
	 * @param docGetShipmentLineListOuput
	 * @param strShortageResolutionReason
	 * @param strCancelReason
	 * @param strCurbShipmentKey
	 * @return inDoc
	 */
	private Document prepareChangeShipmentInput(Document inDoc, Document docGetShipmentLineListOuput,
			String strShortageResolutionReason, String strCancelReason, String strCurbShipmentKey) {
		log.beginTimer("AcademyRecordCustPickForCurbsideConsolidation :: prepareChangeShipmentInput()");

		Element eleInput = inDoc.getDocumentElement();
		eleInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strCurbShipmentKey);
		Element elePreviousShipLines = SCXmlUtil.getChildElement(eleInput, AcademyConstants.ELE_SHIPMENT_LINES);
		if (!YFCObject.isVoid(elePreviousShipLines)) {
			eleInput.removeChild(elePreviousShipLines);
		}

		Element eleShipmentLines = SCXmlUtil.createChild(eleInput, AcademyConstants.ELE_SHIPMENT_LINES);
		Element eleGetShipmentLineList = docGetShipmentLineListOuput.getDocumentElement();
		ArrayList<Element> shipmentLineList = SCXmlUtil.getChildren(eleGetShipmentLineList, AcademyConstants.ELE_SHIPMENT_LINE);

		if ((!YFCCommon.isVoid(shipmentLineList)) && (shipmentLineList.size() > 0)) {
			Iterator<Element> shipmentLineItr = shipmentLineList.iterator();
			while (shipmentLineItr.hasNext()) {
				Element shipmentLine = (Element) shipmentLineItr.next();

				shipmentLine.setAttribute(AcademyConstants.ATTR_SHRTG_RESOL_REASON, strShortageResolutionReason);
				shipmentLine.setAttribute(AcademyConstants.ATTR_CANCEL_REASON, strCancelReason);

				String strCustQty = shipmentLine.getAttribute(AcademyConstants.ATTR_CUST_PCKD_QTY);
				if (!YFCObject.isVoid(strCustQty)) {
					Double dCustomerPickedQty = Double.parseDouble(strCustQty);
					Double dOriginalQty = Double.parseDouble(shipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY));
					Double dQty = dOriginalQty - dCustomerPickedQty;
					String strQty = String.valueOf(dQty);
					shipmentLine.setAttribute(AcademyConstants.ATTR_SHORTAGE_QTY, strQty);
				} else {
					Double dOriginalQty = Double.parseDouble(shipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY));
					String strQty = String.valueOf(dOriginalQty);
					shipmentLine.setAttribute(AcademyConstants.ATTR_SHORTAGE_QTY, strQty);
				}
				SCXmlUtil.importElement(eleShipmentLines, shipmentLine);
			}
		}
		log.verbose("prepareChangeShipmentInput() input :\n " + XMLUtil.getXMLString(inDoc));
		log.endTimer("AcademyRecordCustPickForCurbsideConsolidation :: prepareChangeShipmentInput()");
		return inDoc;
	}

	/**
	 * This method prepares getShipmentLineList input and invokes the API   
	 * Sample getShipmentLineList API Input XML:
	 * <ShipmentLine IsCustomerPickComplete="Y" IsCustomerPickCompleteQryType="NE" IsPickable="Y" ShipmentKey="">
	<ComplexQuery>
		<And>
			<Exp Name="ShortageResolutionReason" QryType="ISNULL"/>
		</And>
	</ComplexQuery>
	</ShipmentLine>
	 * 
	 * @param env
	 * @param strCurbShipmentKey
	 * @return docChangeShipmentOut
	 */
	private Document callGetShipmentLineList(YFSEnvironment env, String strCurbShipmentKey) throws Exception {
		log.beginTimer("AcademyRecordCustPickForCurbsideConsolidation :: callGetShipmentLineList()");

		Document docGetShipmentLineListOuput = null;
		Document docGetShipmentLineListIn = null;

		docGetShipmentLineListIn = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT_LINE);
		docGetShipmentLineListIn.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
				strCurbShipmentKey);
		docGetShipmentLineListIn.getDocumentElement().setAttribute(AcademyConstants.ATTR_IS_CUST_PICK_COMPLETE,
				AcademyConstants.STR_YES);
		docGetShipmentLineListIn.getDocumentElement().setAttribute(AcademyConstants.ATTR_IS_CUST_PICK_COMPLETE_QRY_TYPE,
				AcademyConstants.STR_NE);
		docGetShipmentLineListIn.getDocumentElement().setAttribute(AcademyConstants.ATTR_IS_PICKABLE_FLAG,
				AcademyConstants.STR_YES);
		Element eleComplexQuery = docGetShipmentLineListIn.createElement(AcademyConstants.COMPLEX_QRY_ELEMENT);
		Element eleAnd = docGetShipmentLineListIn.createElement(AcademyConstants.COMPLEX_AND_ELEMENT);

		Element eleExp = docGetShipmentLineListIn.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_SHRTG_RESOL_REASON);
		eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_NOT_NULL);
		eleAnd.appendChild(eleExp);
		eleComplexQuery.appendChild(eleAnd);
		docGetShipmentLineListIn.getDocumentElement().appendChild(eleComplexQuery);
		Document docgetShipmentLineListTemplate = XMLUtil
				.getDocument(AcademyConstants.ACAD_GET_SHIPMENT_LINE_LIST_FOR_RCD_CUST);
		log.verbose("getShipmentLineList API indoc XML : \n" + XMLUtil.getXMLString(docGetShipmentLineListIn));
		env.setApiTemplate(AcademyConstants.SER_GET_SHIPMENT_LINE_LIST, docgetShipmentLineListTemplate);
		docGetShipmentLineListOuput = AcademyUtil.invokeAPI(env, AcademyConstants.SER_GET_SHIPMENT_LINE_LIST,
				docGetShipmentLineListIn);
		env.clearApiTemplate(AcademyConstants.SER_GET_SHIPMENT_LINE_LIST);
		log.verbose("getShipmentLineList API output XML : \n" + XMLUtil.getXMLString(docGetShipmentLineListOuput));
		
		log.endTimer("AcademyRecordCustPickForCurbsideConsolidation :: callGetShipmentLineList()");
		return docGetShipmentLineListOuput;

	}

	
	@Override
	public void setProperties(Properties arg0) throws Exception {
		log.verbose(" setProperties ");
	}

}