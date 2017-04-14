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
/**
 * 
 */
package com.aplana.dbmi.service.impl;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

import com.aplana.dbmi.service.DataException;

/**
 * ���������� ������� smart-save ��� ��������� �� ����� ���������� ��������.
 * ��. ����� {@link SmartSaveManager}}
 * @author rabdullin
 */
public interface SmartSaveConfig extends EJBObject
{
	final public static String BEAN_SSMGRCFG = "smartSaveConfig";
	final public static String CONFIG_FOLDER = "dbmi/";

	/**
	 * @return true, ���� ��������� ������������� smart-save.
	 */
	boolean isEnSmartSaveMode() throws DataException, RemoteException;

	/**
	 * @param true, ����� ��������� ������������� smart-save.
	 */
	void setEnSmartSaveMode(boolean value) throws DataException, RemoteException;


	/**
	 * ��������� ���������������� ��������� �� properties-�����. ���������
	 * ����� ������� ������������ ����� {@link CONFIG_FOLDER}
	 * @param fileName
	 * @throws DataException
	 * @throws RemoteException
	 */
	//void setConfigFileName(String fileName) throws DataException, RemoteException;

	/**
	 * �������� ���������������� �������� �� �����.
	 * @param name �������� ��������� (�������� �����������)
	 * @return �������� �� ������������ ��� null, ���� ��� ������.
	 * @throws DataException
	 * @throws RemoteException
	 */
	//String getConfigParameter(String name) throws DataException, RemoteException;

	/**
	 * ������ �������� ����������������� ���������.
	 * @param name �������� ��������� (�������� �����������)
	 * @param value
	 * @throws DataException
	 * @throws RemoteException
	 */
	//void setConfigParameter(String name, String value) throws DataException, RemoteException;

	/**
	 * @return true: ��������� ������ �������, �.�. ����� ������ ����������� ������
	 * �� ����-��� ���������� ������ ����������� ���������.
	 */
	boolean isEnChkWriteByRead() throws DataException, RemoteException;

	/**
	 * @param true, ����� �������� ����� �������� ������ ����������� �������.
	 */
	void setEnChkWriteByRead(boolean value) throws DataException, RemoteException;

	/**
	 * @return ������� ������� �� ������ ��� �������� ����-��� ���������� ������
	 * ���, ������� ������������.
	 */
	ActionOnErrorCheckWriteByRead getActionOnErrorCheckWriteByRead() throws DataException, RemoteException;
	void setActionOnErrorCheckWriteByRead(ActionOnErrorCheckWriteByRead value) throws DataException, RemoteException;
	/**
	 *  @param value - ������ ��������� ����� �������, ��� ������� ��������� ����������� 
	 *  ���������� ��������
	 */
	public void setHardSaveAttributes(String value) throws DataException, RemoteException;
	
	/**
	 * 
	 * @return ������ ��������� ����� �������, ��� ������� ��������� ����������� 
	 *  ���������� ��������
	 */
	public String getHardSaveAttributes() throws DataException, RemoteException;
	/**
	 * ��������� ���������������� ��������� �� properties-�����.
	 * @param fileName
	 * @throws DataException
	 * @throws RemoteException
	 */
//	void loadConfigFile(String fileName) throws DataException, RemoteException;

	/**
	 * ������� ��� ����������� ������ �� ����� ������ ����� ������.
	 * @author rabdullin
	 */
	public enum ActionOnErrorCheckWriteByRead {

		/**
		 * ��������������� (�� ����� �������������) 
		 */
		actIgnore

		/**
		 * ������������� � ������� ����� � ��������� ...
		 */
		, actSwitchToSimpleMode

		/**
		 * ������� ����������
		 */
		, actRaise
		// , actRetry
	}

}
