<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.lang.*" %>
<%@ page import="java.lang.Object.*" %>
<%@ page import="com.yantra.yfc.dom.*" %>

<%
/* This file is used to calculate final value displayed in adjustment detail screen. */
    YFCElement rootElement = (YFCElement)request.getAttribute("Item");
    if(rootElement != null){
        YFCNodeList inventorySupplyNodeList = rootElement.getElementsByTagName("InventorySupply");
        if(inventorySupplyNodeList.getLength() > 0){
            YFCElement inventorySupplyElement= (YFCElement)inventorySupplyNodeList.item(0);
            if(inventorySupplyElement != null){
                String sBtnAction= request.getParameter("BtnAction");
                if (equals(sBtnAction, "CALCULATE")){
                    double dQtyToBeAdjusted =0;
                    double dNewQty =0; 
                    String sQtyToBeAdjusted = request.getParameter("xml:/Items/Item/@Quantity");
                    String sQtyQryType= request.getParameter("xml:/Item/Supplies/InventorySupply/@QtyQryType");
                    String sAvailability=request.getParameter("xml:/Items/Item/@Availability");
                    if(!isVoid(sQtyToBeAdjusted)){
                        dQtyToBeAdjusted = Double.valueOf(sQtyToBeAdjusted).doubleValue();
                        if(dQtyToBeAdjusted < 0){
                            dNewQty = dQtyToBeAdjusted * (-1);
                            inventorySupplyElement.setQtyAttribute("NewQuantity", dNewQty);
                        }
                        else{
                            inventorySupplyElement.setQtyAttribute("NewQuantity",  dQtyToBeAdjusted );
                        }

                        inventorySupplyElement.setQtyAttribute("QtyToBeAdjusted",  dQtyToBeAdjusted );
                        inventorySupplyElement.setQtyAttribute("FinalQuantity",  inventorySupplyElement.getDoubleAttribute("Quantity") + dQtyToBeAdjusted);
                        inventorySupplyElement.setAttribute("QtyQryType",  sQtyQryType);
                        inventorySupplyElement.setAttribute("AvailabilityType",  sAvailability);
                    }
                }
            }
        }
    }
    
%>


