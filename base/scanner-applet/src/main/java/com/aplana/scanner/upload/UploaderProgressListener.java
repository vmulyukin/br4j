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
package com.aplana.scanner.upload;

import java.util.Observable;
import java.util.Observer;

/**
 * {@link ProgressListener} 
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class UploaderProgressListener extends Observable implements ProgressListener {
	private long bytesToSend;
	
	/**
	 * Constructs a listener instance.
	 *
	 * @param observer     the <code>Observer</code> object to be informed of upload progress 
	 * @param bytesToSend  the number of bytes to be sent to the server
	 */
	public UploaderProgressListener(Observer observer, long bytesToSend) {
		// for some reson, twice much bytes is sent to server, so this is ugly patch
		this.bytesToSend = bytesToSend * 2;
		this.addObserver(observer);
	}

	/* (non-Javadoc)
	 * @see com.aplana.scanner.upload.ProgressListener#progressUpdate(long)
	 */
	public void progressUpdate(long progress) {
		this.setChanged();
		this.notifyObservers((int)(progress / bytesToSend * 100));
	}
}
