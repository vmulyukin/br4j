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
package com.aplana.dbmi.workflowbuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.PortletRequestDataBinder;
import org.springframework.web.portlet.mvc.SimpleFormController;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.LockableObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;

public class LockableObjectListController extends SimpleFormController implements MessageSourceAware {
	public static final String ACTION_ADD = "add";
	public static final String ACTION_EDIT = "edit";
	public static final String ACTION_OK = "ok";
	public static final String ACTION_CANCEL = "cancel";
	public static final String ACTION_REFRESH = "refresh";
	
	public static final String PARAM_OBJECT_ID = "objectId";
	public static final String PARAM_ACTION = "action";
	
	public static final int PAGE_SIZE = 20;
	
	private MessageSource messageSource;
	private Class objectType;
	private String resourceBundleBaseName;
	private boolean numericObjectId;
	private String editAccessRoles;
	
	public LockableObjectListController() {	

		numericObjectId = false;
	}
	
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	protected MessageSource getMessageSource() {
		return messageSource;
	}

	protected Class getObjectType() {
		return objectType;
	}
	
	public void setObjectType(Class objectType) {
		this.objectType = objectType;	
	}
	
	protected String getResourceBundleBaseName() {
		return resourceBundleBaseName;
	}

	public void setResourceBundleBaseName(String resourceBundleBaseName) {
		this.resourceBundleBaseName = resourceBundleBaseName;
	}
	
	public boolean isNumericObjectId() {
		return numericObjectId;
	}

	public void setNumericObjectId(boolean numericObjectId) {
		this.numericObjectId = numericObjectId;
	}
	
	public String getEditAccessRoles() {
		return editAccessRoles;
	}

	public void setEditAccessRoles(String editAccessRoles) {
		this.editAccessRoles = editAccessRoles;
	}

	protected ObjectId parseObjectId(String id) {
		if (numericObjectId) {
			return new ObjectId(getObjectType(), new Long(id));	
		} else {
			return new ObjectId(getObjectType(), id);
		}
	}
	
	protected Object formBackingObject(PortletRequest request) throws Exception {
		LockableObjectListCommandBean bean = (LockableObjectListCommandBean)getCommandClass().newInstance();
		AsyncDataServiceBean dataService = PortletUtil.createService(request);
		bean.setDataService(dataService);
		refresh(bean);
		return bean;
	}
	
	protected void onFormChange(ActionRequest request, ActionResponse response,	Object command) throws Exception {		
		LockableObjectListCommandBean bean = (LockableObjectListCommandBean)command;
		String action = request.getParameter(PARAM_ACTION);
		try {
			processFormChangeAction(action, request, response, bean);
		} catch (Exception e) {
			logger.error("Exception caught", e);
			bean.setMessage(e.getMessage());
		}
		setSortAndPaginationParameters(bean, response);
	}
	
	protected void onSubmitAction(ActionRequest request, ActionResponse response, Object command, BindException errors) throws Exception {		
		LockableObjectListCommandBean bean = (LockableObjectListCommandBean)command;
		setSortAndPaginationParameters(bean, response);
		String action = request.getParameter(PARAM_ACTION);
		try {
			LockableObject object = bean.getSelectedObject();			
			if (ACTION_OK.equals(action)) {
				if (!beforeSave(bean, request)) {
					return;
				}
				ObjectId objectId = bean.getDataService().saveObject(object, ExecuteOption.SYNC);
				if (object.getId() == null) {
					object = (LockableObject)bean.getDataService().getById(objectId);
					bean.getObjects().add(object);
				} else {
					replaceObjectInList(object, bean);
					bean.getDataService().doAction(new UnlockObject(object));
				}
				bean.setMessage(getMessage("msg.SaveSuccess", request));
			} else {
				if (object.getId() != null) {
					bean.getDataService().doAction(new UnlockObject(object));
				}
				bean.setMessage(getMessage("msg.Cancelled", request));
			}
			bean.setSelectedObject(null);
		} catch (Exception e) {
			logger.error("Exception caught", e);
			bean.setMessage(e.getMessage());
		}
	}

	protected ModelAndView onSubmitRender(RenderRequest request,
			RenderResponse response, Object command, BindException errors)
			throws Exception {
		return showForm(request, response, errors);
	}
	
	protected boolean isFormSubmission(PortletRequest request) {
		return request.getParameter(PARAM_ACTION) != null;
	}

