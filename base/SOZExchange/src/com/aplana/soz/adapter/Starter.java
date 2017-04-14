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
package com.aplana.soz.adapter;

import com.aplana.soz.SOZConfigFacade;
import com.aplana.soz.SOZException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

public class Starter implements Job {
    final static Log logger = LogFactory.getLog(Starter.class);

    private final static SOZAdapter soz = new SOZAdapter();

    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        if (soz.isActive()) {
            logger.warn("Still in active phase.");
            return;
        }
        try {
            Result result = soz.runControlledExport();
            logger.info("Export finished: " + result);
        } catch (SOZException e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        SchedulerFactory sf = new StdSchedulerFactory();
        try {

            soz.initConfiguration();

            int periodSeconds = SOZConfigFacade.getInstance().getPeriod();
            logger.info("SOZAdapter will be scheduled every " + periodSeconds + " seconds to run.");
            Scheduler sh = sf.getScheduler();
            sh.start();
            JobDetail jd = new JobDetail("Standalone periodic SOZAdapter job", "NoGroup", Starter.class);
            Trigger trigger = TriggerUtils.makeSecondlyTrigger(periodSeconds);
            trigger.setStartTime(new Date());
            trigger.setName("Simple periodic trigger");
            sh.scheduleJob(jd, trigger);
        } catch (SchedulerException e) {
            logger.error("Error in quartz scheduler:", e);
        } catch (SOZException e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

}
