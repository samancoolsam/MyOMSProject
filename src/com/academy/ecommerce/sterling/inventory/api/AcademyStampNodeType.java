package com.academy.ecommerce.sterling.inventory.api;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;


/**
 * @author C0013089
 * This class is to Stamp NodeType of each 'Supplies' line in the input.
 */
public class AcademyStampNodeType 
{
	public static YFCLogCategory log = YFCLogCategory.instance(AcademyStampNodeType.class);

	/**This method is use to stamp NodeType for each ShipNode in Supplies tag
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document stampNodeType(YFSEnvironment env, Document inDoc) throws Exception{
		log.verbose("Input to the class :: " +XMLUtil.getXMLString(inDoc));

		Document docOutGetShipNodeList = null;
		Element eleInventorySupplies = null;
		Element eleShipNodeList = null;
		Element eleSupplies = null;
		Element eleShipNode = null;
		NodeList nlShipNode = null;
		NodeList nlSupplies = null;
		String strNodeType = "";
		String strShipNode = "";
		String strNode = "";
		HashMap<String, String> hmNodeTypes = new HashMap<String, String>();

		eleInventorySupplies = inDoc.getDocumentElement();
		nlSupplies = eleInventorySupplies.getElementsByTagName(AcademyConstants.ELE_SUPPLIES);
		docOutGetShipNodeList = callGetShipNodeListApi(env, nlSupplies);
		eleShipNodeList = docOutGetShipNodeList.getDocumentElement();
		nlShipNode = eleShipNodeList.getElementsByTagName(AcademyConstants.SHIP_NODE);
		
		//Looping through each shipNode and store ShipNode and NodeType in HasMap which will use to stamp NodeType for each ShipNode.
		for (int iShipNode=0;iShipNode<nlShipNode.getLength();iShipNode++){
			eleShipNode = (Element) nlShipNode.item(iShipNode);
			strNodeType = eleShipNode.getAttribute(AcademyConstants.ATTR_NODE_TYPE);
			strShipNode = eleShipNode.getAttribute(AcademyConstants.SHIP_NODE);
			hmNodeTypes.put(strShipNode, strNodeType);
		}

		//Stamping NodeType for each ShipNode
		for (int iSupplies=0;iSupplies<nlSupplies.getLength();iSupplies++){
			eleSupplies = (Element) nlSupplies.item(iSupplies);
			strNode = eleSupplies.getAttribute(AcademyConstants.ELE_NODE);
			strNodeType = hmNodeTypes.get(strNode).toString();
			eleSupplies.setAttribute(AcademyConstants.ATTR_NODE_TYPE, strNodeType);
		}

		log.verbose("End of the class :: " +XMLUtil.getXMLString(inDoc));
		return inDoc;
	}
	
	/**
	 * API Call to get the Shipnode list
	 * @throws Exception
	 */
	private Document callGetShipNodeListApi(YFSEnvironment env, NodeList nlSupplies) throws Exception{

		Document docInGetShipNodeList = null;
		Document docTemplateGetShipNodeList = null;
		Document docOutGetShipNodeList = null;
		Element eleShipNode = null;
		Element eleComplexQuery = null;
		Element eleOr = null;
		Element eleSupplies = null;
		Element eleExp = null;
		Element eleShipNodeList = null;
		Element eleShipNodeTemp = null;
		String strNode = "";
		ArrayList<String> aNodesList = new ArrayList<String>();

		docInGetShipNodeList = XMLUtil.createDocument(AcademyConstants.SHIP_NODE);
		eleShipNode = docInGetShipNodeList.getDocumentElement();
		eleComplexQuery = docInGetShipNodeList.createElement(AcademyConstants.COMPLEX_QRY_ELEMENT);
		eleOr = docInGetShipNodeList.createElement(AcademyConstants.COMPLEX_OR_ELEMENT);
		for (int iSupplies=0;iSupplies<nlSupplies.getLength();iSupplies++){
			eleSupplies = (Element) nlSupplies.item(iSupplies);
			strNode = eleSupplies.getAttribute(AcademyConstants.ELE_NODE);

			//Using ArrayList to avoid duplicate Node in the GetShipNodeList API call
			if(!aNodesList.contains(strNode)){
				eleExp = docInGetShipNodeList.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
				eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.SHIP_NODE);
				eleExp.setAttribute(AcademyConstants.ATTR_VALUE, strNode);
				eleOr.appendChild(eleExp);
				aNodesList.add(strNode);
			}
		}
		
		eleComplexQuery.appendChild(eleOr);
		eleShipNode.appendChild(eleComplexQuery);
		log.verbose("Input to GetShipNodeListApi :: " +XMLUtil.getXMLString(docInGetShipNodeList));

		docTemplateGetShipNodeList = XMLUtil.createDocument(AcademyConstants.ELE_SHIP_NODE_LIST);
		eleShipNodeList = docTemplateGetShipNodeList.getDocumentElement();
		eleShipNodeTemp = docTemplateGetShipNodeList.createElement(AcademyConstants.SHIP_NODE);
		eleShipNodeTemp.setAttribute(AcademyConstants.ATTR_NODE_TYPE, AcademyConstants.STR_EMPTY_STRING);
		eleShipNodeTemp.setAttribute(AcademyConstants.SHIP_NODE, AcademyConstants.STR_EMPTY_STRING);
		eleShipNodeList.appendChild(eleShipNodeTemp);
		log.verbose("Template of GetShipNodeListApi :: " +XMLUtil.getXMLString(docTemplateGetShipNodeList));

		env.setApiTemplate(AcademyConstants.API_GET_SHIP_NODE_LIST, docTemplateGetShipNodeList);
		docOutGetShipNodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIP_NODE_LIST, docInGetShipNodeList);
		log.verbose("Output from getShipNodeList : "+XMLUtil.getXMLString(docOutGetShipNodeList));
		env.clearApiTemplate(AcademyConstants.API_GET_SHIP_NODE_LIST);		

		return docOutGetShipNodeList;	
	}
}
