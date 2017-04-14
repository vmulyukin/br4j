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
<head></head>
<body>
<p>Notification of the System "Corporate Marketing Information Database MTS"</p>
<p>The following tasks' time is up:<br>
<table>
<tr>
<th>ID</th>
<th>Card Name</th>
<th>Description</th>
<th>Customer</th>
<th>Expiration Date</th>
</tr>
<#list cards as card>
<tr>
<td>${card.id.id?c}</td>
<td><a href="${util.makeUrl(card.id)}">${(card.getAttributeById(util.makeId('StringAttribute', 'NAME')).stringValue)!"(No name)"}</a></td>
<td>${(card.getAttributeById(util.makeId('TextAttribute', 'DESCR')).stringValue)!"(No description)"}</td>
<td>${(card.getAttributeById(util.makeId('PersonAttribute', "request.customer")).stringValue)!"(Unknown)"}</td>
<td>${(card.getAttributeById(util.makeId('DateAttribute', "request.estimated")).stringValue)!"(Not set)"}</td>
</tr>
</#list>
</table>
</p>
<p>Do not reply to this letter.<br>
Administrator of the System<br>marketinginfoadmin@mts.ru</p>
</body>
</html>
