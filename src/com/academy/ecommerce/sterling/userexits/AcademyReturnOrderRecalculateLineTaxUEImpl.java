package com.academy.ecommerce.sterling.userexits;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyPricingAndPromotionUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSExtnLineTaxCalculationInputStruct;
import com.yantra.yfs.japi.YFSExtnTaxBreakup;
import com.yantra.yfs.japi.YFSExtnTaxCalculationOutStruct;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSRecalculateLineTaxUE;

/**
 * 
 */

/**
 * @author vgummadidala
 * this UE Implemented for overriding tax values & to handle Proration issue corrections as  part of CR#18.
 * 
 */
public class AcademyReturnOrderRecalculateLineTaxUEImpl implements
		YFSRecalculateLineTaxUE {
	Document outDoc = null;
		// creating the instance of Logger
	YFCLogCategory m_Logger = YFCLogCategory
			.instance(AcademyReturnOrderRecalculateLineTaxUEImpl.class.getName());

	public YFSExtnTaxCalculationOutStruct recalculateLineTax(
			YFSEnvironment env,
			YFSExtnLineTaxCalculationInputStruct taxInputStruct)
			throws YFSUserExitException {
		YFSExtnTaxCalculationOutStruct outputStruct = new YFSExtnTaxCalculationOutStruct();
		List<YFSExtnTaxBreakup> outputList = new ArrayList<YFSExtnTaxBreakup>();
		//YFSExtnTaxBreakup outputStateTax = new YFSExtnTaxBreakup();
		
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		Document inXML = null,docOrderDet;
		try {
			m_Logger.beginTimer(" Begining of AcademyReturnOrderRecalculateLineTaxUEImpl ->  recalculateLineTax() Api");
			m_Logger.verbose("Entering into  AcademyReturnOrderRecalculateLineTaxUEImpl ->");
			
			String sOrderHdrKey = taxInputStruct.orderHeaderKey,totalInvoicedTax="0";
			
			if (sOrderHdrKey != null && !sOrderHdrKey.equals("")) {
				
				boolean needListApiCall = false;				
				if(env.getTxnObject("RtnWGReceiptDoc")==null){
					needListApiCall = true;
					m_Logger.verbose("The object 'RtnWGReceiptDoc' in env is not available ");
				}else{
					m_Logger.verbose("The object 'RtnWGReceiptDoc' in env is available ");
					inXML = (Document)env.getTxnObject("RtnWGReceiptDoc");
					m_Logger.verbose("Order on env is "+inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY)+" and Order in input is : "+sOrderHdrKey);
					if(!inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY).equals(sOrderHdrKey))
						needListApiCall = true;
				}
				m_Logger.verbose("Required getOrderList API call : "+needListApiCall+" bForInvoice is : "+taxInputStruct.bForInvoice);
				if(needListApiCall && !taxInputStruct.bForInvoice){
					m_Logger.verbose("Call may be from changeOrder API flow");
					outputStruct.colTax = taxInputStruct.colTax;
					m_Logger.verbose("Return Tax in Input Stub as output Stub");
					return outputStruct;
				}
				
				 /*docOrderDet = XMLUtil
						.createDocument("OrderLine");
				docOrderDet.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
						sOrderHdrKey);
				docOrderDet.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,
						taxInputStruct.orderLineKey);
				docOrderDet.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE,
						AcademyConstants.STR_RETURN_DOCTYPE);
				env
						.setApiTemplate("getOrderLineList",
								"global/template/api/getOrderLineDetails.LineChargesForReturnInvoice.xml");

				inXML = AcademyUtil.invokeAPI(env, "getOrderLineList",
						docOrderDet);

				env.clearApiTemplate("getOrderLineList");*/
				if(inXML == null){
					m_Logger.verbose("Order details are empty ..");
					outputStruct.colTax = taxInputStruct.colTax;
					return outputStruct;
				}else 
					m_Logger.verbose("The Order details in env object is : "+XMLUtil.getXMLString(inXML));
				Element eleOrderLine = (Element)XPathUtil.getNode(inXML, "Order/OrderLines/OrderLine[@OrderLineKey='"+taxInputStruct.orderLineKey+"']");
				if(eleOrderLine==null)
					return outputStruct;
				NodeList lineTaxesList = eleOrderLine.getElementsByTagName(AcademyConstants.ELE_LINE_TAX);
				
				m_Logger.verbose("The length of line tax is : "+lineTaxesList.getLength());

				YFSExtnTaxBreakup outputChargeTax[] = new YFSExtnTaxBreakup[lineTaxesList.getLength()];
				List taxesList=taxInputStruct.colTax;
					Iterator taxItr=taxesList.iterator(); 
					
					int i=0;
					while(taxItr.hasNext())
					{  
						YFSExtnTaxBreakup tempTax=(YFSExtnTaxBreakup) taxItr.next();
				
						Element lineTaxEle = (Element) XPathUtil.getNode(eleOrderLine, "LineTaxes/LineTax" +
								"[@ChargeCategory='"+tempTax.chargeCategory+"' and " +
										"@ChargeName='"+tempTax.chargeName+"' and "+ "@TaxName='"+tempTax.taxName+"']");
						m_Logger.verbose("Inside the main Logic  and checking for LineTaxEle is Null condition"+XMLUtil.getElementXMLString(lineTaxEle));
						if (!YFCObject.isVoid(lineTaxEle))
						{
							String chargeTax = lineTaxEle.getAttribute(AcademyConstants.ATTR_TAX);
							totalInvoicedTax=lineTaxEle.getAttribute("InvoicedTax");
						outputChargeTax[i] = new YFSExtnTaxBreakup();
						outputChargeTax[i].taxName = tempTax.taxName;
					
						outputChargeTax[i].chargeCategory=tempTax.chargeCategory;
						outputChargeTax[i].chargeName=tempTax.chargeName;
						/*double tax=0.0;
						
						if(taxInputStruct.bLastInvoiceForOrderLine)
						{
							m_Logger.verbose("Inside the main Logic- doing for LastInvoice process condition ");
							tax = (Double
									.parseDouble(chargeTax)) - (Double
											.parseDouble(totalInvoicedTax));
							//tax = (Double.valueOf(twoDForm.format(shipTax)));
						}
						else
						{
							m_Logger.verbose("Inside the main Logic- doing for NOT the LastInvoice process condition ");
							if(foundChargeInEnvInstance && tempTax.chargeName.equals(AcademyConstants.STR_RETURNSHIPPING_CHARGE)){
								chargeTax = XPathUtil.getString(dRtnOrder.getDocumentElement(), 
										"OrderLines/OrderLine[@OrderLineKey='"+taxInputStruct.orderLineKey+"']/LineTaxes/LineTax" +
												"[@ChargeCategory='"+tempTax.chargeCategory+"' and @ChargeName='"+tempTax.chargeName+"' and "+ "@TaxName='"+tempTax.taxName+"']/@Tax");
								tax = Double.parseDouble(chargeTax);
							}else{
								tax= (taxInputStruct.currentQty
										* (Double
												.valueOf(Double.parseDouble(chargeTax) / (taxInputStruct.lineQty))));
							}
						}*/
						outputChargeTax[i].tax = Double.valueOf(lineTaxEle.getAttribute(AcademyConstants.ATTR_TAX));
						
						outputList.add(outputChargeTax[i]);
							
					}
						i++;
						
					}
				
				outputStruct.colTax = outputList;
				m_Logger.verbose("out put taxInputStruct.bLastInvoiceForOrderLine Flag ->"+taxInputStruct.bLastInvoiceForOrderLine);
			
			m_Logger.verbose("Exiting  AcademyReturnOrderRecalculateLineTaxUEImpl()");;
			}
			m_Logger.endTimer(" End of AcademyReturnOrderRecalculateLineTaxUEImpl ->  recalculateLineTax() Api");
		} catch (Exception e) {
			//throw new YFSUserExitException();
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		}

		return outputStruct;

	}
	
	
	
}
