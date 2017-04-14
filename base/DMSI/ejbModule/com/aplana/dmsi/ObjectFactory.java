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
package com.aplana.dmsi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aplana.dbmi.model.TypeStandard;

public class ObjectFactory {

	private static Map<Long, TypeStandard> loadingTypesByThread = new ConcurrentHashMap<Long, TypeStandard>();

	private static TypeStandard getCurrentLoadingType() {
		if (!loadingTypesByThread.containsKey(getId())) {
			throw new IllegalStateException("Loading type for current thread is not defined");
		}
		return loadingTypesByThread.get(getId());
	}

	public static void startWork(TypeStandard loadingType) {
		if (loadingType == null) {
			loadingType = TypeStandard.DELO;
		}
		loadingTypesByThread.put(getId(), loadingType);
	}

	public static void finishWork() {
		loadingTypesByThread.remove(getId());
	}

	private static Long getId() {
		return Thread.currentThread().getId();
	}

	public static com.aplana.dmsi.types.Header createHeader() {
		TypeStandard loadingType = getCurrentLoadingType();
		switch (loadingType) {
		case GOST:
			return new com.aplana.dmsi.types.Header();
		case DELO:
			return new com.aplana.dmsi.types.delo.Header();
		default:
			return new com.aplana.dmsi.types.delo.Header();
		}
	}

	public static com.aplana.dmsi.types.Task createTask() {
		TypeStandard loadingType = getCurrentLoadingType();
		switch (loadingType) {
		case GOST:
			return new com.aplana.dmsi.types.Task();
		case DELO:
			return new com.aplana.dmsi.types.delo.Task();
		default:
			return new com.aplana.dmsi.types.delo.Task();
		}
	}

	public static com.aplana.dmsi.types.ExpansionType createExpansion() {
		TypeStandard loadingType = getCurrentLoadingType();
		switch (loadingType) {
		case GOST:
			return new com.aplana.dmsi.types.GostExpansionType();
		case DELO:
			return new com.aplana.dmsi.types.delo.ExpansionType();
		default:
			return new com.aplana.dmsi.types.delo.ExpansionType();
		}
	}
}
