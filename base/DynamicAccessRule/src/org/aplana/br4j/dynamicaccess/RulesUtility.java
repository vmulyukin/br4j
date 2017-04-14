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
package org.aplana.br4j.dynamicaccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.xmldef.Operation;
import org.aplana.br4j.dynamicaccess.xmldef.Permission;
import org.aplana.br4j.dynamicaccess.xmldef.Rule;
import org.aplana.br4j.dynamicaccess.xmldef.RuleCloner;
import org.aplana.br4j.dynamicaccess.xmldef.Status;
import org.aplana.br4j.dynamicaccess.xmldef.Template;
import org.aplana.br4j.dynamicaccess.xmldef.WFMoveType;
import org.aplana.br4j.dynamicaccess.xmldef.WfMove;
import org.aplana.br4j.dynamicaccess.xmldef.types.Action;
import org.aplana.br4j.dynamicaccess.xmldef.types.OperationType;
import org.aplana.br4j.dynamicaccess.xmldef.types.PermissionCloner;

/**
 * 
 * @author atsvetkov
 *
 */
public class RulesUtility {

	protected static final Log logger = LogFactory.getLog(RulesUtility.class);

	public static final String RULE_NAME_SUFFIX = "_SYSTEM_";
	public static final String RULE_NAME_COPY_SUFFIX = "_�����";
	public static final String RULE_NAME_MODIFIED_PREFIX = "[������]";

	public static int UNDEFINED_RULE_TYPE  = -1;
	public static int PERSON_RULE_TYPE     =  1;
	public static int ROLE_RULE_TYPE       =  2;
	public static int PROFILE_RULE_TYPE    =  3;
	public static int DELEGATION_RULE_TYPE =  4;

	private static final String STATUS_ID_DELEMETER = ", ";

	public static String getRuleName(String value) {
		int systemIndex = value.indexOf(RULE_NAME_SUFFIX);
		if (systemIndex > 0) {
			return value.substring(0, systemIndex);
		}
		return value;
	}
	
	public static void updateRuleName(Rule rule, String newName) {
		if (rule != null) {
			rule.setName(newName);
			if (rule.getRulePerson() != null) {
				rule.getRulePerson().setName(newName);
			} else if (rule.getRuleRole() != null) {
				rule.getRuleRole().setName(newName);
			} else if (rule.getRuleProfile() != null) {
				rule.getRuleProfile().setName(newName);
			} else if (rule.getRuleDelegation() != null) {
				rule.getRuleDelegation().setName(newName);
			}
		}
	}
	
	public static String addCopySuffix(String value) {
		int systemIndex = value.indexOf(RULE_NAME_SUFFIX);
		if (systemIndex > 0) {
			return value.substring(0, systemIndex) + RULE_NAME_COPY_SUFFIX + value.substring(systemIndex, value.length());
		}
		return value + RULE_NAME_COPY_SUFFIX;
	}
	
	public static String join(List<String> list) {
	    if (list.isEmpty()){
	    	return "";
	    }
	    Iterator<String> iter = list.iterator();
	    StringBuffer buffer = new StringBuffer(iter.next());
	    while (iter.hasNext()) {
	    	buffer.append(STATUS_ID_DELEMETER).append(iter.next());
	    }
	    return buffer.toString();
	}
	

    public static List<String> getPersonLinkedStatusIds(List<Rule> rules) {
        List<String> personLinkedStatusIds = new ArrayList<String>();
        for(Rule rule : rules) {
        	if(rule.getRulePerson() != null && rule.getRulePerson().getLinkedStatusId() != null) {
        		personLinkedStatusIds.add(rule.getRulePerson().getLinkedStatusId());
        	}
        }
    	return personLinkedStatusIds;
	}

    public static List<String> getPersonStaticRoleCodes(List<Rule> rules) {
        List<String> personStaticRoleCodes = new ArrayList<String>();
        for(Rule rule : rules) {
        	if(rule.getRulePerson() != null && rule.getRulePerson().getRoleCode() != null) {
        		personStaticRoleCodes.add(rule.getRulePerson().getRoleCode());
        	}
        }
    	return personStaticRoleCodes;
	}
    
