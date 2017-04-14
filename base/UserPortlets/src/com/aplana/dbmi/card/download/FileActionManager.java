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
package com.aplana.dbmi.card.download;

import java.io.InputStream;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.card.Parametrized;
import com.aplana.dbmi.card.download.actionhandler.FileActionHandler;
import com.aplana.dbmi.card.download.descriptor.FileActionDescriptor;
import com.aplana.dbmi.card.download.descriptor.FileActionDescriptorReader;
import com.aplana.dbmi.card.download.descriptor.FileActionVariantDescriptor;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.web.tag.util.ClassUtils;

/**
 * class that help to extract dispatched file action handler
 */
public class FileActionManager {

    protected Log logger = LogFactory.getLog(getClass());

    private final static String CONFIG_FILE = "/com/aplana/dbmi/card/download/descriptor/downloadAction.xml";

    private DataServiceBean dataServiceBean;
    private FileActionDescriptor fileActionDescriptor;

    public FileActionManager(DataServiceBean dataServiceBean) {
        this.dataServiceBean = dataServiceBean;
    }

    /**
     * returns stream with config action handler data
     * @return stream with config action handler data
     */
    protected InputStream getConfigStream() {
        return getClass().getResourceAsStream(CONFIG_FILE);
    }

    /**
     * try to create new instance of the FileActionDescriptorReader
     * @return FileActionDescriptorReader
     * @throws XPathExpressionException if fail process FileActionDescriptor data
     */
    protected FileActionDescriptorReader getNewActionDescriptorReader() throws XPathExpressionException {
        return new FileActionDescriptorReader();
    }

    /**
     * Reads descriptor data from configuration file
     */
    protected FileActionDescriptor readActionDescriptor()
            throws DataException {

        FileActionDescriptor result;

        try {
            final InputStream stream = getConfigStream();
            FileActionDescriptorReader reader = getNewActionDescriptorReader();
            result = reader.read(stream);
        } catch (Exception e) {
            logger.error("Failed to read config file", e);
            throw new DataException();
        }

        return result;
    }

    /**
     * try to find configuration of the action by action id and create new instance of the FileActionHandler
     * @param actionId action id that used to search action handler in the configuration file
     * @return FileActionHandler
     * @throws DataException if fail create or find FileActionHandler from config file
     */
    public final FileActionHandler createInstance(String actionId) throws DataException {
        final FileActionVariantDescriptor d =
                getFileActionDescriptor().getActionVariantDescriptorByActionId(actionId);
        if (d == null) {
            logger.error("Can not find FileActionVariantDescriptor by action id=" + actionId);
            throw new DataException();
        }

        FileActionHandler result;
        try {
            result = getNewInstanceActionHandler(d.getActionClass());
        } catch (Exception e) {
            logger.error("Failed to instantiate ActionHandler class:" + e.getMessage(), e);
            throw new DataException();
        }

        if (result instanceof Parametrized) {
            final Parametrized p = (Parametrized) result;
            for (Map.Entry<String, String> entry : d.getParameters().entrySet()) {
                p.setParameter(entry.getKey(), entry.getValue());
            }
        } else if (d.getParameters() != null && d.getParameters().size() > 0) {
            logger.warn(result.getClass().getName() + " is not Parametrized descendant. Parameters will be ignored");
        }

        result.setServiceBean(getDataServiceBean());
        return result;
    }

    protected FileActionHandler getNewInstanceActionHandler(String actionClass) throws DataException {

        Class handlerClass;
        try {
            handlerClass = ClassUtils.forName(actionClass);
        } catch (ClassNotFoundException e) {
            logger.error("Can nof find class by name " + actionClass, e);
            throw new DataException();
        }

        FileActionHandler handler;
        try {
            handler = (FileActionHandler) handlerClass.newInstance();
        } catch (InstantiationException e) {
            logger.error("Can nof instantiate class  " + actionClass, e);
            throw new DataException();
        } catch (IllegalAccessException e) {
            logger.error("Can nof instantiate class  " + actionClass, e);
            throw new DataException();
        }

        return handler;

    }

    public DataServiceBean getDataServiceBean() {
        return dataServiceBean;
    }

    public void setDataServiceBean(DataServiceBean dataServiceBean) {
        this.dataServiceBean = dataServiceBean;
    }

    /**
     * cerate new v and init his properties
     * @param dataServiceBean DataServiceBean
     * @return FileActionManager with init properties
     * @throws DataException if fail read FileActionManager from configuration
     */
    public static FileActionManager getInstance(DataServiceBean dataServiceBean) throws DataException{
        FileActionManager result = new FileActionManager(dataServiceBean);
        result.setFileActionDescriptor(result.readActionDescriptor());
        return result;
    }

    public FileActionDescriptor getFileActionDescriptor() {
        return fileActionDescriptor;
    }

    public void setFileActionDescriptor(FileActionDescriptor fileActionDescriptor) {
        this.fileActionDescriptor = fileActionDescriptor;
    }
}
