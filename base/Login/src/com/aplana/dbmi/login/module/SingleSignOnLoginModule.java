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
/**
 * 
 */
package com.aplana.dbmi.login.module;

import javax.security.jacc.PolicyContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.portal.identity.UserStatus;
import org.jboss.portal.identity.auth.IdentityLoginModule;

/**
 * Simple extension to standard portal login module; adds primitive SSO support.
 * @author erentsov
 *
 */
public class SingleSignOnLoginModule extends IdentityLoginModule {
	protected boolean validatePassword(String inputPassword, String expectedPassword){
		
		try {
			HttpServletRequest request = (HttpServletRequest) PolicyContext.getContext("javax.servlet.http.HttpServletRequest");
			Object ssoSuccess = request.getAttribute("br4jSsoSuccess");
			if(ssoSuccess != null){
				//TODO Probably this check should be performed inside SSO servlet?
				UserStatus userStatus = getUserStatus(inputPassword);
				
				//Password check doesn't matter until user have permission to login.
				if (userStatus == UserStatus.OK || userStatus == UserStatus.WRONGPASSWORD) {
					log.info("Successful SSO for user" + getUsername());
					return true; 
				}
				else if (userStatus == UserStatus.DISABLE){
					request.setAttribute("org.jboss.portal.loginError", "This account is disabled");
					return false;
				} else if (userStatus == UserStatus.NOTASSIGNEDTOROLE){
					request.setAttribute("org.jboss.portal.loginError", "The user doesn't have the correct role");
					return false;
				} else if (userStatus == UserStatus.UNEXISTING){
					request.setAttribute("org.jboss.portal.loginError", "The user doesn't exist or the password is incorrect");
					return false;
				} else {
					log.error("SSO unexpected error.");
					return false;
				}
			}
		} catch (Exception e){
			log.error("SSO Failure");
			e.printStackTrace();
		}
		
		return super.validatePassword(inputPassword, expectedPassword);
	}
}
