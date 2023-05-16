package com.academy.som;

import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.IYRCApiCallbackhandler;
import com.yantra.yfc.rcp.IYRCPostWindowOpenInitializer;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCEditorInput;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCXmlUtils;

public class AcademyPostWindowOpenInitializer implements
		IYRCPostWindowOpenInitializer, IYRCApiCallbackhandler {
	//	START: SHIN-22
	private static String CLASSNAME = "com.academy.som.AcademyPostWindowOpenInitializer";
	//	END: SHIN-22
	@Override
	public void postWindowOpen() {
		 Composite comp = YRCDesktopUI.getCurrentPage();
		    if ((comp.getClass().getName().equalsIgnoreCase("com.yantra.pca.ycd.rcp.tasks.quickAccess.wizards.YCDQuickAccessWizard"))){
//	    	  YRCPlatformUI.closeEditor("com.yantra.pca.ycd.rcp.editors.YCDQuickAccessEditor", true);
	    	  String editorId = "com.yantra.pca.ycd.rcp.editors.YCDShipmentEditor";
	          String[] attrs = new String[] { "ShipmentKey" };
	          Element printPickTicketInput = createInputforPrintPickTicket();
	          YRCPlatformUI.openEditor(editorId, new YRCEditorInput(printPickTicketInput, attrs, "YCD_TASK_PRINT_PICK_TICKET"));
	          YRCPlatformUI.closeEditor("com.yantra.pca.ycd.rcp.editors.YCDQuickAccessEditor", true);
			  YRCPlatformUI.enableMenuItem("com.yantra.pca.ycd.rcp.tasks.common.actions.YCDHomeAction", false);
		    }
		    //START: SHIN-22
		    // Calling the method callGetShipNodeListAPI
			callGetShipNodeListAPI();
			//END: SHIN-22
	}
    
	private Element createInputforPrintPickTicket()
	{
		Document editorInputXml = YRCXmlUtils.createFromString("<PrintPickTicket/>");
	    editorInputXml.getDocumentElement().setAttribute("ShowShipmentsWithPickupLines", "Y");
	    return editorInputXml.getDocumentElement();
	}
	//START STL-1678 To set IsUPCBaseBPSearchEnable="Y". Based on this flag UPC field will be enable in Backroom pick search screen.
	/** Invoke getCommonCodeList API for common code UPC_SEARCH_BP
	 * 
	 */
	private void getCommonCodeListForUPCSearchBP() {
		String strMethodName = "callGetCommonCodeListForUPCSearchBP()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, strMethodName);
		
		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.GET_COMMON_CODE_LIST);
		context.setInputXml(getCommonCodeListInput(AcademyPCAConstants.STR_COMMON_CODE_UPC_SEARCH_BP));
		YRCPlatformUI.callApi(context, this);		

		AcademySIMTraceUtil.endMessage(CLASSNAME,strMethodName);
	}
	
	/** invoke getCommonCodeList API for the given CodeType
	 * @param codeType
	 * @return
	 */
	private Document getCommonCodeListInput(String codeType) {
		AcademySIMTraceUtil
				.logMessage("Inside getCommonCodeListInput for " + codeType);
		String strMethodName = "getCommonCodeListInput()";
		AcademySIMTraceUtil.startMessage(CLASSNAME,strMethodName);
		
		Document docInputgetCommonCodeList = YRCXmlUtils.createDocument(AcademyPCAConstants.COMMON_CODE_ELEMENT);
		Element rootElement = docInputgetCommonCodeList.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.CODE_TYPE_ATTR, codeType);
		rootElement.setAttribute(AcademyPCAConstants.CODE_VALUE_ATTR, YRCPlatformUI.getUserElement().getAttribute(AcademyPCAConstants.SHIPNODE_ATTR));		
		AcademySIMTraceUtil.logMessage("INPUT to getCommonCodeList : "+ YRCXmlUtils.getString(rootElement));
		
		AcademySIMTraceUtil.endMessage(CLASSNAME,strMethodName);
		return docInputgetCommonCodeList;
	}
	//END STL-1678 To set IsUPCBaseBPSearchEnable="Y". Based on this flag UPC field will be enable in Backroom pick search screen. 
	
	//START: SHIN-22
	// This callGetShipNodeListAPI() will invoke the getShipNodeList API

	public void callGetShipNodeListAPI() {
		String strMethodName = "callGetShipNodeListAPI()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, strMethodName);
		
		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.GET_SHIP_NODE_LIST_COMMAND);
		context.setInputXml(prepareInputForGetShipNodeList());
		YRCPlatformUI.callApi(context, this);
		

		AcademySIMTraceUtil.endMessage(CLASSNAME,strMethodName);
	}
	
	// The method will return form id
	private String getFormId() {
		return "com.yantra.pca.ycd.rcp.tasks.common.advancedShipmentSearch.screens.YCDAdvancedShipmentSearchCriteriaPanel";
	}

	// The method will prepare the input for getshipNodeList API
	public Document prepareInputForGetShipNodeList() {
		String str = "prepareInputForGetShipNodeList()";
		
		AcademySIMTraceUtil.startMessage(CLASSNAME, str);
		Document getShipNodeListInputDoc = YRCXmlUtils
				.createDocument(AcademyPCAConstants.SHIPNODE_ATTR);
		Element eleRootElement = getShipNodeListInputDoc.getDocumentElement();
		eleRootElement.setAttribute(AcademyPCAConstants.SHIPNODE_ATTR,
				YRCPlatformUI.getUserElement().getAttribute(
						AcademyPCAConstants.ATTR_NODE));
		
		AcademySIMTraceUtil.endMessage(CLASSNAME,str);
		
		return getShipNodeListInputDoc;
		
		
	}


	
    /*This method will the handle the API completion and from the API output 
     * fetches the attribute NodeType and checking whether it is SharedInventoryDC.
     * If IsSharedInventoryDC set the attribute as Y OR N*/
	public void handleApiCompletion(YRCApiContext context) {
		
		String strMethodName = "handleApiCompletion()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, strMethodName);
		Document shipNodeListDoc;
		Element eleshipNode;
		String strNodeType="";
		Element eleGetCommonCodeListOutput;
		Element eleGetCommonCodeListInput;
				
		if (context.getInvokeAPIStatus() < 0) {
			//to handle API call failures
			AcademySIMTraceUtil.logMessage(context.getApiName()
					+ " call Failed");
			AcademySIMTraceUtil.logMessage("Error Output: \n", context
					.getOutputXml().getDocumentElement());
		} else {

			if (context.getApiName().equals(
					AcademyPCAConstants.GET_SHIP_NODE_LIST_COMMAND)) {
				AcademySIMTraceUtil
						.logMessage("handleApiCompletion(context) :: "
								+ context.getApiName());
				
				shipNodeListDoc = context.getOutputXml();
				eleshipNode = (Element) shipNodeListDoc
						.getElementsByTagName(AcademyPCAConstants.SHIPNODE_ATTR)
						.item(0);
				strNodeType = eleshipNode.getAttribute(AcademyPCAConstants.ATTR_NODE_TYPE);
			
				if (AcademyPCAConstants.ATTR_VAL_SHAREDINV_DC.equals(strNodeType)){
					YRCPlatformUI.getUserElement().setAttribute(AcademyPCAConstants.ATTR_IS_SHAREDINV_DC,AcademyPCAConstants.STRING_Y);
					AcademySIMTraceUtil
					.logMessage("IsSharedInventoryDC::"+YRCPlatformUI.getUserElement().getAttribute(AcademyPCAConstants.ATTR_IS_SHAREDINV_DC));
					
					//START STL-1678 To set IsUPCBaseBPSearchEnable="Y". Based on this flag UPC field will be enable in Backroom pick search screen. 
					getCommonCodeListForUPCSearchBP();
					//END STL-1678 To set IsUPCBaseBPSearchEnable="Y". Based on this flag UPC field will be enable in Backroom pick search screen. 
				} else {
					YRCPlatformUI.getUserElement().setAttribute(AcademyPCAConstants.ATTR_IS_SHAREDINV_DC,AcademyPCAConstants.STRING_N);
					AcademySIMTraceUtil
					.logMessage("IsSharedInventoryDC::"+YRCPlatformUI.getUserElement().getAttribute(AcademyPCAConstants.ATTR_IS_SHAREDINV_DC));
				}
				
			}
			//START STL-1678 To set IsUPCBaseBPSearchEnable="Y". Based on this flag UPC field will be enable in Backroom pick search screen. 
			else if (context.getApiName().equals(AcademyPCAConstants.GET_COMMON_CODE_LIST)) {
				AcademySIMTraceUtil.logMessage("handleApiCompletion(context) :: " + context.getApiName());
				eleGetCommonCodeListInput = context.getInputXml().getDocumentElement();
				eleGetCommonCodeListOutput = context.getOutputXml().getDocumentElement();
				
				if(AcademyPCAConstants.STR_COMMON_CODE_UPC_SEARCH_BP.equals(eleGetCommonCodeListInput.getAttribute(AcademyPCAConstants.CODE_TYPE_ATTR))){
					Element eleCommonCode = (Element) eleGetCommonCodeListOutput.getElementsByTagName(AcademyPCAConstants.COMMON_CODE_ELEMENT).item(0);

					if(!YRCPlatformUI.isVoid(eleCommonCode)){
						YRCPlatformUI.getUserElement().setAttribute(AcademyPCAConstants.ATTR_IS_UPC_BASE_BP_SEARCH_ENABLE,AcademyPCAConstants.STRING_Y);
						AcademySIMTraceUtil.logMessage("IsUPCBaseBPSearchEnable::Y");
					} else {
						YRCPlatformUI.getUserElement().setAttribute(AcademyPCAConstants.ATTR_IS_UPC_BASE_BP_SEARCH_ENABLE,AcademyPCAConstants.STRING_N);
						AcademySIMTraceUtil.logMessage("IsUPCBaseBPSearchEnable::N");
					}
				}				
			}	
			//END STL-1678 To set IsUPCBaseBPSearchEnable="Y". Based on this flag UPC field will be enable in Backroom pick search screen. 
		}
		AcademySIMTraceUtil.endMessage(CLASSNAME,strMethodName);
	}
	//END: SHIN-22
	            
}
