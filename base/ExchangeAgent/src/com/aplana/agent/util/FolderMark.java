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
package com.aplana.agent.util;

public enum FolderMark {
	LOCK_FILE("folder.lock"),
	QUEUE_FILE("folder.queued"),
	DELME_FILE("folder.delme");
	
	final String value;
	
	FolderMark(String value) {
		this.value = value;
	}
	
	public static boolean isValueOf(String v) {
        for (FolderMark c: FolderMark.values()) {
            if (c.value.equals(v)) {
                return true;
            }
        }
        return false;
    }
	
	public String toString() {
		return value;
	}
}