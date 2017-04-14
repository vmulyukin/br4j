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

import java.util.EventListener;

/**
 * An exception listener is notified of internal {@link Task} exceptions.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public interface TaskExceptionListener extends EventListener {
	/**
	 * This method is called when an exception was caught when performing a task's
	 * <code>doInBackground</code> method.
	 *
	 * @param source  the object that caught the exception
	 * @param t       the <code>Throwable</code> that was caught
	 */
	public void exceptionThrown(Object source, Throwable t);
}
