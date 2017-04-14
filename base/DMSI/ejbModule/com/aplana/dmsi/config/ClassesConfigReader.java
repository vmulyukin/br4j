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
package com.aplana.dmsi.config;

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
import com.aplana.dmsi.Parametrized;
import com.aplana.dmsi.card.DMSIObjectConverter;
import com.aplana.dmsi.config.AttributeIdValueSource.Target;
import com.aplana.dmsi.util.ReflectionUtils;
import com.aplana.dmsi.util.XmlUtils;
import com.aplana.dmsi.value.controllers.ControllerFactory;
import com.aplana.dmsi.value.converters.Converter;
import com.aplana.dmsi.value.filters.CollectionFilter;

public class ClassesConfigReader extends ConfigReader<ClassConfig> {

    private Map<Class<?>, ClassConfig> configsByClass = new HashMap<Class<?>, ClassConfig>();

    @Override
    protected List<ClassConfig> readConfigs(Element root)
	    throws ConfigurationException {
	List<ClassConfig> configs = new ArrayList<ClassConfig>();
	NodeList configElements = XmlUtils.getSubElements(root, "config");
	for (int i = 0; i < configElements.getLength(); ++i) {
	    ClassConfig config = parseConfigElement((Element) configElements
		    .item(i));
	    configsByClass.put(config.getType(), config);
	    configs.add(config);
	}
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
	    Class<?> extendsType = initializeDMSIType(extendsClassName);
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
    }

    private Class<?> readConfigType(Element configElement)
	    throws ConfigurationException {
	String className = configElement.getAttribute("forClass");
	if ("".equals(className)) {
	    throw new ConfigurationException("Class of config is not defined");
	}
	return initializeDMSIType(className);
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
	fieldConfig.setUseInSearch(readFieldUseInSearch(fieldElement));
	for (ValueSource source : readValueSources(getValuesSourcesElement(fieldElement))) {
	    fieldConfig.addValueSource(source);
	}
	fieldConfig.setConverter(readConverter(fieldElement));
	fieldConfig.setTypeName(readType(fieldElement));
	fieldConfig.setControllerFactory(readControllerFactory(fieldElement));
	if (fieldConfig.getValueSources().isEmpty()) {
	    throw new ConfigurationException("Value source is not defined for "
		    + fieldConfig.getFieldName());
	}
	fieldConfig
		.setComplexFieldConverter(readComplexFieldConverter(fieldElement));
	fieldConfig.setIgnoringFields(readFieldIgnoringFields(fieldElement));
	fieldConfig.setFilter(readFilter(fieldElement));

    }

    private boolean readFieldReadonly(Element fieldElement) {
	String isFieldReadonly = fieldElement.getAttribute("readonly");
	return Boolean.parseBoolean(isFieldReadonly);
    }

    private boolean readFieldNewCard(Element fieldElement) {
	String isNewCard = fieldElement.getAttribute("newCard");
	return Boolean.parseBoolean(isNewCard);
    }

    private boolean readFieldUseInSearch(Element fieldElement) {
	String isNewCard = fieldElement.getAttribute("useInSearch");
	return Boolean.parseBoolean(isNewCard);
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

    private Element getValuesSourcesElement(Element fieldElement) {
	return XmlUtils.getSingleSubElement(fieldElement, "valueSources");
    }

    private Collection<ValueSource> readValueSources(Element valueSourcesElement) {
	Collection<ValueSource> sources = new ArrayList<ValueSource>();
	if (valueSourcesElement == null) {
	    return sources;
	}
	NodeList valuesSourcesElements = XmlUtils
		.getAllSubElements(valueSourcesElement);
	for (int i = 0; i < valuesSourcesElements.getLength(); ++i) {
	    Element valueSourceElement = (Element) valuesSourcesElements
		    .item(i);
	    String tagName = valueSourceElement.getTagName();
	    if ("attribute".equals(tagName)) {
		ObjectId attributeId = readAttribute(valueSourceElement);
		boolean isReadOnly = readValueSourceReadOnly(valueSourceElement);
		AttributeIdValueSource valueSource = new AttributeIdValueSource(
			attributeId, isReadOnly);
		valueSource
			.setTarget(readValueSourceTarget(valueSourceElement));
		sources.add(valueSource);
	    } else if ("config".equals(tagName)) {
		ClassConfig config = parseConfigElement(valueSourceElement);
		boolean isReadOnly = readValueSourceReadOnly(valueSourceElement);
		sources.add(new ClassConfigValueSource(config, isReadOnly));
	    } else if ("self".equals(tagName)) {
		boolean isReadOnly = readValueSourceReadOnly(valueSourceElement);
		sources.add(new SelfValueSource(isReadOnly));
	    } else if ("group".equals(tagName)) {
		boolean isReadOnly = readValueSourceReadOnly(valueSourceElement);
		Collection<ValueSource> valueSources = readValueSources(valueSourceElement);
		sources.add(new GroupValueSource(valueSources, isReadOnly));
	    } else if ("value".equals(tagName)) {
		sources.add(new StaticValueSource(readValue(valueSourceElement)));
	    }
	}
	return sources;
    }

    private ObjectId readAttribute(Element attributeIdElement) {
	Class<?> attributeType = AttrUtils.getAttrClass(attributeIdElement
		.getAttribute("type"));
	return ObjectIdUtils.getObjectId(attributeType, attributeIdElement
		.getTextContent(), false);
    }

    private String readValue(Element valueElement) {
	return valueElement.getTextContent();
    }

    private boolean readValueSourceReadOnly(Element valueSourceElement) {
	return Boolean
		.parseBoolean(valueSourceElement.getAttribute("readOnly"));
    }

    private Target readValueSourceTarget(Element valueSourceElement) {
	String targetValue = valueSourceElement.getAttribute("target");
	if ("name".equals(targetValue))
	    return Target.ATTR_NAME;
	return Target.ATTR_VALUE;
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

    private DMSIObjectConverter readComplexFieldConverter(Element fieldElement) {
	Element converterElement = XmlUtils.getSingleSubElement(fieldElement,
		"complexFieldConverter");
	if (converterElement == null) {
	    return null;
	}

	String converterClassName = converterElement.getAttribute("class");
	DMSIObjectConverter converter = ReflectionUtils.instantiateClass(
		DMSIObjectConverter.class, converterClassName);
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

    private String[] readFieldIgnoringFields(Element fieldElement) {
	Element ignoringFieldsElement = XmlUtils.getSingleSubElement(
		fieldElement, "ignoringFields");
	if (ignoringFieldsElement == null) {
	    return new String[0];
	}
	String textValue = ignoringFieldsElement.getTextContent();
	return textValue.trim().split("\\s*,\\s*");
    }

    private CollectionFilter readFilter(Element fieldElement) {
	Element filterElement = XmlUtils.getSingleSubElement(fieldElement,
		"filter");
	if (filterElement == null) {
	    return null;
	}
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
