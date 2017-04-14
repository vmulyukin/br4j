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
/**
 *
 */
package com.aplana.dbmi.jbr.util;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.BasePropertySelector;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.QueryFactory;

/**
 * ��������� �������� ��������� � ������� ������� ��������.
 * ��������:
 * 		1) �����( '=' ��� '.eg.'),
 * 		2) �� ����� ( '#' ��� '!=' ��� .neq.'),
 * 		3) ������ ( '>' ��� '.gt.'),
 * 		4) ������ ��� �����( '>=' ��� '=>' ��� '.gte.'),
 * 		5) ������ ( '<' ��� '.lt.'),
 * 		6) ������ ��� ����� ( '<=' ��� '=>' ��� '.lte.').
 * 		7) ������������ RegExp ('%=' ��� '=%')
 * ���� ��������:
 * 		1) ObjectId - ������ �������� = � #,
 * 		2) DataObject (�� id) - ������ �������� = � #,
 * 		3) ��������� ���� Integer/Long, Float, Date, String - ��� ��������.
 */
public class AttributeSelector extends BasePropertySelector implements Cloneable
{


	private ObjectId attrId;
	private CompareOperation cond;
	private List<ObjectId> path = null;
	private QueryFactory queryFactory;
	private Database database;
	
	public static final String DELIMITER = ",";

	/**
	 * �������������� ��������
	 */
	public static enum CompareOperation {
		EQ,		NEQ,
		LT,		LTE,
		GT,		GTE,
		RE
	};

