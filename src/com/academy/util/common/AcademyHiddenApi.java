package com.academy.util.common;

//J2SE Imports
import java.util.Properties;
import org.w3c.dom.Document;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.ycp.core.YCPEntityApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.shared.ycp.YFSContext;
import com.yantra.ycp.core.YCPTemplateManager;

/**
* Calls apis not exposed by Sterling.
*/
public class AcademyHiddenApi
{
	//Instance to store the properties configured for the condition in configurator
	private Properties	props;

	// Stores property:PATH configured in configurator
	public void setProperties(Properties props) throws Exception
	{
		this.props = props;

	}

	/**
	 * Calls apis not exposed by Sterling
	 *
	 * @param env
	 *            Yantra Environment Context.
	 * @param inXML
	 *            Input Document.
	 * @return inXML
	 */
	public Document callHiddenApi(YFSEnvironment env, Document inXML) throws Exception
	{
		String apiName = props.getProperty("API_NAME");
		String localApiName = apiName;

		if(apiName.startsWith("get") && apiName.endsWith("List"))
		{
			localApiName = "list" + apiName.substring(3,apiName.length()-4);
		}
		else
		if(apiName.startsWith("get") && apiName.endsWith("Details"))
		{
			localApiName = apiName.substring(0,apiName.length()-7);
		}

		YFCDocument inDoc = YFCDocument.getDocumentFor(inXML);

    	YCPTemplateManager.getInstance().setEntityTemplate((YFSContext)env, apiName, inDoc, localApiName);


		return YCPEntityApi.getInstance().invoke((YFSContext)env, localApiName, inDoc, (YFCElement)null).getDocument();
	}
}