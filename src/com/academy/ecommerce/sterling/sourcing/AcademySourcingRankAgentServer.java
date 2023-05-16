package com.academy.ecommerce.sterling.sourcing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSConnectionHolder;
import com.yantra.yfs.japi.YFSEnvironment;


/**
 * 
 * OMNI- 4252 SourcingRankAgent Server is updated after removing NodeCapacity. 
 * Commented the getResourcePoolCapacity and findNeglectingNodes methods.
 * Node Capacity Flag is used to find Neglect Nodes(if it is N they are neglected) and 
 * Node capacity is used to prepare  the rank for the ShipNode.(if Capacity > 0 then rank is 
 * calculated.) Updated the code (commented the if condition capacity > 0). 
 * Node Capacity Flag is defaulted to Y always.
 * 
 */

public class AcademySourcingRankAgentServer extends YCPBaseAgent {
	
	private String strShipmentKeyFilterDays = null;
	
	private String strPendingPickFilterDays = null;
	
	private String strPickRateFilterDays = null;
		
	private String strRejectionRateFilterDays = null;
	
	private String strShipmentKeyFilterDate = null;
	
	private String strPickRateFilterDate = null;
	
	private String strPendingPickFilterDate = null;
	
	private String strRejectionRateFilterDate = null;
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySourcingRankAgentServer.class);
	//private Document rankTableInDoc = XMLUtil.createDocument("AcadShipNodeRank");
	@Override
	public List<Document> getJobs(YFSEnvironment env, Document inXML) throws Exception {

		log.verbose("Inside AcademySourcingRankAgentServer getJobs.The Input xml is : " + XMLUtil.getXMLString(inXML));

		NodeList nlgetOrgList = null;
		Element getOrgEle = null;
		HashMap<String,Integer> storeRankMap         = new HashMap<String,Integer>();
		HashMap<String,Integer> storeNodeCapacityMap = new HashMap<String,Integer>();
		HashMap<String,String> storeElgFlagMap       = new HashMap<String,String>();
		List<Document> outputList                    = new ArrayList<Document>();
		
		Element eleLastMessage = (Element) inXML.getElementsByTagName("LastMessage").item(0);
		if (!YFCObject.isNull(eleLastMessage)) {
			eleLastMessage.hasAttribute("LastMessagesAdded");
			outputList.clear();
		} else {
            

//			int iPendingPicks    = 0;
//			int iPickRate 		 = 0;
//			int iRejectionRate   = 0;
//			int iNodeCapacity    = 0;
//			int iRank            = 0;
			String storeFlag        = null;
			String nodeCapacityFlag = null;
			String strcurDate = getCurrentDate();

			double iConstA = Double.parseDouble(inXML.getDocumentElement().getAttribute("NodeCapacityConst"));
			double iConstB = Double.parseDouble(inXML.getDocumentElement().getAttribute("PendingPicksConst"));
			double iConstC = Double.parseDouble(inXML.getDocumentElement().getAttribute("PickRateConst"));
			double iConstD = Double.parseDouble(inXML.getDocumentElement().getAttribute("RejectionRateConst"));
			String strPickRateStatus      = inXML.getDocumentElement().getAttribute("PickRateStatus");
			String strPendingPickStatus   = inXML.getDocumentElement().getAttribute("PendingPickStatus");
			String strRejectionRateStatus = inXML.getDocumentElement().getAttribute("RejectionRateStatus");
			String strRejectionChannel = inXML.getDocumentElement().getAttribute("RejectionChannel");
			
			double rankThreshold            = Double.parseDouble(inXML.getDocumentElement().getAttribute("RankThreshold"));
			String strStoreNonElgFlagList   = inXML.getDocumentElement().getAttribute("StoreNonElgFlag");
			strShipmentKeyFilterDays =inXML.getDocumentElement().getAttribute("ShipmentKeyFilterDays");	
			strShipmentKeyFilterDate = calculateTotalDays(strShipmentKeyFilterDays);
			strPendingPickFilterDays =inXML.getDocumentElement().getAttribute("PendingPickFilterDays");	
			strPendingPickFilterDate = calculateTotalDays(strPendingPickFilterDays);
			strPickRateFilterDays =inXML.getDocumentElement().getAttribute("PickRateFilterDays");	
			strPickRateFilterDate = calculateTotalDays(strPickRateFilterDays);
			strRejectionRateFilterDays =inXML.getDocumentElement().getAttribute("RejectionRateFilterDays");
			strRejectionRateFilterDate = calculateTotalDays(strRejectionRateFilterDays);
			
			String strStoreElgFlag          = null;
			
			ArrayList<String> alStoreNonElg     = new ArrayList<String>();
			//ArrayList<String> alNeglectingNodes = new ArrayList<String>();  OMNI-4252 
			ArrayList<String> alPickRateStatus  = new ArrayList<String>();

			
			Document getShipNodeRankListInDoc   = XMLUtil.createDocument("AcadShipNodeRank");
			Document getShipNodeRankListOutDoc  = AcademyUtil.invokeService(env, "AcademygetShipNodeRankList",getShipNodeRankListInDoc);
			// OMNI-4252 Removing Node capacacity
			//NodeList nlShipNodeList             = getShipNodeRankListOutDoc.getElementsByTagName("AcadShipNodeRank");
			//alNeglectingNodes                   = findNeglectingNodes(nlShipNodeList);
			// End  OMNI-4252 
			HashMap<String,Integer>  pendingPicks     = getRates(env, getQueryForPendingPicks(strPendingPickStatus));
			HashMap<String,Integer>  pickRate         = getRates(env, getQueryForPickRate(strPickRateStatus, strRejectionRateStatus));
			HashMap<String,Integer>  rejectionRate    = getRates(env, getQueryForRejectionRate(strRejectionRateStatus,strRejectionChannel)); 

			String strShipNode             =   null;
			String strCapacityDate         =   null;
			Document resourcePoolOutDoc    =   null;
			String  pickRateStatusForQuery =   null;
				
			
			if(!StringUtil.isEmpty(strStoreNonElgFlagList)){
			   
				String strStroeNonElgArry[] = strStoreNonElgFlagList.split(",");
				for (int i = 0; i < strStroeNonElgArry.length; i++) {
					alStoreNonElg.add(strStroeNonElgArry[i]);
				}
			}
			
			

			Document getOrgListOutDoc = getNodeOrganizations(env);
			if (!YFCObject.isNull(getOrgListOutDoc)) {
				nlgetOrgList = getOrgListOutDoc.getElementsByTagName("Organization");

			}
			
			
				for (int i = 0; i < nlgetOrgList.getLength(); i++) {
					
					int iPendingPicks    = 0;
					int iPickRate 		 = 0;
					int iRejectionRate   = 0;
					int iNodeCapacity    = 0;
					int iRank            = 0;

					getOrgEle         = (Element) nlgetOrgList.item(i);
					String strOrgCode = getOrgEle.getAttribute("OrganizationCode");
					log.verbose("****Node***" + strOrgCode);
					
					//if (!alNeglectingNodes.contains(strOrgCode)) { OMNI-4252 
                      //  log.verbose("Node: "+ strOrgCode + "not a neglectedNode");  OMNI-4252 
						if (pendingPicks.containsKey(strOrgCode)) {
							iPendingPicks = pendingPicks.get(strOrgCode);
							log.verbose("Pending Picks From DB" + iPendingPicks);
						} 

						if (pickRate.containsKey(strOrgCode)) {
							iPickRate = pickRate.get(strOrgCode);
							log.verbose("Pick Rate From DB" + iPickRate);
						} 

						if (rejectionRate.containsKey(strOrgCode)) {
							iRejectionRate = rejectionRate.get(strOrgCode);
							log.verbose("Pick Rate From DB" + iPickRate);
						}

						if (alStoreNonElg.contains(strOrgCode)) {
							strStoreElgFlag = "N";
						} else {
							strStoreElgFlag = "Y";
						}

						//strShipNode          =   inXML.getDocumentElement().getAttribute("OrganizationCode");
						//Start OMNI-4252 Removing Node Capacity
//						strCapacityDate      =   getCurrentDate();
//						resourcePoolOutDoc   =   getResourcePool(env, strCapacityDate,strOrgCode/* strShipNode*/);
//
//						Element resourcePoolOutEle = (Element) resourcePoolOutDoc.getElementsByTagName("Date").item(0);
//						if (resourcePoolOutEle != null && resourcePoolOutEle.hasAttributes()) {
//							iNodeCapacity = (int) Double.parseDouble(resourcePoolOutEle.getAttribute("Availability"));
//							log.verbose("Node Capacity for"+strOrgCode + "is" +iNodeCapacity);
//							//storeNodeCapacityMap.put(strOrgCode /*strShipNode*/, iNodeCapacity);
//						}
					//	if (iNodeCapacity > 0) {
						//End OMNI-4252
							iRank = (int) ((iConstA * iNodeCapacity) + (iConstB * iPendingPicks) + (iConstC * iPickRate) + (iConstD * iRejectionRate));
							log.verbose("iRank for the Node:" + strOrgCode /*strShipNode*/ + "is" + iRank);
							//storeRankMap.put(strOrgCode /*strShipNode*/, iRank);
					//	}  OMNI-4252
						log.verbose("iRank for the Node:" + strOrgCode /*strShipNode*/ + "is" + iRank);
						storeRankMap.put(strOrgCode /*strShipNode*/, iRank);
						//storeNodeCapacityMap.put(strOrgCode /*strShipNode*/, iNodeCapacity); OMNI-4252 
						storeElgFlagMap.put(strOrgCode /*strShipNode*/, strStoreElgFlag);
					//} OMNI-4252 

				}
                
                log.verbose("Size of the storeRankMap is" + storeRankMap.size());
               // log.verbose("Size of the storeNodeCapacityMap is" + storeNodeCapacityMap.size()); OMNI-4252 
                log.verbose("Size of the storeElgFlagMap is" + storeElgFlagMap.size());
				HashMap<String,Integer> sortedMap = sortHashMapByValue(storeRankMap);
				double requiredSize     = sortedMap.size()*rankThreshold/100;
				log.verbose("Required size is" +requiredSize );
				int counter          = 0;
				//int nodeCapacity     = 0;
				int storerank        = 0;
				String strStoreElgFlagV = null;
				
				for(String key : sortedMap.keySet()){
					log.verbose("Counter is "+ counter);
					log.verbose("Preparing the ShipNodeRankInput docment  for node:" +key);
					storerank =  sortedMap.get(key);
					log.verbose("Store rank:" +storerank);
					storeFlag =  (counter > requiredSize) ?"N" : "Y" ;
					log.verbose("storeFlag :" + storeFlag);
					//nodeCapacity = storeNodeCapacityMap.get(key);
					nodeCapacityFlag = "Y" ;
							/*(nodeCapacity >0) ? "Y" : "N"; OMNI-4252 Removing Node Capacity */
					log.verbose("nodeCapacityFlag :" + nodeCapacityFlag);
					strStoreElgFlagV = storeElgFlagMap.get(key);
					log.verbose("strStoreElgFlag :" + strStoreElgFlagV);
					Document acadShipNodeRankInput =  getRankTableInDoc(env, storerank, nodeCapacityFlag, storeFlag, key, strStoreElgFlagV);
					counter ++;
					outputList.add(acadShipNodeRankInput);
				}

		}

		return outputList;
	}

	@Override
	public void executeJob(YFSEnvironment env, Document inXML) throws Exception {

		log.verbose("Inside AcademySourcingRankAgentServer executeJob.The Input xml is : " + XMLUtil.getXMLString(inXML));

		ArrayList<String> alShipNode          = new ArrayList<String>();
		Document   getShipNodeRankListInDoc   = XMLUtil.createDocument("AcadShipNodeRank");
		Document   getShipNodeRankListOutDoc  = AcademyUtil.invokeService(env, "AcademygetShipNodeRankList",getShipNodeRankListInDoc);
		

		if (!YFCObject.isNull(getShipNodeRankListOutDoc)) {
			
			NodeList nlShipNodeList = getShipNodeRankListOutDoc.getElementsByTagName("AcadShipNodeRank");
			for (int i = 0; i < nlShipNodeList.getLength(); i++) {
				
				Element shipNodeEle = (Element) nlShipNodeList.item(i);
				alShipNode.add(shipNodeEle.getAttribute("ShipNode"));

			}
		}
		
		String strOrgCode  = inXML.getDocumentElement().getAttribute("ShipNode");
		String serviceName =  alShipNode.contains(strOrgCode) ? "AcademyManageShipNodeRank" : "AcademyCreateShipNodeRank";
		log.verbose("Service called for the Node " + strOrgCode + "is " + serviceName);
	    AcademyUtil.invokeService(env, serviceName, inXML);
		
		
	}


	private  HashMap<String, Integer> sortHashMapByValue(HashMap<String, Integer> hm){ 
	  
	        List<Map.Entry<String, Integer> > list =  new LinkedList<Map.Entry<String, Integer> >(hm.entrySet()); 
	        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() { 
	            public int compare(Map.Entry<String, Integer> o1,  
	                               Map.Entry<String, Integer> o2) 
	            { 
	                return (o1.getValue()).compareTo(o2.getValue()); 
	            } 
	        }); 
	   
	        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>(); 
	        for (Map.Entry<String, Integer> aa : list) { 
	            temp.put(aa.getKey(), aa.getValue()); 
	        } 
	        return temp; 
	    } 
	

	 
  private String getCurrentDate(){
		Date curDate = new Date();
		SimpleDateFormat newDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strcurDate = newDateFormat.format(curDate);
		return strcurDate;

	} 
	
	
	
  private HashMap<String,Integer> getRates(YFSEnvironment env,String query) throws SQLException{
	  
	  
		log.verbose("Executing the query " +query );
		Connection oracleConnection = null;
		PreparedStatement ps 		= null;
		ResultSet resultSet 		= null;
		HashMap<String, Integer> ratesFromDb    = new HashMap<String, Integer>();
		
		try {
			YFSConnectionHolder connHolder = (YFSConnectionHolder) env;
			oracleConnection = connHolder.getDBConnection();
			ps 				 = oracleConnection.prepareStatement(query);
			resultSet 		 = ps.executeQuery();
			
			while (resultSet.next()) {
				ratesFromDb.put(resultSet.getString(1).trim(), resultSet
						.getInt(2));
			}
		} 
		finally {
			if(ps != null){
				ps.close();
				ps = null;
			}
				
			if(resultSet != null){
				resultSet.close();
				resultSet = null;
			}
				
//			if(oracleConnection != null){
//				oracleConnection.close();
//			}
				
		}		
		  return ratesFromDb;	
	}

	private String calculateTotalDays(String strParam)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Calendar cal                = Calendar.getInstance();
		cal.add(Calendar.DATE, -Integer.parseInt(strParam));
		String strTotalDays        = dateFormat.format(cal.getTime());
		return strTotalDays;
	}
