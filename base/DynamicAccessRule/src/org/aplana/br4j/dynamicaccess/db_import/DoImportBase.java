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
package org.aplana.br4j.dynamicaccess.db_import;

import java.util.List;

import javax.sql.DataSource;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.db_export.CancelException;
import org.aplana.br4j.dynamicaccess.db_export.DbException;
import org.aplana.br4j.dynamicaccess.db_export.objects.ConnectionFactory;
import org.aplana.br4j.dynamicaccess.db_export.objects.TemplateDao;
import org.aplana.br4j.dynamicaccess.xmldef.*;

public class DoImportBase extends DoImportTemplate {

	private static final String TEMPLATE_NOTE = "������: ";

	protected final Log logger = LogFactory.getLog(getClass());
    private String currentTemplate;

    public DoImportBase(String url, String username, String password, AccessConfig ac) throws DbException {
    	super(url, username, password, ac);
    }

    @Override
    protected Exception doInBackground() throws Exception {
        if (config == null) {
        	throw new IllegalArgumentException("AccessConfig is null");
        }
        long start = System.currentTimeMillis();
        showProgressMonitor();

        try {
            TemplateDao templateDao = new TemplateDao(getDataSource());
            List<Template> templates = templateDao.getAllTempaltes();
            logger.debug("Templates count loaded: "  + templates.size() + " templates: " + templates);
            int index = 0;

            for(Template template : templates) {
            	if (isCancelled()) {
					throw new CancelException("������� �� ���� ����������� �������������");
				}
				setCurrentTemplate(TEMPLATE_NOTE + template.getName());
                setProgress( 100 * (index + 1) / templates.size()); // for progress monitor            	
				loadTemplateData(template, templateDao);
				logger.debug("Template loaded: " + template);
				index++;
            }   

            Long end = System.currentTimeMillis();
            logger.info("Loading from database was finished in: "  + (end - start) + " ms" );

        } catch (Exception e) {
            logger.error(e.getMessage());
            return e;
        }
        return null;
    }

	public String getCurrentTemplate() {
		return currentTemplate;
	}

	public void setCurrentTemplate(String currentTemplate) {
		this.currentTemplate = currentTemplate;
	}

	private void showProgressMonitor() {
		setProgress(1);
	}
}