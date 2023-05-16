echo "Starting ACADEMY_FINANCIAL_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_FINANCIAL_AGENT_SERVER "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_PUBLISH_INVOICE_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PUBLISH_INVOICE_AGENT_SERVER "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_PUBLISH_INVOICE_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PUBLISH_INVOICE_AGENT_SERVER "-Xms1024m -Xmx1024m" 2 > /dev/null &
sleep 3

#echo "Starting ACADEMY_PUBLISH_INVOICE_AGENT_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PUBLISH_INVOICE_AGENT_SERVER "-Xms1024m -Xmx1024m" 3 > /dev/null &
#sleep 3

#echo "Starting ACADEMY_PUBLISH_INVOICE_AGENT_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PUBLISH_INVOICE_AGENT_SERVER "-Xms1024m -Xmx1024m" 4 > /dev/null &
#sleep 3

#echo "Starting ACADEMY_PUBLISH_INVOICE_AGENT_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PUBLISH_INVOICE_AGENT_SERVER "-Xms1024m -Xmx1024m" 5 > /dev/null &
#sleep 3

#echo "Starting ACADEMY_PUBLISH_INVOICE_AGENT_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PUBLISH_INVOICE_AGENT_SERVER "-Xms1024m -Xmx1024m" 6 > /dev/null &
#sleep 3

#echo "Starting ACADEMY_PUBLISH_INVOICE_AGENT_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PUBLISH_INVOICE_AGENT_SERVER "-Xms1024m -Xmx1024m" 7 > /dev/null &
#sleep 3

#echo "Starting ACADEMY_PUBLISH_INVOICE_AGENT_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PUBLISH_INVOICE_AGENT_SERVER "-Xms1024m -Xmx1024m" 8 > /dev/null &
#sleep 3

#echo "Starting ACADEMY_PUBLISH_INVOICE_AGENT_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PUBLISH_INVOICE_AGENT_SERVER "-Xms1024m -Xmx1024m" 9 > /dev/null &
#sleep 3

echo "Starting ACADEMY_DSV_EMAIL_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_DSV_EMAIL_SERVER "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_DSV_AGENT_SERVER1 ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_DSV_AGENT_SERVER1 "-Xms2048m -Xmx2048m" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_SHIPMENT_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_SHIPMENT_AGENT_SERVER "-Xms4096m -Xmx4096m" 1 > /dev/null &
sleep 3

echo "Starting AcademyConfirmShipmentServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyConfirmShipmentServer "-Xms2048m -Xmx2048m" 1 > /dev/null &
sleep 3

#echo "Starting AcademyConfirmShipmentServer ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyConfirmShipmentServer "-Xms2048m -Xmx2048m" 2 > /dev/null &
#sleep 3

echo "Starting ACADEMY_OMS_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_OMS_AGENT_SERVER "-Xms1024m -Xmx2048m" 1 > /dev/null &
sleep 3

echo "Starting ACAD_FIN_AGENT_SERVER_1 ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACAD_FIN_AGENT_SERVER_1 "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting ACAD_FIN_AGENT_SERVER_1 ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACAD_FIN_AGENT_SERVER_1 "-Xms1024m -Xmx1024m" 2 > /dev/null &
sleep 3

echo "Starting ACAD_FIN_AGENT_SERVER_1 ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACAD_FIN_AGENT_SERVER_1 "-Xms1024m -Xmx1024m" 3 > /dev/null &
sleep 3

