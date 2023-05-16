package com.academy.ecommerce.sterling.manifest;

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyStampPrinterId {
	private static YFCLogCategory log = YFCLogCategory
	.instance(AcademyStampPrinterId.class);
	private Properties props;
	
	public void setProperties(Properties props) {
		this.props = props;
	}

	public Document stampPrinterId(YFSEnvironment env, Document inDoc){
		Element containerEle = inDoc.getDocumentElement();
		String sImageLocation = props.getProperty("Image_Location");
		String sContainerNo = containerEle.getAttribute("ContainerNo");
		containerEle.setAttribute("CarrierLabel", sImageLocation + sContainerNo);
		String packStationId = props.getProperty("PRINTER_ID");
		containerEle.setAttribute("PrinterId", packStationId);
		return inDoc;
		
	}
	
	public Document stampRePrinterId(YFSEnvironment env, Document inDoc){
		String isReprintFlow = "N";
		Element containerEle = inDoc.getDocumentElement();
		String sImageLocation = props.getProperty("Image_Location");
		String sContainerNo = containerEle.getAttribute("ContainerNo");
		containerEle.setAttribute("CarrierLabel", sImageLocation + sContainerNo);
		if(env.getTxnObject("ReprintPackSlip")!=null)
			isReprintFlow = (String)env.getTxnObject("ReprintPackSlip");
			log.verbose("*** isReprintFlow*****" +isReprintFlow);
		if(isReprintFlow.equals("Y")){
			log.verbose("***This is reprint flow for container label*****");
			if(env.getTxnObject("ReprintPrinterId")!=null){
			String strPrinterId = (String)env.getTxnObject("ReprintPrinterId");
			log.verbose("**** Reprinter Id in the environment is*****"+strPrinterId);
			containerEle.setAttribute("PrinterId", strPrinterId);
			}
		}
		return inDoc;
		
	}
	
	//	EFP-8 Create Return Labels at Pack Station :: START
	public Document stampPrinterIdForReturnLabel(YFSEnvironment env, Document inDoc){
		Element containerEle = inDoc.getDocumentElement();
		String sImageLocation = props.getProperty("Image_Location");
		String sContainerNo = containerEle.getAttribute("ContainerNo");
		containerEle.setAttribute("CarrierLabel", sImageLocation + "ReturnLabel"+sContainerNo);
		return inDoc;
		
	}
	
	//EFP-8 Create Return Labels at Pack Station :: END
}
