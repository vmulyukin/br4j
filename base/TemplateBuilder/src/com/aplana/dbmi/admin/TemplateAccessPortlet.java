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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.SimpleFormController;

import com.aplana.dbmi.admin.WebTemplateAccessBean.AccessItem;
import com.aplana.dbmi.admin.WebTemplateAccessBean.Permissions;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.CardAccess;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.LocalizedString;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.Workflow;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;

public class TemplateAccessPortlet extends SimpleFormController {
	private Log logger = LogFactory.getLog(getClass());
	
	public static final String PARAM_PORTLET_ACTION = "portlet_action";
	public static final String PARAM_ACTION = "action";
	
	public static final String ACTION_EDIT_PERMISSION = "editPermission";
	public static final String PARAM_PERMISSION_TYPE = "permissionsType";
	public static final String PARAM_OBJECT_ID = "objectId";
	
	public static final String ACTION_DELETE_ACCESS_ITEM = "deleteAccessItem";
	public static final String PARAM_ITEM_INDEX = "itemIndex";
	
	public static final String ACTION_ADD_ACCESS_ITEM = "addAccessItem";
	public static final String ACTION_OK = "ok";
	public static final String ACTION_CANCEL = "cancel";
	
	private WebTemplateBean getTemplateBean(PortletRequest request) {
		return (WebTemplateBean) request.getPortletSession().getAttribute("com.aplana.dbmi.admin.TemplatesPortlet.FORM.templateBean");
	}
	
	protected Object formBackingObject(PortletRequest request) throws Exception {
		WebTemplateAccessBean bean = (WebTemplateAccessBean)super.formBackingObject(request);
		WebTemplateBean templateBean = getTemplateBean(request);
		bean.setTemplate(templateBean);
		DataServiceBean dataServiceBean = PortletUtil.createService(request);
		
		bean.setSystemRoles(dataServiceBean.listAll(SystemRole.class));
		
		Iterator i = bean.getTemplate().getBlocks().iterator();
		List personAttributes = new ArrayList();
		while (i.hasNext()) {
			AttributeBlock block = (AttributeBlock)i.next();
			Iterator j = block.getAttributes().iterator();
			while (j.hasNext()) {
				Object attr = j.next();
				if (attr instanceof PersonAttribute) {
					personAttributes.add(attr);
				}
			}
		}
		bean.setPersonAttributes(personAttributes);
		
		Map mapSystemRoles = collectionToMap(bean.getSystemRoles());
		Map mapPersonAttributes = collectionToMap(bean.getPersonAttributes());
		//dataServiceBean.listChildren(bean.getTemplate().getId(), CardAccess.class);
		Collection cardAccess = templateBean.getCardAccess(); 

		Collection records = dataServiceBean.listChildren(bean.getTemplate().getWorkflow(), CardState.class);
		bean.setCardReadPermissions(new ArrayList(records.size()));
		bean.setCardEditPermissions(new ArrayList(records.size()));
		i = records.iterator();		
		while (i.hasNext()) {
			CardState cs = (CardState)i.next();
			Permissions item = new Permissions();
			
			item.setNames(new LocalizedString[] {cs.getName() });
			item.setDataObject(cs);
			item.setType(CardAccess.READ_CARD);			
			initPermissions(item, cardAccess, mapSystemRoles, mapPersonAttributes);
			bean.getCardReadPermissions().add(item);
			item = new Permissions();
			item.setNames(new LocalizedString[] { cs.getName() });
			item.setDataObject(cs);
			item.setType(CardAccess.EDIT_CARD);
			initPermissions(item, cardAccess, mapSystemRoles, mapPersonAttributes);
			bean.getCardEditPermissions().add(item);
		}

		Map cardStatuses = collectionToMap(records);		
		records = dataServiceBean.listChildren(bean.getTemplate().getWorkflow(), WorkflowMove.class);
		bean.setWorkflowMovePermissions(new ArrayList(records.size()));
		i = records.iterator();		
		while (i.hasNext()) {
			WorkflowMove wfm = (WorkflowMove)i.next();
			Permissions item = new Permissions();
			CardState csFrom = (CardState)cardStatuses.get(wfm.getFromState()),
				csTo = (CardState)cardStatuses.get(wfm.getToState());
			item.setNames(new LocalizedString[] {
				wfm.getName().hasEmptyValues() ? wfm.getDefaultName() : wfm.getName(),
				csFrom.getName(),
				csTo.getName()
			});
			item.setDataObject(wfm);
			item.setType(CardAccess.WORKFLOW_MOVE);
			initPermissions(item, cardAccess, mapSystemRoles, mapPersonAttributes);
			bean.getWorkflowMovePermissions().add(item);
		}

		Workflow workflow = (Workflow)dataServiceBean.getById(bean.getTemplate().getWorkflow());
		bean.setInitialState((CardState)cardStatuses.get(workflow.getInitialState()));
		
		Permissions cardCreatePermissions = new Permissions();
		cardCreatePermissions.setNames(new LocalizedString[] {bean.getInitialState().getName()});
		cardCreatePermissions.setType(CardAccess.CREATE_CARD);
		initPermissions(cardCreatePermissions, cardAccess, mapSystemRoles, mapPersonAttributes);
		cardCreatePermissions.setAllowForAll(false);
		bean.setCardCreatePermissions(cardCreatePermissions);
		return bean;
	}
	
