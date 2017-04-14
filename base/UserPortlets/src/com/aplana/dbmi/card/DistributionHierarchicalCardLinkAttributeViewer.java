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
package com.aplana.dbmi.card;

import com.aplana.crypto.DownloadCardsApplet;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptor;
import com.aplana.dbmi.card.actionhandler.DistributionHierarchicalCardLinkAttributeActionsManager;
import com.aplana.dbmi.model.Attribute;

/**
 * class that extends {@link HierarchicalCardLinkAttributeEditor} add opportunity to
 * to download cards in to file using {@link DownloadCardsApplet}
 */
public class DistributionHierarchicalCardLinkAttributeViewer extends HierarchicalCardLinkAttributeViewer {

    public final static String DISTRIBUTION_PARAM_STARTONLOAD = "startonload";
    public final static String DISTRIBUTION_PARAM_DOWNLOAD_VALUES = "downloadvalues";
    public static final String PARAM_NAMESPACE = "namespace";
    public static final String PARAM_ATTR_CODE = "attrCode";

    public final static String JSON_CARD_ID = "cardid";
    public final static String JSON_RECIPIENT_ID = "recipientid";
    public final static String JSON_FILE_NAME = "filename";

    public DistributionHierarchicalCardLinkAttributeViewer() {
        super();
        setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/DistributionHierarchicalCardList.jsp");
    }

    @Override
    protected void initActionsManager(CardPortletSessionBean sessionBean, ActionsDescriptor actionsDescriptor, Attribute attr) {
        DistributionHierarchicalCardLinkAttributeActionsManager am =
                DistributionHierarchicalCardLinkAttributeActionsManager.getInstance(sessionBean, actionsDescriptor, attr);
        CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
        cardInfo.setAttributeEditorData(attr.getId(), ACTIONS_MANAGER_KEY, am);
    }

}
