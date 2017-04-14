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
package com.aplana.dbmi.model.web;

import com.jenkov.prizetags.tree.impl.Tree;
import com.jenkov.prizetags.tree.itf.ITree;

public class TreeControl extends AbstractControl{
	private Tree tree;
	private Boolean showRoot;

	public Tree getTree() {
		return tree;
	}

	public void setTree(Tree tree) {
		this.tree = tree;
	}
    
	public String getNodeName(){
		return getName() + ".node";
	}

	public Boolean getShowRoot() {
		return showRoot;
	}

	public void setShowRoot(Boolean showRoot) {
		this.showRoot = showRoot;
	}
	
}
