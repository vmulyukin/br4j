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
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
      xmlns:xdms="http://www.infpres.com/IEDMS"       
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"       
      xsi:schemaLocation="http://www.infpres.com/IEDMS file:///w:/Doc/IEDMS/IEDMS.xsd"
      exclude-result-prefixes="xdms xsi">
 
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <card>
             <xsl:apply-templates select="//xdms:header"/>
             <xsl:apply-templates select="//xdms:document"/>
             <xsl:apply-templates select="//xdms:files"/>
             <xsl:apply-templates select="//xdms:posting"/>
        </card>
    </xsl:template>


   <xsl:template match="xdms:header">
        <xsl:apply-templates select="xdms:source"/>    	         
   </xsl:template>

   <xsl:template match="xdms:source">
        <xsl:apply-templates select="xdms:organization"/> 
        <xsl:apply-templates select="xdms:system"/> 
        <xsl:apply-templates select="xdms:details"/> 
   </xsl:template>

   <xsl:template match="xdms:source/xdms:organization">
        <xsl:apply-templates select="xdms:organization"/> 
        <xsl:apply-templates select="xdms:region"/> 
   </xsl:template>


   <xsl:template match="xdms:source/xdms:organization/xdms:organization">
        <attribute type="string" name="Организация-отправитель\название">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:source/xdms:organization/xdms:region">
        <attribute type="string" name="Организация-отправитель\регион">
           <value>
              <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>


   <xsl:template match="xdms:system">
        <attribute type="string" name="Источник\система">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:details">
        <attribute type="string" name="Источник\детали">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>



<!-- ===========================Документ==================================== -->

   <xsl:template match="xdms:document">
        <xsl:apply-templates select="xdms:kind"/>
        <xsl:apply-templates select="xdms:num"/>
        <xsl:apply-templates select="xdms:signedBy"/>
        <xsl:apply-templates select="xdms:signed"/>
        <xsl:apply-templates select="xdms:addresses"/>
        <xsl:apply-templates select="xdms:pages"/>
        <xsl:apply-templates select="xdms:enclosuresPages"/>
        <xsl:apply-templates select="xdms:annotation"/>
        <xsl:apply-templates select="xdms:correspondents"/>
        <xsl:apply-templates select="xdms:links"/>
        <xsl:apply-templates select="xdms:clauses"/>
   </xsl:template>

   <xsl:template match="xdms:kind">
        <attribute type="string" name="Документ\тип">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>  
   </xsl:template>

   <xsl:template match="xdms:num">
        <xsl:apply-templates select="xdms:number"/>
        <xsl:apply-templates select="xdms:date"/>   
   </xsl:template>

   <xsl:template match="xdms:number">
        <attribute type="string" name="Документ\номер\номер">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:date">
        <attribute type="string" name="Документ\номер\дата">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:signedBy">
        <xsl:apply-templates select="xdms:person"/>
        <xsl:apply-templates select="xdms:department"/>   
        <xsl:apply-templates select="xdms:post"/> 
   </xsl:template>

   <xsl:template match="xdms:signedBy/xdms:person">
        <attribute type="string" name="Документ\Подписант\ФИО">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:signedBy/xdms:department">
        <attribute type="string" name="Документ\Подписант\Подразделение">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:signedBy/xdms:post">
        <attribute type="string" name="Документ\Подписант\Должность">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:signed">
        <attribute type="string" name="Документ\Подписан">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>


   <xsl:template match="xdms:addresses">
        <xsl:apply-templates select="xdms:addresse"/>
   </xsl:template>

   <xsl:template match="xdms:addresse">
        <xsl:apply-templates select="xdms:organization"/>
        <xsl:apply-templates select="xdms:person"/>
        <xsl:apply-templates select="xdms:comment"/>
   </xsl:template>

   <xsl:template match="xdms:addresse/xdms:organization">
        <xsl:apply-templates select="xdms:organization"/> 
        <xsl:apply-templates select="xdms:region"/> 
   </xsl:template>


   <xsl:template match="xdms:addresse/xdms:organization/xdms:organization">
        <attribute type="string" name="Организация-получатель\название">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:addresse/xdms:organization/xdms:region">
        <attribute type="string" name="Организация-получатель\регион">
           <value>
              <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:addresse/xdms:person">
        <xsl:apply-templates select="xdms:person"/> 
        <xsl:apply-templates select="xdms:post"/> 
   </xsl:template>

   <xsl:template match="xdms:addresse/xdms:person/xdms:person">
        <attribute type="string" name="Получатель\ФИО">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:addresse/xdms:person/xdms:post">
        <attribute type="string" name="Получатель\Должность">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:addresse/xdms:comment">
        <attribute type="string" name="Получатель\Комментарий">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:pages">
        <attribute type="string" name="Документ\Число страниц">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:enclosuresPages">
        <attribute type="string" name="Документ\Число страниц приложений">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:annotation">
        <attribute type="string" name="Документ\Аннотация">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:correspondents">
        <attribute type="string" name="Документ\Корреспонденты">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:links">
        <attribute type="string" name="Документ\Ссылки">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

   <xsl:template match="xdms:clauses">
        <attribute type="string" name="Документ\Оговорки">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

<!-- ===========================Файлы==================================== -->


   <xsl:template match="xdms:files">
        <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="xdms:file">
        <attribute type="string" name="Файл\Имя">
           <value>
               <xsl:value-of select="@*|*"/>
           </value>
        </attribute>
        <xsl:apply-templates select="xdms:description"/>
   </xsl:template>

   <xsl:template match="xdms:description">
        <attribute type="string" name="Файл\Описание">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>


<!-- ===========================Постинг==================================== -->

   <xsl:template match="xdms:posting">
        <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="xdms:posting/xdms:destination">
        <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="xdms:posting/xdms:destination/xdms:destination">
        <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="xdms:posting/xdms:destination/xdms:destination/xdms:organization">
        <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="xdms:posting/xdms:destination/xdms:destination/xdms:organization/xdms:organization">
        <attribute type="string" name="Постинг\Организация">
           <value>
               <xsl:apply-templates />
           </value>
        </attribute>
   </xsl:template>

</xsl:stylesheet>

