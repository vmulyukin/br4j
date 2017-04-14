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
	<xsl:output method="xml" indent="yes" encoding="UTF-16"/>
	
	<!-- GOST to Case -->
	
	<xsl:template match="/Header">
		<xsl:variable name="headerElement">
			<header>
				<xsl:if test="not(@to_org_id)">
					<xsl:attribute name="to_org_id"></xsl:attribute>
				</xsl:if>
				<xsl:if test="not(@to_sys_id)">
					<xsl:attribute name="to_sys_id"></xsl:attribute>
				</xsl:if>
				<xsl:if test="not(@to_system)">
					<xsl:attribute name="to_system"></xsl:attribute>
				</xsl:if>
				<xsl:apply-templates select="@*"/>
			</header>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="(Acknowledgement)">
				<xsl:choose>
					<xsl:when test="./Acknowledgement/@ack_type='2' and count(./Acknowledgement/AckResult[@errorcode!=0])>0">
						<refusal>
							<xsl:copy-of select="$headerElement" />
							<Acknowledgement ack_type='0'>
								<xsl:apply-templates select="Acknowledgement"/>
							</Acknowledgement>
						</refusal>
					</xsl:when>
					<xsl:when test="./Acknowledgement/@ack_type='2' and count(./Acknowledgement/AckResult[@errorcode!=0])=0">
						<receipt>
							<xsl:copy-of select="$headerElement" />
							<Acknowledgement ack_type='3'>
								<xsl:apply-templates select="Acknowledgement"/>
							</Acknowledgement>
						</receipt>
					</xsl:when>
					<xsl:otherwise>
						<passport>
							<xsl:copy-of select="$headerElement" />
							<Acknowledgement ack_type='2'>
								<xsl:apply-templates select="Acknowledgement"/>
							</Acknowledgement>
						</passport>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<passport>
					<xsl:copy-of select="$headerElement" />
					<xsl:if  test="(Document)">
						<document>
							<xsl:apply-templates select="Document"/>
						</document>
					</xsl:if >
					<xsl:if  test="(TaskList)">
						<TaskList>
							<xsl:apply-templates select="TaskList"/>
						</TaskList>
					</xsl:if >
					<xsl:if  test="(AddDocuments)">
						<AddDocuments>
							<xsl:apply-templates select="AddDocuments"/>
						</AddDocuments>
					</xsl:if >
					<xsl:if  test="(Expansion)">
						<Expansion>
							<xsl:apply-templates select="Expansion"/>
						</Expansion>
					</xsl:if >				
				</passport>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>	
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
<!--Элемент Header-->
	<xsl:template match="/Header/@standart">
		<xsl:attribute name="standart">Стандарт ДОУ</xsl:attribute>
	</xsl:template>	
	
	<xsl:template match="/Header/@version">
		<xsl:attribute name="version">1.0</xsl:attribute>
	</xsl:template>	
	
<!--DateTime-->
	<xsl:template match="/Header/@time">
		<xsl:attribute name="time"><xsl:value-of select="substring(.,1,10)"/></xsl:attribute>
	</xsl:template>
	
<!--Элемент Document-->
	<xsl:template match="/Header/Document">
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates select="RegNumber"/>
			<xsl:apply-templates select="Confident"/>
			<xsl:apply-templates select="DocNumber"/>
			<xsl:apply-templates select="DocTransfer"/>
			<xsl:apply-templates select="RegHistory"/>
			<xsl:apply-templates select="Author"/>
			<xsl:apply-templates select="Validator"/>
			<xsl:apply-templates select="Addressee"/>
			<xsl:if test="not(Writer)">
				<xsl:element name="Writer"></xsl:element>				
			</xsl:if>
			<xsl:apply-templates select="Writer"/>
	</xsl:template>
	
	<xsl:template match="/Header/Document/DocNumber">
		<xsl:if test="position() = 1">
			<DocNumber>
				<xsl:apply-templates select="@*|node()"/>
			</DocNumber>
		</xsl:if>
	</xsl:template>
	
<!--Элемент TaskList-->
	<xsl:template match="/Header/TaskList">
			<xsl:apply-templates select="@*|node()"/>
	</xsl:template>
	
