package com.academy.util.env;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.w3c.dom.Document;

/**
 * This API will merge the input document with the document stored in the
 * environment context under a certain key. The key should be configured in
 * Configurator as an Argument for the API node.
 * @author ssankar
 */
public class AcademyMergeDocumentInEnvWithInputAPI implements YIFCustomApi {
    
    /**
     * Instance to store the properties configured for the condition in Configurator.
     */
    private Properties props;
    
    /*
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyMergeDocumentInEnvWithInputAPI.class);
        
    /** Creates a new instance of AcademyMergeDocumentInEnvWithInputAPI */
    public AcademyMergeDocumentInEnvWithInputAPI() {
    }
    
    /**
     * Stores properties configured in configurator. The property expected by this API
     * is: academy.mergedoc.env.key
     * @param props Properties configured in Configurator.
     */
    public void setProperties(Properties props) {
        this.props = props;
    }

    /**
     * Retrieves the document stored in the environment context under the key
     * configured in property: academy.mergedoc.env.key. Merges this with
     * the Input Document and returns the merged document. Since org.w3c.dom.Document
     * follows the Composition pattern and does not allow an element to belong to
     * more than one Document, a clone of the input and environment documents
     * would be created prior to merging. The merging thus leaves both the input
     * document and the document in the environment intact.The structure of the
     * merged document would be:
     * <AcademyMergedDocument>
     *     <InputDocument>
     *         ...INPUT DOCUMENT...
     *     </InputDocument>
     *     <EnvironmentDocument>
     *         ...DOCUMENT RETRIEVED FROM THE ENVIRONMENT...
     *     </EnvironmentDocument>
     * </AcademyMergedDocument>
     * @return Returns the document stored in the environment merged with the
     * input document.
     * @param env Yantra Environment Context.
     * @param inDoc Input Document to be merged with the document in the environment.
     * @throws java.lang.Exception Thrown if unable to clone the input document
     * or the document retrieved from the environment.
     */    
    public Document mergeDocumentInEnvWithInputAPI(YFSEnvironment env, Document inDoc) throws Exception {
    	log.verbose("Entering AcademyMergeDocumentInEnvWithInputAPI.mergeDocumentInEnvWithInputAPI()");
        
        YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        
        if (log.isDebugEnabled())
        	log.debug("Got Document: " + yfcInDoc.getString());
        
        YFCDocument mergeDoc = YFCDocument.createDocument(AcademyConstants.TAG_MERGE_ROOT_DOC);
        YFCElement rootElement = mergeDoc.getDocumentElement();
        YFCElement wrapperElement = rootElement.createChild(AcademyConstants.TAG_MERGE_INPUT_DOC);
        
        //Insert input document as first child.
        //Now, to import, we have to remove the element from the original Document
        //and then insert into new parent. Since we dont want to disturb the original
        //document, lets operate on its clone.
        YFCDocument cloneInDoc = XMLUtil.cloneDocument(yfcInDoc);
        YFCElement yfce = cloneInDoc.getDocumentElement();
        cloneInDoc.removeChild(yfce);
        wrapperElement.importNode(yfce);

        //Get document in env.
        String docid = props.getProperty(AcademyConstants.KEY_MERGEDOC_ENV_KEY);
        if (docid != null)
            addDocumentInEnv(env, rootElement, docid);
        
        int i = 1;
        while ((docid = props.getProperty(AcademyConstants.KEY_MERGEDOC_ENV_KEY + "." + i++)) != null) {
            addDocumentInEnv(env, rootElement, docid);
        }

        if (log.isDebugEnabled())
        	log.debug("Returning Merged Document: " + mergeDoc.getString());

        log.verbose("Exiting AcademyMergeDocumentInEnvWithInputAPI.mergeDocumentInEnvWithInputAPI()");
        return mergeDoc.getDocument();
    }
    
    /**
     * Retrieves the document stored in the environment context under the key
     * configured in property: academy.mergedoc.env.key. Merges this with
     * the Input Document and returns the merged document. Since org.w3c.dom.Document
     * follows the Composition pattern and does not allow an element to belong to
     * more than one Document, a clone of the input and environment documents
     * would be created prior to merging. The merging thus leaves both the input
     * document and the document in the environment intact.The structure of the
     * merged document would be:
     * <InputDocumentTag>
     *     <EnvironmentDocument>
     *         ...DOCUMENT RETRIEVED FROM THE ENVIRONMENT...
     *     </EnvironmentDocument>
     * </InputDocumentTag>
     * @return Returns the document stored in the environment merged with the
     * input document.
     * @param env Yantra Environment Context.
     * @param inDoc Input Document to be merged with the document in the environment.
     * @throws java.lang.Exception Thrown if unable to clone the input document
     * or the document retrieved from the environment.
     */    
    public Document mergeEnvDocumentWithInputAPI(YFSEnvironment env, Document inDoc) throws Exception {
    	log.verbose("Entering AcademyMergeDocumentInEnvWithInputAPI.mergeEnvDocumentWithInputAPI()");
        
        YFCDocument inputDoc = YFCDocument.getDocumentFor(inDoc);
        
        if (log.isDebugEnabled())
        	log.debug("Got Input Document: " + inputDoc.getString());
  
        //Get document in env.
        String docid = props.getProperty(AcademyConstants.KEY_MERGEDOC_ENV_KEY);
        YFCDocument environmentDoc = getDocumentFromEnvWithKey(env, docid);

        if (log.isDebugEnabled())
        	log.debug("EnviornmentDocument is : " + environmentDoc.getString());

        //Insert input document as first child.
        //Now, to import, we have to remove the element from the original Document
        //and then insert into new parent. Since we dont want to disturb the original
        //document, lets operate on its clone.
		YFCElement inElem = inputDoc.getDocumentElement();
        YFCDocument cloneEnvDoc = XMLUtil.cloneDocument(environmentDoc);
        YFCElement cloneEnvElem = cloneEnvDoc.getDocumentElement();
        cloneEnvDoc.removeChild(cloneEnvElem);
        inElem.importNode(cloneEnvElem);
        
        if (log.isDebugEnabled())
        	log.debug("Env Document merged with Input is: " + environmentDoc.getString());
        log.verbose("Exiting AcademyMergeDocumentInEnvWithInputAPI.mergeDocumentInEnvWithInputAPI()");
        return inputDoc.getDocument();
    }
    
