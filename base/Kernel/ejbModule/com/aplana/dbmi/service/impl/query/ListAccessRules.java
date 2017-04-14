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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.AccessAttribute;
import com.aplana.dbmi.model.AccessCard;
import com.aplana.dbmi.model.AccessRule;
import com.aplana.dbmi.model.AccessTemplate;
import com.aplana.dbmi.model.AccessWorkflowMove;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.LocalizedString;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ObjectIdAndName;
import com.aplana.dbmi.model.PersonAccessRule;
import com.aplana.dbmi.model.PersonProfileAccessRule;
import com.aplana.dbmi.model.RoleAccessRule;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.filter.access.AccessRuleFilter;
import com.aplana.dbmi.model.filter.access.FilterByTemplate;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.QueryBase;

public class ListAccessRules extends QueryBase {

	protected boolean supportsFilter(Class type) {
		if (AccessRuleFilter.class.isAssignableFrom(type))
			return true;
		return super.supportsFilter(type);
	}

	public Object processQuery() throws DataException {
		AccessRuleFilter filter = (AccessRuleFilter) getFilter();
		ArrayList plugins = new ArrayList();
		if (filter.needOperationType(AccessCard.class))
			plugins.add(new AccessCardPlugin());
		if (filter.needOperationType(AccessTemplate.class))
			plugins.add(new AccessTemplatePlugin());
		if (filter.needOperationType(AccessWorkflowMove.class))
			plugins.add(new AccessMovePlugin());
		if (filter.needOperationType(AccessAttribute.class))
			plugins.add(new AccessAttributePlugin());
		
		if (filter instanceof FilterByTemplate)
			plugins.add(new TemplateFilterPlugin((FilterByTemplate) filter));
		if (filter instanceof AccessRuleIdFilter)
			plugins.add(new IdFilterPlugin(((AccessRuleIdFilter) filter).getTargetId()));
		
		List rules = new ArrayList();
		if (filter.needRuleType(RoleAccessRule.class))
			rules.addAll(listRoleRules(filter, plugins));
		if (filter.needRuleType(PersonAccessRule.class))
			rules.addAll(listPersonRules(filter, plugins));
		if (filter.needRuleType(PersonProfileAccessRule.class))
			rules.addAll(listProfileRules(filter, plugins));
		return rules;
	}

