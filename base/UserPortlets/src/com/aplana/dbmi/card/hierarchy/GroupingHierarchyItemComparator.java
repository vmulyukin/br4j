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
package com.aplana.dbmi.card.hierarchy;

import java.util.Comparator;

import com.aplana.dbmi.model.LocalizedString;

public class GroupingHierarchyItemComparator implements Comparator {
	private boolean descending;
	
	public GroupingHierarchyItemComparator(boolean descending) {
		this.descending = descending;
	}
	
	public int compare(Object obj1, Object obj2) {
		LocalizedString label1 = ((GroupingHierarchyItem)obj1).getLabel(),
			label2 = ((GroupingHierarchyItem)obj2).getLabel();
		if (label1 == null && label2 != null) {
			// ������ ��� ���������� ������ ��������� ����� ������ ������
			return 1;
		} else if (label1 != null && label2 == null) {
			// ������ ��� ���������� ������ ��������� ����� ������ ������
			return -1;
		} else if (label1 == null && label2 == null) {
			return 0;
		} else {
			int result = label1.compareToIgnoreCase(label2); 
			return descending ? - result : result; 
		}
	}
}
