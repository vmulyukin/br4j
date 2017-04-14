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
package com.aplana.dbmi.jasperreports;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jboss.portal.api.node.PortalNode;

import com.aplana.dbmi.service.DataException;

public class GetDocFromRes {
	private Connection conn = null;
	
	@SuppressWarnings("finally")
	public Integer getDocId(Connection conn, Integer id) {
		Integer result = null;
		this.conn = conn;
		try {
			result=getId(id);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result;
		}
	}
	private Integer getId(Integer id) throws DataException, SQLException{
		Integer docId=id;
		String sql = " Select template_id from card where card_id="+id.intValue();
		Statement stmt = this.conn.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
					rs.next();
					Integer templateId = rs.getInt("template_id");
					if (templateId == 324) {
						sql = " Select number_value from attribute_value where attribute_code in ('JBR_DOCB_BYDOC', 'JBR_RIMP_PARASSIG') and card_id = "+id.intValue();
						rs = stmt.executeQuery(sql);
						rs.next();
						Integer cardId = rs.getInt("number_value");
						docId=getId(cardId);
					}

		
		return docId;
	}
	
}
