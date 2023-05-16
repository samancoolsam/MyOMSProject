package com.academy.ecommerce.sterling.los;

import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyGetLosRestrictionAPI implements YIFCustomApi {
	YFCLogCategory log = YFCLogCategory
			.instance(AcademyGetLosRestrictionAPI.class);

	public void setProperties(Properties arg0) throws Exception {

	}

	public Document updateOrderLineWithLos(YFSEnvironment env, Document inDoc) {
		log.beginTimer(" Begin of AcademyGetLosRestrictionAPI ->updateOrderLineWithLos Api");
		if(log.isVerboseEnabled()){
			log.verbose("Entering method : updateOrderLineWithLos for the API :AcademyGetLosRestrictionAPI");
		}
		Element orderElem = inDoc.getDocumentElement();
		Element orderLinesElem = (Element) orderElem.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINES).item(0);
		NodeList listOfLines = orderLinesElem.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINE);
		int length = listOfLines.getLength();
		for (int i = 0; i < length; i++) {
			Element orderlineElem = (Element) listOfLines.item(i);
			Document losList = getLosRestriction(env, orderlineElem);
			if (losList != null && losList.hasChildNodes()) {
				setLosValues(losList, orderlineElem,inDoc);
			} else {
				if(log.isVerboseEnabled()){
					log.verbose("Los List returned is null");
				}
			}
		}
		if(log.isVerboseEnabled()){
			log.verbose("Output of the API : "+XMLUtil.getElementXMLString(inDoc.getDocumentElement()));
		}
		log.endTimer(" End of AcademyGetLosRestrictionAPI ->updateOrderLineWithLos Api");
		return inDoc;
	}

	private void setLosValues(Document losList, Element orderlineElem, Document inDoc) {
		
		Element losRootElem=losList.getDocumentElement();
		log.beginTimer(" Begin of AcademyGetLosRestrictionAPI ->setLosValues Api");
		if(losRootElem!=null){
			try {
				Document cloneDoc=XMLUtil.cloneDocument(losList);
				Element cloneDocElem=cloneDoc.getDocumentElement();
				cloneDoc.removeChild(cloneDocElem);
				Node tempNode=inDoc.importNode(cloneDocElem, true);
				orderlineElem.appendChild(tempNode);
			} catch (Exception e) {
				
				e.printStackTrace();
				
			}
		
		}else{
			
		}
		log.endTimer(" End of AcademyGetLosRestrictionAPI ->setLosValues Api");
	}

	private Document getLosRestriction(YFSEnvironment env, Element orderlineElem) {
		Document retDoc=null;
		try {
			log.beginTimer(" Begin of AcademyGetLosRestrictionAPI ->getLosRestriction Api");
			Document acadLosRestrictionDoc = XMLUtil
					.createDocument(AcademyConstants.ELEM_ACAD_LOS_REST);
			String itemType = getItemType(orderlineElem);
			acadLosRestrictionDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, itemType);
			Element personInfoShipTo=(Element) orderlineElem.getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).item(0);
			Element extnElemForPersonInfo=(Element) personInfoShipTo.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
			acadLosRestrictionDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_IS_APO_FPO, extnElemForPersonInfo.getAttribute(AcademyConstants.ATTR_EXTN_IS_APO_FPO));
			acadLosRestrictionDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_IS_PO_BOX, extnElemForPersonInfo.getAttribute(AcademyConstants.ATTR_EXTN_IS_PO_BOX));
			if(extnElemForPersonInfo.getAttribute(AcademyConstants.ATTR_EXTN_IS_APO_FPO).equals("N")&&extnElemForPersonInfo.getAttribute(AcademyConstants.ATTR_EXTN_IS_PO_BOX).equals("N")){
				acadLosRestrictionDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_IS_REGULAR_ADDRESS, "Y");
			}else{
				acadLosRestrictionDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_IS_REGULAR_ADDRESS, "N");
			}
			
			retDoc=AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_GET_LOS_RESTRICTION_LIST, acadLosRestrictionDoc);
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.endTimer(" End of AcademyGetLosRestrictionAPI ->getLosRestriction Api");
		return retDoc;
	}

	private String getItemType(Element orderlineElem) {
		log.beginTimer(" Begin of AcademyGetLosRestrictionAPI ->getItemType Api");
		Element itemElem, itemExtnElem, itemPrimaryInfo = null;
		itemElem = (Element) orderlineElem.getElementsByTagName(AcademyConstants.ELE_ITEM_DETAILS).item(0);
		if (itemElem != null && itemElem.hasChildNodes()) {
			itemExtnElem = (Element) itemElem.getElementsByTagName(AcademyConstants.ELE_EXTN)
					.item(0);
			if (itemExtnElem != null && itemExtnElem.hasAttributes()) {
				
				if (itemExtnElem.getAttribute(AcademyConstants.ATTR_EXTN_IS_GIFT_CARD).equals("Y")) {
					log.endTimer(" End of AcademyGetLosRestrictionAPI ->getItemType Api");
					return AcademyConstants.GIFT_CARD;
				}
				if (itemExtnElem.getAttribute(AcademyConstants.ATTR_EXTN_WHITE_GLOVE_ELIGIBLE).equals(
						"Y")) {
					log.endTimer(" End of AcademyGetLosRestrictionAPI ->getItemType Api");
					return AcademyConstants.WHITE_GLOVE;
				}
				if (itemExtnElem.getAttribute(AcademyConstants.ATTR_EXTN_KIT).equals("Y")) {
					log.endTimer(" End of AcademyGetLosRestrictionAPI ->getItemType Api");
					return AcademyConstants.BULK_KIT;
				}
				if (itemExtnElem.getAttribute(AcademyConstants.ATTR_EXTN_SHIP_ALONE).equals("Y")
						&& itemExtnElem.getAttribute(AcademyConstants.ATTR_EXTN_CONVEYABLE).equals(
								"N")) {
					log.endTimer(" End of AcademyGetLosRestrictionAPI ->getItemType Api");
					return AcademyConstants.BULK_KIT;
				}
				
			}

			itemPrimaryInfo = (Element) itemElem.getElementsByTagName(AcademyConstants.ELE_PRIMARY_INFO).item(0);
			if (itemPrimaryInfo != null && itemPrimaryInfo.hasAttributes()) {
				if (itemPrimaryInfo.getAttribute(AcademyConstants.ATTR_IS_HAZMAT).equals("Y")) {
					log.endTimer(" End of AcademyGetLosRestrictionAPI ->getItemType Api");
					return AcademyConstants.HAZMAT;
				}
			}
			if (itemExtnElem.getAttribute(AcademyConstants.ATTR_EXTN_CONVEYABLE).equals("Y")) {
				log.endTimer(" End of AcademyGetLosRestrictionAPI ->getItemType Api");
				return AcademyConstants.CONVEYABLE;
			}

		}
		log.endTimer(" End of AcademyGetLosRestrictionAPI ->getItemType Api");
		return "";
	}

}
