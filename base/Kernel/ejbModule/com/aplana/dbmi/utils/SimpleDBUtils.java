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

import com.aplana.dbmi.model.ObjectId;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * @author RAbdullin
 *	Some useful database procs.
 */
public abstract class SimpleDBUtils {
	
	/**
	 * ��������� SQL ������ ��� ��������� ���-�� ������� � �������.
	 * @param jdbc
	 * @param tableName
	 * @return the number of rows in the table or (-1) if @param(jdbc) is incorrect.
	 */
	public static int sqlGetTableRowsCount( JdbcTemplate jdbc, String tableName)
	{
		if (jdbc == null) 
			return -1;
		final String sql 
			= String.format( "SELECT COUNT(1) FROM %s", tableName);
		return jdbc.queryForInt(sql);
	}
	
	public static String getClobAsStr(final ResultSet rs, int fldNum) 
		throws SQLException
	{
		if (rs == null) return null;
		return rs.getString(fldNum);
	}

	public static String getClobAsStr(final ResultSet rs, final String fldName) 
		throws SQLException
	{
		if (rs == null) return null;
		return rs.getString(fldName);
	}

	/**
	 * ������������� ������ ���� � ������, ������ ��� ����� ���� � ��������� ���������.
	 * @param rawdata: ����-������ ������.
	 * @param codebase: ��������� ����-������.
	 * @return java-������, ��������� �� rawdata/codebase.
	 * @throws UnsupportedEncodingException 
	 */
	private static String newString( final byte[] rawdata, final String codebase) 
		throws UnsupportedEncodingException
	{
		if (rawdata == null || rawdata.length < 1) 
			return "";
		return new String( rawdata, codebase);
	}


	/**
	 * �������� ������ blob ���� � ���� ������ � ��������� ���������.
	 * @param rs �������� ����� ������ (��)
	 * @param fldNum ����� ���� (�� 1) � ��
	 * @param codebase ���������� ������ � blob'�
	 * @return ������, ���������� �� ���������� blob-���� � ������ ���������
	 * @throws SQLException
	 * @throws UnsupportedEncodingException
	 */
	public static String getBlobAsStr(final ResultSet rs, int fldNum, 
			final String codebase)
		throws SQLException, UnsupportedEncodingException
	{
		return (rs == null) ? null : newString( rs.getBytes(fldNum), codebase);
	}
	
	/**
	 * �������� ������ �� blob'� � �������� UTF-8
	 * @param rs �������� ����� ������ (��)
	 * @param fldNum ����� ���� (�� 1) � ��
	 * @return ������, ���������� �� ���������� blob-���� � ������ ���������
	 * @throws SQLException
	 * @throws UnsupportedEncodingException
	 */
	public static String getBlobAsStr(final ResultSet rs, int fldNum)
		throws SQLException, UnsupportedEncodingException
	{
		return getBlobAsStr(rs, fldNum, "UTF-8");
	}
	
	public static String getBlobAsStr(final ResultSet rs, final String fldName, 
			final String codebase)
		throws SQLException, UnsupportedEncodingException
	{
		return (rs == null) ? null : newString( rs.getBytes(fldName), codebase);
	}
	
	public static String getBlobAsStr(final ResultSet rs, final String fldName)
		throws SQLException, UnsupportedEncodingException
	{
		return getBlobAsStr(rs, fldName, "UTF-8");
	}


