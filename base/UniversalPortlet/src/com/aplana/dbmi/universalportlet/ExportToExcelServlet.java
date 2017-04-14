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
package com.aplana.dbmi.universalportlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.AccessControlException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class for Servlet: ExportToExcelServlet
 * 
 */
public class ExportToExcelServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
    private static final long serialVersionUID = 1L;
	private static final char FIELD_TERMINATOR = '"';
    private static final char FIELD_SEPARATOR = ';';

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {

            String namespace = request.getParameter("namespace");
//            System.out.println("ExportToExcelServlet: namespace=" + namespace);
//            Locale locale = request.getLocale();
//            System.out.println("ExportToExcelServlet: locale=" + locale);
//            String language = locale.getLanguage();
//            System.out.println("ExportToExcelServlet: language=" + language);
            
//            String requestCharacterEncoding = request.getCharacterEncoding();
//            System.out.println("ExportToExcelServlet: request.CharacterEncoding=" + requestCharacterEncoding);
            
            HttpSession session = request.getSession();
/*            Enumeration attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attName = (String) attributeNames.nextElement();
//                System.out.println("ExportToExcelServlet: attName=" + attName + ", value=" + session.getAttribute(attName));
            }*/
            UniversalPortletSessionBean sessionBean = (UniversalPortletSessionBean) session.getAttribute(UniversalPortlet.SESSION_BEAN + '.' + namespace);
            List<List<String>> data = sessionBean.getData();
            List<ColumnDescription> metaData = sessionBean.getColumnsMetaData();
            
            String mime = "text/csv; charset=windows-1251";
            response.setContentType(mime);
            response.setHeader("Content-Disposition", "attachment; filename=\"data.csv\"");
            PrintWriter writer = response.getWriter();
            
            boolean firstColumn = true;
            for (Iterator<ColumnDescription> metaIter = metaData.iterator(); metaIter.hasNext();) {
                ColumnDescription columnDescription = metaIter.next();
                
                if (!firstColumn) {
                    writer.print(FIELD_SEPARATOR);
                }
                writer.print(FIELD_TERMINATOR + escapeString(columnDescription.getDisplayName()) + FIELD_TERMINATOR);
                firstColumn = false;
            }
            writer.println();
            
            for (Iterator<List<String>> dataIter = data.iterator(); dataIter.hasNext();) {
                List<String> row = dataIter.next();
                firstColumn = true;
                for (Iterator<String> rowIter = row.iterator(); rowIter.hasNext();) {
                    
                    if (!firstColumn) {
                        writer.print(FIELD_SEPARATOR);
                    }
                    writer.print(FIELD_TERMINATOR + escapeString(rowIter.next()) + FIELD_TERMINATOR);
                    firstColumn = false;
                }
                writer.println();
            }
            
            writer.close();
//            System.out.println("ExportToExcelServlet: response.characterEncoding=" + response.getCharacterEncoding());
        } catch (AccessControlException e) {
            System.out.println(e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
    
    private String escapeString(String str) {
        return str.replaceAll("\"", "\"\"");
    }
}