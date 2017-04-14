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

import java.awt.Frame;
import com.borland.jbcl.layout.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.xmldef.*;
import org.aplana.br4j.dynamicaccess.xmldef.types.Action;
import org.aplana.br4j.dynamicaccess.xmldef.types.PermissionCloner;
import org.aplana.br4j.dynamicaccess.db_export.objects.AccessListDao;
import org.aplana.br4j.dynamicaccess.rule_attribute.*;
import org.aplana.br4j.dynamicaccess.rule_attribute.RuleFormAttribute.StaticRole;
import org.aplana.br4j.dynamicaccess.rule_attribute.StatusFormAttribute.LinkedStatus;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import javax.swing.*;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.event.*;

public class RulesForm extends DialogBase implements DocumentListener { //��������� �������� �����

    protected final Log logger = LogFactory.getLog(getClass());

    //��������� ���� �������� ��� ����������� ���������
    JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();
    JLabel jLabel3 = new JLabel();
    JLabel jLabel4 = new JLabel();
    JLabel jLabel5 = new JLabel();
    JLabel jLabel6 = new JLabel();
    JLabel jLabel7 = new JLabel();
    //��������� ��������� ���� ��� ����� �������� ��������� ����
    //������ ��������� ���� ��������� � ���������� ����� ��� ����, ��� � �� ����
    JTextField txtRuleName = new JTextField();
    JTextField txtRule2 = new JTextField();
    JTextField txtRule3 = new JTextField();
    JTextField txtRule4 = new JTextField();
    JTextField txtRule5 = new JTextField();
    DefaultListModel listModel = new DefaultListModel();
    JList linkedStatusIdsList = new JList(listModel);
    JScrollPane paneListRule6 = new JScrollPane(linkedStatusIdsList);
    JTextField txtRule7 = new JTextField();
    StaticRole staticRole;

    DefaultListModel staticRolesListModel = new DefaultListModel();
    JList staticRolesJList = new JList(staticRolesListModel);
    JScrollPane staticRolesScrollPane = new JScrollPane(staticRolesJList);

    String txtRule2Code;
    String txtRule3Code;
    String txtRule4Code;
    String txtRule5Code;


    //��������� ��������� ��� ������ �� ���������� xml �����
    private AccessConfig m_config;
    private Template m_template;
    private FindAttributeName attrNames;
    private int m_indexChoiceRule;
    private List<Rule> rules;

    private String currentName = "";
    JButton jbtnEllipse1 = new JButton();
    JButton jbtnEllipse2 = new JButton();
    JButton jbtnEllipse3 = new JButton();
    JButton jbtnEllipse4 = new JButton();
    JButton jbtnEllipse5 = new JButton();
    JButton jbtnEllipse6 = new JButton();
    Frame par;
    private int comboBoxIndex;
    JButton jbtnC1 = new JButton();
    JButton jbtnC2 = new JButton();
    JButton jbtnC3 = new JButton();
    JButton jbtnC4 = new JButton();
    JButton jbtnC5 = new JButton();
    JButton jbtnC6 = new JButton();
    JPopupMenu linkedStatusIdsPopupMenu = new JPopupMenu();
    JPopupMenu staticRolesPopupMenu = new JPopupMenu();

    final int STATUS_LINK_HEIGHT = 50;


    public RulesForm(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        try {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            pack();
            setAutoSize();
        } catch (Exception exception) {
            exception.printStackTrace();
            logger.error("Exception occured: " + exception.getMessage());
        }
    }

    // DocumentListener methods
    public void insertUpdate(DocumentEvent ev) {
        closeTextField(txtRule4.getText(), txtRule2.getText(), txtRule3.getText(),
                m_indexChoiceRule);
    }

    public void removeUpdate(DocumentEvent ev) {
        closeTextField(txtRule4.getText(), txtRule2.getText(), txtRule3.getText(),
                m_indexChoiceRule);
    }

    public void changedUpdate(DocumentEvent ev) {
        closeTextField(txtRule4.getText(), txtRule2.getText(), txtRule3.getText(),
                m_indexChoiceRule);
    }


    /**
     * * ����� ��������� �� ������ EditConfigMainForm ��������� ��� ������ � ����� ����� �������
     *
     * @param parent          Frame - ��������� �������� ������������� ������(��������)
     * @param template        Template - ��������� ������ �� ���������� �������� �������
     * @param rules           List<Rule> - ������ ������ ��� ��������������
     * @param indexChoiceRule int - ��������� ������ �������� ���� �������
     */
    public RulesForm(Frame parent, AccessConfig config, Template template, List<Rule> rules, FindAttributeName attrNames, int indexChoiceRule,
                     int index) {
        this(parent, "����", true);
        this.par = parent;
        this.m_config = config;
        this.m_template = template;
        this.rules = rules;
        this.attrNames = attrNames;
        this.m_indexChoiceRule = indexChoiceRule;
        this.comboBoxIndex = index;
        initLinkedStatusIdsPopupMenu();
        initStaticRolesPopupMenu();
    }

