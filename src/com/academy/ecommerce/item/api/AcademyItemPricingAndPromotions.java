package com.academy.ecommerce.item.api;

import java.io.File;
import java.io.FileInputStream;
import org.w3c.dom.Document;

import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.ue.YFSGetExternalPricesForItemListUE;
import com.yantra.yfs.japi.ue.YFSGetPromotionsForItemListUE;

public class AcademyItemPricingAndPromotions implements YFSGetExternalPricesForItemListUE,YFSGetPromotionsForItemListUE {
	/**
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyItemPricingAndPromotions.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
 
	}
	public Document getExternalPricesForItemList(YFSEnvironment env, Document doc) throws com.yantra.yfs.japi.YFSUserExitException {
		log.verbose("inXML to getExternalPricesForItemList"+YFCDocument.getDocumentFor(doc));
		log.verbose("getExternalPricesForItemList");
		YFCDocument outDoc = null;
		try{ 
			outDoc = YFCDocument.parse(new FileInputStream(new File("D:\\Academy80\\Interfaces\\getExternalPricesForItemList.xml")));
		}catch (Exception e){
			e.printStackTrace();
		}
		return outDoc.getDocument();
	}
	public Document getPromotionsForItemList(YFSEnvironment env, Document doc) throws com.yantra.yfs.japi.YFSUserExitException {
		log.verbose("inXML to getPromotionsForItemList"+YFCDocument.getDocumentFor(doc));
		log.verbose("getPromotionsForItemList");
		YFCDocument outDoc = null;
		try{
			outDoc = YFCDocument.parse(new FileInputStream(new File("D:\\Academy\\XMLS\\getPromotionsForItemList.xml")));
		}catch (Exception e){
			e.printStackTrace();
		}
		return outDoc.getDocument();
	}
}
