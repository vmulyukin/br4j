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
package com.aplana.dbmi.card.download;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.card.*;
import com.aplana.dbmi.card.hierarchy.CardsJSActionHelperServlet;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * search recipient card data
 */
public class DownloadCardsServlet extends CardsJSActionHelperServlet {

    private final static DateFormat DATEFORMAT = new SimpleDateFormat("dd-MM-yyyy");

    private static final ObjectId ATTR_METHOD =
            ObjectId.predefined(ListAttribute.class, "jbr.distributionItem.method");
    private static final ObjectId ATTR_RECIPIENT =
            ObjectId.predefined(CardLinkAttribute.class, "jbr.distributionItem.recipient");
    private static final ObjectId ATTR_REG_DATE =
            ObjectId.predefined(DateAttribute.class, "jbr.maindoc.regdate");
    private static final ObjectId ATTR_REG_NUMBER =
            ObjectId.predefined(StringAttribute.class, "jbr.maindoc.regnum");

    /**
     * convert data that necessary to download cards to the JSON format
     *
     * @param recipientCardMap map of the loaded recipient cards
     * @return JSON array of the objects that holds card id,  recipient id,
     *         and download card file name
     */
    protected JSONArray getJSONDownloadValues(
            Map<ObjectId,
            Card> recipientCardMap,
            HttpServletRequest request) throws DataException{

        JSONArray result = new JSONArray();
        if (recipientCardMap == null || recipientCardMap.isEmpty()) {
            return result;
        }
        try {

            JSONObject jso;
            String cardFileName;
            ObjectId itemId;
            Card item;

            for (Iterator<ObjectId> i = recipientCardMap.keySet().iterator(); i.hasNext();) {
                itemId = i.next();
                item = recipientCardMap.get(itemId);
                cardFileName = getCardFileName(item, request);
                try {

                    cardFileName = URLEncoder.encode(cardFileName, "UTF-8");

                } catch (UnsupportedEncodingException uee) {
                    logger.error("Can not create encode card file name");

                }

                jso = new JSONObject();
                jso.append(DistributionHierarchicalCardLinkAttributeViewer.JSON_CARD_ID, getActiveCardId(request));
                jso.append(DistributionHierarchicalCardLinkAttributeViewer.JSON_RECIPIENT_ID, itemId.getId());
                jso.append(DistributionHierarchicalCardLinkAttributeViewer.JSON_FILE_NAME, cardFileName);
                result.put(jso);

            }

        } catch (JSONException jse) {
            logger.error("Can not create JSON array");
            throw new DataException(jse.getMessage());
        }

        return result;
    }

    protected Map<ObjectId, Card> getCards(List ids, HttpServletRequest request) throws DataException, ServiceException {
        Search search = new Search();
        search.setByAttributes(false);
        search.setByCode(true);
        search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(ids));
        search.setColumns(new ArrayList(3));
        SearchResult.Column col = new SearchResult.Column();
        col.setAttributeId(ATTR_METHOD);
        search.getColumns().add(col);
        col = new SearchResult.Column();
        col.setAttributeId(Card.ATTR_STATE);
        search.getColumns().add(col);
        col = new SearchResult.Column();
        col.setAttributeId(ATTR_RECIPIENT);
        col.setLabelAttrId(Attribute.ID_NAME);
        search.getColumns().add(col);
        SearchResult result =
                (SearchResult) getDataServiceBean(request).doAction(search);

        HashMap map = new HashMap(ids.size());
        for (Iterator itr = result.getCards().iterator(); itr.hasNext();) {
            Card card = (Card) itr.next();
            map.put(card.getId(), card);
        }
        return map;
    }

    private String getRegNumberString(HttpServletRequest request) {

        CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
        Card c = sessionBean.getActiveCard();
        StringAttribute sa = (StringAttribute) c.getAttributeById(ATTR_REG_NUMBER);
        if (sa != null && sa.getValue() != null) {
            return sa.getValue();
        }
        return "";

    }

    private String getRegDateString(HttpServletRequest request) {

        CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
        Card c = sessionBean.getActiveCard();
        DateAttribute da = (DateAttribute) c.getAttributeById(ATTR_REG_DATE);
        if (da != null && da.getValue() != null) {
            return DATEFORMAT.format(da.getValue());
        }
        return "";

    }

    private String getRecipientStr(Card recipientcard) {
        return recipientcard.getAttributeById(ATTR_RECIPIENT).getStringValue();
    }

    private String getCardFileName(Card recipientcard, HttpServletRequest request) {

        String recipient = getRecipientStr(recipientcard);
        String regNumber = getRegNumberString(request);
        String regDate = getRegDateString(request);
        StringBuffer stb = new StringBuffer();
        //pattern to remove reserved characters by file naming convention
        Pattern p = Pattern.compile("([<>:\"/\\\\|?* ])");
        Matcher m = p.matcher(recipient);
        recipient = m.replaceAll("_");

        stb.append(regNumber).append("_").append(regDate).append("_").append(recipient).append(".xml");

        String result = stb.toString();

        return result;

    }

}
