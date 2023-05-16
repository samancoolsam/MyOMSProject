package com.academy.ecommerce.sterling.oraclerightnow;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author kgopal
 * 
 * Users shall be able to retrieve a list of existing orders for the last 30
 * days through Sterling in the RightNow application, near real time, using the
 * following search criteria: Customer Email, Customer Phone #, Last Name and
 * First Name.
 * 
 * Service Name: AcademyOracleRNGetRecentOrders
 * 
 * Input XML:
 * 
 * <Input FlowName="AcademyOracleRNGetRecentOrders"> <Login LoginID="admin"
 * Password="password" /> <Order CustomerEmailID=""
 * CustomerFirstName="Tejaswini" CustomerLastName="G" CustomerPhoneNo="" />
 * </Input>
 * 
 * 
 * Sample Ouput:
 * 
 * <OrderList LastOrderHeaderKey="201409100811542384552970" LastRecordSet="Y"
 * ReadFromHistory="N" TotalOrderList="57"> <Order BillToID=""
 * CustomerEMailID="Tejaswini.g@gmail.com" CustomerFirstName="Tejaswini"
 * CustomerLastName="G" CustomerZipCode="77081-2602"
 * OrderDate="2014-08-18T15:36:36-05:00"
 * OrderHeaderKey="201408180340592384152604" OrderName="" OrderNo="201408181"
 * Status="Backordered"> <PriceInfo Currency="USD" TotalAmount="42.78"/>
 * <PersonInfoBillTo AddressLine1="5750 GULFTON STREET"/> </Order> <Order
 * BillToID=" " CustomerEMailID="Tejaswini.g@gmail.com"
 * CustomerFirstName="Tejaswini" CustomerLastName="G"
 * CustomerZipCode="77081-2602" OrderDate="2014-08-18T15:36:36-05:00"
 * OrderHeaderKey="201408180350012384152672" OrderName="" OrderNo="201408182"
 * Status="Included In Shipment"> <PriceInfo Currency="USD"
 * TotalAmount="42.78"/> <PersonInfoBillTo AddressLine1="5750 GULFTON STREET"/>
 * </Order> </OrderList>
 * 
 */

public class AcademyOracleRNGetRecentOrders implements YIFCustomApi {

	private Properties props;

	private static final YFCLogCategory log = YFCLogCategory
			.instance(AcademyOracleRNGetRecentOrders.class);

	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	public Document oracleRNGetRecentOrders(YFSEnvironment env, Document inDoc)
			throws Exception {

		log.verbose("Input to method oracleRNGetRecentOrders is: "
				+ XMLUtil.getXMLString(inDoc));

		Document docOrderListInput = null;
		Document docOrderListOutput = null;

		Element rootElement = inDoc.getDocumentElement();

		// Getting the login element and validating the login credentials
		Element eleLogin = (Element) rootElement.getElementsByTagName("Login")
				.item(0);
		String strLoginID = eleLogin.getAttribute("LoginID");
		String strPassword = eleLogin.getAttribute("Password");
		Document docResponse = AcademyUtil.validateLoginCredentials(env,
				strLoginID, strPassword);

		if (YFCObject.isVoid(docResponse)) {
			docOrderListOutput = XMLUtil.createDocument("Error");
			docOrderListOutput.getDocumentElement().setAttribute("ErrorCode",
					AcademyConstants.LOGIN_ERROR_CODE);
			docOrderListOutput.getDocumentElement().setAttribute(
					"ErrorDescription", AcademyConstants.LOGIN_ERROR_DESC);
			return docOrderListOutput;
		} else {

			log
					.verbose("User is authenticated successfully. Going to call getOrderList() API");

			YFCDate yfcDate = new YFCDate(new Date());
			String strDateFormat = "yyyy-MM-dd";
			SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
			Calendar cal = Calendar.getInstance();
			int days = Integer.valueOf(props.getProperty("NO_OF_DAYS"))
					.intValue();
			cal.add(Calendar.DATE, (-1 * days));

			Element eleOrder = (Element) rootElement.getElementsByTagName(
					"Order").item(0);
			String strToRange = sDateFormat.format(yfcDate) + "T23:59:59";
			String strFromRange = sDateFormat.format(cal.getTime())
					+ "T00:00:00";
			eleOrder.setAttribute("DocumentType",
					AcademyConstants.SALES_DOCUMENT_TYPE);
			eleOrder.setAttribute("EnterpriseCode",
					AcademyConstants.ENTERPRISE_CODE_SHIPMENT);
			eleOrder.setAttribute("DraftOrderFlag", AcademyConstants.STR_NO);
			eleOrder.setAttribute("ReadFromHistory", AcademyConstants.STR_NO);
			eleOrder.setAttribute("OrderDateQryType", AcademyConstants.ORDER_DATE_RANGE);
			eleOrder.setAttribute(AcademyConstants.ORDER_DATE_FROM_RANGE, strFromRange);
			eleOrder.setAttribute(AcademyConstants.ORDER_DATE_TO_RANGE, strToRange);
			Element eleOrderBy = inDoc.createElement(AcademyConstants.ELE_ORDERBY);
			Element eleAttribute = inDoc.createElement(AcademyConstants.ELE_ATTRIBUTE);
			eleAttribute.setAttribute("Desc", props.getProperty("SORT_DESC"));
			eleAttribute.setAttribute("Name", AcademyConstants.ATTR_ORDER_DATE);
			eleOrderBy.appendChild(eleAttribute);
			eleOrder.appendChild(eleOrderBy);

			docOrderListInput = XMLUtil.getDocumentForElement(eleOrder);
			log.verbose("The input to getOrderList() API is: "
					+ XMLUtil.getXMLString(docOrderListInput));

			docOrderListOutput = AcademyUtil.invokeService(env,
					"AcademyOracleRNGetOrderList", docOrderListInput);
			log.verbose("Output of getOrderList() API is :"
					+ XMLUtil.getXMLString(docOrderListOutput));

			return docOrderListOutput;

		}
	}
}
