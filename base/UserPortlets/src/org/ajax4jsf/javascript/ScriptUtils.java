/**
 * License Agreement.
 *
 * Rich Faces - Natural Ajax for Java Server Faces (JSF)
 *
 * Copyright (C) 2007 Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package org.ajax4jsf.javascript;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;



import org.apache.commons.beanutils.PropertyUtils;

/**
 * @author shura (latest modification by $Author: alexsmirnov $)
 * @version $Revision: 1.1.2.3 $ $Date: 2007/01/24 13:22:31 $
 * 
 */
public class ScriptUtils {

	/**
	 * This is utility class, don't instantiate.
	 */
	private ScriptUtils() {

	}

	/**
	 * Convert any Java Object to JavaScript representation ( as possible ).
	 * @param obj
	 * @return
	 */
	public static String toScript(Object obj)  {
		if (null == obj) {
			return "null";
		} else if (obj instanceof ScriptString) {
			return ((ScriptString) obj).toScript();
		} else if (obj.getClass().isArray()) {
			StringBuffer ret = new StringBuffer("[");
			boolean first = true;
			for (int i = 0; i < Array.getLength(obj); i++) {
				Object element = Array.get(obj, i);
				if (!first) {
					ret.append(',');
				}
				ret.append(toScript(element));
				first = false;
			}
			return ret.append("] ").toString();
		} else if (obj instanceof Collection) {
			// Collections put as JavaScript array.
			Collection collection = (Collection) obj;
			StringBuffer ret = new StringBuffer("[");
			boolean first = true;
			for (Iterator iter = collection.iterator(); iter.hasNext();) {
				Object element = iter.next();
				if (!first) {
					ret.append(',');
				}
				ret.append(toScript(element));
				first = false;
			}
			return ret.append("] ").toString();
		} else if (obj instanceof Map) {
			// Maps put as JavaScript hash.
			Map map = (Map) obj;

			StringBuffer ret = new StringBuffer("{");
			boolean first = true;
			for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry) iter.next();
				if (!first) {
					ret.append(',');
				}
				
				addEncodedString(ret, entry.getKey());
				ret.append(":");
				ret.append(toScript(entry.getValue()));
				first = false;
			}
			return ret.append("} ").toString();
		} else if (obj instanceof Number || obj instanceof Boolean) {
			// numbers and boolean put as-is, without conversion
			return obj.toString();
		} else if (obj instanceof String) {
			// all other put as encoded strings.
			StringBuffer ret = new StringBuffer();
			addEncodedString(ret, obj);
			return ret.toString();
		}
		// All other objects threaded as Java Beans.
		try {
			StringBuffer ret = new StringBuffer("{");
			PropertyDescriptor[] propertyDescriptors = PropertyUtils
					.getPropertyDescriptors(obj);
			boolean first = true;
			for (int i = 0; i < propertyDescriptors.length; i++) {
				PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
				String key = propertyDescriptor.getName();
				if ("class".equals(key)) {
					continue;
				}
				if (!first) {
					ret.append(',');
				}
				addEncodedString(ret, key);
				ret.append(":");
				ret.append(toScript(PropertyUtils.getProperty(obj, key)));
				first = false;
			}
			return ret.append("} ").toString();
		} catch (Exception e) {
			throw new RuntimeException(
					"Error in conversion Java Object to JavaScript", e);
		}
	}

	public static void addEncodedString(StringBuffer buff, Object obj) {
		buff.append("'");
		addEncoded(buff, obj);
		buff.append("'");

	}

	public static void addEncoded(StringBuffer buff, Object obj) {
		JSEncoder encoder = new JSEncoder();
		char chars[] = obj.toString().toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (!encoder.compile(c)) {
				buff.append(encoder.encode(c));
			} else {
				buff.append(c);
			}
		}
	}
}
