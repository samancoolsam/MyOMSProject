//package declaration
package com.academy.ecommerce.sterling.shipment;
//java util import statements
import java.util.HashMap;
import java.util.Properties;
//w3c util import statements
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
//academy util import statements
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.ecommerce.sterling.util.AcademyWGCarrierOverrideUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
//yantra util import statements
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

/**
 * This class modify CreateShipment Input XML to have OrderNO, OrderHeaderKey
 * and Carrier Service Priority, also it checks for the APO/FPO Address and if it is APO/FPO downgrades LOS
 *
 * @author Anushri
 */
public class AcademySFSBeforeCreateShipmentUEImpl implements YIFCustomApi
{
	//Set the logger
	private static YFCLogCategory	log	= YFCLogCategory.instance(AcademyYDMBeforeCreateShipmentUEImpl.class);
	// Instantiate the Porperties
	private Properties	prop;
	// Holds the value for SCAC and CarrierServiceCode
	public void setProperties(Properties prop) throws Exception
	{
		this.prop = prop;
	}
	public Document beforeCreateShipment(YFSEnvironment env, Document inDoc) throws Exception
	{
		log.beginTimer(" Begin of AcademySFSBeforeCreateShipmentUEImpl  beforeCreateShipment()- Api");
		if (log.isVerboseEnabled())
		{
			log.verbose("************* Inside AcademySFSBeforeCreateShipmentUEImpl ::beforeCreateShipment ***************");
			log.verbose("********* Input XML for method beforeCreateShipment :: AcademySFSBeforeCreateShipmentUEImpl" + XMLUtil.getXMLString(inDoc));
		}

		/*
		 * Input XML format <Shipment Action="Create"
		 * BillToAddressKey="20120207033529115759837" BillToCustomerId=""
		 * BuyerMarkForNodeId="" BuyerOrganizationCode=""
		 * BuyerReceivingNodeId="" CarrierAccountNo=""
		 * CarrierServiceCode="Ground" CarrierType="PARCEL"
		 * CreateNewShipment="Y" Currency="USD" CustomerPoNo="" DeliveryCode=""
		 * DeliveryMethod="SHP" DeliveryTS="2012-06-27T06:13:12-04:00"
		 * DepartmentCode="" DocumentType="0001" EnterpriseCode="Academy_Direct"
		 * EspCheckRequired="N" ExpectedDeliveryDate="2012-06-27T06:13:12-04:00"
		 * ExpectedShipmentDate="2012-06-27T06:13:12-04:00"
		 * FindShipmentAndAdd="Y" FreightTerms="PREPAID" GiftFlag="N"
		 * IsAppointmentReqd="N" ItemClassification="" MarkForKey=""
		 * MergeNode="" OrderAvailableOnSystem="Y" OrderType=""
		 * OverrideModificationRules="N" PackAndHold="N" PackListType=""
		 * PriorityCode="" ReceivingNode="" RoutingSource="02" SCAC="UPSN"
		 * SellerOrganizationCode="Academy_Direct"
		 * ShipDate="2012-06-27T06:13:12-04:00" ShipNode="005"
		 * ShipToCustomerId="" ShipmentConsolidationGroupId=""
		 * ShipmentType="CON" ToAddressKey="20120207033529115759837"
		 * UpdateOrderFulfillment="N" WorkOrderApptKey="" WorkOrderKey="">
		 * <ShipmentLines> <ShipmentLine BuyerMarkForNodeId="" CustomerPoNo=""
		 * DepartmentCode="" GiftFlag="N" MarkForKey=""
		 * OrderHeaderKey="20120627055922232429"
		 * OrderLineKey="20120627055923232430" OrderNo="A062702"
		 * OrderReleaseKey="20120627061313232819" OrderType="" Quantity="5.00"
		 * ShipToCustomerId="" ShipmentConsolidationGroupId=""/>
		 * </ShipmentLines> </Shipment>
		 */
		//Fetch the root element from the input xml
		Element shipmentElm = (Element) inDoc.getDocumentElement();
		//Declare the string variable
		String currOrderNo = "";
		String currHeaderKey = "";
		String sOrdRelKey = "";
		String carrServiceCode = "";
		//EFP-17
		String strZipCode = "";
		String strShipNode = "";
		//EFP-17
		//Fetch the attribute value of EnterpriseCode
		String organizationCode = shipmentElm.getAttribute("EnterpriseCode");
		// Fetch the element ShipmentLine
		Element eleShipLine = (Element) inDoc.getElementsByTagName("ShipmentLine").item(0);
		//Check if ShipmentLine not is void

		if (!YFCObject.isVoid(eleShipLine))
		{
			//Fetch the OrderReleaseKey attribute value
			sOrdRelKey = eleShipLine.getAttribute("OrderReleaseKey");
		}
		//Check if value of sOrdRelKey is void 
		if (YFCObject.isVoid(sOrdRelKey))
		{	
			//Throw exception
			throw new YFSUserExitException(" Order release key in beforeCreate Shipment UE implementation is null...............");
		}
		//Start: Process API getOrderReleaseDetails
		//Create root element OrderReleaseDetail
		Document docGetReleaseDetIP = XMLUtil.createDocument("OrderReleaseDetail");
		//Set the attribute value of OrderReleaseKey
		docGetReleaseDetIP.getDocumentElement().setAttribute("OrderReleaseKey", sOrdRelKey);
		//Set the template for getOrderReleaseDetails API
		env.setApiTemplate("getOrderReleaseDetails", "global/template/api/getOrderReleaseDetails.SCACUpdate.xml");
		//Invoke API getOrderReleaseDetails
		Document docGetReleaseDet = AcademyUtil.invokeAPI(env, "getOrderReleaseDetails", docGetReleaseDetIP);
		//Clear the template
		env.clearApiTemplate("getOrderReleaseDetails");
		//End: Process API getOrderReleaseDetails
		//Fetch element PersonInfoShipTo
		Element PSTElm = (Element) docGetReleaseDet.getDocumentElement().getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).item(0);
		//Fetch element PersonInfoShipTo/Extn
		Element extnPSTElm = (Element) PSTElm.getElementsByTagName("Extn").item(0);
		//Fetch the attribute value ExtnIsAPOFPO
		String isAPOSFOFlag = extnPSTElm.getAttribute(AcademyConstants.ACADEMY_SFS_IS_APO_OR_FPO);
		
