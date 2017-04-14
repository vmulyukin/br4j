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
package com.aplana.dbmi.card.actionhandler;

import com.aplana.dbmi.actionhandler.ActionHandler;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.hierarchy.CardFilterCondition;
import com.aplana.dbmi.model.Card;

/**
 * ������� ����� ��� ������������ ������ � CardPortlet'� 
 */
public abstract class CardPortletActionHandler extends ActionHandler {
	private CardPortletSessionBean sessionBean;
	protected boolean showInViewMode = false;

	protected CardPortletSessionBean getSessionBean() {
		return sessionBean;
	}

	public void setSessionBean(CardPortletSessionBean sessionBean) {
		this.sessionBean = sessionBean;
		if (serviceBean != null){
			if (Long.valueOf(0).equals(serviceBean.getPerson().getId().getId())){
				String traceString = "[TRACE_0_USER] stackTrace: \r\n";
				StackTraceElement[] stack = Thread.currentThread().getStackTrace();
				for (StackTraceElement stackTraceElement : stack) {
					traceString += " " + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")\r\n";
				}
				logger.error(traceString);		
			}
		}
	}
	
	@Override
	public boolean isApplicableForCard(Card c) {
		throw new IllegalStateException(getClass().getName() + " instances couldn't be used in card list attributes");
	}

	@Override
	public boolean isApplicableForUser() {
		final CardFilterCondition condition = getCondition();
		return (condition == null || condition.check(sessionBean.getActiveCard()) );
	}
}
