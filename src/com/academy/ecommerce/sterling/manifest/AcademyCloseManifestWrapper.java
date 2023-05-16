package com.academy.ecommerce.sterling.manifest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.cts.sterling.custom.accelerators.util.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * Acts as a wrapper to closeManifest API as exception handler needs to be implemented.
 * An email will be trigerred to the admin users in case of an exception. 
 * 
 * @author gTejaswini
 * 
 */

public class AcademyCloseManifestWrapper implements YIFCustomApi {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyCloseManifestWrapper.class);

	public Document closeManifest(YFSEnvironment env, Document inXML) throws Exception {

		log.verbose(" Begining of AcademySFSCloseManifestAsyncService Api");
		Document docOutputcloseManifest = null;

		try {
			docOutputcloseManifest = AcademyUtil.invokeAPI(env, "closeManifest", inXML);
		} catch (Exception ex) {
			// Get the stack trace.
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			stampShipNode(env, inXML);
			AcademyUtil.invokeService(env, AcademyConstants.RAISE_ALERT_ON_CLOSE_MANIFEST_FAILURE, inXML);
			log.info("Close Manifest Failed: InXML: " + XMLUtil.getString(inXML));
			log.info("Exception Stack Trace: " + exceptionAsString);
			throw new Exception(ex.getMessage());
		}
		log.verbose("End of AcademySFSCloseManifestAsyncService Api");
		return docOutputcloseManifest;
	}
	
	/**
	 * Stamps ship node in the output if it is empty in the input.
	 * 
	 * @param env
	 *            YFSEnvironment
	 * @param inXML
	 *            Document holds manifest data.
	 * @return void
	 */
	
	private void stampShipNode(YFSEnvironment env, Document inXML) throws Exception {
		
		String shipNode = "";
		Element manifestElem = inXML.getDocumentElement();		
		if (manifestElem.hasAttribute(AcademyConstants.SHIP_NODE)) {
			shipNode = manifestElem.getAttribute(AcademyConstants.SHIP_NODE);
			log.verbose("stampShipNode()_shipNodeInInput: " + shipNode);
		} 
		
		if (null == shipNode || shipNode.equalsIgnoreCase("")) {
			// Stamp Ship Node
			Document getManifestListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_MANIFEST_LIST, inXML);
			log.verbose(XMLUtil.getString(getManifestListOutDoc));
			Element manifestsElem = getManifestListOutDoc.getDocumentElement();
			if (manifestsElem.hasChildNodes()) {
				Element manifestOutElem = (Element) manifestsElem.getElementsByTagName(AcademyConstants.ELEM_MANIFEST).item(0);
				String shipNodeFromOutput = "";
				if (manifestOutElem.hasAttribute(AcademyConstants.SHIP_NODE)) {
					shipNodeFromOutput = manifestOutElem.getAttribute(AcademyConstants.SHIP_NODE);
					if (shipNodeFromOutput != null && !shipNodeFromOutput.equalsIgnoreCase("")) {
						manifestElem.setAttribute(AcademyConstants.SHIP_NODE, shipNodeFromOutput);
						log.verbose("stampShipNode()_shipNodeFromOutput: " + shipNodeFromOutput);
					}
				}
			}
		}
	}
	
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}
}
