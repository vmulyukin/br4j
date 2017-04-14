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
package com.aplana.dbmi.action;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
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


import org.springframework.util.StringUtils;

import com.aplana.dbmi.action.Search.IntegerSearchConfigValue.SearchType;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.action.xml.SearchXmlHelper;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardAccess;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;

/**
 * {@link Action} implementation used to perform search for {@link Card cards}
 * satisfying various conditions.
 * <br>
 * There are three types of search (last two could be used simultaneously):
 * <ul>
 * <li>Search by card codes</li>
 * In this case {@link #getWords()} property should contain list of numeric card identifiers
 * delimited by comma or semicolon. All other parameters of search object in this case will be ignored.
 * Search result will contain all cards with given identifiers presented in database.
 * <li>Search by attributes</li>
 * In this case {@link #getWords()} property should contain keyword.
 * This keyword will be searched in all string and text attributes of cards stored in database.
 * <br>
 * If it is necessary to restrict set of attributes to be looked for a keyword occurence, then these 
 * attributes should be added to {@link #getAttributes() attributes} collection by 
 * {@link #addStringAttribute(ObjectId)} method.<br>
 * Additional constraints on attribute values could be defined by executing following
 * methods {@link #addCardLinkAttribute(ObjectId, ObjectId)},
 * {@link #addDateAttribute(ObjectId, Date, Date)}, {@link #addIntegerAttribute(ObjectId, int, int)},
 * {@link #addListAttribute(ObjectId, Collection)}, {@link #addPersonAttribute(ObjectId, ObjectId)}.
 * <br>
 * Also cards could be filtered by {@link #setStates(Collection) card states} and 
 * {@link #setTemplates(Collection) templates used for cards creation}.
 * <br>
 * Search result will contain all cards containing given keywords in string and text attributes. 
 * <li>Full-text search</li>
 * If used in conjuction with search by attributes then works like simple search by attributes 
 * but instead of simple search for keyword occurence in string and text attributes
 * more complex context search is performed (keyword could contain special search expression).<br>
 * If used standalone, then searches given keyword in material files attached to cards. * 
 * </ul>
 * <br> 
 * This action returns {@link SearchResult} object as a result
 */
@SuppressWarnings("all")
public class Search implements Action<SearchResult> {
	private static final long serialVersionUID = 11L;
	private String nameRu;
	private String nameEn;
	private String words;
	private boolean strictWords=false;
	private boolean byCode;
	private boolean byAttributes;
	private boolean byMaterial;
	private Collection states;
	private Collection templates;
	private Collection materialTypes;
	//contains ObjectId and values 
	private Map<ObjectId,Object> attributes = new HashMap<ObjectId,Object>();
	private String summaryRu;
	private String summaryEn;
	private ObjectId fetchLink;
	private Collection<Column> columns;
	private Filter filter = new Filter();
	private Set ignoredIds;
	private boolean dontFetch = false;
	private long searchLimit = 0;
	//������ �������� ���������� �������� (��������� � ������ �� �����)
	private boolean isCountOnly = false;

	private String sqlXmlName = null;
	// ����� ��-��. �������� ��������� ��� sql-������� �� ��-�� sqlXmlName
	private String sqlParametersName = null;
	private Map<String, Object> paramsAliases = new HashMap<String, Object>();
	
	/** Represents attribute for storing attached files */
	private ObjectId docLinkAttribute = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	
	
	public static final String PARENT_TEMPLATE = "parent_template";
	
	public static enum SearchTag {
		TAG_SEARCH_FULL_TEXT,
		TAG_SEARCH_WILDCARD,
		TAG_SEARCH_REGNUM,
		TAG_SEARCH_TWO_REGNUM,
		TAG_SEARCH_ID,
		TAG_SEARCH, //����������� ������������� ������-���� ���� �� �������������
		NO_TAG //������ - � ������������ ���
	}

	/**
	 * Gets collection of attribute values constraints defined in this Search object.
	 * Each element in the returned collection is a <tt>Map.Entry</tt>,
	 * key contains string identifier of attribute, value contains object definin
	 * values constraint. 
	 * @return collection of attribute values constraints defined in this Search object
	 */
	public Collection getAttributes() {
		Map attributeCodesMap = new HashMap();
		Iterator i = attributes.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry item = (Map.Entry)i.next();
			ObjectId attributeId = (ObjectId)item.getKey();
			attributeCodesMap.put(attributeId.getId(), item.getValue());
		}

