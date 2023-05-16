package com.academy.ecommerce.sterling.interfaces.api;

import java.io.File;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;



public class AcademyPublishCountAdjToIP {
	
	private static YFCLogCategory log = YFCLogCategory
	.instance(AcademyPublishCountAdjToIP.class);

	public Document publishCountAdjToIP(YFSEnvironment env, Document inputDoc) throws Exception {
		
		Element inputElement = inputDoc.getDocumentElement();
		if (!YFCObject.isNull(inputElement)) {
			
			String countRequestKey = inputElement.getAttribute("CountRequestKey");
			
			if (!YFCObject.isNull(countRequestKey)) {
				//Call the api getCountRequestDetails with countRequestKey
				Document inputDocToGetCountReqDetails = XMLUtil.createDocument("CountRequest");
				Element inputEleToGetCountReqDetails = inputDocToGetCountReqDetails.getDocumentElement();
				inputEleToGetCountReqDetails.setAttribute("CountRequestKey", countRequestKey);
				
				Document outDocGetCountReq = AcademyUtil.invokeAPI(env, "getCountRequestDetails", inputDocToGetCountReqDetails);
				
				Element outEleGetCountReq = outDocGetCountReq.getDocumentElement();
				NodeList countResultNS = outDocGetCountReq.getElementsByTagName("CountResult");
				String user = outEleGetCountReq.getAttribute("RequestingUserId");
				

				
				//Call the api AcademyGetLocationInventoryList Service to get the reason code user id
				Document inputDocToGetLocInvAuditList = XMLUtil.createDocument("LocationInventoryAudit");
				Element inputEleToGetLocInvAuditList = inputDocToGetLocInvAuditList.getDocumentElement();
				inputEleToGetLocInvAuditList.setAttribute("CountRequestKey", countRequestKey);
				// Issue:STL-32 Sterling is not sending the Inventory adjustment from 12:00 AM to 1:00 AM to IP
				// added 2 attributes like FromCreatets and ToCreatets
				inputEleToGetLocInvAuditList.setAttribute("FromCreatets",
						"1900-01-01T00:00:00");
				inputEleToGetLocInvAuditList.setAttribute("ToCreatets",
						"2500-01-01T00:00:00");
				
				//inputEleToGetLocInvAuditList.setAttribute("LocationId", setLocationId);
				
				for (int i = 0; i < countResultNS.getLength(); i++) {
					Element countResult = (Element) countResultNS.item(i);
					String varianceAccepted = countResult.getAttribute("VarianceAccepted");
					
					if ("Y".equals(varianceAccepted)) {
						String setLocationId = countResult.getAttribute("LocationId");
						inputEleToGetLocInvAuditList.setAttribute("LocationId", setLocationId);
						log.verbose("this is the location id for the input api" + setLocationId);
					}
				}
				
				Document outDocGetLocInvAudit = AcademyUtil.invokeService(env, "AcademyGetLocationInventoryAuditList", inputDocToGetLocInvAuditList);
				
				Element outEleGetLocInvAuditList = outDocGetLocInvAudit.getDocumentElement();
				Element outEleGetLocInvAudit = (Element) outEleGetLocInvAuditList.getElementsByTagName("LocationInventoryAudit").item(0);

				log.verbose("Audit table output xml" + XMLUtil.getElementXMLString(outEleGetLocInvAuditList));
				
				if (!YFCObject.isNull(outEleGetLocInvAudit)) {
					
					log.verbose("Audit table output element" + XMLUtil.getElementXMLString(outEleGetLocInvAuditList));
					
					String reasonCode = outEleGetLocInvAudit.getAttribute("ReasonCode");
					String reasonText = outEleGetLocInvAudit.getAttribute("ReasonText");
					//String userId = outEleGetLocInvAudit.getAttribute("Modifyuserid");
					String modifyts = outEleGetLocInvAudit.getAttribute("Modifyts");
					
					for (int countResultList = 0; countResultList < countResultNS.getLength(); countResultList++) {

						Element countResult = (Element) countResultNS.item(countResultList);
						String varianceAccepted = countResult.getAttribute("VarianceAccepted");

						if ("Y".equals(varianceAccepted)) {

							log.verbose("count result element to be published" + XMLUtil.getElementXMLString(countResult));
							Document sendInvUpdateToIP = XMLUtil.createDocument("Adjustment");
							Element inventoryEle = XMLUtil.createElement(sendInvUpdateToIP, "Inventory", null);
							sendInvUpdateToIP.getDocumentElement().appendChild(inventoryEle);

							inventoryEle.setAttribute("ShipNode", countResult.getAttribute("Node"));
							inventoryEle.setAttribute("ExtnTimeStamp", modifyts);
							inventoryEle.setAttribute("ItemID", countResult	.getAttribute("ItemID"));
							inventoryEle.setAttribute("UnitOfMeasure", "EACH");
							inventoryEle.setAttribute("ProductClass", "GOOD");
							inventoryEle.setAttribute("AdjustmentReasonCode",reasonCode);
							inventoryEle.setAttribute("QuantityAdjusted", countResult.getAttribute("VarianceQuantity"));
							inventoryEle.setAttribute("UserId", user);

							log.verbose("Output message sent to IP" + XMLUtil.getXMLString(sendInvUpdateToIP));
							AcademyUtil.invokeService(env,"AcademyPublishCountADJ",	sendInvUpdateToIP);

						}
					}
				}
				
			}
		}
		return inputDoc;
	}
	
/*	public static void main(String[] args)
	{
		Document doc = YFCDocument.getDocumentFor(new File("C://input.xml")).getDocument();
		
		try {
			Document outDoc = new AcademyPublishCountAdjToIP().publishCountAdjToIP(doc);
			System.out.println("this is val" + XMLUtil.getXMLString(outDoc));
		} catch (Exception e) {
			// TODO: handle exception
		}
	}	*/
}
