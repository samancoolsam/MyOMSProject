package com.academy.ecommerce.sterling.sts;

/**#########################################################################################
*
* Project Name                : STS 2.0
* Module                      : OMNI-32358
* Author                      : Radhakrishna Mediboina(C0015568)
* Author Group				  : CTS - POD
* Date                        : 05-Aprl-2021 
* Description				  : Stamp SO shipment number on TO container level for the BOPIS 
								Save the sale lines on SO shipment creation. 
* 								 
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       		Remarks/Description                      
* ---------------------------------------------------------------------------------
* 05-Aprl-2021		CTS  	 			  1.0           	Initial version
*
* #########################################################################################*/

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyUpdateShipmentNoOnTOContainerForSTS {
	
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyUpdateShipmentNoOnTOContainerForSTS.class);
	
	/**
	 * This method update the SO SHP No on TO Container level.
	 * 
	 * Pseudo Logic:
	 * 
	 * On SO Shipment creation for STS lines 
	
		If Shipment type is STS 
			Then call the getCompleteOrderDetails API for that SO Shipment.			
			Fetch the SO OrderLineKey from each shipment line of Event XML (ConsolidateToShipment on_success Event)
			(For loop)				
				Fetch the chainedFromOrderLineKey from each container of TO shipment line level and compare.
				(For Loop)					
					If SO OrderLineKey and ChainedFromOrderLineKey are same 
						Then call the changeShipment API to update SO_shp_no on respective container level (EXTN_SO_SHIPMENT_NO in yfs_shipment_caontainer table).				
				End the loop.				
			End the loop.			
		End	
		
	 * @param env,
	 * @param docInput
	 **/
	public Document updateSOSipmentNoOnTOContainerLevel(YFSEnvironment env, Document docInput) throws Exception {		
		log.beginTimer("AcademyUpdateShipmentNoOnTOContainerForSTS.updateSOSipmentNoInTOContainerLevel() - Start");
		log.verbose("\n docInput XML :: \n"+XMLUtil.getXMLString(docInput));
		
		Document outDocgetCompleteOrderDetails = null;
		String strOrderHeaderKey = null;
		String strOrderLineKey = null;
		String strChainedFromOrderLineKey = null;
		String strSOShipmentNo = null;
				
		strOrderHeaderKey = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
		strSOShipmentNo = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);		
		//Invoking the getCompleteOrderDetails API for TO Container Details
		outDocgetCompleteOrderDetails = getCompleteOrderDetails(env, strOrderHeaderKey);		
		
		//Loop each ShipmentLine element
		Element eleShipment = docInput.getDocumentElement();
		NodeList nlShipmentLine = eleShipment.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);				
		for (int i = 0; i < nlShipmentLine.getLength(); i++) {			
			Element eleShipmentLine = (Element) nlShipmentLine.item(i);
			strOrderLineKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			log.verbose("\n Sales OrderLineKey :: " + strOrderLineKey);
			//Loop each Container element of getCompleteOrderDetails API outDoc
			NodeList nlContainer = outDocgetCompleteOrderDetails.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_CONTAINER);			
			for (int j = 0; j < nlContainer.getLength(); j++) {
				Element eleContainer = (Element) nlContainer.item(j);
				//OMNI-80134,80135 - Starts - Fetching the ChainedFromOrderLineKey from ContainerDetails
				NodeList nlContainerDetail = eleContainer.getElementsByTagName(AcademyConstants.CONTAINER_DETL_ELEMENT);		
				for (int icontainerDetail = 0; icontainerDetail < nlContainerDetail.getLength(); icontainerDetail++) {
					Element eleContainerDetail = (Element) nlContainerDetail.item(icontainerDetail);
					Element eleShipLine = SCXmlUtil.getChildElement(eleContainerDetail, AcademyConstants.ELE_SHIPMENT_LINE);
				//OMNI-80134,80135 - Ends
					strChainedFromOrderLineKey = eleShipLine.getAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_LINE_KEY);
					log.verbose("\n ChainedfromOrderlinekey :: " + strChainedFromOrderLineKey);
					//Comparing the SO OrderLineKey with TO Shipmentline's ChainedFromOrderLineKey
					if(strOrderLineKey.equals(strChainedFromOrderLineKey)) {
						updateSOShipmentNoAtContainerLevel(env,eleContainer, strSOShipmentNo);
					}
				}				
				
			}
		}
		log.beginTimer("AcademyUpdateShipmentNoOnTOContainerForSTS.updateSOSipmentNoInTOContainerLevel() - End");
		return docInput;	
	}


	/** This method executes getCOmpleteOrderDetails API to fetch the respective TO Containers.
	 * 
	 * Sample Input XML:
	 * 	<Order DocumentType="0001" EnterpriseCode="Academy_Direct" OrderHeaderKey="20210329072216208213242"/>
	 * 
	 * @param env
	 * @param inDoc
	 * @return outDoc
	 * @throws Exception 
	 **/	
	private Document getCompleteOrderDetails(YFSEnvironment env, String strOrderHeaderKey) throws Exception {
		log.beginTimer("AcademyUpdateShipmentNoOnTOContainerForSTS.getCompleteOrderDetails() method - Start");		
		Document docIngetCOmpleteOrderDetails = null;
		Document docOutgetCOmpleteOrderDetails = null;
				
		//getCompleteOrderDetails API inDoc
		docIngetCOmpleteOrderDetails = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		docIngetCOmpleteOrderDetails.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
		docIngetCOmpleteOrderDetails.getDocumentElement().setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		docIngetCOmpleteOrderDetails.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
		log.verbose("getCompleteOrderDetails API indoc :: \n" + XMLUtil.getXMLString(docIngetCOmpleteOrderDetails));
		
		//Set the getCompleteOrderDetails API Template and invoking the API				
		env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, AcademyConstants.STR_TEMPLATEFILE_GETCOMPLETEORDERDETAILS_BOPISSAVETHESALEFLOW);
		docOutgetCOmpleteOrderDetails = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, docIngetCOmpleteOrderDetails);
		env.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS);
		
		log.verbose("Outdoc of getCompleteOrderDetails :: \n"+ XMLUtil.getXMLString(docOutgetCOmpleteOrderDetails));				
		log.endTimer("AcademyUpdateShipmentNoOnTOContainerForSTS.getCompleteOrderDetails() method - End");
				
		return docOutgetCOmpleteOrderDetails;
	}
	
	
	/** This method invokes changeShipment API to update the SOSHipmentNo on respective TO Container Level
	 * @param env
	 * @param eleContainer
	 * @param strSOShipmentNo
	 * @throws Exception 
	 **/
	/** InDoc:
	 * <Shipment ShipmentKey="">
			<Containers>
				<Container ShipmentContainerKey="" IsReceived="">
					<Extn ExtnSOShipmentNo=""/>
				</Container>
			</Containers>
		</Shipment>
	 **/
	private void updateSOShipmentNoAtContainerLevel(YFSEnvironment env, Element eleContainer, String strSOShipmentNo) throws Exception {
		log.beginTimer("AcademyUpdateShipmentNoOnTOContainerForSTS.updateSOShipmentNoAtContainerLevel() method - Start");
		Document docInChangeShipment = null;
		Document docOutChangeShipment = null;
		
		String strTOShipmentKey = eleContainer.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		String strShipmentContainerKey = eleContainer.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY);		
		
		//Prepare changeShipment API inDoc
		docInChangeShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		docInChangeShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strTOShipmentKey);
		Element eleContainers = docInChangeShipment.createElement(AcademyConstants.ELE_CONTAINERS);		
		Element elContainer = docInChangeShipment.createElement(AcademyConstants.ELE_CONTAINER);
		elContainer.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, strShipmentContainerKey);
		elContainer.setAttribute(AcademyConstants.ATTR_IS_RECEIVED, AcademyConstants.STR_YES);
		Element eleEXTN = docInChangeShipment.createElement(AcademyConstants.ELE_EXTN);
		eleEXTN.setAttribute(AcademyConstants.ATTR_EXTN_SO_SHP_NO, strSOShipmentNo);		
		elContainer.appendChild(eleEXTN);
		eleContainers.appendChild(elContainer);
		docInChangeShipment.getDocumentElement().appendChild(eleContainers);
		
		log.verbose("Input - ChangeShipment Api :: \n" + XMLUtil.getXMLString(docInChangeShipment));
		docOutChangeShipment = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docInChangeShipment);
		log.verbose("outDoc - ChangeShipment Api :: \n" + XMLUtil.getXMLString(docOutChangeShipment));
		
		log.beginTimer("AcademyUpdateShipmentNoOnTOContainerForSTS.updateSOShipmentNoAtContainerLevel() method - End");
	}

}
