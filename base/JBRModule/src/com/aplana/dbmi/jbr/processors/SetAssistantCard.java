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
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.aplana.dbmi.jbr.action.GetAssistants;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.utils.StrUtils;

/**
 * @comment RAbdullin
 * ����:  ���������� ���� � ����������� � ������� ��� ��������� ��������� 
 * (��������, ������ �����������).
 * �������������� ������ ��������� � �������� assistantAttrId � �� ����� �������� 
 * � ������� ��������������� ��������� "forceSave" (false, true) - ������������
 * ������ ���� �� ������ destLinkAttrId, ��� �������� destLinkAttrId != null 
 * ���������� ������ ����������� �����!
 * ���������:
 * 		srcChiefAttrId: id- (���������) �������� ������ ������ ���� U ��� C/E 
 * � ������� ��������, ��� ������� ���� ������� ���������� ��� ������ � 
 * assistantsAttrId. ����� ���:
 * 		{+} id {!������_�����}
 * ��� ���� � ������: (����) �������� true ����� addChiefToo;
 *   id: ����� �� objectids ��� ���� �������� ��� ��������;
 *   {!������_�����}: (����) ������ ����������� ����� (������������� ����� ��� 
 *   ��� ����� � �������), ���� �� ����� �� ������� ������ �������� ��� �� 
 * �������� <id>, ����� ��� �����������; ���� ������ ����� ���� (�� �����), �� 
 * ��������������� ��� "����".
 * 		(�����) srcChiefRoles: (����, default=������) ������ ����������� �����, ���� ��
 * ����� �� ������� ������ �������� ��� �� srcChiefAttrId, ����� ��� �����������; 
 * ���� ������ ����� ���� (�� �����), �� ��������������� ��� "����". 
 * 		assistantsAttrId: PersonAttribute ID - ������� User-������� ��� ���������
 * ��������������� ������.
 * 		forceSave: boolean (����, default=false) - ���� �� ��������� ���������� 
 * �������� ����� ����� �� (������������ ������ ��� destLinkAttrId == null).
 * 		destLinkAttrId: ���� ������, �� ����� ���� � ��������, ��������� � 
 * ���� ��������-������ ������� �������� (� �� ������� assistantsAttrId), ����� 
 * (���� null), �� ����� ���� � ������� �������� (� ������������ forceSave).
 * 		assistantsPreclear: (����, ����=false) true=�������� ���������� ������ 
 * ����������� �������� (��������, ����� ���� ����������� ��� ������ ����
 * ��������� � ��� ��������� ������ ����), false= �������� � ���� (���� ������� 
 * ��-���������, ����� ����� ���� ����� ���������� �� ���������� �����).
 */
