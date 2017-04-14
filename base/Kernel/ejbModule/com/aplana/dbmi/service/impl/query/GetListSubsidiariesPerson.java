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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.ireferent.GetLayerSubsidiariesPerson;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * @author PPanichev
 *
 */
public class GetListSubsidiariesPerson extends ActionQueryBase {
    
    public GetListSubsidiariesPerson() {
	
    }
    
    public GetLayerSubsidiariesPerson getGetListSubsidiariesPerson() {
	return (GetLayerSubsidiariesPerson) getAction();
    }

    /* (non-Javadoc)
     * @see com.aplana.dbmi.service.impl.QueryBase#processQuery()
     */
    @Override
    public Object processQuery() throws DataException {
	final GetLayerSubsidiariesPerson subsidiariesPerson = getGetListSubsidiariesPerson();
	final Set<ObjectId> templatesOrg = subsidiariesPerson.getTemplatesOrganization();
	final Set<ObjectId> cardlinksOrg = subsidiariesPerson.getCardlinksOrganization();
	final String parents = subsidiariesPerson.getParents();
	final ObjectId templatePers = subsidiariesPerson.getTemplatePerson();
	final ObjectId overhead = subsidiariesPerson.getOverheadCardLinkId();
	
	final Map<Long, Long> ids = new HashMap<Long, Long>();//(Map<Long, Long>)
	getJdbcTemplate().query(
		"select av1.card_id as person_card, av.card_id as org_card from attribute_value av " + // , av.number_value as card_parent
		"inner join " +
		"attribute_value av1 " +
		        "on av1.attribute_code in (" +
		        SetStringToCommaDelimitedString(cardlinksOrg) +
		        ") and av1.number_value=av.card_id " +
		        "and av.number_value in (" + parents + ") " +
		        "and av.attribute_code=? " +
		        "and av1.card_id in ( " +
				"select c.card_id from card c " +
					"where av1.card_id = c.card_id " +
					"and c.template_id = ? )" +
				"and av.card_id in ( " +
				"select c.card_id from card c " +
					"where av.card_id = c.card_id " +
					"and c.template_id in ( " +
		        //"and av1.template_id =? " +
		        //"and av.template_id in (" +
		        ObjectIdUtils.numericIdsToCommaDelimitedString(templatesOrg)+
		        "))",
			    new Object[] {overhead.getId(), templatePers.getId()},
			    new int[] {Types.VARCHAR, Types.NUMERIC}, 
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
    
    private String SetStringToCommaDelimitedString(Collection ids) {
	if (ids == null || ids.isEmpty())
		return "";
	final StringBuffer result = new StringBuffer();
	for ( Iterator i = ids.iterator(); i.hasNext(); ) 
	{
		final Object obj = i.next();
		if (obj == null) continue;
		result.append("'" + ((ObjectId)obj).getId().toString() + "'");
		if (i.hasNext())
			result.append(',');
	}
	return result.toString();
}

}