<!--Элемент AddDocuments-->
	<xsl:template match="/Header/AddDocuments">
			<xsl:apply-templates select="@*|node()"/>
	</xsl:template>
	
<!--Элемент Expansion-->
	<xsl:template match="/Header/Expansion">
			<xsl:apply-templates select="@*|node()"/>
	</xsl:template>
	
<!--Элемент Acknowledgement-->
	<xsl:template match="/Header/Acknowledgement">
			<xsl:apply-templates select="@*|node()"/>
	</xsl:template>
	
	<xsl:template match="Acknowledgement/@ack_type">
		<!--xsl:attribute name="ack_type">
			<xsl:choose>
				<xsl:when test=".='1'">2</xsl:when>
				<xsl:when test=".='2'">3</xsl:when>
			</xsl:choose>
		</xsl:attribute-->
	</xsl:template>
	
	<xsl:template match="AckResult">	
		<AckResult>
			<xsl:attribute name="errorcode">0<!-- xsl:value-of select="@errorcode"/--></xsl:attribute>
			<xsl:attribute name="errortext"><xsl:value-of select="."/></xsl:attribute>
		</AckResult>
	</xsl:template>
	
	<xsl:template match="Acknowledgement/RegNumber">
		<RegNumer>
			<xsl:attribute name="regnum">
				<xsl:value-of select="."/>
			</xsl:attribute>
			<xsl:apply-templates select="@*"/>
		</RegNumer>
	</xsl:template>

