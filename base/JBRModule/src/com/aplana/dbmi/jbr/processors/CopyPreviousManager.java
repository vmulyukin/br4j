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
package com.aplana.dbmi.jbr.processors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataException;

/**
 * Copies manager from one attribute to another inside the same card.
 * The types of the source and target attributes should be the same.
 * Used to store the history of previous managers of the card. This is required by the new system of access rules.
 *
 * @author atsvetkov
 */
public class CopyPreviousManager extends BaseCopyAttributeProcessor {

    @Override
    public Object process() throws DataException {
        super.process();

        final Card card = getCard();
        final ObjectId cardId = getCardId();
        if (cardId == null) {
            logger.warn("Impossible to copy attributes until card is saved -> exiting");
            return null;
        }
        PersonAttribute from = (PersonAttribute) card.getAttributeById(new ObjectId(PersonAttribute.class, attrcodeFrom));

        PersonAttribute to = (PersonAttribute) card.getAttributeById(new ObjectId(
                PersonAttribute.class, attrcodeTo));
        if (to == null) {
            to = new PersonAttribute();
            to.setId(attrcodeTo);
            to.clear();
            card.getAttributes().add(to);

        }
        // as PersonAttribute supports multiple values, just add new values in
        // this attribute
        addValueToPersonAttribute(to, from);

        logger.info("Previous managers were stored in attribute " + attrcodeTo + " of card " + cardId.getId());

        return null;
    }

    /**
     * Gets current manager ids stored in database for specified card in person attribute indicated by attrcodeFrom parameter.
     *
     * @param cardId id of card.
     * @return List of {@link Person}s
     */
    @SuppressWarnings("unchecked")
    private List<Person> getCurrentManagerIds(final ObjectId cardId) {
        List<Person> currentManagerIds = getJdbcTemplate().query(
                "Select number_value id FROM attribute_value WHERE attribute_code=? AND card_id = ?",
                new Object[]{attrcodeFrom, cardId.getId()},
                new int[]{Types.VARCHAR, Types.NUMERIC}, new ParameterizedRowMapper<Person>() {
                    public Person mapRow(ResultSet rs, int index) throws SQLException {
                        Person person = new Person();
                        person.setId(rs.getLong("id"));
                        return person;
                    }
                }
        );
        return currentManagerIds;
    }

    /**
     * Adds value to {@link PersonAttribute}.
     *
     * @param to   {@link PersonAttribute} to be modified.
     * @param from collection of {@link Person}s that will be added to {@link PersonAttribute}.
     */
    private void addValueToPersonAttribute(PersonAttribute to, PersonAttribute from) {
        Collection personsTo = to.getValues();
        if (personsTo == null) {
            to.clear();
            personsTo = to.getValues();
        }
        Collection<Person> personsFrom = (Collection<Person>) from.getValues();
        if (personsFrom != null) {
            for (Person person : personsFrom) {
                if (!personsTo.contains(person)) {
                    personsTo.add(person);
                }
            }
        }
    }

}
