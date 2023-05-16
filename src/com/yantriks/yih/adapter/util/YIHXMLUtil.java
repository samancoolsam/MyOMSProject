package com.yantriks.yih.adapter.util;

import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.DatatypeConverter;

public class YIHXMLUtil
{
  private static final String YANTRA_ISNAMESPACEWARE = null;
  private static YFCLogCategory logger = (YFCLogCategory)YFCLogCategory.getLogger(YantriksCommonUtil.class.getName());
  
  public static String getXMLString(Document document)
  {
    return serialize(document);
  }
  
  public static String serialize(Node node)
  {
    return serialize(node, "iso-8859-1", true);
  }
  
  /* Error */
  public static String serialize(Node node, String encoding, boolean indenting)
  {
	return encoding;
    // Byte code:
    //   0: aconst_null
    //   1: astore_3
    //   2: aconst_null
    //   3: astore 4
    //   5: aconst_null
    //   6: astore 5
    //   8: aconst_null
    //   9: astore 6
    //   11: new 52	org/apache/xml/serialize/OutputFormat
    //   14: dup
    //   15: ldc 54
    //   17: aload_1
    //   18: iload_2
    //   19: invokespecial 56	org/apache/xml/serialize/OutputFormat:<init>	(Ljava/lang/String;Ljava/lang/String;Z)V
    //   22: astore_3
    //   23: aload_3
    //   24: iconst_1
    //   25: invokevirtual 59	org/apache/xml/serialize/OutputFormat:setOmitXMLDeclaration	(Z)V
    //   28: new 63	java/io/StringWriter
    //   31: dup
    //   32: invokespecial 65	java/io/StringWriter:<init>	()V
    //   35: astore 4
    //   37: new 66	org/apache/xml/serialize/XMLSerializer
    //   40: dup
    //   41: aload 4
    //   43: aload_3
    //   44: invokespecial 68	org/apache/xml/serialize/XMLSerializer:<init>	(Ljava/io/Writer;Lorg/apache/xml/serialize/OutputFormat;)V
    //   47: astore 5
    //   49: aload_0
    //   50: invokeinterface 71 1 0
    //   55: istore 7
    //   57: iload 7
    //   59: lookupswitch	default:+69->128, 1:+57->116, 9:+45->104, 11:+33->92
    //   92: aload 5
    //   94: aload_0
    //   95: checkcast 77	org/w3c/dom/DocumentFragment
    //   98: invokevirtual 79	org/apache/xml/serialize/XMLSerializer:serialize	(Lorg/w3c/dom/DocumentFragment;)V
    //   101: goto +37 -> 138
    //   104: aload 5
    //   106: aload_0
    //   107: checkcast 82	org/w3c/dom/Document
    //   110: invokevirtual 84	org/apache/xml/serialize/XMLSerializer:serialize	(Lorg/w3c/dom/Document;)V
    //   113: goto +25 -> 138
    //   116: aload 5
    //   118: aload_0
    //   119: checkcast 87	org/w3c/dom/Element
    //   122: invokevirtual 89	org/apache/xml/serialize/XMLSerializer:serialize	(Lorg/w3c/dom/Element;)V
    //   125: goto +13 -> 138
    //   128: new 92	java/io/IOException
    //   131: dup
    //   132: ldc 94
    //   134: invokespecial 96	java/io/IOException:<init>	(Ljava/lang/String;)V
    //   137: athrow
    //   138: aload 4
    //   140: invokevirtual 99	java/io/StringWriter:toString	()Ljava/lang/String;
    //   143: astore 6
    //   145: goto +54 -> 199
    //   148: astore 7
    //   150: aload 7
    //   152: invokevirtual 102	java/io/IOException:getMessage	()Ljava/lang/String;
    //   155: astore 6
    //   157: aload 4
    //   159: invokevirtual 105	java/io/StringWriter:close	()V
    //   162: goto +54 -> 216
    //   165: astore 9
    //   167: aload 9
    //   169: invokevirtual 102	java/io/IOException:getMessage	()Ljava/lang/String;
    //   172: astore 6
    //   174: goto +42 -> 216
    //   177: astore 8
    //   179: aload 4
    //   181: invokevirtual 105	java/io/StringWriter:close	()V
    //   184: goto +12 -> 196
    //   187: astore 9
    //   189: aload 9
    //   191: invokevirtual 102	java/io/IOException:getMessage	()Ljava/lang/String;
    //   194: astore 6
    //   196: aload 8
    //   198: athrow
    //   199: aload 4
    //   201: invokevirtual 105	java/io/StringWriter:close	()V
    //   204: goto +12 -> 216
    //   207: astore 9
    //   209: aload 9
    //   211: invokevirtual 102	java/io/IOException:getMessage	()Ljava/lang/String;
    //   214: astore 6
    //   216: aload 6
    //   218: areturn
    // Line number table:
    //   Java source line #70	-> byte code offset #0
    //   Java source line #71	-> byte code offset #2
    //   Java source line #72	-> byte code offset #5
    //   Java source line #73	-> byte code offset #8
    //   Java source line #76	-> byte code offset #11
    //   Java source line #77	-> byte code offset #23
    //   Java source line #79	-> byte code offset #28
    //   Java source line #81	-> byte code offset #37
    //   Java source line #83	-> byte code offset #49
    //   Java source line #85	-> byte code offset #57
    //   Java source line #87	-> byte code offset #92
    //   Java source line #88	-> byte code offset #101
    //   Java source line #90	-> byte code offset #104
    //   Java source line #91	-> byte code offset #113
    //   Java source line #93	-> byte code offset #116
    //   Java source line #94	-> byte code offset #125
    //   Java source line #95	-> byte code offset #128
    //   Java source line #98	-> byte code offset #138
    //   Java source line #99	-> byte code offset #145
    //   Java source line #100	-> byte code offset #150
    //   Java source line #103	-> byte code offset #157
    //   Java source line #104	-> byte code offset #162
    //   Java source line #105	-> byte code offset #167
    //   Java source line #101	-> byte code offset #177
    //   Java source line #103	-> byte code offset #179
    //   Java source line #104	-> byte code offset #184
    //   Java source line #105	-> byte code offset #189
    //   Java source line #108	-> byte code offset #196
    //   Java source line #103	-> byte code offset #199
    //   Java source line #104	-> byte code offset #204
    //   Java source line #105	-> byte code offset #209
    //   Java source line #110	-> byte code offset #216
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	219	0	node	Node
    //   0	219	1	encoding	String
    //   0	219	2	indenting	boolean
    //   1	43	3	outFmt	org.apache.xml.serialize.OutputFormat
    //   3	197	4	strWriter	java.io.StringWriter
    //   6	111	5	xmlSerializer	org.apache.xml.serialize.XMLSerializer
    //   9	208	6	retVal	String
    //   55	3	7	ntype	short
    //   148	3	7	e	java.io.IOException
    //   177	20	8	localObject	Object
    //   165	3	9	ie	java.io.IOException
    //   187	3	9	ie	java.io.IOException
    //   207	3	9	ie	java.io.IOException
    // Exception table:
    //   from	to	target	type
    //   11	145	148	java/io/IOException
    //   157	162	165	java/io/IOException
    //   11	157	177	finally
    //   179	184	187	java/io/IOException
    //   199	204	207	java/io/IOException
  }
  
