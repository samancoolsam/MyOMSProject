package com.academy.ecommerce.sterling.shipment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;

/**
 * This class will be invoked during container list screen load and fetches the recommended container type of the container
 * based on the ACAD_DIRECTED_PACKAGING_LOOKUP custom table
 * 
 * @author C0028786
 *
 */
public class AcademyGetRecommendedContainer {
    private static final YFCLogCategory log = YFCLogCategory.instance(AcademyGetRecommendedContainer.class);

    /**
     * method to fetch recommended container type from the custom DB
     * @param env
     * @param inXML
     * @return
     * @throws Exception
     */
    public Document getContainerType(YFSEnvironment env, Document inXML) throws Exception {
        log.beginTimer("AcademyGetRecommendedContainer.getContainerType :: Starts ::");
        log.verbose("AcademyGetRecommendedContainer.getContainerType :: input XML ::" + XMLUtil.getXMLString(inXML));
        String strContainerNo = null;
        String strContainerQuantity = null;
        String strItemID = null;
        String strContainerItemID = null;
        String strContainerType = null;
        Document docAcadDirectedPackagingLookupOut = null;
        strContainerNo = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_CONTAINER_NO);
        log.verbose("AcademyGetRecommendedContainer :: strContainerNo ::" + strContainerNo);
        NodeList nlCommonCodeOut = XPathUtil.getNodeList(inXML.getDocumentElement(), AcademyConstants.XPATH_ATTR_COMMON_CODE);
        NodeList nlContainer = (NodeList) inXML.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
        Element eleContainer = (Element) nlContainer.item(0);
        log.verbose("AcademyGetRecommendedContainer :: eleContainer :: " + XMLUtil.getElementXMLString(eleContainer));

        String strRecommendedContainer = "";
        Element eleContainerDetails = SCXmlUtil.getChildElement(eleContainer, AcademyConstants.ELE_CONTAINER_DTLS);
        NodeList nlContainerDetails = eleContainerDetails.getElementsByTagName(AcademyConstants.CONTAINER_DETL_ELEMENT);

        Element eleShipment = SCXmlUtil.getChildElement(eleContainer, AcademyConstants.ELE_SHIPMENT);
        Element eleShipmentLines = SCXmlUtil.getChildElement(eleShipment, AcademyConstants.ELE_SHIPMENT_LINES);
        NodeList nlShipmentLines = eleShipmentLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
        int containerDetailsLength = nlContainerDetails.getLength();
        int shipmentLineLength = nlShipmentLines.getLength();

        for (int contDetl = 0; contDetl < containerDetailsLength; contDetl++) {
            log.verbose("AcademyGetRecommendedContainer :: inside container details loop ::contDetl" + contDetl);

            Element eleContainerDetail = (Element) nlContainerDetails.item(contDetl);
            strContainerItemID = eleContainerDetail.getAttribute(AcademyConstants.ITEM_ID);
            strContainerQuantity = eleContainerDetail.getAttribute(AcademyConstants.ATTR_QUANTITY);
            String strContShipmentLineKey = eleContainerDetail.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);

