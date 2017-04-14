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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.GenerateCardExportFile.ExportType;
import com.aplana.dbmi.action.ImportAttribute;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.GenerateCardExportFile;
import com.aplana.dbmi.action.ParseCardImportFile;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.ServletUtil;
import com.aplana.dbmi.showlist.MIShowListPortlet;
import com.aplana.dbmi.showlist.MIShowListPortletSessionBean;

/**
 * A servlet designed to control cards/custom entities export file creation
 * 
 */
public class ExportToCsvServlet extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(ExportToCsvServlet.class);
	
	public static final String PARAM_EXPORT_TEMPLATE = "EXPORT_TEMPLATE";
	public static final String PARAM_EXPORT_CUSTOM = "EXPORT_CUSTOM";
	public static final String PARAM_CSV_TEMPLATE_FILE_CARD_ID = "CSV_TEMPLATE_FILE_CARD_ID";
	
	private static final String FILENAME_DATE_FORMAT = "yyyy-MM-dd";
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         try {
        	 
        	 // a template of cards
        	 final String templateId = request.getParameter(PARAM_EXPORT_TEMPLATE);
        	 // custom table parameter
        	 final String custom = request.getParameter(PARAM_EXPORT_CUSTOM);
        	 
        	 final DataServiceBean serviceBean = ServletUtil.createService(request);
        	 Material material = findMaterial(request, serviceBean);
        	 
        	 GenerateCardExportFile exportAction = new GenerateCardExportFile();
        	 ExportType exportType = null;
        	 if(templateId != null) {
        		 if(custom != null)
        			 throw new IllegalStateException("Both export type parameters cannot be provided at the same time");
	        	 exportType = ExportType.CARDS;
	        	 exportAction.setImportAttributes(parseImportAttributes(material, serviceBean, templateId));
	        	 exportAction.setSearch(getSearch(request));
	         } else if(custom != null) { // for universal portlet
	        	 exportType = ExportType.CUSTOM;
	        	 exportAction.setSqlFilter("");
        	 } else throw new IllegalStateException("Neither "+PARAM_EXPORT_TEMPLATE+" parameter (for cards export) nor "+PARAM_EXPORT_CUSTOM+" (for custom export) were provided");
        	 
        	 exportAction.setExportType(exportType);
        	 final List<String> lines = serviceBean.doAction(exportAction);
        	 writeToCsv(request, response, lines, findMaterial(request, serviceBean), generateExportFileName(exportAction, serviceBean));
         } catch (Exception e) {
        	 logger.error(e);
        	 sendError(request, response, e);
         }

    }
	
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
    
    /**
     * Sends result to the client as a csv file
     * @param request
     * @param response
     * @param lines result lines to be written
     * @param header export template file header
     * @throws IOException
     */
    private void writeToCsv(HttpServletRequest request, HttpServletResponse response, List<String> lines, Material header, String fileName) throws IOException {
    	String mime = "text/csv; charset=UTF-8";
        response.setContentType(mime);
        response.setCharacterEncoding("UTF-8");
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''"+fileName);
        
        final List<String> result = new ArrayList<String>();
    	BufferedReader buf = null;
    	// read export template file header
    	try {
    		buf = new BufferedReader(new InputStreamReader(header.getData()));
    		String line = "";
    		while((line = buf.readLine()) != null) {
    			result.add(line);
    		}
    	} finally {
    		if(buf != null)
    			buf.close();
    	}
    	result.addAll(lines);
    	
    	PrintWriter writer = response.getWriter();
        for(String line : result) {
        	writer.print(line);
        	writer.println("");
        }
        writer.close();
    }
    
    /**
     * Looks for an export template file by card_id that is retrieved from request parameter
     * @param request
     * @param serviceBean
     * @return
     * @throws ServiceException
     * @throws DataException
     */
    private Material findMaterial(HttpServletRequest request, DataServiceBean serviceBean) throws ServiceException, DataException {
    	final String cardIdStr = request.getParameter(PARAM_CSV_TEMPLATE_FILE_CARD_ID);
    	if(cardIdStr == null || cardIdStr.equals(""))
    		throw new DataException("Csv template file card_id must be provided");
    	ObjectId cardId = new ObjectId(Card.class, Long.parseLong(cardIdStr));
	   	
	   	DownloadFile downloadAction = new DownloadFile();
	   	downloadAction.setCardId(cardId);
	   	
	   	return serviceBean.doAction(downloadAction);
    }
    
    /**
     * Parses export template file to a list of ImportAttrute
     * @param material export template file
     * @param serviceBean
     * @param templateId cards template to be exported
     * @return
     * @throws ServiceException
     * @throws DataException
     */
    private List<ImportAttribute> parseImportAttributes(Material material, DataServiceBean serviceBean, String templateId) throws ServiceException, DataException {
    	ParseCardImportFile parseAction = new ParseCardImportFile();
	   	parseAction.setFile(material.getData());
	   	parseAction.setTemplateId(ObjectIdUtils.getObjectId(Template.class, templateId, true));
	   	parseAction.setGetHead(true);
	   	List<List<ImportAttribute>> importList = serviceBean.doAction(parseAction);
	   	
	   	return importList.get(0);
    }
    
    /**
     * Retrieves search object from session bean (for cards export)
     * @param request
     * @return
     */
    private Search getSearch(HttpServletRequest request) {
    	String namespace = request.getParameter("namespace");
        MIShowListPortletSessionBean sessionBean = (MIShowListPortletSessionBean) request.getSession().getAttribute(MIShowListPortlet.SESSION_BEAN + '.' + namespace);
    	Search search = sessionBean.getExecSearch();
   	 	if(search == null)
   	 		return null;
   	 	search.clearFilter();
   	 	search.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
   	 	return search.makeCopy();
    }
    
    /**
     * Generates result file name in format yyyy-MM-dd_%name%, where %name& = %temlpate_id%-%template_name_rus% for cards export and %custom_table_name_rus% for custom export
     * i.e. 2014-12-22_10-�������(����������) 
     * @param action
     * @param serviceBean
     * @return
     * @throws ServiceException
     * @throws DataException
     */
    private String generateExportFileName(GenerateCardExportFile action, DataServiceBean serviceBean) throws ServiceException, DataException {
    	StringBuilder sb =  new StringBuilder();
    	sb.append(new SimpleDateFormat(FILENAME_DATE_FORMAT).format(new Date()))
    				.append("_");
    	if(action.getExportType() == ExportType.CARDS) {
    		Collection<Template> templates = action.getSearch().getTemplates();
    		for(Template template : templates) {
    			Template t = serviceBean.getById(template.getId());
    			sb.append(t.getId().getId());
    			sb.append("-");
    			sb.append(t.getNameRu().replaceAll("\\s+", "_"));
    		}
    	} else sb.append("custom"); //TODO set real name
    	sb.append(".csv").toString();
    	return sb.toString();
    }
    
    private void sendError(HttpServletRequest request, HttpServletResponse response, Throwable t) throws ServletException, IOException 
	{
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		ResourceBundle messages = ResourceBundle.getBundle("com.aplana.dbmi.card.nl.CardPortletResource", request.getLocale());
		try {
			out.println("<html>");
			out.println("<body>");
			out.println("<br>");
			out.println("<font color=\"red\">" + t.getMessage() + "<br></font>");
			out.println("<br>");
			out.println("<br>");

			out.println("<a href=\"javascript: window.close();\">"); 
			out.println(messages.getString("print.page.close.btn"));
			out.println("</a>");

			out.println("</body>");
			out.println("</html>");
		} finally {
			out.close();
		}
	}
}
