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
import java.util.Calendar;
import java.util.Date;

import javax.jws.WebService;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataService;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.DataServiceHome;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.dbmi.ws.GostStatisticService;
import com.aplana.dbmi.ws.goststatisticservice.GostStatisticResponse;

/**
 * ����� <code>GostStatisticServiceImpl</code> �������� ����������� ���-������� <code>GostStatisticService</code>
 * ������� �������������� � ���������� ������ �� ������������ � ���������� ���������� ���� � ������� �������
 * <p>
 *
 * @author  aklyuev
 */
@WebService(endpointInterface = "com.aplana.dbmi.ws.GostStatisticService")
public class GostStatisticServiceImpl extends ServiceImplBase implements GostStatisticService {

	private Log logger = LogFactory.getLog(getClass());

	@Override
	public GostStatisticResponse getStatistic(Integer period) {
		GostStatisticResponse response = null;
		try {
			Date startTime = Calendar.getInstance().getTime();
			if (logger.isDebugEnabled()) {
				logger.debug("Start collecting gost statistic data at " + startTime);
			}
			response = new GostStatisticResponse();
			response.setSendStatisticResponseList(SendStatisticResponseHelper.getResponseList(getDataServiceBean(), period));
			response.setReceiveStatisticResponseList(ReceiveStatisticResponseHelper.getResponseList(getDataServiceBean(), period));

			if (logger.isDebugEnabled()) {
				Date endTime = Calendar.getInstance().getTime();
				logger.debug("Collecting gost statistic is completed successfully and took " + ((endTime.getTime() - startTime.getTime()) / 1000 + " seconds"));
			}

		} catch (DataException dataEx) {
			logger.error("Following exception occurred during collecting GOST statistic data: ", dataEx);
		} catch (Exception e) {
			logger.error("Unknown error occurred during collecting GOST statistic data: ", e);
		}
		return response;
	}

	public static String convertStreamToString(InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}

}