  public static Document createDocument(String docElementTag)
  {
    Document doc = null;
    try
    {
      DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
      fac.setNamespaceAware(true);
      DocumentBuilder dbdr = fac.newDocumentBuilder();
      
      doc = dbdr.newDocument();
      Element ele = doc.createElement(docElementTag);
      doc.appendChild(ele);
    }
    catch (ParserConfigurationException e)
    {
      logger.error(e);
      throw new YFCException(e);
    }
    return doc;
  }
  
  public static String getElementXMLString(Element element)
  {
    return serialize(element);
  }
  
  public static Document getDocumentForElement(Element inElement)
  {
    Document doc = null;
    try
    {
      DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
      fac.setNamespaceAware(true);
      
      DocumentBuilder dbdr = fac.newDocumentBuilder();
      doc = dbdr.newDocument();
      Element docElement = doc.createElement(inElement.getNodeName());
      doc.appendChild(docElement);
      copyElement(doc, inElement, docElement);
    }
    catch (ParserConfigurationException e)
    {
      logger.error(e);
      throw new YFCException(e);
    }
    return doc;
  }
  
  public static void copyElement(Document destDoc, Element srcElem, Element destElem)
  {
    NamedNodeMap attrMap = srcElem.getAttributes();
    int attrLength = attrMap.getLength();
    for (int count = 0; count < attrLength; count++)
    {
      Node attr = attrMap.item(count);
      String attrName = attr.getNodeName();
      String attrValue = attr.getNodeValue();
      destElem.setAttribute(attrName, attrValue);
    }
    if (srcElem.hasChildNodes())
    {
      NodeList childList = srcElem.getChildNodes();
      int numOfChildren = childList.getLength();
      for (int cnt = 0; cnt < numOfChildren; cnt++)
      {
        Object childSrcNode = childList.item(cnt);
        if ((childSrcNode instanceof CharacterData))
        {
          if ((childSrcNode instanceof Text))
          {
            String data = ((CharacterData)childSrcNode).getData();
            Node childDestNode = destDoc.createTextNode(data);
            destElem.appendChild(childDestNode);
          }
          else if ((childSrcNode instanceof Comment))
          {
            String data = ((CharacterData)childSrcNode).getData();
            Node childDestNode = destDoc.createComment(data);
            destElem.appendChild(childDestNode);
          }
        }
        else
        {
          Element childSrcElem = (Element)childSrcNode;
          Element childDestElem = appendChild(destDoc, destElem, 
            childSrcElem.getNodeName(), null);
          copyElement(destDoc, childSrcElem, childDestElem);
        }
      }
    }
  }
  
