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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter;
import org.springframework.util.CollectionUtils;

import com.aplana.dbmi.action.AddToArchive;
import com.aplana.dbmi.archive.AttributeValueArchiveValue;
import com.aplana.dbmi.archive.WriteArchiveSqlQueryExecutor;
import com.aplana.dbmi.archive.CardArchiveValue;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.utils.SimpleDBUtils;

/**
 * ���������� ������ AddToArchive (������ ������ � �������� ������� card_archive � attribute_value_archive)
 * @author ppolushkin
 * @since 19.12.2014
 */
public class DoAddToArchive extends ActionQueryBase {

	private static final long serialVersionUID = 1L;
	
	AddToArchive action;

	@Override
	public Object processQuery() throws DataException {
		
		action = getAction();
		
		if(CollectionUtils.isEmpty(action.getCopyDBValues())
				&& CollectionUtils.isEmpty(action.getModelValues())) {
			
			logger.warn("No data for transfer to the archive");
			return null;
		}
		
		if(!CollectionUtils.isEmpty(action.getCopyDBValues())) {
			
			WriteArchiveSqlQueryExecutor exec = new WriteFromDbArchiveSqlQueryExecutor();
			exec.execSqlQuery();
			return null;
		}
		
		if(!CollectionUtils.isEmpty(action.getModelValues())) {
			
			WriteArchiveSqlQueryExecutor exec = new WriteFromModelArchiveSqlQueryExecutor();
			exec.execSqlQuery();
			return null;
		}
		
		return null;
	}
	
	public class WriteFromModelArchiveSqlQueryExecutor implements WriteArchiveSqlQueryExecutor {

		@Override
		public void execSqlQuery() {
			final List<CardArchiveValue> dataToArchive = action.getModelValues();
			
			StringBuilder insertCards = new StringBuilder();
			StringBuilder deleteCards = new StringBuilder();
			StringBuilder deleteAttributes = new StringBuilder();
			
			Iterator<CardArchiveValue> it = dataToArchive.iterator();
			
			boolean isFirstCard = true;
			boolean isFirstAttrs = true;
			
			while(it.hasNext()) {
				final CardArchiveValue currCard = it.next();
				
				if(isFirstCard) {
					isFirstCard = false;
				} else {
					deleteCards.append("union \n");
					insertCards.append("union \n");
				}
				
				deleteCards.append("select card_id from card_archive where card_id = ").append(currCard.getCardId()).append(" \n");
				deleteCards.append("	and not exists (select 1 from attribute_value_archive where card_id = ").append(currCard.getCardId()).append(" ) \n");
				
				insertCards.append("select ").append(currCard.getCardId()).append(", ").append(currCard.getTemplateId()).append(", \n");
				insertCards.append("       ").append(currCard.getParentCardId() != null ? currCard.getParentCardId() : "null").append(", ");
				insertCards.append(currCard.getStatusId()).append(" \n");
				insertCards.append("where \n");
				insertCards.append("	not exists (select 1 from card_archive where card_id = ").append(currCard.getCardId()).append(" ) \n");
				
				final Set<AttributeValueArchiveValue> currAttrs = currCard.getAttrValues();
				
				// ���� ����� ��������� ����
				if(CollectionUtils.isEmpty(currAttrs)) {
					continue;
				}
				
				if(isFirstAttrs) {
					isFirstAttrs = false;
				} else {
					deleteAttributes.append("union \n");
				}
				
				deleteAttributes.append("select card_id from attribute_value_archive where card_id = ").append(currCard.getCardId()).append(" \n ");
				
			}
			
			final String sqlIns = 
					"INSERT INTO attribute_value_archive \n" +
					"(card_id, attribute_code, number_value, string_value, date_value, " +
					"value_id, another_value, long_binary_value, template_id) \n" +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) \n";
			
