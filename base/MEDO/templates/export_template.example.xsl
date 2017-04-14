<!--

      Licensed to the Apache Software Foundation (ASF) under one or more
      contributor license agreements.  See the NOTICE file distributed with
      this work for additional information regarding copyright ownership.
      The ASF licenses this file to you under the Apache License, Version 2.0
      (the "License"); you may not use this file except in compliance with
      the License.  You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.

-->
﻿<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
      xmlns:xdms="http://www.infpres.com/IEDMS"
	  >

 	<xsl:import href="mapping.xsl"/>
    <xsl:output method="xml" indent="yes" encoding="windows-1251" cdata-section-elements="xdms:eds"/>

	<xsl:template match="/card">
		<xdms:communication xdms:version="2.0">	
			
			<xdms:header> <!-- Обязательный; attribute name="type" - обязательный: \in{"Документ", "Квитанция о регистрации", "Уведомление"} -->
				<xsl:attribute name="xdms:uid"> <!-- Обязательный -->
					<xsl:value-of select="attribute[@code='${header.uid}']/value" />
				</xsl:attribute>
				
				<xsl:attribute name="xdms:type"> <!-- Обязательный -->
					<xsl:call-template name="headerType" />
				</xsl:attribute>
				
				<xdms:source> <!-- Обязательный -->
					<xsl:call-template name="communicationPartner">
						<xsl:with-param name="param_organization" select="'${header.source.organization}'"/>
						<xsl:with-param name="param_comment" select="'${header.source.comment}'" />
						<xsl:with-param name="param_uid_attribute" select="'${header.source.uid}'" />
					</xsl:call-template>
				</xdms:source>
				
				<xsl:call-template name="paste-non-empty"> <!--xdms:operator-->
					<xsl:with-param name="param_element">
						<xdms:operator> <!-- Необязательный -->
							<xsl:value-of select="attribute[@code='${header.operator}']/value" />
						</xdms:operator>
					</xsl:with-param>
				</xsl:call-template>
				
				<xsl:call-template name="paste-non-empty"> <!--xdms:comment-->
					<xsl:with-param name="param_element">
						<xdms:comment> <!-- Необязательный -->
							<xsl:value-of select="attribute[@code='${header.comment}']/value" />
						</xdms:comment>
					</xsl:with-param>
				</xsl:call-template>
			</xdms:header>
			
			<xsl:call-template name="typeElement" />
				
			<!--<xsl:variable name="postingElement">
				<xdms:posting>--> <!-- Необязательный -->
