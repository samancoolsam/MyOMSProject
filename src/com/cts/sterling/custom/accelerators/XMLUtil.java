package com.cts.sterling.custom.accelerators.util;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.yantra.yfc.dom.YFCDocument;



/**
 * The Class XMLUtil.
 */
public class XMLUtil 
{

	/**
	 * getFirstChildNodeByName() will give FirstChildNode specified by childName in Node.
	 * If the no childnode is found and createNode is 'true',
	 * then this method will create new childnode under the node
	 *
	 * @param node the parent node
	 * @param childName the child node's name
	 * @param createNode the create node or not
	 * @return the first child node by name
	 */
	public static Node getFirstChildNodeByName(Node node, String childName,boolean createNode)
	{

		NodeList childList = node.getChildNodes();
		if (childList.getLength()>0) 
		{
			for (int childIndex = 0; childIndex < childList.getLength(); childIndex++) {
				if (childList.item(childIndex).getNodeName().equals(childName)) {
					return childList.item(childIndex);
				}
			}
		}

		if (createNode) {
			Node newNode = node.getOwnerDocument().createElement(childName);
			node.appendChild(newNode);
			return newNode;
		}
		return null;
	}

	/**
	 * getDocument() is used to get empty document.
	 *
	 * @return the document
	 * @throws ParserConfigurationException the parser configuration exception
	 */
	public static Document getDocument() throws ParserConfigurationException 
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		dbf.setValidating(true);
		Document doc = db.newDocument();
		return doc;
	}

	/**
	 * getDocument(Element, boolean) is used to get the document from an Element.
	 *
	 * @param inputElement the input element
	 * @param deep the deep
	 * @return the document
	 * @throws ParserConfigurationException the parser configuration exception
	 */
	public static Document getDocument(Element inputElement, boolean deep)  throws ParserConfigurationException 
	{
		Document outputDocument = getDocument();
		outputDocument.appendChild(outputDocument.importNode(inputElement, deep));
		return outputDocument;
	}

	/**
	 * getDocument(InputStream) is used to get the document from InputStream.
	 *
	 * @param inputStream the input stream
	 * @return the document
	 * @throws SAXException the sAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException the parser configuration exception
	 */
	public static Document getDocument(InputStream inputStream)	throws SAXException, IOException, ParserConfigurationException 
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		dbf.setValidating(true);
		Document doc = db.parse(inputStream);
		return doc;
	}

	/**
	 * getDocument(String) is used to get the document from a string in XML format.
	 *
	 * @param inputXMLString the input xml string
	 * @return the document
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws Exception the exception
	 */
	public static Document getDocument(String inputXMLString)throws IllegalArgumentException, Exception 
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		dbf.setValidating(true);
		Document resultDocument = db.parse(new InputSource(
				new BufferedReader(new InputStreamReader(
						new ByteArrayInputStream(inputXMLString.getBytes())))));
		return resultDocument;
	}

	/**
	 * getDocument(String, boolean) is used to get the document through the specified inputXMLString and validate also.
	 *
	 * @param inputXMLString the input xml string
	 * @param validate the validate
	 * @return the document
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws Exception the exception
	 */
	public static Document getDocument(String inputXMLString, boolean validate)	throws IllegalArgumentException, Exception 
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		dbf.setValidating(validate);
		Document resultDocument = null;

		try 
		{
			resultDocument = db.parse(new InputSource(
					new BufferedReader(
							new InputStreamReader(new ByteArrayInputStream(
									inputXMLString.getBytes())))));
		}
		catch (FileNotFoundException fne) 
		{
			if (validate) {
				throw new FileNotFoundException(
				"DTD declared for XML not found!");
			}
		}
		return resultDocument;
	}

	/**
	 * getFirstElementByName(parentElement, tagName) is used to get first instance of element specified by tagName
	 * in the parentElement
	 *
	 * @param ele the element
	 * @param tagName the tag name
	 * @return the first element by name
	 */
	public static Element getFirstElementByName(Element parentElement, String tagName) 
	{
		StringTokenizer st = new StringTokenizer(tagName, "/");
		Element curr = parentElement;
		Node node;
		String tag;
		while (st.hasMoreTokens()) 
		{
			tag = st.nextToken();
			node = curr.getFirstChild();
			while (node != null) 
			{
				if (node.getNodeType() == Node.ELEMENT_NODE
						&& tag.equals(node.getNodeName())) 
				{
					break;
				}
				node = node.getNextSibling();
			}
			if (node != null)
				curr = (Element) node;
			else
				return null;
		}
		return curr;
	}

	/**
	 * getXmlFromFile(String) is used to get an XML Document from the specified file.
	 * the specified File shouldn't be empty or NULL. 
	 *
	 * @param inputFileName the input file name
	 * @return the xml from file
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws Exception the exception
	 */
	public static Document getXmlFromFile(String inputFileName)	throws IllegalArgumentException, Exception 
	{
		if (null == inputFileName || 0 == inputFileName.trim().length()) {
			throw new IllegalArgumentException(
			"Input Filename cannot be null or empty ");
		}
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document resultDocument = documentBuilder.parse(new File(inputFileName));
		return resultDocument;
	}

	/**
	 * getString(document) is used  to get an Document object as string.
	 *
	 * @param inputDocument the input document
	 * @return the string
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public static String getString(Document inputDocument) throws IllegalArgumentException 
	{
		return YFCDocument.getDocumentFor(inputDocument).getString();
	}

	/**
	 * getXmlString(node) is used  to get an Node object as string.
	 *
	 * @param node the node
	 * @return the xml string
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws Exception the exception
	 */
	public static String getXmlString(Node node)throws IllegalArgumentException, Exception
	{
		return YFCDocument.getNodeFor(node).toString();
	}

	/**
	 *renameAttribute() is used to rename the attribute name under one specified Element.
	 *This will get the specified attribute as 'oldName' under the specified Element 'elementObject'.
	 *It removes the 'oldName' attribute and set the new attribute in the same place with attributeName 'newName'.
	 *
	 * @param elementObject the element object
	 * @param oldName the old name
	 * @param newName the new name
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws Exception the exception
	 */
	public static void renameAttribute(Element elementObject, String oldName,String newName) throws IllegalArgumentException, Exception 
	{
		String attributeValue = null;
		attributeValue = elementObject.getAttribute(oldName);
		elementObject.removeAttribute(oldName);
		elementObject.setAttribute(newName, attributeValue);
	}

	/**
	 * setManyAttributes(Element, Map) is used to set many attributes at a time under the specified Element.
	 * First get the attributes what and all to set, goto the specified Element and stamp all the
	 * attributes one by one.
	 *
	 * @param element the element
	 * @param attributeMap the set of attributes under Map.
	 * @return the element
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws Exception the exception
	 */
	public static Element setManyAttributes(Element element, Map attributeMap)throws IllegalArgumentException, Exception 
	{
		Object[] keyList = attributeMap.keySet().toArray();
		for (int index = 0; index < keyList.length; index++)
		{
			String attributeName = keyList[index].toString();
			String attributeValue = attributeMap.get(keyList[index]).toString();
			element.setAttribute(attributeName, attributeValue);
		}
		return element;
	}

	/**
	 * writeXmlToFile(Document, targetfilename) is used to write the specified Document 
	 * to the specified targetfilename.
	 * FileName should be absolute path.
	 *
	 * @param inputDocument the input document
	 * @param targetFileName the target file name
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws Exception the exception
	 */
	public static void writeXmlToFile(Document inputDocument,String targetFileName) throws IllegalArgumentException, Exception 
	{
		String xmlString = getXmlString(inputDocument);
		try (DataOutputStream dataOutputStream = new DataOutputStream(
				new FileOutputStream(targetFileName))){	//OMNI-90680
		dataOutputStream.writeBytes(xmlString);	
		dataOutputStream.close();}//OMNI-90680
	}

	/**
	 * createDocument(docElementTag) is used to create Document with root element as docElementTag.
	 *
	 * @param docElementTag the doc element tag
	 * @return the document
	 * @throws ParserConfigurationException the parser configuration exception
	 */
	public static Document createDocument(String docElementTag)	throws ParserConfigurationException
	{
		Document doc = getDocument();
		Element ele = doc.createElement(docElementTag);
		doc.appendChild(ele);
		return doc;
	}

	/**
	 * getAttributeFromXPath(Document,xpath) gets the attribute from the input document at the specified Xpath.
	 *
	 * @param inXml the in xml
	 * @param xpath the xpath
	 * @return the attribute from x path
	 * @throws XPathExpressionException the x path expression exception
	 */
	public final static String getAttributeFromXPath(Document inXml,String xpath) throws XPathExpressionException 
	{
		XPathExpression expression = XPathFactory.newInstance().newXPath().compile(xpath);
		return XMLUtil.getAttributeFromXPath(inXml, expression);
	}

	private final static String getAttributeFromXPath(Document inXml,XPathExpression expression) throws XPathExpressionException 
	{
		return expression.evaluate(inXml);
	}

	/**
	 * copyAttributes(Element,Element,String) is used to copy the specified attributes from one Element to another Element.
	 * First it will fetch the specified 'attributes' from the'fromElement' and copy into the 'toElement'.
	 *
	 * @param fromElement the from element
	 * @param toElement the to element
	 * @param attributes set of attributes
	 */
	public final static void copyAttributes(final Element fromElement,final Element toElement, final String[] attributes) 
	{
		int size = attributes.length;
		String attribute = null;
		for (int cnt = 0; cnt < size; cnt++) {
			attribute = attributes[cnt];
			if (fromElement.hasAttribute(attribute)) {
				toElement.setAttribute(attribute, fromElement
						.getAttribute(attribute));
			}
		}
	}

	/**
	 * getDocument(inputSource) used to get the Document from InputSource.
	 *
	 * @param inSource the in source
	 * @return the document
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the sAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Document getDocument( InputSource inSource ) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilder dbdr = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return dbdr.parse( inSource );
	}

	/**
	 * Serialize Document, Element or Node using this mehtod. Encoding used is iso-8859-1
	 *  and indenting is set to true
	 *
	 * @param node the node
	 * @return the string
	 */
	public static String serialize( Node node )
	{
		return serialize(node, "iso-8859-1", true);
	}

	/**
	 * Serialize(Node,String,boolean) is used to serialize the Document, Element or Node.
	 * @param node the node
	 * @param encoding the encoding
	 * @param indenting the indenting
	 * @return the string
	 */
	public static String serialize(Node node, String encoding, boolean indenting)
	{
		OutputFormat outFmt = null;
		StringWriter strWriter = new StringWriter();
		XMLSerializer xmlSerializer = null;
		String retVal = null;

		try{
			outFmt = new OutputFormat("xml", encoding, indenting);
			outFmt.setOmitXMLDeclaration(true);
			xmlSerializer = new XMLSerializer(strWriter, outFmt);

			short ntype = node.getNodeType();

			switch(ntype)
			{
			case Node.DOCUMENT_FRAGMENT_NODE: xmlSerializer.serialize((DocumentFragment)node); break;
			case Node.DOCUMENT_NODE: xmlSerializer.serialize((Document)node); break;
			case Node.ELEMENT_NODE: xmlSerializer.serialize((Element)node); break;
			default: throw new IOException("Can serialize only Document, DocumentFragment and Element type nodes");
			}

			retVal = strWriter.toString();
		} catch (IOException e) {
			retVal = e.getMessage();
		} finally{
			try {
				strWriter.close();
			} catch (IOException ie) {}
		}

		return retVal;
	}

	/**
	 * parseSpecialCharacters(String) Parses the special characters for the specified string.
	 * It will parse the special characters like '&,<,",\'s
	 *
	 * @param str the  inputstring
	 * @return parsed String
	 */
	public static String parseSpecialCharacters(String str)
	{
		if (str == null || str.length() == 0)
			return str;

		StringBuffer buf = new StringBuffer(str);
		int i = 0;
		char c;

		while (i < buf.length())
		{
			c = buf.charAt(i);
			if (c == '&') {
				buf.replace(i, i+1, "&amp;");
				i += 5;
			}
			else if (c == '<') {
				buf.replace(i, i+1, "&lt;");
				i += 4;
			}
			else if (c == '"') {
				buf.replace(i, i+1, "&quot;");
				i += 6;
			}
			else if (c == '\'') {
				buf.replace(i, i+1, "&apos;");
				i += 6;
			}
			else
				i++;
		}

		return buf.toString();
	}
	/**
	 * 
	 * This method will return Node list specified by XPath from the input document object
	 * @param inputDocument
	 * @param XPath
	 * @return
	 * @throws Exception
	 */
	
	
	public static List<Node> getNodesByXpath(Document inputDocument,String XPath) throws Exception
	{
		XMLUtil xmlUtil = new XMLUtil();
		List<Node> nodeList=new ArrayList<Node>();
		NodeList nl = null;
		nl = inputDocument.getElementsByTagName(xmlUtil.getLastToken(XPath,"/"));
		for(int i=0;i<nl.getLength();i++)
		{
			Node n = nl.item(i);
			if(xmlUtil.nodeHasCorrectXPath(n,XPath))
			{
				nodeList.add(n);
			}
		}
		return nodeList;
	}

	private boolean nodeHasCorrectXPath(Node n, String XPath) 
	{
		ArrayList<String> stringList = new ArrayList<String>();
		StringTokenizer t = new StringTokenizer(XPath,"/");
		while(t.hasMoreTokens())
		{
			stringList.add(t.nextToken());
		}
		int xpathTokenSize = stringList.size();
		while(n.getNodeType()!=9)
		{
			xpathTokenSize = xpathTokenSize-1;
			if(!n.getNodeName().equals(stringList.get(xpathTokenSize)))
			{
				return false;
			}
			n = n.getParentNode();
		}
		return true;
	}

	private String getLastToken(String XPath,String token)
	{
		StringTokenizer t = new StringTokenizer(XPath,"/");
		String lastToken = "";
		while(t.hasMoreTokens())
		{
			lastToken = t.nextToken();
		}
		return lastToken;
	}
}