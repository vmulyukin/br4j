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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <code>TaskInfoBuilder</code> is used to instantiate {@link TaskInfo}.
 * 
 * <p>The builder will try to keep itself in a valid state, with 
 * reasonable defaults set for calling build() at any point.  For instance
 * if you do not invoked <i>withIdentity(..)</i> a trigger id will be generated
 * for you.</p>
 *  
 * 
 * <p>Client code can be such as this:</p>
 * <pre>
 *		TaskInfoBuilder.newTaskInfo()
				.forJob(name)
				.withSchedule(cronExpr)
				.withInfo(info)
				.startAt(start.getTime()).build()
 * <pre>
 */

public class TaskInfoBuilder<T extends TaskInfo> {
	private String taskId;
    private Date startTime = new Date();
    private Date lastExecTime;
	private String moduleName;
	private String methodName = "process";
	private String info;
    private String cronExpr;
	private long interval = 0;
	private String unit= IntervalTaskInfo.UNIT_SECOND;
    private TaskInfo taskInfo;
    private boolean persist = true;
	private Map<String, String> argMap = new HashMap<String, String>();
	private String xmlConf;

	public static final String MAP_ARG_CONFIG = "config";
	public static final String CURR_TASK_ID = "current_task_id";
    
    private TaskInfoBuilder() {

    }
    
    /**
     * Create a new TaskInfoBuilder with which to define a 
     * specification for a TaskInfo.
     * 
     * @return the new TriggerBuilder
     */
    public static TaskInfoBuilder<TaskInfo> newTaskInfo() {
        return new TaskInfoBuilder<TaskInfo>();
    }
	
    /**
     * Produce the <code>TaskInfo</code>.
     * 
     * @return a TaskInfo that meets the specifications of the builder.
     */
	 @SuppressWarnings("unchecked")
	public T build() {

	   if(cronExpr != null)
            taskInfo = new CronTaskInfo(cronExpr);
	   else
			taskInfo = new IntervalTaskInfo(interval, unit);

       if (taskId == null)
       {
    	   taskId = generateUniqueTaskId();
       }
       //add current task id to args map
	   this.argMap.put(CURR_TASK_ID, taskId);
	   
	   taskInfo.setId(taskId);
	   taskInfo.setModuleName(moduleName);
	   taskInfo.setStart(startTime);
	   taskInfo.setMethodName(methodName);
	   taskInfo.setInfo(info);
	   taskInfo.setPersistent(persist);
	   taskInfo.setXmlConfig(this.xmlConf);
	   
	   taskInfo.setArgs(new Object[] { argMap });
	   taskInfo.setArgTypes(new Class[] { Map.class });
	   
	   taskInfo.setLastExecTime(lastExecTime);
   
       return (T) taskInfo;
    }

	 /**
	     * Use a <code>TaskInfo</code> with the given id to
	     * identify the TaskInfo.
	     * 
	     * <p>If none of the 'withIdentity' methods are set on the TaskInfoBuilder,
	     * then a random, unique id will be generated.</p>
	     * 
	     * @param the id element for the TaskInfo id
	     * @return the updated TaskInfoBuilder
	     * @see TaskInfo#getId()
	     */
	 
	public TaskInfoBuilder<T> withIdentity(String id) {
	    this.taskId = id;
	    return this;
	}

    /**
     * Set the module name which should be fired
     * 
     * @param moduleName the name of the job to fire. 
     * @return the updated TaskInfoBuilder
     * @see TaskInfo#getModuleName()
     */
	
	public TaskInfoBuilder<T> forJob(String moduleName) {
		this.moduleName = moduleName;
        return this;
    }

    /**
     * Set the module name and method which should be fired
     * 
     * @param moduleName the name of the job to fire.
     * @param methodName the name of the method to fire.
     * @return the updated TaskInfoBuilder
     * @see TaskInfo#getModuleName()
     * @see TaskInfo#getMethodName()
     */
	
	public TaskInfoBuilder<T> forJob(String moduleName, String methodName) {
		this.moduleName = moduleName;
		this.methodName = methodName;
        return this;
    }
	
