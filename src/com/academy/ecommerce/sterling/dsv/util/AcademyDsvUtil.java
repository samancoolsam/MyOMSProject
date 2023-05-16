package com.academy.ecommerce.sterling.dsv.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * The util class for Drop Ship Vendor Project.
 * 
 * @author Manjusha V (215812)
 *
 */
public class AcademyDsvUtil {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyDsvUtil.class.getName());

	/**
	 * This method will add lead time to the value of 'DateInvoiced' attribute by validation the value of 'ShipmentType' attribute.
	 * If 
	 * 		1. ShipmentType = WG --> Lead Time will be 15. 
	 * 		2. ShipmentType = NWG --> Lead Time will be 5.
	 * 
	 * The Lead Time is configured in common codes.
	 * 
	 * After confirmShipment, the custom logic will iterate the items present in shipment lines and will stamp the value of ShipmentType
	 * attribute under <Shipment> tag as WG or NWG. If shipment contains only non-white glove item, then ShipmentType will be NWG
	 * Else, if shipment contains at least one white glove item, the ShipmentType will be WG.
	 */
	
	
	public static String getParsedInvoiedDateAfterUpdation(String strInvoicedDate,  int iLeadDays) throws Exception
	{
		log.verbose("AcademyDsvUtil.getParsedInvoiedDateAfterUpdation () starts");
		
		String strInvoicedDateSubStr="";
		String strInvoiedDateAfterUpdation="";
				
		/**
		 * If ShipmentType = "WG" Then calculate 'DateInvoiced' + 15 days and 
		 * If ShipmentType = "NWG" Then calculate 'DateInvoiced' + 5 days. 
		 * 
		 * If mix items are present, then the ShipmentType will be "WG".
		 */
		
		Date dInvoiedDateAfterUpdation = new Date ();
		
		strInvoicedDateSubStr = strInvoicedDate.substring(0, 10);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date dInvoicedDateSubStr = dateFormat.parse(strInvoicedDateSubStr); 
		
		log.verbose("DateInvoiced's date part substring: " +dInvoicedDateSubStr);
		
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(dInvoicedDateSubStr);
		calendar.add(5, iLeadDays);
		strInvoiedDateAfterUpdation = calendar.getTime().toString();
		strInvoiedDateAfterUpdation = calendar.getTime().toString();
	
		SimpleDateFormat invoicedDateInFormatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
		dInvoiedDateAfterUpdation = invoicedDateInFormatter.parse(strInvoiedDateAfterUpdation);
		log.verbose("Estimated Delivery Date: "+dInvoiedDateAfterUpdation);
		
		SimpleDateFormat invoicedDateOutFormatter = new SimpleDateFormat("yyyyMMdd");
		strInvoiedDateAfterUpdation =  invoicedDateOutFormatter.format(dInvoiedDateAfterUpdation);
		
		log.verbose("Estimated Delivery Date in yyyyMMdd format: " +strInvoiedDateAfterUpdation);
		log.verbose("AcademyDsvUtil.getParsedInvoiedDateAfterUpdation () ends");
		
		return strInvoiedDateAfterUpdation;
	}


	/**
	 * This method returns the transaction_key for a given transactionId
	 */

	public static String getTransactionDetails (YFSEnvironment env, String strTranId)
	{
		
		log.verbose("AcademyDsvUtil.getTransactionDetails () starts");
		
		String strTranKey = "";
		Document getTransactionListOutDoc =null;
		
		YFCDocument  getTransactionListInDoc =YFCDocument.createDocument("Transaction");
		YFCElement getTransactionListInEle=getTransactionListInDoc.getDocumentElement();
		getTransactionListInEle.setAttribute("Tranid", strTranId);
		
		log.verbose("input doc to getTransactionList API: --> "+getTransactionListInDoc.getString()+"<---");
		
		Document outputTemplate = YFCDocument.getDocumentFor("<TransactionList><Transaction TransactionKey='' /></TransactionList>").getDocument();
		env.setApiTemplate(AcademyConstants.DSV_GET_TRANSACTION_LIST_API, outputTemplate);
		
		try {
			getTransactionListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.DSV_GET_TRANSACTION_LIST_API, getTransactionListInDoc.getString());
		} catch (YFCException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		env.clearApiTemplate(AcademyConstants.DSV_GET_TRANSACTION_LIST_API);
		log.verbose("Output doc of getTransactionList API: --> "+XMLUtil.getXMLString(getTransactionListOutDoc)+"<---");
		
		try {
			strTranKey = XMLUtil.getString(getTransactionListOutDoc, "/TransactionList/Transaction/@TransactionKey");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		log.verbose("Transaction Key: "+strTranKey);
		log.verbose("AcademyDsvUtil.getTransactionDetails () ends");
		
		return strTranKey;
	}

	
	/**
	 * This method invokes the getCommonCodeList API for the common code 'SHP_DELVRY_DAYS'
	 * and Fetch the common code value configured for the code type 'SHP_DELVRY_DAYS' and fetch the CodeLongDescription.
	 * 
	 * Depending on the value of 'strShipmentType' argument (which is the ShipmentType attribute value at shipment level to distinguish
	 * a white glove or non-white glove shipment.), the CodeLongDescription will be returned to the caller.
	 * 
	 * CodeLongDescription is configured in common code as '15' for ShipmentType=WG and as '5' for ShipmentType=NWG.
	 */
	
	public static Integer getCommonCodeValueForCodeType (YFSEnvironment env, String strShipmentType)
	{
		log.verbose("AcademyDsvUtil.getCommonCodeValueForCodeType () starts");
		
		String strShipDeliveryDaysCodeValue ="";
		int iLeadTime = 0;
		
		Document getCommonCodeListInDoc =null;
		try {
			getCommonCodeListInDoc = XMLUtil.createDocument("CommonCode");
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		getCommonCodeListInDoc.getDocumentElement().setAttribute("CodeType", AcademyConstants.DSV_COMMONCODE_SHP_DELVRY_DAYS);
		log.verbose("getCommonCodeList input Doc: "+XMLUtil.getXMLString(getCommonCodeListInDoc)+"<----");
	
		Document getCommonCodeListOutDoc = null;
		try {
			getCommonCodeListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.DSV_GET_COMMONCODE_LIST_API, getCommonCodeListInDoc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		log.verbose("getCommonCodeList API Output Doc: " +XMLUtil.getXMLString(getCommonCodeListOutDoc)+"<.....");
	
		if(getCommonCodeListOutDoc!=null){
			YFCDocument getCommonCodeListOutYFCDoc = YFCDocument.getDocumentFor(getCommonCodeListOutDoc);
			YFCNodeList<YFCElement>  CommonCodeList = getCommonCodeListOutYFCDoc.getElementsByTagName("CommonCode");
			
			for (YFCElement CommonCodeListEle : CommonCodeList){
				strShipDeliveryDaysCodeValue = CommonCodeListEle.getAttribute("CodeValue");
				
				if ((strShipmentType.equals(AcademyConstants.DSV_SHIPMENT_TYPE_NON_WHITEGLOVE)) && 
						(AcademyConstants.DSV_COMMONCODE_VAL_NON_WHITEGLOVE.equalsIgnoreCase(strShipDeliveryDaysCodeValue))){
					iLeadTime = Integer.parseInt(CommonCodeListEle.getAttribute("CodeLongDescription"));
				}else if (((strShipmentType.equals(AcademyConstants.DSV_SHIPMENT_TYPE_WHITEGLOVE))) &&
						(AcademyConstants.DSV_COMMONCODE_VAL_WHITEGLOVE.equalsIgnoreCase(strShipDeliveryDaysCodeValue))){
					iLeadTime = Integer.parseInt(CommonCodeListEle.getAttribute("CodeLongDescription"));
				}
			}
		}
		else{
			log.verbose("getCommonCodeList Output Doc is null..");
		}
		log.verbose("Lead Time = " + iLeadTime);
		
		return iLeadTime;
	}
}
