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
 *         Time: 15:41
 */
public interface OtherParameters
{
    interface Dmsi extends ru.datateh.jbr.iuh.parameters.other.Dmsi {}
    interface Gost extends ru.datateh.jbr.iuh.parameters.other.Gost {}
    interface MaterialSync extends ru.datateh.jbr.iuh.parameters.other.MaterialSync {}
    interface Medo extends ru.datateh.jbr.iuh.parameters.other.Medo {}
    interface OpenOffice extends ru.datateh.jbr.iuh.parameters.common.OpenOffice {}
    interface Owriter extends ru.datateh.jbr.iuh.parameters.other.Owriter {}
    interface Replication extends ru.datateh.jbr.iuh.parameters.other.Replication {}
    interface Soz extends ru.datateh.jbr.iuh.parameters.other.Soz {}
    interface ScriptExecutionSkip extends ru.datateh.jbr.iuh.parameters.other.ScriptExecutionSkip {}
}
