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
<xsl:stylesheet version="1.0" 
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
        xmlns:xs="http://www.w3.org/2001/XMLSchema"
        xmlns="http://www.w3.org/1999/xhtml">
    <xsl:output method="html" version="4.0" encoding="UTF-8" indent="no" omit-xml-declaration="yes"/>
    <xsl:template match="/">
    	<xsl:if test="/report/part">
	        <xsl:for-each select="/report/part">
	 			<xsl:sort select="position()" data-type="number" order="descending"/>
				<xsl:value-of select="."/>
				<xsl:if test="not (position()=last())">
					<xsl:text>; </xsl:text>
				</xsl:if> 
			</xsl:for-each>
		</xsl:if>
    </xsl:template>
</xsl:stylesheet>