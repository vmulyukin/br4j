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
import org.aplana.br4j.dynamicaccess.db_export.DbException;
import org.aplana.br4j.dynamicaccess.db_export.objects.ConnectionFactory;
import org.aplana.br4j.dynamicaccess.db_export.objects.TemplateDao;
import org.aplana.br4j.dynamicaccess.xmldef.AccessConfig;
import org.aplana.br4j.dynamicaccess.xmldef.Template;

public class DoLoadTemplateList extends SwingWorker<Exception, Void> {

	protected final Log logger = LogFactory.getLog(getClass());
	private DataSource dataSource;
	private Template[] templates;

	public DoLoadTemplateList(String url, String username, String password) throws DbException {
    	dataSource = ConnectionFactory.getDataSource(url, username, password);
    }

    public DataSource getDataSource() {
		return dataSource;
	}

    public Template[] getTemplates() {
    	return templates;
    }

	@Override
	protected Exception doInBackground() throws Exception {
		try {
            TemplateDao templateDao = new TemplateDao(getDataSource());
            List<Template> templateList = templateDao.getAllTempaltes();
            templates = templateList.toArray(new Template[templateList.size()]);
		} catch (Exception e) {
            logger.error(e.getMessage());
            return e;
        }
		return null;
	}
}