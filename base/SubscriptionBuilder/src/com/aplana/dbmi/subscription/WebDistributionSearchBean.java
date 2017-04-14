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
package com.aplana.dbmi.subscription;

import java.util.HashMap;
import java.util.Map;

import com.aplana.dbmi.model.Distribution;
import com.aplana.dbmi.model.web.WebSearchBean;
import com.jenkov.prizetags.tree.itf.ITree;

public class WebDistributionSearchBean extends WebSearchBean{

    private Distribution distribution = new Distribution();
    private ITree tree;
    private Map regions  = new HashMap();

	public Distribution getDistribution() {
		return distribution;
	}

	public void setDistribution(Distribution distribution) {
		this.distribution = distribution;
	}


	public Map getRegions() {
		return regions;
	}

	public void setRegions(Map regions) {
		this.regions = regions;
	}

	public ITree getTree() {
		return tree;
	}

	public void setTree(ITree tree) {
		this.tree = tree;
	}

}
