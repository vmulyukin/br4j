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
package com.aplana.dbmi.card.actionhandler.jbr;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.DistributionHierarchicalCardLinkAttributeViewer;
import com.aplana.dbmi.card.Parametrized;
import com.aplana.dbmi.card.PortletFormManager;
import com.aplana.dbmi.card.PortletFormManagerAware;
import com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionHandler;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

/**
 */
// FIXME: (N.Zhegalin) Probably this class is not used now
@Deprecated
public class DownloadFilesHandler extends CardPortletAttributeEditorActionHandler
        implements PortletFormManagerAware, Parametrized {

    private final static DateFormat DATEFORMAT = new SimpleDateFormat("dd-MM-yyyy");

    private static final ObjectId ATTR_METHOD =
            ObjectId.predefined(ListAttribute.class, "jbr.distributionItem.method");
    private static final ObjectId ATTR_RECIPIENT =
            ObjectId.predefined(CardLinkAttribute.class, "jbr.distributionItem.recipient");
    private static final ObjectId ATTR_REG_DATE =
            ObjectId.predefined(DateAttribute.class, "jbr.maindoc.regdate");
    private static final ObjectId ATTR_REG_NUMBER =
            ObjectId.predefined(StringAttribute.class, "jbr.maindoc.regnum");

    private PortletFormManager portletFormManager;

    public void setPortletFormManager(PortletFormManager portletFormManager) {
        this.portletFormManager = portletFormManager;
    }

    public void setParameter(String name, String value) {
        // TODO Auto-generated method stub
    }

    @Override
    /**
     * add to request attributes flag to enable downloading cards
     * add data that necessary to download cards
     */
    protected void process(Attribute attr, List<ObjectId> cardIds,
                           ActionRequest request, ActionResponse response)
            throws DataException {

        if (cardIds == null || cardIds.size() == 0)
            cardIds = new ArrayList(((CardLinkAttribute) attr).getIdsLinked());
        Map<ObjectId, Card> cardMap;
        try {
            cardMap = getCards(cardIds);
        } catch (Exception e) {
            getCardPortletSessionBean().setMessage(e.getMessage());
            logger.error("Can not get recipient cards by card ids");
            throw new DataException(e.getMessage());
        }

        request.getPortletSession().setAttribute(
                DistributionHierarchicalCardLinkAttributeViewer.DISTRIBUTION_PARAM_STARTONLOAD,
                true);
        request.getPortletSession().setAttribute(
                DistributionHierarchicalCardLinkAttributeViewer.DISTRIBUTION_PARAM_DOWNLOAD_VALUES,
                getJSONDownloadValues(cardMap));

    }

    /**
     * convert data that necessary to download cards to the JSON format
     *
     * @param recipientCardMap map of the loaded recipient cards
     * @return JSON array of the objects that holds card id,  recipient id,
     *         and download card file name
     */
    private JSONArray getJSONDownloadValues(Map<ObjectId, Card> recipientCardMap) throws DataException{
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
                cardFileName = getCardFileName(item);
                try {

                    cardFileName = URLEncoder.encode(cardFileName, "UTF-8");

                } catch (UnsupportedEncodingException uee) {
                    logger.error("Can not create encode card file name");

                }

                jso = new JSONObject();
                jso.append(DistributionHierarchicalCardLinkAttributeViewer.JSON_CARD_ID, getActiveCardId());
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

    private Map<ObjectId, Card> getCards(List ids) throws DataException, ServiceException {
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
        SearchResult result = (SearchResult) serviceBean.doAction(search);

        HashMap map = new HashMap(ids.size());
        for (Iterator itr = result.getCards().iterator(); itr.hasNext();) {
            Card card = (Card) itr.next();
            map.put(card.getId(), card);
        }
        return map;
    }

    private String getRegNumberString() {

        CardPortletSessionBean sessionBean = getCardPortletSessionBean();
        Card c = sessionBean.getActiveCard();
        StringAttribute sa = (StringAttribute) c.getAttributeById(ATTR_REG_NUMBER);
        if (sa != null && sa.getValue() != null) {
            return sa.getValue();
        }
        return "";

    }

    private String getRegDateString() {

        CardPortletSessionBean sessionBean = getCardPortletSessionBean();
        Card c = sessionBean.getActiveCard();
        DateAttribute da = (DateAttribute) c.getAttributeById(ATTR_REG_DATE);
        if (da != null && da.getValue() != null) {
            return DATEFORMAT.format(da.getValue());
        }
        return "";

    }

    private Long getActiveCardId() {

        CardPortletSessionBean sessionBean = getCardPortletSessionBean();
        Card c = sessionBean.getActiveCard();
        return (Long) c.getId().getId();

    }

    private String getRecipientStr(Card recipientcard) {
        return recipientcard.getAttributeById(ATTR_RECIPIENT).getStringValue();
    }

    private String getCardFileName(Card recipientcard) {

        String recipient = getRecipientStr(recipientcard);
        String regNumber = getRegNumberString();
        String regDate = getRegDateString();
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

