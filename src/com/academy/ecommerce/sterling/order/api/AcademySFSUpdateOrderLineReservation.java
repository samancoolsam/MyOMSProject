//package declaration
package com.academy.ecommerce.sterling.order.api;
//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
//academy import statements
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
//yantra import statements
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * Description: Class AcademySFSUpdateOrderLineReservation will 
 * 1. read the OnSuccess xml of CreatOrder transaction
 * 2. update the OrderLineReservation for each OrderLine based on Inventory Reservation details
 * 
 * @throws Exception
 */

public class AcademySFSUpdateOrderLineReservation
{
	//declaring the variables
	private static YIFApi api = null;

	static
	{
		try
		{
			api = YIFClientFactory.getInstance().getApi();
		}
		catch (YIFClientCreationException e)
		{
			e.printStackTrace();
		}
	}

	
	/**
	 * Method: updateOrderLineReservation: This method will set the OrderLineReservations. 
	 * @param env Yantra Environment Context
	 * @param inDoc input Document from OnSuccess CreateOrder event
	 * @return changeOrder input XML
	 */
	public Document updateOrderLineReservation(YFSEnvironment env, Document inDoc) throws Exception 
	{
		//Declare element variable
		Element eleRoot=null;
		Element eleOrderLine=null;
		Element eleReserRoot=null;
		Element eleOrder=null;
		Element eleOrderLines=null;
		
		//Declare nodelist variable
		NodeList nlOrderLine=null;
		//Declare String variable
		String strReserId="";
		//Declare document variable
		Document getResrvationdoc=null;
		Document docChangeOrder=null;
		//Fetch root element of the input xml
		eleRoot=inDoc.getDocumentElement();	
		
		//Fetch the OrderLine NodeList
		nlOrderLine=eleRoot.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINE);
		//Fetch the firt element of OrderLine
		eleOrderLine=(Element)nlOrderLine.item(0);
		//Fetch the value of ReservationID
		strReserId=eleOrderLine.getAttribute(AcademyConstants.ATTR_RESERV_ID);
		//Invoke method to get the Inventory Reservation List
		getResrvationdoc = getReservation(env,strReserId);
		//Fetch the root element of output document getResrvationdoc		
		eleReserRoot=getResrvationdoc.getDocumentElement();
		
