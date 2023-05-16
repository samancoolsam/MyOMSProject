package com.academy.ecommerce.sterling.shipment;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.cts.sterling.custom.accelerators.util.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dblayer.YFCContext;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * Created for STL-680 Update the shipment type to WG when the LOS is changed to
 * CEVA. User will login to the application console and modify the shipment
 * carrier Type to CEVA and Save.
 * 
 * 
 */

public class AcademyModifyShipmentType implements YIFCustomApi {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyModifyShipmentType.class);

	/**
	 * Update the shipment type to WG when the LOS is changed to CEVA.
	 * 
	 * @param env
	 *            YFSEnvironment
	 * 
	 * @param inXML
	 *            Document
	 * 
	 * @return docOutputGetShipmentDetails changeShipment Output.
	 */

	public Document updateShipmentType(YFSEnvironment env, Document inXML) throws Exception {

		log.verbose("AcademyModifyShipmentType_updateShipmentType_InXML:" + XMLUtil.getString(inXML));
		//changes as part of shipment modification for upgrade
		Element eleChangeShipment = inXML.getDocumentElement();
		eleChangeShipment.removeAttribute("OverrideModificationRules");
		Document inDocChangeShipment = eleChangeShipment.getOwnerDocument();
		log.verbose("Input to changeShipment API is :" + XMLUtil.getString(inDocChangeShipment));
		//changes as part of shipment modification for upgrade
		
		Document docOutputGetShipmentDetails = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, inXML);

		// List of Carriers which can fulfill WG has been defined in common code
		// - SCAC_for_WG. Any new carrier who can fulfill WG Shipments needs to
		// be added in Common code.
		Document getCommonCodeListInputXML = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		getCommonCodeListInputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.SCAC_FOR_WG);
		Document outXML = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST, getCommonCodeListInputXML);
		NodeList commonCodeList = outXML.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);

		// Get the list of Carriers which can fulfill White Glove(WG) Shipments.
		ArrayList<String> carrierList = new ArrayList<String>();
		if (commonCodeList != null) {
			int commonCodeLength = commonCodeList.getLength();
			for (int i = 0; i < commonCodeLength; i++) {
				Element commonCodeElem = (Element) commonCodeList.item(i);
				String codeValue = commonCodeElem.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
				if (!YFCObject.isVoid(codeValue)) {
					log.verbose("Carrier for Shipping WG Shipment Type:" + codeValue);
					carrierList.add(codeValue);
				}
			}
		}

		// Check If SCAC is CEVA. If yes then update the shipment type to 'WG'
		Element eleShipment = inXML.getDocumentElement();
		if (eleShipment.hasAttribute(AcademyConstants.ATTR_SCAC)) {
			String strSCAC = eleShipment.getAttribute(AcademyConstants.ATTR_SCAC);
			if (!YFCObject.isVoid(strSCAC) && carrierList != null && carrierList.contains(strSCAC)) {
				log.verbose("Carrier is for the Shipment type WG");
				String strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
				//Start STL-735 Changes: Skipping the change of ShipmentType to WG for AMMO shipments
				String strShipmentType = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
				
				if ("".equals(strShipmentType) ||
						null == strShipmentType) {
					strShipmentType=fetchShipmentType(env, strShipmentKey);
				}
				//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
				if(AcademyConstants.AMMO_SHIPMENT_TYPE.equals(strShipmentType) || AcademyConstants.HAZMAT_SHIPMENT_TYPE.equals(strShipmentType)) {
					return docOutputGetShipmentDetails;
				}
				//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
				
				//End STL-735 Changes
				String strUpdateShipmentTypeQry = "UPDATE " + AcademyConstants.TABLE_YFS_SHIPMENT + " set " + "SHIPMENT_TYPE" + "='" + AcademyConstants.WG + "'  where SHIPMENT_KEY='" + strShipmentKey + "'";
				log.verbose("Query to update Shipment Type as WG: \n " + strUpdateShipmentTypeQry);
				Statement stmt = null;

				try {
					YFCContext ctxt = (YFCContext) env;
					stmt = ctxt.getConnection().createStatement();
					int hasUpdated = stmt.executeUpdate(strUpdateShipmentTypeQry);
					if (hasUpdated > 0) {
						log.verbose("Shipment type has been successfully updated to WG.");
					}
				} catch (SQLException sqlEx) {
					log.verbose("Error occured: Shipment type has not been updated.");
					sqlEx.printStackTrace();
					throw sqlEx;
				} finally {
					if (stmt != null)
						stmt.close();
					stmt = null;
				}
			}
		}
		return docOutputGetShipmentDetails;
	}
	/**
	 * STL-735
	 * This method fetches the ShipmentType on the basis of ShipmentKey
	 * @param env
	 * @param strShipmentKey
	 * @return ShipmentType
	 * @throws Exception
	 */
	
	private String fetchShipmentType(YFSEnvironment env, String strShipmentKey) throws Exception{
		String strShipmentType = null;
		Document inShipmentListDoc = XMLUtil.createDocument("Shipment");
		Element inShipmentElem = inShipmentListDoc.getDocumentElement();
		inShipmentElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
		
		Document docShipmentListOutputTemplate = XMLUtil.createDocument("Shipment");
		Element eleShipmentTemplate = docShipmentListOutputTemplate.getDocumentElement();
		eleShipmentTemplate.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, "");
		eleShipmentTemplate.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, "");
		
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docShipmentListOutputTemplate);
		Document docOutputGetShipmentList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, inShipmentListDoc);		
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		
		Element outShipmentElem = (Element) docOutputGetShipmentList.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
		strShipmentType = outShipmentElem.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
		return strShipmentType;
	}
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}
}
