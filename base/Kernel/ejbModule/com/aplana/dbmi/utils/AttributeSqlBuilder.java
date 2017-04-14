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
package com.aplana.dbmi.utils;

import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import com.aplana.dbmi.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.PreparedStatementSetter;

import com.aplana.dbmi.service.DataException;

/**
 *	����� � �������������� SQL-�������� ������������ ������� Attribute ��� 
 * ��������/���������� ��/� �������(�) attribute_value.
 * @author rabdullin
 */
public abstract class AttributeSqlBuilder {

	/**
	 * ������� � ������� attribute_value ...
	 * (����� ������� �� 2011/07/28, RuSA)
	 */
	public static final String[] AV_COLUMNS = {
			// 'attr_value_id',
			"card_id",
			"attribute_code",
			"number_value",

			"string_value",
			"date_value",
			"value_id",

			"another_value",
			"long_binary_value"
			// 'template_id'
	};

	public static final String AV_DATE_COLUMN = "date_value";

	/**
	 * ������� � ������� attribute_value � ������� ��� ����������� ��������-��������.
	 * (����� ������� � ������� �� 2011/07/28, RuSA)
	 */
	public static final String[] AV_DATA_COLUMNS = {
		"number_value",
		"string_value",
		AV_DATE_COLUMN,

		"value_id",
		"another_value",
		"long_binary_value"
	};

	public static class DataAttributeSQL extends DataException 
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public DataAttributeSQL() {
			super();
		}

		/**
		 * @param msgId
		 * @param params
		 * @param cause
		 */
		public DataAttributeSQL(String msgId, Object[] params, Throwable cause) {
			super(msgId, params, cause);
		}

		/**
		 * @param msgId
		 * @param params
		 */
		public DataAttributeSQL(String msgId, Object[] params) {
			super(msgId, params);
		}

		/**
		 * @param msgId
		 * @param cause
		 */
		public DataAttributeSQL(String msgId, Throwable cause) {
			super(msgId, cause);
		}

		/**
		 * @param msgId
		 */
		public DataAttributeSQL(String msgId) {
			super(msgId);
		}

