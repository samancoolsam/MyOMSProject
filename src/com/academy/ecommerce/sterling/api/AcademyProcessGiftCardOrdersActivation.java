package com.academy.ecommerce.sterling.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;



public class AcademyProcessGiftCardOrdersActivation extends YCPBaseTaskAgent {
	
	String firstName="",lastName="";
	private static YFCLogCategory log = YFCLogCategory
	.instance(AcademyProcessGiftCardOrdersActivation.class);
	public Document executeTask(YFSEnvironment env, Document agentXML)
			throws Exception {

		String taskQKey,shipmentType=null,shipmentNo,shipmentKey,shipmentStatus,baseDropStatus=AcademyConstants.STR_BASEDROP_STATUS_1600_002_51;
		Document outShipDoc,outGCDoc,outDoc, inXML, inShipDoc;
		Element ordEle, extnEle, rootElem, currTaskElem;
		boolean isBulkItems=false,isShipStatus=false;
		try {
			log.beginTimer(" Begining of AcademyProcessGiftCardOrdersActivation -> executeTask ()");
			taskQKey = agentXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_TASK_Q_KEY);
			
			shipmentKey=agentXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY);
			inShipDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			inShipDoc.getDocumentElement().setAttribute(AcademyConstants.SHIPMENT_KEY,
					agentXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY));
			
			env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_DETAILS, "global/template/api/getShipmentDetails.BulkGCActivation.xml");
			outDoc = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_SHIPMENT_DETAILS,inShipDoc);
			env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_DETAILS);
			
			Element shipmentEle=(Element)outDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
			shipmentType=shipmentEle.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
			shipmentNo=shipmentEle.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);			
			
			if(shipmentType.equalsIgnoreCase(AcademyConstants.STR_GC_SHIP_TYPE) || shipmentType.equalsIgnoreCase(AcademyConstants.STR_GC_ONLY_SHIP_TYPE))
				{
			NodeList shipLineList=outDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
					
					for(int j=0;j<shipLineList.getLength();j++)
					{
						Element shipLineEle=(Element)shipLineList.item(j);
						if(checkGCItems(env,shipLineEle.getAttribute(AcademyConstants.ITEM_ID)))
						{
						//getCommonCode List Api to get the Qty
						Document docGetReasonCodesInput = XMLUtil
						.createDocument(AcademyConstants.ELE_COMMON_CODE);
				docGetReasonCodesInput.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_CODE_TYPE,
						"GET_GC_QTY");
				Document docCommonCodeListOutput = AcademyUtil.invokeAPI(env,
						AcademyConstants.API_GET_COMMON_CODELIST,
						docGetReasonCodesInput);
				String gcQty=((Element)docCommonCodeListOutput.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE).item(0)).getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
				//check for bulkGC items.
			
						if(Double.parseDouble(shipLineEle.getAttribute(AcademyConstants.ATTR_QUANTITY))>=Double.parseDouble(gcQty))
						{
							isBulkItems=true;
							}
						else{
							//Do GC Activation and raise an alert if any failures forming InputXML for Non-Bulk GC Activation.
							Element linePriceInfoEle=(Element)shipLineEle.getElementsByTagName(AcademyConstants.ELE_LINEPRICE_INFO).item(0);
							Document rt_ShipmentLine = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT_LINE);
							Element el_ShipmentLine=rt_ShipmentLine.getDocumentElement();
							el_ShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY,shipLineEle.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
							el_ShipmentLine.setAttribute("UnitPrice",linePriceInfoEle.getAttribute(AcademyConstants.ATTR_UNIT_PRICE));
							AcademyUtil.invokeService(env,
									"AcademyGCLoadActivationProcess",rt_ShipmentLine);
							//AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_SHIPMENT, outDoc);
						}
						}
						
			 
					}
							
			if(isBulkItems)
			{
				// Start - Fix for # 3968 as part of R027
				if(outDoc.getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).getLength() > 0){
					firstName=((Element)outDoc.getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).item(0)).getAttribute(AcademyConstants.ATTR_FNAME);
					lastName=((Element)outDoc.getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).item(0)).getAttribute(AcademyConstants.ATTR_LNAME);
				}
				// End - Fix for # 3968 as part of R027
							sendGCActivationCodeForBulkGCItems(env,outDoc,shipmentEle,shipmentKey,shipmentNo);
							baseDropStatus=AcademyConstants.STR_BASEDROP_STATUS_1600_002_52;
			}
			Document changeShipmentStatusDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			changeShipmentStatusDoc.getDocumentElement().setAttribute(AcademyConstants.SHIPMENT_KEY,
					agentXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY));
			changeShipmentStatusDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS,baseDropStatus);
			changeShipmentStatusDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRANSID, AcademyConstants.ATTR_CHANGE_SHIPMENT_TRANS_ID);
		
			AcademyUtil.invokeAPI(env,AcademyConstants.CHANGE_SHIPMENT_STATUS_API, changeShipmentStatusDoc);
				}
			// Completing Register process by calling RegisterProcess Api()
			inXML = XMLUtil
					.createDocument(AcademyConstants.ELE_REG_PROCESS_COMP_INPUT);

			rootElem = inXML.getDocumentElement();

			rootElem.setAttribute(AcademyConstants.ATTR_KEEP_TASK_OPEN, AcademyConstants.STR_NO);

			currTaskElem = inXML.createElement(AcademyConstants.ELE_CURR_TASK);

			currTaskElem.setAttribute(AcademyConstants.ATTR_TASK_Q_KEY,
					taskQKey);

			rootElem.appendChild(currTaskElem);

			AcademyUtil.invokeAPI(env,
					AcademyConstants.API_REGISTER_PROCESS_COMPLETION, inXML);
			log.endTimer(" End of AcademyProcessGiftCardOrdersActivation -> executeTask ()");

		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

		return agentXML;
	}

	private boolean checkGCItems(YFSEnvironment env,String itemID)throws Exception
	{	log.beginTimer(" Begining of AcademyProcessGiftCardOrdersActivation -> checkGCItems ()");	
		Document rt_Item =  XMLUtil.createDocument(AcademyConstants.ITEM);
					Element el_Item=rt_Item.getDocumentElement();
					Element el_Extn=rt_Item.createElement(AcademyConstants.ELE_EXTN);
					el_Item.appendChild(el_Extn);
					el_Extn.setAttribute(AcademyConstants.ATTR_EXTN_IS_GIFT_CARD,AcademyConstants.STR_YES);

					Element el_ComplexQuery=rt_Item.createElement("ComplexQuery");
					el_Item.appendChild(el_ComplexQuery);
					el_ComplexQuery.setAttribute("Operator","AND");

					Element el_Or=rt_Item.createElement("Or");
					el_ComplexQuery.appendChild(el_Or);

					Element el_Exp=rt_Item.createElement("Exp");
					el_Or.appendChild(el_Exp);
					el_Exp.setAttribute(AcademyConstants.ATTR_NAME,AcademyConstants.ITEM_ID);
					el_Exp.setAttribute(AcademyConstants.ATTR_VALUE,itemID);
					
					Document outItemListDoc=AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_ITEM_LIST, rt_Item);
					if (!YFCObject.isVoid(outItemListDoc)){
					//String TotalRecords = outItemListDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS);
					Element itemEle = (Element)outItemListDoc.getElementsByTagName(AcademyConstants.ITEM).item(0);
					log.endTimer(" Ending of AcademyProcessGiftCardOrdersActivation -> checkGCItems ()");
					if ( !YFCObject.isVoid(itemEle)){
					return true;
											}
										}
		  return false;
	}
	
	private void sendGCActivationCodeForBulkGCItems(YFSEnvironment env,Document outDoc,Element shipmentEle,String shipmentKey,String shipmentNo)throws Exception
	{
		log.beginTimer(" Begining of AcademyProcessGiftCardOrdersActivation -> sendGCActivationCodeForBulkGCItems ()");
		//generate GC Activation Code and send an mail to customer.
		Document inputShipDoc=XMLUtil.getDocumentForElement(shipmentEle);
		
		Document outGCActDoc = AcademyUtil.invokeService(env,
				AcademyConstants.ACADEMY_GC_ACTIVATION_SERVICE,inputShipDoc);
		 Element ordEle = (Element) outDoc.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
		outGCActDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_NO,
				ordEle.getAttribute(AcademyConstants.ATTR_ORDER_NO));
		outGCActDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO,shipmentNo);
		outGCActDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_FNAME,this.firstName);
		outGCActDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_LNAME,this.lastName);
		outGCActDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID,
				ordEle.getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID));

		String gcActivationCode = outGCActDoc.getDocumentElement().getAttribute(
				AcademyConstants.STR_GC_ACTIVATION_CODE);
		
		//Start WN-697 : Sterling to consume special characters and include them in customer-facing emails, but to remove them before settlement.				
		AcademyUtil.convertUnicodeToSpecialChar(env, outGCActDoc.getDocumentElement(), null, false);
		//End WN-697 : Sterling to consume special characters and include them in customer-facing emails, but to remove them before settlement.
		
// invoke service to send an Mail to Customer
		AcademyUtil.invokeService(env,
				AcademyConstants.ACADEMY_GC_ACTIVATION_MAIL_SERVICE,outGCActDoc);

		// calling changeShipment to store Activation code

		Document rt_Shipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element el_Shipment=rt_Shipment.getDocumentElement();
		el_Shipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,shipmentKey);
		Element el_Extn=rt_Shipment.createElement(AcademyConstants.ELE_EXTN);
		el_Shipment.appendChild(el_Extn);
		el_Extn.setAttribute(AcademyConstants.ATTR_EXTN_BULK_GCACT_STATUS,AcademyConstants.STR_ACTIVATION_CODE_SENT);
		el_Extn.setAttribute(AcademyConstants.ATTR_EXTN_GCACT_CODE,gcActivationCode);
		AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_SHIPMENT, rt_Shipment);
		log.endTimer(" End of AcademyProcessGiftCardOrdersActivation -> sendGCActivationCodeForBulkGCItems ()");	}
	
}
