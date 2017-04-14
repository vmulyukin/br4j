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
package com.aplana.dbmi.jbr.processors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.model.Tree;
import com.aplana.dbmi.jbr.processors.docgraph.GraphProcessorBase;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.UserData;
/**
 * The forest consists of nodes and leaves. The forest has at least one to many roots.
 * The built forest does not include the origin document.<br>
 * 
 * If either {@code forestOriginBackLinkAttrId} or {@code forestOriginLinkAttrId} are not defined, 
 * it means the origin document is not considered and the processor will build related to 
 * the current node tree only.<br>
 * 
 * Parameters of TreeProcessor:<br>
 * {@code forestOriginBackLinkAttrId}: {@link ObjectId} that retains a link of type {@link BackLinkAttribute}
 * to the origin document. From that document the whole forest can be built.<br>
 * {@code forestOriginLinkAttrId}: {@link ObjectId} that retains a link of type {@link CardLinkAttribute}
 * to all roots of the forest. That link is holded by the origin document.<br>
 * {@code nodeLinkAttrIds}: a set of {@link ObjectId} that contains links between nodes in the forest.<br>
 * {@code leafLinkAttrIds}: a set of {@link ObjectId} that contains links from a node to its leaves.<br>
 * All of the rest parameters passed to the processor will be transefered to sub-processor.
 * <br>
 * (04.10.2010, GoRik) ������� ��� ������ � ����������� ���������������
 *  @param processorClassNames - ������ ���� �������������� � �� ������� ���� "<%��� �������������1%>|<%�����1%>,...,<%��� �������������N%>|<%�����N%>"
 */
public class TreeProcessor extends ProcessCard {

	private static final long serialVersionUID = 1L;

