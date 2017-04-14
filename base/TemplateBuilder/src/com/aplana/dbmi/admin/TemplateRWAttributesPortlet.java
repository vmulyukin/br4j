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
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import org.springframework.beans.BeanUtils;
import org.springframework.validation.BindException;
import org.springframework.web.portlet.mvc.SimpleFormController;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeViewParamDetail;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;

/**
 * Portlet for customize the default and custom values of attributes.
 * 
 * @author disanbirdin
 * 
 */
public class TemplateRWAttributesPortlet extends SimpleFormController {

	private ResourceBundle messages;

	/**
	 * init form
	 */
	protected Object formBackingObject(PortletRequest request) throws Exception {

		ContextProvider.getContext().setLocale(request.getLocale());

		WebTemplateBean templateBean = (WebTemplateBean) request.getPortletSession().getAttribute("com.aplana.dbmi.admin.TemplatesPortlet.FORM.templateBean");
		WebTemplateRWAttributesBean bean = (WebTemplateRWAttributesBean) super.formBackingObject(request);
		bean.reset();
		bean.clearMaps();

		try {
			DataServiceBean dataServiceBean = PortletUtil.createService(request);

			BeanUtils.copyProperties(templateBean, bean, new String[] { "locker", "lockTime" });

			bean.setAttributesViewParamDetails(dataServiceBean.listChildren(bean.getId(), AttributeViewParamDetail.class));
			bean.setCardStates(new ArrayList(dataServiceBean.listChildren(templateBean.getWorkflow(), CardState.class)));
			bean.setRoles((List) dataServiceBean.listAll(SystemRole.class));

			// sets default mode on init
			bean.setMode(WebTemplateRWAttributesBean.DEFAULT_MODE);
			initIfDefaultMode(bean);

		} catch (Exception e) {
			e.printStackTrace();
			bean.setMessage(e.getMessage());
		}
		return bean;
	}

	/**
	 * submit form
	 */
	public void onSubmitAction(ActionRequest request, ActionResponse response, Object command, BindException errors) throws Exception {

		ContextProvider.getContext().setLocale(request.getLocale());
		messages = ResourceBundle.getBundle("templates", request.getLocale());

		WebTemplateBean templateBean = (WebTemplateBean) request.getPortletSession().getAttribute("com.aplana.dbmi.admin.TemplatesPortlet.FORM.templateBean");
		WebTemplateRWAttributesBean bean = (WebTemplateRWAttributesBean) command;

		if (WebTemplateRWAttributesBean.APPLY_TEMPLATE.equals(bean.getTemplateApplyClose())) {

			String mode = bean.getMode();

			if (WebTemplateRWAttributesBean.CUSTOM_MODE.equals(mode)) {

				//doIfActionApplyCustomMode(templateBean, bean);

			} else if (WebTemplateRWAttributesBean.DEFAULT_MODE.equals(mode)) {

				doIfActionApplyDefaultMode(templateBean, bean);

			}

		} else if (WebTemplateRWAttributesBean.CLOSE_TEMPLATE.equals(bean.getTemplateApplyClose())) {

			// do nothing

		} else {
			if (WebTemplateRWAttributesBean.DEFAULT_MODE.equals(bean.getMode())) {

				initIfDefaultMode(bean);

			} else if (WebTemplateRWAttributesBean.CUSTOM_MODE.equals(bean.getMode())) {

				//initIfCustomMode(bean);
			}
		}

		bean.setTemplateApplyClose(null);

		doProcess(request, response, command, templateBean);
	}

	private void initIfDefaultMode(WebTemplateRWAttributesBean bean) {
		bean.clearMaps();
		fillAttributesDefaultMode(bean);
		bean.setRwAttributesInitialized(true);
	}

	/*
	private void initIfCustomMode(WebTemplateRWAttributesBean bean) {
		bean.clearMaps();

		if (bean.getRoleId() == null || bean.getCardStateId() == null || bean.getRoleId().equals("") || bean.getCardStateId().equals("")) {

			bean.setRwAttributesInitialized(false);

		} else {

			fillAttributesCustomMode(bean);
			bean.setRwAttributesInitialized(true);
		}
	}
	*/
	
	/**
	 * Default Mode. Initialize default values of attributes in templatebean
	 * according the user interactions.
	 * 
	 * @param templateBean
	 *            {@link WebTemplateBean}
	 * @param bean
	 *            {@link WebTemplateRWAttributesBean}
	 */
	private void doIfActionApplyDefaultMode(WebTemplateBean templateBean, WebTemplateRWAttributesBean bean) {
		for (Iterator itBlock = templateBean.getBlocks().iterator(); itBlock.hasNext();) {
			TemplateBlock block = (TemplateBlock) itBlock.next();
			for (Iterator itAttribute = block.getAttributes().iterator(); itAttribute.hasNext();) {
				Attribute attribute = (Attribute) itAttribute.next();

				Object attributeCode = attribute.getId().getId();

				Object attributeCodeAttrReq = bean.getTemplateRWAttributesRequired().get(attribute.getId().getId());
				attribute.setMandatory(attributeCode.equals(attributeCodeAttrReq));

				Object attributeCodeAttrHidden = bean.getTemplateRWAttributesHidden().get(attribute.getId().getId());
				attribute.setHidden(attributeCode.equals(attributeCodeAttrHidden));

				Object attributeCodeAttrReadOnly = bean.getTemplateRWAttributesReadOnly().get(attribute.getId().getId());
				
				boolean readOnly = attributeCode.equals(attributeCodeAttrReadOnly);
				if(! (attribute instanceof BackLinkAttribute)){
					attribute.setReadOnly(readOnly);
				} else if (readOnly) {
						attribute.setReadOnly(true);
				}
				
			}
		}
	}

