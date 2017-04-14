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

import com.aplana.dbmi.service.*;
import com.aplana.dbmi.ws.ZoneDowProblemsService;
import com.aplana.dbmi.ws.zdproblemsservice.ZoneDowProblemsResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.WebService;
/**
 * ����� <code>ZoneDowProblemsServiceImpl</code> �������� ����������� ���-������� <code>ZoneDowProblemsService</code>
 * ������� �������������� � ���������� ������ �� ������������ ��� �������� "���� ���"
 * <p>
 *
 * @author  echirkov
 */
@WebService(endpointInterface = "com.aplana.dbmi.ws.ZoneDowProblemsService")
public class ZoneDowProblemsServiceImpl extends ServiceImplBase implements ZoneDowProblemsService {

	private Log logger = LogFactory.getLog(getClass());

	@Override
	public ZoneDowProblemsResponse getZDProblemOrgsStat() {
		ZoneDowProblemsResponse response = null;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Start collecting organisation without Zones DOW");
			}
			response = ZoneDowProblemsHelper.getProblemOrganisations(getDataServiceBean());
		} catch (DataException dataEx) {
			logger.error("Following exception occurred during collecting Organisation without Zones DOW statistic data: ", dataEx);
		} catch (Exception e) {
			logger.error("Unknown error occurred during collecting Organisation without Zones DOW statistic data: ", e);
		}
		return response;
	}
}