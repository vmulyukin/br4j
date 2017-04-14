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
package com.aplana.dbmi.service.impl.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.RowCallbackHandler;

import com.aplana.dbmi.model.AttributeViewParam;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardVersion;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.filter.CardViewModeFilter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.access.CardAccessUtils;
import com.aplana.dbmi.utils.SimpleDBUtils;

/**
 * Query used to fetch display parameters for {@link Card} attributes.<br>
 * Returns collection of {@link AttributeViewParam} objects defining display parameters
 * for card attributes in GUI.<br>
 * <ul>Could be used as fetch-child query for following parent types:
 * <li>{@link Template} - returns display parameters for newly created by given template card;</li>
 * <li>{@link Card} - returns display parameters for given {@link Card};</li>
 * <li>{@link CardVersion} - returns display parameters for {@link CardVersion previous version}
 * of card</li>
 * </ul>
 */
public class ListAttributeViewParameters extends ChildrenQueryBase {

	private static final long serialVersionUID = 1L;

	/**
	 * Returns collection of {@link AttributeViewParam} objects defining display parameters
	 * for card attributes in GUI<br>
	 * @return collection of {@link AttributeViewParam} objects defining display parameters
	 * for card attributes in GUI.
	 */
	public Object processQuery() throws DataException {
		final Map<String, AttributeViewParam> attrMap = new HashMap<String, AttributeViewParam>();
		CardViewModeFilter filter = (CardViewModeFilter) getFilter();
		RowCallbackHandler rh = new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				String attrCode = rs.getString(1);
				ObjectId id = new ObjectId(AttributeViewParam.class, attrCode);
				AttributeViewParam res = (AttributeViewParam)DataObject.createFromId(id);
				res.setAttribute(attrCode);
				res.setMandatory(rs.getInt(2) == 1);
				res.setHidden(rs.getInt(3) == 1);
				res.setReadOnly(rs.getInt(4) == 1);
				attrMap.put(attrCode, res);
			}
		}; 
		
		// query for default attribute view parameters
		String defaultSql 
			= "select ta.attribute_code, ta.is_mandatory, ta.is_hidden, ta.is_readonly from template_attribute ta where ";
		Object[] defaultParams;
		int[] defaultTypes;
		
		// query for attribute view parameters specific for given card status and user
		String sql = 
			"select ta.attribute_code" +
			", min(avp.is_mandatory) as is_mandatory" +
			", min(avp.is_hidden) as is_hidden" +
			", min(avp.is_readonly) as is_readonly " +
			"from template_attribute ta, attribute_view_param avp " +
			"where ta.template_attr_id = avp.template_attr_id and ";

		Object[] params;
		int[] types;
		
		final ObjectId userId = getUser().getPerson().getId();
		
		if (Template.class.equals(getParent().getType())) {
			final Long templateId = (Long)getParent().getId();
			// For newly created cards
			defaultSql += "ta.template_id = ?";
			defaultParams = new Object[] { templateId };
			defaultTypes = new int[] { Types.NUMERIC };
			
			sql += "ta.template_id = ?" +
			" and exists (" +
			"  select 1 from workflow w, template t where" +
			"   t.template_id = ta.template_id" +			
			"   and t.workflow_id = w.workflow_id" +
			"   and w.initial_status_id = avp.status_id" +
			" )" +
			" and (avp.role_code is null or exists (" +
			"  select 1 from person_role pr where" +
			"   pr.person_id = ?" +
			"   and pr.role_code = avp.role_code" +
			" ))" +
			" and avp.person_attribute_code is null";
			params = new Object[] { templateId, userId.getId() };
			types = new int[] {Types.NUMERIC, Types.NUMERIC};
		} else if (Card.class.equals(getParent().getType())){
			final Long cardId = (Long)getParent().getId();
			// For existent cards
			defaultSql += "exists (select 1 from card c where c.card_id = ? and c.template_id = ta.template_id)";
			defaultParams = new Object[] { cardId };
			defaultTypes = new int[] { Types.NUMERIC };
			sql += "exists (" +
			"  select 1 from card c where" +
			"   c.card_id = ?" +
			"   and c.status_id = avp.status_id" +
			"   and c.template_id = ta.template_id)";

			final Set userRoles	= CardAccessUtils.getPersonRoles4Card( userId, getParent(), getJdbcTemplate());			
			if (userRoles == null || userRoles.isEmpty()) {
				sql += " and avp.role_code is null";
			} else {
				sql += " and (avp.role_code is null or avp.role_code in (" + SimpleDBUtils.stringIdentifiersToCommaSeparatedSqlString(userRoles) + "))";
			}
			
			final Set userPersonAttributes = CardAccessUtils.getUserPersonAttributesForCard(userId, getParent(), getJdbcTemplate());
			if (userPersonAttributes.isEmpty()) {
				sql += " and avp.person_attribute_code is null";
			} else {
				sql += " and (avp.person_attribute_code is null or avp.person_attribute_code in (" + SimpleDBUtils.stringIdentifiersToCommaSeparatedSqlString(userPersonAttributes) + "))";
			}
			params = new Object[] { cardId };
			types = new int[] {Types.NUMERIC };

		} else if (CardVersion.class.equals(getParent().getType())) {
			// For previous card versions
			CardVersion.CompositeId compositeId = (CardVersion.CompositeId)getParent().getId();

			final Long cardId = new Long(compositeId.getCard());
			final Long cardVersionId = new Long(compositeId.getVersion());
			
			defaultSql += "exists (select 1 from card c where c.card_id = ? and c.template_id = ta.template_id)";
			defaultParams = new Object[] { cardId };
			defaultTypes = new int[] { Types.NUMERIC };		

			sql += "exists (" +
			"  select 1 from card_version cv, card c where" +
			"   c.card_id = ?" +
			"   and cv.card_id = c.card_id" +
			"   and cv.version_id = ?" +
			"   and cv.status_id = avp.status_id" +
			"   and c.template_id = ta.template_id)";

			final ObjectId cardObjectId = new ObjectId(Card.class, cardId);
			final Set userRoles = CardAccessUtils.getPersonRoles4Card( userId, cardObjectId, getJdbcTemplate());			
			if (userRoles == null || userRoles.isEmpty()) {
				sql += " and avp.role_code is null";
			} else {
				sql += " and ((avp.role_code is null) or (avp.role_code in (" + SimpleDBUtils.stringIdentifiersToCommaSeparatedSqlString(userRoles) + ")))";
			}

			final Set userPersonAttributes = CardAccessUtils.getUserPersonAttributesForCard(userId, cardObjectId, getJdbcTemplate());
			if (userPersonAttributes.isEmpty()) {
				sql += " and avp.person_attribute_code is null";
			} else {
				sql += " and (avp.person_attribute_code is null or avp.person_attribute_code in (" + SimpleDBUtils.stringIdentifiersToCommaSeparatedSqlString(userPersonAttributes) + "))";
			}
			
			params = new Object[] { 
				cardId,
				cardVersionId 
			};
			types = new int[] {Types.NUMERIC, Types.NUMERIC}; 
		} else {
			return null;
		}
		String viewModeSql = null;
		if (filter != null && filter.getViewMode() != null) {
			viewModeSql = sql + " and avp.view_code = '" + filter.getViewMode().getId() + "' group by ta.attribute_code";
		}
		sql += " and avp.view_code IS NULL group by ta.attribute_code";

		final long too_much_ms = 2000;
		long start_ms = System.currentTimeMillis();
		getJdbcTemplate().query(defaultSql, defaultParams, defaultTypes, rh);
		long duration_ms = System.currentTimeMillis() - start_ms;
		if (duration_ms > too_much_ms)
			logger.warn("possibly this SQL need optimization (run in "
					+ duration_ms +"ms): \n"+ defaultSql);

		start_ms = System.currentTimeMillis();
		getJdbcTemplate().query(sql, params, types, rh);
		duration_ms = System.currentTimeMillis() - start_ms;
		if (duration_ms > too_much_ms)
			logger.warn("possibly this SQL need optimization (run in "
					+ duration_ms +"ms): \n"+ sql);

		if (null != viewModeSql) {
			start_ms = System.currentTimeMillis();
			getJdbcTemplate().query(viewModeSql, params, types, rh);
			duration_ms = System.currentTimeMillis() - start_ms;
			if (duration_ms > too_much_ms)
				logger.warn("possibly this SQL need optimization (run in "
						+ duration_ms +"ms): \n"+ viewModeSql);
		}

		return new ArrayList<AttributeViewParam>(attrMap.values());
	}
	
	@Override
	protected boolean supportsFilter(Class<?> type) {
		return CardViewModeFilter.class.equals(type);
	}
}
