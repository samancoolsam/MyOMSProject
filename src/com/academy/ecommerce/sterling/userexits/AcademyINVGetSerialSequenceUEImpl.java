package com.academy.ecommerce.sterling.userexits;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.INVGetSerialSequenceUE;
/**
 * 
 * @author stummala
 *
 *This UE generates a range of serial numbers with 12 digit in a sequence while packing from Pack Station.
 * Product invokes this UE 
 *  - from pack station UI ; to validate Qty based on the range scanned by packer
 *  - and addToContainer transaction process ; to generate actual serial number on the range.
 *  
 */
public class AcademyINVGetSerialSequenceUEImpl implements
		INVGetSerialSequenceUE {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyINVGetSerialSequenceUEImpl.class);
	/**
	 * @return Document with
	 * 		<SerialList>
	 * 			<Serial SerialNo="" />
	 * 		</SerialList>
	 * @throws YFSUserExitException
	 */
	public Document generateSerialSequence(YFSEnvironment env, Document inDoc)
			throws YFSUserExitException {
		log.verbose("Begining of AcademyINVGetSerialSequenceUEImpl -> generateSerialSequence() API ");
		log.verbose(" Input to AcademyINVGetSerialSequenceUEImpl is : "+XMLUtil.getXMLString(inDoc));
		if(YFCObject.isVoid(inDoc))
			return inDoc;
		Document outDocument  =  null;
		try{
			Element docElement = inDoc.getDocumentElement();
			outDocument  = XMLUtil.createDocument(AcademyConstants.ELE_SERIAL_LIST);
			// check whether UE invoked from pack station or addToContainer process.
			if(docElement.getTagName().equals("Container")){
				Element distSerialListEle = outDocument.getDocumentElement();				
				generateSerialNoInRange(env, docElement, distSerialListEle);				
			}else{
				// Add a logic to validate if Item is Gift Card or not
				/*boolean isGCItem = checkForGCItem(env,docElement.getAttribute(AcademyConstants.ATTR_ITEM_ID));
				if(!isGCItem){
					YFSUserExitException ueExce = new YFSUserExitException();
					ueExce.setErrorCode(AcademyConstants.ERR_CODE_07);
					throw ueExce;
				}*/
				NodeList serialElemlist = docElement.getElementsByTagName(AcademyConstants.ELE_SERIAL_ELEM);
				log.verbose(" Serial Elem : "+serialElemlist.getLength());
				if(serialElemlist.getLength() > 0){	
					Element serialListEle = outDocument.getDocumentElement();
					for(int indx=0; indx<serialElemlist.getLength(); indx++){
						Element serialElem = (Element)serialElemlist.item(indx);
						String fromSerial = serialElem.getAttribute(AcademyConstants.ATTR_FROM_SERIAL_NO);
						String toSerial = serialElem.getAttribute(AcademyConstants.ATTR_TO_SERIAL_NO);
						log.verbose("fromSerial = "+fromSerial+" toSerial = "+toSerial);
						processSerialList(env,fromSerial, toSerial, serialListEle, inDoc);				
					}
				}
			}
		}catch(Exception ex){
			if (ex instanceof YFSUserExitException) {
				throw (YFSUserExitException)ex;
			}
			YFSUserExitException ueExce = new YFSUserExitException(ex.getMessage());
			throw ueExce;
		}
		if(outDocument != null && !YFCObject.isVoid(outDocument))
			log.verbose("output xml is : "+XMLUtil.getXMLString(outDocument));
		log.verbose(" Ending of AcademyINVGetSerialSequenceUEImpl -> generateSerialSequence() API");
		return outDocument;
	}
	private void processSerialList(YFSEnvironment env, String fromSerial, String toSerial, Element serialListEle, Document docGetSerialNos) throws YFSUserExitException{
		try{
			log.verbose("Begining of AcademyINVGetSerialSequenceUEImpl -> processSerialList() API ");
			log.verbose("fromSerial = "+fromSerial+" toSerial = "+toSerial);
			if(fromSerial.length() == AcademyConstants.GC_SERIAL_SIZE || toSerial.length() == AcademyConstants.GC_SERIAL_SIZE){
				// Remove the last digit from serial no
				fromSerial = fromSerial.substring(0, fromSerial.length()-1);
				toSerial = toSerial.substring(0, toSerial.length()-1);
				log.verbose(" After removed last digit \n fromSerial = "+fromSerial+" toSerial = "+toSerial);
			}else if(fromSerial.length() != AcademyConstants.GC_SERIAL_SIZE-1 || toSerial.length() != AcademyConstants.GC_SERIAL_SIZE-1){
				String errorCode = "INV80_045";
				if(fromSerial.length() != AcademyConstants.GC_SERIAL_SIZE-1){
					log.verbose("fromSerial = "+fromSerial);
					if(fromSerial.length() <= 0)
						errorCode = "INV80_043";
				}
				if(toSerial.length() != AcademyConstants.GC_SERIAL_SIZE-1){
					log.verbose(" toSerial = "+toSerial);
					if(toSerial.length() <= 0)
						errorCode = "INV80_044";
				}
				log.verbose("Required valid Serial No.......");
				throw new YFSUserExitException(errorCode);
			}
			log.verbose("Check for Non-numeric Serial No.");
			// validate whether the Serial No is only with digits or not.
			String type = validateFromToSerialNumberDatatype(fromSerial,toSerial);
			log.verbose("valid Serial Number ");
			// Input to getSerialNumbers API to check whether SerialNo(s) are already in use or not
			/**
			 * <Serial ItemID="" LocationId="" OrganizationCode="" ProductClass="" ShipNodeKey="" UnitOfMeasure="">
			 * 	<SerialElem FromSerialNo="" LocationType="" ToSerialNo=""/>
			 * </Serial>
			 */	
			log.verbose("Check for Serial Number are in use or not");
			Element eleSerialElem = (Element)docGetSerialNos.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SERIAL_ELEM).item(0);
			eleSerialElem.setAttribute(AcademyConstants.ATTR_FROM_SERIAL_NO, fromSerial);
			eleSerialElem.setAttribute(AcademyConstants.ATTR_TO_SERIAL_NO, toSerial);
			log.verbose("Invoke getSerialNumbers API with 12 digit sequence .. "+XMLUtil.getXMLString(docGetSerialNos));
			Document outGetSerialNos = AcademyUtil.invokeAPI(env, "getSerialNumbers", docGetSerialNos);
			if(outGetSerialNos != null && !YFCObject.isVoid(outGetSerialNos)){
				log.verbose("Output of getSerialNumbers API is : "+XMLUtil.getXMLString(outGetSerialNos));
				int isSequenceReq = outGetSerialNos.getElementsByTagName(AcademyConstants.ELE_SERIAL).getLength();
				if(isSequenceReq > 0){
					log.verbose("The range of serial numbers already requested by some other shipment..");
					throw new YFSUserExitException("INV80_032");
				}
			}
			
			if(type.equals("Integer")){
				int fromSerialNo = Integer.parseInt(fromSerial);
				int toSerialNo = Integer.parseInt(toSerial);
				if(fromSerialNo > toSerialNo){
					throw new YFSUserExitException("INV80_040");					
				}
				int totalSerialNo = toSerialNo - fromSerialNo;
				log.verbose("Total serial scans "+totalSerialNo);
				if(totalSerialNo <= 0){
					throw new YFSUserExitException("INV80_033");
				}
				try{
					/**
					 * <SerialList>
					 * 		<Serial SerialNo="" />
					 * 	</SerialList>
					 */
					for(int i=fromSerialNo; i<=toSerialNo; i++){
						Element eleSerialNo = serialListEle.getOwnerDocument().createElement(AcademyConstants.ELE_SERIAL);
						eleSerialNo.setAttribute(AcademyConstants.ATTR_SERIAL_NO, Integer.toString(i));
						serialListEle.appendChild(eleSerialNo);
					}						
				}catch(Exception ex){
					throw new YFSUserExitException(ex.getMessage());
				}
			}else if(type.equals("Long")){
				long fromSerialNo = Long.parseLong(fromSerial);
				long toSerialNo = Long.parseLong(toSerial);
				if(fromSerialNo > toSerialNo){
					throw new YFSUserExitException("INV80_040");
				}
				long totalSerialNo = toSerialNo - fromSerialNo;
				log.verbose("Total serial scans "+totalSerialNo);
				if(totalSerialNo <= 0){
					throw new YFSUserExitException("INV80_033");
				}
				try{
					/**
					 * <SerialList>
					 * 		<Serial SerialNo="" />
					 * 	</SerialList>
					 */
					for(long i=fromSerialNo; i<=toSerialNo; i++){
						Element eleSerialNo = serialListEle.getOwnerDocument().createElement(AcademyConstants.ELE_SERIAL);
						eleSerialNo.setAttribute(AcademyConstants.ATTR_SERIAL_NO, Long.toString(i));
						serialListEle.appendChild(eleSerialNo);
					}						
				}catch(Exception ex){
					throw new YFSUserExitException(ex.getMessage());
				}
			}			
		}catch(YFSUserExitException ueEx){
			throw ueEx;
		}catch(Exception ex){
			ex.printStackTrace();
			YFSUserExitException ueExce = new YFSUserExitException(ex.getMessage());
			throw ueExce;
		}
	}
	
	private void generateSerialNoInRange(YFSEnvironment env, Element docElement, Element distSerialListEle) throws YFSUserExitException{
		log.verbose("Begining of AcademyINVGetSerialSequenceUEImpl -> generateSerialNoInRange() API ");
		NodeList eleInventoryLst = docElement.getElementsByTagName(AcademyConstants.ELE_INVENTORY);
		if(eleInventoryLst.getLength() <= 0)
			return;
		Document docGetSerialNos = null;
		try{
			docGetSerialNos = XMLUtil.createDocument(AcademyConstants.ELE_SERIAL);
		}catch(Exception e){
			e.printStackTrace();
			throw new YFSUserExitException(e.getMessage());
		}
		Element eleGetSerialNos = docGetSerialNos.getDocumentElement();
		eleGetSerialNos.setAttribute(AcademyConstants.ORGANIZATION_CODE, docElement.getAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE));
		Element elePackLocation = (Element)docElement.getElementsByTagName("PackLocation").item(0);
		eleGetSerialNos.setAttribute("LocationId", elePackLocation.getAttribute("LocationId"));
		eleGetSerialNos.setAttribute("ShipNodeKey", AcademyConstants.WMS_NODE);
		
		for(int inv=0; inv<eleInventoryLst.getLength(); inv++){
			Element eleInventory = (Element)eleInventoryLst.item(inv);
			if(eleInventory.getElementsByTagName(AcademyConstants.ELE_INVENTORY_ITEM).getLength() > 0){
				Element eleInventoryItem = (Element)eleInventory.getElementsByTagName(AcademyConstants.ELE_INVENTORY_ITEM).item(0);
				/*boolean isGCItem = checkForGCItem(env,eleInventoryItem.getAttribute(AcademyConstants.ITEM_ID));
				log.verbose("Is GC Item : "+isGCItem);
				if(!isGCItem)
					continue;*/
				if(eleGetSerialNos != null && !YFCObject.isVoid(eleGetSerialNos)){
					Element eleGetSerilaNos = docGetSerialNos.getDocumentElement();
					eleGetSerilaNos.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleInventoryItem.getAttribute(AcademyConstants.ATTR_ITEM_ID));
					eleGetSerilaNos.setAttribute(AcademyConstants.ATTR_PROD_CLASS, eleInventoryItem.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
					eleGetSerilaNos.setAttribute(AcademyConstants.ATTR_UOM, eleInventoryItem.getAttribute(AcademyConstants.ATTR_UOM));
					Element eleSerialElem = docGetSerialNos.createElement(AcademyConstants.ELE_SERIAL_ELEM);
					eleGetSerialNos.appendChild(eleSerialElem);
				}
				log.verbose("No of SerialList elements are : "+eleInventory.getElementsByTagName(AcademyConstants.ELE_SERIAL_LIST).getLength());
				if(eleInventory.getElementsByTagName(AcademyConstants.ELE_SERIAL_LIST).getLength() <= 0)
					continue;
				NodeList lstSerialList = eleInventory.getElementsByTagName(AcademyConstants.ELE_SERIAL_LIST);
				for(int indx=0; indx<lstSerialList.getLength(); indx++){
					Element eleSerialList = (Element)lstSerialList.item(indx);
					if(eleSerialList.getElementsByTagName(AcademyConstants.ELE_SERIAL_RANGE_LIST).getLength() > 0){
						Element serialRangeList = (Element)eleSerialList.getElementsByTagName(AcademyConstants.ELE_SERIAL_RANGE_LIST).item(0);
						NodeList lstSerialRange = serialRangeList.getElementsByTagName(AcademyConstants.ELE_SERIAL_RANGE);
						log.verbose("No of SerialRange ele are : "+lstSerialRange.getLength());
						if(lstSerialRange.getLength() > 0){
							Element serialRange = (Element)lstSerialRange.item(0);
							String fromSerialNo = serialRange.getAttribute(AcademyConstants.ATTR_FROM_SERIAL_NO);
							String toSerialNo = serialRange.getAttribute(AcademyConstants.ATTR_TO_SERIAL_NO);
							processSerialList(env,fromSerialNo, toSerialNo, distSerialListEle, docGetSerialNos);
						}
					}								
				}				
			}
		}	
		log.verbose(" Ending of AcademyINVGetSerialSequenceUEImpl -> generateSerialNoInRange() API \n"+XMLUtil.getElementXMLString(distSerialListEle));
	}

	/**
	 * This method check weather Item is Gift Card item or not. 
	 * @param env
	 * @param strItemId
	 * @return boolean
	 */
	private boolean checkForGCItem(YFSEnvironment env, String strItemId)throws YFSUserExitException {
		log.verbose("Item Id is : "+strItemId);
		if(!YFCObject.isVoid(strItemId)){
			try{
				Document inputItemList = XMLUtil.getDocument("<Item ItemID=\""+strItemId+"\"><Extn ExtnIsGiftCard=\"Y\" /></Item>");
				Document outputItemList = XMLUtil.getDocument("<ItemList TotalNumberOfRecords=\"\"><Item ItemID=\"\"><Extn /></Item></ItemList>");
				env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, outputItemList);
				log.verbose("Input to getItemList API is : "+XMLUtil.getXMLString(inputItemList));				
				outputItemList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST, inputItemList);
				if(!YFCObject.isVoid(outputItemList)){
					log.verbose("output of getItemList API is :"+XMLUtil.getXMLString(outputItemList));
					NodeList totalNoOfRecords = outputItemList.getDocumentElement().getElementsByTagName(AcademyConstants.ITEM);
					if(totalNoOfRecords.getLength() > 0)
						return true;
				}
			}catch(Exception ex){
				ex.printStackTrace();
				throw new YFSUserExitException(ex.getMessage());
			}			
		}
		return false;
	}	
	
	   private String validateFromToSerialNumberDatatype(String fromSerialNo, String toSerialNo) throws YFSUserExitException{
	        String sDatatype = "";
	        try{
	            Integer.parseInt(fromSerialNo);
	            Integer.parseInt(toSerialNo);
	            sDatatype = "Integer";
	        }
	        catch(NumberFormatException e){
	            if(YFCObject.equals(sDatatype, ""))
	                try{
	                    Long.parseLong(fromSerialNo);
	                    Long.parseLong(fromSerialNo);
	                    sDatatype = "Long";
	                }
	                catch(NumberFormatException ex){
	                    throw new YFSUserExitException("INV80_038");
	                }
	        }
	        return sDatatype;
	    } 
}
