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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.filter.PersonCardIdFilter;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.search.SearchFilterPortlet;
import com.aplana.dbmi.search.SearchFilterPortletSessionBean;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * Represents editor in extended search for person
 *
 */
public class AjaxPersonSearchEditor extends CommonCardLinkPickerWithExtraVariantsSearchEditor {

	public static final String PARAM_SHOW_INACTIVE_PERSONS_CHECKBOX = "showInactiveFlag";
	public static final String PARAM_SHOW_INACTIVE_PERSONS_CHECKBOX_ONLY_ADMIN = "showInactiveFlagOnlyAdmin";

	public static final String KEY_SHOW_INACTIVE_PERSONS_CHECKBOX            = PARAM_SHOW_INACTIVE_PERSONS_CHECKBOX;

	/**
	 * Flag to indicate if we need to show 'checkbox' to add inactive persons to display
	 */
	private boolean showInactivePersonsFlag = true;

	private boolean showInactiveFlagOnlyAdmin = true;

	private static final Log logger = LogFactory.getLog(AjaxPersonSearchEditor.class);

	public boolean gatherData(ActionRequest request, Attribute attr) throws DataException {
		super.gatherData(request, attr);

		PersonAttribute pa = (PersonAttribute)attr;
		String param = request.getParameter(getAttrHtmlId(pa) + "_values");

		if (param == null) {
			return false;
		} else if ("".equals(param.trim())) {
			pa.setValues(new ArrayList<Person>(0));
			return true;
		} else {
			Collection<ObjectId> selectedCards = ObjectIdUtils.commaDelimitedStringToNumericIds(param, Card.class);

			DataServiceBean serviceBean = SearchFilterPortlet.getSessionBean(request).getServiceBean();

			PersonCardIdFilter f = new PersonCardIdFilter();
			f.setCardIds(selectedCards); 
			try {
				pa.setValues(serviceBean.filter(Person.class, f));
			} catch (ServiceException e) {
				logger.error("Exception caught", e);
			}
			return true;
		}		
	}

	@Override
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

	@Override
	protected void storeAttributeEditorsParameters(PortletRequest request,
			Attribute attr) throws DataException {
		super.storeAttributeEditorsParameters(request, attr);

		SearchFilterPortletSessionBean searchFilterBean = getSessionBean(request);

		if ( showInactiveFlagOnlyAdmin == true ) {
			boolean isAdmin = false;
			ObjectId adminRole = ObjectId.predefined(SystemRole.class, "admin");
			AsyncDataServiceBean serviceBean = searchFilterBean.getServiceBean();
			Person currentUser = serviceBean.getPerson();
			Collection<Role> roles;
			try {
				roles = serviceBean.listChildren(currentUser.getId(), Role.class);
			} catch (DataException e) {
				logger.error("", e);
				throw new DataException(e);
			} catch (ServiceException e) {
				logger.error("", e);
				throw new DataException(e);
			}
			Iterator<Role> ir = roles.iterator();
			while (ir.hasNext()) {
				if (ir.next().getSystemRole().getId().equals(adminRole)) {
					isAdmin = true;
					break;
				}
			}
			if (!isAdmin )
				showInactivePersonsFlag = false;
		}
		searchFilterBean.setSearchEditorData(attr.getId(), KEY_SHOW_INACTIVE_PERSONS_CHECKBOX, showInactivePersonsFlag);		

	}

	@Override
	public void setParameter(String name, String value) {
		if (PARAM_SHOW_INACTIVE_PERSONS_CHECKBOX.equalsIgnoreCase(name))
			this.showInactivePersonsFlag = Boolean.parseBoolean(value.trim());
		else
			super.setParameter(name, value);
	}	
}
