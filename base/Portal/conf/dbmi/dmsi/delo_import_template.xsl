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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
		
	<!-- Case to GOST -->
	
	<xsl:template match="/">
		<xsl:apply-templates select="passport"/>
		<xsl:apply-templates select="refusal"/>
		<xsl:apply-templates select="receipt"/>		
	</xsl:template>
	
	<xsl:template match="/refusal">
		<Header>
			<xsl:apply-templates select="header"/>
			<xsl:apply-templates select="Acknowledgement"/>
		</Header>
	</xsl:template>
	
	<xsl:template match="/receipt">
		<Header>
			<xsl:apply-templates select="header"/>
			<xsl:apply-templates select="Acknowledgement"/>
		</Header>
	</xsl:template>
	
	<xsl:template match="/passport">
		<Header>
			<xsl:apply-templates select="header"/>
			<xsl:apply-templates select="document"/>
			<xsl:apply-templates select="AddDocuments"/>
			<xsl:apply-templates select="Expansion"/>
			<xsl:apply-templates select="Acknowledgement"/>
		</Header>
	</xsl:template>	
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>


<!--Элемент Header-->	
	<xsl:template match="header">
		<xsl:apply-templates select="@*"/>
	</xsl:template>
	
	<xsl:template match="header/@standart">
		<xsl:attribute name="standart">Стандарт системы управления документами</xsl:attribute>
	</xsl:template>	
	
	<xsl:template match="header/@version">
		<xsl:attribute name="version">1.0</xsl:attribute>
	</xsl:template>	

<!--DateTime-->
	<xsl:template match="@time">
		<xsl:attribute name="time"><xsl:value-of select="."/>T00:00:00.000</xsl:attribute>
	</xsl:template>
	
	
