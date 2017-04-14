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
package com.aplana.dbmi.action;

import java.io.InputStream;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

/**
 * {@link Action} implementation used to convert 
 * material file attached to given {@link Card} object.
 * Returns byte[] object representing material being converted into pdf.
 */
public class ConvertToPdf implements ObjectAction<InputStream> {
	
	private static final long serialVersionUID = 3L;

	private Material material;
	private int versionId = Material.CURRENT_VERSION;
	
	/**
	 * Gets material that should be converted
	 * @return material to convert
	 */
	public Material getMaterial() {
		return material;
	}

	/**
	 * Sets material that should be converted
	 * @param material to be converted
	 */
	public void setMaterial(Material material) {
		if (material == null)
			throw new IllegalArgumentException("Not a card ID");
		this.material = material;
	}

	/**
	 * @see Action#getResultType()
	 */
	public Class<?> getResultType() {
		return InputStream.class;
	}

	/**
	 * @see ObjectAction#getObjectId()
	 */
	public ObjectId getObjectId() {
		return getMaterial().getCardId();
	}
}