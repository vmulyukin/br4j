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
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.ireferent.WSObjectFactory;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ReflectionUtils;
import com.aplana.ireferent.util.XmlUtils;

public class TypesConfigReader extends ConfigReader<Type> {

    /*
     * (non-Javadoc)
     *
     * @see com.aplana.ireferent.config.ConfigReader#readConfigs(org.w3c.dom.Element)
     */
    @Override
    protected List<Type> readConfigs(Element root)
	    throws ConfigurationException {
	List<Type> types = new ArrayList<Type>();
	NodeList typeDefElements = XmlUtils.getSubElements(root, "typedef");
	for (int i = 0; i < typeDefElements.getLength(); ++i) {
	    Element typeDefElement = (Element) typeDefElements.item(i);
	    Type type = new Type();
	    type.setName(readTypeName(typeDefElement));
	    for (Type.Entity entity : readEntities(typeDefElement)) {
		type.addEntity(entity);
	    }
	    type.setFactory(readFactory(typeDefElement));
	    types.add(type);
	}
	return types;
    }

    private String readTypeName(Element typeElement)
	    throws ConfigurationException {
	String typeName = typeElement.getAttribute("name");
	if ("".equals(typeName)) {
	    throw new ConfigurationException(
		    "Mandatory attribute 'name' is absent");
	}
	return typeName;
    }

    private Collection<Type.Entity> readEntities(Element typeElement) {
	NodeList entitiesList = XmlUtils.getSubElements(typeElement, "entity");
	Collection<Type.Entity> entities = new ArrayList<Type.Entity>();
	for (int i = 0; i < entitiesList.getLength(); ++i) {
	    Element entityElement = (Element) entitiesList.item(i);
	    entities.add(readEntity(entityElement));
	}
	return entities;
    }

    private Type.Entity readEntity(Element entityElement) {
	Type.Entity entity = new Type.Entity();
	Class<? extends WSObject> entityTypeClass = readEntityTypeClass(entityElement);
	entity.setType(readEntityTypeClass(entityElement));
	entity.setMType(readEntityMTypeClass(entityElement));
	entity.setTemplateId(getTemplateId(entityTypeClass));
	return entity;
    }

    private Class<? extends WSObject> readEntityTypeClass(Element entityElement) {
	Class<? extends WSObject> typeClass = readClassFromSubElement(
		entityElement, "typeClass");
	if (typeClass == null)
	    throw new ConfigurationException(
		    "Mandatory typeClass element is not found");

	return typeClass;
    }

    private ObjectId getTemplateId(Class<? extends WSObject> typeClass) {
	ClassConfig typeConfig = ClassConfigManager.instance()
		.getConfigByClass(typeClass);
	ObjectId templateId = typeConfig.getTemplateId();
	if (templateId == null) {
	    throw new ConfigurationException(
		    "Template is not defined for type " + typeClass);
	}
	return templateId;
    }

    private Class<? extends WSObject> readEntityMTypeClass(Element entityElement) {
	return readClassFromSubElement(entityElement, "mTypeClass");
    }

    private Class<? extends WSObject> readClassFromSubElement(Element parent,
	    String elementName) {
	Element subElement = XmlUtils.getSingleSubElement(parent, elementName);
	if (subElement != null && !"".equals(subElement.getTextContent())) {
	    return initializeWSObjectType(subElement.getTextContent());
	}
	return null;
    }

    private Class<? extends WSObjectFactory> readFactory(Element configElement) {
	Element factoryElement = XmlUtils.getSingleSubElement(configElement,
		"factory");

	if (factoryElement != null
		&& !"".equals(factoryElement.getTextContent())) {
	    return ReflectionUtils.initializeClass(WSObjectFactory.class,
		    factoryElement.getTextContent());
	}
	return null;
    }
}
