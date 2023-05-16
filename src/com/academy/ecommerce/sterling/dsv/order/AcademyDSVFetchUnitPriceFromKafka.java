package com.academy.ecommerce.sterling.dsv.order;

import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyProcessKafkaWebserviceUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.ibm.sterling.afc.jsonutil.PLTJSONUtils;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**#########################################################################################
 *
 * Project Name                : DSV
 * Author                      : Fulfillment POD
 * Author Group				  : DSV
 * Date                        : 26-FEB-2023 
 * Description				  : This class fetches the Unit Price from Firestore DB via Kafka and 
 * 								updates the same as part of the OrderLine
 * 								 
 * ---------------------------------------------------------------------------------
 * Date            	Author         		Version#       		Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 27-Feb-2023		Everest  	 		1.0           		Initial version
 *
 * #########################################################################################*/

public class AcademyDSVFetchUnitPriceFromKafka
{

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyDSVFetchUnitPriceFromKafka.class);
	AcademyProcessKafkaWebserviceUtil acadKafkaWebserviceUtil = new AcademyProcessKafkaWebserviceUtil();

	/**
	 * @param env
	 * @param InXml
	 * @return Document
	 * @throws Exception
	 * 
	 * Fetch the order details from the input xml and invoke a REST API 
	 * call to Kafka. Based on the successful response update the details 
	 * on the OrderLine
	 * 
	 */ 
	public Document fetchAndUpdateUnitPriceOnOrderLine(YFSEnvironment env, Document inDoc) throws Exception
	{
		log.beginTimer("AcademyDSVFetchUnitPriceFromKafka.fetchAndUpdateUnitPriceOnOrderLine method ");
		log.debug("Input XML for DSV Order ................."+ XMLUtil.getXMLString(inDoc));
		try {
			if(!checkIfUnitPriceIsAlreadyUpdatedOnOrderLine(inDoc)) {
				log.verbose("Unit Price needs to be invoked from Kafka :: ");

				String strOrderNo = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO);

				Document docResponse = acadKafkaWebserviceUtil.invokeKafkaWebserviceApi(env, "academy.kafka.unitcost", 
						prepareSupplierUpdateJson(inDoc), "KafkaUnitCost");

				String strStatus = docResponse.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS);
				String strResponseCode = docResponse.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_CODE);
				String strResponseMessage = docResponse.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_MESSAGE);

				//Success Response for Token 
				if (!YFCObject.isVoid(strStatus) && AcademyConstants.STR_SUCCESS.equals(strStatus)) {
					log.verbose("strResponse String = " + strStatus + " : Response Code as "+strResponseCode
							+ " : Response Message as "+strResponseMessage);
					//Update the UnitCost At Orderline level.
					translateResponseAndUpdateOrderLine(env, inDoc, docResponse);

				}
				else if(!YFCObject.isVoid(strStatus) && (AcademyConstants.STR_RETRY.equals(strStatus)
						|| AcademyConstants.STATUS_CODE_ERROR.equals(strStatus))) {
					//Creating info logger for Splunk alerts 
					log.info("Kafka UnitCost Error :: Error While trying update UnitCost. OrderNo:: " + strOrderNo +" ResponseCode :: " 
							+ strResponseCode + " :: and ResponseMessage :: " + strResponseMessage
							+ " :: and OMS Configured Exception handling is :: " + strStatus);
				}
				else {
					//Creating info logger for Splunk alerts 
					log.info("Kafka UnitCost Error :: Error While trying update UnitCost. OrderNo:: " + strOrderNo +" ResponseCode :: " 
							+ strResponseCode + " :: and ResponseMessage :: " + strResponseMessage
							+ " :: and Exception Type not in COP :: " + strStatus);
				}			
			}
		}
		catch (YFSException yfsEx) {
			log.info("Kafka UnitCost Error :: Exception while invoking UnitCost.YFSException Handling. OrderNo ::" + 
					inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO)	
					+ " . Error Code  :: "+yfsEx.getErrorCode() + " . Error Desc  :: "+yfsEx.getErrorDescription());
		}
		catch (Exception exp) {
			//Creating info logger for Splunk alerts 
			log.info("Kafka UnitCost Error :: Exception while invoking UnitCost. OrderNo ::" + 
					inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO)	+ " . Error Trace :: "+exp.getStackTrace());
		}

		log.endTimer("AcademyDSVFetchUnitPriceFromKafka.fetchAndUpdateUnitPriceOnOrderLine method ");	
		return inDoc;
	}


	/**
	 * @param InXml
	 * @return Document
	 * @throws Exception
	 * 
	 * Fetch the order details from the input xml and invoke a REST API 
	 * call to Kafka. Based on the successful response update the details 
	 * on the OrderLine
	 * 
	 */ 
	private boolean checkIfUnitPriceIsAlreadyUpdatedOnOrderLine(Document inDoc) throws Exception
	{
		log.beginTimer("AcademyDSVFetchUnitPriceFromKafka.checkIfUnitPriceIsAlreadyUpdatedOnOrderLine method ");
		log.debug("Input XML for checkIfUnitPriceIsAlreadyUpdatedOnOrderLine ...."+ XMLUtil.getXMLString(inDoc));

		NodeList nlOrderLineUnitPrice = XPathUtil.getNodeList(inDoc, "/Order/OrderLines/OrderLine/References/Reference[@Name='VendorUnitCost']");

		log.verbose("nodeListLength :: " + nlOrderLineUnitPrice.getLength());

		if(nlOrderLineUnitPrice.getLength() > 0) {
			return true;
		}
		log.endTimer("AcademyDSVFetchUnitPriceFromKafka.fetchAndUpdateUnitPriceOnOrderLine method ");	
		return false;
	}


	/**
	 * This method is creates a webservice for the UnitCost to Kafka
	 * 
	 * @param env
	 * @return shipmentConfDoc
	 * @throws Exception
	 */
	private String prepareSupplierUpdateJson(Document inDoc) throws Exception {
		log.beginTimer("AcademyDSVFetchUnitPriceFromKafka.prepareSupplierUpdateJson method ");
		JSONArray jsonRecords = new JSONArray();

		try {

			NodeList nlOrderLine = XPathUtil.getNodeList(inDoc, AcademyConstants.XPATH_ORDERLINES_LINE);
			for(int iOl=0; iOl < nlOrderLine.getLength(); iOl++) {

				Element eleOrderLine = (Element) nlOrderLine.item(iOl);
				Element eleItem = SCXmlUtil.getChildElement(eleOrderLine, AcademyConstants.ELEM_ITEM);

				JSONObject jsonItems = new JSONObject();

				jsonItems.put(AcademyConstants.STR_ITEM_ID, eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID)); //Contains a Unique Random Value
				jsonItems.put("supplierId", eleOrderLine.getAttribute(AcademyConstants.ATTR_SHIP_NODE));
				jsonRecords.add(jsonItems);
			}

			log.verbose("Final JSON to Kafka--->" + jsonRecords.toString());			
		}
		catch (YFSException yfsEx) {
			throw yfsEx;
		}
		catch (Exception exp) {
			//Creating info logger for Splunk alerts 
			log.info("Kafka UnitCost Error :: Exception while preparing UnitCost input. OrderNo ::" + 
					inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO)	+ " . Error Trace :: "+exp.getStackTrace());

		}
		log.endTimer("AcademyDSVFetchUnitPriceFromKafka.prepareSupplierUpdateJson method ");
		return jsonRecords.toString();
	}


	/**
	 * This method is creates a webservice for the UnitCost to Kafka
	 * 
	 * @param env
	 * @return shipmentConfDoc
	 * @throws Exception
	 */
	private void translateResponseAndUpdateOrderLine(YFSEnvironment env, Document inDoc, Document docResponse) throws Exception {
		log.beginTimer("AcademyDSVFetchUnitPriceFromKafka.translateResponseAndUpdateOrderLine method ");

		try {

			String strJsonResp = docResponse.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_MESSAGE);
			if(strJsonResp.startsWith("[")) {
				strJsonResp = "{ \"OrderLine\" :" + strJsonResp + "}" ;
			}

			Document docKafkaResponse = PLTJSONUtils.getXmlFromJSON(strJsonResp, "KafkaResponse");
			log.verbose("The Converted Response is : " + SCXmlUtil.getString(docKafkaResponse));
			Element eleOrder = inDoc.getDocumentElement();			

			//Create changeOrder Input
			Document docChangeOrder = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			Element eleChangeOrder = docChangeOrder.getDocumentElement();
			eleChangeOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, 
					eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
			eleChangeOrder.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);

			Element eleChangeOrderLines = docChangeOrder.createElement(AcademyConstants.ELE_ORDER_LINES);
			eleChangeOrder.appendChild(eleChangeOrderLines);

			String strErrorMessage =  XPathUtil.getString(docKafkaResponse, "./errorResponse/@message");
			String strErrorStatusCode =  XPathUtil.getString(docKafkaResponse, "./errorResponse/@statusCode");
			
			if(!YFCObject.isVoid(strErrorMessage) || !YFCObject.isVoid(strErrorStatusCode)) {
				log.info("Kafka UnitCost Error :: Error Message from Kafka. OrderNo ::" + 
						inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO)	+ 
						" : Error Response is : " + docResponse.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_MESSAGE)
						+ " . Error Code is " + strErrorStatusCode	+ " . Error Message is " + strErrorMessage);
			}

			NodeList nlOrderLine = XPathUtil.getNodeList(inDoc, AcademyConstants.XPATH_ORDERLINES_LINE);
			for(int iOl=0; iOl < nlOrderLine.getLength(); iOl++) {

				Element eleOrderLine = (Element) nlOrderLine.item(iOl);
				Element eleItem = SCXmlUtil.getChildElement(eleOrderLine, AcademyConstants.ELEM_ITEM);
				String strItemId = eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID);

				String strUnitPrice =  XPathUtil.getString(docKafkaResponse, "/KafkaResponse/OrderLine[@itemId='"+ strItemId +"']/@itemCost");
				log.verbose("strItemId :: strUnitPrice " + strItemId + " :: " + strUnitPrice);		

				if(!YFCObject.isVoid(strUnitPrice)) {
					Element eleChangeOrderLine = docChangeOrder.createElement(AcademyConstants.ELE_ORDER_LINE);
					eleChangeOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, 
							eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
					Element eleReferences = docChangeOrder.createElement(AcademyConstants.ELE_REFERENCES);
					Element eleReference = docChangeOrder.createElement(AcademyConstants.ELE_REFERENCE);
					eleReference.setAttribute(AcademyConstants.ATTR_NAME, "VendorUnitCost");
					eleReference.setAttribute(AcademyConstants.ATTR_VALUE, strUnitPrice);

					eleReferences.appendChild(eleReference);
					eleChangeOrderLine.appendChild(eleReferences);
					eleChangeOrderLines.appendChild(eleChangeOrderLine);
				}
			}

			log.verbose("Final ChangeOrder Input --->" + SCXmlUtil.getString(docChangeOrder));
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docChangeOrder);
		}
		catch (Exception exp) {
			//Creating info logger for Splunk alerts 
			log.info("Kafka UnitCost Error :: Exception while Traslating Response. OrderNo ::" + 
					inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO)	+ 
					" : Error Response is : " + docResponse.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_MESSAGE)
					+ " . Error Trace :: "+exp.getStackTrace());

		}
		log.endTimer("AcademyDSVFetchUnitPriceFromKafka.translateResponseAndUpdateOrderLine method ");
	}

}
