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
package com.aplana.medo.cards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

/**
 * One of <code>CardHandler</code> that allows to search 'Outcome' card.
 */
public class OutcomeExportCardHandler extends ExportCardHandler {

    public static final ObjectId JBR_FILES = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
    public static final ObjectId DATE_SIGNING = ObjectId.predefined(
	    DateAttribute.class, "jbr.outcoming.signdate");
    public static final ObjectId DATE_CREATED = ObjectId.predefined(
	    DateAttribute.class, "created");
    public static final ObjectId REG_NUMBER = ObjectId.predefined(
	    StringAttribute.class, "regnumber");
    public static final ObjectId SHORT_DESCRIPTION = ObjectId.predefined(
	    TextAttribute.class, "jbr.document.title");
    public static final ObjectId RELATED_DOC = ObjectId.predefined(
	    TypedCardLinkAttribute.class, "jbr.relatdocs");
    public static final ObjectId DSP = ObjectId.predefined(ListAttribute.class, "jbr.dsp");

    public static final ObjectId TYPE_LINK_INRESP = ObjectId.predefined(
    	    ReferenceValue.class, "jbr.inResponse"); // "� ����� ��"
    public static final ObjectId TYPE_LINK_RESP = ObjectId.predefined(
    	    ReferenceValue.class, "jbr.Response"); // "�����"
    public static final ObjectId TYPE_LINK_EXEC = ObjectId.predefined(
    	    ReferenceValue.class, "jbr.Execute"); // "����������"
    public static final ObjectId TYPE_LINK_LINKED = ObjectId.predefined(
    	    ReferenceValue.class, "jbr.medo_og.informationResponseDocType"); // "������ �"
   public static final ObjectId TYPE_LINK_INEXEC = ObjectId.predefined(
    	    ReferenceValue.class, "jbr.inExecute"); // "�� ����������"
   public static final ObjectId TYPE_LINK_INLIKED = ObjectId.predefined(
    	    ReferenceValue.class, "jbr.inLikedWith"); // "� ����� �"

   public static final ObjectId DSP_YES = ObjectId.predefined(ReferenceValue.class, "jbr.isDsp");
   public static final ObjectId ACTIVE_FILE_STATE = ObjectId.predefined(CardState.class, "active");
   public static final ObjectId IS_PRIME_ATTR_ID = ObjectId.predefined(ListAttribute.class, "jbr.prime");
   public static final ObjectId SOURCE_FILE_FOR_STAMP_ATTR_ID = ObjectId.predefined(ListAttribute.class, "jbr.sourceFileForStamp");
   public static final ObjectId YES_ID = ObjectId.predefined(ReferenceValue.class, "jbr.incoming.control.yes");

    private String id = null;
    private TextAttribute theme = null;
    private StringAttribute regnumber = null;
    private DateAttribute date_signing = null;
    private ObjectId[] linkedFileCards = {};
    private TypedCardLinkAttribute relatedDoc = null;
    private boolean isDsp = false;
    private boolean sendSourceAttachment;

    public OutcomeExportCardHandler(ObjectId card_id, boolean sendSourceAttachment) throws DataException, ServiceException {
	this.serviceBean = getServiceBean();
	card = (Card) serviceBean.getById(card_id);
	if (card != null) {
	    // Generate UID ////////////////////////////////
	   /* StringAttribute uidAttribute = (StringAttribute) card
		    .getAttributeById(DistributionItemCardHandler.UUID_ATTRIBUTE_ID);
	    uid = uidAttribute.getValue();
	    if (uid == null || "".equals(uid)) {
		uid = UUID.randomUUID().toString();
		uidAttribute.setValue(uid);
		saveCard();
	    }*/
	    // ///////////////////////////////////////////////
		this.sendSourceAttachment = sendSourceAttachment;
	    id = card.getId().getId().toString();
	    CardLinkAttribute doclinks = (CardLinkAttribute) card
		.getAttributeById(JBR_FILES);
	    if (doclinks != null)
		linkedFileCards = filterDocLinks(doclinks.getIdsArray());
	    theme = (TextAttribute)card.getAttributeById(SHORT_DESCRIPTION);
	    if (theme.getValue() == null) {
		theme.setValue("");
	    }
	    regnumber = (StringAttribute)card.getAttributeById(REG_NUMBER);
	    if (regnumber.getValue() == null) {
		    regnumber.setValue("0");
	    }
	    date_signing = (DateAttribute)card.getAttributeById(DATE_SIGNING);
	    if (date_signing.getValue() == null) {
		    date_signing = (DateAttribute) card
		    .getAttributeById(DATE_CREATED);
	    }
	    relatedDoc = (TypedCardLinkAttribute) card
		.getAttributeById(RELATED_DOC);

	    ListAttribute dspAttribute = (ListAttribute) card.getAttributeById(DSP);
	    if (dspAttribute != null) {
	    	ReferenceValue dspValue = dspAttribute.getValue();
	    	isDsp = DSP_YES.equals(dspValue.getId());
	    }
	} else
	    throw new CardException("jbr.medo.card.outcomeexport.notFound");
	logger.info("Create object OutcomeExport with current parameters: "
		+ getParameterValuesLog());
    }