	/**
	 * Converts collection of string-valued {@link ObjectId} instances to comma-separated
	 * string consisted of single-quoted string literals with proper SQL escaping. 
	 * @param objectIds collection of string-valued {@link ObjectId} instances
	 * @return string ready to be inlined in SQL script or empty string if objectIds is empty
	 * @throws NullPointerException if objectIds is null
	 * @throws ClassCastException if objectIds contains non-string identifiers
	 */
	public static String stringIdentifiersToCommaSeparatedSqlString( 
			final Collection<?> objectIds) 
	{
		final StringBuilder result = new StringBuilder();
		for (Iterator<?> i = objectIds.iterator(); i.hasNext();) {
			final ObjectId id = (ObjectId) i.next();
			if (id == null || id.getId() == null) continue;
			String stId;
			if (id.getId() instanceof Long){ 
				stId = String.valueOf(id.getId());
				result.append(stId);
			}else{
				stId = StringEscapeUtils.escapeSql((String)id.getId());
				result.append('\'').append(stId).append('\'');
			}
			if (i.hasNext()) {
				result.append(',');
			}
		}
		return result.toString();
	}
	
	
	/**
	 * Converts collection of string-valued {@link ObjectId} instances to comma-separated
	 * string consisted of double-quoted string literals with proper SQL escaping.
	 * @param objectIds collection of string-valued {@link ObjectId} instances
	 * @return string ready to be inlined in SQL script or empty string if objectIds is empty
	 * @throws NullPointerException if objectIds is null
	 * @throws ClassCastException if objectIds contains non-string identifiers
	 */
	public static String stringIdentifiersToCommaSeparatedDoubleQuotedSqlString( 
			final Collection<?> objectIds) {
		final StringBuilder result = new StringBuilder();
		for (Iterator<?> i = objectIds.iterator(); i.hasNext();) {
			final ObjectId id = (ObjectId) i.next();
			if (id == null || id.getId() == null) continue;
			String stId;
			if (id.getId() instanceof Long){ 
				stId = String.valueOf( id.getId() );
				result.append(stId);
			}else{
				stId = StringEscapeUtils.escapeSql((String)id.getId());
				result.append('\'').append('\'').append(stId).append('\'').append('\'');
			}
			if (i.hasNext()) {
				result.append(',');
			}
		}
		return result.toString();
	}

	/**
	 * ����������� ������ ��������� �������� � ���� sql-������, ������� ��� 
	 * ������������� � SQL-���������� ���� "A in ('k1', 'k2', ...)".
	 * (�.�. ����� ����������� � ��������� �������� ����� �������) 
	 * @param strValues: ������ �����.
	 * @return
	 */
	public static String getAsSqlStrList(Collection<String> strValues) { 
		final StringBuilder buf = new StringBuilder();
		for (final Iterator<String> iterator = strValues.iterator(); iterator.hasNext();) {
			final String item = iterator.next();
			buf.append("'")
				.append( StringEscapeUtils.escapeSql(item))
				.append("'");
			if (iterator.hasNext())
				buf.append(',');
			
		}
		return buf.toString();
	}

	/**
	 * ����������� ������ Long �������� � ���� sql-������, ������� ��� 
	 * ������������� � SQL-���������� ���� "A in (x1, x2, ...)".
	 * (�������� ����������� ����� �������) 
	 * @param listOfLong: ������ Long.
	 * @return
	 */
	public static String getAsSqlIdList(Collection<Long> listOfLong) { 
		final StringBuilder buf = new StringBuilder();
		for (final Iterator<Long> iterator = listOfLong.iterator(); iterator.hasNext();) {
			final Long item = iterator.next();
			buf.append( String.valueOf(item.longValue()) );
			if (iterator.hasNext())
				buf.append(',');
			
		}
		return buf.toString();
	}
	
