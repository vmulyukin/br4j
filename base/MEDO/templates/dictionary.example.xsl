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

	<!--xsl:template match="xdms:reason">
		<xsl:variable name="type" select="." />
		<xsl:choose>
			<xsl:when test="$type='Не подлежит регистрации'">
				<xsl:text>${id.refusedReason.RegistrationIsNA}</xsl:text>
			</xsl:when>
			<xsl:when test="$type='Ошибка адресации'">
				<xsl:text>${id.refusedReason.AddressingError}</xsl:text>
			</xsl:when>
			<xsl:when test="$type='Не указан корреспондент'">
				<xsl:text>${id.refusedReason.CorrespondentIsNotDefined}</xsl:text>
			</xsl:when>
			<xsl:when test="$type='Нет искового заявления'">
				<xsl:text>${id.refusedReason.PetitionIsAbsent}</xsl:text>
			</xsl:when>
			<xsl:when test="$type='Отсутствует текст'">
				<xsl:text>${id.refusedReason.TextIsAbsent}</xsl:text>
			</xsl:when>
			<xsl:when test="$type='Нет подписи'">
				<xsl:text>${id.refusedReason.SignatoryIsAbsent}</xsl:text>
			</xsl:when>
			<xsl:when test="$type='Приложение отсутствует'">
				<xsl:text>${id.refusedReason.AttachmentIsAbsent}</xsl:text>
			</xsl:when>
			<xsl:when test="$type='Листаж приложения указан ошибочно'">
				<xsl:text>${id.refusedReason.AttachmentPagesIsIncorrect}</xsl:text>
			</xsl:when>
			<xsl:when test="$type='Несовпадение реквизитов приложения с приложенными документами'">
				<xsl:text>${id.refusedReason.DocumentEntriesInconsistency}</xsl:text>
			</xsl:when>
		</xsl:choose>
	</xsl:template-->
	
	<xsl:template match="xdms:insteadOfDistributed">
		<xsl:variable name="value" select="." />
		<xsl:choose>
			<xsl:when test="$value='true' or $value='1'">
				<xsl:text>${id.insteadOfDistributed.yes}</xsl:text>
			</xsl:when>
			<xsl:when test="$value='false' or $value='0'">
				<xsl:text>${id.insteadOfDistributed.no}</xsl:text>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:courseText">
		<xsl:variable name="statusCode" select="."/>
		<xsl:choose>
			<xsl:when test="$statusCode='Обновление'">
				<xsl:text>${id.courseText.processingRequired}</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>${id.courseText.processed}</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>	

	<xsl:template name="deliveryType">
		<xsl:text>${id.deliveryType.MEDO}</xsl:text>
	</xsl:template>
</xsl:stylesheet>
