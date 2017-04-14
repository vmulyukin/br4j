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
package com.aplana.ireferent.actions;

import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.types.TaskReportInternal;
import com.aplana.ireferent.types.WSObject;

public class SetDeclineReasonAction extends ChangeCardAction {

    private static final String DECLINE_REASON_VALUE_PARAM = "declineReason";

    private String declineReasonValue;

    @Override
    public void setParameter(String key, Object value) {
	if (DECLINE_REASON_VALUE_PARAM.equals(key)) {
	    declineReasonValue = String.valueOf(value);
	} else {
	    super.setParameter(key, value);
	}
    }

    @Override
    public void doAction(DataServiceBean serviceBean, WSObject object)
	    throws IReferentException {
	final String declineReason = getDeclineReasonValue();
	final boolean isDeclineReasonDefined = !"".equals(declineReason);
	if (!isDeclineReasonDefined) {
	    return;
	}
	super.doAction(serviceBean, object);
    }

    @Override
    protected WSObject createObject() {
	TaskReportInternal object = new TaskReportInternal();
	object.setDeclineReason(declineReasonValue);
	return object;
    }

    private String getDeclineReasonValue() {
	return declineReasonValue == null ? "" : declineReasonValue.toString()
		.trim();
    }
}
