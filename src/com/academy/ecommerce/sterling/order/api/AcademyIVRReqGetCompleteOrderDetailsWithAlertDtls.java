package com.academy.ecommerce.sterling.order.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author kparvath
 * 
 * Customer gives Order No,
 * then retrieve the Order details and alert details for that order.
 * 
 * Service Name: AcademyIVRReqGetCompleteOrderDetailsWithAlertDtls
 * 
 * Input XML:
 * 	<Order OrderNo="" EnterpriseCode="" DocumentType=""/>
 * 
 * Sample Output:
 * 
 * 	<Order MaxOrderStatusDesc="" OrderDate="" OrderNo="">
 *  		<OrderLines>
 *  			<OrderLine MaxLineStatusDesc="">
 *  				<Shipment ActualDeliveryDate="" CarrierServiceCode="" ExpectedDeliveryDate="" ShipDate="">
 *  					<Containers>
 *							<Container TrackingNo=""/>
 *						</Containers>
 *					</Shipment>
 *				</OrderLine>
 *			</OrderLines>
 *	<Inbox ExceptionType="" QueueId="" Status="" Description="" InboxKey="" >
 *			<InboxReferencesList>
 *				<InboxReferences Name="" ReferenceType="" value=""/>
 *			</InboxReferencesList>
 *			<InboxNotesList>
 *				<InboxNotes AuditTransactionId="" ContactReference="" ContactType="" ContactUser="" NoteText=""
 *							ReasonCode="" SequenceNo="" Tranid="" />
 *			</InboxNotesList>
 *		</Inbox>
 *	</Order>
 *
 */
public class AcademyIVRReqGetCompleteOrderDetailsWithAlertDtls {
	
	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory
	.instance(AcademyIVRReqGetCompleteOrderDetailsWithAlertDtls.class);
	
	public Document reqWebServiceWithOrderNo(YFSEnvironment env, Document inXML) throws Exception
	{
		Document getCompleteOrderDetailsOutDoc = AcademyUtil.invokeService(env, "AcademyIVRReqGetCompleteOrderDetails", inXML);
		
		log.verbose("getCompleteOrderDetailsOutDoc:: "+ XMLUtil.getXMLString(getCompleteOrderDetailsOutDoc));
		
		Element rootEle = (Element) inXML.getDocumentElement();
		
		String orderNo = rootEle.getAttribute("OrderNo");
		String enterpriseCode = rootEle.getAttribute("EnterpriseCode");
		String documentType =rootEle.getAttribute("DocumentType");
		
		Document getExpListForOrderInDoc = XMLUtil.createDocument("Inbox");
		
		getExpListForOrderInDoc.getDocumentElement().setAttribute("OrderNo", orderNo);
		getExpListForOrderInDoc.getDocumentElement().setAttribute("EnterpriseCode", enterpriseCode);
		getExpListForOrderInDoc.getDocumentElement().setAttribute("DocumentType", documentType);
		
		Element orderByElement = XMLUtil.createElement(getExpListForOrderInDoc, "OrderBy", false);
		Element attributeElement = XMLUtil.createElement(getExpListForOrderInDoc, "Attribute", true);
		
		attributeElement.setAttribute("Name", "ExceptionType");
		
		orderByElement.appendChild(attributeElement);
		getExpListForOrderInDoc.getDocumentElement().appendChild(orderByElement);
			
		log.verbose("getExpListForOrderInDoc:: "+ XMLUtil.getXMLString(getExpListForOrderInDoc));
		
		Document getExpListForOrderOutDoc = AcademyUtil.invokeService(env, "AcademyIVRGetExceptionListForOrder", getExpListForOrderInDoc);
		
		log.verbose("getExpListForOrderOutDoc:: "+ XMLUtil.getXMLString(getExpListForOrderOutDoc));
		
		Element getExpListElement = getExpListForOrderOutDoc.getDocumentElement();
		
		Node importEle = getCompleteOrderDetailsOutDoc.importNode(getExpListElement, true);
		
		getCompleteOrderDetailsOutDoc.getDocumentElement().appendChild(importEle);
		log.verbose("getCompleteOrderDetailsOutDoc with Alert Details:: "+ XMLUtil.getXMLString(getCompleteOrderDetailsOutDoc));
		
		return getCompleteOrderDetailsOutDoc;
		
	}

}
