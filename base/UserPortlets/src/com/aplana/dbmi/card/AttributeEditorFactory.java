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

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.search.SearchFilterPortletSessionBean;
import com.aplana.dbmi.util.SmartMap;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;

public class AttributeEditorFactory {
	public static final String CONFIG_FOLDER = "dbmi/card/";
	public static final String CONFIG_FILE = CONFIG_FOLDER + "editors.xml";
	public static final String OVER_CONFIG_FILE_PATTERN = "*-editors.xml";
	
	private static AttributeEditorFactory singleton = null;
	
	public static AttributeEditorFactory getFactory() {
		if (singleton == null)
			singleton = new AttributeEditorFactory();
		return singleton;
	}
	
	public AttributeEditor getEditor(Attribute attr, CardPortletSessionBean session) {
		return createClass(root.getEditorClass(attr, session));
	}
	
	public AttributeEditor getViewer(Attribute attr, CardPortletSessionBean session) {
		return createClass(root.getViewerClass(attr, session));
	}

	/**
	 * Creates Search Filter Attribute Editor class for given attribute 
	 * @param attr the attribute to create search filter editor
	 */
	public AttributeEditor getFilterEditor(Attribute attr, SearchFilterPortletSessionBean sessionBean) {
		return createClass(root.getSearchFilterClass(attr, sessionBean));
	}	

	private AttributeEditor createClass(EditorInfo classInfo) {
		if (classInfo == null)
			return null;
		try {
			AttributeEditor editor = (AttributeEditor) Class.forName(classInfo.className).newInstance();
			if (classInfo.params != null && editor instanceof Parametrized) {
				for (Map.Entry<String, String> param: classInfo.params.entrySet()) {
					((Parametrized) editor).setParameter( param.getKey(), param.getValue());
				}
			}
			if (editor instanceof JspAttributeEditor) {
				((JspAttributeEditor) editor).setExtraJavascriptInfoList(classInfo.extraJavascriptInfoList);
			}
			return editor;
		} catch (Exception e) {
			logger.error("Error creating attribute editor or viewer", e);
			return null;
		}
	}

	private static final String TAG_ROOT = "attributes";
	private static final String TAG_SELECT = "select";
	private static final String TAG_CASE = "case";
	private static final String TAG_EDITOR = "editor";
	private static final String TAG_VIEWER = "viewer";
	private static final String TAG_SEARCH_FILTER = "filter";
	private static final String TAG_PARAMETER = "parameter";
	private static final String TAG_EXTRA_JAVASCRIPT = "extraJavascript";
	private static final String ATTR_PACKAGE = "package";
	private static final String ATTR_PROPERTY = "property";
	private static final String ATTR_SCOPE = "scope";
	private static final String ATTR_VALUE = "value";
	private static final String ATTR_CLASS = "class";
	private static final String ATTR_NAME = "name";
	private static final String VAL_SCOPE_SESSION = "session";
	
	private EditorMap root = new EditorMap();
	private Log logger = LogFactory.getLog(getClass());
	
