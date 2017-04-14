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
package com.aplana.dbmi.storage.utils;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * @author RAbdullin (2010/01)
 * �������������� � �������� � ������� ��� �� � ���� ����.
 * ������������� ��� �������� ASCII-7 ���������� � Linux.
 * ������ ������:
 *	��������		strEncode
 *	'simple' 		'simple' 
 *	'normal name' 	'normal name'
 *	'�������' 		'ru@_r__u__s__s__k__o__e_'
 *	'������� ����'	'ru@_r__u__s__s__k__o__e_ _n__a__z__v_'
 *	'�������_mixed'	'ru@_r__u__s__s__k__o__e____mixed'
 */
public class TranslitConvertor {
	
	// ����������� ��������� ��������������� ���������� ������� (����������).
	// ������������ � �� � ����� �������.
	final public static char CHAR_DELIMITER = '_';
	
	final public static String PRFX_TRN = "ru@"; // ������� ������������������� ������ (� ��������� ��������)
	
	final static String RU_CHARS = 
		"��������������������������������" +
		"�����Ũ��������������������������"
		; 

	/**
	 * ���������, ���� �� � ������ ������ ����� ��� '_'.
	 * @param s
	 * @return
	 */
	public static boolean hasRuChars( String s)
	{
		if (s != null)
			for (int i = 0; i < s.length(); i++) {
				if (RU_CHARS.indexOf(s.charAt(i)) >=0)
					// ���� ������� �������...
					return true;
			}
		return false;
	}
	
	/**
	 * �������������� � �������� ������������� �����. �������� � strDecode.
	 * ���� � ������ ���� ������� �������, �� ��� ����� ����������������� � "_" + ������ ���������,
	 * ������ "_" ��������. �� ��-������� �������� ��� ���������.
	 * @param naturalStr
	 * @return ������ ���� �� �������� � �������� - ��� ���������, 
	 * ����� ������������������� ������, � ������ ��������� "ru@".
	 */
	public static String strEncode(String naturalStr)
	{
		if (!hasRuChars(naturalStr))
			// �� ��������� ��������������
			return naturalStr;
		
		// -> � ��������...
		final StringBuffer buf = new StringBuffer();
		buf.append(PRFX_TRN); // (!) ���������� �������� ��������������
		for (int i = 0; i < naturalStr.length(); i++) {
			buf.append( charToLatin( naturalStr.charAt(i)));
		}
		return buf.toString();
	}
	
	/**
	 * �������� � strEncode ��������������.
	 * @param encodedStr ������ ������������ ��������,
	 * (!) ��������� ������������ �������-������� ����������� �������� '_'.
	 * @return ��������������� ������.
	 */
	public static String strDecode(String encodedStr)
	{
		if (encodedStr == null || "".equals(encodedStr)) return encodedStr;
		if (!encodedStr.startsWith(PRFX_TRN))
			// �� ���������� � ������� �������� -> ������� ��� ����
			return encodedStr;
	
		// �������� -> ������ �, ��������, �������� ���������...
		final StringBuffer bufDecoded = new StringBuffer();
		for (int i = PRFX_TRN.length(); i < encodedStr.length(); i++) {
			final char ch = encodedStr.charAt(i); 
			if (ch == CHAR_DELIMITER) {
				// �������� ������������������ "_abc_"
				// if (i + 2 >= encodedStr.length()) then break; // ��� �����
				int iend = encodedStr.indexOf(CHAR_DELIMITER, i + 2); // ������� �������� � ������� ������� ���������
				if (iend < 0) iend = encodedStr.length(); // �� ����� ������ ���� ��� ������������ "_" 
				bufDecoded.append( latinToChar( encodedStr.substring(i, iend+1)));
				i = iend;
			} else // ������� ������...
				bufDecoded.append(ch);
		}
		return bufDecoded.toString();
	}

