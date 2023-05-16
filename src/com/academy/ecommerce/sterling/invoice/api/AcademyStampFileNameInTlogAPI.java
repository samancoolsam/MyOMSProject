package com.academy.ecommerce.sterling.invoice.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;


/**STL-1479 : This class is used to stamp the fileName attribute in TLogs.
 * @author ndey
 *
 */
public class AcademyStampFileNameInTlogAPI implements  YIFCustomApi {
	
	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyStampFileNameInTlogAPI.class);
	
	
	/**This method is used to stamp the fileName attribute in TLogs.
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document stampFileNameInTlog(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer(" begin of stampFileNameInTlog");
		log.verbose("\n******* input doc ********" + XMLUtil.getXMLString(inDoc));
		
		Element eleInvoiceDetail = inDoc.getDocumentElement();
		Element eleinvoiceHeader = (Element) inDoc.getElementsByTagName(AcademyConstants.ELE_INVOICE_HDR).item(0);
		
		String strOrdInvoiceKey = eleinvoiceHeader.getAttribute(AcademyConstants.ATTR_ORDER_INVOICE_KEY);
		String strTerminalNo = eleinvoiceHeader.getAttribute(AcademyConstants.TLOG_TERMINALNO);
		String strTransactionNo = eleinvoiceHeader.getAttribute(AcademyConstants.TLOG_TRANSACTIONNO);
		String strChangeOnOfFlag = YFSSystem.getProperty("academy.tlog.change.file.name");
					
		String strFileName = null;
		
		if(!YFCObject.isVoid(strOrdInvoiceKey) && !YFCObject.isVoid(strTerminalNo) && !YFCObject.isVoid(strTransactionNo))			
		{
			if(!StringUtil.isEmpty(strChangeOnOfFlag) && "Y".equalsIgnoreCase(strChangeOnOfFlag))
			{
			//changes for OMNI-66399 Start 
			//strFileName = strOrdInvoiceKey.substring(0,14).concat(AcademyConstants.STR_HYPHEN)
			strFileName = strOrdInvoiceKey.concat(AcademyConstants.STR_HYPHEN)
			.concat(strTerminalNo)
			.concat(strTransactionNo)
			.concat(AcademyConstants.ATTR_FILE_FORMAT_XML);
			//Changes for OMNI-66399 End
			eleInvoiceDetail.setAttribute(AcademyConstants.ATTR_FILE_NAME, strFileName);
			}
			else
			{
				strFileName = strOrdInvoiceKey.substring(0,14).concat(AcademyConstants.STR_HYPHEN)
					.concat(strTerminalNo)
					.concat(strTransactionNo)
					.concat(AcademyConstants.ATTR_FILE_FORMAT_XML);
					eleInvoiceDetail.setAttribute(AcademyConstants.ATTR_FILE_NAME, strFileName);
			}
		}
		return inDoc;
	}


	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}