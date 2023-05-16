package com.academy.ecommerce.sterling.manifest;

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.constants.AcademyConstants;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyStampPackLocation {

	private Properties props;
	
	public void setProperties(Properties props) {
		this.props = props;
	}

	public Document stampPackLocation(YFSEnvironment yfsenv, Document inDoc){
		
		Element containerEle = inDoc.getDocumentElement();
		String sLocation = containerEle.getAttribute("ContainerLocation");
		String packStationId = "";
		if(AcademyConstants.STR_STO_PACKLOC_BULK.equals(sLocation))
		{
			packStationId=props.getProperty("PACK-BULK");
		}
		else if (AcademyConstants.STR_STO_PACKLOC_MEZZ.equals(sLocation))
		{
			packStationId=props.getProperty("PACK-MEZZ");
		}
		else
		{
			packStationId = props.getProperty("PACK_STATION");
		}
		containerEle.setAttribute("StationId", packStationId);
		return inDoc;
		
	}

}
