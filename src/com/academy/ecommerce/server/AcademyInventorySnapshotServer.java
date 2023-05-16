package com.academy.ecommerce.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyInventorySnapshotServer extends YCPBaseAgent {
	
	
	/*
     * Instance of logger
     */
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyInventorySnapshotServer.class);

	
	public List getJobs(YFSEnvironment env, Document inXML, Document lastMessage)
			throws Exception {

		List aList = new ArrayList();

		return aList;

	}

	  /*
	   * Call getItemList to get list of all bundle parent item and components. 
	   * Call method to Prepare Map which stores Component item and its corresponding parent item
     * 		This method calls getInventorySnapshot API with input parameters.
     *  	Calls replaceComponents method to replace component items with parent item as obtained from step1
     *  	Calls Service to drop final snapshot message to JMS Q
     */
	public void executeJob(YFSEnvironment env, Document inDoc) throws Exception {

		String sNode = null;
		String sOrganizationCode = null;
		String sDropService = null;
		Document finalDoc = null;

		if (!YFCObject.isVoid(inDoc)) {
			sNode = inDoc.getDocumentElement().getAttribute(AcademyConstants.SHIP_NODE);
			sOrganizationCode = inDoc.getDocumentElement().getAttribute(
					AcademyConstants.ORGANIZATION_CODE);
			sDropService = inDoc.getDocumentElement().getAttribute("Service");

			log.verbose("InputXML to Execute Job is  ........ "
					+ XMLUtil.getXMLString(inDoc));

			if (!YFCObject.isVoid(sNode)
					&& !YFCObject.isVoid(sOrganizationCode)
					&& !YFCObject.isVoid(sDropService)) {

				//First get the component item details. This is done so that child
				//	items can be replace with parent item in snapshot.

				Document inItemAPIDoc = XMLUtil
						.createDocument(AcademyConstants.ITEM);

				Element inElemItemAPI = inItemAPIDoc.getDocumentElement();

				Element ePrimaryElement = XMLUtil.createElement(inItemAPIDoc,
						AcademyConstants.PRIM_INFO, null);

				XMLUtil.appendChild(inElemItemAPI, ePrimaryElement);

				ePrimaryElement.setAttribute(AcademyConstants.KIT_CODE,
						AcademyConstants.BUNDLE);

				log.verbose("InputXML  to getItemList ........ "
						+ XMLUtil.getXMLString(inItemAPIDoc));

				Document outDocItemList = AcademyUtil.invokeService(env,
						"AcademygetItemList", inItemAPIDoc);

				log.verbose("Output of getItemList ........ "
						+ XMLUtil.getXMLString(outDocItemList));

				env.clearApiTemplate("getItemList");

				HashMap<String, String> mCompItem = prepareMapForCompAndParent(outDocItemList);
				
				Document inAPIDoc = XMLUtil
						.createDocument("GetInventorySnapShot");

				Element inAPIElem = inAPIDoc.getDocumentElement();

				inAPIElem.setAttribute(AcademyConstants.ORGANIZATION_CODE, sOrganizationCode);

				inAPIElem.setAttribute(AcademyConstants.SHIP_NODE, sNode);

				log.verbose("InputXML  to getInventorySnapshot API is ........ "
								+ XMLUtil.getXMLString(inAPIDoc));

				Document outDocSnapshot = AcademyUtil.invokeAPI(env,
						"getInventorySnapShot", inAPIDoc);

				log.verbose("Output XMl of getInventorySnapshot and input to replaceComponents method........ "
								+ XMLUtil.getXMLString(outDocSnapshot));
				
				
				finalDoc = replaceComponents(mCompItem, outDocSnapshot);
				
				//finalDoc = updateBundleQtyWithMinComp(beforeBundleQtyDoc);
			
				YFCDocument dDoc = YFCDocument.getDocumentFor(finalDoc);
				YFCElement eElem = dDoc.getDocumentElement();
				
				YFCDate d = new YFCDate();
				
				eElem.setDateTimeAttribute("TimeStamp", d);
				
				log.verbose("Final Snapshot XML to Drop Service is  ........ "
						+ XMLUtil.getXMLString(finalDoc));
				
				AcademyUtil.invokeService(env, sDropService, finalDoc);

			}

		}
	}

	private HashMap<String, String> prepareMapForCompAndParent(
			Document outDocItemList) {
		
		HashMap<String, String> mCompItem = new HashMap<String, String>();
		
		String sItemId = "";

		String sComponentItem = "";
		
		if (!YFCObject.isVoid(outDocItemList)) {

			Element eItemListElem = outDocItemList.getDocumentElement();

			List nl = XMLUtil.getSubNodeList(eItemListElem,
					AcademyConstants.ITEM);
			int iListSize = nl.size();
			for (int i = 0; i < iListSize; i++) {

				Element eItemElem = (Element) nl.get(i);

				Element eComponents = XMLUtil.getFirstElementByName(eItemElem,
						AcademyConstants.COMPONENTS);
				
				sItemId = eItemElem.getAttribute(AcademyConstants.ITEM_ID);

				List nL1 = XMLUtil.getSubNodeList(eComponents, AcademyConstants.COMPONENT);
				int iList1Size = nL1.size();
				for (int j = 0; j < iList1Size; j++) {

					Element eComponent = (Element) nL1.get(j);
					
					sComponentItem = eComponent.getAttribute(AcademyConstants.COMPONENT_ITEM_ID);
					mCompItem.put(sComponentItem, sItemId);

				}

			}
		}
		
		log.verbose("Output of prepareMapForCompAndParent method  ........ "
				+ mCompItem.toString());
		return mCompItem;
	}

	private Document replaceComponents(HashMap<String, String> mCompItem,
			Document outDocSnapshot) {
		
		log.verbose("Input to replace Components is "
				+ XMLUtil.getXMLString(outDocSnapshot));
//Declaration of variables 
		HashMap<String,Double> mCompQty= new HashMap<String, Double>();		
		ArrayList<Element> aLCompElement = new ArrayList<Element>();		
		String sSnapshotItem = "";
		int iALLength;		
		String sParentItem = "";		
		String  sCompQty="";		
		String sSnapshotItem1 = "";
	// Variable declaration ends
		
		if (!YFCObject.isVoid(outDocSnapshot)) {
			
			Element eInventorySnapshot = outDocSnapshot.getDocumentElement();

			Element sShipnode = XMLUtil.getFirstElementByName(
					eInventorySnapshot, AcademyConstants.SHIP_NODE);

			List lsnapshotItemList = XMLUtil.getSubNodeList(sShipnode,
					AcademyConstants.ITEM);

			
			for (int k = 0; k < lsnapshotItemList.size(); k++) {

				Element elemSnapshotItem = (Element) lsnapshotItemList.get(k);
				
				sSnapshotItem = elemSnapshotItem.getAttribute(AcademyConstants.ITEM_ID);
				
				if (mCompItem.containsKey(sSnapshotItem)) {

					String sparentItem = (String) mCompItem.get(sSnapshotItem);					
					elemSnapshotItem.setAttribute(AcademyConstants.ITEM_ID,	sparentItem);
					aLCompElement.add(elemSnapshotItem);

				}

			}
		}
		
		/* Logic below will iterate through array list containing parent
		item element and update quantity with minimum Qty of any comp item */
		
		 iALLength = aLCompElement.size();
		 for (int i=0; i< iALLength; i++){
			
			Element elemSnapshotItem = aLCompElement.get(i);
			
			Element eSupplyDetails = XMLUtil.getFirstElementByName(elemSnapshotItem, AcademyConstants.SUPPLY_DETAILS);
			//get Qty of first parent item
			sCompQty = eSupplyDetails.getAttribute(AcademyConstants.ATTR_QUANTITY);
			
			Double dCompQty = Double.valueOf(sCompQty);
			
			sParentItem = elemSnapshotItem.getAttribute(AcademyConstants.ITEM_ID);
			
			/*If Parent item is already in map then compare Qty in map.
			If it is less that update supply details element */
			
			if (mCompQty.containsKey(sParentItem)) {

				Double dMapCompQty = (Double) mCompQty.get(sParentItem);
				
				if (dMapCompQty > dCompQty){
					mCompQty.put(sParentItem, dCompQty);
					
				}
			}	else mCompQty.put(sParentItem, dCompQty);
		}
		 
		 // Map mCompQty will have minimun of Comp Qty and parent item as key
		 
		for (int i=0; i< iALLength; i++){		
			Element elemSnapshotItem = aLCompElement.get(i);
			
			log.verbose("Input before replacing minimujm Qty"
					+ XMLUtil.getXMLString(elemSnapshotItem.getOwnerDocument()));
			
			Element eSupplyDetails = XMLUtil.getFirstElementByName(elemSnapshotItem, AcademyConstants.SUPPLY_DETAILS);
			sSnapshotItem1 = elemSnapshotItem.getAttribute(AcademyConstants.ITEM_ID);
			eSupplyDetails.setAttribute(AcademyConstants.ATTR_QUANTITY, Double.toString(mCompQty.get(sSnapshotItem1)));
			
			log.verbose("Output after replacing minimujm Qty"
					+ XMLUtil.getXMLString(elemSnapshotItem.getOwnerDocument()));
			
		}
			
		return outDocSnapshot;
	}

}
