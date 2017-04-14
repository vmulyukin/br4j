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

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;

import java.util.ArrayList;
import java.util.Collection;

/**
 * ����� �������������. ���������� ��� ���������� �������� ����. ��������� ������� ����� ������ ������������,
 * � ���� ����� ������� �� ������� ��� ������� �� �������� ����. � ����� � �������� �������� ���� ���������
 * ������ ������ ������������. 
 * @author larin
 *
 */
public class CteateVisaFromMultyNegotiators extends ProcessCard{
	public static final ObjectId VISA_PERSON_ATTRIBUTE = ObjectId.predefined(PersonAttribute.class, "jbr.visa.person");
	public static final ObjectId VISA_DOCUMENT_ATTRIBUTE = ObjectId.predefined(BackLinkAttribute.class, "jbr.visa.parent");
	public static final ObjectId DOCUMENT_VISA_ATTRIBUTE = ObjectId.predefined(CardLinkAttribute.class, "jbr.visa.set"); 

	@Override
	public Object process() throws DataException {
		Card visaCard = getCard();
		PersonAttribute visaPersonAttr = 
			(PersonAttribute)visaCard.getAttributeById(VISA_PERSON_ATTRIBUTE);
		
		Collection<Person> visaPersons = visaPersonAttr.getValues();
		//��������� ������� � ���� ����������� ��������� ������
		if (visaPersons != null && visaPersons.size() > 1){
			//����� ������ ������� ������������, ����� �� ������ ���� ����� ���
			Person firstPerson = null;
			long firstId = (Long) visaCard.getId().getId();
			ArrayList addadVisas = new ArrayList();
			//���� �� �����������, �� ������� ������������ ����� ������� ������� ��������� ����
			/*
			 * �.�., 12.01.2011
			 * � ������� ���� @Card, �������������� �������� ����, � ����� �� ��������:
			 * 1) ��������� id;
			 * 2) � �������, ���������� ���������� � �����������, ������������
			 * ������������ ������� (�������), ����� ������;
			 * 3) ���������� ���������� ����� - ��������� id �������� � ������������ ��������
			 * ����� �����.
			 * 
			 * ����� ����� ����������������� ����������� id �������� ���� � � �������,
			 * ���������� ����������� ������������ ������ �������. ����� ����� ��������
			 * ���� ��������� �����������.
			 */
			for (Person visaPerson : visaPersons) {
				if (firstPerson == null){
					firstPerson = visaPerson; 
				}else{
					visaCard.clearId();
					
					//������������� ������������
					visaCard.getAttributeById(VISA_PERSON_ATTRIBUTE).clear();
					((PersonAttribute)visaCard.getAttributeById(VISA_PERSON_ATTRIBUTE)).setPerson(visaPerson);
					
					//���������
					ObjectId newVisaId = (ObjectId)saveAction(visaCard);
					
					//������������
					execAction( new UnlockObject(newVisaId), getSystemUser());					
					
					//��������� � ��������� ����������� ���
					addadVisas.add(newVisaId);
				}
			}
			//������� ���� ����������� �� ����������� �������
			visaPersonAttr.clear();
			visaPersonAttr.setPerson(firstPerson);
			//��������������� Id
			visaCard.setId(firstId);
			
			//����� ���� ���������� � ��������
			//�������� ������� ������ �� ��������
			BackLinkAttribute documentAttr = 
				(BackLinkAttribute)visaCard.getAttributeById(VISA_DOCUMENT_ATTRIBUTE);
			
			//�� BackLinkAttr �������� ��������
			ListProject listAction = new ListProject();
			listAction.setCard(visaCard.getId());
			listAction.setAttribute(documentAttr.getId());
			SearchResult execResult = (SearchResult)execAction(listAction);
			if (execResult.getCards().size() > 0){
				Card document = (Card)execResult.getCards().get(0);
				
				ObjectQueryBase objectQuery = getQueryFactory().getFetchQuery(Card.class);
				objectQuery.setId(document.getId());
				document = (Card)getDatabase().executeQuery(getSystemUser(), objectQuery);		
				
				CardLinkAttribute visaAttr = (CardLinkAttribute)document.getAttributeById(DOCUMENT_VISA_ATTRIBUTE);
				visaAttr.addIdsLinked(addadVisas);

				execAction( new LockObject(document.getId()), getSystemUser());			
				try {
					saveAction(document);
				} finally {
					execAction( new UnlockObject(document.getId()), getSystemUser());
				}
				
				//���������� ����������� ���� ���������������� � ��������� �������� ���������
				// ��������� �� BR4J00040199 (mem leak, 14.08.2015)
				//EventContext.getInstance().raiseEvent(new CardChangeEvent(document.getId()));
			}
		}
		return null;
	}
	

	/**
	 * ����� ��������� ������ �� ����������
	 * @param dataObject
	 * @return
	 * @throws DataException
	 */
	private Object saveAction(DataObject dataObject) throws DataException{
		SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(dataObject);
		saveQuery.setObject(dataObject);
		return getDatabase().executeQuery(getSystemUser(), saveQuery);		
	}

}
