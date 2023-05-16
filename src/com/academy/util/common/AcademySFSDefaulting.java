//package declaration
package com.academy.util.common;
//java util import statements
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
//academy import statements
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.xml.XMLUtil;
import com.academy.util.constants.AcademyConstants;
//yantra import statements
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * Description: Class AcademySFSDefaulting gets the value of the a particular attribute specified in the arguments
 * and stamps it for all the values in a particular path. This is again specified in the arguments
 */
public class AcademySFSDefaulting
{

	
	//Instance to store the properties configured for the condition in configurator
	private Properties	props;
	
	// Stores property:PATH configured in configurator
	public void setProperties(Properties props) throws Exception
	{
		this.props = props;
		
	}

	/**
	 * Checks the value to be stamped and stamps it accordingly
	 * in the xpath mentioned in the arguments set in the service
	 *
	 * @param env
	 *            Yantra Environment Context.
	 * @param inXML
	 *            Input Document.
	 * @return inXML
	 */
	public Document stampAttributesByXPath(YFSEnvironment env, Document inXML) throws Exception
	{
		//Declare Nodelist variable
		NodeList nlDesElement=null;
		//Declare String variable
		String noOfPropertiesConfigured ="";
		String sourceXPath="";
		String destXPath="";
		String parentDestXpath="";
		String attrDestName="";
		String attrSourceName="";
		String sourceValue = "";
		String sourceNewValue="";
		String sourceEnterpriseCode="";
		String sourceCommonCodeType="";
		String strStartIndex="";
		String strEndIndex="";
		//Declare integer varibale
		int destindex=0;

	
		//Fetch the Argument count defined in the argument
		noOfPropertiesConfigured = (String) props.getProperty(AcademyConstants.KEY_COUNT_ARG);
		
		//Parse it to integer
		int nProperties = Integer.parseInt(noOfPropertiesConfigured);

		//Loop through each argument count
		for(int count = 1 ; count <= nProperties ; count++ )
		 {
			//Fetch the argument value of  GET_XPATH_
			sourceXPath =(String) props.getProperty(AcademyConstants.KEY_GET_XPATH+count);
			//Fetch the argument value of SET_XPATH_
			destXPath =(String) props.getProperty(AcademyConstants.KEY_SET_XPATH+count);
			if (!YFCObject.isVoid(destXPath))
			{
			//Fetch the details till index /@
			destindex=destXPath.lastIndexOf(AcademyConstants.ATTR_INDEX);
			
			//Fetch the attribute name
			attrDestName=destXPath.substring(destindex+2,destXPath.length());
			
			}
			//Check if sourceXPath starts with common:
			if(sourceXPath.toLowerCase().startsWith(AcademyConstants.KEY_ARG_COMMON))
			{
				//Split the string for the matched argument ":"
				String[]sourcenewXPath = sourceXPath.split(AcademyConstants.STR_COLON);
		     	//Store the values
				sourceEnterpriseCode=sourcenewXPath[1];				
				sourceCommonCodeType=sourcenewXPath[2];
				//Fetch the xPath
				sourceXPath= sourceXPath.substring(sourceEnterpriseCode.length()+sourceCommonCodeType.length()+9);				
				//fetch the value of the xPath
				sourceNewValue = getValue(env,sourceXPath,  inXML);
				
				//Invoke method to fetch the common code list for the given CodeType
				HashMap<String, String> hmpDocIdPrefix = AcademyCommonCode.getCommonCodeListAsHashMap(env, sourceCommonCodeType, sourceEnterpriseCode);
				//Fetch the value of the required key
				sourceValue=hmpDocIdPrefix.get(sourceNewValue);				
			}
			//Check if sourceXPath starts with concat:
			else if(sourceXPath.toLowerCase().startsWith(AcademyConstants.KEY_ARG_CONCAT))
			{
				//Fetch the source XPth
				sourceXPath= sourceXPath.substring(sourceXPath.lastIndexOf(AcademyConstants.STR_COLON)+1);
				//Fetch the Argument count defined in the argument
		     	int noOfConcetArguments = Integer.parseInt(props.getProperty(sourceXPath + AcademyConstants.KEY_CONCAT_ARG));
	     		//Define string builder
				StringBuilder strbAppend= new StringBuilder();
				//Loop through the each arguments
				for(int concatcount = 1 ; concatcount <= noOfConcetArguments ; concatcount++ )
				 {
					//Fetch the argument value
					String sourcenewXPath =(String) props.getProperty(sourceXPath+ "_" + concatcount);
					//Fetch the source value
					sourceValue = getValue(env,sourcenewXPath,  inXML);
					//Append the details into the string builder
					strbAppend.append(sourceValue);
					
				 }
				//Store the store the builder
				sourceValue=strbAppend.toString();
				
			}
			//Check if sourceXPath starts with RenameRoot:
			else if(sourceXPath.toLowerCase().startsWith(AcademyConstants.KEY_RENAME_ROOT))
			{
				//Fetch the source XPth
				sourceXPath= sourceXPath.substring(sourceXPath.lastIndexOf(AcademyConstants.STR_COLON)+1);
				Element inXmlRoot = inXML.getDocumentElement();
				inXML.renameNode(inXmlRoot, inXmlRoot.getNodeName(), sourceXPath);
				
				
				//Element eleOrgRoot=inXML.getDocumentElement();
				//inXML=XMLUtil.createDocument(sourceXPath);
				//Element eleRename = inXML.getDocumentElement();
				//XMLUtil.copyElement(inXML, eleOrgRoot, eleRename);
				continue;
			}
			//Check if sourceXPath starts with ChangeRoot:
			else if(sourceXPath.toLowerCase().startsWith(AcademyConstants.KEY_CHANGE_ROOT))
			{
				sourceXPath= sourceXPath.substring(sourceXPath.lastIndexOf(AcademyConstants.STR_COLON)+1);
				Element eleChangeRoot=(Element)XMLUtil.getNodeListByXPath(inXML, sourceXPath).item(0);
				//inXML.replaceChild(inXML.getDocumentElement(), eleChangeRoot);		
				inXML=XMLUtil.getDocumentForElement(eleChangeRoot);
				continue;
			}
			else if(sourceXPath.toLowerCase().startsWith(AcademyConstants.KEY_ARG_SUBSTRING))
			{
				String[]srcIndex = sourceXPath.split(AcademyConstants.STR_COLON);
		     	//Store the values
				strStartIndex=srcIndex[1];				
				strEndIndex=srcIndex[2];
				//Fetch the xPath
				sourceXPath= sourceXPath.substring(strStartIndex.length()+strEndIndex.length()+12);	
				//Fetch the attribute name
				attrSourceName=sourceXPath.substring((sourceXPath.lastIndexOf(AcademyConstants.ATTR_INDEX))+2,sourceXPath.length());
				//Fetch the nodelist
				NodeList nlSourceElement=XMLUtil.getNodeListByXPath(inXML, sourceXPath.substring(0, sourceXPath.lastIndexOf(AcademyConstants.ATTR_INDEX)));
				//Loop through each nodelist
				for (int i=0; i<nlSourceElement.getLength();i++)
				{
					//Fetch the element
					Element elesourceXPath=(Element)nlSourceElement.item(i);
					//fetch the attribute value
					sourceNewValue=elesourceXPath.getAttribute(attrSourceName);
					//Validate the attribute
					if (sourceNewValue != null && !sourceNewValue.equals("") && ((sourceNewValue.length()>Integer.parseInt(strEndIndex) ||(sourceNewValue.length()==Integer.parseInt(strEndIndex)))))
					{			
					//Trim the attribute value
					sourceNewValue=sourceNewValue.substring(Integer.parseInt(strStartIndex), Integer.parseInt(strEndIndex));
					//Set the trimmed attribute value
					XMLUtil.setAttribute((Element)nlSourceElement.item(i), attrSourceName, sourceNewValue);	
					}
				}
				continue;
			}
			else
			{
				//Fetch the sourceValue
				sourceValue = getValue(env,sourceXPath,  inXML);
			}
			
			//Check if the source value is not null or not blank
			if(sourceValue != null && !sourceValue.equals(""))
			{
				//if true,check if destination xpath starts with env 
				if(destXPath.toLowerCase().startsWith("env:"))
				{
					//set the source value into desitination xpath
					env.setTxnObject(destXPath.substring(4), sourceValue);
				}
				else
				{
					//Fetch the details till index /@
					destindex=destXPath.lastIndexOf(AcademyConstants.ATTR_INDEX);
					//Fetch the root element
					parentDestXpath=destXPath.substring(0, destindex);
					//Fetch the attribute name
					attrDestName=destXPath.substring(destindex+2,destXPath.length());
					//Fetch the Nodelist of element stored in parentDestXpath
					nlDesElement=XMLUtil.getNodeListByXPath(inXML, parentDestXpath);

					//Loop through each NodeList
					for (int i=0; i<nlDesElement.getLength();i++)
					{
						if(sourceValue != null && !sourceValue.equals(""))
						{
							//Set the attribute value into the attribute fetched from SET_XPATH_
							XMLUtil.setAttribute((Element)nlDesElement.item(i), attrDestName, sourceValue);
						}
					}
				}
			}
		}

	  //Return the XML
	  return inXML;
	}

	

