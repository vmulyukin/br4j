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
package com.aplana.dbmi.ws.goststatisticservice;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class GostStatisticResponse {

	private List<SendStatisticResponse> sendStatisticResponseList;
	private List<ReceiveStatisticResponse> receiveStatisticResponseList;

	public List<SendStatisticResponse> getSendStatisticResponseList() {
		return sendStatisticResponseList;
	}
	public void setSendStatisticResponseList(
			List<SendStatisticResponse> sendStatisticResponseList) {
		this.sendStatisticResponseList = sendStatisticResponseList;
	}
	public List<ReceiveStatisticResponse> getReceiveStatisticResponseList() {
		return receiveStatisticResponseList;
	}
	public void setReceiveStatisticResponseList(
			List<ReceiveStatisticResponse> receiveStatisticResponseList) {
		this.receiveStatisticResponseList = receiveStatisticResponseList;
	}
}