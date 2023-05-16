package com.academy.ecommerce.sterling.bopis.sfspacking;

import java.util.List;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfc.util.YFCException;

/**
 * This class is responsible to update container weight entered by store user in UI.
 * @author Sanchit and Neeti
 * <Shipment
   DisplayLocalizedFieldInLocale="xml:CurrentUser:/User/@Localecode"
   SCAC="" ShipmentKey="xml:scControllerInput:/Shipment/@ShipmentKey">
   <Containers>
   <Container ActualWeight="" ActualWeightUOM=""
   IsPackProcessComplete="" ShipmentContainerKey=""/>
   <Container ActualWeight="" ActualWeightUOM=""
   IsPackProcessComplete="" ShipmentContainerKey=""/>   

</Containers>
</Shipment>
 *
 *BOPIS-122 : SFS Packing in webStore
 */
public class AcademyUpdateContainerWeight {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyUpdateContainerWeight.class);

	//Define properties to fetch argument value from service configuration
	private Properties props;
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}
	
	
	/**
	 * This method is responsible to update container weight entered by store user in UI.
	 * @param env
	 * @param inXML
	 * @return
	 * @throws Exception
	 */
	public  Document updateContainerWeight(YFSEnvironment env, Document inXML) throws Exception {
		
		log.beginTimer("AcademyUpdateContainerWeight::updateContainerWeight");
		log.verbose("Entering the method AcademyUpdateContainerWeight.updateContainerWeight");
		//System.out.println("Entering the method AcademyUpdateContainerWeight.updateContainerWeight");
		Boolean bCallChangeShipment=false;
		String strActualWeight = null;
		String  strScac=null;
		String strMaxContainerWeight=props.getProperty(AcademyConstants.STR_MAX_CONTAINER_WEIGHT);
		double dMaxContainerWeight = Double.parseDouble(strMaxContainerWeight);
		String strIgnoreScacForContWeight = props.getProperty(AcademyConstants.STR_IGNORE_SCAC_FOR_CONT_WEIGHT);
		
		Element eleRoot=inXML.getDocumentElement();
		strScac= eleRoot.getAttribute(AcademyConstants.ATTR_SCAC);
		
		NodeList NLContainer = inXML.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
		//BOPIS-1576. Since now multiple containers can come in single request. Looping through each container for validation instead //statement.
		for (int i = 0; i < NLContainer.getLength(); i++)
		{  
			Element EleContainer = (Element) NLContainer.item(i);
			strActualWeight=EleContainer.getAttribute(AcademyConstants.ATTR_ACTUAL_WEIGHT);
			
			if(EleContainer.hasAttribute(AcademyConstants.ATTR_IS_PACK_PROCESS_COMPLETED))
			{   //System.out.println("Removing attribute IsPackProcessComplete");
				log.verbose("Removing attribute IsPackProcessComplete");
				EleContainer.removeAttribute(AcademyConstants.ATTR_IS_PACK_PROCESS_COMPLETED);
			}
			if(!strIgnoreScacForContWeight.contains(strScac) && (Double.parseDouble(strActualWeight) > dMaxContainerWeight)){
				YFCException ex = new YFCException(AcademyConstants.STR_ERROR_CODE_12);
				// ex.setErrorCode("AcademyConstants.STR_ERROR_CODE_12");
				ex.setErrorDescription("Container weight("+strActualWeight+") is more than "+ dMaxContainerWeight +" lbs.");
				throw ex;
			}
									
			else{
				EleContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_GROSS_WEIGHT, strActualWeight);
				EleContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_NET_WEIGHT, strActualWeight);
				bCallChangeShipment=true;
								
			}
			
		}
		
		log.verbose("bCallChangeShipment"+bCallChangeShipment);
		//System.out.println("bCallChangeShipment"+bCallChangeShipment);
		if(bCallChangeShipment)
		{ 
			log.verbose("Calling service AcademyUpdateWeightChangeShipment with input"+XMLUtil.getXMLString(inXML));
			//System.out.println("Calling service AcademyUpdateWeightChangeShipment with input"+XMLUtil.getXMLString(inXML));
			AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_CHANGE_SHIPMENT_FOR_UPDATE_WEIGHT, inXML);
		}
		log.endTimer("AcademyUpdateContainerWeight::updateContainerWeight");
		log.verbose("Check the input doc for changeShipment: =" +XMLUtil.getXMLString(inXML));
		//System.out.println("Check the input doc for changeShipment: =" +XMLUtil.getXMLString(inXML));
		
		return inXML;
		
	}

}
