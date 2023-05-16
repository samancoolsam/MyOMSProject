//package declaration
package com.academy.ecommerce.sterling.shipment;
//import statements
//java util import statements
import java.util.Properties;
//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
//academy import statements
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
//yantra import statements
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
/**
 * Description: Class AcademySFSCheckForStoreShipment checks for the NodeType is STORE OR NodeType is DC AND ShipNode is 001
 * If yes, it sets the attribute IsOnboardedStore="Y"
 */
public class AcademySFSCheckForStoreShipment
{
	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySFSCheckForStoreShipment.class);
	
	//Instance to store the properties configured for the condition in configurator
	private Properties	props;
	// Stores property:PATH configured in configurator
	public void setProperties(Properties props) throws Exception
	{
		this.props = props;		
	}	
	private static YIFApi	api	= null;
	static
	{
		try
		{
			api = YIFClientFactory.getInstance().getApi();
		} catch (YIFClientCreationException e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @param inXML
	 *            Input Document.
	 * @return getShipNodeListOutputDoc
	 */
	public Document isStoreShipment(YFSEnvironment env, Document inXML) throws Exception
	{
		
		log.verbose("AcademySFSCheckForStoreShipment inXML:"+ XMLUtil.getXMLString(inXML));
		//Declare document variable
		Document getShipNodeListInputDoc = null;
		Document getShipNodeListOutputDoc = null;
		Document templateDoc=null;
		//Declare element variables
		Element eleRootElement =null;
		Element eleOutShipNode=null;
		//Declare String variable
		String strShipNode="";
		String strNodeType="";
		//Start: Process API getShipNodeList
		//Create root element ShipNode
		getShipNodeListInputDoc = XMLUtil.createDocument(AcademyConstants.SHIP_NODE);
		eleRootElement = getShipNodeListInputDoc.getDocumentElement();
		//Fetch the attribute value of ShipNode from the Xpath configured in the service arugument 
		strShipNode= XMLUtil.getAttributeFromXPath(inXML,(String) props.getProperty(AcademyConstants.KEY_ATTR_XPATH));
		//Check if the value is blank
		if (!strShipNode.trim().equals(""))
		{
			//setting the value for attribute ShipNode
			eleRootElement.setAttribute(AcademyConstants.SHIP_NODE,strShipNode );
			
			log.verbose("getShipNodeList API inXML:"+ XMLUtil.getXMLString(getShipNodeListInputDoc));
			
			//Set the template for getShipNodeList API
			templateDoc = YFCDocument.getDocumentFor("<ShipNodeList><ShipNode ShipNode=\"\" NodeType=\"\" /></ShipNodeList>").getDocument();
			env.setApiTemplate(AcademyConstants.API_GET_SHIP_NODE_LIST, templateDoc);
			// invoking the API getShipNodeList
			getShipNodeListOutputDoc = api.getShipNodeList(env, getShipNodeListInputDoc);
			//End: Process API getShipNodeList
			
			log.verbose("getShipNodeList API outXML:"+ XMLUtil.getXMLString(getShipNodeListOutputDoc));
			
			//Fetch the attribute ShipNode from the output xml
			eleOutShipNode = (Element) getShipNodeListOutputDoc.getElementsByTagName(AcademyConstants.SHIP_NODE).item(0);
			//Check if ShipNode value if void
			if (!YFCObject.isVoid(eleOutShipNode))
			{
				//Fetch NodeType Value
				strNodeType = eleOutShipNode.getAttribute("NodeType");
				// START SHIN-3 Adding the new NodeType - 'SharedInventoryDC' check in addition to existing 'Store' Check 	
				if(strNodeType.equals("Store") || (AcademyConstants.ATTR_VAL_SHAREDINV_DC).equals(strNodeType))
				// END SHIN-3	
				{
					log.verbose("Store Shipment");
					getShipNodeListOutputDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ONBOARD_STORE, AcademyConstants.ATTR_Y);
					return getShipNodeListOutputDoc;
				}
				/* START SHIN-3 Commenting below logic as it is not needed. Taken care of above in 'SharedInventoryDC' check 
				else if(strNodeType.equals("DC"))
				{
					log.verbose("DC Shipment");
					Document commonCodeDoc = getCommonCodeList(env);

					Element eleCommonCode = (Element) commonCodeDoc.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE).item(0);

					String strCodeValue = eleCommonCode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
					
					if(strShipNode.equals(strCodeValue))			
					{
						log.verbose("DC and 001");
						getShipNodeListOutputDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ONBOARD_STORE, AcademyConstants.ATTR_Y);
						return getShipNodeListOutputDoc;
					}

				}
				 END SHIN-3 End of Comment  */
				else
				{
					//Return the input xml
					return inXML;
				}		
			}		
		}
		//Return the input XML
		return inXML;	
	}
	/* START SHIN-3 Commenting this as DC_AS_STORE is redundant common code post Shared Inventory 2.0
	public Document getCommonCodeList(YFSEnvironment env) throws Exception
	{
		Document getCommonCodeListInDoc=null;
		Document getCommonCodeListOutDoc=null;
		Element eleRootElement=null;

		getCommonCodeListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		eleRootElement = getCommonCodeListInDoc.getDocumentElement();

		eleRootElement.setAttribute(AcademyConstants.ATTR_CODE_TYPE, "DC_AS_STORE");

		getCommonCodeListOutDoc = api.getCommonCodeList(env, getCommonCodeListInDoc);
		
		log.verbose("getCommonCodeList API outXML:"+ XMLUtil.getXMLString(getCommonCodeListOutDoc));
		
		return getCommonCodeListOutDoc;
	}
	END SHIN-3 End of Comment   */
}