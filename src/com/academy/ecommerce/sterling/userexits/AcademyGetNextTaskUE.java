package com.academy.ecommerce.sterling.userexits;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.ue.YCPCanTaskBeSuggestedUE;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.*;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class AcademyGetNextTaskUE
    implements YCPCanTaskBeSuggestedUE
{

	private static YFCLogCategory log = YFCLogCategory
	.instance(AcademyGetNextTaskUE.class);

    public AcademyGetNextTaskUE()
    {
    }

    public Document canTaskBeSuggested(YFSEnvironment env, Document inDoc)
        throws YFSUserExitException
    {
        log.beginTimer(" Begining of AcademyGetNextTaskUE-> canTaskBeSuggested Api");
        YFCException ex = new YFCException();
        Element eleTask = inDoc.getDocumentElement();
        String strAssignedUser = env.getUserId();
        log.verbose((new StringBuilder("****************** Getting the user from the environment. " +
        		"User performing this task is ************")).append(strAssignedUser).toString());
        String strTaskStatus = eleTask.getAttribute("TaskStatus");
        String strTaskType = eleTask.getAttribute("TaskType");
        //String strInEquipmentId = eleTask.getAttribute("EquipmentId");
        Element eleInTaskReferences = (Element)eleTask.getElementsByTagName("TaskReferences").item(0);
        String strInShipmentNo = eleInTaskReferences.getAttribute("ShipmentNo");
        String strInBatchNo = eleInTaskReferences.getAttribute("BatchNo");
        log.verbose("****************** Incoming Batch No is ************" + strInBatchNo);
        Integer iTaskStatus = Integer.valueOf(Integer.parseInt(strTaskStatus));
        if(iTaskStatus.intValue() == 1300)
        {
            log.verbose("****************** Task Status is In-Progress ************");
            eleTask.setAttribute("CanTaskBeSuggested", "Y");
        } else
        if(iTaskStatus.intValue() < 1300)
        {
            log.verbose("****************** Task Status is either Open or Suggested ************");
            try
            {
                Document docTask = XMLUtil.createDocument("Task");
                Element eTask = docTask.getDocumentElement();
                eTask.setAttribute("AssignedToUserId", strAssignedUser);
                eTask.setAttribute("TaskStatus", "1300");
                Document outTempGetTaskList = YFCDocument.parse("<TaskList TotalNumberOfRecords=\"\"><Task AssignedToUserId=\"\" EquipmentId=\"\"" +
				" TargetLocationId=\"\" TaskKey=\"\" TaskType=\"\" TaskStatus=\"\"><TaskReferences BatchNo=\"\" ShipmentNo=\"\" /><TaskType ActivityGroupId=\"\" /></Task></TaskList>"
				).getDocument();
                env.setApiTemplate("getTaskList", outTempGetTaskList);
                log.verbose((new StringBuilder("****************** getTaskList output Document :::::")).append(XMLUtil.getXMLString(outTempGetTaskList)).toString());
                Document outDocTaskList = AcademyUtil.invokeAPI(env, "getTaskList", docTask);
                env.clearApiTemplates();
                if(!YFCObject.isNull(outDocTaskList))
                {
                    log.verbose("****************** getTaskList API output is not null************");
                    Element eTaskList = outDocTaskList.getDocumentElement();
                    String strTotalNumberOfInProgressTasks = eTaskList.getAttribute("TotalNumberOfRecords");
                    Integer iTotalNumberOfInProgressTasks = Integer.valueOf(Integer.parseInt(strTotalNumberOfInProgressTasks));
                    log.verbose((new StringBuilder("****************** Total Number of records is************")).append(iTotalNumberOfInProgressTasks).toString());
                    if(iTotalNumberOfInProgressTasks.intValue() > 0)
                    {
                        log.verbose("****************** Total Number of records is greater than 0************");
                        Element eleOutTask = (Element)eTaskList.getElementsByTagName("Task").item(0);
                        String strOutTaskType = eleOutTask.getAttribute("TaskType");
                        Element eleOutTaskReferences = (Element)eleOutTask.getElementsByTagName("TaskReferences").item(0);
                        String strOutBatchNo = eleOutTaskReferences.getAttribute("BatchNo");
                        log.verbose("****************** Output of TaskList Batch No is ************" + strOutBatchNo);
                        if("CBP".equals(strTaskType))
                        {
                            log.verbose("Task Type is CBP");
                            //String strEquipmentId = eleOutTask.getAttribute("EquipmentId");
                            
                            if(strOutBatchNo.equals(strInBatchNo))
                            {
                                log.verbose("User is picking the same batch/cart");
                                eleTask.setAttribute("CanTaskBeSuggested", "Y");
                            } else
                            {
                            	log.verbose("Incoming Batch No is " + strInBatchNo);
                            	log.verbose("In progress task is not CBP task/In progress task belongs to different batch : " + strOutBatchNo);
                                log.verbose("User is picking some other batch/cart");
                                eleTask.setAttribute("CanTaskBeSuggested", "N");
                                //ex.setErrorCode("EXTN_ACAD010");
                                //ex.setErrorDescription(": USER HAS TASKS IN-PROGRESS");
                                ex.setAttribute(YFCException.ERROR_CODE, "EXTN_ACAD010");
                                ex.setAttribute(YFCException.ERROR_DESCRIPTION, ": USER HAS TASKS IN-PROGRESS");
                                log.verbose("Created exception object");
                                log.verbose(ex);
                                throw ex;
                            }
                        } else
                        if("SIO".equals(strTaskType))
                        {
                            log.verbose("Task Type is SIO");
                            if("SIO".equals(strOutTaskType))
                            {
                                log.verbose("User is performing SIO tasks and has SIO inprogress task");
                                eleTask.setAttribute("CanTaskBeSuggested", "Y");
                            } else
                            {
                                log.verbose("User is performing SIO tasks and has some other inprogress task");
                                eleTask.setAttribute("CanTaskBeSuggested", "N");
                                //ex.setErrorCode("EXTN_ACAD010");
                                //ex.setErrorDescription(": USER HAS TASKS IN-PROGRESS");
                                ex.setAttribute(YFCException.ERROR_CODE, "EXTN_ACAD010");
                                ex.setAttribute(YFCException.ERROR_DESCRIPTION, ": USER HAS TASKS IN-PROGRESS");
                                log.verbose("Created exception object");
                                log.verbose(ex);
                                throw ex;
                            }
                        } else
                        if("PFB".equals(strTaskType))
                        {
                            log.verbose("Task Type is PFB");
                            Element eleTaskRef = (Element)eleOutTask.getElementsByTagName("TaskReferences").item(0);
                            String strShipmentNo = eleTaskRef.getAttribute("ShipmentNo");
                            if("PFB".equals(strOutTaskType) && strShipmentNo.equals(strInShipmentNo))
                            {
                                log.verbose("User is picking the same shipment");
                                eleTask.setAttribute("CanTaskBeSuggested", "Y");
                            } else
                            {
                                log.verbose("User is picking some other shipment");
                                eleTask.setAttribute("CanTaskBeSuggested", "N");
                                //ex.setErrorCode("EXTN_ACAD010");
                                //ex.setErrorDescription(": USER HAS TASKS IN-PROGRESS");
                                ex.setAttribute(YFCException.ERROR_CODE, "EXTN_ACAD010");
                                ex.setAttribute(YFCException.ERROR_DESCRIPTION, "USER HAS TASKS IN-PROGRESS");
                                log.verbose("Created exception object");
                                log.verbose(ex);
                                throw ex;
                            }
                        } else
                        if("STO-Pick".equals(strTaskType))
                        {
                        	log.verbose("Task Type is STO-Pick");
                        	Element eleTaskRef = (Element)eleOutTask.getElementsByTagName("TaskReferences").item(0);
                            String strShipmentNo = eleTaskRef.getAttribute("ShipmentNo");
                            if("STO-Pick".equals(strOutTaskType) && strShipmentNo.equals(strInShipmentNo))
                            {
                                log.verbose("User is picking the same STO shipment");
                                eleTask.setAttribute("CanTaskBeSuggested", "Y");
                            } else
                            {
                                log.verbose("User is picking some other STO shipment");
                                eleTask.setAttribute("CanTaskBeSuggested", "N");
                                //ex.setErrorCode("EXTN_ACAD010");
                                //ex.setErrorDescription(": USER HAS TASKS IN-PROGRESS");
                                ex.setAttribute(YFCException.ERROR_CODE, "EXTN_ACAD010");
                                ex.setAttribute(YFCException.ERROR_DESCRIPTION, "USER HAS TASKS IN-PROGRESS");
                                log.verbose("Created exception object");
                                log.verbose(ex);
                                throw ex;
                            }
                        }else
                        if("FirstCount".equals(strTaskType) || "SeconCount".equals(strTaskType) || "ExcepCount".equals(strTaskType) || 
                        		"SupCount".equals(strTaskType) || "IPM".equals(strTaskType) || "IPB".equals(strTaskType))
                        {
                        	log.verbose("User is performing either Count or Inbound inprogress tasks");
                            eleTask.setAttribute("CanTaskBeSuggested", "Y");
                        }
                        else
                        {
                            log.verbose("User has some other inprogress tasks");
                            eleTask.setAttribute("CanTaskBeSuggested", "N");
                            //ex.setErrorCode("EXTN_ACAD010");
                            //ex.setErrorDescription(": USER HAS TASKS IN-PROGRESS");
                            ex.setAttribute(YFCException.ERROR_CODE, "EXTN_ACAD010");
                            ex.setAttribute(YFCException.ERROR_DESCRIPTION, ": USER HAS TASKS IN-PROGRESS");
                            log.verbose("Created exception object");
                            log.verbose(ex);
                            throw ex;
                        }
                    } else
                    {
                        log.verbose("****************** Total Number of records is equal to 0************");
                        inDoc.getDocumentElement().setAttribute("CanTaskBeSuggested", "Y");
                    }
                }
            }
            catch(ParserConfigurationException e)
            {
                e.printStackTrace();
            }
            catch(SAXException e)
            {
                e.printStackTrace();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            catch(YFCException e)
            {
            	log.verbose("Inside YFCException Catch Block");
                e.printStackTrace();
                throw e;
            }
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        }
        log.endTimer(" End of AcademyGetNextTaskUE-> canTaskBeSuggested Api");
        return inDoc;
    }

}
