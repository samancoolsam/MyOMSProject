package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyGenerateContainerSCM implements YIFCustomApi {

	private Document outDoc = null;
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyGenerateContainerSCM.class);
	public Document generateContainerSCM(YFSEnvironment env, Document inDoc)
			throws Exception {
		log.beginTimer(" Begining of AcademyGenerateContainerSCM ->generateContainerSCM Api");
		if (!YFCObject.isVoid(inDoc.getDocumentElement())) {
			NodeList nListContainer = XMLUtil.getNodeList(inDoc
					.getDocumentElement(), "Container");
			if (!YFCObject.isVoid(nListContainer)) {
				int iLength = nListContainer.getLength();
				Element eContainer = null;
				for (int i = 0; i < iLength; i++) {
					eContainer = (Element) nListContainer.item(i);
					Document docChangeShipmentContainer = XMLUtil
							.createDocument("Container");
					docChangeShipmentContainer
							.getDocumentElement()
							.setAttribute(
									"ShipmentContainerKey",
									eContainer
											.getAttribute("ShipmentContainerKey"));
					docChangeShipmentContainer.getDocumentElement()
							.setAttribute("GenerateContainerScm", AcademyConstants.STR_YES);
					outDoc = AcademyUtil.invokeAPI(env,
							"changeShipmentContainer",
							docChangeShipmentContainer);
				}
			}
		}
		log.endTimer(" End of AcademyGenerateContainerSCM ->generateContainerSCM Api");
		return outDoc;

	}

	public void setProperties(Properties arg0) throws Exception {
	}

}
