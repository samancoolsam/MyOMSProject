package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
/**
 * Added as part og GCD-45 to validate 16 digt gift cards during packing process.
 * @author C0007319
 *
 */
public class AcademyBarCodeTranslatorForGCSerialNo implements YIFCustomApi{
		  /**
	   * Instance of logger
	   */
	  private static YFCLogCategory log = YFCLogCategory.instance(AcademyBarCodeTranslatorForGCSerialNo.class);
	  /**
	   * method validates the bar code for 13 and 16 digit and call OOB DummySerialTranslator
	   * @param env
	   * @param inDoc
	   * @return
	   * @throws Exception
	   */
	  /*<BarCode BarCodeData="1234567890123456" BarCodeType="SerialScan" MaxTranslations="0">
    <BarCodeTranslation AliasName="" BarCodeEndPosition="16"
        BarCodeLength="16" BarCodeStartPosition="1"
        BarCodeTranslationKey="201607121450432374788788"
        BarCodeTranslationSource="ExternalSource"
        BarCodeType="SerialScan" Description="DummySerialTranslator_16"
        FlowKey="201607191121512374802774" OrganizationCode="005"
        TranslationSequence="3" VariableLengthFlag="N"/>
    <ContextualInfo EnterpriseCode="Academy_Direct" OrganizationCode="005"/>
    <ContainerContextualInfo CaseId="00000000001020955011"/>
    <ItemContextualInfo InventoryUOM="EACH" ItemID="019588367"/>
    <LocationContextualInfo LocationId="PACKLOC" StationId="PACK23"/>
    <ShipmentContextualInfo ShipNode="005"
        ShipmentKey="201607141218222374792282" ShipmentNo="102940260"/>
</BarCode>

	   */
	  public Document translateBarCode(YFSEnvironment env, Document inDoc) throws Exception {
		  log.beginTimer(" Begining of AcademytranslateBarCode-> translateBarCode Api");
		  log.verbose("Input to AcademyBarCodeTranslatorForGCSerialNo.translateBarCode()"+XMLUtil.getXMLString(inDoc));
		  if(!YFCObject.isVoid(inDoc)){
				  return getBarCodeTranslatorOutput(inDoc);
			  }			  
		  return inDoc;
		  }
		
	  /**
	   * Returns dummyTranslator output.
	   * @param inDoc
	   * @return
	 * @throws Exception 
	   */
	  
	  private Document getBarCodeTranslatorOutput(Document inDoc) throws Exception {
		  log.verbose("Inside getBarCodeTranslatorOutput()");
		  Document barCodeTranslatorOutput = XMLUtil.createDocument("BarCode");	
			Element barCodeTranslatorOutputEle = barCodeTranslatorOutput.getDocumentElement();
			barCodeTranslatorOutputEle.setAttribute("BarCodeData", inDoc.getDocumentElement().getAttribute("BarCodeData"));
			barCodeTranslatorOutputEle.setAttribute("BarCodeType","SerialScan");
			NodeList inputNodes = inDoc.getDocumentElement().getChildNodes();
			Element translationsEle = barCodeTranslatorOutput.createElement("Translations");
			translationsEle.setAttribute("BarCodeTranslationSource", "DummySerialTranslator");
			translationsEle.setAttribute("TotalNumberOfRecords", "1");
			barCodeTranslatorOutputEle.appendChild(translationsEle);
			Element translationEle = barCodeTranslatorOutput.createElement("Translation");
			translationsEle.appendChild(translationEle);
			
			for(int i=0;i<inputNodes.getLength();i++){
				Element childNode = (Element)inputNodes.item(i);
				 Element newChildEle = barCodeTranslatorOutput.createElement(childNode.getNodeName());
				 if(!newChildEle.getNodeName().equalsIgnoreCase("BarCodeTranslation")){
				XMLUtil.copyElement(barCodeTranslatorOutput, childNode,newChildEle);
				translationEle.appendChild(newChildEle);}
			}
			Element itemContexInfo = (Element)translationEle.getElementsByTagName("ItemContextualInfo").item(0);
			itemContexInfo.setAttribute("Quantity","1");
			Element inventoryEle = barCodeTranslatorOutput.createElement("Inventory");
			itemContexInfo.appendChild(inventoryEle);
			Element serialDetailEle = barCodeTranslatorOutput.createElement("SerialDetail");
			serialDetailEle.setAttribute("SerialNo", barCodeTranslatorOutputEle.getAttribute("BarCodeData"));
			inventoryEle.appendChild(serialDetailEle);
			log.verbose("Returning o/p::"+XMLUtil.getXMLString(barCodeTranslatorOutput));
			return barCodeTranslatorOutput;
		}

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
