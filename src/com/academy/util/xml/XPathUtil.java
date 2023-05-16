/*
 * Created on Nov 24, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.academy.util.xml;

import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

import com.yantra.yfc.log.YFCLogCategory;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 *  <B>Important Note:It is strongly recommended to use XPathWrapper instead of this class</B>
 *  An utility class to use the Xpath to access the nodes in a xml document.
 */
public class XPathUtil {
	/**
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(XPathUtil.class);

    /**
     * @return
     * @throws Exception
     * @deprecated use XMLUtil.newDocument() instead.
     */
    public static Document getDocument() throws Exception {
        //Create a new Document Bilder Factory instance
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        
        //Create new document builder
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        
        //Create and return document object
        return documentBuilder.newDocument();
    }
    
    /**
     * Evaluates the given Xpath and returns the corresponding node.
     *
     * @param node  document context.
     * @param xpath xpath that has to be evaluated.
     * @return node if found
     * @throws Exception exception
     */
    public static Node getNode(Node node, String xpath)
    throws Exception {
        if (null == node) {
            return null;
        }
        Node ret = null;
        try {
            ret = XPathAPI.selectSingleNode(node, xpath);
        } catch (TransformerException e) {
            throw e;
        }
        return ret;
    }
    
    /**
     * Evaluates the given Xpath and returns the corresponding value as a String.
     *
     * @param node  document context.
     * @param xpath xpath that has to be evaluated.
     * @return String Value of the XPath Execution.
     * @throws Exception exception
     */
    public static String  getString(Node node, String xpath)
    throws Exception {
        if (null == node) {
            return null;
        }
        String value = null;
        try {
            XObject xobj = XPathAPI.eval(node, xpath);
            value = xobj.toString();
        } catch (TransformerException e) {
            throw e;
        }
        return value;
    }
    
    /**
     * Evaluates the given Xpath and returns the corresponding node list.
     *
     * @param node  document context
     * @param xpath xpath to be evaluated
     * @return nodelist
     * @throws Exception exception
     */
    public static NodeList getNodeList(Node node, String xpath)
    throws Exception {
        if (null == node) {
            return null;
        }
        NodeList ret = null;
        try {
            ret = XPathAPI.selectNodeList(node, xpath);
        } catch (TransformerException e) {
            throw e;
        }
        return ret;
    }
    
    /**
     * Evaluates the given Xpath and returns the corresponding node iterator.
     *
     * @param node  document context
     * @param xpath xpath to be evaluated
     * @return nodelist
     * @throws Exception exception
     */
    public static NodeIterator getNodeIterator(Node node, String xpath)
    throws Exception {
        if (null == node) {
            return null;
        }
        NodeIterator ret = null;
        try {
            ret = XPathAPI.selectNodeIterator(node, xpath);
        } catch (TransformerException e) {
            throw e;
        }
        return ret;
    }
    
    /**
     * @param elName
     * @return
     * @throws Exception
     * @deprecated use XMLUtil.createDocument(String docElementTag)
     */
    public static Document getEmptyDoc(String elName) throws Exception {
        Document ret = getDocument();
        Element el = ret.createElement(elName);
        ret.appendChild(el);
        return ret;
    }
    public static Node getNodeWS(Node node,String str,QName nodeType) throws Exception
	{
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		 StringBuffer sb = new StringBuffer(str);

		if(str.startsWith("."))
		{
			sb.replace(0, 1, "/");
		
		}/*else if(str.startsWith("/"))
		{
			sb.replace(0, 0, "/");
		}*/
		String str1 = sb.toString();
		log.verbose("str1:"+str1);
		Node node1 =  (Node) xpath.evaluate(str1, node, nodeType);
		
		return node1;
	}
	public static NodeList getNodeListWS(Element ele,String str,QName nodeType) throws Exception
	{
			
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		 StringBuffer sb = new StringBuffer(str);

		if(str.startsWith("."))
		{
			sb.replace(0, 1, "/");
	
		}
		String str1 = sb.toString();			
		NodeList nodeList = (NodeList) xpath.evaluate(str1, ele, nodeType);
		
		return nodeList;
	}
	public static String getStringWS(Node node,String str,QName nodeType) throws Exception
	{			
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		StringBuffer sb = new StringBuffer(str);
	    sb.replace(0, 0, "//");
		String str1 = sb.toString();
		String string = (String) xpath.evaluate(str1, node, nodeType);
		return string;
	}
    
}
