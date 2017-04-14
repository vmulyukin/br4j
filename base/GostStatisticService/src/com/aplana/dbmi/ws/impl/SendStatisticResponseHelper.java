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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.SQLQueryAction;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.ws.goststatisticservice.SendStatisticResponse;
import com.aplana.dmsi.Configuration;

/**
 * ����� <code>SendStatisticResponseHelper</code> �������� ��������������� ��� <code>GostStatisticServiceImpl</code>.
 * �������� �� ���� � ���������� ������ �� ������������ ���������� ���� � ������� �������
 * <p>
 *
 * @author  aklyuev
 */
public class SendStatisticResponseHelper {
	private static final String SEND_GOST_STATISTIC_SQL = "dbmi/gost/sendGOSTStatisticSQL.sql";

	private static List<Map> getRows(DataServiceBean serviceBean, Integer period) throws Exception {
		InputStream sqlFileStream = null;
		try {
			ObjectId senderOrgId = Configuration.instance().getDefaultOrganizationId();
			Long defaultOrgId = (senderOrgId == null) ? 1 : (Long) senderOrgId.getId();

			sqlFileStream = Portal.getFactory().getConfigService().loadConfigFile(SEND_GOST_STATISTIC_SQL);

			String sql = GostStatisticServiceImpl.convertStreamToString(sqlFileStream);
			
			SQLQueryAction action = new SQLQueryAction();
			action.setSql(sql);
			final MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("days", period, SQLQueryAction.PARAM_NUMBER);
			params.addValue("defaultOrgId", defaultOrgId, SQLQueryAction.PARAM_NUMBER);
			action.setParams(params);

			return (List<Map>) serviceBean.doAction(action);
		} finally {
			IOUtils.closeQuietly(sqlFileStream);
		}
	}

	public static List<SendStatisticResponse> getResponseList(DataServiceBean serviceBean, Integer period) throws DataException {
		List<SendStatisticResponse> response = null;
		try {

			List<Map> rows = getRows(serviceBean, period);
			if (rows != null) {
				response = new ArrayList<SendStatisticResponse>(rows.size());

				for (Map row : rows) {

					SendStatisticResponse resp = new SendStatisticResponse();

					resp.setUuid((String) row.get("uuid"));
					resp.setElmStatus(((java.math.BigDecimal) row.get("elm_status")).longValue());
					resp.setElmStatusName((String) row.get("elm_status_name"));
					resp.setBasedocTemplate(((java.math.BigDecimal) row.get("basedoc_template")).longValue());
					resp.setBasedocStatus(((java.math.BigDecimal) row.get("basedoc_status")).longValue());
					resp.setElmCreatedDate((Date) row.get("elm_created"));
					resp.setSenderOrgFullName((String) row.get("sender_org_fullname"));
					resp.setDestOrgFullName((String) row.get("dest_org_fullname"));
					resp.setBasedocRegDate((Date) row.get("basedoc_regdate"));
					resp.setBasedocRegNumber((String) row.get("basedoc_regnumber"));
					resp.setGostMessageCreateTime((Date) row.get("gost_created"));
					resp.setDefaultOrgFullName((String) row.get("default_org_fullname"));
					resp.setNotifReceivedCreated((Date) row.get("notif_received_created"));
					resp.setNotifRegisteredCreated((Date) row.get("notif_registered_created"));

					response.add(resp);
				}
			}
		}
		catch (Exception ex) {
			throw new DataException("Exception occurred during collecting send gost statistic data", ex);
		}

		return response;
	}
}