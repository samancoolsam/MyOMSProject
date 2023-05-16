package com.academy.ecommerce.sterling.sts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;

/*
 * This is class is invoked from WEB SOM UI on the load of "Detailed Receiving Report screen" 
 * to get the count of scanned containers in the batch and also gives the details of any
 * partially received containers in that batch
 * On display of count in the UI updating the ClosedFlag to Y in the EXTN_STS_CONTAINERS
 * table 
 */

public class AcademySTSScannedContainerList implements YIFCustomApi  {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSScannedContainerList.class);
	private static YIFApi api = null;
	private Properties properties;
	HashMap<String, String> hmContaineTOOrders = new HashMap<String, String>();

	static {
		try {
			api = YIFClientFactory.getInstance().getApi();
		} catch (YIFClientCreationException e) {
			e.printStackTrace();
		}
	}

	/*
	 * This method is used to get count of scanned containers and 
	 * PartiallyReceived containers count in the batch
	 * 
	 * @param YFSEnvironment env
	 * @param Document inXML
	 * @throws Exception
	 */
	public Document getScannedContainerCount(YFSEnvironment env, Document inXML) throws Exception {
		Document ContainersListOutput = null;
		Element eleInXML = inXML.getDocumentElement();
		String strBatchNoTemp = "";
		int iBatchCount = 0;
		String strContainerTemp="";
		Integer iContainercount = 0;
		
		String strNoofHours = properties.getProperty(AcademyConstants.ATTR_No_Of_HOURS);
		NodeList nlContainers = eleInXML.getElementsByTagName(AcademyConstants.ELE_ACAD_STS_TO_CONTAINERS);
		for (int index = 0; index < nlContainers.getLength(); index++) {
			Element eleAcadSTSContainers = (Element) nlContainers.item(index);
			String strBatchNo = eleAcadSTSContainers.getAttribute(AcademyConstants.BATCH_NO);
			String strShipmentKey = eleAcadSTSContainers.getAttribute(AcademyConstants.SHIPMENT_KEY);
			String strOrderNo = eleAcadSTSContainers.getAttribute(AcademyConstants.ATTR_TO_ORDERNO);
			String strContainerNo = eleAcadSTSContainers.getAttribute(AcademyConstants.ATTR_CONTAINER_NO);
			String strCurrentTimeStamp = null;
			SimpleDateFormat sdfDateFormat = null;
			Date dtCreateHourTime = null;
			Date dtCurrentDate = null;
			String strCreatets = null;
			String strCreatedTime=null;
			String strModifiedTime= null;
			int  iHours = 0;
			Calendar cal = null;
			strCreatets = eleAcadSTSContainers.getAttribute(AcademyConstants.ATTR_CREATETS);
				try {
					cal = Calendar.getInstance();
					sdfDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
					log.verbose(sdfDateFormat.format(cal.getTime()));
					if(!YFCObject.isVoid(strNoofHours))
					{
						iHours =Integer.parseInt(strNoofHours);
					}
					log.verbose("Hours: " + iHours);
					cal.add(Calendar.HOUR, -iHours);
					strModifiedTime = sdfDateFormat.format(cal.getTime());
					log.verbose("strModifiedTime :: " + strModifiedTime);
					dtCurrentDate = sdfDateFormat.parse(strModifiedTime);
					log.verbose("Modified Date: " + dtCurrentDate);

					// Set the shipment createts to Calendar
					cal.setTime(sdfDateFormat.parse(strCreatets));

					strCreatedTime = sdfDateFormat.format(cal.getTime());
					log.verbose("StrCreatedTime :: " + strCreatedTime);
					dtCreateHourTime = sdfDateFormat.parse(strCreatedTime);
					log.verbose(strCreatedTime);
					log.verbose("Created Date:: " + dtCreateHourTime);
					if (dtCreateHourTime.after(dtCurrentDate)) {
						iContainercount++;
						if(!YFCObject.isVoid(strContainerNo) && !YFCObject.isVoid(strOrderNo))
						{
							if(hmContaineTOOrders.containsKey(strOrderNo))
							{
								strContainerTemp = strContainerTemp +"," +strContainerNo;
			                    log.verbose(strContainerTemp);
			                    String strContainerNoOld = hmContaineTOOrders.get(strOrderNo);
			                    hmContaineTOOrders.replace(strOrderNo, strContainerNoOld, strContainerTemp);
							}
							else
							{
								strContainerTemp = strContainerNo;
								hmContaineTOOrders.put(strOrderNo, strContainerNo);
							}
							
						} 
						
					} else if (dtCreateHourTime.equals(dtCurrentDate)) {
						iContainercount++;
						if(!YFCObject.isVoid(strContainerNo) && !YFCObject.isVoid(strOrderNo))
						{
							if(hmContaineTOOrders.containsKey(strOrderNo))
							{
								strContainerTemp = strContainerTemp +"," +strContainerNo;
			                    log.verbose(strContainerTemp);
			                    String strContainerNoOld = hmContaineTOOrders.get(strOrderNo);
			                    hmContaineTOOrders.replace(strOrderNo, strContainerNoOld, strContainerTemp);
							}
							else
							{
								strContainerTemp = strContainerNo;
								hmContaineTOOrders.put(strOrderNo, strContainerNo);
							}
							
						} 
					}
					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		String containerList = iContainercount.toString();
		eleInXML.setAttribute(AcademyConstants.ATTR_TOTAL_RECEIVED_CONTAINERS, containerList);

		
		//Partially received containers check
		Element elePartailreceivedContainers  = inXML.createElement(AcademyConstants.ELE_PARTIALLY_RECEIVED_CONTAINERS);
		eleInXML.appendChild(elePartailreceivedContainers);
	
		Iterator iterator = hmContaineTOOrders.keySet().iterator();
		while(iterator.hasNext())
		{	
			String strOrderNo =(String) iterator.next();
			log.verbose(hmContaineTOOrders.get(strOrderNo));
			log.verbose(strOrderNo);
			log.verbose("before shipment call");
			Document docGetShipmentList = prepareAndInvokeGetShipmentList(env, strOrderNo);
			log.verbose("Afert shipment call: " +XMLUtil.getXMLString(docGetShipmentList));
			Boolean isPartaillyReceived = checkPartialContainersInOrder(docGetShipmentList, strOrderNo);
			log.verbose("isPartaillyReceived: " +isPartaillyReceived );
			if(isPartaillyReceived)
			{
				NodeList nlAcadSTSTOContainers = XPathUtil.getNodeList(eleInXML, "/AcadSTSTOContainersList/AcadSTSTOContainers[@TOOrderNo='"
						+strOrderNo + "']");
				//NodeList nlShipment = XPathUtil.getNodeList(docGetShipmentList, "/Shipments/Shipment");
				Element eleShipmentList = docGetShipmentList.getDocumentElement();
				
				//Element eleContainerFromList = (Element) nlShipmentContainers.item(0);
				for(int index=0; index<nlAcadSTSTOContainers.getLength(); index++)
				{
				  Element eleAcadSTSTOContainers = (Element)	nlAcadSTSTOContainers.item(index);
				  Element eleContainer = inXML.createElement(AcademyConstants.ELE_CONTAINER);
				  eleContainer.setAttribute(AcademyConstants.SHIPMENT_KEY, eleAcadSTSTOContainers.getAttribute(AcademyConstants.SHIPMENT_KEY));
				  eleContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_NO, eleAcadSTSTOContainers.getAttribute(AcademyConstants.ATTR_CONTAINER_NO));
				  eleContainer.setAttribute(AcademyConstants.ATTR_IS_PARTAILLY_RECEIVED, AcademyConstants.STR_YES);
				  Element eleShipment = (Element) eleShipmentList.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
				  Element elePartialReceivedShipment = inXML.createElement(AcademyConstants.ELE_SHIPMENT);
				  String strOrderHeaderKey = eleShipment.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
				  elePartialReceivedShipment.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
				  eleContainer.appendChild(elePartialReceivedShipment);
				  elePartailreceivedContainers.appendChild(eleContainer);
				  log.verbose("Containers: " +XMLUtil.getElementXMLString(elePartailreceivedContainers));
				}
			}
		}
		
		log.verbose("inDoc after modified: " +XMLUtil.getXMLString(inXML));
		NodeList nlPartialContainers = XPathUtil.getNodeList(inXML, "/AcadSTSTOContainersList/PartaillyReceivedContainers/Container[@IsPartaillyReceived='Y']");
		Integer iPartailContainersCount = nlPartialContainers.getLength();
		log.verbose("Partail Containers Count: " +iPartailContainersCount);
		eleInXML.setAttribute(AcademyConstants.ATTR__PARTIALLY_RECEIVED_COUNT,iPartailContainersCount.toString());
		
		
		// invoke service to close flags of all containers
	   updateClosedFlagInCustomTable(env,inXML);
		return inXML;

	}

	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		this.properties = props;
	}

	/*
	 * This method invokes the getShipmentList api to get 
	 * all the container list for a order
	 * 
	 *  @param	YFSEnvironment 	env
	 * 	@param	String  strTOOrderNo
	 * 
	 */
	public Document prepareAndInvokeGetShipmentList(YFSEnvironment env, String strTOOrderNo)
	{
		Document docShipmentListIn =null;
		Document docShipmentListOut = null;
		try {
			docShipmentListIn = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element eleShipmentListIn = docShipmentListIn.getDocumentElement();
			eleShipmentListIn.setAttribute(AcademyConstants.ATTR_ORDER_NO, strTOOrderNo);
			eleShipmentListIn.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE);
			eleShipmentListIn.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
			log.verbose("Get Shipment List: " + XMLUtil.getXMLString(docShipmentListIn));
			//docShipmentListOut = AcademyUtil.invokeService(env, "AcademySTSGetShipmentList", docShipmentListIn);
			docShipmentListOut = AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_STS_GET_SHIPMENT_LIST, docShipmentListIn);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return docShipmentListOut;
	}
	
	/*
	 * This method invokes the getShipmentContainerList api to get 
	 * all the container list for a Shipment
	 * 
	 *  @param	YFSEnvironment 	env
	 * 	@param	String  strShipmentKey
	 * 
	 */
