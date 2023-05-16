//package declaration
package com.academy.ecommerce.sterling.shipment;
//import statements
//java util import statements
import java.util.Properties;
//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
//academy import statements
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;

//yantra import statements
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * Description: Class AcademySFSStorePrintMessage contains four methods
 * 1)getXml method: This is will prepare the input xml structure for fetching the details from ACAD_STORE_PRINT_MSG table
 * 2)createPrintMessage method: This method will create entry into ACAD_STORE_PRINT_MSG table
 * 3)updatePrintMessage method: This method will update the details for PrintMessage into ACAD_STORE_PRINT_MSG table
 * 4)fetchPrintMessage method: This method will fetch the details from ACAD_STORE_PRINT_MSG table
 * @throws Exception
 * @return outXML
 */

public class AcademySFSStorePrintMessage
{	
	//Begin : OMNI-646
	private static Logger log = Logger
			.getLogger(AcademySFSStorePrintMessage.class.getName());
	//End : OMNI-646
	
	//Define properties to fetch argument value from service configuration
	private Properties	props;
	public void setProperties(Properties props) throws Exception
	{
		this.props = props;
	}
	private YFCException yfcEx = null;
	/**
	 * Description: Method getXml: This method will prepare the input structure as shown below:
	 * <ACADStorePrintMsg ShipmentContainerKey="" PrintKey="" PrintMessage="" MessageType=""/>
	 * @param: inXML document.
	 * @param: ShipmentContainerKey and MessageType
	 * @throws Exception
	 * @return inDoc
	 */
	public Document getXml(YFSEnvironment env, Document inXML) throws Exception
	{
		//Declare document variables
		Document inDoc =null;
		//Declare element variables
		Element eleinDoc=null;
		//Declare string variables
		String strXPATH="";
		String strMessageType="";
		String strAttributeValue="";
		//Fetch ShipmentContainerKey from the argument
		strXPATH=props.getProperty(AcademyConstants.KEY_CONTAINER_KEY);
		//Fetch MessageType from the argument
		strMessageType=props.getProperty(AcademyConstants.KEY_MESSAGE_TYPE);
		//Fetch ShipmentContainerKey value from the xml
		strAttributeValue = XMLUtil.getAttributeFromXPath(inXML, strXPATH);
		//Start: Prepare input to fetchStorePrintMessage
		//Create root element
		inDoc = XMLUtil.createDocument(AcademyConstants.ELE_STORE_PRINT_MSG);
		eleinDoc=inDoc.getDocumentElement();
		//Set attribute value for ShipmentContainerKey
		eleinDoc.setAttribute(AcademyConstants.ATTR_SHIP_CONT_KEY, strAttributeValue);
		//Set attribute value for MessageType
		eleinDoc.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, strMessageType);
		//return the xml
		return inDoc;
		//End: Prepare input to fetchStorePrintMessage
	}
	/**
	 * Description: Method createPrintMessage
	 * @param: inXML document.
	 * @throws Exception
	 */
	public void createPrintMessage(YFSEnvironment env, Document inXML) throws Exception
	{
		//Declare document variable
		Document inDoc=null;
		//Declare element variable
		Element eleinDoc=null;
		//call method to fetch the input xml
		inDoc = getXml(env, inXML);
		//Fetch the root element
		eleinDoc=inDoc.getDocumentElement();
		//Set attribute value for PrintMessage
		eleinDoc.setAttribute(AcademyConstants.ATTR_PRINT_MESSAGE, XMLUtil.getXMLString(inXML));
		//Invoke service to update the details

		AcademyUtil.invokeService(env,AcademyConstants.SER_CREATE_PRINT_MSG, inDoc);
	}
	/**
	 * Description: Method updatePrintMessage
	 * @param: inXML document.
	 * @param: FlowName
	 * @throws Exception
	 */
	public void updatePrintMessage(YFSEnvironment env, Document inXML) throws Exception
	{
		//Declare the document variable
		Document inDoc=null;
		Document outPrintMessage=null;
		Document outDoc=null;
		//Declare element variable
		Element eleinXML=null;
		Element eleoutDoc=null;
		Element elePrint=null;
		Element eleInputData=null;
		Element eleInputDataParentRoot=null;
		Element eleNewInputData=null;
		//Declare String varable
		String strFlowName="";
		String strPrintMessage="";

		//Fetch FlowName from the argument
		strFlowName=props.getProperty(AcademyConstants.KEY_FLOW_NAME);
		//Fetch the root element of the input aml
		eleinXML=inXML.getDocumentElement();
		//Call method to fetch the input xml
		inDoc = getXml(env, inXML);
		//Invoke service to fetch the details from ACAD_STORE_PRINT_MSG table
		outDoc=AcademyUtil.invokeService(env,AcademyConstants.SER_FETCH_PRINT_MSG, inDoc);
		//Fetch the element ACADStorePrintMsg
		elePrint= (Element) outDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_STORE_PRINT_MSG).item(0);
		//Fetch the attribute PrintMessage
		strPrintMessage=elePrint.getAttribute(AcademyConstants.ATTR_PRINT_MESSAGE);
		//Fetch the document from PrintMessage
		outPrintMessage=XMLUtil.getDocument(strPrintMessage);
		//Fetch the root element
		eleoutDoc=outPrintMessage.getDocumentElement();
		//Fetch the node for the matching FlowName
		eleInputData=(Element) XPathUtil.getNode(eleoutDoc,"PrintDocument/InputData[@FlowName='"+ strFlowName + "']");
		//Fetch the parent node
		eleInputDataParentRoot=(Element) eleInputData.getParentNode();
		//Remove the node "Input"
	 	XMLUtil.removeChild(eleInputDataParentRoot, (Element) eleInputData);
		//Create new element InputData
	 	eleNewInputData=outPrintMessage.createElement(AcademyConstants.ELE_INPUT_DATA);
	 	//Append the child
		eleInputDataParentRoot.appendChild(eleNewInputData);
		//import the required xml to the node Input
		XMLUtil.importElement(eleNewInputData, eleinXML);
		//Set attribute value of PrintMessage
		elePrint.setAttribute(AcademyConstants.ATTR_PRINT_MESSAGE, XMLUtil.getXMLString(outPrintMessage));
		//Invoke service to upadte the Print message
		AcademyUtil.invokeService(env,AcademyConstants.SER_UPDATE_PRINT_MSG, XMLUtil.getDocumentForElement(elePrint));

	}
	/**
	 * Description: Method fetchPrintMessage
	 * @param: inXML document.
	 * @param: FlowName
	 * @throws Exception
	 * @return: document
	 */
	public Document fetchPrintMessage(YFSEnvironment env, Document inXML) throws Exception
	{
		//Declare the document variable
		Document inDoc=null;
		Document outDoc=null;
		//Declare the element variable
		Element eleoutdoc=null;
		//Declare the string
		String strPrintMessage=null;
		//Call method to fetch the input xml
		inDoc = getXml(env, inXML);
		//Invoke service to fetch the details from ACAD_STORE_PRINT_MSG table
		outDoc=AcademyUtil.invokeService(env,AcademyConstants.SER_FETCH_PRINT_MSG, inDoc);
		//Check if output document in null
		if (!YFCObject.isVoid(outDoc))
		 {
			//Fetch the element ACADStorePrintMsg
			eleoutdoc= (Element) outDoc.getElementsByTagName(AcademyConstants.ELE_STORE_PRINT_MSG).item(0);
			
			//Begin : OMNI-646
			if(!YFCObject.isVoid(eleoutdoc) && 
					AcademyConstants.STR_CARRIER_RETURN_LABEL
					.equalsIgnoreCase(inDoc.getDocumentElement()
							.getAttribute(AcademyConstants.ATTR_MESSAGE_TYPE))) {
				
				log.debug("Print Message (ACAD_STORE_PRINT_MSG) ::"+XMLUtil.getElementXMLString(eleoutdoc));
							
				//Fetch the attribute value of PrintMessage
				strPrintMessage=eleoutdoc.getAttribute(AcademyConstants.ATTR_PRINT_MESSAGE);
			}else if (AcademyConstants.STR_CARRIER_RETURN_LABEL
					.equalsIgnoreCase(inDoc.getDocumentElement()
							.getAttribute(AcademyConstants.ATTR_MESSAGE_TYPE))){
				log.debug("Document is null ::");
				strPrintMessage = "<PrintDocuments SupportsIntgForRetLabel='N' LabelAvailableForReprint='N' />";
			}else {				
				log.debug("SHIPPING_LABEL_LOGIC ::");
			//End : OMNI-646
				
				//Fetch the attribute value of PrintMessage
				strPrintMessage=eleoutdoc.getAttribute(AcademyConstants.ATTR_PRINT_MESSAGE);
			}

		  }
		  else
		  {
			yfcEx = new YFCException("EXTN_ACADEMY_10");
			throw yfcEx;

			}
	//Return the document of PrintMessage
	return XMLUtil.getDocument(strPrintMessage);
}
}