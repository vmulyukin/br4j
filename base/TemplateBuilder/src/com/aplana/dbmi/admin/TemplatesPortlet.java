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
package com.aplana.dbmi.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.portlet.mvc.SimpleFormController;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.model.Workflow;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;

public class TemplatesPortlet extends SimpleFormController {
	protected final Log logger = LogFactory.getLog(getClass());
	private ResourceBundle messages;
	private String editAccessRoles;
	
	protected Object formBackingObject(PortletRequest request) throws Exception {
		ContextProvider.getContext().setLocale(request.getLocale());
		WebTemplateBean webTemplateBean = null;
		if (isSessionForm() && request.getPortletSession().getAttribute(getFormSessionAttributeName()) != null) {
			webTemplateBean = (WebTemplateBean) request.getPortletSession().getAttribute(getFormSessionAttributeName());
		} else {
			webTemplateBean = (WebTemplateBean) super.formBackingObject(request);
		}

		try {
			webTemplateBean.setChanged(false);
			AsyncDataServiceBean dataService = PortletUtil.createService(request);
			webTemplateBean.setDataService(dataService);
			webTemplateBean.setWorkflows((List) dataService.listAll(Workflow.class));
			webTemplateBean.setEditAccessRoles(editAccessRoles);

			loadTemplates(webTemplateBean, dataService);
		} catch (Exception e) {
			logger.error("Error forming backing object:", e);
			webTemplateBean.setMessage(e.getMessage());
		}
		webTemplateBean.setShowAccessSettingsWarning(false);
		return webTemplateBean;
	}

	private void loadTemplates(WebTemplateBean webTemplateBean, AsyncDataServiceBean dataService) throws DataException, ServiceException {
		List templates = new LinkedList(dataService.listAll(Template.class));
		// TODO Ad-hoc solution: sort by russian name
		Collections.sort(templates, new Comparator() {
			public int compare(Object o0, Object o1) {
				Template t0 = (Template) o0;
				Template t1 = (Template) o1;

				return t0.getNameRu().compareTo(t1.getNameRu());
			}
		});
		webTemplateBean.setTemplates(templates);
	}

	protected Map referenceData(PortletRequest request, Object command, Errors errors) {

		Map model = new HashMap();
		return model;

	}