	/**
	 * ������������ ������� ��� ���������� X �� �������������� ��������� [a, b].
	 * (����� ����������� $arg == inlineArg, $a == inlineA, $b == inlineB)
	 * �� ���������� �������:
	 * 		���� (a == null):
	 * 			(b == null): return "";
	 * 			(b != null): return return "($arg <= $b)";
	 * 		����� // ��� (a != null):
	 * 			���� (b == null):  return "($arg >= $a)";
	 * 			// ����� ��� ��� ������� �� null
	 * 			���� (a == b):  return "($arg = $a)";
	 * 			����� : return "( ($arg >= $a) and ($arg <= $b) )";
	 * 
	 * @param a: ������, ��������������� ������� a;
	 * @param b: ������, ��������������� ������� b;
	 * 
	 * @param inlineArg: �������� ��������� ��� ������� � ���������;
	 * 
	 * @param inlineA: �������� ������� a ��� ������� � ��������� (���� 
	 * ��������� ������� ����������� - � ��������). ��� sql-������� ����� ����� 
	 * ����� ������������ a.toString(), �.�. ��������, ��� typeof(a)==Long ����� 
	 * �������� ���������� ����������� ����� ���� (!?), � ��� ����� ���� 
	 * �������� (���������) �������.
	 * 
	 * @param inlineB: ���� ��� � �, �� ��� ������� b.
	 * 
	 * @return ������ ��������������� ������� (����� �������� ��� ������� � sql 
	 * ������, ���� ��������� ������� inlineXXX). ���� ��� ������� NULL, �.�. 
	 * (a == null == b), �� ������������ "". 
	 */
	public static String mkCondInsideInterval( Object a, Object b, 
			String inlineArg, String inlineA, String inlineB)
	{
		if (a == null) {
			if (b == null) return "";
			// "($arg <= $b)";
			return String.format( "(%s <= %s)", inlineArg, inlineB);
		}

		if (b == null) // "($arg >= $a)" 
			return String.format( "(%s >= %s)", inlineArg, inlineA);

		// ����� ��� ��� a � b �� null
		if (a.equals(b) )  //  ������ a � b ������ ������� ����������
			// "($arg = $a)"
			return String.format( "(%s = %s)", inlineArg, inlineA);
		// "( ($arg >= $a) and ($arg <= $b) )"
		return String.format( "( (%s >= %s) and (%s <= %s) )", inlineArg, inlineA, inlineArg, inlineB);
	}

	/**
	 * ������������ ����� ������ ��� ��������������.
	 */
	final static int MAXLINELEN = 4096;

