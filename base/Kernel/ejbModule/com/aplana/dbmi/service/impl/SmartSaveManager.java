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
 * ���������� ��������� (������) �������� � ����� "������� ������ ���������"
 * �.�. �������� ���������� (� ��� ����� ������������), ������ �����.
 * ��� �����, ����� ��������� ��������� ����������� ������ �������� ��������� ��������.
 */
package com.aplana.dbmi.service.impl;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.SecurityAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.query.WriteQuery;


/**
 * ���������� ������� smart-save ��� ��������� �� ����� ���������� ��������.
 *   ��� ���������� ������ (smartSaveMode = false) (������� /�� smart/ �����), 
 * ��� ������� �������� �������� �������� ��������� ���������, � ����� ��������� 
 * ��� �����.
 *   ��� ��������� ����� ������, ����������� �������� ������ ���������� ���������,
 * �.�. ������������ � ����������� ��������, � ����� ����������� ������������� � 
 * ����. ��� ��������� ������� ���-�� ����������� �������� �� 10-20% �� ��������.
 * 
 * ��. {@link com.aplana.dbmi.service.impl.query.SaveCard} �
 * {@link com.aplana.dbmi.service.impl.query.DoOverwriteCardAttributes}
 * 
 * @author rabdullin
 */
public class SmartSaveManager extends ActionQueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;

	// final static String BEAN_SSMGR = "smartSaveManager";

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.service.impl.QueryBase#processQuery()
	 */
	@Override
	public Object processQuery() throws DataException {
		return null;
	}

	/**
	 * �������� ��������� �������� � ���������� �������
	 * (��������, ��� ������������ raw-���������)
	 * @param cardId
	 */
	public RawCard fastLoadCard(ObjectId cardId) 
	{
		if (cardId == null || cardId.getId() == null)
			return null;

		final Long id = (Long) cardId.getId();

		final long msecStart = System.currentTimeMillis();

		final RawCard card = new RawCard(id);

		getJdbcTemplate().query( 
				"SELECT fav.attr_value_id, \n" +
				"		fav.attribute_code, fav.number_value, fav.string_value \n" +
				"		fav.date_value, fav.value_id, fav.another_value,  fav.long_binary_value \n" +
				"FROM attribute_value as fav WHERE fav.card_id = ?",
				new Object[] { id }, new int[] {Types.NUMERIC},

				new RowMapper() {

					public Object mapRow( ResultSet rs, int rowNum)
							throws SQLException 
					{

						final long attr_id = rs.getLong(1);

						final String attribute_code = rs.getString(2);
						final Long number_value = (rs.getObject(3) == null) ? null : rs.getLong(3);
						final String string_value = rs.getString(4);

						final Date date_value = rs.getDate(5);
						final Long value_id = (rs.getObject(6) == null) ? null : rs.getLong(6);
						final String another_value = rs.getString(7);

						final byte[] long_binary_value = (rs.getObject(8) == null) ? null : rs.getBytes(8);

						final RawCard.RealRawAttributeValue result = 
							card.new RealRawAttributeValue( attr_id, 
								attribute_code,
								number_value, string_value, date_value,
								value_id, another_value,
								long_binary_value
							);
						card.attributes.add(result);
						return result;
					}
				}
		);

		final long msecDuration = System.currentTimeMillis() - msecStart;
		logger.debug( MessageFormat.format( " fast card {0} loaded in {1} msec", id, msecDuration));

		return card;
	}

	/**
	 * ��������� ������� �������� � ���������� ������ 
	 * (��������, ��� ������������ raw-���������)
	 * @param card
	 * @throws DataException 
	 */
	public RawCard fastMakeCard(Card card) throws DataException
	{
		if (card == null)
			return null;

		final long msecStart = System.currentTimeMillis();
		final Long id = (card.getId() != null) ? (Long) card.getId().getId() : null;
		final RawCard result= new RawCard( id);
		for( TemplateBlock block : card.<TemplateBlock>getAttributes()) 
		{
			for( final Attribute attr :  (Collection<Attribute>) block.getAttributes() ) 
			{
				final RawCard.RawAttributeValue rawAttr = result.new RawAttributeValue();
				rawAttr.assignAttribute(attr);
				result.attributes.add(rawAttr);
			}
		}
		final long msecDuration = System.currentTimeMillis() - msecStart;
		logger.debug( MessageFormat.format( " fast card {0} created in {1} msec", id, msecDuration));

		return result;
	}

	public class RawCard {

		// NULL = new card
		Long cardId; // NUMERIC(x,0)
		final List<RawAttributeValue> attributes = new ArrayList<RawAttributeValue>();

		public RawCard(Long cardId) {
			super();
			this.cardId = cardId;
		}

		/**
		 * ������������ ����� ��� ������ ������� attribute_value
		 * (����� ������������ ��� ������������ ������, ��� �������� �� �� 
		 * ������������ ��������� ��{@LINK RealRawAttributeValue})
		 * @author RAbdullin
		 *
		 */
		class RawAttributeValue {
			// template_id INTEGER  == ignored here

			String attributeCode; // VARCHAR(20)

			Long number_value; // NUMERIC(x, 0)
			String string_value ; // VARCHAR(32000)
			Date date_value; // TIMESTAMP 

			Long value_id; // NUMERIC(x,0)
			String another_value; // VARCHAR(256)
			byte[] long_binary_value; // BYTEA

			public RawAttributeValue() {
				super();
			}

			/**
			 * @param attr
			 * @return ���-�� ���-�� ������� ��������������� ���������
			 */
			public int assignAttribute(Attribute attr) throws DataException 
			{
				clear();

				if (attr == null)
					return 0;

				attributeCode = (String) attr.getId().getId();

				if (attributeCode == null) {
					logger.warn( "attribute "+ attr + " has NULL attributeCode");
					return 0;
				}

				// BackLinkAttribute ...
				if (attr instanceof BackLinkAttribute) {
					// this.skippedAttrs.add(attr);
					return 0;
				}

				if (attr instanceof SecurityAttribute) {
					// this.securityAttrs.add( (SecurityAttribute) attr);
					return 0;
				}

				boolean result = false;
				int processed = 1; // ���-�� ������������ ��������� ...
				try {
					if (attr.isEmpty()) {
						logger.debug(MessageFormat.format("attribute ''{0}'' is empty -> no emition performed", attributeCode));
					}

					// IntegerAttribute ...
					else if (attr instanceof IntegerAttribute) {
						final IntegerAttribute intAttr = (IntegerAttribute) attr;
						this.setInteger(intAttr.getValue());
						result = true;
					}

					// DateAttribute ...
					else if (attr instanceof DateAttribute) {
						final DateAttribute dateAttr = (DateAttribute) attr;
						result = setDate( dateAttr.getValue());
					}

					// HtmlAttribute (this must be before StringAttribute cause of derivation)...
					else if (attr instanceof HtmlAttribute) {
						final HtmlAttribute htmlAttr = (HtmlAttribute) attr;
						result = setBinary( htmlAttr.getValue());
					}

					// StringAttribute 
					// and TextAttribute 
					// and CardHistoryAttribute ...
					else if (attr instanceof StringAttribute) {
						final StringAttribute strAttr = (StringAttribute) attr;
						result = setString( strAttr.getValue());
					}

					// ListAttribute ...
					else if (attr instanceof ListAttribute) {
						final ReferenceValue ref = ((ListAttribute) attr).getValue();
						result = setReference(ref);
					}

					// TreeAttribute ...
					else if (attr instanceof TreeAttribute) {
						final Collection<ReferenceValue> refs = ((TreeAttribute) attr).getValues();
						if (refs == null)
							return 0;
						processed = 0;
						for (ReferenceValue ref : refs) {
							if (this.setReference(ref)) {
								result = true;
								++processed;
							}
						}
					}

					// CardLinkAttribute
					// and TypedCardLinkAttribute ...
					else if (attr instanceof CardLinkAttribute) {
						final Collection<ObjectId> ids = ((CardLinkAttribute)attr).getIdsLinked();
						if (ids == null)
							return 0;
						processed = 0;
						for (final ObjectId id : ids ) 
						{
							Long typeId = null;
							if (attr instanceof TypedCardLinkAttribute) {
								typeId = (Long)((TypedCardLinkAttribute)attr).getTypes().get(id.getId());
							}

							if (this.setLongAndValId( (Long) id.getId(), typeId )) {
								result = true;
								++processed;
							}
						}
					}

					// PersonAttribute ...
					else if (attr instanceof PersonAttribute) 
					{
						final Collection<Person> persons = ((PersonAttribute) attr).getValues();
						if (persons == null) 
							return 0;

						processed = 0;
						for (Person person : persons) 
						{
							final Long id = (Long) person.getId().getId();
							if (this.setLong(id)) {
								result = true;
								++processed;
							}
						}
					}
					else {
						// Attribute
						// MaterialAttribute
						// DatePeriodAttribute
						// ReferenceAttribute
						// StateSearchAttribute
						logger.warn( "typeof attribute is not directly saveable and was skipped: " + attr.getId() );
					}

				} finally {
					// if (result) { ... } else { this.skippedAttrs.add(attr); }
				}
				return processed;
			}

			private void setInteger(int value) {
				this.number_value = new Long(value);
			}

			private boolean setLong(Long value) {
				// if (value == null) return false;
				if (value == null) return false;
				this.number_value = value;
				return true;
			}

			private boolean setValId(Long valId) {
				if (valId == null) 
					return false;
				this.value_id = valId;
				return true;
			}

			private boolean setLongAndValId(Long id, Long typeId) {
				if (id == null) 
					return false;
				setLong( id);
				setValId( typeId);
				return true;
			}

			private boolean setString(String value) 
			{
				// (!) ����� NULL-�������� �����������
				if (value == null || value.length() == 0) return false;
				this.string_value = value;
				return true;
			}

			private boolean setDate(Date value) {
				if (value == null)
					return false;
				this.date_value = value;
				return true;
			}

			private boolean setBinary(String value) throws DataException 
			{
				if (value == null || value.length() == 0)
					return false;
				try {
					this.long_binary_value = (value == null) ? null : value.getBytes("UTF-8");
				} catch (UnsupportedEncodingException e) {
					logger.error( "rawAttr '"+ this.attributeCode +"' : fail to get as UTF8/bytes string '"+ value+ "'", e);
					throw new DataException("convertion problem for "+ attributeCode, e);
				}
				return true;
			}

			/**
			 * @param ref
			 */
			private boolean setReference(ReferenceValue ref) {
				if (ref == null) 
					return false;
				if (ref.getId() != null)
					this.value_id = (Long) ref.getId().getId();
				this.another_value = 
						(ReferenceValue.ID_ANOTHER.equals(ref.getId()))
								? ref.getValueRu() : null;
				return true;
			}

			private void clear() {
				this.attributeCode = null;

				this.number_value = null;
				this.string_value = null;
				this.date_value = null; 

				this.value_id = null;
				this.another_value = null;
				this.long_binary_value = null;
			}

			public RawAttributeValue(String attribute_code, Long number_value,
					String string_value, Date date_value, Long value_id,
					String another_value, byte[] long_binary_value) 
			{
				super();
				this.attributeCode = attribute_code;
				this.number_value = number_value;
				this.string_value = string_value;
				this.date_value = date_value;
				this.value_id = value_id;
				this.another_value = another_value;
				this.long_binary_value = long_binary_value;
			}

			boolean isEqual( RawAttributeValue v) 
			{
				if (this == v)
					return true;
				if (v == null) 
					return false;

				if (this.attributeCode == null || !this.attributeCode.equals(this.attributeCode))
					// ��� ����� �������� ����������� ������ ������ ��������� ...
					return false;

				if (!cmpVals( this.number_value, v.number_value))
					return false;
				if (!cmpVals( this.string_value, v.string_value))
					return false;
				if (!cmpVals( this.date_value, v.date_value))
					return false;
				if (!cmpVals( this.value_id, v.value_id))
					return false;
				if (!cmpVals( this.another_value, v.another_value))
					return false;
				if (!Arrays.equals( this.long_binary_value, v.long_binary_value))
					return false;
				return true;
			}

			/**
			 * ��������� ���� ��������, ��� ��� null-�������� ��������� ��������������.
			 * @param <T>
			 * @param v1
			 * @param v2
			 * @return
			 */
			final <T extends Object> boolean cmpVals( T v1, T v2){
				return (v1 == null) ? v2 == null : v1.equals(v2);
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime* result
						+ ((this.another_value == null) ? 0 : this.another_value.hashCode());
				result = prime* result
						+ ((this.attributeCode == null) ? 0 : this.attributeCode.hashCode());
				result = prime* result
						+ ((this.date_value == null) ? 0 : this.date_value.hashCode());
				result = prime* result
						+ Arrays.hashCode(this.long_binary_value);
				result = prime* result
						+ ((this.number_value == null) ? 0 : this.number_value.hashCode());
				result = prime* result
						+ ((this.string_value == null) ? 0 : this.string_value.hashCode());
				result = prime* result
						+ ((this.value_id == null) ? 0 : this.value_id.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				// if (getClass() != obj.getClass()) return false;
				if (!this.getClass().isAssignableFrom(obj.getClass()))
					return false;
				return this.isEqual( (RawAttributeValue) obj);
			}
		}

		class RealRawAttributeValue extends RawAttributeValue {

			final long attr_value_id; // NUMERIC(x,0)

			/**
			 * @param attributeCode
			 * @param number_value
			 * @param string_value
			 * @param date_value
			 * @param value_id
			 * @param another_value
			 * @param long_binary_value
			 */
			public RealRawAttributeValue( long attr_value_id, 
					String attributeCode,
					Long number_value, String string_value, Date date_value,
					Long value_id, String another_value,
					byte[] long_binary_value) 
			{
				super( attributeCode, number_value, string_value, date_value, value_id,
						another_value, long_binary_value);
				this.attr_value_id = attr_value_id;
			}
		}

	}

}