	protected boolean isFormChangeRequest(PortletRequest request) {
		String action = request.getParameter(PARAM_ACTION);
		return !ACTION_OK.equals(action) && !ACTION_CANCEL.equals(action);
	}

	protected Map referenceData(PortletRequest request, Object command,	Errors errors) throws Exception {
		Map result = new HashMap();
		result.put("resourceBundleBasename", getResourceBundleBaseName());
		return result;
	}

	protected void processFormChangeAction(String action, ActionRequest request, ActionResponse response, LockableObjectListCommandBean command) throws Exception {
		LockableObjectListCommandBean bean = (LockableObjectListCommandBean)command;
		if (ACTION_EDIT.equals(action)) {
			if (bean.isEditAccessExists()){
				command.setSelectedObject(fetchAndLock(request.getParameter(PARAM_OBJECT_ID), command.getDataService()));
				initEdit(command);
			}
			else
				throw new DataException("admin.edit.access.error");
		} else if (ACTION_REFRESH.equals(action)) {
			refresh(command);				
		} else if (ACTION_ADD.equals(action)) {
			if (bean.isEditAccessExists()){
				LockableObject object = newObject(); 
				command.setSelectedObject(object);
				initEdit(command);
			}
			else
				throw new DataException("admin.edit.access.error");
		}		
	}
	
	protected void refresh(LockableObjectListCommandBean bean) throws DataException, ServiceException {
		List objects = (List)bean.getDataService().listAll(getObjectType());
		bean.setObjects(objects);
		bean.setSelectedObject(null);
		bean.getSortAndPaginationParameters().clear();
		bean.setEditAccessRoles(editAccessRoles);
	}
	
	protected void initEdit(LockableObjectListCommandBean command) throws Exception {
	}
	
	protected boolean beforeSave(LockableObjectListCommandBean command, ActionRequest request) throws Exception {
		return true;
	}
	
	protected LockableObject newObject() throws Exception {
		return (LockableObject)getObjectType().newInstance();		
	}
	
	protected LockableObject fetchAndLock(String id, DataServiceBean dataService) throws Exception {
		ObjectId objectId = parseObjectId(id);
		LockableObject object = (LockableObject)dataService.getById(objectId);
		dataService.doAction(new LockObject(object));
		return object;
	}
	
	protected void replaceObjectInList(LockableObject object, LockableObjectListCommandBean command) {
		ListIterator i = command.getObjects().listIterator();
		ObjectId objectId = object.getId();
		while (i.hasNext()) {
			DataObject item = (DataObject)i.next();
			if (item.getId().equals(objectId)) {
				i.set(object);
				break;
			}
		}
	}
	
	protected void setSortAndPaginationParameters(LockableObjectListCommandBean bean, ActionResponse response) {
		Iterator i = bean.getSortAndPaginationParameters().keySet().iterator();
		while (i.hasNext()) {
			String param  = (String)i.next();
			response.setRenderParameter(param, (String)bean.getSortAndPaginationParameters().get(param));
		}
	}
	
	protected String getMessage(String messageCode, PortletRequest request) {
		return messageSource.getMessage(messageCode, null, request.getLocale());
	}
	
	protected String getMessage(String messageCode, Object[] params, PortletRequest request) {
		return messageSource.getMessage(messageCode, params, request.getLocale());
	}
	
	protected ModelAndView handleRenderRequestInternal(RenderRequest request,
			RenderResponse response) throws Exception {
		PortletSession session = request.getPortletSession(false);
		if (session == null) {			
			// ����� �����������:
			// ��� ��������� ���, ��� ����, ����� �������� ������������� ������
			// 'could not obtain portlet session' �����������, ����� ������������
			// ������ � �������� ����� �� ������� �� ���������� ���� ��������, ��
			// ��� ���� �� ��� ��������� �� �������. ������ ��������� ����� ��������� ������,
			// ������ �� ����������� � ��������. �������� ��� JBoss Portal.
			session = request.getPortletSession(true);
			logger.warn("Attempting to render portlet without session. Creating new.");
			Object command = formBackingObject(request);			
			session.setAttribute(getRenderCommandSessionAttributeName(), command);
			PortletRequestDataBinder binder = bindAndValidate(request, command);
			session.setAttribute(getRenderErrorsSessionAttributeName(), new BindException(binder.getBindingResult()));
		}
		return super.handleRenderRequestInternal(request, response);
	}
}
