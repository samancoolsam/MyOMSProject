package com.academy.util.common;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.interop.client.InteropEnvStub;
import com.yantra.interop.client.InteropLocalClient;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.services.webservices.rpc.ejb.YIFWebServiceException;

public class ResourceUtil {
	private YIFApi api;
	private YFSEnvironment env ;
	private static Properties resources = new Properties();

	private static ArrayList msgResBundles = new ArrayList();

	private static ArrayList msgResBundleNames = new ArrayList();

	private static int numMsgResBundlesLoaded = 0;

	private final static String DESC_NOT_FOUND = "Error Description Not Found";
	
	private static boolean hasDummyInvoiceSeqNo = false;

	// Currently, we see the need for only PROD and DEV to be two modes
	// in which Yantra would be run. Therefore, the flag is a boolean.
	// If mode modes develop in the future (like TEST etc), then the key would
	// have to be redefined.
	public final static String YANTRA_RUNTIME_MODE = "yantra.implementation.runtime.mode";

	public static boolean IS_PRODUCTION_MODE = true;

	static {
		loadDefaultResources();
	}
	private static void loadDefaultResources() {
		msgResBundleNames.clear();
		msgResBundles.clear();
		resources.clear();
		//		server level configuration files
		loadResourceFile("/resources/yfs.properties");
		loadResourceFile("/resources/yifclient.properties");
		loadResourceFile("/resources/extn/AcademyServer.properties");
		//loadResourceFile("/resources/extn/BBYServerError.properties");
		
	}
	private static void loadResourceFile(String filename) {
		InputStream is = null;
		try {

			/*
			 * YRCPlatformUI.trace("Loading Properties from: " + filename + ": " +
			 * ResourceUtil.class.getResource(filename));
			 */
			is = ResourceUtil.class.getResourceAsStream(filename);
			resources.load(is);
			is.close();
		} catch (Exception e) {
			
		}
		
	}
	public static String resolveMsgCode(String code) {
		
		// TODO HARI: Change This
		
		return code;
	}

	

	public static String resolveMsgCode(String code, Object[] args) {
		
		// TODO HARI: Change This
		
		return code;
	}
	
	/**
	 * Get resource by name
	 * 
	 * @param name
	 *            the resource name
	 */
	public static String get(String name) {

		String retVal = resources.getProperty(name);
		if (retVal != null)
			retVal = retVal.trim();

		return retVal;
	}

	/**
	 * Get resource or the default value
	 * 
	 * @param name
	 *            the resource name.
	 * @param defaultValue
	 *            the default value if the resource does not exist.
	 */
	public static String get(String name, String defaultValue) {
		String retval = StringUtil.nonNull(resources.getProperty(name));
		if (retval.equals(""))
			retval = defaultValue;
		return retval.trim();
	}

   protected void setUp () throws Exception  {
    	
    	api = YIFClientFactory.getInstance().getApi();
    	Document environmentDoc = XMLUtil.getDocumentFromString("<Environment userId=\"yantra\" progId=\"yantra\"/>");
    	env = api.createEnvironment(environmentDoc);
    	getCommonCodeList(env);
        }

	public static Map getCommonCodeList(YFSEnvironment env) {
		Map mp=new HashMap<String, Integer>();
		try {
			Document getCommonCodeListInputXML = XMLUtil.createDocument("CommonCode");
			getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeType", "PRO_NUMBER");
			Document outXML=AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML);
			if(outXML!=null){
				NodeList listOfCommCodes=outXML.getElementsByTagName("CommonCode");
				int len=listOfCommCodes.getLength();
				if(len>0){
					for(int i=0;i<len;i++){
						Element commCodeElem=(Element) listOfCommCodes.item(i);
						mp.put(commCodeElem.getAttribute("CodeValue"), commCodeElem.getAttribute("CodeShortDescription"));
				}
			}
		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mp;
	}
	
	public static boolean getHasDummyInvoiceNoSeq(){
		return hasDummyInvoiceSeqNo;
	}
	
	public static void setHasDummyInvoiceNoSeq(boolean hasDummyRecord){
		hasDummyInvoiceSeqNo = hasDummyRecord;
	}
}
