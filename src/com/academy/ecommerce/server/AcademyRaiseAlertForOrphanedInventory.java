package com.academy.ecommerce.server;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.shipment.AcademyProcessBackroomPick;
import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyRaiseAlertForOrphanedInventory extends YCPBaseAgent {
    
    private static final YFCLogCategory log     = YFCLogCategory.instance(AcademyRaiseAlertForOrphanedInventory.class);
    
    public List getJobs(YFSEnvironment env, Document inXML,Document lastMessage)throws Exception{
        
        
        log.verbose("Class:AcademyRaiseAlertForOrphanedInventory ::: method:getJobs : entry");
        List OrphanedLocationInvList = new ArrayList();
        //Prepare Input Doc for getNodeInventory API
        Document getNodeInventoryInDoc = prepareInputDocForAPI();
        //Call  getNodeInventory API
        Document getNodeInventoryOutDoc = AcademyUtil.invokeAPI(env, "getNodeInventory", getNodeInventoryInDoc);
        Element rootEle =  getNodeInventoryOutDoc.getDocumentElement();
        Element lPNListEle = (Element) rootEle.getElementsByTagName("LPNList").item(0);
        lPNListEle.getParentNode().removeChild(lPNListEle);
        //Check if lastMessage is null
        if(lastMessage==null){
            // if yes, add the response document into the array list
            OrphanedLocationInvList.add(getNodeInventoryOutDoc);
        }
        
        log.verbose("Class:AcademyRaiseAlertForOrphanedInventory ::: method:getJobs : exit with returned Document : \n" + XMLUtil.getXMLString(getNodeInventoryOutDoc) );
        return OrphanedLocationInvList;
        
    }
    
    /**
     * This method prepares and returs the input Doc
     * to the getNodeInventory API
     * 
     * @return
     * @throws Exception
     */
    private Document prepareInputDocForAPI() throws Exception {
        /*Input to getNodeInventory API
        * 
        *   <NodeInventory Node="005" ZoneId="EQM">    
            </NodeInventory>
        *
        */
        Document getNodeInventoryInputDoc = XMLUtil.createDocument("NodeInventory");
        Element nodeInventoryElement = getNodeInventoryInputDoc.getDocumentElement();
        nodeInventoryElement.setAttribute("Node", "005");
        nodeInventoryElement.setAttribute("ZoneId", "EQM");
        
        return getNodeInventoryInputDoc;
    }

    public void executeJob(YFSEnvironment env, Document getNodeInventoryOutDoc) throws Exception {
        // TODO Auto-generated method stub
        
        log.verbose("Class:AcademyRaiseAlertForOrphanedInventory ::: method:executeJob : entry");
        if(getNodeInventoryOutDoc!=null)
        {
            log.verbose("Input to executeJob is : \n" + XMLUtil.getXMLString(getNodeInventoryOutDoc));
            NodeList locationInventorylist = getNodeInventoryOutDoc.getElementsByTagName("LocationInventory");
            Element locationInventoryElement = null;
            for (int count = 0; count < locationInventorylist.getLength(); count++) {
                locationInventoryElement = (Element) locationInventorylist.item(count);
                Double pendOutQtyStr = Double.parseDouble(locationInventoryElement.getAttribute("PendOutQty"));
                Double quantityStr = Double.parseDouble(locationInventoryElement.getAttribute("Quantity"));
                String locationIdStr = locationInventoryElement.getAttribute("LocationId");
                //Checking if the LocationInventory element has PendOutQty>0, Quantity=0 and LocationId starts with MSCLOC                
                if(!( pendOutQtyStr > 0 && quantityStr == 0 && locationIdStr.startsWith("MSCLOC"))){
                    //removing the unmatched elements from the LocationInventory List
                    locationInventoryElement.getParentNode().removeChild(locationInventoryElement);
                    count--;   
                }
               
            }
            log.verbose("getNodeInventoryOutDoc only with orphaned Inventory records \n" + XMLUtil.getXMLString(getNodeInventoryOutDoc) );
            //Calling the AcademySendEmailWhenOrphandInvFound service which will send email alert
            AcademyUtil.invokeService(env, "AcademySendEmailWhenOrphandInvFound", getNodeInventoryOutDoc);
            
        }
        log.verbose("Class:AcademyRaiseAlertForOrphanedInventory ::: method:executeJob  : exit");
    }

}
