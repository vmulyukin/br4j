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
package com.aplana.dbmi.portlet;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.card.AttributeEditorFactory;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.DocumentPickerAttributeEditor;
import com.aplana.dbmi.card.JspAttributeEditor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptorReader;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.util.TemplateComparator;

public class ARMDocumentPickerAttributeEditor extends DocumentPickerAttributeEditor{
	
	public static final ObjectId ATTR_PREPARED_DOCS = ObjectId.predefined(TypedCardLinkAttribute.class, "jbr.report.result");
	
	private static CardLinkPickerDescriptor descriptor = null;
	
	protected String configName = "picker/reportPreparedDocsARM.xml";
	
	private TypedCardLinkAttribute attr;
	private DocumentPickerAttributeEditor editor;
	private DataServiceBean dataService;
	
	public ARMDocumentPickerAttributeEditor(RenderRequest request, DataServiceBean dataService, 
			String attrId, String attrName) {
		this.dataService = dataService;
		this.config = configName;
		
		attr = new TypedCardLinkAttribute();
		attr.setId(attrId);	
		attr.setNameRu(attrName);	
		attr.setNameEn(attrName);	
		
		AttributeEditorFactory factory = AttributeEditorFactory.getFactory();
		retrieveEditor(factory);
		
		try {
			editor.initEditor(request, attr);
			editor.setParameter(PARAM_JSP, "/WEB-INF/jsp/resolutionReport/empty.jsp");
			editor.setRemoveReferenceData(false);
		} catch (DataException e) {
			logger.error(e);
		}
		
		try {
			getDescriptor(attr, dataService);
		} catch (DataException e) {
			logger.error(e);
		}
		
		CardPortletSessionBean cardPortletSessionBean = CardPortlet.getSessionBean(request);
		cardPortletSessionBean.getActiveCardInfo().
			setAttributeEditorData(attr.getId(), KEY_DESCRIPTOR, descriptor);
	}

	private void retrieveEditor(AttributeEditorFactory factory) {
		// retrieve editor for prepared docs attribute, as only this attribute has DocumentPickerAttributeEditor
		TypedCardLinkAttribute preparedDocsAttr = new TypedCardLinkAttribute();
		preparedDocsAttr.setId(ATTR_PREPARED_DOCS);
		editor = (DocumentPickerAttributeEditor) factory.getEditor(preparedDocsAttr, null);
		// rewrite config name, that has been set through setParameter in AttributeEditorFactory
		// to properly initialize editor with required config
		editor.setParameter(PARAM_CONFIG, configName);
	}
	
	public void setEditorDataToRequest(RenderRequest request, RenderResponse response) 
					throws DataException, IOException, PortletException {
		request.setAttribute(JspAttributeEditor.ATTR_ATTRIBUTE, attr); 
		
		List<Template> filterTemplates = getFilterTemplates(attr, dataService);
		Collections.sort(filterTemplates, new TemplateComparator());
		request.setAttribute("filterTemplates", filterTemplates);
		
		List<Integer> filterYears = getFilterYears();
		request.setAttribute("filterYears", filterYears);
		
		editor.writeEditorCode(request, response, attr);
	}
	
	private List<Template> getFilterTemplates(Attribute attr, DataServiceBean serviceBean) throws DataException {
		CardLinkPickerDescriptor descriptor = null;
		
		synchronized (DocumentPickerAttributeEditor.class) {
			descriptor = getDescriptor(attr, serviceBean);
		}
		
		Collection<Template> filterTemlates = descriptor.getDefaultVariantDescriptor().getSearch().getTemplates();
		List<Template> documentTypes = editor.getFullTemplates(serviceBean, filterTemlates);

		return documentTypes;
	}
	
	private List<Integer> getFilterYears() {
		return editor.generateFilterYears();
	}
	
	private CardLinkPickerDescriptor getDescriptor(Attribute attr, DataServiceBean serviceBean) throws DataException {
		if(null != descriptor) {
			return descriptor;
		}
		
		try {
			CardLinkPickerDescriptorReader reader = new CardLinkPickerDescriptorReader(serviceBean);
			InputStream stream = getConfigStream();			
			descriptor = reader.read(stream, attr);
		} catch (XPathExpressionException e) {
			logger.error("Failed to read config file", e);
			throw new DataException(e);
		} catch (SAXException e) {
			logger.error("Failed to read config file", e);
			throw new DataException(e);
		} catch (IOException e) {
			logger.error("Failed to read config file", e);
			throw new DataException(e);
		} catch (ParserConfigurationException e) {
			logger.error("Failed to read config file", e);
			throw new DataException(e);
		} catch (ParseException e) {
			logger.error("Failed to read config file", e);
			throw new DataException(e);
		} catch (ServiceException e) {
			logger.error("Failed to read config file", e);
			throw new DataException(e);
		}
		
 		return descriptor;
	}
	
	protected InputStream getConfigStream() throws DataException {
		try {
			return Portal.getFactory().getConfigService().loadConfigFile(AttributeEditorFactory.CONFIG_FOLDER + config);
		} catch (IOException e) {
			logger.error("Couldn't open hierarchy descriptor file: " + config, e);
			throw new DataException(e);
		}
	}
}
