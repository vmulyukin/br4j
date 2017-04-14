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

import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.jbr.action.CheckCardNumber.CheckCardNumberResult;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.MessageException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.BasePropertySelector;
import com.aplana.dbmi.service.impl.Validator;

/**
 * @author RAbdullin   . (2009/12/14, RuSA)
 *              c.
 */
public class CheckCardNumber extends ProcessCard /*implements Validator*/ {

	private static final long serialVersionUID = 1L;

	/**
	 *    "(true)  ,  - 
	 *   NULL", 
	 * - -   (false).  
	 */
	final static String PARAM_THROWIFNULL = "throw_if_incomes_null";

	static final String PARAM_ATTR_TEST = "attr_test";

	/**
	 * ID   " "  " ".
	 */
	final static String RESNAME_NUMOUT = "jbr.incoming.outnumber";  // 'JBR_REGD_NUMOUT'
	final static String RESNAME_DATEOUT = "jbr.incoming.outdate";   // 'JBR_REGD_DATEOUT'
	final static String RESNAME_SENDER = "jbr.incoming.sender"; // 'JBR_INFD_SENDER'
	final static String RESNAME_ZONEDOW = "jbr.zoneDOW"; // "JBR_ZONE_DOW"
	// final static String RESNAME_ORG = "jbr.incoming.organization"; // 'JBR_PERS_ORG'

	static private ObjectId ATTRID_NUMOUT; 
	static private ObjectId ATTRID_DATEOUT;
	static private ObjectId ATTRID_SENDER;
	static private ObjectId ATTRID_ZONEDOW;
	// static private ObjectId ATTRID_ORG;

	//    
	protected boolean checkEnable = true;

	
	private final List<BasePropertySelector> conditions = new ArrayList<BasePropertySelector>();
	/**
	 *    ,      
	 *       ,   
	 *  ,        :
	 * private static  final ObjectId ATTRID_X = ObjectId.predefined( Y.class, "X");  
	 */
	static void checkInitIds() throws DataException
	{
		if (ATTRID_NUMOUT == null)
			ATTRID_NUMOUT =	checkGetIdByResName( RESNAME_NUMOUT, StringAttribute.class);

		if (ATTRID_DATEOUT == null)
			ATTRID_DATEOUT = checkGetIdByResName( RESNAME_DATEOUT, DateAttribute.class);

		if (ATTRID_SENDER == null)
			ATTRID_SENDER = checkGetIdByResName( RESNAME_SENDER, CardLinkAttribute.class);

		if (ATTRID_ZONEDOW == null)
			ATTRID_ZONEDOW = checkGetIdByResName( RESNAME_ZONEDOW, CardLinkAttribute.class);

		//if (ATTRID_ORG == null)
		//	ATTRID_ORG = checkGetIdByResName( RESNAME_ORG, CardLinkAttribute.class);
	}


