<#--

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
<#compress>
<#include "common.ftl">
<@header/>
<#assign answer = util.getAnswerString(doc)/>
<#assign docInfo = "№ ${(doc.getAttributeById(util.makeId('StringAttribute', 'jbr.incoming.outnumber')).stringValue)!} от ${(doc.getAttributeById(util.makeId('DateAttribute', 'jbr.incoming.outdate')).stringValue)!}"/>

<#switch msgType>
	<#case "DOCUMENT">Вам направлен документ <@link card=doc attrId="NAME"/> (ID ${doc.id.id}). ${answer}
		<#break>
	<#case "ACKNOWLEDGEMENT">
		<#if ackType = "Refused">
			<p>Ваш документ ${docInfo} не был зарегистрирован. Причина отказа: ${errorMessage!}</p>
		<#else><#if ackType = "Recieved">
			<p>Ваш документ ${docInfo} был успешно загружен.</p>
		<#else><#if ackType = "NotRecieved">
			<p>Загрузка документа провалена по причине: ${errorMessage!}</p>
		<#else><#if ackType = "Registered">
			<p>Ваш документ ${docInfo} зарегистрирован за № ${ackRegNumber!} от ${(ackRegDate?date)!}</p>
		</#if>
		</#if>
		</#if>
		</#if>
		<#break>
	<#default>
</#switch>
<@footer/>
</#compress>