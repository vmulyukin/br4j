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
package org.aplana.br4j.dynamicaccess.db_export;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.db_export.objects.PermissionObjectDao;
import org.aplana.br4j.dynamicaccess.db_export.subjects.PermissionSubjectDao;

import java.sql.*;

public class AccessRuleStorageDao {

    protected final Log logger = LogFactory.getLog(getClass());

    private AccessRule ar;
    private Connection connection;
	
	    public AccessRuleStorageDao(AccessRule ar, Connection connection) {
        this.ar = ar;
        this.connection = connection;
    }

    public void insertPermission(PermissionObjectDao pObject, PermissionSubjectDao pSubject) throws SQLException {
        if (pSubject == null) throw new IllegalArgumentException("Subject is null");

        if (pObject != null) pObject.setAr(ar);
        pSubject.setAr(ar);

        insertAccessRule();
        pSubject.insertSubject();
        if (pObject != null) pObject.insertObject();

        logger.debug("Rule " + ar + " added");

    }

    private void insertAccessRule() throws SQLException {
        Long id = null;
        // ���� � ������� ��� rule_id (������� �����), �� ���������� ��� ��� �������
        logger.info("try generate new id for rule: template_id="+ar.getTemplateId()+" and status_id="+ar.getStatusId());
    	PreparedStatement statement = connection.prepareStatement(
        	"INSERT INTO access_rule (template_id, status_id, perm_hash) values (?, ?, ?) RETURNING rule_id");
	    statement.setLong(1, ar.getTemplateId());
	    if (ar.getStatusId() == null) {
	        statement.setNull(2, Types.NUMERIC);
	    } else {
	        statement.setLong(2, ar.getStatusId());
	    }
	    statement.setString(3, ar.getPermHash());
	    ResultSet rs = statement.executeQuery();
	    if (rs.next()){
		    id = rs.getLong(1);
	    	if (id==0)
		    	// id �� ������������ => ������ ������
		    	throw new SQLException("Can not generate new rule_id");
	    } else {
	    	// id �� ������������ => ������ ������
	    	throw new SQLException("Can not generate new rule_id");
	    }
	    statement.close();
    	ar.setRuleId(id);
    	return;
    }


}
