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
<#assign system="Boss Referent"/>
<#assign email="br4j@it.ru"/>

<#macro header title>
<html>
<head></head>
<body>
<p><b>${title}</b> &mdash; Уведомление Системы "${system}"</p>
</#macro>
<#macro footer>
<p>Сообщение отправлено Системой автоматически.<br>Администратор Системы<br>${email}</p>
</body>
</html>
</#macro>
<#macro link card attrId="NAME" attrType="StringAttribute" noValue="(No value)">
<#local attr = card.getAttributeById(util.makeId(attrType, attrId))/>
<#if attr.stringValue?length &gt; 0>
	<#local text = attr.stringValue/>
<#else>
	<#local text = noValue/>
</#if>
<a href="${util.makeUrl(card.id)}">${text?html}</a>
</#macro>
