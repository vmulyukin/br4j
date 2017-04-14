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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.actionhandler.ActionsManager;
import com.aplana.dbmi.actionhandler.descriptor.ActionHandlerDescriptor;
import com.aplana.dbmi.actionhandler.descriptor.SelectionType;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptorReader;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;
import com.aplana.dbmi.card.util.SearchUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.DateUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;

/**
 * Base class for CardLinkPickerEditor
 * @author skashanski
 *
 */
public abstract class CommonCardLinkPickerEditor extends JspAttributeEditor {

	
	public static final String PARAM_CONFIG = "config";
	public static final String PARAM_INLINE = "inline";
	public static final String PARAM_SHOW_TITLE = "showTitle";
	public static final String PARAM_SHOW_EMPTY = "showEmpty";
	//��������� ������� � �����. TODO ��������� ��� ���������� ��������� �� ������� � ���������� 
	public static final String PARAM_TYPE_CAPTION = "typeCaption";
	public static final String PARAM_DATE_TYPE_CAPTION = "dateCaption";
	public static final String PARAM_CONNECTION_TYPE_SHOW ="enableConnectionTypeShow";
	public static final String PARAM_MULTI_VALUED = "multiValued";
	private Boolean multiValued = null;
	/*
	 * true, ����� ��������� ���������� ������� �������� � ������.
	 */
	public static final String PARAM_CAN_SELECT_CURCARD = "canSelectCurrentCard";

	
	/**
	 * key  
	 */
	public static final String KEY_DESCRIPTOR = "cardLinkPickerDescriptor";
	public static final String KEY_SHOW_TITLE = "showTitle";
	public static final String KEY_SHOW_EMPTY = "showEmpty";
	public static final String KEY_TYPE_CAPTION = "typeCaption";
	public static final String KEY_DATE_TYPE_CAPTION = "dateCaption";
	public static final String KEY_CACHE_RESET = "cacheReset";
	

	/**
	 * JSON fields
	 */
	public static final String FIELD_CHOICE_VALUE = "choiceValue";
	public static final String FIELD_HIERARCHY_COLUMNS = "hierarchyColumns";	
	public static final String FIELD_HIERARCHY_SUPPORTED = "hierarchySupported";
	public static final String FIELD_COLUMNS = "columns";
	public static final String FIELD_CHILDREN = "children";
	public static final String FIELD_HIERARCHY_ACTIONS = "hierarchyActions";
	
	
	/**
	 * actions
	 */
	public static final String ACTION_MANAGER_PREFIX = "cardLinkPickerActionsManager_";
	
	
	
	protected String config = null;
	private boolean inline = false;
	protected boolean typed = false; // ������� ��� ��������� typedLink'�
	protected boolean dated = false; // ������� ��� ��������� datedTypedLink'�
	protected boolean showTitle = true;
	protected boolean showEmpty = true;
	protected String typeCaption = null;
	protected String dateCaption = null;
	protected boolean enableConnectionTypeShow = true; // false ��� �������� ������� "������� �����������" � ������� �������� ��������
	// true, ����� ��������� ������� ������� �������� � ������.
	protected boolean canSelectCurCard = false;
	protected String variantAlias;
	
	// ������ id-��������, ������� �� ���� �������� � ������.
	protected Set<ObjectId> restrictedCardIds = new HashSet<ObjectId>();

	
	public CommonCardLinkPickerEditor() {
		
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/CardLinkPickerInclude.jsp");
		
	}
	
	
	@Override
	public void setParameter(String name, String value) {
		if (PARAM_CONFIG.equalsIgnoreCase(name))
			this.config = value;
		else if (PARAM_INLINE.equalsIgnoreCase(name))
			this.inline = "true".equalsIgnoreCase(value);
		else if (PARAM_SHOW_TITLE.equalsIgnoreCase(name))
			showTitle = Boolean.parseBoolean(value);
		else if (PARAM_SHOW_EMPTY.equalsIgnoreCase(name))
			showEmpty = Boolean.parseBoolean(value);
		else if (PARAM_TYPE_CAPTION.equalsIgnoreCase(name))
			typeCaption = value;
		else if (PARAM_DATE_TYPE_CAPTION.equalsIgnoreCase(name))
			dateCaption = value;
		else if (PARAM_CAN_SELECT_CURCARD.equalsIgnoreCase(name))
			canSelectCurCard = Boolean.parseBoolean(value);
		else if (PARAM_CONNECTION_TYPE_SHOW.equalsIgnoreCase(name))////!
			enableConnectionTypeShow = Boolean.parseBoolean(value);
		else if (PARAM_MULTI_VALUED.equalsIgnoreCase(name))
			multiValued = Boolean.parseBoolean(value);
		else
			super.setParameter(name, value);
	}
	
	
	
