package com.academy.ecommerce.sterling.manifest;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyStampSTOPrintDocument implements YIFCustomApi {
	private Properties props;
	private Element eleContainer = null;

	public void setProperties(Properties props) {
		this.props = props;
	}

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyStampSTOPrintDocument.class);

	public Document stampFileLocation(YFSEnvironment env, Document inDoc)
			throws Exception {
		log.beginTimer(" Begining of AcademyStampSTOPrintDocument-> stampFileLocation Api");
		log.verbose("Input doc to AcademyStampSTOPrintDocument is " +XMLUtil.getXMLString(inDoc));
		Element inDocElem = inDoc.getDocumentElement();
		eleContainer = (Element) XMLUtil.getNode(inDocElem,"Container");
		if (!YFCObject.isVoid(eleContainer)) 
		{
				eleContainer.setAttribute("PrintDocumentId", "ACAD_CONTAINER_LABEL_STO");
		}

		log.endTimer(" End of AcademyStampSTOPrintDocument-> stampFileLocation Api");
		log.verbose("Output doc of AcademyStampSTOPrintDocument is " +XMLUtil.getXMLString(inDoc));
		return inDoc;

	}

}