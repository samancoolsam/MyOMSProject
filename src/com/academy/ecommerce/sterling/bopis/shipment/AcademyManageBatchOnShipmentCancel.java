package com.academy.ecommerce.sterling.bopis.shipment;

import java.util.LinkedHashSet;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.util.YFCUtils;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyManageBatchOnShipmentCancel implements YIFCustomApi {

	private static Logger log = Logger.getLogger(AcademyManageBatchOnShipmentCancel.class.getName());
	private Properties props;
	Document docOutGetStoreBatchList = null;
	LinkedHashSet<String> listShipmentKeys = null;
	String strShipmentKey = "";
	String strBatchStatus="";

	public void setProperties(Properties arg0) throws Exception {
		this.props = props;
	}

	public Document prepareInputToManageBatch(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Entering AcademyManageBatchOnShipmentCancel.resetBatch() :: " + XMLUtil.getXMLString(inDoc));
		String strStoreBatchKey = null;
		String strShipmentLineKey = null;
		NodeList nlShipmentline = null;
		Element eleShipmentline = null;
		Document docManageBatch = null;
		boolean needManageBatch = true;
		String storeBatchKey = "";
		docManageBatch = XMLUtil.createDocument("MultiApi");
		Element eleApiChangeShipment = docManageBatch.createElement("API");
		eleApiChangeShipment.setAttribute("Name", "changeShipment");
		Element eleInput = docManageBatch.createElement("Input");
		Element eleMAShipment = docManageBatch.createElement("Shipment");
		strShipmentKey = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		eleMAShipment.setAttribute("ShipmentKey", strShipmentKey);
		eleMAShipment.setAttribute("Action", "Modify");
		nlShipmentline = inDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		Element eleShipmentlines = docManageBatch.createElement("ShipmentLines");

		int count = 0;
		// Loop through all the shipments
		for (int i = 0; i < nlShipmentline.getLength(); i++) {

			eleShipmentline = (Element) nlShipmentline.item(i);
			strStoreBatchKey = eleShipmentline.getAttribute(AcademyConstants.ATTR_STORE_BATCH_KEY);

			if (!YFCObject.isVoid(strStoreBatchKey)) {
				storeBatchKey = strStoreBatchKey;
			}

			Element eleMAShipmentline = docManageBatch.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
			eleMAShipmentline.setAttribute("Action", "Modify");
			strShipmentLineKey = eleShipmentline.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
			eleMAShipmentline.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, strShipmentLineKey);
			eleMAShipmentline.setAttribute("StoreBatchKey", " ");
			XMLUtil.appendChild(eleShipmentlines, eleMAShipmentline);

			count++;

		}

		if (count != 0) {
			XMLUtil.appendChild(eleMAShipment, eleShipmentlines);
			XMLUtil.appendChild(eleInput, eleMAShipment);
			XMLUtil.appendChild(eleApiChangeShipment, eleInput);
			XMLUtil.appendChild(docManageBatch.getDocumentElement(), eleApiChangeShipment);
		}
		log.verbose("StoreBatchKey - " + strStoreBatchKey);

		Document outGetShipmentList = null;
		if (storeBatchKey.equals("")) {
			String strGetShipmentTemplate = "<Shipments><Shipment ShipmentKey=\"\"><ShipmentLines>\r\n"
					+ "<ShipmentLine StoreBatchKey=\"\" ShipmentLineKey=\"\" /> </ShipmentLines> </Shipment> </Shipments>";
			Document outputTemplate = YFCDocument.getDocumentFor(strGetShipmentTemplate).getDocument();
			Document docGetShipmentList = XMLUtil.createDocument("Shipment");
			docGetShipmentList.getDocumentElement().setAttribute("ShipmentKey", strShipmentKey);
			env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, outputTemplate);
			outGetShipmentList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, docGetShipmentList);
			env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);

			if (!YFCObject.isVoid(outGetShipmentList)) {
				Element eleShipmentLine = XMLUtil.getElementByXPath(outGetShipmentList,
						"/Shipments/Shipment/ShipmentLines/ShipmentLine[@StoreBatchKey!=null or @StoreBatchKey!=' ']");
				if (!YFCObject.isVoid(eleShipmentLine)) {
					storeBatchKey = eleShipmentLine.getAttribute("StoreBatchKey");
				}
			}
		}

		if (!YFCObject.isVoid(storeBatchKey)) {
			needManageBatch = isManageBatchRequired(env, storeBatchKey);

			if (needManageBatch) {

				listShipmentKeys.remove(strShipmentKey);
				for (String strshipmentKey : listShipmentKeys) {
					Element eleApiChangeShipmentStatus = docManageBatch.createElement("API");
					eleApiChangeShipmentStatus.setAttribute("Name", "changeShipmentStatus");
					Element eleInputChangeShipmentStatus = docManageBatch.createElement("Input");
					Element eleChangeShipmentStatus = docManageBatch.createElement(AcademyConstants.ELE_SHIPMENT);
					eleChangeShipmentStatus.setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS,
							AcademyConstants.STATUS_READY_FOR_PACK_VAL);
					eleChangeShipmentStatus.setAttribute(AcademyConstants.ATTR_TRANSID,
							AcademyConstants.STR_BACKROOM_PICK_TRAN);
					eleChangeShipmentStatus.setAttribute(AcademyConstants.ATTR_SELL_ORG_CODE,
							AcademyConstants.PRIMARY_ENTERPRISE);
					eleChangeShipmentStatus.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strshipmentKey);

					XMLUtil.appendChild(eleInputChangeShipmentStatus, eleChangeShipmentStatus);
					XMLUtil.appendChild(eleApiChangeShipmentStatus, eleInputChangeShipmentStatus);
					XMLUtil.appendChild(docManageBatch.getDocumentElement(), eleApiChangeShipmentStatus);
				}

				Element eleApiManageBatch = docManageBatch.createElement("API");
				eleApiManageBatch.setAttribute("Name", "manageStoreBatch");
				Element eleInputManageStoreBatch = docManageBatch.createElement("Input");

				Element docInManageStoreBatch = docManageBatch.createElement(AcademyConstants.ELE_STORE_BATCH);
				docInManageStoreBatch.setAttribute(AcademyConstants.ATTR_ACTION, "Modify");
				docInManageStoreBatch.setAttribute(AcademyConstants.ATTR_STORE_BATCH_KEY, storeBatchKey);
				docInManageStoreBatch.setAttribute("Status", "3000");

				XMLUtil.appendChild(eleInputManageStoreBatch, docInManageStoreBatch);
				XMLUtil.appendChild(eleApiManageBatch, eleInputManageStoreBatch);
				XMLUtil.appendChild(docManageBatch.getDocumentElement(), eleApiManageBatch);

			}
		}
		log.verbose("Output from manageStoreBatch API : " + XMLUtil.getXMLString(docManageBatch));

		if (count == 0 ||("3000".equalsIgnoreCase(strBatchStatus))) {
			docManageBatch = XMLUtil.getDocument("<ApiSuccess />");
		}

		return docManageBatch;
	}

	private boolean isManageBatchRequired(YFSEnvironment env, String strStoreBatchKey) throws Exception {
		log.verbose("Entering getStoreBatchDetails()");
		Document docInGetStoreBatchList = null;
		docInGetStoreBatchList = XMLUtil.createDocument(AcademyConstants.ELE_STORE_BATCH);
		docInGetStoreBatchList.getDocumentElement().setAttribute(AcademyConstants.ATTR_STORE_BATCH_KEY,
				strStoreBatchKey);
		String strgetStoreBatchListTemplate = "<StoreBatchList>\r\n" + "	<StoreBatch Status=\"\" StoreBatchKey=\"\">\r\n"
				+ "				<ShipmentLines>\r\n"
				+ "					<ShipmentLine ShipmentKey=\"\" ShipmentLineKey=\"\"\r\n"
				+ "						BackroomPickComplete=\"\" ShortageQty=\"\" />\r\n"
				+ "				</ShipmentLines>\r\n" + "		</StoreBatch>\r\n" + "</StoreBatchList>";

		Document outputTemplate = YFCDocument.getDocumentFor(strgetStoreBatchListTemplate).getDocument();

		log.verbose("Input to getStoreBatchDetails API : " + XMLUtil.getXMLString(docInGetStoreBatchList));
		env.setApiTemplate("getStoreBatchList", outputTemplate);
		docOutGetStoreBatchList = AcademyUtil.invokeAPI(env, "getStoreBatchList", docInGetStoreBatchList);
		env.clearApiTemplate("getStoreBatchList");
		log.verbose("Output from getStoreBatchDetails API : " + XMLUtil.getXMLString(docOutGetStoreBatchList));
        
		 Element eleStoreBatchList = (Element) docOutGetStoreBatchList.getElementsByTagName("StoreBatch").item(0);
		 strBatchStatus=eleStoreBatchList.getAttribute("Status");
		 
		 
		NodeList nlShipmentlines = docOutGetStoreBatchList.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		boolean needManageBatch = true;
		if("3000".equalsIgnoreCase(strBatchStatus))
		{
			needManageBatch=false;
		 return needManageBatch;
		}
		listShipmentKeys = new LinkedHashSet<String>();
		for (int i = 0; i < nlShipmentlines.getLength(); i++) {
			Element eleShipmentLine = (Element) nlShipmentlines.item(i);
			listShipmentKeys.add(eleShipmentLine.getAttribute("ShipmentKey"));
			String strBackroomPickComplete = YFCUtils.isVoid(eleShipmentLine.getAttribute("BackroomPickComplete")) ? ""
					: eleShipmentLine.getAttribute("BackroomPickComplete");

			if (((strBackroomPickComplete.equals("")) || (!strBackroomPickComplete.equals("Y")))
					&& !(eleShipmentLine.getAttribute("ShipmentKey").equals(strShipmentKey))) {
				needManageBatch = false;
			}
		}
		
		log.verbose("Exiting getStoreBatchDetails()");
		return needManageBatch;
	}

}