	/**
	 * Checks the value to be stamped and stamps it accordingly in the xpath mentioned 
	 * in the arguments set in the service.Also the stamped value will be saved in the 
	 * transaction map.
	 *
	 * @param env
	 *            Yantra Environment Context.
	 * @param inXML
	 *            Input Document.
	 * @return inXML
	 */
	public Document saveDataToTransactionMap(YFSEnvironment env, Document inXML) throws Exception
	{
		//Declare String variable
		String noOfPropertiesConfigured ="";
		String sourceXPath="";
		String destXPath="";
		String sourceValue = "";

		//Fetch the Argument count defined in the argument
		noOfPropertiesConfigured = (String) props.get(AcademyConstants.KEY_COUNT_ARG);
		//Parse it to integer
		int nProperties = Integer.parseInt(noOfPropertiesConfigured);

		//Loop through each argumrnt count
		for(int count = 1 ; count <= nProperties ; count++ )
		{
			//Fetch the arugument value of  GET_XPATH_
			sourceXPath =(String) props.getProperty(AcademyConstants.KEY_GET_XPATH+count);
			//Fetch the arugument value of SET_XPATH_
			destXPath =(String) props.getProperty(AcademyConstants.KEY_SET_XPATH+count);
		
			sourceValue = getValue(env,sourceXPath,  inXML);
			
			//Check if the source value is not null or not blank and destination xpath starts with env
			if(sourceValue != null && !sourceValue.equals("") && destXPath.toLowerCase().startsWith("env:"))
			{
				//Set the detination xpath with value from sourceValue
				env.setTxnObject(destXPath.substring(4), sourceValue);
			}
		}

	  //Return the XML
	  return inXML;
	}
	
	public static String getValue(YFSEnvironment env,String sourceXPath, Document inXML)throws Exception
	{
		String sourceValue = "";
		
		//check if source path value starts with env
		if(sourceXPath.toLowerCase().startsWith("env:"))
		{
			//Fetch the source value from the fourth position of the xpath
			sourceValue = (String)env.getTxnObject(sourceXPath.substring(4));
		}
		//else check if there is any '@' in the path
		else if(sourceXPath.indexOf("@") != -1)
		{
			//Fetch the source value of the attribute
			sourceValue = XMLUtil.getAttributeFromXPath(inXML, sourceXPath);
		}
		else
		{
			//Fetch the source xpath value
			sourceValue = sourceXPath;
		}

		return sourceValue;		
	}
}