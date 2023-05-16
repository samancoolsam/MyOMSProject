
scDefine([
	"scbase/loader!dojo/_base/lang",	
	"scbase/loader!wsc",
	"scbase/loader!dojo/_base/array",
	"scbase/loader!sc/plat/dojo/utils/BaseUtils",
	"scbase/loader!sc/plat/dojo/utils/BundleUtils",
	"scbase/loader!sc/plat/dojo/utils/ScreenUtils",
    "scbase/loader!ias/utils/UIUtils",
	"scbase/loader!ias/utils/ContextUtils",
	"scbase/loader!ias/utils/ScreenUtils",
	"scbase/loader!ias/utils/WizardUtils",
	"scbase/loader!sc/plat/dojo/utils/WidgetUtils",
	"scbase/loader!sc/plat/dojo/utils/Util",
	"scbase/loader!sc/plat/dojo/utils/EventUtils",
	"scbase/loader!sc/plat/dojo/Userprefs",
	"scbase/loader!ias/utils/BaseTemplateUtils",
	"scbase/loader!sc/plat/dojo/utils/ModelUtils",
	"scbase/loader!sc/plat/dojo/utils/EditorUtils",
	"scbase/loader!sc/plat/dojo/utils/WizardUtils",
	"scbase/loader!sc/plat/dojo/utils/RepeatingPanelUtils",	
	"scbase/loader!wsc/common/widgets/LabelSetWidget",
	"scbase/loader!sc/plat/dojo/utils/ControllerUtils",
	"scbase/loader!ias/utils/UOMUtils",
	"scbase/loader!wsc/components/common/utils/CommonUtils",
	"scbase/loader!sc/plat/dojo/utils/ResourcePermissionUtils",
	"scbase/loader!dojox/html/entities"
	],
	function(dLang,wsc,dArray,scBaseUtils,scBundleUtils,scScreenUtils,iasUIUtils,iasContextUtils,iasScreenUtils,iasWizardUtils,scWidgetUtils,scUtil,scEventUtils,scUserprefs,iasBaseTemplateUtils,scModelUtils,scEditorUtils,scWizardUtils,scRepeatingPanelUtils,wscLabelSetWidget,scControllerUtils,iasUOMUtils,wscCommonUtils,scResourcePermissionUtils,dHtmlEntities){
		var util = dLang.getObject("extn.components.shipment.common.utils.ShipmentUtilsExtn", true, wsc);
		util.showNextTask = function(screen, shipmentModel, shipNodePath, startBRPLink,contBRPLink,pickTicketLink,startPackLink,contPackLink,custPickLink) {
    	var status = scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentModel);
        var deliveryMethod = scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", shipmentModel);
        var shipNode = scModelUtils.getStringValueFromPath(shipNodePath, shipmentModel);
       /* if(scBaseUtils.isVoid(shipNode))
        	shipNode = scModelUtils.getStringValueFromPath("Shipment.ShipNode.ShipNode", shipmentModel);*/
        var currentStore = iasContextUtils.getFromContext("CurrentStore");
        var allowedTransactions = scModelUtils.getModelListFromPath("Shipment.AllowedTransactions.Transaction",shipmentModel)
        var allowedTransactionList = [];
        for(i=0;i<allowedTransactions.length;i++){
        	allowedTransactionList.push(scModelUtils.getStringValueFromPath("Tranid",allowedTransactions[i]));
        }
        var brpPermission = null;
        var printPermission = null;
        if (scBaseUtils.equals(deliveryMethod, "SHP")) {
            brpPermission = "WSC000017";
            printPermission = "WSC000021";
        } else {
            brpPermission = "WSC000006";
            printPermission = "WSC000009";
        }
        
        if(scBaseUtils.equals(currentStore, shipNode)){
        	if((dArray.indexOf(allowedTransactionList,"YCD_BACKROOM_PICK_IN_PROGRESS.0006") > -1) || dArray.indexOf(allowedTransactionList,"YCD_BACKROOM_PICK_IN_PROGRESS") > -1){
             	if(scResourcePermissionUtils.hasPermission(brpPermission))
             		scWidgetUtils.showWidget(screen, startBRPLink, true, null);
             	if(scResourcePermissionUtils.hasPermission(printPermission))
             		scWidgetUtils.showWidget(screen, pickTicketLink, true, null);
             }
			 else if((dArray.indexOf(allowedTransactionList,"YCD_BACKROOM_PICK.0006") > -1 || dArray.indexOf(allowedTransactionList,"YCD_BACKROOM_PICK") > -1) && scResourcePermissionUtils.hasPermission(brpPermission)){
             	scWidgetUtils.showWidget(screen, contBRPLink, true, null);
             }
			 else if(((dArray.indexOf(allowedTransactionList,"YCD_UNDO_BACKROOM_PICK.0006") > -1 || dArray.indexOf(allowedTransactionList,"YCD_UNDO_BACKROOM_PICK") > -1) || (dArray.indexOf(allowedTransactionList,"PACK_SHIPMENT_COMPLETE.0006") > -1 || dArray.indexOf(allowedTransactionList,"PACK_SHIPMENT_COMPLETE") > -1)) && 
			   !scBaseUtils.equals(status,"1100.70.06.70")) {
             	scWidgetUtils.showWidget(screen, startPackLink, true, null);
			 }
			else if((dArray.indexOf(allowedTransactionList,"PACK_SHIPMENT_COMPLETE.0006") > -1 || dArray.indexOf(allowedTransactionList,"PACK_SHIPMENT_COMPLETE") > -1) && (dArray.indexOf(allowedTransactionList,"UNDO_PACK_SHMT_COMPLETE.0006") > -1 || dArray.indexOf(allowedTransactionList,"UNDO_PACK_SHMT_COMPLETE") > -1)){
				scWidgetUtils.showWidget(screen, contPackLink, true, null);
			} else if((dArray.indexOf(allowedTransactionList,"CONFIRM_SHIPMENT.0006") > -1 || dArray.indexOf(allowedTransactionList,"CONFIRM_SHIPMENT") > -1) && scBaseUtils.equals(deliveryMethod, "PICK")) {
				scWidgetUtils.showWidget(screen, custPickLink, true, null);
			}
		}
    }
	return util;
});