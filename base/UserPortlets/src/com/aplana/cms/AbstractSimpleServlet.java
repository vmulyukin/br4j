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
package com.aplana.cms;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

/**
 * Servlet implementation class ParentCardServlet
 */
public abstract class AbstractSimpleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected Log logger = LogFactory.getLog(getClass());
	private ContentProducer cms = null;
	private ApplicationContext applicationContext = null;
	private ServletContentRequest wrappedReq = null;
	private ServletContentResponse wrappedResp = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AbstractSimpleServlet() {
        super();
       
    }
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");        
        response.setCharacterEncoding("UTF-8");
        initialization(request, response); 
        process(getWrappedReq(), getWrappedResp());

    }
    
    public abstract void process(ServletContentRequest wrappedReq, ServletContentResponse wrappedResp) throws ServletException, IOException;
    
    
    


	private void initialization(HttpServletRequest request, HttpServletResponse response){
    	createRequestResponseWrapper(request, response);
    	cms = new ContentProducer(wrappedReq, wrappedResp, getApplicationContext());
    	
    }
    
    private void createRequestResponseWrapper(HttpServletRequest request, HttpServletResponse response){
        wrappedReq = new ServletContentRequest(request);
		wrappedResp = new ServletContentResponse(request, response);
    }
    
    protected void contentPrinter(String content) throws IOException{
    	PrintWriter out =null;
    	try{
    		out = getWrappedResp().getWriter();
    		out.print(content);
    	}finally{
    		if(out!=null){
    			out.close();
    		}
    	}	    	
    }
    
    protected Object doAction(Action action) throws DataException, ServiceException{
    	return getCms().getService().doAction(action);
    }
    
    
    protected ApplicationContext getApplicationContext(){
    	if(applicationContext==null){
    		applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
    	}    	
    	return applicationContext;
    }
    
    public ContentProducer getCms() {
		return cms;
	}

	public ServletContentRequest getWrappedReq() {
		return wrappedReq;
	}

	public ServletContentResponse getWrappedResp() {
		return wrappedResp;
	}
	
	

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}
	
	
}