	/**��������� ����������� ����������, ��� �� ������� ��� ����� ������e �����, � � ����� ����� ����� ��������
	 *@see createSelector method.
	 */
	static Map<String, CompareOperation> equals_st = new TreeMap<String, CompareOperation>(new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			if (o1.length() > o2.length()) {
				return -1;
			} else if (o1.length() < o2.length()) {
				return 1;
			} else {
				return o2.compareTo(o1);
			}
		}
	});

	static Map<CompareOperation, String> oper_names = new Hashtable<CompareOperation, String>();
	static {
		
		equals_st.put( "=", CompareOperation.EQ);
		equals_st.put( ".eq.", CompareOperation.EQ);

		equals_st.put( "#", CompareOperation.NEQ);
		equals_st.put( "!=", CompareOperation.NEQ);
		equals_st.put( ".neq.", CompareOperation.NEQ);

		equals_st.put( "<", CompareOperation.LT);
		equals_st.put( ".lt.", CompareOperation.LT);

		equals_st.put( ">", CompareOperation.GT);
		equals_st.put( ".gt.", CompareOperation.GT);

		equals_st.put( "<=", CompareOperation.LTE);
		equals_st.put( "=<", CompareOperation.LTE);
		equals_st.put( ".lte.", CompareOperation.LTE);

		equals_st.put( ">=", CompareOperation.GTE);
		equals_st.put( "=>", CompareOperation.GTE);
		equals_st.put( ".gte.", CompareOperation.GTE);
		
		equals_st.put( "%=", CompareOperation.RE);
		equals_st.put( "=%", CompareOperation.RE);

		// "������������" ����������� ��� ��������
		oper_names.put( CompareOperation.EQ, "=");
		oper_names.put( CompareOperation.NEQ, "<>");
		oper_names.put( CompareOperation.LT, "<");
		oper_names.put( CompareOperation.LTE, "<=");
		oper_names.put( CompareOperation.GT, ">");
		oper_names.put( CompareOperation.GTE, ">=");
		oper_names.put( CompareOperation.RE, "%=");
	}

	/**
	 * ����� ��������, ����-��� ��������� oper.
	 * @param oper
	 * @return ��� �������� ��� null, ���� ��� ����� ��������.
	 */
	public static CompareOperation findOper(final String oper) {
		return (oper != null) ? equals_st.get(oper.toLowerCase()) : null;
	}

	/**
	 * ���������, ���������� �� �����-���� �������� ������ ������.
	 * @param s
	 * @return ������ ��������, ��� null, ���� � ������ ��� �� ����� ��������.
	 */
	public static String containOper(final String s) {
		if (s != null) {
			final String sLower = s.toLowerCase();

			Iterator<String> iterator = equals_st.keySet().iterator();
			while (iterator.hasNext()) {
				final String condStr = iterator.next();
				if (sLower.indexOf(condStr) != -1) {
					// ����� �������.
					return condStr;
				}
			}
		}
		return null; // ��� �� ����� ��������
	}

	/**
	 * ������� ��������.
	 * @param mixedValue: �������� ���� "<id_��������>=<id_��������>"
	 * @return
	 * @throws DataException
	 */
	public static AttributeSelector createSelector(String mixedValue) throws DataException{
		if (mixedValue != null) mixedValue= mixedValue.trim();
		if (mixedValue == null || mixedValue.length() < 1)
			return null;

		final String cond_st = containOper(mixedValue);
		if (cond_st == null) { // �� ������� �� ���� ������� ���������
			throw new DataException( "general.unique",
					new Object[] {"No operation of '"+equals_st.entrySet()+ "' assumed inside \'" + mixedValue+ "\'"}
			);
		}
		final int cond_size = cond_st.length();
		final int iStart = mixedValue.toLowerCase().indexOf(cond_st);
		final AttributeSelector selector = new AttributeSelector(
				mixedValue.substring(0, iStart),
				mixedValue.substring( iStart+ cond_size));
		selector.setCondition(cond_st);
		return selector;
	}

	/**
	 * ������� �������� � ������������ ����� �������� �� ���������.
	 * @param mixedValue: �������� ���� "<id_��������>=<id_��������>"
	 * @param attributeType: ������ ���� Class - ��� �������� �� ���������
	 * @return
	 * @throws DataException
	 */
	public static AttributeSelector createSelector( String mixedValue,
			Class<? extends Attribute> attributeType) throws DataException
	{
		if (mixedValue != null) mixedValue= mixedValue.trim();
		if (mixedValue == null || mixedValue.length() < 1)
			return null;

		final String cond_st = containOper(mixedValue);
		if (cond_st == null) { // �� ������� �� ���� ������� ���������
			throw new DataException( "general.unique",
					new Object[] {"No operation of '"+equals_st.entrySet()+ "' assumed inside \'" + mixedValue+ "\'"}
			);
		}
		final int cond_size = cond_st.length();
		final int iStart = mixedValue.toLowerCase().indexOf(cond_st);
		final AttributeSelector selector = new AttributeSelector(
				mixedValue.substring(0, iStart),
				mixedValue.substring( iStart+ cond_size),
				attributeType);
		selector.setCondition(cond_st);
		return selector;
	}

	public static String makeAttrInfo(String fmt_2, Card card, ObjectId attrId) {
		return MessageFormat.format(fmt_2, new Object[] {
				(card == null) ? null : card.getId(), attrId });
	}

	/**
	 * Create by attribute code and value code.
	 * @param propName alias in objectids.properties or attribute_code with default type ReferenceAttribute.
	 * @param value the alias od value code of ReferenceValue.
	 */
	private AttributeSelector(String propName, String value) {
		this(propName, value, null);
	}

	/**
	 * Create by attribute code and value code.
	 * @param propName alias in objectids.properties or attribute_code with default type attributeType.
	 * @param value the alias od value code of ReferenceValue.
	 * @param attributeType default attribute class in the case when propName is attributeCode.
	 * null means ReferenceAttribute.
	 */
	private AttributeSelector(String propName, String value,
			Class<? extends Attribute> attributeType)
	{
		super(null, value);
		final boolean isStartByDot = (propName.charAt(0) == '.');
		if (isStartByDot) {
			int separatorPos = propName.indexOf(PathAttributeDescriptior.REG_ATTR_SEPARATOR);
			if (separatorPos > 1) {
				this.propName = propName.substring(1, separatorPos);
				propName = propName.substring(separatorPos + 1);
			} else {
				this.propName = propName.substring(1);
				propName = null;
			}
		}

		path = new PathAttributeDescriptior(propName).getAttrIds();

		// ��������� � attrId ��������� ��� �������� �������������
		if (this.path != null && path.size() > 1) {
			this.attrId = path.get(path.size() - 1);
			path = path.subList(0, path.size() - 1);
		} else {
			// attribute id ...
			this.path = null;
			if (attributeType == null) attributeType = ReferenceAttribute.class;
			this.attrId = IdUtils.smartMakeAttrId( propName, attributeType);
		}

		// value ids ...
		final List<ObjectId> valueIds = IdUtils.stringToAttrIds(value, ReferenceValue.class, false, DELIMITER, false);
		super.value = IdUtils.makeObjectIdStringLine(valueIds, DELIMITER);
	}
	
	public int hashCode() {
		int hashAttr = 0;
		int hashValue = 0;
		if (null != attrId)
			hashAttr = attrId.hashCode();
		if (null != super.value)
			hashValue = super.value.hashCode();
			
		return hashAttr ^ hashValue;
	}

	public ObjectId getAttrId() {
		// TODO: ��������� ���������� ������������� (��������� ��� �������� �������������)
		return this.attrId == null ? new ObjectId(StringAttribute.class, "fake") : this.attrId;
	}

	public String getPropName()
	{
		return propName;
	}

	public String getValue2Compare()
	{
		return value;
	}

	public void setValue(String value){
		this.value = value;
	}

	private void setCondition(final String condition) throws DataException {
		this.cond = findOper(condition);
		if (condition != null && this.cond == null)
			throw new DataException( "general.unique",
					new Object[] {"Unsupported operation \'"+ condition+
					"\' (available only "+ equals_st.entrySet()+ ")" }
			);
	}

	/**
	 * Checks if value of given object property matches desired value of this property.
	 * <br>
	 * If value of property is a {@link DataObject} or {@link ObjectId} instance then
	 * desired value is interpreted as {@link ObjectId identifier} and comparison
	 * of two {@link ObjectId} instances is performed.
	 * <br>
	 * Otherwise simple string comparison is performed.
	 * @return true is value of given object property is found to be the same as
	 * desired value, false otherwise.
	 */
	@Override
	public boolean satisfies(Object object) {
		try {
			if (load)
				loadIntermediateObjects(object);
			Object propValue = getValueByName(object, propName);

			/* ��������� ������� */
			final boolean flagEq = (cond == CompareOperation.EQ);
			final boolean flagNEq = (cond == CompareOperation.NEQ);
			// �������� �� ���� - ��� �� ���� �����, ��� �������� �� null
			if (value.equalsIgnoreCase("null") /*|| value.equalsIgnoreCase("0")*/) {
				if ( flagEq || flagNEq )
					return ((propValue == null)) == flagEq;
				logger.warn("For null value only operation '=' or '#' are permitted inside parameter attr_test");
				return false;
			}
			if (propValue == null) {
				if ( flagEq || flagNEq )
					return !flagEq; // (null != ��������) ������ true, (null == ��������) ������ false
				return false;
			}

			// ReferenceValue: DataObject ������ ������������ ����� ID ->
			// ��������� ID � ����� �������� �� �������� (propValue instanceof ObjectId))
			if (propValue instanceof DataObject) { // ����  propName referencevalue, template, card ��� ������ ������ ����������� �� DataObject
				propValue = ((DataObject) propValue).getId();
				// ����� propValue ������������ ��� Id ������ DataObject...
				// ��������, ��� ReferenceValue ����� ���
			}

			if (propValue instanceof ObjectId) {
				if ( flagEq || flagNEq ) {
					final Class<?> type = ((ObjectId) propValue).getType();
					for(String strValue : value.split(DELIMITER)) {
						ObjectId id = ObjectId.predefined(type, strValue);
						if (id == null)
							try {
								id = new ObjectId(type, Long.parseLong(strValue));
							} catch (NumberFormatException e) {
								id = new ObjectId(type, strValue);
							}
						final boolean isEq = propValue.equals(id);
						if(flagEq && isEq) {
							return true;
						}
						if(!flagEq && isEq) {
							return false;
						}
						//return propValue.equals(id) == flagEq;
					}
				}
				// TODO: (2010/12/27, RuSA) �.�. ������ ����-������� ��� ���������� ������� ������ �������� ��������� ��� ObjectId (��� �������� � ���������� ������� ��� �������� �������� ����)?
				// ��������� ID ����� ��������� ������ �� ��������� - ��������� ������ false
				logger.warn( "Operation "+ cond+ " for comparing ObjectIds' always give false");
				return flagEq ? false : true;
			}

			final boolean isScalarValue =
					(propValue instanceof Integer) ||
					(propValue instanceof Long) ||
					(propValue instanceof Float) ||

					(propValue instanceof Date) ||
					(propValue instanceof String)
				;
			if (isScalarValue) {
				final int chkCode = getEqualsValue(propValue, value);
				switch (cond) {
					case EQ: { // =
						return (chkCode == 0);
					}
					case NEQ: { // #
						return (chkCode != 0);
					}
					case LT: { // <
						return (chkCode < 0);
					}
					case GT: { // >
						return (chkCode > 0);
					}
					case LTE: { // <=, =<
						return (chkCode <= 0);
					}
					case GTE: { // >=, =>
						return (chkCode >= 0);
					}
					case RE: {
						try {
							Pattern p = Pattern.compile(this.value);
							return p.matcher(String.valueOf(propValue)).matches();
						} catch (Exception ex) {
							logger.warn("RegExp comparation fails while mathing " + this.value + " pattern in "+ propValue +" string due to " + ex.getMessage(), ex);
							return false;
						}
					}
					default: { // �� ������� �� ������ �������
						logger.warn("Unsupported operation "+ cond+ " inside parameter attr_test");
						return false;
					}
				} // switch
			}
			/** ****************** */
			if ( flagEq || flagNEq ) {
				return value.equals(propValue) == flagEq;
			}
			logger.warn("Unsupported operation "+ cond+ " inside parameter attr_test for non-scalar value type " + propValue.getClass());
			return false; // ��������, ���� �� ����������� �� ���� �������
		} catch (Exception e) {
			logger.warn("Error retrieving property " + propName + " of object "
					+ object.getClass().getName(), e);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private int getEqualsValue(Object objValue, String strValue)
		throws ParseException
	{
		Object val = null;
		if (objValue instanceof Date) {
			final DateFormat suffix_date = new SimpleDateFormat("yyyy-MM-dd");
			val = suffix_date.parse(strValue);
		} else if (objValue instanceof Integer) {
			val = Integer.parseInt(strValue);
		} else if (objValue instanceof Long) {
			val = Long.parseLong(strValue);
		} else if (objValue instanceof String) {
			val = strValue;
		} else if (objValue instanceof Float) {
			val = Float.parseFloat(strValue);
		}
		if (objValue == null)
			return (val == null) ? 0 : -1;
		return ((Comparable)objValue).compareTo(val);
	}

	private void loadIntermediateObjects(Object object) throws DataException {
		if (propName == null || propName.length() == 0) {
			return;
		}
		String[] parts = propName.split("\\.");
		String prop = parts[0];
		for (int i = 0; i < parts.length - 1; i++) {
			Object val;
			try {
				val = PropertyUtils.getProperty(object, prop);
			} catch (Exception e) {
				logger.warn("Error retrieving property " + prop +
						" of object " + object.getClass().getName(), e);
				return;
			}
			if (!(val instanceof DataObject))
				return;
			DataObject intermediate = (DataObject) val;
			if (intermediate.getId() == null)
				return;
			ObjectQueryBase query = getQueryFactory().getFetchQuery(intermediate.getId().getType());
			query.setId(intermediate.getId());
			intermediate = (DataObject) getDatabase().executeQuery(getSystemUser(), query);
			try {
				PropertyUtils.setProperty(object, prop, intermediate);
			} catch (Exception e) {
				logger.warn("Error modifying property " + prop +
						" of object " + object.getClass().getName(), e);
				return;
			}

			prop += "." + parts[i + 1];
		}
	}

	/**
	 * ��� baseObj: Card  ��������� �������� �������� �� ��� ����� name, ���������
	 * � ���� id ��� ����. (!) ���� ��� ���������� � �����, �� ������������
	 * ������� ��������� �������� ��� ������� ���� Card.
	 * ��� ������ ����� - ��������� �������� �� ����� name.
	 */
	@Override
	protected Object getValueByName(Object baseObj, String name) throws Exception {
		if (name == null && this.attrId == null) {
			return null;
		}
		Object result = baseObj;
		if (name != null) {
			result = super.getValueByName(baseObj, name);
		}
		if (path != null) {
			for (ObjectId pathPart : path) {
				if (!(result instanceof Card)) {
					logger.warn("Condition contains path but there is no card value on some of steps");
				return null;
			}
				result = getAttributeValue((Card) result, pathPart);
				ObjectId cardId = ObjectIdUtils.getObjectId(Card.class, (String) result, true);
				ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
				query.setId(cardId);
				result = getDatabase().executeQuery(getSystemUser(), query);
			}
		}

		if (this.attrId != null) {
			if (result instanceof Card) {
				result = getAttributeValue((Card) result, attrId);
			} else {
				logger.warn("Attribute is defined but object is no card");
				result = null;
			}
		}
		return result;
	}

	private Object getAttributeValue(final Card c, ObjectId attributeId) throws DataException {
		final Attribute attr = c.getAttributeById(attributeId);
		if (attr == null) {
			logger.warn(makeAttrInfo(ProcessCard.MSG_CARD_0_HAS_NO_ATTRIBUTE_1, c, attributeId));
			return null;
		}
			try {
				if (attr instanceof ListAttribute)
					return ((ListAttribute)attr).getValue();
				if (attr instanceof StringAttribute)
					return ((StringAttribute)attr).getValue();
				if (attr instanceof TextAttribute)
					return ((TextAttribute)attr).getValue();
				if (attr instanceof IntegerAttribute)
					return new Integer(((IntegerAttribute)attr).getValue());
				if (attr instanceof DateAttribute)
					return ((DateAttribute)attr).getValue();
				if (attr instanceof CardLinkAttribute){
					Iterator it = ((CardLinkAttribute)attr).getLabelLinkedMap().keySet().iterator();
					if(it.hasNext())
						return ((ObjectId)it.next()).getId().toString();
					else // ������ CardLink �������
						return null;
				}
				if (attr instanceof BackLinkAttribute) {
					final ListProject action = new ListProject();
					action.setCard(c.getId());
					action.setAttribute(((BackLinkAttribute)attr).getId());
					final ActionQueryBase query = getQueryFactory()
							.getActionQuery(action);
					query.setAction(action);
					final SearchResult rs = (SearchResult) getDatabase()
							.executeQuery(getSystemUser(), query);
					final List<Card> cards = CardUtils.getCardsList(rs);
					if (cards != null )
						return cards.get(0).getId().getId().toString();
					else
						return cards;
				}
				if (attr instanceof PersonAttribute){
					// personId as String
					//��� ��� �������� ���������� �� �� ����������� ��������, �� ���� ��������� ���� ��� ���
					// TODO
					Iterator iterator = ((PersonAttribute)attr).getValues().iterator();
					Person person=null;
					if(iterator.hasNext()){
						person = (Person) iterator.next();
					}

					return person==null?null:person.getId().getId().toString();
				}
			} catch(NullPointerException x) {
				return null;
			}

			return attr.getId();
//			if (attr instanceof BackLinkAttribute) return ((BackLinkAttribute)attr).getId();
//			if (attr instanceof TypedCardLinkAttribute) return ((TypedCardLinkAttribute)attr).getId();
//			if (attr instanceof CardLinkAttribute) return ((CardLinkAttribute)attr).getId();
//			if (attr instanceof TreeAttribute) return ((TreeAttribute)attr).getId();
//			if (attr instanceof MaterialAttribute) return ((MaterialAttribute)attr).getId();
		}

	/**
	 * ������� �������
	 * @return
	 */
	public CompareOperation getCond() {
		return cond;
	}

	/**
	 * ���������� �������
	 * @param cond
	 */
	public void setCond(CompareOperation cond) {
		this.cond = cond;
	}

	/**
	 * @return ��������� ����������� ������� ��������
	 */
	public String getOperName() {
		return oper_names.get(this.cond);
	}

	@Override
	public String toString() {
		return MessageFormat.format( "?({0}{1}{2})", new Object[] {
					this.propName,
					this.getOperName(),
					this.value
					});
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected AttributeSelector clone() throws CloneNotSupportedException {
		AttributeSelector sel = (AttributeSelector) super.clone();
		if (this.path != null) {
			sel.path = new ArrayList<ObjectId>(this.path);
		}
		if (this.attrId != null) {
			sel.attrId = new ObjectId(this.attrId.getType(), this.attrId.getId());
		}
		return sel;
	}

	public void setQueryFactory(QueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	@Override
	protected QueryFactory getQueryFactory() {
		return queryFactory != null? queryFactory : super.getQueryFactory();
	}

	@Override
	protected Database getDatabase() {
		return database != null? database : super.getDatabase();
	}
	
	

}
