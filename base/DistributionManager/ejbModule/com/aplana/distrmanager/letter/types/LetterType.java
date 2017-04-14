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
package com.aplana.distrmanager.letter.types;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public enum LetterType {
	
	FOR_SEND {
		public Class<?> clazzLetterType() throws Exception {
			clazzLetterType = Class.forName(Package.getPackage().concat(CLAZZ_LETTER_TYPE));
			return clazzLetterType;
		}
		
		public Object getValue() {
			enumValue = getEnumValue(LetterType.FOR_SEND);
			return enumValue;
		}
	}, 
	SENT {
		public Class<?> clazzLetterType() throws Exception {
			clazzLetterType = Class.forName(Package.getPackage().concat(CLAZZ_LETTER_TYPE));
			return clazzLetterType;
		}
		
		public Object getValue() {
			enumValue = getEnumValue(LetterType.SENT);
			return enumValue;
		}
	},
	NOT_SENT {
		public Class<?> clazzLetterType() throws Exception {
			clazzLetterType = Class.forName(Package.getPackage().concat(CLAZZ_LETTER_TYPE));
			return clazzLetterType;
		}
		
		public Object getValue() {
			enumValue = getEnumValue(LetterType.NOT_SENT);
			return enumValue;
		}
	};
	
	private static Log loggerStatic = LogFactory.getLog(LetterType.class);
	private static final String CLAZZ_LETTER_TYPE = ".LetterType";
	private static Object enumValue = null;
	private static Class<?> clazzLetterType = null;
	
	public abstract Object getValue();
	public abstract Class<?> clazzLetterType() throws Exception;
	
	private static Object getEnumValue(LetterType type) {
		Object enumValue = null;
		try {
			Method valueOfLetterType = clazzLetterType.getMethod("valueOf", new Class[] {String.class});
			enumValue = valueOfLetterType.invoke(null, new Object[]{type.name()});
		} catch (Exception e) {
			loggerStatic.error("jbr.DistributionManager.letter.types.clazzLetterType.valueOf.notFound", e);
		}
		return enumValue;
	}
}
