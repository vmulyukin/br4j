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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.aplana.dbmi.model.ContextProvider;

/**
 * @author RAbdullin
 * Localization strings resource simplification.
 *
 */
public class BundlePool {
	/**
	 * minimum pool size
	 */
	public static final int MININUM_POOLSIZE = 3;
	
	/**
	 * resource bundle pool.
	 * key : string = resName + localName.
	 */
	private static final Map bundlePool = new HashMap(MININUM_POOLSIZE);
	private static int poolCapacity = MININUM_POOLSIZE;
	
	/**
	 * Get string from resource pool using current locale.
	 * @see(overload safeGetString)
	 * @param 
	 * @param 
	 * @return
	 */
	public static String safeGetString( final String resourceName, final String itemId)
	{
		return safeGetString( resourceName, itemId, null);
	}
	
	/**
	 * Get string from resource pool using the locale.
	 * @param resourceName	the properties-file name ("myres" if file is ".\myres.properties").
	 * @param itemId	the string identifier in the resource.
	 * @param locale	desired locale, null = current one.
	 * @return the loaded localized string from resource or null if problems occured.
	 */
	public static String safeGetString( final String resourceName,  
			final String itemId, Locale locale )
	{
		try {
			return unsafeGetString(resourceName, itemId, locale);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Equivalent for MessageFormat( safeGetString(arg0, arg1, arg2), ...args...);
	 * @param resourceName the name of the properties-file.
	 * @param itemIdOfFmt the string id of the format string inside resource bundle.
	 * @param locale desired locale, null = current.
	 * @param args parameters for MessageFormat.format( fmtStr, args)
	 * @return
	 */
	public static String safeGetStringFmt( final String resourceName, 
		final String itemIdOfFmt, Locale locale, Object[] args )
	{
		final String fmt = safeGetString(resourceName, itemIdOfFmt, locale);
		if (fmt == null) return null;
		return MessageFormat.format( fmt, args);
	}

	public static String safeGetStringFmt( final String resourceName, 
		final String itemIdOfFmt, Object[] args )
	{
		return safeGetStringFmt( resourceName, itemIdOfFmt, null, args);
	}
	
	/**
	 * Get string from resource pool using the locale.
	 * @param resourceName	the properties-file name ("myres" if file is ".\myres.properties").
	 * @param itemId	the string identifier in the resource.
	 * @param locale	desired locale, null = current one.
	 * @return the loaded localized string from resource or null if problems occured.
	 */
	public static String unsafeGetString( final String resourceName,  
			final String itemId, Locale locale )
	{
		final ResourceBundle rb = ensureBundle(resourceName, locale);
		return rb.getString(itemId);
	}
	
	/**
	 * Try to find in the current pool, if not found - put it inside (free 
	 * place if capacity oversized).
	 * @param resourceName
	 * @param locale
	 * @return resource bundle
	 */
	private static ResourceBundle ensureBundle( final String resourceName,
			Locale locale)
	{
		if (locale == null)
			locale = ContextProvider.getContext().getLocale();

		final String keyStr = makePoolKey( resourceName, locale);

		// try to find in the pool...
		ResourceBundle result = (ResourceBundle) bundlePool.get(keyStr);
		if (result == null)
		{	// not found -> create new one ...
			result = ResourceBundle.getBundle(resourceName, locale);
			putIntoPool( keyStr, result);
		}
		return result;
	}

	/**
	 * @param keyStr
	 * @param result
	 */
	private static void putIntoPool(String keyStr, ResourceBundle result) {
		if (result == null) return;

		// free space for new entry if pool is full ...
		if (bundlePool.size() >= poolCapacity)
			setMaxPoolSize(poolCapacity - 1);
		
		bundlePool.put(keyStr, result);
	}

	/**
	 * @param resourceName
	 * @param locale
	 * @return
	 */
	private static String makePoolKey(String resourceName, Locale locale) {
		return MessageFormat.format( "{0}_{1}",
					new Object[] {
						resourceName,
						(locale != null) ? locale.getCountry() : ""
					});
	}

	public static int getPoolCapacity()
	{
		return poolCapacity;
	}
	
	/**
	 * Set new pool size.
	 * @param newSize desired size (cannot be less than MININUM_POOLSIZE)
	 * @return new actual value.
	 */
	public static int setPoolCapacity(int newSize)
	{
		if (newSize < MININUM_POOLSIZE)
			newSize = MININUM_POOLSIZE;
		
		if (bundlePool.size() > newSize) 
			// ������ ��� ������� ����� ��������� ������������
			setMaxPoolSize(newSize);
		if (newSize != poolCapacity) 
			poolCapacity = newSize;
		return poolCapacity;
	}

	/**
	 * @param newSize
	 */
	private static void setMaxPoolSize(int newSize) {

		if (newSize < MININUM_POOLSIZE)	newSize = MININUM_POOLSIZE;

		if (newSize >= bundlePool.size()) return;

		// ������������
		Object[] keys = bundlePool.keySet().toArray();
		// TODO: ���� ������ LRU (least recent use)
		for (int i = 0; i < keys.length; i++) {
			if (newSize >= bundlePool.size()) 
				break;
			bundlePool.remove( keys[i] );
		}
	}
	
}
