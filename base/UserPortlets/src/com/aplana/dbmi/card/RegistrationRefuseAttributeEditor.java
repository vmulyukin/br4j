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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;

/**
 * Represents registration refuse reasons picker for StringAttribute.
 * Refuse reasons list content depends on delivery method (e.g. medo, gost, delo).
 * Also the picker can be configured to be selectable but not editable for different delivery methods (e.g. for medo). 
 * Default is editable (for gost and others)
 * 
 * @author vialeksandrov
 *
 */

public class RegistrationRefuseAttributeEditor extends StringAttributeEditor
{
	public static final String PARAM_OPTIONS = "options";
	public static final String PARAM_IS_EDITABLE = "isEditable";
	private Set<String> defaultOptions = null;
	private Set<String> medoOptions = null;
	private Set<String> gostOptions = null;
	private Set<String> deloOptions = null;
	private List<String> notEditableMethods = new ArrayList<String>();
	private String configFileName;

	public enum DeliveryMethod {
	    medo, gost, delo
	}
	
	public RegistrationRefuseAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/RegistrationRefuseOptions.jsp");
	}
	
	public void setParameter(String name, String value){
		if(name.equalsIgnoreCase("config")) {
			configFileName = value;
		}else if (name.equalsIgnoreCase("notEditableMethods")){
			notEditableMethods = Arrays.asList(value.split(","));
		}else {
			super.setParameter(name, value);
		}
	}
	
	public void writeEditorCode(RenderRequest request, RenderResponse response, Attribute attr) throws IOException, PortletException{
		CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
		Set<String> methodOptions = new HashSet<String>();
		boolean isEditable = true;
		if (null != sessionBean.getActiveCard()){
			final ListAttribute cardAttr = (ListAttribute)sessionBean.getActiveCard()
					.getAttributeById(ObjectId.predefined(ListAttribute.class, "jbr.deliveryItem.method"));
			if (cardAttr != null) {
				ObjectId valueId = cardAttr.getValue() == null ? null : cardAttr.getValue().getId();
				if (valueId != null) {
					if (valueId.equals(ObjectId.predefined(ReferenceValue.class, "jbr.deliveryItem.method.medo"))){	
						methodOptions = getMedoOptions();
					}else if (valueId.equals(ObjectId.predefined(ReferenceValue.class, "jbr.deliveryItem.method.gost"))){
						methodOptions = getGostOptions();
					}else if (valueId.equals(ObjectId.predefined(ReferenceValue.class, "jbr.deliveryItem.method.delo"))){
						methodOptions = getDeloOptions();
					}

					if (notEditableMethods.contains(valueId.getId().toString())){
						isEditable = false;
					}
				}
			}
		}
		if (!methodOptions.isEmpty()) {
			request.setAttribute(PARAM_OPTIONS, methodOptions);
		}else {
			request.setAttribute(PARAM_OPTIONS, getDefaultOptions());
		}
		request.setAttribute(PARAM_IS_EDITABLE, isEditable);
		super.writeEditorCode(request, response, attr);
	}
	
	private Set<String> readConfig(String filename, String deliveryMethod){
		Set<String> options = new HashSet<String>();
		try {
			final InputStream stream = Portal.getFactory().getConfigService().loadConfigFile(AttributeEditorFactory.CONFIG_FOLDER + filename);
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
		    Element root = doc.getDocumentElement();
		    XPath xpath = XPathFactory.newInstance().newXPath();
		    NodeList values = (NodeList) xpath.evaluate("/options/" + deliveryMethod + "/option/text()", root, XPathConstants.NODESET);
		    for(int i = 0; i < values.getLength(); i++){
		    	options.add(values.item(i).getTextContent());
		    }
		} catch (Exception e) {
			logger.error("Error while reading from configuration file (" + filename + ")", e);
		}
		return options;
	}
	
	public Set<String> getDefaultOptions() {
		if (null == defaultOptions) {
			defaultOptions = new HashSet<String>();
			defaultOptions =  readConfig(configFileName, "default");
		}
		return defaultOptions;
	}

	public Set<String> getMedoOptions() {
		if (null == medoOptions) {
			medoOptions = new HashSet<String>();
			medoOptions =  readConfig(configFileName, DeliveryMethod.medo.name());
		}
		return medoOptions;
	}

	public Set<String> getGostOptions() {
		if (null == gostOptions) {
			gostOptions = new HashSet<String>();
			gostOptions =  readConfig(configFileName, DeliveryMethod.gost.name());
		}
		return gostOptions;
	}

	public Set<String> getDeloOptions() {
		if (null == deloOptions) {
			deloOptions = new HashSet<String>();
			deloOptions =  readConfig(configFileName, DeliveryMethod.delo.name());
		}
		return deloOptions;
	}
}