	@Override
	public void initEditor(PortletRequest request, Attribute attr)
			throws DataException {

		initializeTypedParameter(attr);
		
		initializeDatedTypedParameter(attr);
		
		initializeParameterJSP(attr);

		storeAttributeEditorsParameters(request, attr);
		
		DataServiceBean dataServiceBean = getDataServiceBean(request);
		
		CardLinkPickerDescriptor d = readCardLinkPickerDescriptor(dataServiceBean, attr);
		
		checkInlineMode(d);
		
		// �������������� ������ ��������
		initializeActions(request, attr, d);
		
		storeKeyDescriptor(request, attr, d);
		
		checkCurrentCard(request);		

		super.initEditor(request, attr);
		
		if(multiValued != null){
			if(attr instanceof PersonAttribute){
				((PersonAttribute)attr).setMultiValued(multiValued);
			} else {
				((CardLinkAttribute)attr).setMultiValued(multiValued);
			}
			HierarchyDescriptor hierarchyDescriptor = getCardLinkVariantDescriptor(attr, request).getHierarchyDescriptor();
			if(hierarchyDescriptor != null){
				ActionHandlerDescriptor actionHandlerDescriptor = hierarchyDescriptor.getActionsDescriptor().getActionHandlerDescriptor(CardLinkPickerAttributeEditor.ACTION_ACCEPT);
				if(actionHandlerDescriptor != null){
					actionHandlerDescriptor.setSelectionType(attr.isMultiValued() ? SelectionType.MULTIPLE : SelectionType.SINGLE);
				}
			}
		}
	}
	
	
	
	/**
	 * Verifies if it is possible to add current card to chosen items
	 */
	protected void checkCurrentCard(PortletRequest request) {
		//do nothing by default
	}
	
	/**
	 * Stores configuratino parameters for given attribute in Portlet Session 
	 * @param request
	 * @param attr
	 * @throws DataException 
	 */
	protected abstract void storeAttributeEditorsParameters(PortletRequest request, Attribute attr) throws DataException;
	
	/**
	 * Returns Action's manager for given attribute and descriptor
	 * @param attrId - attribute ID
	 * @param vd - variant descriptor 
	 * @param request potlet request
	 * @return Action's manager 
	 */
	protected abstract ActionsManager getActionsManager(ObjectId attrId, CardLinkPickerVariantDescriptor vd, PortletRequest request);
	
	
	
	@Override
	public boolean processAction(ActionRequest request,
			ActionResponse response, Attribute attr) throws DataException {
		
		String attrCode = getRequestAttributeId(request);
		
		ObjectId attrId = attr.getId();
		if (attrCode == null || !attrCode.equals(attrId.getId())) {
			return false;
		}
		CardLinkPickerVariantDescriptor vd;
		if(variantAlias != null && !variantAlias.isEmpty()){
			vd = getCardLinkPickerDescriptor(attr, request).getVariantDescriptor(variantAlias);
		} else {
			vd = getCardLinkVariantDescriptor(attr, request);
		}
		
		ActionsManager am = getActionsManager(attrId, vd, request);
		
		return am.processAction(request, response);
	}


	protected String getRequestAttributeId(ActionRequest request) {
		String attrCode = request.getParameter(CardPortlet.ATTR_ID_FIELD);
		return attrCode;
	}
	
	
	

	

