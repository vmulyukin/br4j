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
package com.aplana.dbmi.jbr.util;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;

import java.text.MessageFormat;

/**
 * �������� �������� ������ ��������.
 * @author RAbdullin
 */
public class AttributeLocator {

	private String originalRef;
	private ObjectId attrId; // ��� �������
	private ObjectId bkSideAttrId; // ��� back-link'�� - ������� � ��������� ��������
	private boolean flBackLink;

	/**
	 * @param reference ������ ����: "{���:} id {@id2}", ��� {x} = �������������� ����� x. 
	 * @throws DataException 
	 */
	public AttributeLocator(String reference) throws DataException 
	{
		setOriginalRef( reference);
	}

	/**
	 * @param attrId: �������.
	 * @param bkSideAttrId: ������� ������ ��������� ��������, �� ������� ��������� ������ attrId, 
	 * (!) ��������� �������� �������� ������ ��� attrId = back-link �������.
	 * @throws DataException 
	 */
	public AttributeLocator(ObjectId attrId, ObjectId bkSideAttrId) 
		throws DataException 
	{
		super();

		this.flBackLink = (attrId != null) 
			&& BackLinkAttribute.class.isAssignableFrom(attrId.getType());

		if (bkSideAttrId != null && !this.flBackLink) 
			throw new DataException("general.unique",
							new Object[] { "Attribute naming:: linked attribute can be used ONLY for backlink attribute but not for "+ attrId}
						);

		this.attrId = attrId;
		this.bkSideAttrId = (flBackLink) ? bkSideAttrId : null;
		this.originalRef = makeDefaultRef();
	}

	/**
	 * @param attrId: �������.
	 * @throws DataException 
	 */
	public AttributeLocator(ObjectId attrId) throws DataException {
		this( attrId, null);
	}


	/**
	 * @return id ��������� ��������.
	 * ���������� - ������ ����������� (@SEE: originalRef)
	 */
	public ObjectId getAttrId() {
		return this.attrId;
	}

	private String makeDefaultRef() {
		if (attrId == null)
			return null;

		final StringBuilder buf = new StringBuilder();
		buf.append(AttrUtils.getAttrTypeString(attrId.getType())); // ��� ��������
		buf.append(':');
		buf.append(attrId.getId()); // ��� ��������
		if (isBackLink() && this.bkSideAttrId != null)
			buf.append('@').append(bkSideAttrId.getId()); // @ + ��� link-��������
		return buf.toString();
	}

	/**
	 * @return ���� �������� ������� ��� backlink, �� ���-�� id �������� 
	 * ������ ��������� ��������, ����� null. 
	 * ���������� - ������ ����������� (@SEE: originalRef)
	 */
	public ObjectId getLinkId() {
		return (flBackLink) ? this.bkSideAttrId : null;
	}

	/**
	 * @return �������� ������, ����������� ������ �� �������.
	 */
	public String getOriginalRef() {
		return this.originalRef;
	}

	 /**
	  * @return true, ���� (@SEE:attrId) �������� ��������� ���� backLink.
	 * ���������� - ������ ����������� (@SEE: originalRef).
	  */
	public boolean isBackLink()
	{
		return this.flBackLink;
	}

	public void clear()
	{
		this.attrId = null;
		this.bkSideAttrId = null;
		this.originalRef = null;
		this.flBackLink = false;
	}

	/**
	 * ������ ��������� ������ - ������������� ����������� ��������� ���
	 * attrId/bkSideAttrid/flBackLink.
	 * @param s ������ ����: "{���:} id {@id2}", ��� {x} = �������������� ����� x.
	 * ��������, "string: jbr.organization.shortName"
	 * 			 "back: jbr.sender@fullname"
	 * ���� ��� ������, �� ������ ���������������� �������� � ����� ������ � 
	 * �����-���� �����, ���� ��� ������, �� ����������� �� "string".
	 * @throws DataException 
	 */
	public void setOriginalRef(String s) throws DataException {

		if ( (s == null && this.originalRef == null)
			|| (s != null && s.equals(this.originalRef)))
			// ��� ���������...
			return;

		clear();
		this.originalRef = s;
		if (s == null) return;

		String typeName = null; // default will be string
		String idTag;
		String subLink = null;	// ������ ��� backlink

		// ��������� �������� ��� ���� ��������...
		final int posDelim = s.indexOf(':');
		if (posDelim >= 0)
		{	// ��� ���� ������ � ������...
			typeName = s.substring(0, posDelim).trim();	// �� �����������
			idTag = s.substring(posDelim + 1).trim();	// �� ����� ������
		} else
			idTag= s.trim();

		// ��������� �������...
		final int posDog = idTag.indexOf('@');
		if (posDog >= 0)
		{	// ��������� ������� ������ ...
			subLink = idTag.substring(posDog + 1).trim(); // �� ����� ������
			idTag = idTag.substring(0, posDog).trim();  // �� �����������
		}  // else subLink = null;

		// ������� ������� ���������������� � ������� object ids ��������...
		attrId = null;
		if (typeName != null)
			attrId = ObjectIdUtils.getObjectId( AttrUtils.getAttrClass(typeName),
						idTag, false);

		if (attrId== null)
			attrId = IdUtils.tryFindPredefinedObjectId(idTag); 

		// ���� ����������������� �������� ��� - id ����� � ���� �������� �����... 
		if (attrId == null)
			attrId = new ObjectId(Attribute.class, s);

		this.flBackLink = BackLinkAttribute.class.isAssignableFrom(attrId.getType())
				||	CardLinkAttribute.class.isAssignableFrom(attrId.getType());

		// ������...
		this.bkSideAttrId = IdUtils.tryFindPredefinedObjectId(subLink);

		if (bkSideAttrId != null && !this.flBackLink) 
			throw new DataException("general.unique",
						new Object[] { "Attribute naming:: linked attribute can be used ONLY for cardlink attribute but not for "+ attrId}
					);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.attrId == null) ? 0 : this.attrId.hashCode());
		result = prime * result
				+ ((this.bkSideAttrId == null) ? 0 : this.bkSideAttrId.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final AttributeLocator other = (AttributeLocator) obj;

		// ���� �������� ������ ��������� - ����� ��������� �� ������...
		if ( 	(this.originalRef == null && other.originalRef == null)
			|| 	(this.originalRef != null && this.originalRef.equals(other.originalRef))
			)
			return true;

		if (this.attrId == null) {
			if (other.attrId != null)
				return false;
		} else if (!this.attrId.equals(other.attrId))
			return false;
		if (this.bkSideAttrId == null) {
			if (other.bkSideAttrId != null)
				return false;
		} else if (!this.bkSideAttrId.equals(other.bkSideAttrId))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// return super.toString();
		return	(bkSideAttrId == null)
				? MessageFormat.format("[{0}]", attrId)
				: MessageFormat.format("[{0}(-BkLink->)@{1}]", attrId, bkSideAttrId)
			;
	}

}