	/**
	 * �������������� ������� � ��������.
	 * �����, ��� ����� � �� ��� ������� ���-�� ��� ���������, ��������� ������������� � ������������ ���������.
	 * @param ch
	 * @return �������� ��� �����: ru-����� � "_abc_" ������ ������� �������� ��� ���������.
	 */
	public static String charToLatin(char ch) {
		final String found = latinCode.get(ch);
		// ���� ������ ������� ��� -> ������� ����� ��� ������
		return (found != null) ? CHAR_DELIMITER + found + CHAR_DELIMITER 
							   : String.valueOf(ch);
	}
	
	/**
	 * �������������� ��������� ������ �������� � ru-������ ��� ����-�-���� ������.
	 * @param strOfChar
	 * @return �������� ��� �����.
	 */
	private static char latinToChar(String strOfChar) {
		if (strOfChar == null || strOfChar.length() == 0) 
			return '\000';

		if (strOfChar.length() == 1) // ���� ������������ - ������ ������ ���� ������...
			return strOfChar.charAt(0);
		
		// ����� ��� ��� ����� �������� ������� ����...
		if (strOfChar.length() < 3 
			|| (strOfChar.charAt(0) != CHAR_DELIMITER)
			|| (strOfChar.charAt(strOfChar.length()-1) != CHAR_DELIMITER)
			)
			throw new InvalidParameterException( String.format( "Incorrect translitera '%s'", strOfChar));
		
		// �������������� ����������� ...
		final Character found = latinDecode.get(strOfChar.substring(1, strOfChar.length()-1));
		// ���� ������ ������� ��� -> ��������� ���������� ...
		if (found == null) 
			throw new InvalidParameterException( String.format( "Invalid translitera '%s'", strOfChar));
		return found;
	}
	
	// ������������: ��� ����� -> ��������
	static HashMap<Character, String> 
		latinCode = doInit(); // new HashMap<Character, String>();
	// ������������: �������� -> ��� �����
	static HashMap<String, Character> latinDecode;
	
	static private boolean init = false;
	@SuppressWarnings("unchecked")
	static synchronized HashMap<Character, String> doInit() 
	{
		if (init) return latinCode;
		
		init = true;
		latinCode = new HashMap<Character, String>(); 
		latinDecode = new HashMap<String, Character>();
		
		latinCode.put(CHAR_DELIMITER, String.valueOf(CHAR_DELIMITER));
		
		latinCode.put('�', "a");
		latinCode.put('�', "b");
		latinCode.put('�', "v");

		latinCode.put('�', "g");
		latinCode.put('�', "d");
		latinCode.put('�', "e");

		latinCode.put('�', "yo");
		latinCode.put('�', "zh"); // "dj"
		latinCode.put('�', "z");

		latinCode.put('�', "i");
		latinCode.put('�', "k");
		latinCode.put('�', "l");

		latinCode.put('�', "m");
		latinCode.put('�', "n");
		latinCode.put('�', "o");

		latinCode.put('�', "p");
		latinCode.put('�', "r");
		latinCode.put('�', "s");

		latinCode.put('�', "t");
		latinCode.put('�', "u");
		latinCode.put('�', "f");
		
		latinCode.put('�', "kh");
		latinCode.put('�', "ts");
		latinCode.put('�', "ch");

		latinCode.put('�', "sh");
		latinCode.put('�', "shch");
		latinCode.put('�', "hd");
		
		latinCode.put('�', "ji");
		latinCode.put('�', "lt");
		latinCode.put('�', "ae");

		latinCode.put('�', "iu");
		latinCode.put('�', "ia");
		
		// ���������� ������ � ������� �������� (ru -> lat)
		final Entry<Character, String>[] arr =  
			latinCode.entrySet().toArray(new Entry[0]);
		for (final Entry<Character, String> item : arr) 
			latinCode.put( Character.toUpperCase(item.getKey()), item.getValue().toUpperCase());
		
		// ���������� �������� ��������������: lat -> ru
		for (final Iterator<Entry<Character, String>> iterator 
				= latinCode.entrySet().iterator(); iterator.hasNext();) 
		{
			final Entry<Character, String> item = iterator.next();
			latinDecode.put( item.getValue(), item.getKey());
		}
		
		return latinCode;
	}
}