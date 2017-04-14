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
/**
 * 
 */
package com.aplana.dbmi.archive;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;

/**
 * ������ � ������� ����������������� ����� ������
 * @author ppolushkin
 * @since 19.12.2014
 */
public class ArchiveConfigReader {
	
	public static final String CONFIG_FOLDER = "dbmi/archive";
	public static final String CONFIG_FILE = CONFIG_FOLDER + "/archiveAttributes.xml";
	public static final String TAG_ATTR = "attribute";
	public static final String TAG_TEMPLATE = "template";
	public static final String TAG_CHILD = "child";
	public static final String ATTR_ID = "id";
	public static final String ATTR_TEMPLATE = "template";
	
	private final static Log logger = LogFactory.getLog(ArchiveConfigReader.class);
	
	private static class ArchiveConfigSetHolder {
		public static Set<ArchiveConfig> archiveConfigSet = createArchiveConfigSet();
	}
	
	private static Document initFromXml() {
		Document doc = null;
		InputStream is = null;
		try {
			is = Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE);
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);			
		} catch (Exception e) {
			logger.error("Error initializing attributes configuration", e);
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("Error closing configuration file InputStream " + CONFIG_FILE, e);
				}
			}
		}
		return doc;
	}
	
	private static Set<ObjectId> collectAttributes(Element aNode) {
		try {
			NodeList childs = aNode.getChildNodes();
			if(childs == null || childs.getLength() == 0) {
				return null;
			}
			Set<ObjectId> setAttrs = null;
			ObjectId attrId;
			String attrStr;
			Element child;
			for (int a = 0; a < childs.getLength(); a++) {
				if(childs.item(a).getNodeName().equals("#text") ||
						childs.item(a).getNodeName().equals("#comment")) {
					continue;
				}
				child = (Element) childs.item(a);
				if(child.getTagName().equalsIgnoreCase(TAG_ATTR)) {
					attrStr = child.getAttribute(ATTR_ID);
					attrId = IdUtils.smartMakeAttrId(attrStr.trim(), StringAttribute.class);
					if(attrId != null && setAttrs == null) {
						setAttrs = new HashSet<ObjectId>();
						setAttrs.add(attrId);
					} else if(attrId != null) {
						setAttrs.add(attrId);
					} else {
						logger.error("Error while parsing attribute " + attrStr + " for template " + aNode.getAttribute(ATTR_ID));
					}
				}
			}
			return setAttrs;
		} catch (Exception e) {
			logger.error("Error while collecting attributes for archive", e);
			return null;
		}
	}
	
	private static Map<Long, Set<ObjectId>> collectChildren(Element aNode) {
		try {
			NodeList childs = aNode.getChildNodes();
			if(childs == null || childs.getLength() == 0) {
				return null;
			}
			Map<Long, Set<ObjectId>> children = null;
			Set<ObjectId> attrs;
			String template;
			ObjectId templateId;
			Element child;
			for (int a = 0; a < childs.getLength(); a++) {
				if(childs.item(a).getNodeName().equals("#text") ||
						childs.item(a).getNodeName().equals("#comment")) {
					continue;
				}
				child = (Element)childs.item(a);
				if(child.getTagName().equalsIgnoreCase(TAG_CHILD)) {
					template = child.getAttribute(ATTR_TEMPLATE);
					templateId = IdUtils.smartMakeAttrId(template.trim(), Template.class);
					
					attrs = collectAttributes(child);
					
					if(children == null) {
						children = new HashMap<Long, Set<ObjectId>>();
					}
					children.put((Long) templateId.getId(), attrs);
				}
			}
			return children;
		} catch (Exception e) {
			logger.error("Error while collecting attributes for archive", e);
			return null;
		}
	}
	
	private static Set<ArchiveConfig> createArchiveConfigSet() {
		Set<ArchiveConfig> createdSet = null;
		Document document = initFromXml();
		if(document == null)
			return null;
		
		try {
			createdSet = new HashSet<ArchiveConfig>();
			Element root = document.getDocumentElement();
			NodeList allTmplList = root.getElementsByTagName(TAG_TEMPLATE);
			Element tmplNode;
			String template;
			ObjectId templateId;
			Set<ObjectId> attrs;
			Map<Long, Set<ObjectId>> children;
			ArchiveConfig ac;
			for (int a = 0; a < allTmplList.getLength(); a++){
				ac = new ArchiveConfig();
				
				tmplNode = (Element) allTmplList.item(a);
				template = tmplNode.getAttribute(ATTR_ID);
				templateId = IdUtils.smartMakeAttrId(template.trim(), Template.class);
				
				if(templateId == null) {
					continue;
				}
					
				ac.setTemplate((Long) templateId.getId());
				
				attrs = collectAttributes(tmplNode);
				ac.setAttributes(attrs);
				
				children = collectChildren(tmplNode);
				ac.setChildren(children);
				
				createdSet.add(ac);
			}
		} catch(Exception e) {
			logger.error("Error parsing document " + CONFIG_FILE, e);
		}
		
		return createdSet;
	}
	
	public static Set<ArchiveConfig> getArchiveConfigSet() {
		
		return ArchiveConfigSetHolder.archiveConfigSet;
	}

}
