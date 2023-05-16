package com.academy.ecommerce.sterling.bopis.batch.api;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.constants.AcademyConstants;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
/**
 * This class is used to sort batched based on CarrierServiceCode and OrderDate.
 * This class will be called from Web Store Batch list UI screen.
 * @author rastaj1
 *
 */
public class AcademyBOPISGetStoreBatchListWrapper {
	//logger
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademyBOPISGetStoreBatchListWrapper.class);
	//Define properties to fetch argument value from service configuration
	//public static HashMap<String,String> orderOrderDate = new HashMap<String, String>();
	private Properties props;

	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}
	public static Map<String, Integer> rushLosPriorityMap = new HashMap<String,Integer>();

	/**
	 * This method will be called from Web Store Batch list UI Screen. This method will sort batched based on Carrier Service Code using Java Comparator.
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception 
	 */
	public Document getSortedStoreBatchList(YFSEnvironment env, Document inDoc) throws Exception{
		log.beginTimer("AcademyBOPISGetStoreBatchListWrapper::getSortedStoreBatchList");


		//fetch the batch lines and store them in an array list

		//HashSet<String> orderNoSet = new HashSet<String>();

		final NodeList nlBatchList =
				inDoc.getElementsByTagName(AcademyConstants.ELE_STORE_BATCH);
		List<Element> batchList = new ArrayList<Element>();
		for (int i=0; i < nlBatchList.getLength(); i++) {
			Element eleBatch= (Element)nlBatchList.item(i);
			batchList.add(eleBatch);
			//			NodeList shipmentLineNL = eleBatchLine.getElementsByTagName("ShipmentLine");
			//			//NodeList shipmentLineNL = (NodeList) XMLUtil.getElementsByTagName(eleBatchLine, "ShipmentLine");
			//			int shipmentLineLen = shipmentLineNL.getLength();
			//			for(int j=0; j<shipmentLineLen;j++) {
			//				Element shipmentLineEle= (Element) shipmentLineNL.item(j);
			//				String orderNo = shipmentLineEle.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			//				orderNoSet.add(orderNo);
			//			}
			SCXmlUtil.removeNode(eleBatch);
			i--;
		}

		//setOrderDatemap(env, orderNoSet);

		log.verbose("After removing StoreBatch's ::" + SCXmlUtil.getString(inDoc));
		Document getCommonCodeListOutDoc = AcademyCommonCode.getCommonCodeList(env, AcademyConstants.RUSH_LOS_LIST_CODE_TYPE, AcademyConstants.HUB_CODE);
		NodeList rushLosList = getCommonCodeListOutDoc.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
		rushLosPriorityMap = new HashMap<String,Integer>();
		for(int i=0; i<rushLosList.getLength(); i++){
			Element eleLOS = (Element)rushLosList.item(i);
			String sLOS = eleLOS.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
			String sPriority = eleLOS.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
			rushLosPriorityMap.put(sLOS, Integer.parseInt(sPriority));
		}
		log.verbose("Map In main method::" + rushLosPriorityMap.size());
		Collections.sort(batchList, new SortBatchList());

		int length = batchList.size();
		for (int i = 0; i < length; i++) {
			Element eleBatch = batchList.get(i);

			Node eleBatchInOutDoc = inDoc.importNode(eleBatch, true);
			inDoc.getDocumentElement().appendChild(eleBatchInOutDoc);
		}

		log.endTimer("AcademyBOPISGetStoreBatchListWrapper::getSortedStoreBatchList");
		return inDoc;	
	}

	/** BOPIS-1244--Commenting below method
	 * private void setOrderDatemap(YFSEnvironment env, HashSet<String> orderNoSet)
			throws ParserConfigurationException, SAXException, IOException, Exception, XPathExpressionException {
		Iterator<String> orderIterator = orderNoSet.iterator();
		while(orderIterator.hasNext()) {
			String orderNo = orderIterator.next().toString();
			if(!YFCCommon.isVoid(orderNo)) {
				Document getOrderListInDoc = XMLUtil.getDocument("<Order OrderNo='"+orderNo+"' />");
				Document getOrderListOutDoc = AcademyUtil.invokeService(env, "AcademyBOPISGetOrderDate", getOrderListInDoc);
				String orderDate = XMLUtil.getAttributeFromXPath(getOrderListOutDoc, "OrderList/Order/@OrderDate");
				if(!YFCCommon.isVoid(orderDate)) {
					orderOrderDate.put(orderNo, orderDate);
				}
			}

		}
	} **/

	static class SortBatchList implements Comparator<Element>,Serializable {
		@Override
		public int compare(Element elem1, Element elem2) {
			log.beginTimer("SortBatchList::compare");
			String los1 = SCXmlUtil.getXpathAttribute(elem1, "ShipmentLines/ShipmentLine/Shipment/@CarrierServiceCode");
			String los2 = SCXmlUtil.getXpathAttribute(elem2, "ShipmentLines/ShipmentLine/Shipment/@CarrierServiceCode");
			log.verbose("LOS 1 is ::" + los1 + "   LOS 2 is ::" + los2);
			log.verbose("Map In Comparator::" + rushLosPriorityMap.size());
			int returnVal = 0;
			boolean compareDocumentType = false;
			if(rushLosPriorityMap.containsKey(los1) && !rushLosPriorityMap.containsKey(los2)){
				log.verbose("Only los1 is present in rush los type::LOS 1 is " + los1);
				returnVal= -1;
			} else if(!rushLosPriorityMap.containsKey(los1) && rushLosPriorityMap.containsKey(los2)){
				log.verbose("Only los2 is present in rush los type::LOS 2 is " + los2);
				returnVal= 1;
			} else if(rushLosPriorityMap.containsKey(los1) && rushLosPriorityMap.containsKey(los2)){
				log.verbose("both Los are present in rush priority map");
				int priority1 = rushLosPriorityMap.get(los1);
				int priority2 = rushLosPriorityMap.get(los2);

				if(priority1<priority2){
					log.verbose("priority of LOS 1 is more then LOS2 ");
					returnVal= -1;
				} else if(priority2<priority1){
					log.verbose("priority of LOS 2 is more then LOS2 ");
					returnVal=  1;
				} else if(priority1==priority2){
					log.verbose("Both the LOS have equal priority");
					//Change For OMNI-48478
					compareDocumentType = true;
				}
			} else {
				log.verbose("both Los are not present in rush priority map");
				compareDocumentType = true;
			}
			if(compareDocumentType){
				try {
					returnVal=  compareDocumentType(elem1, elem2);
				} catch (ParseException e) {
					log.error(e);
				}
			}
			log.verbose("return value is ::" + returnVal);
			log.endTimer("SortBatchList::end");
			return returnVal;
		}
		/*OMNI-48478 Start */
		private int compareDocumentType(Element elem1, Element elem2) throws ParseException {
			int returnVal = 0;
			String docType1 = SCXmlUtil.getXpathAttribute(elem1, "ShipmentLines/ShipmentLine/Shipment/@DocumentType");
			String docType2 = SCXmlUtil.getXpathAttribute(elem2, "ShipmentLines/ShipmentLine/Shipment/@DocumentType");
			boolean compareDates=false;
			
		if("0006".equals(docType1)&& ! ("0006").equals(docType2)) {
			returnVal= -1;
		}
		else if("0006".equals(docType2)&& ! ("0006").equals(docType1)) {
			return 1;
		}
		else {
			compareDates = true;
		}
		if(compareDates){
			try {
				returnVal=  compareOrderDate(elem1, elem2);
			} catch (ParseException e) {
				log.error(e);
			}
		}
		return returnVal;
		}
		/*OMNI-48478 End */
		
		private int compareOrderDate(Element elem1, Element elem2) throws ParseException {
			log.beginTimer("SortBatchList::compareOrderDate");
			int returnVal = 0;
			long time1 = getOldestOrderDateTimeInBatch(elem1);
			long time2 = getOldestOrderDateTimeInBatch(elem2);
			log.verbose("time1 is "+ time1+"   time2 is "+time2);

			if(time1<time2){
				log.verbose("Order for first Batch is older then second");
				returnVal=-1;
			} else if(time1>time2){
				log.verbose("Order for second Batch is older then first");
				returnVal=1;
			} else{
				returnVal = 0;
			}
			log.verbose("return value from compareOrderDate is " + returnVal);
			log.endTimer("SortBatchList::compareOrderDate");
			return returnVal;
		}

		private long getOldestOrderDateTimeInBatch(Element elem2) throws ParseException {
			log.beginTimer("SortBatchList::getOldestOrderDateTimeInBatch");
			NodeList shipmentLineNl = elem2.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			long minVal = 0;
			for(int i=0;i<shipmentLineNl.getLength();i++){
				Element eleShipmentLine = (Element)shipmentLineNl.item(i);
				//String orderNo = eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_NO);
				//log.verbose("OrderNo is " + orderNo);
				String orderDate = SCXmlUtil.getChildElement(eleShipmentLine, AcademyConstants.ELE_EXTN).getAttribute("ExtnOrderDate");
				//orderOrderDate.get(orderNo);
				if(!YFCCommon.isVoid(orderDate)){
					Date date1 = simpleDateFormat.parse(orderDate);
					Calendar cal1 = Calendar.getInstance();
					cal1.setTime(date1);
					long time = cal1.getTimeInMillis(); 
					if(i==0){
						minVal=time;
					}
					if(time < minVal){
						minVal = time;
					}
				}
			}

			log.verbose("Oldest time in a batch is " + minVal);

			log.endTimer("SortBatchList::getOldestOrderDateTimeInBatch");
			return minVal;
		}
	}
}
