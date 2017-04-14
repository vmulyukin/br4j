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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.xml.SearchXmlHelper;
import com.aplana.dbmi.actionhandler.descriptor.ActionHandlerDescriptor;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptor;
import com.aplana.dbmi.actionhandler.descriptor.SelectionType;
import com.aplana.dbmi.card.actionhandler.AddLinkedCardActionHandler;
import com.aplana.dbmi.card.actionhandler.EditCardLinksActionHandler;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.LocalizedString;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;

public class CardLinkExAttributeEditor extends ActionsSupportingAttributeEditor
{
	public static final String PARAM_TEMPLATE = "template";
	public static final String PARAM_CREATE = "create";
	public static final String PARAM_REMOVE = "remove";
	public static final String PARAM_CHANGE = "change";
	public static final String PARAM_SHOW_TITLE = "showTitle";
	public static final String PARAM_SHOW_EMPTY = "showEmpty";
	
	public static final String ACTION_REMOVE = "link_remove";
	public static final String FIELD_LINKED_ID = "link_id";
	public static final String KEY_REMOVE = "remove";
	public static final String KEY_SHOW_TITLE = "showTitle";
	public static final String KEY_SHOW_EMPTY = "showEmpty";
	
	private String templateId;
	
	private boolean modeCreate = true;
	private boolean modeRemove = true;
	private boolean modeChange = false;
	private boolean showTitle = true;
	private boolean showEmpty = true;
	
	private static final String PARAM_CONFIG = "config";
	private String config = null;
	protected Collection<SearchResult.Column> columns;

