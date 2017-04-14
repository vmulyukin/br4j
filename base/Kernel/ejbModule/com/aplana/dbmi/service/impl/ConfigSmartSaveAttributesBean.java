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

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ejb.support.AbstractStatelessSessionBean;

import com.aplana.dbmi.service.DataException;

/**
 * ��� � ������������ ��� ������ ��������� smart-save ��� ��������� �� ����� 
 * ���������� ��������.

 * @author rabdullin
 */
public class ConfigSmartSaveAttributesBean
	// extends org.springframework.jndi.JndiObjectLocator
	extends AbstractStatelessSessionBean
	implements javax.ejb.SessionBean, SmartSaveConfig
{
	private static final long serialVersionUID = 1L;

	protected final Log logger = LogFactory.getLog(getClass());

	private boolean enSmartSaveMode = true;
	private boolean enChkWriteByRead = true;
	private String hardSaveAttributes = ""; 
	private ActionOnErrorCheckWriteByRead actionOnErrorCheckWriteByRead = ActionOnErrorCheckWriteByRead.actIgnore;

	private static int g_counter = 0;

	private final int _id = g_counter++;

	public ConfigSmartSaveAttributesBean() {
		super();
	}

	/*
	@Override
	public void afterPropertiesSet() throws IllegalArgumentException,
			NamingException 
	{
		super.afterPropertiesSet();
		// ������� ����� �� ����������������� ����� ...
	}
	*/

	@Override
	protected void onEjbCreate() throws CreateException {
		// super.onEjbCreate();
	}

	/**
	 * @return true, ���� ��������� ������������� smart-save.
	 */
	public boolean isEnSmartSaveMode() throws DataException, RemoteException
	{
		return this.enSmartSaveMode;
	}

	/**
	 * @param true, ����� ��������� ������������� smart-save.
	 */
	public void setEnSmartSaveMode(boolean value) throws DataException, RemoteException
	{
		this.enSmartSaveMode = value;
		logger.info( "set smart save mode to " + (value ? "ENABLED" : "DISABLED"));
	}

	/**
	 * @return true: ��������� ������ �������, �.�. ����� ������ ����������� ������
	 * �� ����-��� ���������� ������ ����������� ���������.
	 */
	public boolean isEnChkWriteByRead() throws DataException, RemoteException
	{
		return this.enChkWriteByRead;
	}

	/**
	 * @param true, ����� �������� ����� �������� ������ ����������� �������.
	 */
	public void setEnChkWriteByRead(boolean value) throws DataException, RemoteException
	{
		this.enChkWriteByRead = value;
		logger.info( "set check write by read mode to " + (value ? "ENABLED" : "DISABLED"));
	}
	
	public void setHardSaveAttributes(String value){
		this.hardSaveAttributes = value;
		logger.info( "set hard save for attributes "+value);
	}

	public String getHardSaveAttributes(){
		return this.hardSaveAttributes;
	}
	/* (non-Javadoc)
	 * @see com.aplana.dbmi.service.impl.SmartSaveManager#getActionOnErrorCheckWriteByRead()
	 */
	public ActionOnErrorCheckWriteByRead getActionOnErrorCheckWriteByRead()
		throws DataException, RemoteException
	{
		return actionOnErrorCheckWriteByRead;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.service.impl.SmartSaveManager#setActionOnErrorCheckWriteByRead(com.aplana.dbmi.service.impl.SmartSaveManager.ActionOnErrorCheckWriteByRead)
	 */
	public void setActionOnErrorCheckWriteByRead(
			ActionOnErrorCheckWriteByRead value) throws DataException, RemoteException 
	{
		this.actionOnErrorCheckWriteByRead = value;
		logger.info( "set action on check write by read error to " + value);
	}

	/* (non-Javadoc)
	 * @see javax.ejb.EJBObject#getEJBHome()
	 */
	public EJBHome getEJBHome() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.ejb.EJBObject#remove()
	 */
	public void remove() throws RemoveException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see javax.ejb.EJBObject#getHandle()
	 */
	public Handle getHandle() {
		return new Handle(){
			private static final long serialVersionUID = 1L;
			public EJBObject getEJBObject() throws RemoteException {
				return ConfigSmartSaveAttributesBean.this;
			}};
	}

	/* (non-Javadoc)
	 * @see javax.ejb.EJBObject#getPrimaryKey()
	 */
	public Object getPrimaryKey() {
		return this._id;
	}

	/* (non-Javadoc)
	 * @see javax.ejb.EJBObject#isIdentical(javax.ejb.EJBObject)
	 */
	public boolean isIdentical(EJBObject obj) {
		if (!(obj instanceof ConfigSmartSaveAttributesBean))
			return false;
		final ConfigSmartSaveAttributesBean other = (ConfigSmartSaveAttributesBean) obj;
		return (this.enSmartSaveMode == other.enSmartSaveMode)
			&& (this.enChkWriteByRead == other.enChkWriteByRead)
			&& (this.actionOnErrorCheckWriteByRead == other.actionOnErrorCheckWriteByRead);
	}

}
