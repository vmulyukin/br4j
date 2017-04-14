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
package com.aplana.dbmi.jbr.processors.card.runcheck;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;

import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.UserData;

/**
 * ��������������� ����� ��� �������� ����������� �������� ������������ � ������� �������� �������� ������� ���.
 * ������� ��������  "attrId" ��������� ������� (���� {@link PersonAttribute} ��� {@link CardLinkAttribute})
 * ��� �������� �������� ������������ ��������
 *
 */
public class CheckCurrentUserInAttr extends ProcessCard implements CardChecker {
   private static final long serialVersionUID = 1L;

   protected final static String PARAM_ATTR_ID = "attrId";
   
   protected ObjectId attrId;

   protected Card curCard;

   public void setParameters(Map<String, String> parameters) throws CardCheckException {
       super.params.clear();
       if (parameters != null) {
           for (Map.Entry<String, String> item : parameters.entrySet()) {
               setParameter(item.getKey(), item.getValue());
           }
       }
   }

   public void setCard(Card card) {
       this.curCard = card;
   }

   public void checkCard() throws CardCheckException {
       try {
           init();
    	   process();
       } catch (DataException ex) {
           throw new CardCheckException(ex);
       }
   }


   protected void init() throws DataException {
	   
	   final String aId = getParameter(PARAM_ATTR_ID, null);
       if (aId == null) {
           throw new CardCheckException( "Parameter " +  PARAM_ATTR_ID + " is not configured -> check is FALSE");
       }
       
       attrId = IdUtils.smartMakeAttrId( aId, CardLinkAttribute.class );
       if (attrId == null) {
           throw new CardCheckException( "Value of parameter " +  PARAM_ATTR_ID + " could not identified -> check is FALSE");
       }
       
       // ��������� �� ������������ ����� ���������
       if (!attrId.getType().equals(PersonAttribute.class) && !attrId.getType().equals(CardLinkAttribute.class))
           throw new CardCheckException( "Value of parameter " +  PARAM_ATTR_ID + " is defined wrong -> check is FALSE");

       // ��������� ���
       if (curCard == null) {
           curCard = super.loadCardById( getCardId() );
           if (curCard == null)
               throw new CardCheckException( "general.null", new Object[] {"card"});
       }
   }
   
   @Override
   public Object process() throws DataException {
              
       Set<Person> persons = getPersonsList(curCard, attrId, false);
       if (persons == null){
           logger.debug("Card "+ curCard.getId()+ " has empty attribute "+ attrId + "in card " + curCard.getId() +" -> check is FALSE");
           throw new CardCheckException( "No persons in "+attrId+"@"+curCard.getId()+"");
       }
       
       if (! persons.contains((Person)getUser().getPerson())){
           logger.debug("Card "+ curCard.getId()+ " has not current person "+ getUser().getPerson().getId()+ " in attribute "+ attrId +" -> check is FALSE");
           throw new CardCheckException( "No current person in card "+attrId+"@"+curCard.getId()+"");
       }

       return null;
   }

   @Override
   public String toString(){
       return MessageFormat.format( "CheckCurrentUserInAttr ({0}=''{1}'')", 
                   PARAM_ATTR_ID, attrId != null ? attrId.getId() : "null" );
   }
   
   protected void setAttrId(ObjectId attrId) {
       this.attrId = attrId;
   }
}
