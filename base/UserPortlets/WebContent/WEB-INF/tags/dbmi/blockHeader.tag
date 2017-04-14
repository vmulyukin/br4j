<%--

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

--%>
<%-- 
	Отображает заголовок блока характеристик.
	Рядом с заголовком отображается кнопка, позволяющая сворачивать и разворачивать блок.
	Для свертывания/развертывания блока используется javascript-функция submitForm_Collapse(id),
	принимающая аргументом идентификатор блока.
	Текст, отображаемый в заголовке блока, может передаваться через параметр title либо
	в качестве содержимого тега.
	
	Параметры тега:
	id - Идентификатор блока
	title - Заголовок блока; игнорируется, если тег непустой
	displayed - true, если блок развернут
  --%>
<%@tag body-content="scriptless" import="com.aplana.dbmi.card.CardPortlet"%>
<%@attribute name="id" required="true" rtexprvalue="true" description="Идентификатор блока"%>
<%@attribute name="title" required="false" rtexprvalue="true" description="Заголовок блока"%>
<%@attribute name="displayed" type="java.lang.Boolean" required="false" rtexprvalue="true" description="true, если блок развернут"%>
<%@attribute name="savestate" type="java.lang.Boolean" required="true"  rtexprvalue="true" description="true, если нужно запоминать состояние блока"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<table class="partitionContainer">
  <col width="*" />
  <col width="32px" align="right" />
  <tbody>
      <tr>
         <td>
            <table class="partition">
               <tr>
                  <td class="partition_left"/>

                  <td class="partition_middle">
                     <c:if test="${empty title}"><jsp:doBody /></c:if>
                     <c:if test="${!empty title}">${title}</c:if>
                  </td>

                  <td class="partition_right"/>

               </tr>
            </table>
         </td>

         <td>
            <A HREF="javascript:block_collapse('${id}', ${savestate});"
            	class="noLine">
               <c:if test="${displayed}">
                  <span class="arrow" id="<%="ARROW_BLOCK_"%>${id}">&nbsp;</span>
               </c:if>
            
               <c:if test="${!displayed}">
            	   <span class="arrow_up" id="<%="ARROW_BLOCK_"%>${id}">&nbsp;</span>
               </c:if>
            </A>
         </td>

      </tr>
  </tbody>
</table>
