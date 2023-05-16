package com.academy.ecommerce.sterling.invoice.api;

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;

/* This service is for appending GC card details to Tlog message
 * GCD-255,GCD-256,GCD-257
 */
public class AcademyTlogGCIssuedDetails_v2 {

	private static Logger log = Logger.getLogger(AcademyTlogGCIssuedDetails_v2.class.getName());
	
	private Properties props;
	
	/**
	 * @param props
	 */
	public void setProperties(Properties props) {
		this.props = props;
	}
	
	/**
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document processTlogXML(YFSEnvironment env, Document inDoc) throws Exception {

		Document outGetShipmentDetails = null;
		Document giftCardIssued = null;		
		
		Element eleShipment = null;
		Element eleContrs = null;
		Element eleContr = null;
		Element eleContrDtls = null;
		Element eleContainerDetail = null;
		Element eleShipTagSerls = null;
		Element eleShipTagSerl = null;		
		Element eleCurrContainer = null;
		Element eleCurrContainerDetail = null;
		Element eleCurrShipTagSerial = null;
		
		NodeList nlContainer = null;
		NodeList nlContainerDetail = null;
		NodeList nlShipTagSerial = null;
		
		int bAppendGCCtr = 0;
		boolean bAppendGCDtl = false;		
		
		log.verbose("Begin of AcademyTlogGCIssuedDetails.processTlogXML() inDoc ::"	+ XMLUtil.getXMLString(inDoc));
		//Creating the shipment Document
		Document getShipmentDtl = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);

		String strShipmentKey = XPathUtil.getString(inDoc,"/InvoiceDetail/InvoiceHeader/Shipment/" + "@ShipmentKey");
		String strShipmentNo = XPathUtil.getString(inDoc,"/InvoiceDetail/InvoiceHeader/Shipment/" + "@ShipmentNo");
		log.verbose("AcademyTlogGCIssuedDetails.processTlogXML() ShipmentKey ::"+ strShipmentKey);
		
		if (!YFSObject.isVoid(strShipmentKey)) {
			Element el_gtCmpShipDtl = getShipmentDtl.getDocumentElement();
			el_gtCmpShipDtl.setAttribute(AcademyConstants.SHIPMENT_KEY,	strShipmentKey);  
			log.verbose("AcademyTlogGCIssuedDetails.processTlogXML() getCmplShipDTL input doc"+ XMLUtil.getXMLString(getShipmentDtl));
			
			//Document Template for the getShipment Details
			Document outputTemplate = XMLUtil
					.getDocument("<Shipment ShipmentKey=\"\"  ShipmentNo=\"\">"
							+ "<Containers TotalNumberOfRecords=\"\">" 
							+ "<Container TrackingNo=\"\">"
							+ "<ContainerDetails>"
							+ "<ContainerDetail  ContainerDetailsKey=\"\"  ItemID=\"\"  ShipmentKey=\"\"  ShipmentLineKey=\"\"  UnitOfMeasure=\"\"  OrderLineKey=\"\" isHistory=\"\">"
							+ "<ShipmentTagSerials  TotalNumberOfRecords=\"\">"
							+ "<ShipmentTagSerial   ShipmentLineKey=\"\"  LotAttribute1=\"\"  LotAttribute2=\"\"  SerialNo=\"\" />"
							+ "</ShipmentTagSerials>" 
							+ "</ContainerDetail>"
							+ "</ContainerDetails>"
							+ "</Container>"
							+ "</Containers>" 
							+ "</Shipment>");

			env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_DETAILS, outputTemplate);
			outGetShipmentDetails = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_SHIPMENT_DETAILS, getShipmentDtl);
			env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_DETAILS);
			log.verbose("AcademyTlogGCIssuedDetails.processTlogXML() getShipmentDetails OutputXML ::"+ XMLUtil.getXMLString(outGetShipmentDetails));
			
			giftCardIssued = XMLUtil.createDocument("GiftCardIssued");			
			eleShipment = giftCardIssued.createElement(AcademyConstants.ELE_SHIPMENT);
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
			
			eleContrs = giftCardIssued.createElement(AcademyConstants.ELE_CONTAINERS);
			eleShipment.appendChild(eleContrs);
			
			if (!YFSObject.isVoid(outGetShipmentDetails)) {
				nlContainer = outGetShipmentDetails.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
				eleContr = giftCardIssued.createElement(AcademyConstants.ELE_CONTAINER);

				
				
				for (int iCont=0; iCont < nlContainer.getLength(); iCont++ ) {
					eleCurrContainer = (Element) nlContainer.item(iCont);
					nlContainerDetail = eleCurrContainer.getElementsByTagName(AcademyConstants.CONTAINER_DETL_ELEMENT);					
					eleContr.setAttribute("TrackingNo", eleCurrContainer.getAttribute("TrackingNo"));					
					
					eleContrDtls = giftCardIssued.createElement(AcademyConstants.ELE_CONTAINER_DTLS);
					eleContr.appendChild(eleContrDtls);
					
					for (int iContDtl=0; iContDtl < nlContainerDetail.getLength(); iContDtl++ ) {
						eleCurrContainerDetail = (Element) nlContainerDetail.item(iContDtl);
						nlShipTagSerial = eleCurrContainerDetail.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_TAG_SERIAL);						
						
						eleContainerDetail = giftCardIssued.createElement(AcademyConstants.CONTAINER_DETL_ELEMENT);
						eleContrDtls.appendChild(eleContainerDetail);
						
						eleShipTagSerls = giftCardIssued.createElement("ShipmentTagSerials");
						eleShipTagSerls.setAttribute("TotalNumberOfRecords",String.valueOf(nlShipTagSerial.getLength()));
						
						//GC qty 51 or more considered as Bulk orders
						//if (nlShipTagSerial.getLength() < 51){
							for (int iShipTagSrl=0; iShipTagSrl < nlShipTagSerial.getLength(); iShipTagSrl++ ) {
								eleCurrShipTagSerial = (Element) nlShipTagSerial.item(iShipTagSrl);
								eleShipTagSerl = giftCardIssued.createElement(AcademyConstants.ELE_SHIPMENT_TAG_SERIAL);
								
								if (!YFSObject.isVoid(eleCurrShipTagSerial.getAttribute("SerialNo"))) {
									bAppendGCDtl = true;
									bAppendGCCtr++;
									eleShipTagSerl.setAttribute("LotAttribute1", eleCurrShipTagSerial.getAttribute("LotAttribute1"));
									eleShipTagSerl.setAttribute("LotAttribute2", eleCurrShipTagSerial.getAttribute("LotAttribute2"));
									eleShipTagSerl.setAttribute("SerialNo", eleCurrShipTagSerial.getAttribute("SerialNo"));								
									eleShipTagSerls.appendChild(eleShipTagSerl);
								}
							}//end of shipment tag for loop
						//}
						if(bAppendGCDtl){
							eleContainerDetail.setAttribute("ContainerDetailsKey", eleCurrContainerDetail.getAttribute("ContainerDetailsKey"));
							eleContainerDetail.setAttribute("ShipmentKey", eleCurrContainerDetail.getAttribute("ShipmentKey"));
							eleContainerDetail.setAttribute("ShipmentLineKey", eleCurrContainerDetail.getAttribute("ShipmentLineKey"));
							eleContainerDetail.setAttribute("OrderLineKey", eleCurrContainerDetail.getAttribute("OrderLineKey"));
							eleContainerDetail.setAttribute("ItemID", eleCurrContainerDetail.getAttribute("ItemID"));

							eleContainerDetail.appendChild(eleShipTagSerls);
							
						}
					}//end of container detail for loop
					if(bAppendGCDtl){
						eleContrs.appendChild(eleContr);
					}
					bAppendGCDtl = false;//Reset the flag for next Container loop
				}//end of container for loop				
			}
			giftCardIssued.getDocumentElement().appendChild(eleShipment);
			
			if(bAppendGCCtr > 0) {
				Element elamentFinalTlog = (Element) inDoc.importNode(giftCardIssued.getDocumentElement(), true);
				inDoc.getDocumentElement().appendChild(elamentFinalTlog);
			}			
		}
		log.verbose("End of AcademyTlogGCIssuedDetails.processTlogXML() Api outDoc::"+ XMLUtil.getXMLString(inDoc));
		return inDoc;
	}
}