<!--					<xdms:destination> --> <!-- Обязательный; attribute name="id" - необязательный; attribute name="retro" - необязательный; attribute name="modified" - необязательный; -->
<!--						<xsl:call-template name="deliveryDestination">
							<xsl:with-param name="param_organization" select="$var_posting_organization" />
							<xsl:with-param name="param_region" select="$var_posting_region" />
							<xsl:with-param name="param_system" select="$var_posting_system" />
							<xsl:with-param name="param_details" select="$var_posting_details" />
							<xsl:with-param name="param_files" select="$var_posting_files" />
						</xsl:call-template>
					</xdms:destination>
				</xdms:posting>
			</xsl:variable>
			<xsl:call-template name="paste-non-empty">
				<xsl:with-param name="param_element" select="$postingElement" />
			</xsl:call-template>-->
		</xdms:communication>			
	</xsl:template>
	
	<xsl:template name="document_files">			
					<xdms:document> <!-- Обязательный; attribute name="uid" - обязательный; attribute name="id" - необязательный; -->
						<xsl:attribute name="xdms:uid"> <!-- Обязательный -->
							<xsl:value-of select="attribute[@code='${document.attrUid}']/value" />
						</xsl:attribute>
						<xsl:variable name="kindElement">
							<xdms:kind> <!-- Необязательный; attribute name="id" - необязательный; attribute name="retro" - необязательный; attribute name="modified" - необязательный;-->
								<xsl:call-template name="classifyItemAttrs">
									<xsl:with-param name="param_id" select="'${document.kind.attrId}'"/>
									<xsl:with-param name="param_retro" select="'${document.kind.attrRetro}'"/>
									<xsl:with-param name="param_modified" select="'${document.kind.attrModified}'"/>
								</xsl:call-template>
								<xsl:value-of select="attribute[@code='${document.kind}']/value/@description" />
							</xdms:kind>
						</xsl:variable>
						<xsl:call-template name="paste-non-empty"> <!--xdms:kind-->
							<xsl:with-param name="param_element" select="$kindElement" />
						</xsl:call-template>
						
						<xdms:num> <!-- Обязательный -->
							<xsl:call-template name="documentNumber" >
								<xsl:with-param name="param_number" select="'${document.num.number}'" />
								<xsl:with-param name="param_date" select="'${document.num.date}'" />
							</xsl:call-template>
						</xdms:num>
						
						<xsl:variable name="classificationElement">
							<xdms:classification> <!-- Необязательный; attribute name="id" - необязательный; attribute name="retro" - необязательный; attribute name="modified" - необязательный;-->
								<xsl:call-template name="classifyItemAttrs">
									<xsl:with-param name="param_id" select="'${document.classification.attrId}'"/>
									<xsl:with-param name="param_retro" select="'${document.classification.attrRetro}'"/>
									<xsl:with-param name="param_modified" select="'${document.classification.attrModified}'"/>
								</xsl:call-template>
								<xsl:value-of select="attribute[@code='${document.classification}']/value" />
							</xdms:classification>
						</xsl:variable>
						
						<xsl:call-template name="paste-non-empty"> <!--xdms:classification-->
							<xsl:with-param name="param_element" select="$classificationElement" />
						</xsl:call-template>

						<xsl:variable name="urgencyElement">
							<xdms:urgency> <!-- Необязательный; attribute name="id" - необязательный; attribute name="retro" - необязательный; attribute name="modified" - необязательный;-->
								<xsl:call-template name="classifyItemAttrs">
									<xsl:with-param name="param_id" select="'${document.urgency.attrId}'"/>
									<xsl:with-param name="param_retro" select="'${document.urgency.attrRetro}'"/>
									<xsl:with-param name="param_modified" select="'${document.urgency.attrModified}'"/>
								</xsl:call-template>
								<xsl:value-of select="attribute[@code='${document.urgency}']/value" />
							</xdms:urgency>
						</xsl:variable>
						
						<xsl:call-template name="paste-non-empty"> <!--xdms:urgency-->
							<xsl:with-param name="param_element" select="$urgencyElement" />
						</xsl:call-template>
						
						<xsl:call-template name="paste-non-empty"> <!--xdms:insteadOfDistributed-->
							<xsl:with-param name="param_element" >
								<xdms:insteadOfDistributed> <!-- Необязательный -->
									<xsl:value-of select="attribute[@code='${document.insteadOfDistributed}']/value" />
								</xdms:insteadOfDistributed>
							</xsl:with-param>
						</xsl:call-template>
						
						<xdms:signatories> <!-- Обязательный;  -->
							<xdms:signatory> <!-- Обязательный; -->
								<xsl:call-template name="signatory">
									<xsl:with-param name="param_region" select="'${document.signatories.signatory.region}'" />
									<xsl:with-param name="param_organization" select="'${document.signatories.signatory.organization}'" />
									<xsl:with-param name="param_person" select="'${document.signatories.signatory.person}'" /> 
									<xsl:with-param name="param_department" select="'${document.signatories.signatory.department}'" />
									<xsl:with-param name="param_post" select="'${document.signatories.signatory.post}'" />
									<xsl:with-param name="param_contactInfo" select="'${document.signatories.signatory.contactInfo}'" />
									<xsl:with-param name="param_signed" select="'${document.signatories.signatory.signed}'" /> 
									<xsl:with-param name="param_comment" select="'${document.signatories.signatory.comment}'" />
								</xsl:call-template>
							</xdms:signatory>
						</xdms:signatories>
						
						<xdms:addressees> <!-- Обязательный -->
							<xdms:addressee> <!-- Обязательный -->
								<xsl:call-template name="addressee">
									<xsl:with-param name="param_region" select="'${document.addressees.addressee.region}'" />
									<xsl:with-param name="param_organization" select="'${document.addressees.addressee.organization}'" />
									<xsl:with-param name="param_person" select="'${document.addressees.addressee.person}'" />
									<xsl:with-param name="param_department" select="'${document.addressees.addressee.department}'" />
									<xsl:with-param name="param_post" select="'${document.addressees.addressee.post}'" />
									<xsl:with-param name="param_contactInfo" select="'${document.addressees.addressee.contactInfo}'" />
									<xsl:with-param name="param_comment" select="'${document.addressees.addressee.comment}'" />
								</xsl:call-template>
							</xdms:addressee>
						</xdms:addressees>
						
						<xdms:pages> <!-- Обязательный -->
							<xsl:value-of select="attribute[@code='${document.pages}']/value" />
						</xdms:pages>
						
						<xsl:call-template name="paste-non-empty"> <!--xdms:enclosuresPages-->
							<xsl:with-param name="param_element">
								<xdms:enclosuresPages> <!-- Необязательный -->
									<xsl:value-of select="attribute[@code='${document.enclosuresPages}']/value" />
								</xdms:enclosuresPages>
							</xsl:with-param>
						</xsl:call-template>
						
						<xdms:annotation> <!-- Обязательный только узел, значение не обязательно -->
							<xsl:value-of select="attribute[@code='${document.annotation}']/value" />
						</xdms:annotation>
						
						<xsl:variable name="enclosuresElement"> <!--xdms:enclosures-->
							<xdms:enclosures> <!-- Необязательный; -->
								<xdms:enclosure>
									<xsl:call-template name="enclosure">
										<xsl:with-param name="param_title" select="'${document.enclosures.enclosure.title}'" />
										<xsl:with-param name="param_reference_region" select="'${document.enclosures.enclosure.reference.region}'" />
										<xsl:with-param name="param_reference_organization" select="'${document.enclosures.enclosure.reference.organization}'" />
										<xsl:with-param name="param_reference_person" select="'${document.enclosures.enclosure.reference.person}'" />
										<xsl:with-param name="param_reference_department" select="'${document.enclosures.enclosure.reference.department}'" />
										<xsl:with-param name="param_reference_post" select="'${document.enclosures.enclosure.reference.post}'" />
										<xsl:with-param name="param_reference_number" select="'${document.enclosures.enclosure.reference.num.number}'" />
										<xsl:with-param name="param_reference_date" select="'${document.enclosures.enclosure.reference.num.date}'" />
										<xsl:with-param name="param_reference_comment" select="'${document.enclosures.enclosure.reference.comment}'" />
										<xsl:with-param name="param_pages" select="'${document.enclosures.enclosure.pages}'" />
										<xsl:with-param name="param_files" select="'${document.enclosures.enclosure.files}'" />
										<xsl:with-param name="param_comment" select="'${document.enclosures.enclosure.comment}'" />
										<xsl:with-param name="param_reference_attr_region_id" select="'${document.enclosures.enclosure.reference.attrRegion.id}'" />
										<xsl:with-param name="param_reference_attr_region_retro" select="'${document.enclosures.enclosure.reference.attrRegion.retro}'" />
										<xsl:with-param name="param_reference_attr_region_modified" select="'${document.enclosures.enclosure.reference.attrRegion.modified}'" />
										<xsl:with-param name="param_reference_attr_organization_id" select="'${document.enclosures.enclosure.reference.attrOrganization.id}'" />
										<xsl:with-param name="param_reference_attr_organization_retro" select="'${document.enclosures.enclosure.reference.attrOrganization.retro}'" />
										<xsl:with-param name="param_reference_attr_organization_modified" select="'${document.enclosures.enclosure.reference.attrOrganization.modified}'" />
										<xsl:with-param name="param_reference_attr_person_id" select="'${document.enclosures.enclosure.reference.attrPerson.id}'" />
										<xsl:with-param name="param_reference_attr_person_retro" select="'${document.enclosures.enclosure.reference.attrPerson.retro}'" />
										<xsl:with-param name="param_reference_attr_person_modified" select="'${document.enclosures.enclosure.reference.attrPerson.modified}'" />
										<xsl:with-param name="param_reference_attr_department_id" select="'${document.enclosures.enclosure.reference.attrDepartment.id}'" />
										<xsl:with-param name="param_reference_attr_department_retro" select="'${document.enclosures.enclosure.reference.attrDepartment.retro}'" />
										<xsl:with-param name="param_reference_attr_department_modified" select="'${document.enclosures.enclosure.reference.attrDepartment.modified}'" />
										<xsl:with-param name="param_reference_attr_post_id" select="'${document.enclosures.enclosure.reference.attrPost.id}'" />
										<xsl:with-param name="param_reference_attr_post_retro" select="'${document.enclosures.enclosure.reference.attrPost.retro}'" />
										<xsl:with-param name="param_reference_attr_post_modified" select="'${document.enclosures.enclosure.reference.attrPost.modified}'" />
									</xsl:call-template>
								</xdms:enclosure>
							</xdms:enclosures>
						</xsl:variable>
						<xsl:call-template name="paste-non-empty">
							<xsl:with-param name="param_element" select="$enclosuresElement" />
						</xsl:call-template>
						
						<xsl:variable name="correspondentsElement"> <!--xdms:correspondents-->
							<xdms:correspondents> <!-- Необязательный -->
								<xdms:correspondent>
									<xsl:call-template name="correspondent">
										<xsl:with-param name="param_region" select="'${document.correspondents.correspondent.region}'" />
										<xsl:with-param name="param_organization" select="'${document.correspondents.correspondent.organization}'" />
										<xsl:with-param name="param_person" select="'${document.correspondents.correspondent.person}'" />
										<xsl:with-param name="param_department" select="'${document.correspondents.correspondent.department}'" />
										<xsl:with-param name="param_post" select="'${document.correspondents.correspondent.post}'" />
										<xsl:with-param name="param_contactInfo" select="'${document.correspondents.correspondent.contactInfo}'" />
										<xsl:with-param name="param_number" select="'${document.correspondents.correspondent.num.number}'" />
										<xsl:with-param name="param_date" select="'${document.correspondents.correspondent.num.date}'" />
										<xsl:with-param name="param_comment" select="'${document.correspondents.correspondent.comment}'" />
									</xsl:call-template>
								</xdms:correspondent>
							</xdms:correspondents>
						</xsl:variable>
						<xsl:call-template name="paste-non-empty">
							<xsl:with-param name="param_element" select="$correspondentsElement" />
						</xsl:call-template>
		<xsl:variable name="linksElement"> <!--xdms:links-->
		<xdms:links> <!-- Необязательный -->
		<xdms:link>					
									<xsl:call-template name="linkedDocument">
										<xsl:with-param name="param_attr_uid" select="'${document.links.link.uid}'"/>
										<xsl:with-param name="param_link_type" select="'${document.links.link.linkType}'"/>
										<xsl:with-param name="param_number_document" select="'${document.links.link.document.num.number}'"/>
										<xsl:with-param name="param_date_document" select="'${document.links.link.document.num.date}'"/>
										<xsl:with-param name="param_kind" select="'${document.links.link.document.kind}'"/>		
										<xsl:with-param name="param_organization" select="'${document.links.link.document.correspondents.correspondent.organization}'"/>
										<xsl:with-param name="param_region" select="'${document.links.link.document.correspondents.correspondent.region}'"/>
										<xsl:with-param name="param_person" select="'${document.links.link.document.correspondents.correspondent.person}'"/>
										<xsl:with-param name="param_department" select="'${document.links.link.document.correspondents.correspondent.department}'"/>
										<xsl:with-param name="param_post" select="'${document.links.link.document.correspondents.correspondent.post}'"/>
										<xsl:with-param name="param_comment" select="'${document.links.link.document.correspondents.correspondent.comment}'"/>
										<xsl:with-param name="param_number" select="'${document.links.link.document.correspondents.correspondent.num.number}'"/>
										<xsl:with-param name="param_date" select="'${document.links.link.document.correspondents.correspondent.num.date}'"/>
										<xsl:with-param name="param_signed" select="'${document.links.link.document.signatories.signatory.signed}'"/>
										<xsl:with-param name="param_ref_region" select="'${document.links.link.reference.region}'"/>
										<xsl:with-param name="param_ref_organization" select="'${document.links.link.reference.organization}'"/>
										<xsl:with-param name="param_ref_person" select="'${document.links.link.reference.person}'"/>
										<xsl:with-param name="param_ref_department" select="'${document.links.link.reference.department}'"/>
										<xsl:with-param name="param_ref_post" select="'${document.links.link.reference.post}'"/>
										<xsl:with-param name="param_ref_number" select="'${document.links.link.reference.num.number}'"/>
										<xsl:with-param name="param_ref_data" select="'${document.links.link.reference.num.data}'"/>
										<xsl:with-param name="param_ref_comment" select="'${document.links.link.reference.comment}'"/>
										<xsl:with-param name="param_attr_ref_region_id" select="'${document.links.link.reference.attrRegion.id}'"/>
										<xsl:with-param name="param_attr_ref_region_retro" select="'${document.links.link.reference.attrRegion.retro}'"/>
										<xsl:with-param name="param_attr_ref_region_modified" select="'${document.links.link.reference.attrRegion.modified}'"/>
										<xsl:with-param name="param_attr_ref_organization_id" select="'${document.links.link.reference.attrOrganization.id}'"/>
										<xsl:with-param name="param_attr_ref_organization_retro" select="'${document.links.link.reference.attrOrganization.retro}'"/>
										<xsl:with-param name="param_attr_ref_organization_modified" select="'${document.links.link.reference.attrOrganization.modified}'"/>
										<xsl:with-param name="param_attr_ref_person_id" select="'${document.links.link.reference.attrPerson.id}'"/>
										<xsl:with-param name="param_attr_ref_person_retro" select="'${document.links.link.reference.attrPerson.retro}'"/>
										<xsl:with-param name="param_attr_ref_person_modified" select="'${document.links.link.reference.attrPerson.modified}'"/>
										<xsl:with-param name="param_attr_ref_department_id" select="'${document.links.link.reference.attrDepartment.id}'"/>
										<xsl:with-param name="param_attr_ref_department_retro" select="'${document.links.link.reference.attrDepartment.retro}'"/>
										<xsl:with-param name="param_attr_ref_department_modified" select="'${document.links.link.reference.attrDepartment.modified}'"/>
										<xsl:with-param name="param_attr_ref_post_id" select="'${document.links.link.reference.attrPost.id}'"/>
										<xsl:with-param name="param_attr_ref_post_retro" select="'${document.links.link.reference.attrPost.retro}'"/>
										<xsl:with-param name="param_attr_ref_post_modified" select="'${document.links.link.reference.attrPost.modified}'"/>
											</xsl:call-template>
								</xdms:link>
																					</xdms:links>
		</xsl:variable>
						<xsl:call-template name="paste-non-empty">
							<xsl:with-param name="param_element" select="$linksElement" />
						</xsl:call-template>
						
						<xsl:variable name="clausesElement">
							<xdms:clauses> <!-- Необязательный -->
								<xdms:clause> <!-- Обязательный только узел, значение не обязательно -->
									<xsl:call-template name="documentClause">
										<xsl:with-param name="param_designation" select="'${document.clauses.clause.designation}'" />
										<xsl:with-param name="param_text" select="'${document.clauses.clause.text}'" />
										<xsl:with-param name="param_deadline" select="'${document.clauses.clause.deadline}'" />
										<xsl:with-param name="param_principal_region" select="'${document.clauses.clause.principal.region}'" />
										<xsl:with-param name="param_principal_organization" select="'${document.clauses.clause.principal.organization}'" />
										<xsl:with-param name="param_principal_person" select="'${document.clauses.clause.principal.person}'" />
										<xsl:with-param name="param_principal_department" select="'${document.clauses.clause.principal.department}'" />
										<xsl:with-param name="param_principal_post" select="'${document.clauses.clause.principal.post}'" />
										<xsl:with-param name="param_principal_contactInfo" select="'${document.clauses.clause.principal.contactInfo}'" />
										<xsl:with-param name="param_principal_comment" select="'${document.clauses.clause.principal.comment}'" />
										<xsl:with-param name="param_principals_name" select="'${document.clauses.clause.principals.name}'" />
										<xsl:with-param name="param_principals_region" select="'${document.clauses.clause.principals.contents.region}'" />
										<xsl:with-param name="param_principals_organization" select="'${document.clauses.clause.principals.contents.organization}'" />
										<xsl:with-param name="param_principals_person" select="'${document.clauses.clause.principals.contents.person}'" />
										<xsl:with-param name="param_principals_department" select="'${document.clauses.clause.principals.contents.department}'" />
										<xsl:with-param name="param_principals_post" select="'${document.clauses.clause.principals.contents.post}'" />
										<xsl:with-param name="param_principals_contactInfo" select="'${document.clauses.clause.principals.contents.contactInfo}'" />
										<xsl:with-param name="param_principals_comment" select="'${document.clauses.clause.principals.contents.comment}'" />
										<xsl:with-param name="param_principals_attr_id" select="'${document.clauses.clause.principals.atrrId}'" />
										<xsl:with-param name="param_parcipant_region" select="'${document.clauses.clause.parcipants.parcipant.region}'" />
										<xsl:with-param name="param_parcipant_organization" select="'${document.clauses.clause.parcipants.parcipant.organization}'" />
										<xsl:with-param name="param_parcipant_person" select="'${document.clauses.clause.parcipants.parcipant.person}'" />
										<xsl:with-param name="param_parcipant_department" select="'${document.clauses.clause.parcipants.parcipant.department}'" />
										<xsl:with-param name="param_parcipant_post" select="'${document.clauses.clause.parcipants.parcipant.post}'" />
										<xsl:with-param name="param_parcipant_contactInfo" select="'${document.clauses.clause.parcipants.parcipant.contactInfo}'" />
										<xsl:with-param name="param_parcipant_comment" select="'${document.clauses.clause.parcipants.parcipant.comment}'" />
										<xsl:with-param name="param_parcipants_name" select="'${document.clauses.clause.parcipants.parcipants.name}'" />
										<xsl:with-param name="param_parcipants_region" select="'${document.clauses.clause.parcipants.parcipants.contents.region}'" />
										<xsl:with-param name="param_parcipants_organization" select="'${document.clauses.clause.parcipants.parcipants.contents.organization}'" />
										<xsl:with-param name="param_parcipants_person" select="'${document.clauses.clause.parcipants.parcipants.contents.person}'" />
										<xsl:with-param name="param_parcipants_department" select="'${document.clauses.clause.parcipants.parcipants.contents.department}'" />
										<xsl:with-param name="param_parcipants_post" select="'${document.clauses.clause.parcipants.parcipants.contents.post}'" />
										<xsl:with-param name="param_parcipants_contactInfo" select="'${document.clauses.clause.parcipants.parcipants.contents.contactInfo}'" />
										<xsl:with-param name="param_parcipants_comment" select="'${document.clauses.clause.parcipants.parcipants.contents.comment}'" />
										<xsl:with-param name="param_parcipants_attr_id" select="'${document.clauses.clause.parcipants.parcipants.attrId}'" />
										<xsl:with-param name="param_comment" select="'${document.clauses.clause.comment}'"/>
										<xsl:with-param name="param_attr_localId" select="'${document.clauses.clause.attrLocalId}'"/>
										<xsl:with-param name="param_attr_id" select="'${document.clauses.clause.attrId}'"/>
									</xsl:call-template>
								</xdms:clause>
							</xdms:clauses>
						</xsl:variable>
						<xsl:call-template name="paste-non-empty">
							<xsl:with-param name="param_element" select="$clausesElement" />
						</xsl:call-template>
						
						<xsl:variable name="executorElement">
							<xdms:executor> <!-- Необязательный; -->
								<xsl:call-template name="addressee">
									<xsl:with-param name="param_region" select="'${document.executor.region}'" />
									<xsl:with-param name="param_organization" select="'${document.executor.organization}'" />
									<xsl:with-param name="param_person" select="'${document.executor.person}'" />
									<xsl:with-param name="param_department" select="'${document.executor.department}'" />
									<xsl:with-param name="param_post" select="'${document.executor.post}'" />
									<xsl:with-param name="param_contactInfo" select="'${document.executor.contactInfo}'" />
									<xsl:with-param name="param_comment" select="'${document.executor.comment}'" />
								</xsl:call-template>
							</xdms:executor>
						</xsl:variable>
						<xsl:call-template name="paste-non-empty">
							<xsl:with-param name="param_element" select="$executorElement" />
						</xsl:call-template>
						
						<xsl:call-template name="paste-non-empty"> <!--xdms:comment-->
							<xsl:with-param name="param_element">
								<xdms:comment> <!-- Необязательный -->
									<xsl:value-of select="attribute[@code='${document.comment}']/value" />
								</xdms:comment>
							</xsl:with-param>
						</xsl:call-template>
					</xdms:document>
		
					<xdms:files> <!-- Обязательный -->		
							<xsl:variable name="fileNodes" select="attribute[@code='${files.file.attrLocalName}']/value" />
							<xsl:for-each  select="$fileNodes">
								<xsl:if test="./@description != ''"> <!-- Нужен параметр localName узла -->
									<xdms:file> <!-- Необязательный; attribute name="localName" - обязательный; attribute name="localId" - необязательный; attribute name="type" - необязательный; -->
										<xsl:call-template name="associatedFile">
											<xsl:with-param name="param_group" select="'${files.file.group}'" />
											<xsl:with-param name="param_description" select="'${files.file.description}'" />
											<xsl:with-param name="param_pages" select="'${files.file.pages}'" />
											<xsl:with-param name="param_eds" select="./@edsTemp" />
											<xsl:with-param name="param_localName" select="./@description" />
											<xsl:with-param name="param_localId" select="'${files.file.attrLocalId}'" />
											<xsl:with-param name="param_type" select="'${files.file.attrType}'" />
										</xsl:call-template>
									</xdms:file>
								</xsl:if>
							</xsl:for-each>
					</xdms:files>
				</xsl:template>
	
	<xsl:template name="notification_tmpl">
		<xdms:notification> <!-- attribute name="type" - обязательный; attribute name="uid" - обязательный; attribute name="id" - необязательный; -->
					
			<xsl:attribute name="xdms:type"> <!-- Обязательный -->
				<xsl:call-template name="notificationType" />
			</xsl:attribute>
			<xsl:attribute name="xdms:uid"> <!-- Обязательный -->
				<xsl:value-of select="attribute[@code='${notification.uid}']/value" />
			</xsl:attribute>
			<xsl:call-template name="paste-non-empty-attribute">
				<xsl:with-param name="param_name" select="'xdms:id'" />
				<xsl:with-param name="param_value" select="'${notification.id}'" />
			</xsl:call-template>
						
			<xsl:call-template name="notificationElement"/>
			
			<xsl:call-template name="paste-non-empty"> <!-- xdms:comment -->
				<xsl:with-param name="param_element">
					<xdms:comment> <!-- Необязательный -->
						<xsl:value-of select="attribute[@code='${notification.comment}']/value" />
					</xdms:comment>
				</xsl:with-param>
			</xsl:call-template>
			
			
		</xdms:notification>
	</xsl:template>
	
	<xsl:template name="documentAccepted">
		<xsl:param name="param_documentAccepted_number" />
		<xsl:param name="param_documentAccepted_date" />
		
		<xsl:variable name="documentAcceptedElement">
			<xdms:documentAccepted>
				<xsl:call-template name="notification"/>
				<xdms:num>
					<xsl:call-template name="documentNumber">
						<xsl:with-param name="param_number" select="$param_documentAccepted_number" />
						<xsl:with-param name="param_date" select="$param_documentAccepted_date" />
					</xsl:call-template>
				</xdms:num>
			</xdms:documentAccepted>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element" select="$documentAcceptedElement" />
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="documentRefused">
		<xsl:param name="param_documentRefused_reason" />
		
		<xsl:variable name="documentRefusedElement">
			<xdms:documentRefused>
				<xsl:call-template name="notification"/>
				<xdms:reason>
					<xsl:value-of select="attribute[@code=$param_documentRefused_reason]/value"/>
				</xdms:reason>
			</xdms:documentRefused>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element" select="$documentRefusedElement" />
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="executorAssigned">
		<xsl:param name="param_executorAssigned_secretary_region" />	
		<xsl:param name="param_executorAssigned_secretary_organization" />
		<xsl:param name="param_executorAssigned_secretary_person" />
		<xsl:param name="param_executorAssigned_secretary_department" />
		<xsl:param name="param_executorAssigned_secretary_post" />
		<xsl:param name="param_executorAssigned_secretary_contactInfo" />
		<xsl:param name="param_executorAssigned_secretary_comment" />
		<xsl:param name="param_executorAssigned_manager_region" />
		<xsl:param name="param_executorAssigned_manager_organization" />
		<xsl:param name="param_executorAssigned_manager_person" />
		<xsl:param name="param_executorAssigned_manager_department" />
		<xsl:param name="param_executorAssigned_manager_post" />
		<xsl:param name="param_executorAssigned_manager_contactInfo" />
		<xsl:param name="param_executorAssigned_manager_comment" />
		<xsl:param name="param_executorAssigned_executor_region" />
		<xsl:param name="param_executorAssigned_executor_organization" />
		<xsl:param name="param_executorAssigned_executor_person" />
		<xsl:param name="param_executorAssigned_executor_department" />
		<xsl:param name="param_executorAssigned_executor_post" />
		<xsl:param name="param_executorAssigned_executor_contactInfo" />
		<xsl:param name="param_executorAssigned_executor_comment" />
		
		<xsl:variable name="executorAssignedElement">
			<xdms:executorAssigned>
				<xsl:call-template name="notification"/>
				<xdms:secretary>
					<xsl:call-template name="addressee">
						<xsl:with-param name="param_region" select="$param_executorAssigned_secretary_region" />
						<xsl:with-param name="param_organization" select="$param_executorAssigned_secretary_organization" />
						<xsl:with-param name="param_person" select="$param_executorAssigned_secretary_person" />
						<xsl:with-param name="param_department" select="$param_executorAssigned_secretary_department" />
						<xsl:with-param name="param_post" select="$param_executorAssigned_secretary_post" />
						<xsl:with-param name="param_contactInfo" select="$param_executorAssigned_secretary_contactInfo" />
						<xsl:with-param name="param_comment" select="$param_executorAssigned_secretary_comment" />
					</xsl:call-template>
				</xdms:secretary>
				<xdms:manager>
					<xsl:call-template name="addressee">
						<xsl:with-param name="param_region" select="$param_executorAssigned_manager_region" />
						<xsl:with-param name="param_organization" select="$param_executorAssigned_manager_organization" />
						<xsl:with-param name="param_person" select="$param_executorAssigned_manager_person" />
						<xsl:with-param name="param_department" select="$param_executorAssigned_manager_department" />
						<xsl:with-param name="param_post" select="$param_executorAssigned_manager_post" />
						<xsl:with-param name="param_contactInfo" select="$param_executorAssigned_manager_contactInfo" />
						<xsl:with-param name="param_comment" select="$param_executorAssigned_manager_comment" />
					</xsl:call-template>
				</xdms:manager>
				<xdms:executor>
					<xsl:call-template name="addressee">
						<xsl:with-param name="param_region" select="$param_executorAssigned_executor_region" />
						<xsl:with-param name="param_organization" select="$param_executorAssigned_executor_organization" />
						<xsl:with-param name="param_person" select="$param_executorAssigned_executor_person" />
						<xsl:with-param name="param_department" select="$param_executorAssigned_executor_department" />
						<xsl:with-param name="param_post" select="$param_executorAssigned_executor_post" />
						<xsl:with-param name="param_contactInfo" select="$param_executorAssigned_executor_contactInfo" />
						<xsl:with-param name="param_comment" select="$param_executorAssigned_executor_comment" />
					</xsl:call-template>
				</xdms:executor>
			</xdms:executorAssigned>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element" select="$executorAssignedElement" />
		</xsl:call-template>
	</xsl:template>
	
	<!-- Уведомление "&#x0418;сполнение услуги" не проходящее через схему, используется для обмена между СОЗ и С&#x0418;Р -->
	<xsl:template name="executionService">
		<xsl:param name="param_executionService_reason" />
		
		<xsl:variable name="executionServiceElement">
			<xdms:executionService>
				<xsl:call-template name="notification"/>
				<xdms:reason>
					<xsl:value-of select="attribute[@code=$param_executionService_reason]/value"/>
				</xdms:reason>
			</xdms:executionService>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element" select="$executionServiceElement" />
		</xsl:call-template>
	</xsl:template>
	<!-- End Уведомление "&#x0418;сполнение услуги" -->
	
	
	<xsl:template name="reportPrepared">
		<xsl:param name="param_reportPrepared_signatory_region" />	
		<xsl:param name="param_reportPrepared_signatory_organization" />
		<xsl:param name="param_reportPrepared_signatory_person" />
		<xsl:param name="param_reportPrepared_signatory_department" />
		<xsl:param name="param_reportPrepared_signatory_post" />
		<xsl:param name="param_reportPrepared_signatory_contactInfo" />
		<xsl:param name="param_reportPrepared_signatory_signed" />
		<xsl:param name="param_reportPrepared_signatory_comment" />
		
		<xsl:variable name="reportPreparedElement">
			<xdms:reportPrepared>
				<xsl:call-template name="notification"/>
				<xdms:signatory>
					<xsl:call-template name="signatory">
						<xsl:with-param name="param_region" select="$param_reportPrepared_signatory_region" />
						<xsl:with-param name="param_organization" select="$param_reportPrepared_signatory_organization" />
						<xsl:with-param name="param_person" select="$param_reportPrepared_signatory_person" /> 
						<xsl:with-param name="param_department" select="$param_reportPrepared_signatory_department" />
						<xsl:with-param name="param_post" select="$param_reportPrepared_signatory_post" />
						<xsl:with-param name="param_contactInfo" select="$param_reportPrepared_signatory_contactInfo" />
						<xsl:with-param name="param_signed" select="$param_reportPrepared_signatory_signed" /> 
						<xsl:with-param name="param_comment" select="$param_reportPrepared_signatory_comment" />
					</xsl:call-template>
				</xdms:signatory>
			</xdms:reportPrepared>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element" select="$reportPreparedElement" />
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="reportSent">
		<xsl:param name="param_reportSent_report_region" />
		<xsl:param name="param_reportSent_report_organization" />
		<xsl:param name="param_reportSent_report_person" />
		<xsl:param name="param_reportSent_report_department" />
		<xsl:param name="param_reportSent_report_post" />
		<xsl:param name="param_reportSent_report_num_number" />
		<xsl:param name="param_reportSent_report_num_date" />
		<xsl:param name="param_reportSent_report_comment" />
		<xsl:param name="param_reportSent_report_attrRegion_id" />
		<xsl:param name="param_reportSent_report_attrRegion_retro" />
		<xsl:param name="param_reportSent_report_attrRegion_modified" />
		<xsl:param name="param_reportSent_report_attrOrganization_id" />
		<xsl:param name="param_reportSent_report_attrOrganization_retro" />
		<xsl:param name="param_reportSent_report_attrOrganization_modified" />
		<xsl:param name="param_reportSent_report_attrPerson_id" />
		<xsl:param name="param_reportSent_report_attrPerson_retro" />
		<xsl:param name="param_reportSent_report_attrPerson_modified" />
		<xsl:param name="param_reportSent_report_attrDepartment_id" />
		<xsl:param name="param_reportSent_report_attrDepartment_retro" />
		<xsl:param name="param_reportSent_report_attrDepartment_modified" />
		<xsl:param name="param_reportSent_report_attrPost_id" />
		<xsl:param name="param_reportSent_report_attrPost_retro" />
		<xsl:param name="param_reportSent_report_attrPost_modified" />
		
		<xsl:variable name="reportSentElement">
			<xdms:reportSent>
				<xsl:call-template name="notification"/>
				<xdms:report>
					<xsl:call-template name="documentReference"> 
						<xsl:with-param name="param_region" select="$param_reportSent_report_region" />
						<xsl:with-param name="param_organization" select="$param_reportSent_report_organization" />
						<xsl:with-param name="param_person" select="$param_reportSent_report_person" />
						<xsl:with-param name="param_department" select="$param_reportSent_report_department" />
						<xsl:with-param name="param_post" select="$param_reportSent_report_post" />
						<xsl:with-param name="param_number" select="$param_reportSent_report_num_number" />
						<xsl:with-param name="param_date" select="$param_reportSent_report_num_date" />
						<xsl:with-param name="param_comment" select="$param_reportSent_report_comment" />
						<xsl:with-param name="param_attr_region_id" select="$param_reportSent_report_attrRegion_id" />
						<xsl:with-param name="param_attr_region_retro" select="$param_reportSent_report_attrRegion_retro" />
						<xsl:with-param name="param_attr_region_modified" select="$param_reportSent_report_attrRegion_modified" />
						<xsl:with-param name="param_attr_organization_id" select="$param_reportSent_report_attrOrganization_id" />
						<xsl:with-param name="param_attr_organization_retro" select="$param_reportSent_report_attrOrganization_retro" />
						<xsl:with-param name="param_attr_organization_modified" select="$param_reportSent_report_attrOrganization_modified" />
						<xsl:with-param name="param_attr_person_id" select="$param_reportSent_report_attrPerson_id" />
						<xsl:with-param name="param_attr_person_retro" select="$param_reportSent_report_attrPerson_retro" />
						<xsl:with-param name="param_attr_person_modified" select="$param_reportSent_report_attrPerson_modified" />
						<xsl:with-param name="param_attr_department_id" select="$param_reportSent_report_attrDepartment_id" />
						<xsl:with-param name="param_attr_department_retro" select="$param_reportSent_report_attrDepartment_retro" />
						<xsl:with-param name="param_attr_department_modified" select="$param_reportSent_report_attrDepartment_modified" />
						<xsl:with-param name="param_attr_post_id" select="$param_reportSent_report_attrPost_id" />
						<xsl:with-param name="param_attr_post_retro" select="$param_reportSent_report_attrPost_retro" />
						<xsl:with-param name="param_attr_post_modified" select="$param_reportSent_report_attrPost_modified" />
					</xsl:call-template>
				</xdms:report>
			</xdms:reportSent>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element" select="$reportSentElement" />
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="courseChanged">
		<xsl:param name="param_courseChanged_courseText" />
		<xsl:param name="param_courseChanged_reference_region" />
		<xsl:param name="param_courseChanged_reference_organization" />
		<xsl:param name="param_courseChanged_reference_person" />
		<xsl:param name="param_courseChanged_reference_department" />
		<xsl:param name="param_courseChanged_reference_post" />
		<xsl:param name="param_courseChanged_reference_num_number" />
		<xsl:param name="param_courseChanged_reference_num_date" />
		<xsl:param name="param_courseChanged_reference_comment" />
		<xsl:param name="param_courseChanged_reference_attrRegion_id" />
		<xsl:param name="param_courseChanged_reference_attrRegion_retro" />
		<xsl:param name="param_courseChanged_reference_attrRegion_modified" />
		<xsl:param name="param_courseChanged_reference_attrOrganization_id" />
		<xsl:param name="param_courseChanged_reference_attrOrganization_retro" />
		<xsl:param name="param_courseChanged_reference_attrOrganization_modified" />
		<xsl:param name="param_courseChanged_reference_attrPerson_id" />
		<xsl:param name="param_courseChanged_reference_attrPerson_retro" />
		<xsl:param name="param_courseChanged_reference_attrPerson_modified" />
		<xsl:param name="param_courseChanged_reference_attrDepartment_id" />
		<xsl:param name="param_courseChanged_reference_attrDepartment_retro" />
		<xsl:param name="param_courseChanged_reference_attrDepartment_modified" />
		<xsl:param name="param_courseChanged_reference_attrPost_id" />
		<xsl:param name="param_courseChanged_reference_attrPost_retro" />
		<xsl:param name="param_courseChanged_reference_attrPost_modified" />
		
		<xsl:variable name="courseChangedElement">
			<xdms:courseChanged>
				<xsl:call-template name="notification"/>
				<xdms:courseText>
					<xsl:value-of select="attribute[@code=$param_courseChanged_courseText]/value" />
				</xdms:courseText>
				<xdms:reference>
					<xsl:call-template name="documentReference"> 
						<xsl:with-param name="param_region" select="$param_courseChanged_reference_region" />
						<xsl:with-param name="param_organization" select="$param_courseChanged_reference_organization" />
						<xsl:with-param name="param_person" select="$param_courseChanged_reference_person" />
						<xsl:with-param name="param_department" select="$param_courseChanged_reference_department" />
						<xsl:with-param name="param_post" select="$param_courseChanged_reference_post" />
						<xsl:with-param name="param_number" select="$param_courseChanged_reference_num_number" />
						<xsl:with-param name="param_date" select="$param_courseChanged_reference_num_date" />
						<xsl:with-param name="param_comment" select="$param_courseChanged_reference_comment" />
						<xsl:with-param name="param_attr_region_id" select="$param_courseChanged_reference_attrRegion_id" />
						<xsl:with-param name="param_attr_region_retro" select="$param_courseChanged_reference_attrRegion_retro" />
						<xsl:with-param name="param_attr_region_modified" select="$param_courseChanged_reference_attrRegion_modified" />
						<xsl:with-param name="param_attr_organization_id" select="$param_courseChanged_reference_attrOrganization_id" />
						<xsl:with-param name="param_attr_organization_retro" select="$param_courseChanged_reference_attrOrganization_retro" />
						<xsl:with-param name="param_attr_organization_modified" select="$param_courseChanged_reference_attrOrganization_modified" />
						<xsl:with-param name="param_attr_person_id" select="$param_courseChanged_reference_attrPerson_id" />
						<xsl:with-param name="param_attr_person_retro" select="$param_courseChanged_reference_attrPerson_retro" />
						<xsl:with-param name="param_attr_person_modified" select="$param_courseChanged_reference_attrPerson_modified" />
						<xsl:with-param name="param_attr_department_id" select="$param_courseChanged_reference_attrDepartment_id" />
						<xsl:with-param name="param_attr_department_retro" select="$param_courseChanged_reference_attrDepartment_retro" />
						<xsl:with-param name="param_attr_department_modified" select="$param_courseChanged_reference_attrDepartment_modified" />
						<xsl:with-param name="param_attr_post_id" select="$param_courseChanged_reference_attrPost_id" />
						<xsl:with-param name="param_attr_post_retro" select="$param_courseChanged_reference_attrPost_retro" />
						<xsl:with-param name="param_attr_post_modified" select="$param_courseChanged_reference_attrPost_modified" />
					</xsl:call-template>
				</xdms:reference>
			</xdms:courseChanged>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element" select="$courseChangedElement" />
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="notification">
		<xdms:time> <!-- обязательный -->
			<xsl:value-of select="attribute[@code='${notification.time}']/value" />
		</xdms:time>
		<xsl:variable name="foundationElement"> <!-- Необязательный -->
			<xdms:foundation>
				<xsl:call-template name="documentReference"> 
					<xsl:with-param name="param_region" select="'${notification.foundation.region}'" />
					<xsl:with-param name="param_organization" select="'${notification.foundation.organization}'" />
					<xsl:with-param name="param_person" select="'${notification.foundation.person}'" />
					<xsl:with-param name="param_department" select="'${notification.foundation.department}'" />
					<xsl:with-param name="param_post" select="'${notification.foundation.post}'" />
					<xsl:with-param name="param_number" select="'${notification.foundation.num.number}'" />
					<xsl:with-param name="param_date" select="'${notification.foundation.num.date}'" />
					<xsl:with-param name="param_comment" select="'${notification.foundation.comment}'" />
					<xsl:with-param name="param_attr_region_id" select="'${notification.foundation.attrRegion.id}'" />
					<xsl:with-param name="param_attr_region_retro" select="'${notification.foundation.attrRegion.retro}'" />
					<xsl:with-param name="param_attr_region_modified" select="'${notification.foundation.attrRegion.modified}'" />
					<xsl:with-param name="param_attr_organization_id" select="'${notification.foundation.attrOrganization.id}'" />
					<xsl:with-param name="param_attr_organization_retro" select="'${notification.foundation.attrOrganization.retro}'" />
					<xsl:with-param name="param_attr_organization_modified" select="'${notification.foundation.attrOrganization.modified}'" />
					<xsl:with-param name="param_attr_person_id" select="'${notification.foundation.attrPerson.id}'" />
					<xsl:with-param name="param_attr_person_retro" select="'${notification.foundation.attrPerson.retro}'" />
					<xsl:with-param name="param_attr_person_modified" select="'${notification.foundation.attrPerson.modified}'" />
					<xsl:with-param name="param_attr_department_id" select="'${notification.foundation.attrDepartment.id}'" />
					<xsl:with-param name="param_attr_department_retro" select="'${notification.foundation.attrDepartment.retro}'" />
					<xsl:with-param name="param_attr_department_modified" select="'${notification.foundation.attrDepartment.modified}'" />
					<xsl:with-param name="param_attr_post_id" select="'${notification.foundation.attrPost.id}'" />
					<xsl:with-param name="param_attr_post_retro" select="'${notification.foundation.attrPost.retro}'" />
					<xsl:with-param name="param_attr_post_modified" select="'${notification.foundation.attrPost.modified}'" />
				</xsl:call-template>
			</xdms:foundation>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element" select="$foundationElement" />
		</xsl:call-template>
		<xsl:variable name="clauseElement"> <!-- Необязательный -->
			<xdms:clause>
				<xsl:call-template name="documentClause">
					<xsl:with-param name="param_designation" select="'${notification.clause.designation}'" />
					<xsl:with-param name="param_text" select="'${notification.clause.text}'" />
					<xsl:with-param name="param_deadline" select="'${notification.clause.deadline}'" />
					<xsl:with-param name="param_principal_region" select="'${notification.clause.principal.region}'" />
					<xsl:with-param name="param_principal_organization" select="'${notification.clause.principal.organization}'" />
					<xsl:with-param name="param_principal_person" select="'${notification.clause.principal.person}'" />
					<xsl:with-param name="param_principal_department" select="'${notification.clause.principal.department}'" />
					<xsl:with-param name="param_principal_post" select="'${notification.clause.principal.post}'" />
					<xsl:with-param name="param_principal_contactInfo" select="'${notification.clause.principal.contactInfo}'" />
					<xsl:with-param name="param_principal_comment" select="'${notification.clause.principal.comment}'" />
					<xsl:with-param name="param_principals_name" select="'${notification.clause.principals.name}'" />
					<xsl:with-param name="param_principals_region" select="'${notification.clause.principals.contents.region}'" />
					<xsl:with-param name="param_principals_organization" select="'${notification.clause.principals.contents.organization}'" />
					<xsl:with-param name="param_principals_person" select="'${notification.clause.principals.contents.person}'" />
					<xsl:with-param name="param_principals_department" select="'${notification.clause.principals.contents.department}'" />
					<xsl:with-param name="param_principals_post" select="'${notification.clause.principals.contents.post}'" />
					<xsl:with-param name="param_principals_contactInfo" select="'${notification.clause.principals.contents.contactInfo}'" />
					<xsl:with-param name="param_principals_comment" select="'${notification.clause.principals.contents.comment}'" />
					<xsl:with-param name="param_principals_attr_id" select="'${notification.clause.principals.contents.attrId}'" />
					<xsl:with-param name="param_parcipant_region" select="'${notification.clause.parcipants.parcipant.region}'" />
					<xsl:with-param name="param_parcipant_organization" select="'${notification.clause.parcipants.parcipant.organization}'" />
					<xsl:with-param name="param_parcipant_person" select="'${notification.clause.parcipants.parcipant.person}'" />
					<xsl:with-param name="param_parcipant_department" select="'${notification.clause.parcipants.parcipant.department}'" />
					<xsl:with-param name="param_parcipant_post" select="'${notification.clause.parcipants.parcipant.post}'" />
					<xsl:with-param name="param_parcipant_contactInfo" select="'${notification.clause.parcipants.parcipant.contactInfo}'" />
					<xsl:with-param name="param_parcipant_comment" select="'${notification.clause.parcipants.parcipant.comment}'" />
					<xsl:with-param name="param_parcipants_name" select="'${notification.clause.parcipants.parcipants.name}'" />
					<xsl:with-param name="param_parcipants_region" select="'${notification.clause.parcipants.parcipants.contents.region}'" />
					<xsl:with-param name="param_parcipants_organization" select="'${notification.clause.parcipants.parcipants.contents.organization}'" />
					<xsl:with-param name="param_parcipants_person" select="'${notification.clause.parcipants.parcipants.contents.person}'" />
					<xsl:with-param name="param_parcipants_department" select="'${notification.clause.parcipants.parcipants.contents.department}'" />
					<xsl:with-param name="param_parcipants_post" select="'${notification.clause.parcipants.parcipants.contents.post}'" />
					<xsl:with-param name="param_parcipants_contactInfo" select="'${notification.clause.parcipants.parcipants.contents.contactInfo}'" />
					<xsl:with-param name="param_parcipants_comment" select="'${notification.clause.parcipants.parcipants.contents.comment}'" />
					<xsl:with-param name="param_parcipants_attr_id" select="'${notification.clause.parcipants.parcipants.contents.attrId}'" />
					<xsl:with-param name="param_comment" select="'${notification.clause.comment}'"/>
					<xsl:with-param name="param_attr_localId" select="'${notification.clause.attrLocalId}'"/>
					<xsl:with-param name="param_attr_id" select="'${notification.clause.attrId}'"/>
				</xsl:call-template>
			</xdms:clause>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element" select="$clauseElement" />
		</xsl:call-template>
	</xsl:template>
	
	<!--<xsl:template name="organization">
		<xsl:param name="param_organization" />
		<xsl:param name="param_region" />
		<xdms:organization> --> <!-- Обязательный; attribute name="id" - необязательный; attribute name="retro" - необязательный; attribute name="modified" - необязательный; -->