	/**
	 * Custom Mode. Prepare a list of {@link AttributeViewParamDetail} for store
	 * in the Session. This list contains the custom values of attributes
	 * depending on System Role and Card State.
	 * 
	 * @param templateBean
	 * 
	 * @param bean
	 * @throws DataException
	 *             if database errors occurred
	 * @throws ServiceException
	 *             if system errors occurred
	 */
	private void doIfActionApplyCustomMode(WebTemplateBean templateBean, WebTemplateRWAttributesBean bean) throws DataException, ServiceException {

		updateAvpdBeanCollection(bean);

		// set in templateBean avpd list
		templateBean.setAttributesViewParamDetails(new ArrayList(bean.getAttributesViewParamDetails()));

	}

	/**
	 * update collection if new entries added and modify if changed.
	 * 
	 * @param bean
	 *            backed bean
	 */
	private void updateAvpdBeanCollection(WebTemplateRWAttributesBean bean) {
		List avpdListNew = getNewAvpdList(bean);

		// its better to write an equal method for AttributeViewParamDetail.

		Collection avpdListOld = bean.getAttributesViewParamDetails();

		List tempAvpd = new ArrayList();
		for (int i = 0; i < avpdListNew.size(); i++) {
			AttributeViewParamDetail avpdNew = (AttributeViewParamDetail) avpdListNew.get(i);

			boolean exists = false;

			for (Iterator iterator = avpdListOld.iterator(); iterator.hasNext();) {
				AttributeViewParamDetail avpdOld = (AttributeViewParamDetail) iterator.next();

				if(equalNewAvpdOldAvpd(avpdNew, avpdOld)){

					exists = true;

					avpdOld.setMandatory(avpdNew.isMandatory());
					avpdOld.setReadOnly(avpdNew.isReadOnly());
					avpdOld.setHidden(avpdNew.isHidden());
					break;
				}
			}
			if (!exists) {
				tempAvpd.add(avpdNew);
			}
		}
		avpdListOld.addAll(tempAvpd);
	}

	private boolean equalNewAvpdOldAvpd(AttributeViewParamDetail avpdNew, AttributeViewParamDetail avpdOld){
		return (
				avpdNew.getAttributeCode().equals(avpdOld.getAttributeCode()) 
				&& avpdNew.getStateId() == avpdOld.getStateId()
				&& (
						(avpdNew.getRoleCode() == null && avpdOld.getRoleCode() == null) 
						|| (avpdNew.getRoleCode() != null && avpdNew.getRoleCode().equals(avpdOld.getRoleCode()))
					)
				);

	}
	/**
	 * Get list of new attributeViewParamdetails entries
	 * 
	 * @param bean
	 *            backed bean
	 * @return List of new attributeViewParamdetails entries
	 */
	private List getNewAvpdList(WebTemplateRWAttributesBean bean) {

		long cardStateId = Long.parseLong(bean.getCardStateId().toString());
		String roleId = bean.getRoleId().toString();

		if (WebTemplateRWAttributesBean.ALL_ROLES.equals(roleId)) {
			roleId = null;
		}

		List avpdListNew = new ArrayList();
		List avpdListToRemove = new ArrayList();

		for (Iterator itBlock = bean.getBlocks().iterator(); itBlock.hasNext();) {
			TemplateBlock block = (TemplateBlock) itBlock.next();

			for (Iterator itAttribute = block.getAttributes().iterator(); itAttribute.hasNext();) {
				Attribute attribute = (Attribute) itAttribute.next();
				Object attributeCode = attribute.getId().getId();

				AttributeViewParamDetail avpd = new AttributeViewParamDetail();
				avpd.setAttributeCode(attributeCode.toString());

				// avpd.setTemplateAttributeId(...);

				avpd.setRoleCode(roleId);
				avpd.setStateId(cardStateId);

				Object attributeCodeAttrReq = bean.getTemplateRWAttributesRequired().get(attribute.getId().getId());
				avpd.setMandatory(attributeCode.equals(attributeCodeAttrReq));

				Object attributeCodeAttrHidden = bean.getTemplateRWAttributesHidden().get(attribute.getId().getId());
				avpd.setHidden(attributeCode.equals(attributeCodeAttrHidden));

				Object attributeCodeAttrReadOnly = bean.getTemplateRWAttributesReadOnly().get(attribute.getId().getId());
				avpd.setReadOnly(attributeCode.equals(attributeCodeAttrReadOnly));

				// need to delete if equals default value
				if (attribute.isMandatory() == avpd.isMandatory() && attribute.isHidden() == avpd.isHidden() && attribute.isReadOnly() == avpd.isReadOnly()) {
					avpdListToRemove.add(avpd);
				} else {
					avpdListNew.add(avpd);
				}

			}
		}
		Collection attributesViewParamDetails = bean.getAttributesViewParamDetails();
		List temp = new ArrayList();
		for (Iterator iterator = attributesViewParamDetails.iterator(); iterator.hasNext();) {
			AttributeViewParamDetail avpd = (AttributeViewParamDetail) iterator.next();

			for (Iterator iterator2 = avpdListToRemove.iterator(); iterator2.hasNext();) {
				AttributeViewParamDetail avpdToRemove = (AttributeViewParamDetail) iterator2.next();
				
				if(equalNewAvpdOldAvpd(avpd, avpdToRemove)){
					temp.add(avpd);
				}
			}

		}
		attributesViewParamDetails.removeAll(temp);

		return avpdListNew;
	}

