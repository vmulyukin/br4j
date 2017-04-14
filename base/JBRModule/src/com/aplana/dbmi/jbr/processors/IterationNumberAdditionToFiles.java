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

import java.util.Iterator;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;


/*
 * ����� ��������������. ���������� ��� ���������� ���������. 
 * ������ �������� �������� "����� ��������" ��� ������ �������� ��������� 
 * �� �������� �������� "����� �������� ������������" ���������, 
 * ���� � ����� "����� ��������" ����� ����.
 */
public class IterationNumberAdditionToFiles extends ProcessCard {
	private static final long serialVersionUID = 1L;
	private static final String PARAM_ITERATION_NUMBER = "IterationNumber";
	private static final String PARAM_FILES_ATTR = "filesAttr";
	ObjectId IterationNumberID;
	ObjectId fileLinksId = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	public static final ObjectId FILE_ITERATION_NUMBER = ObjectId.predefined(IntegerAttribute.class, "jbr.version");

	
	@Override
	public Object process() throws DataException {
		Card document = getCard();
			
		IntegerAttribute docIterNumberAttr = document.getAttributeById(IterationNumberID);
		Integer docIterNum =docIterNumberAttr.getValue();
		CardLinkAttribute fileLinks = document.getAttributeById(fileLinksId);
		
		if (fileLinks != null)
		{
			Iterator<ObjectId> iter = fileLinks.getIdsLinked().iterator();
	        while (iter.hasNext()) {
	            ObjectId fileId = iter.next();
	            
	            ObjectQueryBase objectQuery = getQueryFactory().getFetchQuery(Card.class);
				objectQuery.setId(fileId);
				Card file = getDatabase().executeQuery(getSystemUser(), objectQuery);
				
				IntegerAttribute fileIterNumberAttr = file.getAttributeById(FILE_ITERATION_NUMBER);
				Integer fileIterNum = fileIterNumberAttr.getValue();
				if(fileIterNum == 0)
				{
					fileIterNumberAttr.setValue(docIterNum);
					
					execAction(new LockObject(fileId), getSystemUser());			
					try {
						saveAction(file);
					} finally {
						execAction(new UnlockObject(fileId), getSystemUser());
					}
				}				
	        }
		}

		return null;
	}

	/*
	 * ����� ��������� ������ �� ����������
	*/ 
	private Object saveAction(DataObject dataObject) throws DataException{
		SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(dataObject);
		saveQuery.setObject(dataObject);
		return getDatabase().executeQuery(getSystemUser(), saveQuery);		
	}

	@Override
	public void setParameter(String name, String value) {
		if (name == null) 
			return;
		if (PARAM_ITERATION_NUMBER.equalsIgnoreCase(name)) {
			this.IterationNumberID = ObjectId.predefined(IntegerAttribute.class, value);
		} else if (PARAM_FILES_ATTR.equalsIgnoreCase(name)) {
			this.fileLinksId = ObjectId.predefined(CardLinkAttribute.class, value);
		}

		super.setParameter(name, value);
	}
}