<!--			<xsl:call-template name="classifyItemAttrs">
				<xsl:with-param name="param_values" select="substring-after($param_organization, ';')"/>
			</xsl:call-template>
			<xsl:value-of select="attribute[@code=substring-before($param_organization, ';')]/value" />
		</xdms:organization>
		
		<xsl:variable name="regionSubElement">
			<xdms:region>--> <!-- Необязательный;  attribute name="id" - необязательный; attribute name="retro" - необязательный; attribute name="modified" - необязательный; -->
<!--				<xsl:call-template name="classifyItemAttrs">
					<xsl:with-param name="param_values" select="substring-after($param_region, ';')"/>
				</xsl:call-template>
				<xsl:value-of select="attribute[@code=substring-before($param_region, ';')]/value"/>
			</xdms:region>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element" select name="$regionSubElement" />
		</xsl:call-template>
	</xsl:template>	-->

	<xsl:template name="communicationPartner">
		<xsl:param name="param_organization" />
		<xsl:param name="param_comment" />
		<xsl:param name="param_uid_attribute" />
		<xsl:attribute name="xdms:uid"> <!-- Обязательный -->
			<xsl:value-of select="'126b3150-c538-40de-88c7-e8674b5d7843'" />
		</xsl:attribute>
		<xdms:organization> <!-- Обязательный -->
			<xsl:value-of select="'Федеральная служба исполнения наказаний'" />
		</xdms:organization>
		<xsl:call-template name="paste-non-empty"> <!--xdms:comment-->
			<xsl:with-param name="param_element">
				<xdms:comment> <!-- Необязательный -->
					<xsl:value-of select="attribute[@code=$param_comment]/value"/>
				</xdms:comment>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="documentNumber">
		<xsl:param name="param_number"/>
		<xsl:param name="param_date" />
		<xdms:number> <!-- Обязательный -->
			<xsl:value-of select="attribute[@code=$param_number]/value" />
		</xdms:number>
		<xdms:date> <!-- Обязательный -->
			<xsl:value-of select="attribute[@code=$param_date]/value" />
		</xdms:date>
	</xsl:template>
	
	<xsl:template name="anyone">
		<xsl:param name="param_region" />
		<xsl:param name="param_organization" />
		<xsl:param name="param_person" />
		<xsl:param name="param_department" />
		<xsl:param name="param_post" />
		<xsl:param name="param_contactInfo" />
		<xsl:call-template name="paste-non-empty"> <!--xdms:region-->
			<xsl:with-param name="param_element" >
				<xdms:region> 
					<xsl:value-of select="attribute[@code=$param_region]/value/@description" />
				</xdms:region>
			</xsl:with-param>
		</xsl:call-template>
		<xsl:call-template name="paste-non-empty"> <!--xdms:organization-->
			<xsl:with-param name="param_element" >
				<xdms:organization> 
					<xsl:value-of select="attribute[@code=$param_organization]/value" />
				</xdms:organization>
			</xsl:with-param>
		</xsl:call-template>
		<xsl:call-template name="paste-non-empty"> <!--xdms:person-->
			<xsl:with-param name="param_element" >
				<xdms:person> 
					<xsl:value-of select="attribute[@code=$param_person]/value/@description" />
				</xdms:person>
			</xsl:with-param>
		</xsl:call-template>
		<xsl:call-template name="paste-non-empty"> <!--xdms:department-->
			<xsl:with-param name="param_element" >
				<xdms:department> 
					<xsl:value-of select="attribute[@code=$param_department]/value" />
				</xdms:department>
			</xsl:with-param>
		</xsl:call-template>
		<xsl:call-template name="paste-non-empty"> <!--xdms:post-->
			<xsl:with-param name="param_element" >
				<xdms:post> 
					<xsl:value-of select="attribute[@code=$param_post]/value" />
				</xdms:post>
			</xsl:with-param>
		</xsl:call-template>
		<xsl:call-template name="paste-non-empty"> <!--xdms:contactInfo-->
			<xsl:with-param name="param_element" >
				<xdms:contactInfo> 
					<xsl:value-of select="attribute[@code=$param_contactInfo]/value" />
				</xdms:contactInfo>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="addressee">
		<xsl:param name="param_region" />
		<xsl:param name="param_organization" />
		<xsl:param name="param_person" />
		<xsl:param name="param_department" />
		<xsl:param name="param_post" />
		<xsl:param name="param_contactInfo" />
		<xsl:param name="param_comment" />
		<xsl:call-template name="anyone">
			<xsl:with-param name="param_region" select="$param_region" />
			<xsl:with-param name="param_organization" select="$param_organization" />
			<xsl:with-param name="param_person" select="$param_person"/>
			<xsl:with-param name="param_department" select="$param_department" />
			<xsl:with-param name="param_post" select="$param_post" />
			<xsl:with-param name="param_contactInfo" select="$param_contactInfo" />
		</xsl:call-template>
		<xsl:call-template name="paste-non-empty"> <!--xdms:comment-->
			<xsl:with-param name="param_element" >
				<xdms:comment> 
					<xsl:value-of select="attribute[@code=$param_comment]/value" />
				</xdms:comment>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="addresseeList">
		<xsl:param name="param_name" />
		<xsl:param name="param_region" />
		<xsl:param name="param_organization" />
		<xsl:param name="param_person" />
		<xsl:param name="param_department" />
		<xsl:param name="param_post" />
		<xsl:param name="param_contactInfo" />
		<xsl:param name="param_comment" />
		<xsl:param name="param_id" />
		<xdms:name>
			<xsl:value-of select="attribute[@code=$param_name]/value" />
		</xdms:name>
		<xdms:contents>
			<xdms:addressee>
				<xsl:call-template name="addressee">
					<xsl:with-param name="param_region" select="$param_region" />
					<xsl:with-param name="param_organization" select="$param_organization" />
					<xsl:with-param name="param_person" select="$param_person" />
					<xsl:with-param name="param_department" select="$param_department" />
					<xsl:with-param name="param_post" select="$param_post" />
					<xsl:with-param name="param_contactInfo" select="$param_contactInfo" />
					<xsl:with-param name="param_comment" select="$param_comment" />
				</xsl:call-template>
			</xdms:addressee>
		</xdms:contents>
		<xsl:call-template name="paste-non-empty-attribute">
			<xsl:with-param name="param_name" select="'id'" />
			<xsl:with-param name="param_value" select="$param_id" />
		</xsl:call-template>
	</xsl:template>
	