		//************Changed by Anjana*****************************
		//Fetch the attribute value ExtnIsPOBOXADDRESS
		String isPOBOXADDRESSFlag = extnPSTElm.getAttribute(AcademyConstants.ACADEMY_SFS_IS_PO_BOX_ADDRESS);
		
		//************Changed by Anjana*****************************
		
		//Fetch the value of attribute CarrierServiceCode
		carrServiceCode = shipmentElm.getAttribute("CarrierServiceCode");
		
		//EFP-17 EFW Carrier Integration - Start
		strShipNode = docGetReleaseDet.getDocumentElement().getAttribute(AcademyConstants.SHIP_NODE);
		strZipCode = PSTElm.getAttribute(AcademyConstants.ZIP_CODE);
		String strReqCarrServiceCode = carrServiceCode;
		if(YFCObject.isNull(strReqCarrServiceCode) || YFCObject.isVoid(strReqCarrServiceCode)){
			strReqCarrServiceCode = shipmentElm.getAttribute("RequestedCarrierServiceCode");
		}
				
		if(!YFCObject.isVoid(strReqCarrServiceCode) && !YFCObject.isNull(strReqCarrServiceCode)
				&& (strReqCarrServiceCode.equalsIgnoreCase(AcademyConstants.STR_ROOM_OF_CHOICE) 
				|| strReqCarrServiceCode.equalsIgnoreCase(AcademyConstants.STR_APPOINTMENT)				
				|| strReqCarrServiceCode.equalsIgnoreCase(AcademyConstants.STR_CARRIER_SERVICE_TLGROUND))){
			
			String scacWg = AcademyWGCarrierOverrideUtil.findCarrierForWG(env, strZipCode.trim(), 
					strReqCarrServiceCode.trim(), strShipNode.trim());
			
			if(!YFCObject.isVoid(scacWg) && !YFCObject.isNull(scacWg)){
				//Set the attribute value of SCAC
				shipmentElm.setAttribute(AcademyConstants.ATTR_SCAC, scacWg);
				//Set the attribute value of CarrierServiceCode
				shipmentElm.setAttribute("CarrierServiceCode", strReqCarrServiceCode);
			}
			
		}
		//EFP-17 EFW Carrier Integration - End
		
		
		//Begin: OMNI-8714
		if(isPOBOXADDRESSFlag.equalsIgnoreCase(AcademyConstants.STR_YES) && prop != null) {
			
			String strStdPOBoxSCAC = (String) prop.get(AcademyConstants.ACADEMY_STD_PO_BOX_SCAC);
			String strStdPOBoxCarrierServiceCode = (String) prop.get(AcademyConstants.ACADEMY_STD_PO_BOX_SERVICECODE);;
			
			if(!StringUtil.isEmpty(strStdPOBoxSCAC) && !StringUtil.isEmpty(strStdPOBoxCarrierServiceCode)) {
				
				//Set the attribute value of SCAC
				shipmentElm.setAttribute(AcademyConstants.ATTR_SCAC, strStdPOBoxSCAC);
				
				//Set the attribute value of CarrierServiceCode
				shipmentElm.setAttribute("CarrierServiceCode", strStdPOBoxCarrierServiceCode);
				
				//Set the variable carrServiceCode
				carrServiceCode = strStdPOBoxCarrierServiceCode;
			}				
					
		}
		//End: OMNI-8714
		
