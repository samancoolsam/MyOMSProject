package com.academy.ecommerce.sterling.shipment;

import java.rmi.RemoteException;

import org.w3c.dom.Document;

import com.academy.util.constants.AcademyConstants;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyPrintBOPISModifyPrintDocument 
{

	//Api instance set up
	private static YIFApi	api	= null;
	static
	{
		try
		{
			api = YIFClientFactory.getInstance().getApi();
		} catch (YIFClientCreationException e)
		{
			e.printStackTrace();
		}
	}


	public Document modifyPrintDocument(YFSEnvironment env,Document inDoc) throws YFSException, RemoteException
	{
		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
		YFCElement elePrntDocmts = yfcInDoc.getDocumentElement();
		YFCNodeList<YFCElement> nlShipment = elePrntDocmts.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
		YFCElement eleShipment = nlShipment.item(0);
		String strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);

		YFCDocument inDocShipmentList = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
		YFCElement eleShpInDocShpLst = inDocShipmentList.getDocumentElement();
		eleShpInDocShpLst.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);

		YFCDocument templateShipmentList = YFCDocument.getDocumentFor("<Shipments><Shipment ShipmentKey=\"\" DeliveryMethod=\"\"/></Shipments>");
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST,templateShipmentList.getDocument());
		Document outDocShipmentList = api.getShipmentList(env,inDocShipmentList.getDocument());
		YFCDocument yfcOutDocShpLst = YFCDocument.getDocumentFor(outDocShipmentList);
		YFCElement eleShipments = yfcOutDocShpLst.getDocumentElement();
		YFCElement eleOutDocShp = eleShipments.getChildElement(AcademyConstants.ELE_SHIPMENT);
		String strDeliveryMethod = eleOutDocShp.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
		if(YFCCommon.equalsIgnoreCase(AcademyConstants.STR_PICK,strDeliveryMethod))
		{
			//call common code and update the input xml.
			YFCDocument inDocCommonCodeLst = YFCDocument.createDocument(AcademyConstants.ELE_COMMON_CODE);
			YFCElement eleCommonCode = inDocCommonCodeLst.getDocumentElement();
			eleCommonCode.setAttribute(AcademyConstants.ATTR_CODE_TYPE,"BOPIS_SINGLE_SHPMT");
			eleCommonCode.setAttribute(AcademyConstants.ORGANIZATION_CODE,AcademyConstants.HUB_CODE);
			Document outDocCommonCode = api.getCommonCodeList(env,inDocCommonCodeLst.getDocument());

			String strPrintDocumentID = "";
			String strPrintData ="";
			YFCDocument outDocCommonCodeLst = YFCDocument.getDocumentFor(outDocCommonCode);
			YFCElement eleOutDocCommonCodeLst = outDocCommonCodeLst.getDocumentElement();
			YFCNodeList<YFCElement> nlCommonCode = eleOutDocCommonCodeLst.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
			for(YFCElement outDocCommnCode: nlCommonCode)
			{
				String strCodeValue = outDocCommnCode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
				String strCodeShrtDesc = outDocCommnCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
				if(YFCCommon.equalsIgnoreCase("PrintDocumentID",strCodeValue))
				{
					strPrintDocumentID=strCodeShrtDesc;
				}
				else if(YFCCommon.equalsIgnoreCase("PrintData",strCodeValue))
				{
					strPrintData=strCodeShrtDesc;
				}
			}

			//Stamp the above values in the input doc.
			YFCElement elePrintDocument = elePrntDocmts.getChildElement("PrintDocument");
			YFCElement eleInputData = elePrintDocument.getChildElement("InputData");
			YFCElement eleinDocShpment = eleInputData.getChildElement(AcademyConstants.ELE_SHIPMENT);
			elePrintDocument.setAttribute(AcademyConstants.ATTR_DOCUMENT_ID,strPrintDocumentID);
			eleinDocShpment.setAttribute(AcademyConstants.ATTR_DOCUMENT_ID,strPrintDocumentID);
			eleinDocShpment.setAttribute("PrintData",strPrintData);

		}

		return inDoc;
	}
}
