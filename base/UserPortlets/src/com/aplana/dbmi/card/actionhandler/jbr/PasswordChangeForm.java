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
package com.aplana.dbmi.card.actionhandler.jbr;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;

import com.aplana.dbmi.action.GetPersonByCard;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.PortletForm;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataServiceBean;

public class PasswordChangeForm implements PortletForm {
	private static final Log logger = LogFactory.getLog(PasswordChangeForm.class);
	private static final String PASSWORD_CHANGE_JSP = "PasswordChangeForm.jsp";
	public static final String ACTION_CHANGE = "CHANGE_PASSWORD";
	public static final String VALIDATE_PASS_ATTRIBUTE = "VALIDATE_PASSWORD";

	//Indicates whether current password validation is required or not
	private boolean isPassValidationRequired = false;

	public boolean isPassValidationRequired() {
		return isPassValidationRequired;
	}

	public void setPassValidationRequired(boolean isPassValidationRequired) {
		this.isPassValidationRequired = isPassValidationRequired;
	}

	public void doFormView(RenderRequest request, RenderResponse response)
			throws IOException, PortletException {
		PortletSession session = request.getPortletSession();
		request.setAttribute(VALIDATE_PASS_ATTRIBUTE, isPassValidationRequired());
		
		PortletRequestDispatcher rd =
			session.getPortletContext().getRequestDispatcher(CardPortlet.JSP_FOLDER + PASSWORD_CHANGE_JSP);
		
		rd.include(request, response);
		
	}

	public void processFormAction(ActionRequest request, ActionResponse response)
			throws IOException, PortletException {
		try {
			CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
			String action = request.getParameter(CardPortlet.ACTION_FIELD);
			if (CardPortlet.BACK_ACTION.equals(action)) {	
				sessionBean.getActiveCardInfo().getPortletFormManager().closeForm();
			}else if (ACTION_CHANGE.equals(action)) {
				//Process password change action
				boolean needCloseForm = true;
				GetPersonByCard getUserAction = new GetPersonByCard(sessionBean.getActiveCard().getId());
				try {
					DataServiceBean serviceBean = sessionBean.getServiceBean();
					Person person = (Person)serviceBean.doAction(getUserAction);
					if (null != person) {
						if (isPassValidationRequired()){
							//Validate current password first
							String curPassword = request.getParameter("cur_pass");
							try {
								Portal.getFactory().getUserService().validatePassword(person.getLogin(), curPassword);
								changePassword(person, request, sessionBean);
							}catch (Exception e) {
								logger.error("Password is invalid for current user " + person.getLogin(), e);
								sessionBean.setMessageWithType(sessionBean.getResourceBundle()
										.getString("password.validate.fail.msg"), PortletMessageType.ERROR);
								needCloseForm = false;
							}
						}else {
							changePassword(person, request, sessionBean);
						}
					}
					
				}catch (Exception e) {
					logger.error("Cannot get user login.", e);
					sessionBean.setMessageWithType(sessionBean.getResourceBundle()
							.getString("password.change.fail.msg"), PortletMessageType.ERROR);
				}
				if (needCloseForm)
					sessionBean.getActiveCardInfo().getPortletFormManager().closeForm();
			}
		} catch(Exception ex){
			new PortletException("Cannot processFormAction", ex);
		}
	}
	

	private void changePassword(Person person, ActionRequest request, CardPortletSessionBean sessionBean){
		String newPassword = request.getParameter("new_pass");
		try {
			Portal.getFactory().getUserService().changePassword(person.getLogin(), newPassword);
			sessionBean.setMessageWithType(sessionBean.getResourceBundle().getString("password.change.success.msg"), PortletMessageType.EVENT);
		}catch (Exception e) {
			logger.error("Cannot change password for user " + person.getLogin(), e);
			sessionBean.setMessageWithType(sessionBean.getResourceBundle().getString("password.change.fail.msg"), PortletMessageType.ERROR);
		}
	}
}
