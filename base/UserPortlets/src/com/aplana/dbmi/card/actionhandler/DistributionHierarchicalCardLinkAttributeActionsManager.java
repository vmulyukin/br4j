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
package com.aplana.dbmi.card.actionhandler;

import com.aplana.dbmi.actionhandler.descriptor.ActionHandlerDescriptor;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptor;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class DistributionHierarchicalCardLinkAttributeActionsManager extends CardPortletAttributeEditorActionsManager {

    public final static String DOWLOAD_FILES_ACTION = "downloadFiles";

    @Override
    public List<String> getActiveActionIds() {
        String mode = getSessionBean().getActiveCardInfo().getMode();
        List<String> result = super.getActiveActionIds();
        List<String> actions = super.getActiveActionIds();
        List<String> filtered = new ArrayList<String>(actions.size());

        final ObjectId jbrOutcomming = ObjectId.predefined(Template.class, "jbr.outcoming");
        Object templateId = getSessionBean().getActiveCard().getTemplate().getId();

        for (String actionId: actions ) {
            ActionHandlerDescriptor ahd = getActionsDescriptor().getActionHandlerDescriptor(actionId);
            if (CardPortlet.CARD_EDIT_MODE.equals(mode) && ahd.isForEditMode() ||
                    CardPortlet.CARD_VIEW_MODE.equals(mode) && ahd.isForViewMode())
                //download action should appear in the outcoming distribution only
                if (DOWLOAD_FILES_ACTION.equals(actionId) ) {

                    if (jbrOutcomming != null && jbrOutcomming.getId().equals(templateId)) {
                        filtered.add(actionId);
                    }

                } else {

                    filtered.add(actionId);

                }



        }
        logger.info(filtered.size() + " of " + actions.size() + " actions available in " + mode +
                " for " + getAttribute().getId().getId());
        return filtered;

    }

    public static DistributionHierarchicalCardLinkAttributeActionsManager getInstance(CardPortletSessionBean sessionBean,
            ActionsDescriptor actionsDescriptor, Attribute attr) {
        DistributionHierarchicalCardLinkAttributeActionsManager am =
                new DistributionHierarchicalCardLinkAttributeActionsManager();
        CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
        am.setSessionBean(sessionBean);
        am.setPortletFormManager(cardInfo.getPortletFormManager());
        am.setActionsDescriptor(actionsDescriptor);
        am.setAttribute(attr);
        am.setServiceBean(sessionBean.getServiceBean());
        return am;
    }



}
