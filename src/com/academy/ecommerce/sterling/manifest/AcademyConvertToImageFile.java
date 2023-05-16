package com.academy.ecommerce.sterling.manifest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.woodstock.util.Base64Coder;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyConvertToImageFile implements YIFCustomApi {
	private Properties props;
	
	
	
	public void setProperties(Properties props) {
		this.props = props;
	}
	
	
	
	
	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyConvertToImageFile.class);
	
	
	
	public Document convertToImageFile(YFSEnvironment env, Document inDoc) throws YFCException {
		if (!YFCObject.isVoid(inDoc)) {
			try {
				String sfolderLoc = props.getProperty("Folder_Location");
				String strisElseIfCheckRequired = props.getProperty("IS_ELSEIF_CHECK_REQUIRED");
				log.verbose("Folder Location is ::" + sfolderLoc);
				log.debug("Extra ElseIf Check Required ::" + strisElseIfCheckRequired);
				
				Element inDocElem = inDoc.getDocumentElement();
				String sContainerNo = inDocElem.getAttribute("ContainerNo");
				log.verbose("Container #########" + sContainerNo);
				String sShipmentContainerKey = inDocElem.getAttribute("ShipmentContainerKey");
				String sPrintBuffer = inDocElem.getAttribute("PrintBuffer");
				//PROD ISSUE: Reprint Label Fix : Begin
				String strFinishPackCheck = (String) env.getTxnObject(AcademyConstants.TXN_OBJ_FINISH_PACK);
				if(!YFCCommon.equalsIgnoreCase("Y",strFinishPackCheck))
				{
					log.debug("-------RCP FLOW and Web Store Get_Tracking_No Flow-----: strFinishPackTxnObj Check"+strFinishPackCheck);
					log.beginTimer("strFinishPackCheck::"+strFinishPackCheck + "sContainerNo::" +sContainerNo);
					InputStream is = new ByteArrayInputStream(sPrintBuffer.getBytes());
					// Changed for CR#38
					// FileOutputStream os = new FileOutputStream(new File(sfolderLoc +
					// sContainerNo + ".png"));
					FileOutputStream os = new FileOutputStream(new File(sfolderLoc + sContainerNo));
					log.verbose("Folder Creation is Successful");
					// FileOutputStream os = new FileOutputStream(new
					// File(sContainerNo));
					Base64Coder.base64Decode(is, os);
					log.verbose("Base64 Encoding..");
					os.close();
				}
				else if(YFCCommon.equalsIgnoreCase("Y",strFinishPackCheck) && !YFCCommon.isVoid(sPrintBuffer))
				{	
					log.debug("-------WebStore Exception Flow-----: strFinishPackTxnObj Check"+strFinishPackCheck);
					log.beginTimer("strFinishPackCheck::"+strFinishPackCheck + "sContainerNo::" +sContainerNo);
					InputStream is = new ByteArrayInputStream(sPrintBuffer.getBytes());
					// Changed for CR#38
					// FileOutputStream os = new FileOutputStream(new File(sfolderLoc +
					// sContainerNo + ".png"));
					FileOutputStream os = new FileOutputStream(new File(sfolderLoc + sContainerNo));
					log.verbose("Folder Creation is Successful");
					// FileOutputStream os = new FileOutputStream(new
					// File(sContainerNo));
					Base64Coder.base64Decode(is, os);
					log.verbose("Base64 Encoding..");
					os.close();
				}
				//PROD ISSUE: Reprint Label Fix : End
				//EFP-8 Create Return Labels at Pack Station :: START
				//Element returnPrintBufferEle = XMLUtil.getFirstElementByName(inDocElem,"ContainerReturnTracking");
				Element returnPrintBufferEle = XMLUtil.getFirstElementByName(inDocElem,"ContainerReturnTrackingList");
				log.verbose("ReturnPrintBufferEle...");
				InputStream is_return = null;
				FileOutputStream os_return = null;
				boolean printReturnLabel = false;
				if(returnPrintBufferEle!=null)
				{
					Element returnPrintImageEle = XMLUtil.getFirstElementByName(returnPrintBufferEle,"ContainerReturnTracking");
					if(returnPrintImageEle!=null)
					{
						String returnPrintBuffer = returnPrintImageEle.getAttribute("ReturnPrintBuffer");
						log.verbose("returnPrintBuffer is :" + returnPrintBuffer);
						
						if (!YFCObject.isVoid(returnPrintBuffer)) {
						
						printReturnLabel = true;
						is_return = new ByteArrayInputStream(returnPrintBuffer.getBytes());
						os_return = new FileOutputStream(new File(sfolderLoc + "ReturnLabel"+sContainerNo));
						log.verbose("Return file creation is Successful");
						Base64Coder.base64Decode(is_return, os_return);
						log.verbose("Base64 Encoding..");
						os_return.close();
					}
				}
			}
				
				//EFP-8 Create Return Labels at Pack Station :: END
				
				String carrierServiceCode = inDoc.getDocumentElement().getAttribute("SCAC");
				
				log.verbose("#### Carrier Service Code #### :" + carrierServiceCode);
				
				if (carrierServiceCode != null
						&& (carrierServiceCode.startsWith("USPS") || carrierServiceCode.startsWith("FEDX") || carrierServiceCode
								.startsWith("SmartPost"))) {
					
					try {
						
						//EFP-8 Create Return Labels at Pack Station :: START
						log.verbose("**** printReturnLabel flag is****" + printReturnLabel);
						if(printReturnLabel)
						{
							log.verbose("**** Folder Location is: " + "/usr/bin/bash " + sfolderLoc + "process.sh " + sfolderLoc + "ReturnLabel"+sContainerNo + " -90");
							Process pr = Runtime.getRuntime().exec("/usr/bin/bash " + sfolderLoc + "process.sh " + sfolderLoc + "ReturnLabel"+sContainerNo + " -90");
							BufferedReader stdInput_R = new BufferedReader(new InputStreamReader(pr.getInputStream()));
							BufferedReader stdError_R = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
							String sr = null;
						//read the output from the command
							log.verbose("Here is the standard output of the command:\n");
							while ((sr = stdInput_R.readLine()) != null) {
								log.verbose(sr);
							}
							
							
							// read any errors from the attempted command
							log.verbose("Here is the standard error of the command (if any):\n");
							while ((sr = stdError_R.readLine()) != null) {
								log.verbose(sr);
							}
							
						}
						//EFP-8 Create Return Labels at Pack Station :: END

						// using the Runtime exec method:
						if(!YFCCommon.equalsIgnoreCase("Y",strFinishPackCheck))
						{
							log.debug("**** RCP and WebStore Get_tracking_no Flow*****Folder Location is: " + "/usr/bin/bash " + sfolderLoc + "process.sh " + sfolderLoc + sContainerNo + " -90");
							log.beginTimer("strFinishPackCheck::"+strFinishPackCheck + "sContainerNo::" +sContainerNo);
							Process p = Runtime.getRuntime().exec("/usr/bin/bash " + sfolderLoc + "process.sh " + sfolderLoc + sContainerNo + " -90");
							String s = null;

							BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

							BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));


							// read the output from the command
							log.verbose("Here is the standard output of the command:\n");
							while ((s = stdInput.readLine()) != null) {
								log.verbose(s);
							}


							// read any errors from the attempted command
							log.verbose("Here is the standard error of the command (if any):\n");
							while ((s = stdError.readLine()) != null) {
								log.verbose(s);
							}
						}
						//SFS-47 Shipping Label Rotation- Start
						else if(YFCCommon.equalsIgnoreCase("Y",strisElseIfCheckRequired))
						{
							if(YFCCommon.equalsIgnoreCase("Y",strFinishPackCheck) && !YFCCommon.isVoid(sPrintBuffer)){
							log.debug(" ****Web Store Exception Flow**** Folder Location is: " + "/usr/bin/bash " + sfolderLoc + "process.sh " + sfolderLoc + sContainerNo + " -90");
							log.beginTimer(" strFinishPackCheck::"+strFinishPackCheck + "sContainerNo::" +sContainerNo);
							Process p = Runtime.getRuntime().exec("/usr/bin/bash " + sfolderLoc + "process.sh " + sfolderLoc + sContainerNo + " -90");
							String s = null;

							BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

							BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));


							// read the output from the command
							log.verbose("Here is the standard output of the command:\n");
							while ((s = stdInput.readLine()) != null) {
								log.verbose(s);
							}


							// read any errors from the attempted command
							log.verbose("Here is the standard error of the command (if any):\n");
							while ((s = stdError.readLine()) != null) {
								log.verbose(s);
							}
						}
						}
						//SFS-47 Shipping Label Rotation- End
					} catch (Exception e) {
						log.verbose("exception happened - here's what I know: ");
						e.printStackTrace();
						throw new YFCException(e);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				log.verbose(ex);
				throw new YFCException(ex);
			}
			
		}
		return inDoc;
	}
}
