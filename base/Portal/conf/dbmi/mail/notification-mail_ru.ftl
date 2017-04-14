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
<html>
<head>
<style type="text/css">
th, td {
border-bottom: 1px solid gray;
}
</style>
</head>
<body>
<p><b>Обновление информации</b> &mdash; Уведомление Системы "Корпоративная база маркетинговой информации МТС"</p>
<p>С момента последнего уведомления следующая информация в Базе маркетинговой информации была изменена/обновлена:<br>
<table cellspacing="0" cellpadding="5" style="border-collapse: collapse;">
<tr>
<th>ID</th>
<th>Название</th>
<th>Описание</th>
<th>Дата изменения</th>
</tr>
<#list cards as card>
<tr>
<td>${card.id.id?c}</td>
<td><a href="${util.makeUrl(card.id)}">${(card.getAttributeById(util.makeId('StringAttribute', 'NAME')).stringValue)!"(Без названия)"}</a></td>
<td>${(card.getAttributeById(util.makeId('TextAttribute', 'DESCR')).stringValue)!"(Без описания)"}</td>
<td>${(card.getAttributeById(util.makeId('TextAttribute', 'CHANGED')).stringValue)!"(Не изменялось)"}</td>
</tr>
</#list>
</table>
</p>
<p>Сообщение отправлено Системой автоматически.<br>Администратор Системы<br>marketinginfoadmin@mts.ru</p>
</body>
</html>
