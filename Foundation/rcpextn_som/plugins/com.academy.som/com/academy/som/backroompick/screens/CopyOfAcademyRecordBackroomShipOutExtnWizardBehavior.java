package com.academy.som.backroompick.screens;

import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCBaseBehavior;
import com.yantra.yfc.rcp.YRCDialog;
import com.yantra.yfc.rcp.YRCEvent;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCFormatResponse;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.YRCXPathUtils;
import com.yantra.yfc.rcp.YRCXmlUtils;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.xpath.XPathConstants;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CopyOfAcademyRecordBackroomShipOutExtnWizardBehavior extends YRCWizardExtensionBehavior
{
  private String weight;
  private String containerType;
  private static String CLASSNAME = "AcademyRecordBackroomShipOutExtnWizardBehavior";
  private String pageBeingShown;
  public boolean completePickDone;
  private boolean noPickDone;
  private boolean shipDtlsModelSet;
  private String strPackslipPrinterId;
  private String strSinglePckstnCodeValue;
  private String strConcatShipNode;
  private String strCommonComboValue;
  private String strComboboxValue;
  HashMap<String, String> packMap = new HashMap<String, String>();

  public void init()
  {
  }

  public String getExtnNextPage(String currentPageId)
  {
    return null;
  }

  public IYRCComposite createPage(String pageIdToBeShown)
  {
    return null;
  }

  public void pageBeingDisposed(String pageToBeDisposed)
  {
    String str1 = "pageBeingDisposed(pageToBeDisposed)";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "pageBeingDisposed(pageToBeDisposed)");

    if (pageToBeDisposed
      .equals("com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPRecordBackroomShortageResolution"))
    {
      YRCPlatformUI.enableAction(
        "BACKROOM_PICK_NEXT_ACTION", 
        true);
    }

    AcademySIMTraceUtil.endMessage(CLASSNAME, "pageBeingDisposed(pageToBeDisposed)");
  }

  public void initPage(String pageBeingShown)
  {
    String str1 = "initPage(pageBeingShown)";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "initPage(pageBeingShown)");

    if ((pageBeingShown
      .equals("com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPOBERecordBackroomPickShipOut")) || 
      (pageBeingShown
      .equals("com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPRecordBackroomPickScanMode")) || 
      (pageBeingShown
      .equals("com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPRecordBackroomPickSearch")))
    {
      this.pageBeingShown = pageBeingShown;
      AcademySIMTraceUtil.logMessage("for pageBeingShown: " + 
        pageBeingShown + " :: START");

      YRCPlatformUI.enableAction(
        "BACKROOM_PICK_NEXT_ACTION", 
        true);
      callApiForContainerTypes();
      AcademySIMTraceUtil.logMessage("for pageBeingShown: " + 
        pageBeingShown + " :: END");
    }
    else {
      YRCPlatformUI.enableAction(
        "BACKROOM_PICK_NEXT_ACTION", 
        false);
    }

    if (pageBeingShown
      .equals("com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPRecordBackroomPickScanMode"))
    {
      this.shipDtlsModelSet = false;

      addEventHandler("txtItemID", 
        31);
      addEventHandler("text2", 
        31);
      getCommonCodeList("MUL_PACK_STORE");
    }

    if (pageBeingShown
      .equals("com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPRecordBackroomShortageResolution"))
    {
      setFieldValue("rdbSearchOnOrder", Boolean.valueOf(true));

      getCommonCodeList("INV_SHRT_RSN");
      //getCommonCodeList("MUL_PACK_STORE");
    }

    AcademySIMTraceUtil.endMessage(CLASSNAME, "initPage(pageBeingShown)");
  }

  private void getCommonCodeList(String CodeType)
  {
    AcademySIMTraceUtil.logMessage("Inside getCommonCodeList for Pack station");
    YRCApiContext yrcApiContext = new YRCApiContext();
    yrcApiContext.setApiName("getCommonCodeList");
    Document inDoc = YRCXmlUtils.createDocument("CommonCode");

    yrcApiContext.setInputXml(getCommonCodeListInput(inDoc, CodeType).getOwnerDocument());
    AcademySIMTraceUtil.logMessage("Code Type for Pack station: " + CodeType);
    yrcApiContext.setFormId(getFormId());

    callApi(yrcApiContext);
  }

  private Element getCommonCodeListInput(Document docInputgetCommonCodeList, String codeType)
  {
    AcademySIMTraceUtil.logMessage("Inside getCommonCodeListInput for Pack station");
    String str = "getCommonCodeListInput(docInputgetCommonCodeList)";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "getCommonCodeListInput(docInputgetCommonCodeList)");
    Element rootElement = docInputgetCommonCodeList.getDocumentElement();
    rootElement.setAttribute("CodeType", codeType);
    AcademySIMTraceUtil.logMessage("INPUT to getCommonCodeList for Pack station : " + YRCXmlUtils.getString(rootElement));
    AcademySIMTraceUtil.endMessage(CLASSNAME, "getCommonCodeListInput(docInputgetCommonCodeList)");
    return rootElement;
  }

  private void callApiForContainerTypes()
  {
    String str = "callApiForContainerTypes()";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "callApiForContainerTypes()");

    YRCApiContext context = new YRCApiContext();
    context.setApiName("getItemList");
    context.setFormId(getFormId());
    context.setInputXml(prepareInputForContainerTypes());
    callApi(context);

    AcademySIMTraceUtil.endMessage(CLASSNAME, "callApiForContainerTypes()");
  }

  private Document prepareInputForContainerTypes()
  {
    String str = "prepareInputForContainerTypes()";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "prepareInputForContainerTypes()");

    Document itemInputDoc = 
      YRCXmlUtils.createDocument("Item");
    itemInputDoc.getDocumentElement().setAttribute(
      "IsShippingCntr", 
      "Y");

    AcademySIMTraceUtil.endMessage(CLASSNAME, "prepareInputForContainerTypes()");
    return itemInputDoc;
  }

  private void callServiceForShipmentContainerProcessing(Element shipmentElement)
  {
    String str = "callServiceForShipmentContainerProcessing(shipmentElement)";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "callServiceForShipmentContainerProcessing(shipmentElement)");

    YRCApiContext context = new YRCApiContext();
    context
      .setApiName("CreateContainersAndPrintService");
    context.setFormId(getFormId());
    context.setInputXml(shipmentElement.getOwnerDocument());
    callApi(context);

    AcademySIMTraceUtil.endMessage(CLASSNAME, "callServiceForShipmentContainerProcessing(shipmentElement)");
  }

  public boolean preCommand(YRCApiContext context)
  {
    String str1 = "preCommand(context)";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "preCommand(context)");

    if (context.getApiName().equals(
      "TranslateBarCode"))
    {
      Document barcodeInputDoc = context.getInputXml();

      Element rootElement = barcodeInputDoc.getDocumentElement();
      rootElement.setAttribute("BarCodeType", 
        "SFSItem");
      Element ctxtInfoElement = (Element)rootElement
        .getElementsByTagName(
        "ContextualInfo").item(0);
      ctxtInfoElement.setAttribute("OrganizationCode", 
        "DEFAULT");
      context.setInputXml(barcodeInputDoc);
      AcademySIMTraceUtil.logMessage("Modified Input for " + 
        context.getApiName() + " command: \n", rootElement);
    }

    AcademySIMTraceUtil.endMessage(CLASSNAME, "preCommand(context)");
    return super.preCommand(context);
  }

  public void handleApiCompletion(YRCApiContext context)
  {
    String str1 = "handleApiCompletion()";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "handleApiCompletion()");

    if (context.getInvokeAPIStatus() < 0)
    {
      AcademySIMTraceUtil.logMessage(context.getApiName() + 
        " call Failed");
      AcademySIMTraceUtil.logMessage("Error Output: \n", 
        context.getOutputXml().getDocumentElement());
    }
    else {
      if (context.getApiName().equals(
        "getItemList"))
      {
        AcademySIMTraceUtil.logMessage("Context API: " + 
          context.getApiName());
        AcademySIMTraceUtil.logMessage("getItemList Output: \n", 
          context.getOutputXml().getDocumentElement());

        Document vendorPkgDoc = 
          YRCXmlUtils.createFromString("<Item ItemID='VendorPackage'><PrimaryInformation ShortDescription='VENDOR PACKAGE'/></Item>");
        Node ndVendorPkg = context.getOutputXml().importNode(
          vendorPkgDoc.getDocumentElement(), true);
        context.getOutputXml().getDocumentElement()
          .appendChild(ndVendorPkg);

        setExtentionModel("Extn_getItemList_Output", 
          context.getOutputXml().getDocumentElement());
      }

      if (context.getApiName().equals(
        "CreateContainersAndPrintService"))
      {
        AcademySIMTraceUtil.logMessage("Context Service: " + 
          context.getApiName());
        AcademySIMTraceUtil.logMessage("Service Output: \n", 
          context.getOutputXml().getDocumentElement());

        AcademySIMTraceUtil.logMessage("calling OOB Next Action : com.yantra.sop.SOPNextRecordBackroomAction :: START");

        YRCPlatformUI.fireAction("com.yantra.sop.SOPNextRecordBackroomAction");
        AcademySIMTraceUtil.logMessage("calling OOB Next Action : com.yantra.sop.SOPNextRecordBackroomAction :: END");
      }

      if (context.getApiName().equals(
        "getCommonCodeList"))
      {
        AcademySIMTraceUtil.logMessage("Inside handle api");
        AcademySIMTraceUtil.logMessage("Context API: " + 
          context.getApiName());

        Element reasonCodeOutput = context
          .getOutputXml().getDocumentElement();
        AcademySIMTraceUtil.logMessage("Api Output: \n", 
          context.getOutputXml().getDocumentElement());
        Element eleCommonCode = (Element)reasonCodeOutput.getElementsByTagName("CommonCode").item(0);
        this.strSinglePckstnCodeValue = eleCommonCode.getAttribute("CodeValue");
        AcademySIMTraceUtil.logMessage("Single PACK Station Code Value is :" + this.strSinglePckstnCodeValue);
        String strCodeType = eleCommonCode.getAttribute("CodeType");
        if (strCodeType.equalsIgnoreCase("INV_SHRT_RSN"))
        {
          setExtentionModel("Extn_inv_shrt_rsn_Output", reasonCodeOutput);
        }
        if (strCodeType.equalsIgnoreCase("MUL_PACK_STORE"))
        {
          AcademySIMTraceUtil.logMessage("getCommonCodeList Output for MUL_PACK_STORE for Pack station:" + YRCXmlUtils.getString(reasonCodeOutput));

          NodeList mulPckStnNL = reasonCodeOutput.getElementsByTagName("CommonCode");
          Element eleMulPckStnCommonCode;
          for (int i = 0; i < mulPckStnNL.getLength(); i++)
          {
            eleMulPckStnCommonCode = (Element)mulPckStnNL.item(i);
            AcademySIMTraceUtil.logMessage("inside for loop for Pack station" + YRCXmlUtils.getString(eleMulPckStnCommonCode));
            String strCommonCodeValue = eleMulPckStnCommonCode.getAttribute("CodeValue");
            AcademySIMTraceUtil.logMessage("Multiple PACK Station Code Value is :" + strCommonCodeValue);
            String strCommonCodeShrDes = eleMulPckStnCommonCode.getAttribute("CodeShortDescription");
            AcademySIMTraceUtil.logMessage("Multiple PACK Station Description is :" + strCommonCodeShrDes);
            this.packMap.put(strCommonCodeValue, strCommonCodeShrDes);
            AcademySIMTraceUtil.logMessage("Hash MAP Value for Pack station :" + this.packMap);
          }
          for (String key : this.packMap.keySet())
          {
            String value = (String)this.packMap.get(key);
            AcademySIMTraceUtil.logMessage(key + " " + value);
          }
          String shipNodeFromInput = YRCPlatformUI.getUserElement().getAttribute("ShipNode");
          AcademySIMTraceUtil.logMessage("ShipNode Value is::" + shipNodeFromInput);
          if (this.packMap.containsKey(shipNodeFromInput))
          {
            this.strConcatShipNode = shipNodeFromInput.concat("_PACK_STN");
            AcademySIMTraceUtil.logMessage("CommonCodeType for Pack station Is::" + this.strConcatShipNode);
            getCommonCodeList(this.strConcatShipNode);
          }
          else {
            AcademySIMTraceUtil.logMessage("CommonCodeType Is::PACK_PRINTER_ID");
            getCommonCodeList("PACK_PRINTER_ID");

            hideField("extn_PrinterIdComboBox", this);
            hideField("extn_lblPrinter_Id", this);
            AcademySIMTraceUtil.logMessage("The Node User has currently logged in has Single Pack Station:" +YRCXmlUtils.getString(reasonCodeOutput));
          }

          AcademySIMTraceUtil.logMessage("concatenated string" + this.strConcatShipNode);
        }
        else if (strCodeType.equalsIgnoreCase(this.strConcatShipNode))
        {
          setExtentionModel("Extn_PackSlip_PrinterID_Output", reasonCodeOutput);
          AcademySIMTraceUtil.logMessage("getCommonCodeList Output With Multiple PAckStation" + reasonCodeOutput);
        }

        AcademySIMTraceUtil.logMessage("The Node User has currently logged in has Single Pack Station:" + YRCXmlUtils.getString(reasonCodeOutput));
      }

      if (context.getApiName().equals(
        "getItemDetailsList"))
      {
        AcademySIMTraceUtil.logMessage("Context API: " + 
          context.getApiName());
        AcademySIMTraceUtil.logMessage("getItemList Output: \n", 
          context.getOutputXml().getDocumentElement());
        setExtentionModel(
          "Extn_ItemDetail_List", 
          context.getOutputXml().getDocumentElement());

        Element eleItemList = context.getOutputXml()
          .getDocumentElement();
        Element eleShipmentDetailsModel = getModel("ShipmentDetails");

        NodeList nlItemList = eleItemList
          .getElementsByTagName("Item");
        for (int listIndex = 0; listIndex < nlItemList.getLength(); listIndex++) {
          Element eleItem = (Element)nlItemList.item(listIndex);
          String strItemID = eleItem
            .getAttribute("ItemID");
          Element eleShipmentItemDtlNode = (Element)
            YRCXPathUtils.evaluate(
            eleShipmentDetailsModel, 
            "/Shipment/ShipmentLines/ShipmentLine/OrderLine/Item[@ItemID='" + 
            strItemID + "']", 
            XPathConstants.NODE);

          if (!YRCPlatformUI.isVoid(eleShipmentItemDtlNode)) {
            NodeList nlChildNodes = eleItem.getChildNodes();
            for (int i = 0; i < nlChildNodes.getLength(); i++) {
              Node ndChildNode = eleShipmentDetailsModel
                .getOwnerDocument().importNode(
                nlChildNodes.item(i), true);
              eleShipmentItemDtlNode.appendChild(ndChildNode);
            }
          }
        }
        AcademySIMTraceUtil.logMessage("ShipmentDetails Model : \n", 
          eleShipmentDetailsModel);
      }
    }
    AcademySIMTraceUtil.endMessage(CLASSNAME, "handleApiCompletion()");
    super.handleApiCompletion(context);
  }

  public static void hideField(String fieldName, Object extnBehavior)
  {
    GridData gridData = new GridData();
    gridData.exclude = true;
    YRCBaseBehavior parentBehavior = (YRCBaseBehavior)extnBehavior;
    parentBehavior.setControlVisible(fieldName, false);
    parentBehavior.setControlLayoutData(fieldName, gridData);
  }

  protected void handleEvent(String fieldName, YRCEvent event)
  {
    String str1 = "handleEvent(fieldName, event)";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "handleEvent(fieldName, event)");

    if ((this.pageBeingShown
      .equals("com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPRecordBackroomPickScanMode")) && 
      (fieldName.equals("txtItemID")) && 
      (!isFieldBlank(getFieldValue(fieldName))) && 
      (event.detail == 16))
    {
      managePickedFieldsForScan(getModel("ShipmentDetails"));
    }

    if ((this.pageBeingShown
      .equals("com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPRecordBackroomPickScanMode")) && 
      (fieldName.equals("text2")) && 
      (!isFieldBlank(getFieldValue(fieldName))) && 
      (event.detail == 16))
    {
      managePickedQtyForItemScanned(getModel("ShipmentDetails"));
    }

    AcademySIMTraceUtil.endMessage(CLASSNAME, "handleEvent(fieldName, event)");
  }

  private void managePickedQtyForItemScanned(Element shipmentElement)
  {
    String str1 = "managePickedQtyForItemScanned(shipmentElement)";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "managePickedQtyForItemScanned(shipmentElement)");

    AcademySIMTraceUtil.logMessage("managePickedQtyForItemScanned(shipmentElement) :: shipDtlsModelSet = " + 
      this.shipDtlsModelSet + " :: BEGIN");

    NodeList shipmentLineNL = (NodeList)YRCXPathUtils.evaluate(
      shipmentElement, 
      "/Shipment/ShipmentLines/ShipmentLine", 
      XPathConstants.NODESET);

    for (int listIndex = 0; listIndex < shipmentLineNL.getLength(); listIndex++)
    {
      Element shipmentLineElement = (Element)shipmentLineNL
        .item(listIndex);

      String itemID = shipmentLineElement
        .getAttribute("ItemID");

      if (getFieldValue("txtLastItem")
        .equals(itemID)) {
        double pickedQuantityVal = 0.0D;
        String pickedQuantity = shipmentLineElement
          .getAttribute("PickedQuantity1");
        if (!isFieldBlank(pickedQuantity)) {
          pickedQuantityVal = Double.parseDouble(pickedQuantity);
        }
        double backroomPickedQtyVal = 
          Double.parseDouble(shipmentLineElement
          .getAttribute("BackroomPickedQuantity"));
        double quantityOrderedVal = 
          Double.parseDouble(shipmentLineElement
          .getAttribute("Quantity"));
        if (pickedQuantityVal > 0.0D) {
          if (!this.shipDtlsModelSet) {
            if (quantityOrderedVal >= backroomPickedQtyVal + pickedQuantityVal) {
              continue;
            }
            double correctedVal = 1.0D;
            shipmentLineElement.setAttribute(
              "PickedQuantity1", 
              String.valueOf(correctedVal));
            repopulateModel("ShipmentDetails");
          }
          else {
            this.shipDtlsModelSet = false;
          }
        }
      }
    }

    AcademySIMTraceUtil.logMessage("managePickedQtyForItemScanned(shipmentElement) :: shipDtlsModelSet = " + 
      this.shipDtlsModelSet + " :: END");
    AcademySIMTraceUtil.endMessage(CLASSNAME, "managePickedQtyForItemScanned(shipmentElement)");
  }

  private void managePickedFieldsForScan(Element shipmentElement)
  {
    String str1 = "managePickedFieldsForScan(shipmentElement)";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "managePickedFieldsForScan(shipmentElement)");

    NodeList shipmentLineNL = (NodeList)YRCXPathUtils.evaluate(
      shipmentElement, 
      "/Shipment/ShipmentLines/ShipmentLine", 
      XPathConstants.NODESET);

    for (int listIndex = 0; listIndex < shipmentLineNL.getLength(); listIndex++)
    {
      Element shipmentLineElement = (Element)shipmentLineNL
        .item(listIndex);

      double pickedQuantityVal = 0.0D;
      String pickedQuantity = shipmentLineElement
        .getAttribute("PickedQuantity1");
      if (!isFieldBlank(pickedQuantity)) {
        pickedQuantityVal = Double.parseDouble(pickedQuantity);
      }
      double backroomPickedQtyVal = 
        Double.parseDouble(shipmentLineElement
        .getAttribute("BackroomPickedQuantity"));
      double quantityOrderedVal = Double.parseDouble(shipmentLineElement
        .getAttribute("Quantity"));
      if ((pickedQuantityVal <= 0.0D) || 
        (quantityOrderedVal > backroomPickedQtyVal + pickedQuantityVal)) {
        continue;
      }
      double correctedVal = quantityOrderedVal - 
        backroomPickedQtyVal;
      shipmentLineElement.setAttribute(
        "PickedQuantity1", 
        String.valueOf(correctedVal));
      repopulateModel("ShipmentDetails");
    }

    AcademySIMTraceUtil.endMessage(CLASSNAME, "managePickedFieldsForScan(shipmentElement)");
  }

  public void postSetModel(String nameSpace)
  {
    String str1 = "postSetModel(nameSpace)";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "postSetModel(nameSpace)");

    if (nameSpace.equals("ShipmentDetailsForResolution"))
    {
      AcademySIMTraceUtil.logMessage("postSetModel(nameSpace) :: NameSpace " + 
        nameSpace + " :: BEGIN");

      Element shipmentElement = getModel(nameSpace);

      shipmentElement.setAttribute(
        "ContainerType", this.containerType);
      shipmentElement.setAttribute(
        "ContainerGrossWeight", this.weight);
      shipmentElement.setAttribute(
        "IsCompletePick", 
        "N");
      String itemKeyXpathStr = "/ItemList/Item[@ItemID='" + 
        this.containerType + 
        "']/@" + 
        "ItemKey";
      String cntrTypeKey = (String)YRCXPathUtils.evaluate(
        getModel("Extn_getItemList_Output"), 
        itemKeyXpathStr, XPathConstants.STRING);
      shipmentElement.setAttribute(
        "ContainerTypeKey", cntrTypeKey);
      AcademySIMTraceUtil.logMessage("postSetModel(" + nameSpace + 
        "): \n", shipmentElement);
      repopulateModel(nameSpace);

      AcademySIMTraceUtil.logMessage("postSetModel(nameSpace) :: NameSpace " + 
        nameSpace + " :: END");
    }

    if (nameSpace.equals("ShipmentDetails"))
    {
      AcademySIMTraceUtil.logMessage("postSetModel(nameSpace) :: NameSpace " + 
        nameSpace + " :: BEGIN");

      if (this.pageBeingShown
        .equals("com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPRecordBackroomPickScanMode"))
      {
        if (!this.shipDtlsModelSet)
          this.shipDtlsModelSet = true;
        else {
          this.shipDtlsModelSet = false;
        }

        Element eleExtnItemDetailList = getModel("Extn_ItemDetail_List");

        if (YRCPlatformUI.isVoid(eleExtnItemDetailList)) {
          Element eleShipmentDtls = getModel(nameSpace);

          AcademySIMTraceUtil.logMessage("ShipmentDetailModel", 
            eleShipmentDtls);

          NodeList nlItemList = eleShipmentDtls
            .getElementsByTagName("Item");

          callApiForItemDetails(nlItemList);
        }

      }

      AcademySIMTraceUtil.logMessage("postSetModel(nameSpace) :: NameSpace " + 
        nameSpace + " :: END");
    }

    AcademySIMTraceUtil.endMessage(CLASSNAME, "postSetModel(nameSpace)");
    super.postSetModel(nameSpace);
  }

  public YRCValidationResponse validateTextField(String fieldName, String fieldValue)
  {
    String str1 = "validateTextField(fieldName,fieldValue)";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "validateTextField(fieldName,fieldValue)");
    YRCValidationResponse response = super.validateButtonClick(fieldName);

    if ((fieldName.equals("extn_txtWeight")) || 
      (fieldName.equals("extn_txtWeight_scan")))
    {
      AcademySIMTraceUtil.logMessage("fieldName : " + fieldName);
      if (!isFieldBlank(fieldValue))
      {
        if (!fieldValue
          .matches("^(\\+)?[0-9]*(\\.[0-9]+)?$"))
        {
          String errorMessage = 
            YRCPlatformUI.getString("CONTAINER_WEIGHT_VAL_ERR_MSG_KEY");
          response = showError(errorMessage);
          setFocus(fieldName);
          setFieldValue(fieldName, "");
        }
      }
    }
    AcademySIMTraceUtil.endMessage(CLASSNAME, "validateTextField(fieldName,fieldValue)");
    return response;
  }

  private YRCValidationResponse showError(String errorMessage)
  {
    YRCFormatResponse response = new YRCFormatResponse(
      3, errorMessage, null);
    YRCPlatformUI.showError("Error", errorMessage);
    return response;
  }

  public YRCValidationResponse validateButtonClick(String fieldName)
  {
    String str1 = "validateButtonClick(fieldName)";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "validateButtonClick(fieldName)");

    YRCValidationResponse response = super.validateButtonClick(fieldName);

    if (fieldName.equals("extn_btnPickNext"))
    {
      AcademySIMTraceUtil.logMessage("validateButtonClick(fieldName) called for : " + 
        fieldName);
      if ((doProceedAction()) && 
        (!this.completePickDone))
      {
        AcademySIMTraceUtil.logMessage("calling OOB Next Action : com.yantra.sop.SOPNextRecordBackroomAction :: START");

        YRCPlatformUI.fireAction("com.yantra.sop.SOPNextRecordBackroomAction");
        AcademySIMTraceUtil.logMessage("calling OOB Next Action : com.yantra.sop.SOPNextRecordBackroomAction :: END");
      }

    }

    if (fieldName.equals("extn_btnPickConfirm"))
    {
      AcademySIMTraceUtil.logMessage("validateButtonClick(fieldName) called for : " + 
        fieldName);
      doConfirmAction();
    }

    AcademySIMTraceUtil.endMessage(CLASSNAME, "validateButtonClick(fieldName)");
    return response;
  }

  public void doConfirmAction()
  {
    String str1 = "doConfirmAction()";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "doConfirmAction()");

    Element shipmentElement = getModel("ShipmentDetailsForResolution");
    String strRadioSeltd = getTargetModel(
      "SelectedRadioButton").getAttribute(
      "radioSelection");

    shipmentElement.setAttribute("radioSelection", 
      strRadioSeltd);

    AcademySIMTraceUtil.logMessage(
      "validateButtonClick(extn_btnPickConfirm): doConfirmAction(): \n", 
      shipmentElement);

    if (strRadioSeltd
      .equals("InventoryShortage"))
    {
      AcademySIMTraceUtil.logMessage("validateButtonClick(extn_btnPickConfirm): AcademyShortageResolutionConfirmPopup called :: BEGIN");
      AcademyShortageResolutionConfirmPopup resoPopup = new AcademyShortageResolutionConfirmPopup(
        new Shell(Display.getDefault()), 0);
      YRCDialog popupDialog = new YRCDialog(resoPopup, 300, 120, 
        "Confirm Shortage Resolution", null);
      if (!YRCDialog.isDialogOpen())
      {
        popupDialog.open();
      }

      AcademySIMTraceUtil.logMessage("validateButtonClick(extn_btnPickConfirm): AcademyShortageResolutionConfirmPopup called :: END");

      if (resoPopup.getSelectionConfirm())
      {
        AcademySIMTraceUtil.logMessage("validateButtonClick(extn_btnPickConfirm): doConfirmAction(): Service called");
        shipmentElement.setAttribute("PrintPackId", strCommonComboValue);
        AcademySIMTraceUtil.logMessage("Printer Id selected in previous screen for inventoruy shortage is"+strCommonComboValue);
        callServiceForShipmentContainerProcessing(shipmentElement);
      }

    }
    else if(strRadioSeltd
  	      .equals("PickLater"))
  {
  		AcademySIMTraceUtil.logMessage("validateButtonClick(extn_btnPickConfirm): doConfirmAction(): Service called for Will Pick Later");
  	   shipmentElement.setAttribute("PrintPackId", strCommonComboValue);
  	   AcademySIMTraceUtil.logMessage("Printer Id selected in previous screen for will pick later is"+strCommonComboValue);
  	   callServiceForShipmentContainerProcessing(shipmentElement);
  }
    AcademySIMTraceUtil.endMessage(CLASSNAME, "doConfirmAction()");
  }

  public boolean doProceedAction()
  {
    String str1 = "doProceedAction()";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "doProceedAction()");
    boolean proceedToNext = true;

    this.noPickDone = false;
    this.completePickDone = false;

    if (this.pageBeingShown
      .equals("com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPOBERecordBackroomPickShipOut"))
    {
      this.weight = getFieldValue("extn_txtWeight");
      this.containerType = getFieldValue("extn_cmbContainerType");
    }
    else if (this.pageBeingShown
      .equals("com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPRecordBackroomPickScanMode")) {
      this.weight = getFieldValue("extn_txtWeight_scan");
      this.containerType = getFieldValue("extn_cmbContainerType_scan");
    }

    AcademySIMTraceUtil.logMessage("Container Weight: " + this.weight);
    AcademySIMTraceUtil.logMessage("Container Type: " + this.containerType);

    Element shipmentElement = getModel("ShipmentDetails");
    NodeList shipmentLineNL = (NodeList)YRCXPathUtils.evaluate(
      shipmentElement, 
      "/Shipment/ShipmentLines/ShipmentLine", 
      XPathConstants.NODESET);

    if ("VendorPackage".equalsIgnoreCase(this.containerType)) {
      this.weight = "0";
    }

    if ((!isFieldBlank(this.weight)) && (!isFieldBlank(this.containerType)))
    {
      if (isAnyPickedQuantityEntered(shipmentLineNL))
      {
        proceedToNext = true;
      }
      else proceedToNext = false;

      if (("VendorPackage"
        .equalsIgnoreCase(this.containerType)) && 
        (proceedToNext)) {
        for (int i = 0; i < shipmentLineNL.getLength(); i++) {
          Element eleShipmentLine = (Element)shipmentLineNL
            .item(i);

          String pickedQuantity = eleShipmentLine
            .getAttribute("PickedQuantity1");
          if (isFieldBlank(pickedQuantity)) {
            continue;
          }
          if (pickedQuantity
            .equals("0.0")) {
            continue;
          }
          Element eleItem = (Element)YRCXPathUtils.evaluate(
            eleShipmentLine, "OrderLine/Item", 
            XPathConstants.NODE);
          String strItemType = getItemType(eleItem);
          if ("CON".equalsIgnoreCase(strItemType)) {
            String errorMessage = 
              YRCPlatformUI.getString("VENDOR_PKG_CON_PICK_ERROR_MSG_KEY");
            showError(errorMessage);
            proceedToNext = false;
            break;
          }
          if (!"BULK".equalsIgnoreCase(strItemType))
          {
            continue;
          }
          if (Double.valueOf(pickedQuantity)
            .intValue() > 1) {
            String errorMessage = 
              YRCPlatformUI.getString("VENDOR_PKG_MULTI_QTY_BLK_PICK_ERROR_MSG_KEY");
            showError(errorMessage);
            proceedToNext = false;
            break;
          }

        }

      }

      if ((validatePickForCall(shipmentLineNL)) && 
        (proceedToNext == Boolean.TRUE.booleanValue()))
      {
        AcademySIMTraceUtil.logMessage("Printer_Id selected for Pack station is " + getFieldValue("extn_PrinterIdComboBox"));
        String strComboboxValue = getFieldValue("extn_PrinterIdComboBox");
        if (strComboboxValue != null)
        {
          shipmentElement.setAttribute("PrintPackId", strComboboxValue);
          AcademySIMTraceUtil.logMessage("Printer_Id selected for Pack station is " + strComboboxValue);
        }
        else {
          shipmentElement.setAttribute("PrintPackId", this.strSinglePckstnCodeValue);
          AcademySIMTraceUtil.logMessage("Printer_Id selected is for Pack station " + this.strSinglePckstnCodeValue);
        }

        shipmentElement.setAttribute(
          "ContainerType", this.containerType);
        shipmentElement.setAttribute(
          "ContainerGrossWeight", this.weight);
        shipmentElement.setAttribute(
          "IsCompletePick", 
          "Y");
        String itemKeyXpathStr = "/ItemList/Item[@ItemID='" + 
          this.containerType + 
          "']/@" + 
          "ItemKey";
        String cntrTypeKey = (String)YRCXPathUtils.evaluate(
          getModel("Extn_getItemList_Output"), 
          itemKeyXpathStr, XPathConstants.STRING);
        shipmentElement.setAttribute(
          "ContainerTypeKey", cntrTypeKey);

        AcademySIMTraceUtil.logMessage("validateButtonClick(extn_btnPickNext): doProceedAction(): Service called");
        this.completePickDone = true;

        callServiceForShipmentContainerProcessing(shipmentElement);
      }
      if ((!validatePickForCall(shipmentLineNL)) && 
  	        (proceedToNext == Boolean.TRUE.booleanValue()))
    {
  	  AcademySIMTraceUtil.logMessage("Going in to the If condition of Will pick later / Inv shortage");
  	  try{
  	  strComboboxValue = getFieldValue("extn_PrinterIdComboBox");
  	  }catch(Exception e){
  		  e.printStackTrace();
  	  }
  	  AcademySIMTraceUtil.logMessage("Get Feild Value of Pack station is:"+strComboboxValue);
		if(strComboboxValue!=null)
		{
			strCommonComboValue = strComboboxValue;
			AcademySIMTraceUtil.logMessage("Get Feild Value of Pack station is:"+strCommonComboValue);
		
		}
		else{
			strCommonComboValue = strSinglePckstnCodeValue;
			AcademySIMTraceUtil.logMessage("Get Feild Value of Pack station is:"+strCommonComboValue);
		}
		
		AcademySIMTraceUtil.logMessage("Printer_Id selected is for Pack station "+strCommonComboValue);
    }
    }
    else if (isAnyPickedQuantityEntered(shipmentLineNL))
    {
      if (this.noPickDone)
      {
        proceedToNext = true;
      }
      else
      {
        String errorMessage = 
          YRCPlatformUI.getString("CONTAINER_WEIGHT_TYPE_ERR_MSG_KEY");
        showError(errorMessage);
        proceedToNext = false;
      }
    }
    else
    {
      String errorMessage = 
        YRCPlatformUI.getString("CONTAINER_WEIGHT_TYPE_ERR_MSG_KEY");
      showError(errorMessage);
      proceedToNext = false;
    }

    AcademySIMTraceUtil.logMessage(
      "validateButtonClick(extn_btnPickNext): doProceedAction(): \n", 
      shipmentElement);
    AcademySIMTraceUtil.endMessage(CLASSNAME, "doProceedAction()");
    AcademySIMTraceUtil.logMessage("doProceedAction() : " + proceedToNext + 
      " returned");
    return proceedToNext;
  }

  private String getItemType(Element eleItem) {
    String strItemType = null;
    Element eleClassificationCode = (Element)eleItem
      .getElementsByTagName("ClassificationCodes")
      .item(0);
    Element eleExtn = (Element)eleItem
      .getElementsByTagName("Extn").item(0);
    String strStorageType = eleClassificationCode
      .getAttribute("StorageType");
    String strExtnWhiteGloveEligible = eleExtn
      .getAttribute("ExtnWhiteGloveEligible");
    if (strStorageType.startsWith("CON")) {
      strItemType = "CON";
    }
    if ((strStorageType.startsWith("NCON")) && 
      ("N".equalsIgnoreCase(strExtnWhiteGloveEligible))) {
      strItemType = "BULK";
    }
    return strItemType;
  }

  private boolean isPickedUOMEntered(Element shipmentLineElement)
  {
    String str1 = "isPickedUOMEntered(shipmentLineElement)";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "isPickedUOMEntered(shipmentLineElement)");

    String pickedUOM = shipmentLineElement
      .getAttribute("PickedUOM1");
    if (isFieldBlank(pickedUOM))
    {
      AcademySIMTraceUtil.endMessage(CLASSNAME, "isPickedUOMEntered(shipmentLineElement)");
      AcademySIMTraceUtil.logMessage("isPickedUOMEntered(shipmentLineElement) : False returned");
      return false;
    }
    AcademySIMTraceUtil.endMessage(CLASSNAME, "isPickedUOMEntered(shipmentLineElement)");
    AcademySIMTraceUtil.logMessage("isPickedUOMEntered(shipmentLineElement) : True returned");
    return true;
  }

  private boolean isAnyPickedQuantityEntered(NodeList shipmentLineNL)
  {
    String str1 = "isAnyPickedQuantityEntered(shipmentLineNL)";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "isAnyPickedQuantityEntered(shipmentLineNL)");

    boolean pickCheck = false;
    boolean uomCheck = false;

    for (int listIndex = 0; listIndex < shipmentLineNL.getLength(); listIndex++)
    {
      Element shipmentLineElement = (Element)shipmentLineNL
        .item(listIndex);
      String pickedQuantity = shipmentLineElement
        .getAttribute("PickedQuantity1");
      if (isFieldBlank(pickedQuantity)) {
        continue;
      }
      if (pickedQuantity
        .equals("0.0"))
        continue;
      if ((!uomCheck) && (!isPickedUOMEntered(shipmentLineElement)))
      {
        String errorMessage = 
          YRCPlatformUI.getString("UOM_NOT_PICKED_ERR_MSG_KEY");
        showError(errorMessage);
        uomCheck = true;
      }
      pickCheck = true;
    }

    AcademySIMTraceUtil.endMessage(CLASSNAME, "isAnyPickedQuantityEntered(shipmentLineNL)");
    if (uomCheck) {
      AcademySIMTraceUtil.logMessage("isAnyPickedQuantityEntered(shipmentLineNL) : " + (!uomCheck) + 
        " returned");
      return !uomCheck;
    }if ((pickCheck == uomCheck) && (pickCheck == Boolean.FALSE.booleanValue())) {
      AcademySIMTraceUtil.logMessage("isAnyPickedQuantityEntered(shipmentLineNL) : " + (!pickCheck) + 
        " returned");
      this.noPickDone = (!pickCheck);
      return !pickCheck;
    }
    AcademySIMTraceUtil.logMessage("isAnyPickedQuantityEntered(shipmentLineNL) : " + pickCheck + 
      " returned");
    return pickCheck;
  }

  private boolean validatePickForCall(NodeList shipmentLineNL)
  {
    String str1 = "validatePickForCall(shipmentLineNL)";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "validatePickForCall(shipmentLineNL)");

    boolean validatePick = false;

    for (int listIndex = 0; listIndex < shipmentLineNL.getLength(); listIndex++)
    {
      Element shipmentLineElement = (Element)shipmentLineNL
        .item(listIndex);

      double pickedQuantityVal = 0.0D;
      String pickedQuantity = shipmentLineElement
        .getAttribute("PickedQuantity1");
      if (!isFieldBlank(pickedQuantity)) {
        pickedQuantityVal = Double.parseDouble(pickedQuantity);
      }
      double backroomPickedQtyVal = 
        Double.parseDouble(shipmentLineElement
        .getAttribute("BackroomPickedQuantity"));
      double quantityOrderedVal = Double.parseDouble(shipmentLineElement
        .getAttribute("Quantity"));

      if (quantityOrderedVal == backroomPickedQtyVal + pickedQuantityVal)
      {
        AcademySIMTraceUtil.logMessage("[" + listIndex + 
          "] pickedQuantityVal: " + pickedQuantityVal + 
          " backroomPickedQtyVal " + backroomPickedQtyVal + 
          " quantityOrderedVal " + quantityOrderedVal);

        validatePick = true;
      }
      else
      {
        validatePick = false;
        break;
      }
    }
    AcademySIMTraceUtil.endMessage(CLASSNAME, "validatePickForCall(shipmentLineNL)");
    AcademySIMTraceUtil.logMessage("validatePickForCall(shipmentLineNL) : " + validatePick + 
      " returned");
    return validatePick;
  }

  private boolean isFieldBlank(String fieldValue)
  {
    return (fieldValue == null) || (fieldValue.trim().length() == 0);
  }

  public YRCValidationResponse validateLinkClick(String fieldName)
  {
    return super.validateLinkClick(fieldName);
  }

  public void validateComboField(String fieldName, String fieldValue)
  {
    if ("extn_cmbContainerType_scan".equalsIgnoreCase(fieldName))
    {
      if ("VendorPackage"
        .equalsIgnoreCase(fieldValue)) {
        setFieldValue("extn_txtWeight_scan", "");
        disableField("extn_txtWeight_scan");
      } else {
        enableField("extn_txtWeight_scan");
      }
    }
    super.validateComboField(fieldName, fieldValue);
  }

  public YRCExtendedTableBindingData getExtendedTableBindingData(String tableName, ArrayList tableColumnNames)
  {
    return super.getExtendedTableBindingData(tableName, tableColumnNames);
  }

  private Document prepareGetItemListInput(NodeList nlItemList) {
    AcademySIMTraceUtil.startMessage(CLASSNAME, 
      "prepareGetItemListInput(nlItemList)");

    Document getItemListInputDoc = 
      YRCXmlUtils.createDocument("Item");
    Element rootElement = getItemListInputDoc.getDocumentElement();

    if (nlItemList.getLength() == 1)
    {
      Element eleItem = (Element)nlItemList.item(0);
      rootElement.setAttribute("ItemID", 
        eleItem.getAttribute("ItemID"));
    }
    else {
      Element complexQryElement = getItemListInputDoc
        .createElement("ComplexQuery");
      complexQryElement.setAttribute(
        "Operator", 
        "AND");
      Element complexAndElement = getItemListInputDoc
        .createElement("And");
      Element complexOrElement = getItemListInputDoc
        .createElement("Or");

      for (int listIndex = 0; listIndex < nlItemList.getLength(); listIndex++) {
        Element eleItem = (Element)nlItemList.item(listIndex);
        Element expElement = getItemListInputDoc
          .createElement("Exp");
        expElement.setAttribute("Name", 
          "ItemID");
        expElement.setAttribute("Value", 
          eleItem.getAttribute("ItemID"));
        complexOrElement.appendChild(expElement);
      }

      complexAndElement.appendChild(complexOrElement);
      complexQryElement.appendChild(complexAndElement);
      rootElement.appendChild(complexQryElement);
    }

    AcademySIMTraceUtil.logMessage("prepareGetItemListInput", 
      getItemListInputDoc);

    AcademySIMTraceUtil.endMessage(CLASSNAME, 
      "prepareGetItemListInput(nlItemList)");
    return getItemListInputDoc;
  }

  private void callApiForItemDetails(NodeList nlItemList)
  {
    String str = "callApiForItemDetails()";
    AcademySIMTraceUtil.startMessage(CLASSNAME, "callApiForItemDetails()");

    YRCApiContext context = new YRCApiContext();
    context.setApiName("getItemDetailsList");
    context.setFormId(getFormId());
    context.setInputXml(prepareGetItemListInput(nlItemList));
    callApi(context);

    AcademySIMTraceUtil.endMessage(CLASSNAME, "callApiForItemDetails()");
  }
}