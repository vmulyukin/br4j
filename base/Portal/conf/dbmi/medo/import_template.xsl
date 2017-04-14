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
	  xmlns="http://aplana.com/dbmi/exchange/model/Card"
	  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
	  xmlns:xdms="http://www.infpres.com/IEDMS" 
	  exclude-result-prefixes="xdms">
	  
	<xsl:import href="dictionary.xsl"/>
	<xsl:output method="xml" indent="yes"/>

	<xsl:template match="/">
		<xsl:variable name="type" select="//xdms:header/@xdms:type"/>
		<xsl:choose>
			<xsl:when test="$type='Документ'">
				<card templateId="224">
					<attribute code="ADMIN_118450" type="list">
						<value><xsl:call-template name="deliveryType" /></value>
					</attribute>
					<xsl:apply-templates select="//xdms:header" mode="document"/>
					<xsl:apply-templates select="//xdms:document"/>
					<xsl:apply-templates select="//xdms:files"/>
					<xsl:apply-templates select="//xdms:posting"/>
				</card>
			</xsl:when>
			<xsl:when test="$type='Уведомление'">
				<card>
					<xsl:variable name="notificationType" select="//xdms:notification/@xdms:type"/>
					<xsl:choose>
						<xsl:when test="$notificationType='Зарегистрирован'">
							<xsl:attribute name="templateId">1067</xsl:attribute>
							<xsl:attribute name="processor">notificationForOutcome</xsl:attribute>
						</xsl:when>
						<xsl:when test="$notificationType='Отказано в регистрации'">
							<xsl:attribute name="templateId">1070</xsl:attribute>
							<xsl:attribute name="processor">notificationForOutcome</xsl:attribute>
						</xsl:when>
						<xsl:when test="$notificationType='Назначен исполнитель'">
							<xsl:attribute name="templateId">1068</xsl:attribute>
							<xsl:attribute name="processor">notificationForOutcome</xsl:attribute>
						</xsl:when>
						<xsl:when test="$notificationType='Доклад направлен'">
							<xsl:attribute name="templateId">1069</xsl:attribute>
							<xsl:attribute name="processor">notificationForOutcome</xsl:attribute>
						</xsl:when>
						<xsl:when test="$notificationType='&#x0418;сполнение'">
							<xsl:attribute name="templateId">1072</xsl:attribute>
							<xsl:attribute name="processor">notificationForIncome</xsl:attribute>
						</xsl:when>
					</xsl:choose>
					<xsl:apply-templates select="//xdms:header" mode="notification"/>
					<xsl:apply-templates select="//xdms:notification"/>
					<xsl:apply-templates select="//xdms:posting"/>
				</card>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<!-- Header for DOCUMENT -->
	<xsl:template match="xdms:header" mode="document">
		<xsl:apply-templates select="@xdms:uid" mode="document"/>
		<xsl:apply-templates select="@xdms:created" mode="document"/>
		<source>
			<xsl:apply-templates select="xdms:source" mode="document"/>
		</source>
		<xsl:apply-templates select="xdms:operator" mode="document"/>
		<xsl:apply-templates select="xdms:comment" mode="document"/>
	</xsl:template>

	<xsl:template match="xdms:header/@xdms:uid" mode="document">
		<attribute code="JBR_MEDO_UID_MES" type="string" name="Header\@UID">
			<value>
				<xsl:value-of select="." />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:header/@xdms:created" mode="document"> 
		<attribute code="JBR_MEDO_CREATE_DATE" type="date" name="Header\@created">
			<value>
				<xsl:value-of select="." />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Source -->
	<xsl:template match="xdms:header/xdms:source" mode="document"> <!-- Communication partner -->
		<xsl:apply-templates select="@xdms:uid" mode="document"/> 
		<xsl:apply-templates select="xdms:organization" mode="document"/> 
		<xsl:apply-templates select="xdms:comment" mode="document"/>
	</xsl:template>

	<xsl:template match="xdms:header/xdms:source/@xdms:uid" mode="document">
		<attribute code="UNDEFINED" type="string" name="Header\Source\@UID">
			<auxiliary>ORGANIZATION_UID</auxiliary>
			<value>
				<xsl:value-of select="." />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:header/xdms:source/xdms:organization" mode="document">
		<attribute code="UNDEFINED" type="string" name="Header\Source\Organization">
			<auxiliary>ORGANIZATION</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:header/xdms:source/xdms:comment" mode="document">
		<attribute code="UNDEFINED" type="string" name="Header\Source\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Operator -->
	<xsl:template match="xdms:header/xdms:operator"  mode="document">
		<attribute code="UNDEFINED" type="string" name="Header\Operator">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Comment -->
	<xsl:template match="xdms:header/xdms:comment"  mode="document">
		<attribute code="UNDEFINED" type="string" name="Header\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Header for NOTIFICATION -->
	
	<xsl:template match="xdms:header" mode="notification">
		<xsl:apply-templates select="@xdms:uid" mode="notification"/>
		<xsl:apply-templates select="@xdms:created" mode="notification"/>
		<sender>
			<xsl:apply-templates select="xdms:source" mode="notification"/>
		</sender>
		<xsl:apply-templates select="xdms:operator" mode="notification"/>
		<xsl:apply-templates select="xdms:comment" mode="notification"/>
	</xsl:template>

	<xsl:template match="xdms:header/@xdms:uid" mode="notification">
		<attribute code="MED_IMP_DOC_UID_NOT" type="string" name="Header\@UID">
			<value>
				<xsl:value-of select="." />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:header/@xdms:created" mode="notification">
		<attribute code="MED_CREATED" type="date" name="Header\@created">
			<value>
				<xsl:value-of select="." />
			</value>
		</attribute>
	</xsl:template>	

	<!-- Header Source for NOTIFICATION -->
	<xsl:template match="xdms:header/xdms:source" mode="notification"> <!-- Communication partner -->
		<xsl:apply-templates select="@xdms:uid" mode="notification"/> 
		<xsl:apply-templates select="xdms:organization" mode="notification"/> 
		<xsl:apply-templates select="xdms:comment" mode="notification"/>
	</xsl:template>
 
	<xsl:template match="xdms:header/xdms:source/@xdms:uid" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Header\Source\@UID">
			<auxiliary>ORGANIZATION_UID</auxiliary>
			<value>
				<xsl:value-of select="." />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:header/xdms:source/xdms:organization" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Header\Source\Organization">
			<auxiliary>ORGANIZATION</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:header/xdms:source/xdms:comment" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Header\Source\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Operator -->
	<xsl:template match="xdms:header/xdms:operator" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Header\Operator">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Comment -->
	<xsl:template match="xdms:header/xdms:comment" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Header\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