	/**
	 * Init attributes for GUI by selecting the default mode.
	 * 
	 * @param bean
	 *            Bean
	 */
	private void fillAttributesDefaultMode(WebTemplateRWAttributesBean bean) {
		for (Iterator itBlock = bean.getBlocks().iterator(); itBlock.hasNext();) {
			TemplateBlock block = (TemplateBlock) itBlock.next();
			for (Iterator itAttribute = block.getAttributes().iterator(); itAttribute.hasNext();) {
				Attribute attribute = (Attribute) itAttribute.next();
				Object attributeId = attribute.getId().getId();

				if (attribute.isMandatory()) {
					bean.getTemplateRWAttributesRequired().put(attributeId, attributeId);
				}
				if (attribute.isReadOnly()) {
					bean.getTemplateRWAttributesReadOnly().put(attributeId, attributeId);
				}
				if (attribute.isHidden()) {
					bean.getTemplateRWAttributesHidden().put(attributeId, attributeId);
				}
			}
		}
	}

	/**
	 * Init attributes for GUI by selecting the custom mode.
	 * 
	 * @param bean
	 * @param avpdList
	 */
	private void fillAttributesCustomMode(WebTemplateRWAttributesBean bean) {

		long cardStateId = Long.parseLong(bean.getCardStateId().toString());
		String roleId = bean.getRoleId().toString();

		Collection avpdList = bean.getAttributesViewParamDetails();

		for (Iterator itBlock = bean.getBlocks().iterator(); itBlock.hasNext();) {
			TemplateBlock block = (TemplateBlock) itBlock.next();

			for (Iterator itAttribute = block.getAttributes().iterator(); itAttribute.hasNext();) {
				Attribute attribute = (Attribute) itAttribute.next();

				Object attributeId = attribute.getId().getId();

				// default settings
				if (attribute.isMandatory()) {
					bean.getTemplateRWAttributesRequired().put(attributeId, attributeId);
				}
				if (attribute.isReadOnly()) {
					bean.getTemplateRWAttributesReadOnly().put(attributeId, attributeId);
				}
				if (attribute.isHidden()) {
					bean.getTemplateRWAttributesHidden().put(attributeId, attributeId);
				}

				for (Iterator iterator = avpdList.iterator(); iterator.hasNext();) {
					AttributeViewParamDetail avpd = (AttributeViewParamDetail) iterator.next();

					if (attributeId.equals(avpd.getAttributeCode()) && cardStateId == avpd.getStateId()

					&& (roleId.equals(avpd.getRoleCode()) || (roleId.equals(WebTemplateRWAttributesBean.ALL_ROLES) && avpd.getRoleCode() == null))) {

						if (attribute.isMandatory() != avpd.isMandatory() || attribute.isHidden() != avpd.isHidden() || attribute.isReadOnly() != avpd.isReadOnly()) {
							// custom settings if not equal default

							if (avpd.isMandatory()) {
								bean.getTemplateRWAttributesRequired().put(attributeId, attributeId);
							} else {
								bean.getTemplateRWAttributesRequired().remove(attributeId);
							}

							if (avpd.isReadOnly()) {
								bean.getTemplateRWAttributesReadOnly().put(attributeId, attributeId);
							} else {
								bean.getTemplateRWAttributesReadOnly().remove(attributeId);
							}

							if (avpd.isHidden()) {
								bean.getTemplateRWAttributesHidden().put(attributeId, attributeId);
							} else {
								bean.getTemplateRWAttributesHidden().remove(attributeId);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * process request, response
	 * 
	 * @param request
	 * @param response
	 * @param command
	 * @param templateBean
	 */
	private void doProcess(ActionRequest request, ActionResponse response, Object command, WebTemplateBean templateBean) {
		request.getPortletSession().setAttribute("com.aplana.dbmi.admin.TemplatesPortlet.FORM.templateBean", templateBean);
		String formAttrName = getFormSessionAttributeName(request);
		request.getPortletSession().setAttribute(formAttrName, command);
		response.setRenderParameter("portlet_action", "editTemplateRWAttr");
	}
}