package com.academy.ecommerce.sterling.shipment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

public class AcademyStampShipmentTypes implements YIFCustomApi  {
	
	   private Properties props;
	  public void setProperties(Properties props) {
	        this.props = props;
	    }
		
   /**
   * Instance of logger
   */
  private static YFCLogCategory log = YFCLogCategory.instance(AcademyStampShipmentTypes.class);
  
  public Document stampShipmentTypes(YFSEnvironment env, Document inDoc) throws Exception {
	  
	  //Logic is to call getItemDetails for first shipment line item. 
	  String sItemId = "";
	  String sUnitOfMeasure ="";
	  String sEnterpriseCode = "";
	  String sStorageType = "";
	  String sKitCode = "";
	  String sIsGift = "";
	  String sWhiteGlove="";
	  String sItemType = "";
	  String sCarrierCode = "";
	  String sScac = "";
	  Boolean bOvernight = false;
	  String orderNo = null;
	  String sOrderHeaderKey = null;
	  String sDeliveryMethod = null;
	  //String sScacAndService = "";
	  //Start - STL-731 Changes - If LineType for an OrderLine is AMMO then stamp the ShipmentType as AMMO. 
	  String sLineType = "";
	  //End - STL-731 Changes - If LineType for an OrderLine is AMMO then stamp the ShipmentType as AMMO.
	  log.beginTimer(" Beginning of AcademyStampShipmentTypes-> stampShipmentTypes() Api");
	  try{
		  Element inDocElem = inDoc.getDocumentElement();
		  log.verbose("Input to StampShipmentTypes :: " + XMLUtil.getXMLString(inDoc));
		  sDeliveryMethod = inDocElem.getAttribute("DeliveryMethod");
		  if(!YFCObject.isVoid(inDocElem)){
			 inDocElem.setAttribute("FreightTerms", "PREPAID"); 
			 sCarrierCode = inDocElem.getAttribute("CarrierServiceCode"); 
			 sScac = inDocElem.getAttribute("SCAC"); 
			 
			 Element  inShipmentLines = XMLUtil.getFirstElementByName(inDocElem,AcademyConstants.ELE_SHIPMENT_LINES);
	  		 if(!YFCObject.isVoid(inShipmentLines)){
	  			NodeList nL1 = inShipmentLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
	  			int TotalNoOfLines = nL1.getLength();
	  			Element  inShipmentLine = XMLUtil.getFirstElementByName(inShipmentLines,AcademyConstants.ELE_SHIPMENT_LINE);
	  			
	  			if(!YFCObject.isVoid(inShipmentLine)){
	  				
	  				String sLineKey = inShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
	  				//if(!YFCObject.isVoid(sLineKey)){
	  				// As part of Clean up process, replace to invoke getOrderLineDetails API for each OrderLine with getOrderList API
	  				sOrderHeaderKey = inShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY); 
	  				orderNo = inShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_NO);
	  				if(!YFCObject.isVoid(sOrderHeaderKey)){
	  					Document ingetOrderList = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
	  					ingetOrderList.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, sOrderHeaderKey);
	  					//Start - STL-731 Changes - Added LineType in the output template.
	  					String outputTemp = "<OrderList><Order OrderHeaderKey='' OrderNo=''><OrderLines TotalNumberOfRecords=''>" +
	  							"<OrderLine KitCode='' OrderLineKey='' LineType=''><Item ItemID=''/></OrderLine></OrderLines></Order></OrderList>";
	  					//End - STL-731 Changes - Added LineType in the output template
	  					Document outputGetOrderList = YFCDocument.getDocumentFor(outputTemp).getDocument();
	  					env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, outputGetOrderList);
	  					Document outOrderListDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, ingetOrderList);
	  					env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
	  					/*Document ingetOrderlineList = XMLUtil.createDocument("OrderLineDetail");
	  	  	  			Element ingetOrderLineElem = ingetOrderlineList.getDocumentElement();
	  	  	  			
	  	  	  			ingetOrderLineElem.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,sLineKey );
	  	  	  			
	  	  	  			Document outOrderlineDoc = AcademyUtil.invokeService(env, "AcademyGetOrderLineDetails", ingetOrderlineList);*/
	  	  	  			
	  	  	  			if(!YFCObject.isVoid(outOrderListDoc) && 
		  	  	  					outOrderListDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_ORDER_LINE).getLength()>0){
		  	  	  			Element eOrder = (Element)XMLUtil.getNode(outOrderListDoc.getDocumentElement(), "Order");
		  	  	  			//sScacAndService = eOrderline.getAttribute("ScacAndService");
		  	  	  			Element eOrderline = (Element)XMLUtil.getNode(eOrder, "OrderLines/OrderLine[@OrderLineKey='"+sLineKey+"']");
			  	  	  		if(!YFSObject.isVoid(eOrderline)){
			  	  	  			Element eItem = XMLUtil.getFirstElementByName(eOrderline, AcademyConstants.ITEM); 	  	  			
			  	  	  			sKitCode = eOrderline.getAttribute("KitCode"); 	  	  			  	  				 	  				
			  	  	  			//eItem.getAttribute(AcademyConstants.ATTR_ITEM_ID);
			  	  	  			
			  	  	  			//Start - STL-731 Changes - Fetch LineType
			  	  	  			sLineType = eOrderline.getAttribute(AcademyConstants.ATTR_LINE_TYPE);
			  	  	  			//End - STL-731 Changes - fetch LineType
			  	  	  			
			  	  	  			Document getCommonCodeListInputXML = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			  	  		
			  	  	  			getCommonCodeListInputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, "WAVE_OVERNIGHT");
			  	  	  			getCommonCodeListInputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE, sCarrierCode);
			  	  	  			Document outCommonCodeListXML=AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_COMMONCODE_LIST, getCommonCodeListInputXML);
			  	  	  			if(outCommonCodeListXML!=null){
			  	  	  				NodeList outputList=outCommonCodeListXML.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
			  	  	  				if(outputList.getLength()!= 0){
			  	  	  					bOvernight = true;
			  	  	  				}
			  	  	  			}
			  	  				sEnterpriseCode = inDocElem.getAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE);
			  	  				sItemId = inShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID);
			  	  				
			  	  				sUnitOfMeasure = inShipmentLine.getAttribute(AcademyConstants.ATTR_UOM);
			  	  				
			  	  				/*Document inItemDoc = XMLUtil.createDocument(AcademyConstants.ITEM);
			  	  				Element inItemElem = inItemDoc.getDocumentElement();
			  	  				inItemElem.setAttribute(AcademyConstants.ORGANIZATION_CODE, sEnterpriseCode);
			  	  				inItemElem.setAttribute(AcademyConstants.ATTR_ITEM_ID, eItem.getAttribute(AcademyConstants.ATTR_ITEM_ID));
			  	  				inItemElem.setAttribute(AcademyConstants.ATTR_UOM,AcademyConstants.UNIT_OF_MEASURE);
			  	  				
			  	  				Document outItemDoc = AcademyUtil.invokeService(env, "AcademyGetItemDetails", inItemDoc);
			  	  				Element outItemElem = outItemDoc.getDocumentElement();*/
			  	  				
			  	  				Element outItemElem = getItemDetails(env, eItem.getAttribute(AcademyConstants.ATTR_ITEM_ID)); 	  				
			  	  				
			  	  				Element eExtn = XMLUtil.getFirstElementByName(outItemElem, AcademyConstants.ELE_EXTN);
			  	  				Element eClassificationCode = XMLUtil.getFirstElementByName(outItemElem, "ClassificationCodes");
			  	  				Element ePrimaryInformation = XMLUtil.getFirstElementByName(outItemElem, "PrimaryInformation");
			  	  			
			  	  				sItemType = ePrimaryInformation.getAttribute("ItemType");
			  	  				sStorageType = eClassificationCode.getAttribute("StorageType");
			  	  				
			  	  				sIsGift = eExtn.getAttribute(AcademyConstants.ATTR_EXTN_IS_GIFT_CARD);
			  	  			    sWhiteGlove = eExtn.getAttribute("ExtnWhiteGloveEligible");
			  	  				//OMNI-89597 and OMNI-89591 Start
			  	  				String strDocumentType = inDocElem.getAttribute(AcademyConstants.STR_DOCUMENT_TYPE);
								String strPackListType=inDocElem.getAttribute(AcademyConstants.STR_PACK_LIST_TYPE);
								log.verbose("the document type is:" +strDocumentType+  "The packlisttype is" +strPackListType);
                               
								//OMNI-89597 and OMNI-89591 End
			  	  				//This map contains storagetype - shipment type relation ship.
			  	  				HashMap hsMap = prepareMapforArguments();
			  	  				
								boolean isGCOShipment = false;
									//calling the GC method if they have multiple shipment lines and all Items are GC Items only
									if(TotalNoOfLines > 1){
										log.verbose("TotalNoOfLines > 1--> Checking only GC Items");
										isGCOShipment = checkOnlyGCItems(env,inShipmentLines,sEnterpriseCode, eOrder);
									}
								
			  	  				boolean isGCShipment = false;
			  	  				//calling the GC method only if there are multiple shipment lines - fix for #11484
			  	  				if(TotalNoOfLines > 1){
			  	  					isGCShipment = checkGCItems(env,inShipmentLines,sEnterpriseCode, eOrder);
			  	  				}
			  	  				//Start - STL-731 - If LineType of an OrderLine is AMMO then stamp the ShipmentType as AMMO
			  	  				if(AcademyConstants.AMMO_LINE_TYPE.equals(sLineType)) {
				  	  				log.verbose("ShipmentType stamped as AMMO");
									inDocElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.AMMO_SHIPMENT_TYPE);
			  	  				}
			  	  				//End - STL-731 - If LineType of an OrderLine is AMMO then stamp the ShipmentType as AMMO
			  	  				//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
			  	  				else if (AcademyConstants.HAZMAT_LINE_TYPE.equals(sLineType)) {
								// START OMNI-59925 adding sWhiteGlove=Y, WGHazmat items
									if(sWhiteGlove.equalsIgnoreCase("Y"))
									{
										log.verbose(AcademyConstants.ATTR_EXTN_IS_GIFT_CARD +"--> "+ sWhiteGlove);
										log.verbose(AcademyConstants.ATTR_LINE_TYPE+"--> "+ sLineType);
										log.verbose("ShipmentType stamped as WG");
										inDocElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.SHIPMENT_TYPE_WG);
									}else 
									{
										log.verbose("ShipmentType stamped as HAZMAT");
										inDocElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.HAZMAT_SHIPMENT_TYPE);
									}
			  	  				}
			  	  				//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
			  	  				//SOF :: Start WN-2036 : SO Changes 
			  	  				else if(AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equals(sLineType)) {
				  	  				log.verbose("Stamping ShipmentType as SOF");
									inDocElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.STR_SPECIAL_ORDER_FIREARMS);
									//OMNI-93544 - Start
									if(!YFCObject.isVoid(sDeliveryMethod) && sDeliveryMethod.equals("PICK"))
										env.setTxnObject("ISSOFEnabled", "TRUE");
									//OMNI-93544 - End
			  	  				}	
			  	  				//SOF :: End WN-2036 : SO Changes
			  	  				else {
			  	  					sLineType = getLineType( eExtn);

			  	  					log.verbose("StampShipmentTypes.stampShipmentTypes() sLineType : " + sLineType);

				  	  			    //Start - STL-1320: New Wave for Checkout Funnel OVNT Shipments
									/* Start - Changes made for STL-934 Checkout Funnel */
				  	  				/*if(AcademyConstants.ATTR_CSA_LINE_TYPE.equals(sLineType)){
				  	  				inDocElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.ATTR_CSA_SHIPMENT_TYPE);
				  	  				log.verbose("ShipmentType is stamped as CHKOUTFNL");
				  	  				}*/
				  	  			    /* End - Changes made for STL-934 Checkout Funnel */
				  	  				
									if(AcademyConstants.ATTR_CSA_LINE_TYPE.equals(sLineType)){
											if(bOvernight)
											{
												inDocElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.ATTR_CSA_OVNT_SHIPMENT_TYPE);
												log.verbose("ShipmentType is stamped as CSA for Checkout Funnel OVNT Shipment");
											}
											else
											{
												inDocElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.ATTR_CSA_SHIPMENT_TYPE);
												log.verbose("ShipmentType is stamped as CHKOUTFNL");
											}

										}
									//End - STL-1320: New Wave for Checkout Funnel OVNT Shipments
									
				  	  				//checking for GC item if there is only one Shipment Line
				  	  				else if(AcademyConstants.STR_YES.equals(sIsGift)&& TotalNoOfLines == 1){
										log.verbose("TotalNoOfLines = 1--> Checking only GC Items Stamping ST as GCO");
										inDocElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.STR_GC_ONLY_SHIP_TYPE);
									}
									else if (isGCOShipment){
										log.verbose("TotalNoOfLines > 1--> Checking only GC Items Stamping ST as GCO");
										inDocElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.STR_GC_ONLY_SHIP_TYPE);
									}
									else if(isGCShipment){
										log.verbose("TotalNoOfLines = 1--> Checking GC Items Stamping ST as GC");
										inDocElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.STR_GC_SHIP_TYPE);
									}
					  	  			/* * code commented since KIT Shipment Group has been discarded from design
									else if("BUNDLE".equals(sKitCode)){
					  	  				inDocElem.setAttribute("ShipmentType", "KIT");
					  	  			} */	
									//check for White Glove Item - Fix for CR# 38
				  	  	  			else if(AcademyConstants.STR_YES.equals(sWhiteGlove)){
				  	  	  				inDocElem.setAttribute("ShipmentType", "WG");
				  	  	  			}
				  	  				else if("NCONLP".equals(sStorageType) || "NCONNLP".equals(sStorageType) ){
				  	  					if ("NSANHV".equals(sItemType) || "NSAHV".equals(sItemType)) {
				  	  						//check whether the shipments belong to Overnight Carrier Service
				  	  						if(bOvernight){
				  	  	  						inDocElem.setAttribute("ShipmentType", "CONOVNT");
				  	  						}else{
				  	  							inDocElem.setAttribute("ShipmentType", "CON");
				  	  						}
										}else{
											if(bOvernight){
				  	  	  						inDocElem.setAttribute("ShipmentType", "BULKOVNT");
				  	  						}else{
				  	  							inDocElem.setAttribute("ShipmentType", (String) hsMap.get(sStorageType));
				  	  						}
										}			
				  	  				}
				  					else{ 
										//check whether the shipments belong to Overnight Carrier Service
				  						if(bOvernight){
											inDocElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, (String) hsMap.get(sStorageType)+"OVNT");
										}else{
											inDocElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, (String) hsMap.get(sStorageType));
										}
				  					}
				  	  	  		}
			  	  	  		}
		  	  			}
	  	  	  			log.verbose("outXML from  StampShipmentTypes :: " + XMLUtil.getXMLString(inDoc));
	  	  	  		  }
	  				}
	  			}
	  		 
	  		 // upgrade - downgrade code begin
	  		 
			Element eleExtn = inDoc.createElement("Extn");
			eleExtn.setAttribute("ExtnOriginalShipmentScac", inDocElem.getAttribute("SCAC"));
			//EFP-17  - Start
			String strCarrrierServiceCode = inDocElem.getAttribute("CarrierServiceCode");			
			if(YFCObject.isNull(strCarrrierServiceCode) || 
					YFCObject.isVoid(strCarrrierServiceCode)){
				
				strCarrrierServiceCode = inDocElem.getAttribute("RequestedCarrierServiceCode");
			}
			eleExtn.setAttribute("ExtnOriginalShipmentLos", strCarrrierServiceCode);
			//EFP-17  - End
			inDocElem.appendChild(eleExtn);
			
	  		log.verbose("***outside APO FPO check***" +env.getTxnObject("RemoveScac"));
  		 	Boolean str = false;
  		 	if(!YFCObject.isVoid(env.getTxnObject("RemoveScac")))
  		 	{
  		 		str = (Boolean)env.getTxnObject("RemoveScac");
  		 	}

  		 	if(str)
  		 	{
  		 		log.verbose("***inside APO FPO check***");
  		 		inDoc.getDocumentElement().setAttribute("SCAC", "");
  		 		inDoc.getDocumentElement().setAttribute("CarrierServiceCode", "");
				log.verbose("***inXML***" +XMLUtil.getXMLString(inDoc));
  		 	}
  		 	
	  		if(!"WG".equals(inDocElem.getAttribute("ShipmentType")) && !"GC".equals(inDocElem.getAttribute("ShipmentType")) && !"GCO".equals(inDocElem.getAttribute("ShipmentType")))
			{
	  			env.setTxnObject("RemoveScac", true);
				log.verbose("***inside WG check***");
				/*inDocElem.setAttribute("SCAC", "");
				inDocElem.setAttribute("CarrierServiceCode", "");*/
				log.verbose("outXML from  StampShipmentTypes :: " + XMLUtil.getXMLString(inDoc));
			}
	  		
	  		
	  		 	/*log.verbose("***outside APO FPO check***" +env.getTxnObject("RemoveScac"));
	  		 	Boolean str = false;
	  		 	if(!YFCObject.isVoid(env.getTxnObject("RemoveScac")))
	  		 	{
	  		 		str = (Boolean)env.getTxnObject("RemoveScac");
	  		 	}

	  		 	if(str)
	  		 	{
	  		 		log.verbose("***inside APO FPO check***");
	  		 		inDoc.getDocumentElement().setAttribute("SCAC", "");
	  		 		inDoc.getDocumentElement().setAttribute("CarrierServiceCode", "");
					log.verbose("***inXML***" +XMLUtil.getXMLString(inDoc));
	  		 	}*/
	  		// upgrade - downgrade code end
	  		 	
	  		}
	  }
	  catch (Exception e) {
		  e.printStackTrace();
		  if (e instanceof YFCException) {
			YFCException yfcEx = (YFCException) e;
			Document inputCreateException = XMLUtil.createDocument(AcademyConstants.ELE_INBOX);
			Element rootEle = inputCreateException.getDocumentElement();
			rootEle.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG, AcademyConstants.STR_YES);
			rootEle.setAttribute(AcademyConstants.ATTR_CONSOLIDATE, AcademyConstants.STR_YES);
			rootEle.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE, "Agent Exception");
			rootEle.setAttribute("ErrorReason", "Invalid Item");
			rootEle.setAttribute(AcademyConstants.ATTR_ORDER_NO, orderNo);
			rootEle.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, sOrderHeaderKey);
						
			Element eleConsolidateTempl = inputCreateException.createElement(AcademyConstants.ELE_CONSOLIDATE_TEMPLT);
			rootEle.appendChild(eleConsolidateTempl);
			
			Element eleInboxRefLst = inputCreateException.createElement(AcademyConstants.ELE_INBOX_REF_LIST);
			Element eleInboxRef = inputCreateException.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, "ApiName");
			eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, AcademyConstants.STR_EXCPTN_REF_VALUE);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, "AcademyConsolidateToShipment");
			eleInboxRefLst.appendChild(eleInboxRef);
			
			eleInboxRef = inputCreateException.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, "LoginId");
			eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, AcademyConstants.STR_EXCPTN_REF_VALUE);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, env.getProgId());
			eleInboxRefLst.appendChild(eleInboxRef);
			
			eleInboxRef = inputCreateException.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, "ERRORDESCRIPTION");
			eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, AcademyConstants.STR_EXCPTN_REF_VALUE);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, yfcEx.getAttribute(YFCException.ERROR_DESCRIPTION));
			eleInboxRefLst.appendChild(eleInboxRef);
			
			eleInboxRef = inputCreateException.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, "ERRORCODE");
			eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, AcademyConstants.STR_EXCPTN_REF_VALUE);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, yfcEx.getAttribute(YFCException.ERROR_CODE));
			eleInboxRefLst.appendChild(eleInboxRef);
			
			eleInboxRef = inputCreateException.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, "ERRORTYPE");
			eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, AcademyConstants.STR_EXCPTN_REF_VALUE);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, "NONREPROCESS");
			eleInboxRefLst.appendChild(eleInboxRef);
			
			rootEle.appendChild(eleInboxRefLst);
			log.verbose("Create exception : "+XMLUtil.getXMLString(inputCreateException));
			YIFApi yifApi;
			YFSEnvironment envNew;
			yifApi = YIFClientFactory.getInstance().getLocalApi();
			Document docEnv = XMLUtil.createDocument(AcademyConstants.ELE_ENV);
			docEnv.getDocumentElement().setAttribute(AcademyConstants.ATTR_USR_ID, env.getUserId());
			docEnv.getDocumentElement().setAttribute(AcademyConstants.ATTR_PROG_ID, env.getProgId());
			envNew = yifApi.createEnvironment(docEnv);
			yifApi.invoke(envNew, AcademyConstants.API_CREATE_EXCEPTION, inputCreateException);
			yifApi.releaseEnvironment(envNew);
		}
		  throw e;
	  }
	  log.endTimer(" End of AcademyStampShipmentTypes-> stampShipmentTypes() Api");
  		return inDoc;
  }

  private Element getItemDetails(YFSEnvironment env, String itemID) throws Exception{
	  Element eleItem = null;
	 Document inItemDoc = XMLUtil.createDocument(AcademyConstants.ITEM);
	  Element inItemElem = inItemDoc.getDocumentElement();
	  //inItemElem.setAttribute(AcademyConstants.ORGANIZATION_CODE, sEnterpriseCode);
	  inItemElem.setAttribute(AcademyConstants.ATTR_ITEM_ID, itemID);
	  inItemElem.setAttribute(AcademyConstants.ATTR_UOM,AcademyConstants.UNIT_OF_MEASURE);
	  String outputTemp = "<ItemList><Item ItemKey='' ItemID=''><PrimaryInformation ItemType=''/>" +
	  		"<ClassificationCodes StorageType=''/>" +
	  		"<Extn ExtnConveyable='' ExtnIsGiftCard='' ExtnKit='' ExtnLabelPick='' ExtnShipAlone='' ExtnWhiteGloveEligible=''/>" +
	  		"</Item></ItemList>";
	  Document outputGetOrderList = YFCDocument.getDocumentFor(outputTemp).getDocument();
	  env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, outputGetOrderList);
	  Document outItemDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST, inItemDoc);
	  env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
	  Element outItemElem = outItemDoc.getDocumentElement();
	  if(outItemDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ITEM).getLength()>0)
		  eleItem =  (Element)XMLUtil.getNode(outItemDoc.getDocumentElement(), "Item");
	  else{
		  YFCException yfsUEEx = new YFCException();
		  yfsUEEx.setAttribute(YFCException.ERROR_CODE, "EXTN_ACADEMY_12");
		  yfsUEEx.setAttribute(YFCException.ERROR_DESCRIPTION, "Invalid Item "+itemID+". It may be in held status or not existed");
		  yfsUEEx.setErrorDescription("Invalid Item "+itemID+". It may be in held status or not existed");
		  throw yfsUEEx;			  
	  }	  
	  return eleItem; 	  
  }
  
  private boolean checkGCItems(YFSEnvironment env,Element inShipmentLines, String entCode, Element eOrder) throws Exception
  {
	  log.beginTimer(" Begining of AcademyStampShipmentTypes-> checkGCItems() Api");
	  	log.verbose("Inside the checkGCItems()-> to check for GC Items");
	  	boolean isGiftCardShipment = false;
	  	NodeList nShipmentLines = inShipmentLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		int totalShipmentLines = nShipmentLines.getLength();
		for(int i=0;i<totalShipmentLines;i++)
		{
			Element shipLineEle= (Element)nShipmentLines.item(i);
			if(!YFCObject.isVoid(shipLineEle)){
  				
  				String sOrderLineKey = shipLineEle.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
  				if(!YFCObject.isVoid(sOrderLineKey)){
  					Element eleOrderline = (Element)XMLUtil.getNode(eOrder, "OrderLines/OrderLine[@OrderLineKey='"+sOrderLineKey+"']");
  					/*Document docGetOrderLineListInput = XMLUtil.createDocument("OrderLineDetail");
  	  	  			Element eleGetOrderLine = docGetOrderLineListInput.getDocumentElement();
  	  	  			
  	  	  			eleGetOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,sOrderLineKey);  	  	  			
  	  	  			Document outOrderlineDoc = AcademyUtil.invokeService(env, "AcademyGetOrderLineDetails", docGetOrderLineListInput);*/
  	  	  			if(!YFCObject.isVoid(eleOrderline)){
	  	  	  			//Element eleOrderline = outOrderlineDoc.getDocumentElement();	  	  	  			
	  	  	  			Element eleItem = XMLUtil.getFirstElementByName(eleOrderline, AcademyConstants.ITEM);
	  	  	  			
		  	  	  		/*Document inItemDoc = XMLUtil.createDocument(AcademyConstants.ITEM);
	  	  				Element inItemElem = inItemDoc.getDocumentElement();
	  	  				inItemElem.setAttribute(AcademyConstants.ORGANIZATION_CODE, entCode);
	  	  				inItemElem.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID));
	  	  				inItemElem.setAttribute(AcademyConstants.ATTR_UOM, AcademyConstants.UNIT_OF_MEASURE);
	  	  				
	  	  				Document outItemDoc = AcademyUtil.invokeService(env, "AcademyGetItemDetails", inItemDoc);*/
	  	  				
	  	  				Element outItemElem = getItemDetails(env, eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID));
	  	  				
	  	  				Element eleItemExtn = XMLUtil.getFirstElementByName(outItemElem, AcademyConstants.ELE_EXTN);
	  	  	  			String isGiftCard = eleItemExtn.getAttribute(AcademyConstants.ATTR_EXTN_IS_GIFT_CARD);
	  	  	  			if (AcademyConstants.STR_YES.equals(isGiftCard)){
		  	  	  			isGiftCardShipment = true;
		  	  	  			break;
	  	  	  			}	  	  	  		
  	  	  			}
  	  	  		}		
  			}
		}		  
		log.endTimer(" end of AcademyStampShipmentTypes-> checkGCItems() Api");
		return isGiftCardShipment;
  }
  
  private boolean checkOnlyGCItems(YFSEnvironment env, Element inShipmentLines, String enterpriseCode, Element eOrder) throws Exception {

		log.beginTimer(" Begining of AcademyStampShipmentTypes-> checkOnlyGCItems() Api");
		log.verbose("Inside the checkOnlyGCItems()-> to check for GC Items");
		boolean isGiftCardOnlyOShipments = true;
		NodeList nShipmentLines = inShipmentLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		int totalShipmentLines = nShipmentLines.getLength();
		log.verbose("Total Shipment Lines"+ totalShipmentLines);
		for(int i=0;i<totalShipmentLines;i++)
		{
			Element shipLineEle= (Element)nShipmentLines.item(i);
			if(!YFCObject.isVoid(shipLineEle)){

				log.verbose("Shipment line ELE is not null");
				String sOrderLineKey = shipLineEle.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
				if(!YFCObject.isVoid(sOrderLineKey)){

					log.verbose("OLK is not NULL");
					Element eleOrderline = (Element)XMLUtil.getNode(eOrder, "OrderLines/OrderLine[@OrderLineKey='"+sOrderLineKey+"']");
					if(!YFCObject.isVoid(eleOrderline)){

						log.verbose("Order Line ELE is not NULL");
						Element eleItem = XMLUtil.getFirstElementByName(eleOrderline, AcademyConstants.ITEM);
						Element outItemElem = getItemDetails(env, eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID));

						log.verbose("Item Element"+ XMLUtil.getElementXMLString(outItemElem));
						Element eleItemExtn = XMLUtil.getFirstElementByName(outItemElem, AcademyConstants.ELE_EXTN);
						String isGiftCard = eleItemExtn.getAttribute(AcademyConstants.ATTR_EXTN_IS_GIFT_CARD);
						log.verbose("IS GIFT CARD:::"+ isGiftCard);
						if (AcademyConstants.STR_YES.equals(isGiftCard)){
							log.verbose("IF condition");
							isGiftCardOnlyOShipments = true;
						}
						else{
							log.verbose("ELSE Condition");
							isGiftCardOnlyOShipments = false;
							break;
						}
					}
				}
			}
		}
		log.verbose("isGiftCardShipment :: "+ isGiftCardOnlyOShipments);
		log.endTimer(" end of AcademyStampShipmentTypes-> checkOnlyGCItems() Api");
		return isGiftCardOnlyOShipments;
	}
  
