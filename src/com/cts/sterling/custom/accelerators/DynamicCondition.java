package com.cts.sterling.custom.accelerators;

import static javax.xml.xpath.XPathConstants.BOOLEAN;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * The Class DynamicCondition implements YCPDynamicConditionEx.
 */
public class DynamicCondition implements YCPDynamicConditionEx 
{

	private static YFCLogCategory log = YFCLogCategory.instance(DynamicCondition.class);
	private Map props;

	/**
	 * evaluateCondition() will used to evaluate XPATH in a Document object. for this method to operate properly below agruments should be set
	 * <table>
	 * <tr><td>Argument</td><td>Description</td></tr>
	 * <tr><td>NO_OF_PROPERTIES</td><td>provide the no of xpaths</td></tr>
	 * <tr><td>XPATH_EXPRESSION_&lt;i&gt;</td><td>ith part of XPATH expression; these XPATH <br/>
	 * expressions will be combined to form one complete expression.
	 * <br/> Make sure that expression is a valid one.</td></tr>
	 * </table>
	 * <table>
	 * <tr><td><b>example 1:</b></td></tr>
	 * <tr><td><img src="images/DynamicCondition_config_1.jpg"/></td></tr>
	 * </table>
	 */
	public boolean evaluateCondition(final YFSEnvironment env,final String name, final Map mapData, final Document doc) 
	{
		return evaluateCondition(name, doc);
	}

	private boolean evaluateCondition(final String serviceName,final Document inXML) throws YFSException 
	{

		String sourceXPath = "";
		String noOfPropertiesConfigured = (String) props.get("NO_OF_PROPERTIES");

		if(!isNumber(noOfPropertiesConfigured)){
			throw new YFSException(serviceName+"Exception in ExpressionEvaluator :: Property - NumberOfProperties configured is not a Number");
		}

		int nProperties = Integer.parseInt(noOfPropertiesConfigured);
		for(int count = 1 ; count <= nProperties ; count++ ){
			sourceXPath = sourceXPath + (String) props.get("XPATH_EXPRESSION_"+count);
		}

		if ("".equalsIgnoreCase(sourceXPath)) {
			throw new YFSException(serviceName
					+ ": Must set 'XPathTest' Condition Property on condition.");
		}
		if (inXML == null) {
			throw new YFSException(serviceName
					+ ": Input Document to the expression evaluator is null");
		}

		Boolean result;

		try 
		{
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xpath.compile(sourceXPath);
			result = (Boolean) expr.evaluate(inXML, BOOLEAN);
		} 
		catch (XPathExpressionException e) 
		{
			log.error(e.getMessage(), e);

			YFSException yfsE = new YFSException(serviceName
					+ ": Condition threw exception processing [" + sourceXPath
					+ "]");
			yfsE.setStackTrace(e.getStackTrace());
			throw yfsE;
		}
		return result;
	}

	public void setProperties(Map arg0) 
	{
		props = arg0;
	}

	private boolean isNumber(String noOfAction)
	{
		for (int count = 0; count < noOfAction.length(); count++) 
		{
			if (!Character.isDigit(noOfAction.charAt(count)))
				return false;
		}
		return true;
	}
}
