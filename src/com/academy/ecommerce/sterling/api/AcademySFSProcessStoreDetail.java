/** Description: Class AcademySFSProcessStoreDetail has two methods getPageRecord and 
 *              prepareInputcreateRescheduleStore. 
 *              The getPageRecord method returns the list of release or shipment records
 *              that has to be rescheduled.
 *              prepareInputcreateRescheduleStore will create entries of release key or 
 *              shipment key in ACA_RESCHEDULE_STORE table
 * @throws Exception
 */	

//package declaration
package com.academy.ecommerce.sterling.api;
//java util import statements
import java.util.Properties;
//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
//yantra import statements
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFCustomApi;
//academy util import statements
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;

public class AcademySFSProcessStoreDetail implements YIFCustomApi
{
	//Instance to store the properties configured for the condition in Configurator.
	private Properties props;
	/**
	 * Stores properties configured in configurator. The property expected by this API
	 * is: PAGE_SIZE_VALUE
	 * @param service Properties configured in Configurator.
	 */
	public void setProperties(Properties props) throws Exception 
	{
		this.props = props;
	}

	//Declaring global variable	
	String gstrReleaseEntity="R";
	String gstrShipmentEntity="S";
	/**
	 * Prepares input required for retrieving release list or shipment list 
	 * based on ShipNode and Statuses.
	 * @param env Yantra Environment Context.
	 * @param inXML Input Document.
	 * @return void
	 */ 
	public void getStoreRecord(YFSEnvironment env, Document inXML)
	throws Exception 
	{
		//Fetch the root element Organization
		Element eleOrg =inXML.getDocumentElement();
		//Fetch element Organization/Node
		Element eleNode=XMLUtil.getFirstElementByName(
				eleOrg, AcademyConstants.ELE_NODE);
		//Fetch the value of attribute ShipNode
		String strShipNode=eleOrg.getAttribute(AcademyConstants.ORGANIZATION_CODE);
		
		//Invoke method getPageRecord to process Release entity API	
		//Pass argument: ShipNode,API name getOrderReleaseList, ReleaseFlag=R
		getPageRecord(env,strShipNode,
				AcademyConstants.API_GET_ORDER_RELEASE_LIST,gstrReleaseEntity);
		//Invoke method getPageRecord to process Shipment Entity API
		//Pass argument: ShipNode,API name getShipmentList, ShipmentFlag=S
		getPageRecord(env,strShipNode,
				AcademyConstants.API_GET_SHIPMENT_LIST,gstrShipmentEntity);					

	}