    private void initLinkedStatusIdsPopupMenu() {
        linkedStatusIdsList.addMouseListener(new DeletePopupMenuAdapter(linkedStatusIdsList, linkedStatusIdsPopupMenu));
        final JMenuItem deleteItem = new JMenuItem("�������");
        deleteItem.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent ae) {
                int[] selectedIndices = linkedStatusIdsList.getSelectedIndices();
                for (int index : selectedIndices) {
                    ((DefaultListModel) linkedStatusIdsList.getModel()).remove(index);
                }

            }
        });

        linkedStatusIdsPopupMenu.add(deleteItem);

    }

    private void initStaticRolesPopupMenu() {
        staticRolesJList.addMouseListener(new DeletePopupMenuAdapter(staticRolesJList, staticRolesPopupMenu));
        final JMenuItem deleteItem = new JMenuItem("�������");
        deleteItem.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent ae) {
                int[] selectedIndices = staticRolesJList.getSelectedIndices();
                for (int index : selectedIndices) {
                	((DefaultListModel) staticRolesJList.getModel()).remove(index);
                }

            }
        });

        staticRolesPopupMenu.add(deleteItem);

    }

    /**
     * ����� ������������ ������� �� ������ "Ok".
     *
     * @return boolean
     */
    public boolean onOk() {
        List<Rule> oldRules = rules;
        List<Rule> newRules = new ArrayList<Rule>();

		if (!isValidName(txtRuleName.getText())) {
			return false;
		}

		String ruleName = txtRuleName.getText();

        //���� ������ ����� 1 - �.�. ��� "������������"
        if (m_indexChoiceRule == 1) {
            if (isEmpty(txtRule2.getText())) {
                showErrorMessage("������� ������������ - ������������ ����", "������!!!");
                return false;
            }
            if (isEmpty(txtRule3.getText()) && !isEmpty(txtRule4.getText())) {
                showErrorMessage("������� ������������� ����� ������ ���������, ���� �� ������ ������� �����",
                        "������!!!");
                return false;
            }

			if (linkedStatusIdsList.getModel().getSize() == 0) {
				if (getSelectedStaticRoles(staticRolesJList).size() == 0) {
					Rule newRule = createPersonRuleEmptyStaticRoleAndStatus(ruleName);
					newRules.add(newRule);
				} else {
					for (StaticRole staticRole : getSelectedStaticRoles(staticRolesJList)) {
						Rule newRule = createPersonRuleEmptyStatus(ruleName, staticRole);
						newRules.add(newRule);
					}
				}
			} else if (linkedStatusIdsList.getModel().getSize() > 0) {
				for (LinkedStatus linkedStatus : getSelectedLinkedStatuses(linkedStatusIdsList)) {
					if (getSelectedStaticRoles(staticRolesJList).size() == 0) {
						Rule newRule = createPersonRuleEmptyStaticRole(ruleName, linkedStatus);
						newRules.add(newRule);
					} else {
						for (StaticRole staticRole : getSelectedStaticRoles(staticRolesJList)) {
							Rule newRule = createPersonRule(ruleName, linkedStatus, staticRole);
							newRules.add(newRule);
						}

					}
				}
			}

		}
        // ���� ������ ����� 2 - �.�. ��� "����"
        else if (m_indexChoiceRule == 2) {
            if (isEmpty(txtRule2.getText())) {
                showErrorMessage("������������� ���� - ������������ ����", "������!!!");
                return false;
            }
            Rule newRule = createRoleRule(ruleName);
            newRules.add(newRule);

        }
        // ���� ������ ����� 3 - �.�. ��� "�������"
        else if (m_indexChoiceRule == 3) {
			if (isEmpty(txtRule2.getText()) || isEmpty(txtRule3.getText())) {
				showErrorMessage("������� ������� � ��������� ������� - ������������ ����.", "������!!!");
				return false;
			}
			if (linkedStatusIdsList.getModel().getSize() == 0) {
				if (getSelectedStaticRoles(staticRolesJList).size() == 0) {
					Rule newRule = createProfileRuleEmptyStaticRoleAndStatus(ruleName);
					newRules.add(newRule);
				} else {
					for (StaticRole staticRole : getSelectedStaticRoles(staticRolesJList)) {
						Rule newRule = createProfileRuleEmptyStatus(ruleName, staticRole);
						newRules.add(newRule);
					}
				}
			} else if (linkedStatusIdsList.getModel().getSize() > 0) {
				for (LinkedStatus linkedStatus : getSelectedLinkedStatuses(linkedStatusIdsList)) {
					if (getSelectedStaticRoles(staticRolesJList).size() == 0) {
						Rule newRule = createProfileRuleEmptyStaticRole(ruleName, linkedStatus);
						newRules.add(newRule);
					} else {
						for (StaticRole staticRole : getSelectedStaticRoles(staticRolesJList)) {
							Rule newRule = createProfileRule(ruleName, linkedStatus, staticRole);
							newRules.add(newRule);
						}
					}
				}
			}
		}
        // ���� ������ ����� 4 - �.�. ��� "�������������"
        else if (m_indexChoiceRule == 4) {
            if (isEmpty(txtRule2.getText())) {
                showErrorMessage("������� ����� - ������������ ����.", "������!!!");
                return false;
            }
            Rule newRule = createDelegationRule(ruleName);
            newRules.add(newRule);
        }
        //�������� �� �������������� ������.
        //������ ������� ��������� ����� �� ����� ������
        String newBaseName = newRules.get(0).getName().split(RulesUtility.RULE_NAME_SUFFIX)[0];
        
        Set<Permission> allBaseTemplatePermission = EditConfigMainForm.allBasePermissions.get(m_template.getTemplateIdLong());
        if(allBaseTemplatePermission == null){
        	allBaseTemplatePermission = new HashSet<Permission>();
        	EditConfigMainForm.allBasePermissions.put(m_template.getTemplateIdLong(), allBaseTemplatePermission);
        }
        if(oldRules.size() == newRules.size() && newRules.containsAll(oldRules)){
        	String oldBasename = oldRules.get(0).getName().split(RulesUtility.RULE_NAME_SUFFIX)[0];
        	if(oldBasename.equals(newBaseName)){
        		//������� �� ����������
        		return true;
        	} else {
        		//�������������� �������
        		for(Rule oldRule: oldRules){
        			List<Permission> permissions = getExistingPermissionsForRule(oldRule.getName());
        			oldRule.deepUpdateName(oldRule.getName().replace(oldBasename, newBaseName));
        			for(Permission p : permissions){
    					Iterator<Permission> iterator =  allBaseTemplatePermission.iterator();
    					Set<Permission> updatedBasePermissions = new HashSet<Permission>();
    					while(iterator.hasNext()){
    						Permission basePermission = iterator.next();
    						if(basePermission.equals(p)){
    							iterator.remove();
    						}
    						basePermission.setRule(oldRule.getName());
    						updatedBasePermissions.add(basePermission);
    					}
    					allBaseTemplatePermission.addAll(updatedBasePermissions);
        				p.setRule(oldRule.getName());
        			}
        			oldRule.setAction(Action.RENAME);
        		}
        		return true;
        	}
        }
        
        //�������� ������������ ������
        for(Rule newRule: newRules){
        	for(Rule rule : m_template.getRules().getRule()){
        		if(rule.getRuleHash().equals(newRule.getRuleHash())  && !oldRules.contains(rule)){
        			showErrorMessage("������ ������� ��� ����������", "������!!!");
        			return false;
        		}
        	}
        }

        //��������� �������
        if (oldRules.size() > 0) {
        	List<Permission> permissions = getExistingPermissionsForRule(oldRules.get(0).getName());
        	List<Permission> clonedPermissions = new ArrayList<Permission>();
        	for(Permission p: permissions){
        		clonedPermissions.add(PermissionCloner.clonePermission(p));
        	}
        	//������� ������� �� oldRules, ������� ���� � newRules (��� ����� ��������)
        	//��, ������� ���������� � oldRules, ������� �� ��������
        	Iterator<Rule> iterator =  oldRules.iterator();
        	while(iterator.hasNext()){
        		Rule oldRule = iterator.next();
        		if(newRules.contains(oldRule)){
        			iterator.remove();
        			List<Permission> permsToRemove = getExistingPermissionsForRule(oldRule.getName());
        			m_template.getPermissionList().removeAll(permsToRemove);
        			m_template.getRules().getRuleList().remove(oldRule);
        			allBaseTemplatePermission.removeAll(permsToRemove);
        		}
        	}
        	
        	//����������� ��� ������ ������� (���� �� ������������� � ������)
        	for(Rule oldRule: oldRules){
        		List<Permission> permsToRemove = getExistingPermissionsForRule(oldRule.getName());
        		oldRule.deepUpdateName(RulesUtility.RULE_NAME_MODIFIED_PREFIX + oldRule.getName());
        		for(Permission p: permsToRemove){
        			p.setRule(oldRule.getName());
        		}
        	}
        	
        	RulesUtility.markRuleAsRemoved(oldRules, m_template, EditConfigMainForm.allBasePermissions);
            addNewRules(newRules); //����� �������� ������� � ������ �� ����, ���� ��������� ��������� ����
        	addNewPermissions(newRules, clonedPermissions, allBaseTemplatePermission);
        	return true;
        }
        
        addNewRules(newRules);
        return true;
    }

	private Rule createDelegationRule(String ruleName) {
		Rule newRule = new Rule();
		newRule.setName(ruleName);

		RuleDelegation ruleDelegation = new RuleDelegation();
		newRule.setRuleDelegation(ruleDelegation);
		newRule.getRuleDelegation().setName(ruleName);
		newRule.getRuleDelegation().setLinkAttributeCode(txtRule2Code);
		return newRule;
	}

    /**
     * Creates new role {@link Rule}. 
     * @param ruleName
     * @return
     */
	private Rule createRoleRule(String ruleName) {
		Rule newRule = new Rule();
		newRule.setName(ruleName);
		RuleRole newRuleRole = new RuleRole();
		newRuleRole.setName(ruleName);
		newRuleRole.setRoleCode(txtRule2.getText());
		newRule.setRuleRole(newRuleRole);
		return newRule;
	}

	private Rule createProfileRuleEmptyStaticRoleAndStatus(String ruleName) {
		Rule newRule = new Rule();
		newRule.setName(ruleName);
		RuleProfile ruleProfile = createBaseRuleProfile(ruleName);		
		newRule.setRuleProfile(ruleProfile);
		return newRule;
	}

	private Rule createProfileRuleEmptyStaticRole(String ruleName, LinkedStatus linkedStatus) {
		Rule newRule = new Rule();
		String newRuleName = ruleName + RulesUtility.RULE_NAME_SUFFIX + linkedStatus.getLinkedStatusId();
		newRule.setName(newRuleName);
		RuleProfile ruleProfile = createBaseRuleProfile(newRuleName);
		ruleProfile.setLinkedStatusId(linkedStatus.getLinkedStatusId());
		newRule.setRuleProfile(ruleProfile);
		return newRule;
	}

	private Rule createProfileRuleEmptyStatus(String ruleName, StaticRole staticRole) {
		Rule newRule = new Rule();
		String newRuleName = ruleName + RulesUtility.RULE_NAME_SUFFIX + staticRole.getRoleCode();
		newRule.setName(newRuleName);
		RuleProfile ruleProfile = createBaseRuleProfile(newRuleName);
		ruleProfile.setRoleCode(staticRole.getRoleCode());
		//linked status is empty
		newRule.setRuleProfile(ruleProfile);

		return newRule;
	}
	
	private Rule createProfileRule(String ruleName, LinkedStatus linkedStatus, StaticRole staticRole) {
		Rule newRule = new Rule();
		String newRuleName = ruleName + RulesUtility.RULE_NAME_SUFFIX + linkedStatus.getLinkedStatusId() + "_" + staticRole.getRoleCode();
		newRule.setName(newRuleName);
		RuleProfile ruleProfile = createBaseRuleProfile(newRuleName);
		ruleProfile.setLinkedStatusId(linkedStatus.getLinkedStatusId());
		ruleProfile.setRoleCode(staticRole.getRoleCode());
		newRule.setRuleProfile(ruleProfile);
		return newRule;
	}

	private RuleProfile createBaseRuleProfile(String newRuleName) {
		RuleProfile ruleProfile = new RuleProfile();
		ruleProfile.setName(newRuleName);
		ruleProfile.setProfileAttributeCode(txtRule2Code);
		ruleProfile.setTargetAttributeCode(txtRule3Code);
		ruleProfile.setLinkAttributeCode(txtRule4Code);
		ruleProfile.setIntermedAttributeCode(txtRule5Code);
		return ruleProfile;
	}

	private Rule createPersonRule(String ruleName, LinkedStatus linkedStatus, StaticRole staticRole) {
		Rule newRule = new Rule();
		String newRuleName = ruleName + RulesUtility.RULE_NAME_SUFFIX + linkedStatus.getLinkedStatusId() + "_" + staticRole.getRoleCode();
		newRule.setName(newRuleName);
		RulePerson newRulePerson = createBaseRulePerson(newRuleName);
		newRulePerson.setLinkedStatusId(linkedStatus.getLinkedStatusId());

		newRulePerson.setRoleCode(staticRole.getRoleCode());
		newRule.setRulePerson(newRulePerson);
		return newRule;
	}

	private Rule createPersonRuleEmptyStaticRole(String ruleName, LinkedStatus linkedStatus) {
		Rule rule = new Rule();
		String newRuleName = ruleName + RulesUtility.RULE_NAME_SUFFIX + linkedStatus.getLinkedStatusId();
		rule.setName(newRuleName);
		RulePerson newRulePerson = createBaseRulePerson(newRuleName);
		newRulePerson.setLinkedStatusId(linkedStatus.getLinkedStatusId());
//		static role is empty
		rule.setRulePerson(newRulePerson);
		return rule;
	}
	private Rule createPersonRuleEmptyStatus(String ruleName, StaticRole staticRole) {
		Rule newRule = new Rule();
		String newRuleName = ruleName + RulesUtility.RULE_NAME_SUFFIX + staticRole.getRoleCode();
		newRule.setName(newRuleName);
		RulePerson newRulePerson = createBaseRulePerson(newRuleName);
		newRulePerson.setRoleCode(staticRole.getRoleCode());
		//linked status is empty
		newRule.setRulePerson(newRulePerson);
		return newRule;
	}

	private Rule createPersonRuleEmptyStaticRoleAndStatus(String ruleName) {
		Rule newRule = new Rule();
		newRule.setName(ruleName);
		RulePerson newRulePerson = createBaseRulePerson(ruleName);
		//linked status and static role are empty
		newRule.setRulePerson(newRulePerson);
		return newRule;
	}

	/** 
	 * Creates {@link RulePerson} and fills it base attributes (all but except static role and linked status)
	 * @param ruleName
	 * @return
	 */	
	private RulePerson createBaseRulePerson(String ruleName) {
		RulePerson newRulePerson = new RulePerson();
		newRulePerson.setName(ruleName);
		newRulePerson.setPersonAttributeCode(txtRule2Code);
		newRulePerson.setLink(txtRule3Code);
		newRulePerson.setIntermedAttributeCode(txtRule4Code);
		return newRulePerson;
	}
	
	/**
	 * Retrieve {@link Permission}s for the specified {@link Rule}.
	 * @param localRuleName
	 * @return
	 */
    private List<Permission> getExistingPermissionsForRule(String localRuleName) {
        List<Permission> permissions = RulesUtility.getPermissionsByRuleName(localRuleName, m_template);
        return permissions;
    }

    private void addNewPermissions(List<Rule> newRules, List<Permission> permissions, Set<Permission> allBaseTemplatePermission) {
        int count = 0;
        for (Rule newRule : newRules) {
            for (Permission permission : permissions) {
                Permission clonedPermission = PermissionCloner.clonePermission(permission);
                clonedPermission.setRule(newRule.getName());
                clonedPermission.generatePermHashes(m_template);
                
                Permission blankPermision = PermissionCloner.clonePermission(clonedPermission);
                blankPermision.setOperations(new Operations());
                blankPermision.setWfMoves(new WfMoves());
                allBaseTemplatePermission.add(blankPermision);
                
                for(Operation o : clonedPermission.getOperations().getOperations()){
                	if(!Action.REMOVE.equals(o.getAction())){
                		o.setAction(Action.ADD);
                	}
                }
                for(WfMove w : clonedPermission.getWfMoves().getWfMove()){
                	if(!Action.REMOVE.equals(w.getAction())){
                		w.setAction(Action.ADD);
                	}
                }
                logger.debug("Added permission: " + clonedPermission);
                m_template.addPermission(clonedPermission);
                count++;
            }

        }
        logger.debug("Added Permissions: " + count);
    }

    private void deleteOldPermissions(List<Rule> oldRules) {
        for (Rule rule : oldRules) {
            if (rule != null) {
                List<Permission> oldPermissionsToDelete = RulesUtility.getPermissionsByRuleName(rule.getName(), m_template);
                int deleted = deletePermissions(oldPermissionsToDelete);
                logger.debug("Deleted permissions: " + deleted);
            }
        }
    }

    private void addNewRules(List<Rule> newRules) {
        for (Rule rule : newRules) {
            m_template.getRules().addRule(rule);
            logger.debug("Adding rule: " + rule.getName());
        }

    }

    private void deleteOldRules(List<Rule> rules, Template template) {
        RulesUtility.deleteRules(rules, template);
    }

    private int deletePermissions(List<Permission> oldPermissions) {
        int removed = 0;
        for (Permission permission : oldPermissions) {
            removed += RulesUtility.deletePermission(permission, m_template);
        }
        return removed;
    }

    public void removerPermission(Permission permission, Template template) {
        if (permission != null) {
            for (int i = 0; i < template.getPermissionCount(); i++) {
                if (permission.getRule() != null
                        && permission.getRule().equals(template.getPermission(i).getRule())
                        && permission.getStatus() != null
                        && permission.getStatus().equals(template.getPermission(i).getStatus())) {
                    template.removePermission(i);
                    logger.debug("Deleting permission: " + permission);
                    i--;
                }
            }
        }
    }

    /**
     * ����� ��������� ����� �������������� ������ ��� �� ��������.
     * � ��� ������ ����� ������� ����������, ����� ���������� ������ �� ��������(���������� �����), �������
     * ������������� ����� �����.
     *
     * @param e WindowEvent
     */
    public void this_windowOpened(WindowEvent e) {
        //���� ������� ���������� ��������� ��� � ����������� ��� ����
    	FindAttributeName attributeNameHelper = new FindAttributeName(m_config);
    	
        Rule editedRule = null;
        if (rules != null && rules.size() > 0) {
            editedRule = rules.get(0);
        }

        if (editedRule != null) {
            currentName = editedRule.getName();
            txtRuleName.setText(RulesUtility.getRuleName(editedRule.getName()));

            //��� "�������"
            if (m_indexChoiceRule == 1) {
                txtRule2Code = editedRule.getRulePerson().getPersonAttributeCode();
                txtRule2.setText(txtRule2Code!=null?attrNames.getAttributeName(txtRule2Code)+" / "+txtRule2Code:attrNames.getAttributeName(txtRule2Code));
                txtRule3Code = editedRule.getRulePerson().getLink();
                txtRule3.setText(txtRule3Code!=null?attrNames.getAttributeName(txtRule3Code)+" / "+txtRule3Code:attrNames.getAttributeName(txtRule3Code));
                txtRule4Code = editedRule.getRulePerson().getIntermedAttributeCode();
                txtRule4.setText(txtRule4Code!=null?attrNames.getAttributeName(txtRule4Code)+" / "+txtRule4Code:attrNames.getAttributeName(txtRule4Code));

                updatePersonLinkedStatusList();
                
            	updatePersonStaticRoleList(attributeNameHelper);
                          	
                panelInputControls.add(jLabel6, new XYConstraints(5, 125, 120, 50));
                panelInputControls.add(jLabel7, new XYConstraints(5, 180, 120, 25));
                panelInputControls.add(paneListRule6, new XYConstraints(130, 125, 250, 50));

                panelInputControls.add(staticRolesScrollPane, new XYConstraints(130, 180, 250, 50));
                panelInputControls.add(jbtnEllipse5, new XYConstraints(379, 125, 25, 50));
                panelInputControls.add(jbtnEllipse6, new XYConstraints(379, 180, 25, 50));
                panelInputControls.add(jbtnC5, new XYConstraints(403, 125, 38, 50));
                panelInputControls.add(jbtnC6, new XYConstraints(403, 180, 38, 50));
            } else if (m_indexChoiceRule == 2) { //��� "����"
                txtRule2.setText(editedRule.getRuleRole().getRoleCode());
            } else if (m_indexChoiceRule == 3) { //��� "�������"
            	List<String> profileStatusIds = RulesUtility.getProfileLinkedStatusIds(rules);
            	
                txtRule2Code = editedRule.getRuleProfile().getProfileAttributeCode();
                txtRule2.setText(txtRule2Code!=null?attrNames.getAttributeName(txtRule2Code)+" / "+txtRule2Code:attrNames.getAttributeName(txtRule2Code));
                txtRule3Code = editedRule.getRuleProfile().getTargetAttributeCode();
                txtRule3.setText(txtRule3Code!=null?attrNames.getAttributeName(txtRule3Code)+" / "+txtRule3Code:attrNames.getAttributeName(txtRule3Code));
                txtRule4Code = editedRule.getRuleProfile().getLinkAttributeCode();
                txtRule4.setText(txtRule4Code!=null?attrNames.getAttributeName(txtRule4Code)+" / "+txtRule4Code:attrNames.getAttributeName(txtRule4Code));
                txtRule5Code = editedRule.getRuleProfile().getIntermedAttributeCode();
                txtRule5.setText(txtRule5Code!=null?attrNames.getAttributeName(txtRule5Code)+" / "+txtRule5Code:attrNames.getAttributeName(txtRule5Code));

                updateProfileLinkedStatusList();
                
            	updateProfileStaticRoleList(attributeNameHelper);
				                
            } else if (m_indexChoiceRule == 4) { //��� "�������������"
                txtRule2Code = editedRule.getRuleDelegation().getLinkAttributeCode();
                txtRule2.setText(txtRule2Code!=null?attrNames.getAttributeName(attrNames.getAttributeName(txtRule2Code))+" / "+txtRule2Code:attrNames.getAttributeName(attrNames.getAttributeName(txtRule2Code)));
            }
        }
        //��� ������� ���� ������� ������� ���� ����� ���������
        //��� "�������"
        if (m_indexChoiceRule == 1) {
            jLabel2.setText("������� ������������");
            jLabel3.setText("������� �����");
            jLabel4.setText("������� ������������� �����");
            jLabel6.setText("������� ������");
            jLabel7.setText("����������� ����");
            if (editedRule == null) {
                txtRule2.setText("");
                txtRule3.setText("");
                txtRule4.setText("");
                jbtnEllipse6.setEnabled(false);
                jbtnC6.setEnabled(false);
                linkedStatusIdsList.setEnabled(false);
                staticRolesJList.setEnabled(false);
                jbtnEllipse5.setEnabled(false);
                jbtnC5.setEnabled(false);

                panelInputControls.add(jLabel6, new XYConstraints(5, 125, 120, STATUS_LINK_HEIGHT));
                panelInputControls.add(jLabel7, new XYConstraints(5, 185, 120, 25));

                panelInputControls.add(paneListRule6, new XYConstraints(130, 125, 250, STATUS_LINK_HEIGHT));
                panelInputControls.add(staticRolesScrollPane, new XYConstraints(130, 185, 250, STATUS_LINK_HEIGHT));
                panelInputControls.add(jbtnEllipse5, new XYConstraints(379, 125, 25, STATUS_LINK_HEIGHT));
                panelInputControls.add(jbtnEllipse6, new XYConstraints(379, 185, 25, STATUS_LINK_HEIGHT));
                panelInputControls.add(jbtnC5, new XYConstraints(403, 125, 38, STATUS_LINK_HEIGHT));
                panelInputControls.add(jbtnC6, new XYConstraints(403, 185, 38, STATUS_LINK_HEIGHT));

            }
            jLabel5.setVisible(false);
            txtRule5.setVisible(false);
            jbtnEllipse4.setVisible(false);
            jbtnC4.setVisible(false);
        } else if (m_indexChoiceRule == 2) { //��� "����"
            jLabel2.setText("������������� ����");
            if (editedRule == null) {
                txtRule2.setText("");
            }

            hideComponent();

        } else if (m_indexChoiceRule == 3) { //��� "�������"
            jLabel2.setText("������� �������");
            jLabel3.setText("��������� �������");
            jLabel4.setText("������� �����");
            jLabel5.setText("������� ������������� �����");
            jLabel6.setText("������� ������");
            jLabel7.setText("����������� ����");
            if (editedRule == null) {
                txtRule2.setText("");
                txtRule3.setText("");
                txtRule4.setText("");
                txtRule5.setText("");
                staticRolesJList.setEnabled(false);
                jbtnEllipse6.setEnabled(false);
                jbtnC6.setEnabled(false);
                linkedStatusIdsList.setEnabled(false);
                staticRolesJList.setEnabled(false);
                jbtnEllipse5.setEnabled(false);
                jbtnC5.setEnabled(false);
            }
        } else if (m_indexChoiceRule == 4) { //��� "�������������"
            jLabel2.setText("������� �����");
            if (editedRule == null) {
                txtRule2.setText("");
            }

            hideComponent();
        }
    }

	/**
	 * ��������� ���������� staticRolesJList ��� ����������� �������.
	 */
	private void updateProfileStaticRoleList(FindAttributeName attributeNameHelper) {
		List<String> personStaticRoleCodes = RulesUtility.getProfileStaticRoleCodes(rules);
		Map<String, String> uniqueRoleNameToRoleCode = new HashMap<String, String>();            	
		for (String roleCode : personStaticRoleCodes) {
			String roleName = attributeNameHelper.getAttributeName(roleCode);
			uniqueRoleNameToRoleCode.put(roleName, roleCode);                	
		}

		for (String roleName : uniqueRoleNameToRoleCode.keySet()) {
			String roleCode = uniqueRoleNameToRoleCode.get(roleName);
			((DefaultListModel) staticRolesJList.getModel()).addElement(new StaticRole(roleCode, roleName));
		}
	}

	/**
	 * ��������� ���������� linkedStatusIdsList ��� ����������� �������.
	 */
	private void updateProfileLinkedStatusList() {
		List<String> linkedStatusIds = RulesUtility.getProfileLinkedStatusIds(rules);                
		Map<String, String> uniqueStatusNameToStatusId = new HashMap<String, String>();            	
		for (String statusId : linkedStatusIds) {
			String statusName = AccessConfigUtility.getStatusNameByStatusId(statusId, m_config);                	
			uniqueStatusNameToStatusId.put(statusName, statusId);                	
		}
		for(String statusName : uniqueStatusNameToStatusId.keySet()){
			String statusId = uniqueStatusNameToStatusId.get(statusName);
		    ((DefaultListModel) linkedStatusIdsList.getModel()).addElement(new LinkedStatus(statusId, statusName));                	
		}
	}

	/**
	 * ��������� ���������� staticRolesJList ��� ������������� �������.
	 */
	private void updatePersonStaticRoleList(FindAttributeName attributeNameHelper) {
		List<String> personStaticRoleCodes = RulesUtility.getPersonStaticRoleCodes(rules);
		Map<String, String> uniqueRoleNameToRoleCode = new HashMap<String, String>();            	
		for (String roleCode : personStaticRoleCodes) {
			String roleName = attributeNameHelper.getAttributeName(roleCode);
			uniqueRoleNameToRoleCode.put(roleName, roleCode);                	
		}

		for (String roleName : uniqueRoleNameToRoleCode.keySet()) {
			String roleCode = uniqueRoleNameToRoleCode.get(roleName);
			((DefaultListModel) staticRolesJList.getModel()).addElement(new StaticRole(roleCode, roleName));
		}
	}

	/**
	 * ��������� ���������� linkedStatusIdsList ��� ������������� �������.
	 */
	private void updatePersonLinkedStatusList() {
		List<String> linkedStatusIds = RulesUtility.getPersonLinkedStatusIds(rules);                
		Map<String, String> uniqueStatusNameToStatusId = new HashMap<String, String>();            	
		for (String statusId : linkedStatusIds) {
			String statusName = AccessConfigUtility.getStatusNameByStatusId(statusId, m_config);                	
			uniqueStatusNameToStatusId.put(statusName, statusId);                	
		}

		for(String statusName : uniqueStatusNameToStatusId.keySet()){
			String statusId = uniqueStatusNameToStatusId.get(statusName);
		    ((DefaultListModel) linkedStatusIdsList.getModel()).addElement(new LinkedStatus(statusId, statusName));                	
		}
	}

    private StaticRole initStaticRole(String roleCode) {
        StaticRole staticRole = new StaticRole(roleCode, attrNames.getAttributeName(roleCode));
        return staticRole;
    }


    public void jbtnEllipse1_actionPerformed(ActionEvent e) {
        RuleFormAttribute ruleFormAttribute = new RuleFormAttribute(par,
                m_config, m_template,
                txtRuleName.getText() + " - " + txtRule2.getText(),
                txtRule2Code, 1,
                comboBoxIndex,
                m_indexChoiceRule);

        ruleFormAttribute.setVisible(true);
        txtRule2Code = ruleFormAttribute.returnString;
        if (m_indexChoiceRule == 2) { //��� "����"
            txtRule2.setText(txtRule2Code);
        } else {
            txtRule2.setText(txtRule2Code!=null?attrNames.getAttributeName(txtRule2Code)+" / "+txtRule2Code:attrNames.getAttributeName(txtRule2Code));
        }

    }

    public void jbtnEllipse2_actionPerformed(ActionEvent e) {
        RuleFormAttribute ruleFormAttribute = new RuleFormAttribute(par,
                m_config, m_template, txtRuleName.getText()
                        + " - " + txtRule3.getText(),
                txtRule3Code, 2, comboBoxIndex, m_indexChoiceRule);
        ruleFormAttribute.setVisible(true);
        txtRule3Code = ruleFormAttribute.returnString;
        txtRule3.setText(txtRule3Code!=null?attrNames.getAttributeName(txtRule3Code)+" / "+txtRule3Code:attrNames.getAttributeName(txtRule3Code));
    }

    public void jbtnEllipse3_actionPerformed(ActionEvent e) {
        RuleFormAttribute ruleFormAttribute = new RuleFormAttribute(par,
                m_config, m_template, txtRuleName.getText()
                        + " - " + txtRule4.getText(),
                txtRule4Code, 3, comboBoxIndex, m_indexChoiceRule);
        ruleFormAttribute.setVisible(true);
        txtRule4Code = ruleFormAttribute.returnString;
        txtRule4.setText(txtRule4Code!=null?attrNames.getAttributeName(txtRule4Code)+" / "+txtRule4Code:attrNames.getAttributeName(txtRule4Code));
    }

    public void jbtnEllipse4_actionPerformed(ActionEvent e) {
        RuleFormAttribute ruleFormAttribute = new RuleFormAttribute(par, m_config, m_template,
                txtRuleName.getText() + " - " + txtRule5.getText(),
                txtRule5Code, 4, comboBoxIndex, m_indexChoiceRule);
        ruleFormAttribute.setVisible(true);
        txtRule5Code = ruleFormAttribute.returnString;
        txtRule5.setText(txtRule5Code!=null?attrNames.getAttributeName(txtRule5Code)+" / "+txtRule5Code:attrNames.getAttributeName(txtRule5Code));
    }

    public void jbtnEllipse5_actionPerformed(ActionEvent e) {
        StatusFormAttribute statusFormAttribute = new StatusFormAttribute(par, m_config, m_template,
                txtRuleName.getText(), "", 5);
        statusFormAttribute.setVisible(true);
        List<LinkedStatus> linkedStatuses = statusFormAttribute.getSelectedStatuses();

        for (LinkedStatus linkedStatus : linkedStatuses) {
            if (!containsElement(linkedStatusIdsList, linkedStatus)) {
                ((DefaultListModel) linkedStatusIdsList.getModel()).addElement(linkedStatus);
            }
        }
    }

    public void jbtnEllipse6_actionPerformed(ActionEvent e) {

        RuleFormAttribute ruleFormAttribute = new RuleFormAttribute(par, m_config, m_template,
                txtRuleName.getText(),
                "", 6, comboBoxIndex, m_indexChoiceRule);
        ruleFormAttribute.setVisible(true);

        List<StaticRole> selectedStaticRoles = ruleFormAttribute.getSelectedStaticRoles();
        for (StaticRole selectedStaticRole : selectedStaticRoles) {

            if (!staticRolesListModel.contains(selectedStaticRole)) {
            	((DefaultListModel) staticRolesJList.getModel()).addElement(selectedStaticRole);
            }
        }
    }    

    /**
     * Defines whether {@link JList} contains specified element.
     *
     * @param list
     * @param element
     * @return <code>true</code> if contains, <code>false</code> othrewise.
     */
    private boolean containsElement(JList list, LinkedStatus element) {
        List<LinkedStatus> listItems = getSelectedLinkedStatuses(list);
        return listItems.contains(element);
    }

    /**
     * Retreives all elements from JList and return them as List.
     *
     * @param list
     * @return
     */
    private List<LinkedStatus> getSelectedLinkedStatuses(JList list) {
        List<LinkedStatus> listItems = new ArrayList<LinkedStatus>();
        for (int i = 0; i < list.getModel().getSize(); i++) {
            LinkedStatus item = (LinkedStatus) list.getModel().getElementAt(i);
            listItems.add(item);
        }
        return listItems;
    }

    private List<StaticRole> getSelectedStaticRoles(JList list) {
        List<StaticRole> listItems = new ArrayList<StaticRole>();
        for (int i = 0; i < list.getModel().getSize(); i++) {
        	StaticRole item = (StaticRole) list.getModel().getElementAt(i);
            listItems.add(item);
        }
        return listItems;
    }

    private <T> List<T> getListItems(JList list) {
        List<T> listItems = new ArrayList<T>();
        for (int i = 0; i < list.getModel().getSize(); i++) {
            T item = (T) list.getModel().getElementAt(i);
            listItems.add(item);
        }
        return listItems;
    }

    public void jbtnC1_actionPerformed(ActionEvent e) {
        txtRule2.setText("");
    }

    public void jbtnC2_actionPerformed(ActionEvent e) {
        txtRule3.setText("");
        txtRule3Code = null;
    }

    public void jbtnC3_actionPerformed(ActionEvent e) {
        txtRule4.setText("");
        txtRule4Code = null;
    }

    public void jbtnC4_actionPerformed(ActionEvent e) {
        txtRule5.setText("");
        txtRule5Code = null;
    }

    public void jbtnC5_actionPerformed(ActionEvent e) {
        ((DefaultListModel) linkedStatusIdsList.getModel()).clear();
    }

    public void jbtnC6_actionPerformed(ActionEvent e) {
        staticRolesListModel.clear();
    }

    /**
     * ����� ��������� ���������� �� �����
     *
     * @throws Exception
     */
    private void jbInit() throws Exception {
        jLabel1.setText("������������"); //��������� ������ �������
        jbtnEllipse1.setBorder(BorderFactory.createLineBorder(Color.gray));
        jbtnEllipse1.setText("...");
        jbtnEllipse1.addActionListener(new RulesForm_jbtnEllipse1_actionAdapter(this));
        jbtnEllipse2.setBorder(BorderFactory.createLineBorder(Color.gray));
        jbtnEllipse2.setText("...");
        jbtnEllipse2.addActionListener(new RulesForm_jbtnEllipse2_actionAdapter(this));
        jbtnEllipse3.setBorder(BorderFactory.createLineBorder(Color.gray));
        jbtnEllipse3.setText("...");
        jbtnEllipse3.addActionListener(new RulesForm_jbtnEllipse3_actionAdapter(this));
        jbtnEllipse4.setBorder(BorderFactory.createLineBorder(Color.gray));
        jbtnEllipse4.setText("...");
        jbtnEllipse4.addActionListener(new RulesForm_jbtnEllipse4_actionAdapter(this));
        jbtnEllipse5.setBorder(BorderFactory.createLineBorder(Color.black));
        jbtnEllipse5.setText("...");
        jbtnEllipse5.addActionListener(new RulesForm_jbtnEllipse5_actionAdapter(this));
        jbtnEllipse6.setBorder(BorderFactory.createLineBorder(Color.black));
        jbtnEllipse6.setText("...");
        jbtnEllipse6.addActionListener(new RulesForm_jbtnEllipse6_actionAdapter(this));
        jbtnC1.setBorder(BorderFactory.createLineBorder(Color.black));
        jbtnC1.setMinimumSize(new Dimension(25, 23));
        jbtnC1.setText("X");
        jbtnC1.addActionListener(new RulesForm_jbtnC1_actionAdapter(this));
        jbtnC2.setBorder(BorderFactory.createLineBorder(Color.black));
        jbtnC2.setMinimumSize(new Dimension(25, 23));
        jbtnC2.setText("X");
        jbtnC2.addActionListener(new RulesForm_jbtnC2_actionAdapter(this));
        jbtnC3.setBorder(BorderFactory.createLineBorder(Color.black));
        jbtnC3.setText("X");
        jbtnC3.addActionListener(new RulesForm_jbtnC3_actionAdapter(this));
        jbtnC4.setBorder(BorderFactory.createLineBorder(Color.black));
        jbtnC4.setText("X");
        jbtnC4.addActionListener(new RulesForm_jbtnC4_actionAdapter(this));
        jbtnC5.setBorder(BorderFactory.createLineBorder(Color.black));
        jbtnC5.setText("X");
        jbtnC5.addActionListener(new RulesForm_jbtnC5_actionAdapter(this));
        jbtnC6.setBorder(BorderFactory.createLineBorder(Color.black));
        jbtnC6.setText("X");
        jbtnC6.addActionListener(new RulesForm_jbtnC6_actionAdapter(this));
        //listRule6.setBorder(BorderFactory.createLineBorder(Color.black));
        linkedStatusIdsList.setBackground(getBackground());
        staticRolesJList.setBackground(getBackground());

        panelInputControls.add(jLabel1, new XYConstraints(5, 5, 120, 25));
        panelInputControls.add(jLabel2, new XYConstraints(5, 35, 120, 25));
        panelInputControls.add(jLabel3, new XYConstraints(5, 65, 120, 25));
        panelInputControls.add(jLabel4, new XYConstraints(5, 95, 120, 25));
        panelInputControls.add(jLabel5, new XYConstraints(5, 125, 120, 25));
        panelInputControls.add(jLabel6, new XYConstraints(5, 155, 120, 50));
        panelInputControls.add(jLabel7, new XYConstraints(5, 210, 120, 25));
        //� ��� ����� ������ ��������� �� �������
        jLabel2.setText("");
        jLabel3.setText("");
        jLabel4.setText("");
        jLabel5.setText("");
        jLabel6.setText("");
        jLabel7.setText("");
        //�������� �������� ����������� ���� ��� ��������� �������
        panelInputControls.add(txtRuleName, new XYConstraints(130, 5, 313, 25));
        panelInputControls.add(txtRule2, new XYConstraints(130, 35, 250, 25));
        panelInputControls.add(txtRule3, new XYConstraints(130, 65, 250, 25));
        panelInputControls.add(txtRule4, new XYConstraints(130, 95, 250, 25));
        panelInputControls.add(txtRule5, new XYConstraints(130, 125, 250, 25));

        panelInputControls.add(paneListRule6, new XYConstraints(130, 155, 250, 50));

        panelInputControls.add(staticRolesScrollPane, new XYConstraints(130, 210, 250, 50));

        txtRuleName.getDocument().addDocumentListener(this);
        txtRule2.getDocument().addDocumentListener(this);
        txtRule3.getDocument().addDocumentListener(this);
        txtRule4.getDocument().addDocumentListener(this);

        panelInputControls.add(jbtnEllipse1, new XYConstraints(379, 35, 25, 25));
        panelInputControls.add(jbtnEllipse2, new XYConstraints(379, 65, 25, 25));
        panelInputControls.add(jbtnEllipse3, new XYConstraints(379, 95, 25, 25));
        panelInputControls.add(jbtnEllipse4, new XYConstraints(379, 125, 25, 25));
        panelInputControls.add(jbtnEllipse5, new XYConstraints(379, 155, 25, 50));
        panelInputControls.add(jbtnEllipse6, new XYConstraints(379, 210, 25, 50));
        panelInputControls.add(jbtnC1, new XYConstraints(403, 35, 38, 25));
        panelInputControls.add(jbtnC2, new XYConstraints(403, 65, 38, 25));
        panelInputControls.add(jbtnC3, new XYConstraints(403, 95, 38, 25));
        panelInputControls.add(jbtnC4, new XYConstraints(403, 125, 38, 25));
        panelInputControls.add(jbtnC5, new XYConstraints(403, 155, 38, 50));
        panelInputControls.add(jbtnC6, new XYConstraints(403, 210, 38, 50));

        txtRuleName.setText("");
        txtRule2.setText("");
        txtRule2.setEditable(false);
        txtRule3.setText("");
        txtRule3.setEditable(false);
        txtRule4.setText("");
        txtRule4.setEditable(false);
        txtRule5.setText("");
        txtRule5.setEditable(false);
        linkedStatusIdsList.setEnabled(false);
        ((DefaultListModel) linkedStatusIdsList.getModel()).clear();

        staticRolesJList.setEnabled(false);
        ((DefaultListModel) staticRolesJList.getModel()).clear();

        //jbtnEllipse5.setEnabled(false);
        //jbtnC5.setEnabled(false);

        this.addWindowListener(new RulesForm_this_windowAdapter(this));
    }

    private void closeTextField(String tf4, String tf2, String tf3,
                                int indexRule) {
        //�������� ����� ��� ������� �������
        if (indexRule == 1) {
            //���� ������� ������������ ������
            if (tf2 == null || tf2.equals("")) {
                staticRolesJList.setEnabled(false);
                staticRolesListModel.clear();
                jbtnEllipse6.setEnabled(false);
                jbtnC6.setEnabled(false);
            } else {
                staticRolesJList.setEnabled(true);
                jbtnEllipse6.setEnabled(true);
                jbtnC6.setEnabled(true);
            }
            if (tf3 == null || tf3.equals("")) {
                linkedStatusIdsList.setEnabled(false);
                jbtnEllipse5.setEnabled(false);
                jbtnC5.setEnabled(false);
            } else {
                linkedStatusIdsList.setEnabled(true);
                jbtnEllipse5.setEnabled(true);
                jbtnC5.setEnabled(true);
            }

        }

        if (indexRule == 3) {
            if (tf3 == null || tf3.equals("")) {
                staticRolesJList.setEnabled(false);
                staticRolesListModel.clear();
                jbtnEllipse6.setEnabled(false);
                jbtnC6.setEnabled(false);
            } else {
                staticRolesJList.setEnabled(true);
                jbtnEllipse6.setEnabled(true);
                jbtnC6.setEnabled(true);
            }
            if (tf4 == null || tf4.equals("")) {
                linkedStatusIdsList.setEnabled(false);
                ((DefaultListModel) linkedStatusIdsList.getModel()).clear();
                jbtnEllipse5.setEnabled(false);
                jbtnC5.setEnabled(false);
            } else {
                linkedStatusIdsList.setEnabled(true);
                jbtnEllipse5.setEnabled(true);
                jbtnC5.setEnabled(true);
            }

        }
    }

    private void showErrorMessage(String message, String title) {
        JOptionPane.showConfirmDialog(this,
                message, title,
                JOptionPane.CLOSED_OPTION,
                JOptionPane.WARNING_MESSAGE);
    }

    private boolean isValidName(String name) {
        if (isEmpty(name)) {
            JOptionPane.showConfirmDialog(this,
                    "��� �������� �� ����� ���� ������!"
                    , "������ �����!!!",
                    JOptionPane.CLOSED_OPTION,
                    JOptionPane.ERROR_MESSAGE);
            return false;

        }
        for (int i = 0; i < m_template.getRules().getRuleCount(); i++) {
            if (name.equals(m_template.getRules().getRule(i).getName())
                    && !currentName.equals(name)) {
                JOptionPane.showConfirmDialog(this,
                        "����� ��� ��� ����������!\n\r ������� ������!"
                        , "������ �����!!!",
                        JOptionPane.CLOSED_OPTION,
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    private void hideComponent() {
        jLabel3.setVisible(false);
        jLabel4.setVisible(false);
        jLabel5.setVisible(false);
        txtRule3.setVisible(false);
        txtRule4.setVisible(false);
        txtRule5.setVisible(false);

        linkedStatusIdsList.setVisible(false);
        paneListRule6.setVisible(false);
        staticRolesScrollPane.setVisible(false);
        jbtnEllipse2.setVisible(false);
        jbtnEllipse3.setVisible(false);
        jbtnEllipse4.setVisible(false);
        jbtnEllipse5.setVisible(false);
        jbtnEllipse6.setVisible(false);
        jbtnC2.setVisible(false);
        jbtnC3.setVisible(false);
        jbtnC4.setVisible(false);
        jbtnC5.setVisible(false);
        jbtnC6.setVisible(false);
    }
}


class RulesForm_jbtnC6_actionAdapter implements ActionListener {
    private RulesForm adaptee;

    RulesForm_jbtnC6_actionAdapter(RulesForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnC6_actionPerformed(e);
    }
}


class RulesForm_jbtnC5_actionAdapter implements ActionListener {
    private RulesForm adaptee;

    RulesForm_jbtnC5_actionAdapter(RulesForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnC5_actionPerformed(e);
    }
}


class RulesForm_jbtnEllipse6_actionAdapter implements ActionListener {
    private RulesForm adaptee;

    RulesForm_jbtnEllipse6_actionAdapter(RulesForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnEllipse6_actionPerformed(e);
    }
}


class RulesForm_jbtnEllipse5_actionAdapter implements ActionListener {
    private RulesForm adaptee;

    RulesForm_jbtnEllipse5_actionAdapter(RulesForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnEllipse5_actionPerformed(e);
    }
}


class RulesForm_jbtnC4_actionAdapter implements ActionListener {
    private RulesForm adaptee;

    RulesForm_jbtnC4_actionAdapter(RulesForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnC4_actionPerformed(e);
    }
}


class RulesForm_jbtnC3_actionAdapter implements ActionListener {
    private RulesForm adaptee;

    RulesForm_jbtnC3_actionAdapter(RulesForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnC3_actionPerformed(e);
    }
}


class RulesForm_jbtnC2_actionAdapter implements ActionListener {
    private RulesForm adaptee;

    RulesForm_jbtnC2_actionAdapter(RulesForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnC2_actionPerformed(e);
    }
}


class RulesForm_jbtnC1_actionAdapter implements ActionListener {
    private RulesForm adaptee;

    RulesForm_jbtnC1_actionAdapter(RulesForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnC1_actionPerformed(e);
    }
}


class RulesForm_jbtnEllipse4_actionAdapter implements ActionListener {
    private RulesForm adaptee;

    RulesForm_jbtnEllipse4_actionAdapter(RulesForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnEllipse4_actionPerformed(e);
    }
}


class RulesForm_jbtnEllipse3_actionAdapter implements ActionListener {
    private RulesForm adaptee;

    RulesForm_jbtnEllipse3_actionAdapter(RulesForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnEllipse3_actionPerformed(e);
    }
}


class RulesForm_jbtnEllipse2_actionAdapter implements ActionListener {
    private RulesForm adaptee;

    RulesForm_jbtnEllipse2_actionAdapter(RulesForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnEllipse2_actionPerformed(e);
    }
}


class RulesForm_jbtnEllipse1_actionAdapter implements ActionListener {
    private RulesForm adaptee;

    RulesForm_jbtnEllipse1_actionAdapter(RulesForm adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jbtnEllipse1_actionPerformed(e);
    }
}


class RulesForm_this_windowAdapter extends WindowAdapter {
    private RulesForm adaptee;

    RulesForm_this_windowAdapter(RulesForm adaptee) {
        this.adaptee = adaptee;
    }

    public void windowOpened(WindowEvent e) {
        adaptee.this_windowOpened(e);
    }
}

    class DeletePopupMenuAdapter extends MouseAdapter {
        private JList adaptee;
        private JPopupMenu menu;

        DeletePopupMenuAdapter(JList adaptee, JPopupMenu menu) {
            this.adaptee = adaptee;
            this.menu = menu;
        }

        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        private void showPopup(MouseEvent e) {
            int rowAtPoint = adaptee.locationToIndex(e.getPoint());
            if (rowAtPoint >= 0 && rowAtPoint < adaptee.getModel().getSize()) {
                adaptee.setSelectionInterval(rowAtPoint, rowAtPoint);
            } else {
                adaptee.clearSelection();
            }

            int rowindex = adaptee.getSelectedIndex();
            if (rowindex < 0) {
                return;
            }

            if (e.isPopupTrigger() && e.getComponent() instanceof JList) {
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
