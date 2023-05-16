//package declaration
package com.academy.ecommerce.sterling.item.api;
//java util import statements
import java.util.Properties;
import java.util.HashMap;
//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
//academy import statements
import com.academy.ecommerce.sterling.shipment.AcademySFSPrintPendingShipments;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
//yantra import statements
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;


/**
 * Description: Class AcademySFSProcessPrintItemPickTickets gets the list of all
 * items against the requested shipnode.
 *
 * @throws Exception
 */
public class AcademySFSProcessPrintItemPickTickets implements YIFCustomApi
{
	//log set up 
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademySFSProcessPrintItemPickTickets.class);
	// Instance to store the properties configured for the condition in
	// Configurator
	private Properties	prop;
	// Stores property configured in configurator
	public void setProperties(Properties prop) throws Exception
	{
		this.prop = prop;
	}
	/**
	 * Retrieves Page Number value.
	 *
	 * @param env
	 *            Yantra Environment Context.
	 * @param inXML
	 *            Input Document Value.
	 * @return void
	 */
	public static HashMap<String,String> getPageNumbers(String pageNo)
	{
		//Declare the hashmap
		HashMap<String,String> pageNumbers = new HashMap<String,String>();
		//Split the string for the matched argument ","
		String[] pageNos = pageNo.split(",");
		//Loop through the array
		for(int i=0; i< pageNos.length; i++)
		{
			//Fetch the record
			pageNo = pageNos[i];
			//Fetch the value indexed with '-'
			int indexOfSeperator = pageNo.indexOf("-");
			//If no record exist
			if(indexOfSeperator == -1)
			{
				try
				{
					//Parse Integer value of pageNo
					Integer.parseInt(pageNo);
					//Put the pageNo value into hashmap
					pageNumbers.put(pageNo,null);
				}
				catch(Exception e){}
			}
			else
			{
				try
				{
					//Fetch the value from the position before '-'
					int start = Integer.parseInt(pageNo.substring(0,indexOfSeperator));
					//Fetch the value from the position after '-'
					int end = Integer.parseInt(pageNo.substring(indexOfSeperator+1));
					//compare the values
					if(start < end)
					{
						//Loop through the value range
						for(int j=start; j<=end; j++)
						{
							//Put each value into hashmap
							pageNumbers.put(j+"",null);
						}
					}
					else
					{
						//Loop through the value range
						for(int j=end; j<=start; j++)
						{
							//Put each value into hashmap
							pageNumbers.put(j+"",null);
						}
					}
				}
				catch(Exception e){}
			}
		}
		//return the hashmap
		return pageNumbers;
	}


	/**
	 * Retrieves item list based on ShipNode.
	 *
	 * @param env
	 *            Yantra Environment Context.
	 * @param inXML
	 *            Input Document Value.
	 * @return void
	 *
	 * <Page BeforeChildrenPrintDocumentId="ITEM_LABEL"
	 * GeneratedOn="2012-06-25T11:18:43-04:00" IsFirstPage="Y" IsLastPage="Y"
	 * IsValidPage="Y" PageNumber="1"
	 * PageSetToken="null:-10a15a5:138222e0966:-7fcf" PrinterId="PDFPrinter1"
	 * ShipNode="500">
	 * <Output>
	 * <StoreItemPickQuantityList>
	 * <StoreItemPickQuantity Description="Berkley Gulp! Small Pre-Cut
	 * Stripz&amp;#8482; 30-Pack" ItemID="0000327007" ItemImageUrl="a.bmp"
	 * Quantity="2.00" ShipNode="500"/> <StoreItemPickQuantity
	 * Description="Berkley Gulp! Small Pre-Cut Stripz&amp;#8482; 30-Pack"
	 * ItemID="0017472580" ItemImageUrl="c:/yantra/console/icons/home.gif"
	 * Quantity="2.00" ShipNode="500"/>
	 * </StoreItemPickQuantityList>
	 * </Output>
	 * </Page>
	 */
	public void getItemPickLists(YFSEnvironment env, Document inXML) throws Exception
	{
		// Declare integer variable
		int index = 0;
		int nindex = 1;
		// Declare String variable and set the value
		String isLastPage = AcademyConstants.STR_NO;
		String TotalNumberOfPages="";
		// Fetch the root element
		Element elePrint = inXML.getDocumentElement();
		// Fetch the attribute value of PickticketNo
		String pickticketNo = elePrint.getAttribute("PickticketNo");
		
		//SFS2.0 001 Printer Id start
		String strIsPickTicketPrinted = elePrint.getAttribute("PickTicketPrinted");
		env.setTxnObject("PickTicketPrinted", strIsPickTicketPrinted);
		log.verbose(" PickTicketPrinted is :" + strIsPickTicketPrinted);
		String strPrinterId = elePrint.getAttribute("PrinterID");
		String ItemPickTicketId = "";
		Document inCommonCodeDoc = XMLUtil.createDocument("CommonCode");
		Element inComElem = inCommonCodeDoc.getDocumentElement();
		inComElem.setAttribute("CodeType", strPrinterId);
		Document outCommDoc = AcademyUtil.invokeAPI(env, "getCommonCodeList",
				inCommonCodeDoc);
			if (!YFCObject.isVoid(outCommDoc)) {
			Element outCommElem = outCommDoc.getDocumentElement();
			NodeList CommonCodeList = XMLUtil.getNodeList(outCommElem,
					"CommonCode");
			if (!YFCObject.isVoid(CommonCodeList)) {
				int iLength = CommonCodeList.getLength();
				for (int k = 0; k < iLength; k++) {
					Element CommonCode = (Element) CommonCodeList.item(k);
					String codevalue = CommonCode.getAttribute("CodeValue");
					if (codevalue.contains("STORE_ITEM")) {
						ItemPickTicketId = CommonCode
								.getAttribute("CodeValue");
						env.setTxnObject("ItemPickTicketPrinterId", ItemPickTicketId);
					log.verbose(" ItemPickTicketPrinterId is :" + ItemPickTicketId);
					} 
				}
			}
					}
		log.endTimer(" End of getCommonCodeList To get Printer Id-> getCommonCodeList Api");
	
		//SFS2.0 001 Printer Id end
		
		// Fetch the attribute value of PageNo
		HashMap<String,String> pageNos = getPageNumbers(elePrint.getAttribute("PageNo"));

		// Check if attribute value of PickticketNo is blank
		if (pickticketNo.equals(""))
		{
			// throw Exception
		}
		// Loop through the record IsLastPage=N
		while (isLastPage.equals(AcademyConstants.STR_NO))
		{
			// START: Prepare input for getPage API
			// Set element Page
			Document getPageInput = XMLUtil.createDocument(AcademyConstants.ELE_PAGE);
			Element eleRoot = getPageInput.getDocumentElement();

			int pageNo = index + 1;
			// Set the attribute PageNumber, by mapping index value
			eleRoot.setAttribute(AcademyConstants.ATTR_PAGE_NUMBER, Integer.toString(pageNo));
			// Set the attribute PageSize, by mapping the value from service
			// argument
			eleRoot.setAttribute(AcademyConstants.ATTR_PAGE_SIZE, prop.getProperty(AcademyConstants.KEY_PAGE_SIZE_VALUE));
			// Set the value for attribute PaginationStrategy
			eleRoot.setAttribute(AcademyConstants.ATTR_PAGINATION_STRATEGY, AcademyConstants.ATTR_GENERIC);
			// Set the value for attribute Refresh
			eleRoot.setAttribute(AcademyConstants.ATTR_REFRESH, AcademyConstants.ATTR_Y);
			// Create element API
			Element apiElement = getPageInput.createElement(AcademyConstants.ELE_API);
			// Set the value for attribute IsFlow
			apiElement.setAttribute(AcademyConstants.ATTR_IS_FLOW, AcademyConstants.STR_YES);
			// Map the value for Service Name
			apiElement.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.CUSTOM_ITEM_PICK_QTY_SERV);
			// Append the child element
			eleRoot.appendChild(apiElement);
			// Create element Input
			Element inputElement = getPageInput.createElement(AcademyConstants.ELE_INPUT);
			// append the child element
			apiElement.appendChild(inputElement);
			// import element
			Element elenewPrint = XMLUtil.importElement(inputElement, elePrint);

			//Creating element OrderBy
			Element eleOrderBy=getPageInput.createElement(AcademyConstants.ELE_ORDERBY);
			elenewPrint.appendChild(eleOrderBy);

			//Creating element Attribute
			Element eleAttribute=getPageInput.createElement(AcademyConstants.ELE_ATTRIBUTE);
			//Setting the attribute value of Name
			eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.COL_LONG_SKU);
			//Append child element
			eleOrderBy.appendChild(eleAttribute);

			// Create element Template
			Element eleTemplate = getPageInput.createElement(AcademyConstants.ELE_TEMPLATE);
			// Append the child element
			//Bopis 2034 - Start
			Document tempgetPageDoc = YFCDocument.getDocumentFor(
					"<Page GeneratedOn=\"\" IsFirstPage=\"\" IsLastPage=\"\" IsValidPage=\"\" PageNumber=\"\" PageSetToken=\"\" PageSize=\"\" StartRowNumber=\"\" TotalNumberOfPages=\"\"><Output/></Page>")
					.getDocument();
			
			env.setApiTemplate(AcademyConstants.API_GET_PAGE, tempgetPageDoc);
			//Bopis 2034 -End
			apiElement.appendChild(eleTemplate);
			// END: Preparing input document for getPage API
			// Invoke getPage Standard Sterling API
			Document getPageOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_PAGE, getPageInput);
			//Fetch the root element from the getPageOutput xml
			Element getPageOutputRoot = getPageOutput.getDocumentElement();
			//Fetch the NodeList StoreItemPickQuantity
			NodeList storeItemPickQuantitys = getPageOutputRoot.getElementsByTagName("StoreItemPickQuantity");
			//Fetch the count of the NodeList
			int itemLength = storeItemPickQuantitys.getLength();
			
			//start OMNI-48662
			Document outDocSIMUpdate = getSIMUpdatesForPrintItemPick(env, getPageOutput);
			//end OMNI-48662
			
			//Check If the count is greater than zero
			if (itemLength > 0)
			{
				// Loop through each Shipment element
				for (int j = 0; j < itemLength; j++)
				{
					//Fetch the element StoreItemPickQuantity
					Element item = (Element) storeItemPickQuantitys.item(j);
					//START: STL1241
				   
					String strrItemID = item.getAttribute("ItemID");
					//Academy BOPIS Print Management: Begin				
					String strItemShipNode = item.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
					String strTotalStock = AcademySFSPrintPendingShipments.getTotalStock(env,strrItemID,strItemShipNode,"GOOD","EACH");
					item.setAttribute("TotalStock", strTotalStock);
					//Academy BOPIS Print Management: End
					    
						String itemAliasValue = AcademyUtil.getItemAliasValueForItem(strrItemID, env);
						if(!YFCObject.isVoid(itemAliasValue))
						{
						log.verbose("************ALIAS VALUE IS NOT NULL***********************");
						item.setAttribute(AcademyConstants.ATTR_ITEM_BARCODE,itemAliasValue );
						}
						else
						{
							log.verbose("************ALIAS VALUE IS NULL***********************");
						item.setAttribute(AcademyConstants.ATTR_ITEM_BARCODE, AcademySFSPrintPendingShipments.generateBarcode(item.getAttribute(AcademyConstants.ATTR_ITEM_ID)));	
						}
						 
				    //END: STL1241
						
					//start OMNI-48662
					if(!YFCObject.isVoid(outDocSIMUpdate)) {
						log.verbose("SIM RestAPI invocation for SFS & BOPIS Backroompick screen");
						YFCDocument yfcDocSIMUpdate = YFCDocument.getDocumentFor(outDocSIMUpdate);
						YFCElement eleRootJsonObject = yfcDocSIMUpdate.getDocumentElement();
						log.verbose("eleRootJsonObject.hasChildNodes() :: " +eleRootJsonObject.hasChildNodes());
						String strResErrorCode = eleRootJsonObject.getAttribute(AcademyConstants.STRI_CODE);
						log.verbose("SIM Response Error Code :: " +strResErrorCode);
						if(!YFCObject.isVoid(eleRootJsonObject) && eleRootJsonObject.getNodeName().equals(AcademyConstants.STR_JSON_OBJECT) 
								&& eleRootJsonObject.hasChildNodes() && YFCObject.isVoid(strResErrorCode)) {
							item.setAttribute(AcademyConstants.STR_SIM_REST_API, AcademyConstants.STRI_SUCCESS);
							log.verbose("SIM RestAPI Enabled and API invocation is success");
							YFCNodeList<YFCElement> eleSIMItemNodeList= eleRootJsonObject.getElementsByTagName(AcademyConstants.ITEM);
							for (YFCElement eleSIMItem : eleSIMItemNodeList){
								YFCElement eleChieldJsonObject = eleSIMItem.getChildElement(AcademyConstants.STR_JSON_OBJECT);
								String strResItemId = eleChieldJsonObject.getAttribute(AcademyConstants.STR_ITEM_ID);
								log.verbose("SIM Response ItemId :: " +strResItemId);
								
								if((!YFCObject.isVoid(strrItemID) && !YFCObject.isVoid(strResItemId)) && strrItemID.equals(strResItemId)) {
									
									item.setAttribute(AcademyConstants.STR_AVL_STOCK_ON_HAND, eleChieldJsonObject.getAttribute(AcademyConstants.STRI_AVL_STOCK_ON_HAND));
									log.verbose("SIM Response AvailableStockOnHand :: " + eleChieldJsonObject.getAttribute(AcademyConstants.STRI_AVL_STOCK_ON_HAND));
									
									item.setAttribute(AcademyConstants.STR_LAST_REC_DATE, eleChieldJsonObject.getAttribute(AcademyConstants.STRI_LAST_REC_DATE));
									log.verbose("SIM Response LastReceivedDate :: " + eleChieldJsonObject.getAttribute(AcademyConstants.STRI_LAST_REC_DATE));
									
									item.setAttribute(AcademyConstants.STR_ITEM_PRICE, eleChieldJsonObject.getAttribute(AcademyConstants.STR_RETAIL_PRICE));
									log.verbose("SIM Response ItemPrice/retailPrice :: " + eleChieldJsonObject.getAttribute(AcademyConstants.STR_RETAIL_PRICE));
								}
							}
						} else {
							//log.verbose("SIM invocation is disabled at service level, IsSIMRestAPIEnabled shd be Y or RestAPI invocation is failed");
							//String strResErrormessage = eleRootJsonObject.getAttribute("message");
							log.verbose("Invalid SIM Response1 - outDocSIMUpdate :: \n" +SCXmlUtil.getString(outDocSIMUpdate));
							item.setAttribute(AcademyConstants.STR_SIM_REST_API, AcademyConstants.STRI_FAILURE);
							item.setAttribute(AcademyConstants.STR_AVL_STOCK_ON_HAND, "");
							item.setAttribute(AcademyConstants.STR_LAST_REC_DATE, "");
							item.setAttribute(AcademyConstants.STR_ITEM_PRICE, "");
						}					
					}else {
						//log.verbose("SIM RestAPI Disabled or RestAPI invocation is failed");
						log.verbose("Invalid SIM Response2 - outDocSIMUpdate :: \n" +SCXmlUtil.getString(outDocSIMUpdate));
						item.setAttribute(AcademyConstants.STR_SIM_REST_API, AcademyConstants.STRI_FAILURE);
						item.setAttribute(AcademyConstants.STR_AVL_STOCK_ON_HAND, "");
						item.setAttribute(AcademyConstants.STR_LAST_REC_DATE, "");
						item.setAttribute(AcademyConstants.STR_ITEM_PRICE, "");
					}
				}
				//end OMNI-48662

				// Fetch the attribute value IsLastPage
				isLastPage = getPageOutputRoot.getAttribute(AcademyConstants.ATTR_IS_LAST_PAGE);
				TotalNumberOfPages=getPageOutputRoot.getAttribute(AcademyConstants.ATTR_TOTAL_NO_OF_PAGES);
				// Map the value for ShipNode
				getPageOutputRoot.setAttribute(AcademyConstants.SHIP_NODE, elePrint.getAttribute(AcademyConstants.SHIP_NODE));
				//Set the attribute PrinterId
				getPageOutputRoot.setAttribute(AcademyConstants.ATTR_PRINTER_ID, prop.getProperty(AcademyConstants.KEY_PRINTER_ID_VALUE));
				// Set the attribute BeforeChildrenPrintDocumentId
				getPageOutputRoot.setAttribute(AcademyConstants.ATTR_DOCUMENT_ID, prop.getProperty(AcademyConstants.KEY_DOCUMENT_ID_VALUE));
				//Bopis 2034- Start
				getPageOutputRoot.setAttribute(AcademyConstants.ATTR_PAGE_NUMBER, nindex +" of "+ TotalNumberOfPages  );
				//Bopis 2034 - End
				//Validate the pageNos value
				if(pageNos.isEmpty() || pageNos.containsKey(pageNo+""))
				{
					// Invoke service to print the item pick ticket
					AcademyUtil.invokeService(env, AcademyConstants.SERV_PRINT_PICK_ITEM_TKT, getPageOutput);
				}
			}
			else
			{
				//break the loop
				break;
			}

			// Increment the index counter value
			index++;
			nindex++;
			
		}
	}
	
	//start OMNI-48662
	//Fulfillment- Update the Print Pick Ticket to display the Available stock on hand, Last Received Date & Item Price
	/**
	 * inDoc to Service:
	 * <Items StoreID="033"> 
	 * 		<Item ItemID="010035624"/> 
	 * 		<Item ItemID="010035625"/>
	 * </Items>
	 */
	private Document getSIMUpdatesForPrintItemPick(YFSEnvironment env, Document inDoc)
			throws Exception {
		log.verbose("getSIMUpdatesForPrintItemPick - Start");
		Document outDocService = null; 
		Document indocService= null;
		String strStoreID = null;
		
		indocService = XMLUtil.createDocument(AcademyConstants.ELE_ITEMS);
		Element eleItems = indocService.getDocumentElement();
		
		//Fetch the root element from the getPageOutput xml
		Element getPageOutputRoot = inDoc.getDocumentElement();
		//Fetch the NodeList StoreItemPickQuantity
		NodeList storeItemPickQuantitys = getPageOutputRoot.getElementsByTagName("StoreItemPickQuantity");
				
		for (int i=0;i<storeItemPickQuantitys.getLength();i++){					
			//Fetch the element StoreItemPickQuantity
			Element item = (Element) storeItemPickQuantitys.item(i);
			String strrItemID = item.getAttribute(AcademyConstants.ITEM_ID);
			log.verbose("getSIMUpdatesForPrintItemPick - strItemID :: " + strrItemID);
			Element itemElement = XMLUtil.createElement(indocService, AcademyConstants.ITEM, true);
			itemElement.setAttribute(AcademyConstants.ITEM_ID, strrItemID);
			eleItems.appendChild(itemElement);
			strStoreID = item.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		}
		eleItems.setAttribute(AcademyConstants.STR_STORE_ID, strStoreID);
		log.verbose("getSIMUpdatesForPrintItemPick - strStoreID :: " + strStoreID);
		log.verbose("Start: Invoking the AcademySIMIntegrationRestAPIWebService, inXML: \n" + XMLUtil.getXMLString(indocService));
		//outDocService = AcademyUtil.invokeService(env, "AcademySIMIntegrationRestAPIService", indocService);
		outDocService = AcademyUtil.invokeService(env, "AcademySIMIntegrationRestAPIWebService", indocService);
		log.verbose("End: Invoking the AcademySIMIntegrationRestAPIWebService, outDoc: \n" + XMLUtil.getXMLString(outDocService));
		log.verbose("getSIMUpdatesForPrintItemPick - End");
		return outDocService;
	}
	//end OMNI-48662
}