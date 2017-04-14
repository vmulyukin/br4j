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
package com.aplana.dbmi.module.masssave;

import com.aplana.dbmi.ConfigService;
import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.MassSave;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.task.AbstractTask;
import org.apache.commons.io.FileUtils;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class MassSaveCardTask extends AbstractTask {
	private static final long serialVersionUID = 1L;
	private static Boolean working = false;
	private static String CONFIG_FILE = "dbmi/massSaveTask/query.sql";
	
	public MassSaveCardTask() {
		setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey("businessBeanFactory");
	}
	
	public void process(Map<?, ?> parameters) {
        synchronized (MassSaveCardTask.class) {
            if (working) {
                logger.warn("Process MassSaveCardTask is already working. Skipping.");
                return;
            }
            working = true;
        }
        logger.info("TASK MassSaveCardTask started");
        try {
        	ConfigService config = Portal.getFactory().getConfigService();
        	URL url = config.getConfigFileUrl(CONFIG_FILE);
        	File f = FileUtils.toFile(url);
        	if (!f.exists()) {
        		logger.error("Config with sql query not exists! Exit");
        		return;
        	}
        	String sql = FileUtils.readFileToString(f);
        	serviceBean.setSessionId(""+Thread.currentThread().getId());
        	List<ObjectId> ids = serviceBean.doAction(new MassSave(sql));
        	logger.info("Found " + ids.size() + " cards");
        	
    		for (int i = 0; i<ids.size(); i++) {
    			ObjectId id = ids.get(i);
    			logger.info("["+i+"] Starting save for " + id);
    			
    			//������ ������� ������ ����������� ����� (�������)
    			//manager.cleanAccessListByCardAndSourceAttrs(id);
    			//manager.updateAccessToCard(Collections.singletonList(id), null);
    			
    			//� ���� ������� ��������� �������������� �������� (��������, �� ���������)
    			Card card = serviceBean.getById(id);
    			
    			serviceBean.doAction(new LockObject(id));
    			
    			try {
    				serviceBean.saveObject(card);
    			} catch (Exception e) {
    				logger.error("Error save card " + id, e);
    			} finally {
    				serviceBean.doAction(new UnlockObject(id));
    			}
    		}
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            synchronized (MassSaveCardTask.class) {
                working = false;
                logger.info("TASK MassSaveCardTask finished");
            }
        }
    }
	
}
