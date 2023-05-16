package com.academy.ecommerce.sterling.condition;

import java.rmi.RemoteException;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/*
 * The AcademyIsSharedInventoryDC class fetched the 'ShipNode' from input xml. Invokes 'getShipNodeList' api. 
 * Get 'NodeType' from the api output xml. Checks if the  'NodeType' attribute has value of 'SharedInventoryDC'. 
 * If yes, return 'true' else return 'false' 
 *  
 */
public class AcademyIsSharedInventoryDC implements YCPDynamicConditionEx {

	private static final YFCLogCategory log = YFCLogCategory
			.instance(AcademyIsSharedInventoryDC.class);
	
	private Map argMap;

	//Stores property:ShipNodePath configured in configurator
	public void setProperties(Map map) {
		argMap = map;
	}	
	
	public boolean evaluateCondition(YFSEnvironment env, String sName,
			Map mapData, Document inputXML) {
		log.beginTimer("Begining of AcademyIsSharedInventoryDC -> evaluateCondition");

		// Declare document variable
		Document getShipNodeListInputDoc = null;
		Document getShipNodeListOutputDoc = null;
		Document templateDoc = null;
		// Declare element variables
		Element eleRootElement = null;
		Element eleOutShipNode = null;	

		try {
			//	Fetching the Xpath
			String path =(String)argMap.get(AcademyConstants.SHIP_NODE_XPATH);
			
			//	Fetch the attribute value of ShipNode from the Xpath configured in the service arugument 
			String strShipNode= XMLUtil.getAttributeFromXPath(inputXML,(String) path);
			
			// Create root element ShipNode
			getShipNodeListInputDoc = XMLUtil.createDocument(AcademyConstants.SHIP_NODE);
			eleRootElement = getShipNodeListInputDoc.getDocumentElement();

			if (strShipNode != null && !(strShipNode.trim().equals(""))) {
				// setting the value for attribute ShipNode
				eleRootElement.setAttribute(AcademyConstants.SHIP_NODE,
						strShipNode);
				log.verbose("getShipNodeList API inXML:"
						+ XMLUtil.getXMLString(getShipNodeListInputDoc));
				// Set the template for getShipNodeList API
				templateDoc = YFCDocument
						.getDocumentFor(
								"<ShipNodeList><ShipNode ShipNode=\"\" NodeType=\"\" /></ShipNodeList>")
						.getDocument();
				env.setApiTemplate(AcademyConstants.API_GET_SHIP_NODE_LIST,
						templateDoc);

				// invoking the API getShipNodeList
				getShipNodeListOutputDoc = AcademyUtil.invokeAPI(env,
						AcademyConstants.API_GET_SHIP_NODE_LIST,
						getShipNodeListInputDoc);
				env.clearApiTemplate(AcademyConstants.API_GET_SHIP_NODE_LIST);

				log.verbose("getShipNodeList API outXML:"
						+ XMLUtil.getXMLString(getShipNodeListOutputDoc));

				// Fetch the attribute ShipNode from the output xml
				eleOutShipNode = (Element) getShipNodeListOutputDoc
						.getElementsByTagName(AcademyConstants.SHIP_NODE).item(
								0);
				// Check if ShipNode value if void
				if (eleOutShipNode != null && !YFCObject.isVoid(eleOutShipNode)) {

					// Fetch NodeType Value
					String strNodeType = eleOutShipNode
							.getAttribute("NodeType");

					if ("SharedInventoryDC".equals(strNodeType)) {
						log.verbose("The NodeType is SharedInventoryDC and return true");
						return true;
					}
				}
			}

		} catch (YFSException e) {
			log.verbose(" YFSException in Method"+e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			log.verbose(" RemoteException in Method"+e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			log.verbose(" Exception in Method"+e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.endTimer("End of AcademyIsSharedInventoryDC-> evaluateCondition");
		return false;
	}
}
