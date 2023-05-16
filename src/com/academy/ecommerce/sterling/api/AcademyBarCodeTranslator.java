package com.academy.ecommerce.sterling.api;

import java.util.Properties;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;

/**
*  Fix for BOPIS-1250 -> Scan Barcode issue.
*/
public class AcademyBarCodeTranslator {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyBarCodeTranslator.class);
	private static Properties	props;

	// Stores property configured in configurator
	
	public void setProperties(Properties props) throws Exception
	{
		this.props = props;
	}
	/**
	 * This is the main method of the class which invokes different method to return Barcode ouput from this service.
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception 
	 */


	public Document barCodeTranslator(YFSEnvironment env, Document inDoc) throws Exception {

		YFCDocument yfcDocBarCodeInput = YFCDocument.getDocumentFor(inDoc);
		YFCElement eleOutDocBarCode = yfcDocBarCodeInput.getDocumentElement();
		String strBarCodeData = eleOutDocBarCode.getAttribute(AcademyConstants.ATTR_BAR_CODE_DATA);
		YFCElement eleContextualInfo = eleOutDocBarCode.getChildElement(AcademyConstants.ELE_BAR_CONTEXT_INFO);
		//Invoke getItemList to get Item Details
		Document docGetItemListOp = getItemListOutput(env, strBarCodeData);
		//prepare Output to return from this service through getItemList Output
		Document outputDocForBarCode = prepareBarCodeOpFromGetItemList(eleContextualInfo, docGetItemListOp);

		return outputDocForBarCode;
	}

	/**
	 * @param eleContextualInfo
	 * @param docGetItemListOp
	 * 
	 * This method prepares input in Bar code format to return from this service.
	 */
	public Document prepareBarCodeOpFromGetItemList(YFCElement eleContextualInfo,
			Document docGetItemListOp) {
		YFCDocument yfcDocGetItemListOp = YFCDocument.getDocumentFor(docGetItemListOp);
		YFCElement eleRootGetItemListOp = yfcDocGetItemListOp.getDocumentElement();
		YFCElement eleItemGetItemListOp = eleRootGetItemListOp.getChildElement(AcademyConstants.ITEM);
		YFCElement elePrimaryInfo = eleItemGetItemListOp.getChildElement(AcademyConstants.ELE_PRIMARY_INFO);
		YFCDocument yfcDocBarCodeOutput = YFCDocument.createDocument(AcademyConstants.ELE_BAR_CODE);
		YFCElement eleRootBarCodeOutput = yfcDocBarCodeOutput.getDocumentElement();
		YFCElement eleTranslations = eleRootBarCodeOutput.createChild(AcademyConstants.ELE_TRANSLATIONS);
		eleTranslations.setAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS, AcademyConstants.STR_ONE);
		YFCElement eleTranslation = eleTranslations.createChild(AcademyConstants.ELE_TRANSLATION);
		YFCElement eleContextualInfoOp = eleTranslation.createChild(AcademyConstants.ELE_BAR_CONTEXT_INFO);
		YFCElement eleItemContextualInfo = eleTranslation.createChild(AcademyConstants.ELE_CONTEXT_INFO);
		eleContextualInfoOp.setAttribute(AcademyConstants.ORGANIZATION_CODE, eleItemGetItemListOp.getAttribute(AcademyConstants.ORGANIZATION_CODE));
		eleContextualInfoOp.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, eleContextualInfo.getAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE));
		eleItemContextualInfo.setAttribute(AcademyConstants.ATTR_INV_UOM, eleItemGetItemListOp.getAttribute(AcademyConstants.ATTR_UOM));
		eleItemContextualInfo.setAttribute(AcademyConstants.KIT_CODE, elePrimaryInfo.getAttribute(AcademyConstants.KIT_CODE));
		eleItemContextualInfo.setAttribute(AcademyConstants.ITEM_ID, eleItemGetItemListOp.getAttribute(AcademyConstants.ITEM_ID));
		return yfcDocBarCodeOutput.getDocument();
	}

	/**
	 * @param env
	 * @param strBarCodeData
	 * @return getItemListOutput Document
	 * @throws Exception
	 * 
	 * This method is used to prepare and call getItemList through Bar Code data sent in Input
	 */
	public Document getItemListOutput(YFSEnvironment env, String strBarCodeData)
			throws Exception {
		Document docGetItemListOp = null;
		YFCDocument yfcDocGetItemListIp = YFCDocument.createDocument(AcademyConstants.ITEM);
		YFCElement eleGetItemListRootIp = yfcDocGetItemListIp.getDocumentElement();
		YFCElement eleItemAliasList = eleGetItemListRootIp.createChild(AcademyConstants.ELE_ITEM_ALIAS_LIST);
		YFCElement eleItemAlias = eleItemAliasList.createChild(AcademyConstants.ELE_ITEM_ALIAS);
		// Fetch Service Argument values
		String appendOneZero = props.getProperty("AppendOneZero");
		String appendTwoZero = props.getProperty("AppendTwoZero");
		String strDigitLength12 = props.getProperty(AcademyConstants.NUM_OF_DIGITS_12);
		String strDigitLength13 = props.getProperty(AcademyConstants.NUM_OF_DIGITS_13);
		int iDigitLength12 = Integer.parseInt(strDigitLength12);
		int iDigitLength13 = Integer.parseInt(strDigitLength13);
		int iLengthOfBarCode = strBarCodeData.length();
		if (iLengthOfBarCode == iDigitLength12){
			String strBarCodeDataAfter14Digit = appendTwoZero + strBarCodeData;
			eleItemAlias.setAttribute(AcademyConstants.ATTR_ALIAS_VAL, strBarCodeDataAfter14Digit);
			docGetItemListOp =AcademyUtil.invokeService(env, AcademyConstants.GET_ITEM_LIST_API_FOR_BAR_CODE, yfcDocGetItemListIp.getDocument());	
		    YFCDocument yfcDocGetItemListOpAfter14Digit = YFCDocument.getDocumentFor(docGetItemListOp);
		    YFCElement yfcEleGetItemListRootAfter14Digit = yfcDocGetItemListOpAfter14Digit.getDocumentElement();
		    String strNoOfRecordsAfter14Digit = yfcEleGetItemListRootAfter14Digit.getAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS);
			if (YFCCommon.equalsIgnoreCase(strNoOfRecordsAfter14Digit, AcademyConstants.STR_ZERO)){
				String strBarCodeDataAfter13Digit = appendOneZero + strBarCodeData;
				eleItemAlias.setAttribute(AcademyConstants.ATTR_ALIAS_VAL, strBarCodeDataAfter13Digit);
				docGetItemListOp =AcademyUtil.invokeService(env, AcademyConstants.GET_ITEM_LIST_API_FOR_BAR_CODE, yfcDocGetItemListIp.getDocument());	
			    YFCDocument yfcDocGetItemListOpAfter13Digit = YFCDocument.getDocumentFor(docGetItemListOp);
			    YFCElement yfcEleGetItemListRootAfter13Digit = yfcDocGetItemListOpAfter13Digit.getDocumentElement();
			    String strNoOfRecordsAfter13Digit = yfcEleGetItemListRootAfter13Digit.getAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS);
			    if (YFCCommon.equalsIgnoreCase(strNoOfRecordsAfter13Digit, AcademyConstants.STR_ZERO)){
			    	eleItemAlias.setAttribute(AcademyConstants.ATTR_ALIAS_VAL, strBarCodeData);
					docGetItemListOp =AcademyUtil.invokeService(env, AcademyConstants.GET_ITEM_LIST_API_FOR_BAR_CODE, yfcDocGetItemListIp.getDocument());	
				    YFCDocument yfcDocGetItemListOpWith12Digit = YFCDocument.getDocumentFor(docGetItemListOp);
				    YFCElement yfcEleGetItemListRootWith12Digit = yfcDocGetItemListOpWith12Digit.getDocumentElement();
				    String strNoOfRecordsWith12Digit = yfcEleGetItemListRootWith12Digit.getAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS);
					   
				    if (YFCCommon.equalsIgnoreCase(strNoOfRecordsWith12Digit, AcademyConstants.STR_ZERO)){
				    	YFCException excep = new YFCException("No_Record_found_for_Item");
       	       		    excep.setAttribute(AcademyConstants.ATTR_ERROR_DESC,"No Record Found For Item");
       	       	          throw excep;
				    }
			    }
			}
		}else if (iLengthOfBarCode == iDigitLength13){
			String strBarCodeDataAfter14Digit = appendOneZero + strBarCodeData;
			eleItemAlias.setAttribute(AcademyConstants.ATTR_ALIAS_VAL, strBarCodeDataAfter14Digit);
			docGetItemListOp =AcademyUtil.invokeService(env, AcademyConstants.GET_ITEM_LIST_API_FOR_BAR_CODE, yfcDocGetItemListIp.getDocument());	
		    YFCDocument yfcDocGetItemListOpAfter14Digit = YFCDocument.getDocumentFor(docGetItemListOp);
		    YFCElement yfcEleGetItemListRootAfter14Digit = yfcDocGetItemListOpAfter14Digit.getDocumentElement();
		    String strNoOfRecordsAfter14Digit = yfcEleGetItemListRootAfter14Digit.getAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS);
			if (YFCCommon.equalsIgnoreCase(strNoOfRecordsAfter14Digit, AcademyConstants.STR_ZERO)){
				eleItemAlias.setAttribute(AcademyConstants.ATTR_ALIAS_VAL, strBarCodeData);
				docGetItemListOp =AcademyUtil.invokeService(env, AcademyConstants.GET_ITEM_LIST_API_FOR_BAR_CODE, yfcDocGetItemListIp.getDocument());	
			    YFCDocument yfcDocGetItemListOpWith13Digit = YFCDocument.getDocumentFor(docGetItemListOp);
			    YFCElement yfcEleGetItemListRootWith13Digit = yfcDocGetItemListOpWith13Digit.getDocumentElement();
			    String strNoOfRecordsWith13Digit = yfcEleGetItemListRootWith13Digit.getAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS);
			    if (YFCCommon.equalsIgnoreCase(strNoOfRecordsWith13Digit, AcademyConstants.STR_ZERO)){
			    	YFCException excep = new YFCException("No_Record_found_for_Item");
   	       		    excep.setAttribute(AcademyConstants.ATTR_ERROR_DESC,"No Record Found For Item");
   	       	          throw excep;
			}
		}
		}else{
			eleItemAlias.setAttribute(AcademyConstants.ATTR_ALIAS_VAL, strBarCodeData);
			docGetItemListOp =AcademyUtil.invokeService(env, AcademyConstants.GET_ITEM_LIST_API_FOR_BAR_CODE, yfcDocGetItemListIp.getDocument());	
			YFCDocument yfcDocGetItemListOp = YFCDocument.getDocumentFor(docGetItemListOp);
		    YFCElement yfcEleGetItemListRoot = yfcDocGetItemListOp.getDocumentElement();
		    String strNoOfRecords = yfcEleGetItemListRoot.getAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS);
			   
		    if (YFCCommon.equalsIgnoreCase(strNoOfRecords, AcademyConstants.STR_ZERO)){
		    	YFCException excep = new YFCException("No_Record_found_for_Item");
	       		    excep.setAttribute(AcademyConstants.ATTR_ERROR_DESC,"No Record Found For Item");
	       	          throw excep;
		    }
		}
		return docGetItemListOp;
	}

}