//	private String getQueryForPendingPicks(String strShipmentKeyFilterDays){
//		
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
//		Calendar cal                = Calendar.getInstance();
//		cal.add(Calendar.DATE, -Integer.parseInt(strShipmentKeyFilterDays));
//	  	String strFilterDate        = dateFormat.format(cal.getTime());
//	  	String query                = "SELECT SHIPNODE_KEY, count(SHIPNODE_KEY) AS COUNT FROM YFS_SHIPMENT  WHERE STATUS ='1100.70.06.10' AND SHIPMENT_KEY > '" +strFilterDate+"' GROUP BY (SHIPNODE_KEY)";
//		return query;
//	}
//	
	private String getQueryForPickRate(String strPickRateStatus,String strRejectionStatus){

		
		 StringBuilder pickRateStatusForQuerySb =new StringBuilder();
		String strPickRateStatusArray[] = strPickRateStatus.split(",");
		for (int i = 0; i < strPickRateStatusArray.length; i++) {
			pickRateStatusForQuerySb.append("'").append(strPickRateStatusArray[i]).append("',");
		}
		String strPickRateStatusForQuery = pickRateStatusForQuerySb.toString();
		
		StringBuilder rejectionkRateStatusForQuerySb =new StringBuilder();
		 String strRejectionPickRateStatusArray[] = strRejectionStatus.split(",");
			for (int i = 0; i < strRejectionPickRateStatusArray.length; i++) {
				rejectionkRateStatusForQuerySb.append("'").append(strRejectionPickRateStatusArray[i]).append("',");
			}
			String strRejectionRateStatusForQuery = rejectionkRateStatusForQuerySb.toString();
			
			/*String query = "SELECT s.SHIPNODE_KEY ,count (1) countofshipments_readytoship FROM strladm.YFS_shipment s, strladm.YFS_SHIPMENT_STATUS_AUDIT a, strladm.YFS_SHIP_NODE n  "+
			"  WHERE s.SHIPMENT_KEY > VARCHAR_FORMAT (CURRENT TIMESTAMP - 10 DAYS,'YYYYMMDDHH24MISS') "+
		            "AND a.SHIPMENT_STATUS_AUDIT_KEY > VARCHAR_FORMAT (CURRENT TIMESTAMP, 'YYYYMMDD') AND s.SHIPMENT_KEY = a.SHIPMENT_KEY "+
		            "AND a.NEW_STATUS in ("+strPickRateStatusForQuery.substring(0, strPickRateStatusForQuery.length()-1)+") AND a.NEW_STATUS not in ("
		             +strRejectionRateStatusForQuery.substring(0, strRejectionRateStatusForQuery.length()-1)+") AND s.SHIPNODE_KEY = n.SHIPNODE_KEY AND n.NODE_TYPE = 'Store' "+
		     "GROUP BY substr (a.SHIPMENT_STATUS_AUDIT_KEY, 1, 8), s.SHIPNODE_KEY ORDER BY s.SHIPNODE_KEY WITH UR";*/
		
			String query = "SELECT s.SHIPNODE_KEY ,count (1) countofshipments_readytoship FROM strladm.YFS_shipment s, strladm.YFS_SHIPMENT_STATUS_AUDIT a, strladm.YFS_SHIP_NODE n  "+
			"  WHERE s.SHIPMENT_KEY >  '"+strShipmentKeyFilterDate+"'  "+
		            "AND a.SHIPMENT_STATUS_AUDIT_KEY >  '"+strPickRateFilterDate+"'  AND s.SHIPMENT_KEY = a.SHIPMENT_KEY " +
		            "AND a.NEW_STATUS in ("+strPickRateStatusForQuery.substring(0, strPickRateStatusForQuery.length()-1)+")  AND s.SHIPNODE_KEY = n.SHIPNODE_KEY AND n.NODE_TYPE = 'Store' "+
		     "GROUP BY s.SHIPNODE_KEY ORDER BY s.SHIPNODE_KEY WITH UR";
	    return query;

	

	}
	
	private String getQueryForRejectionRate(String strRejectionStatus, String strRejectionChannel){

		 StringBuilder rejectionkRateStatusForQuerySb =new StringBuilder();
		 String strRejectionPickRateStatusArray[] = strRejectionStatus.split(",");
			for (int i = 0; i < strRejectionPickRateStatusArray.length; i++) {
				rejectionkRateStatusForQuerySb.append("'").append(strRejectionPickRateStatusArray[i]).append("',");
			}
			String strRejectionRateStatusForQuery = rejectionkRateStatusForQuerySb.toString();
			
			StringBuilder rejectionChannelForQuerySb =new StringBuilder();
			 String strRejectionChannelArray[] = strRejectionChannel.split(",");
				for (int i = 0; i < strRejectionChannelArray.length; i++) {
					rejectionChannelForQuerySb.append("'").append(strRejectionChannelArray[i]).append("',");
				}
				String strRejectionChannelForQuery = rejectionChannelForQuerySb.toString();
			
			/*String query = "SELECT s.SHIPNODE_KEY,count (1) countofshipments_readytoship FROM YFS_shipment s, YFS_SHIPMENT_STATUS_AUDIT a, YFS_SHIP_NODE n "+
		      "WHERE s.SHIPMENT_KEY > VARCHAR_FORMAT (CURRENT TIMESTAMP - 10 DAYS,'YYYYMMDDHH24MISS') AND s.EXTN_SHORTPICK_REASON_CODE <> ''"+
		      "AND a.SHIPMENT_STATUS_AUDIT_KEY > VARCHAR_FORMAT (CURRENT TIMESTAMP, 'YYYYMMDD') AND s.SHIPMENT_KEY = a.SHIPMENT_KEY " +
		      "AND a.NEW_STATUS in ("+ strRejectionRateStatusForQuery.substring(0, strRejectionRateStatusForQuery.length()-1)+") AND s.SHIPNODE_KEY = n.SHIPNODE_KEY AND n.NODE_TYPE = 'Store'"+
		     " GROUP BY substr (a.SHIPMENT_STATUS_AUDIT_KEY, 1, 8), s.SHIPNODE_KEY ORDER BY s.SHIPNODE_KEY WITH UR";*/
			
				String query = "SELECT s.SHIPNODE_KEY,count (1) countofshipments_cancelled FROM YFS_shipment s, YFS_SHIPMENT_STATUS_AUDIT a, YFS_SHIP_NODE n "+
			      "WHERE s.SHIPMENT_KEY >  '"+strShipmentKeyFilterDate+"' " +
			      "AND a.SHIPMENT_STATUS_AUDIT_KEY >  '"+strRejectionRateFilterDate+"' AND s.SHIPMENT_KEY = a.SHIPMENT_KEY " +
			      "AND a.NEW_STATUS in ("+ strRejectionRateStatusForQuery.substring(0, strRejectionRateStatusForQuery.length()-1)+") AND s.SHIPNODE_KEY = n.SHIPNODE_KEY  "+
			      "AND s.STATUS in ("+ strRejectionRateStatusForQuery.substring(0, strRejectionRateStatusForQuery.length()-1)+") "+
			      "AND n.NODE_TYPE = 'Store'"+
			      " AND a.MODIFYPROGID in ("+ strRejectionChannelForQuery.substring(0, strRejectionChannelForQuery.length()-1)+")"+
			     " GROUP BY s.SHIPNODE_KEY ORDER BY s.SHIPNODE_KEY WITH UR";
		    return query;

	}
	
	
	
	private  String getQueryForPendingPicks(String strPendingPickStatus){
		StringBuilder pendingkRateStatusForQuerySb =new StringBuilder();
		 String strRendingPickRateStatusArray[] = strPendingPickStatus.split(",");
			for (int i = 0; i < strRendingPickRateStatusArray.length; i++) {
				pendingkRateStatusForQuerySb.append("'").append(strRendingPickRateStatusArray[i]).append("',");
			}
			String strPendingPickRateStatusForQuery = pendingkRateStatusForQuerySb.toString();
			/*String query = "SELECT s.SHIPNODE_KEY,count (1) countofshipments_readytoship FROM YFS_shipment s, YFS_SHIPMENT_STATUS_AUDIT a, YFS_SHIP_NODE n"+
		     " WHERE s.SHIPMENT_KEY > VARCHAR_FORMAT (CURRENT TIMESTAMP - 10 DAYS,'YYYYMMDDHH24MISS') "+
		     "AND a.SHIPMENT_STATUS_AUDIT_KEY > VARCHAR_FORMAT (CURRENT TIMESTAMP, 'YYYYMMDD') AND s.SHIPMENT_KEY = a.SHIPMENT_KEY "+
		     "AND a.NEW_STATUS in ("+strPendingPickRateStatusForQuery.substring(0,strPendingPickRateStatusForQuery.length()-1)+")  AND s.SHIPNODE_KEY = n.SHIPNODE_KEY AND n.NODE_TYPE = 'Store' "+
		     "GROUP BY substr (a.SHIPMENT_STATUS_AUDIT_KEY, 1, 8), s.SHIPNODE_KEY ORDER BY s.SHIPNODE_KEY WITH UR";*/
			
			/*String query = "SELECT s.SHIPNODE_KEY,count (1) countofshipments_pendingpicks FROM YFS_shipment s, YFS_SHIPMENT_STATUS_AUDIT a, YFS_SHIP_NODE n"+
		     " WHERE s.SHIPMENT_KEY > '"+strShipmentKeyFilterDate+"' "+
		     "AND a.SHIPMENT_STATUS_AUDIT_KEY > '"+strPendingPickFilterDate+"' AND s.SHIPMENT_KEY = a.SHIPMENT_KEY " +
		     "AND a.NEW_STATUS in ("+strPendingPickRateStatusForQuery.substring(0,strPendingPickRateStatusForQuery.length()-1)+")  AND s.SHIPNODE_KEY = n.SHIPNODE_KEY AND n.NODE_TYPE = 'Store' "+
		     "GROUP BY s.SHIPNODE_KEY ORDER BY s.SHIPNODE_KEY WITH UR";*/
			
			String query = "SELECT s.SHIPNODE_KEY,count (1) countofshipments_pendingpicks FROM YFS_shipment s, YFS_SHIP_NODE n"+
		     " WHERE s.SHIPMENT_KEY > '"+strPendingPickFilterDate+"' "+		     
		     "AND s.STATUS in ("+strPendingPickRateStatusForQuery.substring(0,strPendingPickRateStatusForQuery.length()-1)+")  AND s.SHIPNODE_KEY = n.SHIPNODE_KEY AND n.NODE_TYPE = 'Store' "+
		     "GROUP BY s.SHIPNODE_KEY ORDER BY s.SHIPNODE_KEY WITH UR";						
		    return query;
		
}
	
	
	private Document getNodeOrganizations(YFSEnvironment env) throws Exception{
		
		Document getOrgListInDoc     = XMLUtil.createDocument("Organization");
		getOrgListInDoc.getDocumentElement().setAttribute("IsNode", "Y");
		Element eleNode          = getOrgListInDoc.createElement("Node");
		eleNode.setAttribute("NodeType", "Store");
		getOrgListInDoc.getDocumentElement().appendChild(eleNode);
		Document getOrgListOutDoc     = AcademyUtil.invokeService(env, "AcademygetOrganizationList", getOrgListInDoc);
		return getOrgListOutDoc;
		
	}
	
	
	/* OMNI-4252 Removing Node Capacity
	 * private Document getResourcePool(YFSEnvironment env,String strCapacityDate,
	 * String strShipNode) throws Exception{
	 * 
	 * Document resourcePoolInDoc = XMLUtil.createDocument("ResourcePool"); Element
	 * resourcePoolInEle = resourcePoolInDoc.getDocumentElement();
	 * resourcePoolInEle.setAttribute("ProviderOrganizationCode", "DEFAULT");
	 * resourcePoolInEle.setAttribute("StartDate", strCapacityDate);
	 * resourcePoolInEle.setAttribute("Node", strShipNode);
	 * resourcePoolInEle.setAttribute("ItemGroupCode", "PROD");
	 * resourcePoolInEle.setAttribute("CapacityOrganizationCode", "DEFAULT");
	 * resourcePoolInEle.setAttribute("EndDate", strCapacityDate); Document
	 * resourcePoolOutDoc = AcademyUtil.invokeAPI(env, "getResourcePoolCapacity",
	 * resourcePoolInDoc); return resourcePoolOutDoc;
	 * 
	 * }
	 */
	 
	
   private Document  getRankTableInDoc(YFSEnvironment env,int iRank,String availNodeCapacityFlag,String storeFlag,String strShipNode,String strStoreElgFlag ) throws ParserConfigurationException{
	   
	    Document rankTableInDoc = XMLUtil.createDocument("AcadShipNodeRank");
	    rankTableInDoc.getDocumentElement().setAttribute("AvailNodeCapacity",availNodeCapacityFlag);
		rankTableInDoc.getDocumentElement().setAttribute("ShipNode", strShipNode);
		rankTableInDoc.getDocumentElement().setAttribute("Rank", Integer.toString(iRank));
		rankTableInDoc.getDocumentElement().setAttribute("StoreFlag", storeFlag);
		rankTableInDoc.getDocumentElement().setAttribute("StoreElgFlag", strStoreElgFlag);
		log.verbose("AcadShipNodeRank xml for the  node" + strShipNode +     XMLUtil.getXMLString(rankTableInDoc));
		return rankTableInDoc;
		
	   
   }
   
	/* OMNI-4252 Removing Node Capacity
	 * private ArrayList<String> findNeglectingNodes(NodeList nlShipNodeList){
	 * 
	 * ArrayList<String> alNeglectingNodes = new ArrayList<String>();
	 * 
	 * for (int i = 0; i < nlShipNodeList.getLength(); i++) { Element shipNodeEle =
	 * (Element) nlShipNodeList.item(i); if (!YFCObject.isNull(shipNodeEle)) {
	 * //alShipNode.add(shipNodeEle.getAttribute("ShipNode")); if
	 * (("N").equalsIgnoreCase(shipNodeEle.getAttribute("AvailNodeCapacity")) &&
	 * (getCurrentDate()
	 * .equalsIgnoreCase(shipNodeEle.getAttribute("Modifyts").substring(0, 10)))) {
	 * alNeglectingNodes.add(shipNodeEle.getAttribute("ShipNode")); } } }
	 * 
	 * return alNeglectingNodes;
	 * 
	 * }
	 */
}
