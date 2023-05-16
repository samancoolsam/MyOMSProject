//package declaration
package com.academy.ecommerce.sterling.shipment;

//java util import statements
import java.util.HashMap;
import java.util.Properties;
//w3c util import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
//academy util import statements
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
//yantra util import statements
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/*Input to this service is below
 <Print ShipNode="" ShipmentContainerKey="" ShipmentKey="" ShipmentType="GC/CON/CONOVNT/BLP/BULKOVNT/BNLP/WG"/>
 */
public class AcademySFSReprintRequest implements YIFCustomApi
{
	//set the properties variable
	private Properties	props;
	private static final YFCLogCategory	log		= YFCLogCategory.instance(AcademySFSReprintRequest.class);
	public void setProperties(Properties props)
	{
		this.props = props;
	}
	/** This method invokes respective print service for printing Pack slip, Shipping label and Invoice
	 * @param env
	 * @param inDoc
	 * @return void
	 * @throws Exception
	 */
	public void print(YFSEnvironment env, Document inDoc) throws Exception
	{
		//Fetch the SHIPMENT_TYPE common code value by invoking getCommonCodeList method.
		//Store the CodeValue and CodeShortDescription in hashmap as key and value
		HashMap<String, String> hmpShipmentType = AcademyCommonCode.getCommonCodeListAsHashMap(env, AcademyConstants.ATTR_SHIPMENT_TYPE_CODE_VAL, "");
		//Fetch the arugument value and fetch the hashmap value for the ShipmentType. Concate the result into the string variable
		String strServiceName = props.getProperty(AcademyConstants.KEY_PRINTER_PREFIX) + hmpShipmentType.get(inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE));
		//Start STL-1600 Retrieving Service for reprint WG shipping label
		String strWGPrintService = props.getProperty(AcademyConstants.API_ACADEMY_PRINT_SHIPPING_LABEL_WG);
		//End STL-1600 Retrieving Service for reprint WG shipping label
		String containerKey = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY).trim();
		String strPrintPackId = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_REPRINT_PACK_STN);
		String strShipmentType = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
		//WMS Optimization Repritn BOL
		
		String PackSlipRePrinterId = "";
		String BOLRePrinterId = "";
		String ShippingLabelRePrinterId = "";
		String ReturnLabelRePrinterId = "";
		String ORMDLabelRePrinterId = "";		//Added for STL-737
		
		Document inCommonCodeDoc = XMLUtil.createDocument("CommonCode");
	
		/*If The CommonCode Type is called for selecting Pack slip printer
		Input to getCommonCodeList is:
			<CommonCode CodeType="" />*/
		
		Element inComElem = inCommonCodeDoc.getDocumentElement();
		inComElem.setAttribute("CodeType", strPrintPackId);
		Document outCommDoc = AcademyUtil.invokeAPI(env, "getCommonCodeList",
				inCommonCodeDoc);
		if (!YFCObject.isVoid(outCommDoc)) {
			Element outCommElem = outCommDoc.getDocumentElement();
			NodeList CommonCodeList = XMLUtil.getNodeList(outCommElem,
					"CommonCode");
		String strCodeValueCheck = "";	
		if(strShipmentType.equals("BLP") || strShipmentType.equals("BNLP") || strShipmentType.equals("BULKOVNT")){
			strCodeValueCheck = "BULK_PRINTER";
		}
		else if(strShipmentType.contains("WG")){
			strCodeValueCheck = "WG_PRINTER";
		}
		// Start STL-737 Changes: For AMMO shipment also, use NON_BULK_PACK
         //Start :Changes made for STL-934 Checkout Funnel
		//Start : Changes made for STL-1320 Checkout Funnel OVNT
		//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
		else if(strShipmentType.contains("CON") ||
				strShipmentType.contains(AcademyConstants.STR_GC_ONLY_SHIP_TYPE) || 
				strShipmentType.contains(AcademyConstants.STR_GC_SHIP_TYPE) || 
				strShipmentType.equals(AcademyConstants.AMMO_SHIPMENT_TYPE)||
				AcademyConstants.HAZMAT_SHIPMENT_TYPE.equals(strShipmentType)||
				strShipmentType.equals(AcademyConstants.ATTR_CSA_SHIPMENT_TYPE) || 
				strShipmentType.equals(AcademyConstants.ATTR_CSA_OVNT_SHIPMENT_TYPE)){
			strCodeValueCheck = "NON_BULK_PACK";
		}
		//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
		//End : Changes made for STL-1320 Checkout Funnel OVNT
		// End STL-737 Changes
       //End : Changes made for STL-934 Checkout Funnel
			if (!YFCObject.isVoid(CommonCodeList)) {
				int iLength2 = CommonCodeList.getLength();
				for (int k = 0; k < iLength2; k++) {
					Element CommonCode = (Element) CommonCodeList.item(k);
					String codevalue = CommonCode.getAttribute("CodeValue");
					if (codevalue.contains(strCodeValueCheck)) {
						PackSlipRePrinterId = codevalue;
						env.setTxnObject("PackSlipRePrinterId", PackSlipRePrinterId);
						log.verbose(" PackSlip Printer Id is :" + PackSlipRePrinterId);
					} 
					if(codevalue.contains("BOL") && strCodeValueCheck.equals("WG_PRINTER")){
						BOLRePrinterId = codevalue;
						env.setTxnObject("BOLRePrinterId", BOLRePrinterId);
						log.verbose("BOL Printer Id is :" + BOLRePrinterId);
					}
					//STL-1600 All shipments including WG will have shipping label
					//if(codevalue.contains("SHIPPING_LABEL") && (strCodeValueCheck.equals("BULK_PRINTER") || strCodeValueCheck.equals("NON_BULK_PACK"))){
                    if(codevalue.contains("SHIPPING_LABEL")) {  
						ShippingLabelRePrinterId = codevalue;
                        env.setTxnObject("ShippingLabelRePrinterId", ShippingLabelRePrinterId);
                        log.verbose(" Shipping Label PrinterId is :" + ShippingLabelRePrinterId);
                  }
                  if(codevalue.contains("RETURN_LABEL") && (strCodeValueCheck.equals("BULK_PRINTER") || strCodeValueCheck.equals("NON_BULK_PACK"))){
                        ReturnLabelRePrinterId = codevalue;
                        env.setTxnObject("ReturnLabelRePrinterId", ReturnLabelRePrinterId);
                        log.verbose(" Return Label PrinterId is :" + ReturnLabelRePrinterId);
                  }
                  //Start STL-737 changes: ORMD label for ammo shipment
                  if(codevalue.contains("ORMD_LABEL")){
                	  ORMDLabelRePrinterId = codevalue;
                      env.setTxnObject("ORMDLabelRePrinterId", ORMDLabelRePrinterId);
                      log.verbose(" ORMD Label PrinterId is :" + ORMDLabelRePrinterId);
                      log.verbose(" ORMD Label PrinterId is :" + ORMDLabelRePrinterId);
                  }
                  //End STL-737 changes
				}
			}
		}
		log.endTimer(" End of getCommonCodeList To get Printer Id-> getCommonCodeList Api");
	
		//WMS Optimization Repritn BOL
		
		//Check if ShipmentContainerKey is not blank
		if (!containerKey.equals(""))
		{
			//Start STL-1600 Reprint Shipping Label for WG. we are invoking AcademyPrintShippingLabelWG
			if(AcademyConstants.WG.equalsIgnoreCase(strShipmentType) && strServiceName.contains(AcademyConstants.STR_CHECK_SHIPPING_LABEL)){
				log.verbose("Input XML to WG Shipping Label Service " + XMLUtil.getXMLString(inDoc));
				AcademyUtil.invokeService(env, strWGPrintService, inDoc);
			}
			else {
				//End STL-1600 Reprint Shipping Label for WG. we are invoking AcademyPrintShippingLabelWG
			Document docContainer = XMLUtil.createDocument(AcademyConstants.ELE_CONTAINER);
			docContainer.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY,containerKey);
			//Invoke service stored in variable strServiceName
			AcademyUtil.invokeService(env, strServiceName, docContainer);
			}//STL-1600
		} else
		{
			//Start: Process getShipmentList API
			//Create element Shipment
			Document docShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			//Set attribute value of ShipmentKey
			docShipment.getDocumentElement().setAttribute(AcademyConstants.SHIPMENT_KEY, inDoc.getDocumentElement().getAttribute(AcademyConstants.SHIPMENT_KEY));
			//Set the template for getShipmentList API
			env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, YFCDocument.getDocumentFor("<Shipments><Shipment ShipmentKey=\"\"><Containers><Container ShipmentContainerKey=\"\"/></Containers></Shipment></Shipments>").getDocument());
			//Invoke getShipmentList API
			Document docOutputGetShipmentList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, docShipment);
			//Clear the template
			env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
			//End: Process getShipmentList API
			//Fetch the NodeList of node Container
			NodeList nlShipmentList = docOutputGetShipmentList.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
			//Loop through each NodeList record
			for (int i = 0; i < nlShipmentList.getLength(); i++)
			{
				//Fetch the element Container
				Element eleContainer = (Element) nlShipmentList.item(i);
				//Invoke service stored in variable strServiceName
				AcademyUtil.invokeService(env, strServiceName, XMLUtil.getDocumentForElement(eleContainer));
			}
		}
	}
}