		// Downgrading the Service in case when Ship to address is for APO/FPO
		if (isAPOSFOFlag.equalsIgnoreCase(AcademyConstants.STR_YES) && prop != null)
		{	
			// Fetch the Service SCAC for downgrad
			String downGrdSCAC = (String) prop.get(AcademyConstants.ACADEMY_APO_FPO_SCAC);
			// Fetch the Service Code for downgrad
			String downGrdServiceCode = (String) prop.get(AcademyConstants.ACADEMY_APO_FPO_SERVICECODE);
			//In case passed SCAC and Service Code is not Void
			if (!YFCObject.isVoid(downGrdServiceCode) && !YFCObject.isVoid(downGrdSCAC))
			{	//Set the attribute value of SCAC
				shipmentElm.setAttribute(AcademyConstants.ATTR_SCAC, downGrdSCAC);
				//Set the attribute value of CarrierServiceCode
				shipmentElm.setAttribute("CarrierServiceCode", downGrdServiceCode);
				//Set the variable carrServiceCode
				carrServiceCode = downGrdServiceCode;
			}
		}				
		
		//Fetch values of LOS_PRIORITY from commoncode. Store the details in hashmap
		HashMap<String, String> carrServiceCodePriorityMapping = AcademyCommonCode.getCommonCodeListAsHashMap(env, AcademyConstants.COMMON_CODE_LOS_PRIORITY, organizationCode);
		//Fetch the value for the key carrServiceCode from hashmap
		String carrServicePriority = carrServiceCodePriorityMapping.get(carrServiceCode);
		//Fetch the NodeList of element ShipmentLine
		NodeList shipmentLineNodeList = shipmentElm.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		//Fetch the element ShipmentLine
		Element shipLineElm = (Element) shipmentLineNodeList.item(0);
		//Fetch the attribute value of OrderNo
		currOrderNo = shipLineElm.getAttribute(AcademyConstants.ATTR_ORDER_NO);
		//Fetch the attribute value of OrderHeaderKey
		currHeaderKey = shipLineElm.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
		//Set the attribute value of OrderNo
		shipmentElm.setAttribute(AcademyConstants.ATTR_ORDER_NO, currOrderNo);
		//Set the attribute value of OrderHeaderKey
		shipmentElm.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, currHeaderKey);
		
		// Start Change for LOS upgrade/Downgrade (Extn is already available on shipment)
		Element extnElm = (Element) inDoc.getElementsByTagName("Extn").item(0);
		if(YFCObject.isNull(extnElm)||YFCObject.isVoid(extnElm))
		{
		//Create element Extn
		extnElm = inDoc.createElement("Extn");
		//Append the element
		shipmentElm.appendChild(extnElm);
		}
		// End Change for LOS upgrade/Downgrade
		
		//Check if carrServicePriority is not null or not blank
		if (!(carrServicePriority == null || carrServicePriority.equals("")))
		{
			//Set the attribute value of ExtnSCACPriority t value of carrServicePriority
			extnElm.setAttribute(AcademyConstants.COL_EXTN_SCAC_PRIORITY, carrServicePriority);
		}
		else
		{
			//Set the attribute value of ExtnSCACPriority to 100 
			extnElm.setAttribute(AcademyConstants.COL_EXTN_SCAC_PRIORITY, AcademyConstants.DEFAULT_LOS_PRIORITY);
		}
		//Print the verbose log
		if (log.isVerboseEnabled())
		{
			log.verbose("********* Output XML for method beforeCreateShipment :: AcademySFSBeforeCreateShipmentUEImpl" + XMLUtil.getXMLString(inDoc));
		}
		log.endTimer(" End of AcademySFSBeforeCreateShipmentUEImpl  beforeCreateShipment()- Api");
		//Return the input xml
		return inDoc;

	}
}
