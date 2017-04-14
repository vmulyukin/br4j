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
import java.util.Iterator;
import java.util.LinkedHashSet;

public class ListOfDepartments {
	private Connection conn;

	public ListOfDepartments(Connection conn) {
		this.conn = conn;
	}
	
	public String getDepartamentsWithSubordinates(String strParents) throws SQLException {
		if (strParents == null || strParents.isEmpty())
			return strParents;
		LinkedHashSet<Long> result = parserIds(strParents);
		result.addAll(getSubordinates(result));
		return toString(result);
	}
	
	private LinkedHashSet<Long> getSubordinates(LinkedHashSet<Long> parents) throws SQLException {
		LinkedHashSet<Long> subordinates = getDirectSubordinates(parents);
		if (!subordinates.isEmpty())
			subordinates.addAll(getSubordinates(subordinates));
		return subordinates;
	}
	
	private LinkedHashSet<Long> getDirectSubordinates(LinkedHashSet<Long> parents) throws SQLException {
		LinkedHashSet<Long> result = new LinkedHashSet<Long>();
		
		String sql = 
			"select cr.card_id "+
			"from card cr "+
			"inner join attribute_value av_parDep on "+
			"	av_parDep.card_id = cr.card_id "+
			"	and av_parDep.attribute_code = 'JBR_DEPT_PARENT_LINK' "+
			"where av_parDep.number_value in ("+ toString(parents) +"); ";

		ResultSet rs = conn.createStatement().executeQuery(sql);
		while (rs.next()) {
			result.add(rs.getLong(1));
		}
		return result;
	}
	
	private static String toString(LinkedHashSet<Long> ids) {
		StringBuilder buf = new StringBuilder();
		Iterator<Long> iter = ids.iterator();
		while (iter.hasNext()) {
			buf.append(iter.next());
			if (iter.hasNext())
				buf.append(",");
		}
		return buf.toString();
	}
	
	private static LinkedHashSet<Long> parserIds(String ids) {
		LinkedHashSet<Long> result = new LinkedHashSet<Long>();
		for (String id : ids.split(","))
			result.add(Long.valueOf(id.trim()));
		return result;
	}
}
