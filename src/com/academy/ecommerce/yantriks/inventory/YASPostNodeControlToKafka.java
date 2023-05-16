package com.academy.ecommerce.yantriks.inventory;

import static com.academy.util.constants.AcademyConstants.ATTR_ITEM_ID;
import static com.academy.util.constants.AcademyConstants.ATTR_SHIPMENT_KEY;
import static com.academy.util.constants.AcademyConstants.ATTR_SHIPMENT_TYPE;
import static com.academy.util.constants.AcademyConstants.ATTR_UOM;
import static com.academy.util.constants.AcademyConstants.ELE_SHIPMENT;
import static com.academy.util.constants.AcademyConstants.ELE_SHIPMENT_LINE;
import static com.academy.util.constants.AcademyConstants.ELE_SHIPMENT_LINES;
import static com.academy.util.constants.AcademyConstants.E_SUPPLY;
import static com.academy.util.constants.AcademyConstants.GET_SHIPMENT_LIST_API;
import static com.academy.util.constants.AcademyConstants.SERVICE_YANTRIKS_KAFKA_DELTA_UPDATE;
import static com.academy.util.constants.AcademyConstants.STR_SHIP_TO_STORE;
import static com.academy.util.constants.AcademyConstants.TEMPLATE_GET_SHIPMENT_LIST_KAFKA_UPDATE;
import static com.academy.util.constants.AcademyConstants.UOM_EACH_VAL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.sts.AcademySTSCreateInventoryNodeControl;
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantriks.yih.adapter.util.YantriksCommonUtil;


/**
 * This class added for OMNI-34709, Yantriks Node Control update
 * @author C0027651
 *
 */

public class YASPostNodeControlToKafka {
	private static final YFCLogCategory log = YFCLogCategory.instance(YASPostNodeControlToKafka.class);

	/**
	 * This method will post store's inventory shortage node control to Kafka
	 * @param env
	 * @param docInput sample ---
	 * <StoreBatch IgnoreOrdering="Y" ShipNode="033" StoreBatchKey="202106030134102409093148">
       <Item ItemID="117680450" ShortageQty="2"
        ShortageReason="None on Hand Physically" UnitOfMeasure="EACH"/>
       </StoreBatch>
	 * @throws Exception
	 */
	public static void kafkaUpdateForBatchStoreNC(YFSEnvironment env, Document docInput) throws Exception {

		log.verbose("Input - kafkaUpdateForStoreNC :: " + XMLUtil.getXMLString(docInput));

		Document invShortage = SCXmlUtil.createDocument(AcademyConstants.ELE_BOPIS_NODE_CONTROL);
		preparekafkaStoreNodeControlXml(env, invShortage, docInput);

	}
	
	/**
	 * This method will post bopis/sfs inv shortage to kafka
	 * @param env
	 * @param shipmentKey
	 * @throws Exception
	 */
	
