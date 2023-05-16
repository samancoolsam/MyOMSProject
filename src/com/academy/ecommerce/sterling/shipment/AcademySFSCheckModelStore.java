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
import com.yantra.yfs.japi.YFSEnvironment;
/**
 * Description: Class AcademySFSCheckModelStore checks if the store is onboarded or not. 
 * If yes, it sets the attribute IsOnboardedStore="Y"
 */
public class AcademySFSCheckModelStore
{
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
	 * checks if the store is onboarded or not. 
	 * If yes, it sets the attribute IsOnboardedStore="Y"
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @param inXML
	 *            Input Document.
	 * @return getShipNodeListOutputDoc
	 */
	public Document isModelStore(YFSEnvironment env, Document inXML) throws Exception
	{
		
		//Declare document variable
		Document getShipNodeListInputDoc = null;
		Document getShipNodeListOutputDoc = null;
		Document templateDoc=null;
		//Declare element variables
		Element eleRootElement =null;
		Element eleOutShipNode=null;
		//Declare String variable
		String strShipNode="";
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
		//Set the template for getShipNodeList API
		templateDoc = YFCDocument.getDocumentFor("<ShipNodeList><ShipNode ShipNode=\"\" IsModelStore=\"\" /></ShipNodeList>").getDocument();
		env.setApiTemplate(AcademyConstants.API_GET_SHIP_NODE_LIST, templateDoc);
		// invoking the API getShipNodeList
		getShipNodeListOutputDoc = api.getShipNodeList(env, getShipNodeListInputDoc);
		//End: Process API getShipNodeList
		//Fetch the attribute ShipNode from the output xml
		eleOutShipNode = (Element) getShipNodeListOutputDoc.getElementsByTagName(AcademyConstants.SHIP_NODE).item(0);
		//Check if ShipNode value if void
		if (!YFCObject.isVoid(eleOutShipNode))
		 {
			//Check if attribute value of IsModelStore is blank
			if(!eleOutShipNode.getAttribute(AcademyConstants.ATTR_MODEL_STORE).trim().equals(""))			
		    {		
				//Set the attribute value if IsOnboardedStore=Y
			   getShipNodeListOutputDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ONBOARD_STORE, AcademyConstants.ATTR_Y);
			   //Retunr the getShipNodeListOutputDoc xml 
			    return getShipNodeListOutputDoc;
		     }
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
}