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
package com.aplana.dbmi.card;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.actionhandler.ActionsManager;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptor;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptorReader;
import com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionsManager;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;

/**
 * ����������� �����, �������������� ����� �������� ��������� � �������������
 * ������� ������
 * @author DSultanbekov
 */
public abstract class ActionsSupportingAttributeEditor extends JspAttributeEditor {
	public final static String ACTIONS_DESCRIPTOR = "actionsDescriptor";
	public final static String ACTIONS_MANAGER_KEY = "actionsManager";
	protected final static String ACTIONS_CONFIG_PARAM = "actionsConfig";

	private String actionsConfig;

	@Override
	public void setParameter(String name, String value) {
		if (ACTIONS_CONFIG_PARAM.equals(name)) {
			this.actionsConfig = value;
		} else {
			super.setParameter(name, value);
		}
	}

	@Override
	public void initEditor(PortletRequest request, Attribute attr)
			throws DataException {
		super.initEditor(request, attr);
		CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
		try {
			ActionsDescriptor ad = loadActionsDescriptor(sessionBean);
			sessionBean.getActiveCardInfo().setAttributeEditorData(attr.getId(), ACTIONS_DESCRIPTOR, ad);
			initActionsManager(sessionBean, ad, attr);
		} catch (Exception e) {
			logger.error("Failed to load actions descriptor from file '" + actionsConfig + "'", e);
		}
	}

	protected ActionsDescriptor loadActionsDescriptor(CardPortletSessionBean sessionBean) throws Exception {
		if (actionsConfig != null) {
			final InputStream stream = Portal.getFactory().getConfigService().loadConfigFile(AttributeEditorFactory.CONFIG_FOLDER + actionsConfig);
			try {
				final ActionsDescriptorReader reader = new ActionsDescriptorReader();
				return reader.readFromFile(stream, sessionBean.getServiceBean());
			} finally {
				stream.close();
			}
		}
		return new ActionsDescriptor();
	}

	@Override
	public boolean processAction(ActionRequest request,
			ActionResponse response, Attribute attr) throws DataException {
		final String attrCode = request.getParameter(CardPortlet.ATTR_ID_FIELD);
		if (attrCode == null || !attrCode.equals(attr.getId().getId())) {
			return false;
		}
		final CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
		final ActionsManager am = getActionsManager(sessionBean, attr.getId());
		return (am == null) ? false : am.processAction(request, response);
	}

	protected void initActionsManager(CardPortletSessionBean sessionBean, ActionsDescriptor actionsDescriptor, Attribute attr) {
		ActionsManager am = CardPortletAttributeEditorActionsManager.getInstance(sessionBean, actionsDescriptor, attr);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		cardInfo.setAttributeEditorData(attr.getId(), ACTIONS_MANAGER_KEY, am);
	}

	@Override
	protected Map<String, Object> getReferenceData(Attribute attr, PortletRequest request)
			throws PortletException {
		final Map<String, Object> result = super.getReferenceData(attr, request);
		final CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
		result.put("actionsManager", getActionsManager(sessionBean, attr.getId()));
		return result;
	}

	@Override
	protected String formExtraJavaScript(RenderRequest request, Attribute attr) throws PortletException, IllegalArgumentException {
		final CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
		final ActionsManager am = getActionsManager(sessionBean, attr.getId());
		List<ExtraJavascriptInfo> actionsExtraJsInfoList = am.getActionsExtraJavascriptInfo();
		String actionsExtraJsScript = formExtraJavaScriptFromList(attr, actionsExtraJsInfoList);
		String extraJsScript = super.formExtraJavaScript(request, attr);
		StringBuilder extraJsBuilder = new StringBuilder(extraJsScript);

		if (actionsExtraJsScript.length() > 0) {
			if (extraJsBuilder.length() <= 0) {
				extraJsBuilder.append(actionsExtraJsScript);
			} else {
				extraJsBuilder.append("\n").append(actionsExtraJsScript);
			}
		}
		return extraJsBuilder.toString();
	}

	public static ActionsManager getActionsManager(CardPortletSessionBean sessionBean, ObjectId attrId) {
		return (ActionsManager)sessionBean.getActiveCardInfo().getAttributeEditorData(attrId, ACTIONS_MANAGER_KEY);
	}
}
