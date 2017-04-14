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
package com.aplana.dbmi.filestorage.convertmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.filestorage.convertmanager.ConverterStatistic.Parameters;
import com.aplana.dbmi.storage.impl.PdfConvertorSettings;

/**
 * ���������� ����������� ������� �����������, ������� ���������� ��������������� � ��������� �����
 * @author ���������
 *
 */
public class ActiveTaskQueueController {
	
	final static long DEFAULT_QUOTA_MSEC = 15000;
	final static long DEFAULT_ACTIVE_QUOTA_LIMIT = 10;
	
	private long quotatime = DEFAULT_QUOTA_MSEC;
	private long activeTaskQueueLimit = DEFAULT_ACTIVE_QUOTA_LIMIT; 
	private List<ConverterStatistic> converterStatistics = null;
	
	
	private Map<Long, ThreadTask> activeTasks = new HashMap<Long, ThreadTask>();	
	
	private static ActiveTaskQueueController singleton = null;
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	private ActiveTaskQueueController(){
		converterStatistics= Collections.synchronizedList(new ArrayList<ConverterStatistic>());
		setQuotaTime( PdfConvertorSettings.getQuotaTime());
		setActiveQueueSize(PdfConvertorSettings.getActiveTaskQueueSize());
	}
	
	public static ActiveTaskQueueController getInstance(){
		
		if(singleton==null){
			singleton = new ActiveTaskQueueController();
		}		
		return singleton;
	}
	
	public boolean checkExecutionTask(Task task){
		if(task==null){
			return false;
		}
		ThreadTask threadTask = activeTasks.get(task.getMaterial().getCardId().getId());
		if(threadTask!=null){
			for (EventListener iel : task.getListEventListeners()) {
				threadTask.getTask().addIEventListener(iel);
			}
			return true;			
		}
		return false;
	}
	
	public void addThreadTask(ThreadTask threadTask){
		activeTasks.put((Long) threadTask.getTask().getMaterial().getCardId().getId(), threadTask);
	}
	
	public boolean checkAdd(){
		return tasteMakeSpace();
	}	
	
	private boolean tasteMakeSpace(){		
		clearDoneTask();
		if(activeTasks.keySet().size()<activeTaskQueueLimit){
			return true;
		}		
		
		Long key = searchLongTask();
		if(key==null){
			return false;
		}		
		
		ThreadTask threadTask = activeTasks.get(key);
		if(threadTask.getTimeWorking()>quotatime){
			threadTask.stop();			
			activeTasks.remove(key);
			logger.error(threadTask.getThreadTaskName()+" ERROR");
			addIndicatorValue(ConverterStatistic.Parameters.ERROR_ID, 1);
			addIndicatorValue(ConverterStatistic.Parameters.TIME_WORK_ID, threadTask.getTimeWorking());
			return true;
		}		
		return false;
	}	
	
	private Long searchLongTask(){
		long maxWorkingTime = 0;
		Long key =null;
		Iterator<Map.Entry<Long, ThreadTask>> iterator = activeTasks.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<Long, ThreadTask> threadTask = iterator.next();		
			if(threadTask.getValue().getTimeWorking()>maxWorkingTime){
				maxWorkingTime = threadTask.getValue().getTimeWorking();
				key = threadTask.getKey();
			}
		}
		return key;		
	}
	
	public synchronized int clearDoneTask(){
		int countRemove = 0;		
		long timeWork = 0; 
		Iterator<Map.Entry<Long, ThreadTask>> iterator = activeTasks.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<Long, ThreadTask> threadTask = iterator.next();
			if(threadTask.getValue().isDone()){
				iterator.remove();				
				timeWork+=threadTask.getValue().getTimeWorking();
				countRemove++;
				logger.info(threadTask.getValue().getThreadTaskName()+" SUCCESS");
				continue;
			}
		}
		addIndicatorValue(ConverterStatistic.Parameters.SUCCESS_ID, countRemove);	
		addIndicatorValue(ConverterStatistic.Parameters.TIME_WORK_ID, timeWork);
		return countRemove;
	}
	
	public synchronized void addIndicatorValue(Parameters indicatorId, long value){
		for(ConverterStatistic converterStatistic: converterStatistics){
			converterStatistic.addData(indicatorId, value);
		}		
	}
	
	public void setQuotaTime(String value)  {
		if (value != null && value.length() > 0) {
			try {
				this.quotatime=Long.valueOf(value);
				return;
			} catch(Exception ex) {
				logger.error("problem assigning quota time by value '"+ value+ "', default  "+ DEFAULT_QUOTA_MSEC+ " is used instead", ex);
			}
		}
		this.quotatime = DEFAULT_QUOTA_MSEC;
	}
	
	public void setActiveQueueSize(String value)  {
		if (value != null && value.length() > 0) {
			try {
				this.activeTaskQueueLimit=Long.valueOf(value);
				return;
			} catch(Exception ex) {
				logger.error("problem assigning queue size by value '"+ value+ "', default  "+ DEFAULT_ACTIVE_QUOTA_LIMIT+ " is used instead", ex);
			}
		}
		this.activeTaskQueueLimit = DEFAULT_ACTIVE_QUOTA_LIMIT;
	}
	
	public synchronized void addConverterStatistic(ConverterStatistic statistic){
		converterStatistics.add(statistic);		
	}
	
	public synchronized void removeConverterStatistic(ConverterStatistic statistic){
		converterStatistics.remove(statistic);		
	}
	
	int getActiveTasksSize() {
		return activeTasks.size();
	}
	
	long getActiveTasksLimit() {
		return activeTaskQueueLimit;
	}
	
	void setActiveTasksLimit(long l) {
		this.activeTaskQueueLimit = l;
	}
	
	String getActiveTasksWorkTime() {
		if (activeTasks.isEmpty()) {
			return "";
		}
		StringBuilder res = new StringBuilder("Thread name (with card id)\t|\tWork time (ms)\n");
        res.append("---------------------------------------------\n");
		Iterator<Map.Entry<Long, ThreadTask>> iterator = activeTasks.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<Long, ThreadTask> threadTask = iterator.next();		
			res.append(threadTask.getValue().getThreadTaskName())
			   .append("\t|\t").append(threadTask.getValue().getTimeWorking());
			res.append("\n");
		}
		return res.toString();
	}

}
