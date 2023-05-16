//package declaration
package com.academy.ecommerce.sterling.util;

//import statements
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.util.YFCCommon;
//java util import statements
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//academy util import statements
import com.academy.util.xml.XMLUtil;

//yantra import statements
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/** Description: Class AcademyCommonCode retrieves the values 
 *              stored in the common code 
 */
public class AcademyCommonCode 
{
	private static YIFApi api = null;

	static
	{
		try
		{
			api = YIFClientFactory.getInstance().getApi();
		} catch (YIFClientCreationException e) 
		{
			e.printStackTrace();
		}
	}

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyCommonCode.class);

	/**
	 * gets the list of common code values by calling getCommonCodeList API
	 * @param env Yantra Environment Context.
	 * @param CodeType String value to determine common code.
	 * @param OrganizationCode String containingg Organization Code
	 * @return hmCommonCodeList HashMap containing [Code Value, CodeShortDescription]
	 */ 
	public static HashMap<String,String> getCommonCodeListAsHashMap(YFSEnvironment env,String CodeType,String OrganizationCode) throws Exception
	{
		//Declare the hashmap variable
		HashMap<String,String> hmCommonCodeList=new HashMap<String,String>();
		//Start: Process API getCommonCodeList
		//Create root element CommonCode
		Document inDoc = XMLUtil.createDocument("CommonCode");
		Element eleRooElement =(Element) inDoc.getDocumentElement();
		//Set attribute value for CodeType
		eleRooElement.setAttribute("CodeType", CodeType);
		//set attribute value for OrganizationCode
		eleRooElement.setAttribute("OrganizationCode", OrganizationCode);
		//creating the output template for getCommonCodeList API
		Document templateDoc = YFCDocument.getDocumentFor("<CommonCode CodeValue=\"\" CodeShortDescription=\"\" />").getDocument();
		//Print verbose log
		if(log.isVerboseEnabled())
		{
			log.verbose("template is" + XMLUtil.getXMLString(templateDoc));
		}
		env.setApiTemplate("getCommonCodeList", templateDoc);
		//Invoke getCommonCodeList API
		Document outDoc = api.getCommonCodeList(env,inDoc);
		//Clear the template
		env.clearApiTemplate("getCommonCodeList");
		//End: Process API getCommonCodeList
		if(log.isVerboseEnabled())
		{
			log.verbose("Output of the API is:" + XMLUtil.getXMLString(outDoc));
		}
		//Fetch the NodeList of element CommonCode
		NodeList nlGetCodeList = (NodeList) outDoc.getElementsByTagName("CommonCode");
		//populating the hashmap hmcommonCodeList		
		//Loop through the NodeList record
		for(int i = 0;i < nlGetCodeList.getLength();i++)
		{
			//Fetch the element CommonCode
			Element eleCode = (Element)nlGetCodeList.item(i);
			//Fetch the attribute value CodeValue
			String strCodeValue = eleCode.getAttribute("CodeValue");
			//Fetch the attribute Value CodeShortDescription
			String strCodeDesc = eleCode.getAttribute("CodeShortDescription");
			//Put the value of CodeValue and CodeShortDescription into hashmap
			hmCommonCodeList.put(strCodeValue, strCodeDesc);
		}
		//Return the hashmap list
		return hmCommonCodeList;
	}