    /**
     * Retrieves the document stored in the environment context under the key
     * configured in property: academy.mergedoc.env.key. Merges this with
     * the Input Document and returns the merged document. Since org.w3c.dom.Document
     * follows the Composition pattern and does not allow an element to belong to
     * more than one Document, a clone of the input and environment documents
     * would be created prior to merging. The merging thus leaves both the input
     * document and the document in the environment intact.The structure of the
     * merged document would be:
     * <EnvironmentDocument>
     *     <InputDocumentTag>
     *         ...DOCUMENT RETRIEVED FROM INPUT...
     *     </InputDocumentTag>
     * </EnvironmentDocument>
     * @return Returns the document stored in the environment merged with the
     * input document.
     * @param env Yantra Environment Context.
     * @param inDoc Input Document to be merged with the document in the environment.
     * @throws java.lang.Exception Thrown if unable to clone the input document
     * or the document retrieved from the environment.
     */    
    public Document mergeInputDocumentWithEnvAPI(YFSEnvironment env, Document inDoc) throws Exception {
    	log.verbose("Entering AcademyMergeDocumentInEnvWithInputAPI.mergeInputDocumentWithEnvAPI()");
        
        YFCDocument inputDoc = YFCDocument.getDocumentFor(inDoc);
        
        if (log.isDebugEnabled())
        	log.debug("Got Input Document: " + inputDoc.getString());
  
        //Get document in env.
        String docid = props.getProperty(AcademyConstants.KEY_MERGEDOC_ENV_KEY);
        YFCDocument environmentDoc = getDocumentFromEnvWithKey(env, docid);

        if (log.isDebugEnabled())
        	log.debug("EnviornmentDocument is : " + environmentDoc.getString());

        //Insert input document as first child.
        //Now, to import, we have to remove the element from the original Document
        //and then insert into new parent. Since we dont want to disturb the original
        //document, lets operate on its clone.
		YFCElement envElem = environmentDoc.getDocumentElement();
        YFCDocument cloneInputDoc = XMLUtil.cloneDocument(inputDoc);
        YFCElement cloneInputElem = cloneInputDoc.getDocumentElement();
        cloneInputDoc.removeChild(cloneInputElem);
        envElem.importNode(cloneInputElem);
        
        log.verbose("Exiting AcademyMergeDocumentInEnvWithInputAPI.mergeInputDocumentWithEnvAPI()");
        if (log.isDebugEnabled())
        	log.debug("Input Document merged with Env is: " + environmentDoc.getString());
  
        return environmentDoc.getDocument();
    }
    
    
    private void addDocumentInEnv(YFSEnvironment env, YFCElement rootElement, String docid)
    throws Exception {
        if (log.isDebugEnabled())
        	log.debug("Looking in Env for key [" + docid + "]");
        
        YFCDocument envDoc = getDocumentFromEnvWithKey(env, docid);

        //Create wrapper document.
        
        //Insert document obtained form the env as second child of the wrapper inDoc.
        YFCElement wrapperElement = rootElement.createChild(AcademyConstants.TAG_MERGE_ENVIRONMENT_DOC);        
        if (envDoc != null) {
            //Lets operate on env's clone.
            YFCDocument cloneInDoc = XMLUtil.cloneDocument(envDoc);
            YFCElement yfce = cloneInDoc.getDocumentElement();
            cloneInDoc.removeChild(yfce);
            wrapperElement.importNode(yfce);
        }
    }

	/**
	 * @param env
	 * @param docid
	 * @return
	 */
	private YFCDocument getDocumentFromEnvWithKey(YFSEnvironment env,
			String docid) {
		Object obj = AcademyUtil.getContextObject(env, docid);

        YFCDocument envDoc = null;
        if (obj instanceof Document) {
            envDoc = YFCDocument.getDocumentFor((Document) obj);
        } else if (obj instanceof YFCDocument) {
            envDoc = (YFCDocument) obj;
        }

        if (log.isDebugEnabled()) {
            if (envDoc != null) {
            	log.debug("Document Found in Env for Key [" + docid + "] Document [" + envDoc.getString() + "]");
            } else {
            	log.debug("No document found in Env for Key [" + docid + "] Values [" + obj + "]");
            }
        }
		return envDoc;
	}
    
}
