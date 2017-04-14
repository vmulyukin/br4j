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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;

public class CopyPersonFromPaths extends AbstractCopyPersonProcessor {

	private static final long serialVersionUID = 1L;

	/**
	 * �������� ���������� (������������) - ������������� �������������� ������ ��������,
	 * � ������� ���������� ���� ��������� �������������
	 * ������: code
	 * code - attribute_code �������� ��� ��� ������� �� objectid.properties
	 * �������� ������ ������� ���� U
	 */
	public static final String PARAM_TARGET_ATTR = "targetAttr";

	/**
	 * boolean-��������: true=(��-���������) �������� ������� ������ � �������� 
	 * targetAttr, false=��������� ������� ������.
	 */
	public static final String PARAM_REPLACE_TARGET_LIST = "replaceTargetAttrList";
	
	/**
	 * �������� ���������� (������������) - ���� � ��������� (� �������������� 
	 * � ���� ���������), �� ������� ����� �������������.
	 * ������: paths
	 * ���
	 * 		paths ::= path(;path)*
	 * 		path ::= attr(->attr)*
	 * 		attr ::= type:code
	 * (��� () ���������� �������� ������� ������� � ���� � ������ �� ������,
	 * � * ���������, ��� ������� ��� �������� � ������ (����������� � ()) ����� ���������� 0 � ����� ���)
	 * type - ��� ��������������. ��������� �������������� ���� C � U, � �������������� ���������� 
	 * �������� ��� ��� - link � user.
	 * code - attribute_code �������� ��� ��� ������� �� objectid.properties
	 * ������:
	 * 		targetAttr="
	 * 			user:JBR_INFD_EXECUTOR->link:JBR_PERS_DEPT_LINK->user:JBR_DEPT_RESP_DOW;
	 * 			..."
	 *  ��������� ��������� � ������ �� ����� ������ ���� ���� U, �.�. �� ���� ������� ������� ��� targetAttr
	 *  ���� � ���� ����������� ������� ���� U (�� ���������), �� �� ��� �������� ����� �������
	 *  �������� �������(�������) ������� ������������� ��������� ����� ��������. � ������ ��������
	 *  ���� ���� � ���� ��������.
	 *  ��������� �������� �� ���� ���������� � ������, �.�. ������ ������� � ���� ��� ������� � ������ ��������
	 *  UPD: 01.03.2011 O.E. - ������ � �������� ���������� �������� ����� ������������
	 *  � C, ���� ��� - ������ �� �������� ���������� �������.
	 */
	public static final String PARAM_PATHS = "paths";

	/**
	 * true, ����� ��������� ������ �������� �� �� (� �� ������������ ��������
	 * �� Action) - ��� ������ ����-�����������, 
	 * ��-��������� = false (������ ��� ���-�����������). 
	 */
	protected static final String PARAM_RELOAD_CARD = "reloadCard";

	/**
	 * true = ��������� ���������� ������ �� ����� �� (����� ��������� ��� ���-�����������),
	 * ��-��������� true.
	 */
	protected static final String PARAM_UPDATE_DB = "updateDB";

	protected Card originCard;
	protected ObjectId targetAttrId;
	protected String[] targetPathsStr;
	protected List<LinkedList<ObjectId>> paths;