<!--Элемент Document-->
	<xsl:template match="/passport/document">
		<Document>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates select="RegNumber"/>
			<xsl:apply-templates select="RegNumer"/>
			<xsl:apply-templates select="Confident"/>
			<xsl:apply-templates select="DocNumber"/>
			<xsl:apply-templates select="Addressee"/>
			<xsl:apply-templates select="DocTransfer"/>
			<xsl:apply-templates select="Reghistory"/>
			<xsl:apply-templates select="Author"/>
			<xsl:apply-templates select="Validator"/>
			<xsl:apply-templates select="Writer"/>
		</Document>
		<TaskList>
			<xsl:apply-templates select="Task"/>
		</TaskList>
	</xsl:template>
	
	<xsl:template match="/passport/document/Reghistory">
		<RegHistory>
			<xsl:apply-templates select="@*|node()"/>
		</RegHistory>
	</xsl:template>
	
	<xsl:template match="Reghistory/Organization">
		<OrganizationOnly>
			<xsl:attribute name="organization_string"><xsl:value-of select="normalize-space(concat(@fullname,' ',@shortname,' ',@ownership,' ',@ogrn,' ',@inn))"/></xsl:attribute>
			<xsl:apply-templates select="@*|node()"/>
		</OrganizationOnly>
	</xsl:template>
	
	<xsl:template match="Author/Organization">
		<OrganizationWithSign>
			<xsl:attribute name="organization_string"><xsl:value-of select="normalize-space(concat(@fullname,' ',@shortname,' ',@ownership,' ',@ogrn,' ',@inn))"/></xsl:attribute>
			<xsl:apply-templates select="@*|node()"/>
		</OrganizationWithSign>
	</xsl:template>
	
	<xsl:template match="Executor/Organization">
		<Organization>
			<xsl:attribute name="organization_string"><xsl:value-of select="normalize-space(concat(@fullname,' ',@shortname,' ',@ownership,' ',@ogrn,' ',@inn))"/></xsl:attribute>
			<xsl:apply-templates select="@*|node()"/>
		</Organization>
	</xsl:template>
	
	<xsl:template match="/passport/document/Author/PrivatePerson">
		<PrivatePersonWithSign>
			<xsl:choose > 
				<xsl:when test="boolean(normalize-space(@pas_ser))">
					<xsl:if test="@inn"><xsl:attribute name="inn"><xsl:value-of select="@inn"/></xsl:attribute></xsl:if>
					<xsl:attribute name="doc_kind">паспорт</xsl:attribute>
					<xsl:attribute name="doc_num"><xsl:value-of select="concat(@pas_ser,' ',@pas_num)"/></xsl:attribute>
					<xsl:attribute name="doc_org"><xsl:value-of select="@pas_org"/></xsl:attribute>
					<xsl:attribute name="doc_date"><xsl:value-of select="@pas_date"/></xsl:attribute>
					<xsl:apply-templates select="node()"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="@inn|node()"/>
				</xsl:otherwise>
			</xsl:choose>
		</PrivatePersonWithSign>
	</xsl:template>
	
	<xsl:template match="Author/Organization/OfficialPerson">
		<OfficialPersonWithSign>
			<xsl:apply-templates select="@*|node()"/>
		</OfficialPersonWithSign>
	</xsl:template>
	
	<xsl:template match="Validator/Organization">
		<OrganizationWithSign>
			<xsl:attribute name="organization_string"><xsl:value-of select="normalize-space(concat(@fullname,' ',@shortname,' ',@ownership,' ',@ogrn,' ',@inn))"/></xsl:attribute>
			<xsl:apply-templates select="@*|node()"/>
		</OrganizationWithSign>
	</xsl:template>
	
	<xsl:template match="/passport/document/Validator/Organization/OfficialPerson">
		<OfficialPersonWithSign>
			<xsl:apply-templates select="@*|node()"/>
		</OfficialPersonWithSign>
	</xsl:template>

	<xsl:template match="/passport/document/Validator/PrivatePerson">
		<PrivatePersonWithSign>
			<xsl:choose > 
				<xsl:when test="boolean(normalize-space(@pas_ser))">
					<xsl:if test="@inn"><xsl:attribute name="inn"><xsl:value-of select="@inn"/></xsl:attribute></xsl:if>
					<xsl:attribute name="doc_kind">паспорт</xsl:attribute>
					<xsl:attribute name="doc_num"><xsl:value-of select="concat(@pas_ser,' ',@pas_num)"/></xsl:attribute>
					<xsl:attribute name="doc_org"><xsl:value-of select="@pas_org"/></xsl:attribute>
					<xsl:attribute name="doc_date"><xsl:value-of select="@pas_date"/></xsl:attribute>
					<xsl:apply-templates select="node()"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="@inn|node()"/>
				</xsl:otherwise>
			</xsl:choose>
		</PrivatePersonWithSign>
	</xsl:template>
	
	<xsl:template match="/passport/document/Writer/PrivatePerson">
		<PrivatePerson>
			<xsl:choose > 
				<xsl:when test="boolean(normalize-space(@pas_ser))">
					<xsl:if test="@inn"><xsl:attribute name="inn"><xsl:value-of select="@inn"/></xsl:attribute></xsl:if>
					<xsl:attribute name="doc_kind">паспорт</xsl:attribute>
					<xsl:attribute name="doc_num"><xsl:value-of select="concat(@pas_ser,' ',@pas_num)"/></xsl:attribute>
					<xsl:attribute name="doc_org"><xsl:value-of select="@pas_org"/></xsl:attribute>
					<xsl:attribute name="doc_date"><xsl:value-of select="@pas_date"/></xsl:attribute>
					<xsl:apply-templates select="node()"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="@inn|node()"/>
				</xsl:otherwise>
			</xsl:choose>
		</PrivatePerson>
	</xsl:template>
	
	
	<xsl:template match="Writer/Organization">
		<Organization>
			<xsl:attribute name="organization_string"><xsl:value-of select="normalize-space(concat(@fullname,' ',@shortname,' ',@ownership,' ',@ogrn,' ',@inn))"/></xsl:attribute>
			<xsl:apply-templates select="@*|node()"/>
		</Organization>
	</xsl:template>
	
	<xsl:template match="Writer/PrivatePerson">
		<PrivatePerson>
			<xsl:choose > 
				<xsl:when test="boolean(normalize-space(@pas_ser))">
					<xsl:if test="@inn"><xsl:attribute name="inn"><xsl:value-of select="@inn"/></xsl:attribute></xsl:if>
					<xsl:attribute name="doc_kind">паспорт</xsl:attribute>
					<xsl:attribute name="doc_num"><xsl:value-of select="concat(@pas_ser,' ',@pas_num)"/></xsl:attribute>
					<xsl:attribute name="doc_org"><xsl:value-of select="@pas_org"/></xsl:attribute>
					<xsl:attribute name="doc_date"><xsl:value-of select="@pas_date"/></xsl:attribute>
					<xsl:apply-templates select="node()"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="@inn|node()"/>
				</xsl:otherwise>
			</xsl:choose>
		</PrivatePerson>
	</xsl:template>
	
<!--Элемент TaskList-->
	<xsl:template match="/passport/document/Task/Author">
		<AuthorOrganization>
			<xsl:apply-templates select="@*|node()"/>
		</AuthorOrganization>
	</xsl:template>
	
