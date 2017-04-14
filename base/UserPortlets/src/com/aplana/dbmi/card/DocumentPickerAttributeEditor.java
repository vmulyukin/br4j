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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.filter.TemplateIdListFilter;
import com.aplana.dbmi.model.util.DateUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Editor for Card's Link documents in dialog, which makes filtering by Year and Template 
 *  
 * @author skashanski
 *
 */
public class DocumentPickerAttributeEditor extends CardLinkPickerAttributeEditor {
	
	public static final String PARAM_YEAR_START = "yearStart";
	public static final String PARAM_YEAR_END = "yearEnd";
	public static final String PARAM_VISIBLE_INPUTS = "visibleInputElements";
	public static final String PARAM_REFERENCE_LIST = "referenceList";
	public static final String PARAM_REFERENCE_LIST_FOR_LINK_TEMPLATE = "referenceListForLinkTemplate";

	public static final String KEY_YEARS = "years";
	public static final String KEY_DOC_TYPES = "docTypes";
	public static final String KEY_INPUT_IDS = "inputIds";
	public static final String KEY_REFERENCE_LIST_IDS = "referenceList";
	public static final String KEY_REFERENCE_LIST_FOR_LINK_TEMPLATES = "referenceListForLinkTemplates";

	/**
	 * contains year start value to generate list for filtering  by year 
	 */
	private String yearStart = null;

	/**
	 * contains year end value to generate list for filtering  by year 
	 */
	private String yearEnd = null;
	
	/**
	 * contains comma delimited Document's types(jbr.incoming, br.outcoming and etc.) to filter by document type 
	 */
	// private String documentTypes = null;
	
	//Ids of inputs to be shown on searching form
	private List<String> visibleInputElementsIds;

	/**
	 * contains comma delimited referenceValue's types(1502, 1503 and etc.) to filter by link type
	 */
	private Set<String> visibleReferenceValuesIds;
	
	/**
	 * contains comma delimited referenceValue's types(1502, 1503 and etc.) to filter by link type
	 */
	private Map<Long, String> referenceValuesIdsFoTemplates;
	public DocumentPickerAttributeEditor() {
		super();
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/DocumentPickerInclude.jsp");
	}
	
