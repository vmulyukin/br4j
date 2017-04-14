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
package org.aplana.br4j.dynamicaccess.db_export.objects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CardAccessDao extends PermissionObjectDao {
    private String operationCode;

    public CardAccessDao(String operationCode, Connection connection) {
        super(connection);
        this.operationCode = operationCode;
    }

    @Override
    public void insertObject() throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO access_card_rule (rule_id, operation_code) values (?, ?)");
        statement.setLong(1, ar.getRuleId());
        statement.setString(2, operationCode);
        statement.executeUpdate();
    }
}
