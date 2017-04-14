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
package com.aplana.dbmi.jbr.processors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.CardAction;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.LinkItem;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.client.CardLinkLoader;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.BasePropertySelector;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.utils.StrUtils;

/**
 * @author RAbdullin
 * ������� ��� ������������ ��������.
 * ����� ����������� ������ ��������� id-�������� � �� �����.
 * ��������������, ���������� ��������� ��������� � HashList.
 */
public abstract class ProcessCard extends AbstractCardProcessor {
	private static final long serialVersionUID = 4627450971720646077L;
	/*
	 * ���������� ���������
	 */
	public static final String MSG_CARD_0_HAS_EMPTY_ATTR_1 = "Card {0} contain EMPTY attribute ''{1}''";
	public static final String MSG_CARD_0_HAS_NONEMPTY_ATTR_1 = "Card {0} contain NOT EMPTY attribute ''{1}''";
	public static final String MSG_CARD_0_HAS_NO_ATTRIBUTE_1 = "Card {0} DOES NOT CONTAIN attribute ''{1}''";
	public static final String MSG_CARD_ATTRIBUTE_HAS_INVALID_CLASS_5 = "Card {0} contain attribute ''{1}'' with class ''{2}'' but supported classes are only ''{3}'' or ''{4}''";
	public static final String MSG_CARD_0_HAS_NO_MAIN_DOC_VIA_ATTRIBUTE_1 = "Card {0} has no main doc via attribute {1}";

	public static long TOO_SLOW_EXEC_LOW_BOUND_TIME_MS = 2000;

	/**
	 * ���������, ����������� ����� �� xml-����������.
	 * ��� = ����, �������� � ������ ��������, ��� ���������� �������� (��. {@link makeParamKey}).
	 * �������� = �������� "��� ����".
	 */
	final protected Map<String, String> params = new HashMap<String, String>();

	/** ������� ��� �������� cardlink/backlink:
	 * ���� = if cardlink-�������� ��� backlink-��������;
	 * �������� - ������ ��������� � ��������� ��������;
	 */
	final protected Map<MixedId, List<Card>> cardsLinkAttrCash = new HashMap<MixedId, List<Card>>();

	/**
	 * ��� ��������� ��������
	 */
	final protected Map<ObjectId, Card> cardsCash = new HashMap<ObjectId, Card>();

	/**
	 * id backlink-�������� ��� ��������� ���������-���������
	 */
	protected static final String PARAM_MAIN_DOC_BACKLINK_ATTR_ID = "mainDoc.AttrId";

	/**
	 * ������� ��� �������� ���������-��������� (� value ����� ���� �������
	 * �� {@link AttributeSelector} )...
	 */
	protected static final String PARAM_MAIN_DOC_CONDITION = "mainDoc.condition";
	protected List<BasePropertySelector> mainDocConditions = null;
	protected static final String PARAM_RESULT_CONDITION = "result_condition";

	/**
	 * ��� ��������, ������� ������ ��� ����������, ����� ��������� ���������� 
	 */
	protected static final String PARAM_CHANGE_ATTRIBUTE = "changeAttribute";
	protected static final String PARAM_CHANGE_ATTRIBUTE_OPTION = "changeAttributeOption";
	protected List<ObjectId> changeAttributes = null;
	protected boolean isMultiplicationChangeAttributeOption = true;
	
	/**
	 * ������ ���������, �������� �������� ����� ������������� �������� ���� �������
	 */
	protected String booleanExpression;
	

