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
package com.aplana.dbmi.module.docflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;

@Deprecated
public class AsyncVisaProcessor implements DocumentProcessor, BeanNameAware {
	protected Log logger = LogFactory.getLog(getClass());
	
	private DocumentManager manager;
	private DocumentProcessor processingBean;
	private ObjectId docId;
	private int maxRetries = 15;
	private int retryInterval = 20000;	// 10s
	
	public void setDocumentManager(DocumentManager manager) {
		this.manager = manager;
	}
	
	public void setDocumentId(ObjectId docId) {
		this.docId = docId;
	}

	public void setProcessingBean(DocumentProcessor processingBean) {
		this.processingBean = processingBean;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxReties) {
		this.maxRetries = maxReties;
	}

	public int getRetryInterval() {
		return retryInterval;
	}

	public void setRetryInterval(int retryInterval) {
		this.retryInterval = retryInterval;
	}

	public void process() {
		//final Thread parentThread = Thread.currentThread();
		manager.addDocument(docId, new Runnable() {
			public void run() {
				int retry = 0;
				try {
					Thread.sleep(5000);		// Give time for finishing calling transaction
					//��������� ������ ������ ����� ���� ��� �������������� ������������ �����
					/*while (parentThread.getState().equals(Thread.State.RUNNABLE)){
						Thread.sleep(1000);		// Give time for finishing calling transaction						
					}*/
				} catch (InterruptedException e) {
					logger.info("Startup pause interrupted", e);
					//***** what to dO?
				}
								
				processingBean.setDocumentId(docId);
				while (retry++ < getMaxRetries()) {
					logger.info("[" + getName() + "] Processing of document " + docId.getId() + " started" +
							(retry > 1 ? "; retry " + retry : ""));
					try {
						processingBean.process();
						break;
					} catch (DataException e) {
						logger.error("[" + getName() + "] "
								+ "(retry "+ retry +"/"+ getMaxRetries()+ ")"
								+" Processing of document " + docId.getId()
								+ " terminated with error", e);
						// proceed to the next retry
					}
					try {
						Thread.sleep(getRetryInterval());
					} catch (InterruptedException e) {
						logger.info("Pause interrupted", e);
						//***** what to do?
					}
				}
				logger.info("[" + getName() + "] Processing of document " + docId.getId() + " finished");
			}
		});
	}

	private String name;
	public void setBeanName(String name) { this.name = name; }
	public String getName() { return name; }
}
