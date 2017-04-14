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
package com.aplana.dbmi.module.docflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;

/**
 * ����� ���������� ��������� �������� ��� ��� �������� ������������
 * @author lyakin
 *
 */
public class CreateVisaCard {

	public static final ObjectId VISA_PERIOD = ObjectId.predefined(IntegerAttribute.class, "jbr.visa.period");
    public static final ObjectId VISA_NUMBER = ObjectId.predefined(IntegerAttribute.class, "jbr.visa.number");
    public static final ObjectId VISA_PERSON = ObjectId.predefined(PersonAttribute.class, "jbr.visa.person");
    public static final ObjectId VISA_TEMPLATE = ObjectId.predefined(Template.class, "jbr.visa");
    public static final ObjectId VISA_FOR_TASK_TEMPLATE = ObjectId.predefined(Template.class, "jbr.taskVisa");
    public static final ObjectId TASK_TEMPLATE = ObjectId.predefined(Template.class, "jbr.resolution");
    public static final ObjectId STAGE_MEMBER = ObjectId.predefined(CardLinkAttribute.class, "jbr.stage.stagemember");
    public static final ObjectId STAGE = ObjectId.predefined(CardLinkAttribute.class, "jbr.negotiationStages");
    public static final ObjectId INNER_PERSON_TEMPLATE = ObjectId.predefined(Template.class, "jbr.internalPerson");
    public static final ObjectId POSITION_TEMPLATE = ObjectId.predefined(Template.class, "jbr.position");
    public static final ObjectId DEPARTMENT_TEMPLATE = ObjectId.predefined(Template.class, "jbr.department");
    public static final ObjectId ORGANIZATION_TEMPLATE = ObjectId.predefined(Template.class, "jbr.organization");
    public static final ObjectId STAGE_ORDER = ObjectId.predefined(IntegerAttribute.class, "jbr.negotiationorder");
    public static final ObjectId STAGE_PERIOD = ObjectId.predefined(IntegerAttribute.class, "jbr.negotiationperiod");
    public static final ObjectId DEPARTMENT_NEGOTIATION_RESPONSIBLE = ObjectId.predefined(PersonAttribute.class, "jbr.department.responsibleForNegotiation");
    public static final ObjectId ORGANIZATION_BOSS = ObjectId.predefined(CardLinkAttribute.class, "jbr.organization.link.chief");
    public static final ObjectId DEPARTMENT_BOSS = ObjectId.predefined(CardLinkAttribute.class, "jbr.department.chief");
    public static final ObjectId STAGE_TYPE = ObjectId.predefined(ListAttribute.class, "jbr.stage.type");

    public static final ObjectId STAGE_TYPE_POSITION = ObjectId.predefined(ReferenceValue.class, "jbr.stage.type.Position");
    public static final ObjectId STAGE_TYPE_CURATOR = ObjectId.predefined(ReferenceValue.class, "jbr.stage.type.Curator");
    public static final ObjectId STAGE_TYPE_MULTY_PERSON = ObjectId.predefined(ReferenceValue.class, "jbr.stage.type.MultyPerson");
    public static final ObjectId STAGE_TYPE_PERSON = ObjectId.predefined(ReferenceValue.class, "jbr.stage.type.Person");
    public static final ObjectId STAGE_TYPE_DEPARTMENT = ObjectId.predefined(ReferenceValue.class, "jbr.stage.type.Department");
    public static final ObjectId STAGE_TYPE_ORGANIZATION = ObjectId.predefined(ReferenceValue.class, "jbr.stage.type.Organization");

    public static final ObjectId DOCUMENT_AUTHOR = ObjectId.predefined(PersonAttribute.class, "author");

