package com.academy.ecommerce.sterling.sts;

/*##################################################################################
*
* Project Name                : STS
* Module                      : OMS
* Author                      : CTS
* Date                        : 16-July-2020 
* Description				   : This class does following 
* 								  1.It Receives the Trailer Updates for STS Shipments 					   
* 									and marks the shipment as Shipped To Store if the 
* 									shipment status is in Ready To Ship To Store status
* 									else it will call changeShipment to update the Date 
* 									fields.
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       Remarks/Description                      
* ---------------------------------------------------------------------------------
* 16-JULY-2020		CTS  	 			  1.0           	Initial version
* ##################################################################################*/

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XPathUtil;
import com.cts.sterling.custom.accelerators.util.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademySTSUpdateTrailerDetails { 
	
	/**
	 * log variable.
	 */
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSUpdateTrailerDetails.class);
	
	public Document updateTrailerDetails(YFSEnvironment env, Document inDoc) throws IllegalArgumentException, Exception{
		
		log.beginTimer("AcademySTSUpdateTrailerDetails.updateTrailerDetails()");
		log.verbose("Start - Inside AcademySTSUpdateTrailerDetails.updateTrailerDetails()"); 
		log.verbose("Input - AcademySTSUpdateTrailerDetails.updateTrailerDetails() input"+XMLUtil.getString(inDoc));
		
		String strShipmentNo = null;
		String strShipmentStatus = null;
		String strExtnContext 	 = null;		
		String strTrailerNo  = null; 
		String strManifestNo = null;
	
		String strExpectedDeliveryDate = null;
		String strExpectedShipmentDate = null;
		String strActualShipmentDate  = null;
		
		Element eleInputEle = null;
		Element eleExtn = null;
		eleInputEle = inDoc.getDocumentElement();
		eleExtn = (Element)eleInputEle.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
		strShipmentNo = eleInputEle.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		strExtnContext = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_CONTEXT);
		strManifestNo = eleInputEle.getAttribute(AcademyConstants.ATTR_MANIFEST_NO);
		strTrailerNo  = eleInputEle.getAttribute(AcademyConstants.STR_TRAILER_NO);
		strExpectedDeliveryDate = eleInputEle.getAttribute(AcademyConstants.ATTR_EXPECTED_DELIVERY_DATE);
		strExpectedShipmentDate = eleInputEle.getAttribute(AcademyConstants.ATTR_EXPECTED_SHIPMENT_DATE);
		strActualShipmentDate  = eleInputEle.getAttribute(AcademyConstants.ATTR_ACTUAL_SHIPMENT_DATE);
		
		if ((!YFCObject.isVoid(strShipmentNo) && !YFCObject.isNull(strShipmentNo)) || (!YFCObject.isVoid(strExtnContext)  && !YFCObject.isNull(strExtnContext))) {

			if (YFCObject.isNull(strTrailerNo)) {
				eleInputEle.removeAttribute(AcademyConstants.STR_TRAILER_NO);
			}
			if (YFCObject.isNull(strManifestNo)) {
				eleInputEle.removeAttribute(AcademyConstants.ATTR_MANIFEST_NO);
			}
			if (YFCObject.isNull(strExpectedDeliveryDate)) {
				eleInputEle.removeAttribute(AcademyConstants.ATTR_EXPECTED_DELIVERY_DATE);
			}
			if (YFCObject.isNull(strExpectedShipmentDate)) {
				eleInputEle.removeAttribute(AcademyConstants.ATTR_EXPECTED_SHIPMENT_DATE);
			}
			if (YFCObject.isNull(strActualShipmentDate)) {
				eleInputEle.removeAttribute(AcademyConstants.ATTR_ACTUAL_SHIPMENT_DATE);
			}

			Document docgetShipmentListOut = getShipmentList(env, strShipmentNo);
			Element eleShipment = (Element) XPathUtil.getNode(docgetShipmentListOut,
					AcademyConstants.XPATH_ELEM_SHIPMENT);
			if (!YFCObject.isNull(eleShipment) && eleShipment.hasAttributes()) {
				log.verbose("Enetring to process trailer messages" + XMLUtil.getString(docgetShipmentListOut));
				strShipmentStatus = eleShipment.getAttribute(AcademyConstants.ATTR_STATUS);
				eleInputEle.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,
						eleShipment.getAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE));
				eleInputEle.setAttribute(AcademyConstants.ATTR_SELL_ORG_CODE,
						eleShipment.getAttribute(AcademyConstants.ATTR_SELL_ORG_CODE));

				if (!YFCObject.isNull(strExtnContext) && (strExtnContext.equals(AcademyConstants.STR_TRAILER_DEPARTURE)
						|| strExtnContext.equals(AcademyConstants.STR_GATE_OUT))) {
					if (strShipmentStatus.equals(AcademyConstants.VAL_READY_TO_SHIP_TO_STORE_STATUS)) {
						log.verbose("Input - ConfirmShipment Api" + XMLUtil.getString(inDoc));
						/**
						 * OMNI30544, OMNI30545 Begin
						 * Setting the transaction objects to identify the status change in sales order and control 
						 * the yantriks updates based on these objects
						 */
						log.verbose("Setting transaction objects as Shipped and "+eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
						env.setTxnObject(AcademyConstants.ATTR_STATUS, AcademyConstants.V_SHIPPED);
						env.setTxnObject(AcademyConstants.ATTR_SHIPMENT_KEY,eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
						log.verbose("Txn Objects set successfully");
						// OMNI30544, OMNI30545 END
						AcademyUtil.invokeAPI(env, AcademyConstants.CONFIRM_SHIPMENT_API, inDoc);
						
					} else {
						log.verbose("Input - ChangeShipment Api" + XMLUtil.getString(inDoc));
						AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, inDoc);

					}

				} else {
					String errorDescription = "ExtnContent is blank";
					YFSException yfse = new YFSException();
					yfse.setErrorCode(AcademyConstants.ERR_CODE_23);
					yfse.setErrorDescription(errorDescription);
					throw yfse;
				}
			} else {
				String errorDescription = "Shipment is not Valid";
				YFSException yfse = new YFSException();
				yfse.setErrorCode(AcademyConstants.ERR_CODE_24);
				yfse.setErrorDescription(errorDescription);
				throw yfse;
			}
		}else {
			String errorDescription = "ShipmentNo or ExtnContext is not passed in Input";
			YFSException yfse = new YFSException();
			yfse.setErrorCode(AcademyConstants.ERR_CODE_25);
			yfse.setErrorDescription(errorDescription);
			throw yfse;
		}
			
		
		log.verbose("End - AcademySTSUpdateTrailerDetails.updateTrailerDetails()");
		log.endTimer("AcademySTSUpdateTrailerDetails.updateTrailerDetails()");
		return inDoc;
	}

	
	private Document getShipmentList(YFSEnvironment env, String strShipmentNo) throws IllegalArgumentException, Exception {
		// TODO Auto-generated method stub 
		log.verbose("Start - Inside AcademySTSUpdateTrailerDetails.getShipmentList()"); 
		log.verbose(strShipmentNo);
		
		Document docgetShipmentListInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT); 
		
		docgetShipmentListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		
		Document docgetShipmentListTemplate = XMLUtil.getDocument("<Shipment EnterpriseCode='' SellerOrganizationCode='' ShipNode='' ShipmentNo='' TrailerNo='' ManifestNo=''"
				+ " ExpectedDeliveryDate='' ExpectedShipmentDate='' ActualDeliveryDate='' Status='' ShipmentKey='' /> ");
		
		
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docgetShipmentListTemplate); 
		
		log.verbose("Input for getShipmentList Api is "+XMLUtil.getString(docgetShipmentListInput));
		log.verbose("Template for getShipmentList Api is "+XMLUtil.getString(docgetShipmentListTemplate));
		Document docgetShipmentListOutput = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_GET_SHIPMENT_LIST, docgetShipmentListInput);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		log.verbose("Output for getShipmentList Api is "+XMLUtil.getString(docgetShipmentListOutput));
		log.verbose("End - Inside AcademySTSUpdateTrailerDetails.getShipmentList()"); 
		return docgetShipmentListOutput;
	}

}
