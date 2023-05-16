
package com.academy.ecommerce.sterling.shipment;

/**
 * @author sahmed
 */
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyDeterminePackSlipPrint implements YIFCustomApi
{
	private static YFCLogCategory	log				= YFCLogCategory.instance(AcademyDeterminePackSlipPrint.class);
	private Properties				props;
	private Element					eleShipment		= null;
	private Element					eleContainer	= null;
	private String					strInvoiceNo;

	/**
	 * This method evaluate the logic to print pack slip for Item Pick with old
	 * format pack slip, or batch pick with new format pack slip.
	 * ADD_TO_CONTAINER.ON_CONTAINER_PACK_PROCESS_COMPLETE event xml will be the
	 * input to this class
	 * 
	 * @throws Exception
	 */
	/**
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document determinePackSlipFlow(YFSEnvironment env, Document inDoc) throws Exception
	{
		log.beginTimer("Begining of AcademyDeterminePackSlipPrint-> determinePackSlipFlow Api");
		inDoc = removeZeroedContainerizedQuantityLines(env, inDoc);
		String strIsPackProcessComplete = " ";
		String strTotalQuantity = " ";
		String strContainerQty = " ";
		int iContainerQuantity;
		int iContainerizedQuantity = 0;
		boolean regeneratedInvoiceNumber = false;
		eleContainer = inDoc.getDocumentElement();
		eleShipment = (Element) eleContainer.getElementsByTagName("Shipment").item(0);
		String strDocType = eleShipment.getAttribute("DocumentType");
		Element eleContainerDetails = (Element) eleContainer.getElementsByTagName("ContainerDetails").item(0);
		String strExtnInvoiceNo = XMLUtil.getString(eleShipment, "OrderInvoiceList/OrderInvoice/Extn/@ExtnInvoiceNo");
		 /* Start change for STL-244
		  * Updating the ExtnInvoiceNo if it is not getting passed by ADD_TO_CONTAINER.
		  * PACK_PROCESS_COMPLETE Event but Shipment and Invoice has been updated with the old 
		  * ExtnInvoiceNo
		  * */
		if (StringUtil.isEmpty(strExtnInvoiceNo))
		{
			strExtnInvoiceNo = XMLUtil.getString(eleShipment, "Extn/@ExtnInvoiceNo");
			log.verbose("strExtnInvoiceNo from Shipment Extension" + strExtnInvoiceNo);
		}
		// End change for STL-244
		log.verbose("Invoice number is : " + strExtnInvoiceNo);
		/* Check for reprint flow from env object */

		String isReprintFlow = "N";
		if (env.getTxnObject("ReprintPackSlip") != null)
			isReprintFlow = (String) env.getTxnObject("ReprintPackSlip");

		log.verbose("isReprintFlow : " + isReprintFlow);

		if (isReprintFlow.equals("Y"))
		{
			log.verbose("*** This is reprint flow*****");
			eleContainer.setAttribute("PrintBatchPackSlip", "Y");
			eleContainer.setAttribute("PrintBatchShippingLabel", "N");
			/*
			 * if (env.getTxnObject("ReprintPrinterId") != null) { String
			 * strPrinterId = (String) env .getTxnObject("ReprintPrinterId");
			 * eleContainer.setAttribute("PrinterId", strPrinterId); } For
			 * reprint do not regenerate the Invoice Number it will already
			 * exist. eleContainerDetails.setAttribute("InvoiceNo",
			 * strExtnInvoiceNo);
			 */
			return inDoc;
		}
		// STL-244
		// condition change as part of STL-244: if
		// (StringUtil.isEmpty(strExtnInvoiceNo)) {
		
		/* Start - OMNI- 46029 Skip Invoice check for STS 2.0 shipments */
		boolean skipInvoice = false;
		if(StringUtil.isEmpty(strExtnInvoiceNo) && "0006".equals(strDocType)) {
			skipInvoice=true;
			log.verbose("Skipping invoice check");
		}
		/* End - OMNI- 46029 Skip Invoice check for STS 2.0 shipments */
		else if (StringUtil.isEmpty(strExtnInvoiceNo))
		{
			callGetInvoiceNoUE(env, inDoc, eleShipment);
			regeneratedInvoiceNumber = true;
		} else
		{
			eleShipment.setAttribute("ExtnInvoiceNo", strExtnInvoiceNo);
			String strOrderInvoiceKey = XMLUtil.getString(eleShipment, "OrderInvoiceList/OrderInvoice/@OrderInvoiceKey");
			eleShipment.setAttribute("OrderInvoiceKey", strOrderInvoiceKey);

			Document docShipment = XMLUtil.createDocument("Shipment");
			XMLUtil.copyElement(docShipment, eleShipment, docShipment.getDocumentElement());
			if(log.isVerboseEnabled()) {
			log.verbose("****Shipment document is *****" + XMLUtil.getXMLString(docShipment));
			}
			Document docInvoiceNo = AcademyUtil.invokeService(env, "AcademyValidateInvoiceNumberService", docShipment);
			if (!YFCObject.isVoid(docInvoiceNo))
			{
				strInvoiceNo = docInvoiceNo.getDocumentElement().getAttribute("InvoiceNo");
				log.verbose("**** Invoice Header Document is returned from AcademyValidateInvoiceNumberService****");
				if ("Y".equals(docInvoiceNo.getDocumentElement().getAttribute("RegeneratedInvoiceNumber")))
				{
					log.verbose("**** Invoice Number is regenerated. New Invoice Number is******" + strInvoiceNo);
					regeneratedInvoiceNumber = true;
				} else
				{
					log.verbose("**** Invoice Number is not regenerated. Using the same Invoice Number is******" + strInvoiceNo);
					regeneratedInvoiceNumber = false;
				}
			}
		}
		// OMNI- 46029 Skip Invoice check for STS 2.0 shipments
		if(!skipInvoice) {
			eleContainerDetails.setAttribute("InvoiceNo", strInvoiceNo);
			// Attribute for Return Label Print as part of # 4775
			eleContainer.setAttribute("InvoiceNo", strInvoiceNo);
		}
		NodeList nContainerDetail = XMLUtil.getNodeList(eleContainerDetails, "ContainerDetail");
		int iContainerDetail = nContainerDetail.getLength();

		for (int i = 0; i < iContainerDetail; i++)
		{
			Element eleContainerDetail = (Element) nContainerDetail.item(i);
			strContainerQty = eleContainerDetail.getAttribute("Quantity");
			iContainerQuantity = (int) Math.round(Double.parseDouble(strContainerQty));
			iContainerizedQuantity = iContainerizedQuantity + iContainerQuantity;
		}
		log.verbose("***** Total on the container is ************" + iContainerizedQuantity);

		String strShipmentGroup = eleShipment.getAttribute("ShipmentGroupId");
		strIsPackProcessComplete = eleShipment.getAttribute("IsPackProcessComplete");
		strTotalQuantity = eleShipment.getAttribute("TotalQuantity");
		int iTotalShipmentQty = (int) Math.round(Double.parseDouble(strTotalQuantity));
		log.verbose("**** Total Shipment Quantity ******" + iTotalShipmentQty);

		if (strShipmentGroup.equalsIgnoreCase("SL-SQ-SG"))
		{
			eleContainer.setAttribute("PrintBatchPackSlip", "N");
			eleContainer.setAttribute("PrintBatchShippingLabel", "N");
			
		} 
          //Start :Changes made for STL-934 Checkout Funnel
		else if(strShipmentGroup.equalsIgnoreCase("CON-CSA-SG")){
			eleContainer.setAttribute("PrintBatchPackSlip", "Y");
			eleContainer.setAttribute("PrintBatchShippingLabel", "Y");
		}
		 //End :Changes made for STL-934 Checkout Funnel 
		else if ((strShipmentGroup.equalsIgnoreCase("CON-ML-SG")) || 
				(strShipmentGroup.equalsIgnoreCase("CON-OVNT")) ||
				(strShipmentGroup.equals("CON-GC-SG")) || 
				(strShipmentGroup.equals("GC-SG")) ||
				(strShipmentGroup.equals("CON-AMO-SG")))//Added for Ammo ORMD label printing
                
		{
			if ((iTotalShipmentQty == iContainerizedQuantity) && (strIsPackProcessComplete.equals("Y")))
			{
				/***************************************************************
				 * if total qty is equal to containerized quantity , then ensure
				 * there has been no cancellation. if there are any cancellation
				 * then set PrintBatchPackSlip to Y else N
				 **************************************************************/
				Element eleExtn = (Element) eleShipment.getElementsByTagName("Extn").item(0);
				String strExtnLineCancelled = eleExtn.getAttribute("ExtnLinesCancelled");
				if ("Y".equals(strExtnLineCancelled))
				{
					eleContainer.setAttribute("PrintBatchPackSlip", "Y");
					eleContainer.setAttribute("PrintBatchShippingLabel", "Y");
					log.verbose("***Cancellation on Shipment lines has occured. PackSlip will be printed and Shipping label will be printed ****");
				} else
				{
					/*
					 * If invoice is generated on the same day as packing then
					 * don't print the pack slip, else print it
					 */
					if (regeneratedInvoiceNumber)
					{
						eleContainer.setAttribute("PrintBatchPackSlip", "N");
						eleContainer.setAttribute("PrintBatchShippingLabel", "Y");
						log.verbose("***Although Shipment Qty is equal to Containerized Qty, since invoice# was printed after cut-off time, PackSlip will be printed and Shipping label will be printed ****");
					} else
					{
						eleContainer.setAttribute("PrintBatchPackSlip", "N");
						eleContainer.setAttribute("PrintBatchShippingLabel", "Y");
						log.verbose("***Shipment Qty is equal to Containerized Qty and invoice was generated the same day, hence PackSlip will not be printed and Shipping label will be printed ****");
					}
				}
			} else if ((iContainerizedQuantity < iTotalShipmentQty) && (strIsPackProcessComplete.equals("N")))
			{
				eleContainer.setAttribute("PrintBatchPackSlip", "Y");
				eleContainer.setAttribute("PrintBatchShippingLabel", "Y");
				log.verbose("***Containerized Qty is less than the Shipment Qty and pack process is not completed for shipment.PackSlip will be printed and Shipping label will be printed ****");
			} else if ((iContainerizedQuantity < iTotalShipmentQty) && (strIsPackProcessComplete.equals("Y")))
			{
				eleContainer.setAttribute("PrintBatchPackSlip", "Y");
				eleContainer.setAttribute("PrintBatchShippingLabel", "Y");
				log.verbose("**** Containerized Qty is less than the Shipment Qty and pack process is complete.Pack Slip will be printed and Shipping label will be printed ******");
			}
		}
		log.endTimer("End of AcademyDeterminePackSlipPrint-> determinePackSlipFlow Api");
		return inDoc;
	}

	private Document removeZeroedContainerizedQuantityLines(YFSEnvironment env, Document inDoc)
	{
		log.beginTimer("Begining of AcademyDeterminePackSlipPrint-> removeZeroedContainerizedQuantityLines Api");
		try
		{
			Element eleContainerDetails = (Element) inDoc.getDocumentElement().getElementsByTagName("ContainerDetails").item(0);
			NodeList nContainerDetail = XMLUtil.getNodeList(eleContainerDetails, "ContainerDetail");
			int iContainerDetail = nContainerDetail.getLength();
			if (iContainerDetail > 0)
			{
				int j = 0;
				for (int i = 0; i < iContainerDetail; i++)
				{
					Element eleContainerDetail = (Element) nContainerDetail.item(i);
					String strContainerQty = eleContainerDetail.getAttribute("Quantity");
					int iContainerQty = (int) Math.round(Double.parseDouble(strContainerQty));
					if (iContainerQty == 0)
					{
						eleContainerDetails.removeChild(eleContainerDetail);
						j = j + 1;
					}
				}
				log.verbose("****No of container detail elements removed since qty is 0 *********" + j);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		log.endTimer("End of AcademyDeterminePackSlipPrint-> removeZeroedContainerizedQuantityLines Api");
		return inDoc;
	}

	private void callGetInvoiceNoUE(YFSEnvironment env, Document inDoc, Element eleShipment) throws ParserConfigurationException, Exception
	{
		log.beginTimer(" Begining of AcademyProrateForPackSlip-> callGetInvoiceNoUE Api");

		Document docInvoiceDetail = XMLUtil.createDocument("InvoiceDetail");
		Element eleInvoiceHeader = XMLUtil.createElement(docInvoiceDetail, "InvoiceHeader", null);
		docInvoiceDetail.getDocumentElement().appendChild(eleInvoiceHeader);

		Element eleShipment1 = XMLUtil.createElement(docInvoiceDetail, "Shipment", null);
		eleInvoiceHeader.appendChild(eleShipment1);
		eleShipment1.setAttribute("ShipmentKey", eleShipment.getAttribute("ShipmentKey"));
		eleShipment1.setAttribute("ShipDate", eleShipment.getAttribute("ShipDate"));
		eleShipment1.setAttribute("ShipNode", eleShipment.getAttribute("ShipNode"));
		eleShipment1.setAttribute("SCAC", eleShipment.getAttribute("SCAC"));
		eleShipment1.setAttribute("CallInvoiceUEFromPrintFLow", "Y");

		log.verbose("********** CallInvoiceUEFromPrintFLow set to Y");
		Document outDoc = AcademyUtil.invokeService(env, "AcademyInvokeGetInvoiceNo", docInvoiceDetail);
		if(log.isVerboseEnabled()){
		log.verbose("********** output doc of AcademyInvokeGetInvoiceNo : " + XMLUtil.getXMLString(outDoc));
		}

		strInvoiceNo = outDoc.getDocumentElement().getAttribute("InvoiceNo");
		log.verbose("********** invoice number : " + strInvoiceNo);
		String strGetProformaOrderInvoiceKey = XMLUtil.getString(eleShipment, "OrderInvoiceList/OrderInvoice/@OrderInvoiceKey");
		Document docInput = XMLUtil.createDocument("OrderInvoice");
		docInput.getDocumentElement().setAttribute("OrderInvoiceKey", strGetProformaOrderInvoiceKey);
		Element eleExtn = XMLUtil.createElement(docInput, "Extn", null);
		docInput.getDocumentElement().appendChild(eleExtn);
		eleExtn.setAttribute("ExtnInvoiceNo", strInvoiceNo);
		Document outdoc = AcademyUtil.invokeAPI(env, "changeOrderInvoice", docInput);
		if(log.isVerboseEnabled()){
		log.verbose("*********** output of change order Invoice api : " + XMLUtil.getXMLString(outdoc));
		}
		log.endTimer(" End of AcademyProrateForPackSlip-> callGetInvoiceNoUE Api");
	}

	public void setProperties(Properties props) throws Exception
	{
		this.props = props;

	}

}
