package com.academy.ecommerce.yantriks.general;

import static com.academy.util.constants.AcademyConstants.ATTR_SHIP_DATE;
import static com.academy.util.constants.AcademyConstants.ATTR_STATUS_DATE;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * 
 * Since Shipdate is taken from shipment table, its value could be the past
 * unacceptable values. To make sure real-time update is published to Yantriks,
 * Shipdate is mapped same as status date.
 *
 */
public class AcademyMapShipDate {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyMapShipDate.class);

	public Document mapShipDate(YFSEnvironment env, Document docInXml) {
		Element eleRoot = docInXml.getDocumentElement();
		String strShipDate = eleRoot.getAttribute(ATTR_SHIP_DATE);
		String strStatusDate = eleRoot.getAttribute(ATTR_STATUS_DATE);

		log.debug("ShipDate is " + strShipDate);
		log.debug("StatusDate is " + strStatusDate);
		if (!YFCObject.isVoid(strStatusDate)) {
			eleRoot.setAttribute(ATTR_SHIP_DATE, strStatusDate);
		}
		log.debug(XMLUtil.serialize(docInXml));
		return docInXml;
	}
}
