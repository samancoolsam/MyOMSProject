package com.academy.ecommerce.sterling.bopis.sfspacking.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class identifies if the Shipment is Bulk or non Bulk
 * If non Bulk return true else return false.
 * Sets the PrinterId's in Env for Bulk Shipment.
 * @author Abhishek Aggarwal
 *
 */
public class AcademyBOPISIdentifyPrintAndSetTransacObj implements YCPDynamicConditionEx
{
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademyBOPISIdentifyPrintAndSetTransacObj.class);

	@Override
	public boolean evaluateCondition(YFSEnvironment env, String arg1,Map arg2, Document arg3) 
	{
		String strIWebStoreFlow = (String) env.getTxnObject(AcademyConstants.A_IS_WEB_STORE_FLOW);
		log.verbose("AcademyBOPISIdentifyPrintAndSetTransacObj.evaluateCondition.Begin :"+arg3);
		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(arg3);
		YFCElement eleContainer = yfcInDoc.getDocumentElement();
		YFCElement eleShipment = eleContainer.getChildElement("Shipment");
		String strShipmentType = eleShipment.getAttribute("ShipmentType");
		if(strShipmentType.equals("CON") || strShipmentType.equals("CONOVNT") || strShipmentType.equals("GC") || 
				strShipmentType.equals(AcademyConstants.STR_GC_ONLY_SHIP_TYPE) ||
				strShipmentType.equals(AcademyConstants.AMMO_SHIPMENT_TYPE)|| strShipmentType.equals(AcademyConstants.ATTR_CSA_SHIPMENT_TYPE)
				|| AcademyConstants.HAZMAT_SHIPMENT_TYPE.equals(strShipmentType) || strShipmentType.equals(AcademyConstants.ATTR_CSA_OVNT_SHIPMENT_TYPE))
		{
			log.debug("------------Non Bulk Shipment------------");
			return true;
		}
		else if(YFCCommon.equalsIgnoreCase("Y", strIWebStoreFlow))
		{
			log.debug("------------Bulk Shipment------------");
			String PackSlipPrinterId = "";
			String BOLPrinterId = "";
			String ShippingLabelPrinterId = "";
			String ReturnLabelPrinterId = "";
			String ORMDLabelPrinterId = "";
			//Set Transaction object and return false.
			Document inCommonCodeDoc;
			try {
				inCommonCodeDoc = XMLUtil.createDocument("CommonCode");

				/*If The CommonCode Type is called for selecting Pack slip printer
			Input to getCommonCodeList is:
				<CommonCode CodeType="" />*/

				Element inComElem = inCommonCodeDoc.getDocumentElement();
				inComElem.setAttribute("CodeType", "PACK_STATION1");

				Document outCommDoc = AcademyUtil.invokeAPI(env, "getCommonCodeList",
						inCommonCodeDoc);
				log.verbose("getCommonCodeListOutDoc "+outCommDoc);
				
				if (!YFCObject.isVoid(outCommDoc)) {
					Element outCommElem = outCommDoc.getDocumentElement();
					NodeList CommonCodeList = XMLUtil.getNodeList(outCommElem,
							"CommonCode");
					String strCodeValueCheck = "";	
					if(strShipmentType.equals("BLP") || strShipmentType.equals("BNLP") || strShipmentType.equals("BULKOVNT"))
					{
						strCodeValueCheck = "BULK_PRINTER";
					}
					else if(strShipmentType.contains("WG")){
						strCodeValueCheck = "WG_PRINTER";
					}
					else if(strShipmentType.contains("CON") || 
							strShipmentType.contains(AcademyConstants.STR_GC_ONLY_SHIP_TYPE) || 
							strShipmentType.contains(AcademyConstants.STR_GC_SHIP_TYPE) || 
							strShipmentType.equals(AcademyConstants.AMMO_SHIPMENT_TYPE) ||
							AcademyConstants.HAZMAT_SHIPMENT_TYPE.equals(strShipmentType) ||
							strShipmentType.equals(AcademyConstants.ATTR_CSA_SHIPMENT_TYPE) ||  
							strShipmentType.equals(AcademyConstants.ATTR_CSA_OVNT_SHIPMENT_TYPE))
					{
						strCodeValueCheck = "NON_BULK_PACK";
					}
					if (!YFCObject.isVoid(CommonCodeList))
					{
						int iLength2 = CommonCodeList.getLength();
						for (int k = 0; k < iLength2; k++) 
						{
							Element CommonCode = (Element) CommonCodeList.item(k);
							String codevalue = CommonCode.getAttribute("CodeValue");
							if (codevalue.contains(strCodeValueCheck)) 
							{
								PackSlipPrinterId = codevalue;
								env.setTxnObject("PackSlipPrinterId", PackSlipPrinterId);
								log.verbose(" PackSlip Printer Id is :" + PackSlipPrinterId);
							} 
							if(codevalue.contains("BOL") && strCodeValueCheck.equals("WG_PRINTER"))
							{
								BOLPrinterId = codevalue;
								env.setTxnObject("BOLPrinterId", BOLPrinterId);
								log.verbose("BOL Printer Id is :" + BOLPrinterId);
							}
							if(codevalue.contains("SHIPPING_LABEL"))
							{
								ShippingLabelPrinterId = codevalue;
								env.setTxnObject("ShippingLabelPrinterId", ShippingLabelPrinterId);
								log.verbose(" Shipping Label PrinterId is :" + ShippingLabelPrinterId);						
							}
							if(codevalue.contains("RETURN_LABEL") && (strCodeValueCheck.equals("BULK_PRINTER") || strCodeValueCheck.equals("NON_BULK_PACK")))
							{
								ReturnLabelPrinterId = codevalue;
								env.setTxnObject("ReturnLabelPrinterId", ReturnLabelPrinterId);
								log.verbose(" Return Label PrinterId is :" + ReturnLabelPrinterId);
							}
							if(codevalue.contains("ORMD_LABEL"))
							{
								ORMDLabelPrinterId = codevalue;
								env.setTxnObject("ORMDLabelPrinterId", ORMDLabelPrinterId);
								log.verbose(" ORMD Label PrinterId is :" + ORMDLabelPrinterId);
								log.verbose(" ORMD Label PrinterId is :" + ORMDLabelPrinterId);
							}
						}
					}
				}
				log.endTimer(" End of getCommonCodeList To get Printer Id-> getCommonCodeList Api");

			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			log.verbose("AcademyBOPISIdentifyPrintAndSetTransacObj.evaluateCondition.End :"+arg3);
		}
		return false;
	}		

	@Override
	public void setProperties(Map arg0) 
	{
		// TODO Auto-generated method stub

	}

}
