package com.academy.ecommerce.sterling.cancel;

import java.util.ArrayList;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyPicktEmailTemplate {

	//Define properties to fetch argument value from service configuration
		private Properties	props;
		public void setProperties(Properties props) throws Exception
		{
			this.props = props;
		}
		private static YFCLogCategory log = YFCLogCategory
				.instance(AcademyPicktEmailTemplate.class);
		
		public Document processPickEmailTemplate(YFSEnvironment env, Document inXML) throws Exception{
			
			log.beginTimer(" Begining of processPickEmailTemplate -> AcademyPicktEmailTemplate");
			
			String strModifyProgId=null;
			String strReasonCode=null;
			String strCodeType=null;
			Document outputCommomCodeList=null;
			Element eleCommonCode=null;
			NodeList nleleCommonCode=null;
			
			Element inEle=inXML.getDocumentElement();
			Element eleOrderAudit=(Element) inXML.getElementsByTagName("OrderAudit").item(0);
			ArrayList<String> alProgIds=new ArrayList<String>();
			String strProgIdsArgm=props.getProperty("PROG_IDS");
			
			String strProgIdArry[] =strProgIdsArgm.split(",");
			for(int i=0;i<strProgIdArry.length;i++) {
				alProgIds.add(strProgIdArry[i]);	
			}
			if (eleOrderAudit != null && eleOrderAudit.hasAttributes()) {
				strModifyProgId=eleOrderAudit.getAttribute("Modifyprogid");
				log.verbose("ModifyProgID From Event:"+strModifyProgId);
			}
			if(alProgIds.contains(strModifyProgId)) {
				inEle.setAttribute("CustomerCancellation", "False");
				return inXML;		
			}
			if (eleOrderAudit != null && eleOrderAudit.hasAttributes()) {
				strReasonCode=eleOrderAudit.getAttribute("ReasonCode");
			    log.verbose("Reason Code From Event:"+strReasonCode);
			}
		
			// OMNI-4017, 5888, 5885 BOPIS: Cancel Email consolidation at Order level for cancellations - START
			String strIsBOPISCancelFromWebStore = (String)env.getTxnObject(AcademyConstants.ATTR_IS_WEB_STORE_FLAG);
			if(!YFCObject.isVoid(strIsBOPISCancelFromWebStore) && (strIsBOPISCancelFromWebStore.equals(AcademyConstants.ATTR_Y)))
			{
				log.verbose("Txn Object:"+strIsBOPISCancelFromWebStore);
				 inEle.setAttribute(AcademyConstants.STR_SKIP_EMAIL, AcademyConstants.STR_TRUE);
				 
			}
			// OMNI-4017, 5888, 5885 BOPIS: Cancel Email consolidation at Order level for cancellations - END
			
			if(!(StringUtil.isEmpty(strReasonCode))) {
			
			Document inputTocommonCodeList=XMLUtil.createDocument("CommonCode");
			inputTocommonCodeList.getDocumentElement().setAttribute("CodeValue", strReasonCode);
			inputTocommonCodeList.getDocumentElement().setAttribute("CodeShortDescription", strReasonCode);
			outputCommomCodeList=AcademyUtil.invokeAPI(env, "getCommonCodeList", inputTocommonCodeList);
			nleleCommonCode = outputCommomCodeList.getDocumentElement().getElementsByTagName("CommonCode");
			if(nleleCommonCode.getLength() > 0){
			for(int i=0;i<nleleCommonCode.getLength();i++){
				eleCommonCode=(Element) nleleCommonCode.item(i);
			if (eleCommonCode != null && eleCommonCode.hasAttributes()) {
				strCodeType=eleCommonCode.getAttribute("CodeType");
				log.verbose("Code Type From Commom Code:"+strCodeType);
				
				if (strCodeType.equalsIgnoreCase("Academy")) {
					inEle.setAttribute("CustomerCancellation", "False");
					break;
				}
				else if(strCodeType.equalsIgnoreCase("Customer")){
					inEle.setAttribute("CustomerCancellation", "True");
					break;
				}
				else {
					inEle.setAttribute("CustomerCancellation", "False");
				}
			}
			else {
				inEle.setAttribute("CustomerCancellation", "False");
			}
			}
			}
			else {
				inEle.setAttribute("CustomerCancellation", "False");
			}
			}
			else {
				inEle.setAttribute("CustomerCancellation", "False");
			}
			
			
			
			log.endTimer(" Ending of processPickEmailTemplate -> AcademyPicktEmailTemplate");
			return inXML;
			
		}
}
