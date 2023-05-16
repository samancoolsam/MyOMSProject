package com.academy.ecommerce.sterling.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author <a href="mailto:Kruthi.KM@cognizant.com">Kruthi KM</a>, Created on
 *         01/08/2014. This Dynamic condition class will get invoked when
 *         Sterling receives No Inventory message from Exeter-WMS via ESB.This
 *         class will evaluate whether Sterling has already received ContainerId
 *         from Exeter or if already Sterling containers are generated.Also
 *         checks if the Shipment is already cancelled by SOM user.
 * 
 */
public class AcademyCheckIfContainerExists implements YCPDynamicConditionEx {
	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyCheckIfContainerExists.class);

	Document getShipmentListInDoc = null;

	Document getShipmentListOutDoc = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yantra.ycp.japi.YCPDynamicConditionEx#evaluateCondition(com.yantra.yfs.japi.YFSEnvironment,
	 *      java.lang.String, java.util.Map, org.w3c.dom.Document) This method
	 *      will check if Sterling has already recieved the ContainerID from
	 *      Exeter and also If Sterling container already exists for the
	 *      Shipment which is an exception scenario If any one of the above is
	 *      true then No Inventory message recieved from Exeter is ignored and
	 *      an alert is raised to Business user else the message is processed.
	 */

	public boolean evaluateCondition(YFSEnvironment env, String str, Map map,
			Document inXml) {
		// TODO Auto-generated method stub
		log.verbose("Input to AcademyCheckIfContainerExists-evaluateCondition():: "
				+ XMLUtil.getXMLString(inXml));
		try {
			if (!YFCObject.isVoid(inXml)) {
				Element eleInXML = inXml.getDocumentElement();
				getShipmentListInDoc = XMLUtil
						.createDocument(AcademyConstants.ELE_SHIPMENT);
				getShipmentListInDoc
						.getDocumentElement()
						.setAttribute(
								AcademyConstants.ATTR_SHIPMENT_NO,
								eleInXML
										.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));

				Document outputTemplate = YFCDocument
						.getDocumentFor(
								"<Shipments><Shipment> <Extn ExtnExeterContainerId=''/> <Containers><Container/></Containers> </Shipment></Shipments>")
						.getDocument();
				env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST,
						outputTemplate);
				Document getShipmentListOutDoc = AcademyUtil.invokeAPI(env,
						AcademyConstants.API_GET_SHIPMENT_LIST,
						getShipmentListInDoc);
				env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
				log.verbose("getShipmentList API output - "
						+ XMLUtil.getXMLString(getShipmentListOutDoc));

				String strStatus = ((Element) getShipmentListOutDoc
						.getElementsByTagName(AcademyConstants.ELE_SHIPMENT)
						.item(0)).getAttribute(AcademyConstants.ATTR_STATUS);

				// Check if Exeter ContainerID is present or if the Sterling
				// Containers are already created for the shipment
				String strExeterContainerID = XPathUtil.getString(
						getShipmentListOutDoc,
						"/Shipments/Shipment/Extn/@ExtnExeterContainerId");
				log.verbose("ContainerId from Exeter-WMS::" + strExeterContainerID);
				NodeList eleContainerNL = XPathUtil.getNodeList(
						getShipmentListOutDoc,
						"/Shipments/Shipment/Containers/Container");
				log.verbose("eleContainerNL.getLength():::"
						+ eleContainerNL.getLength());

				if (!YFCObject.isNull(strExeterContainerID)
						|| !YFCObject.isVoid(strExeterContainerID)
						|| eleContainerNL.getLength() > 0
						|| strStatus.equals("9000")) {
					
					return true;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public void setProperties(Map arg0) {
		// TODO Auto-generated method stub

	}

}