    public static List<String> getProfileLinkedStatusIds(List<Rule> rules) {
        List<String> profileLinkedStatusIds = new ArrayList<String>();
        for(Rule rule : rules) {
        	if(rule.getRuleProfile() != null && rule.getRuleProfile().getLinkedStatusId() != null) {
        		profileLinkedStatusIds.add(rule.getRuleProfile().getLinkedStatusId());
        	}
        }
    	return profileLinkedStatusIds;
	}

    public static List<String> getProfileStaticRoleCodes(List<Rule> rules) {
        List<String> personStaticRoleCodes = new ArrayList<String>();
        for(Rule rule : rules) {
        	if(rule.getRuleProfile() != null && rule.getRuleProfile().getRoleCode() != null) {
        		personStaticRoleCodes.add(rule.getRuleProfile().getRoleCode());
        	}
        }
    	return personStaticRoleCodes;
	}

    /**
     * Retrieve {@link Permission}s for the specified {@link Rule}.
     * @param ruleName used to find {@link Permission}s
     * @param template {@link Template} containing {@link Permission}s being looked for
     * @return {@link List} of {@link Permission}
     */
	public static List<Permission> getPermissionsByRuleName(String ruleName, Template template) {
		Permission[] permissions = template.getPermission();
		List<Permission> foundPermissions = new ArrayList<Permission>();

		if (permissions != null) {
			for (Permission permission : permissions) {
				if (permission.getRule().equals(ruleName)) {
					foundPermissions.add(permission);
				}
			}
		}
		return foundPermissions;
	}

	/**
     * Finds {@link Permission}s having exactly the specified rule and status.
     * @param ruleName used to find {@link Permission}s.
	 * @param statusId used to find {@link Permission}s.
     * @param template {@link Template} where {{@link Permission} are looked for
	 * @return {@link List} of {@link Permission}
	 */
	public static List<Permission> getPermissionsByRuleNameAndStatus(String ruleName, String statusId, Template template) {
		Permission[] permissions = template.getPermission();
		List<Permission> foundPermissions = new ArrayList<Permission>();

		if (permissions != null) {
			for (Permission permission : permissions) {
				if (permission.getRule().equals(ruleName) && (permission.getStatus().equals(statusId))) {
					foundPermissions.add(permission);
				}
			}
		}
		return foundPermissions;
	}

	/**
	 * Returns the status id found by status name for specified template. If the status with specified name is not found then returns null.
	 * @param statusName the name of status.
	 * @param template the template under the question.
	 * @return the status id.
	 */
	public static String getStatusIdByStatusNameForTemplate(String statusName, Template template) {
		Status[] statuses = template.getStatus();
		for (Status status : statuses) {
			if (statusName.equals(status.getName())) {
				return status.getStatus_id();
			}
		}
		return null;
	}
	
	/**
	 * Deletes {@link Rule} by rule name from {@link Template}.
	 * @param rules to be deleted.
	 * @param template {@link Template} containing rule under the question
	 */
	public static void deleteRules(List<Rule> rules, Template template) {
		for(Rule rule : rules){
			if(rule != null){
				for (int i = 0; i < template.getRules().getRuleCount(); i++) {
					if(rule.getName() != null && rule.getName().equals(template.getRules().getRule(i).getName())){
						template.getRules().removeRule(i);
						logger.debug("Deleting rule: " + rule.getName());
						i--;
					}
				}
			}
		}		
	}

	/**
	 * Copies all rules from {@link Template}.
	 * 
	 * @param template containing source rules.
	 * @return {@link List} of {@link Rule}.
	 */
	public static List<Rule> copyRules(Template template) {
		List<Rule> clonedRules = new ArrayList<Rule>();
		for(Rule rule : template.getRules().getRule()){
			clonedRules.add(RuleCloner.clone(rule));
		}
		return clonedRules;
	}