	private final Map<String, String> parameters = new HashMap<String, String>();
	// cardlink, �� �������� �������������� ������� �� ����� � �����
	private ObjectId forestOriginBackLinkAttrId = null;
	// cardlink-��������, �� �������� �������������� ������� �� ����� � ����� (��� ������ ������ ������ ������)
	private final Set<ObjectId> forestOriginLinkAttrIds = new HashSet<ObjectId>();
	private final Set<ObjectId> nodeLinkAttrIds = new HashSet<ObjectId>();	// cardlink-��������  
	private final Set<ObjectId> leafLinkAttrIds = new HashSet<ObjectId>();	// cardlink-��������
	// ������������, �������� ���������� ��������� ��������� ������ (�������� ������-������ ������ ������ � �������������) - �������� ������������� � ���������� ����������
	private String processorClassName;
	private String processorClassNames;	// ������ ��������������, ������� ������ ����� �������� TreeProcessor 
	private boolean curCardIsDefaultRoot = false;	// ������� �������� - ������ �� ���������, ���� �� ���������� forestOriginBackLinkAttrId � forestOriginLinkAttrIds ����� �� ����� �������  
	private Tree tree = new Tree();	// ������ ������ (�� ����� �� ��������� �����)
	private boolean processForest = true;
	private ObjectId originCardId = null; 	// �������� - ������ ������ 
//	private boolean linkReverse = false;	// ����������� ����������� ������ (true - �����, false - ����)
//	private int depth = 0;					// ������� ������ ������ ()

	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException {
		long startTime = System.currentTimeMillis();
		logger.info( "Start processing processor '"+this.getClass().getName()+"' with subprocessors '"+
				( (processorClassName != null) && (processorClassName.length()>0) ? processorClassName + "," : "")
				+ processorClassNames+ "'");
		final ObjectId cardId = getCardId();
		if (cardId == null) {
			logger.warn("CardId is null -> processor exiting"); // ������� �������� �����
			return null;
		}
		final UserData user = getSystemUser(); // ������� ������������, ������������� �����

		if (nodeLinkAttrIds == null || nodeLinkAttrIds.isEmpty()){ // ��������� - ����� ����� �������
			logger.error("(!?) nodeLinkAttrIds is not set -> exiting");	// ������������ ��������
			return null;
		}
		if (leafLinkAttrIds != null && !leafLinkAttrIds.isEmpty()) //  � ����� - ��� ����� �� �� �������
			// ���� �������� ����� ����� ids, �������� ������
			if (!Collections.disjoint(nodeLinkAttrIds, leafLinkAttrIds)){
				logger.error("(!?) nodeLinkAttrIds and leafLinkAttrIds have shared elements -> exiting");
				return null;
			}

		// forestOriginBackLinkAttrId - ������ ������ ���� ������� ��� ������� ��������
		// ���� forestOriginBackLinkAttrId �/��� forestOriginLinkAttrId �� ����������,
		// ��� ��������, ��� ������������� ��������� (������������ �����) �� ���������������, �
		// ��������� ����� ������� �������� ��������� ��������� ������ � ������� ����� ������
//		Collection<ObjectId> roots = getRootsFromOrigin(forestOriginBackLinkAttrId, forestOriginLinkAttrIds);
		Collection<Pair> roots = getPairRootsFromOrigin(forestOriginBackLinkAttrId, forestOriginLinkAttrIds);
		if (roots.isEmpty()){
			if (curCardIsDefaultRoot&&(forestOriginLinkAttrIds==null||forestOriginLinkAttrIds.isEmpty())){// ����� �� �������, �� ������ ������� ������� ��������
				logger.warn("Can't determine the origin card. I'll get current Card by root.");
				roots.add(new Pair(null, cardId, null));
				processForest = false; // �� ������� ���� ��� ����
			} else {	
				logger.warn("Can't determine the origin card. I'll build related tree only.");
				// ������� ������� (nodeLinkAttrIds) ������ � ������� ����� ����� (cardId)
	//			roots = getRelatedRoots(cardId, nodeLinkAttrIds);
				roots = getRelatedPairRoots(cardId, nodeLinkAttrIds);
				processForest = false; // �� ������� ���� ��� ����
			}
		}
		// ������ � roots ����� ids ��������� � ������� ������ ��������� (��� ������������ �� backlink, ��� � ��������) 

		// ��������� ������ ������ �� ����� ������� �����
//		buildRelatedTree(roots);
		// ������ ������ ������ � ������ ����� �������� �������� �� ������ ������ �� �������� � ���� id, �� � ��� ��������, �� �������� �� ������ � ���������
		buildRelatedTreeForPairRoots(roots);
		if (tree.size() == 0){
			logger.error("Tree is suspiciously empty ! Exit.");
			return null;
		}

		List<SubProcessor> subProcessors = new ArrayList<SubProcessor>();
		if (processorClassName!=null&&!processorClassName.equalsIgnoreCase(""))	// �������� ������������� (processorClassName ���� ��������� � ������ ����������� ��������������)
			subProcessors.add(new SubProcessor(processorClassName, ""));
		if (processorClassNames!=null&&!processorClassNames.equalsIgnoreCase(""))
			subProcessors.addAll(BuildSubProcessorArrayList(processorClassNames));	// ���������� ������ �������������� (������ ������� ������)
		for(Iterator<SubProcessor> spi = subProcessors.iterator(); spi.hasNext(); ){
		// ��� ����������� ������������, �������������� �������� ���������� ��� ������ � ����������:
			// ��������� ���������� ������
			List<ObjectId> linkAttrIds = null;		// ������ �� ����� ������� ������ (� �������������� ������ ������ ���� ����� ������ ����� ������ � ���� ���������) 
			List<ObjectId> linkChildAttrIds = null;	// �������� � ���������, �� ������� ���� ������ ����� � ������ ������
			List<ObjectId> linkLeafAttrIds = null;	// �������� � ���������, �� ������� ���� ������ ������ � ������ ������
//			boolean subCurCardIsDefaultRoot = false;// ������� �������� - ������� ������ �� ��������� ��� ���������, ���� �� ���������� forestOriginBackLinkAttrId � forestOriginLinkAttrIds ����� �� ����� �������  
			SubProcessor sp = spi.next(); 
			GraphProcessorBase processor;
			try {
				final Class<?> prClass = Class.forName(sp.name);
				processor = (GraphProcessorBase)prClass.newInstance();
				final Set<Class<?>> ints = new HashSet<Class<?>>(Arrays.asList(prClass.getInterfaces()));
				Class<?> supa = prClass.getSuperclass();
				while (supa != null){
					ints.addAll(Arrays.asList(supa.getInterfaces()));
					supa = supa.getSuperclass();
				}	
				
				if (ints.contains(DatabaseClient.class)){
					((DatabaseClient)processor).setJdbcTemplate(getJdbcTemplate());
				}
				processor.init(getCurrentQuery());
				processor.setBeanFactory(getBeanFactory());
				processor.setUser(user);
				processor.setAction(getAction());
				processor.setCurExecPhase(getCurExecPhase());
				if (ints.contains(Parametrized.class)){
					for (Iterator<String> i = parameters.keySet().iterator(); i.hasNext(); ){
						String name = i.next();
						if (name.startsWith(sp.alias+".")){	// ���� ��������� �������� �������� ����� �������� �������������, �� ����� ���� ��������
							// ���� ��� ��������������� ������ ��������� linkAttrId, linkChildAttrId, linkLeafAttrId, �� �������� �� � ��������� ���������� TreeProcessor-� 
							if ("linkAttrIds".equalsIgnoreCase(name.substring((sp.alias+".").length()))){
								linkAttrIds = stringToAttrIds(CardLinkAttribute.class, parameters.get(name));
							} else if ("linkChildAttrIds".equalsIgnoreCase(name.substring((sp.alias+".").length()))){
								linkChildAttrIds = stringToAttrIds(CardLinkAttribute.class, parameters.get(name));
							} else if ("linkLeafAttrIds".equalsIgnoreCase(name.substring((sp.alias+".").length()))){
								linkLeafAttrIds = stringToAttrIds(CardLinkAttribute.class, parameters.get(name));
/*							} else if ("�urCardIsDefaultRoot".equalsIgnoreCase(name.substring((sp.alias+".").length()))){
								subCurCardIsDefaultRoot = Boolean.parseBoolean(parameters.get(name));*/
							} else {
								((Parametrized)processor).setParameter(name.substring((sp.alias+".").length()), parameters.get(name));
							}
						}
						if (sp.alias.equalsIgnoreCase(""))	// ���� ��������� ������� ������������ ��� ������, �� ������ ����� ���� �������� (��������� linkAttrId, linkChildAttrId � linkLeafAttrId ��� ������������� ��� ������ �� ������������, �.�. �� ���������� �� ������ �������������� TreeProseccor-�� ������)
							((Parametrized)processor).setParameter(name, parameters.get(name));
					}
				}
			} catch (Exception e) {
				logger.error("Class "+sp.name+" not found !");
				throw new DataException(e);
			}
		
//		for (ObjectId Id : tree.getAllNodeIds()) {
		//TODO ��������� �������� ������ ������ ����� ���� ��� ���������� ������������� ����
		//TODO ������ ���������� ������������ ������ ������� ������� ���� � ������ (��� ����)
			logger.info("Calling proc "+sp.name+" for card "+cardId.getId());
			try {
				processor.setOriginNodeId(this.originCardId);
				processor.setCurNodeId(cardId);
				Tree subTree = tree;	// ��� ������ �������������� ����� ����������� ��� ���������� ��������� ������ (�� ��������� - �������������� ������) 
				if ((linkAttrIds!=null&&!linkAttrIds.isEmpty())||(linkChildAttrIds!=null&&!linkChildAttrIds.isEmpty())||(linkLeafAttrIds!=null&&!linkLeafAttrIds.isEmpty())){	// ���� ���� �� ���� �������� �� ���� linkAttrId, linkChildAttrId, linkLeafAttrId �����, �� ��������� ���������
//					subTree.clear();
					// ���������� ��������� 
					subTree = tree.getSubTree(linkAttrIds, linkChildAttrIds, linkLeafAttrIds);
				}
				processor.setTree(subTree);
				if (processForest){
/*					logger.warn("Process all nodes are: "+ ObjectIdUtils.numericIdsToCommaDelimitedString(tree.getAllNodeIds()));
					logger.warn("Copy to nodes are: "+ ObjectIdUtils.numericIdsToCommaDelimitedString(tree.getAllLeafIds()));
*/					processor.setAllChildNodes(new HashSet<ObjectId>(subTree.getAllNodeIds()));	// ��� �������� ������, ����� ������� ��������� � AllChildNodes 	
					processor.setCopyToNodes(new HashSet<ObjectId>(subTree.getAllLeafIds()));		// ��� ������ ������ ������ ��������� � copyToNodeIds
				} else {
/*					logger.warn("Child nodes are: "+ ObjectIdUtils.numericIdsToCommaDelimitedString(tree.getAllChildrenNodes(cardId)));
					logger.warn("Parent nodes are: "+ ObjectIdUtils.numericIdsToCommaDelimitedString(tree.getPathTo(cardId)));
					logger.warn("Copy to nodes are: "+ ObjectIdUtils.numericIdsToCommaDelimitedString(tree.getChildrenLeaves(cardId)));
					// ��� ���� ��� ������� �������� ������������ � AllChildNodes
*/					Set<ObjectId> allChildNodes = new HashSet<ObjectId>();
					if (curCardIsDefaultRoot)	// � ������ ����� ������� �������� ��� ������������� ��������� � ���� ��������
						allChildNodes.add(cardId);
					allChildNodes.addAll(subTree.getAllChildrenNodes(cardId));
					processor.setAllChildNodes(allChildNodes);
					// ���� �� ������ ������� �������� � ������� �������� ������������ � AllParentNodes
					Set<ObjectId> allParentNodes = new HashSet<ObjectId>();
					if (curCardIsDefaultRoot)	// � ������ ��������� ������� �������� ��� ������������� ��������� � ���� ��������
						allParentNodes.add(cardId);
					allParentNodes.addAll(subTree.getPathTo(cardId));
					processor.setAllParentNodes(allParentNodes);
					// ��� ������ ��� ������� �������� ��������� � CopyToNodes
					processor.setCopyToNodes(new HashSet<ObjectId>(subTree.getChildrenLeaves(cardId)));
				}
			} catch (Exception e) {
				logger.error("There is an error when setting "+sp.name+" processor !");
				throw new DataException(e);
			}
			try {
				// �������� ����� ������ ������������� � debug-������
				long subStartTime = System.currentTimeMillis();
				logger.debug( "Start processing processor '"+processor.getClass().getName()+"': time = "+ startTime);
				processor.processNode();	// ��������� ������������
				logger.debug( "Finish processing processor '"+processor.getClass().getName()+"': workingtime = "+ (System.currentTimeMillis()-subStartTime));
			} catch (DataException e) {
				logger.error("There is an error when running "+sp.name+" processor !", e);
				throw e;
			} catch (Exception e) {
				logger.error("There is an error when running "+sp.name+" processor !", e);
				throw new DataException(e);
			}
		}
		logger.info( "Finish processing processor '"+this.getClass().getName()+"' with subprocessors '"+processorClassNames+"': workingtime = "+ (System.currentTimeMillis()-startTime));
		return null;
	}
	
