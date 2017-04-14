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
package com.aplana.dbmi.model.util;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;

import java.util.*;

/**
 * Utility class for operations with {@link ObjectId} and {@link DataObject} instances
 */
public class ObjectIdUtils {
	
	/**
	 * Creates {@link ObjectId} from given String.
	 * Firstly it tests if there is exists {@link ObjectId#predefined(Class, String)} predefined} identifier with
	 * name equal to given string. If such predefined identifier exists then returns it as result.
	 * Otherwise constructs new ObjectId  and sets its {@link ObjectId#getId() id} property
	 * to value defined by given string. If isNumeric argument is true, then code is converted to Long
	 * object, otherwise code is used directly 
	 * If isNumeric argument is true 
	 * @param clazz type of {@link ObjectId} being created
	 * @param code predefined identifier or value of identifier
	 * @param isNumeric true if Long-based identifier is expected, false - otherwise
	 * @return created ObjectId
	 */
	public static ObjectId getObjectId(Class<?> clazz, String code, boolean isNumeric) {
		ObjectId result = ObjectId.predefined(clazz, code);
		if (result == null) {
			if (isNumeric) {
				result = new ObjectId(clazz, Long.parseLong(code));
			} else {
				result = new ObjectId(clazz, code);
			}
		}
		return result;
	}
	
	/**
	 * Same as getObjectId(Class<?> clazz, String code, boolean isNumeric), but allows to declare a set
	 * of data objects classes which attribute alias in code can be associated with.
	 * @param classes set of allowed attribute classes
	 * @param defaultClass Self-descriptive, class is used for "plain" initialization of {@link ObjectId} if no alias had been found.
	 * @param code predefined identifier or value of identifier
	 * @param isNumeric true if Long-based identifier is expected, false - otherwise; used only for "plain" initialization, ignored otherwise
	 * @return created ObjectId
	 */
	public static ObjectId getObjectId(List<Class<? extends Attribute>> classes, Class<?> defaultClass, String code, boolean isNumeric){
		ObjectId result;
		for (Class<?> clazz : classes) {
			result = ObjectId.predefined(clazz, code);
			if (result != null)
				return result;
		}
		if (isNumeric) {
			result = new ObjectId(defaultClass, Long.parseLong(code));
		} else {
			result = new ObjectId(defaultClass, code);
		}
		return result;
	}
	
	public static ObjectId getAttrObjectId(String codeWithPrefix, String delimiter){
		String[] prefixAndCode = codeWithPrefix.trim().split(delimiter, 2);
		if(prefixAndCode.length < 2) return null;
		Class<? extends Attribute> attrClass = AttrUtils.getAttrClass(prefixAndCode[0].trim());
		if(attrClass == null) return null;
		return getObjectId(attrClass, prefixAndCode[1].trim(), false);
	}

	/**
	 * Creates Map from given collection of {@link DataObject} descendants.
	 * @param collection collection of {@link DataObject} descendants
	 * @return Map where keys are {@link ObjectId} instances 
	 * and values are given {@link DataObject data objects}
	 */
	public static <T extends DataObject> Map<ObjectId, T> collectionToObjectIdMap(Collection<T> collection) {
		if (collection == null) return null;
		return fillObjectIdMapFromCollection( new HashMap<ObjectId, T>(collection.size()), collection);
	}

	/**
	 * Fills given Map with values from given {@link DataObject} collection.
	 * For every item in collection value returned by {@link DataObject#getId()} is used as keys
	 * and item itself - as value 
	 * @param map map to be filled
	 * @param collection collection of {@link DataObject} descendants
	 */
	public static <T extends DataObject> Map<ObjectId, T> fillObjectIdMapFromCollection(Map<ObjectId, T> map, 
			Collection<T> collection
		) 
	{
		if (map != null && collection != null) {
			for (final T obj : collection) {
				if (obj != null && obj.getId() != null)
					map.put(obj.getId(), obj);
			}
		}
		return map;
	}
	
	/**
	 * Creates set containing all unique identifiers from given collection of {@link DataObject}
	 * @param collection collection of {@link DataObject}
	 * @return set of {@link ObjectId}
	 */
	public static Set<ObjectId> collectionToSetOfIds(Collection<?> collection) {
		if (collection == null) return null;
		return fillObjectIdSetFromCollection(new LinkedHashSet<ObjectId>(), collection);
	}

	/**
	 * Fills given set of {@link ObjectId} with identifiers from collection 
	 * of {@link DataObject} or (@link ObjectId). 
	 * @param dest of {@link ObjectId}
	 * @param collection list of {@link DataObject} or (@link ObjectId).
	 */
	public static Set<ObjectId> fillObjectIdSetFromCollection(Set<ObjectId> dest, Collection<?> collection) {
		if (dest != null && collection != null) {
			for (Object aCollection : collection) {
				final ObjectId objId = getIdFrom(aCollection);
				if (objId != null) dest.add(objId);
			}
		}
		return dest;
	}
	
