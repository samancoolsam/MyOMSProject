package com.academy.ecommerce.sterling.dsv.shipment;

import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * This class will be invoked on On_Cancell Even of the changeRelease
 * 
 * 
 * @author vishwanatha Neelakantappa (C0008434)
 **/

/*
 * ON_CANCEL Service:AcademyDSVSOCancel is Called.  
 * From Data published - YFS_ORDER_RELEASE_CHANGE.ON_CANCEL.xml OrderNo retrived 
 * CompleteOrderDetails API Call is made with Template : 
 * <Order OrderNo= " ">
 *	<OrderLines> 
 *		<OrderLine OrderLineKey= " "> 
 *		<Item ItemID= " "/>
 *	</OrderLine> 
 *	</OrderLines>
 *</Order> get the SO Order Line for the Cancellation
 *Document  docOrderInputForCancel is formed
 *	<?xml  version="1.0" encoding="UTF-8"?>	
 *	<Order DocumentType=" " EnterpriseCode=" "   OrderNo=" " Override="Y" ReasonCode="">
 *		<OrderLines>
 *			<OrderLine  Action="MODIFY" OrderLineKey=" " OrderedQty=""/>
 *			<OrderLine  Action="MODIFY" OrderLineKey=" " OrderedQty=""/>
 *		</OrderLines>
 *	</Order>	 
 *	GetOrderRelease Api Call is made to get the Notes by Passing following Template:
 *
 *<OrderRelease  OrderReleaseKey="">
 *		<Notes>
 *			<Note ReasonCode=""/>
 *		</Notes>
 *	</OrderRelease>
 *	
 * ReasonCode from the GetRelease Details is passed in to docOrderInputForCancel at the Order Level
 * @param env @param inDoc @return docOrderInputForCancel with new
 * attributes/vaules to Change order API
 * 
 * @throws Exeception (General)
 */
public class AcademyDSVSOCancel implements YIFCustomApi {

