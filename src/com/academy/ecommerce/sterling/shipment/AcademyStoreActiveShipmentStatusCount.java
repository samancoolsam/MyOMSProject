package com.academy.ecommerce.sterling.shipment;

import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.ibm.icu.text.SimpleDateFormat;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import org.apache.commons.lang3.time.DateUtils;

public class AcademyStoreActiveShipmentStatusCount implements YIFCustomApi {

	// Define properties to fetch service level argument values
	private Properties props;
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyStoreActiveShipmentStatusCount.class);

	/**
	 * This method takes ShipNode key as input and returns the total count of Active
	 * Shipments which are in the Ready For Customer Pick Up & Paper Work Initiated
	 * Status.
	 * 
	 * Sample I/P: <ACADScanBatchHdr Action="OPEN" StoreNo="033"/>
	 * 
	 * Sample O/P: <Shipments StagedShipmentsCount="124"/>
	 *
	 */

	public Document getStoreActiveShipmentsCount(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer(this.getClass() + ".getStoreActiveShipmentsCount");
		log.verbose("AcademyStoreActiveShipmentsStatusCount.java ::InDoc " + XMLUtil.getXMLString(inDoc));

		String strNoOfDays = null;
		String stagedShipmentCount = null;
		Document getShipmentListOutputDoc = null;
		
		//Preparing InDoc for Service 
		String strStoreNo = XPathUtil.getString(inDoc,AcademyConstants.XPATH_STORE_NO);		
		Document docActiveShipmentCountInput = XMLUtil.createDocument(AcademyConstants.STR_SCAN_BATCH_HDR);
		Element eleAcadScanBatchHder= docActiveShipmentCountInput.getDocumentElement();
		eleAcadScanBatchHder.setAttribute(AcademyConstants.STR_STORE_NO, strStoreNo);
		eleAcadScanBatchHder.setAttribute(AcademyConstants.STR_ACTION, AcademyConstants.STR_OPEN);
		log.verbose("Input to AcademyStoreActiveShipmentStatusCount:: " +XMLUtil.getXMLString(docActiveShipmentCountInput));
		// Fetch the ValidateStatus & NoOfDays from service arguments
		strNoOfDays = props.getProperty(AcademyConstants.STR_NO_OF_DAYS);
		String strStatus = props.getProperty(AcademyConstants.STR_VALIDATE_STATUS);
		String[] lStatus = strStatus.split(AcademyConstants.STR_COMMA);
		String strGetShipmentNo = XPathUtil.getString(inDoc, AcademyConstants.XPATH_GET_SHIPMENT_NO_LIST_FLAG);
				
		// calculating date from last 45 days
		int days = Integer.parseInt(strNoOfDays);
		String strPastDay = null;
		Date pastday = DateUtils.addDays(new Date(), -days);
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);
		strPastDay = sdf.format(pastday);

		StringBuilder statusDate = new StringBuilder();
		statusDate.append(strPastDay);
		statusDate.append(AcademyConstants.STR_12AM_TIME);
		log.verbose("Calculated Status Date is : " + statusDate);

		// Preparing InDoc for getShipmentList (ComplexQuery)
		YFCDocument getShipmentListInputDoc = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
		YFCElement eleShipment = getShipmentListInputDoc.getDocumentElement();
		YFCElement eleComplexQuery = eleShipment.createChild(AcademyConstants.COMPLEX_QRY_ELEMENT);

		eleComplexQuery.setAttribute(AcademyConstants.COMPLEX_OPERATOR_ATTR, AcademyConstants.COMPLEX_OPERATOR_AND_VAL);
		YFCElement eleAnd = eleComplexQuery.createChild(AcademyConstants.COMPLEX_AND_ELEMENT);
		YFCElement eleOr = eleComplexQuery.createChild(AcademyConstants.COMPLEX_OR_ELEMENT);

		for (String str : lStatus) {
			YFCElement eleExp0 = eleOr.createChild(AcademyConstants.COMPLEX_EXP_ELEMENT);
			eleExp0.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_STATUS);
			eleExp0.setAttribute(AcademyConstants.ATTR_VALUE, str);
			eleExp0.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
			eleOr.appendChild(eleExp0);
			eleAnd.appendChild(eleOr);
		}

		YFCElement eleExp1 = eleAnd.createChild(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp1.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_STATUS_DATE);
		eleExp1.setAttribute(AcademyConstants.ATTR_VALUE, statusDate.toString());
		eleExp1.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.GT_QRY_TYPE);
		eleAnd.appendChild(eleExp1);

		YFCElement eleExp2 = eleAnd.createChild(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp2.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ELE_SHIP_NODE);
		eleExp2.setAttribute(AcademyConstants.ATTR_VALUE, strStoreNo);
		eleExp2.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleAnd.appendChild(eleExp2);
		
		YFCElement eleExp3 = eleAnd.createChild(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp3.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.STR_DOCUMENT_TYPE);
		eleExp3.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STR_DOCUMENT_TYPE_VALUE);
		eleExp3.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleAnd.appendChild(eleExp3);
		
		YFCElement eleExp4 = eleAnd.createChild(AcademyConstants.COMPLEX_EXP_ELEMENT);
		eleExp4.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_DEL_METHOD);
		eleExp4.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STR_DELIVERY_METHOD_VALUE);
		eleExp4.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
		eleAnd.appendChild(eleExp4);

		/*
		 * <Shipment><ComplexQuery Operator="AND"><And><Or><Exp Name="Status" Value="1100.70.06.30.5" QryType="EQ"/>
		 * <Exp Name="Status" Value="1100.70.06.30.7" QryType="EQ"/></Or><Exp Name="StatusDate" Value="2022-03-24 04:24:23-05:00" QryType="GT"/>
		 * <Exp Name="ShipNode" Value="033" QryType="EQ"/><Exp Name="DocumentType" Value="0001" QryType="EQ"/>
		 * <Exp Name="DeliveryMethod" Value="PICK" QryType="EQ"/></And></ComplexQuery></Shipment>
		 */
		log.verbose("getShipmentList API Input :: " + XMLUtil.getXMLString(getShipmentListInputDoc.getDocument()));

		// Preparing OutDoc for getShipmentList
		String strGetShipmentListTemplate = AcademyConstants.GET_COUNT_OF_SHIPMENT_LIST_TEMPLATE;
		
		if (!YFCObject.isVoid(strGetShipmentNo) && strGetShipmentNo.equals(AcademyConstants.ATTR_Y)) {
			log.verbose("The flag is Y");
			log.verbose("The getShipmentList Template" +AcademyConstants.GET_SHIPMENT_LIST_TEMPLATE);
			strGetShipmentListTemplate = AcademyConstants.GET_SHIPMENT_LIST_TEMPLATE;		
		} 
		Document docGetShipmentListTemplate = YFCDocument.getDocumentFor(strGetShipmentListTemplate).getDocument();
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docGetShipmentListTemplate);
		getShipmentListOutputDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
				getShipmentListInputDoc.getDocument());
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		stagedShipmentCount = getShipmentListOutputDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS);
		if (!YFCObject.isVoid(stagedShipmentCount)) {
			getShipmentListOutputDoc.getDocumentElement().setAttribute(AcademyConstants.STR_STAGED_SHIPMENTS_COUNT, stagedShipmentCount);			
		}
		log.verbose("getShipmentList API output is :: " + XMLUtil.getXMLString(getShipmentListOutputDoc));
		return getShipmentListOutputDoc;
	}

}