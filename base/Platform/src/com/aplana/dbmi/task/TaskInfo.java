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
package com.aplana.dbmi.task;

import java.util.Date;

public abstract class TaskInfo
{
	private String id;
	private String moduleName;
	private String methodName;
	private Object[] args;
	private Class[] argTypes;
	protected Date start;
	private String info;
	private String xmlConf;
	private boolean persistent;
	private Date lastExecTime;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getModuleName() {
		return moduleName;
	}
	
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;

	}
	
	public Date getStart() {
		return start;
	}
	
	public void setStart(Date start) {
		this.start = start;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	public String getInfo() {
		return info;
	}
	
	public void setInfo(String info) {
		this.info = info;
	}
	
	public void setArgs(Object[] args)
	{
		this.args = args;
	}
	
	public void setArgTypes(Class[] argTypes)
	{
		this.argTypes = argTypes;
	}
	
	public void setXmlConfig(String xml)
	{	
		this.xmlConf = xml;
	}
	
	public String getXmlConfig()
	{	
		return xmlConf;
	}
	
	public Object[] getArgs()
	{
		return args;
	}
	
	public Class[] getArgTypes()
	{
		return argTypes;
	}
	
	public void setPersistent( boolean persistent) {
		this.persistent = persistent;

	}
	
	public boolean isPersistent() {
		return persistent;
	}
	
	
	public abstract String getRepeatIntervalStr();

	public abstract String getCronExpr();

	public Date getLastExecTime() {
		return lastExecTime;
	}

	public void setLastExecTime(Date lastExecTime) {
		this.lastExecTime = lastExecTime;
	}

}