echo "Starting ACAD_FIN_AGENT_SERVER_1 ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACAD_FIN_AGENT_SERVER_1 "-Xms1024m -Xmx1024m" 4 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_COLLECTION ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_COLLECTION "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_COLLECTION ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_COLLECTION "-Xms1024m -Xmx1024m" 2 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_COLLECTION ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_COLLECTION "-Xms1024m -Xmx1024m" 3 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_COLLECTION ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_COLLECTION "-Xms1024m -Xmx1024m" 4 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_COLLECTION ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_COLLECTION "-Xms1024m -Xmx1024m" 5 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_COLLECTION ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_COLLECTION "-Xms1024m -Xmx1024m" 6 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_EXECUTION_CHARGE_AGENT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_EXECUTION_CHARGE_AGENT "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_EXECUTION_CHARGE_AGENT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_EXECUTION_CHARGE_AGENT "-Xms1024m -Xmx1024m" 2 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_EXECUTION_CHARGE_AGENT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_EXECUTION_CHARGE_AGENT "-Xms1024m -Xmx1024m" 3 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_EXECUTION_CHARGE_AGENT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_EXECUTION_CHARGE_AGENT "-Xms1024m -Xmx1024m" 4 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_EXECUTION_CHARGE_AGENT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_EXECUTION_CHARGE_AGENT "-Xms1024m -Xmx1024m" 5 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_EXECUTION_CHARGE_AGENT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_EXECUTION_CHARGE_AGENT "-Xms1024m -Xmx1024m" 6 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_EXECUTION_CHARGE_AGENT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_EXECUTION_CHARGE_AGENT "-Xms1024m -Xmx1024m" 7 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_EXECUTION_CHARGE_AGENT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_EXECUTION_CHARGE_AGENT "-Xms1024m -Xmx1024m" 8 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_EXECUTION_AUTH_AGENT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_EXECUTION_AUTH_AGENT "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_EXECUTION_AUTH_AGENT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_EXECUTION_AUTH_AGENT "-Xms1024m -Xmx1024m" 2 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_EXECUTION_AUTH_AGENT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_EXECUTION_AUTH_AGENT "-Xms1024m -Xmx1024m" 3 > /dev/null &
sleep 3

echo "Starting ACADEMY_PAYMENT_EXECUTION_AUTH_AGENT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_PAYMENT_EXECUTION_AUTH_AGENT "-Xms1024m -Xmx1024m" 4 > /dev/null &
sleep 3

echo "Starting ACADEMY_OMS_AGENT_SERVER1 ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_OMS_AGENT_SERVER1 "-Xms2048m -Xmx2048m" 1 > /dev/null &
sleep 3

echo "Starting AcademyDSVPOShipment ..."
#STL-1732 - change from 1024 to 2048
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyDSVPOShipment "-Xms2048m -Xmx2048m" 1 > /dev/null &
sleep 3

echo "Starting AcademyDSVPOAckInterface ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyDSVPOAckInterface "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademyCreateOrderInterfaceServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyCreateOrderInterfaceServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademyDSVIPPOUpdateServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyDSVIPPOUpdateServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademyRedInterfaceResponseServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyRedInterfaceResponseServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademyInventorySyncFromWMS ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyInventorySyncFromWMS "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademyReceiveReturnIntegServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyReceiveReturnIntegServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademyUploadQVDServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyUploadQVDServer  "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_INVENTORY_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_INVENTORY_AGENT_SERVER "-Xms2048m -Xmx2048m -Xgcpolicy:gencon" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_INVENTORY_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_INVENTORY_AGENT_SERVER "-Xms2048m -Xmx2048m -Xgcpolicy:gencon" 2 > /dev/null &
sleep 3

echo "Starting ACADEMY_INVENTORY_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_INVENTORY_AGENT_SERVER "-Xms2048m -Xmx2048m -Xgcpolicy:gencon" 3 > /dev/null &
sleep 3

echo "Starting ACADEMY_INVENTORY_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_INVENTORY_AGENT_SERVER "-Xms2048m -Xmx2048m -Xgcpolicy:gencon" 4 > /dev/null &
sleep 3

# Added 3 more ACADEMY_INVENTORY_AGENT_SERVER agents for 10/30/2014 release
#echo "Starting ACADEMY_INVENTORY_AGENT_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_INVENTORY_AGENT_SERVER "-Xms2048m -Xmx2048m -Xgcpolicy:gencon" 5 > /dev/null &
#sleep 3

