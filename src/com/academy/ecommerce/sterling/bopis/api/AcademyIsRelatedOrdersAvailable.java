package com.academy.ecommerce.sterling.bopis.api;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyIsRelatedOrdersAvailable {
private static final YFCLogCategory log = YFCLogCategory.instance(AcademyIsRelatedOrdersAvailable.class);

/**
 * This method determines whether an order has related order.
 * @param env
 * @param inDoc
 * Sample IP : <Shipment ShipmentNo="" OrderNo="" EmailID="" Status="" ShipNode=""/>
 * @return
 * Sample OP : <Shipment NotifyRelatedOrders=Y/N CodeLongDescription=IL,FL CodeShortDescription=Y/N/>
 */

public Document isRelatedOrdersAvailable(YFSEnvironment env, Document inDoc) {
log.beginTimer(this.getClass() + ".isRelatedOrdersAvailable");
log.verbose("AcademyIsRelatedOrdersAvailable.java ::InDoc " + XMLUtil.getXMLString(inDoc));

  Document docOutput = null;
  Document docRFCPListOut = null;
  String strGetCommonCodeListInput = null;
  String strShowRelatedOrders = null;
  String strShipNode = null;
  String strEmailID = null;
  String strRFCPCount = null;
  
 
  AcademySearchShipmenNoOrderNoFromWSC getRFCPObj = new AcademySearchShipmenNoOrderNoFromWSC();
  //OMNI-90544 START
   String strCodeLongDesc = null;
   String strCodeShortDesc = null;
   Document docGetCommonCodeListOutput;
    //OMNI-90544 END
 try {
      docOutput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
      strShipNode = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE);
      strEmailID = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_EMAIL_ID);
      Document docGetCommonCodeListInput = getCommonCodeListInput();
      docGetCommonCodeListOutput = AcademyUtil.invokeAPI(env,
      AcademyConstants.API_GET_COMMON_CODELIST, docGetCommonCodeListInput);
      //OMNI-90544 START
      Element eleRelatedOrders = (Element) XPathUtil.getNode(docGetCommonCodeListOutput, "/CommonCodeList/CommonCode[@CodeValue='"+"ENABLE_SHOW_RELATED_ORDERS"+"']");
      Element eleRestrictStore = (Element) XPathUtil.getNode(docGetCommonCodeListOutput, "/CommonCodeList/CommonCode[@CodeValue='"+"RESTRICT_PICKEDUP_STSFA"+"']");
      strShowRelatedOrders = eleRelatedOrders.getAttribute(AcademyConstants.XPATH_COMMON_CODE_SHORT_DESC);
      if(!YFCCommon.isVoid(eleRestrictStore)){
         strCodeShortDesc = eleRestrictStore.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
         strCodeLongDesc=eleRestrictStore.getAttribute(AcademyConstants.CODE_LONG_DESC);
         docOutput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC,strCodeShortDesc);
         docOutput.getDocumentElement().setAttribute(AcademyConstants.CODE_LONG_DESC,strCodeLongDesc);
    }
    //OMNI-90544 END
     log.verbose("GetCommonCodeList Output ::" +XMLUtil.getXMLString(docGetCommonCodeListOutput)
     + "\nshipNode: " + strShipNode + "\nemailId:  " + strEmailID + "\nShowRelatedOrders" +strShowRelatedOrders);

    if(!YFCObject.isVoid(strShowRelatedOrders) && strShowRelatedOrders.equals(AcademyConstants.STR_YES)) {
       docRFCPListOut = getRFCPObj.verifyAndGetRFCPOrderList(env, strShipNode, strEmailID, null);
       log.verbose("AcademyIsRelatedOrdersAvailable.java ::RFCP and Related Orders List Output "
       + XMLUtil.getXMLString(docRFCPListOut));
       if(!YFCObject.isVoid(docRFCPListOut)) {
          strRFCPCount = docRFCPListOut.getDocumentElement().getAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS);
          log.verbose("The number of shipments is:" +strRFCPCount);
          if(!YFCObject.isVoid(strRFCPCount) && Integer.parseInt(strRFCPCount) > 1) {
             docOutput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHOW_RELATED_ORDERS, AcademyConstants.STR_YES);
             }else {
                    docOutput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHOW_RELATED_ORDERS, AcademyConstants.STR_NO);
             }
         }
          }else {
               docOutput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHOW_RELATED_ORDERS, AcademyConstants.STR_NO);
              }

             } catch (Exception e) {
               e.printStackTrace();
             } 
                log.verbose("AcademyIsRelatedOrdersAvailable.java ::outDoc " + XMLUtil.getXMLString(docOutput));
                log.endTimer(this.getClass() + ".isRelatedOrdersAvailable");
                return docOutput;
         }
           /** OMNI-90544 START
           This method is to create common code input using complex query 
          
       
          <CommonCode CodeType="TGL_RCP_WEB_SOM_UI">
          <ComplexQuery Operator="AND"> 
          <Or> 
          <Exp Name="CodeValue" QryType="EQ" Value="RESTRICT_PICKEDUP_STSFA"/> 
          <Exp Name="CodeValue" QryType="EQ" Value="ENABLE_SHOW_RELATED_ORDERS"/> 
         </Or> 
         </ComplexQuery> 
         </CommonCode>
           */
         private Document getCommonCodeListInput() throws ParserConfigurationException {
           Document docGetCommonCodeListIn = null;
           Element eleComplexQuery = null;
           Element eleAnd = null;
           Element eleOr = null;
           Element eleExp = null;
           docGetCommonCodeListIn = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
           Element eleGetCommonCodeListIn = docGetCommonCodeListIn.getDocumentElement();
           eleGetCommonCodeListIn.setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.V_TGL_RCP_WEB_SOM_UI);
           eleComplexQuery = docGetCommonCodeListIn.createElement(AcademyConstants.COMPLEX_QRY_ELEMENT);
           eleAnd = docGetCommonCodeListIn.createElement(AcademyConstants.COMPLEX_AND_ELEMENT);
           eleOr = docGetCommonCodeListIn.createElement(AcademyConstants.COMPLEX_OR_ELEMENT);
           eleExp = docGetCommonCodeListIn.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
           eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_COMMON_CODE_VALUE);
           eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
           eleExp.setAttribute(AcademyConstants.ATTR_VALUE, "RESTRICT_PICKEDUP_STSFA");
           eleOr.appendChild(eleExp);
           eleExp = docGetCommonCodeListIn.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
           eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_COMMON_CODE_VALUE);
           eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
           eleExp.setAttribute(AcademyConstants.ATTR_VALUE, "ENABLE_SHOW_RELATED_ORDERS");
           eleOr.appendChild(eleExp);
           eleAnd.appendChild(eleOr);
           eleComplexQuery.appendChild(eleAnd);
           eleGetCommonCodeListIn.appendChild(eleComplexQuery);
           log.verbose(XMLUtil.getElementXMLString(eleGetCommonCodeListIn));
           return docGetCommonCodeListIn;
        }
    //OMNI-90544 END

  }
