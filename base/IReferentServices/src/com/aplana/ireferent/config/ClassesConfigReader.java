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
package com.aplana.ireferent.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.ireferent.Parametrized;
import com.aplana.ireferent.util.ReflectionUtils;
import com.aplana.ireferent.util.XmlUtils;
import com.aplana.ireferent.value.controllers.ControllerFactory;
import com.aplana.ireferent.value.converters.Converter;
import com.aplana.ireferent.value.filters.CollectionFilter;

public class ClassesConfigReader extends ConfigReader<ClassConfig> {

    private Map<Class<?>, ClassConfig> configsByClass;

    @Override
    protected List<ClassConfig> readConfigs(Element root)
	    throws ConfigurationException {
	configsByClass = new HashMap<Class<?>, ClassConfig>();
	List<ClassConfig> configs = new ArrayList<ClassConfig>();
	NodeList configElements = XmlUtils.getSubElements(root, "config");
	for (int i = 0; i < configElements.getLength(); ++i) {
	    ClassConfig config = parseConfigElement((Element) configElements
		    .item(i));
	    configsByClass.put(config.getType(), config);
	}
	configs.addAll(configsByClass.values());
	return configs;
    }

    private ClassConfig parseConfigElement(Element configElement)
	    throws ConfigurationException {
	ClassConfig conf = createConfig(configElement);
	fillConfig(conf, configElement);
	return conf;
    }

    private ClassConfig createConfig(Element configElement)
	    throws ConfigurationException {
	String extendsClassName = configElement.getAttribute("extendsConfigOf");
	ClassConfig conf;
	if ("".equals(extendsClassName)) {
	    conf = new ClassConfig();
	} else {
	    Class<?> extendsType = initializeIReferentType(extendsClassName);
	    if (configsByClass.containsKey(extendsType)) {
		conf = configsByClass.get(extendsType).clone();
	    } else {
		throw new ConfigurationException(String
			.format("The base config for %s is not found",
				extendsClassName));
	    }
	}
	return conf;
    }

    private void fillConfig(ClassConfig conf, Element configElement)
	    throws ConfigurationException {
	conf.setType(readConfigType(configElement));
	ObjectId templateId = readTemplate(configElement);
	if (templateId != null) {
	    conf.setTemplateId(templateId);
	}
	for (FieldConfig fieldConfig : readFields(configElement)) {
	    conf.addFieldConfig(fieldConfig);
	}
	for (ExtensionConfig extensionConfig : readExtensions(configElement)) {
	    conf.addExtensionConfig(extensionConfig);
	}
    }

    private Class<?> readConfigType(Element configElement)
	    throws ConfigurationException {
	String className = configElement.getAttribute("forClass");
	if ("".equals(className)) {
	    throw new ConfigurationException("Class of config is not defined");
	}
	return initializeIReferentType(className);
    }

    private ObjectId readTemplate(Element configElement) {
	Element templateElement = XmlUtils.getSingleSubElement(configElement,
		"template");
	if (templateElement != null) {
	    return ObjectIdUtils.getObjectId(Template.class, templateElement
		    .getTextContent().trim(), true);
	}
	return null;
    }

    private Collection<FieldConfig> readFields(Element configElement)
	    throws ConfigurationException {
	Collection<FieldConfig> fieldConfigs = new ArrayList<FieldConfig>();
	NodeList fieldsList = XmlUtils.getSubElements(configElement, "field");
	for (int i = 0; i < fieldsList.getLength(); i++) {
	    Element fieldElement = (Element) fieldsList.item(i);
	    fieldConfigs.add(readField(fieldElement));
	}
	return fieldConfigs;
    }

    private FieldConfig readField(Element fieldElement)
	    throws ConfigurationException {
	FieldConfig fieldConfig = new FieldConfig();
	fillFieldConfig(fieldConfig, fieldElement);
	return fieldConfig;
    }

    private void fillFieldConfig(FieldConfig fieldConfig, Element fieldElement) {
	fieldConfig.setReadonly(readFieldReadonly(fieldElement));
	fieldConfig.setFieldName(readFieldName(fieldElement));
	fieldConfig.setNewCard(readFieldNewCard(fieldElement));
	fieldConfig.setParent(readFieldParent(fieldElement));
	fieldConfig.setAttributeId(readAttribute(fieldElement));
	fieldConfig.setConverter(readConverter(fieldElement));
	fieldConfig.setTypeName(readType(fieldElement));
	fieldConfig.setConfig(readFieldConfig(fieldElement));
	fieldConfig.setControllerFactory(readControllerFactory(fieldElement));
	if (fieldConfig.getAttributeId() == null
		&& fieldConfig.getConfig() == null) {
	    throw new ConfigurationException(
		    "Either attribute or config should be defined for field "
			    + fieldConfig.getFieldName());
	}

	fieldConfig.setFilters(readFilters(fieldElement));
    }

    private boolean readFieldReadonly(Element fieldElement) {
	String isFieldReadonly = fieldElement.getAttribute("readonly");
	return Boolean.parseBoolean(isFieldReadonly);
    }

    private boolean readFieldNewCard(Element fieldElement) {
	String isNewCard = fieldElement.getAttribute("newCard");
	return Boolean.parseBoolean(isNewCard);
    }
    
