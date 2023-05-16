package com.academy.ecommerce.sterling.inventory.api;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;

import com.yantra.interop.japi.YIFCustomApi;

import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;

import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class AcademySendInvAdjMsgOnUnreceiptAPI implements YIFCustomApi {

	
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademySendInvAdjMsgOnUnreceiptAPI.class);

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}

	/**
	 * 
	 * 
	 * @param env
	 *            Environment variable
	 * @param inDoc
	 *            Input document
	 * 
	 * @throws Exception
	 *             Generic exception
	 */
	public Document unreceiptInvAdjustments(YFSEnvironment env, Document inDoc)
                                                throws Exception {
                                log.beginTimer(" Begin of AcademySendInvAdjMsgOnUnreceiptAPI ->unreceiptInvAdjustments Api");
                             //Issue-34:Inventory adjustment is not posting to IP during Unreceipt operation for container contains more than one item.
							 
    NodeList receiptLineList= inDoc.getDocumentElement().getElementsByTagName("ReceiptLine");
                                for(int index=0;index<receiptLineList.getLength(); index++){
								
								//added one more for loop to make new attribute PickLine as 'N' in each receiptLine
                                                for(int index2=0;index2<receiptLineList.getLength(); index2++){
                                                                Element receiptLine = (Element) inDoc.getDocumentElement().getElementsByTagName("ReceiptLine").item(index2); 
                                                                receiptLine.setAttribute("PickLine", "N");
                                                }
                                                Element receiptLine = (Element) inDoc.getDocumentElement().getElementsByTagName("ReceiptLine").item(index); 
												
												// In Each receiptLine element set PickLine='Y' to pick receiptLine one by one
                                                receiptLine.setAttribute("PickLine", "Y");
                                                
                                                String unreceiptQty = receiptLine.getAttribute("UnreceiveQuantity");
                                                
                                                log.verbose("Unreceive Quantity: " + unreceiptQty);
                                                
                                                if(unreceiptQty != null) {
                                                                double dUnreceiptQty = -1 * Double.parseDouble(unreceiptQty);
                                                                receiptLine.setAttribute("UnreceiveQuantity", Double.toString(dUnreceiptQty)); 
                                                }
                                                
                                                String receiptHeaderKey = inDoc.getDocumentElement().getAttribute("ReceiptHeaderKey");
                                                String userId = getUserIdForUnreceipt(env, receiptHeaderKey);
                                                inDoc.getDocumentElement().setAttribute("UserId", userId);
                                                inDoc.getDocumentElement().setAttribute("ExtnTimeStamp", AcademyUtil.getCurrentXMLGMTTime()); 
                                                log.verbose("InDoc after stamping timestamp: " + XMLUtil.getXMLString(inDoc));
                                                
                               //Call Service AcademyPublishInvAfterAdjOnUnreceipt to change format and internally call one more service to send message to MQ
                                                AcademyUtil.invokeService(env, "AcademyPublishInvAfterAdjOnUnreceipt", inDoc);
                                }
                                log.endTimer(" End of AcademySendInvAdjMsgOnUnreceiptAPI ->unreceiptInvAdjustments Api");
                                return inDoc;                     
                }

	
	
	
	/**
	 *  Get Financial Transaction code for given transaction type
	 * @param env
	 * @param finTransType
	 * @throws YFSException
	 */
	private String getUserIdForUnreceipt(YFSEnvironment env, String receiptHeaderKey) throws YFSException
	{
		YFCDocument receiptListInDoc = YFCDocument.createDocument("Receipt");
		receiptListInDoc.getDocumentElement().setAttribute("ReceiptHeaderKey", receiptHeaderKey);
		Document receiptListOutDoc = null;
		String userId = "";
		
		try
		{
			//tempDoc = XMLUtil.createDocument("<Receipt><ReceiptLines><ReceiptLine/></ReceiptLines></Receipt>");
			//env.setApiTemplate("getReceiptList", tempDoc);
			receiptListOutDoc = AcademyUtil.invokeService(env, "AcademyGetReceiptList", receiptListInDoc.getDocument());
			//commonCodeOutDoc = YFCDocument.getDocumentFor(new File("D://Document2.xml")).getDocument();
		}
		catch (Exception e)
		{
			throw new YFSException("Error Invoking getReceiptList API");
		}

		NodeList nList = receiptListOutDoc.getElementsByTagName("ReceiptLine");
		
		if (nList.getLength() > 0)
		{
			Element receiptEl = (Element) nList.item(0);
			userId = receiptEl.getAttribute("Modifyuserid");
		}
		log.verbose("UserID is : " + userId);

		return userId;
	}
	
	public static void main(String args[]) {
    	//log.verbose("getCurrentXMLGMTTime:  " + AcademyUtil.getCurrentXMLGMTTime()); 
		String sCurrentGMTTime="";
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date currentDate = new Date();
        Calendar here = Calendar.getInstance();
        int gmtoffset = here.get(Calendar.DST_OFFSET)
        + here.get(Calendar.ZONE_OFFSET);
        Date GMTDate = new Date(currentDate.getTime() - gmtoffset);
        sCurrentGMTTime = formatter.format(GMTDate);
        
        log.verbose("Datetime is:" + sCurrentGMTTime);
    	
    }

	
}