#echo "Starting ACADEMY_INVENTORY_AGENT_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_INVENTORY_AGENT_SERVER "-Xms2048m -Xmx2048m -Xgcpolicy:gencon" 6 > /dev/null &
#sleep 3

#echo "Starting ACADEMY_INVENTORY_AGENT_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_INVENTORY_AGENT_SERVER "-Xms2048m -Xmx2048m -Xgcpolicy:gencon" 7 > /dev/null &
#sleep 3

#Start-Added as part of Shared Inventory

#echo "Starting ACADEMY_INVENTORY_AGENT_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_INVENTORY_AGENT_SERVER "-Xms2048m -Xmx2048m -Xgcpolicy:gencon" 8 > /dev/null &
#sleep 3

#echo "Starting ACADEMY_INVENTORY_AGENT_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_INVENTORY_AGENT_SERVER "-Xms2048m -Xmx2048m -Xgcpolicy:gencon" 9 > /dev/null &
#sleep 3

#End-Added as part of Shared Inventory

echo "Starting AcademyCancelShipmentServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyCancelShipmentServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademyReceiveRMAServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyReceiveRMAServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademySFSFullInventorySyncInterface ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademySFSFullInventorySyncInterface "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademySFSFullInventorySyncInterface ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademySFSFullInventorySyncInterface "-Xms1024m -Xmx1024m" 2 > /dev/null &
sleep 3

#Start-Added as part of Shared Inventory

echo "Starting AcademySFSFullInventorySyncInterface ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademySFSFullInventorySyncInterface "-Xms2048m -Xmx2048m" 3 > /dev/null &
sleep 3

echo "Starting AcademySFSFullInventorySyncInterface ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademySFSFullInventorySyncInterface "-Xms2048m -Xmx2048m" 4 > /dev/null &
sleep 3

#End-Added as part of Shared Inventory

echo "Starting AcademyPickMessageForBulkPrint ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyPickMessageForBulkPrint "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademyWGDeliveryConfirmation ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyWGDeliveryConfirmation "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademySFSTrickleInventoryFeedInterface ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademySFSTrickleInventoryFeedInterface "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

#echo "Starting AcademyDSVTrickleInventoryFeedInterface ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyDSVTrickleInventoryFeedInterface "-Xms1024m -Xmx1024m" 1 > /dev/null &
#sleep 3

#echo "Starting AcademyDSVFullInventorySyncInterface ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyDSVFullInventorySyncInterface "-Xms1024m -Xmx1024m" 1 > /dev/null &
#sleep 3

# Comment Start as part of STL-1456
#echo "Starting ACADEMY_CLOSE_MANIFEST_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_CLOSE_MANIFEST_SERVER "-Xms1024m -Xmx1024m" 1 > /dev/null &
#sleep 3
#echo "Starting ACADEMY_CLOSE_MANIFEST_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_CLOSE_MANIFEST_SERVER "-Xms1024m -Xmx1024m" 2 > /dev/null &
#sleep 3
#echo "Starting ACADEMY_CLOSE_MANIFEST_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_CLOSE_MANIFEST_SERVER "-Xms1024m -Xmx1024m" 3 > /dev/null &
#sleep 3
#echo "Starting ACADEMY_CLOSE_MANIFEST_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_CLOSE_MANIFEST_SERVER "-Xms1024m -Xmx1024m" 4 > /dev/null &
#sleep 3
#echo "Starting ACADEMY_CLOSE_MANIFEST_SERVER ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_CLOSE_MANIFEST_SERVER "-Xms1024m -Xmx1024m" 5 > /dev/null &
#sleep 3

#echo "Starting ACADCloseManifestReprocessingAgent ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADCloseManifestReprocessingAgent "-Xms1024m -Xmx1024m" 1 > /dev/null &
#sleep 3
# Comment End as part of STL-1456

