//package declaration

package com.academy.ecommerce.sterling.shipment;

//java util import statements
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
//w3c util import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//academy util import statements
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
//yantra util import statements
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.ycp.core.YCPContext;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyPrintPackSlipForBulk implements YIFCustomApi
{
	//set the logger
	private static YFCLogCategory	log		= YFCLogCategory.instance(AcademyPrintPackSlipForBulk.class);
	//Declare document variable
	private Document outDoc	= null;

	public Document printPackSlipForBulk(YFSEnvironment env, Document inDoc) throws Exception
	{
		log.beginTimer(" Begining of AcademyPrintPackSlipForBulk -> Api");
		//Check if input xml is not void
		if (!YFCObject.isVoid(inDoc.getDocumentElement()))
		{
			if(log.isVerboseEnabled()){
			log.verbose("*** invoking generateContainerSCM with input doc *** " + XMLUtil.getXMLString(inDoc));
			}
			//Fetch the NodeList of element Container
			NodeList nListContainer = XMLUtil.getNodeList(inDoc.getDocumentElement(), "Container");
			//Check if NodeList is not void
			if (!YFCObject.isVoid(nListContainer))
			{
				//Store the NodeList record count into an integer variable
				int iLength = nListContainer.getLength();
				//Declare the element variable
				Element eContainer = null;
				//Loop through the NodeList record
				for (int i = 0; i < iLength; i++)
				{
					//Fetch the element Container
					eContainer = (Element) nListContainer.item(i);
					generateContainerSCM(env, eContainer);

					// start change for SFS
					//((YCPContext) env).commit();
					// end change for SFS

					if (isWGCarrier(env, eContainer.getAttribute("SCAC")))
					{
						/* OMNI-57396 Start : Preventing Pack slip generation for WG*/
						//printPackSlipForWhiteGloveBulkShipment(env, eContainer);
						/* OMNI-57396 End */
						/* OMNI-59163 Start */
						Document docContainer = XMLUtil.getDocumentForElement(eContainer);
						AcademyUtil.invokeService(env, AcademyConstants.SERV_ACADEMY_STAMP_INVOICE_NO_FOR_WG, docContainer);
						inDoc = outDoc;
						/* OMNI-59163 End */
					} else
					{
						printPackSlipForNonWhiteGloveBulkShipment(env, eContainer);
					}
				}
			}
		}
		log.endTimer(" End of AcademyPrintPackSlipForBulk  ->printPackSlipForBulk Api");
		return outDoc;

	}

	private void printPackSlipForNonWhiteGloveBulkShipment(YFSEnvironment env, Element eContainer) throws ParserConfigurationException, Exception
	{
		log.beginTimer(" Begin of AcademyPrintPackSlipForBulk  ->printPackSlipForNonWhiteGloveBulkShipment Api");
		log.verbose("*** Carrier getting used is *** " + eContainer.getAttribute("SCAC"));
		Document docContainerList = XMLUtil.createDocument("ContainerList");
		Element eleContainer = (Element) docContainerList.importNode(eContainer, true);
		docContainerList.getDocumentElement().appendChild(eleContainer);
		outDoc = AcademyUtil.invokeService(env, "AcademyGetTrackingNo", docContainerList);
		log.endTimer(" End of AcademyPrintPackSlipForBulk  ->printPackSlipForNonWhiteGloveBulkShipment Api");
	}

	private void printPackSlipForWhiteGloveBulkShipment(YFSEnvironment env, Element eContainer) throws ParserConfigurationException, Exception
	{
		log.verbose("*** Carrier getting used is *** " + eContainer.getAttribute("SCAC"));
		Document docContainer = XMLUtil.getDocumentForElement(eContainer);
		outDoc = AcademyUtil.invokeService(env, "AcademyPrintShippingLabelForWhiteGlove", docContainer);
	}

	private void generateContainerSCM(YFSEnvironment env, Element eContainer) throws ParserConfigurationException, Exception
	{
		log.beginTimer(" begining of AcademyPrintPackSlipForBulk  ->generateContainerSCM Api");

		// start added for SFS
		//Fetch the attribute value of ContainerScm
		String containerScm = eContainer.getAttribute("ContainerScm").trim();
		//Check if ContainerScm is not blank
		if (!containerScm.equals(""))
		{
			return;
		}

		// end added for SFS

		Document docChangeShipmentContainer = XMLUtil.createDocument("Container");
		docChangeShipmentContainer.getDocumentElement().setAttribute("ShipmentContainerKey", eContainer.getAttribute("ShipmentContainerKey"));
		docChangeShipmentContainer.getDocumentElement().setAttribute("GenerateContainerScm", "Y");
		Document changeShipmentContainerDoc = AcademyUtil.invokeAPI(env, "changeShipmentContainer", docChangeShipmentContainer);


		// start added for SFS, code moved from printPackSlipForBulk
		((YCPContext) env).commit();
		// end added for SFS, code moved from printPackSlipForBulk

		if(log.isVerboseEnabled()){
		log.verbose("*** changeShipmentContainer API call to generate SCM *** " + XMLUtil.getXMLString(changeShipmentContainerDoc));
		}
		log.endTimer(" End of AcademyPrintPackSlipForBulk  ->generateContainerSCM Api");
	}

	public void setProperties(Properties arg0) throws Exception
	{
	}

	private boolean isWGCarrier(YFSEnvironment env, String curScac) throws Exception
	{
		Document docWGSCACLst = getWhiteGloveScacList(env);
		if (docWGSCACLst != null)
		{
			Node nCommonCode = XPathUtil.getNode(docWGSCACLst, "CommonCodeList/CommonCode[@CodeValue='" + curScac + "']");
			if (nCommonCode != null)
				return true;
		}
		return false;
	}

	private Document getWhiteGloveScacList(YFSEnvironment env) throws Exception
	{
		Document docWGSCACLst = null;
		try
		{
			Document docScacCodeInput = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			docScacCodeInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.STR_WG_SCAC_CODE);
			docScacCodeInput.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
			env.setApiTemplate(AcademyConstants.API_GET_COMMON_CODELIST, "global/template/api/getCommonCodeList.IsWhiteGloveSCAC.xml");
			docWGSCACLst = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST, docScacCodeInput);
			env.clearApiTemplate(AcademyConstants.API_GET_COMMON_CODELIST);
		} catch (Exception e)
		{
			e.printStackTrace();
			log.verbose("Failed to invoke getCommonCodeList API : " + e);
			throw e;
		}
		return docWGSCACLst;
	}
}
