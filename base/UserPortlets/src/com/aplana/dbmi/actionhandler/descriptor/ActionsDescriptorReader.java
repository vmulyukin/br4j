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
package com.aplana.dbmi.actionhandler.descriptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import com.aplana.dbmi.card.ExtraJavascriptInfo;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.aplana.dbmi.actionhandler.ActionHandler;
import com.aplana.dbmi.card.hierarchy.Messages;
import com.aplana.dbmi.card.hierarchy.descriptor.ConditionReader;
import com.aplana.dbmi.card.hierarchy.descriptor.MessagesReader;
import com.aplana.dbmi.card.util.AbstractXmlDescriptorReader;
import com.aplana.dbmi.model.LocalizedString;
import com.aplana.dbmi.service.DataServiceBean;

public class ActionsDescriptorReader extends AbstractXmlDescriptorReader {
    private static final String TAG_EXTRA_JAVASCRIPT = "extraJavascript";
    private static final String TAG_PARAMETER = "parameter";
	private static final String ATTR_CLASS = "class";
	private static final String ATTR_NAME = "name";

    private ConditionReader conditionReader;

    public ActionsDescriptorReader(XPath xpath, ConditionReader conditionReader) throws XPathExpressionException {
        super(xpath);
        this.conditionReader = conditionReader;
    }

    public ActionsDescriptorReader() throws XPathExpressionException {
        this.conditionReader = new ConditionReader(this.xpath);
    }

