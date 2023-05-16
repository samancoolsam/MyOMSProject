/**#########################################################################################
 *
 * Project Name                : ASO
 * Module                      : OMNI-61620
 * Author                      : C0027652
 * Author Group				   : CTS - POD
 * Date                        : 04-FEB-2022
 * Description				   : This class is added as part of STS 3.0 implementation. It is designed to read the message 
 * 								 from the AcademyAsyncScheduleServerForSTS integration server queue and if inventory check is required , 
 * 								 invokes Yantriks to fetch the getavailability for the item and based on the availability invokes
 * 								 change Order to stamp procureFromNode and order line source controls and then schedules the line.
 * 								 If inventory check is not required, yantriks availability is not fetched and directly schedules the line
 * 								 based on the procureFromNode stamped during record shortage.
 *                                
 * 								

 * ---------------------------------------------------------------------------------
 * Date            	Author         		Version#       		Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 04-FEB-2022		CTS  	 			 1.0           		Initial version
 *
 * #########################################################################################*/

package com.academy.ecommerce.yantriks.inventory;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantriks.yih.adapter.util.YantriksCommonUtil;
import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;

public class AcademyProcessScheduleMsgForSaveTheSale {
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyProcessScheduleMsgForSaveTheSale.class.getName());
	private Properties props;
	private boolean procureFromDC = false;
	private boolean procureFromStore = false;
	private boolean pickFromAlternateStore = false;
	private String strAlternateStoreNode = null;
	Map<String, String> OdrLineKeyFulfillType = new HashMap<String, String>();
	Map<String, String> OdrLineKeyProcureFromNode = new HashMap<String, String>();
	private String strASDistance = null; // OMNI-106259 change
	private String strOriginalShipNodeName = null; // OMNI-107478 change
	private String strPersonInfoKey = null; //OMNI-106943 change
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	/**
	 * @Input <Order AlternateStorePick="Y" InventoryCheckRequired="Y" SaveTheSale=
	 *        "Y" OrderHeaderKey="20230227064408434673544" ShipmentKey=
	 *        "20230227064528434673581"> <OrderLines>
	 *        <OrderLine OrderLineKey="20230227064408434673545" OrderedQty="1.00"
	 *        ProcureFromNode="" ShipNode="038" ShipmentLineKey=
	 *        "20230227064528434673580"> <Item ItemID="122912075"/> <OrderHoldTypes>
	 *        <OrderHoldType HoldType="STS_RELEASE_HOLD" Status="1100"/>
	 *        </OrderHoldTypes> </OrderLine> </OrderLines> </Order>
	 * @param env
	 * @param inDoc
	 * @throws Exception
	 */
	public void validateAndScheduleSTSLine(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer("START :: AcademyProcessScheduleMsgForSaveTheSale.validateAndScheduleSTSLine() :: START");
		log.debug("Input Document of validateAndScheduleSTSLine() :: " + SCXmlUtil.getString(inDoc));
		String sOrderHeaderKey = null;
		Document docReqAttributesForAS = null;
		boolean cancelLine = false;
		try {
			String strNewShipNode = null;
			sOrderHeaderKey = XMLUtil.getString(inDoc, "/Order/@OrderHeaderKey");
			String strInventoryCheckRequired = XMLUtil.getString(inDoc, "/Order/@InventoryCheckRequired");
			String strAlternateStorePick = XMLUtil.getString(inDoc, "/Order/@AlternateStorePick");
			String strSaveTheSale = XMLUtil.getString(inDoc, "/Order/@SaveTheSale");
			String strIsSTSOrder = XMLUtil.getString(inDoc, "/Order/@IsSTS2Shipment");
			String sOrderLineKey = XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine/@OrderLineKey");
			String procureFromNode = XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine/@ProcureFromNode");
			boolean isInventoryCheckRequired = strInventoryCheckRequired.equalsIgnoreCase("Y") ? true : false;
			boolean isAlternateStorePick = strAlternateStorePick.equalsIgnoreCase("Y") ? true : false;
			boolean isSaveTheSale = strSaveTheSale.equalsIgnoreCase("Y") ? true : false;
			log.verbose("isInventoryCheckRequired :: " + isInventoryCheckRequired + " | isSaveTheSale :: "
					+ isSaveTheSale + " | isAlternateStorePick :: " + isAlternateStorePick);
			if (isInventoryCheckRequired && !isAlternateStorePick) {
				log.verbose("STS3.0 Orderlines to execute");
				if (!YFCCommon.isVoid(procureFromNode)) {
					log.verbose("STS 3.0 :: Sequence to execute DC --> Store");
					strNewShipNode = procureFromDC(env, inDoc, sOrderLineKey, procureFromNode);
					if (YFCCommon.isVoid(strNewShipNode)) {
						log.verbose("No Inventory in DC");
						strNewShipNode = procureFromStore(env, inDoc, sOrderLineKey, "");
						if (YFCCommon.isVoid(strNewShipNode)) {
							log.verbose("No Inventory in Store");
							procureFromStore = false;
						}
					}
				} else {
					log.verbose("STS 3.0 :: Sequence to execute Store --> DC");
					strNewShipNode = procureFromStore(env, inDoc, sOrderLineKey, "");
					if (YFCCommon.isVoid(strNewShipNode)) {
						log.verbose("No Inventory in Store");
						procureFromNode = getStoreDCMapping(env, inDoc);
						strNewShipNode = procureFromDC(env, inDoc, sOrderLineKey, procureFromNode);
						if (YFCCommon.isVoid(strNewShipNode)) {
							log.verbose("No Inventory in DC");
							procureFromDC = false;
						}
					}
				}
			}

			else if (!isInventoryCheckRequired && isAlternateStorePick) {

				log.verbose("Alternate Store Pick OrderLines to execute");
				if (isSaveTheSale) {
					log.verbose("STS1.0/STS2.0 + Alternate Store Order Lines to execute");
					if (!YFCCommon.isVoid(procureFromNode)) {
						log.verbose("STS1.0 + Alternate Store");
						log.verbose("Sequence :: ProcureFrom DC -> AlternateStore");
						strNewShipNode = procureFromDC(env, inDoc, sOrderLineKey, procureFromNode);
						if (YFCCommon.isVoid(strNewShipNode)) {
							log.verbose("No Inventory in DC");
							docReqAttributesForAS = pickFromAlternateStore(env, inDoc, sOrderLineKey);
							strNewShipNode = XMLUtil.getString(docReqAttributesForAS, "//Promise/@ShipNode");
							if (YFCCommon.isVoid(strNewShipNode)) {
								log.verbose("Cancel :: true \n No Inventory in DC & Alternate Store");
								cancelLine = true;
								pickFromAlternateStore = false;
							}
						}
					} else {
						log.verbose("STS2.0 + Alternate Store");
						log.verbose("Sequence :: AlternateStore Pick -> ProcureFrom Store");
						docReqAttributesForAS = pickFromAlternateStore(env, inDoc, sOrderLineKey);
						strNewShipNode = XMLUtil.getString(docReqAttributesForAS, "//Promise/@ShipNode");
						if (YFCCommon.isVoid(strNewShipNode)) {
							log.verbose("No Inventory in Alternate Store");
							pickFromAlternateStore = false;
						}
					}
				} else {
					log.verbose("Save The Sale :: false | Alternate Store :: true");
					docReqAttributesForAS = pickFromAlternateStore(env, inDoc, sOrderLineKey);
					strNewShipNode = XMLUtil.getString(docReqAttributesForAS, "//Promise/@ShipNode");
					if (YFCCommon.isVoid(strNewShipNode)) {
						cancelLine = true;
						pickFromAlternateStore = false;
					}
				}
			}

			else if (isInventoryCheckRequired && isAlternateStorePick) {
				log.verbose("STS3.0 + Alternate Store Order Lines to execute");

				if (!YFCCommon.isVoid(procureFromNode)) {
					log.verbose("Sequence :: procureFrom DC -> AlternateStore Pick -> ProcureFrom Store");
					strNewShipNode = procureFromDC(env, inDoc, sOrderLineKey, procureFromNode);
					if (YFCCommon.isVoid(strNewShipNode)) {
						log.verbose("No Inventory in DC");
						docReqAttributesForAS = pickFromAlternateStore(env, inDoc, sOrderLineKey);
						strNewShipNode = XMLUtil.getString(docReqAttributesForAS, "//Promise/@ShipNode");
						if (YFCCommon.isVoid(strNewShipNode)) {
							log.verbose("No Inventory in Alternate Store");
							strNewShipNode = procureFromStore(env, inDoc, sOrderLineKey, "");
							if (YFCCommon.isVoid(strNewShipNode)) {
								log.verbose("No Inventory in Store");
								procureFromStore = false;
							}
						}
					}
				} else {
					log.verbose("Sequence :: AlternateStore Pick -> ProcureFrom Store -> procureFrom DC");
					docReqAttributesForAS = pickFromAlternateStore(env, inDoc, sOrderLineKey);
					strNewShipNode = XMLUtil.getString(docReqAttributesForAS, "//Promise/@ShipNode");
					if (YFCCommon.isVoid(strNewShipNode)) {
						log.verbose("No Inventory in Alternate Store");
						strNewShipNode = procureFromStore(env, inDoc, sOrderLineKey, "");
						if (YFCCommon.isVoid(strNewShipNode)) {
							log.verbose("No Inventory in Store");
							procureFromNode = getStoreDCMapping(env, inDoc);
							strNewShipNode = procureFromDC(env, inDoc, sOrderLineKey, procureFromNode);
							if (YFCCommon.isVoid(strNewShipNode)) {
								log.verbose("No Inventory in DC");
								procureFromDC = false;
							}
						}
					}
				}
			}

			// OMNI-70667
			if (AcademyConstants.STR_YES.equalsIgnoreCase(strIsSTSOrder)) {
				env.setTxnObject("IsResourcedOrder", "Y");
			}
			// OMNI-70667

			if (pickFromAlternateStore) {
				if (!YFCCommon.isVoid(docReqAttributesForAS)) {
					strASDistance = XMLUtil.getString(docReqAttributesForAS, "//Promise/@Distance");
					strOriginalShipNodeName = XMLUtil.getString(docReqAttributesForAS, "//Promise/@LastName");
					strPersonInfoKey = XMLUtil.getString(docReqAttributesForAS, "//Promise/@PersonInfoKey");  //OMNI-106943 change
				}
			}

			log.verbose(
					"The values of internal flags before calling invokeChangeOrder() method::: pickFromAlternateStore= "
							+ pickFromAlternateStore + ", procureFromStore= " + procureFromStore + ", procureFromDC= "
							+ procureFromDC + ", strNewShipNode= " + strNewShipNode + ", cancelLine= " + cancelLine);
			
			if (((pickFromAlternateStore || procureFromStore || procureFromDC) && !YFCCommon.isVoid(strNewShipNode))
					|| cancelLine) {
				invokeChangeOrder(env, inDoc, strNewShipNode, cancelLine);
			}

			if (!cancelLine) {
				env.setTxnObject("YntrxResponse", null);
				scheduleSTSOrderLine(env, sOrderHeaderKey, sOrderLineKey);
			}

		} catch (Exception e) {
			YFSException yfsException = new YFSException(e.getMessage(), AcademyConstants.ERR_CODE_49, e.getMessage());
			log.info(
					"Exception occurred in AcademyProcessScheduleMsgForSaveTheSale::validateAndScheduleSTSLine() :: for OrderheaderKey : "
							+ sOrderHeaderKey + "Exception : " + e.getMessage());
			e.printStackTrace();
			throw yfsException;
		} finally {
			log.endTimer("END :: AcademyProcessScheduleMsgForSaveTheSale.validateAndScheduleSTSLine() :: END");
		}

	}

	private void scheduleSTSOrderLine(YFSEnvironment env, String sOrderHeaderKey, String sOrderLineKey) {
		log.verbose(" AcademyProcessScheduleMsgForSaveTheSale-->scheduleSTSOrderLine() method START");
		try {
			Document docInScheduleOdr = XMLUtil.createDocument(AcademyConstants.ELE_SCHEDULE_ORDER);
			Element eleInScheduleOdr = docInScheduleOdr.getDocumentElement();
			if (pickFromAlternateStore) {
				eleInScheduleOdr.setAttribute(AcademyConstants.ATTR_SCHEDULE_AND_RELEASE, AcademyConstants.STR_YES);
			} else {
				eleInScheduleOdr.setAttribute(AcademyConstants.ATTR_SCHEDULE_AND_RELEASE, AcademyConstants.STR_NO);
			}
			eleInScheduleOdr.setAttribute(AcademyConstants.ATTR_IGNORE_RELEASE_DATE, AcademyConstants.STR_NO);
			eleInScheduleOdr.setAttribute(AcademyConstants.ATTR_IGNORE_ORDERING, AcademyConstants.STR_YES);
			eleInScheduleOdr.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, sOrderHeaderKey);
			eleInScheduleOdr.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, sOrderLineKey);
			log.debug("Input Document of scheduleOrder ::" + XMLUtil.getXMLString(docInScheduleOdr));
			AcademyUtil.invokeAPI(env, AcademyConstants.API_SCHEDULE_ORDER, docInScheduleOdr);

			// Below method is invoked to send Delay Status Update To WCS for ASP line.
			if (pickFromAlternateStore) {
				log.verbose(
						"Current Line Converted to AS. Hence invoking sendDelayStatusUpdateToWCSForASLine() method to send delay update to WCS");
				sendDelayStatusUpdateToWCSForASLine(env, sOrderLineKey, sOrderHeaderKey);
			}

		} catch (Exception exp) {
			log.verbose("Excetpion occured in AcademyProcessScheduleMsgForSaveTheSale-->scheduleSTSOrderLine() method");
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}
		log.verbose(" AcademyProcessScheduleMsgForSaveTheSale-->scheduleSTSOrderLine() method END");
	}

	private String getStoreDCMapping(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->getStoreDCMapping() method START");
		String strShipNode = XMLUtil.getString(inDoc, ".//OrderLines/OrderLine/@ShipNode");
		String strDCMappingInput = "<CommonCode CodeType='" + AcademyConstants.STS_STORE_DC_MAPPING + "' CodeValue='"
				+ strShipNode + "'/>";
		Document inDocStoreDCmapCC = XMLUtil.getDocument(strDCMappingInput);
		Document outDocStoreDCmapCC = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST,
				inDocStoreDCmapCC);
		String strProcureFromDCNode = XMLUtil.getString(outDocStoreDCmapCC, ".//CommonCode/@CodeShortDescription");
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->getStoreDCMapping() method END");
		return strProcureFromDCNode;
	}

	private String procureFromDC(YFSEnvironment env, Document inDoc, String sOrderLineKey, String procureFromNode)
			throws Exception {
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->procureFromDC() method START");
		String strNewShipNode = null;
		try {
			setInventoryDetails(env, "STS", procureFromNode);
			setTxnObjectFulfillmentType(env, sOrderLineKey, "STS");
			setTxnObjectProcureFromNode(env, sOrderLineKey, procureFromNode);
			procureFromDC = true;
			procureFromStore = false;
			pickFromAlternateStore = false;
			strNewShipNode = findInventory(env, inDoc, procureFromNode);
			if (env.getTxnObject("YntrxGetAvailibiltyResp") != null) {
				HashMap<String, Map<String, List<String>>> itemToSupplyMap = (HashMap<String, Map<String, List<String>>>) env
						.getTxnObject("YntrxGetAvailibiltyResp");
				log.debug("itemToSupplyMap :" + itemToSupplyMap);
			}
			log.verbose("DC node :: " + strNewShipNode);
		} catch (Exception exp) {
			log.verbose("Excetpion occured in AcademyProcessScheduleMsgForSaveTheSale-->procureFromDC() method");
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->procureFromDC() method END");
		return strNewShipNode;
	}

	private String procureFromStore(YFSEnvironment env, Document inDoc, String sOrderLineKey, String procureFromNode)
			throws Exception {
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->procureFromStore() method STRAT");
		String strNewShipNode = null;
		try {
			setInventoryDetails(env, "STS", procureFromNode);
			setTxnObjectFulfillmentType(env, sOrderLineKey, "STS");
			setTxnObjectProcureFromNode(env, sOrderLineKey, procureFromNode);
			procureFromDC = false;
			procureFromStore = true;
			pickFromAlternateStore = false;
			strNewShipNode = findInventory(env, inDoc, procureFromNode);
			if (env.getTxnObject("YntrxGetAvailibiltyResp") != null) {
				HashMap<String, Map<String, List<String>>> itemToSupplyMap = (HashMap<String, Map<String, List<String>>>) env
						.getTxnObject("YntrxGetAvailibiltyResp");
				log.debug("itemToSupplyMap :" + itemToSupplyMap);
			}
			log.verbose("Store node :: " + strNewShipNode);
		} catch (Exception exp) {
			log.verbose("Excetpion occured in AcademyProcessScheduleMsgForSaveTheSale-->procureFromStore() method");
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->procureFromStore() method END");
		return strNewShipNode;
	}

	private Document pickFromAlternateStore(YFSEnvironment env, Document inDoc, String sOrderLineKey) throws Exception {
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->pickFromAlternateStore() method START");
		Document docReqAttributesForAS = null;
		String strOriginalShipNodeName = null;
		String strZipCode = null;
		Element elePersonInfoShipTo = null;
		try {
			setInventoryDetails(env, "BOPIS", "");
			setTxnObjectFulfillmentType(env, sOrderLineKey, "BOPIS");
			setTxnObjectProcureFromNode(env, sOrderLineKey, "");
			procureFromDC = false;
			procureFromStore = false;
			pickFromAlternateStore = true;
			Document outDocGetOrderDetails = invokeGetOrderDetails(env, inDoc);
			strAlternateStoreNode = XPathUtil.getString(outDocGetOrderDetails, "/Order/Extn/@ExtnASShipNode");
			Element eleOrderLine = XMLUtil.getElementByXPath(outDocGetOrderDetails,
					".//OrderLines/OrderLine[@OrderLineKey ='" + sOrderLineKey + "']");
			if (!YFCCommon.isVoid(eleOrderLine)) {
				elePersonInfoShipTo = (Element) eleOrderLine
						.getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).item(0);
				if (!YFCCommon.isVoid(elePersonInfoShipTo)) {
					strZipCode = elePersonInfoShipTo.getAttribute(AcademyConstants.ZIP_CODE);
				}
			} else {
				log.verbose("Current OrderLinekey is not found from the getOrderDetails API output");
			}
			
			if(!YFCCommon.isVoid(strZipCode) && strZipCode.length() >= 5) {
				String strZipCode5D = 	strZipCode.substring(0,5);	// Trimming ZipCode to 5 Digits
				log.verbose("5 Digit ZipCode is :: "+strZipCode5D);
				docReqAttributesForAS = findInventoryForAS(env, inDoc, strZipCode5D);
			}else {
				log.verbose("ZipCode is either NULL or Less than 5 Digits. Hence not invoking findInventory API!!");
				YFSException e = new YFSException();
				e.setErrorCode("Invalid ZipCode");
				e.setErrorDescription(
						"ZipCode in AcademyProcessScheduleMsgForSaveTheSale.pickFromAlternateStore is either NULL or Less than 5 Digits.");
				throw e;
			}
			
			if (!YFCCommon.isVoid(docReqAttributesForAS) && !YFCCommon.isVoid(elePersonInfoShipTo)) {
				strOriginalShipNodeName = elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_LNAME); // OMNI-107478 change
				log.verbose("Value of OriginalShipNodeName is :: " + strOriginalShipNodeName);
				docReqAttributesForAS.getDocumentElement().setAttribute("LastName", strOriginalShipNodeName);
				log.verbose("Document having required attributes for AS :: " + XMLUtil.getXMLString(docReqAttributesForAS));
			}else {
				log.verbose("Document returned from findInventoryForAS() method is NULL because no ShipNode found");
			}
			
			if (env.getTxnObject("YntrxGetAvailibiltyResp") != null) {
				HashMap<String, Map<String, List<String>>> itemToSupplyMap = (HashMap<String, Map<String, List<String>>>) env
						.getTxnObject("YntrxGetAvailibiltyResp");
				log.debug("itemToSupplyMap :" + itemToSupplyMap);
			}
		} catch (Exception exp) {
			log.verbose(
					"Excetpion occured in AcademyProcessScheduleMsgForSaveTheSale-->pickFromAlternateStore() method");
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->pickFromAlternateStore() method END");
		return docReqAttributesForAS;
	}

	private void setInventoryDetails(YFSEnvironment env, String fulfillmenttype, String procureFromNode)
			throws JSONException {
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->setInventoryDetails() method START");
		log.verbose("Fulfillment Type :: " + fulfillmenttype + " ProcureFromNode :: " + procureFromNode
				+ " values inside setInventoryDetails() method");
		Object ObjYntrxGetAvail = env.getTxnObject("YntrxGetAvailibiltyResp");
		if (!YFCCommon.isVoid(ObjYntrxGetAvail)) {
			env.setTxnObject("YntrxGetAvailibiltyResp", null);
			String YntrxResponse = (String) env.getTxnObject("YntrxResponse");
			HashMap<String, Map<String, List<String>>> hmapItemLocationAtp = getInventoryDetailsFromYantriksResponse(
					env, YntrxResponse, fulfillmenttype, procureFromNode);
			log.verbose("Prepare Item Node ATP using Trnx Object YntrxGetAvailibiltyResp");
			log.verbose("Set YntrxGetAvailibiltyResp :: " + hmapItemLocationAtp);
			if (!YFCCommon.isVoid(hmapItemLocationAtp) && hmapItemLocationAtp.size() > 0) {
				env.setTxnObject("YntrxGetAvailibiltyResp", hmapItemLocationAtp);
			}
		}
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->setInventoryDetails() method END");
	}

	private HashMap<String, Map<String, List<String>>> getInventoryDetailsFromYantriksResponse(YFSEnvironment env,
			String YntrxResponse, String requiredFulfillmenttype, String procureFromNode) throws JSONException {
		HashMap<String, Map<String, List<String>>> itemLocationAtp = new HashMap<>();
		Map<String, List<String>> yihSupplies = new HashMap<>();
		String strReqFulFullment = null;
		if (requiredFulfillmenttype.equals(AcademyConstants.V_FULFILLMENT_TYPE_BOPIS)) {
			strReqFulFullment = AcademyConstants.STR_PICK;
		} else if (requiredFulfillmenttype.equals(AcademyConstants.V_FULFILLMENT_TYPE_STS)) {
			if (!YFCCommon.isVoid(procureFromNode)) {
				strReqFulFullment = AcademyConstants.V_FULFILLMENT_TYPE_STS;
			} else {
				strReqFulFullment = AcademyConstants.V_FULFILLMENT_TYPE_STSFS;
			}
		}
		JSONObject jResponseObj = new JSONObject(YntrxResponse);
		JSONArray availByProductsArray = jResponseObj.getJSONArray(AcademyConstants.JSON_ATTR_AVAILABILITY_BY_PRODUCTS);
		JSONObject currentProductAvail = (JSONObject) availByProductsArray.getJSONObject(0);
		String productId = currentProductAvail.getString("productId");
		JSONArray arryAvailabilityByFulfillmentTypes = currentProductAvail
				.getJSONArray("availabilityByFulfillmentTypes");
		int iFulfillmentTypes = arryAvailabilityByFulfillmentTypes.length();
		for (int iCount = 0; iCount < iFulfillmentTypes; iCount++) {
			JSONObject oAvailabilityByFulfillmentTypes = (JSONObject) arryAvailabilityByFulfillmentTypes
					.getJSONObject(iCount);
			String fulfillmentType = oAvailabilityByFulfillmentTypes
					.getString(AcademyConstants.JSON_ATTR_FULFILLMENT_TYPE);
			if (fulfillmentType.equalsIgnoreCase(strReqFulFullment)) {
				JSONArray arryAvailabilityDetails = oAvailabilityByFulfillmentTypes.getJSONArray("availabilityDetails");
				JSONObject oAvailabilityDetails = (JSONObject) arryAvailabilityDetails.getJSONObject(0);
				// String atp = oAvailabilityDetails.getString("atp");
				int iatp = oAvailabilityDetails.getInt(AcademyConstants.JSON_ATTR_ATP);
				if (iatp != 0) {
					JSONArray arryAvailabilityByLocations = oAvailabilityDetails
							.getJSONArray("availabilityByLocations");
					int iLocation = arryAvailabilityByLocations.length();
					String locationId = null;
					for (int jCount = 0; jCount < iLocation; jCount++) {
						List<String> atpAndDate = new ArrayList<>();
						JSONObject OAvailabilityByLocations = (JSONObject) arryAvailabilityByLocations
								.getJSONObject(jCount);
						locationId = OAvailabilityByLocations.getString("locationId");
						// String locationatp = oAvailabilityDetails.getString("atp");
						int iLocationatp = OAvailabilityByLocations.getInt(AcademyConstants.JSON_ATTR_ATP);
						SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.US);
						String currentDate = YantriksCommonUtil.getCurrentDateOrTimeStamp(formatter);
						if (log.isDebugEnabled())
							log.debug("Current Date :: " + currentDate);
						if (iLocationatp != 0) {
							atpAndDate.add(String.valueOf(iLocationatp));
							atpAndDate.add(currentDate);
						}
						JSONArray futureDateArray = OAvailabilityByLocations
								.getJSONArray(AcademyConstants.JSON_ATTR_FUTURE_QTY_DATES);
						if (futureDateArray.length() != 0) {
							for (int l = 0; l < futureDateArray.size(); l++) {
								JSONObject currentFutureDateObj = futureDateArray.getJSONObject(l);
								int fQty = currentFutureDateObj.getInt(AcademyConstants.JSON_ATTR_FUTURE_QTY);
								String futureDate = currentFutureDateObj
										.getString(AcademyConstants.JSON_ATTR_FUTURE_QTY_DATE);
								if (fQty != 0) {
									atpAndDate.add(String.valueOf(fQty));
									atpAndDate.add(futureDate);
								}
							}
						}
						yihSupplies.put(locationId, atpAndDate);
					}
					itemLocationAtp.put(productId, yihSupplies);
					break;
				}
			}
		}
		return itemLocationAtp;
	}

	private YFSEnvironment getNewEnvObject(YFSEnvironment env)
			throws YIFClientCreationException, ParserConfigurationException, YFSException, RemoteException {
		log.verbose("Inside AcademyProcessScheduleMsgForSaveTheSale-->getNewEnvObject() method");

		YIFApi api = YIFClientFactory.getInstance().getLocalApi();
		Document env1Doc = XMLUtil.createDocument(AcademyConstants.ELE_ENV);
		env1Doc.getDocumentElement().setAttribute(AcademyConstants.ATTR_USR_ID, "admin");
		env1Doc.getDocumentElement().setAttribute(AcademyConstants.ATTR_PROG_ID, env.getProgId());
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->getNewEnvObject() method END");
		return api.createEnvironment(env1Doc);
	}

	private Document invokeGetOrderLineListForAS(YFSEnvironment env, String sOrderLineKey, String sOrderHeaderKey)
			throws Exception {
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->invokeGetOrderLineListForAS() method START");
		Document outDocGetOrderLineList = null;
		try {
			if (!YFCCommon.isVoid(sOrderLineKey)) {
				String strGetOrderLineList = "<OrderLine OrderLineKey='" + sOrderLineKey
						+ "'> <Extn ExtnIsASP='Y'/> </OrderLine>";
				Document inDocGetOrderLineList = XMLUtil.getDocument(strGetOrderLineList);
				Document outDocGetOrderLineListTemplate = XMLUtil.getDocument("<OrderLineList LastOrderLineKey=''>\r\n"
						+ "	<OrderLine OrderLineKey='' ChainedFromOrderLineKey='' FulfillmentType='' MinLineStatus='' OriginalOrderedQty='' OrderedQty='' ShipNode=''>\r\n"
						+ "		<Order DocumentType='' OrderNo=''> \r\n"
						+ "			<Extn ExtnOriginalShipNode='' ExtnASShipNode=''/>\r\n" + "		</Order>\r\n"
						+ "		<Extn ExtnInitialPromiseDate='' ExtnWCOrderItemIdentifier='' ExtnIsASP=''/>	\r\n"
						+ "	</OrderLine>\r\n" + "</OrderLineList>");
				env.setApiTemplate("getOrderLineList", outDocGetOrderLineListTemplate);
				log.verbose(
						"In Document getOrderLineList for AS Line:: " + XMLUtil.getXMLString(inDocGetOrderLineList));
				outDocGetOrderLineList = AcademyUtil.invokeAPI(env, "getOrderLineList", inDocGetOrderLineList);
				env.clearApiTemplate("getOrderLineList");
				log.verbose(
						"Out Document getOrderLineList for AS Line :: " + XMLUtil.getXMLString(outDocGetOrderLineList));
			}else {
				log.verbose("OrderLineKey is null. Hence not invoking getOrderLineList API!!");
				YFSException e = new YFSException();
				e.setErrorCode("OrderLineKey is Void");
				e.setErrorDescription(
						"OrderLineKey in AcademyProcessScheduleMsgForSaveTheSale.invokeGetOrderLineListForAS is NULL.");
				throw e;
			}
		} catch (Exception exp) {
			log.verbose(
					"Excetpion occured in AcademyProcessScheduleMsgForSaveTheSale-->invokeGetOrderLineListForAS() method");
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}

		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->invokeGetOrderLineListForAS() method END");
		return outDocGetOrderLineList;
	}

	private Document invokeGetOrderDetails(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->invokeGetOrderDetails() method START");
		Document outDocGetOrderDetails = null;
		try {
			String sOrderHeaderKey = XMLUtil.getString(inDoc, "/Order/@OrderHeaderKey");
			if (!YFCCommon.isVoid(sOrderHeaderKey)) {
				String strGetOrderDetails = "<Order OrderHeaderKey='" + sOrderHeaderKey + "'/>";
				Document inDocGetOrderDetails = XMLUtil.getDocument(strGetOrderDetails);
				Document outDocGetOrderDetailsTemplate = XMLUtil
						.getDocument("<Order DocumentType='' OrderHeaderKey='' OrderNo=''>\r\n"
								+ "	<Extn ExtnOriginalShipNode='' ExtnASShipNode='' ExtnASDistance=''/>\r\n"
								+ "	<OrderLines>\r\n"
								+ "		<OrderLine OrderLineKey='' ChainedFromOrderLineKey='' FulfillmentType='' MinLineStatus='' OriginalOrderedQty='' OrderedQty='' ShipNode=''>\r\n"
								+ "			<PersonInfoShipTo LastName='' ZipCode=''/>\r\n"
								+ "			<Extn ExtnNoOfShortPicks='' ExtnInitialPromiseDate='' ExtnWCOrderItemIdentifier='' ExtnIsASP=''/>	\r\n"
								+ "		</OrderLine>\r\n" + "	</OrderLines>\r\n" + "</Order>");
				env.setApiTemplate("getOrderDetails", outDocGetOrderDetailsTemplate);
				log.verbose("In Document getOrderDetails :: " + XMLUtil.getXMLString(inDocGetOrderDetails));
				outDocGetOrderDetails = AcademyUtil.invokeAPI(env, "getOrderDetails", inDocGetOrderDetails);
				env.clearApiTemplate("getOrderDetails");
				log.verbose("Out Document getOrderDetails :: " + XMLUtil.getXMLString(outDocGetOrderDetails));
			}else {
				log.verbose("OrderHeaderKey is null. Hence not invoking getOrderDetails API!!");
				YFSException e = new YFSException();
				e.setErrorCode("OrderHeaderKey is Void");
				e.setErrorDescription(
						"OrderHeaderKey in AcademyProcessScheduleMsgForSaveTheSale.invokeGetOrderDetails is NULL.");
				throw e;
			}
		} catch (Exception exp) {
			log.verbose(
					"Excetpion occured in AcademyProcessScheduleMsgForSaveTheSale-->invokeGetOrderDetails() method");
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}

		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->invokeGetOrderDetails() method END");
		return outDocGetOrderDetails;
	}

	/**
	 * 
	 * @param env
	 * @param sOrderHeaderKey
	 * @param sOrderLineKey
	 * @param strNewShipNode
	 * @param cancelLine
	 * @throws Exception
	 *             <Order Override="Y" Action="MODIFY" OrderHeaderKey=
	 *             "20220701003103277659956"> <OrderLines>
	 *             <OrderLine FulfillmentType="BOPIS" OrderLineKey=
	 *             "20220701003103277659957" ProcureFromNode="" ShipNode= "165"/>
	 *             </OrderLines> </Order>
	 */
	private void invokeChangeOrder(YFSEnvironment env, Document inDoc, String strNewShipNode, boolean cancelLine)
			throws Exception {
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->invokeChangeOrder() method START");
		try {
			String strInventoryCheckRequired = XMLUtil.getString(inDoc, "/Order/@InventoryCheckRequired");
			String strAlternateStorePick = XMLUtil.getString(inDoc, "/Order/@AlternateStorePick");
			boolean isInventoryCheckRequired = strInventoryCheckRequired.equalsIgnoreCase("Y") ? true : false;
			boolean isAlternateStorePick = strAlternateStorePick.equalsIgnoreCase("Y") ? true : false;
			log.verbose("isInventoryCheckRequired :: " + isInventoryCheckRequired + " isAlternateStorePick :: "
					+ isAlternateStorePick + " values inside invokeChangeOrder() method");
			Element inEle = inDoc.getDocumentElement();
			String sOrderHeaderKey = inEle.getAttribute(AcademyConstants.STR_ORDR_HDR_KEY);
			Element inEleOrderLine = (Element) XMLUtil.getNode(inDoc, ".//OrderLines/OrderLine");
			String sOrderLineKey = inEleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			String strShipNode = inEleOrderLine.getAttribute(AcademyConstants.ELE_SHIP_NODE);

			Document inDocChangeOrder = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			Element inEleChangeOrder = inDocChangeOrder.getDocumentElement();
			inEleChangeOrder.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);
			inEleChangeOrder.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY);
			inEleChangeOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, sOrderHeaderKey);
			Element eleOrderLines = inDocChangeOrder.createElement(AcademyConstants.ELE_ORDER_LINES);
			Element eleOrderLine = inDocChangeOrder.createElement(AcademyConstants.ELE_ORDER_LINE);
			eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, sOrderLineKey);
			eleOrderLines.appendChild(eleOrderLine);
			inEleChangeOrder.appendChild(eleOrderLines);

			if (cancelLine) {
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, "0");
				// OMNI-105693 - Fix
				String strReasonCode = XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine/@ReasonCode");
				if (!YFCCommon.isVoid(strReasonCode)) {
					Element elechangeOrderNotes = inDocChangeOrder.createElement(AcademyConstants.ELE_NOTES);
					eleOrderLine.appendChild(elechangeOrderNotes);
					Element elechangeOrderNote = inDocChangeOrder.createElement(AcademyConstants.ELE_NOTE);
					elechangeOrderNotes.appendChild(elechangeOrderNote);
					elechangeOrderNote.setAttribute(AcademyConstants.ATTR_OPERATION,
							AcademyConstants.STR_OPERATION_VAL_CREATE);
					elechangeOrderNote.setAttribute(AcademyConstants.ATTR_NOTE_TEXT, strReasonCode);
					elechangeOrderNote.setAttribute(AcademyConstants.ATTR_REASON_CODE, strReasonCode);
				}
				// OMNI-105693 - Fix
			} else {
				if (pickFromAlternateStore) {
					Element inEleOrderExtn = inDocChangeOrder.createElement(AcademyConstants.ELE_EXTN);
					if (YFCCommon.isVoid(strAlternateStoreNode)) {
						inEleOrderExtn.setAttribute(AcademyConstants.STR_EXTN_AS_SHIP_NODE, strNewShipNode);
						inEleOrderExtn.setAttribute(AcademyConstants.STR_EXTN_ORIGINAL_SHIP_NODE, strShipNode);
						inEleOrderExtn.setAttribute(AcademyConstants.ATTR_EXTN_AS_DISTANCE, strASDistance); // OMNI-106259 Change
						inEleOrderExtn.setAttribute(AcademyConstants.ATTR_EXTN_ORIGINAL_SHIPNODE_NAME,
								strOriginalShipNodeName);
					}
					inEleChangeOrder.appendChild(inEleOrderExtn);
					eleOrderLine.setAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE,
							AcademyConstants.V_FULFILLMENT_TYPE_BOPIS);
					eleOrderLine.setAttribute(AcademyConstants.ATTR_PROCURE_FROM_NODE, AcademyConstants.STR_BLANK);
					eleOrderLine.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strNewShipNode);
					eleOrderLine.setAttribute(AcademyConstants.ATTRIBUTE_SHIP_TO_KEY, strPersonInfoKey); //OMNI-106943
					NodeList ndHoldType = XMLUtil.getNodeList(inDoc,
							".//OrderLine/OrderHoldTypes/OrderHoldType[@HoldType='STS_RELEASE_HOLD' and @Status='1100']");
					if (ndHoldType.getLength() > 0) {
						Element eleOrderHoldTypes = inDocChangeOrder
								.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPES);
						eleOrderLine.appendChild(eleOrderHoldTypes);
						Element eleOrderHoldType = inDocChangeOrder.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPE);
						eleOrderHoldTypes.appendChild(eleOrderHoldType);
						eleOrderHoldType.setAttribute(AcademyConstants.ATTR_HOLD_TYPE,
								AcademyConstants.STR_STS_RELEASE_HOLD);
						eleOrderHoldType.setAttribute(AcademyConstants.ATTR_STATUS,
								AcademyConstants.STR_HOLD_RESOLVED_STATUS);
					}

					DateFormat dateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
					Calendar cal = Calendar.getInstance();
					// OMNI-105510 Updating the Initial Promise Date for ASP Line START
					String strASDefaultPickUpHrs = props.getProperty(AcademyConstants.PROP_AS_DEFAULT_PICKUP_HRS);
					log.verbose("ASP Default PickUp Hrs Configured value is :: " + strASDefaultPickUpHrs + "hrs");
					String strRevisedPromiseDate = null;
					if (!YFCCommon.isVoid(strASDefaultPickUpHrs)) {
						int intASDefaultPickUpHrs = Integer.parseInt(strASDefaultPickUpHrs);
						cal.add(Calendar.HOUR, intASDefaultPickUpHrs);
						strRevisedPromiseDate = dateFormat.format(cal.getTime());
						log.verbose("Updated Intial Promise Date for ASP is :: " + strRevisedPromiseDate);
					}
					// OMNI-105510 Updating the Initial Promise Date for ASP Line END
					Element elechangeOrderLineExtn = inDocChangeOrder.createElement(AcademyConstants.ELE_EXTN);
					eleOrderLine.appendChild(elechangeOrderLineExtn);
					elechangeOrderLineExtn.setAttribute(AcademyConstants.EXTN_INITIAL_PROMISE_DATE,
							strRevisedPromiseDate); // OMNI-105510 change
					elechangeOrderLineExtn.setAttribute(AcademyConstants.EXTN_DC_DEPARTURE_DATE, "");
					elechangeOrderLineExtn.setAttribute(AcademyConstants.EXTN_STORE_DELIVERY_DATE, "");
					elechangeOrderLineExtn.setAttribute(AcademyConstants.EXTN_ORIGINAL_FULFILLMENT_TYPE,
							AcademyConstants.STR_EMPTY_STRING);
					elechangeOrderLineExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_ASP, AcademyConstants.STR_YES); // OMNI-106139 change
				} else if (isInventoryCheckRequired) {
					String procureFromNode = XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine/@ProcureFromNode");
					boolean bUpdateDates = false;
					Element eleOrderLineSrcCtrls = inDocChangeOrder
							.createElement(AcademyConstants.ELE_ORDER_LINE_SOURCING_CNTRLS);
					Element eleOrderLineSrcCtrl = inDocChangeOrder
							.createElement(AcademyConstants.ELE_ORDER_LINE_SOURCING_CNTRL);
					eleOrderLine.appendChild(eleOrderLineSrcCtrls);
					eleOrderLineSrcCtrls.appendChild(eleOrderLineSrcCtrl);
					if (procureFromDC && (YFCCommon.isVoid(procureFromNode))) {
						eleOrderLine.setAttribute(AcademyConstants.STR_PROCURE_FROM_NODE, strNewShipNode);
						eleOrderLineSrcCtrl.setAttribute(AcademyConstants.INVENTORY_CHECK_CODE,
								AcademyConstants.STR_EMPTY_STRING);
						eleOrderLineSrcCtrl.setAttribute(AcademyConstants.ELE_NODE, strShipNode);
						bUpdateDates = true;
					} else if (procureFromStore && (!YFCCommon.isVoid(procureFromNode))) {
						eleOrderLine.setAttribute(AcademyConstants.STR_PROCURE_FROM_NODE,
								AcademyConstants.STR_EMPTY_STRING);
						eleOrderLineSrcCtrl.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_CREATE);
						eleOrderLineSrcCtrl.setAttribute(AcademyConstants.INVENTORY_CHECK_CODE,
								AcademyConstants.STR_NOINV);
						eleOrderLineSrcCtrl.setAttribute(AcademyConstants.ATTR_SUPPRESS_PROCUREMENT,
								AcademyConstants.STR_NO);
						eleOrderLineSrcCtrl.setAttribute(AcademyConstants.ATTR_SUPPRESS_SOURCING,
								AcademyConstants.STR_NO);
						eleOrderLineSrcCtrl.setAttribute(AcademyConstants.ELE_NODE, strShipNode);
						bUpdateDates = true;
					}
					if (bUpdateDates) {

						String strPromDateSLA = null;
						if (procureFromStore == true) {
							strPromDateSLA = props.getProperty(AcademyConstants.PROMISE_DATE_DC_SLA);
						} else if (procureFromStore == true) {
							strPromDateSLA = props.getProperty(AcademyConstants.PROMISE_DATE_STORE_SLA);
						}

						String strDepartDateSLA = props.getProperty(AcademyConstants.DEPARTURE_DATE_SLA);
						String strArrivalDateSLA = props.getProperty(AcademyConstants.ARRIVAL_DATE_SLA);

						Integer iPromDateSLA = Integer.parseInt(strPromDateSLA);
						Integer iDepartDateSLA = Integer.parseInt(strDepartDateSLA);
						Integer iArrivalDateSLA = Integer.parseInt(strArrivalDateSLA);

						if (iPromDateSLA != 0 && iDepartDateSLA != 0 && iArrivalDateSLA != 0) {
							DateFormat dateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
							Calendar cal = Calendar.getInstance();
							cal.add(Calendar.DATE, iPromDateSLA);
							String promDate = dateFormat.format(cal.getTime());
							log.verbose("Intial Promise date is" + promDate);
							cal.setTime(dateFormat.parse(promDate));
							cal.add(Calendar.DATE, -iDepartDateSLA);
							String departDate = dateFormat.format(cal.getTime());
							log.verbose("DC departure date is" + departDate);
							cal.setTime(dateFormat.parse(promDate));
							cal.add(Calendar.DATE, -iArrivalDateSLA);
							String arrivalDate = dateFormat.format(cal.getTime());
							log.verbose("Store arrival date is" + arrivalDate);
							Element elechangeOrderLineExtn = inDocChangeOrder.createElement(AcademyConstants.ELE_EXTN);
							eleOrderLine.appendChild(elechangeOrderLineExtn);
							elechangeOrderLineExtn.setAttribute(AcademyConstants.EXTN_INITIAL_PROMISE_DATE, promDate);
							elechangeOrderLineExtn.setAttribute(AcademyConstants.EXTN_DC_DEPARTURE_DATE, departDate);
							elechangeOrderLineExtn.setAttribute(AcademyConstants.EXTN_STORE_DELIVERY_DATE, arrivalDate);
						}

					}
				}
			}

			log.verbose("Input Document changeOrder :: " + SCXmlUtil.getString(inDocChangeOrder));
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, inDocChangeOrder);
		} catch (Exception exp) {
			log.verbose("Excetpion occured in AcademyProcessScheduleMsgForSaveTheSale-->invokeChangeOrder() method");
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->invokeChangeOrder() method END");
	}

	private void setTxnObjectFulfillmentType(YFSEnvironment env, String sOrderLineKey, String fulfillmenttype) {
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->setTxnObjectFulfillmentType() method START");
		try {
			Object oFulFillmentType = env.getTxnObject("OrderLineKey_FulfillmentType");

			log.verbose("OdrLineKeyFulfillType map address" + OdrLineKeyFulfillType.hashCode());
			if (!YFCCommon.isVoid(oFulFillmentType)) {
				log.verbose("oFulFillmentType map address" + oFulFillmentType.hashCode());
				env.setTxnObject("OrderLineKey_FulfillmentType", null);
				OdrLineKeyFulfillType.replace(sOrderLineKey, fulfillmenttype);
				env.setTxnObject("YntrxGetAvailibiltyResp", null);
			} else {
				env.setTxnObject("SaveYntrxResponse", true);
				OdrLineKeyFulfillType.put(sOrderLineKey, fulfillmenttype);
			}
			env.setTxnObject("OrderLineKey_FulfillmentType", OdrLineKeyFulfillType);
			Object oFulFillmentType01 = env.getTxnObject("OrderLineKey_FulfillmentType");
			log.verbose("oFulFillmentType map address" + oFulFillmentType01.hashCode());
		} catch (Exception exp) {
			log.verbose(
					"Excetpion occured in AcademyProcessScheduleMsgForSaveTheSale-->setTxnObjectFulfillmentType() method");
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->setTxnObjectFulfillmentType() method END");
	}

	private void setTxnObjectProcureFromNode(YFSEnvironment env, String sOrderLineKey, String procureFromNode) {
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->setTxnObjectProcureFromNode() method START");
		try {
			Object oProcureFromNode = env.getTxnObject("OrderLineKey_ProcureFromNode");

			log.verbose("OdrLineKeyProcureFromNode map address" + OdrLineKeyProcureFromNode.hashCode());

			if (!YFCCommon.isVoid(oProcureFromNode)) {
				log.verbose("oProcureFromNode map address" + oProcureFromNode.hashCode());
				env.setTxnObject("OrderLineKey_ProcureFromNode", null);
				OdrLineKeyProcureFromNode.replace(sOrderLineKey, procureFromNode);
			} else {
				OdrLineKeyProcureFromNode.put(sOrderLineKey, procureFromNode);
			}
			env.setTxnObject("OrderLineKey_ProcureFromNode", OdrLineKeyProcureFromNode);

			OdrLineKeyProcureFromNode.put(sOrderLineKey, procureFromNode);
			env.setTxnObject("OrderLineKey_ProcureFromNode", OdrLineKeyProcureFromNode);

			Object oProcureFromNode01 = env.getTxnObject("OrderLineKey_ProcureFromNode");
			log.verbose("oProcureFromNode map address" + oProcureFromNode01.hashCode());
		} catch (Exception exp) {
			log.verbose(
					"Excetpion occured in AcademyProcessScheduleMsgForSaveTheSale-->setTxnObjectProcureFromNode() method");
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->setTxnObjectProcureFromNode() method END");
	}

	private String getGreaterValue(String strATPThreshold, String strOrderedQty) {
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->getGreaterValue() method START");
		String strHighValue = null;
		try {
			Double dATPThreshold = getDoubleValue(strATPThreshold);
			Double dOrderedQty = getDoubleValue(strOrderedQty);
			Double dHighValue = dATPThreshold >= dOrderedQty ? dATPThreshold : dOrderedQty;
			strHighValue = dHighValue.toString();
		} catch (Exception exp) {
			log.verbose("Excetpion occured in AcademyProcessScheduleMsgForSaveTheSale-->getGreaterValue() method");
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->getGreaterValue() method END");
		return strHighValue;

	}

	private Double getDoubleValue(String strValue) {
		return Double.parseDouble(strValue);
	}

	private Document findInventoryForAS(YFSEnvironment env, Document inDoc, String strZipCode) throws Exception {
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->findInventoryForAS() method START");
		Document inDocFindInv = null;
		String strNewShipNode = null;
		String strASNodeDistance = null;
		Document docReqAttributesForAS = null;
		String strPersonInfoKey = null;
		try {
			inDocFindInv = prepareFindInventoryDoc(inDoc);
			String strShipNode = XMLUtil.getString(inDoc, ".//OrderLines/OrderLine/@ShipNode");
			String strOrderedQty = XMLUtil.getString(inDocFindInv, ".//PromiseLines/PromiseLine/@RequiredQty");
			Element elePromiseLine = (Element) XMLUtil.getNode(inDocFindInv, ".//PromiseLines/PromiseLine");
			Element inEleFindInv = inDocFindInv.getDocumentElement();
			String strExludedShipNodes = (String) props.getProperty(AcademyConstants.SHIP_NODES_TO_EXCLUDE);
			log.verbose("Configured Shipnodes to Exclude are :: " + strExludedShipNodes);
			ArrayList<String> lisExludedShipNodes = new ArrayList<String>(
					Arrays.asList(strExludedShipNodes.split(",")));
			lisExludedShipNodes.add(strShipNode);
			Element eleExcludedShipNodes = inDocFindInv.createElement(AcademyConstants.ELE_EXCLUDED_SHIP_NODES);
			elePromiseLine.appendChild(eleExcludedShipNodes);
			for (String excludedNode : lisExludedShipNodes) {
				if (!YFCObject.isVoid(excludedNode)) {
					Element eleExcludedShipNode = inDocFindInv.createElement(AcademyConstants.ELE_EXCLUDED_SHIP_NODE);
					eleExcludedShipNode.setAttribute(AcademyConstants.ELE_NODE, excludedNode);
					eleExcludedShipNode.setAttribute(AcademyConstants.ATTR_SUPPRESS_NODE_CAPACITY,
							AcademyConstants.STR_YES);
					eleExcludedShipNode.setAttribute(AcademyConstants.ATTR_SUPRESS_PROCUREMENT,
							AcademyConstants.STR_YES);
					eleExcludedShipNode.setAttribute(AcademyConstants.ATTR_SUPRESS_SOURCING, AcademyConstants.STR_YES);
					eleExcludedShipNodes.appendChild(eleExcludedShipNode);
				}
			}

			log.verbose("Inside findInventoryForAS method strAlternateStoreNode value :: " + strAlternateStoreNode);
			if (!YFCCommon.isVoid(strAlternateStoreNode)) {
				addShipNodeDetails(inDocFindInv, elePromiseLine, strAlternateStoreNode);
			}

			Element eleShipToAddr = inDocFindInv.createElement(AcademyConstants.ELE_SHIP_TO_ADDRESS);
			eleShipToAddr.setAttribute(AcademyConstants.ZIP_CODE, strZipCode);
			inEleFindInv.appendChild(eleShipToAddr);
			String strDistanceToConsider = (String) props.getProperty(AcademyConstants.STR_DISTANCE_IN_MILES);
			log.verbose("Configured Distance to Consider in Miles :: " + strDistanceToConsider);
			inEleFindInv.setAttribute(AcademyConstants.STR_DISTANCE_TO_CONSIDER, strDistanceToConsider);
			inEleFindInv.setAttribute(AcademyConstants.ATTR_DISTANCE_UOM_TO_CONSIDER, AcademyConstants.VAL_MILE);

			log.verbose("In Document findInventory ::  " + XMLUtil.getXMLString(inDocFindInv));
			env.setApiTemplate(AcademyConstants.FIND_INVENTORY, AcademyConstants.STR_API_TEMPLATE_FINDINVENTORY_ASP); //OMNI-106943 
			Document outDocFindInv = AcademyUtil.invokeAPI(env, AcademyConstants.FIND_INVENTORY, inDocFindInv);
			log.verbose("Out Document findInventory ::  " + XMLUtil.getXMLString(outDocFindInv));
			env.clearApiTemplate(AcademyConstants.FIND_INVENTORY); //OMNI-106943 

			Element eleAssignment = XMLUtil.getElementByXPath(outDocFindInv,
					".//PromiseLines/PromiseLine/Assignments/Assignment[@Quantity >='" + strOrderedQty + "']");
			// OMNI-106259 Changes Start
			if (!YFCCommon.isVoid(eleAssignment)) {
				strASNodeDistance = eleAssignment.getAttribute(AcademyConstants.ATTR_DISTANCE);
				log.verbose("Distance of AS node from Original BOPIS store is :: " + strASNodeDistance);
				if (getDoubleValue(strASNodeDistance) != 0.0) {
					strNewShipNode = eleAssignment.getAttribute(AcademyConstants.SHIP_NODE);
					Element eleShipNode = (Element) eleAssignment.getElementsByTagName(AcademyConstants.ELE_SHIP_NODE).item(0);
					Element eleShipNodePersonInfo = (Element) eleShipNode.getElementsByTagName("ShipNodePersonInfo").item(0);
					strPersonInfoKey = eleShipNodePersonInfo.getAttribute(AcademyConstants.ATTR_PERSON_INFO_KEY); //OMNI-106943
					docReqAttributesForAS = XMLUtil.createDocument(AcademyConstants.PROMISE);
					docReqAttributesForAS.getDocumentElement().setAttribute(AcademyConstants.SHIP_NODE, strNewShipNode);
					docReqAttributesForAS.getDocumentElement().setAttribute(AcademyConstants.ATTR_DISTANCE, strASNodeDistance);
					docReqAttributesForAS.getDocumentElement().setAttribute(AcademyConstants.ATTR_PERSON_INFO_KEY, strPersonInfoKey); //OMNI-106943
				}else {
					log.verbose(
							"Invalid Data!! Distance of AS node from Original BOPIS store is Zero. Hence not selecting the ShipNode.");
				}
			}
			// OMNI-106259 Changes End
			log.verbose("ShipNode incase of AS :: " + strNewShipNode);

		} catch (Exception exp) {
			log.verbose("Excetpion occured in AcademyProcessScheduleMsgForSaveTheSale-->findInventoryForAS() method");
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->findInventoryForAS() method END");
		return docReqAttributesForAS;

	}

	private String findInventory(YFSEnvironment env, Document inDoc, String procureFromNode) throws Exception {
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->findInventory() method START");
		Document inDocFindInv = null;
		String strNewShipNode = null;
		try {
			inDocFindInv = prepareFindInventoryDoc(inDoc);
			String strShipNode = XMLUtil.getString(inDoc, ".//OrderLines/OrderLine/@ShipNode");
			String strOrderedQty = XMLUtil.getString(inDocFindInv, ".//PromiseLines/PromiseLine/@RequiredQty");
			Element elePromiseLine = (Element) XMLUtil.getNode(inDocFindInv, ".//PromiseLines/PromiseLine");
			if (procureFromDC) {
				addShipNodeDetails(inDocFindInv, elePromiseLine, procureFromNode);
			} else {
				String strExludedShipNodes = (String) props.getProperty(AcademyConstants.SHIP_NODES_TO_EXCLUDE);
				log.verbose("Configured Shipnodes to Exclude are :: " + strExludedShipNodes);
				ArrayList<String> lisExludedShipNodes = new ArrayList<String>(
						Arrays.asList(strExludedShipNodes.split(",")));
				lisExludedShipNodes.add(strShipNode);
				Element eleExcludedShipNodes = inDocFindInv.createElement(AcademyConstants.ELE_EXCLUDED_SHIP_NODES);
				elePromiseLine.appendChild(eleExcludedShipNodes);
				for (String excludedNode : lisExludedShipNodes) {
					if (!YFCObject.isVoid(excludedNode)) {
						Element eleExcludedShipNode = inDocFindInv
								.createElement(AcademyConstants.ELE_EXCLUDED_SHIP_NODE);
						eleExcludedShipNode.setAttribute(AcademyConstants.ELE_NODE, excludedNode);
						eleExcludedShipNode.setAttribute(AcademyConstants.ATTR_SUPPRESS_NODE_CAPACITY,
								AcademyConstants.STR_YES);
						eleExcludedShipNode.setAttribute(AcademyConstants.ATTR_SUPRESS_PROCUREMENT,
								AcademyConstants.STR_YES);
						eleExcludedShipNode.setAttribute(AcademyConstants.ATTR_SUPRESS_SOURCING,
								AcademyConstants.STR_YES);
						eleExcludedShipNodes.appendChild(eleExcludedShipNode);
					}
				}
			}
			strNewShipNode = invokeFindInventory(env, inDocFindInv, strOrderedQty);
			log.verbose("Inside findInventory method strNewShipNode value :: " + strNewShipNode);
		} catch (Exception exp) {
			log.verbose("Excetpion occured in AcademyProcessScheduleMsgForSaveTheSale-->getGreaterValue() method");
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->findInventory() method END");
		return strNewShipNode;
	}

	private void addShipNodeDetails(Document inDocFindInv, Element elePromiseLine, String strShipNode) {
		Element eleShipNodes = inDocFindInv.createElement(AcademyConstants.ELE_SHIP_NODES);
		Element eleShipNode = inDocFindInv.createElement(AcademyConstants.ELE_SHIP_NODE);
		elePromiseLine.appendChild(eleShipNodes);
		eleShipNodes.appendChild(eleShipNode);
		eleShipNode.setAttribute("Node", strShipNode);
	}

	private Document prepareFindInventoryDoc(Document inDoc) throws Exception {
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->prepareFindInventoryDoc() method START");
		Document inDocFindInv = null;
		try {
			Element inEle = inDoc.getDocumentElement();
			Node nOrderLine = XMLUtil.getNode(inDoc, ".//OrderLines/OrderLine");
			Element eleOrderLine = (Element) nOrderLine;
			String sOrderHeaderKey = inEle.getAttribute(AcademyConstants.STR_ORDR_HDR_KEY);
			Element eleItem = SCXmlUtil.getChildElement(eleOrderLine, AcademyConstants.ITEM);
			String strItemId = eleItem.getAttribute(AcademyConstants.ITEM_ID);
			String strRequestedQty = XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine/@OrderedQty");
			inDocFindInv = XMLUtil.createDocument(AcademyConstants.PROMISE);
			Element inEleFindInv = inDocFindInv.getDocumentElement();
			inEleFindInv.setAttribute(AcademyConstants.ATTR_CHECK_CAPACITY, AcademyConstants.STR_NO);
			inEleFindInv.setAttribute(AcademyConstants.ATTR_AGGREGATE_SUPPLY_OF_NON_REQUESTED_TAG,
					AcademyConstants.STR_YES);
			inEleFindInv.setAttribute(AcademyConstants.ATTR_DEMAND_TYPE, AcademyConstants.STR_SCHEDULED);
			inEleFindInv.setAttribute(AcademyConstants.ATTR_ORDER_REF, sOrderHeaderKey);
			inEleFindInv.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.A_ACADEMY_DIRECT);
			inEleFindInv.setAttribute(AcademyConstants.ATTR_IGNORE_PROMISED, AcademyConstants.STR_NO);
			inEleFindInv.setAttribute(AcademyConstants.ATTR_IGNORE_UNPROMISED, AcademyConstants.STR_YES);
			inEleFindInv.setAttribute(AcademyConstants.ITEM_ID, strItemId);
			inEleFindInv.setAttribute(AcademyConstants.ORGANIZATION_CODE, AcademyConstants.A_ACADEMY_DIRECT);
			Element elePromiseLines = inDocFindInv.createElement(AcademyConstants.PROMISE_LINES);
			inEleFindInv.appendChild(elePromiseLines);
			Element elePromiseLine = inDocFindInv.createElement(AcademyConstants.PROMISE_LINE);
			elePromiseLines.appendChild(elePromiseLine);
			elePromiseLine.setAttribute(AcademyConstants.ITEM_ID, strItemId);
			elePromiseLine.setAttribute(AcademyConstants.ATTR_PROD_CLASS, AcademyConstants.PRODUCT_CLASS);
			elePromiseLine.setAttribute("DeliveryMethod", "PICK");
			if (pickFromAlternateStore) {
				inEleFindInv.setAttribute("FulfillmentType", "BOPIS");
				inEleFindInv.setAttribute("DistributionRuleId", "DG_BOPIS");
				elePromiseLine.setAttribute("DistributionRuleId", "DG_BOPIS");
				elePromiseLine.setAttribute("FulfillmentType", "BOPIS");
				String strATPThreshold = (String) props.getProperty(AcademyConstants.ATP_THRESHOLD);
				log.verbose("ATP Threshold value Configured is :: " + strATPThreshold);
				strRequestedQty = getGreaterValue(strATPThreshold, strRequestedQty);
				elePromiseLine.setAttribute(AcademyConstants.REQUIRED_QTY, strRequestedQty);

			} else if (procureFromDC) {
				inEleFindInv.setAttribute("FulfillmentType", "STS");
				elePromiseLine.setAttribute("FulfillmentType", "STS");
				elePromiseLine.setAttribute(AcademyConstants.REQUIRED_QTY, AcademyConstants.STR_ONE);
			} else if (procureFromStore) {
				inEleFindInv.setAttribute("FulfillmentType", "STS");
				inEleFindInv.setAttribute("DistributionRuleId", "DG_STS_STORE");
				elePromiseLine.setAttribute("DistributionRuleId", "DG_STS_STORE");
				elePromiseLine.setAttribute("FulfillmentType", "STS");
				elePromiseLine.setAttribute(AcademyConstants.REQUIRED_QTY, AcademyConstants.STR_ONE);
			}
			elePromiseLine.setAttribute(AcademyConstants.ATTR_UOM, AcademyConstants.UOM_EACH_VAL);

			log.verbose(
					"The Requested Qty in AcademyProcessScheduleMsgForSaveTheSale-->prepareFindInventoryDoc() method is ::"
							+ strRequestedQty);
		} catch (Exception exp) {
			log.verbose(
					"Excetpion occured in AcademyProcessScheduleMsgForSaveTheSale-->prepareFindInventoryDoc() method");
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->prepareFindInventoryDoc() method END");
		return inDocFindInv;
	}

	private String invokeFindInventory(YFSEnvironment env, Document inDocFindInv, String strOrderedQty)
			throws Exception {
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->invokeFindInventory() method START");
		String strNewShipNode = null;
		try {
			log.verbose("In Document findInventory ::  " + XMLUtil.getXMLString(inDocFindInv));
			Document outDocFindInv = AcademyUtil.invokeAPI(env, AcademyConstants.FIND_INVENTORY, inDocFindInv);
			log.verbose("Out Document findInventory ::  " + XMLUtil.getXMLString(outDocFindInv));

			strNewShipNode = XMLUtil.getString(outDocFindInv,
					".//PromiseLines/PromiseLine/Assignments/Assignment[@Quantity >'0']/@ShipNode");
			log.verbose("ShipNode incase of STS1.0 or STS2.0 :: " + strNewShipNode);

		} catch (Exception exp) {
			log.verbose("Excetpion occured in AcademyProcessScheduleMsgForSaveTheSale-->invokeFindInventory() method");
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}

		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->invokeFindInventory() method END");
		return strNewShipNode;
	}
	
	// This method will send the Delay Status Update to WCS if the Line got Converted to ASP line.
	private void sendDelayStatusUpdateToWCSForASLine(YFSEnvironment env, String sOrderLineKey, String sOrderHeaderKey) {
		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->sendDelayStatusUpdateToWCSForASLines() method START");
		log.verbose("OrderHeaderKey :: " + sOrderHeaderKey + ", OrderLineKey :: " + sOrderLineKey);
		String strMinLineStatus = null;
		try {
			Document docIpToService = invokeGetOrderLineListForAS(env, sOrderLineKey, sOrderHeaderKey);
			Element eleIpToService = XMLUtil.getElementByXPath(docIpToService, "//OrderLine");
			if (!YFCCommon.isVoid(eleIpToService)) {
				strMinLineStatus = eleIpToService.getAttribute(AcademyConstants.ATTR_MIN_LINE_STATUS);
				log.verbose("MinLineStatus is :: " + strMinLineStatus);
				if (!YFCCommon.isVoid(strMinLineStatus)
						&& !(AcademyConstants.VAL_CANCELLED_STATUS.equalsIgnoreCase(strMinLineStatus))
						&& isGreaterOrEqualToReleaseStatus(strMinLineStatus)) {
					log.verbose("Sending Delay Status Update To WCS since the line with OrderLineKey: " + sOrderLineKey
							+ " is an AS line and MinLineStatus is " + strMinLineStatus);
					log.verbose("Invoking AcademySendASPLineStatusUpdateToWCS Service with input :: "
							+ XMLUtil.getXMLString(docIpToService));
					AcademyUtil.invokeService(env, AcademyConstants.SERV_ACADEMY_SEND_ASP_LINE_STATUS_UPDATE_TO_WCS,
							docIpToService);
				} else {
					log.verbose("Line with OrderLineKey: " + sOrderLineKey
							+ "is an AS line but Status is either less than Release status or line got Cancelled:: "
							+ strMinLineStatus + ". Hence not sending Delay Status Update to WCS!!");
				}
			} else {
				log.verbose("Line with OrderLineKey: " + sOrderLineKey
						+ " is not an AS Line. Hence not Sending any Delay Status Update to WCS!!");
			}

		} catch (Exception exp) {
			log.verbose(
					"Excetpion occured in AcademyProcessScheduleMsgForSaveTheSale-->sendDelayStatusUpdateToWCSForASLines() method");
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}

		log.verbose("AcademyProcessScheduleMsgForSaveTheSale-->sendDelayStatusUpdateToWCSForASLines() method END");
	}
	
	private boolean isGreaterOrEqualToReleaseStatus(String strMinLineStatus) {
		boolean isgreaterOrEqual;
		Double dMinLineStatus = getDoubleDataType(strMinLineStatus);
		Double dReleaseStatus = getDoubleDataType(AcademyConstants.V_STATUS_3200);
		isgreaterOrEqual = Double.compare(dMinLineStatus, dReleaseStatus) >= 0 ? true : false;
		log.verbose("Is ASP line status Greater than or Equal to Release status :: " + isgreaterOrEqual);
		return isgreaterOrEqual;
	}

	private static Double getDoubleDataType(String strValue) {
		Double dValue = 0.0;
		try {
			dValue = Double.valueOf(strValue);
		} catch (Exception exp) {
			System.out.println("Exception occured while parsing String to Double!!");
			YFSException e = new YFSException(exp.getMessage());
			throw e;
		}
		return dValue;
	}
	
}