	public CardLinkExAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/CardLinksEx.jsp");
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/CardLinksExInclude.jsp");
	}

	@Override
	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		return false;
	}

	@Override
	public boolean processAction(ActionRequest request,	ActionResponse response, Attribute attr) throws DataException {
		final String action = request.getParameter(CardPortlet.ACTION_FIELD);
		final String attrId = request.getParameter(CardPortlet.ATTR_ID_FIELD);
		if (attrId == null || !attrId.equals(attr.getId().getId()))
			return false;
		final CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		if (ACTION_REMOVE.equals(action)) {
			removeLink((CardLinkAttribute) attr,
					new ObjectId(Card.class, Long.parseLong(request.getParameter(FIELD_LINKED_ID))));
			loadAttributeValues(attr, request);
			return true;
		} else {
			return super.processAction(request, response, attr);
		}
	}

	protected void removeLink(CardLinkAttribute attr, ObjectId linkId) 
	{
		if (attr != null && linkId != null)
			attr.removeLinkedId(linkId);
	}
	
	protected void addLink(CardLinkAttribute attr, ObjectId linkId) 
	{
		if (attr != null && linkId != null)
			attr.addLinkedId(linkId);
	}

	@Override
	public void initEditor(PortletRequest request, Attribute attr)
			throws DataException {
		super.initEditor(request, attr);
		initColumnsFromXml();
		loadAttributeValues(attr, request);
		CardPortletCardInfo info = getCardPortletSessionBean(request).getActiveCardInfo();
		if (modeRemove) {
			info.setAttributeEditorData(attr.getId(), KEY_REMOVE, Boolean.TRUE);
		}
		info.setAttributeEditorData(attr.getId(), KEY_SHOW_TITLE, new Boolean(showTitle));
		info.setAttributeEditorData(attr.getId(), KEY_SHOW_EMPTY, new Boolean(showEmpty));
	}
	
	@Override
	public void loadAttributeValues(Attribute attr, PortletRequest request) {
		if(columns != null) {
			LinkedCardUtils.reloadLinks(request, (CardLinkAttribute) attr, this.columns);
		} else LinkedCardUtils.reloadLinks(request, (CardLinkAttribute) attr);
	}

	@Override
	protected ActionsDescriptor loadActionsDescriptor(
			CardPortletSessionBean sessionBean) throws Exception {
		ActionsDescriptor ad = super.loadActionsDescriptor(sessionBean);
		ResourceBundle rbEng = ResourceBundle.getBundle("com.aplana.dbmi.gui.nl.CardLinkEditResource", ContextProvider.LOCALE_ENG);
		ResourceBundle rbRus = ResourceBundle.getBundle("com.aplana.dbmi.gui.nl.CardLinkEditResource", ContextProvider.LOCALE_RUS);
		if (modeCreate && templateId != null) {
			ActionHandlerDescriptor d = new ActionHandlerDescriptor();
			d.setSelectionType(SelectionType.NONE);
			d.setHandlerClass(AddLinkedCardActionHandler.class);
			Map parameters = new HashMap(1);
			parameters.put(AddLinkedCardActionHandler.TEMPLATE_ID_PARAM, templateId);
			d.setParameters(parameters);
			LocalizedString title = new LocalizedString();
			title.setValueRu(rbRus.getString("button.add"));
			title.setValueEn(rbEng.getString("button.add"));
			d.setTitle(title);
			d.setId(getClass().getName() + ":add");
			ad.addItem(d);
		}
		if (modeChange) {
			ActionHandlerDescriptor d = new ActionHandlerDescriptor();
			d.setSelectionType(SelectionType.NONE);
			d.setHandlerClass(EditCardLinksActionHandler.class);
			d.setParameters(new HashMap(0));
			LocalizedString title = new LocalizedString();
			title.setValueRu(rbRus.getString("button.edit"));
			title.setValueEn(rbEng.getString("button.edit"));
			d.setTitle(title);
			d.setId(getClass().getName() + ":edit");
			ad.addItem(d);
		}
		return ad; 
	}

	@Override
	public void writeEditorCode(RenderRequest request, RenderResponse response,
			Attribute attr) throws IOException, PortletException {
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		if (!CardPortlet.CARD_EDIT_MODE.equals(sessionBean.getActiveCardInfo().getMode()))
			throw new IllegalStateException("CardLinkExAttributeEditor can not be used in view mode");
		super.writeEditorCode(request, response, attr);
	}

	@Override
	public void setParameter(String name, String value) {
		if (PARAM_TEMPLATE.equalsIgnoreCase(name))
			setTemplate(value);
		else if (PARAM_CREATE.equalsIgnoreCase(name))
			modeCreate = Boolean.parseBoolean(value);
		else if (PARAM_REMOVE.equalsIgnoreCase(name))
			modeRemove = Boolean.parseBoolean(value);
		else if (PARAM_CHANGE.equalsIgnoreCase(name))
			modeChange = Boolean.parseBoolean(value);
		else if (PARAM_SHOW_TITLE.equalsIgnoreCase(name))
			showTitle = Boolean.parseBoolean(value);
		else if (PARAM_SHOW_EMPTY.equalsIgnoreCase(name))
			showEmpty = Boolean.parseBoolean(value);
		else if(PARAM_CONFIG.equalsIgnoreCase(name))
			config = value;
		else
			super.setParameter(name, value);
	}
	
	@SuppressWarnings("unchecked")
	private void initColumnsFromXml() throws DataException{
		if(config != null){
			try {
				final Search search = new Search();
				final InputStream xml = Portal.getFactory().getConfigService().loadConfigFile(AttributeEditorFactory.CONFIG_FOLDER + config);
				try {
					SearchXmlHelper.initFromXml(search, xml);
				} finally {
					xml.close();
				}
				final Collection<SearchResult.Column> columnsLocal = search.getColumns();
				if(columnsLocal != null && !columnsLocal.isEmpty())
					this.columns = columnsLocal;
			} catch (IOException e) {
				logger.error("Couldn't open hierarchy descriptor file: " + config, e);
			}
		}
	}

	public void setTemplate(String template) {
		ObjectId id = ObjectId.predefined(Template.class, template);
		templateId = (id != null) ? id.getId().toString() : template;
	}

	@Override
	public boolean doesFullRendering(Attribute attr) {
		return true;
	}

	public boolean isModeCreate() {
		return modeCreate;
	}

	public boolean isModeRemove() {
		return modeRemove;
	}

	public boolean isModeChange() {
		return modeChange;
	}

	public boolean isShowTitle() {
		return showTitle;
	}

	public boolean isShowEmpty() {
		return showEmpty;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		CardLinkExAttributeEditor other = (CardLinkExAttributeEditor) obj;
		return
			modeCreate == other.modeCreate && modeRemove == other.modeRemove &&
			modeChange == other.modeChange &&
			(templateId == null ? other.templateId == null : templateId.equals(other.templateId));
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ 
			(modeCreate ? 0x33152A76 : 0x66666666) ^
			(modeRemove ? 0x298CCE6D : 0xAAAAAAAA) ^
			(templateId == null ? 0 : templateId.hashCode());
	}
}
