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
package com.aplana.dbmi.service.impl.access;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.AccessCheckerBase;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Access checker used to determine if current user can perform given {@link WorkflowMove}.
 */
public class PerformWorkflowMove extends AccessCheckerBase {
    private AccessRuleManager manager;

    public void setJdbcTemplate(JdbcTemplate jdbc) {
        super.setJdbcTemplate(jdbc);
        manager = new AccessRuleManager(jdbc);
    }

    public boolean checkAccess() throws DataException {
        ChangeState request = (ChangeState) getAction();
        //***** Can request contain fromStateId & toStateId instead of wfmId?
        return manager.isWorkflowMoveAllowed(
                request.getCard().getId(),
                request.getWorkflowMove().getId(),
                getUser().getPerson().getId());
    }
}
