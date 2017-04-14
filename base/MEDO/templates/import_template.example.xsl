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
					<attribute code="${deliveryType}" type="${deliveryType.TYPE}">
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
							<xsl:attribute name="templateId">${template.med.out.documentRegister}</xsl:attribute>
							<xsl:attribute name="processor">notificationForOutcome</xsl:attribute>
						</xsl:when>
						<xsl:when test="$notificationType='Отказано в регистрации'">
							<xsl:attribute name="templateId">${template.med.out.registrDenied}</xsl:attribute>
							<xsl:attribute name="processor">notificationForOutcome</xsl:attribute>
						</xsl:when>
						<xsl:when test="$notificationType='Назначен исполнитель'">
							<xsl:attribute name="templateId">${template.med.out.resolutionAccept}</xsl:attribute>
							<xsl:attribute name="processor">notificationForOutcome</xsl:attribute>
						</xsl:when>
						<xsl:when test="$notificationType='Доклад направлен'">
							<xsl:attribute name="templateId">${template.med.out.reportSent}</xsl:attribute>
							<xsl:attribute name="processor">notificationForOutcome</xsl:attribute>
						</xsl:when>
						<xsl:when test="$notificationType='&#x0418;сполнение'">
							<xsl:attribute name="templateId">${template.med.out.execution}</xsl:attribute>
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
		<attribute code="${_document_.header.uid}" type="${_document_.header.uid.TYPE}" name="Header\@UID">
			<value>
				<xsl:value-of select="." />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:header/@xdms:created" mode="document"> 
		<attribute code="${_document_.header.created}" type="${_document_.header.created.TYPE}" name="Header\@created">
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
		<attribute code="${_document_.header.source.uid}" type="${_document_.header.source.uid.TYPE}" name="Header\Source\@UID">
			<auxiliary>${param.source.organizationUID}</auxiliary>
			<value>
				<xsl:value-of select="." />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:header/xdms:source/xdms:organization" mode="document">
		<attribute code="${_document_.header.source.organization}" type="${_document_.header.source.organization.TYPE}" name="Header\Source\Organization">
			<auxiliary>${param.anyone.organization}</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:header/xdms:source/xdms:comment" mode="document">
		<attribute code="${_document_.header.source.comment}" type="${_document_.header.source.comment.TYPE}" name="Header\Source\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Operator -->
	<xsl:template match="xdms:header/xdms:operator"  mode="document">
		<attribute code="${_document_.header.operator}" type="${_document_.header.operator.TYPE}" name="Header\Operator">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Comment -->
	<xsl:template match="xdms:header/xdms:comment"  mode="document">
		<attribute code="${_document_.header.comment}" type="${_document_.header.comment.TYPE}" name="Header\Comment">
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
		<attribute code="${_notification_.header.uid}" type="${_notification_.header.uid.TYPE}" name="Header\@UID">
			<value>
				<xsl:value-of select="." />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:header/@xdms:created" mode="notification">
		<attribute code="${_notification_.header.created}" type="${_notification_.header.created.TYPE}" name="Header\@created">
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
		<attribute code="${_notification_.header.source.uid}" type="${_notification_.header.source.uid.TYPE}" name="Header\Source\@UID">
			<auxiliary>${param.source.organizationUID}</auxiliary>
			<value>
				<xsl:value-of select="." />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:header/xdms:source/xdms:organization" mode="notification">
		<attribute code="${_notification_.header.source.organization}" type="${_notification_.header.source.organization.TYPE}" name="Header\Source\Organization">
			<auxiliary>${param.anyone.organization}</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:header/xdms:source/xdms:comment" mode="notification">
		<attribute code="${_notification_.header.source.comment}" type="${_notification_.header.source.comment.TYPE}" name="Header\Source\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Operator -->
	<xsl:template match="xdms:header/xdms:operator" mode="notification">
		<attribute code="${_notification_.header.operator}" type="${_notification_.header.operator.TYPE}" name="Header\Operator">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Comment -->
	<xsl:template match="xdms:header/xdms:comment" mode="notification">
		<attribute code="${_notification_.header.comment}" type="${_notification_.header.comment.TYPE}" name="Header\Comment">
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
		<attribute code="${document.uid}" type="${document.uid.TYPE}" name="Document\@UID">
			<value><xsl:value-of select="." /></value>
		</attribute>
	</xsl:template>

	<!-- Kind -->

	<xsl:template match="xdms:document/xdms:kind">
		<docType>
			<attribute code="${document.kind}" type="${document.kind.TYPE}" name="Document\Kind">
				<auxiliary>${param.document.docType}</auxiliary>
				<value>
					<xsl:apply-templates />
				</value>
			</attribute>
		</docType>
		<attribute code="${document.kind}" type="${document.kind.TYPE}" name="Document\Kind">
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
		<attribute code="${document.num.number}" type="${document.num.number.TYPE}" name="Document\Num\Number">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:document/xdms:num/xdms:date">
		<attribute code="${document.num.date}" type="${document.num.date.TYPE}" name="Document\Num\Date">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Classification -->

	<xsl:template match="xdms:document/xdms:classification">
		<attribute code="${document.classification}" type="${document.classification.TYPE}" name="Document\Classification">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Urgency -->
	
	<xsl:template match="xdms:document/xdms:urgency">
		<attribute code="${document.urgency}" type="${document.urgency.TYPE}" name="Document\Urgency">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- InsteadOfDistributed -->
	
	<xsl:template match="xdms:document/xdms:insteadOfDistributed">
		<attribute code="${document.insteadOfDistributed}" type="${document.insteadOfDistributed.TYPE}" name="Document\InsteadOfDistributed">
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
		<attribute code="${document.signatories.signatory.region}" type="${document.signatories.signatory.region.TYPE}" name="Document\Signatories\Signatory\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:signatories/xdms:signatory/xdms:organization">
		<attribute code="${document.signatories.signatory.organization}" type="${document.signatories.signatory.organization.TYPE}" name="Document\Signatories\Signatory\Organization">
			<auxiliary>${param.anyone.organization}</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:signatories/xdms:signatory/xdms:person">
		<attribute code="${document.signatories.signatory.person}" type="${document.signatories.signatory.person.TYPE}" name="Document\Signatories\Signatory\Person">
			<auxiliary>${param.anyone.person}</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:signatories/xdms:signatory/xdms:department">
		<attribute code="${document.signatories.signatory.department}" type="${document.signatories.signatory.department.TYPE}" name="Document\Signatories\Signatory\Department">
			<auxiliary>${param.anyone.department}</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:signatories/xdms:signatory/xdms:post">
		<attribute code="${document.signatories.signatory.post}" type="${document.signatories.signatory.post.TYPE}" name="Document\Signatories\Signatory\Post">
			<auxiliary>${param.anyone.position}</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:signatories/xdms:signatory/xdms:contactInfo">
		<attribute code="${document.signatories.signatory.contactInfo}" type="${document.signatories.signatory.contactInfo.TYPE}" name="Document\Signatories\Signatory\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:signatories/xdms:signatory/xdms:signed">
		<attribute code="${document.signatories.signatory.signed}" type="${document.signatories.signatory.signed.TYPE}" name="Document\Signatories\Signatory\Signed">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:signatories/xdms:signatory/xdms:comment">
		<attribute code="${document.signatories.signatory.comment}" type="${document.signatories.signatory.comment.TYPE}" name="Document\Signatories\Signatory\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Addressees -->
	
	<xsl:template match="xdms:document/xdms:addressees">
		<receiver>
			<attribute code="${document.receiver}" type="${document.receiver.TYPE}" name="Document\Addressees\Addressee\Organization">
				<auxiliary>ORGANIZATION</auxiliary>
				<value>
					<xsl:apply-templates select="xdms:addressee/xdms:organization"/>
				</value>
			</attribute>
		</receiver>

		<attribute code="${document.addressees}" type="${document.addressees.TYPE}">
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
		<attribute code="${document.pages}" type="${document.pages.TYPE}" name="Document\Pages">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- EnclosuresPages -->
	
	<xsl:template match="xdms:document/xdms:enclosuresPages">
		<attribute code="${document.enclosuresPages}" type="${document.enclosuresPages.TYPE}" name="Document\EnclosuresPages">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Annotation -->
	
	<xsl:template match="xdms:document/xdms:annotation">
		<attribute code="${document.annotation}" type="${document.annotation.TYPE}" name="Document\Annotation">
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
		<attribute code="${document.enclosures.enclosure.title}" type="${document.enclosures.enclosure.title.TYPE}" name="Document\Enclosures\Enclosure\Title">
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
		<attribute code="${document.enclosures.enclosure.reference.region}" type="${document.enclosures.enclosure.reference.region.TYPE}" name="Document\Enclosures\Enclosure\Reference\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:organization">
		<attribute code="${document.enclosures.enclosure.reference.organization}" type="${document.enclosures.enclosure.reference.organization.TYPE}" name="Document\Enclosures\Enclosure\Reference\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:person">
		<attribute code="${document.enclosures.enclosure.reference.person}" type="${document.enclosures.enclosure.reference.person.TYPE}" name="Document\Enclosures\Enclosure\Reference\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:department">
		<attribute code="${document.enclosures.enclosure.reference.department}" type="${document.enclosures.enclosure.reference.department.TYPE}" name="Document\Enclosures\Enclosure\Reference\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:post">
		<attribute code="${document.enclosures.enclosure.reference.post}" type="${document.enclosures.enclosure.reference.post.TYPE}" name="Document\Enclosures\Enclosure\Reference\Post">
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
		<attribute code="${document.enclosures.enclosure.reference.num.number}" type="${document.enclosures.enclosure.reference.num.number.TYPE}" name="Document\Enclosures\Enclosure\Reference\Num\Number">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:num/xdms:date">
		<attribute code="${document.enclosures.enclosure.reference.num.date}" type="${document.enclosures.enclosure.reference.num.date.TYPE}" name="Document\Enclosures\Enclosure\Reference\Num\Date">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:comment">
		<attribute code="${document.enclosures.enclosure.reference.comment}" type="${document.enclosures.enclosure.reference.comment.TYPE}" name="Document\Enclosures\Enclosure\Reference\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:pages">
		<attribute code="${document.enclosures.enclosure.pages}" type="${document.enclosures.enclosure.pages.TYPE}" name="Document\Enclosures\Enclosure\Pages">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:files">
		<attribute code="${document.enclosures.enclosure.files}" type="${document.enclosures.enclosure.files.TYPE}" name="Document\Enclosures\Enclosure\Files">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:enclosures/xdms:enclosure/xdms:comment">
		<attribute code="${document.enclosures.enclosure.comment}" type="${document.enclosures.enclosure.comment.TYPE}" name="Document\Enclosures\Enclosure\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Correspondents -->
	
	<xsl:template match="xdms:document/xdms:correspondents">
		<attribute code="${document.correspondents}" type="${document.correspondents.TYPE}">
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
		<attribute code="${document.links.link.linkType}" type="${document.links.link.linkType.TYPE}" name="Document\Links\Link\LinkType">
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
		<attribute code="${document.links.link.document.kind}" type="${document.links.link.document.kind.TYPE}" name="Document\Links\Link\Document\Kind">
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
		<attribute code="${document.links.link.document.num.number}" type="${document.links.link.document.num.number.TYPE}" name="Document\Links\Link\Document\Num\Number">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:num/xdms:date">
		<attribute code="${document.links.link.document.num.date}" type="${document.links.link.document.num.date.TYPE}" name="Document\Links\Link\Document\Num\Date">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Links\Classification -->

	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:classification">
		<attribute code="${document.links.link.document.classification}" type="${document.links.link.document.classification.TYPE}" name="Document\Links\Link\Document\Classification">
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
		<attribute code="${document.links.link.document.signatories.signatory.region}" type="${document.links.link.document.signatories.signatory.region.TYPE}" name="Document\Links\Link\Document\Signatories\Signatory\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories/xdms:signatory/xdms:organization">
		<attribute code="${document.links.link.document.signatories.signatory.organization}" type="${document.links.link.document.signatories.signatory.organization.TYPE}" name="Document\Links\Link\Document\Signatories\Signatory\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories/xdms:signatory/xdms:person">
		<attribute code="${document.links.link.document.signatories.signatory.person}" type="${document.links.link.document.signatories.signatory.person.TYPE}" name="Document\Links\Link\Document\Signatories\Signatory\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories/xdms:signatory/xdms:department">
		<attribute code="${document.links.link.document.signatories.signatory.department}" type="${document.links.link.document.signatories.signatory.department.TYPE}" name="Document\Links\Link\Document\Signatories\Signatory\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories/xdms:signatory/xdms:post">
		<attribute code="${document.links.link.document.signatories.signatory.post}" type="${document.links.link.document.signatories.signatory.post.TYPE}" name="Document\Links\Link\Document\Signatories\Signatory\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories/xdms:signatory/xdms:contactInfo">
		<attribute code="${document.links.link.document.signatories.signatory.contactInfo}" type="${document.links.link.document.signatories.signatory.contactInfo.TYPE}" name="Document\Links\Link\Document\Signatories\Signatory\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories/xdms:signatory/xdms:signed">
		<attribute code="${document.links.link.document.signatories.signatory.signed}" type="${document.links.link.document.signatories.signatory.signed.TYPE}" name="Document\Links\Link\Document\Signatories\Signatory\Signed">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:signatories/xdms:signatory/xdms:comment">
		<attribute code="${document.links.link.document.signatories.signatory.comment}" type="${document.links.link.document.signatories.signatory.comment.TYPE}" name="Document\Links\Link\Document\Signatories\Signatory\Comment">
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
		<attribute code="${document.links.link.document.addressees.addressee.region}" type="${document.links.link.document.addressees.addressee.region.TYPE}" name="Document\Links\Link\Document\Addressees\Addressee\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:addressees/xdms:addressee/xdms:organization">
		<attribute code="${document.links.link.document.addressees.addressee.organization}" type="${document.links.link.document.addressees.addressee.organization.TYPE}" name="Document\Links\Link\Document\Addressees\Addressee\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:addressees/xdms:addressee/xdms:person">
		<attribute code="${document.links.link.document.addressees.addressee.person}" type="${document.links.link.document.addressees.addressee.person.TYPE}" name="Document\Links\Link\Document\Addressees\Addressee\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:addressees/xdms:addressee/xdms:department">
		<attribute code="${document.links.link.document.addressees.addressee.department}" type="${document.links.link.document.addressees.addressee.department.TYPE}" name="Document\Links\Link\Document\Addressees\Addressee\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:addressees/xdms:addressee/xdms:post">
		<attribute code="${document.links.link.document.addressees.addressee.post}" type="${document.links.link.document.addressees.addressee.post.TYPE}" name="Document\Links\Link\Document\Addressees\Addressee\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:addressees/xdms:addressee/xdms:contactInfo">
		<attribute code="${document.links.link.document.addressees.addressee.contactInfo}" type="${document.links.link.document.addressees.addressee.contactInfo.TYPE}" name="Document\Links\Link\Document\Addressees\Addressee\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:addressees/xdms:addressee/xdms:comment">
		<attribute code="${document.links.link.document.addressees.addressee.comment}" type="${document.links.link.document.addressees.addressee.comment.TYPE}" name="Document\Links\Link\Document\Addressees\Addressee\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Links\Pages -->

	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:pages">
		<attribute code="${document.links.link.document.pages}" type="${document.links.link.document.pages.TYPE}" name="Document\Links\Link\Document\Pages">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Links\EnclosuresPages -->
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosuresPages">
		<attribute code="${document.links.link.document.enclosuresPages}" type="${document.links.link.document.enclosuresPages.TYPE}" name="Document\Links\Link\Document\EnclosuresPages">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Links\Annotation -->
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:annotation">
		<attribute code="${document.links.link.document.annotation}" type="${document.links.link.document.annotation.TYPE}" name="Document\Links\Link\Document\Annotation">
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
		<attribute code="${document.links.link.document.enclosures.enclosure.title}" type="${document.links.link.document.enclosures.enclosure.title.TYPE}" name="Document\Links\Link\Document\Enclosures\Enclosure\Title">
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
		<attribute code="${document.links.link.document.enclosures.enclosure.reference.region}" type="${document.links.link.document.enclosures.enclosure.reference.region.TYPE}" name="Document\Links\Link\Document\Enclosures\Enclosure\Reference\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:organization">
		<attribute code="${document.links.link.document.enclosures.enclosure.reference.organization}" type="${document.links.link.document.enclosures.enclosure.reference.organization.TYPE}" name="Document\Links\Link\Document\Enclosures\Enclosure\Reference\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:person">
		<attribute code="${document.links.link.document.enclosures.enclosure.reference.person}" type="${document.links.link.document.enclosures.enclosure.reference.person.TYPE}" name="Document\Links\Link\Document\Enclosures\Enclosure\Reference\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:department">
		<attribute code="${document.links.link.document.enclosures.enclosure.reference.department}" type="${document.links.link.document.enclosures.enclosure.reference.department.TYPE}" name="Document\Links\Link\Document\Enclosures\Enclosure\Reference\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:post">
		<attribute code="${document.links.link.document.enclosures.enclosure.reference.post}" type="${document.links.link.document.enclosures.enclosure.reference.post.TYPE}" name="Document\Links\Link\Document\Enclosures\Enclosure\Reference\Post">
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
		<attribute code="${document.links.link.document.enclosures.enclosure.reference.num.number}" type="${document.links.link.document.enclosures.enclosure.reference.num.number.TYPE}" name="Document\Links\Link\Document\Enclosures\Enclosure\Reference\Num\Number">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:num/xdms:date">
		<attribute code="${document.links.link.document.enclosures.enclosure.reference.num.date}" type="${document.links.link.document.enclosures.enclosure.reference.num.date.TYPE}" name="Document\Links\Link\Document\Enclosures\Enclosure\Reference\Num\Date">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:reference/xdms:comment">
		<attribute code="${document.links.link.document.enclosures.enclosure.reference.comment}" type="${document.links.link.document.enclosures.enclosure.reference.comment.TYPE}" name="Document\Links\Link\Document\Enclosures\Enclosure\Reference\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:pages">
		<attribute code="${document.links.link.document.enclosures.enclosure.pages}" type="${document.links.link.document.enclosures.enclosure.pages.TYPE}" name="Document\Links\Link\Document\Enclosures\Enclosure\Pages">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:files">
		<attribute code="${document.links.link.document.enclosures.enclosure.files}" type="${document.links.link.document.enclosures.enclosure.files.TYPE}" name="Document\Links\Link\Document\Enclosures\Enclosure\Files">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:enclosures/xdms:enclosure/xdms:comment">
		<attribute code="${document.links.link.document.enclosures.enclosure.comment}" type="${document.links.link.document.enclosures.enclosure.comment.TYPE}" name="Document\Links\Link\Document\Enclosures\Enclosure\Comment">
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
		<attribute code="${document.links.link.document.correspondents.correspondent.region}" type="${document.links.link.document.correspondents.correspondent.region.TYPE}" name="Document\Links\Link\Document\Correspondents\Correspondent\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:organization">
		<attribute code="${document.links.link.document.correspondents.correspondent.organization}" type="${document.links.link.document.correspondents.correspondent.organization.TYPE}" name="Document\Links\Link\Document\Correspondents\Correspondent\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:person">
		<attribute code="${document.links.link.document.correspondents.correspondent.person}" type="${document.links.link.document.correspondents.correspondent.person.TYPE}" name="Document\Links\Link\Document\Correspondents\Correspondent\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:department">
		<attribute code="${document.links.link.document.correspondents.correspondent.department}" type="${document.links.link.document.correspondents.correspondent.department.TYPE}" name="Document\Links\Link\Document\Correspondents\Correspondent\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:post">
		<attribute code="${document.links.link.document.correspondents.correspondent.post}" type="${document.links.link.document.correspondents.correspondent.post.TYPE}" name="Document\Links\Link\Document\Correspondents\Correspondent\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:contactInfo">
		<attribute code="${document.links.link.document.correspondents.correspondent.contactInfo}" type="${document.links.link.document.correspondents.correspondent.contactInfo.TYPE}" name="Document\Links\Link\Document\Correspondents\Correspondent\ContactInfo">
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
		<attribute code="${document.links.link.document.correspondents.correspondent.num.number}" type="${document.links.link.document.correspondents.correspondent.num.number.TYPE}" name="Document\Links\Link\Document\Correspondents\Correspondent\Num\Number">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:num/xdms:date">
		<attribute code="${document.links.link.document.correspondents.correspondent.num.date}" type="${document.links.link.document.correspondents.correspondent.num.date.TYPE}" name="Document\Links\Link\Document\Correspondents\Correspondent\Num\Date">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:correspondents/xdms:correspondent/xdms:comment">
		<attribute code="${document.links.link.document.correspondents.correspondent.comment}" type="${document.links.link.document.correspondents.correspondent.comment.TYPE}" name="Document\Links\Link\Document\Correspondents\Correspondent\Comment">
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
		<attribute code="${document.links.link.document.clauses.clause.designation}" type="${document.links.link.document.clauses.clause.designation.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Designation">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:text">
		<attribute code="${document.links.link.document.clauses.clause.text}" type="${document.links.link.document.clauses.clause.text.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Text">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:deadline">
		<attribute code="${document.links.link.document.clauses.clause.deadline}" type="${document.links.link.document.clauses.clause.deadline.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Deadline">
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
		<attribute code="${document.links.link.document.clauses.clause.principal.region}" type="${document.links.link.document.clauses.clause.principal.region.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Principal\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:organization">
		<attribute code="${document.links.link.document.clauses.clause.principal.organization}" type="${document.links.link.document.clauses.clause.principal.organization.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Principal\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:person">
		<attribute code="${document.links.link.document.clauses.clause.principal.person}" type="${document.links.link.document.clauses.clause.principal.person.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Principal\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:department">
		<attribute code="${document.links.link.document.clauses.clause.principal.department}" type="${document.links.link.document.clauses.clause.principal.department.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Principal\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:post">
		<attribute code="${document.links.link.document.clauses.clause.principal.post}" type="${document.links.link.document.clauses.clause.principal.post.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Principal\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:contactInfo">
		<attribute code="${document.links.link.document.clauses.clause.principal.contactInfo}" type="${document.links.link.document.clauses.clause.principal.contactInfo.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Principal\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:comment">
		<attribute code="${document.links.link.document.clauses.clause.principal.comment}" type="${document.links.link.document.clauses.clause.principal.comment.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Principal\Comment">
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
		<attribute code="${document.links.link.document.clauses.clause.principals.name}" type="${document.links.link.document.clauses.clause.principals.name.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Principals\Name">
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
		<attribute code="${document.links.link.document.clauses.clause.principals.contents.region}" type="${document.links.link.document.clauses.clause.principals.contents.region.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Principals\Contents\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:organization">
		<attribute code="${document.links.link.document.clauses.clause.principals.contents.organization}" type="${document.links.link.document.clauses.clause.principals.contents.organization.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Principals\Contents\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:person">
		<attribute code="${document.links.link.document.clauses.clause.principals.contents.person}" type="${document.links.link.document.clauses.clause.principals.contents.person.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Principals\Contents\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:department">
		<attribute code="${document.links.link.document.clauses.clause.principals.contents.department}" type="${document.links.link.document.clauses.clause.principals.contents.department.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Principals\Contents\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:post">
		<attribute code="${document.links.link.document.clauses.clause.principals.contents.post}" type="${document.links.link.document.clauses.clause.principals.contents.post.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Principals\Contents\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:contactInfo">
		<attribute code="${document.links.link.document.clauses.clause.principals.contents.contactInfo}" type="${document.links.link.document.clauses.clause.principals.contents.contactInfo.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Principals\Contents\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:comment">
		<attribute code="${document.links.link.document.clauses.clause.principals.contents.comment}" type="${document.links.link.document.clauses.clause.principals.contents.comment.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Principals\Contents\Comment">
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
		<attribute code="${document.links.link.document.clauses.clause.parcipants.parcipant.region}" type="${document.links.link.document.clauses.clause.parcipants.parcipant.region.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipant\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:organization">
		<attribute code="${document.links.link.document.clauses.clause.parcipants.parcipant.organization}" type="${document.links.link.document.clauses.clause.parcipants.parcipant.organization.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipant\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:person">
		<attribute code="${document.links.link.document.clauses.clause.parcipants.parcipant.person}" type="${document.links.link.document.clauses.clause.parcipants.parcipant.person.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipant\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:department">
		<attribute code="${document.links.link.document.clauses.clause.parcipants.parcipant.department}" type="${document.links.link.document.clauses.clause.parcipants.parcipant.department.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipant\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:post">
		<attribute code="${document.links.link.document.clauses.clause.parcipants.parcipant.post}" type="${document.links.link.document.clauses.clause.parcipants.parcipant.post.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipant\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:contactInfo">
		<attribute code="${document.links.link.document.clauses.clause.parcipants.parcipant.contactInfo}" type="${document.links.link.document.clauses.clause.parcipants.parcipant.contactInfo.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipant\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:comment">
		<attribute code="${document.links.link.document.clauses.clause.parcipants.parcipant.comment}" type="${document.links.link.document.clauses.clause.parcipants.parcipant.comment.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipant\Comment">
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
		<attribute code="${document.links.link.document.clauses.clause.parcipants.parcipants.name}" type="${document.links.link.document.clauses.clause.parcipants.parcipants.name.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipants\Name">
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
		<attribute code="${document.links.link.document.clauses.clause.parcipants.parcipants.contents.region}" type="${document.links.link.document.clauses.clause.parcipants.parcipants.contents.region.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipants\Contents\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:organization">
		<attribute code="${document.links.link.document.clauses.clause.parcipants.parcipants.contents.organization}" type="${document.links.link.document.clauses.clause.parcipants.parcipants.contents.organization.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipants\Contents\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:person">
		<attribute code="${document.links.link.document.clauses.clause.parcipants.parcipants.contents.person}" type="${document.links.link.document.clauses.clause.parcipants.parcipants.contents.person.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipants\Contents\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:department">
		<attribute code="${document.links.link.document.clauses.clause.parcipants.parcipants.contents.department}" type="${document.links.link.document.clauses.clause.parcipants.parcipants.contents.department.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipants\Contents\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:post">
		<attribute code="${document.links.link.document.clauses.clause.parcipants.parcipants.contents.post}" type="${document.links.link.document.clauses.clause.parcipants.parcipants.contents.post.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipants\Contents\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:contactInfo">
		<attribute code="${document.links.link.document.clauses.clause.parcipants.parcipants.contents.contactInfo}" type="${document.links.link.document.clauses.clause.parcipants.parcipants.contents.contactInfo.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipants\Contents\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:comment">
		<attribute code="${document.links.link.document.clauses.clause.parcipants.parcipants.contents.comment}" type="${document.links.link.document.clauses.clause.parcipants.parcipants.contents.comment.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Parcipants\Parcipants\Contents\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:clauses/xdms:clause/xdms:comment">
		<attribute code="${document.links.link.document.clauses.clause.comment}" type="${document.links.link.document.clauses.clause.comment.TYPE}" name="Document\Links\Link\Document\Clauses\Clause\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Links\Comment -->
	
	<xsl:template match="xdms:document/xdms:links/xdms:link/xdms:document/xdms:comment">
		<attribute code="${document.links.link.document.comment}" type="${document.links.link.document.comment.TYPE}" name="Document\Links\Link\Document\Comment">
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
		<attribute code="${document.clauses.clause.designation}" type="${document.clauses.clause.designation.TYPE}" name="Document\Clauses\Clause\Designation">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:text">
		<attribute code="${document.clauses.clause.text}" type="${document.clauses.clause.text.TYPE}" name="Document\Clauses\Clause\Text">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:deadline">
		<attribute code="${document.clauses.clause.deadline}" type="${document.clauses.clause.deadline.TYPE}" name="Document\Clauses\Clause\Deadline">
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
		<attribute code="${document.clauses.clause.principal.region}" type="${document.clauses.clause.principal.region.TYPE}" name="Document\Clauses\Clause\Principal\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:organization">
		<attribute code="${document.clauses.clause.principal.organization}" type="${document.clauses.clause.principal.organization.TYPE}" name="Document\Clauses\Clause\Principal\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:person">
		<attribute code="${document.clauses.clause.principal.person}" type="${document.clauses.clause.principal.person.TYPE}" name="Document\Clauses\Clause\Principal\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:department">
		<attribute code="${document.clauses.clause.principal.department}" type="${document.clauses.clause.principal.department.TYPE}" name="Document\Clauses\Clause\Principal\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:post">
		<attribute code="${document.clauses.clause.principal.post}" type="${document.clauses.clause.principal.post.TYPE}" name="Document\Clauses\Clause\Principal\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:contactInfo">
		<attribute code="${document.clauses.clause.principal.contactInfo}" type="${document.clauses.clause.principal.contactInfo.TYPE}" name="Document\Clauses\Clause\Principal\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principal/xdms:comment">
		<attribute code="${document.clauses.clause.principal.comment}" type="${document.clauses.clause.principal.comment.TYPE}" name="Document\Clauses\Clause\Principal\Comment">
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
		<attribute code="${document.clauses.clause.principals.name}" type="${document.clauses.clause.principals.name.TYPE}" name="Document\Clauses\Clause\Principals\Name">
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
		<attribute code="${document.clauses.clause.principals.contents.region}" type="${document.clauses.clause.principals.contents.region.TYPE}" name="Document\Clauses\Clause\Principals\Contents\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:organization">
		<attribute code="${document.clauses.clause.principals.contents.organization}" type="${document.clauses.clause.principals.contents.organization.TYPE}" name="Document\Clauses\Clause\Principals\Contents\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:person">
		<attribute code="${document.clauses.clause.principals.contents.person}" type="${document.clauses.clause.principals.contents.person.TYPE}" name="Document\Clauses\Clause\Principals\Contents\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:department">
		<attribute code="${document.clauses.clause.principals.contents.department}" type="${document.clauses.clause.principals.contents.department.TYPE}" name="Document\Clauses\Clause\Principals\Contents\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:post">
		<attribute code="${document.clauses.clause.principals.contents.post}" type="${document.clauses.clause.principals.contents.post.TYPE}" name="Document\Clauses\Clause\Principals\Contents\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:contactInfo">
		<attribute code="${document.clauses.clause.principals.contents.contactInfo}" type="${document.clauses.clause.principals.contents.contactInfo.TYPE}" name="Document\Clauses\Clause\Principals\Contents\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:principals/xdms:contents/xdms:comment">
		<attribute code="${document.clauses.clause.principals.contents.comment}" type="${document.clauses.clause.principals.contents.comment.TYPE}" name="Document\Clauses\Clause\Principals\Contents\Comment">
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
		<attribute code="${document.clauses.clause.parcipants.parcipant.region}" type="${document.clauses.clause.parcipants.parcipant.region.TYPE}" name="Document\Clauses\Clause\Parcipants\Parcipant\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:organization">
		<attribute code="${document.clauses.clause.parcipants.parcipant.organization}" type="${document.clauses.clause.parcipants.parcipant.organization.TYPE}" name="Document\Clauses\Clause\Parcipants\Parcipant\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:person">
		<attribute code="${document.clauses.clause.parcipants.parcipant.person}" type="${document.clauses.clause.parcipants.parcipant.person.TYPE}" name="Document\Clauses\Clause\Parcipants\Parcipant\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:department">
		<attribute code="${document.clauses.clause.parcipants.parcipant.department}" type="${document.clauses.clause.parcipants.parcipant.department.TYPE}" name="Document\Clauses\Clause\Parcipants\Parcipant\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:post">
		<attribute code="${document.clauses.clause.parcipants.parcipant.post}" type="${document.clauses.clause.parcipants.parcipant.post.TYPE}" name="Document\Clauses\Clause\Parcipants\Parcipant\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:contactInfo">
		<attribute code="${document.clauses.clause.parcipants.parcipant.contactInfo}" type="${document.clauses.clause.parcipants.parcipant.contactInfo.TYPE}" name="Document\Clauses\Clause\Parcipants\Parcipant\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipant/xdms:comment">
		<attribute code="${document.clauses.clause.parcipants.parcipant.comment}" type="${document.clauses.clause.parcipants.parcipant.comment.TYPE}" name="Document\Clauses\Clause\Parcipants\Parcipant\Comment">
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
		<attribute code="${document.clauses.clause.parcipants.parcipants.name}" type="${document.clauses.clause.parcipants.parcipants.name.TYPE}" name="Document\Clauses\Clause\Parcipants\Parcipants\Name">
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
		<attribute code="${document.clauses.clause.parcipants.parcipants.contents.region}" type="${document.clauses.clause.parcipants.parcipants.contents.region.TYPE}" name="Document\Clauses\Clause\Parcipants\Parcipants\Contents\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:organization">
		<attribute code="${document.clauses.clause.parcipants.parcipants.contents.organization}" type="${document.clauses.clause.parcipants.parcipants.contents.organization.TYPE}" name="Document\Clauses\Clause\Parcipants\Parcipants\Contents\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:person">
		<attribute code="${document.clauses.clause.parcipants.parcipants.contents.person}" type="${document.clauses.clause.parcipants.parcipants.contents.person.TYPE}" name="Document\Clauses\Clause\Parcipants\Parcipants\Contents\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:department">
		<attribute code="${document.clauses.clause.parcipants.parcipants.contents.department}" type="${document.clauses.clause.parcipants.parcipants.contents.department.TYPE}" name="Document\Clauses\Clause\Parcipants\Parcipants\Contents\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:post">
		<attribute code="${document.clauses.clause.parcipants.parcipants.contents.post}" type="${document.clauses.clause.parcipants.parcipants.contents.post.TYPE}" name="Document\Clauses\Clause\Parcipants\Parcipants\Contents\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:contactInfo">
		<attribute code="${document.clauses.clause.parcipants.parcipants.contents.contactInfo}" type="${document.clauses.clause.parcipants.parcipants.contents.contactInfo.TYPE}" name="Document\Clauses\Clause\Parcipants\Parcipants\Contents\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:comment">
		<attribute code="${document.clauses.clause.parcipants.parcipants.contents.comment}" type="${document.clauses.clause.parcipants.parcipants.contents.comment.TYPE}" name="Document\Clauses\Clause\Parcipants\Parcipants\Contents\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:clauses/xdms:clause/xdms:comment">
		<attribute code="${document.clauses.clause.comment}" type="${document.clauses.clause.comment.TYPE}" name="Document\Clauses\Clause\Comment">
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
		<attribute code="${document.executor.region}" type="${document.executor.region.TYPE}" name="Document\Executor\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:executor/xdms:organization">
		<attribute code="${document.executor.organization}" type="${document.executor.organization.TYPE}" name="Document\Executor\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:executor/xdms:person">
		<attribute code="${document.executor.person}" type="${document.executor.person.TYPE}" name="Document\Executor\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:executor/xdms:department">
		<attribute code="${document.executor.department}" type="${document.executor.department.TYPE}" name="Document\Executor\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:executor/xdms:post">
		<attribute code="${document.executor.post}" type="${document.executor.post.TYPE}" name="Document\Executor\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:document/xdms:executor/xdms:contactInfo">
		<attribute code="${document.executor.contactInfo}" type="${document.executor.contactInfo.TYPE}" name="Document\Executor\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:document/xdms:executor/xdms:comment">
		<attribute code="${document.executor.comment}" type="${document.executor.comment.TYPE}" name="Document\Executor\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<!-- Comment -->
	
	<xsl:template match="xdms:document/xdms:comment">
		<service>
			<attribute code="${document.comment}" type="${document.comment.TYPE}" name="Document\Comment">
				<auxiliary>${param.document.serviceCode}</auxiliary>
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
		<attribute code="${files.file.group}" type="${files.file.group.TYPE}" name="Files\File\Group">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:files/xdms:file/xdms:description">
		<attribute code="${files.file.description}" type="${files.file.description.TYPE}" name="Files\File\Description">
			<auxiliary>${param.file.description}</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:files/xdms:file/xdms:pages">
		<attribute code="${files.file.pages}" type="${files.file.pages.TYPE}" name="Files\File\Pages">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:files/xdms:file/xdms:eds">
		<attribute code="${files.file.eds}" type="${files.file.eds.TYPE}" name="Files\File\EDS">
			<auxiliary>${param.file.eds}</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:files/xdms:file/@xdms:localName">
		<attribute code="${files.file.attrLocalName}" type="${files.file.attrLocalName.TYPE}" name="Files\File\AttrLocalName">
			<auxiliary>${param.file.name}</auxiliary>
			<value>
				<xsl:value-of select="."/>
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:files/xdms:file/@xdms:type">
		<attribute> <!-- code="..." type="..." name="..."> --> 
			<auxiliary>${param.file.isReference}</auxiliary>
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
		<attribute code="${notification.uid}" type="${notification.uid.TYPE}" name="Notification\@UID">
			<auxiliary>${param.cardId.notification.uid}</auxiliary>
			<value>
				<xsl:value-of select="." />
			</value>
		</attribute>
	</xsl:template>

	<xsl:template match="xdms:notification/@xdms:id">
		<attribute code="${notification.id}" type="${notification.id.TYPE}" name="Notification\@ID">
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
		<attribute code="${notification.documentAccepted.num.number}" type="${notification.documentAccepted.num.number.TYPE}" name="Notification\DocumentAccepted\Num\Number">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:documentAccepted/xdms:num/xdms:date">
		<attribute code="${notification.documentAccepted.num.date}" type="${notification.documentAccepted.num.date.TYPE}" name="Notification\DocumentAccepted\Num\Date">
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
		<attribute code="${notification.documentRefused.reason}" type="${notification.documentRefused.reason.TYPE}" name="Notification\DocumentRefused\Reason">
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
		<attribute code="${notification.executorAssigned.secretary.region}" type="${notification.executorAssigned.secretary.region.TYPE}" name="Notification\ExecutorAssigned\Secretary\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:secretary/xdms:organization">
		<attribute code="${notification.executorAssigned.secretary.organization}" type="${notification.executorAssigned.secretary.organization.TYPE}" name="Notification\ExecutorAssigned\Secretary\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:secretary/xdms:person">
		<attribute code="${notification.executorAssigned.secretary.person}" type="${notification.executorAssigned.secretary.person.TYPE}" name="Notification\ExecutorAssigned\Secretary\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:secretary/xdms:department">
		<attribute code="${notification.executorAssigned.secretary.department}" type="${notification.executorAssigned.secretary.department.TYPE}" name="Notification\ExecutorAssigned\Secretary\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:secretary/xdms:post">
		<attribute code="${notification.executorAssigned.secretary.post}" type="${notification.executorAssigned.secretary.post.TYPE}" name="Notification\ExecutorAssigned\Secretary\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:secretary/xdms:contactInfo">
		<attribute code="${notification.executorAssigned.secretary.contactInfo}" type="${notification.executorAssigned.secretary.contactInfo.TYPE}" name="Notification\ExecutorAssigned\Secretary\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:secretary/xdms:comment">
		<attribute code="${notification.executorAssigned.secretary.comment}" type="${notification.executorAssigned.secretary.comment.TYPE}" name="Notification\ExecutorAssigned\Secretary\Comment">
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
		<attribute code="${notification.executorAssigned.manager.region}" type="${notification.executorAssigned.manager.region.TYPE}" name="Notification\ExecutorAssigned\Manager\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:manager/xdms:organization">
		<attribute code="${notification.executorAssigned.manager.organization}" type="${notification.executorAssigned.manager.organization.TYPE}" name="Notification\ExecutorAssigned\Manager\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:manager/xdms:person">
		<attribute code="${notification.executorAssigned.manager.person}" type="${notification.executorAssigned.manager.person.TYPE}" name="Notification\ExecutorAssigned\Manager\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:manager/xdms:department">
		<attribute code="${notification.executorAssigned.manager.department}" type="${notification.executorAssigned.manager.department.TYPE}" name="Notification\ExecutorAssigned\Manager\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:manager/xdms:post">
		<attribute code="${notification.executorAssigned.manager.post}" type="${notification.executorAssigned.manager.post.TYPE}" name="Notification\ExecutorAssigned\Manager\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:manager/xdms:contactInfo">
		<attribute code="${notification.executorAssigned.manager.contactInfo}" type="${notification.executorAssigned.manager.contactInfo.TYPE}" name="Notification\ExecutorAssigned\Manager\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:manager/xdms:comment">
		<attribute code="${notification.executorAssigned.manager.comment}" type="${notification.executorAssigned.manager.comment.TYPE}" name="Notification\ExecutorAssigned\Manager\Comment">
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
		<attribute code="${notification.executorAssigned.executor.region}" type="${notification.executorAssigned.executor.region.TYPE}" name="Notification\ExecutorAssigned\Executor\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:executor/xdms:organization">
		<attribute code="${notification.executorAssigned.executor.organization}" type="${notification.executorAssigned.executor.organization.TYPE}" name="Notification\ExecutorAssigned\Executor\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:executor/xdms:person">
		<attribute code="${notification.executorAssigned.executor.person}" type="${notification.executorAssigned.executor.person.TYPE}" name="Notification\ExecutorAssigned\Executor\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:executor/xdms:department">
		<attribute code="${notification.executorAssigned.executor.department}" type="${notification.executorAssigned.executor.department.TYPE}" name="Notification\ExecutorAssigned\Executor\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:executor/xdms:post">
		<attribute code="${notification.executorAssigned.executor.post}" type="${notification.executorAssigned.executor.post.TYPE}" name="Notification\ExecutorAssigned\Executor\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:executor/xdms:contactInfo">
		<attribute code="${notification.executorAssigned.executor.contactInfo}" type="${notification.executorAssigned.executor.contactInfo.TYPE}" name="Notification\ExecutorAssigned\Executor\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:executorAssigned/xdms:executor/xdms:comment">
		<attribute code="${notification.executorAssigned.executor.comment}" type="${notification.executorAssigned.executor.comment.TYPE}" name="Notification\ExecutorAssigned\Executor\Comment">
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
		<attribute code="${notification.reportPrepared.signatory.region}" type="${notification.reportPrepared.signatory.region.TYPE}" name="Notification\ReportPrepared\Signatory\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:reportPrepared/xdms:signatory/xdms:organization">
		<attribute code="${notification.reportPrepared.signatory.organization}" type="${notification.reportPrepared.signatory.organization.TYPE}" name="Notification\ReportPrepared\Signatory\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:reportPrepared/xdms:signatory/xdms:person">
		<attribute code="${notification.reportPrepared.signatory.person}" type="${notification.reportPrepared.signatory.person.TYPE}" name="Notification\ReportPrepared\Signatory\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:reportPrepared/xdms:signatory/xdms:department">
		<attribute code="${notification.reportPrepared.signatory.department}" type="${notification.reportPrepared.signatory.department.TYPE}" name="Notification\ReportPrepared\Signatory\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportPrepared/xdms:signatory/xdms:post">
		<attribute code="${notification.reportPrepared.signatory.post}" type="${notification.reportPrepared.signatory.post.TYPE}" name="Notification\ReportPrepared\Signatory\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:reportPrepared/xdms:signatory/xdms:contactInfo">
		<attribute code="${notification.reportPrepared.signatory.contactInfo}" type="${notification.reportPrepared.signatory.contactInfo.TYPE}" name="Notification\ReportPrepared\Signatory\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:notification/xdms:reportPrepared/xdms:signatory/xdms:signed">
		<attribute code="${notification.reportPrepared.signatory.signed}" type="${notification.reportPrepared.signatory.signed.TYPE}" name="Notification\ReportPrepared\Signatory\Signed">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportPrepared/xdms:signatory/xdms:comment">
		<attribute code="${notification.reportPrepared.signatory.comment}" type="${notification.reportPrepared.signatory.comment.TYPE}" name="Notification\ReportPrepared\Signatory\Comment">
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
		<attribute code="${notification.reportSent.report.region}" type="${notification.reportSent.report.region.TYPE}" name="Notification\ReportSent\Report\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportSent/xdms:report/xdms:organization">
		<attribute code="${notification.reportSent.report.organization}" type="${notification.reportSent.report.organization.TYPE}" name="Notification\ReportSent\Report\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportSent/xdms:report/xdms:person">
		<attribute code="${notification.reportSent.report.person}" type="${notification.reportSent.report.person.TYPE}" name="Notification\ReportSent\Report\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportSent/xdms:report/xdms:department">
		<attribute code="${notification.reportSent.report.department}" type="${notification.reportSent.report.department.TYPE}" name="Notification\ReportSent\Report\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportSent/xdms:report/xdms:post">
		<attribute code="${notification.reportSent.report.post}" type="${notification.reportSent.report.post.TYPE}" name="Notification\ReportSent\Report\Post">
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
		<attribute code="${notification.reportSent.report.num.number}" type="${notification.reportSent.report.num.number.TYPE}" name="Notification\ReportSent\Report\Num\Number">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportSent/xdms:report/xdms:num/xdms:date">
		<attribute code="${notification.reportSent.report.num.date}" type="${notification.reportSent.report.num.date.TYPE}" name="Notification\ReportSent\Report\Num\Date">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:reportSent/xdms:report/xdms:comment">
		<attribute code="${notification.reportSent.report.comment}" type="${notification.reportSent.report.comment.TYPE}" name="Notification\ReportSent\Report\Comment">
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
		<attribute code="${notification.courseChanged.courseText}" type="${notification.courseChanged.courseText.TYPE}" name="Notification\CourseChanged\CourseText">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
		<attribute code="${notification.courseChanged.courseText_status}" type="${notification.courseChanged.courseText_status.TYPE}" name="Notification\CourseChanged\CourseText">
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
		<attribute code="${notification.courseChanged.reference.region}" type="${notification.courseChanged.reference.region.TYPE}" name="Notification\CourseChanged\Reference\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:reference/xdms:organization">
		<attribute code="${notification.courseChanged.reference.organization}" type="${notification.courseChanged.reference.organization.TYPE}" name="Notification\CourseChanged\Reference\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:reference/xdms:person">
		<attribute code="${notification.courseChanged.reference.person}" type="${notification.courseChanged.reference.person.TYPE}" name="Notification\CourseChanged\Reference\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:reference/xdms:department">
		<attribute code="${notification.courseChanged.reference.department}" type="${notification.courseChanged.reference.department.TYPE}" name="Notification\CourseChanged\Reference\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:reference/xdms:post">
		<attribute code="${notification.courseChanged.reference.post}" type="${notification.courseChanged.reference.post.TYPE}" name="Notification\CourseChanged\Reference\Post">
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
		<attribute code="${notification.courseChanged.reference.num.number}" type="${notification.courseChanged.reference.num.number.TYPE}" name="Notification\CourseChanged\Reference\Num\Number">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:reference/xdms:num/xdms:date">
		<attribute code="${notification.courseChanged.reference.num.date}" type="${notification.courseChanged.reference.num.date.TYPE}" name="Notification\CourseChanged\Reference\Num\Date">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:notification/xdms:courseChanged/xdms:reference/xdms:comment">
		<attribute code="${notification.courseChanged.reference.comment}" type="${notification.courseChanged.reference.comment.TYPE}" name="Notification\CourseChanged\Reference\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<!--Notification Comment-->

	<xsl:template match="xdms:notification/xdms:comment">
		<attribute code="${notification.comment}" type="${notification.comment.TYPE}" name="Notification\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>

	<!-- Time -->
	<xsl:template match="xdms:time" mode="notification">
		<attribute code="${notification.time}" type="${notification.time.TYPE}" name="Notification\Time">
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
		<attribute code="${notification.foundation.region}" type="${notification.foundation.region.TYPE}" name="Notification\Foundation\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:foundation/xdms:organization" mode="notification">
		<attribute code="${notification.foundation.organization}" type="${notification.foundation.organization.TYPE}" name="Notification\Foundation\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:foundation/xdms:person" mode="notification">
		<attribute code="${notification.foundation.person}" type="${notification.foundation.person.TYPE}" name="Notification\Foundation\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:foundation/xdms:department" mode="notification">
		<attribute code="${notification.foundation.department}" type="${notification.foundation.department.TYPE}" name="Notification\Foundation\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:foundation/xdms:post" mode="notification">
		<attribute code="${notification.foundation.post}" type="${notification.foundation.post.TYPE}" name="Notification\Foundation\Post">
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
		<attribute code="${notification.foundation.num.number}" type="${notification.foundation.num.number.TYPE}" name="Notification\Foundation\Num\Number">
			<auxiliary>${param.documentReference.regnum}</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:foundation/xdms:num/xdms:date" mode="notification">
		<attribute code="${notification.foundation.num.date}" type="${notification.foundation.num.date.TYPE}" name="Notification\Foundation\Num\Date">
			<auxiliary>${param.documentReference.regdate}</auxiliary>
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:foundation/xdms:comment" mode="notification">
		<attribute code="${notification.foundation.comment}" type="${notification.foundation.comment.TYPE}" name="Notification\Foundation\Comment">
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
		<attribute code="${notification.clause.designation}" type="${notification.clause.designation.TYPE}" name="Notification\Clause\Designation">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:text" mode="notification">
		<attribute code="${notification.clause.text}" type="${notification.clause.text.TYPE}" name="Notification\Clause\Text">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:deadline" mode="notification">
		<attribute code="${notification.clause.deadline}" type="${notification.clause.deadline.TYPE}" name="Notification\Clause\Deadline">
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
		<attribute code="${notification.clause.principal.region}" type="${notification.clause.principal.region.TYPE}" name="Notification\Clause\Principal\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principal/xdms:organization" mode="notification">
		<attribute code="${notification.clause.principal.organization}" type="${notification.clause.principal.organization.TYPE}" name="Notification\Clause\Principal\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principal/xdms:person" mode="notification">
		<attribute code="${notification.clause.principal.person}" type="${notification.clause.principal.person.TYPE}" name="Notification\Clause\Principal\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principal/xdms:department" mode="notification">
		<attribute code="${notification.clause.principal.department}" type="${notification.clause.principal.department.TYPE}" name="Notification\Clause\Principal\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:principal/xdms:post" mode="notification">
		<attribute code="${notification.clause.principal.post}" type="${notification.clause.principal.post.TYPE}" name="Notification\Clause\Principal\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principal/xdms:contactInfo" mode="notification">
		<attribute code="${notification.clause.principal.contactInfo}" type="${notification.clause.principal.contactInfo.TYPE}" name="Notification\Clause\Principal\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:principal/xdms:comment" mode="notification">
		<attribute code="${notification.clause.principal.comment}" type="${notification.clause.principal.comment.TYPE}" name="Notification\Clause\Principal\Comment">
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
		<attribute code="${notification.clause.principals.name}" type="${notification.clause.principals.name.TYPE}" name="Notification\Clause\Principals\Name">
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
		<attribute code="${notification.clause.principals.contents.region}" type="${notification.clause.principals.contents.region.TYPE}" name="Notification\Clause\Principals\Contents\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principals/xdms:contents/xdms:organization" mode="notification">
		<attribute code="${notification.clause.principals.contents.organization}" type="${notification.clause.principals.contents.organization.TYPE}" name="Notification\Clause\Principals\Contents\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principals/xdms:contents/xdms:person" mode="notification">
		<attribute code="${notification.clause.principals.contents.person}" type="${notification.clause.principals.contents.person.TYPE}" name="Notification\Clause\Principals\Contents\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principals/xdms:contents/xdms:department" mode="notification">
		<attribute code="${notification.clause.principals.contents.department}" type="${notification.clause.principals.contents.department.TYPE}" name="Notification\Clause\Principals\Contents\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:principals/xdms:contents/xdms:post" mode="notification">
		<attribute code="${notification.clause.principals.contents.post}" type="${notification.clause.principals.contents.post.TYPE}" name="Notification\Clause\Principals\Contents\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:principals/xdms:contents/xdms:contactInfo" mode="notification">
		<attribute code="${notification.clause.principals.contents.contactInfo}" type="${notification.clause.principals.contents.contactInfo.TYPE}" name="Notification\Clause\Principals\Contents\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:principals/xdms:contents/xdms:comment" mode="notification">
		<attribute code="${notification.clause.principals.contents.comment}" type="${notification.clause.principals.contents.comment.TYPE}" name="Notification\Clause\Principals\Contents\Comment">
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
		<attribute code="${notification.clause.parcipants.parcipant.region}" type="${notification.clause.parcipants.parcipant.region.TYPE}" name="Notification\Clause\Parcipants\Parcipant\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipant/xdms:organization" mode="notification">
		<attribute code="${notification.clause.parcipants.parcipant.organization}" type="${notification.clause.parcipants.parcipant.organization.TYPE}" name="Notification\Clause\Parcipants\Parcipant\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipant/xdms:person" mode="notification">
		<attribute code="${notification.clause.parcipants.parcipant.person}" type="${notification.clause.parcipants.parcipant.person.TYPE}" name="Notification\Clause\Parcipants\Parcipant\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipant/xdms:department" mode="notification">
		<attribute code="${notification.clause.parcipants.parcipant.department}" type="${notification.clause.parcipants.parcipant.department.TYPE}" name="Notification\Clause\Parcipants\Parcipant\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipant/xdms:post" mode="notification">
		<attribute code="${notification.clause.parcipants.parcipant.post}" type="${notification.clause.parcipants.parcipant.post.TYPE}" name="Notification\Clause\Parcipants\Parcipant\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipant/xdms:contactInfo" mode="notification">
		<attribute code="${notification.clause.parcipants.parcipant.contactInfo}" type="${notification.clause.parcipants.parcipant.contactInfo.TYPE}" name="Notification\Clause\Parcipants\Parcipant\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipant/xdms:comment" mode="notification">
		<attribute code="${notification.clause.parcipants.parcipant.comment}" type="${notification.clause.parcipants.parcipant.comment.TYPE}" name="Notification\Clause\Parcipants\Parcipant\Comment">
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
		<attribute code="${notification.clause.parcipants.parcipants.name}" type="${notification.clause.parcipants.parcipants.name.TYPE}" name="Notification\Clause\Parcipants\Parcipants\Name">
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
		<attribute code="${notification.clause.parcipants.parcipants.contents.region}" type="${notification.clause.parcipants.parcipants.contents.region.TYPE}" name="Notification\Clause\Parcipants\Parcipants\Contents\Region">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:organization" mode="notification">
		<attribute code="${notification.clause.parcipants.parcipants.contents.organization}" type="${notification.clause.parcipants.parcipants.contents.organization.TYPE}" name="Notification\Clause\Parcipants\Parcipants\Contents\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:person" mode="notification">
		<attribute code="${notification.clause.parcipants.parcipants.contents.person}" type="${notification.clause.parcipants.parcipants.contents.person.TYPE}" name="Notification\Clause\Parcipants\Parcipants\Contents\Person">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:department" mode="notification">
		<attribute code="${notification.clause.parcipants.parcipants.contents.department}" type="${notification.clause.parcipants.parcipants.contents.department.TYPE}" name="Notification\Clause\Parcipants\Parcipants\Contents\Department">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:post" mode="notification">
		<attribute code="${notification.clause.parcipants.parcipants.contents.post}" type="${notification.clause.parcipants.parcipants.contents.post.TYPE}" name="Notification\Clause\Parcipants\Parcipants\Contents\Post">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>	
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:contactInfo" mode="notification">
		<attribute code="${notification.clause.parcipants.parcipants.contents.contactInfo}" type="${notification.clause.parcipants.parcipants.contents.contactInfo.TYPE}" name="Notification\Clause\Parcipants\Parcipants\Contents\ContactInfo">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:parcipants/xdms:parcipants/xdms:contents/xdms:comment" mode="notification">
		<attribute code="${notification.clause.parcipants.parcipants.contents.comment}" type="${notification.clause.parcipants.parcipants.contents.comment.TYPE}" name="Notification\Clause\Parcipants\Parcipants\Contents\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:clause/xdms:comment" mode="notification">
		<attribute code="${notification.clause.comment}" type="${notification.clause.comment.TYPE}" name="Notification\Clause\Comment">
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
		<attribute code="${deliveryIndex.destination.destination.organization}" type="${deliveryIndex.destination.destination.organization.TYPE}" name="DeliveryIndex\Destination\Destination\Organization">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:deliveryIndex/xdms:destination/xdms:destination/xdms:comment">
		<attribute code="${deliveryIndex.destination.destination.comment}" type="${deliveryIndex.destination.destination.comment.TYPE}" name="DeliveryIndex\Destination\Destination\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:deliveryIndex/xdms:destination/xdms:files">
		<attribute code="${deliveryIndex.destination.files}" type="${deliveryIndex.destination.files.TYPE}" name="DeliveryIndex\Destination\Files">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
	
	<xsl:template match="xdms:deliveryIndex/xdms:destination/xdms:comment">
		<attribute code="${deliveryIndex.destination.comment}" type="${deliveryIndex.destination.comment.TYPE}" name="DeliveryIndex\Destination\Comment">
			<value>
				<xsl:apply-templates />
			</value>
		</attribute>
	</xsl:template>
</xsl:stylesheet>


