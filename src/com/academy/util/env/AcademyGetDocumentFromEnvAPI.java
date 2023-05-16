package com.academy.util.env;

import java.util.Properties;

import org.w3c.dom.Document;


import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCNode;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This API will retrieve the object stored in the environment context under a
 * certain key. The key should be configured in the Configurator as an Argument
 * for the API node.
 */
public class AcademyGetDocumentFromEnvAPI implements YIFCustomApi {

    /**
     * Instance to store the properties configured for the condition in Configurator.
     */
    private Properties props;
    
    /*
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyGetDocumentFromEnvAPI.class);
      
    /**
     * Creates a new instance of AcademyGetDocumentFromEnvAPI.
     */
    public AcademyGetDocumentFromEnvAPI() {
    }
    
    /**
     * Stores properties configured in configurator. The property expected by this API
     * is: academy.helpdesk.getdoc.env.key
     * @param props Properties configured in Configurator.
     */
    public void setProperties(Properties props) {
        this.props = props;
    }

    /**
     * Retrieves the object stored in the environment context under the key
     * specified in the property: academy.helpdesk.getdoc.env.key. The object should
     * be an instance of either org.w3c.dom.Document or com.yantra.yfc.dom.YFCDocument.
     * @param env Yantra Environment Context.
     * @param inDoc Input Document. Not used by this API.
     * @return Returns the document stored in the environment.
     */    
    public Document getDocumentFromEnv(YFSEnvironment env, Document inDoc) 
        throws Exception {
        
    	log.verbose("Entering AcademyGetDocumentFromEnvAPI.getDocumentFromEnv()");
        if (log.isDebugEnabled())
           log.debug("Got Document: " + XMLUtil.getXMLString(inDoc));
        String docid = props.getProperty(AcademyConstants.KEY_GETDOC_ENV_KEY);
        if (log.isDebugEnabled())
        	log.debug("Looking in Env for key [" + docid + "]");
        
        Object obj = AcademyUtil.getContextObject(env, docid);

        Document doc = null;
        if (obj instanceof Document) {
            doc = (Document) obj;
        } else if (obj instanceof YFCDocument) {
            doc = ((YFCDocument) obj).getDocument();
        } else {
            YFCDocument tmpDoc = YFCDocument.parse("<" + AcademyConstants.TAG_INTERNAL_ENVELOPE + "/>");
            YFCNode tnode = tmpDoc.createTextNode(obj.toString());
            tmpDoc.getDocumentElement().appendChild(tnode);
            doc = tmpDoc.getDocument();            
        }
        
        if (log.isDebugEnabled()) {
            if (doc != null) {
            	log.debug("Document Found in Env for Key [" + docid
                        + "] Document [" + XMLUtil.getXMLString(doc) + "]");
            } else {
            	log.debug("No document found in Env for Key [" + docid
                        + "] Values [" + obj + "]");
            }
        }
        
        log.verbose("Exiting AcademyGetDocumentFromEnvAPI.getDocumentFromEnv()");
        return doc;
    }

}

