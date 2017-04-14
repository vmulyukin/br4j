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
package com.aplana.dbmi.module.notif;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.CardLinkLoader;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * This class is just a collection of utility methods which can be used in FreeMarker templates.
 *
 * @author apirozhkov
 * @see FreemarkerConfig
 */
public class TemplateUtil extends DataServiceClient
{
	private static final String DEFAULT_PACKAGE = "com.aplana.dbmi.model";

	protected Log logger = LogFactory.getLog(getClass());

	//TODO Get the link mask from FreeMarker configuration
	private static final String RESOURCE_BUNDLE = "dbmi/mail/notificationConfig.properties";
	private static final String RESOURCE_ID = "link.card";
	private static final String POPUP_RESOURCE_ID = "popup.link.card";
	private String link = null;

	/**
	 * Creates {@link com.aplana.dbmi.model.ObjectId ObjectId} object from two strings.
	 * <p><b>Examples:</b>
	 * <ul><li><code>makeId("StringAttribute", "NAME")</code>
	 * <li><code>makeId("Template", "cms.sitearea")</code></ul>
	 *
	 * @param type Class name of data object. Can be either fully qualified or without package name
	 * (defaults <code>com.aplana.dbmi.model</code>).
	 * @param id Object's ID. Can be either real database ID or entry name in <code>objectids.properties</code>.
	 * @return Created ObjectId or <code>null</code> if type name is wrong.
	 * @see com.aplana.dbmi.model.ObjectId#predefined(Class, String)
	 */
	public ObjectId makeId(String type, String id) {
		if (!type.contains("."))
			type = DEFAULT_PACKAGE + "." + type;
		Class clazz;
		try {
			clazz = Class.forName(type);
		} catch (ClassNotFoundException e) {
			logger.error("Unknown object type: " + type, e);
			return null;
		}
		ObjectId oid = ObjectId.predefined(clazz, id);
		if (oid == null)
			try {
				oid = new ObjectId(clazz, Long.parseLong(id));
			} catch (NumberFormatException e) {
				oid = new ObjectId(clazz, id);
			}
		return oid;
	}

