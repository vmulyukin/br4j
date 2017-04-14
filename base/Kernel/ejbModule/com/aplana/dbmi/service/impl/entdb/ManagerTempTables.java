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
package com.aplana.dbmi.service.impl.entdb;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.model.ObjectId;

/**
 * @author RAbdullin
 * �������� ��������� ������.
 * �������� ������ - ������� ������� ����� �������, ���������� �����.
 * 
 * ������ ������:
 * 
 * 		1) ��� ��������, ��������� � �������� ���� (��������� ��� 
 * ManagerTempTables) ������:
 * 		createAll(); ... ; close();
 * 
 * 		2) �������� ������ ��������� ������ (��� "������" ������):
 * 		createOnly( {...} ); ...; close();
 * 
 * �������� � create-������� ��� ��������� ������: ��. 
 * 		@see(const TMPTABLEID_XXX) 
 * 		@see(Map mapSQLCreate)
 * 
 * ��� ���������� ������������� ��������� ������ ���� ������ ������ ����� 
�������� ������� (����) � �������� (SQL-create) � mapSQLCreate (����� 
������� regTmpTable()).
 */
public class ManagerTempTables {

	protected final Log logger = LogFactory.getLog(getClass());
	private JdbcTemplate jdbc;
	private boolean started;
	
	/**
	 * ���������� Id �������� ��� ������� � ����� batch-insert ��������.
	 */
	final static int CARDINSERT_BATCH_COUNT = 1024;
	final static int CARDINSERT_MIN_BATCH_COUNT = 10;

	/**
	 * ����������� ���-�� �������, ������� � �������� ���������� ��������� ��������� �������.
	 */
	public static final int MIN_EFFECTIVE_COUNT = 4096;

	/**
	 * ������ - �������� ������� (� ������� ��������) <-> SQL ������ �� �������� ������� 
	 */
	private final Map/*Map<String, String>*/ mapSQLCreate =	new HashMap();
	
	/**
	 * ����� ��������� � ������� ������ ������.
	 */
	private final Set /*Set<String>*/ tablesCreated = new HashSet();
	
	/**
	 * true = ��������� ������� ������������ (��������� � start � ��������� � close);
	 * false = (!) ��������� ������� �� ������������, ��������������� ��� 
	 * 			���������� ����������� ����������;
	 */
	private boolean useTempTables = true;
	
	/**
	 * �������� ��������� ������, ������� ����� �������/������� �������������.
	 */

	public final static String TMPTABLEID_CARDID = "gtemp_cardid".toUpperCase();
	public final static String TMPTABLEID_CARDID_WINDOW = "gtemp_cardid_w".toUpperCase();
	public final static String TMPTABLEID_RESULT_CARDID = "gtemp_result_cardid".toUpperCase();
	
	/**
	 * ����� ��������� ������ � �������������� SQL ������� �� �� ��������.
	 * ��������������: 
	 * 		� ������� String-��������, 
	 * 		������� = (0: ��� �������, 1: SQL Create)
	 */
	final static String[][] SQL4TABLES = {

		{ TMPTABLEID_CARDID,
			"CREATE TEMPORARY TABLE "+ TMPTABLEID_CARDID+ " ( \n" + 
			"	card_id numeric(9),\n" + 
			"	ordnum serial NOT NULL PRIMARY KEY \n"+  // (!) ��� ��� ����� �������� ��������� ������� card_id, ���������� ����� �������� xml-SQL. 
			") ON COMMIT DROP;\n"
			+ "CREATE INDEX "+ TMPTABLEID_CARDID +"_IDX ON "
				+ TMPTABLEID_CARDID+" USING btree (card_id);\n" 
		},

		{ TMPTABLEID_CARDID_WINDOW, 
			"CREATE TEMPORARY TABLE "+ TMPTABLEID_CARDID_WINDOW +" ( \n" + 
			"	card_id numeric(9), \n" + 
			"	ordnum serial NOT NULL PRIMARY KEY \n"+  // (!) ��� ��� ����� �������� ��������� ������� card_id, ���������� ����� �������� xml-SQL. 
			") ON COMMIT DROP;\n" 
			+ "CREATE INDEX "+ TMPTABLEID_CARDID_WINDOW + "_IDX ON "
				+ TMPTABLEID_CARDID_WINDOW+ " USING btree(card_id);"
		},
		
		{ TMPTABLEID_RESULT_CARDID,
			"CREATE TEMPORARY TABLE " + TMPTABLEID_RESULT_CARDID + "\n" + 
			"(\n" + 
			"  card_id numeric(9),\n" + 
			"  ordnum serial NOT NULL PRIMARY KEY \n" +
			") ON COMMIT DROP;\n" 
			+ "CREATE INDEX "+ TMPTABLEID_RESULT_CARDID + "_IDX ON "
			+ TMPTABLEID_RESULT_CARDID+ " USING btree(card_id);"
		}
	};

