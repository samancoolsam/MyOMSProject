package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySFSTranslateBarCodeForSOM {
	
	//log set up
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademySFSTranslateBarCodeForSOM.class);
	private static Properties	props;

	// Stores property configured in configurator
	public void setProperties(Properties props) throws Exception
	{
		this.props = props;
	}
	public  Document callToTranslatebarCode(YFSEnvironment env, Document docTranslatebarCodeInput) throws Exception{
		
		Element rootElement = null;
		Element outRootElement = null;
		Element eleTranslation = null;
		String strBarCodeData = "";
		String strItemDigit1 = "";
		//String strItemDigit2 = "";
		String appendValue = "";
		String appendValue1 = "";
		String barCodeDataWithOneZeroAppend = "";
		String barCodeDataWithTwoZeroAppend = "";
		Document docInputTranslatebarCode = null;
		Document docOutputTranslatebarCode = null;
		Document docInputTranslatebarCodeThirteenDigit = null;
		Document docOutputTranslatebarCodeThirteenDigit = null;
		Document docInputTranslatebarCodeFourteenDigit = null;
		Document docOutputTranslatebarCodeFourteenDigit = null;
		
		rootElement = docTranslatebarCodeInput.getDocumentElement();
		strBarCodeData = rootElement.getAttribute("BarCodeData");
		strItemDigit1 = props.getProperty("ItemDigit1");
		int itemDigit = Integer.parseInt(strItemDigit1);
		//strItemDigit2 = props.getProperty("ItemDigit2");
		appendValue = props.getProperty("12_TO_13_BARCODE");
		appendValue1 = props.getProperty("12_TO_14_BARCODE");
		//if the item alias value is >12 or if it is  a 13 digit value
		//the below if condition will be executed
		
		int barCodeDigit = strBarCodeData.length();
	
		if (barCodeDigit == itemDigit){
			log.verbose("BarCode length is 12");
			barCodeDataWithOneZeroAppend = appendValue + strBarCodeData;
			log.verbose("Barcode data prefixed with 0 :: " + barCodeDataWithOneZeroAppend);
			rootElement.setAttribute("BarCodeData", barCodeDataWithOneZeroAppend);	
			docInputTranslatebarCodeThirteenDigit = XMLUtil.getDocumentForElement(rootElement);
			log.verbose("Input to translateBarcode with 13 digit"+ XMLUtil.getXMLString(docInputTranslatebarCodeThirteenDigit) );
			docOutputTranslatebarCodeThirteenDigit = AcademyUtil.invokeAPI(env, "translateBarCode", docInputTranslatebarCodeThirteenDigit);
			log.verbose("Output from translateBarcode with 13 digit"+ XMLUtil.getXMLString(docOutputTranslatebarCodeThirteenDigit) );
			//if the item alias value is >13 or if it is  a 14 digit value
			//the below if condition will be executed
			outRootElement = docOutputTranslatebarCodeThirteenDigit.getDocumentElement();
			eleTranslation = (Element) outRootElement.getElementsByTagName("Translations").item(0);
			String strTotalNumberOfRecords = eleTranslation.getAttribute("TotalNumberOfRecords");
			int totalNoOfRcds = Integer.parseInt(strTotalNumberOfRecords);
			if (totalNoOfRcds == 0){
				//String strOutBarCodeData = outRootElement.getAttribute("BarCodeData");
				barCodeDataWithTwoZeroAppend = appendValue1 + strBarCodeData ;
				log.verbose("Barcode data prefixed with 00 ::" + barCodeDataWithTwoZeroAppend);
				rootElement.setAttribute("BarCodeData", barCodeDataWithTwoZeroAppend);	
				docInputTranslatebarCodeFourteenDigit = XMLUtil.getDocumentForElement(rootElement);	
				log.verbose("Input to translateBarcode with 14 digit"+ XMLUtil.getXMLString(docInputTranslatebarCodeFourteenDigit) );
				docOutputTranslatebarCodeFourteenDigit = AcademyUtil.invokeAPI(env, "translateBarCode", docInputTranslatebarCodeFourteenDigit);
				log.verbose("Output from translateBarcode with 14 digit"+ XMLUtil.getXMLString(docOutputTranslatebarCodeFourteenDigit) );
				return docOutputTranslatebarCodeFourteenDigit ;
			}
			return docOutputTranslatebarCodeThirteenDigit ;
		}		
		
		else
			{
			docOutputTranslatebarCode = AcademyUtil.invokeAPI(env, "translateBarCode", docTranslatebarCodeInput);
			}
		
		return docOutputTranslatebarCode ;
		
	}
}