		//PrepareChangeOrder input xml
		//Create element Order
		docChangeOrder = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		eleOrder=docChangeOrder.getDocumentElement();
		//Set attribute OrderHeaderKey
		eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, eleRoot.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
		//Create element OrderLines
		eleOrderLines = docChangeOrder.createElement(AcademyConstants.ELE_ORDER_LINES);
		eleOrder.appendChild(eleOrderLines);		
		//Loop through the OrderLine NodeList
		for (int count=0;count<nlOrderLine.getLength();count++)
		{
			//Declare element variable
			Element elechangOrderLine=null;
			Element eleOrderLineReservations=null;
			Element eleItem=null;
			Element eleReser=null;
			//Declare String variable
			String strOrderlineQty="";
			String strItemID="";
			//Create element OrderLine
			elechangOrderLine = docChangeOrder.createElement(AcademyConstants.ELEM_ORDER_LINE);
			eleOrderLines.appendChild(elechangOrderLine);	
			//Create element OrderLineReservations
			eleOrderLineReservations = docChangeOrder.createElement(AcademyConstants.ELE_LINE_RESERVS);
			elechangOrderLine.appendChild(eleOrderLineReservations);				
			//Fetch the element OrderLine
			eleOrderLine=(Element)nlOrderLine.item(count);
			//Set the attribute OrderLineKey
			elechangOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
			//Set the attribute OrderedQty
			strOrderlineQty=eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
			//Fetch the element Item
			eleItem=(Element)eleOrderLine.getElementsByTagName(AcademyConstants.ITEM).item(0);
			//Fetch the attribute ItemID
			strItemID=eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID);
			//Parse the value to double
			double iLineQty=Double.parseDouble(strOrderlineQty);
			//Check if value is zero
			if(iLineQty ==0)
			{
				continue;
			}			
			//Fetch the InventoryReservation Node that matches with ItemID and Quantity			
			eleReser = (Element)XMLUtil.getNodeList(eleReserRoot,"InventoryReservation[(@Quantity='"+ strOrderlineQty+"') and (Item/@ItemID='"+ strItemID+"')]").item(0);
			//Check for void record
			if(!YFCObject.isVoid(eleReser))
			{
				//Create element OrderLineReservation
				Element eleOrderLineReservation = docChangeOrder.createElement(AcademyConstants.ELE_LINE_RESERV);
				eleOrderLineReservations.appendChild(eleOrderLineReservation);
				//Set the attribute ReservationID
				eleOrderLineReservation.setAttribute(AcademyConstants.ATTR_RESERV_ID, strReserId);
				//Set the attribute Quantity
				eleOrderLineReservation.setAttribute(AcademyConstants.ATTR_QUANTITY, eleReser.getAttribute(AcademyConstants.ATTR_QUANTITY));
				//Set the attribute Node
				eleOrderLineReservation.setAttribute(AcademyConstants.ATTR_NODE, eleReser.getAttribute(AcademyConstants.SHIP_NODE));
				//Set the attribute RequestedReservationDate
				eleOrderLineReservation.setAttribute(AcademyConstants.ATTR_REQ_RESR_DATE, eleReser.getAttribute(AcademyConstants.ATTR_SHIP_DATE));
				//Set the attribute UnitOfMeasure
				eleOrderLineReservation.setAttribute(AcademyConstants.ATTR_UOM, eleItem.getAttribute(AcademyConstants.ATTR_UOM));
				//Set the attribute ProductClass
				eleOrderLineReservation.setAttribute(AcademyConstants.ATTR_PROD_CLASS, eleItem.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
				//Set the attribute ItemID
				eleOrderLineReservation.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID));
				//Set the reservation quantity to zero
				eleReser.setAttribute(AcademyConstants.ATTR_QUANTITY,"0");
			}
			else
			{
				//Fetch the InventoryReservation Node that matches with ItemID
				NodeList nlReservation=XMLUtil.getNodeList(eleReserRoot,"InventoryReservation[(Item/@ItemID='"+ strItemID+"')]");
				//Loop through InventoryReservation NodeList
				for (int index=0;index<nlReservation.getLength();index++)
				{	
					//Check for zero value
					if(iLineQty ==0)
					{
						break;
					}
					//Fetch the element InventoryReservation
					eleReser=(Element)nlReservation.item(index);
					//Fetch the attribute Quantity
					String strReserQty=eleReser.getAttribute(AcademyConstants.ATTR_QUANTITY);
					//Parse it to double
					double iResQty=Double.parseDouble(strReserQty);
					//Declare double variable
					double resvQtyUsed = 0;
					//Check for zero value
					if(iResQty ==0)
					{
						continue;
					}				
					//Check if OrderLine Qty is less than the available reservation qty
					if(iLineQty < iResQty)
					{
						//Set resvQtyUsed to OrderLine Qty
						resvQtyUsed = iLineQty;
					}
					else
					{
						//Set resvQtyUsed to available reservation qty
						resvQtyUsed = iResQty;
					}
					//Subtract the OrderLine Qty with the resvQtyUsed
					iLineQty = iLineQty - resvQtyUsed;
					//Subtract the the available reservation Qty with the resvQtyUsed
					iResQty = iResQty - resvQtyUsed;
					//Create element OrderLineReservation
					Element eleOrderLineReservation = docChangeOrder.createElement(AcademyConstants.ELE_LINE_RESERV);
					eleOrderLineReservations.appendChild(eleOrderLineReservation);
					//Set the attribute ReservationID
					eleOrderLineReservation.setAttribute(AcademyConstants.ATTR_RESERV_ID, strReserId);
					//Set the attribute Quantity
					eleOrderLineReservation.setAttribute(AcademyConstants.ATTR_QUANTITY, resvQtyUsed+"");
					//Set the attribute Node
					eleOrderLineReservation.setAttribute(AcademyConstants.ATTR_NODE, eleReser.getAttribute(AcademyConstants.SHIP_NODE));
					//Set the attribute RequestedReservationDate
					eleOrderLineReservation.setAttribute(AcademyConstants.ATTR_REQ_RESR_DATE, eleReser.getAttribute(AcademyConstants.ATTR_SHIP_DATE));
					//Set the attribute UnitOfMeasure
					eleOrderLineReservation.setAttribute(AcademyConstants.ATTR_UOM, eleItem.getAttribute(AcademyConstants.ATTR_UOM));
					//Set the attribute ProductClass
					eleOrderLineReservation.setAttribute(AcademyConstants.ATTR_PROD_CLASS, eleItem.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
					//Set the attribute ItemID
					eleOrderLineReservation.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID));
					//Set the reservation quantity to zero
					eleReser.setAttribute(AcademyConstants.ATTR_QUANTITY,iResQty+"");					
				}
			}			
		}
		
		//return the changeOrder document.		
		return docChangeOrder;
	}
	/**
	 * Method: getReservation: This method will invoke getInventoryReservationList API
	 * @param env Yantra Environment Context
	 * @param ReservationID input String
	 * @return getInvListOutXML
	 */
	public Document getReservation(YFSEnvironment env, String strResrId) throws Exception 
	{			
		//Declare Document variable
		Document getInvResrvDoc =null;
		Document getInvListOutXML=null;
		//Declare element variable
		Element invResrvEle=null;
		//Create input xml :
		/*
		 * <InventoryReservation ReservationID=""/>
		 */
		getInvResrvDoc = XMLUtil.createDocument(AcademyConstants.ELE_INV_RESERV);
		invResrvEle = getInvResrvDoc.getDocumentElement();
		invResrvEle.setAttribute(AcademyConstants.ATTR_RESERV_ID, strResrId);
		getInvListOutXML = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_INVENTORYRESERV_LIST, getInvResrvDoc);		
		return getInvListOutXML;
	}
}