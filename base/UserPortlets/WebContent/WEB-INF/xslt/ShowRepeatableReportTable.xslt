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
<!-- Шаблон преобразует xml из атрибута jbr.report.text в таблицу для вывода в jsp -->
<xsl:stylesheet version="1.0" 
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
        xmlns:xs="http://www.w3.org/2001/XMLSchema"
        xmlns="http://www.w3.org/1999/xhtml">
    <xsl:output method="html" version="4.0" encoding="UTF-8" indent="no" omit-xml-declaration="yes"/>
    <xsl:template match="/">
    	<xsl:if test="/report/part">
    		<!-- Заголовок -->
	    	<table class="res RepeatableReportTable" noEmpty="true" style="width: 100%; margin-top: 0px;">
	    	<!-- Выводим заголовки столбцов -->
	    	<tr>
	    	<xsl:for-each select="/report/columns/column">
	    		<th><xsl:value-of select="."/></th>
	    	</xsl:for-each>
	    	</tr>
    		<!-- Для каждого элемента в обратном порядке -->
	        <xsl:for-each select="/report/part">
	 			<xsl:sort select="position()" data-type="number" order="descending"/>
	        	<tr>
	        	<td>
	        		<xsl:value-of select="@round"/>
	        	</td>
				<!-- Дата выводится в собственном формате -->
        		<td><xsl:call-template name="formatDateTime">
               		<xsl:with-param name="dateTime" select="@timestamp" />
        		</xsl:call-template></td>
	        	<td>
	        		<xsl:value-of select="@action"/>
	        	</td>
	        	<td>
	        		<xsl:value-of select="@fact-user"/>
	        	</td>
				<!-- В тексте отчёта к переводам строки добавляются <br/> -->
				<td><xsl:call-template name="multiLineText">
					<xsl:with-param name="pText" select="."/>
				</xsl:call-template></td>
				</tr>
			</xsl:for-each>
			</table>
		</xsl:if>
    </xsl:template>
    
    <!-- Перевод формата даты из "yyyy-MM-dd'T'HH:mm:ss" в "dd.MM.yy HH:mm:ss" -->
	<xsl:template name="formatDateTime">
        <xsl:param name="dateTime" />
        <xsl:variable name="date" select="substring-before($dateTime, 'T')" />
        <xsl:variable name="year" select="substring-before($date, '-')" />
        <xsl:variable name="month-day" select="substring-after($date, '-')" />
        <xsl:variable name="month" select="substring-before($month-day, '-')" />
        <xsl:variable name="day" select="substring-after($month-day, '-')" />
        <xsl:variable name="time" select="substring-after($dateTime, 'T')" />
        <xsl:value-of select="concat($day, '.', $month, '.', substring($year, 3, 2), ' ', $time)" />
	</xsl:template>

	<!-- Вставка <br/> перед символами переноса строки -->
    <xsl:template name="multiLineText">
        <xsl:param name="pText"/>
        <xsl:choose>
          <xsl:when test="contains($pText, '&#xA;')">
            <xsl:value-of select="substring-before($pText,'&#xA;')"/>
            <br/>
            <xsl:call-template name="multiLineText">
              <xsl:with-param name="pText" 
                   select="substring-after($pText,'&#xA;')"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$pText"/>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>