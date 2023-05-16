package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySTSSetRePrinterID implements YIFCustomApi {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSSetRePrinterID.class);

	Properties props;

	@Override
	public void setProperties(Properties props) throws Exception {
		this.props = props;

	}

	public Document setSTSRePrinterID(YFSEnvironment env, Document docInp) throws Exception {

		log.verbose("Input - setSTSRePrinterID() :: " + XMLUtil.getXMLString(docInp));

		String strPrintPackId = docInp.getDocumentElement()
				.getAttribute(AcademyConstants.ATTR_SHIPMENT_REPRINT_PACK_STN);

		String strRePrinterReference = props.getProperty(AcademyConstants.STR_REPRINTER_REFERENCE);

		Document docCommonCodeOut = getCommonCodeList(env, strPrintPackId);

		if (null != docCommonCodeOut) {

			NodeList nlCommonCode = docCommonCodeOut.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);

			for (int iCC = 0; iCC < nlCommonCode.getLength(); iCC++) {

				Element eleCommonCode = (Element) nlCommonCode.item(iCC);

				String strCodeValue = eleCommonCode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
				String strCodeShortDesc = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
				
				if (!StringUtil.isEmpty(strCodeValue)) {

					log.verbose("Code Value :: " + strCodeValue);

					if (strCodeValue.contains(AcademyConstants.STR_STS_SHIPPING_LABEL_PRINTER_NAME)
							&& strCodeValue.contains(strRePrinterReference)) {
						env.setTxnObject(AcademyConstants.STR_STS_SHIPPING_LABEL_REPRINTER_ID, strCodeShortDesc);
					} else if (strCodeValue.contains(AcademyConstants.STR_ORMD_LABEL_PRINTER_NAME)
							&& strCodeValue.contains(strRePrinterReference)) {
						env.setTxnObject(AcademyConstants.STR_STS_ORMD_LABEL_REPRINTER_ID, strCodeValue);
					}
				}
			}
		}

		return docInp;
	}

	private Document getCommonCodeList(YFSEnvironment env, String strCodeType) {

		Document getCommonCodeListInDoc = null;
		Document getCommonCodeListOutDoc = null;

		try {
			getCommonCodeListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, strCodeType);
			getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.HUB_CODE);

			log.verbose("getCommonCodeList input Doc: " + XMLUtil.getXMLString(getCommonCodeListInDoc));

			env.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST,
					AcademyConstants.STR_TEMPLATEFILE_GET_COMMON_CODE_LIST_PACK_STATION_DEVICE);

			getCommonCodeListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST,
					getCommonCodeListInDoc);

			env.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);

			log.verbose("getCommonCodeList API Output Doc: " + XMLUtil.getXMLString(getCommonCodeListOutDoc));

		} catch (Exception e) {
			log.error("Exception - getCommonCodeList()::" + e);
		}

		return getCommonCodeListOutDoc;
	}

}
