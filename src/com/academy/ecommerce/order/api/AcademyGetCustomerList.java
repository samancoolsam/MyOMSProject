package com.academy.ecommerce.order.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyGetCustomerList implements YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyGetCustomerList.class);
	
	private Document outDoc=null;

	/**
	 *  Date: 12/17/2010  Author: Manju
	 *   
	 *  Academy is facing deadlock issues on getCustomerList API periodically. Support
	 *  suggested to break transaction boundary for this API. Hence this custom API.
	 *  
	 *  Logic: 
	 *  1. Create a new environment
	 *  2. Call getCustomerList API using new environemnt
	 *  3. Release environment
	 *  
	 * @param env
	 *            Envrionment variable passed to the API
	 * @param inDoc
	 *            Input document of
	 * 
	 * @return docOutput Manipulated output document for further processing of
	 *         createOrder transaction
	 * 
	 * @throws Exception
	 *             Generic Exception
	 */
	public Document getCustomerList(YFSEnvironment env,
			Document inDoc) throws Exception {
		
		YIFApi yifApi;
		YFSEnvironment envNew;
		Document docGetCustomerListOutput;

		try {

			yifApi = YIFClientFactory.getInstance().getLocalApi();
			Document docEnv = XMLUtil.createDocument(AcademyConstants.ELE_ENV);
			docEnv.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_USR_ID, env.getUserId());
			docEnv.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_PROG_ID, env.getProgId());
			envNew = yifApi.createEnvironment(docEnv);
			
			Document docGetCustomerDetailsTemplate = XMLUtil.createDocument(AcademyConstants.ELE_CUSTOMER_LIST);
			Element eleCustomer = docGetCustomerDetailsTemplate.createElement(AcademyConstants.ELE_CUSTOMER);
			docGetCustomerDetailsTemplate.getDocumentElement().appendChild(
			eleCustomer);
			envNew.setApiTemplate(AcademyConstants.API_GET_CUST_LIST,
			docGetCustomerDetailsTemplate);
			
			docGetCustomerListOutput = AcademyUtil.invokeAPI(envNew,
					AcademyConstants.API_GET_CUST_LIST,	inDoc);
			
			envNew.clearApiTemplate(AcademyConstants.API_GET_CUST_LIST);
			
			yifApi.releaseEnvironment(envNew);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		
				
		return docGetCustomerListOutput;
	}
	
	public static void main(String[] args) {
		try{
		YIFApi yifApi = YIFClientFactory.getInstance().getLocalApi();
		Document test = null;
		YFSEnvironment env;
		Document docEnv = XMLUtil.createDocument(AcademyConstants.ELE_ENV);
		docEnv.getDocumentElement().setAttribute(
				AcademyConstants.ATTR_USR_ID, "test");
		docEnv.getDocumentElement().setAttribute(
				AcademyConstants.ATTR_PROG_ID, "manju");
		env = yifApi.createEnvironment(docEnv);
		new AcademyGetCustomerList().getCustomerList(env, test);
		} catch(Exception e) {
			
		}
	}
	
	public void setProperties(Properties arg0) throws Exception {

	}

}
