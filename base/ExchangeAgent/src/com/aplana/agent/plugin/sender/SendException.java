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
package com.aplana.agent.plugin.sender;


public class SendException extends Exception {
    
    /**
     * Indicates weither the send should be retried.
     */
    private boolean shouldRetry;
    
    public SendException() {
    }

    public SendException(String message) {
        super(message);
    }

    public SendException(String message, boolean shouldRetry) {
        super(message);
        this.shouldRetry = shouldRetry;
    }
    
    public SendException(String message, Throwable cause) {
        super(message, cause);
    }

    public SendException(String message, Throwable cause, boolean shouldRetry) {
        super(message, cause);
        this.shouldRetry = shouldRetry;
    }

    public SendException(Throwable cause) {
        super(cause);
    }

    public SendException(Throwable cause, boolean shouldRetry) {
        super(cause);
        this.shouldRetry = shouldRetry;
    }
    
    public boolean isShouldRetry() {
        return shouldRetry;
    }
        
}
