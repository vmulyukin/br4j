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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Future result of asynchronous query<br>
 * Methods of {@link Future} blocks for as long as the real result is not set by
 * the {@link com.aplana.dbmi.service.impl.async.QueriesPerformer performer}
 * 
 */
public class AsyncResult implements Future<Object> {

	private AsyncTask realFuture;
	private CountDownLatch latch = new CountDownLatch(1);

	public void setRealFuture(AsyncTask realFuture) {
		this.realFuture = realFuture;
		latch.countDown();
	}
	
	public boolean isSet() {
		return realFuture != null;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return realFuture.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return realFuture.isCancelled();
	}

	@Override
	public boolean isDone() {
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return realFuture.isDone();
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return realFuture.get();
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return realFuture.get(timeout, unit);
	}

}
