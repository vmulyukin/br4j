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
package com.aplana.dbmi.model;

public enum ORDMoveCondition {

    ALL_ORD_IN_FINAL_STATE(true, "All top-level commissions and acquaintances are in the final state."),
    NOT_ALL_ORD_IN_FINAL_STATE(false, "NOT all top-level commissions and acquaintances are in the final state."),
    EXEC_EXPIRED_NO_COMMISSION_NO_INFO(true, "Execution date for ORD is expired. " + ORDMoveCondition.DAYS_BEFORE_MOVE + " days left. There are no commissions and acquaintances."),
    EXEC_NOT_EXPIRED_NO_COMMISSION_NO_INFO(false, "Execution date for ORD is NOT expired. " + ORDMoveCondition.DAYS_BEFORE_MOVE + " does NOT left. There are no commissions and acquaintances."),
    INFO_EXPIRED_NO_COMMISSION(true, "There are no commissions. Acquaintances are expired. " + ORDMoveCondition.DAYS_BEFORE_MOVE +" days left."),
    INFO_NOT_EXPIRED_NO_COMMISSION(false, "There are no commissions.  Acquaintances are NOT expired." + ORDMoveCondition.DAYS_BEFORE_MOVE +" days does NOT left.");

    public final static int DAYS_BEFORE_MOVE = 30;

    private String description;
    private boolean passed;

    private ORDMoveCondition(boolean b, String s) {
        description = s;
        passed = b;
    }

    public boolean isPassed() {
        return passed;
    }

    @Override
    public String toString() {
        return description;
    }
}
