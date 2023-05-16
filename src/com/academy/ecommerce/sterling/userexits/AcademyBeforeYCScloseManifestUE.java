package com.academy.ecommerce.sterling.userexits;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.YFSUserExitException;

import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.ydm.integration.ycs.japi.ue.YCSBeforeCloseManifestUE;
import com.academy.util.xml.XMLUtil;


/**
 * @author mchattaraj/naveenk
 *
 */
public class AcademyBeforeYCScloseManifestUE implements YCSBeforeCloseManifestUE {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyBeforeYCScloseManifestUE.class);
	
	private Properties props;
	public void setProperties(Properties props) {
        this.props = props;
    }
	public Document beforeYCScloseManifestAPI(YFSEnvironment env, org.w3c.dom.Document inXML) throws YFSUserExitException {
		// TODO Auto-generated method stub
		YFSException ye = new YFSException();
		try{
			log.verbose("##Inside beforeCloseManifestContinue method in beforeYCSCloseManifestUE##");
			
				String strShipnode = inXML.getDocumentElement().getAttribute("ShipNode");
				String strManifestNo = inXML.getDocumentElement().getAttribute("ManifestNumber");
				if(!YFCObject.isVoid(strManifestNo)){
					
					YFCDocument docGetManifestList = YFCDocument.createDocument("Manifest");
					docGetManifestList.getDocumentElement().setAttribute("ManifestNo", strManifestNo);
					docGetManifestList.getDocumentElement().setAttribute("ShipNode", strShipnode);
					
					Document docOutputGetManifestList = AcademyUtil.invokeAPI(env, "getManifestList", docGetManifestList.getDocument());
					log.verbose("Output of getManifestList :"+ XMLUtil.getXMLString(docOutputGetManifestList));
					
					Element eleManifests = docOutputGetManifestList.getDocumentElement();
					Element eleManifest = (Element)eleManifests.getElementsByTagName("Manifest").item(0);
					
					String actualManifestDate = eleManifest.getAttribute("ManifestDate");	
					
					String strDateFormat = AcademyConstants.STR_SIMPLE_DATE_PATTERN;
					SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
					
					Calendar cal = Calendar.getInstance();
					String sCurrentDate = sDateFormat.format(cal.getTime());
					//String sActualManifestDate = sDateFormat.format(actualManifestDate);
					
					log.verbose("sCurrentDate :"+ sCurrentDate);
					log.verbose("sActualManifestDate :"+ actualManifestDate);
					
					Date dCurrentDate = sDateFormat.parse(sCurrentDate);
					log.verbose("Got current date object");
					Date dActualManifestDate = sDateFormat.parse(actualManifestDate);
					log.verbose("Got manifest date object");
					
					int comManifestDate = dActualManifestDate.compareTo(dCurrentDate);
					
					if(comManifestDate > 0)
					{						
						ye.setErrorDescription("Close manifest can only be performed for current or previous dates");
						ye.setErrorCode("EXTN0011");
						throw ye;
					}
					
					//Start EFP-12 USPS To Stores
					String strCarrier = inXML.getDocumentElement().getAttribute("Carrier");
					if(!YFCObject.isVoid(strCarrier) && (strCarrier.equalsIgnoreCase(AcademyConstants.STR_USPS_SCAC)
							|| strCarrier.equalsIgnoreCase(AcademyConstants.STR_USPS_ENDICIA))){
						env.setTxnObject(AcademyConstants.STR_IS_USPS_MANIFEST, AcademyConstants.STR_YES);
						log.verbose("##Setting Environment Object IsUSPSManifest to 'Y' in beforeYCSCloseManifestUE##");
					}else{
						env.setTxnObject(AcademyConstants.STR_IS_USPS_MANIFEST, AcademyConstants.STR_NO);
						log.verbose("##Setting Environment Object IsUSPSManifest to 'N' in beforeYCSCloseManifestUE##");
					}
					//End EFP-12 USPS To Stores
				}
			
		}catch(Exception e){
			if (!StringUtil.isEmpty(ye.getErrorCode())) {
				throw ye;
			} else {
				YFSUserExitException ex = new YFSUserExitException();
				throw ex;
			}
		}
		log.verbose("##End of beforeCloseManifestContinue method in beforeYCSCloseManifestUE##");
		return inXML;
	}
}
