package com.academy.ecommerce.sterling.userexits;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSBeforeCreateOrderUE;

/**
* Class "AcademyBeforeCreatePurchaseOrderUE" in implementing the YFSBeforeCreateOrderUE User Exit.
* Invoking for 'CreatePurchaseOrder' Transaction.
 * 
 */
public class AcademyBeforeCreatePurchaseOrderUE implements YFSBeforeCreateOrderUE {
	/**
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyBeforeCreatePurchaseOrderUE.class);

                private static YIFApi api = null;

                static {
                                try {
                                                api = YIFClientFactory.getInstance().getApi();
                                } catch (YIFClientCreationException e) {
                                                e.printStackTrace();
                                }

                }

                /*
                * (non-Javadoc)
                * 
                 * @see com.yantra.yfs.japi.ue.YFSBeforeCreateOrderUE#beforeCreateOrder(com.yantra.yfs.japi.YFSEnvironment,
                *      org.w3c.dom.Document)
                *      Getting the OrderHeaderKey from the inDoc and call the getOrderList API to get the details for the particulat Order
                *      Stamping the details of LinePriceInfo , LineCharges, LineTaxes and PaymentMethods of SO to the PO.
                *      
                * 
                 */
                public Document beforeCreateOrder(YFSEnvironment env, Document inDoc) {

                                log.verbose("input xml for PO................."
                                                                + XMLUtil.getXMLString(inDoc));

                                try {
                                                /* retrieve list of OrderLine */
                                                NodeList list = inDoc.getElementsByTagName("OrderLine");
                                                Element orderLineEle = (Element) list.item(0);

                                                /* creating the Document with Order as the root node */
                                                Document getOrderListInput = XMLUtil.createDocument("Order");
                                                String salesOHK = orderLineEle
                                                                                .getAttribute("ChainedFromOrderHeaderKey");
                                                log.verbose("ohk of is************" + salesOHK);
                                                getOrderListInput.getDocumentElement().setAttribute(
                                                                                "OrderHeaderKey", salesOHK);
                                                log.verbose("input :: "
                                                                                + XMLUtil.getXMLString(getOrderListInput));
                                                Document getOrderListOutput = getSODetails(env, getOrderListInput);
                                                log.verbose("output xml of getOrderlist for PO................."+ XMLUtil.getXMLString(getOrderListOutput));
                                                                                                                

                                                //
                                                copyAttributesFromSOToPO(inDoc, list, getOrderListOutput);

                                } catch (Exception e) {
                                                e.printStackTrace();
                                }

                                log.verbose("final xml for PO................."
                                                                + XMLUtil.getXMLString(inDoc));

                                return inDoc;
                }

                /**
                * @param env
                * @param getOrderListInput (passing the OrderHeaderKey of the SO for the input xml of getOrderList)
                * @return Document
                * @throws Exception
                * 
                * By setting the template for the getOrderList API , will get all the details of the SO.
                */
                private Document getSODetails(YFSEnvironment env, Document getOrderListInput)
                                                throws Exception {
                                Document getOrderListOutput = null;

                                /* Creating the XML Template for the getOrderList api */
                                Document getOrderListTemplate = XMLUtil
                                                                .getDocument("<Order OrderHeaderKey=\" \">"
                                                                                                + "<PersonInfoBillTo FirstName=\" \" LastName=\" \" />" +"<OrderLines>" + "<OrderLine OrderLineKey=\" \" CarrierServiceCode=\"\" SCAC=\"\">"
                                                                                                + "<LinePriceInfo />" + "<LineCharges>"
                                                                                                + "<LineCharge/>" + "</LineCharges>" + "<LineTaxes>"
                                                                                                + "<LineTax />" + "</LineTaxes>" + "<Extn />"
																								+"</OrderLine>"+ "</OrderLines>" + "</Order>");
                                env.setApiTemplate("getOrderList", getOrderListTemplate);
                                /* invoking the getOrderDetails Api with getOrderListInput xml */
                                getOrderListOutput = api.getOrderDetails(env, getOrderListInput);
                                return getOrderListOutput;
                }

                /**
                * @param inDoc (input xml for the BeforeCreateOrderUE)
                * @param list (OrderLine NodeList which contains the all Order Lines)
                * @param getOrderListOutput (Document after invoking the getOrderList API with the relevant template)
                * Stamping the details of SO to PO in the OrderLine Level.
                */
                public void copyAttributesFromSOToPO(Document inDoc, NodeList list,
                                                Document getOrderListOutput) 
                {
                	Element elegetOrderListOutput=getOrderListOutput.getDocumentElement();
                	String strSalesOrderNo=elegetOrderListOutput.getAttribute("OrderNo");
                	
                	Element eleinDoc=inDoc.getDocumentElement();
                	eleinDoc.setAttribute("OrderName",strSalesOrderNo);
                	
                	inDoc.getElementsByTagName("Order").item(0).appendChild(
                			inDoc.importNode(((Element) getOrderListOutput.getElementsByTagName("Order").item(0)).
                					getElementsByTagName("PersonInfoBillTo").item(0), true));
                	 
                                for (int i = 0; i < list.getLength(); i++) {

                                                Element purchaseOrderLineEle = (Element) list.item(i);
                                                String salesOLK = purchaseOrderLineEle
                                                                                .getAttribute("ChainedFromOrderLineKey");
                                                NodeList golList = getOrderListOutput
                                                                                .getElementsByTagName("OrderLine");

                                                for (int j = 0; j < golList.getLength(); j++) {
                                                                Element golele = (Element) golList.item(j);
                                                                if (salesOLK.equals(golele.getAttribute("OrderLineKey"))) {
                                                                                /* importing the node and appending it */
                                                                	purchaseOrderLineEle.setAttribute("SCAC", golele.getAttribute("SCAC"));
                                                                	purchaseOrderLineEle.setAttribute("CarrierServiceCode", golele.getAttribute("CarrierServiceCode"));
                                                                	
                                                                	
                                                        inDoc.getElementsByTagName("OrderLine").item(i).appendChild(inDoc.importNode(((Element) getOrderListOutput.getElementsByTagName("OrderLine").item(j)).getElementsByTagName("LinePriceInfo").item(0), true));
                                                                                                                
                                                                                                                                                                                                                
                                                                                inDoc.getElementsByTagName("OrderLine").item(i).appendChild(inDoc.importNode(((Element) getOrderListOutput.getElementsByTagName("OrderLine").item(j)).getElementsByTagName("LineCharges").item(0), true));
                                                                                                                
                                                                                                                                                                                                               
                                                             
                                                                                                                                                                                                                                               
                                                                                                                                                                                                                
                                                                                inDoc.getElementsByTagName("OrderLine").item(i)
                                                                                                                .appendChild(inDoc.importNode(((Element) getOrderListOutput.getElementsByTagName("OrderLine").item(j)).getElementsByTagName("LineTaxes").item(0), true));
                                                                                                                                                
                                                                            
																			 inDoc.getElementsByTagName("OrderLine").item(i).
																			 appendChild(inDoc.importNode(((Element) getOrderListOutput.
																					 getElementsByTagName("OrderLine").item(j)).
																					 getElementsByTagName("Extn").item(0), true));
																			 
																			 
                                                                                log.verbose("end of loop");
                                                                }
                                                }
                                }
                }

public String beforeCreateOrder(YFSEnvironment arg0, String arg1) throws YFSUserExitException {
	// TODO Auto-generated method stub
	return arg1;
}
}

