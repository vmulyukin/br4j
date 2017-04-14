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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.db_export.BaseAccessDao;
import org.springframework.jdbc.core.RowMapper;

/**
 * DAO for accessing card information.
 * @author atsvetkov
 *
 */
public class CardDao extends BaseAccessDao {
	
	protected final Log logger = LogFactory.getLog(getClass());

	private static final String GET_CARD_IDS_SQL = "Select t.card_id from card t";
	
    public CardDao(DataSource dataSource) {
    	super(dataSource);
    }

    public List<Long> getCardIds(){
    	return getJdbcTemplate().query(GET_CARD_IDS_SQL, new RowMapper<Long>(){
    		
    		public Long mapRow(ResultSet resultSet, int line) throws SQLException {
    			return resultSet.getLong(1);
    		}
    	});    	
    }
}