			final AbstractInterruptibleBatchPreparedStatementSetter bpss = 
					new AbstractInterruptibleBatchPreparedStatementSetter() 
			{

				@Override
				protected boolean setValuesIfAvailable(PreparedStatement stmt, int index)
						throws SQLException {
					
					CardArchiveValue currCard;
					for (Iterator<CardArchiveValue> it = dataToArchive.iterator(); it.hasNext();) {
						currCard = it.next();
						
						final Set<AttributeValueArchiveValue> currAttrs = currCard.getAttrValues();
						
						// ���� ����� ��������� ����
						if(CollectionUtils.isEmpty(currAttrs)) {
							continue;
						}
						
						AttributeValueArchiveValue attrValue;
						for (Iterator<AttributeValueArchiveValue> itAttr = currAttrs.iterator(); itAttr.hasNext();) {
							attrValue = itAttr.next();
						
							setParamToDefault(stmt, currCard);
						
							if(attrValue.getAttributeCode() != null) {
								stmt.setString(2, (String) attrValue.getAttributeCode().getId());
							}
							if(attrValue.getNumberValue()!= null) {
								stmt.setLong(3, attrValue.getNumberValue());
							}
							if(attrValue.getStringValue() != null) {
								stmt.setString(4, (String) attrValue.getStringValue());
							}
							if(attrValue.getDateValue() != null) {
								Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
								stmt.setTimestamp(5, SimpleDBUtils.sqlTimestamp(attrValue.getDateValue()), calendar);
							}
							if(attrValue.getValueId()!= null) {
								stmt.setLong(6, attrValue.getValueId());
							}
							if(attrValue.getAnotherValue() != null) {
								stmt.setString(7, (String) attrValue.getAnotherValue());
							}
							if(attrValue.getLongBinaryValue() != null) {
								try {
									stmt.setBytes(8, attrValue.getLongBinaryValue().getBytes("UTF-8"));
								} catch (UnsupportedEncodingException e) {
									logger.error("Cannot encode string to binary", e);
								}
							}
							stmt.addBatch();
						}
						
					}
					
					return false;
				}
				
				void setParamToDefault(PreparedStatement stmt, CardArchiveValue cardValue) throws SQLException {
					stmt.setLong(1, cardValue.getCardId());
					stmt.setNull(2, Types.VARCHAR);
					stmt.setNull(3, Types.NUMERIC);

					stmt.setNull(4, Types.VARCHAR);
					stmt.setNull(5, Types.TIMESTAMP);
					stmt.setNull(6, Types.NUMERIC);

					stmt.setNull(7, Types.VARCHAR);
					stmt.setNull(8, Types.BINARY);
					stmt.setLong(9, cardValue.getTemplateId());
				}
				
			};
			
			if(deleteAttributes.length() > 0) {
				// ���� ������� �������� ��� ��������, ����������� ������� select � ���� ������ delete
				deleteAttributes.insert(0, "delete from attribute_value_archive where card_id in ( \n");
				deleteAttributes.append(" ) \n");
				long deletedAttrs = getJdbcTemplate().update(deleteAttributes.toString());
				logger.info("From attribute_value_archive deleted " + deletedAttrs + " rows");
			}
			
			if(deleteCards.length() > 0) {
				// ���� ������� �������� ��� ��������, ����������� ������� select � ���� ������ delete
				deleteCards.insert(0, "delete from card_archive where card_id in ( \n");
				deleteCards.append(" ) \n");
				long deletedCards = getJdbcTemplate().update(deleteCards.toString());
				logger.info("From card_archive deleted " + deletedCards + " rows");
			}
			
			if(insertCards.length() > 0) {
				// ���� ���� �������� ��� �������, ����������� ������� select � ���� ������ insert
				insertCards.insert(0, "insert into card_archive (card_id, template_id, parent_card_id, status_id) \n");
				long insertedCards = getJdbcTemplate().update(insertCards.toString());
				logger.info("To card_archive inserted " + insertedCards + " rows");
			}
			