	/**
	 * @param jdbc
	 */
	public ManagerTempTables(JdbcTemplate jdbc) {
		super();
		this.jdbc = jdbc;
		regDefaultTables();
	}

	protected JdbcTemplate getJdbcTemplate() {
		return this.jdbc;
	}

	public void setJdbcTemplate(JdbcTemplate value) {
		if (value == this.jdbc) return;
		if (value != null && value.equals(this.jdbc)) return;
		
		close();
		this.jdbc = value;
		this.started = false;
	}
	
	/**
	 * @return ��������� ������������ ���� ��������� ������ � sql-create 
	 * �������� ��� �� ��������. ���� - ��� ������� � ������� ��������.
	 */
	public Map/*Map<String, String>*/ getMapSQLCreate() {
		return mapSQLCreate;
	}
	
	/**
	 * @param tableName: ��� �������.
	 * @return ������� Sql-create ��� ��������� ������� 
	 * ��� null, ���� ��� ����� ����� ������������������.
	 */
	public String getSqlCreate(final String tableName)
	{
		if (tableName == null) 
			return null;
		return (String) this.mapSQLCreate.get(tableName.toUpperCase());
	}
	
	/**
	 * ���������������� �������� �������. 
	 */
	private void regDefaultTables()
	{
		for (int i = 0; i < SQL4TABLES.length; i++) {
			final String[] tableEntry = SQL4TABLES[i]; 
			this.mapSQLCreate.put( tableEntry[0].toUpperCase(), tableEntry[1]);
		}
	}
	
	/**
	 * ����������������/�������� SQL-������ ��� �������� ������� tableName.
	 * �������� ���� �� startXXX ��� ����� close().
	 * @param tableName
	 * @param sqlCreate: ����� sql-������ ��� �������� ������� tableName,
	 * ���� null -> ������� ���� ������ �� ��������. 
	 * @return (!) ������ SQL-������ �������� �������: ���� null, �� �������
	 * �� ����, ����� ��� ���� � ����� ���� ��������� ������ sql-create. 
	 * @throws Exception 
	 */
	public String regTmpTable(String tableName, final String sqlCreate) 
		throws Exception
	{
		if (isStarted()) 
			throw new Exception("Invalid call of regTmpTable(): tables are opened");
		
		if (tableName == null || tableName.length() < 1)
			return null;
		
		tableName = tableName.toUpperCase();
		final boolean exists = this.mapSQLCreate.containsKey(tableName);
		
		final String prevSql = (exists)
				? (String) this.mapSQLCreate.get(tableName)
				: null;
		
		if (sqlCreate != null) // �������� ��������...
			this.mapSQLCreate.put(tableName, sqlCreate);
		else if (exists) // ������� ���������, �.� �������� null-create...
			this.mapSQLCreate.remove(tableName);
		
		return prevSql; // ������� ������� SQL
	}
	
	/**
	 * @return the tablesCreated
	 */
	public Set getTablesCreated() {
		return this.tablesCreated;
	}

	/**
	 * 
	 * @return ������� ��������� ��������: 
	 * 		true = ��������� ��� ������ ���� �� ������� startXXX, ��������� 
	 * ������� �������;
	 * 		false = ��������� ������ ���� ���, ��������� ��� ����� close() ��� 
	 * ��� �� ���� �� ������ ������ startXXX;
	 *  
	 */
	public boolean isStarted()
	{
		return this.started;
	}
	