	/**
	 * ������ �������� �� ���������. ����� �������� ����� ������������ ����
	 * ����� getXXXParameter, ���� ����� protected params.
	 * @param name ����-�������� ���������, �������� � ������ �������� ���
	 * ���������� ��������, ������ ������� � ���������� getXXXParameter ���������
	 * ��� ������������� (��. {@link makeParamKey}).
	 * @param value ��������, ����� ��������� ������ � ����� ����.
	 */
	@Override
	public void setParameter(String name, String value) {
		//super.setParameter(name, value);

		name = makeParamKey(name);
		if (name != null)
			this.params.put( name, value );

		if ( PARAM_MAIN_DOC_CONDITION.equalsIgnoreCase(name.trim()) ) {
			try {
				final AttributeSelector selector = AttributeSelector.createSelector(value);
				if( this.mainDocConditions == null )
					this.mainDocConditions = new ArrayList<BasePropertySelector>();
				this.mainDocConditions.add( selector );
			} catch (DataException ex) {
				ex.printStackTrace();
			}
		} else if ( PARAM_CHANGE_ATTRIBUTE.equalsIgnoreCase(name.trim()) ) {
			try {
				String[] values = value.split(";");
				for (String val : values) {
					if (val.trim().length() > 0) {
						final ObjectId changeAttribute = AttrUtils.getAttributeId(val.trim());
						if (this.changeAttributes == null)
							this.changeAttributes = new ArrayList<ObjectId>();
						this.changeAttributes.add(changeAttribute);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if ( PARAM_CHANGE_ATTRIBUTE_OPTION.equalsIgnoreCase(name.trim()) ) {
			isMultiplicationChangeAttributeOption = !"OR".equals(value);
		}
		else  if (name.equalsIgnoreCase(PARAM_RESULT_CONDITION)) {
			if (null != value)
				this.booleanExpression = value.trim();
		} else {
			// call parent's setParameter() only if this parameter is unused here in this class
			// to avoid "[WARN] possibly unused parameter found..." message
			super.setParameter( name, value );
		}
	}

	public void clear() {
		this.params.clear();
		this.cardsCash.clear();
		this.cardsLinkAttrCash.clear();
	}

	/**
	 * ��������� �������������� ������� ���������� ��������.
	 * @param infoStage: �������� �������� ��� ���������.
	 * @param start_ms: ����� ������ �������� � �� (System.currentTimeMillis()).
	 */
	protected void logTime(String infoStage, long start_ms) {
		final long duration_ms = System.currentTimeMillis() - start_ms;
		final boolean isSlow = (duration_ms > TOO_SLOW_EXEC_LOW_BOUND_TIME_MS);
		if (logger.isTraceEnabled() || isSlow) {
			if (isSlow)
				logger.warn(  infoStage + " time is TOO SLOW : [ms] "
						+ String.valueOf(duration_ms) + " > " + TOO_SLOW_EXEC_LOW_BOUND_TIME_MS);
			else
				logger.trace( infoStage + " time is [ms]: "+ String.valueOf(duration_ms));
		}
	}



	/**
	 * ��������� ��������� ����������
	 */
	public String getParameter(final String paramTag, final String defaultValue) {
		final String result = this.params.get( makeParamKey(paramTag));
		return (result != null) ? result : defaultValue;
	}

	public String getParameterTrimmed(final String paramTag, final String defaultValue) {
		String result = getParameter(paramTag, defaultValue);
		return (result != null) ? result.trim() : defaultValue;
	}

	public boolean getBooleanParameter(final String paramTag, final boolean defaultValue) {
		final String value = this.params.get( makeParamKey(paramTag));
		return StrUtils.stringToBool( value, defaultValue);
	}

	public long getLongParameter(final String paramTag, final long defaultValue) {
		long result = defaultValue;
		try  {
			result = Long.parseLong( getParameter(paramTag, String.valueOf(defaultValue)));
		} catch (NumberFormatException e) {
			//do nothing, will return default value
		}
		return result;
	}

	/**
	 * ������� id �� ����� ���������, �������� ��������� ������ ����� ���
	 * ����� �� ��� ��� {@link smartMakeAttrId}.
	 * @param paramTag: �������� ���������.
	 * @param defaultAttrClass: �����/��� ������������ id, ������������, ����
	 * �������� ��������� ��� ��� �������� (� �� ����� �� objectid).
	 * @param isNumeric: ������������ ��������� � defaultAttrClass ��� ��������
	 * �������� ����� ���������.
	 * @return ��������� id ��� null, ���� �������� ��������� null.
	 */
	public ObjectId getAttrIdParameter( final String paramTag,
			final Class<?> defaultAttrClass, final boolean isNumeric)
	{
		final String value = this.params.get( makeParamKey(paramTag));
		return (value == null)
					? null
					: IdUtils.smartMakeAttrId( value, defaultAttrClass, isNumeric);
	}


	/**
	 * �������� ������ id-������, ��������� � ����������� ��������� � ���� ������.
	 * ������ ���� ������ ��������� ��������� id ������������� ����� �������
	 * ��� ����� � �������.
	 * @param paramTag
	 * @param defaultAttrClass: �����/��� ������������ id, ������������, ����
	 * �������� ��������� ��� ��� �������� (� �� ����� �� objectid).
	 * @param isNumeric: ������������ ��������� � defaultAttrClass ��� ��������
	 * �������� ����� ���������.
	 * @param addNulls: true, ���� ���� ��������� ���������� ������ id.
	 * @return ������ ��������� id, null-�������� ��������� � ������ ����
	 * ��������� addNulls==true.
	 */
	public List<ObjectId> getAttrIdsListParameter( final String paramTag,
			final Class<?> defaultAttrClass,
			final String defaultValue,
			final boolean isNumeric,
			final boolean addNulls
		)
	{
		String value = this.params.get( makeParamKey(paramTag));
		if (value == null)
			value = defaultValue;
		return (value == null)
					? null
					: IdUtils.stringToAttrIds( value, defaultAttrClass, isNumeric, addNulls);
	}

	public List<ObjectId> getAttrIdsListParameter( final String paramTag,
			final Class<?> defaultAttrClass,
			final boolean isNumeric,
			final boolean addNulls
		)
	{
		return getAttrIdsListParameter(paramTag, defaultAttrClass, null, isNumeric, addNulls);
	}

	/**
	 * getAttrIdsListParameter( ..., isNumeric=false, addNulls=false);
	 * @return ������ ��������� id, null-�������� �� ��������� � ������.
	 */
	public List<ObjectId> getAttrIdsListParameter( final String paramTag,
			final Class<?> defaultAttrClass)
	{
		return getAttrIdsListParameter(paramTag, defaultAttrClass, false, false);
	}

	public List<ObjectId> getAttrIdsListParameter( final String paramTag,
			final Class<?> defaultAttrClass, final String defaultValue)
	{
		return getAttrIdsListParameter(paramTag, defaultAttrClass, defaultValue, false, false);
	}

	/**
	 * �� ����� �������� ����-�������� ��������� (������� � ������ ������� �
	 * �������� ���������� ��������).
	 * @param name ��� ���������.
	 * @return ������������� �������� ��� ������������� � this.params.
	 */

	protected static String makeParamKey(String name)
	{
		return (name == null) ? null : name.trim().toLowerCase();
	}


	/**
	 * �������� �������� �������� ��� ������� ����������.
	 * ���� ��� ��������, �� ���� id, �������� ����������� (����������� fetch)
	 * �� ����� ���������� ������������.
	 * @param user: ������������ �� ����� �������� ����������� ��������
	 * ������������� ��� �������� ��������, ���� null - �� �� ����� ����������
	 * ������������ (!).
	 * @param attrToChk: ���� �����, �� ���� ������� ����� �����������
	 * ����������� �������� (���������� ������� ��������), �.�., ���� �����
	 * �� ������ �������� ��������, � ������� �� ����� ����� �������� - ��������
	 * ������ ��������� �������� � ����� ��������� �� ����;
	 * ���� ������ null, �� ����� �������� �������� �������� ����� ��������� ������.
	 * @return (������) �������� ��� null, ���� ��� �������� �������� � �� �����
	 * id ��� �������� ���������� (��������, ��� ��������).
	 * @throws DataException
	 */
	public Card getCard( UserData user, final ObjectId attrToChk)
		throws DataException {
		if (user == null)
			user = getSystemUser();

		// ��������� �������� �������� � ������ ��������� (����� ���� � ��� ��
		// ��������� ��� ��������� � ������ ���� ���������:
		// Store/ChangeState, pre/post.
		Card acard = null;
		boolean isObject = false;
		boolean isAction= false;
		if (getObject() instanceof Card) {
			isObject = true;
			acard = (Card) getObject();
		} else if (getAction() instanceof ChangeState) {
			isAction = true;
			acard = ((ChangeState) getAction()).getCard();
		} else if (getAction() instanceof CardAction) {
			acard = ((CardAction) getAction()).getCard();
		} else if (getResult() instanceof Card){
			acard = (Card) getResult();
		}


		// �������� ������� ��������...
		if (	acard == null
				|| (   (attrToChk != null)
					&& (acard.getAttributeById(attrToChk) == null) // � �������� ��� ������������ ��������...
				)
			)
		{	// ��� �������� ��� ��� ��������... �������� ��������� �� id ...
			final ObjectId id = this.getCardId();
			if (id != null) {
				acard = loadCardById( id, user);

				// (2010/07/07, RuSA) >>> ����������� �������� ��������� ��� �������
				if (isObject)
					setObject( acard);
				else if (isAction)
					((ChangeState) getAction()).setCard( acard);
				// <<<
			}
		}
		return acard;
	}

	/**
	 * �������� �������� �������� ��� ������� ����������. ��� �������������
	 * �������� ���������� �� ����� ���������� ������������.
	 * @return ������ �������� (������� �������� ����������� ����� ��
	 * ������� �������� {@link Attribute.ID_CHANGE_DATE}).
	 * @throws DataException
	 */
	public Card getCard() throws DataException {
		return getCard( getSystemUser(), Attribute.ID_CHANGE_DATE);
	}


	/**
	 * ��������� �������� �� id. ������������ ������� ���.
	 * ���������� �� �����������, �� ���-�� null.
	 * @param id ��������
	 * @return �������� ��� null ��� �����������.
	 * @throws DataException
	 */
	public Card loadCardById(ObjectId id) throws DataException {
		return this.loadCardById( id, getSystemUser());
	}

	/**
	 * �������� �������� �� ��. ������������ ��������� ���.
	 * @param id ��������
	 * @param user ������� ������������
	 * @return ����������� ��������
	 */
	public Card loadCardById(ObjectId id, UserData user)
	{
		return this.loadCard( this.cardsCash, id, user);
	}

	/**
	 * ��������� ������� �� id. 
	 * ���������� �� �����������, �� ���-�� null.
	 * @param id ��������
	 * @return ������� ��� null ��� �����������.
	 * @throws DataException
	 */
	public Person loadPersonById(ObjectId id) throws DataException {
		return this.loadPersonById( id, getSystemUser());
	}

	/**
	 * �������� ������� �� ��. 
	 * @param id �������� �������
	 * @param user ������� ������������
	 * @return ����������� ������������
	 */
	public Person loadPersonById(ObjectId id, UserData user)
	{
		return this.loadPerson( id, user);
	}

	/**
	 * �������� ����������� �������� �� ���������� ��������-������.
	 * @param ownerCard ��������, � ������� ������� ������� attrId.
	 * @param attrId  id-�������� � ownerCard.
	 * @return ������ ��������� �������� �� linked-card.
	 * @throws DataException
	 */
	public Card getLinkedCardById(Card ownerCard, ObjectId attrId) throws DataException {
		if ( (ownerCard == null) || (attrId == null) )
			return null;

		final CardLinkAttribute attrLink = ownerCard.getAttributeById(attrId);

		final Card result = getFirstLinkedCard(attrLink, ownerCard.getId() );

		if (result == null && logger.isDebugEnabled())
			logger.debug(MessageFormat.format("attribute ''{0}'' not found in card with id=''{1}''",
				attrId.toString(), ownerCard.getId().toString()));

		return result;
	}

	/**
	 * @param attrLink: �������, �������� �� �������� ���� ���������.
	 * @param cardId: ��� ������������� ����������� (�� �����������) - ��������,
	 * � ������� ���������� ������� attrLink, ���� null, �� �� ����������.
	 * @return ������ ������ fetch-����������� ��������.
	 * @throws DataException
	 */
	protected Card getFirstLinkedCard(final CardLinkAttribute attrLink,  final ObjectId cardId)
		throws DataException
	{
		if (attrLink == null) return null;

		final MixedId mid = new MixedId( cardId, attrLink.getId() );
		if ((cardId != null) && cardsLinkAttrCash.containsKey(mid)) {
			// ���� � ����...
			final List<Card> list = cardsLinkAttrCash.get(mid);
			return (list == null || list.isEmpty()) ? null : list.get(0);
		}
		final Card result = loadCardById( attrLink.getSingleLinkedId());
			cardsLinkAttrCash.put( mid, Collections.singletonList(result));
		return result;
	}

	private SearchResult execListProject( ObjectId cardId, ObjectId backLinkAttrId,
			UserData user) throws DataException
	{
		final ListProject action = new ListProject();
		action.setCard( cardId);
		action.setAttribute( backLinkAttrId);

		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		final SearchResult rs = getDatabase().executeQuery( user, query);
		return rs;
	}

	/**
	 * ����� BackLink, fetch-������ ��� ��������, ����������� �� ���������.
	 * @param cardId: id ��������.
	 * @param backLinkAttrId: id backlink-��������.
	 * @return
	 */
	public List<Card> loadAllBackLinks(final ObjectId cardId,
			final ObjectId backLinkAttrId, UserData user)
	{

		if (backLinkAttrId == null || cardId == null)
			return null;

		// ���-��������
		final MixedId mid = new MixedId( cardId, backLinkAttrId);
		if (cardsLinkAttrCash.containsKey(mid))	// ���� � ����
			return cardsLinkAttrCash.get(mid);

		// ��������...
		try {
			// �������� ��������...
			final List<Card> cards = CardUtils.getCardsList( execListProject(cardId, backLinkAttrId, user));
			ArrayList<Card> result = null;
			if (cards != null) {
				result = new ArrayList<Card>( cards.size());
				for (Card card : cards) {
					final Card fullCard = this.loadCardById(card.getId());
					result.add(fullCard);
				}
			}
			cardsLinkAttrCash.put( mid, result); // �������� backLink-����� ��������
			return result;

		} catch (DataException e) {
			logger.error(MessageFormat.format("Error fetching back-linked cards list for child card id=''{0}'' by back link ''{1}''",
				cardId.toString(), backLinkAttrId.getId()), e);
			return null;
		}
	}

	/**
	 * ����� BackLink, ������ ��� ��������, ����������� �� ��������� ��
	 * ��������� ��������.
	 * @param card
	 * @param attr
	 * @return
	 */
	public List<Card> loadAllBackLinks(final ObjectId cardId, final BackLinkAttribute attr,
			final UserData user)
	{
		if ( cardId == null || attr == null)
			return null;
		return loadAllBackLinks( cardId, attr.getId(), user);
	}

	public List<Card> loadAllBackLinks(final ObjectId cardId, final BackLinkAttribute attr)
		throws DataException
	{
		if ( cardId == null || attr == null)
			return null;
		return loadAllBackLinks( cardId, attr.getId(), getSystemUser());
	}

	/**
	 * ����� CardLink, fetch-������ ��� ��������� ��������.
	 * @param card
	 * @param attr
	 * @return ������ ������ fetch-����������� ��������.
	 */
	public List<Card> loadAllLinks( final ObjectId cardId,
			final CardLinkAttribute attr,
			final UserData user)
	{
		if (attr == null || cardId == null)
			return null;

		// ���-��������
		final MixedId mid = new MixedId( cardId, attr.getId());
		if (cardsLinkAttrCash.containsKey(mid))	// ���� � ����
			return cardsLinkAttrCash.get(mid);

		// ��������...
		try {
			// �������� ��������...
			ArrayList<Card> result = null;
			final Collection<ObjectId> ids = CardUtils.getAttrLinks(attr);
			if ( ids != null && !ids.isEmpty()) {
				result = new ArrayList<Card>( ids.size());
				for (ObjectId id : ids) {
					if (id == null) continue;
					final Card fullCard = this.loadCardById(id, user);
					result.add(fullCard);
				}
			}
			if (result != null) cardsLinkAttrCash.put( mid, result); // �������� cardLink-����� ��������
			return result;

		} catch (Exception e) {
			logger.error(MessageFormat.format("Error fetching card-linked cards list for child card id=''{0}'' by link ''{1}''",
				cardId.toString(), attr.getId()), e);
			return null;
		}
	}

	/**
	 * ����� CardLink, fetch-������ ��� ��������� ��������.
	 * @param card
	 * @param attr
	 * @return ������ ������ fetch-����������� ��������.
	 */
	public List<Card> loadAllPersons( final ObjectId cardId,
			final PersonAttribute attr,
			final UserData user)
	{
		if (attr == null || cardId == null)
			return null;

		// ���-��������
		final MixedId mid = new MixedId( cardId, attr.getId());
		if (cardsLinkAttrCash.containsKey(mid))	// ���� � ����
			return cardsLinkAttrCash.get(mid);

		
		try
		{
			// �������� ��������...
			ArrayList<Card> result = null;
			final Collection<Person> persons = attr.getValues();
			if ( persons != null && !persons.isEmpty())
			{
				result = new ArrayList<Card>( persons.size());
				for (Person p : persons) {
					if (p == null||p.getId()==null) continue;
					ObjectId pCardId = p.getCardId(); 
					if(p.getCardId()==null){
						Person per = this.loadPersonById(p.getId());
						pCardId = per.getCardId();
					}
					final Card fullCard = this.loadCardById(pCardId, user);
					result.add(fullCard);
				}
			}
			if (result != null) cardsLinkAttrCash.put( mid, result); // �������� cardLink-����� ��������
			return result;

		} catch (Exception e) {
			logger.error(MessageFormat.format("Error fetching person-linked cards list for child card id=''{0}'' by link ''{1}''",
				cardId.toString(), attr.getId()), e);
			return null;
		}
	}
	
	/**
	 * �������� (��� ������������� ���������) id ��������� �������� �� ������
	 * ���������� �������� (�.�. ������ ��� ������� ����� ������� ���
	 * CardLinkAttribute, TypedCardLinkAttribute ��� BackLinkAttribute).
	 * @param cardId
	 * @param attr
	 * @return ������ id-������ ��������� ��������.
	 * @throws DataException
	 */
	public List<ObjectId> getAllLinkedIdsByAttr( final ObjectId cardId,
			final Attribute attr, final UserData user
		) throws DataException
	{
		if (attr == null || cardId == null)
			return null;

		// ���-��������
		final MixedId mid = new MixedId( cardId, attr.getId());
		if (cardsLinkAttrCash.containsKey(mid))	{
			// ���� � ���� ������� ������...
			final Set<ObjectId> setIds = ObjectIdUtils.collectionToSetOfIds(cardsLinkAttrCash.get(mid));
			return (setIds == null) ? null : new ArrayList<ObjectId>(setIds);
		}

		List<ObjectId> result = null;
		if (CardLinkAttribute.class.isAssignableFrom(attr.getClass())) {
			final CardLinkAttribute clink = (CardLinkAttribute) attr;
			final Collection<ObjectId> ids = CardUtils.getAttrLinks(clink);
			if (ids != null && !ids.isEmpty())
				result = new ArrayList<ObjectId>( ids);
		} else if (BackLinkAttribute.class.isAssignableFrom(attr.getClass())) {
			// �������� ids ��� back-link ��������...
			// final BackLinkAttribute clink = (BackLinkAttribute) attr;
			// ��������� ������ id ��������� ��������...
			final List<Card> rslist = CardUtils.getCardsList(execListProject(cardId, attr.getId(), user));
			if (rslist != null) {
				final Set<ObjectId> setIds = ObjectIdUtils.collectionToSetOfIds(rslist);
				result = (setIds == null) ? null : new ArrayList<ObjectId>(setIds);
			}
		}

		return result;
	}

	/**
	 * ��������� ��������� �������� �� ������ ���������� ��������, ��� �������
	 * ����� ������� ��� CardLinkAttribute, TypedCardLinkAttribute ��� BackLinkAttribute.
	 * @param cardId
	 * @param attr
	 * @return
	 * @throws DataException
	 */
	public List<Card> loadAllLinkedCardsByAttr( final ObjectId cardId,
			final Attribute attr, final UserData user
		) throws DataException
	{
		if (attr == null || cardId == null)
			return null;

		if (attr instanceof CardLinkAttribute)
		{
			return loadAllLinks(cardId, (CardLinkAttribute) attr, user);
		} else if (attr instanceof BackLinkAttribute) {
			return loadAllBackLinks( cardId, (BackLinkAttribute) attr, user);
		} else if (attr instanceof PersonAttribute) {
			return loadAllPersons( cardId, (PersonAttribute) attr, user);
		}

		logger.error("Invalid attribute type " + attr.getId()+ " only backLink/cardLink/typedLink enabled.");
		throw new DataException(
				"general.unique",
				new Object[] { "Attribute naming:: linked attribute can be used ONLY for backlink/cardlink/typedlink attribute but not for "+ attr.getId() }
			);
	}

	public List<Card> loadAllLinkedCardsByAttr( final ObjectId cardId,
			final Attribute attr) throws DataException
	{
		return loadAllLinkedCardsByAttr(cardId, attr, getSystemUser() );
	}


	/**
	 * ������� id, �������� ��� ��� �� ������� "objectsids.properties", � ����
	 * ��� ��� � ������� - ������ ����������.
	 * @param resIdName: �������� ����� � �������.
	 * @param isType: ��� ������������ id.
	 * @return ������������������ id
	 * @throws DataException ���� ��� � ������� ����� resIdName.
	 */
	public static ObjectId checkGetIdByResName(String resIdName, final Class<?> idType)
		throws DataException
	{
		final ObjectId result = ObjectId.predefined( idType, resIdName);
		if (result == null)
			throw new DataException( "jbr.processor.nodestattr_1",	new Object[] { resIdName } );
		return result;
	}


	private Card loadCard( Map<ObjectId, Card> map, ObjectId cardId, UserData user) {
		if (cardId == null || cardId.getId() == null)
			return null;
		if (!Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card id: " + cardId);

		if (map.containsKey(cardId))
			return map.get(cardId);

		try {
			final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
			// TODO: ����� �������� �������� ���� ������� ��� � �����-�� ���, ����� ��������� ���-�� ������
			cardQuery.setAccessChecker(null);
			cardQuery.setId(cardId);
			final Card card = getDatabase().executeQuery( user, cardQuery);
			map.put(cardId, card); // �����������
			return card;
		} catch (DataException e) {
			// logger.warn("Error fetching card " + cardId.getId() + "; skipped", e);
			logger.error(MessageFormat.format("Error fetching card object with id=''{0}''", cardId), e);
			return null;
		}
	}


	private Person loadPerson( ObjectId personId, UserData user) {
		if (personId == null || personId.getId() == null)
			return null;
		if (!Person.class.equals(personId.getType()))
			throw new IllegalArgumentException("Not a person id: " + personId);

		try {
			final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Person.class);
			// TODO: ����� �������� �������� ���� ������� ��� � �����-�� ���, ����� ��������� ���-�� ������
			cardQuery.setAccessChecker(null);
			cardQuery.setId(personId);
			final Person person = getDatabase().executeQuery( user, cardQuery);
			return person;
		} catch (DataException e) {
			// logger.warn("Error fetching card " + cardId.getId() + "; skipped", e);
			logger.error(MessageFormat.format("Error fetching person object with id=''{0}''", personId), e);
			return null;
		}
	}
	/**
	 * �������� ������ ������ �� �������� � ����� CardLinkAttrinute ��� PersonAttribute.
	 * @param card
	 * @param attr id �������� �� ������� nbgf CardLinkAttribute (������������
	 * ��������) ��� PersonAttribute (���� �������).
	 * @param verbose: true, ����� ������������� ��������������.
	 * @return ������ ������, ��������������� ������������� � attr id.
	 * @throws DataException
	 */
	public Set<Person> getPersonsList( Card card, ObjectId attrListId,
			boolean verbose)
		throws DataException
	{
		if (card == null || attrListId == null)
			return null;

		final String cardInfo = (card.getId() == null || card.getId().getId() == null)
						? "id=null"
						: String.valueOf(card.getId().getId());
		// ��� ��������� ������ (PersonAttribute) ��� (CardlinkAttribute)
		final Attribute attrList = card.getAttributeById(attrListId);
		if (attrList == null) {
			if (verbose && logger.isInfoEnabled())
				logger.info( MessageFormat.format( MSG_CARD_0_HAS_NO_ATTRIBUTE_1,
						cardInfo, attrListId));
			return null;
		}

		// ����������� ������ ������� ���� �����: ������� �
		// ������ �������� ������...
		final boolean isPersonList =
				PersonAttribute.class.isAssignableFrom(attrList.getClass());
		final boolean isCardList =
				CardLinkAttribute.class.isAssignableFrom(attrList.getClass());
		if (!isPersonList && !isCardList) {
			if (verbose && logger.isInfoEnabled())
				logger.warn( MessageFormat.format( MSG_CARD_ATTRIBUTE_HAS_INVALID_CLASS_5,
						cardInfo,
						attrListId,
						attrList.getClass().getName(),
						PersonAttribute.class.getName(),
						CardLinkAttribute.class.getName()
					));
			return null;
		}

		if (isPersonList) {
			final PersonAttribute persons = (PersonAttribute) attrList;
			if (persons.getValues() == null || persons.getValues().size() == 0)
			{
				if (verbose && logger.isInfoEnabled())
					logger.info( MessageFormat.format( MSG_CARD_0_HAS_EMPTY_ATTR_1, cardInfo, attrListId));
				return null;
			}
			return new LinkedHashSet<Person>( CardUtils.getAttrPersons(persons));
		}

		// ����� ������� �������� CardLink-��...
		final CardLinkAttribute cards = (CardLinkAttribute) attrList;
		if (cards.getLinkedCount() < 1)
		{
			if (verbose && logger.isInfoEnabled())
				logger.info( MessageFormat.format( MSG_CARD_0_HAS_EMPTY_ATTR_1, cardInfo, attrListId));
			return null;
		}

		/*
		 * ��������� ������ ������ �� id �������� ������...
		 */
		return CardUtils.getPersonsByCards(cards.getIdsLinked(), getQueryFactory(),
					getDatabase(), getSystemUser());
	}


	/**
	 * �������� ������ id ������������ �������� �� �������� � ����� CardLinkAttrinute ��� PersonAttribute.
	 * @param attr ������ ���� CardLinkAttribute ��� PersonAttribute.
	 * @return �����-��������� id ������������ ��������, ������������� � attr.
	 * @throws DataException
	 * @throws ServiceException
	 */
	public Set<ObjectId> getCardIdsList(Attribute attr) {
		if (attr == null) return null;

		final boolean attrIsPerson =
			PersonAttribute.class.isAssignableFrom(attr.getClass());
		final boolean attrIsCardLink =
			CardLinkAttribute.class.isAssignableFrom(attr.getClass());

		// ��� ��������� ������ (PersonAttribute) ��� (CardlinkAttribute)
		if (!attrIsPerson && !attrIsCardLink)
			return null;

		if (attrIsCardLink) {
			final CardLinkAttribute persons = (CardLinkAttribute) attr;
			if (persons.isEmpty()) {
				// logger.info( makeAttrInfo( MSG_CARD_HAS_EMPTY_ATTR_2, this.card, this.attrId));
				return null;
			}
			final Collection<ObjectId> ids = CardUtils.getAttrLinks(persons);
			return (ids == null)? new HashSet<ObjectId>() : new HashSet<ObjectId>(ids);
		}

		/*
		 *  ����� ��� ��� ����� PersonAttribute,
		 *  ��������� �� �������� ������ ��������...
		 */
		final Collection<Person> persons = CardUtils.getAttrPersons( (PersonAttribute) attr);
		if (persons == null || persons.isEmpty())
			return null;

		final Set<ObjectId> result = new HashSet<ObjectId>();
		for (Person person : persons) {
			if (person != null && person.getCardId() != null)
				result.add( person.getCardId());
		}
		return result;
	}

	/**
	 * ����� ��� ��������� �������� ������� �� �������� ��������� � ���������.
	 * @param cardId: id ��������.
	 * @param destState: id �������� ���������.
	 * @param user: ������ ������������ ��� �������.
	 * @return ������� ��� null, ���� ��� ������.
	 * @throws DataException
	 */
	public WorkflowMove findWorkFlowMove(ObjectId cardId, ObjectId destState,
			UserData user)
		throws DataException
	{
		final WorkflowMove result = CardUtils.findWorkFlowMove(
				cardId, destState, getQueryFactory(), getDatabase(), user);
		return result;
	}

	/**
	 * TODO ����� ��������� �� ����� ������ ������ � ���������� ��
	 * TODO � �������������� CardUtils.expandLinks(...)
	 * �� ���������� ������ ��������� �������� � ������� ����������.
	 * ���� attributes �����, �� ����� ��������� ����������� ��������.
	 * @param idOrObjList: ������ id �������� ��� ������ ����� ��������.
	 * @param attributes: ������ ��������.
	 * @return ����������� ��������, ���������� ��� ������� ��������� ��������
	 * ��� null, ���� ��� �� ����� ��� ������ ����.
	 * @throws DataException
	 */
	public List<Card> expandLinks(
				Collection<?> idOrObjList,
				ObjectId[] attributes,
				UserData user
			) throws DataException
	{
		final List<Card> result = CardUtils.expandLinks(
				idOrObjList, attributes, getQueryFactory(), getDatabase(), user);
		return result;
	}


	/**
	 * �� ���������� ��������-������ ��������� �������� � ������� ����������.
	 * ���� attributes �����, �� ����� ��������� ����������� ��������.
	 * @param idOrObjList: ������ id �������� ��� ������ ����� ��������.
	 * @param attributes: ������ ��������.
	 * @return ����������� ��������, ���������� ��� ������� ��������� ��������
	 * ��� null, ���� ��� �� ����� ��� ������ ����.
	 * @throws DataException
	 */
	public List<Card> expandLinks(
				CardLinkAttribute linkAttr,
				ObjectId[] attributes,
				UserData user
			) throws DataException
	{
		final List<Card> result = CardUtils.expandLinks(
				linkAttr, attributes, getQueryFactory(), getDatabase(), user);
		return result;
	}


	/**
	 * ��������� ���������� ��������.
	 * (!) ��� ������������� �������� ����������� �� ����� ���������� ���
	 * ������������� user.
	 * @param card
	 * @param user ������������ �� ����� �������� ����������� ���������� ���
	 * �������������, �������� null = systemUser.
	 * @throws DataException
	 */
	public ObjectId saveCard(Card card, UserData user)
		throws DataException
	{
		if (card == null) return null;

		if (user == null)
			user = getSystemUser();

		ObjectId id = null;

		boolean postUnlock = false;
		if (card.getId() != null) {
			super.execAction(new LockObject(card.getId()));
			postUnlock = true;
		}
		try {
			id = rawSaveCard(card, user);
		} finally {
			if (postUnlock)
				super.execAction(new UnlockObject(card.getId()));
		}
		return id;
	}

	public void reloadCard()
	throws DataException{
		reloadCard(getSystemUser());
	}
	
	/**
	 * ��������� �������� �� �� � �������� � � ������ 
	 * @param user ������������ �� ����� �������� ����������� �������� �� ��
	 * @throws DataException
	 */
	public void reloadCard(UserData user)
		throws DataException
	{
		Card activeCard = loadCardById(getCardId(), user); 
		if (getObject() instanceof Card) {
			setObject(activeCard);
		} else if (getAction() instanceof ChangeState) {
			setObject(activeCard);
			((ChangeState) getAction()).setCard(activeCard);
		} else if (getResult() instanceof Card){
			setResult(activeCard);
		}
	}

	/**
	 * @param card
	 * @return
	 * @throws DataException
	 */
	protected ObjectId rawSaveCard(Card card, UserData user)
			throws DataException
	{
		final SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(card);
		// TODO: ����� �������� �������� ���� �������
		// ��� � �����-�� ���, ����� ��������� ���-�� ������
		saveQuery.setAccessChecker(null);
		saveQuery.setObject(card);
		return (ObjectId) getDatabase().executeQuery(user, saveQuery);
	}

	/**
	 * ��� ����������� ����� ���� cardId + attrId.
	 * @author RAbdullin
	 */
	private class MixedId {
		final ObjectId cardId;
		final ObjectId attrId;

		/**
		 * @param cardId
		 * @param attrId
		 */
		public MixedId(ObjectId cardId, ObjectId attrId) {
			super();
			this.cardId = cardId;
			this.attrId = attrId;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
				+ ((this.cardId == null) ? 0 : this.cardId.hashCode());
			result = prime * result
				+ ((this.attrId == null) ? 0 : this.attrId.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;

			final MixedId other = (MixedId) obj;

			if (this.cardId == null) {
				if (other.cardId != null) return false;
			} else if (!this.cardId.equals(other.cardId))
				return false;

			if (this.attrId == null) {
				if (other.attrId != null) return false;
			} else if (!this.attrId.equals(other.attrId))
				return false;

			return true;
		}
	}

	/**
	 * �������� �������� ��������� � ��������.
	 * @param cardId ��������
	 * @param user ������������, �� ����� �������� ������� ��� ���������
	 * @param attributes �������� ��� ����������
	 * @throws DataException
	 */
	public void doOverwriteCardAttributes(ObjectId cardId, UserData user,
			Attribute... attributes) throws DataException
	{
		if ( cardId == null || attributes == null || attributes.length == 0)
			return;

		if (user == null)
			user = getSystemUser();

		super.execAction(new LockObject(cardId));
		try {
			final OverwriteCardAttributes writer = new OverwriteCardAttributes();
			final Collection<Attribute> colAttr = Arrays.asList(attributes);
			writer.setCardId(cardId);
			writer.setAttributes(colAttr);

			super.execAction(writer, user);
		} finally {
			super.execAction(new UnlockObject(cardId));
		}
	}

	/**
	 * �������� �������� ��������� � �������� �� ����� ���������� ������������.
	 * @param cardId ��������
	 * @param attributes �������� ��� ����������
	 * @throws DataException
	 */
	public void doOverwriteCardAttributes(ObjectId cardId, Attribute... attributes)
					throws DataException{
		doOverwriteCardAttributes( cardId, getSystemUser(), attributes);
	}


	/**
	 * �������� ������ �������� � ��������� ����������.
	 * @param ids ������ ��������
	 * @param fetchAttributes ������ ����������� ��� �������� ���������
	 * @param stubIfNull ��������� ������ ���������� ��� ���������, �� ������� ��������
	 */
	@SuppressWarnings("unchecked")
	public Collection<Card> fetchCards(Collection<ObjectId> ids, Collection<ObjectId> fetchAttributes, boolean stubIfNull)
		throws DataException{
		Collection<Card> cards = CardLinkLoader.loadCardsByIds(
				ids, fetchAttributes.toArray(new ObjectId[fetchAttributes.size()]), getSystemUser(), getQueryFactory(), getDatabase()
		);

		for (Card card: cards){
			ListAttribute stateAttr = card.getAttributeById(Card.ATTR_STATE);
			if(stateAttr != null) card.setState(new ObjectId(CardState.class, stateAttr.getValue().getId().getId()));
			ListAttribute templateAttr = card.getAttributeById(Card.ATTR_TEMPLATE);
			if(templateAttr != null) card.setTemplate(new ObjectId(Template.class, templateAttr.getValue().getId().getId()));
			if(stubIfNull){
				for(ObjectId attrId: fetchAttributes){
					if(card.getAttributeById(attrId) == null){
						ObjectQueryBase getAttributeQuery = getQueryFactory().getFetchQuery(attrId.getType());
						getAttributeQuery.setId(attrId);
						Attribute a = getDatabase().executeQuery(getSystemUser(), getAttributeQuery);
						card.getAttributes().add(a);
					}
				}
			}

		}
		return cards;
	}

	/**
	 * �������� ������������ �������� � ��������� ����������.
	 * @param id ����� ��������
	 * @param fetchAttributes ������ ����������� ��� �������� ���������
	 * @param stubIfNull ��������� ������ ���������� ��� ���������, �� ������� ��������
	 */
	public Card fetchSingleCard(ObjectId id, Collection<ObjectId> fetchAttributes, boolean stubIfNull) throws DataException{
		return fetchCards(Collections.singleton(id), fetchAttributes, stubIfNull).iterator().next();
	}
	
	/**
	 * �������� ������������ �������� �� ����� ����������
	 * 
	 * @param cardId
	 * @return
	 * @throws DataException
	 */
	protected Card fetchCard(ObjectId cardId) throws DataException {
		final ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
		query.setId(cardId);
		return getDatabase().executeQuery(getSystemUser(), query);
	}

	/**
	 * �������� ������������ �� id
	 */
	public Person getPerson(ObjectId personId) throws DataException{
		if(personId == null || personId.getId() == null) return null;
		ObjectQueryBase getPersonQuery = getQueryFactory().getFetchQuery(Person.class);
		getPersonQuery.setId(personId);
		return getDatabase().executeQuery(getSystemUser(), getPersonQuery);
	}

	/**
	 * �������� ������������ �� systemrole
	 */
	public List<Person> getPersonByRole(ObjectId roleId) throws DataException{
		if(roleId == null || roleId.getId() == null) return null;
		ChildrenQueryBase getChildrenQuery = getQueryFactory().getChildrenQuery(SystemRole.class, Person.class);
		getChildrenQuery.setParent(roleId);
		return getDatabase().executeQuery(getSystemUser(), getChildrenQuery);
	}

	/**
	 * �������� �������������� �������� ����� ���� ������ ������������ �����
	 * @param firstId - id �������� ��������
	 * @param linksChain - ������ ������ (����� ���� �������/��������� CardLink'���)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Collection<ObjectId> traverseCardLinksChain(ObjectId firstId, List<LinkItem> linksChain){
		if(firstId == null) return null;
		if(linksChain == null || linksChain.isEmpty()) return Collections.singletonList(firstId);

		StringBuilder builder = new StringBuilder();
		builder.append("?");
		int i = linksChain.size();
		Object[] args = new Object[i + 1];
		int[] argsTypes = new int[i + 1];
		args[i] = firstId.getId();
		argsTypes[i] = Types.NUMERIC;
		i--;

		for(LinkItem item : linksChain){
			builder.insert(0,
					"select " + (item.isReversed() ? "card_id " : "number_value ") + "from attribute_value "
					+ "where attribute_code = ? and " + (item.isReversed() ? "number_value " : "card_id ") + "in ("
			).append(")");
			args[i] = item.getLinkId().getId();
			argsTypes[i] = Types.VARCHAR;
			i--;
		}

		return getJdbcTemplate().query(builder.toString(), args, argsTypes, new RowMapper(){
			public Object mapRow(ResultSet arg0, int arg1) throws SQLException {
				return new ObjectId(Card.class, arg0.getLong(1));
			}
		});
	}

	/**
	 * ����� �� ��������� ������� ��� ���������-���������
	 * @return true/false
	 */
	protected boolean isMainDocConditionSet() {
		if (mainDocConditions != null)
			if(!mainDocConditions.isEmpty())
				return true;
		return false;
	}

	/**
	 * ���������, �������� �� ��������-��������� ��� ��������� �������
	 * @param card ������� ��������
	 * @return true, ���� ��������-��������� �������� ������� ���-���, false �����.
	 * (!) ���� ������ �� ���-��������� �� ���������� (�� ������ � ������������),
	 * �� �������� ���-�� true;
	 * (!) ���� ���-��������� �� ����� ���� �������� ���������, ��� ������� ��
	 * �����������, �.�. �������� ��� false.
	 * @throws DataException
	 */
	protected boolean chkDocMain(Card card) throws DataException {
		final List<Card> linkedCardsList = new ArrayList<Card>();

		return chkDocMain(card, linkedCardsList);
	}
	/**
	 * ���������, �������� �� ��������-��������� ��� ��������� �������
	 * @param card ������� ��������
	 * @return true, ���� ��������-��������� �������� ������� ���-���, false �����.
	 * (!) ���� ������ �� ���-��������� �� ���������� (�� ������ � ������������),
	 * �� �������� ���-�� true;
	 * (!) ���� ���-��������� �� ����� ���� �������� ���������, ��� ������� ��
	 * �����������, �.�. �������� ��� false.
	 * @throws DataException
	 */
	protected boolean chkDocMain(Card card, List<Card> linkedCardsList) throws DataException {
		if (linkedCardsList == null) {
			linkedCardsList = new ArrayList<Card>();
		}
		final String sId = getParameterTrimmed( PARAM_MAIN_DOC_BACKLINK_ATTR_ID, null );
		if (sId == null || card == null || mainDocConditions == null || mainDocConditions.isEmpty()) {
			if( logger.isDebugEnabled() )
				logger.debug(MessageFormat.format("No conditions for main doc of card {0}", card == null ? null : card.getId()));
			return true;
		}

		final ObjectId mainDocId = IdUtils.smartMakeAttrId( sId, BackLinkAttribute.class );
		if (mainDocId == null) {
			logger.warn(MessageFormat.format(MSG_CARD_0_HAS_NO_ATTRIBUTE_1, card.getId(), sId));
			return false;
		}

		final Attribute attr = card.getAttributeById(mainDocId);
		if (attr == null) {
			logger.warn(MessageFormat.format(MSG_CARD_0_HAS_NO_ATTRIBUTE_1, card.getId(), mainDocId));
			return false;
		}

		linkedCardsList.clear();
		linkedCardsList.addAll(loadAllLinkedCardsByAttr( card.getId(), attr ));
		if (linkedCardsList.isEmpty()) {
			logger.warn(MessageFormat.format(MSG_CARD_0_HAS_NO_MAIN_DOC_VIA_ATTRIBUTE_1, card.getId(), mainDocId));
			return false;
		}

		// �������� �������...
		for (Card cardMainDoc: linkedCardsList) {
			for (BasePropertySelector cond: this.mainDocConditions) {
				if (!cond.satisfies(cardMainDoc)) {
					logger.info( "Card " + cardMainDoc.getId().getId() + " does not satisfy condition " + cond );
					return false;
				}
			}
		}

		return true;
	}


	/**
	 * ���������, �������� �� ��������-��������� ��� ��������� �������
	 * @param cardMainDoc ������������ ��������
	 * @return true, ���� ��������-��������� �������� ������� ���-���, false �����.
	 * (!) ���� ������ �� ���-��������� �� ���������� (�� ������ � ������������),
	 * �� �������� ���-�� true;
	 * (!) ���� ���-��������� �� ����� ���� �������� ���������, ��� ������� ��
	 * �����������, �.�. �������� ��� false.
	 * @throws DataException
	 */
	protected boolean chkDocMain_direct(Card cardMainDoc) throws DataException {
		if (cardMainDoc == null) {
			logger.info( "Parent card not defined. Checking is failed.");
			return false;
		}

		if( (mainDocConditions == null) || (mainDocConditions.isEmpty()))
			// ������ ���������
			return true;
		// � ������� �� ������ ������, �� ����� ������� ListProject	� ������ ��������� ��������
		// ��� ��� �������� � ��� ��� ���� �����������
		// ������ ������� �� ������ CreateCard, ����� ����� � ��������� � ���� ��� ���, � ���.�������� ����
		// � ListProject (DoListLInkedCards) � ���� �������.
		// �������� �������... ( ��. ���� � chkDocMain() )
		for (BasePropertySelector cond: this.mainDocConditions ) {
			if (!cond.satisfies(cardMainDoc)) {
				logger.info( "Card " + cardMainDoc.getId().getId() + " does not satisfy condition " + cond );
				return false;
			}
		}
		return true;
	}


	/**
	 * ���������� ����� ������������ �������� ����� ���� CreateCard, ���� ��������� ��� ������ ��
	 * @return �������� ���������-��������� ��� null, ���� ����� ����� �� �������
	 */
	protected Card getMainDocCardFromCreateCardAction() {
		Action action = getAction();
		if (action == null) {
			logger.warn( "Processor is not connected to action." );
			return null;
		}
		if (!(action instanceof CreateCard) ) {
			logger.error( "Connected action is not CreateCard action" );
			return null;
		}
		CreateCard action_create = (CreateCard)action;
		// try to get parent from action
		Card parent_card = action_create.getParent();
		if (parent_card == null) {
			logger.error( "Card is just created and CreateCard action has no parent card set, cannot find main doc card!" );
			return null;
		}
		return parent_card;
	}
	
	protected boolean isAttributeChanged(Attribute attr, Card card) throws DataException{
		switch (this.getCurExecPhase()){
			case POSTPROCESS: {
				return isAttributeChangedInPostPhase(attr, card);
			}
			case PREPROCESS: {
				return isAttributeChangedInPrePhase(attr, card);
			}
			case VALIDATE: {
				return isAttributeChangedInPrePhase(attr, card);
			}
			case PREPARE: {
				return isAttributeChangedInPrePhase(attr, card);
			}
			default: throw new DataException("Unknown phase of processor: "+this.getCurExecPhase());
		}		
	}
	/**
	 * ����� ���������, ��������� �� ������� � ������ �� ��������� � ���, ��� ������ � ���� 
	 * (���������� ��� ���-�����������)
	 * @param attr - ������� �� ��������
	 * @param card - ������ ��������
	 * @return true - ���������, false - �� ���������
	 */
	private boolean isAttributeChangedInPrePhase(Attribute attr, Card card){
		// ������� �� ����� ��� �������� �� ������, �� ������� �� �������
		if (attr==null||card==null)
			return false;
		// ���� �������� �����, �� �������, ��� ������� ���������
		if (card.getId()==null)
			return true;
		if (attr instanceof CardLinkAttribute){
			String ids = ((CardLinkAttribute)attr).getLinkedIds();
			if (ids==null||ids.trim().length()==0)
				ids = "-1";
			final int oldCount = getJdbcTemplate().queryForInt(
					"select count(*) FROM attribute_value " +
					"WHERE attribute_code=? AND card_id=?",
					new Object[] { attr.getId().getId().toString(), card.getId().getId() },
					new int[] { Types.VARCHAR, Types.NUMERIC }
				);
			final int newCount = getJdbcTemplate().queryForInt(
					"select count(*) FROM card " +
					"WHERE card_id in (-1, "+ids+")",
					new Object[] { },
					new int[] { }
				);
			final int newInOldCount = getJdbcTemplate().queryForInt(
					"select count(*) FROM attribute_value " +
					"WHERE attribute_code=? AND card_id=? and number_value in ("+ids+")",
					new Object[] { attr.getId().getId().toString(), card.getId().getId() },
					new int[] { Types.VARCHAR, Types.NUMERIC }
				);
			return !((oldCount==newCount)&(oldCount==newInOldCount)&(newCount==newInOldCount));
		} else if (attr instanceof StringAttribute){
			String value = ((StringAttribute)attr).getValue();
			if (value == null)
				value = "";
			final int count = getJdbcTemplate().queryForInt(
					"select 1 FROM attribute_value av " +
					"WHERE av.attribute_code=? AND av.card_id=? and coalesce(av.string_value, '')=coalesce(?, '')",
					new Object[] { attr.getId().getId().toString(), card.getId().getId(), value },
					new int[] { Types.VARCHAR, Types.NUMERIC, Types.VARCHAR }
				);
			return !(count==1);
		} else if (attr instanceof DateAttribute){
			final Date value = ((DateAttribute)attr).getValue();
			final int count = getJdbcTemplate().queryForInt(
					"select 1 FROM attribute_value av " +
					"WHERE av.attribute_code=? AND av.card_id=? and coalesce(av.date_value, '1970-01-01')=coalesce(?, '1970-01-01')",
					new Object[] { attr.getId().getId().toString(), card.getId().getId(), value },
					new int[] { Types.VARCHAR, Types.NUMERIC, Types.DATE }
				);
			return !(count==1);
		} else if (attr instanceof ListAttribute){
			final ReferenceValue value = ((ListAttribute)attr).getValue();
			final int count = getJdbcTemplate().queryForInt(
					"select 1 FROM attribute_value av " +
					"WHERE av.attribute_code=? AND av.card_id=? and coalesce(av.value_id, -1)=coalesce(?, -1)",
					new Object[] { attr.getId().getId().toString(), card.getId().getId(), (value==null)?-1:value.getId() },
					new int[] { Types.VARCHAR, Types.NUMERIC, Types.NUMERIC }
				);
			return !(count==1);
		} else if (attr instanceof PersonAttribute){
			final Collection list = ((PersonAttribute)attr).getValues();
			final String ids = IdUtils.makeIdPersonList(list, ",");
			final int oldCount = getJdbcTemplate().queryForInt(
					"select count(*) FROM attribute_value " +
					"WHERE attribute_code=? AND card_id=?",
					new Object[] { attr.getId().getId().toString(), card.getId().getId() },
					new int[] { Types.VARCHAR, Types.NUMERIC }
				);
			final int newCount = getJdbcTemplate().queryForInt(
					"select count(*) FROM person " +
					"WHERE person_id in (-1, "+ids+")",
					new Object[] { },
					new int[] { }
				);
			final int newInOldCount = getJdbcTemplate().queryForInt(
					"select count(*) FROM attribute_value " +
					"WHERE attribute_code=? AND card_id=? and number_value in ("+ids+")",
					new Object[] { attr.getId().getId().toString(), card.getId().getId() },
					new int[] { Types.VARCHAR, Types.NUMERIC }
				);
			return !((oldCount==newCount)&(oldCount==newInOldCount)&(newCount==newInOldCount));		
		}

		return true;
	}

	/**
	 * ����� ���������, ��������� �� ������� � ��������� ������ �������� �� ��������� � ����������
	 * (���������� ��� ����-�����������)
	 * @param attr - ������� �� ��������
	 * @param card - ������ ��������
	 * @return true - ���������, false - �� ���������
	 */
	private boolean isAttributeChangedInPostPhase(Attribute attr, Card card){
		// �������� �� ������ => ������� �� �������
		if (card==null)
			return false;
		// ���� �������� �����, �� �������, ��� ������� ���������
		if (card.getId()==null)
			return true;
		// �������� ����� ��������� ������ ��������
		long prevVersionId;
		try{
			prevVersionId = getJdbcTemplate().queryForInt(
				"select coalesce(max(cv.version_id), -1) \n" +
				"from card_version cv \n" +
				"where cv.card_id = ?",
				new Object[] { card.getId().getId() },
				new int[] { Types.NUMERIC }
			);
		} catch(EmptyResultDataAccessException e){
			prevVersionId = -1;
		}
		// ���� ���������� ������ ���, �� ������ �������� ������ ����� ����� � ������ ������� ����� �������
		if (prevVersionId==0||prevVersionId==-1){
			return true;
		}
		if (attr instanceof CardLinkAttribute){
			final int oldCount = getJdbcTemplate().queryForInt(
					"select count(*) from attribute_value_hist av " +
					"where card_id = ? and av.version_id = ? and av.attribute_code = ?",
					new Object[] { card.getId().getId(), prevVersionId, attr.getId().getId().toString() },
					new int[] { Types.NUMERIC, Types.NUMERIC, Types.VARCHAR }
				);
			final int newCount = getJdbcTemplate().queryForInt(
					"select count(*) from attribute_value av " +
					"where card_id = ? and av.attribute_code = ?",
					new Object[] { card.getId().getId(), attr.getId().getId().toString() },
					new int[] { Types.NUMERIC, Types.VARCHAR }
				);
			final int newInOldCount = getJdbcTemplate().queryForInt(
					"select count(*) FROM attribute_value_hist avh " +
					"WHERE avh.attribute_code=? AND avh.card_id=? and avh.version_id = ? and avh.number_value in (" +
					"select av.number_value from attribute_value av " +
					"where av.attribute_code = ? and av.card_id = ?)",
					new Object[] { attr.getId().getId().toString(), card.getId().getId(), prevVersionId, attr.getId().getId().toString(), card.getId().getId() },
					new int[] { Types.VARCHAR, Types.NUMERIC, Types.NUMERIC, Types.VARCHAR, Types.NUMERIC }
				);
			return !((oldCount==newCount)&(oldCount==newInOldCount)&(newCount==newInOldCount));
		} else if (attr instanceof StringAttribute){
			String value;
			List<String> values = getJdbcTemplate().queryForList(
					"select string_value FROM attribute_value av " +
					"WHERE av.attribute_code=? AND av.card_id=?",
					new Object[] { attr.getId().getId().toString(), card.getId().getId()},
					new int[] { Types.VARCHAR, Types.NUMERIC });
			if (values==null||values.isEmpty()){
				value = "";
			} else {
				value = values.get(0);
			}
			final int count = getJdbcTemplate().queryForInt(
					"select 1 FROM attribute_value_hist avh " +
					"WHERE avh.attribute_code=? AND avh.card_id=? and avh.version_id coalesce(avh.string_value, '')=coalesce(?, '')",
					new Object[] { attr.getId().getId().toString(), card.getId().getId(), value },
					new int[] { Types.VARCHAR, Types.NUMERIC, Types.VARCHAR }
				);
			return !(count==1);
		} else if (attr instanceof DateAttribute){
			Date value;
			List<Date> values = getJdbcTemplate().queryForList(
					"select date_value FROM attribute_value av " +
					"WHERE av.attribute_code=? AND av.card_id=?",
					new Object[] { attr.getId().getId().toString(), card.getId().getId()},
					new int[] { Types.VARCHAR, Types.NUMERIC });
			if (values==null||values.isEmpty()){
				value = null;
			} else {
				value = values.get(0);
			}
			final int count = getJdbcTemplate().queryForInt(
					"select 1 FROM attribute_value_hist avh " +
					"WHERE avh.attribute_code=? AND avh.card_id=? and avh.version_id = ? and coalesce(avh.date_value, '1970-01-01')=coalesce(?, '1970-01-01')",
					new Object[] { attr.getId().getId().toString(), card.getId().getId(), value },
					new int[] { Types.VARCHAR, Types.NUMERIC, Types.DATE }
				);
			return !(count==1);
		} else if (attr instanceof ListAttribute){
			Long value;
			List<Long> values = getJdbcTemplate().queryForList(
					"select value_id FROM attribute_value av " +
					"WHERE av.attribute_code=? AND av.card_id=?",
					new Object[] { attr.getId().getId().toString(), card.getId().getId()},
					new int[] { Types.VARCHAR, Types.NUMERIC });
			if (values==null||values.isEmpty()){
				value = null;
			} else {
				value = values.get(0);
			}

			final int count = getJdbcTemplate().queryForInt(
					"select 1 FROM attribute_value av " +
					"WHERE av.attribute_code=? AND av.card_id=? and coalesce(av.value_id, -1)=coalesce(?, -1)",
					new Object[] { attr.getId().getId().toString(), card.getId().getId(), (value==null)?-1:value },
					new int[] { Types.VARCHAR, Types.NUMERIC, Types.NUMERIC }
				);
			return !(count==1);
		} else if (attr instanceof PersonAttribute){
			final int oldCount = getJdbcTemplate().queryForInt(
					"select count(*) from attribute_value_hist av " +
					"where card_id = ? and av.version_id = ? and av.attribute_code = ?",
					new Object[] { card.getId().getId(), prevVersionId, attr.getId().getId().toString() },
					new int[] { Types.NUMERIC, Types.NUMERIC, Types.VARCHAR }
				);
			final int newCount = getJdbcTemplate().queryForInt(
					"select count(*) from attribute_value av " +
					"where card_id = ? and av.attribute_code = ?",
					new Object[] { card.getId().getId(), attr.getId().getId().toString() },
					new int[] { Types.NUMERIC, Types.VARCHAR }
				);
			final int newInOldCount = getJdbcTemplate().queryForInt(
					"select count(*) FROM attribute_value_hist avh " +
					"WHERE avh.attribute_code=? AND avh.card_id=? and avh.version_id = ? and avh.number_value in (" +
					"select av.number_value from attribute_value av " +
					"where av.attribute_code = ? and av.card_id = ?)",
					new Object[] { attr.getId().getId().toString(), card.getId().getId(), prevVersionId, attr.getId().getId().toString(), card.getId().getId() },
					new int[] { Types.VARCHAR, Types.NUMERIC, Types.NUMERIC, Types.VARCHAR, Types.NUMERIC }
				);
			return !((oldCount==newCount)&(oldCount==newInOldCount)&(newCount==newInOldCount));
		}

		return true;
	}

	/**
	 * ��������� �������� ��������� � ���������, �������� �� �� ��� ��������� �������
	 * @param card ������� ��������
	 * @return card,  �������� ��������-��������� ��� null
	 * @throws DataException
	 */
	protected Card validateAndCalculateParent(final Card card) throws DataException {
		Card c_parent = null;
		Card parentCard = card;
		boolean isMainDocRight = false;
		boolean isParentCardRight = false;
		if( card.getId() == null ) {
			logger.warn( "Current card has no card_id yet! just created?? Check parent from CreateCard action" );
			parentCard = getMainDocCardFromCreateCardAction();
			isParentCardRight = !templateEquals(parentCard, card);
			if (isParentCardRight) {
				isMainDocRight = chkDocMain_direct(parentCard);
			}
		}

		if (!isParentCardRight) {
			List<Card> linkedCardsList = new ArrayList<Card>();
			isMainDocRight = chkDocMain(parentCard, linkedCardsList);
			if (!linkedCardsList.isEmpty()) {
				parentCard = linkedCardsList.get(0);
			}
		}

		if (logger.isInfoEnabled() && !isMainDocRight) {
			logger.info( "Conditions on main doc " + parentCard + " are not satisfied");
		}

		if (isMainDocRight) {
			c_parent = parentCard;
		}
		return c_parent;
	}
	
	/**
	 * ��������� �������� �������� �� ���������� ����.
	 * 
	 * @param parentCard - ������� (������������) ��������
	 * @param attrList - ���� (������ ����������)
	 * @return List<ObjectId> ������ �������� �������� 
	 * @throws DataException
	 */
	protected List<ObjectId> calculateChildren(Card parentCard, LinkedList<ObjectId> attrList) throws DataException {
    	// ��� �������� ��������� ����
    	final List<ObjectId> cardsEndLayer = new ArrayList<ObjectId>(10);
    	cardsEndLayer.add(parentCard.getId());
		for (ObjectId objLayer : attrList) {
			List<ObjectId> cardsNextLayer = new ArrayList<ObjectId>(10);
			for (ObjectId aCardsEndLayer : cardsEndLayer) {
				Card currentCard = fetchCard(aCardsEndLayer);
				Attribute attr = currentCard.getAttributeById(objLayer);
				if (attr instanceof CardLinkAttribute) {
					CardLinkAttribute linkLayer = (CardLinkAttribute) attr;
					cardsNextLayer.addAll(linkLayer.getIdsLinked());
				}
			}
			cardsEndLayer.clear();
			cardsEndLayer.addAll(cardsNextLayer);
			cardsNextLayer.clear();
		}
    	return cardsEndLayer;
    }

	private static boolean templateEquals(Card card, Card anotherCard) {
		if (card == null || anotherCard == null) {
			return false;
		}
		if (card.getTemplate() == null || anotherCard.getTemplate() == null) {
			return false;
		}
		return card.getTemplate().equals(anotherCard.getTemplate());
	}
}