	/**
	 * Removes permission by rule name and status from {@link Template}.
	 * @param permission to be removed.
	 * @param template {@link Template} containing rule under the question. 
	 */
	public static int deletePermission(Permission permission, Template template) {
		int removed = 0;
		if(permission != null) {
			for (int i = 0; i < template.getPermissionCount(); i++) {
				if (permission.getRule() != null
						&& permission.getRule().equals(template.getPermission(i).getRule())
						&& permission.getStatus() != null
						&& permission.getStatus().equals(template.getPermission(i).getStatus())) {
					template.removePermission(i);
					removed++;
					logger.debug("Deleting permission: " + permission);
					i--;
				}
			}
		}
		return removed;
	}
	
    /**
     * Finds {@link Rule}s with name containing the passed ruleName.
     * @param ruleName used to find rules.
     * @return {@link List} of found {@link Rule}.
     */
	public static List<Rule> getRulesByRuleNameFromModel(String ruleName, Template template) {
		List<Rule> rulesList = Arrays.asList(template.getRules().getRule());
		
		return getRulesByRuleName(ruleName, rulesList);
	}

	public static List<Rule> getRulesByRuleName(String ruleName, List<Rule> rulesList) {
		List<Rule> foundRules = new ArrayList<Rule>();
		
		if(ruleName != null){
			for(Rule rule: rulesList){
				if(ruleName.equals(rule.getName().split(RulesUtility.RULE_NAME_SUFFIX)[0])){
					foundRules.add(rule);
				}
			}
		}
		
		return foundRules;
	}

	public static int getRuleType(Rule rule) {
		int type = UNDEFINED_RULE_TYPE;
		if (rule != null) {
			if (rule.getRulePerson() != null) {
				type = PERSON_RULE_TYPE;
			} else if (rule.getRuleRole() != null) { 
				type = ROLE_RULE_TYPE;
			} else if (rule.getRuleProfile() != null) {
				type = PROFILE_RULE_TYPE;
			} else if (rule.getRuleDelegation() != null) {
				type = DELEGATION_RULE_TYPE;
			}
		}
		return type;
	}
	
