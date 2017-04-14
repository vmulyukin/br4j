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
package com.aplana.medo.gate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Options;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;

public class Starter implements Job{
    final static Log logger = LogFactory.getLog(Starter.class);
    static final String ROUTE_FILE_NAME = "conf/route.properties";
    static final String SCHEDULER_FILE_NAME = "conf/schedule.properties";

    
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
    	MEDOGate gate = MEDOGate.getInstance();
    	if (gate.isActive()){
    		logger.warn("Still in active phase.");
    		return;
    	}
		try {
			InputStreamReader isr = new InputStreamReader(
					new FileInputStream(new File(ROUTE_FILE_NAME)), Charset.forName("Cp1251"));
			Options ini = new Options(isr); 
			Properties options = new Properties();
			options.putAll(ini);
	    	gate.setProperties(options);
	    	gate.execute();

		} catch (FileNotFoundException e) {
			logger.error("File not found: "+ROUTE_FILE_NAME);
		} catch (InvalidFileFormatException e) {
			logger.error("File "+ROUTE_FILE_NAME+" has invalid format.", e);
		} catch (IOException e) {
			logger.error("IOException occured when reding file "+ROUTE_FILE_NAME, e);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SchedulerFactory sf = new StdSchedulerFactory();
		try {
				FileInputStream fis = new FileInputStream(new File(SCHEDULER_FILE_NAME));
				Properties props = new Properties();
				props.load(fis);
				String s = props.getProperty("period.seconds", "10");
				int period_seconds = Integer.parseInt(s);
				Starter.logger.info("MEDOGate will be scheduled every "+period_seconds+" seconds to run.");
				Scheduler sh = sf.getScheduler();
				sh.start();
				JobDetail jd = new JobDetail("Standalone periodic MEDOGate job", "NoGroup", Starter.class);
				Trigger trigger = TriggerUtils.makeSecondlyTrigger(period_seconds);
				trigger.setStartTime(new Date());
				trigger.setName("Simple periodic trigger");
				sh.scheduleJob(jd, trigger);
		} catch (FileNotFoundException e) {
			Starter.logger.error("File not found: "+SCHEDULER_FILE_NAME);
		} catch (SchedulerException e) {
			Starter.logger.error("Error in quartz scheduler:", e);
		} catch (IOException e) {
			Starter.logger.error("IO error when reading "+SCHEDULER_FILE_NAME);
		} catch (NumberFormatException e) {
			Starter.logger.error("Invalid number format in config file "+SCHEDULER_FILE_NAME);
		}
	}

}