            for (int shipLine = 0; shipLine < shipmentLineLength; shipLine++) {
                log.verbose("AcademyGetRecommendedContainer :: inside shipment lines loop ::shipLine" + shipLine);
                Element eleShipmentLine = (Element) nlShipmentLines.item(shipLine);
                Element eleItemDetails = (Element) eleShipmentLine.getElementsByTagName(AcademyConstants.ATTR_ITEM_DETAILS).item(0);
                strItemID = eleItemDetails.getAttribute(AcademyConstants.ITEM_ID);
                String strShipmentLineKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
                Element eleItemDetailsExtn = SCXmlUtil.getChildElement(eleItemDetails, AcademyConstants.ELE_ITEM_DETAILS_EXTN);
                log.verbose("AcademyGetRecommendedContainer :: strContainerItemID ::" + strContainerItemID);
                log.verbose("AcademyGetRecommendedContainer :: strItemID ::" + strItemID);

                if (strContShipmentLineKey.equalsIgnoreCase(strShipmentLineKey)) {

                    docAcadDirectedPackagingLookupOut = getSuitableContainer(env, eleItemDetailsExtn, strContainerQuantity, nlCommonCodeOut);
                    if (docAcadDirectedPackagingLookupOut != null) {
                        log.verbose("AcademyGetRecommendedContainer :: docAcadDirectedPackagingLookupOut" + XMLUtil.getXMLString(docAcadDirectedPackagingLookupOut));
                        if (YFCObject.isVoid(strRecommendedContainer)) {
                            log.verbose("AcademyGetRecommendedContainer :: strRecommendedContainer is empty ::");
                            strRecommendedContainer = docAcadDirectedPackagingLookupOut.getDocumentElement().getAttribute(AcademyConstants.ATTR_CONTAINER);
                            eleContainer.setAttribute(AcademyConstants.RECOMMENDED_CONTAINER, strRecommendedContainer);
                            log.verbose("AcademyGetRecommendedContainer :: setting strRecommendedContainer ::" + strRecommendedContainer);
                        } else {
                            log.verbose("AcademyGetRecommendedContainer :: strRecommendedContainer is ::" + strRecommendedContainer);
                            strContainerType = docAcadDirectedPackagingLookupOut.getDocumentElement().getAttribute(AcademyConstants.ATTR_CONTAINER);
                            log.verbose("AcademyGetRecommendedContainer :: strContainerType is ::" + strContainerType);

                            if (strContainerType.equalsIgnoreCase(strRecommendedContainer)) {
                                eleContainer.setAttribute(AcademyConstants.RECOMMENDED_CONTAINER, strContainerType);
                            } else if (!(strContainerType.equalsIgnoreCase(strRecommendedContainer))) {
                                eleContainer.setAttribute(AcademyConstants.RECOMMENDED_CONTAINER, AcademyConstants.STR_EMPTY_STRING);
                            }
                            log.verbose("AcademyGetRecommendedContainer :: Final strRecommendedContainer ::" + strRecommendedContainer);
                        }
                    }
                    break;
                }
            }
        }
        log.endTimer("AcademyGetRecommendedContainer.getContainerType :: End");
        return inXML;

    }

    /**
     * method to set the priority of the custom DB based on the item details and getThresholdForShipmentLine output
     * @param env
     * @param eleItemDetailsExtn
     * @param strContainerQuantity
     * @param nlCommonCodeOut
     * @return
     * @throws Exception
     */
    private Document getSuitableContainer(YFSEnvironment env, Element eleItemDetailsExtn, String strContainerQuantity, NodeList nlCommonCodeOut) throws Exception {
        log.verbose("AcademyGetRecommendedContainer.getSuitableContainer :: Starts");
        String strExtnClass = eleItemDetailsExtn.getAttribute(AcademyConstants.ATTR_EXTN_SIZE);
        String strExtnMultiBox = eleItemDetailsExtn.getAttribute(AcademyConstants.ATTR_EXTN_MULTIBOX);
        String strExtnSizeCodeDescription = eleItemDetailsExtn.getAttribute(AcademyConstants.ATTR_EXTN_SIZECODE);
        String strExtnShipAlone = eleItemDetailsExtn.getAttribute(AcademyConstants.ATTR_EXTN_SHIPALONE);
        Document outDocAcadGetDPLookup = null;
        log.verbose("AcademyGetRecommendedContainer.getSuitableContainer :: strContainerQuantity ::" + strContainerQuantity);
        
        if ((!YFCObject.isVoid(strExtnClass)) && (!YFCObject.isVoid(strExtnSizeCodeDescription)) 
        		&& (!AcademyConstants.ATTR_Y.equalsIgnoreCase(strExtnMultiBox))){ 
            log.verbose("AcademyGetRecommendedContainer.getSuitableContainer :: strExtnMultiBox :: N");

            if (AcademyConstants.ATTR_Y.equalsIgnoreCase(strExtnShipAlone)) {
                log.verbose("AcademyGetRecommendedContainer.getSuitableContainer :: ExtnShipAlone :: Y");

                outDocAcadGetDPLookup = prepareAndInvokeDPLookup(env, strExtnClass, strExtnSizeCodeDescription,
                    AcademyConstants.ATTR_Y, AcademyConstants.ATTR_N, AcademyConstants.ATTR_N);
            }
            else if (AcademyConstants.ATTR_N.equalsIgnoreCase(strExtnShipAlone)) {
                log.verbose("AcademyGetRecommendedContainer.getSuitableContainer :: ExtnShipAlone :: N");
                String strLow = "N";
                String strMedium = "N";
                String strHigh = "N";
                String strThreshold = getThresholdForShipmentLine(strContainerQuantity, nlCommonCodeOut);
                log.verbose("AcademyGetRecommendedContainer.getSuitableContainer :: strThreshold ::" + strThreshold);
                
               if(!YFCObject.isVoid(strThreshold)){
            	   
                if (AcademyConstants.ATTR_LOW.equalsIgnoreCase(strThreshold)) {
                    strLow = AcademyConstants.STR_YES;
                } else if (AcademyConstants.ATTR_MEDIUM.equalsIgnoreCase(strThreshold)) {
                    strMedium = AcademyConstants.STR_YES;
                } else if (AcademyConstants.ATTR_HIGH.equalsIgnoreCase(strThreshold)) {
                    strHigh = AcademyConstants.STR_YES;
                }
                
                outDocAcadGetDPLookup = prepareAndInvokeDPLookup(env, strExtnClass, strExtnSizeCodeDescription, strLow, strMedium, strHigh);
            }
            }
        }
        log.verbose("AcademyGetRecommendedContainer.getSuitableContainer :: Ends");
        return outDocAcadGetDPLookup;

    }

    /**
     * method to prepare input document and invoke custom DB of getting container recommendation via service AcadGetDPLookup
     * @param env
     * @param strExtnClass
     * @param strExtnSizeCodeDescription
     * @param strLow
     * @param strMedium
     * @param strHigh
     * @return
     * @throws Exception
     */
    private Document prepareAndInvokeDPLookup(YFSEnvironment env, String strExtnClass, String strExtnSizeCodeDescription, String strLow,
        String strMedium, String strHigh) throws Exception {
        log.verbose("AcademyGetRecommendedContainer.prepareAndInvokeDPLookup :: Starts");

        Document docDPlookupInput = XMLUtil.createDocument("AcadDirectedPackagingLookup");
        Element eleAcadDirectedPackagingLookup = docDPlookupInput.getDocumentElement();
        eleAcadDirectedPackagingLookup.setAttribute(AcademyConstants.ATTR_PKG_CLASS, strExtnClass);
        eleAcadDirectedPackagingLookup.setAttribute(AcademyConstants.ATTR_SIZE, strExtnSizeCodeDescription);
        eleAcadDirectedPackagingLookup.setAttribute(AcademyConstants.ATTR_LOW, strLow);
        eleAcadDirectedPackagingLookup.setAttribute(AcademyConstants.ATTR_MEDIUM, strMedium);
        eleAcadDirectedPackagingLookup.setAttribute(AcademyConstants.ATTR_HIGH, strHigh);
        Document outDocAcadGetDPLookup = AcademyUtil.invokeService(env, "AcadGetDPLookup", docDPlookupInput);
        if (outDocAcadGetDPLookup != null) {
            log.verbose("prepareAndInvokeDPLookup :: outDocAcadGetDPLookup ::" + XMLUtil.getXMLString(outDocAcadGetDPLookup));
        }
        log.verbose("AcademyGetRecommendedContainer.prepareAndInvokeDPLookup :: Ends");
        return outDocAcadGetDPLookup;
    }

    /**
     * method to assign priority based on the container quantity
     * @param strContainerQuantity
     * @param nlCommonCodeOut
     * @return
     * @throws Exception
     */
    private String getThresholdForShipmentLine(String strContainerQuantity, NodeList nlCommonCodeOut) {
        log.verbose("AcademyGetRecommendedContainer.getThresholdForShipmentLine :: Starts");
        String strThresholdShipLine = null;
        if (nlCommonCodeOut != null) {
        	for (int i = 0; i < nlCommonCodeOut.getLength(); i++) {
        		
                    log.verbose("Inside For loop::" + i);
                    Element eleCommonCode = (Element) nlCommonCodeOut.item(i);
                    log.verbose("AcademyGetRecommendedContainer.getThresholdForShipmentLine :: eleCommonCode :: " + XMLUtil.getElementXMLString(eleCommonCode));
                    if (null != eleCommonCode) {
                        String strThresholdVal = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
                        log.verbose("Threshold Type::" + strThresholdVal);

                        String strCodeValue = eleCommonCode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
                        log.verbose("Threshold Value::" + strCodeValue);

                        String[] strThresholdArray = strThresholdVal.split("-");
                        String strMin = "";
                        String strMax = "";

                        if (strThresholdArray.length > 1) {
                            strMin = strThresholdArray[0];
                            strMax = strThresholdArray[1];
                        } else {
                            strMin = strThresholdArray[0];
                        }

                        if (!YFCObject.isVoid(strMin) && null != strMin && (YFCObject.isVoid(strMax) || null == strMax)) {

                            if (Double.parseDouble(strContainerQuantity) == Double.parseDouble(strMin)) {
                                strThresholdShipLine = strCodeValue;
                                log.verbose("Set Threshold ::" + strThresholdShipLine);
                                break;
                            }

                        } else if (!YFCObject.isVoid(strMin) && null != strMin &&
                            !YFCObject.isVoid(strMax) && null != strMax) {
                            log.verbose("::strContainerQuantity ::" + strContainerQuantity);
                            log.verbose("::strMin ::" + strMin);
                            log.verbose("::strMax ::" + strMax);

                            if (Double.parseDouble(strContainerQuantity) >= Double.parseDouble(strMin) &&
                                Double.parseDouble(strContainerQuantity) <= Double.parseDouble(strMax)) {

                                strThresholdShipLine = strCodeValue;
                                log.verbose("Set Threshold ::" + strThresholdShipLine);
                                break;
                            }
                        }
                    }
                }
            }
        log.verbose("AcademyGetRecommendedContainer.getThresholdForShipmentLine :: Ends");
        return strThresholdShipLine;
    }

}