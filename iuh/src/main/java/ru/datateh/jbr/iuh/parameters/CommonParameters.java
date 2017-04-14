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
package ru.datateh.jbr.iuh.parameters;

/**
 * @author etarakanov
 *         Date: 16.04.2015
 *         Time: 15:12
 */
public interface CommonParameters
{
    interface Iuh extends ru.datateh.jbr.iuh.parameters.common.Iuh {}
    interface System extends ru.datateh.jbr.iuh.parameters.common.System {}
    interface OpenOffice extends ru.datateh.jbr.iuh.parameters.common.OpenOffice {}
    interface DataSources extends ru.datateh.jbr.iuh.parameters.common.DataSources {}
    interface Solr extends ru.datateh.jbr.iuh.parameters.common.Solr {}
}
