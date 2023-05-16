package com.academy.util.xml;

import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
/**
 * @author neeladha
 *
 */
public class AcademyAddBOPISAttributes {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyAddBOPISAttributes.class);
	/**
	 * @param env
	 * @param inXML
	 * @return
	 * @throws Exception
	 * This method will convert the order drop XML to BOPIS Order XML.
	 * It will add the required BOPIS order specific attributes in the xml.
	 */
	public Document AddBOPISAttributes(YFSEnvironment env, Document inXML) throws Exception {
		log.beginTimer("AcademyAddBOPISAttributes::AddBOPISAttributes");
		
		try
		{
		String strCustomerEmailID=inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID);
		if (!YFSObject.isVoid(strCustomerEmailID))
		{ 
			String storeId=CallCommonCodeAPIForBOPISStoreUser(env,strCustomerEmailID);
			if (!YFSObject.isVoid(storeId))
			 { 
				log.verbose("storeId= " + storeId);
			 
		     NodeList NLOrderLine = inXML.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
		     for (int i = 0; i < NLOrderLine.getLength(); i++) 
	         {   
	         Element eleOrderLine = (Element) NLOrderLine.item(i);
		     Element elePersonInfoShipToOld= (Element) eleOrderLine.getElementsByTagName(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO).item(0);
    		  
			 Document outdocgetOrgList = getStoreAddress(env,storeId);
			 Element eleCorporatePersonInfo=(Element)outdocgetOrgList.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_CORPORATE_PERSONAL_INFO).item(0);
			 XMLUtil.removeChild(eleOrderLine, elePersonInfoShipToOld);
			 Element elePersonInfoShipTo = inXML.createElement(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO); 
			 eleOrderLine.appendChild(elePersonInfoShipTo);
			 XMLUtil.setAttributes(eleCorporatePersonInfo, elePersonInfoShipTo);
				
			 eleOrderLine.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD,AcademyConstants.STR_PICK_DELIVERY_METHOD); 
			 
			 //Start : OMNI-6448 : STS Order Creation similar to BOPIS
			 String strProcureFromNode = eleOrderLine.getAttribute(AcademyConstants.ATTR_PROCURE_FROM_NODE);
			 if(!YFCObject.isVoid(strProcureFromNode)){
				 eleOrderLine.setAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE,AcademyConstants.STR_SHIP_TO_STORE);
			 } else {
				 eleOrderLine.setAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE,AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE);
			 }
			 //End : OMNI-6448 : STS Order Creation similar to BOPIS
			 
			 eleOrderLine.setAttribute(AcademyConstants.ATTR_SHIP_NODE,storeId);
			 eleOrderLine.setAttribute(AcademyConstants.ATTR_IS_FIRM_PREDEFINED_NODE,AcademyConstants.STR_YES);
			 eleOrderLine.setAttribute(AcademyConstants.CARRIER_SERVICE_CODE,"");
			 eleOrderLine.setAttribute(AcademyConstants.ATTR_SCAC,"");
	       } 
			 
		}	
			else
			 {
				 log.verbose("Shipnode not present for the CustomerEMailID = "); 
			 }
	      } 
		
		    else
		     {
		    	 log.verbose("CustomerEMailID not present in the XML "); 
		     }
	    }
		
		
		catch (Exception e) {
			log.error(e);
			throw new YFSException("Exception in the method AcademyAddBOPISAttributes.AddBOPISAttributes-Method" +e.getMessage());
		}
		
		log.endTimer("AcademyAddBOPISAttributes::AddBOPISAttributes");
		log.verbose("Returning the xml = " + XMLUtil.getXMLString(inXML));
		return inXML;
	}
	
	
	/**
	 * @param env
	 * @param strFulfillmentType
	 * @return 
	 * This method invokes the getcommonCodeList api for getting the BOPIS_BETA_USR
	 */
	public String CallCommonCodeAPIForBOPISStoreUser(YFSEnvironment env, String CustomerEmailID) {
		/*
		 * call getCommonCodeList API <CommonCode CodeType="APPEASE_GC"/>
		 */
		Document docCommonCodeOut= null;
		String StoreId=null;
		try {
			Document getCommonCodeListInputXML = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			getCommonCodeListInputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE,AcademyConstants.BOPIS_BETA_STORE_USR);
			
			docCommonCodeOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST,
					getCommonCodeListInputXML);
			System.out.println("Output of CallCommonCodeAPI"
					+ XMLUtil.getXMLString(docCommonCodeOut));
			
			NodeList NLCommonCode = docCommonCodeOut.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
			for (int i=0;i<NLCommonCode.getLength();i++)
			{
				Element eleCommonCode = (Element)NLCommonCode .item(i);
				String strCodeValue= eleCommonCode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
			    if (!YFSObject.isVoid(strCodeValue) && strCodeValue.equalsIgnoreCase(CustomerEmailID))
			    {
			    	StoreId=eleCommonCode.getAttribute(AcademyConstants.CODE_LONG_DESC);
			    	break;
			    }
			}
			

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return StoreId;
	}
	
	 /**
	 * @param env
	 * @param OrgnCode
	 * @return
	 * @throws Exception
	 * This method prepares the input for calling getOrrganizationList API
	 */
	private Document getStoreAddress(YFSEnvironment env, String strOrgCode) throws Exception
		{
						
			log.verbose("Organization Code is "+ strOrgCode);
			Document indocOrgList = XMLUtil.createDocument(AcademyConstants.ORG_ELEMENT);
			indocOrgList.getDocumentElement().setAttribute(AcademyConstants.ORG_CODE_ATTR,strOrgCode);
			log.verbose("Calling getOrganizationList with input " +XMLUtil.getXMLString(indocOrgList));
			Document outdocOrgList = AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_CALL_GET_ORGANIZATION_LIST, indocOrgList);
			log.verbose("Output of getOrganizationList " +XMLUtil.getXMLString(outdocOrgList));
			return outdocOrgList;
		}
	 
}