public class SetAssistantCard extends AbstractCopyPersonProcessor 
	// implements Parametrized 
{
	private static final long serialVersionUID = 1L;

//	private ObjectId srcChiefAttrId;
//	private List<ObjectId> srcChiefRoles; // ���� �� ���������� (role1 "OR" role2 ...)
	// �������� �������� "�����" ��� ��������� �� ����������...
	private List<SrcPersonAttrDesc> srcChiefList = new ArrayList<SrcPersonAttrDesc>();

	private ObjectId assistantsAttrId;
	private boolean forceSave = false;
	private boolean assistantsPreclear = false;

	// ������� � ������� ��� ��������� ������ ���������;
	// ���� �� ������ - ������������ �������;
	private ObjectId destLinkAttrId;


	@Override
	public Object process() throws DataException {

		final Card card = super.getCard(); // (Card)getObject();
		if (card == null) {
			logger.warn( "No active card -> exiting");
			return null;
		}

		final Set<ObjectId> personIds = collectAssistentsAndChiefs(card);
		logger.debug( "collected persons counter: " + personIds.size() 
				+ " for card "+ card.getId() + "\n\t , attribute(s) "+ srcChiefList);

		// ���������� �����������...
		final boolean saveIntoCurrentCard = (this.destLinkAttrId == null);
		if (saveIntoCurrentCard) {
			logger.debug( "Saving into active card id=" + card.getId());
			putIntoCurrent( card, personIds);
		} else {
			logger.debug( "Saving into linked cards for card " + card.getId());
			putIntoLinked( card, this.destLinkAttrId, personIds);
		}

		return null;
	}


	/**
	 * �� �������� �������� ������������ ������ ������ (�� ������������������
	 * ��������� this.srcChiefList). 
	 * @param card
	 * @throws DataException
	 */
	@SuppressWarnings("unchecked")
	private Set<ObjectId> collectAssistentsAndChiefs( final Card card) 
		throws DataException 
	{
		final Set<ObjectId> resultPersonIds = new HashSet<ObjectId>();
		if(this.srcChiefList != null) {

			// ������ ��-�����, ��� ������� ���� ���� ���������� � �� ������ ���� ...
			// (����� ������� ����� "�����")
			final Set<ObjectId> allChiefsForAssist = new HashSet<ObjectId>(10);

			for (SrcPersonAttrDesc chiefDesc : this.srcChiefList) 
			{
				final Collection<Person> chiefs = getPersonsList( card, chiefDesc.getChiefAttrId(), true);
				if (chiefs == null || chiefs.isEmpty())
					continue;
				final Set<ObjectId> chiefIds = ObjectIdUtils.collectionToSetOfIds(chiefs);

				if (chiefDesc.isAddChief()) {
					// ���������� ������ ���� ...
					resultPersonIds.addAll( chiefIds);
					logger.debug( chiefIds.size()+ " chief persons added of attribute "+ chiefDesc.getChiefAttrId());
				}

				// ��������� ������ ����������� (� ���� ������)...
				if (chiefDesc.isAddAssist()) {
					if (chiefDesc.getChiefRoles() == null || chiefDesc.getChiefRoles().isEmpty()) {
						// ����������, ����� ����� ����� ���� ��������� ...
						allChiefsForAssist.addAll( chiefIds);
					} else
						loadAssistents(resultPersonIds, chiefIds, chiefDesc.getChiefRoles());
				} else
					logger.debug( "no assistents demand for attribute "+ chiefDesc.getChiefAttrId());
			}

			// ����� ������ ���� ����������� ... 
			if (!allChiefsForAssist.isEmpty()) {
				loadAssistents( resultPersonIds, allChiefsForAssist, null);
			}
		}
		return resultPersonIds;
	}

	private void loadAssistents( Set<ObjectId> destPersonIds, 
			final Set<ObjectId> chiefIds, Collection<ObjectId> chiefRoles
			) throws DataException 
	{
		final List<ObjectId> assistents = getAssistants( chiefIds, chiefRoles);
		final String sRoles = (chiefRoles == null || chiefRoles.isEmpty()) 
				? "any" 
				: IdUtils.makeIdCodesEnum(chiefRoles, ", ");
		if (assistents != null && !assistents.isEmpty()) {
			// for( ObjectId id : assistents ) persons.add( DataObject.createFromId(id));
			destPersonIds.addAll(assistents);
			logger.debug( assistents.size()+ " assistents found for chief persons of attribute(s) "+ chiefIds+ ", with roles "+ sRoles);
		} else
			logger.debug( "no assistents found for chief persons of attribute(s) "+ chiefIds+ ", with roles "+ sRoles);
	}

	/**
	 * @param chiefIds ������ id-������ ������, ��� ������� ���� �������� �����������. 
	 * @return ������ �����������, �������� ����� � ������ chiefPersonAttr.
	 * @throws DataException 
	 */
	private List<ObjectId> getAssistants( Collection<ObjectId> chiefIds, 
				Collection<ObjectId> filterByRoles
		) throws DataException 
	{
		final List<ObjectId> result = new ArrayList<ObjectId>();
		if (!chiefIds.isEmpty()) {
			// ����� action ��� ��������� ������ ����������
			final GetAssistants action = new GetAssistants();
			action.setChiefIds( chiefIds);
			action.setChiefRoleIds(filterByRoles);
			final List<?> assistants = (List<?>) super.execAction(action, getSystemUser());
			if (assistants != null) {
				for (Object obj : assistants) {
					result.add( ObjectIdUtils.getIdFrom(obj));
				}
			}
		}
		return result;
	}


	/**
	 * �������� ������ ������ � �������� destCard � ������� this.assistantsAttrId. 
	 */
	private void putIntoCurrent(Card destCard, final Collection<ObjectId> personIds)
	{
		final PersonAttribute destAttrAssistents = (PersonAttribute)
				destCard.getAttributeById(assistantsAttrId);
		if (destAttrAssistents == null) {
			logger.warn("Assistants attr " + assistantsAttrId+ " not found in card " + destCard.getId() + " -> exiting ");
			return;
		}

		final List<Person> persons = makePersons(personIds); 
		// ���������� �������� ��������...
		if (this.assistantsPreclear) { // ������ ����� ������� ...
			destAttrAssistents.clear();
		} else { // ���������� � �������...
			final Collection<Person> list = CardUtils.getAttrPersons(destAttrAssistents);
			if (persons != null && list != null )
				persons.addAll( list); // getValues = List<Person>
		}
		if (persons != null && !persons.isEmpty())
			destAttrAssistents.setValues(persons);

//		if (this.forceSave || this.assistantsPreclear) {
//			// ���� ����� ������ ��� ������ ��������� (��� ����� ���� �������� 
//			// ������ � ����, ����� ����� ��������� ����������� DoOverwriteCardAttributes)...
//		}

		// ���� ���� - ����� ��������� � ��...
		if (this.forceSave) {
			// CardUtils.dropAttributes( getJdbcTemplate(), new Object[]{assistantsAttrId}, destCard.getId());
			insertCardPersonAttributeValues( destCard.getId(), assistantsAttrId, persons, true);
			logger.trace( "Data saved into card " + destCard.getId());
		}
	}


	/**
	 * @param personIds
	 * @return
	 */
	private List<Person> makePersons(Collection<ObjectId> personIds) {
		if (personIds == null || personIds.isEmpty()) 
			return null;		
		final List<Person> result = new ArrayList<Person>( personIds.size());
		for (ObjectId id: personIds) {
			result.add( (Person) DataObject.createFromId(id));
		}
		return result;
	}


	/**
	 * ���������� ������ ����������� � ������� this.assistantsAttrId �� ��� 
	 * �������� �� ������ linkAttr �������� card.
	 * ���������� ������ � ��.
	 * @param card: �������� ��� ��������� �������� destLinkAttrId.
	 * @param destLinkAttrId: ������� �� ������� ������� ��������.
	 * @param personIds: ������ id ������ ��� ���������� � ������� ��������.
	 * @throws DataException 
	 */
	private void putIntoLinked( Card card, 
				final ObjectId linkedDestCardsAttrId,
				final Collection<ObjectId> personIds
		) throws DataException 
	{
		final Attribute linkAttribute = card.getAttributeById(linkedDestCardsAttrId);
		if (linkAttribute == null) {
			logger.warn("Link attribute " + linkedDestCardsAttrId+ " not found in card " + card.getId() + " -> exiting ");
			return;
		}

		final List<ObjectId> destIds = super.getAllLinkedIdsByAttr(card.getId(), linkAttribute, getSystemUser());
		if (destIds == null || destIds.isEmpty()) {
			logger.warn("Empty list of link attribute " + linkedDestCardsAttrId+ " in card " + card.getId() + " -> exiting ");
			return;
		}

		// ����� ������� ��� ������������ � ������� �������� ������ �� ������� 
		// ���� �� ��, ���� ������ �� ��, ������� ����� ��������� (����� �� 
		// �������������)
		// dropAssistentPersonsData(destIds, personIds); � (2011/08/09, RuSA) �������� ����������� ������ insertCardPersons

		// �� ������ ��������� ������ � ��������� ��������...
		// DONE: smart-save: ������������� ����� preClear �� ����� ������, � �������� �������� �� �����, � �� �� ����� ...
		insertCardsPersonAttributeValues( destIds, assistantsAttrId, personIds, this.assistantsPreclear);
	}


//DONE ��� ����� ����� ����� ����� ���������������� ����� �� AbstractCopyPersonProcessor, �� �����������
// ������ ��������� �� ���������� ���� �� ��� ������. ����� ����� ������� ����� ����� ������ ������� ������
// � ���������� � � AbstractCardProcessor ?	
/*	protected void insertCardPersonAttributeValues(ObjectId cardId, 
			ObjectId personAttributeId, 
			Collection<ObjectId> personIds) 
	{
		logger.debug("Writing changes into database.");
		if ((personIds != null) && !personIds.isEmpty()) {
			int cnt = getJdbcTemplate().update(
				"insert into attribute_value \n" +
				"\t (card_id, attribute_code, number_value) \n" +
				"\t select ?, ?, p.person_id \n" +
				"\t from person p where p.person_id in ( \n" +
				"\t\t " + ObjectIdUtils.numericIdsToCommaDelimitedString(personIds) + "\n" +
				"\t )",
				new Object[] {
					cardId.getId(),
					personAttributeId.getId()
				},
				new int[] {
					Types.NUMERIC,
					Types.VARCHAR
				}
			);
			logger.debug(cnt + " records inserted into card "+ cardId.getId() 
					+ ", attribute '" + personAttributeId + "'");
		} else {
			logger.debug("Nothing to insert.");
		}
	}
*/

	@Override
	public void setParameter(String name, String value) {
		if (name == null || value == null)
			return;

		super.setParameter(name, value);

		name = name.toUpperCase(); 
		if (	name.startsWith( "srcChiefAttrId".toUpperCase())
				|| name.startsWith( "chiefAttrId".toUpperCase()) ) 
		{
			// this.srcChiefAttrId = ObjectIdUtils.getObjectId(PersonAttribute.class, value, false);
			final SrcPersonAttrDesc desc = SrcPersonAttrDesc.initDesc(value, logger);
			if (desc == null) {
				logger.warn("Empty desc skipped at paramter '"+ name + "'='"+value+"'");
			} else
				this.srcChiefList.add(desc);
		} else if ("assistantsPreclear".equalsIgnoreCase(name)) {
			this.assistantsPreclear = StrUtils.stringToBool( value, this.assistantsPreclear);
		} else if ("assistantsAttrId".equalsIgnoreCase(name)) {
			// this.assistantsAttrId = ObjectIdUtils.getObjectId(PersonAttribute.class, value, false);
			this.assistantsAttrId = IdUtils.smartMakeAttrId( value, PersonAttribute.class);
		} else if ("forceSave".equalsIgnoreCase(name)) {
			if ("true".equalsIgnoreCase(value))
				forceSave = true;
		} else if ("destLinkAttrId".equalsIgnoreCase(name)) {
			// destLinkAttrId = ObjectIdUtils.getObjectId(BackLinkAttribute.class, value, false);
			this.destLinkAttrId = IdUtils.smartMakeAttrId( value, BackLinkAttribute.class);
		} else {
			// logger.warn("Ignored unknown paramter '"+ name + "'='"+value+"'");
			super.setParameter(name, value);
		}
	}


	/**
	 * �����-��������� ��� �������� ������ �� �������� ���� U ��� C/E (���������� 
	 * ��� ������������ ������ ������):
	 * 		1) id ��������;
	 * 		2) ������ ����������� ����������� �����;
	 * 		3) ��������: ��������/��� �������� ������.
	 *  
	 * @author RAbdullin
	 */
	private static class SrcPersonAttrDesc {

		// ������� �� ������� ������ (C ��� U)
		final private ObjectId chiefAttrId;

		// ������ ����������� �����
		final private Set<ObjectId> chiefRoles; 

		// ���� �� ��������� srcChiefAttrId � �������� ������
		final private boolean addChief;
		// ���� �� ��������� ����������� � �������� ������
		final private boolean addAssist;

		/**
		 * @param idChiefAttr
		 * @param chiefRoleIds
		 * @param chiefAdd
		 * @param assistAdd
		 */
		public SrcPersonAttrDesc(ObjectId idChiefAttr,
				Collection<ObjectId> chiefRoleIds, 
				boolean chiefAdd,
				boolean assistAdd) {
			super();
			this.chiefAttrId = idChiefAttr;
			this.chiefRoles = (chiefRoleIds == null) ? null : new HashSet<ObjectId>(chiefRoleIds);
			this.addChief = chiefAdd;
			this.addAssist= assistAdd;
		} 

		/**
		 * ������� ��������� �������� ������ - ���������� ��������.
		 * @param s ������ �������� ����:
		 * 		{[C+|-]}{[A+|-]} id {!������_�����}
		 * ��� ���� �������� � ������ 
		 * 		[C+]: �������� true ����� addChief;
		 * 		[C-]: (�� ����) �������� false ����� addChief;
		 * 		[A+]: (�� ����) �������� true ����� addAssist;
		 * 		[A-]: �������� false ����� addAssist;
		 * ������� �������� C � A, � ����� ���-���� ��������� ������������.
		 * true ����� ������� ���: + | 1 | � | Y. 
		 *   id: ����� �� objectids ��� ���� �������� ��� ��������;
		 *   ���� {!������_�����}: ������ ����������� ����� ����� ����� ����� 
		 *   (������������ ��� ��� ����� � �������).
		 */
		public static SrcPersonAttrDesc initDesc( String s, Log logger)
		{
			if (s == null) return null;
			s = s.trim(); if (s.length() < 1) return null;

			ObjectId chiefAttrId = null;
			List<ObjectId> roles = null;
			boolean flAddChief = false;
			boolean flAddAssist = true;

			// ���� ���������� ���� ��� ����������...
			int i = 0;
			while (s.length() > i && s.charAt(i) == '[') {
				i++;
				final int posEnd = s.indexOf(']', i);
				if (posEnd < 0) break;
				final String data = s.substring(i, posEnd);
				i = posEnd + 1;
				if (data.length() >= 2)
				{
					final boolean flag = "+1Tt��Yy".indexOf(data.charAt(1)) >= 0;
					if ("Cc����".indexOf( data.charAt(0)) >= 0)
						flAddChief = flag;
					else 
						flAddAssist = flag;
				}
			}
			if (i > 0)
				s = (i < s.length()) ? s.substring(i) : "";

			// Id � ���� ...
			final String parts[] = s.split("!");
			chiefAttrId = IdUtils.smartMakeAttrId(parts[0].trim(), PersonAttribute.class);
			if (parts.length > 1) 
				roles = IdUtils.stringToAttrIds( parts[1].trim(), Role.class);
			if (parts.length > 2) {
				if (logger != null)
					logger.warn("too many chars '!' at line '"+ s +"', only first one accepted");
			}
			if (chiefAttrId == null) {
				if (logger != null)
					logger.warn("formatted string '"+ s +"' is ingnored due to absent id");
				return null;
			}
			return new SrcPersonAttrDesc( chiefAttrId, roles, flAddChief, flAddAssist);
		}

		/**
		 * @return the srcChiefAttrId
		 */
		public ObjectId getChiefAttrId() {
			return this.chiefAttrId;
		}

		/**
		 * @return the srcChiefRoles
		 */
		public Set<ObjectId> getChiefRoles() {
			return this.chiefRoles;
		}

		public boolean isAddChief() {
			return this.addChief;
		}

		public boolean isAddAssist() {
			return this.addAssist;
		}

		@Override
		public String toString() {
			return MessageFormat.format( "{0}( {1}{2}chiefAttr={3} {4})", 
					getClass().getName()
					, (this.addAssist ? "+assist," : "")
					, (this.addChief  ? "+chief," : "")
					, this.chiefAttrId
					, this.chiefRoles
				);
		}


		public boolean equals1(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass())
				return false;

			final SrcPersonAttrDesc other = (SrcPersonAttrDesc) obj;
			if ( this.addAssist != other.addAssist
					|| this.addChief != other.addChief
				)
				return false;

			if (this.chiefAttrId == null) {
				if (other.chiefAttrId != null) return false;
			} else if (!this.chiefAttrId.equals(other.chiefAttrId))
					return false;

			if (this.chiefRoles == null) {
				if (other.chiefRoles != null) return false;
			} else if (
						!this.chiefRoles.containsAll(other.chiefRoles)
						|| !other.chiefRoles.containsAll(this.chiefRoles)
				)
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (this.addAssist ? 1231 : 1237);
			result = prime * result + (this.addChief ? 1231 : 1237);
			result = prime
					* result
					+ ((this.chiefAttrId == null) ? 0 : this.chiefAttrId.hashCode());
			result = prime
					* result
					+ ((this.chiefRoles == null) ? 0 : this.chiefRoles.hashCode());
			return result;
		}

	}

}