<!--Элемент AddDocuments-->
	<xsl:template match="/passport/AddDocuments">
		<AddDocuments>
			<xsl:apply-templates select="@*|node()"/>
		</AddDocuments>
	</xsl:template>
	
	<xsl:template match="/passport/AddDocuments/Folder">
		<Folder>
			<xsl:attribute name="contents"><xsl:value-of select="normalize-space(text())"/></xsl:attribute>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates select="DocTransfer"/>
			<xsl:apply-templates select="Note"/>
			<xsl:apply-templates select="Document"/>
		</Folder>
	</xsl:template>
	
	<xsl:template match="Folder/Document">
		<Referred>
			<xsl:attribute name="idnumber"><xsl:value-of select="@idnumber"/></xsl:attribute>
			<xsl:attribute name="retype">1</xsl:attribute>
			<xsl:apply-templates select="node()"/>
		</Referred>
	</xsl:template>
		
<!--Элемент Expansion-->
	<xsl:template match="/passport/Expansion">
		<Expansion>
			<xsl:apply-templates select="@*|node()"/>
		</Expansion>
	</xsl:template>
	
	
<!--Элемент Acknowledgement-->
	<xsl:template match="Acknowledgement">
		<Acknowledgement>
			<xsl:apply-templates select="@*|node()"/>
		</Acknowledgement>
	</xsl:template>
	
	<xsl:template match="Acknowledgement/@ack_type">
		<xsl:attribute name="ack_type">
			<xsl:choose>
				<xsl:when test=".='0'">2</xsl:when>
				<xsl:when test=".='1'">1</xsl:when>
				<xsl:when test=".='2'">1</xsl:when>
				<xsl:when test=".='3'">2</xsl:when>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>
	
<!---->	
	<xsl:template match="DocTransfer">
		<DocTransfer>
			<xsl:attribute name="transfertype">0</xsl:attribute>
			<xsl:apply-templates select="@*|node()"/>
		</DocTransfer>
	</xsl:template>
	
	<xsl:template match="AckResult">	
		<AckResult>
			<xsl:attribute name="errorcode"><xsl:value-of select="@errorcode"/></xsl:attribute>
			<xsl:value-of select="@errortext"/>
		</AckResult>
	</xsl:template>
	
	<xsl:template match="RegNumer">	
		<RegNumber>
			<xsl:apply-templates select="@*|node()"/>
		</RegNumber>
	</xsl:template>
	
	<xsl:template match="Acknowledgement/RegNumer">	
		<RegNumber>
			<xsl:attribute name="regdate"><xsl:value-of select="@regdate"/></xsl:attribute>
			<xsl:value-of select="@regnum"/>
		</RegNumber>
	</xsl:template>

	<xsl:template match="DocNumber/Organization">
		<OrganizationOnly>
			<xsl:attribute name="organization_string"><xsl:value-of select="normalize-space(concat(@fullname,' ',@shortname,' ',@ownership,' ',@ogrn,' ',@inn))"/></xsl:attribute>
			<xsl:apply-templates select="@*|node()"/>
		</OrganizationOnly>
	</xsl:template>
	
	<xsl:template match="Addressee/Organization">
		<Organization>
			<xsl:attribute name="organization_string"><xsl:value-of select="normalize-space(concat(@fullname,' ',@shortname,' ',@ownership,' ',@ogrn,' ',@inn))"/></xsl:attribute>
			<xsl:apply-templates select="@*|node()"/>
		</Organization>
	</xsl:template>
	
	
	<xsl:template match="Econtact/@type">
		<xsl:attribute name="type">
			<xsl:choose>
				<xsl:when test=".='р'">1</xsl:when>
				<xsl:when test=".='д'">2</xsl:when>
				<xsl:when test=".='м'">3</xsl:when>
				<xsl:when test=".='ф'">4</xsl:when>
				<xsl:when test=".='п'">5</xsl:when>
				<xsl:when test=".='и'">6</xsl:when>
				<xsl:otherwise>0</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="Referred/@retype">
		<xsl:attribute name="retype">
			<xsl:choose>
				<xsl:when test=".='д'">1</xsl:when>
				<xsl:when test=".='з'">2</xsl:when>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="Author/@type">	
	</xsl:template>
	
	<xsl:template match="Addressee/@type">	
	</xsl:template>
	
	<xsl:template match="Validator/@type">	
	</xsl:template>
	
	<xsl:template match="Writer/@type">	
	</xsl:template>
	
</xsl:stylesheet>