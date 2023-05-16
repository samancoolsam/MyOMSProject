package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.shared.ycp.YFSContext;
import com.yantra.yfc.date.YTimestamp;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author kparvath
 * Fix for STL-947
 * Added as part of Shared Inventory.
 * 
 * Getting the Sequence # from Custom DB Sequence (EXTN_TRANSFER_NO_SEQ) and storing in YFS_SHIPMENT table as extended attribute.
 * 
 * setting the Sequence # to the inDoc, which will be used in XSL and published to RMS System.
 *
 */
public class AcademyGetTransferNo implements YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyGetTransferNo.class);

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}
	public Document getTransferNo(YFSEnvironment env, Document inDoc) throws Exception
	{
		log.verbose("Input Doc::" + XMLUtil.getXMLString(inDoc));
		YFSContext oEnv = (YFSContext) env;
		long seqTransferNo = oEnv.getNextDBSeqNo("EXTN_TRANSFER_NO_SEQ");

		String transferNo = String.valueOf(seqTransferNo);

		log.verbose("SeqSegmentNo " + transferNo);
		
		Element eleEXTN = (Element) inDoc.getElementsByTagName("Extn").item(0);
		eleEXTN.setAttribute("ExtnRMSTransferNo", transferNo);

		//Preapre changeShipment input and invoke to store RMS Transfer No. in YFS_SHIPMENT table.
		Document inDocChangeShipment = XMLUtil.createDocument("Shipment");

		inDocChangeShipment.getDocumentElement().setAttribute("ShipmentKey", inDoc.getDocumentElement().getAttribute("ShipmentKey"));

		Element eleExtn = inDocChangeShipment.createElement("Extn");
		eleExtn.setAttribute("ExtnRMSTransferNo", transferNo);
		inDocChangeShipment.getDocumentElement().appendChild(eleExtn);

		log.verbose("Change Shipment Input XML::"+ XMLUtil.getXMLString(inDocChangeShipment));
		Document outDocChangeShipment = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, inDocChangeShipment);

		log.verbose("Change Shipment Output XML::"+ XMLUtil.getXMLString(outDocChangeShipment));

		//getting Current DateTime and stamping to the inDoc, need to publish this DateTime to RMS
		
		/*YTimestamp currentDateTime = YTimestamp.newMutableTimestamp(); 
		String strCurrentDateTime = currentDateTime.getString("yyyy-MM-dd'T'HH:mm:s");
		inDoc.getDocumentElement().setAttribute("CurrentDateTime", strCurrentDateTime);*/
		
		log.verbose("outDoc ::"+ XMLUtil.getXMLString(inDoc));
		
		return inDoc;
	}
}

