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
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.ws.goststatisticservice.ReceiveStatisticResponse;

/**
 * ����� <code>ReceiveStatisticResponseHelper</code> �������� ��������������� ��� <code>GostStatisticServiceImpl</code>.
 * �������� �� ���� � ���������� ������ � ���������� ���������� ����
 * <p>
 *
 * @author  aklyuev
 */
public class ReceiveStatisticResponseHelper {
	private static final String RECEIVE_GOST_STATISTIC_SQL = "dbmi/gost/receiveGOSTStatisticSQL.sql";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List<Map> getRows(DataServiceBean serviceBean, Integer period) throws Exception {
		InputStream sqlFileStream = null;
		try {
			sqlFileStream = Portal.getFactory().getConfigService().loadConfigFile(RECEIVE_GOST_STATISTIC_SQL);

			String sql = GostStatisticServiceImpl.convertStreamToString(sqlFileStream);
			SQLQueryAction action = new SQLQueryAction();
			action.setSql(sql);
			final MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("days", period, SQLQueryAction.PARAM_NUMBER);
			action.setParams(params);

			return (List<Map>) serviceBean.doAction(action);
		} finally {
			IOUtils.closeQuietly(sqlFileStream);
		}
	}

	public static List<ReceiveStatisticResponse> getResponseList(DataServiceBean serviceBean, Integer period) throws DataException {
		List<ReceiveStatisticResponse> response = null;
		try {
			@SuppressWarnings("rawtypes")
			List<Map> rows = getRows(serviceBean, period);
			if (rows != null) {
				response = new ArrayList<ReceiveStatisticResponse>(rows.size());

				for (Map row : rows) {

					ReceiveStatisticResponse resp = new ReceiveStatisticResponse();

					resp.setUuid((String) row.get("uuid"));
					resp.setStatus(((java.math.BigDecimal) row.get("status")).longValue());
					resp.setCreated((Date) row.get("created"));
					resp.setNotifReceivedId((String) row.get("notif_received_uuid"));
					resp.setNotifReceivedCreated((Date) row.get("notif_received_created"));
					resp.setNotifRegId((String) row.get("notif_registered_uuid"));
					resp.setNotifRegCreated((Date) row.get("notif_registered_created"));
					resp.setIncomingCreated((Date) row.get("incoming_created"));
					resp.setIncomingRegNum((String) row.get("incoming_regnum"));
					resp.setIncomingRegistered((Date) row.get("incoming_regdate"));
					resp.setIncomingRegRejectReason((String) row.get("incoming_reason"));

					response.add(resp);
				}
			}
		}
		catch (Exception ex) {
			throw new DataException("Exception occurred during collecting receive gost statistic data", ex);
		}

		return response;
	}
}
