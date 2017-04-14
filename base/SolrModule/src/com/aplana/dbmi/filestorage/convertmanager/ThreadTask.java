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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.filestorage.converters.PdfConverter;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;

public class ThreadTask {

	protected final Log logger = LogFactory.getLog(getClass());
	public final static String THREAD_NAME_PREFIX = "thread_converter_";

	//final private ExecutorService service = Executors.newFixedThreadPool(1); 
	private Thread threadService = null;
	private String threadTaskName = null;
	private long timestart = 0;
	Task task = null;
	PdfConverter pdfconverter=null;
	private boolean done = false;

	public ThreadTask(Task task, PdfConverter pdfconverter) {
		this.pdfconverter = pdfconverter;
		this.task = task;
		this.threadTaskName = generateTheadName(task);
	}

	public void start(){
		timestart = System.currentTimeMillis();
		threadService = new Thread(new WorkThread(), threadTaskName);
		threadService.start();
		//service.execute( new WorkThread() );
		return;
	}

	public Task getTask(){
		return task;
	}

	public long getTimeWorking(){
		return System.currentTimeMillis()-timestart;
	}
	
	public String getThreadTaskName(){
		return threadTaskName;
	}

	public boolean isDone(){
		return done;
	}

	void setDone(boolean flDone){
		this.done =  flDone;
	}
	
	private String generateTheadName(Task task){
		ObjectId cardId = task.getMaterial().getCardId();
		if(cardId==null){
			return THREAD_NAME_PREFIX+"anonym";
		}
		return THREAD_NAME_PREFIX+String.valueOf(cardId.getId());
	}

	public void stop(){
		try{
			pdfconverter.stop();
		}catch (Exception e) {
			logger.error("������ ��� ��������� ����������!!! "+e);
		}finally{
			threadService.interrupt();
			//service.shutdownNow();
		}		
	}

	private class WorkThread implements Runnable 
	{
		private Exception exception = null;

		public WorkThread() { }

		public void run() {
			try {
				task.setResult(null);
				exception = null;
				Thread.sleep(10);
				processQuery();
			} catch(Exception ex) {
				exception = ex;
			} finally {
				notifyEndProcess();
				setDone(true);
			}
		}

		void processQuery()throws DataException {
			task.setResult(pdfconverter.processQuery(task));
		}

		void notifyEndProcess() 
		{
			final EventListener[] listeners = task.getListEventListeners();
			if( listeners == null)
				return;
			for (EventListener  listener: listeners) {
				if (listener != null) {
					final Thread thread = newCallBackThread( listener, task, exception);
					thread.start();
				}
			}
		}

		private Thread newCallBackThread( final EventListener alistener, 
				final Task atask, final Exception ex)
		{
			final Runnable runn = new Runnable() 
			{
				public void run() {
					try {
						Thread.sleep(10);
						alistener.onEvent( atask, ex);
					} catch (Exception e) {
						logger.error( "problem at onEvent() detected for task "+ atask.toString(),ex );
					}
				}
			};
			return new Thread(runn);
		}
	}

}
