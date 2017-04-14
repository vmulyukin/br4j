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
/**
 * 
 */
package com.aplana.ireferent.card;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.ireferent.IReferentException;

/**
 * @author PPanichev
 *
 */
public class FileCard {
    
    private Integer length = null;
    private String name = null;
    private String url = null;
    private int version;
    private InputStream data = null;
    private String id = null;
    private DataServiceBean serviceBean = null;
    private Material material = null;
    
    private Log logger = LogFactory.getLog(getClass());
    
    public FileCard(DownloadFile downloadFile, DataServiceBean serviceBeanFC) {
	this.serviceBean = serviceBeanFC;
	try {
	    material = (Material) serviceBeanFC.doAction(downloadFile);
	} catch (DataException ex) {
	    logger.error("Error during downloading of material", ex);
	} catch (ServiceException ex) {
	    logger.error("Error during downloading of material", ex);
	}
	if (material != null) {
	    id = material.getCardId().getId().toString();
	    length = material.getLength();
	    name = material.getName();
	    url = material.getUrl();
	    version = material.getVersionId();
	    data = material.getData();
	    logger.info("Create object FileCard with current parameters: "
			+ getParameterValuesLog());
	} else
	    logger.info("com.aplana.ireferent.card.FileCard notMaterial. FileCard: " + getParameterValuesLog());
    }

    public long getCardId() throws IReferentException {
	if (id != null ) return Long.parseLong(id);
		throw new IReferentException("com.aplana.ireferent.card.FileCard.notFound");
    }

    protected String getParameterValuesLog() {
	StringBuilder logBuilder = new StringBuilder();
	logBuilder.append(String.format("id='%s', ", id));
	return logBuilder.toString();
    }

    /**
     * @return the length
     */
    public Integer getLength() {
        return this.length;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return this.version;
    }

    /**
     * @return the data
     */
    public InputStream getData() {
        return this.data;
    }

    /**
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return the serviceBean
     */
    public DataServiceBean getServiceBean() {
        return this.serviceBean;
    }
}