	public void onSubmitAction(ActionRequest request, ActionResponse response, Object command, BindException errors) throws Exception {
		ContextProvider.getContext().setLocale(request.getLocale());
		messages = ResourceBundle.getBundle("templates", request.getLocale());

		WebTemplateBean templateBean = (WebTemplateBean) command;
		if (templateBean.getRealId() != null && "".equals(templateBean.getRealId().toString())) {
			templateBean.setRealId(null);
		}

		try {
			AsyncDataServiceBean dataService = templateBean.getDataService();
			if (request.getParameter("new_id") != null) {
				if (!templateBean.isEditAccessExists()){
					throw new DataException("admin.edit.access.error");
				}
				templateBean.setChanged(true);
				BeanUtils.copyProperties(new Template(), templateBean);
				templateBean.setCardAccess(new ArrayList());
				templateBean.setRealId(request.getParameter("new_id"));
				ArrayList blocks = new ArrayList();
				AttributeBlock ab = (AttributeBlock) dataService.getById(AttributeBlock.ID_COMMON);
				TemplateBlock tb = TemplateBlock.createFromAttributeBlock(ab);
//TODO ������� ����� ���������� ��������� Ace		
//				tb.setColumn(0);
				blocks.add(tb);
				templateBean.setBlocks(blocks);
				templateBean.clearBlocks();
				templateBean.getBlocksLeft().addAll(blocks);

				if (templateBean.getShowTemplate() != null && templateBean.getShowTemplate().booleanValue()) {
					Collection attributeBlocks = dataService.listAll(AttributeBlock.class);
					MyBeanUtils.removeAll(attributeBlocks, templateBean.getBlocks());
					for (Iterator it = attributeBlocks.iterator(); it.hasNext();) {
						AttributeBlock block = (AttributeBlock) it.next();
						if (!block.isActive()) {// (block.isSystem()) {
							it.remove();
						}
					}
					templateBean.setBlockItems(attributeBlocks);
				}

			} else if (request.getParameter("edit_id") != null) {
				if (!templateBean.isEditAccessExists()){
					throw new DataException("admin.edit.access.error");
				}
				templateBean.setChanged(false);
				templateBean.setShowAccessSettingsWarning(false);
				Template template = (Template) dataService.getById(new ObjectId(Template.class, request.getParameter("edit_id")));
				dataService.doAction(new LockObject(template));
				BeanUtils.copyProperties(template, templateBean, new String[] { "locker", "lockTime" });
				templateBean.setRealId(template.getId().getId());
				templateBean.clearBlocks();
				for (Iterator it = templateBean.getBlocks().iterator(); it.hasNext();) {
					TemplateBlock block = (TemplateBlock) it.next();
					templateBean.getBlockColumn(TagConstant.POS_LEFT).add(block);
//TODO ������� ����� ���������� ��������� Ace			
//					templateBean.getBlockColumn(templateBean.getPosition(block.getColumn())).add(block);
				}

				if (templateBean.getShowTemplate() != null && templateBean.getShowTemplate().booleanValue()) {
					Collection attributeBlocks = dataService.listAll(AttributeBlock.class);
					MyBeanUtils.removeAll(attributeBlocks, templateBean.getBlocks());
					for (Iterator it = attributeBlocks.iterator(); it.hasNext();) {
						AttributeBlock block = (AttributeBlock) it.next();
						if (!block.isActive()) {// (block.isSystem()) {
							it.remove();
						}
					}
					templateBean.setBlockItems(attributeBlocks);
				}

			} else if (request.getParameter("delete_id") != null) {
				if (!templateBean.isEditAccessExists()){
					throw new DataException("admin.edit.access.error");
				}

				Template template = (Template) dataService.getById(new ObjectId(Template.class, request.getParameter("delete_id")));
				dataService.doAction(new LockObject(template));
				template.setActive(false);
				dataService.saveObject(template, ExecuteOption.SYNC);
				dataService.doAction(new UnlockObject(template));
				Collection templates = dataService.listAll(Template.class);
				templateBean.setTemplates(templates);
			} else if (request.getParameter("block_up_id") != null) {
				templateBean.setChanged(true);
				templateBean.upBlock(Integer.parseInt(request.getParameter("block_column")), request.getParameter("block_up_id"));
			} else if (request.getParameter("block_down_id") != null) {
				templateBean.setChanged(true);
				templateBean.downBlock(Integer.parseInt(request.getParameter("block_column")), request.getParameter("block_down_id"));
			} else if (request.getParameter("block_left_id") != null) {
				templateBean.setChanged(true);
				templateBean.leftBlock(request.getParameter("block_left_id"), Integer.parseInt(request.getParameter("block_column")));
			} else if (request.getParameter("block_right_id") != null) {
				templateBean.setChanged(true);
				templateBean.rightBlock(request.getParameter("block_right_id"), Integer.parseInt(request.getParameter("block_column")));
			} else if (request.getParameter("block_remove_id") != null) {
				templateBean.setChanged(true);
				templateBean.removeBlock(Integer.parseInt(request.getParameter("block_column")), request.getParameter("block_remove_id"));
			} else if (WebTemplateBean.BLOCK_ACTION.equals(templateBean.getTemplateAction())) {
				templateBean.setChanged(true);
				AttributeBlock ab = (AttributeBlock) dataService.getById(new ObjectId(AttributeBlock.class, templateBean.getBlockId()));
				TemplateBlock block = TemplateBlock.createFromAttributeBlock(ab);
//TODO ������� ����� ���������� ��������� Ace				
//				block.setColumn(0);
				templateBean.getBlocksLeft().add(block);
				templateBean.getBlocks().add(block);

				Collection attributeBlocks = dataService.listAll(AttributeBlock.class);
				List allBlocks = new ArrayList();
				allBlocks.addAll(templateBean.getBlocksLeft());
				allBlocks.addAll(templateBean.getBlocksRight());
				allBlocks.addAll(templateBean.getBlocksDown());

				MyBeanUtils.removeAll(attributeBlocks, allBlocks);
				for (Iterator it = attributeBlocks.iterator(); it.hasNext();) {
					AttributeBlock block1 = (AttributeBlock) it.next();
					if (!block1.isActive()) {// (block1.isSystem()) {
						it.remove();
					}
				}
				templateBean.setBlockItems(attributeBlocks);

			} else if (WebTemplateBean.EDIT_TEMPLATE_ACCESS_ACTION.equals(templateBean.getTemplateAction())) {
				// saveTemplate(templateBean, dataService);
				templateBean.setShowAccessSettingsWarning(false);
				templateBean.setMessage(null);
				response.sendRedirect(templateBean.getRedirectURL());
			}
			// disanbirdin: just keep changes for a while
			else if (WebTemplateBean.EDIT_TEMPLATE_RW_ATTR_ACTION.equals(templateBean.getTemplateAction())) {
				response.sendRedirect(templateBean.getRedirectURL());
			} else if (WebTemplateBean.EDIT_TEMPLATE_ATTR_ACTION.equals(templateBean.getTemplateAction())) {
				response.sendRedirect(templateBean.getRedirectURL());
			} else if(WebTemplateBean.EDIT_TEMPLATE_WORFLOW_MOVE_REQ_ATTR_ACTION.equals(templateBean.getTemplateAction())) {
				response.sendRedirect(templateBean.getRedirectURL());
			}  
			// end of changes
			else if (WebTemplateBean.SAVE_ACTION.equals(templateBean.getTemplateAction())) {
				if (!templateBean.isEditAccessExists()){
					throw new DataException("admin.edit.access.error");
				}
				saveTemplate(templateBean, dataService);
				
			} else if (WebTemplateBean.CLOSE_ACTION.equals(templateBean.getTemplateAction()) && templateBean.getRealId() != null) {
				if (Long.parseLong(templateBean.getRealId().toString()) > 0) {

					dataService.doAction(new UnlockObject(new ObjectId(Template.class, Long.parseLong(templateBean.getRealId().toString()))));
				}
				BeanUtils.copyProperties(new Template(), templateBean);
				templateBean.setRealId(null);
				templateBean.setChanged(false);
			}
		} catch (Exception e) {
			logger.error("Error performing action: ", e);
			templateBean.setMessage(e.getMessage());
		}

		if (isSessionForm()) {
			String formAttrName = getFormSessionAttributeName(request);
			request.getPortletSession().setAttribute(formAttrName, templateBean);
		}
	}


