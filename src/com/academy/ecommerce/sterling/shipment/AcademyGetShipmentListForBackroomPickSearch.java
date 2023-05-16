package com.academy.ecommerce.sterling.shipment;

import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;

/** Added this file as part of STL-1678 to allow UPC based search in RecodBackroom pick search screen.
 * This class will invoke existing translateBarcode service(AcademySFSTranslateBarCodeForSOM) to get the Item. 
 * Invoke getShipmentList API to get only one shipment from the API output. 
 * If user scan item then translateBarcode api will not return any value. In this case we call getShipmentList with provided ItemId.
 * Sorting can be done with any Shipment attribute.
 * @author ndey
 *
 */
public class AcademyGetShipmentListForBackroomPickSearch implements YIFCustomApi {
	
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyGetShipmentListForBackroomPickSearch.class);
	
	private Properties props;
	
	public void setProperties(Properties props) {     	
		this.props = props;
	}
	
	/**This method will invoke existing translateBarcode service(AcademySFSTranslateBarCodeForSOM) to get the Item. 
	 * Invoke getShipmentList API to get only one shipment from the API output. 
	 * If user scan item then translateBarcode api will not return any value. In this case we call getShipmentList with provided ItemId.
	 * Sorting can be done with any Shipment attribute.
	 * 
	 * ExtnExeterContainerId should be between configured range.
	 * 
	 * @param env
	 * @param inXML
	 * @return
	 * @throws Exception
	 */
	public Document getShipmentListForBackroomPickSearch(YFSEnvironment env, Document inXML) throws Exception{
		log.verbose("Inside AcademyGetShipmentListForBackroomPickSearch.getShipmentListForBackroomPickSearch()");
		
		Document docGetShipmentListOut = null;
		String strItemId = null;
		String strExtnExeterContainerId = null;
		String strFromExtnExeterContainerId = null;
		String strToExtnExeterContainerId = null;
		String strShipNode = null;
		
		Element eleExtn = null;
		Element eleShipment = inXML.getDocumentElement();
		
		String strUPCCode = eleShipment.getAttribute(AcademyConstants.ATTR_UPC_CODE);
		log.verbose("strUPCCode::"+strUPCCode);
	
		docGetShipmentListOut = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENTS);		
		
		if(!YFSObject.isVoid(strUPCCode)){	
			log.verbose("User scanned UPC/ItemID");
			
			strShipNode = eleShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
			strFromExtnExeterContainerId = props.getProperty(AcademyConstants.ATTR_FROM_EXTN_EXETER_CONTAINER_ID + AcademyConstants.STR_UNDERSCORE + strShipNode);
			strToExtnExeterContainerId = props.getProperty(AcademyConstants.ATTR_TO_EXTN_EXETER_CONTAINER_ID + AcademyConstants.STR_UNDERSCORE + strShipNode);
			
			strItemId = getItemId(env, strUPCCode);//Get ItemId from UPC	
			
			eleExtn = (Element)eleShipment.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
			strExtnExeterContainerId = eleExtn.getAttribute(AcademyConstants.ATTR_EXETER_CONTAINER_ID);
			
			if(YFSObject.isVoid(strExtnExeterContainerId)){
				eleExtn.setAttribute(AcademyConstants.ATTR_FROM_EXTN_EXETER_CONTAINER_ID, strFromExtnExeterContainerId);
				eleExtn.setAttribute(AcademyConstants.ATTR_TO_EXTN_EXETER_CONTAINER_ID, strToExtnExeterContainerId);
				eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_EXETER_CONTAINER_ID_QRY_TYPE, AcademyConstants.STR_QUERY_TYPE_BETWEEN);
			}
			
			eleShipment.setAttribute(AcademyConstants.ATTR_MAX_RECORD, AcademyConstants.STR_ONE);
			eleShipment.setAttribute(AcademyConstants.ATTR_TOTAL_QTY, AcademyConstants.STR_ONE);
			eleShipment.setAttribute(AcademyConstants.ATTR_QUERY_TIME_OUT, props.getProperty(AcademyConstants.ATTR_QUERY_TIME_OUT));
			
			Element eleShipmentLines = inXML.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
			eleShipment.appendChild(eleShipmentLines);
			
			Element eleShipmentLine = inXML.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
			eleShipmentLines.appendChild(eleShipmentLine);
			//Set ItemID.
			if(YFSObject.isVoid(strItemId)){
				log.verbose("Considering scanned value as ItemId");
				eleShipmentLine.setAttribute(AcademyConstants.ATTR_ITEM_ID, strUPCCode);
			}else{
				log.verbose("ItemId : "+strItemId);
				eleShipmentLine.setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemId);
			}
			
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, AcademyConstants.STR_ONE);
			
			Element eleOrderBy = inXML.createElement(AcademyConstants.ELE_ORDERBY);
			eleShipment.appendChild(eleOrderBy);

			Element eleAttribute = inXML.createElement(AcademyConstants.ELE_ATTRIBUTE);
			eleOrderBy.appendChild(eleAttribute);

			eleAttribute.setAttribute(AcademyConstants.ATTR_DESC_SHORT, AcademyConstants.STR_NO);
			eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, props.getProperty(AcademyConstants.ELE_ORDERBY));
		}
		
		docGetShipmentListOut = invokeGetShipmentList(env, inXML);
		log.verbose("Exit AcademyGetShipmentListForBackroomPickSearch.getShipmentListForBackroomPickSearch()");
		return docGetShipmentListOut;
	}
	
	/*Invoke getShipmentList API which will return only one shipment. Shipments are order by ExpectedShipmentDate. This parameter is configurable.
	 * @param env
	 * @param inXML
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws Exception
	 */
	private Document invokeGetShipmentList(YFSEnvironment env, Document inXML)
			throws ParserConfigurationException, SAXException, IOException,
			Exception {
		log.verbose("Inside AcademyGetShipmentListForBackroomPickSearch.invokeGetShipmentList()");
		
		Document docGetShipmentListOut;
		String strGetShipmentListTemplate = "<Shipments><Shipment SellerOrganizationCode='' ShipNode='' ShipmentKey='' ShipmentNo='' Status=''/></Shipments>";
		Document docGetShipmentListTemplate = XMLUtil.getDocument(strGetShipmentListTemplate);
	
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docGetShipmentListTemplate);
		log.verbose("***getShipmentList input***" +XMLUtil.getXMLString(inXML));
		
		docGetShipmentListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, inXML);
		log.verbose("***docGetShipmentListOut***" +XMLUtil.getXMLString(docGetShipmentListOut));
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		
		log.verbose("Exit AcademyGetShipmentListForBackroomPickSearch.invokeGetShipmentList()");		
		return docGetShipmentListOut;
	}
	
	/**Invoke AcademySFSTranslateBarCodeForSOM service to get item from scanned UPC. 
	 * This service will convert 12(or 13) digit bar code to 14 digit then call translateBarCode API.
	 * If this service return no output, we are returning UPC as ITemID considering user has scan Item ID.
	 * @param env
	 * @param strUPCCode
	 * @return
	 * @throws Exception
	 */
	private String getItemId(YFSEnvironment env, String strUPCCode) throws Exception {
		log.verbose("Inside AcademyGetShipmentListForBackroomPickSearch.getItemId()");
		String strItemId = null;
		Document docTranslateBarCodeOutput = null;
		Document docTranslateBarCodeInput = null;
		Element eleTranslateBarCodeInput = null;
		
		String strTranslateBarCodeInput = "<BarCode BarCodeData='' BarCodeType='SFSItem'>	" +
												" <ContextualInfo EnterpriseCode='DEFAULT' OrganizationCode='DEFAULT'/>	  " +
										" </BarCode>";
		
		docTranslateBarCodeInput = XMLUtil.getDocument(strTranslateBarCodeInput);
		eleTranslateBarCodeInput = docTranslateBarCodeInput.getDocumentElement();
		eleTranslateBarCodeInput.setAttribute("BarCodeData", strUPCCode);
		
		log.verbose("***docTranslateBarCodeInput***" +XMLUtil.getXMLString(docTranslateBarCodeInput));
		
		docTranslateBarCodeOutput = AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_TRANSLATE_BARCODE_FOR_SOM, docTranslateBarCodeInput);
		log.verbose("***docTranslateBarCodeOutput***" +XMLUtil.getXMLString(docTranslateBarCodeOutput));
		/*Sample output
		<BarCode BarCodeData="00400124555644" BarCodeType="SFSItem">
	      <Translations BarCodeTranslationSource="ItemAlias" TotalNumberOfRecords="1">
	        <Translation>
	            <ContextualInfo EnterpriseCode="DEFAULT"
	                InventoryOrganizationCode="Academy_Direct" OrganizationCode="DEFAULT"/>
	            <ItemContextualInfo InventoryUOM="EACH" ItemID="012455564" KitCode=""/>
	        </Translation>
	      </Translations>
		</BarCode>*/
		
		strItemId = XPathUtil.getString(docTranslateBarCodeOutput,AcademyConstants.XPATH_BARCODE_ITEM_ID);
		log.verbose("strItemId:: " + strItemId);
		
		log.verbose("Exit AcademyGetShipmentListForBackroomPickSearch.getItemId()");
		return strItemId;		
	}
}