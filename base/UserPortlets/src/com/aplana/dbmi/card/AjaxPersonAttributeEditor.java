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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.portlet.ActionRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.filter.PersonCardIdFilter;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class AjaxPersonAttributeEditor extends CardLinkPickerAttributeEditor {

	private static final Log logger = LogFactory.getLog(AjaxPersonAttributeEditor.class);

	public boolean gatherData(ActionRequest request, Attribute attr) throws DataException {
		PersonAttribute pa = (PersonAttribute)attr;
		String param = request.getParameter(getAttrHtmlId(pa) + "_values");
		variantAlias = request.getParameter(getAttrHtmlId(pa) + "_variantAlias");
		
		if (param == null) {
			return false;
		} else if ("".equals(param.trim())) {
			pa.setValues(new ArrayList(0));
			return true;
		} else {
			Collection selectedCards = ObjectIdUtils.commaDelimitedStringToNumericIds(param, Card.class);
			PersonCardIdFilter f = new PersonCardIdFilter();
			f.setCardIds(selectedCards); 
			DataServiceBean serviceBean = CardPortlet.getSessionBean(request).getServiceBean();
			try {
				pa.setValues(serviceBean.filter(Person.class, f));
			} catch (ServiceException e) {
				logger.error("Exception caught", e);
			}
			return true;
		}		
	}

	protected Collection getSelectedCardIds(Attribute attr) {
		PersonAttribute pa = (PersonAttribute)attr;
		Set result = new LinkedHashSet();
		if (pa.getValues() != null) {
			Iterator i = pa.getValues().iterator();
			while (i.hasNext()) {
				Person p = (Person)i.next();
				if (p.getCardId() != null) {
					result.add(p.getCardId());
				} else {
					logger.warn("Person with id = " + p.getId().getId() + " doesn't have proper card link. Ignoring...");
				}
			}
		}
		return result;
	}
}
