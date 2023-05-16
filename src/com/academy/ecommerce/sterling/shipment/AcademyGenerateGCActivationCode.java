package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;
import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.core.YCPContext;

// This Custom Api implemented for - to generate the GCActivation Code which contains AlphaNumerice of 6digits

public class AcademyGenerateGCActivationCode implements YIFCustomApi {
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyGenerateGCActivationCode.class);
	private static Random rn = new Random();

	public Document generateGCActivationCode(YFSEnvironment env, Document inXML)
			throws Exception {
		log.beginTimer(" Begining of AcademyGenerateGCActivationCode ->generateGCActivationCode Api");
		int seqNo=(int)((YCPContext) env).getNextDBSeqNo("ACT_SEQ");
		String activationSequenceNo=Integer.toString(seqNo);
		if(activationSequenceNo.length()<3)
		{ 
	        if(activationSequenceNo.length()==2)
	        	activationSequenceNo="0"+activationSequenceNo;
	        else activationSequenceNo="00"+activationSequenceNo;
			
		}
		String gcActivationCode=randomstring(3,3)+activationSequenceNo;
		Document ordDoc=XMLUtil.createDocument("Order");
		ordDoc.getDocumentElement().setAttribute("GCActivationCode", gcActivationCode);
		log.endTimer(" End of AcademyGenerateGCActivationCode -> generateGCActivationCode Api");
				return ordDoc;
}

	
	public static int rand(int lo, int hi)
	    {  
	            int n = hi - lo + 1;
	            int i = rn.nextInt() % n;
	            if (i < 0)
	                    i = -i;
	       
	            return lo + i;
	    }
		public static String randomstring(int lo, int hi)
	    {  
	            int n = rand(lo, hi);
	            byte b[] = new byte[n];
	            for (int i = 0; i < n; i++)
	                    b[i] = (byte)rand('a', 'z');
	       
	            return new String(b);
	    }
	    /*public static String randomstring()
	    {
	            return randomstring(3, 3);
	    }*/
	
}
