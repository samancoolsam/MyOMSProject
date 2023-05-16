package com.academy.ecommerce.sterling.shipment;
public class AcademyShipmentConatinerList
{
	 public static void main(String[] args)
	{
	}
	
}
/*package com.academy.ecommerce.sterling;

import java.util.Properties;

import org.dom4j.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyShipmentConatinerList implements YIFCustomApi {
	public void setProperties(Properties props) {} 
	
	
	  /**
 * Instance of logger
 */
/*
private static YFCLogCategory log = YFCLogCategory.instance(AcademyShipmentContainerList.class);
private Document outXMLfromservice = null;

public void ShipmentDetailsOutput(YFSEnvironment env, Document inDoc) throws Exception {
	log.beginTimer(" Begining of AcademyShipmentContainerList-> AcademyShipmentConatinerListApi");
	  log.verbose("Input document AcademyShipmentContainerList: "+XMLUtil.getXMLString(inDoc));
	  if(!YFCObject.isVoid(inDoc)){
		  Element StrShipment = inDoc.getDocumentElement();
		  String strShipmentKey = strShipment.getAttribute("ShipmentKey");
		  log.verbose("ShipmentKey");
		  if(!StrShipmentKey.equals(null))
		  {
			  log.verbose("ShipmentKey");
			  log.verbose("Before calling GetShipmentConatinerListAPI");
			  GetShipmentConatinerListAPI(env, strShipmentKey);
			  log.verbose("After calling GetShipmentConatinerListAPI");
		  
	  }
}
	  public void GetShipmentConatinerListAPI(YFSEnvironment env,String strShipmentKey) throws Exception{
		  try{
			  Document inXMLGetSCDtls = XMLUtil.createDocument("Container");
		        Element eleGetSCDtls = inXMLGetSCDtls.getDocumentElement();
		        eleGetSCDtls.setAttribute("ShipmentKey", strShipmentKey);
		        log.verbose("Input to GetShipmentContainerList API: "+XMLUtil.getXMLString(inXMLGetSCDtls));
		        Document tempGetSCDtls = YFCDocument.parse("<Containers> " +
						"<Container ContainerNo=\"\" ShipmentContainerKey=\"\" TrackingNo=\"\"  > " +
						"</Containers>").getDocument();
				env.setApiTemplate("getShipmentContainerList", tempGetSCDtls);
				
				Document outXMLGetSCDtls = AcademyUtil.invokeAPI(env,"getShipmentContainerList",inXMLGetSCDtls);
				env.clearApiTemplates();
				log.verbose("Output of getSCDetails API: "+XMLUtil.getXMLString(outXMLGetSCDtls));
				
				Element containers=outXMLGetSCDtls.getDocumentElement();
		    	// System.out.println(""+XMLUtil.getElementXMLString(containers));
		    	NodeList conNodeList = containers.getElementsByTagName("Container");
		    	//System.out.println(""+conNodeList.getLength());
		         //System.out.println(conNodeList.toString());
		        for(int i =0; i<conNodeList.getLength();i++)
		           {
		        	   	Element container=(Element) conNodeList.item(i);
		        	   	String strShipContKey=container.getAttribute("ShipmentContainerKey");
		        	   	String strTrackingNo=container.getAttribute("TrackingNo");
		        	  // 	System.out.println(""+strShipContKey+" "+strTrackingNo);
		               // Element taskElement = (Integer) taskNodeList.getLength();
		                if(!strTrackingNo.equals(null))
		                {
		                	Document inXMLforAPBPS = XMLUtil.createDocument("Container");
		    		        Element elein = inXMLforAPBPS.getDocumentElement();
		    		        elein.setAttribute("ShipmentContainerKey", strShipContKey);
		    		        //log.verbose("Input to GetShipmentContainerList API: "+XMLUtil.getXMLString(inXMLforAPBPS));
		    		        
		                  outXMLfromservice=AcademyUtil.invokeService(env, AcademyPrintBulkPackSlip, inXMLforAPBPS);
		                }
		           }
				
				
				
				
	  }
		           
			  
		  }
		  }catch(Exception e)
		  {	  
			  log.verbose("Error captured in ShipmentDetailsOutput method");
		  	  throw e;
		  }
		  }
	return outXMLfromservice;
}
*/