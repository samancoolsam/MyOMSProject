package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This custom API sets a flag to denote if the shipment status has to be "Partially Delivered" depending on the status of containers. 
 * If  all of the containers for a shipment is in "Container Delivered" (1300.100) status then the flag which denotes Partial Delivery is set to 'N' 
 * and if any of the containers is in "Packed" (1300) status, the flag is set to 'Y' to indicate it has to be moved to "Partially Delivered" status.
 * 
 * ===============Sample Input processed===========================
 * <AcademyMergedDocument>
 <InputDocument>
 <Shipments TotalNumberOfRecords="">
 <Shipment SellerOrganizationCode="Academy_Direct"
 ShipNode="005" ShipmentKey=""
 ShipmentNo="" Status="">
 <Containers>
 <Container ContainerNo=""
 ShipmentContainerKey=""
 Status="" TrackingNo=""/>
 <Container ContainerNo=""
 ShipmentContainerKey=""
 Status="" TrackingNo=""/>
 </Containers>
 </Shipment>
 </Shipments>
 </InputDocument>
 <EnvironmentDocument>
 <Shipment DeliveryDate="" TrackingNo=""/>
 </EnvironmentDocument>
 </AcademyMergedDocument>
  *===============Sample Output obtained=================================
 * <AcademyMergedDocument>
 <InputDocument>
 <Shipments TotalNumberOfRecords="">
 <Shipment SellerOrganizationCode="Academy_Direct"
 ShipNode="005" ShipmentKey=""
 ShipmentNo="" Status="">
 <Containers>
 <Container ContainerNo=""
 ShipmentContainerKey=""
 Status="" TrackingNo=""/>
 <Container ContainerNo=""
 ShipmentContainerKey=""
 Status="" TrackingNo=""/>
 </Containers>
 </Shipment>
 </Shipments>
 </InputDocument>
 <EnvironmentDocument>
 <Shipment DeliveryDate="" PartialDelivery="" TrackingNo=""/>
 </EnvironmentDocument>
 </AcademyMergedDocument>
 *==========================================================================
 * @author Avinash
 *
 */
public class AcademyShipmentStatusModifierAPI implements YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyShipmentStatusModifierAPI.class);

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * @param env -
	 *            This reference represents the environment object
	 * @return Document - This returns the XML which contains another attribute
	 *         along with the input XML, which indicates the Partial Delivery or
	 *         a full Delivery.
	 * @throws YFCException
	 * 
	 * 
	 */
	public Document modifyShipmentStatus(YFSEnvironment env, Document inputDoc)
			throws YFCException {

		Document inDoc = null;

		try {
			if (AcademyUtil.getContextObject(env, "formatContainerInput") instanceof org.w3c.dom.Document) {
				inDoc = (Document) AcademyUtil.getContextObject(env,
						"formatContainerInput");
			}

			if (inDoc == null) {
				throw new Exception("Input for this document cannot be null");
			}

			log
					.verbose("**************entering the method modifyShipmentStatus() of the class AcademyShipmentStatusModifierAPI********* "
							+ XMLUtil.getXMLString(inDoc));

			Element eleShipment = (Element) inDoc.getDocumentElement();
			NodeList containerList = eleShipment
					.getElementsByTagName("Container");
			String trackingNo = "";

			String status = "Delivered";

			String trackingNumberFromInput = XMLUtil
					.getString(eleShipment,
							"/AcademyMergedDocument/EnvironmentDocument/Shipment/@TrackingNo");

			log.verbose("Tracking # is " + trackingNumberFromInput);

			/**
			 * This for-loop block iterates through all the containers, identifies the container that's to be delivered and also sets the 
			 * indicator to denote the status of the shipment; either Partially Delivered or Delivered.
			 */
			for (int i = 0; i < containerList.getLength(); i++) {

				trackingNo = ((Element) containerList.item(i))
						.getAttribute("TrackingNo");

				log.verbose("Tracking No of this container is " + trackingNo);

				Element container = (Element) containerList.item(i);
				if (trackingNumberFromInput.equals(trackingNo)) {

					container = (Element) containerList.item(i);
					container.setAttribute("Status", "1300.100");

				}

				//Even if one container is in Packed status, break out of the loop setting the indicator to "Partially Delivered".

				if (!container.getAttribute("Status").equals("1300.100")) {
					log
							.verbose("This container is not delivered yet "
									+ trackingNo
									+ " and hence setting the status of the shipment to Partially Delivered(1600.002.99)and breaking out of the loop");
					status = "Partially Delivered";
					break;
				}
			}
			// 1600.002.99
			Element shipment = (Element) XMLUtil.getNode(eleShipment,
					"/AcademyMergedDocument/EnvironmentDocument/Shipment");

			if ("Partially Delivered".equals(status)) {

				log.verbose("Setting Partial Delivery indicator to Y");
				shipment.setAttribute("PartialDelivery",
						AcademyConstants.STR_YES);
			}

			else {

				log.verbose("Setting Partial Delivery indicator to N");
				shipment.setAttribute("PartialDelivery",
						AcademyConstants.STR_NO);
			}

			log.verbose("Final output XML is " + XMLUtil.getXMLString(inDoc));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			YFCException exception = new YFCException();
			exception.setErrorDescription(e.getMessage());
			throw exception;
		}

		log
				.verbose("**************exiting the method modifyShipmentStatus() of the class AcademyShipmentStatusModifierAPI "
						+ XMLUtil.getXMLString(inDoc));

		return inDoc;

	}

}
