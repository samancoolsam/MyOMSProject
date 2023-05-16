/**
 * 
 */
package com.academy.ecommerce.sterling.shipment;

import java.util.ArrayList;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNode;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.YFSUserExitException;

/**
 * @author dsharma
 * 
 */
public class AcademyYDMBeforeChangeShipmentUEImpl implements YIFCustomApi {

	private static final YFCLogCategory log = YFCLogCategory
			.instance(AcademyYDMBeforeChangeShipmentUEImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yantra.ydm.japi.ue.YDMBeforeChangeShipment#beforeChangeShipment(com
	 *      .yantra.yfs.japi.YFSEnvironment, org.w3c.dom.Document)
	 */
	public Document beforeChangeShipment(YFSEnvironment env, Document inXML)
			throws YFSUserExitException {
		log
				.beginTimer(" Begining of AcademyYDMBeforeChangeShipmentUEImpl-> beforeChangeShipment Api");
		String strShipmentNo = "";
		String strEnterpriseCode = "";
		String strDocumentType = "";
		String strProgId = "";
		Document docInputGetShipmentDetails = null;
		Document docOutputGetShipmentDetails = null;
		Document vertexQuoteCallReq = null;
		try {
			//OMNI-40994 :Start
			strProgId= env.getProgId();
			log.debug("ProgId is: " +strProgId );
			//OMNI-40994 :End
			if (inXML.getDocumentElement().getAttribute("DocumentType").equals(
					"0001")) {

				if (env.getTxnObject("ShipmentQuoteCall") == null) {
					/**
					 * Start - Fix for # 4079 and 4131
					 * 
					 * Don't invoke Vertex when 
					 * 		- Level of Service changed 
					 * 		- Changed a Shipment Line Qty to Zero and no other active shipment line in a Shipment 
					 * Invoke Vertex when 
					 * 		- Change the Shipment Line Qty with valid Qty
					 * 
					 * The input to the Vertex should be 
					 * 		- modified Qty of Shipment Line 
					 * 		- ignore the shipment line with zero qty 
					 * 		- ignore the removed shipment line
					 */
					NodeList list = inXML.getDocumentElement()
							.getElementsByTagName(
									AcademyConstants.ELE_SHIPMENT_LINE);
					log
							.verbose("No of Shipment Line while modifying the Shipment is : "
									+ list.getLength());
					if (list.getLength() <= 0)
						return inXML;

					// convert the input to Vertex Quote Call Request xml.
					inXML.getDocumentElement().setAttribute("CallType",
							"QuoteCall");

					Element eleShipment = inXML.getDocumentElement();
					strShipmentNo = eleShipment.getAttribute("ShipmentNo");
					strEnterpriseCode = eleShipment.getAttribute("EnterpriseCode");
					strDocumentType = eleShipment.getAttribute("DocumentType");
					
					/*
					 * NodeList nlShipLines = eleShipment
					 * .getElementsByTagName("ShipmentLine"); int iNoOfOrdlines =
					 * nlShipLines.getLength(); if (iNoOfOrdlines > 0) {
					 * vertexQuoteCallReq = AcademyUtil.invokeService(env,
					 * "AcademyChangeShipmentToQuoteCallRequest", inXML); } else {
					 */
					/* Start Fix - 9692 */
					docInputGetShipmentDetails = XMLUtil
							.createDocument("Shipment");
					docInputGetShipmentDetails.getDocumentElement()
					.setAttribute("ShipmentNo", strShipmentNo);
					docInputGetShipmentDetails.getDocumentElement()
					.setAttribute("EnterpriseCode", strEnterpriseCode);
					docInputGetShipmentDetails.getDocumentElement()
					.setAttribute("DocumentType", strDocumentType);
					/*docInputGetShipmentDetails.getDocumentElement()
							.setAttribute("ShipmentKey", strShipmentKey);*/
					env.setApiTemplate("getShipmentList",
									"global/template/api/getShipmentDetails.CallToVertex.xml");
					docOutputGetShipmentDetails = AcademyUtil.invokeAPI(env,
							"getShipmentList", docInputGetShipmentDetails);
					env.clearApiTemplate("getShipmentList");
					docOutputGetShipmentDetails = XMLUtil.getDocumentForElement((Element)XMLUtil.getFirstElementByName(docOutputGetShipmentDetails.getDocumentElement(), AcademyConstants.ELE_SHIPMENT));
					docOutputGetShipmentDetails.getDocumentElement()
							.setAttribute("CallType", "QuoteCall");
					// CR - Vertex changes; set Sterling Function name
					docOutputGetShipmentDetails.getDocumentElement()
							.setAttribute("TranType", "ChangeShipment");
					/**
					 * Start - Fix for 4131 as part of R026H - Use the latest
					 * changes of Shipment line and ignore them in Vertex call
					 * 
					 */
					boolean bUpdateShipmentForPackSlip = false;
					Element shipmentDetailEle = docOutputGetShipmentDetails
							.getDocumentElement();
					String strShipmentType = shipmentDetailEle
							.getAttribute("ShipmentType");
					Element eleShipmentLines = (Element) shipmentDetailEle
							.getElementsByTagName(
									AcademyConstants.ELE_SHIPMENT_LINES)
							.item(0);
					for (int i = 0; i < list.getLength(); i++) {
						Element eleModShipmentLine = (Element) list.item(i);
						// START STL-1654 : For zero line quantity, vertex call was not happening. Due to different document, removeChild function was failing.
						//Modified the document for XPathUtil to retain the same document.
						// Get the matching Shipment Line from Shipment Details
						//Document docShipmentLines =XMLUtil.getDocumentForElement(eleShipmentLines);
						/*Element eleShipmentLine = XMLUtil.getElementByXPath(docShipmentLines, "/ShipmentLines/ShipmentLine[@ShipmentLineKey='"
								+ eleModShipmentLine
								.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY)
						+ "']");*/
						Element eleShipmentLine = XMLUtil.getElementByXPath(docOutputGetShipmentDetails, "Shipment/ShipmentLines/ShipmentLine[@ShipmentLineKey='"
								+ eleModShipmentLine
								.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY)
						+ "']");
						// END STL-1654 : For zero line quantity, vertex call was not happening. Due to different document, removeChild function was failing.
						/*Element eleShipmentLine = (Element) XPathUtil
								.getNode(
										eleShipmentLines,
										"ShipmentLine[@ShipmentLineKey='"
												+ eleModShipmentLine
														.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY)
												+ "']");*/
						// Found the Shipment Line or not
						if (!YFCObject.isNull(eleShipmentLine)
								&& !YFCObject.isVoid(eleShipmentLine)) {
							// Get the attribute 'Action' from Modified shipment
							// line
							String action = eleModShipmentLine
									.getAttribute(AcademyConstants.ATTR_ACTION);
							// Check for Remove Shipment Line or Qty changed
							log
									.verbose("Check for type of Modification on ShipmentLine");
							if (!YFCObject.isNull(action)
									&& !YFCObject.isVoid(action)
									&& action.equals("Delete")) {
								// Removing the Shipment Line
								log
										.verbose("Removing the Shipment Line : "
												+ eleModShipmentLine
														.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY)
												+ " From Shipment: "
												+ shipmentDetailEle
														.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));

								// Remove the line from input to Vertex
								log
										.verbose("Find the matching Shipment Line in Shipment Details. Remove from details");
								eleShipmentLines.removeChild(eleShipmentLine);
								bUpdateShipmentForPackSlip = true;
							} else {
								// Flow for Shipment Qty Modification.
								// Get the Modified Qty and Actual Qty
								double modShipmentLineQty = Double
										.parseDouble(eleModShipmentLine
												.getAttribute(AcademyConstants.ATTR_QUANTITY));
								double shipmentLineQty = Double
										.parseDouble(eleShipmentLine
												.getAttribute(AcademyConstants.ATTR_QUANTITY));
								log
										.verbose("Modified Shipment line Qty is : "
												+ modShipmentLineQty
												+ " and Actual Qty of Shipment Line is : "
												+ shipmentLineQty);
								// Check for modified Shipment Line Qty to zero
								if (modShipmentLineQty == 0) {
									// No need to send zero Qty to Vertex.
									// Therefore remove
									log
											.verbose("Removing the Shipment Line when modified Qty is zero");
									bUpdateShipmentForPackSlip = true;
									eleShipmentLines
											.removeChild(eleShipmentLine);
									} else if (modShipmentLineQty != shipmentLineQty) {
									// update the Qty with modified Qty
									log
											.verbose("update the Qty with modified Qty");
									bUpdateShipmentForPackSlip = true;
									eleShipmentLine
											.setAttribute(
													AcademyConstants.ATTR_QUANTITY,
													eleModShipmentLine
															.getAttribute(AcademyConstants.ATTR_QUANTITY));
								}
							}
						} // End of Matching Shipment Line check
					} // End of For loop					
					/***********************************************************
					 * Chec if bUpdateShipmentForPackSlip= true. If true append
					 * the Extn element to the inXML with ExtnLinesCancelled
					 * flag set to Y This change is part of the BR2 BatchPrint
					 * logic to determine if there has been any line
					 * cancellation
					 **********************************************************/
					if ((bUpdateShipmentForPackSlip)
							&& (strShipmentType.equals("CON")
									|| strShipmentType.equals("CONOVNT") || strShipmentType
									.equals("GC")) ||  strShipmentType
									.equals("GCO")) {
						log.verbose("****Pack Slip attribute on shipment flow being executed");
						//checkWaveStatusAndUpdateShipment(env, shipmentDetailEle,inXML);
						//String IsPrintBatchPackSlipRequired = "Y ";
						env.setTxnObject("IsPrintBatchPackSlipRequired", "Y");
			
					}
					// Check the modified ShipmentDetails before the Vertex call
					if (docOutputGetShipmentDetails.getDocumentElement()
							.getElementsByTagName(
									AcademyConstants.ELE_SHIPMENT_LINE)
							.getLength() > 0) {
						log
								.verbose("Shipment Details contains active shipment line. Therefore, invoke Vertex");
						vertexQuoteCallReq = AcademyUtil.invokeService(env,
								"AcademyChangeShipmentToQuoteCallRequest",
								docOutputGetShipmentDetails);
						env.clearApiTemplate("getShipmentDetails");
						/* End Fix - 9692 */

						// }
						Document vertexQuoteCallResp = AcademyUtil
								.invokeService(env,
										"AcademyVertexQuoteCallRequest",
										vertexQuoteCallReq);

						env.setTxnObject("ShipmentQuoteCall",
								vertexQuoteCallResp);
						//STL-1694 Begin : This flag is used to update ExtnInvoiceNo for new PRO_FORMA invoice CHANGE_SHIPMENT.ON_SUCCESS event
						env.setTxnObject(AcademyConstants.STR_UPDATE_EXTN_INVOICE_NO,AcademyConstants.STR_YES);
						log.verbose(AcademyConstants.STR_UPDATE_EXTN_INVOICE_NO +" object is set in env as :"+ AcademyConstants.STR_YES);
						//STL-1694 End
					}

					// END for Fix # 4079 and # 4131
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			//throw new YFSException(e.getMessage());
		}
		log
				.endTimer(" End of AcademyYDMBeforeChangeShipmentUEImpl-> beforeChangeShipment Api");
		//Bopis Inventory Management : Unreserve Message to SIM changes :Begin
		inXML = stampShipLneFlgToSndMsgToSIM(inXML,env);
		//Bopis Inventory Management : Unreserve Message to SIM changes :End
		//OMNI-40994 :Start
		/* OMNI-56207 Enabling OOB Custom shipment cancellation logic for WCS Cancellation - Start */
		String strEnabledProgId = null;
		try {
			strEnabledProgId = isProgIDEnabledForShipCancel(env);
		} catch (Exception e) {
			e.printStackTrace();
		}			
		if(!YFCCommon.isVoid(strEnabledProgId) && strEnabledProgId.contains(strProgId)) {
				// This OOB attribute cancels the shipment if the last shipment line is cancelled
				inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_CANCEL_SHIPMENT_ON_ZERO_QTY, AcademyConstants.STR_YES);
		}
		/* OMNI-56207 Enabling OOB Custom shipment cancellation logic for WCS Cancellation - End */
		//OMNI-40994 :End
		return inXML;
	}
	//Bopis Inventory Management : Unreserve Message to SIM changes :Begin
	/**
	 * This method will stamp the attribute ExtnMsgToSIM 
	 * to identify on success of change shipment if the msg needs to be send to 
	 * SIM.
	 * @param inXML
	 * @return
	 */
	public Document stampShipLneFlgToSndMsgToSIM(Document inXML,YFSEnvironment env)
	{
		ArrayList<String> arrShipmentLineListOfUE = new ArrayList<String>();
		YFCDocument inDoc = YFCDocument.getDocumentFor(inXML);
		YFCElement eleShipment = inDoc.getDocumentElement();
		YFCNodeList<YFCElement> nlShipmentLine = eleShipment.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		for(YFCElement eleShipmentLine : nlShipmentLine)
		{
			String strShipmentLineKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
			double dblStrShortageQty=0;
			YFCElement eleExtn = eleShipmentLine.getChildElement(AcademyConstants.ELE_EXTN);
			if(YFCCommon.isVoid(eleExtn))
			{
				eleExtn = eleShipmentLine.createChild(AcademyConstants.ELE_EXTN);
			}
			String strShortageQty = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHORTAGE_QTY);
			// code changes for OMNI-99090-- Start
			String strExtnReasonCode = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_REASON_CODE);
			// code changes for OMNI-99090-- End
			if(!YFCCommon.isVoid(strShortageQty))
			{
				dblStrShortageQty = Double.parseDouble(strShortageQty);
			}
			// Added AcademyConstants.STR_CUSTOMER_ABANDONED.equals(strExtnReasonCode) for OMNI-99090
			if((!YFCCommon.isVoid(strShortageQty) && dblStrShortageQty>0) || AcademyConstants.STR_CUSTOMER_ABANDONED.equals(strExtnReasonCode) )
			{
				if(!arrShipmentLineListOfUE.contains(strShipmentLineKey))
				{
					//use in transaction object to send cycle count and Unreserve message to SIM.
					arrShipmentLineListOfUE.add(strShipmentLineKey);
				}
				String strExtnMsgToSIM = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_MSG_TO_SIM);
				eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_MSG_TO_SIM,strExtnMsgToSIM);
			}
			else if(YFCCommon.isVoid(strShortageQty) || dblStrShortageQty==0)
			{
				eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_MSG_TO_SIM,AcademyConstants.STR_NO);
			}
			if(YFCCommon.isVoid(strExtnReasonCode))
			{
				eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_REASON_CODE,AcademyConstants.STR_BLANK);
			}
		}
		env.setTxnObject("arrShipmentLineListOfUE", arrShipmentLineListOfUE);
		return inDoc.getDocument();
	}
	//Bopis Inventory Management : UnReserve Message to SIM changes :End
	public void setProperties(Properties arg0) throws Exception {

	}
	 /*
	  * This method is used to return the Common Code value for enabling the cancellation of Shipments
	  * for specified Modified Program id's if the last shipment line is cancelled
	  */
	 public static String isProgIDEnabledForShipCancel (YFSEnvironment env) throws Exception {

	    	Document getCommonCodeListInDoc = null;
	    	Document getCommonCodeListOutDoc = null;
	    	String strProgID = null;

	    	try {
	    		getCommonCodeListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
	    		getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.CANCELLATION_PROG_ID);
	    		getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ORG_CODE_ATTR, AcademyConstants.PRIMARY_ENTERPRISE);

	    		Document getCommonCodeListOPTempl = XMLUtil
	    				.getDocument("<CommonCode CodeValue='' CodeShortDescription=''/>");
	    		env.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST, getCommonCodeListOPTempl);

	    		getCommonCodeListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST,
	    				getCommonCodeListInDoc);
	    		env.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);

	    		if (getCommonCodeListOutDoc != null) {
	    			Element CommonCodeListEle = getCommonCodeListOutDoc.getDocumentElement();
	    			Element CommonCodeEle = (Element) CommonCodeListEle.getElementsByTagName("CommonCode").item(0);
	    			strProgID = CommonCodeEle.getAttribute("CodeValue");
	    		}
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		throw (Exception) e;
	    	}
	    	
	    	return strProgID;
	    }
}
