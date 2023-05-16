package com.academy.ecommerce.sterling.inventory.api;

import java.math.BigDecimal;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.util.YFCUtils;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyManageNodeOnNegativeAdjust implements YIFCustomApi {

	private static Logger log = Logger.getLogger(AcademyManageNodeOnNegativeAdjust.class.getName());
	private Properties props;

	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	public Document prepareInputForManageNodeMultiAPI(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Entering AcademyManageNodeOnNegativeAdjust() :: " + XMLUtil.getXMLString(inDoc));
		String strAdjustmentType = null;
		String strQuantity = null;

		Element eleSupply = null;
		Document docMultiAPI = null;

		eleSupply = inDoc.getDocumentElement();
		strQuantity = eleSupply.getAttribute("Quantity");
		strAdjustmentType = eleSupply.getAttribute("AdjustmentType");
		String strShipNode=eleSupply.getAttribute("ShipNode");
		String strEligibleNodeType = props.getProperty("ELIGIBLE_NODE_TYPE");
		log.debug("strEligibleNodeType" + strEligibleNodeType);

		int count = 0;
		if (!YFCObject.isVoid(strQuantity) && !YFCObject.isVoid(strAdjustmentType)&&!YFCObject.isVoid(strShipNode)) {

			BigDecimal strBigQuantity = new BigDecimal(YFCUtils.isVoid(strQuantity) ? "0.00" : strQuantity);

			if (strBigQuantity.intValue() < 0 && strAdjustmentType.equals("ADJUSTMENT")) {
				
				//Start BOPIS 2050
				Document tempgetShipNodeListDoc = YFCDocument.getDocumentFor(
						"<ShipNode ShipNode=\"\" NodeType=\"\" />")
						.getDocument();
				
				env.setApiTemplate(AcademyConstants.API_GET_SHIP_NODE_LIST, tempgetShipNodeListDoc);
				Document docgetShipNodeListIP=XMLUtil.createDocument("ShipNode");
				Element elegetShipNodeList=docgetShipNodeListIP.getDocumentElement();
				elegetShipNodeList.setAttribute("ShipNode",strShipNode );
				Document docgetShipNodeListOP=AcademyUtil.invokeAPI
						(env,AcademyConstants.API_GET_SHIP_NODE_LIST, docgetShipNodeListIP);
				
				Element elegetShipNodeListOP=(Element) docgetShipNodeListOP.getElementsByTagName("ShipNode").item(0);
				String strNodeType=elegetShipNodeListOP.getAttribute("NodeType");
				
				if(strEligibleNodeType.contains(strNodeType)){
				//End BOPIS 2050
				count++;
				docMultiAPI = XMLUtil.createDocument("MultiApi");
				Element eleApiInventoryNodeControl = docMultiAPI.createElement("API");
				eleApiInventoryNodeControl.setAttribute("Name", "manageInventoryNodeControl");
				Element eleInput = docMultiAPI.createElement("Input");
				Element eleInventoryNodeControl = docMultiAPI.createElement("InventoryNodeControl");
				eleInventoryNodeControl.setAttribute("InvPictureIncorrectTillDate",
						eleSupply.getAttribute("ShipByDate"));
				eleInventoryNodeControl.setAttribute("InventoryPictureCorrect", "Y");
				eleInventoryNodeControl.setAttribute("ItemID", eleSupply.getAttribute("ItemID"));
				eleInventoryNodeControl.setAttribute("Node", eleSupply.getAttribute("ShipNode"));
				eleInventoryNodeControl.setAttribute("NodeControlType", "ON_HOLD");
				eleInventoryNodeControl.setAttribute("OrganizationCode", eleSupply.getAttribute("InventoryOrganizationCode"));
				eleInventoryNodeControl.setAttribute("ProductClass", eleSupply.getAttribute("ProductClass"));
				eleInventoryNodeControl.setAttribute("UnitOfMeasure", eleSupply.getAttribute("UnitOfMeasure"));
				XMLUtil.appendChild(eleInput, eleInventoryNodeControl);
				XMLUtil.appendChild(eleApiInventoryNodeControl, eleInput);
				XMLUtil.appendChild(docMultiAPI.getDocumentElement(), eleApiInventoryNodeControl);

				Element eleApiInventoryActivity = docMultiAPI.createElement("API");
				eleApiInventoryActivity.setAttribute("Name", "createInventoryActivity");
				Element eleInputInventoryActivity = docMultiAPI.createElement("Input");
				Element eleInventoryActivity = docMultiAPI.createElement("InventoryActivity");
				eleInventoryActivity.setAttribute("CreateForInvItemsAtNode", "N");
				eleInventoryActivity.setAttribute("ItemID", eleSupply.getAttribute("ItemID"));
				eleInventoryActivity.setAttribute("Node", eleSupply.getAttribute("ShipNode"));
				eleInventoryActivity.setAttribute("OrganizationCode", eleSupply.getAttribute("InventoryOrganizationCode"));
				eleInventoryActivity.setAttribute("ProductClass", eleSupply.getAttribute("ProductClass"));
				eleInventoryActivity.setAttribute("UnitOfMeasure", eleSupply.getAttribute("UnitOfMeasure"));
				XMLUtil.appendChild(eleInputInventoryActivity, eleInventoryActivity);
				XMLUtil.appendChild(eleApiInventoryActivity, eleInputInventoryActivity);
				XMLUtil.appendChild(docMultiAPI.getDocumentElement(), eleApiInventoryActivity);

				AcademyUtil.invokeAPI(env, AcademyConstants.API_MULTI_API, docMultiAPI);
			}
			}
		}

		if (count == 0) {
			docMultiAPI = XMLUtil.getDocument("<ApiSuccess />");
		}

		return docMultiAPI;
	}

}
