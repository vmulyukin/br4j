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

import com.aplana.dbmi.model.ObjectId;

/**
 * Extends {@link Action} interface with identifier, specifying some object in database.
 * Should be implemented by all action classes which are used for processing of
 * single object in database.<br>
 * If given 
 * This interface must be implemented to use access checkers.
 */
public interface ObjectAction<ResultType> extends Action<ResultType>
{
	/**
	 * Gets identifier of object to be processed by this Action
	 * @return identifier of object to be processed by this Action
	 */
	public ObjectId getObjectId();
}
