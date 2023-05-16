package com.academy.util.xml;

import java.util.Properties;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.academy.util.logger.Logger;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyXMLSplitter {
	
	  public AcademyXMLSplitter()
	    {
	    }
	  public void setProperties(Properties props)
	    {
	        this.props = props;
	    }

	    private Properties props;
	    
	    private static Logger log = Logger.getLogger(AcademyXMLSplitter.class.getName());
	    
	    public Document splitXMLAndExecuteService(YFSEnvironment env, Document inDoc)
	        throws Exception
	    {
	        log.verbose("Entering AcademyXMLSplitter.splitXMLAndExecuteService()");
	        String sChildXMLName = props.getProperty("ChildXML");
	        String sServiceName = props.getProperty("ServiceName");
	        String sAPIName = props.getProperty("APIName");
	        boolean bIsAPI = !YFCObject.isVoid(sAPIName);
	        YFCDocument inYFCDocument = YFCDocument.getDocumentFor(inDoc);
	        YFCElement inYFCElement = inYFCDocument.getDocumentElement();
	        YFCNodeList nl = inYFCElement.getElementsByTagName(sChildXMLName);
	        int iNodeListLength = nl.getLength();
	        for(int i = 0; i < iNodeListLength; i++)
	        {
	            YFCElement eChildElem = (YFCElement)nl.item(i);
	            i--;
	            iNodeListLength--;
	            YFCElement newElem = (YFCElement)inYFCElement.removeChild(eChildElem);
	            YFCDocument childDoc = YFCDocument.parse(eChildElem.getString());
	            if(bIsAPI)
	            {
	                log.verbose((new StringBuilder("Invoking API :")).append(sAPIName).toString());
	                AcademyUtil.invokeAPI(env, sAPIName, childDoc.getDocument());
	            } else
	            {
	                log.verbose((new StringBuilder("Invoking Service :")).append(sServiceName).toString());
	                AcademyUtil.invokeService(env, sServiceName, childDoc.getDocument());
	            }
	        }

	        log.verbose("Exiting AcademyXMLSplitter.splitXMLAndExecuteService()");
	        return inDoc;
	    }

}