	/**
	 * Retrieves release list or shipment list based on ShipNode and Statuses.
	 * @param env Yantra Environment Context.
	 * @param ShipNode Value.
	 * @param API name(getOrderReleaseList/getShipmentList)
	 * @param EntityFlag. To determine release/shipment record.
	 * @return void
	 */ 
	public void getPageRecord(YFSEnvironment env,String ShipNode, 
			String strAPI, String strEntityFlag)throws Exception 
	 {
		//Declare Integer variable
		int index=0;
		//Declare String variable
		String strIsLastPage="N";
		//Use while loop till output response: Page/@IsLastPage=N
		while (strIsLastPage.equalsIgnoreCase(AcademyConstants.STR_NO))
		{
			//Declare String variable
			String strIndex=Integer.toString(index+1);
			//Declare Element variable
			Element eleInputEntity=null;
			Element eleTemplateEntity=null;
			Element eleTemplateList=null;
			Element eleInput=null;
			Element eleTemplate=null;

			//START: Preparing input document for getPage API
			//XML structure for getPage API
			//<Page PageNumber="" PageSize="" PaginationStrategy="GENERIC" Refresh="Y">
			// <API IsFlow="N" Name="getOrderReleaseList/getShipmentList">
			//	 <Input>
			//    <OrderRelease Status="" ShipNode=""/>
			//	   or
			//	  <Shipment Status="" StatusQueryType="" ShipNode=""/>
			//	</Input>
			//	<Template> 
			//	 <OrderReleaseList><OrderRelease OrderReleaseKey=""/></OrderReleaseList>
			//	  or
			//   <Shipments><Shipment ShipmentKey=""/></Shipments>
			//  </Template>
			// </API>
			// </Page>

			//Create the root element
			Document getPageInput=XMLUtil.createDocument(AcademyConstants.ELE_PAGE);
			Element eleRooElement=getPageInput.getDocumentElement();
			//Set the attribute PageNumber, by mapping index value
			eleRooElement.setAttribute(AcademyConstants.ATTR_PAGE_NUMBER,strIndex);
			//Set the attribute PageSize, by mapping the value from service argument
			eleRooElement.setAttribute(AcademyConstants.ATTR_PAGE_SIZE,
					props.getProperty(AcademyConstants.KEY_PAGE_SIZE_VALUE));
					
			//Set the value for attribute PaginationStrategy
			eleRooElement.setAttribute(AcademyConstants.ATTR_PAGINATION_STRATEGY,
					AcademyConstants.ATTR_GENERIC);
			//Set the value for attribute Refersh
			eleRooElement.setAttribute(AcademyConstants.ATTR_REFRESH,
					AcademyConstants.ATTR_Y);
			//Create element API
			Element eleAPI=getPageInput.createElement(AcademyConstants.ELE_API);
			//Set the value for attribute IsFlow
			eleAPI.setAttribute(AcademyConstants.ATTR_IS_FLOW,
					AcademyConstants.STR_NO);
			//Map the value for APIName
			eleAPI.setAttribute(AcademyConstants.ATTR_NAME,strAPI);
			//Append the child element
			eleRooElement.appendChild(eleAPI);
			//Create element Input
			eleInput=getPageInput.createElement(AcademyConstants.ELE_INPUT);
			//append the child element
			eleAPI.appendChild(eleInput);
			//Check if Entity is Release
			if(strEntityFlag.equalsIgnoreCase(gstrReleaseEntity)){
				//Create element OrderRelease
				eleInputEntity=getPageInput.createElement(
						AcademyConstants.ELE_ORD_RELEASE);
				//Set the value for attribute Status
				eleInputEntity.setAttribute(AcademyConstants.ATTR_STATUS,
						AcademyConstants.VAL_RELE_STATUS);
			}
			//If Entity is not Release
			else{
				//Create element Shipment
				eleInputEntity=getPageInput.createElement(AcademyConstants.ELE_SHIPMENT);
				//Set the value for attribute Status
				eleInputEntity.setAttribute(AcademyConstants.ATTR_STATUS,
						AcademyConstants.VAL_SHIP_STATUS);
				//Set the value for attribute StatusQueryType
				eleInputEntity.setAttribute(AcademyConstants.ATTR_STATUS_QRY,
						AcademyConstants.VAL_STATUS_QRY);
			}
			//Set the value for attribute ShipNode
			eleInputEntity.setAttribute(AcademyConstants.SHIP_NODE,ShipNode);
			//Append Child element
			eleInput.appendChild(eleInputEntity);
			//Create element Template
			eleTemplate=getPageInput.createElement(AcademyConstants.ELE_TEMPLATE);
			//Check if Entity is Release
			if(strEntityFlag.equalsIgnoreCase(gstrReleaseEntity)){
				//Create element OrderReleaseList
				eleTemplateList=getPageInput.createElement(
						AcademyConstants.ELE_RELEASE_LIST);
				//Append Child element
				eleTemplate.appendChild(eleTemplateList);
				//Create element OrderRelease
				eleTemplateEntity=getPageInput.createElement(
						AcademyConstants.ELE_ORD_RELEASE);
				//Set the value for attribute OrderReleaseKey
				eleTemplateEntity.setAttribute(AcademyConstants.ATTR_RELEASE_KEY,"");
			}
			//Check if Entity is not Release
			else{
				//Create element Shipments
				eleTemplateList=getPageInput.createElement(
						AcademyConstants.ELE_SHIPMENTS);
				//Append the child element
				eleTemplate.appendChild(eleTemplateList);
				//Create element Shipment
				eleTemplateEntity=getPageInput.createElement(
						AcademyConstants.ELE_SHIPMENT);
				//Set the value for attribute ShipmentKey
				eleTemplateEntity.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,"");
			}
			//Append the child element
			eleTemplateList.appendChild(eleTemplateEntity);		
			//Append the child element
			eleAPI.appendChild(eleTemplate);
			//END: Preparing input document for getPage API
			//Invoke getPage Standard Sterling API
			Document getPageOutput=AcademyUtil.invokeAPI(env, 
					AcademyConstants.API_GET_PAGE, getPageInput);
			//Fetch the attribute value IsLastPage
			strIsLastPage=getPageOutput.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_IS_LAST_PAGE);
			//Invoke method prepareInputcreateRescheduleStore to process getPage output
			//Pass argument: environment variable, getPage API output, EntityFlag (R/S)
			prepareInputcreateRescheduleStore(env,getPageOutput,strEntityFlag);
			//Increment the index counter value
			index++;
		}
	}
	/**
	 * Prepare input for createACARescheduleStore API with EntityKey as 
	 * OrderReleaseKey or ShipmentKey.
	 * @param env Yantra Environment Context.
	 * @param getPageOutput Document.
	 * @param EntityFlag. To determine release/shipment record.
	 * @return void
	 */ 
	public void prepareInputcreateRescheduleStore(YFSEnvironment env,
			Document getPageOutput, String EntityFlag)throws Exception 
	 {
		//Declare Document variable
		Document creatACARescheduleStore=null;
		//Declare NodeList variable
		NodeList nlList=null;
		//Declare element
		Element eleOuputEntity=null;
		Element eleRooElement=null;
		
		//Check if Entity is Release
		if(EntityFlag.equalsIgnoreCase(gstrReleaseEntity))
		{
			//Fetch the NodeList of element OrderRelease
			nlList=getPageOutput.getElementsByTagName(AcademyConstants.ELE_ORD_RELEASE);
		}
		//If Entity is not Release
		else
		{
			//Fetch the NodeList of element Shipment
			nlList=getPageOutput.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
		}
		//Loop through each NodeList
		for (int index=0;index<nlList.getLength();index++)
		{
			//Fetch the element
			eleOuputEntity = (Element) nlList.item(index);
			//START: Preparing input document for createACARescheduleStore 
			//Extended Database API
			//XML structure for createACARescheduleStore API
			//<ACARescheduleStore EntityKey="" EntityFlag="" CancelStatus=""/>
			//Create element ACARescheduleStore
			creatACARescheduleStore=XMLUtil.createDocument(
					AcademyConstants.ELE_ACS_RESCHEDULE_STORE);
			eleRooElement=creatACARescheduleStore.getDocumentElement();
			//Check if Entity Flag =R
			if(EntityFlag.equalsIgnoreCase(gstrReleaseEntity))
			{
				//Set the attribute value of OrderReleaseKey
				eleRooElement.setAttribute(AcademyConstants.ATTR_ENTITY_KEY,
						eleOuputEntity.getAttribute(AcademyConstants.ATTR_RELEASE_KEY));
			}
			//If Check if Entity Flag=S
			else
			{
				//Set the attribute value of ShipmentKey
				eleRooElement.setAttribute(AcademyConstants.ATTR_ENTITY_KEY,
						eleOuputEntity.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
			}
			//Set the attribute EntityFalg
			eleRooElement.setAttribute(AcademyConstants.ATTR_ENTITY_FLAG,EntityFlag);
			//Set the attribute CancelStatus
			eleRooElement.setAttribute(AcademyConstants.ATTR_CANCEL_STATUS, 
					AcademyConstants.ATTR_Y);
			//START: Preparing input document for createACARescheduleStore 
			//Extended Database API 
			//Invoke service AcademySFSUpdateRecordService
			AcademyUtil.invokeService(env, AcademyConstants.SERV_ACA_SFSUPDATE_RECORD, 
					creatACARescheduleStore);
		}
	}
}
