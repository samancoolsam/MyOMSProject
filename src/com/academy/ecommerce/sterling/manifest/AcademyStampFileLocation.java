package com.academy.ecommerce.sterling.manifest;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyStampFileLocation implements YIFCustomApi {
	private Properties props;
	private Element eleContainer = null;

	public void setProperties(Properties props) {
		this.props = props;
	}

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyStampFileLocation.class);

	public Document stampFileLocation(YFSEnvironment env, Document inDoc)
			throws Exception {
		log.beginTimer(" Begining of AcademyStampFileLocation-> stampFileLocation Api");
		Element inDocElem = inDoc.getDocumentElement();
		eleContainer = (Element) XMLUtil.getNode(inDocElem,"EnvironmentDocument/Container");
		Element eleShipment = XMLUtil.getFirstElementByName(inDocElem,"EnvironmentDocument/Container/Shipment");
		if (!YFCObject.isVoid(eleShipment)) {
			if(isWGCarrier(env, eleShipment.getAttribute("SCAC"))){
				eleContainer.setAttribute("PrintDocumentId", "ACAD_SHIPPING_WHITEGLOVE_LABEL");
			}else{
				String sFolderPath = props.getProperty("Folder_Location");
				String sImageLocation = props.getProperty("Image_Location");
				eleContainer.setAttribute("PrintDocumentId", "ACAD_PACKSLIP_BULK");
				String sContainerNo = eleContainer.getAttribute("ContainerNo");
				//eleContainer.setAttribute("FileLocation", sFolderPath + sContainerNo + ".png");
				eleContainer.setAttribute("FileLocation", sFolderPath + sContainerNo);
				eleContainer.setAttribute("CarrierLabel", sImageLocation + sContainerNo);
			}
		}
		
		// stamp correct printdocument id.
		checkGiftInstruction(env, eleContainer);
			
		// call common code and stamp marketinglines
		stampMarketingLines(env, eleContainer);
		log.endTimer(" End of AcademyStampFileLocation-> stampFileLocation Api");
		return inDoc;

	}

	private void checkGiftInstruction(YFSEnvironment env, Element inDocElem)
			throws Exception {
		log.beginTimer(" Begining of AcademyStampFileLocation-> checkGiftInstruction Api");
		if (!YFCObject.isVoid(inDocElem)) {
			Element Shipment = XMLUtil.getFirstElementByName(inDocElem,
					"Shipment");
			if (!YFCObject.isVoid(Shipment)) {
				Element Instructions = XMLUtil.getFirstElementByName(Shipment,
						"Instructions");
				if (!YFCObject.isVoid(Instructions)) {
					NodeList InstructionList = XMLUtil.getNodeList(
							Instructions, "Instruction");
					if (!YFCObject.isVoid(InstructionList)) {
						int iLen = InstructionList.getLength();
						for (int k = 0; k < iLen; k++) {
							Element Instruction = (Element) InstructionList
									.item(k);
							String InstuctionType = Instruction
									.getAttribute("InstructionType");
							if ("GIFT".equals(InstuctionType)) {
								inDocElem.setAttribute("IsGiftMessageShipment",
										"Y");
								if(isWGCarrier(env, Shipment.getAttribute("SCAC"))){
									inDocElem.setAttribute("PrintDocumentId",
									"ACAD_SHIPPING_WHITEGLOVE_GIFT");
								}else{
									inDocElem.setAttribute("PrintDocumentId",
									"ACAD_PACKSLIP_BULK_GIFT");
								}
							}
						}
					}
				}
			}
		}
		log.endTimer(" End of AcademyStampFileLocation-> checkGiftInstruction Api");
	}

	public void stampMarketingLines(YFSEnvironment env, Element inDocElem)
			throws Exception {
		log.beginTimer(" Begining of AcademyStampFileLocation-> stampMarketingLines Api");
		// TODO Auto-generated method stub
		String MarketingLine1 = "";
		String MarketingLine2 = "";
		Document inCommonCodeDoc = XMLUtil.createDocument("CommonCode");
		Element inComElem = inCommonCodeDoc.getDocumentElement();
		inComElem.setAttribute("CodeType", "ACA_MARK_LINES");
		Document outCommDoc = AcademyUtil.invokeAPI(env, "getCommonCodeList",
				inCommonCodeDoc);

		if (!YFCObject.isVoid(outCommDoc)) {
			Element outCommElem = outCommDoc.getDocumentElement();
			NodeList CommonCodeList = XMLUtil.getNodeList(outCommElem,
					"CommonCode");
			if (!YFCObject.isVoid(CommonCodeList)) {
				int iLength2 = CommonCodeList.getLength();
				for (int k = 0; k < iLength2; k++) {
					Element CommonCode = (Element) CommonCodeList.item(k);
					String codevalue = CommonCode.getAttribute("CodeValue");
					if ("Line1".equals(codevalue)) {
						MarketingLine1 = CommonCode
								.getAttribute("CodeShortDescription");
					} else {
						MarketingLine2 = CommonCode
								.getAttribute("CodeShortDescription");
					}
				}
			}
			if (!YFCObject.isVoid(inDocElem)) {
				inDocElem.setAttribute("MarketingLine1", MarketingLine1);
				inDocElem.setAttribute("MarketingLine2", MarketingLine2);
			}
		}
		log.endTimer(" End of AcademyStampFileLocation-> stampMarketingLines Api");
	}
	private boolean isWGCarrier(YFSEnvironment env, String curScac) throws Exception{
		Document docWGSCACLst = getWhiteGloveScacList(env);			
		if(docWGSCACLst != null){
			Node nCommonCode = XPathUtil.getNode(docWGSCACLst, "CommonCodeList/CommonCode[@CodeValue='" + curScac + "']");
			if(nCommonCode != null)
				return true;	
		}
		return false;
	}

	private Document getWhiteGloveScacList(YFSEnvironment env) throws Exception{
		Document docWGSCACLst = null;
		try{
		Document docScacCodeInput = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		docScacCodeInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.STR_WG_SCAC_CODE);
		docScacCodeInput.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		env.setApiTemplate(AcademyConstants.API_GET_COMMON_CODELIST, "global/template/api/getCommonCodeList.IsWhiteGloveSCAC.xml");
		docWGSCACLst = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_COMMON_CODELIST,docScacCodeInput);
		env.clearApiTemplate(AcademyConstants.API_GET_COMMON_CODELIST);
		}catch(Exception e){
			e.printStackTrace();
			log.verbose("Failed to invoke getCommonCodeList API : "+e);
			throw e;
		}
		return docWGSCACLst;
	}

}