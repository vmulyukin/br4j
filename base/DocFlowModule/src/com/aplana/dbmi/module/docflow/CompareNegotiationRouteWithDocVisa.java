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
import java.util.Iterator;
import java.util.Set;

import com.aplana.dbmi.jbr.processors.AbstractCardProcessor;
import com.aplana.dbmi.jbr.util.CardUtils;
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
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;

public class CompareNegotiationRouteWithDocVisa extends AbstractCardProcessor implements Parametrized {
	private static final long serialVersionUID = 1L;
	
	public static final ObjectId TYPICAL_ROUTE = ObjectId.predefined(CardLinkAttribute.class, "jbr.saveNegotiationList");
	public static final ObjectId VISA_LIST = ObjectId.predefined(CardLinkAttribute.class, "jbr.visa.set");
	public static final ObjectId VISA_PERIOD = ObjectId.predefined(IntegerAttribute.class, "jbr.visa.period");
	public static final ObjectId VISA_NUMBER = ObjectId.predefined(IntegerAttribute.class, "jbr.visa.number");
	public static final ObjectId VISA_PERSON = ObjectId.predefined(PersonAttribute.class, "jbr.visa.person");
	public static final ObjectId STAGE_MEMBER = ObjectId.predefined(CardLinkAttribute.class, "jbr.stage.stagemember");
	public static final ObjectId STAGE = ObjectId.predefined(CardLinkAttribute.class, "jbr.negotiationStages");
	public static final ObjectId STAGE_ORDER = ObjectId.predefined(IntegerAttribute.class, "jbr.negotiationorder");
	public static final ObjectId STAGE_PERIOD = ObjectId.predefined(IntegerAttribute.class, "jbr.negotiationperiod");
	public static final ObjectId NEGOTIATION_LIST_ALLOW_CHANGE = ObjectId.predefined(ListAttribute.class, "jbr.negotiationlist.allowChange");
	public static final int NO_VALUE = 1450;
	 
	public static final ObjectId INNER_PERSON_TEMPLATE = ObjectId.predefined(Template.class, "jbr.internalPerson");
	public static final ObjectId POSITION_TEMPLATE = ObjectId.predefined(Template.class, "jbr.position");
    public static final ObjectId DEPARTMENT_TEMPLATE = ObjectId.predefined(Template.class, "jbr.department");
    public static final ObjectId ORGANIZATION_TEMPLATE = ObjectId.predefined(Template.class, "jbr.organization");

    public static final ObjectId DEPARTMENT_NEGOTIATION_RESPONSIBLE = ObjectId.predefined(PersonAttribute.class, "jbr.department.responsibleForNegotiation");    
    public static final ObjectId ORGANIZATION_BOSS = ObjectId.predefined(PersonAttribute.class, "jbr.organization.chief"); 
    public static final ObjectId DEPARTMENT_BOSS = ObjectId.predefined(CardLinkAttribute.class, "jbr.department.chief"); 
    
	public static final ObjectId NAME = ObjectId.predefined(StringAttribute.class, "name");
	public static final ObjectId AUTHOR = ObjectId.predefined(PersonAttribute.class, "author");
	
	public static final ObjectId STAGE_TYPE = ObjectId.predefined(ListAttribute.class, "jbr.stage.type");
	
	public static final ObjectId STAGE_TYPE_POSITION = ObjectId.predefined(ReferenceValue.class, "jbr.stage.type.Position");
	public static final ObjectId STAGE_TYPE_CURATOR = ObjectId.predefined(ReferenceValue.class, "jbr.stage.type.Curator");
	public static final ObjectId STAGE_TYPE_MULTY_PERSON = ObjectId.predefined(ReferenceValue.class, "jbr.stage.type.MultyPerson");
	public static final ObjectId STAGE_TYPE_PERSON = ObjectId.predefined(ReferenceValue.class, "jbr.stage.type.Person");
	public static final ObjectId STAGE_TYPE_DEPARTMENT = ObjectId.predefined(ReferenceValue.class, "jbr.stage.type.Department");
	public static final ObjectId STAGE_TYPE_ORGANIZATION = ObjectId.predefined(ReferenceValue.class, "jbr.stage.type.Organization");
	
