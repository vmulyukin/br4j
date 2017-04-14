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
package com.aplana.dbmi.service.impl.query;

import com.aplana.dbmi.action.WriteEventLog;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.LogEventBean;

public class DoWriteEventLog extends ActionQueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;
	private LogEventBean logEventBean = null;

	@Override
	public Object processQuery() throws DataException {
		WriteEventLog eventLog = (WriteEventLog)getAction();
		logEventBean=getLogEventBean();
		if (logEventBean==null || eventLog==null)
			return null;
		
		try {
			logEventBean.logEventExt(getUser(), eventLog.getEntry());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private LogEventBean getLogEventBean(){
		if (!this.getBeanFactory().containsBean(LogEventBean.BEAN_ID)){
            logger.error("Not found bean LogEventBean: "
                    + LogEventBean.BEAN_ID + " !");
            return null;
		}
        return (LogEventBean) this.getBeanFactory().getBean(LogEventBean.BEAN_ID);
	}

}
