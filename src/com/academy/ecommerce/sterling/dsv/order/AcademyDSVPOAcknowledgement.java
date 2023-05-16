package com.academy.ecommerce.sterling.dsv.order;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @author 285458
 * OMS will be getting the POAcknowledge message from the Vendor Net.
 * If no modifications done by VendorNet for the PO, then, the PO Status moved to PO_ACKNOWLEDGED
 * If any modifications done by VendorNet for the PO, then, the PO status moved to PO_MODIFIED
 */
public class AcademyDSVPOAcknowledgement
{
	/**
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyDSVPOAcknowledgement.class);
    
	private static YIFApi api = null;

	static 
	{
		try {
			api = YIFClientFactory.getInstance().getApi();
		} catch (YIFClientCreationException e) {
			e.printStackTrace();
		}

	}
	/**
	 * @param env
	 * @param InXml (Input xml coming from the VendorNet)
	 * @return Document
	 * @throws Exception
	 * 
	 * By using the Input xml, checking like whether the <Mofications> is there or not.
	 * If it is not there, PO Status moved to PO_ACKNOWLEDGED.
	 * If it is there, checking whether Quantity, UnitCost or ESD has been changed.
	 * Based on the Modifiaction proceeding further to change the PO Status to PO_MODIFIED.
	 */ 
	public Document poAcknowledgement(YFSEnvironment env, Document InXml) throws Exception
	{
		log.verbose("input xml from Vendor regarding the PO Modififications................."+ XMLUtil.getXMLString(InXml));
		boolean flag = ValidateTheXml(env,InXml);
		boolean IsAcknowledge = false;
		if(flag)
		{
		String LineNo = " ";
		String alertDetails="";
		String description="";
		Element orderElement  = null;
		NodeList orderLineList = InXml.getElementsByTagName("OrderLine");
		for(int i=0;i<orderLineList.getLength();i++)
		{
			NodeList modificationsList = InXml.getElementsByTagName("Modifications");
			 orderElement = (Element) orderLineList.item(i);
			if(modificationsList.getLength() == 0)
			{
				IsAcknowledge= true;
				changeStatustoAcknowledge(env,InXml);
			}
			else 
			{
				IsAcknowledge =false;
				LineNo = "  Line:"+orderElement.getAttribute("PrimeLineNo")+"got Modified::";
				NodeList modificationList = orderElement.getElementsByTagName("Modification");
				Element modificationsElement = null;
				for(int j=0;j<modificationList.getLength();j++)
				{
					 modificationsElement = (Element) modificationList.item(j);
					 
					alertDetails=alertDetails+" Original" +
					""+modificationsElement.getAttribute("Type")+": "+modificationsElement.getAttribute("OriginalValue")+" and Changed" +
					""+modificationsElement.getAttribute("Type")+" : "+modificationsElement.getAttribute("UpdatedValue");
					if(modificationsElement.getAttribute("Type").equals("Quantity")|| modificationsElement.getAttribute("Type").equals("UnitCost")||modificationsElement.getAttribute("Type").equals("ESD"))
					{
						if(modificationsElement.getAttribute("Type").equals("Quantity"))
						{
							changeReleasetoCancelRestQty(env,InXml,modificationsElement,orderElement);
						}

						changeOrdertoAddNoteText(env,InXml,modificationsElement,orderElement);		
					}

				}
				description = description+LineNo+alertDetails;
				alertDetails=""; 			
			}
				
			
		}
		if(!IsAcknowledge)
		{
		 raiseAnAlert(env,InXml,orderElement,description);
		 sendAnEmail(env,InXml);
		 changeStatustoModified(env,InXml);
		}
	}
		
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
		String OrderHeaderKey = ((Element) orderList.item(0)).getAttribute("OrderHeaderKey");
		
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
		if(isEmptyOrNull(OrderHeaderKey))
		{
			ErrorMsg = ErrorMsg + "\n OrderHeaderKey is missing";
		}
		
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
	 * @return Document
	 * @throws Exception
	 * If there is no modifications done by VendorNet then, changing the PO Status to PO_ACKNOWLEDGED
	 * by caaling the changeOrderStatus API.
	 */
	public Document changeStatustoAcknowledge(YFSEnvironment env, Document Inxml) throws Exception
	{
		NodeList orderList = Inxml.getElementsByTagName("Order");
		String EnterpriseCode = ((Element) orderList.item(0)).getAttribute("EnterpriseCode");
		String OrderNo = ((Element) orderList.item(0)).getAttribute("OrderNo");
		String DocumentType = ((Element) orderList.item(0)).getAttribute("DocumentType");

		Document changeOrderStatusOutputForPOAcknowledge = null;
		Document changeOrderStatusInputForPOAcknowledge = XMLUtil.getDocument("<OrderStatusChange  " +
				"BaseDropStatus=\"3200.001\" " +
				"DocumentType=\""+DocumentType+"\" "+
				"EnterpriseCode=\""+EnterpriseCode+"\" "+
				"OrderNo=\""+OrderNo+"\" " +
		"TransactionId=\"EXTN_PO_Acknowledged.0005.ex \"> </OrderStatusChange> ");
		changeOrderStatusOutputForPOAcknowledge = api.changeOrderStatus(env, changeOrderStatusInputForPOAcknowledge);
		return changeOrderStatusOutputForPOAcknowledge;

	}
	/**
	 * @param env
	 * @param Inxml (Input xml coming from the VendorNet)
	 * @return Document
	 * @throws Exception
	 * If any modifications done by vendorNet then, changing the PO Status to PO_MODIFIED
	 * by caaling the changeOrderStatus API.
	 */
	public Document changeStatustoModified(YFSEnvironment env,Document Inxml)throws Exception
	{
		NodeList orderList = Inxml.getElementsByTagName("Order");
		String EnterpriseCode = ((Element) orderList.item(0)).getAttribute("EnterpriseCode");
		String OrderNo = ((Element) orderList.item(0)).getAttribute("OrderNo");
		String DocumentType = ((Element) orderList.item(0)).getAttribute("DocumentType");

		Document changeOrderStatusOutputForPOModified = null;
		Document changeOrderStatusInputForPOModified = XMLUtil.getDocument("<OrderStatusChange  " +
				"BaseDropStatus=\"3200.002\" " +
				"DocumentType=\""+DocumentType+"\" "+
				"EnterpriseCode=\""+EnterpriseCode+"\" "+
				"OrderNo=\""+OrderNo+"\" " +
		"TransactionId=\"EXTN_PO_Acknowledged.0005.ex\"> </OrderStatusChange> ");

		changeOrderStatusOutputForPOModified = api.changeOrderStatus(env, changeOrderStatusInputForPOModified);
		return changeOrderStatusOutputForPOModified;
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
	public Document changeOrdertoAddNoteText(YFSEnvironment env,Document Inxml,Element modificationsElement,Element orderElement)throws Exception
	{
		NodeList orderList = Inxml.getElementsByTagName("Order");
		String EnterpriseCode = ((Element) orderList.item(0)).getAttribute("EnterpriseCode");
		String OrderNo = ((Element) orderList.item(0)).getAttribute("OrderNo");
		String DocumentType = ((Element) orderList.item(0)).getAttribute("DocumentType");
		
		Document changeOrderOutputToAddNote =null;
		Document changeOrderInputToAddNote = XMLUtil.getDocument("<Order Action=\"MODIFY\" OrderNo=\""+OrderNo+"\" " +
				"EnterpriseCode=\""+EnterpriseCode+"\" DocumentType=\""+DocumentType+"\" >"+
				"<OrderLines>"+
				"<OrderLine PrimeLineNo=\""+modificationsElement.getAttribute("PrimeLineNo")+"\" "+
				"SubLineNo=\""+orderElement.getAttribute("SubLineNo")+"\" >"+
				"<Notes>"+
				"<Note NoteText= \" "+modificationsElement.getAttribute("Type")+" hasbeen modified to "+modificationsElement.getAttribute("UpdatedValue") +"  from "+modificationsElement.getAttribute("OriginalValue")+" \" >"+
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
	 * @param modificationsElement (Element at the Modification Level)
	 * @param orderElement (Element at the OrderLine Level)
	 * @return Document
	 * @throws Exception
	 * If OrderedQty has been modified by VendorNet then, 
	 * cancelling the rest of the Quantity by calling the changeRelease API.
	 */
	public Document changeReleasetoCancelRestQty(YFSEnvironment env,Document Inxml,Element modificationsElement,Element orderElement)throws Exception
	{
		String OrderReleaseKey  = Inxml.getDocumentElement().getAttribute("OrderReleaseKey");
		Document changeReleaseOutput= null;
		Document changeReleaseInput = XMLUtil.getDocument("<OrderRelease Action=\"MODIFY\" OrderReleaseKey=\" "+OrderReleaseKey+" \">"+
				"<OrderLines>"+
				"<OrderLine Action=\"CANCEL\" "+
				"PrimeLineNo=\""+modificationsElement.getAttribute("PrimeLineNo")+"\" "+
				"StatusQuantity=\""+modificationsElement.getAttribute("UpdatedValue")+"\" "+
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
	 * @param orderElement (Element at the OrderLine Level)
	 * @param LineNo (PrimeLineNo in the Modifiaction Level)
	 * @param alertDetails (Detail Description of the Alert)
	 * @return Document
	 * @throws Exception
	 * If any modifiactions done by VendorNet then, Raising an Alert to notify all modifiactions
	 * by calling createException API.
	 */
	public Document raiseAnAlert(YFSEnvironment env, Document Inxml,Element orderElement,String description) throws Exception
	{
		NodeList orderList = Inxml.getElementsByTagName("Order");
		String OrderNo = ((Element) orderList.item(0)).getAttribute("OrderNo");
		String OrderHeaderKey = ((Element) orderList.item(0)).getAttribute("OrderHeaderKey");
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
	public Document sendAnEmail(YFSEnvironment env, Document Inxml) throws Exception
	{
		Document ServiceOutput = null;
		ServiceOutput = AcademyUtil.invokeService(env,"AcademyDSVPOModEmail", Inxml);
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
