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

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.aplana.dbmi.model.ObjectId;

/**
 * ����� ���������� ������� �������� (� ������) ������� {@link QueryBase}
 * ��� ������ Query �������� ����������� �� ���������� � ���� ��������� (�� {@link AsyncDatabaseBeanDecorator})
 * @author desu
 */
@ManagedResource(objectName="br4j:name=activeQueryBases", description="MBean for ActiveQueryBases")
public class ActiveQueryBases {
	public static final String BEAN_NAME = "activeQueryBases".intern();
	private ConcurrentHashMap<ObjectId, QueryBase> queries = new ConcurrentHashMap<ObjectId, QueryBase>();
	
	public void add(QueryBase qb) {
		queries.put(qb.getUid(), qb);
	}
	
	public void remove(QueryBase qb) {
		queries.remove(qb.getUid());
	}
	
	public QueryBase get(ObjectId uid) {
		return queries.get(uid);
	}
	
	@ManagedAttribute(description="Get count of currently running queries")
	public int getSize() {
		return queries.size();
	}
}