/*	public Document prepareAndInvokeGetShipmentContainerList(YFSEnvironment env, String strShipmentKey)
	{
		Document docShipmentContainerListIn =null;
		Document docShipmentContainerListOut = null;
		try {
			docShipmentContainerListIn = XMLUtil.createDocument(AcademyConstants.ELE_CONTAINER);
			Element eleShipmentContainerListIn = docShipmentContainerListIn.getDocumentElement();
			eleShipmentContainerListIn.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
			log.verbose("Get Shipment Container List: " + XMLUtil.getXMLString(docShipmentContainerListIn));
			docShipmentContainerListOut = AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_GET_SHIPMENT_CONTAINERS_LIST, docShipmentContainerListIn);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return docShipmentContainerListOut;
	}
	
	/*
	 * This method will check whether the scanned received is Partially received or not
	 * by comparing with the container list of getShipmetnContainerList api
	 * 
	 */
	
/*	public Boolean checkPartialContainersInShipment(Document docShipmentContainerList, String strShipmentKey)
	{
		Boolean bIsPartiallyReceived = false;
		Element eleShipmentContainerList = docShipmentContainerList.getDocumentElement();
		NodeList nlContainerList = eleShipmentContainerList.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
		for(int i=0; i<nlContainerList.getLength();  i++)
		{
			Element eleContainer = (Element) nlContainerList.item(i);
			String strContainerNo= eleContainer.getAttribute(AcademyConstants.ATTR_CONTAINER_NO);
			String strIsReceived = eleContainer.getAttribute(AcademyConstants.ATTR_IS_RECEIVED); 
			String strReceivedContainers = hmContaineTOOrders.get(strShipmentKey);
			if(strReceivedContainers.contains(strContainerNo))
			{
				continue;
			}
			else
			{
				//if(! YFCObject.isVoid(strIsReceived) && (strIsReceived.equals("N")))
				//{
					bIsPartiallyReceived = true;
					break;
				//}
			}
		}
		return bIsPartiallyReceived;
	}
	*/
	
	/*
	 * This method will check whether the scanned received is Partially received or not
	 * by comparing with the container list of getShipmentList api
	 * 
	 */
	
	public Boolean checkPartialContainersInOrder(Document docShipmentList, String strOrderNo) throws Exception
	{
		Boolean bIsPartiallyReceived = false;
		Element eleShipmentList = docShipmentList.getDocumentElement();
		NodeList nlShipmentList =  XPathUtil.getNodeList(eleShipmentList, "/Shipments/Shipment");
				//eleShipmentList.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
		for(int i =0 ; i<nlShipmentList.getLength(); i++)
		{
		 Element eleShipment = (Element) nlShipmentList.item(i);
		 Element eleStatus = (Element) eleShipment.getElementsByTagName(AcademyConstants.ATTR_STATUS).item(0);
		 String strStatus = eleStatus.getAttribute(AcademyConstants.ATTR_STATUS);
		NodeList nlContainerList = eleShipment.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
		log.verbose("Shipment Status:" + strStatus);
		if((!strStatus.equals(AcademyConstants.VAL_CANCELLED_STATUS)) && (nlContainerList.getLength() ==0))
		{
			log.verbose("No Containers Available");
			bIsPartiallyReceived = true;
			break;
		}
		for(int c=0; c<nlContainerList.getLength();  c++)
		{
			Element eleContainer = (Element) nlContainerList.item(c);
			String strContainerNo= eleContainer.getAttribute(AcademyConstants.ATTR_CONTAINER_NO);
			String strIsReceived = eleContainer.getAttribute(AcademyConstants.ATTR_IS_RECEIVED);
			String strZone = eleContainer.getAttribute(AcademyConstants.ELE_ZONE);
			Element eleExtn = (Element)eleContainer.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
		    String strExtnIsSOCancelled="";
		    if(!YFCObject.isVoid(eleExtn))
		    {
		    	strExtnIsSOCancelled = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_IS_SO_CANCELLED);
		    }
			String strReceivedContainers = hmContaineTOOrders.get(strOrderNo);
			log.verbose("Received Containers: " +strReceivedContainers +" TOOrderNo: " +strOrderNo +" ContainerNo: " +strContainerNo);
			if(strReceivedContainers.contains(strContainerNo))
			{
				continue;
			}
			else
			{
				log.verbose("Container: " +strContainerNo +"Is Received:" +strIsReceived);
				if((!YFCObject.isVoid(strIsReceived)) && (strIsReceived.equals("N")) && ((!strExtnIsSOCancelled.equals(AcademyConstants.ATTR_Y)) && (!strZone.equals("LOST"))))
				{
					log.verbose("Set PartaillyReceivedFlag to true");
					bIsPartiallyReceived = true;
					break;
				}
			}
		}
		
		}
		return bIsPartiallyReceived;
	}
	
	/*
	 * This method is used to update the Closed Flag to N for all the 
	 * scanned containers in the custom table ACAD_STS_TO_CONTAINERS
	 * 
	 * @param	YFSEnvironment 	env
	 * @param	Document  inDoc
	 * 
	 */
	public void updateClosedFlagInCustomTable(YFSEnvironment env, Document inDoc)
	{
		Element eleInXML = inDoc.getDocumentElement();
		NodeList nlAcadSTSTOContainers = eleInXML.getElementsByTagName(AcademyConstants.ELE_ACAD_STS_TO_CONTAINERS);
		for(int index=0; index<nlAcadSTSTOContainers.getLength(); index++)
		{
			
			Element eleAcadSTSTOContainer = (Element) nlAcadSTSTOContainers.item(index);
			String strContainerNo = eleAcadSTSTOContainer.getAttribute(AcademyConstants.ATTR_CONTAINER_NO);
			log.verbose("Container No: " +strContainerNo);
			try {
				if(!YFCObject.isVoid(strContainerNo))
				{
				Document docChangeExtnContainerInput = prepareInputToUpdateClosedFlagInCustomTable(strContainerNo);
				AcademyUtil.invokeService(env,AcademyConstants.SERVICE_ACADEMY_UPDATE_CLOSED_FLAG_IN_CUSTOM_TABLE, docChangeExtnContainerInput);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/*
	 * This method prepares input to the AcademyUpdateClosedFlagInCustomTable service
	 * @param	String 	strContainerNo
	 * @throws	YFCException
	 */
		 
	public Document prepareInputToUpdateClosedFlagInCustomTable(String strContainerNo) throws Exception
	{	
		Document docInputAcadSTSToContainer = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_STS_TO_CONTAINERS);
		Element eleInputAcadSTSToContainer = docInputAcadSTSToContainer.getDocumentElement();
		eleInputAcadSTSToContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_NO, strContainerNo);
		eleInputAcadSTSToContainer.setAttribute(AcademyConstants.ATTR_CLOSED_FLAG, AcademyConstants.STR_YES);
		log.verbose("In XMl to update ClosedFlag: " +XMLUtil.getXMLString(docInputAcadSTSToContainer));
		return docInputAcadSTSToContainer;
	
	}
}
