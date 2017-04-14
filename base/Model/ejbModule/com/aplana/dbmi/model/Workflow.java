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
package com.aplana.dbmi.model;

import java.util.List;

/**
 * Workflow is a set of rules which defines possible {@link CardState stages of processing}
 * and its order for {@link Card cards}. Each Workflow consists of the set of {@link WorkflowMove moves},
 * and defines {@link #getInitialState() inital state} for cards processed by this workflow.
 * Workflow is assigned to {@link Template templates} and {@link Card} uses workflow assigned to
 * card's template. 
 */
public class Workflow extends LockableObject {
	private static final long serialVersionUID = 3L;
	
	private LocalizedString name;
	private boolean active;
	private ObjectId initialState;
	private List moves;
	
	/**
	 * Default constructor
	 */
	public Workflow() {
		name = new LocalizedString();
		active = true;
	}

	/**
	 * Returns list of {@link WorkflowMove} instances which belongs to this {@link Workflow}
	 * @return list of {@link WorkflowMove} instances
	 */
	public List getMoves() {
		return moves;
	}

	/**
	 * Sets list of {@link WorkflowMove moves} which comprises this {@link Workflow}
	 * @param moves list of {@link WorkflowMove} instances
	 */
	public void setMoves(List moves) {
		this.moves = moves;
	}

	/**
	 * Returns identifier of initial {@link CardState} for cards processed by this workflow
	 * @return identifier of initial {@link CardState} for cards processed by this workflow
	 */
	public ObjectId getInitialState() {
		return initialState;
	}

	/**
	 * Sets identifier of initial {@link CardState} for cards processed by this workflow
	 * @param initialState identifier of initial {@link CardState} for cards processed by this workflow
	 */
	public void setInitialState(ObjectId initialState) {
		this.initialState = initialState;
	}
	
	/**
	 * Returns localized name of workflow
	 * @return localized name of workflow
	 */
	public LocalizedString getName() {
		return name;
	}

	/**
	 * Checks if this {@link Workflow} object is active.
	 * @return value of isActive flag
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Sets value of isActive flag
	 * @param active desired value of isActive flag 
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
}
