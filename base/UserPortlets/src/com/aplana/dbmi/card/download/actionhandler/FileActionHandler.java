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
package com.aplana.dbmi.card.download.actionhandler;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * class that handle dispatching from FileCardServlet
 */
public abstract class FileActionHandler {

    protected Log logger = LogFactory.getLog(getClass());

    private DataServiceBean serviceBean;

    public void setServiceBean(DataServiceBean serviceBean) {
        this.serviceBean = serviceBean;
    }

    public DataServiceBean getServiceBean() {
        return serviceBean;
    }

    /**
     * handle dispatching from FileCardServlet
     * implements action execution
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws DataException if the error occur during action execution
     */
    public abstract void process(HttpServletRequest request, HttpServletResponse response) throws DataException;

}
