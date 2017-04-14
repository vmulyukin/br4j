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
package com.aplana.dbmi.action;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.dbmi.archive.WriteArchiveSqlQueryExecutor;
import com.aplana.dbmi.archive.CardArchiveValue;
import com.aplana.dbmi.model.ObjectId;

/**
 * ���� ��� ������ ������ � �������� ������� card_archive � attribute_value_archive
 * @author ppolushkin
 * @since 19.12.2014
 */
public class AddToArchive implements Action {

	private static final long serialVersionUID = 1L;
	
	private Map<Long, Set<ObjectId>> copyDBValues;
	
	private List<CardArchiveValue> modelValues;
	
	private Class<WriteArchiveSqlQueryExecutor> sqlExecutor;

	public Map<Long, Set<ObjectId>> getCopyDBValues() {
		return copyDBValues;
	}

	public void setCopyDBValues(Map<Long, Set<ObjectId>> copyDBValues) {
		this.copyDBValues = copyDBValues;
	}
	
	public List<CardArchiveValue> getModelValues() {
		return modelValues;
	}

	public void setModelValues(List<CardArchiveValue> modelValues) {
		this.modelValues = modelValues;
	}

	public Class<WriteArchiveSqlQueryExecutor> getSqlExecutor() {
		return sqlExecutor;
	}

	public void setSqlExecutor(Class<WriteArchiveSqlQueryExecutor> sqlExecutor) {
		this.sqlExecutor = sqlExecutor;
	}

	@Override
	public Class<?> getResultType() {
		return null;
	}

}
