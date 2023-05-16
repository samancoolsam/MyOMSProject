package com.academy.ecommerce.sterling.userexits;

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.ycs.japi.ue.YCScloseManifestUserExit;
import com.yantra.shared.ycp.YFSContext;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;


/**
 * @author nkannapan
 *
 */
public class AcademyYCScloseManifestFixUE implements YCScloseManifestUserExit {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyYCScloseManifestUE.class);
	
	private Properties props;

	/**It sets the properties
	 * @param props
	 */
	public void setProperties(Properties props) {
        this.props = props;
    }

	
	public boolean closeManifestContinue(YFSContext env, String s)throws YFSUserExitException {
		// TODO Auto-generated method stub
		
		boolean isCloseManifestRqd = true;
		try {
			log
					.beginTimer(" Begining of AcademyYCScloseManifestUE -> closeManifestContinue Api");
			log
					.verbose("##Inside closeManifestContinue method in YCSCloseManifestUE##");
			YFCDocument inXML = YFCDocument.parse(s);
			String sCarrier = inXML.getDocumentElement().getAttribute(
					AcademyConstants.STR_CARRIER);

			/*
			String sListCarriers= props.getProperty("Carrier");
			log.verbose("Carrier: "+ sCarrier);
			StringTokenizer st = new StringTokenizer(sListCarriers,",");
			while (st.hasMoreTokens()){
				if(st.nextToken().equals(sCarrier)){
					isCloseManifestRqd = false;
					break;
				}
			} */	
			
			//Start : OMNI-323 : Updated the code for Agile Stabilization for FEDX & Smartpost carrier .
			if (sCarrier.equals(AcademyConstants.STR_USPS_ENDICIA)
					|| sCarrier.equals(AcademyConstants.STR_USPS_LETTER)
					|| sCarrier.contains(AcademyConstants.SCAC_FEDX)
					|| sCarrier.contains(AcademyConstants.STR_SMART_POST)) {
				isCloseManifestRqd = false;
			}
			//End : OMNI-323 : Updated the code for Agile Stabilization for FEDX & Smartpost carrier .
			
			log.verbose("##final isCloseManifestRqd:: ##"+isCloseManifestRqd);
			log.endTimer(" End of AcademyYCScloseManifestUE -> closeManifestContinue Api");
		}catch(Exception e){
			e.printStackTrace();
			throw new YFSUserExitException(e.getMessage());
		}
		return isCloseManifestRqd;
	}
	
	/**
	 * This method is updated as part of Agile stabilization. The existing logic
	 * is updated to fetch NodetType and updated the URL for it..
	 * @param env
	 * @param inXML - getting the selected parameters from inXML 
	 * @return String - inXMLElem.getString() output
	 * @throws Exception
	 */

	public String closeManifest(YFSContext env, String inXML)
			throws YFSUserExitException {
		log.verbose("YCScloseManifestUserExitImpl - closeManifest - inXML"+ inXML);
		YFSEnvironment envs = env.getEnvironment();
		
		Document docInput = YFCDocument.getDocumentFor(inXML).getDocument();
		Element eleInput = docInput.getDocumentElement();
		
		//If manifest is being ovewritten, this Attribute should be set as Y.
		eleInput.setAttribute(AcademyConstants.STR_IGNORE_INTEGRATION_ERRORS, AcademyConstants.ATTR_Y);
		eleInput.setAttribute(AcademyConstants.STR_DO_NOT_EXIT_API, AcademyConstants.ATTR_Y);
		
		
		//Start : OMNI-323 : Code changes for Agile Stabilization
		try {
			eleInput = updateAgileURLBasedOnNodeType(envs, eleInput);
		} catch (Exception e) {
			e.printStackTrace();
			log.verbose("Error in closeManifest:updateAgileURLBasedOnNodeType ::" + XMLUtil.getElementXMLString(eleInput));
		}
		log.verbose("closeManifest element " + XMLUtil.getElementXMLString(eleInput));
		//End : OMNI-323 : code changes for Agile Stabilization
		
		return XMLUtil.getXMLString(docInput);

	}

	/**
	 * This method is added as part of OMNI-323 : Agile Stabilization
	 * This method is used to fetch NodetType and updated the URL for it..
	 * @param env
	 * @param inElem -getting the selected parameters from inElem
	 * @return Element - inElem output
	 * @throws Exception
	 */

	private Element updateAgileURLBasedOnNodeType(YFSEnvironment env, Element eleInput) throws Exception {
		log	.verbose("Begin of AcademyYCScloseManifestUE.updateAgileURLBasedOnNodeType() method");
		log.verbose("Input of  inputXML : " + XMLUtil.getElementXMLString(eleInput));

		String strNodeType = getShipNodeList(env, eleInput);
		log.verbose("close Manifest NodeType  :" + strNodeType);
	
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
		
		log.verbose("End of AcademyYCScloseManifestUE.updateAgileURLBasedOnNodeType() method");
		return eleInput;
	}

	/**
	 * This method is added as part of OMNI-323 : Agile Stabilization
	 * This method invokes getShipmentList API to find out hte NodeType
	 * for a given ShipNode
	 * 
	 * @param env
	 * @param inXMLElem -getting the selected parameters from inXMLElem
	 * @return String - strNodeType output
	 * @throws Exception
	 */

	private String getShipNodeList(YFSEnvironment env, Element eleInput)
			throws Exception {
		log.verbose("Begin of AcademyYCScloseManifestUE.getShipNodeList() method");
		log.verbose("Input of  inputXML : " + eleInput);

		Document docGetShipNodeListOutput = null;
		Document docGetShipNodeListInput = null;
		String strNodeType = null;
		
		String strShipNode = eleInput.getAttribute(AcademyConstants.SHIP_NODE);
		if (!YFCObject.isVoid(strShipNode)) {
			docGetShipNodeListInput = XMLUtil.createDocument(AcademyConstants.SHIP_NODE);
			docGetShipNodeListInput.getDocumentElement().setAttribute(
					AcademyConstants.SHIP_NODE, strShipNode);
			Document outputTemplate = YFCDocument.getDocumentFor(
					"<ShipNodeList><ShipNode ShipNode='' NodeType=''></ShipNode></ShipNodeList>").getDocument();
			
			log.verbose("getShipNodeList API input - " + XMLUtil.getXMLString(docGetShipNodeListInput));
			env.setApiTemplate(AcademyConstants.API_GET_SHIP_NODE_LIST, outputTemplate);
			docGetShipNodeListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIP_NODE_LIST, docGetShipNodeListInput);
			env.clearApiTemplate(AcademyConstants.API_GET_SHIP_NODE_LIST);
			log.verbose("getShipNodeList API output - " + XMLUtil.getXMLString(docGetShipNodeListOutput));
			
			strNodeType = XPathUtil.getString(docGetShipNodeListOutput.getDocumentElement(), AcademyConstants.XPATH_SHIP_NODE_LIST_NODE_TYPE); 
		}
		log.verbose("End of AcademyYCScloseManifestUE.getShipNodeList() method");
		return strNodeType;
	}
	

}
