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

import com.aplana.dbmi.action.ImportCardFromXml.ImportCard.Result;
import com.aplana.dbmi.action.ImportCardFromXml.ImportCard.StatusDescription;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dmsi.action.ExceptionOccurredAction;

public class DoExceptionOccurredProcessing extends ActionQueryBase {

	private static final long serialVersionUID = 1L;

	public static final String EVENT_ID = "EXCEPTION_OCCURRED";

	@Override
	public String getEvent() {
		return EVENT_ID;
	}

	@Override
	public Object processQuery() throws DataException {
		ExceptionOccurredAction action = (ExceptionOccurredAction) getAction();
		Result result = new Result();
		StatusDescription status = new StatusDescription();
		status.setError(action.getErrorMessage());
		result.setStatusDescription(status);
		return result;
	}
}
