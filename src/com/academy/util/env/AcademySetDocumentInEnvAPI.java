package com.academy.util.env;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import java.util.Properties;
import org.w3c.dom.Document;

/**
 * This API will store the input document in the environment context under a
 * certain key. The key should be configured in the Configurator as an Argument
 * for the API node.
 * @author ssankar
 */
public class AcademySetDocumentInEnvAPI implements YIFCustomApi {
    
    /**
     * Instance to store the properties configured for the condition in Configurator.
     */
    private Properties props;
    
    /*
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademySetDocumentInEnvAPI.class);
    
    
    /**
     * Creates a new instance of BLSSetDocumentInEnvAPI 
     */
    public AcademySetDocumentInEnvAPI() {
    }
    
    /**
     * Stores properties configured in configurator. The property expected by this API
     * is: bls.helpdesk.setdoc.env.key
     * @param props Properties configured in Configurator.
     */
    public void setProperties(Properties props) {
        this.props = props;	
    }

    /**
     * Stores the input document in the environment context under the key
     * configured in property: bls.helpdesk.setdoc.env.key. A copy of the
     * document will be created and stored so that changes to the input document
     * by downstream components do not affect the copy stored in the environment.
     * @return Returns the document stored in the environment.
     * @param env Yantra Environment Context.
     * @param inDoc Input Document to be stored.
     * @throws java.lang.Exception Thrown if unable to clone the input document.
     */    
    public Document setDocumentInEnv(YFSEnvironment env, Document inDoc) throws Exception {
    	log.verbose("Entering BLSSetDocumentInEnvAPI.setDocumentInEnv()");
        
        String docid = props.getProperty(AcademyConstants.KEY_SETDOC_ENV_KEY);
                
        if (log.isDebugEnabled())
        	log.debug("Setting in Env Key [ " + docid + "] Document [" + XMLUtil.getXMLString(inDoc) + "]");

        //1. We got to make a copy of the doc and then store in env.
        //This is because, we are stoirng the env as given to us now
        //If we store the reference, further modifications of the doc
        //in the SDf would affect the reference in env too.
        
        Document cloneDoc = XMLUtil.cloneDocument(inDoc);
        
        //We'll simply set the value and return the same document as output.
        //If some other object was in the env for the same key,
        //We'll not return that, since the document that has to be saved is the 
        //latest and needs to be carried forward to the next component in the SDF.
        //If the obj that is in the env currently is needed, then it needs to be 
        //explicitly fetched.
        
        AcademyUtil.setContextObject(env, docid, cloneDoc);
        
        log.verbose("Exiting BLSSetDocumentInEnvAPI.setDocumentInEnv()");
        return cloneDoc;
    }

}