	private void initPermissions(Permissions permissions, Collection cardAccess, Map mapSystemRoles, Map mapPersonAttributes) {
		List items = new ArrayList();
		Iterator i = cardAccess.iterator();
		while (i.hasNext()) {
			CardAccess ca = (CardAccess)i.next();
			if (!ca.getPermissionType().equals(permissions.getType())) {
				continue;
			}
			
			if ((permissions.getObjectId() == null && ca.getObjectId() == null) || permissions.getObjectId().equals(ca.getObjectId())) {
				if (ca.getRoleId() == null && ca.getPersonAttributeId() == null) {
					permissions.setAllowForAll(true);
					continue;
				}
				AccessItem item = new AccessItem();				
				if (ca.getPersonAttributeId() != null) {
					PersonAttribute attr = (PersonAttribute)mapPersonAttributes.get(ca.getPersonAttributeId());
					if (attr == null) {
						logger.warn("Ignoring CardAccess record referencing '" + ca.getPersonAttributeId().getId() + "' attribute as this attribute is not found in template");
						continue;
					} else {
						item.setPersonAttribute(attr);
					}
				}
				if (ca.getRoleId() != null) {
					item.setRole((SystemRole)mapSystemRoles.get(ca.getRoleId()));
				}
				items.add(item);
			}
		}
		permissions.setAccessItems(items);
	}
	
	private Map collectionToMap(Collection items) {
		Map result = new HashMap(items.size());
		Iterator i = items.iterator();
		while (i.hasNext()) {
			DataObject item = (DataObject)i.next();
			result.put(item.getId(), item);
		}
		return result;
	}

