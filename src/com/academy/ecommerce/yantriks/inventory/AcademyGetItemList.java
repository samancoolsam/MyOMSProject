package com.academy.ecommerce.yantriks.inventory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyGetItemList {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyGetItemList.class);

	/*
	 * <?xml version="1.0" encoding="UTF-8"?> <Item IgnoreOrdering="Y" ItemID="241089654"
	 * MaximumRecords="500" OrganizationCode="DEFAULT"/>
	 */

	/**
	 * This method get the item details
	 * 
	 * @param env
	 * @param inDoc
	 * @return Document
	 * @throws Exception
	 */

	public Document getItemList(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer("AcademyGetItemList::getItemList");
		log.debug("AcademyGetItemList.getItemList() Input XML ::" + XMLUtil.getXMLString(inDoc));
		Document docOut=null;
		Element eleItem = inDoc.getDocumentElement();
		String strItemID = eleItem.getAttribute(AcademyConstants.ITEM_ID);
		try {
			if (!YFCObject.isVoid(strItemID)) {
				log.debug("ItemID is :"+strItemID);
				docOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST, inDoc);
				Element eleItemList = docOut.getDocumentElement();
				Element eleOutItem = (Element) eleItemList.getElementsByTagName(AcademyConstants.ITEM).item(0);
				if (YFCCommon.isVoid(eleOutItem)) {
					YFSException yfsException = new YFSException();
					yfsException.setErrorDescription("YFS:Invalid ItemID");
					yfsException.setErrorCode("YFS10047");
					throw yfsException;
				}
				
			} else {
				YFSException yfsException = new YFSException();
				yfsException.setErrorDescription("YFS:Blank ItemID");
				yfsException.setErrorCode("YFS10047");
				throw yfsException;
			}
		} catch (Exception yfsException) {
			throw yfsException;
		}
		log.endTimer("AcademyGetItemList::getItemList");
		return docOut;
	}
}
