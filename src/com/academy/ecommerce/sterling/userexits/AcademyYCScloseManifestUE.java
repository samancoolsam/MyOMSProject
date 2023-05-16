package com.academy.ecommerce.sterling.userexits;

import java.util.Properties;
import java.util.StringTokenizer;

import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.ycs.japi.ue.YCScloseManifestUserExit;
import com.yantra.shared.ycp.YFSContext;
import com.yantra.yfc.dom.YFCDocument;


/**
 * @author nkannapan
 *
 */
public class AcademyYCScloseManifestUE implements YCScloseManifestUserExit {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyYCScloseManifestUE.class);
	
	private Properties props;
	public void setProperties(Properties props) {
        this.props = props;
    }
	public boolean closeManifestContinue(YFSContext env, String s)throws YFSUserExitException {
		// TODO Auto-generated method stub
		
		boolean isCloseManifestRqd = true;
		try{
			log.beginTimer(" Begining of AcademyYCScloseManifestUE -> closeManifestContinue Api");
			log.verbose("##Inside closeManifestContinue method in YCSCloseManifestUE##");
			YFCDocument inXML = YFCDocument.parse(s);			
			String sCarrier = inXML.getDocumentElement().getAttribute("Carrier");	
			
			/*
			String sListCarriers= props.getProperty("Carrier");
			System.out.println("Carrier: "+ sCarrier);
			StringTokenizer st = new StringTokenizer(sListCarriers,",");
			while (st.hasMoreTokens()){
				if(st.nextToken().equals(sCarrier)){
					isCloseManifestRqd = false;
					break;
				}
			} */	
			
			if(sCarrier.equals("USPS-Endicia") || sCarrier.equals("USPS-Letter"))
			{
				isCloseManifestRqd = false;
			}
			log.endTimer(" End of AcademyYCScloseManifestUE -> closeManifestContinue Api");
		}catch(Exception e){
			e.printStackTrace();
			throw new YFSUserExitException(e.getMessage());
		}
		return isCloseManifestRqd;
	}

	public String closeManifest(YFSContext env, String str) throws YFSUserExitException {
		//return the inputXML as outputXML
		return str;						
	}

}
