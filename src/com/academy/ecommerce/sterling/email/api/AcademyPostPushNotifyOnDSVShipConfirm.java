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
* Module                      : OMNI-38141 PostNotification - DSV Ship confirm
* Date                        : 26-MAY-2021 
* Description				  : This class prepares PostNotification message xml for DSV Shipment 
* 								 
*
* #########################################################################################*/


public class AcademyPostPushNotifyOnDSVShipConfirm {

	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostPushNotifyOnDSVShipConfirm.class);

	/**
	 * This method customizes input xml to post notification message for DSV Shipment to ESB queue
	 * 
	 * @param inDoc
	 * @return docPNMessage
	 * @throws Exception
	 */

	/*Sample Input:
	 * <Shipments>
		    <Shipment OrderHeaderKey="20210525071257213291513"
		        OrderNo="Y100215034" SCAC="FEDX"
		        ShipmentKey="20210525072058213293113" ShipmentType="NWG">
		        <ShipmentLines>
		            <ShipmentLine OrderHeaderKey="20210525071257213291513"
		                ShipmentKey="20210525072058213293113" ShipmentLineKey="20210525072058213293112"/>
		        </ShipmentLines>
		        <OrderInvoiceList>
		            <OrderInvoice DateInvoiced="2021-05-25T07:20:58-05:00"
		                InvoiceNo="298172" InvoiceType="PRO_FORMA" OrderNo="2021052505DSV"/>
		        </OrderInvoiceList>
		    </Shipment>
		</Shipments>

	 */
	public Document preparePostNotificationMsgDSVShipment(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("AcademyPostPushNotifyOnDSVShipConfirm.preparePostNotificationMessageDSVShipment()_InXML start:" + XMLUtil.getXMLString(inDoc));
		Document orderDoc = callGetCompleteOrderDetails(env, inDoc);
		Element eleOrderDoc= orderDoc.getDocumentElement();
		String sCurrentShipmentKey = XMLUtil.getAttributeFromXPath(inDoc, "Shipments/Shipment/@ShipmentKey");
		//AcademyEmailUtil.removeOrderlines(inDoc, env, eleOrderDoc);
		removeOrderlines(inDoc, env, eleOrderDoc,sCurrentShipmentKey);
		AcademyEmailUtil.updateMessageRef(env, eleOrderDoc, "DSV_SHP_CONF_MSG_ID_PROD", "DSV_SHP_CONF_MSG_ID", "DSV_SHP_CONF_MSG_TYPE");
	    log.verbose("AcademyPostPushNotifyOnDSVShipConfirm.preparePostNotificationMessageDSVShipment()_InXML end:" + XMLUtil.getXMLString(inDoc));
	    return orderDoc;
	         
	        
	}
	
	
	private Document callGetCompleteOrderDetails(YFSEnvironment env, Document inDoc) throws Exception {
		String sSalesOrderNo = XMLUtil.getAttributeFromXPath(inDoc, "Shipments/Shipment/OrderInvoiceList/OrderInvoice/@OrderNo");
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
				    "<ItemDetails DisplayItemId='' ><PrimaryInformation Description='' ImageLocation='' ImageID=''/></ItemDetails></OrderLine></OrderLines>"+
					"<Shipments><Shipment ShipmentNo='' ShipmentKey=''>"+
					"<ShipmentLines>"+
					"<ShipmentLine OrderLineKey='' ShipmentKey='' Quantity='' ChainedFromOrderLineKey=''/>"+
					"</ShipmentLines></Shipment></Shipments>" + 
					"</Order>");
			env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, templateDocGetCompleteOrderDetails);	
			 outXML = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, inputXML);
			log.verbose("DSV Ship Confirm - getCompleteOrderDetails - " + XMLUtil.getXMLString(outXML));
			env.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		return outXML;
	}
	
	
		/**
		 * This method Compares the list of OrderlineKey with the sales order orderlineKey 
		 * fetched in the hashset and removes the orderline
		 * element from order that doesn't match
		 * 
		 * @param inDoc
		 * @param eleIndoc
		 * @throws Exception
		 */
		
		public static void removeOrderlines(Document inDoc,YFSEnvironment env,Element eleIndoc,String sCurrentShipmentKey) throws Exception {
			log.verbose("Inside removeOrderlines Method");
			NodeList currentShipmentLines = XPathUtil.getNodeList(eleIndoc, "/Order/Shipments/Shipment[@ShipmentKey='" + sCurrentShipmentKey + "']/ShipmentLines/ShipmentLine");
			Set<String> hsShipmentOrderLineKeys = new HashSet<String>();
			//Fetches the list of orderlinekey for the currentShipmentKey

			for (int i = 0; i < currentShipmentLines.getLength(); i++) {
				Element shipmentLine = (Element) currentShipmentLines.item(i);
				//fetch orderlineKey for current shipment line
				String strOrderlineKey =shipmentLine.getAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_LINE_KEY);
				//store orderlineKey in hashset
				hsShipmentOrderLineKeys.add(strOrderlineKey);

			}

			NodeList completeOrderLines = XPathUtil.getNodeList(eleIndoc, "/Order/OrderLines/OrderLine");
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
