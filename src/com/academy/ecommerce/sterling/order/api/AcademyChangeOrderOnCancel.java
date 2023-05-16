package com.academy.ecommerce.sterling.order.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyChangeOrderOnCancel implements YIFCustomApi {

	private Properties	props;
	public void setProperties(Properties props) throws Exception
	{
		this.props = props;
	}
	private YFCException yfcEx = null;

	private static final YFCLogCategory log = YFCLogCategory
			.instance(AcademyChangeOrderOnCancel.class);

	public Document cancelOrder(YFSEnvironment env, Document inDoc) throws Exception {
		log
				.beginTimer(" Begining of AcademyChangeOrderOnCancel -> cancelOrder Api");
		validateModReasonCode(inDoc);
		/*Retrive the ArrayList of all the shipmentKey's set in the env Object in the BeforeChangeOrderUE*/
		ArrayList lShipments = (ArrayList) env.getTxnObject("ShipmentKey");
		if (lShipments!=null && lShipments.size() > 0) {
			callMultiApiForShipmentUpdate(env, lShipments, lShipments.size());
		}
		log.endTimer(" End of AcademyChangeOrderOnCancel  -> cancelOrder Api");
		return inDoc;
	}

	public void validateModReasonCode(Document inDoc)
	
	{
		String sModifyprogIdArg=props.getProperty("Modifyprogid");
		String sModifyprogId = inDoc.getDocumentElement().getAttribute("Modifyprogid");
			if(sModifyprogId.equals(sModifyprogIdArg))
			{
				Element eleOrderAudit = (Element)inDoc.getElementsByTagName("OrderAudit").item(0);
				String sReasonCode = eleOrderAudit.getAttribute("ReasonCode");
				if(StringUtil.isEmpty(sReasonCode))
				{
					
					log.verbose("Reason Code is Empty");
			        yfcEx = new YFCException();
			        yfcEx.setAttribute(YFCException.ERROR_CODE,"EXTN_ACADEMY_CANCEL");
			        yfcEx.setAttribute(YFCException.ERROR_DESCRIPTION,"Reason Code is Mandatory for Cancel");
			        yfcEx.printStackTrace();
			        throw yfcEx;
				}
			}
	}
	private void callMultiApiForShipmentUpdate(YFSEnvironment env,
			List lShipments, int iShipments) {
		Document docMultiApi;
		log
				.beginTimer(" Begining of AcademyChangeOrderOnCancel -> callMultiApiForShipmentUpdate Api");
		try {
			docMultiApi = XMLUtil.createDocument("MultiApi");
			Element eleMultiApi = docMultiApi.getDocumentElement();
			for (int j = 0; j < iShipments; j++) {
				Element eleApi = docMultiApi.createElement("API");
				eleMultiApi.appendChild(eleApi);
				eleApi.setAttribute("Name", "changeShipment");
				Element eleInput = docMultiApi.createElement("Input");
				eleApi.appendChild(eleInput);
				String strShipKey = (String) lShipments.get(j);
				Element eleShipment = docMultiApi.createElement("Shipment");
				eleInput.appendChild(eleShipment);
				eleShipment.setAttribute("ShipmentKey", strShipKey);
				Element eleExtn = docMultiApi.createElement("Extn");
				eleExtn.setAttribute("ExtnLinesCancelled", "Y");
				eleShipment.appendChild(eleExtn);
			}
			log.verbose("*** Input to MultiApi is *****"
					+ XMLUtil.getXMLString(docMultiApi));
			AcademyUtil.invokeAPI(env, "multiApi", docMultiApi);
			log
					.verbose("**** Shipment has been updated with ExtnLinesCancelled Flag");
			log
					.endTimer(" End of AcademyChangeOrderOnCancel  -> callMultiApiForShipmentUpdate Api");
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