	/**
	 * ��������� �������� ���� ������������������ ��������� ������.
	 * (!) ���� ������� �����-���� ��������� ������� (isStarted==true), �� 
	 * �������������� �� ���� �������.
	 * @return true ���� ������� ������� � false ����� (��������, ���� isStarted==true). 
	 */
	public boolean startAll()
	{
		return start( (String[]) this.mapSQLCreate.keySet().toArray( new String[] {} ) );
	}
	
	/**
	 * ��������� �������� ������ ��������� ��������� ������.
	 */
	public boolean start( final String[] tableNames )
	{
		if (this.started) 
			return false;
		createSqlTempTables(tableNames);
		this.started = true;
		return true;
	}
	
	/**
	 * ������� ��������� �������.
	 */
	public void close()
	{
		if (!this.started) return;
		dropSqlTempTables();
		this.tablesCreated.clear();
		this.started = false;
	}
	
	/**
	 * �������� id �������� � ������� gtemp_cardid.
	 * @param cardIds: ������ ObjectId, ������� ���� �������� � gtemp_cardid.
	 */
	public void insertCardIds(final Collection cardIds) {
		insertCardIds( cardIds, true, CARDINSERT_BATCH_COUNT);
	}

	/**
	 * �������� id �������� � ������� gtemp_cardid.
	 * @param cardIds: ������ ObjectId, ������� ���� �������� � gtemp_cardid.
	 * @param useBatch: true, ������������ ������� �������, false: ����������������.
	 * @param batchBlockSize: (�������� ��� useBatch=true) ���-�� ������� ���
	 * ����� batch-insert ��������: ������ �� cardIds ����� ����������� �������
	 * ���������������� �������. ����������� ������������ ��������
	 * CARDINSERT_MIN_BATCH_COUNT (10).
	 */
	private void insertCardIds(final Collection cardIds, boolean useBatch,
			int batchBlockSize) {
		final long timeStart = System.currentTimeMillis();

		if (cardIds == null) return;

		if (useBatch) { /* (2010/01/15, RuSA) ������-�������� ���������� */

			if (batchBlockSize < CARDINSERT_MIN_BATCH_COUNT)
				batchBlockSize = CARDINSERT_MIN_BATCH_COUNT;

			final List listBuf = new ArrayList();

			for( final Iterator itemId = cardIds.iterator(); itemId.hasNext(); ) {
				final ObjectId cardId = (ObjectId) itemId.next();
				listBuf.add(cardId);
				// ���� ������� ������ ����� ��� ��� ���������  ->  ��������...
				if (listBuf.size() >= batchBlockSize || !itemId.hasNext())
				{
					insertCardIdsBatch(listBuf);
					listBuf.clear();
				}
			}

			if (listBuf.size() > 0) // ��-����, ���� �������� ������ �� ������ (RuSA)
				insertCardIdsBatch(listBuf);

		} else {		/* ���������������� ����������: */
			for( final Iterator i = cardIds.iterator(); i.hasNext(); ) {
				final ObjectId cardId = (ObjectId) i.next();
				if (logger.isDebugEnabled())
					logger.debug( String.format( "Insert card id %d into gtemp_cardid", 
							cardId.getId() ));

				getJdbcTemplate().update("INSERT INTO gtemp_cardid (card_id) VALUES (?)",
						new Object[] { (Long) cardId.getId() },
						new int[] { Types.NUMERIC });
			}
		}

	}

