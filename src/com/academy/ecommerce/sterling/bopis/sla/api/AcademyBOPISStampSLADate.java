package com.academy.ecommerce.sterling.bopis.sla.api;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


import com.academy.util.constants.AcademyConstants;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
/**
 * This Class calculates and stamps the BOPIS_SLA_DATE for BOPIS shipments which are in Ready for Backroom picking status.
 * @author rastaj1
 *
 */
public class AcademyBOPISStampSLADate implements YIFCustomApi{
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademyBOPISStampSLADate.class);

	//Define properties to fetch argument value from service configuration
	private Properties props;
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}
	/**
	 * This method is called from BeforeCreateShipmentUE. this method calculates and stamps the BOPIS_SLA_DATE for BOPIS shipments.
	 * @param env
	 * @param inDoc
	 * @return
	 */
	public Document stampSLADate(YFSEnvironment env, Document inDoc){
		log.beginTimer("AcademyBOPISStampSLADate::stampSLADate");
		Element eleShipment = inDoc.getDocumentElement();


		log.verbose("This is a BOPIS Shipment and in Ready for Backroom picking status. Hence SLA logic needs to be executed");

		//Bopis SLA from service arguments.Default is 120mins
		String bopisSLA = props.getProperty(AcademyConstants.BOPIS_SLA_MIN, "120");
		log.verbose("BOPIS SLA fetched from service arguments is " + bopisSLA);
		int iBopisSLA = Integer.parseInt(bopisSLA);
		String strDatePattern = AcademyConstants.STR_DATE_TIME_PATTERN;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(strDatePattern);

		String orderDateStr = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_DATE);

		try {
			Date orderDate;
			if(!YFCCommon.isVoid(orderDateStr)){
				orderDate = simpleDateFormat.parse(orderDateStr);
			} else {
				orderDate = new Date();
			}
			Calendar cal = Calendar.getInstance();
			cal.setTime(orderDate);

			long timeInMilliSeconds= cal.getTimeInMillis();

			Date afterAddingSLA=new Date(timeInMilliSeconds + (iBopisSLA*60000) );//+ (iBopisSLA * 60000)

			String newTime = simpleDateFormat.format(afterAddingSLA);
			log.verbose("The Due date for picking after adding SLA is ::" + newTime);
			stampSLADateType(eleShipment, newTime);

		} catch (Exception e) {
			log.error("Caught Exception while stamping DueDate for Bopis based on SLA", e);
		} 

		log.endTimer("AcademyBOPISStampSLADate::stampSLADate");
		return inDoc;
	}

	/**
	 * This method is used to stamp BOPIS SLA Date in additional Date in create Shipment input.
	 * @param shipmentKey
	 * @param newTime
	 * @return
	 */
	private void stampSLADateType(Element eleShipment, String newTime) {
		log.beginTimer("AcademyBOPISStampSLADate::stampSLADate");
		//prepare change Shipment api input

		Element eleAdditionalDates = SCXmlUtil.getChildElement(eleShipment, AcademyConstants.E_ADDITIONAL_DATES, true);
		Element eleAdditionalDate = SCXmlUtil.createChild(eleAdditionalDates, AcademyConstants.E_ADDITIONAL_DATE);
		eleAdditionalDate.setAttribute(AcademyConstants.A_DATE_TYPE_ID, AcademyConstants.BOPIS_SLA_DATE_TYPE);
		eleAdditionalDate.setAttribute(AcademyConstants.A_EXPECTED_DATE, newTime);
		eleAdditionalDate.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_CREATE);
		log.verbose("createShipment input after adding SLA :: " + SCXmlUtil.getString(eleShipment));
		log.endTimer("AcademyBOPISStampSLADate::stampSLADate");
		return;

	}
}
