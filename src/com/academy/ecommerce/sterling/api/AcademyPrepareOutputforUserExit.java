package com.academy.ecommerce.sterling.api;

import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyPrepareOutputforUserExit implements YIFCustomApi {
	/**
     * Instance to store the properties configured for the condition in Configurator.
     */
    private Properties props;
    
    /*
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyPrepareOutputforUserExit.class);
    public void setProperties(Properties props) {
        this.props = props;
    }
    
    public Document stampCanReceiptBeClosed(YFSEnvironment env, Document inDoc) throws Exception {
    	Document outDoc = XMLUtil.createDocument("Receipt");
    	Element outElem = outDoc.getDocumentElement();
    	boolean bCanReceiptBeClosed = false;
    	log.beginTimer(" Begining of AcademyPrepareOutputforUserExit-> stampCanReceiptBeClosed Api");
    	String sExceptionZones= props.getProperty("ExceptionZone");
		
		StringTokenizer st = new StringTokenizer(sExceptionZones,",");
		
		while (st.hasMoreTokens()&& !bCanReceiptBeClosed){
			String sExceptionZone = st.nextToken();
			Element eTaskList = XMLUtil.getFirstElementByName(inDoc.getDocumentElement(), "InputDocument/TaskList");
    		if (!YFCObject.isVoid(eTaskList)){
    			
    			List nL1 = XMLUtil.getSubNodeList(eTaskList, "Task");	
    			int iLength = nL1.size();
    			
    				for(int j= 0; j < iLength; j++){
					
					Element elemTask = (Element) nL1.get(j);
					
					String sTargetLocationId = elemTask.getAttribute("TargetZoneId");
					if (sTargetLocationId.equals(sExceptionZone)){
						bCanReceiptBeClosed = true;
					}
						}
    		}
		}
		
		Element eInventoryElem = XMLUtil.getFirstElementByName(inDoc.getDocumentElement(), "EnvironmentDocument/Task/Inventory");
		
		Element eReferenceElem = XMLUtil.getFirstElementByName(inDoc.getDocumentElement(), "EnvironmentDocument/Task/TaskReferences");
		if(bCanReceiptBeClosed){
			outElem.setAttribute("CanReceiptBeClosed", AcademyConstants.STR_NO);
		}else
			
			outElem.setAttribute("CanReceiptBeClosed", AcademyConstants.STR_YES);
		if(!YFCObject.isVoid(eInventoryElem)){
			outElem.setAttribute("ReceiptHeaderKey", eInventoryElem.getAttribute("ReceiptHeaderKey"));
		}
	if(!YFCObject.isVoid(eReferenceElem)){
		
		outElem.setAttribute("ReceiptNo", eReferenceElem.getAttribute("ReceiptNo"));
		}
	log.endTimer(" End of AcademyPrepareOutputforUserExit-> stampCanReceiptBeClosed Api");
    	return outDoc;
    }
}