    private boolean readFieldParent(Element fieldElement) {
    String isParent = fieldElement.getAttribute("isParent");
    return Boolean.parseBoolean(isParent);
    }

    private String readFieldName(Element fieldElement)
	    throws ConfigurationException {
	String fieldName = fieldElement.getAttribute("name");
	if ("".equals(fieldName)) {
	    throw new ConfigurationException(
		    "Name attribute is not defined. Skip field.");
	}
	return fieldName;
    }

    private ObjectId readAttribute(Element fieldElement) {
	Element attributeElement = XmlUtils.getSingleSubElement(fieldElement,
		"attribute");
	if (attributeElement == null) {
	    return null;
	}
	Class<?> attributeType = AttrUtils.getAttrClass(attributeElement
		.getAttribute("type"));
	return ObjectIdUtils.getObjectId(attributeType, attributeElement
		.getTextContent(), false);
    }

    private Converter readConverter(Element fieldElement) {
	Element converterElement = XmlUtils.getSingleSubElement(fieldElement,
		"converter");
	if (converterElement == null) {
	    return null;
	}
	String converterClassName = converterElement.getAttribute("class");
	Converter converter = ReflectionUtils.instantiateClass(Converter.class,
		converterClassName);
	if (converter instanceof Parametrized) {
	    NodeList parameterElements = XmlUtils.getSubElements(
		    converterElement, "parameter");
	    for (int j = 0; j < parameterElements.getLength(); ++j) {
		Element parameterElement = (Element) parameterElements.item(j);
		String parameterKey = parameterElement.getAttribute("name");
		String parameterValue = parameterElement.getTextContent();
		((Parametrized) converter).setParameter(parameterKey,
			parameterValue);
	    }
	}
	return converter;
    }

    private String readType(Element fieldElement) throws ConfigurationException {
	Element typeElement = XmlUtils
		.getSingleSubElement(fieldElement, "type");
	if (typeElement == null) {
	    return "";
	}
	return typeElement.getAttribute("name");
    }

    private ClassConfig readFieldConfig(Element fieldElement)
	    throws ConfigurationException {
	Element subConfigElement = XmlUtils.getSingleSubElement(fieldElement,
		"config");
	if (subConfigElement == null) {
	    return null;
	}
	return parseConfigElement(subConfigElement);
    }

    private ControllerFactory readControllerFactory(Element fieldElement) {
	Element controllerElement = XmlUtils.getSingleSubElement(fieldElement,
		"controllerFactory");
	if (controllerElement == null) {
	    return null;
	}
	String controllerClassName = controllerElement.getAttribute("class");
	ControllerFactory controllerFactory = ReflectionUtils.instantiateClass(
		ControllerFactory.class, controllerClassName);
	return controllerFactory;
    }

    private Collection<ExtensionConfig> readExtensions(Element configElement)
	    throws ConfigurationException {
	Collection<ExtensionConfig> extensionConfigs = new ArrayList<ExtensionConfig>();
	NodeList extensionsList = XmlUtils.getSubElements(configElement,
		"extension");
	for (int i = 0; i < extensionsList.getLength(); i++) {
	    Element extensionElement = (Element) extensionsList.item(i);
	    extensionConfigs.add(readExtension(extensionElement));
	}
	return extensionConfigs;
    }

    private ExtensionConfig readExtension(Element extensionElement)
	    throws ConfigurationException {
	ExtensionConfig extensionConfig = new ExtensionConfig();
	fillExtensionConfig(extensionConfig, extensionElement);
	return extensionConfig;
    }

    private void fillExtensionConfig(ExtensionConfig extensionConfig,
	    Element extensionElement) {
	fillFieldConfig(extensionConfig, extensionElement);
	extensionConfig.setMultiValued(readMultivalued(extensionElement));
    }

    private boolean readMultivalued(Element extensionElement)
	    throws ConfigurationException {
	return Boolean.parseBoolean(extensionElement
		.getAttribute("multivalued"));
    }
    
    private Map<String, CollectionFilter> readFilters(Element fieldElement)
    	throws ConfigurationException {
    	Map<String, CollectionFilter> filters = new HashMap<String, CollectionFilter>();
    	NodeList filtersList = XmlUtils.getSubElements(fieldElement, "filter");
    	for (int i = 0; i < filtersList.getLength(); i++) {
    		Element filterElement = (Element) filtersList.item(i);
    		String filterSetId = filterElement.getAttribute("forSetId");
    		CollectionFilter filter = readFilter(filterElement);
    		filters.put(filterSetId, filter);
    	}
    	return filters;
    }
    
	private CollectionFilter readFilter(Element filterElement)
	{
		String filterClassName = filterElement.getAttribute("class");
		
		CollectionFilter filter = ReflectionUtils.instantiateClass(
				CollectionFilter.class, filterClassName);
		if (filter instanceof Parametrized) {
			NodeList parameterElements = XmlUtils.getSubElements(filterElement,
    		    "parameter");
			for (int j = 0; j < parameterElements.getLength(); ++j) {
				Element parameterElement = (Element) parameterElements.item(j);
				String parameterKey = parameterElement.getAttribute("name");
				String parameterValue = parameterElement.getTextContent();
				((Parametrized) filter).setParameter(parameterKey,
						parameterValue);
			}
		}
		return filter;
	}
}
