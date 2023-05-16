package com.academy.ecommerce.sterling.email.api;

import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

/**#########################################################################################
*
* Project Name                : OMS_ESPPhase2_June2021_Rel6
* Module                      : PostNotification Cancel
* Date                        : 24-MAY-2021 
* Description				  : This class translates/updates PostNotification Cancel message xml posted to  queue.
* 								 
*
* #########################################################################################*/

import org.w3c.dom.Document;

import com.academy.ecommerce.sterling.email.util.AcademyEmailUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**#########################################################################################
*
* Project Name                : OMS_ESPPhase2_June2021_Rel6
* Module                      : OMNI-38138 PostNotification - BOPIS CANCEL
* Date                        : 03-JUN-2021 
* Description				  : This class prepares PostNotification message xml for Bopis Cancel - 
* 								when entire shipment is cancelled during backrooom pick 
* 								 
*
* #########################################################################################*/


public class AcademyPostPushNotifyOnBOPISCancel {

	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostPushNotifyOnBOPISCancel.class);

	/**
	 * This method customizes input xml to post notification message for DSV Shipment to ESB queue
	 * 
	 * @param inDoc
	 * @return docPushNotificationMessage
	 * @throws Exception
	 */

	/*Sample Input:
	 * <Shipment CustomerEMailID="NagaShylaja.Garigipati@academy.com"
		    DeliveryMethod="PICK" IgnoreOrdering="Y" OrderNo="2021060306BOPIS" ShipmentNo="400285877">
		    <ShipmentLines>
		        <ShipmentLine ShipmentLineKey="202106030948092864481684" ShipmentLineNo="1">
		            <OrderLine OrderedQty="0" OriginalOrderedQty="3.00">
		                <Order
		                    CustomerEMailID="NagaShylaja.Garigipati@academy.com" OrderNo="2021060306BOPIS">
		                    <PersonInfoBillTo DayPhone="971554893998"
		                        FirstName="Sailaja" LastName="Garigipati"/>
		                    <PersonInfoShipTo DayPhone="971554893998"
		                        FirstName="Sailaja" LastName="Garigipati"/>
		                </Order>
		                <Item ItemDesc="SS 2PK BBALL SOLID S:YELLOW:SMALL" ItemID="017920620"/>
		            </OrderLine>
		        </ShipmentLine>
		    </ShipmentLines>
		</Shipment>
	 */
	public Document preparePostNotificationOnBopisCancel(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("AcademyPostPushNotifyOnBOPISCancel.preparePostNotificationOnBopisCancel()_InXML start:" + XMLUtil.getXMLString(inDoc));
		Document orderDoc = callGetCompleteOrderDetails(env, inDoc);
		Element eleOrderDoc= orderDoc.getDocumentElement();
		String sMaxOrderStatus=eleOrderDoc.getAttribute("MaxOrderStatus");
		String sShipmentNo=inDoc.getDocumentElement().getAttribute("ShipmentNo");
		log.verbose("sMaxOrderStatus::::: " + sMaxOrderStatus);
		boolean partialCancellation = false;
		  if(!YFCObject.isVoid(sMaxOrderStatus)
	   				&& !sMaxOrderStatus.equalsIgnoreCase(AcademyConstants.VAL_CANCELLED_STATUS))

		  {
			  //if MaxOrderStatus is not 9000, set partialCancelleation value to true.
			  partialCancellation=true;
		  }
		 log.verbose("partialCancellation::::: " + partialCancellation);
		removeOrderlines(env, eleOrderDoc,sShipmentNo);
		 if(partialCancellation) {
    		 AcademyEmailUtil.updateMessageRef(env, eleOrderDoc, "ACADEMY_PARTIAL_CANCEL_MSG_ID_PROD", "ACADEMY_PARTIAL_CANCEL_MSG_ID_STG", "ACADEMY_PARTIAL_CANCEL_MSG_TYPE");
         }else {

        	 AcademyEmailUtil.updateMessageRef(env, eleOrderDoc, "ACADEMY_CANCEL_MSG_ID_PROD", "ACADEMY_CANCEL_MSG_ID_STG", "ACADEMY_CANCEL_MSG_TYPE");
         }
	    log.verbose("AcademyPostPushNotifyOnBOPISCancel.preparePostNotificationOnBopisCancel()_InXML end:" + XMLUtil.getXMLString(inDoc));
	    return orderDoc;
	         
	        
	}
	
	
	private Document callGetCompleteOrderDetails(YFSEnvironment env,Document inDoc) throws Exception {
		String sSalesOrderNo = XMLUtil.getAttributeFromXPath(inDoc, "Shipment/@OrderNo");
		Document outXML=null;
		Document inputXML=null;
		try {
			inputXML = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			inputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_NO, sSalesOrderNo);
			inputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
			inputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
			Document templateDocGetCompleteOrderDetails = XMLUtil.getDocument("<Order DocumentType='' EnterpriseCode='' OrderHeaderKey=''  OrderNo=''  Status='' MaxOrderStatus='' CustomerEMailID=''>" + 
					"<PersonInfoBillTo ZipCode=''/>"+
					"		<OrderLines>" + 
					"			<OrderLine DeliveryMethod='' MaxLineStatus='' MinLineStatus='' OrderHeaderKey='' OrderLineKey='' OrderedQty='' OriginalOrderedQty='' "+
				    "             PrimeLineNo='' Status=''>" +
				    "<ItemDetails DisplayItemId='' >"+
					"<PrimaryInformation Description='' ImageLocation='' ImageID=''/>"+
					"</ItemDetails>"+
					"</OrderLine>"+
					"</OrderLines>" + 
					"<Shipments><Shipment ShipmentNo='' ShipmentKey=''>"+
					"<ShipmentLines>"+
					"<ShipmentLine OrderLineKey='' ShipmentKey='' Quantity=''/>"+
					"</ShipmentLines></Shipment></Shipments>" + 
					"</Order>");
			
			env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, templateDocGetCompleteOrderDetails);	
			 outXML = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, inputXML);
			log.verbose("Push Notification BopisCancel - getCompleteOrderDetails - " + XMLUtil.getXMLString(outXML));
			env.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		return outXML;
	}
	
	
		/**
		 * This method removes orderlines which are not cancelled
		 * @param inDoc
		 * @param eleIndoc
		 * @throws Exception
		 */
		
		public static void removeOrderlines(YFSEnvironment env,Element eleIndoc,String sShipmentNo) throws Exception {
			log.verbose("Inside removeOrderlines Method");
			NodeList currentShipmentLines = XPathUtil.getNodeList(eleIndoc, "/Order/Shipments/Shipment[@ShipmentNo='" + sShipmentNo + "']/ShipmentLines/ShipmentLine");
			Set<String> hsShipmentOrderLineKeys = new HashSet<String>();
			//Fetches the list of orderlinekey for the currentShipmentKey

			for (int i = 0; i < currentShipmentLines.getLength(); i++) {
				Element shipmentLine = (Element) currentShipmentLines.item(i);
				//fetch orderlineKey for current shipment line
				String strOrderlineKey =shipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
				//store orderlineKey in hashset
				hsShipmentOrderLineKeys.add(strOrderlineKey);

			}
			NodeList completeOrderLines =XPathUtil.getNodeList(eleIndoc, "/Order/OrderLines/OrderLine");
			for (int i = 0; i < completeOrderLines.getLength(); i++) {
				Element eleOrderline = (Element) completeOrderLines.item(i);
				//fetch orderlineKey for current orderline line
				String strOLK =eleOrderline.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);					
				if (!hsShipmentOrderLineKeys.contains(strOLK)) {					
					//remove current orderline from inDoc
					eleOrderline.getParentNode().removeChild(eleOrderline);
				}	
			}
		}

}
