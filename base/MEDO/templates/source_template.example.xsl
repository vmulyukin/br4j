<?xml version="1.0" encoding="UTF-8"?>
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
<xsl:stylesheet 
	  xmlns="http://aplana.com/dbmi/exchange/model/Card"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
      xmlns:xdms="http://www.infpres.com/IEDMS" 
	  exclude-result-prefixes="xdms">
	
    <xsl:output method="xml" indent="yes"/>
	
	<xsl:template match="/">
		<card>
			<xsl:apply-templates select="xdms:communication/xdms:document" />
		</card>
	</xsl:template>

   <xsl:template match="xdms:document">
   		<xsl:apply-templates select="@xdms:uid"/>
		<xsl:apply-templates select="@xdms:id"/>
   </xsl:template>
   
   <xsl:template match="xdms:document/@xdms:uid">
		<attribute code="${document.uid}" type="${document.uid.TYPE}" name="Document\@UID">
			<value><xsl:value-of select="." /></value>
		</attribute>
   </xsl:template>
   
   <xsl:template match="xdms:document/@xdms:id">
		<attribute code="${document.id}" type="${document.id.TYPE}" name="Document\@ID">
			<value><xsl:value-of select="." /></value>
		</attribute>
   </xsl:template>
</xsl:stylesheet>