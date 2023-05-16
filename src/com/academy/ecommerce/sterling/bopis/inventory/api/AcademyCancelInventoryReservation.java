package com.academy.ecommerce.sterling.bopis.inventory.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @Author : Chiranthan(SapientRazorfish_)
 * @JIRA# : BOPIS-264 – WCS-OMS Inventory Reservation (Real time Call)
 * @Date : Created on 11-07-2018
 * 
 * @Purpose : 
 * This interface(AcademyCancelInventoryReservationService) is exposed as a REST WebService, for cancellation of inventory reservation from WCS.
 * 
 * This class does the following
 *     - get all the reservations for the ReservationID using getInventoryReservationList API
 *     - For each reservation line, call cancelReservation API
 *   	  
 **/

public class AcademyCancelInventoryReservation{
	
	private static Logger log = Logger.getLogger(AcademyCancelInventoryReservation.class.getName());
	private Properties props;
	public void setProperties(Properties props) throws Exception {		
		this.props = props;
	}
	
	/**
	 * This method loops through getInventoryReservationList API output & calls cancelReservation for each reservation line.
	 * @param env
	 * @param inDoc
	 */
	public Document cancelReservedInventory(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Entering AcademyCancelInventoryReservation.cancelReservedInventory() :: "+XMLUtil.getXMLString(inDoc));
		String strReservationID = null;
		NodeList nlInventoryReservation = null;
		Element eleInventoryReservation = null;
		Element eleCancelReservationResp = null;
		Element eleCancelResrvResp = null;
		Document docOutGetInvRsrvList = null;
		Document docCancelReservationResp = null;		
		YFSException ex = new YFSException();
		String errorDescription = null;	
		
		try{
			strReservationID = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESERV_ID);
			//If WCS sends blank ReservationID, throw YFSException
			if (YFCObject.isVoid(strReservationID)) {
				errorDescription = "Blank ReservationID";
				ex.setErrorCode("RCAN0002");
				ex.setErrorDescription(errorDescription);
				throw ex;
			}
			
			docOutGetInvRsrvList = getInventoryReservationList(env, strReservationID);
			
			nlInventoryReservation = docOutGetInvRsrvList.getElementsByTagName(AcademyConstants.ELE_INV_RESERV);
			
			//If WCS tries to cancel invalid(wrong reservationID/already cancelled reservation) reservation, throw YFSException
			if(!(nlInventoryReservation.getLength() > 0)){
				errorDescription = "Reservation does not exist in OMS";
				ex.setErrorCode("RCAN0001");
				ex.setErrorDescription(errorDescription);
				throw ex;
			}
			
			for(int i=0; i < nlInventoryReservation.getLength(); i++){
				eleInventoryReservation = (Element) nlInventoryReservation.item(i);
				
				cancelReservation(env, eleInventoryReservation);
			}			
		}catch (Exception e){			
			if (!StringUtil.isEmpty(ex.getErrorCode())) {
				throw ex;
			} else {				
				e.printStackTrace();			
				throw new YFSException(e.getMessage(),"RCAN0003", "Exception Ocuured while cancelling inventory reservation : "+strReservationID);
			}
		}
		
		String strRespDescription = "ReservationID " + strReservationID + " is cancelled successfully.";
		
		docCancelReservationResp = XMLUtil.createDocument(AcademyConstants.ELE_CANCEL_RESERV);
		eleCancelReservationResp = docCancelReservationResp.getDocumentElement();
		eleCancelResrvResp = docCancelReservationResp.createElement(AcademyConstants.STR_RESULT);
		eleCancelResrvResp.setAttribute(AcademyConstants.STR_SUCCESS, AcademyConstants.STR_YES);
		eleCancelResrvResp.setAttribute(AcademyConstants.ATTR_DESC, strRespDescription);
		eleCancelResrvResp.setAttribute(AcademyConstants.STR_HTTP_CODE, AcademyConstants.SUCCESS_CODE);		
		eleCancelReservationResp.appendChild(eleCancelResrvResp);	
		
		log.verbose("Exiting AcademyCancelInventoryReservation.cancelReservedInventory() :: "+XMLUtil.getXMLString(docCancelReservationResp));
		