<!-- ===========================Document==================================== -->

	<xsl:template match="xdms:document">
		<xsl:apply-templates select="@xdms:uid"/>
		<xsl:apply-templates select="xdms:kind"/>
		<xsl:apply-templates select="xdms:num"/>
		<xsl:apply-templates select="xdms:classification"/>
		<xsl:apply-templates select="xdms:urgency"/>
		<xsl:apply-templates select="xdms:insteadOfDistributed"/>
		<xsl:apply-templates select="xdms:signatories"/>
		<xsl:apply-templates select="xdms:addressees"/>
		<xsl:apply-templates select="xdms:pages"/>
		<xsl:apply-templates select="xdms:enclosuresPages"/>
		<xsl:apply-templates select="xdms:annotation"/>
		<xsl:apply-templates select="xdms:enclosures"/>
		<xsl:apply-templates select="xdms:correspondents"/>
		<xsl:apply-templates select="xdms:links"/>
		<xsl:apply-templates select="xdms:clauses"/>
		<xsl:apply-templates select="xdms:executor"/>
		<xsl:apply-templates select="xdms:comment"/>
	</xsl:template>

	<xsl:template match="xdms:document/@xdms:uid">
		<attribute code="JBR_IMP_DOC_UID" type="string" name="Document\@UID">
			<value><xsl:value-of select="." /></value>
		</attribute>
	</xsl:template>

	<!-- Kind -->

	<xsl:template match="xdms:document/xdms:kind">
		<docType>
			<attribute code="JBR_MEDO_DOC_TYPE" type="string" name="Document\Kind">
				<auxiliary>DOCUMENT_TYPE</auxiliary>
				<value>
					<xsl:apply-templates />
				</value>
			</attribute>
		</docType>
		<attribute code="JBR_MEDO_DOC_TYPE" type="string" name="Document\Kind">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>  
	</xsl:template>

	<!-- Num -->
	
	<xsl:template match="xdms:document/xdms:num">
		<xsl:apply-templates select="xdms:number"/>
		<xsl:apply-templates select="xdms:date"/>
	</xsl:template>

	<xsl:template match="xdms:document/xdms:num/xdms:number">
		<attribute code="JBR_REGD_NUMOUT" type="string" name="Document\Num\Number">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:document/xdms:num/xdms:date">
		<attribute code="JBR_REGD_DATEOUT" type="date" name="Document\Num\Date">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Classification -->

	<xsl:template match="xdms:document/xdms:classification">
		<attribute code="JBR_MEDO_CLASSIF" type="string" name="Document\Classification">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Urgency -->
	
	<xsl:template match="xdms:document/xdms:urgency">
		<attribute code="JBR_MEDO_URGENCY" type="string" name="Document\Urgency">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- InsteadOfDistributed -->
	
	<xsl:template match="xdms:document/xdms:insteadOfDistributed">
		<attribute code="JBR_MEDO_DISTR_INST" type="list" name="Document\InsteadOfDistributed">
			<value>
				<xsl:apply-imports />
			</value>
		</attribute>
	</xsl:template>	
	
	<!-- Signatories -->

	<xsl:template match="xdms:document/xdms:signatories">
		<xsl:apply-templates select="xdms:signatory" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:signatories/xdms:signatory">
		<signatory>
			<xsl:apply-templates select="xdms:region" />
			<xsl:apply-templates select="xdms:organization" />
			<xsl:apply-templates select="xdms:person" />
			<xsl:apply-templates select="xdms:department" />
			<xsl:apply-templates select="xdms:post" />
			<xsl:apply-templates select="xdms:contactInfo" />
			<xsl:apply-templates select="xdms:comment" />
		</signatory>
		<xsl:apply-templates select="xdms:signed" />
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:signatories/xdms:signatory/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Document\Signatories\Signatory\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:signatories/xdms:signatory/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Document\Signatories\Signatory\Organization">
			<auxiliary>ORGANIZATION</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:signatories/xdms:signatory/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Document\Signatories\Signatory\Person">
			<auxiliary>PERSON</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:signatories/xdms:signatory/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Document\Signatories\Signatory\Department">
			<auxiliary>DEPARTMENT</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:signatories/xdms:signatory/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Document\Signatories\Signatory\Post">
			<auxiliary>POSITION</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:signatories/xdms:signatory/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Document\Signatories\Signatory\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:signatories/xdms:signatory/xdms:signed">
		<attribute code="JBR_MEDO_SIGN_DATE" type="date" name="Document\Signatories\Signatory\Signed">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:signatories/xdms:signatory/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Signatories\Signatory\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Addressees -->
	
	<xsl:template match="xdms:document/xdms:addressees">
		<attribute code="JBR_MEDO_ADDR" type="html">
			<value>	
				<xsl:apply-templates select="xdms:addressee" />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:addressees/xdms:addressee">
		<xsl:apply-templates select="xdms:region" /><xsl:text>, </xsl:text>
		<xsl:apply-templates select="xdms:organization" /><xsl:text>, </xsl:text>
		<xsl:apply-templates select="xdms:person" /><xsl:text>, </xsl:text>
		<xsl:apply-templates select="xdms:department" /><xsl:text>, </xsl:text>
		<xsl:apply-templates select="xdms:post" /><xsl:text>, </xsl:text>
		<xsl:apply-templates select="xdms:contactInfo" /><xsl:text>, </xsl:text>
		<xsl:apply-templates select="xdms:comment" /><xsl:text>&#xA;&#xA;</xsl:text>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:addressees/xdms:addressee/xdms:region">
				<xsl:apply-templates />
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:addressees/xdms:addressee/xdms:organization">
				<xsl:apply-templates />
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:addressees/xdms:addressee/xdms:person">
				<xsl:apply-templates />
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:addressees/xdms:addressee/xdms:department">
				<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:addressees/xdms:addressee/xdms:post">
				<xsl:apply-templates />
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:addressees/xdms:addressee/xdms:contactInfo">
				<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:addressees/xdms:addressee/xdms:comment">
				<xsl:apply-templates />
	</xsl:template>
	
	<!-- Pages -->

	<xsl:template match="xdms:document/xdms:pages">
		<attribute code="JBR_ORIG_QUANTPAP" type="integer" name="Document\Pages">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- EnclosuresPages -->
	
	<xsl:template match="xdms:document/xdms:enclosuresPages">
		<attribute code="JBR_ORIG_QUANTPAPAT" type="integer" name="Document\EnclosuresPages">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Annotation -->
	
	<xsl:template match="xdms:document/xdms:annotation">
		<attribute code="JBR_INFD_SHORTDESC" type="text" name="Document\Annotation">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Enclosures -->
	
	<xsl:template match="xdms:document/xdms:enclosures">
		<xsl:apply-templates select="xdms:enclosure" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure">
		<xsl:apply-templates select="xdms:title" />
		<xsl:apply-templates select="xdms:reference" />
		<xsl:apply-templates select="xdms:pages" />
		<xsl:apply-templates select="xdms:files" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:title">
		<attribute code="UNDEFINED" type="string" name="Document\Enclosures\Enclosure\Title">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:num" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Document\Enclosures\Enclosure\Reference\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Document\Enclosures\Enclosure\Reference\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Document\Enclosures\Enclosure\Reference\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Document\Enclosures\Enclosure\Reference\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Document\Enclosures\Enclosure\Reference\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:num">
		<xsl:apply-templates select="xdms:number" />
		<xsl:apply-templates select="xdms:date" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:num/xdms:number">
		<attribute code="UNDEFINED" type="string" name="Document\Enclosures\Enclosure\Reference\Num\Number">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:num/xdms:date">
		<attribute code="UNDEFINED" type="string" name="Document\Enclosures\Enclosure\Reference\Num\Date">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Enclosures\Enclosure\Reference\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:pages">
		<attribute code="UNDEFINED" type="string" name="Document\Enclosures\Enclosure\Pages">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:files">
		<attribute code="UNDEFINED" type="string" name="Document\Enclosures\Enclosure\Files">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Enclosures\Enclosure\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Correspondents -->
	
	<xsl:template match="xdms:document/xdms:correspondents">
		<attribute code="JBR_MEDO_CORRESP" type="html">
			<value>
				<xsl:apply-templates select="xdms:correspondent" />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:correspondents/xdms:correspondent">
			<xsl:apply-templates select="xdms:region" /><xsl:text>, </xsl:text>
			<xsl:apply-templates select="xdms:organization" /><xsl:text>, </xsl:text>
			<xsl:apply-templates select="xdms:person" /><xsl:text>,</xsl:text>
			<xsl:apply-templates select="xdms:department" /><xsl:text>, </xsl:text>
			<xsl:apply-templates select="xdms:post" /><xsl:text>, </xsl:text>
			<xsl:apply-templates select="xdms:contactInfo" /><xsl:text>, </xsl:text>
			<xsl:apply-templates select="xdms:num" /><xsl:text>, </xsl:text>
			<xsl:apply-templates select="xdms:comment" /><xsl:text>&#xA;&#xA;</xsl:text>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:correspondents/xdms:correspondent/xdms:region">
				<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:correspondents/xdms:correspondent/xdms:organization">
				<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:correspondents/xdms:correspondent/xdms:person">
				<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:correspondents/xdms:correspondent/xdms:department">
				<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:correspondents/xdms:correspondent/xdms:post">
				<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:correspondents/xdms:correspondent/xdms:contactInfo">
				<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:correspondents/xdms:correspondent/xdms:num">
		<xsl:apply-templates select="xdms:number" />
		<xsl:apply-templates select="xdms:date" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:correspondents/xdms:correspondent/xdms:num/xdms:number">
				<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:correspondents/xdms:correspondent/xdms:num/xdms:date">
				<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:correspondents/xdms:correspondent/xdms:comment">
				<xsl:apply-templates />
	</xsl:template>
	
	<!-- Links -->

	<xsl:template match="xdms:document/xdms:links">
		<xsl:apply-templates select="xdms:link" />
	</xsl:template>

	<xsl:template match="xdms:document/xdms:links/xdms:link">
		<xsl:apply-templates select="xdms:linkType" />
		<xsl:apply-templates select="xdms:document" />
		<xsl:apply-templates select="xdms:reference" />
	</xsl:template>

	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:linkType">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\LinkType">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document">
		<xsl:apply-templates select="xdms:kind"/>
		<xsl:apply-templates select="xdms:num"/>
		<xsl:apply-templates select="xdms:classification"/>
		<xsl:apply-templates select="xdms:signatories"/>
		<xsl:apply-templates select="xdms:addressees"/>
		<xsl:apply-templates select="xdms:pages"/>
		<xsl:apply-templates select="xdms:enclosuresPages"/>
		<xsl:apply-templates select="xdms:annotation"/>
		<xsl:apply-templates select="xdms:enclosures"/>
		<xsl:apply-templates select="xdms:correspondents"/>
		<xsl:apply-templates select="xdms:links"/>
		<xsl:apply-templates select="xdms:clauses"/>
		<xsl:apply-templates select="xdms:comment"/>
	</xsl:template>

	<!-- Links\Kind -->

	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:kind">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Kind">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>  
	</xsl:template>

	<!-- Links\Num -->

	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:num">
		<xsl:apply-templates select="xdms:number"/>
		<xsl:apply-templates select="xdms:date"/>
	</xsl:template>

	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:num/xdms:number">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Num\Number">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:num/xdms:date">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Num\Date">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Links\Classification -->

	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:classification">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Classification">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Links\Signatories -->

	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories">
		<xsl:apply-templates select="signatory" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories/xdms:signatory">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:contactInfo" />
		<xsl:apply-templates select="xdms:signed" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories/xdms:signatory/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Signatories\Signatory\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories/xdms:signatory/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Signatories\Signatory\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories/xdms:signatory/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Signatories\Signatory\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories/xdms:signatory/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Signatories\Signatory\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories/xdms:signatory/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Signatories\Signatory\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories/xdms:signatory/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Signatories\Signatory\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories/xdms:signatory/xdms:signed">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Signatories\Signatory\Signed">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories/xdms:signatory/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Signatories\Signatory\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Links\Addressees -->
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:addressees">
		<xsl:apply-templates select="addressee" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:addressees/xdms:addressee">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:contactInfo" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:addressees/xdms:addressee/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Addressees\Addressee\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:addressees/xdms:addressee/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Addressees\Addressee\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:addressees/xdms:addressee/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Addressees\Addressee\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:addressees/xdms:addressee/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Addressees\Addressee\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:addressees/xdms:addressee/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Addressees\Addressee\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:addressees/xdms:addressee/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Addressees\Addressee\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:addressees/xdms:addressee/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Addressees\Addressee\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Links\Pages -->

	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:pages">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Pages">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Links\EnclosuresPages -->
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosuresPages">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\EnclosuresPages">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Links\Annotation -->
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:annotation">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Annotation">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Links\Enclosures -->
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures">
		<xsl:apply-templates select="xdms:enclosure" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure">
		<xsl:apply-templates select="xdms:title" />
		<xsl:apply-templates select="xdms:reference" />
		<xsl:apply-templates select="xdms:pages" />
		<xsl:apply-templates select="xdms:files" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:title">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Enclosures\Enclosure\Title">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:num" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Enclosures\Enclosure\Reference\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Enclosures\Enclosure\Reference\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Enclosures\Enclosure\Reference\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Enclosures\Enclosure\Reference\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Enclosures\Enclosure\Reference\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:num">
		<xsl:apply-templates select="xdms:number" />
		<xsl:apply-templates select="xdms:date" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:num/xdms:number">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Enclosures\Enclosure\Reference\Num\Number">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:num/xdms:date">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Enclosures\Enclosure\Reference\Num\Date">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Enclosures\Enclosure\Reference\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:pages">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Enclosures\Enclosure\Pages">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:files">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Enclosures\Enclosure\Files">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Enclosures\Enclosure\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Links\Correspondents -->
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents">
		<xsl:apply-templates select="xdms:correspondent" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:contactInfo" />
		<xsl:apply-templates select="xdms:num" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Correspondents\Correspondent\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Correspondents\Correspondent\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Correspondents\Correspondent\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Correspondents\Correspondent\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Correspondents\Correspondent\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Correspondents\Correspondent\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:num">
		<xsl:apply-templates select="xdms:number" />
		<xsl:apply-templates select="xdms:date" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:num/xdms:number">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Correspondents\Correspondent\Num\Number">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:num/xdms:date">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Correspondents\Correspondent\Num\Date">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Correspondents\Correspondent\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Links\Links -->

	<!-- Links\Clauses -->
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses">
		<xsl:apply-templates select="xdms:clause" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause">
		<xsl:apply-templates select="xdms:designation" />
		<xsl:apply-templates select="xdms:text" />
		<xsl:apply-templates select="xdms:deadline" />
		<xsl:apply-templates select="xdms:principal" />
		<xsl:apply-templates select="xdms:principals" />
		<xsl:apply-templates select="xdms:parcipants" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:designation">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Designation">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:text">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Text">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:deadline">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Deadline">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principal">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:contactInfo" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Principal\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Principal\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Principal\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Principal\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Principal\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Principal\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Principal\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principals">
		<xsl:apply-templates select="xdms:name" />
		<xsl:apply-templates select="xdms:contents" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:name">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Principals\Name">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:contactInfo" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Principals\Contents\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Principals\Contents\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Principals\Contents\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Principals\Contents\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Principals\Contents\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Principals\Contents\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Principals\Contents\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants">
		<xsl:apply-templates select="xdms:parcipant" />
		<xsl:apply-templates select="xdms:parcipants" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:contactInfo" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipant\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipant\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipant\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipant\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipant\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipant\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipant\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants">
		<xsl:apply-templates select="xdms:name" />
		<xsl:apply-templates select="xdms:contents" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:name">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipants\Name">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:contactInfo" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipants\Contents\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipants\Contents\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipants\Contents\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipants\Contents\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipants\Contents\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipants\Contents\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipants\Contents\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Clauses\Clause\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Links\Comment -->
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Links\Link\Document\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Clauses -->
	<xsl:template match="xdms:document/xdms:clauses">
		<xsl:apply-templates select="xdms:clause" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause">
		<xsl:apply-templates select="xdms:designation" />
		<xsl:apply-templates select="xdms:text" />
		<xsl:apply-templates select="xdms:deadline" />
		<xsl:apply-templates select="xdms:principal" />
		<xsl:apply-templates select="xdms:principals" />
		<xsl:apply-templates select="xdms:parcipants" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:designation">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Designation">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:text">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Text">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:deadline">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Deadline">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principal">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:contactInfo" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Principal\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Principal\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Principal\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Principal\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Principal\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Principal\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Principal\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principals">
		<xsl:apply-templates select="xdms:name" />
		<xsl:apply-templates select="xdms:contents" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:name">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Principals\Name">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:contactInfo" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Principals\Contents\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Principals\Contents\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Principals\Contents\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Principals\Contents\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Principals\Contents\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Principals\Contents\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Principals\Contents\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants">
		<xsl:apply-templates select="xdms:parcipant" />
		<xsl:apply-templates select="xdms:parcipants" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:contactInfo" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Parcipants\Parcipant\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Parcipants\Parcipant\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Parcipants\Parcipant\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Parcipants\Parcipant\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Parcipants\Parcipant\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Parcipants\Parcipant\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Parcipants\Parcipant\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants">
		<xsl:apply-templates select="xdms:name" />
		<xsl:apply-templates select="xdms:contents" />
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:name">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Parcipants\Parcipants\Name">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:contactInfo" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Parcipants\Parcipants\Contents\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Parcipants\Parcipants\Contents\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Parcipants\Parcipants\Contents\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Parcipants\Parcipants\Contents\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Parcipants\Parcipants\Contents\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Parcipants\Parcipants\Contents\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Parcipants\Parcipants\Contents\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Clauses\Clause\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Executor -->
	
	<xsl:template match="xdms:document/xdms:executor">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:contactInfo" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:executor/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Document\Executor\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:executor/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Document\Executor\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:executor/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Document\Executor\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:executor/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Document\Executor\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:executor/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Document\Executor\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:executor/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Document\Executor\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:executor/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Document\Executor\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Comment -->
	
	<xsl:template match="xdms:document/xdms:comment">
		<service>
			<attribute code="JBR_IMPL_INRES" type="text" name="Document\Comment">
				<auxiliary>SERVICE_CODE</auxiliary>
				<value>
					<xsl:apply-templates />
				</value>
			</attribute>
		</service>
	</xsl:template>


