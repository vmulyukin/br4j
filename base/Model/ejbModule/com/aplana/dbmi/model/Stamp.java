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
package com.aplana.dbmi.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  ��������� ���������������� ������ 
 *
 * @author Vlad Alexandrov
 * @version 1.1
 * @since   2014-12-31
 */

public class Stamp {
	public static final String SERIAL_FIELD = "Serial";
	public static final String OWNER_FIELD = "Owner";
	public static final String NOTBEFORE_FIELD = "NotBefore";
	public static final String NOTAFTER_FIELD = "NotAfter";
	public static final String ORG_NAME_FIELD = "OrgName";

	// ����������� ������ ����� A4 210x297 ��,
	// ��� ������������� c����������� ���������� 595x842 ����� (25,4 �� �������� 72 �����)
	public static final int A4YSizeMM = 297;
	public static final float pointResolution = 72/25.4f;

	private RegistrationData regData;
	private SignatureData sigData;
	
	public RegistrationData getRegData()
	{
		return regData;
	}
	public SignatureData getSigData()
	{
		return sigData;
	}
	public void setRegData(RegistrationData regData)
	{
		this.regData = regData;
	}
	public void setSigData(SignatureData sigData)
	{
		this.sigData = sigData;
	}
	
	public static class SignatureData {
		private Map<String, String> fields = new HashMap<String, String>();
		private Map<String, String> markFields = new HashMap<String, String>();
		private String locationString;
		private boolean markOnly = false;
		private String position;
		
		public String getLocationString() {
			return locationString;
		}
		
		public void setLocationString (String locationString) {
			this.locationString = locationString ;
		}
		public void setSerial(String serial)
		{
			fields.put(SERIAL_FIELD, serial);
		}
		public void setOwner(String owner)
		{
			fields.put(OWNER_FIELD, owner);
		}
		public void setNotBefore(String notBefore)
		{
			fields.put(NOTBEFORE_FIELD, notBefore);
		}
		public void setNotAfter(String notAfter)
		{
			fields.put(NOTAFTER_FIELD, notAfter);
		}
		public String getSerial()
		{
			return fields.get(SERIAL_FIELD);
		}
		public String getOwner()
		{
			return fields.get(OWNER_FIELD);
		}
		public String getNotBefore()
		{
			return fields.get(NOTBEFORE_FIELD);
		}
		public String getNotAfter()
		{
			return fields.get(NOTAFTER_FIELD);
		}
		public Map<String, String> getFields()
		{
			return fields;
		}
		public void setOrgName(String orgName)
		{
			markFields.put(ORG_NAME_FIELD, orgName);
		}
		public Map<String, String> getMarkFields()
		{
			return markFields;
		}

		public boolean isMarkOnly() {
			return markOnly;
		}

		public void setMarkOnly(boolean markOnly) {
			this.markOnly = markOnly;
		}

		public void setPosition(String parameter) {
			this.position = (String)parameter;
			
		}
		
		public String getPosition() {
			return position;
		}
	}


	public static class RegistrationData {

		// ���� �����������
		private String regDate;
		// ��������������� �����
		private String regNum;
		private boolean mainStamp;
		private boolean bottomStamp;
		// ���������� ������
		private int xPosMM;
		private int yPosMM;

		public String getRegDate()
		{
			return regDate;
		}
		public String getRegNum()
		{
			return regNum;
		}
		public int getXPosMM()
		{
			return xPosMM;
		}
		public int getYPosMM()
		{
			return yPosMM;
		}
		public void setRegDate(String regDate)
		{
			this.regDate = regDate;
		}
		public void setRegNum(String regNum)
		{
			this.regNum = regNum;
		}
		public void setXPosMM(int xPosMM)
		{
			this.xPosMM = xPosMM;
		}
		public void setYPosMM(int yPosMM)
		{
			this.yPosMM = yPosMM;
		}
		// - ���������� ���. ������ � ������:
		public float getXPosP(){
			
			return this.getXPosMM() * pointResolution;
		}
		public float getYPosP()
		{
			return (A4YSizeMM - this.getYPosMM()) * pointResolution;
		}
		public boolean isMainStamp() {
			return mainStamp;
		}
		public void setMainStamp(boolean mainStamp) {
			this.mainStamp = mainStamp;
		}
		public boolean isBottomStamp() {
			return bottomStamp;
		}
		public void setBottomStamp(boolean bottomStamp) {
			this.bottomStamp = bottomStamp;
		}
	}
}