	/**
	 * Returns variant descriptor with configuration data for given attribute from Portlet Session
	 * @param attr attribute to get descriptor configuration data 
	 * @param request - Portlet Request
	 */
	protected abstract CardLinkPickerVariantDescriptor getCardLinkVariantDescriptor(Attribute attr, PortletRequest request);

	
	
	
	/**
	 * Returns DataServiceBean from Portlet Session
	 * @param request passed PortletRequest 
	 */
	protected abstract DataServiceBean getDataServiceBean(PortletRequest request);
	

	/**
	 * Stores Descriptor configuration data for given attribute in Portlet Session 
	 * @param request given Portlet request 
	 * @param attr given attribute
	 * @param d - descriptor with configuration data
	 */
	protected abstract void storeKeyDescriptor(PortletRequest request, Attribute attr, CardLinkPickerDescriptor d);
	
	

	/**
	 * Initialize actions for passed attribute 
	 * @param request given Portlet request
	 * @param attr attribute to initialize action 
	 * @param d descriptor with configuration data for passed attribute
	 */
	protected abstract void initializeActions(PortletRequest request, Attribute attr, CardLinkPickerDescriptor d);
	
	
	
	protected void initializeParameterJSP(Attribute attr) {
		
		if (inline) {
			if (attr.isMultiValued()) {
				throw new IllegalArgumentException(
						"Inline mode couldn't be used for multi-valued attributes (" 
						+ attr.getId() + ")");
			}
			setParameter(PARAM_JSP,
					"/WEB-INF/jsp/html/attr/InlineCardLinkPicker.jsp");
		} else {
			setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/CardLinkPicker.jsp");
		}
	}
	
	
	protected void initializeTypedParameter(Attribute attr) {
		typed = attr.getType().equals(Attribute.TYPE_TYPED_CARD_LINK)
				|| attr.getType().equals(Attribute.TYPE_DATED_TYPED_CARD_LINK);
	}
	
	protected void initializeDatedTypedParameter(Attribute attr) {
		dated = attr.getType().equals(Attribute.TYPE_DATED_TYPED_CARD_LINK);
	}	
	
	/**
	 * Checks inline mode : it is not possible to use multi-variants in inline mode 
	 */
	protected void checkInlineMode(CardLinkPickerDescriptor d) {
		if (inline && d.getVariants().size() > 1) {
			throw new IllegalArgumentException(
					"Inline mode couldn't use multi-variants descriptors");
		}
	}
	
	
	/**
	 * Reads descriptor data from configuration file
	 */
	protected CardLinkPickerDescriptor readCardLinkPickerDescriptor(com.aplana.dbmi.service.DataServiceBean dataServiceBean, Attribute attr) throws DataException {
		
		try {
			
			final InputStream stream = getConfigStream(); 
			
			CardLinkPickerDescriptorReader reader = new CardLinkPickerDescriptorReader(dataServiceBean);
			
			return reader.read(stream, attr);
			
		} catch (Exception e) {
			logger.error("Failed to read config file " + config, e);
			throw new DataException(e);

		}
		
	}
	
	
	@Override
	protected Map<String, Object> getReferenceData(Attribute attr, PortletRequest request) 
		throws PortletException 
	{
		final Map<String, Object> result = super.getReferenceData(attr, request);
		if (result != null) {
			String choiceAttrHtmlId = null;
			final CardLinkPickerDescriptor d = getCardLinkPickerDescriptor(attr, request);
		
			if (d != null) {
				String activeVariantDescAlias = getActiveVariantDescriptor(d, request).getAlias();
				result.put("activeVariant", activeVariantDescAlias );
				try {
					result.put("variants", getVariantsJSON(d, request, attr));
					result.put("choiceAttrHtmlId", getChoiceAttrId(d, request, attr));
					result.put("isLocalChoice", d.isLocalChoice());
					result.put("sharedValues", d.isSharedValues());
					result.put("selectedValues", ObjectIdUtils.numericIdsToCommaDelimitedString(getSelectedCardIds(attr)));
					List<ReferenceValue> choiceAttrValues = new ArrayList<ReferenceValue>();
					if(d.isLocalChoice()){
						for(CardLinkPickerVariantDescriptor vd: d.getVariants()){
							ReferenceValue rv = new ReferenceValue();
							rv.setId(vd.getChoiceReferenceValueId());
							rv.setValueRu(vd.getTitle());
							rv.setValueEn(vd.getTitle());
							choiceAttrValues.add(rv);
						}
						result.put("choiceAttrValues", choiceAttrValues);
					}
					return result;
				} catch (Exception e) {
					throw new PortletException(e);
				}
			}
		}
		return result;
	}
	
	
	
