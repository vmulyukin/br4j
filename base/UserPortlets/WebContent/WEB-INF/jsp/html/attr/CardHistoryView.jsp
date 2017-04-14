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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>


<%@taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<fmt:setBundle basename="com.aplana.dbmi.gui.nl.CardHistoryResource"/>

<portlet:defineObjects/>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery.dataTables.min.css"/>
<script src="${pageContext.request.contextPath}/js/jquery.dataTables.min.js"></script>

<div id="historyContainer" style="max-height: 400px; width:100%; overflow: auto;">
    <table id="historyGrid" class="display" cellspacing="0" width="100%">
        <thead>
        <tr>
            <th style="width: 1%;"></th>
            <th style="width: 14%;"><fmt:message key="history.table.date"/></th>
            <th style="width: 19%;"><fmt:message key="history.table.user"/></th>
            <th style="width: 24%;"><fmt:message key="history.table.description"/></th>
            <th style="width: 42%;"><fmt:message key="history.table.exinf"/></th>
        </tr>
        </thead>
        <tbody>
        </tbody>
    </table>
</div>

<script>
    var curLimit = 1000;
    var curOffset = 0;
    var allowLoad = false;
    var index = 0;

    function getHistory() {
        var recs = [];
        allowLoad = false;
        dojo.xhrGet({
            url: "/DBMI-UserPortlets/CardHistoryServlet",
            content: {
                cardId: ${cardIdForHistory},
                limit:  curLimit,
                offset: curOffset
            },
            sync: true,
            handleAs: 'json',
            load: function(data) {
                recs = data;
                if (data.length > 0) {
                    if (curOffset == 0) {
                        jQuery('#historyGrid').find('.dataTables_empty').parent().remove();
                    }
                    curOffset += curLimit;
                    if (data.length == curLimit) {
                        allowLoad = true;
                    }
                }
            },
            error: function(error) {
                console.error(error);
            }
        });
        return recs;
    }

    function loadHistory() {
        var recs = getHistory();
        var t = jQuery('#historyGrid').DataTable();

        recs.forEach(function(rec) {
            t.row.add( [
                ++index,
                rec.date,
                rec.actorFullName,
                rec.link,
                rec.comment
            ] );
        });

        t.draw();
    }

    dojo.addOnLoad(function() {
        jQuery('#historyGrid').DataTable({
            "paging": false,
            "ordering": false,
            "info": false,
            "filter": false
        });

        loadHistory();
    });

    jQuery('#historyContainer').scroll(function(event) {
        if (!allowLoad) {
            return;
        }

        var div = jQuery(event.target);
        var divTop = div.scrollTop();
        var divHeight = div.height();
        var tableHeight = jQuery('#historyGrid_wrapper').height();
        var scrollTrigger = 0.90;

        if  ((divTop/(tableHeight-divHeight)) > scrollTrigger) {
            loadHistory();
        }
    });
</script>