    public ActionsDescriptor readFromFile(InputStream stream, DataServiceBean serviceBean) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
        Element root = doc.getDocumentElement();
        MessagesReader mr = new MessagesReader(xpath);
        Element elem = (Element) xpath.evaluate("./messages", root, XPathConstants.NODE);
        Messages messages = mr.read(elem);
        elem = (Element) xpath.evaluate("./actions", root, XPathConstants.NODE);
        return readFromNode(elem, messages, serviceBean);
    }

    @SuppressWarnings("unchecked")
	public ActionsDescriptor readFromNode(Element actionsElement, Messages messages, DataServiceBean serviceBean) throws XPathExpressionException {
        final NodeList actionNodes = (NodeList) xpath.evaluate("./action", actionsElement, XPathConstants.NODESET);

        final ActionsDescriptor result = new ActionsDescriptor();
        result.setMessages(messages);
        // ������������� ����� ���������� �������
        final String comparator = actionsElement.getAttribute("sort");
        if(!StringUtils.isEmpty(comparator)) {
        	try {
        		final Class<Comparator<ActionHandlerDescriptor>> clazz = (Class<Comparator<ActionHandlerDescriptor>>) Class.forName(comparator);
				result.setComp(clazz.newInstance());
        	} catch (Exception e) {
        		if(logger.isErrorEnabled())
        			logger.error("Couldn't find Comparator<ActionHandlerDescriptor> class with name: " + comparator, e);
        	}
        }
        for (int i = 0; i < actionNodes.getLength(); ++i) {
            Element actionElem = (Element) actionNodes.item(i);
            ActionHandlerDescriptor ad = new ActionHandlerDescriptor();
            String attr = actionElem.getAttribute("handler");
            ExtraJavascriptInfo ajsd;
            //check handler
            //handler can be so JS function as Java function

            try {

                ajsd = getExtraJavascriptDescriptor(actionElem);
                if (ajsd != null) {

                    com.aplana.dbmi.card.actionhandler.JSActionHandler.class.getCanonicalName();

                    ad.setExtraJavascriptInfo(ajsd);
                }


            } catch (Exception e) {
            	if(logger.isErrorEnabled())
            		logger.error("Couldn't create Javascript ActionExtraJavascriptDescriptor", e);
                continue;
            }


            if ((attr == null || attr.length() <= 0) && ajsd != null) {
                attr = getDefaultJSHandlerClass();
            }

            if ((attr == null || attr.length() <= 0)) {
            	if(logger.isErrorEnabled())
            		logger.error("Couldn't find ActionHandler class with name: " + attr);
                continue;
            }

            try {
                ad.setHandlerClass((Class<ActionHandler>) Class.forName(attr));
            } catch (Exception e) {
            	if(logger.isErrorEnabled())
            		logger.error("Couldn't find ActionHandler class with name: " + attr, e);
                continue;
            }


            Element conditionNode = (Element) xpath.evaluate("./condition", actionElem, XPathConstants.NODE);
            if (conditionNode != null) {
                ad.setCondition(conditionReader.readCondition(conditionNode, serviceBean));
            }

            final NodeList params = (NodeList) xpath.evaluate("./parameters/parameter", actionElem, XPathConstants.NODESET);
            final Map<String, String> paramMap = new HashMap<String, String>();
            if (params != null && params.getLength() > 0) {
                for (int j = 0; j < params.getLength(); ++j) {
                    Element paramElem = (Element) params.item(j);
                    paramMap.put(paramElem.getAttribute("name"), paramElem.getAttribute("value"));
                }
            }
            ad.setParameters(paramMap);

            attr = actionElem.getAttribute("title");
            final LocalizedString title = messages.getMessage(attr);
            if (title == null) {
            	if(logger.isErrorEnabled())
            		logger.error("Title message with key '" + attr + "' is not found");
                continue;
            }
            ad.setTitle(title);

            attr = actionElem.getAttribute("confirmation");
            if (!"".equals(attr)) {
                final LocalizedString confirmation = messages.getMessage(attr);
                if (confirmation == null) {
                	if(logger.isErrorEnabled())
                		logger.error("Confirmation message with key '" + attr + "' is not found");
                    continue;
                }
                ad.setConfirmation(confirmation);
            }


            if (actionElem.hasAttribute("mode")) {
                attr = actionElem.getAttribute("mode");
                ad.setForEditMode("edit".equalsIgnoreCase(attr) || "all".equalsIgnoreCase(attr));
                ad.setForViewMode("view".equalsIgnoreCase(attr) || "all".equalsIgnoreCase(attr));
            }
            
            if (actionElem.hasAttribute("roleForMode")) {
                attr = actionElem.getAttribute("roleForMode");
                ad.setRoleForMode(attr);
            }
            
            if (actionElem.hasAttribute("permission")) {
                attr = actionElem.getAttribute("permission");
                ad.setNeedWritePermission("write".equalsIgnoreCase(attr));
            }

            attr = actionElem.getAttribute("selectionType");
            ad.setSelectionType(SelectionType.fromString(attr));
            attr = actionElem.getAttribute("id");
            ad.setId(attr);
            result.addItem(ad);
        }
        return result;
    }

    protected ExtraJavascriptInfo getExtraJavascriptDescriptor(Element actionNode) throws XPathExpressionException{

        ExtraJavascriptInfo extraJavascriptDescriptor = null;
        final NodeList extraJavascriptNodes = actionNode.getElementsByTagName(TAG_EXTRA_JAVASCRIPT);
        if (extraJavascriptNodes != null && extraJavascriptNodes.getLength() > 0) {

            extraJavascriptDescriptor = new ExtraJavascriptInfo();

            Element extraJavascriptNode = (Element) extraJavascriptNodes.item(0);
            if (!extraJavascriptNode.hasAttribute(ATTR_CLASS)) {
            	logger.warn("Action manager configuration error: no class defined for extraJavascript");
                throw new XPathExpressionException("Action manager configuration error: no class defined for extraJavascript");
            }

            String className = extraJavascriptNode.getAttribute(ATTR_CLASS);
            extraJavascriptDescriptor.setClassName(className);

            extraJavascriptDescriptor.setParams(new HashMap<String, String>());
            NodeList parameterNodes = extraJavascriptNode.getElementsByTagName(TAG_PARAMETER);
            if (parameterNodes != null && parameterNodes.getLength() > 0) {
                for (int k = 0; k < parameterNodes.getLength(); k++) {
                    Element param = (Element) parameterNodes.item(k);
                    if (param.getParentNode() != extraJavascriptNode)
                        continue;
                    if (!param.hasAttribute(ATTR_NAME)) {
                    	logger.warn("Action manager configuration error: parameter without name");
                        throw new XPathExpressionException("Action manager configuration error: parameter without name");
                    }
                    extraJavascriptDescriptor.getParams().put(param.getAttribute(ATTR_NAME), param.getTextContent());
                }
            }

        }
        return extraJavascriptDescriptor;

    }

    protected String getDefaultJSHandlerClass(){
        return "com.aplana.dbmi.card.actionhandler.JSActionHandler";
    }

}