	/**
	 * ������������ ������������� xml-������� � ������� ������� � ������������� 
	 * �����������.
	 * @param sqlText ����� �������
	 * @param args ��������� �������
	 * @param types ���� ���������� ������� 
	 * (!) ���������� ��������� args � types ������ ���������
	 * @return ������ � xml-��������� �������� �������
	 */
	public static String getSqlQueryInfo(final String sqlText, final Object[] args,
			final int[] types)
	{
		final StringBuilder buf = new StringBuilder();
		try {
			buf.append( "\n<dynamic-query>\n" ); // (0)
			try {
				
				buf.append( "\t<sql>\n" ); // (1*)
				try {
					buf.append( "\t\t<![CDATA[\n" ); // (1**)
					try {	
						buf.append( sqlText );
					} finally {
						buf.append( "\n\t\t]]>\n"); // (1**)
					}
				} finally {
					buf.append( "\t</sql>\n"); // (1*)
				}

				/* ����� ���������� */
				buf.append( MessageFormat.format( "\t<args count=\"{0}\">\n",
						new Object[] { 
							( (args == null) ? "null" : String.valueOf(args.length) )
						})); // (2*)
				try {
					// <param name="userId" display-name="User id" type="integer" />
					if ( (args != null) && (args.length > 0) )
					{
						final int typesLen = (types != null) ? types.length : 0;
						for( int i = 0; i < args.length; i++)
						{
							final String argSqlType = ( types != null && (i < typesLen))
									? getSqlTypeName(types[i])
									: "NULL (array argTypes[] too small)";
							
							final Object argJavaType = (args[i] != null)
									? args[i].getClass().getName()
									: "NULL";
									
							try {
								buf.append( MessageFormat.format(
										"\t\t<argument order_0based=\"{0}\" sqltype=\"{1}\" javatype=\"{2}\">\n", 
										new Object[] { 
												i,
												argSqlType,
												argJavaType
												} )); // (2**)
					
								if ( args[i] == null)
								{	// null value
									buf.append("\t\t\t<NULL/>\n");
								} else {
									final String argValue = 
										StringEscapeUtils.escapeXml(
												MessageFormat.format( "{0}", String.valueOf(args[i]))
										);

									if (argValue == null) {
										buf.append("\t\t\t<value NULL />\n");
									} else if ( argValue.length() <= 127 
											&& (argValue.indexOf('\n') < 0)
											&& !(
												(types[i] == Types.CHAR)
												|| (types[i] == Types.VARCHAR)
												|| (types[i] == Types.LONGVARCHAR)
											)
										) 
									{
										// �������� �������
										buf.append( MessageFormat.format( 
												"\t\t\t<value toString=\"{0}\" />\n", 
												new Object[] {argValue} 
										));
									} else {
										// ������� �������
										buf.append( MessageFormat.format( 
												"\t\t\t<value len=\"{0}\">\n", 
												new Object[] { String.valueOf(argValue.length()) } 
										)); // (2***)
										if (argValue.length() > 0) {
											try {
												buf.append( "\t\t\t\t<![CDATA[\n" ); // (2****)
												try {	
													buf.append( boundLen( argValue, MAXLINELEN) );
												} finally {
													buf.append( "\n\t\t\t\t]]>\n"); // (2****)
												}
											} finally {
												buf.append( "\t\t\t</value>\n"); //  (2***) 
											}
										}
										if (args[i] != null && argValue.length() > 0) {
											buf.append( MessageFormat.format( 
													"\t\t\t<val_as_str len=\"{0}\">\n", 
													new Object[] { String.valueOf(argValue.length()) } 
											)); // (2***)
											try {
												buf.append( "\t\t\t\t<![CDATA[\n>>>\n" ); // (2****)
												try {	
													buf.append( boundLen( String.valueOf(args[i]), MAXLINELEN) );
												} finally {
													buf.append( "\n<<<\t\t\t\t]]>\n"); // (2****)
												}
											} finally {
												buf.append( "\t\t\t</val_as_str>\n"); //  (2***) 
											}
										}
									}
								}
							} catch (Exception e) {
								buf.append( MessageFormat.format(
										"\t\t\t<arg.exception>\n" +
										"\t\t\t\t<![CDATA[\n" +
										"{0}" +
										"\t\t\t\t]]>\n" + 		// end CDATA
										"\t\t\t</arg.exception>\n"	// end exception
										, new Object[] {e} )); // (2**)
							} finally {
								buf.append( "\t\t</argument>\n" ); // (2**)
							}
						}
					} 
				} finally {
					buf.append( "\t</args>\n"); // (2*)
				}

			} finally {
				buf.append( "</dynamic-query>\n" ); // (0)
			}

		} catch (Exception e) {
			e.printStackTrace();
			// buf.append("\n Exception building xml sql info : " + e.getMessage());
			buf.append( MessageFormat.format(
					"\t\t\t<exception>\n" +
					"\t\t\t\t<![CDATA[\n" +
					"{0}" +
					"\t\t\t\t]]>\n" + 		// end CDATA
					"\t\t\t</exception>\n"	// end exception
					, new Object[] {e} )); // (2**)
		}
		return buf.toString();
	}

	/**
	 * @param value
	 * @param maxLen
	 * @return ������ value � ������ �� ����� maxLen
	 */
	private static Object boundLen(String value, int maxLen) {
		if ( maxLen < 3 || value == null || value.length() <= maxLen)
			return value;
		return value.substring(0, maxLen - 3) + "...";
	}

	public static void makeInfoColumns(ResultSetMetaData metaData, StringBuffer dstBuf)
	throws SQLException 
{
	if (metaData == null || dstBuf == null) return;
	// column names and type...
	dstBuf.append( String.format("\nDataset has %d columns\n", metaData.getColumnCount()));
	for (int i = 1; i <= metaData.getColumnCount(); i++) {
		final String colName = metaData.getColumnLabel(i);
		final String colType = metaData.getColumnTypeName(i);
		final int colLen = metaData.getColumnDisplaySize(i);
		final int colDec = metaData.getPrecision(i);
		dstBuf.append( String.format("\t[%d] %s:%s[%d.%d]\n", 
				i, colName, colType, colLen, colDec));
	}
}