  public static Element appendChild(Document doc, Element parentElement, String elementName, Object value)
  {
    Element childElement = createElement(doc, elementName, value);
    parentElement.appendChild(childElement);
    return childElement;
  }
  
  public static Element createElement(Document doc, String elementName, Object hashAttributes)
  {
    return createElement(doc, elementName, hashAttributes, false);
  }
  
  private static Element createElement(Document doc, String elementName, Object hashAttributes, boolean textNodeFlag)
  {
    Element elem = doc.createElement(elementName);
    if (hashAttributes != null) {
      if ((hashAttributes instanceof String))
      {
        if (textNodeFlag) {
          elem.appendChild(doc
            .createTextNode((String)hashAttributes));
        }
      }
      else if ((hashAttributes instanceof Hashtable))
      {
        Enumeration e = ((Hashtable)hashAttributes).keys();
        String attributeName;
        String attributeValue;
        for (; e.hasMoreElements(); elem.setAttribute(attributeName, 
              attributeValue))
        {
          attributeName = (String)e.nextElement();
          attributeValue = 
            (String)((Hashtable)hashAttributes).get(attributeName);
        }
      }
    }
    return elem;
  }
  
  public static String massageItemID(String itemID)
  {
    if (itemID.contains("-")) {
      itemID = itemID.replaceAll("-", "");
    }
    return itemID;
  }
  
	
	public static String callYantriksAPI(String apiUrl, String httpMethod, String body){

		logger.debug("Enter PTYIHUtils.callYantriksAPI");
		if(YFCCommon.isVoid(httpMethod) || YFCCommon.isVoid(body) || YFCCommon.isVoid(apiUrl)){
			logger.debug("Mandatory parameters are missing");
			if(logger.isDebugEnabled()){
				logger.debug("httpMethod:: "+httpMethod+" body:: "+body+" apiUrl:: "+apiUrl);
			}
			return "";
		}
		String outputStr = "";
		try {
		String protocol = YFSSystem.getProperty(YantriksConstants.YIH_PROTOCOL); //yih.protocol
		String host = YFSSystem.getProperty(YantriksConstants.YIH_HOSTNAME); //yih.hostname
		String port = YFSSystem.getProperty(YantriksConstants.YIH_PORT);     //yih.port
		String timeout = YFSSystem.getProperty(YantriksConstants.YIH_TIMEOUT); //yih.timeout
		//Added for PDOMS-2451 -- Start
		String strIsSecurityEnabled = YFSSystem.getProperty(YantriksConstants.YIH_IS_API_SECURITY_ENABLED);
		String strUserName = YFSSystem.getProperty(YantriksConstants.YIH_USER_NAME);
		String strPwd = YFSSystem.getProperty(YantriksConstants.YIH_PASSWORD);
		String userCredentials = strUserName + ":" + strPwd;
		byte[] message = userCredentials.getBytes("UTF-8");
		String encodedAuth = DatatypeConverter.printBase64Binary(message);
		//String basicAuth = Base64.getEncoder().encodeToString((userName+":"+password).getBytes(StandardCharsets.UTF_8));
		//Added for PDOMS-2451 -- End
		
		if(logger.isDebugEnabled()){
			logger.debug("protocol:: "+protocol+" host:: "+host+" port:: "+port+" timeout:: "+timeout);
		}

		URL url = null;
			if(!YFCCommon.isVoid(port))
			{
				url = new URL(protocol + "://" + host + port + apiUrl);
			}
			else
			{
				url = new URL(protocol + "://" + host + apiUrl);
			}
			logger.debug("URL is:"+url.toString());
			if(logger.isDebugEnabled()){
				logger.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
				logger.debug("HTTP url : " + url);
				logger.debug("Body: " + body);
				logger.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			}
			long startTime = System.currentTimeMillis();
			// Check if protocol is http
			if(YantriksConstants.YIH_HTTP_PROTOCOL.equals(protocol)){
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();			
				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setRequestMethod(httpMethod);
				conn.setRequestProperty("Accept", "application/json");
				conn.setRequestProperty("Content-Type", "application/json");
				//Added for PDOMS-2451 -- Start
				if(YantriksConstants.TRUE.equalsIgnoreCase(strIsSecurityEnabled)){
				conn.setRequestProperty ("Authorization", "Basic "+encodedAuth);
				}
				//Added for PDOMS-2451 -- End

				if(!YFCCommon.isVoid(timeout))
					conn.setConnectTimeout(Integer.parseInt(timeout));

				if(httpMethod.equals(YantriksConstants.YIH_REQ_METHOD_POST)){
					OutputStream os = conn.getOutputStream();
					os.write(body.getBytes());
					os.flush();
				}
				long endTime   = System.currentTimeMillis();
				if(logger.isDebugEnabled()){
					logger.debug("Output from Server ...." + conn.toString());
				}
				if (conn.getResponseCode() != 200) {

					logger.debug("Yantriks API Failed : HTTP error code : " + conn.getResponseCode());
				}
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

				String outputLine = null;
				while ((outputLine = br.readLine()) != null) {
					outputStr = outputStr.concat(outputLine);
				}
				if(logger.isDebugEnabled()){
					logger.debug("Output from Server ....");
					logger.debug(outputStr);
				}
				if(outputStr!=null){
					String statusCode = getJsonAttribute(outputStr, "\"status\":");
					String statusMessage = getJsonAttribute(outputStr, "\"message\":");
					long totalTime = endTime - startTime;
					logger.debug("URL: "+url + ", StatusCode: "+statusCode + ", ResponseTime: "+totalTime);
					// If the response status code is 500, create an alert with ErrorCode and Error description
					if (statusCode.equals("500") || statusCode.equals("350") || statusCode.equals("400")) {
						logger.debug("Status Code from the Response is: " + statusCode);
						logger.debug("Status message from the Response is: " + statusMessage);
						throw new YFSException();
					}
				}
				conn.disconnect();
			} else if(YantriksConstants.YIH_HTTPS_PROTOCOL.equals(protocol)){
				// Check if protocol is https
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();			
				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setRequestMethod(httpMethod);
				conn.setRequestProperty("Accept", "application/json");
				conn.setRequestProperty("Content-Type", "application/json");
				//Added for PDOMS-2451 -- Start
				if(YantriksConstants.TRUE.equalsIgnoreCase(strIsSecurityEnabled)){
				conn.setRequestProperty ("Authorization", "Basic "+encodedAuth);
				}
				//Added for PDOMS-2451 -- End

				if(!YFCCommon.isVoid(timeout))
					conn.setConnectTimeout(Integer.parseInt(timeout));

				if(httpMethod.equals(YantriksConstants.YIH_REQ_METHOD_POST)){
					OutputStream os = conn.getOutputStream();
					os.write(body.getBytes());
					os.flush();
				}
				long endTime   = System.currentTimeMillis();
				if(logger.isDebugEnabled()){
					logger.debug("Output from Server ...." + conn.toString());
				}
				if (conn.getResponseCode() != 200) {

					logger.debug("Yantriks API Failed : HTTP error code : " + conn.getResponseCode());
				}
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

				String outputLine = null;
				while ((outputLine = br.readLine()) != null) {
					outputStr = outputStr.concat(outputLine);
				}
				if(logger.isDebugEnabled()){
					logger.debug("Output from Server ....");
					logger.debug(outputStr);
				}
				if(outputStr!=null){
					String statusCode = getJsonAttribute(outputStr, "\"status\":");
					String statusMessage = getJsonAttribute(outputStr, "\"message\":");
					long totalTime = endTime - startTime;
					logger.debug("URL: "+url + ", StatusCode: "+statusCode + ", ResponseTime: "+totalTime);
					// If the response status code is 500, create an alert with ErrorCode and Error description
					if (statusCode.equals("500") || statusCode.equals("350") || statusCode.equals("400")) {
						logger.debug("Status Code from the Response is: " + statusCode);
						logger.debug("Status message from the Response is: " + statusMessage);
						throw new YFSException();
					}
				}
				conn.disconnect();
			}
		} catch (Exception e) {
			logger.error("Error : " +  e.getMessage() + " URL: "+ apiUrl + " Input: "+body);
			e.printStackTrace();
			//throw new RuntimeException(e);
		}
		logger.debug("Exit PTYIHUtils.callYantriksAPI");
		return outputStr; 
	}