	/**
	 * Builds the tree which is related to the current card. 
	 * @param cardId ID of the card that retains in the tree
	 * @return current node
	 */
	void buildRelatedTree(Collection<ObjectId> roots){
		tree.setRoots(roots); // ������������� ����� ������

		final Collection<ObjectId> allAttrIds = new HashSet<ObjectId>(nodeLinkAttrIds);
		allAttrIds.addAll(leafLinkAttrIds);	// ������ � ������ ���������-������ � nodeLinkAttrIds � leafLinkAttrIds 

		Collection<Pair> parents = new ArrayList<Pair>();
		for (ObjectId root : roots) 
			parents.add(new Pair(null, root, null));	// ��������� ������ ��������� �� ������ �������� ���������

		while (!parents.isEmpty()){		// ����� �� ���� �������� (�� ������� ��������� �� ��������� �����)
			Collection<Pair> kids = loadChildren(parents, allAttrIds);
			for (Pair pair : kids) 
				// ��������� ������ ������ ��������� �������� � ��������� (���� ���������) ��� ��� ����, � �� ����, � ������� ��������, �� �������� ���� ������������ ������ �� ��������
				tree.addChild(pair.parentId, pair.cardId, leafLinkAttrIds.contains(pair.attr), pair.attr);
			parents = kids;
		}
	}
	
