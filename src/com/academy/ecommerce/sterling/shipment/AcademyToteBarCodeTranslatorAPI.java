package com.academy.ecommerce.sterling.shipment;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;

import com.yantra.interop.japi.YIFCustomApi;

import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;

import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

import com.yantra.yfc.util.YFCException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Properties;

public class AcademyToteBarCodeTranslatorAPI implements YIFCustomApi {

	Document docInput = null;

	Document docOutput = null;

	Double getToteQuantity = 0.0;
	Double getShipmentQuantity = 0.0;

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyToteBarCodeTranslatorAPI.class);

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}

	/**
	 * This method will retrieve the item id based on the alias value passed in
	 * the input
	 * @param inDoc
	 *            Input document
	 * @return docOutput Document in translateBarCode format with details of
	 *         Shipment retrieved
	 */
	public Document translateToteBarCode(YFSEnvironment env, Document inDoc)throws Exception {

		String strBarCode = null;
		log.beginTimer(" Begining of AcademyToteBarCodeTranslatorAPI-> translateToteBarCode Api");
		this.docInput = inDoc;
		log.verbose("****** In translateToteBarCode method");
		if (!YFCObject.isVoid(inDoc))
			log.verbose("***** In translateLPNAlias: input document is:::" + XMLUtil.getXMLString(inDoc));
		
		if (!YFCObject.isVoid(docInput)) {
			strBarCode = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_BAR_CODE_DATA);
		}

		if (YFCObject.isVoid(strBarCode)) {
			YFSException exception = new YFSException();
			exception.setErrorCode(AcademyConstants.ERR_CODE_02);
			throw exception;
		}
		
		docOutput = getOutputDocument(docInput, env);
		
		String shipKey = "";															//shipmentKey from translateBarCode output
		String ctrKey = "";																//CaseID from translateBarCode output
		String shipNo = "";																//ShipmentNo from translateBarCode output
		Element rootBarCodeEle = docOutput.getDocumentElement();
		NodeList shipContextInfoEle = rootBarCodeEle.getElementsByTagName("ShipmentContextualInfo");
		NodeList ctrContextInfoEle = rootBarCodeEle.getElementsByTagName("ContainerContextualInfo");
		if(shipContextInfoEle.getLength() != 0){
			shipKey = ((Element)shipContextInfoEle.item(0)).getAttribute("ShipmentKey");
			shipNo=((Element)shipContextInfoEle.item(0)).getAttribute("ShipmentNo");
		}
		if(ctrContextInfoEle.getLength() != 0){
			ctrKey = ((Element)ctrContextInfoEle.item(0)).getAttribute("CaseId");
		}
				
		if (!YFCObject.isVoid(shipKey) && !YFCObject.isVoid(ctrKey))
		{
			getToteQuantity = getDetailsOfTote(strBarCode, env); // returns the total tote quantity

			getShipmentQuantity = getDetailsOfShipment(shipKey, env); // returns unplaced quantity in the shipment

				if (getToteQuantity > getShipmentQuantity)
				{

					String finalErrDesc = ("Tote Quantity is more than Total Shipment(" + shipNo + ") Quantity");
					log.verbose("errDesc is" + finalErrDesc);
					YFCException exception = new YFCException(finalErrDesc);
					exception.setErrorDescription(finalErrDesc);
					log.verbose("Created exception object");
					exception.printStackTrace();
					throw exception;

				}

				if (getToteQuantity < getShipmentQuantity)
				{

					String finalErrDesc = ("Tote Quantity is less than Total Shipment(" + shipNo + ") Quantity");
					log.verbose("errDesc is" + finalErrDesc);
					YFCException exception = new YFCException(finalErrDesc);
					exception.setErrorDescription(finalErrDesc);
					log.verbose("Created exception object");
					exception.printStackTrace();
					throw exception;
				}
		}
		docOutput.getDocumentElement().setAttribute("BarCodeType", "PackInitiation");
		log.endTimer(" End of AcademyToteBarCodeTranslatorAPI-> translateToteBarCode Api");
		return docOutput;
		
		
	
	}

	/**
	 * Method retrieves the details of the tote by making 'getLPNDetails' API call
	 * using the scanned tote value
	 */
	private Double getDetailsOfTote(String strToteNumber, YFSEnvironment env) {
		try {
			log.beginTimer(" Begining of AcademyToteBarCodeTranslatorAPI-> getDetailsOfTote Api");
			log.verbose("****** getDetailsOfTote : inside method to get tote details");
			Document docInputGetLPNDetails = XMLUtil.createDocument("GetLPNDetails");
			
			Element eleGetLPNDtls = docInputGetLPNDetails.getDocumentElement();
			Element eleLPN = docInputGetLPNDetails.createElement("LPN");
			eleGetLPNDtls.appendChild(eleLPN);

			eleGetLPNDtls.setAttribute("Node","005");
			eleLPN.setAttribute("CaseId",strToteNumber);

			Document outTempGetLPNDtls = YFCDocument.parse("<GetLPNDetails EnterpriseCode=\"\" InventoryOrganizationCode=\"\" Node=\"\">" +
					"<LPN CaseId=\"\" PalletId=\"\" ><ItemInventoryDetailList><ItemInventoryDetail Quantity=\"\" >" +
					"<InventoryItem ItemID=\"\" ProductClass=\"\" UnitOfMeasure=\"\">" +
					"</InventoryItem></ItemInventoryDetail></ItemInventoryDetailList></LPN></GetLPNDetails>").getDocument();
			env.setApiTemplate("getLPNDetails", outTempGetLPNDtls);
			Document docLPNDtlsOutput = AcademyUtil.invokeAPI(env,"getLPNDetails", docInputGetLPNDetails);
			log.verbose("****** Output getDetailsOfTote document is ::" + XMLUtil.getXMLString(docLPNDtlsOutput));
			env.clearApiTemplates();
			
			Element getLPNDtlsEle = docLPNDtlsOutput.getDocumentElement();
			NodeList itemInventoryNodeList = getLPNDtlsEle.getElementsByTagName("ItemInventoryDetail");
			
			Double Quantity = 0.0;
			String strQty = "";
			Double TotalToteQty = 0.0;
			
			for (int i = 0; i < itemInventoryNodeList.getLength(); i++) {
				strQty = ((Element) itemInventoryNodeList.item(i)).getAttribute("Quantity");
				Quantity = Double.parseDouble(strQty);
				TotalToteQty = TotalToteQty + Quantity;
			}
			log.endTimer(" End of AcademyToteBarCodeTranslatorAPI-> getDetailsOfTote Api");
			return TotalToteQty;
		} catch (Exception e) {
			throw new YFSException(e.getMessage());
		}
	}

	/**
	 * Method retrieves the details of the Shipment by making 'getShipmentList' API call
	 * using the shipment obtained from barcode translation
	 */
	private Double getDetailsOfShipment(String shipKey, YFSEnvironment env) {
		try {
			log.beginTimer(" Begining of AcademyToteBarCodeTranslatorAPI-> getDetailsOfShipment Api");
			log.verbose("****** getDetailsOfShipment : inside method to get item details");
			Document docInputGetShipmentList = XMLUtil.createDocument("Shipment");
			
			Element eleShipment = docInputGetShipmentList.getDocumentElement();
			eleShipment.setAttribute("ShipmentKey",shipKey);

			Document outTempGetShipmentList = YFCDocument.parse("<Shipments><Shipment ShipmentKey = \"\" UnplacedQuantity=\"\" TotalQuantity = \"\"/></Shipments>").getDocument();
			env.setApiTemplate("getShipmentList", outTempGetShipmentList);
			Document docShipmentListOutput = AcademyUtil.invokeAPI(env,"getShipmentList", docInputGetShipmentList);
			log.verbose("****** Output getDetailsOfShipment document is ::" + XMLUtil.getXMLString(docShipmentListOutput));
			env.clearApiTemplates();

			//String totalShipmentQty = ((Element)docShipmentListOutput.getDocumentElement().getElementsByTagName("Shipment").item(0)).getAttribute("TotalQuantity");
			String unplacedShipmentQty = ((Element)docShipmentListOutput.getDocumentElement().getElementsByTagName("Shipment").item(0)).getAttribute("UnplacedQuantity");
			//Double TotalShipQty = Double.parseDouble(totalShipmentQty);
			Double UnplacedShipQty = Double.parseDouble(unplacedShipmentQty);
			log.endTimer(" End of AcademyToteBarCodeTranslatorAPI-> getDetailsOfShipment Api");
			return UnplacedShipQty;
		} catch (Exception e) {
			
			throw new YFSException(e.getMessage());
		}
	}
	/**
	 * Method to form the output document from the input document
	 */
	private Document getOutputDocument(Document docInput, YFSEnvironment env) {
		try {
			log.beginTimer(" Begining of AcademyToteBarCodeTranslatorAPI-> getOutputDocument Api");
			log.verbose("****** getOutputDocument : inside method to get TranslateBarCode output");
			Document docInputBarCode = XMLUtil.createDocument("BarCode");
			
			Element eleBarCode = docInputBarCode.getDocumentElement();
			Element eleContextualInfo = docInputBarCode.createElement("ContextualInfo");
			eleBarCode.appendChild(eleContextualInfo);

			eleContextualInfo.setAttribute("OrganizationCode","005");
			eleBarCode.setAttribute(AcademyConstants.ATTR_BAR_CODE_DATA,docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_BAR_CODE_DATA));
			eleBarCode.setAttribute(AcademyConstants.ATTR_BAR_CODE_TYPE,"CustomPackInitiation");
			log.verbose("****** getOutputDocument input XML :::" + XMLUtil.getXMLString(docInputBarCode));
			Document docOutputBarCode = AcademyUtil.invokeAPI(env,"translateBarCode", docInputBarCode);
			log.verbose("****** getOutputDocument output XML :::" + XMLUtil.getXMLString(docOutputBarCode));
			log.endTimer(" End of AcademyToteBarCodeTranslatorAPI-> getOutputDocument Api");
			return docOutputBarCode;
		} catch (Exception e) {
			
			throw new YFSException(e.getMessage());
		}
	}
}
