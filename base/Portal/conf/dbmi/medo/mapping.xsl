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
			<xsl:when test="$type='364'">
				<xsl:value-of select="'Документ'"/>
			</xsl:when>
			<xsl:when test="$type='990'">
				<xsl:value-of select="'Уведомление'"/>
			</xsl:when>
			<xsl:when test="$type='1025'">
				<xsl:value-of select="'Уведомление'"/>
			</xsl:when>
			<xsl:when test="$type='1065'">
				<xsl:value-of select="'Уведомление'"/>
			</xsl:when>
			<xsl:when test="$type='1066'">
				<xsl:value-of select="'Уведомление'"/>
			</xsl:when>
			<xsl:when test="$type='id_template'">
				<xsl:value-of select="'Квитанция'"/>
			</xsl:when>
			<xsl:when test="$type='995'">
				<xsl:value-of select="'Уведомление'"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="linkType">
		<xsl:param name="param_link_type" />
		<xsl:variable name="linkTypeValue" select="attribute[@code=$param_link_type]/value" />
		<xsl:if test="normalize-space($linkTypeValue)!=''">
			<xsl:choose>
				<xsl:when test="$linkTypeValue='1502'">
					<xsl:value-of select="'В ответ на ...'"/>
				</xsl:when>
				<xsl:when test="$linkTypeValue='1503'">
					<xsl:value-of select="'Oтвет'"/>
				</xsl:when>
				<xsl:when test="$linkTypeValue='1504'">
					<xsl:value-of select="'&#x0418;сполнение'"/>
				</xsl:when>
				<xsl:when test="$linkTypeValue='1601'">
					<xsl:value-of select="'Во исполнение'"/>
				</xsl:when>
				<xsl:when test="$linkTypeValue='1602'">
					<xsl:value-of select="'В связи с ...'"/>
				</xsl:when>
				<xsl:when test="$linkTypeValue='${referencevalue.jbr.medo_og.informationResponseDocType}'">
					<xsl:value-of select="'Связан с ...'"/>
				</xsl:when>
				<xsl:when test="$linkTypeValue='1604'">
					<xsl:value-of select="'&#x0418;сполнение услуги'"/>
				</xsl:when>	
			</xsl:choose>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="typeElement">
		<xsl:variable name="type" select="@templateId" />
		<xsl:choose>
			<xsl:when test="$type='364'">
				<xsl:call-template name="document_files" />
			</xsl:when>
			<xsl:when test="$type='990'">
				<xsl:call-template name="notification_tmpl" />
			</xsl:when>
			<xsl:when test="$type='1025'">
				<xsl:call-template name="notification_tmpl" />
			</xsl:when>
			<xsl:when test="$type='1065'">
				<xsl:call-template name="notification_tmpl" />
			</xsl:when>
			<xsl:when test="$type='1066'">
				<xsl:call-template name="notification_tmpl" />
			</xsl:when>
			<xsl:when test="$type='995'">
				<xsl:call-template name="notification_tmpl" />
			</xsl:when>
			<xsl:when test="$type='ID_template3'">
				
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="notificationType">
		<xsl:variable name="type" select="@templateId" />
		<xsl:choose>
			<xsl:when test="$type='1025'">
				<xsl:value-of select="'Зарегистрирован'"/>
			</xsl:when>
			<xsl:when test="$type='1066'">
				<xsl:value-of select="'Отказано в регистрации'"/>
			</xsl:when>
			<xsl:when test="$type='990'">
				<xsl:value-of select="'Назначен исполнитель'"/>
			</xsl:when>
			<xsl:when test="$type='ID_template4'">
				<xsl:value-of select="'Доклад подготовлен'"/>
			</xsl:when>
			<xsl:when test="$type='1065'">
				<xsl:value-of select="'Доклад направлен'"/>
			</xsl:when>
			<xsl:when test="$type='ID_template6'">
				<xsl:value-of select="'&#x0418;сполнение'"/>
			</xsl:when>
			<xsl:when test="$type='995'">
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
			<xsl:when test="$type='1025'"> <!-- Зареген -->
				<xsl:call-template name="documentAccepted">
					<xsl:with-param name="param_documentAccepted_number" select = "'MED_REGD_REGNUM'" />
					<xsl:with-param name="param_documentAccepted_date" select = "'MED_REGD_DATEREG'" />	
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$type='1066'"> <!-- Отказано в регистрации -->
				<xsl:call-template name="documentRefused">
					<xsl:with-param name="param_documentRefused_reason" select = "'MED_REAS_REFUS'" />	
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$type='995'"> <!-- &#x0418;сполнение услуги -->
				<xsl:call-template name="executionService">
					<xsl:with-param name="param_executionService_reason" select = "'MED_NTF_CMT_FOR_APT'" />	
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$type='990'"> <!-- Назначен исполнитель -->
				<xsl:call-template name="executorAssigned">
					<xsl:with-param name="param_executorAssigned_secretary_region" select = "'UNDEFINED'" />	
					<xsl:with-param name="param_executorAssigned_secretary_organization" select = "'JBR_SECRETARY_DORG_FULLNAME'" />
					<xsl:with-param name="param_executorAssigned_secretary_person" select = "'JBR_SECRETARY_PERS_FULL_NAME'" />
					<xsl:with-param name="param_executorAssigned_secretary_department" select = "'JBR_SECRETARY_DEPT_FULLNAME'" />
					<xsl:with-param name="param_executorAssigned_secretary_post" select = "'JBR_SECRETARY_PERS_POSITION'" />
					<xsl:with-param name="param_executorAssigned_secretary_contactInfo" select = "'JBR_SECRETARY_PERS_PHONE'" />
					<xsl:with-param name="param_executorAssigned_secretary_comment" select = "'UNDEFINED'" />
					<xsl:with-param name="param_executorAssigned_manager_region" select = "'UNDEFINED'" />
					<xsl:with-param name="param_executorAssigned_manager_organization" select = "'JBR_MANAGER_DORG_FULLNAME'" />
					<xsl:with-param name="param_executorAssigned_manager_person" select = "'JBR_MANAGER_PERS_FULL_NAME'" />
					<xsl:with-param name="param_executorAssigned_manager_department" select = "'JBR_MANAGER_DEPT_FULLNAME'" />
					<xsl:with-param name="param_executorAssigned_manager_post" select = "'JBR_MANAGER_PERS_POSITION'" />
					<xsl:with-param name="param_executorAssigned_manager_contactInfo" select = "'JBR_MANAGER_PERS_PHONE'" />
					<xsl:with-param name="param_executorAssigned_manager_comment" select = "'UNDEFINED'" />
					<xsl:with-param name="param_executorAssigned_executor_region" select = "'UNDEFINED'" />
					<xsl:with-param name="param_executorAssigned_executor_organization" select = "'JBR_EXECUTOR_DORG_FULLNAME'" />
					<xsl:with-param name="param_executorAssigned_executor_person" select = "'JBR_EXECUTOR_PERS_FULL_NAME'" />
					<xsl:with-param name="param_executorAssigned_executor_department" select = "'JBR_EXECUTOR_DEPT_FULLNAME'" />
					<xsl:with-param name="param_executorAssigned_executor_post" select = "'JBR_EXECUTOR_PERS_POSITION'" />
					<xsl:with-param name="param_executorAssigned_executor_contactInfo" select = "'JBR_EXECUTOR_PERS_PHONE'" />
					<xsl:with-param name="param_executorAssigned_executor_comment" select = "'UNDEFINED'" />
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$type='ID_template4'"> <!-- Доклад подготовлен -->
				<xsl:call-template name="reportPrepared">
					<xsl:with-param name="param_reportPrepared_signatory_region" select = "'UNDEFINED'" />	
					<xsl:with-param name="param_reportPrepared_signatory_organization" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportPrepared_signatory_person" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportPrepared_signatory_department" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportPrepared_signatory_post" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportPrepared_signatory_contactInfo" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportPrepared_signatory_signed" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportPrepared_signatory_comment" select = "'UNDEFINED'" />
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$type='1065'"> <!-- Доклад направлен -->
				<xsl:call-template name="reportSent">	
					<xsl:with-param name="param_reportSent_report_region" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportSent_report_organization" select = "'JBR_REPORT_DORG_FULLNAME'" />
					<xsl:with-param name="param_reportSent_report_person" select = "'JBR_REPORT_PERS_FULL_NAME'" />
					<xsl:with-param name="param_reportSent_report_department" select = "'JBR_REPORT_DEPT_FULLNAME'" />
					<xsl:with-param name="param_reportSent_report_post" select = "'JBR_REPORT_PERS_POSITION'" />
					<xsl:with-param name="param_reportSent_report_num_number" select = "'MED_OUTNUM_FOIV'" />
					<xsl:with-param name="param_reportSent_report_num_date" select = "'MED_DATE_OUTNUM_FOIV'" />
					<xsl:with-param name="param_reportSent_report_comment" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportSent_report_attrRegion_id" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportSent_report_attrRegion_retro" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportSent_report_attrRegion_modified" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportSent_report_attrOrganization_id" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportSent_report_attrOrganization_retro" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportSent_report_attrOrganization_modified" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportSent_report_attrPerson_id" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportSent_report_attrPerson_retro" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportSent_report_attrPerson_modified" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportSent_report_attrDepartment_id" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportSent_report_attrDepartment_retro" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportSent_report_attrDepartment_modified" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportSent_report_attrPost_id" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportSent_report_attrPost_retro" select = "'UNDEFINED'" />
					<xsl:with-param name="param_reportSent_report_attrPost_modified" select = "'UNDEFINED'" />
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$type='ID_template6'"> <!-- исполнение -->
				<xsl:call-template name="courseChanged">
					<xsl:with-param name="param_courseChanged_courseText" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_region" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_organization" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_person" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_department" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_post" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_num_number" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_num_date" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_comment" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_attrRegion_id" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_attrRegion_retro" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_attrRegion_modified" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_attrOrganization_id" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_attrOrganization_retro" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_attrOrganization_modified" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_attrPerson_id" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_attrPerson_retro" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_attrPerson_modified" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_attrDepartment_id" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_attrDepartment_retro" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_attrDepartment_modified" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_attrPost_id" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_attrPost_retro" select = "'UNDEFINED'" />
					<xsl:with-param name="param_courseChanged_reference_attrPost_modified" select = "'UNDEFINED'" />
				</xsl:call-template>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>