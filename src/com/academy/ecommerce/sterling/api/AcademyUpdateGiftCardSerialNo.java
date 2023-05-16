package com.academy.ecommerce.sterling.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;

public class AcademyUpdateGiftCardSerialNo implements YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyUpdateGiftCardSerialNo.class);
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}
	/**
	 *  This method processes the serial no of each gift card and update it with 13th digit
	 *  before to send ISD  
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public static Document processGCSerialNo(Document inDoc) throws Exception{
		if(YFCObject.isVoid(inDoc))
			return inDoc;
		log.verbose(" Bigining of AcademyUpdateGiftCardSerialNo ->  processGCSerialNo() API \n Input is : "+XMLUtil.getXMLString(inDoc));
		Element docElement = inDoc.getDocumentElement();

		NodeList tagSerialLst = docElement.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_TAG_SERIAL);
		if(tagSerialLst.getLength() > 0){
			log.verbose("element ShipmentTagSerial list size is : "+tagSerialLst.getLength());
			for(int i=0; i<tagSerialLst.getLength(); i++){
				Element eleTagSerial =  (Element)tagSerialLst.item(i);
				String serialNo = eleTagSerial.getAttribute(AcademyConstants.ATTR_SERIAL_NO);
				log.verbose("Serial No is : "+serialNo);
				if(YFCObject.isVoid(serialNo))
					continue;
				serialNo = getGCSerialNoWithCheckDigit(serialNo);
				eleTagSerial.setAttribute(AcademyConstants.ATTR_SERIAL_NO, serialNo);
							
			}
		}
		log.verbose(" OutDoc : "+XMLUtil.getXMLString(inDoc)+"\n End of AcademyUpdateGiftCardSerialNo ->  processGCSerialNo() API");
		return inDoc;
	}
	
	/*This method is for getting the GC list from a order for GC search screen
	 * This method is separated from the above because the functionality is different
	 * This is introduced as part of Bug # 4876*/
	
	public static Document getGCSerialNoList(Document inDoc) throws Exception{
		if(YFCObject.isVoid(inDoc))
			return inDoc;
		
		Document giftCardListDoc = XMLUtil.createDocument("ShipmentTagSerialList");
		Element eleGiftCardList = giftCardListDoc.getDocumentElement();
				
		log.verbose(" Bigining of AcademyUpdateGiftCardSerialNo ->  processGCSerialNo() API \n Input is : "+XMLUtil.getXMLString(inDoc));
		Element docElement = inDoc.getDocumentElement();
		NodeList tagSerialLst = docElement.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_TAG_SERIAL);
		
		String unitCost = "";
		NodeList tagOrderLineLst = docElement.getElementsByTagName("OrderLine");
		
		if(tagSerialLst.getLength() > 0){
			log.verbose("element ShipmentTagSerial list size is : "+tagSerialLst.getLength());
			for(int i=0; i<tagSerialLst.getLength(); i++){
				Element eleTagSerial =  (Element)tagSerialLst.item(i);
				String serialNo = eleTagSerial.getAttribute(AcademyConstants.ATTR_SERIAL_NO);
				log.verbose("Serial No is : "+serialNo);
				if(YFCObject.isVoid(serialNo))
					continue;
				serialNo = getGCSerialNoWithCheckDigit(serialNo);
				eleTagSerial.setAttribute(AcademyConstants.ATTR_SERIAL_NO, serialNo);
				
				//Fix to display amount in gc_list.jsp
				String strShipmentLineKey = eleTagSerial.getAttribute("ShipmentLineKey");				
				if(tagOrderLineLst.getLength() > 0)
				{
					for(int j=0; j<tagOrderLineLst.getLength(); j++)
					{
						Element eleOrderLine =  (Element)tagOrderLineLst.item(j);
						Element eleShipmentLine = (Element)eleOrderLine.getElementsByTagName("ShipmentLine").item(0);
						String strShipmentLineKey1 = eleShipmentLine.getAttribute("ShipmentLineKey");
						if(strShipmentLineKey1.equals(strShipmentLineKey))
						{
							Element eleItem = (Element)eleOrderLine.getElementsByTagName("Item").item(0);
							unitCost = eleItem.getAttribute("UnitCost");
						}
					}
				}
				eleTagSerial.setAttribute(AcademyConstants.ATTR_UNIT_COST, unitCost);
				// end of fix to display amount in gc_list.jsp
				
				Node tagSerialNode = giftCardListDoc.importNode(eleTagSerial, true);
				eleGiftCardList.appendChild(tagSerialNode);
				
			}
		}
		log.verbose(" OutDoc : "+XMLUtil.getXMLString(inDoc)+"\n End of AcademyUpdateGiftCardSerialNo ->  processGCSerialNo() API");
		return giftCardListDoc;
	}
	
	 /**
     * 
     * @param strGCSerialNo
     * @return
     */
    public static String getGCSerialNoWithCheckDigit(String strGCSerialNo){
    	log.verbose("Bigining of AcademyUpdateGiftCardSerialNo -> getGCSerialNoWithCheckDigit() API.....");
    	log.verbose("Gift Card Serial No is : "+strGCSerialNo);
    	int serialLength = strGCSerialNo.length();
    	log.verbose("The length of Serial is : "+serialLength);		
    	if(serialLength == AcademyConstants.GC_SERIAL_SIZE-1){
    		int evenTotal = 0, oddTotal = 0;  		
    		for(int i=0; i< serialLength; i++){
    			if(i%2 == 0){
    				evenTotal+=Integer.parseInt(strGCSerialNo.substring(i, i+1));
    			}else{
    				oddTotal+=Integer.parseInt(strGCSerialNo.substring(i, i+1));
    			}
    		}
    		log.verbose("total of digits at even position is "+evenTotal+"\n total of digits at odd position is "+oddTotal);
    		/**
    		 * Formula to calculate total of EAN13 is
    		 * formulaTotal = total of digits at even position + (total of digits at odd position * 3)
    		 * EAN13Digit  = 10 - digit at one's position from 'formulaTotal'
    		 * 
    		 */
    		int total = evenTotal+(oddTotal*3);
    		log.verbose(" formula of EAN13 total is "+total);
    		String rightDigit = Integer.toString(total);
    		rightDigit = rightDigit.substring(rightDigit.length()-1);    		
    		log.verbose(" digit at one's position is "+rightDigit);    		
    		int lastdigit = 0;
    		if(Integer.parseInt(rightDigit) != 0)
    			lastdigit = 10-Integer.parseInt(rightDigit);
    		log.verbose(" check digit of SerialNo is "+lastdigit);
    		strGCSerialNo = strGCSerialNo+Integer.toString(lastdigit);
    		
    	}
    	log.verbose("Serila No to ISD is "+strGCSerialNo+"\n End of AcademyUpdateGiftCardSerialNo -> getGCSerialNoWithCheckDigit() API");
    	return strGCSerialNo;
    }
	/*public static void main(String args[]){
		try{
			Document bulkGCActivation = YFCDocument.getDocumentFor(new File("C://GCActivation.xml")).getDocument();
			AcademyUpdateGiftCardSerialNo.processGCSerialNo(bulkGCActivation);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}*/
}

