package com.academy.ecommerce.sterling.inventory.api;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.interop.japi.YIFCustomApi;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author Arun Sekhar
 * @Date 04-28-2014
 * @version 1.0
 */

public class AcademyIncreaseCptyOnShipCancel implements YIFCustomApi {

	/**
	 * Instance to store the properties configured for the condition in
	 * configurator *
	 */
	private Properties props;

	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyIncreaseCptyOnShipCancel.class);

	/**
	 * 
	 * @param env
	 * @param eventOutXML -
	 *            Output of ON_SUCCESS event (Transaction ID: CANCEL_SHIPMENT)
	 */
	public void overrideCapacity(YFSEnvironment env, Document eventOutXML) {
		log
				.verbose("---> AcademyIncreaseCptyOnShipCancel :: overrideCapacity()"
						+ YFCDocument.getDocumentFor(eventOutXML));
		Document getResourcePoolCptyInputDoc = null;
		Document getResourcePoolCptyOutDoc = null;
		Document overrideResourcePoolCptyInDoc = null;
		Document overrideResourcePoolCptyOutDoc = null;

		try {
			String strExpectedShipmentDate = XMLUtil.getAttributeFromXPath(
					eventOutXML, "/Shipment/@ExpectedShipmentDate");
			log.verbose("strExpectedShipmentDate is: "
					+ strExpectedShipmentDate);
			/** Prepare input for getResourcePoolCapacity * */
			getResourcePoolCptyInputDoc = prepareGetResourcePoolCptyInput(
					eventOutXML, strExpectedShipmentDate);
			log.verbose("getResourcePoolCptyInputDoc: "
					+ YFCDocument.getDocumentFor(getResourcePoolCptyInputDoc));
			getResourcePoolCptyOutDoc = AcademyUtil.invokeAPI(env,
					"getResourcePoolCapacity", getResourcePoolCptyInputDoc);
			log.verbose("getResourcePoolCptyOutDoc: "
					+ YFCDocument.getDocumentFor(getResourcePoolCptyOutDoc));

			
			/** Prepare input for overrideResourcePoolCapacity * */
			overrideResourcePoolCptyInDoc = prepareOverrideResourcePoolCptyInput(
					getResourcePoolCptyOutDoc, strExpectedShipmentDate);
			log
					.verbose("overrideResourcePoolCptyInDoc: "
							+ YFCDocument
									.getDocumentFor(overrideResourcePoolCptyInDoc));
			overrideResourcePoolCptyOutDoc = AcademyUtil.invokeAPI(env,
					"overrideResourcePoolCapacity",
					overrideResourcePoolCptyInDoc);
			log.verbose("overrideResourcePoolCptyOutDoc: "
					+ YFCDocument
							.getDocumentFor(overrideResourcePoolCptyOutDoc));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param strExpectedShipmentDate
	 * @return isExpShipDateCurrent 
	 * @throws ParseException
	 * 
	 * This method will check if we have crossed the expected ship date, in which case we do not want to update the capacity for a past date.
	 * 
	 */
	public boolean checkIfResDateIsCurrent(String strExpectedShipmentDate)
			throws ParseException {
		boolean isExpShipDateCurrent = false;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		String currentDate = sdf.format(cal.getTime());
		
		if(!sdf.parse(currentDate).after(sdf.parse(strExpectedShipmentDate))){
			isExpShipDateCurrent = true;	
		}
		return isExpShipDateCurrent;
	}
	
	/**
	 * This method will prepare the input for the getResourcePoolCapacity API *
	 */
	public Document prepareGetResourcePoolCptyInput(Document inDoc,
			String strExpectedShipmentDate)
			throws ParserConfigurationException, XPathExpressionException {
		log
				.verbose("---> AcademyIncreaseCptyOnShipCancel :: prepareGetResourcePoolCptyInput()");

		/** Fetch all required parameters from the inDoc here * */
		String strNode = XMLUtil.getAttributeFromXPath(inDoc,
				"/Shipment/@ShipNode");
		String strResourcePoolId = strNode;

		String strItemGroupCode = props.getProperty("ItemGroupCode");

		Document getResourcePoolCptyInDoc = XMLUtil
				.createDocument("ResourcePool");
		Element eleResourcePool = getResourcePoolCptyInDoc.getDocumentElement();
		eleResourcePool.setAttribute("Node", strNode);
		eleResourcePool.setAttribute("ItemGroupCode", strItemGroupCode);
		eleResourcePool.setAttribute("ResourcePoolId", strResourcePoolId);
		// eleResourcePool.setAttribute("ResourcePoolKey", strResourcePoolKey);
		// eleResourcePool.setAttribute("ProviderOrganizationCode",strProviderOrganizationCode);
		eleResourcePool.setAttribute("CapacityOrganizationCode",
				AcademyConstants.CATALOG_ORG_CODE);
		eleResourcePool.setAttribute("ProviderOrganizationCode",
				AcademyConstants.CATALOG_ORG_CODE);
		eleResourcePool.setAttribute("StartDate", strExpectedShipmentDate);
		eleResourcePool.setAttribute("EndDate", strExpectedShipmentDate);

		log.verbose("Input to getResourcePoolCapacity: "
				+ YFCDocument.getDocumentFor(getResourcePoolCptyInDoc));
		log
				.verbose("<--- AcademyIncreaseCptyOnShipCancel :: prepareGetResourcePoolCptyInput()");
		return getResourcePoolCptyInDoc;
	}

	/**
	 * This method will prepare the input for the overrideResourcePoolCapacity
	 * API *
	 */
	public Document prepareOverrideResourcePoolCptyInput(
			Document getResourcePoolCptyOutDoc, String strExpectedShipmentDate)
			throws ParserConfigurationException, XPathExpressionException {
		log
				.verbose("---> AcademyIncreaseCptyOnShipCancel :: prepareOverrideResourcePoolCptyInput()");
		double dblOverridenCpty = 0;
		/** Fetch all required parameters from the inDoc here * */
		String strCapacityOrganizationCode = XMLUtil.getAttributeFromXPath(
				getResourcePoolCptyOutDoc,
				"/Node/ResourcePools/ResourcePool/@CapacityOrganizationCode");
		log.verbose("CapacityOrganizationCode: " + strCapacityOrganizationCode);

		String strNode = XMLUtil.getAttributeFromXPath(
				getResourcePoolCptyOutDoc,
				"/Node/ResourcePools/ResourcePool/@Node");
		log.verbose("Node: " + strNode);

		String strResourcePoolId = XMLUtil.getAttributeFromXPath(
				getResourcePoolCptyOutDoc,
				"/Node/ResourcePools/ResourcePool/@ResourcePoolId");
		log.verbose("ResourcePoolId: " + strResourcePoolId);

		String strStartTime = XMLUtil
				.getAttributeFromXPath(getResourcePoolCptyOutDoc,
						"/Node/ResourcePools/ResourcePool/ServiceSlots/ServiceSlot/@StartTime");
		log.verbose("StartTime: " + strStartTime);

		String strEndTime = XMLUtil
				.getAttributeFromXPath(getResourcePoolCptyOutDoc,
						"/Node/ResourcePools/ResourcePool/ServiceSlots/ServiceSlot/@EndTime");
		log.verbose("EndTime: " + strEndTime);

		String strCapacity = XMLUtil
				.getAttributeFromXPath(
						getResourcePoolCptyOutDoc,
						"/Node/ResourcePools/ResourcePool/ServiceSlots/ServiceSlot/Dates/Date/@Capacity");
		log.verbose("Capacity: " + strCapacity);

		String strOverridenCpty = Double.toString((Double
				.parseDouble(strCapacity)) - 1.00);
		log.verbose("strOverridenCpty: " + strOverridenCpty);

		Document overrideResourcePoolCptyInDoc = XMLUtil
				.createDocument("OverrideResourcePoolCapacity");
		Element eleOverrideResourcePoolCpty = overrideResourcePoolCptyInDoc
				.getDocumentElement();

		Element eleResourcePools = overrideResourcePoolCptyInDoc
				.createElement("ResourcePools");
		eleOverrideResourcePoolCpty.appendChild(eleResourcePools);

		Element eleResourcePool = overrideResourcePoolCptyInDoc
				.createElement("ResourcePool");
		eleResourcePool.setAttribute("Node", strNode);
		eleResourcePool.setAttribute("CapacityOrganizationCode",
				strCapacityOrganizationCode);
		eleResourcePool.setAttribute("ResourcePoolId", strResourcePoolId);
		eleResourcePools.appendChild(eleResourcePool);

		Element eleServiceSlots = overrideResourcePoolCptyInDoc
				.createElement("ServiceSlots");
		eleResourcePool.appendChild(eleServiceSlots);

		Element eleServiceSlot = overrideResourcePoolCptyInDoc
				.createElement("ServiceSlot");
		eleServiceSlot.setAttribute("StartTime", strStartTime);
		eleServiceSlot.setAttribute("EndTime", strEndTime);
		eleServiceSlots.appendChild(eleServiceSlot);

		Element eleDates = overrideResourcePoolCptyInDoc.createElement("Dates");
		eleServiceSlot.appendChild(eleDates);

		Element eleDate = overrideResourcePoolCptyInDoc.createElement("Date");
		eleDate.setAttribute("Capacity", strOverridenCpty);
		eleDate.setAttribute("Date", strExpectedShipmentDate);
		eleDates.appendChild(eleDate);

		log.verbose("Input to overrideResourcePoolCapacity: "
				+ YFCDocument.getDocumentFor(overrideResourcePoolCptyInDoc));
		log
				.verbose("<--- AcademyIncreaseCptyOnShipCancel :: prepareOverrideResourcePoolCptyInput()");
		return overrideResourcePoolCptyInDoc;
	}
}
