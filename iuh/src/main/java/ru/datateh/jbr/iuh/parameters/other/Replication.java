/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to you under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package ru.datateh.jbr.iuh.parameters.other;

/**
 * @author etarakanov
 *         Date: 16.04.2015
 *         Time: 15:33
 */
public interface Replication
{
    /**
     * Здесь указывается уникальный идентификатор данной СЭД (узла Репликации)
     */
    String SERVER_GUID = "br4j.dbmi.replication.ServerGUID";
    /**
     * Директория, откуда модуль Репликация забирает пакеты для загрузки.
     */
    String INCOMING_FOLDER = "br4j.dbmi.replication.IncomingFolder";
    /**
     * Директория, куда модуль Репликация будет выгружать пакеты для отправки.
     */
    String OUTGOING_FOLDER = "br4j.dbmi.replication.OutgoingFolder";
    /**
     * Здесь указываются уникальные идентификаторы организаций, базирующихся на данной СЭД.
     */
    String REPLICATION_MEMBER_GUID = "br4j.dbmi.replication.GUID";
}