			if(bpss.getBatchSize() > 0) {
				final int[] inserted = getJdbcTemplate().batchUpdate(sqlIns, bpss);
				int insertedAttributes = 0;
				for (int i : inserted) {
					insertedAttributes += i;
				}
				logger.info("To attribute_value_archive inserted " + insertedAttributes + " rows");
			}
		}
		
	}
	
	public class WriteFromDbArchiveSqlQueryExecutor implements WriteArchiveSqlQueryExecutor {

		@Override
		public void execSqlQuery() {
			final Map<Long, Set<ObjectId>> dataToArchive = action.getCopyDBValues();
			
			StringBuilder insertCards = new StringBuilder();
			StringBuilder insertAttributes = new StringBuilder();
			StringBuilder deleteCards = new StringBuilder();
			StringBuilder deleteAttributes = new StringBuilder();
			
			Iterator<Long> it = dataToArchive.keySet().iterator();
			
			boolean isFirstCard = true;
			boolean isFirstAttrs = true;
			
			while(it.hasNext()) {
				Long currCard = it.next();
				// � ���� ���� ���� ����� ���� ������, �� ����� ����� ������
				if(currCard == null) {
					continue;
				}
				
				if(isFirstCard) {
					isFirstCard = false;
				} else {
					deleteCards.append("union \n");
					insertCards.append("union \n");
				}
				
				deleteCards.append("select card_id from card where card_id = ").append(currCard).append(" \n");
				deleteCards.append("	and not exists (select 1 from attribute_value_archive where card_id = ").append(currCard).append(" ) \n");
				
				insertCards.append("select card_id, template_id, is_active, parent_card_id, file_storage1, external_path, \n");
				insertCards.append("	   locked_by, lock_time, file_name, status_id, file_store_url, keyword \n");
				insertCards.append("from card where card_id = ").append(currCard).append(" \n");
				insertCards.append("	and not exists (select 1 from card_archive where card_id = ").append(currCard).append(" ) \n");
				
				final Set<ObjectId> currAttrs = dataToArchive.get(currCard);
				
				// ���� ����� ��������� ����
				if(CollectionUtils.isEmpty(currAttrs)) {
					continue;
				}
				
				if(isFirstAttrs) {
					isFirstAttrs = false;
				} else {
					deleteAttributes.append("union \n");
					insertAttributes.append("union \n");
				}
				
				deleteAttributes.append("select card_id from attribute_value_archive where card_id = ").append(currCard).append(" \n");
				
				insertAttributes.append("select attr_value_id, card_id, attribute_code, number_value, string_value, \n");
				insertAttributes.append("	   date_value, value_id, another_value, long_binary_value, template_id \n");
				insertAttributes.append("from attribute_value where card_id = ").append(currCard).append(" \n");
				insertAttributes.append(" 	and attribute_code in ( ").append(ObjectIdUtils.numericIdsToCommaDelimitedString(currAttrs, ",", "'", "'")).append(") \n");
			}
			
			if(deleteAttributes.length() > 0) {
				// ���� ������� �������� ��� ��������, ����������� ������� select � ���� ������ delete
				deleteAttributes.insert(0, "delete from attribute_value_archive where card_id in ( \n");
				deleteAttributes.append(" ) \n");
				long deletedAttrs = getJdbcTemplate().update(deleteAttributes.toString());
				logger.info("From attribute_value_archive deleted " + deletedAttrs + " rows");
			}
			
			if(deleteCards.length() > 0) {
				// ���� ������� �������� ��� ��������, ����������� ������� select � ���� ������ delete
				deleteCards.insert(0, "delete from card_archive where card_id in ( \n");
				deleteCards.append(" ) \n");
				long deletedCards = getJdbcTemplate().update(deleteCards.toString());
				logger.info("From card_archive deleted " + deletedCards + " rows");
			}
			
			if(insertCards.length() > 0) {
				// ���� ���� �������� ��� �������, ����������� ������� select � ���� ������ insert
				// ��� ���������� � StringBuilder ��������� ����� � ������, ��������� ����� insert, ������ ���������� � �������� �������
				insertCards.insert(0, "						  locked_by, lock_time, file_name, status_id, file_store_url, keyword) \n");
				insertCards.insert(0, "insert into card_archive (card_id, template_id, is_active, parent_card_id, file_storage1, external_path, \n");
				long insertedCards = getJdbcTemplate().update(insertCards.toString());
				logger.info("To card_archive inserted " + insertedCards + " rows");
			}
			
			if(insertAttributes.length() > 0) {
				// ���� ���� �������� ��� �������, ����������� ������� select � ���� ������ insert
				// ��� ���������� � StringBuilder ��������� ����� � ������, ��������� ����� insert, ������ ���������� � �������� �������
				insertAttributes.insert(0, "						  date_value, value_id, another_value, long_binary_value, template_id) \n");
				insertAttributes.insert(0, "insert into attribute_value_archive (attr_value_id, card_id, attribute_code, number_value, string_value, \n");
				long insertedAttributes = getJdbcTemplate().update(insertAttributes.toString());
				logger.info("To attribute_value_archive inserted " + insertedAttributes + " rows");
			}
		}
		
	}

}
