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

import java.util.Map;

import javax.portlet.RenderRequest;

import com.aplana.dbmi.card.CardPortletSessionBean;

public class SpecificCustomStoreHandlerFactory {

	public final static String GROUP_EXECUTION_ACTION = "GROUP_EXECUTION";
	public final static String GROUP_RESOLUTION_ACTION = "GROUP_RESOLUTION";
	
	public final static String GROUP_EXECUTION_TEMPLATE = "1045";
	public final static String GROUP_RESOLUTION_TEMPLATE = "325";

	public static SpecificCustomStoreHandler getCustomStoreHandler(
			String action, CardPortletSessionBean sessionBean, RenderRequest request) {
		if (GROUP_EXECUTION_ACTION.equals(action)) {
			return new GroupExecutionHandler(sessionBean, request);
		} else if (GROUP_RESOLUTION_ACTION.equals(action)) {
			return new GroupResolutionHandler(sessionBean, request);
		} else
			return null;
	}
}
