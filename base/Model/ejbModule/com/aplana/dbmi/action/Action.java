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
package com.aplana.dbmi.action;

import java.io.Serializable;

/**
 * This interface is used to represent some abstract action performed in DBMI system.
 * Each action is processed by {@link com.aplana.dbmi.service.DataService} EJB.
 * All information required to perform action should be defined in custom action object,
 * implementing this interface. 
 * 
 * Generic type T is the class of the returned object by executing this action.
 */
public interface Action<ResultType> extends Serializable
{
	/**
	 * Gets type of action's result. Could be null if action doesn't return any value
	 * @return type of action's result
	 */
	public Class<?> getResultType();
}
