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
package com.aplana.dbmi.action.file;

import java.awt.Image;
import java.util.List;

import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Stamp;


/**
 * Action used to copy material file (with PDF convertion and reg stamp applying) from one card to another
 * @author valexandrov
 */
public class MaterialToImageList implements ObjectAction<List<Image>> {
	private static final long serialVersionUID = 1L;
	
	private ObjectId cardId;

	private String locationString;

	public void setCardId(ObjectId cardId) {
		this.cardId = cardId;
	}

	@Override
	public Class<?> getResultType() {
		// TODO Auto-generated method stub
		return List.class;
	}

	@Override
	public ObjectId getObjectId() {
		// TODO Auto-generated method stub
		return cardId;
	}

	public void setLocationString(String localizationString) {
		this.locationString = localizationString;
		
	}

	public String getLocationString() {
		return locationString;
	}
}