	List<Card> walkPath( LinkedList<ObjectId> path, Card target) throws DataException {
		final LinkedList<Card> sources = new LinkedList<Card>(); // ������� ����� ��������
		sources.add(target); // �������� � ������� ��������... 

		// ������� ���� �����...
		for( ObjectId attrId : path) {
			final long attrStart_ms = System.currentTimeMillis();

			final Set<ObjectId> nextCardIds = new  HashSet<ObjectId>();
			final Set<Card> nextCards = new HashSet<Card>(10);

			// ���������� ������ ���� � ���� (� ������� ������� ��������) ...
			final long levelScanStart_ms = System.currentTimeMillis();
			while (sources.size() > 0) {
				final Card card = sources.removeLast();

				final Attribute attr = card.getAttributeById(attrId);
				if (attr == null) {
					logger.warn( MessageFormat.format( MSG_CARD_0_HAS_NO_ATTRIBUTE_1, 
							new Object[] { card, attrId} ));
					continue;
				}

				final long linksLoadStart_ms = System.currentTimeMillis();
				String linkKind = "link";
				if (BackLinkAttribute.class.isAssignableFrom(attr.getClass())) {
					linkKind = "backlink";
					final List<Card> list = loadAllBackLinks(card.getId(), attrId, getSystemUser());
					if (list != null) 
						nextCards.addAll(list);
				} else { // CardLink/Person/TypedLink
					final Set<ObjectId> cardIds = super.getCardIdsList( attr);
					if (cardIds != null)
						nextCardIds.addAll(cardIds);
				}
				logTime( "\t\t\t\t\t "+ linkKind + " loading ", linksLoadStart_ms);
			}
			logTime( "\t\t\t\t level cards scanning total", levelScanStart_ms);

			// ��������� ��������, ���������� �� ������ ���� ...
			for( ObjectId idCard : nextCardIds) {
				final long loadStart_ms = System.currentTimeMillis();
				sources.add(loadCardById(idCard));
				logTime( "\t\t\t\t load card "+ idCard, loadStart_ms);
			}
			sources.addAll(nextCards);

			logTime( "\t\t\t getting pathstep for attr '"+ attrId + "' in total ", attrStart_ms);
		}
		return sources;
	}

	@SuppressWarnings({ "cast", "unchecked" })
	@Override
	public Object process() throws DataException {

		final long timeStart_ms = System.currentTimeMillis();

		final ObjectId cardId = getCardId();

		Card target = null;
		target = getCard(cardId);

		logTime( "loading card "+ cardId, timeStart_ms);

		if (target == null) {
			logger.warn("There is no card for processing (targetCard is null)");
			return null;
		}

		final PersonAttribute targetAttrPerson = (PersonAttribute)target.getAttributeById(targetAttrId);
		if (targetAttrPerson == null) {
			logger.warn( MessageFormat.format( MSG_CARD_0_HAS_NO_ATTRIBUTE_1, 
					new Object[] { target.getId(), targetAttrId} ));
			return null;
		}

		/* ���� ���� person-��������� �� ���� �����... */
		final long processPathsStart_ms = System.currentTimeMillis();
		final Set<Person> values = new HashSet<Person>();
		for (int i=0; i < paths.size(); i++) {
			final long pathStart_ms = System.currentTimeMillis();

			final LinkedList<ObjectId> path = paths.get(i);

			// ���������� ��������� � ������� ������� � ������� ���� -
			// ��� �������� �������, �� �������� ������ �� ����������� ...
			final ObjectId sourceAttr = path.removeLast();

			List<Card> finalCards = walkPath(path, target);

			// ��� ������ �������� �� sources �������� ������ (ObjectId)
			final long postStart_ms = System.currentTimeMillis();
			for(Card source : finalCards) {
				Set<Person> temp = getPersonsList(source, sourceAttr, true);
				if(temp != null) values.addAll(temp);
				/*final PersonAttribute personAttr = (PersonAttribute) source.getAttributeById(sourceAttr);
				if (personAttr != null) {
					if (personAttr.getValues() != null) {
						values.addAll( (Collection<Person>) personAttr.getValues());
					}
				} else
					logger.warn( MessageFormat.format( MSG_CARD_0_HAS_NO_ATTRIBUTE_1, 
							new Object[] { source.getId(), sourceAttr} ));*/
			}
			finalCards.clear();
			logTime( "\t\t\t post phase ", postStart_ms);

			logTime( "\t\t processing path ["+ String.valueOf(i) + "] '"
					+ this.targetPathsStr[i] + "' in total ", pathStart_ms);
		} // for
		logTime( "\t (*) preparing paths for card "+ cardId, processPathsStart_ms);

		/* ���������� � ������� ��������... */
		final long postTimeStart_ms = System.currentTimeMillis();
		if (getBooleanParameter(PARAM_REPLACE_TARGET_LIST, true)) {
			logger.info( "Replacing previous persons list");
		} else { // ������ ������� ������ ...
			final Collection<Person> oldPersons = CardUtils.getAttrPersons(targetAttrPerson);
			if (oldPersons != null) {
				logger.info( "Appending persons to previous list ...");
				values.addAll(oldPersons);
			}
		}
		logger.debug("Card "+ cardId + " destination attribute '"
				+ targetAttrPerson.getId() + "' will get persons ["
				+ values + "]"
			);
		targetAttrPerson.setValues(values);

		/* ���������� ������ � �� ... */
		if (getBooleanParameter(PARAM_UPDATE_DB, true) && (cardId != null) ) {

			logger.trace("Updating DB...");
			/*
			final long dropStart_ms = System.currentTimeMillis();
				CardUtils.dropAttributes( getJdbcTemplate(), 
					new Object[]{ targetAttrId.getId() },
					cardId);
			logTime( "\t\t dropping attribute "+ targetAttrId.getId()+" for card "+ cardId, dropStart_ms);
			 */

			if (values.isEmpty()) {
				logger.info( "DB insert: nothing to insert for attribute(s) '"
						+ targetAttrId 
						+ "' for card "+ cardId.getId() );
			} else {
				insertCardPersonAttributeValues( cardId, targetAttrId, values, true);
				markChangeDate(cardId);
				updateDateAttrInCard(target, Attribute.ID_CHANGE_DATE);
			}
		} else 
			logger.info("DB updates configured to be skipped (attributes '"+ targetAttrId 
					+ "' of card "+ cardId + ")" );

		logTime( "\t post process for card "+ cardId+ " total ", postTimeStart_ms);
		logTime( "* Job for card "+ cardId, timeStart_ms);
		return null;
	}

