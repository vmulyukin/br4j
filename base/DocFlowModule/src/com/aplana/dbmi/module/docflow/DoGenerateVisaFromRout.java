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
import java.util.List;

import com.aplana.dbmi.action.CloneCard;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.file.CopyMaterial;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.query.WriteQuery;

/**
 * ���������� ������ Action GenerateVisaFromRout. �������� �������� ��� �� �������� ������������.
 * @author lyakin
 *
 */

public class DoGenerateVisaFromRout extends ActionQueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;
	
	public static final ObjectId VISA_LIST = ObjectId.predefined(CardLinkAttribute.class, "jbr.visa.set");
	public static final ObjectId ATTACH = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	public static final ObjectId TYPICAL_ROUTE = ObjectId.predefined(CardLinkAttribute.class, "jbr.saveNegotiationList");
	public static final ObjectId URGENCY_LEVEL = ObjectId.predefined(ListAttribute.class, "jbr.urgencyLevel");
	//public static final ObjectId BOSS_URGENCY_LEVEL = ObjectId.predefined(ListAttribute.class, "jbr.boss.urgency");
	public static final ObjectId NEGOTIATION_LIST_URGENCY_LEVEL = ObjectId.predefined(ListAttribute.class, "jbr.negotiationlist.urgencyCategory");
	
	@Override
	public Object processQuery() throws DataException {

		final GenerateVisaFromRout action = (GenerateVisaFromRout)getAction();
		final ObjectId docId = action.getDocId();
		if (docId==null){
			throw new DataException("action.parentdoc.not.save");
		}
		final ObjectId routId = action.getRoutId();
		final QueryFactory f = getQueryFactory();
		final  Database d = getDatabase();
		final  UserData user = getUser();
		final ObjectQueryBase q = f.getFetchQuery(Card.class);
		ActionQueryBase aq = f.getActionQuery(CreateCard.class);
		ActionQueryBase aqUo = f.getActionQuery(UnlockObject.class);
		Card doc=null;
		Card rout=null;
		CreateVisaCard createVisa = new CreateVisaCard();
		ArrayList<Card> cards = new ArrayList<Card>();
		ArrayList<Card> linkcards = new ArrayList<Card>();
		cards=createVisa.getCards(routId,f,d,user,docId);
		Iterator<Card> iter = cards.iterator();
		while (iter.hasNext()) {
			Card visaCard = iter.next();
			final SaveQueryBase sq = f.getSaveQuery(visaCard);
			sq.setObject(visaCard);
			final ObjectId result = (ObjectId)d.executeQuery(user, sq);

			UnlockObject uo = new UnlockObject();
			uo.setId(result);
			aqUo.setAction(uo);
			d.executeQuery(user, aqUo);
			q.setId(result);
			// ����� �������� ����, �� �� ��� ���� ���, ������� ������ � �� ����� �������
			// ToDo: ���������� �� �������������� �������� ��� ������ GetCard � ����� ���������� � ��������� ��������
			Card visa = (Card)d.executeQuery(user, q);
			linkcards.add(visa);
		}
		q.setId(docId);
		doc = (Card)d.executeQuery(user, q);
		CardLinkAttribute linkVisa = (CardLinkAttribute) doc.getAttributeById(VISA_LIST);
		CardLinkAttribute linkDocAtach = (CardLinkAttribute) doc.getAttributeById(ATTACH);
		((CardLinkAttribute) doc.getAttributeById(TYPICAL_ROUTE)).addLinkedId(routId);
		linkVisa.setIdsLinked(linkcards);
		//�������� �������� ��������
		q.setId(routId);
		rout=(Card)d.executeQuery(user, q);
		//������������� ��������� ��������� ��� ��������� �� �������� ��������
		ReferenceValue urgencyLevel = ((ListAttribute) rout.getAttributeById(NEGOTIATION_LIST_URGENCY_LEVEL)).getValue();
		ListAttribute urgencyAttribute = (ListAttribute) doc.getAttributeById(URGENCY_LEVEL);
		if (urgencyLevel!=null && urgencyAttribute != null){
			urgencyAttribute.setValue(urgencyLevel);
			//((ListAttribute) doc.getAttributeById(BOSS_URGENCY_LEVEL)).setValue(urgencyLevel);
		}


//		q.setId(routId);
//		Card negotiationList = (Card)d.executeQuery(user, q);
		CardLinkAttribute linkNegotiationListAttach = (CardLinkAttribute) rout.getAttributeById(ATTACH);
  		if (linkNegotiationListAttach != null) {
  			Iterator<ObjectId> iterAttach = linkNegotiationListAttach.getIdsLinked().iterator();
  			final List<DataObject> clonedCards = new ArrayList<DataObject>();
  			while (iterAttach.hasNext()) {

   				ObjectId attachId = iterAttach.next();

   				final CloneCard cloneAction = new CloneCard();
				cloneAction.setOrigId(attachId);
				aq = f.getActionQuery(cloneAction);
				aq.setAction(cloneAction);
				final Card loaded = (Card)d.executeQuery(user, aq);
				SaveQueryBase sq = getQueryFactory().getSaveQuery(loaded);
				sq.setObject(loaded);
				final ObjectId clonedCardId = (ObjectId)d.executeQuery(user, sq);
				clonedCards.add(DataObject.createFromId(clonedCardId));
				// �������� ���� ���������, ���� �� ����
				CopyMaterial copyMaterial = new CopyMaterial();
				copyMaterial.setFromCardId(attachId);
				copyMaterial.setToCardId(clonedCardId);
				aq = f.getActionQuery(copyMaterial);
				aq.setAction(copyMaterial);
				d.executeQuery(getSystemUser(), aq);
  			}
			linkDocAtach.addIdsLinked(clonedCards);
  		}

  		final SaveQueryBase sq = f.getSaveQuery(doc);
		sq.setObject(doc);
		@SuppressWarnings("unused")
		final ObjectId result = (ObjectId)d.executeQuery(user, sq);
		return null;
	}


}
