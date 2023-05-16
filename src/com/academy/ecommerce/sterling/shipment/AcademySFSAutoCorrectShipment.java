//package declaration
package com.academy.ecommerce.sterling.shipment;
//java util import statements
import java.util.HashMap;
import java.util.Properties;
import java.util.Map.Entry; 
//java w3c statements
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
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfs.japi.YFSEnvironment;
 
/*Input to this service is <Shipment ShipmentKey=""/> */
/**
 * Description: Class AcademySFSAutoCorrectShipment does two parts
 * 1)Update the container quantity to BackroomPickedQuantity of a shipmentline
 * 2)If shipment is pack complte, move the shipment to Ready To Ship status.
  * @throws Exception
  */
public class AcademySFSAutoCorrectShipment {
  
private static YIFApi api = null;
static
{
 try
  {
    api = YIFClientFactory.getInstance().getApi();
  } catch (YIFClientCreationException e)
  {
  	e.printStackTrace();
  }
}
//Set properties
private Properties props = new Properties() ;
public void setProperties(Properties props)
{
	this.props = props;
}
//Define hasmap to store the container quantities: KEY_STATUS_VAL
private  HashMap<String,Double> hmpContainerQty = new HashMap<String, Double>();
  
public void autoCorrectShipment(YFSEnvironment env,Document inDoc) throws Exception
{ 	 
 //Declare Document variable
 Document getShipmentDetailOut=null;
 Document changeShipmentInput=null;
 Document changeShipmentStatusDoc=null;
//Declare Element variable
 Element eleRoot=null;
 Element eleGetShipRoot=null;
 Element eleContainerDetail=null;
 Element eleShipment=null;
 Element eleShipmentLines=null;
 Element eleShipmentLine=null;
 Element eleNewRoot=null;
 Element shipmentLineElement=null;
 //Declare NodeList
 NodeList nlContainerDetail=null;
 NodeList nlshipmentLine=null;
//Declare String varibale	 
 String strShipmentLineKey="";
 //Declare double variable
 double iContainerQty=0;
 double backroomPickedQtyVal=0;
 double quantityOrderedVal=0;
 //Declare the boolean variable
 boolean isCompletePick = false;
 
 //Fetch the root element
 eleRoot=inDoc.getDocumentElement();
 //Call method to invoke getShipmentDetails
 getShipmentDetailOut = callgetShipmentDetails(env,eleRoot);
//Fetch the root element
 eleGetShipRoot=getShipmentDetailOut.getDocumentElement();
 //Fetch the Nodelist ContainerDetail
 nlContainerDetail=eleGetShipRoot.getElementsByTagName("ContainerDetail");
 //Loop through the nodelist ContainerDetail
 for (int i=0;i<nlContainerDetail.getLength();i++)
  {
  	 //Fetch the element ContainerDetail
     eleContainerDetail=(Element)nlContainerDetail.item(i);
     //Fetch the attribute ShipmentLineKey
     strShipmentLineKey=eleContainerDetail.getAttribute("ShipmentLineKey");
     //Check if the ShipmentLineKey exist in hashmap
     if (hmpContainerQty.containsKey(strShipmentLineKey)==true)
       { 
        //if true then add the Container/@Quantity to the value of the existing ShipmentKey in hashmap
	    iContainerQty= hmpContainerQty.get(eleContainerDetail.getAttribute("ShipmentLineKey")) + Double.parseDouble(eleContainerDetail.getAttribute("Quantity"));
  	    //Replace the new Quantity for the existing ShipmentLineKey in hashmap
	    hmpContainerQty.put(eleContainerDetail.getAttribute("ShipmentLineKey"), iContainerQty);
	   }
	   else
	   {
		 //if false then put the new key=value record into the hashmap
	   	 hmpContainerQty.put(strShipmentLineKey,  Double.parseDouble(eleContainerDetail.getAttribute("Quantity")));
	   }
  }

//Start: Process for changeShipmet 
//Create element Shipment
changeShipmentInput = XMLUtil.createDocument("Shipment");
eleShipment = changeShipmentInput.getDocumentElement();
//Set the attribute Action
eleShipment.setAttribute("Action", "Modify");
//Set the attribute ShipmentKey with value received from the input xml
eleShipment.setAttribute("ShipmentKey", eleRoot.getAttribute("ShipmentKey"));
//Create the element ShipmetnLines
eleShipmentLines=changeShipmentInput.createElement("ShipmentLines");
//Append the child element
eleShipment.appendChild(eleShipmentLines);
//Loop through each entry in hashmap
for (Entry<String, Double> entry : hmpContainerQty.entrySet())
 {
	//Create element ShipmentLine
	eleShipmentLine=changeShipmentInput.createElement("ShipmentLine");
	//Set attribute ShipmentLineKey with hashmap key
	eleShipmentLine.setAttribute("ShipmentLineKey",entry.getKey());
	//Set attribute BackroomPickedQuantity with hashmap value
	eleShipmentLine.setAttribute("BackroomPickedQuantity",Double.toString(entry.getValue()));
	//Append the child element
	eleShipmentLines.appendChild(eleShipmentLine);			
 }
//Invoke changeShipment API 
AcademyUtil.invokeAPI(env,"changeShipment", changeShipmentInput);
//End: Process for changeShipmet API
			
//Start: Process for changeShipmetStatus
//Call method to Fetch the updated shipment Detail
getShipmentDetailOut = callgetShipmentDetails(env,eleRoot);	
//Fetch the root element
eleNewRoot=getShipmentDetailOut.getDocumentElement();
//Check if status is equal to Ready For Backroom Pick
if(eleNewRoot.getAttribute(AcademyConstants.ATTR_STATUS).equalsIgnoreCase(props.getProperty(AcademyConstants.KEY_STATUS_VAL)))
{
//Fetch the nodelist ShipmentLine
nlshipmentLine=eleNewRoot.getElementsByTagName("ShipmentLine");
//Loop through the NodeList ShipmentLine
for(int listIndex = 0 ; listIndex<nlshipmentLine.getLength(); listIndex++)
 {   
   //Fetch the element ShipmentLine
   shipmentLineElement = (Element) nlshipmentLine.item(listIndex);
   //Fetch the the value of attribute BackroomPickedQuantity
   backroomPickedQtyVal = Double.parseDouble(shipmentLineElement.getAttribute("BackroomPickedQuantity"));
   //Fetch the the value of attribute Quantity
   quantityOrderedVal = Double.parseDouble(shipmentLineElement.getAttribute("Quantity"));
   //Check if BackroomPickedQuantity is equal to the Ordered Qty
   if (quantityOrderedVal == backroomPickedQtyVal)
     {   
	   //if true set flage isCompletePick to true
	   isCompletePick=true;
     }
     else
     {
	   //if true set flage isCompletePick to false  
	   isCompletePick = false;
	   //Break the loop
	   break;
     }
 }
//Check isCompletePick is true
if(isCompletePick==true)
 {
   //Create element Shipment
   changeShipmentStatusDoc=XMLUtil.createDocument("Shipment");
   eleShipment = changeShipmentStatusDoc.getDocumentElement();
   //Set attribute ShipmentKey with value received from the input xml 
   eleShipment.setAttribute("ShipmentKey", eleRoot.getAttribute("ShipmentKey"));
   //Set the TransactionId
   eleShipment.setAttribute("BaseDropStatus", "1100.70.06.30");
   eleShipment.setAttribute("TransactionId", "YCD_BACKROOM_PICK");
   //Invoke changeShipmentStatus API to change the shipment status
   AcademyUtil.invokeAPI(env,"changeShipmentStatus", changeShipmentStatusDoc);	  
 }
//End: Process for changeShipmetStatus
}
}
 
 /**
  * Description: Method callgetShipmentDetails will invoke getShipmentDetails API for the provided input
  * * @throws Exception
  * @return getShipmentDetails output
   */
 
public Document callgetShipmentDetails(YFSEnvironment env, Element eleRoot) throws Exception
{ 
 //Declare Document variable
 Document outDoc =null;
 //Set the template for getShipmentDetails API
 env.setApiTemplate("getShipmentDetails", YFCDocument.getDocumentFor("<Shipment ShipmentKey=\"\" Status=\"\"><ShipmentLines><ShipmentLine ShipmentLineKey=\"\" Quantity=\"\" BackroomPickedQuantity=\"\"/></ShipmentLines>" +
 "<Containers><Container ShipmentContainerKey=\"\"><ContainerDetails><ContainerDetail Quantity=\"\" ShipmentLineKey=\"\" /></ContainerDetails></Container></Containers></Shipment>").getDocument());
 //Invoke getShipmentDetails API
 outDoc=AcademyUtil.invokeAPI(env,"getShipmentDetails", XMLUtil.getDocumentForElement(eleRoot));
 //return the output xml
 return outDoc;
 }
}