<!--	<xsl:template name="person">
		<xsl:param name="param_person" />
		<xsl:param name="param_department" />
		<xsl:param name="param_post" />
		<xdms:person> --> <!-- Обязательный; attribute name="id" - необязательный; attribute name="retro" - необязательный; attribute name="modified" - необязательный; -->
<!--			<xsl:call-template name="classifyItemAttrs">
				<xsl:with-param name="param_values" select="substring-after($param_person, ';')" />
			</xsl:call-template>
			<xsl:value-of select="attribute[@code=substring-before($param_person, ';')]/value" />
		</xdms:person>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element">
				<xdms:department> --> <!-- Необязательный; attribute name="id" - необязательный; attribute name="retro" - необязательный; attribute name="modified" - необязательный; -->
<!--					<xsl:call-template name="classifyItemAttrs">
						<xsl:with-param name="param_values" select="substring-after($param_department, ';')" />
					</xsl:call-template>
					<xsl:value-of select="attribute[@code=substring-before($param_department, ';')]/value" />
				</xdms:department>
			</xsl:with-param>
		</xsl:call-template>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element">
				<xdms:post> --> <!-- Необязательный; attribute name="id" - необязательный; attribute name="retro" - необязательный; attribute name="modified" - необязательный; -->
<!--					<xsl:call-template name="classifyItemAttrs">
						<xsl:with-param name="param_values" select="substring-after($param_post, ';')" />
					</xsl:call-template>
					<xsl:value-of select="attribute[@code=substring-before($param_post, ';')]/value" />
				</xdms:post>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>-->
	
	<!--<xsl:template name="communicationEntity">
		<xsl:param name="param_organization" />
		<xsl:param name="param_region" />
		<xsl:param name="param_person" />
		<xsl:param name="param_department" />
		<xsl:param name="param_post" />
		<xsl:param name="param_comment" />
		<xdms:organization> --> <!-- Обязательный -->
