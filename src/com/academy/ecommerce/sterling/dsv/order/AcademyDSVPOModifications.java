package com.academy.ecommerce.sterling.dsv.order;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.academy.util.logger.Logger;

/**
 * @author 285458
 * OMS will be getting the POModifications message from the Vendor Net.
 * 
 */
public class AcademyDSVPOModifications
{
	private static YIFApi api = null;

	static 
	{
		try {
			api = YIFClientFactory.getInstance().getApi();
		} catch (YIFClientCreationException e) {
			e.printStackTrace();
		}

	}
	private static Logger log = Logger.getLogger(AcademyDSVPOModifications.class.getName());
	/**
	 * @param env
	 * @param InXml (Input xml coming from the VendorNet)
	 * @return Document
	 * @throws Exception
	 * 
	 * By using the Input xml, checking like whether the <Mofications> is there or not.
	 * If it is there, checking whether Quantity, UnitCost or ESD has been changed.
	 * Based on the Modifiaction proceeding further to change the PO. 
	 */ 
	public Document poModifications(YFSEnvironment env, Document InXml) throws Exception
	{
		log.debug("input xml from Vendor regarding  PO Modififications................."+ XMLUtil.getXMLString(InXml));
		//Calling ValidateTheXml to validate the input xml
		boolean flag = ValidateTheXml(env,InXml);
		
		//Calling getOrderListOutput method to get getOrderList output xml
		Document getOrderListOutput = callGetOrderList(env, InXml);

		Document modifiedXML = modifiedDocument(InXml, getOrderListOutput);

		boolean IsModified = false;

		String LineNo = " ";
		String alertDetails="";
		String description="";
		Element orderElement  = null;
		NodeList orderLineList = modifiedXML.getElementsByTagName("OrderLine");
		for(int i=0;i<orderLineList.getLength();i++)
		{
			//NodeList modificationsList = modifiedXML.getElementsByTagName("Modifications");
			orderElement = (Element) orderLineList.item(i);
			String PrimeLineNo = orderElement.getAttribute("PrimeLineNo");
			//LineNo = "  Line:"+PrimeLineNo+"got Modified::";
			NodeList modificationList = orderElement.getElementsByTagName("Modification");
			Element modificationsElement = null;
			for(int j=0;j<modificationList.getLength();j++)
			{
				modificationsElement = (Element) modificationList.item(j);

				
				
				if(modificationsElement.getAttribute("IsModified").equals("Y"))
				{
					LineNo = "  Line:"+PrimeLineNo+"got Modified::";
					alertDetails=alertDetails+" Original" +
					""+modificationsElement.getAttribute("Type")+": "+modificationsElement.getAttribute("OriginalValue")+" and Changed" +
					""+modificationsElement.getAttribute("Type")+" : "+modificationsElement.getAttribute("Value");
					
					log.debug("is modified is Y");
					IsModified = true;
					if(modificationsElement.getAttribute("Type").equals("Quantity"))
					{
						log.debug("modification type is Qty");
						changeReleasetoCancelRestQty(env,modifiedXML,modificationsElement,orderElement);
					}

					changeOrdertoAddNoteText(env,modifiedXML,modificationsElement,orderElement);		
				}

			}
			description = description+LineNo+alertDetails;
			alertDetails=""; 			
		}
		if(IsModified)
		{
			log.debug("raising alrt and sending an email");
			raiseAnAlert(env,modifiedXML,orderElement,description);
			sendAnEmail(env,modifiedXML);
		}
		return modifiedXML;
	}
	/**
	 * @param env 
	 * @param InXml (Input xml coming from the VendorNet)
	 * @return Document
	 * Invoking getOrderList api.
	 */
	public Document callGetOrderList(YFSEnvironment env, Document InXml) throws Exception
	{
		String orderNo= ((Element) InXml.getElementsByTagName("Order").item(0)).getAttribute("OrderNo");
		Document getOrderListInput = XMLUtil.getDocument("<Order OrderNo=\""+orderNo+"\" />");
		Document getOrderListOutput = null;
		Document getOrderListTemplate = XMLUtil.getDocument("<OrderList>"+
				"<Order OrderHeaderKey=\"\" OrderNo=\"\" EnterpriseCode=\"\" DocumentType=\"\" OrderDate=\"\">"+
				"<OrderLines>"+
				"<OrderLine PrimeLineNo=\"\" SubLineNo=\"\" OrderedQty=\"\" >"+
				"<Item UnitCost=\"\" />"+
				"</OrderLine>"+
				"</OrderLines>"+
				"</Order>"+
		"</OrderList>");
		env.setApiTemplate("getOrderList", getOrderListTemplate);
		getOrderListOutput = api.getOrderList(env, getOrderListInput);

		log.debug("getOrderList api output"+XMLUtil.getXMLString(getOrderListOutput));
		return getOrderListOutput;

	}
	/**
	 * @param env 
	 * @param InXml (Input xml coming from the VendorNet)
	 * @param getOrderListOutput
	 * @return Document
	 * Identifying and stamping the modifications done on each order line.
	 */
	public Document modifiedDocument(Document InXml,Document getOrderListOutput) throws Exception
	{
		String ESD = ((Element) getOrderListOutput.getElementsByTagName("Order").item(0)).getAttribute("OrderDate");
		
		String OrderHeaderKey = ((Element) getOrderListOutput.getElementsByTagName("Order").item(0)).getAttribute("OrderHeaderKey");
		Element Order=  (Element)InXml.getElementsByTagName("Order").item(0);
		Order.setAttribute("OrderHeaderKey", OrderHeaderKey);
		
		
		String ESDdate = ESD.split("T")[0];
		
		String Time = ESD.substring(11, 13);
		int intTime = Integer.parseInt(Time);
		
		  	  
		Calendar calendar = Calendar.getInstance();     	   	
     	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
     	calendar.setTime(sdf.parse(ESDdate));
     	
     	log.debug(calendar.getTime().toString());
     	log.debug((sdf.format(calendar.getTime())).toString());
     	
     	
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        log.debug("Weekday: " + weekday);
               
        if(intTime>=13)
        {
        	if(weekday==7)
        	{
        		calendar.add(Calendar.DATE,2);
        	}
        	else
        	{
        		calendar.add(Calendar.DATE,1);
        	}
        }


		
		NodeList modOLList = InXml.getElementsByTagName("OrderLine");
		NodeList GOLList  = getOrderListOutput.getElementsByTagName("OrderLine");
		//NodeList modificationList = InXml.getElementsByTagName("Modification");

		String quantity="";
		String unitCost="";
		for(int i=0;i<modOLList.getLength();i++)
		{
			
			Element modEle = (Element) modOLList.item(i);
			NodeList modificationList = modEle.getElementsByTagName("Modification");
			for(int j=0;j<GOLList.getLength();j++)
			{
				Element GOLEle = (Element) GOLList.item(j);
				if(modEle.getAttribute("PrimeLineNo").equals(GOLEle.getAttribute("PrimeLineNo")))
				{
					quantity = GOLEle.getAttribute("OrderedQty");
					log.debug("qty is"+ quantity);
					unitCost = ((Element) GOLEle.getElementsByTagName("Item").item(0)).getAttribute("UnitCost");
					log.debug("unitcost is "+ unitCost);

				}
				for(int k=0;k<modificationList.getLength();k++)
				{
					Element modificationEle = (Element) modificationList.item(k);
					String modType = modificationEle.getAttribute("Type");
					if(modType.equals("Quantity"))
					{
						log.debug("qty");
						if(modificationEle.getAttribute("Value").equals(quantity))
						{
							modificationEle.setAttribute("IsModified", "N");
						}
						else
						{
							modificationEle.setAttribute("IsModified", "Y");
							modificationEle.setAttribute("OriginalValue", quantity);
						}
					}
					if(modType.equals("UnitCost"))
					{
						log.debug("Unitcost");
						if(modificationEle.getAttribute("Value").equals(unitCost))
						{
							modificationEle.setAttribute("IsModified", "N");
						}
						else
						{
							modificationEle.setAttribute("IsModified", "Y");
							modificationEle.setAttribute("OriginalValue", unitCost);
						}
					}
					if(modType.equals("ESD"))
					{
						log.debug("esd"+(sdf.format(calendar.getTime())).toString());
						if(modificationEle.getAttribute("Value").equals((sdf.format(calendar.getTime())).toString()))
						{
							modificationEle.setAttribute("IsModified", "N");
						}
						else
						{
							modificationEle.setAttribute("IsModified", "Y");
							modificationEle.setAttribute("OriginalValue", (sdf.format(calendar.getTime())).toString());
						}
					}
				}
			}
		}
		log.debug("Modified xml is"+ XMLUtil.getXMLString(InXml));
		return InXml;

	}
	/**
	 * @param env 
	 * @param InXml (Input xml coming from the VendorNet)
	 * @return boolean
	 * Validating the input xml which is coming from the VendorNet.
	 * Validating like checking whether the requuired attributes are coming or not.
	 */
	private boolean ValidateTheXml(YFSEnvironment env, Document InXml)
	{
		String ErrorMsg = "";
		String OrderReleaseKey = InXml.getDocumentElement().getAttribute("OrderReleaseKey");
		NodeList orderList = InXml.getElementsByTagName("Order");
		String EnterpriseCode = ((Element) orderList.item(0)).getAttribute("EnterpriseCode");
		String OrderNo = ((Element) orderList.item(0)).getAttribute("OrderNo");
		String DocumentType = ((Element) orderList.item(0)).getAttribute("DocumentType");
		//String OrderHeaderKey = ((Element) orderList.item(0)).getAttribute("OrderHeaderKey");
		
		if(isEmptyOrNull(OrderReleaseKey))
		{
			ErrorMsg = ErrorMsg + "OrderReleaseKey is missing";
		}
		
		if(isEmptyOrNull(EnterpriseCode))
		{
			ErrorMsg = ErrorMsg + "\n EnterpriseCode is missing";
		}
		if(isEmptyOrNull(OrderNo))
		{
			ErrorMsg = ErrorMsg + "\n OrderNo is missing";
		}
		if(isEmptyOrNull(DocumentType))
		{
			ErrorMsg = ErrorMsg + "\n DocumentType is missing ";
		}
		/*if(isEmptyOrNull(OrderHeaderKey))
		{
			ErrorMsg = ErrorMsg + "\n OrderHeaderKey is missing";
		}*/
		
		NodeList orderLineList = InXml.getElementsByTagName("OrderLine");
		for(int i=0;i<orderLineList.getLength();i++)
		{
			Element orderElement = (Element) orderLineList.item(i);
			String PrimeLineNo= orderElement.getAttribute("PrimeLineNo");
			String SubLineNo= orderElement.getAttribute("SubLineNo");
			String Line = "\n OrderLineLine No:: "+ (i+1);
			boolean primeLineNoMissing = false;
			boolean subLineNoMissing = false;
			if(isEmptyOrNull(PrimeLineNo))
			{
				primeLineNoMissing = true;
				//ErrorMsg = ErrorMsg+ Line+ " \n PrimeLineNo is missing";			
			}
			if(isEmptyOrNull(SubLineNo))
			{
				subLineNoMissing = true;
				//ErrorMsg = ErrorMsg +Line+ " \n SubLineNo is missing";
			}
			
			if(primeLineNoMissing && !subLineNoMissing)
			{
				ErrorMsg = ErrorMsg+ Line+ " \n PrimeLineNo is missing";	
			}
			else if(!primeLineNoMissing && subLineNoMissing)
			{
				ErrorMsg = ErrorMsg +Line+ " \n SubLineNo is missing";
			}
			else if(primeLineNoMissing && subLineNoMissing)
			{
				ErrorMsg = ErrorMsg +Line+ " \n PrimeLineNo and SubLineNo are missing";
			}
			
					
		}
		boolean flag  = true;
		if(!isEmptyOrNull(ErrorMsg))
		{
			flag = false;
			YFSException excep = new YFSException(ErrorMsg);
			excep.setErrorCode("Invalid Inforamtion");
			excep.setErrorDescription("Mandatory Feilds are missing");
			throw excep;
		}
		return flag;
		
		
			
	}
	/**
	 * @param env
	 * @param Inxml (Input xml coming from the VendorNet)
	 * @param modificationsElement (Element at the Modification Level)
	 * @param orderElement (Element at the OrderLine Level)
	 * @return Document
	 * @throws Exception
	 * If OrderedQty has been modified by VendorNet then, 
	 * cancelling the rest of the Quantity by calling the changeRelease API.
	 */
	public Document changeReleasetoCancelRestQty(YFSEnvironment env,Document modifiedXML,Element modificationsElement,Element orderElement)throws Exception
	{
		log.debug("cancelling the Qty");
		String OrderReleaseKey  = modifiedXML.getDocumentElement().getAttribute("OrderReleaseKey");
		Document changeReleaseOutput= null;
		Document changeReleaseInput = XMLUtil.getDocument("<OrderRelease Action=\"MODIFY\" OrderReleaseKey=\" "+OrderReleaseKey+" \">"+
				"<OrderLines>"+
				"<OrderLine Action=\"CANCEL\" "+
				"PrimeLineNo=\""+orderElement.getAttribute("PrimeLineNo")+"\" "+
				"StatusQuantity=\""+modificationsElement.getAttribute("Value")+"\" "+
				"SubLineNo=\""+orderElement.getAttribute("SubLineNo")+"\">"+
				"</OrderLine>"+
				"</OrderLines>"+
		"</OrderRelease>");
		changeReleaseOutput  = api.changeRelease(env, changeReleaseInput);
		return changeReleaseOutput;

	}
	/**
	 * @param env
	 * @param Inxml (Input xml coming from the VendorNet)
	 * @param modificationsElement (Element at the Modification Level)
	 * @param orderElement (Element at the OrderLine Level)
	 * @return Document
	 * @throws Exception
	 * If any modifiactions done by VendorNet then, adding the NoteText at the OrderLine Level
	 * by calling the changeOrder API.
	 */
	public Document changeOrdertoAddNoteText(YFSEnvironment env,Document modifiedXML,Element modificationsElement,Element orderElement)throws Exception
	{
		log.debug("adding note text");
		NodeList orderList = modifiedXML.getElementsByTagName("Order");
		String EnterpriseCode = ((Element) orderList.item(0)).getAttribute("EnterpriseCode");
		String OrderNo = ((Element) orderList.item(0)).getAttribute("OrderNo");
		String DocumentType = ((Element) orderList.item(0)).getAttribute("DocumentType");

		Document changeOrderOutputToAddNote =null;
		Document changeOrderInputToAddNote = XMLUtil.getDocument("<Order Action=\"MODIFY\" OrderNo=\""+OrderNo+"\" " +
				"EnterpriseCode=\""+EnterpriseCode+"\" DocumentType=\""+DocumentType+"\" >"+
				"<OrderLines>"+
				"<OrderLine PrimeLineNo=\""+orderElement.getAttribute("PrimeLineNo")+"\" "+
				"SubLineNo=\""+orderElement.getAttribute("SubLineNo")+"\" >"+
				"<Notes>"+
				"<Note NoteText= \" "+modificationsElement.getAttribute("Type")+" hasbeen modified to "+modificationsElement.getAttribute("Value") +"  from "+modificationsElement.getAttribute("OriginalValue")+" \" >"+
				"</Note>"+
				"</Notes>"+
				"</OrderLine>"+
				"</OrderLines>"+
		"</Order>");

		changeOrderOutputToAddNote = api.changeOrder(env, changeOrderInputToAddNote);
		return changeOrderOutputToAddNote;

	}
	/**
	 * @param env
	 * @param Inxml (Input xml coming from the VendorNet)
	 * @param orderElement (Element at the OrderLine Level)
	 * @param LineNo (PrimeLineNo in the Modifiaction Level)
	 * @param alertDetails (Detail Description of the Alert)
	 * @return Document
	 * @throws Exception
	 * If any modifiactions done by VendorNet then, Raising an Alert to notify all modifiactions
	 * by calling createException API.
	 */
	public Document raiseAnAlert(YFSEnvironment env, Document modifiedXML,Element orderElement,String description) throws Exception
	{
		log.debug("raising Alert");
		NodeList orderList = modifiedXML.getElementsByTagName("Order");
		String OrderNo = ((Element) orderList.item(0)).getAttribute("OrderNo");
		String OrderHeaderKey = ((Element) orderList.item(0)).getAttribute("OrderHeaderKey");
		//String EnterpriseCode = ((Element) orderList.item(0)).getAttribute("EnterpriseCode");
		//String DocumentType = ((Element) orderList.item(0)).getAttribute("DocumentType");
		Document createExceptionOutput = null;
		Document createExceptionInput = XMLUtil.getDocument("<Inbox ExceptionType=\"Academy_DSV_Modifications\" "+
				"OrderNo=\""+OrderNo+"\" " +
				"OrderHeaderKey=\""+OrderHeaderKey+"\" "+
				"DetailDescription=\""+description+"\" >"+
		"</Inbox>");
		createExceptionOutput = api.createException(env,createExceptionInput);
		
		return createExceptionOutput;

	}
	/**
	 * @param env
	 * @param Inxml (Input xml coming from the VendorNet)
	 * @return Document
	 * @throws Exception
	 * If any modifiactions done by VendorNet then, sending an Email to Business Group to notify all modifiactions
	 * by invoking the Service.
	 */
	public Document sendAnEmail(YFSEnvironment env, Document modifiedXML) throws Exception
	{
		log.debug("ending Email");
		Document ServiceOutput = null;
		ServiceOutput = AcademyUtil.invokeService(env,"AcademyDSVPOModEmail", modifiedXML);
		return ServiceOutput;
		
	}
	/**
	 * Checks whether a string is empty or null.
	 *
	 * @param argS String
	 *
	 * @return boolean
	 */
	private static boolean isEmptyOrNull(final String argS) {
		if (null==argS) { return true; }
		for (int ln=0; ln < argS.length() && !Character.isWhitespace(argS.charAt(ln)); ln++) {
			return false;
		}
		return true;
	}


}