	 /**
     * Set the time the TaskInfo should start at - the trigger may or may
     * not fire at this time - depending upon the schedule configured for
     * the TaskInfo.  However the TaskInfo will NOT fire before this time,
     * regardless of the TaskInfo's schedule.
     *  
     * @param start the start time for the TaskInfo.
     * @return the updated TaskInfoBuilder
     * @see TaskInfo#getStart()
     * @see DateBuilder
     */
	public TaskInfoBuilder<T> startAt(Date start) {
		this.startTime = start;
        return this;
    }
	
    /**
     * Set the given (human-meaningful) description of the TaskInfo.
     * 
     * @param info the description for the Trigger
     * @return the updated TriggerBuilder
     * @see TaskInfo#getInfo()
     */
	public TaskInfoBuilder<T> withInfo(String info) {
		this.info = info;
        return this;
    }
	
	 /**
     * Set the {@link Interval} that will be used to define the 
     * TaskInfo's schedule.
     *  
     * @param interval the repeat interval value to use.
     * @param unit the repeat interval units to use.
     * 
     * 
     * @return the updated TaskInfoBuilder
     */
	public TaskInfoBuilder<T> withSchedule(long interval, String unit)
	{
		this.interval = interval;
		this.unit = unit;
        return this;
	}

	 /**
     * Set the {@link Interval} that will be used to define the 
     * TaskInfo's schedule.
     *  
     * @param interval the repeat interval value in ms to use.
     * 
     * 
     * @return the updated TaskInfoBuilder
     */
	public TaskInfoBuilder<T> withSchedule(long millis)
	{

		if (TimeUnit.MILLISECONDS.toDays(millis) > 0)
		{
			this.interval = TimeUnit.MILLISECONDS.toDays(millis);
			this.unit = IntervalTaskInfo.UNIT_DAY;
		}
		else if (TimeUnit.MILLISECONDS.toHours(millis) > 0)
		{
			this.interval = TimeUnit.MILLISECONDS.toHours(millis);
			this.unit = IntervalTaskInfo.UNIT_HOUR;
		}
		else if (TimeUnit.MILLISECONDS.toMinutes(millis) > 0)
		{
			this.interval = TimeUnit.MILLISECONDS.toMinutes(millis);
			this.unit = IntervalTaskInfo.UNIT_MINUTE;
		}
		else if (TimeUnit.MILLISECONDS.toSeconds(millis) > 0)
		{
			this.interval = TimeUnit.MILLISECONDS.toSeconds(millis);
			this.unit = IntervalTaskInfo.UNIT_SECOND;
		}

        return this;
	}
	 /**
     * Set the cron expression that will be used to define the 
     * TaskInfo's schedule.
     *  
     * @param cronExpr the cron expression to use.
     * @return the updated TaskInfoBuilder
     */
	public TaskInfoBuilder<T> withSchedule(String cronExpr)
	{
		this.cronExpr = cronExpr;
        return this;
	}
	
	 /**
     * Specify whether the TaskInfo should be saved to database
     *  
     * @param persist the flag, if set false, the task will not be saved to database 
     * (the task is saved by default).
     * @return the updated TaskInfoBuilder
     */
	public TaskInfoBuilder<T> persistTask(boolean persist)
	{
		this.persist = persist;
        return this;
	}
	
	 /**
     * Set xml config for TaskInfo and add it to task arguments map with the "config" key
     * 
     * @param xml_text xml config
     * @return the updated TaskInfoBuilder
     */
	public TaskInfoBuilder<T> usingXmlConfig(String xml_text)
	{
		this.xmlConf = xml_text;

		this.argMap.put( MAP_ARG_CONFIG, xml_text);
        return this;
	}
	
	private static String generateUniqueTaskId()
	{
		return UUID.randomUUID().toString();
	}

	public Date getLastExecTime() {
		return lastExecTime;
	}

	public void setLastExecTime(Date lastExecTime) {
		this.lastExecTime = lastExecTime;
	}
}
