package com.academy.som.util.logging;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCXmlUtils;

/**
 * This is a utility class for looging all the messages shown to the user
 * 
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 * Copyright © 2006-2009 Sterling Commerce, Inc. All Rights Reserved.
 */
public class AcademySIMTraceUtil {
	/**
	 * This method is used to display the message at the start of each method
	 *
	 * @param strClass -
	 *            <br/> - Class Name to be logged
	 * @param strMethod -
	 *            <br/> - Method Name to be logged
	 *
	 */
	public static void startMessage(final String className,
			final String methodName) {

		if (YRCPlatformUI.isTraceEnabled()) {
			YRCPlatformUI.trace("Start of " + className + "." + methodName
					+ " Method");
		}

	}
	
	/**
	 * This method is used to display the message at the end of method
	 *
	 * @param strClass -
	 *            <br/> - Class Name to be logged
	 * @param strMethod -
	 *            <br/> - Method Name to be logged
	 *
	 */

	public static void endMessage(final String className,
			final String methodName) {

		if (YRCPlatformUI.isTraceEnabled()) {
			YRCPlatformUI.trace("End of " + className + "." + methodName
					+ " Method");
		}

	}
	
	/**
	 * This method is used to log messages onto the log file
	 *
	 * @param msg -
	 *         <br/> - Custom Message string to be added to the log
	 *
	 */

	public static void logMessage(final String msg) {

		if (YRCPlatformUI.isTraceEnabled()) {
			YRCPlatformUI.trace(msg);
		}
	}
	
	/**
	 * This method is used to log messages onto the log file with Element XML
	 *
	 * @param fstMsg
	 * 			<br/> - Custom message to be shown for the XML to be logged
	 * @param ele
	 * 			<br/> - Element XML to be logged
	 */
	public static void logMessage(final String fstMsg, final Element ele) {

		if (YRCPlatformUI.isTraceEnabled()) {
			if( ele!=null ){
				YRCPlatformUI.trace(fstMsg + " " + YRCXmlUtils.getString(ele));
			}else{
				YRCPlatformUI.trace(fstMsg);
			}
		}
	}
	
	/**
	 * This method is used to log messages onto the log file with Document XML
	 * 
	 * @param fstMsg
	 * 			<br/> - Custom message to be shown for the XML to be logged
	 * @param xml
	 * 			<br/> - Document XML to be logged
	 */
	public static void logMessage(final String fstMsg, final Document xml) {

		if (YRCPlatformUI.isTraceEnabled()) {
			if( xml!=null ){
				YRCPlatformUI.trace(fstMsg + " " + YRCXmlUtils.getString(xml));
			}else{
				YRCPlatformUI.trace(fstMsg);
			}
		}
	}
	
	/**
	 * This method is used to log messages onto the log file
	 * @param className
	 * 			<br/> - name of the class in error
	 * @param methodName
	 * 			<br/> - name of the class method in error
	 * @param exceptionType
	 * 			<br/> - exception type name
	 * @param exceptionDesc
	 * 			<br/> - exception message
	 */
	public static void logErrorMessage(final String className,
			final String methodName, final String exceptionType,
			final String exceptionDesc) {

		if (YRCPlatformUI.isTraceEnabled()) {
			String msgText = "\nException :: " + "\nClass Name =  " + className
					+ "\nMethod Name = " + methodName + "\nException Type = "
					+ exceptionType + "\nException Description = "
					+ exceptionDesc
					+ "\n --------------------------------------------\n";
			YRCPlatformUI.trace(msgText);
		}
	}
	
	
	/**
    *
    * This method takes a document Element as input and returns the XML String.
    * @param element   a valid element object for which XML output in String form is required.
    * @return XML String of the given element
    */
   
   public static String getElementXMLString( Element element ) {
       return serialize( element );
   }
   
   /**
    * Returns a formatted XML string for the Node, using encoding 'iso-8859-1'.
    *
    * @param node   a valid document object for which XML output in String form is required.
    *
    * @return the formatted XML string.
    */
   
   public static String serialize( Node node ) {
       return serialize(node, "iso-8859-1", true);
   }
   
   /**
    *	Return a XML string for a Node, with specified encoding and indenting flag.
    *	<p>
    *	<b>Note:</b> only serialize DOCUMENT_NODE, ELEMENT_NODE, and DOCUMENT_FRAGMENT_NODE
    *
    *	@param node the input node.
    *	@param encoding such as "UTF-8", "iso-8859-1"
    *	@param indenting indenting output or not.
    *
    *	@return the XML string
    */
   public static String serialize(Node node, String encoding, boolean indenting) {
       OutputFormat outFmt = null;
       StringWriter strWriter = null;
       XMLSerializer xmlSerializer = null;
       String retVal = null;
       
       try{
           outFmt = new OutputFormat("xml", encoding, indenting);
           outFmt.setOmitXMLDeclaration(true);
           
           strWriter = new StringWriter();
           
           xmlSerializer = new XMLSerializer(strWriter, outFmt);
           
           short ntype = node.getNodeType();
           
           switch(ntype) {
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
}
