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
package com.aplana.dbmi.actionhandler;

import com.aplana.dbmi.card.hierarchy.CardFilterCondition;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import java.util.List;

/**
 * ��� ����������� �����, �������������� ����� ��������� ��������, ������� ����� ��������� � �������
 * @author DSultanbekov
 */
public abstract class ActionHandler {
	protected Log logger = LogFactory.getLog(getClass());	

	protected AsyncDataServiceBean serviceBean;
	private CardFilterCondition condition;
	
	/**
	 * Checks if this action handler is applicable for given user and given parameters (if any exists). 
	 * This method can use member dataService variable, so make sure that you initialized it first.
	 */	
	public boolean isApplicableForUser() {
		return true;
	}
	
	public void setServiceBean(AsyncDataServiceBean serviceBean) {
		this.serviceBean = serviceBean;
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
	
	public abstract void process(List<ObjectId> cardIds, ActionRequest request, ActionResponse response) throws DataException;

	public boolean isApplicableForCard(Card c) {
		return (condition == null) || condition.check(c);
	}

	public CardFilterCondition getCondition() {
		return condition;
	}

	public void setCondition(CardFilterCondition condition) {
		this.condition = condition;
	}
}
