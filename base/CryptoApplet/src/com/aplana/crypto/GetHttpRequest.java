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
package com.aplana.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GetHttpRequest {

	protected URLConnection connection;
	Map<String, String> cookies = new HashMap<String, String>();

	/**
	 * Creates a new HTTP Get request on a freshly opened
	 * URLConnection
	 *
	 * @param connection
	 *            an already open URL connection
	 */
	public GetHttpRequest(URLConnection connection) {
		this.connection = connection;
	}

	/**
	 * Creates a new HTTP Get request for a specified URL
	 *
	 * @param url
	 *            the URL to send request to
	 * @throws IOException
	 */
	public GetHttpRequest(URL url) throws IOException {
		this(url.openConnection());
	}

	/**
	 * Creates a new HTTP Get request for a specified URL string
	 *
	 * @param urlString
	 *            the string representation of the URL to send request to
	 * @throws IOException
	 */
	public GetHttpRequest(String urlString) throws IOException {
		this(new URL(urlString));
	}

	private void setCookiesToRequest() {
		StringBuffer cookieList = new StringBuffer();

		for (Iterator i = cookies.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) (i.next());
			cookieList.append(entry.getKey().toString() + "="
					+ entry.getValue());

			if (i.hasNext()) {
				cookieList.append("; ");
			}
		}
		if (cookieList.length() > 0) {
			connection.setRequestProperty("Cookie", cookieList.toString());
		}
	}

	/**
	 * adds a cookie to the requst
	 *
	 * @param name
	 *            cookie name
	 * @param value
	 *            cookie value
	 * @throws IOException
	 */
	public void setCookie(String name, String value) throws IOException {
		cookies.put(name, value);
	}

	/**
	 * adds cookies to the request
	 *
	 * @param cookies
	 *            the cookie "name-to-value" map
	 * @throws IOException
	 */
	public void setCookies(Map cookies) throws IOException {
		if (cookies == null)
			return;
		this.cookies.putAll(cookies);
	}

	/**
	 * adds cookies to the request
	 *
	 * @param cookies
	 *            array of cookie names and values (cookies[2*i] is a name,
	 *            cookies[2*i + 1] is a value)
	 * @throws IOException
	 */
	public void setCookies(String[] cookies) throws IOException {
		if (cookies == null)
			return;
		for (int i = 0; i < cookies.length - 1; i += 2) {
			setCookie(cookies[i], cookies[i + 1]);
		}
	}

	/**
	 * sends the Get request to the server, with all the cookies that were added
	 *
	 * @return input stream with the server response
	 * @throws IOException
	 */
	public InputStream get() throws IOException {
		setCookiesToRequest();
		try {
		    return AccessController
		    		.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
		    			public InputStream run() throws Exception {
		    					return connection.getInputStream();
		    			}
		    		});
		} catch (PrivilegedActionException ex) {
		    Exception cause = ex.getException();
		    if (cause instanceof IOException){
				throw (IOException)cause;
		    }
		    throw new IllegalStateException("Unexpected type of exception "
			    + cause.getClass().getName(), cause);
		}
	}

	/**
	 * sends the Get request to the server, with all the cookies that were added
	 *
	 * @param cookies
	 *            request cookies
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setCookies
	 */
	public InputStream get(Map cookies) throws IOException {
		setCookies(cookies);
		return get();
	}

	/**
	 * sends the Get request to the server, with all the cookies that were added
	 *
	 * @param cookies
	 *            request cookies
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setCookies
	 */
	public InputStream get(String[] cookies)
			throws IOException {
		setCookies(cookies);
		return get();
	}

	/**
	 *sends the Get request to the server, with all the cookies that were added
	 *
	 * @param cookies
	 *            request cookies
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setCookies
	 */
	public static InputStream get(URL url, Map cookies)
			throws IOException {
		return new GetHttpRequest(url).get(cookies);
	}

	/**
	 * sends the Get request to the server, with all the cookies that were added
	 *
	 * @param cookies
	 *            request cookies
	 * @return input stream with the server response
	 * @throws IOException
	 * @see setCookies
	 */
	public static InputStream get(URL url, String[] cookies) throws IOException {
		return new GetHttpRequest(url).get(cookies);
	}

    public URLConnection getConnection() {
        return connection;
    }

    public void setConnection(URLConnection connection) {
        this.connection = connection;
    }

    public String getHeaderField(String name) {
	return this.connection.getHeaderField(name);
    }
}