    /**
     * ������� ��������� ���� � ������ (Permission) �� ��������� �������� �� ��������� � ��������� ���������,
     * ������� ��������� � allBasePermissions
     * @param permission
     * @param templateId
     * @param allBasePermissions
     */
    public static void  createOperationsModification(Permission permission, Template template, Map<Long,Set<Permission>> allBasePermissions){
    	Long templateId = Long.parseLong(template.getTemplate_id());
    	if(!allBasePermissions.containsKey(templateId)){
    		allBasePermissions.put(templateId, new HashSet<Permission>());
    	}
    	Set<Permission> templatePermissions = allBasePermissions.get(templateId);
    	if(!templatePermissions.contains(permission)){
    		Permission p = new Permission();
    		p.setRule(permission.getRule());
    		p.setStatus(permission.getStatus());
    		templatePermissions.add(p);
    	}
    	for(Permission basePermission : templatePermissions){
    		if(basePermission.hashCode() == permission.hashCode()){
    			permission.removeAutoOperationsAndWFMoves();
    			for(Operation operation : permission.getOperations().getOperations()){
    				Operation baseEqualOperation = getOperationByHash(basePermission.getOperations().getOperations(), operation.getPermHash());
    				if(baseEqualOperation == null){
    					operation.setAction(Action.ADD);
    				} else if(Action.ADD.equals(baseEqualOperation.getAction())){
    					operation.setAction(Action.ADD);
    				} else {
    					operation.setAction(null);
    				}
    			}
    			for(Operation baseOperation : basePermission.getOperations().getOperations()){
    				Operation equalOperation = getOperationByHash(permission.getOperations().getOperations(), baseOperation.getPermHash());
    				if(equalOperation == null){
    					if(baseOperation.getAction() == null || baseOperation.getAction().equals(Action.ADD)){
    						Operation newOperation = new Operation(baseOperation.getOperationType());
    						newOperation.setPermHash(baseOperation.getPermHash());
    						newOperation.setAction(Action.REMOVE);
    						permission.getOperations().addOperation(newOperation);
    					}
    				}
    			}
    			for(WfMove wfMove : permission.getWfMoves().getWfMove()){
    				WfMove baseEqualWfMove = getWfMoveByHash(basePermission.getWfMoves().getWfMove(), wfMove.getPermHash());
    				if(baseEqualWfMove == null){
    					wfMove.setAction(Action.ADD);
    				} else if(Action.ADD.equals(baseEqualWfMove.getAction())){
    					wfMove.setAction(Action.ADD);
    				} else {
    					wfMove.setAction(null);
    				}
    			}
    			for(WfMove baseWfMove : basePermission.getWfMoves().getWfMove()){
    				WfMove equalWfMove = getWfMoveByHash(permission.getWfMoves().getWfMove(), baseWfMove.getPermHash());
    				if(equalWfMove == null){
    					if(baseWfMove.getAction() == null || baseWfMove.getAction().equals(Action.ADD)){
    						WfMove newWfMove = new WfMove();
    						newWfMove.setWfm_id(baseWfMove.getWfm_id());
    						newWfMove.setName(baseWfMove.getName());
    						newWfMove.setPermHash(baseWfMove.getPermHash());
    						newWfMove.setAction(Action.REMOVE);
    						permission.getWfMoves().addWfMove(newWfMove);
    					}
    				}
    			}
    			
    			//���� ������� ����� READ �� ������ ������ � ����� �� WRITE.
    			if(permission.hasOperation(OperationType.READ, Action.REMOVE) 
    					&& !permission.hasOperation(OperationType.WRITE, Action.REMOVE)){
    				permission.getOperations().addOperation(new Operation(OperationType.WRITE, Action.REMOVE));
    			}
    			//���� ������� ����� WRITE, �� ������ ������ ����� �� ��� ��������� ��������.
    			if(permission.hasOperation(OperationType.WRITE, Action.REMOVE)){
        			for(WFMoveType wFMoveType: template.getWFMoveType()){
        				if(permission.getStatus().equals(wFMoveType.getWfm_from_status()) && 
        							!permission.hasWfMove(wFMoveType.getWfm_id(), Action.REMOVE)){
        					permission.getWfMoves().addWfMove(
        							new WfMove(wFMoveType.getName() + "->" + wFMoveType.getWfm_to(),
        									wFMoveType.getWfm_id(), Action.REMOVE));
        				}
        			}
    			}
    			if(permission.hasOperation(OperationType.WRITE, Action.ADD) 
    					&& !permission.hasOperation(OperationType.WRITE, Action.REMOVE)){
    				permission.getOperation(OperationType.READ).setAction(Action.ADD);
    			}
    			for(WfMove wfMove: permission.getWfMoves().getWfMove()){
    				if(Action.ADD.equals(wfMove.getAction())){
    					permission.getOperation(OperationType.READ).setAction(Action.ADD);
    					permission.getOperation(OperationType.WRITE).setAction(Action.ADD);
    				}
    			}
    				
    			permission.generatePermHashes(template);
    		}    		
    	}
    }
    
    private static Operation getOperationByHash(Operation[] operations, String permHash){
    	for(Operation operation : operations){
    		if(operation.getPermHash().equals(permHash)){
    			return operation;
    		}
    	}
    	return null;
    }
    
    private static WfMove getWfMoveByHash(WfMove[] wfMoves, String permHash){
    	for(WfMove wfMove : wfMoves){
    		if(wfMove.getPermHash().equals(permHash)){
    			return wfMove;
    		}
    	}
    	return null;
    }
	
