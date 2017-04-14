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
package org.aplana.br4j.dynamicaccess.db_import;

import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.EditConfigMainForm;
import org.aplana.br4j.dynamicaccess.db_export.CancelException;
import org.aplana.br4j.dynamicaccess.db_export.DbException;
import org.aplana.br4j.dynamicaccess.db_export.objects.ConnectionFactory;
import org.aplana.br4j.dynamicaccess.db_export.objects.TemplateDao;
import org.aplana.br4j.dynamicaccess.xmldef.*;

public class DoImportTemplate extends SwingWorker<Exception, Void> {

	private static final String TEMPLATE_NAME = "����������� (������� �������)";

	protected final Log logger = LogFactory.getLog(getClass());
    public AccessConfig config = null;
    private DataSource dataSource;

    private Template template = null;

    public DoImportTemplate(String url, String username, String password, AccessConfig ac) throws DbException {
        config = ac;
    	dataSource = ConnectionFactory.getDataSource(url, username, password);
    }

    public DoImportTemplate(String url, String username, String password, AccessConfig ac, String templateName, String templateId) throws DbException {
        config = ac;
    	dataSource = ConnectionFactory.getDataSource(url, username, password);
    	this.template = TemplateDao.createTemplate(templateName, templateId);
    }

    @Override
	protected Exception doInBackground() throws Exception {
		if (config == null) {
			throw new IllegalArgumentException("AccessConfig is null");
		}
		try {
			TemplateDao templateDao = new TemplateDao(getDataSource());
			logger.debug("Loading " + template + " template.");
			if (isCancelled()) {
				throw new CancelException("������� �� ���� ����������� �������������");
			}
			loadTemplateData(template, templateDao);
			logger.debug("Template loaded: " + template);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return e;
		}
		return null;
	}

	protected void loadTemplateData(Template template, TemplateDao templateDao) throws DbException {
		loadWorkflowForTemplate(template, templateDao);
		loadStatusForTemplate(template, templateDao);
		loadAttributeRuleForTemplate(template, templateDao);
		loadPersonRuleForTemplate(template, templateDao);
		loadRoleForTemplate(template, templateDao);
		loadProfileRuleForTemplate(template, templateDao);
		loadDelegationRuleForTemplate(template, templateDao);
		loadPermissionForTemplate(template, templateDao);
		
		Iterator<Template> iterator = config.getTemplateList().iterator();
		while(iterator.hasNext()){
			Template t = iterator.next();
			if(t.equals(template)){
				iterator.remove();
			}
		}
		EditConfigMainForm.allBasePermissions.remove(template.getTemplateIdLong());
		
		config.addTemplate(template);
	}

    public DataSource getDataSource() {
		return dataSource;
	}

    private void loadWorkflowForTemplate(Template template, TemplateDao templateDao) throws DbException {
    	Long start = System.currentTimeMillis();

    	List<WFMoveType> wfMoveTypeList = templateDao.getWorkflowsForTemplate(template);
		if (wfMoveTypeList != null) {
			for (WFMoveType wfMoveType : wfMoveTypeList) {
				template.addWFMoveType(wfMoveType);
			}
		}
		Long end = System.currentTimeMillis();
    }

    private void loadStatusForTemplate(Template template, TemplateDao templateDao) throws DbException {
    	List<Status> statusList = templateDao.getStatusesForTemplate(template);
		if (statusList != null) {
			for (Status status : statusList) {
				template.addStatus(status);
			}
		}
    }

    private void loadAttributeRuleForTemplate(Template template, TemplateDao templateDao) throws DbException {
    	List<AttributeRule> attributeRuleList = templateDao.getAttributeRuleForTemplate(template);
		if (attributeRuleList != null) {
			for (AttributeRule attributeRule : attributeRuleList) {
				template.addAttributeRule(attributeRule);
			}
		}
    }

    private void loadPersonRuleForTemplate(Template template, TemplateDao templateDao) throws DbException {
    	List<Rule> personRuleList = templateDao.getPersonRuleForTemplate(template);
		if (personRuleList != null) {
			for (Rule personRule : personRuleList) {
				if(personRule != null) {
					template.getRules().addRule(personRule);
				}
			}
		}
    }

    private void loadRoleForTemplate(Template template, TemplateDao templateDao) throws DbException {
    	List<Rule> roleRuleList = templateDao.getRoleRuleForTemplate(template);
		if (roleRuleList != null) {
			for (Rule roleRule : roleRuleList) {
				if(roleRule != null){
					template.getRules().addRule(roleRule);
				}
				if(roleRule == null){
					logger.debug("Rule is null");
				}
			}
		}
    }

    private void loadProfileRuleForTemplate(Template template, TemplateDao templateDao) throws DbException {
       	List<Rule> profileRuleList = templateDao.getProfileRuleForTemplate(template);
		if (profileRuleList != null) {
			for (Rule profileRule : profileRuleList) {
				if(profileRule != null){
					template.getRules().addRule(profileRule);
				}
			}
		}
    }

    private void loadDelegationRuleForTemplate(Template template, TemplateDao templateDao) throws DbException {
       	List<Rule> delagationRuleList = templateDao.getDelegationRuleForTemplate(template);
		if (delagationRuleList != null) {
			for (Rule delegationRule : delagationRuleList) {
				if(delegationRule != null) {
					template.getRules().addRule(delegationRule);
				}
			}
		}
    }

    private void loadPermissionForTemplate(Template template, TemplateDao templateDao) throws DbException {
       	Long start = System.currentTimeMillis();
    	List<Permission> permissionList = templateDao.getPermissionsForTemplate(template);
		if (permissionList != null) {
			for (Permission permission : permissionList) {
				if(permission != null) {
					template.addPermission(permission);
				}
			}
		}
		Long end = System.currentTimeMillis();
		if(TEMPLATE_NAME.equals(template.getName())){
			logger.debug("Added permission: " + template.getPermission().length + " in: " + (end - start));
		}
    }
}