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
package com.aplana.dbmi.service.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * ���� �� ������������� ��������� ��� ������������� � ���������������� �����
 * {@link QueryFactory}. <br/> ����������� ��������� ��� ���������:
 * <ul>
 * <li>{@link SelectorFactory} - ������� ��� �������� ����������, �����������
 * ���������</li>
 * <li>���������� �������, ������� ����� ��������� � ���� �������� AND, OR �
 * ���������</li>
 * </ul>
 *
 */
public class ConditionSelector implements Selector {

	public static interface SelectorFactory {
		Selector createSelector(String condition);
	}

	private SelectorFactory selectorFactory;
	private Selector selector;

	public ConditionSelector(SelectorFactory selectorFactory, String condition) {
		if (selectorFactory == null || condition == null) {
			throw new NullPointerException("Constructor arguments should be not null");
		}
		this.selectorFactory = selectorFactory;
		this.selector = parseCondition(condition);
	}

	private Selector parseCondition(String condition) {
		String[] orConditions = condition.split("\\s+[o,O][r,R]\\s+");
		if (orConditions.length > 1) {
			List<Selector> selectors = new ArrayList<Selector>(orConditions.length);
			for (int i = 0; i < orConditions.length; ++i) {
				selectors.add(parseCondition(orConditions[i]));
			}
			return new OrSelector(selectors);
		}
		String[] andConditions = condition.split("\\s+[a,A][n,N][d,D]\\s+");
		if (andConditions.length > 1) {
			List<Selector> selectors = new ArrayList<Selector>(andConditions.length);
			for (int i = 0; i < andConditions.length; ++i) {
				selectors.add(parseCondition(andConditions[i]));
			}
			return new AndSelector(selectors);
		}
		Selector atomicSelector = selectorFactory.createSelector(condition);
		if (atomicSelector == null) {
			throw new IllegalArgumentException("It is impossible to create selector for part of condition " + condition);
		}
		return atomicSelector;
	}

	public boolean satisfies(Object object) {
		return selector.satisfies(object);
	}

}
