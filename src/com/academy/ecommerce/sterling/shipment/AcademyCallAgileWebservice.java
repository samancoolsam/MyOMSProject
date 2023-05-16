/**
 * 
 */
package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;

import org.w3c.dom.Document;

import com.academy.ecommerce.sterling.util.AcademyServiceUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author sahmed
 *
 */
public class AcademyCallAgileWebservice implements YIFCustomApi {

	/* (non-Javadoc)
	 * @see com.yantra.interop.japi.YIFCustomApi#setProperties(java.util.Properties)
	 */
	
	private Properties props;
	/**
	 * 
	 * Instance of logger
	 * 
	 */
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyCallAgileWebservice.class);
	
	public void setProperties(Properties props) {
		this.props = props;
	}
	
	public Document callAgileInterfaceWebservice(YFSEnvironment env, Document inDoc)
	throws Exception {
		log.beginTimer(" Begining of AcademyCallAgileWebservice->callAgileInterfaceWebservice Api");
		log.verbose("*** invoking callAgileInterfaceWebservice with input doc *** " + XMLUtil.getXMLString(inDoc));
		env.setTxnObject("AgileMethodName", "InvokeAgileWebserviceForShipment");
		String methodName = props.getProperty("AgileMethodName");
		log.verbose("*** AgileMethodName *** " + methodName);
		Document outDoc= AcademyUtil.invokeService(env, "AcademyAgentAddressSOAPCall", inDoc);
		log.endTimer(" End of AcademyCallAgileWebservice->callAgileInterfaceWebservice Api");
		return outDoc;
		
	}

}