	/**
	 * Generates variants descriptor representation at JSON format
	 * @param descriptor descriptor with configuration data 
	 * @param request Portlet Request
	 * @param attr the attribute to generate descriptor's variants
	 * @throws JSONException if there any JSON related exceptions
	 */
	protected JSONObject getVariantsJSON(CardLinkPickerDescriptor descriptor,
			PortletRequest request, Attribute attr) throws JSONException {
		final JSONObject result = new JSONObject();

		for (CardLinkPickerVariantDescriptor vd : descriptor.getVariants()) {

			final JSONObject jv = new JSONObject();

			final String key = vd.getAlias();
			jv.put(FIELD_CHOICE_VALUE, key);
			jv.put(PARAM_CONNECTION_TYPE_SHOW, new Boolean(
					enableConnectionTypeShow));
			jv.put(FIELD_COLUMNS, SearchUtils.getColumnsJSON(vd.getColumns()));

			if (vd.getHierarchyDescriptor() != null) {
				jv.put(FIELD_HIERARCHY_SUPPORTED, true);
				if (vd.getHierarchyDescriptor().getNoColumns()) {
					jv.put(FIELD_HIERARCHY_COLUMNS, new JSONArray());
				} else {
					jv.put(FIELD_HIERARCHY_COLUMNS, SearchUtils.getColumnsJSON(vd
						.getHierarchyDescriptor().getColumns(
								HierarchyDescriptor.COLUMNS_MAIN)));
				}
				final ActionsManager am = getActionsManager(attr.getId(), vd,
						request);
				jv.put(FIELD_HIERARCHY_ACTIONS, am.getActionsJSON());
			} else {
				jv.put(FIELD_HIERARCHY_SUPPORTED, false);
			}

			final JSONArray cards = new JSONArray();
			jv.put("cards", cards);

			if (typed || dated) {
				JSONObject types = new JSONObject();
				jv.put("types", types);
			}
			if (dated) {
				JSONObject dates = new JSONObject();
				jv.put("dates", dates);
			}
			result.put(key, jv);
		}

		return result;
	}	
	
	/**
	 * Returns Choice attribute ID for given Card Link descriptor 
	 * @param d passed Card Link descriptor 
	 * @param request Portlet Request 
	 * @param attr 
	 */
	protected String getChoiceAttrId(CardLinkPickerDescriptor d, PortletRequest request, Attribute attr) {
		
		return null;
		
	}
	
	
	/**
	 * Returns Active variant descriptor for given Card Link descriptor  
	 * @param d passed CardLinkPickerDeescriptor
	 * @param request PortletRequest
	 */
	protected abstract CardLinkPickerVariantDescriptor getActiveVariantDescriptor(CardLinkPickerDescriptor d,  PortletRequest request);
	
	
	/**
	 * Returns descriptor with configuration data for passed attribute
	 * @param attr attribute to get descriptor
	 * @param request Portlet Request
	 */
	protected abstract CardLinkPickerDescriptor getCardLinkPickerDescriptor(Attribute attr, PortletRequest request);
	

	@Override
	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		
		final CardLinkAttribute a = (CardLinkAttribute) attr;
		
		final String param = request.getParameter(getAttrHtmlId(a) + "_values");
		variantAlias = request.getParameter(getAttrHtmlId(a) + "_variantAlias");
		
		if (param == null) {
			return false;
		}

		if ("".equals(param.trim())) {
			a.clear();
			return true;
		}

