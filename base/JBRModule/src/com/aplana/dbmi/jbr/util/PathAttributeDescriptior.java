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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;

public class PathAttributeDescriptior {
	public static String REG_ATTR_SEPARATOR = "@";
	private Integer maxLength = null; // ���������� �������� ������� �����
										// �������
	private List<ObjectId> attrIds; // ����� ���������. ��� ����� ����������
									// ���������, ��������� �������� �������
									// ��������

	private String delimiter = "; ";// �����������

	public PathAttributeDescriptior(String reference) {
		setOriginalRef(reference);
	}

	public PathAttributeDescriptior(ObjectId attrId) {
		attrIds = new ArrayList<ObjectId>(1);
		attrIds.add(attrId);
	}

	public void setOriginalRef(String s) {
		if (s == null)
			return;

		/*
		 * int position = s.indexOf("#delim"); 
		 * if (s.contains("#delim=") &  position>=0){ 
		 * String tmp=s.substring(position, s.length());
		 * delimiter=tmp.replace("#delim='\\\\", "").replace("'", "");
		 * s=s.substring(0,position); }
		 * 
		 * // ��������� �������� ����� int posLength = s.indexOf('#');
		 * 
		 * if (posLength >= 0) { try { maxLength =
		 * Integer.valueOf(s.substring(posLength+1).trim()); } catch (Exception
		 * e) { maxLength = null; } s = s.substring(0, posLength); }
		 */

		if (s.contains("#"))
			s = ParseCell(s);

		// ������ ���������
		final String[] ids = s.split(REG_ATTR_SEPARATOR);
		attrIds = new ArrayList<ObjectId>(ids.length);
		for (String id : ids) {
			attrIds.add(getAttrId(id));
		}

	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(Integer newMaxLength) {
		this.maxLength = newMaxLength;
	}

	// id ::= [type:]code
	private ObjectId getAttrId(String id) {
		ObjectId result = null;

		String code, type;
		int posSep = id.indexOf(":");
		code = id.substring(posSep + 1).trim();
		type = posSep > 0 ? id.substring(0, posSep).trim() : null;

		// ���������� �������������, ��� ��������� ���� ����� ���
		// TypedCardLinkAttribute
		if (code.equals("_CARDTYPE")) {
			result = new ObjectId(ReferenceValue.class, "_CARDTYPE");
		} else if (type != null) {
			result = ObjectIdUtils.getObjectId(AttrUtils.getAttrClass(type),
					code, false);
		} else {
			result = IdUtils.tryFindPredefinedObjectId(code);
		}

		return result;
	}

	public List<ObjectId> getAttrIds() {
		return attrIds;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String newDelimiter) {
		delimiter = newDelimiter;
	}

	// ������� ������� ����������
	// ����� �������� ����� ������...
	final static String strptrn = "#[0-9]+|#delim='.*'";
	final static Pattern pattern = Pattern.compile(strptrn);

	/*
	 * ���������� ������ str, ������ �� �� �����, ������� ������ �������.
	 * @param str ������ � �����.
	 * ������: 
	 *		str �� �����: "link: jbr.ThemeOfQuery@NAME#40#delim='\\, '" 
	 * 		���������� ��� ���������: #40#delim='\\; '
	 *		result = ""link: jbr.ThemeOfQuery@NAME"
	 */ 
	private String ParseCell(final String str) {
		final int position = str.indexOf("#");
		if (position < 0) return str;

		final Matcher matcher = pattern.matcher(str);

		while (matcher.find()) {
			final String tmp = matcher.group();
			if (tmp.contains("delim")) {
				setDelimiter( tmp.replace("#delim='\\\\", "").replace("'", ""));
			} else {
				try {
					setMaxLength( Integer.parseInt(tmp.replace("#", "")) );
				} catch (Exception e) {
					setMaxLength( null);
				}
			}
		}
		return str.substring(0, position);
	}
}
