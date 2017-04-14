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
package com.aplana.dmsi.types.common;

import javax.xml.datatype.XMLGregorianCalendar;

import com.aplana.dbmi.model.TypeStandard;
import com.aplana.dmsi.types.DMSIObject;
import com.aplana.dmsi.types.Header;

public class Packet extends DMSIObject {

	private Organization sender;
	private TypeStandard type;
	private XMLGregorianCalendar date;
	private String packetUid;
	private String messageUid;
	private String processingResult;
	private Long errorCode;
	private File packetData;
	private Header header;

	public File getPacketData() {
		return this.packetData;
	}

	public void setPacketData(File packetData) {
		this.packetData = packetData;
	}

	public Organization getSender() {
		return this.sender;
	}

	public void setSender(Organization sender) {
		this.sender = sender;
	}

	public TypeStandard getType() {
		return this.type;
	}

	public void setType(TypeStandard type) {
		this.type = type;
	}

	public XMLGregorianCalendar getDate() {
		return this.date;
	}

	public void setDate(XMLGregorianCalendar date) {
		this.date = date;
	}

	public String getPacketUid() {
		return this.packetUid;
	}

	public void setPacketUid(String packetUid) {
		this.packetUid = packetUid;
	}

	public String getMessageUid() {
		return this.messageUid;
	}

	public void setMessageUid(String messageUid) {
		this.messageUid = messageUid;
	}

	public String getProcessingResult() {
		return this.processingResult;
	}

	public void setProcessingResult(String processingResult) {
		this.processingResult = processingResult;
	}

	public Long getErrorCode() {
		return this.errorCode;
	}

	public void setErrorCode(Long errorCode) {
		this.errorCode = errorCode;
	}

	public Header getHeader() {
		return this.header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

}
