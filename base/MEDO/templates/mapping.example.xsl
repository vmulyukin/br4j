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
 
    <xsl:output method="xml" indent="yes" encoding="windows-1251" />

	<xsl:template name="headerType">
		<xsl:variable name="type" select="@templateId" />
		<xsl:choose>
			<xsl:when test="$type='${template.jbr.outcoming}'">
				<xsl:value-of select="'Документ'"/>
			</xsl:when>
			<xsl:when test="$type='${template.med.resolutionAccept}'">
				<xsl:value-of select="'Уведомление'"/>
			</xsl:when>
			<xsl:when test="$type='${template.med.documentRegister}'">
				<xsl:value-of select="'Уведомление'"/>
			</xsl:when>
			<xsl:when test="$type='${template.med.reportSent}'">
				<xsl:value-of select="'Уведомление'"/>
			</xsl:when>
			<xsl:when test="$type='${template.med.registrDenied}'">
				<xsl:value-of select="'Уведомление'"/>
			</xsl:when>
			<xsl:when test="$type='id_template'">
				<xsl:value-of select="'Квитанция'"/>
			</xsl:when>
			<xsl:when test="$type='${template.med.notifyForApplicant}'">
				<xsl:value-of select="'Уведомление'"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="linkType">
		<xsl:param name="param_link_type" />
		<xsl:variable name="linkTypeValue" select="attribute[@code=$param_link_type]/value" />
		<xsl:if test="normalize-space($linkTypeValue)!=''">
			<xsl:choose>
				<xsl:when test="$linkTypeValue='${referencevalue.jbr.inResponse}'">
					<xsl:value-of select="'В ответ на ...'"/>
				</xsl:when>
				<xsl:when test="$linkTypeValue='${referencevalue.jbr.Response}'">
					<xsl:value-of select="'Oтвет'"/>
				</xsl:when>
				<xsl:when test="$linkTypeValue='${referencevalue.jbr.Execute}'">
					<xsl:value-of select="'&#x0418;сполнение'"/>
				</xsl:when>
				<xsl:when test="$linkTypeValue='${referencevalue.jbr.inExecute}'">
					<xsl:value-of select="'Во исполнение'"/>
				</xsl:when>
				<xsl:when test="$linkTypeValue='${referencevalue.jbr.inLikedWith}'">
					<xsl:value-of select="'В связи с ...'"/>
				</xsl:when>
				<xsl:when test="$linkTypeValue='${referencevalue.jbr.medo_og.informationResponseDocType}'">
					<xsl:value-of select="'Связан с ...'"/>
				</xsl:when>
				<xsl:when test="$linkTypeValue='${referencevalue.jbr.executeService}'">
					<xsl:value-of select="'&#x0418;сполнение услуги'"/>
				</xsl:when>	
			</xsl:choose>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="typeElement">
		<xsl:variable name="type" select="@templateId" />
		<xsl:choose>
			<xsl:when test="$type='${template.jbr.outcoming}'">
				<xsl:call-template name="document_files" />
			</xsl:when>
			<xsl:when test="$type='${template.med.resolutionAccept}'">
				<xsl:call-template name="notification_tmpl" />
			</xsl:when>
			<xsl:when test="$type='${template.med.documentRegister}'">
				<xsl:call-template name="notification_tmpl" />
			</xsl:when>
			<xsl:when test="$type='${template.med.reportSent}'">
				<xsl:call-template name="notification_tmpl" />
			</xsl:when>
			<xsl:when test="$type='${template.med.registrDenied}'">
				<xsl:call-template name="notification_tmpl" />
			</xsl:when>
			<xsl:when test="$type='${template.med.notifyForApplicant}'">
				<xsl:call-template name="notification_tmpl" />
			</xsl:when>
			<xsl:when test="$type='ID_template3'">
				
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="notificationType">
		<xsl:variable name="type" select="@templateId" />
		<xsl:choose>
			<xsl:when test="$type='${template.med.documentRegister}'">
				<xsl:value-of select="'Зарегистрирован'"/>
			</xsl:when>
			<xsl:when test="$type='${template.med.registrDenied}'">
				<xsl:value-of select="'Отказано в регистрации'"/>
			</xsl:when>
			<xsl:when test="$type='${template.med.resolutionAccept}'">
				<xsl:value-of select="'Назначен исполнитель'"/>
			</xsl:when>
			<xsl:when test="$type='ID_template4'">
				<xsl:value-of select="'Доклад подготовлен'"/>
			</xsl:when>
			<xsl:when test="$type='${template.med.reportSent}'">
				<xsl:value-of select="'Доклад направлен'"/>
			</xsl:when>
			<xsl:when test="$type='ID_template6'">
				<xsl:value-of select="'&#x0418;сполнение'"/>
			</xsl:when>
			<xsl:when test="$type='${template.med.notifyForApplicant}'">
				<xsl:value-of select="'&#x0418;сполнение услуги'"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'Отказано в регистрации'"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="notificationElement">
		<xsl:variable name="type" select="@templateId" />
		<xsl:choose>
			<xsl:when test="$type='${template.med.documentRegister}'"> <!-- Зареген -->
				<xsl:call-template name="documentAccepted">
					<xsl:with-param name="param_documentAccepted_number" select = "'${notification.documentAccepted.num.number}'" />
					<xsl:with-param name="param_documentAccepted_date" select = "'${notification.documentAccepted.num.date}'" />	
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$type='${template.med.registrDenied}'"> <!-- Отказано в регистрации -->
				<xsl:call-template name="documentRefused">
					<xsl:with-param name="param_documentRefused_reason" select = "'${notification.documentRefused.reason}'" />	
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$type='${template.med.notifyForApplicant}'"> <!-- &#x0418;сполнение услуги -->
				<xsl:call-template name="executionService">
					<xsl:with-param name="param_executionService_reason" select = "'${notification.executionService.reason}'" />	
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$type='${template.med.resolutionAccept}'"> <!-- Назначен исполнитель -->
				<xsl:call-template name="executorAssigned">
					<xsl:with-param name="param_executorAssigned_secretary_region" select = "'${notification.executorAssigned.secretary.region}'" />	
					<xsl:with-param name="param_executorAssigned_secretary_organization" select = "'${notification.executorAssigned.secretary.organization}'" />
					<xsl:with-param name="param_executorAssigned_secretary_person" select = "'${notification.executorAssigned.secretary.person}'" />
					<xsl:with-param name="param_executorAssigned_secretary_department" select = "'${notification.executorAssigned.secretary.department}'" />
					<xsl:with-param name="param_executorAssigned_secretary_post" select = "'${notification.executorAssigned.secretary.post}'" />
					<xsl:with-param name="param_executorAssigned_secretary_contactInfo" select = "'${notification.executorAssigned.secretary.contactInfo}'" />
					<xsl:with-param name="param_executorAssigned_secretary_comment" select = "'${notification.executorAssigned.secretary.comment}'" />
					<xsl:with-param name="param_executorAssigned_manager_region" select = "'${notification.executorAssigned.manager.region}'" />
					<xsl:with-param name="param_executorAssigned_manager_organization" select = "'${notification.executorAssigned.manager.organization}'" />
					<xsl:with-param name="param_executorAssigned_manager_person" select = "'${notification.executorAssigned.manager.person}'" />
					<xsl:with-param name="param_executorAssigned_manager_department" select = "'${notification.executorAssigned.manager.department}'" />
					<xsl:with-param name="param_executorAssigned_manager_post" select = "'${notification.executorAssigned.manager.post}'" />
					<xsl:with-param name="param_executorAssigned_manager_contactInfo" select = "'${notification.executorAssigned.manager.contactInfo}'" />
					<xsl:with-param name="param_executorAssigned_manager_comment" select = "'${notification.executorAssigned.manager.comment}'" />
					<xsl:with-param name="param_executorAssigned_executor_region" select = "'${notification.executorAssigned.executor.region}'" />
					<xsl:with-param name="param_executorAssigned_executor_organization" select = "'${notification.executorAssigned.executor.organization}'" />
					<xsl:with-param name="param_executorAssigned_executor_person" select = "'${notification.executorAssigned.executor.person}'" />
					<xsl:with-param name="param_executorAssigned_executor_department" select = "'${notification.executorAssigned.executor.department}'" />
					<xsl:with-param name="param_executorAssigned_executor_post" select = "'${notification.executorAssigned.executor.post}'" />
					<xsl:with-param name="param_executorAssigned_executor_contactInfo" select = "'${notification.executorAssigned.executor.contactInfo}'" />
					<xsl:with-param name="param_executorAssigned_executor_comment" select = "'${notification.executorAssigned.executor.comment}'" />
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$type='ID_template4'"> <!-- Доклад подготовлен -->
				<xsl:call-template name="reportPrepared">
					<xsl:with-param name="param_reportPrepared_signatory_region" select = "'${notification.reportPrepared.signatory.region}'" />	
					<xsl:with-param name="param_reportPrepared_signatory_organization" select = "'${notification.reportPrepared.signatory.organization}'" />
					<xsl:with-param name="param_reportPrepared_signatory_person" select = "'${notification.reportPrepared.signatory.person}'" />
					<xsl:with-param name="param_reportPrepared_signatory_department" select = "'${notification.reportPrepared.signatory.department}'" />
					<xsl:with-param name="param_reportPrepared_signatory_post" select = "'${notification.reportPrepared.signatory.post}'" />
					<xsl:with-param name="param_reportPrepared_signatory_contactInfo" select = "'${notification.reportPrepared.signatory.contactInfo}'" />
					<xsl:with-param name="param_reportPrepared_signatory_signed" select = "'${notification.reportPrepared.signatory.signed}'" />
					<xsl:with-param name="param_reportPrepared_signatory_comment" select = "'${notification.reportPrepared.signatory.comment}'" />
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$type='${template.med.reportSent}'"> <!-- Доклад направлен -->
				<xsl:call-template name="reportSent">	
					<xsl:with-param name="param_reportSent_report_region" select = "'${notification.reportSent.report.region}'" />
					<xsl:with-param name="param_reportSent_report_organization" select = "'${notification.reportSent.report.organization}'" />
					<xsl:with-param name="param_reportSent_report_person" select = "'${notification.reportSent.report.person}'" />
					<xsl:with-param name="param_reportSent_report_department" select = "'${notification.reportSent.report.department}'" />
					<xsl:with-param name="param_reportSent_report_post" select = "'${notification.reportSent.report.post}'" />
					<xsl:with-param name="param_reportSent_report_num_number" select = "'${notification.reportSent.report.num.number}'" />
					<xsl:with-param name="param_reportSent_report_num_date" select = "'${notification.reportSent.report.num.date}'" />
					<xsl:with-param name="param_reportSent_report_comment" select = "'${notification.reportSent.report.comment}'" />
					<xsl:with-param name="param_reportSent_report_attrRegion_id" select = "'${notification.reportSent.report.attrRegion.id}'" />
					<xsl:with-param name="param_reportSent_report_attrRegion_retro" select = "'${notification.reportSent.report.attrRegion.retro}'" />
					<xsl:with-param name="param_reportSent_report_attrRegion_modified" select = "'${notification.reportSent.report.attrRegion.modified}'" />
					<xsl:with-param name="param_reportSent_report_attrOrganization_id" select = "'${notification.reportSent.report.attrOrganization.id}'" />
					<xsl:with-param name="param_reportSent_report_attrOrganization_retro" select = "'${notification.reportSent.report.attrOrganization.retro}'" />
					<xsl:with-param name="param_reportSent_report_attrOrganization_modified" select = "'${notification.reportSent.report.attrOrganization.modified}'" />
					<xsl:with-param name="param_reportSent_report_attrPerson_id" select = "'${notification.reportSent.report.attrPerson.id}'" />
					<xsl:with-param name="param_reportSent_report_attrPerson_retro" select = "'${notification.reportSent.report.attrPerson.retro}'" />
					<xsl:with-param name="param_reportSent_report_attrPerson_modified" select = "'${notification.reportSent.report.attrPerson.modified}'" />
					<xsl:with-param name="param_reportSent_report_attrDepartment_id" select = "'${notification.reportSent.report.attrDepartment.id}'" />
					<xsl:with-param name="param_reportSent_report_attrDepartment_retro" select = "'${notification.reportSent.report.attrDepartment.retro}'" />
					<xsl:with-param name="param_reportSent_report_attrDepartment_modified" select = "'${notification.reportSent.report.attrDepartment.modified}'" />
					<xsl:with-param name="param_reportSent_report_attrPost_id" select = "'${notification.reportSent.report.attrPost.id}'" />
					<xsl:with-param name="param_reportSent_report_attrPost_retro" select = "'${notification.reportSent.report.attrPost.retro}'" />
					<xsl:with-param name="param_reportSent_report_attrPost_modified" select = "'${notification.reportSent.report.attrPost.modified}'" />
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$type='ID_template6'"> <!-- исполнение -->
				<xsl:call-template name="courseChanged">
					<xsl:with-param name="param_courseChanged_courseText" select = "'${notification.courseChanged.courseText}'" />
					<xsl:with-param name="param_courseChanged_reference_region" select = "'${notification.courseChanged.reference.region}'" />
					<xsl:with-param name="param_courseChanged_reference_organization" select = "'${notification.courseChanged.reference.organization}'" />
					<xsl:with-param name="param_courseChanged_reference_person" select = "'${notification.courseChanged.reference.person}'" />
					<xsl:with-param name="param_courseChanged_reference_department" select = "'${notification.courseChanged.reference.department}'" />
					<xsl:with-param name="param_courseChanged_reference_post" select = "'${notification.courseChanged.reference.post}'" />
					<xsl:with-param name="param_courseChanged_reference_num_number" select = "'${notification.courseChanged.reference.num.number}'" />
					<xsl:with-param name="param_courseChanged_reference_num_date" select = "'${notification.courseChanged.reference.num.date}'" />
					<xsl:with-param name="param_courseChanged_reference_comment" select = "'${notification.courseChanged.reference.comment}'" />
					<xsl:with-param name="param_courseChanged_reference_attrRegion_id" select = "'${notification.courseChanged.reference.attrRegion.id}'" />
					<xsl:with-param name="param_courseChanged_reference_attrRegion_retro" select = "'${notification.courseChanged.reference.attrRegion.retro}'" />
					<xsl:with-param name="param_courseChanged_reference_attrRegion_modified" select = "'${notification.courseChanged.reference.attrRegion.modified}'" />
					<xsl:with-param name="param_courseChanged_reference_attrOrganization_id" select = "'${notification.courseChanged.reference.attrOrganization.id}'" />
					<xsl:with-param name="param_courseChanged_reference_attrOrganization_retro" select = "'${notification.courseChanged.reference.attrOrganization.retro}'" />
					<xsl:with-param name="param_courseChanged_reference_attrOrganization_modified" select = "'${notification.courseChanged.reference.attrOrganization.modified}'" />
					<xsl:with-param name="param_courseChanged_reference_attrPerson_id" select = "'${notification.courseChanged.reference.attrPerson.id}'" />
					<xsl:with-param name="param_courseChanged_reference_attrPerson_retro" select = "'${notification.courseChanged.reference.attrPerson.retro}'" />
					<xsl:with-param name="param_courseChanged_reference_attrPerson_modified" select = "'${notification.courseChanged.reference.attrPerson.modified}'" />
					<xsl:with-param name="param_courseChanged_reference_attrDepartment_id" select = "'${notification.courseChanged.reference.attrDepartment.id}'" />
					<xsl:with-param name="param_courseChanged_reference_attrDepartment_retro" select = "'${notification.courseChanged.reference.attrDepartment.retro}'" />
					<xsl:with-param name="param_courseChanged_reference_attrDepartment_modified" select = "'${notification.courseChanged.reference.attrDepartment.modified}'" />
					<xsl:with-param name="param_courseChanged_reference_attrPost_id" select = "'${notification.courseChanged.reference.attrPost.id}'" />
					<xsl:with-param name="param_courseChanged_reference_attrPost_retro" select = "'${notification.courseChanged.reference.attrPost.retro}'" />
					<xsl:with-param name="param_courseChanged_reference_attrPost_modified" select = "'${notification.courseChanged.reference.attrPost.modified}'" />
				</xsl:call-template>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>