	private List listRoleRules(AccessRuleFilter filter, final List plugins) {
		ArrayList arguments = new ArrayList();
		addAllArguments(arguments, plugins);
		return getJdbcTemplate().query(
				"SELECT r.rule_id, rr.role_code, sr.role_name_rus, sr.role_name_eng" +
					surround(", ", getAllColumns(plugins), " ") +
				" FROM role_access_rule rr " +
					"LEFT JOIN system_role sr ON rr.role_code=sr.role_code " +
				"JOIN access_rule r ON rr.rule_id=r.rule_id " +
					getAllJoins(plugins) +
				surround(" WHERE ", getAllConditions(plugins), ""),
				arguments.toArray(),
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						RoleAccessRule rule = new RoleAccessRule();
						rule.setId(rs.getLong(1));
						rule.setRole(readStringIdAndName(rs, 2, SystemRole.class));
						readAllColumns(rs, 5, rule, plugins);
						return rule;
					}
				});
	}

	private List listPersonRules(AccessRuleFilter filter, final List plugins) {
		ArrayList arguments = new ArrayList();
		addAllArguments(arguments, plugins);
		return getJdbcTemplate().query(
				"SELECT r.rule_id, " +
					"pr.person_attr_code, ap.attr_name_rus, ap.attr_name_eng, ap.data_type, " +		// 2-5
					"pr.link_attr_code, al.attr_name_rus, al.attr_name_eng, al.data_type, " +		// 6-9
					"pr.intermed_attr_code, ai.attr_name_rus, ai.attr_name_eng, ai.data_type, " +	// 10-13
					"pr.linked_status_id, cs.name_rus, cs.name_eng, " +								// 14-16
					"pr.role_code, sr.role_name_rus, sr.role_name_eng" +							// 17-19
					surround(", ", getAllColumns(plugins), " ") +									// 20+
				" FROM person_access_rule pr " +
			    	"LEFT JOIN attribute ap ON pr.person_attr_code=ap.attribute_code " +
			    	"LEFT JOIN attribute al ON pr.link_attr_code=al.attribute_code " +
			    	"LEFT JOIN attribute ai ON pr.intermed_attr_code=ai.attribute_code " +
			    	"LEFT JOIN card_status cs ON pr.linked_status_id=cs.status_id " +
			    	"LEFT JOIN system_role sr ON pr.role_code=sr.role_code " +
				"JOIN access_rule r ON pr.rule_id=r.rule_id " +
					getAllJoins(plugins) +
				surround(" WHERE ", getAllConditions(plugins), ""),
				arguments.toArray(),
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						PersonAccessRule rule = new PersonAccessRule();
						rule.setId(rs.getLong(1));
						rule.setPersonAttribute(readAttributeIdAndName(rs, 2));
						rule.setLinkAttribute(readAttributeIdAndName(rs, 6));
						rule.setIntermediateLinkAttribute(readAttributeIdAndName(rs, 10));
						rule.setLinkedStateId(readNumericIdAndName(rs, 14, CardState.class));
						rule.setRoleId(readStringIdAndName(rs, 17, SystemRole.class));
						readAllColumns(rs, 20, rule, plugins);
						return rule;
					}
				});
	}
	
	private List listProfileRules(AccessRuleFilter filter, final List plugins) {
		ArrayList arguments = new ArrayList();
		addAllArguments(arguments, plugins);
		return getJdbcTemplate().query(
				"SELECT r.rule_id, " +
					"pr.profile_attr_code, ap.attr_name_rus, ap.attr_name_eng, ap.data_type, " +	// 2-5
					"pr.target_attr_code, at.attr_name_rus, at.attr_name_eng, at.data_type, " +		// 6-9
					"pr.link_attr_code, al.attr_name_rus, al.attr_name_eng, al.data_type, " +		// 10-13
					"pr.intermed_attr_code, ai.attr_name_rus, ai.attr_name_eng, ai.data_type, " +	// 14-17
					"pr.linked_status_id, cs.name_rus, cs.name_eng, " +								// 18-20
					"pr.role_code, sr.role_name_rus, sr.role_name_eng" +							// 21-23
					surround(", ", getAllColumns(plugins), " ") +									// 24+
				" FROM profile_access_rule pr " +
		    		"LEFT JOIN attribute ap ON pr.profile_attr_code=ap.attribute_code " +
			    	"LEFT JOIN attribute at ON pr.target_attr_code=at.attribute_code " +
			    	"LEFT JOIN attribute al ON pr.link_attr_code=al.attribute_code " +
			    	"LEFT JOIN attribute ai ON pr.intermed_attr_code=ai.attribute_code " +
			    	"LEFT JOIN card_status cs ON pr.linked_status_id=cs.status_id " +
			    	"LEFT JOIN system_role sr ON pr.role_code=sr.role_code " +
				"JOIN access_rule r ON pr.rule_id=r.rule_id " +
					getAllJoins(plugins) +
				surround(" WHERE ", getAllConditions(plugins), ""),
				arguments.toArray(),
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						PersonProfileAccessRule rule = new PersonProfileAccessRule();
						rule.setId(rs.getLong(1));
						rule.setProfileAttribute(readAttributeIdAndName(rs, 2));
						rule.setTargetAttribute(readAttributeIdAndName(rs, 6));
						rule.setLinkAttribute(readAttributeIdAndName(rs, 10));
						rule.setIntermediateLinkAttribute(readAttributeIdAndName(rs, 14));
						rule.setLinkedStateId(readNumericIdAndName(rs, 18, CardState.class));
						rule.setRoleId(readStringIdAndName(rs, 21, SystemRole.class));
						readAllColumns(rs, 24, rule, plugins);
						return rule;
					}
				});
	}
	
	abstract private class QueryPlugin {
		
		String joins;
		String columns;
		String conditions;
		String condGroup;
		
		int addArguments(List args) {
			return 0;
		}
		int readRow(ResultSet rs, int idxStart, AccessRule rule) throws SQLException {
			return 0;
		}
	}
	
	private static final String OPERATION_TYPE_GROUP = "opType";
	
	private class AccessCardPlugin extends QueryPlugin {
		
		public AccessCardPlugin() {
			joins = "LEFT JOIN access_card_rule cr ON r.rule_id=cr.rule_id " +
					"LEFT JOIN template crt ON r.template_id=crt.template_id " +
					"LEFT JOIN card_status crs ON r.status_id=crs.status_id";
			columns = "cr.rule_id, crt.template_id, crt.template_name_rus, crt.template_name_eng, " +
					"crs.status_id, crs.name_rus, crs.name_eng, cr.operation_code";
			conditions = "cr.rule_id IS NOT NULL";
			condGroup = OPERATION_TYPE_GROUP;
		}

		int readRow(ResultSet rs, int idxStart, AccessRule rule) throws SQLException {
			if (rs.getObject(idxStart) != null) {
				AccessCard operation = new AccessCard();
				operation.setTemplate(readNumericIdAndName(rs, idxStart + 1, Template.class));
				operation.setStatus(readNumericIdAndName(rs, idxStart + 4, CardState.class));
				operation.setOperation(rs.getString(idxStart + 7));
				rule.setAccessOperation(operation);
			}
			return 8;
		}
	}
	
	private class AccessTemplatePlugin extends QueryPlugin {
		
		public AccessTemplatePlugin() {
			joins = "LEFT JOIN access_template_rule tr ON r.rule_id=tr.rule_id " +
					"LEFT JOIN template trt ON r.template_id=trt.template_id";
			columns = "tr.rule_id, trt.template_id, trt.template_name_rus, trt.template_name_eng, tr.operation_code";
			conditions = "tr.rule_id IS NOT NULL";
			condGroup = OPERATION_TYPE_GROUP;
		}

		int readRow(ResultSet rs, int idxStart, AccessRule rule) throws SQLException {
			if (rs.getObject(idxStart) != null) {
				AccessTemplate operation = new AccessTemplate();
				operation.setTemplate(readNumericIdAndName(rs, idxStart + 1, Template.class));
				operation.setOperation(rs.getString(idxStart + 4));
				rule.setAccessOperation(operation);
			}
			return 5;
		}
	}
	
	private class AccessMovePlugin extends QueryPlugin {
		
		public AccessMovePlugin() {
			joins = "LEFT JOIN access_move_rule mr ON r.rule_id=mr.rule_id " +
					"LEFT JOIN workflow_move mrm ON mr.wfm_id=mrm.wfm_id " +
					"LEFT JOIN card_status mrst ON mrm.to_status_id=mrst.status_id " +
					"LEFT JOIN template mrt ON r.template_id=mrt.template_id " +
					"LEFT JOIN card_status mrs ON r.status_id=mrs.status_id";
			columns = "mr.rule_id, mrm.wfm_id, mrm.name_rus, mrm.name_eng, " +
					"mrst.default_move_name_rus, mrst.default_move_name_eng, " +
					"mrt.template_id, mrt.template_name_rus, mrt.template_name_eng, " +
					"mrs.status_id, mrs.name_rus, mrs.name_eng";
			conditions = "mr.rule_id IS NOT NULL";
			condGroup = OPERATION_TYPE_GROUP;
		}
		
		int readRow(ResultSet rs, int idxStart, AccessRule rule) throws SQLException {
			if (rs.getObject(idxStart) != null) {
				AccessWorkflowMove operation = new AccessWorkflowMove();
				operation.setTemplate(readNumericIdAndName(rs, idxStart + 6, Template.class));
				if (rs.getObject(idxStart + 1) != null) {
					WorkflowMove move = (WorkflowMove) WorkflowMove.createFromId(
							readNumericIdAndName(rs, idxStart + 1, WorkflowMove.class));
					move.getDefaultName().setValueRu(rs.getString(idxStart + 4));
					move.getDefaultName().setValueEn(rs.getString(idxStart + 5));
					move.setFromState(readNumericIdAndName(rs, idxStart + 9, CardState.class));
					operation.setMove(move);
				}
				rule.setAccessOperation(operation);
			}
			return 12;
		}
	}
	
	private class AccessAttributePlugin extends QueryPlugin {
		
		public AccessAttributePlugin() {
			joins = "LEFT JOIN access_attr_rule ar ON r.rule_id=ar.rule_id " +
					"LEFT JOIN attribute ara ON ar.attribute_code=ara.attribute_code " +
					"LEFT JOIN template art ON r.template_id=art.template_id " +
					"LEFT JOIN card_status ars ON r.status_id=ars.status_id " +
					"LEFT JOIN workflow_move arm ON ar.wfm_id=arm.wfm_id " +
					"LEFT JOIN card_status arst ON arm.to_status_id=arst.status_id";
			columns = "ar.rule_id, ar.operation_code, " +
					"ara.attribute_code, ara.attr_name_rus, ara.attr_name_eng, ara.data_type, " +
					"art.template_id, art.template_name_rus, art.template_name_eng, " +
					"ars.status_id, ars.name_rus, ars.name_eng, " +
					"arm.wfm_id, arm.name_rus, arm.name_eng, " +
					"arst.default_move_name_rus, arst.default_move_name_eng";
			conditions = "ar.rule_id IS NOT NULL";
			condGroup = OPERATION_TYPE_GROUP;
		}

		int readRow(ResultSet rs, int idxStart, AccessRule rule) throws SQLException {
			if (rs.getObject(idxStart) != null) {
				AccessAttribute operation = new AccessAttribute();
				operation.setOperation(rs.getString(idxStart + 1));
				operation.setAttribute(readAttributeIdAndName(rs, idxStart + 2));
				operation.setTemplate(readNumericIdAndName(rs, idxStart + 6, Template.class));
				operation.setStatus(readNumericIdAndName(rs, idxStart + 9, CardState.class));
				ObjectIdAndName move = readNumericIdAndName(rs, idxStart + 12, WorkflowMove.class);
				if (operation.getMove() != null) {
					if (move.getName().getValueRu() == null || move.getName().getValueRu().trim().length() == 0)
						move.getName().setValueRu(rs.getString(idxStart + 15));
					if (move.getName().getValueEn() == null || move.getName().getValueEn().trim().length() == 0)
						move.getName().setValueEn(rs.getString(idxStart + 16));
				}
				operation.setMove(move);
			}
			return 17;
		}
	}
	
	private class TemplateFilterPlugin extends QueryPlugin {
		
		public TemplateFilterPlugin(FilterByTemplate filter) {
			conditions = surround("(", concatenateStrings(new IdIterator("r.template_id",
					filter.getTemplates().iterator()), " OR "), ")");
		}
	}
	
	public class IdFilterPlugin extends QueryPlugin {
		
		private ObjectId id;
		
		public IdFilterPlugin(ObjectId targetId) {
			this.id = targetId;
			conditions = "r.rule_id=?";
		}

		int addArguments(List args) {
			args.add(id.getId());
			return 1;
		}
	}
	
	private class IdIterator implements Iterator {
		String field;
		Iterator internal;
		
		public IdIterator(String field, Iterator ids) {
			this.field = field;
			internal = ids;
		}

		public boolean hasNext() {
			return internal.hasNext();
		}

		public Object next() {
			return field + "=" + ((ObjectId) internal.next()).getId();
		}

		public void remove() {
			throw new UnsupportedOperationException("Shall never be called");
		}
		
	}
	
	abstract private class PluginIterator implements Iterator {
		Iterator internal;
		
		public PluginIterator(List plugins) {
			internal = plugins.iterator();
		}
		
		public boolean hasNext() {
			return internal.hasNext();
		}

		abstract public Object next();

		public void remove() {
			throw new UnsupportedOperationException("Shall never be called");
		}
	}
	
	private String getAllJoins(List plugins) {
		return concatenateStrings(new PluginIterator(plugins) {
			public Object next() {
				return ((QueryPlugin) internal.next()).joins;
			}
		}, " ");
	}
	
	private String getAllColumns(List plugins) {
		return concatenateStrings(new PluginIterator(plugins) {
			public Object next() {
				return ((QueryPlugin) internal.next()).columns;
			}
		}, ", ");
	}
	
	private String getAllConditions(List plugins) {
		return concatenateStrings(new PluginIterator(plugins) {
			private HashMap groups = new HashMap();
			private Iterator groupItr;
			public boolean hasNext() {
				return internal.hasNext() || (groupItr != null && groupItr.hasNext());
			}
			public Object next() {
				while (internal.hasNext()) {
					QueryPlugin plugin = (QueryPlugin) internal.next();
					if (plugin.condGroup == null) {
						String next = plugin.conditions;
						if (!internal.hasNext())
							groupItr = groups.values().iterator();
						return next;
					}
					if (!groups.containsKey(plugin.condGroup))
						groups.put(plugin.condGroup, new ArrayList());
					((ArrayList) groups.get(plugin.condGroup)).add(plugin.conditions);
					
				}
				if (groupItr == null)
					groupItr = groups.values().iterator();
				ArrayList group = (ArrayList) groupItr.next();
				return "(" + concatenateStrings(group.iterator(), " OR ") + ")";
			}
		}, " AND ");
	}
	
	private void addAllArguments(List arguments, List plugins) {
		for (Iterator itr = plugins.iterator(); itr.hasNext(); ) {
			QueryPlugin plugin = (QueryPlugin) itr.next();
			plugin.addArguments(arguments);
		}
	}
	
	private void readAllColumns(ResultSet rs, int idxStart, AccessRule rule, List plugins)
			throws SQLException {
		for (Iterator itr = plugins.iterator(); itr.hasNext(); ) {
			QueryPlugin plugin = (QueryPlugin) itr.next();
			idxStart += plugin.readRow(rs, idxStart, rule);
		}
	}
	
	private String surround(String prologue, String main, String epilogue) {
		if (main == null || main.length() == 0)
			return "";
		return prologue + main + epilogue;
	}
	
	private String concatenateStrings(Iterator strings, String conjunction) {
		StringBuffer result = new StringBuffer();
		while (strings.hasNext()) {
			String string = (String) strings.next();
			if (string == null || string.length() == 0)
				continue;
			if (result.length() > 0)
				result.append(conjunction);
			result.append(string);
		}
		return result.toString();
	}
	
	private ObjectIdAndName readObjectIdAndName(ResultSet rs, int idxStart, Class idType, boolean idNumeric)
			throws SQLException {
		if (rs.getObject(idxStart) == null)
			return null;
		ObjectIdAndName id = idNumeric ?
				new ObjectIdAndName(idType, rs.getLong(idxStart)) :
				new ObjectIdAndName(idType, rs.getString(idxStart));
		id.setName(new LocalizedString(rs.getString(idxStart + 1), rs.getString(idxStart + 2)));
		return id;
	}
	
	private ObjectIdAndName readNumericIdAndName(ResultSet rs, int idxStart, Class idType)
			throws SQLException {
		return readObjectIdAndName(rs, idxStart, idType, true);
	}
	
	private ObjectIdAndName readStringIdAndName(ResultSet rs, int idxStart, Class idType)
			throws SQLException {
		return readObjectIdAndName(rs, idxStart, idType, false);
	}
	
	private ObjectIdAndName readAttributeIdAndName(ResultSet rs, int idxStart)
			throws SQLException {
		if (rs.getObject(idxStart) == null)
			return null;
		return readStringIdAndName(rs, idxStart, AttributeTypes.getAttributeClass(rs.getString(idxStart + 3)));
	}
}
