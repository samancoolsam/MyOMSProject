package com.academy.ecommerce.sterling.util;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author sharon.wilson
 * 
 * It implements the logic to override the carrier for WG items.
 * It checks on ACAD_WG_CARRIER_OVERRIDE table with ZipCode, if no entry found then 
 * checks the CommonCode to get the carrier preference
 * 
 * /*EFP-17*/

public class AcademyWGCarrierOverrideUtil {

	// Set the logger
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyWGCarrierOverrideUtil.class);

	public static String findCarrierForWG(YFSEnvironment env, String strZipCode,
			String strCarrServiceCode, String strShipNode)throws Exception {

		log.beginTimer("Begin of AcademyWGCarrierOverrideUtil findCarrierForWG()");
		
		if (log.isVerboseEnabled()) {
			log.verbose("********* Inside AcademyWGCarrierOverrideUtil ::findCarrierForWG **********");
			log.verbose("********* Input XML for method findCarrierForWG :: AcademyWGCarrierOverrideUtil"+
					"ZipCode:"+strZipCode+" ShipNode:"+strShipNode+" CarrierSvcCode:"+strCarrServiceCode);
		}
						
		String strCodeType = strShipNode.trim() + "_" + "CARRIER_OVERRIDE";
		String strCarrierWG = getCarrierFromWG(env, strZipCode.trim());
		return (!YFCObject.isVoid(strCarrierWG) && 
				!YFCObject.isNull(strCarrierWG)) ? strCarrierWG
				: getCarrierFromCommonCode(env, strCodeType, strCarrServiceCode);
	}

	public static String getCarrierFromWG(YFSEnvironment env, String strZipCode)
			throws Exception {
		
		if (log.isVerboseEnabled()) {
			log.verbose("********* Inside AcademyWGCarrierOverrideUtil ::getCarrierFromWG **********");
			log.verbose("********* Input for method getCarrierFromWG :: AcademyWGCarrierOverrideUtil"+
					"ZipCode:"+strZipCode);
		}
		String strCarrierWGOveride = "";
		Document getCarrierfromAcadWGInput = XMLUtil
				.createDocument("AcadWgCarrierOverride");
		getCarrierfromAcadWGInput.getDocumentElement().setAttribute(
				AcademyConstants.ZIP_CODE, strZipCode.substring(0, 5));
		Document getCarrierfromAcadWGOutput = AcademyUtil.invokeService(env,
				"AcademyGetCarrierFromZipCode", getCarrierfromAcadWGInput);
		
		if(!YFCObject.isNull(getCarrierfromAcadWGOutput) 
				&& !YFCObject.isVoid(getCarrierfromAcadWGOutput)){
			
			strCarrierWGOveride = getCarrierfromAcadWGOutput
			.getDocumentElement().getAttribute(AcademyConstants.ATTR_SCAC);
	
			if (log.isVerboseEnabled()) {
				log.verbose("Carrier Selected is::"+strCarrierWGOveride);			
			}
		}
				
		return strCarrierWGOveride;
	}

	public static String getCarrierFromCommonCode(YFSEnvironment env,
			String CodeType, String strCarrServiceCode) throws Exception {
		
		if (log.isVerboseEnabled()) {
			log.verbose("********* Inside AcademyWGCarrierOverrideUtil ::getCarrierFromCommonCode **********");
			log.verbose("********* Input for method getCarrierFromCommonCode :: AcademyWGCarrierOverrideUtil"+
					"CodeType:"+CodeType);
		}
		
		String strCarrierFromCommonCode = "";
		Document docGetCommonCodeInput = XMLUtil.createDocument("CommonCode");
		docGetCommonCodeInput.getDocumentElement().setAttribute("CodeType",
				CodeType);
		Document docGetCommonCodeOutput = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_GET_COMMONCODE_LIST, docGetCommonCodeInput);
		
		if(!YFCObject.isVoid(docGetCommonCodeOutput) 
				&& !YFCObject.isNull(docGetCommonCodeOutput)){
			
			strCarrierFromCommonCode = XMLUtil.getAttributeFromXPath(
					docGetCommonCodeOutput, 
					"/CommonCodeList/CommonCode[@CodeValue='"+strCarrServiceCode+"']/@CodeShortDescription");
			
			if (log.isVerboseEnabled()) {
				log.verbose("Carrier From CommonCode:"+strCarrierFromCommonCode);			
			}
		}
		
		return strCarrierFromCommonCode;

	}
}