<!-- ===========================Files==================================== -->

	<xsl:template match="xdms:files">
		<xsl:apply-templates select="xdms:file"/>
	</xsl:template>

	<xsl:template match="xdms:files/xdms:file">
		<file>
			<xsl:apply-templates select="xdms:group" />
			<xsl:apply-templates select="xdms:description" />
			<xsl:apply-templates select="xdms:pages" />
			<xsl:apply-templates select="xdms:eds" />
			<xsl:apply-templates select="@xdms:localName" />
			<xsl:apply-templates select="@xdms:type" />
		</file>
	</xsl:template>

	<xsl:template match="xdms:files/xdms:file/xdms:group">
		<attribute code="UNDEFINED" type="string" name="Files\File\Group">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:files/xdms:file/xdms:description">
		<attribute code="UNDEFINED" type="string" name="Files\File\Description">
			<auxiliary>FILE_DESCRIPTION</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:files/xdms:file/xdms:pages">
		<attribute code="UNDEFINED" type="string" name="Files\File\Pages">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:files/xdms:file/xdms:eds">
		<attribute code="ADMIN_67129" type="html" name="Files\File\EDS">
			<auxiliary>EDS</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:files/xdms:file/@xdms:localName">
		<attribute code="UNDEFINED" type="string" name="Files\File\AttrLocalName">
			<auxiliary>FILE_NAME</auxiliary>
			<value>
				<xsl:value-of select="."/>
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:files/xdms:file/@xdms:type">
		<attribute> <!-- code="..." type="..." name="..."> --> 
			<auxiliary>IS_REFERENCE</auxiliary>
			<value>
				<xsl:choose>
  					<xsl:when test=".='reference'">true</xsl:when>
  					<xsl:otherwise>false</xsl:otherwise>
				</xsl:choose>
			</value>
		</attribute>
	</xsl:template>

