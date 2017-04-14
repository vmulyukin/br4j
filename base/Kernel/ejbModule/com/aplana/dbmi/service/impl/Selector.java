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
package com.aplana.dbmi.service.impl;

//import com.aplana.dbmi.service.DataException;

/**
 * Interface defining a conditional clause in {@link QueryFactory} config file.
 * Could be used to define specific configuration parameters for query processing given
 * object if this object satisfies some condition.
 * @see BasePropertySelector
 */
public interface Selector
{
	/**
	 * Checks if given object satisfies the condition represented by this Selector
	 * @param object Object to be checked
	 * @return true if object satisfies the condition represented by this Selector,
	 * false otherwise
	 */
	public boolean satisfies(Object object) /*throws DataException*/;
	
}
