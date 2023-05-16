package com.academy.ecommerce.yantriks.general;


import org.w3c.dom.Document;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyLocationOnboardingToYantriks {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyLocationOnboardingToYantriks.class);

	/**
	 * OMNI-22625 -  This method is to post a message to the queue if new organization is
	 * created in OMS OR if any details/parameters are modified in the existing
	 * organization
	 * 
	 * @param env
	 * @param inDoc
	 * @return inDoc
	 * @throws Exception
	 */

	public Document publishLocationOnboardingDetailsToQ(YFSEnvironment env, Document inDoc) throws Exception {

		String methodName = "publishLocationOnboardingDetailsToQ";
		log.beginTimer(methodName);
		log.verbose("Input to  " + methodName + " : " + SCXmlUtil.getString(inDoc));
		/*Fetching the environment object which is set if any of the ExtnIsBopisEnabled, ExtnIsDSVEnabled, ExtnIsSFSEnabled or ExtnIsSTSEnabled Flags are updated*/
		String isFlagUpdated = (String) env.getTxnObject("isFlagUpdated");
		log.verbose("Value of isFlagUpdated: "+isFlagUpdated);
		/**
		 * Sample Organization creation XML to be published to queue 
		 * <Organization BusinessCalendarKey=""
		 * CallingOrganizationCode="DEFAULT" ContextOrganizationCode="DEFAULT"
		 * CreatorOrganizationKey="DEFAULT" DefaultPriceProgramKey=""
		 * IsHubOrganization="N" IsNode="Y"IsSeller="N" LocaleCode="Cental Time2"
		 * ModifyFlag="Y" Operation="Create" OrganizationCode="12346"
		 * OrganizationName="Store 12346" OwnerOrganizationCode="DEFAULT"
		 * ParentOrganizationCode="DEFAULT"
		 * ParentOrganizationCode_OrganizationCode="DEFAULT"
		 * PrimaryEnterpriseKey="DEFAULT" ResourceIDForMasterData="YCPPM212"
		 * USE_DEPRECATED_PRICING_ENGINE="Y">
		 * <Node Description="12346" IsFulfillmentNode="Y" IsYard="N" NodeType="Store"
		 * ShipNode="12346" Shipnode="12346" ShipnodeKey="12346"/>
		 * <CorporatePersonInfo AddressLine1="ABC Street1 Rd" AddressLine2=" "
		 * AddressLine3=" " AddressLine4=" " AddressLine5=" " AddressLine6=" "
		 * AlternateEmailID=" " Beeper=" " City="Houston" Company=" " Country="US"
		 * Country_CodeShortDescription="US" DayFaxNo=" " DayPhone=" " Department=" "
		 * DisplayCoordinates="Y" EMailID=" " ErrorTxt=" " EveningFaxNo=" " EveningPhone
		 * =" " FirstName="Store 12346" HttpUrl=" " JobTitle=" " LastName=" " MiddleName
		 * =" " MobilePhone=" " OtherPhone=" " PersonID=" " PreferredShipAddress=" "
		 * ScreenTitle="Address" SelectedTab="0" State="TX" Suffix=" " Title=" "
		 * UseCount=" " VerificationStatus=" " ZipCode="77063"/> <EnterpriseOrgList>
		 * <OrgEnterprise EnterpriseOrganizationKey="DEFAULT" OrganizationKey=""/>
		 * </EnterpriseOrgList> <OrgRoleList>
		 * <OrgRole OrganizationKey="12346" RoleKey="NODE"/> </OrgRoleList>
		 * </Organization>
		 */
		
		/*Posting the msg to the queue for new organization creation and modification to the existing organization other than the updation of the EXTN flags*/
		if (YFCCommon.isVoid(isFlagUpdated)) {
			log.verbose("Publish Location Onboarding Details to the Queue");
			publishMessageToQ(env, inDoc);
			log.verbose("Return from the class after publishing the location onboarding details to the queue");
		}
		else{
			log.verbose("Extn Flag is updated , hence not publishing location onboarding details to the queue");
		}
		log.endTimer(methodName);
		return inDoc;

	}


	private void publishMessageToQ(YFSEnvironment env, Document docInput) throws Exception {
		/*Invoke AcademyYantriksKafkaDeltaUpdate Service to insert the record into the  Queue*/
		AcademyUtil.invokeService(env, AcademyConstants.SERVICE_YANTRIKS_KAFKA_DELTA_UPDATE, docInput);
	}




}