<!-- ===========================Notification==================================== -->

	<xsl:template match="xdms:notification">
		<distributionItem>
			<xsl:apply-templates select="@xdms:uid"/>
		</distributionItem>
		<xsl:apply-templates select="@xdms:uid"/>
		<xsl:apply-templates select="@xdms:id"/>
		<xsl:apply-templates select="xdms:documentAccepted" />
		<xsl:apply-templates select="xdms:documentRefused" />
		<xsl:apply-templates select="xdms:executorAssigned" />
		<xsl:apply-templates select="xdms:reportPrepared" />
		<xsl:apply-templates select="xdms:reportSent" />
		<xsl:apply-templates select="xdms:courseChanged" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>
	
	<xsl:template match="xdms:notification/@xdms:uid">
		<attribute code="MED_IMP_DOC_UID" type="string" name="Notification\@UID">
			<auxiliary>UUID</auxiliary>
			<value>
				<xsl:value-of select="." />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:notification/@xdms:id">
		<attribute code="MED_IMP_DOC_ID" type="string" name="Notification\@ID">
			<value>
				<xsl:value-of select="." />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Document accepted -->
	<xsl:template match="xdms:notification/xdms:documentAccepted">
		<xsl:apply-templates select="xdms:time" mode="notification" />
		<xsl:apply-templates select="xdms:foundation" mode="notification" />
		<xsl:apply-templates select="xdms:clause" mode="notification" />
		<xsl:apply-templates select="xdms:num"/>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:documentAccepted/xdms:num">
		<xsl:apply-templates select="xdms:number" />
		<xsl:apply-templates select="xdms:date" />
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:documentAccepted/xdms:num/xdms:number">
		<attribute code="MED_REGD_REGNUM" type="string" name="Notification\DocumentAccepted\Num\Number">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:documentAccepted/xdms:num/xdms:date">
		<attribute code="MED_REGD_DATEREG" type="date" name="Notification\DocumentAccepted\Num\Date">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Document refused -->
	<xsl:template match="xdms:notification/xdms:documentRefused">
		<xsl:apply-templates select="xdms:time" mode="notification" />
		<xsl:apply-templates select="xdms:foundation" mode="notification" />
		<xsl:apply-templates select="xdms:clause" mode="notification" />
		<xsl:apply-templates select="xdms:reason" />
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:documentRefused/xdms:reason">
		<attribute code="MED_REAS_REFUS" type="text" name="Notification\DocumentRefused\Reason">
			<value>
				<xsl:apply-imports />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Executor assigned -->
	<xsl:template match="xdms:notification/xdms:executorAssigned">
		<xsl:apply-templates select="xdms:time" mode="notification" />
		<xsl:apply-templates select="xdms:foundation" mode="notification" />
		<xsl:apply-templates select="xdms:clause" mode="notification" />
		<xsl:apply-templates select="xdms:secretary" />
		<xsl:apply-templates select="xdms:manager" />
		<xsl:apply-templates select="xdms:executor" />
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:secretary">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:contactInfo" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:secretary/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Secretary\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:secretary/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Secretary\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:secretary/xdms:person">
		<attribute code="MED_NTF_DIR_FIO" type="string" name="Notification\ExecutorAssigned\Secretary\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:secretary/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Secretary\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:secretary/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Secretary\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:secretary/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Secretary\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:secretary/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Secretary\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:manager">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:contactInfo" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:manager/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Manager\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:manager/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Manager\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:manager/xdms:person">
		<attribute code="MED_NOTIF_EXECDPTDIR" type="string" name="Notification\ExecutorAssigned\Manager\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:manager/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Manager\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:manager/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Manager\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:manager/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Manager\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:manager/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Manager\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:executor">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:contactInfo" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:executor/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Executor\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:executor/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Executor\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:executor/xdms:person">
		<attribute code="MED_NOTIF_RESP_EXEC" type="string" name="Notification\ExecutorAssigned\Executor\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:executor/xdms:department">
		<attribute code="MED_NTF_EXECDPT_NAME" type="string" name="Notification\ExecutorAssigned\Executor\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:executor/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Executor\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:executor/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Executor\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:executor/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Notification\ExecutorAssigned\Executor\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Report prepared -->
	<xsl:template match="xdms:notification/xdms:reportPrepared">
		<xsl:apply-templates select="xdms:time" mode="notification" />
		<xsl:apply-templates select="xdms:foundation" mode="notification" />
		<xsl:apply-templates select="xdms:clause" mode="notification" />
		<xsl:apply-templates select="xdms:signatory"  />
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportPrepared/xdms:signatory">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:contactInfo" />
		<xsl:apply-templates select="xdms:signed" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:reportPrepared/xdms:signatory/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Notification\ReportPrepared\Signatory\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:reportPrepared/xdms:signatory/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Notification\ReportPrepared\Signatory\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:reportPrepared/xdms:signatory/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Notification\ReportPrepared\Signatory\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:reportPrepared/xdms:signatory/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Notification\ReportPrepared\Signatory\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportPrepared/xdms:signatory/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Notification\ReportPrepared\Signatory\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:reportPrepared/xdms:signatory/xdms:contactInfo">
		<attribute code="UNDEFINED" type="string" name="Notification\ReportPrepared\Signatory\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:reportPrepared/xdms:signatory/xdms:signed">
		<attribute code="UNDEFINED" type="string" name="Notification\ReportPrepared\Signatory\Signed">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportPrepared/xdms:signatory/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Notification\ReportPrepared\Signatory\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Report sent -->
	<xsl:template match="xdms:notification/xdms:reportSent">
		<xsl:apply-templates select="xdms:time" mode="notification" />
		<xsl:apply-templates select="xdms:foundation" mode="notification" />
		<xsl:apply-templates select="xdms:clause" mode="notification" />
		<xsl:apply-templates select="xdms:report" />		
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:reportSent/xdms:report">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:num" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportSent/xdms:report/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Notification\ReportSent\Report\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportSent/xdms:report/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Notification\ReportSent\Report\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportSent/xdms:report/xdms:person">
		<attribute code="MED_NOTIF_SIGNER" type="string" name="Notification\ReportSent\Report\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportSent/xdms:report/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Notification\ReportSent\Report\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportSent/xdms:report/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Notification\ReportSent\Report\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportSent/xdms:report/xdms:num">
		<xsl:apply-templates select="xdms:number" />
		<xsl:apply-templates select="xdms:date" />
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportSent/xdms:report/xdms:num/xdms:number">
		<attribute code="MED_OUTNUM_FOIV" type="text" name="Notification\ReportSent\Report\Num\Number">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportSent/xdms:report/xdms:num/xdms:date">
		<attribute code="MED_DATE_OUTNUM_FOIV" type="date" name="Notification\ReportSent\Report\Num\Date">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportSent/xdms:report/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Notification\ReportSent\Report\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Course changed -->
	<xsl:template match="xdms:notification/xdms:courseChanged">
		<xsl:apply-templates select="xdms:time" mode="notification" />
		<xsl:apply-templates select="xdms:foundation" mode="notification" />
		<xsl:apply-templates select="xdms:clause" mode="notification" />
		<xsl:apply-templates select="xdms:courseText" />
		<xsl:apply-templates select="xdms:reference" />
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:courseText">
		<attribute code="JBR_MEDO_COURSE_TEXT" type="text" name="Notification\CourseChanged\CourseText">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
		<attribute code="JBR_SIR_CRD_PROCESS" type="list" name="Notification\CourseChanged\CourseText">
			<value>
				<xsl:apply-imports />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:reference">
		<xsl:apply-templates select="xdms:region" />
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:person" />
		<xsl:apply-templates select="xdms:department" />
		<xsl:apply-templates select="xdms:post" />
		<xsl:apply-templates select="xdms:num" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:reference/xdms:region">
		<attribute code="UNDEFINED" type="string" name="Notification\CourseChanged\Reference\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:reference/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="Notification\CourseChanged\Reference\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:reference/xdms:person">
		<attribute code="UNDEFINED" type="string" name="Notification\CourseChanged\Reference\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:reference/xdms:department">
		<attribute code="UNDEFINED" type="string" name="Notification\CourseChanged\Reference\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:reference/xdms:post">
		<attribute code="UNDEFINED" type="string" name="Notification\CourseChanged\Reference\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:reference/xdms:num">
		<xsl:apply-templates select="xdms:number" mode="notification" />
		<xsl:apply-templates select="xdms:date" mode="notification" />
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:reference/xdms:num/xdms:number">
		<attribute code="UNDEFINED" type="string" name="Notification\CourseChanged\Reference\Num\Number">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:reference/xdms:num/xdms:date">
		<attribute code="UNDEFINED" type="string" name="Notification\CourseChanged\Reference\Num\Date">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:reference/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Notification\CourseChanged\Reference\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<!--Notification Comment-->

	<xsl:template match="xdms:notification/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="Notification\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Time -->
	<xsl:template match="xdms:time" mode="notification">
		<attribute code="JBR_MEDO_FORM_DATE" type="date" name="Notification\Time">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Foundation -->
	
	<xsl:template match="xdms:foundation" mode="notification">
		<xsl:variable name="var_foundation">
			<xsl:apply-templates select="xdms:region" mode="notification" />
			<xsl:apply-templates select="xdms:organization" mode="notification" />
			<xsl:apply-templates select="xdms:person" mode="notification" />
			<xsl:apply-templates select="xdms:department" mode="notification" />
			<xsl:apply-templates select="xdms:post" mode="notification" />
			<xsl:apply-templates select="xdms:num" mode="notification" />
			<xsl:apply-templates select="xdms:comment" mode="notification" />
		</xsl:variable>
		<foundation>
			<xsl:copy-of select="$var_foundation" />
		</foundation>
		<xsl:copy-of select="$var_foundation" />
	</xsl:template>
	
	<xsl:template match="xdms:foundation/xdms:region" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Foundation\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:foundation/xdms:organization" mode="notification">
		<attribute code="MED_DORG_FULLNAME" type="text" name="Notification\Foundation\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:foundation/xdms:person" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Foundation\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:foundation/xdms:department" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Foundation\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:foundation/xdms:post" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Foundation\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:foundation/xdms:num" mode="notification">
		<xsl:apply-templates select="xdms:number" mode="notification" />
		<xsl:apply-templates select="xdms:date" mode="notification" />
	</xsl:template>
	
	<xsl:template match="xdms:foundation/xdms:num/xdms:number" mode="notification">
		<attribute code="MED_REGD_NUMOUT" type="string" name="Notification\Foundation\Num\Number">
			<auxiliary>REF_REG_NUM</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:foundation/xdms:num/xdms:date" mode="notification">
		<attribute code="MED_REGD_DATEOUT" type="date" name="Notification\Foundation\Num\Date">
			<auxiliary>REF_REG_DATE</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:foundation/xdms:comment" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Foundation\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Notification clause -->
	
	<xsl:template match="xdms:clause" mode="notification">
		<xsl:apply-templates select="xdms:designation" mode="notification" />
		<xsl:apply-templates select="xdms:text" mode="notification" />
		<xsl:apply-templates select="xdms:deadline" mode="notification" />
		<xsl:apply-templates select="xdms:principal" mode="notification" />
		<xsl:apply-templates select="xdms:principals" mode="notification" />
		<xsl:apply-templates select="xdms:parcipants" mode="notification" />
		<xsl:apply-templates select="xdms:comment" mode="notification" />
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:designation" mode="notification">
		<attribute code="MED_RESOLUTION_ITEM" type="text" name="Notification\Clause\Designation">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:text" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Text">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:deadline" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Deadline">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:principal" mode="notification">
		<xsl:apply-templates select="xdms:region" mode="notification" />
		<xsl:apply-templates select="xdms:organization" mode="notification" />
		<xsl:apply-templates select="xdms:person" mode="notification" />
		<xsl:apply-templates select="xdms:department" mode="notification" />
		<xsl:apply-templates select="xdms:post" mode="notification" />
		<xsl:apply-templates select="xdms:contactInfo" mode="notification" />
		<xsl:apply-templates select="xdms:comment" mode="notification" />
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principal/xdms:region" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Principal\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principal/xdms:organization" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Principal\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principal/xdms:person" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Principal\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principal/xdms:department" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Principal\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:principal/xdms:post" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Principal\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principal/xdms:contactInfo" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Principal\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:principal/xdms:comment" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Principal\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principals" mode="notification">
		<xsl:apply-templates select="xdms:name" mode="notification" />
		<xsl:apply-templates select="xdms:contents" mode="notification" />
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:principals/xdms:name" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Principals\Name">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:principals/xdms:contents" mode="notification">
		<xsl:apply-templates select="xdms:region" mode="notification" />
		<xsl:apply-templates select="xdms:organization" mode="notification" />
		<xsl:apply-templates select="xdms:person" mode="notification" />
		<xsl:apply-templates select="xdms:department" mode="notification" />
		<xsl:apply-templates select="xdms:post" mode="notification" />
		<xsl:apply-templates select="xdms:contactInfo" mode="notification" />
		<xsl:apply-templates select="xdms:comment" mode="notification" />
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principals/xdms:contents/xdms:region" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Principals\Contents\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principals/xdms:contents/xdms:organization" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Principals\Contents\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principals/xdms:contents/xdms:person" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Principals\Contents\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principals/xdms:contents/xdms:department" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Principals\Contents\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:principals/xdms:contents/xdms:post" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Principals\Contents\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principals/xdms:contents/xdms:contactInfo" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Principals\Contents\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:principals/xdms:contents/xdms:comment" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Principals\Contents\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants" mode="notification">
		<xsl:apply-templates select="xdms:parcipant" mode="notification" />
		<xsl:apply-templates select="xdms:parcipants" mode="notification" />
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipant" mode="notification">
		<xsl:apply-templates select="xdms:region" mode="notification" />
		<xsl:apply-templates select="xdms:organization" mode="notification" />
		<xsl:apply-templates select="xdms:person" mode="notification" />
		<xsl:apply-templates select="xdms:department" mode="notification" />
		<xsl:apply-templates select="xdms:post" mode="notification" />
		<xsl:apply-templates select="xdms:contactInfo" mode="notification" />
		<xsl:apply-templates select="xdms:comment" mode="notification" />
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipant/xdms:region" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Parcipants\Parcipant\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipant/xdms:organization" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Parcipants\Parcipant\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipant/xdms:person" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Parcipants\Parcipant\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipant/xdms:department" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Parcipants\Parcipant\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipant/xdms:post" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Parcipants\Parcipant\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipant/xdms:contactInfo" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Parcipants\Parcipant\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipant/xdms:comment" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Parcipants\Parcipant\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipants" mode="notification">
		<xsl:apply-templates select="xdms:name" mode="notification" />
		<xsl:apply-templates select="xdms:contents" mode="notification" />
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipants/xdms:name" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Parcipants\Parcipants\Name">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents" mode="notification">
		<xsl:apply-templates select="xdms:region" mode="notification" />
		<xsl:apply-templates select="xdms:organization" mode="notification" />
		<xsl:apply-templates select="xdms:person" mode="notification" />
		<xsl:apply-templates select="xdms:department" mode="notification" />
		<xsl:apply-templates select="xdms:post" mode="notification" />
		<xsl:apply-templates select="xdms:contactInfo" mode="notification" />
		<xsl:apply-templates select="xdms:comment" mode="notification" />
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:region" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Parcipants\Parcipants\Contents\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:organization" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Parcipants\Parcipants\Contents\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:person" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Parcipants\Parcipants\Contents\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:department" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Parcipants\Parcipants\Contents\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:post" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Parcipants\Parcipants\Contents\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:contactInfo" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Parcipants\Parcipants\Contents\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:comment" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Parcipants\Parcipants\Contents\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:comment" mode="notification">
		<attribute code="UNDEFINED" type="string" name="Notification\Clause\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
<!-- ===========================Delivery Index==================================== -->

	<xsl:template match="xdms:deliveryIndex">
		<xsl:apply-templates select="xdms:destination" />
	</xsl:template>
	
	<xsl:template match="xdms:deliveryIndex/xdms:destination">
		<xsl:apply-templates select="xdms:destination" />
		<xsl:apply-templates select="xdms:files" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>
	
	<xsl:template match="xdms:deliveryIndex/xdms:destination/xdms:destination">
		<xsl:apply-templates select="xdms:organization" />
		<xsl:apply-templates select="xdms:comment" />
	</xsl:template>
	
	<xsl:template match="xdms:deliveryIndex/xdms:destination/xdms:destination/xdms:organization">
		<attribute code="UNDEFINED" type="string" name="DeliveryIndex\Destination\Destination\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:deliveryIndex/xdms:destination/xdms:destination/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="DeliveryIndex\Destination\Destination\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:deliveryIndex/xdms:destination/xdms:files">
		<attribute code="UNDEFINED" type="string" name="DeliveryIndex\Destination\Files">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:deliveryIndex/xdms:destination/xdms:comment">
		<attribute code="UNDEFINED" type="string" name="DeliveryIndex\Destination\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
</xsl:stylesheet>


