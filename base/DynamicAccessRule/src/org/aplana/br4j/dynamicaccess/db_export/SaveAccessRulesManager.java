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
package org.aplana.br4j.dynamicaccess.db_export;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.db_export.objects.AccessRuleDao;
import org.aplana.br4j.dynamicaccess.db_export.objects.CardAccessDao;
import org.aplana.br4j.dynamicaccess.db_export.objects.CreateCardDao;
import org.aplana.br4j.dynamicaccess.db_export.objects.MoveAccessDao;
import org.aplana.br4j.dynamicaccess.db_export.subjects.PermissionSubjectDao;
import org.aplana.br4j.dynamicaccess.xmldef.Operation;
import org.aplana.br4j.dynamicaccess.xmldef.Operations;
import org.aplana.br4j.dynamicaccess.xmldef.Permission;
import org.aplana.br4j.dynamicaccess.xmldef.Rule;
import org.aplana.br4j.dynamicaccess.xmldef.Template;
import org.aplana.br4j.dynamicaccess.xmldef.WfMove;
import org.aplana.br4j.dynamicaccess.xmldef.WfMoves;
import org.aplana.br4j.dynamicaccess.xmldef.types.Action;
import org.aplana.br4j.dynamicaccess.xmldef.types.OperationType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public class SaveAccessRulesManager {
	
    public final static String NO_RULE = "NO_RULE";
    public final static String NO_STATUS = "NO_STATUS";

    public final static String READ = "R";
    public final static String WRITE = "W";
    public final static String CREATE = "C";

    public final static String CREATE_DESCR = "create";

    private final static String GET_RULE_IDS_BY_TEMPLATE_SQL = "SELECT rule_id FROM access_rule WHERE template_id = ?";
    //NOTE: If you will add new table in this request then don't forget to increase RULE_TABLES_FOR_TEMPLATE_COUNT value
    private final static String CLEANUP_RULE_TABLES_FOR_TEMPLATE_SQL = 	
    												"BEGIN;" +
    												"DELETE FROM person_access_rule     WHERE rule_id in (" + GET_RULE_IDS_BY_TEMPLATE_SQL + ");" +	
    												"DELETE FROM role_access_rule       WHERE rule_id in ("+  GET_RULE_IDS_BY_TEMPLATE_SQL + ");" +
    												"DELETE FROM profile_access_rule    WHERE rule_id in ("+  GET_RULE_IDS_BY_TEMPLATE_SQL + ");" +
    												"DELETE FROM delegation_access_rule WHERE rule_id in ("+  GET_RULE_IDS_BY_TEMPLATE_SQL + ");" +
    												"DELETE FROM access_card_rule       WHERE rule_id in ("+  GET_RULE_IDS_BY_TEMPLATE_SQL + ");" +
    												"DELETE FROM access_template_rule   WHERE rule_id in ("+  GET_RULE_IDS_BY_TEMPLATE_SQL + ");" +
    												"DELETE FROM access_move_rule       WHERE rule_id in ("+  GET_RULE_IDS_BY_TEMPLATE_SQL + ");" +
    												"DELETE FROM access_attr_rule       WHERE rule_id in ("+  GET_RULE_IDS_BY_TEMPLATE_SQL + ");" +
    												"DELETE FROM access_list            WHERE rule_id in ("+  GET_RULE_IDS_BY_TEMPLATE_SQL + ");" +
    												"DELETE FROM access_rule          WHERE template_id = ?;" + 
    												"COMMIT;";

    private final static int RULE_TABLES_FOR_TEMPLATE_COUNT = 10;

	protected final Log logger = LogFactory.getLog(getClass());
	
	private Connection connection;
	
	private String uniqueStatusId = "-1";  // ���������� ������, ��� ��� ������� ����� ����������� ������ ��� ������ � ���� �������
	private boolean partial;

	public String getUniqueStatusId() {
		return uniqueStatusId;
	}

	public void setUniqueStatusId(String uniqueStatusId) {
		this.uniqueStatusId = uniqueStatusId;
	}

	public SaveAccessRulesManager(DataSource datasource) throws DbException {
		try {
			this.connection = datasource.getConnection();
		} catch(SQLException e) {
			throw new DbException(e.getMessage());
		}

	}

	/**
	 * Used when we need to have some control on {@link Connection}
	 * @param connection
	 * @throws DbException
	 */
	public SaveAccessRulesManager(Connection connection) throws DbException {
		this.connection = connection;
	}
	
    /**
     * ����� ������� �������� ������ ������� ��� ������������ ���� �������
     */
    public void cleanUpTables() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.executeUpdate("TRUNCATE  access_list, access_attr_rule" +
                ", access_move_rule, access_card_rule" +
                ", access_template_rule, role_access_rule" +
                ", profile_access_rule, person_access_rule" +
                ", delegation_access_rule, access_rule");

        stmt.close();
    }
    
    /**
	 * Clear approprite rule tables for particular template
	 * @param templateName
	 * @throws SQLException
	 */
    public void cleanUpRuleTablesForTemplate(String templateId) throws SQLException {
    	PreparedStatement stmt = connection.prepareStatement(CLEANUP_RULE_TABLES_FOR_TEMPLATE_SQL);
    	Integer id = Integer.valueOf(templateId);
    	for (int i = 1; i <= RULE_TABLES_FOR_TEMPLATE_COUNT; i++) {
        	stmt.setInt(i, id);
        }
    	stmt.executeUpdate();
        stmt.close();
    }

    public void processTemplate(Template template) throws SQLException {
        logger.debug("Processing template " + template.getName() + "...");

        if (template.getRules() == null || template.getRules().getRule() == null) {
            return;
        }        
        Rule[] rules = template.getRules().getRule();
		for (Rule rule : rules) {
			processRule(rule, template);
		}
    }

	private Connection getConnection() {				
		return connection;
	}
	
	public Set<String> processRule(Rule rule, Template template) throws SQLException {
		Set<String> result = new HashSet<String>();
		PermissionSubjectDao permissionSubject = new PermissionSubjectDao(rule, connection);
		
		logger.debug("   Processing rule " + permissionSubject.getName());
		List<Permission> permissions = getRolePermissions(permissionSubject, template);

		Long templateId = Long.parseLong(template.getTemplate_id());
		
		AccessRuleDao accessRuleDao = new AccessRuleDao(new SingleConnectionDataSource(connection, false));
		if(Action.RENAME.equals(rule.getAction())){
			accessRuleDao.renameRuleByRuleHash(rule.getRuleHash(), rule.getName(), templateId);
		}
		if (!permissions.isEmpty()) {

			for (Permission permission : permissions) {
				Long statusId = NO_STATUS.equals(permission.getStatus()) ? null : Long
						.parseLong(permission.getStatus());
				Long locUniqueStatusId = (NO_STATUS.equals(uniqueStatusId)) ? null : Long
						.parseLong(uniqueStatusId);
				// ���� ����� ������ � ��� �� ���������, ������� � ���������� �������
				if ((statusId!=null&&locUniqueStatusId==null)||(statusId==null&&locUniqueStatusId!=null&&locUniqueStatusId!=-1)||(statusId!=null&&locUniqueStatusId!=null&&locUniqueStatusId!=-1&&!statusId.equals(locUniqueStatusId))){
					continue;
				}
				
				Operations operations = permission.getOperations();
				if(operations != null){
					for(Operation operation: operations.getOperations()){
						if(partial && !Action.ADD.equals(operation.getAction())){
							continue;
						} else if (!partial && Action.REMOVE.equals(operation.getAction())){
							continue;
						}
						result.add(operation.getPermHash());
						OperationType operationType = operation.getOperationType();
		
						if (OperationType.READ.equals(operationType)) {
							saveCardRule(permissionSubject, templateId, statusId, operation);
						} else if (OperationType.WRITE.equals(operationType)) {
							saveCardRule(permissionSubject, templateId, statusId, operation);
						} else if (OperationType.CREATE.equals(operationType)) {
							AccessRule ar = new AccessRule(templateId, statusId, operation.getPermHash());
							CreateCardDao ccd = new CreateCardDao(connection);
							AccessRuleStorageDao store = new AccessRuleStorageDao(ar, connection);
							store.insertPermission(ccd, permissionSubject);
							logger.debug("Card creation rule " + ar + " added for " + permissionSubject.getName());
						}
					}
				}

				WfMoves wfMoves = permission.getWfMoves();
				if (wfMoves != null) {
					for (WfMove wfMove : wfMoves.getWfMove()) {
						if(partial && !Action.ADD.equals(wfMove.getAction())){
							continue;
						} else if (!partial && Action.REMOVE.equals(wfMove.getAction())){
							continue;
						}
						result.add(wfMove.getPermHash());
						String wfmId = wfMove.getWfm_id();
						AccessRule ar = new AccessRule(templateId, statusId, wfMove.getPermHash());
						MoveAccessDao mad = new MoveAccessDao(Long.parseLong(wfmId), connection);
						AccessRuleStorageDao store = new AccessRuleStorageDao(ar, connection);
						store.insertPermission(mad, permissionSubject);
						logger.debug("Workflow move " + wfmId + " rule " + ar + " added for "
								+ permissionSubject.getName());
					}
				}
			}
		} /*else {
			if (!NO_RULE.equals(permissionSubject.getName())) {
				// save empty rules (without permissions)
				AccessRule ar = new AccessRule(templateId, null);
				AccessRuleStorageDao store = new AccessRuleStorageDao(ar, connection);
				store.insertPermission(null, permissionSubject);
			}
		}*/
		return result;
	}

	private void saveCardRule(PermissionSubjectDao permissionSubject, Long templateId, Long statusId,
			Operation operation) throws SQLException {
		AccessRule ar = new AccessRule(templateId, statusId, operation.getPermHash());
		CardAccessDao cad = new CardAccessDao(operation.getOperationType().getDataBaseOperationCode(), connection);
		AccessRuleStorageDao store = new AccessRuleStorageDao(ar, connection);
		store.insertPermission(cad, permissionSubject);

		logger.debug("Card operation " + operation + " rule " + ar + " added for " + permissionSubject.getName());
	}

	private List<Permission> getRolePermissions(PermissionSubjectDao permissionSubject, Template template) {
		Permission[] permissions = template.getPermission();
		List<Permission> rolePermissions = new ArrayList<Permission>();

		if (permissions != null) {
			for (Permission permission : permissions) {
				if (permissionSubject.getName().equals(permission.getRule())) {
					rolePermissions.add(permission);
				}
			}
		}
		return rolePermissions;
	}

	public void setPartial(boolean partial) {
		this.partial = partial;
	}
}