echo "Starting AcademyItemInterfaceServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyItemInterfaceServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademyBatchPrintServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyBatchPrintServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_WMS_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_WMS_AGENT_SERVER "-Xms2048m -Xmx2048m" 1 > /dev/null &
sleep 3

echo "Starting AcademyExceptionMonitor ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyExceptionMonitor "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_OMS_PURGE_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_OMS_PURGE_AGENT_SERVER "-Xms1024m -Xmx2048m" 1 > /dev/null &
sleep 3

echo "Starting AcademyAuditPurgeAgent1 ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyAuditPurgeAgent1 "-Xms1024m -Xmx2048m" 1 > /dev/null &
sleep 3

echo "Starting AcademyAuditPurgeAgent2 ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyAuditPurgeAgent2 "-Xms1024m -Xmx2048m" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_WMS_PURGE_AGENT_SERVER1 ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_WMS_PURGE_AGENT_SERVER1 "-Xms1024m -Xmx2048m" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_ORDER_PURGE_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_ORDER_PURGE_AGENT_SERVER "-Xms1024m -Xmx2048m" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_MONITOR_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_MONITOR_AGENT_SERVER "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_WMS_PURGE_AGENT_SERVER2 ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_WMS_PURGE_AGENT_SERVER2 "-Xms1024m -Xmx2048m" 1 > /dev/null &
sleep 3

#echo "Starting AcademyDSVFullInventoryLoadInterface ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyDSVFullInventoryLoadInterface "-Xms1024m -Xmx1024m" 1 > /dev/null &
#sleep 3

echo "Starting AcademyGetBulkGCLinesQueueMsg ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyGetBulkGCLinesQueueMsg "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

#echo "Starting AcademyManageCustomerServer ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyManageCustomerServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
#sleep 3

echo "Starting AcademyProcessHoldTypeServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyProcessHoldTypeServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademyResolveOrderHoldServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyResolveOrderHoldServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademySFSReceiveStoreDetailInterface ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademySFSReceiveStoreDetailInterface "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

#echo "Starting AcademyProcessSendInvoiceServer ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyProcessSendInvoiceServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
#sleep 3

echo "Starting AcademyVendorInvoiceFeedInterface ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyVendorInvoiceFeedInterface "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademyGiftCardActivate ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyGiftCardActivate "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_RESERVATION_PURGE_AGENT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_RESERVATION_PURGE_AGENT "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademyInventorySupplyTempPurgeServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyInventorySupplyTempPurgeServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting VendorInvoiceAgentServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh VendorInvoiceAgentServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademySFSProcBackUpStoreAllocationAlert ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademySFSProcBackUpStoreAllocationAlert "-Xms2048m -Xmx2048m" 1 > /dev/null &
sleep 3

echo "Starting AcademyProcessFinTranServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyProcessFinTranServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

#echo "Starting ACADEMY_UPDRADE_DOWNGRADE_SHIPMENT_AGENT ..."
#nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_UPDRADE_DOWNGRADE_SHIPMENT_AGENT "-Xms1024m -Xmx1024m" 1 > /dev/null &
#sleep 3

echo "Starting AcademyReceiveNoInvStatusServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyReceiveNoInvStatusServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademyReceiveNoInvStatusServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyReceiveNoInvStatusServer "-Xms1024m -Xmx1024m" 2 > /dev/null &
sleep 3

echo "Starting AcademyReceiveContainerIdServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyReceiveContainerIdServer "-Xms2048m -Xmx2048m" 1 > /dev/null &
sleep 3

echo "Starting AcademyReceiveContainerIdServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyReceiveContainerIdServer "-Xms2048m -Xmx2048m" 2 > /dev/null &
sleep 3

echo "Starting ACADEMY_EMAIL_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_EMAIL_AGENT_SERVER "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting AcademyErrorReprocessingAgent ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyErrorReprocessingAgent "-Xms1024m -Xmx2048m" 1 > /dev/null &
sleep 3

echo "Starting AcademyCreateInventoryActivityServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyCreateInventoryActivityServer "-Xms1024m -Xmx2048m" 1 > /dev/null &
sleep 3

echo "Starting AcadInventorySnapShotMsgServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcadInventorySnapShotMsgServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting HealthMonitor ..."
nohup startHealthMonitor.sh > nohup.out &
sleep 3

echo "Starting AcademyDefaultAgentServer ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyDefaultAgentServer "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

#SHIN- 1 To start the agent ACADEMY_SI_SHIPMENT_SERVER (001 and 701 shipment creation)
echo "Starting ACADEMY_SI_SHIPMENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_SI_SHIPMENT_SERVER "-Xms1024m -Xmx2048m" 1 > /dev/null &
sleep 3

#Added as part of STL-1433
echo "Starting AcademyDeleteDuplicateContainerAgent ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyDeleteDuplicateContainerAgent "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3
#Added as part of STL-1378
echo "Starting ACADEMY_VOID_AUTH_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_VOID_AUTH_SERVER "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

#Added as part of STL-1456
echo "Starting ACAD_CLOSE_MANIFEST_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACAD_CLOSE_MANIFEST_AGENT_SERVER "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3
echo "Starting ACAD_CLOSE_MANIFEST_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACAD_CLOSE_MANIFEST_AGENT_SERVER "-Xms1024m -Xmx1024m" 2 > /dev/null &
sleep 3
echo "Starting ACAD_CLOSE_MANIFEST_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACAD_CLOSE_MANIFEST_AGENT_SERVER "-Xms1024m -Xmx1024m" 3 > /dev/null &
sleep 3
echo "Starting ACAD_CLOSE_MANIFEST_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACAD_CLOSE_MANIFEST_AGENT_SERVER "-Xms1024m -Xmx1024m" 4 > /dev/null &
sleep 3
echo "Starting ACAD_CLOSE_MANIFEST_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACAD_CLOSE_MANIFEST_AGENT_SERVER "-Xms1024m -Xmx1024m" 5 > /dev/null &
sleep 3
# End as part of STL-1456

#Added as part of STL-1510
echo "Starting ACAD_CLOSE_MANIFEST_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_SCHEDULE_ORDER_AGENT_1 "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3
# End as part of STL-1510

#Added as part of STL-1567
echo "Starting AcadFedxExpShipmentNotificationAgent ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcadFedxExpShipmentNotificationAgent "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3
# End as part of STL-1567

#Added as part of STL-1648
echo "Starting ACADEMY_ACTIVATE_PAYMENT_AGENT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_ACTIVATE_PAYMENT_AGENT "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3
# End as part of STL-1648

#Added as part of GCD-111
echo "Starting AcademyGCActivationReprocessingAgent ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh AcademyGCActivationReprocessingAgent "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3
# End as part of GCD-111

# Added as part of WN-198
echo "Starting ACADEMY_CANCEL_DELAYED_ORDER_AGENT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_CANCEL_DELAYED_ORDER_AGENT "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3
# Added as part of WN-198

# Added as part of WN-2041,WN-2042
echo "Starting ACADEMY_SOF_ACQDSP_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_SOF_ACQDSP_SERVER "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3
# Added as part of WN-2041,WN-2042

# Added as part of WN-2044
echo "Starting ACADEMY_RECEIPT_PURGE_AGENT_SERVER ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_RECEIPT_PURGE_AGENT_SERVER "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3

echo "Starting ACADEMY_SOF_CLOSERECEIPT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_SOF_CLOSERECEIPT "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3
# Added as part of WN-2044

# Added as part of Payment Migration
echo "Starting ACADEMY_CANCEL_DECLINED_ORDER_AGENT ..."
nohup /apps/SterlingOMS/Foundation/bin/ss.sh ACADEMY_CANCEL_DECLINED_ORDER_AGENT "-Xms1024m -Xmx1024m" 1 > /dev/null &
sleep 3
# Added as part of Payment Migration