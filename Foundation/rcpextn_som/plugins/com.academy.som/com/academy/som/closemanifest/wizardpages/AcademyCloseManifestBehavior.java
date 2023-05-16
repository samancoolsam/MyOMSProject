package com.academy.som.closemanifest.wizardpages;

import javax.xml.xpath.XPathConstants;

import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.som.util.AcademyPCAConstants;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCWizardPageBehavior;
import com.yantra.yfc.rcp.YRCXPathUtils;
import com.yantra.yfc.rcp.YRCXmlUtils;

public class AcademyCloseManifestBehavior 
extends YRCWizardPageBehavior
{
  public static final String OPEN_MANIFEST_LIST_MODEL = "OpenManifestList";
  public static final String CLOSE_MANIFEST_TARGET = "SelectedManifestForCloseOperation";
  private String formId;
  private AcademyCloseManifest ownerForm;
  
  public AcademyCloseManifestBehavior(Composite ownerComposite, String formId)
  {
    super(ownerComposite);
    this.formId = formId;
    this.ownerForm = ((AcademyCloseManifest)ownerComposite);
  }
  
  public void initPage()
  {
    super.initPage();
    pageAboutToBeShown();
  }
  
  public void pageAboutToBeShown()
  {
    super.pageAboutToBeShown();
    getOpenManifestList();
  }
  
  private void getOpenManifestList()
  {
    Element manifestInputElem = getManifestListInput();
    
    YRCApiContext ctx = new YRCApiContext();
    ctx.setApiName("GetManifestList");
    ctx.setInputXml(manifestInputElem.getOwnerDocument());
    ctx.setFormId("com.academy.som.closemanifest.wizards.AcademyCloseManifestWizard");
    callApi(ctx);
  }
  
  private Element getManifestListInput()
  {
    //String manifestInputStr = "<Manifest ShipNode=\"" + SOPApplicationContext.getInstance().getStoreNo() + "\" ManifestClosedFlag=\"N\"><Shipments><Shipment Status=\"" + "1400" + "\" StatusQryType=\"LE\" /></Shipments></Manifest>";
	String manifestInputStr = "<Manifest ShipNode=\"" + YRCPlatformUI.getUserElement().getAttribute(AcademyPCAConstants.SHIPNODE_ATTR) + "\" ManifestClosedFlag=\"N\"><Shipments><Shipment Status=\"" + "1400" + "\" StatusQryType=\"LE\" /></Shipments></Manifest>";
    Document manifestInputDoc = YRCXmlUtils.createFromString(manifestInputStr);
    return manifestInputDoc.getDocumentElement();
  }
  
  public void handleApiCompletion(YRCApiContext ctx)
  {
    Document outDoc = null;
    if ((ctx.getInvokeAPIStatus() >= 0) && 
      ("GetManifestList".equals(ctx.getApiName())))
    {
      outDoc = ctx.getOutputXml();
      processManifestListOutput(outDoc);
    }
  }
  
  private void processManifestListOutput(Document outDoc)
  {
    Element manifestListElem = outDoc.getDocumentElement();
    setModel("OpenManifestList", manifestListElem);
    

    displayRadiosForOpenManifests(manifestListElem);
  }
  
  private void displayRadiosForOpenManifests(Element manifestListElem)
  {
    int totalNumberOfManifests = YRCXmlUtils.getIntAttribute(manifestListElem, "TotalNumberOfRecords");
    
    NodeList manifestList = (NodeList)YRCXPathUtils.evaluate(manifestListElem, "/Manifests/Manifest", XPathConstants.NODESET);
    String[] radioDisplayTexts = new String[manifestList.getLength()];
    String[] manifestKeys = new String[manifestList.getLength()];
    for (int manifestCount = 0; manifestCount < manifestList.getLength(); manifestCount++)
    {
      Element manifestElem = (Element)manifestList.item(manifestCount);
      Element manifestShipmentsElem = YRCXmlUtils.getXPathElement(manifestElem, "/Manifest/Shipments");
      
      String scac = manifestElem.getAttribute("Scac");
      String manifestNo = manifestElem.getAttribute("ManifestNo");
      String manifestKey = manifestElem.getAttribute("ManifestKey");
      int noOfShipments = YRCXmlUtils.getIntAttribute(manifestShipmentsElem, "TotalNumberOfRecords");
      

      String radioDisplayText = scac + " Manifest# " + manifestNo + " with " + noOfShipments + " shipments.";
      radioDisplayTexts[manifestCount] = radioDisplayText;
      manifestKeys[manifestCount] = manifestKey;
    }
    this.ownerForm.createRadioButtonsForManifests(radioDisplayTexts, manifestKeys);
    if (totalNumberOfManifests == 0) {
      this.ownerForm.btnClose.setEnabled(false);
    }
  }
  
  public void closeManifest()
  {
    showNextPage();
  }
}
