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
package com.aplana.dbmi.card.graph;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.card.LinkedCardUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class GraphLoader {
	private static Log logger = LogFactory.getLog(GraphLoader.class);
	
	private static final Class cardLinkAttruteClass = CardLinkAttribute.class;
	private static final Class stringAttruteClass = StringAttribute.class;
	
	private static final ObjectId ATTR_SNAME = ObjectId.predefined(StringAttribute.class, "jbr.person.lastName");
	private static final ObjectId ATTR_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.firstName");
	private static final ObjectId ATTR_MNAME = ObjectId.predefined(StringAttribute.class, "jbr.person.middleName");
	
	private DataServiceBean serviceBean;
	private GraphDescriptor descr;
	private Graph graph;
	
	public GraphLoader(GraphDescriptor descr, DataServiceBean serviceBean) {
		this.descr = descr;
		this.serviceBean = serviceBean;
	}
	
	public Graph load(Long cardId) throws DataException, ServiceException {
		graph = new Graph();
		int curDeep = 0;
		int maxDeep = descr.getDepth();
		
		// ����������� ������������� �����
		NodeDescr curDoc;
		LinkedList curLayer; 
		LinkedList nextLayer; 
		
		NodeDescr firstDoc = new NodeDescr();
		firstDoc.nodeId = graph.nextNodeId();
		firstDoc.cardId = cardId;
		firstDoc.nameCardSet = descr.getNameFirstCardSet();
		
		curLayer = new LinkedList();
		curLayer.add(firstDoc);
		Set<Long> visitedCardsCurLayer = new HashSet<Long>();
		Set<Long> visitedCardsLastLayer = new HashSet<Long>();
		
		nextLayer = new LinkedList();
		while (curLayer != null && curLayer.size() > 0) {
			boolean lastLayer = (curDeep >= maxDeep);
			curDoc = (NodeDescr)curLayer.remove(0);
			if(!visitedCardsLastLayer.contains(curDoc.cardId)){
				visitedCardsCurLayer.add(curDoc.cardId);
				
				GraphDescriptor.CardSet cardSet = descr.getCardSet(curDoc.nameCardSet);
				List attrLinks = new LinkedList(cardSet.getLinks().keySet());
				Graph.Node node = getNode(curDoc.cardId, cardSet.getType(), attrLinks, cardSet.getAttrs(), lastLayer, cardSet.getParams());
				graph.addNode(curDoc.nodeId, node);
				
				if (node.getLinks() != null) {
					Iterator iter = node.getLinks().iterator();
					while (iter.hasNext()) {
						Graph.Link link = (Graph.Link)iter.next();
						NodeDescr nd = new NodeDescr();
						nd.nodeId = link.getTargetNodeId();
						nd.cardId = link.getTargetCardId();
						nd.nameCardSet = cardSet.getTarget(link.getIdLink());
						nextLayer.add(nd);
					}
				}
			} 
			if (curLayer.size() == 0) {
				curLayer = nextLayer;
				nextLayer = new LinkedList();
				visitedCardsLastLayer.addAll(visitedCardsCurLayer);
				curDeep++;
			}
		}
		return graph;
	}
	
	// attrLinks - ������ ������ (ObjectId)
	// attrs - id ���������(ObjectId) -> ��� ���������(String) 
	// lastLayer - ���� true, �� �� ���� �������� ����
	// params - ���_��������(String) -> Map(���_���������(String) -> ��������(String))
	private Graph.Node getNode(Long cardId, String typeCardSet, List attrLinks, Map attrs, boolean lastLayer, Map params) throws DataException, ServiceException {
		Graph.Node node = new Graph.Node();
		node.setCardId(cardId);
		node.setType(typeCardSet);
		// ������ ����� �������� � ����������� ������ ���������
		// ����� CardLinkAttribute � BackLinkAttribute ���� �� �������.
		// �������� ��������� ���� CardLinkAttribute ���� � search � ���������� ����������
		// �������� ��������� ���� BackLinkAttribute ��������� ����������� Search
		List attrCardLinks = new LinkedList();
		Iterator iter = attrLinks.iterator();
		while (iter.hasNext()) {
			ObjectId attrLinkId = (ObjectId)iter.next();
			if (CardLinkAttribute.class.isAssignableFrom(attrLinkId.getType())) {
				attrCardLinks.add(attrLinkId);
			} 
		}
		
		Search search = new Search();
		search.setByCode(true);
		search.setWords(cardId.toString());
		
		List columns = new ArrayList(attrCardLinks.size()+attrs.size()+1);
		SearchResult.Column col;
		
		// ���������� ��� ������� ���������
		col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_TEMPLATE);
		columns.add(col);
		
		iter = attrCardLinks.iterator();
		while (iter.hasNext()) {
			col = new SearchResult.Column();
			col.setAttributeId((ObjectId)iter.next());
			columns.add(col);
		}
		
		iter = attrs.keySet().iterator();
		while (iter.hasNext()) {
			col = new SearchResult.Column();
			col.setAttributeId((ObjectId)iter.next());
			columns.add(col);
		}
		search.setColumns(columns);
		
		SearchResult res = (SearchResult)serviceBean.doAction(search);
		// ����� ���������
		Map nameAttrs = new HashMap(); // id �������� (ObjectId) -> ������������� �������� �������� (String)
		iter = res.getColumns().iterator();
		while (iter.hasNext()) {
			SearchResult.Column column = (SearchResult.Column) iter.next();
			nameAttrs.put(column.getAttributeId(), column.getName());
		}
		iter = res.getCards().iterator();
		if (iter.hasNext()) {
			Card c = (Card)iter.next();
			
			String nameTemplate = ((ListAttribute)c.getAttributeById(Card.ATTR_TEMPLATE)).getValue().getValue();
			node.setLabel(nameTemplate);
			
			List links = new LinkedList();
			if (!lastLayer) {
				// ������������� ������ �� ��������� ��������
				for (int i=0; i<attrLinks.size(); i++) {
					ObjectId attrId = (ObjectId)attrLinks.get(i);
					if (CardLinkAttribute.class.isAssignableFrom(attrId.getType())) {
						CardLinkAttribute cardLinkAttr = (CardLinkAttribute)c.getAttributeById((ObjectId)attrLinks.get(i));
						//�.�. 2010/03
						//if (cardLinkAttr != null && cardLinkAttr.getValues() != null && cardLinkAttr.getValues().size() > 0) {
						if (cardLinkAttr != null && cardLinkAttr.getLinkedCount() > 0) {
							//iter = cardLinkAttr.getValues().iterator();
							iter = cardLinkAttr.getIdsLinked().iterator();
							while (iter.hasNext()) {
								//Card targetCard = (Card)iter.next();
								ObjectId targetCardId = (ObjectId) iter.next();
								Graph.Link link = new Graph.Link();
								
								link.setIdLink(cardLinkAttr.getId());
								link.setTargetCardId((Long)targetCardId.getId());
								link.setTargetNodeId(graph.nextNodeId());
								links.add(link);
							}
						}
					} else if (attrId.getType().equals(BackLinkAttribute.class)) {
							ListProject searchBack = new ListProject();
							searchBack.setAttribute(attrId);
							searchBack.setCard(new ObjectId(Card.class, cardId));
							SearchResult resBack = (SearchResult) serviceBean.doAction(searchBack);
							Iterator iterBackCards = resBack.getCards().iterator();
							while (iterBackCards.hasNext()) {
								ObjectId targetCardId = ((Card)iterBackCards.next()).getId();
								Graph.Link link = new Graph.Link();
								
								link.setIdLink(attrId);
								link.setTargetCardId((Long)targetCardId.getId());
								link.setTargetNodeId(graph.nextNodeId());
								links.add(link);
							}
					}
				}
			}
			node.setLinks(links);
			
			Map attrValues = new HashMap();
			Iterator keys = attrs.keySet().iterator();
			while (keys.hasNext()) {
				ObjectId idAttr = (ObjectId)keys.next();
				// ����� ���� null
				Attribute attr = (Attribute)c.getAttributeById(idAttr);
				String value = null;
				if (attr!= null && attr.getStringValue() != null) {
					final Map attrParams = (Map)params.get(attrs.get(idAttr));
					if (attrParams != null) {
						if (DateAttribute.class.equals(idAttr.getType())) {
							if (attrParams.get("pattern") != null) {
								final String pattern = (String)attrParams.get("pattern");
								if (pattern != null && attr != null && 
										( (DateAttribute) attr).getValue()!= null ) {
								
									final SimpleDateFormat format = new SimpleDateFormat(pattern);
									value = format.format(((DateAttribute)attr).getValue());
								}
							}
						} else if (CardLinkAttribute.class.equals(idAttr.getType())) {
							if (attrParams.get("user") != null) {
								try {
									value = getUsersFromCardLink((CardLinkAttribute)attr);
								} catch (Exception e) {
									logger.error("Failed to read user name "+attr.getId().getId());
									value = "";
								}
							}
						}
					} 
					if (value == null) {
						value = attr.getStringValue();
					}
				} else {
					value = "";
				}
				String title = (String)nameAttrs.get(idAttr);
				String name = (String)attrs.get(idAttr);
				attrValues.put(name, new Graph.Field(title, value));
			}
			node.setAttrs(attrValues);
		}
		return node;
	}
	
	private String getUsersFromCardLink(CardLinkAttribute attr) throws DataException, ServiceException {
		Search search = new Search();
		search.setWords(attr.getLinkedIds());
		search.setByCode(true);
		
		Collection columns = new ArrayList(3);
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(ATTR_SNAME);
		columns.add(col);
		col = new SearchResult.Column();
		col.setAttributeId(ATTR_NAME);
		columns.add(col);
		col = new SearchResult.Column();
		col.setAttributeId(ATTR_MNAME);
		columns.add(col);
		search.setColumns(columns);
		
		String result = null;
		Iterator userCards = ((SearchResult)serviceBean.doAction(search)).getCards().iterator();
		while (userCards.hasNext()) {
			Card user = (Card)userCards.next();
			String sname = user.getAttributeById(ATTR_SNAME).getStringValue();
			String name = user.getAttributeById(ATTR_NAME).getStringValue();
			String mname = user.getAttributeById(ATTR_MNAME).getStringValue();
			String fio = getFormatFIO(sname, name, mname);
			if (result == null) {
				result = fio;
			} else {
				result += ", " + fio;
			}
		}
		return result;
	}

	private String getFormatFIO(String sname, String name, String mname) {
		sname = sname != null ? sname : "";
		name = name != null && name.length() > 0 ? name.substring(0, 1)+"." : "";
		mname = mname != null && mname.length() > 0 ? mname.substring(0, 1)+"." : "";
		return sname+" "+name+mname;
	}

	private class Node {
		
	}
	
	private ObjectId quickObjectId(String nameAttr) {
		return new ObjectId(cardLinkAttruteClass, nameAttr);
	}
	
	static class NodeDescr {
		public long nodeId;
		public Long cardId;
		public String nameCardSet;
	}
}