    private ObjectId[] filterDocLinks(ObjectId[] idsArray) throws DataException, ServiceException {
        if (idsArray == null || idsArray.length == 0) {
            return new ObjectId[0];
        }

        Search fetcher = new Search();
        fetcher.setByCode(true);
        String ids = ObjectIdUtils.numericIdsToCommaDelimitedString(Arrays.asList(idsArray));
        fetcher.setWords(ids);
        Collection<Column> columns = new ArrayList<Column>();
        Column col = new Column();
        col.setAttributeId(Card.ATTR_STATE);
        columns.add(col);

        //��������� ���������� �� �������� "�������� ��������", ��� �� ��� ��� ������� � ������
        Column col2 = new Column();
        col2.setAttributeId(IS_PRIME_ATTR_ID);
        col2.setSortable(true);
        col2.setSorting(SearchResult.Column.SORT_ASCENDING);
        columns.add(col2);

		if (sendSourceAttachment == false) {
			//��������� �������� �������� "�������� ���� ��� ��������� ���.������"
			Column col3 = new Column();
			col3.setAttributeId(SOURCE_FILE_FOR_STAMP_ATTR_ID);
			columns.add(col3);
		}

        fetcher.setColumns(columns);
        SearchResult searchResult = (SearchResult)serviceBean.doAction(fetcher);
        List<Card> cards = searchResult.getCards();

        List<ObjectId> filteredCardIds = new ArrayList<ObjectId>();
        for (Card foundCard : cards){
            ListAttribute attr = (ListAttribute)foundCard.getAttributeById(Card.ATTR_STATE);
            if (attr == null) {
                continue;
            }
            ReferenceValue value = attr.getValue();
            if (value == null)
                continue;

			if (sendSourceAttachment == false) {
				//���� "�������� ���� ��� ��������� ���.������" = "��", ���������� ��� ��������
				ListAttribute sourceFlag = (ListAttribute)foundCard.getAttributeById(SOURCE_FILE_FOR_STAMP_ATTR_ID);
				if (sourceFlag != null && sourceFlag.getValue() != null && sourceFlag.getValue().getId().equals(YES_ID)) {
					continue;
				}
			}

            if (ACTIVE_FILE_STATE.getId().equals(value.getId().getId())) {
                filteredCardIds.add(foundCard.getId());
            }
        }
        return filteredCardIds.toArray(new ObjectId[filteredCardIds.size()]);
    }

    /**
     * @return the id
     */
    public String getId() {
	return this.id;
    }

    /**
     * @return the theme
     */
    public String getTheme() {
	if (this.theme == null) return null;
        return this.theme.getValue();
    }

    /**
     * @return the regnumber
     */
    public String getRegNumber() {
	if (this.regnumber == null) return null;
        return this.regnumber.getValue();
    }

    /**
     * @return the date_signing
     */
    public Date getDateSigning() {
	if (this.date_signing == null) return null;
        return this.date_signing.getValue();
    }

    public Card getCard() throws CardException {
	if (card != null)
	return card;
	throw new CardException("jbr.medo.card.outcomeexport.notFound");
    }

    public ObjectId[] getLinkedFileCards() {
	return linkedFileCards;
    }

    /**
     * @return the relatedDoc
     */
    public TypedCardLinkAttribute getRelatedDoc() {
        return this.relatedDoc;
    }

    /**
     * Returns id of card found according to current state of class. If there
     * was found more than one card the first will be returned.
     *
     * @return id of searched card
     * @throws CardException
     *                 if card was not found or other error
     */
    @Override
    public long getCardId() throws CardException {
	if (card != null ) return (Long)card.getId().getId();
	throw new CardException("jbr.medo.card.outcomeexport.notFound");
    }

    public boolean isDsp() {
    	return this.isDsp;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aplana.medo.cards.CardHandler#getParameterValuesLog()
     */
    @Override
    protected String getParameterValuesLog() {
	StringBuilder logBuilder = new StringBuilder();
	logBuilder.append(String.format("id='%s', ", id));
	logBuilder.append(String.format("theme='%s', ", theme));
	logBuilder.append(String.format("regnumber='%s', ", regnumber));
	logBuilder.append(String.format("date signing='%s', ", date_signing));
	return logBuilder.toString();
    }

    /*
     * @return a card on cardLinkId and followingCardId
     */
    public static Card findCard(ObjectId cardLinkId, ObjectId followingCardId) throws DataException, ServiceException {
	Card card = search(cardLinkId, followingCardId);
	if (card == null) {
	    throw new CardException("jbr.medo.card.outcomeexport.notFound");
	}
	loggerSt.info("Found card: " + card.getId());
	return card;
    }

    private static Card search(ObjectId cardLinkId, ObjectId followingCardId) throws DataException, ServiceException {
	serviceBeanStatic = getServiceBeanStatic();
	CardLinkAttribute a = (CardLinkAttribute) serviceBeanStatic
	.getById(cardLinkId);
	Search search = new Search();
	search.addCardLinkAttribute(a.getId(), followingCardId);
	search.setByAttributes(true);
	try {
	    SearchResult result = (SearchResult) serviceBeanStatic
		    .doAction(search);
	    @SuppressWarnings("unchecked")
	    List<Card> cards = result.getCards();
	    if (cards.size() > 1) {
		loggerSt.error("Should be found only one OutcomeExport card ! cardId: " + followingCardId.getId() + "; ", new CardException());
		//throw new CardException();
	    }
	    if (cards.size() == 0) {
		    loggerSt
			    .error("Not found outcome card ! Iteration is interrupted, cardId: " + followingCardId.getId() + "; ");
		    throw new CardException("jbr.medo.outcomeexport.notFound (cardssize = 0)");
	    }

	    Card cardId = cards.get(0);
	    cardId = (Card) serviceBeanStatic.getById(cardId.getId()); // � Search � ��������������� ���������, �������� �������� ��������
	    return cardId;
	} catch (DataException ex) {
	    throw new CardException("jbr.medo.card.outcomeexport.searchFailed", ex);
	} catch (ServiceException ex) {
	    throw new CardException("jbr.medo.card.outcomeexport.searchFailed", ex);
	}
    }
}
