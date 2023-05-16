package com.academy.ecommerce.sterling.order.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyBOPISUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSBeforeCreateOrderUE;
import com.academy.ecommerce.sterling.util.AcademyCommonCode;

public class AcademyBeforeCreateOrderUEImpl implements YFSBeforeCreateOrderUE {

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyBeforeCreateOrderUEImpl.class);
	
	//Start: OMNI-90416 : Separate BOPIS Shipment for FA and Non-FA
		private static Properties props;

		public void setProperties(Properties props) {
			this.props = props;
		}
	//End: OMNI-90416 : Separate BOPIS Shipment for FA and Non-FA
	/**
	 * return outDoc Returns the final document which is manipulated based on
	 * the input passed
	 * 
	 * This method would call the product (COM) services that needs to be called
	 * before order creation and then calls custom services which woould perform
	 * certain custom logic & stamp attributes
	 */
	public Document beforeCreateOrder(YFSEnvironment env, Document doc) throws YFSUserExitException 
	{
		Document outDoc = null;
		Document outYCDServiceOutput = null;
		//OMNI-7689
		String strDocumentType = null;
		//OMNI-7689
		
		
		
		try 
		{
			log.beginTimer(" Begining of AcademyBeforeCreateOrderUEImpl->beforeCreateOrder Api");
			log.verbose("********** iNPUT DOC - " + XMLUtil.getXMLString(doc));
			
			/*OMNI-25629: Start Change - Setting the transaction object  */
			env.setTxnObject("IsInvokedOnCreateOrder", true);
			log.debug("Setting env object for createOrder");
			/* OMNI-25629 : End Change*/
			
			//bug#11252 - Stamping Item Product Class as GOOD - Only for GC Refund Fulfillment Sales Order.
			String purpose = doc.getDocumentElement().getAttribute("OrderPurpose");
					
			if(purpose.equalsIgnoreCase("REFUND")) 
			{
				Document envDoc = (Document) AcademyUtil.removeContextObject(env, "AcadParentSalesOrderForRefund");
				YFCElement inDocEle = YFCDocument.getDocumentFor(doc).getDocumentElement();
				YFCElement referencesEle = inDocEle.getChildElement("References");
				YFCElement elementNotes = inDocEle.getChildElement(AcademyConstants.ELE_NOTES);
				
				if(!YFCObject.isVoid(envDoc)) 
				{
					log.verbose("^^^^^^^^^^^^^^^^^^^^^Inside beforeCreateOrder and envDoc : " + XMLUtil.getXMLString(envDoc));
					Element docElement = envDoc.getDocumentElement();
					
					if(!YFCObject.isVoid(docElement))
					{
						log.verbose("****** input doc element - " + XMLUtil.getElementXMLString(docElement));
						
						doc.getDocumentElement().setAttribute("BillToID", docElement.getAttribute("BillToID"));
						if(YFCObject.isVoid(referencesEle)) 
						{
							referencesEle = inDocEle.createChild("References");
						}
						
						YFCElement referenceEle = referencesEle.createChild("Reference");
						referenceEle.setAttribute("Name", "RefundParentOrderNo");
						referenceEle.setAttribute("Value", docElement.getAttribute("OrderNo"));
						
						referenceEle = referencesEle.createChild("Reference");
						referenceEle.setAttribute("Name", "RefundParentOrderHeaderKey");
						referenceEle.setAttribute("Value", docElement.getAttribute("OrderHeaderKey"));
						
						log.verbose("++++++++ References Element - " + referencesEle.toString());
						
						// FIX for Defect #3340 - When a Tax refund is issued for an order - there is no tracking available 
						// to tie refund order to the original order for which the refund is issued 
						if(YFCObject.isVoid(elementNotes)) 
						{
							elementNotes = inDocEle.createChild(AcademyConstants.ELE_NOTES);
						}
						
						YFCElement elementNote = elementNotes.createChild(AcademyConstants.ELE_NOTE);
						elementNote.setAttribute(AcademyConstants.ATTR_NOTE_TEXT, "The original Sales Order number is " + docElement.getAttribute("OrderNo"));

						log.verbose("---------- Notes Element - " + elementNotes.toString());			
											
					}
				}
				
//				Stamping Item Product Class as GOOD & ScacAndService as USPSStandard
				Element ordLineEle=(Element)doc.getElementsByTagName("OrderLine").item(0);
				ordLineEle.setAttribute("ScacAndService", "USPS-Letter - Priority");
				Element ItemEle=(Element)ordLineEle.getElementsByTagName("Item").item(0);
				ItemEle.setAttribute("ProductClass", "GOOD");
				
				// START: OMNI-5006 - Stamping Item ID as EGC Refund Item if EGC is enabled and
				// set the Fulfillment Type and LineType as EGC
				if (AcademyUtil.isEGCEnabled(env)) {
					ordLineEle.setAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE, AcademyConstants.STR_EGC);
					ordLineEle.setAttribute(AcademyConstants.ATTR_LINE_TYPE, AcademyConstants.STR_EGC);
					ordLineEle.setAttribute(AcademyConstants.ATTR_SHIP_NODE, AcademyConstants.STR_EGC_SHIP_NODE);
					String strRefundItemForEGC = getEGCRefundItem(env);
					ItemEle.setAttribute(AcademyConstants.ATTR_ITEM_ID, strRefundItemForEGC);
				}
				// END: OMNI-5006
				
				// Populate ShipToKey on OrderLine when the Order is of Refund
				// Take the ShipToKey from OrderHeader	
				ordLineEle.setAttribute("ShipToKey", doc.getDocumentElement().getAttribute("ShipToKey"));
			}
			/*
			Begin: OMNI-1755 :Cardinal Technical Cleanup
			
			//START : START : STL-1163
			modifyAuthoExpirationDate(env, doc);
			//END : START : STL-1163
			
			End: OMNI-1755 : Cardinal Technical Cleanup
			*/
			log.verbose("****************** Input Document :::::" + XMLUtil.getXMLString(doc));
			outYCDServiceOutput = AcademyUtil.invokeService(env, AcademyConstants.YCD_BEFORE_CREATE_ORDER_SERVICE, doc);
			
			log.verbose("****************** Output Document from YCD Service :::::" + XMLUtil.getXMLString(outYCDServiceOutput));
			
			/**
			 *  The below changes are as part of the code Clean up process. Replace the getItemDetails API with getItemList API
			 *  In case of exception scenarios,
			 *  	- Item ID is not passed in the input 
			 *  	- Item is not existed in the system
			 *  	- Item is in held (unpublished) status
			 *  getItemList API returns empty result. Therefore, should raise an alert and stop the process
			 */
			
			//OMNI-7689: Begin			
			strDocumentType = doc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DOC_TYPE);
			if(AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE.equals(strDocumentType)) {
				log.verbose("Document type is "+strDocumentType);

				doc = updateSalesOrderInfo(env, doc);
				
			}
			//OMNI-7689: End
			
			stampAttributesOnOrderLines(env, outYCDServiceOutput);
			//outDoc = AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_BEFORE_CREATE_ORDER_SERVICE, outYCDServiceOutput);
			
			log.endTimer(" End of AcademyBeforeCreateOrderUEImpl->beforeCreateOrder Api");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			if(e instanceof YFSUserExitException) {
				throw (YFSUserExitException)e;
				
			}
			YFSUserExitException ueExce = new YFSUserExitException(e.getMessage());
			throw ueExce;
		}
		
		if (outDoc == null) 
		{
			return doc;
		}
		
		log.verbose("****************** Output Document after calling custom service :::::" + XMLUtil.getXMLString(outDoc));
		return outYCDServiceOutput;
	}

	public String beforeCreateOrder(YFSEnvironment arg0, String arg1)
			throws YFSUserExitException {

		return arg1;
	}
	
	/**
	 * Part of code clean process; below code is copied from custom AcademyStampAttributesOnOrderLine API
	 */
	/**
	 * This would get the BeforeCreateOrderUE input and verify if attributes
	 * like 'SourcingClassification' at the header level and 'PackListType' at
	 * the line level needs to be stamped based on certain Item & Customer
	 * attributes. If required, appropriate methods will be called to stamp the
	 * above attributes.
	 * 
	 * @param env
	 *            Envrionment variable passed to the API
	 * @param inDoc
	 *            Input document of 
	 * 
	 * @throws YFSUserExitException
	 *             
	 */
	public void stampAttributesOnOrderLines(YFSEnvironment env, Document inDoc) throws YFSUserExitException {
		Document docCustomerDetails = null;
		String strCorporateCustomer = null;
		String strCustomerId = null;
		log.beginTimer(" Begining of AcademyBeforeCreateOrderUEImpl- >stampAttributesOnOrderLines Api");
		if (!YFCObject.isVoid(inDoc)) {
			if (log.isVerboseEnabled()) {
				log
						.verbose("********* Inside stampAttributesOnOrderLines input document is :::"
								+ XMLUtil.getXMLString(inDoc));
			}
			//prepareOutputDocument(inDoc);
			strCustomerId = inDoc.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_BILL_TO_ID);
			
			/*  Start - 12/17/2010 - Commented by Manju to avoid getCustomerList call since corporate Customer logic is out of scope
			docCustomerDetails = getCustomerDetails(strCustomerId);
			try {
				if (!YFCObject.isVoid(docCustomerDetails)) {
					strCorporateCustomer = XPathUtil.getString(
							docCustomerDetails.getDocumentElement(),
							AcademyConstants.XPATH_CUST_CORP_CUSTOMER);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!YFCObject.isVoid(strCorporateCustomer)) {
				if (log.isVerboseEnabled()) {
					log
							.verbose("********* Calling method to verify and stamp attributes on order header ******");
				}
				stampOrderAttributesIfCorporateCustomer(strCorporateCustomer);
			}
			   End - 12/17/2010
			*/
			if (log.isVerboseEnabled()) {
				log
						.verbose("********* Calling method to verify and stamp attributes on order lines ******");
			}
			stampPackListTypeForOrderLines(env,inDoc);
			if (log.isVerboseEnabled()) {
				log
						.verbose("********* Calling method to clear templates in env ******");
			}
			
			//Start - STL-731 - Stamp LineType as AMMO for the OrderLines which have items with HazmatClass C1 
			/* Start - Changes made for STL-934 Checkout Funnel */
			stampLineTypeForOrderLines(env,inDoc);
			//End - STL-731 - Stamp LineType as AMMO for the OrderLines which have items with HazmatClass C1
			/* End - Changes made for STL-934 Checkout Funnel */
			
		}
		/*
		 * System.out.println("#### Final docOutput is :::::" +
		 * XMLUtil.getXMLString(docOutput));
		 */
		log.endTimer(" End of AcademyBeforeCreateOrderUEImpl- >stampAttributesOnOrderLines Api");
		//return docOutput;
	}
	
	/** Added for SFS2.0. 
	 * This method will invoke getCommonCodeList API and returns the API output document to the caller.
	 * @throws Exception 
	 */
	
	public Document getCommonCodeList (YFSEnvironment envLocal) throws Exception{
		log.beginTimer("***Begining of AcademyBeforeCreateOrderUEImpl.getCommonCodeList()");
		
		Document getCommonCodeListInDoc =null;
		Document getCommonCodeListOutDoc = null;
		
        try {
        	getCommonCodeListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
        	getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.MAX_QTY_HOLD);
        	getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE, AcademyConstants.DSV_ENTERPRISE_CODE);
        	
        	log.verbose("getCommonCodeList input Doc: "+XMLUtil.getXMLString(getCommonCodeListInDoc));
        	
        	Document getCommonCodeListOPTempl = XMLUtil.getDocument("<CommonCode OrganizationCode ='' CodeType='' CodeValue=''/>");
        	envLocal.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST, getCommonCodeListOPTempl);
      
        	getCommonCodeListOutDoc = AcademyUtil.invokeAPI(envLocal, AcademyConstants.API_GET_COMMONCODE_LIST, getCommonCodeListInDoc);
        	envLocal.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);
        	
        	log.verbose("getCommonCodeList API Output Doc: " +XMLUtil.getXMLString(getCommonCodeListOutDoc));
        }catch (Exception e){
			e.printStackTrace();
			throw (Exception)e;
        }
        log.endTimer("***End of AcademyBeforeCreateOrderUEImpl.getCommonCodeList()");
        return getCommonCodeListOutDoc;
	}
	/**
	 * SFS2.0 Ends
	 */

	/**
	 * This method will stamp 'PackListType' attribute on lines which have items
	 * that have 'Ship Alone' flag enabled
	 * 
	 * 
	 */
	private void stampPackListTypeForOrderLines(YFSEnvironment envLocal, Document inDoc) throws YFSUserExitException{
		log.verbose("inside stampPackListTypeForOrderLines, indoc: "+XMLUtil.getXMLString(inDoc));
		Element eleOrderInOutput = null;
		NodeList nListOrderLines = null;
		int iNoOfOrderLines = 0;
		
		Element eleCurrentOrderLine = null;
		Element extnItem = null;
		String strItemID = null;
		String strPrimeLineNo = null;
		String strShipAloneFlag = null;
		eleOrderInOutput = inDoc.getDocumentElement();
		YFSUserExitException itemExce = null;
		
		//SOF :: WN-2036 - Start
		String strLineType = null;
		//SOF :: WN-2036 - End
		
		/**
		 * Added for SFS2.0
		 */
		
		Document getCommonCodeListOutputDoc=null;
		Element orderHoldTypesRootEle=null;
		NodeList nListOrderHoldTypes = null;
		String strMaxQtyHld=null;
		String strOrderedQty=null;
		int iMaxQtyHld =0;
		float fOrderedQty=0;
		boolean bMaxQtyHoldFlag=false;
		/*** SFS2.0 Ends  */
		
		try {
			log.beginTimer(" Begin of AcademyBeforeCreateOrderUEImpl - >stampPackListTypeForOrderLines Api");
			if (log.isVerboseEnabled()) {
				log.verbose("********* Inside stampPackListTypeForOrderLines  ******");
			}
			
			/**
			 * SFS2.0 Release 2.0 Begin
			 * the method, 'getCommonCodeList' will invoke getCommonCodeList API and will return the API output document to the caller.
			 */
			log.verbose("before invoking getCommonCodeList()");
			getCommonCodeListOutputDoc =  getCommonCodeList(envLocal);
			log.verbose("after invoking getCommonCodeList()");
			
	        if(getCommonCodeListOutputDoc!=null){
	        	log.verbose("getCommonCodeList OP is not null");
	        	Element CommonCodeListEle  = getCommonCodeListOutputDoc.getDocumentElement();
	        	Element CommonCodeEle  = (Element) CommonCodeListEle.getElementsByTagName("CommonCode").item(0);
	        	strMaxQtyHld = CommonCodeEle.getAttribute("CodeValue");
	        	iMaxQtyHld =  Integer.parseInt(strMaxQtyHld);
	        	log.verbose("Max Hold Value: "+iMaxQtyHld);
	        }
	        /**
			 * SFS2.0 Release 2.0 End
			 */
	        
	        
			// Start - Fix for # 3968
			Element elePersonInfoShipTo = eleOrderInOutput.getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).getLength()> 0 ? (Element)eleOrderInOutput.getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).item(0) : null;
			// End - Fix for # 3968
			nListOrderLines = XMLUtil.getNodeList(eleOrderInOutput, AcademyConstants.XPATH_ORDERLINE);

			if (!YFCObject.isVoid(nListOrderLines)) {
				iNoOfOrderLines = nListOrderLines.getLength();
			}
			for (int i = 0; i < iNoOfOrderLines; i++) {
				eleCurrentOrderLine = (Element) nListOrderLines.item(i);

				/**
				 * SFS2.0 Release 2.0 Begin
				 */
				
				strOrderedQty =  eleCurrentOrderLine.getAttribute("OrderedQty");
				fOrderedQty = Float.parseFloat(strOrderedQty);
				String strFulfillmentType = eleCurrentOrderLine .getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
				log.verbose("Order Qty: " +fOrderedQty);
				if ((!bMaxQtyHoldFlag)&& (fOrderedQty>=iMaxQtyHld)) {
					log.verbose("The OrderLine OrderedQty is greater than Max Hold Qty. So order needs to be put on hold");
					bMaxQtyHoldFlag=true;					
				}

				/**
				 * SFS2.0 Release 2.0 Ends
				 */
				
				if (!YFCObject.isVoid(eleCurrentOrderLine)) {
					strItemID = XPathUtil.getString(eleCurrentOrderLine, AcademyConstants.XPATH_ITEM_ITEMID);
					if(strItemID == null || (strItemID != null && strItemID.trim().length()<=0)){
						log.verbose(AcademyConstants.ERR_ITEM_NOT_FOUND);
						itemExce = new YFSUserExitException("EXTN_ACADEMY_01");
						//itemExce.setErrorDescription(AcademyConstants.ERR_ITEM_NOT_FOUND);
						throw itemExce;
					}
					strPrimeLineNo = eleCurrentOrderLine .getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO);
					//Start - OMNI-6618 : STS TO Release Consolidation
					if(YFCObject.isVoid(strPrimeLineNo)) {
						strPrimeLineNo = Integer.toString(i);
					}
					//End - OMNI-6618 : STS TO Release Consolidation
					
					// Start - Fix for # 3968
					int countPersonInfoShipTo = eleCurrentOrderLine.getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).getLength();
					Element personInfoShipTo = countPersonInfoShipTo > 0 ? (Element)eleCurrentOrderLine.getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).item(0) : inDoc.createElement(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO);
					if(!(personInfoShipTo.hasAttribute("AddressLine1") && personInfoShipTo.hasAttribute(AcademyConstants.ATTR_FNAME) 
							&& personInfoShipTo.hasAttribute(AcademyConstants.ATTR_LNAME)) && 
							!(YFCObject.isNull(elePersonInfoShipTo) && YFCObject.isVoid(elePersonInfoShipTo))){
						log.verbose("PersonInfoShipTo on Line : "+XMLUtil.getElementXMLString(personInfoShipTo));						
						XMLUtil.setAttributes(elePersonInfoShipTo, personInfoShipTo);
						if(countPersonInfoShipTo <= 0)
							eleCurrentOrderLine.appendChild(elePersonInfoShipTo);
						log.verbose("Post copy of PersonInfoShipTo is : "+XMLUtil.getElementXMLString(eleCurrentOrderLine));
					}
					// End - Fix for # 3968
				}			

				Document getItemList = XMLUtil.createDocument(AcademyConstants.ITEM);
				getItemList.getDocumentElement().setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemID);
				Document outPutTemplate = YFCDocument.getDocumentFor("<ItemList> <Item ItemKey=''><Extn ExtnShipAlone='' ExtnDepartmentName='' ExtnWhiteGloveEligible=''/></Item> </ItemList>").getDocument();
				envLocal.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, outPutTemplate);
				log.verbose("itemList " + XMLUtil.getXMLString(getItemList));
				Document docGetItemDetailsOutput = AcademyUtil.invokeAPI(envLocal, AcademyConstants.API_GET_ITEM_LIST, getItemList);
				envLocal.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
				log.verbose("getItemList output " + XMLUtil.getXMLString(docGetItemDetailsOutput));

				if (!YFCObject.isVoid(docGetItemDetailsOutput) && docGetItemDetailsOutput.getDocumentElement().getElementsByTagName(AcademyConstants.ITEM).getLength()>0) 
				{
					extnItem = (Element) docGetItemDetailsOutput.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
					strShipAloneFlag = extnItem.getAttribute(AcademyConstants.ATTR_EXTN_SHPALONE);

					String strExtnWhiteGloveEligible = extnItem.getAttribute(AcademyConstants.ATTR_EXTN_WHITE_GLOVE_ELIGIBLE);
					String strDocumentType = eleOrderInOutput.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
					String strDepartmentName= extnItem.getAttribute("ExtnDepartmentName");
					// Code Changes OMSNI-105592--Start
					String strExtnhasFirearm02Items = SCXmlUtil.getXpathAttribute(eleCurrentOrderLine,
							"Extn/@ExtnHasFireArm02Items");
					// Code Changes OMSNI-105592--end
					//Start: OMNI-90416 : Separate BOPIS Shipment for FA and Non-FA
					List<String> lSOFItemDepts = Arrays.asList(props.getProperty("SOFItemDeps").split(AcademyConstants.STR_COMMA));
					//End: OMNI-90416 : Separate BOPIS Shipment for FA and Non-FA
					//OMNI-91501 Begin
					String strBOPISFASplitFlag=getBOPISFASplitShipmentFlag(envLocal);
					//OMNI-91501 End
					//SOF :: WN-2036 - Start
					//if (!YFCObject.isVoid(strShipAloneFlag)) 
					strLineType = eleCurrentOrderLine.getAttribute(AcademyConstants.ATTR_LINE_TYPE);
					if(AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equals(strLineType)){
						log.verbose("******** Order line has SOF Item. Hence stamping PackListType ****");
						eleCurrentOrderLine.setAttribute(AcademyConstants.ATTR_PACKLIST_TYPE, strPrimeLineNo);
					}
					//Start: OMNI-89597, OMNI-90416 : Separate BOPIS/STS Shipment for FA and Non-FA
					// Code Changes OMSNI-105592 added check strExtnhasFirearm02Items
					else if (((AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE.equals(strDocumentType) || (AcademyConstants.SALES_DOCUMENT_TYPE.equals(strDocumentType))
							&& (AcademyConstants.STR_SHIP_TO_STORE.equals(strFulfillmentType) || (AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE.equals(strFulfillmentType) && (!YFCObject.isVoid(strBOPISFASplitFlag) && (AcademyConstants.STR_YES).equalsIgnoreCase(strBOPISFASplitFlag))))))){
						log.verbose("******** STS1.0 Order line has SOF Item. Hence stamping PackListType ****");
						if (!YFCObject.isVoid(strExtnhasFirearm02Items)
								&& AcademyConstants.ATTR_Y.equals(strExtnhasFirearm02Items)) {
							log.verbose("******** strExtnhasFirearm02Items ****"+strExtnhasFirearm02Items);
							eleCurrentOrderLine.setAttribute(AcademyConstants.ATTR_PACKLIST_TYPE,
									AcademyConstants.STS_FA);
						} else if (YFCObject.isVoid(strExtnhasFirearm02Items) && !YFCObject.isVoid(strDepartmentName)
								&& lSOFItemDepts.contains(strDepartmentName)) {
							eleCurrentOrderLine.setAttribute(AcademyConstants.ATTR_PACKLIST_TYPE,
									AcademyConstants.STS_FA);
						}
					}
					//End: OMNI-89597, OMNI-90416 : Separate BOPIS/STS Shipment for FA and Non-FA
					//Start OMNI-6448 : STS Order Creation similar to BOPIS
					else if (!YFCObject.isVoid(strShipAloneFlag) 
							&& !YFCCommon.equalsIgnoreCase(strFulfillmentType, AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE)
							&& !YFCCommon.equalsIgnoreCase(strFulfillmentType, AcademyConstants.STR_SHIP_TO_STORE)) 
					//End OMNI-6448 : STS Order Creation similar to BOPIS
					//SOF :: WN-2036 - End
					{
						/** *****Start CR 38 ******** */
						/** Fix for EFP-3 - Start */
						/*if (!YFCObject.isVoid(strExtnWhiteGloveEligible)) 
						{
							if ((strShipAloneFlag.equals(AcademyConstants.STR_YES))
									&& (strExtnWhiteGloveEligible.equals(AcademyConstants.STR_NO))) */
						if ((strShipAloneFlag.equals(AcademyConstants.STR_YES)))	
						/** Fix for EFP-3 - End */
							{
								/** *****End CR 38 ******** */
								if (log.isVerboseEnabled()) 
								{
									//log.verbose("******** Order line has Ship Alone Item & non-white glove and hence stamping PackListType ***8");
									log.verbose("******** Order line has Ship Alone Item and hence stamping PackListType ***");
								}
								eleCurrentOrderLine.setAttribute(AcademyConstants.ATTR_PACKLIST_TYPE, strPrimeLineNo);
							}
						//}
					}
				}else{
					log.verbose("Item "+strItemID+" is not found. It may held status or not existed.");
					itemExce =  new YFSUserExitException("EXTN_ACADEMY_11");
					//itemExce.setErrorDescription("Item "+strItemID+" is not found. It may held status or not existed.");
					throw itemExce;
				}
			}
			
			/**
			 * check cart has  E-Gift Card Lines OMNI-5001
			 */
			boolean bEGCMixedCartOrder= false;
			if(AcademyBOPISUtil.checkIfOrderHasEGCline(inDoc)){				
				bEGCMixedCartOrder = true;
			}
			log.verbose("Order has any E-GIFT CARD line : "+bEGCMixedCartOrder);
			//End OMNI-5001
			//Start - BOPIS-11 : ASO_Order Capture And Processing
			//if (bMaxQtyHoldFlag){
			//In case of mixed cart BOPIS orders, even if any of the orderlines(BOPIS/NonBOPIS) has max ordered quantity, bypass placing hold on the order.
			boolean bBOPISMixedCartOrder= false;
			if(AcademyBOPISUtil.checkIfOrderHasBOPISline(inDoc)){				
					bBOPISMixedCartOrder = true;
			}
			log.verbose("Order has any BOPIS line : "+bBOPISMixedCartOrder);
			
			/**
			 * SFS2.0 Release 2.0 	  
			 * If bMaxQtyHoldFlag is set to true, if there is no element <OrderHoldTypes> present in the method input document, 
			 * then add '<OrderHoldTypes>'. And create child element <OrderHoldType>. 
			 * Set the attributes HoldType="" ReasonText="".
			 */
			
			//OMNI-6448 : STS Order Release Consolidation same as BOPIS
			String strDocumentType = eleOrderInOutput.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
			log.verbose(" strDocumentType :: " + strDocumentType);
			//OMNI-5001 !bEGCMixedCartOrder condition added for EGC
			if (bMaxQtyHoldFlag && !bBOPISMixedCartOrder && !bEGCMixedCartOrder
					&& !(!YFCObject.isVoid(strDocumentType) && strDocumentType.equals("0006"))){
			//End - BOPIS-11 : ASO_Order Capture And Processing
				log.verbose("Max Qty Hold Flag: "+bMaxQtyHoldFlag);
				nListOrderHoldTypes = XMLUtil.getNodeList(eleOrderInOutput, AcademyConstants.XPATH_ORDERHOLDTYPES);
								
				
				/**
				 * if <OrderHoldTypes> element is not present in the inDoc, then create it. 
				 * Create <OrderHoldType> as child element of <OrderHoldTypes>
				 * and set 'HoldType', 'ReasonText'.
				 */
				if ((YFCObject.isVoid(nListOrderHoldTypes)) || (YFCObject.isNull(nListOrderHoldTypes))) {
					Element orderHoldTypesEle= inDoc.createElement("OrderHoldTypes");
					orderHoldTypesRootEle= (Element) eleOrderInOutput.appendChild(orderHoldTypesEle);
				}else{
					/**
					 * get a handler to the existing <OrderHoldTypes> element
					 */
					orderHoldTypesRootEle = (Element) XMLUtil.getNode(inDoc.getDocumentElement(),AcademyConstants.XPATH_ORDERHOLDTYPES);
					
				} 	
				/**
				 * create the child element <OrderHoldType> with attributes.
				 */
				Element orderHoldTypeEle= inDoc.createElement("OrderHoldType");
				Element inDocOrderHoldTypeEle= (Element) orderHoldTypesRootEle.appendChild(orderHoldTypeEle);
				orderHoldTypeEle.setAttribute(AcademyConstants.ATTR_HOLD_TYPE, AcademyConstants.MAX_QTY_HOLD);
				orderHoldTypeEle.setAttribute(AcademyConstants.ATTR_REASON_TEXT, AcademyConstants.HOLD_REASON_TEXT);
				//orderHoldTypeEle.setAttribute("Status", "1100");
				
				log.verbose("Updated Document: "+XMLUtil.getXMLString(inDoc));
			}
			log.endTimer(" End of AcademyBeforeCreateOrderUEImpl- >stampPackListTypeForOrderLines Api");
		} 
		catch (Exception e) {
			e.printStackTrace();
			if (e instanceof YFSUserExitException) {
				throw (YFSUserExitException)e;
			}
			YFSUserExitException ueEx = new YFSUserExitException(e.getMessage());
			throw ueEx;
		}
	}
	
	/**
	 * STL-731
	 * This method will stamp LineType="AMMO" for order lines that have ammunition item.
	 * @param envLocal	YFSEnvironment
	 * @param inDoc		Document
	 * @throws YFSUserExitException
	 */
	private void stampLineTypeForOrderLines(YFSEnvironment envLocal, Document inDoc) throws YFSUserExitException {
		log.beginTimer("Begin - AcademyBeforeCreateOrderUEImpl->stampLineTypeForAmmoOrderLines");
		
		log.verbose("**********Start - AcademyBeforeCreateOrderUEImpl - inDoc Contents");
		log.verbose(XMLUtil.getXMLString(inDoc));
		log.verbose("**********End - AcademyBeforeCreateOrderUEImpl - inDoc Contents");
		
		NodeList orderLineList = inDoc.getElementsByTagName("OrderLine");		
		log.verbose("**********Total Number Of Order Lines = " + orderLineList.getLength());
		
		int noOfOrderLines = orderLineList.getLength();
		for (int counter=0; counter<noOfOrderLines; counter++) {
			Element eleOrderLine = (Element) orderLineList.item(counter);
			Element eleItem = (Element) eleOrderLine.getElementsByTagName(AcademyConstants.ELEM_ITEM).item(0);
			String	itemID = eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID);
			String	uom = eleItem.getAttribute(AcademyConstants.ATTR_UOM);
			// Change for BOPIS -1633 Start
			String strFulfillmentType = eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
			
			
			Document getItemListInDoc;
			Document getItemListOutputDoc;
			Document getItemListOutputTemplate;
			Element eleClassificationCodes;
			String strHazmatClass;
			//Start : OMNI-6448 : STS Order Creation similar to BOPIS
			//Start: OMNI-5001 EGC Fulfillment added EGC fulfillment Type
            if ((!YFCCommon.equals(strFulfillmentType, AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE)
            		&& !YFCCommon.equals(strFulfillmentType, AcademyConstants.STR_SHIP_TO_STORE) 
            		&& !YFCCommon.equals(strFulfillmentType, AcademyConstants.STR_E_GIFT_CARD))
            		|| YFCCommon.isVoid(strFulfillmentType)){
            	//End: OMNI-5001 EGC Fulfillment
            	//End : OMNI-6448 : STS Order Creation similar to BOPIS
            	// Change for BOPIS -1633 End
            
			//SOF :: Start WN-2032 : Create Order call: WCS must send ShipNode and FulfillmentType
			String strLineType = eleOrderLine.getAttribute(AcademyConstants.ATTR_LINE_TYPE);
			//For SOF orderLines, WCS will only send LineType="SOF" in CreateOrder XML. Hence we just have to retain it.
			if(YFCObject.isVoid(strLineType) || !AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equals(strLineType)){
			//SOF :: End WN-2032 : Create Order call: WCS must send ShipNode and FulfillmentType
				
			//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
			HashMap hmHazmatCommonCodeList = new HashMap();
    	    String strOrganizationCode = AcademyConstants.PRIMARY_ENTERPRISE;
    	    //End Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
			
			try {
				//Preparing input document with ItemID, UnitOfMeasure and OrganizationCode for getItemList API
				getItemListInDoc = XMLUtil.createDocument(AcademyConstants.ELEM_ITEM);
				getItemListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ITEM_ID, itemID);
				getItemListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_UOM, uom);
				getItemListInDoc.getDocumentElement().setAttribute(AcademyConstants.ORG_CODE_ATTR, AcademyConstants.HUB_CODE);
	        	
				/* Start - Changes made for STL-934 Checkout Funnel */
	        	getItemListOutputTemplate = XMLUtil.getDocument("<ItemList> <Item> <ClassificationCodes HazmatClass=''/> <Extn ExtnConveyable='' ExtnShipAlone='' ExtnWhiteGloveEligible=''/> </Item> </ItemList>");
	        	/* End - Changes made for STL-934 Checkout Funnel */
	        	envLocal.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, getItemListOutputTemplate);
	      
	        	getItemListOutputDoc = AcademyUtil.invokeAPI(envLocal, AcademyConstants.API_GET_ITEM_LIST, getItemListInDoc);
	        	envLocal.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
	        	
	        	Element eleItemList = getItemListOutputDoc.getDocumentElement();
	        	Element eleItemOp = (Element) eleItemList.getElementsByTagName(AcademyConstants.ELEM_ITEM).item(0);
	        
	        	eleClassificationCodes = (Element) eleItemOp.getElementsByTagName(AcademyConstants.ELE_CLASSIFICATION_CODES).item(0);
	        	strHazmatClass = eleClassificationCodes.getAttribute(AcademyConstants.ATTR_HAZMAT_CLASS);
	        	
	        	/* Start - Changes made for STL-934 Checkout Funnel */
	        	Element eleExtnAttributes =(Element) eleItemOp.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
	        	String strConveyable=eleExtnAttributes.getAttribute(AcademyConstants.ATTR_EXTN_CONVEYABLE);
	        	String strShipAlone=eleExtnAttributes.getAttribute(AcademyConstants.ATTR_EXTN_SHIP_ALONE);
	        	String strWhiteGlove=eleExtnAttributes.getAttribute(AcademyConstants.ATTR_EXTN_WHITE_GLOVE_ELIGIBLE);
	        	/* End - Changes made for STL-934 Checkout Funnel */
	        	
	        	//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
	        	/*if(AcademyConstants.AMMO_HAZMAT_CLASS.equals(strHazmatClass)) {
	        		log.verbose(itemID + " LineType changed to AMMO");
	        		eleOrderLine.setAttribute(AcademyConstants.ATTR_LINE_TYPE, AcademyConstants.AMMO_LINE_TYPE);
	        	}*/
	        	
	    	    hmHazmatCommonCodeList = AcademyCommonCode.getCommonCodeListAsHashMap(envLocal, AcademyConstants.HAZMAT_CLASS_COMMON_CODE, strOrganizationCode);
	         	if (!YFCObject.isVoid(strHazmatClass) && hmHazmatCommonCodeList.containsKey(strHazmatClass))
	         	{
		    	    if( AcademyConstants.AMMO_HAZMAT_CLASS.equals(strHazmatClass))
		        	{
		        			log.verbose(itemID + " LineType changed to AMMO");
			        		eleOrderLine.setAttribute(AcademyConstants.ATTR_LINE_TYPE, AcademyConstants.AMMO_LINE_TYPE);
		        	}
		        		else {
		        			log.verbose(itemID + " LineType changed to HAZMAT");
			        		eleOrderLine.setAttribute(AcademyConstants.ATTR_LINE_TYPE, AcademyConstants.HAZMAT_LINE_TYPE);
		        		}
	         	}
	          	//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
	        	
	        	/* Start - Changes made for STL-934 Checkout Funnel */
	        	
	        	if((!YFCObject.isNull(strConveyable)&&AcademyConstants.STR_NO.equalsIgnoreCase(strConveyable))
	        			&&(!YFCObject.isNull(strShipAlone)&&AcademyConstants.STR_NO.equalsIgnoreCase(strShipAlone))&&
	        			    (!YFCObject.isNull(strShipAlone)&&AcademyConstants.STR_NO.equalsIgnoreCase(strWhiteGlove))){
                  eleOrderLine.setAttribute(AcademyConstants.ATTR_LINE_TYPE, AcademyConstants.ATTR_CSA_LINE_TYPE);
	        			        		log.verbose(itemID + " LineType changed to CHKOUTFNL");
	        	}
	        	/* End - Changes made for STL-934 Checkout Funnel */

			}
			catch (Exception e) {
				log.verbose(e.toString());
				throw new YFSUserExitException(e.toString());
			}
		}
		}
		}
		log.verbose("**********Start - AcademyBeforeCreateOrderUEImpl - Updated inDoc Contents");
		log.verbose(XMLUtil.getXMLString(inDoc));
		log.verbose("**********End - AcademyBeforeCreateOrderUEImpl - Updated inDoc Contents");
		log.endTimer("Begin - AcademyBeforeCreateOrderUEImpl->stampLineTypeForOrderLines");
	}	
	
	
	/** This method modify AuthorizationExpirationDate for paypall order based on cut of time and Auth expire days configured in common Code.
	 * @param env
	 * @param doc
	 * @throws Exception
	 */
	
	/*
	Begin: OMNI-1755 :Cardinal Technical Cleanup
	
	public void modifyAuthoExpirationDate(YFSEnvironment env, Document doc) throws Exception
	{
		String strPathPaymentDetails = "/Order/PaymentMethods/PaymentMethod[@PaymentType='"+AcademyConstants.PAYPAL+"']/PaymentDetailsList/PaymentDetails";

		NodeList nlPaymentDetails = XMLUtil.getNodeList(doc.getDocumentElement(), strPathPaymentDetails);
		if (nlPaymentDetails.getLength() > 0)
		{
			String strOrderDate = doc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_DATE);
			String strExpirationDate = AcademyPaymentProcessingUtil.getAuthorizationExpirationDateForPayPall(env, strOrderDate);
			for (int i = 0; i < nlPaymentDetails.getLength(); i++)
			{
				Element elePaymentDetails = (Element)nlPaymentDetails.item(i);
				elePaymentDetails.setAttribute(AcademyConstants.ATTR_AUTH_EXP_DATE, strExpirationDate);
				log.verbose("strAuthorizationExpirationDate:\n" + elePaymentDetails.getAttribute(AcademyConstants.ATTR_AUTH_EXP_DATE));
			}			
		}
	}
	End: OMNI-1755 : Cardinal Technical Cleanup
	*/
	
	//OMNI-7689
	private Document updateSalesOrderInfo (YFSEnvironment env, Document docInput) throws Exception {
		log.verbose("Begin - updateSalesOrderInfo() :: ");
		Document docGetOrdListOut = null;		
	
		Element eleOrderLine = (Element) docInput.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE).item(0);
		String strChainedFromOrderHeaderKey = eleOrderLine.getAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_HEADER_KEY);
		
		if(!StringUtil.isEmpty(strChainedFromOrderHeaderKey)) {
			log.verbose("Chained From OHK :: "+strChainedFromOrderHeaderKey);
			
			docGetOrdListOut = invokeGetOrderList(env, strChainedFromOrderHeaderKey);
			//Start : OMNI-8907 : UpdateBilling info on Transfer Order
			if(null != docGetOrdListOut) {
				Element eleOrder = (Element) docGetOrdListOut.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
				
				docInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_NAME, 
						eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO));

				Element eleTOPersonInfoBillTo = XmlUtils.createChild(docInput.getDocumentElement(), AcademyConstants.ELE_PERSON_INFO_BILL_TO);
				XMLUtil.copyElement(docInput, XmlUtils.getChildElement(eleOrder, AcademyConstants.ELE_PERSON_INFO_BILL_TO), eleTOPersonInfoBillTo);
			}
			//End : OMNI-8907 : UpdateBilling info on Transfer Order
		}				
		
		log.verbose("End - updateSalesOrderInfo() :: ");
		return docInput;
	}
	
	private Document invokeGetOrderList(YFSEnvironment env, String strChainedFromOrderHeaderKey) throws Exception {
		
		log.verbose("Begin - invokeGetOrderList() :: ");
		
		Document docGetOrdListInp = null;
		Document docGetOrdListOut = null;
		Document docGetOrderListTemplate = null;
		
		docGetOrdListInp = XMLUtil.createDocument("Order");
		docGetOrdListInp.getDocumentElement().setAttribute(
                AcademyConstants.ATTR_ORDER_HEADER_KEY, strChainedFromOrderHeaderKey);
		log.verbose("input - getOrderList :: "+ XMLUtil.getXMLString(docGetOrdListInp));
		
		docGetOrderListTemplate = XMLUtil.getDocument(
				"<OrderList>"
				+"<Order OrderHeaderKey=\"\" OrderNo=\"\" >"
				+ "<PersonInfoBillTo />"
				+ "</Order>"
				+"</OrderList>");
		
		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, docGetOrderListTemplate);
		docGetOrdListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, docGetOrdListInp);
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
		
		log.verbose("output - getOrderList :: "+ XMLUtil.getXMLString(docGetOrdListOut));
		
		log.verbose("Begin - invokeGetOrderList() :: ");
		
		return docGetOrdListOut;
	}
	//OMNI-7689
	
	//START: OMNI-5006 Returns the EGC Refund Item configured in Common Code
	
	public String getEGCRefundItem(YFSEnvironment env) throws Exception {

		Document getCommonCodeListInDoc = null;
		Document getCommonCodeListOutDoc = null;
		String strEGCRefundItem = null;

		try {
			getCommonCodeListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE,
					AcademyConstants.STR_REFUND_ITEM_EGC);
			getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ORG_CODE_ATTR, 
					AcademyConstants.PRIMARY_ENTERPRISE);
    		getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE, 
    				AcademyConstants.STR_EGC_REFUND_ITEM_CODE_VALUE);

			Document getCommonCodeListOPTempl = XMLUtil
					.getDocument("<CommonCode OrganizationCode ='' CodeType='' CodeValue='' CodeShortDescription=''/>");
			env.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST, getCommonCodeListOPTempl);

			getCommonCodeListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST,
					getCommonCodeListInDoc);
			env.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);

			if (getCommonCodeListOutDoc != null) {
				Element CommonCodeListEle = getCommonCodeListOutDoc.getDocumentElement();
				Element CommonCodeEle = (Element) CommonCodeListEle.getElementsByTagName("CommonCode").item(0);
				strEGCRefundItem = CommonCodeEle.getAttribute("CodeShortDescription");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw (Exception) e;
		}
		return strEGCRefundItem;
	}
	//END: OMNI-5006

	/**
	 * This method is added as part of OMNI-91501. It calls the getCommonCodeList API 
	 * to fetch the details if BOPIS Firearm is enabled
	 * @param env
	 * 
	 */
	private String getBOPISFASplitShipmentFlag(YFSEnvironment env) throws Exception {

		Document getCommonCodeListInDoc = null;
		Document getCommonCodeListOutDoc = null;
		String strBOPISFAFlag = null;

		try {
			getCommonCodeListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE,
					AcademyConstants.V_TGL_RCP_WEB_SOM_UI);
			
    		getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE, 
    				AcademyConstants.STR_ENABLE_BOPIS_FIREARM);

			Document getCommonCodeListOPTempl = XMLUtil
					.getDocument("<CommonCode OrganizationCode ='' CodeType='' CodeValue='' CodeShortDescription=''/>");
			env.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST, getCommonCodeListOPTempl);

			getCommonCodeListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST,
					getCommonCodeListInDoc);
			env.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);

			if (getCommonCodeListOutDoc != null) {
				Element CommonCodeListEle = getCommonCodeListOutDoc.getDocumentElement();
				Element CommonCodeEle = (Element) CommonCodeListEle.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE).item(0);
				strBOPISFAFlag = CommonCodeEle.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw (Exception) e;
		}
		return strBOPISFAFlag;
	}
}
