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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.parser.ItemTag;
import com.aplana.dbmi.parser.XmlParse;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.showlist.SearchAdapter;


public class AttributeEditorDialog {
	
	public static final ObjectId ATTR_NAME = ObjectId.predefined(StringAttribute.class, "name");
	protected final static ObjectId TEMPLATE_CA_ID = ObjectId.predefined(Template.class, "jbr.citizenrequest");
	protected final static ObjectId REGDATE_ID = ObjectId.predefined(DateAttribute.class, "regdate");
	
	public final static String ATTRIBUTE = "attribute";
	public final static String COLUMN = "column";
	public final static String MAIN_SEARCH="main-search";
	public final static String ALTERNATE_SEARCH="alternate-search";
	
	public final static String EMPTY_ATTR = "emptyAttr";
	public final static String ID = "id";
	public final static String LABEL_ATTR_ID = "labelAttrId";
	public static final String CONFIG_FILE_PROCESSOR_PARAMETERS_PREFIX = "dbmi/dialog-settings/";
	public final static String STATUS = "status";
	public final static String SUFFIX = "suffix";
	public final static String GROUP = "group";
	public final static String VALUE = "value";
	public final static String DELIMITER = "#delim#";
	
	
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	protected String title;
	protected String actionCode;
	protected Card card;
	protected boolean cache=false;
	protected List<ArrayList<Object>> values = null;
	protected List<String> columnsSuffix = null;
	protected List<String> columnGroups = null;
	protected String[] emptyAttrIndex = null;
	
	protected DataServiceBean serviceBean = null;
	
