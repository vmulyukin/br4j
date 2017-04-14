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
import com.aplana.dbmi.service.DataException;

public class Get2LevelDept {
	private Connection conn = null;
	
	@SuppressWarnings("finally")
	public String print(Connection conn, Long id) {
		String result = "";
		Long depId=null;
		this.conn = conn;
		try {
			depId=get2LevelDept(id);
			Statement stmt = this.conn.createStatement();
			ResultSet rs = stmt.executeQuery("Select string_value as dep_name from attribute_value where card_id="+depId.longValue()+" and attribute_code='NAME'");
			rs.next();
			String deptName = rs.getString("dep_name");
			if (deptName.equals("������� ������������� �������"))
				result="";
			else
				result=deptName+", ";  
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result;
		}
	}
	private Long get2LevelDept(Long idDept) throws DataException, SQLException{
		Long idDepChild=idDept;
		Statement stmt = this.conn.createStatement();
		ResultSet rs = stmt.executeQuery(getSql(idDepChild));
					rs.next();
					Long deptId = rs.getLong("depid");
					if (deptId != -1) {
						rs = stmt.executeQuery(getSql(deptId));
						rs.next();
						Long idDepParrent = rs.getLong("depid");
						if (idDepParrent != -1){
							idDepChild=get2LevelDept(deptId);
						}
					}

		
		return idDepChild;
	}
	
	private String getSql(Long id) {//"+id.longValue()+"
		String sql = 
			" Select "+
			" (case when exists ( "+
			" Select  "+
			" number_value "+
			" from attribute_value "+
			" where card_id="+id.longValue()+" and attribute_code='JBR_DEPT_PARENT_LINK' "+
			" )  "+
			" then ( "+
			" Select  "+
			" number_value "+
			" from attribute_value "+
			" where card_id="+id.longValue()+" and attribute_code='JBR_DEPT_PARENT_LINK' "+
			" )  "+
			" else -1 end "+ 
			" )as depId ";
			
		return sql;
	}

	
}
