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
package com.aplana.dbmi.action;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GetCardEventHistory extends GetCardHistory {
	private static final long serialVersionUID = -2430535645198447785L;	
	private FilterEvent filterEvent;
	
	public FilterEvent getFilterEvent() {
		return this.filterEvent;
	}

	public void setFilterEvent(FilterEvent filterEvent) {
		this.filterEvent = filterEvent;
	}
	
	public static class FilterEvent {
		Map<String, String> events = new HashMap<String, String>();
		
		public void addEvent(String event, String value) {
			if (event == null || "".equals(event)){
				throw new IllegalArgumentException("Action should be non-empty string");
			}
			if (!events.containsKey(event)){
				events.put(event, event);
			}
		}
		
		public Map<String, String> getEvents() {
			return Collections.unmodifiableMap(events);
		}
	}
}
