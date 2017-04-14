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
package com.aplana.dbmi.access.delegate;

import java.util.Iterator;

import com.aplana.dbmi.action.SaveDelegatesAction;
//import com.aplana.dbmi.model.PermissionDelegate;
import com.aplana.dbmi.model.Delegation;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.query.WriteQuery;

/**
 * ���������� ����� ������ ���������.
 * @author RAbdullin
 *
 */
public class SaveDelegates extends ActionQueryBase implements WriteQuery {

	public Object processQuery() throws DataException {

		final SaveDelegatesAction action = (SaveDelegatesAction) super.getAction(); 
		if ( action == null || action.getDelegates() == null) 
			return null;

		// TODO: ������������ BatchInsert
		SaveQueryBase query = null;
		for (Iterator iterator = action.getDelegates().iterator(); iterator.hasNext();) {
			final Delegation item = (Delegation) iterator.next();
			if (query == null)
				query = getQueryFactory().getSaveQuery(item);
			query.setObject(item);
			ObjectId id = (ObjectId) getDatabase().executeQuery( getUser(), query);
            if(item.getId() == null || item.getId().getId().equals(0)) {
                item.setId(id);
            }
		}

		return null;
	}

}