	protected void onFormChange(ActionRequest request, ActionResponse response,	Object command) throws Exception {
		WebTemplateAccessBean bean = (WebTemplateAccessBean)command;
		setSortParameters(bean, response);
		String action = request.getParameter(PARAM_ACTION);				
		if (ACTION_EDIT_PERMISSION.equals(action)) {
			Long type = new Long(Long.parseLong(request.getParameter(PARAM_PERMISSION_TYPE)));
			Long objectId = null;
			if (!CardAccess.CREATE_CARD.equals(type)) {
				objectId = new Long(Long.parseLong(request.getParameter(PARAM_OBJECT_ID)));
			}
			bean.setSelectedPermissions(bean.getPermissions(type, objectId));
			if (bean.getSelectedPermissions() == null) {
				logger.warn("Couldn't find permissions for type = '" + type + "' and objectId = '" + objectId + "'");
			}
		} else if (ACTION_DELETE_ACCESS_ITEM.equals(action)) {
			int index = Integer.parseInt(request.getParameter(PARAM_ITEM_INDEX));
			bean.getSelectedPermissions().getAccessItems().remove(index);
		} else if (ACTION_ADD_ACCESS_ITEM.equals(action)) {
			AccessItem item = new AccessItem();
			item.setPersonAttribute(bean.getPersonAttribute(bean.getAttrCode()));
			item.setRole(bean.getSystemRole(bean.getRoleCode()));
			ResourceBundle messages = ResourceBundle.getBundle("templateAccess", request.getLocale());
			boolean isGood = true;
			if (CardAccess.CREATE_CARD.equals(bean.getSelectedPermissions().getType())) {
				if (item.getRole() == null) {
					isGood = false;
					bean.setMessage(messages.getString("msg.roleRequired"));
				}
			} else {
				if (item.getPersonAttribute() == null && item.getRole() == null) {
					isGood = false;
					bean.setMessage(messages.getString("msg.personAttributeOrRoleRequired"));
				}
			}
			if (isGood) {
				String st = getAccessItemKey(item.getRole(), item.getPersonAttribute());
				Iterator i = bean.getSelectedPermissions().getAccessItems().iterator();
				while (i.hasNext()) {
					AccessItem ai = (AccessItem)i.next();
					String stCur = getAccessItemKey(ai.getRole(), ai.getPersonAttribute());
					if (st.equals(stCur)) {
						isGood = false;
						bean.setMessage(messages.getString("msg.accessItemAlreadyExists"));
					}
				}
			}
			if (isGood) {
				bean.getSelectedPermissions().getAccessItems().add(item);
			}
			bean.setAttrCode(null);
			bean.setRoleCode(null);
		}
	}

	protected boolean isFormChangeRequest(PortletRequest request) {
		String action = request.getParameter(PARAM_ACTION); 
		return !(ACTION_OK.equals(action) || ACTION_CANCEL.equals(action));
	}

	protected void onSubmitAction(ActionRequest request, ActionResponse response, Object command, BindException errors) throws Exception {
		WebTemplateAccessBean bean = (WebTemplateAccessBean)command;
		bean.setSuccess(true);
		String action = request.getParameter(PARAM_ACTION);
		if (action.equals(ACTION_OK)) {			
			if (!checkAccessSettings(request, bean)) {
				bean.setSuccess(false);
				setSortParameters(bean, response);
				return;
			}
			List cardAccess = new ArrayList();
			ObjectId templateId = bean.getTemplate().getId();
			Iterator i = bean.getWorkflowMovePermissions().iterator();
			while (i.hasNext()) {
				permissionsToCardAccess((Permissions)i.next(), cardAccess, templateId, CardAccess.WORKFLOW_MOVE);
			}
			i = bean.getCardReadPermissions().iterator();
			while (i.hasNext()) {
				permissionsToCardAccess((Permissions)i.next(), cardAccess, templateId, CardAccess.READ_CARD);
			}
			i = bean.getCardEditPermissions().iterator();
			while (i.hasNext()) {
				permissionsToCardAccess((Permissions)i.next(), cardAccess, templateId, CardAccess.EDIT_CARD);
			}
			permissionsToCardAccess((Permissions)bean.getCardCreatePermissions(), cardAccess, templateId, CardAccess.CREATE_CARD);
			WebTemplateBean templateBean = getTemplateBean(request); 
			templateBean.setCardAccess(cardAccess);
			templateBean.setChanged(true);
		} 
	}

	protected ModelAndView onSubmitRender(RenderRequest request, RenderResponse response, Object command, BindException errors) throws Exception {
		if (this.isFormSubmission(request) && !isFormChangeRequest(request)) {
			WebTemplateAccessBean bean = (WebTemplateAccessBean)command;
			if (bean.isSuccess()) {
				// ��������� �� �������� �� ������� ��������
				return new ModelAndView("templates", "templateBean", getTemplateBean(request));	
			} else {
				return showForm(request, response, errors);
			}
		} else {
			return super.onSubmitRender(request, response, command, errors);
		}
	}
	
