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
package com.aplana.ireferent.actions;

import com.aplana.ireferent.config.ConfigurationException;
import com.aplana.ireferent.types.WSObject;

public class SetArmFlagAction extends ChangeCardAction {

    @Override
    protected WSObject createObject() {
	WSObject obj = super.createObject();
	if (!(obj instanceof ArmViewed)) {
	    throw new ConfigurationException(
		    "Object for setting arm flag should implement "
			    + ArmViewed.class.getName());
	}

	ArmViewed armViewed = (ArmViewed) obj;
	armViewed.setArmViewed("1");
	return obj;
    }

    @Override
    protected boolean isConstructNewObject() {
	return true;
    }
}