	private static Logger log = Logger.getLogger(AcademyDSVSOCancel.class
			.getName());

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}

	public Document dsvSOCancellation(YFSEnvironment env, Document inDoc)
			throws Exception {

		Document docOrderInputForCancel = null;
		Element eleOrderLines = null;
		Document docInGetCompleteOrderDetails = null;
		Document docOutGetCompleteOrderDetails = null;
		Element eleOrderLine = null;
		Document docGetReleaseForNotes = null;
		Element eleitemIDinDOC = null;
		String strInItemID = null;

		log.verbose("Input document -->" + XMLUtil.getXMLString(inDoc));

		try {
			
			Document outputTemplate = XMLUtil
					.getDocument(" <Order OrderNo=\"\">" + "<OrderLines>"
							+ "<OrderLine OrderLineKey=\"\">"
							+ "<Item ItemID=\"\"/>" + "</OrderLine>"
							+ "</OrderLines>" + "</Order>");
			Element eleOrdLines = inDoc.getDocumentElement();
			String strorderNoInDoc = eleOrdLines
					.getAttribute(AcademyConstants.ATTR_ORDER_NAME);
			String strEnterpriseCode = eleOrdLines
					.getAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE);
			docInGetCompleteOrderDetails = XMLUtil
					.createDocument(AcademyConstants.ELE_ORDER);
			docInGetCompleteOrderDetails.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_ORDER_NO, strorderNoInDoc);
			docInGetCompleteOrderDetails.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_DOC_TYPE,
					AcademyConstants.SALES_DOCUMENT_TYPE);
			docInGetCompleteOrderDetails.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_ENTERPRISE_CODE, strEnterpriseCode);
			env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
					outputTemplate);
			/* invoke getCompleteOrderDetails API */
			docOutGetCompleteOrderDetails = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
					docInGetCompleteOrderDetails);
			// Clear API template
			env
					.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS);
			log.verbose("getCompleteOrderDetails API Output document: -----> "
					+ XMLUtil.getXMLString(docOutGetCompleteOrderDetails)
					+ "<--------");

			NodeList nListOrderLineInDoc = XMLUtil.getNodeList(inDoc,
					"/Order/OrderLines/OrderLine");
			int iNoEleOrLnInDoc = nListOrderLineInDoc.getLength();
			/** Form the XML for the Cancellation * */
			docOrderInputForCancel = XMLUtil
					.createDocument(AcademyConstants.ELE_ORDER);
			docOrderInputForCancel.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_ORDER_NO, strorderNoInDoc);
			docOrderInputForCancel.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_DOC_TYPE,
					AcademyConstants.SALES_DOCUMENT_TYPE);
			docOrderInputForCancel.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_ENTERPRISE_CODE, strEnterpriseCode);
			docOrderInputForCancel.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_REASON_CODE, "ReasonCode");
			docOrderInputForCancel.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_OVERRIDE, AcademyConstants.ATTR_Y);
			/* Created OrderLines */
			eleOrderLines = docOrderInputForCancel
					.createElement(AcademyConstants.ELEM_ORDER_LINES);
			XMLUtil.appendChild(docOrderInputForCancel.getDocumentElement(),
					eleOrderLines);
			String eleOrderedQty;
			for (int i = 0; i < iNoEleOrLnInDoc; i++) {
				Element elementOrderLineInDoc = (Element) nListOrderLineInDoc
						.item(i);
				/* To Get the OrderedQty from InDoc */
				eleOrderedQty = elementOrderLineInDoc
						.getAttribute("OrderedQty");
				log.verbose("OrderQty  From InDOC eleOrderedQty ----->"
						+ eleOrderedQty + "------->");
				eleOrderLine = docOrderInputForCancel
						.createElement(AcademyConstants.ELEM_ORDER_LINE);
				/* Set Action To Modify */
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ACTION,
						AcademyConstants.STR_ACTION_MODIFY_UPPR);
				/* Set the OrderQty from Indoc */
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY,
						eleOrderedQty);
				/*
				 * Get the ItemID from InDOC loop through the CompletOrder
				 * Details to fet the OrderLineKey
				 */
				eleitemIDinDOC = (Element) elementOrderLineInDoc
						.getElementsByTagName("Item").item(0);
				strInItemID = eleitemIDinDOC.getAttribute("ItemID");
				/*From the Get Order Complete Detaild Output Doc fetch ordeLinekey based on the ITEM_ID from inDoc#*/
				Node eleGetComOrderItem = XMLUtil.getNode(
						docOutGetCompleteOrderDetails.getDocumentElement(),
						"/Order/OrderLines/OrderLine/Item[@ItemID='"
								+ strInItemID + "']");
				Element eleGetComOrderOL = (Element) eleGetComOrderItem
						.getParentNode();

				String strOrLnInCmpDTL = eleGetComOrderOL
						.getAttribute("OrderLineKey");

				eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,
						strOrLnInCmpDTL);

				XMLUtil.importElement(eleOrderLines, eleOrderLine);
   
			}
			/* GetOrderReleaseDetails*------> */
			Element orderLinesEle = (Element) inDoc.getElementsByTagName(
					"OrderLines").item(0);
			String strorderReleaseKey = XMLUtil
					.getString(orderLinesEle,
							"OrderLine/StatusBreakupForCanceledQty/CanceledFrom/@OrderReleaseKey");
			docGetReleaseForNotes = XMLUtil
					.createDocument(AcademyConstants.ELE_RELEASE_DTL);
			docGetReleaseForNotes.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_DOC_TYPE,
					AcademyConstants.DOCUMENT_TYPE_PO);
			docGetReleaseForNotes.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_ENTERPRISE_CODE, strEnterpriseCode);
			docGetReleaseForNotes.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_RELEASE_KEY, strorderReleaseKey);
			Document outputTemplateRelease = XMLUtil
					.getDocument("<OrderRelease  OrderReleaseKey=\"\">"
							+ "<Notes>" + "<Note ReasonCode=\"\"/>"
							+ "</Notes>" + "</OrderRelease>");
			env.setApiTemplate(AcademyConstants.API_GET_ORDER_RELEASE_DETAILS,
					outputTemplateRelease);
			Document docOutGetOrderReleaseDetails = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_ORDER_RELEASE_DETAILS,
					docGetReleaseForNotes);
			env
					.clearApiTemplate(AcademyConstants.API_GET_ORDER_RELEASE_DETAILS);
			log.verbose("getOrderReleaseDetails API Output document: -----> "
					+ XMLUtil.getXMLString(docOutGetOrderReleaseDetails)
					+ "<-------");
			String strnotesReasonCode = null;
			NodeList nListNotes = XMLUtil.getNodeList(
					docOutGetOrderReleaseDetails, "/OrderRelease/Notes/Note");
			int iNoOfNotesLine = nListNotes.getLength();
			Element elmReleaseNotes = (Element) nListNotes
					.item(iNoOfNotesLine - 1);
			strnotesReasonCode = elmReleaseNotes
					.getAttribute(AcademyConstants.ATTR_REASON_CODE);
			docOrderInputForCancel.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_MOD_REASON_CODE, strnotesReasonCode);
			log.verbose("Input document to getCompleteOrderDetails API ------>"
					+ XMLUtil.getElementXMLString(docOrderInputForCancel
							.getDocumentElement()));
		} catch (RuntimeException e) {
			log.verbose("RuntimeExeption thrown in method: dsvSOCancellation");
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		} catch (ParserConfigurationException e) {
			log
					.verbose("ParserConfiguationExeption thrown in method: dsvSOCancellation");
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

		return docOrderInputForCancel;
	}

}