		return attributeCodesMap.entrySet();
	}
	
	public Collection getFullAttributes() {
		return attributes.entrySet();
	}
	
	public ObjectId getDocLinkAttribute() {
		return docLinkAttribute;
	}



	public void setDocLinkAttribute(ObjectId docLinkAttribute) {
		this.docLinkAttribute = docLinkAttribute;
	}



	/**
	 * Gets collection of attribute values constraints defined in this Search object.
	 * Each element in the returned collection is a <tt>Map.Entry</tt>,
	 * key contains ObjectId identifier of attribute, value contains object definition
	 * values constraint. 
	 * @return collection of attribute values constraints defined in this Search object
	 */	
	public Collection<ObjectId> getObjectIdAttributes() {
		return attributes.keySet();
	}	
	
	/**
	 * Returns object representing value constraint defined in this Search request
	 * for given attribute.
	 * @param id Identifier of attribute
	 * @return object representing value constraint, or null if value of this attribute
	 * is not restricted
	 */
	public Object getAttribute(ObjectId id) {
		return attributes.get(id);
	}
	
	/**
	 * Adds parameters to attribute search from {@link Attribute}. Multi-valued attributes
	 * cannot be searched by exact match and will be searched by set intersection. Other attributes
	 * will be searched by exact match.
	 * @param attributes Attributes collection
	 */
	public void addAttribute(Attribute a){
		ObjectId attrId = a.getId();
		if(a instanceof StringAttribute){
			this.addStringAttribute(attrId, a.getStringValue(), TextSearchConfigValue.EXACT_MATCH);
		} else if (a instanceof IntegerAttribute){
			int value = ((IntegerAttribute) a).getValue();
			this.addIntegerAttribute( attrId, value, value);
		} else if (a instanceof CardLinkAttribute){
			for(ObjectId id : ((CardLinkAttribute) a).getIdsLinked()){
				this.addCardLinkAttribute(attrId, id);
			}
		} else if(a instanceof PersonAttribute){			
			for(Object o : ((PersonAttribute) a).getValues()){
				this.addPersonAttribute(attrId, ((Person) o).getId());
			}
		} else if(a instanceof DateAttribute){
			Date value = ((DateAttribute) a).getValue();
			this.addDateAttribute(attrId, value, value);
		} else if(a instanceof ListAttribute){
			this.addListAttribute(attrId, Collections.singleton(((ListAttribute) a).getValue()));
		} else if (a instanceof TreeAttribute){
			this.addListAttribute(attrId, ((TreeAttribute) a).getValues());
		}		
	}
	
	/**
	 * Same as {@link #addAttribute(Attribute)} but takes {@link Collection} of attributes
	 * @param attributes
	 */
	public void addAttributes(Collection<Attribute> attributes){
		for(Attribute a : attributes) addAttribute(a);
	}

	/**
	 * Checks if search will be performed by attributes values 
	 * @return true if search by attributes will be performed, false otherwise
	 */
	public boolean isByAttributes() {
		return byAttributes;
	}

	/**
	 * Sets isByAttributes flag of this search object
	 * @param byAttributes true if search by attributes needs to be performed, false otherwise
	 */
	public void setByAttributes(boolean byAttributes) {
		this.byAttributes = byAttributes;
	}
	
	/**
	 * Checks if search will be performed by card codes 
	 * @return true if search by code will be performed, false otherwise
	 */
	public boolean isByCode() {
		return byCode;
	}

	/**
	 * Sets isByCode flag of this search object
	 * @param byCode true if search by card codes needs to be performed, false otherwise
	 */
	public void setByCode(boolean byCode) {
		this.byCode = byCode;
		if (byCode) {
			//this.byAttributes = false;
			this.byMaterial = false;
		}
		
	}

	/**
	 * Checks if full-text search will be performed 
	 * @return true if full-text search by cards materials will be performed, false otherwise
	 */	
	public boolean isByMaterial() {
		return byMaterial;
	}

	/**
	 * Sets isByMaterial flag of this search object
	 * @param byMaterial true if full-text search by cards material needs to be performed, false otherwise
	 */	
	public void setByMaterial(boolean byMaterial) {
		this.byMaterial = byMaterial;
		
		if (byMaterial) {
			byCode = false;
		}
	}

	/**
	 * Gets material types, one which card should have to be included in search result.
	 * If empty or null then any material type is allowed. 
	 * @return Collection of {@link com.aplana.dbmi.action.xml.MaterialType} objects representing allowed material types.
	 */
	public Collection getMaterialTypes() {
		return materialTypes;
	}

	/**
	 * Sets material types, one which card should have to be included in search result.
	 * If empty or null then any material type is allowed.
	 * @param materialTypes collection of {@link com.aplana.dbmi.action.xml.MaterialType} objects representing allowed material types.
	 */
	public void setMaterialTypes(Collection materialTypes) {
		this.materialTypes = materialTypes;
	}

	/**
	 * Gets localized name of this search object
	 * @return returns {@link #getNameEn()} or {@link #getNameRu()} depending on caller's locale context
	 */
	public String getName() {
		return ContextProvider.getContext().getLocaleString(nameRu, nameEn);
	}

	/**
	 * Gets english name of this search object
	 * @return english name of this search object
	 */
	public String getNameEn() {
		return nameEn;
	}

	/**
	 * Sets english name of this search object
	 * @param nameEn desired value of english name
	 */
	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}

	/**
	 * Gets russian name of this search object
	 * @return russian name of this search object
	 */	
	public String getNameRu() {
		return nameRu;
	}

	/**
	 * Sets russian name of this search object
	 * @param nameRu desired value of russian name
	 */	
	public void setNameRu(String nameRu) {
		this.nameRu = nameRu;
	}
	
	/**
	 * Gets collection of string identifiers of {@link CardState card states}
	 * allowed in search result.
	 * @return collection of string identifiers of card states allowed in search result.
	 */
	public Collection getStates() {
		return states;
	}

	/**
	 * Sets collection of string identifiers of {@link CardState card states}
	 * allowed in search result.
	 * If empty or null then any card state is allowed.
	 * @param states collection of string identifiers of {@link CardState card states}
	 * allowed in search result.
	 */	
	public void setStates(Collection states) {
		this.states = states;
	}

	/**
	 * Gets collection of {@link Template templates} allowed in search result.
	 * @return collection of {@link Template} objects allowed in search result.
	 */
	public Collection getTemplates() {
		return templates;
	}

	/**
	 * Sets collection of {@link Template templates} allowed in search result.
	 * @param templates collection of {@link Template} objects allowed in search result.
	 */	
	public void setTemplates(Collection templates) {
		this.templates = templates;
	}

	/**
	 * Gets search keyword.
	 * If {@link #isByCode()} = true then keyword contains list of card identifiers delimited 
	 * by comma or semicolon.
	 * Otherwise keyword is a string value to be searched in string and text attributes (and possibly
	 * in cards material) of cards.
	 * @return search keyword or list of cards identifiers depending on {@link #isByCode()} values
	 */
	public String getWords() {
		return words;
	}

	/**
	/**
	 * Sets search keyword.<br>
	 * If {@link #isByCode()} = true then keyword contains list of card identifiers delimited 
	 * by comma or semicolon.
	 * Otherwise keyword is a string value to be searched in string and text attributes (and possibly
	 * in cards material) of cards.
	 * @param words search keyword or list of cards identifiers depending on {@link #isByCode()} values
	 */
	public void setWords(String words) {
		this.words = words;
	}
	
	
	
	public boolean isStrictWords() {
		return strictWords;
	}

	public void setStrictWords(boolean strictWords) {
		this.strictWords = strictWords;
	}

	/**
	 * Adds String or Text attribute, values of which will be compared with search keyword.
	 * If no String or Text attributes was added then all such attributes will checked. 
	 * @param id Identifier of attribute to be included in search
	 * @throws IllegalArgumentException if given identifier is not a {@link StringAttribute} or
	 * {@link TextAttribute} identifier
	 */
	public void addStringAttribute(ObjectId id) {
		
		validateStringAttribute(id);
		
		attributes.put(id, Boolean.TRUE);
	}

	private void validateStringAttribute(ObjectId id) {
		
		if (!StringAttribute.class.isAssignableFrom(id.getType())){			
			throw new IllegalArgumentException("Not a string or text attribute");
		}
	}	

	public void addStringAttribute(ObjectId id, String value) {
		addStringAttribute( id, value, TextSearchConfigValue.CONTAINS);
	}
	
	public void addStringAttribute(ObjectId id, String value, int searchType) {
		addStringAttribute( id, value, searchType, null);
	}
	
	/**
	 * Adds String or Text attribute, values of which will be compared with search value.
	 * @param id Identifier of attribute to be included in search
	 * @value value to search
	 * @searchType search type @see TextSearchConfigValue
	 * @throws IllegalArgumentException if given identifier is not a {@link StringAttribute} or
	 * {@link TextAttribute} identifier
	 */	
	public void addStringAttribute(ObjectId id, String value, int searchType, Map<String, Object> advParams) {
		
		validateStringAttribute(id);
		
		if (!StringUtils.hasText(value) && ((searchType == TextSearchConfigValue.EXACT_MATCH) || ((searchType == TextSearchConfigValue.EXACT_MATCH_NOT_CASE_SENSITIVE))))
			return;//do nothing
		
		if ((TextSearchConfigValue.CONTAINS != searchType) && (TextSearchConfigValue.EXACT_MATCH != searchType) 
				&& (TextSearchConfigValue.STARTS_FROM != searchType) && (TextSearchConfigValue.EXACT_MATCH_NOT_CASE_SENSITIVE != searchType))
			throw new IllegalArgumentException("Invalid Search Type argument!");
		
		TextSearchConfigValue searchValue = new TextSearchConfigValue(value, searchType);
		searchValue.setParameters(advParams);
		attributes.put(id, searchValue);
		
	}
	
	
	/**
	 * Adds constraint on values of given {@link DateAttribute}
	 * @param id identifier of {@link DateAttribute} values of which 
	 * needs to be constrainted
	 * @param start minimal allowed date
	 * @param end maximal allowed date
	 * @throws IllegalArgumentException is id is not a {@link DateAttribute} identifier
	 */
	public void addDateAttribute(ObjectId id, Date start, Date end) {
		
		if (!checkParameters(id, start, end))
			return;
		
		attributes.put(id, new DatePeriod(start, end));
	}

	private boolean checkParameters(ObjectId id, Date start, Date end) {
		
		if (!DateAttribute.class.equals(id.getType()))
			throw new IllegalArgumentException("Not a date attribute");
		
		if (start == null && end == null)
			return false;
		
		return true;
	}
	

	/**
	 * Adds constraint on values of given {@link DateAttribute} 
	 * @param id identifier of {@link DateAttribute} values of which
	 * @param start minimal allowed date
	 * @param end maximal allowed date
	 * @param includedEmpty  flag to indicate if we need to include empty dates
	 * @throws IllegalArgumentException is id is not a {@link DateAttribute} identifier
	 */
	public void addDateAttribute(ObjectId id, Date start, Date end, boolean includedEmpty) {
		
		if (!checkParameters(id, start, end))
			return;
		
		DatePeriod datePeriod = new DatePeriod(start, end);
		datePeriod.setIncludedEmpty(includedEmpty);
		
		attributes.put(id, datePeriod);
	}


	/**
	 * Adds constraint on values of given {@link IntegerAttribute}
	 * @param id identifier of {@link IntegerAttribute} values of which 
	 * needs to be constrainted
	 * @param min minimal allowed value
	 * @param max maximal allowed value
	 * @throws IllegalArgumentException is id is not a {@link IntegerAttribute} identifier
	 */	
	public void addIntegerAttribute(ObjectId id, int min, int max) {
		if (!IntegerAttribute.class.equals(id.getType()))
			throw new IllegalArgumentException("Not an integer attribute");
		attributes.put(id, new Interval(min, max));
	}
	
	
	public void addIntegerAttribute(ObjectId id, Integer value, SearchType searchType){
		if (!IntegerAttribute.class.equals(id.getType()))
			throw new IllegalArgumentException("Not an integer attribute");		
		IntegerSearchConfigValue searchValue = new IntegerSearchConfigValue(value, searchType);		
		attributes.put(id, searchValue);
	}
	
	/**
	 * Adds constraint on values of given {@link ListAttribute} or {@link TreeAttribute}
	 * @param id identifier of attribute whose values needs to be constrainted.
	 * @param values collection of {@link ReferenceValue} objects containing allowed values
	 * of attribute
	 * @throws IllegalArgumentException if id is not a {@link ListAttribute} or
	 * {@link TreeAttribute} identifier
	 */
	public void addListAttribute(ObjectId id, Collection values) {
		if (!ListAttribute.class.equals(id.getType()) &&
			!TreeAttribute.class.equals(id.getType()))
			throw new IllegalArgumentException("Not a list or tree attribute");
		if (values == null || values.size() == 0)
			return;
		attributes.put(id, values);
	}

	/**
	 * Adds constraint on values of given {@link PersonAttribute}.
	 * There is a special {@link Person#ID_CURRENT person identifier}
	 * that could be used to select cards which contains user who performs search
	 * as value of given {@link PersonAttribute}.   
	 * @param id identifier of {@link PersonAttribute}
	 * @param person identifier of {@link Person} which is allowed as attribute value
	 * @throws IllegalArgumentException if id is not a {@link PersonAttribute} identifier,
	 * or if person is not a {@link Person} identifier.
	 */
	public void addPersonAttribute(ObjectId id, ObjectId person) {
		if (!PersonAttribute.class.equals(id.getType()))
			throw new IllegalArgumentException("Not a person attribute");
		if (!Person.class.equals(person.getType()))
			throw new IllegalArgumentException("Not a person");
		if (Person.ID_CURRENT.equals(person))
			attributes.put(id, person);
		else
			addAttributeToNumericIdList(id, person);
	}
	
	/**
	 * Adds constraint on values of given {@link CardLinkAttribute}.
	 * @param id identifier of {@link CardLinkAttribute} values of which needs
	 * to be constrainted.
	 * @param cardId identifier of {@link Card} representing allowed value of
	 * {@link CardLinkAttribute}.
	 * @throws IllegalArgumentException if id is not {@link CardLinkAttribute} identifier,
	 * or if cardId is not a {@link Card} identifier.
	 */
	public void addCardLinkAttribute(ObjectId id, ObjectId cardId) {
		
		ObjectIdUtils.validateId(id, CardLinkAttribute.class);
		ObjectIdUtils.validateId(cardId, Card.class);
		
		addAttributeToNumericIdList(id, cardId);		
	}
	
	public void addBackLinkAttribute(ObjectId id, ObjectId cardId) {
		
		ObjectIdUtils.validateId(id, BackLinkAttribute.class);
		ObjectIdUtils.validateId(cardId, Card.class);
		
		addAttributeToNumericIdList(id, cardId);		
	}

	private void addAttributeToNumericIdList(ObjectId id, ObjectId cardId) {
		
		NumericIdList list = (NumericIdList)attributes.get(id);
		if (list == null) {
			list = new NumericIdList();
			attributes.put(id, list);
		}
		list.addId(cardId);		
	}
	
	/**
	 * Clears all attribute values constraints
	 */
	public void clearAttributes() {
		attributes = new HashMap();
	}
	
	/**
	 * Checks if value of given attribute is constrained in this Search object
	 * @param id identifier of attribute
	 * @return true if there is exists value constraint for given attribute, false otherwise
	 */
	public boolean hasAttribute(ObjectId id) {
		return attributes.containsKey(id);
	}
	
	/**
	 * Returns collection of allowed values for given {@link ListAttribute} or {@link TreeAttribute}.
	 * @param id identifier of attribute
	 * @return collection of {@link ReferenceValue} objects representing set of allowed attribute values.
	 * Note that {@link ReferenceValue} objects in this collection could be only partially initialized.
	 * If value of given attribute is not constrainted then returns empty collection.
	 * @throws IllegalArgumentException is id is not a {@link ListAttribute} or
	 * {@link TreeAttribute} identifier
	 */
	public Collection getListAttributeValues(ObjectId id) {
		if (!ListAttribute.class.equals(id.getType()) &&
			!TreeAttribute.class.equals(id.getType()))
			throw new IllegalArgumentException("Not a list or tree attribute");
		if (!attributes.containsKey(id))
			return new ArrayList();
		return (Collection) attributes.get(id);
	}
	
	/**
	 * Returns {@link DatePeriod} object representing constraint defined for 
	 * values of given {@link DateAttribute} 
	 * @param id identifier of {@link DateAttribute}
	 * @return {@link DatePeriod} object representing allowed date interval.
	 * If value of given attribute is not constrainted then this {@link DatePeriod} object
	 * will have both {@link DatePeriod#end} and {@link DatePeriod#start} properties
	 * set to null.
	 * @throws IllegalArgumentException is id is not a {@link DateAttribute} identifier
	 */
	public DatePeriod getDateAttributePeriod(ObjectId id) {
		if (!DateAttribute.class.equals(id.getType()))
			throw new IllegalArgumentException("Not a date attribute");
		if (!attributes.containsKey(id))
			return new DatePeriod(null, null);
		return (DatePeriod) attributes.get(id);
	}
	
	/**
	 * Returns {@link Interval} object representing constraint defined for 
	 * values of given {@link IntegerAttribute}
	 * @param id identifier of {@link IntegerAttribute}
	 * @return {@link Interval} object representing allowed interval of numbers.
	 * If value of given attribute is not constrainted then this {@link Interval} object
	 * will be constrainted by minimal and maximal values allowed for Integer data type.
	 */
	public Interval getIntegerAttributeInterval(ObjectId id) {
		if (!IntegerAttribute.class.equals(id.getType()))
			throw new IllegalArgumentException("Not an integer attribute");
		if (!attributes.containsKey(id))
			return new Interval(Integer.MIN_VALUE, Integer.MIN_VALUE);
		return (Interval) attributes.get(id);
	}

	
	/**
	 * Checks if search will fetch cards 
	 * @return true if search will not fetch cards, false otherwise
	 */

	/**
	 * Represents text search value and search type 
	 *
	 */
	public static class TextSearchConfigValue extends AdvancedOptions implements Serializable {
		
		public String value;
		public int searchType = CONTAINS;
		
		public TextSearchConfigValue(String value, int searchType) {
			this.value = value;
			this.searchType = searchType;
		}
		
		/** search types*/
		
		/** search by exact match */ 
		public static final int EXACT_MATCH = 0;
		/** search by exact match - not case sens */
		public static final int EXACT_MATCH_NOT_CASE_SENSITIVE = 3;
		/** search words that contain given value */
		public static final int CONTAINS = 1;
		/** search words that start from given value */
		public static final int STARTS_FROM = 2;
		
		
	}
	
	public static class IntegerSearchConfigValue implements Serializable{
		public Integer value = null;
		public SearchType searchType = SearchType.CONTAINS;
		public enum SearchType{
			CONTAINS,
			EXACT_MATCH; //������� ������������
		}
		public IntegerSearchConfigValue(Integer value, SearchType searchType) {
			super();
			this.value = value;
			this.searchType=searchType;
		}
		
	}
	
	
	/**
	 * Class used to define values interval for {@link DateAttribute} 
	 */	
	public static class DatePeriod implements Serializable
	{
		private static final long serialVersionUID = Search.serialVersionUID;
		public Date start;
		public Date end;
		/** flag to indicate if we need to include empty dates*/
		private boolean isIncludedEmpty = false;
		
		
		
		/**
		 * Creates new instance with given interval bounds
		 * @param start minimal allowed date
		 * @param end maximal allowed date
		 */
		public DatePeriod(Date start, Date end) {
			this.start = start;
			this.end = end;
		}
		
 

		public Date getStart() {
			return start;
		}


		public Date getEnd() {
			return end;
		}


		public boolean isIncludedEmpty() {
			return isIncludedEmpty;
	}
	


		public void setIncludedEmpty(boolean isIncludedEmpty) {
			this.isIncludedEmpty = isIncludedEmpty;
		}
		
		
		
	}
	
	/**
	 * Class used to define values interval for {@link IntegerAttribute} 
	 */
	public static class Interval implements Serializable
	{
		private static final long serialVersionUID = Search.serialVersionUID;
		/**
		 * null-�������� ��� min/max.
		 */
		public final static long EMPTY = Long.MIN_VALUE;

		public long min;
		public long max;
		
		/**
		 * Creates new instance with given interval bounds
		 * @param min minimal allowed value
		 * @param max maximal allowed value
		 */
		// (2009/12/23, RuSA) ��� ������ �� ���������������, ������� ����� ��� 
		// Long, int' ����� �� ����������.
		// OLD: public Interval(int min, int max) {
		public Interval(long min, long max) {
			this.min = min;
			this.max = max;
		}
	}
	
	/**
	 * Class used to define list of numeric ids to filter attribute values
	 * Should be used for CardLinkAttribute only (may be in future for PersonAttribute too)
	 */
	public static class NumericIdList implements Serializable
	{
		private static final long serialVersionUID = 1L;
		private Set numericIds = new HashSet();
		public void addId(ObjectId id) {
			numericIds.add(id);
		}
		
		public Collection<ObjectId> getNumericIds() {
			return numericIds;
		}
		public String getCommaDelimitedString() {
			return ObjectIdUtils.numericIdsToCommaDelimitedString(numericIds);
		}
		public boolean isEmpty() {
			return numericIds.isEmpty();
		}
	}

	/**
	 * ����������� ����� ��� �������� ��������� ���������, ������������, ��� ��� �������� ������ ������������� �������,
	 * � �� ���� �� ���� ��.
	 */
	public static abstract class FullySatisfyingSearchAttribute implements Serializable {
		public abstract boolean isFull();
		public  abstract String getCondition();
	}
	
	/**
	 * Marker class for allowing possibility of searching for empty attributes
	 */
	public static class EmptyAttribute extends FullySatisfyingSearchAttribute implements Serializable {
		
		private static final long serialVersionUID = Search.serialVersionUID;
		private boolean fullEmpty;
		private EmptyAttribute(boolean fullEmpty){
			this.fullEmpty = fullEmpty;
		}
		public static final EmptyAttribute INSTANCE = new EmptyAttribute(false);
		public static final EmptyAttribute FULL_EMPTY_INSTANCE = new EmptyAttribute(true);

		@Override
		public boolean isFull() {
			return fullEmpty;
		}

		@Override
		public String getCondition() {
			return fullEmpty ? "is not null" : "is null";
		}
	}
	
	/**
	 * Marker class for allowing possibility of searching for existing attributes
	 */
	public static class ExistAttribute extends FullySatisfyingSearchAttribute implements Serializable {
		
		private static final long serialVersionUID = Search.serialVersionUID;
		private boolean fullExists;
		private ExistAttribute(boolean fullExists) {
			this.fullExists = fullExists;
		}
		public static final ExistAttribute INSTANCE = new ExistAttribute(false);
		public static final ExistAttribute FULL_EXISTS_INSTANCE = new ExistAttribute(true);

		@Override
		public boolean isFull() {
			return fullExists;
		}

		@Override
		public String getCondition() {
			return fullExists ? "is null" : "is not null";
		}
	}
	
	public static class AdvancedOptions implements Serializable{
		private Map<String, Object> parameters = new HashMap<String, Object>();

		public Map<String, Object> getParameters() {
			return parameters;
		}

		public void setParameters(Map<String, Object> parameters) {
			this.parameters = parameters;
		}
		
		public void addParameter(String key, Object value){
			parameters.put(key, value);
		}
		
		public Object getParameter(String key){
			return parameters.get(key);
		}
		
		
	}
	
	/**
	 * Initialized this Search object with data from XML string
	 * @param xml InputStream containing XML data with Search definition
	 * @throws DataException in case of XML processing error
	 * @see SearchXmlHelper#initFromXml(Search, InputStream)
	 */
	public void initFromXml(InputStream xml) throws DataException
	{
		SearchXmlHelper.initFromXml(this, xml);
	}

	/**
	 * This method serializes this Search object into XML and writes it
	 * into a given OutputStream
	 * @param xml OutputStream to write XML into
	 * @see SearchXmlHelper#storeToXml(Search, OutputStream)
	 */
	public void storeToXml(OutputStream xml) //throws IOException
	{
		SearchXmlHelper.storeToXml(this, xml);
	}

	/**
	 * Adds constraint on attribute values of cards to be included in search result.
	 * It is not recommended to use this method directly. Call one of the following
	 * methods instead: {@link #addCardLinkAttribute(ObjectId, ObjectId)},
	 * {@link #addDateAttribute(ObjectId, Date, Date)}, {@link #addIntegerAttribute(ObjectId, int, int)},
	 * {@link #addListAttribute(ObjectId, Collection)}, {@link #addPersonAttribute(ObjectId, ObjectId)},
	 * {@link #addStringAttribute(ObjectId)}
	 * @param id objectId identifier of attribute values of which needs to be constrained
	 * @param value object defining attribute values constraint
	 */
	public void addAttribute(ObjectId id, Object value) {
		attributes.put(id, value);
	}

	/**
	 * @see Action#getResultType()
	 */
	public Class getResultType() {
		return SearchResult.class;
	}

	/**
	 * Gets human-readable description of this {@link Search} object in english language
	 * @return search summary in english
	 */	
	public String getSummaryEn() {
		return summaryEn;
	}

	/**
	 * Sets human-readable description of this {@link Search} object in english language
	 * @param summaryEn desired value of Search summary in english
	 */
	public void setSummaryEn(String summaryEn) {
		this.summaryEn = summaryEn;
	}

	/**
	 * Gets human-readable description of this {@link Search} object in russian language
	 * @return search summary in russian
	 */	
	public String getSummaryRu() {
		return summaryRu;
	}

	/**
	 * Sets human-readable description of this {@link Search} object in russian language
	 * @param summaryRu desired value of Search summary in russian
	 */
	public void setSummaryRu(String summaryRu) {
		this.summaryRu = summaryRu;
	}
	
	/**
	 * Returns localized version of human-readable description of this {@link Search} object.
	 * Summary is generated automatically by DBMI system depending on search conditions included
	 * in this object.
	 * @return returns {@link #getSummaryEn()} or {@link #getSummaryRu()} depending on
	 * caller's locale context
	 */
	public String getSummary() {
		return ContextProvider.getContext().getLocaleString(summaryRu, summaryEn);
	}
	
	/**
	 * Gets collection of {@link SearchResult.Column columns} to be included in {@link SearchResult}<br>
	 * If no columns was specified and card search is restricted to single template then set of columns 
	 * specified as {@link com.aplana.dbmi.model.Attribute#isSearchShow() 'show in search result'} for current 
	 * template will be used.<br>
	 * If no columns was specified and card search is not restricted to single template	then following hardcoded 
	 * set of columns will be used: name, description, region, creation date, template. 
	 * @return collection of {@link SearchResult.Column columns} to be included in {@link SearchResult}
	 */
	public Collection<Column> getColumns() {
		return columns;
	}

	/**
	 * Sets collection of {@link SearchResult.Column columns} to be included in {@link SearchResult}<br>
	 * If no columns was specified and card search is restricted to single template then set of columns 
	 * specified as {@link com.aplana.dbmi.model.Attribute#isSearchShow() 'show in search result'} for current 
	 * template will be used.<br>
	 * If no columns was specified and card search is not restricted to single template	then following hardcoded 
	 * set of columns will be used: name, description, region, creation date, template. 
	 * @param columns collection of {@link SearchResult.Column columns} to be included in {@link SearchResult}
	 */
	public void setColumns(Collection columns) {
		this.columns = columns;
	}
	
	/**
	 * 
	 * @return
	 * @since v.7
	 */
	public ObjectId getFetchLink()
	{
		return fetchLink;
	}
	
	/**
	 * 
	 * @param column
	 * @since v.7
	 */
	public void setFetchLink(ObjectId fetchLink)
	{
		if (!CardLinkAttribute.class.equals(fetchLink.getType()) &&
				!BackLinkAttribute.class.equals(fetchLink.getType()))
			throw new IllegalArgumentException("Not a card link attribute");
		this.fetchLink = fetchLink;
	}

	/**
	 * used in advanced search
	 * @param the name of xml file with the sql text, it's parameters, etc
	 * assigning non-empty value disables all other search-styles (byAttributes,
	 * byCode, byMaterials)
	 */
	public void setSqlXmlName(String sqlXmlName) {
		this.sqlXmlName = sqlXmlName;
		if (isBySql())
		{	// set all other search styles to "off" value...
			this.byAttributes = false;
			this.byCode = false;
			this.byMaterial = false;
		}
	}

	/**
	 * used in advanced search
	 * @return the name of xml file with the sql text, it's parameters, etc
	 */
	public String getSqlXmlName() {
		return sqlXmlName;
	}
	
	/**
	 * Checks if some special sql query is specified 
	 * @return true if custom sql query is specified, false - otherwise
	 */
	public boolean isBySql() {
		return this.sqlXmlName != null && (this.sqlXmlName.length() > 0);
	}

	public Filter getFilter() {
		return filter;
	}

	public void clearFilter(){
		filter = new Search.Filter();
	}

	public Set getIgnoredIds() {
		return ignoredIds;
	}

	public void setIgnoredIds(Set ignoredIds) {
		this.ignoredIds = ignoredIds;
	}

	/**
	 * Creates copy of this {@link Search} object
	 * @return new instance of {@link Search} class with same parameters as this one
	 */
	public Search makeCopy() {
		final Search result = new Search();
		result.setNameRu(nameRu);
		result.setNameEn(nameEn);
		result.setWords(words);
		result.setByAttributes(byAttributes);
		result.setByCode(byCode);
		result.setByMaterial(byMaterial);
		result.setSqlXmlName(sqlXmlName);
		result.setSqlParametersName(sqlParametersName);
		result.setColumns(columns == null ? null : new ArrayList(columns));
		result.setMaterialTypes(materialTypes == null ? null : new ArrayList(materialTypes));
		result.setStates(states == null ? null : new ArrayList(states));
		result.setTemplates(templates == null ? null : new ArrayList(templates) ); // (2009/12/28, RuSA) OLD: (templates)
		final Iterator i = attributes.entrySet().iterator();
		while (i.hasNext()) {
			final Map.Entry item = (Map.Entry)i.next();
			result.addAttribute((ObjectId)item.getKey(), item.getValue());
		}
		result.filter = new Search.Filter(filter);
		result.setIgnoredIds(ignoredIds == null ? null : new HashSet(ignoredIds)); 
		result.setParamsAliases(paramsAliases);
		return result;
	}

	public boolean isDontFetch() {
		return dontFetch;
	}

	public void setDontFetch(boolean dontFetch) {
		this.dontFetch = dontFetch;
	}
	
	/**
	 * @param specialSqlParams  <String, Object>
	 */
	public void setParamsAliases( Map<String, Object> sqlParams) {
		this.paramsAliases = sqlParams;
	}

	/**
	 * @result <String, Object>
	 */
	public Map<String, Object> getParamsAliases() {
		return paramsAliases;
	}
	
	public long getSearchLimit() {
		return searchLimit;
	}

	public void setSearchLimit(long searchLimit) {
		this.searchLimit = searchLimit;
	}
	
	public boolean isCountOnly() {
		return isCountOnly;
	}
	
	public void setCountOnly(boolean isCountOnly) {
		this.isCountOnly = isCountOnly;
	}
	
	/**
	 * @return the sqlParametersName
	 */
	public String getSqlParametersName() {
		return this.sqlParametersName;
	}

	/**
	 * @param sqlParametersName to set
	 */
	public void setSqlParametersName(String sqlParametersName) {
		this.sqlParametersName = sqlParametersName;
	}

	public static class Filter implements Serializable, Cloneable{

		private static final long serialVersionUID = Search.serialVersionUID;

		public static final Long CU_DONT_CHECK_PERMISSIONS = new Long(-1);
		public static final Long CU_DONT_CHECK_PERMISSIONS_FINAL_DOC = new Long(-2);
		public static final Long CU_RW_PERMISSIONS = new Long(0); 
		public static final Long CU_READ_PERMISSION = CardAccess.READ_CARD; 
		public static final Long CU_WRITE_PERMISSION = CardAccess.EDIT_CARD;
		

		public static int PGSIZE_UNLIMITED = 0;

		private Long currentUserPermission = CU_DONT_CHECK_PERMISSIONS;

		private int page = 1;	// first page = 1
		private int pageSize = PGSIZE_UNLIMITED;
		private int wholeSize = 0;
		// [VIAleksandrov] BR4J00036160 (����������� ������� ������������� � ������).
		// ���������, ��� ������� �� ����� ������������� �������� ���� � ������, 
		// ���� ���� �������� ���� �������� � {@link Search.Filter.currentUserPermission)
		private Collection templatesWithoutPermCheck = new ArrayList();

		// OLD: private SearchResult.Column orderColumn;   ������ ����������� �������
		// OLD: private int orderColumnN;					����� �� ������� ������ ��� orderColumn

		// List<SearchResult.OrderedColumn> ������ ����������� ������� � ������ �������
		private final List orderedColumns = new ArrayList(5);

		private boolean customCardSortOrder = false;
		
		/**
		 * Default constructor
		 */
		public Filter(){
		}
		
		/**
		 * Copy constructor
		 */
		public Filter(Filter f){
			this.currentUserPermission = f.currentUserPermission;
			this.customCardSortOrder = f.customCardSortOrder;
			this.orderedColumns.addAll(f.orderedColumns);
			this.page = f.page;
			this.pageSize = f.pageSize;
			this.wholeSize = f.wholeSize;
			this.templatesWithoutPermCheck.addAll(f.templatesWithoutPermCheck);
		}

		// [VIAleksandrov] BR4J00036160 (����������� ������� ������������� � ������).
		// TODO ����������� ��������� ���� ������� ���������,
		// ���� �� ����� ����������� ���������� �������� ������������ ������ � ������.
		// ��� ���������� ��� ���������� �������� ���� � DoSearch ��� �������� �������� � 
		// ������������ ������ � ������ �������!!

		/**
		 * Gets collection of {@link Template templates} allowed in search result without permission checking.
		 * @return collection of {@link Template} objects allowed in search result without permission checking.
		 */
		public Collection getTemplatesWithoutPermCheck() {
			return templatesWithoutPermCheck;
		}

		/**
		 * Sets collection of {@link Template templates} allowed in search result without permission checking.
		 * @param templates collection of {@link Template} objects allowed in search result without permission checking.
		 */	
		public void setTemplatesWithoutPermCheck(Collection templatesWithoutPermCheck) {
			this.templatesWithoutPermCheck = templatesWithoutPermCheck;
		}

		/**
		 * First column in sorted list.
		 * @return the first order list column
		 * @deprecated use orderColumns list instead
		 */
		public SearchResult.Column getOrderColumn() {
			// if (orderColumn == null) orderColumn = new SearchResult.Column();
			// return orderColumn;
			if (orderedColumns.isEmpty())
				orderedColumns.add( new OrderedColumn(new SearchResult.Column(), 1) );
			return ((OrderedColumn) orderedColumns.get(0)).getColumn();
		}

		/**
		 * Set first column for sorted list.
		 * @param orderColumn
		 * @param orderN: the column index/number in some external list, this 
		 * number is used for sorting inside sortOrderColumns.
		 * @deprecated use orderColumns list instead or use addOrderColumn.
		 */
		public void setOrderColumn(SearchResult.Column orderColumn) {
			if (orderColumn == null)
				this.orderedColumns.clear();
			else if (this.orderedColumns.isEmpty())
				orderedColumns.add( new OrderedColumn( orderColumn, 1) );
			else
				((OrderedColumn) orderedColumns.get(0)).setColumn( orderColumn);
		}

		public void setPage(int page) {
			this.page = page;
		}

		public int getPageSize() {
			return pageSize;
		}

		public void setPageSize(int pageSize) {
			this.pageSize = pageSize;
		}

		public Long getCurrentUserPermission() {
			return currentUserPermission;
		}

		public int getWholeSize() {
			return wholeSize;
		}

		public void setWholeSize(int wholeSize) {
			this.wholeSize = wholeSize;
		}

		/**
		 * @return the sort index of the first column of the ordering list.
		 * @deprecated use {@link orderColumns}} instead.
		 */
		public int getOrderColumnN() {
			// return orderColumnN;
			return (this.orderedColumns.isEmpty()) 
					? 0 
					: ((OrderedColumn) orderedColumns.get(0)).getColOrderNum();
		}

		/**
		 * Set the sort index of the first column of the ordering list.
		 * @param orderColumnN
		 * @deprecated use {@link orderColumns}} instead.
		 */
		public void setOrderColumnN(int orderColumnN) {
			// this.orderColumnN = orderColumnN;
			if (orderColumnN < 1)
				this.orderedColumns.clear();
			else if (this.orderedColumns.isEmpty())
				orderedColumns.add( new OrderedColumn( new SearchResult.Column(), orderColumnN) );
			else
				((OrderedColumn) orderedColumns.get(0)).setColOrderNum( orderColumnN);
		}

		/**
		 * @return the orderedColumns with sorting columns list of {$link OrderedColumn}.
		 */
		public List getOrderedColumns() {
			return this.orderedColumns;
		}

		/**
		 * Add ordered column into the ordering list orderedColumns.
		 * (!) Call sortOrderColumns after adding all items.
		 * @param orderColumn
		 * @param orderN: the column index/number in some external list, this 
		 * number is used for sorting inside sortOrderColumns.
		 */
		public void addOrderColumn(SearchResult.Column orderColumn, int orderN) {
			if (orderColumn == null) 
				return;
			//BR4J00015453 - New sorting column check added
			for(Object column: orderedColumns){
					if(orderColumn.equals(((OrderedColumn)column).getColumn()))  
					{
						return;
					}
			}
			orderedColumns.add(new OrderedColumn( orderColumn, orderN));
		}

		/**
		 * sort the orderedColumns according to the items colOrderNum.
		 */
		public void sortOrderColumns()
		{
			Collections.sort(this.orderedColumns,
					new Comparator() {
						// compare OrderedColumn items via colOrderNum
						public int compare(Object a, Object b) {
							if (a== null)
								return (b==null) ? 0 : -1;
							if (b == null)
								return 1;
							final OrderedColumn aCol = (OrderedColumn) a;
							final OrderedColumn bCol = (OrderedColumn) b;
							return aCol.getColOrderNum() - bCol.getColOrderNum();
						}
				});
		}

		public int getPage() {
			return page;
		}

		public String getCurrentUserPermissionString() {
			if (currentUserPermission.equals(CU_DONT_CHECK_PERMISSIONS))
				return "";
			else if (currentUserPermission.equals(CU_RW_PERMISSIONS))
				return CU_READ_PERMISSION.toString()+","+CU_WRITE_PERMISSION.toString();
			else if (currentUserPermission.equals(CU_READ_PERMISSION))
				return CU_READ_PERMISSION.toString();
			else if (currentUserPermission.equals(CU_WRITE_PERMISSION))
				return CU_READ_PERMISSION.toString()+","+CU_WRITE_PERMISSION.toString();
			return "0";
		}

		public void setCurrentUserRestrict(Long currentUserRestriction) {
			this.currentUserPermission = currentUserRestriction;
		}


		public Object clone(){
			Object o = null;
			try {
				o = super.clone();
			} catch (CloneNotSupportedException e) {}
			return o;
		}

		/**
		 * true, when sort order of the cards will be specified by the external SQL,
		 * false (mostly) if sort order specified inside columns. 
		 * @return true if custom sql query sort order will be using, false - otherwise.
		 */
		public boolean isCustomCardSortOrder() {
			return customCardSortOrder;
		}

		public void setCustomCardSortOrder(boolean customOrder) {
			this.customCardSortOrder = customOrder;
		}

		public static class PermissionsHelper {

			public static final String MNEM_CU_DONT_CHECK_PERMISSIONS = "CU_DONT_CHECK_PERMISSIONS";
			public static final String MNEM_CU_RW_PERMISSIONS = "CU_RW_PERMISSIONS"; 
			public static final String MNEM_CU_READ_PERMISSION = "CU_READ_PERMISSION";
			public static final String MNEM_CU_WRITE_PERMISSION = "CU_WRITE_PERMISSION";

			/**
			 * ����� ��������� ����� - ��� �����. ��. const Filter.CU_XXX.
			 * 		����: string = ������������� �����������, "�������� ����";
			 * 		��������: long = ����-��� ���.
			 */
			private static final Map /*<String,Long>*/ mnemonicPerm = new HashMap(4);

			/**
			 * �������� ����� � mnemonicPerm.
			 */
			private static final Map /*<Long, String>*/ permMnemonic = new HashMap(4);

			static {
				mnemonicPerm.put( MNEM_CU_DONT_CHECK_PERMISSIONS.toUpperCase(), CU_DONT_CHECK_PERMISSIONS);
				mnemonicPerm.put( MNEM_CU_RW_PERMISSIONS.toUpperCase(), CU_RW_PERMISSIONS);
				mnemonicPerm.put( MNEM_CU_READ_PERMISSION.toUpperCase(), CU_READ_PERMISSION);
				mnemonicPerm.put( MNEM_CU_WRITE_PERMISSION.toUpperCase(), CU_WRITE_PERMISSION);

				for ( Iterator/*Long, String*/ iterator = mnemonicPerm.entrySet().iterator(); 
						iterator.hasNext();) {
					final Map.Entry item = (Map.Entry) iterator.next();
					permMnemonic.put( item.getValue(), item.getKey());
	}
			}

	/**
			 * �������� ��� ����, ����-��� �������������� �����������.
			 * @param mnemonic: ����������� ���� (������������� ��� ��������)
			 * �� {@link MNEM_CU_XXX}
			 * @return ��. {@link Search.Filter.CU_XXX} ��� null, ���� ��� �����.
			 * (!) ���� �������� �������� �� ����-�� �� ����� �� �������� CU_XXX ����� ���-�� Null.
			 */
			public static Long getEnumFromString(String mnemonic)
			{
				if(mnemonic != null)
				{
					mnemonic = mnemonic.trim().toUpperCase();
					try {
						final Long code = new Long( Long.parseLong(mnemonic));
						return (permMnemonic.containsKey(code)) ? code : null; 
					} catch (NumberFormatException ex) {
						// ����� ��������� ������ - ��� ��������� ��������...
					}
					// ��������� �����
					return (Long) mnemonicPerm.get(mnemonic);
				}
				return null;
			}

			public static Long fromString(String name)
			{
				// init(); return (Long) mnemonicPerm.get(mnemonic);
				return getEnumFromString(name);
			}

			/**
			 * �������� ������������ ������, ����-��� ���� ���� �������.
			 * @param permCode
			 * @return ������������� ������, ��� null, ���� ��� ������ ����
			 * (��. {@link Filter.CU_XXX}).
			 */
			public static final String intoCode(Long permCode) {
				return (String) permMnemonic.get(permCode); 
			}
		}
	} // class Filter

	/**
	 * ��������� ��� �������� ������� � �� ����������� ������ (� ��������� 
	 * ������� ������)
	 * @author RAbdullin
	 *
	 */
	public static class OrderedColumn implements Serializable
	{
		
		private static final long serialVersionUID = 1L;

		private int colOrderNum;
		private Column column;

		/**
		 * @param colOrderNum
		 * @param column
		 */
		public OrderedColumn( Column column, int colOrderNum) {
			super();
			this.colOrderNum = colOrderNum;
			this.column = column;
		}

		/**
		 * @return the order in some external list
		 */
		public int getColOrderNum() {
			return this.colOrderNum;
		}

		/**
		 * @param colOrderNum set the order in some external list
		 */
		public void setColOrderNum(int colOrderNum) {
			this.colOrderNum = colOrderNum;
		}

		/**
		 * @return the column
		 */
		public Column getColumn() {
			return this.column;
		}

		/**
		 * @param column the column to set
		 */
		public void setColumn(Column column) {
			this.column = column;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return MessageFormat.format("'{' orderNum={0}, column={1} '}'", 
					new Object[] { new Integer(colOrderNum), 
						(column == null ? "null" : column.getName())
					}
				); 
		}
	}

}