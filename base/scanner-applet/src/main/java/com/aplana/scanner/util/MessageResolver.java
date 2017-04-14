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
package com.aplana.scanner.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class to resolve messages from <code>ResourceBundle</code>s.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class MessageResolver {
	private static final Log logger = LogFactory.getLog(MessageResolver.class);
	
	private final Map<String, Map<Locale, ResourceBundle>> cachedResourceBundles =
		new HashMap<String, Map<Locale, ResourceBundle>>();
	
	private String[] basenames;
	
	/**
	 * Default constructor.
	 */
	public MessageResolver() {
		basenames = new String[0];
	}
	/**
	 * Constructs an object instance by an array of basenames.
	 *
	 * @param basenames  an array of basenames
	 * 
	 * @see #setBasenames(String...)
	 */
	public MessageResolver(String... basenames) {
		setBasenames(basenames);
	}
	
	/**
	 * Set an array of basenames, each following {@link java.util.ResourceBundle}
	 * conventions: essentially, a fully-qualified classpath location. If it
	 * doesn't contain a package qualifier (such as <code>org.mypackage</code>),
	 * it will be resolved from the classpath root.
	 * <p>The associated resource bundles will be checked sequentially
	 * when resolving a message code. Note that message definitions in a
	 * <i>previous</i> resource bundle will override ones in a later bundle,
	 * due to the sequential lookup.
	 * <p>Note that ResourceBundle names are effectively classpath locations: As a
	 * consequence, the JDK's standard ResourceBundle treats dots as package separators.
	 * This means that "test.theme" is effectively equivalent to "test/theme",
	 * just like it is for programmatic <code>java.util.ResourceBundle</code> usage.
	 * 
	 * @see java.util.ResourceBundle#getBundle(String)
	 */
	public void setBasenames(String... basenames)  {
		if (basenames != null) {
			this.basenames = new String[basenames.length];
			for (int i = 0; i < basenames.length; i++) {
				String basename = basenames[i];
				if (basename == null)
					throw new IllegalArgumentException("Basename cannot be null");
				
				this.basenames[i] = basename.trim();
				if (this.basenames[i].length() == 0)
					throw new IllegalArgumentException("Basename must not be empty");
			}
		}
		else
			this.basenames = new String[0];
	}
	
	/**
   * Convenience method to get a resource string for the default locale.
   *
   * @param  code    the code identifying the message to obtain
   * @return the resolved message
   */
  public final String getMessage(String code) {
  	return getMessage(code, Locale.getDefault());
  }
	
	/**
   * Convenience method to get a resource string.
   *
   * @param  code    the code identifying the message to obtain
   * @param  locale  the locale
   * @return the resolved message
   */
  public final String getMessage(String code, Locale locale) {
  	String result = null;
  	for (int i = 0; result == null && i < this.basenames.length; i++) {
			ResourceBundle bundle = getResourceBundle(this.basenames[i], locale);
			if (bundle != null) {
				result = getStringOrNull(bundle, code);
			}
		}
		return result;
  }
  
	/**
   * Gets the {@link ResourceBundle} for the applet.
   *
   * @param  locale  the locale to get resource bundle for
   * @return the {@link ResourceBundle} for the applet
   */
	protected ResourceBundle getResourceBundle(String basename, Locale locale) {
		synchronized (cachedResourceBundles) {
			Map<Locale, ResourceBundle> localeMap = cachedResourceBundles.get(basename);
			if (localeMap != null) {
				ResourceBundle bundle = localeMap.get(locale);
				if (bundle != null) {
					return bundle;
				}
			}
			try {
				ResourceBundle bundle = ResourceBundle.getBundle(basename, locale);
				if (localeMap == null) {
					localeMap = new HashMap<Locale, ResourceBundle>();
					cachedResourceBundles.put(basename, localeMap);
				}
				localeMap.put(locale, bundle);
				return bundle;
			}
			catch (MissingResourceException e) {
				if (logger.isWarnEnabled())
					logger.warn("ResourceBundle [" + basename + "] not found: " + e.getMessage());
				throw e;
			}
		}
	}
	
	private String getStringOrNull(ResourceBundle bundle, String key) {
		try {
			return bundle.getString(key);
		}
		catch (MissingResourceException ex) {
			// Assume key not found
			// -> do NOT throw the exception to allow for checking parent message source.
			return null;
		}
	}
}
