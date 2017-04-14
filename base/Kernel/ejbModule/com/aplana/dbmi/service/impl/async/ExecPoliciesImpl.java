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
package com.aplana.dbmi.service.impl.async;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.InfoMessage;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.ObjectNotLockedException;
import com.aplana.dbmi.service.impl.async.QueriesQueue.RunMode;

/**
 * {@link ExecPolicy} implementations
 * 
 */
public class ExecPoliciesImpl {

	public abstract static class AbstractExecPolicy implements ExecPolicy {

		private static final long serialVersionUID = -7531037520503393514L;
		protected QueryContainer query;
		protected QueriesQueue<QueryContainer> queue;
		protected final Log logger = LogFactory.getLog(getClass());
		protected ExecutionException retryReason;

		@Override
		public void setQuery(QueryContainer q) {
			this.query = q;
		}

		@Override
		public void setQueue(QueriesQueue<QueryContainer> q) {
			this.queue = q;
		}

		@Override
		public boolean checkApplicability() {
			return queue != null && query != null;
		}
		
		public void setRetryReason(ExecutionException e) {
			this.retryReason = e;
		}
		
		public InfoMessage getRetryReason() {
			InfoMessage msg = new InfoMessage(query.getQuery().getLogEntry());
			msg.setMessage(this.retryReason.getCause().getMessage());
			return msg;
		}
	}

	/**
	 * Standard retry policy similar to Docflow AsyncVisaTrigger retrying.
	 * 
	 */
	public static class StandartRetryPolicy extends AbstractExecPolicy {

		private static final long serialVersionUID = -1433256610554977215L;
		private int priorityChange = 0;
		private int retryCount = 15;
		private long waitingTime = 20000;
		private static final Log logger = LogFactory
				.getLog(StandartRetryPolicy.class);

		/**
		 * Sets the priority delta before retrying
		 * 
		 * @param priorityChange
		 */
		public void setPriorityChange(int priorityChange) {
			this.priorityChange = priorityChange;
		}

		/**
		 * Sets retry chances value
		 * 
		 * @param retryCount
		 */
		public void setRetryCount(int retryCount) {
			this.retryCount = retryCount;
		}

		/**
		 * Sets waiting time (in ms) before retrying
		 * 
		 * @param waitingTime
		 */
		public void setWaitingTime(long waitingTime) {
			this.waitingTime = waitingTime;
		}

		@Override
		public void performPolicy() {
			query.renew();
			Timer t = new Timer();
			TimerTask task = new TimerTask() {
				public void run() {
					retryCount--;
					query.setPriority(query.getPriority() + priorityChange);
					try {
						queue.addQuery(query, RunMode.IMMEDIATE);
					} catch (DataException e) {
						logger.error("Error in performing policy", e);
					}
				}
			};
			t.schedule(task, waitingTime);
		}

		@Override
		public boolean checkApplicability() {
			if (!super.checkApplicability())
				return false;
			if (retryCount > 0)
				return true;
			else
				return false;
		}

		@Override
		public String toString() {
			return "[Retry Policy: Retry count = " + retryCount
					+ ", Waiting time = " + waitingTime
					+ ", Priority changing = " + priorityChange + "]";
		}

	}

	/**
	 * Standart reject policy
	 *
	 */
	public static class StandartRejectPolicy extends AbstractExecPolicy {

		private static final long serialVersionUID = -7553304373941657991L;

		@Override
		public void performPolicy() {
			if (logger.isErrorEnabled()) {
				logger.error("Dropping during performing policy \n -->  Query: "
						+ query.getClassName()
						+ ". Event: "
						+ query.getQuery().getEvent()
						+ ". ObjectId: "
						+ query.getQuery().getEventObject() + ".");
			}
//			query.getLockObject() TODO ���������� ���������� ��� �������������� ���� ��������
			queue.failed(query);

		}

		@Override
		public boolean checkApplicability() {
			return true;
		}

	}

	/**
	 * Iteration policy. Retries task N times every T1 seconds, and then retries N times every T2 seconds (T2 > T1)
	 */
	public static class IterationRetryPolicy extends AbstractExecPolicy {

