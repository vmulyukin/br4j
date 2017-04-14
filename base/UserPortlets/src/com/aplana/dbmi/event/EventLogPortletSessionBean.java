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
package com.aplana.dbmi.event;

import java.util.List;

import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.util.JspUtils;

public class EventLogPortletSessionBean {
	
	
	private DataServiceBean dataServiceBean = null;
	
	private String userLoginStr = null;
	
	private TextMessage message;
	
	private String title = "Event Log Portlet";
	
	
	/***************************************************************************
	 * DEFAULT_X 
	 ***************************************************************************/
	final static boolean DEFAULT_ShowBtnRefresh = true;
	final static boolean DEFAULT_ShowToolbar = true;
	final static boolean DEFAULT_ShowTitle = true;
	
	final static int DEFAULT_DataOffset = 0;
	
	
	private boolean showBtnRefresh = DEFAULT_ShowBtnRefresh;
	private boolean showToolbar = DEFAULT_ShowToolbar;
	private boolean showTitle = DEFAULT_ShowTitle;
	private int dataOffset = DEFAULT_DataOffset;
	
	
	//��������� ������ � ��� ����, ��� ��� ����� �������� �� JSP (���������)
	private List<List<String>> dataList;
	
	//�������
	private List<SearchResult.Column> columns;
	
	
	public enum EventPeriod {
		/**
		 * ������ �� �������
		 */
		TODAY,			
		/**
		 * ������ �� ������� ������� ������ (������� � ���������� ������������)
		 */
		WEEK,
		/**
		 * �� ��� �����
		 */
		ALL
	};
	
	public enum EventResult {
		/**
		 * �������
		 */
		SUCCESS,			
		/**
		 * ���������
		 */
		FAILURE,			
		/**
		 * ���
		 */
		ALL
	};
	
	
	// ����� (�����/����). default - User-mode
	private boolean adminMode = false;
	
	// ���������� �� ���������. default - false
	private boolean showState = false;
	
	// ���������� �� ��������� �� event_log_detail. default - false
	private boolean showMsg = false;
	
	// ������, �� ������� ���������� ������� (�������/������ � ������������/��� �����). default - today
	private EventPeriod period = EventPeriod.TODAY;
		
	// ������� � ������������ ����������� (��������/� ��������/���). default - all
	private EventResult result = EventResult.ALL;
	
	
	/**
	 * �������� ���
	 */
	public void clear() {
		
		this.setShowBtnRefresh(DEFAULT_ShowBtnRefresh);
		this.setShowToolbar(DEFAULT_ShowToolbar);
		this.setShowTitle(DEFAULT_ShowTitle);
		
		this.showState = false;
		this.adminMode = false;
		this.period = EventPeriod.TODAY;
		this.result = EventResult.ALL;
		
		this.dataOffset = DEFAULT_DataOffset;
		this.columns = null; 	// this.columns.clear();
		this.dataList = null;	// this.dataList.clear();

		this.message = null;
		
	}	
	
	
	/*
	 * getters/setters 
	 */
	
	public boolean isAdminMode() {
		return adminMode;
	}

	public void setAdminMode(boolean adminMode) {
		this.adminMode = adminMode;
	}

	public boolean isShowState() {
		return showState;
	}

	public void setShowState(boolean showState) {
		this.showState = showState;
	}

	public EventPeriod getPeriod() {
		return period;
	}

	public void setPeriod(EventPeriod period) {
		this.period = period;
	}

	public EventResult getResult() {
		return result;
	}

	public void setResult(EventResult result) {
		this.result = result;
	}

	public boolean isShowBtnRefresh() {
		return showBtnRefresh;
	}

	public void setShowBtnRefresh(boolean showBtnRefresh) {
		this.showBtnRefresh = showBtnRefresh;
	}

	public boolean isShowToolbar() {
		return showToolbar;
	}

	public void setShowToolbar(boolean showToolbar) {
		this.showToolbar = showToolbar;
	}
	
	public boolean isShowTitle() {
		return showTitle;
	}

	public void setShowTitle(boolean showTitle) {
		this.showTitle = showTitle;
	}

	public DataServiceBean getDataServiceBean() {
		return dataServiceBean;
	}

	public void setDataServiceBean(DataServiceBean dataServiceBean) {
		this.dataServiceBean = dataServiceBean;
	}
	
	public String getUserLoginStr() {
		return this.userLoginStr;
	}

	public void setUserLoginStr(String userLogin) {
		if (JspUtils.equals(this.userLoginStr, userLogin)) return;
		this.userLoginStr = userLogin;
	}
	

	/**
	 * @return the columns
	 */
	public List<SearchResult.Column> getColumns() {
		return columns;
	}

	/**
	 * @param columns the columns to set
	 */
	public void setColumns(List<SearchResult.Column> columns) {
		this.columns = columns;
	}
	
	
	
	public void setErrorMsg(String msg) {
		this.message = new TextMessage(msg, true); 
	}

	public void setTextMessage(String msg) {
		this.message = new TextMessage(msg); 
	}

	public TextMessage getMessage() {
		return message;
	}

	public void setMessage(TextMessage message) {
		this.message = message;
	}
	
	
	/**
	 * @return the dataOffset
	 */
	public int getDataOffset() {
		return dataOffset;
	}
	
	/**
	 * @param dataOffset the dataOffset to set
	 */
	public void setDataOffset(int dataOffset) {
		this.dataOffset = dataOffset;
	}
	
	
	public List<List<String>> getDataList() {
		return dataList;
	}

	public void setDataList(List<List<String>> dataList) {
		this.dataList = dataList;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isShowMsg() {
		return showMsg;
	}

	public void setShowMsg(boolean showMsg) {
		this.showMsg = showMsg;
	}



	static public class TextMessage {
		final String msg;
		final boolean error;

		public TextMessage(String msg, boolean error) {
			super();
			this.msg = msg;
			this.error = error;
		}

		public TextMessage(String msg) {
			this(msg, false);
		}

		public String getMsg() {
			return msg;
		}

		public boolean isError() {
			return error;
		}

	}
}
