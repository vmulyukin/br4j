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
package com.aplana.dbmi.model;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

	
/**
 * ����������� ����� ����������, ������� ����� ���� ������������ �� ������ � �������.
 * @author RAbdullin
 *
 */
public class PermissionSet extends DataObject {

	private static final long serialVersionUID = 1L;

	private final LocalizedString name = new LocalizedString();

	private final HashSet /*<CardAccess>*/ set = new HashSet();

	// @Override
	public void setId(long id) {
		super.setId(new ObjectId(PermissionSet.class, id));
	}

	/**
	 * @return ����� ���������� CardAccess ������� ������.
	 */
	public HashSet /*<CardAccess>*/ getSet() {
		return this.set;
	}

	public boolean isEmpty() {
		return (this.set == null) || this.set.isEmpty();
	}

	public int size() {
		return (this.set == null) ? -1 : this.set.size();
	}

	public Iterator iterator() {
		return (this.set == null) ?  Collections.emptySet().iterator() : this.set.iterator();
	}

	/**
	 * @return Ru-�������� ������ ���������.
	 */
	public LocalizedString getName() {
		return name;
	}

	/**
	 * @return ������ Ru-�������� ������.
	 */
	public void setName(LocalizedString value) {
		if (value != null) {
			this.name.assign( value);
		} else {
			this.name.setValueRu("");
			this.name.setValueEn("");
		}
	}

	public void setNameLacales(String nameRU, String nameEN) {
		this.name.setValueRu(nameRU);
		this.name.setValueEn(nameEN);
	}

	public void addItem(CardAccess item)
	{
		if (item != null)
			set.add(item);
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof PermissionSet))
			return false;

		final PermissionSet other = (PermissionSet) obj;

		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;

		if (!compareHashSet( this.set, other.set))
			return false;

		return true;
	}


	public String toString() {
		final StringBuffer buf = new StringBuffer();
		buf.append( MessageFormat.format(
				"{0}( id: {1}, count: {2}",
				new Object[] { 
						this.getClass().getName(),
						this.getId(),
						new Integer( size())
			} ));
		if (this.isEmpty())
			buf.append(", empty");
		else {
			int i = 0;
			for (Iterator iterator = this.iterator(); iterator.hasNext(); ) {
				++i;
				final CardAccess item = (CardAccess) iterator.next();
				buf.append( MessageFormat.format(
						"\n \t [{0}] \t {1}",
						new Object[] { 
								new Integer(i),
								item
							} 
					));
			}
		}
		buf.append( "\n)");
		return buf.toString();
	}

	public static boolean compareHashSet(HashSet set1, HashSet set2) {

		if (set1 == null || set2 == null)
			return (set1 == set2);

		if (set1.size() != set2.size())
			return false;
		
		// ������������ ���������
		final Iterator iterator1 = set1.iterator();
		for (Iterator iterator2 = set2.iterator(); iterator2.hasNext();) {
			final Object obj1 = (Object) iterator1.next();
			final Object obj2 = (Object) iterator2.next();
			if (obj1 == null || obj2 == null) {
				if (obj1 != obj2) return false;
			} else if (!obj1.equals(obj2))
				return false;
		}

		return true;
	}

}
