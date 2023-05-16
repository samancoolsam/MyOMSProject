package com.academy.ecommerce.sterling.los;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class extends YCPBaseAgent class, which is non task based Agent.
 * This class provides a generic implementation of getJobs and executeJob of YCPBaseAgent class there by
 * reducing the development time.
 * <table>
	<tr><td><b>example 1:</b></td></tr>
	<tr><td><img src="images/AgentConfigurationUtility_config_1.jpg"/></td></tr>
	<tr><td><img src="images/AgentConfigurationUtility_config_2.jpg"/></td></tr>
	</table>
 */
public class AgentConfigurationUtility extends YCPBaseAgent 
{

	/** 
	 * This field is read from criteria parameter named SERVICE_FOR_EXECUTE_JOB of Agent configuration.
	 */
	public static final String SERVICE_FOR_EXECUTE_JOB = "SERVICE_FOR_EXECUTE_JOB";

	/** 
	 * This field is read from criteria parameter named JOB_ELEMENT_NAME of Agent configuration.
	 */
	public static final String JOB_ELEMENT_NAME = "JOB_ELEMENT_NAME";

	/** 
	 * This field is read from criteria parameter named SERVICE_FOR_GET_JOBS of Agent configuration.
	 */
	public static final String SERVICE_FOR_GET_JOBS = "SERVICE_FOR_GET_JOBS";

	/** The Constant DUMMY_XML_STRING. */
	private static final String DUMMY_XML_STRING = "<Dummy/>";

	/**
	 * Implements getJobs method of YCPBaseAgent.
	 * getJobs() will execute the service specified by SERVICE_FOR_GET_JOBS parameter from the criteria parameters of agent.
	 * Each element in the output XML of the service, with name specified by JOB_ELEMENT_NAME parameter from the criteria 
	 * parameters, will be converted to a Document object and added to an ArraylList<Document>. This array list will be 
	 * output by method. The last message processed will be available in path MessageXml/LastMessage. In the first run of getJobs
	 * LastMessage element will not be present.   
	 * @param env
	 * @param inDoc input Document to getJobs()
	 * @return List- returns the list of jobs fetched by getJobs()
	 * @throws Exception 
	 */
	public List<Document> getJobs(final YFSEnvironment env, final Document inDoc,	final Document lastMessage) throws Exception 
	{
		List<Document> listOfJobs = new ArrayList<Document>();
		Element messageXmlEle = inDoc.getDocumentElement();
		String strServiceName = messageXmlEle.getAttribute(SERVICE_FOR_GET_JOBS);
		String strIdentifier = messageXmlEle.getAttribute(JOB_ELEMENT_NAME);
		String strExecuteJobService = messageXmlEle.getAttribute(SERVICE_FOR_EXECUTE_JOB);
		if (strIdentifier == null || strIdentifier.equals("")) 
		{
			throw new IllegalArgumentException(
			"AgentConfigurationUtility :: Xpath property : JOB_ELEMENT_NAME configured cannot be empty ");
		}
		if (strExecuteJobService == null || strExecuteJobService.equals("")) 
		{
			throw new IllegalArgumentException(
			"AgentConfigurationUtility :: Xpath property : SERVICE_FOR_EXECUTE_JOB configured cannot be empty ");
		}
		if (strServiceName == null || strServiceName.equals("")) 
		{
			Document dummyDoc = XMLUtil.getDocument(DUMMY_XML_STRING);
			dummyDoc.getDocumentElement().setAttribute(SERVICE_FOR_EXECUTE_JOB,strExecuteJobService);
			listOfJobs.add(dummyDoc);
			return listOfJobs;
		}
		YIFApi api = YIFClientFactory.getInstance().getLocalApi();
		if(lastMessage!=null)
		{
			Element lastMsg = inDoc.createElement("LastMessage");
			lastMsg.appendChild(inDoc.importNode(lastMessage.getDocumentElement(), true));
			inDoc.getDocumentElement().appendChild(lastMsg);
		}
		Document outDoc = api.executeFlow(env, strServiceName, inDoc);
		NodeList aJobElementsList = outDoc.getDocumentElement().getElementsByTagName(strIdentifier);
		for (int count = 0; count < aJobElementsList.getLength(); count++) {
			Element tempElement = (Element) aJobElementsList.item(count);
			Document doc = XMLUtil.getDocument(tempElement, true);
			doc.getDocumentElement().setAttribute(SERVICE_FOR_EXECUTE_JOB,strExecuteJobService);
			listOfJobs.add(doc);
		}
		return listOfJobs;

	}
	/**
	 * Implements executeJob method of YCPBaseAgent.
	 * executeJob() will invoke the service specified by SERVICE_FOR_EXECUTE_JOB parameter from the criteria parameters of agent.
	 * @param env
	 * @param inDoc input Document to executeJob()
	 * @throws Exception
	 */
	public void executeJob(YFSEnvironment yfsenv, Document inDoc) throws Exception {

		if (inDoc == null) {
			throw new IllegalArgumentException(
			"AgentConfigurationUtility :: Exception in executeJobmethod input document is null");
		}
		String strExecService = inDoc.getDocumentElement().getAttribute(SERVICE_FOR_EXECUTE_JOB);
		if (strExecService == null || strExecService.equals("")) {
			throw new IllegalArgumentException(
			"AgentConfigurationUtility :: Action property : SERVICE_FOR_EXECUTE_JOB configured cannot be empty ");
		}

		inDoc.getDocumentElement().removeAttribute(SERVICE_FOR_EXECUTE_JOB);
		YIFApi api = YIFClientFactory.getInstance().getLocalApi();
		api.executeFlow(yfsenv, strExecService, inDoc);
	}

}
