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
<#assign importedDoc = util.getLinkedCard(doc, "jbr.original.source")!/>
<#assign answer = util.getAnswerString(doc, true)/>
<#assign docInfo = "№ ${(doc.getAttributeById(util.makeId('StringAttribute', 'jbr.incoming.outnumber')).stringValue)!} от ${(doc.getAttributeById(util.makeId('DateAttribute', 'jbr.incoming.outdate')).stringValue)!}"/>

<#switch msgType>
	<#case "DOCUMENT">№ ${regNumber!} от ${(regDate?date)!} ${answer}
		<#break>
	<#case "ACKNOWLEDGEMENT">
		<#if ackType = "Refused">
			Refusal ${docInfo}
		<#else><#if ackType = "Recieved">
			Документ ${docInfo} загружен
		<#else><#if ackType = "NotRecieved">
			Загрузка документа провалена
		<#else><#if ackType = "Registered">
			Receipt ${docInfo}
		</#if>
		</#if>
		</#if>
		</#if>
		<#break>
	<#default>
</#switch>
</#compress>