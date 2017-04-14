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

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ������ ��������, �������� � �������� �������
 * @author ���������
 *
 */
public class SimpleStat implements ConverterStatistic {
	
	

	private long averagems = 0;
	private long countexecutedtask = 0;
	private long counterrortask = 0;
	
	private final Log logger = LogFactory.getLog(getClass());

	/**
	 * @return total exec time in milliseconds
	 */
	public long getTotalTime_ms() {
		return  averagems * (countexecutedtask+counterrortask);
	}

	public long getAvgTime_ms() {
		return  averagems;
	}

	public long getTotalTasks() {
		return this.countexecutedtask + this.counterrortask;
	}

	public long getExecutedTasks() {
		return this.countexecutedtask;
	}

	public long getErrorTasks() {
		return this.counterrortask;
	}

	public void addData(Parameters parameter, long value) {
		
		switch (parameter) {
		case SUCCESS_ID:
			countexecutedtask= countexecutedtask + value;
			break;
			
		case ERROR_ID:
			counterrortask= counterrortask + value;
			break;
		
		case TIME_WORK_ID:
			long countTask =getTotalTasks();
			averagems= (getTotalTime_ms() + value)/(countTask==0?1:countTask);
			break;

		default:
			break;
		}		
	}
	
	public synchronized void logPrint(){
		logger.info(logText());
	}

	public synchronized String logText() {
		final StringBuilder sb = new StringBuilder("Statistics of "+ this.getClass().getSimpleName() ); 
		sb.append( MessageFormat.format("\n\t avg time ''{0}''' msec per task", getAvgTime_ms() ));
		sb.append( MessageFormat.format("\n\t total time ''{0}'' msec", getTotalTime_ms() ));
		sb.append( MessageFormat.format("\n\t task counters,  total/executed/error:  {0}/ {1}/ {2}' ",
				getTotalTasks(), getExecutedTasks(), getErrorTasks() ));
		return sb.toString();
	}

}
