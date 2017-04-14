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
package com.aplana.dbmi.ws.impl;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.SQLQueryAction;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.ws.zdproblemsservice.ZDOrganisation;
import com.aplana.dbmi.ws.zdproblemsservice.ZoneDowProblemsResponse;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * �������� � �������������� ������ ��� <code>ZoneDowProblemsServiceImpl</code>
 * Created by EChirkov on 30.09.2015.
 */
public class ZoneDowProblemsHelper {
	private static final String ZD_PROBLEMS_SQL = "dbmi/statistics/zoneDowOrgProblems/zoneDowOrgProblemsSQL.sql";
	private static final String checkSQL = "dbmi/statistics/zoneDowOrgProblems/checkCA.sql";
	private static final String ZD_PROBLEMS_FSIN_SQL = "dbmi/statistics/zoneDowOrgProblems/zoneDowOrgProblemsFSIN.sql";

	public static ZoneDowProblemsResponse getProblemOrganisations(DataServiceBean serviceBean) throws DataException, ServiceException {

		SQLQueryAction sqlQueryAction = new SQLQueryAction();
		sqlQueryAction.setSql(getSqlFromFile(checkSQL));
		List<Map> checkResult = serviceBean.doAction(sqlQueryAction);
		Boolean isCa = (Boolean)checkResult.get(0).get("isCA");
		List<Map> problemOrgs = null;
		if(isCa){
			sqlQueryAction.setSql(getSqlFromFile(ZD_PROBLEMS_FSIN_SQL));
		} else {
			sqlQueryAction.setSql(getSqlFromFile(ZD_PROBLEMS_SQL));
		}
		problemOrgs = serviceBean.doAction(sqlQueryAction);

		if(problemOrgs.size() == 0){
			return null;
		}

		Collection<ZDOrganisation> organisations = new ArrayList<ZDOrganisation>(problemOrgs.size());

		for(Map<String,Object> row : problemOrgs){
			ZDOrganisation org = new ZDOrganisation();
			org.setCard_id(((BigDecimal) row.get("card_id")).longValue());
			org.setTemplate((String) row.get("template"));
			org.setStatus((String) row.get("status"));
			org.setName((String) row.get("name"));
			organisations.add(org);
		}
		return new ZoneDowProblemsResponse(organisations);
	}

	private static String getSqlFromFile(String path){
		InputStream is = null;
		String sql = null;
		try {
			is = Portal.getFactory().getConfigService().loadConfigFile(path);
			sql = IOUtils.toString(is, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(is);
		}
		return sql;
	}
}
