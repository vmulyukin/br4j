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
package com.aplana.dbmi.model.search.ext;

import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.model.ObjectId;

public class TriggeredRouteSearchObjectId extends RouteSearchObjectId implements TriggeredObjectId{

	private List<List<RouteSearchNode>> extraRoutes;
	
	private boolean enableExtraAttrIds = true;
	
	public TriggeredRouteSearchObjectId(Class type, Object id,
			String routeSource, String extraRouteSource) {
		super(type, id, routeSource);
		this.extraRoutes = parseRouteSource(extraRouteSource);
	}

	public boolean isEnableExtraAttrIds() {
		return enableExtraAttrIds;
	}

	public void setEnableExtraAttrIds(boolean enableExtraAttrIds) {
		this.enableExtraAttrIds = enableExtraAttrIds;
	}
	
	public List<List<RouteSearchNode>> getRoutes() {
		List<List<RouteSearchNode>> result = new ArrayList<List<RouteSearchNode>>(routes.size()+extraRoutes.size());
		result.addAll(routes);
		if(this.enableExtraAttrIds){
			result.addAll(extraRoutes);
		}
		return result;
	}
}
