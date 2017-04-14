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

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.CardLinkLoader;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.QueryBase;

import java.util.*;

/**
 * Created by echirkov on 10.08.2015.
 * ��������� ��������� ��������� � ������ ����������:
 *  1. ���� ��� �������� ����������� ��������� �� ���������� ������� ���������,
 *  �� ��������� �� ���������� ��������� � ��� �� �����������.
 *  2. � ��������� ������� ��������� ��� ���������
 */
public class DoDependentResolutionChangeState extends DoDependentChangeState{

    public static ObjectId RESOLUTION_TEMPLATE_ID = ObjectId.predefined(Template.class, "jbr.resolution");
    public static ObjectId EXECUTION_STATE_ID = ObjectId.predefined(CardState.class, "execution");
    public static ObjectId FIO_SIGN_ATTR_ID = ObjectId.predefined(PersonAttribute.class, "jbr.resolution.FioSign");


    @Override
    protected Collection<Card> getDependentCards(Card baseCard) throws DataException {
        QueryBase primaryQuery = getPrimaryQuery();
        if (primaryQuery instanceof ActionQueryBase) {
            ActionQueryBase primaryActionQueryBase = (ActionQueryBase) primaryQuery;
            if (primaryActionQueryBase.getAction() instanceof ChangeState) {
                ChangeState changeState = primaryActionQueryBase.getAction();
                //���� ������������ ���� ��� ������� ��������� �� ����������, �� ��������� ���������
                if(changeState.getCard().getTemplate().equals(RESOLUTION_TEMPLATE_ID)
                        && changeState.getWorkflowMove().getToState().equals(EXECUTION_STATE_ID)){
                    Collection<Card> linkedCards = super.getDependentCards(baseCard);
                    PersonAttribute primaryFioSign = changeState.getCard().getAttributeById(FIO_SIGN_ATTR_ID);
                    Iterator<Card> i = linkedCards.iterator();
                    while(i.hasNext()){
                        PersonAttribute currentFioSign = i.next().getAttributeById(FIO_SIGN_ATTR_ID);
                        if(!currentFioSign.equalValue(primaryFioSign)){
                            i.remove();
                        }
                    }
                    return linkedCards;
                }
            }
        }
        return super.getDependentCards(baseCard);
    }

    protected Collection<Card> getBackLinkedCards(ObjectId attrId, ObjectId cardId) throws DataException {
        final ListProject list = new ListProject();
        list.setAttribute(attrId);
        list.setCard(cardId);
        final List<SearchResult.Column> cols = new ArrayList<SearchResult.Column>(1);
        cols.add( CardUtils.createColumn(Card.ATTR_STATE));
        cols.add( CardUtils.createColumn(FIO_SIGN_ATTR_ID));
        list.setColumns( cols);
        return CardUtils.execSearchCards(list, getQueryFactory(), getDatabase(), getOperUser());
    }

    protected Collection<Card> getLinkedCards(CardLinkAttribute attr) throws DataException {

        if (attr == null || attr.getLinkedCount() < 1)
            return Collections.emptyList();

        return CardLinkLoader.loadCardsByLink(attr, new ObjectId[]{Card.ATTR_STATE, FIO_SIGN_ATTR_ID},
                getOperUser(), getQueryFactory(), getDatabase());
    }
}