	public AttributeEditorDialog(){
		columnsSuffix = new ArrayList<String>();
		columnGroups = new ArrayList<String>();
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setActionCode(String actionCode){
		this.actionCode=actionCode;
	}
	
	public String getActionCode(){
		return actionCode;
	}
	
	public void setActiveCard(Card card){
		this.card=card;
	}
	
	
	
	public boolean isCache() {
		return cache;
	}
	public void setCache(boolean cache) {
		this.cache = cache;
	}
	
	public String getJSONData() throws JSONException{
		List<ArrayList<Object>> values = null;
		try {
			values = getData();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject result = new JSONObject();
		
		
		
		JSONArray ar = new JSONArray();
		
		for (ArrayList<Object> arrayList : values) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", arrayList.get(0));
			jsonObject.put("text", collectionToString(arrayList, 2));		
			ar.put(jsonObject);	
		}
		
		result.put("values", ar);
		
		return result.toString();		
	}
	
	public void setDataServiceBean(DataServiceBean dataServiceBean){
		this.serviceBean=dataServiceBean;
	}
	
	private String collectionToString(List<Object> columnValues, int skipCount){
		StringBuilder stringBuilder = new StringBuilder();
		if(columnValues==null){
			return stringBuilder.toString();
		}
		//������� �������� �������
		for(int i = 0; i < skipCount; i ++){
			columnValues.remove(0);
		}
		
		//������� ��������� ������� ��� ��������
		List<Map<String,Object>> compoundValues = new ArrayList();
		for(int i = 0; i < columnValues.size(); i++){
			Map<String, Object> columnObject= new HashMap<String, Object>();
			columnObject.put(VALUE, columnValues.get(i) == null ? "" : columnValues.get(i));
			columnObject.put(SUFFIX, columnsSuffix.get(i) == null ? "" : columnsSuffix.get(i));
			columnObject.put(GROUP, columnGroups.get(i) == null ? "" : columnGroups.get(i));
			compoundValues.add(columnObject);
		}
		
		//������� �������� � ��������, �������� � ���� ������
		for(Map<String,Object> main: compoundValues){
			if(main.get(GROUP).toString().isEmpty()){
				continue;
			}
			String mainGroup =  main.get(GROUP).toString();
			for(Map<String,Object> other: compoundValues){
				
				if(main == other || other.get(GROUP).toString().isEmpty()){
					continue;
				}
				String otherGroup = other.get(GROUP).toString();
				if(mainGroup.equals(otherGroup))
				{	
					//�������� �������� �� ������ �� ��������
					Object mergedValue = merge(main, other);
					//���� �� ������� �����, �� ���� ������
					if(mergedValue.equals(other.get(VALUE))){
						continue;
					}
					other.put(VALUE, merge(main, other));
					//���������� �������� main �������
					main.put(VALUE, "");
					main.put(SUFFIX, "");
					main.put(GROUP, "");
					continue;
				}
			}
		}
		
		for(Map<String,Object> column: compoundValues){
			if(column.get(VALUE).toString().isEmpty() || column.get(VALUE).equals("null")){
				continue;
			}
			stringBuilder.append(column.get(VALUE)).append(column.get(SUFFIX));
		}
		
		return stringBuilder.toString().replace(DELIMITER, ", ");
	}
	
	//���������� ��������, ������ �� �������� ���� �������
	private Object merge(Map<String, Object> main,
			Map<String, Object> other) {
		String result = "";
		String [] mainValuesArray = main.get(VALUE).toString().split(DELIMITER, -1);
		String [] otherValuesArray = other.get(VALUE).toString().split(DELIMITER, -1);
		if(mainValuesArray.length == otherValuesArray.length){
			for(int i = 0; i < mainValuesArray.length;i++){
				result = result + mainValuesArray[i]+main.get(SUFFIX).toString()+otherValuesArray[i];
				if(i != mainValuesArray.length - 1){
					result = result + DELIMITER;
				}
			}
			return result;
		}
		return other.get(VALUE);
	}

	public boolean isData() throws DataException, ServiceException{
		return !getData().isEmpty();
	}
	
	public List<ArrayList<Object>> getData() throws DataException, ServiceException{
		if(cache && values!=null && !values.isEmpty()){
			return values;
		}
		Search search = getSearch();
		if(search!=null){
			SearchAdapter searchAdapter = new SearchAdapter(); 
			searchAdapter.setCardLinkDelimiter(DELIMITER);
			searchAdapter.executeSearch(serviceBean, search);
			values = searchAdapter.getData();
		}else{
			values = new ArrayList<ArrayList<Object>>();
		}
		return values;
	}
	
	private Search getSearch(){
		
		Search search = new Search();
		
		search.setTemplates(Collections.singleton(TEMPLATE_CA_ID));
		search.setIgnoredIds(Collections.singleton(card.getId()));
		
		ItemTag itemTag = getItemTagAttributes();

		Map<String, List<Object>> params =  generateObjectIdFromItemTag(itemTag);
		
		//���������� ����� Search ������������ �������� ��� ��������������
		generationSelectionCriteria(search, params);
		if(search.getAttributes()==null || search.getAttributes().isEmpty()){
			return null;
		}
		/*for (Object attrId : params.get(MAIN_SEARCH)) {
			search.addAttribute(card.getAttributeById((ObjectId) attrId));
		}*/
		if (!params.get(COLUMN).isEmpty()) {			
			search.setColumns(params.get(COLUMN));
		}
		if (!params.get(STATUS).isEmpty()) {
			search.setStates(params.get(STATUS));
		}
		
/* � ������ BR4J00038699 (� ���� ������ ���������� ��������� ������� ������� ��� ������������ � �� ��,
 								��� ������� ����� �������� ������� ���������;
 								���� ����������� �� ���������)

		Calendar to = Calendar.getInstance();
		Calendar from = Calendar.getInstance();	
		from.set(to.get(Calendar.YEAR), 0, 1, 0, 0, 0);
		search.addDateAttribute(REGDATE_ID, from.getTime(), to.getTime());
*/		
		search.setByAttributes(true);
			
		return search;
	}
	
	private void generationSelectionCriteria(Search search, Map<String, List<Object>> params){
		if(search==null){
			return;
		} 
		
		String type = checkAttrListEmpty(params.get(MAIN_SEARCH), emptyAttrIndex, card)?MAIN_SEARCH:ALTERNATE_SEARCH;		
		Attribute attribute = null;
		for (Object attrId : params.get(type)) {
			attribute = card.getAttributeById((ObjectId) attrId);
			if(!checkAttrEmpty(attribute)){
				search.addAttribute(card.getAttributeById((ObjectId) attrId));
			}
		}
	}
	
	private boolean checkAttrListEmpty(List<Object> attrs, String[] emptyAttrIndex, Card card){
		Attribute attribute = null;
		for (int i = 0; i < emptyAttrIndex.length; i++) {
			attribute = card.getAttributeById((ObjectId) attrs.get(Integer.valueOf(emptyAttrIndex[i])-1));
			if(checkAttrEmpty(attribute)){
				return false;
			}
		}
		return true;
		
	}
	
	private boolean checkAttrEmpty(Attribute attribute){
		
		if(attribute==null){
			return true;
		}
		
		if(attribute instanceof CardLinkAttribute){
			List<ObjectId> ids = ((CardLinkAttribute)attribute).getIdsLinked();
			return ids==null || ids.isEmpty()?true:false;
		}else if(attribute.getStringValue().isEmpty()){
			return true;
		}
		return false;
	}
	
	
	
	private ItemTag getItemTagAttributes() {
		ItemTag itemTag = new ItemTag();
		try{
		columnsSuffix.clear();
		String filePath=Portal.getFactory().getConfigService().getConfigFileUrl(CONFIG_FILE_PROCESSOR_PARAMETERS_PREFIX+"ogCheckRepeatability.xml").getPath();		
		XmlParse.parse(itemTag, filePath);
		}catch (Exception e) {
			logger.error(e.getMessage());
		}		
		return itemTag;
	}
	
	private Map<String, List<Object>> generateObjectIdFromItemTag(ItemTag itemTag){
		
		Map<String, List<Object>> params = new HashMap<String, List<Object>>();		
		
		List<Object> columns = new ArrayList<Object>();
		List<Object> status = new ArrayList<Object>();
		List<ItemTag> itemTags = itemTag.getItemTags();
		for (ItemTag item : itemTags) {	
			if(item.getTag().equals(MAIN_SEARCH)){
				String emptyAttr = item.getAttrMap().get(EMPTY_ATTR);
				if(emptyAttr!=null){
					emptyAttrIndex = emptyAttr.split(",");
				}
				params.put(MAIN_SEARCH, createAttributeIdList(item));
				
			}else if(item.getTag().equals(ALTERNATE_SEARCH)){
				params.put(ALTERNATE_SEARCH, createAttributeIdList(item));
				//attrs.add(createAttributeId(item));
			}else if(item.getTag().equals(COLUMN)){
				columns.add(createColumn(item));
			}else if(item.getTag().equals(STATUS)){
				status.add(createState(item));
			}
		}
		
		//params.put(ATTRIBUTE, attrs);
		params.put(COLUMN, columns);
		params.put(STATUS, status);
		return params;
	}
	
	private List<Object> createAttributeIdList(ItemTag maintag) {
		List<Object> attrs = new ArrayList<Object>();
		for (ItemTag item : maintag.getItemTags()) {
			attrs.add(createAttributeId(item));
		}
		return attrs;
	}

	private ObjectId createAttributeId(ItemTag itemTag){
		return IdUtils.smartMakeAttrId(itemTag.getAttrMap().get(ID), Attribute.class, false);
	}
	
	private Column createColumn(ItemTag itemTag){
		
			final SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(IdUtils.smartMakeAttrId(itemTag.getAttrMap().get(ID), Attribute.class, false));
			if(itemTag.getAttrMap().get(LABEL_ATTR_ID)!=null){
				col.setLabelAttrId(IdUtils.smartMakeAttrId(itemTag.getAttrMap().get(LABEL_ATTR_ID), Attribute.class, false));
				col.setFullLabelAttrId(itemTag.getAttrMap().get(LABEL_ATTR_ID));
			}
			columnsSuffix.add(itemTag.getAttrMap().get(SUFFIX));
			columnGroups.add(itemTag.getAttrMap().get(GROUP));
		return col;
	}
	
	private ObjectId createState(ItemTag itemTag){
		return IdUtils.makeStateId(itemTag.getMsg());
	}
	
	
}
