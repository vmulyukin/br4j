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

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringEscapeUtils;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.Search.SearchTag;
import com.aplana.dbmi.model.DataObject;

/**
 * @author RAbdullin
 *	Some string utility procs.
 */
public class StrUtils {
	public static final String WILDCARD_CHARACTER = "*";
	
	/**
	 * Check if string is empty or null.
	 * @param s
	 * @return true, if string is null or empty.
	 */
	public static boolean isStringEmpty(final String s)
	{
		return (s == null) || (s.length() == 0);
	}
	
	/**
	 * Get value, replacing empty one it by default value.
	 * @param val
	 * @param defIfEmpty
	 * @return @param(val) if it is not empty, otherwise @param(defIfEmpty).
	 */
	public static String nvl( String val, String defIfEmpty)
	{
		return isStringEmpty(val) ? defIfEmpty : val;
	}
	
	/**
	 * Make string enumeration of the items as list with delimiters.  
	 * @param col
	 * @param delimiter
	 * @param quoteOpen ����������� �������.
	 * @param quoteClose ����������� �������.
	 * @return
	 */
	public static String getAsString( final Collection<?> col, 
			final String delimiter, String quoteOpen, String quoteClose)
	{
		if (col == null)
			return null;
		if (quoteOpen == null) quoteOpen = "";
		if (quoteClose == null) quoteClose = "";
		final StringBuffer result = new StringBuffer(5);
		final Iterator<?> itr = col.iterator();
		// final String fmtStr = (isStringEmpty(quote)) ? "{1}" : "{0}{1}{2}";
		while (itr.hasNext()) {
			final Object item = itr.next();

			String strItem;
			if (item instanceof DataObject) {
				strItem = ((DataObject) item).getId().getId().toString();
			} else {
				strItem = (item != null) ? item.toString() : "" ;
			}

			result.append(quoteOpen).append(strItem).append(quoteClose);
			if (delimiter != null && itr.hasNext()) {
				result.append(delimiter);
			}
		}
		return result.toString();
	}

	/**
	 * Make string enumeration of the items as list with delimiters.  
	 * @param col
	 * @param delimiter
	 * @param quote ������������-������� ��������� ���������.
	 * @return
	 */
	public static String getAsString( final Collection<?> col, 
			final String delimiter, final String quote)
	{
		return getAsString( col, delimiter, quote, quote);
	}
	
	/**
	 * ������� ������ � ������������ ��� �������������-�������.
	 * @param coll
	 * @param delimiter
	 * @return
	 */
	public static String getAsString(Collection<?> col, String delimiter)
	{
		return getAsString( col, delimiter, null);
	}

	/**
	 * ������� ������ � ������������ �������.
	 * @param coll
	 * @return
	 */
	public static String getAsString(Collection<?> col)
	{
		return getAsString( col, ", ");
	}

	/**
	 * �������������� ������ � boolean � ������ ��������� ������������� true 
	 * (��� ����� �������� � ���������� �������� � ������ � � �����). 
	 * @param value: �������� ��� ��������������.
	 * @param defaultValue: ��������, ���� value==null ��� �����.
	 * @return
	 */
	public static boolean stringToBool( String value, final boolean defaultValue)
	{
		if (value != null) value = value.trim();		
		return (value == null || value.length() == 0) 
				? defaultValue 
				: ( 	value.equalsIgnoreCase("true")
						||	value.equalsIgnoreCase("1")
						||	value.equalsIgnoreCase("+")
						||	value.equalsIgnoreCase("y")
						||	value.equalsIgnoreCase("yes")
						||	value.equalsIgnoreCase("�")
						||	value.equalsIgnoreCase("��")
				)
				; 
	}

	/** ������� ������ � ��������.
	 * @param st
	 * @param openQuote ����������� ������
	 * @param closeQuote ����������� ������
	 * @return
	public static String inQuotes( final String st, final String openQuote, 
			final String closeQuote) {
		return MessageFormat.format( "{0}{1}{2}", new Object[] { openQuote, st, closeQuote } );
	}
	 */

	/**
	 * ������� ������ � ������������ ��������.
	 * @see inQuotes
	 * @param s
	 * @param quote
	 * @return
	 */
	public static String inQuotes( final String s, final char quote) {
		// return inQuotes(s, quote, quote);
		if (s == null)
			return null;

		if ( (s.length() >= 1) && (s.charAt(0) == quote) )
			// ������ ��� ��������
			return s;
		
		// ��������� �������...
		final StringBuffer result = new StringBuffer(); // �������
		result.append(quote); // �������
		for (int i = 0; i < s.length(); i++)
		{
			char ch = s.charAt(i);
			if (ch == '\\') {	
				// ����������� ���� �������� - '\' � �� ���
				result.append(ch);
				if (++i >= s.length()) break; // for i 
				ch = s.charAt(i);
			} else if (ch == quote) {
				// ��������� ��� ������� ...
				result.append(ch);
			}
			result.append(ch);
		}
		result.append(quote); // �������
		return result.toString();
	}

	/**
	 * �������� ������ � ������� �������, � ���������� ���������� ���������
	 * ������� � ��� "\z".
	 * @param s
	 * @return
	 */
	public static String inQuotes(String s) {
		return inQuotes(s, '"');
	}
	
