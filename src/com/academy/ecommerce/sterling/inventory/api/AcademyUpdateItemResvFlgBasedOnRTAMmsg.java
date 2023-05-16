package com.academy.ecommerce.sterling.inventory.api;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.xml.XMLUtil;

public class AcademyUpdateItemResvFlgBasedOnRTAMmsg {
	
	private Properties props;

	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyUpdateItemResvFlgBasedOnRTAMmsg.class);
	
	public  Document updateItemResvFlgBasedOnRTAMmsg(YFSEnvironment env, Document inXML) throws Exception {
		
		double dGlobalLevelInv=0.00;
		double dWatermarkLevelInv=0.00;
		double dMultiplicationFactor=0.00;
		double dThresholdLevelInv=0.00;
		boolean bSkipWatermarkCalculation=false;
		Document outDoc=null;
		
		log.verbose("Input XML ::"+XMLUtil.getXMLString(inXML));
		
		dMultiplicationFactor=Double.parseDouble(props.getProperty("MULTIPLICATION_FACTOR"));
		//adding loginfo flag
		String sLogInfoFlag = props.getProperty("LogInfoFlag");
		
		Document inDocgetItemList=XMLUtil.createDocument("Item");
		Element elegetItemList=inDocgetItemList.getDocumentElement();
		elegetItemList.setAttribute("ItemID", inXML.getDocumentElement().getAttribute("ItemID"));
		elegetItemList.setAttribute("ProductClass", inXML.getDocumentElement().getAttribute("ProductClass"));
		elegetItemList.setAttribute("UnitOfMeasure", inXML.getDocumentElement().getAttribute("UnitOfMeasure"));
		
		Document outDocgetItemList=AcademyUtil.invokeService(env, "AcademygetItemList", inDocgetItemList);
		Element eleExtn=(Element) outDocgetItemList.getDocumentElement().getElementsByTagName("Extn").item(0);
		if(!YFCObject.isNull(eleExtn)){
		String strExtnWatermark=eleExtn.getAttribute("ExtnWatermark");
		String strExtnEcommerceCode=eleExtn.getAttribute("ExtnEcommerceCode");
		String strExtnPromClear=eleExtn.getAttribute("ExtnPromClear");
		String strExtnPromEndDate=eleExtn.getAttribute("ExtnPromEndDate");
		//Adding flag variable
		String strExtnReserveInv=eleExtn.getAttribute("ExtnReserveInv");
		
		if(!StringUtil.isEmpty(strExtnWatermark)){
			dWatermarkLevelInv=Double.parseDouble(strExtnWatermark);
		}
		
		if(!StringUtil.isEmpty(strExtnEcommerceCode) && strExtnEcommerceCode.equalsIgnoreCase("04")){
			bSkipWatermarkCalculation=true;
		}
		else if(!StringUtil.isEmpty(strExtnPromClear) && strExtnPromClear.equalsIgnoreCase("Y") && !StringUtil.isEmpty(strExtnPromEndDate)) {
			
			SimpleDateFormat newDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		  	
		  	Date sysDate = new Date();
		  	String strcurDate = newDateFormat.format(sysDate);
		  	
		  	Date reqDateFormat=newDateFormat.parse(strcurDate); 
		  	Date dtExtnPromEndDate = newDateFormat.parse(strExtnPromEndDate);
		  	 
		  	int result=dtExtnPromEndDate.compareTo(reqDateFormat);
		  	log.verbose("Date Comparision Result:"+result);
		  	if(result>=0) {
		  		bSkipWatermarkCalculation=true;
		  	}
		  	else {
		  		bSkipWatermarkCalculation=false;
		  	}
			
		}
		
		Document inDocmanageItem=XMLUtil.createDocument("ItemList");
		Element elemanageItem=inDocmanageItem.getDocumentElement();
		Element eleItem=inDocmanageItem.createElement("Item");
		eleItem.setAttribute("Action", "Modify");
		eleItem.setAttribute("ItemID", inXML.getDocumentElement().getAttribute("ItemID"));
		eleItem.setAttribute("ProductClass", inXML.getDocumentElement().getAttribute("ProductClass"));
		eleItem.setAttribute("UnitOfMeasure", inXML.getDocumentElement().getAttribute("UnitOfMeasure"));
		eleItem.setAttribute("OrganizationCode", "DEFAULT");
		Element eleExtnmanageItem=inDocmanageItem.createElement("Extn");
		
		if(bSkipWatermarkCalculation) {
			eleExtnmanageItem.setAttribute("ExtnReserveInv", "Y");
		}
		else{
		if(dWatermarkLevelInv > 0){	
			dThresholdLevelInv=dWatermarkLevelInv * dMultiplicationFactor;
		}
		
		Element eleAvailabilityChange=(Element) inXML.getDocumentElement().getElementsByTagName("AvailabilityChange").item(0);	
		if(!YFCObject.isNull(eleAvailabilityChange)){
			String strOnhandAvailableQuantity=eleAvailabilityChange.getAttribute("OnhandAvailableQuantity");
			if(!StringUtil.isEmpty(strOnhandAvailableQuantity)){
				dGlobalLevelInv=Double.parseDouble(strOnhandAvailableQuantity);	
			}
		}
		log.verbose("RTAM Inv:Threshold Calculated Inv"+dGlobalLevelInv+":"+dThresholdLevelInv);
		
			
			if(dGlobalLevelInv > dThresholdLevelInv){
			eleExtnmanageItem.setAttribute("ExtnReserveInv", "N");
			}
			else{
				eleExtnmanageItem.setAttribute("ExtnReserveInv", "Y");	
			}
			
		}
			eleItem.appendChild(eleExtnmanageItem);
			elemanageItem.appendChild(eleItem);
			
			log.verbose("manageItem Input XML::"+XMLUtil.getXMLString(inDocmanageItem));
			//adding code to avoid calling of manageItem multilple times
			if(!StringUtil.isEmpty(strExtnReserveInv)&& !(eleExtnmanageItem.getAttribute("ExtnReserveInv").equalsIgnoreCase(strExtnReserveInv))){
				//Adding below for loginfo				
				if (sLogInfoFlag.equals("Y")) {
					log.info("ItemID: "+ inXML.getDocumentElement().getAttribute("ItemID")+ 
							" ReserveInv flag: " + eleExtnmanageItem.getAttribute("ExtnReserveInv") +
							" ExtnWatermark : " + strExtnWatermark + 
							" Current time stamp : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				}
				
				outDoc=AcademyUtil.invokeAPI(env, "manageItem", inDocmanageItem);				
			}
			
			
		}
		

		return outDoc;
		
	}

}