	public static void kafkaUpdateForStoreNC(YFSEnvironment env, String shipmentKey) throws Exception {


		Document invShortage = SCXmlUtil.createDocument(AcademyConstants.ELE_BOPIS_NODE_CONTROL);

		Document docInToGetShipmentDetails = SCXmlUtil.createDocument(ELE_SHIPMENT);
		Element eleRootOfDocInToGetShipment = docInToGetShipmentDetails.getDocumentElement();
		eleRootOfDocInToGetShipment.setAttribute(ATTR_SHIPMENT_KEY, shipmentKey);

		log.verbose("Input to getShipmentList API for store shortage: " + SCXmlUtil.getString(docInToGetShipmentDetails));

		env.setApiTemplate(GET_SHIPMENT_LIST_API, TEMPLATE_GET_SHIPMENT_LIST_KAFKA_UPDATE);
		Document docOutShipmentDetails = AcademyUtil.invokeAPI(env, GET_SHIPMENT_LIST_API, docInToGetShipmentDetails);
		env.clearApiTemplate(GET_SHIPMENT_LIST_API);

		log.verbose("Shipment details output for store shortage"+SCXmlUtil.getString(docOutShipmentDetails));
		Element eleShipments = docOutShipmentDetails.getDocumentElement();
		Element eleShipment = SCXmlUtil.getChildElement(eleShipments, ELE_SHIPMENT);
		if (!YFCObject.isVoid(eleShipment)) {
			
			String shipNode = eleShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
				Element eleShipmentLines = SCXmlUtil.getChildElement(eleShipment, ELE_SHIPMENT_LINES);
				List<Element> listOfShipmentLines = SCXmlUtil.getChildren(eleShipmentLines, ELE_SHIPMENT_LINE);
				for (Element eleCurrentShipmentLine : listOfShipmentLines) {
					Element eleInvNodeControl = invShortage.getDocumentElement();
					eleInvNodeControl.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleCurrentShipmentLine.getAttribute(ATTR_ITEM_ID));
					eleInvNodeControl.setAttribute(AcademyConstants.YIH_LOCATION_ID,shipNode);
					String locationType = YantriksCommonUtil.getLocationType(env, shipNode);
					eleInvNodeControl.setAttribute(AcademyConstants.JSON_ATTR_LOCATION_TYPE, locationType);
//					System.out.println(XMLUtil.getXMLString(invShortage));
					log.verbose("Input - Yantriks Node_Control :: " + XMLUtil.getXMLString(invShortage));
					AcademyUtil.invokeService(env, SERVICE_YANTRIKS_KAFKA_DELTA_UPDATE, invShortage);
					
				}
		}
	}

	/**
	 * This method will post shared inventory shortage node control to Kafka
	 * @param env
	 * @param docInput
	 * @throws Exception
	 */

	public static void kafkaUpdateForSTSNC(YFSEnvironment env, Document docInput) throws Exception {

		Document invShortage = SCXmlUtil.createDocument(AcademyConstants.ELE_SHARED_NODE_CONTROL);
		preparekafkaNodeControlXml(env, invShortage, docInput);

	}

	/**
	 * This method will prepare node control xml for inventory shortage and post it to Kaafka
	 * @param env
	 * @param invShortage
	 * @param docInput
	 * @throws Exception
	 */
	private static void preparekafkaNodeControlXml(YFSEnvironment env, Document invShortage, Document docInput ) throws Exception {

		NodeList nlShipmentLine = docInput.getDocumentElement()
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		String shipNode = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE);

		for (int iSL = 0; iSL < nlShipmentLine.getLength(); iSL++) {

			Element eleShipmentLine = (Element) nlShipmentLine.item(iSL);

			String itemId = eleShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID);

			Element eleInvNodeControl = invShortage.getDocumentElement();
			eleInvNodeControl.setAttribute(AcademyConstants.ATTR_ITEM_ID, itemId);
			eleInvNodeControl.setAttribute(AcademyConstants.YIH_LOCATION_ID,shipNode);
			String locationType = YantriksCommonUtil.getLocationType(env, shipNode);
			eleInvNodeControl.setAttribute(AcademyConstants.JSON_ATTR_LOCATION_TYPE, locationType);
			System.out.println(XMLUtil.getXMLString(invShortage));
		}

		log.verbose("Input - Yantriks Node_Control :: " + XMLUtil.getXMLString(invShortage));
		AcademyUtil.invokeService(env, SERVICE_YANTRIKS_KAFKA_DELTA_UPDATE, invShortage);

	}

	private static void preparekafkaStoreNodeControlXml(YFSEnvironment env, Document invShortage, Document docInput ) throws Exception {

		Element eleRoot = docInput.getDocumentElement();
		String shipNode = eleRoot.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		String itemId = XPathUtil.getString(eleRoot, "//StoreBatch/Item/@ItemID");
		Element eleInvNodeControl = invShortage.getDocumentElement();
		eleInvNodeControl.setAttribute(AcademyConstants.ATTR_ITEM_ID, itemId);
		eleInvNodeControl.setAttribute(AcademyConstants.YIH_LOCATION_ID,shipNode);
		String locationType = YantriksCommonUtil.getLocationType(env, shipNode);
		eleInvNodeControl.setAttribute(AcademyConstants.JSON_ATTR_LOCATION_TYPE,locationType);
//		System.out.println(XMLUtil.getXMLString(invShortage));
		log.verbose("Input - Yantriks Node_Control :: " + XMLUtil.getXMLString(invShortage));
		AcademyUtil.invokeService(env, SERVICE_YANTRIKS_KAFKA_DELTA_UPDATE, invShortage);

	}

	/**
	 * This method will give Code Value's description for yantrik node control
	 * @param env
	 * @return
	 * @throws Exception
	 */
	public static Map<String,String> getNodeControlValue(YFSEnvironment env) throws Exception {

		Document docCommonCode = AcademyCommonCode.getCommonCodeList(env, AcademyConstants.YFS_NODE_CONTROL_CODE_TYPE,
				AcademyConstants.PRIMARY_ENTERPRISE);
		String yantriksNCFlag = XPathUtil.getString(docCommonCode.getDocumentElement(),
				"//CommonCodeList/CommonCode[@CodeValue='" + AcademyConstants.YFS_NODE_CONTROL_CODE_VALUE + "']/@CodeShortDescription");
		String omsNCFlag = XPathUtil.getString(docCommonCode.getDocumentElement(),
				"//CommonCodeList/CommonCode[@CodeValue='" + AcademyConstants.OMS_NODE_CONTROL_CODE_VALUE + "']/@CodeShortDescription");

		Map<String,String> nodeControlFlag = new HashMap<String,String>();
		nodeControlFlag.put(AcademyConstants.OMS_NODE_CONTROL_CODE_VALUE, omsNCFlag);
		nodeControlFlag.put(AcademyConstants.YFS_NODE_CONTROL_CODE_VALUE, yantriksNCFlag);

		log.verbose(" nodeControlFlag:: " + nodeControlFlag);

		return nodeControlFlag;
	}
	

}


