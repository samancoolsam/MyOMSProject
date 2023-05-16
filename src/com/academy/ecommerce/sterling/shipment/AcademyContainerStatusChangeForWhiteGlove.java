/**
 * 
 */
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
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author Administrator
 * 
 */
public class AcademyContainerStatusChangeForWhiteGlove implements YIFCustomApi {

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyContainerStatusChangeForWhiteGlove.class);

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	public Document changeContainerStatus(YFSEnvironment env,Document inDoc)
			throws YFCException {
		log
				.beginTimer(" Begining of AcademyContainerStatusChangeForWhiteGlove->changeContainerStatus() Api");
		
		log.verbose("The input document is "+XMLUtil.getXMLString(inDoc));

		try {

			Element shipmentList = inDoc.getDocumentElement();
			
			
			if (!YFCObject.isVoid(shipmentList)) {

				Element shipment = (Element) XMLUtil.getNode(shipmentList,
						"/AcademyMergedDocument/InputDocument/Shipments");
				
				/*
				 * <Container AcceptOutOfSequenceUpdates="Y" Action="Modify"
				 * TransactionId="Container_Delivered.5001.ex" ContainerNo=""
				 * ShipmentContainerKey="" TrackingNo=""> </Container>
				 */

				Document changeShipmentInDoc = XMLUtil.createDocument("Shipment");
				
				changeShipmentInDoc.getDocumentElement().setAttribute("Action", AcademyConstants.STR_ACTION_MODIFY);
				changeShipmentInDoc.getDocumentElement().setAttribute("ShipmentKey", ((Element) inDoc.getElementsByTagName("Shipment").item(0)).getAttribute("ShipmentKey"));
				
				Element eleContainers = changeShipmentInDoc.createElement("Containers");
				changeShipmentInDoc.getDocumentElement().appendChild(eleContainers);
				
				Element containerElement = changeShipmentInDoc.createElement(AcademyConstants.ELE_CONTAINER);
				
				
				log.verbose("The input to the changeShipment API before the container element is "+XMLUtil.getXMLString(changeShipmentInDoc));
				
				NodeList containerList = XMLUtil
						.getNodeList(shipment,
								"/AcademyMergedDocument/InputDocument/Shipments/Shipment/Containers/Container");

				if (containerList != null) {
					
					for (int i = 0; i < containerList.getLength(); i++) {
						
						Element container = (Element) containerList.item(i);
										
						

				//containerElement.setAttribute("AcceptOutOfSequenceUpdates",
						//AcademyConstants.STR_YES);
				containerElement.setAttribute("Action",
						AcademyConstants.STR_ACTION_MODIFY);
				containerElement.setAttribute("TransactionId",
						"Container_Delivered.5001.ex");

						containerElement.setAttribute("ContainerNo", container
								.getAttribute("ContainerNo"));
						containerElement.setAttribute("ShipmentContainerKey",
								container.getAttribute("ShipmentContainerKey"));
						
						log.verbose("The input to the API is "+XMLUtil.getElementXMLString(containerElement));

						
					}
				}
				
				eleContainers.appendChild(containerElement);
				
				log.verbose("The input to the API is "+XMLUtil.getXMLString(changeShipmentInDoc));
				
				Document changeShipmentOutDoc = AcademyUtil.invokeAPI(env, "changeShipment",
						changeShipmentInDoc);
				log.verbose("The output from the API is "
						+ XMLUtil.getXMLString(changeShipmentOutDoc));

				

			}
		} catch (Exception e) {

			YFCException exception = new YFCException();
			exception.setErrorDescription(e.getLocalizedMessage());
			throw exception;
		}

		log
				.endTimer(" End of AcademyContainerStatusChangeForWhiteGlove->changeContainerStatus() Api");
		
		return inDoc;
	}
	

	

}