    /**
     * �������� ��� ��������� ����� ��� ������� �� �������� (Action.REMOVE)
     * @param rulesToDelete
     * @param m_template
     * @param allBasePermissions
     */
	public static void markRuleAsRemoved(List<Rule> rulesToDelete, Template m_template, Map<Long,Set<Permission>> allBasePermissions) {
		for(Rule rule: rulesToDelete){
			List<Permission> permissionsToDelete = RulesUtility.getPermissionsByRuleName(rule.getName(), m_template);
			Map<String, Permission> statusPermissions  = new HashMap<String, Permission>();
			for(Permission permission : permissionsToDelete){
				Long templateId = m_template.getTemplateIdLong();
				statusPermissions.put(permission.getStatus(), permission);
		    	if(!allBasePermissions.containsKey(templateId)){
		    		allBasePermissions.put(templateId, new HashSet<Permission>());
		    	}
	    		if(!allBasePermissions.get(templateId).contains(permission)){
	    			allBasePermissions.get(templateId).add(PermissionCloner.clonePermission(permission));
	    		}
			}

			for(Status status : m_template.getStatus()){
				if(!statusPermissions.containsKey(status.getStatus_id())){
					Permission p = new Permission();
					p.setStatus(status.getStatus_id());
					p.setRule(rule.getName());
					if(status.getStatus_id().equals("NO_STATUS")){
						if(rule.getName().equals("NO_RULE") || rule.getRuleRole() != null){
							p.getOperations().addOperation(new Operation(OperationType.CREATE, Action.REMOVE));
						}
					} else {
						p.getOperations().addOperation(new Operation(OperationType.READ, Action.REMOVE));
						p.getOperations().addOperation(new Operation(OperationType.WRITE, Action.REMOVE));
						for(WFMoveType wFMoveType : m_template.getWFMoveType()){
							if(status.getStatus_id().equals(wFMoveType.getWfm_from_status())){
								p.getWfMoves().addWfMove(new WfMove(wFMoveType.getName() + "->" + wFMoveType.getWfm_to(),
    									wFMoveType.getWfm_id(), Action.REMOVE));
							}
						}
					}
					p.generatePermHashes(m_template);
					m_template.addPermission(p);
				} else {
					Permission p = statusPermissions.get(status.getStatus_id());
					if(status.getStatus_id().equals("NO_STATUS")){
						p.getOperation(OperationType.CREATE).setAction(Action.REMOVE);
					} else {
						Operation o = p.getOperation(OperationType.READ);
						if (o != null){
							o.setAction(Action.REMOVE);
						} else {
							p.getOperations().addOperation(new Operation(OperationType.READ, Action.REMOVE));
						}
						o = p.getOperation(OperationType.WRITE);
						if (o != null){
							o.setAction(Action.REMOVE);
						} else {
							p.getOperations().addOperation(new Operation(OperationType.WRITE, Action.REMOVE));
						}
						for(WFMoveType wFMoveType : m_template.getWFMoveType()){
							if(status.getStatus_id().equals(wFMoveType.getWfm_from_status())){
								WfMove wfMove = p.getWfMove(wFMoveType.getWfm_id());
								if(wfMove != null){
									wfMove.setAction(Action.REMOVE);
								} else {
									p.getWfMoves().addWfMove(new WfMove(wFMoveType.getName() + "->" + wFMoveType.getWfm_to(),
        									wFMoveType.getWfm_id(), Action.REMOVE));
								}
							}
						}
					}
					p.generatePermHashes(m_template);
				}
				
			}
		}
	}

