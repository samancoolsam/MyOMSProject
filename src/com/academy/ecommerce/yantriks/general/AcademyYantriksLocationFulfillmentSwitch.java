/**************************************************************************
  * Description	    : This class is invoked as part on On_Success event of ManageOrganization transaction when there is an update to Extn Fulfillment Flags.
 * 
 * 
 * --------------------------------
 * 	Date             Author               
 * --------------------------------
 *  26-April-2021      Cognizant			 	 
 * 
 * -------------------------------------------------------------------------
 **************************************************************************/

package com.academy.ecommerce.yantriks.general;

import static com.academy.util.constants.AcademyConstants.*;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyYantriksLocationFulfillmentSwitch {
	private Properties props;
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyYantriksLocationFulfillmentSwitch.class);

	public Document switchLocationFlag(YFSEnvironment env, Document docInXml) throws Exception {
		log.beginTimer("switchLocationFlag()");
		log.verbose("Input Document to Switch Flag \n" + SCXmlUtil.getString(docInXml));

		String strIsYantriksEnabled = props.getProperty(STR_YANTRIKS_ENABLED);
		Element eleRoot = docInXml.getDocumentElement();
		String strFlagUpdate = eleRoot.getAttribute(ATTR_FLAG_UPDATE);
		log.verbose("Flag Update is for " + strFlagUpdate);
		env.setTxnObject(ATTR_IS_FLAG_UPDATED, STR_YES);

		if (STR_YES.equals(strIsYantriksEnabled)
				&& (!YFCObject.isVoid(strFlagUpdate) && !STR_SHIP_TO_STORE.equals(strFlagUpdate))) {

			log.verbose("Yantriks is enabled for Location Fulfillment Switch");
			Element eleNode = SCXmlUtil.getChildElement(eleRoot, ELE_NODE);
			String strNodeType = eleNode.getAttribute(A_NODE_TYPE);
			Element eleExtn = SCXmlUtil.getChildElement(eleRoot, ELE_EXTN);

			String isEnabled = STR_NO;
			if (V_FULFILLMENT_TYPE_SFS.equals(strFlagUpdate)) {
				isEnabled = eleExtn.getAttribute(ATTR_EXTN_SFS_ENABLED);
			} else if (V_FULFILLMENT_TYPE_BOPIS.equals(strFlagUpdate)) {
				isEnabled = eleExtn.getAttribute(ATTR_EXTN_IS_BOPIS_ENABLED);
			} else if (V_FULFILLMENT_TYPE_DSV.equals(strFlagUpdate)) {
				isEnabled = eleExtn.getAttribute(ATTR_EXTN_DSV_ENABLED);
			}

			Document docToPublish = SCXmlUtil.createDocument(ELE_LOC_FULFILLMENT);
			Element eleRootToPublish = docToPublish.getDocumentElement();
			eleRootToPublish.setAttribute(SHIP_NODE, eleRoot.getAttribute(ORGANIZATION_CODE));
			eleRootToPublish.setAttribute(ATTR_FLAG_UPDATE, strFlagUpdate);
			eleRootToPublish.setAttribute(A_NODE_TYPE, strNodeType);
			eleRootToPublish.setAttribute(ATTR_ENABLED, isEnabled);
			//Start OMNI-51797 :: STS 2.0 OMS -> Yantriks SFS messages
			Document docToPublishSTSFS = SCXmlUtil.createDocument(ELE_LOC_FULFILLMENT);
			Element eleRootToPublishSTSFS = docToPublishSTSFS.getDocumentElement();
			//End OMNI-51797:: STS 2.0 OMS -> Yantriks SFS messages
			if (V_FULFILLMENT_TYPE_SFS.equals(strFlagUpdate)) {
				eleRootToPublish.setAttribute(ATTR_FULFILLMENT_TYPE, V_FULFILLMENT_TYPE_SHIP);
				//Start OMNI-51797 :: STS 2.0 OMS -> Yantriks SFS messages
				eleRootToPublishSTSFS.setAttribute(SHIP_NODE, eleRoot.getAttribute(ORGANIZATION_CODE));
				eleRootToPublishSTSFS.setAttribute(ATTR_FLAG_UPDATE, strFlagUpdate);
				eleRootToPublishSTSFS.setAttribute(A_NODE_TYPE, strNodeType);
				eleRootToPublishSTSFS.setAttribute(ATTR_ENABLED, isEnabled);
				eleRootToPublishSTSFS.setAttribute(ATTR_FULFILLMENT_TYPE, V_FULFILLMENT_TYPE_STFS);
				log.verbose("Publish This XML for STSFS\n" + SCXmlUtil.getString(eleRootToPublishSTSFS));
				publishMessageToQ(env, docToPublishSTSFS);
				//End OMNI-51797 :: STS 2.0 OMS -> Yantriks SFS messages
			} else if (V_FULFILLMENT_TYPE_BOPIS.equals(strFlagUpdate)) {
				eleRootToPublish.setAttribute(ATTR_FULFILLMENT_TYPE, V_FULFILLMENT_TYPE_PICK);
			} else if (V_FULFILLMENT_TYPE_DSV.equals(strFlagUpdate)) {
				eleRootToPublish.setAttribute(ATTR_FULFILLMENT_TYPE, V_FULFILLMENT_TYPE_SHIP);
				eleRootToPublish.setAttribute(A_NODE_TYPE, V_FULFILLMENT_TYPE_DSV);
			}
			log.verbose("Publish This XML \n" + SCXmlUtil.getString(docToPublish));
			publishMessageToQ(env, docToPublish);
		} else {
			log.verbose("Yantriks Not Turned ON or Update is for STS. STS Updates are ignored for Yantriks");
		}
		log.endTimer("switchLocationFlag()");

		return docInXml;
	}

	private void publishMessageToQ(YFSEnvironment env, Document docInput) throws Exception {
		// Invoke Service to insert record in Q
		AcademyUtil.invokeService(env, SERVICE_YANTRIKS_KAFKA_DELTA_UPDATE, docInput);
	}

	public void setProperties(Properties props) {
		this.props = props;
	}
}
