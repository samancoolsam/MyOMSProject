/**
 * 
 */
package com.academy.ecommerce.sterling.order.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author dbanwar
 *
 */
public class AcademyGetReturnOrderDetailsForStatusUpdate implements YIFCustomApi {

	/**
     * log variable.
     */
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyGetReturnOrderDetailsForStatusUpdate.class);
	
	public Document processReturnOrdeDetailsForStatusUpdate(YFSEnvironment env, Document inXML) throws Exception 
	{
		log.beginTimer(" Begining of AcademyGetReturnOrderDetailsForStatusUpdate->processReturnOrdeDetailsForStatusUpdate Api");
		if (!YFCObject.isVoid(inXML.getDocumentElement())) 
		{
			Element inputXML = inXML.getDocumentElement();
			NodeList orderLineNodeList = inputXML.getElementsByTagName("OrderLine");
			if(orderLineNodeList!=null && orderLineNodeList.getLength() > 0)
			{
				for (int i = 0; i < orderLineNodeList.getLength(); i++) 
				{
					Element orderLineElement = (Element) orderLineNodeList.item(i);
					String orderLineKey = XPathUtil.getString(orderLineElement, "./@DerivedFromOrderLineKey");

					if (!StringUtil.isEmpty(orderLineKey)) 
					{
						YFCElement elem = YFCDocument.createDocument("OrderLine").getDocumentElement();
						elem.setAttribute("OrderLineKey", orderLineKey);
						Document outputDocument = AcademyUtil.invokeAPI(env, "getOrderLineList", elem.getOwnerDocument().getDocument());
			
						if(outputDocument != null)
						{
							String itemIdentifier = XPathUtil.getString(outputDocument, "OrderLineList/OrderLine/Extn/@ExtnWCOrderItemIdentifier");
							Element extnItemIdentifier = (Element)inputXML.getElementsByTagName("Order/OrderLines/OrderLine/Extn").item(0);
							extnItemIdentifier.setAttribute("ExtnWCOrderItemIdentifier", itemIdentifier);
						}

					}
				}
			}
		}
		log.endTimer(" End of AcademyGetReturnOrderDetailsForStatusUpdate->processReturnOrdeDetailsForStatusUpdate Api");
		return inXML;
	}

	/* (non-Javadoc)
	 * @see com.yantra.interop.japi.YIFCustomApi#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

}
