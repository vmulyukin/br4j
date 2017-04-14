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
package com.aplana.dbmi.card.actionhandler.multicard;

import javax.portlet.RenderRequest;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;

public abstract class SpecificCustomStoreHandler implements CardPortletCardInfo.CustomStoreHandler{

	
	protected CardPortletSessionBean sessionBean;
	protected PortletService portletService;

	public SpecificCustomStoreHandler(CardPortletSessionBean sessionBean,
			 RenderRequest request) {
		portletService = Portal.getFactory().getPortletService();
		this.sessionBean = sessionBean;
		processParameters(request);
		preProcessCard();
	}

	protected abstract void preProcessCard();

	protected abstract void processParameters(RenderRequest request) ;

}