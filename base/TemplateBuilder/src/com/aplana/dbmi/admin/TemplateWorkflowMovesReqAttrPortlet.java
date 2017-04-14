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
package com.aplana.dbmi.admin;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.BindException;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.SimpleFormController;

import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.admin.WebTemplateBean;
import com.aplana.dbmi.service.DataException;

public class TemplateWorkflowMovesReqAttrPortlet extends SimpleFormController {

	private final Log logger = LogFactory.getLog(getClass());

	protected Object formBackingObject(PortletRequest request) throws Exception {
		ContextProvider.getContext().setLocale(request.getLocale());

		final WebTemplateBean templateBean = (WebTemplateBean) request.getPortletSession().getAttribute("com.aplana.dbmi.admin.TemplatesPortlet.FORM.templateBean");
		final WebTemplateWorkflowMovesReqAttrBean bean = 
			(WebTemplateWorkflowMovesReqAttrBean) super.formBackingObject(request);
		bean.reset();

		// (!) ����� ���������� WorkflowMoveRequiredFields
		BeanUtils.copyProperties(templateBean, bean, new String[] { "locker", "lockTime" }); 
		try {
			DataServiceBean dataServiceBean = PortletUtil.createService(request);
			
			bean.setWorkflowMoves(dataServiceBean.listChildren(templateBean.getWorkflow(), WorkflowMove.class));
			//bean.setTemplateBlocks(templateBean.getBlocks());
			//bean.setWorkflowMovesRequiredFields(dataServiceBean.listChildren(templateBean.getId(), WorkflowMoveRequiredField.class));

			bean.initShowWorkflowMovesRequiredFields();

		} catch (Exception e) {
			logger.error(e);
			bean.setMessage(e.getMessage());
		}
		return bean;

	}
	
	protected boolean isFormChangeRequest(PortletRequest request) {
		final String tac = request.getParameter("templateApplyClose");
		return (tac == null) || "".equals(tac);
	}
	
	protected void onFormChange(ActionRequest request, ActionResponse response, Object command) throws Exception {
		final WebTemplateWorkflowMovesReqAttrBean bean = (WebTemplateWorkflowMovesReqAttrBean) command;
		bean.processAction();
		bean.initShowWorkflowMovesRequiredFields();
		response.setRenderParameter("portlet_action", "editTemplateWorkflowMovesReqAttr");
	}

	public void onSubmitAction(ActionRequest request, ActionResponse response, Object command, BindException errors) throws Exception {
		ContextProvider.getContext().setLocale(request.getLocale());
		try{
			final WebTemplateBean templateBean = (WebTemplateBean) request.getPortletSession().getAttribute("com.aplana.dbmi.admin.TemplatesPortlet.FORM.templateBean");
			final WebTemplateWorkflowMovesReqAttrBean bean = (WebTemplateWorkflowMovesReqAttrBean) command;
			if (!templateBean.isEditAccessExists()){
				throw new DataException("admin.edit.access.error");
			}
			// (!) ����� ���������� WorkflowMoveRequiredFields
			BeanUtils.copyProperties(bean, templateBean, new String[] { "locker", "lockTime" }); 
	
			if(WebTemplateWorkflowMovesReqAttrBean.APPLY_TEMPLATE.equals(bean.getTemplateApplyClose())){
				;
			} else if(WebTemplateWorkflowMovesReqAttrBean.CLOSE_TEMPLATE.equals(bean.getTemplateApplyClose())){
				;
			}
		} catch (Exception e){
			throw new Exception(e);
		}
		
		// doProcess(request, response, command, templateBean);
	}
	
	protected ModelAndView onSubmitRender(RenderRequest request, RenderResponse response, Object command, BindException errors) throws Exception {
		return showForm(request, response, errors);
	}
	

	/* (non-Javadoc)
	 * @see org.springframework.web.portlet.mvc.AbstractFormController#isFormSubmission(javax.portlet.PortletRequest)
	 */
//	protected boolean isFormSubmission(PortletRequest request) {
//		return !"init".equals(request.getParameter("action"));
//	}

	/**
	 * process request, response
	 * 
	 * @param request
	 * @param response
	 * @param command
	 * @param templateBean
	private void doProcess(ActionRequest request, ActionResponse response, Object command, WebTemplateBean templateBean) {
		//request.getPortletSession().setAttribute("com.aplana.dbmi.admin.TemplatesPortlet.FORM.templateBean", templateBean);
		//final String formAttrName = getFormSessionAttributeName(request);
		//request.getPortletSession().setAttribute(formAttrName, command);
		//response.setRenderParameter("portlet_action", "editTemplateWorkflowMovesReqAttr");
	}
	 */ 

}
