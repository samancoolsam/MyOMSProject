package com.academy.ecommerce.sterling.bopis.sfspacking.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Document;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * This class will be invoked fot SFS shipment on Finish Pack button on Web SOM.
 * 
 * @author vargoel
 * 
 */
public class AcademySFSProcessFinishPack implements YIFCustomApi{
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySFSProcessFinishPack.class);
	List<Document> arrayListShipment = new ArrayList<Document>();

	/**
	 * This is the main method which will read the input and invoke following
	 * methods to execute the actions on click of Finish Pack button.
	 * 
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document processFinishPack(YFSEnvironment env, Document inDoc) throws Exception {
		
		log.beginTimer("AcademySFSProcessFinishPack::processFinishPack");
		log.verbose("Entering the method AcademySFSProcessFinishPack.processFinishPack");
		
		YFCDocument yfcOutputAcademySFSProcessFinishPick = null;
		
		try {
			YFCDocument yfcInputDoc = YFCDocument.getDocumentFor(inDoc);
			YFCElement eleInputRoot = yfcInputDoc.getDocumentElement();
			String strShipmentKey = AcademyConstants.STR_BLANK;
			String strDeliveryMethod = AcademyConstants.STR_BLANK;
			YFCNodeList<YFCElement> nlShipment = eleInputRoot.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
			String strDocType = null;
			for (YFCElement eleShipment : nlShipment) {
				strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
				strDocType =  eleShipment.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
				// Invoke method for AcademySFSBeforeCreateContainersAndPrintService
				invokeSFSBeforeContAndPrinService(env, eleShipment);
			}

			// Prepare Input doc for Corrugation Item update
			YFCDocument yfcDocChangeShipmentForCorrugationItem = YFCDocument
					.createDocument(AcademyConstants.ELE_SHIPMENT);
			YFCElement eleChangeShip = yfcDocChangeShipmentForCorrugationItem.getDocumentElement();
			YFCElement eleContainers = eleChangeShip.createChild(AcademyConstants.ELE_CONTAINERS);
			boolean boolIsChangeShipmentRequired = false;
			// Iterate over arrayList.
			for (Document docUpdatedShipment : arrayListShipment) {
				if (log.isVerboseEnabled()) {
					log.verbose("Input to AcademySFSCreateContainersAndPrintService service: ="
							+ XMLUtil.getXMLString(docUpdatedShipment));
				}
				strDocType = docUpdatedShipment.getDocumentElement().getAttribute(AcademyConstants.ATTR_DOC_TYPE);

				// invoke method for AcademySFSCreateContainersAndPrintService
				AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_SFS_CREATE_CONT_PRINT_SERVICE,
						docUpdatedShipment);
				YFCDocument yfcDocUpdatedShipment = YFCDocument.getDocumentFor(docUpdatedShipment);
				YFCElement eleShipmentUpdate = yfcDocUpdatedShipment.getDocumentElement();
				strDeliveryMethod = eleShipmentUpdate.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
				if (YFCCommon.equalsIgnoreCase(eleShipmentUpdate.getAttribute("ContainerType"), "C31")
						&& YFCCommon.equalsIgnoreCase(eleShipmentUpdate.getAttribute("CarrierServiceCode"),
								AcademyConstants.STR_PRIORITY_MAIL)) {
					boolIsChangeShipmentRequired = true;
					eleChangeShip.setAttribute("ShipmentKey",
							eleShipmentUpdate.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
					eleChangeShip.setAttribute("CarrierServiceCode", AcademyConstants.STR_FIRST_CLASS_MAIL);
					YFCElement eleContainer = eleContainers.createChild(AcademyConstants.ELE_CONTAINER);
					eleContainer.setAttribute("CarrierServiceCode", AcademyConstants.STR_FIRST_CLASS_MAIL);
				}
			}
			if (boolIsChangeShipmentRequired) {
				if (log.isVerboseEnabled()) {
					log.verbose("Input to changeShipment api to update carrier service code: ="
							+ XMLUtil.getXMLString(yfcDocChangeShipmentForCorrugationItem.getDocument()));
				}
				Document docChangeShipment = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT,
						yfcDocChangeShipmentForCorrugationItem.getDocument());
				strDocType = docChangeShipment.getDocumentElement().getAttribute(AcademyConstants.ATTR_DOC_TYPE);

			}
			// Invoke method for AcademyOOTBChangeShipmentOnFinishPack service
			changeShipOnFinishPack(env, strShipmentKey);
			// Invoke method to update Invoice number.
			
			/* Start - OMNI-46029 - Skip Invoice stamping for STS2.0 Shipments */
			if(!AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE.equals(strDocType)) {
				stampInvoiceNo(env, strShipmentKey, strDeliveryMethod);
			}
			/* End - OMNI-46029 - Skip Invoice stamping for STS2.0 Shipments */
			// Invoke method for StoreContLabel_94

