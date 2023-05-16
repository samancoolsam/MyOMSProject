package com.academy.ecommerce.sterling.condition;

import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.YCPDynamicCondition;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyValidateGiftCardOrders implements YCPDynamicCondition{
	
	private Properties props;
	    
	    /*
	     * Instance of logger
	     */
	    private static YFCLogCategory log = YFCLogCategory.instance(AcademyValidateGiftCardOrders.class);
	   
	    /**
	     * Stores properties configured in configurator. The property expected by this API
	     * is: academy.helpdesk.getdoc.env.key
	     * @param props Properties configured in Configurator.
	     */
	    
	    public void setProperties(Properties props) {
	    	this.props = props;
	    }
	    

		public boolean evaluateCondition(YFSEnvironment env, String sName,
				Map mapData, String sXMLData) {
			boolean retValue = false;
			
			log.beginTimer(" Begining of AcademyValidateGiftCardOrders-> evaluateCondition Api");
			//ShipmentType
			
			String Shipmentkey = (String)mapData.get(AcademyConstants.SHIPMENT_KEY);
			
			log.debug("ShipmentKey for transaction is" + Shipmentkey );
			//get the Item ID and check which is of type GIFTCard type with Qty>50 then return true.
			
			try 
			{
				Document outItemListDoc=null; 

				//Create Shipment element to get the shipment details
				YFCDocument inShipDoc = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
				YFCElement inShipElem = inShipDoc.getDocumentElement();
				inShipElem.setAttribute(AcademyConstants.SHIPMENT_KEY, Shipmentkey);
				String outputTemplate = "<Shipments> <Shipment ShipmentNo='' ShipmentKey=''><ShipmentLines>" +
						"<ShipmentLine Quantity='' ItemID=''/></ShipmentLines></Shipment> </Shipments>";
				Document outputTemplateDocument = YFCDocument.getDocumentFor(outputTemplate).getDocument();
				env.setApiTemplate("getShipmentList", outputTemplateDocument);
				Document outDoc = AcademyUtil.invokeAPI(env, "getShipmentList", inShipDoc.getDocument());
				env.clearApiTemplate("getShipmentList");
				log.verbose("shipment list - " + XMLUtil.getXMLString(outDoc));
				
				NodeList shipLineList = outDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
				int shipLineLen = shipLineList.getLength();
				for(int i=0;i<shipLineLen;i++)
				{
					Element shipLineEle=(Element)shipLineList.item(i);
					
					//getCommonCode List Api to get the Qty
					Document docGetReasonCodesInput = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
					docGetReasonCodesInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, "GET_GC_QTY");
			
					Document docCommonCodeListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST, docGetReasonCodesInput);
					String gcQty=((Element)docCommonCodeListOutput.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE).
							item(0)).getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
					
					if(Double.parseDouble(shipLineEle.getAttribute(AcademyConstants.ATTR_QUANTITY))>Double.parseDouble(gcQty))
					{
					//<?xml version="1.0" encoding="UTF-16"?><Item><Extn ExtnIsGiftCard="Y" /><ComplexQuery Operator="AND">
						//<Or><Exp Name="ItemID" Value="0012525358" /></Or></ComplexQuery></Item>
						Document rt_Item =  XMLUtil.createDocument(AcademyConstants.ITEM);
						Element el_Item=rt_Item.getDocumentElement();
						Element el_Extn=rt_Item.createElement(AcademyConstants.ELE_EXTN);
						el_Item.appendChild(el_Extn);
						el_Extn.setAttribute(AcademyConstants.ATTR_EXTN_IS_GIFT_CARD,AcademyConstants.STR_YES);

						Element el_ComplexQuery=rt_Item.createElement("ComplexQuery");
						el_Item.appendChild(el_ComplexQuery);
						el_ComplexQuery.setAttribute("Operator","AND");
						Element el_Or=rt_Item.createElement("Or");
						el_ComplexQuery.appendChild(el_Or);

						Element el_Exp=rt_Item.createElement("Exp");
						el_Or.appendChild(el_Exp);
						el_Exp.setAttribute(AcademyConstants.ATTR_NAME,AcademyConstants.ITEM_ID);
						el_Exp.setAttribute(AcademyConstants.ATTR_VALUE,shipLineEle.getAttribute(AcademyConstants.ITEM_ID));
						outItemListDoc=AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_ITEM_LIST, rt_Item);
					}
				}
				env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_DETAILS);		
				if (!YFCObject.isVoid(outItemListDoc)){
					//String TotalRecords = outItemListDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS);
					Element itemEle = (Element)outItemListDoc.getElementsByTagName(AcademyConstants.ITEM).item(0);
					if ( !YFCObject.isVoid(itemEle)){
						retValue = true;
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new YFSException("Failed Condition" +e.getMessage());
			}
			log.endTimer(" End of AcademyValidateGiftCardOrders-> evaluateCondition Api");
			return retValue;
		}
}