    private ObjectId templateId;
    /**
     * ��������� �������� ����, ��������� �� �������� ��������
     * @param negotiationTemplateId
     * @param f
     * @param d
     * @param user
     * @param docId
     * @return
     * @throws DataException
     */
	@SuppressWarnings("unchecked")
	public ArrayList<Card> getCards(ObjectId negotiationTemplateId,QueryFactory f,Database d,UserData user,ObjectId docId) throws DataException{

		templateId = negotiationTemplateId;
		ArrayList<Card> cards = new ArrayList<Card>();

		ObjectQueryBase q = f.getFetchQuery(Card.class);
		ObjectQueryBase qPerson = f.getFetchQuery(Person.class);
		q.setId(templateId);
		Card negotiationCard = (Card)d.executeQuery(user, q);

  		CardLinkAttribute linkStage = (CardLinkAttribute) negotiationCard.getAttributeById(STAGE);
  		if (linkStage != null) {
  			Iterator<ObjectId> iterStage = linkStage.getIdsLinked().iterator();
  			//���� �� ������ ��������
  			while (iterStage.hasNext()) {
   				ObjectId stageId = iterStage.next();
   				q.setId(stageId);

   				//�������� ����
   				Card stageCard = (Card)d.executeQuery(user, q);
  				int order = ((IntegerAttribute) stageCard.getAttributeById(STAGE_ORDER)).getValue();
  				int period = ((IntegerAttribute) stageCard.getAttributeById(STAGE_PERIOD)).getValue();
  				ListAttribute  stageTypeLA = (ListAttribute) stageCard.getAttributeById(STAGE_TYPE);
  				if (stageTypeLA.getValue() != null){
	  				ObjectId stageType = stageTypeLA.getValue().getId();

					ArrayList<Person> persons = new ArrayList<Person>();
					q.setId(docId);
                    Card document = (Card)d.executeQuery(user, q);

	  				if (stageType != null){
	  					//��� �������� ������������
	  					/**��������� ���������� �������� ��� ��������� ����� (BR4J00039096)
	  					 * PPanichev 02.03.2015
	  					 */
	  					/*if (stageType.equals(STAGE_TYPE_MULTY_PERSON)){
	  						//������� �������� ���� c ������ ��������.
	  						//��� ���� ���� ��� ���� ������� ���� �� �� ��������� � ��� ������������
		   	            	persons.add(new Person());
	  		   				createCard=true;
	  					}
	  					//��� ��������
	  					else*/ if (stageType.equals(STAGE_TYPE_CURATOR)){
	  						//�������� �������� ��� �������� ���������� ����� �� ������� ������������� ��� ������ �������� ���������
	  		   				//� ��������� �������� ������
	  		   				Person docAuthor = ((PersonAttribute)document.getAttributeById(DOCUMENT_AUTHOR)).getPerson();

	  		   				//���������� GetPersonCurator ��� ��������� ��������
		   					ActionQueryBase aq = f.getActionQuery(GetPersonCurator.class);
		   					GetPersonCurator getCurator = new GetPersonCurator(docAuthor);
	  		   				aq = f.getActionQuery(getCurator);
	  		   				aq.setAction(getCurator);
	  		   				Person curatorPerson = (Person)d.executeQuery(user, aq);
	  		   				if (curatorPerson != null){
	  		   					persons.add(curatorPerson);
	  		   				}
	  					}else{
		  					//��� ��������� �����
			   				CardLinkAttribute stageMember  = (CardLinkAttribute) stageCard.getAttributeById(STAGE_MEMBER);
			  				Iterator<ObjectId> iterStaff = stageMember.getIdsLinked().iterator();
			  				if (iterStaff != null) {

			  					//���� �� ���������� �����
			  					while (iterStaff.hasNext()) {
			  						ArrayList<ObjectId> internalPersons = new ArrayList<ObjectId>();
			  						ObjectId stageMemberId = iterStaff.next();
			  		   				q.setId(stageMemberId);

			  		   				//�������� ��������� �����
			  		   				Card stageMemberCard = (Card)d.executeQuery(user, q);
			  		   				ObjectId templateId = stageMemberCard.getTemplate();
			  		   				//����������� ������� ����������
			  		   				if (templateId.equals(INNER_PERSON_TEMPLATE) && (stageType.equals(STAGE_TYPE_PERSON) || stageType.equals(STAGE_TYPE_MULTY_PERSON))){
				  		   	            internalPersons.add(stageMemberCard.getId());
				  		   	            Set<Person> personList = CardUtils.getPersonsByCards(internalPersons,f, d, user);
				  		   	            //��������� ���������� ������ � ���������
				  		   	            persons.addAll(personList);
			  		   				}
			  		   				//����������� ���������
			  		   				else if (templateId.equals(POSITION_TEMPLATE) && stageType.equals(STAGE_TYPE_POSITION)){

			  		   					ActionQueryBase aq = f.getActionQuery(GetPositionPersons.class);
				  		   				GetPositionPersons posPersons = new GetPositionPersons();
				  		   				posPersons.setPositionId(stageMemberCard.getId());
				  		   				aq = f.getActionQuery(posPersons);
				  		   				aq.setAction(posPersons);
				  		   				ObjectId[] personsIdArray = (ObjectId[])d.executeQuery(user, aq);
				  		   				if (personsIdArray!=null){
					  		   				//���������� ������� � ��������� � ���������
					  		   				for (int i=0; i<personsIdArray.length; i++){
					  		   					qPerson.setId(personsIdArray[i]);
					  		   					Person person = (Person)d.executeQuery(user, qPerson);
					  		   					persons.add(person);
					  		   				}
				  		   				}
			  		   				}
			  		   				//����������� �������������
			  		   				//����������� �������������� �� ������������
			  		   				else if (templateId.equals(DEPARTMENT_TEMPLATE) && stageType.equals(STAGE_TYPE_DEPARTMENT)){
			  		   					PersonAttribute attr = (PersonAttribute)
			  		   						stageMemberCard.getAttributeById(DEPARTMENT_NEGOTIATION_RESPONSIBLE);
			  		   					if (attr != null && attr.getValues() != null && attr.getValues().size() > 0){
			  		   						persons.addAll(attr.getValues());
			  		   					}
			  		   					//� ���� ��� ���, - ������������ �������������
			  		   					else
			  		   					{
			  		   						CardLinkAttribute cardLinkAttr = (CardLinkAttribute)
			  		   						stageMemberCard.getAttributeById(DEPARTMENT_BOSS);
			  		   						if (cardLinkAttr != null && !cardLinkAttr.isEmpty()) {

			  		   							Set<Person> departmentChiefList = CardUtils.getPersonsByCards(Collections.singletonList(cardLinkAttr.getFirstIdLinked()), f, d, user);
			  		   							persons.addAll(departmentChiefList);
			  		   						}
			  		   						//���� � ������������ ���, ����������� ����������.
		  		   							if (persons.size() < 1)
		  		   								throw new DataException
		  		   								(
		  		   									"docflow.visa.department.noDefaultApprover",
		  		   									new Object[]{((StringAttribute)stageMemberCard.getAttributeById(Attribute.ID_NAME)).getValue()}
		  		   								);
			  		   					}
			  		   				}
			  		   				//����������� �����������
			  		   				//����������� ������������
			  		   				else if (templateId.equals(ORGANIZATION_TEMPLATE) && stageType.equals(STAGE_TYPE_ORGANIZATION)){
			  		   					CardLinkAttribute bossCardIdAttr = stageMemberCard.getAttributeById(ORGANIZATION_BOSS);
			  		   					if (bossCardIdAttr != null && !bossCardIdAttr.isEmpty()) {

			  		   					    Set<Person> boss = CardUtils.getPersonsByCards(Collections.singletonList(bossCardIdAttr.getFirstIdLinked()), f, d, user);
			  		   					    persons.addAll(boss);
			  		   					}
			  		   				}
			  					}
			  				}
		  				}
	  				}
	   				if (!persons.isEmpty()){
  		   				ActionQueryBase aqCreateCard = f.getActionQuery(CreateCard.class);
  		   				for (Person visaPerson : persons) {
	  		   				CreateCard createAction = new CreateCard();
	  		   				if (TASK_TEMPLATE.equals(document.getTemplate())){
	  		   				    createAction.setTemplate(VISA_FOR_TASK_TEMPLATE);
	  		   				} else {
	  		   				    createAction.setTemplate(VISA_TEMPLATE);
	  		   				}
							aqCreateCard.setAction(createAction);
							Card visaCardCreate = (Card)d.executeQuery(user, aqCreateCard);
							((IntegerAttribute) visaCardCreate.getAttributeById(VISA_NUMBER)).setValue(order);
							((IntegerAttribute) visaCardCreate.getAttributeById(VISA_PERIOD)).setValue(period);

							//��� �������� ������������ �� ��������� �������
							if (visaPerson.getId() != null){
								((PersonAttribute) visaCardCreate.getAttributeById(VISA_PERSON)).setValues(Collections.singletonList(visaPerson));
							}
							cards.add(visaCardCreate);
  		   				}
	   				}
	  			}
  			}
  		}
  		/* ���� ����������� � ������ ����� �������� ���� � ��� �� �������,
		 * ������� ����� ������ ���� (22.11.2010 - �. �.)
		 */
  		for (int i = 0; i < cards.size() - 1; i++) {
			Card iCard = cards.get(i);
			if(((PersonAttribute)(iCard.getAttributeById(VISA_PERSON))).getValues() == null) {
				continue;
			}
			long iPid = (Long)((Person)((PersonAttribute)(iCard.getAttributeById(VISA_PERSON))).getValues().iterator().next()).getId().getId();
			int iOrder = ((IntegerAttribute) iCard.getAttributeById(VISA_NUMBER)).getValue();
			for (int j = i + 1; j < cards.size(); j++) {
				Card jCard = cards.get(j);
				if(((PersonAttribute)(jCard.getAttributeById(VISA_PERSON))).getValues() == null) {
					continue;
				}
				long jPid = (Long)((Person)((PersonAttribute)(jCard.getAttributeById(VISA_PERSON))).getValues().iterator().next()).getId().getId();
				if(iPid == jPid) {
					int jOrder = ((IntegerAttribute) jCard.getAttributeById(VISA_NUMBER)).getValue();
					if (jOrder < iOrder) {
						cards.remove(j--);
					} else {
						cards.remove(i--); 
						break;
					}
				}
			}
  		}
		/*---------------------------------------------------------------------------------------*/
  		return cards;
	}
}

