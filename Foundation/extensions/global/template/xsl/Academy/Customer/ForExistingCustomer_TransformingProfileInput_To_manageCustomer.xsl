<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:template match="/AcademyMergedDocument">
		<xsl:element name="Customer">
			<xsl:attribute name="CustomerType">
				<xsl:text>02</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="ExternalCustomerID">
				<xsl:value-of
					select="EnvironmentDocument/Customer/@ExternalCustomerID" />
			</xsl:attribute>
			<xsl:attribute name="CustomerKey">
				<xsl:value-of
					select="InputDocument/CustomerList/Customer/@CustomerKey" />
			</xsl:attribute>
			<xsl:attribute name="RegisteredDate">
				<xsl:value-of
					select="EnvironmentDocument/Customer/@RegisteredDate" />
			</xsl:attribute>
			<xsl:attribute name="SyncTS">
				<xsl:value-of
					select="EnvironmentDocument/Customer/@SyncTS" />
			</xsl:attribute>

			<!--  checking the Synch Time stamp for existed Customer Profiles - for doing the Updation based on Timestmaps -->
			<xsl:variable name="webSynchTS"
				select="EnvironmentDocument/Customer/@SyncTS" />
			<xsl:variable name="existedSynchTS"
				select="InputDocument/CustomerList/Customer/@SyncTS" />

			<xsl:variable name="TransLate_existedSynchTS"
				select="translate(translate(translate($existedSynchTS,'-',''),'T',''),':','')" />
			<!--  <xsl:variable name="TransLate_webSynchTS"
				select="translate(translate(translate($webSynchTS,'-',''),'T',''),':','')" />  -->
<xsl:variable name="TransLate_webSynchTS" select="translate(translate(translate(translate(translate($webSynchTS,'-',''),'T',''),':',''),' ',''),'.','')" />
			<xsl:attribute name="CheckSyncTimeStamp">
				<xsl:choose>
				<xsl:when test="(normalize-space($TransLate_existedSynchTS) ='')">
                                       <xsl:value-of select="'Update'" />
                                    </xsl:when>
                                 
					<xsl:when
						test="$TransLate_webSynchTS >= $TransLate_existedSynchTS">
						<xsl:value-of select="'Update'" />
					</xsl:when>

					<xsl:otherwise>
						<xsl:value-of select="'Ignore'" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<!-- End of setting the new Attribute to indicate to invoke manangeCustomer Call -->
			
			<xsl:attribute name="OrganizationCode">
				<xsl:text>Academy_Direct</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="Operation">
				<xsl:text>Manage</xsl:text>
			</xsl:attribute>
			<Extn>
				<xsl:attribute name="ExtnTaxExemptID">
					<xsl:value-of
						select="EnvironmentDocument/Customer/Extn/@ExtnTaxExemptId" />
				</xsl:attribute>
			</Extn>
			<xsl:variable name="varExistingWebEmailId"
				select="InputDocument/CustomerList/Customer/CustomerContactList/CustomerContact/@EmailID" />
			<xsl:variable name="varInputWebEmailId"
				select="EnvironmentDocument/Customer/CustomerContactList/CustomerContact/@EmailID" />
			<xsl:element name="CustomerContactList">
				<xsl:choose>
					<xsl:when
						test="$varExistingWebEmailId = $varInputWebEmailId">
						<xsl:copy-of
							select="EnvironmentDocument/Customer/CustomerContactList/CustomerContact/." />

					</xsl:when>
					<xsl:otherwise>



						<xsl:element name="CustomerContact">
							<xsl:attribute name="Operation">
								<xsl:text>Delete</xsl:text>
							</xsl:attribute>
							<xsl:attribute name="CustomerContactID">
								<xsl:value-of
									select="InputDocument/CustomerList/Customer/CustomerContactList/CustomerContact/@CustomerContactID" />
							</xsl:attribute>
							<xsl:attribute name="EmailID">
								<xsl:value-of
									select="$varExistingWebEmailId" />
							</xsl:attribute>
						</xsl:element>


						<xsl:copy-of
							select="EnvironmentDocument/Customer/CustomerContactList/CustomerContact/." />

					</xsl:otherwise>
				</xsl:choose>
			</xsl:element>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>
