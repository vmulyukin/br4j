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
package com.aplana.dbmi.card.hierarchy.descriptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.aplana.dbmi.action.xml.SearchXmlHelper;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptor;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptorReader;
import com.aplana.dbmi.card.hierarchy.Messages;
import com.aplana.dbmi.card.hierarchy.util.AttributeHandler;
import com.aplana.dbmi.card.util.AbstractXmlDescriptorReader;
import com.aplana.dbmi.card.util.SearchUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class HierarchyDescriptorReader extends AbstractXmlDescriptorReader {
	private XPathExpression groupExpression = xpath.compile("./group");
	private XPathExpression cardSetExpression = xpath.compile("./cardSet");
	private XPathExpression parentLinkExpression = xpath.compile("./parent");
	private XPathExpression childrenLinksExpression = xpath.compile("./children");
	private XPathExpression infoLinksExpression = xpath.compile("./info");
	private XPathExpression columnsExpression = xpath.compile("./columns");
	private XPathExpression conditionExpression = xpath.compile("./condition");
	private XPathExpression hierarchyExpression = xpath.compile("./hierarchy");

	private MessagesReader messagesReader;
	private ConditionReader conditionReader;
	private ActionsDescriptorReader actionsReader;

	public HierarchyDescriptorReader() throws XPathExpressionException {
		this.messagesReader = new MessagesReader(this.xpath);
		this.conditionReader = new ConditionReader(this.xpath);
		this.actionsReader = new ActionsDescriptorReader(this.xpath, conditionReader);
	}

	public HierarchyDescriptorReader(XPath xpath, MessagesReader messagesReader) throws XPathExpressionException {
		super(xpath);
		this.conditionReader = new ConditionReader(this.xpath);
		this.messagesReader = messagesReader;
		this.actionsReader = new ActionsDescriptorReader(this.xpath, conditionReader);
	}

	private CardItemsMergeMode readParentMergeMode(Element hierarchyElem) {
		String st = hierarchyElem.getAttribute("parentMergeMode");
		if (st != null && !"".equals(st)) {
			return CardItemsMergeMode.fromString(st);
		} else {
			return CardItemsMergeMode.NONE;
		}
	}
	
	private String readHierarchySQL(Element hierarchyElem){
		if(hierarchyElem.hasAttribute("hierarchySQL")){
			return hierarchyElem.getAttribute("hierarchySQL");
		} else {
			return null;
		}
	}

	public HierarchyDescriptor read(InputStream stream, DataServiceBean serviceBean) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, DataException, ServiceException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
		Element root = doc.getDocumentElement();
		Messages messages = messagesReader.read((Element)xpath.evaluate("./messages", root, XPathConstants.NODE));
		return readFromNode(root, messages, serviceBean);
	}

	private String xmlToString(Element elem) {
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(elem);
			transformer.transform(source, result);
			return result.getWriter().toString();
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	public HierarchyDescriptor readFromNode(Element rootElem, Messages messages, DataServiceBean serviceBean) throws XPathExpressionException, DataException, ServiceException {
		HierarchyDescriptor hd = new HierarchyDescriptor(xmlToString(rootElem).hashCode());
		Element hierarchyElem = (Element)hierarchyExpression.evaluate(rootElem, XPathConstants.NODE);
		hd.setParentMergeMode(readParentMergeMode(hierarchyElem));
		hd.setHierarchySQL(readHierarchySQL(hierarchyElem));
		hd.setTerminalNodesOnly(hierarchyElem.hasAttribute("terminalNodesOnly")?hierarchyElem.getAttribute("terminalNodesOnly").equals("true"):false);
		hd.setCardSets(
			readCardSetDescriptors(
				hierarchyElem,
				messages,
				serviceBean
			)
		);

		Element noColumnsElement = (Element)xpath.evaluate("./noColumns", rootElem, XPathConstants.NODE);
		if (noColumnsElement != null) {
			String noColumn = noColumnsElement.getAttribute("value");
			if (noColumn.equals("true")) {
				hd.setNoColumns(true);
			}
		}
		NodeList columnSetNodes = (NodeList) columnsExpression.evaluate(rootElem, XPathConstants.NODESET);
		for (int i = 0; i < columnSetNodes.getLength(); i++) {
			Element columnSetElem = (Element) columnSetNodes.item(i);
			String key = columnSetElem.getAttribute("id");
			if (key == null || "".equals(key))
				key = HierarchyDescriptor.COLUMNS_MAIN;
			List columns = SearchXmlHelper.readColumnsDefinition(columnSetElem, "./column");
			hd.setColumns(key, SearchUtils.getNotReplaceColumns(SearchUtils.initializeColumns(columns, serviceBean)));
		}
		readStyling(hd, rootElem, serviceBean);
		hd.setMessages(messages);
		Element actionsElement = (Element)xpath.evaluate("./actions", rootElem, XPathConstants.NODE);
		if (actionsElement != null) {
			hd.setActionsDescriptor(actionsReader.readFromNode(actionsElement, messages, serviceBean));
		} else {
			hd.setActionsDescriptor(new ActionsDescriptor());
		}
		Element cacheElement = (Element)xpath.evaluate("./cacheable", rootElem, XPathConstants.NODE);
		if (cacheElement != null) {
			hd.setCacheReloadTime(Integer.parseInt(cacheElement.getAttribute("reloadTime")));
		}
		String readOnlyAttributeValue = rootElem.getAttribute("readOnly");
		if (readOnlyAttributeValue != null && !"".equals(readOnlyAttributeValue)) {
			hd.setReadonly(Boolean.parseBoolean(readOnlyAttributeValue));
		}
		return hd;
	}

	public List readCardSetDescriptors(Node hierarchyElem, Messages messages, DataServiceBean serviceBean) throws XPathExpressionException {
		NodeList cardSetNodes = (NodeList)cardSetExpression.evaluate(hierarchyElem, XPathConstants.NODESET);
		List result = new ArrayList(cardSetNodes.getLength());
		for (int i = 0; i < cardSetNodes.getLength(); ++i) {
			Element cardSetElem = (Element)cardSetNodes.item(i);
			CardSetDescriptor cd = new CardSetDescriptor();
			cd.setAlias(cardSetElem.getAttribute("alias"));
			String attr = cardSetElem.getAttribute("stored");
			cd.setStored(attr != null && "true".equals(attr));
			attr = cardSetElem.getAttribute("labelAttr");
			if ("".equals(attr)) {
				logger.debug("No labelAttr attribute found for cardSet with alias '" + cd.getAlias() + "'. Using NAME by default.");
				cd.setLabelAttr(Attribute.ID_NAME);
			} else {
				String type = cardSetElem.getAttribute("labelAttrType");
				if ("".equals(type)) {
					logger.debug("No labelAttrType attribute found for cardSet with alias '" + cd.getAlias() + "'. Using StringAttribute.class by default.");
					cd.setLabelAttr(ObjectIdUtils.getObjectId(StringAttribute.class, attr, false));
				} else {
					cd.setLabelAttr(ObjectIdUtils.getObjectId(AttrUtils.getAttrClass(type), attr, false));
				}
			}
			attr = cardSetElem.getAttribute("showOrg");
			cd.setShowOrg(null != attr && "true".equals(attr));
			
			attr = cardSetElem.getAttribute("checkAll");
			cd.setCheckAll(null != attr && "true".equals(attr));
			
			attr = cardSetElem.getAttribute("secondaryLabelAttr");
			if (!"".equals(attr)) {
				String type = cardSetElem.getAttribute("secondaryLabelAttrType");
				if ("".equals(type)) {
					logger.debug("No secondaryLabelAttrType attribute found for cardSet with alias '" + cd.getAlias() + "'. Using StringAttribute.class by default.");
					cd.setSecondaryLabelAttr(ObjectIdUtils.getObjectId(StringAttribute.class, attr, false));
				} else {
					cd.setSecondaryLabelAttr(ObjectIdUtils.getObjectId(AttrUtils.getAttrClass(type), attr, false));
				}
			}

			attr = cardSetElem.getAttribute("linkedLabelAttr");
			if (!"".equals(attr)) {
				String type = cardSetElem.getAttribute("linkedLabelAttrType");
				if ("".equals(type)) {
					logger.debug("No linkedLabelAttrType attribute found for cardSet with alias '" + cd.getAlias() + "'. Using StringAttribute.class by default.");
					cd.setLinkedLabelAttr(ObjectIdUtils.getObjectId(StringAttribute.class, attr, false));
				} else {
					cd.setLinkedLabelAttr(ObjectIdUtils.getObjectId(AttrUtils.getAttrClass(type), attr, false));
				}
			}

			attr = cardSetElem.getAttribute("labelFormat");
			cd.setLabelFormat(messages.getMessage(attr));
			attr = cardSetElem.getAttribute("labelAsLink");
			cd.setLabelAsLink("true".equals(attr));
			cd.setLabelAsDownloadLink("download".equals(attr));
			attr = cardSetElem.getAttribute("maxLength");
			if (attr != null && (!"".equals(attr)))
				cd.setLabelMaxLength(Integer.valueOf(attr));

			attr = cardSetElem.getAttribute("collapsed");
			cd.setCollapsed(attr == null || "true".equals(attr));
			cd.setGrouping(readGroupingDescriptors(cardSetElem));

			List links = readLinks(
				(Node)childrenLinksExpression.evaluate(cardSetElem, XPathConstants.NODE)
			);
			cd.setChildrenLinks(links);
			links = readLinks(
				(Node)infoLinksExpression.evaluate(cardSetElem, XPathConstants.NODE)
			);
			cd.setInfoLinks(links);
			links = readLinks(
				(Node)parentLinkExpression.evaluate(cardSetElem, XPathConstants.NODE)
			);
			if (!links.isEmpty()) {
				cd.setParentLinks(links);
				/*if (links.size() > 1) {
					logger.warn("Too many parent links, skipping all except of first");
				}*/
			}
			Element conditionElem = (Element)conditionExpression.evaluate(cardSetElem, XPathConstants.NODE);
			if (conditionElem != null) {
				cd.setCondition(conditionReader.readCondition(conditionElem, serviceBean));
			}

			attr = cardSetElem.getAttribute("sortAttr");
			if (!"".equals(attr)) {
				String type = cardSetElem.getAttribute("sortAttrType");
				ObjectId attrId = ObjectIdUtils.getObjectId(AttributeHandler.getXmlType(type).getType(), attr, false);
				cd.setSortAttr(attrId);
				attr = cardSetElem.getAttribute("sortOrderByParentAttr");
				if (!"".equals(attr)) {
					cd.setSortOrderByParentAttr(IdUtils.stringToAttrIds(AttributeHandler.getXmlType(type).getType(), attr));
					attr = cardSetElem.getAttribute("parentAttrLink");
					if (!"".equals(attr)) {
						final List<Class<? extends Attribute>> listAttrClasses 
						= new ArrayList<Class<? extends Attribute>>(Arrays.asList(CardLinkAttribute.class, BackLinkAttribute.class));
						cd.setParentAttrLink(ObjectIdUtils.getObjectId(
								listAttrClasses, CardLinkAttribute.class, attr, false));
						attr = cardSetElem.getAttribute("parentAttrLinkReversed");
						cd.setParentAttrLinkReversed(null != attr && "true".equals(attr));
					}
				}
			}

			attr = cardSetElem.getAttribute("sortOrder");
			if ("".equals(attr)) {
				cd.setSortOrder(cd.getSortAttr() == null ? SortOrder.AUTO : SortOrder.ASCENDING);
			} else {
				cd.setSortOrder(SortOrder.fromString(attr));
			}
			result.add(cd);

			attr = cardSetElem.getAttribute("columns");
			if (attr == null || "".equals(attr))
				cd.setColumnsKey(HierarchyDescriptor.COLUMNS_MAIN);
			else
				cd.setColumnsKey(attr);

			attr = cardSetElem.getAttribute("group");
			if (!"".equals(attr)) {
				cd.setGroup(attr);
			}

			attr = cardSetElem.getAttribute("checkChildren");
			cd.setCheckChildren(attr != null && "true".equalsIgnoreCase(attr));

		}
		return result;
	}

	private List readGroupingDescriptors(Node cardSetElem) throws XPathExpressionException {
		NodeList groupingNodes = (NodeList)groupExpression.evaluate(cardSetElem, XPathConstants.NODESET);
		List result = new ArrayList(groupingNodes.getLength());
		for (int i = 0; i < groupingNodes.getLength(); ++i) {
			Element groupElem = (Element)groupingNodes.item(i);
			GroupingDescriptor gd = new GroupingDescriptor();
			String attr = groupElem.getAttribute("attr");
			Class c = AttrUtils.getAttrClass(groupElem.getAttribute("type"));
			gd.setAttr(ObjectIdUtils.getObjectId(c, attr, false));
			attr = groupElem.getAttribute("collapsed");
			gd.setCollapsed(attr == null || "true".equals(attr));
			attr = groupElem.getAttribute("defaultItem");
			gd.setDefaultItemKey(attr);
			attr = groupElem.getAttribute("sortOrder");
			if ("".equals(attr)) {
				gd.setSortOrder(SortOrder.AUTO);
			} else {
				gd.setSortOrder(SortOrder.fromString(attr));
			}
			result.add(gd);
		}
		return result;
	}

	private List readLinks(Node parentNode) throws XPathExpressionException {
		if (parentNode == null) {
			return new ArrayList(0);
		}
		NodeList linkNodes = (NodeList)xpath.evaluate("./link", parentNode, XPathConstants.NODESET);
		List result = new ArrayList(linkNodes.getLength());
		for (int i = 0; i < linkNodes.getLength(); ++i) {
			Element linkElem = (Element)linkNodes.item(i);
			LinkDescriptor ld = new LinkDescriptor();
			String attr = linkElem.getAttribute("linkAttr");
			ld.setCardLinkAttr(ObjectIdUtils.getObjectId(
					Arrays.asList(PersonAttribute.class, CardLinkAttribute.class, BackLinkAttribute.class,  TypedCardLinkAttribute.class), CardLinkAttribute.class, attr, false)
			);
			attr = linkElem.getAttribute("isReversed");
			ld.setReverse(attr != null && "true".equals(attr));
			attr = linkElem.getAttribute("skipNextIfFound");
			ld.setSkipNextIfFound(attr != null && "true".equals(attr));
			attr = linkElem.getAttribute("targetSet");
			ld.setTargetSetAlias(attr);
			attr = linkElem.getAttribute("template");
			ld.setTemplates(IdUtils.stringToAttrIds(Template.class, attr));
			attr = linkElem.getAttribute("status");
			ld.setStatuses(IdUtils.stringToAttrIds(CardState.class, attr));
			result.add(ld);
		}
		return result;
	}

	private void readStyling(HierarchyDescriptor hd, Element rootElem, DataServiceBean serviceBean) throws XPathExpressionException {
		Element styling = (Element)xpath.evaluate("./styling", rootElem, XPathConstants.NODE);
		if (styling == null) {
			hd.setStyles(new ArrayList(0));
			return;
		}
		NodeList styles = (NodeList)xpath.evaluate("./style", styling, XPathConstants.NODESET);
		List result = new ArrayList(styles.getLength());
		for (int i = 0; i < styles.getLength(); ++i) {
			Element style = (Element)styles.item(i);
			StylingDescriptor d = new StylingDescriptor();
			String attr = style.getAttribute("value");
			d.setStyle("".equals(attr) ? null : attr);
			attr = style.getAttribute("icon");
			d.setIconPath("".equals(attr) ? null : attr);
			d.setCondition(conditionReader.readCondition(style, serviceBean));
			result.add(d);
		}
		hd.setStyles(result);
	}
}
