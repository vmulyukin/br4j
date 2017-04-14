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
/**
 * 
 */
package com.aplana.dbmi.service.impl.query;

import java.sql.Types;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.action.ireferent.GetParents;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * @author PPanichev
 *
 * Query used to perform {@link GetParents} action.
 */
public class GetListParents extends ActionQueryBase {
    
    public GetListParents() {
	
    }
    
    public GetParents getGetParents() {
	return (GetParents) getAction();
    }

    /* (non-Javadoc)
     * @see com.aplana.dbmi.service.impl.QueryBase#processQuery()
     */
    @Override
    public Object processQuery() throws DataException {
	final GetParents parents = getGetParents();
	final Set<ObjectId> templates = parents.getTemplates();
	final ObjectId cardState = parents.getCardStateId();
	final ObjectId overhead = parents.getOverheadCardLinkId();
	
	final List<Long> ids = getJdbcTemplate().queryForList("SELECT c.card_id \n" +
	"FROM card c \n" +
	"WHERE c.status_id = ? and c.template_id in (" +
	ObjectIdUtils.numericIdsToCommaDelimitedString(templates) +
	") and not exists( \n" +
	"select 1 from attribute_value av \n" +
	"where av.card_id=c.card_id and av.attribute_code = ? " + 
	")",
	new Object[]{ cardState.getId(), overhead.getId() },
	new int[]{Types.NUMERIC, Types.VARCHAR},
	Long.class);

	return ids;
    }
    
}
