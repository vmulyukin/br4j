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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.ireferent.actions.ActionDescriptor;
import com.aplana.ireferent.actions.ActionHandler;
import com.aplana.ireferent.actions.conditions.Condition;
import com.aplana.ireferent.actions.conditions.ConditionPerformed;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ReflectionUtils;
import com.aplana.ireferent.util.XmlUtils;

public class ActionsConfigReader extends ConfigReader<ActionDescriptor> {

    public final static String ACTIONS_PACKAGE = "com.aplana.ireferent.actions.";
    public final static String CONDITIONS_PACKAGE = "com.aplana.ireferent.actions.conditions.";

    @Override
    protected List<ActionDescriptor> readConfigs(Element root)
	    throws ConfigurationException {
	List<ActionDescriptor> descriptors = new ArrayList<ActionDescriptor>();
	NodeList actionElements = XmlUtils.getSubElements(root, "action");
	for (int i = 0; i < actionElements.getLength(); ++i) {
	    Element el = (Element) actionElements.item(i);
	    descriptors.add(readDescriptor(el));
	}
	return descriptors;
    }

    private ActionDescriptor readDescriptor(Element actionElement) {
	ActionDescriptor descriptor = new ActionDescriptor();
	descriptor.setTitle(readActionTitle(actionElement));
	descriptor.setId(readActionId(actionElement));
	descriptor.setObjectType(readActionObjectType(actionElement));
	for (ActionHandler actionHandler : readActionHandlers(actionElement)) {
	    descriptor.addHandlerWithconditions((Condition) new ConditionPerformed(), actionHandler);
	}
	Map<Condition, Element> conditions = readActionConditions(actionElement);
	for (Condition condition : conditions.keySet()) {
		for (ActionHandler actionHandler : readActionHandlers(conditions.get(condition))) {
		    descriptor.addHandlerWithconditions(condition, actionHandler);
		}
	}
	return descriptor;
    }

    private String readActionTitle(Element actionElement) {
	Element titleElement = XmlUtils.getSingleSubElement(actionElement,
		"title");
	return titleElement == null ? "" : titleElement.getTextContent();
    }

    private String readActionId(Element actionElement) {
	Element idElement = XmlUtils.getSingleSubElement(actionElement, "id");
	if (idElement == null || "".equals(idElement.getTextContent()))
	    throw new ConfigurationException(
		    "Mandatory id element is not defined");
	return idElement.getTextContent();
    }

    private Class<? extends WSObject> readActionObjectType(Element actionElement) {
	Element objTypeElement = XmlUtils.getSingleSubElement(actionElement,
		"objType");
	if (objTypeElement == null
		|| "".equals(objTypeElement.getTextContent()))
	    throw new ConfigurationException(
		    "Mandatory objType element is not defined");
	String simpleClassName = objTypeElement.getTextContent();
	return super.initializeWSObjectType(simpleClassName);
    }
    
    private Map<Condition, Element> readActionConditions(Element actionElement) {
    	Map<Condition, Element> conditions = new LinkedHashMap<Condition, Element>();
    	NodeList conditionElements = XmlUtils.getSubElements(actionElement, "when");
    	for (int i = 0; i < conditionElements.getLength(); ++i) {
    		 Element conditionElement = (Element) conditionElements.item(i);
    		 conditions.put(readActionCondition(conditionElement), conditionElement);
    	}
    	return conditions;
    }
    
    private Condition readActionCondition(Element conditionElement) {
    	Condition condition = createActionCondition(conditionElement);
    	Map<String, String> parameters = readParameters(conditionElement);
    	for (Entry<String, String> parameter : parameters.entrySet()) {
    		condition.setParameter(parameter.getKey(), parameter.getValue());
    	}
    	return condition;
    }
    
    private Condition createActionCondition(Element conditionElement) {
    	String simpleClassName = conditionElement.getAttribute("name");
    	if ("".equals(simpleClassName))
    	    throw new ConfigurationException(
    		    "Mandatory 'name' attribute of condition is not defined");
    	return ReflectionUtils.instantiateClass(Condition.class,
    			CONDITIONS_PACKAGE + simpleClassName);
    }
    private List<ActionHandler> readActionHandlers(Element actionElement) {
	List<ActionHandler> handlers = new ArrayList<ActionHandler>();
	NodeList handlerElements = XmlUtils.getSubElements(actionElement,
		"handler");
	for (int i = 0; i < handlerElements.getLength(); ++i) {
	    Element handlerElement = (Element) handlerElements.item(i);
	    handlers.add(readActionHandler(handlerElement));
	}
	return handlers;
    }

    private ActionHandler readActionHandler(Element handlerElement) {
	ActionHandler handler = createActionHandler(handlerElement);
	Map<String, String> parameters = readParameters(handlerElement);
	for (Entry<String, String> parameter : parameters.entrySet()) {
	    handler.setParameter(parameter.getKey(), parameter.getValue());
	}
	return handler;
    }

    private ActionHandler createActionHandler(Element handlerElement) {
	String simpleClassName = handlerElement.getAttribute("name");
	if ("".equals(simpleClassName))
	    throw new ConfigurationException(
		    "Mandatory 'name' attribute of handler is not defined");
	return ReflectionUtils.instantiateClass(ActionHandler.class,
		ACTIONS_PACKAGE + simpleClassName);
    }

    private Map<String, String> readParameters(
	    Element handlerElement) {
	Map<String, String> parameters = new HashMap<String, String>();
	Element parametersElement = XmlUtils.getSingleSubElement(
		handlerElement, "parameters");
	if (parametersElement != null) {
	    NodeList parameterList = XmlUtils.getSubElements(parametersElement,
		    "parameter");
	    for (int i = 0; i < parameterList.getLength(); ++i) {
		Element parameter = (Element) parameterList.item(i);
		String parameterName = parameter.getAttribute("name");
		if (!"".equals(parameterName)) {
		    parameters.put(parameterName, parameter.getTextContent());
		}
	    }
	}
	return parameters;
    }
}
