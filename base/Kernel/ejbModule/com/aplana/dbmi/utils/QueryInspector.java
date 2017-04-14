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
package com.aplana.dbmi.utils;

import com.aplana.dbmi.service.impl.QueryBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedList;

/**
 * ��������� ������������� query. �������� ���������� ��� ���� ������������ �����������
 * � ��������� query � ���� ������ ������� � ��������� ������� ������ ������� �������.
 * ������������ ��� ������� � ��� ����������.
 * ��� ������������� ���������� �������� TRACE ������� ����������� ��� �������� ������.
 * �� ������, ��������������, �������� �� ������ (TRACE ������ ���� ��������).
 */
public class QueryInspector {
	private static Log logger = LogFactory.getLog(QueryInspector.class);

	private QueryInspector() {}

	private static class QueryNode {
		String name;
		long level;
	}

	private static ThreadLocal<LinkedList<QueryNode>> container = new ThreadLocal<LinkedList<QueryNode>>() {
		@Override
		protected LinkedList<QueryNode> initialValue() {
			return new LinkedList<QueryNode>();
		}
	};

	public static void start(Object qb, String prefix, long level) {
		if (isInspectorDisabled()) {
			return;
		}
		LinkedList<QueryNode> queue = container.get();

		QueryNode node = new QueryNode();
		String name = prefix + " : { " + qb.getClass().getName();
		if (qb instanceof QueryBase) {
			name += " (" + ((QueryBase)qb).getUid().getId() + ")";
		}
		node.level = level;
		node.name = name + "\n";
		queue.offer(node);
	}

	public static void start(Object qb, String prefix) {
		start(qb, prefix, 0l);
	}

	public static void end(long time, long level) {
		if (isInspectorDisabled()) {
			return;
		}
		LinkedList<QueryNode> queue = container.get();

		QueryNode node = new QueryNode();
		String name = "} //" + time + " ms\n";
		node.level = level;
		node.name = name;
		queue.offer(node);
	}

	public static void end(long time) {
		end(time, 0l);
	}

	public static void log() {
		if (isInspectorDisabled()) {
			return;
		}
		StringBuilder sb = new StringBuilder("\n");

		LinkedList<QueryNode> queue = container.get();
		for (QueryNode node : queue) {
			for (int i=0; i<node.level; i++) {
				sb.append("\t");
			}
			sb.append(node.name);
		}

		logger.trace(sb.toString());
		container.remove();
	}

	private static boolean isInspectorDisabled() {
		return !logger.isTraceEnabled();
	}
}
