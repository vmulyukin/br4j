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
package com.aplana.medo.converters.cards;

import java.io.File;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.medo.cards.CardException;
import com.aplana.medo.cards.FileCard;
import com.aplana.medo.converters.ConverterException;

/**
 * FileCardLinkConverter is one of 'cardLink' converters (see
 * {@link CardConverter}). It create new card of 'file' type and return
 * 'attribute'DOM element (see
 * {@link #convert(org.w3c.dom.Document, org.w3c.dom.Element)} that represents
 * cardLink to one.
 */
public class FileCardLinkConverter extends CardConverter {

    public static final String WORKING_FOLDER_KEY = "working.folder";

    private File inFolder;

    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Creates a new instance from a properties configuration and a tag name of
     * input DOM element.
     *
     * @param properties -
     *                configuration that should contain additional property with
     *                {@link #WORKING_FOLDER_KEY} key that define directory
     *                where source file can be found on file system
     * @param name -
     *                tag name of input DOM element. Used to get concrete
     *                property from appropriate set of properties
     * @see CardConverter#CardLinkConverter(Properties, String)
     */
    public FileCardLinkConverter(Properties properties, String name) {
	super(properties, name);
    }

    /**
     * Perform processing of read values - create new card of 'file' type
     *
     * @throws ConverterException
     *
     * @see com.aplana.medo.converters.cards.CardConverter#processValues()
     * @see FileCard#createCard()
     */
    @Override
    protected long processValues() throws ConverterException {
	String fileName = getValueOfTagByKey("file.name");
	String fileDescription = getValueOfTagByKey("file.description");
	boolean isReference = Boolean
		.parseBoolean(getValueOfTagByKey("file.isReference"));
	String eds = getValueOfTagByKey("file.eds");

	String inFolderPath = getProperties().getProperty(WORKING_FOLDER_KEY);
	if (inFolderPath == null) {
	    logger.error(String.format(
		    "Properties should contain value with key='%s'",
		    WORKING_FOLDER_KEY));
	    throw new ConverterException();
	}
	inFolder = new File(inFolderPath);

	if (fileName.equals(""))
	    return -1;

	try {
	    FileCard fileCard;
	    if (isReference) {
		fileCard = new FileCard(fileName);
	    } else {
		fileCard = new FileCard(inFolder, fileName, fileDescription);
	    }
	    fileCard.setServiceBean(serviceBean);
	    fileCard.setEds(eds);
	    return fileCard.createCard();
	} catch (CardException ex) {
	    throw new ConverterException("jbr.medo.converter.file.creation",
		    new Object[] { name }, ex);
	}
    }
}
