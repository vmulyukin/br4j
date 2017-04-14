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
package com.aplana.agent.plugin;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import com.aplana.agent.conf.routetable.Node;

/**
 * common plugin interface
 */
public interface Plugin {
	public final static String TRANSPORT_AGENT_UUID = "transport-agent.uuid";
	public final static String TRANSPORT_AGENT_NAME = "transport-agent.name";
	public final static String AGENT_NAME = "agent.name";
	public final static String AGENT_ID = "agent.id";

	/**
	 * ��������� ��������� ������ ������� �� ���������� ���������� �����, ���������� � ��������� �� ��� ���������� ������.	
	 * @param config ���� � ����������� ������ �������.
	 */
	public void setConfiguration(File config);

	/**
	 * 	��������� ���������� ��������� �������.
	 * @return ����� ���������� ���������.
	 */
	public Properties getEnvironment();

	/**
	 * 	��������� ���������� ��������� ��� �������.
	 * @param envMap - ����� ���������� ���������.
	 */
	public void setEnvironment(Properties envMap);
	
	/**
	 * �������� ��� ��������� (�����) �� ���������� ������������� ���� � �������� � � ���������.
	 * @param node �������� �����
	 * @return ���� � ���, �������������� �� ������ �������� ��������.
	 * @throws GetMailException � ������ ����������� ������, ��������� � �������������� �������� ���������;
	 * PluginException � ������ ��������� ������, � �������, �������������� ���������� ������ � ����������� � ��.
	 */
	public boolean getMail(Node node) throws PluginException;

	/**
	 * ���������� ��������� ��������� (����) �� ���������� (���� ������� ������, ��� ��� ����������� ������ URL) 
	 * � ��������� ����� ����������. 
	 * @param letter URL ���������
	 * @param destination ������������ ����, ����������� ������� ����� ����������
	 * @return ���� � ���, �������������� �� ������ �������� ��������.
	 * @throws SendMailException � ������ ����������� ������, ��������� � �������������� �������� ������ ����������;
	 * PluginException � ������ ��������� ������, � �������, �������������� ���������� ������ � ����������� � ��.
	 */
	public boolean sendMail(URL letter, Node destination) throws PluginException;
	
	/**
	 * ������� ������� ��� ������ ������ �������.
	 * ������, ��������, ������� ��� ������ ������� ��������� �� �������� 
	 * folder.lock � folder.queued (��. ���������� {@link Router}.
	 * FileMover ������ destination ����� �� �������� ����������� (�� �� ����� ����������) ������. 
	 * @param node ����, � ������� ������� �������
	 * @throws PluginException
	 */
	public void cleanResources(Node node) throws PluginException;
}