	private void permissionsToCardAccess(Permissions permissions, List cardAccess, ObjectId templateId, Long permissionType) {
		ObjectId objectId = permissions.getObjectId();
		if (permissions.isAllowForAll()) {
			CardAccess ca = new CardAccess();
			ca.setObjectId(objectId);
			ca.setPermissionType(permissionType);
			ca.setPersonAttributeId(null);
			ca.setRoleId(null);			
			ca.setTemplateId(templateId);
			cardAccess.add(ca);
		} else {
			Iterator i = permissions.getAccessItems().iterator();
			while (i.hasNext()) {
				AccessItem item = (AccessItem)i.next();
				CardAccess ca = new CardAccess();
				ca.setObjectId(objectId);
				ca.setPermissionType(permissionType);
				ca.setPersonAttributeId(item.getPersonAttribute() == null ? null : item.getPersonAttribute().getId());
				ca.setRoleId(item.getRole() == null ? null : item.getRole().getId());
				ca.setTemplateId(templateId);
				cardAccess.add(ca);
			}
		}
	}
	
	private static final String KEY_ALL = "ALL";
	private static final String KEY_NONE = "NONE";
	private static final char KEY_DELIM = '#';

	private String getSetElement(Long permissionType, ObjectId objectId, String subKey) {
		return permissionType.toString() + KEY_DELIM + objectId.getId().toString() + KEY_DELIM + subKey;
	}
	
	private String getAccessItemKey(SystemRole role, PersonAttribute attr) {
		return (role == null ? KEY_NONE : role.getId().getId().toString())
			+ KEY_DELIM + (attr == null ? KEY_NONE : attr.getId().getId().toString());
	}
	
	private String getSetElement(Long permissionType, ObjectId objectId, SystemRole role, PersonAttribute attr) {
		return getSetElement(permissionType, objectId, getAccessItemKey(role, attr));
	}
	
	private boolean hasAccess(Set items, Long permissionType, ObjectId objectId, AccessItem accessItem) {
		if (accessItem.getPersonAttribute() != null && accessItem.getRole() != null) {
			if (items.contains(getSetElement(permissionType, objectId, accessItem.getRole(), null))
				|| items.contains(getSetElement(permissionType, objectId, null, accessItem.getPersonAttribute()))) {
				return true;
			}
		}
		return items.contains(getSetElement(permissionType, objectId, accessItem.getRole(), accessItem.getPersonAttribute())) 
			|| items.contains(getSetElement(permissionType, objectId, KEY_ALL));
	}
	
	private void fillPermissionsData(Set items, Permissions permissions) {
		if (permissions.isAllowForAll()) {
			items.add(getSetElement(permissions.getType(), permissions.getObjectId(), KEY_ALL));
		} else {
			Iterator j = permissions.getAccessItems().iterator();
			while (j.hasNext()) {
				AccessItem accessItem = (AccessItem)j.next();
				items.add(getSetElement(permissions.getType(), permissions.getObjectId(), accessItem.getRole(), accessItem.getPersonAttribute()));
			}
		}
	}
	
	private boolean checkAccess(Set items, Permissions permissions, Long parentPermissionType, ObjectId parentObjectId, WebTemplateAccessBean bean, ResourceBundle messages, String messageKey) {
		boolean result = true;
		String userDescriptor = null;
		if (permissions.isAllowForAll()) {
			if (!items.contains(getSetElement(parentPermissionType, parentObjectId, KEY_ALL))) {
				userDescriptor = messages.getString("msg.anyUser");
				result = false; 
			}
		} else {
			Iterator j = permissions.getAccessItems().iterator();
			while (j.hasNext()) {
				AccessItem accessItem = (AccessItem)j.next();
				if (!hasAccess(items, parentPermissionType, parentObjectId, accessItem)) {
					/**
					 * When checking access consistency for card create permission we should take
					 * in account that user who created card automatically became author of card 
					 */
					if (permissions.isCardCreate()) {
						AccessItem authorItem = new AccessItem();
						authorItem.setRole(accessItem.getRole());
						authorItem.setPersonAttribute((PersonAttribute)DataObject.createFromId(Attribute.ID_AUTHOR));
						if (hasAccess(items, parentPermissionType, parentObjectId, authorItem)) {
							// It's ok: there is no rule for this particular system role, but there is rule for author with this role
							continue;
						}
					}
					
					if (accessItem.getPersonAttribute() != null && accessItem.getRole() != null) {
						userDescriptor = MessageFormat.format(
							messages.getString("msg.personAttributeAndRoleUser"), 
							new Object[] {
								accessItem.getPersonAttribute().getName(),
								accessItem.getRole().getName()
							}
						);						
					} else if (accessItem.getPersonAttribute() != null) {
						userDescriptor = MessageFormat.format(
							messages.getString("msg.personAttributeUser"), 
							new Object[] {accessItem.getPersonAttribute().getName()}
						);
					} else {
						userDescriptor = MessageFormat.format(
							messages.getString("msg.roleUser"), 
							new Object[] {accessItem.getRole().getName()}
						);			
					}
					result = false;
					break;
				}
			}
		}
		if (!result) {
			String name1 = permissions.getNames()[0].getValue();
			String name2 = null;
			if (permissions.getNames().length > 1) {
				name2 = permissions.getNames()[1].getValue();
			}
			bean.setMessage(MessageFormat.format(messages.getString(messageKey), new Object[] {userDescriptor, name1, name2}));
		}
		return result;
	}
	
