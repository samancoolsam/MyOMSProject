package com.academy.ecommerce.sterling.inventory.api;
 
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
 
public class AcademyRemoveElementByAttributeForSynch implements YIFCustomApi {
 
                                
                                private static YFCLogCategory log = YFCLogCategory
                                                                .instance(AcademyRemoveElementByAttributeForSynch.class);
 
                                public void setProperties(Properties arg0) throws Exception {
                                                // TODO Auto-generated method stub
                                }
 
                
                public Document removeElementByAttributeForSynch(YFSEnvironment env, Document inDoc) throws Exception {
                                
                                Element eleInventorySnapShot = null;
                                eleInventorySnapShot = inDoc.getDocumentElement();
                                NodeList getInventorySnapShotItem = eleInventorySnapShot.getElementsByTagName("Item");
                                
                                int i=0,k=0;
                                String strSegmentNo ="";
                                int itemLen = getInventorySnapShotItem.getLength();
                                Element eleInventorySnapShotItem = null;
 
                                log.verbose("*****************itemLength*************:\t" +itemLen);
                                
                                for(i=0;i<itemLen;i++)
                                {
                                                eleInventorySnapShotItem = (Element) getInventorySnapShotItem.item(i);
                                                NodeList getInventorySnapShotSupply = eleInventorySnapShotItem.getElementsByTagName("SupplyDetails");
                                                //int supplyLen = getInventorySnapShotSupply.getLength();
                                                Element eleInventorySnapShotSupply = null;
                                                                for(k=0;k<getInventorySnapShotSupply.getLength();k++) 
                                                                {
                                                                                eleInventorySnapShotSupply = (Element) getInventorySnapShotSupply.item(k);
                                                                                String strLotNo = eleInventorySnapShotSupply.getAttribute(AcademyConstants.ATTR_LOT_NUMBER);
                                                                                String strSegNo = eleInventorySnapShotSupply.getAttribute(AcademyConstants.ATTR_SEGMENT);
                                                                                log.verbose("************LotNumber*************" +strLotNo);
                                                                                log.verbose("************Segment*************" +strSegNo);
                                                                                
                                                                                
                                                                                if((!(YFCObject.isVoid(strSegNo))) || (!(YFCObject.isVoid(strLotNo))))
                                                                                {
                                                                                                //getInventorySnapShotSupply=(NodeList) eleInventorySnapShotItem.removeChild((eleInventorySnapShotSupply));
                                                                                                log.verbose("************RemovingChildWithLotNoAttribute*************");
                                                                                                eleInventorySnapShotItem.removeChild((eleInventorySnapShotSupply));
                                                                                                log.verbose("************SuccessOnRemovingChildWithLotNoAttribute*************");
                                                                                                k--;
                                                                                                //log.verbose("output: "   + XMLUtil.getXMLString(inDoc));
                                                                                }
                                                                                
                                                                                
                                                                }
                                                                
                                                                
                                                
                                                
                                }
                                log.verbose("output: "   + XMLUtil.getXMLString(inDoc));
                                
                                return inDoc;
                                
                                
                                
                                
 
}
                
                
                }