	/**
	 * ������� � ����� ������ �� �� ans � ���������� �� ����� maxRows �����.
	 * ���� maxRows < 0, �� ��� ������.
	 */
	public static int makeInfoDataSet(final ResultSet ans, StringBuffer dstBuf, int maxRows) 
		throws SQLException 
	{
		if (dstBuf == null) return (-1);
		dstBuf.append("---------------------------------------------\n");
		if (ans == null) {
			dstBuf.append("\ndataset is null\n");
			dstBuf.append("---------------------------------------------\n");
			return (-1);
		}

		int iRows = 0;
		while (ans.next())
		{
			++iRows;
			if (maxRows >= 0 && iRows > maxRows) {
				dstBuf.append( String.format("\n\t ... %d rows exceeded -> breaking \n", maxRows));
				break;
			}
			try {
				dstBuf.append( String.format("\t[%2d]", iRows));
				for (int i = 1; i <= ans.getMetaData().getColumnCount(); i++) 
				{
					final String s = ans.getString(i);
					dstBuf.append( String.format("\t\'%s\'", new Object[]{s}));
				}
				dstBuf.append("\n");
			} catch (SQLException ex) {
				dstBuf.append(ex);
			}
		} // while
		if (iRows == 0)
			dstBuf.append("\t EMPTY\n");
		dstBuf.append("---------------------------------------------\n");
		return iRows;
	}

	/**
	 * @param typeCode: ��� ���� ��. (@See: java.sql.Types)
	 * @return ��������� �������� ����
	 */
	public static String getSqlTypeName( /*java.sql.Types*/ int typeCode )
	{
		switch(typeCode) {
			case Types.BIT:			return "bit";
			case Types.TINYINT:		return "tinyInt";
			case Types.SMALLINT:	return "Smallint";

			case Types.INTEGER: 	return "Integer";
			case Types.BIGINT: 		return "BigInt"; 
			case Types.FLOAT: 		return "Float"; 

			case Types.REAL: 		return "Real"; 
			case Types.DOUBLE: 		return "Double";
			case Types.NUMERIC: 	return "Numeric";

			case Types.DECIMAL: 	return "Decimal";
			case Types.CHAR: 		return "Char";
			case Types.VARCHAR: 	return "VarChar";

			case Types.LONGVARCHAR: return "LobgVarChar";
			case Types.DATE: 		return "Date";
			case Types.TIME: 		return "Time";

			case Types.TIMESTAMP: 	return "TimeStamp";
			case Types.BINARY: 		return "Binary";
			case Types.VARBINARY: 	return "VarBinary";

			case Types.LONGVARBINARY: 	return "LongVarBinary";
			case Types.NULL: 		return "NULL";
			case Types.OTHER: 		return "Other";

			case Types.JAVA_OBJECT: return "Java_Object";
			case Types.DISTINCT: 	return "Distinct";
			case Types.STRUCT: 		return "Struct";

			case Types.ARRAY: 		return "Array";
			case Types.BLOB: 		return "Blob";
			case Types.CLOB: 		return "Clob";

			case Types.REF: 		return "Ref";
			case Types.DATALINK: 	return "Datalink";
			case Types.BOOLEAN: 	return "Boolean";

			default:
				return String.format("%s(%d)", Types.class, typeCode);
		} // switch
	}
	
	/**
	 * @param types: ������ <Integer> � ������ sql-����� (���������� ��� ������).
	 * @return ������ int[] ������ ������ List<Integer>.
	 */
	public static int[] makeTypes(Collection<Integer> types) {
		if (types == null)
			return null;
		final int[] result = new int[types.size()];
		int i = 0;
		for (Integer item : types) {
			result[i++] = item;
		}
		return result;
	}

	public static java.sql.Date sqlDate(Date date) {
		if (date == null)
			return null;
		return new java.sql.Date(date.getTime());
	}

	public static java.sql.Timestamp sqlTimestamp(Date date) {
		if (date == null)
			return null;
		return new java.sql.Timestamp(date.getTime());
	}
}
