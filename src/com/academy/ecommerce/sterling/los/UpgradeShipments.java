package com.academy.ecommerce.sterling.los;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class UpgradeShipments 
{
	private static YFCLogCategory log = YFCLogCategory.instance(UpgradeShipments.class);
	private Properties props;
	
	public void setProperties(Properties props)
	{
		this.props = props;

	}
	
	String strFEDEX = "";
	String strGround ="";
	String strHomeDelivery = "";
	String strStandardOvernight = "";
	String strSmartPost = "";
	String str2Day = "";
	
	public Document upgradeShipments(YFSEnvironment env, Document inDoc) throws Exception
	{
		log.verbose("inside upgrade shipments"+XMLUtil.getXMLString(inDoc));
		
		strFEDEX = props.getProperty("FEDEX");
		strGround = props.getProperty("Ground");
		strHomeDelivery = props.getProperty("Home Delivery");
		strStandardOvernight = props.getProperty("Standard Overnight");
		strSmartPost = props.getProperty("Smart Post");
		str2Day = props.getProperty("2Day");
		
		Element eleShipment = inDoc.getDocumentElement();
		String strShipmentKey = eleShipment.getAttribute("ShipmentKey");
		eleShipment.setAttribute("UpgradeShipments", "Y");
		Document docPierbridgeResposnse = AcademyUtil.invokeService(env, "AcademyRateShopService", inDoc);
		log.verbose("***docPierbridgeResposnse***"+XMLUtil.getXMLString(docPierbridgeResposnse));
		Element eleExtnShipment = (Element)eleShipment.getElementsByTagName("Extn").item(0);
		String extnCarrierServiceCode = eleExtnShipment.getAttribute("ExtnOriginalShipmentLos");
		String extnSCAC = eleExtnShipment.getAttribute("ExtnOriginalShipmentScac");
		
		Document resultDoc = getCheapestCarrierService(env, extnSCAC, extnCarrierServiceCode, inDoc, docPierbridgeResposnse);
		if (!YFCObject.isNull(resultDoc))
		{
			log.verbose("***resultDoc***"+XMLUtil.getXMLString(resultDoc));
			Element eleRoot = resultDoc.getDocumentElement();
			YFCDocument docChangeShipment = YFCDocument.createDocument("Shipment");//.getDocument()
			YFCElement eleOutShipment = docChangeShipment.getDocumentElement();
			eleOutShipment.setAttribute("ShipmentKey", strShipmentKey);
			eleOutShipment.setAttribute("SCAC", eleRoot.getAttribute("SCAC"));
			eleOutShipment.setAttribute("CarrierServiceCode", eleRoot.getAttribute("CarrierService"));
			YFCElement eleoutExtn = docChangeShipment.createElement("Extn");
			eleoutExtn.setAttribute("ExtnShipUpgrdOrDowngrd", eleRoot.getAttribute("ShipUpgrdOrDowngrd"));
			eleOutShipment.appendChild(eleoutExtn);
			Document changeShipmentOut = null;
			/*  OMNI-90977 - START - Remove Upgrade/downgrade alerts
			if("U".equals(eleRoot.getAttribute("ShipUpgrdOrDowngrd")))
			{
				log.verbose("***changeShipment input***"+XMLUtil.getXMLString(docChangeShipment.getDocument()));
				changeShipmentOut =  AcademyUtil.invokeAPI(env, "changeShipment", docChangeShipment.getDocument());
				log.verbose("***changeShipment output***"+XMLUtil.getXMLString(changeShipmentOut));
				Document alertDoc = XMLUtil.createDocument("Shipment");
				Element eleAlertShipment = alertDoc.getDocumentElement();
				eleAlertShipment.setAttribute("ShipmentNo", eleShipment.getAttribute("ShipmentNo"));
				eleAlertShipment.setAttribute("ShipmentKey", strShipmentKey);
				eleAlertShipment.setAttribute("SCAC", eleShipment.getAttribute("SCAC"));
				eleAlertShipment.setAttribute("CarrierServiceCode", eleShipment.getAttribute("CarrierServiceCode"));
				eleAlertShipment.setAttribute("ChangedSCAC", eleRoot.getAttribute("SCAC"));
				eleAlertShipment.setAttribute("ChangedCarrierServiceCode", eleRoot.getAttribute("CarrierService"));
				eleAlertShipment.setAttribute("UpgradedOrDowngraded", eleRoot.getAttribute("ShipUpgrdOrDowngrd"));
				log.verbose("***alertDoc***"+XMLUtil.getXMLString(alertDoc));
				AcademyUtil.invokeService(env, "RaiseAlertForChangeInLOS", alertDoc);
			}			
			OMNI-90977 - END - Remove Upgrade/downgrade alerts*/		
			return changeShipmentOut;
		}
		else
		{
			YFCDocument docChangeShipment = YFCDocument.createDocument("Shipment");
			return docChangeShipment.getDocument();
		}
	}
	
	public Document getCheapestCarrierService(YFSEnvironment env, String inSCAC, String inCarrierServiceCode, Document inDoc, Document peirbridgeResponse) throws Exception
	{
		log.verbose("***inside getCheapestCarrierService***");
		NodeList rateLt = peirbridgeResponse.getElementsByTagName("Rate");
		Element tempRateElement = null;
		Document outDoc = YFCDocument.createDocument("Out").getDocument();
		
		Document getCommonCodeListInputXML = XMLUtil.createDocument("CommonCode");
		getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeType", "PriorityCarrier");
		getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeValue", inCarrierServiceCode);
		Document outXML = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML);
		
		NodeList commonCodeList = outXML.getElementsByTagName("CommonCode");
		String strCarrierPriority = "";

		if (commonCodeList != null && !YFCObject.isVoid(commonCodeList))
		{
			Element commonCode = (Element)commonCodeList.item(0);
			String codeValue = commonCode.getAttribute("CodeValue");
			if (inCarrierServiceCode.equals(codeValue))
			{
				strCarrierPriority = commonCode.getAttribute("CodeShortDescription");
				log.verbose("strCarrierPriority is: " + strCarrierPriority);
			}
		}
		
		if(rateLt.getLength()==1)
		{
			tempRateElement = (Element)rateLt.item(0);
			String scac = tempRateElement.getElementsByTagName("SCAC").item(0).getTextContent();
			if(scac.contains(strFEDEX))
			{
				scac="FEDX";
			}
			if("UPS".equals(scac))
			{
				scac="UPSN";
			}
			Element serviceType = (Element)tempRateElement.getElementsByTagName("ServiceType").item(0);
			String carrierService = serviceType.getElementsByTagName("Description").item(0).getTextContent();
			
			
			
			/*if(carrierService.contains(strHomeDelivery))
			{
				if("".equals(XMLUtil.getString(inDoc, "Shipment/ToAddress/@Company")))
				{
					carrierService = strHomeDelivery;
				}
				else
				{
					//carrierService = strGround;
					log.verbose("***** Making the outDoc null ****** ");
					outDoc = null;
					return outDoc;
				}
			}
			else if(carrierService.contains(strGround))
			{
				if("".equals(XMLUtil.getString(inDoc, "Shipment/ToAddress/@Company")))
				{
					//carrierService = strHomeDelivery;
					log.verbose("***** Making the outDoc null ****** ");
					outDoc = null;
					return outDoc;
				}
				else
				{
					carrierService = strGround;
				}
			}*/
			if(carrierService.contains(strHomeDelivery))
			{
				if("".equals(XMLUtil.getString(inDoc, "Shipment/ToAddress/@Company")))
				{
					carrierService = strHomeDelivery;
				}
				else
				{
					Element eleShipment = inDoc.getDocumentElement();
					eleShipment.setAttribute("Exceptional", "Y");
					Document docPierbridgeResposnse = AcademyUtil.invokeService(env, "AcademyRateShopService", inDoc);
					NodeList rateLt1 = docPierbridgeResposnse.getElementsByTagName("Rate");
					boolean isGd = false;
					for(int i=0; i<rateLt1.getLength(); i++)
					{
						Element eleRate = (Element)rateLt1.item(i);
						Element eleServiceType = (Element)eleRate.getElementsByTagName("ServiceType").item(0);
						Element eleDescription = (Element)eleServiceType.getElementsByTagName("Description").item(0);
						String strDescription = eleDescription.getTextContent();
						if(strDescription.contains("Ground"))
						{
							carrierService = "Ground";
							isGd = true;
							break;
						}
					}
					
					//carrierService = strGround;
					if(!isGd)
					{
						log.verbose("***** Making the outDoc null ****** ");
						outDoc = null;
					}
				}
			}
			else if(carrierService.contains(strGround))
			{
				if("".equals(XMLUtil.getString(inDoc, "Shipment/ToAddress/@Company")))
				{					
					Element eleShipment = inDoc.getDocumentElement();
					eleShipment.setAttribute("Exceptional", "Y");
					Document docPierbridgeResposnse = AcademyUtil.invokeService(env, "AcademyRateShopService", inDoc);
					NodeList rateLt1 = docPierbridgeResposnse.getElementsByTagName("Rate");
					boolean isHD = false;
					for(int i=0; i<rateLt1.getLength(); i++)
					{
						Element eleRate = (Element)rateLt1.item(i);
						Element eleServiceType = (Element)eleRate.getElementsByTagName("ServiceType").item(0);
						Element eleDescription = (Element)eleServiceType.getElementsByTagName("Description").item(0);
						String strDescription = eleDescription.getTextContent();
						if(strDescription.contains("Home Delivery"))
						{
							carrierService = "Home Delivery";
							isHD = true;
							break;
						}
					}
					
					//carrierService = strHomeDelivery;
					if(!isHD)
					{
						log.verbose("***** Making the outDoc null ****** ");
						outDoc = null;
					}
				}
				else
				{
					carrierService = strGround;
				}
			}
			else if(carrierService.contains(str2Day))
			{
				carrierService = "2 Day";
			}
			else if(carrierService.contains(strStandardOvernight))
			{
				carrierService = strStandardOvernight;
			}
			else if(carrierService.contains(strSmartPost))
			{
				carrierService = strSmartPost;
			}
			else if(carrierService.contains("2nd Day Air"))
			{
				carrierService = "2nd Day Air";
			}
			else if(carrierService.contains("Next Day Air"))
			{
				carrierService = "Next Day Air";
			}
			
			
			Document getCommonCodeListInputXML1 = XMLUtil.createDocument("CommonCode");
			getCommonCodeListInputXML1.getDocumentElement().setAttribute("CodeType", "PriorityCarrier");
			getCommonCodeListInputXML1.getDocumentElement().setAttribute("CodeValue", carrierService);
			Document outXML1 = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML1);
			NodeList commonCodeList1 = outXML1.getElementsByTagName("CommonCode");
			String strCarrierPriority1 = "";

			if (commonCodeList1 != null && !YFCObject.isVoid(commonCodeList1))
			{
				Element commonCode = (Element)commonCodeList1.item(0);
				String codeValue = commonCode.getAttribute("CodeValue");
				if (carrierService.equals(codeValue))
				{
					strCarrierPriority1 = commonCode.getAttribute("CodeShortDescription");
					log.verbose("strCarrierPriority is: " + strCarrierPriority1);
				}
				log.verbose("strCarrier" + carrierService);
				outDoc.getDocumentElement().setAttribute("SCAC", scac);
				outDoc.getDocumentElement().setAttribute("CarrierService", carrierService);	
				
				int i = Integer.parseInt(strCarrierPriority);			
				int i1 = Integer.parseInt(strCarrierPriority1);
				
				if(i>i1)
				{
					outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","D");
				}
				else if(i<i1)
				{
					outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","U");
				}
			}
			//int carrierLength = carrierService.length();
			//String strCarrier = carrierService.substring(0,carrierLength-2);
			//int iFirstSpaceOccu = strCarrier.indexOf(" ");
			//strCarrier = strCarrier.substring(++iFirstSpaceOccu);
			
			
		}
		else
		{
			Element eleShipment = inDoc.getDocumentElement();
			eleShipment.setAttribute("Exceptional", "Y");
			Document docPierbridgeResposnse = AcademyUtil.invokeService(env, "AcademyRateShopService", inDoc);
			
			Document getCommonCodeListXML = XMLUtil.createDocument("CommonCode");
			getCommonCodeListXML.getDocumentElement().setAttribute("CodeType", "PriorityCarrier");
			Document outgetCommonCodeListXML = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListXML);
			
			String strServiceLevel = getHighest(inDoc, docPierbridgeResposnse, outgetCommonCodeListXML);
			
			if(!("".equals(strServiceLevel)))
			{
				outDoc.getDocumentElement().setAttribute("SCAC", "FEDX");
				outDoc.getDocumentElement().setAttribute("CarrierService", strServiceLevel);
				Document getCommonCodeListInputXML1 = XMLUtil.createDocument("CommonCode");
				getCommonCodeListInputXML1.getDocumentElement().setAttribute("CodeType", "PriorityCarrier");
				getCommonCodeListInputXML1.getDocumentElement().setAttribute("CodeValue", strServiceLevel);
				Document outXML1 = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML1);
				NodeList commonCodeList1 = outXML1.getElementsByTagName("CommonCode");
				String strCarrierPriority1 = "";
	
				if (commonCodeList1 != null && !YFCObject.isVoid(commonCodeList1))
				{
					Element commonCode = (Element)commonCodeList1.item(0);
					String codeValue = commonCode.getAttribute("CodeValue");
					if (strServiceLevel.equals(codeValue))
					{
						strCarrierPriority1 = commonCode.getAttribute("CodeShortDescription");
						log.verbose("strCarrierPriority is: " + strCarrierPriority1);
					}
					int i = Integer.parseInt(strCarrierPriority);			
					int i1 = Integer.parseInt(strCarrierPriority1);
					
					if(i>i1)
					{
						outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","D");
					}
					else if(i<i1)
					{
						outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","U");
					}
				}
				
			}
			else
			{
				log.verbose("***** Making the outDoc null ****** ");
				outDoc = null;
			}
		}
		
		if (!YFCObject.isNull(outDoc))
		{
			log.verbose("***OutDoc***" + XMLUtil.getXMLString(outDoc));
		}
		return outDoc;
	}
	
	public ArrayList <String> inputServiceLevels = new ArrayList <String>();
	public String finalServiceLevel = "";
	public String getHighest(Document docShipment,Document docPierbridgeResposnse,Document outgetCommonCodeListXML) throws Exception 
	{
		XPath xpath = XPathFactory.newInstance().newXPath();

		NodeList rateLt = docPierbridgeResposnse.getElementsByTagName("Rate");
		if(rateLt.getLength()!=0)
		{
			ArrayList <Integer> rateValues = new ArrayList <Integer>();
			for(int i=0; i<rateLt.getLength(); i++)
			{
				Element eleRate = (Element)rateLt.item(i);
				Element eleServiceType = (Element)eleRate.getElementsByTagName("ServiceType").item(0);
				Element eleDescription = (Element)eleServiceType.getElementsByTagName("Description").item(0);
				String strDescription = eleDescription.getTextContent();
				//log.verbose("***strDescription***"+strDescription);

				if(strDescription.contains(strHomeDelivery))
				{
					strDescription = strHomeDelivery;
					inputServiceLevels.add(strDescription);
				}
				else if(strDescription.contains(strGround))
				{
					strDescription = strGround;
					inputServiceLevels.add(strDescription);
				}
				
				else if(strDescription.contains(strStandardOvernight))
				{
					strDescription = strStandardOvernight;
					inputServiceLevels.add(strDescription);
				}
				else if(strDescription.contains(str2Day))
				{
					strDescription = "2 Day";
					inputServiceLevels.add(strDescription);
				}
				else if(strDescription.contains("strSmartPost"))
				{
					strDescription = strSmartPost;
					inputServiceLevels.add(strDescription);
				}
				else if(strDescription.contains("2nd Day Air"))
				{
					strDescription = "2nd Day Air";
					inputServiceLevels.add(strDescription);
				}
				else if(strDescription.contains("Next Day Air"))
				{
					strDescription = "Next Day Air";
					inputServiceLevels.add(strDescription);
				}


				XPathExpression expr= xpath.compile("CommonCodeList/CommonCode[@CodeValue=\"" + strDescription + "\"]");
				Element eleCommonCode = (Element) expr.evaluate(outgetCommonCodeListXML, XPathConstants.NODE);
				if(!YFCObject.isVoid(eleCommonCode))
				{
					String strCodeShortDescription = eleCommonCode.getAttribute("CodeShortDescription");
					//log.verbose("***strCodeShortDescription***"+strCodeShortDescription);
					rateValues.add(Integer.parseInt(strCodeShortDescription)); 					
				}				
			}
			
			
			int maxValue = Collections.max(rateValues);
			XPathExpression expr1= xpath.compile("CommonCodeList/CommonCode[@CodeShortDescription=\"" + String.valueOf(maxValue) + "\"]");
			NodeList commonCodeNL = (NodeList) expr1.evaluate(outgetCommonCodeListXML, XPathConstants.NODESET);
			log.verbose("***length***"+commonCodeNL.getLength());
			for(int j=0; j<commonCodeNL.getLength(); j++)
			{
				Element eleCommonCode = (Element)commonCodeNL.item(j);
				String strCodeValue = eleCommonCode.getAttribute("CodeValue");
				log.verbose("***CodeValue***"+strCodeValue);
				if(inputServiceLevels.contains(strCodeValue))
				{
					if(strCodeValue.equals("Ground"))
					{
						//log.verbose("inside Ground");
						if(!("".equals(XMLUtil.getString(docShipment, "Shipment/ToAddress/@Company"))))
						{
							//log.verbose("***Company inside Ground***"+XMLUtil.getString(docShipment, "Shipment/ToAddress/@Company"));
							finalServiceLevel = "Ground";
						}
						/*else
						{
							//log.verbose("***Company inside Ground***"+XMLUtil.getString(docShipment, "Shipment/ToAddress/@Company"));
							finalServiceLevel = "Home Delivery";
						}*/
					}
					else if(strCodeValue.equals("Home Delivery"))
					{
						//log.verbose("inside Home Delivery");
						if("".equals(XMLUtil.getString(docShipment, "Shipment/ToAddress/@Company")))
						{
							//log.verbose("***Company inside Home Delivery***"+XMLUtil.getString(docShipment, "Shipment/ToAddress/@Company"));
							finalServiceLevel = "Home Delivery";
						}
						/*else
						{
							//log.verbose("***Company inside Ground***"+XMLUtil.getString(docShipment, "Shipment/ToAddress/@Company"));
							finalServiceLevel = "Ground";
						}*/
					}
					else
					{
						finalServiceLevel = strCodeValue;
					}						
				}
			}
		}
		else
		{
			finalServiceLevel = "";
		}
		log.verbose("***finalServiceLevel***"+finalServiceLevel);
		return finalServiceLevel;
	}
}