<!--			<xsl:call-template name="organization">
				<xsl:with-param name="param_organization" select="$param_organization" />
				<xsl:with-param name="param_region" select="$param_region" />
			</xsl:call-template>
		</xdms:organization>
		<xsl:variable name="personElement">
			<xdms:person>--> <!-- Необязательный -->
<!--				<xsl:call-template name="person">
					<xsl:with-param name="param_person" select="$param_person" />
					<xsl:with-param name="param_department" select="$param_department" />
					<xsl:with-param name="param_post" select="$param_post" />			
				</xsl:call-template>
			</xdms:person>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element" select="$personElement" />
		</xsl:call-template>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element">
				<xdms:comment>--> <!-- Необязательный -->
<!--					<xsl:value-of select="attribute[@code=$param_comment]/value" />
				</xdms:comment>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>-->

	<xsl:template name="correspondent">
		<xsl:param name="param_region" />
		<xsl:param name="param_organization" />
		<xsl:param name="param_person" />
		<xsl:param name="param_department" />
		<xsl:param name="param_post" />
		<xsl:param name="param_contactInfo" />
		<xsl:param name="param_number" />
		<xsl:param name="param_date" />
		<xsl:param name="param_comment" />
		<xsl:call-template name="anyone">
			<xsl:with-param name="param_region" select="$param_region" />
			<xsl:with-param name="param_organization" select="$param_organization" />
			<xsl:with-param name="param_person" select="$param_person"/>
			<xsl:with-param name="param_department" select="$param_department" />
			<xsl:with-param name="param_post" select="$param_post" />
			<xsl:with-param name="param_contactInfo" select="$param_contactInfo" />
		</xsl:call-template>
		
		<xsl:variable name="numElement"> <!--xdms:num-->
			<xdms:num>
				<xsl:call-template name="documentNumber">
					<xsl:with-param name="param_number" select="$param_number" />
					<xsl:with-param name="param_date" select="$param_date" />
				</xsl:call-template>
			</xdms:num>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element" select="$numElement" />
		</xsl:call-template>
		
		<xsl:call-template name="paste-non-empty"> <!--xdms:comment-->
			<xsl:with-param name="param_element" >
				<xdms:comment> 
					<xsl:value-of select="attribute[@code=$param_comment]/value" />
				</xdms:comment>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="enclosure">
		<xsl:param name="param_title" />
		<xsl:param name="param_reference_region" />
		<xsl:param name="param_reference_organization" />
		<xsl:param name="param_reference_person" />
		<xsl:param name="param_reference_department" />
		<xsl:param name="param_reference_post" />
		<xsl:param name="param_reference_number" />
		<xsl:param name="param_reference_date" />
		<xsl:param name="param_reference_comment" />
		<xsl:param name="param_pages" />
		<xsl:param name="param_files" />
		<xsl:param name="param_comment" />
		<xsl:param name="param_reference_attr_region_id" />
		<xsl:param name="param_reference_attr_region_retro" />
		<xsl:param name="param_reference_attr_region_modified" />
		<xsl:param name="param_reference_attr_organization_id" />
		<xsl:param name="param_reference_attr_organization_retro" />
		<xsl:param name="param_reference_attr_organization_modified" />
		<xsl:param name="param_reference_attr_person_id" />
		<xsl:param name="param_reference_attr_person_retro" />
		<xsl:param name="param_reference_attr_person_modified" />
		<xsl:param name="param_reference_attr_department_id" />
		<xsl:param name="param_reference_attr_department_retro" />
		<xsl:param name="param_reference_attr_department_modified" />
		<xsl:param name="param_reference_attr_post_id" />
		<xsl:param name="param_reference_attr_post_retro" />
		<xsl:param name="param_reference_attr_post_modified" />
		
		<xdms:title>
			<xsl:value-of select="attribute[@code=$param_title]/value" />
		</xdms:title>
		<xdms:reference>
			<xsl:call-template name="documentReference">
				<xsl:with-param name="param_region" select="$param_reference_region" />
				<xsl:with-param name="param_organization" select="$param_reference_organization" />
				<xsl:with-param name="param_person" select="$param_reference_person" />
				<xsl:with-param name="param_department" select="$param_reference_department" />
				<xsl:with-param name="param_post" select="$param_reference_post" />
				<xsl:with-param name="param_number" select="$param_reference_number" />
				<xsl:with-param name="param_date" select="$param_reference_date" />
				<xsl:with-param name="param_comment" select="$param_reference_comment" />
				<xsl:with-param name="param_attr_region_id" select="$param_reference_attr_region_id" />
				<xsl:with-param name="param_attr_region_retro" select="$param_reference_attr_region_retro" />
				<xsl:with-param name="param_attr_region_modified" select="$param_reference_attr_region_modified" />
				<xsl:with-param name="param_attr_organization_id" select="$param_reference_attr_organization_id" />
				<xsl:with-param name="param_attr_organization_retro" select="$param_reference_attr_organization_retro" />
				<xsl:with-param name="param_attr_organization_modified" select="$param_reference_attr_organization_modified" />
				<xsl:with-param name="param_attr_person_id" select="$param_reference_attr_person_id" />
				<xsl:with-param name="param_attr_person_retro" select="$param_reference_attr_person_retro" />
				<xsl:with-param name="param_attr_person_modified" select="$param_reference_attr_person_modified" />
				<xsl:with-param name="param_attr_department_id" select="$param_reference_attr_department_id" />
				<xsl:with-param name="param_attr_department_retro" select="$param_reference_attr_department_retro" />
				<xsl:with-param name="param_attr_department_modified" select="$param_reference_attr_department_modified" />
				<xsl:with-param name="param_attr_post_id" select="$param_reference_attr_post_id" />
				<xsl:with-param name="param_attr_post_retro" select="$param_reference_attr_post_retro" />
				<xsl:with-param name="param_attr_post_modified" select="$param_reference_attr_post_modified" />
			</xsl:call-template>
		</xdms:reference>
		<xdms:pages>
			<xsl:value-of select="attribute[@code=$param_pages]/value" />
		</xdms:pages>
		<xsl:call-template name="paste-non-empty"> <!--xdms:files-->
			<xsl:with-param name="param_element">
				<xdms:files> 
					<xsl:value-of select="attribute[@code=$param_files]/value" />
				</xdms:files>
			</xsl:with-param>
		</xsl:call-template>
		<xsl:call-template name="paste-non-empty"> <!--xdms:comment-->
			<xsl:with-param name="param_element">
				<xdms:comment> 
					<xsl:value-of select="attribute[@code=$param_comment]/value" />
				</xdms:comment>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="documentReference">
		<xsl:param name="param_region" />
		<xsl:param name="param_organization" />
		<xsl:param name="param_person" />
		<xsl:param name="param_department" />
		<xsl:param name="param_post" />
		<xsl:param name="param_number" />
		<xsl:param name="param_date" />
		<xsl:param name="param_comment" />
		<xsl:param name="param_attr_region_id"/>
		<xsl:param name="param_attr_region_retro"/>
		<xsl:param name="param_attr_region_modified"/>
		<xsl:param name="param_attr_organization_id"/>
		<xsl:param name="param_attr_organization_retro"/>
		<xsl:param name="param_attr_organization_modified"/>
		<xsl:param name="param_attr_person_id"/>
		<xsl:param name="param_attr_person_retro"/>
		<xsl:param name="param_attr_person_modified"/>
		<xsl:param name="param_attr_department_id"/>
		<xsl:param name="param_attr_department_retro"/>
		<xsl:param name="param_attr_department_modified"/>
		<xsl:param name="param_attr_post_id" />
		<xsl:param name="param_attr_post_retro" />
		<xsl:param name="param_attr_post_modified" />
		
		<xsl:variable name="regionElement">
			<xdms:region> 
				<xsl:call-template name="classifyItemAttrs">
					<xsl:with-param name="param_id" select="$param_attr_region_id"/>
					<xsl:with-param name="param_retro" select="$param_attr_region_retro"/>
					<xsl:with-param name="param_modified" select="$param_attr_region_modified"/>
				</xsl:call-template>	
				<xsl:value-of select="attribute[@code=$param_region]/value" />
			</xdms:region>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty"> <!--xdms:region-->
			<xsl:with-param name="param_element" select="$regionElement"/>
		</xsl:call-template>
		
		<xsl:variable name="organizationElement">
			<xdms:organization> 
				<xsl:call-template name="classifyItemAttrs">
					<xsl:with-param name="param_id" select="$param_attr_organization_id"/>
					<xsl:with-param name="param_retro" select="$param_attr_organization_retro"/>
					<xsl:with-param name="param_modified" select="$param_attr_organization_modified"/>
				</xsl:call-template>
				<xsl:value-of select="attribute[@code=$param_organization]/value/@description" />
			</xdms:organization>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty"> <!--xdms:organization-->
			<xsl:with-param name="param_element" select="$organizationElement"/>
		</xsl:call-template>
		
		<xsl:variable name="personElement">
			<xdms:person> 
				<xsl:call-template name="classifyItemAttrs">
					<xsl:with-param name="param_id" select="$param_attr_person_id"/>
					<xsl:with-param name="param_retro" select="$param_attr_person_retro"/>
					<xsl:with-param name="param_modified" select="$param_attr_person_modified"/>
				</xsl:call-template>
				<xsl:value-of select="attribute[@code=$param_person]/value" />
			</xdms:person>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty"> <!--xdms:person-->
			<xsl:with-param name="param_element" select="$personElement"/>
		</xsl:call-template>
		
		<xsl:variable name="departmentElement">
			<xdms:department> 
				<xsl:call-template name="classifyItemAttrs">
					<xsl:with-param name="param_id" select="$param_attr_department_id"/>
					<xsl:with-param name="param_retro" select="$param_attr_department_retro"/>
					<xsl:with-param name="param_modified" select="$param_attr_department_modified"/>
				</xsl:call-template>
				<xsl:value-of select="attribute[@code=$param_department]/value" />
			</xdms:department>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty"> <!--xdms:department-->
			<xsl:with-param name="param_element" select="$departmentElement"/>
		</xsl:call-template>
		
		<xsl:variable name="postElement">
			<xdms:post> 
				<xsl:call-template name="classifyItemAttrs">
					<xsl:with-param name="param_id" select="$param_attr_post_id"/>
					<xsl:with-param name="param_retro" select="$param_attr_post_retro"/>
					<xsl:with-param name="param_modified" select="$param_attr_post_modified"/>
				</xsl:call-template>
				<xsl:value-of select="attribute[@code=$param_post]/value" />
			</xdms:post>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty"> <!--xdms:post-->
			<xsl:with-param name="param_element" select="$postElement"/>
		</xsl:call-template>
		
		<xdms:num>
			<xsl:call-template name="documentNumber" >
				<xsl:with-param name="param_number" select="$param_number" />
				<xsl:with-param name="param_date" select="$param_date" />
			</xsl:call-template>
		</xdms:num>
		<xsl:call-template name="paste-non-empty"> <!--xdms:comment-->
			<xsl:with-param name="param_element" >
				<xdms:comment> 
					<xsl:value-of select="attribute[@code=$param_comment]/value" />
				</xdms:comment>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="linkedDocument">
		<xsl:param name="param_number_document"/>
		<xsl:param name="param_date_document"/>
		<xsl:param name="param_kind"/>
		<xsl:param name="param_link_type"/>
		<xsl:param name="param_organization"/>
		<xsl:param name="param_region"/>
		<xsl:param name="param_person"/>
		<xsl:param name="param_department"/>
		<xsl:param name="param_post"/>
		<xsl:param name="param_comment"/>
		<xsl:param name="param_number"/>
		<xsl:param name="param_date"/>
		<xsl:param name="param_signed"/>
		<xsl:param name="param_ref_region"/>
		<xsl:param name="param_ref_organization"/>
		<xsl:param name="param_ref_person"/>
		<xsl:param name="param_ref_department"/>
		<xsl:param name="param_ref_post"/>
		<xsl:param name="param_ref_number"/>
		<xsl:param name="param_ref_data"/>
		<xsl:param name="param_ref_comment"/>
		<xsl:param name="param_attr_uid"/> 	<!-- Атрибут uid - необязательный; значение вида "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}" -->
		<xsl:param name="param_attr_ref_region_id"/>
		<xsl:param name="param_attr_ref_region_retro"/>
		<xsl:param name="param_attr_ref_region_modified"/>
		<xsl:param name="param_attr_ref_organization_id"/>
		<xsl:param name="param_attr_ref_organization_retro"/>
		<xsl:param name="param_attr_ref_organization_modified"/>
		<xsl:param name="param_attr_ref_person_id"/>
		<xsl:param name="param_attr_ref_person_retro"/>
		<xsl:param name="param_attr_ref_person_modified"/>
		<xsl:param name="param_attr_ref_department_id"/>
		<xsl:param name="param_attr_ref_department_retro"/>
		<xsl:param name="param_attr_ref_department_modified"/>
		<xsl:param name="param_attr_ref_post_id"/>
		<xsl:param name="param_attr_ref_post_retro"/>
		<xsl:param name="param_attr_ref_post_modified"/>
		
		<xsl:call-template name="paste-non-empty-attribute">
			<xsl:with-param name="param_name" select="'xdms:uid'" /> <xsl:with-param name="param_value" select="$param_attr_uid" />
		</xsl:call-template>
		<xdms:linkType> <!-- Обязательный; по умолчанию - значение "Связан с" -->
			<xsl:call-template name="linkType">
			<xsl:with-param name="param_link_type" select="$param_link_type" />
		</xsl:call-template>
		</xdms:linkType>
		<xsl:variable name="doc_vs_ref">false</xsl:variable> <!-- фиктивная переменная, отвечающая за выбор: отображать целиком связанный документ (true) или только ссылку на связанный документ (false) -->
		<xsl:choose>
			<xsl:when test="$doc_vs_ref='true'">
				<xdms:document>
					<xdms:num> <!-- Обязательный -->
			<xsl:call-template name="documentNumber" >
				<xsl:with-param name="param_number" select="$param_number_document" />
				<xsl:with-param name="param_date" select="$param_date_document" />
			</xsl:call-template>
		</xdms:num>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element">
				<xdms:kind> <!-- Необязательный; attribute name="id" - необязательный; attribute name="retro" - необязательный; attribute name="modified" - необязательный; -->
					<xsl:call-template name="classifyItemAttrs">
						<xsl:with-param name="param_values" select="substring-after($param_kind, ';')" />
					</xsl:call-template>
					<xsl:value-of select="attribute[@code=substring-before($param_kind, ';')]/value" />
				</xdms:kind>
			</xsl:with-param>
		</xsl:call-template>
		<xsl:variable name="correspondentsElement">
			<xdms:correspondents> <!-- Необязательный; может встречаться максимум 1 раз -->
				<xsl:call-template name="correspondent">
					<xsl:with-param name="param_organization" select="$param_organization" />
					<xsl:with-param name="param_region" select="$param_region" />
					<xsl:with-param name="param_person" select="$param_person" />
					<xsl:with-param name="param_department" select="$param_department" />
					<xsl:with-param name="param_post" select="$param_post" />
					<xsl:with-param name="param_comment" select="$param_comment" />
					<xsl:with-param name="param_number" select="$param_number" />
					<xsl:with-param name="param_date" select="$param_date" />
					<xsl:with-param name="param_signed" select="$param_signed" />
				</xsl:call-template>
			</xdms:correspondents>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element" select="$correspondentsElement" />
		</xsl:call-template>
	</xdms:document>
			</xsl:when>
			<xsl:otherwise>
				<xdms:reference>
					<xsl:call-template name="documentReference">
						<xsl:with-param name="param_region" select="$param_ref_region" />
						<xsl:with-param name="param_organization" select="$param_ref_organization" />
						<xsl:with-param name="param_person" select="$param_ref_person" />
						<xsl:with-param name="param_department" select="$param_ref_department" />
						<xsl:with-param name="param_post" select="$param_ref_post" />
						<xsl:with-param name="param_number" select="$param_ref_number" />
						<xsl:with-param name="param_date" select="$param_ref_data" />
						<xsl:with-param name="param_comment" select="$param_ref_comment" />
						<xsl:with-param name="param_attr_region_id" select="$param_attr_ref_region_id" />
						<xsl:with-param name="param_attr_region_retro" select="$param_attr_ref_region_retro" />
						<xsl:with-param name="param_attr_region_modified" select="$param_attr_ref_region_modified" />
						<xsl:with-param name="param_attr_organization_id" select="$param_attr_ref_organization_id" />
						<xsl:with-param name="param_attr_organization_retro" select="$param_attr_ref_organization_retro" />
						<xsl:with-param name="param_attr_organization_modified" select="$param_attr_ref_organization_modified" />
						<xsl:with-param name="param_attr_person_id" select="$param_attr_ref_person_id" />
						<xsl:with-param name="param_attr_person_retro" select="$param_attr_ref_person_retro" />
						<xsl:with-param name="param_attr_person_modified" select="$param_attr_ref_person_modified" />
						<xsl:with-param name="param_attr_department_id" select="$param_attr_ref_department_id" />
						<xsl:with-param name="param_attr_department_retro" select="$param_attr_ref_department_retro" />
						<xsl:with-param name="param_attr_department_modified" select="$param_attr_ref_department_modified" />
						<xsl:with-param name="param_attr_post_id" select="$param_attr_ref_post_id" />
						<xsl:with-param name="param_attr_post_retro" select="$param_attr_ref_post_retro" />
						<xsl:with-param name="param_attr_post_modified" select="$param_attr_ref_post_modified" />								
					</xsl:call-template>
				</xdms:reference>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="documentClause">
		<xsl:param name="param_designation" />
		<xsl:param name="param_text" />
		<xsl:param name="param_deadline" />
		<xsl:param name="param_principal_region" />
		<xsl:param name="param_principal_organization" />
		<xsl:param name="param_principal_person" />
		<xsl:param name="param_principal_department" />
		<xsl:param name="param_principal_post" />
		<xsl:param name="param_principal_contactInfo" />
		<xsl:param name="param_principal_comment" />
		<xsl:param name="param_principals_name" />
		<xsl:param name="param_principals_region" />
		<xsl:param name="param_principals_organization" />
		<xsl:param name="param_principals_person" />
		<xsl:param name="param_principals_department" />
		<xsl:param name="param_principals_post" />
		<xsl:param name="param_principals_contactInfo" />
		<xsl:param name="param_principals_comment" />
		<xsl:param name="param_principals_attr_id" />
		<xsl:param name="param_parcipant_region" />
		<xsl:param name="param_parcipant_organization" />
		<xsl:param name="param_parcipant_person" />
		<xsl:param name="param_parcipant_department" />
		<xsl:param name="param_parcipant_post" />
		<xsl:param name="param_parcipant_contactInfo" />
		<xsl:param name="param_parcipant_comment" />
		<xsl:param name="param_parcipants_name" />
		<xsl:param name="param_parcipants_region" />
		<xsl:param name="param_parcipants_organization" />
		<xsl:param name="param_parcipants_person" />
		<xsl:param name="param_parcipants_department" />
		<xsl:param name="param_parcipants_post" />
		<xsl:param name="param_parcipants_contactInfo" />
		<xsl:param name="param_parcipants_comment" />
		<xsl:param name="param_parcipants_attr_id" />
		<xsl:param name="param_comment" />
		<xsl:param name="param_attr_localId" />
		<xsl:param name="param_attr_id" />
	
		<xsl:call-template name="paste-non-empty"> <!--xdms:designation-->
			<xsl:with-param name="param_element">
				<xdms:designation>
					<xsl:value-of select="attribute[@code=$param_designation]/value" />
				</xdms:designation>
			</xsl:with-param>
		</xsl:call-template>
		
		<xsl:call-template name="paste-non-empty"> <!--xdms:text-->
			<xsl:with-param name="param_element">
				<xdms:text>
					<xsl:value-of select="attribute[@code=$param_text]/value" />
				</xdms:text>
			</xsl:with-param>
		</xsl:call-template>
		
		<xsl:call-template name="paste-non-empty"> <!--xdms:deadline-->
			<xsl:with-param name="param_element">
				<xdms:deadline>
					<xsl:value-of select="attribute[@code=$param_deadline]/value" />
				</xdms:deadline>
			</xsl:with-param>
		</xsl:call-template>
		
		<xsl:variable name="principalElement">
			<xdms:principal>
				<xsl:call-template name="addressee">
					<xsl:with-param name="param_region" select="$param_principal_region" />
					<xsl:with-param name="param_organization" select="$param_principal_organization" />
					<xsl:with-param name="param_person" select="$param_principal_person" />
					<xsl:with-param name="param_department" select="$param_principal_department" />
					<xsl:with-param name="param_post" select="$param_principal_post" />
					<xsl:with-param name="param_contactInfo" select="$param_principal_contactInfo" />
					<xsl:with-param name="param_comment" select="$param_principal_comment" />
				</xsl:call-template>
			</xdms:principal>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element" select="$principalElement" />
		</xsl:call-template>
		
		<xsl:variable name="principalsElement">
			<xdms:principals>
				<xsl:call-template name="addresseeList">
					<xsl:with-param name="param_name" select="$param_principals_name" />
					<xsl:with-param name="param_region" select="$param_principals_region" />
					<xsl:with-param name="param_organization" select="$param_principals_organization" />
					<xsl:with-param name="param_person" select="$param_principals_person" />
					<xsl:with-param name="param_department" select="$param_principals_department" />
					<xsl:with-param name="param_post" select="$param_principals_post" />
					<xsl:with-param name="param_contactInfo" select="$param_principals_contactInfo" />
					<xsl:with-param name="param_comment" select="$param_principals_comment" />
					<xsl:with-param name="param_id" select="$param_principals_attr_id" />
				</xsl:call-template>
			</xdms:principals>
		</xsl:variable>
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element" select="$principalsElement" />
		</xsl:call-template>
		
		<xsl:variable name="parcipantsElement">
			<xdms:parcipants>
				<xsl:variable name="parcipantElement">
					<xdms:parcipant>
						<xsl:call-template name="addressee">
							<xsl:with-param name="param_region" select="$param_parcipant_region" />
							<xsl:with-param name="param_organization" select="$param_parcipant_organization" />
							<xsl:with-param name="param_person" select="$param_parcipant_person" />
							<xsl:with-param name="param_department" select="$param_parcipant_department" />
							<xsl:with-param name="param_post" select="$param_parcipant_post" />
							<xsl:with-param name="param_contactInfo" select="$param_parcipant_contactInfo" />
							<xsl:with-param name="param_comment" select="$param_parcipant_comment" />
						</xsl:call-template>
					</xdms:parcipant>
				</xsl:variable>
				<xsl:call-template name="paste-non-empty">
					<xsl:with-param name="param_element" select="$parcipantElement" />
				</xsl:call-template>
		
				<xsl:variable name="parcipantsSubElement">
					<xdms:parcipants>
						<xsl:call-template name="addresseeList">
							<xsl:with-param name="param_name" select="$param_parcipants_name" />
							<xsl:with-param name="param_region" select="$param_parcipants_region" />
							<xsl:with-param name="param_organization" select="$param_parcipants_organization" />	
							<xsl:with-param name="param_person" select="$param_parcipants_person" />
							<xsl:with-param name="param_department" select="$param_parcipants_department" />
							<xsl:with-param name="param_post" select="$param_parcipants_post" />
							<xsl:with-param name="param_contactInfo" select="$param_parcipants_contactInfo" />
							<xsl:with-param name="param_comment" select="$param_parcipants_comment" />
							<xsl:with-param name="param_id" select="$param_parcipants_attr_id" />
						</xsl:call-template>
					</xdms:parcipants>
				</xsl:variable>
				<xsl:call-template name="paste-non-empty">
					<xsl:with-param name="param_element" select="$parcipantsSubElement" />
				</xsl:call-template>
			</xdms:parcipants>
		</xsl:variable>
		
		<xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element" select="$parcipantsElement" />
		</xsl:call-template>
				
		<xsl:call-template name="paste-non-empty"> <!--xdms:comment-->
			<xsl:with-param name="param_element">
				<xdms:comment>
					<xsl:value-of select="attribute[@code=$param_comment]/value" />
				</xdms:comment>
			</xsl:with-param>
		</xsl:call-template>
		
		<xsl:call-template name="paste-non-empty-attribute">
			<xsl:with-param name="param_name" select="'localId'" />
			<xsl:with-param name="param_value" select="$param_attr_localId" />
		</xsl:call-template>
		
		<xsl:call-template name="paste-non-empty-attribute">
			<xsl:with-param name="param_name" select="'id'" />
			<xsl:with-param name="param_value" select="$param_attr_id" />
		</xsl:call-template>
		
	</xsl:template>
	
	<xsl:template name="signatory">
		<xsl:param name="param_region" />
		<xsl:param name="param_organization" />
		<xsl:param name="param_person" />
		<xsl:param name="param_department" />
		<xsl:param name="param_post" />
		<xsl:param name="param_contactInfo" />
		<xsl:param name="param_signed" />
		<xsl:param name="param_comment" />
		<xsl:call-template name="anyone">
			<xsl:with-param name="param_region" select="$param_region" />
			<xsl:with-param name="param_organization" select="$param_organization" />
			<xsl:with-param name="param_person" select="$param_person"/>
			<xsl:with-param name="param_department" select="$param_department" />
			<xsl:with-param name="param_post" select="$param_post" />
			<xsl:with-param name="param_contactInfo" select="$param_contactInfo" />
		</xsl:call-template>
		<xdms:signed>
			<xsl:value-of select="attribute[@code=$param_signed]/value" />
		</xdms:signed>
		<xsl:call-template name="paste-non-empty"> <!--xdms:comment-->
			<xsl:with-param name="param_element" >
				<xdms:comment> 
					<xsl:value-of select="attribute[@code=$param_comment]/value" />
				</xdms:comment>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="associatedFile">
		<!-- Атрибут localName - обязательный, localId - необязательный, type - необязательный -->
		<xsl:param name="param_group" />
		<xsl:param name="param_description" />
		<xsl:param name="param_pages" />
		<xsl:param name="param_eds" />
		<xsl:param name="param_localName" />
		<xsl:param name="param_localId" />
		<xsl:param name="param_type" />
		
		<xsl:attribute name="xdms:localName"> <!-- Обязательный -->
			<xsl:value-of select='$param_localName'/>
		</xsl:attribute>
		
		<xsl:call-template name="paste-non-empty-attribute">
			<xsl:with-param name="param_name" select="'localId'" />
			<xsl:with-param name="param_value" select="$param_localId" />
		</xsl:call-template>
		
		<xsl:call-template name="paste-non-empty-attribute">
			<xsl:with-param name="param_name" select="'type'" />
			<xsl:with-param name="param_value" select="$param_type" />
		</xsl:call-template>
				
		<xdms:group>
			<xsl:value-of select="'Текст документа'" />
		</xdms:group>
		
		<xsl:call-template name="paste-non-empty"> <!-- xdms:description -->
			<xsl:with-param name="param_element">
				<xdms:description> <!-- Необязательный; значение не пустое -->
					<xsl:value-of select="attribute[@code=$param_description]/value" />
				</xdms:description>
			</xsl:with-param>
		</xsl:call-template>
		
		<xsl:call-template name="paste-non-empty"> <!-- xdms:pages -->
			<xsl:with-param name="param_element">
				<xdms:pages>
					<xsl:value-of select="attribute[@code=$param_pages]/value" />
				</xdms:pages>
			</xsl:with-param>
		</xsl:call-template>

		<!-- xdms:eds -->
		<!-- commented for BR4J00040363 (unused tag) -->
		<!--xsl:call-template name="paste-non-empty">
			<xsl:with-param name="param_element">
				<xdms:eds>
					<xsl:value-of select="$param_eds" />
				</xdms:eds>
			</xsl:with-param>
		</xsl:call-template-->
		
	</xsl:template>

	<xsl:template name="deliveryDestination">
		<xsl:param name="param_organization" />
		<xsl:param name="param_region" />
		<xsl:param name="param_system" />
		<xsl:param name="param_details" />
		<xsl:param name="param_files" />
		
		<xdms:destination> <!-- Обязательный -->
			<xsl:call-template name="communicationPartner">
				<xsl:with-param name="param_organization" select="$param_organization"/>
				<xsl:with-param name="param_region" select="$param_region"/>
				<xsl:with-param name="param_system" select="$param_system"/>
				<xsl:with-param name="param_details" select="$param_details"/>
			</xsl:call-template>
		</xdms:destination>
		
		<xsl:call-template name="paste-non-empty"> <!-- xdms:files -->
			<xsl:with-param name="param_element">
				<xdms:files> <!-- Необязательный, может встречаться максимум один раз -->		
					<xsl:value-of select="attribute[@code=$param_files]/value"/>
				</xdms:files>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="paste-non-empty">
		<xsl:param name="param_element" />
		<xsl:if test="normalize-space($param_element)!=''" >
			<xsl:copy-of select="$param_element"/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="paste-non-empty-attribute">
		<xsl:param name="param_name" />
		<xsl:param name="param_value" />
		<xsl:variable name="value" select="attribute[@code=$param_value]/value" />
		<xsl:if test="normalize-space($value)!=''" >
			<xsl:attribute name="{$param_name}">
				<xsl:value-of select="$value" />
			</xsl:attribute>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="classifyItemAttrs">
		<xsl:param name="param_id" />
		<xsl:param name="param_retro" />
		<xsl:param name="param_modified" />		
		
		<xsl:call-template name="paste-non-empty-attribute">
			<xsl:with-param name="param_name" select="'id'" />
			<xsl:with-param name="param_value" select="$param_id" />
		</xsl:call-template>
		<xsl:call-template name="paste-non-empty-attribute">
			<xsl:with-param name="param_name" select="'retro'" />
			<xsl:with-param name="param_value" select="$param_retro" />
		</xsl:call-template>
		<xsl:call-template name="paste-non-empty-attribute">
			<xsl:with-param name="param_name" select="'modified'" />
			<xsl:with-param name="param_value" select="$param_modified" />
		</xsl:call-template>
	</xsl:template>

</xsl:stylesheet>
