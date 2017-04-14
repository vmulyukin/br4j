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
import java.util.*;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.SearchUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;


public class OrderWithAttachmentList extends CardPortlet implements PortletForm{
	public static final String UPLOAD_FORM_JSP = "/WEB-INF/jsp/orderWithAttachmentList.jsp";
	public static final String ACTION_FIELD = "MI_ACTION_FIELD";
	public static final String BACK_ACTION = "MI_BACK_ACTION";
	public static final String SAVE_AND_CLOSE_EDIT_MODE_ACTION = "MI_SAVE_AND_CLOSE_EDIT_MODE_ACTION";
	public static final ObjectId RES_ATTACH = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	public static final ObjectId ATTR_DOC_ATTACH = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	public static final String FIELD_ADD_PREFIX = "add_";
	private PortletService portletService;
	
	private Log logger = LogFactory.getLog(getClass());
    private DataServiceBean serviceBean;
    public static final ObjectId ATTR_RESOLUTION = ObjectId.predefined(BackLinkAttribute.class, "jbr.resolutions");
    public static final ObjectId ATTR_NAME = ObjectId.predefined(StringAttribute.class, "name");
    public static final ObjectId ATTR_RESOLUT = ObjectId.predefined(TextAttribute.class, "jbr.resolutionText");
    public static final ObjectId ATTR_SUBRESOLUTION = ObjectId.predefined(BackLinkAttribute.class, "jbr.linkedResolutions");
    public static final ObjectId ATTACH_AUTHOR = ObjectId.predefined(PersonAttribute.class, "personattribute.author");

    public void doFormView(RenderRequest request, RenderResponse response)
			throws IOException, PortletException {
    	 buildInfoDoc(request);
		PortletRequestDispatcher rd = request.getPortletSession().getPortletContext()
		.getRequestDispatcher(UPLOAD_FORM_JSP);
		rd.include(request, response);
	}

	public void processFormAction(ActionRequest request, ActionResponse response)
			throws IOException, PortletException {
	//	super.processAction(request, response);
		final String action = request.getParameter(ACTION_FIELD);	
		if (BACK_ACTION.equals(action)) {
			backActionHandler(request, response);
		} 
		else if (SAVE_AND_CLOSE_EDIT_MODE_ACTION.equals(action)) {
			try {
			 String[] params = (String[])	request.getParameterMap().get("checkbox");
		  		CardPortletSessionBean sessionBeen = CardPortlet.getSessionBean(request);
		        Card doc = sessionBeen.getActiveCard();
		        CardLinkAttribute attrLink= (CardLinkAttribute)doc.getAttributeById(ATTR_DOC_ATTACH);
		        ArrayList attachArray=new ArrayList();
		           	for (int k=0; k<params.length; k++){
		        		attachArray.add(new ObjectId(Card.class, params[k]));
		        	}
		        	attrLink.addIdsLinked(attachArray);
		        sessionBeen.getServiceBean().saveObject(doc);
				backActionHandler(request, response);
			} catch (DataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	protected void backActionHandler(ActionRequest request, ActionResponse response)
			throws IOException
	{
		CardPortletSessionBean sessionBean = getSessionBean(request);
		sessionBean.getActiveCardInfo().getPortletFormManager().closeForm();
	}

	public void buildInfoDoc(RenderRequest request) {
  		ArrayList attInfo = new ArrayList(); 
  		CardPortletSessionBean sessionBeen = CardPortlet.getSessionBean(request);
        Card doc = sessionBeen.getActiveCard();
  		try {
  			final List<ObjectId> linkRes = SearchUtils.getBackLinkedCardsObjectIds(doc, ATTR_RESOLUTION, serviceBean);
  			if (linkRes != null && linkRes.size() > 0) {
  				for (int i = 0; i < linkRes.size(); i++) {
  					buildInfoResolutionAll(linkRes.get(i),request,attInfo);
  				}
  				request.setAttribute("resInfo", attInfo);

  			}
  		} catch (Exception e) {
  			logger.error("Error in run CourseExecutionHandler when get data from resolutions and reports", e);
  		}
  	}
  	
	private void buildInfoResolutionAll(ObjectId resId,RenderRequest request, ArrayList attInfo) throws DataException, ServiceException {
  		CardPortletSessionBean sessionBeen = CardPortlet.getSessionBean(request);
  		Card res = (Card) sessionBeen.getServiceBean().getById(resId);
  		CardLinkAttribute linkAttach = (CardLinkAttribute) res.getAttributeById(RES_ATTACH);
  		if (linkAttach != null) {
  			AttachInfo attInfoObject= new AttachInfo();
  			Iterator iterAttach = linkAttach.getIdsLinked().iterator();
  			while (iterAttach.hasNext()) {
   				ObjectId attachId = (ObjectId) iterAttach.next();
  				Card attachmentCard = (Card) sessionBeen.getServiceBean().getById(attachId);
  				String attachName = ((StringAttribute) attachmentCard.getAttributeById(ATTR_NAME)).getValue();
  				attInfoObject.setAttachText(attachName);
  				attInfoObject.setAttId(attachId.getId().toString());
  				attInfo.add(attInfoObject);
 			} 		
 // 			String resName = ((StringAttribute) res.getAttributeById(ATTR_RESOLUT)).getValue();
  		}
  			//System.out.println("-----" + resName + "-----");
  			final List<ObjectId> linkSubres = SearchUtils.getBackLinkedCardsObjectIds(res, ATTR_SUBRESOLUTION, serviceBean);
  			if (linkSubres != null) {
  				Iterator iter = linkSubres.iterator();
  				while (iter.hasNext()) {
  					ObjectId subresId = (ObjectId) iter.next();
  					buildInfoResolutionAll(subresId,request,attInfo);
  				}	
  			}
   	}
 

}