	private void saveTemplate(WebTemplateBean templateBean, AsyncDataServiceBean dataService) throws DataException, ServiceException {
		Template template = new Template();
		BeanUtils.copyProperties(templateBean, template);
		if (!"-1".equals(templateBean.getRealId())) {
			template.setId(Long.parseLong(templateBean.getId().getId().toString()));
		}
		template.setBlocks(new ArrayList());
		template.getBlocks().addAll(templateBean.getBlocksLeft());
		template.getBlocks().addAll(templateBean.getBlocksRight());
		template.getBlocks().addAll(templateBean.getBlocksDown());
		template.setActive(true);

		//disanbirdin
		if(templateBean.getAttributesViewParamDetails()!=null){
			template.setAttributesViewParamDetails(new ArrayList(templateBean.getAttributesViewParamDetails()));
		}
		
		ObjectId id = dataService.saveObject(template);
		loadTemplates(templateBean, dataService);
		templateBean.setId(Long.parseLong(id.getId().toString()));
		templateBean.setChanged(false);
		if (templateBean.isShowAccessSettingsWarning()) {
			templateBean.setMessage(messages.getString("templatesTemplateSaveSuccessWithAccessWarning"));
			templateBean.setShowAccessSettingsWarning(false);
		} else {
			templateBean.setMessage(messages.getString("templatesTemplateSaveSuccess"));
		}
	}

	public String getEditAccessRoles() {
		return editAccessRoles;
	}

	public void setEditAccessRoles(String editAccessRoles) {
		this.editAccessRoles = editAccessRoles;
	}
}