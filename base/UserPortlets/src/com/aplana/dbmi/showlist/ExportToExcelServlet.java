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
package com.aplana.dbmi.showlist;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;


/**
 * Servlet implementation class for Servlet: ExportToExcelServlet
 *
 */
 public class ExportToExcelServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	 
     private static final char FIELD_TERMINATOR = '"';
     private static final char FIELD_SEPARATOR = ';';
     //private List ignored
 	 private static final ObjectId ORD_TEMPLATE_ID = ObjectId.predefined(Template.class, "jbr.ord");
 	 private static final String[] ignored_Columns = {"_STATE","_PRELIMINARY_TERM"};
 	 
 	 //������ ������� � ������� ���������� �� ���������� 0 ��� �������� null
 	 private List<ObjectId> objIds;
 	 {
 		 objIds = new java.util.ArrayList();
 		 //��������� ���������
 		 objIds.add(new ObjectId(com.aplana.dbmi.model.ListAttribute.class, "ADMIN_290575"));
 		 objIds.add(new ObjectId(com.aplana.dbmi.model.ListAttribute.class, "JBR_HOWFAST"));
 		 //��������������� ����
 		 objIds.add(new ObjectId(com.aplana.dbmi.model.DateAttribute.class,"JBR_TCON_TERM_PRELIM"));
 	 }
 	 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         try {
        	 
        	 writeToExcel(request, response);
        	 
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
         return (str!=null)?str.replaceAll("\"", "\"\""):null;	// ���� ������� ��� ������� ���������� �������� ��������, � �������� �������� ��� (�������� ��������������)
     }
     
     
     
     /**
      * ������������ ����� �� ������ search ������������� � MIShowListPortletSessionBean 
      * @param request
      * @param response
      * @return ���������� SearchAdapter ���������� � ���� ������ �� ���� ���������, ��� ���������� �� ���������.
      * @throws DataException
      * @throws ServiceException
      */
     private SearchAdapter getData(HttpServletRequest request, HttpServletResponse response) throws DataException, ServiceException{
    	 MIShowListPortletSessionBean sessionBean = getSessionBean(request);
    	 Search search = sessionBean.getExecSearch();
    	 if(search == null)
    		 return null;
    	 search = search.makeCopy();
    	 search.clearFilter();
    	 search.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
    	 DataServiceBean serviceBean = sessionBean.getServiceBean(request, "");
		 final SearchAdapter adapter = new SearchAdapter();
		 adapter.setCardLinkDelimiter(MIShowListPortlet.PREF_DEFAULT_ATTR_DELIMITER);
		 adapter.executeSearch(serviceBean, search);    	 
    	 return adapter;
     }
     
     /**
      * ���������� MIShowListPortletSessionBean
      * @param request
      * @return
      */
     private MIShowListPortletSessionBean getSessionBean(HttpServletRequest request){
         String namespace = request.getParameter("namespace");
         System.out.println("ExportToExcelServlet: namespace=" + namespace);    	 
    	 MIShowListPortletSessionBean sessionBean = (MIShowListPortletSessionBean) request.getSession().getAttribute(MIShowListPortlet.SESSION_BEAN + '.' + namespace);
    	 return sessionBean;    	     	 
     }
     
     
     /**
      * ����� � log �������� ���������
      * @param request
      */
     private void printAttributeNames(HttpServletRequest request){
    	 HttpSession session = request.getSession();
         Enumeration attributeNames = session.getAttributeNames();
         while (attributeNames.hasMoreElements()) {
             String attName = (String) attributeNames.nextElement();
             System.out.println("ExportToExcelServlet: attName=" + attName + ", value=" + session.getAttribute(attName));
         }    	 
     }
     
     private void deleteIgnoredColumns (SearchAdapter adapter){
    	 Collection<SearchResult.Column> cleanedColumns = new LinkedList<SearchResult.Column>();
    	 for (Iterator columnIter = adapter.getResultObject().getColumns().iterator(); columnIter.hasNext();) {
    		 SearchResult.Column column = (SearchResult.Column)columnIter.next();
    		 if(!column.isExcelIgnore()&&!deleteCommonIgnoredColumns(column)){
    			 cleanedColumns.add(column);
    		 }
    	 }
    	 adapter.getResultObject().setColumns(cleanedColumns);
     }
     
     private boolean deleteCommonIgnoredColumns(SearchResult.Column col){
    	 if(Arrays.asList(ignored_Columns).contains(((String)col.getAttributeId().getId()))){
    		 return true;
    	 } else {
    		 return false;
    	 }
     }
     
     /**
      * ������������ �������� � excel
      * @param request
      * @param response
      * @throws Exception
      */
     private void writeToExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	 
    	 Locale locale = request.getLocale();
         System.out.println("ExportToExcelServlet: locale=" + locale);
         String language = locale.getLanguage();
         System.out.println("ExportToExcelServlet: language=" + language);
         
         String requestCharacterEncoding = request.getCharacterEncoding();
         System.out.println("ExportToExcelServlet: request.CharacterEncoding=" + requestCharacterEncoding);
         
         printAttributeNames(request);
         
         String mime = "text/csv; charset=windows-1251";
         response.setContentType(mime);
         response.setHeader("Content-Disposition", "attachment; filename=\"data.csv\"");
         PrintWriter writer = response.getWriter();

         SearchAdapter searchAdapter = getData(request, response);
         if(searchAdapter != null) {
	         deleteIgnoredColumns(searchAdapter);
	         List data = searchAdapter.getData(language, true);
	         List metaData = searchAdapter.getMetaDataNotReplaceColumn(language);
	         
	         boolean firstColumn = true;
	         for (Iterator metaIter = metaData.iterator(); metaIter.hasNext();) {
	             SearchResult.Column columnDescription = (SearchResult.Column) metaIter.next();
	             if(columnDescription!=null) {
	                 String columnName = columnDescription.getName();
	                 //���� �������� ������� null - �� ������� ��
	                 if(columnName != null) {    
	                	 if (!firstColumn) {
		                     writer.print(FIELD_SEPARATOR);
		                 }
	                	 writer.print(FIELD_TERMINATOR + escapeString(columnName) + FIELD_TERMINATOR);
	                	 firstColumn = false;
	                 }
	             }
	         }             
	         writer.println();
	         
			 
	         for (Iterator dataIter = data.iterator(); dataIter.hasNext();) {
	             List row = (List) dataIter.next();
	             firstColumn = true;
	             for (int i = 0 ; i < metaData.size() ; i++) {
	            	 SearchResult.Column columnDescription = (SearchResult.Column) metaData.get(i);
	            	 if(columnDescription!=null) {
	            		 String columnName = columnDescription.getName();
	            		//���� �������� ������� null - �������� ������� �� ���������
	            		 if(columnName!=null) {
	            			 if (!firstColumn) {
	                             writer.print(FIELD_SEPARATOR);
	                         }
	            			 boolean found = false;
	            			 if(columnDescription.getAttributeId()!=null) {
	            				 System.out.println(">>>attribute type: "+columnDescription.getAttributeId().getType()+"__ id: "+columnDescription.getAttributeId().getId());
		            			 for(ObjectId id : objIds) {
		            				 if(columnDescription.getAttributeId().equals(id)&&row.get(i)==null) {
		                        		 found = true;
		                        	 }
		            			 }
	            			 }
	                    	 if(!found) {
	                    		 // (YNikitin, 2012/07/18) ���� �������� � ��������� ������� = null, �� ������� 0 
	                             writer.print(FIELD_TERMINATOR + escapeString(String.valueOf((row.get(i)!=null?row.get(i):String.valueOf(0)))) + FIELD_TERMINATOR);
	                         }
	                         firstColumn = false;
	            		 }
	            	 }
	             }
	             writer.println();
	         }
         }
         
         writer.close();
         System.out.println("ExportToExcelServlet: response.characterEncoding=" + response.getCharacterEncoding());
     }
}
