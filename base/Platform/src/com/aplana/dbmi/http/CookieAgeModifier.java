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
package com.aplana.dbmi.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class CookieAgeModifier implements Filter {
	private static final Log logger = LogFactory.getLog(CookieAgeModifier.class);

	public void destroy() {}
	public void init(FilterConfig fConfig) throws ServletException {}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpSession sess = ((HttpServletRequest)request).getSession(true);

		// ����� ����� ���� ���� ��������� ������ ��� ��� ��������� ������� �� ������� (�.�. ����� ����� ������ ���� ������ � ���������� ������� ����� ������)
		Cookie cookie = new Cookie("JSESSIONID", sess.getId());
		Cookie cookie2 = null;
		cookie.setPath("/");
		Cookie[] cookies = ((HttpServletRequest) request).getCookies();
		if (cookies!=null){
			for(Cookie c : cookies){
				if ("JSESSIONID".equalsIgnoreCase(((Cookie)c).getName())||"JSESSIONIDSSO".equalsIgnoreCase(((Cookie)c).getName())){
					c.setMaxAge(sess.getMaxInactiveInterval());
				}
				// ������� ����������� ������� ����� SSO-��������, � �� ��� �� ���������� ������� ����������
				if ("JSESSIONIDSSO".equalsIgnoreCase(((Cookie)c).getName())){
					cookie2 = new Cookie("JSESSIONIDSSO", ((Cookie)c).getValue());
					cookie2.setPath("/");
					cookie2.setMaxAge(sess.getMaxInactiveInterval());
				}
			}
		}
		cookie.setMaxAge(sess.getMaxInactiveInterval());
		logger.debug("JSESSIONID timeout for session "+cookie.getValue()+" has changed by "+cookie.getMaxAge() + "ms");
		((HttpServletResponse) response).reset();
		((HttpServletResponse) response).addCookie(cookie);
		// ����� ���� ���� �����������, �������� ����� ���� ����� ������� ������� ������ � ������������� ������������ �������� �� �� �������
		if (cookie2!=null){
			((HttpServletResponse) response).addCookie(cookie2);
			logger.debug("JSESSIONIDSSO timeout for session "+cookie2.getValue()+" has changed by "+cookie2.getMaxAge() + "ms");
		}
		chain.doFilter(request, response);
	}

}
