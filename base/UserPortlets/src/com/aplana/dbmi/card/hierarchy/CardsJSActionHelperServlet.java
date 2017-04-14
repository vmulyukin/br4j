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
package com.aplana.dbmi.card.hierarchy;

import com.aplana.dbmi.ajax.AbstractDBMIAjaxServlet;
import com.aplana.dbmi.card.*;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import org.json.JSONArray;
import org.json.JSONWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * class that help JS action extract selected cards for JS action procession
 */
public abstract class CardsJSActionHelperServlet extends AbstractDBMIAjaxServlet {

    protected void generateResponse(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        String attrCode = request.getParameter(DistributionHierarchicalCardLinkAttributeViewer.PARAM_ATTR_CODE);

        Attribute attribute = getAttribute(attrCode, request);

        List<ObjectId> cardIds = getCardIds(request, attribute);
        try {

            process(attribute, cardIds, request, response);

        } catch (DataException de){
            logger.error("Error generating response", de);
            throw new ServletException(de);
        }
    }

    /**
     * extract from request selected cards ids
     * @param request HttpServletRequest
     * @param attribute edited attribute
     * @return List<ObjectId> list of the selected card ids
     */
    public static List<ObjectId> getCardIds(HttpServletRequest request, Attribute attribute) {
        String selectedCards = request.getParameter(getSelectedCardIdsParameterName(attribute));
        return (selectedCards != null && !"".equals(selectedCards))
            ? ObjectIdUtils.commaDelimitedStringToNumericIds(selectedCards, Card.class)
            : new ArrayList<ObjectId>(0);
    }

    /**
     * get selected card ids as comma separated String
     * @param attr edited attribute
     * @return comma separated String of card ids
     */
    public static String getSelectedCardIdsParameterName(Attribute attr) {
        return JspAttributeEditor.getAttrHtmlId(attr) + "_selectedItems";
    }

    protected Long getActiveCardId(HttpServletRequest request) {

        CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
        Card c = sessionBean.getActiveCard();
        return (Long) c.getId().getId();

    }


    protected CardPortletCardInfo getActiveCardInfo(HttpServletRequest request) {

        CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);

        return sessionBean.getActiveCardInfo();
    }

    /**
     * try to extract CardPortlet Session bean
     *
     * @param request HttpServletRequest
     * @return CardPortletSessionBean passed as parameter in the HttpServletRequest
     */
    protected CardPortletSessionBean getCardPortletSessionBean(HttpServletRequest request) {

        String namespace = request.getParameter(DistributionHierarchicalCardLinkAttributeViewer.PARAM_NAMESPACE);

        return CardPortlet.getSessionBean(request, namespace);

    }

    /**
     * extract AttributeId for attribute that edited by DistributionHierarchicalCardLinkAttributeViewer
     * @param attrCode attr code of the attribute
     * @param request HttpServletRequest
     * @return attribute id of the attribute
     */
    protected ObjectId getAttributeId(String attrCode, HttpServletRequest request) {

        CardPortletCardInfo cardInfo = getActiveCardInfo(request);

        Attribute attr = AttrUtils.getAttributeByCode(attrCode, cardInfo.getCard());
        if (attr == null) {
            throw new IllegalStateException("Couldn't find attribute with code = '" + attrCode + "' in card");
        }

        return attr.getId();

    }

    /**
     * extract Attribute for attribute that edited by DistributionHierarchicalCardLinkAttributeViewer
     * @param attrCode attr code of the attribute
     * @param request HttpServletRequest
     * @return attribute id of the attribute
     */
    protected Attribute getAttribute(String attrCode, HttpServletRequest request) {

        CardPortletCardInfo cardInfo = getActiveCardInfo(request);

        Attribute attr = AttrUtils.getAttributeByCode(attrCode, cardInfo.getCard());
        if (attr == null) {
            throw new IllegalStateException("Couldn't find attribute with code = '" + attrCode + "' in card");
        }

        return attr;
    }

    /**
     * create data in the response with selected cards data
     * @param attr Attribute
     * @param cardIds selected card ids
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws DataException if fail extract data from request of find data in the data storage or
     * create cards data
     */
    protected void process(Attribute attr, List<ObjectId> cardIds,
                           HttpServletRequest request, HttpServletResponse response)
            throws DataException {

        if (cardIds == null || cardIds.size() == 0)
            cardIds = new ArrayList(((CardLinkAttribute) attr).getIdsLinked());
        Map<ObjectId, Card> cardMap;
        try {
            cardMap = getCards(cardIds, request);
        } catch (Exception e) {
            logger.error("Can not get recipient cards by card ids");
            throw new DataException(e.getMessage());
        }

        writeCardList(request, response, cardMap);
    }

    protected void writeCardList(HttpServletRequest request,
                               HttpServletResponse response,
                               Map<ObjectId, Card> cardMap) throws DataException {
        try {
            JSONWriter jw = new JSONWriter(response.getWriter());
            JSONArray items = getJSONDownloadValues(cardMap, request);
            jw.object();
            jw.key("items").value(items);
            jw.endObject();
        } catch (Exception e) {
            logger.error("Error generating response", e);
            throw new DataException(e);
        }
    }

    /**
     * search card from data storage
     * @param ids card ids that used to search card
     * @param request HttpServletRequest
     * @return finded cards
     * @throws DataException if fail get card from data storage
     * @throws ServiceException if fail extract data from HttpServletRequest
     */
    protected abstract Map<ObjectId, Card> getCards(List ids, HttpServletRequest request) throws DataException, ServiceException;

    /**
     * generate card data in JSON format
     * @param cardMap card map
     * @param request HttpServletRequest
     * @return JSONArray of card data
     * @throws DataException if fail create JSON data
     */
    protected abstract JSONArray getJSONDownloadValues(Map<ObjectId, Card> cardMap, HttpServletRequest request) throws DataException;

}

