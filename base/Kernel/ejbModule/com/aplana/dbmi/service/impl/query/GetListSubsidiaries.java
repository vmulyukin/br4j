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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.ireferent.GetLayerSubsidiaries;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * @author PPanichev
 *
 */
public class GetListSubsidiaries extends ActionQueryBase {
    
    public GetListSubsidiaries() {
	
    }
    
    public GetLayerSubsidiaries getGetLayerSubsidiaries() {
	return (GetLayerSubsidiaries) getAction();
    }

    /* (non-Javadoc)
     * @see com.aplana.dbmi.service.impl.QueryBase#processQuery()
     */
    @Override
    public Object processQuery() throws DataException {
	final GetLayerSubsidiaries subsidiariesParenting = getGetLayerSubsidiaries();
	final Set<ObjectId> templates = subsidiariesParenting.getTemplates();
	final String parents = subsidiariesParenting.getParents();
	final ObjectId overhead = subsidiariesParenting.getOverheadCardLinkId();
	
	final Map<Long, Long> ids = new HashMap<Long, Long>();//(Map<Long, Long>)
	getJdbcTemplate().query("select av.card_id, av.number_value as card_parent from attribute_value av \n"
			    + "where av.number_value in ("
			    + parents
			    + ") and av.attribute_code=? " 
			    //+ "and av.template_id in ("
			    + "and av.card_id in ( " 
				+ "select c.card_id from card c "
					+ "where av.card_id = c.card_id "
					+ "and c.template_id in ( "
			    + ObjectIdUtils.numericIdsToCommaDelimitedString(templates)
			    + "))",
		    new Object[] { overhead.getId()},
		    new int[] { Types.VARCHAR},
		    new RowMapper() {
		        public Object mapRow( ResultSet rs, int rowNum) throws SQLException 
		        {
		            final Long name = rs.getLong(1);
		            final Long value = rs.getLong(2);
		            ids.put( name, value);
		            return null;
		        }
		     }
	);
	return ids;
    }

}
