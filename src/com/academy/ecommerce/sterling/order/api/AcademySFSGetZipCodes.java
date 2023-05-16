//package declaration
package com.academy.ecommerce.sterling.order.api;

//import statements

//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//java util import statements
import java.util.HashMap;
import java.util.Properties;

//academy import statements
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;

//yantra import statements
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/** Description: Class AcademySFSGetZipCodes gets the zipcodes from the input
 * and processes them depending on whether the zip code is 5 digit or 9 digit
 */

public class AcademySFSGetZipCodes
{
	/** log variable for logging messages */
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademySFSGetZipCodes.class);

	//Instance to store the properties configured for the condition in configurator
	private Properties	props;

	// Stores property:PATH configured in configurator
	public void setProperties(Properties props) throws Exception
	{
		this.props = props;

	}

	/**
	 * Takes the zip code from each PersonInfoShipTo element coming in the input
	 * If zip code is 9 digit then creates a new entry in the table YFS_ZIP_CODE_LOCATION 
	 * @param env Yantra Environment Context
	 * @param inXML Input Document
	 */

	public void getZipCode(YFSEnvironment env, Document inXML)throws Exception
	{
		//creating the input(complex query) for getZipCodeLocationList API
		//<ZipCodeLocation ZipCode="kiran" Country="US">
		//<ComplexQuery Operator="OR">
		//<And><Or>
		//<Exp Name="ZipCode" Value="00501"/>
		//<Exp Name="ZipCode" Value="00544"/>
		//<Exp Name="ZipCode" Value="00601"/>
		//</Or></And>
		//</ComplexQuery>
		//<Template>
		//<ZipCodeLocationList>
		//<ZipCodeLocation ZipCodeLocationKey="" ZipCode="" State="" Longitude="" Latitude=""  Country="" City=""/>
		//</ZipCodeLocationList>
		//</Template>
		//</ZipCodeLocation>

		//Creating the root element ZipCodeLocation
		Document getZipCodeLocationListInputDoc = YFCDocument.createDocument("ZipCodeLocation").getDocument();
		Element eleRootElement = getZipCodeLocationListInputDoc.getDocumentElement();

		//Creating the element Template
		Element eleTemplate = getZipCodeLocationListInputDoc.createElement("Template");
		eleRootElement.appendChild(eleTemplate);

		//Creating the element ZipCodeLocationList
		Element eleZipCodeLocationList = getZipCodeLocationListInputDoc.createElement("ZipCodeLocationList");
		eleTemplate.appendChild(eleZipCodeLocationList);

		//Creating the element ZipCodeLocation
		Element eleZipCodeLoc = getZipCodeLocationListInputDoc.createElement("ZipCodeLocation");
		eleZipCodeLocationList.appendChild(eleZipCodeLoc);

		//Setting attributes needed in the output template
		eleZipCodeLoc.setAttribute("ZipCodeLocationKey", "");
		eleZipCodeLoc.setAttribute("ZipCode", "");
		eleZipCodeLoc.setAttribute("State", "");
		eleZipCodeLoc.setAttribute("Longitude", "");
		eleZipCodeLoc.setAttribute("Latitude", "");
		eleZipCodeLoc.setAttribute("Country", "");
		eleZipCodeLoc.setAttribute("City", "");

		//Creating the element ComplexQuery
		Element eleComplexQuery = getZipCodeLocationListInputDoc.createElement("ComplexQuery");
		eleRootElement.appendChild(eleComplexQuery);
		eleComplexQuery.setAttribute("Operator", "OR");

		//Creating the element And
		Element eleAnd  =  getZipCodeLocationListInputDoc.createElement("And");
		eleComplexQuery.appendChild(eleAnd);

		//Creating the element Or
		Element eleOr  =  getZipCodeLocationListInputDoc.createElement("Or");
		eleAnd.appendChild(eleOr);

		//Fetch the nodelist PersonInfoShipTo
		NodeList nlPersonInfoShipTo=inXML.getElementsByTagName(props.getProperty("ADDRESS_ELEMENT"));

		HashMap<String,String> zipCodeMap = new HashMap<String,String>();

		//Loop through the NodeList PersonInfoShipTo
		for(int i=0;i<nlPersonInfoShipTo.getLength();i++)
		{
			//Fetch the element PersonInfoShipTo
			Element elePersonInfoShipTo=(Element)nlPersonInfoShipTo.item(i);
			//Fetch the attribute ZipCode
			String strZipCode9D=elePersonInfoShipTo.getAttribute("ZipCode");

			if(strZipCode9D.trim().equals(""))
			{
				continue;
			}

			if(strZipCode9D.length() == 5)
			{
				continue;
			}

			//Get the 5 digit zip code from 9 digit zip code
			String strZipCode5D=strZipCode9D.substring(0,5);

			//Form the complex query
			if(zipCodeMap.size() == 0)
			{
				eleRootElement.setAttribute("ZipCode", strZipCode9D);

				Element eleExp = getZipCodeLocationListInputDoc.createElement("Exp");
				eleExp.setAttribute("Name", "ZipCode");
				eleExp.setAttribute("Value", strZipCode5D);
				eleOr.appendChild(eleExp);

				zipCodeMap.put(strZipCode9D,null);
				zipCodeMap.put(strZipCode5D,null);
			}
			else if(!zipCodeMap.containsKey(strZipCode9D))
			{
				Element eleExp = getZipCodeLocationListInputDoc.createElement("Exp");
				eleExp.setAttribute("Name", "ZipCode");
				eleExp.setAttribute("Value", strZipCode9D);
				eleOr.appendChild(eleExp);

				eleExp = getZipCodeLocationListInputDoc.createElement("Exp");
				eleExp.setAttribute("Name", "ZipCode");
				eleExp.setAttribute("Value", strZipCode5D);
				eleOr.appendChild(eleExp);		

				zipCodeMap.put(strZipCode9D,null);
				zipCodeMap.put(strZipCode5D,null);		
			}
		}

		if(zipCodeMap.size() == 0)
		{
			return;
		}


		//Invoke service AcademySFSGetZipCodeLocationList
		Document getZipCodeLocationListOutpututDoc=AcademyUtil.invokeService(env,"AcademySFSGetZipCodeLocationList", getZipCodeLocationListInputDoc);


		//Fetch the nodelist nlZipCodeLocation
		NodeList nlZipCodeLocation=getZipCodeLocationListOutpututDoc.getElementsByTagName("ZipCodeLocation");
		HashMap<String,Element> existingZipCodeMap = new HashMap<String,Element>();

		HashMap<String,String> inputZipCodeMap = new HashMap<String,String>();

		//Loop through the nodelist nlZipCodeLocation
		for(int j=0;j<nlZipCodeLocation.getLength();j++)
		{
			//Fetch the element ZipCodeLocation
			Element eleZipCodeLocation=(Element)nlZipCodeLocation.item(j);

			//Fetch the value of attribute ZipCode
			String strZipCode=eleZipCodeLocation.getAttribute("ZipCode");
			existingZipCodeMap.put(strZipCode, eleZipCodeLocation);
		}

		//Loop through the NodeList PersonInfoShipTo
		for(int i=0;i<nlPersonInfoShipTo.getLength();i++)
		{
			//Fetch the element PersonInfoShipTo
			Element elePersonInfoShipTo=(Element)nlPersonInfoShipTo.item(i);

			//Fetch the attribute ZipCode
			String strZipCode9D=elePersonInfoShipTo.getAttribute("ZipCode");

			if(strZipCode9D.trim().equals(""))
			{
				continue;
			}

			if(strZipCode9D.length() == 5)
			{
				continue;
			}

			//Get the 5 digit zip code from 9 digit zip code
			String strZipCode5D=strZipCode9D.substring(0,5);

			//check to see whether input has same zip codes
			if(!inputZipCodeMap.containsKey(strZipCode9D)){
				//Check if hashmap contains the zipcode
				if(!existingZipCodeMap.containsKey(strZipCode9D))
				{
					//If not, then get the entire element
					Element eleZipCodeLocation = existingZipCodeMap.get(strZipCode5D);

					if(eleZipCodeLocation == null)
					{
						createAlertForExceptions(env, strZipCode5D);
					}
					else
					{
						//Set the value of attributes ZipCode and ZipCodeLocationKey as 9 digit zip code
						eleZipCodeLocation.setAttribute("ZipCode",strZipCode9D);
						eleZipCodeLocation.setAttribute("ZipCodeLocationKey", strZipCode9D);

						//Convert element to document
						Document createZipCodeLocationInputDoc=XMLUtil.getDocumentForElement(eleZipCodeLocation);

						inputZipCodeMap.put(strZipCode9D,null);
						
						//Invoke service AcademySFSCreateZipCodeLocation
						AcademyUtil.invokeService(env,"AcademySFSCreateZipCodeLocation", createZipCodeLocationInputDoc);
					}
				}

			}
		}
	}

	/** This method creates the input for Alert generation, call createException to Raise alert.	 * 
	 * @param env
	 * @param itemStorageList
	 * @param currOrderNo
	 * @param currOrderHeaderkey
	 * @throws Exception
	 * @author 250626
	 */
	private void createAlertForExceptions(YFSEnvironment env, String zipCode) throws Exception
	{
		log.beginTimer(" Begin of AcademySFSGetZipCodes  createAlertForExceptions()- Api");
		//Create element Inbox
		Document createExceptionInputDoc = YFCDocument.createDocument(AcademyConstants.ELE_INBOX).getDocument();

		Element inboxElm = createExceptionInputDoc.getDocumentElement();
		//Set the value for attribute ExceptionType
		inboxElm.setAttribute("ExceptionType", "Invalid Zip Code");
		//Set the value for attribute InboxType
		inboxElm.setAttribute("InboxType", "Zip Code Validation");
		//Set the value for attribute Description
		inboxElm.setAttribute("Description", zipCode);
		//Set the value for attribute QueueId
		inboxElm.setAttribute("QueueId", "SFS_INVALID_ZIP_CODE");
		//Set the value for attribute Consolidate
		inboxElm.setAttribute("Consolidate", "Y");
		//Set the value for attribute ConsolidationWindow
		inboxElm.setAttribute("ConsolidationWindow", "DAY");
		//Create element ConsolidationTemplate
		Element consolidationTemplate = createExceptionInputDoc.createElement("ConsolidationTemplate");
		//Append child element
		inboxElm.appendChild(consolidationTemplate);
		//Create element Inbox
		Element consolidationTemplateInbox = createExceptionInputDoc.createElement("Inbox");
		//Append child element
		consolidationTemplate.appendChild(consolidationTemplateInbox);
		//Set the value for attribute ExceptionType
		consolidationTemplateInbox.setAttribute("ExceptionType", "");
		//Set the value for attribute InboxType
		consolidationTemplateInbox.setAttribute("InboxType", "");
		//Set the value for attribute Description
		consolidationTemplateInbox.setAttribute("Description", "");
		//Set the value for attribute ActiveFlag
		consolidationTemplateInbox.setAttribute("ActiveFlag", "");
		//Set the value for attribute GeneratedOn
		consolidationTemplateInbox.setAttribute("GeneratedOn", "");

		// <ConsolidationTemplate> for Order
		// <Inbox ActiveFlag="" ExceptionType="" InboxType=""
		// OrderHeaderKey=""

		// <ConsolidationTemplate> Generic
		// <Inbox ActiveFlag="" Description="" ExceptionType="" InboxType=""
		//Print the verbose log
		if (log.isVerboseEnabled())
		{
			log.verbose("********* Input XML for createException from method createAlertForExceptions :: AcademySFSProcessBackupStoreAllocationAlertAPI" + XMLUtil.getXMLString(createExceptionInputDoc));
		}
		//Invoke createException API
		AcademyUtil.invokeAPI(env, AcademyConstants.API_CREATE_EXCEPTION, createExceptionInputDoc);

		log.endTimer(" End of AcademySFSGetZipCodes  createAlertForExceptions()- Api");
	}
}