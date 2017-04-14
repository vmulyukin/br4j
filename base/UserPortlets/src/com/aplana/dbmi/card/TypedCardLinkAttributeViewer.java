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

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.xml.SearchXmlHelper;
import com.aplana.dbmi.card.graph.Graph;
import com.aplana.dbmi.card.graph.GraphDescriptor;
import com.aplana.dbmi.card.graph.GraphDescriptorReader;
import com.aplana.dbmi.card.graph.GraphLoader;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;

public class TypedCardLinkAttributeViewer extends ActionsSupportingAttributeEditor {
	public static final String PARAM_CONFIG_GRAPH = "configGraph";
	public static final String PARAM_SHOW_TITLE = "showTitle";
	public static final String PARAM_SHOW_EMPTY = "showEmpty";
	public static final String PARAM_TYPE_CAPTION = "typeCaption";
	public static final String PARAM_CONFIG = "config";
	
	public static final String GRAPH_IS_VIEW = "graphIsView";
	public static final String GRAPH_DATA = "graphData";
	public static final String KEY_SHOW_TITLE = "showTitle";
	public static final String KEY_SHOW_EMPTY = "showEmpty";
	public static final String KEY_TYPE_CAPTION = "typeCaption";
	
	private String configGraph;
	private boolean showTitle = true;
	private boolean showEmpty = true;
	private String typeCaption;
	protected String config;
	protected Collection<SearchResult.Column> columns;
	
	public TypedCardLinkAttributeViewer() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/TypedLinksView.jsp");
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/TypedLinksViewInclude.jsp");
	}
	
	public void setParameter(String name, String value) {
		if (PARAM_CONFIG_GRAPH.equalsIgnoreCase(name))
			configGraph = value; 
		else if (PARAM_SHOW_TITLE.equalsIgnoreCase(name))
			showTitle = Boolean.parseBoolean(value);
		else if (PARAM_SHOW_EMPTY.equalsIgnoreCase(name))
			showEmpty = Boolean.parseBoolean(value);
		else if (PARAM_TYPE_CAPTION.equalsIgnoreCase(name))
			typeCaption = value;
		else if (PARAM_CONFIG.equalsIgnoreCase(name))
			config = value;
		else
			super.setParameter(name, value);
	}
	
	public void initEditor(PortletRequest request, Attribute attr) throws DataException {
		initColumnsFromXml();
		super.initEditor(request, attr);
		// ��������� configGraph ���� �� �����
		// ���������� � writeEditorCode, ��� ���������� �������� ������� ��������
		// loadAttributeValues(attr, request);
	}
	
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

	public void loadAttributeValues(Attribute attr, PortletRequest request) {
		CardPortletSessionBean sessionBean = (CardPortletSessionBean)request.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);		
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		cardInfo.setAttributeEditorData(attr.getId(), KEY_SHOW_TITLE, new Boolean(showTitle));
		cardInfo.setAttributeEditorData(attr.getId(), KEY_SHOW_EMPTY, new Boolean(showEmpty));
		cardInfo.setAttributeEditorData(attr.getId(), KEY_TYPE_CAPTION, typeCaption);
		if(configGraph != null) {
			DataServiceBean serviceBean = sessionBean.getServiceBean();
			InputStream stream;
			try {
				stream = Portal.getFactory().getConfigService().loadConfigFile(AttributeEditorFactory.CONFIG_FOLDER + configGraph);
				GraphDescriptorReader reader = new GraphDescriptorReader();
				GraphDescriptor descripGraph = reader.read(stream);
				GraphLoader loader = new GraphLoader(descripGraph, serviceBean);
				Long cardId = (Long)sessionBean.getActiveCard().getId().getId();
				Graph graph = loader.load(cardId);
				cardInfo.setAttributeEditorData(attr.getId(), GRAPH_IS_VIEW, Boolean.TRUE);
				cardInfo.setAttributeEditorData(attr.getId(), GRAPH_DATA, graph);
			} catch (Exception e) {
				logger.error("Failed to load graph described in file " + configGraph, e);				
			}
		} else {
			cardInfo.setAttributeEditorData(attr.getId(), GRAPH_IS_VIEW, Boolean.FALSE);
			cardInfo.setAttributeEditorData(attr.getId(), GRAPH_DATA, null);
		}
		reloadLinks(request, attr);
	}
	
	public void reloadLinks(PortletRequest request, Attribute attr) {
		if(columns != null) {
			LinkedCardUtils.reloadLinks(request, (TypedCardLinkAttribute) attr, columns);
		} else {
			LinkedCardUtils.reloadLinks(request, (TypedCardLinkAttribute) attr);
		}
	}

	public boolean isValueCollapsable() {
		return true;
	}

	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		return false;
	}
	
	public void writeEditorCode(RenderRequest request, RenderResponse response,
			Attribute attr) throws IOException, PortletException {
		loadAttributeValues(attr, request);
		super.writeEditorCode(request, response, attr);
	}
}
