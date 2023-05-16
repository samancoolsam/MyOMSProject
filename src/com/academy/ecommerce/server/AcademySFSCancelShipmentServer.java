/**
 * Description: Class AcademySFSCancelShipmentServer has two methods getJobs and
 * executeJob. The getJobs method returns the list of release or shipment
 * records that has to be rescheduled. Executes the job in the input XML as a
 * service for each of the document as passed by the getJobs service in a list
 * 
 * @throws Exception
 */

// package declaration

package com.academy.ecommerce.server;

// java util import statements
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

// w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// yantra import statements
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

// academy util import statements
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;

public class AcademySFSCancelShipmentServer extends YCPBaseAgent
{

	// Instance of logger
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySFSCancelShipmentServer.class);
	// Declaring global variable
	private static String gstrCustomExecute	= null;

	/**
	 * Retrieves list of release key or shipment key from from
	 * ACA_RESCHEDULE_STORE table based on CancelStatus="Y". Stors each record
	 * of ACAResheduleStore into Array list.
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @param inXML
	 *            Input Document.
	 * @return List
	 */
	public List getJobs(YFSEnvironment env, Document docinXML) throws Exception
	{

		// Declaring document variables
		Document docInXMLacaRescheduleStore = null;
		Document docOutXMLacaRescheduleStore = null;
		Document docChildRecord = null;
		// Declaring element variable
		Element eleMessageXml = null;
		Element eleRooElement = null;
		Element eleRootInXML = null;
		// Declaring list variable
		List aRescheduleStoretList = new ArrayList();
		List aElementsList = null;

		// Fetch the root element of the input xml
		eleMessageXml = docinXML.getDocumentElement();
		// Fetch the attribute value from input xml

		// START: Preparing input document for getACARescheduleStoreList API
		docInXMLacaRescheduleStore = XMLUtil.createDocument(AcademyConstants.ELE_ACS_RESCHEDULE_STORE);
		eleRooElement = docInXMLacaRescheduleStore.getDocumentElement();
		eleRooElement.setAttribute(AcademyConstants.ATTR_CANCEL_STATUS, AcademyConstants.STR_YES);
		eleRooElement.setAttribute(AcademyConstants.ATTR_MAX_RECORD, eleMessageXml.getAttribute(AcademyConstants.ATTR_NUM_RECORDS));
		// END: Preparing input document for getACARescheduleStoreList API

		// Invoking service AcademySFSGetShipmentCancelRecord
		docOutXMLacaRescheduleStore = AcademyUtil.invokeService(env, AcademyConstants.SERV_CUSTOM_GET, docInXMLacaRescheduleStore);

		// Fetch the root element
		eleRootInXML = docOutXMLacaRescheduleStore.getDocumentElement();
		// Fetch the child element
		aElementsList = XMLUtil.getSubNodeList(eleRootInXML, eleMessageXml.getAttribute(AcademyConstants.ATTR_CHILD_ELE));
		// Iterate through the aElementsList
		for (Iterator itEleRecord = aElementsList.iterator(); itEleRecord.hasNext();)
		{
			// Return the document for the fetched child element
			docChildRecord = XMLUtil.getDocumentForElement((Element) itEleRecord.next());

			// Add the response document into the array list
			aRescheduleStoretList.add(docChildRecord);
		}
		// return the array list
		return aRescheduleStoretList;

	}

	/**
	 * Executes the job in the input XML as a service for each of the element
	 * ACARescheduleStore as passed by the getJobs service in a list.
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @param inXML
	 *            Input Document.
	 * @return void
	 */
	public void executeJob(YFSEnvironment env, Document docinXML) throws Exception
	{
		// Declare String variable
		String strEntityFlag = "R";
		// Fetch the root element
		Element eleRootStore = docinXML.getDocumentElement();

		try
		{
			// Check if Entity is Release
			if (eleRootStore.getAttribute(AcademyConstants.ATTR_ENTITY_FLAG).equalsIgnoreCase(strEntityFlag))
			{
				// START: Preparing input document for changeRelease API
				// Create element OrderRelease
				Document docInXMLchangeRelease = XMLUtil.createDocument(AcademyConstants.ELE_ORD_RELEASE);
				Element eleRootRelease = docInXMLchangeRelease.getDocumentElement();
				// Set attribute value for Action
				eleRootRelease.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.VAL_BACKORDER);
				// Map OrderReleaseKey attribute value
				eleRootRelease.setAttribute(AcademyConstants.ATTR_RELEASE_KEY, eleRootStore.getAttribute(AcademyConstants.ATTR_ENTITY_KEY));
				// Invoking Sterling standard API changeRelease
				AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_RELEASE, docInXMLchangeRelease);
				// END: Preparing input document for changeRelease API
			} else
			{
				// START: Preparing input document for changeShipment API
				// Create element Shipment
				Document docInXMLchangeShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
				Element eleRootShipment = docInXMLchangeShipment.getDocumentElement();
				// Set attribute value for Action
				eleRootShipment.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.VAL_CANCEL);
				// Set attribute value for BackOrderRemovedQuantity
				eleRootShipment.setAttribute(AcademyConstants.ATTR_BACK_ORD_REM_QTY, AcademyConstants.STR_YES);
				// Map ShipmentKey attribute value
				eleRootShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, eleRootStore.getAttribute(AcademyConstants.ATTR_ENTITY_KEY));
				// Invoking Sterling standard API changeShipment
				AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docInXMLchangeShipment);
				// END: Preparing input document for changeShipment API
			}
			// Set attribute value for CancelStatus
			eleRootStore.setAttribute(AcademyConstants.ATTR_CANCEL_STATUS, AcademyConstants.STR_NO);
			// Invoking service AcademySFSExecuteCancelShipment
			AcademyUtil.invokeService(env, AcademyConstants.SERV_CUSTOM_EXECUTE, docinXML);
		} catch (YFSException e)
		{
			// Set attribute value for CancelStatus
			eleRootStore.setAttribute(AcademyConstants.ATTR_CANCEL_STATUS, AcademyConstants.STR_ERR);
			// Invoking service AcademySFSExecuteCancelShipment
			AcademyUtil.invokeService(env, AcademyConstants.SERV_CUSTOM_EXECUTE, docinXML);

		}
	}
}