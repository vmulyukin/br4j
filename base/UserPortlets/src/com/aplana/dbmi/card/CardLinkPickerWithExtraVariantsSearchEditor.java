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

import java.util.Collection;

import javax.portlet.ActionRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.search.SearchFilterPortlet;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;

public class CardLinkPickerWithExtraVariantsSearchEditor extends CommonCardLinkPickerWithExtraVariantsSearchEditor {

	private static final Log logger = LogFactory.getLog(CardLinkPickerWithExtraVariantsSearchEditor.class);

	public boolean gatherData(ActionRequest request, Attribute attr) throws DataException {
		super.gatherData(request, attr);

		CardLinkAttribute linkAttr = (CardLinkAttribute)attr;
		String param = request.getParameter(getAttrHtmlId(linkAttr) + "_values");

		if (param == null) {
			return false;
		} else if ("".equals(param.trim())) {
			linkAttr.clear();
			return true;
		} else {
			Collection<ObjectId> selectedCards = ObjectIdUtils.commaDelimitedStringToNumericIds(param, Card.class);

			DataServiceBean serviceBean = SearchFilterPortlet.getSessionBean(request).getServiceBean();
			selectedCards = processSelectedCard(selectedCards, serviceBean);

			linkAttr.setIdsLinked(selectedCards);

			return true;
		}		
	}

}
