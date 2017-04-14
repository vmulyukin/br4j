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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.CardLinkLoader;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * @comment RAbdullin
 * �������������� ���������� �������� "����� ��������" �������� ������ "������ � �������� ����������".
 * ��������:
 *   1) � ������ ������� �������� "������ � �������� ����������" ('JBR_ORIG_WORKPAP')
 * ���������� ����� ������� �������� (�� ���� � �������� 'JBR_WKPO_DATE'/"����")
 * � �������� "�������� � �������� ����������" (344), � ������� ������� "��������" 
 * ('JBR_WKPO_ACTION') ����� "��������� � ���������";
 *   2) � ���� �������� ���������� �� ������ "����/����:" ('JBR_WKPO_WHERE')
 * ��� ������� �������� � ���� ������;
 *   3) ��� ��� ��������� � ������� ������� �������� "����� ��������" ('JBR_ORIG_STORAGE')
 */
public class SetStoragePlace  extends ProcessCard implements DatabaseClient{
	private static final long serialVersionUID = 1L;

	private final static ObjectId PAPER_WHERE_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.paperwhere"); //JBR_WKPO_WHERE

    private final static ObjectId ACTION_PAPER_DATE_ID = ObjectId.predefined(DateAttribute.class, "jbr.paperdate");//JBR_WKPO_DATE
    private final static ObjectId ACTION_WORKPAPER_ID = ObjectId.predefined(ListAttribute.class, "jbr.paperitemaction");//JBR_WKPO_ACTION
    private final static ObjectId ACTION_PAPER_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.paperaction");//JBR_ORIG_WORKPAP
    private final static ObjectId ACTION_ORIG_ID = ObjectId.predefined(Template.class, "jbr.paperaction");//344
    private final static ObjectId ACTION_TOSTORAGE_ID = ObjectId.predefined(ReferenceValue.class, "jbr.paperAction.toStorage");//1438 "��������� � ���������"
    
    private final static ObjectId PAPER_ORIG_STORAGE_ID = ObjectId.predefined(StringAttribute.class, "jbr.paperstorage"); //JBR_ORIG_STORAGE "����� ��������"
    // private  JdbcTemplate jdbcTemplate;

    private final static String GET_TO_STORAGE_CARDS_SQL = "select c.card_id, av_date.date_value from card c, attribute_value av_date where "+ 
       "exists (select 1 from attribute_value av "+
                  "where av.number_value = c.card_id and av.attribute_code = :paperActionID and "+
                  		"av.card_id = :cardID) "+
       "and c.template_id = :origActID "+
       "and exists (select 1 from attribute_value av "+
                     "where av.card_id = c.card_id and av.attribute_code = :paperItemActionID and "+
                           "av.value_id = :toSorageActionID) "+
       "and av_date.card_id = c.card_id "+
       "and av_date.attribute_code = :paperDateID";


	/*
	 * (non-Javadoc)
	 * @see com.aplana.dbmi.service.impl.ProcessorBase#process()
	 * 		select c.card_id, av_date.date_value from card c, attribute_value av_date 
	 * 		where
	 * 			exists (
	 * 				select 1 from attribute_value av 
	 * 				where 	av.number_value = c.card_id 
	 * 						and av.attribute_code = 'JBR_ORIG_WORKPAP' 
	 * 						and av.card_id = ?
	 * 				) 
	 * 			and c.template_id = 344
	 * 			and exists (
	 * 				select 1 from attribute_value av 
	 * 				where 	av.card_id = c.card_id 
	 * 						and av.attribute_code = 'JBR_WKPO_ACTION' 
	 * 						and av.value_id = 1438
	 * 				)
	 * 			and av_date.card_id = c.card_id
	 * 			and av_date.attribute_code = 'JBR_WKPO_DATE'
	 * 		;
	 * 		update card set status_id = 103 where card_id = 5443;
	 */
	class mCard implements Comparable<mCard>{
		public Long ID;
		public Date date;

		public int compareTo(mCard o) {
			if (o == null) return 1;
			if (date == null) {
				if (o.date == null) return 0;
				return -1;
			}
			return date.compareTo(o.date);
		}
	}

	public class mCardMapper implements ParameterizedRowMapper<mCard>{