	private AttributeEditorFactory() {
		try {
			initFromXml(Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE));
			initFromOverXml(Portal.getFactory().getConfigService().loadMultipleConfigFiles(CONFIG_FOLDER, OVER_CONFIG_FILE_PATTERN));
		} catch (Exception e) {
			logger.error("Can't read attribute editors/viewers configuration", e);
		}
	}
	
	private void initFromXml(InputStream configFile) {
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configFile);
			root = createEditorMap(doc.getDocumentElement(), "com.aplana.dbmi.card");
		} catch (Exception e) {
			logger.error("Error initializing attribute editors/viewers configuration", e);
		}
	}
	
	private void initFromOverXml(List<InputStream> streams){
		Document doc;
		for(InputStream in:streams){
			try {
				doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
				EditorMap otherEditorMap = createEditorMap(doc.getDocumentElement(), "com.aplana.dbmi.card");
				mergeEditorMaps(root,otherEditorMap);
			} catch (Exception e) {
				logger.error("Error initializing attribute editors/viewers configuration", e);
			}
		}
	}
	
	private void mergeEditorMaps(EditorMap rootMap, EditorMap otherMap){
		if(otherMap.defEditor != null){
			rootMap.defEditor = otherMap.defEditor;
		}
		if(otherMap.defViewer != null){
			rootMap.defViewer = otherMap.defViewer;
		}
		if(otherMap.defFilterEditor != null){
			rootMap.defFilterEditor = otherMap.defFilterEditor;
		}
		for (Entry<Object, Object> entry : otherMap.choices.getUndisclosed().entrySet()) {
			if (rootMap.choices.getUndisclosed().containsKey(entry.getKey())) {
				mergeEditorMaps((EditorMap) rootMap.choices.getUndisclosed().get(entry.getKey()), (EditorMap) entry.getValue());
			} else {
				rootMap.choices.put(entry.getKey(), entry.getValue());
			}
		}
	}

	private EditorMap createEditorMap(Element parent, String defPackage) {
		EditorMap map = new EditorMap();
		if (parent.hasAttribute(ATTR_PACKAGE))
			defPackage = parent.getAttribute(ATTR_PACKAGE);
		map.defEditor = defineEditor(getSingleNode(parent, TAG_EDITOR), defPackage);
		map.defViewer = defineEditor(getSingleNode(parent, TAG_VIEWER), defPackage);
		map.defFilterEditor = defineEditor(getSingleNode(parent, TAG_SEARCH_FILTER), defPackage);
		
		Element node = getSingleNode(parent, TAG_SELECT);
		if (node != null) {
			if (!node.hasAttribute(ATTR_PROPERTY)) {
				logger.warn("Attribute editors/viewers configuration error: no property defined in select");
				return map;
			}
			map.propertyName = node.getAttribute(ATTR_PROPERTY);
			map.sessionProperty = VAL_SCOPE_SESSION.equalsIgnoreCase(node.getAttribute(ATTR_SCOPE));
			if (node.hasAttribute(ATTR_PACKAGE))
				defPackage = node.getAttribute(ATTR_PACKAGE);
			NodeList nodes = node.getElementsByTagName(TAG_CASE);
			for (int i = 0; i < nodes.getLength(); i++) {
				Element elem = (Element) nodes.item(i);
				if (elem.getParentNode() != node)
					continue;
				map.choices.put(elem.hasAttribute(ATTR_VALUE) ? elem.getAttribute(ATTR_VALUE) : null, createEditorMap(elem, defPackage));
			}
		}
		return map;
	}

	private EditorInfo defineEditor(Element node, String packageName) {
		if (node == null)
			return null;
		if (!node.hasAttribute(ATTR_CLASS)) {
			logger.warn("Attribute editors/viewers configuration error: no class defined for editor");
			return null;
		}
		final EditorInfo info = new EditorInfo();
		if (node.hasAttribute(ATTR_PACKAGE))
			packageName = node.getAttribute(ATTR_PACKAGE);
		info.className = packageName + "." + node.getAttribute(ATTR_CLASS);
		final NodeList nodes = node.getElementsByTagName(TAG_PARAMETER);
		if (nodes != null && nodes.getLength() > 0) {
			info.params = new HashMap<String, String>();
			for (int i = 0; i < nodes.getLength(); i++) {
				Element param = (Element) nodes.item(i);
				if (param.getParentNode() != node)
					continue;
				if (!param.hasAttribute(ATTR_NAME)) {
					logger.warn("Attribute editors/viewers configuration error: parameter without name");
					continue;
				}
				info.params.put(param.getAttribute(ATTR_NAME), param.getTextContent());
			}
		}
		final NodeList extraJavascriptNodes = node.getElementsByTagName(TAG_EXTRA_JAVASCRIPT);
		info.extraJavascriptInfoList = new ArrayList<ExtraJavascriptInfo>();
		if (extraJavascriptNodes != null && extraJavascriptNodes.getLength() > 0) {
			for (int i = 0; i < extraJavascriptNodes.getLength(); i++) {
				ExtraJavascriptInfo extraJavascriptInfo = new ExtraJavascriptInfo();
				Element extraJavascriptNode = (Element) extraJavascriptNodes.item(i);
				if (extraJavascriptNode.getParentNode() != node)
					continue;
				if (!extraJavascriptNode.hasAttribute(ATTR_CLASS)) {
					logger.warn("Attribute editors/viewers configuration error: no class defined for extraJavascript");
					continue;
				}
				extraJavascriptInfo.setClassName(packageName + "." + extraJavascriptNode.getAttribute(ATTR_CLASS));
				extraJavascriptInfo.setParams(new HashMap<String, String>());
				NodeList parameterNodes = extraJavascriptNode.getElementsByTagName(TAG_PARAMETER);
				if (parameterNodes != null && parameterNodes.getLength() > 0) {
					for (int k = 0; k < parameterNodes.getLength(); k++) {
						Element param = (Element) parameterNodes.item(k);
						if (param.getParentNode() != extraJavascriptNode)
							continue;
						if (!param.hasAttribute(ATTR_NAME)) {
							logger.warn("Attribute editors/viewers configuration error: parameter without name");
							continue;
						}
						extraJavascriptInfo.getParams().put(param.getAttribute(ATTR_NAME), param.getTextContent());
					}
				}
				info.extraJavascriptInfoList.add(extraJavascriptInfo);
			}
		}
		return info;
	}

	private Element getSingleNode(Element parent, String name) {
		NodeList children = parent.getElementsByTagName(name);
		if (children.getLength() == 0)
			return null;
		for (int i = 0; i < children.getLength(); i++)
			if (children.item(i).getParentNode() == parent)
				return (Element) children.item(i);
		return null;
	}
	
	private class EditorInfo {
		String className;
		HashMap<String, String> params;
		List<ExtraJavascriptInfo> extraJavascriptInfoList;
	}

	private class EditorMap {
		EditorInfo defEditor = null;
		EditorInfo defViewer = null;
		EditorInfo defFilterEditor = null;
		String propertyName = null;
		boolean sessionProperty = false;
		SmartMap choices = new SmartMap();

		EditorInfo getEditorClass(Attribute attr, CardPortletSessionBean session) {
			Object bean = sessionProperty ? (Object) session : attr;
			Object value = null;
			try {
				value = PropertyUtils.getProperty(bean, propertyName);
			} catch (Exception e) {
				//No property or inaccessible property - using default editor
			}
			if (value instanceof DataObject)
				value = ((DataObject) value).getId();
			EditorInfo editor = null;
			EditorMap choice = (EditorMap) choices.get(value/*.toString()*/);
			if (choice != null)
				editor = choice.getEditorClass(attr, session);
			return editor == null ? defEditor : editor;
		}

		EditorInfo getViewerClass(Attribute attr, CardPortletSessionBean session) {
			Object bean = sessionProperty ? (Object) session : attr;
			Object value = null;
			try {
				value = PropertyUtils.getProperty(bean, propertyName);
			} catch (Exception e) {
				//No property or inaccessible property - using default viewer
			}

			if (value instanceof DataObject)
				value = ((DataObject) value).getId();
			EditorInfo viewer = null;
			EditorMap choice = (EditorMap) choices.get(value);
			if (choice != null)
				viewer = choice.getViewerClass(attr, session);
			return viewer == null ? defViewer : viewer;
		}
		
		EditorInfo getSearchFilterClass(Attribute attr, SearchFilterPortletSessionBean sessionBean) {
			Object bean = sessionProperty ?  sessionBean : attr;
			Object value = getProperty(bean, propertyName);
			
			if (value instanceof DataObject)
				value = ((DataObject) value).getId();
			else if(value != null && value.getClass().isEnum())//added enum support
				value = value.toString();
			
			EditorInfo searchFilterEditor = null;
			EditorMap choice = (EditorMap) choices.get(value);
			if (choice != null)
				searchFilterEditor = choice.getSearchFilterClass(attr, sessionBean);
			return searchFilterEditor == null ? defFilterEditor : searchFilterEditor ;
		}

		private Object getProperty(Object bean, String property) {
			try {
				return  PropertyUtils.getProperty(bean, property);
			} catch (IllegalAccessException e) {
				logger.info("Impossible to get property " + property + "  for bean " + bean );
			} catch (InvocationTargetException e) {
				logger.info("Impossible to get property " + property + "  for bean " + bean );
			} catch (NoSuchMethodException e) {
				logger.info("Impossible to get property " + property + "  for bean " + bean );
			} catch(IllegalArgumentException e) {
				logger.info("Impossible to get property " + property + "  for bean " + bean );
			}
			return null;
		}		
	}
}