	public Object process() throws DataException {	
		final  Database d = getDatabase();
		final  UserData user= getSystemUser();
		final QueryFactory f = getQueryFactory();
		final ObjectQueryBase q = f.getFetchQuery(Card.class);
		ObjectQueryBase qPerson = f.getFetchQuery(Person.class);
		ArrayList<Card> visaCards = new ArrayList<Card>();
		ArrayList<Card> foundVisaCards = new ArrayList<Card>();
		q.setId(getCardId());
		Card doc = (Card)d.executeQuery(user, q);
		CardLinkAttribute linkRoute = (CardLinkAttribute) doc.getAttributeById(TYPICAL_ROUTE);
		CardLinkAttribute linkVISA = (CardLinkAttribute) doc.getAttributeById(VISA_LIST);
		PersonAttribute docAuthor = (PersonAttribute) doc.getAttributeById(AUTHOR);
		
		//�������� �������� ��� ���������
		if (linkVISA != null) {
			
  			Iterator<ObjectId> iterVISA = linkVISA.getIdsLinked().iterator();
  			while (iterVISA.hasNext()) {
  				ObjectId visaId = iterVISA.next();
   				q.setId(visaId);
   				Card visaCard = (Card)d.executeQuery(user, q);
   				visaCards.add(visaCard);	
  			}
		}
		
		//�������� ������������� �������� ������������
		ObjectId routeId = null;
		if (linkRoute != null) {
  			Iterator<ObjectId> iterRoute = linkRoute.getIdsLinked().iterator();
  			while (iterRoute.hasNext()) {
   				routeId = iterRoute.next();
  			}
		}
		
		//���������� �������� ���������� ������ ���� ������� ��� ������
		if (routeId != null){
						
			//�������� �������� �������� �������� ������������
			q.setId(routeId);
			Card negotiationCard = (Card)d.executeQuery(user, q);
			
			//�������� ����� ����� ��� ��� �������� ������� �������
			ListAttribute allowChange = (ListAttribute)negotiationCard.
				getAttributeById(NEGOTIATION_LIST_ALLOW_CHANGE);
			if (allowChange != null && 
					allowChange.getValue() != null && 
					allowChange.getValue().getId().getId().equals(Long.valueOf(NO_VALUE))){
			
				//�������� ����� �������� �������� ������������
				CardLinkAttribute linkStage = (CardLinkAttribute) negotiationCard.getAttributeById(STAGE);
		  		if (linkStage != null) {
		  			Iterator<ObjectId> iterStage = linkStage.getIdsLinked().iterator();
		  			
		  			//���� �� ������ �������� �������� ������������
		  			while (iterStage.hasNext()) {
		  				//���� ���������� �����
		  				boolean stageIsValid = false;
		  				boolean emptyMultyPerson = false;
		  				ObjectId stageId = iterStage.next();
		   				q.setId(stageId);
		   				
		   				//�������� �������� �����
		   				Card stageCard = (Card)d.executeQuery(user, q);
		   				int orderStage = ((IntegerAttribute) stageCard.getAttributeById(STAGE_ORDER)).getValue();
		  				int periodStage = ((IntegerAttribute) stageCard.getAttributeById(STAGE_PERIOD)).getValue();
		  				ObjectId stageTypeId = ((ListAttribute) stageCard.getAttributeById(STAGE_TYPE)).getValue().getId();
		  				String stageName=((StringAttribute) stageCard.getAttributeById(NAME)).getValue();
		  				
		  				//�������� ������� ���������� �����
		   				CardLinkAttribute stageMember  = (CardLinkAttribute) stageCard.getAttributeById(STAGE_MEMBER);
		   				
		   				if (stageTypeId.equals(STAGE_TYPE_MULTY_PERSON)) {
		   				//if (stageMemberCount == 0){
		   					//������������ �������, � ��������� ������ ���� ����������� (���� �� ����) � ������� ����� � ������ ������������ �����
		   					//���� �� ���� ����� � ���������
		   					emptyMultyPerson = true;
		   					Iterator<Card> iterVisaInDoc = visaCards.iterator();
		   					while (iterVisaInDoc.hasNext()) {
		   						//�������� �������� ����
		   						Card cardVisaInDoc= iterVisaInDoc.next();
		   						int orderVisa = ((IntegerAttribute) cardVisaInDoc.getAttributeById(VISA_NUMBER)).getValue();
		   		  				int periodVisa = ((IntegerAttribute) cardVisaInDoc.getAttributeById(VISA_PERIOD)).getValue();
		   		  				
		   		  				if 
		   		  				(
		   		  						orderVisa == orderStage && 
		   		  						periodVisa == periodStage &&
		   		  						((PersonAttribute)(cardVisaInDoc.getAttributeById(VISA_PERSON))).getValues() != null
		   		  				)
		   		  				{
		   		  					//������� ���� �� ���� ���� ��������������� �������� ������������
		   		  					emptyMultyPerson = false;
		   		  					stageIsValid = true;
		   		  					//��������� ���� ��������������� ����� � ����. ���������, ��� ���� ����� � ����������� ���������� ������� ��� �� ��������������� �� ������ �����
		   		  					foundVisaCards.add(cardVisaInDoc);
		   		  				}
						  		if (emptyMultyPerson) throw new DataException("jbr.processor.emptyMultyPersonVisa",
						  				new String[]{stageName});
		   					}
		   					
  		   				}else if(stageTypeId.equals(STAGE_TYPE_CURATOR)){
  		   					//� ������ ���� ��� �������, ���� �������� �� ������� ������������� ������� � ������������� ������ ���������
  		   					Person curator = (Person)execAction(new GetPersonCurator(docAuthor.getPerson()));
  		   					if (curator != null){
  			   					Iterator<Card> iterVisaInDoc = visaCards.iterator();
  			   					while (iterVisaInDoc.hasNext()) {
  			   						//�������� �������� ����
  			   						Card cardVisaInDoc= iterVisaInDoc.next();
  			   						Person visaPerson = ((PersonAttribute) cardVisaInDoc.getAttributeById(VISA_PERSON)).getPerson();
  			   		  				if (visaPerson != null && curator.getCardId().equals(visaPerson.getCardId())){
  			   		  					//������� ���� �� ���� ���� ��������������� ������������ ���������
  			   		  					stageIsValid = true;
  			   		  					//��������� ���� ��������������� ����� � ����. ���������, ��� ���� ����� � ����������� ���������� ������� ��� �� ��������������� �� ������ �����
  			   		  					foundVisaCards.add(cardVisaInDoc);
  			   		  				}
  			   					}	   					
  		   					}else{
  		   						stageIsValid = true;
  		   					}
		   				}else{
		   					//������������ �� �������, � ��������� ������ ���� ����������� ��������������� �������� � ����� 
		   					//� ��������������� ������� � ������
		   					//�������� ���� ������ ��� ������� �����
		   					
		   					ArrayList<Person> persons = new ArrayList<Person>();
		   					Iterator<ObjectId> iterStaff = stageMember.getIdsLinked().iterator();
		   	  				if (iterStaff != null) {
		   	  					//���� �� ���������� ����� � ����������� �������
			   	  				while (iterStaff.hasNext()) {
			   	  					//�������� �������� �����
			  						ObjectId stageMemberId = iterStaff.next();
			  		   				q.setId(stageMemberId);
			  		   				Card stageMemberCard = (Card)d.executeQuery(user, q);
			  		   				
			  		   				//�������� ������ �������� ��������� �����
			  		   				ObjectId templateId = stageMemberCard.getTemplate();
			  		   				if (templateId.equals(INNER_PERSON_TEMPLATE) && stageTypeId.equals(STAGE_TYPE_PERSON)){
			  		   					//� ������ ���� ��� ������� ���������� �� �������� ��������������� �������
			  		   					ArrayList<ObjectId> internalPersons = new ArrayList<ObjectId>();
			  		   					internalPersons.add(stageMemberCard.getId());
				  		   	            Set<Person> personList = CardUtils.getPersonsByCards(internalPersons,f, d, user);
				  		   	            Iterator<Person> personListIterator = personList.iterator();
				  		   	            //���������� ������� � ��������� � �������
				  		   	            while (personListIterator.hasNext()){
				  		   	            	persons.add(personListIterator.next()); 
				  		   	            }  				  		   	            
				  		   			}
				  		   			else if (templateId.equals(POSITION_TEMPLATE) && stageTypeId.equals(STAGE_TYPE_POSITION)){
				  		   				//� ������ ���� ��� ��������� �� �������� ������� � ���� ����������
				  		   				ActionQueryBase aq = f.getActionQuery(GetPositionPersons.class);	
				  		   				GetPositionPersons posPersons = new GetPositionPersons();
				  		   				posPersons.setPositionId(stageMemberCard.getId());
				  		   				aq = f.getActionQuery(posPersons);
				  		   				aq.setAction(posPersons);
				  		   				ObjectId[] personsIdArray = (ObjectId[])d.executeQuery(user, aq);
				  		   				//���������� ������� � ��������� � ���������
				  		   				for(int i=0; i<personsIdArray.length; i++){
				  		   					qPerson.setId(personsIdArray[i]);
				  		   					Person person = (Person)d.executeQuery(user, qPerson); 
				  		   					persons.add(person);
				  		   				}	       	
			  		   				}else if(templateId.equals(DEPARTMENT_TEMPLATE) && stageTypeId.equals(STAGE_TYPE_DEPARTMENT)){
			  		   					//� ������ ���� ��� ������������� �������� �������������� �� ������������
			  		   					PersonAttribute depResponsible = (PersonAttribute)stageMemberCard.getAttributeById(DEPARTMENT_NEGOTIATION_RESPONSIBLE);
			  		   					if (depResponsible != null && depResponsible.getValues().iterator().hasNext()){
			  		   						persons.add(depResponsible.getPerson());
			  		   					}
			  		   					//� ���� ��� ���, - ������������ �������������
			  		   					else 
			  		   					{
			  		   						CardLinkAttribute depChiefCard = (CardLinkAttribute)stageMemberCard.getAttributeById(DEPARTMENT_BOSS);
			  		   						if (depChiefCard != null)
			  		   						{
			  		   							ArrayList<ObjectId> personCard = new ArrayList<ObjectId>();
			  		   							personCard.add(depChiefCard.getIdsArray()[0]);
			  		   							Set<Person> departmentChiefList = CardUtils.getPersonsByCards(personCard, f, d, user);
			  		   							Iterator<Person> personIdsIterator = departmentChiefList.iterator();  		   						
			  		   							while (personIdsIterator.hasNext())
			  		   							{
			  		   								Person person = personIdsIterator.next(); 
			  		   								persons.add(person);
			  		   							}
			  		   						}
			  		   					}
			  		   				}else if(templateId.equals(ORGANIZATION_TEMPLATE) && stageTypeId.equals(STAGE_TYPE_ORGANIZATION)){
			  		   					//� ������ ���� ��� �����������, �������� ������������
			  		   					PersonAttribute orgBoss = (PersonAttribute)stageMemberCard.getAttributeById(ORGANIZATION_BOSS);
			  		   					if (orgBoss != null && orgBoss.getPerson() != null){
			  		   						persons.add(orgBoss.getPerson());
			  		   					}		
			  		   				}
			   	  				}
			   	  				//���������� ������� �� ����� � � ����� ���������, ��� ������� �� ����� ������ ���� � �����
		   	  					boolean allPersonFound = true;
			   	  				//���� �� ���� ����������� ��������
			   	  				for(int i=0; i<persons.size(); i++){
			   	  					Person stagePerson = (Person)persons.get(i);
			   	  					boolean personFound = false;
				   					//���� �� ���� ����� � ���������
				   					Iterator<Card> iterVisaInDoc = visaCards.iterator();
				   					while (iterVisaInDoc.hasNext()) {
				   						//�������� �������� ����
				   						Card cardVisaInDoc = iterVisaInDoc.next();
				   						int orderVisa = ((IntegerAttribute) cardVisaInDoc.getAttributeById(VISA_NUMBER)).getValue();
				   		  				int periodVisa = ((IntegerAttribute) cardVisaInDoc.getAttributeById(VISA_PERIOD)).getValue();
				   		  				Person personVisa = ((PersonAttribute)cardVisaInDoc.getAttributeById(VISA_PERSON)).getPerson();
				   		  				
				   		  				if (orderVisa == orderStage && periodVisa == periodStage && 
				   		  						personVisa.getId().equals(stagePerson.getId())){
				   		  					personFound = true;
				   		  					//��������� ���� ��������������� ����� � ����. ���������, ��� ���� ����� � ����������� ���������� ������� ��� �� ��������������� �� ������ �����
				   		  					foundVisaCards.add(cardVisaInDoc);
				   		  					break;
				   		  				}
				   					}
				   					allPersonFound = (allPersonFound && personFound);
			   	  				}
		   	  					if (allPersonFound){
		   	  						stageIsValid = true;
		   	  					}			   	  				
		   	  				}
		   				}
		   				
				  		if (!stageIsValid){
				  			throw new DataException("jbr.processor.visadifferenceswithroute", 
				  					new String[]{stageName});
				  		}
		   			}
		  		}
		  		/* ���� ����������� � ������ ����� �������� ���� � ��� �� �������,
				 * ������� ����� ������ ���� (22.11.2010 - �. �.)
				 */
		  		for (int i = 0; i < foundVisaCards.size() - 1; i++)
		  		{
					Card iCard = foundVisaCards.get(i);
					if(((PersonAttribute)(iCard.getAttributeById(VISA_PERSON))).getValues() == null) continue;
					long iPid = (Long)((Person)((PersonAttribute)(iCard.getAttributeById(VISA_PERSON))).getValues().iterator().next()).getId().getId(); 
					int iOrder = ((IntegerAttribute) iCard.getAttributeById(VISA_NUMBER)).getValue();
					for (int j = i + 1; j < foundVisaCards.size(); j++)
					{
						Card jCard = (Card) foundVisaCards.get(j);
						if(((PersonAttribute)(jCard.getAttributeById(VISA_PERSON))).getValues() == null) continue;
						long jPid = (Long)((Person)((PersonAttribute)(jCard.getAttributeById(VISA_PERSON))).getValues().iterator().next()).getId().getId();
						if(iPid == jPid)
						{
							int jOrder = ((IntegerAttribute) jCard.getAttributeById(VISA_NUMBER)).getValue();
							if (jOrder < iOrder) foundVisaCards.remove(j--);
							else {foundVisaCards.remove(i--); break;}
						}
					}
		  		}
				/*---------------------------------------------------------------------------------------*/
		  		//�������� �� �� ��� ��� ��� �� ����������� �� � ������ �����
		  		visaCards.removeAll(foundVisaCards);
		  		if (visaCards.size() > 0){
		  			//����� ������ ���� ��� ��������� �� ������
		  			Card visaCard = visaCards.get(0);
	  				Person personVisa = ((PersonAttribute)visaCard.getAttributeById(VISA_PERSON)).getPerson();
		  			throw new DataException("jbr.processor.additionalVisa", new String[]{ personVisa.getFullName() });
		  		}
			}
		}
		
		return null;
	}
	
	public void setParameter(String name, String value) {
	}
}
