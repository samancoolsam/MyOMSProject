package com.academy.ecommerce.sterling.shipment;

import java.util.HashMap;
import java.util.Properties;
import java.util.Map.Entry;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.log.YFCLogCategory;

public class AcademySpecialCharacter implements YIFCustomApi{

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademySpecialCharacter.class);

      Properties prop = new Properties();
      
      public void setProperties(Properties prop) throws Exception {
            this.prop = prop;
      }

      public Document processDoc(YFSEnvironment env, Document inXML) throws Exception{
	  if (log.isVerboseEnabled()) {
				log
						.verbose("********* AcademySpecialCharacter Begin ******\n"+YFCDocument.getDocumentFor(inXML));
			}
            Element rootElement = inXML.getDocumentElement();
            
            HashMap<String, String> hmRegexCharMap = new HashMap<String, String>(); 
            StringBuilder regexBuilder = new StringBuilder("[");
            for(Entry<Object, Object> propEntry: prop.entrySet()){
                  String propName = (String) propEntry.getKey();
                  if(propName.startsWith("RegexChar_")){
                        String propNamePruned = (propName.replace("RegexChar_", "")).trim();
                        regexBuilder.append(propNamePruned);
                        hmRegexCharMap.put(propNamePruned, (String)propEntry.getValue());
                  }
            }

            
            String regexString = regexBuilder.append("]").toString();
            String escapeNeeded = prop.getProperty("IsEscapeNeeded");
            
            if(!YFCCommon.isVoid(escapeNeeded)){
                  String elementsToReprocess = prop.getProperty("ElementsToReprocess");
                  
                  if(!YFCCommon.isVoid(elementsToReprocess) && elementsToReprocess.contains(";")){
                        String elementsReqd[] = elementsToReprocess.split(";");
                        int noOfElementsToReprocess = elementsReqd.length;
                        
                        for(int index = 0; index<noOfElementsToReprocess; index++){
                              String currentElementName = elementsReqd[index];
                              NodeList elementNL = rootElement.getElementsByTagName(currentElementName);
                              int nodeListLength = elementNL.getLength();
                              
                              for(int listIndex = 0; listIndex<nodeListLength; listIndex++){
                                    Element currElement = (Element) elementNL.item(listIndex);
                                    NamedNodeMap attrsMap = currElement.getAttributes();
                                    int noOfAttrs = attrsMap.getLength();
                                    
									if(currElement!=null){
										for(int attrIndex = 0; attrIndex<noOfAttrs; attrIndex++){
											Node attrNode = attrsMap.item(attrIndex);
											escapeOrReplaceAttrContent(escapeNeeded, regexString, hmRegexCharMap, attrNode);
										}
										escapeOrReplaceTextContent(escapeNeeded, regexString, hmRegexCharMap, currElement);						
									}
                              }
                              
                        }
                  }
            }
			if (log.isVerboseEnabled()) {
				log
						.verbose("********* AcademySpecialCharacter End ******\n"+YFCDocument.getDocumentFor(inXML));
			}
		
            return inXML;
      }

       private void escapeOrReplaceTextContent(String escapeNeeded, String regexString, HashMap<String, String> hmRegexCharMap, Element currElement) {
            if(escapeNeeded.equalsIgnoreCase("Y") || escapeNeeded.equalsIgnoreCase("YES")){
                  if(currElement.getTextContent().contains("&") && hmRegexCharMap.containsKey("&")){
                        String txtContent = currElement.getTextContent();
                        log.verbose("Text Content Before Replace for character '&' is "+txtContent);
                        currElement.setTextContent(txtContent.replaceAll("&", hmRegexCharMap.get("&")));
                        log.verbose("Text Content After Replace for character '&' is "+currElement.getTextContent());
                  }
                  for(Entry<String, String> regexCharEntry: hmRegexCharMap.entrySet()){
                        String regexChar = regexCharEntry.getKey();
                        String txtContent = currElement.getTextContent();
                        if(txtContent.contains(regexChar) && !regexChar.equals("&")){
                              log.verbose("Text Content Before Replace for character '"+regexChar+"' is "+txtContent);
                              currElement.setTextContent(txtContent.replaceAll(regexChar, regexCharEntry.getValue()));
                              log.verbose("Text Content After Replace for character '"+regexChar+"' is "+currElement.getTextContent());
                        }
                  }
            }else if(escapeNeeded.equalsIgnoreCase("N") || escapeNeeded.equalsIgnoreCase("NO")){
                  String txtContent = currElement.getTextContent();
                  log.verbose("Text Content Before Replace with whitespace is "+txtContent);
                  currElement.setTextContent(txtContent.replaceAll(regexString, ""));
                  log.verbose("Text Content After Replace with whitespace is "+currElement.getTextContent());
            }
      }

      private void escapeOrReplaceAttrContent(String escapeNeeded, String regexString, HashMap<String, String> hmRegexCharMap, Node attrNode) {
            if(escapeNeeded.equalsIgnoreCase("Y") || escapeNeeded.equalsIgnoreCase("YES")){
                  if(attrNode.getNodeValue().contains("&") && hmRegexCharMap.containsKey("&")){
                        String attrValue = attrNode.getNodeValue();
                        log.verbose("Attr Content for attribute '"+attrNode.getNodeName()+"' Before Replace for character '&' is "+attrValue);
                        attrNode.setNodeValue(attrValue.replaceAll("&", hmRegexCharMap.get("&")));
                        log.verbose("Attr Content for attribute '"+attrNode.getNodeName()+"' After Replace for character '&' is "+attrNode.getNodeValue());
                  }
                  for(Entry<String, String> regexCharEntry: hmRegexCharMap.entrySet()){
                        String regexChar = regexCharEntry.getKey();
                        String attrValue = attrNode.getNodeValue();
                        if(attrValue.contains(regexChar) && !regexChar.equals("&")){
                              log.verbose("Attr Content for attribute '"+attrNode.getNodeName()+"' Before Replace for character '"+regexChar+"' is "+attrValue);
                              attrNode.setNodeValue(attrValue.replaceAll(regexChar, regexCharEntry.getValue()));
                              log.verbose("Attr Content for attribute '"+attrNode.getNodeName()+"' After Replace for character '"+regexChar+"' is "+attrNode.getNodeValue());
                        }
                  }
            }else if(escapeNeeded.equalsIgnoreCase("N") || escapeNeeded.equalsIgnoreCase("NO")){
                  String attrValue = attrNode.getNodeValue();
                  log.verbose("Attr Content for attribute '"+attrNode.getNodeName()+"' Before Replace with whitespace is "+attrValue);
                  attrNode.setNodeValue(attrValue.replaceAll(regexString, ""));
                  log.verbose("Attr Content for attribute '"+attrNode.getNodeName()+"' After Replace with whitespace is "+attrNode.getNodeValue());
            }
      }
      	  	  
}