	/*
	 *      .
	 * @see com.aplana.dbmi.service.impl.ProcessorBase#process()
	 */
	@Override
	public Object process() throws DataException {

		final Card card = super.getCard();
		if (card == null)
			return null;

		if (!checkConditions(conditions, card)) 
			return null;


		checkInitIds();

		final boolean throwIfNull = super.getBooleanParameter(PARAM_THROWIFNULL, false);

		final com.aplana.dbmi.jbr.action.CheckCardNumber action = 
					new com.aplana.dbmi.jbr.action.CheckCardNumber();

		action.setNumberCanBeNullOrEmpty( super.getBooleanParameter("numberCanBeNullOrEmpty", false));
		action.setDateCanBeNull( super.getBooleanParameter("dateCanBeNull", true));
		action.setOrganizationCanBeNull( super.getBooleanParameter("organizationCanBeNull", true));

		action.setCardId(card.getId());

		final StringAttribute sa = (StringAttribute)card.getAttributeById(ATTRID_NUMOUT);
		// (!) (2011/03/21, GoRik)        .
		if ( throwIfNull && (sa == null || sa.isEmpty()) ) 
			throw new DataException("jbr.card.check.incoming.nonumber");
		action.setNumber( sa.getStringValue());

		// (!) (2010/06/04, RuSA)        .
		final DateAttribute attrdate = (DateAttribute) card.getAttributeById(ATTRID_DATEOUT);
		if ( throwIfNull && (attrdate == null /*|| attrdate.isEmpty()*/) )
			throw new DataException("jbr.card.check.incoming.nodate");
		action.setDate( attrdate.getValue());

		// 
		final ObjectId org = getSenderOrganization(card);
		action.setOrganization( org );

		final ObjectId zone_dow = getZoneDOW(card);
		action.setZoneDOW( zone_dow );

		//  Id-   ...
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		//final String result = (String) getDatabase().executeQuery( getUser(), query);
		final CheckCardNumberResult result =  (CheckCardNumberResult) getDatabase().executeQuery( getSystemUser(), query);
		if (	result != null 
				&& result.getProblemCardIds() != null
				&& !result.getProblemCardIds().isEmpty())
			//throw new DataException("jbr.card.check.incoming.hasthesame_1", new Object[] {result} );
			throw new MessageException("jbr.card.check.incoming.hasthesame", result.getProblemCardIds() );

		// 	throw new DataException("jbr.card.check.incoming.success");		
		return null;
	}

	
	@Override
	public void setParameter(String name, String value) {
		if (name == null || value == null)
			return;
		// final int iStart = value.toLowerCase().indexOf("jbr.current_date");
		if (PARAM_ATTR_TEST.equalsIgnoreCase(name)) {
			
			try {
				final AttributeSelector selector = AttributeSelector.createSelector(value);
				if (selector != null)
					this.conditions.add(selector);
			} catch (DataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else 
			super.setParameter(name, value);
	}

	/**
	 * @param card
	 * @return   
	 */
	private ObjectId getSenderOrganization(Card card) {
		if (card == null)
			return null;

		//  ...
		final CardLinkAttribute sender =  card.getCardLinkAttributeById( ATTRID_SENDER);
		if (sender == null || sender.getLinkedCount() < 1) 
			return null;
		final ObjectId senderId = sender.getIdsArray()[0];
		return senderId; 

		/*
		final ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
		query.setId( senderId);
		final Card cardSender = (Card) getDatabase().executeQuery( this.getUser(), query);

		final CardLinkAttribute orgsList = cardSender.getCardLinkAttributeById( ATTRID_ORG);
		if (orgsList == null || orgsList.getLinkedCount() < 1) 
			return null;
		return orgsList.getIdsArray()[0];
		*/

		/*
		query.setId( org.getIdsArray()[0] );
		final Card cardOrg = (Card) getDatabase().executeQuery( this.getUser(), query);

		final StringAttribute attrName = cardOrg.getAttributeById(Attribute.ID_NAME);
		return (attrName == null) ? null : attrName.getStringValue();
		*/

		/*
		//  Sender   search- ORG.NAME
		final Search search = new Search();
		search.setByCode(true);
		search.setByAttributes(false);
		search.setWords( String.valueOf( (Long)senderId.getId() ));
		// search.setTemplates(new ArrayList());
		// search.getTemplates().add(Template.createFromId(...));
		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		search.setColumns(columns);

		final SearchResult.Column colOrg = new SearchResult.Column();
		columns.add( colOrg);
		colOrg.setAttributeId( ATTRID_ORG);
		colOrg.setLabelAttrId(Attribute.ID_NAME);

		final ActionQueryBase query = getQueryFactory().getActionQuery(search);
		final SearchResult result = (SearchResult) getDatabase().executeQuery( this.getUser(), query);

		if (result == null || result.getCards() == null || result.getCards().isEmpty())
			return null;
		final Card cardSender = (Card) result.getCards().get(0);

		final CardLinkAttribute org = cardSender.getCardLinkAttributeById( ATTRID_ORG);
		if (org == null || org.getLinkedCount() < 1) 
			return null;
		return org.getStringValue();
		*/
	}
	
	private ObjectId getZoneDOW(Card card){
		final CardLinkAttribute sender =  card.getCardLinkAttributeById(ATTRID_ZONEDOW);
		if (sender == null || sender.getLinkedCount() < 1) 
			return null;
		final ObjectId senderId = sender.getIdsArray()[0];
		return senderId; 
	}
	
	private boolean checkConditions(List<BasePropertySelector> conds, Card c)
		throws DataException {
		/*
		 *   : attrList = (ListAttribute)
		 * card.getAttributeById( ((AttributeSelector) cond).attrId);
		 * attrList.getReference() = ObjectId(id='ADMIN_26973',
		 * type=com.apana.dbmi.model.Reference); attrList.getValue() =
		 * ReferenceValue( id=ObjectId(id=1433,
		 * type=com.apana.dbmi.model.ReferenceValue), active=false,
		 * children=null, ..., valueEn="No", valueRu="")
		 */
		if (conds == null || c == null)
			return true;
		boolean cardFetched = false;
		for (BasePropertySelector cond : conds) {
			if (!cardFetched) {
				//      ...
				if ((cond instanceof AttributeSelector)
						&& null == c
								.getAttributeById(((AttributeSelector) cond)
										.getAttrId())) {
					c = super.loadCardById(c.getId());
					cardFetched = true;
				}
			}
			final boolean ok = cond.satisfies(c);
			if (!ok) {
				logger.info("Card " + c.getId().getId()
						+ " did not need to check for repeat because  {" + cond
						+ "} ");
				return false;
			}
		}
		return true;
		}
}