private HashMap prepareMapforArguments() {
	// TODO Auto-generated method stub
	HashMap<String, String> hsShimentType = new HashMap<String, String>();
	
	Set s = props.keySet();
	
	Iterator itr = s.iterator();
	while (itr.hasNext()){
		String str = (String)itr.next();
		
		StringTokenizer st = new StringTokenizer(props.getProperty(str),",");
		
		while (st.hasMoreTokens()){			
			hsShimentType.put(st.nextToken(),str);
		}
	}
	return hsShimentType;
}

private String getLineType(Element el) {
	
	log.verbose("Calling AcademyStampShipmentTypes.getLineType()");
	String sLineType = "";
	String strConveyable=el.getAttribute(AcademyConstants.ATTR_EXTN_CONVEYABLE);
	String strShipAlone=el.getAttribute(AcademyConstants.ATTR_EXTN_SHIP_ALONE);
	String strWhiteGlove=el.getAttribute(AcademyConstants.ATTR_EXTN_WHITE_GLOVE_ELIGIBLE);

	if((!YFCObject.isNull(strConveyable)&&AcademyConstants.STR_NO.equalsIgnoreCase(strConveyable))
			&&(!YFCObject.isNull(strShipAlone)&&AcademyConstants.STR_NO.equalsIgnoreCase(strShipAlone))&&
			    (!YFCObject.isNull(strShipAlone)&&AcademyConstants.STR_NO.equalsIgnoreCase(strWhiteGlove))){
		sLineType = AcademyConstants.ATTR_CSA_LINE_TYPE;
		}
	log.verbose("Passing AcademyStampShipmentTypes.getLineType() sLineType : " + sLineType);

	return sLineType;
}

}