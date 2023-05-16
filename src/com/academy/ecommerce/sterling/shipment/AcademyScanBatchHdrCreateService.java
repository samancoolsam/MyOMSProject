package com.academy.ecommerce.sterling.shipment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;
/* *
* Input: <ACADScanBatchHeader Action='INPROGRESS' StoreNo='033' UserID="User123" />
* 
* Output: <ACADScanBatchHeader AcadScanBatchHeaderKey="20220406080426266116418" BatchID="03320220406080416" BatchScanStatus="INPROGRESS" Createprogid="SterlingHttpTester" Createts="2022-04-06T08:04:26-05:00" Createuserid="admin" EndTime="2500-01-01T00:00:00-06:00" Lockid="0" Modifyprogid="SterlingHttpTester" Modifyts="2022-04-06T08:04:26-05:00" Modifyuserid="admin" StagedShipmentsCount="116" StartTime="2022-04-06T08:04:26-05:00" StoreNo="033" UserID="User123">	<ACADScanBatchDetailsList TotalNumberOfRecords="0"/> </ACADScanBatchHeader>
* */



public class AcademyScanBatchHdrCreateService implements YIFCustomApi{
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyScanBatchHdrCreateService.class);
	private Properties props;
		@Override
		public void setProperties(Properties arg0) throws Exception {
			// TODO Auto-generated method stub
		}
		
			public Document startScanBatchHdr(YFSEnvironment env, Document inDoc) throws Exception{
			log.beginTimer(this.getClass() + ".startScanBatchHdr");
			log.verbose("AcademyScanBatchHdrCreateService.java:startScanBatchHdr():InDoc " + XMLUtil.getXMLString(inDoc));

			Document getACADScanBatchHeaderOutDoc = null;
			Document getShipmentsCountOutDoc=null;
			Element inDocEle;
			inDocEle = inDoc.getDocumentElement();
			String strAction = inDocEle.getAttribute(AcademyConstants.ATTR_HEADER_ACTION);
			log.verbose("Action is : " + strAction);
			String strUserID = inDocEle.getAttribute(AcademyConstants.ATTR_HEADER_USERID);
			log.verbose("userID is : " + strUserID);
			String strStoreNo = inDocEle.getAttribute(AcademyConstants.ATTR_HEADER_STORENO);
			log.verbose("StoreNo is : " + strStoreNo);
								
			if (!YFCObject.isVoid(strUserID) && !YFCObject.isVoid(strStoreNo) && strAction.equalsIgnoreCase(AcademyConstants.STR_INPROGRESS)) {
				
				//OMNI-69511- Start - Add user name to header table
				Document getUserListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_USER); 
				getUserListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_LOGINID, strUserID);
				log.verbose("Input to getUserList API : " + XMLUtil.getXMLString(getUserListInDoc));
				env.setApiTemplate(AcademyConstants.API_GET_USER_LIST, AcademyConstants.GET_USER_LIST_TEMPLATE);
				Document getUserListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_USER_LIST, getUserListInDoc);
				env.clearApiTemplates(); 
				String strUserName = XPathUtil.getString(getUserListOutDoc.getDocumentElement(), AcademyConstants.XPATH_USER_NAME);
				log.verbose("UserName " +strUserName);
				//OMNI-69511- End
				String strDateTypeFormat= AcademyConstants.STR_DATE_TIME_PATTERN_NEW;
           	 	SimpleDateFormat sdf = new SimpleDateFormat(strDateTypeFormat);
     			Calendar cal = Calendar.getInstance();
     			String strCurrentDateTime = sdf.format(cal.getTime());
     			log.verbose("Calculated Current Date-Time : " + strCurrentDateTime);
     			String strConcatStoreAndDateTime =  strStoreNo + strCurrentDateTime;
     			log.verbose("Concatenated Store and Date-Time : " + strConcatStoreAndDateTime);
     			
				
				inDocEle.setAttribute("BatchID", strConcatStoreAndDateTime);
				inDocEle.setAttribute("EndTime", AcademyConstants.STR_END_TIME_FUTURE_DATE);
           	 	inDocEle.setAttribute("UserID", strUserID);
           	 	inDocEle.setAttribute("UserName", strUserName);
           	 	
           	 
			//invoking AcademyScanBatchHdrCreateService to insert record in to ACAD_SCAN_BATCH_HEADER table 	
           	 	getACADScanBatchHeaderOutDoc = AcademyUtil.invokeService(env, "AcademyScanBatchHdrCreateService", inDoc);
           	 	log.verbose("AcademyScanBatchHdrCreateService.java:startScanBatchHdr():OutDoc " + XMLUtil.getXMLString(getACADScanBatchHeaderOutDoc));
			
			//invoking AcademyStoreActiveShipmentStatusCountService to fetch the Active Status Shipments(RFCP & PaperWorkIntiated)count
			getShipmentsCountOutDoc = AcademyUtil.invokeService(env, "AcademyStoreActiveShipmentStatusCountService", inDoc);
			String strActiveShipmentsCount=XPathUtil.getString(getShipmentsCountOutDoc,AcademyConstants.XPATH_SHIPMENTS_COUNT);
			log.verbose("Staged Shipments Count is : " + strActiveShipmentsCount);
			Element eleACADScanBatchHdr = getACADScanBatchHeaderOutDoc.getDocumentElement();
			eleACADScanBatchHdr.setAttribute(AcademyConstants.STR_STAGED_SHIPMENTS_COUNT, strActiveShipmentsCount);
			log.verbose("AcademyStoreActiveShipmentStatusCountService.java::OutDoc " + 
			XMLUtil.getXMLString(getShipmentsCountOutDoc));
					}
			
			return getACADScanBatchHeaderOutDoc;
	}
}