//			for (Document docUpdatedShipment : arrayListShipment) {
//				invokeStoreContLabelService(env, docUpdatedShipment);
//			}
			// Invoke method for AcademyAddContainersToManifestWrapperService
			// service
			invokeAddContToManifestService(env, strShipmentKey);
			// return output from AcademySFSProcessFinishPack Service

			yfcOutputAcademySFSProcessFinishPick = outputDocumentFromService();
			log.endTimer("AcademySFSProcessFinishPack::processFinishPack");
			if (log.isVerboseEnabled()) {
				log.verbose("Output from AcademySFSProcessFinishPack service on Finish Pack: ="
						+ XMLUtil.getXMLString(yfcOutputAcademySFSProcessFinishPick.getDocument()));
			}

		} catch (YFCException e) {
			e.printStackTrace();
			throw e;		
		} catch (YFSException e) {
			e.printStackTrace();
			YFCException yfcexcep = new YFCException(AcademyConstants.ERROR_CODE_SFS0001);
			yfcexcep.setAttribute("YFS_ERROR_CODE", e.getErrorCode());
			yfcexcep.setErrorDescription(e.getErrorDescription());
			yfcexcep.setStackTrace(e.getStackTrace());
			throw yfcexcep;
		}
		catch (Exception e) {
			e.printStackTrace();
			YFCException yfcexcep = new YFCException(AcademyConstants.ERROR_CODE_SFS0001);
			yfcexcep.setErrorDescription(e.getMessage());
			yfcexcep.setStackTrace(e.getStackTrace());
			throw yfcexcep;
		}
		return yfcOutputAcademySFSProcessFinishPick.getDocument();
	}

	/**
	 * @param env
	 * @param strShipmentKey
	 * @throws Exception
	 *             This methos will prepare input to stamp invoice number for the
	 *             shipment.
	 */
	public void stampInvoiceNo(YFSEnvironment env, String strShipmentKey, String strDeliveryMethod) throws Exception {
		log.beginTimer("AcademySFSProcessFinishPack::stampInvoiceNo");
		log.verbose("Entering the method AcademySFSProcessFinishPack.stampInvoiceNo");

		YFCDocument yfcDocInvoiceNo = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
		YFCElement eleShipmentInvoiceNo = yfcDocInvoiceNo.getDocumentElement();
		eleShipmentInvoiceNo.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
		if (!YFCCommon.isVoid(strDeliveryMethod)) {
			eleShipmentInvoiceNo.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, strDeliveryMethod);
		} else {
			eleShipmentInvoiceNo.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, "SHP");
		}
		if (log.isVerboseEnabled()) {
			log.verbose("Input to AcademyStampInvoiceNoOnBOPISOrders: ="
					+ XMLUtil.getXMLString(yfcDocInvoiceNo.getDocument()));
		}

		AcademyUtil.invokeService(env, AcademyConstants.SER_STAMP_INVOICE_NO, yfcDocInvoiceNo.getDocument());
		log.endTimer("AcademySFSProcessFinishPack::stampInvoiceNo");
	}

	/**
	 * @param env
	 * @param strShipmentKey
	 * @throws Exception
	 *             This method prepares input to invoke
	 *             AcademyOOTBChangeShipmentOnFinishPack service.
	 */
	public void changeShipOnFinishPack(YFSEnvironment env, String strShipmentKey) throws Exception {
		log.beginTimer("AcademySFSProcessFinishPack::changeShipOnFinishPack");
		log.verbose("Entering the method AcademySFSProcessFinishPack.changeShipOnFinishPack");

		YFCDocument yfcDocInputForOOTBChangeShipOnFP = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
		YFCElement eleShipmentOOTBChangeShipOnFP = yfcDocInputForOOTBChangeShipOnFP.getDocumentElement();
		eleShipmentOOTBChangeShipOnFP.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
		eleShipmentOOTBChangeShipOnFP.setAttribute(AcademyConstants.ATTR_IS_PACK_PROCESS_COMPLETED,
				AcademyConstants.STR_YES);
		eleShipmentOOTBChangeShipOnFP.setAttribute("ActualFreightCharge", AcademyConstants.STR_BLANK);
		eleShipmentOOTBChangeShipOnFP.setAttribute("ContainerizedQuantity", AcademyConstants.STR_BLANK);
		if (log.isVerboseEnabled()) {
			log.verbose("Input to AcademyOOTBChangeShipmentOnFinishPack : ="
					+ XMLUtil.getXMLString(yfcDocInputForOOTBChangeShipOnFP.getDocument()));
		}

		Document docOOCChangeShipOnFinishPack = AcademyUtil.invokeService(env,
				AcademyConstants.SER_ACAD_OOB_CHANGE_SHIP_FINISH_PACK, yfcDocInputForOOTBChangeShipOnFP.getDocument());
		log.endTimer("AcademySFSProcessFinishPack::changeShipOnFinishPack");
		if (log.isVerboseEnabled()) {
			log.verbose("Output from AcademyOOTBChangeShipmentOnFinishPack service: ="
					+ XMLUtil.getXMLString(docOOCChangeShipOnFinishPack));
		}
	}

	/**
	 * @return This Method will return the output from this service on Success
	 */
	public YFCDocument outputDocumentFromService() {
		// prepare output document from this service
		log.beginTimer("AcademySFSProcessFinishPack::outputDocumentFromService");
		log.verbose("Entering the method AcademySFSProcessFinishPack.outputDocumentFromService");

		YFCDocument yfcOutputAcademySFSProcessFinishPick = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
		YFCElement eleOutputAcadSFSProcessFinishPick = yfcOutputAcademySFSProcessFinishPick.getDocumentElement();
		eleOutputAcadSFSProcessFinishPick.setAttribute(AcademyConstants.ATTR_IS_SUCCESS, AcademyConstants.STR_YES);
		log.endTimer("AcademySFSProcessFinishPack::outputDocumentFromService");
		if (log.isVerboseEnabled()) {
			log.verbose("Output from outputDocumentFromService service: ="
					+ XMLUtil.getXMLString(yfcOutputAcademySFSProcessFinishPick.getDocument()));
		}

		return yfcOutputAcademySFSProcessFinishPick;
	}

	/**
	 * @param env
	 * @param eleShipment
	 *            This method will create input and invoke
	 *            AcademyAddContainersToManifestWrapperService To add container to
	 *            manifest.
	 * @throws Exception 
	 */
	public void invokeAddContToManifestService(YFSEnvironment env, String strShipmentKey) throws Exception {
		log.beginTimer("AcademySFSProcessFinishPack::invokeAddContToManifestService");
		log.verbose("Entering the method AcademySFSProcessFinishPack.invokeAddContToManifestService");

		// prepare input to service
		YFCDocument yfcDocInputAddContToManifest = YFCDocument.createDocument(AcademyConstants.ELE_CONTAINER);
		YFCElement eleInpAddContManifest = yfcDocInputAddContToManifest.getDocumentElement();
		eleInpAddContManifest.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			if (log.isVerboseEnabled()) {
				log.verbose("Input to AcademyAddContainersToManifestWrapperService: ="
						+ XMLUtil.getXMLString(yfcDocInputAddContToManifest.getDocument()));
			}

			Document docOutputAddContToManifest = AcademyUtil.invokeService(env,
					AcademyConstants.SER_ACAD_ADD_CONT_TO_MANIFEST_SERVICE, yfcDocInputAddContToManifest.getDocument());
			log.endTimer("AcademySFSProcessFinishPack::invokeAddContToManifestService");
			if (log.isVerboseEnabled()) {
				log.verbose("Output from AcademyAddContainersToManifestWrapperService: ="
						+ XMLUtil.getXMLString(docOutputAddContToManifest));
			}

	}

	/**
	 * @param env
	 * @param eleShipment
	 * @throws Exception
	 *             This method will create input and invoke
	 *             AcademySFSBeforeCreateContainersAndPrintService.
	 */
	public void invokeSFSBeforeContAndPrinService(YFSEnvironment env, YFCElement eleShipment) throws Exception {
		// prepare input to service
		log.beginTimer("AcademySFSProcessFinishPack::invokeSFSBeforeContAndPrinService");
		log.verbose("Entering the method AcademySFSProcessFinishPack.invokeSFSBeforeContAndPrinService");

		try {
			YFCDocument yfcInputSFSBeforeCreateContAndPrint = YFCDocument.getDocumentFor(eleShipment.toString());
			// YFCElement eleInputSFSBeforeCreateContAndPrintRoot =
			// yfcInputSFSBeforeCreateContAndPrint.getDocumentElement();
			if (log.isVerboseEnabled()) {
				log.verbose("Input to AcademySFSBeforeCreateContainersAndPrintService: ="
						+ XMLUtil.getXMLString(yfcInputSFSBeforeCreateContAndPrint.getDocument()));
			}

			Document docOutputCreateContainerAndPrintService = AcademyUtil.invokeService(env,
					AcademyConstants.SER_ACAD_BEFORE_CREATE_CONT_PRINT_SERVICE,
					yfcInputSFSBeforeCreateContAndPrint.getDocument());
			if (log.isVerboseEnabled()) {
				log.verbose("Output from AcademySFSBeforeCreateContainersAndPrintService: ="
						+ XMLUtil.getXMLString(docOutputCreateContainerAndPrintService));
			}
			YFCDocument yfcDocOutputCreateContainerAndPrintService = YFCDocument
					.getDocumentFor(docOutputCreateContainerAndPrintService);
			arrayListShipment.add(yfcDocOutputCreateContainerAndPrintService.getDocument());

		} catch (YFSException e) {
			e.printStackTrace();
			YFCException excep = new YFCException("SFS0001");
			excep.setAttribute(AcademyConstants.ATTR_ERROR_DESC, (e.getErrorDescription()));
			excep.setAttribute("YFS_ERROR_CODE", e.getErrorCode());
			excep.setStackTrace(e.getStackTrace());
			throw excep;
		}
		log.endTimer("AcademySFSProcessFinishPack::invokeSFSBeforeContAndPrinService");
	}

	/**
	 * @param env
	 * @param eleShipment
	 * @throws Exception
	 *             This method will create input and invoke StoreContainerLabel_94
	 *             service.
	 */
	public void invokeStoreContLabelService(YFSEnvironment env, Document docUpdatedShipment) throws Exception {
		log.beginTimer("AcademySFSProcessFinishPack::invokeStoreContLabelService");
		log.verbose("Entering the method AcademySFSProcessFinishPack.invokeStoreContLabelService");
		// prepare input to service
		YFCDocument yfcInputStoreContainerLabel = YFCDocument.createDocument(AcademyConstants.ELE_CONTAINERS);
		YFCElement eleInputToStoreContainersLabelRoot = yfcInputStoreContainerLabel.getDocumentElement();
		YFCElement eleInputToStoreContainerLabel = eleInputToStoreContainersLabelRoot
				.createChild(AcademyConstants.ELE_CONTAINER);
		YFCElement eleInputToStoreContLabelShipment = eleInputToStoreContainerLabel
				.createChild(AcademyConstants.ELE_SHIPMENT);
		YFCDocument yfcDocUpdatedShipment = YFCDocument.getDocumentFor(docUpdatedShipment);
		YFCElement eleShipment = yfcDocUpdatedShipment.getDocumentElement();
		if (!YFCCommon.isVoid(eleShipment.getAttribute(AcademyConstants.ATTR_SCAC))) {
			eleInputToStoreContainerLabel.setAttribute(AcademyConstants.ATTR_SCAC,
					eleShipment.getAttribute(AcademyConstants.ATTR_SCAC));
		}
		eleInputToStoreContLabelShipment.setAttribute(AcademyConstants.ATTR_SHIP_NODE,
				eleShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE));
		eleInputToStoreContainerLabel.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY,
				eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY));
		eleInputToStoreContainerLabel.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
				eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
		if (log.isVerboseEnabled()) {
			log.verbose(
					"Input to StoreContLabel_94: =" + XMLUtil.getXMLString(yfcInputStoreContainerLabel.getDocument()));
		}
		Document docOutputStoreContLabel_94 = AcademyUtil.invokeService(env, AcademyConstants.SER_STORE_CONT_LABEL_94,
				yfcInputStoreContainerLabel.getDocument());
		log.endTimer("AcademySFSProcessFinishPack::invokeStoreContLabelService");
		if (log.isVerboseEnabled()) {
			log.verbose("Output from StoreContLabel_94: =" + XMLUtil.getXMLString(docOutputStoreContLabel_94));
		}

	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
