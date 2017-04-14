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
package com.aplana.dbmi.login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.web.tomcat.security.login.WebAuthentication;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Represents servlet to authorize from external system using custom SSO
 * 
 * 
 */
public class SSOLoginServlet extends HttpServlet {

	private static final String LOGIN = "login";

	private static final String DATETIME = "datetime";

	private static final String ID = "id";

	private static int TIME_OUT = 0;

	/**
	 * SSO session expiration period in ms
	 */
	private static long SSO_SESION_TIME_OUT_MS = 240000;

	protected Log logger = LogFactory.getLog(getClass());

	public static final String SSO_VERIFIED = "br4jSsoSuccess";

	/**
	 * configuration login properties
	 */
	private Properties dbmiProperties = new Properties();

	public void init() throws ServletException {
		super.init();

		try {
			dbmiProperties.load(this.getClass().getResourceAsStream(
					"/conf/dbmiLogin.properties"));
		} catch (IOException e) {
			throw new ServletException(e);
		}

	}

	/**
	 * Verifies if SSO session "id" was passed to URL If session id was not
	 * passed then redirect to standard login page
	 */
	private boolean checkSessionId(HttpServletRequest req,
			HttpServletResponse resp) throws IOException, ServletException {

		String dbmiLoginPath = getDbmiLoginProperty("dbmiLoginPath");

		dbmiLoginPath = buildFullURL(req, dbmiLoginPath);

		// external server session ID
		String id = req.getParameter(ID);
		if (id == null) {// if session id was't passed then we redirect to the
			// login page
			logger
					.error("SSO Session ID was not passed. Redirect to login page!");
			resp.sendRedirect(dbmiLoginPath);
			return false;
		}

		logger.info("Session ID :" + id);

		return true;

	}

	private String buildFullURL(HttpServletRequest req, String path) {
		String scheme = req.getScheme(); // http
		String serverName = req.getServerName(); // hostname.com
		int serverPort = req.getServerPort(); // 80
		StringBuffer url = new StringBuffer();

		url.append(scheme).append("://").append(serverName);
		url.append(":").append(serverPort);
		url.append(path);

		return url.toString();
	}

	private boolean checkSSOLoginData(long currentTime,
			SSOLoginData ssoLoginData, HttpServletRequest req,
			HttpServletResponse resp) throws IOException, ServletException {
		String dbmiLoginPath = getDbmiLoginProperty("dbmiLoginPath");

		dbmiLoginPath = buildFullURL(req, dbmiLoginPath);

		if (ssoLoginData == null) {
			// there are problems to read sso data from external
			// system...redirect to login page
			logger.info("SSO session was not passed. Redirect to login page.");
			resp.sendRedirect(dbmiLoginPath);
			return false;
		}

		// checks dateTime

		long ssoExternalTime = ssoLoginData.getMilliseconds();
		if ((currentTime - ssoExternalTime) > SSO_SESION_TIME_OUT_MS) {

			logger.info("SSO session was expired. Our start time is : "
					+ currentTime + " arrived SSO time : " + ssoExternalTime);
			logger.info("Redirect to login page");
			resp.sendRedirect(dbmiLoginPath);
			return false;
		}

		return true;

	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		logger.info("SSOLoginServlet requestURL : " + req.getRequestURL());

		if (!checkSessionId(req, resp))
			return;
		// gets current utc time in milliseconds
		long currentTime = (new Date()).getTime();

		SSOLoginData ssoLoginData = readExternalSSOLoginData(req);

		if (!checkSSOLoginData(currentTime, ssoLoginData, req, resp)) 
			return;

		boolean result = authenticate(req, ssoLoginData);

		handleAuthenticationResult(req, resp, result);

	}


	private void handleAuthenticationResult(HttpServletRequest req,
			HttpServletResponse resp, boolean result) throws IOException,
			ServletException {

		String dbmiLoginPath = getDbmiLoginProperty("dbmiLoginPath");
		dbmiLoginPath = buildFullURL(req, dbmiLoginPath);

		String redirectUrl = getDbmiLoginProperty("redirectUrl");
		redirectUrl = buildFullURL(req, redirectUrl);

		if (!result) {// if authentication is failed then redirect to login page
			// {
			logger
					.error("User was  not authenticated. Redirect to login page!");
			resp.sendRedirect(dbmiLoginPath);
		} else {
			logger.info("User was authenticated successfully!");
			resp.sendRedirect(redirectUrl);
		}
	}

	private boolean authenticate(HttpServletRequest req,
			SSOLoginData ssoLoginData) {
		logger.info("SSOLoginServlet authenticating");

		// flag to pass to login module
		req.setAttribute(SSO_VERIFIED, "true");
		WebAuthentication webAuthentication = new WebAuthentication();

		boolean result = webAuthentication.login(ssoLoginData.getLogin(), "");
		return result;
	}

	private String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			logger.error(e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
		return sb.toString();

	}

