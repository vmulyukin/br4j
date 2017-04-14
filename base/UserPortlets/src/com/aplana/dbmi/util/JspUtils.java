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
package com.aplana.dbmi.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.model.ObjectId;

/**
 * ��� �������� ������������� � JSP.
 * @author RAbdullin
 *
 */
public class JspUtils {

	private JspUtils() {
	}

	public static final String getNotNull( String s)
	{
		return getNotNull(s, "");
	}


	/**
	 * �������� �� NULL ��������.
	 * @param sValue ���������� ��������.
	 * @param sDefault �������� �� ��������� �� ������ sValue==null.
	 * @return �������� ������ (���� �������� sValue (���� ��� �� null), 
	 * ���� sDefault, ���� "")
	 */
	public static final String getNotNull( String sValue, String sDefault)
	{
		return ( sValue != null) ? sValue 
					: ( (sDefault != null) ? sDefault : "" );
	}


	public static final boolean equals( Object a, Object b)
	{
		return (a == b) || ( (a != null) && a.equals(b) );
	}


	public static String Date2Str(Date date) {
		return (date == null) ? null
				// : String.format("%1$td.%1$tm.%1$tY", new Object[]{ date });
				: String.format("%1$tY-%1$tm-%1$td", new Object[]{ date });
	}

	public static Date Str2Date(String dt)
	{
		if (dt == null) return null;
		// final SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");
		final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		Date result = null;
		try {
			result = fmt.parse(dt);
		 } catch (Exception ex) {
			 result = null;
		 }
		return result;
	}

	public static final String convertId2Str(ObjectId id, String ifnull)
	{
		return (id == null) ? ifnull : id.getId().toString();	
	}

	public static final String convertId2Str(ObjectId id)
	{
		return convertId2Str(id, "");
	}

	public static final long convertStr2IdLong(String sid)
	{
		return (sid == null || sid.length() < 1)
					? 0
					: Long.parseLong(sid);
	}

	public static boolean getChkBoxValue(String value) {
		return ( (value != null) 
					&& (
							"true".equalsIgnoreCase(value)
							|| "on".equalsIgnoreCase(value)
							|| "1".equalsIgnoreCase(value)
							|| "y".equalsIgnoreCase(value)
							|| "yes".equalsIgnoreCase(value)
							|| "�".equalsIgnoreCase(value)
							|| "��".equalsIgnoreCase(value)
					));
	}
	
	@SuppressWarnings("unchecked")
	public static void removeParameters(ActionRequest request, ActionResponse response, String... params) {
		final Map rParams = (request.getParameterMap() == null) 
			? new HashMap()
			: new HashMap(request.getParameterMap());
		if (params != null && !rParams.isEmpty()) {
			for(String parameter : params)
				rParams.remove(parameter);
		}
		response.setRenderParameters(rParams);
	}

}