	public List<Integer> generateFilterYears() {

		final int startYear = Integer.parseInt(yearStart);

		final Calendar calendar = Calendar.getInstance();

		int currentYear = calendar.get(Calendar.YEAR);

		if ((yearEnd != null) &&yearEnd.startsWith("+")) {
			String incrementValueStr = yearEnd.substring(1);
			
			int incrementVal = Integer.parseInt(incrementValueStr);
			currentYear+=incrementVal;
		}
		
		if ((yearEnd != null) &&yearEnd.startsWith("-")) {
			String decrementValueStr = yearEnd.substring(1);
			
			int incrementVal = Integer.parseInt(decrementValueStr);
			currentYear-=incrementVal;
		}		

		int endYear = currentYear;
		
		if (endYear<=startYear)
			throw new IllegalArgumentException("Value yearStart parameter is grater or equal to yearEnd parameter value!");
		
		List<Integer> result = new ArrayList<Integer>();
		for(int i = startYear;i <= endYear; i++) {
			result.add(i);
		}
		
		return result;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initEditor(PortletRequest request, Attribute attr)
			throws DataException {

		super.initEditor(request, attr);

		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/DocumentPicker.jsp");

		CardPortletSessionBean sessionBean = (CardPortletSessionBean)request.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		DataServiceBean dataServiceBean = sessionBean.getServiceBean();

		CardLinkPickerDescriptor descriptor = (CardLinkPickerDescriptor) cardInfo
				.getAttributeEditorData(attr.getId(), KEY_DESCRIPTOR);

		final List<Integer> filterYearsList = generateFilterYears();
		if (!filterYearsList.isEmpty()) 
			cardInfo.setAttributeEditorData(attr.getId(), KEY_YEARS, filterYearsList);
		
		if(visibleInputElementsIds != null && !visibleInputElementsIds.isEmpty()){
			cardInfo.setAttributeEditorData(attr.getId(), KEY_INPUT_IDS, visibleInputElementsIds);
		}

		if(visibleReferenceValuesIds != null && !visibleReferenceValuesIds.isEmpty()){
			cardInfo.setAttributeEditorData(attr.getId(), KEY_REFERENCE_LIST_IDS, ObjectIdUtils.numericIdsToCommaDelimitedString(visibleReferenceValuesIds));
		}
		
		if(referenceValuesIdsFoTemplates != null && !referenceValuesIdsFoTemplates.isEmpty()){
			cardInfo.setAttributeEditorData(attr.getId(), KEY_REFERENCE_LIST_FOR_LINK_TEMPLATES, referenceValuesIdsFoTemplates);
		}
		
		//gets possible filter templates 
		final Collection<Template> filterTemlates = descriptor.getDefaultVariantDescriptor().getSearch().getTemplates();

		if (!filterTemlates.isEmpty()) {
				// final Collection<ObjectId> filterTemplates = getDocumentTypeIds();
				final List<Template> documentTypes = getFullTemplates( dataServiceBean, filterTemlates);
				Collections.sort(documentTypes,
						new Comparator<Object>() {
						@SuppressWarnings("null")
						public int compare(Object o1, Object o2) {
							final Template tpl1 = (Template)o1, tpl2 = (Template)o2;
							final boolean isNull2 = (tpl2 == null) || (tpl2.getName() == null);
							if (tpl1 == null || tpl1.getName() == null)
								return (isNull2) ? 0 : -1;
							if (isNull2)
								return 1;
							return tpl1.getName().compareTo(tpl2.getName());
						}
					}
				);
				cardInfo.setAttributeEditorData(attr.getId(), KEY_DOC_TYPES, documentTypes);

		}
	}


	@SuppressWarnings("unchecked")
	public List<Template> getFullTemplates(DataServiceBean dataServiceBean,
			Collection<Template> filterTemplates) throws DataException {
		try {
			final TemplateIdListFilter filter = new TemplateIdListFilter(filterTemplates);
			return  (List<Template>) dataServiceBean.filter(Template.class, filter);
		} catch (ServiceException e) {
			throw new DataException(e);
		}
	}


//	private Collection<ObjectId> getDocumentTypeIds() {
//
//		final String[] doctTypesArray = StringUtils.commaDelimitedListToStringArray(documentTypes);
//
//		final Collection<ObjectId> filterTemplates = new ArrayList<ObjectId>();
//
//		for(int i =0 ; i < doctTypesArray.length; i++ ) {
//
//			String documentTypeStr = doctTypesArray[i];
//			ObjectId templateId = ObjectId.predefined(Template.class, documentTypeStr.trim());
//
//			if (templateId == null)
//				continue;
//
//			filterTemplates.add(templateId);
//
//		}
//		return filterTemplates;
//	}


	@Override
	public void setParameter(String name, String value) {
		if (PARAM_VISIBLE_INPUTS.equalsIgnoreCase(name)){
			String[] inputIds = value.split(",");
			for(int i = 0; i < inputIds.length; i++) inputIds[i] = inputIds[i].trim();
			visibleInputElementsIds = Arrays.asList(inputIds);
		} else if (PARAM_YEAR_START.equalsIgnoreCase(name)) {
			yearStart = value;
		} else if (PARAM_YEAR_END.equalsIgnoreCase(name)) {
			yearEnd = value;
		} else if (PARAM_REFERENCE_LIST.equals(name)){
			if (visibleReferenceValuesIds==null){
				visibleReferenceValuesIds = new HashSet<String>();
			}
			String[] inputIds = value.split(",");
			for(int i = 0; i < inputIds.length; i++) visibleReferenceValuesIds.add(inputIds[i].trim());
		} if (PARAM_REFERENCE_LIST_FOR_LINK_TEMPLATE.equals(name)){
			if (referenceValuesIdsFoTemplates==null){
				referenceValuesIdsFoTemplates = new HashMap<Long, String>();
			}
			String[] inputIds = value.split("->");
			if (inputIds.length<2||inputIds.length>2)
				return;
			Long templateId = Long.parseLong(inputIds[0].trim());  
			referenceValuesIdsFoTemplates.put(templateId, inputIds[1].trim());
		} else
			super.setParameter(name, value);
	}

	public static JSONObject getJSONMapTypesForTemplates(Map<Long, String> referenceValuesIdsFoTemplates) {
		final JSONObject jo = new JSONObject();
		makeJSONMapStringIds(referenceValuesIdsFoTemplates, jo);
		return jo;
	}
	
	/**
	 * Map ������� �� Long-����� � ������ �� ������� Long-�������� ����� �������
	 * @param map
	 * @param jo
	 */
	public static void makeJSONMapStringIds(Map map, JSONObject jo) {
		if (map != null) {
			try {
				for(Iterator<?> keys = map.keySet().iterator(); keys.hasNext(); ) {
					final Long templateId = (Long) keys.next();
					if (map.get(templateId) == null||((String)map.get(templateId)).isEmpty())
						jo.put(templateId.toString(), JSONObject.NULL);
					else
						putJsonStringIds(map, jo, templateId);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return;
	}
	
	protected static void putJsonStringIds(Map map, JSONObject jo, Long templateId) throws JSONException {
		String[] ids = ((String)map.get(templateId)).split(",");
		Set<Long> setIds = new HashSet<Long>();
		for(String id: ids){
			setIds.add(Long.parseLong(id.trim()));
		}
		jo.put(templateId.toString(), setIds);
	}	
}
