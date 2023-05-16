package com.academy.ecommerce.sterling.sourcing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class CreateOrder {
	
	private static Properties props;
	private static YFCLogCategory log = YFCLogCategory.instance(CreateOrder.class);

		public void setProperties(Properties props) throws Exception {
			this.props = props;
		}
	
	public static void processSourcingOptimizer(YFSEnvironment env,Document inXML) throws Exception  {
	        BufferedReader br = null;
	        String line = "";
			String csv=props.getProperty("CSV_PATH");
			String xml=props.getProperty("XML_PATH");
			
	        try {

	            br = new BufferedReader(new FileReader(csv));
	            ArrayList <String> alfinaldata=new ArrayList<String>();
	            HashSet<String> hs =new HashSet<String>();
	            while ((line = br.readLine()) != null) {
	            	ArrayList <String> aldata=new ArrayList<String>();
	               String[] s = line.split(",");
	               hs.add(s[0]);
	               for(int z=0;z<s.length;z++)
	               {
	            	   aldata.add(s[z]);
	               }
	               alfinaldata.addAll(aldata);
	               
	               log.verbose("Successfully fethced the details from csv");
	            }
	            for(String k:hs)
	            {
	            	log.verbose("Preparing Create Order xml from CSV");
	            	ArrayList <String> alNodedata=new ArrayList<String>();
	            	for(int j =0;j<alfinaldata.size();j=j+9)
	            	{
	            		if(k.equals(alfinaldata.get(j)))
	            		{	
	            			alNodedata.add(alfinaldata.get(j));
	            			alNodedata.add(alfinaldata.get(j+1));
	            			alNodedata.add(alfinaldata.get(j+2));
	            			alNodedata.add(alfinaldata.get(j+3));
	            			alNodedata.add(alfinaldata.get(j+4));
	            			alNodedata.add(alfinaldata.get(j+5));
	            			alNodedata.add(alfinaldata.get(j+6));
	            			alNodedata.add(alfinaldata.get(j+7));
	            			alNodedata.add(alfinaldata.get(j+8));
	            			
	            		}            	
	            	}
	            			

					/*File fXmlFile = new File(xml);
	    DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    Document doc = dBuilder.parse(fXmlFile);
	    */
	    
	    
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
		DocumentBuilder builder = dbFactory.newDocumentBuilder();
		log.verbose("*** builder.parse() filePath *** " + xml);
		log.verbose("*** InputStream is *** " + CreateOrder.class.getResourceAsStream(xml));
		Document doc = builder.parse(CreateOrder.class.getResourceAsStream(xml));
		log.verbose("*** builder.parse() *** " + XMLUtil.getXMLString(doc));
	    
		 SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		 Date dOrder_date=sdf.parse(alNodedata.get(1)); 
		 Date date = new Date();
		 String syear=new SimpleDateFormat("yyyy").format(date);
		 String smonth=new SimpleDateFormat("MM").format(date);
		 String sdate=new SimpleDateFormat("dd").format(date);
		 String shour=new SimpleDateFormat("HH").format(date);
		 String sminute=new SimpleDateFormat("mm").format(date);
		 String ssecond=new SimpleDateFormat("ss").format(date);
		 System.out.println(syear+smonth+sdate+shour+sminute+ssecond);
		 String DayPhone="7138"+shour+sminute+ssecond ;	   
	     String EMailID="mo"+shour+sminute+ssecond+"@academy.com";	    
		 String OrderNo=alNodedata.get(0)+syear+smonth+sdate+shour+sminute;
		 
		 
		 
	    Element eOrder= (Element) doc.getElementsByTagName("Order").item(0);  
	    eOrder.setAttribute("OrderNo", OrderNo);
	    
	    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:00");
	    String sOrder_date=sdf1.format(dOrder_date);
	  
	    eOrder.setAttribute("OrderDate", sOrder_date);
	    
	    Element ePersonInfoBillTo= (Element) doc.getElementsByTagName("PersonInfoBillTo").item(0);
	    ePersonInfoBillTo.setAttribute("ZipCode", alNodedata.get(7));
	    
	    Document docOut=XMLUtil.createDocument("Order");
	    Element eledocOut=docOut.getDocumentElement();
	    
	    XMLUtil.setAttributes(eOrder, eledocOut);
	    
	    XMLUtil.importElement(eledocOut, ePersonInfoBillTo);
	    
	    Element eleOrdLines=docOut.createElement("OrderLines");
	    
	   for(int i=0;i<alNodedata.size();i=i+9)
	    {
	    	
	    Element eOrderLine =  (Element) doc.getElementsByTagName("OrderLine").item(0);
	    eOrderLine.setAttribute("OrderedQty", alNodedata.get(i+4));
	    eOrderLine.setAttribute("PrimeLineNo", alNodedata.get(i+8));
	    eOrderLine.setAttribute("FulfillmentType", alNodedata.get(i+5));
	    Element eExtn = (Element) doc.getElementsByTagName("Extn").item(0);
	    eOrderLine.appendChild(eExtn);
	    
	    Element eItem = (Element) doc.getElementsByTagName("Item").item(0);
	    eItem.setAttribute("ItemID",alNodedata.get(i+3));
	    eOrderLine.appendChild(eItem);
	    
	    Element eLinePriceInfo = (Element) doc.getElementsByTagName("LinePriceInfo").item(0);
	    eOrderLine.appendChild(eLinePriceInfo);
	    
	    Element eLineCharges = (Element) doc.getElementsByTagName("LineCharges").item(0);
	    eOrderLine.appendChild(eLineCharges);
	    
	    Element eLineTaxes = (Element) doc.getElementsByTagName("LineTaxes").item(0);
	    eOrderLine.appendChild(eLineTaxes);
	    
	    Element ePersonInfoShipTo = (Element) doc.getElementsByTagName("PersonInfoShipTo").item(0);
		ePersonInfoShipTo.setAttribute("DayPhone",DayPhone);
		ePersonInfoShipTo.setAttribute("EMailID",EMailID);
	    ePersonInfoShipTo.setAttribute("ZipCode",alNodedata.get(i+7));
	    eOrderLine.appendChild(ePersonInfoShipTo);
	    
	    XMLUtil.importElement(eleOrdLines, eOrderLine);
	    
	    
	    }
	   Element ePaymentMethods= (Element) doc.getElementsByTagName("PaymentMethods").item(0);
	  
	   XMLUtil.importElement(eledocOut, ePaymentMethods);
	   eledocOut.appendChild(eleOrdLines);
	   log.verbose("Input of Create Order:"+XMLUtil.getXMLString(docOut));
	   AcademyUtil.invokeService(env, "AcademyOrderDumpDataTestDb",docOut);
	   AcademyUtil.invokeService(env, "AcademyOrderDataLoading",docOut);
		
					
	            }
	           } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (br != null) {
	                try {
	                    br.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }

	    }
}

	
	