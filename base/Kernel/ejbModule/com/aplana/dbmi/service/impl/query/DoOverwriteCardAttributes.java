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
package com.aplana.dbmi.service.impl.query;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter;

import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.model.AccessListItem;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.PseudoAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.SecurityAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.SmartSaveConfig;
import com.aplana.dbmi.utils.AttributeSqlBuilder;
import com.aplana.dbmi.utils.SimpleDBUtils;
import com.aplana.dbmi.utils.StrUtils;
import com.aplana.dbmi.utils.AttributeSqlBuilder.InsertAttributesSQLBuilder;

/**
 * {@link ActionQueryBase} descendant used to store defined collection of card attributes
 * into existing card. 
 * @see OverwriteCardAttributes
 * */
public class DoOverwriteCardAttributes extends ActionQueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * {@link SaveCard}
	 */
	@Override
	public Object processQuery() throws DataException 
	{
		// TODO: ����� ����� ��������� ���� �������� �� ��������� read-only ��������� ��

		final OverwriteCardAttributes action = (OverwriteCardAttributes)getAction();
		final ObjectId cardId = action.getCardId();
		final Long id = (Long) cardId.getId();

		final Collection<Attribute> attrs = action.getAttributes();
		final List<SecurityAttribute> secAttrs = new ArrayList<SecurityAttribute>();

		checkLock(cardId);

		if (attrs.size() == 0) {
			return null;
		}

		final SmartSaveConfig ssCfg = getSmartSaveConfig();
		boolean enSmartInsert = false;
		String hardSaveAttributes = null;
		try {
			enSmartInsert = (ssCfg != null) && ssCfg.isEnSmartSaveMode();
			hardSaveAttributes = ssCfg.getHardSaveAttributes();
		} catch (RemoteException ex) {
			logger.error(ex);
		}
		logger.info("smart insert attributes is " + (enSmartInsert ?"ENABLED" :"DISABLED"));

		// ������������ ������ ��������� � ���� "select A1 union select A2 ..." 
		final InsertAttributesSQLBuilder builder;
		if (hardSaveAttributes!=null && hardSaveAttributes!="")
			builder = new InsertAttributesSQLBuilder(hardSaveAttributes);
		else builder = new InsertAttributesSQLBuilder();
		// true, ���� ��������� �����-���� ������
		@SuppressWarnings("unused")
		final boolean hasNewData = builder.emmitSelectAttributes( attrs, "\t\t\t");
		builder.insertArg( 0, id, Types.NUMERIC );

		/*
		 * (!) ����� ������ ������� � builder "����������������", �.�. �����������
		 * ������ SQL-������ builder, � ������ ���� ����� �� ������� SQL, ������
		 * �������� ����� builder.sqlBuf.
		 */

		final String attrList = StrUtils.getAsString(attrs, ", ", "'");
		if (!action.isInsertOnly()) {
			// �������� ������ ���� ���� ��� ...
			/* 
			 * smart-�������� ������� ������ ����:
			 *   1) �������� smart-�����;
			 *   2) ��������� ������;
			 *   3) � ���� �������� sql-�����.
			 */
			String fullSql;
			if (enSmartInsert) {
				final StringBuffer sqlDelOld = AttributeSqlBuilder.makeSqlDeleteOldByCardId(
						builder, "avRemOldAttr", "	AND avRemOldAttr.attribute_code in ("+ attrList + ") \n");
				fullSql = sqlDelOld.toString();
			} else {
				fullSql = 
					"DELETE FROM attribute_value avOver \n" +
					"WHERE avOver.card_id=? \n" +
					"	AND avOver.attribute_code in ("+ attrList + ") \n";
			}
			final int countDel = getJdbcTemplate().update(fullSql, builder.getPreparedStatementSetter() ); 
			if (logger.isDebugEnabled())
				logger.debug("removed "+ countDel+ " attributes ["+ attrList+ "]\n\t from card "
						+ id+ " using smartSave="+ enSmartInsert);
		}

		/*
		 * ���������� ��������� ������ ... 
		 */
		{
			int inserted;
			if (enSmartInsert)
				inserted = this.smartInsertAll( builder, secAttrs);
			else
				inserted = this.insertAll( id, attrs, secAttrs);
			if (logger.isDebugEnabled())
				logger.debug( "insert "+ inserted + " attributes ["+ attrList+ "]\n\t into card "
						+ id + " using smartSave="+ enSmartInsert);
		}

		/* 
		 * ���������� ACL-�������� ... 
		 */
		if (secAttrs.size() > 0) {
			if (!action.isInsertOnly()) {
				getJdbcTemplate().update("DELETE FROM access_control_list WHERE card_id=?",
					new Object[]{id}, new int[]{Types.NUMERIC} );
			}

			final String sqlInsAC = "INSERT INTO access_control_list (card_id, person_id, role_code, value_id) VALUES (?, ?, ?, ?)";
			AbstractInterruptibleBatchPreparedStatementSetter pssS = 
				new AbstractInterruptibleBatchPreparedStatementSetter() {

				@Override
				protected boolean setValuesIfAvailable(PreparedStatement stmt, int index)
						throws SQLException 
				{
					for (SecurityAttribute attr: secAttrs ) 
					{
						Collection items = attr.getAccessList();
						if (items == null)
							continue;
						
						for (Iterator i = items.iterator(); i.hasNext(); ) {
							setParamToDefault(stmt, attr);

							AccessListItem item = (AccessListItem)i.next();
							switch (item.getType()) {
							case AccessListItem.TYPE_PERSON:
								stmt.setObject( 2, item.getPerson().getId().getId(), Types.NUMERIC ); // (2010/03, POSTGRE) OLD: no type 
								break;
							case AccessListItem.TYPE_ROLE:
								stmt.setObject(3, item.getRoleType());
								break;
							case AccessListItem.TYPE_DEPARTMENT:
								stmt.setObject(4, item.getDepartment().getId().getId(), Types.NUMERIC); // (2010/03, POSTGRE) OLD: no type
								break;
							}
							stmt.addBatch();
						}
					}
					return false;
				}

				void setParamToDefault(PreparedStatement stmt, Attribute attr) 
					throws SQLException
				{
					stmt.setObject(1, id, Types.NUMERIC ); // (2010/03, POSTGRE) OLD: no type
					stmt.setNull(2, Types.NUMERIC);
					stmt.setNull(3, Types.VARCHAR);
					stmt.setNull(4, Types.NUMERIC);
				}
			};
			getJdbcTemplate().batchUpdate(sqlInsAC, pssS);
		}

		return null;
	}

	/**
	 * �������� ��� ��������. 
	 * @param cardId id ����������� ��������.
	 * @param attrs ����������� ������ ���������.
	 * @param secAttrs ����������� ���, ���������� ���� SecurityAttribute.
	 * @return ���-�� ���������� ���������.
	 */
	private int insertAll( 
			final Long cardId,
			final Collection<Attribute> attrs, 
			final List<SecurityAttribute> secAttrs ) 
	{
		final String sqlIns = 
				"INSERT INTO attribute_value \n"+
				"(card_id, attribute_code, number_value, string_value, date_value, "+
				"value_id, another_value, long_binary_value) \n"+
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?) \n"
				;
		final AbstractInterruptibleBatchPreparedStatementSetter pssI = 
				new AbstractInterruptibleBatchPreparedStatementSetter() 
		{

			protected boolean setValuesIfAvailable(PreparedStatement stmt, int index)
				throws SQLException 
			{
				for (Iterator<Attribute> iter = attrs.iterator(); iter.hasNext();) {
					final Attribute attr = iter.next();

					setParamToDefault(stmt, attr);
					if (attr instanceof PseudoAttribute)
						continue;
					if (attr instanceof DateAttribute) {
						DateAttribute da = (DateAttribute) attr; 
						Date dt = da.getValue();

						if (dt != null && !da.isShowTime()) {
							Calendar cal = Calendar.getInstance();
							cal.setTimeInMillis(dt.getTime());
							cal.set(Calendar.HOUR_OF_DAY, 0);
							cal.set(Calendar.MINUTE, 0);
							cal.set(Calendar.SECOND, 0);
							cal.set(Calendar.MILLISECOND, 0);
							dt = cal.getTime();
						}

						dt = resetTimeZone(dt);
						/* (2011/02/18, YNikitin aka GoRik) ���������� ��������� �������� ������������ ������� ����, � �� ������� ���� ����-������� 
						 * ���� ����� �� �������, � ����-������ ����� �������� �� �������� �� ���� ��������� ����, � �������� ������������ ����� 
						 * ����� ������� � ������ ���������� � �������������, ����� ��������� � "���� �����" ������
						 */
						Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
						stmt.setTimestamp(5, SimpleDBUtils.sqlTimestamp(dt), calendar);
						stmt.addBatch();
						continue;
					}
					
					if (attr instanceof HtmlAttribute) {
						try {
							final String val = ((HtmlAttribute) attr).getValue();
							if (val == null)
								stmt.setNull(8, Types.BINARY);
							else
								stmt.setBytes(8, val.getBytes("UTF-8"));
						} catch (UnsupportedEncodingException e) {
							logger.error(e);
							continue;
						}
						stmt.addBatch();
						continue;
					} 
					
					if (attr instanceof StringAttribute) {
						stmt.setString(4, ((StringAttribute) attr).getValue());
						stmt.addBatch();
						continue;
					} 
					
					if (attr instanceof IntegerAttribute) {
						stmt.setInt(3, ((IntegerAttribute) attr).getValue());
						stmt.addBatch();
						continue;
					}
					
					if (attr instanceof CardLinkAttribute) 
					{
						// >>> (2010/02, RuSA)
						/*
						final Collection cards = ((CardLinkAttribute)attr).getValues();
						if (cards == null)
						continue;
						 */
						final Collection<ObjectId> ids = ((CardLinkAttribute)attr).getIdsLinked();
						if (ids == null) continue;
						for (Iterator<ObjectId> i = ids.iterator(); i.hasNext(); ) {
							setParamToDefault(stmt, attr);
							// final Card card = (Card)i.next();
							// stmt.setObject(3, card.getId().getId());
							// final ObjectId id = c.getId();
							final ObjectId id = i.next();
							stmt.setObject(3, id.getId(), Types.NUMERIC ); // (2010/03, POSTGRE) OLD: no type

							if (attr instanceof TypedCardLinkAttribute){
								Long typeId = (Long)((TypedCardLinkAttribute)attr).getTypes().get(id.getId());
								if (typeId != null)
									stmt.setLong(6, typeId.longValue());
							}
							if (attr instanceof DatedTypedCardLinkAttribute){
								Date dt = ((DatedTypedCardLinkAttribute)attr).getDates().get(id.getId());
								dt = resetTimeZone(dt);
								Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
								stmt.setTimestamp(5, SimpleDBUtils.sqlTimestamp(dt), calendar);
							}
							stmt.addBatch();
						}
						continue;
						// <<< (2010/02, RuSA)
					}
					
					if (attr instanceof ListAttribute) {
						ReferenceValue ref = ((ListAttribute)attr).getValue();
						if (ref == null) 
							continue;

			            stmt.setObject(6, ref.getId().getId(), Types.NUMERIC ); // (2010/03, POSTGRE) OLD: no type 
			            if (ReferenceValue.ID_ANOTHER.equals(ref.getId()))
			                stmt.setString(7, ref.getValueRu());
			        	stmt.addBatch();
			        	continue;
					}
					
					if (attr instanceof TreeAttribute) {
						Collection refs = ((TreeAttribute)attr).getValues();
						if (refs == null) 
							continue;
						
						for (Iterator i = refs.iterator(); i.hasNext(); ) {
							setParamToDefault(stmt, attr);
				        	ReferenceValue ref = (ReferenceValue)i.next();
			        		stmt.setObject(6, ref.getId().getId(), Types.NUMERIC ); // (2010/03, POSTGRE) OLD: no type
				            if (ReferenceValue.ID_ANOTHER.equals(ref.getId()))
				                stmt.setString(7, ref.getValueRu());
				        	stmt.addBatch();
						}
			        	continue;
					}
					
					if (attr instanceof PersonAttribute) {
						Collection persons = ((PersonAttribute)attr).getValues();
						if (persons == null) 
							continue;
						
						for (Iterator i = persons.iterator(); i.hasNext(); ) {
							setParamToDefault(stmt, attr);
			        		Person person = (Person)i.next();
			        		stmt.setObject(3, person.getId().getId(), Types.NUMERIC ); // (2010/03, POSTGRE) OLD: no type
				        	stmt.addBatch();
						}
			        	continue;
					}
					if (attr instanceof SecurityAttribute) {
						secAttrs.add( (SecurityAttribute) attr);
					}

					if (attr instanceof BackLinkAttribute) {
						// skip this - backlink is not writeable ...
						continue;
					}

					/*
					if (attr instanceof MaterialAttribute) {
						continue;
					}

					throw new SQLException("Unsupported attribute type for "+ attr.getId());
					 */
					logger.warn( "type is not directly saveable: attribute skipped " + attr.getId());
				}
				return false;
			}
			
			void setParamToDefault(PreparedStatement stmt, Attribute attr) throws SQLException{
				stmt.setObject(1, cardId, Types.NUMERIC ); // (2010/03, POSTGRE) OLD: no type
				stmt.setObject(2, attr.getId().getId());
				stmt.setNull(3, Types.DECIMAL);

				stmt.setNull(4, Types.VARCHAR);
				stmt.setNull(5, Types.TIMESTAMP);
				stmt.setNull(6, Types.DECIMAL);

				stmt.setNull(7, Types.VARCHAR);
				stmt.setNull(8, Types.BINARY);
			}
		};
		final int[] updated = getJdbcTemplate().batchUpdate(sqlIns, pssI);
		int result = 0;
		for (int i : updated) {
			result += i;
		}
		return result;
	}
	
	/**
	 * ������� ����-����, ����� � ����� ���� ��������� ���� � ��
	 */
	private Date resetTimeZone(Date dt) {
		if (dt != null) {
			//�������� ��������, ���� ����� �� ����������
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(dt.getTime());
			calendar.set(Calendar.ZONE_OFFSET, 0);
			calendar.set(Calendar.DST_OFFSET, 0);
			return dt = calendar.getTime();
		}
		return null;
	}

	/**
	 * �������� ������ ����� ��������. 
	 * @param builder �������������� ������ ����������� �������� ���������.
	 * @param secAttrs ����������� ���, ���������� ���� SecurityAttribute.
	 * @return ���-�� ���������� ���������.
	 * @throws Exception 
	 */
	private int smartInsertAll(InsertAttributesSQLBuilder builder,
			List<SecurityAttribute> secAttrs) throws DataException 
	{
		if ( !builder.hasAttributes())
			return 0;
		final String sqlNewRecs = 
			"	SELECT c.card_id, attr.attribute_code, attr.number_value, attr.string_value, attr.date_value, " +
					"attr.value_id, attr.another_value, attr.long_binary_value \n" +
			"	FROM \n" +
			"		(SELECT cast(? as NUMERIC)) as c(card_id) \n" +
			"		, ( \n" +
					builder.getSqlBuf() +
			"		) as attr (attribute_code, number_value, string_value, date_value, value_id, another_value, long_binary_value, attrOrder) \n" +
			"	WHERE \n" +
			"		NOT EXISTS ( \n" +
			"			SELECT 1 \n" +
			"			FROM attribute_value avE \n" +
			"			WHERE \n" +
			"				avE.card_id = c.card_id \n" +
			"				AND \n" +
								AttributeSqlBuilder.emmitAttrSqlCompareEqConditions( "avE", "attr", "\t\t\t\t")+
			"		) -- /AND NOT EXISTS \n" +
			"	ORDER BY attr.attrOrder -- keep insert order /n"
			;
		final String sqlIns = 
			"INSERT INTO attribute_value \n"+
			"(card_id, attribute_code, number_value, string_value, date_value, "+
				"value_id, another_value, long_binary_value) \n"+
				sqlNewRecs;
		try {
			/* �������������� ������ ������� ������� � ��� ���������� ... */
			if (logger.isTraceEnabled()) {
				logger.trace( "insert SQL is "
						+ com.aplana.dbmi.utils.SimpleDBUtils.getSqlQueryInfo( sqlIns, builder.args(), builder.types()));
			}

			/* �������������� ��������� ������ ... */
			if (logger.isTraceEnabled()) {
				final StringBuffer dstBuf = new StringBuffer("\n preview inserting records for card "+ builder.args(0)+":>>>");
				getJdbcTemplate().query(sqlNewRecs, builder.getPreparedStatementSetter(), 
						new ResultSetExtractor() {

							public Object extractData(ResultSet rs)
									throws SQLException, DataAccessException 
							{
								SimpleDBUtils.makeInfoColumns(rs.getMetaData(), dstBuf);
								SimpleDBUtils.makeInfoDataSet(rs, dstBuf, 50);
								return rs;
							}});
				dstBuf.append("<<<\n");
				logger.trace(dstBuf);
			}

			final int countIns = getJdbcTemplate().update(sqlIns, builder.getPreparedStatementSetter() );
			return countIns;
		} catch (Exception ex) {
			logger.error("problem inserting data by sql: >>>\n" + sqlIns 
					+ "\n<<< \n\t builder data is: length="
					+ builder.getSqlBuf().length()+ ", built sql is: >>>"
					+ builder.getSqlBuf()
					+ "\n\t<<<, error is:", ex );
			throw new DataException(ex);
		}
	}

	private SmartSaveConfig smartSaveCfg = null;
	public SmartSaveConfig getSmartSaveConfig()
	{
		if (smartSaveCfg == null)
			smartSaveCfg = (SmartSaveConfig) getBeanFactory().getBean( SmartSaveConfig.BEAN_SSMGRCFG, SmartSaveConfig.class);
		return smartSaveCfg;
	}

}