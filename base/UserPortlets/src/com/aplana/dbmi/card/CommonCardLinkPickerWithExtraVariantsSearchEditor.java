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

import java.util.*;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.search.ext.TriggeredObjectId;
import com.aplana.dbmi.search.SearchFilterPortletSessionBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;

public class CommonCardLinkPickerWithExtraVariantsSearchEditor extends CardLinkPickerSearchEditor {

	public static final String PARAM_SHOW_EXTRA_VARIANTS_CHECKBOX = "showExtraAttrsFlag";
	public static final String PARAM_CHECK_EXTRA_VARIANTS_CHECKBOX = "checkExtraAttrsFlag";
	public static final String PARAM_EXTRA_VARIANTS_CHECKBOX_TITLE_CODE = "extraAttrsTitleCode";
	public static final String PARAM_EXTRA_VARIANTS_CHECKBOX_CHECKED_TITLE_CODE = "extraAttrsCheckedTitle";
	public static final String PARAM_EXTRA_VARIANTS_CHECKBOX_UNCHECKED_TITLE_CODE = "extraAttrsUncheckedTitle";

	public static final String PARAM_EXTRA_VARIANTS_CHECKBOX_ON_TOP = "extraVariantsOnTop";

	public static final String KEY_SHOW_EXTRA_ATTRS_CHECKBOX = "showExtraAttrsFlag";
	public static final String KEY_CHECK_EXTRA_ATTRS_CHECKBOX = "checkExtraAttrsFlag";
	public static final String KEY_EXTRA_ATTRS_CHECKBOX_TITLE_CODE = "extraAttrsTitleCode";
	public static final String KEY_EXTRA_ATTRS_CHECKBOX_CHECKED_TITLE_CODE = "extraAttrsCheckedTitle";
	public static final String KEY_EXTRA_ATTRS_CHECKBOX_UNCHECKED_TITLE_CODE = "extraAttrsUncheckedTitle";

	public static final String KEY_EXTRA_VARIANTS_CHECKBOX_ON_TOP = PARAM_EXTRA_VARIANTS_CHECKBOX_ON_TOP;

	private boolean showExtraVariantsFlag = false;

	private boolean checkExtraVariantsFlag = true;

	private String extraAttrsTitleCode = "search.show.flag.ExtraVariants";
	private String extraAttrsCheckedTitle = "search.show.flag.ExtraVariants.checked";
	private String extraAttrsUncheckedTitle = "search.show.flag.ExtraVariants.unchecked";

	private boolean extraVariantsCheckboxOnTop = false; 

	private static final Log logger = LogFactory.getLog(CommonCardLinkPickerWithExtraVariantsSearchEditor.class);

	public boolean gatherData(ActionRequest request, Attribute attr) throws DataException {
		processExtraVariantsFlag(request, attr);
		return true;
	}

	private void processExtraVariantsFlag(ActionRequest request, Attribute attr) {
		if(TriggeredObjectId.class.isAssignableFrom(attr.getId().getClass())){
			String extraPersonFlag = request.getParameter(getAttrHtmlId(attr)+"_ExtraVariantFlag");
			SearchFilterPortletSessionBean searchFilterBean = getSessionBean(request);
			if(extraPersonFlag!=null && (extraPersonFlag.equals("on") || extraPersonFlag.equals("true"))){
				searchFilterBean.setSearchEditorData(attr.getId(), KEY_CHECK_EXTRA_ATTRS_CHECKBOX, true);
				((TriggeredObjectId)attr.getId()).setEnableExtraAttrIds(true);
			} else {
				searchFilterBean.setSearchEditorData(attr.getId(), KEY_CHECK_EXTRA_ATTRS_CHECKBOX, false);
				((TriggeredObjectId)attr.getId()).setEnableExtraAttrIds(false);
			}
		}
	}

	protected Collection<ObjectId> processSelectedCard(Collection<ObjectId> selectedCards, DataServiceBean serviceBean) {
		return selectedCards;
	}

	protected Collection getSelectedCardIds(Attribute attr) {
		if (CardLinkAttribute.class.equals(attr.getClass())) {
			List<ObjectId> result = ((CardLinkAttribute)attr).getIdsLinked();
			return result;
		}
		return Collections.emptyList();
	}

	@Override
	protected void storeAttributeEditorsParameters(PortletRequest request,
			Attribute attr) throws DataException {
		super.storeAttributeEditorsParameters(request, attr);

		SearchFilterPortletSessionBean searchFilterBean = getSessionBean(request);

		searchFilterBean.setSearchEditorData(attr.getId(), KEY_SHOW_EXTRA_ATTRS_CHECKBOX, showExtraVariantsFlag);
		searchFilterBean.setSearchEditorData(attr.getId(), KEY_CHECK_EXTRA_ATTRS_CHECKBOX, checkExtraVariantsFlag);
		searchFilterBean.setSearchEditorData(attr.getId(), KEY_EXTRA_ATTRS_CHECKBOX_TITLE_CODE, extraAttrsTitleCode);
		searchFilterBean.setSearchEditorData(attr.getId(), KEY_EXTRA_ATTRS_CHECKBOX_CHECKED_TITLE_CODE, extraAttrsCheckedTitle);
		searchFilterBean.setSearchEditorData(attr.getId(), KEY_EXTRA_ATTRS_CHECKBOX_UNCHECKED_TITLE_CODE, extraAttrsUncheckedTitle);
		searchFilterBean.setSearchEditorData(attr.getId(), KEY_EXTRA_VARIANTS_CHECKBOX_ON_TOP, extraVariantsCheckboxOnTop);

	}

	@Override
	public void setParameter(String name, String value) {
		if (PARAM_SHOW_EXTRA_VARIANTS_CHECKBOX.equalsIgnoreCase(name))
			this.showExtraVariantsFlag = Boolean.parseBoolean(value.trim());
		else if (PARAM_CHECK_EXTRA_VARIANTS_CHECKBOX.equalsIgnoreCase(name))
			this.checkExtraVariantsFlag = Boolean.parseBoolean(value.trim());
		else if (PARAM_EXTRA_VARIANTS_CHECKBOX_TITLE_CODE.equalsIgnoreCase(name))
			this.extraAttrsTitleCode = value.trim();
		else if (PARAM_EXTRA_VARIANTS_CHECKBOX_CHECKED_TITLE_CODE.equalsIgnoreCase(name))
			this.extraAttrsCheckedTitle = value.trim();
		else if (PARAM_EXTRA_VARIANTS_CHECKBOX_UNCHECKED_TITLE_CODE.equalsIgnoreCase(name))
			this.extraAttrsUncheckedTitle = value.trim();
		else if (PARAM_EXTRA_VARIANTS_CHECKBOX_ON_TOP.equalsIgnoreCase(name))
			this.extraVariantsCheckboxOnTop = Boolean.parseBoolean(value.trim());
		else
			super.setParameter(name, value);
	}	
}
