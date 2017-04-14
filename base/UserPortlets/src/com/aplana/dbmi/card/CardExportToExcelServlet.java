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
package com.aplana.dbmi.card;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.AccessControlException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.TemplateBlock;

/**
 * Servlet implementation class for Servlet: CardExportToExcelServlet
 * 
 */
public class CardExportToExcelServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	private static final long serialVersionUID = 1L;
	
	public static final String NAMESPACE_PARAM_KEY = "namespace";
	private static final char FIELD_TERMINATOR = '"';
	private static final char FIELD_SEPARATOR = ';';

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {

			String namespace = request.getParameter(NAMESPACE_PARAM_KEY);
			System.out.println("CardExportToExcelServlet: namespace=" + namespace);
			Locale locale = request.getLocale();
			System.out.println("CardExportToExcelServlet: locale=" + locale);
			String language = locale.getLanguage();
			System.out.println("CardExportToExcelServlet: language=" + language);

			String requestCharacterEncoding = request.getCharacterEncoding();
			System.out.println("CardExportToExcelServlet: request.CharacterEncoding=" + requestCharacterEncoding);

			HttpSession session = request.getSession();
			Enumeration attributeNames = session.getAttributeNames();
			while (attributeNames.hasMoreElements()) {
				String attName = (String) attributeNames.nextElement();
				System.out.println("CardExportToExcelServlet: attName=" + attName + ", value=" + session.getAttribute(attName));
			}

			String mime = "text/csv; charset=windows-1251";
			response.setContentType(mime);
			response.setHeader("Content-Disposition", "attachment; filename=\"card.csv\"");
			PrintWriter writer = response.getWriter();

			CardPortletSessionBean sessionBean = (CardPortletSessionBean) session.getAttribute(CardPortlet.SESSION_BEAN + '.' + namespace);
			renderContent(writer, sessionBean);
			writer.close();
			System.out.println("CardExportToExcelServlet: response.characterEncoding=" + response.getCharacterEncoding());
		} catch (AccessControlException e) {
			System.out.println(e.getMessage());
			e.printStackTrace(System.out);
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace(System.out);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}

	}

	private void renderContent(PrintWriter writer, CardPortletSessionBean sessionBean) {
		Card card = sessionBean.getActiveCard();
		Collection attributeBlocks = card.getAttributes();
		for (Iterator blocksIter = attributeBlocks.iterator(); blocksIter.hasNext();) {
			TemplateBlock templateBlock = (TemplateBlock) blocksIter.next();
			System.out.println("CardExportToExcelServlet.renderContent: templateBlock.name=" + templateBlock.getName());
			Collection attributes = templateBlock.getAttributes();
			for (Iterator attrIter = attributes.iterator(); attrIter.hasNext();) {
				Attribute attribute = (Attribute) attrIter.next();
				System.out.println("CardExportToExcelServlet.renderContent: attribute.name=" + attribute.getName() + ", attribute.value=" + attribute.getStringValue());
				writer.print(FIELD_TERMINATOR);
				writer.print(escapeString(attribute.getName()));
				writer.print(FIELD_TERMINATOR);
				writer.print(FIELD_SEPARATOR);
				writer.print(FIELD_TERMINATOR);
				writer.print(escapeString(attribute.getStringValue()));
				writer.print(FIELD_TERMINATOR);
				writer.println();
			}
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	private String escapeString(String str) {
		return str != null ? str.replaceAll("\"", "\"\"") : null;
	}
}