		return docCancelReservationResp;		
	}
	
	/**
	 * This method will invoke getInventoryReservationList API
	 * @param env
	 * @param String ReservationID 
	 * @return docOutGetInventoryReservationList
	 */
	public Document getInventoryReservationList(YFSEnvironment env, String strReservationID) throws Exception {
		log.verbose("Entering AcademyCancelInventoryReservation.getInventoryReservationList() :: "+strReservationID);
		Document docInGetInvRsrvList = null;
		Document docOutGetInvRsrvList = null;
		Element eleInGetInvRsrvList = null;
		
		docInGetInvRsrvList = XMLUtil.createDocument(AcademyConstants.ELE_INV_RESERV);
		eleInGetInvRsrvList = docInGetInvRsrvList.getDocumentElement();
		eleInGetInvRsrvList.setAttribute(AcademyConstants.ATTR_RESERV_ID, strReservationID);
		
		log.verbose("Input to getInventoryReservationList : "+XMLUtil.getXMLString(docInGetInvRsrvList));
		docOutGetInvRsrvList = AcademyUtil.invokeService(env, AcademyConstants.SERVICE_GET_INVENTORY_RESERVATION_LIST, docInGetInvRsrvList);
		log.verbose("Output from getInventoryReservationList : "+XMLUtil.getXMLString(docOutGetInvRsrvList));
		
		log.verbose("Exiting AcademyCancelInventoryReservation.getInventoryReservationList()");
		return docOutGetInvRsrvList;
	}
	
	
	/**
	 * This method will invoke cancelReservation API
	 * @param env
	 * @param eleInventoryReservation
	 */
	public void cancelReservation(YFSEnvironment env, Element eleInventoryReservation) throws Exception {
		log.verbose("Entering AcademyCancelInventoryReservation.cancelReservation() :: "+XMLUtil.getElementXMLString(eleInventoryReservation));
		
		Document docInCancelReservation = null;
		Document docOutCancelReservation = null;
		Element eleInCancelReservation = null;
		Element eleItem = (Element) eleInventoryReservation.getElementsByTagName(AcademyConstants.ELEM_ITEM).item(0);
		
		docInCancelReservation = XMLUtil.createDocument(AcademyConstants.ELE_CANCEL_RESERV);
		eleInCancelReservation = docInCancelReservation.getDocumentElement();
		eleInCancelReservation.setAttribute(AcademyConstants.ATTR_RESERV_ID, eleInventoryReservation.getAttribute(AcademyConstants.ATTR_RESERV_ID));
		eleInCancelReservation.setAttribute(AcademyConstants.ATTR_DEMAND_TYPE, eleInventoryReservation.getAttribute(AcademyConstants.ATTR_DEMAND_TYPE));
		eleInCancelReservation.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, eleInventoryReservation.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD));
		eleInCancelReservation.setAttribute(AcademyConstants.ORGANIZATION_CODE, eleInventoryReservation.getAttribute(AcademyConstants.ORGANIZATION_CODE));
		eleInCancelReservation.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID));
		eleInCancelReservation.setAttribute(AcademyConstants.ATTR_PROD_CLASS, eleItem.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
		eleInCancelReservation.setAttribute(AcademyConstants.ATTR_UOM, eleItem.getAttribute(AcademyConstants.ATTR_UOM));
		eleInCancelReservation.setAttribute(AcademyConstants.ATTR_QTY_CANCEL, eleInventoryReservation.getAttribute(AcademyConstants.ATTR_QUANTITY));
		eleInCancelReservation.setAttribute(AcademyConstants.ATTR_SHIP_NODE, eleInventoryReservation.getAttribute(AcademyConstants.ATTR_SHIP_NODE));
		eleInCancelReservation.setAttribute(AcademyConstants.ATTR_SHIP_DATE, eleInventoryReservation.getAttribute(AcademyConstants.ATTR_SHIP_DATE));
		
		log.verbose("Input to cancelReservation : "+XMLUtil.getXMLString(docInCancelReservation));
		docOutCancelReservation = AcademyUtil.invokeAPI(env,AcademyConstants.API_CANCEL_RESERVATION, docInCancelReservation);
		log.verbose("Output from cancelReservation : "+XMLUtil.getXMLString(docOutCancelReservation));
		
		log.verbose("Exiting AcademyCancelInventoryReservation.cancelReservation()");
	}
}
