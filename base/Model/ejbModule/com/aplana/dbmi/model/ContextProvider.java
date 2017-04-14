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
package com.aplana.dbmi.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Utility class used for internationalization purposes.
 * For each portal request stores request locale in ThreadLocal variable.
 * This information is used later in GUI to find out which language to use in labels and etc.
 * 
 */
public class ContextProvider
{
	/**
	 * Russian locale
	 */
	public static final Locale LOCALE_RUS = new Locale("ru");
	/**
	 * English locale
	 */
	public static final Locale LOCALE_ENG = new Locale("en");
	/**
	 * Resource bundle location to use
	 */
	public static final String MESSAGES = "nls.messages";
	
	private static ThreadLocal context = new ThreadLocal() {
		protected Object initialValue() {
			//System.out.println("[DEBUG] ContextProvider: Created new one for thread " + Thread.currentThread().getName());
			return new ContextProvider();
		}
	};
	
	/**
	 * Gets ContextProvider instance attached to current {@link java.lang.Thread}
	 * @return ContextProvider instance attached to current {@link java.lang.Thread}
	 */
	public static ContextProvider getContext() {
		//if (context.get() == null)
		//	context.set(new ContextProvider());
		return (ContextProvider) context.get();
	}
	
	private Locale locale;
	
	private ContextProvider()
	{
		setLocale(Locale.getDefault());
	}
	
	/**
	 * Returns russian or english label depending on caller's locale settings
	 * For now supports English and Russian languages only. If caller requires russian locale then 
	 * russian labels will be shown. For all other languages english titles will be used.
	 * @param ru russian label
	 * @param en english label
	 * @return russian or english label depending on caller's locale settings
	 */
	public String getLocaleString(String ru, String en)
	{
		return "ru".equals(locale.getLanguage()) ? ru : en;
	}
	
	/**
	 * Returns localized message from resource bundle
	 * @param key message key
	 * @return message in language setted by {@link #setLocale(Locale)}  
	 */
	public String getLocaleMessage(String key)
	{
		return ResourceBundle.getBundle(MESSAGES, locale).getString(key);
	}

	/**
	 * Returns date in default format of choosen locale
	 * @param date date value
	 * @return string representation of date (default locale format is used)
	 */
	public String getLocaleDate(Date date)
	{
		return DateFormat.getDateInstance(DateFormat.SHORT, locale).format(date);
	}
	
	/**
	 * Returns date parsed from string-date, previously  generated in default locale format.
	 * @param date string value
	 * @return parsed date
	 */
	public Date getDateFromLocalisedString(String date) {
		Date theDate = null;
		try {
			theDate = DateFormat.getDateInstance(DateFormat.SHORT, locale).parse(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return theDate;
	}

	/**	 
	 * ���������� ���� �� ����������� �� ������, �� ������ defaultTimePattern ������� �� DateAttribute
	 * @param date string value
	 * @return parsed date
	 */
	public Date getDateFromDefaultPatternString(String date) {
		Date theDate = null;
		SimpleDateFormat parseDate = new SimpleDateFormat(DateAttribute.defaultTimePattern);
		try {
			theDate = parseDate.parse(date);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return theDate;
	}
	
	/**
	 * Returns date in choosen format of choosen locale
	 * @param date date value
	 * @return string representation of date and time (default locale format is used)
	 */
	public String getLocaleDateTime(Date date, String timePattern)
	{
		return /*DateFormat.getDateInstance(DateFormat.SHORT, locale).format(date) + " " +*/ (new SimpleDateFormat(timePattern, locale)).format(date);
	}
	/**
	 * Sets locale information for request processing thread
	 * @param locale locale to use. Usually it is a value returned by {@link javax.portlet.PortletRequest#getLocale()} 
	 */
	public void setLocale(Locale locale)
	{
		if (locale == null || !"en".equals(locale.getLanguage()) && !"ru".equals(locale.getLanguage()))
			locale = LOCALE_RUS;
		this.locale = locale;
		//System.out.println("[DEBUG] ContextProvider: Locale set to " + locale.getLanguage() +
		//		" for thread " + Thread.currentThread().getName());
	}

	/**
	 * Returns locale selected for current Thread
	 * @return locale selected for current Thread
	 */
	public Locale getLocale()
	{
		return locale;
	}
}
