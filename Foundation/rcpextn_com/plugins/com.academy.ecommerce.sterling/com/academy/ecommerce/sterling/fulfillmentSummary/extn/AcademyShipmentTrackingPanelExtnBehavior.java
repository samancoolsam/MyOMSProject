package com.academy.ecommerce.sterling.fulfillmentSummary.extn;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.academy.ecommerce.sterling.util.XPathUtil;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;

/**
 * @author dipanshu - This class will check shipment type, if it is White Glove then ProNo will be displayed in place of tracking number
 * 
 */
public class AcademyShipmentTrackingPanelExtnBehavior extends YRCExtentionBehavior
{
	/**
	 * This method initializes the behavior class.
	 */
	public void init()
	{}

	/**
	 * Method for validating the text box.
	 */
	public YRCValidationResponse validateTextField(String fieldName, String fieldValue)
	{
		return super.validateTextField(fieldName, fieldValue);
	}

	/**
	 * Method for validating the combo box entry.
	 */
	public void validateComboField(String fieldName, String fieldValue)
	{
		super.validateComboField(fieldName, fieldValue);
	}

	/**
	 * Method called when a button is clicked.
	 */
	public YRCValidationResponse validateButtonClick(String fieldName)
	{
		return super.validateButtonClick(fieldName);
	}

	/**
	 * Method called when a link is clicked.
	 */
	public YRCValidationResponse validateLinkClick(String fieldName)
	{
		return super.validateLinkClick(fieldName);
	}

	@Override
	public void postCommand(YRCApiContext apiContext)
	{
		YRCPlatformUI.trace(apiContext.getApiName());
		YRCPlatformUI.trace("output - " + XMLUtil.getXMLString(apiContext.getOutputXml()));
		if(apiContext.getApiName().equals("getSalesOrderDetails"))
		{
			Element outputXML = (Element) apiContext.getOutputXml().getDocumentElement();
			if(outputXML != null)
			{
				try
				{
					NodeList listOfShipmentElements = XPathUtil.getNodeList(outputXML, "OrderLines/OrderLine/Containers/Container/Shipment");
					if (!YRCPlatformUI.isVoid(listOfShipmentElements))
					{
						Element shipmentElement = (Element) listOfShipmentElements.item(0);
						YRCPlatformUI.trace("###### Shipment Details :############", XMLUtil.getElementXMLString(shipmentElement));
						String shipmentType = shipmentElement.getAttribute(AcademyPCAConstants.ATTR_SHIPMENT_TYPE);
						YRCPlatformUI.trace("###### Shipment Type :############", shipmentType);
						if ("WG".equals(shipmentType))
						{
							Element containerElement = (Element) XPathUtil.getNodeList(outputXML, "OrderLines/OrderLine/Containers/Container").item(0);
							String proNo = shipmentElement.getAttribute(AcademyPCAConstants.ATTR_PRO_NO);
							containerElement.setAttribute(AcademyPCAConstants.ATTR_TRACKING_NO, proNo);
						}
					}
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		YRCPlatformUI.trace("new output - " + XMLUtil.getXMLString(apiContext.getOutputXml()));
		
		super.postCommand(apiContext);
	}

	@Override
	public boolean preCommand(YRCApiContext apiContext)
	{
		YRCPlatformUI.trace(apiContext.getApiName());
		return super.preCommand(apiContext);
	}

	public YRCExtendedTableBindingData getExtendedTableBindingData(String tableName, ArrayList tableColumnNames)
	{
		return super.getExtendedTableBindingData(tableName, tableColumnNames);
	}

	
	
	@Override
	public void postSetModel(String namespace)
	{
		if (namespace != null)
		{
			if (namespace.equals("Container"))
			{
				Element containerElement = getModel(namespace);
				if (containerElement != null)
				{
					YRCPlatformUI.trace("###### Container Details:############", XMLUtil.getElementXMLString(containerElement));
					try
					{
						NodeList listOfShipmentElements = XPathUtil.getNodeList(containerElement, "Shipment");
						if (!YRCPlatformUI.isVoid(listOfShipmentElements))
						{
							Element shipmentElement = (Element) listOfShipmentElements.item(0);
							YRCPlatformUI.trace("###### Shipment Details :############", XMLUtil.getElementXMLString(shipmentElement));
							String shipmentType = shipmentElement.getAttribute(AcademyPCAConstants.ATTR_SHIPMENT_TYPE);
							YRCPlatformUI.trace("###### Shipment Type :############", shipmentType);
							if ("WG".equals(shipmentType))
							{
								setProNoAndURL(shipmentElement, containerElement);
								repopulateModel("Container");
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			else if (namespace.equals("Shipment"))
			{
				Element shipmentElement = getModel(namespace);
				if (shipmentElement != null)
				{
					YRCPlatformUI.trace("###### Shipment Details:############", XMLUtil.getElementXMLString(shipmentElement));
					try
					{
						String shipmentType = shipmentElement.getAttribute(AcademyPCAConstants.ATTR_SHIPMENT_TYPE);
						YRCPlatformUI.trace("###### Shipment Type :############", shipmentType);
						if ("WG".equals(shipmentType))
						{
							NodeList listOfContainerElements = XPathUtil.getNodeList(shipmentElement, "Containers/Container");
							if (!YRCPlatformUI.isVoid(listOfContainerElements))
							{
								Element containerElement = (Element) listOfContainerElements.item(0);
								YRCPlatformUI.trace("###### Container Element :############", XMLUtil.getElementXMLString(containerElement));
								setProNoAndURL(shipmentElement, containerElement);
								repopulateModel("Shipment");
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			else if (namespace.equals("OrderDetails"))
			{
				Element orderElement = getModel(namespace);
				if (orderElement != null)
				{
					YRCPlatformUI.trace("###### Order Details:############", XMLUtil.getElementXMLString(orderElement));
					try
					{
						NodeList listOfShipmentElements = XPathUtil.getNodeList(orderElement, "Containers/Container/Shipment");
						if (!YRCPlatformUI.isVoid(listOfShipmentElements))
						{
							Element shipmentElement = (Element) listOfShipmentElements.item(0);
							YRCPlatformUI.trace("###### Shipment Details :############", XMLUtil.getElementXMLString(shipmentElement));
							String shipmentType = shipmentElement.getAttribute(AcademyPCAConstants.ATTR_SHIPMENT_TYPE);
							YRCPlatformUI.trace("###### Shipment Type :############", shipmentType);
							if ("WG".equals(shipmentType))
							{
								Element containerElement = (Element) XPathUtil.getNodeList(orderElement, "Containers/Container").item(0);
								setProNoAndURL(shipmentElement, containerElement);
								repopulateModel("OrderDetails");
							}
						}

						YRCPlatformUI.trace("###### Final Order Details:############", XMLUtil.getElementXMLString(orderElement));
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		
		super.postSetModel(namespace);
	}

	private void setProNoAndURL(Element shipmentElement, Element containerElement)
	{
		String proNo = shipmentElement.getAttribute(AcademyPCAConstants.ATTR_PRO_NO);
		String url = "https://www.con-way.com/webapp/manifestrpts_p_app/Tracking/TrackingRS.jsp?PRO=" + proNo;

		YRCPlatformUI.trace("###### pro No :############", proNo);
		YRCPlatformUI.trace("###### URL :############", url);

		containerElement.setAttribute(AcademyPCAConstants.ATTR_TRACKING_NO, proNo);
		containerElement.setAttribute(AcademyPCAConstants.ATTR_URL, url);
	}

}