<!---->
	<xsl:template match="OfficialPersonWithSign">
		<OfficialPerson>
			<xsl:apply-templates select="@*|node()"/>
		</OfficialPerson>
	</xsl:template>
	
	<xsl:template match="PrivatePersonWithSign|PrivatePerson">
		<PrivatePerson>
			<xsl:choose > 
				<xsl:when test="@doc_kind='паспорт' or @doc_kind='Паспорт'">
					<xsl:if test="@inn"><xsl:attribute name="inn"><xsl:value-of select="@inn"/></xsl:attribute></xsl:if>
					<xsl:attribute name="pas_ser"><xsl:value-of select="substring(@doc_num,1,5)"/></xsl:attribute>
					<xsl:attribute name="pas_num"><xsl:value-of select="substring(@doc_num,7,6)"/></xsl:attribute>
					<xsl:attribute name="pas_org"><xsl:value-of select="@doc_org"/></xsl:attribute>
					<xsl:attribute name="pas_date"><xsl:value-of select="@doc_date"/></xsl:attribute>
					<xsl:apply-templates select="node()"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="@inn|node()"/>
				</xsl:otherwise>
			</xsl:choose>
		</PrivatePerson>
	</xsl:template>
	
	<xsl:template match="AuthorOrganization">
		<Author>
			<xsl:attribute name="type">
			<xsl:choose>
				<xsl:when test="(OrganizationWithSign)">ю</xsl:when>
				<xsl:when test="(PrivatePersonWithSign)">ф</xsl:when>
			</xsl:choose>
		</xsl:attribute>
			<xsl:apply-templates select="@*|node()"/>
		</Author>
	</xsl:template>
	
	<xsl:template match="Author">
		<Author>
			<xsl:attribute name="type">
			<xsl:choose>
				<xsl:when test="(OrganizationWithSign)">ю</xsl:when>
				<xsl:when test="(PrivatePersonWithSign)">ф</xsl:when>
			</xsl:choose>
		</xsl:attribute>
			<xsl:apply-templates select="@*|node()"/>
		</Author>
	</xsl:template>
	
	<xsl:template match="Validator">
		<Validator>
			<xsl:attribute name="type">
			<xsl:choose>
				<xsl:when test="(OrganizationWithSign)">ю</xsl:when>
				<xsl:when test="(PrivatePersonWithSign)">ф</xsl:when>
				<xsl:when test="(DocNumber)">д</xsl:when>
			</xsl:choose>
		</xsl:attribute>
			<xsl:apply-templates select="@*|node()"/>
		</Validator>
	</xsl:template>
	
	<xsl:template match="Addressee">
		<Addressee>
			<xsl:attribute name="type">
			<xsl:choose>
				<xsl:when test="(Organization)">ю</xsl:when>
				<xsl:when test="(PrivatePerson)">ф</xsl:when>
			</xsl:choose>
		</xsl:attribute>
			<xsl:apply-templates select="@*|node()"/>
		</Addressee>
	</xsl:template>
	
	<xsl:template match="Writer">
		<Writer>
			<xsl:attribute name="type">
			<xsl:choose>
				<xsl:when test="(Organization)">ю</xsl:when>
				<xsl:when test="(PrivatePerson)">ф</xsl:when>
			</xsl:choose>
		</xsl:attribute>
			<xsl:apply-templates select="@*|node()"/>
		</Writer>
	</xsl:template>

	<xsl:template match="Document/RegNumber">
		<RegNumer>
			<xsl:apply-templates select="@*|node()"/>
		</RegNumer>
	</xsl:template>
	
	<xsl:template match="Document/RegHistory">
		<Reghistory>
			<xsl:apply-templates select="@*|node()"/>
		</Reghistory>
	</xsl:template>
	
	<xsl:template match="OrganizationOnly|OrganizationWithSign">
		<Organization>
			<xsl:apply-templates select="@*|node()"/>
		</Organization>
	</xsl:template>
	
	<xsl:template match="Econtact/@type">
		<xsl:attribute name="type">
			<xsl:choose>
				<xsl:when test=".='1'">р</xsl:when>
				<xsl:when test=".='2'">д</xsl:when>
				<xsl:when test=".='3'">м</xsl:when>
				<xsl:when test=".='4'">ф</xsl:when>
				<xsl:when test=".='5'">п</xsl:when>
				<xsl:when test=".='6'">и</xsl:when>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="Referred">	
		<Referred>
			<xsl:if test="not(@retype)">
				<xsl:attribute name="retype"></xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="@*|node()"/>
		</Referred>
	</xsl:template>
	
	<xsl:template match="Executor">	
		<Executor>
			<xsl:if test="not(@deadline)">
				<xsl:attribute name="deadline"></xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="@*|node()"/>
		</Executor>
	</xsl:template>
	
	<xsl:template match="Task">
		<Task>
			<xsl:apply-templates select="@*|node()"/>
			<xsl:if test="not(Executor)">
				<xsl:element name="Executor">
					<xsl:attribute name="deadline"></xsl:attribute>
					<xsl:element name="Organization"></xsl:element>
				</xsl:element>				
			</xsl:if>
		</Task>
	</xsl:template>
	
	<xsl:template match="Referred/@retype">
		<xsl:attribute name="retype">
			<xsl:choose>
				<xsl:when test=".='1'">д</xsl:when>
				<xsl:when test=".='2'">з</xsl:when>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>	
	
	<xsl:template match="Validator/DocNumber/OrganizationOnly">
		<Organization>
			<xsl:if test="not(Econtact)">
				<xsl:apply-templates select="@*|node()"/>
				<xsl:element name="Econtact"></xsl:element>				
			</xsl:if>
		</Organization>
	</xsl:template>
	
	<xsl:template match="AddDocuments/Folder">	
		<Folder>
			<xsl:apply-templates select="@*"/>
			<xsl:value-of select="@contents"/>
			<xsl:apply-templates select="DocTransfer"/>
			<xsl:apply-templates select="Note"/>
			<xsl:apply-templates select="Referred"/>
		</Folder>
	</xsl:template>

	<xsl:template match="Folder/Referred">	
		<Document>
			<xsl:apply-templates select="@*|RegNumber"/>
		</Document>
	</xsl:template>
	
	<xsl:template match="Folder/Referred/@retype">	
	</xsl:template>
	
	<xsl:template match="DocTransfer/@transfertype">	
	</xsl:template>
	
	<xsl:template match="Folder/@contents">	
	</xsl:template>
	
	<xsl:template match="@organization_string">	
	</xsl:template>
	
	<xsl:template match="@kpp">	
	</xsl:template>
	
</xsl:stylesheet>