	/**
	 * Validation of access settings consistency.
	 * Checks that user who can edit card have permissions to read cards and etc.
	 * @param request request (to retrieve locale information from)
	 * @param bean command object
	 * @return true if validation succeed, false otherwise
	 */
	private boolean checkAccessSettings(ActionRequest request, WebTemplateAccessBean bean) {
		ResourceBundle messages = ResourceBundle.getBundle("templateAccess", request.getLocale());
		Set items = new HashSet();
		Iterator i = bean.getCardReadPermissions().iterator();
		while (i.hasNext()) {
			Permissions permissions = (Permissions)i.next();
			fillPermissionsData(items, permissions);
			if (!permissions.isAllowForAll() && permissions.getAccessItems().isEmpty()) {
				bean.setMessage(MessageFormat.format(messages.getString("msg.noReadAccess"), new Object[] {permissions.getNames()[0].getValue()}));
				return false;
			}
		}

		i = bean.getCardEditPermissions().iterator();
		while (i.hasNext()) {
			Permissions permissions = (Permissions)i.next();
			if (!checkAccess(items, permissions, CardAccess.READ_CARD, permissions.getObjectId(), bean, messages, "msg.cannotReadForEdit")) {
				return false;
			}
			fillPermissionsData(items, permissions);			
		}

		i = bean.getWorkflowMovePermissions().iterator();
		while (i.hasNext()) {
			Permissions permissions = (Permissions)i.next();
			WorkflowMove wm = (WorkflowMove)permissions.getDataObject();
			if (!checkAccess(items, permissions, CardAccess.EDIT_CARD, wm.getFromState(), bean, messages, "msg.cannotEditForWorkflowMove")) {
				return false;
			}
		}
		
		/**
		 * Checking that user who created card will have permissions to edit newly create card
		 */		
		boolean authorCanEditInInitialState = false;
		Permissions permissions = bean.getCardCreatePermissions();
		CardState initialState = bean.getInitialState();
		PersonAttribute attr = bean.getPersonAttribute((String)Attribute.ID_AUTHOR.getId());
		if (attr != null) {
			AccessItem item = new AccessItem();
			item.setPersonAttribute(attr);
			authorCanEditInInitialState = hasAccess(items, CardAccess.EDIT_CARD, initialState.getId(), item);
		}		
		if (!permissions.getAccessItems().isEmpty() && !authorCanEditInInitialState) {
			return checkAccess(items, permissions, CardAccess.EDIT_CARD, initialState.getId(), bean, messages, "msg.cannotEditForCardCreation");
		}
		return true;
	}

	/**
	 * Setting sorting and ordering render parameters for Displaytag tables
	 */
	protected void setSortParameters(WebTemplateAccessBean bean, ActionResponse response) {
		Iterator i = bean.getSortParameters().keySet().iterator();
		while (i.hasNext()) {
			String param  = (String)i.next();
			response.setRenderParameter(param, (String)bean.getSortParameters().get(param));
		}
	}
}