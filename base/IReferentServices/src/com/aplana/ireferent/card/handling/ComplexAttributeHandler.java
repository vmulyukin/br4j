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
package com.aplana.ireferent.card.handling;

import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.value.controllers.ValueController;
import com.aplana.ireferent.value.controllers.ValuePart;

public class ComplexAttributeHandler extends AttributeHandlerExtension {

    private ValueController valueController;

    public ComplexAttributeHandler(ValueController valueController) {
	this.valueController = valueController;
    }

    @Override
    public void setAttributeValue(Object value) throws IReferentException {
	Object oldValue = super.getAttributeValue();
	valueController.setValue(oldValue);
	valueController.appendValue((ValuePart) value);
	super.setAttributeValue(valueController.getValue());
    }

    @Override
    public Object getAttributeValue() throws IReferentException {
	Object attributeValue = super.getAttributeValue();
	valueController.setValue(attributeValue);
	return getLastPart(valueController);
    }

    private ValuePart getLastPart(ValueController controller)
	    throws IReferentException {
	ValuePart[] parts = controller.getParts();
	if (parts.length > 0) {
	    return parts[parts.length - 1];
	}
	return null;
    }
}
