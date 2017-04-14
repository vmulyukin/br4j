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
package com.aplana.cms.card;

import com.aplana.cms.AppContext;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.web.tag.util.StringUtils;


/**
 * Represents helper class for location appropriate Workstation Card form
 * @author skashanski
 *
 */
public class CardFormLocator {

	
	private static final String USER = "Usr";
	private static final String TEMPLATE = "Templ";
	private static final String CMS_CARD_FORM = "cmsCardForm";

	private static String createUniqueSpringName(ObjectId templateId, ObjectId personId) {
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(CMS_CARD_FORM);
		buffer.append(TEMPLATE);
		buffer.append(templateId.getId().toString());
		buffer.append(USER);
		buffer.append(personId.getId().toString());
		
		return buffer.toString();   
	}
	
	public static CmsCardForm getCardForm(ObjectId templateId, ObjectId personId, String formPrefix) {

		// returns form according to given template and user permissions
		// String cardForm = createUniqueSpringName(templateId, personId);
		// temporary for example

		String cardFormId = templateId.getId().toString();
		if (StringUtils.hasLength(formPrefix)) {
			cardFormId = formPrefix + cardFormId;
		}

		String cardForm = "internalCmsCardForm";
		if (StringUtils.hasLength(cardFormId) && cardFormId.startsWith("paper")) {
			if (cardFormId.contains("344")) {
				cardForm = "paperActionsCmsCardForm";
			} else if (cardFormId.contains("284")) {
				cardForm = "filesCmsCardForm";
			} else if (cardFormId.contains("1244")) {
				cardForm = "docStorageCmsCardForm";
			} else {
				cardForm = "paperOriginalCmsCardForm";
			}
		} else {
			if ("284".equals(cardFormId))
				cardForm = "filesCmsCardForm";
			else if ("1104".equals(cardFormId))
				cardForm = "internalVisaCmsCardForm";
			else if ("1164".equals(cardFormId))
				cardForm = "internalVisaExternalCmsCardForm";
			else if ("348".equals(cardFormId))
				cardForm = "visaCmsCardForm";
			else if ("350".equals(cardFormId))
				cardForm = "internalVisaPaperCmsCardForm";
			else if ("784".equals(cardFormId))
				cardForm = "internalCmsCardForm";
			else if ("364".equals(cardFormId))
				cardForm = "outcomingCmsCardForm";
			else if ("764".equals(cardFormId))
				cardForm = "ordCmsCardForm";
			else if ("944".equals(cardFormId))
				cardForm = "listExternalPersonsCmsCardForm";
			else if ("10".equals(cardFormId))
				cardForm = "internalPersonCmsCardForm";
			else if ("464".equals(cardFormId))
				cardForm = "externalPersonCmsCardForm";
			else if("1204".equals(cardFormId))
				cardForm = "internalPersonsListCmsCardForm";
			else if("222".equals(cardFormId))
				cardForm = "organizationCmsCardForm";
			else if("2300".equals(cardFormId))
				cardForm = "typeDocCmsCardForm";
			else if("347".equals(cardFormId))
				cardForm = "nomenCmsCardForm";
			else if("925".equals(cardFormId))
				cardForm = "considerationResultCmsCardForm";
		}
		return (CmsCardForm) AppContext.getApplicationContext().getBean(cardForm);
	}
}