	protected Card getCard(ObjectId cardId) throws DataException {
		final boolean argReload = super.getBooleanParameter(PARAM_RELOAD_CARD, false);
		if (originCard != null) // ��� ���������
			return originCard;
		if (argReload && cardId != null) {
			logger.debug("Card reloaded by id "+ cardId);
			originCard = super.loadCardById( cardId);
		} else {
			logger.debug("Active card is used");
			originCard = getCard();
		}
		return originCard;
	}

	@Override
	public void setParameter(String name, String value) {
		if(PARAM_TARGET_ATTR.equals(name)) {
			targetAttrId = makeObjectId(value, PersonAttribute.class);
		} else if (PARAM_PATHS.equals(name)) {
			this.targetPathsStr = value.split("\\s*[;,]\\s*");
			this.paths = new ArrayList<LinkedList<ObjectId>>(targetPathsStr.length);
			for (int i=0; i < targetPathsStr.length; i++) {
				final String[] attrs = targetPathsStr[i].split("->");
				final LinkedList<ObjectId> path = new LinkedList<ObjectId>();
				for (int j=0; j < attrs.length; j++) {
					path.add( makeObjectId(attrs[j], CardLinkAttribute.class) );
				}
				paths.add(path);
			}
		}
		super.setParameter(name, value);
	}

	protected ObjectId makeObjectId(String typeCode, Class<?> defType) {
		Class<?> type;
		String code;
		final String[] desc = typeCode.trim().split(":");
		if (desc.length == 1) { // ��� ("xxx:") �� ������
			type = defType;
			code = desc[0].trim();
		} else {
			type = AttrUtils.getAttrClass(desc[0].trim());
			code = desc[1].trim();
		}
		return ObjectIdUtils.getObjectId(type, code, false);
	}
}
