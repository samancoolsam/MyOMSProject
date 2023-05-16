package com.academy.util.common;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyGetUserGroupName {
	
	 private static Logger logger = Logger.getLogger(AcademyGetUserGroupName.class.getName());
	 
	 public Document getUserGroupName(YFSEnvironment env,Document inDoc){
		 Map userGroupMap=createUserGroupMap(env);
		 String userGroup=getUserGroup(env,userGroupMap);
		 inDoc.getDocumentElement().setAttribute("ExtnUserGroupName", userGroup);
		 return inDoc;
	 }

	private String getUserGroup(YFSEnvironment env, Map userGroupMap) {

		
		String userId=env.getUserId();
		String currUserGroupName="";
		
		try {
			Document getUserListInputXML=XMLUtil.createDocument("User");
			getUserListInputXML.getDocumentElement().setAttribute("Loginid", userId);
			Document getUserListOutputXML=AcademyUtil.invokeAPI(env, "getUserList", getUserListInputXML);
			
			NodeList userGroupList=getUserListOutputXML.getElementsByTagName("UserGroupList");
			int length=userGroupList.getLength();
			if(length>0){
				
				for(int i=0;i<length;i++){
					Element currUserGroupListElem=(Element) userGroupList.item(i);
					String usergroupkey=currUserGroupListElem.getAttribute("UsergroupKey");
					String userGroupName=getUserGroupName(env,usergroupkey);
					if(currUserGroupName.equals("")&&userGroupMap.get(userGroupName)!=null){
						currUserGroupName=userGroupName;
					}else{
						if(userGroupMap.get(userGroupName)!=null && userGroupMap.get(currUserGroupName)!=null){
							if((Integer) (userGroupMap.get(userGroupName))>(Integer) (userGroupMap.get(currUserGroupName))){
								currUserGroupName=userGroupName;
							}
						}
					}
				}
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return currUserGroupName;
	
	}

	private String getUserGroupName(YFSEnvironment env, String usergroupkey) {

		String usergroupName="";
		try {
			Document getUserGroupListInputXML=XMLUtil.createDocument("UserGroup");
			getUserGroupListInputXML.getDocumentElement().setAttribute("UsergroupKey", usergroupkey);
			Document getUserGroupListOutputXML=AcademyUtil.invokeAPI(env, "getUserGroupList", getUserGroupListInputXML);
			usergroupName=((Element) getUserGroupListOutputXML.getElementsByTagName("UserGroup").item(0)).getAttribute("UsergroupName");
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return usergroupName;
	
	}

	private Map createUserGroupMap(YFSEnvironment env) {
		Map mp=new HashMap<String, Integer>();
		try {
			Document getCommonCodeListInputXML = XMLUtil.createDocument("CommonCode");
		
		getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeType", AcademyConstants.APP_USR_GRP_LST);
		Document outXML=AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML);
		if(outXML!=null){
			NodeList listOfCommCodes=outXML.getElementsByTagName("CommonCode");
			int len=listOfCommCodes.getLength();
			if(len>0){
				for(int i=0;i<len;i++){
				Element commCodeElem=(Element) listOfCommCodes.item(i);
				mp.put(commCodeElem.getAttribute("CodeValue"), Integer.parseInt(commCodeElem.getAttribute("CodeShortDescription")));
				}
			}
		}
		
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mp;
	}

}
