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
import java.sql.Types;
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

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.BatchAsyncExecution;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.BasePropertySelector;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.UserData;

/**
* �������� �������� attr_test: ������� ��� �������� � �������� ��������. (!) ����� �������
* ��������� �������, ������� ����� ����������� ����������. ��������: <parameter
* name="attr_test" value="jbr.incoming.oncontrol=jbr.incoming.control.no"/> //
* comment="1433" <parameter name="attr_test"
* value="jbr.incoming.oncontrol#jbr.incoming.control.yes"/> // comment="1432"
* <parameter name="attr_test" value="list:JBR_IMPL_ONCONT#1432"/> //
* comment="1432 == yes" <parameter name="attr_test"
* value="jbr.deliveryItem.method=modeDeliveryMEDO"/>
* <br>��������� �������������� �������� � ����������. � ������ ���������� ��� ���������� � 
* ���������� ���������� � ����������, ����� �������� ���� ��������� ����������� ����������
* ��������� {@code "calling@"}, ���� ��������� ��������� ��������� {@code "local@"} (���� � ��, � ������).<br>
* � ������ ���������� ���������, ��� ��������� ��� �������� ���������� ��������� � ��������� ���������. ������:
* <pre>
* {@code
* <specific property="workflowMove.id" value="jbr.exam.execute.assistant">
*	<pre-process class="CallOriginsProcessor" runorder="0">
*		<parameter name="backLinkAttrId" value="jbr.exam.parent" />		
*		<parameter name="local@attr_test" value=".template=jbr.incoming"/>					
*		<parameter name="calling@attr_test" value="jbr.AssignmentExecutor#NULL"/>
* 		...
*	</pre-process>
* </specific>
* }
* </pre>
*/
public class CallOriginsProcessor 
		extends ProcessCard 
		implements Parametrized, DatabaseClient 
{
	private static final long serialVersionUID = 3L;

	private static final String LINK_SEPARATOR = "@";
	private static final String EQUAL_ATTRIBUTE_IN_CURRENT_CARD = "eqAttrInCurCard";	// �������������� ������ ��������: ������������ ����� ���������� ������ �� ��� ��������� ���������, � ������� ������������
	private static final String EQUAL_ATTRIBUTE_IN_LINK_CARD = "eqAttrInLinkCard";		// ������� ����� ����� �������� �� ������� ��������
	// ToDo: ��������� ��� �� ������, ����� ����� ���� �������� ��������� ��������� ��� ���������
	
	private Map<String, String> parameters = new HashMap<String, String>();
	static final String PARAM_ATTR_TEST = "attr_test"; 
	private String processorClassName;
	private String callingPrefix = "calling@";//����� ������������, ���� ��� ���������� ����������� ���������� ��������� � ��������
	private String localPrefix = "local@";//����� ������������, ���� ��� ���������� ����������� ���������� ��������� � ��������
	private ObjectId backLinkAttrId;	
	private List<ObjectId> linkAttrIds = new ArrayList<ObjectId>();	// ��������� ����������� ������ ������������� �� ������ �� ��������, ��������������� ��������� � �������, �� � �� ���, �������, ������ � ������� ����� ��������� ������� ����������� 
	private ObjectId  eqAttrInCurCard;	
	private ObjectId  eqAttrInLinkCard;

	/**
	 * ��������� ��������� ��� ��������������.
	 */
	static final String MSG_CARD_0_DID_NOT_SATISFY_CONDITIONS = "card ''{0}'' did not satisfy conditions -> no more cardstate changes performed";
	static final String MSG_PARAMETER_ASSIGNED_3 = "assigned parameter ''{0}''=''{1}''  ->  ''{2}''";
	// DONE: hardcode to be changed into xml-conditions
	/*
	 * ������� ��� ������ �������� ��������, ��� �������� ���� ��� �������� ��
	 * �� ��������, ����� ������� ����� �� ������� <parameter name="attr_test"
	 * value="jbr.incoming.oncontrol=jbr.incoming.control.no" /> //
	 * comment="1433" <parameter name="attr_test"
	 * value="jbr.incoming.oncontrol#jbr.incoming.control.yes"/> //
	 * comment="1432" <parameter name="attr_test"
	 * value="list:JBR_IMPL_ONCONT#1432" /> //
	 * referencevalue.jbr.incoming.control.yes=1432 //
	 * referencevalue.jbr.incoming.control.no=1433 //
	 * listattribute.jbr.incoming.oncontrol=JBR_IMPL_ONCONT
	 */
	private final List<BasePropertySelector> conditions = new ArrayList<BasePropertySelector>();

	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException {
		if (backLinkAttrId == null){
			logger.error("CallOriginsProcessor: backLinkAttrId is not set.");
			return null;
		}
		// final UserData user = getUser();

		Collection<Card> linkedCards = new ArrayList<Card>();
		Collection<Card> finalCards = new ArrayList<Card>();
		
		Card card = null;
		//���� ���� - �������� �������� �� ����������
		if(getAction() instanceof BatchAsyncExecution) {
			//����� ������ ������ ���� �� ������
			//��� ��� ��������������, ��� ��� �������� �������� �� ���������� �� - ����
			Action<?> action = (Action<?>)((BatchAsyncExecution<?>)getAction()).getActions().get(0);
			if(action instanceof ChangeState) {
				card = ((ChangeState)action).getCard();
			} else {
				logger.debug("Current action is not ChangeState. Exit.");
				return null;
			}
		} else card = getCard();
		if (BackLinkAttribute.class.isAssignableFrom(backLinkAttrId.getType())){
			linkedCards = getProjectCards(card.getId(), backLinkAttrId);
		}else if (CardLinkAttribute.class.isAssignableFrom(backLinkAttrId.getType())){
			linkedCards = getLinkedCards(card.getId(), backLinkAttrId);
		}

		if (linkedCards == null || linkedCards.isEmpty()){
			logger.debug("No origin cards found. Exit.");
			return null;
		}
/*		final Card mainCard = linkedCards.iterator().next(); 
		if (linkedCards.size() > 1){
			logger.warn("(!) WARNING : There are more than 1 origin cards - only first "+ mainCard.getId()+ "will be used.");
		}
*/
		for(Card mainCard : linkedCards){
			if (checkConditions(conditions, mainCard)) {
				finalCards.add(mainCard);
			}
		}
		linkedCards.clear();
		linkedCards.addAll(finalCards);
		for (ObjectId linkAttrId: linkAttrIds){
			linkedCards = getLinkCardsForAttr(linkedCards, linkAttrId);
			if (linkedCards == null || linkedCards.isEmpty()){
				logger.debug("No linked cards found. Exit.");
				return null;
			}
		}
		Attribute eqAttributeInCurCard = null; 
		if (eqAttrInCurCard!=null){
			eqAttributeInCurCard = card.getAttributeById(eqAttrInCurCard);
		}
		
		//O.E., 15.05.2012 - ����� ������������� ���������� �������, ������� �� ����� ���������
		linkedCards = fetchCards(ObjectIdUtils.getIdsFromObjects(linkedCards), Collections.singleton(eqAttrInLinkCard), false);
		for(Card mainCard : linkedCards){
			Attribute eqAttributeInLinkCard = null; 
			if (eqAttrInLinkCard!=null){
				eqAttributeInLinkCard = mainCard.getAttributeById(eqAttrInLinkCard);
			}
			// ���� �������� ������, ���� �� ���������, �� �������� �� �����, �� ������������ �� ������ �������� �� ��������
			if (	(eqAttributeInCurCard!=null&&eqAttributeInLinkCard==null)||
					(eqAttributeInCurCard==null&&eqAttributeInLinkCard!=null)||
					(eqAttributeInCurCard!=null&&eqAttributeInLinkCard!=null&&eqAttributeInCurCard.getType().equals(eqAttributeInLinkCard.getType())&&!checkEqAttrValue(eqAttributeInCurCard, eqAttributeInLinkCard))||
					(eqAttributeInCurCard!=null&&eqAttributeInLinkCard!=null&&!eqAttributeInCurCard.getType().equals(eqAttributeInLinkCard.getType()))
				)
				{
				logger.info(MessageFormat.format("card ''{0}'' and ''{1}'' not equals by attribute ''{2}'' and ''{3}'' -> subprocessor skip", card.getId(), mainCard.getId(), eqAttrInCurCard, eqAttrInLinkCard));
				continue;
			}
				
			ProcessorBase processor;
			try {
				final Class<ProcessorBase> prClass = (Class<ProcessorBase>) 
					Class.forName(processorClassName);
				processor = prClass.newInstance();
				final Set<Class<?>> ints = new HashSet<Class<?>>(Arrays.asList(prClass.getInterfaces()));
				Class<?> supa = prClass.getSuperclass();
				while (supa != null){
					ints.addAll(Arrays.asList(supa.getInterfaces()));
					supa = supa.getSuperclass();
				}
				if (ints.contains(DatabaseClient.class)){
					((DatabaseClient)processor).setJdbcTemplate(getJdbcTemplate());
				}
				/* (BR4J00035582, YNikitin) ����� ���, ��� �������� ��������� ��� ������������� � ����������� CallOriginsProcessor � CallCardsLinkedToBaseProcessor ���� ��� ������� �������� ��� JdbcTemplate � BeanFactory, ��� ��� �������� � QueryFactory, 
				 * � ����� ��� ��������� � ��� � ������ SetParameter ��������� ������. 
				 */
				processor.setBeanFactory(getBeanFactory());
				if (ints.contains(Parametrized.class)) {
					for (Iterator<String> i = parameters.keySet().iterator(); i.hasNext(); ){
						final String name = i.next();
						((Parametrized) processor).setParameter(name, parameters.get(name));
					}
				}
			} catch (Exception e) {
				logger.error("Class "+processorClassName+" not found !");
				throw new DataException(e);
			}
	
			try {
				processor.init(getCurrentQuery());
				processor.setObject( mainCard);
				processor.setUser(getUser());
				processor.setAction(getAction());
				processor.setCurExecPhase(getCurExecPhase());
			} catch (Exception e) {
				logger.error("There is an error when setting "+processorClassName+" processor !", e);
				throw new DataException(e.getMessage());
			}
			
			try {
				processor.process();
			} catch (DataException e) {
				logger.error("There is an error when running "+processorClassName+" processor !", e);
				throw e;
			} catch (Exception e) {
				logger.error("There is an error when running "+processorClassName+" processor !", e);						
				throw new DataException(e.getMessage());
			}
		}
		return null;
	}

	private Collection<Card> getLinkCardsForAttr(Collection<Card> cards, ObjectId linkAttrId) throws DataException {
		if (cards==null || cards.size()==0){
			return null;
		}
		List<Card> result = new ArrayList<Card>();
		for(Card card: cards){
			if (BackLinkAttribute.class.isAssignableFrom(linkAttrId.getType())){	
				result.addAll(getProjectCards(card.getId(), linkAttrId));
			}else if (CardLinkAttribute.class.isAssignableFrom(linkAttrId.getType())){
				result.addAll(getLinkedCards(card.getId(), linkAttrId));
			}
		}
		return result;
	}
	
	private Collection<Card> getProjectCards(ObjectId cardId, ObjectId attrId) throws DataException {
		final UserData user = getSystemUser();

		final ListProject action = new ListProject();
		action.setAttribute(attrId);
		action.setCard(cardId);

		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		columns.add( CardUtils.createColumn(Card.ATTR_STATE));
		columns.add( CardUtils.createColumn(Card.ATTR_TEMPLATE));
		action.setColumns(columns);

		final List<Card> list = CardUtils.execSearchCards(action, getQueryFactory(), getDatabase(), user);
		return list;
	}

	@SuppressWarnings("unchecked")
	private List<Card> getLinkedCards(ObjectId cardId, ObjectId attrId) 
	{
		final List<?> cards = getJdbcTemplate().query(
				"select av.number_value, c.status_id, c.template_id \n" +
				"from attribute_value av \n" +
				"	join card c on c.card_id=av.number_value \n" +
				"where av.card_id=? and av.attribute_code=? \n", 
				new Object[]{cardId.getId(), attrId.getId()},
				new int[] { Types.NUMERIC, Types.VARCHAR },
				new RowMapper(){
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						final Card card = new Card();
						card.setId(rs.getLong(1));
						card.setState(new ObjectId(CardState.class, rs.getLong(2)));
						card.setTemplate(rs.getLong(3));
						return card;
					}
		});

		return (List<Card>)cards; 
	}	

	@Override
	public void setParameter(String name, String value) {
		if ("processorClassName".equals(name)) {
			this.processorClassName = value;
		} else	if ("backLinkAttrId".equals(name)) {
//			this.backLinkAttrId = ObjectIdUtils.getObjectId(BackLinkAttribute.class, value, false);
			String[] links = value.split(LINK_SEPARATOR);
			if (links.length==0)
				return;
			this.backLinkAttrId = IdUtils.smartMakeAttrId(links[0], BackLinkAttribute.class, false);
			if(links.length>1){
				for(int i=1;i<links.length; i++){
					linkAttrIds.add(IdUtils.smartMakeAttrId(links[i], BackLinkAttribute.class, false));
				}
			}
		} else if (  isMultiKey( name, PARAM_ATTR_TEST)) {
			try {
				final AttributeSelector selector =
						AttributeSelector.createSelector(value);
				if (selector != null)
					this.conditions.add(selector);
			} catch (DataException ex) {
				ex.printStackTrace();
			}
		} else if (name.startsWith(localPrefix)){ 			
			setParameter(name.substring(localPrefix.length()), value);
		} else if (name.startsWith(callingPrefix)){ 			
			parameters.put(name.substring(callingPrefix.length()), value);			
			super.setParameter(name.substring(callingPrefix.length()), value);
		} else if (name.equalsIgnoreCase(EQUAL_ATTRIBUTE_IN_CURRENT_CARD)){
			eqAttrInCurCard = IdUtils.smartMakeAttrId(value, Attribute.class, false);
		} else if (name.equalsIgnoreCase(EQUAL_ATTRIBUTE_IN_LINK_CARD)){
			eqAttrInLinkCard = IdUtils.smartMakeAttrId(value, Attribute.class, false);
		} else {
			parameters.put(name, value);
			super.setParameter(name, value);
		}
	}
	

	
	
	/**
	 * ����� ������� ���������� �������� ���������� ��������� � ����������� �� ����
	 * @param eqAttributeInCurCard
	 * @param eqAttributeInLinkCard
	 * @return
	 */
	private boolean checkEqAttrValue(Attribute eqAttributeInCurCard, Attribute eqAttributeInLinkCard){
		if(eqAttributeInCurCard instanceof PersonAttribute && eqAttributeInLinkCard instanceof PersonAttribute){
			return checkEqualsAttrObjectId(eqAttributeInCurCard, eqAttributeInLinkCard);
		}else{
			return eqAttributeInCurCard.getStringValue().equals(eqAttributeInLinkCard.getStringValue());
		}		
	}
	
	/**
	 * ���������� ��� �������� �� �� <b>ObjectId</b>
	 * @param eqAttr1
	 * @param eqAttr2
	 * @return
	 */
	private boolean checkEqualsAttrObjectId(Attribute eqAttr1, Attribute eqAttr2){
		if (((PersonAttribute)eqAttr1).getValues() == null || 
			((PersonAttribute)eqAttr2).getValues() == null)
			return false;
		return !Collections.disjoint(((PersonAttribute)eqAttr1).getValues(), ((PersonAttribute)eqAttr2).getValues());
	}
	

	private boolean checkConditions(List<BasePropertySelector> conds, Card c)
			throws DataException 
	{
		/*
		 * ������ �������� ������: attrList = (ListAttribute)
		 * card.getAttributeById( ((AttributeSelector) cond).attrId);
		 * attrList.getReference() = ObjectId(id='ADMIN_26973',
		 * type=com.apana.dbmi.model.Reference); attrList.getValue() =
		 * ReferenceValue( id=ObjectId(id=1433,
		 * type=com.apana.dbmi.model.ReferenceValue), active=false,
		 * children=null, ..., valueEn="No", valueRu="���")
		 */
		if (conds == null || c == null)
			return true;
		boolean cardFetched = false;
		for (BasePropertySelector cond : conds) {
			if (!cardFetched) {
				// ���������� �������� ���� ��� �� ������...
				if ((cond instanceof AttributeSelector)
						&& null == c.getAttributeById(((AttributeSelector) cond).getAttrId())) 
				{
					c = super.loadCardById(c.getId());
					cardFetched = true;
				}
			}
			final boolean ok = cond.satisfies(c);
			if (!ok) {
				logger.info("Card " + c.getId().getId()
						+ " did not satisfies codition {" + cond
						+ "}  -> no jump performed");
				return false;
			}
		}
		return true;
	}

	/**
	 * ��������� �������� �� name ������ ���� "keyBeginXXX"
	 * @param name
	 * @param keyBegin
	 * @return
	 */
	final static boolean isMultiKey(String name, String keyBegin) {
		// return (name != null) && name.equalsIgnoreCase(keyBegin);
		return (name != null) && (keyBegin != null) 
				&& name.toLowerCase().startsWith( keyBegin.toLowerCase() );
	}
}