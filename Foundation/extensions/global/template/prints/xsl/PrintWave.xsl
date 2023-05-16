<?xml version = "1.0" encoding = "UTF-8"?>
<!--
    Document    PrintWave.xsl
    Author      vinayb 
    Description
        Prepares the input of PrintDocumentSet API, to generate Wave Prints.
-->
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
	<xsl:output indent="yes"/>
	<xsl:template match="Print | Wave">
		<PrintDocuments>
			<xsl:attribute name="PrintName">
				<xsl:text>WavePrint</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="FlushToPrinter">
				<xsl:text>Y</xsl:text>
			</xsl:attribute>
			<PrintDocument>
				<xsl:attribute name="Localecode">
					<xsl:text>xml:/WaveList/Wave/Node/@Localecode</xsl:text>
				</xsl:attribute>
				<InputData>
					<xsl:attribute name="APIName">
						<xsl:text>getWaveList</xsl:text>
					</xsl:attribute>
					<Wave>
					<xsl:choose>
						<xsl:when test="name()=&quot;Print&quot;">
							<xsl:copy-of select="Wave/@*" /> 
						</xsl:when>
						<xsl:when test="name()=&quot;Wave&quot;">
							<xsl:copy-of select="@*" /> 
						</xsl:when>
					</xsl:choose>	
					</Wave>
						<Template>
							<WaveList>
							<Wave WaveKey="" WaveNo="">
								<Node NodeKey="" NodeOrgCode="" Localecode=""/>
							</Wave>
							</WaveList>
					</Template>
				</InputData>
			</PrintDocument>
			<PrintDocument>
				<xsl:attribute name="BeforeChildrenPrintDocumentId">
					<!--<xsl:text>TASKLIST</xsl:text> -->
				</xsl:attribute>
				<xsl:attribute name="DataElementPath">
					<xsl:text>xml:/Batch</xsl:text>
				</xsl:attribute>
				<xsl:choose>
					<xsl:when test="name()=&quot;Print&quot;">
					 <xsl:if test="not(Wave)">
						 <xsl:message terminate="yes">Wave Number is Mandatory
						 </xsl:message>
					 </xsl:if>
					 <xsl:copy-of select="PrinterPreference"/>
						<LabelPreference>
							<xsl:attribute name="EquipmentType">
								<xsl:text>xml:/Batch/@EquipmentType</xsl:text>
							</xsl:attribute>
							<xsl:copy-of select="LabelPreference/@*"/>
						</LabelPreference>
					</xsl:when>
					<xsl:when test="name()=&quot;Wave&quot;">
						<PrinterPreference>
							<xsl:attribute name="UsergroupId"/>
							<xsl:attribute name="UserId"/>
							<xsl:attribute name="WorkStationId"/>
							<xsl:attribute name="OrganizationCode"><xsl:text>xml:/Batch/@OrganizationCode</xsl:text></xsl:attribute>
						</PrinterPreference>
						<LabelPreference>
							<xsl:attribute name="Node">
								<xsl:text>xml:/Batch/@OrganizationCode</xsl:text>
							</xsl:attribute>
							<xsl:attribute name="EquipmentType">
								<xsl:text>xml:/Batch/@EquipmentType</xsl:text>
							</xsl:attribute>
						</LabelPreference>
					</xsl:when>
				</xsl:choose>
				<KeyAttributes>
					<KeyAttribute>
						<xsl:attribute name="Name"><xsl:text>BatchKey</xsl:text></xsl:attribute>
					</KeyAttribute>
				</KeyAttributes>
				<InputData>
					<xsl:attribute name="APIName">
						<xsl:text>getEquipmentTypeList</xsl:text>
					</xsl:attribute>
					<EquipmentType>
						<xsl:choose>
							<xsl:when test="name()=&quot;Print&quot;">
								<xsl:attribute name="Node"><xsl:value-of select="Wave/@Node" /> </xsl:attribute>
							</xsl:when>
							<xsl:when test="name()=&quot;Wave&quot;">
								<xsl:attribute name="Node"><xsl:value-of select="@Node" /></xsl:attribute>
							</xsl:when>
						</xsl:choose>	
					</EquipmentType>
					<Template>
						<EquipmentTypes>
							<EquipmentType>
								<xsl:attribute name="EquipmentType"/>
							</EquipmentType>
						</EquipmentTypes>
					</Template>
				<InputData>
					<xsl:attribute name="APIName">
						<xsl:text>getBatchList</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="ParentDataElement">
						<xsl:text>EquipmentType</xsl:text>
					</xsl:attribute>
					<Batch>
						<xsl:attribute name="EquipmentType"><xsl:text>@EquipmentType</xsl:text></xsl:attribute>
						<xsl:choose>
							<xsl:when test="name()=&quot;Print&quot;">
								<xsl:attribute name="WaveNo"><xsl:value-of select="Wave/@WaveNo" /> </xsl:attribute>
							<xsl:attribute name="Node"><xsl:value-of select="Wave/@Node" /> </xsl:attribute>	
							</xsl:when>
							<xsl:when test="name()=&quot;Wave&quot;">
								<xsl:attribute name="WaveNo"><xsl:value-of select="@WaveNo" /></xsl:attribute>
								<xsl:attribute name="Node"><xsl:value-of select="@Node" /> </xsl:attribute>
							</xsl:when>
						</xsl:choose>
						<Tasks> 
							<Task HeldForInventoryShortage="N"/>
						</Tasks>
					</Batch>	
					<Template>
						<BatchList>
							<Batch>
								<xsl:attribute name="BatchKey"/>
							</Batch>
						</BatchList>
					</Template>
					<InputData>
						<xsl:attribute name="FlowName">
							<xsl:text>GetTaskListData</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="ParentDataElement">Batch</xsl:attribute>
						<Batch>
							<xsl:attribute name="BatchKey">@BatchKey</xsl:attribute>
						</Batch>
                                                <SortAttributes>
						<xsl:attribute name="ListElement">BatchTasks</xsl:attribute>
						<xsl:attribute name="SortDirection">ASC</xsl:attribute>
						
							<Attribute>
								<xsl:attribute name="Name">SourceSortSequence</xsl:attribute>

								<xsl:attribute name="isNumeric">Y</xsl:attribute>
							</Attribute>	
				             </SortAttributes>
					<Template>
					 <Api>
						<xsl:attribute name="Name"><xsl:text>getBatchDetails</xsl:text></xsl:attribute>	
					  <Template>
						<Batch ActivityGroupId="" BatchKey="" ReceiptNo="" BatchNo="" BatchStatusDesc="" CountRequestNo="" EquipmentType="" MoveRequestNo="" Node="" OrganizationCode="" ShipmentNo="" Status="" TaskType="" WaveNo="">
						<EquipmentType>
							<EquipmentTypeDetails>
								<EquipmentTypeDetail/>
							</EquipmentTypeDetails>
						</EquipmentType>
						<BatchLocations>
							<BatchLocation CartLocationId="" SlotNumber="" ShipmentKey="" ShipmentContainerKey="">
								<Container ShipmentContainerKey="" ContainerNo="">
									<Corrugation ItemID=""/>
								</Container>
							</BatchLocation>
						</BatchLocations>
						<Tasks>
							<Task SourceLocationId="" SourceZoneId="" SourceSortSequence="" TargetZoneId="" TargetLocationId=""  TargetSortSequence="" OrganizationCode="" TaskStatus="" TargetLPNNo="">
								<Inventory SourceCaseId=""  SourcePalletId="" TargetCaseId="" TargetPalletId=""  ItemId="" UnitOfMeasure="" ProductClass="" Quantity="" TagNumber="" SerialNo="">
									<TagAttributes/>
									<Item ItemID="" UnitOfMeasure="">
										<PrimaryInformation Description=""/>
									</Item>
								</Inventory>
								<TaskReferences ReceiptNo="" WaveNo="" BatchNo="" MoveRequestNo="" ShipmentNo="" CountRequestNo="" ShipmentContainerKey="" ShipmentKey=""/>
								<BatchLocation/>
								<Shipment ShipmentKey="" ShipmentSortLocationId=""/>
							</Task>
						</Tasks>
						</Batch>
						</Template>
					 </Api>	
					</Template>
					</InputData>
				  </InputData>
				 </InputData> 
				<!-- <PrintDocuments>
					 <PrintDocument>
						<xsl:attribute name="BeforeChildrenPrintDocumentId">
							<xsl:text>CONTAINER_LABEL</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="BeforeChildrenLabelFormatId">
							<xsl:text>xml:/Container/@LabelFormatId</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="DataElementPath">
							<xsl:text>xml:/Container</xsl:text>
						</xsl:attribute>
						<PrinterPreference>
						<xsl:attribute name="UsergroupId"/>
						<xsl:attribute name="UserId"/>
						<xsl:attribute name="WorkStationId"><xsl:text>xml:/Container/@ContainerEquipment</xsl:text></xsl:attribute>
						<xsl:attribute name="OrganizationCode"><xsl:text>xml:/Container/Shipment/ShipNode/@ShipNode</xsl:text></xsl:attribute>
					</PrinterPreference>
					<LabelPreference>
						<xsl:attribute name="BuyerOrganizationCode">
							<xsl:text>xml:/Container/Shipment/@BuyerOrganizationCode</xsl:text>
						</xsl:attribute>
					</LabelPreference> 
					<InputData>
							<xsl:attribute name="FlowName">
								<xsl:text>GetShippingLabelData</xsl:text>
							</xsl:attribute>
							<xsl:attribute name="ParentDataElement">
								<xsl:text>BatchLocation</xsl:text>
							</xsl:attribute>
							
							<Container>
								<xsl:attribute name="GenerateContainerScm">Y</xsl:attribute>
								<xsl:attribute name="ShipmentContainerKey">
									<xsl:text>@ShipmentContainerKey</xsl:text>
								</xsl:attribute>
							</Container>
							<Template>
								<Api Name="getShipmentContainerDetails">
								<Template>	
									<Container  ContainerEquipment="" ContainerNo="" ContainerScm="" ContainerType=""  ShipmentContainerKey="" ShipmentKey=""  TrackingNo=""  Zone="" >
										<BatchLocation BatchNo="" CartLocationId="" SlotNumber="" />
										<ContainerDetails TotalNumberOfRecords="">
											<ContainerDetail ItemID="" Quantity="">
												<ShipmentLine ActualQuantity="" CustomerPoNo="" DepartmentCode="" ShipmentLineKey="" MarkForKey="">
													<OrderLine OrderLineKey="">
													    <Item ItemID="" CustomerItem=""/>
													</OrderLine>
													<MarkForAddress  Department=""/>
												</ShipmentLine>
											</ContainerDetail>
										</ContainerDetails>	
										<Shipment BuyerOrganizationCode="" ShipmentNo="" SCAC="" ProNo="" BolNo="" TrailerNo="">
											<ScacAndService CarrierType=""/>
											<ToAddress AddressLine1="" AddressLine2="" FirstName="" MiddleName="" LastName="" City="" State="" Country="" ZipCode="" />
											<FromAddress AddressLine1="" AddressLine2="" FirstName="" MiddleName="" LastName="" City="" State="" Country="" ZipCode="" />
											<MarkForAddress Department="" />
											<ShipNode ShipNode="" NodeOrgCode=""/>
										</Shipment>	
									</Container>
								  </Template>	
								</Api>
							</Template>
						</InputData>
					</PrintDocument> 
				</PrintDocuments>  -->
			</PrintDocument>
			<!-- <PrintDocument>
				<xsl:attribute name="BeforeChildrenPrintDocumentId">
					<xsl:text>TASKLIST</xsl:text>
				</xsl:attribute>
				<xsl:attribute name="DataElementPath">
					<xsl:text>xml:/Batch</xsl:text>
				</xsl:attribute>
				<PrinterPreference>
					<xsl:attribute name="UsergroupId"/>
					<xsl:attribute name="UserId"/>
					<xsl:attribute name="WorkStationId"/>
					<xsl:attribute name="OrganizationCode"><xsl:text>xml:/Batch/@OrganizationCode</xsl:text></xsl:attribute>
				</PrinterPreference>
				<LabelPreference>
					<xsl:attribute name="Node">
						<xsl:text>xml:/Batch/@OrganizationCode</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="EquipmentType">
						<xsl:text>xml:/Batch/@EquipmentType</xsl:text>
					</xsl:attribute>
				</LabelPreference>
				<KeyAttributes>
					<KeyAttribute>
						<xsl:attribute name="Name"><xsl:text>BatchKey</xsl:text></xsl:attribute>
					</KeyAttribute>
				</KeyAttributes>
				<InputData>
					<xsl:attribute name="APIName">
						<xsl:text>getBatchList</xsl:text>
					</xsl:attribute>
					<Batch>
						<xsl:attribute name="EquipmentTypeQryType">
							<xsl:text>VOID</xsl:text>
						</xsl:attribute>
						<xsl:choose>
							<xsl:when test="name()=&quot;Print&quot;">
								<xsl:attribute name="WaveNo"><xsl:value-of select="Wave/@WaveNo" /> </xsl:attribute>
							<xsl:attribute name="Node"><xsl:value-of select="Wave/@Node" /> </xsl:attribute>
							</xsl:when>
							<xsl:when test="name()=&quot;Wave&quot;">
								<xsl:attribute name="WaveNo"><xsl:value-of select="@WaveNo" /></xsl:attribute>
								<xsl:attribute name="Node"><xsl:value-of select="@Node" /> </xsl:attribute>
							</xsl:when>
						</xsl:choose>
						<Tasks> 
							<Task HeldForInventoryShortage="N"/>
						</Tasks>
					</Batch>	
					<Template>
						<BatchList>
							<Batch>
								<xsl:attribute name="BatchKey"/>
							</Batch>
						</BatchList>
					</Template>
					<InputData>
						<xsl:attribute name="FlowName">
							<xsl:text>GetTaskListData</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="ParentDataElement">Batch</xsl:attribute>
						<Batch>
							<xsl:attribute name="BatchKey">@BatchKey</xsl:attribute>
						</Batch>
					<SortAttributes>
						<xsl:attribute name="ListElement">BatchTasks</xsl:attribute>
						<xsl:attribute name="SortDirection">ASC</xsl:attribute>
						
							<Attribute>
								<xsl:attribute name="Name">SourceSortSequence</xsl:attribute>
								<xsl:attribute name="isNumeric">Y</xsl:attribute>
							</Attribute>	
				        </SortAttributes>
					<Template>
					 <Api>
						<xsl:attribute name="Name"><xsl:text>getBatchDetails</xsl:text></xsl:attribute>	
					  <Template>
						<Batch ActivityGroupId="" BatchKey="" ReceiptNo="" BatchNo="" BatchStatusDesc="" CountRequestNo="" EquipmentType="" MoveRequestNo="" Node="" OrganizationCode="" ShipmentNo="" Status="" TaskType="" WaveNo="">
						<EquipmentType>
							<EquipmentTypeDetails>
								<EquipmentTypeDetail/>
							</EquipmentTypeDetails>
						</EquipmentType>
						<BatchLocations>
							<BatchLocation CartLocationId="" SlotNumber="" ShipmentKey="" ShipmentContainerKey="">
								<Container ShipmentContainerKey="" ContainerNo="">
									<Corrugation ItemID=""/>
								</Container>
							</BatchLocation>
						</BatchLocations>
						<Tasks>
							<Task SourceLocationId="" SourceZoneId="" SourceSortSequence="" TargetZoneId="" TargetLocationId=""  TargetSortSequence="" OrganizationCode="" TaskStatus="" TargetLPNNo="">
								<Inventory SourceCaseId=""  SourcePalletId="" TargetCaseId="" TargetPalletId=""  ItemId="" UnitOfMeasure="" ProductClass="" Quantity="" TagNumber="" SerialNo="">
									<TagAttributes/>
									<Item ItemID="" UnitOfMeasure="">
										<PrimaryInformation Description=""/>
									</Item>
								</Inventory>
								<TaskReferences ReceiptNo="" WaveNo="" BatchNo="" MoveRequestNo="" ShipmentNo="" CountRequestNo="" ShipmentContainerKey="" ShipmentKey=""/>
								<BatchLocation/>
								<Shipment ShipmentKey="" ShipmentSortLocationId=""/>
							</Task>
						</Tasks>
						</Batch>
						</Template>
					 </Api>	
					</Template>
					</InputData>
				</InputData>
				 <PrintDocuments>
					<PrintDocument>
						<xsl:attribute name="BeforeChildrenPrintDocumentId">
							<xsl:text>CONTAINER_LABEL</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="BeforeChildrenLabelFormatId">
							<xsl:text>xml:/Container/@LabelFormatId</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="DataElementPath">
							<xsl:text>xml:/Container</xsl:text>
						</xsl:attribute>
					<PrinterPreference>
						<xsl:attribute name="UsergroupId"/>
						<xsl:attribute name="UserId"/>
						<xsl:attribute name="WorkStationId"><xsl:text>xml:/Container/@ContainerEquipment</xsl:text></xsl:attribute>
						<xsl:attribute name="OrganizationCode"><xsl:text>xml:/Container/Shipment/ShipNode/@ShipNode</xsl:text></xsl:attribute>
					</PrinterPreference>
					<LabelPreference>
						<xsl:attribute name="BuyerOrganizationCode">
							<xsl:text>xml:/Container/Shipment/@BuyerOrganizationCode</xsl:text>
						</xsl:attribute>
					</LabelPreference> 
					<InputData>
							<xsl:attribute name="FlowName">
								<xsl:text>GetShippingLabelData</xsl:text>
							</xsl:attribute>
							<xsl:attribute name="ParentDataElement">
								<xsl:text>BatchContainer</xsl:text>
							</xsl:attribute>
							
							<Container>
								<xsl:attribute name="GenerateContainerScm">Y</xsl:attribute>
								<xsl:attribute name="ShipmentContainerKey">
									<xsl:text>@ShipmentContainerKey</xsl:text>
								</xsl:attribute>
							</Container>
							<Template>
								<Api Name="getShipmentContainerDetails">
								<Template>	
								<Container  ContainerEquipment="" ContainerNo="" ContainerScm="" ContainerType=""  ShipmentContainerKey="" ShipmentKey=""  TrackingNo=""  Zone="" >
									<BatchLocation BatchNo="" CartLocationId="" SlotNumber="" />
									<ContainerDetails TotalNumberOfRecords="">
										<ContainerDetail ItemID="" Quantity="">
											<ShipmentLine ActualQuantity="" CustomerPoNo="" DepartmentCode="" ShipmentLineKey="" MarkForKey="">
												<OrderLine OrderLineKey="">
												    <Item ItemID="" CustomerItem=""/>
												</OrderLine>
												<MarkForAddress  Department=""/>
											</ShipmentLine>
										</ContainerDetail>
									</ContainerDetails>	
									<Shipment BuyerOrganizationCode="" ShipmentNo="" SCAC="" ProNo="" BolNo="" TrailerNo="">
										<ScacAndService CarrierType=""/>
										<ToAddress AddressLine1="" AddressLine2="" FirstName="" MiddleName="" LastName="" City="" State="" Country="" ZipCode="" />
										<FromAddress AddressLine1="" AddressLine2="" FirstName="" MiddleName="" LastName="" City="" State="" Country="" ZipCode="" />
										<MarkForAddress Department="" />
										<ShipNode ShipNode="" NodeOrgCode=""/>
									</Shipment>	
								</Container>
								  </Template>	
								</Api>
							</Template>
						</InputData>
					</PrintDocument>
				</PrintDocuments> 
			</PrintDocument> -->
			<PrintDocument>
				<xsl:attribute name="DataElementPath"><xsl:text>xml:/Shipment</xsl:text></xsl:attribute>
				<InputData>
					<xsl:attribute name="APIName">
						<xsl:text>getShipmentList</xsl:text>
					</xsl:attribute>
						<!-- Input Data Element -->
					<Shipment>
					<xsl:attribute name="IgnoreOrdering"><xsl:text>Y</xsl:text></xsl:attribute>
						<xsl:choose>
							<xsl:when test="name()=&quot;Print&quot;">
								<xsl:attribute name="ShipNode"><xsl:value-of select="Wave/@Node" /> </xsl:attribute>
							</xsl:when>
							<xsl:when test="name()=&quot;Wave&quot;">
								<xsl:attribute name="ShipNode"><xsl:value-of select="@Node" /></xsl:attribute>
							</xsl:when>
						</xsl:choose>
					   <ShipmentLines>
						<ShipmentLine>
							<xsl:choose>
								<xsl:when test="name()=&quot;Print&quot;">
									<xsl:attribute name="WaveNo"><xsl:value-of select="Wave/@WaveNo" /></xsl:attribute>
								</xsl:when>
								<xsl:when test="name()=&quot;Wave&quot;">
									<xsl:attribute name="WaveNo"><xsl:value-of select="@WaveNo" /></xsl:attribute> 
								</xsl:when>
							</xsl:choose>	
						</ShipmentLine>
					   </ShipmentLines>
					</Shipment>
					<Template>
						<Shipments>
							<Shipment ShipmentKey=""/>
						</Shipments>	
					</Template>	
				</InputData>
			<PrintDocuments>	
				<xsl:attribute name="FlushToPrinter"><xsl:text>N</xsl:text></xsl:attribute>
				<xsl:attribute name="PrintAfresh"><xsl:text>N</xsl:text></xsl:attribute>
				<PrintDocument>
						<xsl:attribute name="BeforeChildrenPrintDocumentId">
							<xsl:text>PACKLIST</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="DataElementPath">
							<xsl:text>xml:/Shipment</xsl:text>
						</xsl:attribute>
						<PrinterPreference>
							<xsl:attribute name="UsergroupId"/>
							<xsl:attribute name="UserId"/>
							<xsl:attribute name="WorkStationId"/>
							<xsl:attribute name="OrganizationCode"><xsl:text>xml:/Shipment/ShipNode/@NodeOrgCode</xsl:text></xsl:attribute>
						</PrinterPreference>
						<LabelPreference>
							<xsl:attribute name="EnterpriseCode">
								<xsl:text>xml:/Shipment/@EnterpriseCode</xsl:text>
							</xsl:attribute>
							<xsl:attribute name="BuyerOrganizationCode">
								<xsl:text>xml:/Shipment/@BuyerOrganizationCode</xsl:text>
							</xsl:attribute>
							<xsl:attribute name="SellerOrganizationCode">
								<xsl:text>xml:/Shipment/@SellerOrganizationCode</xsl:text>
							</xsl:attribute>
						</LabelPreference>
						<KeyAttributes>
							<KeyAttribute>
								<xsl:attribute name="Name"><xsl:text>ShipmentKey</xsl:text></xsl:attribute>
							</KeyAttribute>
						</KeyAttributes>
							<InputData>
							<xsl:attribute name="FlowName">
								<xsl:text>GetPackListData</xsl:text>
							</xsl:attribute>
							<xsl:attribute name="ParentDataElement">
								<xsl:text>Shipment</xsl:text>
							</xsl:attribute>
							<!-- Input Data Element -->
							<Shipment>
								<xsl:attribute name="ShipmentKey"><xsl:text>@ShipmentKey</xsl:text></xsl:attribute>
							</Shipment>
							<Template>
							<Api Name="getShipmentDetails">
							<Template>	
								<Shipment ShipmentKey="" ShipmentNo="" ActualShipmentDate="" ExpectedShipmentDate="">
									<SellerOrganization OrganizationCode="">
										<CorporatePersonInfo AddressLine1="" AddressLine2="" FirstName="" MiddleName="" LastName="" City="" State="" Country="" ZipCode="" />
									</SellerOrganization>
									<Carrier Scac="" ScacDesc=""/>
									<MarkForAddress/>
									<BillingInformation ShipmentChargeType=""/>
									<Instructions>
										<Instruction InstructionType="" InstructionText=""/>
									</Instructions>
									<ToAddress/>
									<ShipmentLines>
										<ShipmentLine ActualQuantity="" ItemDesc="" ItemID="" OrderHeaderKey="" OrderLineKey="" OrderNo="" OrderReleaseKey="" PrimeLineNo=""  Quantity="" ReleaseNo="" ShipmentKey="" ShipmentLineKey="" ShipmentLineNo="" SubLineNo="" UnitOfMeasure="" BackOrderedQty="" ShipmentSubLineNo="">
											<Order OrderHeaderKey="" OrderNo="">
												<PersonInfoBillTo AddressLine1="" AddressLine2="" FirstName="" MiddleName="" LastName="" City="" State="" Country="" ZipCode=""  />
											</Order>
											<OrderLine  CustomerPONo=""  OrderLineKey="" OrderedQty="" OriginalOrderedQty=""   Status="" StatusQuantity="" SubLineNo="" >
												<Item CustomerItem=""/>
												<OrderStatuses>
													 <OrderStatus OrderLineKey="" OrderReleaseStatusKey=""  Status=""  StatusQty="" TotalQuantity=""/>
												  </OrderStatuses>
											</OrderLine>		
										</ShipmentLine>
									</ShipmentLines>
									<ShipNode NodeOrgCode=""/>
								</Shipment>
							</Template>
							  </Api>	
							  </Template>	
						</InputData>
					</PrintDocument>
				</PrintDocuments>
			</PrintDocument>
		</PrintDocuments>
	</xsl:template>
</xsl:stylesheet>