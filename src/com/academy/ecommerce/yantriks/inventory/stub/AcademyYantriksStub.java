package com.academy.ecommerce.yantriks.inventory.stub;

/**#########################################################################################
 *
 * Project Name                : POD
 * Author                      : Fulfillment POD
 * Author Group				  : Fulfillment
 * Date                        : 15-MAR-2023 
 * Description				  : This class provides a Stub response for all YAntriks API 
 * 								calls from OMS side.
 * 								 
 * ---------------------------------------------------------------------------------
 * Date            	Author         		Version#       		Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 15-Mar-2023		Everest  	 		1.0           		Initial version
 *
 * #########################################################################################*/

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;

import com.academy.util.constants.AcademyConstants;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;

public class AcademyYantriksStub {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyYantriksStub.class);
	private static HashMap<String, HashMap<String, ArrayList<String>>> hmItemLoad = new HashMap<String, HashMap<String, ArrayList<String>>>();


	/**This is main method which will be invoked for the stub code for Yantriks responses.
	 * 
	 * @param apiUrl
	 * @param strRequestJson
	 * @return String
	 */
	public static String invokeYantriksStub(String apiUrl,String strRequestJson) {
		log.beginTimer("AcademyYantriksStub: invokeYantriksStub() method");
		String strStubResponse = "";

		try {
			JSONObject requestJson = new JSONObject(strRequestJson);
			if(apiUrl.contains(AcademyConstants.CREATE_RESERV_API_URL_SUBSTRING_SEARCH)) {
				strStubResponse = invokeYantriksStubForCreateReservation(requestJson);
			}else if(apiUrl.contains("availability/aggregator")){
				strStubResponse = invokeYantriksStubForGetAvailability(requestJson);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception during Yantriks Stub creation");
		}

		log.endTimer("AcademyYantriksStub: invokeYantriksStub() method");
		return strStubResponse;

	}

	/**This is stub code for getAvailability Yantiks API call
	 * 
	 * @param requestJson
	 * @return String
	 */
	private static String invokeYantriksStubForGetAvailability(JSONObject requestJson) throws JSONException, InterruptedException, Exception {
		log.beginTimer("AcademyYantriksStub: invokeYantriksStubForGetAvailability() method");
		String strStubResponse = "";

		JSONObject responseJson = new JSONObject();
		//Read the data from CSV file and prepare the stub response
		responseJson = readFromCSVAndPrepareResponse(requestJson, responseJson);
		
		if(!responseJson.isEmpty()) {
			strStubResponse = responseJson.toString();
		}

		//Get sleep time from property file and convert to long
		String strTimeout = YFSSystem.getProperty(AcademyConstants.CREATE_AVAILABILITY_STUB_SLEEP_TIME);
		Long lSleepTime = (long) 1000; 
		
		if(!YFCObject.isVoid(strTimeout)) {
			lSleepTime = Long.parseLong(strTimeout);
		}
		
		log.verbose("Sleep time for Get Availability stub: "+ lSleepTime);
		Thread.sleep(lSleepTime);		
		log.endTimer("AcademyYantriksStub: invokeYantriksStubForGetAvailability() method");
		return strStubResponse;
	}

	/**This method reads the data from the CSV file and prepares the webservice response accordingly
	 * 
	 * @param requestJson
	 * @return String
	 */
	private static JSONObject readFromCSVAndPrepareResponse(JSONObject requestJson, JSONObject responseJson) throws Exception{
		log.beginTimer("AcademyYantriksStub: readFromCSVAndPrepareResponse() method");
		try {
			//Check if the file is already read and set in the static response
			if(hmItemLoad.size()==0) {
				//read the data from CSV file
				readFromCSV();		
			}

			//Populate the Root level attributes
			responseJson = populateRootLevelAttributesForGetAvailability(responseJson, requestJson);

			JSONArray availabilityByProductsArray = new JSONArray();
			//Prepare the avaialbility response based on the data in the static map
			availabilityByProductsArray = getAvailabilityByProductsArray(requestJson);

			responseJson.put("availabilityByProducts", availabilityByProductsArray);

			log.verbose("The response json is:: " +responseJson); 
		}catch (Exception e) {
			e.printStackTrace();
		}

		log.endTimer("AcademyYantriksStub: readFromCSVAndPrepareResponse() method");
		return responseJson;
	}

	/**This method reads the data from the CSV file and updates the data as part of 
	 * a Static variable 
	 * 
	 */
	private static void readFromCSV() throws Exception{
		log.beginTimer("AcademyYantriksStub: readFromCSV() method");
		BufferedReader brInputFile = null;
		try {
			log.verbose("Item HashMap is not loaded. proceed with data load");
			log.verbose(" Time when the csv load started "+LocalDateTime.now() );
			String strEachLine = "";        
			String strInputFile = YFSSystem.getProperty(AcademyConstants.GET_AVAILABILITY_CSV_PATH);
			//String strInputFile = "D:\\Xmls\\TestData.csv";
			LocalDateTime dtStartDate = LocalDateTime.now();
			log.info("Time when the file is read "+dtStartDate);		

			brInputFile = new BufferedReader(new FileReader(strInputFile));

			while ((strEachLine = brInputFile.readLine()) != null) {
				HashMap<String, ArrayList<String>> hmShipNodeInventory;
				ArrayList<String> lShipNodeInventory;

				//Assuming each line is of format ItemId, FulfillmentType, ShipNode, Inventory Supply Amount, NodeType
				String[] strEachArray = strEachLine.split(",");
				log.verbose(" The String is "+strEachLine);
				String strItemId = strEachArray[0];
				String strFulfillmentType = strEachArray[1];

				if(hmItemLoad.containsKey(strItemId)) {
					hmShipNodeInventory = hmItemLoad.get(strItemId);
					if(hmShipNodeInventory.containsKey(strFulfillmentType)) {
						lShipNodeInventory = hmShipNodeInventory.get(strFulfillmentType);
					}
					else {
						lShipNodeInventory = new ArrayList<String>();
					}
				} else {
					hmShipNodeInventory = new HashMap<String, ArrayList<String>>();
					lShipNodeInventory = new ArrayList<String>();
				}
				
				String strShipNode = strEachArray[2];
				if(strShipNode.length() == 1) {
					strShipNode = "00" + strShipNode;
				}
				else if(strShipNode.length() == 2) {
					strShipNode = "0" + strShipNode;
				}
				log.verbose("Final ShipNode is :: " + strShipNode);
				
				lShipNodeInventory.add(strShipNode + "_" + strEachArray[4] + "_" +strEachArray[3] );
				hmShipNodeInventory.put(strFulfillmentType, lShipNodeInventory);
				hmItemLoad.put(strItemId, hmShipNodeInventory);   
			}
			
			LocalDateTime dtEndDate = LocalDateTime.now();
			Duration duration = Duration.between(dtStartDate, dtEndDate);
			long milliSec = (Math.abs(duration.getNano())/1000000);

			log.info("Total number of Distinct items read " +hmItemLoad.size());  
			log.info("Time when the script to read CSV in milli seconds : "+ milliSec);		

		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (brInputFile != null) {
				try {
					brInputFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		log.endTimer("AcademyYantriksStub: readFromCSV() method");
	}



	private static JSONArray getAvailabilityByProductsArray(JSONObject requestJson) throws JSONException {
		log.beginTimer("AcademyYantriksStub: getAvailabilityByProductsArray() method");
		JSONArray availabilityByProductsArray = new JSONArray();

		JSONArray jArrInputProducts = requestJson.getJSONArray("products");
		log.verbose("The Input Products Array length:: " +jArrInputProducts.length());

		for(int i=0; i< jArrInputProducts.length(); i++){
			JSONObject currentProductAvail = jArrInputProducts.getJSONObject(i);
			String strItemId = currentProductAvail.getString(AcademyConstants.JSON_ATTR_PRODUCT_ID);			
			log.verbose("ProductId :: " +strItemId);

			if(hmItemLoad.containsKey(strItemId)) {
				JSONObject availabilityByProductsObject = new JSONObject();
				JSONArray availabilityByFulfillmentTypesArray = new JSONArray();

				HashMap<String, ArrayList<String>> hmInventory = hmItemLoad.get(strItemId);

				log.verbose("Inventory setup for itemId : " + strItemId + " with inventory as :: "+hmInventory.toString());

				Set<String> keySet = hmInventory.keySet();

				log.verbose("keySet Size:" + keySet.size());

				Iterator<String> iterShipNodeInventory = keySet.iterator();
				log.verbose("Preparing Json Response from CSV");

				//Adding the Availability by product
				availabilityByProductsObject.put(AcademyConstants.JSON_ATTR_PRODUCT_ID, strItemId);
				availabilityByProductsObject.put(AcademyConstants.JSON_ATTR_UOM, AcademyConstants.UNIT_OF_MEASURE);
				availabilityByProductsObject.put(AcademyConstants.JSON_ATTR_GTIN, "");
				availabilityByProductsObject.put(AcademyConstants.JSON_ATTR_LAUNCH_DATE_TIME, "");
				availabilityByProductsObject.put(AcademyConstants.JSON_ATTR_LAUNCH_DATE, "");
				availabilityByProductsObject.put(AcademyConstants.JSON_ATTR_ASSOCIATION_TYPE, AcademyConstants.JSON_ATTR_REGULAR);
				availabilityByProductsArray.add(availabilityByProductsObject);     

				//Adding availability by Fulfillment Type
				while (iterShipNodeInventory.hasNext()) {

					String strFulfillmentType = iterShipNodeInventory.next();
					JSONObject availabilityByFulfillmentTypesObject = new JSONObject();		
					JSONArray availabilityDetailsArray = new JSONArray();
					JSONObject availabilityDetailsObject = new JSONObject();

					log.verbose("The fulfillment"  +strFulfillmentType);
					ArrayList<String> alNodes = hmInventory.get(strFulfillmentType);
					log.verbose("The alnodes::" +alNodes);

					availabilityByFulfillmentTypesObject.put(AcademyConstants.JSON_ATTR_FULFILLMENT_TYPE, strFulfillmentType);

					availabilityByFulfillmentTypesArray.add(availabilityByFulfillmentTypesObject);

					availabilityByProductsObject.put(AcademyConstants.JSON_ATTR_AVAIL_BY_FF_TYPE, availabilityByFulfillmentTypesArray);

					availabilityDetailsObject.put(AcademyConstants.JSON_ATTR_SEGMENT, AcademyConstants.CONST_DEFAULT);
					availabilityDetailsObject.put(AcademyConstants.JSON_ATTR_ATP, 10000);
					availabilityDetailsObject.put(AcademyConstants.JSON_ATTR_SUPPLY, 9999);
					availabilityDetailsObject.put(AcademyConstants.JSON_ATTR_DEMAND, 1);
					availabilityDetailsObject.put(AcademyConstants.JSON_ATTR_SAFETY_STOCK, 0);
					availabilityDetailsObject.put(AcademyConstants.JSON_ATTR_ATP_STATUS, "");

					availabilityDetailsArray.add(availabilityDetailsObject);
					availabilityByFulfillmentTypesObject.put(AcademyConstants.JSON_ATTR_AVAIL_BY_DETAILS, availabilityDetailsArray);      

					JSONArray availabilityByLocationsArray = new JSONArray();

					//Preparing Availability by location
					for (int iAl=0; iAl < alNodes.size(); iAl ++) {
						String strShipNode = alNodes.get(iAl).split("_")[0];
						String strLocationType = alNodes.get(iAl).split("_")[1];
						String strInventory = alNodes.get(iAl).split("_")[2];

						JSONObject availabilityByLocationsObject = new JSONObject();

						availabilityByLocationsObject.put(AcademyConstants.YIH_LOCATION_ID, strShipNode);
						availabilityByLocationsObject.put(AcademyConstants.JSON_ATTR_LOCATION_TYPE, strLocationType);
						availabilityByLocationsObject.put(AcademyConstants.JSON_ATTR_ATP, Integer.parseInt(strInventory));
						availabilityByLocationsObject.put(AcademyConstants.JSON_ATTR_SUPPLY, Integer.parseInt(strInventory));
						availabilityByLocationsObject.put(AcademyConstants.JSON_ATTR_DEMAND, 1);
						availabilityByLocationsObject.put(AcademyConstants.JSON_ATTR_SAFETY_STOCK, 0);
						availabilityByLocationsObject.put(AcademyConstants.JSON_ATTR_ATP_STATUS, "");

						availabilityByLocationsArray.add(availabilityByLocationsObject);
						availabilityDetailsObject.put(AcademyConstants.JSON_ATTR_AVAIL_BY_LOCATIONS, availabilityByLocationsArray);
						JSONArray futureQtyByDates = new JSONArray();
						availabilityByLocationsObject.put(AcademyConstants.JSON_ATTR_FUTURE_QTY_DATES, futureQtyByDates);
					}
				}
			}
		}	

		log.endTimer("AcademyYantriksStub: getAvailabilityByProductsArray() method");
		return availabilityByProductsArray;		
	}

	private static JSONObject populateRootLevelAttributesForGetAvailability(JSONObject responseJson, JSONObject requestJson) throws JSONException {
		log.beginTimer("AcademyYantriksStub: populateRootLevelAttributesForGetAvailability() method");	

		responseJson.put(AcademyConstants.JSON_ATTR_ORG_ID, requestJson.get(AcademyConstants.JSON_ATTR_ORG_ID));
		responseJson.put(AcademyConstants.JSON_ATTR_SELLING_CHANNEL, requestJson.get(AcademyConstants.JSON_ATTR_SELLING_CHANNEL));
		responseJson.put(AcademyConstants.JSON_INP_TRANSACTION_TYPE, requestJson.get(AcademyConstants.JSON_INP_TRANSACTION_TYPE));

		log.verbose("populateRootLevelAttributesForGetAvailability output: " + responseJson);
		log.endTimer("AcademyYantriksStub: populateRootLevelAttributesForGetAvailability() method");
		return responseJson;

	}

	/**
	 * 
	 * @param requestJson
	 * @return
	 * @throws JSONException
	 * @throws InterruptedException
	 */
	private static String invokeYantriksStubForCreateReservation(JSONObject requestJson) throws JSONException, InterruptedException  {

		log.beginTimer("AcademyYantriksStub: invokeYantriksStubForCreateReservation() method");
		String strStubResponse = "";

		JSONObject responseJson = new JSONObject();
		populateRootLevelAttributesForCreateReservation(responseJson, requestJson);

		JSONArray lineReservationDetailsArrayIn = requestJson.getJSONArray(AcademyConstants.JSON_ATTR_LINE_RES_DETAILS);
		JSONArray lineReservationDetailsArray = populateLineReservationDetailsObject(lineReservationDetailsArrayIn);
		responseJson.put(AcademyConstants.JSON_ATTR_LINE_RES_DETAILS, lineReservationDetailsArray);		
		if(!responseJson.isEmpty()) {
			strStubResponse = responseJson.toString();
		}
		
		log.verbose(" strStubREsponse :: " + strStubResponse);
		
		String strTimeout = YFSSystem.getProperty(AcademyConstants.CREATE_RESERV_STUB_SLEEP_TIME);
		Long lSleepTime = (long) 1000; 
		
		if(!YFCObject.isVoid(strTimeout)) {
			lSleepTime = Long.parseLong(strTimeout);
		}
		
		//Get sleep time from property file and convert to long
		log.verbose("Sleep time for create reservation stub: "+ lSleepTime);
		Thread.sleep(lSleepTime);
		
		log.endTimer("AcademyYantriksStub: invokeYantriksStubForCreateReservation() method");
		return strStubResponse;
	}

	/**
	 * 
	 * @param responseJson
	 * @param requestJson
	 * @throws JSONException
	 */
	private static void populateRootLevelAttributesForCreateReservation(JSONObject responseJson, JSONObject requestJson) throws JSONException {
		log.beginTimer("AcademyYantriksStub: populateRootLevelAttributesForCreateReservation() method");	

		responseJson.put(AcademyConstants.JSON_ATTR_UPDATE_TIME, requestJson.get(AcademyConstants.JSON_ATTR_UPDATE_TIME));
		responseJson.put(AcademyConstants.JSON_ATTR_UPDATE_USER, requestJson.get(AcademyConstants.JSON_ATTR_UPDATE_USER));
		responseJson.put(AcademyConstants.JSON_ATTR_ORG_ID, requestJson.get(AcademyConstants.JSON_ATTR_ORG_ID));
		responseJson.put(AcademyConstants.JSON_ATTR_EXP_TIME, requestJson.get(AcademyConstants.JSON_ATTR_EXP_TIME));
		responseJson.put(AcademyConstants.JSON_ATTR_EXP_TIME_UNIT, requestJson.get(AcademyConstants.JSON_ATTR_EXP_TIME_UNIT));
		responseJson.put(AcademyConstants.JSON_ATTR_ORDER_ID, requestJson.get(AcademyConstants.JSON_ATTR_ORDER_ID));
		responseJson.put(AcademyConstants.JSON_ATTR_SELLING_CHANNEL, AcademyConstants.SELLING_CHANNEL_GLOBAL);
		responseJson.put(AcademyConstants.JSON_ATTR_ADDITIONAL_INFO, "");
		responseJson.put(AcademyConstants.JSON_ATTR_ORDER_CREATED_TIME, AcademyConstants.JSON_ATTR_UPDATE_TIME);

		log.verbose("populateRootLevelAttributesForCreateReservation output: " + responseJson);
		log.endTimer("AcademyYantriksStub: populateRootLevelAttributesForCreateReservation() method");

	}

	/**
	 * 
	 * @param lineReservationDetailsArrayIn
	 * @return
	 * @throws JSONException
	 */
	private static JSONArray populateLineReservationDetailsObject(JSONArray lineReservationDetailsArrayIn) throws JSONException {
		log.beginTimer("AcademyYantriksStub: populateLineReservationDetailsObject() method");

		JSONArray lineReservationDetailsArray = new JSONArray();
		JSONObject lineReservationDetailsObj = new JSONObject();
		JSONArray locationReservationDetailsArray = new JSONArray();
		JSONObject locationReservationDetailsObj = new JSONObject();
		JSONArray demandsArray = new JSONArray();
		JSONObject demandsObj = new JSONObject();

		for (int i = 0; i < lineReservationDetailsArrayIn.length(); i++) {
			JSONObject reservationObjIn = lineReservationDetailsArrayIn.getJSONObject(i);
			lineReservationDetailsObj.put(AcademyConstants.YIH_LINE_ID, reservationObjIn.getString(AcademyConstants.YIH_LINE_ID));
			lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_FULFILLMENT_SERVICE, "");
			lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_FULFILLMENT_TYPE, reservationObjIn.getString(AcademyConstants.JSON_ATTR_FULFILLMENT_TYPE));
			lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_ORDERLINEREF, "");
			lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_PRODUCT_ID, reservationObjIn.getString(AcademyConstants.JSON_ATTR_PRODUCT_ID));
			lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_UOM, reservationObjIn.getString(AcademyConstants.JSON_ATTR_UOM));
			lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_ASSOCIATION_TYPE, "");
			lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_REDECIDE_LOCATIONS, "");
			lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_ADDITIONAL_INFO, "");

			JSONArray locationReservationDetailsArrayIn = reservationObjIn.getJSONArray(AcademyConstants.JSON_ATTR_LOCATION_RES_DETAILS);
			for (int j=0; j<locationReservationDetailsArrayIn.length(); j++) {
				JSONObject locationObjIn = locationReservationDetailsArrayIn.getJSONObject(j);
				locationReservationDetailsObj.put(AcademyConstants.YIH_LOCATION_ID, locationObjIn.getString(AcademyConstants.YIH_LOCATION_ID));
				locationReservationDetailsObj.put(AcademyConstants.JSON_ATTR_LOCATION_TYPE, locationObjIn.getString(AcademyConstants.JSON_ATTR_LOCATION_TYPE));

				JSONArray demandsArrayIn = locationObjIn.getJSONArray(AcademyConstants.JSON_ATTR_DEMANDS);
				for (int k=0; k<demandsArrayIn.length(); k++) {
					JSONObject demandObjIn = demandsArrayIn.getJSONObject(k);
					demandsObj.put(AcademyConstants.JSON_ATTR_DEMAND_TYPE, demandObjIn.getString(AcademyConstants.JSON_ATTR_DEMAND_TYPE));
					Date date = new Date();
					SimpleDateFormat formatter = new SimpleDateFormat(AcademyConstants.DATE_FORMAT_YYYY_MM_DD);
					String strSysDate = formatter.format(date);
					demandsObj.put(AcademyConstants.JSON_ATTR_RESERVATION_DATE, strSysDate);
					demandsObj.put(AcademyConstants.JSON_ATTR_SEGMENT, demandObjIn.getString(AcademyConstants.JSON_ATTR_SEGMENT));
					demandsObj.put(AcademyConstants.JSON_ATTR_SEGMENT, demandObjIn.getString(AcademyConstants.JSON_ATTR_SEGMENT));
					demandsObj.put(AcademyConstants.JSON_ATTR_SEGMENT, "");
				}
			}
		}

		demandsArray.add(demandsObj);		
		locationReservationDetailsObj.put(AcademyConstants.JSON_ATTR_DEMANDS, demandsArray);		
		locationReservationDetailsArray.add(locationReservationDetailsObj);		
		lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_LOCATION_RES_DETAILS, locationReservationDetailsArray);
		lineReservationDetailsArray.add(lineReservationDetailsObj);				

		log.endTimer("AcademyYantriksStub: populateLineReservationDetailsObject() method");
		return lineReservationDetailsArray;

	}
}