	/**
	 * ������ �� ������ �������:
	 * 		- ��������� ������ quote ������ ������;
	 * 		- ������ quote - �������� ���� quote �� ����;
	 * 		- "\zzz" ��������� ��� ���� (�.�. '\<qoute>' ����������).
	 * 
	 * (!) ������ �������� � ������ s ������ ���� �������, ����� ������ �� ����������� �� ���������.
	 * 
	 * @param s: ������ ��� �������� �������;
	 * @param quote: ������ �������;
	 * 
	 * @return ������ ��� �������.
	 */
	public static String deQuotes(String s, char quote) {
		if (s == null)
			return null;
		
		if ( (s.length() < 1) || (s.charAt(0) != quote))
			return s;
		
		final StringBuffer result = new StringBuffer();
		// ����� ���������� ������ �������...
		for (int i = 1; i < s.length(); i++)
		{
			char ch = s.charAt(i);
			if (ch == '\\') {	
				// ����������� ���� �������� - '\' � �� ���
				result.append(ch);
				if (++i >= s.length()) break; // for i 
				ch = s.charAt(i);
			} else if (ch == quote) {
				// ��� ������� � ����� ������ ����������...
				if (++i >= s.length()) break; // for i 
				// � ��������� ������ ����� ���� ��������...
				ch = s.charAt(i);
			}
			result.append(ch);
		}
		return result.toString();
	}

	/**
	 * ������ ������� �������.
	 * @param s
	 * @return
	 */
	public static String deQuotes(String s) {
		return deQuotes( s, '"' );
	}
	
	public static String convertFileNameToDeloFormat(String fileName){
		final int module = 100000000;
    	String t[] = fileName.split("\\.");
    	if (t.length > 1) fileName = fileName.substring(0, fileName.length() - t[t.length - 1].length() - 1);
    	NumberFormat format = NumberFormat.getIntegerInstance();
    	format.setMaximumIntegerDigits(8);
    	format.setMinimumIntegerDigits(8);
    	format.setGroupingUsed(false);   	   	
    	int hashCode = fileName.hashCode();
    	return format.format(hashCode < 0 ? module + hashCode % module : hashCode % module) + (t.length > 1 ? "." + t[t.length - 1] : "");
	}
	
	/**
	 * ������������� ������, ��������� �������������, ��� ������������� �� � �������� ��������� ��� LIKE
	 * ��� �������, ����� �� �����-�� �������� �� ������������ {@link java.sql.PreparedStatement}
	 * <br/><b>�� ���������� ��������� �������</b>
	 * @param string ��������� ������������� ������
	 * @return
	 */
	public static String escapeSpecialCharactersForLikeClause(String string){
		return string == null ? null : string.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}
	
	/**
	 * �������� ������ ��� LIKE ������ ���������� ��
	 * ����� escapeSpecialCharactersForLikeClause
	 * @param string ��������� ������������� ������
	 * @return
	 */
	public static String wrapStringForLike(String string){
		String res = null;
		if (string != null) {
			String begin = "%";
			String end   = "%";
			if (string.contains(WILDCARD_CHARACTER)) {
				begin = end = "";
			}
			res = (begin + escapeSpecialCharactersForLikeClause(string).replaceAll("\\"+WILDCARD_CHARACTER, "%") + end).replaceAll("([^\\\\])%{2,}", "$1%");
		}
		return res;
	}
	
	/**
	 * ������������� ������ ����� � ������, ���������� �������� ������ � ��������� ��������, ����������� ��������.
	 * ��������� ������� ������ ����� ����� ������������.
	 * @param strings ������ �����
	 * @return
	 */
	public static String buildSqlStringList(Collection<String> strings){
		StringBuilder builder = new StringBuilder();
		for(Iterator<String> iterator = strings.iterator(); iterator.hasNext();){
			builder.append("'").append(StringEscapeUtils.escapeSql(iterator.next())).append("'");
			if(iterator.hasNext()) builder.append(", ");
		}
		return builder.toString();
	}
	
	public static Search.SearchTag findSearchTag(String nvlWords) {
		Search.SearchTag tag = SearchTag.NO_TAG;
		if (nvlWords != null) {
			//���� ������������ ��� � ��������� ������
			if (nvlWords.startsWith(Search.SearchTag.TAG_SEARCH.toString())) {
				if (nvlWords.startsWith(Search.SearchTag.TAG_SEARCH_REGNUM.toString())) {
					tag = SearchTag.TAG_SEARCH_REGNUM;
				} else if (nvlWords.startsWith(Search.SearchTag.TAG_SEARCH_TWO_REGNUM.toString())) {
					tag = SearchTag.TAG_SEARCH_TWO_REGNUM;
				} else if (nvlWords.startsWith(Search.SearchTag.TAG_SEARCH_FULL_TEXT.toString())) {
					tag = SearchTag.TAG_SEARCH_FULL_TEXT;
				} else if (nvlWords.startsWith(Search.SearchTag.TAG_SEARCH_ID.toString())) {
					tag = SearchTag.TAG_SEARCH_ID;
				}
			}
		}
		if (tag == SearchTag.NO_TAG && nvlWords.matches("[0-9]+")) {
			tag = SearchTag.TAG_SEARCH_ID;
		}
		return tag;
	}
	
	public static String untagWords(String nvlWords) {
		Search.SearchTag tag = findSearchTag(nvlWords);
		if (tag == SearchTag.NO_TAG) {
			return nvlWords;
		} else {
			if (nvlWords.startsWith(tag.toString())) {
				return nvlWords.substring(tag.toString().length());
			}
			return nvlWords;
		}
	}
}
