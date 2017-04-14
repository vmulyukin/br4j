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
package com.aplana.dbmi.service.impl;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Interface to be implemented by {@link ProcessorBase} descendants which needs 
 * to have direct access to DBMI database.<br>
 * {@link QueryBase Query} object supply own processors with properly
 * initialized JdbcTemplate instance if they implements this interface.<br> 
 * All query objects implements this interface as {@link QueryBase} descendants.<br> 
 */
public interface DatabaseClient
{
	/**
	 * Sets JdbcTemplate instance to be used by this DatabaseClient implementation 
	 * @param jdbc JdbcTemplate to use.
	 */
	public void setJdbcTemplate(JdbcTemplate jdbc);
	//public JdbcTemplate getJdbcTemplate();
}
