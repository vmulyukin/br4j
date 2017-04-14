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
package com.aplana.dbmi.service.impl.oracle;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataIntegrityException;
import com.aplana.dbmi.service.impl.ExceptionEnvelope;

/**
 * This class is used by Spring framework to translate checked SQLExceptions thrown 
 * during database access with unchecked DataAccessException descenants.<br>
 * This implementation stores information about error in {@link DataIntegrityException}
 * instance and wraps it with {@link ExceptionEnvelope} to conform spring specification.
 */
public class DBNamesSQLExceptionTranslator extends SQLErrorCodeSQLExceptionTranslator
{
	protected DataAccessException customTranslate(String task, String sql, SQLException sqlEx)
	{
		System.out.println("[DEBUG] A chance to translate exception " + sqlEx +
				" catched while executing " + sql + " during " + task);
		
		Matcher rx;
		switch (sqlEx.getErrorCode()) {
		case 1:
			rx = Pattern.compile("\\([^.]*\\.([^.]*)\\)").matcher(sqlEx.getMessage());
			if (!rx.find())
				return new ExceptionEnvelope(new DataException());
			return new ExceptionEnvelope(
					new DataIntegrityException(DataIntegrityException.MSG_UNIQUE,
							"constraint." + rx.group(1)));
		case 1400:
			rx = Pattern.compile("\\(\"[^\"]*\"\\.\"([^\"]*)\"\\.\"([^\"]*)\"\\)").matcher(sqlEx.getMessage());
			if (!rx.find())
				return new ExceptionEnvelope(new DataException());
			return new ExceptionEnvelope(
					new DataIntegrityException(DataIntegrityException.MSG_NULL,
							"field." + rx.group(1) + "." + rx.group(2)));
		case 2291:
			rx = Pattern.compile("\\([^.]*\\.([^.]*)\\)").matcher(sqlEx.getMessage());
			if (!rx.find())
				return new ExceptionEnvelope(new DataException());
			return new ExceptionEnvelope(
					new DataIntegrityException(DataIntegrityException.MSG_PARENT,
							"constraint." + rx.group(1)));
		}
		
		return super.customTranslate(task, sql, sqlEx);
	}
}
