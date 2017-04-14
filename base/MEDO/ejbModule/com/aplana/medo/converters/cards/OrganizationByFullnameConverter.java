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
package com.aplana.medo.converters.cards;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.medo.cards.CardException;
import com.aplana.medo.cards.IedmsClientOrganizationCardHandler;
import com.aplana.medo.cards.OrganizationByFullNameCardHandler;
import com.aplana.medo.converters.ConverterException;

/**
 * <p>
 * OrganizationByFullnameConverter is one of 'cardLink' converters (see
 * {@link CardConverter}). It tries to find organization using its
 * name. Organization should be IEDMS client. In case if one was not found,
 * exception is thrown.
 * </p>
 * <p>
 * The 'attribute' DOM element (see
 * {@link #convert(org.w3c.dom.Document, org.w3c.dom.Element)} that represents
 * cardLink to one, is returned.
 * </p>
 */
public class OrganizationByFullnameConverter extends CardConverter {

	private Log logger = LogFactory.getLog(getClass());

    public OrganizationByFullnameConverter(Properties properties, String name) {
    	super(properties, name);
    }

    /**
     * Perform processing of read values - try to find organization according name value
     *
     * @throws ConverterException
     *
     * @see com.aplana.medo.converters.cards.CardConverter#processValues()
     * @see IedmsClientOrganizationCardHandler#getCardId()
     */
    @Override
    protected long processValues() throws ConverterException {
		String organizationFullName = getValueOfTagByKey("anyone.organization");

		long cardId = -1;

		try {
		    cardId = new OrganizationByFullNameCardHandler(organizationFullName).getCardId();
		} catch (CardException ex) {
		    // Exception handler is below (cardId should be equal -1)
		}

		if (cardId == -1) {
			//�� �������� �������� ����� ��������� ��-�� ������ ���
			logger.error("Unable to find organization with following fullname: " + organizationFullName);
		}

	return cardId;
    }
}