	/**
	 * �������� ��� ������ ������� ���� �������� ������� ��� ���������� (��� ������� ����� ���������� �� ����������,
	 * ��� ������������� - �� ��������). �������� ����� � ������� ���� �� ���� ��.
	 * @param m_template
	 * @param rule
	 * @param statusName
	 */
	public static void markCellForOverwriting(Template m_template, Rule rule, String statusName) {
		String statusId = RulesUtility.getStatusIdByStatusNameForTemplate(statusName, m_template);
		Permission statusRulePermission = null;
		for(Permission templatePermission: m_template.getPermission()){
			if(templatePermission.getRule().equals(rule.getName()) && templatePermission.getStatus().equals(statusId)){
				statusRulePermission = templatePermission;
			}
		}
		if(statusRulePermission == null){
			statusRulePermission = new Permission();
			statusRulePermission.setRule(rule.getName());
			statusRulePermission.setStatus(statusId);
			m_template.addPermission(statusRulePermission);
		}
		
    	Long templateId = Long.parseLong(m_template.getTemplate_id());
    	if(!EditConfigMainForm.allBasePermissions.containsKey(templateId)){
    		EditConfigMainForm.allBasePermissions.put(templateId, new HashSet<Permission>());
    	}
		if(!EditConfigMainForm.allBasePermissions.get(templateId).contains(statusRulePermission)){
			EditConfigMainForm.allBasePermissions.get(templateId).add(PermissionCloner.clonePermission(statusRulePermission));
		}
		
		if(statusId.equals("NO_STATUS")){
			overwriteOperation(statusRulePermission, OperationType.CREATE);
		} else {
			overwriteOperation(statusRulePermission, OperationType.READ);
			overwriteOperation(statusRulePermission, OperationType.WRITE);
		}
		
		for(WFMoveType moveType: m_template.getWFMoveType()){
			if(moveType.getWfm_from_status().equals(statusRulePermission.getStatus())){
				overwriteWFMove(statusRulePermission, moveType.getWfm_id(),  moveType.getName());
			}
		}
		statusRulePermission.generatePermHashes(m_template);
		
	}
	
	private static void overwriteOperation(Permission permission, OperationType type){
		Operation o = permission.getOperation(type);
		if(o == null){
			o = new Operation(type, Action.REMOVE);
			permission.getOperations().addOperation(o);
		} else if (o.getAction() == null) {
			o.setAction(Action.ADD);
		}
	}
	
	private static void overwriteWFMove(Permission permission, String wfm_id, String wfmName){
		WfMove w = permission.getWfMove(wfm_id);
		if(w == null){
			w = new WfMove(wfmName, wfm_id, Action.REMOVE);
			permission.getWfMoves().addWfMove(w);
		} else if (w.getAction() == null) {
			w.setAction(Action.ADD);
		}
	}

	/**
	 * �������� ��� ������ � ������� ������� ��� ���������� (����� ������ �� ���� ��������).
	 * @param permission
	 * @param type
	 */
	public static void markRuleForOverwriting(Rule rule, Template m_template) {
		for(Status status: m_template.getStatus()){
			markCellForOverwriting(m_template, rule, status.getName());
		}
	}
	
	/**
	 * ���������� ��������� ������������ ��� ������ ������� ����. ����� ���������� � ����, ������������ � allBasePermissions.
	 * @param permission
	 * @param type
	 */
	public static void clearChangesForCell(String statusName, Rule rule, Template template, Map<Long,Set<Permission>> allBasePermissions){
		String statusId = RulesUtility.getStatusIdByStatusNameForTemplate(statusName, template);
		Set<Permission> templateBasePermissions = allBasePermissions.get(template.getTemplateIdLong());
		if(templateBasePermissions == null){
			return;
		}
		Iterator<Permission> iterator = template.getPermissionList().iterator();
		while(iterator.hasNext()){
			Permission p = iterator.next();
			if(p.getRule().equals(rule.getName()) && p.getStatus().equals(statusId) 
					&& templateBasePermissions.contains(p)){
				iterator.remove();
			}
		}
		for(Permission p: templateBasePermissions){
			if(p.getRule().equals(rule.getName()) && p.getStatus().equals(statusId) 
					&& !template.getPermissionList().contains(p)){
				template.addPermission(PermissionCloner.clonePermission(p));
			}
		}
	}

	/**
	 * ���������� ��������� ������������ ��� �������� �������. ����� ���������� � ����, ������������ � allBasePermissions.
	 * @param rule
	 * @param m_template
	 * @param allBasePermissions
	 */
	public static void clearChangesForRule(Rule rule, Template m_template,
			Map<Long, Set<Permission>> allBasePermissions) {
		for(Status status: m_template.getStatus()){
			clearChangesForCell(status.getName(), rule, m_template, allBasePermissions);
		}
	}
}