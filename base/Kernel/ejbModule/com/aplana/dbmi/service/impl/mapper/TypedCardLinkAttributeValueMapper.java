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
package com.aplana.dbmi.service.impl.mapper;

import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.workstation.TypedCardLinkValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Represents  attribute value mapper for {@link com.aplana.dbmi.model.TypedCardLinkAttribute}
 *
 * @author skashanski
 */

public class TypedCardLinkAttributeValueMapper extends
        AbstractAttributeValueMapper<TypedCardLinkAttribute> {

    @Override
    protected TypedCardLinkAttribute createAttribute() {

        return new TypedCardLinkAttribute();
    }


    @Override
    protected void setValue(TypedCardLinkAttribute attr, Object value) {

        Collection linkedIds = (Collection) value;
        Iterator iterator = linkedIds.iterator();

        while (iterator.hasNext()) {
            TypedCardLinkValue tclValue = (TypedCardLinkValue) iterator.next();
            Long typeId = null;
            // not all TypedCardLinkAttributes contain reference values now 
            if (tclValue.getReferenceValue() != null) {
            	typeId = (Long) tclValue.getReferenceValue().getId().getId();
            }
            attr.addType(tclValue.getCardId(), typeId);
        }
        // todo it seems to be one of possible type
        attr.setReference( new ObjectId(Reference.class, "JBR_REF_TYPECLINK"));

    }


}