	public static String getJsonAttribute(String jsonStr, String attr) {
		//return null if attribute is not found.
		if(jsonStr.indexOf(attr) == -1)
			return null;

		//get substring starting after the attribute. 
		jsonStr = jsonStr.substring(jsonStr.indexOf(attr)+attr.length());

		//format ending - get substring till , if it is not last attribute. If it is last attribute, get substring till }.
		jsonStr = jsonStr.indexOf(",") > -1 ? jsonStr.substring(0,jsonStr.indexOf(",")) : jsonStr.substring(0,jsonStr.indexOf("}"));
		return jsonStr.trim();
	}
	public static String getXpathAttribute(final Document doc,
            final String xpath) throws TransformerException, ParserConfigurationException {
        // Validate doc name
        if (doc == null) {
            throw new IllegalArgumentException("Document Object passed "
                    + " cannot be null");
        }
        // Validate xPath name
        if (xpath == null) {
            throw new IllegalArgumentException("XPath String passed "
                    + " cannot be null");
        }
        Node node = getXpathNode(doc, xpath);
        if (node == null) {
            return "";
        } else {
            return node.getNodeValue();
        }
    }
	public static Node getXpathNode(final Document doc, final String xpath)
            throws TransformerException, ParserConfigurationException {
        // Validate doc name
        if (doc == null) {
            throw new IllegalArgumentException("Document Object passed "
                    + " cannot be null");
        }
        // Validate xPath name
        if (xpath == null) {
            throw new IllegalArgumentException("XPath String passed "
                    + " cannot be null");
        }
        NodeList nodeList = getXpathNodeList(doc, xpath);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0);
        } else {
            return null;
        }
    }
	 public static NodeList getXpathNodeList(final Node node, final String xpath)
	            throws ParserConfigurationException, TransformerException {
	        if (node == null) {
	            throw new IllegalArgumentException(
	                    "Input XML string cannot be null in "
	                            + "GenericExXMLUtil.getXpathNodeList method");
	        }
	        if (xpath == null || xpath.trim().equals("")) {
	            throw new IllegalArgumentException(
	                    "Input XML string cannot be null in "
	                            + "GenericExXMLUtil.getXpathNodeList method");
	        }
	        // Create a new XML document from the node to start
	        // the traversal from the node.
	        Document document = YIHXMLUtil.getDocument();
	        document.appendChild(document.importNode(node, true));
	        // Call the getNodeList which takes the Dcoument input
	        // to get the childnodes from the specified parent node.
	        return getXpathNodeList(document, xpath);
	    }

	 public static Document getDocument() throws ParserConfigurationException {
	        // Create a new Document Builder Factory instance
	        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
	                .newInstance();

	        // Create new document builder
	        DocumentBuilder documentBuilder = documentBuilderFactory
	                .newDocumentBuilder();

	        // Create and return document object
	        return documentBuilder.newDocument();
	    }
}