	/**
	 * Returns set filled with identifiers of objects from given collection
	 * @param dataObjects collection of {@link DataObject} or (@link ObjectId).
	 * @return set filled with identifiers of objects from given collection
	 */
	public static Set<ObjectId> getObjectIds(Collection<?> dataObjects) {
		return collectionToSetOfIds(dataObjects);
	}
	
	/**
	 * Compares two collections of {@link DataObject} instances.
	 * For every item in first collection checks if there is exists item with same 
	 * {@link DataObject#getId() identifier} in second collection and vice versa.
	 * One or both of arguments could be null. In this case null collection is considered
	 * as an empty collection.
	 * Order of records in collections is not significant.
	 * @param col1 collection of {@link DataObject} 
	 * @param col2 collection of {@link DataObject}
	 * @return true if both collection contains same set of {@link DataObject data objects}, 
	 * otherwise returns false 
	 */
	public static boolean isSameDataObjects(Collection<?> col1, Collection<?> col2) {
		boolean empty1 = (col1 == null || col1.isEmpty()),
			empty2 = (col2 == null || col2.isEmpty());
		
		if (empty1 && empty2) {
			return true;
		} else if (empty1 != empty2) {
			return false;
		} else {
			final Set<ObjectId> 
				ids1 = ObjectIdUtils.collectionToSetOfIds(col1),
				ids2 = ObjectIdUtils.collectionToSetOfIds(col2);
			if (ids1.size() != ids2.size()) {
				return false;
			}
			for (ObjectId anIds1 : ids1) {
				if (!ids2.contains(anIds1)) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Compares two collections of {@link ObjectId} instances.
	 * Checks if both collections contains same identifiers in same order.
	 * One or both of arguments could be null. In this case null collection is considered
	 * as an empty collection. 
	 * @param col1 collection of {@link ObjectId} 
	 * @param col2 collection of {@link ObjectId}
	 * @return true if both collection contains same set of {@link ObjectId} instances in same order, 
	 * otherwise returns false 
	 */
	public static boolean isSameObjectIdsInSameOrder(List<ObjectId> col1, List<ObjectId> col2) {
		boolean empty1 = (col1 == null || col1.isEmpty()),
			empty2 = (col2 == null || col2.isEmpty());
		if (empty1 && empty2) {
			return true;
		} else if (empty1 != empty2) {
			return false;
		} else if (col1.size() != col2.size()) {
			return false;
		} else {
			final Iterator<ObjectId> i = col1.iterator();
			final Iterator<ObjectId> j = col2.iterator();
			while (i.hasNext()) {
				final ObjectId 
					id1 = i.next(),
					id2 = j.next();
				if ((id1 == null) || !id1.equals(id2)) {
					return false;
				}
			}
			return true;
		}
	}
	
	/**
	 * Intersection two collections of {@link DataObject} or {@link ObjectId}  instances.
	 * Order of records in collections is not significant.
	 * @param col1 collection of {@link DataObject} or {@link ObjectId}
	 * @param col2 collection of {@link DataObject} or {@link ObjectId}
	 * @return true if intersection collection is not empty, 
	 * otherwise returns false 
	 */
	public static boolean isIntersectionDataObjects(Collection<?> col1, Collection<?> col2) {
		boolean empty1 = (col1 == null || col1.isEmpty());
		boolean empty2 = (col2 == null || col2.isEmpty());
		if (empty1 || empty2)
			return false;
		else {
			final Set<ObjectId> ids1 = ObjectIdUtils.collectionToSetOfIds(col1);
			final Set<ObjectId> ids2 = ObjectIdUtils.collectionToSetOfIds(col2);
			for (ObjectId id : ids1) {
				if (ids2.contains(id))
					return true;
			}
			return false;
		}
	}

	/**
	 * �������� id �� obj ���������� �� ���� obj.
	 * @param obj ������ � ����� DataObject ��� ObjectId.
	 * @return ������� ObjectId ����-��� obj.
	 */
	public static ObjectId getIdFrom(Object obj) {
		return (obj instanceof DataObject)
					? ((DataObject) obj).getId()
					: (ObjectId) obj;
	}
	
	public static String getIdStringFrom(Object obj) {
		return  (obj == null) 
					? null
				: (obj instanceof ObjectId)					
					? (String) ((ObjectId) obj).getId()
				: (obj instanceof String)
					?  (String)obj
				: obj.toString()
			;
	}

	public static Long getIdLongFrom(Object obj) {
		return  (obj == null) 
					? null
				: (obj instanceof DataObject)
					? (Long) ((DataObject) obj).getId().getId()
				: (obj instanceof ObjectId)
					? (Long) ((ObjectId) obj).getId()
				: (obj instanceof Long)
					? (Long) obj
				: Long.parseLong(obj.toString())
			;
	}

	/**
	 * Converts collection of {@link ObjectId} or {@link DataObject} to string 
	 * containing comma-delimited list of identifiers.
	 * NOTE: all ObjectId instances must use Long objects as its identifiers 
	 * @param ids list of {@link ObjectId} instances to be converted to string
	 * @return string containing comma-delimited list of identifiers
	 * @throws ClassCastException if given {@link ObjectId} identifiers stores non-Long values
	 */
	public static String numericIdsToCommaDelimitedString(Collection<?> ids) {
		if (ids == null || ids.isEmpty())
			return "";
		final StringBuilder result = new StringBuilder();
		for ( Iterator<?> i = ids.iterator(); i.hasNext(); ) {
			final Object obj = i.next();
			if (obj == null) continue;
			// final ObjectId id = getIdFrom(obj); result.append(((Long)id.getId()).longValue());
			result.append( getIdLongFrom(obj).longValue() );
			if (i.hasNext())
				result.append(',');
		}
		return result.toString();
	}
	
	/**
	 * ������� �� �� ������ ��������
	 * @param cards ������ ��������
	 * @return ��� ObjectId
	 */
	public static Set<ObjectId> cardsToObjectIdsSet(Collection<Card> cards) {
		if (cards == null || cards.isEmpty())
			return new HashSet<ObjectId>();
		final Set<ObjectId> result = new HashSet<ObjectId>(cards.size());
		for (final Card obj : cards) {
			if (obj == null || obj.getId() == null) continue;
			result.add(obj.getId());
		}
		return result;
	}
	
	public static String numericIdsToCommaDelimitedString(Collection<?> ids, String delim, String prefix, String suffix) {
		if (ids == null || ids.isEmpty())
			return "";
		final StringBuilder result = new StringBuilder();
		for ( Iterator<?> i = ids.iterator(); i.hasNext(); ) {
			final Object obj = i.next();
			if (obj == null) continue;
			// final ObjectId id = getIdFrom(obj); result.append(((Long)id.getId()).longValue());
			result.append(prefix).append( getIdStringFrom(obj)).append(suffix);
			if (i.hasNext())
				result.append(delim);
		}
		return result.toString();
	}
	
	/**
	 * Parses comma-delimited string of numbers and converts it to List of {@link ObjectId} instances
	 * Order of identifiers will be kept. Duplicate entries will not be removed.
	 * @param ids string containing comma-delimited list of numbers
	 * @param clazz desired type for {@link ObjectId} instances
	 * @return List of {@link ObjectId} which identifiers is a Long values
	 * @throws NumberFormatException if given string contains other symbols except of digits and commas
	 */
	public static List<ObjectId> commaDelimitedStringToNumericIds(String ids, Class<?> clazz) {
		if (ids == null || "".equals(ids.trim())) {
			return new ArrayList<ObjectId>(0);
		}
		final String[] strings = ids.trim().split(",");
		final List<ObjectId> result = new ArrayList<ObjectId>(strings.length);
		for (final String st : strings) {
			result.add(getObjectId(clazz, st.trim(), true));
		}
		return result;
	}
	
	/**
	 * Parses comma-delimited string of object identifiers and converts it to
	 * List of {@link ObjectId} instances. Order of identifiers will be kept.
	 * Duplicate entries will not be removed.
	 * 
	 * @param ids
	 *                string containing comma-delimited list of identifiers
	 *                (predefined string or string id)
	 * @param clazz
	 *                desired type for {@link ObjectId} instances
	 * @return List of {@link ObjectId}
	 * @see #getObjectId(Class, String, boolean)
	 */
	public static List<ObjectId> commaDelimitedStringToIds(String ids, Class<?> clazz) {
		if (ids == null || "".equals(ids.trim())) {
			return new ArrayList<ObjectId>(0);
		}
		final String[] strings = ids.split(",");
		final List<ObjectId> result = new ArrayList<ObjectId>(strings.length);
		for (String string : strings) {
			result.add(getObjectId(clazz, string.trim(), false));
		}
		return result;
	}
	
	/**
	 * Takes string and forms {@link ObjectId} to {@link ObjectId} {@link Map}. Spaces are ignored.
	 * @param string
	 * 	String with Map definition
	 * @param keyClass
	 * 	required class of map key
	 * @param isKeyIdNumeric
	 * 	Is key id numeric or not. Used only during "plane" initialization (when alias search fails)
	 * @param valueClass
	 * 	required class of map value
	 * @param isValueIdNumeric 
	 * 	Is value id numeric or not. Used only during "plane" initialization (when alias search fails)
	 * @param entryDelimiter
	 *  delimiter for entries
	 *  @param keyValueDelimiter
	 *  delimiter for key and value 
	 */
	public static Map<ObjectId, ObjectId> stringToIdsMap(
		String string, 
		Class<?> keyClass, 
		boolean isKeyIdNumeric, 
		Class<?> valueClass, 
		boolean isValueIdNumeric, 
		String entryDelimiter, 
		String keyValueDelimiter
	){
		HashMap<ObjectId, ObjectId> map = new HashMap<ObjectId, ObjectId>();
		String pairs[] = string.split(entryDelimiter);
		for(String pair : pairs){
			String[] entry = pair.trim().split(keyValueDelimiter, 2);
			ObjectId key = getObjectId(keyClass, entry[0].trim(), isKeyIdNumeric);
			ObjectId value = getObjectId(valueClass, entry[1].trim(), isValueIdNumeric);
			if(key != null && value != null) map.put(key, value);
		}
		return map;
	}
	
	public static Map<ObjectId, ObjectId> stringToIdsMap(String string, Class<?> keyClass, boolean isKeyIdNumeric, Class<?> valueClass, boolean isValueIdNumeric){
		return stringToIdsMap(string, keyClass, isKeyIdNumeric, valueClass, isValueIdNumeric, ",", "=");
	}
	

	/**
	 * Takes string and forms {@link ObjectId} to {@link ObjectId} {@link Map}. String representation of
	 * id is alias of one of the Attribute descendants followed with classIdDelimiter and Attribute code/alias.
	 * Format: 'link:jbr.ReqAuthor->link:jbr.outcoming.citizenAppealAddressee, link:jbr.incoming.sender->link:jbr.outcoming.receiver'
	 * There is:
	 *  entryDelimiter: ','
	 *  keyValueDelimiter: '->'
	 *  classIdDelimiter: ':'
	 * @param string
	 *   String with Map definition
	 * @param entryDelimiter
	 *   Delimiter between entries into input string
	 * @param keyValueDelimiter
	 *   Delimiter into entry between key and value
	 * @param classIdDelimiter
	 *   Delimiter for attribute type prefix
	 * @return Map of ObjectId -> ObjectId
	 */
	public static Map<ObjectId, ObjectId> stringToAttrIdsMap(
			String string, 
			String entryDelimiter, 
			String keyValueDelimiter,
			String classIdDelimiter
	){
		HashMap<ObjectId, ObjectId> map = new HashMap<ObjectId, ObjectId>();
		String pairs[] = string.split(entryDelimiter);
		for(String pair : pairs){
			String[] entry = pair.trim().split(keyValueDelimiter, 2);
			ObjectId key = getAttrObjectId(entry[0].trim(), classIdDelimiter);
			ObjectId value = getAttrObjectId(entry[1].trim(), classIdDelimiter);
			if(key != null && value != null) map.put(key, value);
		}
		return map;
	}

	/**
	 * Checks if given identifier belongs to object of specified type 
	 * @param id identifier to be checked
	 * @param expectedTypes expected object types
	 * @throws IllegalArgumentException if actual and expected types differs
	 */
	public static void validateId(ObjectId id, Class<?>... expectedTypes) {
		ArrayList<Class<?>> expectedTypesList = new ArrayList<Class<?>>(Arrays.asList(expectedTypes));
		if ( (id != null) && !expectedTypesList.contains(id.getType())) {
			throw new IllegalArgumentException(
				"Wrong type of identifier: " + expectedTypesList + 
				" expected but " + id.getType().getName() + " found."
			);
		}
	}
	
	/**
	 * Extracts identifiers of Data Objects and pushes them to separate array; handy for argument passing.
	 */
	public static Collection<ObjectId> getIdsFromObjects(Collection<? extends DataObject> dataObjects){
		ArrayList<ObjectId> list = new ArrayList<ObjectId>();
		for(DataObject object : dataObjects) list.add(object.getId());
		return list;
	}
	
	/**
	 * ���������� �� �������� � ���� ������
	 * ���� �������� ��� �� �� = null, �� ���������� ������ ������
	 * @param c �������� �� ������� ���������� ������� ��
	 * @return �� �������� � ���� ������ ��� ������ ������
	 */
	public static String getCardIdToString(Card c) {
		if(c == null
				|| c.getId() == null
				|| c.getId().getId() == null)
			return "";
		return String.valueOf(c.getId().getId());
	}
	
	/**
	 * ���������� �� �������� � Long-����
	 * ���� �������� ��� �� �� = null, �� ���������� null
	 * @param c �������� �� ������� ���������� ������� ��
	 * @return �� �������� � Long-���� ��� null
	 */
	public static Long getCardIdToLong(Card c) {
		if(c == null
				|| c.getId() == null
				|| c.getId().getId() == null)
			return null;
		return (Long) c.getId().getId();
	}
}
