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
package com.aplana.dbmi.task;

import java.util.concurrent.TimeUnit;

public class IntervalTaskInfo extends TaskInfo
{
	
	public static final String UNIT_SECOND = "sec";
	public static final String UNIT_MINUTE = "minutes";
	public static final String UNIT_HOUR = "hours";
	public static final String UNIT_DAY = "days";

	private long interval;
	private String unit;
	

	public IntervalTaskInfo(long interval, String unit)
	{
		super();
		this.interval = interval;
		this.unit = unit;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}
	
	public void setUnit(String unit) {
		this.unit = unit;
	}

	public long getInterval() {
		return interval;
	}

	public String getUnit() {
		return unit;
	}
	
	/**
	 * Returns the repeat interval in ms
	 * 

	 * @return repeat interval, ms
	 */
	public long getRepeatIntervalMs()
	{
		long intervalMs = 0;
		
		if (unit.equals(UNIT_SECOND))
			intervalMs = TimeUnit.SECONDS.toMillis(interval);
		else if (unit.equals(UNIT_MINUTE))
			intervalMs = TimeUnit.MINUTES.toMillis(interval);
		else if (unit.equals(UNIT_HOUR))
			intervalMs = TimeUnit.HOURS.toMillis(interval);
		else if (unit.equals(UNIT_DAY))
			intervalMs = TimeUnit.DAYS.toMillis(interval);

		return intervalMs;
	}

	public String getRepeatIntervalStr() {
		return interval + " " + unit;
	}

	public String getCronExpr() {
		return "---";
	}
}