		/**
		 * @param cause
		 */
		public DataAttributeSQL(Throwable cause) {
			super(cause);
		}
	}

	/**
	 * ������������ SQL-������� �������� �� ��������� �������� ���� �������, �� 
	 * ���������� ��� � ������� attribute_value.
	 * 
	 * (!) �������� null ��������� ��� ������ ���� �����.
	 * ��������� ����:
	 * 		( ((av1.x is null) and (av2.x is null)) or (av1.x = av2.x) )
	 * 		and ( ((av1.y is null) and (av2.y is null)) or (av1.y = av2.y) )
	 * 		... ������ {x,y} ����� ��� ������ ������� ������� av ...
	 * 
	 * @param dest �������� �����
	 * @param aliasAV1 ����� ������ �������
	 * @param aliasAV2 ����� ������ �������
	 * @param indent ������ ����������� ������� �� ������ ������, ��� 
	 * ��������������� ���������������� sql. 
	 */
	public static void emmitAttrSqlCompareEqConditions( StringBuffer dest, 
			String tableAlias1, String tableAlias2, String indent)
	{
		// if (dest == null) return;
		if (indent == null) indent = "";
		dest.append(indent).append(MessageFormat.format("{0}.attribute_code = {1}.attribute_code \n", tableAlias1, tableAlias2));
		for (String colName: AV_DATA_COLUMNS) {
			dest.append(indent).append("AND ");
			if (AV_DATE_COLUMN.equals(colName))
				emmitSqlDateCompareEq(dest, tableAlias1 + "." +colName, tableAlias2 + "." +colName, null);
			else
				emmitSqlCompareEq(dest, tableAlias1 + "." +colName, tableAlias2 + "." +colName);
			dest.append("\n");
		}
	}

	public static void emmitAttrSqlCompareEqConditions( StringBuffer dest, 
			String tableAlias1, String tableAlias2) 
	{
		emmitAttrSqlCompareEqConditions(dest, tableAlias1, tableAlias2, "\t");
	}

	public static String emmitAttrSqlCompareEqConditions(  
			String tableAlias1, String tableAlias2, String indent)
	{
		final StringBuffer dest = new StringBuffer();
		emmitAttrSqlCompareEqConditions(dest, tableAlias1, tableAlias2, indent);
		return dest.toString();
	}

	private static final String FMT_SQL_CMP_AV_ANY_COLUMN_2 = "( (({0} is null) and ({1} is null)) or ({0} = {1}) )";
	private static final String FMT_SQL_CMP_AV_DATE_COLUMN_3 = "( (({0} is null) and ({1} is null)) or ( date_trunc( ''{2}'', {0}) = date_trunc( ''{2}'', {1})) )";


	/**
	 * ����������� SQL-������� �������� ���� �������.
	 * (!) �������� null ��������� ��� ������ ���� �����.
	 * @param dest �������� �����
	 * @param colName1 ����� ������ �������
	 * @param colName2 ����� ������ �������
	 */
	public static void emmitSqlCompareEq( StringBuffer dest, String colName1, String colName2) {
		// if (dest == null) return;
		dest.append(MessageFormat.format( FMT_SQL_CMP_AV_ANY_COLUMN_2, colName1, colName2));
	}

	final static String CMP_DATE_DEFAULT_PRECISION = "second";

	/**
	 * ����������� SQL-������� �������� ���� ������� � ����� ����.
	 * �������� ������� ����������. �� ��������� = �������.
	 * (!) �������� null ��������� ��� ������ ���� �����.
	 * @param dest �������� �����
	 * @param colName1 ����� ������ �������
	 * @param colName2 ����� ������ �������
	 * @param precise	������� ��������: 'second', 'minute' � �.�; 
	 * �������� null �������� 'second'.
	 */
	public static void emmitSqlDateCompareEq( StringBuffer dest, String colName1, 
			String colName2, String precise) 
	{
		if (precise == null) precise = CMP_DATE_DEFAULT_PRECISION;
		// if (dest == null) return;
		dest.append(MessageFormat.format( FMT_SQL_CMP_AV_DATE_COLUMN_3, colName1, colName2, precise));
	}


	/**
	 * ������������ Delete-/Select- ������ ��� ������� attribute_value.
	 * @param builder �������������� ����� ����� ��������� (��� ������� ��� ������ ��������) 
	 * (!) ����� � �������� sql-������� ������ ���������� ����� cardId, �� ����� 
	 * �� �� ����������� � ��������� builder, � �� ���� ���� ������������ 
	 * ��������� ���� ��������:
	 * 		builder.insertArg( 0, cardId, Types.NUMERIC );
	 * @param operation sql-��������, �������� "DELETE" ��� "SELECT *"
	 * @param tableAlias sql-����� ��� ������� ������� attribute_value
	 * @param whereMoreCond ��� sql-������� ��� where-�����
	 * @return ����� � �������������� sql-�������.
	 */
	private static StringBuffer makeSqlAnyByCardId( 
			InsertAttributesSQLBuilder builder, 
			String operation,
			String tableAlias, String whereMoreCond
			) 
	{
		final StringBuffer dest = new StringBuffer();
		if (builder != null) {
			dest.append( operation);
			dest.append( MessageFormat.format( " FROM attribute_value AS {0} \nWHERE {0}.card_id = ? \n", tableAlias))	;

			// ��� where-������� ...
			if (whereMoreCond != null)
				dest.append( whereMoreCond );

			// ���������� �������� �� ��������� � ����� ������ ...
			dest.append( "	AND ( \n");
			/*
			 * 25.11.2011, O.E. - ��������������� ������ ������������ �������
			 * */
			// TODO: BR4J00004381. Order of coexecutors
			// ���� ���������� ��������� ����� ��������
			if (!builder.getAtrrCodesOrderedList().isEmpty())
				dest	.append( MessageFormat.format( "		{0}.attribute_code in ( ", tableAlias))
						.append(StrUtils.getAsString(builder.getAtrrCodesOrderedList(), ", ", "\'"))
						.append(") \n		")
						.append(" OR ");
			dest.append( " NOT EXISTS ( \n");
			dest.append( "				SELECT 1 \n");
			dest.append( "				FROM ( \n");
			if(builder.hasAttributes()) dest.append( builder.getSqlBuf() );
			else dest.append(" \t\t\t\t\t select text(null) as attribute_code, cast(null as numeric) as number_value, ")
				.append("cast(null as varchar) as string_value, cast(null as timestamp) as date_value, cast(null as numeric) as value_id, ")
				.append("cast(null as varchar) as another_value, cast(null as BYTEA) as long_binary_value \n");
			dest.append( "				) as src \n");
			dest.append( "				WHERE \n");
			dest.append( AttributeSqlBuilder.emmitAttrSqlCompareEqConditions( tableAlias, "src", "\t\t\t\t\t"));
			dest.append( "		) -- /OR NOT EXISTS \n" );

			// (!) (2011/09/05, RuSA) ����� ���� ����� ������� ...
			// "�������, ���� ���� ����� ����� �������� � ������� id, ��� � ������� ..."
			dest.append( MessageFormat.format(
					"		{1} exists( \n"+
					"				select avDups.attr_value_id \n"+
					"				from attribute_value avDups \n"+
					"				where \n"+ 
					"					avDups.attr_value_id < {0}.attr_value_id \n"+
					"					and avDups.card_id = {0}.card_id \n"+
					"					and avDups.attribute_code = {0}.attribute_code \n"+
					"						and ( ((avDups.number_value is null) and ({0}.number_value is null)) or (avDups.number_value = {0}.number_value) ) \n"+
					"						and ( ((avDups.string_value is null) and ({0}.string_value is null)) or (avDups.string_value = {0}.string_value) ) \n"+
					"						and ( ((avDups.date_value is null) and ({0}.date_value is null)) or ( avDups.date_value = {0}.date_value) ) \n"+
					"						and ( ((avDups.value_id is null) and ({0}.value_id is null)) or (avDups.value_id = {0}.value_id) ) \n"+
					"						and ( ((avDups.another_value is null) and ({0}.another_value is null)) or (avDups.another_value = {0}.another_value) ) \n"+
					"						and ( ((avDups.long_binary_value is null) and ({0}.long_binary_value is null)) or (avDups.long_binary_value = {0}.long_binary_value) ) \n"+
					"		) --/or exists \n"
				, tableAlias, ("or") ));

			dest.append( "	) -- /AND \n");

		}
		return dest;
	}

	/**
	 * ������������ Delete-������ ��� ������� attribute_value.
	 * @param builder �������������� ����� ����� ��������� (��� ������� ��� ������ ��������) 
	 * (!) ����� � �������� sql-������� ������ ���������� ����� cardId, �� ����� 
	 * �� �� ����������� � ��������� builder, � �� ���� ���� ������������ 
	 * ��������� ���� ��������:
	 * 		builder.insertArg( 0, cardId, Types.NUMERIC );
	 * @param tableAlias sql-����� ��� ������� ������� attribute_value
	 * @param whereMoreCond ��� sql-������� ��� where-�����
	 * @return ����� � �������������� sql-�������.
	 */
	public static StringBuffer makeSqlDeleteOldByCardId( 
			InsertAttributesSQLBuilder builder, 
			String tableAlias, String whereMoreCond
			) {
		return makeSqlAnyByCardId(builder, "DELETE", tableAlias, whereMoreCond);
	} 

	/**
	 * ������������ Select-������ ��� ������� attribute_value.
	 * @param builder �������������� ����� ����� ��������� (��� ������� ��� ������ ��������) 
	 * (!) ����� � �������� sql-������� ������ ���������� ����� cardId, �� ����� 
	 * �� �� ����������� � ��������� builder, � �� ���� ���� ������������ 
	 * ��������� ���� ��������:
	 * 		builder.insertArg( 0, cardId, Types.NUMERIC );
	 * @param tableAlias sql-����� ��� ������� ������� attribute_value
	 * @param whereMoreCond ��� sql-������� ��� where-�����
	 * @return ����� � �������������� sql-�������.
	 */
	public static StringBuffer makeSqlSelectOldByCardId( 
			InsertAttributesSQLBuilder builder, 
			String tableAlias, String whereMoreCond
			) {
		return makeSqlAnyByCardId(builder, "SELECT *", tableAlias, whereMoreCond);
	} 

	/**
	 * ��������� ��� ���������� ����������������� SQL-�������� �� ������� Attributes.
	 * @author rabdullin
	 *
	 */
	public static class InsertAttributesSQLBuilder { 

		final Log logger = LogFactory.getLog(getClass());

		final private StringBuffer sqlBuf = new StringBuffer();
		final private List<Object> sqlArgs = new ArrayList<Object>();
		final private List<Integer> sqlTypes = new ArrayList<Integer>();

		// ���� ��������� � �������������� �������� - ������������ ���
		// ����� ������� � ����� � ������ ��������� (��������, "�����������" ��� "�������������") 
		final private List<String> atrrCodesOrderedList = new ArrayList<String>();

		// ������ ���������� ��������� (��. {@link emmitSelectAttributeValue}) ...
		// (!) ����� ��� ������ ������ av ����������� �������, �.�. persons/cardlinks
		// ����� �������� ��������� ��������� ������ attribute - �� ������ �� ������ ������� ...
		final private List<Attribute> emmitedAttrs = new ArrayList<Attribute>();

		// ������ ����������� ��������� (��. {@link emmitSelectAttributeValue}) ...
		final private List<Attribute> skippedEmmitAttrs = new ArrayList<Attribute>();

		// ������ ��������� ������ (��. {@link emmitSelectAttributeValue}) ...
		final private List<SecurityAttribute> securityAttrs = new ArrayList<SecurityAttribute>();

		public InsertAttributesSQLBuilder() {
			super();
		}
		
		public InsertAttributesSQLBuilder(String attrs) {
			super();
			// TODO: ��������������� ���� ���� (��� "������������" � "��������������") 
			/*this.atrrCodesOrderedList.add( "JBR_INFD_EXECUTOR");
			//this.atrrCodesOrderedList.add( "JBR_INFD_EXEC_LINK" );
			this.atrrCodesOrderedList.add( "ADMIN_255974");
			this.atrrCodesOrderedList.add( "ADMIN_255979");*/
			
			String  overwriteAttributes = attrs;
			String[] attrList = overwriteAttributes.split(",");
			for(String s: attrList){
				this.atrrCodesOrderedList.add(s.trim());
			}
		}

		/**
		 * @return �������� ����� ��������������� SQL-������.
		 */
		public StringBuffer getSqlBuf() {
			return this.sqlBuf;
		}

		/**
		 * @return true, ���� ���� �������� ����� ��� sql, ���������� �������
		 * ������������.
		 * @deprecated use {@link hasAttributes}
		 */
//		@Deprecated
//		public boolean hasSqlText() {
//			final String sql = this.sqlBuf.toString();
//			return (sql != null) && (sql.trim().length() > 0);
//		}

		/**
		 * @return true, ���� � ������ ������� ���� �������� ��� ����������.
		 */
		public boolean hasAttributes() {
			return this.emmitedAttrs.size() > 0;
		}

		/**
		 * @return �������� SQL-����������
		 */
		public List<Object> getSqlArgs() {
			return this.sqlArgs;
		}

		public Object[] args() {
			return this.sqlArgs.toArray();
		}

		public Object args(int index) {
			return this.sqlArgs.get(index);
		}

		/**
		 * @return ���� SQL-����������
		 */
		public List<Integer> getSqlTypes() {
			return this.sqlTypes;
		} 

		public int[] types() {
			final int[] result = new int[this.sqlTypes.size()];
			for (int i = 0; i < this.sqlTypes.size(); i++) {
				result[i] = this.sqlTypes.get(i);
			}
			return result;
		} 

		/**
		 * @return ������ ����������� ��������� Attribute, ������� ����� � ���� ���� 
		 * � SQL-������ (� ������ ���� ���������).
		 * ������ emmitedAttrs, skippedEmmitAttrs � securityAttrs �������� ������ ����� ���������. 
		 * ��. ����� {@link emmitSelectAttrValue} � {@link emmitInsertAttributes}
		 */
		public List<Attribute> getEmmitedAttrs() {
			return this.emmitedAttrs;
		}

		/**
		 * @return ������ ������������� ��������� Attribute, ������� �� ����� � 
		 * ���� ���� � SQL-������ (�� ���� � ����������� ��������).
		 * ������ emmitedAttrs, skippedEmmitAttrs � securityAttrs �������� ������ ����� ���������. 
		 * ��. ����� {@link emmitSelectAttrValue} � {@link emmitInsertAttributes}
		 */
		public List<Attribute> getSkippedEmmitAttrs() {
			return this.skippedEmmitAttrs;
		}

		/**
		 * @return ������ ��������� SecurityAttribute, ������� �� ����� � ���� ���� 
		 * � SQL-������, �� ������ ���� ���������.
		 * ������ emmitedAttrs, skippedEmmitAttrs � securityAttrs �������� ������ ����� ���������. 
		 * ��. ����� {@link emmitSelectAttrValue} � {@link emmitInsertAttributes}
		 */
		public List<SecurityAttribute> getSecurityAttrs() {
			return this.securityAttrs;
		}

		/**
		 * @return ������ ����� ��������� � �������������� ��������, 
		 * �������� 'JBR_INFD_EXECUTOR'/("�����������") ��� 'ADMIN_255974'/("�������������").
		 */
		public List<String> getAtrrCodesOrderedList() {
			return this.atrrCodesOrderedList;
		}

		/**
		 * �������� setter-���������� ��� �������� sql-������� � sqlBuf.
		 * ������������ ����� sqlArgs/sqlTypes.
		 * @return
		 */
		public PreparedStatementSetter getPreparedStatementSetter() {

			return new PreparedStatementSetter() {

				final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

				public void setValues(PreparedStatement ps)
						throws SQLException 
				{
					if (getSqlArgs().size() > getSqlTypes().size())
						throw new SQLException( MessageFormat.format("{0} arguments has only {1} types", 
								getSqlArgs().size(), getSqlTypes().size()));
					for (int i =0; i < getSqlArgs().size(); i++) {
						final int sqlParIndex = i+1;
						final Object arg = getSqlArgs().get(i); 
						final int sqlType = getSqlTypes().get(i);
						if (arg == null)
							ps.setNull(sqlParIndex, sqlType);
						else if (arg instanceof Date) {
							ps.setTimestamp( sqlParIndex, SimpleDBUtils.sqlTimestamp((Date) arg), calendar);
						} else if (arg instanceof byte[] ) {
							ps.setBytes(sqlParIndex, (byte[]) arg);
						} else { 
							// possibly must implement case for String
							ps.setObject(sqlParIndex, arg, sqlType);
						}
					}
				}
			};
		} 


		/*
		 * ����� ��������� ���� SELECT_XXX ������ ������� ��� ����-��� ����� ���������.
		 * �������� ������� ��� attribute_value  � ���� ��������:
		 * 		1) inline: attribute_code
		 * 		2) "number_value", 
		 * 		3) "string_value",
		 * 		4) "date_value",
		 * 		5) "value_id",
		 * 		6) "another_value",
		 * 		7) "long_binary_value"
		 * 
		 * �� ����� DoOverwrireCardAttributes, � ������� ������� ������������� ���:
		 * 			(1) card_id,
		 * 			(2) attribute_code,
		 * 			(3) number_value,
		 * 			(4) string_value,
		 * 			(5) date_value,
		 * 			(6) value_id,
		 * 			(7) another_value,
		 * 			(8) long_binary_value
		 */
		
		final static String SELECT_NULL = 
			"SELECT cast(NULL as VARCHAR) as attribute_code" +
			", cast(NULL as NUMERIC) as number_value" +
			", cast(NULL as VARCHAR) as string_value " +
			", cast(NULL as TIMESTAMP) as date_value " +

			", cast(NULL as NUMERIC) as value_id " +
			", cast(NULL as VARCHAR) as another_value " +
			", cast(NULL as BYTEA) as long_binary_value \n"
			;

		final static String SELECT_NUMBER_VALUE_2 =
			// "SELECT cast(''{0}'' as VARCHAR) as attribute_code " +
			"SELECT text(''{0}'') as attribute_code " +
			", cast({1} as NUMERIC) as number_value" +
			", cast(NULL as VARCHAR) as string_value " +
			", cast(NULL as TIMESTAMP) as date_value " +

			", cast(NULL as NUMERIC) as value_id " +
			", cast(NULL as VARCHAR) as another_value " +
			", cast(NULL as BYTEA) as long_binary_value \n"
			;

		final static String SELECT_NUMBER_VALUE_AND_VALUE_ID_3 =
			"SELECT text(''{0}'') as attribute_code " +
			", cast({1} as NUMERIC) as number_value" +
			", cast(NULL as VARCHAR) as string_value " +
			", cast(NULL as TIMESTAMP) as date_value " +

			", cast({2} as NUMERIC) as value_id " +
			", cast(NULL as VARCHAR) as another_value " +
			", cast(NULL as BYTEA) as long_binary_value \n"
			;
		
		final static String SELECT_NUMBER_VALUE_AND_DATE_VALUE =
				"SELECT text(''{0}'') as attribute_code " +
				", cast({1} as NUMERIC) as number_value" +
				", cast(NULL as VARCHAR) as string_value " +
				", cast(? as TIMESTAMP) as date_value " +

				", cast(NULL as NUMERIC) as value_id " +
				", cast(NULL as VARCHAR) as another_value " +
				", cast(NULL as BYTEA) as long_binary_value \n"
				;
		
		final static String SELECT_NUMBER_VALUE_AND_DATE_VALUE_AND_VALUE_ID =
				"SELECT text(''{0}'') as attribute_code " +
				", cast({1} as NUMERIC) as number_value" +
				", cast(NULL as VARCHAR) as string_value " +
				", cast(? as TIMESTAMP) as date_value " +

				", cast({2} as NUMERIC) as value_id " +
				", cast(NULL as VARCHAR) as another_value " +
				", cast(NULL as BYTEA) as long_binary_value \n"
				;

		final static String SELECT_STRING_VALUE_1_str =
			"SELECT text(''{0}'') as attribute_code " +
			", cast(NULL as NUMERIC) as number_value" +
			", cast(? as VARCHAR) as string_value " +
			", cast(NULL as TIMESTAMP) as date_value " +

			", cast(NULL as NUMERIC) as value_id " +
			", cast(NULL as VARCHAR) as another_value " +
			", cast(NULL as BYTEA) as long_binary_value \n"
			;

		final static String SELECT_DATE_VALUE_1_date =
			"SELECT text(''{0}'') as attribute_code " +
			", cast(NULL as NUMERIC) as number_value" +
			", cast(NULL as VARCHAR) as string_value " +
			", cast(? as TIMESTAMP) as date_value " +

			", cast(NULL as NUMERIC) as value_id " +
			", cast(NULL as VARCHAR) as another_value " +
			", cast(NULL as BYTEA) as long_binary_value \n"
			;

		final static String SELECT_VALUE_ID_2_str =
			"SELECT text(''{0}'') as attribute_code " +
			", cast(NULL as NUMERIC) as number_value" +
			", cast(NULL as VARCHAR) as string_value " +
			", cast(NULL as TIMESTAMP) as date_value " +

			", cast({1} as NUMERIC) as value_id " +
			", cast(? as VARCHAR) as another_value " +
			", cast(NULL as BYTEA) as long_binary_value \n"
			;

		final static String SELECT_LONG_BINARY_VALUE_1_bytes =
			"SELECT text(''{0}'') as attribute_code " +
			", cast(NULL as NUMERIC) as number_value" +
			", cast(NULL as VARCHAR) as string_value " +
			", cast(NULL as TIMESTAMP) as date_value " +

			", cast(NULL as NUMERIC) as value_id " +
			", cast(NULL as VARCHAR) as another_value " +
			", cast(? as BYTEA) as long_binary_value \n"
			;

		public void clear() {
			sqlBuf.setLength(0);
			bufAttrOrder = 0;
			sqlArgs.clear();
			sqlTypes.clear();

			emmitedAttrs.clear();
			skippedEmmitAttrs.clear();
			securityAttrs.clear();
		}

		/**
		 * ������������� ����������������� select-������, ������� ��������� ������
		 * �� ���� ��������� ���������, � ����:
		 * 		select (... av-������� ��������1 ...)
		 * 		union select (... av-������� ��������2 ...)
		 * 		union select (... av-������� ��������3 ...)
		 * 		...
		 * 
		 * @param list
		 * @param indent
		 * @return true, ���� ��� �������� ���� �� ���� �������.
		 */
		public boolean emmitSelectAttributes( Collection<Attribute> list, String indent)
			throws DataException
		{
			if (list == null)
				return false;
			boolean isFirst = true;
			int attrBuilt = 0; // ���-�� ���������, �������� � sql
			for (Attribute attr: list) {
				final int startPos = this.sqlBuf.length(); 
				if (emmitSelectAttributeValue(attr, indent)) {
					attrBuilt++;
					if (!isFirst) 
						this.sqlBuf.insert( startPos, indent + "UNION \n");
					isFirst = false;
				}
			}
			if (isFirst)
				this.sqlBuf.append("\n");
			logger.debug( attrBuilt 
					+ " attributes put into preparing sql, records caount is "
					+ this.emmitedAttrs.size() );
			return !isFirst;
		}

		/**
		 * ������������ select-������, ���������� �������� ��������.
		 * ���, ������� �����:
		 * 		SELECT NULL, ..., ?, ..., NULL
		 * � ��������� � �������:
		 * 		1) "number_value", 
		 * 		2) "string_value",
		 * 		3) "date_value",
		 * 		4) "value_id",
		 * 		5) "another_value",
		 * 		6) "long_binary_value"
		 * (�� ����� ������ AV_DATA_COLUMNS), ��� ����� NULL, ����� �������
		 * ����-��� ���� ��������, ��� ������� ����� �������� ��������-��������
		 * � sqlArgs/sqlTypes.
		 *   ������� ����������� ������ ���� �������� �� ������, ��� ���� ��� 
		 * ������� ��������� (�� �������) ����������� ����� ���� ������ � sql,
		 * � ��� ��������� (Persons/Links) ������� �����, ������� ������� � 
		 * ������ �� ������ ��������, ����������� � this.emmitedAttrs �� ������ 
		 * ������ ���������� (������������) �������.
		 * 
		 * @param attr
		 * @param indent ������ ������������ ������ �� ������ ������, ��� 
		 * ��������������� ���������������� sql.
		 *  
		 * @return true, ���� ��� ������� �������� ���� �������� select,
		 * false ����� (��������, ��� BackLinkAttribute ��� SecurityAttr ������ � sql �� ���������).
		 */
		@SuppressWarnings("unchecked")
		public boolean emmitSelectAttributeValue( Attribute attr, final String indent)
			throws DataException
		{
			if (attr == null) 
				return false;

			final String attributeCode = (String) attr.getId().getId();
			if (attributeCode == null) {
				logger.warn( "attribute "+ attr + " has NULL attributeCode");
				return false;
			}

			// BackLinkAttribute || PseudoAttribute...
			if (attr instanceof BackLinkAttribute || attr instanceof PseudoAttribute) {
				skippedEmmitAttrs.add(attr);
				return false;
			}

			if (attr instanceof SecurityAttribute) {
				this.securityAttrs.add( (SecurityAttribute) attr);
				return false;
			}

			boolean result = false;
			int processed = 1; // ���-�� ������������ ��������� ...
			final int startBufPos = this.sqlBuf.length();
			try {
				if (attr.isEmpty()) {
					logger.debug(MessageFormat.format("attribute ''{0}'' is empty -> no emition performed", attributeCode));
				}

				// IntegerAttribute ...
				else if (attr instanceof IntegerAttribute) {
					final IntegerAttribute intAttr = (IntegerAttribute) attr;
					this.addSelectInt(attributeCode, intAttr.getValue() );
					result = true;
				}

                // LongAttribute ...
				else if (attr instanceof LongAttribute) {
					final LongAttribute intAttr = (LongAttribute) attr;
					this.addSelectLong(attributeCode, intAttr.getValue() );
					result = true;
				}

				// DateAttribute ...
				else if (attr instanceof DateAttribute) {
					final DateAttribute dateAttr = (DateAttribute) attr;
					result = this.addSelectDate(attributeCode, dateAttr.getValue());
				}

				// HtmlAttribute (this must be before StringAttribute cause of derivation)...
				else if (attr instanceof HtmlAttribute) {
					final HtmlAttribute htmlAttr = (HtmlAttribute) attr;
					result = this.addSelectBinary(attributeCode, htmlAttr.getValue() );
				}

				// StringAttribute 
				// and TextAttribute 
				// and CardHistoryAttribute ...
				else if (attr instanceof StringAttribute) {
					final StringAttribute strAttr = (StringAttribute) attr;
					result = this.addSelectString(attributeCode, strAttr.getValue());
				}

				// ListAttribute ...
				else if (attr instanceof ListAttribute) {
					final ReferenceValue ref = ((ListAttribute) attr).getValue();
					result = this.addSelectReference(attributeCode, ref);
				}

				// TreeAttribute ...
				else if (attr instanceof TreeAttribute) {
					final Collection<ReferenceValue> refs = ((TreeAttribute) attr).getValues();
					final HashSet<Long> ids = new HashSet<Long>();
					if (refs == null)
						return false;
					boolean isFirst = true;
					processed = 0;
					for (ReferenceValue ref : refs) {
						final int startPos = this.sqlBuf.length();
						final Long id = (Long) ref.getId().getId();
						//�� �� �����, ��� � � PersonAttribute
						if(ids.add(id)){
							if (this.addSelectReference(attributeCode, ref)) {
								if (!isFirst)
									this.sqlBuf.insert(startPos, indent + "UNION -- more tree items \n" + indent);
								isFirst = false;
								++processed;
							}
						}
					}
					result = !isFirst;
				}

				// CardLinkAttribute
				// and TypedCardLinkAttribute ...
				else if (attr instanceof CardLinkAttribute) {
					final Collection<ObjectId> ids = ((CardLinkAttribute)attr).getIdsLinked();
					if (ids == null)
						return false;
					boolean isFirst = true;
					processed = 0;
					for (final ObjectId id : ids ) 
					{
						final int startPos = this.sqlBuf.length();
						Long typeId = null;
						if (CardLinkAttribute.class.equals(attr.getClass())) {
							result = this.addSelectLongAndValId(attributeCode, (Long) id.getId(), typeId);
						} else if (TypedCardLinkAttribute.class.equals(attr.getClass())) {
							typeId = (Long)((TypedCardLinkAttribute)attr).getTypes().get(id.getId());
							result = this.addSelectLongAndValId(attributeCode, (Long) id.getId(), typeId);
						} else if (DatedTypedCardLinkAttribute.class.equals(attr.getClass())) {
							typeId = (Long)((DatedTypedCardLinkAttribute)attr).getTypes().get(id.getId());
							final Date dateValue = ((DatedTypedCardLinkAttribute)attr).getDates().get(id.getId());
							result = this.addSelectDatedTypedCardLink(attributeCode, (Long) id.getId(), typeId, dateValue);
						}
						if (result) {
							if (!isFirst)
								this.sqlBuf.insert(startPos, indent + "UNION -- more links \n" + indent);
							isFirst = false;
							++processed;
						}
					}
					result = !isFirst;
				}

				// PersonAttribute ...
				else if (attr instanceof PersonAttribute) 
				{
					final Collection<Person> persons = ((PersonAttribute) attr).getValues();
					final HashSet<Long> ids = new HashSet<Long>();
					if (persons == null) 
						return false;

					boolean isFirst = true;
					processed = 0;
					for (Person person : persons) 
					{
						final int startPos = this.sqlBuf.length();
						final Long id = (Long) person.getId().getId();
						/*�� ������ ������ �������� ��������, ����� PersonAttribute ������ ��������� Person � ���������� ObjectId/
						 * �� �� ��������� �� �������. - �.�., 26.10.2011
						 */
						if(ids.add(id)){
							if (this.addSelectLong(attributeCode, id)) {
								if (!isFirst)
									this.sqlBuf.insert(startPos, indent + "UNION -- more persons \n" + indent);
								isFirst = false;
								++processed;
							}
						}
					}
					result = !isFirst;
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
				if (result) {
					// ���������� �������� ������ ���-�� ���, ����� ����-�� ������� ���� Persons/Cardlink/TypedCardlink ...
					for(int i=0; i<processed;i++)
						this.emmitedAttrs.add(attr);
					this.sqlBuf.insert( startBufPos, indent);
				} else {
					this.skippedEmmitAttrs.add(attr);
				}
			}

			return result;
		}

		/**
		 * �������� � �������� sql-����� ��������� ��������.
		 * @param value ��������
		 * @param types ��� sql-��������
		 */
		public void addArg(Object value, int type) {
			this.sqlArgs.add(value);
			this.sqlTypes.add(type);
		}

		/**
		 * �������� �������� � ������ �������.
		 * @param index ������� (�� ����), ������ ���������.
		 * @param value
		 * @param type
		 */
		public void insertArg(int index, Object value, int type) {
			this.sqlArgs.add( index, value);
			this.sqlTypes.add( index, type);
		}

		/**
		 * @param ref
		 * @return
		 */
		private boolean addSelectReference(String attributeCode, ReferenceValue ref) {
			if (ref == null) 
				return false;
			this.sqlBuf.append( MessageFormat.format(SELECT_VALUE_ID_2_str, attributeCode, String.valueOf(ref.getId().getId())));
			addOrderMarker();

			// this.addArg( ref.getId().getId(), Types.NUMERIC );

			final String another = (ReferenceValue.ID_ANOTHER.equals(ref.getId()))
					? ref.getValueRu() : null;
			this.addArg( another, Types.VARCHAR);

			return true;
		}

		private boolean addSelectInt(String attributeCode, int value) {
			return this.addSelectLong( attributeCode, new Long(value) );
		}

		private boolean addSelectLong(String attributeCode, Long value) {
			// (!) ����� NULL-�������� �����������
			if (value == null) return false;
			this.sqlBuf.append( MessageFormat.format(SELECT_NUMBER_VALUE_2, attributeCode, String.valueOf(value)));
			addOrderMarker();
			// this.addArg(value, Types.NUMERIC);
			return true;
		}

		private boolean addSelectString(String attributeCode, String value) {
			// (!) ����� NULL-�������� �����������
			if (value == null || value.length() == 0) return false;
			this.sqlBuf.append( MessageFormat.format(SELECT_STRING_VALUE_1_str, attributeCode));
			addOrderMarker();
			this.addArg(value, Types.VARCHAR);
			return true;
		}

		private boolean addSelectDate(String attributeCode, Date value) {
			// (!) NULL-�������� �� ��������� ...
			if (value == null) return false;
			this.sqlBuf.append( MessageFormat.format(SELECT_DATE_VALUE_1_date, attributeCode));
			addOrderMarker();
			this.addArg( value, Types.TIMESTAMP);
			return true;
		}

		private boolean addSelectBinary(String attributeCode, String value)
			throws DataException
		{
			// (!) ����� NULL-�������� �����������
			if (value == null || value.length() == 0) 
				return false; 
			this.sqlBuf.append( MessageFormat.format(SELECT_LONG_BINARY_VALUE_1_bytes, attributeCode));
			addOrderMarker();
			try {
				final byte[] sqlVal = (value == null) ? null : value.getBytes("UTF-8");
				this.addArg( sqlVal, Types.BINARY);
			} catch (UnsupportedEncodingException e) {
				logger.error( "fail to get as bytes string '"+ value+ "'", e);
				throw new DataException("convertion problem for "+ attributeCode, e);
			}
			return true;
		}

		/**
		 * @param id
		 * @param typeId
		 */
		private boolean addSelectLongAndValId(String attributeCode, Long id, Long typeId) {
			if (id == null) return false;
			if (typeId == null)
				return addSelectLong(attributeCode, id);

			this.sqlBuf.append( MessageFormat.format(SELECT_NUMBER_VALUE_AND_VALUE_ID_3, 
					attributeCode, String.valueOf(id), String.valueOf(typeId) ));
			addOrderMarker();
			// this.addArg( id, Types.NUMERIC);
			// this.addArg( typeId, Types.NUMERIC);
			return true;
		}
		
		/**
		 * @param attributeCode
		 * @param id
		 * @param date
		 */
		private boolean addSelectLongAndDate(String attributeCode, Long id, Date date) {
			if (id == null) return false;
			if (date == null)
				return addSelectLong(attributeCode, id);

			this.sqlBuf.append(MessageFormat.format(SELECT_NUMBER_VALUE_AND_DATE_VALUE,
					attributeCode, String.valueOf(id)));
			addOrderMarker();
			this.addArg(date, Types.TIMESTAMP);
			return true;
		}
		
		/**
		 * @param id
		 * @param typeId
		 * @param dateValue
		 */
		private boolean addSelectDatedTypedCardLink(String attributeCode, Long id, Long typeId, Date dateValue) {
			if (id == null) return false;
			if (typeId == null && dateValue == null)
				return addSelectLong(attributeCode, id);
			if (typeId == null)
				return addSelectLongAndDate(attributeCode, id, dateValue);
			if (dateValue == null)
				return addSelectLongAndValId(attributeCode, id, typeId);
			
			this.sqlBuf.append(MessageFormat.format(SELECT_NUMBER_VALUE_AND_DATE_VALUE_AND_VALUE_ID, 
					attributeCode, String.valueOf(id), String.valueOf(typeId)));
			addOrderMarker();
			this.addArg(dateValue, Types.TIMESTAMP);
			return true;
		}

		private int bufAttrOrder = 0; //  ������� ����������� ����� ��� ���������
		/**
		 * ��������� � �������� ����� this.sqlBuf ������ ���� ", NNN" ��� ��������
		 * �� �������� ����� �������������� ������� ���������� ������. 
		 */
		private void addOrderMarker() {
			this.sqlBuf.append("\t\t\t\t\t ,").append(++bufAttrOrder).append(" as attrOrder\n");
		}

	}

}
