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

import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * Classpath resource loader.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public final class ResourceLoader {
	/**
	 * Returns the default <code>ClassLoader</code> to use: typically the thread context
	 * <code>ClassLoader</code>, if available; the <code>ClassLoader</code> that loaded the
	 * <code>ResourceLoader</code> class will be used as fallback.
	 * 
	 * @return the default <code>ClassLoader</code> (never <code>null</code>)
	 * @see java.lang.Thread#getContextClassLoader()
	 */
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		}
		catch (Throwable ex) {
			// Cannot access thread context ClassLoader - falling back to system class loader...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = ResourceLoader.class.getClassLoader();
		}
		return cl;
	}
	
	/**
	 * Returns an URL for reading the specified resource from the classpath.
	 *
	 * @param resource  the resource name
	 * @return an URL for reading the resource or <code>null</code> if the resource could not be found
	 */
	public static URL getResource(String resource) {
		return getDefaultClassLoader().getResource(resource);
	}
	
	/**
	 * Returns an input stream for reading the specified resource from the classpath.
	 *
	 * @param resource  the resource name
	 * @return an input stream for reading the resource or <code>null</code> if the resource could not
	 *         be found
	 */
	public static InputStream getResourceAsStream(String resource) {
		return getDefaultClassLoader().getResourceAsStream(resource);
	}
	
	/**
	 * Returns a <code>Source</code> for reading the specified resource from the classpath.
	 *
	 * @param resource  the resource name
	 * @return a source for reading the resource or <code>null</code> if the resource could not
	 *         be found
	 */
	public static Source getResourceAsSource(String resource) {
		URL url = getDefaultClassLoader().getResource(resource);
		return url == null ? null : new StreamSource(url.toExternalForm());
	}
}
