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
package com.aplana.dbmi.support.query;

import java.util.List;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dbmi.support.action.CreateResolution;

public class DoCreateResolution extends ActionQueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;
	
	// "��������-���������"
	static final ObjectId mainDocLink = ObjectId.predefined(CardLinkAttribute.class, "jbr.main.doc");

	@Override
	public Object processQuery() throws DataException {
		CreateResolution action = getAction();
		Card parent = action.getParentCard();
		ObjectId template = action.getTemplateId();
		Attribute link = action.getLinkAttr();
		
		if(link == null)
			throw new NullPointerException("linkAttribute is null");
		if(!(link instanceof BackLinkAttribute))
			throw new ClassCastException("illegal class of linkAttribute. Expected: " + BackLinkAttribute.class);
		if(((BackLinkAttribute) link).getLinkSource() == null)
			throw new IllegalArgumentException("Illegal link - sourceLink not found");
		
		ObjectQueryBase loadParent = getQueryFactory().getFetchQuery(parent.getClass());
		loadParent.setId(parent.getId());
		Card parentLoaded = getDatabase().executeQuery(getUser(), loadParent);
		
		//create resolution
		CreateCard createCard = new CreateCard();
		createCard.setTemplate(template);
		createCard.setLinked(true);
		createCard.setParent(parent);
		ActionQueryBase aqb = getQueryFactory().getActionQuery(createCard);
		aqb.setAction(createCard);
		Card child = getDatabase().executeQuery(getUser(), aqb);
		
		//save resolution first time to get ID
		SaveQueryBase sqb = getQueryFactory().getSaveQuery(child);
		sqb.setObject(child);
		sqb.setAsync(false);
		ObjectId childId = getDatabase().executeQuery(getUser(), sqb);
		child.setId(childId);
		
		try {
			//link maindoc to created resolution and save maindoc
			
			//������ ���� �� ������������ �������� (��������-��������� ���� ��������� 1 ������, ����� ������������ ���������)
			CardLinkAttribute parentAttr = child.getCardLinkAttributeById(((BackLinkAttribute) link).getLinkSource());
			parentAttr.addLinkedId(parent.getId());
			
			//��� �� ����������� ������ �� �� JBR_MAINDOC (backlink JBR_ALL_RESOLUTIONS)
			CardLinkAttribute mainDocParent = parentLoaded.getCardLinkAttributeById(mainDocLink);
			// == null ������ ���� ������ ��
			if(mainDocParent != null) {
				CardLinkAttribute mainDoc = child.getCardLinkAttributeById(mainDocLink);
				List<ObjectId> list = mainDocParent.getIdsLinked();
				if(list != null && !list.isEmpty()) {
					mainDoc.addLinkedId(list.get(0));
				} else {
					logger.error("Parent doc " + parentLoaded.getId() != null ? parentLoaded.getId().getId() : parentLoaded.getId() + " have not link to MainDoc");
				}
				if(list.size() > 1) {
					logger.warn("Parent doc " + parentLoaded.getId() != null ? parentLoaded.getId().getId() : parentLoaded.getId() + " have more than 1 link to MainDoc");
				}
			} else {
				// ���� ������ ��, �� ������ �� ��, ��� � � parentAttr
				CardLinkAttribute mainDoc = child.getCardLinkAttributeById(mainDocLink);
				mainDoc.addLinkedId(parent.getId());
			}
			
			/*OverwriteCardAttributes overwrite_action = new OverwriteCardAttributes();
			overwrite_action.setCardId(parent.getId());
			overwrite_action.setAttributes(Collections.singletonList(links));
			overwrite_action.setInsertOnly(false);
			aqb = getQueryFactory().getActionQuery(overwrite_action);
			aqb.setAction(overwrite_action);
			getDatabase().executeQuery(getUser(), aqb);*/
		
			//��������� ��������� ������ ���, ���� ���������� ���������� ��� ��
			/* ����� ���� ���� ������ ������: ����� ����, ��� �� ��������� ��������� � ������ ���, ����� ��� ���� �� ���� �����������,
			* �.�. ������ ��� ��������������� � ����� �������� �����, ������� ����� ���������� ����������� ����� ����� ������ �����������,
			* ���� ��������� ��� �� ����� �������, �� �.�. ����� ���������� ������������� ��������, �� ������ ������������� ����� (���������� � �������� ��������)
			*/
			//accessManager.updateAccessByCard(child.getId());
			/*ObjectQueryBase getChild = getQueryFactory().getFetchQuery(Card.class);
			getChild.setId(childId);
			child = (Card) getDatabase().executeQuery(getSystemUser(), getChild);*/

			sqb = getQueryFactory().getSaveQuery(child);
			sqb.setObject(child);
			sqb.setAsync(false);
			getDatabase().executeQuery(getSystemUser(), sqb);
		} catch (Exception ex) {
			UnlockObject unlock = new UnlockObject(child);
			aqb = getQueryFactory().getActionQuery(unlock);
			aqb.setAction(unlock);
			getDatabase().executeQuery(getUser(), aqb);
			if (ex instanceof DataException)
				throw (DataException)ex;
			throw new DataException(ex);
		}
		
		return childId;
	}
}