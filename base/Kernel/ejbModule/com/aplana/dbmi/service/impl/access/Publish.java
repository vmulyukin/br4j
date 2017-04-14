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
package com.aplana.dbmi.service.impl.access;

import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.AccessCheckerBase;

/**
 * For now this access checker is used for PublishCard action only.
 * (to set visiblility of links in Links portlet).
 */
public class Publish extends AccessCheckerBase {
	public boolean checkAccess() throws DataException
	{
		CardState state = (CardState) getObject();
		if (CardState.FOR_APPROVAL.equals(state))
			return hasRole(Role.SPECIALIST);
		return hasRole(Role.EDITOR) || hasRole(Role.ADMINISTRATOR);
	}
}
