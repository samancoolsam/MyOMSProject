package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * Description:Class UpdateShipToAddress is added as part of STL-683 - Carrier
 * Integration Story to Stamp company value as DOT if SCAC is Fedex and level of
 * Service is Ground when the company field is blank.
 * 
 */

public class AcademyUpdateShipToAddress implements YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyYDMBeforeCreateShipmentUEImpl.class);

	public void setProperties(Properties props) {
	}

	public Document StampDotForCompany(YFSEnvironment env, Document inXML) throws Exception {

		log.verbose("UpdateShipToAddress.StampDotForCompany()_InXML" + XMLUtil.getXMLString(inXML));
		Element eleRoot = inXML.getDocumentElement();
		String scac = eleRoot.getAttribute(AcademyConstants.ATTR_SCAC);
		String carrierService = eleRoot.getAttribute(AcademyConstants.CARRIER_SERVICE_CODE);

		log.debug("UpdateShipToAddress.StampDotForCompany()_SCAC:" + scac);
		log.debug("UpdateShipToAddress.StampDotForCompany()_carrierService:" + carrierService);

		if (!YFCObject.isVoid(scac) && !YFCObject.isVoid(carrierService) && (scac.equalsIgnoreCase(AcademyConstants.SCAC_FEDEX) || scac.equalsIgnoreCase(AcademyConstants.SCAC_FEDX)) && carrierService.equalsIgnoreCase(AcademyConstants.LOS_GROUND)) {
			// SCAC is fedex and LOS is Ground. Update company field to DOT if
			// the company field is empty.
			
			/* Start - Changes made for PERF-202 to fix YFS10415 Error - Shipment modification rules - key fields cannot be modfied */
			// Form changeShipment API Input
			Document changeShipmentInDoc = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT).getDocument();
			String shipmentKey = eleRoot.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			Element shipmentElem = changeShipmentInDoc.getDocumentElement();
			shipmentElem.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, shipmentKey);			
			/* End - Changes made for PERF-202 to fix YFS10415 Error - Shipment modification rules - key fields cannot be modfied */
			
			NodeList toAddressNL = eleRoot.getElementsByTagName(AcademyConstants.ELEM_TOADDRESS);
			if (toAddressNL != null && toAddressNL.getLength() > 0) {
				Element toAddressElem = (Element) toAddressNL.item(0);
				String company = toAddressElem.getAttribute(AcademyConstants.ATTR_COMPANY);
				log.verbose("UpdateShipToAddress.StampDotForCompany()_company:" + company);
				if ("".equals(company)) {
					// Update company field to DOT.
					log.verbose("Company field has been modified to DOT!");
					toAddressElem.setAttribute(AcademyConstants.ATTR_COMPANY, ".");
					
					/* Start - Changes made for PERF-202 to fix YFS10415 Error - Shipment modification rules - key fields cannot be modfied */
					// Copy toAddress element as a whole to changeShipment API InDoc as it is an address.
					XMLUtil.importElement(shipmentElem, toAddressElem);
					/* End - Changes made for PERF-202 to fix YFS10415 Error - Shipment modification rules - key fields cannot be modfied */
					
					Document outXML = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, changeShipmentInDoc);
					log.verbose("The StampDotForCompany output XML is:" + XMLUtil.getXMLString(outXML));
				}
			}
		}

		return inXML;
	}
}
