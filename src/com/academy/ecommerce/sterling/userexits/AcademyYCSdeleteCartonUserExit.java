package com.academy.ecommerce.sterling.userexits;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.shared.ycp.YFSContext;
import com.yantra.ycs.japi.ue.YCSdeleteCartonUserExit;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

public class AcademyYCSdeleteCartonUserExit implements YCSdeleteCartonUserExit {
	private static final YFCLogCategory log = YFCLogCategory
			.instance(AcademyYCSdeleteCartonUserExit.class);

	/**
	 * This UE is implemented as part of OMNI-325 : Agile Stabilization
	 * This method is called by the removeContainerManifest API before deleting
	 * carton on the YCS side. If this method return false then only it enters DeleteCarton()
	 * 
	 * @param env
	 * @param inXML - getting the selected parameters from inXML 
	 * @return boolean -returns false 
	 * @throws Exception
	 */
	
	
	public boolean DeleteCartonContinue(YFSContext env, String inXML) throws YFSUserExitException {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	/**
	 * This UE is implemented as part of OMNI-325 : Agile Stabilization
	 * This method is used to get the NodeType Value and update the Different URl for Store and DC 
	 * 
	 * @param env
	 * @param inXML - getting the selected parameters from inXML 
	 * @return String -inXMLElem.getString() output
	 * @throws Exception
	 */
	
	public String DeleteCarton(YFSContext env, String inXML) throws YFSUserExitException {
		
		log.verbose("YCSdeleteCartonUserExit - DeleteCarton - inXML"+ inXML);
		YFSEnvironment yfsEnv = env.getEnvironment();
		Document docInput = YFCDocument.getDocumentFor(inXML).getDocument();
		Element eleInput = docInput.getDocumentElement();
		
		try {
			eleInput = updateAgileURLBasedOnNodeType(yfsEnv, eleInput);

		} catch (Exception e) {
			e.printStackTrace();
			log.verbose("Error in DeleteCarton : updateAgileURLBasedOnNodeType - inXML"+ inXML);
		}
		log.verbose(" Updated DeleteCarton output " +  XMLUtil.getElementXMLString(eleInput));
		return XMLUtil.getXMLString(docInput);
	}

	/**
	 * This method is added as part of OMNI-325 : Agile Stabilization
	 * This method gets required parameter from the input xml
	 * 
	 * @param env
	 * @param inXMLElem - getting the selected parameters from inXMLElem
	 * @return Element -inXMLElem output
	 * @throws Exception
	 */

	private Element updateAgileURLBasedOnNodeType(YFSEnvironment env,
			Element eleInput) throws Exception {
		log.verbose("Begin of AcademyYCSdeleteCartonUserExit.updateAgileURLBasedOnNodeType() method");
		log.verbose("Input of  inputXML : " + eleInput);

		 String strNodeType = getShipmentContainerList(env, eleInput);
		 
		log.verbose("getShipmentContainerList strShipNode" + strNodeType);
		// Below are variables which holds the Pierbridge URL from customer_overrides.properties file
		String strURL = "";
		if (!YFCObject.isVoid(strNodeType)) {
			strURL = YFSSystem.getProperty(AcademyConstants.STR_PIER_BRIDGE_URL + AcademyConstants.STR_DOT + strNodeType);
			if (YFCObject.isVoid(strURL)) {
				strURL = YFSSystem.getProperty(AcademyConstants.STR_PIER_BRIDGE_URL + AcademyConstants.STR_DOT + AcademyConstants.STR_STORE);
			}
		} else {
			strURL = YFSSystem.getProperty(AcademyConstants.STR_PIER_BRIDGE_URL + AcademyConstants.STR_DOT + AcademyConstants.STR_STORE);
		}
		Element eleConnectionParameters = XMLUtil.getFirstElementByName(eleInput, AcademyConstants.STR_CONNECTION_PARAMETERS);
		Element elePierbridgeParams = XMLUtil.getFirstElementByName(eleConnectionParameters, AcademyConstants.STR_PIERBRIDGE_PARAMS);
		elePierbridgeParams.setAttribute(AcademyConstants.STR_PB_SERVER_URL, strURL);				

		log.verbose("End of AcademyYCSdeleteCartonUserExit.updateAgileURLBasedOnNodeType() method");
		return eleInput;
	}

	/**
	 * This method is added as part of OMNI-325 : Agile Stabilization
	 * This method gets required parameter from the input xml
	 * 
	 * @param env
	 * @param inXMLElem -getting the selected parameters from inXMLElem
	 * @return String -strNodeType output
	 * @throws Exception
	 */

	private String getShipmentContainerList(YFSEnvironment env,
			Element inXMLElem) throws Exception {
		log.verbose("Begin of AcademyYCSdeleteCartonUserExit.getShipmentContainerList() method");
		log.verbose("Input of  inputXML : " + inXMLElem);

		Document docGetShipmentContainerListOutput = null;
		Document docGetShipmentContainerListInput = null;
		String strNodeType=null;

		String strTrackingNumber = inXMLElem
				.getAttribute(AcademyConstants.STR_TRACKING_NUMBER);

		if (!YFCObject.isVoid(strTrackingNumber)) {
			docGetShipmentContainerListInput = XMLUtil.createDocument(AcademyConstants.ELE_CONTAINER);
			docGetShipmentContainerListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRACKING_NO, strTrackingNumber);

			Document outputTemplate = YFCDocument.getDocumentFor(
					"<Containers><Container TrackingNo=''><Shipment ShipNode=''><ShipNode NodeType=''/></Shipment></Container></Containers>")
					.getDocument();

			log.verbose("getShipmentContainerList API input - " + XMLUtil.getXMLString(docGetShipmentContainerListInput));
			env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST, outputTemplate);
			docGetShipmentContainerListOutput = AcademyUtil.invokeAPI(env, 
					AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST, docGetShipmentContainerListInput);
			env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST);

			log.verbose("getShipmentContainerList API output - " + XMLUtil.getXMLString(docGetShipmentContainerListOutput));
			strNodeType = XPathUtil.getString(docGetShipmentContainerListOutput, AcademyConstants.XPATH_CONTAINER_LIST_NODE_TYPE);
		}
		log.verbose("End of AcademyYCSdeleteCartonUserExit.getShipmentContainerList() method");
		
		return strNodeType;
	}

}
