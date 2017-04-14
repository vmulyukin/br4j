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
package com.aplana.scanner.task;

import java.util.ArrayList;
import java.util.List;

import org.jdesktop.swingworker.SwingWorker;

/**
 * A lengthy GUI-interacting task. 
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public abstract class Task<T, V> extends SwingWorker<T, V> {
	private Throwable throwable;
	private List<TaskExceptionListener> listeners = new ArrayList<TaskExceptionListener>();
	
	/* (non-Javadoc)
	 * @see org.jdesktop.swingworker.SwingWorker#done()
	 */
	@Override
	protected void done() {
		if (throwable != null) {
			for (TaskExceptionListener listener : listeners)
				listener.exceptionThrown(this, throwable);
		}
	}
	
	/**
	 * Sets a <code>Throwable</code> to report it to any registered exception listener in
	 * {@link #done()}.
	 *
	 * @param t  the <code>Throwable</code> to be reported
	 */
	protected void setThrowable(Throwable t) {
		this.throwable = t;
	}
	
	/**
	 * Gets the <code>Throwable</code> thrown in {@link #doInBackground()}.
	 */
	public Throwable getThrowable() {
		return this.throwable;
	}
	
	/**
	 * Adds an exception listener to the listener list. The same listener object may be added more
	 * than once, and will be called as many times as it is added. If listener is <code>null</code>,
	 * no exception is thrown and no action is taken.
	 *
	 * @param listener  the {@link TaskExceptionListener} to be added
	 */
	public void addExceptionListener(TaskExceptionListener listener) {
		if (listener != null)
			listeners.add(listener);
	}
	
	/**
	 * Removes an exception listener from the listener list. If a listener was added more than once
	 * to the same event source, it will be notified one less time after being removed.
	 * If listener is <code>null</code>, or was never added, no exception is thrown and
	 * no action is taken.
	 *
	 * @param listener  the {@link TaskExceptionListener} to be removed
	 */
	public void removeExceptionListener(TaskExceptionListener listener) {
		if (listener != null)
			listeners.remove(listener);
	}
}