		private static final long serialVersionUID = 2L;
		private int priorityChange = 0;
		private int retryCount = 15;
		private long waitingTime = 20000;
		private int iterationCount = 2;		// ���������� ��������
		private int iterationFactor = 50;	// �����������, �� ������� ���������� �������� �� ������ ��������� �������� 
		
		private long curIterWaitingTime = waitingTime;
		private int curIteration = 0;
		private int curIterRetryCount;
		private static final Log logger = LogFactory
				.getLog(IterationRetryPolicy.class);

		/**
		 * Sets the priority delta before retrying
		 * 
		 * @param priorityChange
		 */
		public void setPriorityChange(int priorityChange) {
			this.priorityChange = priorityChange;
		}

		/**
		 * Sets retry chances value for every iteration
		 * 
		 * @param retryCount
		 */
		public void setRetryCount(int retryCount) {
			this.retryCount = retryCount;
		}

		/**
		 * Sets waiting time (in ms) before retrying
		 * 
		 * @param waitingTime
		 */
		public void setWaitingTime(long waitingTime) {
			this.waitingTime = waitingTime;
		}

		/**
		 * Sets count of iteration for retrying
		 * 
		 * @param iterationCount
		 */
		public void setIterationCount(int iterationCount) {
			this.iterationCount = iterationCount;
		}

		/**
		 * Sets count of iteration for retrying
		 * 
		 * @param iterationFactor
		 */
		public void setIterationFactor(int iterationFactor) {
			this.iterationFactor = iterationFactor;
		}

		@Override
		public void performPolicy() {
			query.renew();
			Timer t = new Timer();
			TimerTask task = new TimerTask() {
				public void run() {
					curIterRetryCount--;
					query.setPriority(query.getPriority() + priorityChange);
					try {
						queue.addQuery(query, RunMode.IMMEDIATE);
					} catch (DataException e) {
						logger.error("Error in performing policy", e);
					}
				}
			};
			t.schedule(task, curIterWaitingTime);
		}

		@Override
		/**
		 * �������� �� ����������� ������� �������:
		 * ������������ ����������� ������� � ����������� ��������
		 * ���������� ������� �� ������ �������� ���������, � ��� �������� �� ������ �������� - ��� �������������� ��������, ���������� �� ����������� ��������
		 * 
		 * ������ �������� ����������� ������ � ������ ������, ��������� � ������������ ��������
		 */
		public boolean checkApplicability() {
			if (!super.checkApplicability())
				return false;
			
			// ��������� ������ ������ � ������ ����\��������. ������
			if (!(retryReason.getCause() instanceof ObjectNotLockedException) && 
				!(retryReason.getCause() instanceof ObjectLockedException)) {
				return false;
			}
			
			// ���� ������ �����, �� ����� �������� = 1
			if (curIteration==0){
				curIteration++;
				curIterRetryCount = retryCount;				// ���������� ������� �� ������ �������� = ���������� �������, �������� � ����������
				curIterWaitingTime = waitingTime;			// �������� ������� ������� �� ������� �������� = �������������� ��������, �������� � ����������
			}

			if (curIterRetryCount > 0)						// ���� ���������� ������� �� ��� ��� > 0, �� ��� ��� ����� ���������
				return true;
			else {											// �����
				if (curIteration++ < iterationCount){		// ���� ����� ������� �������� < �� ����������, �� ����� ����������� 
					curIterRetryCount = retryCount;			// ���������� ������� �� ����� �������� = ���������� �������, �������� � ����������
					curIterWaitingTime *= iterationFactor;	// �������� ������� ������� �� ����� �������� = �������� �� ������� ��������, ���������� �� ����������� �������� 
					return true;							// � �������, ��� ��� ��� ����� ������ ���������
				} else										// ���� ����� ������� �������� >= �� ����������, �� ��, ��� ������� ���������
					return false;
			}
		}

		@Override
		public String toString() {
			return "[Retry Policy: Retry count = " + retryCount
					+ ", Waiting time = " + waitingTime
					+ ", Priority changing = " + priorityChange  
					+ ", Iteration count = " + iterationCount
					+ ", Iteration factor = " + iterationFactor + "]";
		}

	}
}