	/**
	 * �������� id �������� � ������� gtemp_cardid ������ �������.
	 * @param cardIds: ������ ObjectId, ������� ���� �������� � gtemp_cardid.
	 */
	private void insertCardIdsBatch( final Collection cardIds) {
		if (cardIds == null || cardIds.isEmpty()) return;

		// TODO ����������� ������������ ������ ������ �������� �������:
		// "INSERT INTO gtemp_cardid (card_id) VALUES (1),(2),(3),(4),..."
		
		/* �������� ���������� */
		final BatchPreparedStatementSetter pss =
			new BatchPreparedStatementSetter() {
				final Iterator iter = cardIds.iterator();

				public int getBatchSize() {
					return cardIds.size();
				}

				public void setValues(PreparedStatement stmt, int index)
					throws SQLException
				{
					final ObjectId cardId = (ObjectId) iter.next();
					if (cardId == null || cardId.getId() == null)
						stmt.setNull( 1, Types.NUMERIC);
					else
						stmt.setLong( 1, ((Long) cardId.getId()).longValue());
				}
			};
		getJdbcTemplate().batchUpdate("INSERT INTO gtemp_cardid (card_id) VALUES (?)", pss);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}
	
	private void createSqlTempTables(final String[] tableNames)
	{
		this.tablesCreated.clear();
		for(int i = 0; i < tableNames.length; i++ ) {
			final String sqlCreate = (String) mapSQLCreate.get(tableNames[i]);
			if (useTempTables && sqlCreate == null)
				throw new IllegalArgumentException( String.format(
						"No SQL-create for table '%s'", 
						new Object[] {tableNames[i]}
						));
			createSqlTable(tableNames[i], sqlCreate);
		}
	}

	/**
	 * @param string
	 * @param string2
	 */
	private void createSqlTable(String tableName, String sqlCreate)
	{
		// ��������, ���� ����, ������� ������� ...
		// if (isTableExists(tableName))
		//	dropSqlTable(tableName);
		
		// �������� ������� ...
		if (useTempTables) {
			// �������� ������ - ���������� ��������� �������...
			logger.trace( String.format( "creating temporary SQL table '%s' ...", 
					new Object[] {tableName} ));
			final long timeStart = System.currentTimeMillis();
			this.getJdbcTemplate().update(sqlCreate);
			final long duration = System.currentTimeMillis() - timeStart;
			logger.debug( String.format( "(OK) temporary SQL table '%s' created in %d ms", 
					new Object[] {tableName, new Long(duration) } ));
		} else {
			// �������� ��������� -> ������� ��� ���������� ���������� �������...
			// ������ � �����
			logger.trace( String.format( "work/temporary SQL table '%s' suggested to be persistent ...", 
					new Object[] {tableName} ));
			// this.getJdbcTemplate().update( "delete from " + tableName );
		}
		this.tablesCreated.add(tableName);
	}

	private void dropSqlTempTables()
	{
		for (Iterator iterator = tablesCreated.iterator(); iterator.hasNext();) {
			final String tableName = (String) iterator.next();
			dropSqlTable(tableName);
		}
	}

	/**
	 * @param tableName: ���������� �������.
	 * @return true, ���� ������� �������.
	 */
	private boolean dropSqlTable(String tableName) {
		try {
			if (useTempTables) {
				// �������� ������ - ���������� ��������� �������...
				logger.trace( String.format( "dropping temporary SQL table '%s' ...", 
						new Object[] {tableName} ));
				final long timeStart = System.currentTimeMillis();
				this.getJdbcTemplate().update( String.format( "DROP TABLE %s", 
						new Object[] {tableName} ));
				final long duration = System.currentTimeMillis() - timeStart;
				logger.debug( String.format( "(OK) SQL table '%s' dropped in %d ms", 
						new Object[] {tableName, new Long(duration) } ));
			} else {
				// �������� - ������� ��� ���������� ���������� �������...
				// ������ � �����
				logger.trace( String.format( "clearing of work persistent SQL table '%s'...", 
						new Object[] {tableName} ));
				final long timeStart = System.currentTimeMillis();
				this.getJdbcTemplate().update( "delete from " + tableName );
				final long duration = System.currentTimeMillis() - timeStart;
				logger.debug( String.format( "(OK) persistent SQL table '%s' cleared in %d ms", 
						new Object[] {tableName, new Long(duration) } ));
			}
			return true;
		} catch (Exception ex) {
			logger.warn( String.format( "SQL drop table '%s' error: %s", 
					new Object[] {tableName, ex.toString() } ));
			return false;
		}
	}

}