	/**
	 * Reads SSO data from external system using Http "GET" connection. External
	 * system responses using JSON format : {id: <GUID>, datetime: <GUID
	 * dateTime >, login: <userLogin> }
	 * 
	 */
	protected SSOLoginData readExternalSSOLoginData(HttpServletRequest req)
			throws ServletException {

		try {

			String externalServerUrl = buildExternalServerUrlLink(req);

			logger.info("SSOLoginServlet reading SSO from external system :  "
					+ externalServerUrl);

			HttpURLConnection connection = createHttpURLConnection(
					externalServerUrl, TIME_OUT);
			connection.connect();
			int status = connection.getResponseCode();
			if (status != 201 && status != 200) {
				logger
						.error("Impossible to get SSO data from external system. Response code is : "
								+ status);
				throw new ServletException(
						"Impossible to get SSO data from external system. Invalid response code is : "
								+ status);
			}

			InputStream response = connection.getInputStream();
			String jsonReply = convertStreamToString(response);
			connection.disconnect();
			JSONObject jsonObject = (JSONObject) new JSONTokener(jsonReply)
					.nextValue();
			SSOLoginData ssoLoginData = convertToSSOLoginData(jsonObject);
			logger.info(buildSSOLoginDataInfo(ssoLoginData));
			return ssoLoginData;

		} catch (MalformedURLException ex) {
			logger.error(ex);
			return null;
		} catch (JSONException ex) {
			logger.error(ex);
			return null;
		} catch (IOException ex) {
			logger.error(ex);
			return null;
		}
	}

	private String buildSSOLoginDataInfo(SSOLoginData ssoLoginData) {

		StringBuffer buffer = new StringBuffer(
				"SSOLoginServlet received SSO : ");

		buffer.append(" id = ");
		buffer.append(ssoLoginData.getId());
		buffer.append(" datetime = ");
		buffer.append(ssoLoginData.getMilliseconds());
		buffer.append(" login = ");
		buffer.append(ssoLoginData.getLogin());

		return buffer.toString();
	}

	private String buildExternalServerUrlLink(HttpServletRequest req)
			throws ServletException {
		// external server session ID
		String id = req.getParameter(ID);
		String externalServerUrl = getDbmiLoginProperty("externalServerUrl");

		StringBuffer buffer = new StringBuffer(externalServerUrl);
		buffer.append("?");
		buffer.append("id=");
		buffer.append(id);

		return buffer.toString();
	}

	private String getParamStringValue(String parameter, JSONObject jsonObject)
			throws JSONException {

		Object value = getParamValue(parameter, jsonObject);

		if (value == null)
			return null;

		if (!(value instanceof String)) {
			logger.error("JSON parameter " + parameter + " is invalid type");
		}

		return (String) value;
	}

	private Object getParamValue(String parameter, JSONObject jsonObject)
			throws JSONException {

		Object value = jsonObject.get(parameter);

		if (value == null) {
			logger.error("JSON parameter " + parameter + " was not passed ");
			return null;
		}
		return value;
	}

	private Long getParamLongValue(String parameter, JSONObject jsonObject)
			throws JSONException {

		Object value = getParamValue(parameter, jsonObject);

		if (value == null)
			return null;

		if (!(value instanceof Long)) {
			logger.error("JSON parameter " + parameter + " is invalid type");
		}

		return (Long) value;
	}

	/**
	 * Creates SSOLoginData from passed JSON object
	 * 
	 * @param jsonObject
	 */
	private SSOLoginData convertToSSOLoginData(JSONObject jsonObject)
			throws JSONException, ServletException {

		String id = getParamStringValue(ID, jsonObject);

		Long dateTime = getParamLongValue(DATETIME, jsonObject);
		if (dateTime == null)
			throw new ServletException(
					"DateTime was not passed. It is impossible to authorize!");

		String login = getParamStringValue(LOGIN, jsonObject);

		if (login == null)
			throw new ServletException(
					"Login was not passed. It is impossible to authorize!");

		SSOLoginData ssoLoginData = new SSOLoginData();
		ssoLoginData.setId(id);
		ssoLoginData.setMilliseconds(dateTime.longValue());
		ssoLoginData.setLogin(login);

		return ssoLoginData;
	}

	/**
	 * Create HttpURLConnection for given url and timeout
	 */
	private HttpURLConnection createHttpURLConnection(String url, int timeout)
			throws MalformedURLException, IOException, ProtocolException {
		URL u = new URL(url);

		HttpURLConnection c = (HttpURLConnection) u.openConnection();
		c.setRequestMethod("GET");
		c.setRequestProperty("Content-length", "0");
		c.setUseCaches(false);
		c.setAllowUserInteraction(false);
		c.setConnectTimeout(timeout);
		c.setReadTimeout(timeout);

		return c;
	}

	/**
	 * Gets configuration property by name
	 * 
	 * @param property
	 *            property name to read
	 * @return
	 */
	private String getDbmiLoginProperty(String property)
			throws ServletException {
		String value = dbmiProperties.getProperty(property);
		
		if (value == null) {
			logger.error("DbmiLogin Property " + property + "was not defined");
			throw new ServletException("Property " + property
					+ " was not defined");
		}

		return value;
	}

}
