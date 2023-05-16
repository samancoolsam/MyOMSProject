package com.cts.sterling.custom.accelerators;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.cts.sterling.custom.accelerators.exception.GenericAPIException;
import com.cts.sterling.custom.accelerators.util.GenericConstantsInterface;
import com.cts.sterling.custom.accelerators.util.XMLUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
  This class provides means to invoke a service or API on an element specified by an XPATH.
  If XPATH repeats in the XML, the service or API will be invoked on each XPATH.

<table>
<tr><td><b>example 1:</b></td></tr>
<tr><td><img src="images/CustomMultiAPI_config_1.jpg"/></td></tr>
<tr><td><img src="images/CustomMultiAPI_config_2.jpg"/></td></tr>
</table>
<table>
<tr><td>API_NAME</td><td>getCommonCodeList</td></tr>
<tr><td>IS_FLOW</td><td>N</td></tr>
<tr><td>MERGE</td><td>Y</td></tr>
<tr><td>XPATH</td><td>Code/CommonCode</td></tr>
</table>
<table>
<tr><td>input XML	:</td><td>output XML	:</td></tr>	
<tr>
<td>
&lt;Code&gt;<br/>
&lt;CommonCode CodeType="TEST" CodeValue="VAL1"/&gt;<br/>
&lt;CommonCode CodeType="TEST" CodeValue="VAL2"/&gt;<br/>
&lt;/Code&gt;
</td>
<td>
&lt;CustomMultiAPI&gt;<br/>&lt;CommonCodeService&gt;<br/>&lt;Input&gt;<br/>&lt;CommonCode CodeType="TEST" CodeValue="VAL1"/&gt;<br/>&lt;/Input&gt;<br/>&lt;Output&gt;<br/>&lt;CommonCodeList&gt;<br/>&lt;CommonCode CodeLongDescription="VAL1" /&gt;<br/>&lt;/CommonCodeList&gt;<br/>&lt;/Output&gt;<br/>&lt;/CommonCodeService&gt;<br/>&lt;CommonCodeService&gt;<br/>&lt;Input&gt;<br/>&lt;CommonCode CodeType="TEST" CodeValue="VAL2"/&gt;<br/>&lt;/Input&gt;<br/>&lt;Output&gt;<br/>&lt;CommonCodeList&gt;<br/>&lt;CommonCode CodeLongDescription="VAL2" /&gt;<br/>&lt;/CommonCodeList&gt;<br/>&lt;/Output&gt;<br/>&lt;/CommonCodeService&gt;<br/>&lt;/CustomMultiAPI&gt;
</td>
</tr>
</table>
<table>
<tr><td><b>example 2:</b></td></tr>
<tr><td><img src="images/CustomMultiAPI_config_1.jpg"/></td></tr>
<tr><td><img src="images/CustomMultiAPI_config_3.jpg"/></td></tr>
</table>
<table>
<tr><td>API_NAME</td><td>getCommonCodeList</td></tr>
<tr><td>IS_FLOW</td><td>N</td></tr>
<tr><td>MERGE</td><td>N</td></tr>
<tr><td>XPATH</td><td>Code/CommonCode</td></tr>
</table>
<table>
<tr><td>input XML	:</td><td>output XML	:</td></tr>	
<tr>
<td>
&lt;Code&gt;<br/>
&lt;CommonCode CodeType="TEST" CodeValue="VAL1"/&gt;<br/>
&lt;CommonCode CodeType="TEST" CodeValue="VAL2"/&gt;<br/>
&lt;/Code&gt;
</td>
<td>
&lt;CustomMultiAPI&gt;<br/>&lt;getCommonCodeList&gt;<br/>&lt;Output&gt;<br/>&lt;CommonCodeList&gt;<br/>&lt;CommonCode CodeLongDescription="VAL1" /&gt;<br/>&lt;/CommonCodeList&gt;<br/>&lt;/Output&gt;<br/>&lt;/getCommonCodeList&gt;<br/>&lt;getCommonCodeList&gt;<br/>&lt;Output&gt;<br/>&lt;CommonCodeList&gt;<br/>&lt;CommonCode CodeLongDescription="VAL2" /&gt;<br/>&lt;/CommonCodeList&gt;<br/>&lt;/Output&gt;<br/>&lt;/getCommonCodeList&gt;<br/>&lt;/CustomMultiAPI&gt;<br/>
</td>
</tr>
</table>					
 */

public class CustomMultiAPI implements GenericConstantsInterface
{		

	private Properties properties;
	
	private List<Node> getNodeInInputDocList;
	
	public void setProperties(Properties properties) throws Exception 
	{        
		this.properties = properties;
	}

	/**
	 * customMultiAPI(). This method work properly below arguments should configured.
	 * API_NAME - specifies the api or service name.
	 * IS_FLOW - specifies whether API_NAME corresponds to service(Y) or api(N).
	 * MERGE - if set to Y input will be merged with output.
	 * @param yfsEnvironment the yfs environment
	 * @param inputDocument the input document
	 * @return the xML splitter merger
	 * @throws Exception 
	 */ 
	public Document customMultiAPI(YFSEnvironment yfsEnvironment, Document inputDocument ) throws Exception
	{
		String merge = properties.getProperty(MERGE);
		String apiName = properties.getProperty(XML_API_NAME);
		Document outputDocument = XMLUtil.createDocument("CustomMultiAPI");        
		YIFApi api = YIFClientFactory.getInstance().getLocalApi();
		getNodeInInputDocList=XMLUtil.getNodesByXpath(inputDocument, properties.getProperty(XPATH));
		for(int i =0;i<getNodeInInputDocList.size();i++)
		{
			Element apiElement = outputDocument.createElement(apiName); 
			Document out = execute(yfsEnvironment,XMLUtil.getDocument((Element)getNodeInInputDocList.get(i), true),api);
			if(merge.equalsIgnoreCase("Y"))
			{
				Element input = outputDocument.createElement("Input");
				input.appendChild(outputDocument.importNode(getNodeInInputDocList.get(i),true));
				apiElement.appendChild(input);		
			}
			Element output = outputDocument.createElement("Output");
			output.appendChild(outputDocument.importNode(out.getDocumentElement(),true));	
			apiElement.appendChild(output);
			outputDocument.getDocumentElement().appendChild(apiElement);
		}
		return outputDocument;
	}

	private Document execute(YFSEnvironment yfsEnvironment, Document document,YIFApi api) throws Exception 
	{
		if(properties.getProperty(IS_FLOW).equalsIgnoreCase("Y"))
		{
			return callService(yfsEnvironment,document,api);
		}
		else
		{
			return callAPI(yfsEnvironment,document,api);
		}
	}


	private Document callService(YFSEnvironment yfsEnvironment, Document inputDocument, YIFApi yifAPI) throws RemoteException, GenericAPIException 
	{
		return yifAPI.executeFlow( yfsEnvironment, this.properties.getProperty(XML_API_NAME), inputDocument );		
	}

	private Document callAPI(YFSEnvironment yfsEnvironment, Document inputDocument, YIFApi yifAPI) throws RemoteException, GenericAPIException 
	{
		return yifAPI.invoke( yfsEnvironment, this.properties.getProperty(XML_API_NAME), inputDocument );		
	}

}