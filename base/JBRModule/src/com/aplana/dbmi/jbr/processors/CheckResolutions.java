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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.utils.StrUtils;

/**
 * @author RAbdullin
 *	�������� �� ������������ ��������� ������ ��������, ��������� � ������.
 */
public class CheckResolutions extends ProcessCard 
	implements DatabaseClient, Parametrized {
	private static final long serialVersionUID = 1L;

	/**
	 * �������� ��������� � ��������� �������� ��������, � ������� ������ ���� 
	 * �������������� ������ ��������.
	 */
	private final static String NAMEPARAM_AttrName4List = "ATTR_NAME_4_LIST";

	// id-�������� ��� ��������� ������
	// (������ ���� ����� ����������)
	private List<ObjectId> attrName4CheckingList = new ArrayList<ObjectId>(); 
	
	/**
	 * �������� ��������� �� ������� ���������� ���������.
	 */
	private final static String NAMEPARAM_ListOk = "LIST_OK";

	// ������ ���������� ��������
	// (������ ���� ����� ����������)
	private final List<ObjectId> listOk 
		= new ArrayList<ObjectId>(); 
	
	/**
	 * �������� ��������� � ��������� ������� ��� ���������� �� ������ 
	 * ���������� ��������.
	 */
	private final static String NAMEPARAM_InfoCardFmt4 = "INFO_CARD_FMT_4";
	
	// ��������� ������ 
	// (����� ���� ������ � ����������)
	private String infoFmt4 = "\t [{0}] {1}, {2} (id={3}) \n"; // �� ��������� ��� 4 ���� ���� ������������ 
	
	/**
	 * �������� �������� � ���������� � ����������� ��������� ���������.
	 * ��� �������� ����������, ���� ��������� ������ ������ this.listOk.
	 */
	private final static ObjectId OBJID_STATUS  
		= Card.ATTR_STATE; // new ObjectId( ListAttribute.class, "_STATE"); 
	
	/* (non-Javadoc)
	 * @see com.aplana.dbmi.service.impl.Parametrized#setParameter(java.lang.String, java.lang.String)
	 */
	@Override
	public void setParameter(String name, String value) 
	{
		if (name == null) return;
		
		if (name.equalsIgnoreCase(NAMEPARAM_InfoCardFmt4))
		{	// ��������� ������ ��� ���� �� ���������� ��������... 
			this.infoFmt4 = value;
			logger.debug( MessageFormat.format( 
					"Format for card info is now ''{0}''", 
					new Object [] { this.infoFmt4 } 
					));
		} 

		else if (name.equalsIgnoreCase(NAMEPARAM_AttrName4List))
		{	// id ����� �������� (�������� �������� �� "objectids.properties")
			//
			String[] values = value.split("->");
			
			for(String val : values) {
				attrName4CheckingList.add(IdUtils.smartMakeAttrId(val, CardLinkAttribute.class));
			}
			
		}

		else if (name.equalsIgnoreCase(NAMEPARAM_ListOk))
		{	// ������ ���������� ��������...
			this.setAccessibleStateList(value);
			if (this.listOk == null || this.listOk.isEmpty())
			{
				logger.warn( MessageFormat.format( 
						"Accessible state list IS EMPTY (!?) after assigning <{0}>", 
						new Object [] { value } 
						));
			} else {
				logger.debug( MessageFormat.format( 
						"Accessible state list has now {0} elements [{1}]", 
						new Object [] { 
								new Integer( this.listOk.size()), 
								StrUtils.getAsString(this.listOk) } 
						));
			}
		}

		else  
			logger.warn( MessageFormat.format( 
					"Unknown parameter name <{0}", 
					new Object [] { value } 
					));
	}

	/**
	 * ���������� ������ ���������� ���������.
	 * @param value
	 */
	private void setAccessibleStateList(String value) {
		
		this.listOk.clear();
		
		if (value == null) return;
		
		final String[] got 
			= org.springframework.util.StringUtils.commaDelimitedListToStringArray(value);

		for (int i = 0; i < got.length; i++ ) {
			this.listOk.add( ObjectIdUtils.getObjectId( CardState.class, got[i], true ));
		}
	}

	/* Check that all linked card resolutions are at complete/reject state.
	 * Raise if not (with the list of incompleted resolutions).
	 * @see com.aplana.dbmi.service.impl.ProcessCard#process()
	 */
	@Override
	public Object process() throws DataException {
		
		final List<Card> /*of cards*/ incompleted 
		 	= this.getIncompletedResolutions();
		
		if (!CollectionUtils.isEmpty(incompleted))
		{	// problem -> we have incompleted list...
			final StringBuffer sb = makeProblemsStrList(incompleted);
			throw new DataException( "jbr.card.check.resolutions.hasincompleted_1", 
						new Object[]{ sb.toString() });
		}
		return null; // OK, checked successfully
	}

	/**
	 * @param incompleted
	 * @return
	 */
	private StringBuffer makeProblemsStrList(final List<Card> incompleted) {
		/// TODO: ���������� ������ �����
		return new StringBuffer( MessageFormat.format(  
				this.infoFmt4, // "\t  [{0}] {1} {2} ({3}) \n" 
				new Object[] { 
					new Integer( (incompleted != null) ? incompleted.size() : 1),
					"",
					"",
					""
				}));
		/*
		final StringBuffer result 
			= new StringBuffer( "<ol>");   // <ul>
		
		// final CardLinkAttribute attr	= (CardLinkAttribute) getCard().getAttributeById(this.attrName4CheckingList);
		// final String attrName = (attr != null) ? attr.getName() : "";
		
		int i = 0;
		for (Iterator iterator = incompleted.iterator(); iterator.hasNext();) {
			final Card card = (Card) iterator.next();
			if (card != null)
			{
				++i;
				final Attribute attrName = card.getAttributeById(Attribute.ID_NAME);
				final Attribute attrStateTag = card.getAttributeById(OBJID_STATUS);
				
				final String cardId = card.getId().getId().toString(); 
				final String cardName = (attrName != null) 
						? attrName.getStringValue() 
						: "noname";
				final String cardStateTag = (attrStateTag != null) 
						? attrStateTag.getStringValue()
						: "";
				// final String cardStateId = (card.getState() != null) ? card.getState().getId().toString() : "null";
				
				result.append("<li>")
					  .append( MessageFormat.format(  
						this.infoFmt4, // "\t  [{0}] {1} {2} ({3}) \n" 
						new Object[] { 
							new Integer(i),
							cardStateTag,
							cardName,
							cardId
						}))
					.append("</li>");
			}
		}
		result.append("</ol>");
		return result;
		 */
	}

	/**
	 * @param id
	 * @return
	 * @throws DataException 
	 */
	private List<Card> getIncompletedResolutions() 
		throws DataException {

		Card card = getCard();
		
		// �������� �������� ���������...
		//
		
		Card compareCard = card;
		List<Card> load = Collections.singletonList(compareCard);
		for(Iterator<ObjectId> iter = attrName4CheckingList.iterator(); iter.hasNext();) {
			ObjectId node = iter.next();
			// TODO: ��������������. ������� �� ��� ��������, � ������ ����������� ��������
			load = loadAllLinkedCardsByAttr(compareCard.getId(), compareCard.getAttributeById(node));
			if(CollectionUtils.isEmpty(load)) {
				return null;
			}
			if(iter.hasNext()) {
				compareCard = load.get(0);
			}
		}
		
		// final ChildrenQueryBase subQuery = getQueryFactory().getChildrenQuery(Card.class, Resolution.class);

		/*final FetchChildrenCards actGetChild 
			= new FetchChildrenCards();
		actGetChild.setCardId(id);
		actGetChild.setLinkAttributeId(this.attrName4CheckingList);
		actGetChild.setColumns( getChildColumns() );*/

		// TODO: (RuSA) getUser -> getSystemUser() ?
		//final List<Card> childList = CardUtils.execSearchCards(actGetChild, getQueryFactory(), getDatabase(), getSystemUser());
		
		/*if (childList == null)
			return null;*/

		// ����� ������ ���������� ��������� ...
		//
		final List<Card> answer = new ArrayList<Card>();
		for ( Card itemCard: load) 
		{
			if (itemCard == null) 
				continue;

			// @note: ����� ��������������� ���������� ���������
			if ( this.isCardAcceptable(itemCard) )
				// OK
				continue;

			// ���������� ���������...
			answer.add(itemCard);
		}

		return (answer.isEmpty()) ? null : answer;
	}

	/**
	 * @param itemCard
	 * @return true, ���� �������� ��������� � ���������� ���������.
	 */
	private boolean isCardAcceptable(Card itemCard) {
		return		(itemCard != null) 
				&& 	(itemCard.getState() != null)
				&&	listOk.contains(itemCard.getState());
	}

	/**
	 * ������� ���� ��� ��������� ��������� �������� � �������� ����������.
	 * @return
	 */
	/*private List<SearchResult.Column> getChildColumns() {

		final List<SearchResult.Column> result = new ArrayList<SearchResult.Column>(5);
		
		final SearchResult.Column col1 = new SearchResult.Column();
			col1.setAttributeId( Attribute.ID_NAME );

		final SearchResult.Column col2 = new SearchResult.Column();
		col2.setAttributeId( Attribute.ID_AUTHOR );
		
		final SearchResult.Column col3 = new SearchResult.Column(); 
			col3.setAttributeId( Attribute.ID_DESCR );
		
		final SearchResult.Column col4 = new SearchResult.Column(); 
			col4.setAttributeId( Attribute.ID_REGION );
		
		final SearchResult.Column col5 = new SearchResult.Column(); 
			col5.setAttributeId( OBJID_STATUS );

		result.add( col1);
		result.add( col2);
		result.add( col3);
		result.add( col4);
		result.add( col5);
		
		return result;
	
	}*/

}