/**
	 * gets the list of common code values by calling getCommonCodeList API
	 * @param env Yantra Environment Context.
	 * @param CodeType String value to determine common code.
	 * @param OrganizationCode String containingg Organization Code
	 * @return hmCommonCodeList HashMap containing [Code Value, CodeLongDescription]
	 */ 
	public static HashMap<String,String> getCommonCodeListAsHashMapLngDesc(YFSEnvironment env,String CodeType,String OrganizationCode) throws Exception
	{
		//Declare the hashmap variable
		HashMap<String,String> hmCommonCodeList=new HashMap<String,String>();
		//Start: Process API getCommonCodeList
		//Create root element CommonCode
		Document inDoc = XMLUtil.createDocument("CommonCode");
		Element eleRooElement =(Element) inDoc.getDocumentElement();
		//Set attribute value for CodeType
		eleRooElement.setAttribute("CodeType", CodeType);
		//set attribute value for OrganizationCode
		eleRooElement.setAttribute("OrganizationCode", OrganizationCode);
		//creating the output template for getCommonCodeList API
		Document templateDoc = YFCDocument.getDocumentFor("<CommonCode CodeValue=\"\" CodeLongDescription=\"\" />").getDocument();
		//Print verbose log
		if(log.isVerboseEnabled())
		{
			log.verbose("template is" + XMLUtil.getXMLString(templateDoc));
		}
		env.setApiTemplate("getCommonCodeList", templateDoc);
		//Invoke getCommonCodeList API
		Document outDoc = api.getCommonCodeList(env,inDoc);
		//Clear the template
		env.clearApiTemplate("getCommonCodeList");
		//End: Process API getCommonCodeList
		if(log.isVerboseEnabled())
		{
			log.verbose("Output of the API is:" + XMLUtil.getXMLString(outDoc));
		}
		//Fetch the NodeList of element CommonCode
		NodeList nlGetCodeList = (NodeList) outDoc.getElementsByTagName("CommonCode");
		//populating the hashmap hmcommonCodeList		
		//Loop through the NodeList record
		for(int i = 0;i < nlGetCodeList.getLength();i++)
		{
			//Fetch the element CommonCode
			Element eleCode = (Element)nlGetCodeList.item(i);
			//Fetch the attribute value CodeValue
			String strCodeValue = eleCode.getAttribute("CodeValue");
			//Fetch the attribute Value CodeShortDescription
			String strCodeDesc = eleCode.getAttribute("CodeLongDescription");
			//Put the value of CodeValue and CodeShortDescription into hashmap
			hmCommonCodeList.put(strCodeValue, strCodeDesc);
		}
		//Return the hashmap list
		return hmCommonCodeList;
	}
	/**
	 * gets the list of common code values by calling getCommonCodeList API
	 * @param env Yantra Environment Context.
	 * @param CodeType String value to determine common code.
	 * @param OrganizationCode String containingg Organization Code
	 * @return Document Document containing output of getCommonCodeList API
	 */ 
	public static Document getCommonCodeList(YFSEnvironment env,String CodeType,String OrganizationCode) throws ParserConfigurationException, YFSException, RemoteException
	{
		//Start: Process API getCommonCodeList
		//Creat the root element CommonCode
		Document inDoc = XMLUtil.createDocument("CommonCode");
		Element eleRooElement = (Element) inDoc.getDocumentElement();
		//Map the attribute value for CodeType
		eleRooElement.setAttribute("CodeType", CodeType);
		//Map the attribute value for OrganizationCode
		eleRooElement.setAttribute("OrganizationCode", OrganizationCode);
		//creating the output template for getCommonCodeList API
		Document templateDoc = YFCDocument.getDocumentFor("<CommonCode CodeValue=\"\" CodeLongDescription=\"\" CodeShortDescription=\"\" />").getDocument();
		//Print verbose log
		if(log.isVerboseEnabled())
		{
			log.verbose("template is" + XMLUtil.getXMLString(templateDoc));
		}
		env.setApiTemplate("getCommonCodeList", templateDoc);
		//Invoke getCommonCodeList API
		Document outDoc = api.getCommonCodeList(env,inDoc);
		//Clear the template
		env.clearApiTemplate("getCommonCodeList");
		//End: Process API getCommonCodeList
		if(log.isVerboseEnabled())
		{
			log.verbose("Output of the API is:" + XMLUtil.getXMLString(outDoc));
		}
		return outDoc;
	}
	/**
	 * @param env
	 * @param CodeType
	 * @param CodeShortDescription
	 * @param OrganizationCode
	 * @return CodeValue
	 */
	public static String getCodeValue(YFSEnvironment env, String CodeType, String CodeShortDescription,
			String OrganizationCode) {

		log.beginTimer(" **START getCommonCodeListCodeValue** ");
		String strCodeValue = "";
		try {
			log.verbose("CodeType : " + CodeType + " OrganizationCode : " + OrganizationCode);
			log.verbose("CodeLongDescription : " + CodeType + " CodeShortDescription : " + CodeShortDescription);
			YFCDocument inDoc = YFCDocument.createDocument("CommonCode");
			YFCElement inEle = inDoc.getDocumentElement();
			inEle.setAttribute("CodeType", CodeType);
			inEle.setAttribute("OrganizationCode", OrganizationCode);
			inEle.setAttribute("CodeShortDescription", CodeShortDescription);
			Document templateDoc = YFCDocument.getDocumentFor("<CommonCode CodeValue='' />").getDocument();
			log.verbose("input getCommonCodeList ::: " + inDoc.toString());
			env.setApiTemplate("getCommonCodeList", templateDoc);
			Document outDoc = api.getCommonCodeList(env, inDoc.getDocument());
			env.clearApiTemplate("getCommonCodeList");
			log.verbose("output getCommonCodeList ::: " + YFCDocument.getDocumentFor(outDoc).toString());
			if (!YFCCommon.isVoid(outDoc)) {
				Element outEle = outDoc.getDocumentElement();
				strCodeValue = SCXmlUtil.getXpathAttribute(outEle, ".//CommonCode/@CodeValue");
			}
		} catch (Exception e) {
			throw new YFSException(
					" **No CodeType-OrganizationCode-CodeShortDescription combination ** " + e.getMessage());
		} finally {
			log.verbose("CodeValue : " + strCodeValue);
			log.endTimer(" **END getCommonCodeListCodeValue** ");
		}
		return strCodeValue;
	}

	/**
	 * @param env
	 * @param CodeType
	 * @param OrganizationCode
	 * @return CodeValueList
	 */
	public static List<String> getCodeValueList(YFSEnvironment env, String CodeType, String OrganizationCode) {

		List<String> codeValueList = new ArrayList<String>();
		log.beginTimer(" **START getCommonCodeListCodeValue** ");
		String strCodeValue = "";
		try {
			log.verbose("CodeType : " + CodeType + " OrganizationCode : " + OrganizationCode + "CodeLongDescription : "
					+ CodeType);
			YFCDocument inDoc = YFCDocument.createDocument("CommonCode");
			YFCElement inEle = inDoc.getDocumentElement();
			inEle.setAttribute("CodeType", CodeType);
			inEle.setAttribute("OrganizationCode", OrganizationCode);
			Document templateDoc = YFCDocument.getDocumentFor("<CommonCode CodeValue='' />").getDocument();
			log.verbose("input getCommonCodeList :::" + inDoc.toString());
			env.setApiTemplate("getCommonCodeList", templateDoc);
			Document outDoc = api.getCommonCodeList(env, inDoc.getDocument());
			env.clearApiTemplate("getCommonCodeList");
			log.verbose("output getCommonCodeList :::" + YFCDocument.getDocumentFor(outDoc).toString());
			if (!YFCCommon.isVoid(outDoc)) {
				XMLUtil.getElementsByTagName(outDoc.getDocumentElement(), "CommonCode").stream()
						.forEach(eleCC -> codeValueList.add(((Element) eleCC).getAttribute("CodeValue")));
			}
		} catch (Exception e) {
			throw new YFSException(
					" **No CodeType-OrganizationCode-CodeShortDescription combination ** " + e.getMessage());
		} finally {
			log.verbose("CodeValueList : " + codeValueList);
			log.endTimer(" **END getCommonCodeListCodeValue** ");
		}
		return codeValueList;
	}
	
	/**
	 * 
	 * @param env
	 * @param CodeType
	 * @param OrganizationCode
	 * @return
	 */
	
	public static  CopyOnWriteArrayList<String> getCodeValueThreadSafeList(YFSEnvironment env, String CodeType, String OrganizationCode) {

		CopyOnWriteArrayList<String> codeValueList = new CopyOnWriteArrayList<String>();
		log.beginTimer(" **START getCommonCodeListCodeValue** ");
		String strCodeValue = "";
		try {
			log.verbose("CodeType : " + CodeType + " OrganizationCode : " + OrganizationCode + "CodeLongDescription : "
					+ CodeType);
			YFCDocument inDoc = YFCDocument.createDocument("CommonCode");
			YFCElement inEle = inDoc.getDocumentElement();
			inEle.setAttribute("CodeType", CodeType);
			inEle.setAttribute("OrganizationCode", OrganizationCode);
			Document templateDoc = YFCDocument.getDocumentFor("<CommonCode CodeValue='' />").getDocument();
			log.verbose("input getCommonCodeList :::" + inDoc.toString());
			env.setApiTemplate("getCommonCodeList", templateDoc);
			Document outDoc = api.getCommonCodeList(env, inDoc.getDocument());
			env.clearApiTemplate("getCommonCodeList");
			log.verbose("output getCommonCodeList :::" + YFCDocument.getDocumentFor(outDoc).toString());
			XMLUtil.getElementsByTagName(outDoc.getDocumentElement(), "CommonCode").stream()
						.forEach(eleCC -> codeValueList.add(((Element) eleCC).getAttribute("CodeValue")));
		
		} catch (Exception e) {
			throw new YFSException(
					" **No CodeType-OrganizationCode-CodeShortDescription combination ** " + e.getMessage());
		} finally {
			log.verbose("CodeValueList : " + codeValueList);
			log.endTimer(" **END getCommonCodeListCodeValue** ");
		}
		return codeValueList;
	}
}