		public mCard mapRow(ResultSet rs, int rowNum) throws SQLException {
			final mCard mc = new mCard();
			mc.ID = rs.getLong("card_id");
			mc.date = rs.getDate("date_value");
			return mc;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException {
		
		Long cardId = 0L;
		try {
			NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(getJdbcTemplate());
			cardId = (Long)getCardId().getId();
			if (logger.isDebugEnabled())
				logger.debug("SetStoragePlace is going to process Card# "+ cardId +" ...");

			final List<mCard> cards = jdbc.query(GET_TO_STORAGE_CARDS_SQL, 
					new MapSqlParameterSource().addValue("cardID", cardId, Types.NUMERIC)
						.addValue("origActID", ACTION_ORIG_ID.getId(), Types.NUMERIC)
						.addValue("paperActionID", ACTION_PAPER_ID.getId(), Types.VARCHAR)
						.addValue("paperItemActionID", ACTION_WORKPAPER_ID.getId(), Types.VARCHAR)
						.addValue("paperDateID", ACTION_PAPER_DATE_ID.getId(), Types.VARCHAR)
						.addValue("toSorageActionID", ACTION_TOSTORAGE_ID.getId(), Types.NUMERIC),
						new mCardMapper()
					);
			if (cards == null || cards.isEmpty())
				return null;
			final mCard mc = Collections.max(cards);

			final ObjectQueryBase q = getQueryFactory().getFetchQuery(Card.class);
			q.setId(new ObjectId(Card.class, mc.ID));
			final Card c = (Card)getDatabase().executeQuery(getSystemUser(), q);

			String s = "";
			final CardLinkAttribute orgAttr = (CardLinkAttribute) c.getAttributeById(PAPER_WHERE_ID);
			if (orgAttr != null)
			{
				// (2010/02, RuSA) values
				final Collection<Card> linkedCards = 
					CardLinkLoader.loadCardsByLink( orgAttr, new ObjectId[] { Attribute.ID_NAME }, 
							getSystemUser(), getQueryFactory(), getDatabase());

				if (linkedCards != null && !linkedCards.isEmpty()) {
					final Card org = linkedCards.iterator().next();
					s = org.getAttributeById(Attribute.ID_NAME).getStringValue();
					if (logger.isDebugEnabled())
						logger.debug("SetStoragePlace has got storage place ID: "+org.getId().getId()+" and its name: "+ s);
				}
	
				final Card card = getCard();
				((StringAttribute)card.getAttributeById(PAPER_ORIG_STORAGE_ID)).setValue(s);
				SaveQueryBase sqb = getQueryFactory().getSaveQuery(card);
				if (logger.isDebugEnabled())
					logger.debug("SetStoragePlace is about to save attribute ID: "+PAPER_ORIG_STORAGE_ID.getId()+
						" with value: " + s + " for Card#"+ cardId +" ...");
				sqb.setObject(card);
				execAction(new LockObject(card), getSystemUser());
				try {
					getDatabase().executeQuery(getSystemUser(), sqb);
				} finally {
					execAction(new UnlockObject(card), getSystemUser());
				}
				if (logger.isDebugEnabled())
					logger.debug("SetStoragePlace has successfully ended");
			}

			final Card card = loadCardById(getCard().getId());
			((StringAttribute)card.getAttributeById(PAPER_ORIG_STORAGE_ID)).setValue(s);
			final SaveQueryBase sqb = getQueryFactory().getSaveQuery(card);
			if (logger.isDebugEnabled())
				logger.debug("SetStoragePlace is about to save attribute ID: "+PAPER_ORIG_STORAGE_ID.getId()+
					" with value: " + s + " for Card#"+ cardId +" ...");
			sqb.setObject(card);
			// ���������� �����
			execAction(new LockObject(card), getSystemUser());
			try {
				getDatabase().executeQuery(getSystemUser(), sqb);
			} finally {
				execAction(new UnlockObject(card), getSystemUser());
			}
			if (logger.isDebugEnabled())
				logger.debug("SetStoragePlace has successfully ended");
		} catch (Exception e1) {
			logger.error("SetStoragePlace: there are problems when setting attribute for Card#"+ cardId, e1);
		}
		return null;
	}
}