	/**
	 * ��������� ������ ������ ��� ������� ������, �������� id-������ � ����������-�������� � ���������� 
	 * @param roots
	 */
	void buildRelatedTreeForPairRoots(Collection<Pair> roots){
		for (Pair root : roots) 
			tree.addRoot(root.cardId, root.attr);	// ��������� ������ ��������� �� ������ �������� ���������

		final Collection<ObjectId> allAttrIds = new HashSet<ObjectId>(nodeLinkAttrIds);
		allAttrIds.addAll(leafLinkAttrIds);	// ������ � ������ ���������-������ � nodeLinkAttrIds � leafLinkAttrIds 

		Collection<Pair> parents = new ArrayList<Pair>(roots);

		while (!parents.isEmpty()){		// ����� �� ���� �������� (�� ������� ��������� �� ��������� �����)
			Collection<Pair> kids = loadChildren(parents, allAttrIds);
			for (Pair pair : kids) 
				// ��������� ������ ������ ��������� �������� � ��������� (���� ���������) ��� ��� ����, � �� ����, � ������� ��������, �� �������� ���� ������������ ������ �� ��������
				tree.addChild(pair.parentId, pair.cardId, leafLinkAttrIds.contains(pair.attr), pair.attr);
			parents = kids;
		}
	}
	/** ����������� ������ ����� ��� �������� ������ ��������-���������, ��������� � ���������� ������� ������� ���������**/
	@SuppressWarnings("unchecked")
	Collection<Pair> loadChildren(Collection<Pair> parents, Collection<ObjectId> attrIds){
		final ArrayList<ObjectId> parentIds = new ArrayList<ObjectId>();
		for (Pair parent : parents) 
			parentIds.add(parent.cardId);

		final String sql = MessageFormat.format(
					"SELECT DISTINCT parent.card_id, parent.number_value, parent.attribute_code \n"+
					/* (!) (2011/07/25, RuSA) ������������� "card as child" ������ 
					 * "attribute_value as child" �������� ������ �� 1 ��� � 600 ���.
					 */
					"FROM attribute_value parent JOIN card child " +
					"		ON child.card_id = parent.number_value \n"+
					"		AND parent.attribute_code IN ({0}) \n"+
					"		AND parent.card_id IN ({1})",
					IdUtils.makeIdCodesQuotedEnum(attrIds), 
					ObjectIdUtils.numericIdsToCommaDelimitedString(parentIds)
			);

		return getJdbcTemplate().query(sql,
				new RowMapper(){
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Pair pair = new Pair(new ObjectId(Card.class, rs.getLong(1)), 
								new ObjectId(Card.class, rs.getLong(2)), 
								new ObjectId(CardLinkAttribute.class, rs.getString(3)));
						return pair;
					}
		});
	}

	/**
	 * @comment ������������ �������� ������� �������� �� ������ linkAttrIds 
	 **/
	/* (2011/07/25, RuSA) ������������ �����-�� ����� - attribute_code ����������� 
	 * � � ���������� � � �����. �� � �� ������������ �����.
	@SuppressWarnings("unchecked")
	Collection<Pair> loadParents(Collection<Pair> children, Collection<ObjectId> linkAttrIds){
		ArrayList<ObjectId> childrenIds = new ArrayList<ObjectId>();
		for (Pair child : children) 
			childrenIds.add(child.cardId);

		String sql = MessageFormat.format(
					"SELECT parent.card_id, child.card_id, parent.attribute_code \n"+
					"FROM attribute_value child \n"+
					"	LEFT JOIN attribute_value parent \n" +
					"		ON child.card_id=parent.number_value \n"+
					"		AND parent.attribute_code IN ({0}) \n"+
					"WHERE \n" +
					"	child.number_value IN ({1}) \n" +
					"	AND child.attribute_code IN ({0}) ",
					IdUtils.makeIdCodesQuotedEnum(linkAttrIds), 
					ObjectIdUtils.numericIdsToCommaDelimitedString(childrenIds)
				);
		
		return getJdbcTemplate().query(sql, 
				new RowMapper(){
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Pair pair = new Pair(new ObjectId(Card.class, rs.getLong(1)), 
								new ObjectId(Card.class, rs.getLong(2)), 
								new ObjectId(CardLinkAttribute.class, rs.getString(3)));
						return pair;
					}
		});
	}
	 */

	/**
	 * 
	 * @param forestOriginBkLnkAttrId
	 * @param forestOriginLnkAttrIds
	 * @return
	 */
	@SuppressWarnings("unchecked")
	Set<Pair> getPairRootsFromOrigin(
			ObjectId forestOriginBkLnkAttrId, Set<ObjectId> forestOriginLnkAttrIds)
	{
		final Set<Pair> childrenIds = new HashSet<Pair>();
		// ������� ��������� �� ������ ���� �������
		if (forestOriginBkLnkAttrId == null 
				|| forestOriginLnkAttrIds == null
				|| forestOriginLnkAttrIds.isEmpty() )
			return childrenIds;

		try {
			Collection<Card> linkedCards = null;

			if ("@SELF".equalsIgnoreCase(forestOriginBackLinkAttrId.getId().toString()) ) {
				// ����� ������� �������� ��� ������ ������
				linkedCards = Collections.singletonList(super.getCard());	// ���� forestOriginBackLinkAttrId ����� ��� @Self, �� ������ �������
			} else 
			{	
				LinkAttribute la = (LinkAttribute) getCard().getAttributeById(forestOriginBackLinkAttrId);
				if(la instanceof BackLinkAttribute) {
					// ����������� �� �����...
					final ListProject action = new ListProject();
					action.setAttribute(forestOriginBkLnkAttrId);
					action.setCard(getCardId());
					final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();

					for(ObjectId attrId : forestOriginLnkAttrIds) {
						columns.add(CardUtils.createColumn(attrId));
					}

					action.setColumns(columns);

					linkedCards = CardUtils.execSearchCards(action, getQueryFactory(), getDatabase(), getSystemUser());	// ����� �������� ����� ������� ������������ ��������
				} else if(la instanceof CardLinkAttribute) {
					final Search action = new Search();
					action.setByCode(true);
					action.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(la.getIdsLinked()));
					final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();

					for(ObjectId attrId : forestOriginLnkAttrIds) {
						columns.add( CardUtils.createColumn(attrId));
					}

					action.setColumns(columns);
					
					linkedCards = CardUtils.execSearchCards(action, getQueryFactory(), getDatabase(), getSystemUser());			
				} else {
					throw new IllegalArgumentException("Invalid type of forestOriginBackLinkAttrId param");
				}
			}

			if (linkedCards == null || linkedCards.isEmpty()) {
				logger.warn("Can't get the origin card for card "+getCardId().getId()+	// ��� ��������� ��� ������� ��������
						" via "+forestOriginBkLnkAttrId);
				return childrenIds;
			}
			if (linkedCards.size() > 1){
				logger.warn("There are more than one origin card for card "+getCardId().getId()+	// ���������� > 1
						" via "+forestOriginBkLnkAttrId);
				//return childrenIds;
			}
			// linkedCards.iterator().next(); // take the first one
			for (final Card originCard : linkedCards) {	// ���� �� ���� ��������� ���������
				if (originCard == null) 
					continue; // return childrenIds;
				this.originCardId = originCard.getId();
				for(ObjectId attrId : forestOriginLnkAttrIds) {
					final LinkAttribute la = (LinkAttribute) originCard.getAttributeById(attrId);
					if (la == null || la.isEmpty()){
						if (!getCardId().getId().equals(originCard.getId()))  // <- ���� �������� �������� �������� � forest, �� ������ ���
							logger.warn("The origin card "+ originCard.getId().getId()+
									" has no links to the tree that contains the current card "
									+getCardId().getId()+" via "+ la);
						// return childrenIds;
					} else {
						List<ObjectId> backLinks;
						if(la instanceof BackLinkAttribute) {
							backLinks = CardUtils.getCardIdsByBackLink(la.getId(), originCard.getId(), 
									getQueryFactory(), getDatabase(), getSystemUser());
						} else {
							backLinks = la.getIdsLinked();
						}
						for(Object id: backLinks)
							childrenIds.add(new Pair(null, (ObjectId)id, attrId));
					}
				}
			}

		} catch (DataException e) {
			logger.error("There is an error when getting the origin card for card "+getCardId().getId());
			e.printStackTrace();
		}
		return childrenIds;
	}
	/**
	 * @comment ����������� ����� ������� ������ ������� �������� (�� ������ forestOriginBkLnkAttrId) + ������������ ���� ������� ������ ��� ���������� �������� �����
	 **/
	@SuppressWarnings("unchecked")
	Set<ObjectId> getRootsFromOrigin(
			ObjectId forestOriginBkLnkAttrId, Set<ObjectId> forestOriginLnkAttrIds)
	{
		final Set<ObjectId> childrenIds = new HashSet<ObjectId>();
		// ������� ��������� �� ������ ���� �������
		if (forestOriginBkLnkAttrId == null 
				|| forestOriginLnkAttrIds == null
				|| forestOriginLnkAttrIds.isEmpty() )
			return childrenIds;

		try {
			Collection<Card> linkedCards = null;

			if ("@SELF".equalsIgnoreCase(forestOriginBackLinkAttrId.getId().toString()) ) {
				// ����� ������� �������� ��� ������ ������
				linkedCards = Collections.singletonList( super.getCard());	// ���� forestOriginBackLinkAttrId ����� ��� @Self, �� ������ �������
			} else 
			{	// ����������� �� �����...
				final ListProject action = new ListProject();
				action.setAttribute(forestOriginBkLnkAttrId);
				action.setCard(getCardId());
				final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();

				for(ObjectId attrId : forestOriginLnkAttrIds) {
					columns.add( CardUtils.createColumn(attrId));
				}

				action.setColumns(columns);

				linkedCards = CardUtils.execSearchCards(action, getQueryFactory(), getDatabase(), getSystemUser());	// ����� �������� ����� ������� ������������ �������� 
			}

			if (linkedCards == null || linkedCards.isEmpty()) {
				logger.error("Can't get the origin card for card "+getCardId().getId()+	// ��� ��������� ��� ������� ��������
						" via "+forestOriginBkLnkAttrId);
				return childrenIds;
			}
			if (linkedCards.size() > 1){
				logger.warn("There are more than one origin card for card "+getCardId().getId()+	// ���������� > 1
						" via "+forestOriginBkLnkAttrId);
				//return childrenIds;
			}
			// linkedCards.iterator().next(); // take the first one
			for (final Card originCard : linkedCards) {	// ���� �� ���� ��������� ���������
				if (originCard == null) 
					continue; // return childrenIds;
				this.originCardId = originCard.getId();
				for(ObjectId attrId : forestOriginLnkAttrIds) {
					final CardLinkAttribute cla = originCard.getCardLinkAttributeById( attrId);
					if (cla == null || cla.isEmpty()){
						if ( !getCardId().getId().equals(originCard.getId()) )  // <- ���� �������� �������� �������� � forest, �� ������ ���
							logger.warn("The origin card "+ originCard.getId().getId()+
									" has no links to the tree that contains the current card "
									+getCardId().getId()+" via "+ cla);
						// return childrenIds;
					} else
						childrenIds.addAll(cla.getIdsLinked());
				}
			}

		} catch (DataException e) {
			logger.error("There is an error when getting the origin card for card "+getCardId().getId());
			e.printStackTrace();
		}
		return childrenIds;
	}

	/** ���������� ����� ������� ��������� ��� �������� ��������, ��������� �� ��������� �������� � ������� ��������� ���������� linkAttrIds**/
	@SuppressWarnings("unchecked")
	Set<ObjectId> getRelatedRoots(ObjectId node, Collection<ObjectId> linkAttrIds){
		List<ObjectId> childrenIds = new ArrayList<ObjectId>();
		List<ObjectId> currLevel = new ArrayList<ObjectId>();

		currLevel.add(node);
		final String sql = "SELECT av.card_id \n"+
		 "FROM attribute_value av \n"+
		   "WHERE av.number_value IN ({0}) AND " +
		   "av.attribute_code IN ({1})";

		logger.debug("Getting roots for "+ node.getId());
		while(!currLevel.isEmpty()){
			childrenIds = currLevel;
			final String sqlExec = MessageFormat.format(sql,
							ObjectIdUtils.numericIdsToCommaDelimitedString(childrenIds), 
							IdUtils.makeIdCodesQuotedEnum(linkAttrIds));
			currLevel = getJdbcTemplate().query(sqlExec, 
					new RowMapper(){
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							final ObjectId id = new ObjectId(Card.class, rs.getLong(1));
							return id;
						}
			});
			if (logger.isDebugEnabled()) {
				for (ObjectId Id : currLevel) {
					logger.debug( "Got parent "+ Id.getId());
				}
			}
		}
		logger.debug("roots are: "+ObjectIdUtils.numericIdsToCommaDelimitedString(childrenIds));
		return new HashSet<ObjectId>( childrenIds);
	}

	/** ���������� ����� ������� ��������� ��� �������� ��������, ��������� �� ��������� �������� � ������� ��������� ���������� linkAttrIds**/
	@SuppressWarnings("unchecked")
	Set<Pair> getRelatedPairRoots(ObjectId node, Collection<ObjectId> linkAttrIds){
		List<Pair> childrenIds = new ArrayList<Pair>();
		List<Pair> currLevel = new ArrayList<Pair>();

		currLevel.add(new Pair(null, node, null));
		final String sql = "SELECT av.card_id, av.attribute_code \n"+
		 "FROM attribute_value av \n"+
		   "WHERE av.number_value IN ({0}) AND " +
		   "av.attribute_code IN ({1})" +
		   " UNION " +
		   "select av.number_value, link.attribute_code " +
		   "FROM card c " +
		   "inner JOIN attribute_option link on link.attribute_code IN ({1}) " +
		   "	and link.option_code=''LINK'' " +
		   "inner join attribute_value av on c.card_id = av.card_id and av.attribute_code = link.option_value " +
		   "WHERE c.card_id IN ({0}) ";

		List<ObjectId> objectIds = new ArrayList<ObjectId>();

		logger.debug("Getting roots for "+ node.getId());
		while(!currLevel.isEmpty()){
			childrenIds = currLevel;
			objectIds.clear();
			for (Pair child: childrenIds)
				objectIds.add(child.cardId);
			
			final String sqlExec = MessageFormat.format(sql,
							ObjectIdUtils.numericIdsToCommaDelimitedString(objectIds), 
							IdUtils.makeIdCodesQuotedEnum(linkAttrIds));
			currLevel = getJdbcTemplate().query(sqlExec, 
					new RowMapper(){
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							final ObjectId id = new ObjectId(Card.class, rs.getLong(1));
							final ObjectId attrid = new ObjectId(CardLinkAttribute.class, rs.getString(2));
							return new Pair(null, id, attrid);
						}
			});
			if (logger.isDebugEnabled()) {
				for (Pair Id : currLevel) {
					logger.debug( "Got parent "+ Id.cardId.getId());
				}
			}
		}
		logger.debug("roots are: "+ObjectIdUtils.numericIdsToCommaDelimitedString(objectIds));
		return new HashSet<Pair>( childrenIds);
	}
	
	@Override
	public void setParameter(String name, String value) {
		if ("nodeLinkAttrIds".equalsIgnoreCase(name)) {
			final List<ObjectId> list = IdUtils.stringToAttrIds(value, CardLinkAttribute.class);
			nodeLinkAttrIds.clear();
			if ((list != null && !list.isEmpty()))
				this.nodeLinkAttrIds.addAll(list);
		} else if ("leafLinkAttrIds".equalsIgnoreCase(name)) {
			final List<ObjectId> list = IdUtils.stringToAttrIds(value, CardLinkAttribute.class);
			this.leafLinkAttrIds.clear();
			if ((list != null && !list.isEmpty()))
				this.leafLinkAttrIds.addAll(list);
		} else if ("forestOriginBackLinkAttrId".equalsIgnoreCase(name)) {
			this.forestOriginBackLinkAttrId = IdUtils.smartMakeAttrId(value, BackLinkAttribute.class);
		} else if ("forestOriginLinkAttrId".equalsIgnoreCase(name)) {
			// ��������� �������
			this.forestOriginLinkAttrIds.clear();
			this.forestOriginLinkAttrIds.add( IdUtils.smartMakeAttrId(value, CardLinkAttribute.class));
		} else if ("forestOriginLinkAttrIds".equalsIgnoreCase(name)) {
			// ������ ���������
			final List<ObjectId> list = IdUtils.stringToAttrIds(value, CardLinkAttribute.class);
			this.forestOriginLinkAttrIds.clear();
			if ((list != null && !list.isEmpty()))
				this.forestOriginLinkAttrIds.addAll( list);
		} else if ("processorClassName".equals(name)) {
			this.processorClassName = value;
		} else if ("curCardIsDefaultRoot".equalsIgnoreCase(name)) {
			this.curCardIsDefaultRoot = Boolean.parseBoolean(value);
		} else if ("processorClassNames".equals(name)) {
			this.processorClassNames = value;
		} else {
			logger.trace( MessageFormat.format( "reg parameter for sub-processors: ''{0}''=''{1}''", name, value));
			parameters.put(name, value);
			super.setParameter(name, value);
		}
	}

	/**
	 * @comment ��������������� ����� ��� ������ � ������������� ������� (������ �� ��������, ������� ��������, ������, �� �������� ��� �������)
	 **/
	private class Pair {
		public ObjectId parentId;
		public ObjectId cardId;
		public ObjectId attr;

		Pair(ObjectId parentId, ObjectId cardId, ObjectId attr){
			this.parentId = parentId;
			this.cardId = cardId;
			this.attr = attr;
		}

		@Override
		public String toString() {
			return (String) ("parent:"+
				parentId == null ? "none" : parentId.getId() +
				" id:"+
				cardId == null ? "none" : cardId.getId() +
				" attr:"+
				attr == null ? "none" : attr.getId());
		}
	}
	/**
	 * ��������������� ����� ������������ (��� ����������� ������ ���������� �������� �����������)
	 **/
	private class SubProcessor {
		public String name;
		public String alias;
		/**
		 * @param name
		 * @param alias
		 */
		public SubProcessor(String name, String alias) {
			this.name = name;
			this.alias = alias;
		}
		
	}
	/**
	 * ��������� ������ �������������� � ������ � ������� �� ������ �������� ������ ����� ������� � ������ "|"
	 **/
	private List<SubProcessor> BuildSubProcessorArrayList(String ClassNames){
		List<SubProcessor> result = new ArrayList<SubProcessor>();
		StringBuilder sb = new StringBuilder();
		sb.append(ClassNames);
		while (sb.length()>0) {
			int nPos = sb.indexOf("|");
			int aPos = (-1==sb.indexOf(","))?sb.length():sb.indexOf(",");
			String name = sb.substring(0, nPos).trim();
			String alias = sb.substring(nPos+1, aPos).trim();
			result.add(new SubProcessor(name, alias));
			sb.delete(0, aPos+1);
		};
		return result;
	}
}