	/**
	 * Generates URL for viewing arbitrary card.
	 * <p>This implementation reads URL template from configuration file
	 * <code>mail/notificationConfig.properties</code> (key <code>link.card</code>).
	 *
	 * @param cardId ID of card which has to be shown on generated URL.
	 * @return String, containing generated URL.
	 * @throws IllegalArgumentException if <code>cardId</code> is not a card's ID.
	 */
	public String makeUrl(ObjectId cardId, String resource_id) {
		if (!Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Only link to card can be generated");
		if (link == null) {
			Properties props = new Properties();
			try {
				props.load(Portal.getFactory().getConfigService().loadConfigFile(RESOURCE_BUNDLE));
			} catch (IOException e) {
				logger.error("Error loading properties from " + RESOURCE_BUNDLE, e);
			}
			link = props.getProperty(resource_id);
		}
		return MessageFormat.format(link, new Object[] { cardId.getId().toString() });
	}
	
	public String makeUrl(ObjectId cardId) {
		return makeUrl(cardId, RESOURCE_ID);
	}
	
	public String makePopupUrl(ObjectId cardId) {
		return makeUrl(cardId, POPUP_RESOURCE_ID);
	}

	/**
	 * Generates URL for viewing arbitrary card.
	 * <p>See {@link #makeUrl(ObjectId)}.
	 *
	 * @param cardId Card which has to be shown on generated URL.
	 * @return String, containing generated URL.
	 */
	public String makeUrl(Card card) {
		return makeUrl(card.getId());
	}
	
	public String makePopupUrl(Card card) {
		return makePopupUrl(card.getId());
	}

	/**
	 * Fetches a card from database.
	 *
	 * @param cardId ID of card to be fetched
	 * @return Card object with all attributes
	 */
	public Card getCard(ObjectId cardId) {
		if (!Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card id");
		try {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
			query.setId(cardId);
			return (Card) getDatabase().executeQuery(getSystemUser(), query);
		} catch (DataException e) {
			logger.error("Error fetching card " + cardId.getId(), e);
			return null;
		}
	}

	/**
	 * Fetches a card linked to given card.
	 * <p>This function suggests that only one card is linked by given attribute.
	 * If more than one linked card will be found, only first will be returned.
	 * Use {@link #getLinkedCards(Card, ObjectId)} to retrieve multiple linked cards.
	 *
	 * @param card Source card
	 * @param attrId ID of {@link CardLinkAttribute} or {@link BackLinkAttribute}
	 * @return Linked card object with all attributes, or null if no card is linked
	 */
	public Card getLinkedCard(Card card, ObjectId attrId) {
		if (!CardLinkAttribute.class.equals(attrId.getType()) &&
				!BackLinkAttribute.class.equals(attrId.getType()) &&
				!TypedCardLinkAttribute.class.equals(attrId.getType()))
			throw new IllegalArgumentException("Not a link attribute id");
		Attribute attr = card.getAttributeById(attrId);
		if (attr == null) {
			logger.warn("Attribute " + attrId.getId() + " doesn't exist in card " + card.getId().getId());
			return null;
		}
		ObjectId cardId = null;
		if (CardLinkAttribute.class.equals(attrId.getType()) || TypedCardLinkAttribute.class.equals(attrId.getType())
				|| DatedTypedCardLinkAttribute.class.equals(attrId.getType())) {
			final CardLinkAttribute linkAttr = (CardLinkAttribute) attr;
			/* >>> (2010/02, RuSA)
			if (linkAttr.getValues() != null && linkAttr.getValues().size() > 1) {
				if (linkAttr.getValues().size() > 1)
					logger.warn(linkAttr.getValues().size() + " cards linked to card " +
							card.getId().getId() + "; using first");
				cardId = ((Card) linkAttr.getValues().iterator().next()).getId();
			}
			 */
			final int c = linkAttr.getLinkedCount();
			if (c > 0) {
				if (c > 1)
					logger.warn(c + " cards linked to card " + card.getId().getId() + "; using first one");
				cardId = linkAttr.getSingleLinkedId();
			}
			// <<< (2010/02, RuSA)
		} else /*if (BackLinkAttribute.class.equals(attrId.getType()))*/ {
			//BackLinkAttribute linkAttr = (BackLinkAttribute) attr;
			ListProject linked = new ListProject(card.getId());
			linked.setAttribute(attrId);
			try {
				ActionQueryBase query = getQueryFactory().getActionQuery(linked);
				query.setAction(linked);
				SearchResult result = (SearchResult) getDatabase().executeQuery(getSystemUser(), query);
				if (result.getCards().size() > 0) {
					if (result.getCards().size() > 1)
						logger.warn(result.getCards().size() + " cards linked to card " +
								card.getId().getId() + "; using first");
					cardId = ((Card) result.getCards().iterator().next()).getId();
				}
			} catch (DataException e) {
				logger.error("Error querying linked cards for card " + card.getId().getId(), e);
			}
		}
		if (cardId == null) {
		    return null;
		}
		return getCard(cardId);
	}

	/**
	 * Fetches a card linked to given card.
	 * <p>This function tries to construct correct attribute ID from given string.
	 * In case of success it calls {@link #getLinkedCard(Card, ObjectId)}.
	 *
	 * @param card Source card
	 * @param attribute Either real database ID of attribute, or entry name in <code>objectids.properties</code>.
	 * @return Linked card object with all attributes, or null if no card is linked
	 */
	public Card getLinkedCard(Card card, String attribute) {
		ObjectId attrId = ObjectId.predefined(CardLinkAttribute.class, attribute);
		if (attrId == null)
			attrId = ObjectId.predefined(BackLinkAttribute.class, attribute);
		if (attrId == null) {
			attrId = new ObjectId(CardLinkAttribute.class, attribute);
			if (card.getAttributeById(attrId) == null) {
				attrId = new ObjectId(BackLinkAttribute.class, attribute);
				if (card.getAttributeById(attrId) == null) {
					logger.warn("Card " + card.getId().getId() + " doesn't have attribute " + attribute);
					return null;
				}
			}
		}
		return getLinkedCard(card, attrId);
	}

	/**
	 * Retrieves cards linked to given card.
	 * <p>Linked cards will contain only those attributes, which are defined in search for given attribute.
	 *
	 * @param card Source card
	 * @param attrId ID of {@link CardLinkAttribute} or {@link BackLinkAttribute}
	 * @return Collection of card objects, or null in case of any errors
	 * @throws DataException
	 */
	public Collection getLinkedCards(Card card, ObjectId attrId) throws DataException {
		if (!CardLinkAttribute.class.equals(attrId.getType()) &&
				!BackLinkAttribute.class.equals(attrId.getType()) && !TypedCardLinkAttribute.class.equals(attrId.getType()))
			throw new IllegalArgumentException("Not a link attribute id");
		final Attribute attr = card.getAttributeById(attrId);
		if (attr == null) {
			logger.warn("Attribute " + attrId.getId() + " doesn't exist in card " + card.getId().getId());
			return null;
		}
		if (CardLinkAttribute.class.equals(attrId.getType()) || TypedCardLinkAttribute.class.equals(attrId.getType())
				|| DatedTypedCardLinkAttribute.class.equals(attrId.getType()))
		{
			// (2010/02, RuSA) OLD: return ((CardLinkAttribute) attr).getValues();
			// return ((CardLinkAttribute) attr).getLinkedCards();
			return CardLinkLoader.loadCardsByLink( (CardLinkAttribute) attr,
						null, getSystemUser(), getQueryFactory(), getDatabase() );
		};

		//if (BackLinkAttribute.class.equals(attrId.getType())) {
			ListProject linked = new ListProject(card.getId());
			linked.setAttribute(attrId);
			try {
				final ActionQueryBase query = getQueryFactory().getActionQuery(linked);
				query.setAction(linked);
				final SearchResult result = (SearchResult) getDatabase().executeQuery(getSystemUser(), query);
				return result.getCards();
			} catch (DataException e) {
				logger.error("Error querying linked cards for card " + card.getId().getId(), e);
				return null;
			}
		//}
	}

	/**
	 * Retrieves cards linked to given card.
	 * <p>This function tries to construct correct attribute ID from given string.
	 * In case of success it calls {@link #getLinkedCards(Card, ObjectId)}.
	 *
	 * @param card Source card
	 * @param attribute Either real database ID of attribute, or entry name in <code>objectids.properties</code>.
	 * @return Collection of card objects, or null in case of any errors
	 * @throws DataException
	 */
	public Collection getLinkedCards(Card card, String attribute) throws DataException {
		ObjectId attrId = ObjectId.predefined(CardLinkAttribute.class, attribute);
		if (attrId == null)
			attrId = ObjectId.predefined(BackLinkAttribute.class, attribute);
		if (attrId == null)
			attrId = ObjectId.predefined(TypedCardLinkAttribute.class, attribute);
		if (attrId == null) {
			attrId = new ObjectId(CardLinkAttribute.class, attribute);
			if (card.getAttributeById(attrId) == null) {
				attrId = new ObjectId(BackLinkAttribute.class, attribute);
				if (card.getAttributeById(attrId) == null) {
					logger.warn("Card " + card.getId().getId() + " doesn't have attribute " + attribute);
					return null;
				}
			}
		}
		return getLinkedCards(card, attrId);
	}

	public String getAnswerString (Card card) {
		return getAnswerString (card, false);
	}

	public String getAnswerString (Card card, boolean single) {
		String answer="";
		ObjectId attrId = makeId("TypedCardLinkAttribute", "jbr.relatdocs");
		TypedCardLinkAttribute typedAttr = (TypedCardLinkAttribute)card.getAttributeById(attrId);
		if (typedAttr == null) {
		    return "";
		}
		Map types = typedAttr.getTypes();
		try {
			Collection cards = getLinkedCards(card, ((CardLinkAttribute)typedAttr).getId());
			if (single == true && cards.size()>1) {
				Attribute theme = card.getAttributeById(makeId("StringAttribute", "LETTER THEME"));
				if (theme != null) {
					return theme.getStringValue();
				} else {
					return "";
				}
			}
			for (Object c : cards) {
				//Card c = (Card)c1;
				Long type = (Long)types.get(((Card)c).getId().getId());
				if (Long.valueOf(1502).equals(type)) {
					Card linkedCard = getCard(((Card)c).getId());
					String ans = "� ����� �� " + (linkedCard.getAttributeById(ObjectId.predefined(StringAttribute.class, "jbr.incoming.outnumber"))).getStringValue()
								 + " �� " + (linkedCard.getAttributeById(ObjectId.predefined(DateAttribute.class, "jbr.incoming.outdate"))).getStringValue();
					if (cards.size()>1) {
						ans = "<p> " + ans + "</p>";
					}
					answer = answer + ans;
				}
			}
		} catch (DataException e) {
			logger.error("Error querying linked cards for card " + card.getId().getId(), e);
			return null;
		}
		if (answer.equals("")) {
			answer = "��������� ��������";
		}
		return answer;
	}
	
	public Card getNewMainConsiderationCard(Card card,String attrId){
		DatedTypedCardLinkAttribute attribute = (DatedTypedCardLinkAttribute)card.getAttributeById(ObjectId.predefined(DatedTypedCardLinkAttribute.class, attrId));
		for(ObjectId id: attribute.getIdsLinked()){
			if(attribute.getCardType(id) != null && 
					attribute.getCardType(id).equals(ObjectId.predefined(ReferenceValue.class, "jbr.commission.control.yes"))){
				return getCard(id);
			}
		}
		return null;
	}
}