		if (typed) {
			
			final TypedCardLinkAttribute atyped = (TypedCardLinkAttribute) a;
			
			final String[] types = param.split(",");
			final StringBuffer cards = new StringBuffer();
			final Long[] typeIds = new Long[types.length];
			final Date[] dateIds = new Date[types.length];
			final List<ObjectId> cardIds = new ArrayList<ObjectId>(types.length);
			for (int i = 0; i < types.length; i++) {
				final String[] type = types[i].split(":");
				if (i != 0)
					cards.append(",");
				cards.append(type[0]);
				final ObjectId cardId = new ObjectId(Card.class, Long
						.parseLong(type[0]));
				if (restrictedCardIds.contains(cardId))
					throw new DataException(
							"jbr.card.check.cardlinkpicker.recurse.error.1",
							new Object[] { atyped.getName() });
				cardIds.add(cardId);
				typeIds[i] = (!type[1].equals("null")) ? new Long(Long
						.parseLong(type[1])) : null;
				if(dated) {
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						dateIds[i] = (!type[2].equals("null")) ? sdf.parse(type[2]) : null;
					} catch(ParseException pe) {
						pe.printStackTrace();
						dateIds[i] = null;
					}
					
				}
			}
			
			// ��������� param � ������������� ���� ������ � ����������
			atyped.clear();
			for (int i = 0; i < types.length; i++) {
				final ObjectId cardId = cardIds.get(i);
				atyped.addLinkedId(cardId); // ��-���� ���������� ������
											// addType...
				atyped.addType((Long) cardId.getId(), typeIds[i]);
				if(dated) {
					((DatedTypedCardLinkAttribute)atyped).addDate((Long) cardId.getId(), dateIds[i]);
				}
			}
			return true;
			
		}

		a.setIdsLinked("-1".equals(param) ? new ArrayList<Object>()
				: ObjectIdUtils.commaDelimitedStringToNumericIds(param,
						Card.class));
		return true;
	}
	
	
	protected InputStream getConfigStream() throws DataException {
		try {
			return Portal.getFactory().getConfigService().loadConfigFile(AttributeEditorFactory.CONFIG_FOLDER + config);
		} catch (IOException e) {
			logger.error("Couldn't open hierarchy descriptor file: " + config, e);
			throw new DataException(e);
		}
	}
	
	
	/**
	 * ���������� ������ ��������������� ��������, ������� ����� �������� ���
	 * ������� ��������� ��������
	 */
	protected Collection<?> getSelectedCardIds(Attribute attr) {
		// (2010/02, RuSA) OLD: return ObjectIdUtils.getObjectIds( ((CardLinkAttribute)attr).getValues() );
		return ((CardLinkAttribute)attr).getIdsLinked();
	}
	
	
	public static JSONObject getJSONMapTypesCardLink(TypedCardLinkAttribute attr) {
		final Map types = attr.getTypes();
		final JSONObject jo = new JSONObject();
		makeJSONMapCardLink(types, jo, Long.class);
		return jo;
	}
	
	public static JSONObject getJSONMapDatesCardLink(DatedTypedCardLinkAttribute attr) {
		final Map<Long,Date> dates = attr.getDates();
		final JSONObject jo = new JSONObject();
		makeJSONMapCardLink(dates, jo, Date.class);
		return jo;
	}
	
	public static void makeJSONMapCardLink(Map map, JSONObject jo, Class<?> clazz) {
		if (map != null) {
			try {
				for(Iterator<?> keys = map.keySet().iterator(); keys.hasNext(); ) {
					final Long cardId = (Long) keys.next();
					if (map.get(cardId) == null)
						jo.put(cardId.toString(), JSONObject.NULL);
					else
						putJson(map, jo, cardId, clazz);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return;
	}
	
	protected static void putJson(Map map, JSONObject jo, Long cardId, Class<?> clazz) throws JSONException {
		if(clazz.equals(Long.class)) {
			jo.put(cardId.toString(), map.get(cardId));
		} else if(clazz.equals(Date.class)) {
			jo.put(cardId.toString(), DateUtils.getStringDate((Date)map.get(cardId)));
		}
	}


	@Override
	public boolean isValueCollapsable() {
		return !inline;
	}	

}
