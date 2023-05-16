package com.academy.ecommerce.sterling.sts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.yantriks.inventory.YASPostNodeControlToKafka;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySTSCreateInventoryNodeControl implements YIFCustomApi {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSCreateInventoryNodeControl.class);

	private Properties props;

	public Document createInventoryNodeControl(YFSEnvironment env, Document docInput) throws Exception {

		log.beginTimer("createInventoryNodeControl");
		log.verbose("Begin - createInventoryNodeControl() :: ");
		log.verbose("Input XML :: " + XMLUtil.getXMLString(docInput));

		if (AcademyConstants.STR_YES.equalsIgnoreCase(props.getProperty(AcademyConstants.STR_APPLY_INV_NODE_CONTROL))) {

			

			NodeList nlShipmentLine = docInput.getDocumentElement()
					.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

			Document docMultiAPI = XMLUtil.createDocument(AcademyConstants.ELE_MULTIAPI);
			String strShipNode = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE);
			String strSource = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SOURCE);
			String strDocumentType = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_DOC_TYPE);
			String strInvPicIncorrectTillDate = getInvPicIncorrectTillDate(strDocumentType);

			for (int iSL = 0; iSL < nlShipmentLine.getLength(); iSL++) {

				Element eleShipmentLine = (Element) nlShipmentLine.item(iSL);

				String strItemID = eleShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID);
				String strShortageQty = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHORTAGE_QTY);

				double dShortageQty = 0;

				if (!StringUtil.isEmpty(strShortageQty)) {
					dShortageQty = Double.parseDouble(strShortageQty);
				}

				if ((AcademyConstants.STR_SOURCE_RCP.equals(strSource) && dShortageQty > 0)
						|| AcademyConstants.STR_SOURCE_WMS.equals(strSource)) {
					
					log.verbose("strSource :: "+strSource);
					log.verbose("dShortageQty :: "+dShortageQty);

					
					/** OMNI-34709 : Start **/
					
					Map<String,String> YantrkNCEnabled = YASPostNodeControlToKafka.getNodeControlValue(env);

					if (!YFCObject.isVoid(YantrkNCEnabled) && YantrkNCEnabled.get(AcademyConstants.OMS_NODE_CONTROL_CODE_VALUE).
							equalsIgnoreCase(AcademyConstants.STR_YES)) {

						log.verbose("OMS node control is ON in STS");

						Element eleAPI = docMultiAPI.createElement(AcademyConstants.ELE_API);
						eleAPI.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.API_MANAGE_INVENTORY_NODE_CONTROL);
						docMultiAPI.getDocumentElement().appendChild(eleAPI);

						Element eleInput = docMultiAPI.createElement(AcademyConstants.ELE_INPUT);
						eleAPI.appendChild(eleInput);

						Element eleInvNodeControl = docMultiAPI.createElement(AcademyConstants.INVENTORY_NODE_CONTROL);
						eleInvNodeControl.setAttribute(AcademyConstants.ATTR_INV_PIC_INCORRECT_TILL_DATE,
								strInvPicIncorrectTillDate);
						eleInvNodeControl.setAttribute(AcademyConstants.ATTR_PROD_CLASS, AcademyConstants.PRODUCT_CLASS);
						eleInvNodeControl.setAttribute(AcademyConstants.ATTR_NODE_CONTROL_TYPE,
								AcademyConstants.STR_ON_HOLD);
						eleInvNodeControl.setAttribute(AcademyConstants.ORGANIZATION_CODE,
								AcademyConstants.PRIMARY_ENTERPRISE);
						eleInvNodeControl.setAttribute(AcademyConstants.ATTR_UOM, AcademyConstants.UNIT_OF_MEASURE);
						eleInvNodeControl.setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemID);
						eleInvNodeControl.setAttribute(AcademyConstants.ATTR_NODE, strShipNode);
						eleInput.appendChild(eleInvNodeControl);
					}

					if (!YFCObject.isVoid(YantrkNCEnabled) && YantrkNCEnabled.get(AcademyConstants.YFS_NODE_CONTROL_CODE_VALUE).equalsIgnoreCase(AcademyConstants.STR_YES)) {
						log.verbose("Yantriks node control is ON in STS");
						YASPostNodeControlToKafka.kafkaUpdateForSTSNC(env, docInput);
					} 
					
					/** OMNI-34709 : End **/
				}

			}

				log.verbose("Input - MultiApi :: " + XMLUtil.getXMLString(docMultiAPI));

				AcademyUtil.invokeAPI(env, AcademyConstants.API_MULTI_API, docMultiAPI);
			}

		
		log.verbose("End - createInventoryNodeControl() :: ");
		log.endTimer("createInventoryNodeControl");
		return docInput;
	}

	private String getInvPicIncorrectTillDate(String strDocType) throws Exception {

		log.verbose("BEGIN - getInvPicIncorrectTillDate() :: ");
		String strInvNodeControlDuration = "";

		if (AcademyConstants.SALES_DOCUMENT_TYPE.equals(strDocType)) {
			strInvNodeControlDuration = props.getProperty(AcademyConstants.STR_INV_NODE_CONTROL_DURATION_IN_HOURS_SO);
		} else if (AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE.equals(strDocType)) {
			strInvNodeControlDuration = props.getProperty(AcademyConstants.STR_INV_NODE_CONTROL_DURATION_IN_HOURS_TO);
		}

		log.verbose("Inv Node Control Duration :: " + strInvNodeControlDuration);

		Integer iHours = Integer.parseInt(strInvNodeControlDuration);
		DateFormat dateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, iHours);
		log.verbose("END - getInvPicIncorrectTillDate() :: ");

		return dateFormat.format(cal.getTime());
	}

	@Override
	public void setProperties(Properties props) throws Exception {
		this.props = props;

	}

}
