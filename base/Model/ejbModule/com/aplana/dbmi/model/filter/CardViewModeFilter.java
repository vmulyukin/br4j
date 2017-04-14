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
package com.aplana.dbmi.model.filter;

import com.aplana.dbmi.model.ObjectId;

public class CardViewModeFilter implements Filter {

	private static final long serialVersionUID = 1L;
    private ObjectId viewMode;

	/**
	 * @param viewMode - ����� ����������� ��� ������������ ���������� ����������� ���������, �������.
	 */
	public CardViewModeFilter(ObjectId viewMode) {
		this.viewMode = viewMode;
	}

	public ObjectId getViewMode() {
		return viewMode;
	}
	public void setViewMode(ObjectId viewMode) {
		this.viewMode = viewMode;
	}
	/**
	 * Checks if this CardViewModeFilter is equal to given one.
	 * @param obj object to compare with
	 * @return true if type and identity information of compared filters are equal.
	 * Returns false if obj is not CardViewModeFilter instance or different filter 
	 */
	public boolean equals(Object obj)
	{
		if (obj == this) 
			return true;
		if (!(obj instanceof CardViewModeFilter)) 
			return false;
		final CardViewModeFilter other = (CardViewModeFilter) obj;
		return 	((viewMode != null) ? viewMode.equals(other.getViewMode()) : null == other.getViewMode());
	}

	@Override
	public int hashCode() {
		return 	( (viewMode != null) ? viewMode.hashCode() : 45634527);
	}
}
